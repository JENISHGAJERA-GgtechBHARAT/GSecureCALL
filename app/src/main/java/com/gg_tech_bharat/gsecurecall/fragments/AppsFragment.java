package com.gg_tech_bharat.gsecurecall.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gg_tech_bharat.gsecurecall.R;
import com.gg_tech_bharat.gsecurecall.adapters.AppsAdapter;
import com.gg_tech_bharat.gsecurecall.databinding.FragmentAppsBinding;
import com.gg_tech_bharat.gsecurecall.databinding.ItemPickerAppBinding;
import com.gg_tech_bharat.gsecurecall.models.ProtectedApplication;
import com.gg_tech_bharat.gsecurecall.viewmodel.AppsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment {

    private FragmentAppsBinding binding;
    private AppsViewModel viewModel;
    private AppsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AppsViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupObservers();
        setupListeners();
    }

    private void setupRecyclerView() {
        binding.rvApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AppsAdapter(requireContext(), new ArrayList<>(), (app, isProtected) -> {
            viewModel.toggleAppProtection(app, isProtected);
            // If toggled off and it was a manually added app, we can refresh lists
            // to make sure it moves back to the "remaining" list if it has no call feature.
            viewModel.loadInstalledApps();
        });
        binding.rvApps.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        viewModel.getAppsList().observe(getViewLifecycleOwner(), apps -> {
            if (apps == null || apps.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvApps.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvApps.setVisibility(View.VISIBLE);
                adapter.updateList(apps);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.rvApps.setVisibility(View.GONE);
                binding.fabAddApp.setEnabled(false);
            } else {
                binding.pbLoading.setVisibility(View.GONE);
                binding.rvApps.setVisibility(View.VISIBLE);
                binding.fabAddApp.setEnabled(true);
            }
        });
    }

    private void setupListeners() {
        binding.fabAddApp.setOnClickListener(v -> showAppPickerDialog());
    }

    private void showAppPickerDialog() {
        List<ProtectedApplication> remainingApps = viewModel.getRemainingAppsList().getValue();
        if (remainingApps == null || remainingApps.isEmpty()) {
            Toast.makeText(requireContext(), "No other applications available", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_app_picker, null);
        RecyclerView rvPicker = dialogView.findViewById(R.id.rv_picker_apps);
        rvPicker.setLayoutManager(new LinearLayoutManager(requireContext()));

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView);

        AlertDialog dialog = builder.create();

        PickerAdapter pickerAdapter = new PickerAdapter(remainingApps, app -> {
            viewModel.toggleAppProtection(app, true);
            Toast.makeText(requireContext(), app.getAppName() + " added to protected list", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            viewModel.loadInstalledApps(); // Refresh both lists
        });

        rvPicker.setAdapter(pickerAdapter);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadInstalledApps();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Inner class Adapter for Picker list ---
    private class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.PickerViewHolder> {

        public interface OnPickerItemClickListener {
            void onItemClick(ProtectedApplication app);
        }

        private final List<ProtectedApplication> list;
        private final OnPickerItemClickListener listener;

        public PickerAdapter(List<ProtectedApplication> list, OnPickerItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public PickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPickerAppBinding itemBinding = ItemPickerAppBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new PickerViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull PickerViewHolder holder, int position) {
            ProtectedApplication app = list.get(position);
            holder.itemBinding.tvPickerName.setText(app.getAppName());

            if (app.getAppIcon() != null) {
                holder.itemBinding.ivPickerIcon.setImageDrawable(app.getAppIcon());
            } else {
                try {
                    holder.itemBinding.ivPickerIcon.setImageDrawable(
                            requireContext().getPackageManager().getApplicationIcon(app.getPackageName())
                    );
                } catch (Exception e) {
                    holder.itemBinding.ivPickerIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(app));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class PickerViewHolder extends RecyclerView.ViewHolder {
            final ItemPickerAppBinding itemBinding;

            public PickerViewHolder(ItemPickerAppBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }
        }
    }
}
