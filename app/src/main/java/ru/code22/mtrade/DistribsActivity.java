package ru.code22.mtrade;

import java.sql.Date;

import org.apache.commons.net.ftp.FTPClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ru.code22.mtrade.MyDatabase.DistribsRecord;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.DistribsPageFragment.onSomeDistribsEventListener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class DistribsActivity extends AppCompatActivity implements onSomeDistribsEventListener {
	
	static final String LOG_TAG = "mtradeLogs";
	static final int PAGE_COUNT = 3;
	static final int PAGE_COUNT_LANDSCAPE = 2;
	
	// в MainActivity 
	static final int DISTRIBS_RESULT_OK=1;
	static final int DISTRIBS_RESULT_CANCEL=2;
	
	// из фрагмента
	//static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    static final int SELECT_CLIENT_FROM_DISTRIBS_REQUEST = 2;
    //static final int SELECT_AGREEMENT_FROM_ORDER_REQUEST = 3;
    //static final int OPEN_NOMENCLATURE_FROM_ORDER_REQUEST = 4;
    static final int SELECT_TRADE_POINT_FROM_DISTRIBS_REQUEST = 5;
	static final int QUANTITY_SIMPLE_REQUEST = 6;
	
    //public static final int IDD_DATE = 1;
    public static final int IDD_CLOSE_QUERY = 2;
    public static final int IDD_CLEAR_LINES = 3;
    public static final int IDD_CANT_SAVE=4;
    
	ViewPager pager;
	MyFragmentPagerAdapter pagerAdapter;
	boolean orientationLandscape;
	
    private LocationManager m_locationManagerInDistribs;
	
	String m_reason_cant_save;
	
	protected void setModified(boolean flag)
	{
		MySingleton g=MySingleton.getInstance();
		if (g.MyDatabase.m_distribs_editing_modified!=flag)
		{
			if (flag)
			{
				setTitle(R.string.title_activity_distribs_changed);
			} else
			{
				setTitle(R.string.title_activity_distribs);
			}
			g.MyDatabase.m_distribs_editing_modified=flag;
		}
	}
	
	//@Override
	public boolean distribsCanBeSaved(StringBuffer reason)
	{
		//if (MyDatabase.m_order_editing.dont_need_send==1)
		//	return true; 
		String strWarning="";
		if (MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.isEmpty())
		{
			//strWarning+=",клиент";
			strWarning+=","+getString(R.string.field_client);
		}
		if (!MySingleton.getInstance().Common.TITAN&&!MySingleton.getInstance().Common.TANDEM&&MySingleton.getInstance().MyDatabase.m_distribs_editing.distr_point_id.isEmpty())
		{
			//strWarning+=",торговая точка";
			strWarning+=","+getString(R.string.field_distr_point);
		}
		if (!strWarning.isEmpty())
		{
			//Toast.makeText(this, "Не заполнены обязательные поля: "+strWarning.substring(1), Toast.LENGTH_LONG).show();
			if (reason==null)
			{
				Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_required_field_not_filled)+strWarning.substring(1), Snackbar.LENGTH_LONG).show();
			} else
			{
				reason.append(getString(R.string.message_required_field_not_filled)+strWarning.substring(1));
			}
			return false;
		}
		return true;
	}
	
	private LocationListener locListener = new LocationListener()
    {
		@Override
		public void onLocationChanged(Location loc) 
		{
			//Globals g=(Globals)getApplication();

			MySingleton g=MySingleton.getInstance();
			
			if (loc!=null)
			{
				m_locationManagerInDistribs.removeUpdates(locListener);
				
				boolean gpsenabled = m_locationManagerInDistribs.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				g.MyDatabase.m_distribs_editing.latitude=loc.getLatitude();
				g.MyDatabase.m_distribs_editing.longitude=loc.getLongitude();
				g.MyDatabase.m_distribs_editing.datecoord=Common.getDateTimeAsString14(new Date(loc.getTime()));
				g.MyDatabase.m_distribs_editing.gpsstate=gpsenabled?1:loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)?2:0;
				g.MyDatabase.m_distribs_editing.gpsaccuracy=loc.getAccuracy();
				g.MyDatabase.m_distribs_editing.accept_coord=0;
				
				int viewId=pager.getId();
				int position=2;
				Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
				if (frag0!=null)
				{
					View view0=frag0.getView();
					if (view0!=null)
					{
						final TextView tvLatitude = (TextView) view0.findViewById(R.id.textViewDistribsLatitudeValue);
						tvLatitude.setText(Double.toString(g.MyDatabase.m_distribs_editing.latitude));
						final TextView tvLongitude = (TextView) view0.findViewById(R.id.textViewDistribsLongitudeValue);
						tvLongitude.setText(Double.toString(g.MyDatabase.m_distribs_editing.longitude));
						final TextView tvDateCoord = (TextView) view0.findViewById(R.id.textViewDistribsDateCoordValue);
						tvDateCoord.setText(g.MyDatabase.m_distribs_editing.datecoord);
				    	final CheckBox cbReceiveCoord=(CheckBox)view0.findViewById(R.id.checkBoxDistribsReceiveCoord);
				    	cbReceiveCoord.setChecked(g.MyDatabase.m_distribs_editing.accept_coord==1);
					}
				}
		        /*
				if (m_bNeedCoord)
		        {
			        m_bNeedCoord=false;
			        m_locationManager.removeUpdates(locListener);
		        }
		        String accept_coord_where=null;
		        String[] accept_coord_selectionArgs=null;
		        getAcceptCoordCondition(accept_coord_where, accept_coord_selectionArgs);
		        
				ContentValues cv=new ContentValues();
				cv.put("latitude", loc.getLatitude());
				cv.put("longitude", loc.getLongitude());
				cv.put("accept_coord", 0);
				if (getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, accept_coord_where, accept_coord_selectionArgs)>0)
				{
					// и увеличиваем версию редактируемой заявки
					// (не проверяем, действительно ли в данный момент она редактируется, и принимает координаты, т.к. это не важно)
					MyDatabase.m_order_editing.version++;
				}
				*/
			}
			
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
    };
    
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return this;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	
	public boolean HandleBundle(Bundle savedInstanceState)
    {
    	boolean bCreatedInSingleton=false;
		MySingleton g=MySingleton.getInstance();
		if (g.MyDatabase==null)
		{
		    g.MyDatabase = new MyDatabase();
		    g.MyDatabase.m_order_editing.bCreatedInSingleton=true;
		    g.MyDatabase.m_refund_editing.bCreatedInSingleton=true;
		    g.MyDatabase.m_distribs_editing.bCreatedInSingleton=true;
		    g.MyDatabase.m_payment_editing.bCreatedInSingleton=true;
			g.MyDatabase.m_message_editing.bCreatedInSingleton=true;
		}
		bCreatedInSingleton=g.MyDatabase.m_distribs_editing.bCreatedInSingleton;
		
		g.MyDatabase.m_distribs_editing_id=savedInstanceState.getLong("distribs_editing_id");
		g.MyDatabase.m_distribs_editing_created=savedInstanceState.getBoolean("distribs_editing_created");
		g.MyDatabase.m_distribs_editing_modified=savedInstanceState.getBoolean("distribs_editing_modified");
		
		g.MyDatabase.m_distribs_editing=savedInstanceState.getParcelable("distribs_editing");
		
		return bCreatedInSingleton;
    }	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();
		
		outState.putLong("distribs_editing_id", g.MyDatabase.m_distribs_editing_id);
		outState.putBoolean("distribs_editing_created", g.MyDatabase.m_refund_editing_created);
		outState.putBoolean("distribs_editing_modified", g.MyDatabase.m_distribs_editing_modified);
		
		outState.putParcelable("distribs_editing", g.MyDatabase.m_distribs_editing);
		
	}

	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		orientationLandscape=(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE);
		
		Object ob1=getLastCustomNonConfigurationInstance();
		if (ob1!=null)
		{
			//Toast.makeText(this, "NOT NULL", Toast.LENGTH_LONG).show();
		}
		
	    setContentView(R.layout.distribs_pager);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	    if (MySingleton.getInstance().Common.PHARAON)
	    {
	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    }
	    
	    boolean bCreatedInSingleton=false;
	    boolean bRecreationActivity=false; 
	    if (savedInstanceState!=null&&savedInstanceState.containsKey("distribs_editing"))
	    {
	    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
	    	// но Singleton при этом не всегда уничтожался
	    	bCreatedInSingleton=HandleBundle(savedInstanceState);
	    }
	    
	    //if (!bCreatedInSingleton)
	    //{
	    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
	    //}
	    
	    MySingleton.getInstance().MyDatabase.m_distribsLinesAdapter=null;
	    
		if (bCreatedInSingleton)
		{
			
		} else
		{
		   	Intent intent=getIntent();
		   	
		   	Bundle bundle=intent.getBundleExtra("extrasBundle");
		    //m_list_stocks_descr = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_descr"));
		    //m_list_stocks_id = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_id"));
	
		   	//m_list_accounts_descr = bundle.getStringArray("list_accounts_descr");
		    //m_list_accounts_id = bundle.getStringArray("list_accounts_id");
		   	
		   	//m_list_prices_descr = bundle.getStringArray("list_prices_descr");
		    //m_list_prices_id = bundle.getStringArray("list_prices_id");
		   	
		    //m_list_stocks_descr = bundle.getStringArray("list_stocks_descr");
		    //m_list_stocks_id = bundle.getStringArray("list_stocks_id");
		   	
		    /*
		    Bundle bundle = new Bundle();
		    bundle.putStringArray("list_stocks_descr",(String[])list_stocks_descr.toArray());
		    bundle.putStringArray("list_stocks_id",(String[])list_stocks_id.toArray());
		    intent.putExtra("extrasBundle", bundle);
		    */
		    
		   	/*
		   	String id = intent.getStringExtra("id");
		   	//Toast.makeText(this, "id="+id, 3000).show();
		   	if (id.isEmpty())
		   	{
		   		// это новый документ
		   	}
		   	*/
		    
		   	/*
		    if (MyDatabase.m_distribs_editing_created)
		    {
		    	// это новый документ
		    	// контрагент в документ попал автоматически из отбора
		    	if (!MyDatabase.m_distribs_editing.client_id.isEmpty())
		    	{
		    		// установим договор по умолчанию, если он единственный
			        Cursor defaultAgreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{MyDatabase.m_refund_editing.client_id.toString()}, null);
			        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
			        {
			        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
			        	//int price_type_idIndex = defaultAgreementCursor.getColumnIndex("price_type_id");
			        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
			        	if (!defaultAgreementCursor.moveToNext())
			        	{
			        		// Есть единственный договор
		    	        	onRefundAgreementSelected(null, _idAgreement);
			        	}
			        }
		    	}
		    }
		    */
		    
		    /*
		    if (intent.getBooleanExtra("recalc_price", false))
		    {
		    	recalcPrice();
		    }
		    */
		}
	   	
	    pager = (ViewPager) findViewById(R.id.pagerDistribs);
	    pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
	    pager.setAdapter(pagerAdapter);
	    
	    pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

	      @Override
	      public void onPageSelected(int position) {
	        Log.d(LOG_TAG, "onPageSelected, position = " + position);
	        if (position==1)
	        {
	        	//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	        	((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(pager.getWindowToken(), 0);
	        } else
	        {
	        }
	      }

	      @Override
	      public void onPageScrolled(int position, float positionOffset,
	          int positionOffsetPixels) {
	      }

	      @Override
	      public void onPageScrollStateChanged(int state) {
	      }
	    });
	    
        m_locationManagerInDistribs = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

	    if (MySingleton.getInstance().MyDatabase.m_distribs_editing.accept_coord==1&&m_locationManagerInDistribs!=null)
	    {
	    	// здесь не проверяем состояние, отработана заявка или нет
	    	// координаты принимаются всегда, однако флаг измененности заявки не устанавливаем
	    	// при получении координат
	    	
	        Criteria criteria = new Criteria();
			criteria.setAccuracy( Criteria.ACCURACY_COARSE );
			String provider = m_locationManagerInDistribs.getBestProvider( criteria, true );
	    	
	    	//m_locationManagerInOrder.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
			if (provider!=null)
			{
				m_locationManagerInDistribs.requestLocationUpdates(provider,  0, 0, locListener);
			}
	    }
	    
	  }
	    
	    	
	  @Override
		protected Dialog onCreateDialog(int id) {
		  DistribsRecord rec=MySingleton.getInstance().MyDatabase.m_distribs_editing;
			switch (id) {
			/*
			case IDD_DATE:
			{
				int day=Integer.parseInt(rec.datedoc.substring(6,8));
				int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
				int year=Integer.parseInt(rec.datedoc.substring(0,4));
			    // set date picker as current date
				return new DatePickerDialog(this, datePickerListener, 
	                         year, month,day);
			}
			case IDD_SHIPPING_BEGIN_TIME:
			{
				int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
				return new TimePickerDialog(this, timePickerListenerBeginTime, 
	                         hourOfDay, minute, true);
			}
			case IDD_SHIPPING_END_TIME:
			{
				int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
				return new TimePickerDialog(this, timePickerListenerEndTime, 
	                         hourOfDay, minute, true);
			}
			*/
			case IDD_CLOSE_QUERY:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.distribs_modiffied));
				builder.setMessage(getResources().getString(R.string.save_it_before_closing));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						StringBuffer reason=new StringBuffer();
						if (distribsCanBeSaved(reason))
						{
							Intent intent=new Intent();
							setResult(DISTRIBS_RESULT_OK, intent);
							m_locationManagerInDistribs.removeUpdates(locListener);
							finish();
						} else
						{
							dialog.cancel();
							//Bundle bundle = new Bundle();
							//bundle.putString("reason", m_reason_cant_save);
							m_reason_cant_save=reason.toString();
							removeDialog(IDD_CANT_SAVE);
							showDialog(IDD_CANT_SAVE);
						}
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent=new Intent();
						setResult(DISTRIBS_RESULT_CANCEL, intent);
						m_locationManagerInDistribs.removeUpdates(locListener);
						finish();
					}
				});
				builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_CLEAR_LINES:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.question_delete_lines));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.clear();
						MySingleton.getInstance().MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
						setModified(true);
						//recalcWeight();
						//redrawWeight();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_CANT_SAVE:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.distribs_cant_be_saved));
				builder.setMessage(m_reason_cant_save);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				builder.setCancelable(false);
				return builder.create();
			}
			}
			return null;
		}
	  
	  
		@Override
		protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle)
		{
			super.onPrepareDialog(id, dialog);
			DistribsRecord rec=MySingleton.getInstance().MyDatabase.m_distribs_editing;
			
			switch (id)
			{
			/*
			case IDD_DATE:
			{
				int day=0;
				int month=0;
				int year=0;
				if (rec.datedoc.length()>=8)
				{
					day=Integer.parseInt(rec.datedoc.substring(6,8));
					month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
					year=Integer.parseInt(rec.datedoc.substring(0,4));
					((DatePickerDialog)dialog).updateDate(year, month, day);
				}
				break;
			}
			*/
			case IDD_CANT_SAVE:
				//m_reason_cant_save=bundle.getString("reason");
				break;
			}
		}
	  
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.distribs, menu);
	return true;
}

