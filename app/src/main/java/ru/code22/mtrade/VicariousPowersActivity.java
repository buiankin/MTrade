package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class VicariousPowersActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<Cursor> {

	static final int VICARIOUS_POWERS_RESULT_OK=1;

	SimpleCursorAdapter mAdapter;
    
	private static final int LOADER_ID = 1;

	static final String[] PROJECTION = new String[] {"_id", "descr", "client_descr", "fio_descr"};
	
    ListView lvVicariousPowers;
    
    static String m_client_id=null;
    static String m_organization_id=null;
    static String m_agreement_id=null;
    static String m_manager_id=null;
    
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
		
		setContentView(R.layout.vicarious_powers);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		m_client_id=getIntent().getStringExtra("client_id");
		m_organization_id=getIntent().getStringExtra("organization_id");
		m_agreement_id=getIntent().getStringExtra("agreement_id");
		m_manager_id=getIntent().getStringExtra("manager_id");
		
		lvVicariousPowers = (ListView)findViewById(R.id.lvVicariousPowers);
		lvVicariousPowers.setEmptyView(findViewById(android.R.id.empty));
		
		String[] fromColumns = {"descr", "client_descr", "fio_descr"};
        int[] toViews = {R.id.vicarious_power_item_descr, R.id.vicarious_power_item_client, R.id.vicarious_power_item_fio};

        //mAdapter = new SimpleCursorAdapter(this, 
        //		android.R.layout.two_line_list_item, null,
        //        fromColumns, toViews, 0);
        
        mAdapter = new SimpleCursorAdapter(this, 
        		R.layout.vicarious_power_item, null,
                fromColumns, toViews, 0);
        
        lvVicariousPowers.setAdapter(mAdapter);
        lvVicariousPowers.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				Intent intent = new Intent();
			    intent.putExtra("id", id);
			    setResult(VICARIOUS_POWERS_RESULT_OK, intent);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vicarious_powers, menu);
		return true;
	}

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
    	
    	String conditionString=null;
    	List<String> conditionArgs=new ArrayList<String>();
    	if (m_organization_id!=null)
    	{
    		conditionString=Common.combineConditions(conditionString, conditionArgs, "organization_id=? and (agreement_id=? or agreement_id=?)", new String[]{m_organization_id, Constants.emptyID, m_agreement_id});
    	}
    	if (m_client_id!=null&&!new MyDatabase.MyID(m_client_id).isEmpty())
    	{
    		conditionString=Common.combineConditions(conditionString, conditionArgs, "(client_id=? or client_id=?)", new String[]{m_client_id, Constants.emptyID});
    	}
    	
    	if (m_manager_id!=null)
    	{
    		conditionString=Common.combineConditions(conditionString, conditionArgs, "(manager_id=? or manager_id=?)", new String[]{m_manager_id, Constants.emptyID});
    	}
    	
    	return new CursorLoader(this, MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI,
	            PROJECTION, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), 
	            "case when client_id='"+m_client_id.toString()+"' then 0 else 1 end, datedoc, numdoc");
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
