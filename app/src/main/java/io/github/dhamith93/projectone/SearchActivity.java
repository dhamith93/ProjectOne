package io.github.dhamith93.projectone;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {
    private String groupId;

    private EditText txtSearchKey;
    private RecyclerView resultListView;

    DatabaseReference usersReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Bundle extras = getIntent().getExtras();

        if (extras != null)
            groupId = extras.getString("projectId");

        txtSearchKey = findViewById(R.id.txtSearchKey);
        resultListView = findViewById(R.id.resultListView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        resultListView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                resultListView.getContext(),
                layoutManager.getOrientation()
        );
        resultListView.addItemDecoration(dividerItemDecoration);

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users");

        txtSearchKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

    }

    private void search() {
        String searchKey = txtSearchKey.getText().toString();

        if (searchKey.isEmpty())
            return;

        Query searchQuery = usersReference
                .orderByChild("email")
                .startAt(searchKey)
                .endAt(searchKey + "\uf8ff");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(searchQuery, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull User user) {
                usersViewHolder.setName(user.getName());
                usersViewHolder.setProfilePic(user.getProfile_pic());
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_row, parent, false);
                return new UsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        resultListView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View view;

        public UsersViewHolder(final View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            ((TextView) view.findViewById(R.id.lblMemberName)).setText(name);
        }

        public void setProfilePic(String url) {
            Picasso.get().load(url).into(((CircleImageView) view.findViewById(R.id.profilePic)));
        }
    }

    private void showSnackBar(String msg) {
        Snackbar.make(
                findViewById(R.id.searchCoordinatorLayout),
                msg,
                Snackbar.LENGTH_LONG
        ).show();
    }
}
