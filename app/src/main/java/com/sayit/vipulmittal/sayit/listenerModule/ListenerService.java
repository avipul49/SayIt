package com.sayit.vipulmittal.sayit.listenerModule;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.sayit.vipulmittal.sayit.R;
import com.sayit.vipulmittal.sayit.camera.Custom_CameraActivity;
import com.sayit.vipulmittal.sayit.core.MainActivity;
import com.sayit.vipulmittal.sayit.location.MapsActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class ListenerService extends Service implements
        edu.cmu.pocketsphinx.RecognitionListener {
    private static final String KWS_SEARCH = "wakeup";
    private static final String CAMERA_SEARCH = "camera";
    private static final String MENU_SEARCH = "menu";
    private static final String LOCATION_SEARCH = "location";
    private static final String KEYPHRASE = "Ding ding start";

    public static final String STARTING_SAY_IT_APPLICATION = "Starting say it application.";
    private static final String LAUNCHING_CAMERA = "Launching camera";
    public static final String SMILE_TAKING_PHOTO = "Smile! taking photo";
    //    public static final String LOCATION_FOUND = "location_found";
    public static final String GETTING_LOCATION = "Getting location";

    public static final String INITIAL_MESSAGE = "initial_message";
    public static final String LOADED = "loaded";
    public static final String RUNNING = "running";
    public static final String STOPPED = "stopped";
    public final static String START = "start";
    public final static String STOP = "stop";
    public final static String STOP_SERVICE = "stop_service";
    private static final int NOTIFICATION_ID = 1;

    private boolean startRequested = false;
    private boolean canStart = false;
    private boolean cameraOpen = false;

    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;
    protected AudioManager mAudioManager;
    private TextToSpeech textToSpeech;
    private String currentSearch = KWS_SEARCH;
    private NotificationManager notificationManager;


    public static boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initSpeechToText();
        initTextToSpeech();

        notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START);
        intentFilter.addAction(STOP_SERVICE);
        registerReceiver(receiver, intentFilter);
    }

    private void initSpeechToText() {
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(ListenerService.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.i("", "Failed to init recognizer " + result);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(LOADED);
                    canStart = true;
                    if (startRequested) {
                        makeNotification(ListenerService.this, "SayIt application is running.", "Say 'Ding ding start' to start listening");
                        switchSearch(currentSearch);
                        intent.setAction(RUNNING);
                    }
                    sendBroadcast(intent);
                }
            }
        }.execute();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(Locale.UK);
                        }
                    }
                });
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                recognizer.stop();
                Log.i("TTS", "Start " + s);
            }

            @Override
            public void onDone(String s) {

                if (s.equals(LAUNCHING_CAMERA)) {
                    cameraOpen = true;
                    dispatchTakePictureIntent();
                } else if (s.equals(SMILE_TAKING_PHOTO)) {
                    Intent intent = new Intent();
                    intent.setAction(Custom_CameraActivity.TAKE_PHOTO);
                    sendBroadcast(intent);
                } else if (s.equals(GETTING_LOCATION)) {
                    Intent intent = new Intent(ListenerService.this, MapsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                start();

            }

            @Override
            public void onError(String s) {

            }
        });
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(START)) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, INITIAL_MESSAGE);
                textToSpeech.speak(STARTING_SAY_IT_APPLICATION, TextToSpeech.QUEUE_FLUSH, map);
            } else if (action.equals(STOP_SERVICE)) {
                Intent intent1 = new Intent();
                intent1.setAction(STOPPED);
                sendBroadcast(intent1);
                stop();
                running = false;
                notificationManager.cancel(NOTIFICATION_ID);
            }

        }
    };

    private void stop() {
        if (recognizer != null) {
            recognizer.stop();
        }
        startRequested = false;
    }

    private void start() {
        startRequested = true;
        if (canStart) {
            switchSearch(currentSearch);
            Intent intent = new Intent();
            intent.setAction(RUNNING);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }


    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(this, Custom_CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            makeNotification(ListenerService.this, "SayIt application is listening.", "Camera and location actions");
            Intent intent = new Intent();
            intent.setAction(MyService.START);
            sendBroadcast(intent);
            stop();
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(currentSearch))
            switchSearch(currentSearch);
    }

    private void switchSearch(String searchName) {
        running = true;
        currentSearch = searchName;
        recognizer.stop();
        if (!searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName, 1000);
        else {
            recognizer.startListening(searchName);
            makeNotification(ListenerService.this, "SayIt application is running.", "Say 'Ding ding start' to start listening");
        }

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                        //.setRawLogDir(assetsDir)
                .setKeywordThreshold(1e-45f)
                .setBoolean("-allphone_ci", true)
                .getRecognizer();
        recognizer.addListener(this);
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

        File cameraGrammar = new File(assetsDir, "camera.gram");
        recognizer.addGrammarSearch(CAMERA_SEARCH, cameraGrammar);

        File locationGrammar = new File(assetsDir, "location.gram");
        recognizer.addGrammarSearch(LOCATION_SEARCH, locationGrammar);
    }

    @Override
    public void onError(Exception error) {
        Log.i("", error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(currentSearch);
    }

    private void makeNotification(Context context, String title, String subtitle) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        Notification n;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = builder.build();
        } else {
            n = builder.getNotification();
        }

        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(NOTIFICATION_ID, n);
    }
}
