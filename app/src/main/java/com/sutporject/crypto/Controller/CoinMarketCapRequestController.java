package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sutporject.crypto.model.Crypto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CoinMarketCapRequestController implements Runnable{

    private static CoinMarketCapRequestController cmc;
    private final String API_KEY="b7338e0c-e7d6-484c-ade8-77c577cb7773";
    private final String url="https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private final int Limit_Of_Coins=5;
    private int indexOfCoins;
    private  Context appCurrentActivity;
    private android.os.Handler handler;


    private CoinMarketCapRequestController(){}

    public static CoinMarketCapRequestController getInstance(){
        if(cmc==null) cmc=new CoinMarketCapRequestController();
        return cmc;
    }

    public boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.appCurrentActivity.getSystemService(appCurrentActivity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public void setAppCurrentActivity(Context appCurrentActivity){
        this.appCurrentActivity=appCurrentActivity;
    }

    public void setIndexOfCoins(){
        this.indexOfCoins=this.indexOfCoins+this.Limit_Of_Coins;
    }

    public void setIndexOfCoins(int indexOfCoins) {
        this.indexOfCoins = indexOfCoins;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void doGetRequest() throws IOException {
        OkHttpClient httpClient=new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.url).newBuilder();
        urlBuilder.addQueryParameter("limit", String.valueOf(this.Limit_Of_Coins));
        urlBuilder.addQueryParameter("start", String.valueOf(this.indexOfCoins));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY)
                .build();

        httpClient.newCall(request).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("mainX", "failed shod dadash");
                        Message message=Message.obtain();
                        message.obj="the connection was interrupted!";
                        handler.sendMessage(message);
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        extractResponseFromRequest(response);
                    }
                }
        );
    }

    public void extractResponseFromRequest(Response response){
        try {
            String resp=response.body().string();
            JSONObject jsonResponse=new JSONObject(resp);
            JSONArray data=  jsonResponse.getJSONArray("data");
            Log.i("main", String.valueOf(jsonResponse));
            Log.i("main2", String.valueOf(data));
            ArrayList<Crypto> allCrypto=new ArrayList<>();
            for(int i=0;i<data.length();i++){
                JSONObject model=data.getJSONObject(i);
                JSONObject quote=model.getJSONObject("quote");
                JSONObject usd=quote.getJSONObject("USD");
                String name= (String) model.get("name");
                String symbol=(String) model.get("symbol");
                double price= (double) usd.get("price");
                double percentage_change_1h=(double) usd.get("percent_change_1h");
                double percentage_change_24h=(double) usd.get("percent_change_24h");
                double percentage_change_7d=(double) usd.get("percent_change_7d");
                allCrypto.add(new Crypto(name,symbol,price,percentage_change_1h,percentage_change_24h,percentage_change_7d));
            }
            Message msg=Message.obtain();
            msg.obj=allCrypto;
            handler.sendMessage(msg);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        if (this.checkDeviceConnection()){
            try {
                this.doGetRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{

        }
    }

}
