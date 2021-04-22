package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import ru.code22.mtrade.ButtonsOkCancelFragment.OnButtonsOkCancelInteractionListener;
import ru.code22.mtrade.NumericKeyboardFragment.OnNumericKeyboardFragmentInteractionListener;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class QuantitySimpleActivity extends AppCompatActivity implements OnNumericKeyboardFragmentInteractionListener, OnButtonsOkCancelInteractionListener{
	
	static final int QUANTITY_SIMPLE_RESULT_OK=1;
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

        setContentView(R.layout.quantity_simple);
        
	   	Intent intent=getIntent();
		String description=intent.getStringExtra("description");
		double quantity=intent.getDoubleExtra("quantity", 0.0);
		
        TextView tvDescription=(TextView)findViewById(R.id.textViewDescr);
        tvDescription.setText(description);
		
		NumericInputFragment fragment = (NumericInputFragment) getSupportFragmentManager()
                .findFragmentById(R.id.numericInputFragment);
        if (fragment != null && fragment.isInLayout()) {
        	fragment.setQuantity(quantity);
        }		
        
    }

	@Override
	public void onNumericKeyboardFragmentKeyPressed(String val) {
		NumericInputFragment fragment = (NumericInputFragment) getSupportFragmentManager()
                .findFragmentById(R.id.numericInputFragment);
        if (fragment != null && fragment.isInLayout()) {
        	fragment.parseKeyboard(val);
        }		
	}
	
	
	private void acceptData(boolean checkQuantity)
	{
		NumericInputFragment fragment = (NumericInputFragment) getSupportFragmentManager()
                .findFragmentById(R.id.numericInputFragment);
        if (fragment != null && fragment.isInLayout()) {
			Intent intent=new Intent();
			double quantity=fragment.getQuantity();
			intent.putExtra("quantity", quantity);
			setResult(QUANTITY_SIMPLE_RESULT_OK, intent);
			finish();
        }		
		
	}

	@Override
	public void onButtonsOkCancel_Ok() {
		acceptData(true);
	}

	@Override
	public void onButtonsOkCancel_Cancel() {
		finish();
		
	}	

}
