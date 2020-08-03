package com.ego.shadow;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.Window;

import java.io.File;

/**
 * @author lxy
 * @time 2018/8/27  11:04
 */
public class UpgradeActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

//        View layout = new View(this);
//        layout.setBackgroundResource(R.drawable.ic_lottery_splash);
//        setContentView(layout);
        setContentView(R.layout.shadow_activity_upgrade);
        String url = getIntent().getStringExtra("Url");
        download(url);
    }

    private void download(String url) {
        final String apk = "shadow.apk";
        Log.e("UpgradeActivity", url);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                long id = prefs.getLong("downloadUpdateId", 0);
                DownloadManager manager = (DownloadManager) UpgradeActivity.this.getSystemService(Activity.DOWNLOAD_SERVICE);
                UpgradeActivity.this.unregisterReceiver(this);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String provider = UpgradeActivity.this.getPackageName() + ".FileProvider";
                    Uri uri = FileProvider.getUriForFile(UpgradeActivity.this, provider, new File(file, apk));
                    install(uri);

                } else {
                    Uri uri = manager.getUriForDownloadedFile(id);
                    install(uri);
                }
            }
        };

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(UpgradeActivity.this);
        DownloadManager manager = (DownloadManager) UpgradeActivity.this.getSystemService(Activity.DOWNLOAD_SERVICE);
        url = Uri.parse(url).toString();
        DownloadManager.Request download = new DownloadManager.Request(Uri.parse(url));
        download.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        download.setAllowedOverRoaming(false);
        download.setVisibleInDownloadsUi(true);
        download.setTitle("正在下载..");
        download.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apk);
        final long id = manager.enqueue(download);
        prefs.edit().putLong("downloadUpdateId", id).commit();
        UpgradeActivity.this.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void install(Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }
}
