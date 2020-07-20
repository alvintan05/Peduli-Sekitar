package com.alvin.projekuas.ui.main.myreport;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Report;
import com.alvin.projekuas.ui.main.detail.DetailReportActivity;
import com.alvin.projekuas.utils.Preference;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyReportFragment extends Fragment {

    // widget
    private RecyclerView rvReport;

    // vars
    private static final String TAG = "MyReportFragment";
    private FirebaseFirestore db;
    private MyReportAdapter adapter;
    private CollectionReference reference;
    private Preference preference;

    public MyReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvReport = view.findViewById(R.id.rv_report);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        preference = new Preference(getActivity());

        db = FirebaseFirestore.getInstance();
        reference = db.collection("report");

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        Query query = reference
                .whereEqualTo("user_id", preference.getUserId())
                .orderBy("created_at", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
                .setQuery(query, Report.class)
                .build();

        adapter = new MyReportAdapter(options, getActivity());

        rvReport.setHasFixedSize(true);
        rvReport.setLayoutManager(new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false));
        rvReport.setAdapter(adapter);

        adapter.setOnItemClickListener(new MyReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String path = documentSnapshot.getReference().getPath();
                Intent intent = new Intent(getActivity(), DetailReportActivity.class);
                intent.putExtra("path", path);
                getActivity().startActivity(intent);
            }

            @Override
            public void onLongItemClick(String path, int position) {
                showDeleteDialog(path);
            }
        });
    }

    private void showDeleteDialog(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle("Anda ingin menghapus laporan?")
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteData(path);
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void deleteData(String path) {
        db.document(path)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Berhasil menghapus laporan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Gagal menghapus laporan", Toast.LENGTH_SHORT).show();
                    }
                });
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


}
