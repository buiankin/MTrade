package ru.code22.mtrade;

import java.sql.Date;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.MyDatabase.RefundRecord;
import ru.code22.mtrade.RefundPageFragment.onSomeRefundEventListener;
import ru.code22.mtrade.preferences.DatePreference;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class RefundActivity extends AppCompatActivity implements onSomeRefundEventListener, OrderAlertDialogFragment.AlertDialogFragmentEventListener {
	
	static final String LOG_TAG = "mtradeLogs";
	static final int PAGE_COUNT = 3;
	static final int PAGE_COUNT_LANDSCAPE = 2;
	
	static final String ACTION = "refreshWeight";
	
	// в MainActivity 
	static final int REFUND_RESULT_OK=1;
	static final int REFUND_RESULT_CANCEL=2;
	
	// из фрагмента
	//static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    //static final int SELECT_CLIENT_FROM_ORDER_REQUEST = 2;
    //static final int SELECT_AGREEMENT_FROM_ORDER_REQUEST = 3;
    //static final int OPEN_NOMENCLATURE_FROM_ORDER_REQUEST = 4;
    //static final int SELECT_TRADE_POINT_FROM_ORDER_REQUEST = 5;
	static final int QUANTITY_REQUEST = 6;
	
    public static final int IDD_DATE = 1;
    public static final int IDD_CLOSE_QUERY = 2;
    public static final int IDD_CLEAR_LINES = 3;
    public static final int IDD_CANT_SAVE=4;

	public static final int MY_ALERT_DIALOG_IDD_DATE=1;
    
	ViewPager pager;
	MyFragmentPagerAdapter pagerAdapter;
	boolean orientationLandscape;
	
    private LocationManager m_locationManagerInRefund;
	
	public static String[] m_list_accounts_descr;
	public static String[] m_list_accounts_id; // здесь числа ввиде строки

	//public static String[] m_list_prices_descr;
	//public static String[] m_list_prices_id;
	
	public static String[] m_list_stocks_descr;
	public static String[] m_list_stocks_id;

	// Запоминаем рабочую дату при открытии заказа
	Calendar m_work_date;
	//
	String m_reason_cant_save;
	
	protected void setModified(boolean flag)
	{
		MySingleton g=MySingleton.getInstance();
		if (g.MyDatabase.m_refund_editing_modified!=flag)
		{
			if (flag)
			{
				setTitle(R.string.title_activity_refund_changed);
			} else
			{
				setTitle(R.string.title_activity_refund);
			}
			g.MyDatabase.m_refund_editing_modified=flag;
		}
	}
	
	protected void recalcWeight()
	{
		MySingleton g=MySingleton.getInstance();
		g.MyDatabase.m_refund_editing.weightDoc=TextDatabase.GetRefundWeight(getContentResolver(), g.MyDatabase.m_refund_editing, null, false);
	}
	
	//@Override
	public boolean refundCanBeSaved(StringBuffer reason)
	{
		MySingleton g=MySingleton.getInstance();
		//if (g.MyDatabase.m_order_editing.dont_need_send==1)
		//	return true; 
		String strWarning="";
		if (g.MyDatabase.m_refund_editing.client_id.isEmpty())
		{
			//strWarning+=",клиент";
			strWarning+=","+getString(R.string.field_client);
		}
		//if (g.Common.FACTORY&&!stuff_agreement_required)
		//{} else
		if (g.MyDatabase.m_refund_editing.agreement_id.isEmpty()&&!g.Common.MEGA&&!g.Common.PHARAOH)
		{
			//strWarning+=",договор";
			strWarning+=","+getString(R.string.field_agreement);
		}
		if (g.MyDatabase.m_refund_editing.stock_id.isEmpty()&&!g.Common.MEGA&&!g.Common.PHARAOH)
		{
			//strWarning+=",склад";
			strWarning+=","+getString(R.string.field_stock);
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
	
	/*
	private void redrawSumWeight()
	{
		g.MyDatabase.m_order_editing.sumDoc=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
		g.MyDatabase.m_order_editing.weightDoc=TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
		
		if (pager!=null)
		{
			int viewId=pager.getId();
			int position=1;
			Fragment frag1=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
			if (frag1!=null)
			{
				View view1=frag1.getView();
		
				if (view1!=null)
				{
					TextView tvStatistics=(TextView)view1.findViewById(R.id.textViewStatistics);
					if (g.MyDatabase.m_order_editing.lines.size()==0)
					{
						tvStatistics.setText("0");
					} else
					{
						StringBuilder sb=new StringBuilder();
						sb.append(Integer.toString(g.MyDatabase.m_order_editing.lines.size()));
						sb.append(": ").append(Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.sumDoc, "%.3f")).append(getString(R.string.default_currency));
						sb.append(", ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
						tvStatistics.setText(sb.toString());
					}
				}
			}
		}
	}
	*/
	
	private LocationListener locListener = new LocationListener()
    {
		@Override
		public void onLocationChanged(Location loc) 
		{
			MySingleton g=MySingleton.getInstance();
			if (loc!=null)
			{
				m_locationManagerInRefund.removeUpdates(locListener);
				
				boolean gpsenabled = m_locationManagerInRefund.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				g.MyDatabase.m_refund_editing.latitude=loc.getLatitude();
				g.MyDatabase.m_refund_editing.longitude=loc.getLongitude();
				g.MyDatabase.m_refund_editing.datecoord=Common.getDateTimeAsString14(new Date(loc.getTime()));
				g.MyDatabase.m_refund_editing.gpsstate=gpsenabled?1:loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)?2:0;
				g.MyDatabase.m_refund_editing.gpsaccuracy=loc.getAccuracy();
				g.MyDatabase.m_refund_editing.accept_coord=0;
				
				int viewId=pager.getId();
				int position=2;
				Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
				if (frag0!=null)
				{
					View view0=frag0.getView();
					if (view0!=null)
					{
						final TextView tvLatitude = (TextView) view0.findViewById(R.id.textViewRefundLatitudeValue);
						tvLatitude.setText(Double.toString(g.MyDatabase.m_refund_editing.latitude));
						final TextView tvLongitude = (TextView) view0.findViewById(R.id.textViewRefundLongitudeValue);
						tvLongitude.setText(Double.toString(g.MyDatabase.m_refund_editing.longitude));
						final TextView tvDateCoord = (TextView) view0.findViewById(R.id.textViewRefundDateCoordValue);
						tvDateCoord.setText(g.MyDatabase.m_refund_editing.datecoord);
				    	
				    	final CheckBox cbReceiveCoord=(CheckBox)view0.findViewById(R.id.checkBoxRefundReceiveCoord);
				    	cbReceiveCoord.setChecked(g.MyDatabase.m_refund_editing.accept_coord==1);
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
					g.MyDatabase.m_order_editing.version++;
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
		bCreatedInSingleton=g.MyDatabase.m_refund_editing.bCreatedInSingleton;
		
		g.MyDatabase.m_refund_editing_id=savedInstanceState.getLong("refund_editing_id");
		g.MyDatabase.m_refund_editing_created=savedInstanceState.getBoolean("refund_editing_created");
		g.MyDatabase.m_refund_editing_modified=savedInstanceState.getBoolean("refund_editing_modified");
		
		g.MyDatabase.m_refund_editing=savedInstanceState.getParcelable("refund_editing");
		
	   	m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
	    m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
	    m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
	    m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");
	    
		return bCreatedInSingleton;
    }	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();
		
		outState.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
		outState.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
		outState.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
		
		outState.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
		
		outState.putStringArray("list_accounts_descr", m_list_accounts_descr);
		outState.putStringArray("list_accounts_id", m_list_accounts_id);
		outState.putStringArray("list_stocks_descr", m_list_stocks_descr);
		outState.putStringArray("list_stocks_id", m_list_stocks_id);
		
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
		
	    setContentView(R.layout.refund_pager);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	    if (g.Common.PHARAOH)
	    {
	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    }
	    
	    boolean bCreatedInSingleton=false;
	    boolean bRecreationActivity=false; 
	    if (savedInstanceState!=null&&savedInstanceState.containsKey("refund_editing"))
	    {
	    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
	    	// но Singleton при этом не всегда уничтожался
	    	bCreatedInSingleton=HandleBundle(savedInstanceState);
	    }
	    
	    //if (!bCreatedInSingleton)
	    //{
	    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
	    //}
	    
		if (ob1!=null)
		{
			// При повороте экрана нам не нужно, чтобы изменился адаптер (он не перерисуется в этом случае на одном из экранов)
			//g.MyDatabase.m_refundLinesAdapter=null;
		} else
		{
			g.MyDatabase.m_refundLinesAdapter=null;
		}
	    
	    SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

		m_work_date=DatePreference.getDateFor(sharedPreferences, "work_date");

		if (bCreatedInSingleton)
		{
			
		} else
		{
		   	Intent intent=getIntent();
		   	
		   	Bundle bundle=intent.getBundleExtra("extrasBundle");
		    //m_list_stocks_descr = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_descr"));
		    //m_list_stocks_id = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_id"));
	
		   	m_list_accounts_descr = bundle.getStringArray("list_accounts_descr");
		    m_list_accounts_id = bundle.getStringArray("list_accounts_id");
		   	
		   	//m_list_prices_descr = bundle.getStringArray("list_prices_descr");
		    //m_list_prices_id = bundle.getStringArray("list_prices_id");
		   	
		    m_list_stocks_descr = bundle.getStringArray("list_stocks_descr");
		    m_list_stocks_id = bundle.getStringArray("list_stocks_id");
		   	
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
		    
		    if (!bRecreationActivity&&g.MyDatabase.m_refund_editing_created)
		    //if (g.MyDatabase.m_order_editing_id==0)
		    {
		    	// это новый документ
		    	// контрагент в документ попал автоматически из отбора
		    	if (!g.MyDatabase.m_refund_editing.client_id.isEmpty())
		    	{
		    		// установим договор по умолчанию, если он единственный
			        Cursor defaultAgreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
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
		}
	    
	    /*
	    if (intent.getBooleanExtra("recalc_price", false))
	    {
	    	recalcPrice();
	    }
	    */
	   	
	    pager = (ViewPager) findViewById(R.id.pagerRefund);
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
	        	/*
	    	    if (Common.PHARAOH)
	    	    {
	    	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    	    } else
	    	    {
	    	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	    	    }
	    	    */
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
	    
        m_locationManagerInRefund = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

	    if (g.MyDatabase.m_refund_editing.accept_coord==1&&m_locationManagerInRefund!=null)
	    {
	    	// здесь не проверяем состояние, отработана заявка или нет
	    	// координаты принимаются всегда, однако флаг измененности заявки не устанавливаем
	    	// при получении координат
	    	
	        Criteria criteria = new Criteria();
			criteria.setAccuracy( Criteria.ACCURACY_COARSE );
			String provider = m_locationManagerInRefund.getBestProvider( criteria, true );
	    	
	    	//m_locationManagerInOrder.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
			if (provider!=null)
			{
				m_locationManagerInRefund.requestLocationUpdates(provider,  0, 0, locListener);
			}
	    }
	    
	  }
	    
	    	
	  @Override
		protected Dialog onCreateDialog(int id) {
		  MySingleton g=MySingleton.getInstance();
		  RefundRecord rec=g.MyDatabase.m_refund_editing;
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
				builder.setTitle(getResources().getString(R.string.refund_modiffied));
				builder.setMessage(getResources().getString(R.string.save_it_before_closing));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						MySingleton g=MySingleton.getInstance();
						StringBuffer reason=new StringBuffer();
						if (refundCanBeSaved(reason)||g.MyDatabase.m_refund_editing.dont_need_send==1)
						{
							Intent intent=new Intent();
							setResult(REFUND_RESULT_OK, intent);
							m_locationManagerInRefund.removeUpdates(locListener);
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
						setResult(REFUND_RESULT_CANCEL, intent);
						m_locationManagerInRefund.removeUpdates(locListener);
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
						MySingleton g=MySingleton.getInstance();
						g.MyDatabase.m_refund_editing.lines.clear();
						g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
						setModified(true);
						recalcWeight();
						redrawWeight();
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
				builder.setTitle(getResources().getString(R.string.refund_cant_be_saved));
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
			MySingleton g=MySingleton.getInstance();
			RefundRecord rec=g.MyDatabase.m_refund_editing;
			
			switch (id)
			{
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
			case IDD_CANT_SAVE:
				//m_reason_cant_save=bundle.getString("reason");
				break;
			}
		}
	  
@Override
public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.order, menu);
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
	MySingleton g=MySingleton.getInstance();
	
	switch (item.getItemId())
	{
	case R.id.action_save:
	{
		StringBuffer reason=new StringBuffer();
		if (refundCanBeSaved(reason)||g.MyDatabase.m_refund_editing.dont_need_send==1)
		{
			Intent intent=new Intent();
			setResult(REFUND_RESULT_OK, intent);
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
		setResult(REFUND_RESULT_CANCEL, intent);
		finish();
		break;
	}
	case R.id.action_settings:
		Intent intent=new Intent(RefundActivity.this, PrefRefundActivity.class);
		startActivity(intent);
		break;
	}
  
  return super.onOptionsItemSelected(item);
}




@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	MySingleton g=MySingleton.getInstance();
	// https://code.google.com/p/android-icon-context-menu/
	
	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo; 
			
	//menu.setHeaderTitle(getString(R.string.menu_context_title));
	MenuInflater inflater = getMenuInflater();
	switch (v.getId()) {
	case R.id.listViewRefundLines:
	{
		if (g.Common.PHARAOH)
		{
			//int pos_group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int pos_child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			//Toast.makeText(OrderActivity.this, String.valueOf(pos_group)+"/"+String.valueOf(pos_child), Toast.LENGTH_LONG).show();
			
			if (pos_child>=0)
			{
				if (pos_child>=0)
				{
					inflater.inflate(R.menu.refund_lines_list, menu);
					
					MenuItem itemChangePrice = menu.findItem(R.id.action_change_price);
					MenuItem itemMoveDown = menu.findItem(R.id.action_move_down);
					MenuItem itemMoveUp = menu.findItem(R.id.action_move_up);
					
					if (itemChangePrice!=null)
					{
						itemChangePrice.setVisible(false);
					}
					itemMoveDown.setVisible(false);
					itemMoveUp.setVisible(false);
				}
			}
			
		} else
		{
			//ExpandableListView elvOrderLines=(ExpandableListView)v;
			inflater.inflate(R.menu.refund_lines_list, menu);
			
			MenuItem itemChangePrice = menu.findItem(R.id.action_change_price);
			MenuItem itemMoveDown = menu.findItem(R.id.action_move_down);
			MenuItem itemMoveUp = menu.findItem(R.id.action_move_up);
			
			int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			if (pos>=0&&pos<g.MyDatabase.m_refund_editing.lines.size())
			{
				if (itemChangePrice!=null)
				{
					itemChangePrice.setVisible((g.MyDatabase.m_refund_editing.lines.get(pos).stuff_nomenclature_flags&1)!=0);
				}
				itemMoveDown.setVisible(pos<g.MyDatabase.m_refund_editing.lines.size()-1);
				itemMoveUp.setVisible(pos>0);
			}
		}
	}
	break;
	}
}

@Override
public boolean onContextItemSelected(MenuItem item) {
	// TODO Auto-generated method stub
	MySingleton g=MySingleton.getInstance();
	
	switch (item.getItemId())
	{
	case R.id.action_delete_line:
	{
		//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
		g.MyDatabase.m_refund_editing.lines.remove(pos);
		g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
		setModified(true);
		recalcWeight();
		redrawWeight();
		return true;
	}
	case R.id.action_clear:
	{
		showDialog(IDD_CLEAR_LINES);
		return true;
	}
	/*
	case R.id.action_change_quantity:
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
		OrderLineRecord line=g.MyDatabase.m_order_editing.lines.get(pos);
		m_order_editing_line_num=pos;
		Intent intent=new Intent(OrderActivity.this, QuantityActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intent.putExtra("id", line.nomenclature_id.toString());
	    intent.putExtra("quantity", line.quantity);
	    intent.putExtra("k", line.k);
	    intent.putExtra("ed", line.ed);
	    intent.putExtra("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
	    
	    // добавлено 30.11.2013, раньше было - только из списка номенклатуры
		ContentValues cv=new ContentValues();
		cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());
	    getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
	    //
	    
	    // TODO устанавливать только при изменении, см. также NomenclatureActitivity, там такой же код
	    TextDatabase.prepareNomenclatureStuff(getContentResolver());
	    
	    Cursor restCursor=getContentResolver().query(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, new String[]{"nom_quantity"}, "nomenclature.id=?", new String[]{line.nomenclature_id.toString()}, null);
	    if (restCursor.moveToFirst())
	    {
		    intent.putExtra("rest", restCursor.getDouble(0));
	    }
	    restCursor.close();
	    
		startActivityForResult(intent, QUANTITY_REQUEST);
		return true;
	}
	*/
	case R.id.action_move_down:
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
		RefundLineRecord temp=g.MyDatabase.m_refund_editing.lines.get(pos);
		temp=g.MyDatabase.m_refund_editing.lines.set(pos+1, temp);
		g.MyDatabase.m_refund_editing.lines.set(pos, temp);
		g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
		setModified(true);
		return true;
	}
	case R.id.action_move_up:
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
		RefundLineRecord temp=g.MyDatabase.m_refund_editing.lines.get(pos);
		temp=g.MyDatabase.m_refund_editing.lines.set(pos-1, temp);
		g.MyDatabase.m_refund_editing.lines.set(pos, temp);
		g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
		setModified(true);
		return true;
	}
	}
	return super.onContextItemSelected(item);
}

@Override
// возвращает true в случае, если клиент изменился
public boolean onRefundClientSelected(View view0, long _id)
{
	MySingleton g=MySingleton.getInstance();
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
	    	if (g.MyDatabase.m_refund_editing.client_id.toString().equals(clientId))
	    	{
	    		return false;
	    	}
	    	String clientDescr = clientsCursor.getString(descrIndex);
	    	String clientAddress = clientsCursor.getString(addressIndex);
	    	String curatorId = clientsCursor.getString(curator_idIndex);
	    	int flags = clientsCursor.getInt(flags_Index); 
	    	
	    	g.MyDatabase.m_refund_editing.client_id=new MyID(clientId);
	    	g.MyDatabase.m_refund_editing.stuff_client_name=clientDescr;
	    	g.MyDatabase.m_refund_editing.stuff_client_address=clientAddress;
	    	g.MyDatabase.m_refund_editing.stuff_select_account=(flags&2)!=0;
	    	// очистим торговую точку
	    	g.MyDatabase.m_refund_editing.distr_point_id=new MyID();
	    	g.MyDatabase.m_refund_editing.stuff_distr_point_name="";
			// Долг контрагента
		    Cursor cursorDebt=getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "client_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
		    if (cursorDebt.moveToNext())
		    {
		    	int indexDebt = cursorDebt.getColumnIndex("saldo");
		    	int indexDebtPast = cursorDebt.getColumnIndex("saldo_past");
				g.MyDatabase.m_refund_editing.stuff_debt=cursorDebt.getDouble(indexDebt); 
				g.MyDatabase.m_refund_editing.stuff_debt_past=cursorDebt.getDouble(indexDebtPast);
		    } else
		    {
		    	g.MyDatabase.m_refund_editing.stuff_debt=0.0;
		    	g.MyDatabase.m_refund_editing.stuff_debt_past=0.0;
		    }
		    // Куратор
		    Cursor curatorsCursor=getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{curatorId}, null);
		    String curatorDescr;
		    if (curatorsCursor!=null &&curatorsCursor.moveToNext())
		    {
		    	int curator_descrIndex = curatorsCursor.getColumnIndex("descr");
		    	curatorDescr = curatorsCursor.getString(curator_descrIndex);
		    	g.MyDatabase.m_refund_editing.curator_id=new MyID(curatorId);
		    	g.MyDatabase.m_refund_editing.stuff_curator_name=curatorDescr;
		    } else
		    {
		    	g.MyDatabase.m_refund_editing.curator_id=new MyID();
		    	g.MyDatabase.m_refund_editing.stuff_curator_name="";
		    	curatorDescr = getResources().getString(R.string.curator_not_set);
		    	g.MyDatabase.m_refund_editing.stuff_debt=0.0;
		    	g.MyDatabase.m_refund_editing.stuff_debt_past=0.0;
		    }
		    
	    	if (view0!=null)
	    	{
	    		// Клиент
	    		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
	    		etClient.setText(clientDescr);
				// и его адрес
				TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewRefundClientAddress);
				tvClientAddress.setText(clientAddress);
	    		// Тип учета
	    		TextView tvAccounts=(TextView)view0.findViewById(R.id.tvAccount);
	    		Spinner spinnerAccounts=(Spinner)view0.findViewById(R.id.spinnerAccount);
	    		if (g.MyDatabase.m_refund_editing.stuff_select_account)
    			{
    				tvAccounts.setVisibility(View.VISIBLE);
    				spinnerAccounts.setVisibility(View.VISIBLE);
    			} else
    			{
    				tvAccounts.setVisibility(View.GONE);
    				spinnerAccounts.setVisibility(View.GONE);
    			}
	    		// Куратор
	    		EditText etCurator=(EditText)view0.findViewById(R.id.editTextCurator);
	    		etCurator.setText(curatorDescr);
				// Долг
				EditText etDebt = (EditText) view0.findViewById(R.id.editTextDebt);
				etDebt.setText(String.format("%.2f", g.MyDatabase.m_refund_editing.stuff_debt));
				EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextDebtPast);
				etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_refund_editing.stuff_debt_past));
				// Торговая точка
				EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
				etDistrPoEditText.setText(g.MyDatabase.m_refund_editing.stuff_distr_point_name);
				
				// Остатки оборудования у клинта
				EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipment");
				if (fragment1!=null)
					fragment1.myRestartLoader();
				EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipmentTare");
				if (fragment2!=null)
				{
					fragment2.myRestartLoader();
				}
	    	}

	    	clientsCursor.close();
	    	curatorsCursor.close();
		    cursorDebt.close();
	    	
	    	return true;
	    }
		clientsCursor.close();
	}
	
	// Клиент не был указан и не изменился
	if (g.MyDatabase.m_refund_editing.client_id.isEmpty())
		return false;
	// Клиент был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
		etClient.setText(getResources().getString(R.string.client_not_set));
		TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewRefundClientAddress);
		tvClientAddress.setText("");
		EditText etCurator=(EditText)view0.findViewById(R.id.editTextCurator);
		etCurator.setText(getResources().getString(R.string.curator_not_set));
		EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
		etDistrPoEditText.setText(getResources().getString(R.string.trade_point_not_set));
	}
	g.MyDatabase.m_refund_editing.client_id=new MyID();
	g.MyDatabase.m_refund_editing.stuff_client_name="";
	g.MyDatabase.m_refund_editing.stuff_client_address="";
	g.MyDatabase.m_refund_editing.curator_id=new MyID();
	g.MyDatabase.m_refund_editing.stuff_curator_name="";
	g.MyDatabase.m_refund_editing.distr_point_id=new MyID();
	g.MyDatabase.m_refund_editing.stuff_distr_point_name="";
	
	return true;
}

