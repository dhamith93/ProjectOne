package io.github.dhamith93.projectone;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

public class ProjectListAdapter extends ArrayAdapter<JSONObject> {
    private Activity context;
    private ArrayList<JSONObject> items;

    public ProjectListAdapter(Activity activity, ArrayList<JSONObject> objects) {
        super(activity, R.layout.project_row, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView =  inflater.inflate(R.layout.project_row, null, true);

        try {
            JSONObject singleObject = items.get(position);
            ((TextView) rowView.findViewById(R.id.lblProjectName)).setText(singleObject.getString("name"));
            ((TextView) rowView.findViewById(R.id.lblProjectDesc)).setText(singleObject.getString("desc"));
            ((ProgressBar) rowView.findViewById(R.id.projectProgressBar)).setProgress(
                    Integer.getInteger(singleObject.getString("progress"))
            );

        } catch (Exception ex) {
            Log.e("PARSE ERROR", ex.getMessage());
        }

        return  rowView;
    }
}
