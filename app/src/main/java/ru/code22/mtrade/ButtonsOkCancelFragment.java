package ru.code22.mtrade;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ButtonsOkCancelFragment extends Fragment {
	
	private OnButtonsOkCancelInteractionListener mListener;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.buttons_ok_cancel_fragment, container, false);
		
		Button btnOk=(Button)view.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onButtonsOkCancel_Ok();
				
			}
		});
		
		Button btnCancel=(Button)view.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onButtonsOkCancel_Cancel();
				
			}
		});
		
		return view;
		
	}
	
	public interface OnButtonsOkCancelInteractionListener {
        
        public void onButtonsOkCancel_Ok();
        public void onButtonsOkCancel_Cancel();
        
    }
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnButtonsOkCancelInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " должен реализовывать интерфейс OnButtonsOkCancelInteractionListener");
        }
    }	
	
	

}
