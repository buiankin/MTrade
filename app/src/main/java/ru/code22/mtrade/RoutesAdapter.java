package ru.code22.mtrade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.w3c.dom.Text;

// https://stackoverflow.com/questions/26245139/how-to-create-recyclerview-with-multiple-view-type

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder> {

    protected final Context mContext;
    private RoutesAdapter.RouteViewHolder mViewHolder;

    private static RoutesAdapter.ClickListener clickListener;

    public static final int TYPE_ROUTE_ITEM = 1; // На самом деле это торговая точка
    public static final int TYPE_VISITING_ITEM = 2;
    public static final int TYPE_ORDER_ITEM = 3;
    public static final int TYPE_REFUND_ITEM = 4;
    public static final int TYPE_PAYMENT_ITEM = 5;
    public static final int TYPE_DISTRIBS_ITEM = 6;

    ArrayList<RouteRecord> routeRecordList = null;

    int defaultTextColor;
    boolean m_bOnlyNotWorkedOut;

    static class RouteRecord {
        public int index_in_list;
        public boolean hidden;

        public Long _id;
        public String distr_point_id;
        public String distr_point_descr;
        public String distr_point_clients_descr; // с 05.06.2019
        public ArrayList<String> distr_point_clients_ids;
        public boolean bVisitPresent;
        public String required_visit_time;
        public String start_visit_time;
        public String end_visit_time;

        public double saldo;
        public double saldo_past;

        ArrayList<RouteOrderRecord> routeOrders;
        ArrayList<RouteRefundRecord> routeRefunds;
        ArrayList<RoutePaymentRecord> routePayments;
        ArrayList<RouteDistribsRecord> routeDistribs;

        public RouteRecord() {
            _id = -1L;
            hidden=false;
            distr_point_id = "";
            distr_point_descr = "";
            distr_point_clients_descr = "";
            distr_point_clients_ids = new ArrayList<String>();
            bVisitPresent = false;
            required_visit_time = "";
            start_visit_time = "";
            end_visit_time = "";

            routeOrders = null;
            routeRefunds = null;
            routePayments = null;
            routeDistribs = null;
        }

        static class RouteOrderRecord {
            Long _id;
            //String order_id;
            String order_uid;
            String client_id;
            String clientDescr;
            String numdoc;
            String datedoc;
            int state;
            double sum_doc;
            double sum_shipping;
            String shipping_date;
            int color;
        }


        static class RouteRefundRecord {
            Long _id;
            //String refund_id;
            String refund_uid;
            String client_id;
            String clientDescr;
            String numdoc;
            String datedoc;
            int state;
            int color;
        }


        static class RoutePaymentRecord {
            Long _id;
            //String payment_id;
            String payment_uid;
            String client_id;
            String clientDescr;
            String numdoc;
            String datedoc;
            int state;
            double sum_doc;
            int color;
        }



        static class RouteDistribsRecord {
            Long _id;
            //String distribs_id;
            String distribs_uid;
            String client_id;
            String clientDescr;
            String numdoc;
            String datedoc;
            int state;
            int color;
        }

        // Возвращает количество строк, занимаемой записью в гридах
        public int getAllocatedLinesCount(boolean bCalcOrders, boolean bCalcRefunds, boolean bCalcPayments, boolean bCalcDistribs)
        {
            int sz=bVisitPresent?2:1; // Название торговой точки + время визита, если оно есть (1+1 или 1+0)
            if (bCalcOrders&&routeOrders!=null)
                sz+=routeOrders.size();
            if (bCalcRefunds&&routeRefunds!=null)
                sz+=routeRefunds.size();
            if (bCalcPayments&&routePayments!=null)
                sz+=routePayments.size();
            if (bCalcDistribs&&routeDistribs!=null)
                sz+=routeDistribs.size();

            return sz;
        }

        public int getAllocatedLinesCount()
        {
            return getAllocatedLinesCount(true, true, true, true);
        }

    }

    void setOnlyNotWorkedOut(boolean bOnlyNotWorkedOut)
    {
        if (m_bOnlyNotWorkedOut!=bOnlyNotWorkedOut)
        {
            m_bOnlyNotWorkedOut=bOnlyNotWorkedOut;
            fillIndices(routeRecordList);
            notifyDataSetChanged();
        }
    }

    void fillIndices(ArrayList<RouteRecord> routeRecordList) {
        //
        // Заполняем индексы
        int idx = 0;
        for (int i = 0; i < routeRecordList.size(); i++) {
            RouteRecord routeRecord = routeRecordList.get(i);
            if (m_bOnlyNotWorkedOut) {
                routeRecord.hidden = (routeRecord.bVisitPresent && !Common.isDateStringEmpty(routeRecord.end_visit_time));
            } else {
                routeRecord.hidden=false;
            }
            if (routeRecord.hidden)
            {
                routeRecord.index_in_list = -1;
                continue;
            }
            routeRecord.index_in_list = idx;
            idx+=routeRecord.getAllocatedLinesCount();
            /*
            if (routeRecord.bVisitPresent)
                idx++;
            if (routeRecord.routeOrders != null)
                idx += routeRecord.routeOrders.size();
            if (routeRecord.routeRefunds != null)
                idx += routeRecord.routeRefunds.size();
            if (routeRecord.routePayments != null)
                idx += routeRecord.routePayments.size();
            if (routeRecord.routeDistribs != null)
                idx += routeRecord.routeDistribs.size();
            idx++;
             */
        }
    }

    public ArrayList<RouteRecord> FillRouteList(Context context, String date) {
        ArrayList<RouteRecord> result = new ArrayList();

        boolean bHaveRealData = false;
        // Заполнены ли уже реальные данные
        Cursor cursorRealRoutesDates = context.getContentResolver().query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI,
                new String[]{"_id"}, "route_date=?", new String[]{date}, "_id desc");
        if (cursorRealRoutesDates.moveToNext()) {
            Cursor cursorRealRoutesLines = context.getContentResolver().query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                    new String[]{"_id", "distr_point_id", "required_visit_time", "start_visit_time", "end_visit_time"}, "real_route_id=?", new String[]{cursorRealRoutesDates.getString(0)}, "lineno");

            int index_id = cursorRealRoutesLines.getColumnIndex("_id");
            int index_DistrPointId = cursorRealRoutesLines.getColumnIndex("distr_point_id");
            int index_required_visit_time = cursorRealRoutesLines.getColumnIndex("required_visit_time");
            int index_start_visit_time = cursorRealRoutesLines.getColumnIndex("start_visit_time");
            int index_end_visit_time = cursorRealRoutesLines.getColumnIndex("end_visit_time");
            while (cursorRealRoutesLines.moveToNext()) {
                bHaveRealData = true;
                RouteRecord rec = new RouteRecord();
                rec._id = cursorRealRoutesLines.getLong(index_id);
                rec.distr_point_id = cursorRealRoutesLines.getString(index_DistrPointId);
                rec.bVisitPresent = false;
                rec.required_visit_time = cursorRealRoutesLines.getString(index_required_visit_time);
                rec.start_visit_time = cursorRealRoutesLines.getString(index_start_visit_time);
                rec.end_visit_time = cursorRealRoutesLines.getString(index_end_visit_time);
                if (!rec.start_visit_time.isEmpty() && !rec.start_visit_time.equals("00010101"))
                    rec.bVisitPresent = true;
                // TODO
                //rec.bVisitPresent=true;
                result.add(rec);
            }
            cursorRealRoutesLines.close();
        }
        cursorRealRoutesDates.close();

        // Если данные еще не заполнялись
        if (!bHaveRealData) {
            // По дате определим маршрут
            Cursor cursor = context.getContentResolver().query(MTradeContentProvider.ROUTES_DATES_CONTENT_URI,
                    new String[]{"route_id"}, "route_date=?", new String[]{date}, "_id desc");

            if (cursor.moveToFirst()) {
                // По маршруту найдем все торговые точки
                Cursor cursorTradePoints = context.getContentResolver().query(MTradeContentProvider.ROUTES_LINES_CONTENT_URI,
                        new String[]{"_id", "distr_point_id", "visit_time"}, "route_id=?", new String[]{cursor.getString(0)}, "lineno");
                int index_id = cursorTradePoints.getColumnIndex("_id");
                int index_DistrPointId = cursorTradePoints.getColumnIndex("distr_point_id");
                int index_visit_time = cursorTradePoints.getColumnIndex("visit_time");
                while (cursorTradePoints.moveToNext()) {
                    //RouteRecord.RouteDescrRecord recDescr=new RouteRecord.RouteDescrRecord();
                    //recDescr._id=cursorTradePoints.getLong(index_id);
                    //recDescr.distr_point_id=cursorTradePoints.getString(index_DistrPointId);
                    //recDescr.visit_time=cursorTradePoints.getString(index_visit_time);
                    //
                    RouteRecord rec = new RouteRecord();
                    rec._id = cursorTradePoints.getLong(index_id);
                    rec.distr_point_id = cursorTradePoints.getString(index_DistrPointId);
                    rec.bVisitPresent = false;
                    rec.required_visit_time = cursorTradePoints.getString(index_visit_time);
                    // TODO
                    //rec.bVisitPresent=true;
                    //rec.start_visit_time="00010101";
                    //rec.end_visit_time="00010101";
                    //
                    result.add(rec);
                }
                cursorTradePoints.close();
            }
            cursor.close();
        }

        // Надо заполнить наименования торговых точек
        // Создаем массив тороговых точек
        String[] selectionArgsDistrPoints = new String[result.size()];
        String[] selectionArgsDocs = new String[result.size() + 2];
        StringBuilder sbDistrPoints = new StringBuilder();
        StringBuilder sbDocs = new StringBuilder();
        sbDocs.append("datedoc between ? and ?");
        selectionArgsDocs[0] = date;
        selectionArgsDocs[1] = date + "Z";
        for (int i = 0; i < result.size(); i++) {
            selectionArgsDistrPoints[i] = result.get(i).distr_point_id;
            selectionArgsDocs[i + 2] = result.get(i).distr_point_id;
            if (i == 0) {
                sbDistrPoints.append("id in (?");
                sbDocs.append(" and distr_point_id in (?");
            } else {
                sbDistrPoints.append(",?");
                sbDocs.append(",?");
            }
        }
        if (selectionArgsDistrPoints.length != 0)
            sbDistrPoints.append(")");
        if (selectionArgsDocs.length != 2)
            sbDocs.append(")");

        //ArrayList<String> clientsInDistrPointsIdList = new ArrayList(); // К сожалению для оптимизации ниже будет еще один список контрагентов, но уже из документов

        //HashMap<String, String> clientsDescrs=TextDatabase.getClientsDescr(context.getContentResolver(), clientIdList);

        // Выберем наименования торговых точек
        Cursor distrPointsCursor = context.getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
                new String[]{"id", "descr", "owner_id"}, sbDistrPoints.toString(), selectionArgsDistrPoints, "_id");
        int index_distrPointsId = distrPointsCursor.getColumnIndex("id");
        int index_distrPointsDescr = distrPointsCursor.getColumnIndex("descr");
        int index_distrClientId = distrPointsCursor.getColumnIndex("owner_id");
        while (distrPointsCursor.moveToNext()) {
            String distr_point_id = distrPointsCursor.getString(index_distrPointsId);
            String distr_point_descr = distrPointsCursor.getString(index_distrPointsDescr);
            String client_id = distrPointsCursor.getString(index_distrClientId);
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).distr_point_id.equals(distr_point_id)) {
                    result.get(i).distr_point_descr = distr_point_descr; // может несколько раз установиться (но одно и то же название торговой точки)
                    result.get(i).distr_point_clients_ids.add(client_id); // один или больше клиентов, повторяться у одной торговой точки не будут, поэтому не проверяем
                    break;
                }
            }
        }
        distrPointsCursor.close();

        ArrayList<String> clientIdList = new ArrayList(); // Для того, чтобы запросить наименования клиентов
        ArrayList<String> documentsUidList = new ArrayList(); // Для данных из журнала (например, цвет)

        // Добавим контрагентов из маршрутов
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.get(i).distr_point_clients_ids.size(); j++) {
                if (!clientIdList.contains(result.get(i).distr_point_clients_ids.get(j)))
                    clientIdList.add(result.get(i).distr_point_clients_ids.get(j));
            }
        }

        // Зная торговые точки, находим документы
        // Заказы
        Cursor ordersJournal = context.getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI,
                new String[]{"_id", "id", "uid", "distr_point_id", "client_id", "numdoc", "datedoc", "state", "sum_doc", "sum_shipping", "shipping_date", "color"}, sbDocs.toString(), selectionArgsDocs, "datedoc");
        int index_orders_id = ordersJournal.getColumnIndex("_id");
        //int index_orders_order_id=ordersJournal.getColumnIndex("id");
        int index_orders_order_uid = ordersJournal.getColumnIndex("uid");
        int index_orders_distr_point_id = ordersJournal.getColumnIndex("distr_point_id");
        int index_orders_numdoc = ordersJournal.getColumnIndex("numdoc");
        int index_orders_datedoc = ordersJournal.getColumnIndex("datedoc");
        int index_orders_client_id = ordersJournal.getColumnIndex("client_id");
        int index_orders_state = ordersJournal.getColumnIndex("state");
        int index_orders_sum_doc = ordersJournal.getColumnIndex("sum_doc");
        int index_orders_sum_shipping = ordersJournal.getColumnIndex("sum_shipping");
        int index_orders_shipping_date = ordersJournal.getColumnIndex("shipping_date");
        int index_orders_color = ordersJournal.getColumnIndex("color");

        while (ordersJournal.moveToNext()) {
            String distr_point_id = ordersJournal.getString(index_orders_distr_point_id);
            for (int i = 0; i < result.size(); i++) {
                RouteRecord routeRecord = result.get(i);
                if (routeRecord.distr_point_id.equals(distr_point_id)) {
                    if (routeRecord.routeOrders == null)
                        routeRecord.routeOrders = new ArrayList();

                    RouteRecord.RouteOrderRecord routeOrderRecord = new RouteRecord.RouteOrderRecord();
                    routeOrderRecord._id = ordersJournal.getLong(index_orders_id);
                    //routeOrderRecord.order_id=ordersJournal.getString(index_orders_order_id);
                    routeOrderRecord.order_uid = ordersJournal.getString(index_orders_order_uid);
                    routeOrderRecord.numdoc = ordersJournal.getString(index_orders_numdoc);
                    routeOrderRecord.client_id = ordersJournal.getString(index_orders_client_id);
                    routeOrderRecord.clientDescr = ""; // заполнится ниже, для всех клиентов сразу
                    routeOrderRecord.datedoc = ordersJournal.getString(index_orders_datedoc);
                    routeOrderRecord.state = ordersJournal.getInt(index_orders_state);
                    routeOrderRecord.sum_doc = ordersJournal.getDouble(index_orders_sum_doc);
                    routeOrderRecord.sum_shipping = ordersJournal.getDouble(index_orders_sum_shipping);
                    routeOrderRecord.shipping_date = ordersJournal.getString(index_orders_shipping_date);
                    routeOrderRecord.color = ordersJournal.getInt(index_orders_color);

                    routeRecord.routeOrders.add(routeOrderRecord);

                    if (!clientIdList.contains(routeOrderRecord.client_id))
                        clientIdList.add(routeOrderRecord.client_id);

                    if (!documentsUidList.contains(routeOrderRecord.order_uid))
                        documentsUidList.add(routeOrderRecord.order_uid);

                    break;
                }
            }

            //ArrayList<RouteRecord> routeRecordList = new ArrayList<RouteRecord>();
        }
        ordersJournal.close();

        // Возвраты
        Cursor refundsJournal = context.getContentResolver().query(MTradeContentProvider.REFUNDS_CONTENT_URI,
                new String[]{"_id", "id", "uid", "distr_point_id", "client_id", "numdoc", "datedoc", "state", "color"}, sbDocs.toString(), selectionArgsDocs, "datedoc");
        int index_refunds_id = refundsJournal.getColumnIndex("_id");
        int index_refunds_order_id = refundsJournal.getColumnIndex("id");
        int index_refunds_order_uid = refundsJournal.getColumnIndex("uid");
        int index_refunds_distr_point_id = refundsJournal.getColumnIndex("distr_point_id");
        int index_refunds_numdoc = refundsJournal.getColumnIndex("numdoc");
        int index_refunds_datedoc = refundsJournal.getColumnIndex("datedoc");
        int index_refunds_client_id = refundsJournal.getColumnIndex("client_id");
        int index_refunds_state = refundsJournal.getColumnIndex("state");
        int index_refunds_color = refundsJournal.getColumnIndex("color");

        while (refundsJournal.moveToNext()) {
            String distr_point_id = refundsJournal.getString(index_refunds_distr_point_id);
            for (int i = 0; i < result.size(); i++) {
                RouteRecord routeRecord = result.get(i);
                if (routeRecord.distr_point_id.equals(distr_point_id)) {
                    if (routeRecord.routeRefunds == null)
                        routeRecord.routeRefunds = new ArrayList();

                    RouteRecord.RouteRefundRecord routeRefundRecord = new RouteRecord.RouteRefundRecord();
                    routeRefundRecord._id = refundsJournal.getLong(index_refunds_id);
                    //routeRefundRecord.refund_id=refundsJournal.getString(index_refunds_order_id);
                    routeRefundRecord.refund_uid = refundsJournal.getString(index_refunds_order_uid);
                    routeRefundRecord.numdoc = refundsJournal.getString(index_refunds_numdoc);
                    routeRefundRecord.client_id = refundsJournal.getString(index_refunds_client_id);
                    routeRefundRecord.clientDescr = ""; // заполнится ниже, для всех клиентов сразу
                    routeRefundRecord.datedoc = refundsJournal.getString(index_refunds_datedoc);
                    routeRefundRecord.state = refundsJournal.getInt(index_refunds_state);
                    routeRefundRecord.color = refundsJournal.getInt(index_refunds_color);

                    routeRecord.routeRefunds.add(routeRefundRecord);

                    if (!clientIdList.contains(routeRefundRecord.client_id))
                        clientIdList.add(routeRefundRecord.client_id);

                    if (!documentsUidList.contains(routeRefundRecord.refund_uid))
                        documentsUidList.add(routeRefundRecord.refund_uid);

                    break;
                }
            }

            //ArrayList<RouteRecord> routeRecordList = new ArrayList<RouteRecord>();
        }
        refundsJournal.close();

        // Платежи
        Cursor paymentsJournal = context.getContentResolver().query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                new String[]{"_id", "id", "uid", "distr_point_id", "client_id", "numdoc", "datedoc", "state", "sum_doc", "color"}, sbDocs.toString(), selectionArgsDocs, "datedoc");
        int index_payments_id = paymentsJournal.getColumnIndex("_id");
        //int index_payments_payment_id=paymentsJournal.getColumnIndex("id");
        int index_payments_payment_uid = paymentsJournal.getColumnIndex("uid");
        int index_payments_distr_point_id = paymentsJournal.getColumnIndex("distr_point_id");
        int index_payments_numdoc = paymentsJournal.getColumnIndex("numdoc");
        int index_payments_datedoc = paymentsJournal.getColumnIndex("datedoc");
        int index_payments_client_id = paymentsJournal.getColumnIndex("client_id");
        int index_payments_state = paymentsJournal.getColumnIndex("state");
        int index_payments_sum_doc = paymentsJournal.getColumnIndex("sum_doc");
        int index_payments_color = paymentsJournal.getColumnIndex("color");

        while (paymentsJournal.moveToNext()) {
            String distr_point_id = paymentsJournal.getString(index_payments_distr_point_id);
            for (int i = 0; i < result.size(); i++) {
                RouteRecord routeRecord = result.get(i);
                if (routeRecord.distr_point_id.equals(distr_point_id)) {
                    if (routeRecord.routePayments == null)
                        routeRecord.routePayments = new ArrayList();

                    RouteRecord.RoutePaymentRecord routePaymentRecord = new RouteRecord.RoutePaymentRecord();
                    routePaymentRecord._id = paymentsJournal.getLong(index_payments_id);
                    //routePaymentRecord.payment_id=paymentsJournal.getString(index_payments_payment_id);
                    routePaymentRecord.payment_uid = paymentsJournal.getString(index_payments_payment_uid);
                    routePaymentRecord.numdoc = paymentsJournal.getString(index_payments_numdoc);
                    routePaymentRecord.client_id = paymentsJournal.getString(index_payments_client_id);
                    routePaymentRecord.clientDescr = ""; // заполнится ниже, для всех клиентов сразу
                    routePaymentRecord.datedoc = paymentsJournal.getString(index_payments_datedoc);
                    routePaymentRecord.state = paymentsJournal.getInt(index_payments_state);
                    routePaymentRecord.sum_doc = paymentsJournal.getDouble(index_payments_sum_doc);
                    // это расчетное поле, оно есть только в projectionMap
                    routePaymentRecord.color = paymentsJournal.getInt(index_payments_color);

                    routeRecord.routePayments.add(routePaymentRecord);

                    if (!clientIdList.contains(routePaymentRecord.client_id))
                        clientIdList.add(routePaymentRecord.client_id);

                    if (!documentsUidList.contains(routePaymentRecord.payment_uid))
                        documentsUidList.add(routePaymentRecord.payment_uid);

                    break;
                }
            }

            //ArrayList<RouteRecord> routeRecordList = new ArrayList<RouteRecord>();
        }
        paymentsJournal.close();


        // Заказы
        Cursor distribsJournal = context.getContentResolver().query(MTradeContentProvider.DISTRIBS_CONTENT_URI,
                new String[]{"_id", "id", "uid", "distr_point_id", "client_id", "numdoc", "datedoc", "state", "color"}, sbDocs.toString(), selectionArgsDocs, "datedoc");
        int index_distribs_id = distribsJournal.getColumnIndex("_id");
        //int index_distribs_order_id=distribsJournal.getColumnIndex("id");
        int index_distribs_order_uid = distribsJournal.getColumnIndex("uid");
        int index_distribs_distr_point_id = distribsJournal.getColumnIndex("distr_point_id");
        int index_distribs_numdoc = distribsJournal.getColumnIndex("numdoc");
        int index_distribs_datedoc = distribsJournal.getColumnIndex("datedoc");
        int index_distribs_client_id = distribsJournal.getColumnIndex("client_id");
        int index_distribs_state = distribsJournal.getColumnIndex("state");
        int index_distribs_color = distribsJournal.getColumnIndex("color");

        while (distribsJournal.moveToNext()) {
            String distr_point_id = distribsJournal.getString(index_distribs_distr_point_id);
            for (int i = 0; i < result.size(); i++) {
                RouteRecord routeRecord = result.get(i);
                if (routeRecord.distr_point_id.equals(distr_point_id)) {
                    if (routeRecord.routeDistribs == null)
                        routeRecord.routeDistribs = new ArrayList();

                    RouteRecord.RouteDistribsRecord routeDistribsRecord = new RouteRecord.RouteDistribsRecord();
                    routeDistribsRecord._id = distribsJournal.getLong(index_distribs_id);
                    //routeDistribsRecord.distribs_id=distribsJournal.getString(index_distribs_order_id);
                    routeDistribsRecord.distribs_uid=distribsJournal.getString(index_distribs_order_uid);
                    routeDistribsRecord.numdoc = distribsJournal.getString(index_distribs_numdoc);
                    routeDistribsRecord.client_id = distribsJournal.getString(index_distribs_client_id);
                    routeDistribsRecord.clientDescr = ""; // заполнится ниже, для всех клиентов сразу
                    routeDistribsRecord.datedoc = distribsJournal.getString(index_distribs_datedoc);
                    routeDistribsRecord.state = distribsJournal.getInt(index_distribs_state);
                    routeDistribsRecord.color = distribsJournal.getInt(index_distribs_color);

                    routeRecord.routeDistribs.add(routeDistribsRecord);

                    if (!clientIdList.contains(routeDistribsRecord.client_id))
                        clientIdList.add(routeDistribsRecord.client_id);

                    if (!documentsUidList.contains(routeDistribsRecord.distribs_uid))
                        documentsUidList.add(routeDistribsRecord.distribs_uid);

                    break;
                }
            }

            //ArrayList<RouteRecord> routeRecordList = new ArrayList<RouteRecord>();
        }
        distribsJournal.close();

        // Заполним названия контрагентов в документах
        // 05.06.2019 и названия контрагентов в торговых точках
        //HashMap<String, String> clientsDescrs = TextDatabase.getClientsDescr(context.getContentResolver(), clientIdList);
        HashMap<String, TextDatabase.ClientDescrWithSaldo> clientsDescrsWithSaldo = TextDatabase.ClientDescrWithSaldo.getClientsDescrWithSaldo(context.getContentResolver(), clientIdList);
        // И color в документах (потому, что цвет только в журнале)
        //HashMap<String, Integer> docsColors=TextDatabase.getDocsColors(context.getContentResolver(), documentsUidList);

        // ArrayList<RouteRecord> result = new ArrayList();
        for (RouteRecord routeRecord : result) {
            // Наименования торговых точек и долг
            double saldo=0.0;
            double saldo_past=0.0;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < routeRecord.distr_point_clients_ids.size(); j++) {
                if (clientsDescrsWithSaldo.containsKey(routeRecord.distr_point_clients_ids.get(j))) {
                    if (sb.length() != 0)
                        sb.append(", ");
                    TextDatabase.ClientDescrWithSaldo rec=clientsDescrsWithSaldo.get(routeRecord.distr_point_clients_ids.get(j));
                    sb.append(rec.descr);
                    saldo+=rec.saldo;
                    saldo_past+=rec.saldo_past;
                }
            }
            routeRecord.distr_point_clients_descr = sb.toString();
            routeRecord.saldo=saldo;
            routeRecord.saldo_past=saldo;

            if (routeRecord.routeOrders != null) {
                for (RouteRecord.RouteOrderRecord routeOrderRecord : routeRecord.routeOrders) {
                    if (clientsDescrsWithSaldo.containsKey(routeOrderRecord.client_id)) {
                        routeOrderRecord.clientDescr = clientsDescrsWithSaldo.get(routeOrderRecord.client_id).descr;
                    } else {
                        routeOrderRecord.clientDescr = "{" + routeOrderRecord.client_id + "}";
                    }
                    /*
                    if (docsColors.containsKey(routeOrderRecord.order_uid))
                    {
                        routeOrderRecord.color=docsColors.get(routeOrderRecord.order_uid);
                    }
                    */
                }
            }
            if (routeRecord.routeRefunds != null) {
                for (RouteRecord.RouteRefundRecord routeRefundRecord : routeRecord.routeRefunds) {
                    if (clientsDescrsWithSaldo.containsKey(routeRefundRecord.client_id)) {
                        routeRefundRecord.clientDescr = clientsDescrsWithSaldo.get(routeRefundRecord.client_id).descr;
                    } else {
                        routeRefundRecord.clientDescr = "{" + routeRefundRecord.client_id + "}";
                    }
                    /*
                    if (docsColors.containsKey(routeRefundRecord.refund_uid))
                    {
                        routeRefundRecord.color=docsColors.get(routeRefundRecord.refund_uid);
                    }
                    */

                }
            }
            if (routeRecord.routePayments != null) {
                for (RouteRecord.RoutePaymentRecord routePaymentRecord : routeRecord.routePayments) {
                    if (clientsDescrsWithSaldo.containsKey(routePaymentRecord.client_id)) {
                        routePaymentRecord.clientDescr = clientsDescrsWithSaldo.get(routePaymentRecord.client_id).descr;
                    } else {
                        routePaymentRecord.clientDescr = "{" + routePaymentRecord.client_id + "}";
                    }
                    /*
                    if (docsColors.containsKey(routePaymentRecord.payment_uid))
                    {
                        routePaymentRecord.color=docsColors.get(routePaymentRecord.payment_uid);
                    }
                    */

                }
            }
            if (routeRecord.routeDistribs != null) {
                for (RouteRecord.RouteDistribsRecord routeDistribsRecord : routeRecord.routeDistribs) {
                    if (clientsDescrsWithSaldo.containsKey(routeDistribsRecord.client_id)) {
                        routeDistribsRecord.clientDescr = clientsDescrsWithSaldo.get(routeDistribsRecord.client_id).descr;
                    } else {
                        routeDistribsRecord.clientDescr = "{" + routeDistribsRecord.client_id + "}";
                    }
                    /*
                    if (docsColors.containsKey(routeDistribsRecord.distribs_uid))
                    {
                        routeDistribsRecord.color=docsColors.get(routeDistribsRecord.distribs_uid);
                    }
                    */

                }
            }
        }

        fillIndices(result);

        return result;
    }

    public boolean addVisitInfo(String distr_point_id, String visitTime) {
        int i;
        boolean bOk = false;
        for (i = 0; i < routeRecordList.size(); i++) {
            RouteRecord routeRecord = routeRecordList.get(i);
            if (routeRecord.distr_point_id.equals(distr_point_id)) {
                routeRecord.start_visit_time = visitTime;
                routeRecord.end_visit_time = "00010101";
                if (!routeRecord.bVisitPresent) {
                    routeRecord.bVisitPresent = true;
                    fillIndices(routeRecordList);
                    // Проверки добавлены на всякий случай, если там есть hidden значения
                    if (routeRecord.index_in_list!=-1) {
                        notifyItemInserted(routeRecord.index_in_list + 1);
                        // 04.07.2019 изменится цвет записи торговой точки
                        notifyItemChanged(routeRecord.index_in_list);
                    }
                    //
                } else {
                    if (routeRecord.index_in_list!=-1) {
                        notifyItemChanged(routeRecord.index_in_list + 1);
                    }
                }
                bOk = true;
            }
        }
        return bOk;
    }

    public boolean addEndVisitInfo(String distr_point_id, String endVisitTime) {
        int i;
        boolean bOk = false;
        for (i = 0; i < routeRecordList.size(); i++) {
            RouteRecord routeRecord = routeRecordList.get(i);
            if (routeRecord.distr_point_id.equals(distr_point_id)) {
                routeRecord.end_visit_time = endVisitTime;
                // Проверка routeRecord.bVisitPresent на случай убедиться, что соответствующая запись есть
                // она, конечно, есть
                if (routeRecord.index_in_list != -1) {
                    if (m_bOnlyNotWorkedOut) {
                        // Запись надо спрятать (со всеми подчиненными элементами)
                        if (!routeRecord.hidden)
                        {
                            routeRecord.hidden=true;
                            int removeElementsCount=routeRecord.getAllocatedLinesCount();
                            while (removeElementsCount-->0)
                            {
                                notifyItemRemoved(routeRecord.index_in_list);
                            }
                            fillIndices(routeRecordList);
                        }
                    } else {
                        // Запись надо добавить
                        if (routeRecord.bVisitPresent) {
                            // Это тоже на всякий случай
                            notifyItemChanged(routeRecord.index_in_list + 1);
                            // 04.07.2019 изменится цвет записи торговой точки
                            notifyItemChanged(routeRecord.index_in_list);
                            //
                        }
                    }
                }
                bOk = true;
                break;
            }
        }
        return bOk;
    }

    public boolean clearVisitInfo(String distr_point_id) {
        int i;
        boolean bOk = false;
        for (i = 0; i < routeRecordList.size(); i++) {
            RouteRecord routeRecord = routeRecordList.get(i);
            if (routeRecord.distr_point_id.equals(distr_point_id)) {
                routeRecord.start_visit_time = "00010101";
                routeRecord.end_visit_time = "00010101";
                if (routeRecord.bVisitPresent) {
                    routeRecord.bVisitPresent = false;
                    if (routeRecord.index_in_list!=-1) {
                        notifyItemRemoved(routeRecord.index_in_list + 1);
                        // 04.07.2019 изменится цвет записи торговой точки
                        notifyItemChanged(routeRecord.index_in_list);
                    }
                    fillIndices(routeRecordList);
                    bOk = true;
                    break;
                }
            }
        }
        return bOk;
    }


    public boolean updateDocumentInRoute(String distr_point_id, Object object) {
        int i;
        boolean bOk = false;
        for (i = 0; i < routeRecordList.size(); i++) {
            RouteRecord routeRecord = routeRecordList.get(i);
            if (routeRecord.distr_point_id.equals(distr_point_id)) {
                // OrderRecord
                if (object instanceof MyDatabase.OrderRecord) {
                    MyDatabase.OrderRecord orderRecord = (MyDatabase.OrderRecord) object;
                    RouteRecord.RouteOrderRecord routeOrderRecord = new RouteRecord.RouteOrderRecord();
                    routeOrderRecord._id = TextDatabase.getOrderIdByUID(mContext.getContentResolver(), orderRecord.uid.toString());
                    //routeOrderRecord.order_id=orderRecord.id.toString();
                    routeOrderRecord.order_uid = orderRecord.uid.toString();
                    routeOrderRecord.client_id = orderRecord.client_id.toString();
                    routeOrderRecord.clientDescr = orderRecord.stuff_client_name;
                    routeOrderRecord.numdoc = orderRecord.numdoc;
                    routeOrderRecord.datedoc = orderRecord.datedoc;
                    routeOrderRecord.sum_doc = orderRecord.sumDoc;
                    routeOrderRecord.sum_shipping = orderRecord.sumShipping;
                    routeOrderRecord.shipping_date = orderRecord.shipping_date;
                    routeOrderRecord.color = TextDatabase.getDocColor(mContext.getContentResolver(), orderRecord.uid.toString());

                    //notifyItemChanged(routeRecord.index_in_list + 1);
                    if (routeRecord.routeOrders == null)
                        routeRecord.routeOrders = new ArrayList();
                    int j;
                    boolean bFound = false;
                    for (j = 0; j < routeRecord.routeOrders.size(); j++) {
                        if (routeRecord.routeOrders.get(j)._id.equals(routeOrderRecord._id)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        // Добавляем только 1 раз
                        routeRecord.routeOrders.add(routeOrderRecord);
                        //
                        fillIndices(routeRecordList);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemInserted(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + routeRecord.routeOrders.size());
                        }
                        bOk = true;
                    } else {
                        routeRecord.routeOrders.set(j, routeOrderRecord);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemChanged(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + j + 1);
                        }
                        bOk = true;
                    }
                }

                // RefundRecord
                if (object instanceof MyDatabase.RefundRecord) {
                    MyDatabase.RefundRecord refundRecord = (MyDatabase.RefundRecord) object;
                    RouteRecord.RouteRefundRecord routeRefundRecord = new RouteRecord.RouteRefundRecord();
                    routeRefundRecord._id = TextDatabase.getDocumentIdByUID(mContext.getContentResolver(), MTradeContentProvider.REFUNDS_CONTENT_URI, refundRecord.uid.toString());
                    //routeRefundRecord.refund_id=refundRecord.id.toString();
                    routeRefundRecord.refund_uid = refundRecord.uid.toString();
                    routeRefundRecord.client_id = refundRecord.client_id.toString();
                    routeRefundRecord.clientDescr = refundRecord.stuff_client_name;
                    routeRefundRecord.numdoc = refundRecord.numdoc;
                    routeRefundRecord.datedoc = refundRecord.datedoc;
                    routeRefundRecord.color = TextDatabase.getDocColor(mContext.getContentResolver(), refundRecord.uid.toString());

                    //notifyItemChanged(routeRecord.index_in_list + 1);
                    if (routeRecord.routeRefunds == null)
                        routeRecord.routeRefunds = new ArrayList();
                    int j;
                    boolean bFound = false;
                    for (j = 0; j < routeRecord.routeRefunds.size(); j++) {
                        if (routeRecord.routeRefunds.get(j)._id.equals(routeRefundRecord._id)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        // Добавляем только 1 раз
                        routeRecord.routeRefunds.add(routeRefundRecord);
                        //
                        fillIndices(routeRecordList);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemInserted(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + routeRecord.routeRefunds.size());
                        }
                        bOk = true;
                    } else {
                        routeRecord.routeRefunds.set(j, routeRefundRecord);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemChanged(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + j + 1);
                        }
                        bOk = true;
                    }
                }

                // PaymentRecord
                if (object instanceof MyDatabase.CashPaymentRecord) {
                    MyDatabase.CashPaymentRecord paymentRecord = (MyDatabase.CashPaymentRecord) object;
                    RouteRecord.RoutePaymentRecord routePaymentRecord = new RouteRecord.RoutePaymentRecord();
                    routePaymentRecord._id = TextDatabase.getDocumentIdByUID(mContext.getContentResolver(), MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, paymentRecord.uid.toString());
                    //routePaymentRecord.payment_id=paymentRecord.id.toString();
                    routePaymentRecord.payment_uid = paymentRecord.uid.toString();
                    routePaymentRecord.client_id = paymentRecord.client_id.toString();
                    routePaymentRecord.clientDescr = paymentRecord.stuff_client_name;
                    routePaymentRecord.numdoc = paymentRecord.numdoc;
                    routePaymentRecord.datedoc = paymentRecord.datedoc;
                    routePaymentRecord.sum_doc = paymentRecord.sumDoc;
                    routePaymentRecord.color = TextDatabase.getDocPaymentColor(mContext.getContentResolver(), paymentRecord.uid.toString());

                    //notifyItemChanged(routeRecord.index_in_list + 1);
                    if (routeRecord.routePayments == null)
                        routeRecord.routePayments = new ArrayList();
                    int j;
                    boolean bFound = false;
                    for (j = 0; j < routeRecord.routePayments.size(); j++) {
                        if (routeRecord.routePayments.get(j)._id.equals(routePaymentRecord._id)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        // Добавляем только 1 раз
                        routeRecord.routePayments.add(routePaymentRecord);
                        //
                        fillIndices(routeRecordList);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemInserted(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + routeRecord.routePayments.size());
                        }
                        bOk = true;
                    } else {
                        routeRecord.routePayments.set(j, routePaymentRecord);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemChanged(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + j + 1);
                        }
                        bOk = true;
                    }
                }

                // DistribsRecord
                if (object instanceof MyDatabase.DistribsRecord) {
                    MyDatabase.DistribsRecord distribsRecord = (MyDatabase.DistribsRecord) object;
                    RouteRecord.RouteDistribsRecord routeDistribsRecord = new RouteRecord.RouteDistribsRecord();
                    routeDistribsRecord._id = TextDatabase.getDocumentIdByUID(mContext.getContentResolver(), MTradeContentProvider.DISTRIBS_CONTENT_URI, distribsRecord.uid.toString());
                    //routeDistribsRecord.distribs_id=distribsRecord.id.toString();
                    routeDistribsRecord.distribs_uid = distribsRecord.uid.toString();
                    routeDistribsRecord.client_id = distribsRecord.client_id.toString();
                    routeDistribsRecord.clientDescr = distribsRecord.stuff_client_name;
                    routeDistribsRecord.numdoc = distribsRecord.numdoc;
                    routeDistribsRecord.datedoc = distribsRecord.datedoc;
                    routeDistribsRecord.color = TextDatabase.getDocColor(mContext.getContentResolver(), distribsRecord.uid.toString());

                    //notifyItemChanged(routeRecord.index_in_list + 1);
                    if (routeRecord.routeDistribs == null)
                        routeRecord.routeDistribs = new ArrayList();
                    int j;
                    boolean bFound = false;
                    for (j = 0; j < routeRecord.routeDistribs.size(); j++) {
                        if (routeRecord.routeDistribs.get(j)._id.equals(routeDistribsRecord._id)) {
                            bFound = true;
                            break;
                        }
                    }
                    if (!bFound) {
                        // Добавляем только 1 раз
                        routeRecord.routeDistribs.add(routeDistribsRecord);
                        //
                        fillIndices(routeRecordList);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemInserted(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + (routeRecord.routePayments == null ? 0 : routeRecord.routePayments.size()) + routeRecord.routeDistribs.size());
                        }
                        bOk = true;
                    } else {
                        routeRecord.routeDistribs.set(j, routeDistribsRecord);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemChanged(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + (routeRecord.routePayments == null ? 0 : routeRecord.routePayments.size()) + j + 1);
                        }
                        bOk = true;
                    }
                }


                break;
            }
        }
        return bOk;
    }

    public boolean updateDocumentInRouteDeleted(String uid) {
        int i;
        for (i=0; i<routeRecordList.size(); i++)
        {
            RouteRecord routeRecord=routeRecordList.get(i);
            int j;
            if (routeRecord.routeOrders!=null) {
                for (j = 0; j < routeRecord.routeOrders.size(); j++) {
                    //RoutesAdapter.Order
                    RouteRecord.RouteOrderRecord routeOrderRecord=routeRecord.routeOrders.get(j);
                    if (routeOrderRecord.order_uid.equals(uid))
                    {
                        // TODO
                        //notifyItemRemoved(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + routeRecord.routeOrders.size());
                        routeRecord.routeOrders.remove(j);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemRemoved(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + j + 1);
                        }
                        fillIndices(routeRecordList);
                        break;
                    }
                }
            }
            if (routeRecord.routeRefunds!=null) {
                for (j = 0; j < routeRecord.routeRefunds.size(); j++) {
                    RouteRecord.RouteRefundRecord routeRefundRecord=routeRecord.routeRefunds.get(j);
                    if (routeRefundRecord.refund_uid.equals(uid))
                    {
                        routeRecord.routeRefunds.remove(j);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemRemoved(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + j + 1);
                        }
                        fillIndices(routeRecordList);
                        break;
                    }
                }
            }
            if (routeRecord.routePayments!=null) {
                for (j = 0; j < routeRecord.routePayments.size(); j++) {
                    RouteRecord.RoutePaymentRecord routePaymentRecord=routeRecord.routePayments.get(j);
                    if (routePaymentRecord.payment_uid.equals(uid))
                    {
                        routeRecord.routePayments.remove(j);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemRemoved(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + j + 1);
                        }
                        fillIndices(routeRecordList);
                        break;
                    }
                }
            }

            if (routeRecord.routeDistribs!=null) {
                for (j = 0; j < routeRecord.routeDistribs.size(); j++) {
                    RouteRecord.RouteDistribsRecord routeDistribsRecord=routeRecord.routeDistribs.get(j);
                    if (routeDistribsRecord.distribs_uid.equals(uid))
                    {
                        routeRecord.routeDistribs.remove(j);
                        if (routeRecord.index_in_list!=-1) {
                            notifyItemRemoved(routeRecord.index_in_list + (routeRecord.bVisitPresent ? 1 : 0) + (routeRecord.routeOrders == null ? 0 : routeRecord.routeOrders.size()) + (routeRecord.routeRefunds == null ? 0 : routeRecord.routeRefunds.size()) + (routeRecord.routePayments == null ? 0 : routeRecord.routePayments.size()) + j + 1);
                        }
                        fillIndices(routeRecordList);
                        break;
                    }
                }
            }



            //ArrayList<RouteRecord.RouteRefundRecord> routeRefunds;
            //ArrayList<RouteRecord.RoutePaymentRecord> routePayments;
            //ArrayList<RouteRecord.RouteDistribsRecord> routeDistribs;


        }
        return true;
    }


    protected RoutesAdapter(Context context, String date, boolean bOnlyNotWorkedOut) {

        super();
        mContext = context;
        m_bOnlyNotWorkedOut = bOnlyNotWorkedOut;
        routeRecordList = FillRouteList(context, date);
        defaultTextColor = Common.getColorFromAttr(context, R.attr.myTextColor);
    }

    public static class RouteItemSpecification {
        public int itemType;
        public Object item;
        public int idx;

        public RouteItemSpecification(int itemType, Object item, int idx) {
            this.itemType = itemType;
            this.item = item;
            this.idx = idx;
        }
    }



    // Возвращает запись торговой точки для элемента
    public RouteRecord getRouteNode(int position) {
        int i;
        RouteRecord rec=null;
        for (i = 0; i < routeRecordList.size(); i++) {
            rec = routeRecordList.get(i);
            if (rec.index_in_list == position)
                return rec;
            if (rec.index_in_list > position) {
                //return routeRecordList.get(i-1);
                rec=routeRecordList.get(i - 1);
                while (rec.index_in_list==-1&&i>1)
                {
                    i--;
                    rec=routeRecordList.get(i - 1);
                }
                return rec;
            }
        }
        return rec;
    }

    // Возвращает элемент в строке
    public RouteItemSpecification getRouteItem(int position) {
        int i;
        for (i = 0; i < routeRecordList.size(); i++) {
            RouteRecord rec = routeRecordList.get(i);
            // признак hidden отдельно не проверяем, т.к. там индекс=-1, и будет пропущен
            if (rec.index_in_list == position)
                return new RouteItemSpecification(TYPE_ROUTE_ITEM, rec, i);
            if (rec.index_in_list > position) {
                break;
            }
        }
        // Надо смотреть в предыдущем
        RouteRecord rec = routeRecordList.get(i - 1);
        // Из-за скрытых элементов надо не предыдущий, а раньше него
        // проверка на i>0 в общем не имеет смысла, т.к. все равно поиск должен быть успешным
        // по определению
        while (rec.index_in_list==-1&&i>1)
        {
            i--;
            rec = routeRecordList.get(i - 1);
        }
        int idx = position - rec.index_in_list;
        idx--; // Пропускаем сам элемент TYPE_ROUTE_ITEM
        if (rec.bVisitPresent) {
            if (idx == 0)
                return new RouteItemSpecification(TYPE_VISITING_ITEM, rec, i - 1);
            idx--;
        }
        if (rec.routeOrders != null) {
            if (idx < rec.routeOrders.size())
                return new RouteItemSpecification(TYPE_ORDER_ITEM, rec.routeOrders.get(idx), i - 1);
            idx -= rec.routeOrders.size();
        }
        if (rec.routeRefunds != null) {
            if (idx < rec.routeRefunds.size())
                return new RouteItemSpecification(TYPE_REFUND_ITEM, rec.routeRefunds.get(idx), i - 1);
            idx -= rec.routeRefunds.size();
        }
        if (rec.routePayments != null) {
            if (idx < rec.routePayments.size())
                return new RouteItemSpecification(TYPE_PAYMENT_ITEM, rec.routePayments.get(idx), i - 1);
            idx -= rec.routePayments.size();
        }
        if (rec.routeDistribs != null) {
            if (idx < rec.routeDistribs.size())
                return new RouteItemSpecification(TYPE_DISTRIBS_ITEM, rec.routeDistribs.get(idx), i - 1);
            idx -= rec.routeDistribs.size();
        }
        // А сюда не придет
        return new RouteItemSpecification(TYPE_ROUTE_ITEM, null, -1);
    }

    @Override
    public int getItemViewType(int position) {
        RouteItemSpecification result = getRouteItem(position);
        return result.itemType;
    }


    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ROUTE_ITEM: {
                RoutesAdapter.RouteViewHolderHeader viewHolder = new RoutesAdapter.RouteViewHolderHeader(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_header, parent, false));
                return viewHolder;
            }
            case TYPE_VISITING_ITEM: {
                RoutesAdapter.RouteViewHolderVisit viewHolder = new RoutesAdapter.RouteViewHolderVisit(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_visit, parent, false));
                return viewHolder;
            }
            case TYPE_ORDER_ITEM: {
                RoutesAdapter.RouteViewHolderOrder viewHolder = new RoutesAdapter.RouteViewHolderOrder(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_order, parent, false));
                return viewHolder;
            }
            case TYPE_REFUND_ITEM: {
                RoutesAdapter.RouteViewHolderRefund viewHolder = new RoutesAdapter.RouteViewHolderRefund(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_order, parent, false));
                return viewHolder;
            }
            case TYPE_PAYMENT_ITEM: {
                RoutesAdapter.RouteViewHolderPayment viewHolder = new RoutesAdapter.RouteViewHolderPayment(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_payment, parent, false));
                return viewHolder;
            }
            case TYPE_DISTRIBS_ITEM: {
                RoutesAdapter.RouteViewHolderDistribs viewHolder = new RoutesAdapter.RouteViewHolderDistribs(LayoutInflater.from(parent.getContext()).inflate(R.layout.route_line_order, parent, false));
                return viewHolder;
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        setViewHolder(holder);
        holder.bindView(null, mContext, position);
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (routeRecordList != null)
        {
            int i=routeRecordList.size();
            while (--i>=0)
            {
                RouteRecord routeRecord = routeRecordList.get(i);
                if (routeRecord.index_in_list!=-1) {
                    // не hidden элемент
                    itemCount = routeRecord.index_in_list + routeRecord.getAllocatedLinesCount();
                    break;
                }
            }

        }
        return itemCount;
        /*
        if (routeRecordList != null && routeRecordList.size() > 0) {
            RouteRecord routeRecord = routeRecordList.get(routeRecordList.size() - 1);
            itemCount=routeRecord.index_in_list+routeRecord.getAllocatedLinesCount();
            itemCount = routeRecord.index_in_list + 1; // Индекс первого элемента +1, т.к. нумерация с нуля
            if (routeRecord.bVisitPresent)
                itemCount++;
            if (routeRecord.routeOrders != null)
                itemCount += routeRecord.routeOrders.size();
            if (routeRecord.routeRefunds != null)
                itemCount += routeRecord.routeRefunds.size();
            if (routeRecord.routePayments != null)
                itemCount += routeRecord.routePayments.size();
            if (routeRecord.routeDistribs != null)
                itemCount += routeRecord.routeDistribs.size();
        }
         */
    }

    protected void setViewHolder(RoutesAdapter.RouteViewHolder viewHolder) {
        this.mViewHolder = viewHolder;
    }


    public class RouteViewHolderHeader extends RouteViewHolder {
        public final TextView mRouteLineHeaderLineno;
        public final RelativeLayout mRouteLineHeaderGroupDebt;
        public final TextView mRouteLineHeaderDescr;
        public final TextView mRouteLineHeaderDebt;
        public final TextView mRouteLineHeaderDebtPast;

        public RouteViewHolderHeader(View view) {
            super(view);
            mRouteLineHeaderLineno = (TextView) view.findViewById(R.id.tvRouteLineHeaderLineNo);
            mRouteLineHeaderDescr = (TextView) view.findViewById(R.id.tvRouteLineHeaderDescr);
            mRouteLineHeaderGroupDebt = (RelativeLayout) view.findViewById(R.id.layoutNomenclatureLineItemGroupDebt);
            mRouteLineHeaderDebt = (TextView) view.findViewById(R.id.tvRouteLineHeaderDebt);
            mRouteLineHeaderDebtPast = (TextView) view.findViewById(R.id.tvRouteLineHeaderDebtPast);
        }

        @Override
        public void bindCursor(int position/*Cursor cursor*/) {
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord) {
                RouteRecord rec = (RouteRecord) result.item;
                mRouteLineHeaderLineno.setText(String.valueOf(result.idx + 1) + ".");
                mRouteLineHeaderDescr.setText(rec.distr_point_descr + (rec.distr_point_clients_descr.isEmpty() ? "" : "(" + rec.distr_point_clients_descr + ")"));
                //if (rec.bVisitPresent)
                if (!Common.isDateStringEmpty(rec.start_visit_time))
                {
                    if (Common.isDateStringEmpty(rec.end_visit_time))
                        itemView.setBackgroundColor(mContext.getResources().getColor(R.color.DARK_GREEN));
                    else
                        itemView.setBackgroundColor(Color.TRANSPARENT);
                } else
                {
                    itemView.setBackgroundColor(Color.TRANSPARENT);
                }
                if (rec.saldo!=0.0&&rec.saldo_past!=0.0)
                {
                    mRouteLineHeaderGroupDebt.setVisibility(View.VISIBLE);
                    mRouteLineHeaderDebt.setText(String.format("%.2f", rec.saldo));
                    mRouteLineHeaderDebtPast.setText(String.format("%.2f", rec.saldo_past));
                } else
                {
                    mRouteLineHeaderGroupDebt.setVisibility(View.GONE);
                    mRouteLineHeaderDebt.setText("-");
                    mRouteLineHeaderDebtPast.setText("-");
                }
            } else {
                mRouteLineHeaderLineno.setText("-");
                mRouteLineHeaderDescr.setText("-");
                mRouteLineHeaderGroupDebt.setVisibility(View.GONE);
                mRouteLineHeaderDebt.setText("-");
                mRouteLineHeaderDebtPast.setText("-");
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            //public boolean bVisitPresent;
            //public String required_visit_time;
            //public String start_visit_time;
            //public String end_visit_time;
        }

    }

    public class RouteViewHolderVisit extends RouteViewHolder {
        public final TextView mRouteLineVisit;

        public RouteViewHolderVisit(View view) {
            super(view);
            mRouteLineVisit = (TextView) view.findViewById(R.id.tvRouteLineVisit);
        }

        @Override
        public void bindCursor(int position/*Cursor cursor*/) {
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord) {
                RouteRecord rec = (RouteRecord) result.item;
                if (rec.end_visit_time != null && !rec.end_visit_time.isEmpty() && !rec.end_visit_time.equals("00010101")) {
                    mRouteLineVisit.setText(Common.dateTimeStringAsText(rec.start_visit_time) + "-" + Common.dateTimeStringAsText(rec.end_visit_time));
                } else
                    mRouteLineVisit.setText(Common.dateTimeStringAsText(rec.start_visit_time));
            } else {
                mRouteLineVisit.setText("-");
            }
        }

    }

    public class RouteViewHolderOrder extends RouteViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public final TextView mTextViewDocumentType;
        public final TextView mTextViewOrderListNumber;
        public final TextView mTextViewOrderListDate;
        public final TextView mTextViewOrderListSum;
        public final TextView mTextViewOrderListClientDescr;
        public final TextView mTextViewOrderListState;

        public long m_order_id = -1;

        public RouteViewHolderOrder(View view) {
            super(view);
            mTextViewDocumentType = (TextView) view.findViewById(R.id.tvDocumentType);
            mTextViewOrderListNumber = (TextView) view.findViewById(R.id.tvOrderListNumber);
            mTextViewOrderListDate = (TextView) view.findViewById(R.id.tvOrderListDate);
            mTextViewOrderListSum = (TextView) view.findViewById(R.id.tvOrderListSum);
            mTextViewOrderListClientDescr = (TextView) view.findViewById(R.id.tvOrderListClientDescr);
            mTextViewOrderListState = (TextView) view.findViewById(R.id.tvOrderListState);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteOrderRecord) {
                RouteRecord.RouteOrderRecord order = (RouteRecord.RouteOrderRecord) result.item;
                //Cursor cursor=mContext.getContentResolver().query(MTradeContentProvider.JOURNAL_CONTENT_URI, new String[]{"order_id", "state"}, "journal._id=?", new String[]{Long.toString(order._id)}, null);
                //int index_order_id=cursor.getColumnIndex("order_id");
                //int index_state=cursor.getColumnIndex("state");
                //if (cursor.moveToFirst()) {
                m_order_id = order._id;//cursor.getLong(index_order_id);
                int stateInt = order.state;//cursor.getInt(index_state);
                E_ORDER_STATE state = E_ORDER_STATE.fromInt(stateInt);
                if (E_ORDER_STATE.getCanBeDeleted(state)) {
                    //  add(int groupId, int itemId, int order, CharSequence title)
                    MenuItem menuItem = menu.add(0, R.id.action_delete_order, position, R.string.action_delete_order);
                    //menuItem.setOnMenuItemClickListener(mOnMyActionClickListener)
                    menuItem.setOnMenuItemClickListener(this);
                }
                if (E_ORDER_STATE.getCanBeCanceled(state)) {
                    //itemCancelOrder.setVisible(true);
                    MenuItem menuItem = menu.add(0, R.id.action_cancel_order, position, R.string.action_cancel_order);
                    menuItem.setOnMenuItemClickListener(this);
                }
                //}
                //cursor.close();
            }
            //menu.setHeaderTitle("Select The Action");
            //menu.add(0, ACTION_1_ID, getAdapterPosition(), "action 1");
            //menu.add(0, ACTION_2_ID, getAdapterPosition(), "action 2");
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete_order: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_order_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("deleteOrder", parameters);
                    }
                    return true;
                }
                case R.id.action_cancel_order: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_order_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("cancelOrder", parameters);
                    }
                    return true;
                }
            }
            return false;
        }


        @Override
        public void bindCursor(int position/*Cursor cursor*/) {
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteOrderRecord) {
                RouteRecord.RouteOrderRecord order = (RouteRecord.RouteOrderRecord) result.item;
                mTextViewDocumentType.setVisibility(View.GONE);

                int textColor = OrdersHelpers.fillOrderColor(defaultTextColor, mContext.getResources(), order.color, itemView);
                mTextViewOrderListNumber.setText(order.numdoc);
                MySingleton g = MySingleton.getInstance();
                if (order.datedoc.length() == 14) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Common.dateStringAsText(order.datedoc));
                    if (g.Common.PHARAON || g.Common.FACTORY) {
                        if (order.shipping_date.length() >= 8) {
                            sb.append('(').append(order.shipping_date).append(')');
                        }
                    }
                    mTextViewOrderListDate.setText(sb.toString());
                } else {
                    mTextViewOrderListDate.setText(order.datedoc);
                }
                mTextViewOrderListClientDescr.setText(order.clientDescr);
                mTextViewOrderListState.setText(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.fromInt(order.state), mContext));
                //String[] fromColumns = {"refund_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc"};
                //int[] toViews = {R.id.tvDocumentType, R.id.tvOrderListNumber, R.id.tvOrderListDate, R.id.tvOrderListState, R.id.tvOrderListClientDescr, R.id.tvOrderListSum}; // The TextView in simple_list_item_1

                double sum_doc = order.sum_doc;
                double sum_shipping = order.sum_shipping;
                boolean bShowSumShippingAnyway = false;
                if (g.Common.PRODLIDER) {
                    E_ORDER_STATE orderState = E_ORDER_STATE.fromInt(order.state);
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
                //
                if ((sum_shipping >= 0 || bShowSumShippingAnyway) && sum_doc != sum_shipping) {
                    mTextViewOrderListSum.setText(Common.DoubleToStringFormat(sum_doc, "%.2f") + "/" + g.Common.DoubleToStringFormat(sum_shipping, "%.2f"));
                    mTextViewOrderListSum.setBackgroundColor(Color.RED);
                } else {
                    // тут как обычно
                    mTextViewOrderListSum.setText(Common.DoubleToStringFormat(sum_doc, "%.2f"));
                    mTextViewOrderListSum.setBackgroundColor(Color.TRANSPARENT);
                }
                mTextViewOrderListNumber.setTextColor(textColor);
                mTextViewOrderListDate.setTextColor(textColor);
                mTextViewOrderListSum.setTextColor(textColor);
                mTextViewOrderListClientDescr.setTextColor(textColor);
                mTextViewOrderListState.setTextColor(textColor);

            } else {
                mTextViewOrderListNumber.setText("-");
            }
        }
    }

    public class RouteViewHolderRefund extends RouteViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public final TextView mTextViewDocumentType;
        public final TextView mTextViewRefundListNumber;
        public final TextView mTextViewRefundListDate;
        public final TextView mTextViewRefundListSum;
        public final TextView mTextViewRefundListClientDescr;
        public final TextView mTextViewRefundListState;

        public long m_refund_id = -1;

        public RouteViewHolderRefund(View view) {
            super(view);
            mTextViewDocumentType = (TextView) view.findViewById(R.id.tvDocumentType);
            mTextViewRefundListNumber = (TextView) view.findViewById(R.id.tvOrderListNumber);
            mTextViewRefundListDate = (TextView) view.findViewById(R.id.tvOrderListDate);
            mTextViewRefundListSum = (TextView) view.findViewById(R.id.tvOrderListSum);
            mTextViewRefundListClientDescr = (TextView) view.findViewById(R.id.tvOrderListClientDescr);
            mTextViewRefundListState = (TextView) view.findViewById(R.id.tvOrderListState);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteRefundRecord) {
                RouteRecord.RouteRefundRecord refund = (RouteRecord.RouteRefundRecord) result.item;
                m_refund_id = refund._id;//cursor.getLong(index_order_id);
                int stateInt = refund.state;//cursor.getInt(index_state);
                E_REFUND_STATE state = E_REFUND_STATE.fromInt(stateInt);
                if (E_REFUND_STATE.getCanBeDeleted(state)) {
                    //  add(int groupId, int itemId, int order, CharSequence title)
                    MenuItem menuItem = menu.add(0, R.id.action_delete_refund, position, R.string.action_delete_refund);
                    //menuItem.setOnMenuItemClickListener(mOnMyActionClickListener)
                    menuItem.setOnMenuItemClickListener(this);
                }
                if (E_REFUND_STATE.getCanBeCanceled(state)) {
                    //itemCancelOrder.setVisible(true);
                    MenuItem menuItem = menu.add(0, R.id.action_cancel_refund, position, R.string.action_cancel_refund);
                    menuItem.setOnMenuItemClickListener(this);
                }
                //}
                //cursor.close();
            }
            //menu.setHeaderTitle("Select The Action");
            //menu.add(0, ACTION_1_ID, getAdapterPosition(), "action 1");
            //menu.add(0, ACTION_2_ID, getAdapterPosition(), "action 2");
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete_refund: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_refund_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("deleteRefund", parameters);
                    }
                    return true;
                }
                case R.id.action_cancel_refund: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_refund_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("cancelRefund", parameters);
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void bindCursor(int position/*Cursor cursor*/) {
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteRefundRecord) {
                RouteRecord.RouteRefundRecord refund = (RouteRecord.RouteRefundRecord) result.item;
                mTextViewDocumentType.setText(R.string.label_refund);

                int textColor = OrdersHelpers.fillOrderColor(defaultTextColor, mContext.getResources(), refund.color, itemView);
                mTextViewRefundListNumber.setText(refund.numdoc);
                MySingleton g = MySingleton.getInstance();
                mTextViewRefundListDate.setText(Common.dateStringAsText(refund.datedoc));
                mTextViewRefundListClientDescr.setText(refund.clientDescr);
                mTextViewRefundListState.setText(MyDatabase.GetRefundStateDescr(E_REFUND_STATE.fromInt(refund.state), mContext));
                mTextViewRefundListSum.setText(R.string.label_refund);
                mTextViewDocumentType.setTextColor(textColor);
                mTextViewRefundListSum.setTextColor(textColor);
                mTextViewRefundListNumber.setTextColor(textColor);
                mTextViewRefundListDate.setTextColor(textColor);
                mTextViewRefundListSum.setTextColor(textColor);
                mTextViewRefundListClientDescr.setTextColor(textColor);
                mTextViewRefundListState.setTextColor(textColor);

            } else {
                mTextViewRefundListNumber.setText("-");
            }
        }
    }

    public class RouteViewHolderPayment extends RouteViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public final TextView mTextViewPaymentListNumber;
        public final TextView mTextViewPaymentListDate;
        public final TextView mTextViewPaymentListState;
        public final TextView mTextViewPaymentListClientDescr;
        public final TextView mTextViewPaymentListSum;

        public long m_payment_id = -1;

        public RouteViewHolderPayment(View view) {
            super(view);
            mTextViewPaymentListNumber = (TextView) view.findViewById(R.id.tvPaymentListNumber);
            mTextViewPaymentListDate = (TextView) view.findViewById(R.id.tvPaymentListDate);
            mTextViewPaymentListState = (TextView) view.findViewById(R.id.tvPaymentListState);
            mTextViewPaymentListClientDescr = (TextView) view.findViewById(R.id.tvPaymentListClientDescr);
            mTextViewPaymentListSum = (TextView) view.findViewById(R.id.tvPaymentListSum);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RoutePaymentRecord) {
                RouteRecord.RoutePaymentRecord order = (RouteRecord.RoutePaymentRecord) result.item;
                m_payment_id = order._id;//cursor.getLong(index_order_id);
                int stateInt = order.state;//cursor.getInt(index_state);
                E_PAYMENT_STATE state = E_PAYMENT_STATE.fromInt(stateInt);
                if (E_PAYMENT_STATE.getPaymentCanBeDeleted(state)) {
                    //  add(int groupId, int itemId, int order, CharSequence title)
                    MenuItem menuItem = menu.add(0, R.id.action_delete_payment, position, R.string.action_delete_payment);
                    //menuItem.setOnMenuItemClickListener(mOnMyActionClickListener)
                    menuItem.setOnMenuItemClickListener(this);
                }
                if (E_PAYMENT_STATE.getPaymentCanBeCanceled(state)) {
                    //itemCancelOrder.setVisible(true);
                    MenuItem menuItem = menu.add(0, R.id.action_cancel_payment, position, R.string.action_cancel_payment);
                    menuItem.setOnMenuItemClickListener(this);
                }
                //}
                //cursor.close();
            }
            //menu.setHeaderTitle("Select The Action");
            //menu.add(0, ACTION_1_ID, getAdapterPosition(), "action 1");
            //menu.add(0, ACTION_2_ID, getAdapterPosition(), "action 2");
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete_payment: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_payment_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("deletePayment", parameters);
                    }
                    return true;
                }
                case R.id.action_cancel_payment: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_payment_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("cancelPayment", parameters);
                    }
                    return true;
                }
            }
            return false;
        }


        @Override
        public void bindCursor(int position/*Cursor cursor*/) {
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RoutePaymentRecord) {
                RouteRecord.RoutePaymentRecord payment = (RouteRecord.RoutePaymentRecord) result.item;
                int color = PaymentsHelpers.fillPaymentColor(defaultTextColor, mContext.getResources(), payment.color, itemView);
                mTextViewPaymentListNumber.setText(payment.numdoc);
                mTextViewPaymentListDate.setText(Common.dateTimeStringAsText(payment.datedoc));
                mTextViewPaymentListState.setText(MyDatabase.GetPaymentStateDescr(E_PAYMENT_STATE.fromInt(payment.state), mContext));
                mTextViewPaymentListClientDescr.setText(payment.clientDescr);
                mTextViewPaymentListSum.setText(String.valueOf(payment.sum_doc));
                mTextViewPaymentListNumber.setTextColor(color);
                mTextViewPaymentListDate.setTextColor(color);
                mTextViewPaymentListState.setTextColor(color);
                mTextViewPaymentListClientDescr.setTextColor(color);
                mTextViewPaymentListSum.setTextColor(color);
            } else {
                mTextViewPaymentListNumber.setText("-");
            }
        }

    }

    public class RouteViewHolderDistribs extends RouteViewHolder implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        //public final TextView mRouteLineDistribs;
        public final TextView mTextViewDocumentType;
        public final TextView mTextViewDistribsListNumber;
        public final TextView mTextViewDistribsListDate;
        public final TextView mTextViewDistribsListSum;
        public final TextView mTextViewDistribsListClientDescr;
        public final TextView mTextViewDistribsListState;

        public long m_distribs_id = -1;

        public RouteViewHolderDistribs(View view) {
            super(view);
            mTextViewDocumentType = (TextView) view.findViewById(R.id.tvDocumentType);
            mTextViewDistribsListNumber = (TextView) view.findViewById(R.id.tvOrderListNumber);
            mTextViewDistribsListDate = (TextView) view.findViewById(R.id.tvOrderListDate);
            mTextViewDistribsListSum = (TextView) view.findViewById(R.id.tvOrderListSum);
            mTextViewDistribsListClientDescr = (TextView) view.findViewById(R.id.tvOrderListClientDescr);
            mTextViewDistribsListState = (TextView) view.findViewById(R.id.tvOrderListState);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteDistribsRecord) {
                RouteRecord.RouteDistribsRecord distribs = (RouteRecord.RouteDistribsRecord) result.item;
                m_distribs_id = distribs._id;//cursor.getLong(index_order_id);
                int stateInt = distribs.state;//cursor.getInt(index_state);
                E_DISTRIBS_STATE state = E_DISTRIBS_STATE.fromInt(stateInt);
                if (E_DISTRIBS_STATE.getDistribsCanBeCanceled(state)) {
                    //  add(int groupId, int itemId, int order, CharSequence title)
                    MenuItem menuItem = menu.add(0, R.id.action_delete_distribs, position, R.string.action_delete_distribs);
                    //menuItem.setOnMenuItemClickListener(mOnMyActionClickListener)
                    menuItem.setOnMenuItemClickListener(this);
                }
                if (E_DISTRIBS_STATE.getDistribsCanBeCanceled(state)) {
                    //itemCancelOrder.setVisible(true);
                    MenuItem menuItem = menu.add(0, R.id.action_cancel_distribs, position, R.string.action_cancel_distribs);
                    menuItem.setOnMenuItemClickListener(this);
                }
                //}
                //cursor.close();
            }
            //menu.setHeaderTitle("Select The Action");
            //menu.add(0, ACTION_1_ID, getAdapterPosition(), "action 1");
            //menu.add(0, ACTION_2_ID, getAdapterPosition(), "action 2");
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_delete_distribs: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_distribs_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("deleteDistribs", parameters);
                    }
                    return true;
                }
                case R.id.action_cancel_distribs: {
                    Bundle parameters = new Bundle();
                    parameters.putLong("id", m_distribs_id);
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).onUniversalEventListener("cancelDistribs", parameters);
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void bindCursor(int position/*Cursor cursor*/) {

            RouteItemSpecification result = getRouteItem(position);
            if (result.item != null && result.item instanceof RouteRecord.RouteDistribsRecord) {
                RouteRecord.RouteDistribsRecord distribs = (RouteRecord.RouteDistribsRecord) result.item;
                mTextViewDocumentType.setText(R.string.label_distribs);
                int textColor = OrdersHelpers.fillOrderColor(defaultTextColor, mContext.getResources(), distribs.color, itemView);
                mTextViewDistribsListNumber.setText(distribs.numdoc);
                MySingleton g = MySingleton.getInstance();
                mTextViewDistribsListDate.setText(Common.dateStringAsText(distribs.datedoc));
                mTextViewDistribsListClientDescr.setText(distribs.clientDescr);
                mTextViewDistribsListState.setText(MyDatabase.GetDistribsStateDescr(E_DISTRIBS_STATE.fromInt(distribs.state), mContext));
                mTextViewDistribsListSum.setText(R.string.label_distribs_short);
                mTextViewDocumentType.setTextColor(textColor);
                mTextViewDistribsListNumber.setTextColor(textColor);
                mTextViewDistribsListDate.setTextColor(textColor);
                mTextViewDistribsListSum.setTextColor(textColor);
                mTextViewDistribsListClientDescr.setTextColor(textColor);
                mTextViewDistribsListState.setTextColor(textColor);

            } else {
                mTextViewDistribsListNumber.setText("-");
            }
        }


    }


    public abstract class RouteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public RouteViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void bindView(View view, Context context, int position/*, Cursor cursor*/) {
            // Bind cursor to our ViewHolder
            mViewHolder.bindCursor(position/*cursor*/);
        }

        public abstract void bindCursor(int position/*Cursor cursor*/);


        @Override
        public void onClick(View v) {
            if (clickListener != null)
                clickListener.onItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v) {
            if (clickListener != null)
                return clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }

    }

    public void setOnItemClickListener(RoutesAdapter.ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);

        boolean onItemLongClick(int position, View v);
    }

}
