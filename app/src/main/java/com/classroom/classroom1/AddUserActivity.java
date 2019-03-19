package com.classroom.classroom1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {

    private static final String TAG = "Add User Activity";
    String classroomId;
    String inputtedUserEmail;
    private EditText userEmailEditText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        setTitle("Add User");

        userEmailEditText = findViewById(R.id.new_user_email_edit_txt);

        Intent intent = getIntent();
        classroomId = intent.getStringExtra("classroomId");
    }


    public void addUser(View view) {
        addUserInClassroom();
    }


    private void addUserInClassroom() {
        inputtedUserEmail = userEmailEditText.getText().toString();
        final DocumentReference userRef = db.collection("user")
                .document(inputtedUserEmail);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Intent rintent = new Intent(AddUserActivity.this, ClassroomDetail.class);

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Used already exists");
                        List classroomList = (ArrayList) document.get("classroom_list");
                        if (classroomList == null) {
                            Log.d(TAG, "classroom_list field not found, new creating..");
                            classroomList = new ArrayList();
                        }

                        // Prevent duplicate addition
                        if (!classroomList.contains(classroomId)) {
                            classroomList.add(classroomId);
                            Log.i(TAG, classroomId + " added to classroom_list");
                            userRef.update("classroom_list", classroomList);
                            Log.i(TAG, "user already Present in class");
                            Log.i(TAG, "classroom_list updated for " + inputtedUserEmail);
                            attachUserToClassroom(userRef);
                        }
                        setResult(RESULT_OK, rintent);

                    } else {
                        setResult(RESULT_CANCELED, rintent);
                        Log.e(TAG, "User does not exist");
                    }
                }

                finish();
            }
        });
    }

    private void attachUserToClassroom(final DocumentReference userRef) {
        final DocumentReference docRef = db.collection("classrooms")
                .document(classroomId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.i(TAG, "found classroom with " + classroomId);

                        List students = (ArrayList) document.get("students");
                        if (students == null) {
                            students = new ArrayList();
                        }

                        // Prevent duplicate addition
                        if (!students.contains(userRef)) {
                            students.add(userRef);
                            docRef.update("students", students);
                        }
                    } else {
                        Log.e(TAG, "classroom with " + classroomId + " not found");
                    }
                }
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
}
