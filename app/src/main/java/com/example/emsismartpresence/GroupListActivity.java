package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {
    private RecyclerView groupsRecyclerView;
    private GroupAdapter groupAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        teacherId = auth.getCurrentUser().getUid();

        groupsRecyclerView = findViewById(R.id.groupsRecyclerView);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchGroups();
    }

    private void fetchGroups() {
        db.collection("groups")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Group group = doc.toObject(Group.class);
                        group.setId(doc.getId());
                        groups.add(group);
                    }
                    groupAdapter = new GroupAdapter(groups, this::onGroupSelected);
                    groupsRecyclerView.setAdapter(groupAdapter);
                });
    }

    private void onGroupSelected(Group group) {
        Log.d("GROUPS", "Groupe sélectionné: " + group.getId());
        Intent intent = new Intent(this, StudentAttendanceActivity.class);
        intent.putExtra("groupId", group.getId());
        startActivity(intent);
    }
}