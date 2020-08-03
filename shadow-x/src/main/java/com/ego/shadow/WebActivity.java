package com.ego.shadow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author lxy
 * @time 2018/8/27  11:03
 */
public class WebActivity extends AppCompatActivity {

    private WebView webView;
    private String url;
    private BannerAd ad = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.shadow_activity_web);
        webView = findViewById(R.id.wv_web);

        ad = BannerAd.banner(this).loadAD();

        webView.setFitsSystemWindows(true);
        WebSettings webSettings = webView.getSettings();

        // 支持javascript
        webSettings.setJavaScriptEnabled(true);

        // 支持使用localStorage(H5页面的支持)
        webSettings.setDomStorageEnabled(true);

        // 支持数据库
        webSettings.setDatabaseEnabled(true);

        // 支持缓存
        webSettings.setAppCacheEnabled(true);
        String appCaceDir = this.getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        webSettings.setAppCachePath(appCaceDir);

        // 设置可以支持缩放
        webSettings.setUseWideViewPort(true);

        // 扩大比例的缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        // 隐藏缩放按钮
        webSettings.setDisplayZoomControls(false);

        // 自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);

        // 隐藏滚动条
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        // 进度显示及隐藏
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                //downloadByBrowser(s);
            }
        });

        // 处理网页内的连接（自身打开）
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.i("WebActivity",url);
                if (url.endsWith(".apk")) {
                    Bundle bundle = new Bundle();
                    bundle.putString("Url", url);
                    Intent intent = new Intent(WebActivity.this, DownloadActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);

                    return true;
                }

                // 如下方案可在非微信内部WebView的H5页面中调出微信支付
                if (url.startsWith("weixin://wap/pay?") | url.startsWith("mqqapi") | url.startsWith("alipay")) {

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    WebBackForwardList backForwardList = webView.copyBackForwardList();
                    int currentIndex = backForwardList.getCurrentIndex();
                    int i = 1;
                    while (true) {
                        WebHistoryItem historyItem = backForwardList.getItemAtIndex(currentIndex - i);
                        if (historyItem != null) {
                            String backPageUrl = historyItem.getUrl();
                            //url拿到可以进行操作

                            if (!isyou(backPageUrl, WebActivity.this.url)) {
                                webView.goBack();
                            } else {
                                webView.goBack();
                                break;
                            }

                        }
                        i++;
                    }
                    startActivity(intent);


                    return true;
                } else if (parseScheme(url)) {
                    try {
                        Intent intent;
                        intent = Intent.parseUri(url,
                                Intent.URI_INTENT_SCHEME);
                        intent.addCategory("android.intent.category.BROWSABLE");
                        intent.setComponent(null);
                        WebBackForwardList backForwardList = webView.copyBackForwardList();
                        int currentIndex = backForwardList.getCurrentIndex();
                        int i = 1;
                        while (true) {
                            WebHistoryItem historyItem = backForwardList.getItemAtIndex(currentIndex - i);
                            if (historyItem != null) {
                                String backPageUrl = historyItem.getUrl();
                                //url拿到可以进行操作
                                if (!isyou(backPageUrl, WebActivity.this.url)) {
                                    webView.goBack();
                                } else {
                                    webView.goBack();
                                    break;
                                }
                            }
                            i++;
                        }
                        startActivity(intent);

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }

        });

        // 使用返回键的方式防止网页重定向
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                }
                return false;
            }
        });

        url = getIntent().getStringExtra("Url");
//        if (TextUtils.isEmpty(url)) {
//            url = "http://m.9744.net/";
//        }
        webView.loadUrl(url);

        Interstitial.of(this).auto();

    }

    public boolean parseScheme(String url) {

        if (url.contains("platformapi/startapp")) {
            return true;
        } else if ((Build.VERSION.SDK_INT > 23)
                && (url.contains("platformapi") && url.contains("startapp"))) {
            return true;
        } else {
            return false;
        }
    }

    private void downloadByBrowser(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isyou(String s1, String s2) {
        String str = s1;
        if (str.indexOf(s2) != -1) {

            return true;

        } else {

            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ad.resume();
    }

    @Override
    public void onPause() {
        ad.pause();
        super.onPause();
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        ad.destroy();
        super.onDestroy();
    }
}
