package com.ego.shadow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.comm.util.AdError;

import java.util.Locale;

/**
 * 插屏广告
 */
public class Interstitial implements UnifiedInterstitialADListener {

    private final String TAG = "Ads";

    private Activity activity;
    private UnifiedInterstitialAD interstitialAD;
    private boolean autoShow = false;
    private boolean debug;
    private boolean clickable;

    public static Interstitial of(Fragment fragment){
        return new Interstitial(fragment.getActivity(),false);
    }

    public static Interstitial of(Fragment fragment,boolean debug){
        return new Interstitial(fragment.getActivity(),debug);
    }

    public static Interstitial of(Activity activity){
        return new Interstitial(activity,false);
    }

    public static Interstitial of(Activity activity,boolean debug){
        return new Interstitial(activity,debug);
    }

    public Interstitial(Activity activity,boolean debug) {
        this.activity = activity;
        this.debug = debug;
        int open_count = Ads.open_count(activity);
        if (open_count >= 15) {
            autoShow = true;
        }

        if (Ads.tencent(activity)) {
            tencent();
        }
    }

    public Interstitial release(){
        this.debug = false;
        return this;
    }

    public Interstitial debug(){
        this.debug = true;
        return this;
    }

    public Interstitial debug(boolean debug){
        this.debug = debug;
        return this;
    }

    public Interstitial auto(){
        autoShow = true;
        return this;
    }

    private void tencent(){
        String app_id  = Ads.tencent_app_id(activity);
        String posId  = Ads.tencent_interstitial_id(activity);
        if (debug || TextUtils.isEmpty(posId)) {
            return;
        }
        interstitialAD = new UnifiedInterstitialAD(activity, app_id, posId,this);
        interstitialAD.loadAD();
    }

    public void show() {
        if (interstitialAD != null && tencentLoaded) {
            interstitialAD.show();
        }
        hideSoftInput();
    }

    public void resume(){
    }

    public void destroy() {
        if (interstitialAD != null) {
            interstitialAD.destroy();
        }
    }

    private boolean tencentLoaded = false;
    @Override
    public void onADReceive() {
        tencentLoaded = true;
        Log.i(TAG,"广告加载成功");
        if (autoShow) {
            interstitialAD.show();
        }
    }

    @Override
    public void onNoAD(AdError error) {
        String msg = String.format(Locale.getDefault(), "Interstitial onNoAD, error code: %d, error msg: %s",
                error.getErrorCode(), error.getErrorMsg());
        Log.e(TAG,msg);
        tencentLoaded = false;
    }

    @Override
    public void onADOpened() {
        Log.i(TAG, "Interstitial onADOpened");
    }

    @Override
    public void onADExposure() {
        Log.i(TAG, "Interstitial onADExposure");
    }

    @Override
    public void onADClicked() {
        Log.i(TAG, "Interstitial onADClicked : " );
        if (clickable){
            return;
        }

        if (DBHelper.with(activity).clickable(DBHelper.AD_INTERSTITIAL)){
            clickable = true;
        }
    }

    @Override
    public void onADLeftApplication() {
        Log.i(TAG, "Interstitial onADLeftApplication");
    }

    @Override
    public void onADClosed() {
        Log.i(TAG, "Interstitial onADClosed");
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = activity.getCurrentFocus();
        if (focusView != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); //强制隐藏键盘
        }
    }
}
