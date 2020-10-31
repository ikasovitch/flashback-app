package com.flashbackapp;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewStoryActivity extends AppCompatActivity {
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String ARG_NAME = "username";
    FirebaseAuth firebaseAuth;
    WebView storyWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        findViewById(R.id.ReturnToMainFromViewStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                launchMainActivity(user);
            }
        });
//
//        firebaseAuth = FirebaseAuth.getInstance();
//        ShowStory();
        storyWebView = (WebView) findViewById(R.id.StoryView);
        storyWebView.loadUrl("https://docs.google.com/document/d/1Qspkh5AJ19JwyXmpFS44eTY5DROiijTUfdnZsklKZCM/edit");
        storyWebView.setWebViewClient(new client());
        WebSettings ws = storyWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        storyWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        storyWebView.clearCache(true);
        storyWebView.clearHistory();
//
//        storyWebView.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
//                req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        launchMainActivity(user);
    }
    public StorageReference GetReferenceToFirebaseFile() {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://shai-mfh-3586.appspot.com");
        return storageRef.child("story.txt");
    }

    public void launchMainActivity(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(ARG_NAME, user.getDisplayName());
            startActivity(intent);
        }
    }

    private class client extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl("javascript:(function() { " +
                    "document.getElementsByClassName(\"docs-ml-promotion-content\")[0].remove(); })()");
        }

    }
}