package io.github.dhamith93.projectone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.github.dhamith93.projectone.adapters.MessageListAdapter;
import io.github.dhamith93.projectone.pojo.Message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int TOTAL_MESSAGE_COUNT = 9;
    private int currentPage = 1;
    private int messagePos = 0;
    private String groupId;
    private String groupName;
    private String taskId;
    private String lastLoadedKey = "";
    private String prevLoadedKey = "";


    private Toolbar toolbar;
    private ImageButton btnSend;
    private EditText txtMsg;
    private RecyclerView messageList;
    private SwipeRefreshLayout swipeRefreshLayout;

    DatabaseReference chatReference;

    private final List<Message> messages = new ArrayList<>();
    private LinearLayoutManager linearLayout;

    private MessageListAdapter messageAdapter;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");

        messageAdapter = new MessageListAdapter(messages);

        toolbar = findViewById(R.id.chatAppBar);
        btnSend = findViewById(R.id.btnSend);
        txtMsg = findViewById(R.id.txtMsg);
        messageList = findViewById(R.id.messageList);
        swipeRefreshLayout = findViewById(R.id.messageSwipeRefreshLayout);

        linearLayout = new LinearLayoutManager(this);
        linearLayout.setStackFromEnd(true);

        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(linearLayout);

        messageList.setAdapter(messageAdapter);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(groupName);

        chatReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("chats")
                .child(groupId);

        user = FirebaseAuth.getInstance().getCurrentUser();

        loadMessages();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageText = txtMsg.getText().toString();

                sendMessage(messageText);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage += 1;
                messagePos = 0;
                loadOldMessages();
            }
        });
    }

    private void sendMessage(String messageText) {
        if (!TextUtils.isEmpty(messageText)) {
            String key = chatReference.push().getKey();

            Map messageInfo = new HashMap<>();
            messageInfo.put("from", user.getUid());
            messageInfo.put("message", messageText);
            messageInfo.put("timeStamp", ServerValue.TIMESTAMP);

            chatReference.child(key).setValue(messageInfo);
            txtMsg.setText("");
        }
    }

    private void loadMessages() {
        Query query = chatReference.limitToLast(currentPage * TOTAL_MESSAGE_COUNT);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
                messageAdapter.notifyDataSetChanged();

                messagePos += 1;

                if (messagePos == 1) {
                    lastLoadedKey = dataSnapshot.getKey();
                    prevLoadedKey = dataSnapshot.getKey();
                }

                messageList.scrollToPosition(messages.size() - 1);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void loadOldMessages() {
        Query query = chatReference.orderByKey().endAt(lastLoadedKey).limitToLast(TOTAL_MESSAGE_COUNT);

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                String key = dataSnapshot.getKey();

                if (!prevLoadedKey.equals(key)) {
                    messages.add(messagePos++, message);
                    messageAdapter.notifyDataSetChanged();
                } else {
                    prevLoadedKey = lastLoadedKey;
                }

                if (messagePos == 1)
                    lastLoadedKey = key;

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {  }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
