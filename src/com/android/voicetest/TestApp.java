package com.android.voicetest;

import android.app.Application;
import com.iflytek.cloud.SpeechUtility;

public class TestApp extends Application{
    @Override
    public void onCreate() {
        SpeechUtility.createUtility(TestApp.this, "appid=");  // Set your own app id here
        super.onCreate();
    }
}


