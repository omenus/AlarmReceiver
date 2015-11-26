package com.example.user.alarmreceiver;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.style.TtsSpan;
import android.util.Log;
import android.widget.DialerFilter;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = BroadcastReceiver.class.getSimpleName();
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private Integer index = 0;

    String encodedAster = "*";
    String encodedHash = Uri.encode("#");
    String prefixNr = "21";

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        // For our recurring task, we'll just display a message
        Integer message = arg1.getIntExtra("VALUE", 0);
        Toast.makeText(arg0, "Uruchomienie " + message, Toast.LENGTH_SHORT).show();

        if(message == 1){
            Toast.makeText(arg0, "Sprawdzenie stanu przekierowań", Toast.LENGTH_SHORT).show();
            callPhone(arg0, arg1, checkRedirectCommand());
        } else if(message == 2) {
            Toast.makeText(arg0, "Anulowanie aktualnego przekierowania", Toast.LENGTH_SHORT).show();
            callPhone(arg0, arg1, removeRedirectCommand());
        } else if(message == 3){
            Toast.makeText(arg0, "Wyłączenie mechanizmu przekierowań", Toast.LENGTH_SHORT).show();
        } else {
            //callPhone(arg0, arg1, getPhoneCommand());
            Toast.makeText(arg0, "Wywolanie sprawdzenia przekierowań", Toast.LENGTH_SHORT).show();
            prepareRedirect(arg0, arg1);
        }
    }

    /**
     * Wyświetla notywikacje w menu dropdown
     * @param context
     * @param intent
     */
    private void buildNotification(Context context, Intent intent, String title, String subtitle) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_stat_action_history)
                        .setContentTitle(title)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentText(subtitle);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(index++, mBuilder.build());
    }

    /**
     * dzwoni pod wybrany numer lub wykonuje kod MMI
     * @param context
     * @param intent
     */
    public void callPhone(Context context, Intent intent, String mmi) {
        Toast.makeText(context, "Nr: " + mmi.toString(), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Nr: " + mmi.toString());
        try {

            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+mmi));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                context.startActivity(callIntent);

                Log.d("wywolanie zdarzenia mmi", "Uruchomienie");
                return;
            } else {
                Log.d("myphone dialer", "brak dostepu");
            }

        } catch (ActivityNotFoundException activityExceptiona) {
            Log.e("myphone dialer", "Call failed");
        }
    }

    /**
     * anulowanie przekierowania
     * ##21#
     * @return
     */
    public String removeRedirectCommand(){
        String mmi = encodedHash+encodedHash + prefixNr + encodedHash;
        return mmi;
    }

//*#21# - sprawdzenie stanu
    public String checkRedirectCommand(){
        String mmi = encodedAster+encodedHash + prefixNr + encodedHash;
        return mmi;
    }


    public String prepareRedirectCommand(String phone){

        String mmi = new StringBuilder(encodedAster)
                .append(encodedAster).append(prefixNr).append(encodedAster)
                .append(Uri.encode("+48")).append(phone).append(encodedHash).toString();

        Log.d(TAG,"In prepare redicre " + phone);
        Log.d(TAG," " + mmi);
        return mmi;
    }


    /**
     * Przygotowanie danych dla redirectu
     * @param context
     * @param intent
     */
    private void prepareRedirect(Context context, Intent intent ){
        //sprawdzmy jaki mamy dzien, w weekend nic nie robimy
        String phone1="",
                phone2="";
        Integer hour1=0,
                hour2=0;
        String mmiCode;
        String zero = "0";
        Calendar calendar = Calendar.getInstance();
        //aktualny dzien tygodnia i godzina
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        // godzina 1  ustawien
        SharedPreferences sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phone1 = sp.getString("phone1", "");
        phone2 = sp.getString("phone2", "");

        hour1 = sp.getInt("hour1", 0);
        hour2 = sp.getInt("hour2", 0);

        Log.d(TAG, phone1);
        Log.d(TAG, "int" + hour1 + " " + hour);
        Log.d(TAG, phone2);
        Log.d(TAG, "int" + hour2);

        // If current day is Sunday, day=1. Saturday, day=7.
        if(day != 1 && day != 7){
            //ok zrobmy cos
            if(hour == hour1 && "" != phone1 && zero != phone1){
                Log.d(TAG, phone1);
                mmiCode =  prepareRedirectCommand(phone1);
                Log.d(TAG, "---------------------");
                Log.d(TAG, mmiCode);
                callPhone(context, intent, mmiCode);

                buildNotification(context, intent,"Przekierowanie na nr:" + phone1, "Godzina: " + hour + " kod: " + mmiCode );
            }else  if(hour == hour1 && phone1.equals(zero)){
                //usun aktualne przekierowanie
                mmiCode = removeRedirectCommand();
                callPhone(context, intent, mmiCode);
                buildNotification(context, intent, "Usunięcie przekierowania", "Godzina: " + hour + " kod: " + mmiCode);
            }
            if(hour == hour2 && "" != phone2 && !phone2.equals(zero)){
                mmiCode = prepareRedirectCommand(phone2);
                callPhone(context, intent, mmiCode);
                buildNotification(context, intent, "Przekierowanie na nr: " + phone2, "Godzina: " + hour + " kod: " + mmiCode);
            } else if(hour == hour2 && phone2.equals(zero)){
                //usun aktualne przekierowanie
                mmiCode = removeRedirectCommand();
                callPhone(context, intent, mmiCode);
                buildNotification(context, intent, "Usunięcie przekierowania", "Godzina: " + hour + " kod: " + mmiCode);
            }
        }
    }

}