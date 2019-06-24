package com.example.antonio.chat;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Usuario on 23/02/2017.
 */

public class MyAplication extends Application {

    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public static final String TAG = MyAplication.class.getSimpleName();
    private RequestQueue mRequestQueue;
    //private ImageLoader mImageLoader;
    private static MyAplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();//Se aplica una fuente a toda la aplicación incluida en el directorio assets/font

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                //.setDefaultFontPath("fonts/Roboto-Regular")
                .setDefaultFontPath("fonts/NotoMono-Regular")//03/01/2017 Se cambia la fuente....
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
        mInstance = this;
    }

    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public static synchronized MyAplication getInstance() {
        return mInstance;
    }
    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

/*    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }*/

    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // asigna un valor a tag si tag está vacío
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    //21/11/2017 NUEVO:PATRÓN SINGLETON APLICADO A LA INSTANCIA DE VOLLEY
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}