package com.example.geotask;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private LinearLayout layoutEmpty; // The "No Tasks" view
    private DatabaseHelper dbHelper;
    private TaskAdapter adapter;
    private List<TaskModel> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. SETUP TOOLBAR (Because we use NoActionBar theme)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. CHECK PERMISSIONS & START SERVICE
        checkPermissions();
        startGeofenceService();

        // 3. INITIALIZE VIEWS
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        dbHelper = new DatabaseHelper(this);

        // 4. SETUP RECYCLERVIEW
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 5. SETUP FAB (Add Button)
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // 6. SETUP SWIPE-TO-DELETE (With Confirmation Dialog)
        setupSwipeToDelete();
    }

    // --- LIFECYCLE METHOD: Runs every time you come back to this screen ---
    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Refresh list to show new/edited tasks
    }

    private void loadTasks() {
        taskList = dbHelper.getAllTasks();
        adapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter);

        // Show/Hide Empty State Image
        checkEmptyState();
    }

    private void checkEmptyState() {
        if (taskList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag-and-drop moving
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TaskModel task = taskList.get(position);

                // SHOW ALERT DIALOG
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Task?")
                        .setMessage("Are you sure you want to delete '" + task.getTitle() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // 1. Delete from DB
                            dbHelper.deleteTask(task.getId());
                            // 2. Remove from List
                            taskList.remove(position);
                            adapter.notifyItemRemoved(position);
                            // 3. Update Empty View if needed
                            checkEmptyState();
                            Toast.makeText(MainActivity.this, "Task Deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // User cancelled, put the item back into the view
                            adapter.notifyItemChanged(position);
                        })
                        .setCancelable(false) // Prevents clicking outside to close
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    // --- MENU LOGIC (The Map Icon at top right) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_map_view) {
            Intent intent = new Intent(MainActivity.this, AllTasksMapActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- SYSTEM LOGIC (Permissions & Service) ---
    private void startGeofenceService() {
        Intent serviceIntent = new Intent(this, GeofenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS
            }, 1);
        }
    }
}