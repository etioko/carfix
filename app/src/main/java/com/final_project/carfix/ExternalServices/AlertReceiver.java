package com.final_project.carfix.ExternalServices;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.final_project.carfix.ClientActivity;
import com.final_project.carfix.R;
import com.final_project.carfix.logic.Treatment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class AlertReceiver extends BroadcastReceiver {

    private APIClient jsonAPI;
    private String ticket_id_string, startDate, exitDate, extra, care_c, vendor, earlier_entries_string, key;

    DBHelper db = new DBHelper();
    Treatment treatment;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(final Context context, final Intent intent) {

        Intent activityIntent = new Intent(context, ClientActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_ONE_SHOT);

        SharedPreferences shared = context.getSharedPreferences("preferences", MODE_PRIVATE);

        key = shared.getString("key", "");
        ticket_id_string = shared.getString("ticket_id", "");
        startDate = shared.getString("start", "");
        exitDate = shared.getString("exitDate", "");
        extra = shared.getString("extra", "");
        care_c = shared.getString("care_category", "");
        vendor = shared.getString("vendor", "");
        earlier_entries_string = shared.getString("earlier_entries", "");

        Integer ticket_id = Integer.parseInt(ticket_id_string);
        Integer earlier_entries = Integer.parseInt(earlier_entries_string);

        Retrofit retrofit = NetworkClient.getRetrofitClient();
        jsonAPI = retrofit.create(APIClient.class);

        PostRequest postRequest;
        if (vendor.compareTo("Ford") == 0 || vendor.compareTo("Mazda") == 0)
            postRequest = new PostRequest(ticket_id, startDate, extra, care_c, vendor, earlier_entries);
        else
            postRequest = new PostRequest(ticket_id, startDate, extra, care_c, "Other", earlier_entries);

        Call<PostResponse> call = jsonAPI.getPostRequest(postRequest);

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (!response.isSuccessful()) {
//                    exitDateButton.setText("code: " + response.code() + "\nbody: " + response.body());
                    return;
                }
                PostResponse postResponse = response.body();

                try {
                    SharedPreferences.Editor shared = context.getSharedPreferences("preferences", MODE_PRIVATE).edit();
                    String content = changeTime(startDate, postResponse.getPrediction()) + "";

                    //this is the db - exitDate
                    final Date d1 = new SimpleDateFormat("yyy-MM-dd HH:mm:ss").parse(exitDate);
                    //this is the new exitDate from the server
                    final Date d2 = new SimpleDateFormat("yyy-MM-dd HH:mm:ss").parse(content);

                    Calendar calDate1 = Calendar.getInstance();
                    calDate1.setTime(d1);

                    Calendar calDateBiggeer = Calendar.getInstance();
                    calDateBiggeer.setTime(d1);
                    calDateBiggeer.add(Calendar.MINUTE, 15);
                    Log.d("--CALdatebigger---", calDateBiggeer.getTime() + "");

                    Calendar calDateSmaller = Calendar.getInstance();
                    calDateSmaller.setTime(d1);
                    calDateSmaller.add(Calendar.MINUTE, -15);
                    Log.d("--CALdatesmaller---", calDateSmaller.getTime() + "");


                    Calendar calDate2 = Calendar.getInstance();
                    calDate2.setTime(d2);


                    Log.d("--calculation---", d1.getTime() + 15 * 60 * 1000 + "");
                    if (calDate2.after(calDateBiggeer) || calDate2.before(calDateSmaller)) {
                        //this is a more/less than 15 min.
                        Query q = db.reference().child("Treatments").orderByKey().equalTo(key);
                        q.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        treatment = ds.getValue(Treatment.class);
//                                        if (ds.getKey().equals(key)) {
                                        DateFormat date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                                        String date2Str = date.format(d2);
                                        String date1Str = date.format(d1);

                                        String ext = treatment.getExitDate();
                                        if (ext.equals(date1Str)) {
                                            treatment.setExitDate(d2.getTime() + "");
                                            db.reference().child("Treatments").child(key).child("exitDate").setValue(date2Str);
                                            showNotification(context, "עדכון", "עודכן זמן היציאה מהמוסך", intent);
                                        }

//                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {

            }
        });

    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.icon_app)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                notificationManager.notify(notificationId, mBuilder.build());

            }
        }, 1000);
    }

    public String changeTime(String startDate, Float adding) throws ParseException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        Date d = date.parse(startDate);
        cal.setTime(d);
        cal.add(Calendar.MINUTE, Math.round(adding));
        return date.format(cal.getTime());
    }
}
