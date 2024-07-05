package ru.code22.mtrade.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.preference.Preference;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.code22.mtrade.R;

// https://github.com/pcchin/dtpreference/blob/main/dtpreference/src/main/java/com/pcchin/dtpreference/DatePreference.java

public class DatePreference extends DialogPreference
        //implements DatePicker.OnDateChangedListener
{
    private boolean currentDateSet = false;
    private Calendar currentDate;

    public DatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DatePreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DatePreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DatePreference(@NonNull Context context) {
        super(context);
    }



    /** Gets the current date that is selected. **/
    public Calendar getDate() {
        if (currentDateSet) {
            return currentDate;
        } else {
            return Calendar.getInstance();
        }
//        try {
//            Date date = formatter().parse(defaultValue());
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
//            return cal;
//        } catch (java.text.ParseException e) {
//            return defaultCalendar();
//        }
    }

    /** Sets the date that is selected. **/
    public void setDate(Calendar date) {
        currentDate = date;
        String dateString=formatter().format(date.getTime());
        persistString(dateString);
        currentDateSet = true;
        setSummary(summaryFormatter().format(date.getTime()));
        if (getOnPreferenceChangeListener() != null) {
            getOnPreferenceChangeListener().onPreferenceChange(this, date);
        }
        //this.dateString = dateString;
    }

    public void setDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        setDate(cal);
    }

    public void setDate(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        setDate(cal);
    }

    public void setDate(String date) {
        try {
            setDate(formatter().parse(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Produces the date formatter used for dates in the XML. The default is yyyy.MM.dd.
     * Override this to change that.
     *
     * @return the SimpleDateFormat used for XML dates
     */
    public static SimpleDateFormat formatter() {
        return new SimpleDateFormat("yyyy.MM.dd");
    }

    /**
     * Produces the date formatter used for showing the date in the summary. The default is MMMM dd, yyyy.
     * Override this to change it.
     *
     * @return the SimpleDateFormat used for summary dates
     */
    public static SimpleDateFormat summaryFormatter() {
        return new SimpleDateFormat("MMMM dd, yyyy");
    }


    /** Gets the default value of the DatePicker from the XML. **/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute
        String value = a.getString(index);
//        if (value != null) {
//            try {
//                return Long.parseLong(value);
//            } catch (NumberFormatException e) {
//                try {
//                    // Раньше данные хранились в таком формате
//                    Date date=new SimpleDateFormat("yyyy.MM.dd").parse(value);
//                    return date.getTime();
//                } catch (ParseException ex) {
//                }
//            }
//        }
//        return null;
        return value==null? null:value;
    }

    /** Sets the initial value of the DatePicker, otherwise defaults to defaultValue. **/
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
//        long value = getPersistedLong(-1);
//        if (value == -1) {
//            setDate((long) defaultValue);
//        } else {
//            setDate(value);
//        }
        String value = getPersistedString("");
        if (value.isEmpty()) {
            value = (String)defaultValue;
        }
        // Раньше данные хранились в таком формате
        Date date= null;
        try {
            date = formatter().parse(value);
            setDate(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

//        if (restoreValue) {
//            this.dateString = getPersistedString(defaultValue());
//            setTheDate(this.dateString);
//        } else {
//            boolean wasNull = this.dateString == null;
//            setDate((String) defaultValue);
//            if (!wasNull)
//                persistDate(this.dateString);
//        }
    }

    /**
     * Called when Android pauses the activity.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        if (isPersistent())
            return super.onSaveInstanceState();
        else
            return new SavedState(super.onSaveInstanceState());
    }

    /**
     * Called when Android restores the activity.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            // try-catch 16.11.2018 потому, что в гугл плее есть ошибка, когда
            // преобразуется AbsSavedState в SavedState
            try {
                setDate(((SavedState) state).dateValue);
            } catch (ClassCastException e) {
            }
        } else {
            SavedState s = (SavedState) state;
            super.onRestoreInstanceState(s.getSuperState());
            setDate(s.dateValue);
        }
    }



    /**
     * The default date to use when the XML does not set it or the XML has an
     * error.
     *
     * @return the Calendar set to the default date
     */
    public static Calendar defaultCalendar() {
        return new GregorianCalendar(1970, 0, 1);
    }

    /**
     * The defaultCalendar() as a string using the {@link #formatter()}.
     *
     * @return a String representation of the default date
     */
    public static String defaultCalendarString() {
        return formatter().format(defaultCalendar().getTime());
    }

//    private String defaultValue() {
//        if (this.dateString == null)
//            setDate(defaultCalendarString());
//        return this.dateString;
//    }

    /**
     * Produces the date the user has selected for the given preference, as a
     * calendar.
     *
     * @param preferences
     *          the SharedPreferences to get the date from
     * @param field
     *          the name of the preference to get the date from
     * @return a Calendar that the user has selected
     */
    public static Calendar getDateFor(SharedPreferences preferences, String field) {
        String dateString=preferences.getString(field, defaultCalendarString());
        Date date = stringToDate(dateString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

//        Long date=preferences.getLong(field, -1);
//        if (date==-1) {
//            return defaultCalendar();
//        } else {
//            Calendar cal = Calendar.getInstance();
//            cal.setTimeInMillis(date);
//            return cal;
//        }
        return cal;
    }

    private static Date stringToDate(String dateString) {
        try {
            return formatter().parse(dateString);
        } catch (ParseException e) {
            return defaultCalendar().getTime();
        }
    }


//    @Override
//    /**
//     * Called when the user changes the date.
//     */
//    public void onDateChanged(DatePicker view, int year, int month, int day) {
//        Calendar selected = new GregorianCalendar(year, month, day);
//        this.changedValueCanBeNull = formatter().format(selected.getTime());
//    }

    private static class SavedState extends Preference.BaseSavedState {
        String dateValue;

        public SavedState(Parcel p) {
            super(p);
            dateValue = p.readString();
        }

        public SavedState(Parcelable p) {
            super(p);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(dateValue);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    /** Returns the layout resource of the DatePicker. **/
    @Override
    public int getDialogLayoutResource() {
        return R.layout.com_pcchin_dtpreference_qz6q5864rndeem9u42ks;
    }
}
