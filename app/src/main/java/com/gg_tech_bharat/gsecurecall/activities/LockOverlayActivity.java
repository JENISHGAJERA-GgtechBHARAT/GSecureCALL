package com.gg_tech_bharat.gsecurecall.activities;

import android.content.Context;
import android.content.Intent;
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
import androidx.biometric.BiometricPrompt;

import com.gg_tech_bharat.gsecurecall.databinding.ActivityLockOverlayBinding;
import com.gg_tech_bharat.gsecurecall.helpers.AuthenticationHelper;
import com.gg_tech_bharat.gsecurecall.helpers.PreferenceHelper;
import android.app.KeyguardManager;
import com.gg_tech_bharat.gsecurecall.utils.Animations;
import com.gg_tech_bharat.gsecurecall.utils.Constants;
import com.gg_tech_bharat.gsecurecall.utils.Logger;

public class LockOverlayActivity extends AppCompatActivity {

    private ActivityLockOverlayBinding binding;
    private PreferenceHelper preferenceHelper;
    private KeyguardManager keyguardManager;
    private boolean hasTriggeredAuth = false;
    private BiometricPrompt activePrompt;
    private final android.os.Handler timeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private boolean isUnlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply lock screen flags so the activity shows on top of keyguard
        configureLockScreenFlags();
        
        binding = ActivityLockOverlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceHelper = PreferenceHelper.getInstance(this);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        setupCallerDetails();
        setupVisuals();
        setupListeners();
        
        // Cancel the fullScreenIntent trigger notification immediately so it stays invisible
        com.gg_tech_bharat.gsecurecall.helpers.NotificationHelper.cancelLockNotification(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasTriggeredAuth) {
            hasTriggeredAuth = true;
            // Delay slightly to let the window settle
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::triggerAuthentication, 200);
        }
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
        // Overlay is now fully transparent and visual cards are hidden.
    }

    private void setupListeners() {
        binding.rootOverlay.setOnClickListener(v -> triggerAuthentication());
    }

    private static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL = 1003;

    private void triggerAuthentication() {
        boolean allowCredential = preferenceHelper.isAllowDeviceCredential();
        
        // Cancel any pending timeout
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        
        // Define a 2-second timeout runnable to cancel BiometricPrompt and fallback
        timeoutRunnable = () -> {
            if (!isUnlocked) {
                Logger.d("Face lock timeout (2 seconds). Cancelling prompt and forcing full-screen default lock screen.");
                if (activePrompt != null) {
                    try {
                        activePrompt.cancelAuthentication();
                    } catch (Exception e) {
                        Logger.e("Error cancelling biometric prompt", e);
                    }
                }
                fallbackConfirmCredentials();
            }
        };
        
        activePrompt = AuthenticationHelper.authenticate(this, allowCredential, new AuthenticationHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                isUnlocked = true;
                if (timeoutRunnable != null) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                }
                handleUnlockSuccess();
            }

            @Override
            public void onError(int errorCode, String errString) {
                Logger.w("Overlay auth error: " + errString);
                // Only fallback if the error is not user cancel/back press
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    fallbackDismissKeyguard();
                }
            }

            @Override
            public void onFailed() {
                if (preferenceHelper.isHapticFeedback()) {
                    binding.rootOverlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        });
        
        // Start 2-second countdown
        timeoutHandler.postDelayed(timeoutRunnable, 2000);
    }

    private void fallbackConfirmCredentials() {
        if (keyguardManager != null) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Unlock to Answer",
                    "Verify screen lock to answer call"
            );
            if (intent != null) {
                try {
                    startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL);
                } catch (Exception e) {
                    Logger.e("Failed to launch confirm credential intent", e);
                    fallbackDismissKeyguard();
                }
            } else {
                fallbackDismissKeyguard();
            }
        } else {
            handleUnlockSuccess();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIAL) {
            if (resultCode == RESULT_OK) {
                isUnlocked = true;
                Logger.d("Confirm Device Credential Succeeded");
                handleUnlockSuccess();
            } else {
                Logger.d("Confirm Device Credential Failed/Cancelled");
                if (preferenceHelper.isHapticFeedback()) {
                    binding.rootOverlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                }
            }
        }
    }

    private void fallbackDismissKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    handleUnlockSuccess();
                }

                @Override
                public void onDismissCancelled() {
                    super.onDismissCancelled();
                    if (preferenceHelper.isHapticFeedback()) {
                        binding.rootOverlay.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                }

                @Override
                public void onDismissError() {
                    super.onDismissError();
                    Logger.e("System dismiss keyguard error");
                }
            });
        } else {
            handleUnlockSuccess();
        }
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
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        Animations.stopPulseAnimation(binding.viewPulseCircle);
    }
}
