package com.classroom.classroom1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class StudentsFragment extends Fragment {

    public static final String CLASSROOM_PROPERTY_TEACHER = "teacher";
    private static final String TAG = "Student Fragment";
    private static final String CLASSROOM_PROPERTY_STUDENT = "students";
    private static final String USER_PROPERTY_NAME = "name";
    String classroomId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LinearLayout parentLayout;
    private int userUpdateBtnVisibility = View.INVISIBLE;

    public StudentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_students, container, false);
        parentLayout = view.findViewById(R.id.member_container);

        classroomId = getArguments().getString(HomeActivity.CLASSROOM_ID);

        addHeaderCardToView(true);
        loadMembers(getArguments().getString(HomeActivity.CLASSROOM_ID));

        return view;
    }

    private void loadMembers(final String classroomId) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("classrooms")
                .document(classroomId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
                                Log.d(TAG, "Found classroom documents successfully" + document.getData());

                                DocumentReference teacher = (DocumentReference) document.get(CLASSROOM_PROPERTY_TEACHER);
                                Object students = document.get(CLASSROOM_PROPERTY_STUDENT);
                                if (teacher == null) {
                                    //TODO show string or snackbar
                                    Log.e(TAG, "No teacher found for class " + classroomId);
                                } else {
                                    if (teacher.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                                        userUpdateBtnVisibility = View.VISIBLE;
                                    }
                                    addTeacherCardToViewByList(teacher, (ArrayList) students);
                                }
                            } else {
                                Log.w(TAG, "No Such document");
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }


    private void addTeacherCardToViewByList(DocumentReference teacher, final ArrayList<DocumentReference> students) {
        teacher.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            addMemberCardToView(task.getResult(), true);
                        } else {
                            Log.e(TAG, "Error getting documents.", task.getException());
                        }

                        addHeaderCardToView(false);
                        if (students == null || students.isEmpty()) {
                            // Set default image
                            getLayoutInflater().inflate(R.layout.end_student_item, parentLayout, true);
                        } else {
                            addMemberCardToViewByList((ArrayList) students);
                        }
                    }
                });
    }

    private void addMemberCardToViewByList(ArrayList<DocumentReference> students) {
        for (DocumentReference post : students) {
            post.get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                addMemberCardToView(task.getResult(), false);
                            } else {
                                Log.e(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }
    }

    private void addMemberCardToView(DocumentSnapshot document, boolean isTeacher) {
        ConstraintLayout card;
        if (isTeacher) {
            card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.single_teacher_item, parentLayout, false);
        } else {
            card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.single_member_item, parentLayout, false);
            card.findViewById(R.id.delete_student_btn).setVisibility(userUpdateBtnVisibility);
        }


        // Set Member Name
        ((TextView) card.findViewById(R.id.member_username)).setText(document.getString(USER_PROPERTY_NAME));
        // Set Member Email
        ((TextView) card.findViewById(R.id.member_email)).setText(document.getId());

        if (!isTeacher) {
            setDeleteOnClickLister(document.getId(), card);
        }

        parentLayout.addView(card);
    }

    private void setDeleteOnClickLister(final String email, ConstraintLayout card) {
        card.findViewById(R.id.delete_student_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove Student")
                        .setMessage("Do you really want to remove " + email + "?")
                        .setIcon(R.drawable.user)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeClassroomFromUser(email);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });

    }

    private void removeClassroomFromUser(final String userEmail) {
        final DocumentReference userRef = db.collection("user")
                .document(userEmail);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List classroomList = (ArrayList) document.get("classroom_list");
                        if (classroomList != null) {
                            classroomList.remove(classroomId);
                        }
                        userRef.update("classroom_list", classroomList);
                        deleteUserFromClassroom(userRef);

                        Toast.makeText(getActivity(), userEmail + " removed", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), userEmail + " unable to remove", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void deleteUserFromClassroom(final DocumentReference userRef) {
        final DocumentReference docRef = db.collection("classrooms")
                .document(classroomId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List students = (ArrayList) document.get("students");
                        if (students != null) {
                            students.remove(userRef);
                        }
                        docRef.update("students", students);
                    }
                }
            }
        });
    }


    private void addHeaderCardToView(Boolean isTeacherCard) {
        ConstraintLayout card;
        TextView title;
        ImageButton addButton;
        if (isTeacherCard) {
            card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.single_member_add_new, parentLayout, false);
            title = card.findViewById(R.id.header_title);
            title.setText("Teacher");
        } else {
            card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.single_member_add_new, parentLayout, false);
            title = card.findViewById(R.id.header_title);
            title.setText("Students");

            addButton = card.findViewById(R.id.add_member_btn);
            addButton.setVisibility(userUpdateBtnVisibility);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), AddUserActivity.class);
                    intent.putExtra("classroomId", classroomId);
                    // TODO check if need to add on result activity here instead of in main classroom detail activity
                    Log.i(TAG, "STARTING ADD USER with code" + ClassroomDetail.REQUEST_CODE_ADD_USER);
                    getActivity().startActivityForResult(intent, ClassroomDetail.REQUEST_CODE_ADD_USER);
                }
            });
        }

        parentLayout.addView(card);
    }
}
