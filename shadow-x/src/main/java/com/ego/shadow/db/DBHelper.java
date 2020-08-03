package com.ego.shadow.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ego.shadow.entity.RewardRow;
import com.ego.shadow.entity.Row;
import com.ego.shadow.utils.RandomUtils;

import org.json.JSONArray;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by e on 2019/12/24.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "Shadow_SQLite";

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "shadow.db";//daily_task
    private static final String TABLE_AD_REWARD = "ad_reward";    //广告奖励
    private static final String TABLE_EXTRACT_RECORD = "extract_record";//提现记录

    //广告类型
    public static final int AD_BANNER = 0;      //banner广告
    public static final int AD_SPLASH = 1;      //闪屏广告
    public static final int AD_INTERSTITIAL = 2;  //插屏广告
    public static final int AD_NATIVE = 3;      //原生广告
    public static final int AD_REWARD = 4;      //视频激励广告

    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

    private Map<String,String> table = new HashMap<>(2);

    private static DBHelper instance;
    public static DBHelper with(Context context){
        if (instance == null){
            synchronized (DBHelper.class){
                if (instance == null){
                    instance = new DBHelper(context);
                }
            }
        }

        return instance;
    }

    private DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);

        table.put(TABLE_AD_REWARD, "(id integer primary key AUTOINCREMENT,ad_type integer, date_time text,timestamp long,amount double,status integer)");
        table.put(TABLE_EXTRACT_RECORD, "(id integer primary key AUTOINCREMENT, date_time text,timestamp long,amount double,status integer,daily_task_ids text,account text)");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Set<String> keys = table.keySet();
        for (String key : keys) {
            StringBuilder sql = new StringBuilder();
            sql.append("create table if not exists ");
            sql.append(key);
            sql.append(table.get(key));

            db.execSQL(sql.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public String ad(int ad_type) {
        if (ad_type == AD_BANNER) {
            return "Banner广告";
        }
        if (ad_type == AD_SPLASH) {
            return "闪屏广告";
        }
        if (ad_type == AD_INTERSTITIAL) {
            return "插屏广告";
        }
        if (ad_type == AD_NATIVE) {
            return "原生广告";
        }
        if (ad_type == AD_REWARD) {
            return "视频激励广告";
        }
        return "其他";
    }

    public int rewardProgress() {
        return progress(AD_REWARD, 10);
    }

    public boolean clickable(int ad_type) {

        if (progress(ad_type, 10) > 0){
            double amount = RandomUtils.reward();
            reward(ad_type,amount);
            return true;
        }

        return false;
    }

    public int progress(int ad_type) {
        return progress(ad_type,10);
    }

    public int progress(int ad_type,int max) {

        String date_time = today();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(id) from ").append(TABLE_AD_REWARD).append(" WHERE ad_type = ? AND date_time = ?");

        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery(sql.toString(), new String[]{String.valueOf(ad_type), date_time});
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }

        int progress = max - count;

        String ad = ad(ad_type);
        StringBuilder log = new StringBuilder();
        log.append("--------------每日任务 start--------------\n");
        log.append("任务：").append(ad).append("\n");
        log.append("每日最多有 ").append(max).append(" 次机会\n");
        log.append("今日已完成 ").append(count).append(" 次\n");
        log.append("今日剩余 ").append(progress).append(" 次\n");
        log.append("--------------每日任务 end--------------");

        Log.i(TAG, sql.toString());
        Log.i(TAG, log.toString());

        cursor.close();
        return progress;
    }

    public String today() {

        String date = format.format(new Date());
        return date;
    }

    public void delete() {
        String date_time = today();
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_AD_REWARD, "date_time=?", new String[]{date_time});
    }

    public void reward(int ad_type, double amount) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db.isDbLockedByCurrentThread()) {
                reward(db, ad_type, amount);
            } else {
                db.beginTransaction();
                try {
                    reward(db, ad_type, amount);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reward(SQLiteDatabase db,int ad_type,double amount){

        ContentValues cv = new ContentValues();
        cv.put("ad_type",ad_type);
        cv.put("date_time", today());
        cv.put("timestamp", System.currentTimeMillis());
        cv.put("status", 0);
        cv.put("amount", amount);
        long result = db.insert(TABLE_AD_REWARD, null, cv);
        if (result != -1) {
            Log.i(TAG, "insert reward " + result + " success");
        }
    }

    public String balance() {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(amount) AS total from ").append(TABLE_AD_REWARD).append(" WHERE status = ?");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(), new String[]{"0"});
        double total = 0.0;
        if (cursor.moveToNext()){
            total = cursor.getDouble(0);
        }
        cursor.close();

        DecimalFormat format = new DecimalFormat("0.00");
        String result = format.format(total);
        return result;
    }

    public void extract(String account){
        try {

            //query
            StringBuilder query = new StringBuilder();
            query.append("SELECT * from ").append(TABLE_AD_REWARD).append(" WHERE status = ?");

            Cursor cursor = getReadableDatabase().rawQuery(query.toString(), new String[]{"0"});
            double total = 0.0;

            JSONArray ids = new JSONArray();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double amount = cursor.getDouble(cursor.getColumnIndex("amount"));
                ids.put(id);
                total += amount;
            }

            cursor.close();

            DecimalFormat format = new DecimalFormat("0.00");
            String result = format.format(total);

            SQLiteDatabase database = getWritableDatabase();
            //insert
            ContentValues insert = new ContentValues();
            insert.put("date_time", today());
            insert.put("timestamp", System.currentTimeMillis());
            insert.put("status", 0);
            insert.put("amount", Double.parseDouble(result));
            insert.put("account", account);
            insert.put("daily_task_ids", ids.toString());
            database.insert(TABLE_EXTRACT_RECORD, null, insert);

            //update
            int size = ids.length();
            for (int i = 0; i < size; i++) {
                int id = ids.getInt(i);
                ContentValues update = new ContentValues();
                update.put("status", 1);
                database.update(TABLE_AD_REWARD, update, "id=?", new String[]{String.valueOf(id)});
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(int id){
        ContentValues cv = new ContentValues();
        cv.put("status", 1);
        getWritableDatabase().update(TABLE_EXTRACT_RECORD, cv, "id=?", new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_EXTRACT_RECORD, "id=?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RewardRow> getRewardRows(){
        List<RewardRow> rewardRows = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * from ").append(TABLE_AD_REWARD).append(" order by timestamp DESC");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(),null);
        while (cursor.moveToNext()) {
            RewardRow row = new RewardRow();
            row.id = cursor.getInt(cursor.getColumnIndex("id"));
            row.date_time = cursor.getString(cursor.getColumnIndex("date_time"));
            row.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            row.amount = cursor.getDouble(cursor.getColumnIndex("amount"));
            row.status = cursor.getInt(cursor.getColumnIndex("status"));
            row.ad_type = cursor.getInt(cursor.getColumnIndex("ad_type"));
            rewardRows.add(row);
        }

        return rewardRows;
    }

    public List<Row> getExtractRecord() {
        List<Row> rows = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * from ").append(TABLE_EXTRACT_RECORD).append(" order by timestamp DESC");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql.toString(), null);
        while (cursor.moveToNext()) {
            Row row = new Row();
            row.id = cursor.getInt(cursor.getColumnIndex("id"));
            row.date_time = cursor.getString(cursor.getColumnIndex("date_time"));
            row.timestamp = cursor.getLong(cursor.getColumnIndex("timestamp"));
            row.amount = cursor.getDouble(cursor.getColumnIndex("amount"));
            row.status = cursor.getInt(cursor.getColumnIndex("status"));
            rows.add(row);
        }

        return rows;
    }

}
