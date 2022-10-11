package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.MyDatabase.SalesHistoryRecord;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

// буквы - использовать addHeaderView
// indicator.xml в папку drawable
// <selector ...
//  <item android:state_expanded="true"
//      android:drawable="@drawable/imageOpen"
//  </item>
//             .. state_empty="true" ...
// а в списке android:groupIndicator="@drawable/indicator"

// OnChildClickListener
// OnGroupCollapseListener
// OnGroupExpandListener
// OnGroupClickListener

public class RefundExpListAdapter extends BaseExpandableListAdapter {
	//private ArrayList<ArrayList<String>> mGroups;
	private Context mContext;
	private int mDefaultTextColor;
	
	Map<String, List<MyDatabase.SalesHistoryRecord>> m_sales;
	
	//public OrderExpListAdapter(Context context, ArrayList<ArrayList<String>> groups)
	public RefundExpListAdapter(Context context, int defaultTextColor)
	{
		mContext=context;
        mDefaultTextColor=defaultTextColor;
		//mGroups=groups;
		m_sales= new HashMap<String, List<MyDatabase.SalesHistoryRecord>>();
	}
	
	public void clearSalesHistory()
	{
		m_sales.clear();
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
			convertView = inflater.inflate(R.layout.order_sales_line_item, null);
		}
		TextView tvDescr = (TextView) convertView.findViewById(R.id.textViewOrderSalesLineNomenclature);
		TextView vtQuantity = (TextView) convertView.findViewById(R.id.textViewOrderSalesLineQuantity);
		TextView tvSum = (TextView) convertView.findViewById(R.id.textViewOrderSalesLineSum);
		RefundLineRecord line=MySingleton.getInstance().MyDatabase.m_refund_editing.lines.get(groupPosition);
		String nomenclatureId=line.nomenclature_id.toString();
		if (m_sales.containsKey(nomenclatureId))
		{
			List<SalesHistoryRecord> list=m_sales.get(nomenclatureId);
			SalesHistoryRecord rec=list.get(childPosition);
			// запись уже была кэширована
			//return m_sales.get(nomenclatureId).size();
			String d=rec.datedoc.length()==14?Common.dateTimeStringAsText(rec.datedoc):Common.dateStringAsText(rec.datedoc);
			tvDescr.setText(rec.numdoc+"; "+d);
			
			if (line.k>0.001)
			{
				vtQuantity.setText(Common.DoubleToStringFormat(rec.quantity/line.k, "%.3f")+line.ed);
			} else
			{
				vtQuantity.setText(Common.DoubleToStringFormat(rec.quantity/line.k, "%.3f"));
			}
			//tvSum.setText(Common.DoubleToStringFormat(rec.price, "%.3f")+mContext.getString(R.string.default_currency));
			MySingleton g = MySingleton.getInstance();
			tvSum.setText(String.format(g.Common.getCurrencyFormatted(mContext), Common.DoubleToStringFormat(rec.price, "%.3f")));
		}
		//textChild.setText(mGroups.get(groupPosition).get(childPosition).);
		
		//Button button=(Button)convertView.findViewById(R.id.buttonChild);
		// setOnClickListener
		convertView.setBackgroundColor(Color.DKGRAY);
			
