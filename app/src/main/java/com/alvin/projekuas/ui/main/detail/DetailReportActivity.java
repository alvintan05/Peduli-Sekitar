package com.alvin.projekuas.ui.main.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Profile;
import com.alvin.projekuas.entity.Report;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailReportActivity extends AppCompatActivity implements View.OnClickListener {

    // widget
    private Toolbar toolbarDetail;
    private CollapsingToolbarLayout detailCollapseToolbar;
    private ImageView imgDetail, imgAvatar;
    private TextView tvDetailTitle, tvDetailDesc, tvDetailUsername, tvDetailDate, tvDetailAlamat;
    private Button btnLokasi;
    private ProgressDialog progressDialog;

    // widget full image dialog
    private Dialog imageDialog;
    private PhotoView photoView;
    private ImageView closeDialog;
    private ProgressBar progressBar;

    // vars
    private FirebaseFirestore db;
    private GeoPoint latlong;
    private Report report;
    private static final String TAG = "DetailReportActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_report);

        // casting
        toolbarDetail = findViewById(R.id.toolbar_detail);
        detailCollapseToolbar = findViewById(R.id.collapse_toolbar_detail);
        imgDetail = findViewById(R.id.img_detail);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailDesc = findViewById(R.id.tv_detail_desc);
        tvDetailAlamat = findViewById(R.id.tv_detail_alamat);
        imgAvatar = findViewById(R.id.img_detail_avatar);
        tvDetailUsername = findViewById(R.id.tv_detail_username);
        tvDetailDate = findViewById(R.id.tv_detail_date);
        btnLokasi = findViewById(R.id.btn_detail_location);
        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Harap Tunggu...");

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(toolbarDetail);
        setUpFullImageDialog();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            getDetailData(extras.getString("path"));
        }

        detailCollapseToolbar.setTitle("Detail Laporan");
        detailCollapseToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        btnLokasi.setOnClickListener(this);
        imgDetail.setOnClickListener(this);
        closeDialog.setOnClickListener(this);
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

    private void getDetailData(String path) {
        progressDialog.show();
        DocumentReference ref = db.document(path);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        report = document.toObject(Report.class);
                        attachDataToView(report);
                        getUserDetail(report.getUser_id());
                        progressDialog.dismiss();
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(DetailReportActivity.this, "Tidak Ada Data", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(DetailReportActivity.this, "Gagal Mengambil Data", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void attachDataToView(Report report) {
        String title = report.getTitle() != null ? report.getTitle() : "Tidak ada data";
        String description = report.getDesc() != null ? report.getDesc() : "Tidak ada data";
        String address = report.getAddress() != null ? report.getAddress() : "Tidak ada data";
        String photo = report.getPhoto() != null ? report.getPhoto() : "Tidak ada data";

        Date userDate = report.getCreated_at().toDate();
        latlong = report.getLatlong();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String convertDate = dateFormat.format(userDate);

        tvDetailTitle.setText(title);
        tvDetailDesc.setText(description);
        tvDetailDate.setText("Dilaporkan pada : " + convertDate);
        tvDetailAlamat.setText(address);

        Glide.with(this)
                .load(photo)
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .into(imgDetail);
    }

    private void getUserDetail(String userId) {
        DocumentReference ref = db.collection("user").document(userId);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Profile profile = document.toObject(Profile.class);
                        attachUserToView(profile);
                    } else {
                        Log.d(TAG, "No such document");
                        Toast.makeText(DetailReportActivity.this, "Tidak Ada Data", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Toast.makeText(DetailReportActivity.this, "Gagal Mengambil Data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void attachUserToView(Profile profile) {
        String avatar = profile.getPhoto() != null ? profile.getPhoto() : "Tidak ada data";
        String username = profile.getName() != null ? profile.getName() : "Tidak ada data";


        tvDetailUsername.setText(username);

        Glide.with(this)
                .load(avatar)
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .apply(RequestOptions.circleCropTransform())
                .into(imgAvatar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_detail_location:
                Intent intent = new Intent(this, DetailMapsActivity.class);
                intent.putExtra("lat", latlong.getLatitude());
                intent.putExtra("long", latlong.getLongitude());
                intent.putExtra("alamat", report.getAddress());
                startActivity(intent);
                break;
            case R.id.img_detail:
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
                .load(report.getPhoto())
                .fitCenter()
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailReportActivity.this, "Harap coba lagi", Toast.LENGTH_SHORT).show();
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
}
