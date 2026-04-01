package com.example.geotask;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    EditText editTitle, editDesc, editAddress;
    Button btnSave, btnMap, btnPickTime;
    TextView textHeader, textRadius, textTimePreview;
    SeekBar seekBarRadius;
    RadioGroup radioGroupType;
    DatabaseHelper dbHelper;

    double finalLat = 0.0;
    double finalLng = 0.0;
    int taskID = -1;
    boolean isEditMode = false;

    long selectedTimeInMillis = 0;
    int selectedRadius = 500;
    String selectedAlertType = "NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // 1. Initialize Views
        editTitle = findViewById(R.id.editTitle);
        editDesc = findViewById(R.id.editDesc);
        editAddress = findViewById(R.id.editAddress);
        btnSave = findViewById(R.id.btnSave);
        btnMap = findViewById(R.id.btnMap);
        btnPickTime = findViewById(R.id.btnPickTime);
        textHeader = findViewById(R.id.textHeader);
        textRadius = findViewById(R.id.textRadius);
        textTimePreview = findViewById(R.id.textTimePreview);
        seekBarRadius = findViewById(R.id.seekBarRadius);
        radioGroupType = findViewById(R.id.radioGroupType);

        dbHelper = new DatabaseHelper(this);

        // 2. Setup Listeners
        setupSeekBar();
        setupTimePicker();
        setupMapButton(); // Moved to own method to ensure it always loads
        setupSaveButton();

        // 3. Handle Edit Mode (Load Data)
        if (getIntent().hasExtra("TASK_ID")) {
            loadTaskData();
        }
    }

    private void setupSeekBar() {
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 100) progress = 100;
                selectedRadius = progress;
                textRadius.setText("Trigger Radius: " + selectedRadius + "m");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupMapButton() {
        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(AddTaskActivity.this, MapPickerActivity.class);
            // Only pass previous location if it is valid (not 0.0)
            if (finalLat != 0.0 && finalLng != 0.0) {
                intent.putExtra("PREVIOUS_LAT", finalLat);
                intent.putExtra("PREVIOUS_LNG", finalLng);
            }
            startActivityForResult(intent, 100);
        });
    }

    private void loadTaskData() {
        isEditMode = true;
        taskID = getIntent().getIntExtra("TASK_ID", -1);

        // Load Texts
        editTitle.setText(getIntent().getStringExtra("TITLE"));
        editDesc.setText(getIntent().getStringExtra("DESC"));
        editAddress.setText(getIntent().getStringExtra("ADDRESS"));
        finalLat = getIntent().getDoubleExtra("LAT", 0.0);
        finalLng = getIntent().getDoubleExtra("LNG", 0.0);

        // --- FIX 1: LOAD RADIUS ---
        int savedRadius = getIntent().getIntExtra("RADIUS", 500);
        if (savedRadius < 100) savedRadius = 500; // Safety default
        selectedRadius = savedRadius;
        seekBarRadius.setProgress(selectedRadius);
        textRadius.setText("Trigger Radius: " + selectedRadius + "m");

        // --- FIX 2: LOAD ALERT TYPE ---
        String savedType = getIntent().getStringExtra("ALERT_TYPE");
        if (savedType != null && savedType.equals("ALARM")) {
            radioGroupType.check(R.id.radioAlarm);
            selectedAlertType = "ALARM";
        } else {
            radioGroupType.check(R.id.radioNotify);
            selectedAlertType = "NOTIFICATION";
        }

        btnSave.setText("Update Task");
        textHeader.setText("Edit Task");
    }

    private void setupTimePicker() {
        btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {
                        Calendar setTime = Calendar.getInstance();
                        setTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        setTime.set(Calendar.MINUTE, minute);

                        if (setTime.getTimeInMillis() < System.currentTimeMillis()) {
                            setTime.add(Calendar.DAY_OF_YEAR, 1);
                        }
                        selectedTimeInMillis = setTime.getTimeInMillis();
                        textTimePreview.setText("Active after: " + hourOfDay + ":" + String.format("%02d", minute));
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        });
    }

    private void setupSaveButton() {
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String desc = editDesc.getText().toString();
            String addr = editAddress.getText().toString();

            if (radioGroupType.getCheckedRadioButtonId() == R.id.radioAlarm) {
                selectedAlertType = "ALARM";
            } else {
                selectedAlertType = "NOTIFICATION";
            }

            if (title.isEmpty()) {
                Toast.makeText(this, "Title Required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditMode) {
                dbHelper.updateTask(taskID, title, desc, addr, finalLat, finalLng, selectedRadius, selectedAlertType, selectedTimeInMillis);
                dbHelper.updateTaskStatus(taskID, 0);
                Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.addTask(title, desc, addr, finalLat, finalLng, selectedRadius, selectedAlertType, selectedTimeInMillis);
                Toast.makeText(this, "Task Saved!", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            editAddress.setText(data.getStringExtra("address"));
            finalLat = data.getDoubleExtra("latitude", 0.0);
            finalLng = data.getDoubleExtra("longitude", 0.0);
        }
    }
}