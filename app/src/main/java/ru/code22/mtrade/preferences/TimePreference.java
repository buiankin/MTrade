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

package ru.code22.mtrade.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import java.util.Calendar;

import ru.code22.mtrade.R;

/** A preference for selecting a specific time.
 * The date that the time is picked is always the current day.
 * This is made based on a tutorial from https://medium.com/@JakobUlbrich **/
public class TimePreference extends DialogPreference {
    private boolean currentTimeSet = false;
    private long currentTime;

    public TimePreference(Context context) {
        super(context);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Gets the current time that is selected. **/
    public long getTime() {
        if (currentTimeSet) {
            return currentTime;
        } else {
            return Calendar.getInstance().getTimeInMillis();
        }
    }

    /** Sets the time that is selected. **/
    public void setTime(long time) {
        currentTime = time;
        persistLong(time);
        currentTimeSet = true;
        if (getOnPreferenceChangeListener() != null) {
            getOnPreferenceChangeListener().onPreferenceChange(this, time);
        }
    }

    /** Sets the default value of the TimePicker from the XML. **/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute
        String value = a.getString(index);
        return value == null ? null : Long.parseLong(value);
    }

    /** Sets the initial value of the TimePicker, otherwise defaults to defaultValue. **/
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        long value = getPersistedLong(-1);
        if (value == -1) {
            setTime((long) defaultValue);
        } else {
            setTime(value);
        }
    }

    /** Returns the layout resource of the TimePicker. **/
    @Override
    public int getDialogLayoutResource() {
        return R.layout.com_pcchin_dtpreference_25f3t27tr7jswszj2eeg;
    }
}