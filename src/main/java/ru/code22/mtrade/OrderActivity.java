package ru.code22.mtrade;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import org.apache.commons.net.ftp.FTPClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import ru.code22.mtrade.OrderPageFragment.onSomeEventListener;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;

import android.text.Html;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class OrderActivity extends AppCompatActivity implements onSomeEventListener, OrderAlertDialogFragment.AlertDialogFragmentEventListener {

	
	static final String LOG_TAG = "mtradeLogs";
	static final int PAGE_COUNT = 3;
	static final int PAGE_COUNT_LANDSCAPE = 2;
	
	static final String ACTION = "refreshSumWeight";
	
	// в MainActivity 
	static final int ORDER_RESULT_OK=1;
	static final int ORDER_RESULT_CANCEL=2;
	
	// из фрагмента
	//static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    //static final int NT_FROM_ORDER_REQUEST = 2;
    //static final int SELECT_AGREEMENT_FROM_ORDER_REQUEST = 3;
    //static final int OPEN_NOMENCLATURE_FROM_ORDER_REQUEST = 4;
    //static final int SELECT_TRADE_POINT_FROM_ORDER_REQUEST = 5;
	static final int QUANTITY_REQUEST = 6;
	
    public static final int IDD_DATE = 1;
    public static final int IDD_CLOSE_QUERY = 2;
    public static final int IDD_CLEAR_LINES = 3;
    public static final int IDD_CANT_SAVE=4;
    public static final int IDD_SHIPPING_BEGIN_TIME = 5;
    public static final int IDD_SHIPPING_END_TIME = 6;
	public static final int IDD_SHOW_PAYMENTS_STATS_PDA = 7;

	private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ONE_ORDER = 1;
    
    private static final int FTP_STATE_LOAD_IN_BASE=1;
    private static final int FTP_STATE_SUCCESS=2;
    private static final int FTP_STATE_FINISHED_ERROR=3;
    private static final int FTP_STATE_SEND_EOUTF=4;
    private static final int FTP_STATE_RECEIVE_ARCH=5;
    private static final int FTP_STATE_LOAD_HISTORY_IN_BASE=6;
    private static final int FTP_STATE_RECEIVE_IMAGE=7;

    public static final int MY_ALERT_DIALOG_IDD_DATE=1;
    public static final int MY_ALERT_DIALOG_IDD_SHIPPING_DATE=2;
    public static final int MY_ALERT_DIALOG_IDD_BEGIN_TIME=3;
    public static final int MY_ALERT_DIALOG_IDD_END_TIME=4;

	ViewPager pager;
	MyFragmentPagerAdapter pagerAdapter;
	boolean orientationLandscape;
	
    private LocationManager m_locationManagerInOrder;
	
	//public static String[] m_list_organizations_descr;
	//public static String[] m_list_organizations_id;
    
	public static String[] m_list_accounts_descr;
	public static String[] m_list_accounts_id; // здесь числа ввиде строки

	public static String[] m_list_prices_descr;
	public static String[] m_list_prices_id;
	
	public static String[] m_list_stocks_descr;
	public static String[] m_list_stocks_id;

	
	// Запоминаем рабочую дату при открытии заказа
	Calendar m_work_date;
	//
	String m_reason_cant_save;
	String m_payments_stats_pda;
	
	long m_copy_order_id; // если объект введен копированием, здесь будет код копируемого
	// это нужно для того, чтобы при восстановлении приложения восстановились данные (если еще не успела произойти запись в базу, при изменении)
	String m_backup_client_id; // с отбором по какому клиенту был создан заказ
	String m_backup_distr_point_id;
	
	protected void setModified(boolean flag)
	{
		MySingleton g=MySingleton.getInstance();
		if (g.MyDatabase.m_order_editing_modified!=flag)
		{
			if (flag)
			{
				setTitle(R.string.title_activity_order_changed);
			} else
			{
				setTitle(R.string.title_activity_order);
			}
			g.MyDatabase.m_order_editing_modified=flag;
		}
		if (flag&&g.Common.NEW_BACKUP_FORMAT&&g.MyDatabase.m_order_new_editing_id==0)
		{
			// Что-то поменяли в документе впервые, записываем документ
			g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
			// editing_backup (последний параметр)
			// 0-документ в нормальном состоянии
			// 1-новый документ, записать не успели
			// 2-документ начали редактировать, но не записали и не отменили изменения
			g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
		}
	}
	
	protected void recalcSumWeight()
	{
		MySingleton g=MySingleton.getInstance();
		g.MyDatabase.m_order_editing.sumDoc=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
		g.MyDatabase.m_order_editing.weightDoc=TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
	}
	
	//@Override
	public boolean orderCanBeSaved(StringBuffer reason)
	{
		MySingleton g=MySingleton.getInstance();
		//if (g.MyDatabase.m_order_editing.dont_need_send==1)
		//	return true;
		if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
		{
			Cursor clientsCursor=getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"blocked"}, "id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
			int blockedIndex=clientsCursor.getColumnIndex("blocked");
			if (clientsCursor.moveToNext())
			{
				if (clientsCursor.getInt(blockedIndex)==1)
				{
					if (reason==null)
					{
						Snackbar.make(findViewById(android.R.id.content), R.string.error_client_blocked, Snackbar.LENGTH_LONG).show();
					} else
					{
						reason.append(getString(R.string.error_client_blocked));
					}
					clientsCursor.close();
					return false;
				}
			}
			clientsCursor.close();
		}
		String strWarning="";
		if (g.MyDatabase.m_order_editing.create_client==1)
		{
			if (g.MyDatabase.m_order_editing.create_client_lastname.trim().isEmpty()&&g.MyDatabase.m_order_editing.create_client_firstname.trim().isEmpty()&&g.MyDatabase.m_order_editing.create_client_surname.trim().isEmpty())
			{
				//strWarning+=",клиент";
				strWarning+=","+getString(R.string.field_client);
			}
		} else
		{
			if (g.MyDatabase.m_order_editing.client_id.isEmpty())
			{
				//strWarning+=",клиент";
				strWarning+=","+getString(R.string.field_client);
			}
		}
		if (g.Common.FACTORY&&!g.MyDatabase.m_order_editing.stuff_agreement_required)
		{} else
		if (g.MyDatabase.m_order_editing.agreement_id.isEmpty()&&!g.Common.MEGA&&!g.Common.PHARAON)
		{
			//strWarning+=",договор";
			strWarning+=","+getString(R.string.field_agreement);
		}
		if (g.MyDatabase.m_order_editing.stock_id.isEmpty()&&!g.Common.MEGA&&!g.Common.PHARAON)
		{
			//strWarning+=",склад";
			strWarning+=","+getString(R.string.field_stock);
		}
		if (g.MyDatabase.m_order_editing.price_type_id.isEmpty()&&!g.Common.PHARAON)
		{
			//strWarning+=",тип цены";
			strWarning+=","+getString(R.string.field_price_type);
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
		if (g.Common.PHARAON)
		{
			if (g.MyDatabase.m_order_editing.shipping_begin_time.isEmpty()||g.MyDatabase.m_order_editing.shipping_date.isEmpty())
			{
				if (reason==null)
				{
					//Toast.makeText(this, "Интервал доставки: дата окончания не может быть меньше даты начала", Toast.LENGTH_LONG).show();
					Snackbar.make(findViewById(android.R.id.content), R.string.error_service_time_not_set, Snackbar.LENGTH_LONG).show();
				} else
				{
					reason.append(getString(R.string.error_service_time_not_set));
				}
				return false;
			}
		} else
		{
			if (g.MyDatabase.m_order_editing.shipping_time!=0 && g.MyDatabase.m_order_editing.shipping_begin_time.compareTo(g.MyDatabase.m_order_editing.shipping_end_time)>0)
			{
				if (reason==null)
				{
					//Toast.makeText(this, "Интервал доставки: дата окончания не может быть меньше даты начала", Toast.LENGTH_LONG).show();
					Snackbar.make(findViewById(android.R.id.content), R.string.error_shipping_interval, Snackbar.LENGTH_LONG).show();
				} else
				{
					reason.append(getString(R.string.error_shipping_interval));
				}
				return false;
			}
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
						sb.append(": ").append(g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.sumDoc, "%.3f")).append(getString(R.string.default_currency));
						sb.append(", ").append(getString(R.string.weight)).append(" ").append(g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
						tvStatistics.setText(sb.toString());
					}
				}
			}
		}
	}
	*/

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		boolean bFineLocationGranted=false;
		boolean bCoarseLocationGranted=false;
		int i;
		for (i=0; i<grantResults.length; i++)
		{
			if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
			{
				if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION))
					bFineLocationGranted=true;
				if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
					bCoarseLocationGranted=true;
			}
		}

		if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ONE_ORDER) {
			if (bFineLocationGranted) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				String provider = m_locationManagerInOrder.getBestProvider(criteria, true);
				if (provider != null)
				{
					try {
						m_locationManagerInOrder.requestLocationUpdates(provider, 0, 0, locListener);
					} catch (SecurityException e)
					{
					}
				}
			} else
			if (bCoarseLocationGranted) {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				String provider = m_locationManagerInOrder.getBestProvider(criteria, true);
				if (provider != null)
				{
					try {
						m_locationManagerInOrder.requestLocationUpdates(provider, 0, 0, locListener);
					} catch (SecurityException e)
					{
					}
				}
			} else
			{
				// Отказано в доступе, запишем дату отказа
				MySingleton g=MySingleton.getInstance();

				ContentValues cv=new ContentValues();
				cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION);
				cv.put("datetime", Common.getDateTimeAsString14(null));
				cv.put("seance_incoming", g.MyDatabase.m_seance.toString());
				getContentResolver().insert(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, cv);

			}
		}
	}

	void startGPS() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				//ActivityCompat.requestPermissions(this, new String[]{permission.toStringValue()}, permission.ordinal());
				MySingleton g = MySingleton.getInstance();
				Cursor permissionsCursor = getContentResolver().query(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, new String[]{"datetime", "seance_incoming"}, "permission_name=?", new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, null);
				if (permissionsCursor.moveToFirst()) {
					int seanceIncomingIndex = permissionsCursor.getColumnIndex("seance_incoming");
					String seanceIncoming = permissionsCursor.getString(seanceIncomingIndex);
					if (seanceIncoming.equals(g.MyDatabase.m_seance.toString())) {
						// в этом сеансе уже спрашивали
						return;
					}
				}
				// Записываем время, когда запросили разрешения
				// (чтобы 2 раза не спрашивать)
				ContentValues cv = new ContentValues();
				cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION);
				cv.put("datetime", Common.getDateTimeAsString14(null));
				cv.put("seance_incoming", g.MyDatabase.m_seance.toString());
				getContentResolver().insert(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, cv);

				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ONE_ORDER);
			} else {
				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_COARSE);
				String provider = m_locationManagerInOrder.getBestProvider(criteria, true);
				if (provider != null) {
					m_locationManagerInOrder.requestLocationUpdates(provider, 0, 0, locListener);
				}
			}
		} else {
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			String provider = m_locationManagerInOrder.getBestProvider(criteria, true);
			if (provider != null) {
				m_locationManagerInOrder.requestLocationUpdates(provider, 0, 0, locListener);
			}
		}
	}


	
	private LocationListener locListener = new LocationListener()
    {
		@Override
		public void onLocationChanged(Location loc) 
		{
			MySingleton g=MySingleton.getInstance();
			if (loc!=null)
			{
				m_locationManagerInOrder.removeUpdates(locListener);
				
				boolean gpsenabled = m_locationManagerInOrder.isProviderEnabled(LocationManager.GPS_PROVIDER);
				
				g.MyDatabase.m_order_editing.latitude=loc.getLatitude();
				g.MyDatabase.m_order_editing.longitude=loc.getLongitude();
				g.MyDatabase.m_order_editing.datecoord=Common.getDateTimeAsString14(new Date(loc.getTime()));
				g.MyDatabase.m_order_editing.gpsstate=gpsenabled?1:loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)?2:0;
				g.MyDatabase.m_order_editing.gpsaccuracy=loc.getAccuracy();
				g.MyDatabase.m_order_editing.accept_coord=0;
				
				int viewId=pager.getId();
				int position=2;
				Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
				if (frag0!=null)
				{
					View view0=frag0.getView();
					if (view0!=null)
					{
						final TextView tvLatitude = (TextView) view0.findViewById(R.id.textViewOrderLatitudeValue);
						tvLatitude.setText(Double.toString(g.MyDatabase.m_order_editing.latitude));
						final TextView tvLongitude = (TextView) view0.findViewById(R.id.textViewOrderLongitudeValue);
						tvLongitude.setText(Double.toString(g.MyDatabase.m_order_editing.longitude));
						final TextView tvDateCoord = (TextView) view0.findViewById(R.id.textViewOrderDateCoordValue);
						tvDateCoord.setText(g.MyDatabase.m_order_editing.datecoord);
				    	
				    	final CheckBox cbReceiveCoord=(CheckBox)view0.findViewById(R.id.checkBoxOrderReceiveCoord);
				    	cbReceiveCoord.setChecked(g.MyDatabase.m_order_editing.accept_coord==1);
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
	    bCreatedInSingleton=g.MyDatabase.m_order_editing.bCreatedInSingleton;
		
		g.MyDatabase.m_order_editing_id=savedInstanceState.getLong("order_editing_id");
		g.MyDatabase.m_order_new_editing_id=savedInstanceState.getLong("order_new_editing_id");
		g.MyDatabase.m_order_editing_created=savedInstanceState.getBoolean("order_editing_created");
		g.MyDatabase.m_order_editing_modified=savedInstanceState.getBoolean("order_editing_modified");
		m_copy_order_id=savedInstanceState.getLong("copy_order_id");
		m_backup_client_id=savedInstanceState.getString("backup_client_id");
		m_backup_distr_point_id=savedInstanceState.getString("backup_distr_point_id");
		
	   	m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
	    m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
	   	m_list_prices_descr = savedInstanceState.getStringArray("list_prices_descr");
	    m_list_prices_id = savedInstanceState.getStringArray("list_prices_id");
	    m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
	    m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");
		
		//Toast.makeText(this, "!!!BACKUP: "+g.MyDatabase.m_order_editing_id+"/"+g.MyDatabase.m_order_new_editing_id, Toast.LENGTH_LONG).show();
		//Log.d(LOG_TAG, "onCreateValues"+g.MyDatabase.m_order_editing_id+"/"+g.MyDatabase.m_order_new_editing_id);
		//
		//ContentValues cv=new ContentValues();
		//cv.put("messagetext", "onCreateValues"+g.MyDatabase.m_order_editing_id+"/"+g.MyDatabase.m_order_new_editing_id);
		//cv.put("messagetype", "0");
		//cv.put("version", "0");
		//getContentResolver().insert(MTradeContentProvider.MTRADE_LOG_CONTENT_URI, cv);
		if (g.MyDatabase.m_order_editing.bCreatedInSingleton)
		{
			Intent intent=new Intent(); // Лишь бы что-нибудь было
			if (g.MyDatabase.m_order_new_editing_id>0)
			{
				Long id1=g.MyDatabase.m_order_new_editing_id;
				Long id2=g.MyDatabase.m_order_editing_id;
				//TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id);
				OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_new_editing_id, new MyID(), new MyID(), false);
				g.MyDatabase.m_order_new_editing_id=id1;
				g.MyDatabase.m_order_editing_id=id2;
				g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
			} else if (g.MyDatabase.m_order_editing_id>0)
			{
				//TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id);
				OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_editing_id, new MyID(), new MyID(), false);
				g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
			} else
			{
				// Документ был создан, но не записан (не было никаких изменений),
				// после этого все было выгружено из памяти
				if (m_copy_order_id>0)
				{
					//
					OrdersHelpers.PrepareData(intent, this, m_copy_order_id, new MyID(m_backup_client_id), new MyID(m_backup_distr_point_id), true);
				} else
				{
					// Создадим новый
					OrdersHelpers.PrepareData(intent, this, 0, new MyID(m_backup_client_id), new MyID(m_backup_distr_point_id), false);
				}
			}
		}
		return bCreatedInSingleton;
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Не требуется, т.к. до этого вызывается onCreate, где все это уже считалось
		//HandleBundle(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();
		outState.putLong("order_editing_id", g.MyDatabase.m_order_editing_id);
		outState.putLong("order_new_editing_id", g.MyDatabase.m_order_new_editing_id);
		outState.putBoolean("order_editing_created", g.MyDatabase.m_order_editing_created);
		outState.putBoolean("order_editing_modified", g.MyDatabase.m_order_editing_modified);
		outState.putLong("copy_order_id", m_copy_order_id);
		outState.putString("backup_client_id", m_backup_client_id);
		outState.putString("backup_distr_point_id", m_backup_distr_point_id);

		
		//Log.d(LOG_TAG, "onSaveInstanceState");
		//
		//ContentValues cv=new ContentValues();
		//cv.put("messagetext", "onSaveInstanceState"+g.MyDatabase.m_order_editing_id+"/"+g.MyDatabase.m_order_new_editing_id);
		//cv.put("messagetype", "0");
		//cv.put("version", "0");
		//getContentResolver().insert(MTradeContentProvider.MTRADE_LOG_CONTENT_URI, cv);
		outState.putStringArray("list_accounts_descr", m_list_accounts_descr);
		outState.putStringArray("list_accounts_id", m_list_accounts_id);
		outState.putStringArray("list_prices_descr", m_list_prices_descr);
		outState.putStringArray("list_prices_id", m_list_prices_id);
		outState.putStringArray("list_stocks_descr", m_list_stocks_descr);
		outState.putStringArray("list_stocks_id", m_list_stocks_id);
	}
	
	
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    // Из-за того, что при OrientationChange надо это делать перед onCreate
        //
        MySingleton g=MySingleton.getInstance();
        g.checkInitByDataAndSetTheme(this);

	    super.onCreate(savedInstanceState);

	    m_copy_order_id=-1;
	    m_backup_client_id=new MyID().toString();
		m_backup_distr_point_id=new MyID().toString();
	    
		orientationLandscape=(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE);
		
		Object ob1=getLastCustomNonConfigurationInstance();
		if (ob1!=null)
		{
			//Toast.makeText(this, "NOT NULL", Toast.LENGTH_LONG).show();
		}

	    setContentView(R.layout.order_pager);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	    if (g.Common.PHARAON)
	    {
	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    }
	    
	    boolean bCreatedInSingleton=false;
	    boolean bRecreationActivity=false; 
	    if (savedInstanceState!=null&&savedInstanceState.containsKey("order_editing_id"))
	    {
	    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
	    	bCreatedInSingleton=HandleBundle(savedInstanceState);
	    }
	    
	    //if (!bCreatedInSingleton)
	    //{
	    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
	    //}
	    
	    g.MyDatabase.m_orderLinesAdapter=null;
	    
	    SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		m_work_date=DatePreference.getDateFor(sharedPreferences, "work_date");
		
		//
		if (bCreatedInSingleton)
		{
			
		} else
		{
		   	Intent intent=getIntent();
		   	
		   	Bundle bundle=intent.getBundleExtra("extrasBundle");
		    //m_list_stocks_descr = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_descr"));
		    //m_list_stocks_id = (ArrayList<String>)Arrays.asList(bundle.getStringArray("list_stocks_id"));
	
		    //m_list_organizations_descr = bundle.getStringArray("list_organizations_descr");
		    //m_list_organizations_id = bundle.getStringArray("list_organizations_id");
		   	
		   	m_list_accounts_descr = bundle.getStringArray("list_accounts_descr");
		    m_list_accounts_id = bundle.getStringArray("list_accounts_id");
		   	
		   	m_list_prices_descr = bundle.getStringArray("list_prices_descr");
		    m_list_prices_id = bundle.getStringArray("list_prices_id");
		   	
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
		    
	    	// 07.06.2018: окно может быть уничтожено и создано заново
		    if (!bRecreationActivity&&g.MyDatabase.m_order_editing_created)
		    //if (g.MyDatabase.m_order_editing_id==0)
		    {
		    	//
		    	// это новый документ
		    	// контрагент в документ попал автоматически из отбора
		    	if (!g.MyDatabase.m_order_editing.client_id.isEmpty())
		    	{
		    		// установим договор по умолчанию, если он единственный
			        Cursor defaultAgreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
			        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
			        {
			        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
			        	//int price_type_idIndex = defaultAgreementCursor.getColumnIndex("price_type_id");
			        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
			        	if (!defaultAgreementCursor.moveToNext())
			        	{
			        		// Есть единственный договор
		    	        	onAgreementSelected(null, _idAgreement);
			        		
			        	}
			        }
			        defaultAgreementCursor.close();
		    	}
		    }
		    
		    if (intent.hasExtra("copy_order_id"))
		    {
		    	m_copy_order_id=intent.getLongExtra("copy_order_id", -1L);
		    }
		    
		    if (intent.hasExtra("backup_client_id"))
		    {
		    	m_backup_client_id=intent.getStringExtra("backup_client_id");
		    }
			if (intent.hasExtra("backup_distr_point_id"))
			{
				m_backup_distr_point_id=intent.getStringExtra("backup_distr_point_id");
			}

		    
		    if (!bRecreationActivity&&intent.getBooleanExtra("recalc_price", false))
		    {
		    	OrdersHelpers.recalcPrice(this);
		    }
		    
		}
	   	
	    pager = (ViewPager) findViewById(R.id.pagerOrder);
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
	    	    if (g.Common.PHARAON)
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
	    
        m_locationManagerInOrder = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

	    if (g.MyDatabase.m_order_editing.accept_coord==1&&m_locationManagerInOrder!=null)
	    {
	    	// здесь не проверяем состояние, отработана заявка или нет
	    	// координаты принимаются всегда, однако флаг измененности заявки не устанавливаем
	    	// при получении координат

			/*
	        Criteria criteria = new Criteria();
			criteria.setAccuracy( Criteria.ACCURACY_COARSE );
			String provider = m_locationManagerInOrder.getBestProvider( criteria, true );
	    	
	    	//m_locationManagerInOrder.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
			if (provider!=null)
			{
				m_locationManagerInOrder.requestLocationUpdates(provider,  0, 0, locListener);
			}
			*/
			startGPS();
	    }
	    
	  }
	    
	    	
	  @Override
		protected Dialog onCreateDialog(int id) {
		  MySingleton g=MySingleton.getInstance();
		  OrderRecord rec=g.MyDatabase.m_order_editing;
			switch (id) {
				case IDD_SHOW_PAYMENTS_STATS_PDA:
				{
					class SettlementsRecord implements Comparable< SettlementsRecord >
					{
						public String datedoc;
						public String numdoc;
						public int recType; // 0-реализация, 1-оплата
						public double sum_doc;

						@Override
						public int compareTo(SettlementsRecord second) {
							int result=this.datedoc.compareTo(second.datedoc);
							if (result==0)
								result=this.numdoc.compareTo(second.numdoc);
							return result;
						}
					};

					AlertDialog.Builder builder=new AlertDialog.Builder(this);
					builder.setTitle(R.string.title_payment_statistics);

					ArrayList<SettlementsRecord> settlements=new ArrayList<>();

					if (!g.MyDatabase.m_order_editing.client_id.isEmpty()) {
						String client_id = g.MyDatabase.m_order_editing.client_id.toString();

						int index_numdoc;
						int index_datedoc;
						int index_sum_doc;

						String conditionString;
						ArrayList<String> conditionArgs;

						// Заказы
						conditionString = "editing_backup=0"; // чтобы текущий созданный документ в статистику не попал
						conditionArgs = new ArrayList<>();
						conditionString = g.Common.combineConditions(conditionString, conditionArgs, E_ORDER_STATE.getStatisticConditionWhere(), E_ORDER_STATE.getStatisticArgs());
						conditionString = g.Common.combineConditions(conditionString, conditionArgs, "client_id=?", new String[]{client_id});

						Cursor ordersCursor = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"numdoc", "datedoc", "sum_doc"}, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), null);
						index_numdoc=ordersCursor.getColumnIndex("numdoc");
						index_datedoc=ordersCursor.getColumnIndex("datedoc");
						index_sum_doc=ordersCursor.getColumnIndex("sum_doc");
						while (ordersCursor.moveToNext())
						{
							SettlementsRecord ettlementsRecord=new SettlementsRecord();
							ettlementsRecord.numdoc=ordersCursor.getString(index_numdoc);
							ettlementsRecord.datedoc=ordersCursor.getString(index_datedoc);
							ettlementsRecord.recType=0;
							ettlementsRecord.sum_doc=ordersCursor.getDouble(index_sum_doc);
							settlements.add(ettlementsRecord);
						}
						ordersCursor.close();
						// Платежи
						conditionString = "";
						conditionArgs = new ArrayList<>();
						conditionString = g.Common.combineConditions(conditionString, conditionArgs, E_PAYMENT_STATE.getPaymentStatisticConditionWhere(), E_PAYMENT_STATE.getPaymentStatisticArgs());
						conditionString = g.Common.combineConditions(conditionString, conditionArgs, "client_id=?", new String[]{client_id});

						Cursor paymentsCursor = getContentResolver().query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"numdoc", "datedoc", "sum_doc"}, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), null);
						index_numdoc=ordersCursor.getColumnIndex("numdoc");
						index_datedoc=ordersCursor.getColumnIndex("datedoc");
						index_sum_doc=ordersCursor.getColumnIndex("sum_doc");
						while (paymentsCursor.moveToNext())
						{
							SettlementsRecord ettlementsRecord=new SettlementsRecord();
							ettlementsRecord.numdoc=paymentsCursor.getString(index_numdoc);
							ettlementsRecord.datedoc=paymentsCursor.getString(index_datedoc);
							ettlementsRecord.recType=1;
							ettlementsRecord.sum_doc=paymentsCursor.getDouble(index_sum_doc);
							settlements.add(ettlementsRecord);
						}
						paymentsCursor.close();

						Collections.sort(settlements, Collections.reverseOrder());

                        WebView wv = new WebView(this);
                        //wv.loadUrl("http:\\www.google.com");
                        //String summary = "<html><body>You scored <b>192</b> points.</body></html>";
                        //String html_data = "<html><body><table><tr><td>1</td><td>2</td><td>3</td></tr><tr><td>1</td><td>2</td><td>3</td></tr></table></body></html>";

                        StringBuilder sb=new StringBuilder();

                        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
                        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" >\n");
                        sb.append("<head>\n");
                        sb.append("  <title>Статистика платежей</title>\n");
                        //|  <meta http-equiv=""Content-Type"" content=""text/html; charset=Windows-1251"" />
                        sb.append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
                        sb.append("  <style type=\"text/css\">\n");
                        sb.append("    .Header{font-family:Arial; font-size:12pt; color:Red; font-weight: bold;}\n");
                        sb.append("    .TableHeader{font-family:Arial; font-weight: bold; text-align: center; color:Blue; background-color: Gold; border-color:Black;}\n");
                        sb.append("    .TableRow1Left{text-align: left; font-weight: bold; background-color: SkyBlue; border-color:Black;}\n");
                        sb.append("    .TableRow1Right{text-align: right; background-color: SkyBlue; border-color:Black;}\n");
                        sb.append("    .TableRow2Left{text-align: left; border-color:Black;}\n");
                        sb.append("    .TableRow2Right{text-align: right; border-color:Black;}\n");
                        sb.append("    .TableEnd{}\n");
                        sb.append("  </style>\n");
                        sb.append("</head>\n");
                        sb.append("<body style=\"font-family:Arial; text-align: center; margin: 0px;\">\n");
                        sb.append("  <span class=\"Header\">Статистика платежей  (не является отчетом о дебиторке, а только показывает суммы документов, созданных в этом КПК)</span><br />\n");
                        //"  клиент "+Клиент+" "+Договор+" "+ПредставлениеПериода(_Дата1, КонецДня(_Дата2))+
                        sb.append("  <table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n");
                        sb.append("    <tr>\n");
                        sb.append("      <td class=\"TableHeader\">Документ</td>\n");
                        sb.append("      <td class=\"TableHeader\">Реализация</td>\n");
                        sb.append("      <td class=\"TableHeader\">Платеж</td>\n");
                        //"      <td class=\"TableHeader\">Менеджер</td>\n"+
                        sb.append("    </tr>\n");

					/*
					html_data=html_data+
						"    <tr>\n"+
						"      <td class=\"TableRow2Left\"><b>Конечный остаток:</b></td>\n"+
						"      <td class=\"TableRow2Right\"><b>"+"111111"+"</b></td>\n"+
						"      <td class=\"TableRow2Right\"><b>"+"2222222"+"</b></td>\n"+
						"      <td class=\"TableRow2Left\"><b>"+""+"</b></td>\n"+
						"    </tr>\n";
					*/

                        for (SettlementsRecord settlement:settlements)
                        {
                            sb.append("    <tr>\n");
                            if (settlement.recType==0) {
                                sb.append(String.format("      <td class=\"TableRow2Left\"><b>Заказ %s от %s</b></td>\n", settlement.numdoc, Common.dateStringAsText(settlement.datedoc)));
                                sb.append(String.format("      <td class=\"TableRow2Right\"><b>%.2f</b></td>\n", settlement.sum_doc));
                                sb.append("      <td class=\"TableRow2Right\"><b></b></td>\n");
                            } else
                            {
                                sb.append(String.format("      <td class=\"TableRow2Left\"><b>Платеж %s от %s</b></td>\n", settlement.numdoc, Common.dateStringAsText(settlement.datedoc)));
                                sb.append("      <td class=\"TableRow2Right\"><b></b></td>\n");
                                sb.append(String.format("      <td class=\"TableRow2Right\"><b>%.2f</b></td>\n", settlement.sum_doc));
                            }
                            //sb.append("      <td class=\"TableRow2Left\"><b>"+""+"</b></td>\n");
                            sb.append("    </tr>\n");
                        }

                        sb.append("</table>\n");
                        sb.append("</body>\n");
                        sb.append("</html>\n");

                        wv.loadData(sb.toString(), "text/html; charset=utf-8", "utf-8");
                        /*
                        wv.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                view.loadUrl(url);
                                return true;
                            }
                        });
                        */

                        builder.setView(wv);

					} else
                    {
                        builder.setMessage("Клиент не выбран");
                    }

					//AlertDialog.Builder builder=new AlertDialog.Builder(this);
					//builder.setText();



					/*
					TextView msg = new TextView(this);
					msg.setText(Html.fromHtml("<html><u>Message</u></html>"));

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Title");
					builder.setView(msg);
					builder.setCancelable(false);
					*/

					builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});

					return builder.create();
				}
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
					builder.setTitle(getResources().getString(R.string.order_modiffied));
					builder.setMessage(getResources().getString(R.string.save_it_before_closing));
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							MySingleton g=MySingleton.getInstance();
							StringBuffer reason=new StringBuffer();
							if (orderCanBeSaved(reason)||g.MyDatabase.m_order_editing.dont_need_send==1)
							{
								Intent intent=new Intent();
								setResult(ORDER_RESULT_OK, intent);
								m_locationManagerInOrder.removeUpdates(locListener);
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
							setResult(ORDER_RESULT_CANCEL, intent);
							m_locationManagerInOrder.removeUpdates(locListener);
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
							g.MyDatabase.m_order_editing.lines.clear();
							g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
							setModified(true);
							recalcSumWeight();
							redrawSumWeight();
							TextDatabase.DeleteOrderLines(getContentResolver(), g.MyDatabase.m_order_new_editing_id);
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
					builder.setTitle(getResources().getString(R.string.order_cant_be_saved));
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
			OrderRecord rec=g.MyDatabase.m_order_editing;
			
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
			case IDD_SHIPPING_BEGIN_TIME:
			{
				int hour=0;
				int minute=0;
				if (rec.shipping_begin_time.length()==4)
				{
					hour=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
					minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
					((TimePickerDialog)dialog).updateTime(hour, minute);
				}
				break;
			}
			case IDD_SHIPPING_END_TIME:
			{
				int hour=0;
				int minute=0;
				if (rec.shipping_end_time.length()==4)
				{
					hour=Integer.parseInt(rec.shipping_end_time.substring(0,2));
					minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
					((TimePickerDialog)dialog).updateTime(hour, minute);
				}
				break;
			}
			case IDD_CANT_SAVE:
				//m_reason_cant_save=bundle.getString("reason");
				break;
			}
		}
	  
	  
		/*
	    private TimePickerDialog.OnTimeSetListener timePickerListenerBeginTime = new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
				
				OrderRecord rec=g.MyDatabase.m_order_editing;
			    
				int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
				if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
				{
					
					int viewId=pager.getId();
					int position=0;
					Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
					View view0=frag0.getView();
					
					StringBuilder sb=new StringBuilder();
					EditText etTime = (EditText) view0.findViewById(R.id.etShippingBeginTime);
					rec.shipping_begin_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
					sb.append(rec.shipping_begin_time.substring(0,2)+":"+rec.shipping_begin_time.substring(2,4));
					etTime.setText(sb.toString());
					
					setModified();
				}
			}
		};
		
	    private TimePickerDialog.OnTimeSetListener timePickerListenerEndTime = new TimePickerDialog.OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
				
				OrderRecord rec=g.MyDatabase.m_order_editing;
			    
				int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
				int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
				if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
				{
					
					int viewId=pager.getId();
					int position=0;
					Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
					View view0=frag0.getView();
					
					StringBuilder sb=new StringBuilder();
					EditText etTime = (EditText) view0.findViewById(R.id.etShippingEndTime);
					rec.shipping_end_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
					sb.append(rec.shipping_end_time.substring(0,2)+":"+rec.shipping_end_time.substring(2,4));
					etTime.setText(sb.toString());
					
					setModified();
				}
			}
		};
		*/
		
	  
		/*
		private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			
		    OrderRecord rec=g.MyDatabase.m_order_editing;
			
			int day=Integer.parseInt(rec.datedoc.substring(6,8));
			int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
			int year=Integer.parseInt(rec.datedoc.substring(0,4));
			
			if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
			{
				rec.datedoc=android.text.format.DateFormat.format("yyyyMMddkkmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay)).toString();
		
				int viewId=pager.getId();
				int position=0;
				Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
				View view0=frag0.getView();
				EditText etDate = (EditText) view0.findViewById(R.id.etDate);
				StringBuilder sb=new StringBuilder();
				sb.append(rec.datedoc.substring(6,8)+"."+rec.datedoc.substring(4,6)+"."+rec.datedoc.substring(0,4));
				etDate.setText(sb.toString());
				
				setModified();
			}
			
		}
		};
		*/

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	MySingleton g=MySingleton.getInstance();
	switch (item.getItemId())
	{
		case R.id.action_show_payments_stats_pda: {
			m_payments_stats_pda="";
			removeDialog(IDD_SHOW_PAYMENTS_STATS_PDA);
			showDialog(IDD_SHOW_PAYMENTS_STATS_PDA);
			break;
		}
		case R.id.action_save:
		{
			StringBuffer reason=new StringBuffer();
			if (orderCanBeSaved(reason)||g.MyDatabase.m_order_editing.dont_need_send==1)
			{
				Intent intent=new Intent();
				setResult(ORDER_RESULT_OK, intent);
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
			setResult(ORDER_RESULT_CANCEL, intent);
			finish();
			break;
		}
		case R.id.action_settings:
			Intent intent=new Intent(OrderActivity.this, PrefOrderActivity.class);
			startActivity(intent);
			break;
	}
  
  return super.onOptionsItemSelected(item);
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
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);
	MySingleton g=MySingleton.getInstance();
	
	// https://code.google.com/p/android-icon-context-menu/
	
	ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo; 
			
	//menu.setHeaderTitle(getString(R.string.menu_context_title));
	MenuInflater inflater = getMenuInflater();
	switch (v.getId()) {
	case R.id.buttonOrderPrint:
	{
		if (g.Common.PHARAON)
		{
			inflater.inflate(R.menu.order_print, menu);
		}
	}
	break;
	case R.id.listViewOrderLines:
	{
		if (g.Common.PHARAON)
		{
			//int pos_group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int pos_child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			//Toast.makeText(OrderActivity.this, String.valueOf(pos_group)+"/"+String.valueOf(pos_child), Toast.LENGTH_LONG).show();
			
			if (pos_child>=0)
			{
				if (pos_child>=0)
				{
					inflater.inflate(R.menu.order_lines_list, menu);
					
					MenuItem itemChangePrice = menu.findItem(R.id.action_change_price);
					MenuItem itemMoveDown = menu.findItem(R.id.action_move_down);
					MenuItem itemMoveUp = menu.findItem(R.id.action_move_up);
					
					itemChangePrice.setVisible(false);
					itemMoveDown.setVisible(false);
					itemMoveUp.setVisible(false);
				}
			}
			
		} else
		{
			//ExpandableListView elvOrderLines=(ExpandableListView)v;
			inflater.inflate(R.menu.order_lines_list, menu);
			
			MenuItem itemChangePrice = menu.findItem(R.id.action_change_price);
			MenuItem itemMoveDown = menu.findItem(R.id.action_move_down);
			MenuItem itemMoveUp = menu.findItem(R.id.action_move_up);
			
			int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			if (pos>=0&&pos<g.MyDatabase.m_order_editing.lines.size())
			{
				itemChangePrice.setVisible((g.MyDatabase.m_order_editing.lines.get(pos).stuff_nomenclature_flags&1)!=0);
				itemMoveDown.setVisible(pos<g.MyDatabase.m_order_editing.lines.size()-1);
				itemMoveUp.setVisible(pos>0);
			}
		}
	}
	break;
	}

}

