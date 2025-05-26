package com.example.emsismartpresence;

public class DocumentProfesseur {
    private String titre;
    private String urlFichier;

    public DocumentProfesseur(String titre, String urlFichier) {
        this.titre = titre;
        this.urlFichier = urlFichier;
    }

    public String getTitre() { return titre; }
    public String getUrlFichier() { return urlFichier; }
}
