package ru.code22.mtrade;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RoutesListFragment extends Fragment {

    private ActivityResultLauncher<Intent> selectRoutesWithDatesActivityResultLauncher;
    private UniversalInterface mListener;

    private boolean m_bDataInited;

    private String m_route_id;
    private String m_route_date;
    private String m_route_descr;

    int defaultTextColor;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    //private static final int ROUTES_LOADER_ID = 1;

    // This is the Adapter being used to display the list's data
    RoutesAdapter routesAdapter;

    public RoutesListFragment() {
        m_bDataInited=false;
    }

    void readDataFromBundle(Bundle bundle) {
        m_bDataInited = true;
        if (bundle != null) {
            m_route_id = bundle.getString("route_id");
            m_route_date = bundle.getString("route_date");
            m_route_descr = bundle.getString("route_descr");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        outState.putString("route_id", m_route_id);
        outState.putString("route_date", m_route_date);
        outState.putString("route_descr", m_route_descr);
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

        m_bDataInited=true;

        if (savedInstanceState==null)
        {
            // Если данные не сохранялись, берем установленные прогрммно при создании фрейма
            savedInstanceState=getArguments();
        }

        readDataFromBundle(savedInstanceState);

        selectRoutesWithDatesActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RoutesWithDatesActivity.ROUTES_WITH_DATES_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                long id = data.getLongExtra("id", 0);
                                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI, id);
                                //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
                                Cursor cursor = getActivity().getContentResolver().query(singleUri, new String[]{"route_id", "route_date", "descr"}, null, null, null);
                                if (cursor.moveToNext()) {
                                    int indexRouteId = cursor.getColumnIndex("route_id");
                                    int indexRouteDate = cursor.getColumnIndex("route_date");
                                    int indexRouteDescr = cursor.getColumnIndex("descr");

                                    Bundle parameters = new Bundle();
                                    parameters.putString("route_id", cursor.getString(indexRouteId));
                                    parameters.putString("route_date", cursor.getString(indexRouteDate));
                                    parameters.putString("route_descr", cursor.getString(indexRouteDescr));

                                    cursor.close();

                                    mListener.onUniversalEventListener("routeAndDateSelected", parameters);
                                }
                            }
                        }
                    }
                });


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean bOnlyWorkedOut=sharedPreferences.getBoolean("routes_show_only_worked_out", false);

        routesAdapter = new RoutesAdapter(getActivity(), m_route_date, bOnlyWorkedOut);

        routesAdapter.setOnItemClickListener(new RoutesAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                RoutesAdapter.RouteItemSpecification itemSpecification=routesAdapter.getRouteItem(position);

                boolean bShowMenu=false;
                Bundle parameters=new Bundle();
                parameters.putLong("position", position);
                if (itemSpecification.itemType==RoutesAdapter.TYPE_ROUTE_ITEM)
                {
                    RoutesAdapter.RouteRecord routeRecord=(RoutesAdapter.RouteRecord)itemSpecification.item;
                    if (!routeRecord.bVisitPresent)
                    {
                        // Визита в эту точку еще не было
                        parameters.putString("distr_point_id", routeRecord.distr_point_id);
                        parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                        mListener.onUniversalEventListener("startVisit", parameters);
                    } else
                        bShowMenu=true;
                }
                if (itemSpecification.itemType==RoutesAdapter.TYPE_ORDER_ITEM)
                {
                    //routeOrderRecord._id=TextDatabase.getOrderIdByUID(mContext.getContentResolver(), orderRecord.uid.toString());
                    RoutesAdapter.RouteRecord nodeRecord=routesAdapter.getRouteNode(position);
                    RoutesAdapter.RouteRecord.RouteOrderRecord routeOrderRecord=(RoutesAdapter.RouteRecord.RouteOrderRecord)itemSpecification.item;
                    parameters.putLong("id", routeOrderRecord._id);
                    parameters.putBoolean("copy_order", false);
                    parameters.putString("distr_point_id", nodeRecord.distr_point_id);
                    mListener.onUniversalEventListener("editOrder", parameters);
                }
                if (itemSpecification.itemType==RoutesAdapter.TYPE_REFUND_ITEM)
                {
                    //routeOrderRecord._id=TextDatabase.getOrderIdByUID(mContext.getContentResolver(), orderRecord.uid.toString());
                    RoutesAdapter.RouteRecord.RouteRefundRecord routeRefundRecord=(RoutesAdapter.RouteRecord.RouteRefundRecord)itemSpecification.item;
                    parameters.putLong("id", routeRefundRecord._id);
                    parameters.putBoolean("copy_refund", false);
                    mListener.onUniversalEventListener("editRefund", parameters);
                }
                if (itemSpecification.itemType==RoutesAdapter.TYPE_PAYMENT_ITEM)
                {
                    //routeOrderRecord._id=TextDatabase.getOrderIdByUID(mContext.getContentResolver(), orderRecord.uid.toString());
                    RoutesAdapter.RouteRecord.RoutePaymentRecord routePaymentRecord=(RoutesAdapter.RouteRecord.RoutePaymentRecord)itemSpecification.item;
                    parameters.putLong("id", routePaymentRecord._id);
                    parameters.putBoolean("copy_payment", false);
                    mListener.onUniversalEventListener("editPayment", parameters);
                }
                if (itemSpecification.itemType==RoutesAdapter.TYPE_DISTRIBS_ITEM)
                {
                    //routeOrderRecord._id=TextDatabase.getOrderIdByUID(mContext.getContentResolver(), orderRecord.uid.toString());
                    RoutesAdapter.RouteRecord.RouteDistribsRecord routeDistribsRecord=(RoutesAdapter.RouteRecord.RouteDistribsRecord)itemSpecification.item;
                    parameters.putLong("id", routeDistribsRecord._id);
                    parameters.putBoolean("copy_distribs", false);
                    mListener.onUniversalEventListener("editDistribs", parameters);
                }

                if (bShowMenu) {
                    final RoutesAdapter.RouteRecord routeRecord=(RoutesAdapter.RouteRecord)itemSpecification.item;
                    PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_create_order: {
                                    Bundle parameters=new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("createOrder", parameters);
                                    return true;
                                }
                                case R.id.action_create_payment: {
                                    Bundle parameters=new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("createPayment", parameters);
                                    return true;
                                }
                                case R.id.action_create_refund: {
                                    Bundle parameters=new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("createRefund", parameters);
                                    return true;
                                }
                                case R.id.action_create_distribs: {
                                    Bundle parameters=new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("createDistribs", parameters);
                                    return true;
                                }
                                case R.id.action_cancel_visit: {
                                    Bundle parameters = new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("cancelVisit", parameters);
                                    return true;
                                }
                                case R.id.action_end_visit: {
                                    Bundle parameters = new Bundle();
                                    parameters.putString("distr_point_id", routeRecord.distr_point_id);
                                    parameters.putString("distr_point_descr", routeRecord.distr_point_descr);
                                    mListener.onUniversalEventListener("endVisit", parameters);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
                    popupMenu.inflate(R.menu.route_line);
                    popupMenu.show();
                }
            }

            @Override
            public boolean onItemLongClick(int position, View v) {

                /*
                // TODO 06.06.2019
                //PopupMenu popupMenu = new PopupMenu(getActivity(), getView().findViewById(R.id.rvRouteByDate));
                PopupMenu popupMenu = new PopupMenu(getActivity(), v, Gravity.CENTER);
                popupMenu.getMenuInflater().inflate(R.menu.main_orders_list, popupMenu.getMenu());
                popupMenu.show();

                return true;
                */
                return false;

            }
        });

        View view = inflater.inflate(R.layout.routes_list_fragment, container, false);

        EditText etRoute = (EditText) view.findViewById(R.id.etRouteWithDate);
        if (!m_route_date.isEmpty())
            etRoute.setText(Common.dateStringAsText(m_route_date)+"/"+m_route_descr);
        else
            etRoute.setText(m_route_descr);

        RecyclerView listViewRouteByDate=(RecyclerView)view.findViewById(R.id.rvRouteByDate);
        listViewRouteByDate.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL,false));
        listViewRouteByDate.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        listViewRouteByDate.setAdapter(routesAdapter);

        final Button buttonSelectClient = (Button) view.findViewById(R.id.buttonFilterSelectRouteWithDate);
        buttonSelectClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RoutesWithDatesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivityForResult(intent, MainActivity.SELECT_ROUTES_WITH_DATES_REQUEST);
                selectRoutesWithDatesActivityResultLauncher.launch(intent);
            }
        });

        return view;
    }

    // Заполняет таблицы с реальными маршрутами
    public boolean fillRealRoutes(String date)
    {
        ContentResolver contentResolver=getActivity().getContentResolver();
        // Стираем старые данные
        // Найдем маршрут за эту дату
        Cursor routesDatesCursor=contentResolver.query(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, new String[]{"route_id"}, "route_date=?", new String[]{date}, "_id desc");
        if (!routesDatesCursor.moveToFirst())
        {
            routesDatesCursor.close();
            return false;
        }
        String routeId=routesDatesCursor.getString(0);
        routesDatesCursor.close();
        // Сами данные маршрута
        Cursor routeCursor=contentResolver.query(MTradeContentProvider.ROUTES_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{routeId}, "_id desc");
        if (!routeCursor.moveToFirst())
        {
            routeCursor.close();
            return false;
        }
        String routeDescr=routeCursor.getString(0);
        routeCursor.close();
        // Был ли уже заполнен реальный маршрут?
        long realRouteId=-1;
        boolean bRealRouteCanBeOverwritten=true;
        ArrayList<String> distrPoints=new ArrayList();
        Cursor realRoutesDatesCursor=contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, new String[]{"_id"}, "route_date=?", new String[]{date}, "_id desc");
        if (realRoutesDatesCursor.moveToFirst())
        {
            // Получим _id реального маршрута
            realRouteId=realRoutesDatesCursor.getLong(0);

            // Прочитаем, какие есть торговые точки в новом маршруте
            Cursor routesLines=contentResolver.query(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, new String[]{"distr_point_id"}, "route_id=?", new String[]{routeId}, "_id desc");
            while (routesLines.moveToNext())
            {
                distrPoints.add(routesLines.getString(0));
            }
            routesLines.close();

            // Прочитаем данные о торговых точках, где были визиты (по _id реального маршрута)
            Cursor realRoutesLines=contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"distr_point_id"}, "real_route_id=? and (start_visit_time<>? or end_visit_time<>?)", new String[]{Long.toString(realRouteId), "00010101", "00010101"}, "_id desc");
            while (realRoutesLines.moveToNext())
            {
                //distrPoints.add(realRoutesLines.getString(0));
                if (!distrPoints.contains(realRoutesLines.getString(0)))
                {
                    bRealRouteCanBeOverwritten=false;
                    break;
                }
            }
            realRoutesLines.close();
        }
        realRoutesDatesCursor.close();
        if (!bRealRouteCanBeOverwritten)
            return false;

        // Поменяем lineno на любое большое число
        int max_lineno=-1;
        Cursor realRoutesLines=contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id", "lineno"}, "real_route_id=?", new String[]{Long.toString(realRouteId)}, "lineno desc");
        while (realRoutesLines.moveToNext())
        {
            int index_realRoutesId=realRoutesLines.getColumnIndex("_id");
            int index_realRoutesLineno=realRoutesLines.getColumnIndex("lineno");
            if (max_lineno==-1)
            {
                max_lineno=realRoutesLines.getInt(index_realRoutesLineno)+10000;
            } else
            {
                max_lineno--;
            }
            ContentValues cv=new ContentValues();
            cv.put("lineno", max_lineno);
            contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(realRoutesLines.getLong(index_realRoutesId))});
        }
        realRoutesLines.close();
        // После этого мы будем заполнять данные и менять lineno, а все, что больше max_lineno, будет удалено

        Cursor routesLines=contentResolver.query(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, new String[]{"lineno", "distr_point_id", "visit_time"}, "route_id=?", new String[]{routeId}, "_id desc");
        int index_routesLinesLineno=routesLines.getColumnIndex("lineno");
        int index_routesLinesDistrPointId=routesLines.getColumnIndex("distr_point_id");
        int index_routesLinesDistrVisitTime=routesLines.getColumnIndex("visit_time");

        // Заполняем маршрут
        if (realRouteId!=-1)
        {
            // Маршрут существует
            ContentValues cv=new ContentValues();
            // route_date не перезаполняем, по условию отбора соответствует записи с _id=realRouteId
            cv.put("route_id", routeId);
            cv.put("route_descr", routeDescr);
            contentResolver.update(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(realRouteId)});

            // Обновим детальные данные
            while (routesLines.moveToNext())
            {
                cv.clear();
                cv.put("lineno", routesLines.getLong(index_routesLinesLineno));
                cv.put("required_visit_time", routesLines.getString(index_routesLinesDistrVisitTime));

                if (contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "real_route_id=? and distr_point_id=?", new String[]{Long.toString(realRouteId), routesLines.getString(index_routesLinesDistrPointId)})==0) {
                    cv.put("real_route_id", realRouteId);
                    cv.put("distr_point_id", routesLines.getString(index_routesLinesDistrPointId));
                    cv.put("start_visit_time", "00010101");
                    cv.put("end_visit_time", "00010101");
                    cv.put("version", 0);
                    cv.put("version_ack", 0);
                    cv.put("versionPDA", 1);
                    cv.put("versionPDA_ack", 0);
                    cv.put("datecoord", 0);
                    cv.put("latitude", 0);
                    cv.put("longitude", 0);
                    // Если ничего не обновилось, добавим
                    contentResolver.insert(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv);
                } else
                {
                    // TODO увеличить счетчик VersionPDA
                }
            }


            // Сотрем неактуальные данные
            if (max_lineno>0) {
                contentResolver.delete(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, "lineno>=?", new String[]{String.valueOf(max_lineno)});
            }
        } else
        {
            // Заполняем новый
            ContentValues cv=new ContentValues();
            cv.put("uid", UUID.randomUUID().toString());
            cv.put("id", "");
            cv.put("route_date", date);
            cv.put("route_id", routeId);
            cv.put("route_descr", routeDescr);
            realRouteId=Long.valueOf(contentResolver.insert(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, cv).getLastPathSegment());

            while (routesLines.moveToNext())
            {
                cv.clear();
                cv.put("lineno", routesLines.getLong(index_routesLinesLineno));
                cv.put("real_route_id", realRouteId);
                cv.put("distr_point_id", routesLines.getString(index_routesLinesDistrPointId));
                cv.put("required_visit_time", routesLines.getString(index_routesLinesDistrVisitTime));
                cv.put("start_visit_time", "00010101");
                cv.put("end_visit_time", "00010101");
                cv.put("version", 0);
                cv.put("version_ack", 0);
                cv.put("versionPDA", 1);
                cv.put("versionPDA_ack", 0);
                cv.put("datecoord", 0);
                cv.put("latitude", 0);
                cv.put("longitude", 0);
                cv.put("gpsstate", 0);
                cv.put("gpsaccuracy", 0);
                cv.put("accept_coord", 0);
                contentResolver.insert(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv);

            }
        }

        routesLines.close();

        // Почему-то вот этого не достаточно
        routesAdapter.routeRecordList = routesAdapter.FillRouteList(getContext(), date);
        routesAdapter.notifyDataSetChanged();
        // Поэтому с 04.12.2019 вызываем restartLoaderForListView()
        // (уровнем выше), т.к. почему-то просто notifyDataSetChanged не перерисовывает
        // похоже, причина в использовании RecyclerView
        return true;

        /*
        db.execSQL("create table routes (" +
                "_id integer primary key autoincrement, " +
                "id text," +
                "code text," +
                "descr text," +
                "manager_id text," +
                "UNIQUE ('id')"+
                ");");


        db.execSQL("create table real_routes_dates (" +
                "_id integer primary key autoincrement, " +
                "uid text,"+ // в КПК
                "id text,"+ // в 1С
                "route_date text," +
                "route_id text," +
                "route_descr text," +
                "UNIQUE ('route_date')"+
                ");");
        db.execSQL("create table real_routes_lines (" +
                "_id integer primary key autoincrement, " +
                "real_route_id integer," +
                "lineno int," +
                "distr_point_id text," +
                "start_visit_time text," +
                "end_visit_time text," +
                "version int," +
                "version_ack int," +
                "versionPDA int," +
                "versionPDA_ack int," +
                "datecoord text," +
                "latitude double," +
                "longitude double," +
                "UNIQUE ('real_route_id', 'lineno')"+
                ");");
        */

    }

    public void setOnlyNotWorkedOut(boolean bOnlyNotWorkedOut)
    {
        routesAdapter.setOnlyNotWorkedOut(bOnlyNotWorkedOut);
    }


    public boolean startVisit(String distr_point_id)
    {
        //Cursor realRoutesLinesCursor=getContext().getContentResolver().query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, "_id", "real_route_id=? and distr_point_id=?"
        ContentResolver contentResolver=getActivity().getContentResolver();
        /*
        // Найдем маршрут за эту дату
        Cursor routesDatesCursor=contentResolver.query(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, new String[]{"route_id"}, "route_date=?", new String[]{m_route_date}, "_id desc");
        if (!routesDatesCursor.moveToFirst())
        {
            routesDatesCursor.close();
            return false;
        }
        String routeId=routesDatesCursor.getString(0);
        routesDatesCursor.close();
        // Сами данные маршрута
        */
        boolean bOk=false;
        int i;
        for (i=0; i<2; i++) {
            // На первом проходе пытаемся найти уже заполненные данные
            // На втором - заполняем и пытаемся найти еще раз
            if (i == 1)
                fillRealRoutes(m_route_date);
            Cursor cursorRealRoutesDates = contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI,
                    new String[]{"_id"}, "route_date=?", new String[]{m_route_date}, "_id desc");
            if (cursorRealRoutesDates.moveToNext()) {
                Cursor cursorRealRoutesLines = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                        new String[]{"_id", "versionPDA", "required_visit_time", "start_visit_time", "end_visit_time"}, "real_route_id=? and distr_point_id=?", new String[]{cursorRealRoutesDates.getString(0), distr_point_id}, "lineno");

                int index_id = cursorRealRoutesLines.getColumnIndex("_id");
                int index_versionPDA = cursorRealRoutesLines.getColumnIndex("versionPDA");
                //int index_required_visit_time = cursorRealRoutesLines.getColumnIndex("required_visit_time");
                //int index_start_visit_time = cursorRealRoutesLines.getColumnIndex("start_visit_time");
                //int index_end_visit_time = cursorRealRoutesLines.getColumnIndex("end_visit_time");
                if (cursorRealRoutesLines.moveToNext())
                {
                    Calendar c = Calendar.getInstance();
                    Date date = c.getTime();
                    String visitTime=Common.getDateTimeAsString14(date);

                    ContentValues cv=new ContentValues();
                    cv.put("start_visit_time", visitTime);
                    cv.put("accept_coord", 1);
                    cv.put("versionPDA", cursorRealRoutesLines.getInt(index_versionPDA)+1); // Увеличим счетчик версий на 1
                    // тут еще увеличить счетчик versionPDA на 1
                    contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "_id=?", new String[]{cursorRealRoutesLines.getString(index_id)});
                    // TODO поиск текущей записи, добавление и обновление списка
                    routesAdapter.addVisitInfo(distr_point_id, visitTime);
                    bOk=true;

                }
                cursorRealRoutesLines.close();

            }
            cursorRealRoutesDates.close();
            if (bOk)
                break;
        }

        return true;
    }

    public boolean endVisit(String distr_point_id)
    {
        ContentResolver contentResolver=getActivity().getContentResolver();

        boolean bOk=false;
        Cursor cursorRealRoutesDates = contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI,
                new String[]{"_id"}, "route_date=?", new String[]{m_route_date}, "_id desc");
        if (cursorRealRoutesDates.moveToNext()) {
            Cursor cursorRealRoutesLines = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                    new String[]{"_id", "versionPDA", "required_visit_time", "start_visit_time", "end_visit_time"}, "real_route_id=? and distr_point_id=?", new String[]{cursorRealRoutesDates.getString(0), distr_point_id}, "lineno");

            int index_id = cursorRealRoutesLines.getColumnIndex("_id");
            int index_versionPDA = cursorRealRoutesLines.getColumnIndex("versionPDA");
            //int index_required_visit_time = cursorRealRoutesLines.getColumnIndex("required_visit_time");
            //int index_start_visit_time = cursorRealRoutesLines.getColumnIndex("start_visit_time");
            //int index_end_visit_time = cursorRealRoutesLines.getColumnIndex("end_visit_time");
            if (cursorRealRoutesLines.moveToNext())
            {
                Calendar c = Calendar.getInstance();
                Date date = c.getTime();
                String visitTime=Common.getDateTimeAsString14(date);

                ContentValues cv=new ContentValues();
                cv.put("end_visit_time", visitTime);
                cv.put("versionPDA", cursorRealRoutesLines.getInt(index_versionPDA)+1); // Увеличим счетчик версий на 1
                // тут еще увеличить счетчик versionPDA на 1
                contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "_id=?", new String[]{cursorRealRoutesLines.getString(index_id)});
                // TODO поиск текущей записи, добавление и обновление списка
                routesAdapter.addEndVisitInfo(distr_point_id, visitTime);
                bOk=true;
            }
            cursorRealRoutesLines.close();
        }
        cursorRealRoutesDates.close();

        return bOk;
    }

    public boolean cancelVisit(String distr_point_id)
    {
        ContentResolver contentResolver=getActivity().getContentResolver();

        boolean bOk=false;
        Cursor cursorRealRoutesDates = contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI,
                new String[]{"_id"}, "route_date=?", new String[]{m_route_date}, "_id desc");
        if (cursorRealRoutesDates.moveToNext()) {
            Cursor cursorRealRoutesLines = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                    new String[]{"_id", "versionPDA", "required_visit_time", "start_visit_time", "end_visit_time"}, "real_route_id=? and distr_point_id=?", new String[]{cursorRealRoutesDates.getString(0), distr_point_id}, "lineno");

            int index_id = cursorRealRoutesLines.getColumnIndex("_id");
            int index_versionPDA = cursorRealRoutesLines.getColumnIndex("versionPDA");
            //int index_required_visit_time = cursorRealRoutesLines.getColumnIndex("required_visit_time");
            //int index_start_visit_time = cursorRealRoutesLines.getColumnIndex("start_visit_time");
            //int index_end_visit_time = cursorRealRoutesLines.getColumnIndex("end_visit_time");
            if (cursorRealRoutesLines.moveToNext())
            {
                Calendar c = Calendar.getInstance();
                Date date = c.getTime();
                String visitTime=Common.getDateTimeAsString14(date);

                ContentValues cv=new ContentValues();
                cv.put("start_visit_time", "00010101");
                cv.put("end_visit_time", "00010101");
                cv.put("versionPDA", cursorRealRoutesLines.getInt(index_versionPDA)+1); // Увеличим счетчик версий на 1
                // тут еще увеличить счетчик versionPDA на 1
                contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "_id=?", new String[]{cursorRealRoutesLines.getString(index_id)});
                // TODO поиск текущей записи, добавление и обновление списка
                routesAdapter.clearVisitInfo(distr_point_id);
                bOk=true;
            }
            cursorRealRoutesLines.close();
        }
        cursorRealRoutesDates.close();

        return bOk;
    }


    public boolean updateDocumentInRoute(String distr_point_id, Object object)
    {
        return routesAdapter.updateDocumentInRoute(distr_point_id, object);
    }

    public boolean updateDocumentInRouteDeleted(String uid)
    {
        return routesAdapter.updateDocumentInRouteDeleted(uid);
    }

    public void onRouteSelected(String route_id, String route_date, String route_descr) {
        m_route_id = route_id;
        m_route_date = route_date;
        m_route_descr = route_descr;
        View view = getView();
        EditText etRoute = (EditText) view.findViewById(R.id.etRouteWithDate);
        if (!m_route_date.isEmpty())
            etRoute.setText(m_route_date+"/"+m_route_descr);
        else
            etRoute.setText(m_route_descr);
        // TODO
        //LoaderManager.getInstance(this).restartLoader(ROUTES_LOADER_ID, null, this);
        RecyclerView listViewRouteByDate=(RecyclerView)view.findViewById(R.id.rvRouteByDate);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean bOnlyWorkedOut=sharedPreferences.getBoolean("routes_show_only_worked_out", false);
        routesAdapter = new RoutesAdapter(getActivity(), m_route_date, bOnlyWorkedOut);
        listViewRouteByDate.setAdapter(routesAdapter);
        //list.remove(position);
        //recycler.removeViewAt(position);
        //mAdapter.notifyItemRemoved(position);
        //mAdapter.notifyItemRangeChanged(position, list.size());
        //routesAdapter.notifyDataSetChanged();
    }

    public void restartLoaderForListView()
    {
        if (m_bDataInited) {
            //LoaderManager.getInstance(this).restartLoader(ROUTES_LOADER_ID, null, this);
            View view = getView();
            RecyclerView listViewRouteByDate=(RecyclerView)view.findViewById(R.id.rvRouteByDate);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean bOnlyWorkedOut=sharedPreferences.getBoolean("routes_show_only_worked_out", false);

            routesAdapter = new RoutesAdapter(getActivity(), m_route_date, bOnlyWorkedOut);
            listViewRouteByDate.setAdapter(routesAdapter);
        }
    }

}
