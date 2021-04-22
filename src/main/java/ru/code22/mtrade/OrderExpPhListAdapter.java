package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import ru.code22.mtrade.MyDatabase.SalesHistoryRecord;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class OrderExpPhListAdapter extends BaseExpandableListAdapter {
	
	NomenclatureHierarchyHelper m_hierarchyHelper;

	class GroupData
	{
		String group_id;
		String group_name;
		boolean need_expand;
		int order_for_sorting;
		ArrayList<MyDatabase.OrderLineRecord> lines;
	}
	
	//private ArrayList<ArrayList<String>> mGroups;
	public ArrayList<GroupData> mGroups;
	private Context mContext;
	private int mDefaultTextColor;
	private MySingleton g;

	//public OrderExpListAdapter(Context context, ArrayList<ArrayList<String>> groups)
	public OrderExpPhListAdapter(Context context, int defaultTextColor)
	{
		mContext=context;
		mDefaultTextColor=defaultTextColor;
		mGroups=new ArrayList<GroupData>();
		m_hierarchyHelper = new NomenclatureHierarchyHelper(context);
		g = MySingleton.getInstance();
	}
	
	public void clearData()
	{
		mGroups.clear();
	}
	
	public void init()
	{
		// группы остаются всегда, однажды созданные
    	for (GroupData group: mGroups)
    	{
    		group.lines.clear();
    		//group.need_expand=false;
    	}
		
	    for (MyDatabase.OrderLineRecord orderLine: MySingleton.getInstance().MyDatabase.m_order_editing.lines)
	    {
    		String groupId=m_hierarchyHelper.getNomenclautureParentId(orderLine.nomenclature_id.toString());
    		String parentId=m_hierarchyHelper.getId(groupId, 1);
    		
    		// такое было в реальных условиях (parentId=null)
    		// (видимо, номенклатуру удалили)
    		int order_for_sorting=0;
    		if (parentId!=null)
    		{
	    		Cursor cursor=mContext.getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"order_for_sorting"}, "id=?", new String[]{parentId}, null);
	    		if (cursor.moveToFirst())
	    		{
	    			order_for_sorting=cursor.getInt(0);
	    		}
	    		cursor.close();
    		}
    		int indexForInsert=0;
	    	GroupData group=null;
	    	for (GroupData groupSearch: mGroups)
	    	{
	    		if (groupSearch.order_for_sorting<order_for_sorting)
	    		{
	    			indexForInsert=mGroups.indexOf(groupSearch)+1;
	    		}
	    		if (groupSearch.group_id.equals(parentId))
	    		{
	    			group=groupSearch;
	    			break;
	    		}
	    	}
	    	if (group==null)
	    	{
	    		group=new GroupData();
	    		if (parentId!=null)
	    		{
		    		group.group_id=new String(parentId);
		    		group.group_name=m_hierarchyHelper.getDescr(parentId, 1);
	    		} else
	    		{
		    		group.group_id=new MyID().toString();
		    		group.group_name="";
	    		}
	    		group.order_for_sorting=order_for_sorting;
	    		group.lines=new ArrayList<MyDatabase.OrderLineRecord>();
	    		group.need_expand=true;
	    		
	    		mGroups.add(indexForInsert, group);
	    	}
	    	group.lines.add(orderLine);
	    }
	}
	
	@Override
	public void notifyDataSetChanged() {
		init();
		super.notifyDataSetChanged();
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
			convertView = inflater.inflate(R.layout.order_line_item, null);
		}
		
		//OrderLineRecord data=(OrderLineRecord)MyDatabase.m_order_editing.lines.get(groupPosition);
		OrderLineRecord data=mGroups.get(groupPosition).lines.get(childPosition);
		TextView tvDiscount = (TextView) convertView.findViewById(R.id.textViewOrderLineDiscount);
		if (data.discount!=0.0)
		{
			tvDiscount.setText(Common.DoubleToStringFormat(data.discount, "%.3f")+"%");
			tvDiscount.setVisibility(View.VISIBLE);
		} else
		{
			tvDiscount.setVisibility(View.GONE);
		}
		
		TextView tvNomenclature = (TextView) convertView.findViewById(R.id.textViewOrderLineNomenclature);
		StringBuilder sb=new StringBuilder();
		if (data.shipping_time.length()==4)
		{
			sb.append(data.shipping_time.substring(0,2));
			sb.append(":");
			sb.append(data.shipping_time.substring(2,4));
			sb.append(" ");
		}
		sb.append(data.stuff_nomenclature);
		if (!data.comment_in_line.isEmpty())
		{
			sb.append(" [");
			sb.append(data.comment_in_line);
			sb.append("]");
		}
		tvNomenclature.setText(sb.toString());
		TextView tvSum = (TextView) convertView.findViewById(R.id.textViewOrderLineSum);
		//tvSum.setText(Common.DoubleToStringFormat(data.total, "%.3f")+getString(R.string.currency_short));
		//tvSum.setText(Common.DoubleToStringFormat(data.total, "%.3f")+mContext.getString(R.string.default_currency));
		tvSum.setText(String.format(g.Common.getCurrencyFormatted(mContext), Common.DoubleToStringFormat(data.total, "%.3f")));
		
		TextView tvQuantity = (TextView) convertView.findViewById(R.id.textViewOrderDate);
		//tvQuantity.setText(Double.toString(data.quantity)+" "+data.ed);
		if (data.quantity!=data.quantity_requested)
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed+" из "+Common.DoubleToStringFormat(data.quantity_requested, "%.3f"));
			tvQuantity.setBackgroundColor(Color.RED);
		} else
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed);
			tvQuantity.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return convertView;
	}


	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroups.get(groupPosition).lines.size();
	}


	@Override
	public Object getGroup(int groupPosition) {
		return mGroups.get(groupPosition);
		//return MyDatabase.m_order_editing.lines.get(groupPosition);
	}


	@Override
	public int getGroupCount() {
		return mGroups.size();
		//return MyDatabase.m_order_editing.lines.size();
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
			convertView = inflater.inflate(R.layout.order_category_line_item, null);
		}
		if (isExpanded)
		{
		} else
		{
		}
		GroupData data=mGroups.get(groupPosition);
		TextView tvDiscount = (TextView) convertView.findViewById(R.id.textViewOrderLineDiscount);
		//if (data.discount!=0.0)
		//{
		//	tvDiscount.setText(Common.DoubleToStringFormat(data.discount, "%.3f")+"%");
		//	tvDiscount.setVisibility(View.VISIBLE);
		//} else
		//{
			tvDiscount.setVisibility(View.GONE);
		//}
		
		TextView tvNomenclature = (TextView) convertView.findViewById(R.id.textViewOrderLineNomenclature);
		tvNomenclature.setText(data.group_name);
        tvNomenclature.setTextColor(mDefaultTextColor);
		TextView tvSum = (TextView) convertView.findViewById(R.id.textViewOrderLineSum);
		TextView tvQuantity = (TextView) convertView.findViewById(R.id.textViewOrderDate);
		tvSum.setText("");
		tvQuantity.setText("");
		/*
		tvSum.setText(Common.DoubleToStringFormat(data.total, "%.3f")+mContext.getString(R.string.default_currency));
		
		if (data.quantity!=data.quantity_requested)
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed+" из "+Common.DoubleToStringFormat(data.quantity_requested, "%.3f"));
			tvQuantity.setBackgroundColor(Color.RED);
		} else
		{
			tvQuantity.setText(Common.DoubleToStringFormat(data.quantity, "%.3f")+" "+data.ed);
			tvQuantity.setBackgroundColor(Color.TRANSPARENT);
		}
		*/
		return convertView;
	}


	@Override
	public boolean hasStableIds() {
		return true;
	}


	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}
	
	
}
