package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import ru.code22.mtrade.PaymentPageFragment.onSomePaymentEventListener;
import ru.code22.mtrade.MyDatabase.MyID;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Date;

public class PaymentActivity extends AppCompatActivity implements onSomePaymentEventListener {

    static final String LOG_TAG = "mtradeLogs";
    static final int PAGE_COUNT = 2;

	static final int PAYMENT_RESULT_OK=1;
	static final int PAYMENT_RESULT_CANCEL=2;
	
    public static final int IDD_CLOSE_QUERY = 1;
    public static final int IDD_CANT_SAVE=2;
    
    static final int SELECT_CLIENT_FROM_PAYMENT_REQUEST = 1;
    static final int SELECT_AGREEMENT_FROM_PAYMENT_REQUEST = 2;
    static final int SELECT_MANAGER_FROM_PAYMENT_REQUEST = 3;
    static final int SELECT_VICARIOUS_POWER_FROM_PAYMENT_REQUEST = 4;

    String m_reason_cant_save;

    ViewPager pager;
    PaymentActivity.MyFragmentPagerAdapter pagerAdapter;
    boolean orientationLandscape;

    private LocationManager m_locationManagerInPayment;

    private LocationListener locListener = new LocationListener()
    {
        @Override
        public void onLocationChanged(Location loc)
        {
            Globals g=(Globals)getApplication();

            if (loc!=null)
            {
                m_locationManagerInPayment.removeUpdates(locListener);

                boolean gpsenabled = m_locationManagerInPayment.isProviderEnabled(LocationManager.GPS_PROVIDER);

                MySingleton.getInstance().MyDatabase.m_payment_editing.latitude=loc.getLatitude();
                MySingleton.getInstance().MyDatabase.m_payment_editing.longitude=loc.getLongitude();
                MySingleton.getInstance().MyDatabase.m_payment_editing.datecoord=Common.getDateTimeAsString14(new Date(loc.getTime()));
                MySingleton.getInstance().MyDatabase.m_payment_editing.gpsstate=gpsenabled?1:loc.getProvider().equals(LocationManager.NETWORK_PROVIDER)?2:0;
                MySingleton.getInstance().MyDatabase.m_payment_editing.gpsaccuracy=loc.getAccuracy();
                MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord=0;

                int viewId=pager.getId();
                int position=1;
                Fragment frag0=getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewId + ":" + position);
                if (frag0!=null)
                {
                    View view0=frag0.getView();
                    if (view0!=null)
                    {
                        final TextView tvLatitude = (TextView) view0.findViewById(R.id.textViewPaymentLatitudeValue);
                        tvLatitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_payment_editing.latitude));
                        final TextView tvLongitude = (TextView) view0.findViewById(R.id.textViewPaymentLongitudeValue);
                        tvLongitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_payment_editing.longitude));
                        final TextView tvDateCoord = (TextView) view0.findViewById(R.id.textViewPaymentDateCoordValue);
                        tvDateCoord.setText(MySingleton.getInstance().MyDatabase.m_payment_editing.datecoord);

                        final CheckBox cbReceiveCoord=(CheckBox)view0.findViewById(R.id.checkBoxPaymentReceiveCoord);
                        cbReceiveCoord.setChecked(MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord==1);
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
		bCreatedInSingleton=g.MyDatabase.m_payment_editing.bCreatedInSingleton;
		
		g.MyDatabase.m_payment_editing_id=savedInstanceState.getLong("payment_editing_id");
		g.MyDatabase.m_payment_editing_modified=savedInstanceState.getBoolean("payment_editing_modified");
		
		g.MyDatabase.m_payment_editing=savedInstanceState.getParcelable("payment_editing");
		
