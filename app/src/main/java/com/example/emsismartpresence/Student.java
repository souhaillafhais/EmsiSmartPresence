package com.example.emsismartpresence;

public class Student {
    private String id;
    private String name;
    private String nfcTagId;
    private String groupId;

    public Student() {}

    public Student(String id, String name, String nfcTagId, String groupId) {
        this.id = id;
        this.name = name;
        this.nfcTagId = nfcTagId;
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNfcTagId() {
        return nfcTagId;
    }

    public void setNfcTagId(String nfcTagId) {
        this.nfcTagId = nfcTagId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}