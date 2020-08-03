package com.ego.shadow;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

/**
 * @author lxy
 * @time 2019/7/5 14:37
 */
public class BannerAd implements UnifiedBannerADListener {

    private static final int LOCATION_DEFAULT = 0;
    private static final int LOCATION_TOP = 1;
    private static final int LOCATION_BOTTOM = 2;

    private UnifiedBannerView bv = null;
    private Activity activity;
    private ViewGroup container;
    private int mLocation = LOCATION_DEFAULT;
    private boolean debug;
    private boolean clickable;

    public static BannerAd banner(Activity activity) {
        RelativeLayout layout = activity.findViewById(R.id.rl_container);
        return new BannerAd(activity, layout);
    }

    public static BannerAd banner(Activity activity,ViewGroup container){
        return new BannerAd(activity,container);
    }

    public static BannerAd banner(Fragment fragment, ViewGroup container){
        return new BannerAd(fragment,container);
    }

    public BannerAd(Fragment fragment, ViewGroup container) {
        this(fragment.getActivity(), container);
    }

    public BannerAd(Activity activity,ViewGroup container) {
        this.activity = activity;
        this.container = container;
    }

    public BannerAd debug(){
        this.debug = true;
        return this;
    }

    public BannerAd release(){
        this.debug = false;
        return this;
    }

    public BannerAd debug(boolean debug){
        this.debug = debug;
        return this;
    }

    public BannerAd loadAD() {
        if (Ads.tencent(activity)) {
            this.tencent();
        }
        return this;
    }

    public BannerAd top(){
        this.mLocation = LOCATION_TOP;
        return this;
    }

    public BannerAd bottom(){
        this.mLocation = LOCATION_BOTTOM;
        return this;
    }

    private void tencent(){
        if (debug || container == null) {
            return;
        }

        String app_id  = Ads.tencent_app_id(activity);
        String banner_id  = Ads.tencent_banner_id(activity);
        this.bv = new UnifiedBannerView(activity,app_id, banner_id,this);
        bv.setRefresh(30);

        // tencent banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        int x = screenSize.x;
        int y = Math.round(screenSize.x / 6.4F);

        if (mLocation == LOCATION_DEFAULT) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(x, y);
            container.addView(bv, params);
        } else if (mLocation == LOCATION_TOP) {
            if (container instanceof RelativeLayout) {
                RelativeLayout v = (RelativeLayout) container;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x, y);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                v.addView(bv, params);

            } else if (container instanceof FrameLayout) {
                FrameLayout v = (FrameLayout) container;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(x, y);
                params.gravity = Gravity.TOP;
                v.addView(bv, params);
            }

        } else if (mLocation == LOCATION_BOTTOM) {
            if (container instanceof RelativeLayout) {
                RelativeLayout v = (RelativeLayout) container;
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x, y);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                v.addView(bv, params);

            } else if (container instanceof FrameLayout) {
                FrameLayout v = (FrameLayout) container;
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(x, y);
                params.gravity = Gravity.BOTTOM;
                v.addView(bv, params);
            }
        }

        bv.loadAD();
    }



    public void resume() {
    }

    public void pause() {
    }

    public void destroy() {
        if (bv != null) {
            bv.destroy();
        }
    }

    @Override
    public void onNoAD(AdError error) {
        Log.i("Ads", String.format("LoadBannerADFail, eCode=%d, errorMsg=%s", error.getErrorCode(), error.getErrorMsg()));
    }

    @Override
    public void onADReceive() {
        Log.i("Ads", "Banner onADReceive");
    }

    @Override
    public void onADExposure() {
        Log.i("Ads", "Banner onADExposure");
    }

    @Override
    public void onADClosed() {
        Log.i("Ads", "Banner onADClosed");
    }

    @Override
    public void onADClicked() {
        Log.i("Ads", "Banner onADClicked");

        if (clickable){
            return;
        }

        if (DBHelper.with(activity).clickable(DBHelper.AD_BANNER)){
            clickable = true;
        }
    }

    @Override
    public void onADLeftApplication() {
        Log.i("Ads", "Banner onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay() {
        Log.i("Ads", "Banner onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay() {
        Log.i("Ads", "Banner onADCloseOverlay");
    }
}
