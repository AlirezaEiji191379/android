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
    public CoinMarketController(Context appCurrentActivity, ApiRequest api , android.os.Handler handler, int start, int limit) {
        this.appCurrentActivity = appCurrentActivity;
        this.api=api;
        this.start=start;
        this.limit=limit;
        this.handler=handler;
    }

    public boolean checkDeviceConnection(){
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
             api.doGetRequestForCryptoData(start,limit);
             while (api.getFetchedCryptos().size()!=limit){}
             ArrayList<Crypto> allCryptos=api.getFetchedCryptos();
             Log.i("main90xxxx", String.valueOf(allCryptos.size()));
             StringBuilder id=new StringBuilder("");
             for(int i=0;i<allCryptos.size();i++){
                id.append(allCryptos.get(i).getId()+",");
             }
             id.deleteCharAt(id.lastIndexOf(","));
             Log.i("main90xxxx", String.valueOf(id));
             api.doGetRequestForCryptoLogo(id.toString());
             while (api.getAllFetchedDrawables().size()!=limit){}
             ArrayList<RequestBuilder>allRbs=api.getAllFetchedDrawables();
             ArrayList<Object> objects=new ArrayList<>();
             objects.add(allCryptos);
             objects.add(allRbs);
             Message message=Message.obtain();
             message.obj=objects;
             handler.sendMessage(message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{

        }
    }

}
