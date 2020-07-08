package com.final_project.carfix;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.final_project.carfix.ExternalServices.Upload;
import com.final_project.carfix.logic.Treatment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class BillActivity extends AppCompatActivity {

    public static final int PICK_IMAGE_REQUEST = 1;
    String carNumber, mileage, treatNeed, action, url;
    Button takeAPic, upload, download;
    ImageView pic;

    private Uri uri;
    private StorageReference storageReference;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        takeAPic = findViewById(R.id.caputre);
        upload = findViewById(R.id.upload);
        pic = findViewById(R.id.pic);
//        download = findViewById(R.id.download);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        ref = FirebaseDatabase.getInstance().getReference("Treatments");

        Intent i = getIntent();
        action = i.getStringExtra("action");
//        if (action.equals("upload")) {
        carNumber = i.getStringExtra("carNumber");
        mileage = i.getStringExtra("mileage");
        treatNeed = i.getStringExtra("treatmentNeed");

        takeAPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    private void uploadFile() {
        if (uri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(uri));
            fileReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(BillActivity.this, "Upload successful!", Toast.LENGTH_LONG).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {

                            ref.orderByChild("carNumber").equalTo(carNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                                        String key = child.getKey();
                                        Treatment t = child.getValue(Treatment.class);
                                        if (t.getMileage().equals(mileage) && t.getTreatmentNeed().equals(treatNeed)) {
                                            String url = uri.toString();
                                            final Upload upload = new Upload(url);
                                            t.setBill(upload.getUrl());
                                            ref.child(key).child("bill").setValue(upload.getUrl());
                                            Toast.makeText(BillActivity.this, "העלאה בוצעה בהצלחה", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(BillActivity.this, ManagerOptionActivity.class);
                                            intent.putExtra("carNumber", carNumber);
                                            startActivity(intent);
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    Toast.makeText(BillActivity.this, "העלאה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "לא נבחר קובץ להעלאה", Toast.LENGTH_SHORT).show();
        }


    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();

            Picasso.with(this).load(uri).into(pic);
        }
    }

}
