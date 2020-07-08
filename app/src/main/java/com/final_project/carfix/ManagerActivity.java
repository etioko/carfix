package com.final_project.carfix;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.ExternalServices.NetworkClient;
import com.final_project.carfix.ExternalServices.PostRequest;
import com.final_project.carfix.ExternalServices.PostResponse;
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
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ManagerActivity extends AppCompatActivity {

    EditText carNum;
    Button createNewUser, garuge_status;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        ImageView imageView = (ImageView) findViewById(R.id.next);
        carNum = (EditText) findViewById(R.id.carNum);
        carNum.setTextColor(Color.WHITE);
        createNewUser = (Button) findViewById(R.id.new_user);
        garuge_status= (Button) findViewById(R.id.status);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (carNum.getText().toString().isEmpty() || carNum.getText().toString().length() < 5 || carNum.getText().toString().length() > 8)
                    Toast.makeText(ManagerActivity.this, "מס' רכב לא תקין, אנא הזמן מחדש", Toast.LENGTH_LONG).show();
                else
                {
                    if(Pattern.matches("[a-zA-Z]+", carNum.getText().toString()) == true)
                        Toast.makeText(ManagerActivity.this, "מס' רכב לא יכול להכיל אותיות", Toast.LENGTH_LONG).show();
                    else {
                        Intent i = new Intent(getApplicationContext(), ManagerOptionActivity.class);
                        i.putExtra("carNumber", carNum.getText().toString());
                        startActivity(i);
                    }
                }

            }
        });
        createNewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("permission", "manager");
                i.putExtra("action","newClientManager");
                startActivity(i);
            }
        });

        garuge_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), StatusActivity.class);
                startActivity(i);
            }
        });
    }
}
