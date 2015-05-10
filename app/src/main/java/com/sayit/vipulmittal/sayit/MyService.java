package com.sayit.vipulmittal.sayit;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MyService extends Service {
    private static final String LAUNCHING_CAMERA = "Launching camera";
    public static final String SMILE_TAKING_PHOTO = "Smile! taking photo";
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    protected boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private boolean mIsStreamSolo;

    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    public final static String START = "start";
    public final static String STOP = "stop";
    private TextToSpeech ttobj;
    private boolean running;

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initSpeechToText();
        initTextToSpeech();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(START);
        intentFilter.addAction(STOP);
        registerReceiver(receiver, intentFilter);
    }

    private void initSpeechToText() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
    }

    private void initTextToSpeech() {
        ttobj = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            ttobj.setLanguage(Locale.UK);
                        }
                    }
                });
        ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                Log.i("TTS", "Start " + s);
            }

            @Override
            public void onDone(String s) {
                Log.i("TTS", s);
                if (s.equals(LAUNCHING_CAMERA)) {
                    dispatchTakePictureIntent();
                } else if (s.equals(SMILE_TAKING_PHOTO)) {
                    Intent intent = new Intent();
                    intent.setAction(Custom_CameraActivity.TAKE_PHOTO);
                    sendBroadcast(intent);
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
            Log.i("S---", "Message received");
            String action = intent.getAction();
            if (action.equals(START)) {
                running = true;
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "initial_message");
                ttobj.speak("Starting say it application. Please say open camera, to see the preview and then say take photo to take a photo.", TextToSpeech.QUEUE_FLUSH, map);
            } else if (action.equals(STOP)) {
                running = false;
                stop();
            }
        }
    };

    private void stop() {
        if (mIsCountDownOn) {
            mIsCountDownOn = false;
            mNoSpeechCountDown.cancel();
        }
        mIsListening = false;
        Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("", e.getMessage());
        }
    }

    private void start() {
        if (mIsCountDownOn) {
            mIsCountDownOn = false;
            mNoSpeechCountDown.cancel();
        }
        mIsListening = false;
        Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
        try {
            mServerMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("", e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<MyService> mtarget;

        IncomingHandler(MyService target) {
            mtarget = new WeakReference<MyService>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            final MyService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        if (!target.mIsStreamSolo) {
                            target.mAudioManager.setStreamSolo(AudioManager.STREAM_SYSTEM, true);
                            target.mIsStreamSolo = true;
                        }
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    if (target.mIsStreamSolo) {
                        target.mAudioManager.setStreamSolo(AudioManager.STREAM_SYSTEM, false);
                        target.mIsStreamSolo = false;
                    }
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    break;
            }
        }
    }

    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mIsCountDownOn = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
            try {
                mServerMessenger.send(message);
                message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                mServerMessenger.send(message);
            } catch (RemoteException e) {
                Log.e("", e.getMessage());
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            if (running) {
                start();
            }
            //Log.d(TAG, "error = " + error); //$NON-NLS-1$
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            if (running) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mIsCountDownOn = true;
                    mNoSpeechCountDown.start();
                }
            }
            Log.d("", "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {
            //Log.d(TAG, "onResults"); //$NON-NLS-1$
            String str = "";
            Log.d("---", "onResults " + results);
            ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++) {
                Log.d("---", "result " + data.get(i));
                str += data.get(i);
            }
            if (data.contains("open camera")) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, LAUNCHING_CAMERA);
                ttobj.speak(LAUNCHING_CAMERA, TextToSpeech.QUEUE_FLUSH, map);
                //dispatchTakePictureIntent();
            } else if (data.contains("take photo")) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, SMILE_TAKING_PHOTO);
                ttobj.speak(SMILE_TAKING_PHOTO, TextToSpeech.QUEUE_FLUSH, map);

            } else if (data.contains("leave") || data.contains("done")) {
                Intent intent = new Intent();
                intent.setAction(Custom_CameraActivity.STOP);
                sendBroadcast(intent);
                start();
            } else {
                start();
            }
            if (mIsStreamSolo) {
                mAudioManager.setStreamSolo(AudioManager.STREAM_SYSTEM, false);
                mIsStreamSolo = false;
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(this, Custom_CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
