package io.github.dhamith93.projectone;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.github.dhamith93.projectone.adapters.FragmentsAdapter;
import io.github.dhamith93.projectone.fragments.BottomNavigationDrawerFragment;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private int selectedTabIndex;
    private Intent welcomeIntent;

    private DatabaseReference projectsDatabase;
    private JSONArray projectArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar((Toolbar) findViewById(R.id.bottom_app_bar));

        welcomeIntent = new Intent(HomeActivity.this, WelcomeActivity.class);
        welcomeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        selectedTabIndex = 0;
        projectArray = new JSONArray();

        ViewPager viewPager = findViewById(R.id.tab_pager);
        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentsAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabIndex = tab.getPosition();
                if (selectedTabIndex == 0 || selectedTabIndex == 2) {
                    ((FloatingActionButton) findViewById(R.id.fab)).hide();
                } else {
                    ((FloatingActionButton) findViewById(R.id.fab)).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        projectsDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects");


        DatabaseReference userDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(currentUid);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.child("projects").getChildren()) {
                    projectsDatabase.child(ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                JSONObject projectObj = new JSONObject(dataSnapshot.getValue().toString());
                                projectArray.put(projectObj);
                            } catch (Exception ex) {
                                Log.d("PARSE ERROR", ex.getMessage());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showSnackBar(databaseError.getMessage());
            }
        });



        (findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class<?> cls = null;
                switch (selectedTabIndex) {
                    case 1:
                        cls = NewProjectActivity.class;
                        break;
                    case 3:
                        cls = NewGroupActivity.class;
                        break;
                    default:
                        // throw error
                        return;
                }

                if (cls != null)
                    startActivity(new Intent(HomeActivity.this, cls));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getUid().isEmpty())
            startActivity(welcomeIntent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            BottomNavigationDrawerFragment drawerFragment = new BottomNavigationDrawerFragment();
            drawerFragment.show(getSupportFragmentManager(), drawerFragment.getTag());
        }

        return true;
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.homeCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
