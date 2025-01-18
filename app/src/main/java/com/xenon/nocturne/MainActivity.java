package com.xenon.nocturne;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout loadingOverlay;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WebView, ProgressBar (spinner), and Overlay
        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);

        // Set dark background for the WebView before page loads
        webView.setBackgroundColor(getResources().getColor(android.R.color.black)); // Dark background

        // Ensure the WebView is hidden initially
        webView.setVisibility(View.GONE);

        // Configure WebView settings
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript

        // Use a WebViewClient to handle page load events
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                // Show the overlay and spinner at the start of page load
                loadingOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the overlay and spinner, then show the WebView
                loadingOverlay.setVisibility(View.GONE);  // Hide the overlay
                webView.setVisibility(View.VISIBLE);      // Show the WebView

                // Remove dark background after the page is loaded
                webView.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Transparent background
            }
        });

        // Use WebChromeClient to manage the loading spinner behavior (no progress needed)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // We don't need to track the progress since we're using an indeterminate spinner
                if (newProgress == 100) {
                    // When loading is finished, hide the spinner
                    progressBar.setVisibility(View.GONE);
                } else {
                    // Show the spinner while the page is loading
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        // Load the desired website
        webView.loadUrl("https://nocturne.brandons.place"); // Replace with your website URL
    }

    @Override
    public void onBackPressed() {
        // Handle back navigation within the WebView
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
