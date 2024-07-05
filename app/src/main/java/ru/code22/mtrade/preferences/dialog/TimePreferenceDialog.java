/*
 * Copyright 2020 PC Chin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.code22.mtrade.preferences.dialog;

import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Calendar;

import ru.code22.mtrade.R;
import ru.code22.mtrade.preferences.TimePreference;

/** The Fragment used in
 * @see TimePreference to display the time. This should not be used outside of TimePreference. **/
public final class TimePreferenceDialog extends PreferenceDialogFragmentCompat {
    private TimePicker timePicker;
    private boolean timeOverwritten = false;
    private long iniTime;

    /** Creates a new instance of this dialog.
     * @param key The key of the preference selected. **/
    public static TimePreferenceDialog newInstance(String key) {
        final TimePreferenceDialog
                fragment = new TimePreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);

        return fragment;
    }

    /** Sets the initial time shown, which overwrites the value within the SharedPreference. **/
    public TimePreferenceDialog setInitialTime(long time) {
        iniTime = time;
        timeOverwritten = true;
        return this;
    }

    /** Initializes the view and sets up the TimePicker. **/
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        timePicker = view.findViewById(R.id.edit);
        // Exception when there is no TimePicker
        if (timePicker == null) {
            throw new IllegalStateException("Dialog view must contain" +
                    " a TimePicker with id 'edit'");
        }

        // Get the time from the related Preference
        long initialTime;
        DialogPreference preference = getPreference();
        if (timeOverwritten) {
            initialTime = iniTime;
        } else if (preference instanceof TimePreference) {
            initialTime = ((TimePreference) preference).getTime();
        } else {
            initialTime = Calendar.getInstance().getTimeInMillis();
        }

        // Set the time to the TimePicker
        if (initialTime != -1) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(initialTime);
            boolean is24hour = DateFormat.is24HourFormat(getContext());

            timePicker.setIs24HourView(is24hour);
            timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        }
    }

    /** Saves the value as a long SharedPreference. **/
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                Calendar newTime = Calendar.getInstance();
                // getCurrentHour and getCurrentMinute are deprecated in API 23
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    newTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    newTime.set(Calendar.MINUTE, timePicker.getMinute());
                } else {
                    newTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    newTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                }
                ((TimePreference) preference).setTime(newTime.getTimeInMillis());
            }
        }
    }
}