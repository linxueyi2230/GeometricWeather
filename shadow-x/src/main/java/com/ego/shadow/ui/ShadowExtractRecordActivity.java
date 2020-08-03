package com.ego.shadow.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ego.shadow.Interstitial;
import com.ego.shadow.R;
import com.ego.shadow.adapter.Adapter;
import com.ego.shadow.adapter.ExtractAdapter;
import com.ego.shadow.db.DBHelper;
import com.ego.shadow.entity.Row;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.List;

/**
 * 提现记录
 */
public class ShadowExtractRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_empty;
    private RecyclerView rv_list;
    private ExtractAdapter adapter;
    private Interstitial interstitial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_extract_record);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_extract_question).setOnClickListener(this);

        tv_empty = findViewById(R.id.tv_empty);
        rv_list = findViewById(R.id.rv_list);

        List<Row> rows = DBHelper.with(this).getExtractRecord();
        if (rows.isEmpty()){
            tv_empty.setVisibility(View.VISIBLE);
            rv_list.setVisibility(View.INVISIBLE);
            return;
        }

        tv_empty.setVisibility(View.INVISIBLE);
        rv_list.setVisibility(View.VISIBLE);

        adapter = new ExtractAdapter(this,rows);
        adapter.getNativeExpressRecyclerView().FIRST_AD_POSITION = 2;
        adapter.getNativeExpressRecyclerView().debug();
        adapter.setListener(new Adapter.Listener<Row>() {
            @Override
            public void click(View view, Row row, int position) {

            }

            @Override
            public Row toAdData(int position, NativeExpressADView adView) {
                Row data = new Row();
                data.adView = adView;
                data.position = position;
                data.isAd = true;
                return data;
            }
        });

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(adapter);

        if (getIntent().getBooleanExtra("ad",false)){
            interstitial = Interstitial.of(this).auto();
            Toast.makeText(this,"您的提现申请已提交，请耐心等待审核付款！",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        }else if (id == R.id.iv_extract_question) {
            dialog();
        }
    }

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage(R.string.shadow_extract).setTitle("提现说明").setNegativeButton("知道了",null).setPositiveButton("联系客服", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                call();
            }
        }).create().show();
    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     */
    public void call() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:15018402350");
        intent.setData(data);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (interstitial !=null){
            interstitial.destroy();
        }
    }
}
