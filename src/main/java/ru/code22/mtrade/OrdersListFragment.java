package ru.code22.mtrade;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class OrdersListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private UniversalInterface mListener;

    private boolean m_bDataInited;

    private String m_client_id;
    private String m_client_descr;
    private boolean m_filter_orders;
    private boolean m_filter_refunds;
    private boolean m_filter_distribs;
    private String m_filter_date_begin;
    private String m_filter_date_end;
    private int m_filter_type;
    private int m_filter_date_type;


    int defaultTextColor;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int ORDERS_LOADER_ID = 1;
    //private static final int PAYMENTS_LOADER_ID = 2;
    //private static final int MESSAGES_LOADER_ID = 3;

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter ordersAdapter;
    //SimpleCursorAdapter paymentsAdapter;
    //SimpleCursorAdapter messagesAdapter;

    static final String[] JOURNAL_PROJECTION = new String[]{"_id", "order_id", "refund_id", "distribs_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc", "sum_shipping", "color", "shipping_date"};

    public OrdersListFragment() {
        m_bDataInited=false;
    }

    void readDataFromBundle(Bundle bundle) {
        m_bDataInited=true;
        if (bundle != null) {
            m_client_id = bundle.getString("client_id");
            m_client_descr = bundle.getString("client_descr");
            m_filter_orders = bundle.getInt("filter_orders") != 0;
            m_filter_refunds = bundle.getInt("filter_refunds") != 0;
            m_filter_distribs = bundle.getInt("filter_distribs") != 0;
            m_filter_date_begin = bundle.getString("filter_date_begin");
            m_filter_date_end = bundle.getString("filter_date_end");
            m_filter_type = bundle.getInt("filter_type");
            m_filter_date_type = bundle.getInt("filter_date_type");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        outState.putString("client_id", m_client_id);
        outState.putString("client_descr", m_client_descr);
        outState.putInt("filter_orders", m_filter_orders ? 1 : 0);
        outState.putInt("filter_refunds", m_filter_refunds ? 1 : 0);
        outState.putInt("filter_distribs", m_filter_distribs ? 1 : 0);
        outState.putString("filter_date_begin", m_filter_date_begin);
        outState.putString("filter_date_end", m_filter_date_end);
        outState.putInt("filter_type", m_filter_type);
        outState.putInt("filter_date_type", m_filter_date_type);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (UniversalInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement interface UniversalInterface");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        m_bDataInited=true;

        if (savedInstanceState==null)
        {
            // Если данные не сохранялись, берем установленные прогрммно при создании фрейма
            savedInstanceState=getArguments();
        }

        readDataFromBundle(savedInstanceState);

        View view = inflater.inflate(R.layout.orders_list_fragment, container, false);

        EditText etClient = (EditText) view.findViewById(R.id.etClient);
        etClient.setText(m_client_descr);

        ListView lvOrders = (ListView) view.findViewById(R.id.listViewOrders);
        lvOrders.setEmptyView(view.findViewById(R.id.empty_orders));

        final Button buttonClearClient = (Button) view.findViewById(R.id.buttonFilterClearClient);
        buttonClearClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle parameters=new Bundle();
                parameters.putString("client_id", null);
                parameters.putString("client_descr", getString(R.string.catalogue_all_clients));
                mListener.onUniversalEventListener("FilterClient", parameters);
            }
        });

        final Button buttonSelectClient = (Button) view.findViewById(R.id.buttonFilterSelectClient);
        buttonSelectClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ClientsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, MainActivity.SELECT_CLIENT_REQUEST);
            }
        });

        final FloatingActionButton fabAddOrder=(FloatingActionButton)view.findViewById(R.id.fab_add_order);
        fabAddOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUniversalEventListener("createOrder", null);
            }
        });


        String[] fromColumns = {"refund_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc"};
        int[] toViews = {R.id.tvDocumentType, R.id.tvOrderListNumber, R.id.tvOrderListDate, R.id.tvOrderListState, R.id.tvOrderListClientDescr, R.id.tvOrderListSum}; // The TextView in simple_list_item_1

        defaultTextColor = Common.getColorFromAttr(getActivity(), R.attr.myTextColor);

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        ordersAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.order_item, null,
                fromColumns, toViews, 0);
        //setListAdapter(mAdapter);
        lvOrders.setAdapter(ordersAdapter);
        lvOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long arg) {
                MySingleton g = MySingleton.getInstance();

                // Когда открыт список платежей, но платежей нет, то при клике
                // срабатывает это событие, но это неправильно, поэтому делаем проверку
                boolean bOrdersMode = true;
                if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                    bOrdersMode = true;//getIsOrdersMode();
                }
                if (bOrdersMode) {
                    //Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                    Cursor cursor = ordersAdapter.getCursor();
                    int index_order_id = cursor.getColumnIndex("order_id");
                    int index_refund_id = cursor.getColumnIndex("refund_id");
                    int index_distribs_id = cursor.getColumnIndex("distribs_id");
                    cursor.moveToPosition(position);
                    if (cursor.getLong(index_order_id) != 0) {
                        Bundle parameters=new Bundle();
                        parameters.putLong("id", cursor.getLong(index_order_id));
                        parameters.putBoolean("copy_order", false);
                        mListener.onUniversalEventListener("editOrder", parameters);
                    } else if (cursor.getLong(index_refund_id) != 0) {
                        Bundle parameters=new Bundle();
                        parameters.putLong("id", cursor.getLong(index_refund_id));
                        parameters.putBoolean("copy_refund", false);
                        parameters.putLong("order_id", 0);
                        mListener.onUniversalEventListener("editRefund", parameters);
                    } else if (cursor.getLong(index_distribs_id) != 0) {
                        Bundle parameters=new Bundle();
                        parameters.putLong("id", cursor.getLong(index_distribs_id));
                        parameters.putBoolean("copy_distribs", false);
                        mListener.onUniversalEventListener("editDistribs", parameters);
                    }
                }

            }
        });
        /*
        lvOrders.setOnItemLongClickListener(new OnItemLongClickListener()
        {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				return true;
			}
		});
		*/

        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                MySingleton g = MySingleton.getInstance();
            	/*
            	switch (view.getId())
            	{
            	case android.R.id.
            	}
            	*/

                // http://ru.wikipedia.org/wiki/Цвета_HTML

                //int index_color=cursor.getColumnIndex(MTradeContentProvider.COLOR_COLUMN);
                int index_color = cursor.getColumnIndex("color");
                int color = cursor.getInt(index_color);
                View w1 = (View) view.getParent();
                TextView tv = (TextView) view;

                int textColor=OrdersHelpers.fillOrderColor(defaultTextColor, getResources(), color, w1);
                tv.setTextColor(textColor);

                String name = cursor.getColumnName(columnIndex);
                if ("datedoc".equals(name)) {
                    String d = cursor.getString(columnIndex);
                    if (d.length() == 14) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(d.substring(6, 8)).append('.').append(d.substring(4, 6)).append('.').append(d.substring(0, 4)).append(' ').append(d.substring(8, 10)).append(':').append(d.substring(10, 12)).append(':').append(d.substring(12, 14));
                        if (g.Common.PHARAON || g.Common.FACTORY) {
                            int index_shipping_date = cursor.getColumnIndex("shipping_date");
                            String shipping_date = cursor.getString(index_shipping_date);
                            if (shipping_date.length() >= 8) {
                                sb.append('(').append(shipping_date.substring(6, 8)).append('.').append(shipping_date.substring(4, 6)).append('.').append(shipping_date.substring(0, 4)).append(')');
                            }
                        }
                        tv.setText(sb.toString());
                        return true;
                    }
                }
                if ("state".equals(name)) {
                    tv.setText(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.fromInt(cursor.getInt(columnIndex)), getActivity()));
                    return true;
                }
                if ("refund_id".equals(name)) {
                    int i = cursor.getInt(columnIndex);
                    if (i != 0) {
                        tv.setVisibility(View.VISIBLE);
                        tv.setText(R.string.label_refund);
                    } else {
                        tv.setVisibility(View.GONE);
                    }
                    return true;
                }
                // 15.04.2015
                if ("sum_doc".equals(name)) {
                    double sum_doc = cursor.getDouble(columnIndex);
                    int sum_shippingIndex = cursor.getColumnIndex("sum_shipping");
                    double sum_shipping = cursor.getDouble(sum_shippingIndex);
                    // 25.05.2015
                    int index_refund_id = cursor.getColumnIndex("refund_id");
                    // 14.11.2016
                    int index_distribs_id = cursor.getColumnIndex("distribs_id");
                    int refund_id = cursor.getInt(index_refund_id);
                    int distribs_id = cursor.getInt(index_distribs_id);
                    // 19.04.2018
                    boolean bShowSumShippingAnyway = false;
                    if (g.Common.PRODLIDER) {
                        int index_state = cursor.getColumnIndex("state");
                        E_ORDER_STATE orderState = E_ORDER_STATE.fromInt(cursor.getInt(index_state));
                        switch (orderState) {
                            case E_ORDER_STATE_LOADED:
                            case E_ORDER_STATE_ACKNOWLEDGED:
                            case E_ORDER_STATE_COMPLETED:
                            case E_ORDER_STATE_CANCELED:
                            case E_ORDER_STATE_WAITING_AGREEMENT:
                                bShowSumShippingAnyway = true;
                                break;
                            default:
                        }
                        // По-моему у новых документов там -1, актуально для bShowSumShippingAnyway
                        if (sum_shipping < 0 && bShowSumShippingAnyway)
                            sum_shipping = 0;
                        //
                    }
                    if (refund_id != 0 || distribs_id != 0) {
                        if (refund_id != 0) {
                            tv.setText(R.string.label_refund);
                        } else {
                            tv.setText(R.string.label_distribs_short);
                        }
                        tv.setBackgroundColor(Color.TRANSPARENT);
                        return true;
                    } else
                        //
                        if ((sum_shipping >= 0 || bShowSumShippingAnyway) && sum_doc != sum_shipping) {
                            tv.setText(Common.DoubleToStringFormat(sum_doc, "%.2f") + "/" + g.Common.DoubleToStringFormat(sum_shipping, "%.2f"));
                            tv.setBackgroundColor(Color.RED);
                            return true;
                        } else {
                            // тут как обычно
                            tv.setText(Common.DoubleToStringFormat(sum_doc, "%.2f"));
                            tv.setBackgroundColor(Color.TRANSPARENT);
                            return true;
                        }
                }

            	/*
                String name = cursor.getColumnName(columnIndex);
                if ("sum_doc".equals(name)) {
                    int color = cursor.getInt(columnIndex);
                    //view.setBackgroundColor(color);
                    return true;
                }
                */
                //view.setBackgroundColor(Color.CYAN);
                //Returns true if the data was bound to the view, false otherwise
                return false;
            }
        };

        registerForContextMenu(lvOrders);

        ordersAdapter.setViewBinder(binder);

        // 26.04.2019
        //LoaderManager.getInstance(this).initLoader(ORDERS_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(ORDERS_LOADER_ID, null, this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MySingleton g = MySingleton.getInstance();
        //return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
        //        PROJECTION, SELECTION, null, null);
        // Create a new CursorLoader with the following query parameters.
        switch (id) {
            case ORDERS_LOADER_ID: {
                String conditionString = "";
                ArrayList<String> conditionArgs = new ArrayList<String>();

                if (m_client_id != null&&!new MyDatabase.MyID(m_client_id).isEmpty()) {
                    conditionString = Common.combineConditions(conditionString, conditionArgs, "client_id=?", new String[]{m_client_id});
                }
                //if (m_bNotClosed) {
                //    conditionString = Common.combineConditions(conditionString, conditionArgs, E_ORDER_STATE.getNotClosedConditionWhere(), E_ORDER_STATE.getNotClosedSelectionArgs());
                //}

                //private static boolean m_filter_orders=false;
                //private static boolean m_filter_refunds=false;
                //private static int m_filter_date_type=0;
                //private static String m_filter_date_begin="", m_filter_date_end="";

                if (m_filter_orders && m_filter_refunds && m_filter_distribs || !(g.Common.PRODLIDER || g.Common.TANDEM)) {
                    // все флаги установлены или не Продлидер
                } else {
    			/*
    			if (m_filter_orders)
    			{
    				conditionString=Common.combineConditions(conditionString, conditionArgs, "iddocdef=0", null);
    			} else
    			{
    				conditionString=Common.combineConditions(conditionString, conditionArgs, "iddocdef=1", null);
    			}
    			*/

                    StringBuilder sb = new StringBuilder();
                    String delimiter = "";
                    if (m_filter_orders) {
                        sb.append(delimiter);
                        sb.append("iddocdef=0");
                        delimiter = " or ";
                    }
                    if (m_filter_refunds) {
                        sb.append(delimiter);
                        sb.append("iddocdef=1");
                        delimiter = " or ";
                    }
                    if (m_filter_distribs) {
                        sb.append(delimiter);
                        sb.append("iddocdef=3");
                        delimiter = " or ";
                    }
                    if (sb.length() == 0) {
                        conditionString = Common.combineConditions(conditionString, conditionArgs, "0=1", new String[]{});
                    } else {
                        conditionString = Common.combineConditions(conditionString, conditionArgs, sb.toString(), new String[]{});
                    }
                }
                if (g.Common.PHARAON && m_filter_type == 1) {
                    // По дате обслуживания
                    //conditionString=Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_date, m_date+"Z"});
                    switch (m_filter_date_type) {
                        case 0:
                            // все
                            break;
                        case 1:
                            // дата
                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                            break;
                        case 2:
                            // интервал
                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                            break;
                    }
                } else {
                    // По дате документа
                    //conditionString=Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_date, m_date+"Z"});
                    switch (m_filter_date_type) {
                        case 0:
                            // все
                            break;
                        case 1:
                            // дата
                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                            break;
                        case 2:
                            // интервал
                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                            break;
                    }
                }
                // 13.09.2014
                //return new CursorLoader(this, MTradeContentProvider.ORDERS_JOURNAL_CONTENT_URI,
                //        ORDERS_PROJECTION, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "orders._id DESC");

                return new CursorLoader(getActivity(), MTradeContentProvider.JOURNAL_CONTENT_URI,
                        JOURNAL_PROJECTION, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "journal._id DESC");

            }
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ORDERS_LOADER_ID:
                ordersAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        switch (loader.getId()) {
            case ORDERS_LOADER_ID:
                ordersAdapter.swapCursor(null);
                break;
        }
    }


    public void onFilterClientSelected(String client_id, String client_descr) {
        m_client_id = client_id;
        m_client_descr = client_descr;
        View view = getView();
        EditText et = (EditText) view.findViewById(R.id.etClient);
        et.setText(client_descr);
        LoaderManager.getInstance(this).restartLoader(ORDERS_LOADER_ID, null, this);
        //getSupportLoaderManager().restartLoader(PAYMENTS_LOADER_ID, null, MainActivity.this);
    }

    public void onFilterChanged(boolean filter_orders, boolean filter_refunds, boolean m_filter_distribs,
                                String filter_date_begin, String filter_date_end, int filter_type, int filter_date_type ) {
        m_filter_orders=filter_orders;
        m_filter_refunds=filter_refunds;
        m_filter_distribs=m_filter_distribs;
        m_filter_date_begin=filter_date_begin;
        m_filter_date_end=filter_date_end;
        m_filter_type=filter_type;
        m_filter_date_type=filter_date_type;
        LoaderManager.getInstance(this).restartLoader(ORDERS_LOADER_ID, null, this);
    }


    public void restartLoaderForListView()
    {
        // Фрагмент еще не толком не создался
        // после создания он сам инициализируется в любом случае
        if (m_bDataInited) {
            LoaderManager.getInstance(this).restartLoader(ORDERS_LOADER_ID, null, this);
        }
    }


}
