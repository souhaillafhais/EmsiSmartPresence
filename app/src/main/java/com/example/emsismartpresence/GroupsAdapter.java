package com.example.emsismartpresence;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
    private List<Group> groups;
    private OnGroupClickListener listener;

    public GroupsAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView groupName;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onGroupClick(groups.get(position));
                }
            });
        }

        public void bind(Group group) {
            groupName.setText(group.getName());
        }
    }

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }
}
