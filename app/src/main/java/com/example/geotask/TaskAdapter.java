package com.example.geotask;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<TaskModel> taskList;

    // Constructor
    public TaskAdapter(List<TaskModel> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel task = taskList.get(position);

        // 1. Set Text Data
        holder.textTitle.setText(task.getTitle());
        holder.textAddress.setText(task.getAddress());

        // 2. Set Checkbox State (Remove listener first to prevent recycling bugs)
        holder.checkboxDone.setOnCheckedChangeListener(null);
        holder.checkboxDone.setChecked(task.getIsCompleted() == 1);

        // 3. Handle Checkbox Click (Mark Done/Pending)
        holder.checkboxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DatabaseHelper db = new DatabaseHelper(holder.itemView.getContext());
            // Update DB status: 1 for Done, 0 for Pending
            db.updateTaskStatus(task.getId(), isChecked ? 1 : 0);
        });

        // 4. Handle Row Click (Open Edit Mode)
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, AddTaskActivity.class);

            // Pass ALL data to the Edit Screen
            intent.putExtra("TASK_ID", task.getId());
            intent.putExtra("TITLE", task.getTitle());
            intent.putExtra("DESC", task.getDescription());
            intent.putExtra("ADDRESS", task.getAddress());
            intent.putExtra("LAT", task.getLatitude());
            intent.putExtra("LNG", task.getLongitude());

            // --- NEW FIELDS (Crucial for Edit Mode) ---
            intent.putExtra("RADIUS", task.getRadius());
            intent.putExtra("ALERT_TYPE", task.getAlertType());
            intent.putExtra("START_TIME", task.getStartTime());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    // Inner Class to hold View Elements
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle, textAddress;
        public CheckBox checkboxDone;

        public ViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAddress = itemView.findViewById(R.id.textAddress);
            checkboxDone = itemView.findViewById(R.id.checkboxDone);
        }
    }
}