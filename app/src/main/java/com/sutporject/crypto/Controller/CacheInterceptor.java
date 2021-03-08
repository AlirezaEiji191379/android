package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

public class CacheInterceptor implements Interceptor {
    private Context appCurrentActivity;

    public CacheInterceptor(Context context){
        this.appCurrentActivity=context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if(this.checkDeviceConnection()){

            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(15, TimeUnit.MINUTES) // 15 minutes cache
                    .build();

            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheControl.toString())
                    .build();
        }else{
            int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            return response.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    }

    private boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.appCurrentActivity.getSystemService(appCurrentActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }



}
