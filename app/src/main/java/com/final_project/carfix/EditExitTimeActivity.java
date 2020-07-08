package com.final_project.carfix;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.logic.Treatment;
import com.final_project.carfix.logic.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EditExitTimeActivity extends AppCompatActivity {

    TextView car_number, exit_date;
    ImageView treatment_add, edit_client, history_tr;
    EditText hours_enter, minutes_enter;
    Spinner spinner_date;
    Button update;


    DBHelper db = new DBHelper();
    Treatment treatment = new Treatment();
    User user = new User();
    String clientId, permission, carNumber, exit_time, vendor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_exit_time_activity);

        treatment_add = (ImageView) findViewById(R.id.treatment_add_manager);
        edit_client = (ImageView) findViewById(R.id.pencil);
        history_tr = (ImageView) findViewById(R.id.history_tr);
        car_number = (TextView) findViewById(R.id.car_number);
        exit_date = (TextView) findViewById(R.id.exit_date);
        hours_enter = (EditText) findViewById(R.id.hours_enter);
        minutes_enter = (EditText) findViewById(R.id.minutes_enter);
        spinner_date = (Spinner) findViewById(R.id.spinner_date);
        update = (Button) findViewById(R.id.update);

        fillSpinner();

        Intent i = getIntent();
        clientId = i.getStringExtra("clientId");
        permission = i.getStringExtra("permission");
        carNumber = i.getStringExtra("carNumber");
        exit_time = i.getStringExtra("exit_time");
        vendor = i.getStringExtra("vendor");

        try {
            exit_date.setText(TreatmentHistoryActivity.changeFormat(exit_time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        car_number.setText(carNumber);

        edit_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("action", "editClient");
                i.putExtra("clientId", clientId);
                startActivity(i);
            }
        });

        treatment_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "manager");
                i.putExtra("action", "addTreatment");
                i.putExtra("image", "");
                i.putExtra("clientId", clientId);
                i.putExtra("exit_time", exit_time);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });
        history_tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentHistoryActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "manager");
                i.putExtra("clientId", clientId);
                i.putExtra("exit_time", exit_time);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String hour, minutes, timeStr;
                hour = hours_enter.getText().toString();
                minutes = minutes_enter.getText().toString();

                if (minutes.equals("00"))
                    timeStr = Integer.parseInt(hour) + ":00";
                else
                    timeStr = Integer.parseInt(hour) + ":" + Integer.parseInt(minutes);

                String[] selectedDate = spinner_date.getSelectedItem().toString().split(",");
                String date = selectedDate[0].trim();
                String day = selectedDate[1].trim();

                LocalTime timeChosen = LocalTime.parse(timeStr);
                LocalTime endTimeInNormalDay = LocalTime.parse("16:30:00");
                LocalTime endTimeInFriday = LocalTime.parse("12:00:00");


                if (timeChosen.equals("") || timeChosen.equals(null))
                    Toast.makeText(EditExitTimeActivity.this, "השעה שהזנת לא תקינה", Toast.LENGTH_SHORT).show();
                else {
                    if (Integer.parseInt(minutes) > 59) {
                        Toast.makeText(EditExitTimeActivity.this, "השעה שהזנת לא תקינה", Toast.LENGTH_SHORT).show();
                    } else {
                        if (day.equals("שישי") && timeChosen.isAfter(endTimeInFriday))
                            Toast.makeText(EditExitTimeActivity.this, "אין קבלת קהל בשישי לאחר 14:00", Toast.LENGTH_SHORT).show();
                        else {
                            if (timeChosen.isAfter(endTimeInNormalDay)) {
                                Toast.makeText(EditExitTimeActivity.this, "אין קבלת קהל לאחר 16:30", Toast.LENGTH_SHORT).show();
                            } else {
                                String[] splitString = spinner_date.getSelectedItem().toString().split(",");

                                SimpleDateFormat newDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date myDate = null;
                                try {
                                    myDate = newDateFormat.parse(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                newDateFormat.applyPattern("yyy-MM-dd");
                                String myDateString = newDateFormat.format(myDate) + " " + timeChosen + ":00";

                                updateEndDate(exit_time, myDateString);
                            }
                        }
                    }

                }
            }
        });
    }

    public void updateEndDate(final String exit_time, final String myDateString) {
        Query q = db.getTreatmentByCarNumber(carNumber);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        treatment = ds.getValue(Treatment.class);
                        String key = ds.getKey();
                        String ext = treatment.getExitDate();
                        if (treatment.getExitDate().equals(exit_time)) {
                            treatment.setExitDate(myDateString);
                            treatment.setManagerChangeTime(true);
                            db.reference().child("Treatments").child(key).child("exitDate").setValue(myDateString);
                            db.reference().child("Treatments").child(key).child("managerChangeTime").setValue(true);
                            Toast.makeText(EditExitTimeActivity.this, "עודכן בהצלחה", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(EditExitTimeActivity.this, ManagerOptionActivity.class);
                            intent.putExtra("carNumber", carNumber);
                            startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fillSpinner() {
        //this is the spinner with the dates - next two weeks!!
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Date currentLocalTime = cal.getTime();
        final DateFormat date = new SimpleDateFormat("dd/MM/yyy");
        String localTime = date.format(currentLocalTime);

        List<String> dates = new ArrayList<String>();

        for (int index = 0; index < 14; index++) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.FRIDAY)
                cal.add(Calendar.DATE, 2);
            else
                cal.add(Calendar.DATE, 1);
            localTime = date.format(cal.getTime());
            int day = cal.get(Calendar.DAY_OF_WEEK);
            switch (day) {
                case 1:
                    localTime += ", ראשון ";
                    break;
                case 2:
                    localTime += ", שני ";
                    break;
                case 3:
                    localTime += ", שלישי ";
                    break;
                case 4:
                    localTime += ", רביעי ";
                    break;
                case 5:
                    localTime += ", חמישי ";
                    break;
                case 6:
                    localTime += ", שישי ";
                    break;
            }
            dates.add(localTime);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_fonts, dates);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_date.setAdapter(spinnerAdapter);
        spinner_date.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) parent.getChildAt(0)).setTextSize(15);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat dateformat = new SimpleDateFormat("yyy-MM-dd HH:mm");
        return dateformat.format(cal.getTime());
    }
}
