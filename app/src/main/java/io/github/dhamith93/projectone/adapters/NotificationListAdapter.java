package io.github.dhamith93.projectone.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.TaskActivity;
import io.github.dhamith93.projectone.pojo.Notification;

public class NotificationListAdapter extends FirebaseRecyclerAdapter<Notification, NotificationListAdapter.NotificationViewHolder> {
    public NotificationListAdapter(@NonNull FirebaseRecyclerOptions<Notification> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull final NotificationViewHolder notificationViewHolder, int i, @NonNull final Notification notification) {
        final String id = getRef(i).getKey();

        DatabaseReference memberReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(notification.getFrom());

        memberReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String sender = dataSnapshot.child("name").getValue().toString();
                String url = dataSnapshot.child("profile_pic").getValue().toString();
                final String type = notification.getType();
                final String groupId = notification.getGroupId();
                String groupName = notification.getGroupName();
                String title = "New Notification";
                String btnText = "View Task";

                if (type.equals("groupInvite")) {
                    btnText = "Accept";
                    title = "Join my group '" + groupName + "'!";
                } else if (type.equals("newTask")) {
                    title = "New task assignment from " + groupName;
                }

                notificationViewHolder.setNotification(title, sender, btnText, url);

                notificationViewHolder.btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String currentUId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        FirebaseDatabase
                                .getInstance()
                                .getReference()
                                .child("notifications")
                                .child(currentUId)
                                .child(id)
                                .removeValue();

                        if (type.equals("groupInvite")) {
                             FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("groups")
                                    .child(groupId)
                                    .child("members")
                                    .child(currentUId)
                                    .setValue("1");

                            DatabaseReference userReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("users")
                                    .child(currentUId)
                                    .child("memberOf")
                                    .child(groupId);
                            HashMap<String, String> activeData = new HashMap<>();
                            activeData.put("active", "1");
                            userReference.setValue(activeData);

                            Query projectReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("projects")
                                    .orderByChild("group")
                                    .equalTo(groupId);

                            projectReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        String projectId = ds.getRef().getKey();

                                        DatabaseReference userUpdateReference = FirebaseDatabase
                                                .getInstance()
                                                .getReference()
                                                .child("users")
                                                .child(currentUId)
                                                .child("projects")
                                                .child(projectId);
                                        HashMap<String, String> activeData = new HashMap<>();
                                        activeData.put("active", "1");
                                        userUpdateReference.setValue(activeData);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });

                        } else if (type.equals("newTask")) {
                            DatabaseReference taskReference = FirebaseDatabase
                                    .getInstance()
                                    .getReference()
                                    .child("tasks")
                                    .child(currentUId)
                                    .child(id)
                                    .child("projectId");

                            taskReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final String projectId = dataSnapshot.getValue().toString();

                                    Intent taskIntent = new Intent(notificationViewHolder.itemView.getRootView().getContext(), TaskActivity.class);
                                    taskIntent.putExtra("projectId", projectId);
                                    taskIntent.putExtra("taskId", id);
                                    taskIntent.putExtra("groupId", groupId);
                                    taskIntent.putExtra("memberId",currentUId);
                                    notificationViewHolder.itemView.getRootView().getContext().startActivity(taskIntent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.noitfication_row, parent, false);

        return new NotificationViewHolder(view);
    }

    public void deleteItem(int position) {
        getRef(position).removeValue();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView lblTitle;
        TextView lblSender;
        Button btnPositive;
        LinearLayout foreground;
        CircleImageView profilePic;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            lblTitle = itemView.findViewById(R.id.lblTitle);
            lblSender = itemView.findViewById(R.id.lblSender);
            btnPositive = itemView.findViewById(R.id.btnNotificationPositive);
            foreground = itemView.findViewById(R.id.foreground);
            profilePic = itemView.findViewById(R.id.notificationProfilePic);
        }

        public void setNotification(String title, String sender, String btnPositiveText, String url) {
            lblTitle.setText(title);
            lblSender.setText(sender);
            btnPositive.setText(btnPositiveText);
            Picasso.get().load(url).into(profilePic);
        }
    }
}
