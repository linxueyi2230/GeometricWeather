package com.ego.shadow;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class DownloadDialog extends AlertDialog {

    private Activity activity;
    private ProgressBar pb_progress;
    private TextView tv_total;
    private TextView tv_progress;

    public DownloadDialog(@NonNull Activity activity) {
        super(activity);
        this.activity = activity;
        this.setCancelable(false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.shadow_dialog_download);
        pb_progress = findViewById(R.id.pb_progress);
        tv_total = findViewById(R.id.tv_total);
        tv_progress = findViewById(R.id.tv_progress);
    }

    public DownloadDialog download(String url) {
        this.show();
        new D().execute(url);
        return this;
    }

    class D extends Downloader {

        StringBuilder sb = new StringBuilder();
        @Override
        protected void onProgress(int progress) {
            pb_progress.setProgress(progress);
            sb.setLength(0);
            sb.append(progress).append("%");
            tv_progress.setText(sb.toString());
            tv_total.setText(String.format("%s/100", progress));
        }

        @Override
        protected void onFinish(int progress, File apk) {
            this.install(activity);
            DownloadDialog.this.dismiss();
        }
    }

}
