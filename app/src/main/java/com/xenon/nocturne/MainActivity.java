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
    private Handler qrScanHandler = new Handler(Looper.getMainLooper());
    private boolean scanning = false;
    private boolean shouldScan = true; // Controls whether scanning should continue
    private long appStartTime; // To store the time when the app starts

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);

        // Store the start time of the app
        appStartTime = System.currentTimeMillis();

        // WebView Settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                loadingOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loadingOverlay.postDelayed(() -> loadingOverlay.setVisibility(View.GONE), 500);
                progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 500);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress == 100 ? View.GONE : View.VISIBLE);
            }
        });

        // Load the URL into the WebView
        webView.loadUrl("https://nocturne.brandons.place");

        // Start QR Code Scanning
        startQRScanner();
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
                // Check if 50 seconds have elapsed or scanning is no longer needed
                long elapsedTime = System.currentTimeMillis() - appStartTime;
                if (elapsedTime <= 50_000 && shouldScan) {
                    if (!scanning) {
                        scanning = true;

                        // Capture WebView as Bitmap
                        webView.post(() -> {
                            Bitmap bitmap = captureWebView();
                            if (bitmap != null) {
                                scanQRCode(bitmap);
                            }
                            scanning = false;
                        });
                    }

                    // Continue scanning
                    qrScanHandler.postDelayed(this, 2000); // Re-scan every 2 seconds
                } else {
                    System.out.println("QR scanning has stopped.");
                    shouldScan = false; // Ensure scanning stops
                }
            }
        }, 2000); // Initial delay of 2 seconds
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
            QRCodeReader reader = new QRCodeReader();
            Result result = reader.decode(binaryBitmap);
            handleQRCodeResult(result.getText());
        } catch (Exception e) {
            // No QR code found, continue scanning
        }
    }

    private void handleQRCodeResult(String qrCode) {
        runOnUiThread(() -> {
            // Log the QR Code detected
            System.out.println("QR Code Detected: " + qrCode);

            // Open the link in the system's default browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrCode));
                startActivity(browserIntent);

                // Stop scanning after opening the link
                shouldScan = false;
                System.out.println("Scanning stopped as the link was opened.");
            } catch (Exception e) {
                System.out.println("Invalid URL: " + qrCode);
            }
        });
    }
}
