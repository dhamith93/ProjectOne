package io.github.dhamith93.projectone;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class ProjectOne extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