@Override
public boolean onContextItemSelected(MenuItem item) {
	MySingleton g=MySingleton.getInstance();
	switch (item.getItemId())
	{
	case R.id.action_delete_line:
	{
		if (g.Common.PHARAON)
		{
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
			int pos_group=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int pos_child=ExpandableListView.getPackedPositionChild(info.packedPosition);
			
    		//g.MyDatabase.m_linesAdapter = new OrderExpPhListAdapter(getActivity().getApplicationContext());
			OrderExpPhListAdapter phListAdapter=(OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter;
			OrderLineRecord data=phListAdapter.mGroups.get(pos_group).lines.get(pos_child);

			g.MyDatabase.m_order_editing.lines.remove(data);
		} else
		{
			//AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
			int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
			if (g.Common.NEW_BACKUP_FORMAT)
			{
				TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing.lines.get(pos).lineno);
			}
			g.MyDatabase.m_order_editing.lines.remove(pos);
		}
		g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
		setModified(true);
		recalcSumWeight();
		redrawSumWeight();
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
		OrderLineRecord temp=g.MyDatabase.m_order_editing.lines.get(pos);
		temp=g.MyDatabase.m_order_editing.lines.set(pos+1, temp);
		g.MyDatabase.m_order_editing.lines.set(pos, temp);
		g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
		setModified(true);
		return true;
	}
	case R.id.action_move_up:
	{
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
		OrderLineRecord temp=g.MyDatabase.m_order_editing.lines.get(pos);
		temp=g.MyDatabase.m_order_editing.lines.set(pos-1, temp);
		g.MyDatabase.m_order_editing.lines.set(pos, temp);
		g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
		setModified(true);
		return true;
	}
	}
	return super.onContextItemSelected(item);
}

