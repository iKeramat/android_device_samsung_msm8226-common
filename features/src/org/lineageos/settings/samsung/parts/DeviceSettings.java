/*
 * Copyright (C) 2018 The Xiaomi-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.lineageos.settings.samsung.parts;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.lineageos.settings.samsung.parts.kcal.KCalSettingsActivity;
import org.lineageos.settings.samsung.parts.preferences.SecureSettingCustomSeekBarPreference;
import org.lineageos.settings.samsung.parts.preferences.SecureSettingListPreference;
import org.lineageos.settings.samsung.parts.preferences.SecureSettingSwitchPreference;
import org.lineageos.settings.samsung.parts.preferences.VibrationSeekBarPreference;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    // Vibration
    public static final String PREF_VIBRATION_STRENGTH = "vibration_strength";
    public static final String VIBRATION_STRENGTH_PATH = "/sys/devices/virtual/timed_output/vibrator/pwm_value";
    public static final int MIN_VIBRATION = 0;
    public static final int MAX_VIBRATION = 100;

    // Display
    private static final String CATEGORY_DISPLAY = "display";
    private static final String PREF_DEVICE_DOZE = "device_doze";
    private static final String PREF_DEVICE_KCAL = "device_kcal";
    private static final String DEVICE_DOZE_PACKAGE_NAME = "org.lineageos.settings.doze";

    // Torch
    public static final String PREF_TORCH_BRIGHTNESS = "torch_brightness";
    public static final String TORCH_BRIGHTNESS_PATH = "/sys/devices/leds-qpnp-41/leds/led:flash_torch/max_brightness";

    // OTG Enabler
    public static final String PREF_OTG_ENABLER = "otg_enabler";
    public static final String OTG_ENABLER_PATH = "/sys/kernel/debug/regulator/8226_smbbp_otg/enable";

    // onCreatePreferences
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_samsung_parts, rootKey);


        SecureSettingCustomSeekBarPreference TorchBrightness = (SecureSettingCustomSeekBarPreference) findPreference(PREF_TORCH_BRIGHTNESS);
        TorchBrightness.setEnabled(FileUtils.fileWritable(TORCH_BRIGHTNESS_PATH));
        TorchBrightness.setOnPreferenceChangeListener(this);

        VibrationSeekBarPreference vibrationStrength = (VibrationSeekBarPreference) findPreference(PREF_VIBRATION_STRENGTH);
        vibrationStrength.setEnabled(FileUtils.fileWritable(VIBRATION_STRENGTH_PATH));
        vibrationStrength.setOnPreferenceChangeListener(this);

        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);
        if (isAppNotInstalled(DEVICE_DOZE_PACKAGE_NAME)) {
            displayCategory.removePreference(findPreference(PREF_DEVICE_DOZE));
        }

        Preference kcal = findPreference(PREF_DEVICE_KCAL);

        kcal.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), KCalSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        if (FileUtils.fileWritable(OTG_ENABLER_PATH)) {
            SecureSettingSwitchPreference otg_enabler = (SecureSettingSwitchPreference) findPreference(PREF_OTG_ENABLER);
            otg_enabler.setChecked(FileUtils.getFileValueAsBoolean(OTG_ENABLER_PATH, false));
            otg_enabler.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PREF_OTG_ENABLER));
        }
    }

    // onPreferenceChange
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_TORCH_BRIGHTNESS:
                FileUtils.setValue(TORCH_BRIGHTNESS_PATH, (int) value);
                break;

            case PREF_VIBRATION_STRENGTH:
                double vibrationValue = (int) value / 100.0 * (MAX_VIBRATION - MIN_VIBRATION) + MIN_VIBRATION;
                FileUtils.setValue(VIBRATION_STRENGTH_PATH, vibrationValue);
                break;

            case PREF_OTG_ENABLER:
                FileUtils.setValue(OTG_ENABLER_PATH, (boolean) value);
                break;

            default:
                break;
        }
        return true;
    }

    // isAppNotInstalled
    private boolean isAppNotInstalled(String uri) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
