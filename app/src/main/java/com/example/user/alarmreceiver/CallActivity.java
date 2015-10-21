package com.example.user.alarmreceiver;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

public class CallActivity extends Activity {
    String phone = "";

    public void onReceive(Context arg0, Intent arg1)
    {
        callPhone(arg0, arg1);
    }

    public void callPhone(Context context, Intent intent) {
        try {
            String phone = "##21#";
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                context.startActivity(callIntent);
                return;
            } else {
                Log.e("myphone dialer", "brak dostepu");
            }

        } catch (ActivityNotFoundException ctivityExceptiona) {
            Log.e("myphone dialer", "Call failed");
        }
    }
}