// обновление меню
@Override
public boolean onPrepareOptionsMenu(Menu menu) {
   	//menu.setGroupVisible(R.id., false);
	return super.onPrepareOptionsMenu(menu);
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	
	switch (item.getItemId())
	{
	case R.id.action_save:
	{
		StringBuffer reason=new StringBuffer();
		if (distribsCanBeSaved(reason))
		{
			Intent intent=new Intent();
			setResult(DISTRIBS_RESULT_OK, intent);
			finish();
		} else
		{
			//Bundle bundle = new Bundle();
			//bundle.putString("reason", m_reason_cant_save);
			m_reason_cant_save=reason.toString();
			removeDialog(IDD_CANT_SAVE);
			showDialog(IDD_CANT_SAVE);
		}
		break;
	}
	case R.id.action_close:
	{
		Intent intent=new Intent();
		setResult(DISTRIBS_RESULT_CANCEL, intent);
		finish();
		break;
	}
	case R.id.action_settings:
		Intent intent=new Intent(DistribsActivity.this, PrefDistribsActivity.class);
		startActivity(intent);
		break;
	}
  
  return super.onOptionsItemSelected(item);
}




@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	
}

@Override
public boolean onContextItemSelected(MenuItem item) {
	return super.onContextItemSelected(item);
}

