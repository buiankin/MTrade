package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.Random;

import net.thepaksoft.fdtrainer.NestedListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderPlaceRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

public class OrderPageFragment extends Fragment {
	// Номера менять нельзя. В другом месте та же нумерация. В частности, цифра 6 точно используется
	//static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    static final int SELECT_CLIENT_FROM_ORDER_REQUEST = 2;
    static final int SELECT_AGREEMENT_FROM_ORDER_REQUEST = 3;
	static final int OPEN_NOMENCLATURE_FROM_ORDER_REQUEST = 4;
    static final int SELECT_TRADE_POINT_FROM_ORDER_REQUEST = 5;
	//static final int QUANTITY_REQUEST = 6;
    static final int SELECT_DISCOUNT_FROM_ORDER_REQUEST = 7;
    static final int SELECT_PLACES_FROM_ORDER_REQUEST = 8;
    static final int OPEN_NOMENCLATURE_GRID_FROM_ORDER_REQUEST = 9;
	static final int SELECT_AGREEMENT30_FROM_ORDER_REQUEST = 10;

	static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

	  int pageNumber;
	  int backColor;

	  String m_distr_point_id_only; // только эта торговая точка может быть выбрана в документе (создание из маршрута)

	  
    View m_view=null;
	EquipmentRestsFragment myFrag1=null;
	EquipmentRestsFragment myFrag2=null;
    
    //SimpleAdapter sAdapter;
    //ArrayList<Map<String, Object>> data;
    
	// сюда записывается номер строки, когда начинают редактировать количество в строке
	int m_order_editing_line_num;
	
	boolean bHeaderPage=false;
	boolean bLinesPage=false;
	boolean bSettingsPage=false;
	
	static boolean bPrintOnce=false;

