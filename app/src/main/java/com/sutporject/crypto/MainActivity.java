package com.sutporject.crypto;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sutporject.crypto.Controller.ApiRequest;
import com.sutporject.crypto.Controller.CoinMarketController;
import com.sutporject.crypto.model.Crypto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private int starIndexOfCoins;
    private final int limitOfCoins=3;
    private ApiRequest apiRequest;
    private ExecutorService executorService;
    //https://s2.coinmarketcap.com/static/img/coins/64x64/1.png
    Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            TextView textView=(TextView) findViewById(R.id.textView);
            CharSequence lastText= textView.getText();
            textView.setText("");
            textView.setMovementMethod(new ScrollingMovementMethod());
            ArrayList<Object> obj= (ArrayList<Object>) msg.obj;
            ArrayList<Crypto> allCrypto= (ArrayList<Crypto>) obj.get(0);
            ArrayList<RequestBuilder> allRbs= (ArrayList<RequestBuilder>) obj.get(1);
            for(int i=0;i<allCrypto.size();i++){
                lastText=lastText+"\n\n"+allCrypto.get(i).toString();
            }
            allRbs.get(0).into((ImageView) findViewById(R.id.imageView));
            allRbs.get(1).into((ImageView) findViewById(R.id.imageView2));
            allRbs.get(2).into((ImageView) findViewById(R.id.imageView3));
            textView.setText(lastText);
            findViewById(R.id.viewBtn).setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.starIndexOfCoins=1;
        //ImageView imageView=(ImageView) findViewById(R.id.imageView);
        //Glide.with(this).load("https://s2.coinmarketcap.com/static/img/coins/64x64/1.png").diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageView);
        apiRequest=new ApiRequest(this.handler,this);
        this.executorService= Executors.newCachedThreadPool();
        ///
        CoinMarketController cmc=new CoinMarketController(this,apiRequest,this.handler,this.starIndexOfCoins,this.limitOfCoins);
        ///
        this.executorService.execute(cmc);
        //executorService.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);///(ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        String status=null;
        if(isConnected==true){
            status="you are connected!";
        }else{
            status="you are not connected!";
        }
        Toast.makeText(this,status, LENGTH_LONG).show();
    }

    public void btnClicked(View view){
        if(view.isEnabled()==false) return;
        view.setEnabled(false);
        if(this.executorService.isTerminated()) this.executorService=Executors.newCachedThreadPool();
        this.starIndexOfCoins=this.starIndexOfCoins+this.limitOfCoins;
        CoinMarketController cmc=new CoinMarketController(this,apiRequest,this.handler,this.starIndexOfCoins,this.limitOfCoins);
        this.executorService.execute(cmc);
//        this.executorService.shutdown();
    }

    public void nextPageClicked(View view){
        Intent intent=new Intent(this,candleActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




}