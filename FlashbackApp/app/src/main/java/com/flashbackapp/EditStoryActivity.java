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
import com.google.firebase.storage.UploadTask;

public class EditStoryActivity extends AppCompatActivity {
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final String ARG_NAME = "username";
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_story);

        findViewById(R.id.ReturnToMainFromEditStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                launchMainActivity(user);
            }
        });

        findViewById(R.id.SaveNewStory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStory(view);
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
        TextView storyInputContentView = findViewById(R.id.StoryInputContent);
        storyInputContentView.setText(story);
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

    public void saveStory(View view) {
        TextView storyInputContentView = findViewById(R.id.StoryInputContent);
        CharSequence newContent = storyInputContentView.getText();
        System.out.println(newContent);
        byte[] content_bute_array = newContent.toString().getBytes();
        StorageReference fileReference = GetReferenceToFirebaseFile();
        UploadTask uploadTask = fileReference.putBytes(content_bute_array);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("New shay story failed to write to firebase");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("New shay story written successfully to firebase");
            }
        });
    }

}
