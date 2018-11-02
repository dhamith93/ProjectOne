package io.github.dhamith93.projectone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.dhamith93.projectone.pojo.MyTask;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.TaskActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyTasksFragment extends Fragment {
    private RecyclerView myTaskList;
    private DatabaseReference usersReference;
    private DatabaseReference projectsReference;
    private FirebaseUser currentUser;

    public MyTasksFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_my_tasks, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        myTaskList = mainView.findViewById(R.id.myTaskList);

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("tasks")
                .child(currentUser.getUid());

        projectsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects");

        myTaskList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        myTaskList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                myTaskList.getContext(),
                layoutManager.getOrientation()
        );
        myTaskList.addItemDecoration(dividerItemDecoration);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        final FirebaseRecyclerOptions<MyTask> options =
                new FirebaseRecyclerOptions.Builder<MyTask>()
                        .setQuery(usersReference, MyTask.class)
                        .build();

        FirebaseRecyclerAdapter<MyTask, MyTaskViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MyTask, MyTaskViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MyTaskViewHolder myTaskViewHolder, int i, @NonNull final MyTask myTask) {

                final String taskId = getRef(i).getKey();
                final String projectId = myTask.getProjectId();

                projectsReference.child(projectId).child("tasks").child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myTaskViewHolder.setName(dataSnapshot.child("name").getValue().toString());
                        myTaskViewHolder.setDesc(dataSnapshot.child("desc").getValue().toString());
                        myTaskViewHolder.setStatus(dataSnapshot.child("status").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                myTaskViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        projectsReference.child(projectId).child("group").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String groupId = dataSnapshot.getValue().toString();
                                Intent taskIntent = new Intent(getContext(), TaskActivity.class);
                                taskIntent.putExtra("projectId", projectId);
                                taskIntent.putExtra("taskId", taskId);
                                taskIntent.putExtra("groupId", groupId);
                                taskIntent.putExtra("memberId", currentUser.getUid());
                                taskIntent.putExtra("fromMyTask", true);
                                startActivity(taskIntent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public MyTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_task_row, parent, false);
                return new MyTaskViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        myTaskList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyTaskViewHolder extends RecyclerView.ViewHolder {
        View view;

        public MyTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            ((TextView) view.findViewById(R.id.lblMyTaskName)).setText(name);
        }

        public void setDesc(String desc) {
            ((TextView) view.findViewById(R.id.lblMyTaskDesc)).setText(desc);
        }

        public void setStatus(String status) {
            ((TextView) view.findViewById(R.id.lblMyTaskStatus)).setText("Status: " + status);
        }
    }
}
