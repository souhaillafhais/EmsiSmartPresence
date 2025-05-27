package com.example.emsismartpresence;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {
    private RecyclerView groupsRecyclerView;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        groupsRecyclerView = findViewById(R.id.groupsRecyclerView);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadUserGroups();
    }

    private void loadUserGroups() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> groupIds = (List<String>) documentSnapshot.get("groups");
                        if (groupIds != null && !groupIds.isEmpty()) {
                            fetchGroupsDetails(groupIds);
                        } else {
                            showNoGroupsMessage();
                        }
                    }
                });
    }

    private void fetchGroupsDetails(List<String> groupIds) {
        db.collection("groups")
                .whereIn(FieldPath.documentId(), groupIds)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Group> groups = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        group.setId(doc.getId());
                        groups.add(group);
                    }
                    setupGroupsAdapter(groups);
                });
    }

    private void setupGroupsAdapter(List<Group> groups) {
        GroupsAdapter adapter = new GroupsAdapter(groups, group -> {
            Intent intent = new Intent(this, StudentListActivity.class);
            intent.putExtra("GROUP_ID", group.getId());
            startActivity(intent);
        });
        groupsRecyclerView.setAdapter(adapter);
    }

    private void showNoGroupsMessage() {
        Toast.makeText(this, "Aucun groupe disponible", Toast.LENGTH_SHORT).show();
    }
}