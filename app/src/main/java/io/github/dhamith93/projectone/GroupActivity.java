package io.github.dhamith93.projectone;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class GroupActivity extends AppCompatActivity {
    private String groupId;
    private boolean isLeader;
    private List<String> members;

    private EditText groupName;
    private RecyclerView memberList;

    private DatabaseReference groupReference;
    private DatabaseReference membersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
            groupId = extras.getString("groupId");

        isLeader = false;

        members = new ArrayList<>();

        groupName = findViewById(R.id.txtGroupName);
        memberList = findViewById(R.id.groupMembersList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        memberList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                memberList.getContext(),
                layoutManager.getOrientation()
        );
        memberList.addItemDecoration(dividerItemDecoration);

        groupReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groups")
                .child(groupId);

        membersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users");

        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                members.clear();
                groupName.setText(dataSnapshot.child("name").getValue().toString());
                String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot ds : dataSnapshot.child("members").getChildren())
                    members.add(ds.getKey());

                DatabaseReference userReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("users")
                        .child(currentUid);

                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("leaderOf").child(groupId).exists()) {
                            (findViewById(R.id.btnGroupUpdate)).setVisibility(View.VISIBLE);
                            (findViewById(R.id.addMemberFab)).setVisibility(View.VISIBLE);
                            groupName.setClickable(true);
                            groupName.setFocusableInTouchMode(true);
                            isLeader = true;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        (findViewById(R.id.btnGroupUpdate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasErrors()) {
                    String newGroupName = groupName.getText().toString();
                    groupReference.child("name").setValue(newGroupName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                showSnackBar("Group name updated!");
                        }
                    });
                }
            }
        });

        (findViewById(R.id.addMemberFab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO search activity
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query memberQuery = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .orderByChild("memberOf/" + groupId + "/active")
                .equalTo("1");

        FirebaseRecyclerOptions<User> options =
        new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(memberQuery, User.class)
                .build();

        final FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, int i, @NonNull User user) {
                usersViewHolder.setName(user.getName());
                usersViewHolder.setProfilePic(user.getProfile_pic());
                final int pos = i;

                DatabaseReference userReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                userReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("leaderOf").child(groupId).exists()) {
                            if (!dataSnapshot.getKey().equals(members.get(pos)))
                                usersViewHolder.setDeleteButtonVisible();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                usersViewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!members.get(pos).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            DatabaseReference memberReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("users")
                                    .child(members.get(pos))
                                    .child("tasks");

                            memberReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String projectId = ds.getKey();
                                        DatabaseReference projectReference = FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("projects")
                                                .child(projectId)
                                                .child("group");

                                        projectReference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String projectGroupId = dataSnapshot.getValue().toString();

                                                if (projectGroupId.equals(groupId)) {
                                                    new AlertDialog.Builder(GroupActivity.this)
                                                            .setTitle("Can't remove member!")
                                                            .setMessage("This member has assigned tasks. Reassign them first!")
                                                            .setNegativeButton(android.R.string.ok, null)
                                                            .create()
                                                            .show();
                                                } else {
                                                    removeMemberFromGroup(members.get(pos));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        } else {
                            showSnackBar(members.get(pos));
                        }
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
        memberList.setAdapter(firebaseRecyclerAdapter);
    }

    private void removeMemberFromGroup(String memberId) {
        // TODO remove user
        showSnackBar("REMOVED");
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View view;
        FloatingActionButton btnDelete;

        public UsersViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
            btnDelete = view.findViewById(R.id.btnMemberDelete);
        }

        public void setName(String name) {
            ((TextView) view.findViewById(R.id.lblMemberName)).setText(name);
        }

        public void setProfilePic(String url) {
            Picasso.get().load(url).into(((CircleImageView) view.findViewById(R.id.profilePic)));
        }

        public void setDeleteButtonVisible() {
            btnDelete.show();
        }
    }

    private boolean hasErrors() {
        boolean error = false;
        String name = groupName.getText().toString();

        if (name.isEmpty()) {
            ((TextInputLayout)findViewById(R.id.nameGroupWrapper))
                    .setError("Group name is empty!");
            error = true;
        }
        return error;
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.groupCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
