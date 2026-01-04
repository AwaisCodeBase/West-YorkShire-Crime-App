package com.uni.crimes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.uni.crimes.R;
import com.uni.crimes.model.Crime;

import java.util.ArrayList;
import java.util.List;

public class CrimeAdapter extends RecyclerView.Adapter<CrimeAdapter.CrimeViewHolder> {
    
    private List<Crime> crimes = new ArrayList<>();
    private OnCrimeClickListener listener;
    
    public interface OnCrimeClickListener {
        void onCrimeClick(Crime crime);
    }
    
    public CrimeAdapter(OnCrimeClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public CrimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crime, parent, false);
        return new CrimeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CrimeViewHolder holder, int position) {
        Crime crime = crimes.get(position);
        holder.bind(crime);
    }
    
    @Override
    public int getItemCount() {
        return crimes.size();
    }
    
    public void setCrimes(List<Crime> crimes) {
        this.crimes = crimes;
        notifyDataSetChanged();
    }
    
    class CrimeViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCrimeType, tvLsoaName, tvOutcome;
        private MaterialCardView cardView;
        private View crimeTypeIndicator;
        
        public CrimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCrimeType = itemView.findViewById(R.id.tv_crime_type);
            tvLsoaName = itemView.findViewById(R.id.tv_lsoa_name);
            tvOutcome = itemView.findViewById(R.id.tv_outcome);
            cardView = itemView.findViewById(R.id.card_crime);
            crimeTypeIndicator = itemView.findViewById(R.id.crime_type_indicator);
            
            cardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCrimeClick(crimes.get(position));
                }
            });
        }
        
        public void bind(Crime crime) {
            tvCrimeType.setText(crime.getCrimeType() != null ? crime.getCrimeType() : "Unknown");
            tvLsoaName.setText(crime.getLsoaName() != null ? crime.getLsoaName() : "Unknown location");
            tvOutcome.setText(crime.getOutcome() != null ? crime.getOutcome() : "No outcome recorded");
            
            // Set indicator color based on crime type
            if (crimeTypeIndicator != null) {
                int colorRes = getCrimeTypeColor(crime.getCrimeType());
                crimeTypeIndicator.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), colorRes)
                );
            }
        }
        
        private int getCrimeTypeColor(String crimeType) {
            if (crimeType == null) return R.color.crime_other;
            
            String type = crimeType.toLowerCase();
            if (type.contains("violence") || type.contains("assault") || type.contains("robbery")) {
                return R.color.crime_violent;
            } else if (type.contains("theft") || type.contains("shoplifting")) {
                return R.color.crime_theft;
            } else if (type.contains("burglary")) {
                return R.color.crime_burglary;
            } else if (type.contains("drug")) {
                return R.color.crime_drugs;
            } else {
                return R.color.crime_other;
            }
        }
    }
}
