package com.android.voicetest;

import android.app.Application;
import com.iflytek.cloud.SpeechUtility;

public class TestApp extends Application{
    @Override
    public void onCreate() {
        // SpeechDemo appid = 55029143
        SpeechUtility.createUtility(TestApp.this, "appid=55029143");
        super.onCreate();
    }
}


