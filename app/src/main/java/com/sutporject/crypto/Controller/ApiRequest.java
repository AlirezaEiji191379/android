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

    private final String API_KEY_CoinMarket="b7338e0c-e7d6-484c-ade8-77c577cb7773";
    private final String API_KEY_Candles="80A71684-F679-44B2-BBE0-A0C9B7E49597";
    private final String urlForData="https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
    private final String urlForImage="https://pro-api.coinmarketcap.com/v1/cryptocurrency/info";
    private final String urlForCandle="https://rest.coinapi.io/v1/ohlcv/";
    private Context context;
    private Handler handler;
    private boolean done=true;
    private ArrayBlockingQueue<Crypto> allFetchedCrypto;////must be corrected!
    private ArrayBlockingQueue<RequestBuilder> allFetchedDrawables;////must be corrected!

    private ArrayBlockingQueue<Candle> allCandles;


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
    }

    public void setAllCandles(ArrayBlockingQueue<Candle> allCandles) {
        this.allCandles = allCandles;
    }

    public void setAllFetchedCrypto(ArrayBlockingQueue<Crypto> allFetchedCrypto) {
        this.allFetchedCrypto = allFetchedCrypto;
    }

    public void setAllFetchedDrawables(ArrayBlockingQueue<RequestBuilder> allFetchedDrawables) {
        this.allFetchedDrawables = allFetchedDrawables;
    }

    public synchronized void  doGetRequestForCryptoData(int start, int limit) throws IOException{
        this.done=false;
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
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
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
            private void extractResponseFromRequest(Response response){

               done=true;
               String resp="";
                try {
                    resp=response.body().string();
                    Log.i("responses", resp);
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONArray data=  jsonResponse.getJSONArray("data");
                    for(int i=0;i<data.length();i++){
                        JSONObject model=data.getJSONObject(i);
                        int id=model.getInt("id");
                        JSONObject quote=model.getJSONObject("quote");
                        JSONObject usd=quote.getJSONObject("USD");
                        String name= (String) model.get("name");
                        String symbol=(String) model.get("symbol");
                        Log.i("responses", symbol);
                        double price= Double.parseDouble(String.valueOf(usd.get("price")));
                        double percentage_change_1h=Double.parseDouble(String.valueOf(usd.get("percent_change_1h")));
                        double percentage_change_24h=Double.parseDouble (String.valueOf(usd.get("percent_change_24h")));
                        double percentage_change_7d=Double.parseDouble(String.valueOf(usd.get("percent_change_7d")));
                        Crypto newCrypto=new Crypto(id,name,symbol,price,percentage_change_1h,percentage_change_7d,percentage_change_24h);
                        allFetchedCrypto.put(newCrypto);
                    }
                    Log.i("responses", "-------------------------------------------");
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Message message=Message.obtain();
                message.what=0;
                handler.sendMessage(message);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //Log.i("responseXXXXX", response.body().string());
                extractResponseFromRequest(response);
            }
        });
    }

    public synchronized void doGetRequestForCryptoLogo(String id){
        this.done=false;
        allFetchedDrawables.clear();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(this.urlForImage).newBuilder();
        urlBuilder.addQueryParameter("id", String.valueOf(id));
        String url = urlBuilder.build().toString();
        Request request=new Request.Builder().url(url)
                .addHeader("Content-type","application/json")
                .addHeader("X-CMC_PRO_API_KEY",this.API_KEY_CoinMarket)
                .build();
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
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
            private void extractResponseFromRequest(Response response){
                RequestOptions myOptions = new RequestOptions()
                        .override(50, 50);
                done=true;
                try {
                    String resp=response.body().string();
                    JSONObject jsonResponse=new JSONObject(resp);
                    JSONObject data=  jsonResponse.getJSONObject("data");
                    String [] tokenize=id.split(",");
                    for(int i=0;i<tokenize.length;i++){
                        JSONObject model=data.getJSONObject(tokenize[i]);
                        String logoUrl= (String) model.get("logo");
                        allFetchedDrawables.put(Glide.with(context).asBitmap().apply(myOptions).load(logoUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE));
                        Log.i("logo Url:",logoUrl);
                    }
                } catch (IOException | JSONException | InterruptedException e) {
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
    }

    public synchronized void doGetRequestForCandles(String symbol, Range range) {
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient httpClient= new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor(this.context))
                .cache(cache)
                .build();

        String miniUrl;
        final String description;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date();
        Log.i("datte::", String.valueOf(formatter.format(date)));
        switch (range) {

            case weekly:
                miniUrl = "period_id=1DAY".concat("&time_end=".concat(formatter.format(date)).concat("&limit=7"));
                description = "Daily candles from now";
                break;

            case oneMonth:
                 miniUrl = "period_id=1DAY".concat("&time_end=".concat(formatter.format(date)).concat("&limit=30"));
                description = "Daily candles from now";
                break;

            default:
                miniUrl = "";
                description = "";

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
                        Log.i("open::", String.valueOf(open));
                    }
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.v("TAG", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                extractCandles(response);
            }
        });
    }
}
