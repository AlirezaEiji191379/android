package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;

import com.bumptech.glide.RequestBuilder;
import com.sutporject.crypto.MainActivity;
import com.sutporject.crypto.model.Crypto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.os.Handler;


public class CoinMarketController implements Runnable{
    private int start;
    private int limit;
    private int priority;
    private ApiRequest api;
    private  Context appCurrentActivity;
    private Handler handler;


    public CoinMarketController(Context appCurrentActivity, ApiRequest api , android.os.Handler handler, int start, int limit) {
        this.appCurrentActivity = appCurrentActivity;
        this.api=api;
        this.start=start;
        this.limit=limit;
        this.handler=handler;
    }


    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.appCurrentActivity.getSystemService(appCurrentActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void getDataFromInternet() throws IOException, InterruptedException {
        ArrayList<Object> objects=api.fetchDataFromCoinMarket(start,limit);
        Message message=Message.obtain();
        if(objects==null){
            message.obj="please connect to internet for fetching more data";
            message.what=0;
            handler.sendMessage(message);
            return;
        }
        message.obj=objects;
        message.what=1;
        handler.sendMessage(message);
    }

    @Override
    public void run() {
        try {
            Log.i("main","the thread with "+priority+" is started!");
            Log.i("main","the thread is with "+start+" index");
            this.getDataFromInternet();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getPriority() {
        return priority;
    }

}

