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
import java.util.concurrent.ArrayBlockingQueue;
import android.os.Handler;


public class CoinMarketController implements Runnable{
    private int start;
    private int limit;
    private ApiRequest api;
    private  Context appCurrentActivity;
    private Handler handler;
    private ArrayBlockingQueue<Crypto> cryptoArrayBlockingQueue;
    private ArrayBlockingQueue<RequestBuilder> requestBuilderArrayBlockingQueue;
    public CoinMarketController(Context appCurrentActivity, ApiRequest api , android.os.Handler handler, int start, int limit) {
        this.appCurrentActivity = appCurrentActivity;
        this.api=api;
        this.start=start;
        this.limit=limit;
        this.handler=handler;
        cryptoArrayBlockingQueue=new ArrayBlockingQueue<>(limit);
        requestBuilderArrayBlockingQueue=new ArrayBlockingQueue<>(limit);
        api.setAllFetchedCrypto(cryptoArrayBlockingQueue);
        api.setAllFetchedDrawables(requestBuilderArrayBlockingQueue);
    }

    public boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.appCurrentActivity.getSystemService(appCurrentActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void getDataFromInternet(){
        try {
            api.doGetRequestForCryptoData(start,limit);
            ArrayList<Crypto> allCryptos=new ArrayList<>();
            ArrayList<RequestBuilder>allRbs=new ArrayList<>();
            for(int i=0;i<limit;i++){
                allCryptos.add(cryptoArrayBlockingQueue.take());
            }
            StringBuilder id=new StringBuilder("");
            for(int i=0;i<allCryptos.size();i++){
                id.append(allCryptos.get(i).getId()+",");
            }
            id.deleteCharAt(id.lastIndexOf(","));
            api.doGetRequestForCryptoLogo(id.toString());
            for(int i=0;i<limit;i++){
                allRbs.add(requestBuilderArrayBlockingQueue.take());
            }
            ArrayList<Object> objects=new ArrayList<>();
            objects.add(allCryptos);
            objects.add(allRbs);
            Message message=Message.obtain();
            message.obj=objects;
            handler.sendMessage(message);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (this.checkDeviceConnection()){
            this.getDataFromInternet();
        }
        else{

        }
    }

}
