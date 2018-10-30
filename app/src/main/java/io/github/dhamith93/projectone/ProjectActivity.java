package io.github.dhamith93.projectone;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProjectActivity extends AppCompatActivity {
    private String projectId;
    private String groupId;
    private int datePos;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener datePickerDialogListener;
    private DatabaseReference projectReference;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtStartDate;
    private EditText txtEndDate;
    private EditText txtGroup;

    private boolean startDateIsValid;
    private boolean endDateIsValid;
    private boolean isOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
            projectId = extras.getString("projectId");

        datePos = 0;
        startDateIsValid = true;
        endDateIsValid = true;
        isOwner = false;

        calendar = Calendar.getInstance();

        txtName = findViewById(R.id.txtName);
        txtDesc = findViewById(R.id.txtDesc);
        txtStartDate = findViewById(R.id.startDate);
        txtEndDate = findViewById(R.id.endDate);
        txtGroup = findViewById(R.id.txtGroup);

        projectReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects")
                .child(projectId);

        projectReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txtName.setText(dataSnapshot.child("name").getValue().toString());
                txtDesc.setText(dataSnapshot.child("desc").getValue().toString());
                txtStartDate.setText(dataSnapshot.child("startDate").getValue().toString());
                txtEndDate.setText(dataSnapshot.child("endDate").getValue().toString());

                String ownerId = dataSnapshot.child("owner").getValue().toString();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                groupId = dataSnapshot.child("group").getValue().toString();

                if (ownerId.equals(currentUserId)) {
                    (findViewById(R.id.btnUpdate)).setVisibility(View.VISIBLE);
                    (findViewById(R.id.floatingActionButton)).setVisibility(View.VISIBLE);
                    txtName.setClickable(true);
                    txtName.setFocusableInTouchMode(true);
                    txtDesc.setClickable(true);
                    txtDesc.setFocusableInTouchMode(true);
                    isOwner = true;
                }

                DatabaseReference groupReference = FirebaseDatabase
                        .getInstance()
                        .getReference()
                        .child("groups")
                        .child(groupId);

                groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        txtGroup.setText(dataSnapshot.child("name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                int progress = Integer.parseInt(dataSnapshot.child("progress").getValue().toString());
                progress = (progress == 0) ? 1 : progress;

                ((ProgressBar) findViewById(R.id.progressBar)).setProgress(progress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        (findViewById(R.id.btnUpdate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasErrors()) {
                    String name = txtName.getText().toString();
                    String desc = txtDesc.getText().toString();
                    String startDate = txtStartDate.getText().toString();
                    String endDate = txtEndDate.getText().toString();

                    HashMap<String, Object> projectData = new HashMap<>();
                    projectData.put("name", name);
                    projectData.put("desc", desc);
                    projectData.put("startDate", startDate);
                    projectData.put("endDate", endDate);

                    projectReference.updateChildren(projectData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            showSnackBar("Project Updated!");
                        }
                    });
                }
            }
        });

        (findViewById(R.id.floatingActionButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newTaskIntent = new Intent(ProjectActivity.this, NewTaskActivity.class);
                newTaskIntent.putExtra("projectId", projectId);
                newTaskIntent.putExtra("groupId", groupId);
                startActivity(newTaskIntent);
            }
        });

        datePickerDialogListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {
                setDate(year, month, date);
            }
        };

        txtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if (isOwner) {
                   datePos = 0;
                   displayDatePicker();
               }
            }
        });

        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOwner) {
                    datePos = 1;
                    displayDatePicker();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

//        final FirebaseRecyclerOptions<Project> options =
//                new FirebaseRecyclerOptions.Builder<Project>()
//                        .setQuery(usersReference, Project.class)
//                        .build();

    }

    private void displayDatePicker() {
        new DatePickerDialog(
                this,
                datePickerDialogListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean hasErrors() {
        boolean error = false;
        String name = txtName.getText().toString();
        String desc = txtDesc.getText().toString();

        if (startDateIsValid && endDateIsValid) {
            String startDate = txtStartDate.getText().toString();
            String endDate = txtEndDate.getText().toString();

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

            if (startDate.isEmpty()) {
                ((TextInputLayout)findViewById(R.id.startDateWrapper))
                        .setError("Start date is empty!");
                error = true;
            }

            if (endDate.isEmpty()) {
                ((TextInputLayout)findViewById(R.id.endDateWrapper))
                        .setError("End date is empty!");
                error = true;
            }
        } else {
            error = true;
        }
        return error;
    }

    private void setDate(int year, int month, int date) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        String dateString = dateFormat.format(calendar.getTime());

        if (datePos == 0) {
            txtStartDate.setText(dateString);
            if (!txtEndDate.getText().toString().isEmpty()) {
                if (endBeforeStart()) {
                    ((TextInputLayout)findViewById(R.id.startDateWrapper))
                            .setError("Start date is after end date!");
                    startDateIsValid = false;
                } else {
                    ((TextInputLayout)findViewById(R.id.startDateWrapper)).setError(null);
                    ((TextInputLayout)findViewById(R.id.endDateWrapper)).setError(null);
                    startDateIsValid = true;
                    endDateIsValid = true;
                }
            }
        } else {
            txtEndDate.setText(dateString);
            if (!txtStartDate.getText().toString().isEmpty()) {
                if (endBeforeStart()) {
                    ((TextInputLayout)findViewById(R.id.endDateWrapper))
                            .setError("End date is before start date!");
                    endDateIsValid = false;
                } else {
                    ((TextInputLayout)findViewById(R.id.startDateWrapper)).setError(null);
                    ((TextInputLayout)findViewById(R.id.endDateWrapper)).setError(null);
                    endDateIsValid = true;
                    startDateIsValid = true;
                }
            }
        }
    }

    private boolean endBeforeStart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy", Locale.US);
        try {
            Date date1 = dateFormat.parse(txtStartDate.getText().toString());
            Date date2 = dateFormat.parse(txtEndDate.getText().toString());
            if (date2.compareTo(date1) < 0)
                return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.projectCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
