package com.ego.shadow.ad;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.ego.shadow.Ads;
import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.util.AdError;

import java.util.Date;
import java.util.Locale;

/**
 * 激励广告
 */
public class Reward implements RewardVideoADListener {

    private static final String TAG = "RewardAd";
    private boolean debug;
    private Context context;
    private RewardVideoAD rewardVideoAD;

    public static Reward of(Activity activity){
        return new Reward(activity);
    }

    public static Reward of(Fragment fragment) {
        return new Reward(fragment.getContext());
    }

    public Reward(Context context) {
        this.context = context;
    }

    public void loadAd() {
        if (debug || !Ads.reward(context)) {
            return;
        }
        String app_id = Ads.tencent_app_id(context);
        String tencent_reward_id = Ads.tencent_reward_id(context);

        rewardVideoAD = new RewardVideoAD(context, app_id, tencent_reward_id, this);
        rewardVideoAD.loadAD();
    }

    public Reward release(){
        this.debug = false;
        return this;
    }

    public Reward debug(){
        this.debug = true;
        return this;
    }

    public Reward debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    /**
     * 广告加载成功，可在此回调后进行广告展示
     **/
    @Override
    public void onADLoad() {
        String msg = "load ad success ! expireTime = " + new Date(System.currentTimeMillis() +
                rewardVideoAD.getExpireTimestamp() - SystemClock.elapsedRealtime());
        Log.i(TAG,msg);

        if (!rewardVideoAD.hasShown()){
            long delta = 1000;//建议给广告过期时间加个buffer，单位ms，这里demo采用1000ms的buffer
            //广告展示检查3：展示广告前判断广告数据未过期
            if (SystemClock.elapsedRealtime() < (rewardVideoAD.getExpireTimestamp() - delta)) {
                rewardVideoAD.showAD();
            } else {
                Toast.makeText(context, "激励视频广告已过期，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "此条广告已经展示过，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 视频素材缓存成功，可在此回调后进行广告展示
     */
    @Override
    public void onVideoCached() {
        Log.i(TAG, "onVideoCached");
    }

    /**
     * 激励视频广告页面展示
     */
    @Override
    public void onADShow() {
        Log.i(TAG, "onADShow");
    }

    /**
     * 激励视频广告曝光
     */
    @Override
    public void onADExpose() {
        Log.i(TAG, "onADExpose");
    }

    /**
     * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
     */
    @Override
    public void onReward() {
        Log.i(TAG, "onReward");
        DBHelper.with(context).clickable(DBHelper.AD_REWARD);
    }

    /**
     * 激励视频广告被点击
     */
    @Override
    public void onADClick() {
        Log.i(TAG, "onADClick");
    }

    /**
     * 激励视频播放完毕
     */
    @Override
    public void onVideoComplete() {
        Log.i(TAG, "onVideoComplete");
    }

    /**
     * 激励视频广告被关闭
     */
    @Override
    public void onADClose() {
        Log.i(TAG, "onADClose");
    }

    /**
     * 广告流程出错
     */
    @Override
    public void onError(AdError adError) {
        String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
                adError.getErrorCode(), adError.getErrorMsg());
        Log.e(TAG,msg);
    }
}
