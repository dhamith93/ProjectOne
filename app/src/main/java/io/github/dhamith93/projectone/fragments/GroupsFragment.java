package io.github.dhamith93.projectone.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.dhamith93.projectone.pojo.Group;
import io.github.dhamith93.projectone.GroupActivity;
import io.github.dhamith93.projectone.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class GroupsFragment extends Fragment {
    private RecyclerView groupList;
    private DatabaseReference usersReference;
    private DatabaseReference groupsReference;

    public GroupsFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupList = mainView.findViewById(R.id.groupList);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(auth.getCurrentUser().getUid())
                .child("memberOf");

        groupsReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("groups");

        groupList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        groupList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                groupList.getContext(),
                layoutManager.getOrientation()
        );
        groupList.addItemDecoration(dividerItemDecoration);

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(usersReference, Group.class)
                        .build();

        FirebaseRecyclerAdapter<Group, GroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupsViewHolder groupsViewHolder, int i, @NonNull final Group group) {

                final String groupId = getRef(i).getKey();

                groupsReference.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupsViewHolder.setGroupName(dataSnapshot.child("name").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                groupsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent groupIntent = new Intent(view.getRootView().getContext(), GroupActivity.class);
                        groupIntent.putExtra("groupId", groupId);
                        view.getRootView().getContext().startActivity(groupIntent);
                    }
                });
            }

            @NonNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_row, parent, false);
                return new GroupsViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        groupList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setGroupName(String groupName) {
            ((TextView) view.findViewById(R.id.lblGroupName)).setText(groupName);
        }
    }
}
