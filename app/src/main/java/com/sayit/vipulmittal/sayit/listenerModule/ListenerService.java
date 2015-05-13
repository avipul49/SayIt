package com.sayit.vipulmittal.sayit.listenerModule;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.sayit.vipulmittal.sayit.camera.Custom_CameraActivity;
import com.sayit.vipulmittal.sayit.location.MapsActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;

import static android.widget.Toast.makeText;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class ListenerService extends Service implements
        edu.cmu.pocketsphinx.RecognitionListener {
    private static final String KWS_SEARCH = "wakeup";
    private static final String CAMERA_SEARCH = "camera";
    private static final String MENU_SEARCH = "menu";
    private static final String LOCATION_SEARCH = "location";
    private static final String KEYPHRASE = "please start";

    public static final String STARTING_SAY_IT_APPLICATION = "Starting say it application.";
    private static final String LAUNCHING_CAMERA = "Launching camera";
    public static final String SMILE_TAKING_PHOTO = "Smile! taking photo";
    public static final String LOCATION_FOUND = "location_found";
    public static final String GETTING_LOCATION = "Getting location";

    public static final String INITIAL_MESSAGE = "initial_message";
    public static final String LOADED = "loaded";
    public static final String RUNNING = "running";
    public static final String STOPPED = "stopped";
    public final static String START = "start";
    public final static String STOP = "stop";
    public final static String STOP_SERVICE = "stop_service";

    private boolean startRequested = false;
    private boolean canStart = false;
    private boolean cameraOpen = false;

    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;
    protected AudioManager mAudioManager;
    private TextToSpeech textToSpeech;
    private String currentSearch = KWS_SEARCH;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initSpeechToText();
        initTextToSpeech();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START);
        intentFilter.addAction(STOP_SERVICE);
        intentFilter.addAction(LOCATION_FOUND);
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
                if (s.equals(INITIAL_MESSAGE)) {
                    //start();
                }
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
            } else if (action.equals(LOCATION_FOUND)) {
                //running = false;
                stop();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, LOCATION_FOUND);
                String local = intent.getStringExtra("location");
                textToSpeech.speak("You are in " + local, TextToSpeech.QUEUE_FLUSH, map);
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
        if (text.equals(KEYPHRASE))
            switchSearch(MENU_SEARCH);
        else if (text.equals(LOCATION_SEARCH))
            switchSearch(LOCATION_SEARCH);
        else if (text.equals(CAMERA_SEARCH)) {
            switchSearch(CAMERA_SEARCH);

        }
        Log.i("Text found", "--------" + text);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            Log.i("Text found", "----On result----" + text);
            Action action = Action.getAction(text);
            HashMap<String, String> map = null;
            switch (action) {
                case OPEN_CAMERA:
                    if (!cameraOpen) {
                        map = new HashMap<String, String>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, LAUNCHING_CAMERA);
                        textToSpeech.speak(LAUNCHING_CAMERA, TextToSpeech.QUEUE_FLUSH, map);
                    }
                    //dispatchTakePictureIntent();
                    break;
                case TAKE_PHOTO:
                    if (cameraOpen) {
                        map = new HashMap<String, String>();
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, SMILE_TAKING_PHOTO);
                        textToSpeech.speak(SMILE_TAKING_PHOTO, TextToSpeech.QUEUE_FLUSH, map);
                    }
                    break;
                case LEAVE:
                    Intent intent = new Intent();
                    intent.setAction(ListenerService.STOP);
                    sendBroadcast(intent);
                    currentSearch = MENU_SEARCH;
                    start();
                    cameraOpen = false;
                    break;
                case WHERE_AM_I:
                    map = new HashMap<String, String>();
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, GETTING_LOCATION);
                    textToSpeech.speak(GETTING_LOCATION, TextToSpeech.QUEUE_FLUSH, map);
                    break;
                case STOP_LISTENING:
                    currentSearch = KWS_SEARCH;
                    start();
                    break;
                case NOT_FOUND:
                    break;
            }
        }
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
        currentSearch = searchName;
        recognizer.stop();
        if (!searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName, 1000);
        else
            recognizer.startListening(searchName);

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
}
