package com.ego.shadow;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class Downloader extends AsyncTask<String, Integer, String> {

    private File mFile;
    private int mProgress = 0;

    abstract protected void onProgress(int progress);

    abstract protected void onFinish(int progress,File apk);

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
            mFile = new File(fileName);
            if (!mFile.exists()) {
                if (!mFile.getParentFile().exists()) {
                    mFile.getParentFile().mkdirs();
                }
                mFile.createNewFile();
            }
            fos = new FileOutputStream(mFile);
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
        this.mProgress = progress[0];
        this.onProgress(mProgress);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mProgress == 100) {
            this.onFinish(mProgress, mFile);
        }
    }

    public File apk() {
        return mFile;
    }

    public void install(Activity activity) {
        Shadow.install(activity, mFile);
    }
}
