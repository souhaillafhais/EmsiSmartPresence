package com.example.emsismartpresence;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocViewHolder> {
    private List<DocumentProfesseur> liste;
    private Context context;

    public DocumentAdapter(List<DocumentProfesseur> liste, Context context) {
        this.liste = liste;
        this.context = context;
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new DocViewHolder(view);
    }




    @Override
    public int getItemCount() {
        return liste.size();
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitre;
        Button btnVoir;

        Button btnTelecharger;

        public DocViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitre = itemView.findViewById(R.id.txtTitre);
            btnVoir = itemView.findViewById(R.id.btnVoir);
            btnTelecharger = itemView.findViewById(R.id.btnTelecharger);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        DocumentProfesseur doc = liste.get(position);
        holder.txtTitre.setText(doc.getTitre());

        holder.btnVoir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(doc.getUrlFichier()));
            context.startActivity(intent);
        });

        holder.btnTelecharger.setOnClickListener(v -> {
            // Lance un téléchargement via DownloadManager
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(doc.getUrlFichier());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(doc.getTitre());
            request.setDescription("Téléchargement en cours...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, uri.getLastPathSegment());

            downloadManager.enqueue(request);
            Toast.makeText(context, "Téléchargement démarré", Toast.LENGTH_SHORT).show();
        });
    }

}

