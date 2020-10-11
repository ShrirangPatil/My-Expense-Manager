package com.example.android.myexpensemanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

    static String TAG = VRExpense.class.getName();
    SpeechRecognizer mRecognizer = null;
    public static final String PRICE_KEY_CODE = "price_key_code";
    public static final String DATE_KEY_CODE = "date_key_code";
    private static final int RECORD_PERMISSION_CODE = 101;
    final RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onResults(Bundle results) {
            Log.i(TAG, "onResult()");
            final ArrayList<String> voiceResults = results
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            final float[] confidenceScore = results
                    .getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            Log.i(TAG, "Obtaining matches: ");
            Runnable tokenizeRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Thread in action " + Thread.currentThread().getName());
                    int index = 0;
                    try {
                        if (voiceResults != null) {
                            float maxConfidence = -1;
                            String maxConfidenceString = "";
                            assert confidenceScore != null;
                            for(String res : voiceResults) {
                                Log.i(TAG, "result = " + res + " score = " + confidenceScore[index]);
                                if (maxConfidence < confidenceScore[index]) {
                                    Log.i(TAG,"maxConfStr"+res);
                                    maxConfidenceString = res;
                                    maxConfidence = confidenceScore[index++];
                                }
                            }
                            analyzeRecord(maxConfidenceString);
                        } else {
                            Log.i(TAG, "voice result is null");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
//            Thread tokenThread = new Thread(tokenizeRunnable, "tokenThread");
//            tokenThread.start();
            Handler handler = new Handler();
            handler.post(tokenizeRunnable);
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.i(TAG, "onReadyForSpeech()");
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
            Log.i(TAG, "onError" + error);
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
//            Log.i(TAG, "onRmsChanged" + rmsdB);
        }
    };

    private  boolean recognizeAdd(String str) {
        Log.d(TAG, "In recognizeAdd");
        return str.equalsIgnoreCase("add") || str.equalsIgnoreCase("spend") ||
                str.equalsIgnoreCase("spent") || str.equalsIgnoreCase("expense") ||
                str.equalsIgnoreCase("expenditure") || str.equalsIgnoreCase("expend") ||
                str.equalsIgnoreCase("expended") || str.equalsIgnoreCase("cost") ||
                str.equalsIgnoreCase("costed") || str.equalsIgnoreCase("bought") ||
                str.equalsIgnoreCase("buy");
    }

    private boolean recognizeQuery(String str) {
        Log.d(TAG, "In recognizeQuery");
        return str.equalsIgnoreCase("find") || str.equalsIgnoreCase("search") ||
                str.equalsIgnoreCase("query");
    }

    private String[] recognizeDate(String str) {
        Log.d(TAG, "In recognize date "+str);
        String[] months = {"january", "february", "march", "april", "may", "june", "july", "august",
                "september", "october", "november", "december"};
        String[] splitStr = str.split(" ");
        String[] date = {"", "", ""};

        for (int i=0;i<splitStr.length;i++) {
            for (int j=0;j<months.length;j++) {
                String month = months[j];
                try {
                    if (splitStr[i].toLowerCase().contains(month)) {
                        date[1] = Integer.valueOf(j + 1).toString();
                        Integer left = Integer.parseInt(splitStr[i - 1]
                                .toLowerCase()
                                .split("th")[0]
                                .split("st")[0]
                                .split("nd")[0]);
                        Integer right = Integer.parseInt(splitStr[i + 1]
                                .toLowerCase()
                                .split("th")[0]
                                .split("st")[0]
                                .split("nd")[0]);
                        Log.d(TAG, "Left = " + left);
                        Log.d(TAG, "Right = " + right);
                        if (left < 31) {
                            date[0] = left.toString();
                            date[2] = right.toString();
                        } else {
                            date[2] = left.toString();
                            date[0] = right.toString();
                        }
                        break;
                    } else if (Integer.parseInt(splitStr[i]) > 0) {
                        if (i + 1 < splitStr.length && Integer.parseInt(splitStr[i + 1]) > 0) {
                            if (i + 2 < splitStr.length && Integer.parseInt(splitStr[i + 2]) > 0) {
                                if (AddExpense.checkDate(splitStr[i] + "/" + splitStr[i + 1] + "/" + splitStr[i + 2])) {
                                    date[0] = splitStr[i];
                                    date[1] = splitStr[i + 1];
                                    date[2] = splitStr[i + 2];
                                } else {
                                    date[2] = splitStr[i];
                                    date[1] = splitStr[i + 1];
                                    date[0] = splitStr[i + 2];
                                }
                            }
                        }
                        break;
                    }
                } catch (NumberFormatException e) {
                    // pass
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return  date;
    }

    private double recognizePrice(String str, String[] date) {
        double price = 0;
        for (String s : str.split(" ")) {
            try {
                if (Double.parseDouble(s) > 0) {
                    boolean isPrice = true;
                    for (String d : date) {
                        if (s.equals(d)) {
                            isPrice = false;
                            break;
                        }
                    }
                    if (isPrice) {
                        price = Double.parseDouble(s);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                // pass
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return price;
    }

    public void analyzeRecord(String result) {
        Log.d(TAG, "In analyzeRecord");
        /* check the date */
        String[] date = recognizeDate(result);
        Log.i(TAG, "Date = "+date[0]+"/"+date[1]+"/"+date[2]);
        /* check the price */
        double price = recognizePrice(result, date);
        Log.i(TAG, "Price = "+price);
        /* check the operation */
        for (String str : result.split(" ")) {
            Log.d(TAG, str);
            if (recognizeAdd(str)) {
                Log.i(TAG, "Add found");
                Intent addIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble(PRICE_KEY_CODE, price);
                bundle.putString(DATE_KEY_CODE, date[0]+"/"+date[1]+"/"+date[2]);
                addIntent.setClass(VRExpense.this, AddExpense.class);
                addIntent.putExtras(bundle);
                Log.i(TAG, "AddExpense Activity Launched");
                startActivity(addIntent);
                break;
            } else if (recognizeQuery(str)) {
                Log.i(TAG, "Search found");
                break;
            }
        }
    }

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