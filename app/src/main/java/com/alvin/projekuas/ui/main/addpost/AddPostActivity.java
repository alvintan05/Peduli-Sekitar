package com.alvin.projekuas.ui.main.addpost;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Report;
import com.alvin.projekuas.ui.main.home.HomeFragment;
import com.alvin.projekuas.utils.Preference;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddPostActivity extends AppCompatActivity implements View.OnClickListener {

    // widget
    private TextInputEditText edtTitle, edtDesc;
    private Button btnAddImage, btnAddLocation, btnSave;
    private ImageView imgPreview;
    private TextView tvLocation;
    private ProgressDialog progressDialog;

    // vars
    private static final int REQUEST_PERMISSION = 100;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int LOCATION_REQUEST = 3;
    private static final String TAG = "AddPostActivity";
    private String filepath;
    private Bitmap image;
    private double latitude, longitude;
    private String username, avatar, userId;
    private Preference preference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // casting widget
        edtTitle = findViewById(R.id.edt_judul);
        edtDesc = findViewById(R.id.edt_deskripsi);
        btnAddImage = findViewById(R.id.btn_add_image);
        btnAddLocation = findViewById(R.id.btn_add_location);
        btnSave = findViewById(R.id.btn_save_data);
        imgPreview = findViewById(R.id.img_preview);
        tvLocation = findViewById(R.id.tv_alamat);
        preference = new Preference(this);
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Harap Tunggu...");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tambah Laporan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userId = preference.getUserId();

        btnSave.setOnClickListener(this);
        btnAddLocation.setOnClickListener(this);
        btnAddImage.setOnClickListener(this);

        getUserData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_image:
                checkStoragePermission();
                break;
            case R.id.btn_add_location:
                checkLocationPermission();
                break;
            case R.id.btn_save_data:
                validate();
                break;
        }
    }

    private void getUserData() {
        progressDialog.show();
        DocumentReference ref = db.collection("user").document(userId);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        username = document.get("name").toString();
                        avatar = document.get("photo").toString();
                        progressDialog.dismiss();
                    } else {
                        Log.d(TAG, "No such document");
                        progressDialog.dismiss();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void checkStoragePermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(AddPostActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);

        } else {

            AlertDialog.Builder alert = new AlertDialog.Builder(AddPostActivity.this);
            alert.setItems(new String[]{"Pilih Gambar", "Camera"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        showFileChooser();
                    } else {
                        showCamera();
                    }
                }
            });
            alert.show();

        }
    }

    private void checkLocationPermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(AddPostActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);
        } else {
            Intent locationIntent = new Intent(this, LocationPickerActivity.class);
            startActivityForResult(locationIntent, LOCATION_REQUEST);
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), GALLERY_REQUEST);
    }

    private void showCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.alvin.projekuas.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        filepath = image.getAbsolutePath();
        return image;
    }

    private byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void validate() {
        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String address = tvLocation.getText().toString();

        if (title.length() > 0 && desc.length() > 0 && address.length() > 0 && image != null) {
            uploadPicture(title, desc, address);
        } else {
            Toast.makeText(this, "Lengkapi Data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPicture(final String title, final String desc, final String address) {
        progressDialog.show();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final StorageReference reference = storageRef.child("report").child(timeStamp);
        UploadTask uploadTask = reference.putBytes(getFileDataFromDrawable(image));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = reference.getDownloadUrl();
                urlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        saveData(title, desc, address, uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure Storage: " + e.getMessage());
            }
        });
    }

    private void saveData(String title, String desc, String address, String photo) {

        GeoPoint geoPoint = new GeoPoint(latitude, longitude);

        Report report = new Report(title, desc, address, photo, userId, Timestamp.now(), geoPoint);

        db.collection("report").document().set(report)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        setResult(HomeFragment.RESULT_ADD);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Data Gagal Ditambahkan", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Glide.with(this)
                    .load(data.getData())
                    .into(imgPreview);
            try {
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            File file = new File(filepath);
            Uri photoUri = FileProvider.getUriForFile(this,
                    "com.alvin.projekuas.fileprovider",
                    file);
            Glide.with(this)
                    .load(photoUri)
                    .into(imgPreview);

            image = BitmapFactory.decodeFile(filepath);
        } else if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            tvLocation.setVisibility(View.VISIBLE);

            latitude = data.getDoubleExtra("latitude", 1);
            longitude = data.getDoubleExtra("longitude", 1);

            tvLocation.setText("" + data.getStringExtra("alamat"));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
