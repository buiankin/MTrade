package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.MyID;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TradePointsActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	SimpleCursorAdapter mAdapter;
    
	private static final int LOADER_ID = 1;

	ListView lvTradePoints;
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

		setContentView(R.layout.trade_points);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		
		final String clientId=getIntent().getStringExtra("client_id");
		if (clientId==null)
			m_client_id=g.MyDatabase.m_order_editing.client_id;
		else
			m_client_id=new MyID(clientId);
		
		
		lvTradePoints = (ListView)findViewById(R.id.lvTradePoints);
		lvTradePoints.setEmptyView(findViewById(android.R.id.empty));
		
		String[] fromColumns = g.Common.PRODLIDER?new String[]{"descr", "address", "pricetype_descr"}:new String[]{"descr", "address"};
        int[] toViews = g.Common.PRODLIDER?new int[]{R.id.trade_points_item_descr, R.id.trade_points_item_address, R.id.trade_points_item_pricetype}:new int[]{android.R.id.text1, android.R.id.text2};

        mAdapter = new SimpleCursorAdapter(this, 
        		g.Common.PRODLIDER?R.layout.trade_points_item:android.R.layout.simple_list_item_2, null,
                fromColumns, toViews, 0);
        
        lvTradePoints.setAdapter(mAdapter);
        lvTradePoints.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				Intent intent = new Intent();
			    intent.putExtra("id", id);
			    setResult(RESULT_OK, intent);
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
    	
    	MySingleton g=MySingleton.getInstance();
    	
    	// В одном случае используется DISTR_POINTS_LIST_CONTENT_URI
    	// а в другом DISTR_POINTS_CONTENT_URI, в котором нет pricetype_descr
    	String[] PROJECTION = g.Common.PRODLIDER?new String[] {"_id", "owner_id", "descr", "address", "pricetype_descr"}:new String[] {"_id", "owner_id", "descr", "address"};
    	
        // creating a Cursor for the data being displayed.
    	return new CursorLoader(this, g.Common.PRODLIDER?MTradeContentProvider.DISTR_POINTS_LIST_CONTENT_URI:MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
	            PROJECTION, "owner_id=?", new String[]{m_client_id.toString()}, "distr_points.descr");
        
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
