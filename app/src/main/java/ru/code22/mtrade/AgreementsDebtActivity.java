package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.code22.mtrade.AgreementsDebtExpListAdapter.GroupData;
import ru.code22.mtrade.MyDatabase.MyID;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

public class AgreementsDebtActivity extends AppCompatActivity {
		
	AgreementsDebtExpListAdapter mAdapter;
	    
    ExpandableListView lvAgreements;
    MyID m_client_id;
    
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
		
		setContentView(R.layout.agreements_debt);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final String clientId=getIntent().getStringExtra("client_id");
		if (clientId==null)
			m_client_id=MySingleton.getInstance().MyDatabase.m_payment_editing.client_id;
		else
			m_client_id=new MyID(clientId);
		
		lvAgreements = (ExpandableListView)findViewById(R.id.lvAgreementsDebt);
		lvAgreements.setEmptyView(findViewById(android.R.id.empty));		
		
		//String[] fromColumns = {"agreement_descr", "organization_descr", "pricetype_descr"};
        //int[] toViews = {R.id.payment_item_descr, R.id.agreement_item_organization, R.id.agreement_item_price_type};

        mAdapter = new AgreementsDebtExpListAdapter(this, m_client_id.toString());
        mAdapter.init();
        /*
        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                String name = cursor.getColumnName(columnIndex);
                if ("saldo".equals(name)||"saldo_past".equals(name)) {
                	double val=cursor.getDouble(columnIndex);
                	if (-0.001<val&&val<=0.001)
                		((TextView)view).setText("");
                	else
                		((TextView)view).setText(String.format("%.2f", val));
                    return true;
                }
                return false;
            }
        };
        mAdapter.setViewBinder(binder);
        */
        
        lvAgreements.setAdapter(mAdapter);
        lvAgreements.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				//Toast.makeText(ClientsActivity.this, "" + position, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
			    intent.putExtra("id", id);
			    setResult(RESULT_OK, intent);
			    finish();				
			}
        });
        
    	registerForContextMenu(lvAgreements);
	}
	
	@Override
	 public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				
		MenuInflater inflater = getMenuInflater();
		switch (v.getId()) {
		case R.id.lvAgreementsDebt:
		{
		    inflater.inflate(R.menu.agreements_debt_list, menu);
		    /*
			MenuItem itemCancelOrder = menu.findItem(R.id.action_cancel_order);
			itemCancelOrder.setVisible(false);

			MenuItem itemDeleteOrder = menu.findItem(R.id.action_delete_order);
			itemDeleteOrder.setVisible(false);
			
		    //selectedWord = ((TextView) info.targetView).getText().toString();
		    long _id = info.id;
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, _id);
			Cursor cursor=getContentResolver().query(singleUri, new String[]{"state"}, null, null, null);
			if (cursor!=null && cursor.moveToNext())
			{
				int stateIndex=cursor.getColumnIndex("state");
				E_ORDER_STATE state=E_ORDER_STATE.fromInt(cursor.getInt(stateIndex));
				if (E_ORDER_STATE.getCanBeDeleted(state))
				{
					itemDeleteOrder.setVisible(true);
				}
				if (E_ORDER_STATE.getCanBeCanceled(state))
				{
					itemCancelOrder.setVisible(true);
				}
			}
			*/
		}
	    break;
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.action_select_agreement:
		{
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			//int _id = (int)info.id;
			//GroupData data=mAdapter.mGroups.get(_id);
			
			//int type = ExpandableListView.getPackedPositionType(info.packedPosition);			
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            //int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
            
            GroupData data=mAdapter.mGroups.get(groupPosition);
			
			Intent intent = new Intent();
		    intent.putExtra("agreement_id", data.agreement_id);
		    intent.putExtra("agreement_descr", data.agreement_descr);
		    //intent.putExtra("organization_id", data.organization_id);
		    intent.putExtra("organization_descr", data.organization_descr);
		    intent.putExtra("manager_id", data.manager_id);
		    intent.putExtra("manager_descr", data.manager_descr);
		    
		    setResult(RESULT_OK, intent);
		    finish();				
			
			return true;
		}
		case R.id.action_select_agreement_with_sum:
		{
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			//int _id = (int)info.id;
			//GroupData data=mAdapter.mGroups.get(_id);
			
			int type = ExpandableListView.getPackedPositionType(info.packedPosition);			
			int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);
            
			GroupData data=mAdapter.mGroups.get(groupPosition);
			
			Intent intent = new Intent();
		    intent.putExtra("agreement_id", data.agreement_id);
		    intent.putExtra("agreement_descr", data.agreement_descr);
		    //intent.putExtra("organization_id", data.organization_id);
		    intent.putExtra("organization_descr", data.organization_descr);
		    intent.putExtra("manager_id", data.manager_id);
		    intent.putExtra("manager_descr", data.manager_descr);
		    
		    if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
		    {
		    	intent.putExtra("sum", data.details.get(childPosition).saldo);		    	
		    } else
		    {
			    intent.putExtra("sum", data.debt);
		    }
		    
		    setResult(RESULT_OK, intent);
		    finish();
		    
			return true;
		}
		}
		return super.onContextItemSelected(item);
	}

}
