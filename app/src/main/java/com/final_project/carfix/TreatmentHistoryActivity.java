package com.final_project.carfix;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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

public class TreatmentHistoryActivity extends AppCompatActivity {

    DBHelper db = new DBHelper();

    ListView listView;
    ImageView treatment_add, editClient, history_clicked, future_icon, contact, edit_exit, show_client;
    TextView title_history, car_number;
    ArrayList<String> treatmentsList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> helper;

    Treatment treatment = new Treatment();

    String carNumber, permission, vendor, clientId, exit_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_history);

        treatment_add = (ImageView) findViewById(R.id.treatmet_add);
        editClient = (ImageView) findViewById(R.id.pencil);
        history_clicked = (ImageView) findViewById(R.id.history_clicked);
        future_icon = (ImageView) findViewById(R.id.future_icon);
        contact = (ImageView)findViewById(R.id.contact);
        edit_exit = (ImageView) findViewById(R.id.edit_exit);
        show_client = (ImageView)findViewById(R.id.show_details);

        listView = (ListView) findViewById(R.id.listTreatments);
        title_history = (TextView) findViewById(R.id.title_history);
        title_history = (TextView) findViewById(R.id.title_history);
        car_number = (TextView) findViewById(R.id.car_number);

        treatmentsList = new ArrayList<>();
        helper = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.treatment_history, R.id.treathistory, treatmentsList);

        Intent i = getIntent();
        carNumber = i.getStringExtra("carNumber");
        permission = i.getStringExtra("permission");
        vendor = i.getStringExtra("vendor");
        clientId = i.getStringExtra("clientId");
        exit_time = i.getStringExtra("exit_time");

        title_history.setText("היסטוריית \nטיפולים");
        car_number.setText(carNumber);

        if (permission.equals("client")) {
            treatment_add.setVisibility(View.GONE);
            editClient.setVisibility(View.GONE);
            edit_exit.setVisibility(View.GONE);
        } else {
            future_icon.setVisibility(View.GONE);
            contact.setVisibility(View.GONE);
            show_client.setVisibility(View.GONE);
        }

        //this is the client buttons.
        show_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("action", "showClient");
                startActivity(i);
            }
        });
        future_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FutureArrivalActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ContactActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });
        //this is the manager buttons.
        treatment_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TreatmentActivity.class);
                intent.putExtra("carNumber", carNumber);
                intent.putExtra("permission", "manager");
                intent.putExtra("action", "addTreatment");
                intent.putExtra("clientId", clientId);
                intent.putExtra("image", "");
                intent.putExtra("exit_time", exit_time);
                intent.putExtra("vendor", vendor);
                startActivity(intent);
            }
        });


        editClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("action", "editClient");
                i.putExtra("clientId", clientId);
                startActivity(i);
            }
        });
        edit_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!exit_time.equals("") && exit_time!=null) {
                    Intent i = new Intent(getApplicationContext(), EditExitTimeActivity.class);
                    i.putExtra("carNumber", carNumber);
                    i.putExtra("permission", "manager");
                    i.putExtra("clientId", clientId);
                    i.putExtra("exit_time", exit_time);
                    i.putExtra("vendor", vendor);
                    startActivity(i);
                } else
                    Toast.makeText(TreatmentHistoryActivity.this, "לרכב זה אין טיפולים פתוחים", Toast.LENGTH_LONG).show();
            }
        });
        Query q = db.reference().child("Treatments").orderByChild("carNumber").equalTo(carNumber);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot treats : dataSnapshot.getChildren()) {
                        treatment = treats.getValue(Treatment.class);
                        try {
                            treatmentsList.add("\nטיפול " + treatment.getTreatmentNeed()
                                    + "\nבתאריך " + changeFormat(treatment.getEnterDate()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        helper.add(treatment.getMileage() + " " + treatment.getTreatmentNeed());
                    }
                }
                else {
                    Toast.makeText(TreatmentHistoryActivity.this, "אין לרכב טיפולים קודמים ", Toast.LENGTH_SHORT).show();
                }
                listView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Intent i = new Intent(TreatmentHistoryActivity.this, TreatmentActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("details", helper.get(position));
                i.putExtra("action", "showTreatment");
                i.putExtra("clientId", clientId);
                i.putExtra("exit_time", exit_time);
                i.putExtra("vendor", vendor);

                if (permission.equals("manager")) {
                    i.putExtra("permission", "manager");
                    AlertDialog.Builder alBuilder = new AlertDialog.Builder(TreatmentHistoryActivity.this);
                    alBuilder.setMessage("האם ברצונך להוסיף חשבונית ?");
                    alBuilder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //he click on yes!
                            i.putExtra("image", "yes");
                            startActivity(i);
                        }
                    });
                    alBuilder.setNegativeButton("לא", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            i.putExtra("image", "no");
                            startActivity(i);
                        }
                    });
                    alBuilder.show();
                } else {
                    i.putExtra("permission", "client");
                    i.putExtra("image", "");
                    startActivity(i);
                }
            }
        });

    }

    public static String changeFormat(String enterDate) throws ParseException {

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //You gotta parse it to a Date before correcting it
        Date parsedDate = simpleDateFormat2.parse(enterDate);
        simpleDateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String newFormatttedDate = simpleDateFormat2.format(parsedDate);

        return newFormatttedDate;
    }
}


