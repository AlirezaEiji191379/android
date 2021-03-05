package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;


public class CoinMarketController implements Runnable{

    private ApiRequest api;
    private  Context appCurrentActivity;

    public CoinMarketController(Context appCurrentActivity, ApiRequest api) {
        this.appCurrentActivity = appCurrentActivity;
        this.api=api;
    }

    public synchronized boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.appCurrentActivity.getSystemService(appCurrentActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void run() {
        if (this.checkDeviceConnection()){
            try {
                api.doGetRequest();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        else{

        }
    }

}