@Override
//возвращает true в случае, если договор изменился
public boolean onRefundAgreementSelected(View view0, long _id)
{
	MySingleton g=MySingleton.getInstance();
	if (_id!=0)
	{
	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS_CONTENT_URI, _id);
	    Cursor agreementsCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "organization_id", "price_type_id", "sale_id"}, null, null, null);
	    if (agreementsCursor!=null &&agreementsCursor.moveToNext())
	    {
	    	int idIndex = agreementsCursor.getColumnIndex("id");
	    	int descrIndex = agreementsCursor.getColumnIndex("descr");
	    	int organization_idIndex = agreementsCursor.getColumnIndex("organization_id");
	    	int priceType_idIndex = agreementsCursor.getColumnIndex("price_type_id");
	    	String agreementId = agreementsCursor.getString(idIndex);
	    	String agreementDescr = agreementsCursor.getString(descrIndex);
	    	String organizationId = agreementsCursor.getString(organization_idIndex);
	    	//String priceTypeId = agreementsCursor.getString(priceType_idIndex);
	    	if (g.MyDatabase.m_refund_editing.agreement_id.toString().equals(agreementId))
	    	{
	    		return false;
	    	}
	    	if (view0!=null)
	    	{
		    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
		    	etAgreement.setText(agreementDescr);
	    	}
	    	g.MyDatabase.m_refund_editing.agreement_id=new MyID(agreementId);
	    	g.MyDatabase.m_refund_editing.stuff_agreement_name=agreementDescr;
	    	// и заполняем организацию
		    Cursor cursorOrganizations=getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId}, null);
		    if (cursorOrganizations!=null &&cursorOrganizations.moveToNext())
		    {
		    	int descrIndexOrganization = cursorOrganizations.getColumnIndex("descr");
		    	String descrOrganization = cursorOrganizations.getString(descrIndexOrganization);
				g.MyDatabase.m_refund_editing.organization_id=new MyID(organizationId);
				g.MyDatabase.m_refund_editing.stuff_organization_name=descrOrganization;
		    	if (view0!=null)
		    	{
			    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewRefundOrganization);
			    	tvOrganization.setText(descrOrganization);
		    	}
		    } else
		    {
		    	if (view0!=null)
		    	{
			    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewRefundOrganization);
			    	tvOrganization.setText(getResources().getString(R.string.organization_not_set));
		    	}
		    	g.MyDatabase.m_refund_editing.organization_id.m_id="";
		    	g.MyDatabase.m_refund_editing.stuff_organization_name=getResources().getString(R.string.item_not_set);
		    }
		    
		    if (g.Common.TANDEM)
		    {
		    	// При изменении договора пересчитываем тару
				EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipment");
				if (fragment1!=null)
					fragment1.myRestartLoader();
				EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipmentTare");
				if (fragment2!=null)
				{
					fragment2.myRestartLoader();
				}
		    }
		    
	    	return true;
	    }
	}
	// Договор не был указан и не изменился
	if (g.MyDatabase.m_refund_editing.agreement_id.isEmpty())
		return false;
	// Договор был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewRefundOrganization);
    	etAgreement.setText(getResources().getString(R.string.agreement_not_set));
    	tvOrganization.setText(getResources().getString(R.string.organization_not_set));
	}    	
	g.MyDatabase.m_refund_editing.agreement_id=new MyID();
	g.MyDatabase.m_refund_editing.stuff_agreement_name="";
	g.MyDatabase.m_refund_editing.organization_id=new MyID();
	g.MyDatabase.m_refund_editing.stuff_organization_name="";
	
	return true;
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);

    if (data == null) 
    	return;
    
}

