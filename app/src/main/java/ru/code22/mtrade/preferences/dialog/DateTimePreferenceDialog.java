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
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Calendar;

import ru.code22.mtrade.R;
import ru.code22.mtrade.preferences.DateTimePreference;

public final class DateTimePreferenceDialog extends PreferenceDialogFragmentCompat {
    private boolean dateSelected;
    private boolean dateOverwritten = false;
    private long iniDate;

    // Variables for selecting date
    private DatePicker datePicker;
    private long minDate = -1, maxDate = -1;

    // Variables for selecting time
    private TimePicker timePicker;
    private int year, month, day;

    /** Creates a new instance of this dialog.
     * @param key The key of the preference selected. **/
    public static DateTimePreferenceDialog newInstance(String key) {
        final DateTimePreferenceDialog fragment = new DateTimePreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        fragment.dateSelected = false;
        return fragment;
    }

    /** Creates a new instance of this dialog with a minimum date and maximum date respectively.
     * @param key The key of the preference selected.
     * @param min The minimum date for the DatePicker. Set to -1 for it to be ignored.
     * @param max The maximum date for the DatePicker. Set to -1 for it to be ignored. **/
    public static DateTimePreferenceDialog newInstance(String key, long min, long max) {
        final DateTimePreferenceDialog fragment = new DateTimePreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        fragment.dateSelected = false;
        fragment.minDate = min;
        fragment.maxDate = max;
        return fragment;
    }

    /** Creates a new instance of this dialog with a set year, month and day respectively.
     * This should only be used after a date has been selected by the user.
     * @param key The key of the preference selected.
     * @param year The year selected by the user.
     * @param month The month selected by the user.
     * @param day The day within the month selected by the user. **/
    private static DateTimePreferenceDialog newInstance(String key, int year, int month, int day) {
        final DateTimePreferenceDialog fragment = new DateTimePreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        fragment.dateSelected = true;
        fragment.year = year;
        fragment.month = month;
        fragment.day = day;
        return fragment;
    }

    /** Sets the initial date and time shown, which overwrites the value within the SharedPreference. **/
    public DateTimePreferenceDialog setInitialDateTime(long dateTime) {
        iniDate = dateTime;
        dateOverwritten = true;
        return this;
    }

    /** Initializes the view and sets up the DatePicker. **/
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        // Get the time from the related Preference
        long initialTime;
        DialogPreference preference = getPreference();
        if (dateOverwritten) {
            initialTime = iniDate;
        } else if (preference instanceof DateTimePreference) {
            initialTime = ((DateTimePreference) preference).getDateTime();
        } else {
            initialTime = Calendar.getInstance().getTimeInMillis();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(initialTime);
        if (dateSelected) {
            timePicker = view.findViewById(R.id.edit);
            // Set the initial time for the TimePicker
            boolean is24hour = DateFormat.is24HourFormat(getContext());
            timePicker.setIs24HourView(is24hour);
            // setCurrentHour and setCurrentMinute are deprecated in API 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                timePicker.setHour(calendar.get(Calendar.HOUR));
                timePicker.setMinute(calendar.get(Calendar.MINUTE));
            } else {
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }
        } else {
            datePicker = view.findViewById(R.id.edit);
            // Set the initial date to the DatePicker
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            if (minDate != -1) datePicker.setMinDate(minDate);
            if (maxDate != -1) datePicker.setMaxDate(maxDate);
        }
    }

    /** Saves the value as a long SharedPreference. **/
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            if (preference instanceof DateTimePreference) {
                if (dateSelected) {
                    // 2nd Popup (TimePicker)
                    Calendar newTime = Calendar.getInstance();
                    // getCurrentHour and getCurrentMinute are deprecated in API 23
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        newTime.set(year, month, day,
                                timePicker.getHour(), timePicker.getMinute(), 0);
                    } else {
                        newTime.set(year, month, day,
                                timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
                    }
                    ((DateTimePreference) preference).setDateTime(newTime.getTimeInMillis());
                    // Resets the preference
                    ((DateTimePreference) preference).updateDateSelected(false);
                } else if (getFragmentManager() != null) {
                    // First Popup (DatePicker)
                    // Creates another dialog
                    ((DateTimePreference) preference).updateDateSelected(true);
                    DateTimePreferenceDialog newDialog = DateTimePreferenceDialog.newInstance(
                            preference.getKey(), datePicker.getYear(),
                            datePicker.getMonth(), datePicker.getDayOfMonth());
                    newDialog.setTargetFragment(getTargetFragment(), 0);
                    newDialog.show(getFragmentManager(), getTag());
                }
            }
        }
    }
}