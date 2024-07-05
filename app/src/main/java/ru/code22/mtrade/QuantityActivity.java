package ru.code22.mtrade;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.ClientPriceRecord;
import ru.code22.mtrade.MyDatabase.CuratorPriceRecord;
import ru.code22.mtrade.MyDatabase.MyID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.Math;

public class QuantityActivity extends AppCompatActivity
			implements LoaderManager.LoaderCallbacks<Cursor> {
	
	static final int QUANTITY_RESULT_OK=1;
	static final int QUANTITY_RESULT_DELETE_LINE=2;
	
	public static final int IDD_QUERY_ZERO_QUANTITY_FROM_NOMENCLATURE = 1;
	public static final int IDD_QUERY_ZERO_QUANTITY_FROM_ORDER = 2;
	public static final int IDD_TIME = 3;
	
	SimpleCursorAdapter m_salesAdapter;
	
	private static final int LOADER_ID = 1;
	
	static final String[] PROJECTION = new String[] {"_id", "numdoc", "datedoc", "quantity", "price"};
	
	boolean m_bPacks;
	EditText m_etQuantity;
	
	long m_id; // _id номенклатуры
	MyID m_nomenclature_id, m_nomenclature_parent_id;
	MyID m_price_type_id; // если тип прайса будет не указан в документе, здесь будет пустое значение
    MyID m_agreement30_id; // только для FACTORY. для "уточнения цен по товарам" из соглашений
	String m_image_file;
	
	double m_price;           // цена
	double m_price_k;         // коэффициент количества, для которого указана цена
	String m_price_ed_izm_id; // id единицы измерения, для которой указана цена 
	String m_price_edIzm;     // наименование этой единицы измерения
	
	String m_nomenclature_quant[];
	String m_nomenclature_edizm_id[];
	double m_nomenclature_quant_k[];
	double m_nomenclature_weight_k[];
	
	double m_rest; // остаток
	double m_min_quantity; // минимальное продаваемое количество
	double m_multiplicity; // кратность при продаже
	
	String m_shipping_time;
	String m_comment_in_line;
	
	static String m_mode="";
	
	long m_copy_order_id; // если объект введен копированием, здесь будет код копируемого
	// это нужно для того, чтобы при восстановлении приложения восстановились данные (если еще не успела произойти запись в базу, при изменении)
	String m_backup_client_id; // с отбором по какому клиенту был создан заказ
	String m_backup_distr_point_id;
	
	
	private void updatePrice()
	{
		TextView tvPrice=(TextView)findViewById(R.id.tvQuantityPrice);
		// Цена, посчитанная при входе
		if (m_price_edIzm==null)
		{
			// Цена не указана или не указан тип цены в заказе
			tvPrice.setText("-");
		} else
		{
			StringBuilder sb=new StringBuilder(Common.DoubleToStringFormat(m_price, "%.3f")).append('/').append(m_price_edIzm);
			int i;
			for (i=0; i<2; i++)
			{
				if (m_price_k<m_nomenclature_quant_k[i]-0.0001||m_price_k>m_nomenclature_quant_k[i]+0.0001)
				{
					sb.append(", ").append(Common.DoubleToStringFormat(m_price/m_price_k*m_nomenclature_quant_k[i], "%.3f")).append('/').append(m_nomenclature_quant[i]);
					if (i==1&&m_nomenclature_quant_k[0]>0.9999&&m_nomenclature_quant_k[0]<1.0001&&(m_nomenclature_quant_k[1]<0.9999||m_nomenclature_quant_k[1]>1.0001)&&m_nomenclature_quant_k[1]>0.0001)
					{
						// Обычный случай, вторая единица измерения есть, с коэффициентом не 1.0
						sb.append('(').append(Common.DoubleToStringFormat(m_nomenclature_quant_k[1]/m_nomenclature_quant_k[0], "%.3f")).append(m_nomenclature_quant[0]).append(')');
					}
				}
			}
			tvPrice.setText(sb.toString());
		}
	}

	private void updateSum()
	{
		TextView tvSum=(TextView)findViewById(R.id.tvQuantitySum);
		if (m_price_edIzm==null)
		{
			// Цена не указана или не указан тип цены в заказе
			tvSum.setText("-");
		} else
		{
			int ed_idx=m_bPacks?1:0;
			double quantity=Double.parseDouble(m_etQuantity.getText().toString());
			if (m_price_k<m_nomenclature_quant_k[ed_idx]-0.0001||m_price_k>m_nomenclature_quant_k[ed_idx]+0.0001)
			{
				// Коэффициент отличается
				tvSum.setText(Common.DoubleToStringFormat(m_price*quantity/m_price_k*m_nomenclature_quant_k[ed_idx], "%.3f"));
			} else
			{
				// Коэффициент не отличается
				tvSum.setText(Common.DoubleToStringFormat(m_price*quantity, "%.3f"));
			}
		}
	}
	
	private void onDigit(char digit)
	{
		String s=m_etQuantity.getText().toString();
		s+=digit;
		if (s.length()>1&&s.charAt(0)=='0'&&!(s.substring(0, 2).equals("0.")))
			m_etQuantity.setText(s.substring(1));
		else
			m_etQuantity.setText(s);
		updateSum();
	}
	public void onDigit_0(View v)
	{
		onDigit('0');
	}
	public void onDigit_1(View v)
	{
		onDigit('1');
	}
	public void onDigit_2(View v)
	{
		onDigit('2');
	}
	public void onDigit_3(View v)
	{
		onDigit('3');
	}
	public void onDigit_4(View v)
	{
		onDigit('4');
	}
	public void onDigit_5(View v)
	{
		onDigit('5');
	}
	public void onDigit_6(View v)
	{
		onDigit('6');
	}
	public void onDigit_7(View v)
	{
		onDigit('7');
	}
	public void onDigit_8(View v)
	{
		onDigit('8');
	}
	public void onDigit_9(View v)
	{
		onDigit('9');
	}
	public void onDigit_dot(View v)
	{
		if (m_etQuantity.getText().toString().indexOf('.')<0)
		{
			onDigit('.');
		}
	}
	public void onDigit_clear(View v)
	{
		m_etQuantity.setText("0");
		updateSum();
	}
	public void onDigit_back(View v)
	{
		//m_etQuantity.setText("0");
		String s=m_etQuantity.getText().toString();
		if (s.length()>1)
			m_etQuantity.setText(s.substring(0, s.length()-1));
		else
			m_etQuantity.setText("0");
		updateSum();
	}
	
	private void acceptData(boolean checkQuantity)
	{
		Intent intent=new Intent();
		// Если открывали из списка номенклатуры
		if (m_id!=0)
		{
			intent.putExtra("_id", m_id);
		}
		// Если открывали из строк заказа
		if (!m_nomenclature_id.isEmpty())
		{
			intent.putExtra("id", m_nomenclature_id.toString());
		}
		double quantity=Double.parseDouble(m_etQuantity.getText().toString());
		if (quantity>-0.0001&&quantity<0.0001)
		{
			// Надо задать вопрос
			if (m_id!=0)
			{
				showDialog(IDD_QUERY_ZERO_QUANTITY_FROM_NOMENCLATURE);
			} else
			{
				showDialog(IDD_QUERY_ZERO_QUANTITY_FROM_ORDER);
			}
		} else
		{
			// 
			intent.putExtra("quantity", quantity);
			intent.putExtra("k", m_nomenclature_quant_k[m_bPacks?1:0]);
			intent.putExtra("ed", m_nomenclature_quant[m_bPacks?1:0]);
			intent.putExtra("comment_in_line", m_comment_in_line);
			if (m_mode.equals("REFUND"))
			{
				setResult(QUANTITY_RESULT_OK, intent);
				finish();
				return;
			}
			intent.putExtra("price", m_price);
			intent.putExtra("price_k", m_price_k);
			
			boolean bMultiplicityOk=true;
			if (m_multiplicity>0.0001)
			{
				// можно продавать только кратное количество
				double fr=quantity*m_nomenclature_quant_k[m_bPacks?1:0]/m_multiplicity;
				fr=fr-Math.floor(fr);
				if (fr>0.999)
					fr-=1.0;
				if (fr<-0.001)
					fr+=1.0;
				if (fr<-0.001||fr>0.001)
				{
					bMultiplicityOk=false;
				}
				
			}

			if (quantity<0.0001)
			{
				// если нулевое количество - условие минимального количества не проверяем - строка будет удалена
			} else
			// Больше, чем на складе можно продавать только кратное количество
			if (checkQuantity&&quantity*m_nomenclature_quant_k[m_bPacks?1:0]>m_rest+0.0001)
			{
				if (!bMultiplicityOk)
				{
					Snackbar.make(findViewById(android.R.id.content), "Продажа количества, не кратного "+Common.DoubleToStringFormat(m_multiplicity, "%.3f")+" запрещена!", Snackbar.LENGTH_LONG).show();
				} else
				{
					AlertDialog.Builder builder=new AlertDialog.Builder(QuantityActivity.this);
					builder.setMessage("Вы продаете больше, чем в наличии на складе, продолжить?");
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int id) {
						    //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
							//loadDataAfterStart();
							//dialog.cancel();
							//QuantityActivity.setResult(QuantityActivity.QUANTITY_RESULT_OK, intent);
							//finish();
							acceptData(false);
						}
					});
					builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					
					builder.setCancelable(false);
					builder.create().show();
				}
			} else
			if (checkQuantity&&m_min_quantity>quantity*m_nomenclature_quant_k[m_bPacks?1:0]+0.0001&&quantity*m_nomenclature_quant_k[m_bPacks?1:0]<m_rest-0.0001)
			{
				// В случае если продаем все что есть на складе, не ругаемся (здесь и в следующей проверке)
				Snackbar.make(findViewById(android.R.id.content), "Продажа ниже минимального количества запрещена!", Snackbar.LENGTH_LONG).show();
			} else
			if (checkQuantity&&!bMultiplicityOk&&quantity*m_nomenclature_quant_k[m_bPacks?1:0]<m_rest-0.0001)
			{
				// На складе товара достаточно, но количество указали не кратное
				Snackbar.make(findViewById(android.R.id.content), "Продажа количества, не кратного "+Common.DoubleToStringFormat(m_multiplicity, "%.3f")+" запрещена!", Snackbar.LENGTH_LONG).show();
			} else
			{
				intent.putExtra("shipping_time", m_shipping_time);
				setResult(QUANTITY_RESULT_OK, intent);
				finish();
			}
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	
    public boolean HandleBundleRefund(Bundle savedInstanceState)
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
		
		return bCreatedInSingleton;
    }
	
    public boolean HandleBundleOrder(Bundle savedInstanceState)
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
		
	   	//m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
	    //m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
	   	//m_list_prices_descr = savedInstanceState.getStringArray("list_prices_descr");
	    //m_list_prices_id = savedInstanceState.getStringArray("list_prices_id");
	    //m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
	    //m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");
		
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
		if (m_mode.equals("REFUND"))
		{
			MySingleton g=MySingleton.getInstance();
			outState.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
			outState.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
			outState.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
			outState.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
		} else
		{
			MySingleton g=MySingleton.getInstance();
			outState.putLong("order_editing_id", g.MyDatabase.m_order_editing_id);
			outState.putLong("order_new_editing_id", g.MyDatabase.m_order_new_editing_id);
			outState.putBoolean("order_editing_created", g.MyDatabase.m_order_editing_created);
			outState.putBoolean("order_editing_modified", g.MyDatabase.m_order_editing_modified);
			outState.putLong("copy_order_id", m_copy_order_id);
			outState.putString("backup_client_id", m_backup_client_id);
            outState.putString("backup_distr_point_id", m_backup_distr_point_id);
			
			//outState.putStringArray("list_accounts_descr", m_list_accounts_descr);
			//outState.putStringArray("list_accounts_id", m_list_accounts_id);
			//outState.putStringArray("list_prices_descr", m_list_prices_descr);
			//outState.putStringArray("list_prices_id", m_list_prices_id);
			//outState.putStringArray("list_stocks_descr", m_list_stocks_descr);
			//outState.putStringArray("list_stocks_id", m_list_stocks_id);
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		setContentView(R.layout.quantity);
		
	    if (g.Common.PHARAOH)
	    {
	    	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	    }
	    
	    boolean bCreatedInSingleton=false;
	    boolean bRecreationActivity=false; 
		
	   	Intent intent=getIntent();
		String mode=intent.getStringExtra("MODE");
		if (mode==null)
			m_mode="";
		else
			m_mode=mode;
	   	
	   	m_bPacks=intent.getBooleanExtra("packs", false);
	   	m_id=intent.getLongExtra("_id", 0);
   		m_rest=intent.getDoubleExtra("rest", 0.0);
   		m_shipping_time=intent.getStringExtra("shipping_time");
   		m_comment_in_line=intent.getStringExtra("comment_in_line");
		if (m_shipping_time==null)
			m_shipping_time="";
		if (m_comment_in_line==null)
			m_comment_in_line="";
		
		if (m_mode.equals("REFUND"))
		{
		    if (savedInstanceState!=null&&savedInstanceState.containsKey("refund_editing"))
		    {
		    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
		    	// но Singleton при этом не всегда уничтожался
		    	bCreatedInSingleton=HandleBundleRefund(savedInstanceState);
		    }
		    //if (!bCreatedInSingleton)
		    //{
		    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
		    //}
			
		} else
		{
		    if (savedInstanceState!=null&&savedInstanceState.containsKey("order_editing_id"))
		    {
		    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
		    	bCreatedInSingleton=HandleBundleOrder(savedInstanceState);
		    }
		    
		    //if (!bCreatedInSingleton)
		    //{
		    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
		    //}
		    
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
			        		// TODO!!!!
		    	        	//onAgreementSelected(null, _idAgreement);
		    	        	OrdersHelpers.onAgreementSelected_RestrictedVer(this, null, _idAgreement, null, null);
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

		
		ListView listViewQuantitySalesHistory=(ListView)findViewById(R.id.listViewQuantitySalesHistory);

		if (g.Common.hasHistory())
		{
		
	        String[] fromColumns = {"numdoc", "price", "quantity"};
	        int[] toViews = {R.id.textViewOrderSalesLineNomenclature, R.id.textViewOrderSalesLineSum, R.id.textViewOrderSalesLineQuantity};
	
	        m_salesAdapter = new SimpleCursorAdapter(this, 
	                R.layout.quantity_sales_line_item, null,
	                fromColumns, toViews, 0);
	        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
	            @Override
	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	
	            	/*
	            	View w1=(View)view.getParent();
	            	
	        		int blocked_index=cursor.getColumnIndex("blocked");
	            	if (cursor.getInt(blocked_index)==0)
	            	{
	            		w1.setBackgroundColor(Color.TRANSPARENT);
	            	} else
	            	{
	            		w1.setBackgroundColor(Color.RED);
	            	}
	                String name = cursor.getColumnName(columnIndex);
	                if ("saldo".equals(name)||"saldo_past".equals(name)) {
	                	double val=cursor.getDouble(columnIndex);
	                	if (-0.001<val&&val<=0.001)
	                		((TextView)view).setText("");
	                	else
	                		((TextView)view).setText(String.format("%.2f", val));
	                    return true;
	                }
	                */
	            	
	            	switch (view.getId())
	            	{
	            	// это номер документа
	            	case R.id.textViewOrderSalesLineNomenclature:
	            	{
	            		int numDocIndex=cursor.getColumnIndex("numdoc");
	                	int dateDocIndex=cursor.getColumnIndex("datedoc");
		    			String d=cursor.getString(dateDocIndex);
		            	if (d.length()==14)
		            		d=Common.dateTimeStringAsText(d);
		            	else
							Common.dateStringAsText(d);
		    			((TextView)view).setText(cursor.getString(numDocIndex)+"; "+d);
	            		return true;
	            	}
	            	}
	            	// TODO если потребуется, можно вывести продажи в ед.изм.
	            	// и сумму вывести в рублях
	            	/*
	    			if (line.k>0.001)
	    			{
	    				vtQuantity.setText(g.Common.DoubleToStringFormat(rec.quantity/line.k, "%.3f")+line.ed);
	    			} else
	    			{
	    				vtQuantity.setText(g.Common.DoubleToStringFormat(rec.quantity/line.k, "%.3f"));
	    			}
	    			tvSum.setText(g.Common.DoubleToStringFormat(rec.price, "%.3f")+getString(R.string.currency_short));
	    			*/
	            	
	                return false;
	            }
	        };
	        
	        m_salesAdapter.setViewBinder(binder);
	        
	        //setListAdapter(mAdapter);
	        listViewQuantitySalesHistory.setAdapter(m_salesAdapter);
		} else
		{
			listViewQuantitySalesHistory.setVisibility(View.GONE);
			m_salesAdapter=null;
		}
        
	    // а также (packs) либо (k, ed)
        
		TextView tvCommentInLine=(TextView)findViewById(R.id.textViewComment);
		EditText etCommentInLine=(EditText)findViewById(R.id.etComment);
		
		if (g.Common.PHARAOH)
		{
			EditText etQuantityTime=(EditText)findViewById(R.id.etQuantityTime);
			if (m_shipping_time.isEmpty())
			{
				etQuantityTime.setText("");
			} else
			{
				etQuantityTime.setText(m_shipping_time.substring(0,2)+":"+m_shipping_time.substring(2,4));
			}
			etQuantityTime.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					String newDate=Common.inputDateToString4(s.toString());
					if (!m_shipping_time.equals(newDate))
					{
						m_shipping_time=newDate;
					}
				}
			});
			
		} else
		{
			
			TextView tvShippingTime=(TextView)findViewById(R.id.tvShippingTime);
			LinearLayout layoutShippingTime=(LinearLayout)findViewById(R.id.layoutShippingTime);
			
			tvShippingTime.setVisibility(View.GONE);
			layoutShippingTime.setVisibility(View.GONE);
			
			if (m_mode.equals("REFUND"))
			{
				tvCommentInLine.setVisibility(View.VISIBLE);
				etCommentInLine.setVisibility(View.VISIBLE);
			} else
			{
				tvCommentInLine.setVisibility(View.GONE);
				etCommentInLine.setVisibility(View.GONE);
			}
		}
		
		etCommentInLine.setText(m_comment_in_line);
		etCommentInLine.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				m_comment_in_line=s.toString();
			}
		});

   		m_nomenclature_parent_id=new MyID();
	   	Cursor cursorNomenclature;
	   	if (m_id==0)
	   	{
	   		m_nomenclature_id=new MyID(intent.getStringExtra("id"));
		   	cursorNomenclature=getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"descr", "parent_id", "quant_1", "quant_2", "edizm_1_id",	"edizm_2_id", "quant_k_1", "quant_k_2", "weight_k_1", "weight_k_2", "min_quantity", "multiplicity", "rozn_price", "m_opt_price", "opt_price", "image_file"}, "id=?", new String[]{m_nomenclature_id.toString()}, null);
	   	} else
	   	{
		   	Uri singleUriNomenclature = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, m_id);
		   	cursorNomenclature=getContentResolver().query(singleUriNomenclature, new String[]{"id", "descr", "parent_id", "quant_1", "quant_2", "edizm_1_id",	"edizm_2_id", "quant_k_1", "quant_k_2", "weight_k_1", "weight_k_2", "min_quantity", "multiplicity", "rozn_price", "m_opt_price", "opt_price", "image_file"}, null, null, null);
		   	if (cursorNomenclature.moveToFirst())
		   	{
		   		int nomenclatureIdIndex=cursorNomenclature.getColumnIndex("id");
		   		m_nomenclature_id=new MyID(cursorNomenclature.getString(nomenclatureIdIndex));
		   	} else
		   	{
		   		m_nomenclature_id=new MyID();
		   	}
	   	}
   		m_price_type_id=new MyID(intent.getStringExtra("price_type_id"));
	   	if (g.Common.FACTORY)
        {
            m_agreement30_id=new MyID(intent.getStringExtra("agreement30_id"));
        } else
        {
            m_agreement30_id=new MyID();
        }
   		
	   	if (cursorNomenclature.moveToFirst())
	   	{
	   		int descrIndex=cursorNomenclature.getColumnIndex("descr");
	   		int nomenclatureParentIdIndex=cursorNomenclature.getColumnIndex("parent_id");
	   		int quant_1_Index=cursorNomenclature.getColumnIndex("quant_1");
	   		int quant_2_Index=cursorNomenclature.getColumnIndex("quant_2");
	   		int edizm_1_id_Index=cursorNomenclature.getColumnIndex("edizm_1_id");
	   		int edizm_2_id_Index=cursorNomenclature.getColumnIndex("edizm_2_id");
	   		int quant_k_1_Index=cursorNomenclature.getColumnIndex("quant_k_1");
	   		int quant_k_2_Index=cursorNomenclature.getColumnIndex("quant_k_2");
	   		int weight_k_1_Index=cursorNomenclature.getColumnIndex("weight_k_1");
	   		int weight_k_2_Index=cursorNomenclature.getColumnIndex("weight_k_2");
	   		int min_quantity_Index=cursorNomenclature.getColumnIndex("min_quantity");
	   		int multiplicity_Index=cursorNomenclature.getColumnIndex("multiplicity");
	   		
	   		m_nomenclature_parent_id=new MyID(cursorNomenclature.getString(nomenclatureParentIdIndex));
	   		
		   	TextView tvDescr=(TextView)findViewById(R.id.textViewQuantityNomenclatureDescr);
		   	tvDescr.setText(cursorNomenclature.getString(descrIndex));
	   		
	   		m_nomenclature_quant=new String[]{cursorNomenclature.getString(quant_1_Index), cursorNomenclature.getString(quant_2_Index)};
	   		m_nomenclature_edizm_id=new String[]{cursorNomenclature.getString(edizm_1_id_Index), cursorNomenclature.getString(edizm_2_id_Index)};
	   		m_nomenclature_quant_k=new double[]{cursorNomenclature.getDouble(quant_k_1_Index), cursorNomenclature.getDouble(quant_k_2_Index)};
	   		m_nomenclature_weight_k=new double[]{cursorNomenclature.getDouble(weight_k_1_Index), cursorNomenclature.getDouble(weight_k_2_Index)};
	   		m_min_quantity=cursorNomenclature.getDouble(min_quantity_Index);
	   		m_multiplicity=cursorNomenclature.getDouble(multiplicity_Index);
	   		
			if (g.Common.MEGA)
			{
				String priceName="rozn_price";
				if (m_price_type_id.toString().equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value())))
				{
					priceName="m_opt_price";
				} else
				if (m_price_type_id.toString().equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_OPT.value())))
				{
					priceName="opt_price";
				}
				int price_Index=cursorNomenclature.getColumnIndex(priceName);
				double price=cursorNomenclature.getDouble(price_Index);
				//double price_k=1.0;
				CuratorPriceRecord curatorPrice=TextDatabase.GetCuratorPrice(getContentResolver(), g.MyDatabase.m_order_editing.curator_id, m_nomenclature_id);
				if (curatorPrice!=null)
				{
					price+=Math.floor(price*curatorPrice.priceProcent+0.00001)/100.0+curatorPrice.priceAdd;
				}
				m_price=price;
				m_price_k=1.0;
				m_price_ed_izm_id=cursorNomenclature.getString(edizm_1_id_Index);
				m_price_edIzm=cursorNomenclature.getString(quant_1_Index);
			} else
			if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART||g.Common.FACTORY)
			{
			   	Cursor cursorPrice=getContentResolver().query(MTradeContentProvider.PRICES_CONTENT_URI, new String[]{"ed_izm_id", "edIzm", "price", "priceProcent", "k"}, "nomenclature_id=? and price_type_id=?", new String[]{m_nomenclature_id.toString(), m_price_type_id.toString()}, null);
			   	double price=0.0;
			   	if (cursorPrice.moveToFirst())
			   	{
			   		int priceIndex=cursorPrice.getColumnIndex("price");
			   		int kIndex=cursorPrice.getColumnIndex("k");
			   		int edIzmIdIndex=cursorPrice.getColumnIndex("ed_izm_id");
			   		int edIzmIndex=cursorPrice.getColumnIndex("edIzm");
			   		price=cursorPrice.getDouble(priceIndex);
			   		double priceProcent=g.MyDatabase.m_order_editing.stuff_discount_procent;
	    			if (priceProcent!=0.0)
	    			{
	    				price+=Math.floor(price*priceProcent+0.00001)/100.0;
	    			}
					m_price=price;
					m_price_k=cursorPrice.getDouble(kIndex);
					if (m_price_k<0.0001)
						m_price_k=1.0;
					m_price_ed_izm_id=cursorPrice.getString(edIzmIdIndex);
					m_price_edIzm=cursorPrice.getString(edIzmIndex);
			   	} else
			   	{
			   		m_price=0.0;
			   		m_price_k=1.0;
			   		m_price_ed_izm_id=null; 
			   		m_price_edIzm=null;
			   	}
			   	cursorPrice.close();
			   	if (g.Common.FACTORY&&!m_agreement30_id.isEmpty())
				{
					// Прайс в соглашении
                    // если задано, заменяет существующую цену
                    cursorPrice=getContentResolver().query(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, new String[]{"ed_izm_id", "edIzm", "price", "k"}, "agreement30_id=? and nomenclature_id=?", new String[]{m_agreement30_id.toString(), m_nomenclature_id.toString()}, null);
                    price=0.0;
                    if (cursorPrice.moveToFirst())
                    {
                        int priceIndex=cursorPrice.getColumnIndex("price");
                        int kIndex=cursorPrice.getColumnIndex("k");
                        int edIzmIdIndex=cursorPrice.getColumnIndex("ed_izm_id");
                        int edIzmIndex=cursorPrice.getColumnIndex("edIzm");
                        price=cursorPrice.getDouble(priceIndex);
                        // TODO выяснить, действует ли скидка на прайс в соглашении
                        double priceProcent=g.MyDatabase.m_order_editing.stuff_discount_procent;
                        if (priceProcent!=0.0)
                        {
                            price+=Math.floor(price*priceProcent+0.00001)/100.0;
                        }
                        m_price=price;
                        m_price_k=cursorPrice.getDouble(kIndex);
                        if (m_price_k<0.0001)
                            m_price_k=1.0;
                        m_price_ed_izm_id=cursorPrice.getString(edIzmIdIndex);
                        m_price_edIzm=cursorPrice.getString(edIzmIndex);
                    }
                    cursorPrice.close();
				}
			} else
			{
			   	Cursor cursorPrice=getContentResolver().query(MTradeContentProvider.PRICES_CONTENT_URI, new String[]{"ed_izm_id", "edIzm", "price", "priceProcent", "k"}, "nomenclature_id=? and price_type_id=?", new String[]{m_nomenclature_id.toString(), m_price_type_id.toString()}, null);
			   	double price=0.0;
			   	if (cursorPrice.moveToFirst())
			   	{
			   		int priceIndex=cursorPrice.getColumnIndex("price");
			   		int kIndex=cursorPrice.getColumnIndex("k");
			   		int edIzmIdIndex=cursorPrice.getColumnIndex("ed_izm_id");
			   		int edIzmIndex=cursorPrice.getColumnIndex("edIzm");
	        		//int price_add_index=cursorPrice.getColumnIndex("priceAdd");
	        		//int price_procent_index=cursorPrice.getColumnIndex("priceProcent");
			   		price=cursorPrice.getDouble(priceIndex);
	    			//double priceAdd=cursorPrice.getDouble(price_add_index);
	    			//double priceProcent=cursorPrice.getDouble(price_procent_index);
					ClientPriceRecord clientPrice=TextDatabase.GetClientPrice(getContentResolver(), g.MyDatabase.m_order_editing.client_id, m_nomenclature_id);
					if (clientPrice!=null)
					{
						price+=Math.floor(price*clientPrice.priceProcent+0.00001)/100.0+clientPrice.priceAdd;
					}
					/*
	    			if (priceProcent!=0.0||priceAdd!=0)
	    			{
	    				price+=Math.floor(price*priceProcent+0.00001)/100.0+priceAdd;
	    			}
	    			*/
					m_price=price;
					m_price_k=cursorPrice.getDouble(kIndex);
					if (m_price_k<0.0001)
						m_price_k=1.0;
					m_price_ed_izm_id=cursorPrice.getString(edIzmIdIndex);
					m_price_edIzm=cursorPrice.getString(edIzmIndex);
			   	} else
			   	{
			   		m_price=0.0;
			   		m_price_k=1.0;
			   		m_price_ed_izm_id=null; 
			   		m_price_edIzm=null;
			   	}
			   	cursorPrice.close();
			}

			if (g.Common.PRODLIDER||g.Common.FACTORY||g.Common.TITAN)
			{
				int imageFileIndex=cursorNomenclature.getColumnIndex("image_file");
				m_image_file=cursorNomenclature.getString(imageFileIndex);
			} else
			{
				m_image_file="";
			}
	   	}
	   	cursorNomenclature.close();
	   	
	   	//Bundle bundle=intent.getBundleExtra("extrasBundle");
	   	
		m_etQuantity=(EditText)findViewById(R.id.etQuantity);
		m_etQuantity.setText("0");

		TextView tvRest=(TextView)findViewById(R.id.tvQuantityRest);
		TextView tvMinQuantity=(TextView)findViewById(R.id.tvQuantityMinQuantity);
		TextView tvMultiplicity=(TextView)findViewById(R.id.tvMultiplicity);
		TextView tvAdvanced1=(TextView)findViewById(R.id.tvAdvanced1);
		TextView tvAdvanced2=(TextView)findViewById(R.id.tvAdvanced2);

		if (m_rest>0)
		{
			tvRest.setVisibility(View.VISIBLE);
    		if (m_rest>Constants.MAX_QUANTITY)
    		{
    			tvRest.setText("Много");
    		} else
    		{
    			tvRest.setText(getResources().getString(R.string.label_rests)+" "+Common.DoubleToStringFormat(m_rest, "%.3f")+" "+m_nomenclature_quant[0]);
    		}
			
		} else
		{
			tvRest.setVisibility(View.GONE);
		}
		if (m_min_quantity>0)
		{
			tvMinQuantity.setVisibility(View.VISIBLE);
			tvMinQuantity.setText(getResources().getString(R.string.label_min_quantity)+" "+Common.DoubleToStringFormat(m_min_quantity, "%.3f")+" "+m_nomenclature_quant[0]);
		} else
		{
			tvMinQuantity.setVisibility(View.GONE);
		}
		if (m_multiplicity>0)
		{
			tvMultiplicity.setVisibility(View.VISIBLE);
			tvMultiplicity.setText(getResources().getString(R.string.label_multiplicity)+" "+Common.DoubleToStringFormat(m_multiplicity, "%.3f"));
		} else
		{
			tvMultiplicity.setVisibility(View.GONE);
		}
		
		updatePrice();
		updateSum();
		
		// Упаковки
		final ToggleButton buttonPacks=(ToggleButton)findViewById(R.id.toggleButtonPacks);
		buttonPacks.setChecked(m_bPacks);
		buttonPacks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bPacks=((ToggleButton)v).isChecked();
				m_etQuantity.setText("0");
				updateSum();
			}
		});

		//if (m_nomenclature_weight_k[m_bPacks?1:0]>1.001) {
		if (g.Common.PRAIT && m_nomenclature_weight_k[m_bPacks?1:0]>0.005) {
			tvAdvanced1.setVisibility(View.VISIBLE);
			tvAdvanced1.setText(getString(R.string.Weight) + " " + Common.DoubleToStringFormat(m_nomenclature_weight_k[m_bPacks ? 1 : 0], "%.3f"));
			tvAdvanced2.setVisibility(View.VISIBLE);
			// Возможно, будет неправильно для упаковок. Например, надо разделить на коэффициент как-то по-другому, но у Прайма только основная единица все равно
			tvAdvanced2.setText(getString(R.string.Price_kg) + " " + Common.DoubleToStringFormat(m_price / m_nomenclature_weight_k[m_bPacks ? 1 : 0], "%.3f"));
		} else
		{
			tvAdvanced1.setVisibility(View.GONE);
			tvAdvanced2.setVisibility(View.GONE);
		}
		
	    // Кнопка OK
		final Button buttonOk=(Button)findViewById(R.id.btnQuantityOk);
		buttonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				acceptData(true);
			}
		});
		final Button buttonRightOk=(Button)findViewById(R.id.btnQuantityRightOk);
		buttonRightOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				acceptData(true);
			}
		});
		
	    // Кнопка Cancel
		final Button buttonClose=(Button)findViewById(R.id.btnQuantityCancel);
		buttonClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	    // Кнопки выбора времени
	    Button buttonQuantitySelectTime=(Button)findViewById(R.id.buttonQuantitySelectTime);
	    buttonQuantitySelectTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_TIME);
			}
		});
	    
	    if (m_salesAdapter!=null)
	    {
			LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
	    }

		final ImageView imageView=(ImageView)findViewById(R.id.imageViewQuantity);
        imageView.setVisibility(View.GONE);

		String id_imageFile=m_image_file;
		if (id_imageFile!=null&&!id_imageFile.isEmpty())
		{
			// если имя файла указано без расширения (так было давно), то добавляем расширение по умолчанию
			String imageFileName=(id_imageFile.indexOf(".")<0)?Common.idToFileName(id_imageFile)+".jpg":id_imageFile;
			File imagesPath=Common.getMyStorageFileDir(QuantityActivity.this, "goods");
			/*
			if (android.os.Build.VERSION.SDK_INT< Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				// получаем путь к изображениям на SD
				imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
			} else
			{
				imagesPath=new File(Environment.getDataDirectory(), "/data/"+ getPackageName()+"/temp");
			}
			 */
			File imageFile=new File(imagesPath, imageFileName);
			if (imageFile.exists())
			{
				// Вариант 1
				//ImageLoader.getInstance().displayImage(Uri.fromFile(imageFile).toString(), imageView);
				//imageView.setVisibility(View.VISIBLE);
				// Вариант 2
				Picasso.get()
						.load(imageFile)
						.into(imageView, new Callback() {
							@Override
							public void onSuccess() {
								imageView.setVisibility(View.VISIBLE);
							}

							@Override
							public void onError(Exception e) {

							}
						});
			}
		}

	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quantity, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.action_settings:
			Intent intent=new Intent(QuantityActivity.this, PrefQuantityActivity.class);
			//if (m_preferences_from_ini)
			//	intent.putExtra("readOnly", true);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
		protected Dialog onCreateDialog(int id) {
			switch (id) {
			case IDD_QUERY_ZERO_QUANTITY_FROM_NOMENCLATURE:
			{
				MySingleton g = MySingleton.getInstance();
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.quantity_not_set));
				if (g.Common.ISTART)
				{
					builder.setMessage(getResources().getString(R.string.ask_remove_nomenclature_from_order));
				} else
				{
					builder.setMessage(getResources().getString(R.string.ask_close_without_changing));
				}
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MySingleton g = MySingleton.getInstance();
						if (g.Common.ISTART) {
							Intent intent = new Intent();
							// 24.10.2019
							intent.putExtra("_id", m_id);
							//
							intent.putExtra("id", m_nomenclature_id.toString());
							setResult(QUANTITY_RESULT_DELETE_LINE, intent);
						}
						// ничего не меняем
						finish();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_QUERY_ZERO_QUANTITY_FROM_ORDER:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.quantity_not_set));
				builder.setMessage(getResources().getString(R.string.ask_remove_nomenclature_from_order));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// удаляем текущую строку с товаром
						Intent intent=new Intent();
						// 24.10.2019
						intent.putExtra("_id", m_id);
						//
						intent.putExtra("id", m_nomenclature_id.toString());
						setResult(QUANTITY_RESULT_DELETE_LINE, intent);
						finish();
					}
				});
				builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						finish();
					}
				});
				
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_TIME:
			{
				int hourOfDay=0;
				int minute=0;
				if (m_shipping_time.length()==4)
				{
					hourOfDay=Integer.parseInt(m_shipping_time.substring(0,2));
					minute=Integer.parseInt(m_shipping_time.substring(2,4));
				}
			    // set date picker as current date
				return new TimePickerDialog(this, timePickerListener, 
	                         hourOfDay, minute, true);
			}
			}
			return null;
	  }
			@Override
			protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
				super.onPrepareDialog(id, dialog, args);
				switch (id)
				{
				case IDD_TIME:
				{
					int hourOfDay=0;
					int minute=0;
					if (m_shipping_time.length()==4)
					{
						hourOfDay=Integer.parseInt(m_shipping_time.substring(0,2));
						minute=Integer.parseInt(m_shipping_time.substring(2,4));
						((TimePickerDialog)dialog).updateTime(hourOfDay, minute);
					}
					break;
				}
				}
			}

	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		
		@Override
		public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
			
			EditText etQuantityTime = (EditText)findViewById(R.id.etQuantityTime);
			
			StringBuilder sb=new StringBuilder();
			m_shipping_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
			sb.append(m_shipping_time.substring(0,2)+":"+m_shipping_time.substring(2,4));
			etQuantityTime.setText(sb.toString());
			
		}
	};
	
	@ Override
	protected void onResume() {
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		String button_place=sharedPreferences.getString("quantity_button_place", "BOTTOM");
		
		final View buttonsBottom=(View)findViewById(R.id.layoutQuantityButtons);
		final Button buttonRightOk=(Button)findViewById(R.id.btnQuantityRightOk);		
		if (button_place.equals("RIGHT"))
		{
			buttonsBottom.setVisibility(View.INVISIBLE);
			buttonRightOk.setVisibility(View.VISIBLE);
		} else
		{
			buttonsBottom.setVisibility(View.VISIBLE);
			buttonRightOk.setVisibility(View.INVISIBLE);
		}
		super.onResume();
	}
	
    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
    	
        //listViewQuantitySalesHistory.setAdapter(m_salesAdapter);
        //Cursor salesLines=mContext.getContentResolver().query(MTradeContentProvider.SALES_LOADED_CONTENT_URI, null, "nomenclature_id=? and client_id=?", new String[]{nomenclatureId, clientId}, "datedoc DESC");
    	MySingleton g=MySingleton.getInstance();
    	
    	String clientId;
    	MyID distrPointId=new MyID();
    	if (m_mode.equals("REFUND"))
    	{
    		clientId=g.MyDatabase.m_refund_editing.client_id.toString();
			distrPointId=g.MyDatabase.m_refund_editing.distr_point_id;
    	} else
    	{
    		clientId=g.MyDatabase.m_order_editing.client_id.toString();
			distrPointId=g.MyDatabase.m_order_editing.distr_point_id;
    	}
    	//return new CursorLoader(this, MTradeContentProvider.SALES_LOADED_WITH_COMMON_GROUPS_CONTENT_URI,
	    //        PROJECTION, "(nomenclature_id=? or nomenclature_id=?) and client_id=?", new String[]{m_nomenclature_id.toString(), m_nomenclature_parent_id.toString(), clientId}, "datedoc DESC");

		//if (distrPointId.isEmpty()) {
    		// Очевидно, торговые точки в конфигурации не используются
			// либо ее не указали, но там не важно, что будет выводиться
			return new CursorLoader(this, MTradeContentProvider.SALES_L2_CONTENT_URI,
					PROJECTION, "client_id=? and (nomenclature_id=? or nomenclature_id=?)", new String[]{clientId, m_nomenclature_id.toString(), m_nomenclature_parent_id.toString()}, "datedoc DESC");
		//} else
		//{
		//	return new CursorLoader(this, MTradeContentProvider.SALES_L2_CONTENT_URI,
		//			PROJECTION, "client_id=? and distr_point_id=? and (nomenclature_id=? or nomenclature_id=?)", new String[]{clientId, distrPointId.toString(), m_nomenclature_id.toString(), m_nomenclature_parent_id.toString()}, "datedoc DESC");
		//}
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
            m_salesAdapter.swapCursor(data);
            break;
        }
        // The listview now displays the queried data.
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    	m_salesAdapter.swapCursor(null);
    }
	
	

}
