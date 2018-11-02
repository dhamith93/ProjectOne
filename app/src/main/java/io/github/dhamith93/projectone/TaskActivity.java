package io.github.dhamith93.projectone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

public class TaskActivity extends AppCompatActivity {
    private String projectId;
    private String taskId;
    private String memberId;
    private String groupId;
    private boolean isOwner;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtMember;
    private CheckBox checkBoxDone;

    private List<String> memberIds;
    private List<String> memberNames;

    private DatabaseReference taskReference;
    private DatabaseReference memberReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            projectId = extras.getString("projectId");
            taskId = extras.getString("taskId");
            memberId = extras.getString("memberId");
            groupId = extras.getString("groupId");

            if (extras.containsKey("fromMyTask"))
                (findViewById(R.id.btnOpenProject)).setVisibility(View.VISIBLE);
        }

        isOwner = false;

        txtName = findViewById(R.id.txtTaskInfoName);
        txtDesc = findViewById(R.id.txtTaskInfoDesc);
        txtMember = findViewById(R.id.taskMember);
        checkBoxDone = findViewById(R.id.checkBoxDone);

        memberIds = new ArrayList<>();
        memberNames = new ArrayList<>();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getUid().equals(memberId)) {
            checkBoxDone.setEnabled(true);
        }

        memberReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(memberId);

        taskReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects")
                .child(projectId)
                .child("tasks")
                .child(taskId);

        memberReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtMember.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        taskReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtName.setText(dataSnapshot.child("name").getValue().toString());
                txtDesc.setText(dataSnapshot.child("desc").getValue().toString());
                String status = dataSnapshot.child("status").getValue().toString();

                if (status.equals("completed"))
                    checkBoxDone.setChecked(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

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

        DatabaseReference projectReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects")
                .child(projectId)
                .child("owner");

        projectReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isOwner = dataSnapshot.getValue().toString().equals(currentUser.getUid());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        checkBoxDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = "pending";
                if (checkBoxDone.isChecked())
                    status = "completed";

                taskReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showSnackBar("Task status updated!");
                        } else {
                            showSnackBar("Error! Please try again.");
                        }

                        final DatabaseReference projectReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("projects")
                                .child(projectId);

                        DatabaseReference tasksReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("projects")
                                .child(projectId)
                                .child("tasks");

                        tasksReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int completedCount = 0;

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    if (ds.child("status").getValue().toString().equals("completed"))
                                        completedCount += 1;
                                }

                                int percentage = (int) (completedCount * 100.0) / (int) dataSnapshot.getChildrenCount();

                                projectReference.child("progress").setValue(String.valueOf(percentage));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });

            }
        });

        txtMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOwner) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                    builder.setTitle("Select a member");
                    CharSequence groups[] = new CharSequence[memberNames.size()];

                    for (int i = 0; i < groups.length; i++)
                        groups[i] = memberNames.get(i);

                    builder.setItems(groups, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateTaskMember(memberIds.get(which), memberNames.get(which));
                        }
                    });
                    builder.show();
                }
            }
        });

        (findViewById(R.id.btnOpenProject)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent projectIntent = new Intent(TaskActivity.this, ProjectActivity.class);
                projectIntent.putExtra("projectId", projectId);
                startActivity(projectIntent);
            }
        });

        (findViewById(R.id.btnTaskChat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO start chat
            }
        });
    }

    private void updateTaskMember(final String newMemberId, final String newMemberName) {
        memberReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnapshot.child("tasks").child(projectId).child(taskId).getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        DatabaseReference newMemberReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(newMemberId)
                .child("tasks")
                .child(projectId)
                .child(taskId);

        HashMap<String, String> taskData = new HashMap<>();
        taskData.put("active", "1");

        newMemberReference.setValue(taskData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                taskReference.child("member").setValue(newMemberId).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("tasks")
                                .child(memberId)
                                .child(taskId)
                                .removeValue();

                        DatabaseReference taskReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("tasks")
                                .child(newMemberId)
                                .child(taskId);

                        memberId = newMemberId;

                        HashMap<String, String> taskInfo = new HashMap<>();
                        taskInfo.put("projectId", projectId);

                        taskReference.setValue(taskInfo);

                        ((EditText) findViewById(R.id.taskMember)).setText(newMemberName);

                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(newMemberId)) {
                            checkBoxDone.setEnabled(true);
                        } else {
                            checkBoxDone.setEnabled(false);
                        }

                        final DatabaseReference notificationReference = FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("notifications")
                                .child(newMemberId)
                                .child(taskId);

                        HashMap<String, String> notificationInfo = new HashMap<>();
                        notificationInfo.put("from", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notificationInfo.put("type", "newTask");
                        notificationInfo.put("groupName", "null");
                        notificationInfo.put("groupId", groupId);
                        notificationInfo.put("seen", "0");

                        notificationReference.setValue(notificationInfo);

                        showSnackBar("Task assigned to " + newMemberName);
                    }
                });
            }
        });
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.singleTaskCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
