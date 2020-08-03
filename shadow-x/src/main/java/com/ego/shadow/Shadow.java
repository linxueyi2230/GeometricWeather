package com.ego.shadow;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.ego.shadow.ui.ShadowPersonalActivity;
import com.ego.shadow.ui.ShadowTestActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * @author lxy
 * @time 2018/8/27  10:40
 */
public class Shadow {

    public static String id;
    public static Class<?> clazz;
    public static String PKG = "com.movie58";
    public static String HOST_22 = "http://22.meimei43.com/back/get_init_data.php";
    public static String HOST_33 = "http://33.meimei43.com/back/get_init_data.php";

    public static String HOST = HOST_22;

    /**------正常的闪屏页-----**/
    public static int splashImage = R.drawable.shadow_splash;
    public static int splashLayout = -1;

    /**------打开开关后的闪屏页-----**/
    public static int shadowImage = R.drawable.ic_lottery_splash;
    public static int shadowLayout = -1;

    public static int downloadImage = R.drawable.ic_download;

    public static boolean checkInstalled = true;

    public static boolean DEBUG = false;
    public static String SHADOW_LOCATION = null;
    public static Application application;
    public static String SHADOW_COMPANY = null;

    public static void init(Application application,String id, Class<?> clazz,boolean debug){
        Shadow.id = id;
        Shadow.clazz = clazz;
        Shadow.DEBUG = debug;
        Shadow.application = application;
        JPushInterface.setDebugMode(debug);
        JPushInterface.init(application);
//        if (debug) {
//            Shadow.clazz = ShadowTestActivity.class;
//        }
        if (isMainProcess(application)){
            Ads.initialize(application);
        }
    }

    private static boolean isMainProcess(Application application) {
        return application.getPackageName().equals(process(application));
    }

    private static String process(Application context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    public static void image(int drawable){
        Shadow.splashImage = drawable;
        Shadow.splashLayout = -1;
    }

    public static void layout(int layout){
        Shadow.splashImage = -1;
        Shadow.splashLayout = layout;
    }

    /**------设置正常审核时显示的闪屏图-----**/
    public static void setNomalDrawable(int drawable){
        image(drawable);
    }

    /**------设置正常审核时显示的闪屏布局-----**/
    public static void setNomalLayout(int layout){
        layout(layout);
    }

    /**------设置打开开关后彩票的闪屏图-----**/
    public static void setLotteryDrawable(int drawable){
        Shadow.shadowImage = drawable;
        Shadow.shadowLayout = -1;
    }

    /**------设置打开开关后彩票的闪屏布局-----**/
    public static void setLotteryLayout(int layout){
        Shadow.shadowImage = -1;
        Shadow.shadowLayout = layout;
    }

    public static void setDownloadImage(int drawable){
        Shadow.downloadImage = drawable;
    }

    public static SplashListener listener;
    public static void setListener(SplashListener listener){
        Shadow.listener = listener;
    }

    public static boolean isInstalled(Context context) {
        return isInstalled(context, PKG);
    }

    public static boolean isInstalled(Context context, String pkg) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }

        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    public static void launcher(Activity activity) {
        Intent intent = activity.getPackageManager().
                getLaunchIntentForPackage(PKG);
        activity.startActivity(intent);
    }

    public static void install(Activity activity, File apk) {
        if (apk == null || !apk.exists()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Intent install = new Intent(Intent.ACTION_VIEW);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(activity, activity.getPackageName()+".FileProvider", apk);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            activity.startActivity(install);
            activity.finish();
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.setDataAndType(Uri.fromFile(new File(apk.getAbsolutePath())), "application/vnd.android.package-archive");
            activity.startActivity(install);
            activity.finish();
        }
    }

    public static void log(String log){
        Log.i("Shadow",log);
    }


    public static void dialog(final Activity activity, final Listener listener) {
        View view = LayoutInflater.from(activity).inflate(R.layout.shadow_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(false).setView(view).create();

        TextView shadow_title = view.findViewById(R.id.shadow_title);
        TextView shadow_disagree = view.findViewById(R.id.shadow_disagree);
        TextView shadow_agree = view.findViewById(R.id.shadow_agree);
        TextView shadow_privacy_policy = view.findViewById(R.id.shadow_privacy_policy);
        TextView shadow_user_agreement = view.findViewById(R.id.shadow_user_agreement);
        TextView shadow_location = view.findViewById(R.id.shadow_location);

        if (!TextUtils.isEmpty(Shadow.SHADOW_LOCATION)){
            shadow_location.setText("· 获取位置信息，为了精准推荐" + Shadow.SHADOW_LOCATION);
        }

        String name = Ads.getAppName(activity);
        shadow_title.setText(String.format("尊敬的用户，感谢您使用%s！在使用前请您务必审阅以下信息", name));

        SharedPreferences sp = activity.getSharedPreferences("Ads",
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.shadow_agree){
                    editor.putBoolean("agree",true);
                    editor.apply();
                    dialog.dismiss();
                    if (listener !=null){
                        listener.agree();
                    }

                }else if (id == R.id.shadow_disagree){
                    editor.putBoolean("agree", false);
                    editor.apply();
                    dialog.dismiss();
                    if (listener != null) {
                        listener.disagree();
                    }

                } else if (id == R.id.shadow_privacy_policy) {
                    ShadowAssetsActivity.assets(activity, 0);
                } else if (id == R.id.shadow_user_agreement) {
                    ShadowAssetsActivity.assets(activity, 1);
                }
            }
        };

        shadow_agree.setOnClickListener(click);
        shadow_disagree.setOnClickListener(click);
        shadow_privacy_policy.setOnClickListener(click);
        shadow_user_agreement.setOnClickListener(click);

        dialog.show();
    }

    public static interface Listener{
        void agree();
        void disagree();
    }

    public static String assets(Context context, String name) {
        try {
            InputStream is = context.getApplicationContext().getAssets().open(name);
            return string(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String string(InputStream is) throws IOException{
        StringBuffer sb = new StringBuffer();
        InputStreamReader reader = new InputStreamReader(is, "UTF-8");
        char[] buf = new char[1024];
        int count;
        while ((count = reader.read(buf)) != -1) {
            sb.append(buf,0,count);
        }
        is.close();
        //log(sb.toString());
        return sb.toString();
    }

    public static void fab(final Activity activity) {
        View view = activity.findViewById(R.id.shadow_personal);
        fab(activity, view);
    }

    public static void fab(final Activity activity,View view){
        if (!Ads.tencent(activity)){
            view.setVisibility(View.INVISIBLE);
            return;
        }

        view.setClickable(true);
        view.setVisibility(View.VISIBLE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toPersonal(activity);
            }
        });
    }

    public static void toPersonal(Activity activity){
        Intent intent = new Intent(activity,ShadowPersonalActivity.class);
        activity.startActivity(intent);
    }
}
