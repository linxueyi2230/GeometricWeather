package com.ego.shadow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ego.shadow.db.DBHelper;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.List;

/**
 * RecyclerView
 * @author lxy
 * @time 2019/11/22 10:53
 */
public abstract class NativeExpressRecyclerView implements NativeExpressAD.NativeExpressADListener {

    private static final String TAG = "NativeAdRecycler";
    public static final int TYPE_AD = 1024;
    private Context context;
    private NativeExpressAD mNativeExpressAD;
    private int mAdWidth = ADSize.FULL_WIDTH;
    private int mAdHeight = ADSize.AUTO_HEIGHT;

    private int mLastAdPosition = 0;
    public int AD_COUNT = 5;    // 加载广告的条数，取值范围为[1, 10]
    public int FIRST_AD_POSITION = 1; // 第一条广告的位置
    public int ITEMS_PER_AD = 5;     // 每间隔10个条目插入一条广告
    private List<NativeExpressADView> mAdViewList;
    private HashMap<NativeExpressADView, Integer> mAdViewPositionMap = new HashMap<>();

    private RecyclerView.Adapter mAdapter;
    private List<? extends AdData> mData;
    private boolean debug;

    public NativeExpressRecyclerView(Activity activity) {
        this.context = activity;
    }

    public NativeExpressRecyclerView(Fragment fragment) {
        this.context = fragment.getContext();
    }

    public NativeExpressRecyclerView(Context context) {
        this.context = context;
    }

    public NativeExpressRecyclerView count(int count) {
        this.AD_COUNT = count;
        return this;
    }

    public NativeExpressRecyclerView width(int width) {
        this.mAdWidth = width;
        return this;
    }

    public NativeExpressRecyclerView height(int height) {
        this.mAdHeight = height;
        return this;
    }

    public NativeExpressRecyclerView release(){
        this.debug = false;
        return this;
    }

    public NativeExpressRecyclerView debug(){
        this.debug = true;
        return this;
    }

    public NativeExpressRecyclerView debug(boolean debug){
        this.debug = debug;
        return this;
    }

    public NativeExpressRecyclerView loadAd(RecyclerView.Adapter adapter, List<? extends AdData> data) {
        this.mAdapter = adapter;
        this.mData = data;
        this.loadAd();
        return this;
    }

    private void loadAd() {
        if (debug || !Ads.tencent(context)) {
            return;
        }

        if (mAdViewList !=null){//refresh

            mLastAdPosition = 0;
            mAdViewPositionMap.clear();
            this.append();
            return;
        }

        String app_id = Ads.tencent_app_id(context);
        String posId = Ads.tencent_native_id(context);
        if (TextUtils.isEmpty(posId)) {
            return;
        }

        try {

            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            ADSize adSize = new ADSize(mAdWidth, mAdHeight);
            mNativeExpressAD = new NativeExpressAD(context,adSize,app_id, posId,  this); // 这里的Context必须为Activity
            mNativeExpressAD.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.WIFI) // 设置什么网络环境下可以自动播放视频
                    .setAutoPlayMuted(true) // 设置自动播放视频时，是否静音
                    .build()); // setVideoOption是可选的，开发者可根据需要选择是否配置
            mNativeExpressAD.setMaxVideoDuration(25);
            mNativeExpressAD.loadAD(AD_COUNT);
        } catch (NumberFormatException e) {
            Log.w(TAG, "ad size invalid.");
        }
    }

    public void destroy() {
        // 使用完了每一个NativeExpressADView之后都要释放掉资源。
        if (mAdViewList != null) {
            for (NativeExpressADView view : mAdViewList) {
                view.destroy();
            }
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.i(TAG, String.format("NativeExpress onNoAD, error code: %d, error msg: %s", adError.getErrorCode(), adError.getErrorMsg()));
        mAdViewList = null;
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {

        Log.i(TAG, "onADLoaded: " + adList.size());
        mAdViewList = adList;
        this.append();
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
        DBHelper.with(context).clickable(DBHelper.AD_NATIVE);
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed");

        if (mAdapter != null) {
            int position = mAdViewPositionMap.get(adView);
            mData.remove(position);
            mAdapter.notifyItemRemoved(position); // position为adView在当前列表中的位置
            mAdapter.notifyItemRangeChanged(0, mData.size() - 1);
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
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
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
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoPause(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoPause: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
        }

        @Override
        public void onVideoComplete(NativeExpressADView nativeExpressADView) {
            Log.i(TAG, "onVideoComplete: "
                    + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
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
    private String getVideoInfo(com.qq.e.comm.pi.AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("{state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition()).append("}");
            return videoBuilder.toString();
        }
        return null;
    }

    abstract public void addAd(int position, NativeExpressADView adView);

    public void append() {

        if (mAdapter == null) {
            return;
        }
        if (isEmpty(mAdViewList)) {
            Log.i(TAG, "原生广告列表为空");
            mAdapter.notifyDataSetChanged();
            return;
        }

        Log.i(TAG, "mLastAdPosition is " + mLastAdPosition);
        Log.i(TAG, "mAdViewList size is " + mAdViewList.size());

        if (mLastAdPosition >= mAdViewList.size()) {
            Log.i(TAG, "广告已显示完毕");
            mAdapter.notifyDataSetChanged();
            return;
        }

        for (int i = mLastAdPosition; i < mAdViewList.size(); i++) {
            int position = FIRST_AD_POSITION + ITEMS_PER_AD * i;
            if (position < mData.size()) {
                NativeExpressADView adView = mAdViewList.get(i);
                if (adView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                    adView.setMediaListener(mediaListener);
                }

                mAdViewPositionMap.put(adView, position); // 把每个广告在列表中位置记录下来
                this.addAd(position, adView);

                mLastAdPosition = i;//将最后的一条广告位置保持
            } else {
                mLastAdPosition = i;
                break;
            }

        }
        mAdapter.notifyDataSetChanged();
    }


    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public int getDataCount() {

        if (isEmpty(mData)) {
            return 0;
        }

        int count = 0;
        for (AdData data : mData) {

            if (!data.isAd) {
                count++;
            }
        }
        return count;
    }

    public void onBindAdHolder(RecyclerView.ViewHolder holder, AdData ad, int position) {
        this.onBindAdHolder(holder, ad.adView, position);
    }

    public void onBindAdHolder(RecyclerView.ViewHolder holder, NativeExpressADView adView, int position) {

        ViewGroup container = holder.itemView.findViewById(R.id.native_container);
        if (container == null) {
            return;
        }

        mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
        if (container.getChildCount() > 0
                && container.getChildAt(0) == adView) {
            return;
        }

        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }

        container.addView(adView);
        adView.render(); // 调用render方法后sdk才会开始展示广告
    }

    public RecyclerView.ViewHolder holder(ViewGroup viewGroup) {
        return holder(viewGroup.getContext());
    }

    public RecyclerView.ViewHolder holder(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.shadow_native_express, null);
        return new AdHolder(view);
    }

    public static class AdHolder extends RecyclerView.ViewHolder {

        public ViewGroup container;

        public AdHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.native_container);
        }
    }
}