@Override
// возвращает true в случае, если клиент изменился
public boolean onClientSelected(View view0, long _id)
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
	    	int priceType_Index = clientsCursor.getColumnIndex("priceType");
	    	String clientId = clientsCursor.getString(idIndex);
	    	if (g.MyDatabase.m_order_editing.client_id.toString().equals(clientId))
	    	{
	    		return false;
	    	}
	    	String clientDescr = clientsCursor.getString(descrIndex);
	    	String clientAddress = clientsCursor.getString(addressIndex);
	    	String curatorId = clientsCursor.getString(curator_idIndex);
	    	int flags = clientsCursor.getInt(flags_Index); 
	    	
	    	g.MyDatabase.m_order_editing.client_id=new MyID(clientId);
	    	g.MyDatabase.m_order_editing.stuff_client_name=clientDescr;
	    	g.MyDatabase.m_order_editing.stuff_client_address=clientAddress;
	    	g.MyDatabase.m_order_editing.stuff_select_account=(flags&2)!=0;
	    	// очистим торговую точку
	    	g.MyDatabase.m_order_editing.distr_point_id=new MyID();
	    	g.MyDatabase.m_order_editing.stuff_distr_point_name="";
			// Долг контрагента
		    Cursor cursorDebt=getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
		    if (cursorDebt.moveToNext())
		    {
		    	int indexDebt = cursorDebt.getColumnIndex("saldo");
		    	int indexDebtPast = cursorDebt.getColumnIndex("saldo_past");
		    	int indexDebtPast30 = cursorDebt.getColumnIndex("saldo_past30");
				g.MyDatabase.m_order_editing.stuff_debt=cursorDebt.getDouble(indexDebt); 
				g.MyDatabase.m_order_editing.stuff_debt_past=cursorDebt.getDouble(indexDebtPast);
				g.MyDatabase.m_order_editing.stuff_debt_past30=cursorDebt.getDouble(indexDebtPast30);
		    } else
		    {
		    	g.MyDatabase.m_order_editing.stuff_debt=0.0;
		    	g.MyDatabase.m_order_editing.stuff_debt_past=0.0;
		    	g.MyDatabase.m_order_editing.stuff_debt_past30=0.0;
		    }
		    // Куратор
		    Cursor curatorsCursor=getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{curatorId}, null);
		    String curatorDescr;
		    if (curatorsCursor!=null &&curatorsCursor.moveToNext())
		    {
		    	int curator_descrIndex = curatorsCursor.getColumnIndex("descr");
		    	curatorDescr = curatorsCursor.getString(curator_descrIndex);
		    	g.MyDatabase.m_order_editing.curator_id=new MyID(curatorId);
		    	g.MyDatabase.m_order_editing.stuff_curator_name=curatorDescr;
		    } else
		    {
		    	g.MyDatabase.m_order_editing.curator_id=new MyID();
		    	g.MyDatabase.m_order_editing.stuff_curator_name="";
		    	curatorDescr = getResources().getString(R.string.curator_not_set);
		    	g.MyDatabase.m_order_editing.stuff_debt=0.0;
		    	g.MyDatabase.m_order_editing.stuff_debt_past=0.0;
		    	g.MyDatabase.m_order_editing.stuff_debt_past30=0.0;
		    }
		    
		    if (g.Common.NEW_BACKUP_FORMAT)
		    {
		    	TextDatabase.SetOrderSQLFields(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"client_id", "distr_point_id", "curator_id"});
		    }
		    
	    	if (view0!=null)
	    	{
	    		// Клиент
	    		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
	    		etClient.setText(clientDescr);
				// и его адрес
				TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewOrdersClientAddress);
				tvClientAddress.setText(clientAddress);
	    		// Тип учета
	    		TextView tvAccounts=(TextView)view0.findViewById(R.id.tvAccount);
	    		Spinner spinnerAccounts=(Spinner)view0.findViewById(R.id.spinnerAccount);
	    		if (g.MyDatabase.m_order_editing.stuff_select_account)
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
				etDebt.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_debt));
				EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextDebtPast);
				etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_debt_past));
				
				TextView textViewDebtPast30 = (TextView)view0.findViewById(R.id.textViewDebtPast30);
				EditText etDebtPast30 = (EditText) view0.findViewById(R.id.editTextDebtPast30);
				if (g.Common.PRODLIDER||g.Common.TANDEM)
				{
					etDebtPast30.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_debt_past30));
				} else
				{
					textViewDebtPast30.setVisibility(View.GONE);
					etDebtPast30.setVisibility(View.GONE);
				}
				
				// Торговая точка
				EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
				etDistrPoEditText.setText(g.MyDatabase.m_order_editing.stuff_distr_point_name);

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

			if (g.Common.MEGA||g.Common.PHARAON)
			{
			    if (onPriceTypeSelected(view0, new MyID(clientsCursor.getString(priceType_Index))))
			    {
			    	OrdersHelpers.recalcPrice(this);
			    	redrawSumWeight();
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
	if (g.MyDatabase.m_order_editing.client_id.isEmpty())
		return false;
	// Клиент был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
		etClient.setText(getResources().getString(R.string.client_not_set));
		TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewOrdersClientAddress);
		tvClientAddress.setText("");
		EditText etCurator=(EditText)view0.findViewById(R.id.editTextCurator);
		etCurator.setText(getResources().getString(R.string.curator_not_set));
		EditText etDistrPoEditText=(EditText)view0.findViewById(R.id.etTradePoint);
		etDistrPoEditText.setText(getResources().getString(R.string.trade_point_not_set));
		
		// Остатки оборудования у клиента
		EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipment");
		if (fragment1!=null)
			fragment1.myRestartLoader();
		EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)getSupportFragmentManager().findFragmentByTag("fragmentEquipmentTare");
		if (fragment2!=null)
		{
			fragment2.myRestartLoader();
		}
		
	}
	g.MyDatabase.m_order_editing.client_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_client_name="";
	g.MyDatabase.m_order_editing.stuff_client_address="";
	g.MyDatabase.m_order_editing.curator_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_curator_name="";
	g.MyDatabase.m_order_editing.distr_point_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_distr_point_name="";
	
	return true;
}

