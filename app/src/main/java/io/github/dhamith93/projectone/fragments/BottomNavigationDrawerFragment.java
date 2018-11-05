package io.github.dhamith93.projectone.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.dhamith93.projectone.HomeActivity;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.WelcomeActivity;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {
    private View view;
    private CircleImageView profilePic;
    private TextView displayName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_bottomsheet, container, false);

        profilePic = view.findViewById(R.id.header_profile_pic);
        displayName = view.findViewById(R.id.notificationName);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        displayName.setText(currentUser.getDisplayName());
        Picasso.get().load(currentUser.getPhotoUrl()).into(profilePic);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((NavigationView) view.findViewById(R.id.navigation_view)).setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_log_out) {
                    FirebaseAuth.getInstance().signOut();

                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.google_client_id))
                            .requestEmail()
                            .build();

                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent welcomeIntent = new Intent(getContext(), WelcomeActivity.class);
                            startActivity(welcomeIntent);
                        }
                    });
                }

                if (menuItem.getItemId() == R.id.action_about) {
                    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                    customTabsIntent.launchUrl(getContext(), Uri.parse(getString(R.string.about_url)));
                }

                return true;
            }
        });
    }
}
