package com.xenon.nocturne;

import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
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

        // WebView, Overlay und ProgressBar initialisieren
        webView = findViewById(R.id.webView);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);

        // Dunklen Hintergrund für die WebView festlegen, bevor die Seite geladen wird
        webView.setBackgroundColor(getResources().getColor(android.R.color.black));

        // Die WebView zunächst unsichtbar machen
        webView.setVisibility(View.GONE);

        // WebView-Einstellungen konfigurieren
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // JavaScript aktivieren

        // WebViewClient setzen, um URLs in der WebView zu laden
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Stelle sicher, dass alle Links innerhalb der WebView geladen werden
                view.loadUrl(url);
                return true;  // Verhindert, dass der Link im externen Browser geöffnet wird
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                // Zeige Overlay und Spinner zu Beginn des Seitenladevorgangs
                loadingOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Verstecke Overlay und Spinner, dann WebView anzeigen
                loadingOverlay.setVisibility(View.GONE);  // Overlay ausblenden
                webView.setVisibility(View.VISIBLE);      // WebView anzeigen

                // Entferne den dunklen Hintergrund, nachdem die Seite geladen ist
                webView.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Transparenter Hintergrund
            }
        });

        // WebChromeClient verwenden, um das Ladeverhalten des Spinners zu verwalten
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Den Fortschritt nicht verfolgen, da wir einen unbestimmten Spinner verwenden
                if (newProgress == 100) {
                    // Wenn das Laden abgeschlossen ist, Spinner ausblenden
                    progressBar.setVisibility(View.GONE);
                } else {
                    // Spinner anzeigen, während die Seite lädt
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });

        // Lade die gewünschte Website
        webView.loadUrl("https://nocturne.brandons.place"); // Ersetze dies mit deiner URL

        // WebView-Einstellungen konfigurieren
        WebSettings webSetting = webView.getSettings();
        webSettings.setDomStorageEnabled(true);  // DOM-Speicherung aktivieren
        webSettings.setJavaScriptEnabled(true);  // JavaScript aktivieren

        // Benutzer-Agent auf Desktop-Modus setzen (User-Agent-String für Desktop-Browser)
        webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // Skalierung deaktivieren, um zu verhindern, dass der Benutzer zoomt
        webSettings.setSupportZoom(false);  // Zoom nicht erlauben
        webSettings.setBuiltInZoomControls(false); // Zoom-Steuerung ausschalten
        webSettings.setDisplayZoomControls(false); // Zoom-Steuerung nicht anzeigen

        // Aktivieren der "Desktop-Ansicht" mit der richtigen Viewport-Größe
        webSettings.setUseWideViewPort(true);   // Benutzt die breite Ansicht der Webseite
        webSettings.setLoadWithOverviewMode(true); // Webseite wird in Übersichtmodus geladen

        // **SCALING DOWN THE CONTENT**: Adjusting scale factor (e.g., 50% of original scale)
        webView.setInitialScale(50);  // Scale down to 50% of the original size

        // CookieManager aktivieren
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie("url", "cookie=value");  // Beispiel, falls du Cookies manuell setzen musst
    }

    @Override
    public void onBackPressed() {
        // Navigiere zurück innerhalb der WebView, wenn möglich
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
