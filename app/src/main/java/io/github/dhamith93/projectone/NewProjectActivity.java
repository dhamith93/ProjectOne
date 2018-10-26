package io.github.dhamith93.projectone;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class NewProjectActivity extends AppCompatActivity {

    private int datePos;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener datePickerDialogListener;

    private EditText txtName;
    private EditText txtDesc;
    private EditText txtStartDate;
    private EditText txtEndDate;

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
                    // add project
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
}
