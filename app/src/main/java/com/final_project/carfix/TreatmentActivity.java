package com.final_project.carfix;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.APIClient;
import com.final_project.carfix.ExternalServices.DBHelper;
import com.final_project.carfix.ExternalServices.NetworkClient;
import com.final_project.carfix.ExternalServices.PostRequest;
import com.final_project.carfix.ExternalServices.PostResponse;
import com.final_project.carfix.logic.Treatment;
import com.final_project.carfix.logic.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TreatmentActivity extends AppCompatActivity {

    TextView title_treatment, textMileage, textNeed;
    EditText mileage, remarks;
    Button addTreat, uploadImage, showImage;
    ImageView treatment_add, editClient, history_clicked, future_icon, edit_prediction, show_details;
    String carNumber, mileageStr, image, permission, action, clientId, vendor, remarkString, treatmentNeedString, milageString;
    Spinner carType, spinnerTreat, treatmentNeed;

    DBHelper db = new DBHelper();
    Treatment t;

    public MutableLiveData<Integer> counter = new MutableLiveData<>();


    //this strings are for open new Treatment;
    String enterDateString, extra, carvendor, exit_time, token, url;

    private StorageReference mStorage = FirebaseStorage.getInstance().getReference();
    private APIClient jsonAPI;

    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment);

        title_treatment = (TextView) findViewById(R.id.title_treatment);
        mileage = (EditText) findViewById(R.id.mileage);
        mileage.setTextColor(Color.WHITE);
        remarks = (EditText) findViewById(R.id.remarks);
        remarks.setTextColor(Color.WHITE);
        carType = (Spinner) findViewById(R.id.carTypeTreat);
        spinnerTreat = (Spinner) findViewById(R.id.spinnerTreat);
        treatmentNeed = (Spinner) findViewById(R.id.treatment_need);
        addTreat = (Button) findViewById(R.id.addTreat);
        uploadImage = (Button) findViewById(R.id.uploadImage);
        showImage = (Button) findViewById(R.id.showImage);
        treatment_add = (ImageView) findViewById(R.id.treatmet_add);
        editClient = (ImageView) findViewById(R.id.edit_details);
        history_clicked = (ImageView) findViewById(R.id.history_clicked);
        future_icon = (ImageView) findViewById(R.id.future_icon);
        edit_prediction = (ImageView) findViewById(R.id.edit_prediction);
        show_details = (ImageView) findViewById(R.id.show_details);
        textMileage = (TextView) findViewById(R.id.textmileage);
        textNeed = (TextView)findViewById(R.id.textneed);

        uploadImage.setVisibility(View.GONE);
        showImage.setVisibility(View.GONE);

        //spinners definition
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this, R.array.carType, R.layout.spinner_fonts);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carType.setAdapter(adapterType);

        ArrayAdapter<CharSequence> adaptertreatNeed = ArrayAdapter.createFromResource(this, R.array.treatmentNeedArray, R.layout.spinner_fonts);
        adaptertreatNeed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        treatmentNeed.setAdapter(adaptertreatNeed);

        ArrayAdapter<CharSequence> adapterModel = ArrayAdapter.createFromResource(this, R.array.treatArray, R.layout.spinner_fonts);
        adapterModel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreat.setAdapter(adapterModel);

        Retrofit retrofit = NetworkClient.getRetrofitClient();
        jsonAPI = retrofit.create(APIClient.class);

        t = new Treatment();

        final Intent intent = getIntent();
        carNumber = intent.getStringExtra("carNumber");
        permission = intent.getStringExtra("permission");
        action = intent.getStringExtra("action");
        clientId = intent.getStringExtra("clientId");
        vendor = intent.getStringExtra("vendor");
        exit_time = intent.getStringExtra("exit_time");
        mileageStr = intent.getStringExtra("details");

        if (permission.equals("client")) {
            treatment_add.setVisibility(View.GONE);
            editClient.setVisibility(View.GONE);
        } else {
            future_icon.setVisibility(View.GONE);
            show_details.setVisibility(View.GONE);
            image = intent.getStringExtra("image");
            if (image.equals("yes"))
                uploadImage.setVisibility(View.VISIBLE);
            else {
                uploadImage.setVisibility(View.GONE);
            }
        }
        counter.observe(this, new Observer<Integer>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onChanged(@Nullable final Integer newName) {
                // Update the UI, in this case, a TextView.
                Log.d("----earliers entries----", newName + "");
                if (Integer.parseInt(treatmentNeedString) <= 30000)
                    createPostRequest(Integer.parseInt(carNumber), token, enterDateString, extra, "A", carvendor, newName, remarkString, treatmentNeedString, milageString);
                else
                    createPostRequest(Integer.parseInt(carNumber), token, enterDateString, extra, "B", carvendor, newName, remarkString, treatmentNeedString, milageString);

                Intent intent = new Intent(TreatmentActivity.this, ManagerOptionActivity.class);
                intent.putExtra("carNumber", carNumber);
                startActivity(intent);
            }
        });

        spinnerTreat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(20);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        treatmentNeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(20);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        carType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(20);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //check if there is a token to this car-number's user.
        //in other words, i check if the user is registerd.
        Query query = db.getUserDetailFromCarNumber(carNumber);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        User user1 = user.getValue(User.class);
                        token = user1.getToken();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        show_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("action", "showClient");
                startActivity(i);
            }
        });
        history_clicked.setOnClickListener(new View.OnClickListener() {
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
        future_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FutureArrivalActivity.class);
                i.putExtra("clientId", clientId);
                i.putExtra("carNumber", carNumber);
                i.putExtra("vendor", vendor);
                i.putExtra("exit_time", exit_time);
                startActivity(i);
            }
        });

        edit_prediction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!exit_time.equals("") && exit_time != null) {
                    Intent i = new Intent(getApplicationContext(), EditExitTimeActivity.class);
                    i.putExtra("carNumber", carNumber);
                    i.putExtra("permission", "manager");
                    i.putExtra("clientId", clientId);
                    i.putExtra("exit_time", exit_time);
                    i.putExtra("vendor", vendor);
                    startActivity(i);
                } else
                    Toast.makeText(TreatmentActivity.this, "לרכב זה אין טיפולים פתוחים", Toast.LENGTH_LONG).show();
            }
        });

        if (action.equals("addTreatment")) {
            addTreatment();
        } else {
            if (action.equals("showTreatment")) {
                showTreatment();
            }
        }
    }

    private void addTreatment() {
        textNeed.setText("");
        textNeed.setTextSize(0);
        textMileage.setText("");
        textMileage.setTextSize(0);
        uploadImage.setVisibility(View.GONE);
        showImage.setVisibility(View.GONE);

        if (!vendor.equals("") && !vendor.equals(null)) {
            carType.setVisibility(View.GONE);
            carvendor = vendor;
        }

        spinnerTreat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(18);
                if (parent.getItemAtPosition(position).toString().equals("טסט")) {
                    extra = "B";
//                            t.setExtra("B");
//                            Toast.makeText(getApplicationContext(),parent.getItemAtPosition(position).toString() , Toast.LENGTH_LONG).show();
                } else if (parent.getItemAtPosition(position).toString().equals("תקלה"))
//                            t.setExtra("C");
                    extra = "C";
                else if (parent.getItemAtPosition(position).toString().equals("טיפול קודם שהתפספס"))
//                            t.setExtra("A");
                    extra = "A";
                else
//                            t.setExtra("N");
                    extra = "N";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        carType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        t.setVendor(parent.getItemAtPosition(position).toString());
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(18);
                carvendor = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        treatmentNeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                ((TextView) parent.getChildAt(0)).setTextSize(18);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addTreat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mileage.getText().equals("") && !treatmentNeed.getSelectedItem().toString().equals("טיפול נדרש")) {
                    db.reference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            DateFormat dateAndTime = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                            DateFormat onlyTime = new SimpleDateFormat("HH:mm:ss");

                            String localTime = onlyTime.format(currentLocalTime);
                            String forAlgorithm = dateAndTime.format(currentLocalTime);

                            LocalTime timeStart = LocalTime.parse(localTime);
                            LocalTime endTimeInNormalDay = LocalTime.parse("16:30:00");
                            LocalTime endTimeInFriday = LocalTime.parse("10:00:00");

                            if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY && timeStart.isAfter(endTimeInFriday))
                            {
                                Toast.makeText(TreatmentActivity.this, "אין קבלת רכבים בשישי לאחר 10:00", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                                    Toast.makeText(TreatmentActivity.this, "אין קבלת רכבים ביום זה", Toast.LENGTH_SHORT).show();
                                else
                                {
                                    if(timeStart.isAfter(endTimeInNormalDay))
                                        Toast.makeText(TreatmentActivity.this, "אין קבלת רכבים בשעה זו", Toast.LENGTH_SHORT).show();
                                    else
                                    {
                                        treatmentNeedString = treatmentNeed.getSelectedItem().toString();
                                        remarkString = remarks.getText().toString();
                                        milageString = mileage.getText().toString();
                                        enterDateString = forAlgorithm;

                                        //calculation of earliers entries AND ADD THE TREATMENT.
                                        getEarlierEnters();
                                    }

                                }

                            }
//                                if (Integer.parseInt(treatmentNeedString) <= 30000)
//                                    createPostRequest(Integer.parseInt(carNumber), token, enterDateString, extra, "A", carvendor, counter_erliers, remarkString, treatmentNeedString, milageString);
//                                else
//                                    createPostRequest(Integer.parseInt(carNumber), token, enterDateString, extra, "B", carvendor, counter_erliers, remarkString, treatmentNeedString, milageString);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "אנא הזן קילומטראג' וטיפול נדרש", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showTreatment() {
        title_treatment.setText("צפייה בטיפול");
        addTreat.setText("חזור");
        final String[] twoMileage = mileageStr.split(" ");
        final String mileageString = twoMileage[0].trim();
        final String tNeedString = twoMileage[1].trim();

        Query q = db.reference().child("Treatments").orderByChild("carNumber").equalTo(carNumber);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot treats : dataSnapshot.getChildren()) {
                        t = treats.getValue(Treatment.class);
                        if (t.getMileage().compareTo(mileageString) == 0 && t.getTreatmentNeed().compareTo(tNeedString) == 0) {
                            mileage.setText(t.getMileage());
                            mileage.setEnabled(false);
                            treatmentNeed.setSelection(((ArrayAdapter<String>) treatmentNeed.getAdapter()).getPosition(t.getTreatmentNeed()));
                            treatmentNeed.setEnabled(false);
                            remarks.setText(t.getRemarks());
                            remarks.setEnabled(false);
                            carType.setSelection(((ArrayAdapter<String>) carType.getAdapter()).getPosition(t.getVendor()));
                            carType.setEnabled(false);
                            spinnerTreat.setSelection(((ArrayAdapter<String>) spinnerTreat.getAdapter()).getPosition(t.getExtra()));
                            spinnerTreat.setEnabled(false);
                            if (!t.getBill().equals(""))
                                showImage.setVisibility(View.VISIBLE);

                            uploadImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                        Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                        if (intent.resolveActivity(getPackageManager()) != null) {
//                                            startActivityForResult(intent1, CAMERA_REQUEST_CODE);
//                                        }
                                    Intent i = new Intent(TreatmentActivity.this, BillActivity.class);
//                                            i.putExtra("action", "upload");
                                    i.putExtra("carNumber", carNumber);
                                    i.putExtra("mileage", mileage.getText().toString());
                                    i.putExtra("treatmentNeed", treatmentNeed.getSelectedItem().toString());
                                    startActivity(i);
                                }
                            });

                            showImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    db.reference().child("Treatments").orderByChild("carNumber").equalTo(carNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                Treatment t = child.getValue(Treatment.class);

                                                if (t.getMileage().equals(mileageString) && t.getTreatmentNeed().equals(tNeedString) && t.getBill() != "") {
                                                    url = t.getBill();
                                                    downloadFile(url);
                                                    Toast.makeText(TreatmentActivity.this, "החשבונית נשמרה בתמונות מכשירך בהצלחה", Toast.LENGTH_LONG).show();

//                                                            new DownloadFileFromURL().download(url);
//                                                            Toast.makeText(TreatmentActivity.this, "החשבונית נשמרה בתמונות מכשירך בהצלחה", Toast.LENGTH_LONG).show();
//                                                            Intent intent1 = new Intent(TreatmentActivity.this, BillActivity.class);
//                                                            intent1.putExtra("action", "show");
//                                                            intent1.putExtra("url", url);
//                                                            startActivity(intent1);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            addTreat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(TreatmentActivity.this, TreatmentHistoryActivity.class);
                                    i.putExtra("carNumber", carNumber);
                                    i.putExtra("permission", permission);
                                    startActivity(i);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {

            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), photo);

            //Show Uri path based on Image
            Toast.makeText(TreatmentActivity.this, "Here " + tempUri, Toast.LENGTH_LONG).show();

            //Show Uri path based on Cursor Content Resolver
            Toast.makeText(this, "Real path for URI : " + getRealPathFromURI(tempUri), Toast.LENGTH_SHORT).show();
//            mStorage.putFile(getImageUri(getApplicationContext(),photo));
            StorageReference storageReference = mStorage.child("Photo").child(tempUri.getLastPathSegment());
            storageReference.putFile(tempUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(TreatmentActivity.this, "התמונה הועלתה בהצלחה", Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(this, "Failed To Capture Image", Toast.LENGTH_SHORT).show();
        }
    }
    private Uri getImageUri(Context applicationContext, Bitmap photo) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(TreatmentActivity.this.getContentResolver(), photo, "Title", null);
        return Uri.parse(path);
    }
    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }
    private void createPostRequest(final int ticket_id, final String token, final String start, final String extra, String care_category, final String vendor, int earlier_entries, final String remarkString, final String treatmentNeedString, final String milageString) {
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
//                    helper.setText("code: " + response.code() + "\nbody: " + response.body());
                    return;
                }
                PostResponse postResponse = response.body();

                String content = "";

                try {
                    content += changeTime(start, postResponse.getPrediction());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                t = new Treatment();
                t.setCarNumber("" + ticket_id);
                t.setEnterDate(start);
                t.setExitDate(content);
                t.setExtra(extra);
                t.setTokenClient(token);
                t.setVendor(vendor);
                t.setMileage(milageString);
                t.setRemarks(remarkString);
                t.setTreatmentNeed(treatmentNeedString);
                t.setBill("");
                t.setManagerChangeTime(false);

                db.reference().child("Treatments").push().setValue(t);
                Toast.makeText(TreatmentActivity.this, "טיפול נשמר בהצלחה", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                Toast.makeText(TreatmentActivity.this, "התקשרות עם השרת נכשלה!", Toast.LENGTH_LONG).show();

            }
        });

    }
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
        return date.format(cal.getTime());

    }
    public void downloadFile(String uRl) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/Camera");
        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) getApplication().getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir("/Camera", url);
        mgr.enqueue(request);

    }
    public void getEarlierEnters() {
        //current time
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        final Date currentLocalTime = cal.getTime();

        Query query = db.reference().child("Treatments").orderByChild("startDate");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int counter_int = 0;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Treatment treatment = dataSnapshot1.getValue(Treatment.class);
                        if (parseStringToDate(treatment.getEnterDate()).compareTo(currentLocalTime) == -1 && parseStringToDate(treatment.getExitDate()).compareTo(currentLocalTime) == 1)//if there is a treat that start before min
                        {
                            counter_int += 1;
//                            Log.e("im here=====>", counter[0]+"");
                        }
                    }
                    if(counter_int == 0)
                        counter.setValue(counter_int);
                    else
                        counter.setValue(counter_int-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public Date parseStringToDate(String stringDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date1 = null;
        try {
            date1 = format.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

}