	/*
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
	@Override
	// when dialog box is closed, below method will be called.
	public void onDateSet(DatePicker view, int selectedYear,
			int selectedMonth, int selectedDay) {
		
		MySingleton g=MySingleton.getInstance();

	    OrderRecord rec=g.MyDatabase.m_order_editing;

		int day=Integer.parseInt(rec.datedoc.substring(6,8));
		int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
		int year=Integer.parseInt(rec.datedoc.substring(0,4));
		
		if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
		{
			rec.datedoc=android.text.format.DateFormat.format("yyyyMMddkkmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay)).toString();
	
			View view0=m_view;
			EditText etDate = (EditText) view0.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(rec.datedoc.substring(6,8)+"."+rec.datedoc.substring(4,6)+"."+rec.datedoc.substring(0,4));
			etDate.setText(sb.toString());
			
			setModified();
		}
		
	}
	};

    private DatePickerDialog.OnDateSetListener datePickerListenerShippingDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

            MySingleton g=MySingleton.getInstance();

            OrderRecord rec=g.MyDatabase.m_order_editing;

            int day=Integer.parseInt(rec.shipping_date.substring(6,8));
            int month=Integer.parseInt(rec.shipping_date.substring(4,6))-1;
            int year=Integer.parseInt(rec.shipping_date.substring(0,4));

            if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
            {
                rec.shipping_date=android.text.format.DateFormat.format("yyyyMMddkkmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay)).toString();

                View view0=m_view;
                EditText etOrderDate = (EditText) view0.findViewById(R.id.etOrderDate);
                StringBuilder sb=new StringBuilder();
                sb.append(rec.shipping_date.substring(6,8)+"."+rec.shipping_date.substring(4,6)+"."+rec.shipping_date.substring(0,4));
                etOrderDate.setText(sb.toString());

                setModified();
            }

        }
    };

	
    private TimePickerDialog.OnTimeSetListener timePickerListenerBeginTime = new TimePickerDialog.OnTimeSetListener() {
		
		@Override
		public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
			
			MySingleton g=MySingleton.getInstance();
			
			OrderRecord rec=g.MyDatabase.m_order_editing;
		    
			int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
			int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
			if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
			{
				
				View view0=m_view;
				
				StringBuilder sb=new StringBuilder();
				EditText etOrderTime = (EditText) view0.findViewById(R.id.etOrderTime);
				EditText etTime = (EditText) view0.findViewById(R.id.etShippingBeginTime);
				rec.shipping_begin_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
				sb.append(rec.shipping_begin_time.substring(0,2)+":"+rec.shipping_begin_time.substring(2,4));
				etOrderTime.setText(sb.toString());
				etTime.setText(sb.toString());
				
				setModified();
			}
		}
	};
	
    private TimePickerDialog.OnTimeSetListener timePickerListenerEndTime = new TimePickerDialog.OnTimeSetListener() {
		
		@Override
		public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
			
			MySingleton g=MySingleton.getInstance();
			
			OrderRecord rec=g.MyDatabase.m_order_editing;
		    
			int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
			int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
			if (hourOfDay!=selectedHourOfDay||minute!=selectedMinute)
			{
				
				View view0=m_view;
				
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

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		MySingleton g=MySingleton.getInstance();
		
	    if (data == null) 
	    	return;
		
		switch (requestCode)
		{
		case SELECT_CLIENT_FROM_ORDER_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
		    long _id=data.getLongExtra("id", 0);
			if (someEventListener.onClientSelected(view0, _id))
			{
				setModified();
				if (g.Common.FACTORY) {
                    // Проверим, есть ли данное соглашение у этого контрагента
                    boolean bWrongAgreement30 = true;
                    Cursor agreements30Cursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, new String[]{"id"}, "id=? and owner_id=?", new String[]{g.MyDatabase.m_order_editing.agreement30_id.toString(), g.MyDatabase.m_order_editing.client_id.toString()}, null);
                    if (agreements30Cursor.moveToNext())
                    {
                        bWrongAgreement30=false;
                    }
                    agreements30Cursor.close();
                    if (bWrongAgreement30)
                    {
                        // Не принадлежит, установим соглашение по умолчанию, если оно единственное
                        Cursor defaultAgreement30Cursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
                        if (defaultAgreement30Cursor.moveToNext())
                        {
                            int _idIndex = defaultAgreement30Cursor.getColumnIndex("_id");
                            Long _idAgreement = defaultAgreement30Cursor.getLong(_idIndex);
                            if (!defaultAgreement30Cursor.moveToNext())
                            {
                                // Есть единственное соглашение
                                someEventListener.onAgreement30Selected(view0, _idAgreement);
                            } else
                            {
                                // Есть несколько договоров
                                someEventListener.onAgreement30Selected(view0, 0);
                            }
                        } else
                        {
                            // Договора нет
                            someEventListener.onAgreement30Selected(view0, 0);
                        }
                        defaultAgreement30Cursor.close();
                    }
                }

				boolean bWrongAgreement=true;
				// Проверим, принадлежит ли договор текущему контрагенту 
		        Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id", "organization_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
		        if (agreementsCursor.moveToNext())
		        {
		        	int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
                    int organization_idIndex = agreementsCursor.getColumnIndex("organization_id"); // проверка организации актуальна только для FACTORY
		        	String owner_id = agreementsCursor.getString(owner_idIndex);
                    String organization_id = agreementsCursor.getString(organization_idIndex);
                    // Для FACTORY сначала проверится соответствие организации
                    if (!g.Common.FACTORY||g.MyDatabase.m_order_editing.organization_id.toString().equals(organization_id)) {
                        // Для всех проверится соответствие контрагента в договоре
                        if (g.MyDatabase.m_order_editing.client_id.toString().equals(owner_id))
                            bWrongAgreement=false;
                    }
		        }
		        agreementsCursor.close();
		        if (bWrongAgreement)
		        {
	        		// Не принадлежит, установим договор по умолчанию, если он единственный
	    	        Cursor defaultAgreementCursor;
	    	        if (g.Common.FACTORY) {
                        defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=? and organization_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString(), g.MyDatabase.m_order_editing.organization_id.toString()}, null);
                    } else
                    {
                        defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
                    }
	    	        if (defaultAgreementCursor.moveToNext())
	    	        {
	    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
	    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
	    	        	if (!defaultAgreementCursor.moveToNext())
	    	        	{
	    	        		// Есть единственный договор
	    	        		someEventListener.onAgreementSelected(view0, _idAgreement);
	    	        	} else
	    	        	{
	    	        		// Есть несколько договоров
	    	        		someEventListener.onAgreementSelected(view0, 0);
	    	        	}
	    	        } else
	    	        {
	    	        	// Договора нет
	    	        	someEventListener.onAgreementSelected(view0, 0);
	    	        }
                    defaultAgreementCursor.close();
		        }
			}
			break;
		}
        case SELECT_AGREEMENT30_FROM_ORDER_REQUEST:
        if (bHeaderPage)
        {
            View view0=m_view;
            //EditText et=(EditText)view0.findViewById(R.id.etAgreement);
            //TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);

            long _id=data.getLongExtra("id", 0);
            if (someEventListener.onAgreement30Selected(view0, _id))
            {
                setModified();
            }
            break;
        }
		case SELECT_AGREEMENT_FROM_ORDER_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
			//EditText et=(EditText)view0.findViewById(R.id.etAgreement);
			//TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);

		    long _id=data.getLongExtra("id", 0);
		    if (someEventListener.onAgreementSelected(view0, _id))
		    {
		    	setModified();
		    }
			break;
		}
		case SELECT_TRADE_POINT_FROM_ORDER_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
			EditText et=(EditText)view0.findViewById(R.id.etTradePoint);
			
			setModified();
			
			MyID newPriceTypeId=new MyID();
			
		    long _id=data.getLongExtra("id", 0);
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, _id);
		    Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr", "address", "price_type_id"}, null, null, null);
		    if (cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	int idIndex = cursor.getColumnIndex("id");
		    	//int addressIndex = cursor.getColumnIndex("address");
		    	int priceTypeIdIndex = cursor.getColumnIndex("price_type_id");
		    	String descr = cursor.getString(descrIndex);
		    	String tradePointId = cursor.getString(idIndex);
		    	//String address = cursor.getString(addressIndex);
		    	String priceTypeId = cursor.getString(priceTypeIdIndex);
		    	et.setText(descr);
				g.MyDatabase.m_order_editing.distr_point_id.m_id=tradePointId;
				g.MyDatabase.m_order_editing.stuff_distr_point_name=descr;
				newPriceTypeId=new MyID(priceTypeId);
		    } else
		    {
		    	et.setText(getResources().getString(R.string.trade_point_not_set));
		    	g.MyDatabase.m_order_editing.distr_point_id.m_id="";
		    	g.MyDatabase.m_order_editing.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
		    }
		    cursor.close();
		    
			// Остатки оборудования у клинта
			EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)getFragmentManager().findFragmentByTag("fragmentEquipment");
			if (fragment1!=null)
				fragment1.myRestartLoader();
			EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)getFragmentManager().findFragmentByTag("fragmentEquipmentTare");
			if (fragment2!=null)
			{
				fragment2.myRestartLoader();
			}
			
			if (g.Common.PRODLIDER)
			{
				// Если в торговой точке нет типа цены, берем из договора (как раньше)
				if (newPriceTypeId.isEmpty())
				{
				    Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id", "price_type_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
				    int priceTypeIdIndex=agreementsCursor.getColumnIndex("price_type_id");
				    if (agreementsCursor.moveToFirst())
				    {
				    	newPriceTypeId=new MyID(agreementsCursor.getString(priceTypeIdIndex));
				    }
				    agreementsCursor.close();
				}
				if (!g.MyDatabase.m_order_editing.price_type_id.equals(newPriceTypeId))
				{
					someEventListener.onPriceTypeSelected(m_view, newPriceTypeId);
				}
			}
		    
			break;
		}
		case SELECT_DISCOUNT_FROM_ORDER_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
			EditText et=(EditText)view0.findViewById(R.id.editTextDiscount);
			
			setModified();
			
		    long _id=data.getLongExtra("id", 0);
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, _id);
		    Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr", "priceProcent"}, null, null, null);
		    if (cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	int idIndex = cursor.getColumnIndex("id");
		    	int priceProcentIndex = cursor.getColumnIndex("priceProcent");
		    	String descr = cursor.getString(descrIndex);
		    	String discountId = cursor.getString(idIndex);
		    	double priceProcent = cursor.getDouble(priceProcentIndex);
		    	et.setText(descr);
				g.MyDatabase.m_order_editing.simple_discount_id=new MyID(discountId);
				g.MyDatabase.m_order_editing.stuff_discount_procent=priceProcent;
				g.MyDatabase.m_order_editing.stuff_discount=descr;
		    } else
		    {
		    	et.setText(getResources().getString(R.string.trade_point_not_set));
		    	g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
		    	g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;
		    	g.MyDatabase.m_order_editing.stuff_discount=getResources().getString(R.string.trade_point_not_set);
		    }
		    cursor.close();
		    someEventListener.someEvent("recalc_price");
		    redrawTicketPrice();
	    	redrawSumWeight();
	    	
			if (g.Common.NEW_BACKUP_FORMAT)
			{
				TextDatabase.SaveOrderSQL(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing_id==0?1:2);
			}
		    
			break;
		}
		case OPEN_NOMENCLATURE_FROM_ORDER_REQUEST:
		case OPEN_NOMENCLATURE_GRID_FROM_ORDER_REQUEST:
		if (bLinesPage)
		{
			if (resultCode==NomenclatureActivity.NOMENCLATURE_RESULT_DOCUMENT_CHANGED||resultCode==NomenclatureGridActivity.NOMENCLATURE_RESULT_DOCUMENT_CHANGED)
			{
				g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
				if (g.Common.PHARAON)
				{
					ExpandableListView lvSimple = (ExpandableListView) m_view.findViewById(R.id.listViewOrderLines);
			        // раскроем список
					int count = ((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).getGroupCount();
					for (int position = 0; position < count; position++)
					{
			    		if (((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(position).need_expand)
			    		{
			    			((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(position).need_expand=true;
							lvSimple.expandGroup(position);
			    		}
					}
				}
				setModified();
				recalcSumWeight();
				redrawSumWeight();
				if (g.Common.NEW_BACKUP_FORMAT)
				{
					// 11.06.2018 убрал, т.к. строки документа записываются в процессе редактирования
					//TextDatabase.SaveOrderSQL(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing_id==0?1:2);
				}
			}
			break;
		}
		case SELECT_PLACES_FROM_ORDER_REQUEST:
		{
	  		if (resultCode==PlacesActivity.PLACES_RESULT_OK)
			if (bHeaderPage)
	  		{
	  			ArrayList<String> listId=data.getStringArrayListExtra("placesIndices");
	  			ArrayList<String> listDescr=data.getStringArrayListExtra("placesDescr");
	  			
	  			// один раз было null, причина не понятна
	  			if (listId!=null)
	  			{
		  			setModified();
				    //long _id=data.getLongExtra("_id", 0);
		  			g.MyDatabase.m_order_editing.shipping_begin_time=data.getStringExtra("order_time");
		  			g.MyDatabase.m_order_editing.shipping_date=data.getStringExtra("order_date");
		  			g.MyDatabase.m_order_editing.stuff_places=data.getStringExtra("places");
		  			
		  			g.MyDatabase.m_order_editing.places.clear();
	  				
		  			int idx=0;
		  			for (String placeId: listId)
		  			{
		  				OrderPlaceRecord place=new OrderPlaceRecord();
		  				place.place_id=new MyID(placeId);
		  				place.stuff_descr=listDescr.get(idx);
		  				g.MyDatabase.m_order_editing.places.add(place);
		  				idx=idx+1;
		  			}
					EditText etOrderTime=(EditText)m_view.findViewById(R.id.etOrderTime);
					if (g.MyDatabase.m_order_editing.shipping_begin_time.isEmpty())
					{
						etOrderTime.setText("");
					} else
					{
						etOrderTime.setText(g.MyDatabase.m_order_editing.shipping_begin_time.substring(0,2)+":"+g.MyDatabase.m_order_editing.shipping_begin_time.substring(2,4));
					}
					EditText etOrderDate=(EditText)m_view.findViewById(R.id.etOrderDate);
					if (g.MyDatabase.m_order_editing.shipping_date.isEmpty())
					{
						etOrderDate.setText("");
					} else
					{
						etOrderDate.setText(Common.dateStringAsText(g.MyDatabase.m_order_editing.shipping_date));
					}
					
					final EditText editTextPlace=(EditText)m_view.findViewById(R.id.editTextPlace);
					editTextPlace.setText(g.MyDatabase.m_order_editing.stuff_places);
	  			}
	  		}
			break;
		}
		case OrderActivity.QUANTITY_REQUEST:
		if (bLinesPage)
		{
			// это обработчик при изменении количества, при вводе количества - в списке номенклатуры

			// TODO проверить что при просто вводе количества из подбора это не открывается
			
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
				    String shipping_time=data.getStringExtra("shipping_time");
				    String comment_in_line=data.getStringExtra("comment_in_line");
	      			
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
	      				nomenclatureCursor=getActivity().getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"descr", "flags"}, "id=?", new String[]{nomenclature_id.toString()}, null);
	      			} else
	      			{
					    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
						nomenclatureCursor=getActivity().getContentResolver().query(singleUri, new String[]{"descr", "flags"}, null, null, null);
	      			}
					if (nomenclatureCursor.moveToFirst())
					{
						// nomenclature_id не меняем, он правильный
				    	int descrIndex = nomenclatureCursor.getColumnIndex("descr");
				    	int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
						line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
						line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
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
						line.shipping_time=shipping_time;
						line.comment_in_line=comment_in_line;
						if (line.lineno==0)
						{
							line.lineno=g.MyDatabase.m_order_editing.getMaxLineno()+1;
						}
						
						g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
						setModified();
						recalcSumWeight();
						redrawSumWeight();
						if (g.Common.NEW_BACKUP_FORMAT)
						{
							TextDatabase.UpdateOrderLine(getActivity().getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);							
						}/* else
						if (g.MyDatabase.m_order_editing.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
						{
							ContentValues cv=new ContentValues();
							cv.put("client_id", ""); // в данном случае это не важно
							cv.put("quantity", quantity);
							cv.put("quantity_requested", quantity);
							cv.put("k", k);
							cv.put("ed", ed);
							cv.put("price", price);
							cv.put("shipping_time", shipping_time);
							cv.put("comment_in_line", comment_in_line);
							
							int cnt=getActivity().getContentResolver().update(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cv, "order_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_order_editing_id), nomenclature_id.toString()});
						}*/
					}
	      		}
	  		}
	  		if (resultCode==QuantityActivity.QUANTITY_RESULT_DELETE_LINE)
	  		{
	      		if (data!=null)
	      		{
				    MyID nomenclature_id=new MyID(data.getStringExtra("id"));
	      			OrderLineRecord line;
	      			if (m_order_editing_line_num<g.MyDatabase.m_order_editing.lines.size())
	      			{
	      				line=g.MyDatabase.m_order_editing.lines.get(m_order_editing_line_num);
	      				// на всякий случай проверка на совпадение номенклатуры
	      				if (line.nomenclature_id.equals(nomenclature_id))
	      				{
							if (g.Common.NEW_BACKUP_FORMAT)
							{
								setModified();
								TextDatabase.DeleteOrderLine(getActivity().getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
							}
    	      				g.MyDatabase.m_order_editing.lines.remove(line);
	      				}
	      			}
	      			g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
	      			setModified();
	      			recalcSumWeight();
	      			redrawSumWeight();
	      		}
	  		}
			break;
		}
		}
	}
	
	
	
	
	private void redrawTicketPrice()
	{
		/*
		if (bHeaderPage)
		{
			EditText editTextTicketM=(EditText)m_view.findViewById(R.id.editTextTicketM);
			EditText editTextTicketW=(EditText)m_view.findViewById(R.id.editTextTicketW);
			
			editTextTicketM.setText(Math.abs(g.MyDatabase.m_order_editing.ticket_m)>0.001?g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.ticket_m, "%.3f"):"");
			editTextTicketW.setText(Math.abs(g.MyDatabase.m_order_editing.ticket_w)>0.001?g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.ticket_w, "%.3f"):"");
		}
		*/
	}
	
	
		
		private void redrawSumWeight()
		{
			//MyDatabase.m_order_editing.sumDoc=MyDatabase.m_order_editing.GetOrderSum(null, false);
			//MyDatabase.m_order_editing.weightDoc=TextDatabase.GetOrderWeight(getActivity().getContentResolver(), MyDatabase.m_order_editing, null, false);
			
			MySingleton g=MySingleton.getInstance();
			
			View view1=m_view;
			if (bLinesPage)
			{
				TextView tvStatistics=(TextView)view1.findViewById(R.id.textViewStatistics);
				if (g.MyDatabase.m_order_editing.lines.size()==0)
				{
					tvStatistics.setText("0");
				} else
				{
					StringBuilder sb=new StringBuilder();
					sb.append(Integer.toString(g.MyDatabase.m_order_editing.lines.size()));
					sb.append(": ").append(String.format(g.Common.getCurrencyFormatted(getActivity()), Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.sumDoc, "%.3f")));
					sb.append(", ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
					tvStatistics.setText(sb.toString());
				}
			}
		}

      protected void setModified()
      {
    	  MySingleton g=MySingleton.getInstance();
    	  if (!g.MyDatabase.m_order_editing_modified)
    	  {
    		  getActivity().setTitle(R.string.title_activity_order_changed);
    		  g.MyDatabase.m_order_editing_modified=true;
    	  }
    	  if (g.Common.NEW_BACKUP_FORMAT&&g.MyDatabase.m_order_new_editing_id==0)
  		  {
    		  // Что-то поменяли в документе впервые, записываем документ
    		  g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
    		  // editing_backup (последний параметр)
    		  // 0-документ в нормальном состоянии
    		  // 1-новый документ, записать не успели
    		  // 2-документ начали редактировать, но не записали и не отменили изменения
    		  g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
  		  }
      }
      protected void recalcSumWeight()
      {
    	  MySingleton g=MySingleton.getInstance();
    	  g.MyDatabase.m_order_editing.sumDoc=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
    	  g.MyDatabase.m_order_editing.weightDoc=TextDatabase.GetOrderWeight(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, null, false);
      }

    public interface onSomeEventListener {
		    public void someEvent(String s);
		    public boolean onClientSelected(View view0, long _id);
		    public boolean onAgreement30Selected(View view0, long _id);
		    public boolean onAgreementSelected(View view0, long _id);
		    public boolean onPriceTypeSelected(View view0, MyID price_type_id);
		    //public boolean orderCanBeSaved(StringBuffer reason);
		  }
	    
		  onSomeEventListener someEventListener;
		  
		  @Override
		  public void onAttach(Activity activity) {
		    super.onAttach(activity);
		        try {
		          someEventListener = (onSomeEventListener) activity;
		        } catch (ClassCastException e) {
		            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
		        }
	  }	  
	  
	  static OrderPageFragment newInstance(int page, String distr_point_id_only) {
		OrderPageFragment pageFragment = new OrderPageFragment();
	    Bundle arguments = new Bundle();
	    arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
	    arguments.putString("distr_point_id_only", distr_point_id_only);
	    pageFragment.setArguments(arguments);
	    
	    return pageFragment;
	  }
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          // TODO Добавляено 21.03.2019
		  setHasOptionsMenu(true);
		  //
          pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
		  m_distr_point_id_only = getArguments().getString("distr_point_id_only");

          Random rnd = new Random();
          backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
	  }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {

		  boolean orientationLandscape=(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE);
		  
		  MySingleton g=MySingleton.getInstance();

		  bHeaderPage=false;
		  bLinesPage=false;
		  bSettingsPage=false;
		
		switch (pageNumber){
		case 0:
			if (orientationLandscape)
			{
				bHeaderPage=true;
				bLinesPage=true;
		    	m_view = inflater.inflate(R.layout.order_header_and_lines, null);
			} else
			{
				bHeaderPage=true;
		    	m_view = inflater.inflate(R.layout.order_header_fragment, null);
			}
		    	
	    	if (g.Common.PRODLIDER||g.Common.TANDEM||g.Common.TITAN)
	    	{
	    		FragmentManager fragMan = getFragmentManager();
	    		
				EquipmentRestsFragment myFrag = (EquipmentRestsFragment) fragMan.findFragmentByTag("fragmentEquipment");
				if (myFrag==null)
				{
		    		FragmentTransaction fragTransaction = fragMan.beginTransaction();
		    		myFrag = EquipmentRestsFragment.newInstance(getString(R.string.label_equipment), EquipmentRestsFragment.EQUIPMENT_RESTS_LOADER_ID);
		    		fragTransaction.add(R.id.linearLayoutOrderHeaderDetails, myFrag, "fragmentEquipment");
		    		fragTransaction.commitAllowingStateLoss();
				}
				myFrag1=myFrag;
	    	}
	    	if (g.Common.TANDEM||g.Common.TITAN)
	    	{
	    		FragmentManager fragMan = getFragmentManager();
	    		
				EquipmentRestsFragment myFrag = (EquipmentRestsFragment) fragMan.findFragmentByTag("fragmentEquipmentTare");
				if (myFrag==null)
				{
		    		FragmentTransaction fragTransaction = fragMan.beginTransaction();
		    		myFrag = EquipmentRestsFragment.newInstance(getString(R.string.label_tare), EquipmentRestsFragment.EQUIPMENT_RESTS_TARE_LOADER_ID);
		    		fragTransaction.add(R.id.linearLayoutOrderHeaderDetails, myFrag, "fragmentEquipmentTare");
		    		fragTransaction.commitAllowingStateLoss();
				}
				myFrag2=myFrag;
	    	}
			break;
		case 1:
			if (orientationLandscape)
			{
				bSettingsPage=true;
				m_view = inflater.inflate(R.layout.order_advanced_fragment, null);
			} else
			{
				bLinesPage=true;
				m_view = inflater.inflate(R.layout.order_lines_fragment, null);
			}
			break;
		case 2:
			bSettingsPage=true;
			m_view = inflater.inflate(R.layout.order_advanced_fragment, null);
			break;
			/*
		case 3: // на самом деле это страница 0, но ландшафт
			bHeaderPage=true;
			bLinesPage=true;
	    	m_view = inflater.inflate(R.layout.order_header_and_lines, null);
			break;
			*/
		default:
	    	m_view = inflater.inflate(R.layout.page_fragment, null);
			TextView tvPage = (TextView) m_view.findViewById(R.id.tvPage);
			tvPage.setText("Page " + pageNumber);
			tvPage.setBackgroundColor(backColor);
		}
		  
		if (bHeaderPage)
	    {
	    	// Заголовок заказа
	    	int organizationVisibility=View.VISIBLE;
	    	// Видимость организации
	    	if (g.MyDatabase.m_organizations_version==0||g.Common.MEGA)
	    	{
	    		organizationVisibility=View.GONE;
	    	}
			TextView textViewOrdersAgreement30=(TextView)m_view.findViewById(R.id.textViewOrdersAgreement30);
			TextView textViewOrdersOrganization30=(TextView)m_view.findViewById(R.id.textViewOrdersOrganization30);
			LinearLayout layoutAgreement30=(LinearLayout)m_view.findViewById(R.id.layoutAgreement30);
            LinearLayout layoutAgreement=(LinearLayout)m_view.findViewById(R.id.layoutAgreement);
			TextView tvOrganizationName = (TextView) m_view.findViewById(R.id.textViewOrdersOrganization);
			TextView tvOrganizationName30 = (TextView) m_view.findViewById(R.id.textViewOrdersOrganization30);

            OrderRecord rec=g.MyDatabase.m_order_editing;

	    	if (g.Common.FACTORY) {
	    		// Кроме договора используется также соглашение, и организация выводится в нем
				textViewOrdersAgreement30.setVisibility(View.VISIBLE);
				textViewOrdersOrganization30.setVisibility(View.VISIBLE);
				layoutAgreement30.setVisibility(View.VISIBLE);
                layoutAgreement.setVisibility(View.VISIBLE);
				tvOrganizationName30.setVisibility(organizationVisibility);
				tvOrganizationName.setVisibility(View.GONE);
                layoutAgreement.setVisibility(rec.stuff_agreement_required?View.VISIBLE:View.GONE);
			} else {
				textViewOrdersAgreement30.setVisibility(View.GONE);
				textViewOrdersOrganization30.setVisibility(View.GONE);
				layoutAgreement30.setVisibility(View.GONE);
				if (g.Common.MEGA||g.Common.PHARAON)
					layoutAgreement.setVisibility(View.GONE);
				else
					layoutAgreement.setVisibility(View.VISIBLE);
				tvOrganizationName30.setVisibility(View.GONE);
				tvOrganizationName.setVisibility(organizationVisibility);
			}
		    // Заполняем данные шапки
			// Номер документа
			EditText etNumber = (EditText) m_view.findViewById(R.id.etNumber);
			etNumber.setText(rec.numdoc);
			// Дата документа
			EditText etDate = (EditText) m_view.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(Common.dateStringAsText(rec.datedoc));
			etDate.setText(sb.toString());
			
			// Время обслуживания
			EditText etOrderTime=(EditText) m_view.findViewById(R.id.etOrderTime);
			// Дата отгрузки/доставки
			EditText etShippingBeginTime=(EditText) m_view.findViewById(R.id.etShippingBeginTime);
			if (rec.shipping_begin_time.isEmpty())
			{
				etShippingBeginTime.setText("");
				etOrderTime.setText("");
			} else
			{
				sb=new StringBuilder();
				sb.append(rec.shipping_begin_time.substring(0,2)+":"+rec.shipping_begin_time.substring(2,4));
				etShippingBeginTime.setText(sb.toString());
				etOrderTime.setText(sb.toString());
			}
			LinearLayout layoutOrderDatePh=(LinearLayout)m_view.findViewById(R.id.layoutOrderDatePh);
            EditText etShippingEndTime=(EditText) m_view.findViewById(R.id.etShippingEndTime);
			if (rec.shipping_end_time.isEmpty())
			{
				etShippingEndTime.setText("");
			} else
			{
				sb=new StringBuilder();
				sb.append(rec.shipping_end_time.substring(0,2)+":"+rec.shipping_end_time.substring(2,4));
				etShippingEndTime.setText(sb.toString());
			}
			// Дата обслуживания
			EditText etOrderDate=(EditText) m_view.findViewById(R.id.etOrderDate);
			if (rec.shipping_date.isEmpty())
			{
				etOrderDate.setText("");
			} else
			{
				sb=new StringBuilder();
				sb.append(Common.dateStringAsText(rec.shipping_date));
				etOrderDate.setText(sb.toString());
			}
			
			RadioGroup radioGroup=(RadioGroup) m_view.findViewById(R.id.radioGroupCreateSelectClient);
			if (g.Common.PHARAON)
			{
				if (g.MyDatabase.m_order_editing.create_client==1)
					radioGroup.check(R.id.radioOrderCreateClient);
				else
					radioGroup.check(R.id.radioOrderExistingClient);
				
				radioGroup.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						MySingleton g=MySingleton.getInstance();
						switch (checkedId)
						{
						case R.id.radioOrderCreateClient:
						    g.MyDatabase.m_order_editing.create_client=1;
						    break;
						case R.id.radioOrderExistingClient:
						    g.MyDatabase.m_order_editing.create_client=0;
						    break;
						}
						// TODO Auto-generated method stub
						updateClientsVisibility();
					}
				});
				
				etOrderTime.addTextChangedListener(new TextWatcher() {
					
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
						MySingleton g=MySingleton.getInstance();
						String newDate=g.Common.inputDateToString4(s.toString());
						if (!g.MyDatabase.m_order_editing.shipping_begin_time.equals(newDate))
						{
							setModified();
							g.MyDatabase.m_order_editing.shipping_begin_time=newDate;
						    if (g.Common.NEW_BACKUP_FORMAT)
						    {
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"shipping_begin_time"});
						    }
						}
					}
				});
				
				
				EditText etLastname=(EditText)m_view.findViewById(R.id.etLastname);
				etLastname.setText(rec.create_client_lastname);
				etLastname.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.create_client_lastname.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.create_client_lastname=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"create_client_lastname"});
						}
					}
				});
				EditText etFirstname=(EditText)m_view.findViewById(R.id.etFirstname);
				etFirstname.setText(rec.create_client_firstname);
				etFirstname.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.create_client_firstname.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.create_client_firstname=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"create_client_firstname"});
						}
					}
				});
				
				EditText etSurname=(EditText)m_view.findViewById(R.id.etSurname);
				etSurname.setText(rec.create_client_surname);
				etSurname.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.create_client_surname.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.create_client_surname=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"create_client_surname"});
						}
					}
				});
				
				EditText etPhone=(EditText)m_view.findViewById(R.id.etPhone);
				etPhone.setText(rec.phone_num);
				etPhone.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.phone_num.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.phone_num=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"phone_num"});
						}
					}
				});
				
				
			} else
			{
				radioGroup.setVisibility(View.GONE);
			}
			updateClientsVisibility();
			
			
			
			// Клиент
			EditText etClient = (EditText) m_view.findViewById(R.id.etClient);
			etClient.setText(rec.stuff_client_name);
			// и его адрес
			TextView tvClientAddress = (TextView) m_view.findViewById(R.id.textViewOrdersClientAddress);
			tvClientAddress.setText(rec.stuff_client_address);
			
			// Договор, организация договора
			TextView tvAgreement30 = (TextView)m_view.findViewById(R.id.textViewOrdersAgreement30);
			TextView tvAgreement = (TextView)m_view.findViewById(R.id.textViewOrdersAgreement);
			EditText etAgreement30 = (EditText) m_view.findViewById(R.id.etAgreement30);
			EditText etAgreement = (EditText) m_view.findViewById(R.id.etAgreement);
			etAgreement30.setText(rec.stuff_agreement30_name);
			etAgreement.setText(rec.stuff_agreement_name);
			tvOrganizationName30.setText(rec.stuff_organization_name);
			tvOrganizationName.setText(rec.stuff_organization_name);
			if (g.Common.MEGA||g.Common.PHARAON)
			{
				tvOrganizationName.setVisibility(View.GONE);
				tvAgreement.setVisibility(View.GONE);
			}
			
			// Цены, билеты
			TextView tvOrderTime=(TextView)m_view.findViewById(R.id.tvOrderTime);
			TextView textViewCardNum=(TextView)m_view.findViewById(R.id.textViewCardNum);
			TextView textViewPlace=(TextView)m_view.findViewById(R.id.textViewPlace);
			TextView textViewCount=(TextView)m_view.findViewById(R.id.textViewCount);
			final EditText etCardNum=(EditText)m_view.findViewById(R.id.etCardNum);
			//final Button buttonSelectOrderTime=(Button)m_view.findViewById(R.id.buttonOrderSelectOrderTime);
			final ImageButton buttonOrderSearchByCardNum=(ImageButton)m_view.findViewById(R.id.buttonOrderSearchByCardNum);
			final ImageButton buttonOrderSearchByPhone=(ImageButton)m_view.findViewById(R.id.buttonOrderSearchByPhone);
			final EditText editTextPlace=(EditText)m_view.findViewById(R.id.editTextPlace);
			final Button buttonOrderSelectPlaces=(Button)m_view.findViewById(R.id.buttonOrderSelectPlaces);
			final EditText editTextCount=(EditText)m_view.findViewById(R.id.editTextCount);
			TextView textViewClientPhone=(TextView)m_view.findViewById(R.id.textViewClientPhone);
			EditText etPhone=(EditText)m_view.findViewById(R.id.etPhone);
			
			TextView textViewKredit=(TextView)m_view.findViewById(R.id.textViewKredit);
			EditText editTextKredit=(EditText)m_view.findViewById(R.id.editTextKredit);

			if (g.Common.PHARAON)
			{
				etCardNum.setText(g.MyDatabase.m_order_editing.card_num!=0?String.valueOf(g.MyDatabase.m_order_editing.card_num):"");
				editTextPlace.setText(g.MyDatabase.m_order_editing.stuff_places);
				/*
				OnFocusChangeListener myOnFocusChangeListener= new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus)
						{
							String s=editTextPlace.getText().toString();
							if (!s.equals(s.replace("0", "")))
							{
								((EditText)v).setText("");
							}
						}
					}
				}; 
				
				editTextPlace.setOnFocusChangeListener(myOnFocusChangeListener);
				editTextCountM.setOnFocusChangeListener(myOnFocusChangeListener);
				editTextCountW.setOnFocusChangeListener(myOnFocusChangeListener);
				editTextTicketM.setOnFocusChangeListener(myOnFocusChangeListener);
				editTextTicketM.setOnFocusChangeListener(myOnFocusChangeListener);
				*/
				
				editTextCount.setText(g.MyDatabase.m_order_editing.quant_mw!=0?String.valueOf(g.MyDatabase.m_order_editing.quant_mw):"");
				//editTextCountM.setText(g.MyDatabase.m_order_editing.quant_m!=0?String.valueOf(g.MyDatabase.m_order_editing.quant_m):"");
				//editTextCountW.setText(g.MyDatabase.m_order_editing.quant_w!=0?String.valueOf(g.MyDatabase.m_order_editing.quant_w):"");
				//editTextTicketM.setText(Math.abs(g.MyDatabase.m_order_editing.ticket_m)>0.001?g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.ticket_m, "%.3f"):"");
				//editTextTicketW.setText(Math.abs(g.MyDatabase.m_order_editing.ticket_w)>0.001?g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.ticket_w, "%.3f"):"");
				
				editTextKredit.setText(Math.abs(g.MyDatabase.m_order_editing.pay_credit)>0.001?Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.pay_credit, "%.3f"):"");
				
				etCardNum.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						try
						{
							if (g.MyDatabase.m_order_editing.card_num!=Integer.parseInt(s.toString()))
							{
								setModified();
								g.MyDatabase.m_order_editing.card_num=Integer.parseInt(s.toString());
							    if (g.Common.NEW_BACKUP_FORMAT)
							    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"card_num"});
							}
						} catch (NumberFormatException e)
						{
							setModified();
							g.MyDatabase.m_order_editing.card_num=0;
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"card_num"});
						}
					}
				});
				
				/*
				editTextPlace.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						try
						{
							if (g.MyDatabase.m_order_editing.place_num!=Integer.parseInt(s.toString()))
							{
								setModified();
								MyDatabase.m_order_editing.place_num=Integer.parseInt(s.toString());
							}
						} catch (NumberFormatException e)
						{
							setModified();
							MyDatabase.m_order_editing.place_num=0;
						}
					}
				});
				*/
				
				buttonOrderSelectPlaces.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Intent intent=new Intent(getActivity(), PlacesActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent, SELECT_PLACES_FROM_ORDER_REQUEST);
					}
				});
				
				editTextCount.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						try
						{
							if (g.MyDatabase.m_order_editing.quant_mw!=Integer.parseInt(s.toString()))
							{
								setModified();
								g.MyDatabase.m_order_editing.quant_mw=Integer.parseInt(s.toString());
							    if (g.Common.NEW_BACKUP_FORMAT)
							    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"quant_mw"});
							}
						} catch (NumberFormatException e)
						{
							setModified();
							g.MyDatabase.m_order_editing.quant_mw=0;
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"quant_mw"});
						}
					}
				});
				/*
				editTextCountM.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						try
						{
							if (g.MyDatabase.m_order_editing.quant_m!=Integer.parseInt(s.toString()))
							{
								setModified();
								MyDatabase.m_order_editing.quant_m=Integer.parseInt(s.toString());
							}
						} catch (NumberFormatException e)
						{
							setModified();
							MyDatabase.m_order_editing.quant_m=0;
						}
					}
				});
				editTextCountW.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						try
						{
							if (g.MyDatabase.m_order_editing.quant_w!=Integer.parseInt(s.toString()))
							{
								setModified();
								MyDatabase.m_order_editing.quant_w=Integer.parseInt(s.toString());
							}
						} catch (NumberFormatException e)
						{
							setModified();
							MyDatabase.m_order_editing.quant_w=0;
						}
					}
				});

				//editTextTicketM.setText(g.Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.ticket_m, "%.3f"));
				
				editTextTicketM.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						try
						{
							if (Math.abs(g.MyDatabase.m_order_editing.ticket_m-Double.parseDouble(s.toString()))>0.0001)
							{
								setModified();
								MyDatabase.m_order_editing.ticket_m=Double.parseDouble(s.toString());
							}
						} catch (NumberFormatException e)
						{
							setModified();
							MyDatabase.m_order_editing.ticket_m=0.0;
						}
					}
				});
				
				editTextTicketW.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						try
						{
							if (Math.abs(g.MyDatabase.m_order_editing.ticket_w-Double.parseDouble(s.toString()))>0.0001)
							{
								setModified();
								MyDatabase.m_order_editing.ticket_w=Double.parseDouble(s.toString());
							}
						} catch (NumberFormatException e)
						{
							setModified();
							MyDatabase.m_order_editing.ticket_w=0.0;
						}
					}
				});
				*/
				
				editTextKredit.addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						try
						{
							if (Math.abs(g.MyDatabase.m_order_editing.pay_credit-Double.parseDouble(s.toString()))>0.0001)
							{
								setModified();
								g.MyDatabase.m_order_editing.pay_credit=Math.floor(Double.parseDouble(s.toString())*100.0+0.00001)/100.0;
							    if (g.Common.NEW_BACKUP_FORMAT)
							    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"pay_credit"});
							}
						} catch (NumberFormatException e)
						{
							setModified();
							g.MyDatabase.m_order_editing.pay_credit=0.0;
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"pay_credit"});
						}
					}
				});
				
			} else
			{
				if (g.Common.FACTORY)
                {
                    // Здесь мы используем только второе поле (дата) без времени
                    layoutOrderDatePh.setVisibility(View.VISIBLE);
                    tvOrderTime.setVisibility(View.INVISIBLE); // Место пусть занимает
                    etOrderTime.setVisibility(View.INVISIBLE);
                    TextView textViewOrderDate=(TextView) m_view.findViewById(R.id.textViewOrderDate);
                    textViewOrderDate.setText(R.string.label_shipping_date);
                } else
                {
                    tvOrderTime.setVisibility(View.GONE);
                    etOrderTime.setVisibility(View.GONE);
                    layoutOrderDatePh.setVisibility(View.GONE);
                }
				//buttonSelectOrderTime.setVisibility(View.GONE);
				textViewCardNum.setVisibility(View.GONE);
				etCardNum.setVisibility(View.GONE);
				buttonOrderSearchByCardNum.setVisibility(View.GONE);
				buttonOrderSearchByPhone.setVisibility(View.GONE);
				textViewPlace.setVisibility(View.GONE);
				editTextPlace.setVisibility(View.GONE);
				textViewCount.setVisibility(View.GONE);
				editTextCount.setVisibility(View.GONE);
				textViewClientPhone.setVisibility(View.GONE);
				etPhone.setVisibility(View.GONE);
				/*
				textViewCountM.setVisibility(View.GONE);
				textViewCountW.setVisibility(View.GONE);
				editTextCountM.setVisibility(View.GONE);
				editTextCountW.setVisibility(View.GONE);
				textViewTicketM.setVisibility(View.GONE);
				textViewTicketW.setVisibility(View.GONE);
				editTextTicketM.setVisibility(View.GONE);
				editTextTicketW.setVisibility(View.GONE);
				*/
				textViewKredit.setVisibility(View.GONE);
				editTextKredit.setVisibility(View.GONE);
			}
			
	    	// Скидка
			TextView tvDiscount = (TextView)m_view.findViewById(R.id.textViewDiscount);
			EditText etDiscount = (EditText)m_view.findViewById(R.id.editTextDiscount);
			Button buttonSelectDiscount = (Button)m_view.findViewById(R.id.buttonOrderSelectDiscount);
			
			if (g.Common.PHARAON)
			{
	    		etDiscount.setText(rec.stuff_discount);
			    // Кнопка выбора скидки
	    		buttonSelectDiscount.setVisibility(View.VISIBLE);
	    		buttonSelectDiscount.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent=new Intent(getActivity(), DiscountsActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(intent, SELECT_DISCOUNT_FROM_ORDER_REQUEST);
					}
				});
	    		
			} else
	    	if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
	    	{
	    		etDiscount.setText(rec.stuff_discount);
	    		buttonSelectDiscount.setVisibility(View.GONE);
	    		
	    	} else
	    	{
	    		tvDiscount.setVisibility(View.GONE);
	    		etDiscount.setVisibility(View.GONE);
	    		buttonSelectDiscount.setVisibility(View.GONE);
	    	}
			
			// Торговая точка
			EditText etTradePoint = (EditText) m_view.findViewById(R.id.etTradePoint);
			etTradePoint.setText(rec.stuff_distr_point_name);
			//if (g.Common.MEGA||g.Common.PHARAON||g.Common.TANDEM||g.Common.TITAN)
			if (!g.Common.isDataFormatWithTradePoints())
			{
				TextView tvTradePoint = (TextView) m_view.findViewById(R.id.textViewTradePoint);
				View layoutTradePoint = m_view.findViewById(R.id.layoutTradePoint);
				tvTradePoint.setVisibility(View.GONE);
				layoutTradePoint.setVisibility(View.GONE);
			}
			
			// Долг
			EditText etDebt = (EditText) m_view.findViewById(R.id.editTextDebt);
			etDebt.setText(String.format("%.2f", rec.stuff_debt));
			EditText etDebtPast = (EditText) m_view.findViewById(R.id.editTextDebtPast);
			etDebtPast.setText(String.format("%.2f", rec.stuff_debt_past));
			
			TextView textViewDebtPast30 = (TextView)m_view.findViewById(R.id.textViewDebtPast30);
			EditText etDebtPast30 = (EditText) m_view.findViewById(R.id.editTextDebtPast30);
			if (g.Common.PRODLIDER||g.Common.TANDEM)
			{
				etDebtPast30.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_debt_past30));
			} else
			{
				textViewDebtPast30.setVisibility(View.GONE);
				etDebtPast30.setVisibility(View.GONE);
			}
			
			// Долг по договору
			TextView textViewDebtByAgreement=(TextView)m_view.findViewById(R.id.textViewDebtByAgreement);
			TableLayout tableLayoutAgreementDebt=(TableLayout)m_view.findViewById(R.id.tableLayoutAgreementDebt);
			if (g.Common.TITAN||g.Common.INFOSTART)
			{
				textViewDebtByAgreement.setVisibility(View.VISIBLE);
				tableLayoutAgreementDebt.setVisibility(View.VISIBLE);
				EditText editTextAgreementDebt=(EditText)m_view.findViewById(R.id.editTextAgreementDebt);
				EditText editTextAgreementDebtPast=(EditText)m_view.findViewById(R.id.editTextAgreementDebtPast);
				editTextAgreementDebt.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt));
				editTextAgreementDebtPast.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt_past));
			} else
			{
				textViewDebtByAgreement.setVisibility(View.GONE);
				tableLayoutAgreementDebt.setVisibility(View.GONE);
			}
			
			// Результат
			TextView tvResult = (TextView)m_view.findViewById(R.id.textViewResult);
			EditText etResult = (EditText)m_view.findViewById(R.id.etResult);
			etResult.setText(rec.comment_closing);
			
			TextView textPriceType=(TextView)m_view.findViewById(R.id.textPriceType);
			EditText etPriceType=(EditText)m_view.findViewById(R.id.etPriceType);
			
			if (g.Common.PHARAON)
			{
				etDebt.setVisibility(View.GONE);
				etDebtPast.setVisibility(View.GONE);
				TextView textViewDebt = (TextView) m_view.findViewById(R.id.textViewDebt);
				TextView textViewDebtPast = (TextView) m_view.findViewById(R.id.textViewDebtPast);
				textViewDebt.setVisibility(View.GONE);
				textViewDebtPast.setVisibility(View.GONE);
				tvResult.setVisibility(View.GONE);
				etResult.setVisibility(View.GONE);
				textPriceType.setVisibility(View.GONE);
				etPriceType.setVisibility(View.GONE);
			}
			
			// Комментарий
			EditText etComment = (EditText) m_view.findViewById(R.id.etComment);
			etComment.setText(rec.comment);
			etComment.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					MySingleton g=MySingleton.getInstance();
					if (!g.MyDatabase.m_order_editing.comment.equals(s.toString()))
					{
						setModified();
						g.MyDatabase.m_order_editing.comment=s.toString();
					    if (g.Common.NEW_BACKUP_FORMAT)
					    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"comment"});
					}
				}
			});
			
			if (g.Common.PRODLIDER||g.Common.TANDEM)
			{
				// Комментарий оплаты
				EditText etCommentPayment = (EditText) m_view.findViewById(R.id.etCommentPayment);
				etCommentPayment.setText(rec.comment_payment);
				etCommentPayment.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.comment_payment.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.comment_payment=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"comment_payment"});
						}
					}
				});
			} else
			{
				TextView tvCommentPayment = (TextView) m_view.findViewById(R.id.textViewCommentPayment);
				tvCommentPayment.setVisibility(View.GONE);				
				EditText etCommentPayment = (EditText) m_view.findViewById(R.id.etCommentPayment);
				etCommentPayment.setVisibility(View.GONE);				
			}
			//tvAccounts.setVisibility(View.VISIBLE);
			//spinnerAccounts.setVisibility(View.VISIBLE);
			
			if (g.Common.PHARAON)
			{
				EditText etCommentManager = (EditText) m_view.findViewById(R.id.etCommentManager);
				etCommentManager.setText(rec.manager_comment);
				etCommentManager.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.manager_comment.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.manager_comment=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"manager_comment"});
						}
					}
				});
				
				EditText etCommentTheme = (EditText) m_view.findViewById(R.id.etCommentTheme);
				etCommentTheme.setText(rec.theme_comment);
				etCommentTheme.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.theme_comment.equals(s.toString()))
						{
							setModified();
							g.MyDatabase.m_order_editing.theme_comment=s.toString();
						    if (g.Common.NEW_BACKUP_FORMAT)
						    	TextDatabase.SetOrderSQLFields(getActivity().getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"theme_comment"});
						}
					}
				});
				
			} else
			{
				TextView textViewCommentManager = (TextView) m_view.findViewById(R.id.textViewCommentManager);
				EditText etCommentManager = (EditText) m_view.findViewById(R.id.etCommentManager);
				TextView textViewCommentTheme = (TextView) m_view.findViewById(R.id.textViewCommentTheme);
				EditText etCommentTheme = (EditText) m_view.findViewById(R.id.etCommentTheme);
				
				textViewCommentManager.setVisibility(View.GONE);
				etCommentManager.setVisibility(View.GONE);
				textViewCommentTheme.setVisibility(View.GONE);
				etCommentTheme.setVisibility(View.GONE);
				
			}
			
			CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxOrderDontSend);
			if (g.MyDatabase.m_order_editing.state!=E_ORDER_STATE.E_ORDER_STATE_CREATED&&g.MyDatabase.m_order_editing.state!=E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
			{
				cbDontSend.setEnabled(false);
				if (g.MyDatabase.m_order_editing.dont_need_send==1)
				{
					g.MyDatabase.m_order_editing.dont_need_send=0;
				}
			} else
			{
				cbDontSend.setEnabled(true);
			}
			
			cbDontSend.setChecked(g.MyDatabase.m_order_editing.dont_need_send==1);
			cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);		
			
			//
		    // Кнопка выбора даты
			final Button buttonSelectDate=(Button)m_view.findViewById(R.id.buttonOrderSelectDate);
			buttonSelectDate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					//getActivity().showDialog(OrderActivity.IDD_DATE);
					//new MyAlertDialog(getActivity()).setType(OrderActivity.IDD_DATE).show();
					//if (getFragmentManager().findFragmentByTag("dialog") != null)
					//    return;
				    //DialogFragment newFragment = MyAlertDialogFragment.newInstance(OrderActivity.IDD_DATE);
				    //newFragment.show(getFragmentManager(), "dialog");
					//new MyAlertDialogFragment(getActivity()).setType(OrderActivity.IDD_DATE).show(getFragmentManager(), "dialog");
					if (getFragmentManager().findFragmentByTag("dialog") == null)
					{
						OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
						//newFragment.setType(OrderActivity.IDD_DATE);
						//newFragment.setListener(OrderPageFragment.this);
						OrderRecord rec=g.MyDatabase.m_order_editing;
						int day=Integer.parseInt(rec.datedoc.substring(6,8));
						int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
						int year=Integer.parseInt(rec.datedoc.substring(0,4));
					    // set date picker as current date
						//newFragment.setDialog(new DatePickerDialog(getActivity(), datePickerListener,
		                //        year, month,day));
                        Bundle args=new Bundle();
                        args.putInt("dialogId", OrderActivity.MY_ALERT_DIALOG_IDD_DATE);
                        args.putInt("dialogType", OrderAlertDialogFragment.DIALOG_TYPE_DATE);
                        args.putInt("day", day);
                        args.putInt("month", month);
                        args.putInt("year", year);
                        newFragment.setArguments(args);
                        newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});

			if (g.Common.FACTORY)
            {
                // У них еще одна дата - дата доставки
                buttonOrderSelectPlaces.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        MySingleton g=MySingleton.getInstance();
                        if (getFragmentManager().findFragmentByTag("dialog") == null)
                        {
                            OrderRecord rec=g.MyDatabase.m_order_editing;
                            if (rec.shipping_date.length()==14) {
                                OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
                                int day = Integer.parseInt(rec.shipping_date.substring(6, 8));
                                int month = Integer.parseInt(rec.shipping_date.substring(4, 6)) - 1;
                                int year = Integer.parseInt(rec.shipping_date.substring(0, 4));
                                // set date picker as current date
                                //newFragment.setDialog(new DatePickerDialog(getActivity(), datePickerListenerShippingDate,
                                //        year, month, day));
                                Bundle args=new Bundle();
                                args.putInt("dialogId", OrderActivity.MY_ALERT_DIALOG_IDD_SHIPPING_DATE);
                                args.putInt("dialogType", OrderAlertDialogFragment.DIALOG_TYPE_DATE);
                                args.putInt("day", day);
                                args.putInt("month", month);
                                args.putInt("year", year);
                                newFragment.setArguments(args);


                                newFragment.show(getFragmentManager(), "dialog");
                            }
                        }
                    }
                });

            }
			
			// Поиск по номеру карты
			//buttonOrderSearchByCardNum=(Button)m_view.findViewById(R.id.buttonOrderSearchByCardNum);
			buttonOrderSearchByCardNum.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Cursor cursor=getActivity().getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"_id","id","descr"}, "card_num=?", new String[]{String.valueOf(g.MyDatabase.m_order_editing.card_num)}, null);
					int index_id=cursor.getColumnIndex("_id");
					int indexId=cursor.getColumnIndex("id");
					int indexDescr=cursor.getColumnIndex("descr");
					if (cursor.moveToNext())
					{
						int _id=cursor.getInt(index_id);
						//String clientId=cursor.getString(indexId);
						//String clientDescr=cursor.getString(indexDescr);
						if (!cursor.moveToNext())
						{
							
							g.MyDatabase.m_order_editing.create_client=0;
							RadioGroup radioGroup=(RadioGroup) m_view.findViewById(R.id.radioGroupCreateSelectClient);
							radioGroup.check(R.id.radioOrderExistingClient);
							
							if (someEventListener.onClientSelected(m_view, _id))
							{
								setModified();
								boolean bWrongAgreement=true;
								// Проверим, принадлежит ли договор текущему контрагенту 
						        Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
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
					    	        Cursor defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
					    	        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
					    	        {
					    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
					    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
					    	        	if (!defaultAgreementCursor.moveToNext())
					    	        	{
					    	        		// Есть единственный договор
					    	        		someEventListener.onAgreementSelected(m_view, _idAgreement);
					    	        	} else
					    	        	{
					    	        		// Есть несколько договоров
					    	        		someEventListener.onAgreementSelected(m_view, 0);
					    	        	}
					    	        } else
					    	        {
					    	        	// Договора нет
					    	        	someEventListener.onAgreementSelected(m_view, 0);
					    	        }
						        }
							}
						}
					}
					cursor.close();
				}
			});
			
			// Поиск по номеру
			//buttonOrderSearchByPhone=(Button)m_view.findViewById(R.id.buttonOrderSearchByPhone);
			buttonOrderSearchByPhone.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Cursor cursor=getActivity().getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"_id","id","descr"}, "phone_num=?", new String[]{String.valueOf(g.MyDatabase.m_order_editing.phone_num)}, null);
					int index_id=cursor.getColumnIndex("_id");
					int indexId=cursor.getColumnIndex("id");
					int indexDescr=cursor.getColumnIndex("descr");
					if (cursor.moveToNext())
					{
						int _id=cursor.getInt(index_id);
						//String clientId=cursor.getString(indexId);
						//String clientDescr=cursor.getString(indexDescr);
						if (!cursor.moveToNext())
						{
							
							g.MyDatabase.m_order_editing.create_client=0;
							RadioGroup radioGroup=(RadioGroup) m_view.findViewById(R.id.radioGroupCreateSelectClient);
							radioGroup.check(R.id.radioOrderExistingClient);
							
							if (someEventListener.onClientSelected(m_view, _id))
							{
								setModified();
								boolean bWrongAgreement=true;
								// Проверим, принадлежит ли договор текущему контрагенту 
						        Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
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
					    	        Cursor defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
					    	        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
					    	        {
					    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
					    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
					    	        	if (!defaultAgreementCursor.moveToNext())
					    	        	{
					    	        		// Есть единственный договор
					    	        		someEventListener.onAgreementSelected(m_view, _idAgreement);
					    	        	} else
					    	        	{
					    	        		// Есть несколько договоров
					    	        		someEventListener.onAgreementSelected(m_view, 0);
					    	        	}
					    	        } else
					    	        {
					    	        	// Договора нет
					    	        	someEventListener.onAgreementSelected(m_view, 0);
					    	        }
						        }
							}
						}
					}
					cursor.close();
				}
			});
			
			//
		    // Кнопка выбора времени заказа
			/*
			buttonSelectOrderTime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (getFragmentManager().findFragmentByTag("dialog") == null)
					{
						OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
						//newFragment.setType(OrderActivity.IDD_DATE);
						//newFragment.setListener(OrderPageFragment.this);
						OrderRecord rec=MyDatabase.m_order_editing;
						int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
						int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
						newFragment.setDialog(new TimePickerDialog(getActivity(), timePickerListenerBeginTime, 
		                         hourOfDay, minute, true));
						newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});
			*/
			
		    // Кнопки выбора времени
			final Button buttonSelectShippingBeginTime=(Button)m_view.findViewById(R.id.buttonOrderSelectShippingBeginTime);
			buttonSelectShippingBeginTime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					// !!!!!!!!!!!!!!
					//getActivity().showDialog(OrderActivity.IDD_SHIPPING_BEGIN_TIME);
					//new MyAlertDialog(getActivity()).setType(OrderActivity.IDD_SHIPPING_BEGIN_TIME).show();
					if (getFragmentManager().findFragmentByTag("dialog") == null)
					{
						OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
						//newFragment.setType(OrderActivity.IDD_DATE);
						//newFragment.setListener(OrderPageFragment.this);
						OrderRecord rec=g.MyDatabase.m_order_editing;
						int hourOfDay=Integer.parseInt(rec.shipping_begin_time.substring(0,2));
						int minute=Integer.parseInt(rec.shipping_begin_time.substring(2,4));
						//newFragment.setDialog(new TimePickerDialog(getActivity(), timePickerListenerBeginTime,
		                //         hourOfDay, minute, true));

                        Bundle args=new Bundle();
                        args.putInt("dialogId", OrderActivity.MY_ALERT_DIALOG_IDD_BEGIN_TIME);
                        args.putInt("dialogType", OrderAlertDialogFragment.DIALOG_TYPE_TIME);
                        args.putInt("hour", hourOfDay);
                        args.putInt("minute", minute);
                        newFragment.setArguments(args);

						newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});
			final Button buttonSelectShippingEndTime=(Button)m_view.findViewById(R.id.buttonOrderSelectShippingEndTime);
			buttonSelectShippingEndTime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					// !!!!!!!!!!!!!!
					//getActivity().showDialog(OrderActivity.IDD_SHIPPING_END_TIME);
					//new MyAlertDialog(getActivity()).setType(OrderActivity.IDD_SHIPPING_END_TIME).show();
					if (getFragmentManager().findFragmentByTag("dialog") == null)
					{
						OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
						//newFragment.setType(OrderActivity.IDD_DATE);
						//newFragment.setListener(OrderPageFragment.this);
						OrderRecord rec=g.MyDatabase.m_order_editing;
						int hourOfDay=Integer.parseInt(rec.shipping_end_time.substring(0,2));
						int minute=Integer.parseInt(rec.shipping_end_time.substring(2,4));
						//newFragment.setDialog(new TimePickerDialog(getActivity(), timePickerListenerEndTime,
		                //         hourOfDay, minute, true));
                        Bundle args=new Bundle();
                        args.putInt("dialogId", OrderActivity.MY_ALERT_DIALOG_IDD_END_TIME);
                        args.putInt("dialogType", OrderAlertDialogFragment.DIALOG_TYPE_TIME);
                        args.putInt("hour", hourOfDay);
                        args.putInt("minute", minute);
                        newFragment.setArguments(args);

    					newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});
		    
		    // Кнопка выбора клиента
			final Button buttonSelectClient=(Button)m_view.findViewById(R.id.buttonOrderSelectClient);
			buttonSelectClient.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), ClientsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_order_editing.client_id.toString());
					if (m_distr_point_id_only!=null)
					{
						intent.putExtra("distr_point_id_only", m_distr_point_id_only);
					}
					startActivityForResult(intent, SELECT_CLIENT_FROM_ORDER_REQUEST);
				}
			});

			// Кнопка выбора соглашения
			final Button buttonSelectAgreement30=(Button)m_view.findViewById(R.id.buttonOrderSelectAgreement30);
			buttonSelectAgreement30.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), Agreements30Activity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_order_editing.client_id.toString());
					startActivityForResult(intent, SELECT_AGREEMENT30_FROM_ORDER_REQUEST);
				}
			});

		    // Кнопка выбора договора
			final Button buttonSelectAgreement=(Button)m_view.findViewById(R.id.buttonOrderSelectAgreement);
			buttonSelectAgreement.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), AgreementsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_order_editing.client_id.toString());
					startActivityForResult(intent, SELECT_AGREEMENT_FROM_ORDER_REQUEST);
				}
			});
			
		    // Кнопка выбора торговой точки
			final Button buttonSelectTradePoint=(Button)m_view.findViewById(R.id.buttonOrderSelectTradePoint);
			buttonSelectTradePoint.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), TradePointsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_order_editing.client_id.toString());
					startActivityForResult(intent, SELECT_TRADE_POINT_FROM_ORDER_REQUEST);
				}
			});
			
			
			// Spinner организации
	        //ArrayAdapter<String> dataAdapterOrganizations = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_organizations_descr);
	        //dataAdapterOrganizations.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        //spinnerOrganization.setAdapter(dataAdapterOrganizations);
			
			// Spinner типы учета
			TextView tvAccounts=(TextView)m_view.findViewById(R.id.tvAccount);
			Spinner spinnerAccounts=(Spinner)m_view.findViewById(R.id.spinnerAccount);
	        ArrayAdapter<String> dataAdapterAccounts = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_accounts_descr);
	        dataAdapterAccounts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerAccounts.setAdapter(dataAdapterAccounts);
	        int i;
	        for (i=0; i<OrderActivity.m_list_accounts_id.length; i++)
	        {
	        	if (Integer.parseInt(OrderActivity.m_list_accounts_id[i])==g.MyDatabase.m_order_editing.bw.value())
	        	{
	        		spinnerAccounts.setSelection(i);
	        	}
	        }
			if (rec.stuff_select_account||g.Common.MEGA)
			{
				tvAccounts.setVisibility(View.VISIBLE);
				spinnerAccounts.setVisibility(View.VISIBLE);
			} else
			{
				tvAccounts.setVisibility(View.GONE);
				spinnerAccounts.setVisibility(View.GONE);
			}
	        spinnerAccounts.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (g.MyDatabase.m_order_editing.bw.value()!=Integer.parseInt(OrderActivity.m_list_accounts_id[index]))
					{
						setModified();
						g.MyDatabase.m_order_editing.bw=E_BW.fromInt(Integer.parseInt(OrderActivity.m_list_accounts_id[index]));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//MyDatabase.m_order_editing.bw=E_BW.E_BW_BOTH;
				}
			});
	        
			// Spinner типы цен
			Spinner spinnerPriceType=(Spinner)m_view.findViewById(R.id.spinnerPriceType);
	        ArrayAdapter<String> dataAdapterPriceTypes = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_prices_descr);
	        dataAdapterPriceTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerPriceType.setAdapter(dataAdapterPriceTypes);
	        if (!Constants.ENABLE_PRICE_TYPE_SELECT)
	        {
	        	// Запрещаем редактирование типа цены
	        	spinnerPriceType.setVisibility(View.GONE);
	        	if (!g.Common.PHARAON)
	        	{
	        		etPriceType.setVisibility(View.VISIBLE);
	        	}
		        for (i=0; i<OrderActivity.m_list_prices_id.length; i++)
		        {
		        	if (OrderActivity.m_list_prices_id[i].equals(g.MyDatabase.m_order_editing.price_type_id.toString()))
		        	{
		        		etPriceType.setText(OrderActivity.m_list_prices_descr[i]);
		        	}
		        }
	        	
	        } else
	        {
	        	// Разрешаем редактирование типа цены
	        	spinnerPriceType.setVisibility(View.VISIBLE);
	        	etPriceType.setVisibility(View.GONE);
	        	//
		        for (i=0; i<OrderActivity.m_list_prices_id.length; i++)
		        {
		        	if (OrderActivity.m_list_prices_id[i].equals(g.MyDatabase.m_order_editing.price_type_id.toString()))
		        	{
		        		spinnerPriceType.setSelection(i);
		        	}
		        }
		        spinnerPriceType.setOnItemSelectedListener(new OnItemSelectedListener() {
	
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
					{
						MySingleton g=MySingleton.getInstance();
						if (!g.MyDatabase.m_order_editing.price_type_id.toString().equals(OrderActivity.m_list_prices_id[index]))
						{
							setModified();
							g.MyDatabase.m_order_editing.price_type_id=new MyID(OrderActivity.m_list_prices_id[index]);
							someEventListener.someEvent("recalc_price");
						}
					}
	
					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						//setModified();
						//MyDatabase.m_order_editing.price_type_id=new MyID();
					}
				});
	        }
	        
			// Spinner склады
			Spinner spinnerStocks=(Spinner)m_view.findViewById(R.id.spinnerStock);
			//String[] data = {"one", "two", "three", "four", "five"};			
	        ArrayAdapter<String> dataAdapterStocks = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_stocks_descr);
	        dataAdapterStocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerStocks.setAdapter(dataAdapterStocks);
	        for (i=0; i<OrderActivity.m_list_stocks_id.length; i++)
	        {
	        	if (OrderActivity.m_list_stocks_id[i].equals(g.MyDatabase.m_order_editing.stock_id.toString()))
	        	{
	        		spinnerStocks.setSelection(i);
	        	}
	        }
	        spinnerStocks.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (!g.MyDatabase.m_order_editing.stock_id.toString().equals(OrderActivity.m_list_stocks_id[index]))
					{
						setModified();
						g.MyDatabase.m_order_editing.stock_id=new MyID(OrderActivity.m_list_stocks_id[index]);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//MyDatabase.m_order_editing.stock_id=new MyID();
				}
	        });
	        
	        if (g.Common.MEGA||g.Common.PHARAON)
	        {
	        	View tvStock=m_view.findViewById(R.id.textViewStock);
	        	tvStock.setVisibility(View.GONE);
	        	spinnerStocks.setVisibility(View.GONE);
	        }
	        
	        // Spinner тип доставки
			Spinner spinnerShippingType=(Spinner)m_view.findViewById(R.id.spinnerShippingType);
			//String[] dataSpinnerShippingType = {"Автотранспорт", "Самовывоз"};
			String[] dataSpinnerShippingType = getResources().getStringArray(R.array.shipping_type);
			
	        ArrayAdapter<String> dataAdapterShippingType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataSpinnerShippingType);
	        dataAdapterShippingType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerShippingType.setAdapter(dataAdapterShippingType);

			int shipping_type=g.MyDatabase.m_order_editing.shipping_type;
	        if (shipping_type<0||shipping_type>=dataSpinnerShippingType.length)
	        	spinnerShippingType.setSelection(0);
	        else
	        	spinnerShippingType.setSelection(shipping_type);
			
	        spinnerShippingType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (g.MyDatabase.m_order_editing.shipping_type!=index)
					{
						setModified();
						g.MyDatabase.m_order_editing.shipping_type=index;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
	        });
	        
	        // Spinner время доставки
			Spinner spinnerShippingTime=(Spinner)m_view.findViewById(R.id.spinnerShippingTime);
			//String[] dataSpinnerShippingTime = {"В любое время", "Интервал"};
			String[] dataSpinnerShippingTime = getResources().getStringArray(R.array.shipping_time);
			
	        ArrayAdapter<String> dataAdapterShippingTime = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, dataSpinnerShippingTime);
			
	        dataAdapterShippingTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerShippingTime.setAdapter(dataAdapterShippingTime);

			int shipping_time=g.MyDatabase.m_order_editing.shipping_time;
	        if (shipping_time<0||shipping_time>=dataSpinnerShippingTime.length)
	        	spinnerShippingTime.setSelection(0);
	        else
	        	spinnerShippingTime.setSelection(shipping_time);
			
	        spinnerShippingTime.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (g.MyDatabase.m_order_editing.shipping_time!=index)
					{
						setModified();
						g.MyDatabase.m_order_editing.shipping_time=index;
						
				        View layoutShippingTime=(View)m_view.findViewById(R.id.layoutShippingTime);
				    	if (g.MyDatabase.m_order_editing.shipping_time==0)
				    	{
					    	layoutShippingTime.setVisibility(View.GONE);
				    	} else
				    	{
				    		layoutShippingTime.setVisibility(View.VISIBLE);
				    	}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
	        });
	        
	        TextView tvShippingType=(TextView)m_view.findViewById(R.id.tvShippingType);
	        TextView tvShippingTime=(TextView)m_view.findViewById(R.id.tvShippingTime);
	        View layoutShippingTime=(View)m_view.findViewById(R.id.layoutShippingTime);
	        
		    if (g.Common.PRODLIDER||g.Common.TANDEM)
		    {
		    	//bw.write(String.format("PL:%d;%d;%s;%s\r\n", orderRecord.shipping_type, orderRecord.shipping_time, orderRecord.shipping_begin_time, orderRecord.shipping_end_time));
		    	if (g.MyDatabase.m_order_editing.shipping_time==0)
		    	{
			    	layoutShippingTime.setVisibility(View.GONE);
		    	}
		    } else
		    {
		    	tvShippingType.setVisibility(View.GONE);
		    	spinnerShippingType.setVisibility(View.GONE);
		    	tvShippingTime.setVisibility(View.GONE);
		    	spinnerShippingTime.setVisibility(View.GONE);
		    	layoutShippingTime.setVisibility(View.GONE);
		    }
	        
	        // Куратор
	        final EditText etCurator=(EditText)m_view.findViewById(R.id.editTextCurator);
	        etCurator.setText(rec.stuff_curator_name);
	        if (g.Common.PHARAON)
	        {
	        	etCurator.setVisibility(View.GONE);
		        TextView textViewCurator=(TextView)m_view.findViewById(R.id.textViewCurator);
		        textViewCurator.setVisibility(View.GONE);
	        }

	        // Состояние
	        final EditText etState=(EditText)m_view.findViewById(R.id.editTextState);
	        if (rec.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
	        {
	        	// Напишем, что состояние "создан"
	        	etState.setText(g.MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_CREATED, getActivity().getApplicationContext()));
	        } else
	        {
	        	etState.setText(g.MyDatabase.GetOrderStateDescr(rec.state, getActivity().getApplicationContext()));
	        }
			
		    // Кнопка OK
			final Button buttonOk=(Button)m_view.findViewById(R.id.buttonOrderOk);
			if (buttonOk!=null)
			{
				buttonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//Intent intent=new Intent();
						//getActivity().setResult(OrderActivity.ORDER_RESULT_OK, intent);
						//getActivity().finish();
						someEventListener.someEvent("ok");
					}
				});
			}
			
			// Кнопка Print
			final Button buttonPrint=(Button)m_view.findViewById(R.id.buttonOrderPrint);
			if (buttonPrint!=null)
			{
				if (g.Common.PHARAON)
				{
					buttonPrint.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							
							bPrintOnce=false;
							
							registerForContextMenu(v); 
							getActivity().openContextMenu(v);
							unregisterForContextMenu(v);
							
							// TODO
							/*
							someEventListener.someEvent("print");
							// флаг "не готов, не отправлять" мог при этом сняться
							CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxOrderDontSend);
							if (cbDontSend!=null)
							{
								cbDontSend.setOnCheckedChangeListener(null);
								cbDontSend.setChecked(g.MyDatabase.m_order_editing.dont_need_send==1);
								cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);
							}
							*/
						}
					});
				} else
				{
					buttonPrint.setVisibility(View.GONE);					
				}
			}
			
		    // Кнопка Cancel
			final Button buttonClose=(Button)m_view.findViewById(R.id.buttonOrderClose);
			if (buttonClose!=null)
			{
				buttonClose.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						someEventListener.someEvent("close");
						
						/*
						if (g.MyDatabase.m_order_editing_modified)
						{
							getActivity().showDialog(OrderActivity.CLOSE_QUERY_DIALOG_ID);
						} else
						{
							Intent intent=new Intent();
							getActivity().setResult(OrderActivity.ORDER_RESULT_CANCEL, intent);
							getActivity().finish();
						}
						*/
					}
				});
			}
			
			
	    }
		/////////////////////////////////////////////
		if (bLinesPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_lines_fragment, null);
	    	
	    	ListView lvOrderLines=(ListView)m_view.findViewById(R.id.listViewOrderLines);
		    registerForContextMenu(lvOrderLines);

		    //TextView tvPage = (TextView) m_view.findViewById(R.id.tvPage);
		    //tvPage.setText("Page " + pageNumber);
		    //tvPage.setBackgroundColor(backColor);
	    	/*
	    	final String ATTRIBUTE_NAME_NOMENCLATURE="nomeclature";
	    	final String ATTRIBUTE_NAME_QUANTITY="quantity";
	    	final String ATTRIBUTE_NAME_SUM="sum";
	    	*/
	    	/*
	    		data = new ArrayList<Map<String, Object>>();
	    	    Map<String, Object> m;
    	    	m = new HashMap<String, Object>();
    	    	m.put(ATTRIBUTE_NAME_NOMENCLATURE, "test sdgj dgjsdhgsd hgih dfgh sdgh d sfghi dghds ghdf gdfg h fgh dfgh fgh dfh hdf !!! dfg");
    	    	m.put(ATTRIBUTE_NAME_QUANTITY, "test");
    	    	m.put(ATTRIBUTE_NAME_SUM, "test");
    	    	data.add(m);
    	    	m = new HashMap<String, Object>();
    	    	m.put(ATTRIBUTE_NAME_NOMENCLATURE, "test");
    	    	m.put(ATTRIBUTE_NAME_QUANTITY, "test");
    	    	m.put(ATTRIBUTE_NAME_SUM, "test");
    	    	data.add(m);
    	    */
	    	
    	    	// массив имен атрибутов, из которых будут читаться данные
    	        //String[] from = { ATTRIBUTE_NAME_NOMENCLATURE, ATTRIBUTE_NAME_QUANTITY,	ATTRIBUTE_NAME_SUM };
    	        // массив ID View-компонентов, в которые будут вставлять данные
    	        //int[] to = { R.id.textViewOrderLineNomenclature, R.id.textViewOrderLineQuantity, R.id.textViewOrderLineSum };

    	        // создаем адаптер
    	        //sAdapter = new SimpleAdapter(getActivity(), data, R.layout.order_line_item, from, to);
    	    	
	    	/*
    			ArrayList<ArrayList<String>> groups=new ArrayList<ArrayList<String>>();
    			
    			ArrayList<String> children1=new ArrayList<String>();
    			ArrayList<String> children2=new ArrayList<String>();
    			
    			children1.add("child1");
    			children1.add("child2");
    			
    			groups.add(children1);
    			
    			children2.add("child1");
    			children2.add("child2");
    			children2.add("child3");
    			
    			groups.add(children2);
    	    	
    	    	//sAdapter = new OrderExpListAdapter(getActivity().getApplicationContext(), groups);
    			sAdapter = new OrderExpListAdapter(getActivity().getApplicationContext());
    	    */
		    	ExpandableListView lvSimple = (ExpandableListView) m_view.findViewById(R.id.listViewOrderLines);
                int defaultTextColor=Common.getColorFromAttr(getActivity(), R.attr.myTextColor);
		    	if (g.Common.PHARAON)
		    	{
		    		g.MyDatabase.m_orderLinesAdapter = new OrderExpPhListAdapter(getActivity().getApplicationContext(), defaultTextColor);
		    		((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).init();
	    	        lvSimple.setAdapter(g.MyDatabase.m_orderLinesAdapter);
	    	        // раскроем список
		    		int count = ((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).getGroupCount();
		    		for (int position = 0; position < count; position++)
		    		{
			    		if (((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(position).need_expand)
			    		{
			    			((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(position).need_expand=true;
							lvSimple.expandGroup(position);
			    		}
		    		}
		    	} else
		    	{
		    		g.MyDatabase.m_orderLinesAdapter = new OrderExpListAdapter(getActivity().getApplicationContext(), defaultTextColor);
	    	        lvSimple.setAdapter(g.MyDatabase.m_orderLinesAdapter);
		    	}
    	        
    	        
    	        /*
    	        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
    	            @Override
    	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
    	            	
    	            	switch (view.getId())
    	            	{
    	            	case R.id.tvNomenclatureLineDescr:
    	            	{
    	            		break;
    	            	}
    	            	}
    	            	return false;
    	            }
    	        };
    	        MyDatabase.m_linesAdapter.setViewBinder(binder);
    	        */
    	        
    	        // Кнопка редактирования
    			final Button buttonEditLines=(Button)m_view.findViewById(R.id.buttonEditLines);
    			buttonEditLines.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					Common.honorBeerman(6);
    					Intent intent=new Intent(getActivity(), NomenclatureActivity.class);
    					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    					startActivityForResult(intent, OPEN_NOMENCLATURE_FROM_ORDER_REQUEST);
    					//startActivity(intent);
    				}
    			});
    			
    	        // Кнопка редактирования вторая
    			final Button buttonEditLinesGrid=(Button)m_view.findViewById(R.id.buttonEditLinesGrid);
    			buttonEditLinesGrid.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					Common.honorBeerman(6);
    					Intent intent=new Intent(getActivity(), NomenclatureGridActivity.class);
    					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    					startActivityForResult(intent, OPEN_NOMENCLATURE_GRID_FROM_ORDER_REQUEST);
    					//startActivity(intent);
    				}
    			});
    			
    			// Статистика документа
    			TextView tvStatistics=(TextView)m_view.findViewById(R.id.textViewStatistics);
    			if (g.MyDatabase.m_order_editing.lines.size()==0)
    			{
    				tvStatistics.setText("0");
    			} else
    			{
    				StringBuilder sb=new StringBuilder();
    				sb.append(Integer.toString(g.MyDatabase.m_order_editing.lines.size()));
    				sb.append(": ").append(String.format(g.Common.getCurrencyFormatted(getActivity()), Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.sumDoc, "%.3f")));
    				sb.append(", ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(g.MyDatabase.m_order_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
    				tvStatistics.setText(sb.toString());
    			}
	    }
	    if (bSettingsPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_advanced_fragment, null);
	    	
			final TextView tvLatitude = (TextView) m_view.findViewById(R.id.textViewOrderLatitudeValue);
			tvLatitude.setText(Double.toString(g.MyDatabase.m_order_editing.latitude));
			final TextView tvLongitude = (TextView) m_view.findViewById(R.id.textViewOrderLongitudeValue);
			tvLongitude.setText(Double.toString(g.MyDatabase.m_order_editing.longitude));
			final TextView tvDateCoord = (TextView) m_view.findViewById(R.id.textViewOrderDateCoordValue);
			tvDateCoord.setText(g.MyDatabase.m_order_editing.datecoord);
	    	
	    	final CheckBox cbReceiveCoord=(CheckBox)m_view.findViewById(R.id.checkBoxOrderReceiveCoord);
	    	cbReceiveCoord.setChecked(g.MyDatabase.m_order_editing.accept_coord==1);
	    	cbReceiveCoord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					MySingleton g=MySingleton.getInstance();
					setModified();
					g.MyDatabase.m_order_editing.accept_coord=isChecked?1:0;
					someEventListener.someEvent("coord");
				}
			});
	    }

	    return m_view;
	  }
	  
		final OnCheckedChangeListener cbDontSendOnCheckedChangeListener=new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				MySingleton g=MySingleton.getInstance();
				setModified();
				g.MyDatabase.m_order_editing.dont_need_send=isChecked?1:0;
			}
		};
	  
	  void updateClientsVisibility()
	  {
		  MySingleton g=MySingleton.getInstance();
		  
		TextView textViewClientLastName=(TextView)m_view.findViewById(R.id.textViewClientLastName);
		EditText etLastname=(EditText)m_view.findViewById(R.id.etLastname);
		TextView textViewClientFirstname=(TextView)m_view.findViewById(R.id.textViewClientFirstName);
		EditText etFirstname=(EditText)m_view.findViewById(R.id.etFirstname);
		TextView textViewClientSurname=(TextView)m_view.findViewById(R.id.textViewClientSurname);
		EditText etSurname=(EditText)m_view.findViewById(R.id.etSurname);
		//TextView textViewClientPhone=(TextView)m_view.findViewById(R.id.textViewClientPhone);
		//EditText etPhone=(EditText)m_view.findViewById(R.id.etPhone);
		
		TextView textViewClient=(TextView)m_view.findViewById(R.id.textViewClient);
		EditText etClient=(EditText)m_view.findViewById(R.id.etClient);
		Button buttonOrderSelectClient=(Button)m_view.findViewById(R.id.buttonOrderSelectClient);
		TextView textViewOrdersClientAddress=(TextView)m_view.findViewById(R.id.textViewOrdersClientAddress);
		
	    if (g.Common.PHARAON&&g.MyDatabase.m_order_editing.create_client==1)
	    {
			textViewClientLastName.setVisibility(View.VISIBLE);
			etLastname.setVisibility(View.VISIBLE);
			textViewClientFirstname.setVisibility(View.VISIBLE);
			etFirstname.setVisibility(View.VISIBLE);
			textViewClientSurname.setVisibility(View.VISIBLE);
			etSurname.setVisibility(View.VISIBLE);
			//textViewClientPhone.setVisibility(View.VISIBLE);
			//etPhone.setVisibility(View.VISIBLE);
			
			textViewClient.setVisibility(View.GONE);
			etClient.setVisibility(View.GONE);
			buttonOrderSelectClient.setVisibility(View.GONE);
			textViewOrdersClientAddress.setVisibility(View.GONE);
	    } else
	    {
			textViewClientLastName.setVisibility(View.GONE);
			etLastname.setVisibility(View.GONE);
			textViewClientFirstname.setVisibility(View.GONE);
			etFirstname.setVisibility(View.GONE);
			textViewClientSurname.setVisibility(View.GONE);
			etSurname.setVisibility(View.GONE);
			//textViewClientPhone.setVisibility(View.GONE);
			//etPhone.setVisibility(View.GONE);
			textViewClient.setVisibility(View.VISIBLE);
			etClient.setVisibility(View.VISIBLE);
			buttonOrderSelectClient.setVisibility(View.VISIBLE);
			textViewOrdersClientAddress.setVisibility(View.VISIBLE);
	    }

	  }
	  
	  private BroadcastReceiver onNotice= new BroadcastReceiver() {

		    @Override
		    public void onReceive(Context context, Intent intent) {
		        // intent can contain anydata
		        //Log.d("sohail","onReceive called");
		        //tv.setText("Broadcast received !");
				redrawSumWeight();

		    }
		};
		
		@Override
		public void onPause() {
			super.onPause();
			
			LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
		}

		@Override
		public void onResume() {
			super.onResume();
			
			IntentFilter iff= new IntentFilter(OrderActivity.ACTION);
	        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);		
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			MySingleton g=MySingleton.getInstance();
			switch (item.getItemId())
			{
			case R.id.action_print_list:
			if (!bPrintOnce)
			{
				bPrintOnce=true;
				someEventListener.someEvent("printList");
				// флаг "не готов, не отправлять" мог при этом сняться
				CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxOrderDontSend);
				if (cbDontSend!=null)
				{
					cbDontSend.setOnCheckedChangeListener(null);
					cbDontSend.setChecked(g.MyDatabase.m_order_editing.dont_need_send==1);
					cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);
				}
			}
			break;
			case R.id.action_print_ko:
			if (!bPrintOnce)
			{
				bPrintOnce=true;
				someEventListener.someEvent("printKO");
				// флаг "не готов, не отправлять" мог при этом сняться
				CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxOrderDontSend);
				if (cbDontSend!=null)
				{
					cbDontSend.setOnCheckedChangeListener(null);
					cbDontSend.setChecked(g.MyDatabase.m_order_editing.dont_need_send==1);
					cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);
				}
			}
			break;
			case R.id.action_print_both:
			if (!bPrintOnce)
			{
				bPrintOnce=true;
				someEventListener.someEvent("printBoth");
				// флаг "не готов, не отправлять" мог при этом сняться
				CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxOrderDontSend);
				if (cbDontSend!=null)
				{
					cbDontSend.setOnCheckedChangeListener(null);
					cbDontSend.setChecked(g.MyDatabase.m_order_editing.dont_need_send==1);
					cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);
				}
			}
			break;
			case R.id.action_change_quantity:
			if (bLinesPage)
			{
				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
				int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
				
				OrderLineRecord line;
				
				// TODO редактирование количества
				// 1С печать общего списка количества 
				if (g.Common.PHARAON)
				{
					int pos_item=ExpandableListView.getPackedPositionChild(info.packedPosition);
					line=((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(pos).lines.get(pos_item);
					m_order_editing_line_num=g.MyDatabase.m_order_editing.lines.indexOf(line);
				} else
				{
					
					line=g.MyDatabase.m_order_editing.lines.get(pos);
					m_order_editing_line_num=pos;
				}
					
				Intent intent=new Intent(getActivity(), QuantityActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    intent.putExtra("id", line.nomenclature_id.toString());
			    intent.putExtra("quantity", line.quantity);
			    intent.putExtra("k", line.k);
			    intent.putExtra("ed", line.ed);
			    intent.putExtra("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
			    if (g.Common.FACTORY)
                {
                    intent.putExtra("agreement30_id", g.MyDatabase.m_order_editing.agreement30_id.toString());
                }
			    intent.putExtra("shipping_time", line.shipping_time);
			    intent.putExtra("comment_in_line", line.comment_in_line);
			    
			    // добавлено 30.11.2013, раньше было - только из списка номенклатуры
				ContentValues cv=new ContentValues();
				cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());
			    getActivity().getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
			    //
			    
			    // TODO устанавливать только при изменении, см. также NomenclatureActitivity, там такой же код
			    TextDatabase.prepareNomenclatureStuff(getActivity().getContentResolver());
			    
			    Cursor restCursor=getActivity().getContentResolver().query(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, new String[]{"nom_quantity"}, "nomenclature.id=?", new String[]{line.nomenclature_id.toString()}, null);
			    if (restCursor.moveToFirst())
			    {
				    intent.putExtra("rest", restCursor.getDouble(0));
			    }
			    restCursor.close();
				startActivityForResult(intent, OrderActivity.QUANTITY_REQUEST);
				
			}
			break;
			case R.id.action_change_price:
			if (bLinesPage)
			{
				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
				int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
				OrderLineRecord line;
				
				if (g.Common.PHARAON)
				{
					int pos_item=ExpandableListView.getPackedPositionChild(info.packedPosition);
					line=((OrderExpPhListAdapter)g.MyDatabase.m_orderLinesAdapter).mGroups.get(pos).lines.get(pos_item);
					m_order_editing_line_num=g.MyDatabase.m_order_editing.lines.indexOf(line);
				} else
				{
					line=g.MyDatabase.m_order_editing.lines.get(pos);
					m_order_editing_line_num=pos;
				}
				
				/*
				Intent intent=new Intent(getActivity(), QuantityActivity.class);
			    intent.putExtra("id", line.nomenclature_id.toString());
			    intent.putExtra("quantity", line.quantity);
			    intent.putExtra("k", line.k);
			    intent.putExtra("ed", line.ed);
			    intent.putExtra("price_type_id", MyDatabase.m_order_editing.price_type_id.toString());
			    */
				
				// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(getActivity());
				View promptsView = li.inflate(R.layout.price, null);
 
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
 
				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);
 
				final EditText userInput = (EditText) promptsView.findViewById(R.id.etPrice);
				
				userInput.setText(Common.DoubleToStringFormat(line.price, "%.3f").replace(',', '.'));
 
				// set dialog message
				alertDialogBuilder
					.setTitle("Введите новую цену")
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok,
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
					    	
					    	MySingleton g=MySingleton.getInstance();
					    	
					    double price=0.0;
					    boolean priceOk=false;
						try {
							// get user input and set it to result
							// edit text
							//result.setText(userInput.getText());
							price = Double.parseDouble(userInput.getText().toString());
							price = Math.floor(price*100.0+0.0001)/100.0;
							priceOk=true;
						} catch (Exception e) {
						}
						
						if (priceOk)
						{
							OrderLineRecord line = g.MyDatabase.m_order_editing.lines
									.get(m_order_editing_line_num);
							line.price = price;
							line.total = Math.floor(line.price * line.quantity
									* 100.0 + 0.00001) / 100.0;
							g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();
							setModified();
							recalcSumWeight();
							redrawSumWeight();
							if (g.Common.NEW_BACKUP_FORMAT)
							{
								TextDatabase.UpdateOrderLine(getActivity().getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
							}
						}
					    }
					  })
					.setNegativeButton(android.R.string.cancel,
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });
				
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				
				alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
 
				// show it
				alertDialog.show();				
			    
			}
			break;
			}
			return super.onContextItemSelected(item);
		}


		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			
		}

		/*
		@Override
		public void onSoftKeyboardShown(boolean isShowing) {
			if (bHeaderPage)
			{
			
				final Button buttonOk=(Button)m_view.findViewById(R.id.buttonOrderOk);
				final Button buttonPrint=(Button)m_view.findViewById(R.id.buttonOrderPrint);
				final Button buttonClose=(Button)m_view.findViewById(R.id.buttonOrderClose);
				
				if (isShowing) {
			        //Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
					if (buttonOk!=null)
						buttonOk.setVisibility(View.GONE);
					if (buttonPrint!=null)
						buttonPrint.setVisibility(View.GONE);
					if (buttonClose!=null)
						buttonClose.setVisibility(View.GONE);
			    } else {
			        //Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
					if (buttonOk!=null)
						buttonOk.setVisibility(View.VISIBLE);
					if (buttonPrint!=null&&g.Common.PHARAON)
						buttonPrint.setVisibility(View.VISIBLE);
					if (buttonClose!=null)
						buttonClose.setVisibility(View.VISIBLE);
			    }
			}
		}
		*/
		
}