@Override
// возвращает true в случае, если клиент изменился
public boolean onDistribsClientSelected(View view0, long _id)
{
	Globals g=(Globals)getApplication();
	
	if (_id!=0)
	{
	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, _id);
	    Cursor clientsCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "address", "curator_id", "flags", "priceType"}, null, null, null);
	    if (clientsCursor.moveToNext())
	    {
	    	int idIndex = clientsCursor.getColumnIndex("id");
	    	int descrIndex = clientsCursor.getColumnIndex("descr");
	    	int addressIndex = clientsCursor.getColumnIndex("address");
	    	int curator_idIndex = clientsCursor.getColumnIndex("curator_id");
	    	int flags_Index = clientsCursor.getColumnIndex("flags");
	    	//int priceType_Index = clientsCursor.getColumnIndex("priceType");
	    	String clientId = clientsCursor.getString(idIndex);
	    	if (MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.toString().equals(clientId))
	    	{
	    		return false;
	    	}
	    	String clientDescr = clientsCursor.getString(descrIndex);
	    	String clientAddress = clientsCursor.getString(addressIndex);
	    	String curatorId = clientsCursor.getString(curator_idIndex);
	    	int flags = clientsCursor.getInt(flags_Index); 
	    	
	    	MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id=new MyID(clientId);
	    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_client_name=clientDescr;
	    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_client_address=clientAddress;
	    	//MyDatabase.m_distribs_editing.stuff_select_account=(flags&2)!=0;
	    	// очистим торговую точку
	    	MySingleton.getInstance().MyDatabase.m_distribs_editing.distr_point_id=new MyID();
	    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_distr_point_name="";
			// Долг контрагента
		    Cursor cursorDebt=getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "client_id=?", new String[]{MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.toString()}, null);
		    if (cursorDebt.moveToNext())
		    {
		    	int indexDebt = cursorDebt.getColumnIndex("saldo");
		    	int indexDebtPast = cursorDebt.getColumnIndex("saldo_past");
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt=cursorDebt.getDouble(indexDebt); 
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt_past=cursorDebt.getDouble(indexDebtPast);
		    } else
		    {
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt=0.0;
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt_past=0.0;
		    }
		    // Куратор
		    Cursor curatorsCursor=getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{curatorId}, null);
		    String curatorDescr;
		    if (curatorsCursor!=null &&curatorsCursor.moveToNext())
		    {
		    	int curator_descrIndex = curatorsCursor.getColumnIndex("descr");
		    	curatorDescr = curatorsCursor.getString(curator_descrIndex);
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.curator_id=new MyID(curatorId);
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_curator_name=curatorDescr;
		    } else
		    {
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.curator_id=new MyID();
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_curator_name="";
		    	curatorDescr = getResources().getString(R.string.curator_not_set);
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt=0.0;
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt_past=0.0;
		    }
		    
	    	if (view0!=null)
	    	{
	    		// Клиент
	    		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
	    		etClient.setText(clientDescr);
				// и его адрес
				TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewDistribsClientAddress);
				tvClientAddress.setText(clientAddress);
	    		// Тип учета
	    		//TextView tvAccounts=(TextView)view0.findViewById(R.id.tvAccount);
	    		//Spinner spinnerAccounts=(Spinner)view0.findViewById(R.id.spinnerAccount);
	    		/*
	    		if (MyDatabase.m_distribs_editing.stuff_select_account)
    			{
    				tvAccounts.setVisibility(View.VISIBLE);
    				spinnerAccounts.setVisibility(View.VISIBLE);
    			} else
    			{
    				tvAccounts.setVisibility(View.GONE);
    				spinnerAccounts.setVisibility(View.GONE);
    			}
    			*/
	    		// Куратор
	    		EditText etCurator=(EditText)view0.findViewById(R.id.editTextCurator);
	    		etCurator.setText(curatorDescr);
				// Долг
				EditText etDebt = (EditText) view0.findViewById(R.id.editTextDebt);
				etDebt.setText(String.format("%.2f", MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt));
				EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextDebtPast);
				etDebtPast.setText(String.format("%.2f", MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_debt_past));
				// Торговая точка
				EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
				etDistrPoEditText.setText(MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_distr_point_name);
	    	}

	    	clientsCursor.close();
	    	curatorsCursor.close();
		    cursorDebt.close();
	    	
	    	return true;
	    }
		clientsCursor.close();
	}
	
	// Клиент не был указан и не изменился
	if (MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.isEmpty())
		return false;
	// Клиент был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
		etClient.setText(getResources().getString(R.string.client_not_set));
		TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewDistribsClientAddress);
		tvClientAddress.setText("");
		EditText etCurator=(EditText)view0.findViewById(R.id.editTextCurator);
		etCurator.setText(getResources().getString(R.string.curator_not_set));
		EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
		etDistrPoEditText.setText(getResources().getString(R.string.trade_point_not_set));
	}
	MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id=new MyID();
	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_client_name="";
	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_client_address="";
	MySingleton.getInstance().MyDatabase.m_distribs_editing.curator_id=new MyID();
	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_curator_name="";
	MySingleton.getInstance().MyDatabase.m_distribs_editing.distr_point_id=new MyID();
	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_distr_point_name="";
	
	return true;
}


