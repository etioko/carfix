package com.final_project.carfix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ContactActivity extends AppCompatActivity {

    ImageView history, future, show_details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        history = (ImageView)findViewById(R.id.history);
        future = (ImageView)findViewById(R.id.future_func);
        show_details = (ImageView)findViewById(R.id.show_details);

        Intent i = getIntent();
        final String carNumber = i.getStringExtra("carNumber");
        final String vendor = i.getStringExtra("vendor");
        final String clientId = i.getStringExtra("clientId");

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TreatmentHistoryActivity.class);
                i.putExtra("carNumber", carNumber);
                i.putExtra("permission", "client");
                i.putExtra("vendor", vendor);
                startActivity(i);
            }
        });

        future.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FutureArrivalActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
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

    }
}
