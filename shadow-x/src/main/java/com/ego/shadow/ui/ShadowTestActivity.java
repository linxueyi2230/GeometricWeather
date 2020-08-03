package com.ego.shadow.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.ego.shadow.R;
import com.ego.shadow.Shadow;
import com.ego.shadow.utils.RandomUtils;

public class ShadowTestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_test);

        findViewById(R.id.btn_reward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Reward.of(ShadowTestActivity.this).loadAd();
                RandomUtils.reward();
            }
        });

        Shadow.fab(this);

        //DBHelper.with(this).progress();

    }
}
