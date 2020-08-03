package com.ego.shadow.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ego.shadow.BannerAd;
import com.ego.shadow.R;
import com.ego.shadow.db.DBHelper;

/**
 * 提现
 */
public class ShadowExtractActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_extract;
    private EditText et_account;
    private BannerAd bannerAd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_extract);

        findViewById(R.id.iv_extract_record).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.btn_extract).setOnClickListener(this);
        RelativeLayout rl_container = findViewById(R.id.rl_container);
        bannerAd = BannerAd.banner(this,rl_container).loadAD();

        TextView tv_balance = findViewById(R.id.tv_balance);

        tv_balance.setText(DBHelper.with(this).balance());
        et_account = findViewById(R.id.et_account);
        btn_extract = findViewById(R.id.btn_extract);
        et_account.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    btn_extract.setEnabled(false);
                    return;
                }

                btn_extract.setEnabled(s.length() > 6);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        }else if (id == R.id.iv_extract_record) {
            toActivity(ShadowExtractRecordActivity.class);
        }else if (id == R.id.btn_extract){
            extract();
        }
    }

    private void extract(){
        final String account = et_account.getText().toString();
        if (TextUtils.isEmpty(account)){
            return;
        }

        final String balance = DBHelper.with(this).balance();
        StringBuilder msg = new StringBuilder();
        msg.append("您将提现 ").append(balance).append(" 元").append("至您的支付宝账号 ").append(account).append(" 请确认您的账号无误");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage(msg).setTitle("提示").setNegativeButton("我再想想",null).setPositiveButton("提现", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper.with(ShadowExtractActivity.this).extract(account);
                Intent intent = new Intent(ShadowExtractActivity.this, ShadowExtractRecordActivity.class);
                intent.putExtra("ad",true);
                startActivity(intent);
                finish();

            }
        }).create().show();

    }

    private void toActivity(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        bannerAd.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bannerAd.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerAd.destroy();
    }
}
