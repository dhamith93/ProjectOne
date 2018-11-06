package io.github.dhamith93.projectone.notifier;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Notifier {
    private String id;
    private String from;
    private String to;
    private String subject;
    private String type;
    private String groupId;

    private DatabaseReference notificationReference;
    private HashMap<String, String> notificationInfo = new HashMap<>();

    public Notifier(String id, String from, String to, String subject, String type, String groupId) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.type = type;
        this.groupId = groupId;

        notificationReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("notifications")
                .child(to)
                .child(id);
    }

    public void send() {
        notificationInfo.put("from", from);
        notificationInfo.put("type", type);
        notificationInfo.put("subject", subject);
        notificationInfo.put("groupId", groupId);
        notificationInfo.put("seen", "0");

        notificationReference.setValue(notificationInfo);
    }

}
