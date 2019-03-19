package com.classroom.classroom1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Add Post Activity";
    private static final int PICK_IMAGE_REQUEST = 234;
    private EditText postInput;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String classroomId;
    //Buttons
    private ImageButton buttonChoose;
    private Button buttonUpload;
    //ImageView
    private ImageView imageView;
    //a Uri object to store file path
    private Uri fileUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private ArrayList<Uri> filePathList;
    private String newlyCreatedPostId;
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // check Fields For Empty Values
            checkFieldsForEmptyValues();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        setTitle("Add Post");
        postInput = findViewById(R.id.postEditText);
        //getting views from layout
        buttonChoose = findViewById(R.id.choose_btn);
        buttonUpload = findViewById(R.id.addPostBtn);
        imageView = findViewById(R.id.attachment_img_view);

        //attaching listener
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        postInput.addTextChangedListener(mTextWatcher);


        Intent intent = getIntent();
        // todo get classroom id and save on clock to post
        classroomId = intent.getStringExtra("classroomId");
        Log.e("ADD POST", classroomId);
        // Call once to disable in begining
        checkFieldsForEmptyValues();
    }

    private void addPostToFireStore() {
        // Create a new classroom
        Map<String, Object> post = new HashMap<>();
        Timestamp createTime = Timestamp.now();
        post.put("content", postInput.getText().toString());
        post.put("status", true);
        post.put("create_time", createTime);
        post.put("update_time", createTime);

        DocumentReference userRef = db.collection("user")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        post.put("user", userRef);
        post.put("author_name", userName);
        post.put("classroom_id", classroomId);
        //temp fix
        if (fileUri != null) {
            post.put("has_attachment", true);
        }

        // Add a new document with a generated ID
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        newlyCreatedPostId = documentReference.getId();
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        uploadFile();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
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

    // --------------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        //if the clicked button is choose
        if (view == buttonChoose) {
            showFileChooser();
        }
        //if the clicked button is upload
        else if (view == buttonUpload) {

            if (postInput.getText().toString() != null) {
                addPostToFireStore();
            }
        }
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //this method will upload the file
    private void uploadFile() {
        //if there is a file to upload
        if (fileUri != null) {
            //displaying a progress dialog while upload is going on
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

//            String extension = fileUri.toString().substring(fileUri.toString().lastIndexOf("."));
//            StorageReference riversRef = storageReference.child("images/pic.jpg");
            StorageReference riversRef = storageReference.child("images/" + newlyCreatedPostId); // + extension);


            // Compress file
            Bitmap bmp;
            byte[] data;
            try {
                bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
                data = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            riversRef.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                            Intent rintent = new Intent(AddPostActivity.this, ClassroomDetail.class);
                            setResult(RESULT_OK, rintent);
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        } else {
            Intent rintent = new Intent(AddPostActivity.this, ClassroomDetail.class);
            setResult(RESULT_OK, rintent);
            finish();
        }
    }

    void checkFieldsForEmptyValues() {

        String s1 = postInput.getText().toString();

        if (s1.equals("")) {
            buttonUpload.setEnabled(false);
            buttonUpload.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else {
            buttonUpload.setEnabled(true);
            buttonUpload.getBackground().setColorFilter(null);
        }
    }
}
