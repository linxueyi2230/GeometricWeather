package com.ego.shadow;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxy
 * @time 2019/6/29 15:25
 */
public class Splash implements SplashADListener {

    private AppCompatActivity activity;
    private ViewGroup container;
    private TextView skipView;
    private View splashHolder;

    public boolean canJump = false;
    private boolean needStart = true;

    /**
     * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
     * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
     * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值（单位ms）
     **/
    private int minSplashTimeWhenNoAD = 3000;
    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;

    private Handler handler = new Handler(Looper.getMainLooper());

    public Splash(Builder builder) {

        activity = builder.activity;
        container = builder.container;
        skipView = builder.skipView;
        splashHolder = builder.splashHolder;
        listener = builder.listener;
        skipView.setVisibility(View.INVISIBLE);

        final boolean from_background = activity.getIntent().getBooleanExtra("from_background", false);
        if (from_background) {
            toLaunch();
            return;
        }

        SharedPreferences sp = activity.getSharedPreferences("Ads",
                Context.MODE_PRIVATE);
        boolean agree = sp.getBoolean("agree", false);
        if (agree) {
            toLaunch();
        } else {
            toDialog();
        }
    }

    private void toLaunch(){
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        }else {
            init();
        }
    }

    private void toDialog() {
        Shadow.dialog(activity, new Shadow.Listener() {
            @Override
            public void agree() {
                toLaunch();
            }

            @Override
            public void disagree() {
                init();
            }
        });
    }

    /**
     *
     * ----------非常重要----------
     *
     * Android6.0以上的权限适配简单示例：
     *
     * 如果targetSDKVersion >= 23，那么必须要申请到所需要的权限，再调用广点通SDK，否则广点通SDK不会工作。
     *
     * Demo代码里是一个基本的权限申请示例，请开发者根据自己的场景合理地编写这部分代码来实现权限申请。
     * 注意：下面的`checkSelfPermission`和`requestPermissions`方法都是在Android6.0的SDK中增加的API，如果您的App还没有适配到Android6.0以上，则不需要调用这些方法，直接调用广点通SDK即可。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<String>();
        if (!checkSelfPermission(Manifest.permission.READ_PHONE_STATE)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // 权限都已经有了，那么直接调用SDK
        if (lackedPermission.isEmpty()) {
            init();

        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] permissions = new String[lackedPermission.size()];
            lackedPermission.toArray(permissions);
            ActivityCompat.requestPermissions(activity, permissions, 1024);
        }
    }

    private boolean checkSelfPermission(String permission){
        return ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1024 && hasAllPermissionsGranted(grantResults)) {
            init();
        }else {
            //没有权限
            if (!Ads.tencent(activity)) {
                Ads.pull(activity);
            }
            this.toApp();
        }
    }

    private void init() {
        listener.init();

//        if (Shadow.DEBUG) {
//            minSplashTimeWhenNoAD = 500;
//            this.toApp();
//            return;
//        }

        if (Ads.tencent(activity)) {
            splash();
            return;
        }

        JSONObject response = Ads.pull(activity);
        if (response != null && Ads.tencent(activity)) {
            splash();
            return;
        }

        this.toApp();
    }

    private void toApp(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                listener.onStartActivity(activity);
                activity.finish();
            }
        }, minSplashTimeWhenNoAD);
    }

    private void splash(){

        String appId = Ads.tencent_app_id(activity);
        String posId = Ads.tencent_splash_id(activity);

        StringBuilder log = new StringBuilder();
        log.append("有广告配置，appID：").append(appId).append(" ;闪屏广告ID：").append(posId);
        Log.e("Ads", log.toString());

        fetchSplashADTime = System.currentTimeMillis();
        new SplashAD(activity, container, skipView, appId, posId, this, 0);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onADDismissed() {
        Log.i("Ads", "SplashADDismissed");
        next();
    }

    @Override
    public void onNoAD(AdError error) {
        Log.e("Ads", String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(), error.getErrorMsg()));
        /**
         * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
         * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
         * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值
         **/
        long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间
        long shouldDelayMills = alreadyDelayMills > minSplashTimeWhenNoAD ? 0 : minSplashTimeWhenNoAD - alreadyDelayMills;//为防止加载广告失败后立刻跳离开屏可能造成的视觉上类似于"闪退"的情况，根据设置的minSplashTimeWhenNoAD
        // 计算出还需要延时多久
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (needStart) {
                    listener.onStartActivity(activity);
                }
                activity.finish();
            }
        }, shouldDelayMills);
    }

    @Override
    public void onADPresent() {
        skipView.setVisibility(View.VISIBLE);
//        skipView.start();
        splashHolder.setVisibility(View.INVISIBLE); // 广告展示后一定要把预设的开屏图片隐藏起来
        Log.i("Ads", "SplashAD onADPresent");
    }

    @Override
    public void onADClicked() {
        Log.i("Ads", "SplashAD onADClicked");
        DBHelper.with(activity).clickable(DBHelper.AD_SPLASH);
    }

    @Override
    public void onADTick(long millisUntilFinished) {
        Log.i("Ads", "SplashADTick " + millisUntilFinished + "ms");
    }

    @Override
    public void onADExposure() {
        Log.i("Ads", "SplashADExposure");
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
        if (canJump) {
            if (needStart) {
                listener.onStartActivity(activity);
            }
            activity.finish();
        } else {
            canJump = true;
        }
    }

    public void onPause() {
        canJump = false;
    }

    public void onResume() {
        if (canJump) {
            next();
        }
        canJump = true;
    }

    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
    }

    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return false;
    }

    private Listener listener;
    public interface Listener{
        void init();
        void onStartActivity(AppCompatActivity activity);
    }

    public static class Builder{
        private AppCompatActivity activity;
        private ViewGroup container;
        private TextView skipView;
        private View splashHolder;
        private Listener listener;

        public Builder activity(AppCompatActivity activity){
            this.activity = activity;
            return this;
        }

        public Builder container(ViewGroup container){
            this.container = container;
            return this;
        }

        public Builder skipView(TextView skipView){
            this.skipView = skipView;
            return this;
        }

        public Builder splashHolder(View splashHolder){
            this.splashHolder = splashHolder;
            return this;
        }

        public Builder listener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public Splash builder(){
            Splash splash = new Splash(this);
            return splash;
        }
    }
}
