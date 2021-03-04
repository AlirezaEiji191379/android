package com.sutporject.crypto.Controller;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class CoinMarketCapRequestController implements Runnable{
    private final String API_KEY="b7338e0c-e7d6-484c-ade8-77c577cb7773";
    private final String url="https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private int limitOfCoins;
    private  Context appCurrentActivity;

    public CoinMarketCapRequestController(int limitOfCoins , Context appCurrentActivity){
        this.limitOfCoins=limitOfCoins;
        this.appCurrentActivity=appCurrentActivity;
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

    public void extractResponseFromRequest(Response response){
        try {
            Log.i("main", "the response is: "+response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doGetRequest() throws IOException {
        OkHttpClient httpClient=new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.url).newBuilder();
        urlBuilder.addQueryParameter("limit", String.valueOf(this.limitOfCoins));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY)
                .build();

        httpClient.newCall(request).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("mainX", "faile shod dadash");
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        extractResponseFromRequest(response);
                    }
                }
        );
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
    }

}
