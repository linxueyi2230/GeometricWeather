package com.ego.shadow;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * @author lxy
 * @time 2019/11/22 11:28
 */
public class NativeDialog {

    public static boolean onBackPressed(Activity activity) {
        String posId = Ads.tencent_native_id(activity);
        if (Ads.tencent(activity) && !TextUtils.isEmpty(posId)) {
            new NativeDialog(activity);
            return true;
        }
        return false;
    }

    public static boolean onBackPressed(Activity activity, boolean debug) {
        if (debug) {
            return false;
        }
        return onBackPressed(activity);
    }

    private Activity activity;

    public NativeDialog(Activity activity) {
        this.activity = activity;
        dialog();
    }

    private void dialog(){
        View view = LayoutInflater.from(activity).inflate(R.layout.shadow_native_dialog, null);
        final AlertDialog dialog = new AlertDialog.Builder(activity).setCancelable(false).setView(view).create();

        TextView shadow_title = view.findViewById(R.id.shadow_native_title);
        TextView shadow_exit = view.findViewById(R.id.shadow_exit);
        TextView shadow_cancel = view.findViewById(R.id.shadow_cancel);
        final FrameLayout native_container = view.findViewById(R.id.native_container);

        String name = Ads.getAppName(activity);
        shadow_title.setText(String.format("现在就要退出%s吗？", name));

        final NativeExpress nativeExpress = NativeExpress.of(activity, native_container);

        shadow_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                activity.finish();
            }
        });

        shadow_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nativeExpress.destroy();
            }
        });

        dialog.show();
    }
}
