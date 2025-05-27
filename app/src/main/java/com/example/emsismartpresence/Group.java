package com.example.emsismartpresence;

import java.util.List;

public class Group {
    private String id;
    private String name;
    private String schoolCity;
    private String campus;
    private String teacherId;
    private List<String> studentIds;

    // Constructeur vide nécessaire pour Firestore
    public Group() {
    }

    public Group(String id, String name, String schoolCity, String campus, String teacherId, List<String> studentIds) {
        this.id = id;
        this.name = name;
        this.schoolCity = schoolCity;
        this.campus = campus;
        this.teacherId = teacherId;
        this.studentIds = studentIds;
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

    public String getSchoolCity() {
        return schoolCity;
    }

    public void setSchoolCity(String schoolCity) {
        this.schoolCity = schoolCity;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }
}

