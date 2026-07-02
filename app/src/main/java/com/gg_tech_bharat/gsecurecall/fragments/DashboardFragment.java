package com.gg_tech_bharat.gsecurecall.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.gg_tech_bharat.gsecurecall.R;
import com.gg_tech_bharat.gsecurecall.activities.AppSelectionActivity;
import com.gg_tech_bharat.gsecurecall.activities.PermissionActivity;
import com.gg_tech_bharat.gsecurecall.activities.SettingsActivity;
import com.gg_tech_bharat.gsecurecall.databinding.FragmentDashboardBinding;
import com.gg_tech_bharat.gsecurecall.viewmodel.MainViewModel;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // 1. Protection State
        viewModel.getIsProtectionEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            binding.switchMainProtection.setOnCheckedChangeListener(null);
            binding.switchMainProtection.setChecked(isEnabled);
            
            if (isEnabled) {
                binding.tvStatusTitle.setText("Protection Active");
                binding.tvStatusSubtitle.setText("Your calls are secured");
                binding.cardMainStatus.setCardBackgroundColor(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.color_active_green_container)
                ));
                binding.tvStatusTitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.color_active_green_text)
                );
                binding.tvStatusSubtitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.color_active_green_text)
                );
            } else {
                binding.tvStatusTitle.setText("Protection Paused");
                binding.tvStatusSubtitle.setText("Secure call lock is disabled");
                binding.cardMainStatus.setCardBackgroundColor(ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.color_disabled_gray_container)
                ));
                binding.tvStatusTitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.color_disabled_gray_text)
                );
                binding.tvStatusSubtitle.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.color_disabled_gray_text)
                );
            }
            
            binding.switchMainProtection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                viewModel.setProtectionEnabled(isChecked);
            });
        });

        // 2. Protected App Count
        viewModel.getProtectedAppsCount().observe(getViewLifecycleOwner(), count -> {
            binding.tvProtectedCount.setText(String.valueOf(count));
        });

        // 3. Permission Health Status
        viewModel.getAllPermissionsGranted().observe(getViewLifecycleOwner(), allGranted -> {
            if (allGranted) {
                binding.tvPermissionsStatus.setText("All Configured");
                binding.tvPermissionsStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            } else {
                binding.tvPermissionsStatus.setText("Action Required");
                binding.tvPermissionsStatus.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });

        // 4. Last Activity Log
        viewModel.getLastActivity().observe(getViewLifecycleOwner(), log -> {
            binding.tvLastActivity.setText(log);
        });
    }

    private void setupListeners() {
        binding.switchMainProtection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setProtectionEnabled(isChecked);
        });

        binding.btnManageApps.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AppSelectionActivity.class));
        });

        binding.btnPermissionSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PermissionActivity.class));
        });

        binding.btnAppSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SettingsActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refreshState();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
