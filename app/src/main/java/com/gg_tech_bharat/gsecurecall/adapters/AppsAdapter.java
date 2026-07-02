package com.gg_tech_bharat.gsecurecall.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gg_tech_bharat.gsecurecall.databinding.ItemAppBinding;
import com.gg_tech_bharat.gsecurecall.models.ProtectedApplication;

import java.util.ArrayList;
import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    public interface OnAppProtectChangeListener {
        void onAppProtectChanged(ProtectedApplication app, boolean isProtected);
    }

    private final Context context;
    private final List<ProtectedApplication> allAppsList;
    private final List<ProtectedApplication> displayedList;
    private final OnAppProtectChangeListener changeListener;

    public AppsAdapter(Context context, List<ProtectedApplication> apps, OnAppProtectChangeListener changeListener) {
        this.context = context;
        this.allAppsList = apps;
        this.displayedList = new ArrayList<>(apps);
        this.changeListener = changeListener;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppBinding binding = ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        ProtectedApplication app = displayedList.get(position);
        
        holder.binding.tvAppName.setText(app.getAppName());
        holder.binding.tvAppPackage.setText(app.getPackageName());
        
        // Dynamic loading of icon to avoid memory leaks
        if (app.getAppIcon() != null) {
            holder.binding.ivAppIcon.setImageDrawable(app.getAppIcon());
        } else {
            try {
                holder.binding.ivAppIcon.setImageDrawable(context.getPackageManager().getApplicationIcon(app.getPackageName()));
            } catch (Exception e) {
                holder.binding.ivAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }

        // Remove listener before setting checked state to avoid infinite callbacks
        holder.binding.switchProtect.setOnCheckedChangeListener(null);
        holder.binding.switchProtect.setChecked(app.isProtected());
        
        holder.binding.switchProtect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            app.setProtected(isChecked);
            if (changeListener != null) {
                changeListener.onAppProtectChanged(app, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayedList.size();
    }

    public void updateList(List<ProtectedApplication> newList) {
        allAppsList.clear();
        allAppsList.addAll(newList);
        filter("");
    }

    public void filter(String query) {
        displayedList.clear();
        if (query == null || query.trim().isEmpty()) {
            displayedList.addAll(allAppsList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (ProtectedApplication app : allAppsList) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) || 
                    app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    displayedList.add(app);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        final ItemAppBinding binding;

        AppViewHolder(ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