@Override
protected void onDestroy() {
	m_locationManagerInRefund.removeUpdates(locListener);
	super.onDestroy();
}

private void onCloseActivity()
{
	MySingleton g=MySingleton.getInstance();
	if (g.MyDatabase.m_refund_editing_modified)
	{
		showDialog(IDD_CLOSE_QUERY);
	} else
	{
		Intent intent=new Intent();
		setResult(REFUND_RESULT_CANCEL, intent);
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
public void someRefundEvent(String s) {
	MySingleton g=MySingleton.getInstance();
	if (s.equals("coord"))
	{
		if (g.MyDatabase.m_refund_editing.accept_coord==1)
		{
			m_locationManagerInRefund.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
		} else
		{
			m_locationManagerInRefund.removeUpdates(locListener);
		}
	} else
	if (s.equals("close"))
	{
		onCloseActivity();
	} else
	if (s.equals("ok"))
	{
		StringBuffer reason=new StringBuffer();
		if (refundCanBeSaved(reason)||g.MyDatabase.m_refund_editing.dont_need_send==1)
		{
			Intent intent=new Intent();
			setResult(REFUND_RESULT_OK, intent);
			// 16.01.2013
			m_locationManagerInRefund.removeUpdates(locListener);
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

    @Override
    public void onButtonsOkCancel_Ok(Integer dialogId, Intent intent) {
        MySingleton g=MySingleton.getInstance();

        MyDatabase.RefundRecord rec=g.MyDatabase.m_refund_editing;

        switch (dialogId) {
            case MY_ALERT_DIALOG_IDD_DATE:
            {
                int day = Integer.parseInt(rec.datedoc.substring(6, 8));
                int month = Integer.parseInt(rec.datedoc.substring(4, 6)) - 1;
                int year = Integer.parseInt(rec.datedoc.substring(0, 4));

                int selectedYear = intent.getIntExtra("year", year);
                int selectedMonth = intent.getIntExtra("month", month);
                int selectedDay = intent.getIntExtra("day", day);

                if (year != selectedYear || month != selectedMonth || day != selectedDay) {
                    rec.datedoc = Common.MyDateFormat("yyyyMMddHHmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                    int viewId = pager.getId();
                    int position = 0;
                    Fragment frag0 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                    if (frag0 != null) {
                        View view0 = frag0.getView();

                        EditText etDate = (EditText) view0.findViewById(R.id.etDate);
                        StringBuilder sb=new StringBuilder();
                        sb.append(Common.dateStringAsText(rec.datedoc));
                        etDate.setText(sb.toString());

                        setModified(true);
                    }
                }
            }
            break;
        }
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	    public MyFragmentPagerAdapter(FragmentManager fm) {
	      super(fm);
	    }

	    @Override
	    public Fragment getItem(int position) {
	    	return RefundPageFragment.newInstance(position);
	    }

	    @Override
	    public int getCount() {
	    	if (orientationLandscape)
	    		return PAGE_COUNT_LANDSCAPE; 
	      return PAGE_COUNT;
	    }
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	String[] titles = getResources().getStringArray(R.array.tabs_refund);
	    	if (orientationLandscape)
	    	{
		    	titles = getResources().getStringArray(R.array.tabs_refund_landscape);
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
		MySingleton g=MySingleton.getInstance();

		Calendar work_date= DatePreference.getDateFor(sharedPreferences, "work_date");

		if (!m_work_date.equals(work_date))
		{
			// изменили рабочую дату - изменим дату заказа
			m_work_date=work_date;
			
			RefundRecord rec=g.MyDatabase.m_refund_editing;
			
			rec.datedoc=Common.MyDateFormat("yyyyMMddHHmmss", m_work_date.getTime());
	
			int viewId=pager.getId();
			int position=0;
			Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
			View view0=frag0.getView();
			EditText etDate = (EditText) view0.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(Common.dateStringAsText(rec.datedoc));
			etDate.setText(sb.toString());
			
			setModified(true);
		}

		/*
		if (m_nHideFormatSettings)
		{
			Preference customPref = (Preference) findPreference("data_format");
			getPreferenceScreen().removePreference(customPref);
		}
		*/
		super.onResume();
	}
		
	private void redrawWeight()
	{
		/*
		int i;
		for (i=0; i< pagerAdapter.getCount(); i++)
		{
			Callbacks cb=(Callbacks) pagerAdapter.getItem(i);
			if (cb!=null)
				cb.onRedrawSumWeightCallback();
		}
		*/
		
		Intent intent = new Intent(ACTION);
		//intent.putExtra("test", "test");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
	}

}

