package com.gg_tech_bharat.gsecurecall.helpers;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import com.gg_tech_bharat.gsecurecall.utils.Logger;

import java.util.concurrent.Executor;

public class AuthenticationHelper {

    public interface AuthCallback {
        void onSuccess();
        void onError(int errorCode, String errString);
        void onFailed();
    }

    public static void authenticate(@NonNull FragmentActivity activity, boolean allowDeviceCredential, @NonNull AuthCallback callback) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Logger.d("Auth Error: " + errorCode + " - " + errString);
                callback.onError(errorCode, errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Logger.d("Auth Succeeded");
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Logger.d("Auth Failed");
                callback.onFailed();
            }
        });

        BiometricPrompt.PromptInfo.Builder promptBuilder = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock to Answer")
                .setSubtitle("Verify lock to answer call")
                .setConfirmationRequired(false);

        if (allowDeviceCredential) {
            // If device credential fallback is allowed, do NOT set negative button text
            // as required by Android BiometricPrompt API.
            int authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
                    | androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
                    | androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;
            promptBuilder.setAllowedAuthenticators(authenticators);
        } else {
            // Biometric only requires negative button text
            int authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
                    | androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
            promptBuilder.setAllowedAuthenticators(authenticators);
            promptBuilder.setNegativeButtonText("Cancel");
        }

        try {
            biometricPrompt.authenticate(promptBuilder.build());
        } catch (Exception e) {
            Logger.e("Failed to launch BiometricPrompt", e);
            callback.onError(-1, e.getMessage());
        }
    }
}
