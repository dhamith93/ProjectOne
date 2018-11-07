package io.github.dhamith93.projectone.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.dhamith93.projectone.R;
import io.github.dhamith93.projectone.pojo.Message;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>{
    private List<Message> messageList;
    private FirebaseUser currentUser;

    public MessageListAdapter(List<Message> messageList) {
        this.messageList = messageList;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        }

        holder.messageText.setText(message.getMessage());
        holder.sentTime.setText(timeStamp);

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
