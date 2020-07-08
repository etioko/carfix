package com.final_project.carfix.ExternalServices;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.final_project.carfix.logic.Treatment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by etioko on 06/01/2019.
 */

public class DBHelper {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    String[] start;
    String[] end;

    public DBHelper() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.reference = firebaseDatabase.getReference();
    }

    public DatabaseReference reference() {
        return this.reference;
    }

    public DatabaseReference getUserTable() {
        return reference.child("Users");
    }

    public Query getUserDetailFromCarNumber(String carNumber) {
        return this.reference().child("Users").orderByChild("car/carNumber").equalTo(carNumber);
    }

    public Query getTreatmentByCarNumber(String carNumber) {
        return this.reference().child("Treatments").orderByChild("carNumber").equalTo(carNumber);
    }

    public int getAmountOfTreatInLastFourHours() {
        final int[] count = {0};
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.HOUR_OF_DAY, -4);
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd/MM/yyy HH:mm");
        final String localTime = date.format(currentLocalTime);

        Query q = this.reference.child("Treatments").orderByChild("enterDate");
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Treatment treatment1 = dataSnapshot1.getValue(Treatment.class);
                        if (treatment1.getExitDate().compareTo(localTime) == 1)//if there is a treat open to this car.
                        {
                            if (treatment1.getEnterDate().compareTo(localTime) == 1)
                                count[0]++;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return count[0];
    }
}
