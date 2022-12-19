package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.MyID;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AgreementsActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<Cursor> {

	static final int AGREEMENTS_RESULT_OK=1;

	SimpleCursorAdapter mAdapter;
	    
	private static final int LOADER_ID = 1;

	static final String[] PROJECTION = new String[] {"_id", "owner_id", "agreement_descr", "organization_descr", "pricetype_descr"};
	static final String[] PROJECTION_TN = new String[] {"_id", "owner_id", "agreement_descr", "organization_descr", "pricetype_descr", "default_manager_descr", "saldo", "saldo_past"};
	
    ListView lvAgreements;
    MyID m_client_id;
	MyID m_organization_id;
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("client_id", m_client_id.toString());
		outState.putString("organization_id", m_organization_id.toString());
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MySingleton g=MySingleton.getInstance();
		
		g.checkInitByDataAndSetTheme(this);
		
		setContentView(R.layout.agreements);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		if (savedInstanceState!=null&&savedInstanceState.containsKey("client_id"))
		{
			// Чтобы при повороте окна или пересоздании формы не стерся признак
			m_client_id=new MyID(savedInstanceState.getString("client_id"));
			m_organization_id=new MyID(savedInstanceState.getString("organization_id"));
		} else {
			final String clientId = getIntent().getStringExtra("client_id");
			if (clientId == null)
				m_client_id = MySingleton.getInstance().MyDatabase.m_order_editing.client_id;
			else
				m_client_id = new MyID(clientId);
			// для FACTORY
			final String organizationId = getIntent().getStringExtra("organization_id");
			if (organizationId == null)
				m_organization_id = MySingleton.getInstance().MyDatabase.m_order_editing.organization_id;
			else
				m_organization_id = new MyID(organizationId);
		}

		lvAgreements = (ListView)findViewById(R.id.lvAgreements);
		lvAgreements.setEmptyView(findViewById(android.R.id.empty));		

        if (MySingleton.getInstance().Common.PRAIT||MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.ISTART||MySingleton.getInstance().Common.FACTORY)
        {
    		String[] fromColumns = {"agreement_descr", "organization_descr", "pricetype_descr", "default_manager_descr", "saldo", "saldo_past"};
            int[] toViews = {R.id.agreement_item_descr, R.id.agreement_item_organization, R.id.agreement_item_price_type, R.id.agreementTextViewManager, R.id.agreementTextViewDebt, R.id.agreementTextViewDebtPast};
        	
	        mAdapter = new SimpleCursorAdapter(this, 
	        		R.layout.agreement_item_debt_only, null,
	                fromColumns, toViews, 0);
	        
	        // TODO 2015 
	        // сделать, чтобы при отсутствии долга колонка с долгом не отображалась
        } else
        {
            //String[] fromColumns = {"agreement_descr", "organization_descr"};
    		String[] fromColumns = {"agreement_descr", "organization_descr", "pricetype_descr"};
            int[] toViews = {R.id.payment_item_descr, R.id.agreement_item_organization, R.id.agreement_item_price_type};
        	
	        mAdapter = new SimpleCursorAdapter(this, 
	        		R.layout.agreement_item, null,
	                fromColumns, toViews, 0);
        }
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
			    setResult(AGREEMENTS_RESULT_OK, intent);
			    finish();				
			}
        });

        // 26.04.2019
		//LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
	}

	
    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
    	Globals g=(Globals)getApplication();
    	if (m_organization_id!=null&&MySingleton.getInstance().Common.FACTORY)
		{
			return new CursorLoader(this, MTradeContentProvider.AGREEMENTS_LIST_WITH_SALDO_ONLY_CONTENT_URI,
					PROJECTION_TN, "owner_id=? and organization_id=?", new String[]{m_client_id.toString(), m_organization_id.toString()}, "organizations.descr");
		}
		// Возможно, здесь проверка FACTORY лишняя
        // но это когда все места, откуда открывается форма договоров, будут проверены
    	if (MySingleton.getInstance().Common.PRAIT||MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.ISTART||MySingleton.getInstance().Common.FACTORY)
    	{
	    	return new CursorLoader(this, MTradeContentProvider.AGREEMENTS_LIST_WITH_SALDO_ONLY_CONTENT_URI,
		            PROJECTION_TN, "owner_id=?", new String[]{m_client_id.toString()}, "organizations.descr");
    	}
    	return new CursorLoader(this, MTradeContentProvider.AGREEMENTS_LIST_CONTENT_URI,
	            PROJECTION, "owner_id=?", new String[]{m_client_id.toString()}, "organizations.descr");
        
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
            // The asynchronous load is complete and the data
            // is now available for use. Only now can we associate
            // the queried Cursor with the SimpleCursorAdapter.
            mAdapter.swapCursor(data);
            break;
        }
        // The listview now displays the queried data.
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }
    
}
