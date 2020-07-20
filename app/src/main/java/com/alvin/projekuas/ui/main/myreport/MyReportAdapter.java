package com.alvin.projekuas.ui.main.myreport;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Report;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class MyReportAdapter extends FirestoreRecyclerAdapter<Report, MyReportAdapter.ReportViewHolder> {

    private Context context;
    private OnItemClickListener listener;

    public MyReportAdapter(@NonNull FirestoreRecyclerOptions<Report> options, Context c) {
        super(options);
        context = c;
    }

    @Override
    protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull Report model) {
        Glide.with(context)
                .load(model.getPhoto())
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .centerCrop()
                .into(holder.imgReport);
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {

        ImageView imgReport;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            imgReport = itemView.findViewById(R.id.img_item_report);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onLongItemClick(getSnapshots().getSnapshot(position).getReference().getPath(), position);
                    }
                    return true;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);

        void onLongItemClick(String path, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
