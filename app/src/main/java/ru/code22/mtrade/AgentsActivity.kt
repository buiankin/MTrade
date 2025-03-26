package ru.code22.mtrade

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader

class AgentsActivity : AppCompatActivity(),
    LoaderManager.LoaderCallbacks<Cursor> {
    var mAdapter: SimpleCursorAdapter? = null

    var lvAgents: ListView? = null

    // https://www.geeksforgeeks.org/how-to-read-data-from-sqlite-database-in-android-using-jetpack-compose/

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val g = MySingleton.getInstance()

        g.checkInitByDataAndSetTheme(this)

        setContentView(R.layout.agents)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)


        lvAgents = findViewById<View>(R.id.lvAgents) as ListView
        lvAgents!!.emptyView = findViewById(android.R.id.empty)

        val fromColumns = arrayOf("code", "descr")
        val toViews = intArrayOf(android.R.id.text1, android.R.id.text2)

        mAdapter = SimpleCursorAdapter(
            this,
            android.R.layout.two_line_list_item, null,
            fromColumns, toViews, 0
        )

        lvAgents!!.adapter = mAdapter
        lvAgents!!.onItemClickListener =
            AdapterView.OnItemClickListener { adapter, view, position, id ->
                val intent = Intent()
                intent.putExtra("id", id)
                setResult(AGENTS_RESULT_OK, intent)
                finish()
            }

        // 26.04.2019
        //LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
    }

    override fun onResume() {
        super.onResume()
        LoaderManager.getInstance(this).initLoader(
            LOADER_ID, null,
            this
        )
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.agents, menu)
        return true
    }

    // Called when a new Loader needs to be created
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return CursorLoader(
            this, MTradeContentProvider.AGENTS_CONTENT_URI,
            PROJECTION, null, null, "descr"
        )
    }

    // Called when a previously created loader has finished loading
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        // A switch-case is useful when dealing with multiple Loaders/IDs
        when (loader.id) {
            LOADER_ID ->             // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter!!.swapCursor(data)
        }
        // The listview now displays the queried data.
    }

    // Called when a previously created loader is reset, making the data unavailable
    override fun onLoaderReset(loader: Loader<Cursor>) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter!!.swapCursor(null)
    }

    companion object {
        private const val AGENTS_RESULT_OK = 1

        private const val LOADER_ID = 1

        val PROJECTION: Array<String> = arrayOf("_id", "code", "descr")
    }
}
