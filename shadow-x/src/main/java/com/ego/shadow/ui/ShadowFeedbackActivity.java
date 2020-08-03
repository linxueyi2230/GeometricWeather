package com.ego.shadow.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ego.shadow.R;

/**
 * @author e
 * @datetime 2020/7/20 12:18 AM
 */
public class ShadowFeedbackActivity extends AppCompatActivity {

    public static void feedback(Fragment fragment) {
        feedback(fragment.getContext());
    }

    public static void feedback(Context context) {
        Intent intent = new Intent(context, ShadowFeedbackActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shadow_activity_feedback);

        final EditText et_feedback = findViewById(R.id.et_feedback);
        findViewById(R.id.tv_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String feedback = et_feedback.getText().toString();
                if (TextUtils.isEmpty(feedback)) {
                    return;
                }

                Toast.makeText(ShadowFeedbackActivity.this, "感谢您的反馈，我们将会最快处理！", Toast.LENGTH_SHORT).show();
                et_feedback.setText("");
                finish();
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
