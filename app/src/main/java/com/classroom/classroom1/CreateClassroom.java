package com.classroom.classroom1;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateClassroom extends AppCompatActivity {
    public static final String TAG = "DEBUG";

    private EditText classroomName, description;
    private Button create;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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

    void checkFieldsForEmptyValues() {
        String s1 = classroomName.getText().toString();
        String s2 = description.getText().toString();

        if (s1.equals("") || s2.equals("")) {
            create.setEnabled(false);
            create.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        } else {
            create.setEnabled(true);
            create.getBackground().setColorFilter(null);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_classroom);
        setTitle("Create Classroom");
        classroomName = findViewById(R.id.editText3);
        description = findViewById(R.id.editText2);
        create = findViewById(R.id.button);
        create.setEnabled(false);
        create.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);

        // set listeners
        classroomName.addTextChangedListener(mTextWatcher);
        description.addTextChangedListener(mTextWatcher);

        // run once to disable if empty
        checkFieldsForEmptyValues();
    }

    public void createClassroom(View view) {
        addClassroomToFireStore();

        Intent intent = new Intent(CreateClassroom.this, HomeActivity.class);
        startActivity(intent);
    }


    private void addClassroomToFireStore() {
        // Create a new classroom
        Map<String, Object> classroom = new HashMap<>();
        Timestamp createTime = Timestamp.now();
        classroom.put("name", classroomName.getText().toString());
        classroom.put("description", description.getText().toString());
        classroom.put("status", true);
        classroom.put("create_time", createTime);

        DocumentReference teacherRef = db.collection("user")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        classroom.put("teacher", teacherRef);


        // Add a new document with a generated ID
        db.collection("classrooms")
                .add(classroom)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        attachClassroomToUser(documentReference.getId());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void attachClassroomToUser(final String classroomId) {
        final DocumentReference docRef = db.collection("user")
                .document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List classroomList = (ArrayList) document.get("classroom_list");
                        if (classroomList == null) {
                            classroomList = new ArrayList();
                        }
                        classroomList.add(classroomId);
                        docRef.update("classroom_list", classroomList);
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
