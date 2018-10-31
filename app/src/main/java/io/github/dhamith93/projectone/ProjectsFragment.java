package io.github.dhamith93.projectone;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectsFragment extends Fragment {
    private RecyclerView projectList;
    private DatabaseReference usersReference;
    private DatabaseReference projectsReference;

    public ProjectsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_projects, container, false);
        projectList = mainView.findViewById(R.id.projectList);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("projects");

        projectsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects");
        projectsReference.keepSynced(true);

        projectList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        projectList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                projectList.getContext(),
                layoutManager.getOrientation()
        );
        projectList.addItemDecoration(dividerItemDecoration);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerOptions<Project> options =
                new FirebaseRecyclerOptions.Builder<Project>()
                        .setQuery(usersReference, Project.class)
                        .build();

        FirebaseRecyclerAdapter<Project, ProjectsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Project, ProjectsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ProjectsViewHolder projectsViewHolder, int i, @NonNull final Project project) {

                final String projectId = getRef(i).getKey();

                projectsReference.child(projectId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        projectsViewHolder.setName(dataSnapshot.child("name").getValue().toString());
                        projectsViewHolder.setDesc(dataSnapshot.child("desc").getValue().toString());
                        projectsViewHolder.setStartDate(dataSnapshot.child("startDate").getValue().toString());
                        projectsViewHolder.setEndDate(dataSnapshot.child("endDate").getValue().toString());
                        projectsViewHolder.setProgress(dataSnapshot.child("progress").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                projectsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent projectIntent = new Intent(view.getRootView().getContext(), ProjectActivity.class);
                        projectIntent.putExtra("projectId", projectId);
                        view.getRootView().getContext().startActivity(projectIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_row, parent, false);
                return new ProjectsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        projectList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ProjectsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ProjectsViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            ((TextView) view.findViewById(R.id.lblProjectName)).setText(name);
        }

        public void setDesc(String desc) {
            ((TextView) view.findViewById(R.id.lblProjectDesc)).setText(desc);
        }

        public void setStartDate(String startDate) {
            ((TextView) view.findViewById(R.id.lblStartDate)).setText("Start Date: " + startDate);
        }

        public void setEndDate(String endDate) {
            ((TextView) view.findViewById(R.id.lblEndDate)).setText("End Date: " + endDate);
        }

        public void setProgress(String progress) {
            ((TextView) view.findViewById(R.id.lblProgress)).setText("Progress: ");
            int progressInt = Integer.parseInt(progress);
            progressInt = (progressInt == 0) ? 1 : progressInt;
            ((ProgressBar) view.findViewById(R.id.projectProgressBar)).setProgress(progressInt);
        }
    }
}
