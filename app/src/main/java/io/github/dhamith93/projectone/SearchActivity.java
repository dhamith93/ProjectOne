package io.github.dhamith93.projectone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.dhamith93.projectone.notifier.Notifier;
import io.github.dhamith93.projectone.pojo.User;

public class SearchActivity extends AppCompatActivity {
    private String groupId;
    private String groupName;

    private EditText txtSearchKey;
    private RecyclerView resultListView;

    DatabaseReference usersReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            groupId = extras.getString("groupId");
            groupName = extras.getString("groupName");
        }

        txtSearchKey = findViewById(R.id.txtSearchKey);
        resultListView = findViewById(R.id.resultListView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        resultListView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                resultListView.getContext(),
                layoutManager.getOrientation()
        );
        resultListView.addItemDecoration(dividerItemDecoration);

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users");

        txtSearchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

    }

    private void search() {
        String searchKey = txtSearchKey.getText().toString();

        if (searchKey.isEmpty())
            return;

        Query searchQuery = usersReference
                .orderByChild("email")
                .startAt(searchKey)
                .endAt(searchKey + "\uf8ff");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(searchQuery, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull User user) {
                final String uid = getRef(i).getKey();
                final String name = user.getName();
                usersViewHolder.setName(user.getName());
                usersViewHolder.setProfilePic(user.getProfile_pic());

                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final DatabaseReference groupReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("groups")
                                .child(groupId)
                                .child("members");

                        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean groupHasMember = false;
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.getKey().equals(uid))
                                        groupHasMember = true;
                                }

                                if (!groupHasMember) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this)
                                            .setMessage("Do you want to invite " + name + " to '" + groupName + "'?")
                                            .create();

                                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            final String currentUId = FirebaseAuth
                                                    .getInstance()
                                                    .getCurrentUser()
                                                    .getUid();

                                            final DatabaseReference notificationReference = FirebaseDatabase
                                                    .getInstance()
                                                    .getReference()
                                                    .child("notifications")
                                                    .child(uid)
                                                    .child(groupId);

                                            notificationReference.keepSynced(true);

                                            notificationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        showSnackBar("You've already sent an invite!");
                                                        return;
                                                    }

                                                    Notifier notifier = new Notifier(
                                                            groupId,
                                                            currentUId,
                                                            uid,
                                                            groupName,
                                                            "groupInvite",
                                                            groupId
                                                    );

                                                    notifier.send();

                                                    showSnackBar("Invitation sent!");

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                                            });
                                        }
                                    });
                                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });

                                    alertDialog.show();

                                    Button btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    Button btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                                    layoutParams.leftMargin = 10;
                                    layoutParams.rightMargin = 10;
                                    btnPositive.setLayoutParams(layoutParams);
                                    btnNegative.setLayoutParams(layoutParams);
                                } else {
                                    showSnackBar(name + " is already a member of group '" + groupName + "'!");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_row, parent, false);
                return new UsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        resultListView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View view;

        public UsersViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            ((TextView) view.findViewById(R.id.lblMemberName)).setText(name);
        }

        public void setProfilePic(String url) {
            Picasso.get().load(url).into(((CircleImageView) view.findViewById(R.id.profilePic)));
        }
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.searchCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
