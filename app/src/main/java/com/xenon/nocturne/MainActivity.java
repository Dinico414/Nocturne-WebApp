package com.xenon.nocturne;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout loadingOverlay;
    private ProgressBar progressBar;
    private final Handler qrScanHandler = new Handler(Looper.getMainLooper());
    private boolean scanning = false;
    private boolean shouldScan = true;
    private boolean doubleBackToExitPressedOnce = false;

    private final Map<Integer, String> buttonLinks = new HashMap<>();
    private Button button1, button2, button3, button4;
    private GestureDetector gestureDetector;
    private boolean isPressed = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeUI();
        configureWebView();

        loadSavedLinks();

        webView.loadUrl("https://nocturne.brandons.place");

        startQRScanner();

        setupButtonListeners(button1, R.id.button1);
        setupButtonListeners(button2, R.id.button2);
        setupButtonListeners(button3, R.id.button3);
        setupButtonListeners(button4, R.id.button4);

        volumeNobButton = findViewById(R.id.volumeNobButton);
        volumeNobLayout = findViewById(R.id.volumeNobLayout);
        setupVolumeNobButton();
    }

    private void initializeUI() {
        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
    }

    @SuppressLint("SetJavaScriptEnabled")
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
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    private void startQRScanner() {
        qrScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
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
            }
        }, 2000);
    }

    private boolean shouldContinueScanning() {
        return shouldScan;
    }


    /**
     * @noinspection CallToPrintStackTrace
     */
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
            if ((qrCode.startsWith("http://") || qrCode.startsWith("https://")) && qrCode.contains("phone-auth?session")) {

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(this, R.color.black));
                builder.setShowTitle(true);

                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.launchUrl(this, Uri.parse(qrCode));
                shouldScan = false;
                System.out.println("Scanning stopped as the link was opened in the custom tab.");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    shouldScan = true;
                    System.out.println("Scanning resumed after 5-minute pause.");
                    startQRScanner();
                }, 5000);
            } else {
                System.out.println("Invalid URL or does not contain 'phone-auth?session': " + qrCode);
            }
        } catch (Exception e) {
            System.out.println("Error opening URL: " + qrCode);
        }
    }

    private void openLinkInWebView(String url) {
        try {
            webView.loadUrl(url);
        } catch (Exception e) {
            System.out.println("Invalid URL: " + url);
        }
    }

    private void setupButtonListeners(Button button, int buttonId) {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedLinks", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        button.setOnClickListener(v -> {
            String link = buttonLinks.get(buttonId);
            if (link != null) {
                openLinkInWebView(link);
            } else {
                Toast.makeText(this, "No link saved to this button.", Toast.LENGTH_SHORT).show();
            }
        });

        button.setOnLongClickListener(v -> {
            String currentUrl = webView.getUrl();
            if (currentUrl != null && (currentUrl.contains("playlist") || currentUrl.contains("collection"))) {
                buttonLinks.put(buttonId, currentUrl);

                editor.putString("button_" + buttonId, currentUrl);
                editor.apply();

                Toast.makeText(this, "Link saved to Button " + (buttonId - R.id.button1 + 1), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You can only save playlists.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private void loadSavedLinks() {
        SharedPreferences sharedPreferences = getSharedPreferences("SavedLinks", MODE_PRIVATE);

        for (int i = 1; i <= 4; i++) {
            String savedLink = sharedPreferences.getString("button_" + (R.id.button1 + i - 1), null);
            if (savedLink != null) {
                buttonLinks.put(R.id.button1 + i - 1, savedLink);
            }
        }
    }

    private ConstraintLayout volumeNobLayout;
    private ImageButton volumeNobButton;
    private boolean isAnimating = false;
    private int initialMargin;
    private Handler handler = new Handler();
    private Runnable resetRunnable;

    @SuppressLint("ClickableViewAccessibility")
    private void setupVolumeNobButton() {
        volumeNobButton = findViewById(R.id.volumeNobButton);
        volumeNobLayout = findViewById(R.id.volumeNobLayout);

        int screenWidth = getScreenWidth();
        initialMargin = screenWidth - dpToPx(20);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) volumeNobLayout.getLayoutParams();
        layoutParams.leftMargin = initialMargin;
        volumeNobLayout.setLayoutParams(layoutParams);

        resetRunnable = () -> {
            if (isAnimating) return;
            animateLayout(initialMargin);
        };

        volumeNobButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isAnimating) {
                        isAnimating = true;
                        animateLayout(initialMargin - dpToPx(120));
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    handler.removeCallbacks(resetRunnable);
                    handler.postDelayed(resetRunnable, 3000);
                    return true;

                default:
                    return false;
            }
        });
    }

    private void animateLayout(int targetMargin) {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) volumeNobLayout.getLayoutParams();
        int currentMargin = layoutParams.leftMargin;

        ValueAnimator animator = ValueAnimator.ofInt(currentMargin, targetMargin);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            layoutParams.leftMargin = animatedValue;
            volumeNobLayout.setLayoutParams(layoutParams);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        animator.start();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }
}
