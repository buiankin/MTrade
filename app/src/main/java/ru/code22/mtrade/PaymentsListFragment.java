package ru.code22.mtrade;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class PaymentsListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ActivityResultLauncher<Intent> selectClientActivityResultLauncher;
    private UniversalInterface mListener;

    private String m_client_id;
    private String m_client_descr;

    int defaultTextColor;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    //private static final int ORDERS_LOADER_ID = 1;
    private static final int PAYMENTS_LOADER_ID = 2;
    //private static final int MESSAGES_LOADER_ID = 3;

    // This is the Adapter being used to display the list's data
    //SimpleCursorAdapter ordersAdapter;
    SimpleCursorAdapter paymentsAdapter;
    //SimpleCursorAdapter messagesAdapter;

    static final String[] CASH_PAYMENTS_PROJECTION = new String[]{"_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc", "color", "zero"};

    public PaymentsListFragment() {
    }

    void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            m_client_id = bundle.getString("client_id");
            m_client_descr = bundle.getString("client_descr");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        outState.putString("client_id", m_client_id);
        outState.putString("client_descr", m_client_descr);
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
        if (savedInstanceState==null)
        {
            // Если данные не сохранялись, берем установленные прогрммно при создании фрейма
            savedInstanceState=getArguments();
        }

        readDataFromBundle(savedInstanceState);

        selectClientActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == ClientsActivity.CLIENTS_RESULT_OK) {
                            Intent data = result.getData();

                            if (data != null) {
                                long id = data.getLongExtra("id", 0);
                                //Toast.makeText(MainActivity.this, "id=" + id, Toast.LENGTH_SHORT).show();
                                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, id);
                                //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
                                Cursor cursor = getActivity().getContentResolver().query(singleUri, new String[]{"descr", "id"}, null, null, null);
                                if (cursor.moveToNext()) {
                                    int descrIndex = cursor.getColumnIndex("descr");
                                    int idIndex = cursor.getColumnIndex("id");
                                    String newWord = cursor.getString(descrIndex);
                                    String clientId = cursor.getString(idIndex);

                                    Bundle parameters = new Bundle();
                                    parameters.putString("client_id", clientId);
                                    parameters.putString("client_descr", newWord);
                                    mListener.onUniversalEventListener("FilterClient", parameters);
                                }
                                cursor.close();
                            }

                        }
                    }
                });


        View view = inflater.inflate(R.layout.payments_list_fragment, container, false);

        EditText etClient = (EditText) view.findViewById(R.id.etClient);
        etClient.setText(m_client_descr);

        ListView lvPayments = (ListView) view.findViewById(R.id.listViewPayments);
        lvPayments.setEmptyView(view.findViewById(R.id.empty_payments));

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
                //startActivityForResult(intent, MainActivity.SELECT_CLIENT_REQUEST);
                selectClientActivityResultLauncher.launch(intent);
            }
        });

        final FloatingActionButton fabAddPayment=(FloatingActionButton)view.findViewById(R.id.fab_add_payment);
        fabAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUniversalEventListener("createPayment", null);
            }
        });

        defaultTextColor = Common.getColorFromAttr(getActivity(), R.attr.myTextColor);

        // Create a progress bar to display while the list loads
        //ProgressBar progressBar = new ProgressBar(this);
        //progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        //progressBar.setIndeterminate(true);
        //getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        //ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        //root.addView(progressBar);

        // For the cursor adapter, specify which columns go into which views
        //String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
        //int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1


        String[] paymentsfromColumns = {"numdoc", "datedoc", "state", "client_descr", "sum_doc", "zero"};
        //String[] paymentsfromColumns = {"client_descr", "sum_doc"};
        int[] paymentsToViews = {R.id.tvPaymentListNumber, R.id.tvPaymentListDate, R.id.tvPaymentListState, R.id.tvPaymentListClientDescr, R.id.tvPaymentListSum, R.id.LinearLayoutPaymentList};

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        paymentsAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.payment_item, null,
                paymentsfromColumns, paymentsToViews, 0);
        lvPayments.setAdapter(paymentsAdapter);
        lvPayments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long arg) {
                //Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                Cursor cursor = paymentsAdapter.getCursor();
                cursor.moveToPosition(position);
                Bundle parameters=new Bundle();
                parameters.putLong("id", cursor.getLong(0));
                parameters.putBoolean("copy_payment", false);
                mListener.onUniversalEventListener("editPayment", parameters);
            }
        });

        SimpleCursorAdapter.ViewBinder binderPayments = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                switch (view.getId()) {
                    case R.id.LinearLayoutPaymentList:
                        {
                        //int index_color=cursor.getColumnIndex(MTradeContentProvider.COLOR_COLUMN);
                        int index_color = cursor.getColumnIndex("color");
                        int color = cursor.getInt(index_color);

                        int textColor=PaymentsHelpers.fillPaymentColor(defaultTextColor, getResources(), color, view);

                        LinearLayout layout=(LinearLayout)view;

                        for (int i=0; i<layout.getChildCount(); i++) {
                            LinearLayout layout0=(LinearLayout)layout.getChildAt(i);
                            for (int j = 0; j < layout0.getChildCount(); j++) {
                                View tv = (View) layout0.getChildAt(j);
                                ((TextView) tv).setTextColor(textColor);
                            }
                        }
                        return true;
                    }
                    case R.id.tvPaymentListDate: {
                        String d = cursor.getString(columnIndex);
                        if (d.length() == 14) {
                            TextView tv = (TextView) view;
                            tv.setText(Common.dateTimeStringAsText(d));
                            return true;
                        }
                    }
                    case R.id.tvPaymentListState:
                        ((TextView) view).setText(MyDatabase.GetPaymentStateDescr(E_PAYMENT_STATE.fromInt(cursor.getInt(columnIndex)), getActivity()));
                        return true;
                }
                return false;
            }
        };

        registerForContextMenu(lvPayments);

        paymentsAdapter.setViewBinder(binderPayments);

        // 26.04.2019
        //LoaderManager.getInstance(this).initLoader(PAYMENTS_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(PAYMENTS_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MySingleton g = MySingleton.getInstance();
        switch (id) {
            case PAYMENTS_LOADER_ID:
                if (m_client_id != null&&!new MyDatabase.MyID(m_client_id).isEmpty()) {
                    return new CursorLoader(getActivity(), MTradeContentProvider.CASH_PAYMENTS_JOURNAL_CONTENT_URI,
                            CASH_PAYMENTS_PROJECTION, "client_id=?", new String[]{m_client_id}, "cash_payments._id DESC");
                } else {
                    return new CursorLoader(getActivity(), MTradeContentProvider.CASH_PAYMENTS_JOURNAL_CONTENT_URI,
                            CASH_PAYMENTS_PROJECTION, null, null, "cash_payments._id DESC");
                }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case PAYMENTS_LOADER_ID:
                paymentsAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        switch (loader.getId()) {
            case PAYMENTS_LOADER_ID:
                paymentsAdapter.swapCursor(null);
                break;
        }
    }


    public void onFilterClientSelected(String client_id, String client_descr) {
        m_client_id = client_id;
        m_client_descr = client_descr;
        View view = getView();
        EditText et = (EditText) view.findViewById(R.id.etClient);
        et.setText(client_descr);
        LoaderManager.getInstance(this).restartLoader(PAYMENTS_LOADER_ID, null, this);
    }

    public void restartLoaderForListView()
    {
        LoaderManager.getInstance(this).restartLoader(PAYMENTS_LOADER_ID, null, this);
    }


}