/*
private void onNewDocumentWithClient(MyID clientId)
{
	if (!id.isEmpty())
	{
	    Cursor clientsCursor=getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{clientId.toString()}, null);
	    if (clientsCursor!=null &&clientsCursor.moveToNext())
	    {
	    	int descrIndex = clientsCursor.getColumnIndex("descr");
	    	String clientDescr = clientsCursor.getString(descrIndex);
	    	if (g.MyDatabase.m_order_editing.client_id.equals(clientId))
	    	{
	    		return false;
	    	}
	    	if (view0!=null)
	    	{
	    		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
	    		etClient.setText(clientDescr);
	    	}
	    	g.MyDatabase.m_order_editing.client_id=new MyID(clientId);
	    	g.MyDatabase.m_order_editing.stuff_client_name=clientDescr;
	    	return true;
	    }
	}
	// Клиент не был указан и не изменился
	if (g.MyDatabase.m_order_editing.client_id.isEmpty())
		return false;
	// Клиент был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
		EditText etClient=(EditText)view0.findViewById(R.id.etClient);
		etClient.setText(getResources().getString(R.string.client_not_set));
	}
	g.MyDatabase.m_order_editing.client_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_client_name="";
	
	return true;
}
*/
@Override
//возвращает true в случае, если соглашение изменилось
public boolean onAgreement30Selected(View view0, long _id)
{
	return OrdersHelpers.onAgreement30Selected_RestrictedVer(this, view0, _id, m_list_prices_id, m_list_prices_descr);
}


