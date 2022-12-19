package ru.code22.mtrade;

import java.util.Random;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.MyDatabase.RefundRecord;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class RefundPageFragment extends Fragment {

	private ActivityResultLauncher<Intent> selectClientFromRefundActivityResultLauncher;
	private ActivityResultLauncher<Intent> selectAgreementFromRefundActivityResultLauncher;
	private ActivityResultLauncher<Intent> openNomenclatureFromRefundActivityResultLauncher;
	private ActivityResultLauncher<Intent> selectTradePointFromRefundActivityResultLauncher;
	private ActivityResultLauncher<Intent> quantityRequestFromRefundActivityResultLauncher;

	////static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    //static final int SELECT_CLIENT_FROM_REFUND_REQUEST = 2;
    //static final int SELECT_AGREEMENT_FROM_REFUND_REQUEST = 3;
    //static final int OPEN_NOMENCLATURE_FROM_REFUND_REQUEST = 4;
    //static final int SELECT_TRADE_POINT_FROM_REFUND_REQUEST = 5;
	////static final int QUANTITY_REQUEST = 6;
    ////static final int SELECT_DISCOUNT_FROM_ORDER_REQUEST = 7;
    ////static final int SELECT_PLACES_FROM_ORDER_REQUEST = 8;

	static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    
	int pageNumber;
	int backColor;
	  
    View m_view=null;
	EquipmentRestsFragment myFrag1=null;
	EquipmentRestsFragment myFrag2=null;

    //SimpleAdapter sAdapter;
    //ArrayList<Map<String, Object>> data;
    
	// сюда записывается номер строки, когда начинают редактировать количество в строке
	int m_refund_editing_line_num;
	
	boolean bHeaderPage=false;
	boolean bLinesPage=false;
	boolean bSettingsPage=false;

	/*
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
	@Override
	// when dialog box is closed, below method will be called.
	public void onDateSet(DatePicker view, int selectedYear,
			int selectedMonth, int selectedDay) {
		
		MySingleton g=MySingleton.getInstance();
		
	    RefundRecord rec=g.MyDatabase.m_refund_editing;
		
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
	*/

	/*
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
	    if (data == null) 
	    	return;
	    
	    MySingleton g=MySingleton.getInstance();
		
		switch (requestCode)
		{
		case SELECT_CLIENT_FROM_REFUND_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
		    long _id=data.getLongExtra("id", 0);
			if (someEventListener.onRefundClientSelected(view0, _id))
			{
				setModified();
				boolean bWrongAgreement=true;
				// Проверим, принадлежит ли договор текущему контрагенту 
		        Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.agreement_id.toString()}, null);
		        if (agreementsCursor.moveToNext())
		        {
		        	int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
		        	String owner_id = agreementsCursor.getString(owner_idIndex);
		        	if (g.MyDatabase.m_refund_editing.client_id.toString().equals(owner_id))
		        		bWrongAgreement=false;
		        }
		        agreementsCursor.close();
		        if (bWrongAgreement)
		        {
	        		// Не принадлежит, установим договор по умолчанию, если он единственный
	    	        Cursor defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
	    	        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
	    	        {
	    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
	    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
	    	        	if (!defaultAgreementCursor.moveToNext())
	    	        	{
	    	        		// Есть единственный договор
	    	        		someEventListener.onRefundAgreementSelected(view0, _idAgreement);
	    	        	} else
	    	        	{
	    	        		// Есть несколько договоров
	    	        		someEventListener.onRefundAgreementSelected(view0, 0);
	    	        	}
	    	        } else
	    	        {
	    	        	// Договора нет
	    	        	someEventListener.onRefundAgreementSelected(view0, 0);
	    	        }
		        }
			}
			break;
		}
		case SELECT_AGREEMENT_FROM_REFUND_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
			EditText et=(EditText)view0.findViewById(R.id.etAgreement);
			TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewRefundOrganization);
			
		    long _id=data.getLongExtra("id", 0);
		    if (someEventListener.onRefundAgreementSelected(view0, _id))
		    {
		    	setModified();
		    }
			break;
		}
		case SELECT_TRADE_POINT_FROM_REFUND_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
			EditText et=(EditText)view0.findViewById(R.id.etTradePoint);
			
			setModified();
			
		    long _id=data.getLongExtra("id", 0);
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, _id);
		    Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr", "address"}, null, null, null);
		    if (cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	int idIndex = cursor.getColumnIndex("id");
		    	//int addressIndex = cursor.getColumnIndex("address");
		    	String descr = cursor.getString(descrIndex);
		    	String tradePointId = cursor.getString(idIndex);
		    	//String address = cursor.getString(addressIndex);
		    	et.setText(descr);
				g.MyDatabase.m_refund_editing.distr_point_id.m_id=tradePointId;
				g.MyDatabase.m_refund_editing.stuff_distr_point_name=descr;
		    } else
		    {
		    	et.setText(getResources().getString(R.string.trade_point_not_set));
		    	g.MyDatabase.m_refund_editing.distr_point_id.m_id="";
		    	g.MyDatabase.m_refund_editing.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
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
			break;
		}
		case OPEN_NOMENCLATURE_FROM_REFUND_REQUEST:
		if (bLinesPage)
		{
			if (resultCode==NomenclatureActivity.NOMENCLATURE_RESULT_DOCUMENT_CHANGED)
			{
				// 12.06.2018 в связи с изменениями в коде, считываем изменения
				Bundle bundle=data.getExtras();
				
				g.MyDatabase.m_refund_editing_id=bundle.getLong("refund_editing_id");
				g.MyDatabase.m_refund_editing_created=bundle.getBoolean("refund_editing_created");
				g.MyDatabase.m_refund_editing_modified=bundle.getBoolean("refund_editing_modified");
				
				g.MyDatabase.m_refund_editing=bundle.getParcelable("refund_editing");
				
				g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
				setModified();
				recalcWeight();
				redrawWeight();
			}
			break;
		}
		case RefundActivity.QUANTITY_REQUEST:
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
				    //double price=data.getDoubleExtra("price", 0.0);
				    //double price_k=data.getDoubleExtra("price_k", 1.0);
				    String ed=data.getStringExtra("ed");
				    String comment_in_line=data.getStringExtra("comment_in_line");
	      			
	  	    	    //long id=data.getLongExtra("id", 0);
	      			RefundLineRecord line;
	      			if (m_refund_editing_line_num<g.MyDatabase.m_refund_editing.lines.size())
	      			{
	      				line=g.MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
	      				if (!line.nomenclature_id.equals(nomenclature_id))
	      				{
	      					line=new RefundLineRecord();
	      				}
	      			} else
	      			{
	      				line=new RefundLineRecord();      				
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
						//line.discount=0.0;
						line.k=k;
						line.ed=ed;
						//if (price_k>-0.0001&&price_k<0.0001)
						//{
						//	price_k=1.0;
						//}
						//if (price_k-k>-0.0001&&price_k-k<0.0001)
						//{
						//	line.price=price;
						//} else
						//{
						//	line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
						//}
						//line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
						line.comment_in_line=comment_in_line;
						
						g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
						setModified();
						recalcWeight();
						redrawWeight();
						if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
						{
							ContentValues cv=new ContentValues();
							cv.put("client_id", ""); // в данном случае это не важно
							cv.put("quantity", quantity);
							cv.put("quantity_requested", quantity);
							cv.put("k", k);
							cv.put("ed", ed);
							//cv.put("price", price);
							cv.put("comment_in_line", comment_in_line);
							
							int cnt=getActivity().getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), nomenclature_id.toString()});
						}
					}
	      		}
	  		}
	  		if (resultCode==QuantityActivity.QUANTITY_RESULT_DELETE_LINE)
	  		{
	      		if (data!=null)
	      		{
				    MyID nomenclature_id=new MyID(data.getStringExtra("id"));
	      			RefundLineRecord line;
	      			if (m_refund_editing_line_num<g.MyDatabase.m_refund_editing.lines.size())
	      			{
	      				line=g.MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
	      				// на всякий случай проверка на совпадение номенклатуры
	      				if (line.nomenclature_id.equals(nomenclature_id))
	      				{
    	      				g.MyDatabase.m_refund_editing.lines.remove(line);
	      				}
	      			}
	      			g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
	      			setModified();
	      			recalcWeight();
	      			redrawWeight();
	      		}
	  		}
			break;
		}
		}
	}

	 */
	
	
		private void redrawWeight()
		{
			MySingleton g=MySingleton.getInstance();
			View view1=m_view;
			if (bLinesPage)
			{
				TextView tvStatistics=(TextView)view1.findViewById(R.id.textViewStatistics);
				if (g.MyDatabase.m_refund_editing.lines.size()==0)
				{
					tvStatistics.setText("0");
				} else
				{
					StringBuilder sb=new StringBuilder();
					sb.append(Integer.toString(g.MyDatabase.m_refund_editing.lines.size()));
					sb.append(": ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(g.MyDatabase.m_refund_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
					tvStatistics.setText(sb.toString());
				}
			}
		}

      protected void setModified()
      {
    	  MySingleton g=MySingleton.getInstance();
    	  if (!g.MyDatabase.m_refund_editing_modified)
    	  {
    		  getActivity().setTitle(R.string.title_activity_refund_changed);
    		  g.MyDatabase.m_refund_editing_modified=true;
    	  }
      }
      protected void recalcWeight()
      {
    	  MySingleton g=MySingleton.getInstance();
    	  g.MyDatabase.m_refund_editing.weightDoc=TextDatabase.GetRefundWeight(getActivity().getContentResolver(), g.MyDatabase.m_refund_editing, null, false);
      }
	  
	  public interface onSomeRefundEventListener {
		    public void someRefundEvent(String s);
		    public boolean onRefundClientSelected(View view0, long _id);
		    public boolean onRefundAgreementSelected(View view0, long _id);
		    //public boolean orderCanBeSaved(StringBuffer reason);
		  }
	    
		  onSomeRefundEventListener someEventListener;
		  
		  @Override
		  public void onAttach(Activity activity) {
		    super.onAttach(activity);
		        try {
		          someEventListener = (onSomeRefundEventListener) activity;
		        } catch (ClassCastException e) {
		            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
		        }
	  }	  
	  
	  static RefundPageFragment newInstance(int page) {
		RefundPageFragment pageFragment = new RefundPageFragment();
	    Bundle arguments = new Bundle();
	    arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
	    pageFragment.setArguments(arguments);
	    
	    return pageFragment;
	  }
	  
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
	    
	    Random rnd = new Random();
	    backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
	  }
	  
	  @Override
	  public View onCreateView(LayoutInflater inflater, ViewGroup container,
	      Bundle savedInstanceState) {

		  boolean orientationLandscape=(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE);
		  
		  MySingleton g=MySingleton.getInstance();

		  selectClientFromRefundActivityResultLauncher = registerForActivityResult(
				  new ActivityResultContracts.StartActivityForResult(),
				  new ActivityResultCallback<ActivityResult>() {
					  @Override
					  public void onActivityResult(ActivityResult result) {
						  if (result.getResultCode() == ClientsActivity.CLIENTS_RESULT_OK) {
							  Intent data = result.getData();
							  if (data != null && bHeaderPage) {
								  View view0=m_view;
								  long _id=data.getLongExtra("id", 0);
								  if (someEventListener.onRefundClientSelected(view0, _id)) {
									  setModified();
									  boolean bWrongAgreement = true;
									  // Проверим, принадлежит ли договор текущему контрагенту
									  Cursor agreementsCursor = getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.agreement_id.toString()}, null);
									  if (agreementsCursor.moveToNext()) {
										  int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
										  String owner_id = agreementsCursor.getString(owner_idIndex);
										  if (g.MyDatabase.m_refund_editing.client_id.toString().equals(owner_id))
											  bWrongAgreement = false;
									  }
									  agreementsCursor.close();
									  if (bWrongAgreement) {
										  // Не принадлежит, установим договор по умолчанию, если он единственный
										  Cursor defaultAgreementCursor = getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
										  if (defaultAgreementCursor != null && defaultAgreementCursor.moveToNext()) {
											  int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
											  Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
											  if (!defaultAgreementCursor.moveToNext()) {
												  // Есть единственный договор
												  someEventListener.onRefundAgreementSelected(view0, _idAgreement);
											  } else {
												  // Есть несколько договоров
												  someEventListener.onRefundAgreementSelected(view0, 0);
											  }
										  } else {
											  // Договора нет
											  someEventListener.onRefundAgreementSelected(view0, 0);
										  }
									  }
								  }
							  }
						  }
					  }
				  });

		  selectAgreementFromRefundActivityResultLauncher = registerForActivityResult(
				  new ActivityResultContracts.StartActivityForResult(),
				  new ActivityResultCallback<ActivityResult>() {
					  @Override
					  public void onActivityResult(ActivityResult result) {
						  if (result.getResultCode() == AgreementsActivity.AGREEMENTS_RESULT_OK) {
							  Intent data = result.getData();
							  if (data != null && bHeaderPage) {
								  View view0=m_view;
								  EditText et=(EditText)view0.findViewById(R.id.etAgreement);
								  TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewRefundOrganization);

								  long _id=data.getLongExtra("id", 0);
								  if (someEventListener.onRefundAgreementSelected(view0, _id))
								  {
									  setModified();
								  }
							  }
						  }
					  }
				  });

		  openNomenclatureFromRefundActivityResultLauncher = registerForActivityResult(
				  new ActivityResultContracts.StartActivityForResult(),
				  new ActivityResultCallback<ActivityResult>() {
					  @Override
					  public void onActivityResult(ActivityResult result) {
						  if (result.getResultCode() == NomenclatureActivity.NOMENCLATURE_RESULT_DOCUMENT_CHANGED) {
							  Intent data = result.getData();
							  if (data != null && bLinesPage) {
								  // 12.06.2018 в связи с изменениями в коде, считываем изменения
								  Bundle bundle=data.getExtras();

								  g.MyDatabase.m_refund_editing_id=bundle.getLong("refund_editing_id");
								  g.MyDatabase.m_refund_editing_created=bundle.getBoolean("refund_editing_created");
								  g.MyDatabase.m_refund_editing_modified=bundle.getBoolean("refund_editing_modified");

								  g.MyDatabase.m_refund_editing=bundle.getParcelable("refund_editing");

								  g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
								  setModified();
								  recalcWeight();
								  redrawWeight();
							  }
						  }
					  }
				  });

		  selectTradePointFromRefundActivityResultLauncher = registerForActivityResult(
				  new ActivityResultContracts.StartActivityForResult(),
				  new ActivityResultCallback<ActivityResult>() {
					  @Override
					  public void onActivityResult(ActivityResult result) {
						  if (result.getResultCode() == TradePointsActivity.TRADE_POINTS_RESULT_OK) {
							  Intent data = result.getData();
							  if (data != null && bHeaderPage) {
								  View view0=m_view;
								  EditText et=(EditText)view0.findViewById(R.id.etTradePoint);

								  setModified();

								  long _id=data.getLongExtra("id", 0);
								  Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, _id);
								  Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr", "address"}, null, null, null);
								  if (cursor.moveToNext())
								  {
									  int descrIndex = cursor.getColumnIndex("descr");
									  int idIndex = cursor.getColumnIndex("id");
									  //int addressIndex = cursor.getColumnIndex("address");
									  String descr = cursor.getString(descrIndex);
									  String tradePointId = cursor.getString(idIndex);
									  //String address = cursor.getString(addressIndex);
									  et.setText(descr);
									  g.MyDatabase.m_refund_editing.distr_point_id.m_id=tradePointId;
									  g.MyDatabase.m_refund_editing.stuff_distr_point_name=descr;
								  } else
								  {
									  et.setText(getResources().getString(R.string.trade_point_not_set));
									  g.MyDatabase.m_refund_editing.distr_point_id.m_id="";
									  g.MyDatabase.m_refund_editing.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
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
							  }
						  }
					  }
				  });


		  quantityRequestFromRefundActivityResultLauncher = registerForActivityResult(
				  new ActivityResultContracts.StartActivityForResult(),
				  new ActivityResultCallback<ActivityResult>() {
					  @Override
					  public void onActivityResult(ActivityResult result) {
						  int resultCode = result.getResultCode();
						  if (resultCode == QuantityActivity.QUANTITY_RESULT_OK) {
							  Intent data = result.getData();
							  if (data != null && bLinesPage) {
								  long _id=data.getLongExtra("_id", 0);
								  MyID nomenclature_id=new MyID(data.getStringExtra("id"));
								  double quantity=data.getDoubleExtra("quantity", 0.0);
								  double k=data.getDoubleExtra("k", 1.0);
								  //double price=data.getDoubleExtra("price", 0.0);
								  //double price_k=data.getDoubleExtra("price_k", 1.0);
								  String ed=data.getStringExtra("ed");
								  String comment_in_line=data.getStringExtra("comment_in_line");

								  //long id=data.getLongExtra("id", 0);
								  RefundLineRecord line;
								  if (m_refund_editing_line_num<g.MyDatabase.m_refund_editing.lines.size())
								  {
									  line=g.MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
									  if (!line.nomenclature_id.equals(nomenclature_id))
									  {
										  line=new RefundLineRecord();
									  }
								  } else
								  {
									  line=new RefundLineRecord();
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
									  //line.discount=0.0;
									  line.k=k;
									  line.ed=ed;
									  //if (price_k>-0.0001&&price_k<0.0001)
									  //{
									  //	price_k=1.0;
									  //}
									  //if (price_k-k>-0.0001&&price_k-k<0.0001)
									  //{
									  //	line.price=price;
									  //} else
									  //{
									  //	line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
									  //}
									  //line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
									  line.comment_in_line=comment_in_line;

									  g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
									  setModified();
									  recalcWeight();
									  redrawWeight();
									  if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
									  {
										  ContentValues cv=new ContentValues();
										  cv.put("client_id", ""); // в данном случае это не важно
										  cv.put("quantity", quantity);
										  cv.put("quantity_requested", quantity);
										  cv.put("k", k);
										  cv.put("ed", ed);
										  //cv.put("price", price);
										  cv.put("comment_in_line", comment_in_line);

										  int cnt=getActivity().getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), nomenclature_id.toString()});
									  }
								  }
							  }
						  } else if (resultCode==QuantityActivity.QUANTITY_RESULT_DELETE_LINE)
						  {
							  Intent data = result.getData();
							  if (data != null && bLinesPage) {
								  MyID nomenclature_id = new MyID(data.getStringExtra("id"));
								  RefundLineRecord line;
								  if (m_refund_editing_line_num < g.MyDatabase.m_refund_editing.lines.size()) {
									  line = g.MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
									  // на всякий случай проверка на совпадение номенклатуры
									  if (line.nomenclature_id.equals(nomenclature_id)) {
										  g.MyDatabase.m_refund_editing.lines.remove(line);
									  }
								  }
								  g.MyDatabase.m_refundLinesAdapter.notifyDataSetChanged();
								  setModified();
								  recalcWeight();
								  redrawWeight();
							  }
						  }
					  }
				  });

		  
		  bHeaderPage=false;
		  bLinesPage=false;
		  bSettingsPage=false;
		
		switch (pageNumber){
		case 0:
			if (orientationLandscape)
			{
				bHeaderPage=true;
				bLinesPage=true;
		    	m_view = inflater.inflate(R.layout.refund_header_and_lines, null);
			} else
			{
				bHeaderPage=true;
		    	m_view = inflater.inflate(R.layout.refund_header_fragment, null);
			}
			
	    	if (g.Common.PRODLIDER||g.Common.TANDEM)
	    	{
	    		FragmentManager fragMan = getFragmentManager();
	    		
				EquipmentRestsFragment myFrag = (EquipmentRestsFragment) fragMan.findFragmentByTag("fragmentEquipment");
				if (myFrag==null)
				{
		    		FragmentTransaction fragTransaction = fragMan.beginTransaction();
		    		myFrag = EquipmentRestsFragment.newInstance(getString(R.string.label_equipment), EquipmentRestsFragment.REFUNDS_EQUIPMENT_RESTS_LOADER_ID);
		    		fragTransaction.add(R.id.linearLayoutRefundHeaderDetails, myFrag, "fragmentEquipment");
		    		fragTransaction.commitAllowingStateLoss();
				}
				myFrag1=myFrag;
	    	}
	    	if (g.Common.TANDEM)
	    	{
	    		FragmentManager fragMan = getFragmentManager();
	    		
				EquipmentRestsFragment myFrag = (EquipmentRestsFragment) fragMan.findFragmentByTag("fragmentEquipmentTare");
				if (myFrag==null)
				{
		    		FragmentTransaction fragTransaction = fragMan.beginTransaction();
		    		myFrag = EquipmentRestsFragment.newInstance(getString(R.string.label_tare), EquipmentRestsFragment.REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID);
		    		fragTransaction.add(R.id.linearLayoutRefundHeaderDetails, myFrag, "fragmentEquipmentTare");
		    		fragTransaction.commitAllowingStateLoss();
				}
				myFrag2=myFrag;
	    	}
			break;
		case 1:
			if (orientationLandscape)
			{
				bSettingsPage=true;
				m_view = inflater.inflate(R.layout.refund_advanced_fragment, null);
			} else
			{
				bLinesPage=true;
				m_view = inflater.inflate(R.layout.refund_lines_fragment, null);
			}
			break;
		case 2:
			bSettingsPage=true;
			m_view = inflater.inflate(R.layout.refund_advanced_fragment, null);
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
		    TextView tvOrganizationName = (TextView) m_view.findViewById(R.id.textViewRefundOrganization);
		    tvOrganizationName.setVisibility(organizationVisibility);
		    // Заполняем данные шапки
			RefundRecord rec=g.MyDatabase.m_refund_editing;
			// Номер документа
			EditText etNumber = (EditText) m_view.findViewById(R.id.etNumber);
			etNumber.setText(rec.numdoc);
			// Дата документа
			EditText etDate = (EditText) m_view.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(Common.dateStringAsText(rec.datedoc));
			etDate.setText(sb.toString());
			
			// Клиент
			EditText etClient = (EditText) m_view.findViewById(R.id.etClient);
			etClient.setText(rec.stuff_client_name);
			// и его адрес
			TextView tvClientAddress = (TextView) m_view.findViewById(R.id.textViewRefundClientAddress);
			tvClientAddress.setText(rec.stuff_client_address);
			
			// Договор, организация договора
			TextView tvAgreement = (TextView)m_view.findViewById(R.id.textViewRefundAgreement);			
			EditText etAgreement = (EditText) m_view.findViewById(R.id.etAgreement);
			etAgreement.setText(rec.stuff_agreement_name);
			tvOrganizationName.setText(rec.stuff_organization_name);
			if (g.Common.MEGA||g.Common.PHARAOH)
			{
				tvOrganizationName.setVisibility(View.GONE);
				tvAgreement.setVisibility(View.GONE);
			}
			
			// Торговая точка
			EditText etTradePoint = (EditText) m_view.findViewById(R.id.etTradePoint);
			etTradePoint.setText(rec.stuff_distr_point_name);
			if (g.Common.MEGA||g.Common.PHARAOH||g.Common.TITAN||g.Common.FACTORY)
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
			
			
			// Результат
			TextView tvResult = (TextView)m_view.findViewById(R.id.textViewResult);
			EditText etResult = (EditText)m_view.findViewById(R.id.etResult);
			etResult.setText(rec.comment_closing);
			
			TextView textPriceType=(TextView)m_view.findViewById(R.id.textPriceType);
			EditText etPriceType=(EditText)m_view.findViewById(R.id.etPriceType);

			// Не знаю, зачем проверяю, у них и возрватов то не бывает
			if (g.Common.PHARAOH)
			{
				etDebt.setVisibility(View.GONE);
				etDebtPast.setVisibility(View.GONE);
				TextView textViewDebt = (TextView) m_view.findViewById(R.id.textViewDebt);
				TextView textViewDebtPast = (TextView) m_view.findViewById(R.id.textViewDebtPast);
				textViewDebt.setVisibility(View.GONE);
				textViewDebtPast.setVisibility(View.GONE);
				tvResult.setVisibility(View.GONE);
				etResult.setVisibility(View.GONE);
				// этого поля нет, может быть код был скопирован из реализации, кстати
				if (textPriceType!=null)
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
					if (!g.MyDatabase.m_refund_editing.comment.equals(s.toString()))
					{
						setModified();
						g.MyDatabase.m_refund_editing.comment=s.toString();
					}
				}
			});
			
			CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxRefundDontSend);
			if (g.MyDatabase.m_refund_editing.state!=E_REFUND_STATE.E_REFUND_STATE_CREATED&&g.MyDatabase.m_refund_editing.state!=E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
			{
				cbDontSend.setEnabled(false);
				if (g.MyDatabase.m_refund_editing.dont_need_send==1)
				{
					g.MyDatabase.m_refund_editing.dont_need_send=0;
				}
			} else
			{
				cbDontSend.setEnabled(true);
			}

			cbDontSend.setChecked(g.MyDatabase.m_refund_editing.dont_need_send==1);
			cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);		
			
			//
		    // Кнопка выбора даты
			final Button buttonSelectDate=(Button)m_view.findViewById(R.id.buttonRefundSelectDate);
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
						RefundRecord rec=g.MyDatabase.m_refund_editing;
						int day=Integer.parseInt(rec.datedoc.substring(6,8));
						int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
						int year=Integer.parseInt(rec.datedoc.substring(0,4));
					    // set date picker as current date
						//newFragment.setDialog(new DatePickerDialog(getActivity(), datePickerListener,
		                //         year, month,day));
						Bundle args=new Bundle();
						args.putInt("dialogId", RefundActivity.MY_ALERT_DIALOG_IDD_DATE);
						args.putInt("dialogType", OrderAlertDialogFragment.DIALOG_TYPE_DATE);
						args.putInt("day", day);
						args.putInt("month", month);
						args.putInt("year", year);
						newFragment.setArguments(args);
						newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});
		    
		    // Кнопка выбора клиента
			final Button buttonSelectClient=(Button)m_view.findViewById(R.id.buttonRefundSelectClient);
			buttonSelectClient.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), ClientsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
					//startActivityForResult(intent, SELECT_CLIENT_FROM_REFUND_REQUEST);
					selectClientFromRefundActivityResultLauncher.launch(intent);
				}
			});
			
		    // Кнопка выбора договора
			final Button buttonSelectAgreement=(Button)m_view.findViewById(R.id.buttonRefundSelectAgreement);
			buttonSelectAgreement.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), AgreementsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
					//startActivityForResult(intent, SELECT_AGREEMENT_FROM_REFUND_REQUEST);
					selectAgreementFromRefundActivityResultLauncher.launch(intent);
				}
			});
			
		    // Кнопка выбора торговой точки
			final Button buttonSelectTradePoint=(Button)m_view.findViewById(R.id.buttonRefundSelectTradePoint);
			buttonSelectTradePoint.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					MySingleton g=MySingleton.getInstance();
					Intent intent=new Intent(getActivity(), TradePointsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
					//startActivityForResult(intent, SELECT_TRADE_POINT_FROM_REFUND_REQUEST);
					selectTradePointFromRefundActivityResultLauncher.launch(intent);
				}
			});
			
			
			// Spinner организации
	        //ArrayAdapter<String> dataAdapterOrganizations = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_organizations_descr);
	        //dataAdapterOrganizations.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        //spinnerOrganization.setAdapter(dataAdapterOrganizations);
			
			// Spinner типы учета
			TextView tvAccounts=(TextView)m_view.findViewById(R.id.tvAccount);
			Spinner spinnerAccounts=(Spinner)m_view.findViewById(R.id.spinnerAccount);
	        ArrayAdapter<String> dataAdapterAccounts = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, RefundActivity.m_list_accounts_descr);
	        dataAdapterAccounts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerAccounts.setAdapter(dataAdapterAccounts);
	        int i;
	        for (i=0; i<RefundActivity.m_list_accounts_id.length; i++)
	        {
	        	if (Integer.parseInt(RefundActivity.m_list_accounts_id[i])==g.MyDatabase.m_refund_editing.bw.value())
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
					if (g.MyDatabase.m_refund_editing.bw.value()!=Integer.parseInt(RefundActivity.m_list_accounts_id[index]))
					{
						setModified();
						g.MyDatabase.m_refund_editing.bw=E_BW.fromInt(Integer.parseInt(RefundActivity.m_list_accounts_id[index]));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//g.MyDatabase.m_order_editing.bw=E_BW.E_BW_BOTH;
				}
			});
	        
	        if (g.Common.MEGA||g.Common.PHARAOH)
	        {
	        	View layoutAgreement=m_view.findViewById(R.id.layoutAgreement);
	        	layoutAgreement.setVisibility(View.GONE);
	        }
	        
			// Spinner склады
			Spinner spinnerStocks=(Spinner)m_view.findViewById(R.id.spinnerStock);
			//String[] data = {"one", "two", "three", "four", "five"};			
	        ArrayAdapter<String> dataAdapterStocks = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, RefundActivity.m_list_stocks_descr);
	        dataAdapterStocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerStocks.setAdapter(dataAdapterStocks);
	        for (i=0; i<RefundActivity.m_list_stocks_id.length; i++)
	        {
	        	if (RefundActivity.m_list_stocks_id[i].equals(g.MyDatabase.m_refund_editing.stock_id.toString()))
	        	{
	        		spinnerStocks.setSelection(i);
	        	}
	        }
	        spinnerStocks.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (!g.MyDatabase.m_refund_editing.stock_id.toString().equals(RefundActivity.m_list_stocks_id[index]))
					{
						setModified();
						g.MyDatabase.m_refund_editing.stock_id=new MyID(RefundActivity.m_list_stocks_id[index]);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//g.MyDatabase.m_order_editing.stock_id=new MyID();
				}
	        });
	        
	        if (g.Common.MEGA||g.Common.PHARAOH)
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

			int shipping_type=g.MyDatabase.m_refund_editing.shipping_type;
			if (shipping_type<0||shipping_type>=dataSpinnerShippingType.length)
				spinnerShippingType.setSelection(0);
			else
				spinnerShippingType.setSelection(shipping_type);

			spinnerShippingType.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					MySingleton g=MySingleton.getInstance();
					if (g.MyDatabase.m_refund_editing.shipping_type!=index)
					{
						setModified();
						g.MyDatabase.m_refund_editing.shipping_type=index;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});

	        // Куратор
	        final EditText etCurator=(EditText)m_view.findViewById(R.id.editTextCurator);
	        etCurator.setText(rec.stuff_curator_name);
	        if (g.Common.PHARAOH)
	        {
	        	etCurator.setVisibility(View.GONE);
		        TextView textViewCurator=(TextView)m_view.findViewById(R.id.textViewCurator);
		        textViewCurator.setVisibility(View.GONE);
	        }

	        // Состояние
	        final EditText etState=(EditText)m_view.findViewById(R.id.editTextState);
	        if (rec.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
	        {
	        	// Напишем, что состояние "создан"
	        	etState.setText(g.MyDatabase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_CREATED, getActivity().getApplicationContext()));
	        } else
	        {
	        	etState.setText(g.MyDatabase.GetRefundStateDescr(rec.state, getActivity().getApplicationContext()));
	        }
			
		    // Кнопка OK
			final Button buttonOk=(Button)m_view.findViewById(R.id.buttonRefundOk);
			if (buttonOk!=null)
			{
				buttonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//Intent intent=new Intent();
						//getActivity().setResult(RefundActivity.ORDER_RESULT_OK, intent);
						//getActivity().finish();
						someEventListener.someRefundEvent("ok");
					}
				});
			}
			
		    // Кнопка Cancel
			final Button buttonClose=(Button)m_view.findViewById(R.id.buttonRefundClose);
			if (buttonClose!=null)
			{
				buttonClose.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						someEventListener.someRefundEvent("close");
					}
				});
			}
			
			
	    }
		/////////////////////////////////////////////
		if (bLinesPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_lines_fragment, null);
	    	
	    	ListView lvRefundLines=(ListView)m_view.findViewById(R.id.listViewRefundLines);
		    registerForContextMenu(lvRefundLines);

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
		    	ExpandableListView lvSimple = (ExpandableListView) m_view.findViewById(R.id.listViewRefundLines);
				int defaultTextColor=Common.getColorFromAttr(getActivity(), R.attr.myTextColor);
		    	if (g.MyDatabase.m_refundLinesAdapter==null) {
					g.MyDatabase.m_refundLinesAdapter = new RefundExpListAdapter(getActivity().getApplicationContext(), defaultTextColor);
				}
    	        lvSimple.setAdapter(g.MyDatabase.m_refundLinesAdapter);
    	        
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
    	        g.MyDatabase.m_linesAdapter.setViewBinder(binder);
    	        */
    	        
    	        // Кнопка редактирования
    			final Button buttonEditLines=(Button)m_view.findViewById(R.id.buttonEditLines);
    			buttonEditLines.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					Intent intent=new Intent(getActivity(), NomenclatureActivity.class);
    					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    					intent.putExtra("MODE", "REFUND");
    					//startActivityForResult(intent, OPEN_NOMENCLATURE_FROM_REFUND_REQUEST);
						openNomenclatureFromRefundActivityResultLauncher.launch(intent);
    					//startActivity(intent);
    				}
    			});
    			
    			// Статистика документа
    			TextView tvStatistics=(TextView)m_view.findViewById(R.id.textViewStatistics);
    			if (g.MyDatabase.m_refund_editing.lines.size()==0)
    			{
    				tvStatistics.setText("0");
    			} else
    			{
    				StringBuilder sb=new StringBuilder();
    				sb.append(Integer.toString(g.MyDatabase.m_refund_editing.lines.size()));
    				sb.append(": ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(g.MyDatabase.m_refund_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
    				tvStatistics.setText(sb.toString());
    			}
	    }
	    if (bSettingsPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_advanced_fragment, null);
	    	
			final TextView tvLatitude = (TextView) m_view.findViewById(R.id.textViewRefundLatitudeValue);
			tvLatitude.setText(Double.toString(g.MyDatabase.m_refund_editing.latitude));
			final TextView tvLongitude = (TextView) m_view.findViewById(R.id.textViewRefundLongitudeValue);
			tvLongitude.setText(Double.toString(g.MyDatabase.m_refund_editing.longitude));
			final TextView tvDateCoord = (TextView) m_view.findViewById(R.id.textViewRefundDateCoordValue);
			tvDateCoord.setText(g.MyDatabase.m_refund_editing.datecoord);
	    	
	    	final CheckBox cbReceiveCoord=(CheckBox)m_view.findViewById(R.id.checkBoxRefundReceiveCoord);
	    	cbReceiveCoord.setChecked(g.MyDatabase.m_refund_editing.accept_coord==1);
	    	cbReceiveCoord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					MySingleton g=MySingleton.getInstance();
					setModified();
					g.MyDatabase.m_refund_editing.accept_coord=isChecked?1:0;
					someEventListener.someRefundEvent("coord");
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
				g.MyDatabase.m_refund_editing.dont_need_send=isChecked?1:0;
			}
		};
	  
	  private BroadcastReceiver onNotice= new BroadcastReceiver() {

		    @Override
		    public void onReceive(Context context, Intent intent) {
		        // intent can contain anydata
		        //Log.d("sohail","onReceive called");
		        //tv.setText("Broadcast received !");
				redrawWeight();

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
			
			IntentFilter iff= new IntentFilter(RefundActivity.ACTION);
	        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			MySingleton g=MySingleton.getInstance();
			switch (item.getItemId())
			{
			case R.id.action_change_quantity:
			if (bLinesPage)
			{
				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
				int pos=ExpandableListView.getPackedPositionGroup(info.packedPosition);
				
				RefundLineRecord line;
				
				line=g.MyDatabase.m_refund_editing.lines.get(pos);
				m_refund_editing_line_num=pos;
					
				Intent intent=new Intent(getActivity(), QuantityActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    intent.putExtra("id", line.nomenclature_id.toString());
			    intent.putExtra("quantity", line.quantity);
			    intent.putExtra("k", line.k);
			    intent.putExtra("ed", line.ed);
			    intent.putExtra("comment_in_line", line.comment_in_line);
			    intent.putExtra("MODE", "REFUND");
			    
			    // добавлено 30.11.2013, раньше было - только из списка номенклатуры
				ContentValues cv=new ContentValues();
				cv.put("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
			    getActivity().getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
			    //
			    
			    // TODO устанавливать только при изменении, см. также NomenclatureActitivity, там такой же код
			    TextDatabase.prepareNomenclatureStuff(getActivity().getContentResolver());

			    // Тандему остатки точно не нужны
			    if (!g.Common.TANDEM) {
					Cursor restCursor = getActivity().getContentResolver().query(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, new String[]{"nom_quantity"}, "nomenclature.id=?", new String[]{line.nomenclature_id.toString()}, null);
					if (restCursor.moveToFirst()) {
						intent.putExtra("rest", restCursor.getDouble(0));
					}
					restCursor.close();
				}

				//startActivityForResult(intent, RefundActivity.QUANTITY_REQUEST);
				quantityRequestFromRefundActivityResultLauncher.launch(intent);
				
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
					if (buttonPrint!=null&&Common.PHARAOH)
						buttonPrint.setVisibility(View.VISIBLE);
					if (buttonClose!=null)
						buttonClose.setVisibility(View.VISIBLE);
			    }
			}
		}
		*/
		
}
