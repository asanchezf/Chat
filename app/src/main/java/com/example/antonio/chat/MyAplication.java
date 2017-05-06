package com.example.antonio.chat;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Usuario on 23/02/2017.
 */

public class MyAplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();//Se aplica una fuente a toda la aplicación incluida en el directorio assets/font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}