@Override
//возвращает true в случае, если договор изменился
// не менять на функцию из OrdersHelpers, т.к. там нет функционала по перерисовке
public boolean onAgreementSelected(View view0, long _id)
{
	// 11.06.2018
	return OrdersHelpers.onAgreementSelected_RestrictedVer(this, view0, _id, m_list_prices_id, m_list_prices_descr);
	/*
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
	    	String priceTypeId = agreementsCursor.getString(priceType_idIndex);
	    	if (g.MyDatabase.m_order_editing.agreement_id.toString().equals(agreementId))
	    	{
	    		return false;
	    	}
	    	if (g.Common.PRODLIDER)
	    	{
	    		// Тип цены сейчас в торговой точке, приоритет выше
	    	    Cursor distrPointsCursor=getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"_id", "price_type_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.distr_point_id.toString()}, null);
	    	    if (distrPointsCursor.moveToFirst()) 
	    	    {
	    	    	int price_type_idIndex=distrPointsCursor.getColumnIndex("price_type_id");
	    	    	MyID priceTypeIdByTradePoint=new MyID(distrPointsCursor.getString(price_type_idIndex));
	    	    	if (!priceTypeIdByTradePoint.isEmpty())
	    	    	{
	    	    		priceTypeId=priceTypeIdByTradePoint.toString();
	    	    	}
	    	    }
	    	    distrPointsCursor.close();	    	    
	    	}
	    	if (view0!=null)
	    	{
		    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
		    	etAgreement.setText(agreementDescr);
	    	}
	    	g.MyDatabase.m_order_editing.agreement_id=new MyID(agreementId);
	    	g.MyDatabase.m_order_editing.stuff_agreement_name=agreementDescr;
	    	if (g.Common.TITAN||g.Common.INFOSTART)
	    	{
	    		// Скидка
		    	int saleIdIndex = agreementsCursor.getColumnIndex("sale_id");
		    	String sale_id = agreementsCursor.getString(saleIdIndex);
			    Cursor discountCursor=getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{sale_id}, null);
			    if (discountCursor.moveToNext())
			    {
			    	int discountDescrIndex = discountCursor.getColumnIndex("descr");
			    	int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
			    	String discountDescr = discountCursor.getString(discountDescrIndex);
			    	g.MyDatabase.m_order_editing.simple_discount_id=new MyID(sale_id);
			    	g.MyDatabase.m_order_editing.stuff_discount=discountDescr;
			    	g.MyDatabase.m_order_editing.stuff_discount_procent=discountCursor.getDouble(discountPriceProcentIndex);
			    } else
			    {
			    	g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
			    	g.MyDatabase.m_order_editing.stuff_discount=getResources().getString(R.string.without_discount);
			    	g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;
			    }
		    	if (view0!=null)
		    	{
			    	EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
			    	etDiscount.setText(g.MyDatabase.m_order_editing.stuff_discount);
		    	}
		    	discountCursor.close();
	    	} else
	    	{
	    		g.MyDatabase.m_order_editing.stuff_discount=getResources().getString(R.string.without_discount);
	    	}
	    	// и заполняем организацию
		    Cursor cursorOrganizations=getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId}, null);
		    if (cursorOrganizations!=null &&cursorOrganizations.moveToNext())
		    {
		    	int descrIndexOrganization = cursorOrganizations.getColumnIndex("descr");
		    	String descrOrganization = cursorOrganizations.getString(descrIndexOrganization);
				g.MyDatabase.m_order_editing.organization_id=new MyID(organizationId);
				g.MyDatabase.m_order_editing.stuff_organization_name=descrOrganization;
		    	if (view0!=null)
		    	{
			    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
			    	tvOrganization.setText(descrOrganization);
		    	}
		    } else
		    {
		    	if (view0!=null)
		    	{
			    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
			    	tvOrganization.setText(getResources().getString(R.string.organization_not_set));
		    	}
		    	g.MyDatabase.m_order_editing.organization_id.m_id="";
		    	g.MyDatabase.m_order_editing.stuff_organization_name=getResources().getString(R.string.item_not_set);
		    }
		    if (onPriceTypeSelected(view0, new MyID(priceTypeId)))
		    {
		    	OrdersHelpers.recalcPrice(this);
		    	redrawSumWeight();
		    }
		    // Долг контрагента по договору
		    if (g.Common.TITAN||g.Common.INFOSTART)
		    {
		    	double agreement_debt=0;
		    	double agreement_debt_past=0;
			    Cursor cursorAgreementDebt=getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
			    int saldoIndex=cursorAgreementDebt.getColumnIndex("saldo");
			    int saldoPastIndex=cursorAgreementDebt.getColumnIndex("saldo_past");
			    while (cursorAgreementDebt.moveToNext())
			    {
			    	agreement_debt=agreement_debt+cursorAgreementDebt.getDouble(saldoIndex);
			    	agreement_debt_past=agreement_debt_past+cursorAgreementDebt.getDouble(saldoPastIndex);
			    }
			    cursorAgreementDebt.close();
		    	g.MyDatabase.m_order_editing.stuff_agreement_debt=agreement_debt;
		    	g.MyDatabase.m_order_editing.stuff_agreement_debt_past=agreement_debt_past;
		    	
		    	if (view0!=null)
		    	{
					EditText etDebt = (EditText) view0.findViewById(R.id.editTextAgreementDebt);
					etDebt.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt));
					EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextAgreementDebtPast);
					etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt_past));
		    	}
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
		    
		    if (g.Common.NEW_BACKUP_FORMAT)
		    {
		    	TextDatabase.SetOrderSQLFields(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
		    }
				
	    	return true;
	    }
	}
	// Договор не был указан и не изменился
	if (g.MyDatabase.m_order_editing.agreement_id.isEmpty())
		return false;
	// Договор был указан, а сейчас не найден - очищаем все
	if (view0!=null)
	{
    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
    	EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
    	etAgreement.setText(getResources().getString(R.string.agreement_not_set));
    	tvOrganization.setText(getResources().getString(R.string.organization_not_set));
    	etDiscount.setText(getResources().getString(R.string.without_discount));
	}    	
	g.MyDatabase.m_order_editing.agreement_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_agreement_name="";
	g.MyDatabase.m_order_editing.organization_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_organization_name="";
	g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
	g.MyDatabase.m_order_editing.stuff_discount=getResources().getString(R.string.without_discount);
	g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;
	
    if (g.Common.NEW_BACKUP_FORMAT)
    {
    	TextDatabase.SetOrderSQLFields(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
    }
	return true;
	*/
}

