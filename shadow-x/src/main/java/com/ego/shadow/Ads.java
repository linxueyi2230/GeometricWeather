package com.ego.shadow;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author lxy
 * @time 2019/4/19 12:36
 */
public class Ads {

    //国际版
    private static String APP_ID = "YvqFyjgInyJQydok0yoSjeSK-MdYXbMMI";
    private static String APP_KEY = "f9XIY5gktlbVYM5nRnj4P3yH";
    private static final String CLAZZ = "Ads";

    private static final String TAG = "ShadowAds";
    private static int mActivityCount = 0;
    private static boolean background = false;
    private static long mBeginTimeMillis = 0;

    private static void log(String log) {
        Log.e(TAG, log);
    }

    public static void initialize(final Application application) {

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.i(TAG,"onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.i(TAG,"onActivityStarted");
            }

            @Override
            public void onActivityResumed(Activity activity) {
                mActivityCount++;
                if (background) {
                    background = false;

                    SharedPreferences sp = activity.getSharedPreferences("Ads",
                            Context.MODE_PRIVATE);
                    boolean agree = sp.getBoolean("agree", false);
                    if (!agree) {
                        return;
                    }

                    //应用进入前台
                    Log.i(TAG, "应用进入前台");
                    if (!Ads.tencent(activity)){
                        return;
                    }

                    long span = System.currentTimeMillis() - mBeginTimeMillis;
                    long max = 2 * 60 * 1000;
                    if (Shadow.DEBUG) {
                        max = 6 * 1000;
                    }
                    if (span >= max) {
                        mBeginTimeMillis = System.currentTimeMillis();
                        Intent intent = new Intent(activity,ShadowActivity.class);
                        intent.putExtra("from_background",true);
                        activity.startActivity(intent);
                    }

                }
                StringBuilder sb = new StringBuilder();
                sb.append("onActivityResumed").append(" mActivityCount = ").append(mActivityCount).append(" background = ").append(background);

                Log.i(TAG,sb.toString());
            }

            @Override
            public void onActivityPaused(Activity activity) {
                String act = activity.getClass().getSimpleName();
                if (TextUtils.equals("ShadowActivity", act) && mActivityCount >= 2) {
                    mActivityCount--;
                }

                Log.i(TAG,"onActivityPaused");
            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityCount--;
                if (mActivityCount <= 0) {
                    background = true;
                    mBeginTimeMillis = System.currentTimeMillis();
                    Log.i(TAG,"应用进入后台");
                    //应用进入后台
                }

                StringBuilder sb = new StringBuilder();
                sb.append("onActivityStopped").append(" mActivityCount = ").append(mActivityCount).append(" background = ").append(background);

                Log.i(TAG,sb.toString());

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.i(TAG,"onActivityDestroyed");
            }
        });
    }

    public static JSONObject pull(final Context context) {
        FutureTask<JSONObject> task = new FutureTask<>(new Callable<JSONObject>() {
            @Override
            public JSONObject call() throws Exception {
                return init(context);
            }
        });

        new Thread(task).start();

        try {
            return task.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JSONObject init(final Context context) throws Exception {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);

        String objectId = sp.getString("objectId", null);

        //如果有objectId，直接查询
        if (!TextUtils.isEmpty(objectId)) {
            log("objectId = " + objectId);

            StringBuilder url = url();
            url.append("/").append(objectId);

            String json = get(url.toString());//{"app_pkg":"com.shenma.calculator","createdAt":"2019-07-04T05:44:55.807Z","updatedAt":"2019-07-04T05:57:23.500Z","app_name":"\u795e\u9a6c\u8ba1\u7b97\u5668","objectId":"5d1d925712215f0093e80b82"}
            return apply(context, json);
        }

        String pkg = context.getPackageName();
        JSONObject whereParams = new JSONObject();
        whereParams.put("app_pkg", pkg);
        String whereJson = where(whereParams.toString());
        JSONObject whereResponse = new JSONObject(whereJson);

        if (whereResponse.has("results")) {
            JSONArray results = whereResponse.getJSONArray("results");
            if (results.length() > 0) {
                log("有记录");
                JSONObject result = results.getJSONObject(0);
                return apply(context, result);

            } else {
                return create(context);
            }

        } else {
            return create(context);
        }
    }

    private static JSONObject create(Context context) throws Exception{

        String pkg = context.getPackageName();
        String app_name = getAppName(context);

        log("没有记录，新增一条记录");
        JSONObject create = new JSONObject();
        create.put("app_pkg", pkg);
        create.put("app_name", app_name);

        create.put("admob_appid", "ca-app-pub-3074315780074099~9979737779");
        create.put("admob_interstitial_id", "ca-app-pub-3074315780074099/7600986440");
        create.put("admob_banner_id", "ca-app-pub-3074315780074099/4720097262");
        create.put("admob_enable", false);

        JSONObject channel = new JSONObject();
        channel.put("shadow",true);
        channel.put("xiaomi",false);
        channel.put("huawei",false);
        channel.put("wandoujia",false);
        channel.put("vivo",false);
        channel.put("baidu",false);
        channel.put("meizu",false);
        channel.put("yingyongbao",false);
        channel.put("qihoo360",false);
        channel.put("sougou",false);
        channel.put("anzhi",false);
        channel.put("yidongmm",false);
        channel.put("coolapk",false);

        create.put("channel",channel);

        String json = post(create.toString());//{"objectId":"5d1dfa8ea91c930074f1efd8","createdAt":"2019-07-04T13:09:34.835Z"}
        return apply(context, json);
    }

    public static String getAppName(Context context) {
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //获取应用 信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            //获取albelRes
            int labelRes = applicationInfo.labelRes;
            //返回App的名称
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject apply(Context context, String json) throws Exception {
        if (TextUtils.isEmpty(json)){
            return null;
        }

        JSONObject result = new JSONObject(json);
        return apply(context, result);
    }

    private static JSONObject apply(Context context, JSONObject result) throws Exception {

        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("result", result.toString());

        editor.putString("objectId", result.optString("objectId", null));
        editor.putString("app_name", result.optString("app_name", null));
        editor.putString("app_pkg", result.optString("app_pkg", null));
        editor.putInt("count", result.optInt("count", 1));

        //google admob
        String admob_appid = result.optString("admob_appid", null);
        editor.putString("admob_appid", admob_appid);
        editor.putString("admob_banner_id", result.optString("admob_banner_id", null));
        editor.putString("admob_interstitial_id", result.optString("admob_interstitial_id", null));
        editor.putBoolean("admob_enable", result.optBoolean("admob_enable", false));

        //腾讯广点通
        editor.putString("tencent_app_id", result.optString("tencent_app_id", null));
        editor.putString("tencent_splash_id", result.optString("tencent_splash_id", null));
        editor.putString("tencent_banner_id", result.optString("tencent_banner_id", null));
        editor.putString("tencent_interstitial_id", result.optString("tencent_interstitial_id", null));
        editor.putString("tencent_native_id", result.optString("tencent_native_id", null));
        editor.putString("tencent_reward_id", result.optString("tencent_reward_id", null));

        if (result.has("channel")) {

            String channelName = channel(context);
            log("已设置渠道：" + channelName);

            JSONObject channel = result.optJSONObject("channel");
            if (channel != null && channel.has(channelName)) {
                log("本地渠道号与云端渠道号匹配：" + channelName);
                editor.putBoolean("tencent_enable", channel.optBoolean(channelName, false));
            } else {
                log("没有匹配的渠道号");
                editor.putBoolean("tencent_enable", result.optBoolean("tencent_enable", false));
            }

        } else {
            log("没有设置渠道号");
            editor.putBoolean("tencent_enable", result.optBoolean("tencent_enable", false));
        }

        int open_count = sp.getInt("open_count", 1);
        open_count++;
        editor.putInt("open_count", open_count);

        editor.apply();

        return result;
    }

    private static String channel(Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (info.metaData.containsKey("channel")) {
                return info.metaData.getString("channel");
            }
            return "shadow";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "shadow";
        }
    }

    public static boolean admob(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);

        boolean admob_enable = sp.getBoolean("admob_enable", false);
        String admob_appid = sp.getString("admob_appid", null);
        return admob_enable && !TextUtils.isEmpty(admob_appid);
    }

    public static String admob_banner_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("admob_banner_id", null);
    }

    public static String admob_interstitial_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("admob_interstitial_id", null);
    }

    public static boolean tencent(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);

        boolean tencent_enable = sp.getBoolean("tencent_enable", false);
        String tencent_app_id = sp.getString("tencent_app_id", null);
        return tencent_enable && !TextUtils.isEmpty(tencent_app_id);
    }

    public static String tencent_app_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_app_id", null);
    }

    public static String tencent_splash_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_splash_id", null);
    }

    public static String tencent_banner_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_banner_id", null);
    }

    public static String tencent_interstitial_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_interstitial_id", null);
    }

    public static String tencent_native_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_native_id", null);
    }

    public static String tencent_reward_id(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        return sp.getString("tencent_reward_id", null);
    }

    public static boolean reward(Context context) {
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);

        boolean tencent_enable = sp.getBoolean("tencent_enable", false);
        String tencent_app_id = sp.getString("tencent_app_id", null);
        String tencent_reward_id = sp.getString("tencent_reward_id", null);
        return tencent_enable && !TextUtils.isEmpty(tencent_app_id) && !TextUtils.isEmpty(tencent_reward_id);
    }

    private static String where(String params) throws Exception {
        StringBuilder url = url();
        String result = URLEncoder.encode(params, "utf-8");
        url.append("?where=").append(result);
        return request(url.toString(), "GET", null);
    }

    private static String get(String url) throws Exception {
        return request(url, "GET", null);
    }

    private static StringBuilder url() {
        StringBuilder url = new StringBuilder();
//        url.append("https://hbxfajoi.api.lncld.net/1.1/classes/").append(CLAZZ);
        url.append("https://us.leancloud.cn/1.1/classes/").append(CLAZZ);
        return url;
    }

    private static String post(String params) throws Exception {
        String url = url().toString();
        return request(url, "POST", params);
    }

    private static String request(String url, String method, String params) throws Exception {

        StringBuilder json = new StringBuilder();

        long timestamp = System.currentTimeMillis();

        String md5 = MD5(timestamp + APP_KEY);
        String sign = String.format("%s,%s", md5, timestamp);

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {

            Log.e("Request", url);

            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestProperty("X-LC-Id", APP_ID);
            connection.setRequestProperty("X-LC-Sign", sign);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod(method);
            connection.setConnectTimeout(3000);

            if (TextUtils.equals("POST", method)) {
                output = connection.getOutputStream();
                output.write(params.getBytes());
            }

            input = connection.getInputStream();

            byte[] buf = new byte[1024];
            int count;
            while ((count = input.read(buf)) != -1) {
                json.append(new String(buf, 0, count, Charset.forName("utf-8")));
            }

            input.close();

            Log.e("Response", json.toString());


        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json.toString();
    }

    private static String MD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());

            byte byteData[] = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++)
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int open_count(Context context){
        SharedPreferences sp = context.getSharedPreferences(CLAZZ,
                Context.MODE_PRIVATE);
        int open_count = sp.getInt("open_count", 1);
        return open_count;
    }
}
