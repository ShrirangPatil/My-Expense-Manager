package com.example.android.myexpensemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class VRExpense extends AppCompatActivity {

    static String TAG = MainActivity.class.getName();
    SpeechRecognizer mRecognizer = null;
    private static final int RECORD_PERMISSION_CODE = 101;
    RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onResults(Bundle results) {
            Log.i(TAG, "onResult()");
            ArrayList<String> voiceResults = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (voiceResults == null) {
                Log.i(TAG,"No voice results");
            } else {
                Log.i(TAG,"Printing matches: ");
                for (String match : voiceResults) {
                    Log.i(TAG, ""+match);
                }
            }
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i(TAG,"onReadyForSpeech()");
        }

        /**
         *  ERROR_NETWORK_TIMEOUT = 1;
         *  ERROR_NETWORK = 2;
         *  ERROR_AUDIO = 3;
         *  ERROR_SERVER = 4;
         *  ERROR_CLIENT = 5;
         *  ERROR_SPEECH_TIMEOUT = 6;
         *  ERROR_NO_MATCH = 7;
         *  ERROR_RECOGNIZER_BUSY = 8;
         *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
         *
         * @param error code is defined in SpeechRecognizer
         */
        @Override
        public void onError(int error) {
            Log.i(TAG, "onError"+error);
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.i(TAG, "onBeginningOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.i(TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG, "onEndOfSpeech");
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.i(TAG, "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            Log.i(TAG, "onPartialResults");
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.i(TAG, "onRmsChanged"+rmsdB);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_expense);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    RECORD_PERMISSION_CODE);
        }
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
        Button startVR = findViewById(R.id.test_start);
        startVR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                        "com.com.example.android.myexpensemanager");
                mRecognizer.setRecognitionListener(listener);
                mRecognizer.startListening(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (RECORD_PERMISSION_CODE == requestCode) {
            if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission for record granted");
            } else {
                onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRecognizer.stopListening();
        mRecognizer = null;
    }
}