public boolean onPriceTypeSelected(View view0, MyID price_type_id)
{
	// 11.06.2018
	return OrdersHelpers.onPriceTypeSelected_RestrictedVer(this, view0, price_type_id, m_list_prices_id, m_list_prices_descr); 
	/*
	MySingleton g=MySingleton.getInstance();
    // Проверяем, изменился ли тип цены
    if (!g.MyDatabase.m_order_editing.price_type_id.equals(price_type_id))
    {
    	g.MyDatabase.m_order_editing.price_type_id=price_type_id;
    	if (view0!=null)
    	{
			Spinner spinnerPriceType=(Spinner)view0.findViewById(R.id.spinnerPriceType);
			EditText etPriceType=(EditText)view0.findViewById(R.id.etPriceType);
	        int i;
	        for (i=0; i<m_list_prices_id.length; i++)
	        {
	        	if (m_list_prices_id[i].equals(g.MyDatabase.m_order_editing.price_type_id.toString()))
	        	{
	        		if (!Constants.ENABLE_PRICE_TYPE_SELECT)
	        		{
	        			etPriceType.setText(m_list_prices_descr[i]);
	        		} else
	        		{
	        			spinnerPriceType.setSelection(i);
	        		}
	        	}
	        }
    	}
    	
	    if (g.Common.NEW_BACKUP_FORMAT)
	    {
	    	TextDatabase.SetOrderSQLFields(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"price_type_id"});
	    }
    	
    	return true;
    }
	return false;
	*/
}
	  
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	
    if (data == null) 
    	return;
    
    /*
	switch (requestCode&0xffff)
	{
	case NT_FROM_ORDER_REQUEST:
	{
		int viewId=pager.getId();
		int position=0;
		Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
		View view0=frag0.getView();
	    long _id=data.getLongExtra("id", 0);
		if (onClientSelected(view0, _id))
		{
			setModified();
			boolean bWrongAgreement=true;
			// Проверим, принадлежит ли договор текущему контрагенту 
	        Cursor agreementsCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
	        if (agreementsCursor!=null &&agreementsCursor.moveToNext())
	        {
	        	int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
	        	String owner_id = agreementsCursor.getString(owner_idIndex);
	        	if (g.MyDatabase.m_order_editing.client_id.toString().equals(owner_id))
	        		bWrongAgreement=false;
	        }
	        if (bWrongAgreement)
	        {
        		// Не принадлежит, установим договор по умолчанию, если он единственный
    	        Cursor defaultAgreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
    	        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
    	        {
    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
    	        	if (!defaultAgreementCursor.moveToNext())
    	        	{
    	        		// Есть единственный договор
	    	        	onAgreementSelected(view0, _idAgreement);
    	        	} else
    	        	{
    	        		// Есть несколько договоров
    	        		onAgreementSelected(view0, 0);
    	        	}
    	        } else
    	        {
    	        	// Договора нет
    	        	onAgreementSelected(view0, 0);
    	        }
	        }
		}
		break;
	}
	case SELECT_AGREEMENT_FROM_ORDER_REQUEST:
	{
		int viewId=pager.getId();
		int position=0;
		Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
		View view0=frag0.getView();
		EditText et=(EditText)view0.findViewById(R.id.etAgreement);
		TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
		
	    long _id=data.getLongExtra("id", 0);
	    if (onAgreementSelected(view0, _id))
	    {
	    	setModified();
	    }
		break;
	}
	case SELECT_TRADE_POINT_FROM_ORDER_REQUEST:
	{
		int viewId=pager.getId();
		int position=0;
		Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
		View view0=frag0.getView();
		EditText et=(EditText)view0.findViewById(R.id.etTradePoint);
		
		setModified();
		
	    long _id=data.getLongExtra("id", 0);
	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, _id);
	    Cursor cursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "address"}, null, null, null);
	    if (cursor!=null &&cursor.moveToNext())
	    {
	    	int descrIndex = cursor.getColumnIndex("descr");
	    	int idIndex = cursor.getColumnIndex("id");
	    	//int addressIndex = cursor.getColumnIndex("address");
	    	String descr = cursor.getString(descrIndex);
	    	String tradePointId = cursor.getString(idIndex);
	    	//String address = cursor.getString(addressIndex);
	    	et.setText(descr);
			g.MyDatabase.m_order_editing.distr_point_id.m_id=tradePointId;
			g.MyDatabase.m_order_editing.stuff_distr_point_name=descr;
	    } else
	    {
	    	et.setText(getResources().getString(R.string.trade_point_not_set));
	    	g.MyDatabase.m_order_editing.distr_point_id.m_id="";
	    	g.MyDatabase.m_order_editing.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
	    }
		break;
	}
	case OPEN_NOMENCLATURE_FROM_ORDER_REQUEST:
	{
		if (resultCode==NomenclatureActivity.NOMENCLATURE_RESULT_ORDER_CHANGED)
		{
			g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
			setModified();
			recalcSumWeight();
			redrawSumWeight();
		}
		break;
	}
	case QUANTITY_REQUEST:
	{
		// это обработчик при изменении количества, при вводе количества - в списке номенклатуры
  		if (resultCode==QuantityActivity.QUANTITY_RESULT_OK)
  		{
      		if (data!=null)
      		{
			    long _id=data.getLongExtra("_id", 0);
			    MyID nomenclature_id=new MyID(data.getStringExtra("id"));
			    double quantity=data.getDoubleExtra("quantity", 0.0);
			    double k=data.getDoubleExtra("k", 1.0);
			    double price=data.getDoubleExtra("price", 0.0);
			    double price_k=data.getDoubleExtra("price_k", 1.0);
			    String ed=data.getStringExtra("ed");
      			
  	    	    //long id=data.getLongExtra("id", 0);
      			OrderLineRecord line;
      			if (m_order_editing_line_num<g.MyDatabase.m_order_editing.lines.size())
      			{
      				line=g.MyDatabase.m_order_editing.lines.get(m_order_editing_line_num);
      				if (!line.nomenclature_id.equals(nomenclature_id))
      				{
      					line=new OrderLineRecord();
      				}
      			} else
      			{
      				line=new OrderLineRecord();      				
      			}
      			Cursor nomenclatureCursor;
      			if (_id==0)
      			{
      				nomenclatureCursor=getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{nomenclature_id.toString()}, null);
      			} else
      			{
				    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
					nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"descr"}, null, null, null);
      			}
				if (nomenclatureCursor.moveToFirst())
				{
					// nomenclature_id не меняем, он правильный
			    	int descrIndex = nomenclatureCursor.getColumnIndex("descr");
					line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
					line.quantity=quantity;
					line.quantity_requested=quantity;
					line.discount=0.0;
					line.k=k;
					line.ed=ed;
					if (price_k>-0.0001&&price_k<0.0001)
					{
						price_k=1.0;
					}
					if (price_k-k>-0.0001&&price_k-k<0.0001)
					{
						line.price=price;
					} else
					{
						line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
					}
					line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;						
					g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
					setModified();
					recalcSumWeight();
					redrawSumWeight();
					if (g.MyDatabase.m_order_editing.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
					{
						ContentValues cv=new ContentValues();
						cv.put("client_id", ""); // в данном случае это не важно
						cv.put("quantity", quantity);
						cv.put("quantity_requested", quantity);
						cv.put("k", k);
						cv.put("ed", ed);
						cv.put("price", price);
						
						int cnt=getContentResolver().update(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cv, "order_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_order_editing_id), nomenclature_id.toString()});
					}
				}
      		}
  		}
		break;
	}
	}*/
}



