package com.sutporject.crypto.Controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Keep;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.sutporject.crypto.model.Crypto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRequest{

    private Context context;
    private Handler handler;
    private final String API_KEY="b7338e0c-e7d6-484c-ade8-77c577cb7773";
    private final String urlForData="https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private final String urlForImage="https://pro-api.coinmarketcap.com/v1/cryptocurrency/info";
    private boolean done=true;
    private ArrayList<Crypto> allFetchedCrypto=new ArrayList<>();
    private ArrayList<RequestBuilder> allFetchedDrawables=new ArrayList<>();
    //private final OkHttpClient httpClient=new OkHttpClient();
    public ApiRequest(Handler handler,Context context) {
        this.handler = handler;
        this.context=context;
    }

    public synchronized ArrayList<Crypto> getFetchedCryptos() {
        return allFetchedCrypto;
    }

    public synchronized ArrayList<RequestBuilder> getAllFetchedDrawables() {
        return allFetchedDrawables;
    }

    public synchronized void  doGetRequestForCryptoData(int start, int limit) throws IOException{
        while (done==false){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.done=false;
        allFetchedCrypto.clear();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForData).newBuilder();
        urlBuilder.addQueryParameter("limit", String.valueOf(limit));
        urlBuilder.addQueryParameter("start", String.valueOf(start));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY)
                .build();
        OkHttpClient httpClient=new OkHttpClient();
        httpClient.newCall(request).enqueue(new Callback() {
            private void extractResponseFromRequest(Response response){
               done=true;
                try {
                    String resp=response.body().string();
                    Log.i("resposne", resp);
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONArray data=  jsonResponse.getJSONArray("data");
                    //ArrayList<Crypto> allCrypto=new ArrayList<>();
                    for(int i=0;i<data.length();i++){
                        JSONObject model=data.getJSONObject(i);
                        int id=model.getInt("id");
                        JSONObject quote=model.getJSONObject("quote");
                        JSONObject usd=quote.getJSONObject("USD");
                        String name= (String) model.get("name");
                        String symbol=(String) model.get("symbol");
                        double price= (double) usd.get("price");
                        double percentage_change_1h=(double) usd.get("percent_change_1h");
                        double percentage_change_24h=(double) usd.get("percent_change_24h");
                        double percentage_change_7d=(double) usd.get("percent_change_7d");
                        Log.i("main90", String.valueOf(id));
                        Crypto newCrypto=new Crypto(id,name,symbol,price,percentage_change_1h,percentage_change_7d,percentage_change_24h);
                        allFetchedCrypto.add(newCrypto);
                    }
//                    Message msg=Message.obtain();
//                    msg.obj=allFetchedCrypto;
//                    handler.sendMessage(msg);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
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
        });
        while (done==false){
            notifyAll();
        }
    }

    public synchronized void doGetRequestForCryptoLogo(String id){
        while (done==false){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.done=false;
        allFetchedDrawables.clear();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForImage).newBuilder();
        urlBuilder.addQueryParameter("id", String.valueOf(id));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY)
                .build();
        OkHttpClient httpClient=new OkHttpClient();
        httpClient.newCall(request).enqueue(new Callback() {
            private void extractResponseFromRequest(Response response){
                done=true;
                try {
                    String resp=response.body().string();
                    Log.i("resposne", resp);
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONObject data=  jsonResponse.getJSONObject("data");
                    Log.i("data: ", String.valueOf(data));
                    //ArrayList<Crypto> allCrypto=new ArrayList<>();
                    String [] tokenize=id.split(",");
                    for(int i=0;i<tokenize.length;i++){
                        JSONObject model=data.getJSONObject(tokenize[i]);
                        String logoUrl= (String) model.get("logo");
                        allFetchedDrawables.add(Glide.with(context).load(logoUrl));
                        Log.i("data: ", logoUrl);
                    }
//                   Message msg=Message.obtain();
//                    msg.obj=allCrypto;
//                    handler.sendMessage(msg);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
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
        });
        while (done==false){
            notifyAll();
        }
    }


}