		return bCreatedInSingleton;
    }	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();
		
		outState.putLong("payment_editing_id", g.MyDatabase.m_payment_editing_id);
		outState.putBoolean("payment_editing_modified", g.MyDatabase.m_payment_editing_modified);
		
		outState.putParcelable("payment_editing", g.MyDatabase.m_payment_editing);
		
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        MySingleton g=MySingleton.getInstance();
        g.checkInitByDataAndSetTheme(this);

        setContentView(R.layout.payment_pager);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

        boolean bCreatedInSingleton=false;
        boolean bRecreationActivity=false;
        if (savedInstanceState!=null&&savedInstanceState.containsKey("payment_editing"))
        {
            bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
            // но Singleton при этом не всегда уничтожался
            bCreatedInSingleton=HandleBundle(savedInstanceState);
        }

        //if (!bCreatedInSingleton)
        //{
        //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
        //}

		pager = (ViewPager) findViewById(R.id.pagerPayment);
		pagerAdapter = new PaymentActivity.MyFragmentPagerAdapter(getSupportFragmentManager());
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

		m_locationManagerInPayment = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

		if (MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord==1&&m_locationManagerInPayment!=null)
		{
			// здесь не проверяем состояние, отработана заявка или нет
			// координаты принимаются всегда, однако флаг измененности заявки не устанавливаем
			// при получении координат

			Criteria criteria = new Criteria();
			criteria.setAccuracy( Criteria.ACCURACY_COARSE );
			String provider = m_locationManagerInPayment.getBestProvider( criteria, true );

			//m_locationManagerInOrder.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
			if (provider!=null)
			{
                m_locationManagerInPayment.requestLocationUpdates(provider,  0, 0, locListener);
			}
		}


	}
	
    protected void setModified()
    {
    	MySingleton g=MySingleton.getInstance();
  	  if (!g.MyDatabase.m_payment_editing_modified)
  	  {
  		  setTitle(R.string.title_activity_payment_changed);
  		  g.MyDatabase.m_payment_editing_modified=true;
  	  }
    }
    
	public boolean paymentCanBeSaved(StringBuffer reason)
	{
		MySingleton g=MySingleton.getInstance();
		String strWarning="";
		if (g.MyDatabase.m_payment_editing.client_id.isEmpty())
		{
			//strWarning+=",клиент";
			strWarning+=","+getString(R.string.field_client);
		}
		//if (g.Common.FACTORY&&!stuff_agreement_required)
		//{} else
		if (g.MyDatabase.m_payment_editing.agreement_id.isEmpty())
		{
			//strWarning+=",договор";
			strWarning+=","+getString(R.string.field_agreement);
		}
		if (g.MyDatabase.m_payment_editing.manager_id.isEmpty())
		{
			//strWarning+=",куратор";
			strWarning+=","+getString(R.string.field_manager);
		}
		if (g.MyDatabase.m_payment_editing.sumDoc<0.0095)
		{
			//strWarning+=",сумма";
			strWarning+=","+getString(R.string.field_sum);
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
	
	  @Override
		protected Dialog onCreateDialog(int id) {
		  //MySingleton g=MySingleton.getInstance();
		  //CashPaymentRecord rec=g.MyDatabase.m_payment_editing;
			switch (id) {
			case IDD_CLOSE_QUERY:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.payment_modiffied));
				builder.setMessage(getResources().getString(R.string.save_it_before_closing));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						StringBuffer reason=new StringBuffer();
						if (paymentCanBeSaved(reason))
						{
							Intent intent=new Intent();
							setResult(PAYMENT_RESULT_OK, intent);
                            m_locationManagerInPayment.removeUpdates(locListener);
							finish();
						} else
						{
							dialog.cancel();
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
						setResult(PAYMENT_RESULT_CANCEL, intent);
                        m_locationManagerInPayment.removeUpdates(locListener);
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
			case IDD_CANT_SAVE:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.payment_cant_be_saved));
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
	  public boolean onOptionsItemSelected(MenuItem item) {
	  	
	  	switch (item.getItemId())
	  	{
	  	
		case R.id.action_settings:
		{
			Intent intent=new Intent(PaymentActivity.this, PrefPaymentActivity.class);
			startActivity(intent);
			break;
		}
	  	case R.id.action_save:
	  	{
	  		StringBuffer reason=new StringBuffer();
	  		if (paymentCanBeSaved(reason))
	  		{
	  			Intent intent=new Intent();
	  			setResult(PAYMENT_RESULT_OK, intent);
	  			finish();
	  		} else
	  		{
	  			m_reason_cant_save=reason.toString();
	  			removeDialog(IDD_CANT_SAVE);
	  			showDialog(IDD_CANT_SAVE);
	  		}
	  		break;
	  	}
	  	case R.id.action_close:
	  	{
	  		Intent intent=new Intent();
	  		setResult(PAYMENT_RESULT_CANCEL, intent);
	  		finish();
	  		break;
	  	}
	  	}
	    
	    return super.onOptionsItemSelected(item);
	  }

	// возвращает true в случае, если клиент изменился
    public boolean onPaymentClientSelected(View view0, long _id)
	{
		MySingleton g=MySingleton.getInstance();
		
		if (_id!=0)
		{
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, _id);
		    Cursor clientsCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "address", "curator_id", "flags", "priceType", "email_for_cheques"}, null, null, null);
		    if (clientsCursor.moveToNext())
		    {
		    	int idIndex = clientsCursor.getColumnIndex("id");
		    	int descrIndex = clientsCursor.getColumnIndex("descr");
		    	int addressIndex = clientsCursor.getColumnIndex("address");
				int emailIndex = clientsCursor.getColumnIndex("email_for_cheques");
		    	int curator_idIndex = clientsCursor.getColumnIndex("curator_id");
		    	int flags_Index = clientsCursor.getColumnIndex("flags");
		    	int priceType_Index = clientsCursor.getColumnIndex("priceType");
		    	String clientId = clientsCursor.getString(idIndex);
		    	if (g.MyDatabase.m_payment_editing.client_id.toString().equals(clientId))
		    	{
		    		return false;
		    	}
		    	String clientDescr = clientsCursor.getString(descrIndex);
		    	String clientAddress = clientsCursor.getString(addressIndex);
				String clientEmail = clientsCursor.getString(emailIndex);
		    	String curatorId = clientsCursor.getString(curator_idIndex);
		    	int flags = clientsCursor.getInt(flags_Index); 
		    	
		    	g.MyDatabase.m_payment_editing.client_id=new MyID(clientId);
		    	g.MyDatabase.m_payment_editing.stuff_client_name=clientDescr;
		    	g.MyDatabase.m_payment_editing.stuff_client_address=clientAddress;
				g.MyDatabase.m_payment_editing.stuff_email=clientEmail;

				// Долг контрагента
			    Cursor cursorDebt=getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{g.MyDatabase.m_payment_editing.client_id.toString()}, null);
			    if (cursorDebt.moveToNext())
			    {
			    	int indexDebt = cursorDebt.getColumnIndex("saldo");
			    	int indexDebtPast = cursorDebt.getColumnIndex("saldo_past");
			    	int indexDebtPast30 = cursorDebt.getColumnIndex("saldo_past30");
					g.MyDatabase.m_payment_editing.stuff_debt=cursorDebt.getDouble(indexDebt); 
					g.MyDatabase.m_payment_editing.stuff_debt_past=cursorDebt.getDouble(indexDebtPast);
					g.MyDatabase.m_payment_editing.stuff_debt_past30=cursorDebt.getDouble(indexDebtPast30);
			    } else
			    {
			    	g.MyDatabase.m_payment_editing.stuff_debt=0.0;
			    	g.MyDatabase.m_payment_editing.stuff_debt_past=0.0;
			    	g.MyDatabase.m_payment_editing.stuff_debt_past30=0.0;
			    }
			    // Куратор
			    /*
			    Cursor curatorsCursor=getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{curatorId}, null);
			    String curatorDescr;
			    if (curatorsCursor!=null &&curatorsCursor.moveToNext())
			    {
			    	int curator_descrIndex = curatorsCursor.getColumnIndex("descr");
			    	curatorDescr = curatorsCursor.getString(curator_descrIndex);
			    	g.MyDatabase.m_payment_editing.manager_id=new MyID(curatorId);
			    	g.MyDatabase.m_payment_editing.stuff_manager_name=curatorDescr;
			    } else
			    {
			    	g.MyDatabase.m_payment_editing.manager_id=new MyID();
			    	g.MyDatabase.m_payment_editing.stuff_manager_name="";
			    	curatorDescr = getResources().getString(R.string.curator_not_set);
			    	g.MyDatabase.m_payment_editing.stuff_debt=0.0;
			    	g.MyDatabase.m_payment_editing.stuff_debt_past=0.0;
			    }
			    */
			    
				// Клиент
	    		EditText etClient=(EditText)view0.findViewById(R.id.etPaymentClient);
	    		etClient.setText(clientDescr);
				// и его адрес
				//TextView tvClientAddress = (TextView)view0.findViewById(R.id.textViewOrdersClientAddress);
				//tvClientAddress.setText(clientAddress);
				// Электронная почта для чеков (это поле видно не у всех, но у всех заполняется)
				TextView tvEmailForCheques=(TextView)view0.findViewById(R.id.tvEmailForCheques);
				if (clientEmail.contains("@"))
					tvEmailForCheques.setText(g.MyDatabase.m_payment_editing.stuff_email);
				else
					tvEmailForCheques.setText(R.string.no_email);
				// Долг
				EditText etDebt = (EditText)view0.findViewById(R.id.editTextDebt);
				etDebt.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt));
				EditText etDebtPast = (EditText)view0.findViewById(R.id.editTextDebtPast);
				etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt_past));
				EditText etDebtPast30 = (EditText)view0.findViewById(R.id.editTextDebtPast30);
				etDebtPast30.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt_past30));
				
				// Менеджер
				//EditText etPaymentManager=(EditText)findViewById(R.id.etPaymentManager);
				//etPaymentManager.setText(g.MyDatabase.m_payment_editing.stuff_manager_name);
		    	clientsCursor.close();
		    	//curatorsCursor.close();
			    cursorDebt.close();
		    	
		    	return true;
		    }
			clientsCursor.close();
		}
		
		// Клиент не был указан и не изменился
		if (g.MyDatabase.m_payment_editing.client_id.isEmpty())
			return false;
		// Клиент был указан, а сейчас не найден - очищаем все
		EditText etPaymentClient=(EditText)view0.findViewById(R.id.etPaymentClient);
		etPaymentClient.setText(getResources().getString(R.string.client_not_set));
		//TextView tvClientAddress = (TextView)findViewById(R.id.textViewOrdersClientAddress);
		//tvClientAddress.setText("");
		//EditText etPaymentManager=(EditText)findViewById(R.id.etPaymentManager);
		//etPaymentManager.setText(getResources().getString(R.string.curator_not_set));
		//EditText etDistrPoEditText=(EditText)findViewById(R.id.etTradePoint);
		//etDistrPoEditText.setText(getResources().getString(R.string.trade_point_not_set));
		
		g.MyDatabase.m_payment_editing.client_id=new MyID();
		g.MyDatabase.m_payment_editing.stuff_client_name="";
		g.MyDatabase.m_payment_editing.stuff_client_address="";
		//g.MyDatabase.m_payment_editing.manager_id=new MyID();
		//g.MyDatabase.m_payment_editing.stuff_manager_name="";
		
		return true;
	}

    public boolean onPaymentAgreementSelected(View view0, long _id)
        {
            MySingleton g = MySingleton.getInstance();
            if (_id != 0) {
                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS_CONTENT_URI, _id);
                Cursor agreementsCursor = getContentResolver().query(singleUri, new String[]{"id", "descr", "organization_id", "price_type_id", "sale_id"}, null, null, null);
                if (agreementsCursor != null && agreementsCursor.moveToNext()) {
                    int idIndex = agreementsCursor.getColumnIndex("id");
                    int descrIndex = agreementsCursor.getColumnIndex("descr");
                    int organization_idIndex = agreementsCursor.getColumnIndex("organization_id");
                    //int priceType_idIndex = agreementsCursor.getColumnIndex("price_type_id");
                    String agreementId = agreementsCursor.getString(idIndex);
                    String agreementDescr = agreementsCursor.getString(descrIndex);
                    String organizationId = agreementsCursor.getString(organization_idIndex);
                    //String priceTypeId = agreementsCursor.getString(priceType_idIndex);
                    if (g.MyDatabase.m_payment_editing.agreement_id.toString().equals(agreementId)) {
                        return false;
                    }
                    EditText etAgreement = (EditText) view0.findViewById(R.id.etPaymentAgreement);
                    etAgreement.setText(agreementDescr);
                    g.MyDatabase.m_payment_editing.agreement_id = new MyID(agreementId);
                    g.MyDatabase.m_payment_editing.stuff_agreement_name = agreementDescr;
                    // и заполняем организацию
                    Cursor cursorOrganizations = getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId}, null);
                    if (cursorOrganizations != null && cursorOrganizations.moveToNext()) {
                        int descrIndexOrganization = cursorOrganizations.getColumnIndex("descr");
                        String descrOrganization = cursorOrganizations.getString(descrIndexOrganization);
                        //g.MyDatabase.m_payment_editing.organization_id=new MyID(organizationId);
                        g.MyDatabase.m_payment_editing.stuff_organization_name = descrOrganization;
                        TextView tvOrganization = (TextView) view0.findViewById(R.id.textViewPaymentOrganization);
                        tvOrganization.setText(descrOrganization);
                    } else {
                        TextView tvOrganization = (TextView) view0.findViewById(R.id.textViewPaymentOrganization);
                        tvOrganization.setText(getResources().getString(R.string.organization_not_set));
                        //g.MyDatabase.m_payment_editing.organization_id.m_id="";
                        g.MyDatabase.m_payment_editing.stuff_organization_name = getResources().getString(R.string.item_not_set);
                    }

                    // Долг контрагента по договору
                    if (g.Common.TITAN || g.Common.INFOSTART) {
                        double agreement_debt = 0;
                        double agreement_debt_past = 0;
                        Cursor cursorAgreementDebt = getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{g.MyDatabase.m_payment_editing.agreement_id.toString()}, null);
                        int saldoIndex = cursorAgreementDebt.getColumnIndex("saldo");
                        int saldoPastIndex = cursorAgreementDebt.getColumnIndex("saldo_past");
                        while (cursorAgreementDebt.moveToNext()) {
                            agreement_debt = agreement_debt + cursorAgreementDebt.getDouble(saldoIndex);
                            agreement_debt_past = agreement_debt_past + cursorAgreementDebt.getDouble(saldoPastIndex);
                        }
                        cursorAgreementDebt.close();
                        g.MyDatabase.m_payment_editing.stuff_agreement_debt = agreement_debt;
                        g.MyDatabase.m_payment_editing.stuff_agreement_debt_past = agreement_debt_past;

                        // Долг по договору
                        EditText etDebt = (EditText) view0.findViewById(R.id.editTextAgreementDebt);
                        etDebt.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt));
                        EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextAgreementDebtPast);
                        etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt_past));
                    }

                    return true;
                }
            }
            // Договор не был указан и не изменился
            if (g.MyDatabase.m_payment_editing.agreement_id.isEmpty())
                return false;
            // Договор был указан, а сейчас не найден - очищаем все
            EditText etAgreement = (EditText) view0.findViewById(R.id.etPaymentAgreement);
            TextView tvOrganization = (TextView) view0.findViewById(R.id.textViewPaymentOrganization);
            etAgreement.setText(getResources().getString(R.string.agreement_not_set));
            tvOrganization.setText(getResources().getString(R.string.organization_not_set));

            g.MyDatabase.m_payment_editing.agreement_id = new MyID();
            g.MyDatabase.m_payment_editing.stuff_agreement_name = "";
            //g.MyDatabase.m_payment_editing.organization_id=new MyID();
            g.MyDatabase.m_payment_editing.stuff_organization_name = "";
            //g.MyDatabase.m_payment_editing.simple_discount_id=new MyID();
            //g.MyDatabase.m_payment_editing.stuff_discount=getResources().getString(R.string.without_discount);
            //g.MyDatabase.m_payment_editing.stuff_discount_procent=0.0;

            return true;
        }

    @Override
    protected void onDestroy() {
        m_locationManagerInPayment.removeUpdates(locListener);
        super.onDestroy();
    }

		private void onCloseActivity()
		{
			MySingleton g=MySingleton.getInstance();
			if (g.MyDatabase.m_payment_editing_modified)
			{
				showDialog(IDD_CLOSE_QUERY);
			} else
			{
				Intent intent=new Intent();
				setResult(PAYMENT_RESULT_CANCEL, intent);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.payment, menu);
		return true;
	}

    @Override
    public void somePaymentEvent(String s) {
        //Globals g=(Globals)getApplication();
        if (s.equals("coord"))
        {
            if (MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord==1)
            {
                m_locationManagerInPayment.requestLocationUpdates(LocationManager.GPS_PROVIDER,  0, 0, locListener);
            } else
            {
                m_locationManagerInPayment.removeUpdates(locListener);
            }
        } else
        if (s.equals("close"))
        {
            onCloseActivity();
        } else
        if (s.equals("ok"))
        {
            StringBuffer reason=new StringBuffer();
            if (paymentCanBeSaved(reason))
            {
                Intent intent=new Intent();
                setResult(PAYMENT_RESULT_OK, intent);
                // 16.01.2013
                m_locationManagerInPayment.removeUpdates(locListener);
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
            return PaymentPageFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            String[] titles = getResources().getStringArray(R.array.tabs_payment);
            if (position<titles.length)
                return titles[position];
            return "Title " + position;
        }

    }

}