@Override
protected void onDestroy() {
	m_locationManagerInOrder.removeUpdates(locListener);
	super.onDestroy();
}

private void onCloseActivity()
{
	MySingleton g=MySingleton.getInstance();
	if (g.MyDatabase.m_order_editing_modified)
	{
		showDialog(IDD_CLOSE_QUERY);
	} else
	{
		Intent intent=new Intent();
		setResult(ORDER_RESULT_CANCEL, intent);
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
	// Это не вызывается, а просто создается новый объект (onCreate)

    super.onConfigurationChanged(newConfig);
}

@Override
public void someEvent(String s) {
	MySingleton g=MySingleton.getInstance();
	if (s.equals("coord"))
	{
		if (g.MyDatabase.m_order_editing.accept_coord==1)
		{
			//m_locationManagerInOrder.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
			startGPS();
		} else
		{
			m_locationManagerInOrder.removeUpdates(locListener);
		}
	} else
	if (s.equals("close"))
	{
		onCloseActivity();
	} else
	if (s.equals("ok"))
	{
		StringBuffer reason=new StringBuffer();
		if (orderCanBeSaved(reason)||g.MyDatabase.m_order_editing.dont_need_send==1)
		{
			Intent intent=new Intent();
			setResult(ORDER_RESULT_OK, intent);
			// 16.01.2013
			m_locationManagerInOrder.removeUpdates(locListener);
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
	} else
	if (s.equals("printList")||s.equals("printKO")||s.equals("printBoth"))
	{
		StringBuffer reason=new StringBuffer();
		// При печати флаг снимем
		if (orderCanBeSaved(reason))
		{
			if (g.MyDatabase.m_order_editing.dont_need_send==1)
			{
				g.MyDatabase.m_order_editing.dont_need_send=0;
				setModified(true);
			}
			if (g.MyDatabase.m_order_editing_modified)
			{
				setModified(false);
				g.MyDatabase.m_order_editing.versionPDA++;
				// Перед записью считаем сумму документа
				g.MyDatabase.m_order_editing.sumDoc=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
				g.MyDatabase.m_order_editing.weightDoc=TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
				if (g.MyDatabase.m_order_editing.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
				{
					g.MyDatabase.m_order_editing.state=E_ORDER_STATE.E_ORDER_STATE_CREATED;
				}
				TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id, 0);
				if (g.Common.NEW_BACKUP_FORMAT)
				{
					Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_new_editing_id);
					getContentResolver().delete(singleUri, "editing_backup<>0", null);
				}
				// Резервное сохрание заявки в текстовый файл, либо удаление этой копии
				if (!g.Common.NEW_BACKUP_FORMAT&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
				    // получаем путь к SD
				    File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
				    // создаем каталог
		            if (!sdPath.exists())
		            {
		            	sdPath.mkdirs();
		            }
				    //
				    File sdFile = new File(sdPath, g.MyDatabase.m_order_editing.uid.toString()+".txt");
				    if (E_ORDER_STATE.getCanBeRestoredFromTextFile(g.MyDatabase.m_order_editing.state))
				    {
					    try {
							//BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
					    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sdFile), "cp1251"));
						    bw.write("0\r\n"); // obsolette m_orders_version
						    bw.write(String.valueOf(g.Common.m_mtrade_version));
						    bw.write("\r\n");
							TextDatabase.SaveOrderToText(bw, g.MyDatabase.m_order_editing, false, true);
							bw.write("@@@"); // начиная с версии 3.0
		    	    	    bw.flush();
		    	    	    bw.close();
						} catch (IOException e) {
							Log.e(LOG_TAG, "error", e);
						}
				    } else
				    {
				    	sdFile.delete();
				    }
				}
			}
			if (g.Common.PHARAON&&g.MyDatabase.m_order_editing.dont_need_send!=1)
			{
				// и запускаем обмен
				//new ExchangeTask().execute(3, 0, "11111");
				if (s.equals("printList"))
				{
					new ExchangeTask2().execute(3, 0, "L"+g.MyDatabase.m_order_editing.uid.toString());
				}
				if (s.equals("printKO"))
				{
					new ExchangeTask2().execute(3, 0, "K"+g.MyDatabase.m_order_editing.uid.toString());
				}
				if (s.equals("printBoth"))
				{
					new ExchangeTask2().execute(3, 0, "B"+g.MyDatabase.m_order_editing.uid.toString());
				}
			}
		} else
		{
			//Bundle bundle = new Bundle();
			//bundle.putString("reason", m_reason_cant_save);
			m_reason_cant_save=reason.toString();
			removeDialog(IDD_CANT_SAVE);
			showDialog(IDD_CANT_SAVE);
		}
	} else
	if (s.equals("recalc_price"))
	{
		OrdersHelpers.recalcPrice(this);
		redrawSumWeight();
	}
	
    //Fragment frag1 = getFragmentManager().findFragmentById(R.id.fragment1);
    //((TextView)frag1.getView().findViewById(R.id.textView)).setText("Text from Fragment 2:" + s);
}

    @Override
    public void onButtonsOkCancel_Ok(Integer dialogId, Intent intent) {

        MySingleton g=MySingleton.getInstance();

        OrderRecord rec=g.MyDatabase.m_order_editing;
        //public static final int MY_ALERT_DIALOG_IDD_SHIPPING_DATE=2;

        switch (dialogId)
        {
            case MY_ALERT_DIALOG_IDD_DATE:
            {
                int day=Integer.parseInt(rec.datedoc.substring(6,8));
                int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
                int year=Integer.parseInt(rec.datedoc.substring(0,4));

                int selectedYear=intent.getIntExtra("year", year);
                int selectedMonth=intent.getIntExtra("month", month);
                int selectedDay=intent.getIntExtra("day", day);

                if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
                {
                    rec.datedoc=Common.MyDateFormat("yyyyMMddHHmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                    int viewId=pager.getId();
                    int position=0;
                    Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                    if (frag0!=null) {
						View view0 = frag0.getView();

						EditText etDate = (EditText) view0.findViewById(R.id.etDate);
						StringBuilder sb = new StringBuilder();
						sb.append(rec.datedoc.substring(6, 8) + "." + rec.datedoc.substring(4, 6) + "." + rec.datedoc.substring(0, 4));
						etDate.setText(sb.toString());

						setModified(true);
					}
                }
            }
            break;
            case MY_ALERT_DIALOG_IDD_SHIPPING_DATE: {
                int day = Integer.parseInt(rec.shipping_date.substring(6, 8));
                int month = Integer.parseInt(rec.shipping_date.substring(4, 6)) - 1;
                int year = Integer.parseInt(rec.shipping_date.substring(0, 4));

                int selectedYear = intent.getIntExtra("year", year);
                int selectedMonth = intent.getIntExtra("month", month);
                int selectedDay = intent.getIntExtra("day", day);

                if (year != selectedYear || month != selectedMonth || day != selectedDay) {
                    rec.shipping_date = Common.MyDateFormat("yyyyMMddHHmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                    int viewId = pager.getId();
                    int position = 0;
                    Fragment frag0 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                    if (frag0 != null) {
                        View view0 = frag0.getView();

                        EditText etOrderDate = (EditText) view0.findViewById(R.id.etOrderDate);
                        StringBuilder sb=new StringBuilder();
                        sb.append(Common.dateStringAsText(rec.shipping_date));
                        etOrderDate.setText(sb.toString());

                        setModified(true);
                    }
                }
            }
            break;
            case MY_ALERT_DIALOG_IDD_BEGIN_TIME:
            {
                int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
                int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));

                int selectedHourOfDay = intent.getIntExtra("hour", hourOfDay);
                int selectedMinute = intent.getIntExtra("minute", minute);

                if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
                {

                    int viewId = pager.getId();
                    int position = 0;
                    Fragment frag0 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                    if (frag0 != null) {
                        View view0 = frag0.getView();

                        StringBuilder sb = new StringBuilder();
                        EditText etOrderTime = (EditText) view0.findViewById(R.id.etOrderTime);
                        EditText etTime = (EditText) view0.findViewById(R.id.etShippingBeginTime);
                        rec.shipping_begin_time = String.format("%02d%02d", selectedHourOfDay, selectedMinute);
                        sb.append(rec.shipping_begin_time.substring(0, 2) + ":" + rec.shipping_begin_time.substring(2, 4));
                        etOrderTime.setText(sb.toString());
                        etTime.setText(sb.toString());

                        setModified(true);
                    }
                }
            }
            break;
            case MY_ALERT_DIALOG_IDD_END_TIME:
            {
                int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
                int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));

                int selectedHourOfDay = intent.getIntExtra("hour", hourOfDay);
                int selectedMinute = intent.getIntExtra("minute", minute);

                if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
                {

                    int viewId = pager.getId();
                    int position = 0;
                    Fragment frag0 = getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                    if (frag0 != null) {
                        View view0 = frag0.getView();

                        StringBuilder sb=new StringBuilder();
                        EditText etTime = (EditText) view0.findViewById(R.id.etShippingEndTime);
                        rec.shipping_end_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
                        sb.append(rec.shipping_end_time.substring(0,2)+":"+rec.shipping_end_time.substring(2,4));
                        etTime.setText(sb.toString());

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
	    	/*
	    	if (orientationLandscape&&position==0)
	    	{
	    		// Страница шапка+товары идет за границами номеров
	    		return OrderPageFragment.newInstance(3);
	    	}
	    	if (orientationLandscape&&position==1)
	    	{
	    		// Перескакиваем товары, выводим дополнительно
	    		return OrderPageFragment.newInstance(2);
	    	}
	    	*/
	    	return OrderPageFragment.newInstance(position, m_backup_distr_point_id);
	    }

	    @Override
	    public int getCount() {
	    	if (orientationLandscape)
	    		return PAGE_COUNT_LANDSCAPE; 
	      return PAGE_COUNT;
	    }
	    @Override
	    public CharSequence getPageTitle(int position) {
	    	String[] titles = getResources().getStringArray(R.array.tabs_order);
	    	if (orientationLandscape)
	    	{
		    	titles = getResources().getStringArray(R.array.tabs_order_landscape);
	    	}
	    	if (position<titles.length)
	    		return titles[position];
	    	return "Title " + position;
	    }
	    
	  }
	  
	  
	@Override
	protected void onResume() {
		MySingleton g=MySingleton.getInstance();
		
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		//String str_work_date=sharedPreferences.getString("work_date", DatePreference.defaultCalendarString());
		
		Calendar work_date=DatePreference.getDateFor(sharedPreferences, "work_date");
		
		if (!m_work_date.equals(work_date))
		{
			// изменили рабочую дату - изменим дату заказа
			m_work_date=work_date;
			
			OrderRecord rec=g.MyDatabase.m_order_editing;
			
			rec.datedoc=Common.MyDateFormat("yyyyMMddHHmmss", m_work_date.getTime());
	
			int viewId=pager.getId();
			int position=0;
			Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
			// 25.06.2020 вот не знаю, почему может быть null после onResume, и не только здесь
			if (frag0!=null) {
				View view0 = frag0.getView();
				EditText etDate = (EditText) view0.findViewById(R.id.etDate);
				StringBuilder sb = new StringBuilder();
				sb.append(Common.dateStringAsText(rec.datedoc));
				etDate.setText(sb.toString());
			}
			
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
		
	private void redrawSumWeight()
	{
		Intent intent = new Intent(ACTION);
		//intent.putExtra("test", "test");
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		
	}
	
    // Параметр doInBackground, прогресс, результат
    class ExchangeTask2 extends AsyncTask<Object, Integer, String> {
    	
        protected ProgressDialog progressDialog;
        int prev_progress_type;
        long file_length;
        boolean bStopped;
        String textToLog;
        boolean cancelIsVisible;
        boolean bIniFileReaded;
        int prevProgress;
    	
        @Override
        protected void onPreExecute() {
        	super.onPreExecute();
        	prev_progress_type=0;
        	prevProgress=-1;
        	bStopped=false;
        	bIniFileReaded=false;
        	cancelIsVisible=true;
        	
			progressDialog=new ProgressDialog(OrderActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle("Обмен");
			progressDialog.setMessage("Подключение к FTP серверу...");
			progressDialog.setCancelable(false);
			progressDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_popup_sync));
			progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Отмена", new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			    	cancel(true);
			    	if (!bStopped)
			    	{
			            //TextView tvExchangeState=(TextView)findViewById(R.id.tvExchangeState);
			            //tvExchangeState.setText("Процесс останавливается");
			    	}
			    }
			});
            //TextView tvExchangeState=(TextView)findViewById(R.id.tvExchangeState);
            //tvExchangeState.setText("Выполняется");
			progressDialog.show();
        }
        
        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
			Common.dismissWithExceptionHandling(progressDialog);
            bStopped=true;
            //TextView tvExchangeState=(TextView)findViewById(R.id.tvExchangeState);
            //tvExchangeState.setText("Выполнено");
            
            //setButtonsExchangeEnabled(true);
        }
        
        @Override
        protected void onCancelled() {
          super.onCancelled();
          Log.e(LOG_TAG, "!!!!!!!!!!Cancel!!!!!!!!!!!!!!!");
          bStopped=true;
          //TextView tvExchangeState=(TextView)findViewById(R.id.tvExchangeState);
          //tvExchangeState.setText("Прервано");
          //setButtonsExchangeEnabled(true);
          
        }
        
        protected void onProgressUpdate(Integer... progress) {
        	boolean bNeedHideCancelButton=false;
        	boolean bNeedShowCancelButton=false;
        	if (progress[0]==-2)
        	{
        		//EditText etLog=(EditText)findViewById(R.id.etTest);
        		//etLog.append(textToLog);
        		return;
        	}
        	if (prev_progress_type!=progress[0]&&progress[0]!=-1)
        	{
        		prev_progress_type=progress[0];
        		switch (progress[0])
        		{
        		case FTP_STATE_LOAD_IN_BASE:
        		case FTP_STATE_LOAD_HISTORY_IN_BASE:
        			progressDialog.setMessage("Загрузка в базу...");
        			bNeedHideCancelButton=true;
        			break;
        		case FTP_STATE_SUCCESS:
        			progressDialog.setMessage("Выполнено");
        			break;
        		case FTP_STATE_FINISHED_ERROR:
        			progressDialog.setMessage("Выполнено с ошибками");
        			bNeedShowCancelButton=true;
        			break;
        		case FTP_STATE_SEND_EOUTF:
        			progressDialog.setMessage("Отправка файла обмена");
        			bNeedShowCancelButton=true;
        			break;
        		case FTP_STATE_RECEIVE_ARCH:
        			progressDialog.setMessage("Получение файла обмена");
        			bNeedShowCancelButton=true;
        			break;
        		case FTP_STATE_RECEIVE_IMAGE:
        			progressDialog.setMessage("Получение изображения номенклатуры");
        			bNeedShowCancelButton=true;
        			break;
        		}
        	}
        	if (progress.length>=2&&prevProgress!=progress[1])
        	{
        		prevProgress=progress[1];
        		progressDialog.setProgress(progress[1]);
        	}
        	
			if (!Constants.MY_DEBUG)
			{
				if (bNeedHideCancelButton&&cancelIsVisible||bNeedShowCancelButton&&!cancelIsVisible)
				{
					int visibility=View.VISIBLE;
					if ((cancelIsVisible=bNeedShowCancelButton)==false)
					{
						visibility=View.GONE;
					}
					// Делаем невидимой кнопку
					progressDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(visibility);
					try
					{
						// И разметку, в которой она находится (это больше для красоты)
						// TODO свое нормальное окно прогресса
						View v=(View)progressDialog.getButton(DialogInterface.BUTTON_NEUTRAL).getParent().getParent();
						v.setVisibility(visibility);
					} catch(Exception e)
					{
						//
					}
				}
			}
        	
        }

		@Override
		protected String doInBackground(Object... params) {
        	int mode=(Integer)params[0];
        	
        	if (mode==3)
        	{
            	// Обмен с веб-сервисом
        		try
        		{
	            	publishProgress(FTP_STATE_SEND_EOUTF, 0);
	    		 	Cursor cursor=getContentResolver().query(MTradeContentProvider.SETTINGS_CONTENT_URI, new String[]{"agent_id"}, null, null, null);
	    		 	String agent_id="";
	       			if (cursor.moveToFirst())
	       			{
	       				agent_id=cursor.getString(0);
	       			}
	       			cursor.close();
	       			if (!agent_id.isEmpty())
	       			{
	       				SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(OrderActivity.this);
	       				String web_server_address=preferences.getString("web_server_address", "192.168.0.100");
	       				MySingleton g=MySingleton.getInstance();
	       				MyWebExchange.doInBackgroundWebService(g.MyDatabase, getApplicationContext(), (String)params[2], agent_id, web_server_address, 0, "", "");
	       			}
	       			textToLog="Загружены изменения\n";
	       			publishProgress(-2);
	       			Log.v(LOG_TAG, "OK");
	       			publishProgress(FTP_STATE_SUCCESS);
        		} catch (Exception e) {
        			//Log.v(LOG_TAG, androidHttpTransport.responseDump);
        			//Log.v(LOG_TAG, e.toString());
        			textToLog="Ошибка при обмене "+e.toString();
        			publishProgress(-2);
        			//Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        			publishProgress(FTP_STATE_FINISHED_ERROR);
        		}
        	}
			return null;
		}
    }

}

