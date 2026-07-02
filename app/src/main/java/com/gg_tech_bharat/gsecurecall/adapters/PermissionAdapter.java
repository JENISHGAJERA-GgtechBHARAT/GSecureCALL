package com.gg_tech_bharat.gsecurecall.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gg_tech_bharat.gsecurecall.databinding.ItemPermissionBinding;
import com.gg_tech_bharat.gsecurecall.models.PermissionItem;

import java.util.List;

public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder> {

    public interface OnPermissionActionClickListener {
        void onPermissionActionClick(PermissionItem item);
    }

    private final Context context;
    private final List<PermissionItem> permissionItems;
    private final OnPermissionActionClickListener actionListener;

    public PermissionAdapter(Context context, List<PermissionItem> permissionItems, OnPermissionActionClickListener actionListener) {
        this.context = context;
        this.permissionItems = permissionItems;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public PermissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPermissionBinding binding = ItemPermissionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PermissionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PermissionViewHolder holder, int position) {
        PermissionItem item = permissionItems.get(position);
        
        holder.binding.tvPermissionTitle.setText(item.getTitle());
        holder.binding.tvPermissionDesc.setText(item.getDescription());
        
        int statusColor;
        String statusText;
        
        if (item.isGranted()) {
            statusColor = ContextCompat.getColor(context, android.R.color.holo_green_dark);
            statusText = "Granted";
            holder.binding.btnGrant.setVisibility(View.GONE);
        } else {
            statusColor = ContextCompat.getColor(context, android.R.color.holo_red_dark);
            statusText = "Required";
            holder.binding.btnGrant.setVisibility(View.VISIBLE);
        }
        
        // Dynamically create circular shape for status indicator
        GradientDrawable indicator = new GradientDrawable();
        indicator.setShape(GradientDrawable.OVAL);
        indicator.setColor(statusColor);
        holder.binding.viewStatusIndicator.setBackground(indicator);
        
        holder.binding.tvStatusText.setText(statusText);
        holder.binding.tvStatusText.setTextColor(statusColor);

        holder.binding.btnGrant.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onPermissionActionClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return permissionItems.size();
    }

    public void updateList(List<PermissionItem> newList) {
        permissionItems.clear();
        permissionItems.addAll(newList);
        notifyDataSetChanged();
    }

    static class PermissionViewHolder extends RecyclerView.ViewHolder {
        final ItemPermissionBinding binding;

        PermissionViewHolder(ItemPermissionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
