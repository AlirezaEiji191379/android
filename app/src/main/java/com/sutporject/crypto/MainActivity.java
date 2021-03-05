package com.sutporject.crypto;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            TextView textView=(TextView) findViewById(R.id.textView);
            CharSequence lastText= textView.getText();
            textView.setText("");
            textView.setMovementMethod(new ScrollingMovementMethod());
            ArrayList<Crypto>all=(ArrayList<Crypto>)msg.obj;
            for(int i=0;i<all.size();i++){
                lastText=lastText+"\n\n"+all.get(i).toString();
            }
            textView.setText(lastText);
        }
    };

    private ApiRequest apiRequest;
    private ExecutorService executorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        apiRequest=new ApiRequest(this.handler,0);
        this.executorService= Executors.newCachedThreadPool();
        ///
        CoinMarketController cmc=new CoinMarketController(this,apiRequest);
        ///
        this.executorService.execute(cmc);
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
        CoinMarketController cmc=new CoinMarketController(this,apiRequest);
        CoinMarketController cmc1=new CoinMarketController(this,apiRequest);
        CoinMarketController cmc2=new CoinMarketController(this,apiRequest);
        CoinMarketController cmc3=new CoinMarketController(this,apiRequest);
        CoinMarketController cmc4=new CoinMarketController(this,apiRequest);
        CoinMarketController cmc5=new CoinMarketController(this,apiRequest);
        this.executorService.execute(cmc);
        this.executorService.execute(cmc1);
        this.executorService.execute(cmc2);
        this.executorService.execute(cmc3);
        this.executorService.execute(cmc4);
        this.executorService.execute(cmc5);
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