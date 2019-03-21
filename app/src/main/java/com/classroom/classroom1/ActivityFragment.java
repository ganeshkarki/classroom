package com.classroom.classroom1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A simple {@link Fragment} subclass.
 * // TODO: Rename to ShowPostListFragment
 */
public class ActivityFragment extends Fragment {
    @NonNull
    public static final String CONTENT_PROPERTY = "content";
    public static final String POST_AUTHOR_NAME_PROPERTY = "author_name";
    public static final String POST_CREATE_TIME_PROPERTY = "create_time";
    public static final String CLASSROOM_PROPERTY_POSTS = "posts";
    private static final String TAG = "Activity Fragment";
    int cardIndex = 0;
    private LinearLayout parentLayout;
    private StorageReference mStorageRef;
    private String classroomId;

    public ActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);
        parentLayout = view.findViewById(R.id.postCardHolder);
        classroomId = getArguments().getString(HomeActivity.CLASSROOM_ID);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        loadPosts(classroomId);

        return view;
    }

    private void loadPosts(String classroomId) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.e(TAG, "++++  " + classroomId + " ");
        db.collection("posts")
                .whereEqualTo("classroom_id", classroomId)
                .orderBy("create_time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (task.getResult() == null || task.getResult().size() == 0) {
                                getLayoutInflater().inflate(R.layout.empty_post_in_class, parentLayout, true);
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    addPostCardToView(document);
                                    Log.e(TAG, document.getId() + " => " + document.getData());
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void addPostCardToView(final QueryDocumentSnapshot document) {
        ConstraintLayout card = (ConstraintLayout) getLayoutInflater().inflate(R.layout.single_post_list, parentLayout, false);

        // Set Post content
        TextView contentText = card.findViewById(R.id.post_content);
        contentText.setText(document.getString(CONTENT_PROPERTY));

        // Set Post time
        String postTime = (String) DateUtils.getRelativeDateTimeString(this.getContext(),
                document.getTimestamp(POST_CREATE_TIME_PROPERTY).toDate().getTime(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
        ((TextView) card.findViewById(R.id.blog_date)).setText(postTime);

        // Set Post Author Name
        ((TextView) card.findViewById(R.id.post_user_name)).setText(document.getString(POST_AUTHOR_NAME_PROPERTY));

        if (document.get("has_attachment") == null) {
            card.findViewById(R.id.attachment_btn).setVisibility(View.INVISIBLE);
            card.findViewById(R.id.attachment_count_text).setVisibility(View.INVISIBLE);
            card.findViewById(R.id.attachment_txt).setVisibility(View.INVISIBLE);
        } else {
            card.findViewById(R.id.attachment_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent showAttachmentIntent = new Intent(getContext(), ShowImagesActivity.class);
                    showAttachmentIntent.putExtra("postId", document.getId());
                    startActivity(showAttachmentIntent);
                }
            });
        }

        parentLayout.addView(card, cardIndex++);
    }
}
