package ru.code22.mtrade;

import java.util.Random;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import ru.code22.mtrade.DistribsLinesAdapter.onDistribsLinesDataChangedListener;
import ru.code22.mtrade.MyDatabase.DistribsLineRecord;
import ru.code22.mtrade.MyDatabase.DistribsRecord;
import ru.code22.mtrade.MyDatabase.MyID;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DistribsPageFragment extends Fragment implements onDistribsLinesDataChangedListener{
	
	//static final int SELECT_DATE_FROM_ORDER_REQUEST = 1;
    static final int SELECT_CLIENT_FROM_DISTRIBS_REQUEST = 2;
    //static final int SELECT_AGREEMENT_FROM_REFUND_REQUEST = 3;
    //static final int OPEN_CONTRACTS_FROM_DISTRIBS_REQUEST = 4;
    static final int SELECT_TRADE_POINT_FROM_DISTRIBS_REQUEST = 5;
	//static final int QUANTITY_REQUEST = 6;
    //static final int SELECT_DISCOUNT_FROM_ORDER_REQUEST = 7;
    //static final int SELECT_PLACES_FROM_ORDER_REQUEST = 8;
    
	static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    
	int pageNumber;
	int backColor;
	  
    View m_view;
    //SimpleAdapter sAdapter;
    //ArrayList<Map<String, Object>> data;
    
	// ???????? ???????????????????????? ?????????? ????????????, ?????????? ???????????????? ?????????????????????????? ???????????????????? ?? ????????????
	int m_distribs_editing_line_num;
	
	boolean bHeaderPage=false;
	boolean bLinesPage=false;
	boolean bSettingsPage=false;
	
	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
	@Override
	// when dialog box is closed, below method will be called.
	public void onDateSet(DatePicker view, int selectedYear,
			int selectedMonth, int selectedDay) {
		
	    DistribsRecord rec=MySingleton.getInstance().MyDatabase.m_distribs_editing;
		
		int day=Integer.parseInt(rec.datedoc.substring(6,8));
		int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
		int year=Integer.parseInt(rec.datedoc.substring(0,4));
		
		if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
		{
			rec.datedoc=Common.MyDateFormat("yyyyMMddHHmmss", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));
	
			View view0=m_view;
			EditText etDate = (EditText) view0.findViewById(R.id.etDate);
			StringBuilder sb=new StringBuilder();
			sb.append(Common.dateStringAsText(rec.datedoc));
			etDate.setText(sb.toString());
			
			setModified();
		}
		
	}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
	    if (data == null) 
	    	return;
	    
	    //Globals g=(Globals)getActivity().getApplication();
		
		switch (requestCode)
		{
		case SELECT_CLIENT_FROM_DISTRIBS_REQUEST:
		if (bHeaderPage)
		{
			View view0=m_view;
		    long _id=data.getLongExtra("id", 0);
			if (someEventListener.onDistribsClientSelected(view0, _id))
			{
				setModified();
				/*
				boolean bWrongAgreement=true;
				// ????????????????, ?????????????????????? ???? ?????????????? ???????????????? ?????????????????????? 
		        Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{MyDatabase.m_refund_editing.agreement_id.toString()}, null);
		        if (agreementsCursor.moveToNext())
		        {
		        	int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
		        	String owner_id = agreementsCursor.getString(owner_idIndex);
		        	if (MyDatabase.m_refund_editing.client_id.toString().equals(owner_id))
		        		bWrongAgreement=false;
		        }
		        agreementsCursor.close();
		        if (bWrongAgreement)
		        {
	        		// ???? ??????????????????????, ?????????????????? ?????????????? ???? ??????????????????, ???????? ???? ????????????????????????
	    	        Cursor defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{MyDatabase.m_refund_editing.client_id.toString()}, null);
	    	        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
	    	        {
	    	        	int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
	    	        	Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
	    	        	if (!defaultAgreementCursor.moveToNext())
	    	        	{
	    	        		// ???????? ???????????????????????? ??????????????
	    	        		someEventListener.onRefundAgreementSelected(view0, _idAgreement);
	    	        	} else
	    	        	{
	    	        		// ???????? ?????????????????? ??????????????????
	    	        		someEventListener.onRefundAgreementSelected(view0, 0);
	    	        	}
	    	        } else
	    	        {
	    	        	// ???????????????? ??????
	    	        	someEventListener.onRefundAgreementSelected(view0, 0);
	    	        }
		        }
		        */
			}
			break;
		}
		/*
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
		*/
		case SELECT_TRADE_POINT_FROM_DISTRIBS_REQUEST:
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
				MySingleton.getInstance().MyDatabase.m_distribs_editing.distr_point_id.m_id=tradePointId;
				MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_distr_point_name=descr;
		    } else
		    {
		    	et.setText(getResources().getString(R.string.trade_point_not_set));
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.distr_point_id.m_id="";
		    	MySingleton.getInstance().MyDatabase.m_distribs_editing.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
		    }
		    cursor.close();
			break;
		}
		/*
		case OPEN_CONTRACTS_FROM_DISTRIBS_REQUEST:
		if (bLinesPage)
		{
			if (resultCode==ContractsActivity.CONTRACTS_RESULT_DOCUMENT_CHANGED)
			{
				MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
				setModified();
				recalcWeight();
				redrawWeight();
			}
			break;
		}
		*/
		case DistribsActivity.QUANTITY_SIMPLE_REQUEST:
		if (bLinesPage)
		{
			if (resultCode==QuantitySimpleActivity.QUANTITY_SIMPLE_RESULT_OK)
			{
	      		if (data!=null)
	      		{
				    long _id=data.getLongExtra("_id", 0);
				    MyID nomenclature_id=new MyID(data.getStringExtra("id"));
				    double quantity=data.getDoubleExtra("quantity", 0.0);
				    
					DistribsLineRecord line=MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.get(m_distribs_editing_line_num);
					line.quantity=quantity;
					MySingleton.getInstance().MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
					setModified();
	      		}
			}
			/*
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
	      			DistribsLineRecord line;
	      			if (m_refund_editing_line_num<MyDatabase.m_refund_editing.lines.size())
	      			{
	      				line=MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
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
						// nomenclature_id ???? ????????????, ???? ????????????????????
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
						
						MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
						setModified();
						recalcWeight();
						redrawWeight();
						if (MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
						{
							ContentValues cv=new ContentValues();
							cv.put("client_id", ""); // ?? ???????????? ???????????? ?????? ???? ??????????
							cv.put("quantity", quantity);
							cv.put("quantity_requested", quantity);
							cv.put("k", k);
							cv.put("ed", ed);
							//cv.put("price", price);
							cv.put("comment_in_line", comment_in_line);
							
							int cnt=getActivity().getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(MyDatabase.m_refund_editing_id), nomenclature_id.toString()});
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
	      			if (m_refund_editing_line_num<MyDatabase.m_refund_editing.lines.size())
	      			{
	      				line=MyDatabase.m_refund_editing.lines.get(m_refund_editing_line_num);
	      				// ???? ???????????? ???????????? ???????????????? ???? ???????????????????? ????????????????????????
	      				if (line.nomenclature_id.equals(nomenclature_id))
	      				{
    	      				MyDatabase.m_refund_editing.lines.remove(line);
	      				}
	      			}
	      			MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
	      			setModified();
	      			recalcWeight();
	      			redrawWeight();
	      		}
	  		}
			break;
			*/
		}
		}
	}
	
	
		private void redrawWeight()
		{
			Globals g=(Globals)getActivity().getApplication();
			View view1=m_view;
			if (bLinesPage)
			{
				TextView tvStatistics=(TextView)view1.findViewById(R.id.textViewStatistics);
				if (MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.size()==0)
				{
					tvStatistics.setText("0");
				} else
				{
					StringBuilder sb=new StringBuilder();
					sb.append(Integer.toString(MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.size()));
					//sb.append(": ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(MyDatabase.m_refund_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
					tvStatistics.setText(sb.toString());
				}
			}
		}

      protected void setModified()
      {
          MySingleton g=MySingleton.getInstance();
    	  if (!g.MyDatabase.m_distribs_editing_modified)
    	  {
    		  getActivity().setTitle(R.string.title_activity_distribs_changed);
    		  g.MyDatabase.m_distribs_editing_modified=true;
    	  }
      }
      protected void recalcWeight()
      {
    	  //MyDatabase.m_refund_editing.weightDoc=TextDatabase.GetRefundWeight(getActivity().getContentResolver(), MyDatabase.m_refund_editing, null, false);
      }
	  
	  public interface onSomeDistribsEventListener {
		    public void someDistribsEvent(String s);
		    public boolean onDistribsClientSelected(View view0, long _id);
		    //public boolean onDistribsAgreementSelected(View view0, long _id);
		    //public boolean orderCanBeSaved(StringBuffer reason);
		  }
	    
		  onSomeDistribsEventListener someEventListener;
		  
		  @Override
		  public void onAttach(Activity activity) {
		    super.onAttach(activity);
		        try {
		          someEventListener = (onSomeDistribsEventListener) activity;
		        } catch (ClassCastException e) {
		            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
		        }
	  }	  
	  
	  static DistribsPageFragment newInstance(int page) {
		DistribsPageFragment pageFragment = new DistribsPageFragment();
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
		  
		  //Globals g=(Globals)getActivity().getApplication();
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
		    	m_view = inflater.inflate(R.layout.distribs_header_and_lines, null);
			} else
			{
				bHeaderPage=true;
		    	m_view = inflater.inflate(R.layout.distribs_header_fragment, null);
			}
			break;
		case 1:
			if (orientationLandscape)
			{
				bSettingsPage=true;
				m_view = inflater.inflate(R.layout.distribs_advanced_fragment, null);
			} else
			{
				bLinesPage=true;
				m_view = inflater.inflate(R.layout.distribs_lines_fragment, null);
			}
			break;
		case 2:
			bSettingsPage=true;
			m_view = inflater.inflate(R.layout.distribs_advanced_fragment, null);
			break;
			/*
		case 3: // ???? ?????????? ???????? ?????? ???????????????? 0, ???? ????????????????
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
	    	// ?????????????????? ????????????
	    	//int organizationVisibility=View.VISIBLE;
	    	// ?????????????????? ??????????????????????
	    	//if (MyDatabase.m_organizations_version==0||Common.MEGA)
	    	//{
	    	//	organizationVisibility=View.GONE;
	    	//}
		    //TextView tvOrganizationName = (TextView) m_view.findViewById(R.id.textViewDistribsOrganization);
		    //tvOrganizationName.setVisibility(organizationVisibility);
		    // ?????????????????? ???????????? ??????????
			DistribsRecord rec=MySingleton.getInstance().MyDatabase.m_distribs_editing;
			// ?????????? ??????????????????
			EditText etNumber = (EditText) m_view.findViewById(R.id.etNumber);
			etNumber.setText(rec.numdoc);
			// ?????????? ??????????????????
			EditText etTime = (EditText) m_view.findViewById(R.id.etDistribsTime);
			StringBuilder sb=new StringBuilder();
			sb.append(rec.datedoc.substring(8,10)+":"+rec.datedoc.substring(10,12));
			etTime.setText(sb.toString());
			// ???????? ??????????????????
			EditText etDate = (EditText) m_view.findViewById(R.id.etDistribsDate);
			sb.setLength(0);
			sb.append(Common.dateStringAsText(rec.datedoc));
			etDate.setText(sb.toString());
			
			// ????????????
			EditText etClient = (EditText) m_view.findViewById(R.id.etClient);
			etClient.setText(rec.stuff_client_name);
			// ?? ?????? ??????????
			TextView tvClientAddress = (TextView) m_view.findViewById(R.id.textViewDistribsClientAddress);
			tvClientAddress.setText(rec.stuff_client_address);
			
	    	// TODO
	    	/*
			// ??????????????, ?????????????????????? ????????????????
			TextView tvAgreement = (TextView)m_view.findViewById(R.id.textViewRefundAgreement);			
			EditText etAgreement = (EditText) m_view.findViewById(R.id.etAgreement);
			etAgreement.setText(rec.stuff_agreement_name);
			tvOrganizationName.setText(rec.stuff_organization_name);
			if (Common.MEGA||Common.PHARAON)
			{
				tvOrganizationName.setVisibility(View.GONE);
				tvAgreement.setVisibility(View.GONE);
			}
			*/
			
			// ???????????????? ??????????
			EditText etTradePoint = (EditText) m_view.findViewById(R.id.etTradePoint);
			etTradePoint.setText(rec.stuff_distr_point_name);
			if (MySingleton.getInstance().Common.MEGA||MySingleton.getInstance().Common.PHARAON||MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
			{
				TextView tvTradePoint = (TextView) m_view.findViewById(R.id.textViewTradePoint);
				View layoutTradePoint = m_view.findViewById(R.id.layoutTradePoint);
				tvTradePoint.setVisibility(View.GONE);
				layoutTradePoint.setVisibility(View.GONE);
			}
			
			// ????????
			EditText etDebt = (EditText) m_view.findViewById(R.id.editTextDebt);
			etDebt.setText(String.format("%.2f", rec.stuff_debt));
			EditText etDebtPast = (EditText) m_view.findViewById(R.id.editTextDebtPast);
			etDebtPast.setText(String.format("%.2f", rec.stuff_debt_past));
			EditText etDebtPast30 = (EditText) m_view.findViewById(R.id.editTextDebtPast30);
			etDebtPast30.setText(String.format("%.2f", rec.stuff_debt_past30));

			/*
			// ??????????????????
			TextView tvResult = (TextView)m_view.findViewById(R.id.textViewResult);
			EditText etResult = (EditText)m_view.findViewById(R.id.etResult);
			etResult.setText(rec.comment_closing);
			
			TextView textPriceType=(TextView)m_view.findViewById(R.id.textPriceType);
			EditText etPriceType=(EditText)m_view.findViewById(R.id.etPriceType);
			
			if (Common.PHARAON)
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
			*/
			
			// ??????????????????????
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
					Globals g=(Globals)getActivity().getApplication();
					if (!MySingleton.getInstance().MyDatabase.m_refund_editing.comment.equals(s.toString()))
					{
						setModified();
						MySingleton.getInstance().MyDatabase.m_refund_editing.comment=s.toString();
					}
				}
			});
			
			/*
			CheckBox cbDontSend=(CheckBox) m_view.findViewById(R.id.checkBoxRefundDontSend);
			if (MyDatabase.m_refund_editing.state!=E_REFUND_STATE.E_REFUND_STATE_CREATED&&MyDatabase.m_refund_editing.state!=E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
			{
				cbDontSend.setEnabled(false);
				if (MyDatabase.m_refund_editing.dont_need_send==1)
				{
					MyDatabase.m_refund_editing.dont_need_send=0;
				}
			} else
			{
				cbDontSend.setEnabled(true);
			}
			
			
			cbDontSend.setChecked(MyDatabase.m_refund_editing.dont_need_send==1);
			cbDontSend.setOnCheckedChangeListener(cbDontSendOnCheckedChangeListener);		
			
			//
		    // ???????????? ???????????? ????????
			final Button buttonSelectDate=(Button)m_view.findViewById(R.id.buttonRefundSelectDate);
			buttonSelectDate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
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
						RefundRecord rec=MyDatabase.m_refund_editing;
						int day=Integer.parseInt(rec.datedoc.substring(6,8));
						int month=Integer.parseInt(rec.datedoc.substring(4,6))-1;
						int year=Integer.parseInt(rec.datedoc.substring(0,4));
					    // set date picker as current date
						newFragment.setDialog(new DatePickerDialog(getActivity(), datePickerListener, 
		                         year, month,day));
						newFragment.show(getFragmentManager(), "dialog");
					}
				}
			});
			*/
		    
		    // ???????????? ???????????? ??????????????
			final Button buttonSelectClient=(Button)m_view.findViewById(R.id.buttonDistribsSelectClient);
			buttonSelectClient.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(getActivity(), ClientsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.toString());
					startActivityForResult(intent, SELECT_CLIENT_FROM_DISTRIBS_REQUEST);
				}
			});
			
		    // ???????????? ???????????? ???????????????? ??????????
			final Button buttonSelectTradePoint=(Button)m_view.findViewById(R.id.buttonDistribsSelectTradePoint);
			buttonSelectTradePoint.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Globals g=(Globals)getActivity().getApplication();
					Intent intent=new Intent(getActivity(), TradePointsActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("client_id", MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.toString());
					startActivityForResult(intent, SELECT_TRADE_POINT_FROM_DISTRIBS_REQUEST);
				}
			});
			
			/*
			// Spinner ??????????????????????
	        //ArrayAdapter<String> dataAdapterOrganizations = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, OrderActivity.m_list_organizations_descr);
	        //dataAdapterOrganizations.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        //spinnerOrganization.setAdapter(dataAdapterOrganizations);
			
			// Spinner ???????? ??????????
			TextView tvAccounts=(TextView)m_view.findViewById(R.id.tvAccount);
			Spinner spinnerAccounts=(Spinner)m_view.findViewById(R.id.spinnerAccount);
	        ArrayAdapter<String> dataAdapterAccounts = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, RefundActivity.m_list_accounts_descr);
	        dataAdapterAccounts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerAccounts.setAdapter(dataAdapterAccounts);
	        int i;
	        for (i=0; i<RefundActivity.m_list_accounts_id.length; i++)
	        {
	        	if (Integer.parseInt(RefundActivity.m_list_accounts_id[i])==MyDatabase.m_refund_editing.bw.value())
	        	{
	        		spinnerAccounts.setSelection(i);
	        	}
	        }
			if (rec.stuff_select_account||Common.MEGA)
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
					if (MyDatabase.m_refund_editing.bw.value()!=Integer.parseInt(RefundActivity.m_list_accounts_id[index]))
					{
						setModified();
						MyDatabase.m_refund_editing.bw=E_BW.fromInt(Integer.parseInt(RefundActivity.m_list_accounts_id[index]));
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//MyDatabase.m_order_editing.bw=E_BW.E_BW_BOTH;
				}
			});
	        
	        if (Common.MEGA||Common.PHARAON)
	        {
	        	View layoutAgreement=m_view.findViewById(R.id.layoutAgreement);
	        	layoutAgreement.setVisibility(View.GONE);
	        }
	        
			// Spinner ????????????
			Spinner spinnerStocks=(Spinner)m_view.findViewById(R.id.spinnerStock);
			//String[] data = {"one", "two", "three", "four", "five"};			
	        ArrayAdapter<String> dataAdapterStocks = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, RefundActivity.m_list_stocks_descr);
	        dataAdapterStocks.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinnerStocks.setAdapter(dataAdapterStocks);
	        for (i=0; i<RefundActivity.m_list_stocks_id.length; i++)
	        {
	        	if (RefundActivity.m_list_stocks_id[i].equals(MyDatabase.m_refund_editing.stock_id.toString()))
	        	{
	        		spinnerStocks.setSelection(i);
	        	}
	        }
	        spinnerStocks.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3)
				{
					if (!MyDatabase.m_refund_editing.stock_id.toString().equals(RefundActivity.m_list_stocks_id[index]))
					{
						setModified();
						MyDatabase.m_refund_editing.stock_id=new MyID(RefundActivity.m_list_stocks_id[index]);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					//setModified();
					//MyDatabase.m_order_editing.stock_id=new MyID();
				}
	        });
	        
	        if (Common.MEGA||Common.PHARAON)
	        {
	        	View tvStock=m_view.findViewById(R.id.textViewStock);
	        	tvStock.setVisibility(View.GONE);
	        	spinnerStocks.setVisibility(View.GONE);
	        }
	        */
	        
	        // ??????????????
	        final EditText etCurator=(EditText)m_view.findViewById(R.id.editTextCurator);
	        etCurator.setText(rec.stuff_curator_name);

	        // ??????????????????
	        final EditText etState=(EditText)m_view.findViewById(R.id.editTextState);
        	etState.setText(MyDatabase.GetDistribsStateDescr(rec.state, getActivity().getApplicationContext()));
			
		    // ???????????? OK
			final Button buttonOk=(Button)m_view.findViewById(R.id.buttonDistribsOk);
			if (buttonOk!=null)
			{
				buttonOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//Intent intent=new Intent();
						//getActivity().setResult(RefundActivity.ORDER_RESULT_OK, intent);
						//getActivity().finish();
						someEventListener.someDistribsEvent("ok");
					}
				});
			}
			
		    // ???????????? Cancel
			final Button buttonClose=(Button)m_view.findViewById(R.id.buttonDistribsClose);
			if (buttonClose!=null)
			{
				buttonClose.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						someEventListener.someDistribsEvent("close");
					}
				});
			}
			
			
	    }
		/////////////////////////////////////////////
		if (bLinesPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_lines_fragment, null);
	    	
	    	ListView lvDistribsLines=(ListView)m_view.findViewById(R.id.listViewDistribsLines);
	    	if (MySingleton.getInstance().Common.DISTRIBS_BY_QUANTITY)
	    	{
	    	lvDistribsLines.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					//Globals g=(Globals)getActivity().getApplication();
                    MySingleton g=MySingleton.getInstance();
					//Toast.makeText(getActivity(), "TEST", Toast.LENGTH_LONG).show();
					
					Intent intent=new Intent(getActivity(), QuantitySimpleActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					
				    //intent.putExtra("id", line.nomenclature_id.toString());
				    //intent.putExtra("quantity", line.quantity);
				    //intent.putExtra("k", line.k);
				    //intent.putExtra("ed", line.ed);
				    //intent.putExtra("comment_in_line", line.comment_in_line);
				    //intent.putExtra("MODE", "REFUND");
					
					m_distribs_editing_line_num=position;
					DistribsLineRecord line=g.MyDatabase.m_distribs_editing.lines.get(position);
					intent.putExtra("description", line.stuff_distribs_contract);
					intent.putExtra("quantity", line.quantity);
					startActivityForResult(intent, DistribsActivity.QUANTITY_SIMPLE_REQUEST);
					
					/*
					m_distribs_editing_line_num=position;
					
					if (getFragmentManager().findFragmentByTag("dialog") == null)
					{
						OrderAlertDialogFragment newFragment = new OrderAlertDialogFragment();
						
						DistribsLineRecord line=MyDatabase.m_distribs_editing.lines.get(position);
						int value=(int)(line.quantity+0.001);
						
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						final NumberPicker np=new NumberPicker(getActivity());
						np.setMinValue(0);
		                np.setMaxValue(30);						
		                np.setValue(value);
						
						final FrameLayout listFrame = new FrameLayout(getActivity());
						listFrame.addView(np, new FrameLayout.LayoutParams(
						        FrameLayout.LayoutParams.WRAP_CONTENT,
						        FrameLayout.LayoutParams.WRAP_CONTENT,
						        Gravity.CENTER));
						
		                builder.setTitle(R.string.SKU);
						builder.setView(listFrame);
						builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int id) {
								//Intent intent = new Intent();
							    //intent.putExtra("id", m_client_id);
							    //setResult(RESULT_OK, intent);
							    //finish();
								DistribsLineRecord line=MyDatabase.m_distribs_editing.lines.get(m_distribs_editing_line_num);
								line.quantity=np.getValue();
								MyDatabase.m_distribsLinesAdapter.notifyDataSetChanged();
								setModified();
							}
						});
						builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						
						builder.setCancelable(false);
						newFragment.setDialog(builder.create());
						newFragment.show(getFragmentManager(), "dialog");
					}
					*/
						// TODO
						/*
							
						Intent intent=new Intent(getActivity(), QuantityActivity.class);
							
					    intent.putExtra("id", line.nomenclature_id.toString());
					    intent.putExtra("quantity", line.quantity);
					    intent.putExtra("k", line.k);
					    intent.putExtra("ed", line.ed);
					    intent.putExtra("comment_in_line", line.comment_in_line);
					    intent.putExtra("MODE", "REFUND");
					    
					    // ?????????????????? 30.11.2013, ???????????? ???????? - ???????????? ???? ???????????? ????????????????????????
						ContentValues cv=new ContentValues();
						cv.put("client_id", MyDatabase.m_refund_editing.client_id.toString());
					    getActivity().getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
					    //
					    
					    // TODO ?????????????????????????? ???????????? ?????? ??????????????????, ????. ?????????? NomenclatureActitivity, ?????? ?????????? ???? ??????
					    TextDatabase.prepareNomenclatureStuff(getActivity().getContentResolver());
					    
					    Cursor restCursor=getActivity().getContentResolver().query(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, new String[]{"nom_quantity"}, "nomenclature.id=?", new String[]{line.nomenclature_id.toString()}, null);
					    if (restCursor.moveToFirst())
					    {
						    intent.putExtra("rest", restCursor.getDouble(0));
					    }
					    restCursor.close();
						startActivityForResult(intent, RefundActivity.QUANTITY_REQUEST);
						
					     */
					
					
				}
			});
	    	}
		    registerForContextMenu(lvDistribsLines);

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
	    	
    	    	// ???????????? ???????? ??????????????????, ???? ?????????????? ?????????? ???????????????? ????????????
    	        //String[] from = { ATTRIBUTE_NAME_NOMENCLATURE, ATTRIBUTE_NAME_QUANTITY,	ATTRIBUTE_NAME_SUM };
    	        // ???????????? ID View-??????????????????????, ?? ?????????????? ?????????? ?????????????????? ????????????
    	        //int[] to = { R.id.textViewOrderLineNomenclature, R.id.textViewOrderLineQuantity, R.id.textViewOrderLineSum };

    	        // ?????????????? ??????????????
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
		    	ListView lvSimple = (ListView) m_view.findViewById(R.id.listViewDistribsLines);
		    	g.MyDatabase.m_distribsLinesAdapter = new DistribsLinesAdapter(getActivity().getApplicationContext(), MySingleton.getInstance().MyDatabase.m_distribs_editing.lines);
	    		// ????????????, ?? ????????????????, ?????? ???????????? ????????????????????, ???? ????????????????
	    		// ?? ?????????????? ???????????????? ?????????????????????? ?????? ?????? - ???? ????????????????
		    	g.MyDatabase.m_distribsLinesAdapter.setOnDistribsLinesDataChangedListener(this);
    	        lvSimple.setAdapter(g.MyDatabase.m_distribsLinesAdapter);

                if (g.Common.m_app_theme.equals("DARK"))
                    g.MyDatabase.m_distribsLinesAdapter.setMyTextColor(Color.WHITE);
                else
                    g.MyDatabase.m_distribsLinesAdapter.setMyTextColor(Color.BLACK);

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
    	        
    	        // ???????????? ????????????????????????????
    			final Button buttonEditLines=(Button)m_view.findViewById(R.id.buttonEditLines);
    			buttonEditLines.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View v) {
    					Intent intent=new Intent(getActivity(), NomenclatureActivity.class);
    					//intent.putExtra("MODE", "REFUND");
    					startActivityForResult(intent, OPEN_CONTRACTS_FROM_DISTRIBS_REQUEST);
    					//startActivity(intent);
    				}
    			});
    	        */
    			
    			// ???????????????????? ??????????????????
    			TextView tvStatistics=(TextView)m_view.findViewById(R.id.textViewStatistics);
    			if (MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.size()==0)
    			{
    				tvStatistics.setText("0");
    			} else
    			{
    				StringBuilder sb=new StringBuilder();
    				sb.append(Integer.toString(MySingleton.getInstance().MyDatabase.m_distribs_editing.lines.size()));
    				//sb.append(": ").append(getString(R.string.weight)).append(" ").append(Common.DoubleToStringFormat(MyDatabase.m_refund_editing.weightDoc, "%.3f")).append(getString(R.string.default_weight));
    				tvStatistics.setText(sb.toString());
    			}
	    }
	    if (bSettingsPage)
	    {
	    	//m_view = inflater.inflate(R.layout.order_advanced_fragment, null);
	    	
			final TextView tvLatitude = (TextView) m_view.findViewById(R.id.textViewDistribsLatitudeValue);
			tvLatitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_distribs_editing.latitude));
			final TextView tvLongitude = (TextView) m_view.findViewById(R.id.textViewDistribsLongitudeValue);
			tvLongitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_distribs_editing.longitude));
			final TextView tvDateCoord = (TextView) m_view.findViewById(R.id.textViewDistribsDateCoordValue);
			tvDateCoord.setText(MySingleton.getInstance().MyDatabase.m_distribs_editing.datecoord);
	    	
	    	final CheckBox cbReceiveCoord=(CheckBox)m_view.findViewById(R.id.checkBoxDistribsReceiveCoord);
	    	cbReceiveCoord.setChecked(MySingleton.getInstance().MyDatabase.m_distribs_editing.accept_coord==1);
	    	cbReceiveCoord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					setModified();
					MySingleton.getInstance().MyDatabase.m_distribs_editing.accept_coord=isChecked?1:0;
					someEventListener.someDistribsEvent("coord");
				}
			});
	    }
	    
	    return m_view;
	  }
	  
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
			//IntentFilter iff= new IntentFilter(RefundActivity.ACTION);
	        //LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice, iff);		
		}

		@Override
		public boolean onContextItemSelected(MenuItem item) {
			return super.onContextItemSelected(item);
		}


		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
			
		}


		@Override
		public void onDistribsLinesDataChanged() {
			setModified();
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
					if (buttonPrint!=null&&Common.PHARAON)
						buttonPrint.setVisibility(View.VISIBLE);
					if (buttonClose!=null)
						buttonClose.setVisibility(View.VISIBLE);
			    }
			}
		}
		*/
		
}
