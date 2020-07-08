package com.final_project.carfix;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.final_project.carfix.ExternalServices.AlarmOnceReceiver;
import com.final_project.carfix.ExternalServices.AlertReceiver;
import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.logic.Treatment;
import com.final_project.carfix.logic.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ClientActivity extends AppCompatActivity {

    ImageView history, future_icon, contact, show_details;
    TextView title, finish_treat, finish_treat_hour;
    MutableLiveData<String> content;

    DBHelper db = new DBHelper();
    User user = new User();

    boolean flag = false;

    String exitDate, clientId, enterDate, carNumber, mileage, vendor, extra, care_category, key;

    public MutableLiveData<Integer> earlier_entries = new MutableLiveData<>();


    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        history = (ImageView) findViewById(R.id.history);
        future_icon = (ImageView) findViewById(R.id.future_func);
        title = (TextView) findViewById(R.id.title);
        finish_treat = (TextView) findViewById(R.id.arrivle_date);
        finish_treat_hour = (TextView) findViewById(R.id.arrivle_hour);
        contact = (ImageView) findViewById(R.id.contact);
        show_details = (ImageView) findViewById(R.id.show_details);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, AlertReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        Intent i = getIntent();
        carNumber = i.getStringExtra("carNumber");

        content = new MutableLiveData<String>();

//        Retrofit retrofit = NetworkClient.getRetrofitClient();
//        jsonAPI = retrofit.create(APIClient.class);

        //this query change the title to "hello" + client's name + carNumber.
        Query query = db.getUserDetailFromCarNumber(carNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        String name = user.getClientDetails().getFirstName();
                        clientId = ds.getKey();
                        title.setText("שלום " + name);
                    }
                }
            }

            @Override

            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentHistoryActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "client");
                i.putExtra("vendor", user.getCar().getCarType());
                i.putExtra("clientId", clientId);
                startActivity(i);
            }
        });
        future_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FutureArrivalActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("clientId", clientId);
                i.putExtra("vendor", user.getCar().getCarType());
                startActivity(i);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contact = new Intent(getApplicationContext(), ContactActivity.class);
                contact.putExtra("carNumber", carNumber);
                contact.putExtra("clientId", clientId);
                contact.putExtra("vendor", user.getCar().getCarType());
                startActivity(contact);
            }
        });

        show_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("action", "showClient");
                i.putExtra("permission", "client");
                i.putExtra("clientId", clientId);
                startActivity(i);
            }
        });

        //server = = = = = ticket_id, start,extra, care_category, vendor,earlier_entries;
        //this query check if there is a treatment with exit date in the future so he check the algorithm with the server.
        Query query1 = db.getTreatmentByCarNumber(carNumber);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Treatment treatment = ds.getValue(Treatment.class);
                        key = ds.getKey();
                        Date date1 = null;
                        try {
                            //now
                            date1 = new SimpleDateFormat("yyy-MM-dd HH:mm").parse(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date date2 = null;
                        try {
                            date2 = new SimpleDateFormat("yyy-MM-dd HH:mm").parse(treatment.getExitDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (date1.compareTo(date2) == -1) //there is a treatment open to this car.
                        {
                            String strDate = treatment.getExitDate();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date convertedDate = new Date();
                            String finalDateString = null;
                            try {
                                convertedDate = dateFormat.parse(strDate);
                                SimpleDateFormat sdfnewformat = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                                finalDateString = sdfnewformat.format(convertedDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //change the textView to exit time.
                            String[] dateStrings = finalDateString.split(" ");
                            String hours = dateStrings[0];
                            String date = dateStrings[1];
                            assert date2 != null;
                            if (date1.getDay() != date2.getDay()) {
                                finish_treat.setText("/nרכבך יהיה מוכן בתאריך" + date);
                                finish_treat_hour.setText(hours);
                            } else {
                                finish_treat.setText("רכבך יהיה מוכן בשעה");
                                finish_treat_hour.setText(hours);
                                setAlarmManagerToEndTime(finalDateString);
                            }
                            //if the manager doesnt change the time the question every 10 minutes starts.
                            if (treatment.getManagerChangeTime() == false)
                                flag = true;
                            else
                                cancelAlarm();
                        }

//                        now i need to check every 10 min if there is a change in exit time.
                        //the flag is true only when the manage change the time.
                        if (flag) {
                            enterDate = treatment.getEnterDate();
                            exitDate = treatment.getExitDate();
                            carNumber = treatment.getCarNumber();
                            extra = treatment.getExtra();
                            mileage = treatment.getTreatmentNeed();
                            vendor = treatment.getVendor();

                            startAlgorithm(date1, date2, key);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startAlgorithm(final Date currDate, final Date exitDate, final String key) {
        //Check how many cars are in the garage that came before me and their treatment ends before me.
        getGarageStatus();

        earlier_entries.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newName) {
                if (Integer.parseInt(mileage) <= 3000)
                    setAlarmEveryTenMin(key, carNumber, enterDate, ClientActivity.this.exitDate, extra, "A", vendor, newName);
                else
                    setAlarmEveryTenMin(key, carNumber, enterDate, ClientActivity.this.exitDate, extra, "B", vendor, newName);

                if (currDate.compareTo(exitDate) == 0 || currDate.compareTo(exitDate) == 1) {
                    cancelAlarm();
                }
            }
        });
    }

    public String changeTime(String startDate, Float adding) throws ParseException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        Date d = date.parse(startDate);
        cal.setTime(d);
        cal.add(Calendar.MINUTE, Math.round(adding));
        return date.format(cal.getTime());
    }

    public static String getCurrentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat dateformat = new SimpleDateFormat("yyy-MM-dd HH:mm");
        return dateformat.format(cal.getTime());
    }

    private void setAlarmEveryTenMin(String key, String ticket_id, String startDate, String exitDate, String extra, String care_category, String vendor, int earlier_entries) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        SharedPreferences.Editor shared = getSharedPreferences("preferences", MODE_PRIVATE).edit();
        Intent intent = new Intent(this, AlertReceiver.class);
        shared.putString("key", key);
        shared.putString("ticket_id", ticket_id);
        shared.putString("start", startDate);
        shared.putString("exitDate", exitDate);
        shared.putString("extra", extra);
        shared.putString("care_category", care_category);
        shared.putString("vendor", vendor);
        shared.putString("earlier_entries", earlier_entries + "");
        shared.commit();

//        sendBroadcast(intent);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 60 * 10, pendingIntent);
    }

    public void cancelAlarm() {
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

    public void getGarageStatus() {
        //current time
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        final Date currentLocalTime = cal.getTime();

        Query query = db.reference().child("Treatments").orderByChild("startDate");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int counter_int = 0;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Treatment treatment = dataSnapshot1.getValue(Treatment.class);
                        if (parseStringToDate(treatment.getEnterDate()).compareTo(currentLocalTime) == -1 && parseStringToDate(treatment.getExitDate()).compareTo(currentLocalTime) == 1)//if there is a treat that start before min
                        {
                            counter_int += 1;
                        }
                    }
                    earlier_entries.setValue(counter_int - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Date parseStringToDate(String stringDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date1 = null;
        try {
            date1 = format.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setAlarmManagerToEndTime(String exitDate) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        Intent intent = new Intent(this, AlarmOnceReceiver.class);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(exitDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        calendar.set(Calendar.YEAR, date.getYear());
//        calendar.set(Calendar.MONTH, date.getMonth());
//        calendar.set(Calendar.DAY_OF_WEEK, date.getDay());
//        calendar.set(Calendar.HOUR, date.getHours());
//        calendar.set(Calendar.MINUTE, date.getMinutes());

        calendar.setTime(date);
        calendar.set(Calendar.SECOND, 0);
        Log.d("---set calendr---", calendar.getTime() + "");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

}

