package com.ego.shadow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

/**
 * @author lxy
 * @time 2019/11/14 12:39
 */
public class ShadowAssetsActivity extends AppCompatActivity {

    public static void policy(Activity activity){
        assets(activity,0);
    }

    public static void agreement(Activity activity){
        assets(activity,1);
    }

    public static void assets(Activity activity, int type) {
        Intent intent = new Intent(activity, ShadowAssetsActivity.class);
        intent.putExtra("type", type);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_assets);

        findViewById(R.id.shadow_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String title = null;
        String content = null;

        int type = getIntent().getIntExtra("type", 0);
        if (type == 0) {
            title = "隐私政策";
            content = Shadow.assets(this, "shadow_privacy_policy.txt");

        } else {
            title = "用户协议";
            content = Shadow.assets(this, "shadow_user_agreement.txt");
        }

        String name = Ads.getAppName(this);
        if (!TextUtils.isEmpty(content)) {

            content = content.replace("新影视大全", name);

            if (!TextUtils.isEmpty(Shadow.SHADOW_COMPANY) && content.contains("本公司")) {
                String company = String.format("《%s》", Shadow.SHADOW_COMPANY);
                content = content.replaceAll("本公司", company);
            }
        }

        TextView shadow_title = findViewById(R.id.shadow_title);
        TextView shadow_content = findViewById(R.id.shadow_content);

        shadow_title.setText(title);
        shadow_content.setText(content);
    }
}
