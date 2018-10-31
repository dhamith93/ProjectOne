package io.github.dhamith93.projectone;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewGroupActivity extends AppCompatActivity {
    private EditText txtGroupName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        txtGroupName = findViewById(R.id.txtNewGroupName);

        (findViewById(R.id.btnGroupAdd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasErrors()) {
                    String groupName = txtGroupName.getText().toString();
                    final String newProjectId = FirebaseDatabase
                            .getInstance()
                            .getReference("groups")
                            .push()
                            .getKey();

                    final DatabaseReference groupsReference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("groups")
                            .child(newProjectId);

                    HashMap<String, String> groupInfo = new HashMap<>();
                    groupInfo.put("name", groupName);

                    groupsReference.setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference groupMemberReference = groupsReference.child("members");
                                final HashMap<String, String> memberInfo = new HashMap<>();
                                memberInfo.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), "1");

                                groupMemberReference.setValue(memberInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference memberReference = FirebaseDatabase
                                                    .getInstance()
                                                    .getReference()
                                                    .child("users")
                                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("leaderOf");
                                            HashMap<String, Object> memberDocInfo = new HashMap<>();
                                            memberDocInfo.put(newProjectId, "1");

                                            memberReference.updateChildren(memberDocInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    DatabaseReference memberOfReference = FirebaseDatabase
                                                            .getInstance()
                                                            .getReference()
                                                            .child("users")
                                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .child("memberOf")
                                                            .child(newProjectId);

                                                    HashMap<String, String> memberActiveInfo = new HashMap<>();
                                                    memberActiveInfo.put("active", "1");

                                                    memberOfReference.setValue(memberActiveInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                        showSnackBar("Error!");
                                    }
                                });
                            }
                            showSnackBar("Error!");
                        }
                    });
                }

            }
        });
    }

    private boolean hasErrors() {
        String name = txtGroupName.getText().toString();
        boolean error = false;

        if (name.isEmpty()) {
            error = true;
            ((EditText) findViewById(R.id.nameNewGroupWrapper)).setError("Group name is empty!");
        }

        return error;
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.newGroupCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
