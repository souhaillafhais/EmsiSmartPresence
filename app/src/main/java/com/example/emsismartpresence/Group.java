package com.example.emsismartpresence;

import java.util.List;

public class Group {
    private String id;
    private String name;
    private List<String> studentIds;

    public Group() {
    }

    public Group(String id, String name, List<String> studentIds) {
        this.id = id;
        this.name = name;
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

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void setStudentIds(List<String> studentIds) {
        this.studentIds = studentIds;
    }
}
