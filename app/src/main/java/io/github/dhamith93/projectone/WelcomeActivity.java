package io.github.dhamith93.projectone;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;

public class WelcomeActivity extends AppCompatActivity {

    private Intent homeIntent;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference firebaseDatabase;

    private static final int RC_SIGN_IN = 9001;
    private static final String REDIRECT_URI_CALLABACK = "local://github.auth";
    private boolean isAuthenticating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();

        homeIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        isAuthenticating = false;

        (findViewById(R.id.btnGoogleSignIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAuthenticating) {
                    signInWithGoogle();
                }
            }
        });

        (findViewById(R.id.btnGitHubSignIn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAuthenticating) {
                    signInWithGitHub();
                }
            }
        });

        Uri uri = getIntent().getData();

        if (uri != null && uri.toString().startsWith(REDIRECT_URI_CALLABACK)) {
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");
            if (code != null && state != null)
                sendPost(code, state);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.getUid().isEmpty())
            startActivity(homeIntent);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithGitHub() {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("http")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", getString(R.string.github_client_id))
                .addQueryParameter("redirect_uri", REDIRECT_URI_CALLABACK)
                .addQueryParameter("state", getRandString())
                .addQueryParameter("scope", "user:email")
                .build();

        Intent githubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()));
        startActivity(githubIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showSnackBar("AUTHENTICATION ERROR!");
                isAuthenticating = false;
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    isAuthenticating = false;
                    if (!task.isSuccessful()) {
                        showSnackBar("AUTHENTICATION ERROR!");
                        return;
                    }

                    addUserToDatabase();
                    startActivity(homeIntent);
                }
            });
    }

    private void sendPost(String code, String state) {
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody form = new FormBody.Builder()
                .add("client_id", getString(R.string.github_client_id))
                .add("client_secret", getString(R.string.github_client_secret))
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI_CALLABACK)
                .add("state", state)
                .build();

        Request request = new Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(form)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isAuthenticating = false;
                showSnackBar("AUTHENTICATION ERROR!");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String[] responseBody = response.body().string().split("[=&]");
                if (responseBody[0].equalsIgnoreCase("access_token")) {
                    signInWithGitHubToken(responseBody[1]);
                } else {
                    showSnackBar("AUTHENTICATION ERROR!");
                }
            }
        });
    }

    private void signInWithGitHubToken(String token) {
        AuthCredential credential = GithubAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        isAuthenticating = false;
                        if (!task.isSuccessful()) {
                            showSnackBar("AUTHENTICATION ERROR!");
                            return;
                        }

                        addUserToDatabase();
                        startActivity(homeIntent);
                    }
                });
    }

    private void addUserToDatabase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(currentUser.getUid());

        final HashMap<String, String> userData = new HashMap<>();
        userData.put("name", currentUser.getDisplayName());
        userData.put("email", currentUser.getEmail());
        userData.put("profile_pic", currentUser.getPhotoUrl().toString());
        userData.put("status", "online");

        firebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    firebaseDatabase.setValue(userData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private String getRandString() {
        return new BigInteger(130, new SecureRandom()).toString();
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.coordinatedLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
