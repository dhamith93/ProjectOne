package io.github.dhamith93.projectone;

import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class TaskActivity extends AppCompatActivity {
    private String projectId;
    private String taskId;
    private String memberId;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtMember;
    private CheckBox checkBoxDone;

    private DatabaseReference taskReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            projectId = extras.getString("projectId");
            taskId = extras.getString("taskId");
            memberId = extras.getString("memberId");
            String groupId = extras.getString("groupId");
        }

        txtName = findViewById(R.id.txtTaskInfoName);
        txtDesc = findViewById(R.id.txtTaskInfoDesc);
        txtMember = findViewById(R.id.taskMember);
        checkBoxDone = findViewById(R.id.checkBoxDone);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getUid().equals(memberId)) {
            Log.e("exception", currentUser.getUid());
            Log.e("exception", memberId);
            checkBoxDone.setEnabled(true);
        }

        DatabaseReference memberReference = FirebaseDatabase
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

        (findViewById(R.id.btnTaskChat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO start chat
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
