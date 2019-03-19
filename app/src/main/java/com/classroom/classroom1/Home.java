package com.classroom.classroom1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Home extends AppCompatActivity {
    public static final String NAME_PROPERTY = "name";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String USER_COLLECTION = "user";
    public static final String USER_TYPE_TEACHER = "teacher";
    public static final String CLASSROOM_ID = "classroomId";
    public static final String CLASSROOM_NAME = "classroomName";
    private static final String TAG = "HomeActivity";
    private LinearLayout parentLayout;
    private SwipeRefreshLayout mySwipeRefreshLayout;
    private FloatingActionButton fabAddButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int cardIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle("Home");

        parentLayout = findViewById(R.id.classroomCardHolder);
        setAddClassroomFloatingButton();
        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        loadClassrooms();
        Log.w(TAG, "card loading ended in OnCreate Method");

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        startActivity(getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        overridePendingTransition(0, 0);
                        finish();
                        overridePendingTransition(0, 0);
                    }
                }
        );
    }

    private void loadClassrooms() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection(USER_COLLECTION).document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Object classList = document.get("classroom_list");
                        if (classList == null || ((ArrayList) classList).isEmpty()) {
                            getLayoutInflater().inflate(R.layout.classroom_list_empty, parentLayout, true);
                        } else {
                            loadClassroomsByList((ArrayList) classList);
                        }
                    }
                }
            }
        });

    }

    private void loadClassroomsByList(ArrayList<String> classroomList) {
        for (int i = classroomList.size() - 1; i >= 0; i--) {
            DocumentReference docRef = db.collection("classrooms").document(classroomList.get(i));
            docRef.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                addClassroomCardToView(task.getResult());
                            } else {
                                Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }

    private void addClassroomCardToView(DocumentSnapshot document) {
        CardView card;

        if (cardIndex % 2 != 0) {
            card = (CardView) getLayoutInflater().inflate(R.layout.classroom_card_odd, parentLayout, false);
        } else {
            card = (CardView) getLayoutInflater().inflate(R.layout.classroom_card_even, parentLayout, false);
        }

        LinearLayout innerLinearLayout = (LinearLayout) card.getChildAt(0);
        TextView nameText = (TextView) innerLinearLayout.getChildAt(0);
        TextView descriptionText = (TextView) innerLinearLayout.getChildAt(1);

        // Set Name
        String name = document.get(NAME_PROPERTY).toString();
        nameText.setText(name);

        // Set Description
        String description = document.get(DESCRIPTION_PROPERTY).toString();
        descriptionText.setText(description);

        Log.w(TAG, "classroom id:" + document.getId() + " name:" + name);
        setOnClickListenerForCard(card, document.getId(), name);

        parentLayout.addView(card);
        cardIndex++;
    }


    private void setOnClickListenerForCard(CardView card, final String classroomId, final String classroomName) {
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, ClassroomDetail.class);
                intent.putExtra(CLASSROOM_ID, classroomId);
                intent.putExtra(CLASSROOM_NAME, classroomName);
                startActivity(intent);
            }
        });
    }

    private void setAddClassroomFloatingButton() {
        // TODO use common DAO
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection(USER_COLLECTION).document(user.getEmail());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userType = document.get("type").toString();

                        if (USER_TYPE_TEACHER.equals(userType)) {
                            showAddClassroomFab();
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    void showAddClassroomFab() {
        fabAddButton = findViewById(R.id.floatingActionButton);
        fabAddButton.setVisibility(View.VISIBLE);
        fabAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, CreateClassroom.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                signOut();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
