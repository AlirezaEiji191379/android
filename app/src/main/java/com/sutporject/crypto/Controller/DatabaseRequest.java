package com.sutporject.crypto.Controller;

import android.database.sqlite.SQLiteDatabase;

import com.sutporject.crypto.model.Crypto;

public class DatabaseRequest {

    private static DatabaseRequest dbController;
    private SQLiteDatabase db;
    private boolean isCreationDone;

    private DatabaseRequest(){}

    public static DatabaseRequest getInstance(){
        if(dbController==null) {
            dbController=new DatabaseRequest();
            dbController.db=SQLiteDatabase.openOrCreateDatabase("crypto_project",null);
        }
        return dbController;
    }


    public void CreateTableForCrypto(){
        db.execSQL("Create table if not exists crypto (id INTEGER primary key Not null," +
                "name varhcar unique Not null, symbol varchar unique not Null, price Real ,last_hour Real ," +
                "last_day Real , last_week Real) ;");
    }

    public void InsertCryptoInDataBase(Crypto crypto){
        db.execSQL("Insert or Replace into crypto (id,name,symbol,price,last_hour,last_day,last_week) values " +
                "("+crypto.getId()+","+crypto.getName()+","+crypto.getSymbol()+","+crypto.getPrice()+","+crypto.getChangesSinceLastHour()+"" +
                ","+crypto.getChangesSinceLastDay()+","+crypto.getChangesSinceLastWeek()+")");

    }

}
