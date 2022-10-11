package ru.code22.mtrade;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

// https://github.com/androidessence/RecyclerViewCursorAdapter/tree/master/sample/src/main/java/com/androidessence/recyclerviewcursoradapter/sample

public class NomenclatureGridActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    //static final int QUANTITY_REQUEST = 1;

    static final int NOMENCLATURE_RESULT_DOCUMENT_CHANGED=1;

    private NomenclatureAdapter mAdapter;
    MyNomenclatureGroupAdapter mGroupAdapter;

    ListView lvNomenclatureGroup;
    Spinner sNomenclatureGroup;
    RecyclerView mRecyclerView;

    private static final int NOMENCLATURE_LOADER_ID = 0;

    boolean m_bQuantityChanged;

    List<String> m_list_groups;
    ArrayList<MyNomenclatureGroupAdapter.Tree> m_list2;
    MyDatabase.MyID m_group_id;
    ArrayList<String> m_group_ids;
    boolean m_b_onlyCurrentLevel;

    static boolean m_bInStock=false;
    static boolean m_bPacks=false;
    //static String m_filter="";
    String m_filter;
    static String m_mode="";

    long m_copy_order_id; // если объект введен копированием, здесь будет код копируемого
    // это нужно для того, чтобы при восстановлении приложения восстановились данные (если еще не успела произойти запись в базу, при изменении)
    String m_backup_client_id; // с отбором по какому клиенту был создан заказ
    String m_backup_distr_point_id;

    // Для варианта Common.isHierarchyNomenclatureInTable
    ArrayList<String> m_nomenclatureSurfing;

    ActivityResultLauncher<Intent> launchQuantityRequest;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        MySingleton g=MySingleton.getInstance();
        g.checkInitByDataAndSetTheme(this);
        g.CheckInitImageLoader(this);

		setContentView(R.layout.nomenclature_grid);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_copy_order_id=-1;
        m_backup_client_id=new MyDatabase.MyID().toString();
        m_backup_distr_point_id=new MyDatabase.MyID().toString();

        m_filter="";
        m_group_id=null;
        m_group_ids=null;
        m_bQuantityChanged=false;

        m_nomenclatureSurfing=null;

        m_b_onlyCurrentLevel= PreferenceManager.getDefaultSharedPreferences(this).getBoolean("nomenclature_only_current_level", false);

        String mode=getIntent().getStringExtra("MODE");
        if (mode==null)
        {
            m_mode="";
        }
        else
        {
            m_mode=mode;
        }

        if (getIntent().hasExtra("nomenclature_surfing"))
        {
            m_nomenclatureSurfing = new ArrayList<>(Arrays.asList(getIntent().getStringArrayExtra("nomenclature_surfing")));
        }

        boolean bCreatedInSingleton=false;
        boolean bRecreationActivity=false;

        if (savedInstanceState!=null&&savedInstanceState.containsKey("QuantityChanged"))
        {
            // Чтобы при повороте окна или пересоздании формы не стерся признак
            m_bQuantityChanged=savedInstanceState.getBoolean("QuantityChanged");
        }

        if (m_mode.equals("REFUND"))
        {
            if (savedInstanceState!=null&&savedInstanceState.containsKey("refund_editing"))
            {
                bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
                // но Singleton при этом не всегда уничтожался
                bCreatedInSingleton=HandleBundleRefund(savedInstanceState);
            }

            //if (!bCreatedInSingleton)
            //{
            //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
            //}


            ContentValues cv=new ContentValues();
            cv.put("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
            getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
        } else
        {
            if (savedInstanceState!=null&&savedInstanceState.containsKey("order_editing_id"))
            {
                bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
                bCreatedInSingleton=HandleBundleOrder(savedInstanceState);
            }

            //if (!bCreatedInSingleton)
            //{
            //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
            //}

            // 07.06.2018: окно может быть уничтожено и создано заново
            if (!bRecreationActivity&&g.MyDatabase.m_order_editing_created)
            //if (g.MyDatabase.m_order_editing_id==0)
            {
                //
                // это новый документ
                // контрагент в документ попал автоматически из отбора
                if (!g.MyDatabase.m_order_editing.client_id.isEmpty())
                {
                    // установим договор по умолчанию, если он единственный
                    Cursor defaultAgreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
                    if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
                    {
                        int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
                        //int price_type_idIndex = defaultAgreementCursor.getColumnIndex("price_type_id");
                        Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
                        if (!defaultAgreementCursor.moveToNext())
                        {
                            // Есть единственный договор
                            OrdersHelpers.onAgreementSelected_RestrictedVer(this, null, _idAgreement, null, null);
                        }
                    }
                    defaultAgreementCursor.close();
                }
            }
            Intent intent=getIntent();

            if (intent.hasExtra("copy_order_id"))
            {
                m_copy_order_id=intent.getLongExtra("copy_order_id", -1L);
            }

            if (intent.hasExtra("backup_client_id"))
            {
                m_backup_client_id=intent.getStringExtra("backup_client_id");
            }

            if (!bRecreationActivity&&intent.getBooleanExtra("recalc_price", false))
            {
                OrdersHelpers.recalcPrice(this);
            }

            ContentValues cv=new ContentValues();
            cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());
            OrdersHelpers.fillOrdersHistoryPeriods(this, cv);
            getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
        }

        NomenclatureActivity ob1=(NomenclatureActivity)getLastCustomNonConfigurationInstance();
        if (ob1!=null)
        {
            m_filter=ob1.m_filter;
            m_group_id=ob1.m_group_id;
            m_group_ids=ob1.m_group_ids;
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        lvNomenclatureGroup = (ListView)findViewById(R.id.lvNomenclatureGroup);
        if (lvNomenclatureGroup!=null)
        {
            lvNomenclatureGroup.setEmptyView(findViewById(R.id.emptyGroup));
        }

        sNomenclatureGroup=(Spinner)findViewById(R.id.spinnerGroupNomenclature);

        mRecyclerView = (RecyclerView) findViewById(R.id.grid_view);

        //to enable optimization of recyclerview
        mRecyclerView.setHasFixedSize(true);

        int spanCount=getSpanCount();

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        /*
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mAdapter.getItemViewType(position))
                {
                    case NomenclatureAdapter.TYPE_NOMENCLATURE_GROUP:
                        return 4;
                    case NomenclatureAdapter.TYPE_NOMENCLATURE_ITEM:
                        return 1;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        */

        //String[] fromColumns = {"descr"};
        //int[] toViews = {R.id.textView2};

        //mAdapter = new SimpleCursorAdapterRecyclerAdapter(this,
        //        R.layout.nomenclature_card, null,
        //        fromColumns, toViews, 0);

		// устанавливаем адаптер через экземпляр класса ImageAdapter
        //RecyclerView.setAdapter(new NomenclatureGridImageAdapter(this));
        //recycleriew.setAdapter(mAdapter);

        mAdapter=new NomenclatureAdapter(this, m_mode, m_bInStock, m_bPacks);
        mRecyclerView.setAdapter(mAdapter);
        /*
        recyclerView.addOnItemTouchListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        });
        */

        mAdapter.setOnItemClickListener(new NomenclatureAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                MySingleton g=MySingleton.getInstance();

                // TODO убедиться, что обновление после изменений осуществляется подобным образом
                // Redraw the old selection and the new
                //notifyItemChanged(selectedPos);
                //selectedPos = getLayoutPosition();
                //notifyItemChanged(selectedPos);

                Cursor cursorList=mAdapter.getCursor();
                cursorList.moveToPosition(position);
                long _id=cursorList.getLong(0);

                Intent intent=new Intent(NomenclatureGridActivity.this, QuantityActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("_id", _id);
                intent.putExtra("packs", m_bPacks);
                if (m_mode.equals("REFUND"))
                {
                } else
                {
                    intent.putExtra("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
                    if (g.Common.FACTORY)
                    {
                        intent.putExtra("agreement30_id", g.MyDatabase.m_order_editing.agreement30_id.toString());
                    }
                }
                cursorList.moveToPosition(position);
                int nom_isFolder=cursorList.getColumnIndex("isFolder");
                int isFolder=cursorList.getInt(nom_isFolder);
                // количество вводим только для элементов
                if (isFolder!=1&&isFolder!=3)
                {
                    int nom_quantityIndex=cursorList.getColumnIndex("nom_quantity");
                    intent.putExtra("rest", cursorList.getDouble(nom_quantityIndex));
                    intent.putExtra("MODE", m_mode);

                    //startActivityForResult(intent, QUANTITY_REQUEST);
                    launchQuantityRequest.launch(intent);
                } else
                {
                    // 29.04.2021 если это группа, показываем только ее (и если способ редактирования такой)
                    if (g.Common.isNomenclatureSurfing())
                    {
                        int indexNomenclatureId=cursorList.getColumnIndex("nomenclature_id");
                        String nomenclature_id=cursorList.getString(indexNomenclatureId);
                        if (m_nomenclatureSurfing==null) {
                            m_nomenclatureSurfing = new ArrayList<>();
                            // Сразу добавляем корневой элемент
                            m_nomenclatureSurfing.add(Constants.emptyID);
                            m_nomenclatureSurfing.add(nomenclature_id);
                        } else if (m_nomenclatureSurfing.contains(nomenclature_id)) {
                            // Удалим все элементы ниже текущего
                            int idx=m_nomenclatureSurfing.indexOf(nomenclature_id);
                            while (m_nomenclatureSurfing.size()>idx+1)
                                m_nomenclatureSurfing.remove(idx+1);
                        } else {
                            //m_nomenclatureSurfing.add(Constants.emptyID);
                            m_nomenclatureSurfing.add(nomenclature_id);
                        }
                        // Для отображения открытых папок
                        mAdapter.setNomenclatureSurfing(m_nomenclatureSurfing);

                        LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
                    }

                }
            }

            @Override
            public void onItemLongClick(int position, View v) {
            }
        });

        setNomenclatureSpinnerData(Constants.emptyID);

        mGroupAdapter = new MyNomenclatureGroupAdapter(this);
        mGroupAdapter.setMyListGroups(m_list_groups, m_list2, m_group_id, m_group_ids);
        mGroupAdapter.setOnRedrawListListener(new MyNomenclatureGroupAdapter.RedrawListLisnener() {
            @Override
            public void onRestartLoader() {
                LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
            }
        });

        if (lvNomenclatureGroup!=null) {
            lvNomenclatureGroup.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            lvNomenclatureGroup.setAdapter(mGroupAdapter);
        }

        // устанавливаем обработчик нажатия
        if (sNomenclatureGroup!=null)
        {
            //
            if (m_group_id!=null)
            {
                sNomenclatureGroup.setOnItemSelectedListener(null);
                int i;
                for (i=0; i<m_list2.size(); i++)
                {
                    if (m_group_id.toString().equals(m_list2.get(i).id))
                    {
                        sNomenclatureGroup.setSelection(i);
                    }
                }
            }

            sNomenclatureGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    if (m_list2.get(position).id==null)
                    {
                        m_group_id=null;
                        m_group_ids=null;
                    } else
                    {
                        m_group_id=new MyDatabase.MyID(m_list2.get(position).id);
                        m_group_ids=new ArrayList<String>();
                        int i;
                        int level=m_list2.get(position).level;
                        m_group_ids.add(m_list2.get(position).id);
                        for (i=position+1;i<m_list2.size();i++)
                        {
                            if (m_list2.get(i).level<=level)
                                break;
                            m_group_ids.add(m_list2.get(i).id);
                        }
                    }
                    // m_group_id - это сам элемент
                    // m_group_ids - это подчиненные элементы
                    if (g.Common.isNomenclatureSurfing()) {
                        // Нас интересуют все предки этого элемента
                        m_nomenclatureSurfing=new ArrayList<>();
                        m_nomenclatureSurfing.add(Constants.emptyID);
                        if (m_group_id!=null) {
                            Cursor cursor = getContentResolver().query(MTradeContentProvider.NOMENCLATURE_HIERARCHY_CONTENT_URI, null, "id=?", new String[]{m_group_id.toString()}, null);
                            if (cursor.moveToNext())
                            {
                                int level_idx=cursor.getColumnIndex("level");
                                int level=cursor.getInt(level_idx);
                                int i;
                                for (i=1; i<=level; i++)
                                {
                                    int levelId_idx=cursor.getColumnIndex(String.format("level%d_id", i));
                                    String currentId=cursor.getString(levelId_idx);
                                    m_nomenclatureSurfing.add(currentId);
                                }
                            }
                            cursor.close();
                        }
                        // Для отображения открытых папок
                        mAdapter.setNomenclatureSurfing(m_nomenclatureSurfing);
                        mRecyclerView.scrollToPosition(0);
                    }

                    LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }

            });

        }

        if (m_mode.equals("REFUND"))
        {
            // для возвратов не надо ни остатков, ни цен. но нужна история продаж
            TextDatabase.prepareNomenclatureStuffForRefund(getContentResolver());
        } else
        {
            TextDatabase.prepareNomenclatureStuff(getContentResolver());
        }

        // Переключатель "На складе"
        final ToggleButton buttonNomenclatureInStock=(ToggleButton)findViewById(R.id.toggleButtonNomenclatureInStock);
        buttonNomenclatureInStock.setChecked(m_bInStock);
        if (m_mode.equals("REFUND"))
        {
            buttonNomenclatureInStock.setVisibility(View.GONE);
        } else
        {
            buttonNomenclatureInStock.setVisibility(View.VISIBLE);
        }
        buttonNomenclatureInStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_bInStock=((ToggleButton)v).isChecked();
                mAdapter.setParameters(m_bInStock, m_bPacks);
                LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
            }
        });

        // Переключатель "Упаковки"
        final ToggleButton buttonNomenclaturePacks=(ToggleButton)findViewById(R.id.toggleButtonNomenclaturePacks);
        buttonNomenclaturePacks.setChecked(m_bPacks);
        buttonNomenclaturePacks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_bPacks=((ToggleButton)v).isChecked();
                mAdapter.setParameters(m_bInStock, m_bPacks);
                LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
            }
        });

        if (g.Common.PHARAON)
        {
            buttonNomenclatureInStock.setVisibility(View.GONE);
            buttonNomenclaturePacks.setVisibility(View.GONE);
        }


        launchQuantityRequest = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        /*
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            // your operation....
                        }
                         */

                        if (result.getResultCode()==QuantityActivity.QUANTITY_RESULT_OK)
                        {
                            Intent data = result.getData();
                            m_bQuantityChanged=true;
                            if (data!=null)
                            {
                                if (m_mode.equals("REFUND"))
                                {
                                    MyDatabase.RefundLineRecord line=new MyDatabase.RefundLineRecord();
                                    long _id=data.getLongExtra("_id", 0);
                                    double quantity=data.getDoubleExtra("quantity", 0.0);
                                    double k=data.getDoubleExtra("k", 1.0);
                                    String ed=data.getStringExtra("ed");
                                    String comment_in_line=data.getStringExtra("comment_in_line");

                                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
                                    Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
                                    if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
                                    {
                                        int descrIndex = nomenclatureCursor.getColumnIndex("descr");
                                        int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
                                        int idIndex = nomenclatureCursor.getColumnIndex("id");
                                        int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
                                        line.nomenclature_id=new MyDatabase.MyID(nomenclatureCursor.getString(idIndex));
                                        line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
                                        line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
                                        line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
                                        line.quantity=quantity;
                                        line.quantity_requested=quantity;
                                        line.k=k;
                                        line.ed=ed;
                                        line.comment_in_line=comment_in_line;

                                        // Проверим, есть ли эта номенклатура в документе
                                        boolean bNomenclatureFound=false;
                                        int line_no=0;
                                        while (line_no<g.MyDatabase.m_refund_editing.lines.size())
                                        {
                                            if (g.MyDatabase.m_refund_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
                                            {
                                                if (bNomenclatureFound)
                                                {
                                                    g.MyDatabase.m_refund_editing.lines.remove(line_no);
                                                    continue;
                                                }
                                                else
                                                {
                                                    g.MyDatabase.m_refund_editing.lines.set(line_no, line);
                                                    bNomenclatureFound=true;

                                                    if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
                                                    {
                                                        // Аналогично OrderActivity
                                                        ContentValues cv=new ContentValues();
                                                        cv.put("client_id", ""); // в данном случае это не важно
                                                        cv.put("quantity", quantity);
                                                        cv.put("quantity_requested", quantity);
                                                        cv.put("k", k);
                                                        cv.put("ed", ed);
                                                        cv.put("comment_in_line", comment_in_line);

                                                        int cnt=getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), line.nomenclature_id.toString()});
                                                    }
                                                }
                                            }
                                            line_no++;
                                        }

                                        if (!bNomenclatureFound)
                                        {
                                            g.MyDatabase.m_refund_editing.lines.add(line);

                                            if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
                                            {
                                                ContentValues cv=new ContentValues();
                                                cv.put("nomenclature_id", line.nomenclature_id.toString());
                                                cv.put("refund_id", String.valueOf(g.MyDatabase.m_refund_editing_id));
                                                cv.put("client_id", ""); // в данном случае это не важно
                                                cv.put("quantity", quantity);
                                                cv.put("quantity_requested", quantity);
                                                cv.put("k", k);
                                                cv.put("ed", ed);
                                                cv.put("comment_in_line", comment_in_line);

                                                getContentResolver().insert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv);
                                            }

                                        }
                                        //g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
                                        //setModified();

                                        // В списке у нас указываются продажи, поэтому строку надо перерисовать
                                        Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
                                        getContentResolver().notifyChange(singleUriList, null);

                                    }

                                } else
                                {
                                    //long id=data.getLongExtra("id", 0);
                                    MyDatabase.OrderLineRecord line=new MyDatabase.OrderLineRecord();
                                    long _id=data.getLongExtra("_id", 0);
                                    double quantity=data.getDoubleExtra("quantity", 0.0);
                                    double k=data.getDoubleExtra("k", 1.0);
                                    double price=data.getDoubleExtra("price", 0.0);
                                    double price_k=data.getDoubleExtra("price_k", 1.0);
                                    String ed=data.getStringExtra("ed");
                                    String shipping_time=data.getStringExtra("shipping_time");
                                    String comment_in_line=data.getStringExtra("comment_in_line");

                                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
                                    Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
                                    if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
                                    {
                                        int descrIndex = nomenclatureCursor.getColumnIndex("descr");
                                        int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
                                        int idIndex = nomenclatureCursor.getColumnIndex("id");
                                        int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
                                        line.nomenclature_id=new MyDatabase.MyID(nomenclatureCursor.getString(idIndex));
                                        line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
                                        line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
                                        line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
                                        line.quantity=quantity;
                                        line.quantity_requested=quantity;
                                        line.discount=0.0;
                                        line.k=k;
                                        line.ed=ed;
                                        if (price_k>-0.0001&&price_k<0.0001)
                                        {
                                            price_k=1.0;
                                        }
                                        if (price_k-k>-0.0001&&price_k-k<0.0001)
                                        {
                                            line.price=price;
                                        } else
                                        {
                                            line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
                                        }
                                        line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
                                        line.shipping_time=shipping_time;
                                        line.comment_in_line=comment_in_line;
                                        line.lineno=g.MyDatabase.m_order_editing.getMaxLineno()+1;

                                        // Проверим, есть ли эта номенклатура в документе
                                        boolean bNomenclatureFound=false;
                                        int line_no=0;
                                        while (line_no<g.MyDatabase.m_order_editing.lines.size())
                                        {
                                            if (g.MyDatabase.m_order_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
                                            {
                                                if (bNomenclatureFound)
                                                {
                                                    if (g.Common.NEW_BACKUP_FORMAT)
                                                    {
                                                        // если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
                                                        // когда m_order_new_editing_id=0
                                                        TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
                                                    }
                                                    g.MyDatabase.m_order_editing.lines.remove(line_no);
                                                    continue;
                                                }
                                                else
                                                {
                                                    // Используем старый номер строки
                                                    line.lineno=g.MyDatabase.m_order_editing.lines.get(line_no).lineno;
                                                    g.MyDatabase.m_order_editing.lines.set(line_no, line);
                                                    bNomenclatureFound=true;
                                                    if (g.Common.NEW_BACKUP_FORMAT)
                                                    {
                                                        if (g.MyDatabase.m_order_new_editing_id==0)
                                                        {
                                                            // Что-то поменяли в документе впервые, записываем документ
                                                            g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
                                                            // editing_backup (последний параметр)
                                                            // 0-документ в нормальном состоянии
                                                            // 1-новый документ, записать не успели
                                                            // 2-документ начали редактировать, но не записали и не отменили изменения
                                                            g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
                                                        } else
                                                        {
                                                            TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
                                                        }
                                                    }
                                                }
                                            }
                                            line_no++;
                                        }

                                        if (!bNomenclatureFound)
                                        {
                                            g.MyDatabase.m_order_editing.lines.add(line);
                                            if (g.Common.NEW_BACKUP_FORMAT)
                                            {
                                                if (g.MyDatabase.m_order_new_editing_id==0)
                                                {
                                                    // Что-то поменяли в документе впервые, записываем документ
                                                    g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
                                                    // editing_backup (последний параметр)
                                                    // 0-документ в нормальном состоянии
                                                    // 1-новый документ, записать не успели
                                                    // 2-документ начали редактировать, но не записали и не отменили изменения
                                                    g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
                                                } else
                                                {
                                                    TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
                                                }
                                            }

                                        }
                                        //g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
                                        //setModified();

                                        // В списке у нас указываются продажи, поэтому строку надо перерисовать
                                        Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
                                        getContentResolver().notifyChange(singleUriList, null);

                                    }
                                }
                            }
                        }



                    }
                });

	}

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public void onNewIntent(final Intent queryIntent) {
        super.onNewIntent(queryIntent);
        setIntent(queryIntent);

        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction))
        {
            //Toast.makeText(this, queryIntent.getStringExtra(SearchManager.QUERY), Toast.LENGTH_LONG).show();
            m_filter=queryIntent.getStringExtra(SearchManager.QUERY);
            m_filter=m_filter.toLowerCase(Locale.getDefault());
            LoaderManager.getInstance(this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nomenclature, menu);

        // http://android-developers.blogspot.ru/2013/08/actionbarcompat-and-io-2013-app-source.html

        //MenuItem shareItem = menu.findItem(R.id.action_search2);
        //ShareActionProvider mActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        // http://habrahabr.ru/post/189680/
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        // Assumes current activity is the searchable activity

        if (searchView!=null)
        {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {

                @Override
                public boolean onClose() {
                    m_filter="";
                    LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
                    return false;
                }
            });
        }


        return true;
    }

    private void setNomenclatureSpinnerData(String parent_id)
    {
        MySingleton g=MySingleton.getInstance();
        // TODO здесь можно взять данные из hierarchy table
        Spinner spinner = (Spinner) findViewById(R.id.spinnerGroupNomenclature);
        //ListView lvHierarchy = (ListView) findViewById(R.id.lvNomenclatureGroup);

        m_list_groups = new ArrayList<String>();
        ContentResolver contentResolver=getContentResolver();
        String[] projection =
                {
                        "_id",
                        "id",
                        "parent_id",
                        "descr"
                };
        m_list2 = new ArrayList<MyNomenclatureGroupAdapter.Tree>();


        Cursor cursor;
        if (g.Common.PHARAON)
        {
            cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
        } else
        {
            cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "descr");
        }
        if (cursor!=null)
        {
            int indexDescr = cursor.getColumnIndex("descr");
            int indexId = cursor.getColumnIndex("id");
            int indexParentId = cursor.getColumnIndex("parent_id");
            int index_Id = cursor.getColumnIndex("_id");
            MyNomenclatureGroupAdapter.Tree t = new MyNomenclatureGroupAdapter.Tree();
            t.descr=getResources().getString(R.string.catalogue_all);
            t.id=null;
            t.parent_id=null;
            t.level=0;
            m_list2.add(t);
            t = new MyNomenclatureGroupAdapter.Tree();
            t.descr=getResources().getString(R.string.catalogue_node);
            t.id="     0   ";
            t.parent_id="";
            t.level=0;
            m_list2.add(t);
            // Сначала просто заполняем список
            while (cursor.moveToNext())
            {
                t = new MyNomenclatureGroupAdapter.Tree();
                t.descr=cursor.getString(indexDescr);
                t.id=cursor.getString(indexId);
                t.parent_id=cursor.getString(indexParentId);
                t._id=cursor.getString(index_Id);
                t.level=0;
                m_list2.add(t);
            }
            // А потом сортируем его
            int i;
            // с единицы потому, что в нуле у нас записан null
            for (i=1; i<m_list2.size(); i++)
            {
                int offset=i+1;
                int j;
                for (j=i+1; j<m_list2.size(); j++)
                {
                    if (m_list2.get(j).parent_id.equals(m_list2.get(i).id))
                    {
                        m_list2.get(j).level=m_list2.get(i).level+1;
                        //Collections.swap(m_list2, offset, j);
                        m_list2.add(offset, m_list2.get(j));
                        m_list2.remove(j+1);
                        offset++;
                    }
                }
            }
            for (i=0; i<m_list2.size(); i++)
            {
                String spaces="";
                int j;
	    		/*
	    		for (j=1; j<m_list2.get(i).level; j++)
	    		{
	    			spaces+=" ";
	    		}
	    		if (m_list2.get(i).level>0)
	    			spaces+="> ";
	    		*/
                for (j=0; j<m_list2.get(i).level; j++)
                {
                    spaces+=">";
                }
                if (m_list2.get(i).level>0)
                    spaces+=" ";
                m_list_groups.add(spaces+m_list2.get(i).descr);
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner!=null)
        {
            spinner.setAdapter(dataAdapter);
        }


    }
    public boolean HandleBundleRefund(Bundle savedInstanceState)
    {
        boolean bCreatedInSingleton=false;
        MySingleton g=MySingleton.getInstance();
        if (g.MyDatabase==null)
        {
            g.MyDatabase = new MyDatabase();
            g.MyDatabase.m_order_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_refund_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_distribs_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_payment_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_message_editing.bCreatedInSingleton=true;
        }
        bCreatedInSingleton=g.MyDatabase.m_refund_editing.bCreatedInSingleton;

        g.MyDatabase.m_refund_editing_id=savedInstanceState.getLong("refund_editing_id");
        g.MyDatabase.m_refund_editing_created=savedInstanceState.getBoolean("refund_editing_created");
        g.MyDatabase.m_refund_editing_modified=savedInstanceState.getBoolean("refund_editing_modified");

        g.MyDatabase.m_refund_editing=savedInstanceState.getParcelable("refund_editing");

        //m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
        //m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
        //m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
        //m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");

        return bCreatedInSingleton;
    }

    public boolean HandleBundleOrder(Bundle savedInstanceState)
    {
        boolean bCreatedInSingleton=false;
        MySingleton g=MySingleton.getInstance();
        if (g.MyDatabase==null)
        {
            g.MyDatabase = new MyDatabase();
            g.MyDatabase.m_order_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_refund_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_distribs_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_payment_editing.bCreatedInSingleton=true;
            g.MyDatabase.m_message_editing.bCreatedInSingleton=true;
        }
        bCreatedInSingleton=g.MyDatabase.m_order_editing.bCreatedInSingleton;

        g.MyDatabase.m_order_editing_id=savedInstanceState.getLong("order_editing_id");
        g.MyDatabase.m_order_new_editing_id=savedInstanceState.getLong("order_new_editing_id");
        g.MyDatabase.m_order_editing_created=savedInstanceState.getBoolean("order_editing_created");
        g.MyDatabase.m_order_editing_modified=savedInstanceState.getBoolean("order_editing_modified");
        m_copy_order_id=savedInstanceState.getLong("copy_order_id");
        m_backup_client_id=savedInstanceState.getString("backup_client_id");
        m_backup_distr_point_id=savedInstanceState.getString("backup_distr_point_id");

        //m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
        //m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
        //m_list_prices_descr = savedInstanceState.getStringArray("list_prices_descr");
        //m_list_prices_id = savedInstanceState.getStringArray("list_prices_id");
        //m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
        //m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");

        if (g.MyDatabase.m_order_editing.bCreatedInSingleton)
        {
            Intent intent=new Intent(); // Лишь бы что-нибудь было
            if (g.MyDatabase.m_order_new_editing_id>0)
            {
                Long id1=g.MyDatabase.m_order_new_editing_id;
                Long id2=g.MyDatabase.m_order_editing_id;
                //TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id);
                OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_new_editing_id, new MyDatabase.MyID(), new MyDatabase.MyID(), false);
                g.MyDatabase.m_order_new_editing_id=id1;
                g.MyDatabase.m_order_editing_id=id2;
                g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
            } else if (g.MyDatabase.m_order_editing_id>0)
            {
                //TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id);
                OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_editing_id, new MyDatabase.MyID(), new MyDatabase.MyID(), false);
                g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
            } else
            {
                // Документ был создан, но не записан (не было никаких изменений),
                // после этого все было выгружено из памяти
                if (m_copy_order_id>0)
                {
                    //
                    OrdersHelpers.PrepareData(intent, this, m_copy_order_id, new MyDatabase.MyID(m_backup_client_id), new MyDatabase.MyID(m_backup_distr_point_id), true);
                } else
                {
                    // Создадим новый
                    OrdersHelpers.PrepareData(intent, this, 0, new MyDatabase.MyID(m_backup_client_id), new MyDatabase.MyID(m_backup_distr_point_id), false);
                }
            }
        }
        return bCreatedInSingleton;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MySingleton g=MySingleton.getInstance();
        if (m_mode.equals("REFUND"))
        {
            outState.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
            outState.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
            outState.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
            outState.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
        } else
        {
            outState.putLong("order_editing_id", g.MyDatabase.m_order_editing_id);
            outState.putLong("order_new_editing_id", g.MyDatabase.m_order_new_editing_id);
            outState.putBoolean("order_editing_created", g.MyDatabase.m_order_editing_created);
            outState.putBoolean("order_editing_modified", g.MyDatabase.m_order_editing_modified);
            outState.putLong("copy_order_id", m_copy_order_id);
            outState.putString("backup_client_id", m_backup_client_id);
            outState.putString("backup_distr_point_id", m_backup_distr_point_id);

            //outState.putStringArray("list_accounts_descr", m_list_accounts_descr);
            //outState.putStringArray("list_accounts_id", m_list_accounts_id);
            //outState.putStringArray("list_prices_descr", m_list_prices_descr);
            //outState.putStringArray("list_prices_id", m_list_prices_id);
            //outState.putStringArray("list_stocks_descr", m_list_stocks_descr);
            //outState.putStringArray("list_stocks_id", m_list_stocks_id);
        }
        outState.putBoolean("QuantityChanged", m_bQuantityChanged);
        if (m_nomenclatureSurfing!=null) {
            outState.putStringArrayList("nomenclature_surfing", m_nomenclatureSurfing);
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        // Если поменялось значение в настройках
        int spanCount=getSpanCount();
        StaggeredGridLayoutManager layoutManager=(StaggeredGridLayoutManager)mRecyclerView.getLayoutManager();
        if (layoutManager!=null) {
            if (layoutManager.getSpanCount()!=spanCount)
                layoutManager.setSpanCount(spanCount);
        }
        // Initialize Loader
        LoaderManager.getInstance(this).initLoader(NOMENCLATURE_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        MySingleton g=MySingleton.getInstance();

        switch(id) {
            case NOMENCLATURE_LOADER_ID: {

                String conditionString = null;
                ArrayList<String> conditionArgs = new ArrayList<>();

                String orderString=null;
                ArrayList<String> orderArgs = new ArrayList<>();

                if (g.Common.isNomenclatureSurfing()) {
                    if (m_nomenclatureSurfing == null || m_nomenclatureSurfing.size() == 0) {

                        m_nomenclatureSurfing=new ArrayList<>();
                        m_nomenclatureSurfing.add(Constants.emptyID);
                    }

                    ArrayList<String> surfingWhereArgs = new ArrayList<>();
                    ArrayList<String> surfingOrdersArgs = new ArrayList<>();

                    StringBuilder sbWhere = new StringBuilder();
                    StringBuilder sbOrder = new StringBuilder();

                    int idx = 0;
                    for (String s : m_nomenclatureSurfing) {
                        // Отбор
                        if (idx == 0)
                            sbWhere.append("nomenclature.id in(");
                        else
                            sbWhere.append(",");
                        sbWhere.append("?");
                        surfingWhereArgs.add(s);
                        // Сортировка
                        if (idx == 0)
                            sbOrder.append("case nomenclature.id");
                        sbOrder.append(String.format(" when ? then %d", idx));
                        surfingOrdersArgs.add(s);
                        idx++;
                    }
                    sbWhere.append(")");
                    sbOrder.append(String.format(" else %d end", idx)); // это уже элементы, у них своя сортировка

                    // и для элементов текущего уровня еще условие
                    sbWhere.append(" or parent_id=?");
                    surfingWhereArgs.add(m_nomenclatureSurfing.get(m_nomenclatureSurfing.size() - 1));

                    String surfingWhereString = sbWhere.toString();
                    String surfingOrderString = sbOrder.toString();

                    conditionString = Common.combineConditions(conditionString, conditionArgs, surfingWhereString, surfingWhereArgs);
                    orderString=Common.combineOrders(orderString, orderArgs, surfingOrderString, surfingOrdersArgs);
                    /*
                    } else
                    {
                        // В начале сам корневой элемент не нужен, поэтому вторая часть условия
                        conditionString = Common.combineConditions(conditionString, conditionArgs, "parent_id=? and nomenclature.id<>?", new String[]{Constants.emptyID, Constants.emptyID});
                        //orderString = Common.combineOrders(orderString, orderArgs, "case when nomenclature.id=? then 0 else 1 end", Arrays.asList(new String[]{Constants.emptyID}));
                    }
                     */
                }

                boolean bInStock=(m_bInStock&&!m_mode.equals("REFUND"));
                if (m_group_id==null)
                {
                    if (g.Common.PHARAON)
                    {
                        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                                NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, "isFolder<>3"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), null, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
                    } else
                    {
                        if (g.Common.isNomenclatureSurfing()) {
                            //conditionString=Common.combineConditions(conditionString, conditionArgs, +(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{});
                            if (bInStock)
                                conditionString=Common.combineConditions(conditionString, conditionArgs, "isFolder=1 or isFolder=3 or nom_quantity>0", new String[]{});
                            if (!m_filter.isEmpty())
                                conditionString=Common.combineConditions(conditionString, conditionArgs,"descr_lower like '%"+m_filter+"%'", new String[]{});
                            // дальше как обычно
                            return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_SURFING_CONTENT_URI,
                                    NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
                        }
                        conditionString=Common.combineConditions(conditionString, conditionArgs, "isFolder<>3"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{});
                        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                                NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");

                    }
                }
                StringBuilder sb=new StringBuilder();
                String[] args2=new String[m_group_ids.size()];
                int i;
                for (i=0;i<m_group_ids.size();i++)
                {
                    if (i!=0)
                        sb.append(',');
                    sb.append('?');
                    args2[i]=m_group_ids.get(i);
                }
                if (m_b_onlyCurrentLevel)
                {
                    // isFolder и т.п. в сортировке, конечно, лишние
                    if (g.Common.PHARAON)
                    {
                        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                                NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, "isFolder=2 and parent_id=?"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{m_group_id.toString()}, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
                    } else
                    {
                        conditionString=Common.combineConditions(conditionString, conditionArgs, "isFolder=2 and parent_id=?"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{m_group_id.toString()});

                        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                                NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
                    }

                }
                // TODO вспомнить, в каких случаях isFolder=3 (иерархия?)
                if (g.Common.PHARAON)
                {
                    return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                            NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
                } else
                {
                    if (g.Common.isNomenclatureSurfing())
                    {
                        //conditionString=Common.combineConditions(conditionString, conditionArgs, "parent_id IN ("+sb.toString()+")"+(bInStock?" and (isFolder=1 or isFolder=3 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2);
                        // parentId здесь не проверяем потому, что мы уже в conditionString перечислили иерархию
                        if (bInStock)
                            conditionString=Common.combineConditions(conditionString, conditionArgs, "isFolder=1 or isFolder=3 or nom_quantity>0", new String[]{});
                        if (!m_filter.isEmpty())
                            conditionString=Common.combineConditions(conditionString, conditionArgs,"descr_lower like '%"+m_filter+"%'", args2);
                        // дальше как обычно
                        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_SURFING_CONTENT_URI,
                                NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
                    }
                    conditionString=Common.combineConditions(conditionString, conditionArgs, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2);
                    return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
                            NomenclatureAdapter.NOMENCLATURE_LIST_COLUMNS, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
                }
            }
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()) {
            case NOMENCLATURE_LOADER_ID:
                mAdapter.swapCursor(data);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch(loader.getId()) {
            case NOMENCLATURE_LOADER_ID:
                mAdapter.swapCursor(null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown loader id: " + loader.getId());
        }
    }

    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MySingleton g=MySingleton.getInstance();

        //String name = data.getStringExtra("name");
        //tvName.setText("Your name is " + name);
        switch (requestCode)
        {
            case QUANTITY_REQUEST:
                if (resultCode==QuantityActivity.QUANTITY_RESULT_OK)
                {
                    m_bQuantityChanged=true;
                    if (data!=null)
                    {
                        if (m_mode.equals("REFUND"))
                        {
                            MyDatabase.RefundLineRecord line=new MyDatabase.RefundLineRecord();
                            long _id=data.getLongExtra("_id", 0);
                            double quantity=data.getDoubleExtra("quantity", 0.0);
                            double k=data.getDoubleExtra("k", 1.0);
                            String ed=data.getStringExtra("ed");
                            String comment_in_line=data.getStringExtra("comment_in_line");

                            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
                            Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
                            if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
                            {
                                int descrIndex = nomenclatureCursor.getColumnIndex("descr");
                                int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
                                int idIndex = nomenclatureCursor.getColumnIndex("id");
                                int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
                                line.nomenclature_id=new MyDatabase.MyID(nomenclatureCursor.getString(idIndex));
                                line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
                                line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
                                line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
                                line.quantity=quantity;
                                line.quantity_requested=quantity;
                                line.k=k;
                                line.ed=ed;
                                line.comment_in_line=comment_in_line;

                                // Проверим, есть ли эта номенклатура в документе
                                boolean bNomenclatureFound=false;
                                int line_no=0;
                                while (line_no<g.MyDatabase.m_refund_editing.lines.size())
                                {
                                    if (g.MyDatabase.m_refund_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
                                    {
                                        if (bNomenclatureFound)
                                        {
                                            g.MyDatabase.m_refund_editing.lines.remove(line_no);
                                            continue;
                                        }
                                        else
                                        {
                                            g.MyDatabase.m_refund_editing.lines.set(line_no, line);
                                            bNomenclatureFound=true;

                                            if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
                                            {
                                                // Аналогично OrderActivity
                                                ContentValues cv=new ContentValues();
                                                cv.put("client_id", ""); // в данном случае это не важно
                                                cv.put("quantity", quantity);
                                                cv.put("quantity_requested", quantity);
                                                cv.put("k", k);
                                                cv.put("ed", ed);
                                                cv.put("comment_in_line", comment_in_line);

                                                int cnt=getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), line.nomenclature_id.toString()});
                                            }
                                        }
                                    }
                                    line_no++;
                                }

                                if (!bNomenclatureFound)
                                {
                                    g.MyDatabase.m_refund_editing.lines.add(line);

                                    if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
                                    {
                                        ContentValues cv=new ContentValues();
                                        cv.put("nomenclature_id", line.nomenclature_id.toString());
                                        cv.put("refund_id", String.valueOf(g.MyDatabase.m_refund_editing_id));
                                        cv.put("client_id", ""); // в данном случае это не важно
                                        cv.put("quantity", quantity);
                                        cv.put("quantity_requested", quantity);
                                        cv.put("k", k);
                                        cv.put("ed", ed);
                                        cv.put("comment_in_line", comment_in_line);

                                        getContentResolver().insert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv);
                                    }

                                }
                                //g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
                                //setModified();

                                // В списке у нас указываются продажи, поэтому строку надо перерисовать
                                Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
                                getContentResolver().notifyChange(singleUriList, null);

                            }

                        } else
                        {
                            //long id=data.getLongExtra("id", 0);
                            MyDatabase.OrderLineRecord line=new MyDatabase.OrderLineRecord();
                            long _id=data.getLongExtra("_id", 0);
                            double quantity=data.getDoubleExtra("quantity", 0.0);
                            double k=data.getDoubleExtra("k", 1.0);
                            double price=data.getDoubleExtra("price", 0.0);
                            double price_k=data.getDoubleExtra("price_k", 1.0);
                            String ed=data.getStringExtra("ed");
                            String shipping_time=data.getStringExtra("shipping_time");
                            String comment_in_line=data.getStringExtra("comment_in_line");

                            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
                            Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
                            if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
                            {
                                int descrIndex = nomenclatureCursor.getColumnIndex("descr");
                                int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
                                int idIndex = nomenclatureCursor.getColumnIndex("id");
                                int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
                                line.nomenclature_id=new MyDatabase.MyID(nomenclatureCursor.getString(idIndex));
                                line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
                                line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
                                line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
                                line.quantity=quantity;
                                line.quantity_requested=quantity;
                                line.discount=0.0;
                                line.k=k;
                                line.ed=ed;
                                if (price_k>-0.0001&&price_k<0.0001)
                                {
                                    price_k=1.0;
                                }
                                if (price_k-k>-0.0001&&price_k-k<0.0001)
                                {
                                    line.price=price;
                                } else
                                {
                                    line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
                                }
                                line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
                                line.shipping_time=shipping_time;
                                line.comment_in_line=comment_in_line;
                                line.lineno=g.MyDatabase.m_order_editing.getMaxLineno()+1;

                                // Проверим, есть ли эта номенклатура в документе
                                boolean bNomenclatureFound=false;
                                int line_no=0;
                                while (line_no<g.MyDatabase.m_order_editing.lines.size())
                                {
                                    if (g.MyDatabase.m_order_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
                                    {
                                        if (bNomenclatureFound)
                                        {
                                            if (g.Common.NEW_BACKUP_FORMAT)
                                            {
                                                // если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
                                                // когда m_order_new_editing_id=0
                                                TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
                                            }
                                            g.MyDatabase.m_order_editing.lines.remove(line_no);
                                            continue;
                                        }
                                        else
                                        {
                                            // Используем старый номер строки
                                            line.lineno=g.MyDatabase.m_order_editing.lines.get(line_no).lineno;
                                            g.MyDatabase.m_order_editing.lines.set(line_no, line);
                                            bNomenclatureFound=true;
                                            if (g.Common.NEW_BACKUP_FORMAT)
                                            {
                                                if (g.MyDatabase.m_order_new_editing_id==0)
                                                {
                                                    // Что-то поменяли в документе впервые, записываем документ
                                                    g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
                                                    // editing_backup (последний параметр)
                                                    // 0-документ в нормальном состоянии
                                                    // 1-новый документ, записать не успели
                                                    // 2-документ начали редактировать, но не записали и не отменили изменения
                                                    g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
                                                } else
                                                {
                                                    TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
                                                }
                                            }
                                        }
                                    }
                                    line_no++;
                                }

                                if (!bNomenclatureFound)
                                {
                                    g.MyDatabase.m_order_editing.lines.add(line);
                                    if (g.Common.NEW_BACKUP_FORMAT)
                                    {
                                        if (g.MyDatabase.m_order_new_editing_id==0)
                                        {
                                            // Что-то поменяли в документе впервые, записываем документ
                                            g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
                                            // editing_backup (последний параметр)
                                            // 0-документ в нормальном состоянии
                                            // 1-новый документ, записать не успели
                                            // 2-документ начали редактировать, но не записали и не отменили изменения
                                            g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
                                        } else
                                        {
                                            TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
                                        }
                                    }

                                }
                                //g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
                                //setModified();

                                // В списке у нас указываются продажи, поэтому строку надо перерисовать
                                Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
                                getContentResolver().notifyChange(singleUriList, null);

                            }
                        }
                    }
                }
                break;
        }
    }
    */

    private boolean checkGoNode()
    {
        // если используется новая иерархия номенклатуры, сначала переходим в корень, а уже потом закрываем
        MySingleton g=MySingleton.getInstance();
        if (g.Common.isNomenclatureSurfing()) {
            if (m_nomenclatureSurfing == null||m_nomenclatureSurfing.size()<=1)
                return false;
            m_nomenclatureSurfing = new ArrayList<>();
            m_nomenclatureSurfing.add(Constants.emptyID);
            mAdapter.setNomenclatureSurfing(m_nomenclatureSurfing);
            LoaderManager.getInstance(NomenclatureGridActivity.this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
            mRecyclerView.scrollToPosition(0);
            return true;
        }
        return false;
    }

    private void onCloseActivity()
    {
        if (m_bQuantityChanged)
        {
            MySingleton g=MySingleton.getInstance();
            Intent intent=new Intent();
            if (m_mode.equals("REFUND"))
            {
                // Важно, чтобы эти значения имели приоритет перед теми, что вводились в документе (RefundActivity) до этого
                Bundle bundle=new Bundle();
                bundle.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
                bundle.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
                bundle.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
                bundle.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
                intent.putExtras(bundle);
            }
            setResult(NOMENCLATURE_RESULT_DOCUMENT_CHANGED, intent);
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent=new Intent(NomenclatureGridActivity.this, PrefNomenclatureGridActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!checkGoNode())
                onCloseActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Alternative variant for API 5 and higher
    @Override
    public void onBackPressed() {
        if (!checkGoNode())
    	    onCloseActivity();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("nomenclature_only_current_level"))
        {
            m_b_onlyCurrentLevel=prefs.getBoolean("nomenclature_only_current_level", false);
            LoaderManager.getInstance(this).restartLoader(NOMENCLATURE_LOADER_ID, null, NomenclatureGridActivity.this);
        }
    }


    public int getSpanCount() {
        boolean orientationLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int spanCount = pref.getInt(orientationLandscape ? "nomenclatureListColumnsLandscape" : "nomenclatureListColumnsPortrait", 2);
        int maxSpanCount = orientationLandscape ? 15:10;
        if (spanCount < 2)
            spanCount = 2;
        if (spanCount > maxSpanCount)
            spanCount = maxSpanCount;

        return spanCount;
    }

}
