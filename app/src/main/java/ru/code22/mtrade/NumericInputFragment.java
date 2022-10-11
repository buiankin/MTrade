package ru.code22.mtrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class NumericInputFragment extends Fragment {
	
	EditText m_etQuantity;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	
        View view = inflater.inflate(R.layout.numeric_input_fragment, container, false);
        
		m_etQuantity=(EditText)view.findViewById(R.id.etQuantity);
		m_etQuantity.setText("0");
        // Кнопка "Назад"
        Button btn = (Button) view.findViewById(R.id.buttonQuantityBack);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String s=m_etQuantity.getText().toString();
				if (s.length()>1)
					m_etQuantity.setText(s.substring(0, s.length()-1));
				else
					m_etQuantity.setText("0");
			}
		});
        
        return view;
    }
    
	private void onDigit(char digit)
	{
		String s=m_etQuantity.getText().toString();
		s+=digit;
		if (s.length()>1&&s.charAt(0)=='0'&&!(s.substring(0, 2).equals("0.")))
			m_etQuantity.setText(s.substring(1));
		else
			m_etQuantity.setText(s);
	}
	public void onDigit_dot()
	{
		if (m_etQuantity.getText().toString().indexOf('.')<0)
		{
			onDigit('.');
		}
	}
	
    public void parseKeyboard(String key)
    {
    	if (key!=null&&key.length()==1)
    	{
    		char c=key.charAt(0);
    		switch (c)
    		{
    		case '0':
    		case '1':
    		case '2':
    		case '3':
    		case '4':
    		case '5':
    		case '6':
    		case '7':
    		case '8':
    		case '9':
    			onDigit(c);
    			break;
    		case '.':
    			onDigit_dot();
    			break;
    		case 'C':
    			// Очистка
    			m_etQuantity.setText("0");
    			break;
    		}
    	}
    }
    
    public double getQuantity()
    {
    	return Double.parseDouble(m_etQuantity.getText().toString());    	
    }
    
    public void setQuantity(double quantity)
    {
    	m_etQuantity.setText(Common.DoubleToStringFormat(quantity, "%.0f"));
    }
    
}
