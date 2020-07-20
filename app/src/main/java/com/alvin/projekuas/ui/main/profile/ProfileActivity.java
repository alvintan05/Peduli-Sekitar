package com.alvin.projekuas.ui.main.profile;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Profile;
import com.alvin.projekuas.ui.login.LoginActivity;
import com.alvin.projekuas.utils.Preference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    // widget
    private ImageView imgAvatar;
    private TextView tvNama, tvEmail, tvNomor, tvAlamat;
    private ProgressDialog progressDialog;
    private Button btnEdit, btnSignOut;

    // widget full image dialog
    private Dialog imageDialog;
    private PhotoView photoView;
    private ImageView closeDialog;
    private ProgressBar progressBar;

    // vars
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference ref;
    private Preference preference;
    private Profile profileUser;
    private String path;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // casting widget
        imgAvatar = findViewById(R.id.img_profile_avatar);
        tvNama = findViewById(R.id.tv_profile_nama);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvNomor = findViewById(R.id.tv_profile_nomor);
        tvAlamat = findViewById(R.id.tv_profile_alamat);
        btnEdit = findViewById(R.id.btn_edit_profile);
        btnSignOut = findViewById(R.id.btn_sign_out);

        preference = new Preference(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Harap Tunggu...");
        db = FirebaseFirestore.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        String userId = preference.getUserId();
        ref = db.collection("user").document(userId);

        getDataFromDB();
        setUpFullImageDialog();

        btnEdit.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);
        closeDialog.setOnClickListener(this);
    }

    private void getDataFromDB() {
        progressDialog.show();
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        profileUser = document.toObject(Profile.class);
                        path = document.getReference().getPath();
                        attachDataToView(profileUser);
                        progressDialog.dismiss();
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(ProfileActivity.this, "Tidak Ada Data", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(ProfileActivity.this, "Gagal Mengambil Data", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void attachDataToView(Profile profile) {
        String nama = profile.getName() != null ? profile.getName() : "Tidak ada data";
        String email = profile.getEmail() != null ? profile.getEmail() : "Tidak ada data";
        String nomor = profile.getPhone() != null ? profile.getPhone() : "Tidak ada data";
        String alamat = profile.getAddress() != null ? profile.getAddress() : "Tidak ada data";
        String photo = profile.getPhoto() != null ? profile.getPhoto() : "";

        tvNama.setText(nama);
        tvEmail.setText(email);
        tvNomor.setText(nomor);
        tvAlamat.setText(alamat);

        Glide.with(this)
                .load(photo)
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .apply(RequestOptions.circleCropTransform())
                .into(imgAvatar);
    }

    private void setUpFullImageDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_fullscreen_image, null);
        imageDialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
        imageDialog.setContentView(view);
        imageDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        // initialize widget dialog
        photoView = view.findViewById(R.id.img_detail);
        closeDialog = view.findViewById(R.id.ic_close_dialog);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_profile:
                editProfile();
                break;
            case R.id.btn_sign_out:
                signOut();
                break;
            case R.id.img_profile_avatar:
                showFullImageDialog();
                break;
            case R.id.ic_close_dialog:
                imageDialog.dismiss();
                break;
        }
    }

    private void showFullImageDialog() {
        imageDialog.show();
        Glide.with(this)
                .load(profileUser.getPhoto())
                .fitCenter()
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Harap coba lagi", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(photoView);
    }

    private void editProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("profile", profileUser);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    private void signOut() {
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        preference.removeUserId();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ref.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ProfileActivity.this, "Error While Loading", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value.exists()) {
                    profileUser = value.toObject(Profile.class);
                    attachDataToView(profileUser);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
