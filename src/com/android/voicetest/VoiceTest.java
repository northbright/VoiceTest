/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.voicetest;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioButton;
import java.util.Date;
import android.text.Layout;
//import android.text.method.ScrollingMovementMethod;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
//import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
//import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;

public class VoiceTest extends Activity {
    final String TAG = "VoiceTest";
    private String mOutputText = "";

    private SpeechRecognizer mIat;
    private RecognizerDialog iatDialog;
    boolean useDialog = true;  // true: use RecognizerDialog, false: use SpeechRecognizer. Choose 1 way only.
    boolean starting = false;
    String finalText = "";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        // Recognizer Way
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        mIat.setParameter(SpeechConstant.ASR_PTT, "false");  // no punctuation

        // or Dialog Way
        iatDialog = new RecognizerDialog(this, mInitListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        Log.i(TAG, "onSaveInstanceState");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Call finish() if you want to start a new activity and exit
        //finish();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        // Check if go Recognizer Way or Dialog Way
        RadioButton rb = (RadioButton)findViewById(R.id.radio_dialog);
        useDialog = rb.isChecked();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onpause");
        super.onPause();
    }

    private void refreshView(boolean started) {
        Button startButton = (Button) findViewById(R.id.button_start);
        Button stopButton = (Button) findViewById(R.id.button_stop);
        RadioButton rbDialog = (RadioButton)findViewById(R.id.radio_dialog);
        RadioButton rbRecognizer = (RadioButton)findViewById(R.id.radio_recognizer);

        if (started) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            rbDialog.setEnabled(false);
            rbRecognizer.setEnabled(false);
            return;
        } else {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            rbDialog.setEnabled(true);
            rbRecognizer.setEnabled(true);
        }            
    }

    public void showMessage(boolean isAppend, String msg) {
        Log.v("Frank", msg);

        TextView outputText = (TextView)findViewById(R.id.output_text_view);
        if (!isAppend)
            outputText.setText("");

        outputText.append(msg + "\n");
        //outputText.setMovementMethod(ScrollingMovementMethod.getInstance()); 

        final Layout layout = outputText.getLayout();
        if (layout != null) {
            final int scrollAmount = layout.getLineTop(outputText.getLineCount()) - outputText.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                outputText.scrollTo(0, scrollAmount);
            else
                outputText.scrollTo(0, 0);
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
    
        switch(view.getId()) {
            case R.id.radio_dialog:
                if (checked)
                    useDialog = true;
                break;
            case R.id.radio_recognizer:
                if (checked)
                    useDialog = false;
                break;
        }
    }

    public void start(View view) {
        showMessage(true, "Start...");
        finalText = "";

        starting = false;
        if (!useDialog) {
            // Recognizer Way
            int ret = mIat.startListening(recognizerListener);
            if(ret != ErrorCode.SUCCESS){
                showMessage(true, "startListening() error: " + ret);
            } else {
                showMessage(true, "OK");
                starting = true;
            }
        } else {
            // Dialog Way
            iatDialog.setListener(recognizerDialogListener);
            iatDialog.show();
            starting = true;
        }
        refreshView(starting);
    }

    public void stop(View view) {
        showMessage(true, "Stop...");

        if (!useDialog) {
            // Recognizer Way
            mIat.stopListening();
        } else {
            // Dialog Way
            iatDialog.dismiss();
        }

        starting = false;
        refreshView(starting);
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showMessage(true, "onInit(): error: " + code);
            } else {
                showMessage(true, "onInit(): succeeded");
            }
        }
    };

    // Recognizer Way
    private RecognizerListener recognizerListener=new RecognizerListener(){
        @Override
        public void onBeginOfSpeech() {
            showMessage(true, "begin of speech");
        }

        @Override
        public void onError(SpeechError error) {
            showMessage(true, "onError(): " + error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            showMessage(true, "end of speech");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            finalText += text;
            if(isLast) {
                showMessage(true, "onResult():last: " + text);
                showMessage(true, "final: " + finalText);
            } else {
                showMessage(true, "onResult(): " + text);
            }
        }

        @Override
        public void onVolumeChanged(int volume) {
            //showMessage(true, "onVolumeChanged(): " + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    // Dialog Way
    private RecognizerDialogListener recognizerDialogListener=new RecognizerDialogListener(){
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            finalText += text;
            if (isLast) {
                showMessage(true, "dialog::onResult():last: " + text);
                showMessage(true, "final: " + finalText);
            } else {
                showMessage(true, "dialog::onResult(): " + text);
            }
        }

        /**
        * OnError.
        */
        public void onError(SpeechError error) {
            String text = error.getPlainDescription(true);
            showMessage(true, "dialog::onError(): " + text);
        }
    };
}

