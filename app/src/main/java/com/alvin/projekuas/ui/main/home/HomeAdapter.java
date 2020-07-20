package com.alvin.projekuas.ui.main.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alvin.projekuas.R;
import com.alvin.projekuas.entity.Report;
import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;

public class HomeAdapter extends FirestoreRecyclerAdapter<Report, HomeAdapter.HomeViewHolder> {

    private Context context;
    private OnItemClickListener listener;

    public HomeAdapter(@NonNull FirestoreRecyclerOptions<Report> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull HomeViewHolder holder, int position, @NonNull Report model) {
        holder.tvTitle.setText(model.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String convertDate = dateFormat.format(model.getCreated_at().toDate());
        holder.tvDate.setText(convertDate);

        Glide.with(context)
                .load(model.getPhoto())
                .error(R.drawable.noimage)
                .fallback(R.drawable.noimage)
                .into(holder.imgReport);

    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new HomeViewHolder(view);
    }

    class HomeViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDate;
        ImageView imgReport;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvDate = itemView.findViewById(R.id.tv_item_date);
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
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}