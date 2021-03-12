package com.sutporject.crypto;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ColorStateListDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.RequestBuilder;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.sutporject.crypto.Controller.ApiRequest;
import com.sutporject.crypto.Controller.CoinCandleController;
import com.sutporject.crypto.Controller.CoinMarketController;
import com.sutporject.crypto.model.Candle;
import com.sutporject.crypto.model.Crypto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
    private boolean firstTime = true;
    private ArrayList<String> allSymbols = new ArrayList<>();
    private boolean loadMore = false;
    private View popupView;
    private PopupWindow popupWindow;
    private boolean sevenCandle = true;
    private boolean popupMessage = false;
    private CandleStickChart chart;


    Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==0){
                starIndexOfCoins=starIndexOfCoins-limitOfCoins;
                if(starIndexOfCoins<0) starIndexOfCoins=1;
                executorService.shutdown();
                findViewById(R.id.viewBtn).setEnabled(true);
                Toast.makeText(MainActivity.this,"please connect to internet for fetching more data",LENGTH_LONG).show();
                return;
            }

            if(!popupMessage){
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

                if(firstTime){
                    allSymbols.addAll(symbols);
                    firstTime = false;
                }if(loadMore){
                    allSymbols.addAll(symbols);
                    loadMore = false;
                }

                findViewById(R.id.viewBtn).setEnabled(true);
                if(refreshLayout.isRefreshing())
                    Toast.makeText(getApplicationContext(),"Updated!", LENGTH_LONG).show();
                refreshLayout.setRefreshing(false);
            }


            else {
                ArrayList<Candle>allCandles= (ArrayList<Candle>) msg.obj;

                chart = popupView.findViewById(R.id.candle_stick_chart);
                ArrayList<CandleEntry> sticks = new ArrayList<>();
                if(sevenCandle){
                    for (int i = 0; i < 7; i++) {
                        Candle candle = allCandles.get(i);
                        sticks.add(new CandleEntry(i+1,(float)candle.getHigh(),(float)candle.getLow(),
                                (float)candle.getOpen(),(float)candle.getClose()));
                    }
                }else{
                    for (int i = 0; i < 30; i++) {
                        Candle candle = allCandles.get(i);
                        sticks.add(new CandleEntry(i+1,(float)candle.getHigh(),(float)candle.getLow(),
                                (float)candle.getOpen(),(float)candle.getClose()));
                    }
                }

                CandleDataSet set1 = new CandleDataSet(sticks, "DataSet 1");

                set1.setColor(Color.rgb(80, 80, 80));
                set1.setShadowColor(Color.DKGRAY);
                set1.setShadowWidth(0.7f);
                set1.setDecreasingColor(Color.RED);
                set1.setDecreasingPaintStyle(Paint.Style.FILL);
                set1.setIncreasingColor(Color.rgb(122, 242, 84));
                set1.setIncreasingPaintStyle(Paint.Style.STROKE);
                set1.setNeutralColor(Color.BLUE);
                set1.setValueTextColor(Color.RED);
                set1.setDrawValues(false);

                CandleData data = new CandleData(set1);
                chart.setData(data);
                chart.invalidate();
                popupMessage = false;
            }

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
                        if(checkDeviceConnection()==false) {
                            refreshLayout.setRefreshing(false);
                            return;
                        }
                        apiRequest.setClearCache(true);
                        int numberOfThreads=0;
                        //int numberOfThreads=(starIndexOfCoins+limitOfCoins-1)/limitOfCoins;
                        Log.i("main","index of coins "+ starIndexOfCoins);
                        if(starIndexOfCoins==1) numberOfThreads=1;
                        else numberOfThreads=(starIndexOfCoins+limitOfCoins-1)/limitOfCoins;
                        starIndexOfCoins=1;
                        //executorService.shutdown();
                        executorService= new ThreadPoolExecutor(1,1,0L, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>(10, new Comparator<Runnable>() {
                            @Override
                            public int compare(Runnable t1, Runnable t2) {
                                if(((CoinMarketController) t1).getPriority()>((CoinMarketController) t2).getPriority()) return -1;
                                else if(((CoinMarketController) t1).getPriority()<((CoinMarketController) t2).getPriority()){return  1;}
                                return 0;
                            }
                        }));
                        Log.i("main","number of threads "+ numberOfThreads);
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
                        if(starIndexOfCoins<0) starIndexOfCoins=1;
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
        loadMore = true;
    }

    private void showPopup(View view){
        LayoutInflater popupInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = popupInflater.inflate(R.layout.popupdiagram,null);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        popupWindow = new PopupWindow(popupView,width-100,height-170,true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(70);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        popupWindow.update(0, 0, popupWindow.getWidth(), popupWindow.getHeight());
    }

    public void nextPageClicked(View view){
//        popupMessage = true;

        lView = (ListView) findViewById(R.id.list);
        int position = lView.getPositionForView(view);
        String popupSymbol = allSymbols.get(position);

        showPopup(view);

        Spinner candleSpinner = (Spinner) popupView.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.candlePopup, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        candleSpinner.setAdapter(spinnerAdapter);

        candleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                popupMessage = true;

                Log.i("selected",Integer.toString(i));

                if(i == 0)
                    sevenCandle = true;
                else
                    sevenCandle = false;
                apiRequest=new ApiRequest(handler,getApplicationContext());
                executorService= Executors.newCachedThreadPool();
                CoinCandleController c;
                if(sevenCandle)
                    c=new CoinCandleController(apiRequest,getApplicationContext(),handler,popupSymbol, ApiRequest.Range.weekly);
               else
                    c=new CoinCandleController(apiRequest,getApplicationContext(),handler,popupSymbol, ApiRequest.Range.oneMonth);
                executorService.execute(c);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                popupMessage = false;

            }
        });

        TextView popupText = (TextView) popupView.findViewById(R.id.popupSymbol);
        popupText.setText(popupSymbol);

//        apiRequest=new ApiRequest(this.handler,this);
//        this.executorService= Executors.newCachedThreadPool();
//        CoinCandleController c=new CoinCandleController(apiRequest,this,handler,popupSymbol, ApiRequest.Range.weekly);
//        this.executorService.execute(c);


//        allSymbols.get(position);
//        TextView symbol = (TextView) findViewById(R.id.symbolTxt);

//        Intent intent=new Intent(this,candleActivity.class);
//        intent.putExtra("symbol",allSymbols.get(position));
//        startActivity(intent);
    }

    public void closePopup(View view) {
        popupWindow.dismiss();
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

