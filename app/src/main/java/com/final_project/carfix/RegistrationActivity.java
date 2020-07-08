package com.final_project.carfix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.logic.Car;
import com.final_project.carfix.logic.ClientDetails;
import com.final_project.carfix.logic.Treatment;
import com.final_project.carfix.logic.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Pattern;


@SuppressWarnings("deprecation")
public class RegistrationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView title_registration;
    private EditText clientIdEditText, carNum, firstName, lastName, address, email, pass, phone, car_year, car_code;
    private Spinner spinnerTypeCar, spinnerModelCar, spinnerPermission;
    private Button register;
    LinearLayout llcarDetails;

    DBHelper db = new DBHelper();
    private FirebaseAuth auth;

    User user, userEdited;
    ClientDetails clientDetails;
    Car car;
    String clientId, action, carNumber;
    boolean duplicate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //personal
        clientIdEditText = (EditText) findViewById(R.id.clientId);
        firstName = (EditText) findViewById(R.id.userName);
        lastName = (EditText) findViewById(R.id.userLastName);
        address = (EditText) findViewById(R.id.userAdd);
        phone = (EditText) findViewById(R.id.userPhone);
        title_registration = (TextView) findViewById(R.id.title_registration);

        //car details
        llcarDetails = (LinearLayout) findViewById(R.id.llcardetails);
        carNum = (EditText) findViewById(R.id.userCarNumber);
        car_year = (EditText) findViewById(R.id.caryear);
        car_code = (EditText) findViewById(R.id.carcode);
        spinnerTypeCar = (Spinner) findViewById(R.id.carType);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this, R.array.carType, R.layout.spinner_fonts);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeCar.setAdapter(adapterType);
        spinnerTypeCar.setOnItemSelectedListener(this);

        spinnerModelCar = (Spinner) findViewById(R.id.carModel);
        ArrayAdapter<CharSequence> adapterModel = ArrayAdapter.createFromResource(this, R.array.carModels, R.layout.spinner_fonts);
        adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModelCar.setAdapter(adapterModel);
        spinnerModelCar.setOnItemSelectedListener(this);

        spinnerPermission = (Spinner) findViewById(R.id.permission);
        ArrayAdapter<CharSequence> adapterMode2 = ArrayAdapter.createFromResource(this, R.array.permissionArray, R.layout.spinner_fonts);
        adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPermission.setAdapter(adapterMode2);
        spinnerPermission.setOnItemSelectedListener(this);
        spinnerPermission.setVisibility(View.GONE);

        //login details
        email = (EditText) findViewById(R.id.userEmail);
        pass = (EditText) findViewById(R.id.userPass);
        register = (Button) findViewById(R.id.register2);

        user = new User();
        clientDetails = new ClientDetails();
        car = new Car();
        auth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        clientId = i.getStringExtra("clientId");
        action = i.getStringExtra("action");
        carNumber = i.getStringExtra("carNumber");

        spinnerModelCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(15);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerTypeCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(15);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (action.equals("newClientManager")) {
            newClientManager();
        } else {
            //new client - from the first screen - עובד!!!!!!!!
            if (action.equals("newClient")) {
                registerClient();
            } else {
                if (action.equals("editClient")) {
                    editClientDetail();
                } else {
                    if (action.equals("showClient")) {
                        showClientDetails();
                    }
                }
            }
        }
    }

    private void newClientManager() {
        spinnerPermission.setVisibility(View.VISIBLE);
        spinnerPermission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(15);
                if (spinnerPermission.getSelectedItem().toString().equals("יועץ שירות")) {
                    registerManager();
                } else {
                    llcarDetails.setVisibility(View.VISIBLE);
                    registerClient();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void editClientDetail() {
        title_registration.setText("עדכון משתמש קיים");
        Query q = db.getUserTable().orderByKey().equalTo(clientId);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot user : dataSnapshot.getChildren()) {
                        final User userEdit = user.getValue(User.class);

                        //show all details of client
                        clientIdEditText.setText(user.getKey());
                        firstName.setText(userEdit.getClientDetails().getFirstName());
                        lastName.setText(userEdit.getClientDetails().getLastName());
                        address.setText(userEdit.getClientDetails().getAddress());

                        //car details
                        carNum.setText(userEdit.getCar().getCarNumber());
                        spinnerModelCar.setSelection(((ArrayAdapter<String>) spinnerModelCar.getAdapter()).getPosition(userEdit.getCar().getCarModel()));
                        spinnerTypeCar.setSelection(((ArrayAdapter<String>) spinnerModelCar.getAdapter()).getPosition(userEdit.getCar().getCarType()));
                        car_code.setText(userEdit.getCar().getCarCode());
                        car_year.setText(userEdit.getCar().getCarYear());

                        email.setText(userEdit.getEmail());
                        email.setEnabled(false);
                        phone.setText(userEdit.getClientDetails().getPhone());
                        pass.setVisibility(View.GONE);
                        register.setText("עדכן");

                        //save changes
                        register.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                userEdit.setClientDetails(new ClientDetails(clientIdEditText.getText().toString(), firstName.getText().toString(),
                                        lastName.getText().toString(), address.getText().toString(), phone.getText().toString()));

                                //String carType, String carModel, String carNumber, String carYear, String carCode
                                userEdit.setCar(new Car(userEdit.getCar().getCarType(), userEdit.getCar().getCarModel(), userEdit.getCar().getCarNumber(), userEdit.getCar().getCarYear(), car_code.getText().toString()));
                                db.reference().child("Users").child(clientIdEditText.getText().toString()).setValue(userEdit);
                                Toast.makeText(RegistrationActivity.this, "שינוי בוצע בהצלחה", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(RegistrationActivity.this, ManagerOptionActivity.class);
                                i.putExtra("carNumber", carNumber);
                                startActivity(i);

                            }
                        });
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "משתמש לא רשום", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void showClientDetails() {
        title_registration.setText("פרטי משתמש");
        Query q = db.getUserTable().orderByKey().equalTo(clientId);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot user : dataSnapshot.getChildren()) {
                        final User userEdit = user.getValue(User.class);

                        //show all details of client
                        clientIdEditText.setText("מספר ת.ז: " + user.getKey());
                        clientIdEditText.setEnabled(false);

                        firstName.setText("שם פרטי: " + userEdit.getClientDetails().getFirstName());
                        firstName.setEnabled(false);

                        lastName.setText("שם משפחה: " + userEdit.getClientDetails().getLastName());
                        lastName.setEnabled(false);

                        address.setText("כתובת: " + userEdit.getClientDetails().getAddress());
                        address.setEnabled(false);

                        phone.setText("נייד: " + userEdit.getClientDetails().getPhone());
                        phone.setEnabled(false);

                        //car details
                        carNum.setText("מס' רכב: " + userEdit.getCar().getCarNumber());
                        carNum.setEnabled(false);

                        spinnerModelCar.setSelection(((ArrayAdapter<String>) spinnerModelCar.getAdapter()).getPosition(userEdit.getCar().getCarModel()));
                        spinnerModelCar.setEnabled(false);
                        spinnerTypeCar.setSelection(((ArrayAdapter<String>) spinnerModelCar.getAdapter()).getPosition(userEdit.getCar().getCarType()));
                        spinnerTypeCar.setEnabled(false);

                        car_code.setText("קוד רכב: " + userEdit.getCar().getCarCode());
                        car_code.setEnabled(false);

                        car_year.setText("שנת רכב: " + userEdit.getCar().getCarYear());
                        car_year.setEnabled(false);

                        email.setText(userEdit.getEmail());
                        email.setEnabled(false);

                        pass.setVisibility(View.GONE);
                        register.setVisibility(View.GONE);
                        //save changes
                        register.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                userEdit.setClientDetails(new ClientDetails(clientIdEditText.getText().toString(), firstName.getText().toString(),
                                        lastName.getText().toString(), address.getText().toString(), phone.getText().toString()));

                                //String carType, String carModel, String carNumber, String carYear, String carCode
                                userEdit.setCar(new Car(userEdit.getCar().getCarType(), userEdit.getCar().getCarModel(), userEdit.getCar().getCarNumber(), userEdit.getCar().getCarYear(), car_code.getText().toString()));
                                db.reference().child("Users").child(clientIdEditText.getText().toString()).setValue(userEdit);
                                Toast.makeText(RegistrationActivity.this, "שינוי בוצע בהצלחה", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(RegistrationActivity.this, ManagerOptionActivity.class);
                                i.putExtra("carNumber", carNumber);
                                startActivity(i);

                            }
                        });
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "משתמש לא רשום", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void userSetValue() {
        if (spinnerPermission.getSelectedItem().toString().equals("לקוח")) {
            user.setClientDetails(setClientDetails());
            car = new Car(spinnerTypeCar.getSelectedItem().toString(), spinnerModelCar.getSelectedItem().toString(), carNum.getText().toString(), car_year.getText().toString(), car_code.getText().toString());
            user.setCar(car);
            user.setEmail(email.getText().toString());
            user.setPermission("client");
        } else {
            user.setClientDetails(setClientDetails());
            user.setEmail(email.getText().toString());
            user.setPermission("manager");
        }
        //get token
        user.setToken(FirebaseInstanceId.getInstance().getToken());

    }

    public ClientDetails setClientDetails() {
        clientDetails.setAddress(address.getText().toString());
        clientDetails.setFirstName(firstName.getText().toString());
        clientDetails.setLastName(lastName.getText().toString());
        clientDetails.setPhone(phone.getText().toString());
        return clientDetails;
    }

    //true= evreything ok.
    public boolean checkUserPersonalDetail(String id, String fName, String lName, String ads, String phone) {
        if (id.equals("") || id.length() < 9 || Pattern.matches("[a-zA-Z]+", id) == true) {
            Toast.makeText(RegistrationActivity.this, "מס' ת.ז אינו תקין", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (fName.equals("") || lName.equals("")) {
                Toast.makeText(RegistrationActivity.this, "אנא הכנס את שמך המלא", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                if (fName.matches(".*[a-z].*") || lName.matches(".*[a-z].*") || ads.matches(".*[a-z].*")) {
                    Toast.makeText(RegistrationActivity.this, "אנא הכנס פרטים בעברית בלבד", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (phone.equals("") || phone.length() != 10) {
                    Toast.makeText(RegistrationActivity.this, "מס' נייד לא תקין", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        }
    }

    public boolean checkCarDetails(String carNum, String carYear, String car_code) {
        if (carNum.equals("") || carNum.length() < 5 || carNum.length() > 9) {
            Toast.makeText(RegistrationActivity.this, "מס' רכב לא תקין", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (carYear.equals("") || carYear.length() != 4) {
                Toast.makeText(RegistrationActivity.this, "שנת רכב לא תקינה", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                if (car_code.equals("") || car_code.length() < 4 || car_code.length() > 6) {
                    Toast.makeText(RegistrationActivity.this, "קוד הרכב לא תקין", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return true;
    }

    public void registerClient() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.reference().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (checkUserPersonalDetail(clientIdEditText.getText().toString(), firstName.getText().toString(), lastName.getText().toString(), address.getText().toString(), phone.getText().toString()) && checkCarDetails(carNum.getText().toString(), car_year.getText().toString(), car_code.getText().toString())
                                && !noDuplicate(carNumber, clientIdEditText.getText().toString())) {
                            userSetValue();
                            auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                                    .addOnFailureListener(RegistrationActivity.this, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegistrationActivity.this, "אי-מייל או סיסמא אינם נכונים", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                //FirebaseUser user = auth.getCurrentUser();
                                                //add user
                                                db.getUserTable().child(clientIdEditText.getText().toString()).setValue(user);

                                                Toast.makeText(RegistrationActivity.this, "הרישום בוצע בהצלחה!", Toast.LENGTH_SHORT).show();
                                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(i);
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegistrationActivity.this, "משתמש כבר רשום במערכת", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    //in this method i check that the id not exist and the carNumber not exist
    private boolean noDuplicate(String carNum, String clientId) {
        Query query = db.getUserTable().orderByChild(clientId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    duplicate = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Query query1 = db.getUserTable().orderByChild("carNumber").equalTo(carNum);
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                    duplicate = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return duplicate;
    }

    public void registerManager() {
        llcarDetails.setVisibility(View.GONE);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkUserPersonalDetail(clientIdEditText.getText().toString(), firstName.getText().toString(), lastName.getText().toString(), address.getText().toString(), phone.getText().toString())) {
                    userSetValue();
                    auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                            .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        //FirebaseUser user = auth.getCurrentUser();
                                        //add user
                                        db.getUserTable().child(clientIdEditText.getText().toString()).setValue(user);
                                        Toast.makeText(RegistrationActivity.this, "הרישום בוצע בהצלחה!", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(i);

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegistrationActivity.this, "אימייל או סיסמא לא תקינים", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
        });
    }
}



