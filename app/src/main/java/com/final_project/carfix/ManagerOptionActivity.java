package com.final_project.carfix;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ManagerOptionActivity extends AppCompatActivity {

    TextView number, type, year, code, name, address, phone, editcar;
    ImageView treatment_add, edit_client, history_tr, edit_prediction;
    DBHelper db = new DBHelper();
    User user = new User();

    LinearLayout register, notRegister;

    String clientId, exit_time;
    Boolean clientSign = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_option);

        register = (LinearLayout) findViewById(R.id.if_register);
        notRegister = (LinearLayout) findViewById(R.id.not_register);

        number = (TextView) findViewById(R.id.number);
        type = (TextView) findViewById(R.id.type);
        year = (TextView) findViewById(R.id.year);
        code = (TextView) findViewById(R.id.code);
        name = (TextView) findViewById(R.id.client_name);
        address = (TextView) findViewById(R.id.address);
        phone = (TextView) findViewById(R.id.phone);
        editcar = (TextView) findViewById(R.id.editcar);
        treatment_add = (ImageView) findViewById(R.id.treatment_add_manager);
        edit_client = (ImageView) findViewById(R.id.pencil);
        history_tr = (ImageView) findViewById(R.id.history_tr);
        edit_prediction = (ImageView) findViewById(R.id.edit_prediction);

        Intent i = getIntent();
        final String carNumber = i.getStringExtra("carNumber");

        checkTreatmentOpen(carNumber);

        //in this query i add the details of the client in the screen.
        Query q = db.getUserDetailFromCarNumber(carNumber);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    notRegister.setVisibility(View.GONE);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        user = ds.getValue(User.class);
                        clientId = ds.getKey();
                        number.setText(carNumber);
                        type.setText(user.getCar().getCarType());
                        year.setText(user.getCar().getCarYear());
                        code.setText(user.getCar().getCarCode());
                        name.setText(user.getClientDetails().getFirstName() + " " + user.getClientDetails().getLastName());
                        address.setText(user.getClientDetails().getAddress());
                        phone.setText(user.getClientDetails().getPhone());
                    }
                } else {
                    register.setVisibility(View.GONE);
                    editcar.setText("רכב מספר " + carNumber);
                    clientSign = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ManagerOptionActivity.this, "בעיית התקשרות לdb", Toast.LENGTH_SHORT).show();
            }
        });

        history_tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentHistoryActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "manager");
                i.putExtra("clientId", clientId);
                i.putExtra("vendor", type.getText().toString());
                i.putExtra("exit_time", exit_time);
                i.putExtra("vendor", type.getText().toString());
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
                i.putExtra("vendor", type.getText().toString());
                startActivity(i);
            }
        });
        edit_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clientSign) {
                    Toast.makeText(ManagerOptionActivity.this, "משתמש לא רשום במערכת - אין נתונים לערוך", Toast.LENGTH_SHORT).show();

                } else {
                    Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                    i.putExtra("carNumber", carNumber);
                    i.putExtra("action", "editClient");
                    i.putExtra("clientId", clientId);
                    startActivity(i);
                }
            }
        });

        edit_prediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exit_time != "") {
                    Intent i = new Intent(getApplicationContext(), EditExitTimeActivity.class);
                    i.putExtra("carNumber", carNumber);
                    i.putExtra("permission", "manager");
                    i.putExtra("clientId", clientId);
                    i.putExtra("exit_time", exit_time);
                    startActivity(i);
                } else
                    Toast.makeText(ManagerOptionActivity.this, "לרכב זה אין טיפולים פתוחים", Toast.LENGTH_LONG).show();
            }
        });


    }

    public void checkTreatmentOpen(String carNumber) {
        Query query1 = db.getTreatmentByCarNumber(carNumber);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Treatment treatment = ds.getValue(Treatment.class);
                        Date date2 = null;
                        try {
                            date2 = new SimpleDateFormat("yyy-MM-dd HH:mm").parse(treatment.getExitDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Date date1 = null;
                        try {
                            //now
                            date1 = new SimpleDateFormat("yyy-MM-dd HH:mm").parse(getCurrentTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (date1.getTime() < date2.getTime()) { //there is a treatment open for this car.
                            exit_time = treatment.getExitDate();
                            return;
                        }
                    }
                    exit_time = "";
                } else
                    exit_time = "";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String getCurrentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat dateformat = new SimpleDateFormat("yyy-MM-dd HH:mm");
        return dateformat.format(cal.getTime());
    }
}
