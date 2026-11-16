package com.samherbst.weighttracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.WeightViewHolder> {

    private List<WeightEntry> weightList;
    private OnItemClickListener listener;

    // Interface for delete button clicks
    public interface OnItemClickListener {
        void onDeleteClick(WeightEntry entry);
    }

    public WeightAdapter(List<WeightEntry> weightList, OnItemClickListener listener) {
        this.weightList = weightList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weight_item, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightEntry entry = weightList.get(position);

        // Format date for display
        holder.dateText.setText(formatDate(entry.getDate()));

        // Format weight with one decimal place
        holder.weightText.setText(String.format(Locale.US, "%.1f", entry.getWeight()));

        // Set delete button click listener
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    /**
     * Format date string from yyyy-MM-dd to readable format
     */
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString; // Return original if parsing fails
        }
    }

    /**
     * Update the dataset and refresh the RecyclerView
     */
    public void updateData(List<WeightEntry> newWeightList) {
        this.weightList = newWeightList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for each grid row
     */
    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView weightText;
        ImageButton deleteButton;

        public WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            weightText = itemView.findViewById(R.id.weightText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
