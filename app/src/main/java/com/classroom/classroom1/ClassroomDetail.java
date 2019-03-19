package com.classroom.classroom1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

public class ClassroomDetail extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_POST = 1;
    public static final int REQUEST_CODE_ADD_USER = 2;
    private static final String TAG = "ClassroomDetail";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private String classroomId;

    private Fragment activityFragment;
    private Fragment studentsFragment;

    private SwipeRefreshLayout mySwipeRefreshLayout;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom_detail);

        // Get the transferred data from source activity.

        Intent intent = getIntent();
        setTitle(intent.getStringExtra(Home.CLASSROOM_NAME));
        Log.i(TAG, "Title set");

        classroomId = intent.getStringExtra(Home.CLASSROOM_ID);
        Log.i(TAG, "received " + classroomId + " id from intent");

        initializeFragment();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), activityFragment, studentsFragment);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.main_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        // Todo: make it add post one
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassroomDetail.this, AddPostActivity.class);
                intent.putExtra("classroomId", classroomId);
                startActivityForResult(intent, REQUEST_CODE_ADD_POST);
            }
        });

        mySwipeRefreshLayout = findViewById(R.id.refreshactivity);
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

    // This method is invoked when target activity return result data back.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        Log.i(TAG, "reached on result of activity");
        View parentLayout = findViewById(R.id.main_content);

        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult(intent, REQUEST_CODE_ADD_POST); method.
        switch (requestCode) {
            // This request code is set by startActivityForResult(intent, REQUEST_CODE_ADD_POST) method.
            case ClassroomDetail.REQUEST_CODE_ADD_POST:
                Log.i(TAG, "Back from Add post activity");
                if (resultCode == RESULT_OK) {
                    // todo see why not returning data
                    Snackbar.make(parentLayout, "Post added. Please refresh your feed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                break;
            case ClassroomDetail.REQUEST_CODE_ADD_USER:
                Log.i(TAG, "Back from Add user activity");
                if (resultCode == RESULT_OK) {
                    // todo see why not returning data
                    Snackbar.make(parentLayout, "Your student is added. Please refresh your feed", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(parentLayout, "Unable to add student. Please check email", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                break;
            default:
                Log.e(TAG, "Unknown request code's result. req:" + requestCode + "res:" + resultCode);
        }
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

    private void initializeFragment() {
        activityFragment = new ActivityFragment();
        studentsFragment = new StudentsFragment();

        // Create a bundle to pass class id to fragments
        Bundle bundle = new Bundle();
        bundle.putString(Home.CLASSROOM_ID, classroomId);

        activityFragment.setArguments(bundle);
        studentsFragment.setArguments(bundle);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Fragment activity, students;

        public SectionsPagerAdapter(FragmentManager fm, Fragment activity, Fragment students) {
            super(fm);

            this.activity = activity;
            this.students = students;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = activity;
                    break;
                case 1:
                    fragment = students;
                    break;
                default:
                    fragment = activity;
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }


// TODO remove if not needed in future
//    private void replaceFragment(Fragment fragment, Fragment currentFragment){
//
//        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//
//        if(fragment == activityFragment){
//            fragmentTransaction.hide(studentsFragment);
//        }
//
//        if(fragment == studentsFragment){
//            fragmentTransaction.hide(activityFragment);
//        }
//
//        fragmentTransaction.show(fragment);
//
//        //fragmentTransaction.replace(R.id.main_container, fragment);
//        fragmentTransaction.commit();
//    }
}
