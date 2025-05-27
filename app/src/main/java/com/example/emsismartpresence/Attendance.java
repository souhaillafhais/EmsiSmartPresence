package com.example.emsismartpresence;

import java.util.Date;

public class Attendance {
    private String groupId;
    private String studentId;
    private boolean present;
    private String method; // "teacher" ou "nfc"
    private Date date;

    public Attendance() {}

    public Attendance(String groupId, String studentId, boolean present, String method, Date date) {
        this.groupId = groupId;
        this.studentId = studentId;
        this.present = present;
        this.method = method;
        this.date = date;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}