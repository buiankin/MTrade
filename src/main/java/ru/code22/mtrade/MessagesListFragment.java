package ru.code22.mtrade;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class MessagesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private UniversalInterface mListener;

    //ListView lvMessages;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    //private static final int ORDERS_LOADER_ID = 1;
    //private static final int PAYMENTS_LOADER_ID = 2;
    private static final int MESSAGES_LOADER_ID = 3;

    // This is the Adapter being used to display the list's data
    //SimpleCursorAdapter ordersAdapter;
    //SimpleCursorAdapter paymentsAdapter;
    SimpleCursorAdapter messagesAdapter;

    static final String[] MESSAGES_LIST_PROJECTION = new String[]{"_id", "uid", "descr", "text", "sender_id", "receiver_id", "client_id", "nomenclature_id", "fname", "datetime", "date1", "date2", "acknowledged", "inout", "type_idx", "zero"};

    public MessagesListFragment() {
    }

    void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            //m_client_id = bundle.getString("client_id");
            //m_client_descr = bundle.getString("client_descr");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        //outState.putString("client_id", m_client_id);
        //outState.putString("client_descr", m_client_descr);
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

        View view = inflater.inflate(R.layout.messages_list_fragment, container, false);

        final FloatingActionButton fabAddMessage=(FloatingActionButton)view.findViewById(R.id.fab_add_message);
        fabAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUniversalEventListener("createMessage", null);
            }
        });


        ListView lvMessages = (ListView)view.findViewById(R.id.listViewMessages);
        lvMessages.setEmptyView(view.findViewById(R.id.empty_messages));

        String[] messagesFromColumns = {"datetime", "descr", "inout", "text", "fname"};
        int[] messagesToViews = {R.id.tvMessagesListDate, R.id.tvMessagesListDetails, R.id.tvMessagesListInOut, R.id.tvMessagesListText, R.id.tvMessagesListPhoto};

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        messagesAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.message_item, null,
                messagesFromColumns, messagesToViews, 0);

        lvMessages.setAdapter(messagesAdapter);
        lvMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long arg) {
                //Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                Cursor cursor = messagesAdapter.getCursor();
                cursor.moveToPosition(position);
                Bundle parameters=new Bundle();
                parameters.putLong("id", cursor.getLong(0));
                parameters.putString("photo", "");
                mListener.onUniversalEventListener("editMessage", parameters);
            }
        });
        lvMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                // TODO Auto-generated method stub
                return false;
            }
        });

        SimpleCursorAdapter.ViewBinder binderM = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                View w1 = (View) view.getParent();

                int acknowledgedIndex = cursor.getColumnIndex("acknowledged");
                int inOutIndex = cursor.getColumnIndex("inout");
                // это сообщение для нас и оно не прочитано
                if ((cursor.getInt(acknowledgedIndex) & (4 | 16)) == 0) {
                    w1.setBackgroundColor(Color.rgb(0, 100, 0));
                } else {
                    w1.setBackgroundColor(Color.TRANSPARENT);
                }
                switch (view.getId()) {
                    case R.id.tvMessagesListDate: {
                        String d = cursor.getString(columnIndex);
                        if (d.length() == 14) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(d.substring(6, 8)).append('.').append(d.substring(4, 6)).append('.').append(d.substring(0, 4)).append(' ').append(d.substring(8, 10)).append(':').append(d.substring(10, 12)).append(':').append(d.substring(12, 14));
                            TextView tv = (TextView) view;
                            tv.setText(sb.toString());
                            return true;
                        }
                    }
                    case R.id.tvMessagesListInOut: {
                        TextView tv = (TextView) view;
                        if (cursor.getInt(inOutIndex) == 0) {
                            tv.setText(R.string.message_type_incoming);
                        } else {
                            tv.setText(R.string.message_type_outgoing);
                        }
                        // SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
                        // @Override
                        return true;
                    }
                }
                return false;
            }
        };

        registerForContextMenu(lvMessages);

        messagesAdapter.setViewBinder(binderM);

        // 26.04.2019
        //LoaderManager.getInstance(this).initLoader(MESSAGES_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(MESSAGES_LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MySingleton g = MySingleton.getInstance();
        switch (id) {
            case MESSAGES_LOADER_ID:
                return new CursorLoader(getActivity(), MTradeContentProvider.MESSAGES_LIST_CONTENT_URI,
                        MESSAGES_LIST_PROJECTION, "isMark<>1", new String[]{}, "messages._id DESC");

        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MESSAGES_LOADER_ID:
                messagesAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        switch (loader.getId()) {
            case MESSAGES_LOADER_ID:
                messagesAdapter.swapCursor(null);
                break;
        }
    }

    public void restartLoaderForListView()
    {
        LoaderManager.getInstance(this).restartLoader(MESSAGES_LOADER_ID, null, this);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MySingleton g = MySingleton.getInstance();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        //menu.setHeaderTitle(getString(R.string.menu_context_title));
        MenuInflater inflater = getActivity().getMenuInflater();
        switch (v.getId()) {
            case R.id.listViewMessages: {
                inflater.inflate(R.menu.main_messages_list, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
        }
        return super.onContextItemSelected(item);
    }
}
