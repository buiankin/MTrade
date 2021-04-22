package ru.code22.mtrade;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class RoutesWithDatesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    SimpleCursorAdapter mAdapter;

    private static final int LOADER_ID = 1;

    static final String[] PROJECTION = new String[] {"_id", "route_date", "descr"};

    ListView lvRoutesWithDates;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MySingleton g=MySingleton.getInstance();

        g.checkInitByDataAndSetTheme(this);

        setContentView(R.layout.routes_with_dates);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvRoutesWithDates = (ListView)findViewById(R.id.lvRoutesWithDates);
        lvRoutesWithDates.setEmptyView(findViewById(android.R.id.empty));

        String[] fromColumns = {"route_date", "descr"};
        int[] toViews = {android.R.id.text1, android.R.id.text2};

        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item, null,
                fromColumns, toViews, 0);

        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                String name = cursor.getColumnName(columnIndex);
                if ("route_date".equals(name)) {
                    TextView tv = (TextView) view;
                    String d = cursor.getString(columnIndex);
                    tv.setText(Common.dateStringAsText(d));
                    return true;
                }
                return false;
            }
        };
        mAdapter.setViewBinder(binder);

        lvRoutesWithDates.setAdapter(mAdapter);
        lvRoutesWithDates.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,
                                    long id) {
                Intent intent = new Intent();
                intent.putExtra("id", id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        // 26.04.2019
        //LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.routes_with_dates, menu);
        //return true;
        return false;
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI,
                PROJECTION, null, null, "route_date desc");
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter.swapCursor(data);
                break;
        }
        // The listview now displays the queried data.
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

}
