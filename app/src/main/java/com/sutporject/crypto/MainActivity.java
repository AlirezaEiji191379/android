package com.sutporject.crypto;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.RequestBuilder;
import com.sutporject.crypto.Controller.ApiRequest;
import com.sutporject.crypto.Controller.CoinCandleController;
import com.sutporject.crypto.Controller.CoinMarketController;
import com.sutporject.crypto.model.Crypto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_LONG;

public class MainActivity extends AppCompatActivity {

    private int starIndexOfCoins;
    private final int limitOfCoins=5;
    private ApiRequest apiRequest;
    private ExecutorService executorService;
    private ListView lView;
    private Adapter listAdapter;
    private SwipeRefreshLayout refreshLayout;

    Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                starIndexOfCoins=starIndexOfCoins-limitOfCoins;
                findViewById(R.id.viewBtn).setEnabled(true);
                Toast.makeText(MainActivity.this,"please connect to internet for fetching more data",LENGTH_LONG).show();
                return;
            }
            ArrayList<Object> obj= (ArrayList<Object>) msg.obj;
            ArrayList<Crypto> allCrypto= (ArrayList<Crypto>) obj.get(0);
            ArrayList<RequestBuilder> allRbs= (ArrayList<RequestBuilder>) obj.get(1);

            lView = (ListView) findViewById(R.id.list);

            ArrayList<String> symbols = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> prices = new ArrayList<>();
            ArrayList<String> hour = new ArrayList<>();
            ArrayList<String> day = new ArrayList<>();
            ArrayList<String> week = new ArrayList<>();
            ArrayList<RequestBuilder> icons = new ArrayList<>();


            for (int i = 0; i < allCrypto.size(); i++) {
                Crypto crypto = allCrypto.get(i);
                symbols.add(crypto.getSymbol());
                names.add(crypto.getName());
                prices.add(Double.toString(crypto.getPrice()));
                hour.add(Double.toString(crypto.getChangesSinceLastHour()));
                day.add(Double.toString(crypto.getChangesSinceLastDay()));
                week.add(Double.toString(crypto.getChangesSinceLastWeek()));
                icons.add(allRbs.get(i));
            }

            if(listAdapter == null || refreshLayout.isRefreshing())
                listAdapter = new Adapter(MainActivity.this, symbols, names, icons,prices,hour,day,week);
            else
                listAdapter.addItems(symbols, names, icons,prices,hour,day,week);

            lView.setAdapter(listAdapter);

            findViewById(R.id.viewBtn).setEnabled(true);
            if(refreshLayout.isRefreshing())
                Toast.makeText(getApplicationContext(),"Updated!", LENGTH_LONG).show();
            refreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.starIndexOfCoins=1;
        apiRequest=new ApiRequest(this.handler,this);
        this.executorService= Executors.newCachedThreadPool();
        CoinMarketController cmc=new CoinMarketController(this,apiRequest,this.handler,this.starIndexOfCoins,this.limitOfCoins);
        cmc.setPriority(Integer.MAX_VALUE);
        this.executorService.execute(cmc);

        refreshLayout = findViewById(R.id.swiperefresh);
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if(checkDeviceConnection()==false) return;
                        apiRequest.setClearCache(true);
                        int numberOfThreads=(starIndexOfCoins+limitOfCoins-1)/limitOfCoins;
                        starIndexOfCoins=1;
                        executorService.shutdown();
                        executorService= new ThreadPoolExecutor(1,1,0L, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>(10, new Comparator<Runnable>() {
                            @Override
                            public int compare(Runnable t1, Runnable t2) {
                                if(((CoinMarketController) t1).getPriority()>((CoinMarketController) t2).getPriority()) return -1;
                                else if(((CoinMarketController) t1).getPriority()<((CoinMarketController) t2).getPriority()){return  1;}
                                return 0;
                            }
                        }));

                        for(int i=0;i<numberOfThreads;i++){
                            if(executorService.isTerminated()) executorService= new ThreadPoolExecutor(1,1,0L, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>(10, new Comparator<Runnable>() {
                                @Override
                                public int compare(Runnable t1, Runnable t2) {
                                    if(((CoinMarketController) t1).getPriority()>((CoinMarketController) t2).getPriority()) return -1;
                                    else if(((CoinMarketController) t1).getPriority()<((CoinMarketController) t2).getPriority()){return  1;}
                                    return 0;
                                }
                            }));
                            CoinMarketController cmc=new CoinMarketController(getApplicationContext(),apiRequest,handler,starIndexOfCoins,limitOfCoins);
                            cmc.setPriority(numberOfThreads-i);
                            starIndexOfCoins=starIndexOfCoins+limitOfCoins;
                            executorService.execute(cmc);
                        }
                        findViewById(R.id.viewBtn).setEnabled(false);
                        starIndexOfCoins=starIndexOfCoins-limitOfCoins;
//                        Log.i("priority2", String.valueOf(starIndexOfCoins));
                    }
                }
        );

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void btnClicked(View view){

        if(view.isEnabled()==false) return;
        view.setEnabled(false);
        if(this.executorService.isTerminated()) this.executorService=Executors.newCachedThreadPool();
        this.starIndexOfCoins=this.starIndexOfCoins+this.limitOfCoins;
        CoinMarketController cmc=new CoinMarketController(this,apiRequest,this.handler,this.starIndexOfCoins,this.limitOfCoins);
        cmc.setPriority(Integer.MAX_VALUE);
        this.executorService.execute(cmc);
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

    private boolean checkDeviceConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);///(ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        String status=null;
        if(isConnected==true){
            //status="you are connected!";
        }else{
            status="you are not connected!";
        }
        Toast.makeText(this,status, LENGTH_LONG).show();
        return isConnected;
    }
}

