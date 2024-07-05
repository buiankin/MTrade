package ru.code22.mtrade;


import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.thepaksoft.fdtrainer.NestedListView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.Date;

public class EquipmentRestsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	static final int EQUIPMENT_RESTS_LOADER_ID = 1;
	static final int EQUIPMENT_RESTS_TARE_LOADER_ID = 2;
	static final String[] EQUIPMENT_RESTS_PROJECTION = new String[] {"_id", "client_id", "nomenclature_descr", "distr_point_id", "sum", "quantity", "empty", "doc_descr", "date", "datepast"};
	
	static final int REFUNDS_EQUIPMENT_RESTS_LOADER_ID = 3;
	static final int REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID = 4;
	
	static final String EXTRA_TITLE = "Title";
	static final String EXTRA_MODE = "Mode";
	
	TextView tvHeader;
	NestedListView lvEquipmentRests;
	SimpleCursorAdapter equipmentRestsAdapter;
	
	String m_datepast;
	
	//private static View view;
	
	String mCaption;
	int mMode;
	
	public static final EquipmentRestsFragment newInstance(String caption, int mode)
	{
		EquipmentRestsFragment f = new EquipmentRestsFragment();
	    Bundle bdl = new Bundle(2);
	    bdl.putString(EXTRA_TITLE, caption);
	    bdl.putInt(EXTRA_MODE, mode);
	    f.setArguments(bdl);
	    return f;
	}
	
	/*
	public EquipmentRestsFragment(String caption, int mode)
	{
		mCaption=caption;
		mMode=mode;
	}
	*/
	
	public void myRestartLoader()
	{
		LoaderManager.getInstance(this).restartLoader(mMode, null, this);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MySingleton g=MySingleton.getInstance();
    	
        mCaption = getArguments().getString(EXTRA_TITLE);
        mMode = getArguments().getInt(EXTRA_MODE);    	

    	/*
    	if (view != null) {
    	    ViewGroup parent = (ViewGroup) view.getParent();
    	    if (parent != null)
    	        parent.removeView(view);
    	}
    	*/
    	//try
    	//{
    		View view = inflater.inflate(R.layout.equipment_rests_fragment, container, false);
	        tvHeader = (TextView)view.findViewById(R.id.textViewEquipmentHeader);
	        tvHeader.setText(mCaption);
	        lvEquipmentRests = (NestedListView)view.findViewById(R.id.listViewEquipmentRests);
	        lvEquipmentRests.setEmptyView(view.findViewById(R.id.empty_equipment_rests));
	        
	        String[] fromColumns = {"nomenclature_descr", MySingleton.getInstance().Common.PRODLIDER?"empty":"sum", "quantity", "doc_descr", "date"};
	        int[] toViews = {R.id.textViewEquipmentLineNomenclature, R.id.textViewEquipmentLineSum, R.id.textViewEquipmentLineQuantity, R.id.textViewEquipmentLineDocDescr, R.id.textViewEquipmentLineDate};
	        
    		Date date=new Date();
    		m_datepast=Common.MyDateFormat("yyyyMMdd", date);
	        
	        equipmentRestsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.quantity_equipment_line_item, null, fromColumns, toViews, 0);
	        
	        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
	            @Override
	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	            	int index_datepast=cursor.getColumnIndex("datepast");
	                String datepast = cursor.getString(index_datepast);
	            	View w1=(View)view.getParent();
	            	if (m_datepast.compareTo(datepast)>=0)
	                	w1.setBackgroundColor(Color.RED);
	            	else
	                	w1.setBackgroundColor(Color.TRANSPARENT);
	            	String name = cursor.getColumnName(columnIndex);
	            	if ("date".equals(name))
	            	{
	            		Globals g=(Globals)getActivity().getApplication();
	            		if (MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
	            		{
	            			view.setVisibility(View.GONE);
	            		}
	                	String d=cursor.getString(columnIndex);
	                	if (d.length()==8)
	                	{
	                		StringBuilder sb=new StringBuilder();
	                		sb.append(Common.dateStringAsText(d));
		                	TextView tv=(TextView)view;
		                	tv.setText(sb.toString());
		        			return true;
	                	}
	            		
	            		return true;
	            	}
					return false;
	            }
	        };
	        equipmentRestsAdapter.setViewBinder(binder);
	        lvEquipmentRests.setAdapter(equipmentRestsAdapter);

	        // 26.04.2019
			//LoaderManager.getInstance(this).initLoader(mMode, null, this);

    	//} catch (InflateException e) {
    	//	/* map is already there, just return view as it is */	
    	//}
        return view;
    }

	@Override
	public void onResume()
	{
		super.onResume();
		LoaderManager.getInstance(this).initLoader(mMode, null, this);
	}


    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}


	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnNumericKeyboardFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " должен реализовывать интерфейс OnNumericKeyboardFragmentInteractionListener");
        }
        */
    }



	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		// если список получится пустой, никаких запросов выполнять не требуется
		switch (id)
		{
		case EQUIPMENT_RESTS_LOADER_ID:
		case EQUIPMENT_RESTS_TARE_LOADER_ID:			
			if (MySingleton.getInstance().MyDatabase.m_order_editing.client_id.isEmpty())
			{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "0=1", null, null);
			}
			break;
			
    	case REFUNDS_EQUIPMENT_RESTS_LOADER_ID:
    	case REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID:
			if (MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.isEmpty())
			{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "0=1", null, null);
			}
			break;
		}
		
    	switch (id)
    	{
    	case EQUIPMENT_RESTS_LOADER_ID:
    		if (MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and equipment_rests.flags=0", new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString()}, null);
    		} else
    		if (MySingleton.getInstance().Common.TANDEM)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and agreement_id=? and equipment_rests.flags=0", new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_order_editing.agreement_id.toString()}, null);
    		} else
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and distr_point_id=?", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_order_editing.distr_point_id.toString()}, null);
    		}
    	case EQUIPMENT_RESTS_TARE_LOADER_ID:
    		if (MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString()}, null);
    		} else
    		if (MySingleton.getInstance().Common.TANDEM)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and agreement_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_order_editing.agreement_id.toString()}, null);
    		} else
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and distr_point_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_order_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_order_editing.distr_point_id.toString()}, null);
    		}
    		
    	case REFUNDS_EQUIPMENT_RESTS_LOADER_ID:
    		if (MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and equipment_rests.flags=0", new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString()}, null);
    		} else
    		if (MySingleton.getInstance().Common.TANDEM)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and agreement_id=? and equipment_rests.flags=0", new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_refund_editing.agreement_id.toString()}, null);
    		} else
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and distr_point_id=?", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_refund_editing.distr_point_id.toString()}, null);
    		}
    	case REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID:
    		if (MySingleton.getInstance().Common.TITAN||MySingleton.getInstance().Common.FACTORY)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString()}, null);
    		} else
    		if (MySingleton.getInstance().Common.TANDEM)
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and agreement_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_refund_editing.agreement_id.toString()}, null);
    		} else
    		{
		        return new CursorLoader(getActivity(), MTradeContentProvider.EQUIPMENT_RESTS_LIST_CONTENT_URI,
		        		EQUIPMENT_RESTS_PROJECTION, "client_id=? and distr_point_id=? and equipment_rests.flags=1", 
		        		new String[]{MySingleton.getInstance().MyDatabase.m_refund_editing.client_id.toString(), MySingleton.getInstance().MyDatabase.m_refund_editing.distr_point_id.toString()}, null);
    		}
    		
    	}
    	return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
        case EQUIPMENT_RESTS_LOADER_ID:
        case EQUIPMENT_RESTS_TARE_LOADER_ID:
    	case REFUNDS_EQUIPMENT_RESTS_LOADER_ID:
    	case REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID:
        	equipmentRestsAdapter.swapCursor(data);
          break;
        }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
        case EQUIPMENT_RESTS_LOADER_ID:
        case EQUIPMENT_RESTS_TARE_LOADER_ID:
    	case REFUNDS_EQUIPMENT_RESTS_LOADER_ID:
    	case REFUNDS_EQUIPMENT_RESTS_TARE_LOADER_ID:
        	equipmentRestsAdapter.swapCursor(null);
        	break;
        }
	}	

}
