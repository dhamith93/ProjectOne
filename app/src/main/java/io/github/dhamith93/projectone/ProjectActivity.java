package io.github.dhamith93.projectone;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ProjectActivity extends AppCompatActivity {
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
            projectId = extras.getString("projectId");

        ((EditText) findViewById(R.id.txtName)).setText(projectId);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
