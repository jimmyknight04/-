package kr.family.story;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MainActivity extends Activity {
    private static final String SITE_URL = "https://our-family-story.kimyangha.chatgpt.site";
    private static final String SALT = "family-story-v1|";
    private static final String HUSBAND_HASH = "71ce5851a5ba17d90265bb29bdc0bb94f8328e0c9da8186bbcb4e850842bf455";
    private static final String WIFE_HASH = "b52799781bda1a7c3fabb39498bc15d6b94b46f6ce3425d5649988723b009d26";
    private static final int FILE_CHOOSER = 1001;

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private SharedPreferences prefs;
    private int failedAttempts;
    private long lockedUntil;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("family_login", MODE_PRIVATE);
        if (prefs.getBoolean("logged_in", false)) showFamilyApp(state); else showLogin();
    }

    private void showLogin() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(30), dp(36), dp(30), dp(36));
        root.setBackgroundColor(Color.rgb(255, 247, 249));

        TextView heart = new TextView(this);
        heart.setText("♥");
        heart.setTextSize(52);
        heart.setTextColor(Color.rgb(217, 95, 123));
        heart.setGravity(Gravity.CENTER);

        TextView title = new TextView(this);
        title.setText("우리 가족 이야기");
        title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(69, 47, 53));
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("우리 둘만의 소중한 공간");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.rgb(130, 102, 110));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(28));

        EditText id = input("아이디");
        EditText password = input("비밀번호");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Button login = new Button(this);
        login.setText("가족 공간 들어가기");
        login.setTextSize(17);
        login.setTextColor(Color.WHITE);
        login.setAllCaps(false);
        login.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        login.setBackground(roundRect(Color.rgb(217, 95, 123), 16));
        LinearLayout.LayoutParams buttonParams = fullWidth(dp(54));
        buttonParams.topMargin = dp(18);

        root.addView(heart, fullWidth(dp(72)));
        root.addView(title, fullWidth(dp(48)));
        root.addView(subtitle, fullWidth(dp(64)));
        root.addView(id, fieldParams());
        root.addView(password, fieldParams());
        root.addView(login, buttonParams);
        setContentView(root);

        login.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now < lockedUntil) {
                Toast.makeText(this, "잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            String user = id.getText().toString().trim();
            String pass = password.getText().toString();
            if (isValid(user, pass)) {
                failedAttempts = 0;
                prefs.edit().putBoolean("logged_in", true).putString("user", user).apply();
                showFamilyApp(null);
            } else {
                failedAttempts++;
                if (failedAttempts >= 5) {
                    lockedUntil = now + 30_000;
                    failedAttempts = 0;
                    Toast.makeText(this, "30초 후 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                } else Toast.makeText(this, "아이디 또는 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid(String user, String pass) {
        String expected;
        if ("jimmyknight".equals(user)) expected = HUSBAND_HASH;
        else if ("flysysy".equals(user)) expected = WIFE_HASH;
        else return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sha256(SALT + user + ":" + pass).getBytes(StandardCharsets.UTF_8));
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) { return ""; }
    }

    private void showFamilyApp(Bundle state) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(16), 0, dp(8), 0);
        bar.setBackgroundColor(Color.rgb(69, 47, 53));

        TextView name = new TextView(this);
        String user = prefs.getString("user", "");
        name.setText(("flysysy".equals(user) ? "아내" : "남편") + "님 ♥");
        name.setTextColor(Color.WHITE);
        name.setTextSize(15);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        Button logout = new Button(this);
        logout.setText("로그아웃");
        logout.setTextColor(Color.WHITE);
        logout.setTextSize(12);
        logout.setAllCaps(false);
        logout.setBackgroundColor(Color.TRANSPARENT);
        logout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            if (webView != null) webView.destroy();
            webView = null;
            showLogin();
        });
        bar.addView(name, new LinearLayout.LayoutParams(0, dp(48), 1));
        bar.addView(logout, new LinearLayout.LayoutParams(dp(88), dp(48)));

        webView = new WebView(this);
        root.addView(bar);
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try { startActivityForResult(params.createIntent(), FILE_CHOOSER); }
                catch (Exception e) { fileCallback = null; return false; }
                return true;
            }
        });
        if (state == null) webView.loadUrl(SITE_URL); else webView.restoreState(state);
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setPadding(dp(18), 0, dp(18), 0);
        input.setBackground(roundRect(Color.WHITE, 14));
        return input;
    }
    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams p = fullWidth(dp(56));
        p.bottomMargin = dp(12);
        return p;
    }
    private LinearLayout.LayoutParams fullWidth(int height) { return new LinearLayout.LayoutParams(-1, height); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private GradientDrawable roundRect(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), Color.rgb(239, 215, 222));
        return d;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
        }
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null) webView.saveState(out);
        super.onSaveInstanceState(out);
    }
}