@Override
protected void onDestroy() {
	m_locationManagerInDistribs.removeUpdates(locListener);
	super.onDestroy();
}

private void onCloseActivity()
{
	if (MySingleton.getInstance().MyDatabase.m_distribs_editing_modified)
	{
		showDialog(IDD_CLOSE_QUERY);
	} else
	{
		Intent intent=new Intent();
		setResult(DISTRIBS_RESULT_CANCEL, intent);
		finish();
	}
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
  if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	  onCloseActivity();
	  return true;
  }
  return super.onKeyDown(keyCode, event);
}
      
// Alternative variant for API 5 and higher
@Override
public void onBackPressed() {
	onCloseActivity();
}    

@Override
public void onConfigurationChanged(Configuration newConfig){
	/*
	if (orientationLandscape!=(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE))
	{
		orientationLandscape=(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE);
		if (orientationLandscape)
		{
		    setContentView(R.layout.order_pager);
		}
	}
	*/
	super.onConfigurationChanged(newConfig);
}

@Override
public void someDistribsEvent(String s) {
	//Globals g=(Globals)getApplication();
	if (s.equals("coord"))
	{
		if (MySingleton.getInstance().MyDatabase.m_distribs_editing.accept_coord==1)
		{
			m_locationManagerInDistribs.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
		} else
		{
			m_locationManagerInDistribs.removeUpdates(locListener);
		}
	} else
	if (s.equals("close"))
	{
		onCloseActivity();
	} else
	if (s.equals("ok"))
	{
		StringBuffer reason=new StringBuffer();
		if (distribsCanBeSaved(reason))
		{
			Intent intent=new Intent();
			setResult(DISTRIBS_RESULT_OK, intent);
			// 16.01.2013
			m_locationManagerInDistribs.removeUpdates(locListener);
			// 
			finish();
		} else
		{
			//Bundle bundle = new Bundle();
			//bundle.putString("reason", m_reason_cant_save);
			m_reason_cant_save=reason.toString();
			removeDialog(IDD_CANT_SAVE);
			showDialog(IDD_CANT_SAVE);
		}
	}
	
    //Fragment frag1 = getFragmentManager().findFragmentById(R.id.fragment1);
    //((TextView)frag1.getView().findViewById(R.id.textView)).setText("Text from Fragment 2:" + s);
}

	  private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	    public MyFragmentPagerAdapter(FragmentManager fm) {
	      super(fm);
	    }

	    @Override
	    public Fragment getItem(int position) {
	    	return DistribsPageFragment.newInstance(position);
	    }

	    @Override
	    public int getCount() {
	    	if (orientationLandscape)
	    		return PAGE_COUNT_LANDSCAPE; 
	      return PAGE_COUNT;
	    }
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	String[] titles = getResources().getStringArray(R.array.tabs_distribs);
	    	if (orientationLandscape)
	    	{
		    	titles = getResources().getStringArray(R.array.tabs_distribs_landscape);
	    	}
	    	if (position<titles.length)
	    		return titles[position];
	    	return "Title " + position;
	    }
	    
	  }
	  
	  
	@Override
	protected void onResume() {
		
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		//String str_work_date=sharedPreferences.getString("work_date", DatePreference.defaultCalendarString());
		
		/*
		Calendar work_date=DatePreference.getDateFor(sharedPreferences, "work_date");
		if (!m_work_date.equals(work_date))
		{
			// изменили рабочую дату - изменим дату заказа
			m_work_date=work_date;
			
			RefundRecord rec=MyDatabase.m_refund_editing;
			
			rec.datedoc=android.text.format.DateFormat.format("yyyyMMddkkmmss", m_work_date.getTime()).toString();
	
			int viewId=pager.getId();
			int position=0;
			Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
			View view0=frag0.getView();
			EditText etDate = (EditText) view0.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(rec.datedoc.substring(6,8)+"."+rec.datedoc.substring(4,6)+"."+rec.datedoc.substring(0,4));
			etDate.setText(sb.toString());
			
			setModified(true);
		}
		*/

		/*
		if (m_nHideFormatSettings)
		{
			Preference customPref = (Preference) findPreference("data_format");
			getPreferenceScreen().removePreference(customPref);
		}
		*/
		super.onResume();
	}

}

