package io.github.dhamith93.projectone.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.adapters.NotificationListAdapter;
import io.github.dhamith93.projectone.pojo.Notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {
    private RecyclerView notificationList;
    private NotificationListAdapter firebaseRecyclerAdapter;
    private DatabaseReference notificationReference;

    public NotificationFragment() {  }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        notificationList = view.findViewById(R.id.notificationList);

        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        notificationReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("notifications")
                .child(currentUid);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        notificationList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        notificationList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                notificationList.getContext(),
                layoutManager.getOrientation()
        );
        notificationList.addItemDecoration(dividerItemDecoration);

        Query query = notificationReference.orderByChild("seen").equalTo("0");

        FirebaseRecyclerOptions<Notification> options =
                new FirebaseRecyclerOptions.Builder<Notification>()
                        .setQuery(query, Notification.class)
                        .build();

        firebaseRecyclerAdapter = new NotificationListAdapter(options);

        notificationList.setAdapter(firebaseRecyclerAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                firebaseRecyclerAdapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(notificationList);
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseRecyclerAdapter.startListening();
    }
}
