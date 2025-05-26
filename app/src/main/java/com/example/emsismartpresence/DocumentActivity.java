package com.example.emsismartpresence;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DocumentActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<DocumentProfesseur> listeDocs;
    DocumentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listeDocs = new ArrayList<>();
        adapter = new DocumentAdapter(listeDocs, this);
        recyclerView.setAdapter(adapter);

        chargerDocumentsDepuisCloudinary();
    }

    private void chargerDocumentsDepuisCloudinary() {
        String urlJson = "https://raw.githubusercontent.com/AhmimouAymane/docs/refs/heads/main/Doc.json";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, urlJson, null,
                response -> {
                    listeDocs.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String titre = obj.getString("titre");
                            String urlFichier = obj.getString("urlFichier");
                            listeDocs.add(new DocumentProfesseur(titre, urlFichier));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

}
