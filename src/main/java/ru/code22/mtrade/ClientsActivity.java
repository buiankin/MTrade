package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.MyID;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.andraskindler.quickscroll.Scrollable;

public class ClientsActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor> {
	
    ClientsAdapter mAdapter;
    
    private static final int LOADER_ID = 1;

    static final String[] PROJECTION = new String[] {"_id", "descr", "address", "saldo", "saldo_past", "blocked", "flags", "email_for_cheques"};
    
    ListView lvClients;
    Spinner sClientsGroup;

    class Tree {String _id; String id; String parent_id; String descr; int level;};
    List<String> m_list_groups;
    ArrayList<Tree> m_list2;
    MyDatabase.MyID m_group_id;
    
    static long m_client_id=0;
    
    String m_filter;
    String m_distr_point_id_only;

    boolean m_bShowEmailForCheques;
    
    class ClientsAdapter extends SimpleCursorAdapter implements Scrollable {

		public ClientsAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}

		@Override
		public String getIndicatorForPosition(int childposition,
				int groupposition) {
			Cursor cursor=(Cursor)getItem(childposition);
			int indexDescr=cursor.getColumnIndex("descr");
			return cursor.getString(indexDescr).substring(0,1);
		}

		@Override
		public int getScrollPosition(int childposition, int groupposition) {
			return childposition;
		}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		
		setContentView(R.layout.clients);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		
	    m_filter="";
	    
		m_group_id=null;
		m_client_id=-1;

		m_bShowEmailForCheques=false;
		
