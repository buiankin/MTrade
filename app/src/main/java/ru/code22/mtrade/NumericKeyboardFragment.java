package ru.code22.mtrade;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class NumericKeyboardFragment extends Fragment implements OnClickListener{
	
    private OnNumericKeyboardFragmentInteractionListener mListener;
    
    static final Map<Integer, String> pairs=new HashMap<Integer, String>(){
    	{
    		put(R.id.button_digit_1, "1");
    		put(R.id.button_digit_2, "2");
    		put(R.id.button_digit_3, "3");
    		put(R.id.button_digit_4, "4");
    		put(R.id.button_digit_5, "5");
    		put(R.id.button_digit_6, "6");
    		put(R.id.button_digit_7, "7");
    		put(R.id.button_digit_8, "8");
    		put(R.id.button_digit_9, "9");
    		put(R.id.button_digit_0, "0");
    		put(R.id.button_dot, ".");
    		put(R.id.buttonClear, "C");
    	}
    };
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.numeric_keyboard_fragment, container, false);

        for (Map.Entry<Integer, String> pair : pairs.entrySet())
        {
            Button btn = (Button) view.findViewById(pair.getKey());
            btn.setOnClickListener(this);
        }
        
        return view;
    }	
	
	public interface OnNumericKeyboardFragmentInteractionListener {
        
        public void onNumericKeyboardFragmentKeyPressed(String val);
        
    }
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnNumericKeyboardFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " должен реализовывать интерфейс OnNumericKeyboardFragmentInteractionListener");
        }
    }	

	@Override
	public void onClick(View v) {
		
		String val=pairs.get(v.getId());
		if (val!=null)
		{
			mListener.onNumericKeyboardFragmentKeyPressed(val);
		}
		
	}

}
