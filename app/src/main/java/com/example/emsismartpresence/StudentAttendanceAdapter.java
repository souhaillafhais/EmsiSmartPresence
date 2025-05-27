package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAttendanceAdapter extends RecyclerView.Adapter<StudentAttendanceAdapter.StudentViewHolder> {
    private List<Student> students;
    private OnAttendanceChangeListener listener;

    public StudentAttendanceAdapter(List<Student> students, OnAttendanceChangeListener listener) {
        this.students = students;
        this.listener = listener;
    }

    // Ajoutez cette nouvelle méthode
    public void updateStudentStatus(String studentId, String status) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equals(studentId)) {
                students.get(i).setStatus(status);
                notifyItemChanged(i);
                break;
            }
        }
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
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {
        private TextView studentName;
        private RadioGroup attendanceRadioGroup;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            attendanceRadioGroup = itemView.findViewById(R.id.attendanceRadioGroup);

            attendanceRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    boolean isPresent = checkedId == R.id.radioPresent;
                    listener.onAttendanceChanged(students.get(position), isPresent);
                }
            });
        }

        public void bind(Student student) {
            studentName.setText(student.getFirstName() + " " + student.getLastName());
            attendanceRadioGroup.check(
                    "present".equals(student.getStatus()) ? R.id.radioPresent : R.id.radioAbsent
            );
        }
    }

    public interface OnAttendanceChangeListener {
        void onAttendanceChanged(Student student, boolean isPresent);
    }
}