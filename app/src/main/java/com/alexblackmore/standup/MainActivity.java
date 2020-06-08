package com.alexblackmore.standup;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notifTitleET = findViewById(R.id.notifNameInputET);
        notifTextET = findViewById(R.id.notifTextInputET);
        repeatIntervalSpinner = findViewById(R.id.repeatIntervalSpinner);
        setNotification1Btn = findViewById(R.id.setButton);
        stopNotification1Btn = findViewById(R.id.stopButton);

        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(this, R.array.repeat_interval_array, android.R.layout.simple_spinner_item);
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        repeatIntervalSpinner = findViewById(R.id.repeatIntervalSpinner);

        if (repeatIntervalSpinner != null) {
            repeatIntervalSpinner.setAdapter(myAdapter);
            repeatIntervalSpinner.setOnItemSelectedListener(this);
        }

        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        setListener = new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent notifyIntent = new Intent(v.getContext(), AlarmReceiver.class);
                notifyIntent.putExtra(Intent.EXTRA_TITLE, notifTitleET.getText().toString());
                notifyIntent.putExtra(Intent.EXTRA_TEXT, notifTextET.getText().toString());

                notifyPI = PendingIntent.getBroadcast(v.getContext(), NOTIF_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                        Toast.makeText(MainActivity.this, "default clause reached", Toast.LENGTH_SHORT).show();
                }

                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, (SystemClock.elapsedRealtime() + selectedIntervalLong), selectedIntervalLong, notifyPI);
                Toast.makeText(MainActivity.this, "started", Toast.LENGTH_SHORT).show();
            }
        };

        stopListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifMgr.cancelAll();
                alarmManager.cancel(notifyPI);
                Toast.makeText(MainActivity.this, "stopped", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, selectedIntervalStr , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String passTitleToAlarm () {
        return notifTitleET.getText().toString();
    }
}
