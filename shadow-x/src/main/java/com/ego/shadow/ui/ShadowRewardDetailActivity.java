package com.ego.shadow.ui;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.ego.shadow.R;
import com.ego.shadow.adapter.Adapter;
import com.ego.shadow.adapter.RewardAdapter;
import com.ego.shadow.db.DBHelper;
import com.ego.shadow.entity.RewardRow;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.List;

/**
 * 收益明细记录
 */
public class ShadowRewardDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_empty;
    private RecyclerView rv_list;
    private RewardAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_reward_detail);

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_reward_rule).setOnClickListener(this);

        tv_empty = findViewById(R.id.tv_empty);
        rv_list = findViewById(R.id.rv_list);

        List<RewardRow> rows = DBHelper.with(this).getRewardRows();
        if (rows.isEmpty()){
            tv_empty.setVisibility(View.VISIBLE);
            rv_list.setVisibility(View.INVISIBLE);
            return;
        }

        tv_empty.setVisibility(View.INVISIBLE);
        rv_list.setVisibility(View.VISIBLE);

        adapter = new RewardAdapter(this,rows);
        adapter.getNativeExpressRecyclerView().FIRST_AD_POSITION = 5;
        adapter.getNativeExpressRecyclerView().debug();
        adapter.setListener(new Adapter.Listener<RewardRow>() {
            @Override
            public void click(View view, RewardRow rewardRow, int position) {

            }

            @Override
            public RewardRow toAdData(int position, NativeExpressADView adView) {
                RewardRow data = new RewardRow();
                data.adView = adView;
                data.position = position;
                data.isAd = true;
                return data;
            }
        });

        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_reward_rule) {
            dialog();
        }
    }

    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setMessage(R.string.shadow_role).setTitle("收益规则").setPositiveButton("知道了", null).create().show();
    }
}
