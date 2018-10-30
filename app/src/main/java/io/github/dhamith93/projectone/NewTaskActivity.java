package io.github.dhamith93.projectone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class NewTaskActivity extends AppCompatActivity {
    private String projectId;
    private String groupId;
    private String selectedMemberId;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtMember;
    private List<String> memberIds;
    private List<String> memberNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            projectId = extras.getString("projectId");
            groupId = extras.getString("groupId");
        }

        txtName = findViewById(R.id.txtTaskName);
        txtDesc = findViewById(R.id.txtTaskDesc);
        txtMember = findViewById(R.id.member);

        memberIds = new ArrayList<>();
        memberNames = new ArrayList<>();

        DatabaseReference groupReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groups")
                .child(groupId)
                .child("members");

        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        memberIds.add(ds.getKey());
                        DatabaseReference userReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("users")
                                .child(ds.getKey());
                        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                memberNames.add(dataSnapshot.child("name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        (findViewById(R.id.btnTaskAdd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasErrors()) {
                    String name = txtName.getText().toString();
                    String desc = txtDesc.getText().toString();


                    try {
                        final String key = FirebaseDatabase
                                .getInstance()
                                .getReference("projects")
                                .child(projectId)
                                .child("tasks")
                                .push()
                                .getKey();

                        DatabaseReference projectReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("projects")
                                .child(projectId)
                                .child("tasks")
                                .child(key);

                        HashMap<String, String> projectData = new HashMap<>();
                        projectData.put("name", name);
                        projectData.put("desc", desc);
                        projectData.put("member", selectedMemberId);

                        projectReference.setValue(projectData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference userReference = FirebaseDatabase
                                            .getInstance()
                                            .getReference()
                                            .child("users")
                                            .child(selectedMemberId)
                                            .child("tasks")
                                            .child(projectId)
                                            .child(key);

                                    HashMap<String, String> project = new HashMap<>();
                                    project.put("active", "1");
                                    userReference.setValue(project).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showSnackBar("ERROR!");
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackBar("ERROR!");
                            }
                        });
                    } catch (Exception ex) {
                        showSnackBar(ex.getMessage());
                    }
                }
            }
        });

        (findViewById(R.id.member)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(NewTaskActivity.this);
                builder.setTitle("Select a member");
                CharSequence groups[] = new CharSequence[memberNames.size()];

                for (int i = 0; i < groups.length; i++)
                    groups[i] = memberNames.get(i);

                builder.setItems(groups, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((EditText) findViewById(R.id.member)).setText(memberNames.get(which));
                        selectedMemberId = memberIds.get(which);
                        ((TextInputLayout)findViewById(R.id.memberWrapper))
                                .setError(null);
                    }
                });
                builder.show();
            }
        });
    }

    private boolean hasErrors() {
        boolean error = false;
        String name = txtName.getText().toString();
        String desc = txtDesc.getText().toString();
        String member = txtMember.getText().toString();

        if (name.isEmpty()) {
            ((TextInputLayout)findViewById(R.id.nameWrapper))
                    .setError("Name is empty!");
            error = true;
        }

        if (desc.isEmpty()) {
            ((TextInputLayout)findViewById(R.id.descWrapper))
                    .setError("Description is empty!");
            error = true;
        }

        if (member.isEmpty()) {
            ((TextInputLayout)findViewById(R.id.memberWrapper))
                    .setError("Select a member!");
            error = true;
        }

        return error;
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.newTaskCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
