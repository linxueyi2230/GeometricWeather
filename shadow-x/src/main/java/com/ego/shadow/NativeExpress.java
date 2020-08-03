package com.ego.shadow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;

import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.pi.AdData;
import com.qq.e.comm.util.AdError;

import java.util.List;

/**
 * RecyclerView
 * @author lxy
 * @time 2019/11/22 10:53
 */
public class NativeExpress implements NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "NativeExpressAd";
    private Context context;
    private ViewGroup container;
    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;
    private boolean debug;
    private boolean clickable;


    public static NativeExpress of(Activity activity,boolean debug) {
        FrameLayout container = activity.findViewById(R.id.native_container);
        return new NativeExpress(activity, container,debug);
    }

    public static NativeExpress of(Activity activity) {
        FrameLayout container = activity.findViewById(R.id.native_container);
        return new NativeExpress(activity, container,false);
    }

    public static NativeExpress of(Fragment fragment, ViewGroup container, boolean debug) {
        return new NativeExpress(fragment.getContext(),container,debug);
    }

    public static NativeExpress of(Fragment fragment, ViewGroup container) {
        return new NativeExpress(fragment.getContext(),container,false);
    }

    public static NativeExpress of(Context context,ViewGroup container) {
        return new NativeExpress(context,container,false);
    }

    public NativeExpress(Context context, ViewGroup container,boolean debug) {
        this.context = context;
        this.container = container;
        this.debug = debug;
        this.loadAd();
    }

    public NativeExpress release(){
        this.debug = false;
        return this;
    }

    public NativeExpress debug(){
        this.debug = true;
        return this;
    }

    public NativeExpress debug(boolean debug){
        this.debug = debug;
        return this;
    }

    public void loadAd() {
        if (!Ads.tencent(context)) {
            return;
        }

        //1109582701
        //1020694177175214
        String app_id = Ads.tencent_app_id(context);
        String posId = Ads.tencent_native_id(context);
        if (debug || TextUtils.isEmpty(posId)) {
            return;
        }

        try {

            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            ADSize adSize = new ADSize(ADSize.FULL_WIDTH,ADSize.AUTO_HEIGHT);
            nativeExpressAD = new NativeExpressAD(context,adSize,app_id, posId,  this); // 这里的Context必须为Activity
            nativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            nativeExpressAD.setMaxVideoDuration(25);
            nativeExpressAD.loadAD(1);
        } catch (NumberFormatException e) {
            Log.w(TAG, "ad size invalid.");
        }
    }

    public void destroy() {
        // 使用完了每一个NativeExpressADView之后都要释放掉资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.i(TAG, String.format("NativeExpress onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.i(TAG, "onADLoaded: " + adList.size());
        // 释放前一个展示的NativeExpressADView的资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }

        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }

        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        nativeExpressADView = adList.get(0);
        Log.i(TAG, "onADLoaded, video info: ");
        if (nativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            nativeExpressADView.setMediaListener(mediaListener);
        }
        // 广告可见才会产生曝光，否则将无法产生收益。
        container.addView(nativeExpressADView);
        nativeExpressADView.render();
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail");
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess");
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked");

        if (clickable){
            return;
        }

        if (DBHelper.with(context).clickable(DBHelper.AD_NATIVE)){
            clickable = true;
        }
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed");
        // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
        if (container != null && container.getChildCount() > 0) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay");
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADCloseOverlay");
    }

    private NativeExpressMediaListener mediaListener = new NativeExpressMediaListener() {
        @Override
        public void onVideoInit(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoInit: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoLoading(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoLoading");
        }

        @Override
        public void onVideoReady(NativeExpressADView nativeExpressADView, long l) {
            Log.i(TAG, "onVideoReady");
        }

        @Override
        public void onVideoStart(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoStart: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoPause(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoPause: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoComplete(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoComplete: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoError(NativeExpressADView nativeExpressADView, AdError adError) {
            Log.i(TAG, "onVideoError");
        }

        @Override
        public void onVideoPageOpen(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoPageOpen");
        }

        @Override
        public void onVideoPageClose(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoPageClose");
        }
    };

    /**
     * 获取播放器实例
     *
     * 仅当视频回调{@link NativeExpressMediaListener#onVideoInit(NativeExpressADView)}调用后才会有返回值
     *
     * @param videoPlayer
     * @return
     */
    private String getVideoInfo(AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("{state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition()).append("}");
            return videoBuilder.toString();
        }
        return null;
    }
}
