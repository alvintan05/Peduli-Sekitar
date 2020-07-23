package com.alvin.projekuas.ui.main.profile;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alvin.projekuas.R;
import com.alvin.projekuas.databinding.ActivityEditProfileBinding;
import com.alvin.projekuas.entity.Profile;
import com.alvin.projekuas.ui.main.addpost.LocationPickerActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    // widget
    private ProgressDialog progressDialog;
    private ActivityEditProfileBinding binding;

    // vars
    private static final String TAG = "EditProfileActivity";
    private static final int REQUEST_PERMISSION = 100;
    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int LOCATION_REQUEST = 3;
    private double latitude, longitude;
    private String filepath;
    private String fireStorePath;
    private Bitmap image;
    private Profile profile;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // casting
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Harap Tunggu...");

        profile = getIntent().getParcelableExtra("profile");
        fireStorePath = getIntent().getStringExtra("path");
        attachToView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnEditLocation.setOnClickListener(this);
        binding.btnUpdate.setOnClickListener(this);
        binding.imgProfileAvatar.setOnClickListener(this);
    }

    private void attachToView() {
        binding.edtNama.setText(profile.getName());
        binding.edtEmail.setText(profile.getEmail());
        binding.edtNomor.setText(profile.getPhone());
        binding.tvEditAlamat.setText(profile.getAddress());

        Glide.with(this)
                .load(profile.getPhoto())
                .apply(RequestOptions.circleCropTransform())
                .into(binding.imgProfileAvatar);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_location:
                checkLocationPermission();
                break;
            case R.id.btn_update:
                validate();
                break;
            case R.id.img_profile_avatar:
                checkStoragePermission();
                break;
        }
    }

    private void checkStoragePermission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_PERMISSION);

        } else {

            AlertDialog.Builder alert = new AlertDialog.Builder(EditProfileActivity.this);
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
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
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
        String nama = binding.edtNama.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String nomor = binding.edtNomor.getText().toString().trim();
        String alamat = binding.tvEditAlamat.getText().toString().trim();

        if (nama.length() > 0 && email.length() > 0 && nomor.length() > 0 && alamat.length() > 0) {

            Profile updateProfile = new Profile(nama, email, nomor, alamat);
            uploadPicture(updateProfile);
        } else {
            Toast.makeText(this, "Harap lengkapi data", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPicture(final Profile updateProfile) {
        progressDialog.show();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final StorageReference reference = storageRef.child("user_profile").child(timeStamp);

        if (image != null) {
            UploadTask uploadTask = reference.putBytes(getFileDataFromDrawable(image));
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> urlTask = reference.getDownloadUrl();
                    urlTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            updateProfile.setPhoto(uri.toString());
                            updateData(updateProfile);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure Storage: " + e.getMessage());
                }
            });
        } else {
            updateProfile.setPhoto(profile.getPhoto());
            updateData(updateProfile);
        }
    }

    private void updateData(Profile updateProfile) {
        db.document(fireStorePath).set(updateProfile, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Data Berhasil Diubah", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Data Gagal Diubah", Toast.LENGTH_SHORT).show();
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
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgProfileAvatar);
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
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imgProfileAvatar);

            image = BitmapFactory.decodeFile(filepath);
        } else if (requestCode == LOCATION_REQUEST && resultCode == RESULT_OK) {
            binding.tvEditAlamat.setText("" + data.getStringExtra("alamat"));

            latitude = data.getDoubleExtra("latitude", 1);
            longitude = data.getDoubleExtra("longitude", 1);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
