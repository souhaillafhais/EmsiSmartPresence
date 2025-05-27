package com.example.emsismartpresence;

public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String nfcCardId;
    private String status;

    public Student() {
    }

    public Student(String id, String firstName, String lastName, String nfcCardId, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nfcCardId = nfcCardId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNfcCardId() {
        return nfcCardId;
    }

    public void setNfcCardId(String nfcCardId) {
        this.nfcCardId = nfcCardId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}