package com.ego.shadow.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ego.shadow.R;
import com.ego.shadow.ShadowAssetsActivity;

public class ShadowAboutActivity extends AppCompatActivity implements View.OnClickListener {

    public static void about(Fragment fragment) {
        about(fragment.getContext());
    }

    public static void about(Context context) {
        Intent intent = new Intent(context, ShadowAboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_about);


        ImageView iv_logo = findViewById(R.id.iv_logo);
        TextView tv_app_name = findViewById(R.id.tv_app_name);
        TextView tv_app_version = findViewById(R.id.tv_app_version);

        try {
            PackageManager packageManager = this.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            String app_name = this.getResources().getString(labelRes);
            tv_app_name.setText(app_name);
            tv_app_version.setText(String.format("V%s", packageInfo.versionName));

            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.getPackageName(), 0);
            ;
            Drawable drawable = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
            iv_logo.setImageDrawable(drawable);

        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_shadow_feedback).setOnClickListener(this);
        findViewById(R.id.tv_privacy_policy).setOnClickListener(this);
        findViewById(R.id.tv_user_agreement).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_back) {
            finish();

        } else if (view.getId() == R.id.tv_privacy_policy) {
            ShadowAssetsActivity.assets(this, 0);

        } else if (view.getId() == R.id.tv_user_agreement) {
            ShadowAssetsActivity.assets(this, 1);
        } else if (view.getId() == R.id.iv_shadow_feedback) {
            ShadowFeedbackActivity.feedback(this);
        }
    }

}
