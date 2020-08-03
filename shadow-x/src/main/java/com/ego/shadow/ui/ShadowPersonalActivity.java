package com.ego.shadow.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ego.shadow.Interstitial;
import com.ego.shadow.NativeExpress;
import com.ego.shadow.R;
import com.ego.shadow.ad.Reward;
import com.ego.shadow.db.DBHelper;

/**
 * 个人中心
 * https://www.jianshu.com/p/4bb31a128e73
 */
public class ShadowPersonalActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String REWARD = "reward";
    private NativeExpress nativeExpress;

    private TextView tv_balance;
    private TextView tv_progress;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(REWARD,intent.getAction())){
                update();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_personal);

        tv_balance = findViewById(R.id.tv_balance);
        tv_progress = findViewById(R.id.tv_progress);

        findViewById(R.id.btn_detail).setOnClickListener(this);
        findViewById(R.id.btn_extract).setOnClickListener(this);
        findViewById(R.id.btn_reward).setOnClickListener(this);
        findViewById(R.id.iv_extract_record).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(REWARD);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        nativeExpress = NativeExpress.of(this);

        Interstitial.of(this).auto();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.btn_detail) {
            toActivity(ShadowRewardDetailActivity.class);
        } else if (id == R.id.btn_extract) {
            extract();
            //toActivity(ShadowExtractActivity.class);
        } else if (id == R.id.btn_reward) {
            //RandomUtils.test(this);
            dialog();
        } else if (id == R.id.iv_extract_record) {
            toActivity(ShadowExtractRecordActivity.class);
        }
    }


    private void extract(){
        String balance = DBHelper.with(this).balance();
        double amount = Double.parseDouble(balance);
        if (amount >=100){
            toActivity(ShadowExtractActivity.class);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage("收益满100元才可以提现哦，快去点击观看视频赚钱收益吧").setTitle("提示").setPositiveButton("我知道了", null).create().show();
    }


    private void toActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage("观看视频广告并点击获取收益").setTitle("提示").setNegativeButton("狠心放弃",null).setPositiveButton("马上观看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reward();
            }
        }).create().show();
    }

    private void reward(){
        Reward.of(ShadowPersonalActivity.this).loadAd();
    }

    private void update(){
        tv_balance.setText(DBHelper.with(this).balance());


    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        int progress = DBHelper.with(this).rewardProgress();
        if (progress > 0){
            StringBuilder sb = new StringBuilder();
            sb.append("今日还可以观看 <font size=10 color=#0F4C81>").append(progress).append("</font> 次");

            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.N){
                tv_progress.setText(Html.fromHtml(sb.toString(),Html.FROM_HTML_MODE_LEGACY));
            }else {
                tv_progress.setText(Html.fromHtml(sb.toString()));
            }

        }else {
            tv_progress.setText("今日观看视频次数已经用完，明天再来哦");
        }
    }

    @Override
    protected void onDestroy() {
        nativeExpress.destroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }
}