	   	Intent intent=getIntent();
		String ClientId=intent.getStringExtra("client_id");
		if (ClientId!=null)
		{
			Cursor cursor=getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"_id"}, "id=?", new String[]{ClientId}, null);
			int index_id=cursor.getColumnIndex("_id");
			if (cursor.moveToNext())
			{
				m_client_id=cursor.getInt(index_id);
			}
		}
		if (intent.hasExtra("distr_point_id_only"))
		{
			m_distr_point_id_only=intent.getStringExtra("distr_point_id_only");
		} else
		{
			m_distr_point_id_only=null;
		}

		if (intent.hasExtra("show_email_for_cheques")) {
			m_bShowEmailForCheques = intent.getBooleanExtra("show_email_for_cheques", false);
		}

		ClientsActivity ob1=(ClientsActivity)getLastCustomNonConfigurationInstance();
		if (ob1!=null)
		{
		    m_filter=ob1.m_filter;
		}
		
		lvClients = (ListView)findViewById(R.id.lvClients);
		lvClients.setEmptyView(findViewById(android.R.id.empty));
		sClientsGroup=(Spinner)findViewById(R.id.spinnerGroupClients);
		
        String[] fromColumns = {"descr", "address", "saldo", "saldo_past"};
        int[] toViews = {R.id.client_item_text1, R.id.client_item_text2, R.id.client_item_text3, R.id.client_item_text4}; // The TextView in simple_list_item_1

		String[] fromColumnsWithEmail = {"descr", "email_for_cheques", "address", "saldo", "saldo_past"};
		int[] toViewsWithEmail = {R.id.client_item_text1, R.id.client_item_text2_0, R.id.client_item_text2, R.id.client_item_text3, R.id.client_item_text4}; // The TextView in simple_list_item_1

        //mAdapter = new SimpleCursorAdapter(this,
        //        R.layout.order_item, null,
        //        fromColumns, toViews, 0);
		if (m_bShowEmailForCheques)
			mAdapter = new ClientsAdapter(this,
					R.layout.client_item_email, null,
					fromColumnsWithEmail, toViewsWithEmail);
		else
			mAdapter = new ClientsAdapter(this,
					R.layout.client_item, null,
					fromColumns, toViews);
        /*
        {
            @Override 
            public void setViewText(TextView v, String text) 
                {                       
                    super.setViewText(v, fmt(v, text)); 
                }
            
            private String fmt(View v, String text) 
            { 
                if (v.getId()==R.id.client_item_text4||v.getId()==R.id.client_item_text3) 
                    {
                		if (text.equals("0"))
                    		return "";
                        //Double d = cursor.getDouble(cursor.getColumnIndex(kCalcDB.TBL_TARIFF_TARIFF)); 
                        //return myCustDecFormatter.format(d);
                    } 
                return text; 
            }             
        };
        */
        
        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            	View w1=(View)view.getParent();
            	
        		int blocked_index=cursor.getColumnIndex("blocked");
				int index_flags=cursor.getColumnIndex("flags");
            	if (cursor.getInt(blocked_index)==0&&(cursor.getInt(index_flags)&4)==0)
            	{
            		w1.setBackgroundColor(Color.TRANSPARENT);
            	} else
            	{
    				// Либо заблокирован, либо флаг&4 - значит, просто красный
            		w1.setBackgroundColor(Color.RED);
            	}
                String name = cursor.getColumnName(columnIndex);
				if ("email_for_cheques".equals(name)) {
					String val=cursor.getString(columnIndex);
					if (!val.contains("@"))
					{
						((TextView)view).setText(R.string.no_email);
						return true;
					}
				} else
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
        
        //setListAdapter(mAdapter);
        lvClients.setAdapter(mAdapter);
        lvClients.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long id) {
				//Toast.makeText(ClientsActivity.this, "" + position, Toast.LENGTH_SHORT).show();
				
				m_client_id=id;
				
				Cursor cursor=mAdapter.getCursor();
				int index_blocked=cursor.getColumnIndex("blocked");
				cursor.moveToPosition(position);
				if (cursor.getLong(index_blocked)!=1)
				{
					Intent intent = new Intent();
				    intent.putExtra("id", id);
				    setResult(RESULT_OK, intent);
				    finish();
				} else
				{
					AlertDialog.Builder builder=new AlertDialog.Builder(ClientsActivity.this);
					builder.setMessage(getString(R.string.client_blocked_select_anyway));
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int id) {
							Intent intent = new Intent();
						    intent.putExtra("id", m_client_id);
						    setResult(RESULT_OK, intent);
						    finish();
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					
					builder.setCancelable(false);
					builder.create().show();
				}
			}
        });
        
        // https://github.com/andraskindler/quickscroll
        
        /* получается не совсем то, что нужно 
		final QuickScroll fastTrack = (QuickScroll)findViewById(R.id.quickscroll);
		fastTrack.init(QuickScroll.TYPE_POPUP, lvClients, mAdapter, QuickScroll.STYLE_NONE);
		fastTrack.setFixedSize(2);
		fastTrack.setPopupColor(QuickScroll.BLUE_LIGHT, QuickScroll.BLUE_LIGHT_SEMITRANSPARENT, 1, Color.WHITE, 1);

		//FrameLayout root = (FrameLayout)findViewById(android.R.id.content);
		ViewGroup root = (ViewGroup)findViewById(R.id.LinearLayoutClientsList);
		root.addView(createAlphabetTrack());
		*/
        
		final QuickScroll quickscroll = (QuickScroll) findViewById(R.id.quickscroll);
		quickscroll.init(QuickScroll.TYPE_INDICATOR_WITH_HANDLE, lvClients, mAdapter, QuickScroll.STYLE_HOLO);
		quickscroll.setFixedSize(1);
		quickscroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);         
        /*
        
        String[] data = {"one", "two", "three", "four", "five"};
        
     // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        sClientsGroup.setAdapter(adapter);
        // заголовок
        sClientsGroup.setPrompt("Title");
        // выделяем элемент 
        sClientsGroup.setSelection(2);
        // устанавливаем обработчик нажатия
        sClientsGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int position, long id) {
        // показываем позиция нажатого элемента
        Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
      
	*/

		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        
        setClientsSpinnerData(Constants.emptyID);
        
        // устанавливаем обработчик нажатия
        sClientsGroup.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
			          int position, long id) {
				//Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
				/*
    	    	int descrIndex = cursor.getColumnIndex("descr");
    	    	int idIndex = cursor.getColumnIndex("id");
    	    	String newWord = cursor.getString(descrIndex);
    	    	String clientId = cursor.getString(idIndex);
    	    	EditText et = (EditText) findViewById(R.id.etClient);
    	    	et.setText(newWord);
				m_client_id=m_myBase.new MyID(clientId); 
				//getSupportLoaderManager().initLoader(ORDERS_LOADER_ID, null, MainActivity.this);
				getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
				*/
				if (m_list2.get(position).id==null)
				{
					m_group_id=null;
				} else
				{
					m_group_id=new MyID(m_list2.get(position).id);
				}
				LoaderManager.getInstance(ClientsActivity.this).restartLoader(LOADER_ID, null, ClientsActivity.this);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        	
		});
        
		
	}
	
	private ViewGroup createAlphabetTrack() {
		final LinearLayout layout = new LinearLayout(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (30 * getResources().getDisplayMetrics().density), LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.BELOW, R.id.spinnerGroupClients);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);

		final LinearLayout.LayoutParams textparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		textparams.weight = 1;
		final int height = getResources().getDisplayMetrics().heightPixels;
		int iterate = 0;
		if (height >= 1024){
			iterate = 1; layout.setWeightSum(26);
		} else {
			iterate = 2; layout.setWeightSum(13);
		}
		for (char character = 'a'; character <= 'z'; character+=iterate) {
			final TextView textview = new TextView(ClientsActivity.this);
			textview.setLayoutParams(textparams);
			textview.setGravity(Gravity.CENTER_HORIZONTAL);
			textview.setText(Character.toString(character));
			layout.addView(textview);
		}

		return layout;
	} 	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.clients, menu);
		
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		// Assumes current activity is the searchable activity
		
	    if (searchView!=null)
	    {
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			//searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				
				@Override
				public boolean onClose() {
		        	m_filter="";
					LoaderManager.getInstance(ClientsActivity.this).restartLoader(LOADER_ID, null, ClientsActivity.this);
					return false;
				}
			});
	    }
		
		
		
		return true;
	}
	
	private void setClientsSpinnerData(String parent_id)
	{
        Spinner spinner = (Spinner) findViewById(R.id.spinnerGroupClients);
        m_list_groups = new ArrayList<String>();
        ContentResolver contentResolver=getContentResolver();        
	    String[] projection =
	    {
	    	"_id",
	    	"id",
	    	"parent_id",
	        "descr"
	    };
	    /*
	    String[] selectionArgs = {parent_id};
	    Cursor cursor=contentResolver.query(MTradeContentProvider.CLIENTS_CONTENT_URI, projection, "parent_id=?", selectionArgs, "descr");
	    if (cursor!=null)
	    {
	    	int index = cursor.getColumnIndex("descr");
	    	while (cursor.moveToNext())
	    	{
		    	String newWord = cursor.getString(index);
		    	list.add(newWord);
	    	}
	    }
	    */
	    //class Tree {String _id; String id; String parent_id; String descr; int level;};
	    //ArrayList<Tree> list2 = new ArrayList<Tree>();
	    m_list2 = new ArrayList<Tree>(); 
	    
	    Cursor cursor=contentResolver.query(MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI, projection, "isFolder=1", null, "descr_lower");
	    if (cursor!=null)
	    {
	    	int indexDescr = cursor.getColumnIndex("descr");
	    	int indexId = cursor.getColumnIndex("id");
	    	int indexParentId = cursor.getColumnIndex("parent_id");
	    	int index_Id = cursor.getColumnIndex("_id");
    		Tree t = new Tree();
    		t.descr=getResources().getString(R.string.catalogue_all);
    		t.id=null;
    		t.parent_id=null;
    		t.level=0;
    		m_list2.add(t);
    		t = new Tree();
    		t.descr=getResources().getString(R.string.catalogue_node);
    		t.id="     0   ";
    		t.parent_id="";
    		t.level=0;
    		m_list2.add(t);
	    	while (cursor.moveToNext())
	    	{
	    		t = new Tree();
		    	t.descr=cursor.getString(indexDescr);
		    	t.id=cursor.getString(indexId);
		    	t.parent_id=cursor.getString(indexParentId);
		    	t._id=cursor.getString(index_Id);
		    	t.level=0;
		    	m_list2.add(t);
	    	}
	    	int i;
	    	// с единицы потому, что в нуле у нас записан null
	    	for (i=1; i<m_list2.size(); i++)
	    	{
	    		int offset=i+1;
	    		int j;
	    		for (j=i+1; j<m_list2.size(); j++)
	    		{
	    			if (m_list2.get(j).parent_id.equals(m_list2.get(i).id))
	    			{
	    				m_list2.get(j).level=m_list2.get(i).level+1;
	    				Collections.swap(m_list2, offset, j);
	    				offset++;
	    			}
	    		}
	    	}
	    	for (i=0; i<m_list2.size(); i++)
	    	{
	    		String spaces="";
	    		int j;
	    		/*
	    		for (j=1; j<m_list2.get(i).level; j++)
	    		{
	    			spaces+=" ";
	    		}
	    		if (m_list2.get(i).level>0)
	    			spaces+="> ";
	    		*/
	    		for (j=0; j<m_list2.get(i).level; j++)
	    		{
	    			spaces+=">";
	    		}
	    		if (m_list2.get(i).level>0)
	    			spaces+=" ";
	    		m_list_groups.add(spaces+m_list2.get(i).descr);
	    	}
	    }
	    
        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        /*
        if (list.size() < 2) {
            spinner.setClickable(false);
            spinner.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Toast.makeText(MainActivity.this, "Catch it!", Toast.LENGTH_SHORT).show();
                    }
					return true;
				}
			});
        }
       */
	}

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        //return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
        //        PROJECTION, SELECTION, null, null);
        // Create a new CursorLoader with the following query parameters.
        //return new CursorLoader(this, Clients.CONTENT_URI,
        //    PROJECTION, "isFolder<>1", null, "descr");

		if (!MyID.isEmptyString(m_distr_point_id_only))
		{
			// TODO
            ArrayList<String> clientsByDistrPoints=new ArrayList<>();
            Cursor clientsCursor=getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{m_distr_point_id_only}, null);
            while (clientsCursor.moveToNext())
            {
                clientsByDistrPoints.add(clientsCursor.getString(0));
            }
            clientsCursor.close();
            String[] selectionArgsClients = new String[clientsByDistrPoints.size()+(m_group_id==null?0:1)];
            StringBuilder sbClients = new StringBuilder();
            for (int i = 0; i < clientsByDistrPoints.size(); i++) {
                selectionArgsClients[i+(m_group_id==null?0:1)]=clientsByDistrPoints.get(i);
                if (i == 0) {
                    sbClients.append(" and id in (?");
                } else {
                    sbClients.append(",?");
                }
            }
            if (clientsByDistrPoints.size() != 0)
                sbClients.append(") ");

            if (m_group_id == null) {
                return new CursorLoader(this, MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI,
                        PROJECTION, "isFolder<>1 "+ sbClients.toString() + (m_filter.isEmpty() ? "" : " and descr_lower like '" + m_filter + "%'"), selectionArgsClients, "descr_lower");

            }
            selectionArgsClients[0]=m_group_id.m_id;
            return new CursorLoader(this, MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI,
                    PROJECTION, "isFolder<>1 and parent_id=? " + sbClients.toString()+ (m_filter.isEmpty() ? "" : " and descr_lower like '" + m_filter + "%'"), selectionArgsClients, "descr_lower");
		} else {
            if (m_group_id == null) {
                return new CursorLoader(this, MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI,
                        PROJECTION, "isFolder<>1" + (m_filter.isEmpty() ? "" : " and descr_lower like '" + m_filter + "%'"), null, "descr_lower");

            }
            return new CursorLoader(this, MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI,
                    PROJECTION, "isFolder<>1 and parent_id=?" + (m_filter.isEmpty() ? "" : " and descr_lower like '" + m_filter + "%'"), new String[]{m_group_id.m_id}, "descr_lower");
        }
        
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
            // The asynchronous load is complete and the data
            // is now available for use. Only now can we associate
            // the queried Cursor with the SimpleCursorAdapter.
        	//data.moveToFirst();
            mAdapter.swapCursor(data);
            
            if (m_client_id!=-1)
            {
	            int dataCount = mAdapter.getCount();
	            for (int i = 0; i < dataCount; i++) {
	                Cursor value = (Cursor) mAdapter.getItem(i);
	                long id = value.getLong(value.getColumnIndex("_id"));
	                if (id == m_client_id) {
	                	lvClients.setSelection(i);
	                	break;
	                }
	            }
            }
            
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
    
	@Override
	public boolean onSearchRequested() {
	    return super.onSearchRequested();
	}
	
	@Override
    public void onNewIntent(final Intent queryIntent) {
        super.onNewIntent(queryIntent);
        setIntent(queryIntent);
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction))
        {
        	//Toast.makeText(this, queryIntent.getStringExtra(SearchManager.QUERY), Toast.LENGTH_LONG).show();
        	m_filter=queryIntent.getStringExtra(SearchManager.QUERY);
        	m_filter=m_filter.toLowerCase(Locale.getDefault());
			LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, ClientsActivity.this);
        }
	}
    

}
