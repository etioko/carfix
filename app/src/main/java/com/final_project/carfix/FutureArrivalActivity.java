package com.final_project.carfix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.APIClient;
import com.final_project.carfix.ExternalServices.NetworkClient;
import com.final_project.carfix.ExternalServices.PostRequest;
import com.final_project.carfix.ExternalServices.PostResponse;

import java.lang.reflect.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FutureArrivalActivity extends AppCompatActivity {
    Button check;
    Spinner spinner_date, treatment_kind_spinner;
    EditText hours_enter, minutes_enter;
    TextView exit_future_date, text_exit;
    ImageView history, contact, show_details;
    LinearLayout layout;

    private String getHour, getMinutes, extra;
    private APIClient jsonAPI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_future_arrival);
        Retrofit retrofit = NetworkClient.getRetrofitClient();

        jsonAPI = retrofit.create(APIClient.class);

        Intent i = getIntent();
        final String clientId = i.getStringExtra("clientId");
        final String carNumber = i.getStringExtra("carNumber");
        final String vendor = i.getStringExtra("vendor");

        check = (Button) findViewById(R.id.check);
        hours_enter = (EditText) findViewById(R.id.hours_enter);
        minutes_enter = (EditText) findViewById(R.id.minutes_enter);
        spinner_date = (Spinner) findViewById(R.id.spinner_date);
        treatment_kind_spinner = (Spinner) findViewById(R.id.treatment_kind_spinner);
        exit_future_date = (TextView) findViewById(R.id.exit_future_date);
        history = (ImageView) findViewById(R.id.history);
        contact = (ImageView) findViewById(R.id.contact);
        show_details = (ImageView) findViewById(R.id.show_details);
        text_exit = (TextView) findViewById(R.id.text_exit);
        layout = (LinearLayout) findViewById(R.id.layout);
        layout.setVisibility(View.GONE);
        text_exit.setVisibility(View.GONE);

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentHistoryActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "client");
                i.putExtra("clientId", clientId);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ContactActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "client");
                i.putExtra("clientId", clientId);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });

        show_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "client");
                i.putExtra("action", "showClient");
                startActivity(i);
            }
        });

        //this is the spinner with the dates - next two weeks!!
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        final DateFormat date = new SimpleDateFormat("dd/MM/yyy");
        String localTime = date.format(currentLocalTime);

        List<String> dates = new ArrayList<String>();

        for (int index = 0; index < 14; index++) {
//            cal.add(Calendar.DATE, -1);
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

        //spinner of the treatment
        ArrayAdapter<CharSequence> adapterModel = ArrayAdapter.createFromResource(this, R.array.treatArray, R.layout.spinner_fonts);
        adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        treatment_kind_spinner.setAdapter(adapterModel);
        treatment_kind_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                ((TextView) parent.getChildAt(0)).setTextSize(15);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String[] splitString = spinner_date.getSelectedItem().toString().split(",");
                String date = splitString[0].trim();
                String day = splitString[1].trim();

                String hour, minutes, timeStr = null;
                hour = hours_enter.getText().toString();
                minutes = minutes_enter.getText().toString();

                if (hour.equals("") || minutes.equals(""))
                    Toast.makeText(FutureArrivalActivity.this, "שעה אינה תקינה", Toast.LENGTH_LONG).show();
                else {
                    if (minutes.equals("00"))
                        timeStr = "00";
                    else
                        timeStr = minutes + "";

                    if (Integer.parseInt(hour) < 10 && hour.length() == 1)
                        timeStr = "0" + Integer.parseInt(hour) + ":" + timeStr + ":00";
                    else
                        timeStr = Integer.parseInt(hour) + ":" + timeStr + ":00";

                    LocalTime endTimeInNormalDay = LocalTime.parse("14:00:00");
                    LocalTime endTimeInFriday = LocalTime.parse("12:00:00");
                    LocalTime timeChosen = LocalTime.parse(timeStr);

                    if (day.equals("") || timeChosen.equals(null) || Integer.parseInt(minutes) > 59)
                        Toast.makeText(FutureArrivalActivity.this, "השעה שהזנת לא תקינה", Toast.LENGTH_SHORT).show();
                    else {
                        if (day.equals("שישי") && timeChosen.isAfter(endTimeInFriday)) {
                            Toast.makeText(FutureArrivalActivity.this, " ביום שישי אין קבלת רכבים לאחר 12:00", Toast.LENGTH_SHORT).show();
                        } else {
                            if (timeChosen.isAfter(endTimeInNormalDay)) {
                                Toast.makeText(FutureArrivalActivity.this, "אין קבלת רכבים לאחר 14:00", Toast.LENGTH_SHORT).show();
                            } else {
                                String treatment = treatment_kind_spinner.getSelectedItem().toString();
                                if (treatment.equals("טסט")) {
                                    extra = "B";
                                } else if (treatment.equals("תקלה"))
                                    extra = "C";
                                else if (treatment.equals("טיפול קודם שהתפספס"))
                                    extra = "A";
                                else
                                    extra = "N";

                                SimpleDateFormat newDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date myDate = null;
                                try {
                                    myDate = newDateFormat.parse(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                newDateFormat.applyPattern("yyyy-MM-dd");
                                String myDateString = newDateFormat.format(myDate) + " " + timeChosen + ":00";
                                layout.setVisibility(View.VISIBLE);

                                checkFutureArrival(Integer.parseInt(clientId), myDateString, extra, "A", vendor, 10);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkFutureArrival(int ticket_id, final String start, String extra, String care_category, String vendor, int earlier_entries) {
        PostRequest postRequest;
        if (vendor.equals("Ford") || vendor.equals("Mazda"))
            postRequest = new PostRequest(ticket_id, start, extra, care_category, vendor, earlier_entries);
        else
            postRequest = new PostRequest(ticket_id, start, extra, care_category, "Other", earlier_entries);

        Call<PostResponse> call = jsonAPI.getPostRequest(postRequest);

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (!response.isSuccessful()) {
                    exit_future_date.setText("התקשרות עם השרת נכשלה");
                    return;
                }
                PostResponse postResponse = response.body();
                //i need to change this in future.
                String content = "";
                try {
                    Log.d("post", "---------------------" + postResponse.getPrediction() + "---------------------");
                    content += changeTime(start, postResponse.getPrediction());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    text_exit.setVisibility(View.VISIBLE);
                    exit_future_date.setText(TreatmentHistoryActivity.changeFormat(content));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(FutureArrivalActivity.this, "התקשרות לשרת נכשלה!", Toast.LENGTH_LONG).show();

            }
        });

    }

    @SuppressLint("LongLogTag")
    public String changeTime(String startDate, Float adding) throws ParseException {
        Calendar cal = Calendar.getInstance();
        DateFormat date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        Date d = date.parse(startDate);
        cal.setTime(d);
        cal.add(Calendar.MINUTE, Math.round(adding));
        Log.d("------cal time-------", date.format(cal.getTime()));

        Calendar closed = Calendar.getInstance();

        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            //the grage closed.
            closed.setTime(d);
            closed.set(Calendar.HOUR_OF_DAY, 12);
            closed.set(Calendar.MINUTE, 00);
            long milliseconds1 = cal.getTimeInMillis();
            long milliseconds2 = closed.getTimeInMillis();
            long diff = milliseconds1 - milliseconds2;

            if (cal.getTime().after(closed.getTime())) {
                cal.add(Calendar.DATE, 2);
                cal.set(Calendar.HOUR_OF_DAY, 7);
                cal.set(Calendar.MINUTE, 30);
                cal.add(Calendar.MILLISECOND, Math.round(diff));
            }
        } else {
            //the grage closed.
            closed.setTime(d);
            closed.set(Calendar.HOUR_OF_DAY, 16);
            closed.set(Calendar.MINUTE, 30);
            long milliseconds1 = cal.getTimeInMillis();
            long milliseconds2 = closed.getTimeInMillis();
            long diff = milliseconds1 - milliseconds2;

            if (cal.getTime().after(closed.getTime())) {
                cal.add(Calendar.DATE, 1);
                cal.set(Calendar.HOUR_OF_DAY, 7);
                cal.set(Calendar.MINUTE, 30);
                cal.add(Calendar.MILLISECOND, Math.round(diff));
            }

        }
        Log.d("------after change-------", date.format(cal.getTime()));
        return date.format(cal.getTime());

    }
}

