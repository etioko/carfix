package com.final_project.carfix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.logic.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText password, email;
    private Button login;
    private TextView forgotPassword;
    private SharedPreferences mPref;
    private CheckBox rememberMe;
    private ImageView eye;

    private static final String PREFS_NAME = "PrefsFile";

    DBHelper db;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        auth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        password.setTextColor(Color.WHITE);
        login = (Button) findViewById(R.id.login);
        forgotPassword = (TextView) findViewById(R.id.forgot_pass);
        rememberMe = (CheckBox) findViewById(R.id.remember);
        eye = (ImageView) findViewById(R.id.eye);
        eye.setVisibility(View.GONE);
        forgotPassword.setPaintFlags(forgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        db = new DBHelper();
        mPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        getPreferencesData();

        password.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(password.getText().length() > 0)
                    eye.setVisibility(View.VISIBLE);
                else
                    eye.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        eye.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {

                    case MotionEvent.ACTION_UP:
                        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;

                    case MotionEvent.ACTION_DOWN:
                        password.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                }
                return true;
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();
                String userPass = password.getText().toString();
                signIn(userEmail, userPass);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });

    }

    private void forgetPassword() {
        if (email.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "הכנס את כתובת המייל שלך", Toast.LENGTH_SHORT).show();
        } else {
            Query query = db.getUserTable().orderByChild("email").equalTo(email.getText().toString().trim());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "המייל נשלח בהצלחה", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else
                        Toast.makeText(LoginActivity.this, "משתמש לא רשום", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    private void getPreferencesData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (sp.contains("pref_name")) {
            String userName = sp.getString("pref_name", "");
            email.setText(userName.toString());
        }
        if (sp.contains("pref_pass")) {
            String pass = sp.getString("pref_pass", "");
            password.setText(pass.toString());
        }
    }

    //login with user and password;
    private void signIn(final String userEmail, final String pass) {

        auth.signInWithEmailAndPassword(userEmail, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Query query = db.getUserTable().orderByChild("email").equalTo(userEmail.trim());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot user : dataSnapshot.getChildren()) {
                                            User userLogin = user.getValue(User.class);
                                            if (rememberMe.isChecked()) {
                                                SharedPreferences.Editor editor = mPref.edit();
                                                editor.putString("pref_name", userEmail);
                                                editor.putString("pref_pass", pass);
                                                editor.apply();
                                            }
                                            if (userLogin.getPermission().equals("client")) {
                                                Intent i = new Intent(getApplicationContext(), ClientActivity.class);
                                                //user car number
                                                i.putExtra("carNumber", userLogin.getCar().getCarNumber());
                                                //user id
                                                i.putExtra("userKey", dataSnapshot.getKey());
                                                startActivity(i);
                                            } else {
                                                if (userLogin.getPermission().equals("manager")) {
                                                    Intent i = new Intent(getApplicationContext(), ManagerActivity.class);
                                                    i.putExtra("userKey", dataSnapshot.getKey());
                                                    startActivity(i);
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "אימייל או סיסמא אינם נכונים ", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
}
