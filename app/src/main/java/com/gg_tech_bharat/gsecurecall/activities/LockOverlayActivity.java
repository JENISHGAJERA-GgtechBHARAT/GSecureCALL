package com.gg_tech_bharat.gsecurecall.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.gg_tech_bharat.gsecurecall.databinding.ActivityLockOverlayBinding;
import com.gg_tech_bharat.gsecurecall.helpers.AuthenticationHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import com.gg_tech_bharat.gsecurecall.utils.Animations;
import com.gg_tech_bharat.gsecurecall.utils.Constants;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class LockOverlayActivity extends AppCompatActivity {

    private ActivityLockOverlayBinding binding;
    private PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply lock screen flags so the activity shows on top of keyguard
        configureLockScreenFlags();
        
        binding = ActivityLockOverlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceHelper = PreferenceHelper.getInstance(this);

        setupCallerDetails();
        setupVisuals();
        setupListeners();
        
        // Auto-launch biometric authentication on start
        triggerAuthentication();
    }

    private void configureLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void setupCallerDetails() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String packageName = extras.getString(Constants.EXTRA_CALL_PACKAGE, "");
            String callerName = extras.getString(Constants.EXTRA_CALLER_NAME, "Incoming Call");
            boolean isVideo = extras.getBoolean(Constants.EXTRA_IS_VIDEO, false);

            binding.tvOverlayCallerName.setText(callerName);
            binding.tvOverlayCallType.setText(isVideo ? "Incoming Video Call" : "Incoming Voice Call");

            // Set app icon and name if package is provided
            if (!packageName.isEmpty()) {
                try {
                    String appLabel = getPackageManager().getApplicationLabel(
                            getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    ).toString();
                    binding.tvOverlayAppName.setText(appLabel);
                } catch (Exception e) {
                    binding.tvOverlayAppName.setText(packageName);
                }

                try {
                    binding.ivOverlayAppIcon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
                } catch (Exception e) {
                    binding.ivOverlayAppIcon.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } else {
                binding.tvOverlayAppName.setText("Call Protection");
            }
        }
    }

    private void setupVisuals() {
        // Create circle shape for pulse background
        GradientDrawable pulseShape = new GradientDrawable();
        pulseShape.setShape(GradientDrawable.OVAL);
        pulseShape.setColor(0x22FFFFFF);
        binding.viewPulseCircle.setBackground(pulseShape);

        // Slide up animation on the card
        Animations.slideUp(binding.cardCallerInfo, 600);
        
        // Start pulse animation on the ripple container
        Animations.startPulseAnimation(binding.viewPulseCircle);
    }

    private void setupListeners() {
        binding.btnUnlock.setOnClickListener(v -> triggerAuthentication());
        binding.layoutRipple.setOnClickListener(v -> triggerAuthentication());
    }

    private void triggerAuthentication() {
        boolean allowCredential = preferenceHelper.isAllowDeviceCredential();
        
        AuthenticationHelper.authenticate(this, allowCredential, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                handleUnlockSuccess();
            }

            @Override
            public void onError(int errorCode, String errString) {
                Logger.w("Overlay auth error: " + errString);
            }

            @Override
            public void onFailed() {
                // Haptic feedback on fail
                if (preferenceHelper.isHapticFeedback()) {
                    binding.rootOverlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        });
    }

    private void handleUnlockSuccess() {
        // 1. Sound feedback
        if (preferenceHelper.isSoundEffects()) {
            try {
                ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
                toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 150);
            } catch (Exception e) {
                Logger.e("Tone error", e);
            }
        }

        // 2. Haptic feedback
        if (preferenceHelper.isHapticFeedback()) {
            binding.rootOverlay.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }

        // 3. Stop animations & close overlay
        Animations.stopPulseAnimation(binding.viewPulseCircle);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back button. User must authenticate to dismiss this overlay activity.
        // Doing nothing prevents system back action.
        Logger.d("Back button pressed on lock screen overlay - ignored");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Animations.stopPulseAnimation(binding.viewPulseCircle);
    }
}
