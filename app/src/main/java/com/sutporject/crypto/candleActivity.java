package com.sutporject.crypto;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;


import com.sutporject.crypto.Controller.ApiRequest;
import com.sutporject.crypto.Controller.CoinCandleController;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.widget.Toast.LENGTH_LONG;

public class candleActivity extends Activity {

    private ApiRequest apiRequest;
    private ExecutorService executorService;

    Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView textView=(TextView) findViewById(R.id.textView1);
        textView.setText("rezaaa");
        apiRequest=new ApiRequest(this.handler,this);
        this.executorService= Executors.newCachedThreadPool();
        CoinCandleController c=new CoinCandleController(apiRequest,this,handler,"BTC", ApiRequest.Range.weekly);
        this.executorService.execute(c);
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

    public void back(View view){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    public void weekBtnClicked(View view){
        CoinCandleController c=new CoinCandleController(apiRequest,this,handler,"BTC", ApiRequest.Range.weekly);
        if(this.executorService.isTerminated())this.executorService=Executors.newCachedThreadPool();
        this.executorService.execute(c);

    }

    public void monthBtnClicked(View view){
        CoinCandleController c=new CoinCandleController(apiRequest,this,handler,"BTC", ApiRequest.Range.oneMonth);
        if(this.executorService.isTerminated())this.executorService=Executors.newCachedThreadPool();
        this.executorService.execute(c);
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
