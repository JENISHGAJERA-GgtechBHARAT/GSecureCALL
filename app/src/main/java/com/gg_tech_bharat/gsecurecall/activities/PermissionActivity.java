package com.gg_tech_bharat.gsecurecall.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gg_tech_bharat.gsecurecall.adapters.PermissionAdapter;
import com.gg_tech_bharat.gsecurecall.databinding.ActivityPermissionBinding;
import com.gg_tech_bharat.gsecurecall.helpers.PermissionHelper;
import com.gg_tech_bharat.gsecurecall.models.PermissionItem;
import com.gg_tech_bharat.gsecurecall.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class PermissionActivity extends AppCompatActivity {

    private ActivityPermissionBinding binding;
    private PermissionAdapter adapter;
    private List<PermissionItem> permissionItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.toolbar.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        permissionItems = new ArrayList<>();
        binding.rvPermissions.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new PermissionAdapter(this, permissionItems, item -> {
            handlePermissionAction(item);
        });
        binding.rvPermissions.setAdapter(adapter);
    }

    private void loadPermissionsData() {
        List<PermissionItem> list = new ArrayList<>();
        
        list.add(new PermissionItem(
                Constants.PERM_ACCESSIBILITY,
                "Accessibility Service",
                "Monitors incoming call window state changes to activate lock screen overlay protection.",
                PermissionHelper.isAccessibilityServiceEnabled(this)
        ));
        
        list.add(new PermissionItem(
                Constants.PERM_NOTIFICATION,
                "Notification Listener Access",
                "Required to intercept incoming call notification banners from selected chat apps.",
                PermissionHelper.isNotificationListenerEnabled(this)
        ));
        
        list.add(new PermissionItem(
                Constants.PERM_OVERLAY,
                "Display Over Other Apps",
                "Permits showing the full-screen secure lock on top of active call windows.",
                PermissionHelper.isOverlayPermissionEnabled(this)
        ));
        
        list.add(new PermissionItem(
                Constants.PERM_FOREGROUND,
                "Foreground Notification",
                "Keeps GSecureCALL alive in the background on newer Android releases.",
                PermissionHelper.isForegroundPermissionEnabled(this)
        ));
        
        list.add(new PermissionItem(
                Constants.PERM_BATTERY,
                "Ignore Battery Optimizations",
                "Ensures the system does not kill GSecureCALL to save power.",
                PermissionHelper.isBatteryOptimizationIgnored(this)
        ));

        list.add(new PermissionItem(
                Constants.PERM_BIOMETRIC,
                "Device Lock Configuration",
                "Checks if screen lock (biometrics or pattern/pin/password) is set up.",
                PermissionHelper.isBiometricEnrolled(this)
        ));

        permissionItems = list;
        adapter.updateList(permissionItems);
    }

    private void handlePermissionAction(PermissionItem item) {
        switch (item.getPermissionKey()) {
            case Constants.PERM_ACCESSIBILITY:
                PermissionHelper.openAccessibilitySettings(this);
                break;
            case Constants.PERM_NOTIFICATION:
                PermissionHelper.openNotificationListenerSettings(this);
                break;
            case Constants.PERM_OVERLAY:
                PermissionHelper.openOverlaySettings(this);
                break;
            case Constants.PERM_FOREGROUND:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                } else {
                    Toast.makeText(this, "Permission auto-granted on this version", Toast.LENGTH_SHORT).show();
                }
                break;
            case Constants.PERM_BATTERY:
                PermissionHelper.requestIgnoreBatteryOptimizations(this);
                break;
            case Constants.PERM_BIOMETRIC:
                // Direct user to configure screen lock
                Intent intent = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadPermissionsData();
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPermissionsData();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
