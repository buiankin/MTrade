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

/** A preference for selecting a specific date and time.
 * The DatePicker dialog will show first, followed by the TimePicker dialog. **/
public class DateTimePreference extends DialogPreference {
    private boolean currentDateTimeSet = false;
    private long currentDateTime;
    private boolean dateSelected = false;

    public DateTimePreference(Context context) {
        super(context);
    }

    public DateTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateTimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DateTimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Gets the current date and time that is selected. **/
    public long getDateTime() {
        if (currentDateTimeSet) {
            return currentDateTime;
        } else {
            return Calendar.getInstance().getTimeInMillis();
        }
    }

    /** Sets the date and time that is selected. **/
    public void setDateTime(long dateTime) {
        currentDateTime = dateTime;
        persistLong(dateTime);
        currentDateTimeSet = true;
        if (getOnPreferenceChangeListener() != null) {
            getOnPreferenceChangeListener().onPreferenceChange(this, dateTime);
        }
    }

    /** Sets whether the date has been selected by the Fragment.
     * This should not be used outside of
     * @see com.pcchin.dtpreference.dialog.DateTimePreferenceDialog **/
    public void updateDateSelected(boolean dateSelected) {
        this.dateSelected = dateSelected;
    }

    /** Gets the default value of the DatePicker from the XML.
     * The default value of the TimePicker could not be gotten from the XML. **/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute
        String value = a.getString(index);
        return value == null ? null : Long.parseLong(value);
    }

    /** Sets the initial value of the DatePicker, otherwise defaults to defaultValue.
     * The default value of the TimePicker could not be set from the XML. **/
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        long value = getPersistedLong(-1);
        if (value == -1) {
            setDateTime((long) defaultValue);
        } else {
            setDateTime(value);
        }
    }

    /** Returns the layout resource of the DatePicker.
     * The TimePicker will be shown after the DatePicker is shown. **/
    @Override
    public int getDialogLayoutResource() {
        if (dateSelected) {
            return R.layout.com_pcchin_dtpreference_25f3t27tr7jswszj2eeg;
        } else {
            return R.layout.com_pcchin_dtpreference_qz6q5864rndeem9u42ks;
        }
    }
}