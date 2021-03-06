package com.alvin.projekuas.ui.main.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.projekuas.R;
import com.alvin.projekuas.databinding.FragmentHomeBinding;
import com.alvin.projekuas.entity.Report;
import com.alvin.projekuas.ui.main.addpost.AddPostActivity;
import com.alvin.projekuas.ui.main.detail.DetailReportActivity;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HomeFragment extends Fragment implements View.OnClickListener {

    // widget
    private FragmentHomeBinding binding;

    // vars
    private static final String TAG = "HomeFragment";
    public static final int REQUEST_ADD = 100;
    public static final int RESULT_ADD = 101;
    private FirebaseFirestore db;
    private HomeAdapter adapter;
    private CollectionReference reference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        reference = db.collection("report");

        setUpRecyclerView();

        binding.fabAdd.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        Query query = reference.orderBy("created_at", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(query, Report.class)
                .build();

        adapter = new HomeAdapter(options, getActivity());

        binding.rvHome.setHasFixedSize(true);
        binding.rvHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvHome.setAdapter(adapter);

        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String path = documentSnapshot.getReference().getPath();
                Intent intent = new Intent(getActivity(), DetailReportActivity.class);
                intent.putExtra("path", path);
                getActivity().startActivity(intent);
            }
        });
    }

    private void showSnackbar(String message) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(),
                message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                Intent intent = new Intent(getActivity(), AddPostActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_ADD) {
                showSnackbar("Berhasil Menambahkan Laporan");
            }
        }
    }
}