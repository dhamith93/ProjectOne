package io.github.dhamith93.projectone;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

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

import java.text.*;
import java.util.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class NewProjectActivity extends AppCompatActivity {

    private int datePos;
    private String selectedGroupId;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener datePickerDialogListener;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtStartDate;
    private EditText txtEndDate;

    private List<String> groupNames;
    private List<String> groupIds;

    private DatabaseReference groupsReference;

    private boolean startDateIsValid;
    private boolean endDateIsValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        datePos = 0;
        startDateIsValid = true;
        endDateIsValid = true;

        calendar = Calendar.getInstance();

        txtName = findViewById(R.id.txtName);
        txtDesc = findViewById(R.id.txtDesc);
        txtStartDate = findViewById(R.id.startDate);
        txtEndDate = findViewById(R.id.endDate);

        groupNames = new ArrayList<>();
        groupIds = new ArrayList<>();

        DatabaseReference usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("leaderOf");

        groupsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groups");

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        final String groupId = ds.getKey();
                        groupsReference.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                groupIds.add(groupId);
                                groupNames.add(dataSnapshot.child("name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
                datePos = 0;
                displayDatePicker();
            }
        });

        txtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePos = 1;
                displayDatePicker();
            }
        });

        (findViewById(R.id.btnAdd)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasErrors()) {

                    String name = txtName.getText().toString();
                    String desc = txtDesc.getText().toString();
                    String startDate = txtStartDate.getText().toString();
                    String endDate = txtEndDate.getText().toString();

                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    final String key = FirebaseDatabase.getInstance().getReference("projects").push().getKey();

                    DatabaseReference projectReference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("projects")
                            .child(key);

                    HashMap<String, String> projectData = new HashMap<>();
                    projectData.put("name", name);
                    projectData.put("desc", desc);
                    projectData.put("startDate", startDate);
                    projectData.put("endDate", endDate);
                    projectData.put("progress", "0");
                    projectData.put("owner", currentUser.getUid());
                    projectData.put("group", selectedGroupId);

                    projectReference.setValue(projectData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference userReference = FirebaseDatabase
                                        .getInstance()
                                        .getReference()
                                        .child("users")
                                        .child(currentUser.getUid())
                                        .child("projects")
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
                } else {
                    Log.e("DATABASE_ERROR", "NO ERRORS");
                }
            }
        });

        (findViewById(R.id.group)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(NewProjectActivity.this);
                    builder.setTitle("Select a group");
                    CharSequence groups[] = new CharSequence[groupNames.size()];

                    for (int i = 0; i < groups.length; i++)
                        groups[i] = groupNames.get(i);

                    builder.setItems(groups, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((EditText) findViewById(R.id.group)).setText(groupNames.get(which));
                            selectedGroupId = groupIds.get(which);
                            ((TextInputLayout)findViewById(R.id.groupWrapper))
                                    .setError(null);
                        }
                    });
                    builder.show();
                } catch (Exception ex) {
                    showSnackBar(ex.getMessage());
                }
            }
        });
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

            if (selectedGroupId == null ||  selectedGroupId.isEmpty()) {
                ((TextInputLayout)findViewById(R.id.groupWrapper))
                        .setError("Select a group!");
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
                findViewById(R.id.newProjectCoordinatedLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