		return convertView;
	}


	@Override
	public int getChildrenCount(int groupPosition) {
		
		MySingleton g=MySingleton.getInstance();
		
		String clientId=g.MyDatabase.m_refund_editing.client_id.toString();
		String nomenclatureId=g.MyDatabase.m_refund_editing.lines.get(groupPosition).nomenclature_id.toString();

		if (m_sales.containsKey(nomenclatureId))
		{
			// запись уже была кэширована
			return m_sales.get(nomenclatureId).size();
		}
		// запросим данные из базы
		List<MyDatabase.SalesHistoryRecord> list=new ArrayList<MyDatabase.SalesHistoryRecord>();
		//return mGroups.get(groupPosition).size();
		Cursor salesLines=mContext.getContentResolver().query(MTradeContentProvider.SALES_LOADED_CONTENT_URI, null, "nomenclature_id=? and client_id=?", new String[]{nomenclatureId, clientId}, "datedoc DESC");
		//Cursor salesLines=mContext.getContentResolver().query(MTradeContentProvider.SALES_LOADED_CONTENT_URI, null, "nomenclature_id=?", new String[]{nomenclatureId}, "datedoc DESC");
		
		int datedoc_Index=salesLines.getColumnIndex("datedoc");
		int numdoc_Index=salesLines.getColumnIndex("numdoc");
		int refdoc_Index=salesLines.getColumnIndex("refdoc");
		int curatorId_Index=salesLines.getColumnIndex("curator_id");
		int distrPointId_Index=salesLines.getColumnIndex("distr_point_id");
		int nomenclatureId_Index=salesLines.getColumnIndex("nomenclature_id");
		int quantity_Index=salesLines.getColumnIndex("quantity");
		int price_Index=salesLines.getColumnIndex("price");
		
		while(salesLines.moveToNext())
		{
			MyDatabase.SalesHistoryRecord rec=new MyDatabase.SalesHistoryRecord();
			rec.datedoc=salesLines.getString(datedoc_Index);
			rec.numdoc=salesLines.getString(numdoc_Index);
			rec.sale_doc_id=new MyID(salesLines.getString(refdoc_Index));
			rec.curator_id=new MyID(salesLines.getString(curatorId_Index));
			rec.distr_point_id=new MyID(salesLines.getString(distrPointId_Index));
			rec.nomenclature_id=new MyID(salesLines.getString(nomenclatureId_Index));
			rec.quantity=salesLines.getDouble(quantity_Index);
			rec.price=salesLines.getDouble(price_Index);
			list.add(rec);
		}
		salesLines.close();
		m_sales.put(nomenclatureId, list);
		return list.size();
	}


	@Override
	public Object getGroup(int groupPosition) {
		//return mGroups.get(groupPosition);
		return MySingleton.getInstance().MyDatabase.m_refund_editing.lines.get(groupPosition);
	}


	@Override
	public int getGroupCount() {
		//return mGroups.size();
		return MySingleton.getInstance().MyDatabase.m_refund_editing.lines.size();
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
			convertView = inflater.inflate(R.layout.order_line_item, null);
		}
		if (isExpanded)
		{
		} else
		{
		}
		RefundLineRecord data=(RefundLineRecord)MySingleton.getInstance().MyDatabase.m_refund_editing.lines.get(groupPosition);
		TextView tvDiscount = (TextView) convertView.findViewById(R.id.textViewOrderLineDiscount);
		tvDiscount.setVisibility(View.GONE);
		
		TextView tvNomenclature = (TextView) convertView.findViewById(R.id.textViewOrderLineNomenclature);
		StringBuilder sb=new StringBuilder();
		sb.append(data.stuff_nomenclature);
		if (!data.comment_in_line.isEmpty())
		{
			sb.append(" [");
			sb.append(data.comment_in_line);
			sb.append("]");
		}
		tvNomenclature.setText(sb.toString());
        tvNomenclature.setTextColor(mDefaultTextColor);
		
		TextView tvSum = (TextView) convertView.findViewById(R.id.textViewOrderLineSum);
		tvSum.setVisibility(View.GONE);

		TextView tvQuantity = (TextView) convertView.findViewById(R.id.textViewOrderDate);
		if (data.quantity!=data.quantity_requested)
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed+" из "+Common.DoubleToStringFormat(data.quantity_requested, "%.3f"));
			tvQuantity.setBackgroundColor(Color.RED);
		} else
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed);
			tvQuantity.setBackgroundColor(Color.TRANSPARENT);
		}
        tvQuantity.setTextColor(mDefaultTextColor);

		return convertView;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}


	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
		//return true;
	}
	
}
