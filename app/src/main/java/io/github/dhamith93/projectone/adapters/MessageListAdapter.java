package io.github.dhamith93.projectone.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.TaskActivity;
import io.github.dhamith93.projectone.pojo.Message;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>{
    private List<Message> messageList;
    private FirebaseUser currentUser;
    private String groupId;

    public MessageListAdapter(List<Message> messageList) {
        this.messageList = messageList;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return (message.getFrom().equals(currentUser.getUid())) ? 0 : 1;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_message_layout, parent, false);
                break;
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_message_layout, parent, false);
                break;
        }

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:MM:ss");
        String timeStamp = dateFormat.format(new Date(message.getTimeStamp()));

        if (holder.getItemViewType() == 0) {
            holder.setAsFromUser();
            if (position != 0) {
                Message prevMessage = messageList.get(position - 1);

                if (prevMessage.getFrom().equals(message.getFrom())
                    && message.getTimeStamp() - prevMessage.getTimeStamp() < 60000) {
                    holder.sentTime.setVisibility(View.GONE);
                } else {
                    holder.sentTime.setVisibility(View.VISIBLE);
                }
            }
        } else {
            holder.setAsIncoming();

            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child("users")
                    .child(message.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.senderName.setText(dataSnapshot.child("name").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("profile_pic").getValue().toString()).into(holder.profilePic);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            if (position != 0) {
                Message prevMessage = messageList.get(position - 1);

                if (prevMessage.getFrom().equals(message.getFrom())) {
                    holder.profilePic.setVisibility(View.INVISIBLE);
                    holder.senderName.setVisibility(View.GONE);

                    if (message.getTimeStamp() - prevMessage.getTimeStamp() < 60000) {
                        holder.sentTime.setVisibility(View.GONE);
                    }
                } else {
                    holder.profilePic.setVisibility(View.VISIBLE);
                    holder.senderName.setVisibility(View.VISIBLE);
                    holder.sentTime.setVisibility(View.VISIBLE);
                }
            }
        }

        holder.messageText.setText(message.getMessage());
        holder.sentTime.setText(timeStamp);

        HashTagHelper hashTagHelper = HashTagHelper.Creator.create(Color.parseColor("#48C9B0"), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                String[] ids = hashTag.split("_"); // projectId_taskId
                openTaskActivity(ids[0], ids[1], holder.itemView.getContext());
            }
        }, '_', '-');

        hashTagHelper.handle(holder.messageText);
    }

    private void openTaskActivity(final String projectId, final String taskId, final Context context) {
        DatabaseReference projectReferene = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("projects")
                .child(projectId)
                .child("tasks")
                .child(taskId);

        projectReferene.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String memberId = dataSnapshot.child("member").getValue().toString();

                    Intent taskIntent = new Intent(context, TaskActivity.class);
                    taskIntent.putExtra("projectId", projectId);
                    taskIntent.putExtra("taskId", taskId);
                    taskIntent.putExtra("groupId", groupId);
                    taskIntent.putExtra("memberId", memberId);
                    if (currentUser.getUid().equals(memberId))
                        taskIntent.putExtra("fromMyTask", "1");
                    context.startActivity(taskIntent);
                } else {
                    Toast.makeText(context, "Invalid project_task id!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });



    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView senderName;
        TextView sentTime;
        CircleImageView profilePic;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);


        }

        public void setAsFromUser() {
            messageText = itemView.findViewById(R.id.user_message_text);
            sentTime = itemView.findViewById(R.id.user_sent_time);
        }

        public void setAsIncoming() {
            messageText = itemView.findViewById(R.id.incoming_message_text);
            senderName = itemView.findViewById(R.id.sender_name);
            sentTime = itemView.findViewById(R.id.sent_time);
            profilePic = itemView.findViewById(R.id.sender_profile_pic);
        }
    }
}
