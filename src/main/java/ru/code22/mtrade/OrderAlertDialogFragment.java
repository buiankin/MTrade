package ru.code22.mtrade;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

// http://developer.android.com/reference/android/app/DialogFragment.html
// http://developer.android.com/guide/topics/ui/dialogs.html#PassingEvents

public class OrderAlertDialogFragment extends DialogFragment {

    static final int DIALOG_TYPE_DATE=1;
    static final int DIALOG_TYPE_TIME=2;

    private AlertDialogFragmentEventListener mListener;

    int m_idd_type;
    int m_dialog_type;
    int m_day;
    int m_month;
    int m_year;
    int m_hourOfDay;
    int m_minute;

    public interface AlertDialogFragmentEventListener {
        public void onButtonsOkCancel_Ok(Integer dialogId, Intent intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("dialogId", m_idd_type);
        outState.putInt("dialogType", m_dialog_type);
        outState.putInt("day", m_day);
        outState.putInt("month", m_month);
        outState.putInt("year", m_year);
        outState.putInt("hour", m_hourOfDay);
        outState.putInt("minute", m_minute);
    }

    // Override the Fragment.onAttach() method to instantiate the AlertDialogFragmentEventListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AlertDialogFragmentEventListener so we can send events to the host
            mListener = (AlertDialogFragmentEventListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement AlertDialogFragmentEventListener");
        }
    }

    /*
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
    */

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            if (view.isShown()) // Баг-фикс. Потому, что при отмене это тоже срабатывает
                // а также при повороте экрана)))
            {
                Intent result=new Intent();
                result.putExtra("year", selectedYear);
                result.putExtra("month", selectedMonth);
                result.putExtra("day", selectedDay);
                mListener.onButtonsOkCancel_Ok(m_idd_type, result);
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
            if (view.isShown())
            {
                Intent result=new Intent();
                result.putExtra("hour", selectedHourOfDay);
                result.putExtra("minute", selectedMinute);
                mListener.onButtonsOkCancel_Ok(m_idd_type, result);
            }
        }
    };


	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState==null)
        {
            savedInstanceState=getArguments();
        }
        m_idd_type = savedInstanceState.getInt("dialogId");
        m_dialog_type = savedInstanceState.getInt("dialogType");
        if (savedInstanceState.containsKey("day"))
        {
            m_day = savedInstanceState.getInt("day");
            m_month = savedInstanceState.getInt("month");
            m_year = savedInstanceState.getInt("year");
        } else
        {
            m_day=0;
            m_month=0;
            m_year=0;
        }
        if (savedInstanceState.containsKey("hour")) {
            m_hourOfDay = savedInstanceState.getInt("hour");
            m_minute = savedInstanceState.getInt("minute");
        } else
        {
            m_hourOfDay = 0;
            m_minute = 0;
        }

        switch (m_dialog_type)
        {
            case DIALOG_TYPE_DATE:
                return new DatePickerDialog(getActivity(), datePickerListener,  m_year, m_month, m_day);
                //return new DatePickerDialog(getActivity(), datePickerListener,
                //        m_year, m_month, m_day);
            case DIALOG_TYPE_TIME:
                return new TimePickerDialog(getActivity(), timePickerListener,
                     m_hourOfDay, m_minute, true);

        }
        return super.onCreateDialog(savedInstanceState);

		/*
        int idd_type = getArguments().getInt("IDD");
        //builder = new AlertDialog.Builder(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		  OrderRecord rec=MyDatabase.m_order_editing;
			switch (idd_type) {
			case OrderActivity.IDD_DATE:
			{
				int day=Integer.parseInt(rec.datedoc.substring(6,8));
				int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
				int year=Integer.parseInt(rec.datedoc.substring(0,4));
			    // set date picker as current date
				dialog = new DatePickerDialog(getActivity(), datePickerListener, 
	                         year, month,day);
				break;
			}
			case OrderActivity.IDD_SHIPPING_BEGIN_TIME:
			{
				int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
				dialog = new TimePickerDialog(getActivity(), timePickerListenerBeginTime, 
	                         hourOfDay, minute, true);
				break;
			}
			case OrderActivity.IDD_SHIPPING_END_TIME:
			{
				int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
				dialog = new TimePickerDialog(getActivity(), timePickerListenerEndTime, 
	                         hourOfDay, minute, true);
				break;
			}
			case OrderActivity.IDD_CLEAR_LINES:
			{
				// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! баг
				builder.setMessage(getResources().getString(R.string.question_delete_lines));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						MyDatabase.m_order_editing.lines.removeAllElements();
						MyDatabase.m_linesAdapter.notifyDataSetChanged();
						setModified();
						redrawSumWeight();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				builder.setCancelable(false);
				break;
			}
			case OrderActivity.IDD_CANT_SAVE:
			{
				builder.setMessage(getResources().getString(R.string.order_cant_be_saved));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				builder.setCancelable(false);
				break;
			}
			}
       //return new AlertDialog.Builder(getActivity()).setTitle(title).create();
		if (dialog!=null)
			return dialog;
		return builder.create();
    }
    */
		
}
	
}
