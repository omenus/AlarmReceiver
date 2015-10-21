package com.example.user.alarmreceiver;

import android.app.AlarmManager;

import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;


import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    String phone1 = "";
    String phone2 = "";

    Integer hour1;
    Integer hour2;

    {
        hour2 = 8;
        hour1 = 19;
    }

    TextView phoneInput1;
    TextView phoneInput2;

    TextView hourInput1;
    TextView hourInput2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

//        TelephonyManager tMgr;
//        tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        String mPhoneNumber = tMgr.getLine1Number();
//
//        Toast.makeText(this, "Nr: " + mPhoneNumber, Toast.LENGTH_SHORT).show();

        phoneInput1 = (TextView)findViewById(R.id.phone1);
        phoneInput2 = (TextView)findViewById(R.id.phone2);

        hourInput1 = (TextView)findViewById(R.id.editTextHour1);
        hourInput2 = (TextView)findViewById(R.id.editTextHour2);

        loadData();
    }

    /**
     * Uruchomienie mechanizmu przekierowan
     * @param view
     */
    public void startAlarm(View view) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Toast.makeText(this, "Ustawienie alarmów od godziny " + hour, Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
//AlarmManager.INTERVAL_HOUR
        manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000*3600, pendingIntent);
    }

    public void cancelAlarm(View view) {

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        if (manager != null) {
            manager.cancel(pendingIntent);
            Toast.makeText(this, "Wyłączenie mechanizmu przekierowań", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nie ma co anulować", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Wylaczenie aktualnego przekierowania
     * @param view
     */
    public void cancelActualRedirect(View view) {

        Intent alarmIntent2 = new Intent(this, AlarmReceiver.class);
        alarmIntent2.putExtra("VALUE", 2);

        sendBroadcast(alarmIntent2);
    }
    @Override
    public void onClick(View arg0) {
//        if(arg0.getId() == R.id.button_create){
//            //define a new Intent for the second Activity
//            Intent intent = new Intent(this, CreateActivity.class);
//
//            //start the second Activity
//            this.startActivity(intent);
//        }
    }

    private void saveData() {

        phone1 = phoneInput1.getText().toString();
        phone2 = phoneInput2.getText().toString();


        hour1 = Integer.parseInt(hourInput1.getText().toString());
        hour2 = Integer.parseInt(hourInput2.getText().toString());

        SharedPreferences sp =
                getSharedPreferences("MyPrefs",
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(phone1 != "") {
            editor.putString("phone1", phone1);
        }
        if(phone2 != "") {
            editor.putString("phone2", phone2);
        }

        editor.putInt("hour1", hour1);

        editor.putInt("hour2", hour2);

        editor.commit();
        Toast.makeText(this, "Zapisane " + phone1 + " i " + phone2, Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        SharedPreferences sp =
                getSharedPreferences("MyPrefs",
                        Context.MODE_PRIVATE);
        phone1 = sp.getString("phone1", phone1);
        phone2 = sp.getString("phone2", phone2);

        hour1 = sp.getInt("hour1", hour1);
        hour2 = sp.getInt("hour2", hour2);

        phoneInput1.setText(phone1.toString());
        phoneInput2.setText(phone2.toString());



        hourInput1.setText(hour1.toString());
        hourInput2.setText(hour2.toString());

        Toast.makeText(this, "Pobrano ustawienia", Toast.LENGTH_SHORT).show();
    }


    public void onButtonSaveClick(View arg0) {
        saveData();
    }

    public void onButtonCheckRedirectsClick(View view) {
        //define ona new Intent for the second Activity
        Intent alarmIntent2 = new Intent(this, AlarmReceiver.class);
        alarmIntent2.putExtra("VALUE", 1);
        sendBroadcast(alarmIntent2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

}
