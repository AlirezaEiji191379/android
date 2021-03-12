package com.sutporject.crypto.Controller;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sutporject.crypto.model.Candle;
import com.sutporject.crypto.model.Crypto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiRequest{

    private final String API_KEY_CoinMarket="207bf886-2cf9-4acf-b49c-da5aacb817c5";
    private final String API_KEY_Candles="7EB97426-CB5E-40D4-9840-F60CCE6D3FC7";
    private final String urlForData="https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private final String urlForImage="https://pro-api.coinmarketcap.com/v1/cryptocurrency/info";
    private final String urlForCandle="https://rest.coinapi.io/v1/ohlcv/";
    private Context context;
    private Handler handler;
    private boolean done=true;
    private ArrayBlockingQueue<Crypto> allFetchedCrypto;
    private ArrayBlockingQueue<RequestBuilder> allFetchedDrawables;
    private boolean clearCache;
    private ArrayBlockingQueue<Candle> allCandles;
    private ArrayBlockingQueue<Boolean> success=new ArrayBlockingQueue<Boolean>(1);

    public enum Range {
        weekly(7),
        oneMonth(30);
        private int num;
        Range(int num){
            this.num=num;
        }
        public int getNumVal() {
            return this.num;
        }
    }
    public ApiRequest(Handler handler,Context context) {
        this.handler = handler;
        this.context=context;
        allFetchedCrypto=new ArrayBlockingQueue<>(10);
        allFetchedDrawables=new ArrayBlockingQueue<>(10);
    }

    public void setAllCandles(ArrayBlockingQueue<Candle> allCandles) {
        this.allCandles = allCandles;
    }

    public void setClearCache(boolean clearCache) {
        this.clearCache = clearCache;
    }

    public synchronized ArrayList<Object> fetchDataFromCoinMarket(int start, int limit) throws IOException, InterruptedException {
        while (this.done==false){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.done=false;
        if(this.clearCache==true){
            CacheInterceptor.deleteCache(context);
            this.clearCache=false;
        }
        Log.i("main","thread "+ Thread.currentThread().getName()+" is stated!");
        this.doGetRequestForCryptoData(start,limit);

        boolean b=success.take();
        if(b==false){
            this.done=true;
            notifyAll();
            return null;
        }

        ArrayList<Crypto> allCryptos=new ArrayList<>();
        ArrayList<RequestBuilder>allRbs=new ArrayList<>();
        for(int i=0;i<limit;i++){
            Crypto x=allFetchedCrypto.take();
            allCryptos.add(x);
        }
        StringBuilder id=new StringBuilder("");
        for(int i=0;i<allCryptos.size();i++){
            id.append(allCryptos.get(i).getId()+",");
        }
        id.deleteCharAt(id.lastIndexOf(","));
        this.doGetRequestForCryptoLogo(id.toString());
        b=success.take();
        if(b==false){
            this.done=true;
            notifyAll();
            return null;
        }
        for(int i=0;i<limit;i++){
            allRbs.add(allFetchedDrawables.take());
        }
        ArrayList<Object> objects=new ArrayList<>();
        objects.add(allCryptos);
        objects.add(allRbs);
        while (this.done==false){
            notifyAll();
        }
        return objects;
    }

    private synchronized void  doGetRequestForCryptoData(int start, int limit) throws IOException{
        allFetchedCrypto.clear();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForData).newBuilder();
        urlBuilder.addQueryParameter("limit", String.valueOf(limit));
        urlBuilder.addQueryParameter("start", String.valueOf(start));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY_CoinMarket)
                .build();
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 15 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient httpClient= new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor(this.context))
                .cache(cache)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.urlForData+"/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            private void extractResponseFromRequest(Response response) throws InterruptedException {
                success.put(true);
               String resp="";
                try {
                    resp=response.body().string();
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONArray data=  jsonResponse.getJSONArray("data");
                    for(int i=0;i<data.length();i++){
                        JSONObject model=data.getJSONObject(i);
                        int id=model.getInt("id");
                        JSONObject quote=model.getJSONObject("quote");
                        JSONObject usd=quote.getJSONObject("USD");
                        String name= (String) model.get("name");
                        String symbol=(String) model.get("symbol");
                        double price= Double.parseDouble(String.valueOf(usd.get("price")));
                        double percentage_change_1h=Double.parseDouble(String.valueOf(usd.get("percent_change_1h")));
                        double percentage_change_24h=Double.parseDouble (String.valueOf(usd.get("percent_change_24h")));
                        double percentage_change_7d=Double.parseDouble(String.valueOf(usd.get("percent_change_7d")));
                        Crypto newCrypto=new Crypto(id,name,symbol,price,percentage_change_1h,percentage_change_7d,percentage_change_24h);
                        allFetchedCrypto.put(newCrypto);
                    }
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    success.put(false);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    extractResponseFromRequest(response);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized void doGetRequestForCryptoLogo(String id){
        allFetchedDrawables.clear();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForImage).newBuilder();
        urlBuilder.addQueryParameter("id", String.valueOf(id));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY_CoinMarket)
                .build();
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 15 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient httpClient= new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor(this.context))
                .cache(cache)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.urlForImage+"/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            private void extractResponseFromRequest(Response response) throws InterruptedException {
                success.put(true);
                RequestOptions myOptions = new RequestOptions()
                        .override(50, 50);
                try {
                    String resp=response.body().string();
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONObject data=  jsonResponse.getJSONObject("data");
                    String [] tokenize=id.split(",");
                    for(int i=0;i<tokenize.length;i++){
                        JSONObject model=data.getJSONObject(tokenize[i]);
                        String logoUrl= (String) model.get("logo");
                        allFetchedDrawables.put(Glide.with(context).asBitmap().apply(myOptions).load(logoUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE));
                    }
                    done=true;
                } catch (IOException | JSONException | InterruptedException e) {
                    done=true;
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    success.put(false);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    extractResponseFromRequest(response);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public synchronized void doGetRequestForCandles(String symbol, Range range) {
        OkHttpClient httpClient= new OkHttpClient.Builder()
                .build();
        String miniUrl;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        switch (range) {

            case weekly:
                miniUrl = "period_id=1DAY".concat("&time_end=".concat(formatter.format(date)).concat("&limit=7"));
                break;

            case oneMonth:
                 miniUrl = "period_id=1DAY".concat("&time_end=".concat(formatter.format(date)).concat("&limit=30"));
                break;

            default:
                miniUrl = "";

        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForCandle.concat(symbol).concat("/USD/history?".concat(miniUrl)))
                .newBuilder();

        String url = urlBuilder.build().toString();

        final Request request = new Request.Builder().url(url)
                .addHeader("X-CoinAPI-Key", this.API_KEY_Candles)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            private void extractCandles(Response response){
                try {
                    String resp=response.body().string();
                    JSONArray jsonArray=new JSONArray(resp);
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject model=jsonArray.getJSONObject(i);
                        double open=Double.parseDouble(String.valueOf(model.get("price_open")));
                        double high=Double.parseDouble(String.valueOf(model.get("price_high")));
                        double low=Double.parseDouble(String.valueOf(model.get("price_low")));
                        double close=Double.parseDouble(String.valueOf(model.get("price_close")));
                        Candle candle=new Candle(symbol,open,close,high,low);
                        allCandles.put(candle);

                    }
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                extractCandles(response);
            }
        });
    }



}
