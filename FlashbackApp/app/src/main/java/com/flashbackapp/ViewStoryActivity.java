package com.flashbackapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ViewStoryActivity extends AppCompatActivity {
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String ARG_NAME = "username";
    FirebaseAuth firebaseAuth;

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

        firebaseAuth = FirebaseAuth.getInstance();
        ShowStory();
    }

    public StorageReference GetReferenceToFirebaseFile() {
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://shai-mfh-3586.appspot.com");
        return storageRef.child("story.txt");
    }

    public void PrintText(String story) {
        TextView shai_story = findViewById(R.id.StoryText);
        shai_story.setText(story);
        shai_story.setVisibility(View.VISIBLE);
        System.out.print(story);
    }

    public void ShowStory() {
        StorageReference pathReference = GetReferenceToFirebaseFile();
        final long ONE_MEGABYTE = 1024 * 1024;
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                String s = new String(bytes);
                PrintText(s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println(exception.getMessage());
            }
        });
    }

    public void launchMainActivity(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(ARG_NAME, user.getDisplayName());
            startActivity(intent);
        }
    }

}