package com.xenon.nocturne;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout loadingOverlay;
    private ProgressBar progressBar;
    private final Handler qrScanHandler = new Handler(Looper.getMainLooper());
    private boolean scanning = false;
    private boolean shouldScan = true;
    private long appStartTime;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeUI();
        configureWebView();

        appStartTime = System.currentTimeMillis();
        webView.loadUrl("https://nocturne.brandons.place");

        startQRScanner();
    }

    private void initializeUI() {
        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configureWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showLoading(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoadingWithDelay();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void hideLoadingWithDelay() {
        loadingOverlay.postDelayed(() -> showLoading(false), 500);
        progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 500);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void startQRScanner() {
        qrScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (shouldContinueScanning()) {
                    if (!scanning) {
                        scanning = true;
                        webView.post(() -> {
                            Bitmap bitmap = captureWebView();
                            if (bitmap != null) {
                                scanQRCode(bitmap);
                            }
                            scanning = false;
                        });
                    }
                    qrScanHandler.postDelayed(this, 2000);
                } else {
                    shouldScan = false;
                    System.out.println("QR scanning has stopped.");
                }
            }
        }, 2000);
    }

    private boolean shouldContinueScanning() {
        return System.currentTimeMillis() - appStartTime <= 50_000 && shouldScan;
    }

    private Bitmap captureWebView() {
        try {
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void scanQRCode(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new QRCodeReader().decode(binaryBitmap);
            handleQRCodeResult(result.getText());
        } catch (Exception ignored) {
        }
    }

    private void handleQRCodeResult(String qrCode) {
        runOnUiThread(() -> {
            System.out.println("QR Code Detected: " + qrCode);
            openLinkInBrowser(qrCode);
        });
    }

    private void openLinkInBrowser(String qrCode) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(qrCode)));
            shouldScan = false;
            System.out.println("Scanning stopped as the link was opened.");
        } catch (Exception e) {
            System.out.println("Invalid URL: " + qrCode);
        }
    }
}
