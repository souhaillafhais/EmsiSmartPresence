package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder> {
    private List<Student> students;
    private OnAttendanceClickListener listener;

    public interface OnAttendanceClickListener {
        void onAttendanceClick(String studentId, boolean present);
    }

    public StudentAttendanceAdapter(List<Student> students, OnAttendanceClickListener listener) {
        this.students = students;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_attendance, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.studentName.setText(student.getName());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onAttendanceClick(student.getId(), isChecked);
        });
    }

    @Override
    public int getItemCount() { return students.size(); }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName;
        CheckBox checkBox;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.tv_student_name);
            checkBox = itemView.findViewById(R.id.cb_presence);
        }
    }
}
