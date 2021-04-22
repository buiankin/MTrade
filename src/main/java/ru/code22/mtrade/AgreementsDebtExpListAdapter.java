package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.SaldoExtRecord;
import ru.code22.mtrade.MyDatabase.SalesHistoryRecord;
import ru.code22.mtrade.OrderExpPhListAdapter.GroupData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class AgreementsDebtExpListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private String m_client_id;
	
	class GroupData
	{
		String agreement_id;
		String agreement_descr;
		//String organization_id;
		String organization_descr;
		String pricetype_descr;
		String manager_id;
		//String manager_id_to_search; // в случае, если менеджер недоступен, он здесь будет указан, а в предыдущей строке - пустой
		String manager_descr;
        
		double debt;
		double debtPast;
		ArrayList<SaldoExtRecord> details;
	}
	
	public ArrayList<GroupData> mGroups;
	
	public AgreementsDebtExpListAdapter(Context context, String clientId)
	{
		mContext=context;
		m_client_id=clientId;
		mGroups=new ArrayList<GroupData>();
	}
	
	public void clearSalesHistory()
	{
		//m_sales.clear();
	}
	
	public void init()
	{
		Cursor cursor=mContext.getContentResolver().query(MTradeContentProvider.AGREEMENTS_LIST_CONTENT_URI, new String[]{"agreement_id","agreement_descr","organization_descr","pricetype_descr","default_manager_id"}, "owner_id=?", new String[]{m_client_id}, "agreement_descr, agreements.id");
		int indexAgreementId=cursor.getColumnIndex("agreement_id");
		int indexDefaultManagerId=cursor.getColumnIndex("default_manager_id");
		int indexAgreementDecr=cursor.getColumnIndex("agreement_descr");
		//int indexOrganizationId=cursor.getColumnIndex("organization_id");
		int indexOrganizationDescr=cursor.getColumnIndex("organization_descr");
		int indexPriceTypeDescr=cursor.getColumnIndex("pricetype_descr");
		while (cursor.moveToNext())
		{
			GroupData data=new GroupData();
			data.agreement_id=cursor.getString(indexAgreementId);
			data.agreement_descr=cursor.getString(indexAgreementDecr);
			//data.organization_id=cursor.getString(indexOrganizationId);
			data.organization_descr=cursor.getString(indexOrganizationDescr);
			data.pricetype_descr=cursor.getString(indexPriceTypeDescr);
			data.manager_id=cursor.getString(indexDefaultManagerId);
			//data.manager_id_to_search=data.manager_id;
			if (data.manager_id==null)
			{
				// такое есть вроде в промежуточных версиях 3.38-3.39, поэтому проверяем
				data.manager_id="";
				data.manager_descr="";
			} else
			{
				Cursor cursorManager=mContext.getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{data.manager_id}, null);
				if (cursorManager.moveToNext())
				{
					data.manager_descr=cursorManager.getString(0);
				} else
				{
					data.manager_id="";
					data.manager_descr="";
				}
				cursorManager.close();
			}
			data.debt=0.0;
			data.debtPast=0.0;
			data.details=new ArrayList<SaldoExtRecord>();
			mGroups.add(data);
		}
		cursor.close();
		//
		cursor=mContext.getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_JOURNAL_CONTENT_URI, 
				new String[]{"agreement_id", "agreement_descr", "document_id", "manager_id", "document_descr", "manager_descr", "organization_descr", "saldo", "saldo_past"}, "client_id=?", new String[]{m_client_id}, "agreement_descr, agreement_id, document_datetime, document_descr");
		//int indexSaldoClientId=cursor.getColumnIndex("client_id");
		int indexSaldoAgreementId=cursor.getColumnIndex("agreement_id");
		int indexSaldoAgreementDescr=cursor.getColumnIndex("agreement_descr");
		int indexSaldoDocumentId=cursor.getColumnIndex("document_id");
		int indexSaldoManagerId=cursor.getColumnIndex("manager_id");
		int indexSaldoDocumentDescr=cursor.getColumnIndex("document_descr");
		//int indexSaldoOrganizationId=cursor.getColumnIndex("organization_id");
		int indexSaldoOrganizationDescr=cursor.getColumnIndex("organization_descr");
		int indexSaldoManagerDescr=cursor.getColumnIndex("manager_descr");
		int indexSaldoDebt=cursor.getColumnIndex("saldo");
		int indexSaldoDebtPast=cursor.getColumnIndex("saldo_past");
		
		while (cursor.moveToNext())
		{
			String agreement_id=cursor.getString(indexSaldoAgreementId);
			String manager_id=cursor.getString(indexSaldoManagerId);
	    	GroupData group=null;
	    	int insertLocation=-1;
	    	for (GroupData groupSearch: mGroups)
	    	{
	    		if (groupSearch.agreement_id.equals(agreement_id))
	    		{
	    			// Добавлено по умолчанию
	    			if (groupSearch.manager_id.isEmpty())
	    			{
	    				group=groupSearch;
	    				group.manager_id=manager_id; 
	    				group.manager_descr=cursor.getString(indexSaldoManagerDescr);
		    			break;
	    			} else
	    			if (groupSearch.manager_id.equals(manager_id))
	    			{
	    				group=groupSearch;
		    			break;
	    			}
	    			// Новый вариант
	    			/* 
	    			if (groupSearch.manager_id_to_search.equals(manager_id))
	    			{
	    				group=groupSearch;
		    			break;
	    			}
	    			*/
	    			insertLocation=mGroups.indexOf(groupSearch);
	    		}
	    	}
	    	if (group==null)
	    	{
	    		// такого менеджера нет, либо долг по несуществующему (для агента) договору
	    		group=new GroupData();
	    		group.agreement_id=agreement_id;
	    		group.agreement_descr=cursor.getString(indexSaldoAgreementDescr);
	    		//group.organization_id=cursor.getString(indexSaldoOrganizationId);
	    		group.organization_descr=cursor.getString(indexSaldoOrganizationDescr);
	    		group.pricetype_descr=""; // тип цены мы здесь не знаем, но это не так важно
	    		group.manager_id=cursor.getString(indexSaldoManagerId);
	    		//group.manager_id_to_search=group.manager_id;
	    		group.manager_descr=cursor.getString(indexSaldoManagerDescr);
	    		
	    		group.debt=0.0;
	    		group.debtPast=0.0;
	    		group.details=new ArrayList<SaldoExtRecord>();
	    		if (insertLocation>=0)
	    		{
	    			mGroups.add(insertLocation, group);
	    		} else
	    		{
	    			mGroups.add(group);
	    		}
	    	}
			
	    	group.debt=group.debt+cursor.getDouble(indexSaldoDebt);
	    	group.debtPast=group.debtPast+cursor.getDouble(indexSaldoDebtPast);
			
			SaldoExtRecord data=new SaldoExtRecord();
			data.client_id=new MyID(m_client_id);
			data.agreement_id=new MyID(agreement_id);
			data.document_id=new MyID(cursor.getString(indexSaldoDocumentId));
			data.manager_id=new MyID(manager_id);
			data.document_descr=cursor.getString(indexSaldoDocumentDescr);
			data.manager_descr=cursor.getString(indexSaldoManagerDescr);
			data.saldo=cursor.getDouble(indexSaldoDebt);
			data.saldo_past=cursor.getDouble(indexSaldoDebtPast);
			group.details.add(data);
		}
		cursor.close();
	}	

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		//return mGroups.get(groupPosition).get(childPosition);
		return null;
	}


	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}


	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (convertView==null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.agreement_debt_item_detail, null);
		}
		
		GroupData group=mGroups.get(groupPosition);
		SaldoExtRecord data=group.details.get(childPosition);
		
		TextView tvDescr = (TextView) convertView.findViewById(R.id.agreement_item_descr);
		
		TextView tvManager = (TextView) convertView.findViewById(R.id.agreementTextViewManager);
		TextView tvDebt = (TextView) convertView.findViewById(R.id.agreementTextViewDebt);
		TextView tvDebtPast = (TextView) convertView.findViewById(R.id.agreementTextViewDebtPast);
		
		tvDescr.setText(data.document_descr);

		tvManager.setText("");
		tvDebt.setText(String.format("%.2f", data.saldo));
		tvDebtPast.setText(String.format("%.2f", data.saldo_past));
		
		return convertView;
	}


	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).details.size();
	}


	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
	}


	@Override
	public int getGroupCount() {
		return mGroups.size();
	}


	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}


	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		if (convertView==null)
		{
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.agreement_debt_item, null);
		}
		GroupData data=mGroups.get(groupPosition);
		
		TextView tvDescr = (TextView) convertView.findViewById(R.id.agreement_item_descr);
		TextView vtOrganisation = (TextView) convertView.findViewById(R.id.agreement_item_organization);
		TextView tvPriceType = (TextView) convertView.findViewById(R.id.agreement_item_price_type);
		TextView tvManager = (TextView) convertView.findViewById(R.id.agreementTextViewManager);
		TextView tvDebt = (TextView) convertView.findViewById(R.id.agreementTextViewDebt);
		TextView tvDebtPast = (TextView) convertView.findViewById(R.id.agreementTextViewDebtPast);
		
		tvDescr.setText(data.agreement_descr);
		vtOrganisation.setText(data.organization_descr);
		tvPriceType.setText(data.pricetype_descr);
		tvManager.setText(data.manager_descr);
		tvDebt.setText(String.format("%.2f", data.debt));
		tvDebtPast.setText(String.format("%.2f", data.debtPast));
		
		return convertView;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}


	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		//return false;
		return true;
	}
	
	
}
