package com.ego.shadow;

import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadActivity extends AppCompatActivity {

    private TextView tv_loading;
    private String url;
    private ProgressBar pb_progress;
    private int progress = 0;
    private File file;
    private BannerAd ad = null;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.shadow_activity_download);

        RelativeLayout rl_download = findViewById(R.id.rl_download);

        try {
            if (Shadow.downloadImage != -1) {
                rl_download.setBackgroundResource(Shadow.downloadImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        pb_progress = findViewById(R.id.pb_progress);
        tv_loading = findViewById(R.id.tv_loading);

        ClipDrawable d = new ClipDrawable(new ColorDrawable(Color.YELLOW), Gravity.LEFT, ClipDrawable.HORIZONTAL);
        pb_progress.setProgressDrawable(d);
        pb_progress.setBackgroundColor(Color.GRAY);
        pb_progress.setMax(100);
        pb_progress.setScrollBarSize(20);
        pb_progress.setProgress(1);

        url = getIntent().getStringExtra("Url");
        new DownloadAPK(pb_progress, tv_loading).execute(url);

        ad = BannerAd.banner(this,rl_download).bottom().loadAD();
        Interstitial.of(this).auto();
    }

    class DownloadAPK extends AsyncTask<String, Integer, String> {

        TextView tv_loading;
        ProgressBar pb_progress;

        public DownloadAPK(ProgressBar pb, TextView tv) {
            this.pb_progress = pb;
            this.tv_loading = tv;
        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection conn;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;

            try {
                url = new URL(strings[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                int fileLength = conn.getContentLength();
                bis = new BufferedInputStream(conn.getInputStream());
                String fileName = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/lottery.apk";
                } else {
                    fileName = Environment.getExternalStorageDirectory().getPath() + "/magkare/lottery.apk";
                }
                file = new File(fileName);
                if (!file.exists()) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                byte data[] = new byte[4 * 1024];
                long total = 0;
                int count;
                while ((count = bis.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(data, 0, count);
                }
                fos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            DownloadActivity.this.pb_progress.setProgress(progress[0]);
            tv_loading.setText("正在为您更新，请稍等" + progress[0] + "% ...");
            DownloadActivity.this.progress = progress[0];
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progress == 100) {
                tv_loading.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Shadow.install(DownloadActivity.this, file);
                    }
                }, 500);
            }
            //打开安装apk文件操作
            Toast.makeText(getApplication(), "下载完成", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (progress == 100) {
            Shadow.install(DownloadActivity.this, file);
        }
        ad.resume();
    }


    @Override
    public void onPause() {
        ad.pause();
        super.onPause();
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        ad.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }
}
