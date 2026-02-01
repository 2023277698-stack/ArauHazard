package com.example.arauhazard;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import org.json.JSONObject;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<JSONObject> reports;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int id);
    }

    public ReportAdapter(List<JSONObject> reports, OnDeleteClickListener listener) {
        this.reports = reports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        JSONObject report = reports.get(position);
        try {
            int id = report.getInt("id");
            String hazardType = report.optString("hazard_type", "Unknown Hazard");
            String reporterName = report.optString("name", "Anonymous");
            String imagePath = report.optString("image_path", "");

            holder.tvType.setText(hazardType);
            holder.tvReporter.setText("By: " + reporterName);

            if (!imagePath.isEmpty()) {
                holder.imgHazard.setVisibility(View.VISIBLE);
                String imageUrl = "http://10.0.2.2/streetsense/uploads/" + imagePath;

                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .centerCrop()
                        .into(holder.imgHazard);

                // --- ADDED: Click to Zoom Logic ---
                holder.imgHazard.setOnClickListener(v -> showFullImage(holder.itemView.getContext(), imageUrl));

            } else {
                holder.imgHazard.setVisibility(View.GONE);
            }

            holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(id));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ADDED: Full Screen Dialog Helper ---
    private void showFullImage(Context context, String url) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image); // Ensure you created this XML!

        ImageView fullImg = dialog.findViewById(R.id.imgFull);
        Button btnClose = dialog.findViewById(R.id.btnCloseFull);

        Glide.with(context).load(url).into(fullImg);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvReporter;
        ImageView imgHazard;
        Button btnDelete;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvAdminType);
            tvReporter = itemView.findViewById(R.id.tvAdminReporter);
            imgHazard = itemView.findViewById(R.id.imgAdminHazard);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}