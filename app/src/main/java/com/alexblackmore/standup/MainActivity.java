package com.alexblackmore.standup;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String CHANNELTHING = "blargon";
    public static final int NOTIF_ID = 1337;
    public static final String TITLE_EXTRA = "com.alexblackmore.standup.TITLE";
    public static final String TEXT_EXTRA = "com.alexblackmore.standup.TEXT";
    NotificationManager notifMgr;
    NotificationChannel notifChnl;
    public EditText notifTitleET;
    public EditText notifTextET;
    Spinner repeatIntervalSpinner;
    Button setNotification1Btn;
    Button stopNotification1Btn;
    Button.OnClickListener setListener;
    Button.OnClickListener stopListener;
    String selectedIntervalStr;
    long selectedIntervalLong;
    PendingIntent notifyPI;
    AlertDialog.Builder myAlertBuilder;
    AlarmManager alarmManager;
    Intent notifyIntent;
    boolean alarmIsRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notifTitleET = findViewById(R.id.notifNameInputET);
        notifTextET = findViewById(R.id.notifTextInputET);
        repeatIntervalSpinner = findViewById(R.id.repeatIntervalSpinner);
        setNotification1Btn = findViewById(R.id.setButton);
        stopNotification1Btn = findViewById(R.id.stopButton);
        stopNotification1Btn.setEnabled(false);

        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(this, R.array.repeat_interval_array, android.R.layout.simple_spinner_item);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        repeatIntervalSpinner = findViewById(R.id.repeatIntervalSpinner);

        if (repeatIntervalSpinner != null) {
            repeatIntervalSpinner.setAdapter(myAdapter);
            repeatIntervalSpinner.setOnItemSelectedListener(this);
        }

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        setListener = new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                setNotification();
            }
        };

        stopListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifMgr.cancelAll();
                alarmManager.cancel(notifyPI);
                alarmIsRunning = false;
                generateStopDialog();
            }
        };

        setNotification1Btn.setOnClickListener(setListener);
        stopNotification1Btn.setOnClickListener(stopListener);

        createNotifChnl();
    }

    void createNotifChnl() {
        notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notifChnl = new NotificationChannel(CHANNELTHING, "Flarky", NotificationManager.IMPORTANCE_HIGH);
            notifMgr.createNotificationChannel(notifChnl);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedIntervalStr = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void generateConfirmationDialog() {
        myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        myAlertBuilder.setTitle("Reminder created");
        myAlertBuilder.setMessage(
                "You will be reminded of the following:"
                + System.getProperty ("line.separator")
                + System.getProperty ("line.separator")
                + notifTitleET.getText().toString()
                + System.getProperty ("line.separator")
                + notifTextET.getText().toString()
                + System.getProperty ("line.separator")
                + System.getProperty ("line.separator")
                + "in " + selectedIntervalStr + ".");
        myAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        myAlertBuilder.setNegativeButton("Cancel reminder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifMgr.cancelAll();
                alarmManager.cancel(notifyPI);
                alarmIsRunning = false;
                toggleActiveOrInactive();
                Toast.makeText(MainActivity.this, "Reminder cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
        myAlertBuilder.show();
    }

    public void generateStopDialog() {
        myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        myAlertBuilder.setTitle("Reminder stopped");
        myAlertBuilder.setMessage(
                "You have stopped the following reminder:"
                        + System.getProperty ("line.separator")
                        + System.getProperty ("line.separator")
                        + notifTitleET.getText().toString()
                        + System.getProperty ("line.separator")
                        + notifTextET.getText().toString()
                        + System.getProperty ("line.separator")
                        + System.getProperty ("line.separator"));
        myAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toggleActiveOrInactive();
            }
        });
        myAlertBuilder.show();
    }

    public void setNotification() {
        notifyIntent = new Intent(this, AlarmReceiver.class);
        notifyIntent.putExtra(Intent.EXTRA_TITLE, notifTitleET.getText().toString());
        notifyIntent.putExtra(Intent.EXTRA_TEXT, notifTextET.getText().toString());

        notifyPI = PendingIntent.getBroadcast(this, NOTIF_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        switch (selectedIntervalStr) {
            case "1 minute":
                selectedIntervalLong = 1000;
                break;
            case "15 minutes":
                selectedIntervalLong = 4 * 60 * 1000;
                break;
            case "30 minutes":
                selectedIntervalLong = 30 * 60 * 1000;
                break;
            case "60 minutes":
                selectedIntervalLong = 60 * 60 * 1000;
                break;
            default:
                selectedIntervalLong = 60 * 60 * 1000;
        }

        alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, (SystemClock.elapsedRealtime() + selectedIntervalLong), notifyPI);
        alarmIsRunning = true;
        generateConfirmationDialog();
        toggleActiveOrInactive();
    }

    public void toggleActiveOrInactive () {
        if (alarmIsRunning) {
            notifTitleET.setEnabled(false);
            notifTextET.setEnabled(false);
            repeatIntervalSpinner.setEnabled(false);
            setNotification1Btn.setEnabled(false);
            stopNotification1Btn.setEnabled(true);
        } else {
            notifTitleET.setEnabled(true);
            notifTextET.setEnabled(true);
            repeatIntervalSpinner.setEnabled(true);
            setNotification1Btn.setEnabled(true);
            stopNotification1Btn.setEnabled(false);
        }
    }
}
