package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sutporject.crypto.model.Candle;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class CoinCandleController  implements Runnable {
    private ArrayBlockingQueue<Candle> allCandles;
    private ApiRequest api;
    private Context appCurrentActivity;
    private Handler handler;
    private String symbol;
    private ApiRequest.Range range;
    public CoinCandleController(ApiRequest api, Context appCurrentActivity, Handler handler, String symbol, ApiRequest.Range range) {
        this.api = api;
        this.appCurrentActivity = appCurrentActivity;
        this.handler = handler;
        this.symbol = symbol;
        this.range=range;
        this.allCandles=new ArrayBlockingQueue<>(range.getNumVal());
    }

    private void getDataFromInternet(){
        try {
            api.setAllCandles(allCandles);
            api.doGetRequestForCandles(symbol, range);
            ArrayList<Candle> all = new ArrayList<>();
            if(range == ApiRequest.Range.weekly){
                for (int i = 0; i < 7; i++) {
                    all.add(allCandles.take());
                }
            }
            else{
                for (int i = 0; i < 30; i++) {
                    all.add(allCandles.take());
                }
            }
            Message message=Message.obtain();
            message.obj=all;
            message.what=1;
            handler.sendMessage(message);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.getDataFromInternet();
    }
}
