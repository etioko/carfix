package com.final_project.carfix;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.logic.Treatment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class StatusActivity extends AppCompatActivity {

    ListView listView;
    ImageView treatment_add, editClient, history_clicked, future_icon, contact, edit_exit;
    ArrayList<String> statusList;
    ArrayAdapter<String> arrayAdapter;

    DBHelper db = new DBHelper();
    Treatment treatment = new Treatment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        listView = (ListView) findViewById(R.id.listStatus);

        statusList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.treatment_history, R.id.treathistory, statusList);

        Query q = db.reference().child("Treatments");
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot treats : dataSnapshot.getChildren()) {
                        treatment = treats.getValue(Treatment.class);
                        Date date1 = null;
                        try {
                            //now
                            date1 = new SimpleDateFormat("yyy-MM-dd HH:mm").parse(ClientActivity.getCurrentTime());
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
                            try {
                                statusList.add("מספר רכב: " + treatment.getCarNumber() + "\nזמן יציאה משוער "+ TreatmentHistoryActivity.changeFormat(treatment.getExitDate()));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else {
                    Toast.makeText(StatusActivity.this, "אין רכבים עם טיפול פעיל במוסך", Toast.LENGTH_SHORT).show();
                }
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
