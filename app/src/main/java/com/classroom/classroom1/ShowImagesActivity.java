package com.classroom.classroom1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ShowImagesActivity extends AppCompatActivity {

    private String postId;
    private StorageReference mStorageRef;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Attachment Preview");
        setContentView(R.layout.activity_show_images);

        imageView = findViewById(R.id.attachment_img_view);

        // Gent Post Id - needed to retreive attachment
        Intent callingPostIntent = getIntent();
        postId = callingPostIntent.getStringExtra("postId");

        // Initialize storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

        GlideApp.with(this /* context */)
                .load(mStorageRef.child("images/" + postId))
                .into(imageView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
