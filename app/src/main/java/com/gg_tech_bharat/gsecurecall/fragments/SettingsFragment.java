package com.gg_tech_bharat.gsecurecall.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.gg_tech_bharat.gsecurecall.R;
import com.gg_tech_bharat.gsecurecall.databinding.FragmentSettingsBinding;
import com.gg_tech_bharat.gsecurecall.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        // 1. Theme
        viewModel.getThemeMode().observe(getViewLifecycleOwner(), theme -> {
            binding.rgTheme.setOnCheckedChangeListener(null);
            switch (theme) {
                case "light":
                    binding.rbThemeLight.setChecked(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":
                    binding.rbThemeDark.setChecked(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "system":
                default:
                    binding.rbThemeSystem.setChecked(true);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            binding.rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_theme_light) {
                    viewModel.setThemeMode("light");
                } else if (checkedId == R.id.rb_theme_dark) {
                    viewModel.setThemeMode("dark");
                } else {
                    viewModel.setThemeMode("system");
                }
            });
        });

        // 2. Allow Credentials
        viewModel.getAllowDeviceCredential().observe(getViewLifecycleOwner(), allowed -> {
            binding.switchAllowCredential.setOnCheckedChangeListener(null);
            binding.switchAllowCredential.setChecked(allowed);
            binding.switchAllowCredential.setOnCheckedChangeListener((btn, isChecked) -> {
                viewModel.setAllowDeviceCredential(isChecked);
            });
        });

        // 3. Require unlock for call type
        viewModel.getRequireUnlockCallType().observe(getViewLifecycleOwner(), type -> {
            binding.rgCallTypes.setOnCheckedChangeListener(null);
            switch (type) {
                case "voice":
                    binding.rbCallVoice.setChecked(true);
                    break;
                case "video":
                    binding.rbCallVideo.setChecked(true);
                    break;
                case "both":
                default:
                    binding.rbCallBoth.setChecked(true);
                    break;
            }
            binding.rgCallTypes.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_call_voice) {
                    viewModel.setRequireUnlockCallType("voice");
                } else if (checkedId == R.id.rb_call_video) {
                    viewModel.setRequireUnlockCallType("video");
                } else {
                    viewModel.setRequireUnlockCallType("both");
                }
            });
        });

        // 4. Auto Start
        viewModel.getAutoStartBoot().observe(getViewLifecycleOwner(), enabled -> {
            binding.switchAutoStart.setOnCheckedChangeListener(null);
            binding.switchAutoStart.setChecked(enabled);
            binding.switchAutoStart.setOnCheckedChangeListener((btn, isChecked) -> {
                viewModel.setAutoStartBoot(isChecked);
            });
        });

        // 5. Haptic Feedback
        viewModel.getHapticFeedback().observe(getViewLifecycleOwner(), enabled -> {
            binding.switchHaptic.setOnCheckedChangeListener(null);
            binding.switchHaptic.setChecked(enabled);
            binding.switchHaptic.setOnCheckedChangeListener((btn, isChecked) -> {
                viewModel.setHapticFeedback(isChecked);
            });
        });

        // 6. Sound Effects
        viewModel.getSoundEffects().observe(getViewLifecycleOwner(), enabled -> {
            binding.switchSound.setOnCheckedChangeListener(null);
            binding.switchSound.setChecked(enabled);
            binding.switchSound.setOnCheckedChangeListener((btn, isChecked) -> {
                viewModel.setSoundEffects(isChecked);
            });
        });
    }

    private void setupListeners() {
        // Observers setup sets up listeners dynamically
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
