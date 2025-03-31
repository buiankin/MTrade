package ru.code22.mtrade

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.code22.mtrade.models.AgentsModel

import ru.code22.mtrade.ui.theme.MTradeTheme

// https://www.geeksforgeeks.org/how-to-read-data-from-sqlite-database-in-android-using-jetpack-compose/




class AgentsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainContent()
        }
    }


    @Composable
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

    fun MainContent() {

        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        var mDisplayMenu by remember { mutableStateOf(false) }
        val mContext = LocalContext.current

        val settingDataStore = DataStoreManager(this)

        setContent {
            MTradeTheme(darkTheme = true) {
                Surface(
                    // on below line we are specifying modifier and color for our app
                    modifier = Modifier.fillMaxSize()//, color = MaterialTheme.colorScheme.background
                ) {
                    // on the below line we are specifying the theme as the scaffold.
                    Scaffold(
                        // in scaffold we are specifying the top bar.
                        topBar = {
                            // inside top bar we are specifying background color.
                            TopAppBar(
                                //modifier = Modifier.heightIn(max = 56.dp),
                                // along with that we are specifying title for our top bar.
                                title = {
                                    // in the top bar we are specifying tile as a text
                                    Text(
                                        // on below line we are specifying
                                        // text to display in top app bar.
                                        text = getString(R.string.title_activity_agents),

                                        // on below line we are specifying
                                        // modifier to fill max width.
                                        modifier = Modifier.fillMaxWidth(),

                                        // on below line we are specifying
                                        // text alignment.
                                        textAlign = TextAlign.Start,

                                        // on below line we are specifying
                                        // color for our text.
                                        //color = Color.Magenta
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                actions = {

                                    // Creating Icon button favorites, on click
                                    // would create a Toast message
                                    IconButton(onClick = { Toast.makeText(mContext, "Favorite", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Favorite, "")
                                    }

                                    // Creating Icon button for dropdown menu
                                    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                                        Icon(Icons.Default.MoreVert, "")
                                    }

                                    // Creating a dropdown menu
                                    DropdownMenu(
                                        expanded = mDisplayMenu,
                                        onDismissRequest = { mDisplayMenu = false }
                                    ) {

                                        // Creating dropdown menu item, on click
                                        // would create a Toast message
                                        DropdownMenuItem(text = {Text("Settings")}, onClick = { Toast.makeText(mContext, "Settings", Toast.LENGTH_SHORT).show() })

                                        // Creating dropdown menu item, on click
                                        // would create a Toast message
                                        DropdownMenuItem(text = {Text("Logout")}, onClick = { Toast.makeText(mContext, "Logout", Toast.LENGTH_SHORT).show() })
                                    }
                                }
                            )
                        }) { innerPadding ->
                        // on below line we are calling our method to display UI
                        readDataFromDatabase(LocalContext.current, Modifier.padding(innerPadding).fillMaxSize())
                    }
                }
            }
        }
    }

    fun onItemClick(id: Long): Unit {
        val intent = Intent()
        intent.putExtra("id", id)
        setResult(AGENTS_RESULT_OK, intent)
        finish()
    }

    @Composable
    fun readDataFromDatabase(context: Context, modifier: Modifier) {
        // on below line we are creating and initializing our array list
        lateinit var courseList: List<AgentsModel>
        courseList = ArrayList<AgentsModel>()

        val dbHandler = MTradeDBHelper(context);
        courseList = dbHandler.readAgents()

        // on below line we are creating a lazy column for displaying a list view.
        LazyColumn(modifier=modifier) {
            // on below line we are setting data for each item of our listview.
            items(courseList.size, key = {item-> courseList[item]._id}) { item ->
                // on below line we are creating a card for our list view item.
                Card(
                    // on below line we are adding padding from our all sides.
                    modifier = Modifier.padding(8.dp),
                    // on below line we are adding elevation for the card.
                    //elevation = 6.dp
                ) {
                    // on below line we are creating a row for our list view item.
                    Column(
                        // for our row we are adding modifier to set padding from all sides.
                        modifier = Modifier.padding(8.dp).fillMaxWidth().clickable{
                            onItemClick( courseList[item]._id)
                        },
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // on the below line we are creating a text.
                        Text(
                            // inside the text on below line we are
                            // setting text as the language name
                            // from our model class.
                            text = courseList[item].code,

                            // on below line we are adding padding
                            // for our text from all sides.
                            modifier = Modifier.padding(4.dp),

                            // on below line we are adding color for our text
                            //color = Color.Black, textAlign = TextAlign.Center
                        )
                        // on below line inside row we are adding spacer
                        Spacer(modifier = Modifier.width(5.dp))

                        // on the below line we are creating a text.
                        Text(
                            // inside the text on below line we are
                            // setting text as the language name
                            // from our model class.
                            text = "Course Tracks : " + courseList[item].descr,

                            // on below line we are adding padding
                            // for our text from all sides.
                            modifier = Modifier.padding(4.dp),

                            // on below line we are adding color for our text
                            //color = Color.Black, textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.agents, menu)
        return true
    }


    companion object {
        const val AGENTS_RESULT_OK = 1
    }
}

/*
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

*/