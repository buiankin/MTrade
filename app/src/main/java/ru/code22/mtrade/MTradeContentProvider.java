package ru.code22.mtrade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class MTradeContentProvider extends ContentProvider {
	
	private static final String LOG_TAG = "mtradeLogs";
	
	// При забивании количества товара, превышающего остаток, должно выводится предупреждающее 
	// сообщение «не хватает … шт. Продолжить?». (Строки по недостающим товарам выделять красным)	
	// У нас уже в заявке есть красные строки – в случае изменения первоначального количества в заявке показываются оба количества и строка красная. Как их отличить?
	// ну пусть в этом случае будет зеленая ) , если можно.
	
	// Думаю, красным должно выделяться то, чего не хватает в данный момент (так агентам будет понятнее). Если после обмена товара стало хватать, то строка становится бесцветная. И наоборот.
	// Таким образом, внутри заявки строки могут быть трёх цветов: бесцветная (всё нормально), красная (не хватает товара), зеленая (количество товара изменено).	
	
	
	public static final String JOURNAL_ORDER_COLOR_FOR_TRIGGER =
	"case when new.dont_need_send=1 then 1 "+
	// сначала состояния, при которых данные отправляются
    //////////////////////////////////////////
	// запрос отмены - серый
    "when new.state="+E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value()+" then 2 "+
	// согласование - оранжевый
    "when new.state="+E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value()+" then 6 "+
	// а потом все остальные
    /////////////////////////////////////////
	// отменен - красный
    "when new.state="+E_ORDER_STATE.E_ORDER_STATE_CANCELED.value()+" then 7 "+
	// восстановлен, сбой - красный
    "when new.state="+E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value()+" then 3 "+
	// неизвестное - красный
    "when new.state="+E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value()+" then 3 "+
	// отправлен - голубой
	"when new.state="+E_ORDER_STATE.E_ORDER_STATE_SENT.value()+" then 5 "+
	// выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
	"when new.closed_not_full=1 then 4 "+
	// создана и будет отправляться - зеленая
	"when new.versionPDA<>new.versionPDA_ack and (new.state="+E_ORDER_STATE.E_ORDER_STATE_CREATED.value()+") then 10 "+
	"else 0 end";
	
	public static final String JOURNAL_REFUND_COLOR_FOR_TRIGGER =
	"case when new.dont_need_send=1 then 1 "+
	// сначала состояния, при которых данные отправляются
    //////////////////////////////////////////
	// запрос отмены - серый
    "when new.state="+E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value()+" then 2 "+
	// согласование - оранжевый
    "when new.state="+E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()+" then 6 "+
	// а потом все остальные
    /////////////////////////////////////////
	// отменен - красный
    "when new.state="+E_REFUND_STATE.E_REFUND_STATE_CANCELED.value()+" then 7 "+
	// восстановлен, сбой - красный
    "when new.state="+E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value()+" then 3 "+
	// неизвестное - красный
    "when new.state="+E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value()+" then 3 "+
	// отправлен - голубой
	"when new.state="+E_REFUND_STATE.E_REFUND_STATE_SENT.value()+" then 5 "+
	// выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
	"when new.closed_not_full=1 then 4 "+
	// создана и будет отправляться - зеленая
	"when new.versionPDA<>new.versionPDA_ack and (new.state="+E_REFUND_STATE.E_REFUND_STATE_CREATED.value()+") then 10 "+
	"else 0 end";
	
	public static final String JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER =
	// сначала состояния, при которых данные отправляются
    //////////////////////////////////////////
	// запрос отмены - серый
    "case when new.state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value()+" then 2 "+
	// а потом все остальные
    /////////////////////////////////////////
	// отменен - красный
    "when new.state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value()+" then 7 "+
	// неизвестное - красный
    "when new.state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value()+" then 3 "+
	// отправлен - голубой
	"when new.state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()+" then 5 "+
	// создана и будет отправляться - зеленая
	"when new.versionPDA<>new.versionPDA_ack and (new.state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()+") then 10 "+
	"else 0 end";
	
	// в принципе, этот журнал используется только если не используется общий журнал
	// (скорее всего он уже не используется совсем)
	public static final String ORDERS_COLOR_COLUMN =
			"case when dont_need_send=1 then 1 "+
			// сначала состояния, при которых данные отправляются
		    //////////////////////////////////////////
			// запрос отмены - серый
		    "when state="+E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value()+" then 2 "+
			// согласование - оранжевый
		    "when state="+E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value()+" then 6 "+
			// а потом все остальные
		    /////////////////////////////////////////
			// отменен - красный
		    "when state="+E_ORDER_STATE.E_ORDER_STATE_CANCELED.value()+" then 7 "+
			// восстановлен, сбой - красный
		    "when state="+E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value()+" then 3 "+
			// неизвестное - красный
		    "when state="+E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value()+" then 3 "+
			// отправлен - голубой
			"when state="+E_ORDER_STATE.E_ORDER_STATE_SENT.value()+" then 5 "+
			// выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
			"when closed_not_full=1 then 4 "+
			// создана и будет отправляться - зеленая
			"when versionPDA<>versionPDA_ack and (state="+E_ORDER_STATE.E_ORDER_STATE_CREATED.value()+") then 10 "+
			"else 0 end as color";
	
	public static final String CASH_PAYMENTS_COLOR_COLUMN =
			// сначала состояния, при которых данные отправляются
		    //////////////////////////////////////////
			// запрос отмены - серый
		    "case when state="+E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL.value()+" then 2 "+
			// а потом все остальные
		    /////////////////////////////////////////
			// отменен - красный
		    "when state="+E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED.value()+" then 7 "+
			// неизвестное - красный
		    "when state="+E_PAYMENT_STATE.E_PAYMENT_STATE_UNKNOWN.value()+" then 3 "+
			// отправлен - голубой
			"when state="+E_PAYMENT_STATE.E_PAYMENT_STATE_SENT.value()+" then 5 "+
			// создана и будет отправляться - зеленая
			"when versionPDA<>versionPDA_ack and (state="+E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED.value()+") then 10 "+
			"else 0 end as color";

    public static final String REFUNDS_COLOR_COLUMN =
            "case when dont_need_send=1 then 1 "+
            // сначала состояния, при которых данные отправляются
            //////////////////////////////////////////
            // запрос отмены - серый
            "when state="+E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value()+" then 2 "+
            // согласование - оранжевый
            "when state="+E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()+" then 6 "+
            // а потом все остальные
            /////////////////////////////////////////
            // отменен - красный
            "when state="+E_REFUND_STATE.E_REFUND_STATE_CANCELED.value()+" then 7 "+
            // восстановлен, сбой - красный
            "when state="+E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value()+" then 3 "+
            // неизвестное - красный
            "when state="+E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value()+" then 3 "+
            // отправлен - голубой
            "when state="+E_REFUND_STATE.E_REFUND_STATE_SENT.value()+" then 5 "+
            // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
            "when closed_not_full=1 then 4 "+
            // создана и будет отправляться - зеленая
            "when versionPDA<>versionPDA_ack and (state="+E_REFUND_STATE.E_REFUND_STATE_CREATED.value()+") then 10 "+
            "else 0 end as color";

    public static final String DISTRIBS_COLOR_COLUMN =
            // сначала состояния, при которых данные отправляются
            //////////////////////////////////////////
            // запрос отмены - серый
            "case when state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value()+" then 2 "+
            // а потом все остальные
            /////////////////////////////////////////
            // отменен - красный
            "when state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value()+" then 7 "+
            // неизвестное - красный
            "when state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value()+" then 3 "+
            // отправлен - голубой
            "when state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()+" then 5 "+
            // создана и будет отправляться - зеленая
            "when versionPDA<>versionPDA_ack and (state="+E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()+") then 10 "+
            "else 0 end as color";


	public static final String CLIENT_DESCR_COLUMN = "CASE WHEN orders.create_client=1 THEN orders.create_client_descr ELSE IFNULL(clients.descr, \"{\"||orders.client_id||\"}\") END as client_descr";
	public static final String CASH_PAYMENTS_CLIENT_DESCR_COLUMN = "IFNULL(clients.descr, \"{\"||cash_payments.client_id||\"}\") as client_descr";
	public static final String SALDO_CLIENT_DESCR_COLUMN = "IFNULL(clients.descr, \"{\"||saldo_extended.client_id||\"}\") as client_descr";
	public static final String SALDO_AGREEMENT_DESCR_COLUMN = "IFNULL(agreements.descr, saldo_extended.agreement_descr) as agreement_descr";
	public static final String AGREEMENT_DESCR_COLUMN = "agreements.descr as agreement_descr";
    public static final String AGREEMENT30_DESCR_COLUMN = "agreements30.descr as agreement_descr";
	public static final String ORGANIZATION_DESCR_COLUMN = "organizations.descr as organization_descr";
	public static final String PRICE_TYPE_DESCR_COLUMN = "pricetypes.descr as pricetype_descr";
	public static final String NOMENCLATURE_DESCR_COLUMN = "IFNULL(nomenclature.descr, \"{\"||nomenclature_id||\"}\") as nomencl_descr";
	public static final String NOMENCLATURE_WEIGHT_COLUMN = "nomenclature.weight_k_1 as nomencl_weight_k_1";
	public static final String NOMENCLATURE_ID_COLUMN = "nomenclature.id as nomenclature_id";
    public static final String NOMENCLATURE_ID_SURFING_COLUMN = "ifnull(nomenclature.id, h.id) as nomenclature_id";
    public static final String NOMENCLATURE_DESCR_SURFING_COLUMN = "ifnull(nomenclature.descr, h.groupDescr) as descr";
	public static final String NOMENCLATURE_GROUP_COLUMN = "h.groupDescr as h_groupDescr";
	
	public static final String CONTRACT_DESCR_COLUMN = "IFNULL(distribsContracts.descr, \"{\"||contract_id||\"}\") as contract_descr";
	
	
	public static final String NOMENCLATURE_REST_STOCK_COLUMN = "restsS.quantity-restsS.quantity_reserve as nom_quantity";
	public static final String NOMENCLATURE_SALES_COLUMN = "restsS.saledNow as nom_quantity_saled_now";
	public static final String DUMMY_COLUMN = "0 as zero";
	
	// БД
	//private static final String DIR = "/sdcard";
    public static final String DB_NAME = "mdata.db";
    public static final int DB_VERSION = 73;
    
    // Таблица
    private static final String ORDERS_TABLE = "orders";
    private static final String CASH_PAYMENTS_TABLE = "cash_payments";
    private static final String REFUNDS_TABLE = "refunds";
    private static final String CLIENTS_TABLE = "clients";
    private static final String NOMENCLATURE_TABLE = "nomenclature";

    private static final String NOMENCLATURE_LIST_TABLE =
    		"nomenclature left join rests_sales_stuff as restsS on restsS.nomenclature_id=nomenclature.id "+
    		"left join salesV as sales_client on sales_client.nomenclature_id=nomenclature.id or sales_client.nomenclature_id=nomenclature.parent_id "+
    		"left join pricesV as prices on prices.nomenclature_id=nomenclature.id "+
            // здесь хранятся данные как товаров, так и групп, но нам важно для групп только данные по группам, а по товарам и не нужно - в выборку они не включены
            "left join salesV_7 as sales7 on sales7.nomenclature_id=nomenclature.id "+
    		"left join nomenclature_hierarchy h on h.id=nomenclature.id "+
    		"left join nomenclature_hierarchy h_parent on h_parent.id=nomenclature.parent_id " +
    		"left join discounts_stuff d_s on d_s.nomenclature_id=nomenclature.id " +
    		"left join discounts_stuff d_s_p on d_s_p.nomenclature_id=nomenclature.parent_id";

    // отличие от NOMENCLATURE_LIST_TABLE в том, что корневой элемент (он только в иерархии) должен присутствоать
    private static final String NOMENCLATURE_SURFING_TABLE = NOMENCLATURE_LIST_TABLE;
            /*
            "nomenclature_hierarchy h "+
            "left join nomenclature on nomenclature.id=h.id " +
                    "left join rests_sales_stuff as restsS on restsS.nomenclature_id=nomenclature.id "+
                    "left join salesV as sales_client on sales_client.nomenclature_id=nomenclature.id or sales_client.nomenclature_id=nomenclature.parent_id "+
                    "left join pricesV as prices on prices.nomenclature_id=nomenclature.id "+
                    // здесь хранятся данные как товаров, так и групп, но нам важно для групп только данные по группам, а по товарам и не нужно - в выборку они не включены
                    "left join salesV_7 as sales7 on sales7.nomenclature_id=nomenclature.id "+
                    "left join nomenclature_hierarchy h_parent on h_parent.id=nomenclature.parent_id " +
                    "left join discounts_stuff d_s on d_s.nomenclature_id=nomenclature.id " +
                    "left join discounts_stuff d_s_p on d_s_p.nomenclature_id=nomenclature.parent_id";
            */
    
    private static final String RESTS_TABLE = "rests";
    private static final String SALDO_TABLE = "saldo";
    private static final String SALDO_EXTENDED_TABLE = "saldo_extended";
    private static final String SALDO_EXTENDED_JOURNAL_TABLE = "saldo_extended left join clients on clients.id=saldo_extended.client_id left join agreements on agreements.id=saldo_extended.agreement_id";
    private static final String AGREEMENTS_TABLE = "agreements";
    private static final String AGREEMENTS30_TABLE = "agreements30";
    private static final String VERSIONS_TABLE = "versions";
    private static final String VERSIONS_SALES_TABLE = "sales_versions";
    private static final String PRICETYPES_TABLE = "pricetypes";
    private static final String STOCKS_TABLE = "stocks";
    private static final String PRICES_TABLE = "prices";
    private static final String PRICES_AGREEMENTS30_TABLE = "prices_agreements30";
    private static final String AGENTS_TABLE = "agents";
    private static final String CURATORS_TABLE = "curators";
    private static final String PERMISSIONS_REQUESTS_TABLE = "permissions_requests";
    private static final String ROUTES_TABLE = "routes";
    private static final String ROUTES_LINES_TABLE = "routes_lines";
    private static final String ROUTES_DATES_TABLE = "routes_dates";
    private static final String ROUTES_DATES_LIST_TABLE =	"routes_dates left join routes on routes.id=routes_dates.route_id";

    private static final String REAL_ROUTES_DATES_TABLE = "real_routes_dates";
    private static final String REAL_ROUTES_LINES_TABLE = "real_routes_lines";

    private static final String CURATORS_LIST_TABLE = 
    		"(select" +
    		" _id*2 as _id,id,isFolder,parent_id,null as client_id,code,descr from curators " +
    		" union all" +
    		" select" +
    		" max(saldo_extended._id)*2+1,manager_id,2,null,client_id,manager_descr,manager_descr from saldo_extended" +
    		" left join curators on curators.id=manager_id" +
    		" where curators.id is null" +
    		" group by manager_id,client_id,manager_descr,manager_descr" +
    		") cr";
    
    private static final String DISTR_POINTS_TABLE = "distr_points";
    private static final String DISTR_POINTS_LIST_TABLE = "distr_points left join pricetypes on pricetypes.id=distr_points.price_type_id";
    private static final String ORGANIZATIONS_TABLE = "organizations";
    private static final String CLIENTS_WITH_SALDO_TABLE = "clients left join saldo on saldo.client_id=clients.id";
    private static final String ORDERS_LINES_TABLE = "ordersLines";
    private static final String REFUNDS_LINES_TABLE = "refundsLines";
    private static final String ORDERS_JOURNAL_TABLE = "orders left join clients on clients.id=orders.client_id";
    private static final String JOURNAL_TABLE = "journal left join clients on clients.id=journal.client_id";
    
    private static final String CASH_PAYMENTS_JOURNAL_TABLE = "cash_payments left join clients on clients.id=cash_payments.client_id";
    private static final String AGREEMENTS_LIST_TABLE = "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id";
    private static final String AGREEMENTS30_LIST_TABLE = "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id";
    
    private static final String AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE = "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id " +
    		"left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements.id";
    private static final String AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE = "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id " +
            "left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements30.id";

    private static final String ORDERS_LINES_COMPLEMENTED_TABLE = "ordersLines left join nomenclature on nomenclature.id=ordersLines.nomenclature_id";
    private static final String REFUNDS_LINES_COMPLEMENTED_TABLE = "refundsLines left join nomenclature on nomenclature.id=refundsLines.nomenclature_id";
    private static final String SEANCES_TABLE = "seances";
    private static final String VICARIOUS_POWER_TABLE = "vicarious_power";
    private static final String GPS_COORD_TABLE = "gps_coord";
    private static final String MESSAGES_TABLE = "messages";
    private static final String MESSAGES_LIST_TABLE = "messages left join clients on clients.id=messages.client_id and type_idx in ("+E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value()+","+E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value()+") left join agents on agents.id=messages.sender_id and acknowledged&4==0 or agents.id=messages.receiver_id and acknowledged&4!=0";
    //private static final String SALES_TABLE = "sales";
    private static final String CLIENTS_PRICE_TABLE = "clients_price";
    private static final String CURATORS_PRICE_TABLE = "curators_price";
    private static final String SETTINGS_TABLE = "settings";
    private static final String SALES_LOADED_TABLE = "salesloaded";

    private static final String SALES_LOADED_WITH_COMMON_GROUPS_TABLE = "(select max(s._id) as _id, datedoc, numdoc, client_id, case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, sum(quantity) as quantity, price from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, price)";
    
    private static final String NOMENCLATURE_HIERARCHY_TABLE = "nomenclature_hierarchy";
    private static final String SALES_L_TABLE = "salesL";
    private static final String SALES_L2_TABLE = "salesL2";
    private static final String SIMPLE_DISCOUNTS_TABLE = "simple_discounts";
    
    private static final String PLACES_TABLE = "places";
    private static final String OCCUPIED_PLACES_TABLE = "occupied_places";
    private static final String ORDERS_PLACES_TABLE = "ordersPlaces";
    private static final String ORDERS_PLACES_LIST_TABLE = "orders join ordersPlaces on ordersPlaces.order_id=orders._id";
    
    private static final String DISTRIBS_CONTRACTS_TABLE = "distribsContracts";
    private static final String DISTRIBS_CONTRACTS_LIST_TABLE = "distribsContractsList"; // TODO
    private static final String DISTRIBS_LINES_TABLE = "distribsLines";
    private static final String DISTRIBS_LINES_COMPLEMENTED_TABLE = "distribsLines left join distribsContracts on distribsContracts.id=distribsLines.contract_id";
    private static final String DISTRIBS_TABLE = "distribs";
    
    private static final String EQUIPMENT_TABLE = "equipment";
    private static final String EQUIPMENT_RESTS_TABLE = "equipment_rests";
    private static final String EQUIPMENT_RESTS_LIST_TABLE =	"equipment_rests left join equipment on equipment.id=equipment_rests.nomenclature_id";

    private static final String MTRADE_LOG_TABLE = "mtradelog";

    // Uri
    // authority
    public static final String AUTHORITY = "ru.code22.providers.mtrade";
    // path
    private static final String ORDERS_PATH = "orders";
    private static final String CASH_PAYMENTS_PATH="cash_payments";
    private static final String REFUNDS_PATH = "refunds";
    private static final String JOURNAL_PATH = "journal";
    private static final String CLIENTS_PATH = "clients";
    private static final String NOMENCLATURE_PATH = "nomenclature";
    private static final String RESTS_PATH = "rests";
    private static final String SALDO_PATH = "saldo";
    private static final String SALDO_EXTENDED_PATH = "saldo_extended";
    private static final String SALDO_EXTENDED_JOURNAL_PATH = "saldo_extended_journal";
    private static final String AGREEMENTS_PATH = "agreement";
    private static final String AGREEMENTS30_PATH = "agreement30";
    private static final String PRICETYPES_PATH = "pricetypes";
    private static final String VERSIONS_PATH = "versions";
    private static final String STOCKS_PATH = "stocks";
    private static final String PRICES_PATH = "prices";
    private static final String PRICES_AGREEMENTS30_PATH = "pricesAgreements30";
    private static final String AGENTS_PATH = "agents";
    private static final String CURATORS_PATH = "curators";
    private static final String CURATORS_LIST_PATH = "curatorsList";
    private static final String DISTR_POINTS_PATH = "distr_points";
    private static final String DISTR_POINTS_LIST_PATH = "distr_pointsList";
    private static final String ORGANIZATIONS_PATH = "organizations";
    private static final String CLIENTS_WITH_SALDO_PATH = "clients_saldo";
    private static final String ORDERS_LINES_PATH = "ordersLines";
    private static final String REFUNDS_LINES_PATH = "refundsLines";
    private static final String ORDERS_JOURNAL_PATH = "ordersJournal";
    private static final String CASH_PAYMENTS_JOURNAL_PATH = "cash_paymentsJournal";
    private static final String AGREEMENTS_LIST_PATH = "agreementsList";
    private static final String AGREEMENTS30_LIST_PATH = "agreements30List";
    private static final String AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH = "agreementsListSaldoOnly";
    private static final String AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH = "agreements30ListSaldoOnly";
    private static final String ORDERS_LINES_COMPLEMENTED_PATH = "ordersLinesComplemented";
    private static final String REFUNDS_LINES_COMPLEMENTED_PATH = "refundsLinesComplemented";
    private static final String SEANCES_PATH = "seances";
    private static final String SEANCES_INCOMING_PATH = "seancesIncoming";
    private static final String SEANCES_OUTGOING_PATH = "seancesOutgoing";
    private static final String REINDEX_PATH = "reindex";
    private static final String VACUUM_PATH = "vacuum";
    private static final String SORT_PATH = "sort";
    private static final String VICARIOUS_POWER_PATH = "vicarious_power";
    private static final String MESSAGES_PATH = "messages";
    private static final String MESSAGES_LIST_PATH = "messagesList";
    private static final String NOMENCLATURE_LIST_PATH = "nomenclatureList";
    private static final String NOMENCLATURE_SURFING_PATH = "nomenclatureList";
    private static final String SALES_PATH = "sales";
    private static final String RESTS_SALES_STUFF_PATH = "rests_sales_stuff";
    private static final String DISCOUNTS_STUFF_MEGA_PATH = "discounts_stuff_mega";
    private static final String DISCOUNTS_STUFF_SIMPLE_PATH = "discounts_stuff_simple";
    private static final String DISCOUNTS_STUFF_OTHER_PATH = "discounts_stuff_other";
    private static final String PRICESV_MEGA_PATH = "pricesVmega";
    private static final String PRICESV_OTHER_PATH = "pricesVother";
    private static final String CLIENTS_PRICE_PATH = "clients_price";
    private static final String CURATORS_PRICE_PATH = "curators_price";
    private static final String SETTINGS_PATH = "settings";
    private static final String SALES_LOADED_PATH = "salesloaded";
    private static final String SALES_L2_PATH = "salesL2";
    
    private static final String VERSIONS_SALES_PATH = "salesversions";
    private static final String NOMENCLATURE_HIERARCHY_PATH = "nomenclature_hierarchy";
    private static final String CREATE_VIEWS_PATH = "create_views";
    private static final String CREATE_SALES_L_PATH = "create_salesL";
    private static final String ORDERS_SILENT_PATH = "ordersSilewhile (distribsJournal.moveToNext()) {nt";
    private static final String REFUNDS_SILENT_PATH = "refundsSilent";
    
    private static final String SIMPLE_DISCOUNTS_PATH = "simple_discounts";
    
    private static final String PLACES_PATH = "places";
    private static final String OCCUPIED_PLACES_PATH = "occupiedPlaces";
    private static final String ORDERS_PLACES_PATH = "ordersPlaces";
    private static final String ORDERS_PLACES_LIST_PATH = "ordersPlacesList";

    private static final String GPS_COORD_PATH = "gps_coord";
    
    private static final String DISTRIBS_CONTRACTS_PATH = "distribsContracts";
    private static final String DISTRIBS_CONTRACTS_LIST_PATH = "distribsContractsList";
    private static final String DISTRIBS_LINES_PATH = "distribsLines";
    private static final String DISTRIBS_LINES_COMPLEMENTED_PATH = "distribsLinesComplemented";
    private static final String DISTRIBS_PATH = "distribs";
    
    private static final String EQUIPMENT_PATH = "equipment";
    private static final String EQUIPMENT_RESTS_PATH = "equipment_rests";
    private static final String EQUIPMENT_RESTS_LIST_PATH = "equipment_rests_list";
    
    private static final String MTRADE_LOG_PATH = "mtradelog";
    private static final String PERMISSIONS_REQUESTS_PATH = "permissions_requests";

    private static final String ROUTES_PATH = "routes";
    private static final String ROUTES_LINES_PATH = "routes_lines";
    private static final String ROUTES_DATES_PATH = "routes_dates";
    private static final String ROUTES_DATES_LIST_PATH = "routes_dates_list";

    private static final String REAL_ROUTES_DATES_PATH = "real_routes_dates";
    private static final String REAL_ROUTES_LINES_PATH = "real_routes_lines";

    // Общий Uri
    public static final Uri ORDERS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + ORDERS_PATH);
    public static final Uri CASH_PAYMENTS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + CASH_PAYMENTS_PATH);
    public static final Uri REFUNDS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + REFUNDS_PATH);
    public static final Uri JOURNAL_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + JOURNAL_PATH);
    public static final Uri CLIENTS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + CLIENTS_PATH);
    public static final Uri NOMENCLATURE_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + NOMENCLATURE_PATH);    
    public static final Uri RESTS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + RESTS_PATH);    
    public static final Uri SALDO_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SALDO_PATH);
    public static final Uri SALDO_EXTENDED_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + SALDO_EXTENDED_PATH);
    public static final Uri SALDO_EXTENDED_JOURNAL_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SALDO_EXTENDED_JOURNAL_PATH);
    public static final Uri AGREEMENTS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + AGREEMENTS_PATH);
    public static final Uri AGREEMENTS30_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + AGREEMENTS30_PATH);
    public static final Uri VERSIONS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + VERSIONS_PATH);
    public static final Uri PRICETYPES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + PRICETYPES_PATH);
    public static final Uri STOCKS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + STOCKS_PATH);
    public static final Uri PRICES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + PRICES_PATH);
    public static final Uri PRICES_AGREEMENTS30_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + PRICES_AGREEMENTS30_PATH);
    public static final Uri AGENTS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + AGENTS_PATH);
    public static final Uri CURATORS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + CURATORS_PATH);
    public static final Uri CURATORS_LIST_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + CURATORS_LIST_PATH);
    public static final Uri DISTR_POINTS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISTR_POINTS_PATH);
    public static final Uri DISTR_POINTS_LIST_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + DISTR_POINTS_LIST_PATH);
    public static final Uri ORGANIZATIONS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + ORGANIZATIONS_PATH);
    public static final Uri CLIENTS_WITH_SALDO_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + CLIENTS_WITH_SALDO_PATH);
    public static final Uri ORDERS_LINES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + ORDERS_LINES_PATH);
    public static final Uri REFUNDS_LINES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + REFUNDS_LINES_PATH);
    public static final Uri ORDERS_JOURNAL_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + ORDERS_JOURNAL_PATH);
    public static final Uri CASH_PAYMENTS_JOURNAL_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + CASH_PAYMENTS_JOURNAL_PATH);    
    public static final Uri AGREEMENTS_LIST_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + AGREEMENTS_LIST_PATH);
    public static final Uri AGREEMENTS30_LIST_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + AGREEMENTS30_LIST_PATH);
    public static final Uri AGREEMENTS_LIST_WITH_SALDO_ONLY_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH);
    public static final Uri AGREEMENTS30_LIST_WITH_SALDO_ONLY_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH);
    public static final Uri ORDERS_LINES_COMPLEMENTED_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + ORDERS_LINES_COMPLEMENTED_PATH);
    public static final Uri REFUNDS_LINES_COMPLEMENTED_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + REFUNDS_LINES_COMPLEMENTED_PATH);
    public static final Uri SEANCES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SEANCES_PATH);
    public static final Uri SEANCES_INCOMING_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + SEANCES_INCOMING_PATH);
    public static final Uri SEANCES_OUTGOING_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SEANCES_OUTGOING_PATH);
    public static final Uri REINDEX_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + REINDEX_PATH);
    public static final Uri VACUUM_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + VACUUM_PATH);
    public static final Uri SORT_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SORT_PATH);
    public static final Uri VICARIOUS_POWER_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + VICARIOUS_POWER_PATH);
    public static final Uri MESSAGES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + MESSAGES_PATH);
    public static final Uri MESSAGES_LIST_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + MESSAGES_LIST_PATH);
    public static final Uri NOMENCLATURE_LIST_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + NOMENCLATURE_LIST_PATH);
    public static final Uri NOMENCLATURE_SURFING_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + NOMENCLATURE_SURFING_PATH);

    public static final Uri SIMPLE_DISCOUNTS_CONTENT_URI =  Uri.parse("content://"
  	      + AUTHORITY + "/" + SIMPLE_DISCOUNTS_PATH);
    
    public static final Uri SALES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SALES_PATH);

    public static final Uri RESTS_SALES_STUFF_CONTENT_URI = Uri.parse("content://"
    	    	      + AUTHORITY + "/" + RESTS_SALES_STUFF_PATH);
    
    public static final Uri DISCOUNTS_STUFF_MEGA_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISCOUNTS_STUFF_MEGA_PATH);
    
    public static final Uri DISCOUNTS_STUFF_SIMPLE_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISCOUNTS_STUFF_SIMPLE_PATH);
    
    public static final Uri DISCOUNTS_STUFF_OTHER_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + DISCOUNTS_STUFF_OTHER_PATH);
    
    public static final Uri PRICESV_MEGA_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + PRICESV_MEGA_PATH);
    
    public static final Uri PRICESV_OTHER_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + PRICESV_OTHER_PATH);
    
    public static final Uri CLIENTS_PRICE_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + CLIENTS_PRICE_PATH);
    
    public static final Uri CURATORS_PRICE_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + CURATORS_PRICE_PATH);
    
    public static final Uri SETTINGS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + SETTINGS_PATH);

    public static final Uri SALES_LOADED_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + SALES_LOADED_PATH);
    
    //public static final Uri SALES_LOADED_WITH_COMMON_GROUPS_CONTENT_URI = Uri.parse("content://"
  	//      + AUTHORITY + "/" + SALES_LOADED_WITH_COMMON_GROUPS_PATH);
    
    public static final Uri SALES_L2_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + SALES_L2_PATH);
    
    public static final Uri VERSIONS_SALES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + VERSIONS_SALES_PATH);

    public static final Uri NOMENCLATURE_HIERARCHY_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + NOMENCLATURE_HIERARCHY_PATH);
    
    public static final Uri CREATE_VIEWS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + CREATE_VIEWS_PATH);
    
    public static final Uri CREATE_SALES_L_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + CREATE_SALES_L_PATH);
    
    public static final Uri ORDERS_SILENT_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + ORDERS_SILENT_PATH);
    
    public static final Uri REFUNDS_SILENT_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + REFUNDS_SILENT_PATH);    

    public static final Uri PLACES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + PLACES_PATH);
    
    public static final Uri OCCUPIED_PLACES_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + OCCUPIED_PLACES_PATH);
    
    public static final Uri ORDERS_PLACES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + ORDERS_PLACES_PATH);
    
    public static final Uri ORDERS_PLACES_LIST_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + ORDERS_PLACES_LIST_PATH);
    
    public static final Uri GPS_COORD_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + GPS_COORD_PATH);
    

    public static final Uri DISTRIBS_CONTRACTS_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISTRIBS_CONTRACTS_PATH);
    
    public static final Uri DISTRIBS_CONTRACTS_LIST_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISTRIBS_CONTRACTS_LIST_PATH);
    
    public static final Uri DISTRIBS_LINES_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + DISTRIBS_LINES_PATH);
    
    public static final Uri DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + DISTRIBS_LINES_COMPLEMENTED_PATH);

    public static final Uri DISTRIBS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + DISTRIBS_PATH);
    
    public static final Uri EQUIPMENT_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + EQUIPMENT_PATH);

    public static final Uri EQUIPMENT_RESTS_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + EQUIPMENT_RESTS_PATH);
    
    public static final Uri EQUIPMENT_RESTS_LIST_CONTENT_URI = Uri.parse("content://"
  	      + AUTHORITY + "/" + EQUIPMENT_RESTS_LIST_PATH);
    
    public static final Uri MTRADE_LOG_CONTENT_URI = Uri.parse("content://"
    	      + AUTHORITY + "/" + MTRADE_LOG_PATH);

    public static final Uri PERMISSIONS_REQUESTS_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + PERMISSIONS_REQUESTS_PATH);

    public static final Uri ROUTES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTES_PATH);

    public static final Uri ROUTES_LINES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTES_LINES_PATH);

    public static final Uri ROUTES_DATES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTES_DATES_PATH);

    public static final Uri ROUTES_DATES_LIST_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + ROUTES_DATES_LIST_PATH);

    public static final Uri REAL_ROUTES_DATES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + REAL_ROUTES_DATES_PATH);

    public static final Uri REAL_ROUTES_LINES_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + REAL_ROUTES_LINES_PATH);

    private static final UriMatcher sUriMatcher;
 
    private static final int URI_ORDERS = 1;
    private static final int URI_ORDERS_ID = 2;
    private static final int URI_CLIENTS = 3;
    private static final int URI_CLIENTS_ID = 4;
    private static final int URI_NOMENCLATURE = 5;
    private static final int URI_NOMENCLATURE_ID = 6;
    private static final int URI_RESTS = 7;
    private static final int URI_RESTS_ID = 8;
    private static final int URI_SALDO = 9;
    private static final int URI_SALDO_ID = 10;
    private static final int URI_AGREEMENTS = 11;
    private static final int URI_AGREEMENTS_ID = 12;
    private static final int URI_VERSIONS = 13;
    private static final int URI_VERSIONS_ID = 14;
    private static final int URI_PRICETYPES = 15;
    private static final int URI_PRICETYPES_ID = 16;
    private static final int URI_STOCKS = 17;
    private static final int URI_STOCKS_ID = 18;
    private static final int URI_PRICES = 19;
    private static final int URI_PRICES_ID = 20;
    private static final int URI_AGENTS = 21;
    private static final int URI_AGENTS_ID = 22;
    private static final int URI_CURATORS = 23;
    private static final int URI_CURATORS_ID = 24;
    private static final int URI_DISTR_POINTS = 25;
    private static final int URI_DISTR_POINTS_ID = 26;
    private static final int URI_ORGANIZATIONS = 27;
    private static final int URI_ORGANIZATIONS_ID = 28;
    private static final int URI_CLIENTS_WITH_SALDO = 29;
    private static final int URI_ORDERS_LINES = 30;
    private static final int URI_ORDERS_JOURNAL = 31;
    private static final int URI_AGREEMENTS_LIST = 32;
    private static final int URI_ORDERS_LINES_COMPLEMENTED = 33;
    private static final int URI_SEANCES = 34;
    private static final int URI_SEANCES_INCOMING = 35;
    private static final int URI_SEANCES_OUTGOING = 36;
    private static final int URI_REINDEX = 37;
    private static final int URI_MESSAGES = 38;
    private static final int URI_MESSAGES_ID = 39;
    private static final int URI_NOMENCLATURE_LIST = 40;
    private static final int URI_NOMENCLATURE_SURFING = 41;
    private static final int URI_SALES = 50;
    private static final int URI_RESTS_SALES_STUFF = 51;
    
    private static final int URI_CLIENTS_PRICE = 52;
    private static final int URI_SETTINGS = 53;
    private static final int URI_SALES_LOADED = 54;
    private static final int URI_VERSIONS_SALES = 55;
    private static final int URI_NOMENCLATURE_HIERARCHY = 56;
    private static final int URI_CREATE_VIEWS = 57;
    private static final int URI_CREATE_SALES_L = 58;
    private static final int URI_MESSAGES_LIST = 59;
    private static final int URI_ORDERS_SILENT = 60;
    private static final int URI_ORDERS_SILENT_ID = 61;
    
    //private static final int URI_DISCOUNTS_STUFF = 54;
    
    private static final int URI_CURATORS_PRICE = 62;
    
    private static final int URI_SIMPLE_DISCOUNTS = 63;
    private static final int URI_SIMPLE_DISCOUNTS_ID = 64;
    
    private static final int URI_CASH_PAYMENTS = 65;
    private static final int URI_CASH_PAYMENTS_ID = 66;
    private static final int URI_CASH_PAYMENTS_JOURNAL = 67;
    
    private static final int URI_SALDO_EXTENDED = 90;
    private static final int URI_SALDO_EXTENDED_ID = 91;
    private static final int URI_SALDO_EXTENDED_JOURNAL = 92;
    
    private static final int URI_CURATORS_LIST = 93;
    
    private static final int URI_PLACES = 94;
    private static final int URI_OCCUPIED_PLACES = 95;
    private static final int URI_ORDERS_PLACES = 96;
    private static final int URI_ORDERS_PLACES_LIST = 97;
    
    private static final int URI_REFUNDS = 100;
    private static final int URI_REFUNDS_ID = 101;
    private static final int URI_DISTRIBS_ID = 102;
    
    private static final int URI_JOURNAL = 103;
    private static final int URI_JOURNAL_ID = 104;
    
    private static final int URI_REFUNDS_LINES = 105;
    private static final int URI_REFUNDS_LINES_COMPLEMENTED = 106;
    
    private static final int URI_REFUNDS_SILENT = 107;
    private static final int URI_REFUNDS_SILENT_ID = 108;
    
    private static final int URI_SALES_L2 = 109;
    
    private static final int URI_SORT = 111;
    
    private static final int URI_VICARIOUS_POWER = 112;
    private static final int URI_VICARIOUS_POWER_ID = 113;
    
    private static final int URI_GPS_COORD = 114;
    
    private static final int URI_AGREEMENTS_WITH_SALDO_ONLY_LIST = 115;
    
    
    private static final int URI_DISTRIBS_CONTRACTS = 120;
    private static final int URI_DISTRIBS_CONTRACTS_LIST = 121;
    private static final int URI_DISTRIBS_LINES = 122;
    private static final int URI_DISTRIBS_LINES_COMPLEMENTED = 123;
    private static final int URI_DISTRIBS = 124;
    
    private static final int URI_EQUIPMENT = 125;
    private static final int URI_EQUIPMENT_RESTS = 126;
    private static final int URI_EQUIPMENT_RESTS_LIST = 127;
    
    private static final int URI_DISTR_POINTS_LIST = 128;
    
    private static final int URI_PRICESV_MEGA = 129;
    private static final int URI_PRICESV_OTHER = 130;
    
    private static final int URI_DISCOUNTS_STUFF_MEGA = 131;
    private static final int URI_DISCOUNTS_STUFF_SIMPLE = 132;
    private static final int URI_DISCOUNTS_STUFF_OTHER = 133;
    
    private static final int URI_MTRADE_LOG = 134;
    private static final int URI_PERMISSIONS_REQUESTS = 135;

    private static final int URI_AGREEMENTS30 = 136;
    private static final int URI_AGREEMENTS30_ID = 137;
    private static final int URI_AGREEMENTS30_LIST = 138;
    private static final int URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST = 139;
    private static final int URI_PRICES_AGREEMENTS30 = 140;

    private static final int URI_ROUTES = 150;
    private static final int URI_ROUTES_LINES = 151;
    private static final int URI_ROUTES_DATES = 152;
    private static final int URI_ROUTES_DATES_ID = 153;
    private static final int URI_ROUTES_DATES_LIST = 154;
    private static final int URI_ROUTES_DATES_LIST_ID = 155;

    private static final int URI_REAL_ROUTES_DATES = 156;
    private static final int URI_REAL_ROUTES_DATES_ID = 1157;
    private static final int URI_REAL_ROUTES_LINES = 158;

    private static final int URI_VACUUM = 159;

    private static HashMap<String, String> ordersProjectionMap;
    private static HashMap<String, String> cashPaymentsProjectionMap;
    private static HashMap<String, String> refundsProjectionMap;
    private static HashMap<String, String> journalProjectionMap;
    private static HashMap<String, String> clientsProjectionMap;
    private static HashMap<String, String> nomenclatureProjectionMap;
    private static HashMap<String, String> nomenclatureHierarchyProjectionMap;
    private static HashMap<String, String> restsProjectionMap;
    private static HashMap<String, String> saldoProjectionMap;
    private static HashMap<String, String> saldoExtendedProjectionMap;
    private static HashMap<String, String> saldoExtendedJournalProjectionMap;
    private static HashMap<String, String> agreementsProjectionMap;
    private static HashMap<String, String> agreements30ProjectionMap;
    private static HashMap<String, String> versionsProjectionMap;
    private static HashMap<String, String> pricetypesProjectionMap;
    private static HashMap<String, String> stocksProjectionMap;
    private static HashMap<String, String> pricesProjectionMap;
    private static HashMap<String, String> pricesAgreements30ProjectionMap;
    private static HashMap<String, String> agentsProjectionMap;
    private static HashMap<String, String> curatorsProjectionMap;
    private static HashMap<String, String> curatorsListProjectionMap;
    private static HashMap<String, String> distrPointsProjectionMap;
    private static HashMap<String, String> distrPointsListProjectionMap;
    private static HashMap<String, String> organizationsProjectionMap;
    private static HashMap<String, String> clientsWithSaldoProjectionMap;
    private static HashMap<String, String> ordersLinesProjectionMap;
    private static HashMap<String, String> refundsLinesProjectionMap;
    private static HashMap<String, String> ordersJournalProjectionMap;
    private static HashMap<String, String> cashPaymentsJournalProjectionMap;
    private static HashMap<String, String> agreementsListProjectionMap;
    private static HashMap<String, String> agreements30ListProjectionMap;
    private static HashMap<String, String> agreementsListWithSaldoOnlyProjectionMap;
    private static HashMap<String, String> agreements30ListWithSaldoOnlyProjectionMap;
    private static HashMap<String, String> ordersLinesComplementedProjectionMap;
    private static HashMap<String, String> refundsLinesComplementedProjectionMap;
    private static HashMap<String, String> seancesProjectionMap;
    private static HashMap<String, String> vicariousPowerProjectionMap;
    private static HashMap<String, String> messagesProjectionMap;
    private static HashMap<String, String> messagesListProjectionMap;
    private static HashMap<String, String> nomenclatureListProjectionMap;
    private static HashMap<String, String> nomenclatureSurfingProjectionMap;
    private static HashMap<String, String> clientsPriceProjectionMap;
    private static HashMap<String, String> curatorsPriceProjectionMap;
    private static HashMap<String, String> simpleDiscountsProjectionMap;
    private static HashMap<String, String> settingsProjectionMap;
    private static HashMap<String, String> salesLoadedProjectionMap;
    //private static HashMap<String, String> salesLoadedWithCommonGroupsProjectionMap;
    private static HashMap<String, String> salesL2ProjectionMap;
    private static HashMap<String, String> versionsSalesProjectionMap;
    private static HashMap<String, String> hierarchyProjectionMap;
    
    private static HashMap<String, String> placesProjectionMap;
    private static HashMap<String, String> occupiedPlacesProjectionMap;
    private static HashMap<String, String> ordersPlacesProjectionMap;
    private static HashMap<String, String> ordersPlacesListProjectionMap;
    
    private static HashMap<String, String> gpsCoordProjectionMap;
    
    private static HashMap<String, String> distribsContractsProjectionMap;
    private static HashMap<String, String> distribsContractsListProjectionMap;
    private static HashMap<String, String> distribsLinesProjectionMap;
    private static HashMap<String, String> distribsLinesComplementedProjectionMap;
    private static HashMap<String, String> distribsProjectionMap;
    
    private static HashMap<String, String> equipmentProjectionMap;
    private static HashMap<String, String> equipmentRestsProjectionMap;
    private static HashMap<String, String> equipmentRestsListProjectionMap;
    
    private static HashMap<String, String> mtradeLogProjectionMap;
    private static HashMap<String, String> permissionsRequestsProjectionMap;

    private static HashMap<String, String> routesProjectionMap;
    private static HashMap<String, String> routesLinesProjectionMap;
    private static HashMap<String, String> routesDatesProjectionMap;
    private static HashMap<String, String> routesDatesListProjectionMap;

    private static HashMap<String, String> realRoutesDatesProjectionMap;
    private static HashMap<String, String> realRoutesLinesProjectionMap;



    /**
     * This reads a file from the given Resource-Id and calls every line of it as a SQL-Statement
     *
     * @param context
     *
     * @param resourceId
     *  e.g. R.raw.food_db
     *
     * @return Number of SQL-Statements run
     * @throws IOException
     */
    public int insertFromFile(Context context, SQLiteDatabase db, int resourceId) throws IOException {
        // Reseting Counter
        int result = 0;

        // Open the resource
        InputStream insertsStream = context.getResources().openRawResource(resourceId);
        BufferedReader insertReader = new BufferedReader(new InputStreamReader(insertsStream));

        // Iterate through lines (assuming each insert has its own line and theres no other stuff)
        while (insertReader.ready()) {
            String insertStmt = insertReader.readLine();
            db.execSQL(insertStmt);
            result++;
        }
        insertReader.close();

        // returning number of inserted rows
        return result;
    }

	@SuppressLint("Override")
	public class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context)
		{
		    //super(context, Environment.getExternalStorageDirectory()+"/"+DB_NAME, null, DB_VERSION);
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			db.execSQL("PRAGMA encoding = \"UTF-8\"");
			// insert into xxx
			// select * from yyyy;
			// create table xxx
			// as select * from yyy
		}
		
		    public void onCreate(SQLiteDatabase db) {
                Log.d(LOG_TAG, " --- onCreate database --- ");
                db.execSQL("PRAGMA encoding = \"UTF-8\"");
                /*
                try {
                    int insertCount = insertFromFile(getContext(), db, R.raw.backup_dch);
                    //Toast.makeText(this, "Rows loaded from file= " + insertCount, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                 */


		      db.execSQL("create table messages (" +
		    		"_id integer primary key autoincrement," +
		    		"uid text,"+
		    		"sender_id text,"+
		    		"receiver_id text,"+
		    		"text text,"+
		    		"fname text,"+
		    		"datetime text,"+
		    		"acknowledged int,"+
		    		"ver int,"+
		    		
		    		// новые
		    		"type_idx int,"+
		    		"date1 text,"+
		    		"date2 text,"+
		    		"client_id text,"+
		    		"agreement_id text,"+
		    		"nomenclature_id text,"+
		    		"report text,"+
		    		//
		    		"isMark int default 0,"+
		    		"isMarkCnt int default 0,"+
		    		//
		    		"UNIQUE('uid')"+
		    		");");
		      
		      db.execSQL("create table seances (" +
		    		"incoming text null,"+
		    		"outgoing text null"+
		    		");");
		      
		      db.execSQL("insert into seances(incoming) values ('CREATED')");
		      
		      db.execSQL("create table versions (" +
		    		"param text,"+
		    		"ver int,"+
		    		"UNIQUE ('param')" +
		    		");");
		      
		      db.execSQL("create table sales_versions (" +
		    		"param text,"+
		    		"ver int,"+
		    		"UNIQUE ('param')" +
		    		");");
		      
		      db.execSQL("create table clients (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"isFolder int," +
		    		"parent_id text," +
					"parent_id_0 text," +
					"parent_id_1 text," +
					"parent_id_2 text," +
					"parent_id_3 text," +
					"parent_id_4 text," +
		    		"code text," +
		    		"descr text," +
		    		"descr_lower text," +
		    		"descrFull text," +
		    		"address text," +
		    		"address2 text," +
		    		"comment text," +
		    		"curator_id text," +
		    		"priceType text," +
		    		"blocked int," +
		    		"flags int,"+
		    		"card_num text," +
		    		"phone_num text," +
                    "email_for_cheques text," +
		    		"isUsed int default 0,"+
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("create table organizations (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"code text," +
		    		"descr text," +
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("create table agreements (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"owner_id text," +
		    		"organization_id text," +
		    		"code text," +
		    		"descr text," +
		    		"price_type_id text," +
		    		"default_manager_id text," +
		    		"sale_id text," +
		    		"kredit_days int," +
		    		"kredit_sum double," +
		    		"flags int," +
		    		"UNIQUE ('id')" +
		    		");");

                db.execSQL("create table agreements30 (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "owner_id text," +
                        "organization_id text," +
                        "code text," +
                        "descr text," +
                        "price_type_id text," +
                        "default_manager_id text," +
                        "sale_id text," +
                        "kredit_days int," +
                        "kredit_sum double," +
                        "flags int," +
                        "UNIQUE ('id', 'owner_id')" +
                        ");");

                db.execSQL("create table prices_agreements30 (" +
                        "_id integer primary key autoincrement, " +
                        "agreement30_id text," +
                        "nomenclature_id text," +
                        "pack_id text," + // Пока не используется
                        "ed_izm_id text," +
                        "edIzm text," +
                        "price double," +
                        "k double," +
                        "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                        ");");

		      
		      db.execSQL("create table pricetypes (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"isFolder text," + // это баг, тип строка а не число, да и parent_id в справочнике нет
		    		"code text," +
		    		"descr text," +
		    		"isUsed int default 0,"+
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("create table clients_price (" +
		    		"_id integer primary key autoincrement, " +
		    		"client_id text," +
		    		"nomenclature_id text," +
		    		"priceProcent double," +
		    		"priceAdd double," +
		    		"UNIQUE ('client_id', 'nomenclature_id')" +
		    		");");
		      
		      db.execSQL("create table curators_price (" +
		    		"_id integer primary key autoincrement, " +
		    		"curator_id text," +
		    		"nomenclature_id text," +
		    		"priceProcent double," +
		    		"priceAdd double," +
		    		"UNIQUE ('curator_id', 'nomenclature_id')" +
		    		");");
		      
		      db.execSQL("create table simple_discounts (" +
		     		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"code text," +
		    		"descr text," +
		    		"priceProcent double," +
		    		"isUsed int default 0,"+
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("create table stocks (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"isFolder text," +
		    		"code text," +
		    		"descr text," +
		    		"flags int," +
		    		"UNIQUE ('id')" +
		    		");");

		      db.execSQL("CREATE TABLE Nomenclature (" +
		    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
		    		"id TEXT," +
		    		"isFolder INT," +
		    		"parent_id text," +
					"parent_id_0 text," +
					"parent_id_1 text," +
					"parent_id_2 text," +
					"parent_id_3 text," +
					"parent_id_4 text," +
		    		"code TEXT," +
		    		"descr TEXT," +
		    		"descr_lower text," +
		    		"descrFull TEXT," +
		    		"quant_1 TEXT," +
		    		"quant_2 TEXT," +
		    		"edizm_1_id TEXT," +
		    		"edizm_2_id TEXT," +
		    		"quant_k_1 DOUBLE," +
		    		"quant_k_2 DOUBLE," +
		    		"opt_price DOUBLE," +
		    		"m_opt_price DOUBLE," +
		    		"rozn_price DOUBLE," +
		    		"incom_price DOUBLE," +
		    		"IsInPrice INT," +
		    		"flagWithoutDiscont INT," +
		    		"weight_k_1 DOUBLE," +
		    		"weight_k_2 DOUBLE," +
		    		"min_quantity DOUBLE," + // минимальное продаваемое количество за 1 раз
		    		"multiplicity DOUBLE," + // кратность количества (нельзя дробить)
		    		"required_sales DOUBLE," + // план продажи за месяц по каждому клиенту
		    		"flags INT," +
		    		"image_file text," +
		    		"image_file_checksum INT," + // не используется, т.к. для расчета требуется считывать файл
		    		"order_for_sorting INT," +
		    		"group_of_analogs INT," +
		    		"nomenclature_color int default 0,"+
                    "image_width INT,"+
                    "image_height INT,"+
                    "image_file_size INT,"+
                    "compose_with text," +
		    		"isUsed int default 0,"+
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("CREATE TABLE Rests (" +
		    		"stock_id TEXT,"+
		    		"nomenclature_id TEXT,"+
                    "organization_id TEXT default '',"+
		    		"quantity DOUBLE,"+
		    		"quantity_reserve DOUBLE,"+
		    		"isUsed int default 0,"+
		    		"UNIQUE ('stock_id', 'nomenclature_id','organization_id')"+
		    		");");
		      
		      db.execSQL("create table prices (" +
		    		"_id integer primary key autoincrement, " +
		    		"nomenclature_id text," +
		    		"price_type_id text," +
		    		"ed_izm_id text," +
		    		"edIzm text," +
		    		"price double," +
		    		"priceProcent double," + // скорее всего это лишнее поле
		    		"k double," +
		    		"isUsed int default 0,"+
		    		"UNIQUE ('nomenclature_id','price_type_id')" +
		    		");");
		      
		      
		      db.execSQL("create table agents (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"code text," +
		    		"descr text," +
		    		"UNIQUE ('id')" +
		    		");");

		      
		      db.execSQL("create table curators (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"parent_id text," +
		    		"isFolder int," +
		    		"code text," +
		    		"descr text," +
		    		"UNIQUE ('id')" +
		    		");");
		      
		      db.execSQL("create table distr_points (" +
		    		"_id integer primary key autoincrement, " +
		    		"id text," +
		    		"owner_id text," +
		    		"code text," +
		    		"descr text," +
		    		"address text," +
		    		"phones text," +
		    		"contacts text," +
		    		"price_type_id text," +
		    		"UNIQUE (id, owner_id)" +
		    		");");
		      
		      db.execSQL("CREATE TABLE Saldo (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    		"client_id TEXT,"+
			    		"saldo DOUBLE,"+
			    		"saldo_past DOUBLE,"+
			    		"saldo_past30 DOUBLE default 0,"+
			    		"isUsed int default 0,"+
			    		"UNIQUE ('client_id')"+
			    		");");
		      
		      db.execSQL("CREATE TABLE saldo_extended (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    		"client_id TEXT,"+
                        "agreement30_id TEXT,"+
			    		"agreement_id TEXT,"+
			    		"document_id TEXT,"+
			    		"manager_id TEXT,"+
			    		"document_descr TEXT,"+
			    		"document_datetime TEXT,"+
			    		"manager_descr TEXT,"+
			    		"agreement_descr TEXT,"+
			    		"organization_descr TEXT,"+
			    		"saldo DOUBLE,"+
			    		"saldo_past DOUBLE,"+
			    		"isUsed int default 0,"+
			    		"UNIQUE ('client_id','agreement30_id','agreement_id','document_id','manager_id')"+
			    		");");
		      
		      db.execSQL("create table ordersLines (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "order_id integer," + // внутренний код
		    		  "nomenclature_id text," +
		    		  "client_id text," +
		    		  "quantity_requested double," +
		    		  "quantity double," +
		    		  "price double," +
		    		  "total double," +
		    		  "discount DOUBLE,"+
		    		  "k double," +
		    		  "ed text," +
		    		  "shipping_time text," +
		    		  "comment_in_line text," +
		    		  // 0-строка не менялась
		    		  // 1-добавленные строки
		    		  // 2-строка удалена
		    		  // 3-строка изменена
		    		  //"editing_state int," +
		    		  // для состояния 3 - _id измененной строки
		    		  //"old_id int" +
		    		  "lineno int" +
		    		  ");");
		      
		      db.execSQL("create table orders (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "uid text," +
		    		  "version int," +
		    		  "version_ack int," +
		    		  "versionPDA int," +
		    		  "versionPDA_ack int," +
		    		  "id text," +
		    		  "numdoc text," +
		    		  "datedoc text," +
		    		  "client_id text," +
                      "agreement30_id text," +
		    		  "agreement_id text," +
		    		  "distr_point_id text," +
		    		  "comment text," +
		    		  "comment_closing text," +
		    		  "comment_payment text," +
		    		  "closed_not_full int," +
		    		  "state int," +
		    		  "curator_id text," +
		    		  "bw int," +
		    		  "trade_type text," +
		    		  "datecreation text," +
		    		  "datecoord text," +
		    		  "latitude double," +
		    		  "longitude double," +
		    		  "gpsstate int," +
		    		  "gpsaccuracy float," +
                      "gpstype int," +
		    		  "accept_coord int," +
		    		  "dont_need_send int," +
		    		  "price_type_id text," +
		    		  "stock_id text," +
		    		  "sum_doc double," +
		    		  "weight_doc double," +
		    		  "shipping_type int," +
		    		  "shipping_time int," +
		    		  "shipping_begin_time text," +
		    		  "shipping_end_time text," +
		    		  "shipping_date text," +
		    		  
					  "simple_discount_id text," +
		    		  "create_client int," +
		    		  "create_client_descr text," +
		    		  "create_client_surname text," +
		    		  "create_client_firstname text," +
		    		  "create_client_lastname text," +
		    		  "create_client_phone text," +
		    		  "place_num int," +
		    		  "card_num int," +
		    		  "pay_credit double," +
		    		  "ticket_m double," +
		    		  "ticket_w double," +
		    		  "quant_m int," +
		    		  "quant_w int," +
		    		  "quant_mw int," +
					  "manager_comment text," +
					  "theme_comment text," +
					  "phone_num text," +
					  "sum_shipping double," +
					  // 0-документ в нормальном состоянии
					  // 1-новый документ, записать не успели
					  // 2-документ начали редактировать, но не записали и не отменили изменения
					  "editing_backup int," +
		    		  // для состояния 2 - _id редактируемого документа
					  // в других случаях - не важно
		    		  "old_id int" +
					  // До 19.02.2018 editing_backup тут не было
		    		  //"UNIQUE ('uid','id')" +
		    		  ");");
		      // С 19.02.2018
		      db.execSQL("CREATE UNIQUE INDEX OrdersUniqueIndex ON orders ('uid','id','editing_backup')");
		      
		      db.execSQL("create table ordersPlaces (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "order_id integer," + // внутренний код
		    		  "place_id text" +
		    		  ");");
		      
		      db.execSQL("create table cash_payments (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "uid text," +
		    		  "version int," +
		    		  "version_ack int," +
		    		  "versionPDA int," +
		    		  "versionPDA_ack int," +
		    		  "id text," +
		    		  "numdoc text," +
		    		  "datedoc text," +
		    		  "client_id text," +
		    		  "agreement_id text," +
		    		  "comment text," +
		    		  "comment_closing text," +
		    		  "state int," +
		    		  "curator_id text," +
		    		  "manager_descr text," +
		    		  "organization_descr text," +
		    		  "sum_doc double," +
		    		  "vicarious_power_id text, "+
		    		  "vicarious_power_descr text, "+
                      "datecreation text," +
                      "datecoord text," +
                      "latitude double," +
                      "longitude double," +
                      "gpsstate int," +
                      "gpsaccuracy float," +
                      "gpstype int," +
                      "accept_coord int," +
                      "distr_point_id text," + // это только для того, чтобы документ привязался к маршруту
                      "UNIQUE ('uid','id')" +
		    		  ");");
		      
		      
		        db.execSQL("create table vicarious_power (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "id text," +
		    		  "descr text," +
		    		  "numdoc text," +
		    		  "datedoc text," +
		    		  "date_action text," +
		    		  "comment text," +
		    		  "client_id text," +
		    		  "client_descr text," +
		    		  "agreement_id text," +
		    		  "agreement_descr text," +
		    		  "fio_descr text," +
		    		  "manager_id text," +
		    		  "manager_descr text," +
		    		  "organization_id text," +
		    		  "organization_descr text," +
		    		  "state text," +
		    		  "sum_doc double," +
		    		  "UNIQUE ('id')" +
		    		  ");");
		      
		      db.execSQL("create table refundsLines (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "refund_id integer," + // внутренний код
		    		  "nomenclature_id text," +
		    		  "client_id text," +
		    		  "quantity_requested double," +
		    		  "quantity double," +
		    		  //"price double," +
		    		  //"total double," +
		    		  //"discount DOUBLE,"+
		    		  "k double," +
		    		  "ed text," +
		    		  "comment_in_line text" +
		    		  ");");
		      
		      db.execSQL("create table refunds (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "uid text," +
		    		  "version int," +
		    		  "version_ack int," +
		    		  "versionPDA int," +
		    		  "versionPDA_ack int," +
		    		  "base_doc_id text," +
		    		  "id text," +
		    		  "numdoc text," +
		    		  "datedoc text," +
		    		  "client_id text," +
		    		  "agreement_id text," +
		    		  "distr_point_id text," +
		    		  "comment text," +
		    		  "comment_closing text," +
		    		  "closed_not_full int," +
		    		  "state int," +
		    		  "curator_id text," +
		    		  "bw int," +
		    		  "trade_type text," +
		    		  "datecreation text," +
		    		  "datecoord text," +
		    		  "latitude double," +
		    		  "longitude double," +
		    		  "gpsstate float," +
		    		  "gpsaccuracy float," +
		    		  "accept_coord int," +
		    		  "dont_need_send int," +
		    		  "price_type_id text," +
		    		  "stock_id text," +
		    		  "sum_doc double," +
		    		  "weight_doc double," +
					  "simple_discount_id text," +
                      // 20.05.2020
                      "shipping_type int," +
                      //
		    		  "UNIQUE ('uid','id')" +
		    		  ");");
		      
		      db.execSQL("CREATE TABLE distribsContracts (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    		"id TEXT," +
			    		"position int," +
			    		"code TEXT," +
			    		"descr TEXT," +
			    		"UNIQUE ('id')" +
			    		");");
  		
		      db.execSQL("create table distribsLines (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "distribs_id integer," + // внутренний код
		    		  "contract_id text," +
		    		  "quantity double" +
		    		  ");");
		      
		      db.execSQL("create table distribs (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "uid text," +
		    		  "version int," +
		    		  "version_ack int," +
		    		  "versionPDA int," +
		    		  "versionPDA_ack int," +
		    		  "id text," +
		    		  "numdoc text," +
		    		  "datedoc text," +
		    		  "client_id text," +
		    		  "curator_id text," +
		    		  "distr_point_id text," +
		    		  "state int," +
		    		  "comment text," +
		    		  "datecoord text," +
		    		  "latitude double," +
		    		  "longitude double," +
		    		  "gpsstate int," +
		    		  "gpsaccuracy float," +
                      "gpstype int," +
                      "accept_coord int," +
		    		  "UNIQUE ('uid','id')" +
		    		  ");");
		      
		      
		      /*
		      db.execSQL("create table debt_ext (" +
		    		  "_id integer primary key autoincrement, " +
		    		  "client_id text," +
		    		  "agreement_id text," +
		    		  "curator_id text," +
		    		  "document text," +
		    		  "plan_pay_date text," +
		    		  "isExpanded int,"+ // 0 - если общий долг, 1,2,3 и т.д - детальные записи
		    		  "sum_debt double," +
		    		  "UNIQUE ('client_id','agreement_id','curator_id','isExpanded')" +
		    		  ");");
		     */
		      
		      /*
		      cv.clear();
		      cv.put("numdoc", "0001");
		      cv.put("datedoc", "20130101");
		      db.insert("orders", null, cv);
		      
		      cv.clear();
		      cv.put("descr", "Иванов И.И.");
		      cv.put("address", "Нулевой километр");
		      db.insert("clients", null, cv);
		      
		      cv.clear();
		      cv.put("descr", "Петров П.П.");
		      cv.put("address", "Адрес неизвестен");
		      db.insert("clients", null, cv);
		      */
			  
		      db.execSQL("create table settings ("+
		    		  "_id integer primary key autoincrement, " +
		    		  "fmt text null, "+
		    		  "ticket_m double null,"+
		    		  "ticket_w double null,"+
		    		  "agent_id text null,"+
		    		  "gps_interval ind default 0,"+
                      "agent_price_type_id text null"+
		    		  ");");
		      
		      db.execSQL("create table salesloaded ("+
		    		  "_id integer primary key autoincrement, " +
		    		  "ver int,"+
		    		  "datedoc text,"+
		    		  "numdoc text,"+
		    		  "refdoc text,"+
		    		  "client_id text,"+
		    		  "curator_id text,"+
		    		  "distr_point_id text,"+
		    		  "nomenclature_id text,"+
		    		  "quantity double,"+
		    		  // здесь стоимость, а не цена
		    		  "price double,"+
		    		  "UNIQUE ('nomenclature_id', 'refdoc')" +
		    		  ");");
		      
			    db.execSQL("create table nomenclature_hierarchy (" +
			    		"id text,"+
			    		"ord_idx int,"+
			    		"groupDescr text,"+
			    		"level int,"+
			    		"level0_id text,"+
			    		"level1_id text,"+
			    		"level2_id text,"+
			    		"level3_id text,"+
			    		"level4_id text,"+
			    		"level5_id text,"+
			    		"level6_id text,"+
			    		"level7_id text,"+
			    		"level8_id text,"+
                        "dont_use_in_hierarchy int,"+
			    		"UNIQUE ('id')"+
			    		");");
			    
			    db.execSQL("create table salesL (" +
			    		"nomenclature_id text," +
			    		"client_id text," +
                        "distr_point_id text," +
			    		// данные, загруженные из 1С (предыдущие периоды)
			    		"strQuantity text," + // продажи по датам N+N+N+N+
			    		"quantity double," +  // общее количество продаж
			    		// данные в телефоне (текущий период, обновляются при записи или удалении заказа, отбор по клиенту)
			    		//"quantity_now double," +
			    		"UNIQUE (nomenclature_id, client_id)" +
			    		");");
			    
			    db.execSQL("create table salesL2 (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			    		// здесь номенклатура или группа (в зависимости от настроек)
			    		"nomenclature_id text," +
			    		"client_id text," +
                        "distr_point_id text," +
			    		"datedoc text," +
			    		"numdoc text," +
			    		"quantity double," +
			    		// здесь стоимость, а не цена
			    		"price double" +
			    		");");

		        db.execSQL("CREATE TABLE places (" +
		        		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			    		"id TEXT,"+
			    		"code TEXT," +
			    		"descr TEXT," +
			    		"isUsed int default 0,"+
			    		"UNIQUE ('id')"+
			    		");");
			    
		        db.execSQL("CREATE TABLE occupied_places (" +
			    		"place_id TEXT,"+
			    		"client_id TEXT,"+
			    		"document_id TEXT,"+
			    		"datedoc TEXT,"+
			    		"document TEXT,"+
			    		"shipping_time TEXT,"+
			    		"shipping_date TEXT,"+
			    		"isUsed int default 0,"+
			    		"UNIQUE ('place_id', 'client_id','document_id')"+
			    		");");
		        
		        db.execSQL("CREATE TABLE journal (" +
			    		"_id integer primary key autoincrement, " +
		        		// 0-order
			    		// 1-refund
		        		// 2-payment (но их в журнале сейчас нет, они отдельно)
			    		// 3-distribs
		        		"iddocdef int,"+
		        		"order_id INT,"+
		        		"payment_id INT,"+
		        		"refund_id INT,"+
		        		"distribs_id INT,"+
			    		"uid text," +
			    		"id text," +
			    		"numdoc text," +
			    		"datedoc text," +
			    		"shipping_date text," +
			    		"client_id text," +
			    		"use_client_descr int,"+
			    		"client_descr text," +
			    		"state int," +
			    		"sum_doc double," +
			    		"sum_shipping double default -1," +
			    		"color text," +
			    		"isUsed int default 0,"+
		    		    "UNIQUE ('uid','id')," +
		    		    "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
			    		");");
		        
				//db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");				
		        
				db.execSQL("CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
				        "FOR EACH ROW BEGIN " +
				        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +
				//      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
				        "DELETE FROM journal WHERE order_id=OLD._id; " +
				        "END;");
				
				//db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
				//      "FOR EACH ROW BEGIN " +
				//      "DELETE FROM journal WHERE payment_id=OLD._id; " +
				//      "END;");
				
				db.execSQL("CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
				        "FOR EACH ROW BEGIN " +
				        "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
				        "DELETE FROM journal WHERE refund_id=OLD._id; " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
				        "FOR EACH ROW BEGIN " +
				        "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
				        "DELETE FROM journal WHERE distribs_id=OLD._id; " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
				        "WHEN new.editing_backup=0 BEGIN " +
				        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
				        "FOR EACH ROW BEGIN " +
				        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color="+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" where order_id=new._id; " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
				        "FOR EACH ROW BEGIN " +
				        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, "+JOURNAL_REFUND_COLOR_FOR_TRIGGER+"); " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
				        "FOR EACH ROW BEGIN " +
				        "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color="+JOURNAL_REFUND_COLOR_FOR_TRIGGER+" where refund_id=new._id; " +
				        "END;");
				
				
				db.execSQL("CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
				        "FOR EACH ROW BEGIN " +
				        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
				        " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, "+JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER+"); " +
				        "END;");
				
				db.execSQL("CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
				        "FOR EACH ROW BEGIN " +
				        "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color="+JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER+" where distribs_id=new._id; " +
				        "END;");
				
	    		db.execSQL("create table gps_coord (" +
			    		"datecoord text," +
			    		"latitude double," +
			    		"longitude double," +
			    		"gpsstate int," +
                        "gpstype int," +
			    		"gpsaccuracy float," +
			    		"version int" +
			    		");");
	    		
		        db.execSQL("CREATE TABLE equipment (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
			    		"id TEXT," +
			    		"code TEXT," +
			    		"descr TEXT," +
			    		"flags INT," +
			    		"UNIQUE ('id')" +
			    		");");
			      
		        db.execSQL("CREATE TABLE equipment_rests (" +
		        		"_id integer primary key autoincrement,"+
			    		"client_id TEXT,"+
			    		"agreement_id TEXT,"+
			    		"nomenclature_id TEXT,"+
			    		"distr_point_id TEXT,"+
			    		"quantity DOUBLE,"+
			    		"sum DOUBLE,"+
			    		"flags INT,"+
			    		"doc_id TEXT,"+
			    		"doc_descr TEXT,"+
			    		"date TEXT,"+
			    		"datepast TEXT,"+
			    		"UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')"+
			    		");");
	    		
		        db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);");

                db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));");

                db.execSQL("create table routes (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "code text," +
                        "descr text," +
                        "manager_id text," +
                        "UNIQUE ('id')"+
                        ");");

                db.execSQL("create table routes_lines (" +
                        "_id integer primary key autoincrement, " +
                        "route_id text," +
                        "lineno int," +
                        "distr_point_id text," +
                        "visit_time text," +
                        "UNIQUE ('route_id', 'lineno')"+
                        ");");

                db.execSQL("create table routes_dates (" +
                        "_id integer primary key autoincrement, " +
                        "route_date text," +
                        "route_id text," +
                        "UNIQUE ('route_date')"+
                        ");");

                //
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
                        "required_visit_time text," +
                        "start_visit_time text," +
                        "end_visit_time text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "gpsstate int," +
                        "gpsaccuracy float," +
                        "accept_coord int," +
                        "UNIQUE ('real_route_id', 'lineno')"+
                        ");");

            }

		    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		    {
		    	
		    	// есть еще onDowngrade
		    	Log.d(LOG_TAG, " --- onUpgrade database --- ");
		    	db.execSQL("delete from seances");
		    	
		    	if (oldVersion<2)
		    	{
			      db.execSQL("create table settings ("+
			    		  "_id integer primary key autoincrement, " +
			    		  "fmt text null"+
			    		  ");");
			      db.execSQL("create table salesloaded ("+
			    		  "_id integer primary key autoincrement, " +
			    		  "ver int,"+
			    		  "datedoc text,"+
			    		  "numdoc text,"+
			    		  "refdoc text,"+
			    		  "curator_id text,"+
			    		  "distr_point_id text,"+
			    		  "nomenclature_id text,"+
			    		  "quantity double,"+
			    		  // здесь стоимость, а не цена
			    		  "price double"+
			    		  ");");
			      db.execSQL("alter table clients add column flags int");
		    	}
		    	if (oldVersion<3)
		    	{
				      db.execSQL("create table sales_versions (" +
					    		"param text,"+
					    		"ver int,"+
					    		"UNIQUE ('param')" +
					    		");");
		    	}
		    	if (oldVersion<4)
		    	{
				      db.execSQL("alter table salesloaded add column client_id text");
		    	}
		    	
		    	if (oldVersion<8)
		    	{
		    		db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature");
		    		db.execSQL("DROP TABLE nomenclature");
				    db.execSQL("CREATE TABLE Nomenclature (" +
					    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					    		"id TEXT," +
					    		"isFolder INT," +
					    		"parent_id text," +
					    		"code TEXT," +
					    		"descr TEXT," +
					    		"descrFull TEXT," +
					    		"quant_1 TEXT," +
					    		"quant_2 TEXT," +
					    		"edizm_1_id TEXT," +
					    		"edizm_2_id TEXT," +
					    		"quant_k_1 DOUBLE," +
					    		"quant_k_2 DOUBLE," +
					    		"opt_price DOUBLE," +
					    		"m_opt_price DOUBLE," +
					    		"rozn_price DOUBLE," +
					    		"incom_price DOUBLE," +
					    		"IsInPrice INT," +
					    		"flagWithoutDiscont INT," +
					    		"weight_k_1 DOUBLE," +
					    		"weight_k_2 DOUBLE," +
					    		"min_quantity DOUBLE," + // минимальное продаваемое количество за 1 раз
					    		"multiplicity DOUBLE," + // кратность количества (нельзя дробить)
					    		"required_sales DOUBLE," + // план продажи за месяц по каждому клиенту
					    		"flags INT," +
					    		"UNIQUE ('id')" +
					    		");");
				    db.execSQL("insert into nomenclature select * from temp_Nomenclature");
				    db.execSQL("drop table temp_Nomenclature");
				    db.execSQL("update nomenclature set parent_id=?", new String[]{Constants.emptyID});
				    db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "NOMENCLATURE"}); 
		    		
		    	}
		    	
		    	if (oldVersion<9)
		    	{
		    		db.execSQL("alter table messages add column type_idx int");
		    		db.execSQL("alter table messages add column date1 text");
		    		db.execSQL("alter table messages add column date2 text");
		    		db.execSQL("alter table messages add column client_id text");
		    		db.execSQL("alter table messages add column nomenclature_id text");
		    	}
		    	
		    	if (oldVersion<10)
		    	{
		    		db.execSQL("alter table nomenclature add column descr_lower text");
		    		db.execSQL("alter table clients add column descr_lower text");
		    	}
		    	
		    	if (oldVersion<11)
		    	{
		    		db.execSQL("alter table messages add column agreement_id text");
		    		db.execSQL("alter table messages add column report text");
		    		db.execSQL("alter table nomenclature add column parent_id_0 text");
		    		db.execSQL("alter table nomenclature add column parent_id_1 text");
		    		db.execSQL("alter table nomenclature add column parent_id_2 text");
		    		db.execSQL("alter table nomenclature add column parent_id_3 text");
		    		db.execSQL("alter table nomenclature add column parent_id_4 text");
		    		
		    		db.execSQL("alter table clients add column parent_id_0 text");
		    		db.execSQL("alter table clients add column parent_id_1 text");
		    		db.execSQL("alter table clients add column parent_id_2 text");
		    		db.execSQL("alter table clients add column parent_id_3 text");
		    		db.execSQL("alter table clients add column parent_id_4 text");
		    	}
		    	
		    	if (oldVersion<12)
		    	{
		    		// nomenclature_hierarchy создается всегда, см. ниже
		    	}
		    	
		    	if (oldVersion<13)
		    	{
		    		db.execSQL("alter table nomenclature add column image_file text");
		    		db.execSQL("alter table nomenclature add column image_file_checksum INT");
		    	}

		    	if (oldVersion<14)
		    	{
		    		db.execSQL("alter table orders add column shipping_type int");
		    		db.execSQL("alter table orders add column shipping_time text");
		    		db.execSQL("alter table orders add column shipping_begin_time text");
		    		db.execSQL("alter table orders add column shipping_end_time text");
		    	}
		    	
		    	if (oldVersion<15)
		    	{
		    		db.execSQL("alter table messages add column isMark int");
		    		
			        db.execSQL("create table curators_price (" +
			        		"_id integer primary key autoincrement, " +
				    		"curator_id text," +
				    		"nomenclature_id text," +
				    		"priceProcent double," +
				    		"priceAdd double," +
				    		"UNIQUE ('curator_id', 'nomenclature_id')" +
				    		");");
		    		
		    	}
		    	
		    	if (oldVersion<17)
		    	{
		    		db.execSQL("CREATE TEMPORARY TABLE messages_backup as select * from messages");
		    		db.execSQL("DROP TABLE messages");
		    		
		    		db.execSQL("create table messages (" +
				    		"_id integer primary key autoincrement," +
				    		"uid text,"+
				    		"sender_id text,"+
				    		"receiver_id text,"+
				    		"text text,"+
				    		"fname text,"+
				    		"datetime text,"+
				    		"acknowledged int,"+
				    		"ver int,"+
				    		"type_idx int,"+
				    		"date1 text,"+
				    		"date2 text,"+
				    		"client_id text,"+
				    		"agreement_id text,"+
				    		"nomenclature_id text,"+
				    		"report text,"+
				    		"isMark int default 0,"+
				    		"isMarkCnt int default 0,"+
				    		"UNIQUE('uid')"+
				    		");");
		    		
				    db.execSQL("insert into messages select _id,uid,sender_id,receiver_id,text,fname,datetime,acknowledged,ver,type_idx,date1,date2,client_id,agreement_id,nomenclature_id,report,0 isMark,0 isMarkCnt from messages_backup");
				    
				    db.execSQL("drop table messages_backup");
				    
				    db.execSQL("update messages set isMark=0, isMarkCnt=0");
				    
		    	}
		    	
		    	if (oldVersion<18)
		    	{
		    		db.execSQL("alter table ordersLines add column discount DOUBLE");
		    	}
		    	
		    	if (oldVersion<19)
		    	{
		    		// надо запросить торговые точки, в них была ошибка
		    		db.execSQL("update versions set ver=-1 where ver>0 and param=\"D_POINTS\"");
		    	}

		    	if (oldVersion<20)
		    	{
		    		db.execSQL("alter table orders add column comment_payment text");
		    	}
		    	
		    	if (oldVersion<21)
		    	{
			      db.execSQL("create table simple_discounts (" +
				     		"_id integer primary key autoincrement, " +
				    		"id text," +
				    		"code text," +
				    		"descr text," +
				    		"priceProcent double," +
				    		"UNIQUE ('id')" +
				    		");");
		    	}

		    	if (oldVersion<22)
		    	{
		    		db.execSQL("alter table orders add column simple_discount_id text");
		    		db.execSQL("alter table orders add column create_client int");
		    		db.execSQL("alter table orders add column create_client_descr text");
		    		db.execSQL("alter table orders add column create_client_surname text");
		    		db.execSQL("alter table orders add column create_client_firstname text");
		    		db.execSQL("alter table orders add column create_client_lastname text");
		    		db.execSQL("alter table orders add column place_num int");
		    		db.execSQL("alter table orders add column card_num int");
		    		db.execSQL("alter table orders add column pay_credit double");
		    		db.execSQL("alter table orders add column ticket_m double");
		    		db.execSQL("alter table orders add column ticket_w double");
		    		db.execSQL("alter table orders add column quant_m int");
		    		db.execSQL("alter table orders add column quant_w int");
		    		
		    		db.execSQL("alter table settings add column ticket_m double null");
		    		db.execSQL("alter table settings add column ticket_w double null");
		    		db.execSQL("alter table settings add column agent_id text null");
		    		
				    db.execSQL("create table cash_payments (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "uid text," +
				    		  "version int," +
				    		  "version_ack int," +
				    		  "versionPDA int," +
				    		  "versionPDA_ack int," +
				    		  "id text," +
				    		  "numdoc text," +
				    		  "datedoc text," +
				    		  "client_id text," +
				    		  "agreement_id text," +
				    		  "comment text," +
				    		  "comment_closing text," +
				    		  "state int," +
				    		  "curator_id text," +
				    		  "sum_doc double," +
				    		  "UNIQUE ('uid','id')" +
				    		  ");");
				    
				    /*
				    db.execSQL("create table debt_ext (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "client_id text," +
				    		  "agreement_id text," +
				    		  "curator_id text," +
				    		  "document text," +
				    		  "plan_pay_date text," +
				    		  "isExpanded int,"+ // 0 - если общий долг, 1,2,3 и т.д - детальные записи
				    		  "sum_debt double," +
				    		  "UNIQUE ('client_id','agreement_id','curator_id','isExpanded')" +
				    		  ");");
				    */
				    
				    db.execSQL("alter table clients add column isUsed int default 0");
				    db.execSQL("alter table pricetypes add column isUsed int default 0");
				    db.execSQL("alter table nomenclature add column isUsed int default 0");
				    db.execSQL("alter table prices add column isUsed int default 0");
				    db.execSQL("alter table simple_discounts add column isUsed int default 0");
				    db.execSQL("alter table saldo add column isUsed int default 0");
				    db.execSQL("alter table rests add column isUsed int default 0");
				    
		    	}

		    	if (oldVersion<23)
		    	{
				      db.execSQL("CREATE TABLE saldo_extended (" +
					    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					    		"client_id TEXT,"+
					    		"agreement_id TEXT,"+
					    		"document_id TEXT,"+
					    		"manager_id TEXT,"+
					    		"document_descr TEXT,"+
					    		"manager_descr TEXT,"+
					    		"saldo DOUBLE,"+
					    		"saldo_past DOUBLE,"+
					    		"isUsed int default 0,"+
					    		"UNIQUE ('client_id','agreement_id','document_id','manager_id')"+
					    		");");
		    	}

		    	if (oldVersion<24)
		    	{
		    		db.execSQL("alter table orders add column create_client_phone text");
		    		db.execSQL("alter table clients add column card_num text");
		    		db.execSQL("alter table clients add column phone_num text");
		    		
		    		db.execSQL("alter table saldo_extended add column agreement_descr text");
		    		db.execSQL("alter table saldo_extended add column organization_descr text");
		    		db.execSQL("alter table saldo_extended add column document_datetime text");
		    		
		    		db.execSQL("alter table orders add column quant_mw int");
		    		
		    		db.execSQL("alter table agreements add column default_manager_id text");
		    		db.execSQL("update agreements set default_manager_id=''");
		    		
		    		db.execSQL("alter table cash_payments add column manager_descr text");
		    		db.execSQL("alter table cash_payments add column organization_descr text");
		    		
		    		
		    	}
		    	
		    	if (oldVersion<25)
		    	{
				    db.execSQL("create table ordersPlaces (" +
				    		"_id integer primary key autoincrement, " +
				    		"order_id integer," + // внутренний код
				    		"place_id text" +
				    		");");
				      
				    db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
				      
				    db.execSQL("CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
				            "FOR EACH ROW BEGIN DELETE FROM ordersLines WHERE order_id=OLD._id; " +
				            "DELETE FROM ordersPlaces WHERE order_id=OLD._id; END;");
		    		
			        db.execSQL("CREATE TABLE places (" +
			        		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				    		"id TEXT,"+
				    		"code TEXT," +
				    		"descr TEXT," +
				    		"isUsed int default 0,"+
				    		"UNIQUE ('id')"+
				    		");");
				    
			        db.execSQL("CREATE TABLE occupied_places (" +
				    		"place_id TEXT,"+
				    		"client_id TEXT,"+
				    		"document_id TEXT,"+
				    		"datedoc TEXT,"+
				    		"document TEXT,"+
				    		"shipping_time TEXT,"+
				    		"shipping_date TEXT,"+
				    		"isUsed int default 0,"+
				    		"UNIQUE ('place_id', 'client_id','document_id')"+
				    		");");
		    		
		    		db.execSQL("alter table nomenclature add column order_for_sorting INT");
		    		db.execSQL("update nomenclature set order_for_sorting=0");
		    		
		    		db.execSQL("alter table orders add column shipping_date text");
		    		db.execSQL("update orders set shipping_date=\"\"");
		    		
		    		db.execSQL("alter table ordersLines add column shipping_time text");
		    		db.execSQL("update ordersLines set shipping_time=\"\"");
		    		
		    	}
		    	
		    	if (oldVersion<26)
		    	{
		    		db.execSQL("alter table orders add column manager_comment text");
		    		db.execSQL("update orders set manager_comment=\"\"");
		    		
		    		db.execSQL("alter table orders add column theme_comment text");
		    		db.execSQL("update orders set theme_comment=\"\"");		    	

		    		db.execSQL("alter table orders add column phone_num text");
		    		db.execSQL("update orders set phone_num=\"\"");		    	
		    	}
		    	
		    	if (oldVersion<27)
		    	{
		    		db.execSQL("alter table ordersLines add column comment_in_line text");
		    		db.execSQL("update ordersLines set comment_in_line=\"\"");
		    	}
		    	
		    	if (oldVersion<28)
		    	{
		    		db.execSQL("alter table nomenclature add column group_of_analogs INT");
		    		db.execSQL("update nomenclature set group_of_analogs=0");
		    		db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "NOMENCLATURE"});

		    		
				    db.execSQL("create table refundsLines (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "refund_id integer," + // внутренний код
				    		  "nomenclature_id text," +
				    		  "client_id text," +
				    		  "quantity_requested double," +
				    		  "quantity double," +
				    		  //"price double," +
				    		  //"total double," +
				    		  //"discount DOUBLE,"+
				    		  "k double," +
				    		  "ed text," +
				    		  "comment_in_line text" +
				    		  ");");
				      
				    db.execSQL("create table refunds (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "uid text," +
				    		  "version int," +
				    		  "version_ack int," +
				    		  "versionPDA int," +
				    		  "versionPDA_ack int," +
				    		  "base_doc_id text," +
				    		  "id text," +
				    		  "numdoc text," +
				    		  "datedoc text," +
				    		  "client_id text," +
				    		  "agreement_id text," +
				    		  "distr_point_id text," +
				    		  "comment text," +
				    		  "comment_closing text," +
				    		  "closed_not_full int," +
				    		  "state int," +
				    		  "curator_id text," +
				    		  "bw int," +
				    		  "trade_type text," +
				    		  "datecoord text," +
				    		  "latitude double," +
				    		  "longitude double," +
				    		  "accept_coord int," +
				    		  "dont_need_send int," +
				    		  "price_type_id text," +
				    		  "stock_id text," +
				    		  "sum_doc double," +
				    		  "weight_doc double," +
				    		  
							  "simple_discount_id text," +
				    		  "UNIQUE ('uid','id')" +
				    		  ");");
		    		
		    		db.execSQL("alter table agreements add column flags int");
		    		db.execSQL("update agreements set flags=0");
		    		
		    	}
		    	
		    	if (oldVersion<31)
		    	{
		    		db.execSQL("alter table saldo add column saldo_past30 DOUBLE default 0");
		    		db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "SALDO"});
		    	}
		    	if (oldVersion<32)
		    	{
		    		db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "SALDO"});
		    	}
		    	if (oldVersion<34)
		    	{
		    		db.execSQL("alter table orders add column sum_shipping double default -1");
					
		    	}

		    	if (oldVersion<35)
		    	{
		    		db.execSQL("alter table nomenclature add column nomenclature_color INT default 0");
		    		db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "NOMENCLATURE"});
		    	}
		    	
		    	if (oldVersion<36)
		    	{
		    		db.execSQL("alter table cash_payments add column vicarious_power_id text");
		    		db.execSQL("alter table cash_payments add column vicarious_power_descr text");
		    		
		    		db.execSQL("update cash_payments set vicarious_power_id=''");
		    		db.execSQL("update cash_payments set vicarious_power_descr=''");
		    		
			        db.execSQL("create table vicarious_power (" +
			    		  "_id integer primary key autoincrement, " +
			    		  "id text," +
			    		  "descr text," +
			    		  "numdoc text," +
			    		  "datedoc text," +
			    		  "date_action text," +
			    		  "comment text," +
			    		  "client_id text," +
			    		  "client_descr text," +
			    		  "agreement_id text," +
			    		  "agreement_descr text," +
			    		  "fio_descr text," +
			    		  "manager_id text," +
			    		  "manager_descr text," +
			    		  "organization_id text," +
			    		  "organization_descr text," +
			    		  "state text," +
			    		  "sum_doc double," +
			    		  "UNIQUE ('id')" +
			    		  ");");
		    		
		    	}
		    	
		    	if (oldVersion<37)
		    	{
		    		db.execSQL("create table gps_coord (" +
				    		  "datecoord text," +
				    		  "latitude double," +
				    		  "longitude double," +
				    		  "version int" +
				    		  ");");
		    		
		    		db.execSQL("alter table settings add column gps_interval int default 0");
		    	}
		    	
		    	if (oldVersion<38)
		    	{
				      db.execSQL("CREATE TABLE distribsContracts (" +
					    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					    		"id TEXT," +
					    		"position int," +
					    		"code TEXT," +
					    		"descr TEXT," +
					    		"UNIQUE ('id')" +
					    		");");
		    		
				      db.execSQL("create table distribsLines (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "distribs_id integer," + // внутренний код
				    		  "contract_id text," +
				    		  "quantity double" +
				    		  ");");
				      
				      db.execSQL("create table distribs (" +
				    		  "_id integer primary key autoincrement, " +
				    		  "uid text," +
				    		  "version int," +
				    		  "version_ack int," +
				    		  "versionPDA int," +
				    		  "versionPDA_ack int," +
				    		  "id text," +
				    		  "numdoc text," +
				    		  "datedoc text," +
				    		  "client_id text," +
				    		  "curator_id text," +
				    		  "distr_point_id text," +
				    		  "state int," +
				    		  "comment text," +
				    		  "datecoord text," +
				    		  "latitude double," +
				    		  "longitude double," +
				    		  "accept_coord int," +
				    		  "UNIQUE ('uid','id')" +
				    		  ");");
				      
		    	}
		    	
		    	if (oldVersion<39)
		    	{
		    		
		    		db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
		    		db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert");
					db.execSQL("DROP TRIGGER IF EXISTS orders_on_update");
					db.execSQL("DROP TRIGGER IF EXISTS refunds_on_delete");
					db.execSQL("DROP TRIGGER IF EXISTS refunds_on_insert");
					db.execSQL("DROP TRIGGER IF EXISTS refunds_on_update");
					db.execSQL("DROP TRIGGER IF EXISTS distribs_on_delete");
					db.execSQL("DROP TRIGGER IF EXISTS distribs_on_insert");
					db.execSQL("DROP TRIGGER IF EXISTS distribs_on_update");
		    		
		    		// из-за того, что непонятно, как можно добавить distribs_id в UNIQUE,
		    		// удаляем журнал и снова его создаем. заполнится он при последующей реиндексации
		    		db.execSQL("DROP TABLE IF EXISTS journal");
			        db.execSQL("CREATE TABLE journal (" +
				    		"_id integer primary key autoincrement, " +
			        		// 0-order
				    		// 1-refund
			        		// 2-payment (но их в журнале сейчас нет, они отдельно)
				    		// 3-distribs
			        		"iddocdef int,"+
			        		"order_id INT,"+
			        		"payment_id INT,"+
			        		"refund_id INT,"+
			        		"distribs_id INT,"+
				    		"uid text," +
				    		"id text," +
				    		"numdoc text," +
				    		"datedoc text," +
				    		"shipping_date text," +
				    		"client_id text," +
				    		"use_client_descr int,"+
				    		"client_descr text," +
				    		"state int," +
				    		"sum_doc double," +
				    		"sum_shipping double default -1," +
				    		"color text," +
				    		"isUsed int default 0,"+
			    		    "UNIQUE ('uid','id')," +
			    		    "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
				    		");");
			        
			        
					//db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");				
			        
					db.execSQL("CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
					        "FOR EACH ROW BEGIN " +
					        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +
					//      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
					        "DELETE FROM journal WHERE order_id=OLD._id; " +
					        "END;");
					
					//db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
					//      "FOR EACH ROW BEGIN " +
					//      "DELETE FROM journal WHERE payment_id=OLD._id; " +
					//      "END;");
					
					db.execSQL("CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
					        "FOR EACH ROW BEGIN " +
					        "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
					        "DELETE FROM journal WHERE refund_id=OLD._id; " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
					        "FOR EACH ROW BEGIN " +
					        "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
					        "DELETE FROM journal WHERE distribs_id=OLD._id; " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
					        "FOR EACH ROW BEGIN " +
					        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
					        "FOR EACH ROW BEGIN " +
					        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color="+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" where order_id=new._id; " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
					        "FOR EACH ROW BEGIN " +
					        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, "+JOURNAL_REFUND_COLOR_FOR_TRIGGER+"); " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
					        "FOR EACH ROW BEGIN " +
					        "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color="+JOURNAL_REFUND_COLOR_FOR_TRIGGER+" where refund_id=new._id; " +
					        "END;");
					
					
					db.execSQL("CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
					        "FOR EACH ROW BEGIN " +
					        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
					        " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, "+JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER+"); " +
					        "END;");
					
					db.execSQL("CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
					        "FOR EACH ROW BEGIN " +
					        "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color="+JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER+" where distribs_id=new._id; " +
					        "END;");
			        
		    	}
		    	
		    	if (oldVersion<41)
		    	{
				      db.execSQL("CREATE TABLE equipment (" +
					    		"_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					    		"id TEXT," +
					    		"code TEXT," +
					    		"descr TEXT," +
					    		"flags INT," +
					    		"UNIQUE ('id')" +
					    		");");
					      /*
				      db.execSQL("CREATE TABLE equipment_rests (" +
				    		    "_id integer primary key autoincrement,"+
					    		"client_id TEXT,"+
					    		"nomenclature_id TEXT,"+
					    		"distr_point_id TEXT,"+
					    		"quantity DOUBLE,"+
					    		"sum DOUBLE,"+
					    		"flags INT,"+
					    		"UNIQUE ('client_id', 'nomenclature_id', 'distr_point_id')"+
					    		");");
					    */
		    	}
		    	
		    	if (oldVersion<42)
		    	{
		    		/*
		    		db.execSQL("drop table if exists equipment_rests");
			        db.execSQL("CREATE TABLE equipment_rests (" +
			        		"_id integer primary key autoincrement,"+
				    		"client_id TEXT,"+
				    		"agreement_id TEXT,"+
				    		"nomenclature_id TEXT,"+
				    		"distr_point_id TEXT,"+
				    		"quantity DOUBLE,"+
				    		"sum DOUBLE,"+
				    		"flags INT,"+
				    		"UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id')"+
				    		");");
			        db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "EQUIPMENT_RESTS"});
			        */
		    	}
		    	
		    	if (oldVersion<43)
		    	{
		    		db.execSQL("alter table orders add column datecreation text default \"\"");
		    		db.execSQL("alter table orders add column gpsstate int default 0");
		    		db.execSQL("alter table orders add column gpsaccuracy float default 0");
		    		db.execSQL("alter table refunds add column datecreation text default \"\"");
		    		db.execSQL("alter table refunds add column gpsstate int default 0");
		    		db.execSQL("alter table refunds add column gpsaccuracy float default 0");
		    		db.execSQL("alter table distribs add column gpsstate int default 0");
		    		db.execSQL("alter table distribs add column gpsaccuracy float default 0");
		    		db.execSQL("alter table gps_coord add column gpsstate int default 0");
		    		db.execSQL("alter table gps_coord add column gpsaccuracy float default 0");
		    	}
		    	
		    	if (oldVersion<44)
		    	{
		    		  db.execSQL("drop TABLE IF EXISTS equipment_rests");
		    	
		    		  db.execSQL("CREATE TABLE equipment_rests (" +
			        		"_id integer primary key autoincrement,"+
				    		"client_id TEXT,"+
				    		"agreement_id TEXT,"+
				    		"nomenclature_id TEXT,"+
				    		"distr_point_id TEXT,"+
				    		"quantity DOUBLE,"+
				    		"sum DOUBLE,"+
				    		"flags INT,"+
				    		"doc_id TEXT,"+
				    		"doc_descr TEXT,"+
				    		"date TEXT,"+
				    		"datepast TEXT,"+
				    		"UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')"+
				    		");");
		    		  
			          db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "EQUIPMENT_RESTS"});
		    	}
		    	
		    	if (oldVersion<46)
		    	{
		    		db.execSQL("alter table ordersLines add column lineno int");
		    		db.execSQL("alter table orders add column editing_backup int");
		    		db.execSQL("alter table orders add column old_id int");
		    		db.execSQL("alter table distr_points add column price_type_id text");
		    		
		    		db.execSQL("update orders set editing_backup=0, old_id=0, price_type_id=?", new String[]{Constants.emptyID});
		    		db.execSQL("update ordersLines set lineno=0");
					db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "D_POINTS"});
		    	}
		    	if (oldVersion<47)
		    	{
			      	// Меняется только один триггер (insert), но т.к. таблицу переименовываем, и удаляем,
			      	// непонятно, что может произойти с триггером в разных версиях андроида, поэтому пересоздадим все
		    		//
		    		// ну и есть подозрение, что очищаются записи в журнале при удалении таблицы
		    		// поэтому код удаления перенес выше в версии 3.63
		    		db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
		    		db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert");
					db.execSQL("DROP TRIGGER IF EXISTS orders_on_update");
		    		
		    		
		    		//SELECT name FROM sqlite_master WHERE type == 'index' AND lower(tbl_name) == 'orders'
		    		db.execSQL("ALTER TABLE orders RENAME TO orders_old");
			        db.execSQL("create table orders (" +
			    		  "_id integer primary key autoincrement, " +
			    		  "uid text," +
			    		  "version int," +
			    		  "version_ack int," +
			    		  "versionPDA int," +
			    		  "versionPDA_ack int," +
			    		  "id text," +
			    		  "numdoc text," +
			    		  "datedoc text," +
			    		  "client_id text," +
			    		  "agreement_id text," +
			    		  "distr_point_id text," +
			    		  "comment text," +
			    		  "comment_closing text," +
			    		  "comment_payment text," +
			    		  "closed_not_full int," +
			    		  "state int," +
			    		  "curator_id text," +
			    		  "bw int," +
			    		  "trade_type text," +
			    		  "datecreation text," +
			    		  "datecoord text," +
			    		  "latitude double," +
			    		  "longitude double," +
			    		  "gpsstate int," +
			    		  "gpsaccuracy float," +
			    		  "accept_coord int," +
			    		  "dont_need_send int," +
			    		  "price_type_id text," +
			    		  "stock_id text," +
			    		  "sum_doc double," +
			    		  "weight_doc double," +
			    		  "shipping_type int," +
			    		  "shipping_time int," +
			    		  "shipping_begin_time text," +
			    		  "shipping_end_time text," +
			    		  "shipping_date text," +
			    		  
						  "simple_discount_id text," +
			    		  "create_client int," +
			    		  "create_client_descr text," +
			    		  "create_client_surname text," +
			    		  "create_client_firstname text," +
			    		  "create_client_lastname text," +
			    		  "create_client_phone text," +
			    		  "place_num int," +
			    		  "card_num int," +
			    		  "pay_credit double," +
			    		  "ticket_m double," +
			    		  "ticket_w double," +
			    		  "quant_m int," +
			    		  "quant_w int," +
			    		  "quant_mw int," +
						  "manager_comment text," +
						  "theme_comment text," +
						  "phone_num text," +
						  "sum_shipping double," +
						  // 0-документ в нормальном состоянии
						  // 1-новый документ, записать не успели
						  // 2-документ начали редактировать, но не записали и не отменили изменения
						  "editing_backup int," +
			    		  // для состояния 2 - _id редактируемого документа
						  // в других случаях - не важно
			    		  "old_id int" +
						  // До 19.02.2018 editing_backup тут не было
			    		  //"UNIQUE ('uid','id')" +
			    		  ");");
			        
			        db.execSQL("PRAGMA foreign_keys=off");
			      	//db.execSQL("insert into orders select * from orders_old");
			        // Важно перечислить поля, т.к. не у всех порядок полей совпадает (из-за обновлений старых версий)
			        String all_fields_list="_id,uid,version,version_ack,versionPDA,versionPDA_ack,id,numdoc,datedoc,client_id,agreement_id,distr_point_id,"+
			        "comment,comment_closing,comment_payment,closed_not_full,state,curator_id,bw,trade_type,"+
			        "datecreation,datecoord,latitude,longitude,gpsstate,gpsaccuracy,accept_coord,dont_need_send,"+
			        "price_type_id,stock_id,sum_doc,weight_doc,shipping_type,shipping_time,shipping_begin_time,shipping_end_time,"+
			        "shipping_date,simple_discount_id,create_client,create_client_descr,create_client_surname,"+
			        "create_client_firstname,create_client_lastname,create_client_phone,place_num,card_num,pay_credit,ticket_m,ticket_w,quant_m,"+
			        "quant_w,quant_mw,manager_comment,theme_comment,phone_num,sum_shipping,editing_backup,old_id";
			        db.execSQL("insert into orders ("+all_fields_list+") select "+all_fields_list+" from orders_old order by _id");
			      	db.execSQL("PRAGMA foreign_keys=on");
			      	// С 19.02.2018
			      	db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS OrdersUniqueIndex ON orders ('uid','id','editing_backup')");
			      	db.execSQL("drop table orders_old");
			      	
					db.execSQL("CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
					        "FOR EACH ROW BEGIN " +
					        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +
					//      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
					        "DELETE FROM journal WHERE order_id=OLD._id; " +
					        "END;");
					
					//db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
					//        "WHEN new.editing_backup=0 BEGIN " +
					//        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
					//        "END;");
					
					db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
					        "WHEN new.editing_backup=0 BEGIN " +
					        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
					        "END;");
					
					//db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
					//        "FOR EACH ROW BEGIN " +
					//        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color="+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" where order_id=new._id; " +
					//        "END;");
					
					db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
					        "WHEN new.editing_backup=0 BEGIN " +
					        "insert or replace into journal (uid, id, iddocdef, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color, order_id, payment_id, refund_id, distribs_id) values (new.uid, new.id, 0, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+", new._id, 0, 0, 0); "+
					        "END;");
			      
		    	}
		    	if (oldVersion<48)
		    	{
		    		db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);");
		    	}

                if (oldVersion<49) {
                    db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));");
                }

                if (oldVersion<50) {
		    	    // Поле добавится, но в разделе UNIQUE это поле прописано не будет
                    // но т.к. у этой организации, где это важно, будет установлена новая версия,
                    // а у всех остальных это поле всегда будет пустым, то это вообще не важно
                    db.execSQL("alter table saldo_extended add column agreement30_id text");
                    db.execSQL("update saldo_extended set agreement30_id=''");

                    db.execSQL("create table agreements30 (" +
                            "_id integer primary key autoincrement, " +
                            "id text," +
                            "owner_id text," +
                            "organization_id text," +
                            "code text," +
                            "descr text," +
                            "price_type_id text," +
                            "default_manager_id text," +
                            "sale_id text," +
                            "kredit_days int," +
                            "kredit_sum double," +
                            "flags int," +
                            "UNIQUE ('id', 'owner_id')" +
                            ");");

                    db.execSQL("alter table orders add column agreement30_id text");
                    db.execSQL("update orders set agreement30_id=''");

		    	    db.execSQL("create table prices_agreements30 (" +
                            "_id integer primary key autoincrement, " +
                            "agreement30_id text," +
                            "nomenclature_id text," +
                            "pack_id text," + // Пока не используется
                            "ed_izm_id text," +
                            "edIzm text," +
                            "price double," +
                            "k double," +
                            "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                            ");");
                }

                if (oldVersion<51) {

                    db.execSQL("alter table orders add column gpstype int");
                    db.execSQL("alter table distribs add column gpstype int");
                    db.execSQL("alter table gps_coord add column gpstype int");

                    db.execSQL("update orders set gpstype=gpsstate");
                    db.execSQL("update distribs set gpstype=gpsstate");
                    db.execSQL("update gps_coord set gpstype=gpsstate");

                    db.execSQL("alter table cash_payments add column datecreation text");
                    db.execSQL("alter table cash_payments add column latitude text");
                    db.execSQL("alter table cash_payments add column longitude text");
                    db.execSQL("alter table cash_payments add column gpsstate int");
                    db.execSQL("alter table cash_payments add column gpsaccuracy float");
                    db.execSQL("alter table cash_payments add column gpstype int");

                    db.execSQL("update cash_payments set datecreation=''");
                    db.execSQL("update cash_payments set latitude=0.0");
                    db.execSQL("update cash_payments set longitude=0.0");
                    db.execSQL("update cash_payments set gpsstate=0");
                    db.execSQL("update cash_payments set gpsaccuracy=0.0");
                    db.execSQL("update cash_payments set gpstype=0");

                }

                if (oldVersion<52)
                {
                    db.execSQL("alter table cash_payments add column datecoord text");
                    db.execSQL("update cash_payments set datecoord=''");
                }

                if (oldVersion<53)
                {
                    db.execSQL("alter table cash_payments add column accept_coord int");
                    db.execSQL("update cash_payments set accept_coord=0");
                }

                if (oldVersion<54) {
                    db.execSQL("alter table nomenclature add column image_width int");
                    db.execSQL("alter table nomenclature add column image_height int");
                }
                if (oldVersion<55) {
                    db.execSQL("alter table nomenclature add column image_file_size int");
                }

                if (oldVersion<63)
                {
                    db.execSQL("create table routes (" +
                            "_id integer primary key autoincrement, " +
                            "id text," +
                            "code text," +
                            "descr text," +
                            "manager_id text," +
                            "UNIQUE ('id')"+
                            ");");

                    db.execSQL("create table routes_dates (" +
                            "_id integer primary key autoincrement, " +
                            "route_date text," +
                            "route_id text," +
                            "UNIQUE ('route_date')"+
                            ");");

                    db.execSQL("create table routes_lines (" +
                            "_id integer primary key autoincrement, " +
                            "route_id text," +
                            "lineno int," +
                            "distr_point_id text," +
                            "visit_time text," +
                            "UNIQUE ('route_id', 'lineno')" +
                            ");");
                    //
                    db.execSQL("create table real_routes_dates (" +
                            "_id integer primary key autoincrement, " +
                            "uid text," + // в КПК
                            "id text," + // в 1С
                            "route_date text," +
                            "route_id text," +
                            "route_descr text," +
                            "UNIQUE ('route_date')" +
                            ");");

                    db.execSQL("create table real_routes_lines (" +
                            "_id integer primary key autoincrement, " +
                            "real_route_id integer," +
                            "lineno int," +
                            "distr_point_id text," +
                            "required_visit_time text," +
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

                    db.execSQL("alter table cash_payments add column distr_point_id text");

                }

                if (oldVersion<64)
                {
                    db.execSQL("alter table real_routes_lines add column gpsstate int");
                    db.execSQL("alter table real_routes_lines add column gpsaccuracy float");
                    db.execSQL("alter table real_routes_lines add column accept_coord int");
                    db.execSQL("update real_routes_lines set gpsstate=0");
                    db.execSQL("update real_routes_lines set gpsaccuracy=0.0");
                    db.execSQL("update real_routes_lines set accept_coord=0");
                }

                if (oldVersion<66)
                {
                    db.execSQL("alter table refunds add column shipping_type int");
                    db.execSQL("update refunds set shipping_type=1");
                }

                if (oldVersion<69) {
                    //db.execSQL("alter table Rests add column organization_id text default ''");
                    //db.execSQL("update Rests set organization_id=''");

                    db.execSQL("CREATE TEMPORARY TABLE temp_Rests as select * from Rests");
                    db.execSQL("DROP TABLE Rests");
                    db.execSQL("alter table temp_Rests add column organization_id int");
                    // Можно было EmptyID записать туда
                    db.execSQL("update temp_Rests set organization_id=''");

                    db.execSQL("CREATE TABLE Rests (" +
                            "stock_id TEXT,"+
                            "nomenclature_id TEXT,"+
                            "organization_id TEXT default '',"+
                            "quantity DOUBLE,"+
                            "quantity_reserve DOUBLE,"+
                            "isUsed int default 0,"+
                            "UNIQUE ('stock_id', 'nomenclature_id','organization_id')"+
                            ");");

                    db.execSQL("insert into Rests select * from temp_Rests");
                    db.execSQL("drop table temp_Rests");
                    // В принципе это не обязательно для всех, кроме Тандема (а они все равно начинают сначала)
                    db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "RESTS"});

                }

                if (oldVersion<70) {
                    db.execSQL("alter table clients add column email_for_cheques text");
                    db.execSQL("update clients set email_for_cheques=''");
                }

                if (oldVersion<71) {
                    db.execSQL("alter table settings add column agent_price_type_id text null");
                }

                if (oldVersion<72) {
                    db.execSQL("alter table nomenclature add column compose_with text null");
                }

                if (oldVersion<73) {
                    // есть подозрение, что там может быть null
                    // при загрузке из текстовых файлов, в случае, когда это группа
                    db.execSQL("update nomenclature set flags=0");

//                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//                    // В старом формате это была строка, в новом - Long
//                    String oldDate=sharedPreferences.getString("work_date", "");
//                    if (!oldDate.isEmpty()) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.remove("work_date");
//                        try {
//                            Date newDate=new SimpleDateFormat("yyyy.MM.dd").parse(oldDate);
//                            editor.putLong("work_date_l", newDate.getTime());
//                        } catch (ParseException e) {
//                        }
//                        editor.commit();
//                    }
//
//                    String oldDate2=sharedPreferences.getString("start_date_for_occupied_places", "");
//                    if (!oldDate2.isEmpty()) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.remove("start_date_for_occupied_places");
//                        try {
//                            Date newDate=new SimpleDateFormat("yyyy.MM.dd").parse(oldDate2);
//                            editor.putLong("start_date_for_occupied_places_l", newDate.getTime());
//                        } catch (ParseException e) {
//                        }
//                        editor.commit();
//                    }

                }


		    	/*
		    	if (oldVersion<43)
		    	{
		    		  db.execSQL("drop TABLE equipment_rests");
		    		  
				      db.execSQL("CREATE TABLE equipment_rests (" +
				    		    "_id integer primary key autoincrement,"+
					    		"client_id TEXT,"+
					    		"nomenclature_id TEXT,"+
					    		"distr_point_id TEXT,"+
					    		"quantity DOUBLE,"+
					    		"sum DOUBLE,"+
					    		"flags INT,"+
					    		"UNIQUE ('client_id', 'nomenclature_id', 'distr_point_id')"+
					    		");");
		    	}
		    	*/
		    	
		    	// TODO
		    	// удалить колонку create_client_phone
		    	
		        // Продажи в телефоне (текущие)
		    	db.execSQL("drop view if exists salesVB");
		    	/*
			    db.execSQL("CREATE VIEW salesVB as select " +
			    		"nomenclature_id, "+
			    		"client_id, " +
			    		"sum(quantity) as quantity_saled from orders " +
			    		"join ordersLines on ordersLines.order_id=orders._id " +
			    		"where orders.state="+E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value()+" "+
			    		"group by nomenclature_id, client_id;");
			    */

			    // Продажи в 1С (история)
			    db.execSQL("drop view if exists salesV");
			    // То же самое, для окна ввода количества
			    //db.execSQL("drop view if exists salesGroupsV");
			    /*
			    db.execSQL("CREATE VIEW salesV as select " +
			    		"nomenclature_id,"+
			    		"client_id,"+
			    		"sum(quantity) as quantity_saled "+
			    		"from salesloaded "+
			    		"group by nomenclature_id, client_id;");
			    */

			    // Продажи в 1С и в КПК вместе, понедельно, с 30.11.2020
                db.execSQL("drop view if exists salesV_7");

		    	// Эти таблицы пересоздаем всегда 
	    		db.execSQL("drop table if exists nomenclature_hierarchy");
			    db.execSQL("create table nomenclature_hierarchy (" +
			    		"id text,"+
			    		"ord_idx int,"+
			    		"groupDescr text,"+
			    		"level int,"+
			    		"level0_id text,"+
			    		"level1_id text,"+
			    		"level2_id text,"+
			    		"level3_id text,"+
			    		"level4_id text,"+
			    		"level5_id text,"+
			    		"level6_id text,"+
			    		"level7_id text,"+
			    		"level8_id text,"+
                        "dont_use_in_hierarchy int,"+
			    		"UNIQUE ('id')"+
			    		");");

			    db.execSQL("drop table if exists salesL");
			    db.execSQL("create table salesL (" +
			    		"nomenclature_id text," +			    
			    		"client_id text," +
                        //"distr_point_id text," +
			    		"strQuantity text," +
			    		"quantity double," +
			    		//"quantity_now double," +
			    		"UNIQUE (nomenclature_id, client_id)" +
			    		");");
			    
			    db.execSQL("drop table if exists salesL2");
			    db.execSQL("create table salesL2 (" +
			    		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			    		// здесь номенклатура или группа (в зависимости от настроек)
			    		"nomenclature_id text," +
			    		"client_id text," +
                        // 20.09.2019
                        "distr_point_id text," +
                        //
			    		"datedoc text," +
			    		"numdoc text," +
			    		"quantity double," +
			    		// здесь стоимость, а не цена
			    		"price double" +
			    		");");
			    // ^^^^ таблицы истории продаж перезаполняются при изменении версии базы из MainActivity

		    	db.execSQL(String.format("insert into seances(incoming) values ('UPGRADED%d')", oldVersion));
		    }
		    
		    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		    	//
		        onUpgrade(db, oldVersion, newVersion);
		    }		    
		    
		    public void Reindex()
		    {
		    	SQLiteDatabase db = dbHelper.getWritableDatabase();
		    	
		    	db.execSQL("DROP INDEX IF EXISTS Clients_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Clients_parent_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Clients_isFolder_idx");
		    	db.execSQL("DROP INDEX IF EXISTS organizations_idx");
		    	db.execSQL("DROP INDEX IF EXISTS agreements_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS agreements_owner_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Nomenclature_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Nomenclature_parent_id_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Nomenclature_isFolder_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Nomenclature_descr_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Rests_nomenclature_stocks_idx");
		    	db.execSQL("DROP INDEX IF EXISTS prices_nomenclature_price_idx");
		    	db.execSQL("DROP INDEX IF EXISTS clients_price_client_idx");
		    	db.execSQL("DROP INDEX IF EXISTS curators_price_curator_idx");
		    	db.execSQL("DROP INDEX IF EXISTS agents_idx");
		    	db.execSQL("DROP INDEX IF EXISTS curators_idx");
		    	db.execSQL("DROP INDEX IF EXISTS Saldo_client_idx");
		    	db.execSQL("DROP INDEX IF EXISTS saldo_extended_client_idx");
		    	db.execSQL("DROP INDEX IF EXISTS distr_points_idx");
		    	db.execSQL("DROP INDEX IF EXISTS OrdersLines_idx");
		    	db.execSQL("DROP INDEX IF EXISTS OrdersLines_sales");
		    	db.execSQL("DROP INDEX IF EXISTS orders_idx");
		    	db.execSQL("DROP INDEX IF EXISTS orders_datedoc");
		    	db.execSQL("DROP INDEX IF EXISTS orders_uid");
		    	db.execSQL("DROP INDEX IF EXISTS orders_state");
		    	db.execSQL("DROP INDEX IF EXISTS salesloaded_idx");
		    	db.execSQL("DROP INDEX IF EXISTS unique_salesloaded_idx");
		    	db.execSQL("DROP INDEX IF EXISTS journal_order_id");
		    	db.execSQL("DROP INDEX IF EXISTS journal_payment_id");
		    	db.execSQL("DROP INDEX IF EXISTS journal_refund_id");
		    	db.execSQL("DROP INDEX IF EXISTS salesL2_idx");
                //db.execSQL("DROP INDEX IF EXISTS salesL2_no_dp_idx");
		    	db.execSQL("DROP INDEX IF EXISTS equipment_rests_client_id");
                db.execSQL("DROP INDEX IF EXISTS agreements30_owner_id");
		    	
			    db.execSQL("CREATE INDEX Clients_id_idx ON Clients (id ASC);");
			    db.execSQL("CREATE INDEX Clients_parent_id_idx ON Clients (parent_id ASC);");
			    db.execSQL("CREATE INDEX Clients_isFolder_idx ON Clients (isFolder ASC);");
			    db.execSQL("CREATE INDEX organizations_idx ON organizations (id ASC);");
			    db.execSQL("CREATE INDEX agreements_id_idx ON agreements (id ASC);");
			    db.execSQL("CREATE INDEX agreements_owner_id_idx ON agreements (owner_id ASC);");
			    db.execSQL("CREATE INDEX Nomenclature_id_idx ON Nomenclature (id ASC);");
			    db.execSQL("CREATE INDEX Nomenclature_parent_id_idx ON Nomenclature (parent_id ASC);");
			    db.execSQL("CREATE INDEX Nomenclature_isFolder_idx ON Nomenclature (isFolder ASC);");
			    db.execSQL("CREATE INDEX Nomenclature_descr_idx ON Nomenclature (descr ASC);");
			    db.execSQL("CREATE INDEX Rests_nomenclature_stocks_idx ON Rests (nomenclature_id ASC, stock_id ASC);");
			    db.execSQL("CREATE INDEX prices_nomenclature_price_idx ON prices (nomenclature_id ASC, price_type_id ASC);");
			    db.execSQL("CREATE INDEX clients_price_client_idx ON clients_price (client_id ASC);");
			    db.execSQL("CREATE INDEX curators_price_curator_idx ON curators_price (curator_id ASC);");
			    db.execSQL("CREATE INDEX agents_idx ON agents (id ASC);");
			    db.execSQL("CREATE INDEX curators_idx ON curators (id ASC);");
			    db.execSQL("CREATE INDEX Saldo_client_idx ON Saldo (client_id ASC);");
			    db.execSQL("CREATE INDEX saldo_extended_client_idx ON saldo_extended (client_id ASC);");
			    db.execSQL("CREATE INDEX distr_points_idx ON distr_points (id ASC);");
			    db.execSQL("CREATE INDEX OrdersLines_idx ON ordersLines (order_id ASC);");
			    db.execSQL("CREATE INDEX OrdersLines_sales ON ordersLines (nomenclature_id,client_id ASC);");
			    db.execSQL("CREATE INDEX orders_idx ON orders (id ASC);");
			    db.execSQL("CREATE INDEX orders_datedoc ON orders (datedoc ASC);");
			    db.execSQL("CREATE INDEX orders_uid ON orders (uid ASC);");
			    db.execSQL("CREATE INDEX orders_state ON orders (state ASC, accept_coord ASC);");
			    db.execSQL("CREATE INDEX salesloaded_idx ON salesloaded (nomenclature_id ASC, client_id ASC, distr_point_id ASC, datedoc ASC);");
			    db.execSQL("CREATE INDEX unique_salesloaded_idx ON salesloaded (nomenclature_id ASC, refdoc ASC);");
			    db.execSQL("CREATE INDEX journal_order_id ON journal (order_id ASC);");
			    db.execSQL("CREATE INDEX journal_payment_id ON journal (payment_id ASC);");
			    db.execSQL("CREATE INDEX journal_refund_id ON journal (refund_id ASC);");
			    db.execSQL("CREATE INDEX equipment_rests_client_id ON equipment_rests (client_id ASC, agreement_id ASC);");
		    	//db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, distr_point_id ASC, nomenclature_id ASC, datedoc ASC);");
                //db.execSQL("CREATE INDEX salesL2_no_dp_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);");
                db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);");
                db.execSQL("CREATE INDEX agreements30_owner_id ON agreements30 (owner_id ASC);");

			    // TODO сделать удаление несуществующих и добавление новых
			    // (из-за того, что таким образом нарушится сортировка, т.е. порядок ввода документов)
			    //db.execSQL("delete from journal");
			    
			    db.execSQL("update journal set isUsed=0");
			    //db.execSQL("INSERT OR REPLACE INTO journal select j._id, d._id as order_id, 0 as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, d.create_client as use_client_descr, d.create_client_descr as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from orders d left join journal j on j.order_id=d._id;");
			    ////db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, d._id as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from cash_payments d join journal j on j.payment_id=d._id;");
			    //db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, 0 as payment_id, d._id as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from refunds d left join journal j on j.refund_id=d._id;");

			    db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 0 as iddocdef, new._id as order_id, 0 as payment_id, 0 as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date as shipping_date, new.client_id, new.create_client as use_client_descr, new.create_client_descr as client_descr, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" as color, 1 as isUsed from orders new left join journal j on j.order_id=new._id where editing_backup=0;");
			    db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 1 as iddocdef, 0 as order_id, 0 as payment_id, new._id as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, new.sum_doc, 0 as sum_shipping, "+JOURNAL_REFUND_COLOR_FOR_TRIGGER+" as color, 1 as isUsed from refunds new left join journal j on j.refund_id=new._id;");
			    db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 3 as iddocdef, 0 as order_id, 0 as payment_id, 0 as refund_id, new._id as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, 0 as sum_doc, 0 as sum_shipping, "+JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER+" as color, 1 as isUsed from distribs new left join journal j on j.distribs_id=new._id;");
			    
			    db.execSQL("delete from journal where isUsed=0");
		    }

            public void Vacuum() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("vacuum");
            }

	}
	
	private DBHelper dbHelper;

	@Override
	public int delete(Uri uri, String where, String[] selectionArgs) {
		switch (sUriMatcher.match(uri))
		{
		case URI_NOMENCLATURE:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(NOMENCLATURE_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(NOMENCLATURE_CONTENT_URI, null);
			return cnt;
		}
		case URI_NOMENCLATURE_HIERARCHY:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(NOMENCLATURE_HIERARCHY_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(NOMENCLATURE_HIERARCHY_CONTENT_URI, null);
			return cnt;
		}
		case URI_PLACES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(PLACES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(PLACES_CONTENT_URI, null);
			return cnt;
		}
		case URI_OCCUPIED_PLACES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(OCCUPIED_PLACES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(OCCUPIED_PLACES_CONTENT_URI, null);
			return cnt;
		}
		case URI_CLIENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(CLIENTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(CLIENTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_AGREEMENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(AGREEMENTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(AGREEMENTS_CONTENT_URI, null);
			return cnt;
		}
        case URI_AGREEMENTS30:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(AGREEMENTS30_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(AGREEMENTS30_CONTENT_URI, null);
            return cnt;
        }
		case URI_SALDO:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(SALDO_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(SALDO_CONTENT_URI, null);
			return cnt;
		}
		case URI_SALDO_EXTENDED:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(SALDO_EXTENDED_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(SALDO_EXTENDED_CONTENT_URI, null);
			return cnt;
		}
		case URI_RESTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(RESTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(RESTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_ORGANIZATIONS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(ORGANIZATIONS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(ORGANIZATIONS_CONTENT_URI, null);
			return cnt;
		}
		case URI_STOCKS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(STOCKS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(STOCKS_CONTENT_URI, null);
			return cnt;
		}
		case URI_AGENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(AGENTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(AGENTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_DISTRIBS_CONTRACTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(DISTRIBS_CONTRACTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(DISTRIBS_CONTRACTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_DISTR_POINTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(DISTR_POINTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(DISTR_POINTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_PRICETYPES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(PRICETYPES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(PRICETYPES_CONTENT_URI, null);
			return cnt;
		}
		case URI_PRICES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(PRICES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(PRICES_CONTENT_URI, null);
			return cnt;
		}
        case URI_PRICES_AGREEMENTS30:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(PRICES_AGREEMENTS30_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(PRICES_AGREEMENTS30_CONTENT_URI, null);
            return cnt;
        }
		case URI_CLIENTS_PRICE:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(CLIENTS_PRICE_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(CLIENTS_PRICE_CONTENT_URI, null);
			return cnt;
		}
		case URI_CURATORS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(CURATORS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(CURATORS_CONTENT_URI, null);
			return cnt;
		}
		case URI_CURATORS_PRICE:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(CURATORS_PRICE_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(CURATORS_PRICE_CONTENT_URI, null);
			return cnt;
		}
		case URI_SIMPLE_DISCOUNTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(SIMPLE_DISCOUNTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(SIMPLE_DISCOUNTS_CONTENT_URI, null);
			return cnt;
		}
		case URI_ORDERS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(ORDERS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ORDERS_CONTENT_URI, null);
			    getContext().getContentResolver().notifyChange(ORDERS_JOURNAL_CONTENT_URI, null);
			    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		    }
			return cnt;
		}
		case URI_REFUNDS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(REFUNDS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null);
			    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		    }
			return cnt;
		}
		case URI_DISTRIBS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(DISTRIBS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null);
			    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		    }
			return cnt;
		}
		case URI_ORDERS_SILENT_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(ORDERS_TABLE, where, selectionArgs);
			return cnt;
		}
		case URI_REFUNDS_SILENT_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(REFUNDS_TABLE, where, selectionArgs);
			return cnt;
		}
		case URI_ORDERS_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			// получим _id журнала
			long journal_id=0;
			Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "order_id=?", new String[]{_id}, null, null, null);
			if (cursor.moveToNext())
			{
				journal_id=cursor.getLong(0);
			}
			cursor.close();
		    int cnt=db.delete(ORDERS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(ORDERS_CONTENT_URI, Integer.parseInt(_id)), null);
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(ORDERS_JOURNAL_CONTENT_URI, Integer.parseInt(_id)), null);
			    if (journal_id!=0)
			    {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
			    }
		    }
			return cnt;
		}
		case URI_REFUNDS_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			// получим _id журнала
			long journal_id=0;
			Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "refund_id=?", new String[]{_id}, null, null, null);
			if (cursor.moveToNext())
			{
				journal_id=cursor.getLong(0);
			}
			cursor.close();
		    int cnt=db.delete(REFUNDS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(REFUNDS_CONTENT_URI, Integer.parseInt(_id)), null);
			    if (journal_id!=0)
			    {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
			    }
		    }
			return cnt;
		}
		case URI_DISTRIBS_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			// получим _id журнала
			long journal_id=0;
			Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "distribs_id=?", new String[]{_id}, null, null, null);
			if (cursor.moveToNext())
			{
				journal_id=cursor.getLong(0);
			}
			cursor.close();
		    int cnt=db.delete(DISTRIBS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(DISTRIBS_CONTENT_URI, Integer.parseInt(_id)), null);
			    if (journal_id!=0)
			    {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
			    }
		    }
			return cnt;
		}
		case URI_ORDERS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(ORDERS_LINES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(ORDERS_LINES_CONTENT_URI, null);
			return cnt;
		}
		case URI_REFUNDS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(REFUNDS_LINES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(REFUNDS_LINES_CONTENT_URI, null);
			return cnt;
		}
		case URI_DISTRIBS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(DISTRIBS_LINES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(DISTRIBS_LINES_CONTENT_URI, null);
			return cnt;
		}
		case URI_ORDERS_PLACES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(ORDERS_PLACES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(ORDERS_PLACES_CONTENT_URI, null);
			return cnt;
		}
		case URI_CASH_PAYMENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(CASH_PAYMENTS_CONTENT_URI, null);
			    getContext().getContentResolver().notifyChange(CASH_PAYMENTS_JOURNAL_CONTENT_URI, null);
		    }
			return cnt;
		}
		case URI_CASH_PAYMENTS_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
		    int cnt=db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(CASH_PAYMENTS_CONTENT_URI, Integer.parseInt(_id)), null);
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(CASH_PAYMENTS_JOURNAL_CONTENT_URI, Integer.parseInt(_id)), null);
		    }
			return cnt;
		}
		case URI_VERSIONS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(VERSIONS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(VERSIONS_CONTENT_URI, null);
			return cnt;
		}
		case URI_VERSIONS_SALES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(VERSIONS_SALES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(VERSIONS_SALES_CONTENT_URI, null);
			return cnt;
		}
		case URI_SALES_LOADED:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(SALES_LOADED_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(SALES_LOADED_CONTENT_URI, null);
			return cnt;
		}
		case URI_VICARIOUS_POWER:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(VICARIOUS_POWER_CONTENT_URI, null);
			return cnt;
		}
		case URI_VICARIOUS_POWER_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
		    int cnt=db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(VICARIOUS_POWER_CONTENT_URI, Integer.parseInt(_id)), null);
		    }
			return cnt;
		}
		
		case URI_MESSAGES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(MESSAGES_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null);
		    getContext().getContentResolver().notifyChange(MESSAGES_LIST_CONTENT_URI, null);
			return cnt;
		}
		case URI_MESSAGES_ID:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
		    int cnt=db.delete(MESSAGES_TABLE, where, selectionArgs);
		    if (cnt>0)
		    {
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(MESSAGES_CONTENT_URI, Integer.parseInt(_id)), null);
			    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(MESSAGES_LIST_CONTENT_URI, Integer.parseInt(_id)), null);
		    }
			return cnt;
		}
		case URI_GPS_COORD:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(GPS_COORD_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(GPS_COORD_CONTENT_URI, null);
			return cnt;
		}
		case URI_EQUIPMENT:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(EQUIPMENT_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(EQUIPMENT_CONTENT_URI, null);
			return cnt;
		}
		case URI_EQUIPMENT_RESTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.delete(EQUIPMENT_RESTS_TABLE, where, selectionArgs);		    
		    getContext().getContentResolver().notifyChange(EQUIPMENT_RESTS_CONTENT_URI, null);
			return cnt;
		}
        case URI_ROUTES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(ROUTES_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(ROUTES_CONTENT_URI, null);
            return cnt;
        }
        case URI_ROUTES_LINES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(ROUTES_LINES_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(ROUTES_LINES_CONTENT_URI, null);
            return cnt;
        }
        case URI_ROUTES_DATES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(ROUTES_DATES_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(ROUTES_DATES_CONTENT_URI, null);
            return cnt;
        }
        case URI_REAL_ROUTES_DATES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(REAL_ROUTES_DATES_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(REAL_ROUTES_DATES_CONTENT_URI, null);
            return cnt;
        }
        case URI_REAL_ROUTES_LINES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int cnt=db.delete(REAL_ROUTES_LINES_TABLE, where, selectionArgs);
            getContext().getContentResolver().notifyChange(REAL_ROUTES_LINES_CONTENT_URI, null);
            return cnt;
        }


        default:
            throw new IllegalArgumentException("Unknown URI (for delete)" + uri);
		}
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(LOG_TAG, "insert, " + uri.toString());
		switch (sUriMatcher.match(uri)) {
            case URI_NOMENCLATURE: {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(NOMENCLATURE_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(NOMENCLATURE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(NOMENCLATURE_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_NOMENCLATURE_ID: {
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_NOMENCLATURE_ID, " + id);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long rowID = db.insert(NOMENCLATURE_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_PLACES: {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(PLACES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(PLACES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(PLACES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_OCCUPIED_PLACES: {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "place_id=? and client_id=? and document_id=?";
                String[] selectionArgs = new String[]{values.getAsString("place_id"), values.getAsString("client_id"), values.getAsString("document_id")};

                //Do an update if the constraints match
                db.update(OCCUPIED_PLACES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(OCCUPIED_PLACES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(OCCUPIED_PLACES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_CLIENTS: {
                Log.d(LOG_TAG, "URI_CLIENTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(CLIENTS_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(CLIENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_CLIENTS_ID: {
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "URI_CLIENTS_ID, " + id);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long rowID = db.insert(CLIENTS_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_RESTS: {
                Log.d(LOG_TAG, "URI_RESTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "nomenclature_id=? and stock_id=?";
                String[] selectionArgs = new String[]{values.getAsString("nomenclature_id"), values.getAsString("stock_id")};

                // Тандем
                if (values.containsKey("ogranization_id"))
                {
                    selection = "nomenclature_id=? and stock_id=? and ogranization_id=?";
                    selectionArgs = new String[]{values.getAsString("nomenclature_id"), values.getAsString("stock_id"), values.getAsString("ogranization_id")};
                }

                //Do an update if the constraints match
                db.update(RESTS_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(RESTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(RESTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_SALDO: {
                Log.d(LOG_TAG, "URI_SALDO");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "client_id=?";
                String[] selectionArgs = new String[]{values.getAsString("client_id")};

                //Do an update if the constraints match
                db.update(SALDO_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(SALDO_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(SALDO_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_SALDO_EXTENDED: {
                Log.d(LOG_TAG, "URI_SALDO_EXTENDED");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?";
                String[] selectionArgs = new String[]{values.getAsString("client_id"), values.getAsString("agreement30_id"), values.getAsString("agreement_id"),
                        values.getAsString("document_id"), values.getAsString("manager_id")};

                //Do an update if the constraints match
                db.update(SALDO_EXTENDED_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(SALDO_EXTENDED_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(SALDO_EXTENDED_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_VERSIONS: {
                Log.d(LOG_TAG, "URI_VERSIONS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(VERSIONS_TABLE, values, "param=?", new String[]{values.getAsString("param")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(VERSIONS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(VERSIONS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_VERSIONS_SALES: {
                Log.d(LOG_TAG, "URI_VERSIONS_SALES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(VERSIONS_SALES_TABLE, values, "param=?", new String[]{values.getAsString("param")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(VERSIONS_SALES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(VERSIONS_SALES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_SEANCES_INCOMING: {
                Log.d(LOG_TAG, "URI_SEANCES_INCOMING");
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("PRAGMA synchronous=ON");
                if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                    db.delete(SEANCES_TABLE, null, null);
                    db.insert(SEANCES_TABLE, null, values);
                }
                db.execSQL("PRAGMA synchronous=NORMAL");
                return null;
            }
            case URI_SEANCES_OUTGOING: {
                Log.d(LOG_TAG, "URI_SEANCES_OUTGOING");
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                    db.delete(SEANCES_TABLE, null, null);
                    db.insert(SEANCES_TABLE, null, values);
                }
                db.execSQL("PRAGMA synchronous=ON");
                return null;
            }
            case URI_REINDEX: {
                Log.d(LOG_TAG, "URI_REINDEX");
                dbHelper.Reindex();
                return null;
            }
            case URI_VACUUM: {
                Log.d(LOG_TAG, "URI_VACUUM");
                dbHelper.Vacuum();
                return null;
            }


            case URI_SORT: {
                Log.d(LOG_TAG, "URI_SORT");
                String sortType = values.getAsString("sort_type");
                if (sortType.equals("orders_by_date")) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("drop table if exists temp_old_ids");
                    db.execSQL("CREATE TABLE temp_old_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_old integer)");
                    db.execSQL("drop table if exists temp_new_ids");
                    db.execSQL("CREATE TABLE temp_new_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_new integer)");

                    // сначала в первую таблицу записываем старые коды в старом порядке
                    // distinct на случай, если дублируются коды order_id
                    db.execSQL("insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by journal._id");
                    // а потом во вторую - в новом порядке
                    db.execSQL("insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by ifnull(orders.datedoc, refunds.datedoc);");
                    // количество записей будет совпадать
                    //db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);");
                    db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);");
                    db.execSQL("UPDATE journal set _id = -_id");

                    db.execSQL("drop table if exists temp_old_ids");
                    db.execSQL("drop table if exists temp_new_ids");

                } else if (sortType.equals("orders_by_service_date")) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("drop table if exists temp_old_ids");
                    db.execSQL("CREATE TABLE temp_old_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_old integer)");
                    db.execSQL("drop table if exists temp_new_ids");
                    db.execSQL("CREATE TABLE temp_new_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_new integer)");

                    // сначала в первую таблицу записываем старые коды в старом порядке
                    // distinct на случай, если дублируются коды order_id
                    db.execSQL("insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by journal._id desc");
                    // а потом во вторую - в новом порядке
                    db.execSQL("insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by ifnull(orders.shipping_date, refunds.datedoc) desc;");
                    // количество записей будет совпадать
                    //db.execSQL("UPDATE journal set _id = -(select _id_new from temp_new_ids join temp_old_ids on temp_old_ids._id=temp_new_ids._id where temp_old_ids._id_old=journal._id);");
                    db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);");
                    db.execSQL("UPDATE journal set _id = -_id");

                    db.execSQL("drop table if exists temp_old_ids");
                    db.execSQL("drop table if exists temp_new_ids");
                }
                getContext().getContentResolver().notifyChange(ORDERS_CONTENT_URI, null);
                getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);


                return null;
            }


            case URI_CREATE_VIEWS: {
                // TODO отбор по торговым точкам
                // а вообще salesVB нигде не используется уже
                // вместо него rests_sales_stuff
                Log.d(LOG_TAG, "URI_CREATE_VIEWS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Продажи в телефоне (текущие)
                db.execSQL("drop view if exists salesVB");
                db.execSQL("CREATE VIEW salesVB as select " +
                        "nomenclature_id, " +
                        "sum(quantity) as quantity_saled from orders " +
                        "join ordersLines on ordersLines.order_id=orders._id and orders.client_id=\"" + values.getAsString("client_id") + "\" " +
                        "where orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                        "group by nomenclature_id;");

                // Продажи в 1С (история)
                db.execSQL("drop view if exists salesV");
                // То же самое, для окна ввода количества
                //db.execSQL("drop view if exists salesGroupsV");
			/*
			db.execSQL("CREATE VIEW salesV as select " +
					"nomenclature_id,"+
					"sum(quantity) as quantity_saled "+
					"from salesloaded "+
					"where client_id=\""+values.getAsString("client_id")+"\" "+
					"group by nomenclature_id;");
		    */

                db.execSQL("CREATE VIEW salesV as select " +
                        "nomenclature_id," +
                        "strQuantity " +
                        "from salesL " +
                        "where client_id=\"" + values.getAsString("client_id") + "\" " +
                        ";");

                db.execSQL("drop view if exists salesV_7");

                db.execSQL("CREATE VIEW salesV_7 as " +
                "select "+
                "nomenclature_id,"+
                "sum(case when datedoc between '"+values.getAsString("dt1b")+"' and '"+values.getAsString("dt1e")+"' then quantity else 0 end) quantity7_1,"+
                        "sum(case when datedoc between '"+values.getAsString("dt2b")+"' and '"+values.getAsString("dt2e")+"' then quantity else 0 end) quantity7_2,"+
                        "sum(case when datedoc between '"+values.getAsString("dt3b")+"' and '"+values.getAsString("dt3e")+"' then quantity else 0 end) quantity7_3,"+
                        "sum(case when datedoc between '"+values.getAsString("dt4b")+"' and '"+values.getAsString("dt4e")+"' then quantity else 0 end) quantity7_4 "+
                        "from "+
                    "("+
                            "select "+
                    "nomenclature_id,"+
                            "datedoc,"+
                "case when ifnull(sum(quantity_server), 0)>=ifnull(sum(quantity_pda), 0) then ifnull(sum(quantity_server), 0) else sum(quantity_pda) end quantity "+
                    "from"+
                            "("+
                                    "select nomenclature_id, substr(datedoc, 1, 8) datedoc, quantity quantity_server, 0 quantity_pda from salesL2 "+
                                    // Оставим только группы
                                    "join nomenclature n on n.id=nomenclature_id and n.isFolder=1 "+
                                    "where client_id='"+values.getAsString("client_id")+"' and datedoc between '"+values.getAsString("dtb")+"' and '"+values.getAsString("dte")+"' "+
                    "union all "+
                    "select n.parent_id nomenclature_id, substr(datedoc, 1, 8) datedoc, 0 quantity_server, quantity quantity_pda from orders "+
                    "join ordersLines on ordersLines.order_id=orders._id "+
                    "join nomenclature n on n.id=ordersLines.nomenclature_id "+
                    "where datedoc between '"+values.getAsString("dtb") +"' and '"+values.getAsString("dte")+"' and orders.client_id='"+values.getAsString("client_id")+"' "+
                    "and orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                    ") sales0 "+
                    "group by nomenclature_id, datedoc"+
                    ") sales "+
                    "group by nomenclature_id"+
                    ";");

                    return null;
            }

            case URI_CREATE_SALES_L: {
                Log.d(LOG_TAG, "URI_RECALC_SALES_IN_LINE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.execSQL("PRAGMA synchronous=NORMAL");
                db.beginTransaction();

                db.execSQL("delete from salesL");
                db.execSQL("delete from salesL2");

                // Заполнение salesL
                //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, distr_point_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, distr_point_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
                Cursor cursor = db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
                // '    45   '
                //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id where s.nomenclature_id='    45   ' group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
                //
                int nomenclatureIdIndex = cursor.getColumnIndex("nomenclature_id");
                int clientIdIndex = cursor.getColumnIndex("client_id");
                //int distrPointIdIndex=cursor.getColumnIndex("distr_point_id");
                //int datedocIndex=cursor.getColumnIndex("datedoc");
                int quantityIndex = cursor.getColumnIndex("quantity");
                String prevNomenclatureId = "";
                String prevClientId = "";
                //String prevDistrPointId="";
                String strQuantity = "";
                int cnt = 0;
                double quantityTotal = 0.0;

                try {

                    while (cursor.moveToNext()) {
                        String nomenclatureId = cursor.getString(nomenclatureIdIndex);
                        String clientId = cursor.getString(clientIdIndex);
                        //String distrPointId=cursor.getString(distrPointIdIndex);
                        //String datedoc=cursor.getString(datedocIndex);
                        double quantity = cursor.getDouble(quantityIndex);
                        //if (!prevNomenclatureId.equals(nomenclatureId)||!(prevClientId.equals(clientId))||!(prevDistrPointId.equals(distrPointId)))
                        if (!prevNomenclatureId.equals(nomenclatureId) || !(prevClientId.equals(clientId))) {
                            if (quantityTotal > 0.001) {
                                ContentValues cv = new ContentValues();
                                cv.put("nomenclature_id", prevNomenclatureId);
                                cv.put("client_id", prevClientId);
                                //cv.put("distr_point_id", prevDistrPointId);
                                cv.put("strQuantity", strQuantity);
                                cv.put("quantity", quantityTotal);
                                //cv.put("quantity_now", 0.0);

                                //db.insert(SALES_L_TABLE, null, cv);
                                db.insertWithOnConflict(SALES_L_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                            }

                            prevNomenclatureId = nomenclatureId;
                            prevClientId = clientId;
                            //prevDistrPointId=distrPointId;

                            quantityTotal = 0.0;
                            strQuantity = "";
                            cnt = 0;
                        }
                        if (quantity > 0.001) {
                            quantityTotal += quantity;
                            if (strQuantity.isEmpty()) {
                                strQuantity = Common.DoubleToStringFormat(quantity, "%.3f");
                            } else {
                                cnt++;
                                if (cnt == 4)
                                    strQuantity += "+";
                                else if (cnt < 4)
                                    strQuantity += "+" + Common.DoubleToStringFormat(quantity, "%.3f");
                            }
                        }
                    }

                    if (quantityTotal > 0.001) {
                        ContentValues cv = new ContentValues();
                        cv.put("nomenclature_id", prevNomenclatureId);
                        cv.put("client_id", prevClientId);
                        //cv.put("distr_point_id", prevDistrPointId);
                        cv.put("quantity", quantityTotal);
                        cv.put("strQuantity", strQuantity);

                        //db.insert(SALES_L_TABLE, null, cv);
                        db.insertWithOnConflict(SALES_L_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                    }

                    db.setTransactionSuccessful();
                    getContext().getContentResolver().notifyChange(uri, null);

                } catch (SQLException e) {
                    Log.e(LOG_TAG, e.toString());
                } finally {
                    db.endTransaction();
                }
                cursor.close();

                // Заполнение salesL2
                // если торговые точки не используются, то везде будет одно и то же значение, и группировка от этого не пострадает
                db.execSQL("insert into salesL2 select " +
                        "null as _id," +
                        "case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id," +
                        "client_id," +
                        // 20.09.2019
                        "null as distr_point_id," +
                        //
                        "datedoc," +
                        "numdoc," +
                        "sum(quantity) as quantity," +
                        "sum(price) as price " +
                        "from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id " +
                        "group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, distr_point_id;");

                return null;
            }

            case URI_PRICETYPES: {
                Log.d(LOG_TAG, "URI_PRICETYPES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(PRICETYPES_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(PRICETYPES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(PRICETYPES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_AGREEMENTS: {
                Log.d(LOG_TAG, "URI_AGREEMENTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(AGREEMENTS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(AGREEMENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(AGREEMENTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_AGREEMENTS30: {
                Log.d(LOG_TAG, "URI_AGREEMENTS30");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(AGREEMENTS30_TABLE, values, "id=? and owner_id=?", new String[]{values.getAsString("id"), values.getAsString("owner_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(AGREEMENTS30_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(AGREEMENTS30_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_STOCKS: {
                Log.d(LOG_TAG, "URI_STOCKS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(STOCKS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(STOCKS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(STOCKS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_PRICES: {
                Log.d(LOG_TAG, "URI_PRICES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(PRICES_TABLE, values, "nomenclature_id=? and price_type_id=?", new String[]{values.getAsString("nomenclature_id"), values.getAsString("price_type_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(PRICES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(PRICES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_PRICES_AGREEMENTS30: {
                Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(PRICES_AGREEMENTS30_TABLE, values, "agreement30_id=? and nomenclature_id=?", new String[]{values.getAsString("agreement30_id"), values.getAsString("nomenclature_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(PRICES_AGREEMENTS30_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(PRICES_AGREEMENTS30_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_CLIENTS_PRICE: {
                Log.d(LOG_TAG, "URI_CLIENTS_PRICE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(CLIENTS_PRICE_TABLE, values, "client_id=? and nomenclature_id=?", new String[]{values.getAsString("client_id"), values.getAsString("nomenclature_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(CLIENTS_PRICE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(CLIENTS_PRICE_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_CURATORS_PRICE: {
                Log.d(LOG_TAG, "URI_CURATORS_PRICE");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(CURATORS_PRICE_TABLE, values, "curator_id=? and nomenclature_id=?", new String[]{values.getAsString("curator_id"), values.getAsString("nomenclature_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(CURATORS_PRICE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(CURATORS_PRICE_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }

            case URI_SIMPLE_DISCOUNTS: {
                Log.d(LOG_TAG, "URI_SIMPLE_DISCOUNTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(SIMPLE_DISCOUNTS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(SIMPLE_DISCOUNTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(SIMPLE_DISCOUNTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }

            case URI_AGENTS: {
                Log.d(LOG_TAG, "URI_AGENTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(AGENTS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(AGENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(AGENTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_CURATORS: {
                Log.d(LOG_TAG, "URI_CURATORS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(CURATORS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(CURATORS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(CURATORS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_DISTR_POINTS: {
                Log.d(LOG_TAG, "URI_DISTR_POINTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(DISTR_POINTS_TABLE, values, "id=? and owner_id=?", new String[]{values.getAsString("id"), values.getAsString("owner_id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(DISTR_POINTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(DISTR_POINTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_ORGANIZATIONS: {
                Log.d(LOG_TAG, "URI_ORGANIZATIONS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(ORGANIZATIONS_TABLE, values, "id=?", new String[]{values.getAsString("id")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(ORGANIZATIONS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(ORGANIZATIONS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_ORDERS_SILENT: {
                Log.d(LOG_TAG, "URI_ORDERS_SILENT");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                // с 19.02.2018
                db.update(ORDERS_TABLE, values, "uid=? and editing_backup=?", new String[]{values.getAsString("uid"), values.getAsString("editing_backup")});
                // до
                //db.update(ORDERS_TABLE, values, "uid=?", new String[] {values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(ORDERS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID);

                return resultUri;
            }
            case URI_ORDERS: {
                Log.d(LOG_TAG, "URI_ORDERS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(ORDERS_TABLE, values, "uid=? and editing_backup=?", new String[]{values.getAsString("uid"), values.getAsString("editing_backup")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(ORDERS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(ORDERS_JOURNAL_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                // получим _id журнала
                long journal_id = 0;
                Cursor cursor = db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "order_id=?", new String[]{String.valueOf(rowID)}, null, null, null);
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0);
                    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
                }
                cursor.close();
                //db.close();
                return resultUri;
            }
            case URI_ORDERS_LINES: {
                Log.d(LOG_TAG, "URI_ORDERS_LINES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(ORDERS_LINES_TABLE, null, values);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(ORDERS_LINES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(ORDERS_LINES_COMPLEMENTED_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }


            case URI_REFUNDS_SILENT: {
                Log.d(LOG_TAG, "URI_REFUNDS_SILENT");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(REFUNDS_TABLE, values, "uid=?", new String[]{values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(REFUNDS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID);

                return resultUri;
            }
            case URI_REFUNDS: {
                Log.d(LOG_TAG, "URI_REFUNDS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(REFUNDS_TABLE, values, "uid=?", new String[]{values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(REFUNDS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                // получим _id журнала
                long journal_id = 0;
                Cursor cursor = db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "refund_id=?", new String[]{String.valueOf(rowID)}, null, null, null);
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0);
                    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
                }
                cursor.close();
                //db.close();
                return resultUri;
            }
            case URI_REFUNDS_LINES: {
                Log.d(LOG_TAG, "URI_REFUNDS_LINES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(REFUNDS_LINES_TABLE, null, values);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(REFUNDS_LINES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(REFUNDS_LINES_COMPLEMENTED_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_DISTRIBS_LINES: {
                Log.d(LOG_TAG, "URI_DISTRIBS_LINES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(DISTRIBS_LINES_TABLE, null, values);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(DISTRIBS_LINES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }

            case URI_CASH_PAYMENTS: {
                Log.d(LOG_TAG, "URI_CASH_PAYMENTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(CASH_PAYMENTS_TABLE, values, "uid=?", new String[]{values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(CASH_PAYMENTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_JOURNAL_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_VICARIOUS_POWER: {
                //Log.d(LOG_TAG, "URI_MESSAGES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(VICARIOUS_POWER_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(VICARIOUS_POWER_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(VICARIOUS_POWER_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_MESSAGES: {
                //Log.d(LOG_TAG, "URI_MESSAGES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "uid=?";
                String[] selectionArgs = new String[]{values.getAsString("uid")};

                //Do an update if the constraints match
                db.update(MESSAGES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(MESSAGES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                resultUri = ContentUris.withAppendedId(MESSAGES_LIST_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_SALES: {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("drop table if exists sales;");
                // Продажи каждому клиенту
                db.execSQL("create table sales as " +
                        "select ordersLines.client_id as client_id, nomenclature_id, sum(quantity) as quantity_saled from ordersLines join orders on orders._id=ordersLines.order_id and datedoc>=? group by ordersLines.client_id, nomenclature_id" +
                        ";", new String[]{values.getAsString("datebegin")});
                // Общие продажи (клиент=null)
                db.execSQL("insert into sales " +
                        "select null as client_id, nomenclature_id, sum(quantity_saled) as quantity_saled from sales group by nomenclature_id" +
                        ";");
			/*
			if (values.containsKey("datebegin"))
			{
				
			}
			*/
                // insert into xxx
                // select * from yyyy;

                return null;
            }
            // Больше не используется
		/*
		case URI_RESTSV:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.execSQL("DROP VIEW IF EXISTS RestsV;");
			db.execSQL("CREATE VIEW RestsV as select " +
	    		"nomenclature_id,"+
	    		"sum(quantity) as quantity,"+
	    		"sum(quantity_reserve) as quantity_reserve "+ 
	    		"from rests "+(values.containsKey("stock_id")?"where stock_id=\""+values.getAsString("stock_id")+"\"":"")+
	    		"group by nomenclature_id;");
			return null;
		}
    	*/
            case URI_RESTS_SALES_STUFF: {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS rests_sales_stuff;");
                //db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature");
                //db.execSQL("INSERT INTO RestsSalesStuff select * from temp_Nomenclature");

                db.execSQL("CREATE TABLE rests_sales_stuff AS " +
                        "SELECT nomenclature_id, sum(quantity) as quantity, sum(quantity_reserve) as quantity_reserve, sum(saled) as saledNow " +
                        "FROM ( " +
                        "SELECT nomenclature_id, quantity, quantity_reserve, 0 as saled " +
                        "FROM rests " + (values.containsKey("stock_id") ? "where stock_id=\"" + values.getAsString("stock_id") + "\" " : "") +
                        // У тандема разделение остатков по организациям
                        // проверяется, есть ли stock_id, чтобы написать where или and
                        ((values.containsKey("organization_id")&&!values.containsKey("stock_id")) ? " where organization_id=\"" + values.getAsString("organization_id") + "\" " : "") +
                        ((values.containsKey("organization_id")&&values.containsKey("stock_id")) ? " and organization_id=\"" + values.getAsString("organization_id") + "\" " : "") +
                        //
                        (values.containsKey("client_id") ?
                                "UNION ALL " +
                                        "SELECT nomenclature_id, 0, 0, quantity as saled " +
                                        "FROM orders join ordersLines on ordersLines.order_id=orders._id " +
                                        "WHERE orders.client_id=\"" + values.getAsString("client_id") + "\" " +
                                        (values.containsKey("distr_point_id") ? " and distr_point_id=\"" + values.getAsString("distr_point_id") + "\" " : "") +
                                        // TODO остальные состояния
                                        "and orders.state in (" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + ") " : "") +
                        //
                        ") in1 " +
                        "GROUP BY nomenclature_id");

                db.execSQL("CREATE INDEX sales_nomenclature_id ON rests_sales_stuff (nomenclature_id ASC);");

                return null;
            }
            case URI_DISCOUNTS_STUFF_MEGA:
            case URI_DISCOUNTS_STUFF_SIMPLE:
            case URI_DISCOUNTS_STUFF_OTHER: {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS discounts_stuff;");
                db.execSQL("DROP TABLE IF EXISTS temp_discounts_stuff;");

                //"_id integer primary key autoincrement, " +
                //"client_id text," +
                //"nomenclature_id text," +
                //"priceProcent double," +
                //"priceAdd double," +

                // добавлено 26.02.2018
                boolean bFastMode = false;
                //

                if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_MEGA) {
                    db.execSQL("CREATE TEMPORARY TABLE temp_discounts_stuff AS " +
                            // добавляем группы
                            "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                            "from nomenclature_hierarchy h join curators_price p on " + (values.containsKey("curator_id") ? "curator_id=\"" + values.getAsString("curator_id") + "\" " : "0=1 ") +
                            "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +
                            // и номенклатуру отдельно, уровень ставим 100
                            "union all " +
                            "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                            "from nomenclature_hierarchy h join curators_price p on " + (values.containsKey("curator_id") ? "curator_id=\"" + values.getAsString("curator_id") + "\" " : "0=1 ") +
                            "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                            ";");
                } else
                    //if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART)
                    if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_SIMPLE) {
                        // Тут простейший (обычный для 1С) вариант, скидка от номенклатуры не зависит
                        // а только от договора, но номенклатуру все равно выберем
                        // будет в итоге при работе соединение с той же самой таблицей
                        // группы нам здесь не нужны, только элементы
                        db.execSQL("CREATE TEMPORARY TABLE temp_discounts_stuff AS " +
                                "select _id p_id, id nomenclature_id, 100 level, " + values.getAsString("priceProcent") + " priceProcent, 0 priceAdd " +
                                "from nomenclature where isFolder<>1 " +
                                ";");

                        bFastMode = true;
                    } else {
                        db.execSQL("CREATE TEMPORARY TABLE temp_discounts_stuff AS " +
                                // добавляем группы
                                "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join clients_price p on " + (values.containsKey("client_id") ? "client_id=\"" + values.getAsString("client_id") + "\" " : "0=1 ") +
                                "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +
                                // и номенклатуру отдельно, уровень ставим 100
                                "union all " +
                                "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join clients_price p on " + (values.containsKey("client_id") ? "client_id=\"" + values.getAsString("client_id") + "\" " : "0=1 ") +
                                "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                                ";");
                    }

                if (bFastMode) {
                    // Просто копируем эту таблицу
                    db.execSQL("CREATE TABLE discounts_stuff AS " +
                            "select nomenclature_id, p_id, level, priceProcent, priceAdd from temp_discounts_stuff;");
                } else {
                    // заполним level максимальным совпавшим значением для номенклатуры, id - чтобы появилась колонка
                    db.execSQL("CREATE TABLE discounts_stuff AS " +
                            "select nomenclature_id, max(p_id) p_id, max(level) level, 0.0 as priceProcent, 0.0 as priceAdd from temp_discounts_stuff group by nomenclature_id;");
                    // находим _id из clients_prict
                    db.execSQL("UPDATE discounts_stuff set p_id = (select p_id from temp_discounts_stuff t2 where t2.nomenclature_id=discounts_stuff.nomenclature_id and t2.level=discounts_stuff.level);");
                    // и все остальное
                    db.execSQL("UPDATE discounts_stuff set " +
                            "priceProcent = (select priceProcent from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id)," +
                            "priceAdd = (select priceAdd from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id);");
                }

                db.execSQL("CREATE INDEX discounts_nomenclature_id ON discounts_stuff (nomenclature_id ASC);");

                return null;
            }

            case URI_PRICESV_MEGA:
            case URI_PRICESV_OTHER: {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("DROP VIEW IF EXISTS PricesV;");
                //if (Common.MEGA)
                if (sUriMatcher.match(uri) == URI_PRICESV_MEGA) {
                    String priceName = "rozn_price";
                    String price_type_id = values.getAsString("price_type_id");
                    if (price_type_id.equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value()))) {
                        priceName = "m_opt_price";
                    } else if (price_type_id.equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_OPT.value()))) {
                        priceName = "opt_price";
                    }
                    db.execSQL("CREATE VIEW PricesV as select " +
                            "id as nomenclature_id," +
                            "edizm_1_id as ed_izm_id," +
                            "quant_1 as edIzm," +
                            priceName + " as price," +
                            "0 as priceProcent," +
                            "quant_k_1 as k " +
                            "from nomenclature " +
                            ";");

                } else
		    /*
			if (Common.TITAN)
			{
				// Тут простейший (обычный для 1С) вариант, скидка от номенклатуры не зависит
				// а только от договора, но номенклатуру все равно выберем
				// будет в итоге при работе соединение с той же самой таблицей
				db.execSQL("CREATE VIEW PricesV as select " +
			    		"nomenclature_id,"+
						"ed_izm_id,"+
			    		"edIzm,"+
			    		"price,"+
			    		"priceProcent,"+
			    		"k "+
			    		"from prices where price_type_id=\""+values.getAsString("price_type_id")+"\""+
			    		";");
			} else
			*/
                    if (values.containsKey("agreement30_id")) {
                        // это Common.FACTORY
                        db.execSQL("CREATE VIEW PricesV as select " +
                                "ifnull(pa30.nomenclature_id, prices.nomenclature_id) nomenclature_id," +
                                "ifnull(pa30.ed_izm_id, prices.ed_izm_id) ed_izm_id," +
                                "ifnull(pa30.edIzm, prices.edIzm) edIzm," +
                                "ifnull(pa30.price, prices.price) price," +
                                "priceProcent," + // на самом деле это лишнее поле, оно не используется
                                "ifnull(pa30.k, prices.k) k " +
                                "from prices" +
                                // вообще тут нужен FULL JOIN, но он не поддерживается
                                " left join prices_agreements30 pa30 on pa30.agreement30_id=\"" + values.getAsString("agreement30_id") + "\"" +
                                " and pa30.nomenclature_id=prices.nomenclature_id" +
                                " where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                ";");

                    } else {
                        db.execSQL("CREATE VIEW PricesV as select " +
                                "nomenclature_id," +
                                "ed_izm_id," +
                                "edIzm," +
                                "price," +
                                "priceProcent," +
                                "k " +
                                "from prices where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                ";");
                    }
                return null;
            }
            case URI_SETTINGS: {
                Log.d(LOG_TAG, "URI_SETTINGS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                int cnt = db.update(SETTINGS_TABLE, values, null, null);
                if (cnt <= 0) {
                    db.insert(SETTINGS_TABLE, null, values);
                }
                return null;
            }
            case URI_SALES_LOADED: {
                Log.d(LOG_TAG, "URI_SALES_LOADED");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(SALES_LOADED_TABLE, values, "nomenclature_id=? and refdoc=?", new String[]{values.getAsString("nomenclature_id"), values.getAsString("refdoc")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(SALES_LOADED_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(SALES_LOADED_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_NOMENCLATURE_HIERARCHY: {
                Log.d(LOG_TAG, "NOMENCLATURE_HIERARCHY");
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long rowID = db.insertWithOnConflict(NOMENCLATURE_HIERARCHY_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                Uri resultUri = ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
            case URI_GPS_COORD: {
                Log.d(LOG_TAG, "URI_GPS_COORD");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // вообще можно просто добавлять, дублирования кода не будет
                //Do an update if the constraints match
                db.update(GPS_COORD_TABLE, values, "datecoord=?", new String[]{values.getAsString("datecoord")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(GPS_COORD_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                return null;
            }
            case URI_DISTRIBS_CONTRACTS: {
                //Log.d(LOG_TAG, "URI_DISTRIBS_CONTRACTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(DISTRIBS_CONTRACTS_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(DISTRIBS_CONTRACTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(DISTRIBS_CONTRACTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_DISTRIBS: {
                Log.d(LOG_TAG, "URI_DISTRIBS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                //Do an update if the constraints match
                db.update(DISTRIBS_TABLE, values, "uid=?", new String[]{values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(DISTRIBS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                Uri resultUri = ContentUris.withAppendedId(DISTRIBS_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);
                // получим _id журнала
                long journal_id = 0;
                Cursor cursor = db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "distribs_id=?", new String[]{String.valueOf(rowID)}, null, null, null);
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0);
                    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
                }
                cursor.close();
                //db.close();
                return resultUri;
            }
            case URI_EQUIPMENT: {
                //Log.d(LOG_TAG, "URI_EQUIPMENT");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(EQUIPMENT_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(EQUIPMENT_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(EQUIPMENT_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }
            case URI_EQUIPMENT_RESTS: {
                Log.d(LOG_TAG, "URI_EQUIPMENT_RESTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "client_id=? and agreement_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?";
                String[] selectionArgs = new String[]{values.getAsString("client_id"), values.getAsString("agreement_id"), values.getAsString("nomenclature_id"), values.getAsString("distr_point_id"), values.getAsString("doc_id")};

                //Do an update if the constraints match
                db.update(EQUIPMENT_RESTS_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(EQUIPMENT_RESTS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(EQUIPMENT_RESTS_CONTENT_URI, rowID);
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext().getContentResolver().notifyChange(resultUri, null);
                //db.close();
                return resultUri;
            }

            case URI_MTRADE_LOG: {
                Log.d(LOG_TAG, "URI_MTRADE_LOG");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(MTRADE_LOG_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(MTRADE_LOG_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_PERMISSIONS_REQUESTS: {
                Log.d(LOG_TAG, "URI_PERMISSIONS_REQUESTS");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(PERMISSIONS_REQUESTS_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(PERMISSIONS_REQUESTS_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_ROUTES:
            {
                Log.d(LOG_TAG, "URI_ROUTES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "id=?";
                String[] selectionArgs = new String[]{values.getAsString("id")};

                //Do an update if the constraints match
                db.update(ROUTES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(ROUTES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(ROUTES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_ROUTES_LINES:
            {
                Log.d(LOG_TAG, "URI_ROUTES_LINES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(ROUTES_LINES_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(ROUTES_LINES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_ROUTES_DATES:
            {
                Log.d(LOG_TAG, "URI_ROUTES_DATES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "route_date=?";
                String[] selectionArgs = new String[]{values.getAsString("route_date")};

                //Do an update if the constraints match
                db.update(ROUTES_DATES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(ROUTES_DATES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(ROUTES_DATES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_REAL_ROUTES_DATES:
            {
                Log.d(LOG_TAG, "URI_ROUTES_DATES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                String selection = "route_date=?";
                String[] selectionArgs = new String[]{values.getAsString("route_date")};

                //Do an update if the constraints match
                db.update(REAL_ROUTES_DATES_TABLE, values, selection, selectionArgs);

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                long rowID = db.insertWithOnConflict(REAL_ROUTES_DATES_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                Uri resultUri = ContentUris.withAppendedId(REAL_ROUTES_DATES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

            case URI_REAL_ROUTES_LINES:
            {
                Log.d(LOG_TAG, "URI_ROUTES_LINES");
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                long rowID = db.insert(REAL_ROUTES_LINES_TABLE, null, values);
                Uri resultUri = ContentUris.withAppendedId(REAL_ROUTES_LINES_CONTENT_URI, rowID);
                getContext().getContentResolver().notifyChange(resultUri, null);

                return resultUri;
            }

        default:
            throw new IllegalArgumentException("Unknown URI (for insert) " + uri);
		}
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values){
	    int numInserted = 0;
	    
		switch (sUriMatcher.match(uri)) {
		case URI_PRICES:
		{
			Log.d(LOG_TAG, "URI_PRICES_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(PRICES_TABLE, cv, "nomenclature_id=? and price_type_id=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("price_type_id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(PRICES_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
        case URI_PRICES_AGREEMENTS30:
        {
            Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30_BULK_INSERT");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("PRAGMA synchronous=NORMAL");
            db.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(PRICES_AGREEMENTS30_TABLE, cv, "agreement30_id=? and nomenclature_id=?", new String[] {cv.getAsString("agreement30_id"), cv.getAsString("nomenclature_id")});

                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    long rowID = db.insertWithOnConflict(PRICES_AGREEMENTS30_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            //db.close();
            return numInserted;
        }

		case URI_SALDO:
		{
			Log.d(LOG_TAG, "URI_SALDO_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(SALDO_TABLE, cv, "client_id=?", new String[] {cv.getAsString("client_id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(SALDO_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_SALDO_EXTENDED:
		{
			Log.d(LOG_TAG, "URI_SALDO_EXTENDED_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(SALDO_EXTENDED_TABLE, cv, "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?",
				    		new String[] {cv.getAsString("client_id"), cv.getAsString("agreement30_id"), cv.getAsString("agreement_id"),
						    		cv.getAsString("document_id"), cv.getAsString("manager_id")});
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(SALDO_EXTENDED_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_AGREEMENTS:
		{
			Log.d(LOG_TAG, "URI_AGREEMENTS_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(AGREEMENTS_TABLE, cv, "id=?", new String[] {cv.getAsString("id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(AGREEMENTS_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
        case URI_AGREEMENTS30:
        {
            Log.d(LOG_TAG, "URI_AGREEMENTS30_BULK_INSERT");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("PRAGMA synchronous=NORMAL");
            db.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(AGREEMENTS30_TABLE, cv, "id=? and owner_id=?", new String[] {cv.getAsString("id"), cv.getAsString("owner_id")});

                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    long rowID = db.insertWithOnConflict(AGREEMENTS30_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            //db.close();
            return numInserted;
        }

		case URI_RESTS:
		{
			Log.d(LOG_TAG, "URI_RESTS_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
                    // Тандем
                    if (cv.containsKey("organization_id"))
                        db.update(RESTS_TABLE, cv, "nomenclature_id=? and stock_id=? and organization_id=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("stock_id"), cv.getAsString("organization_id")});
                    else
                        db.update(RESTS_TABLE, cv, "nomenclature_id=? and stock_id=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("stock_id")});
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(RESTS_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}


        case URI_ROUTES:
        {
            Log.d(LOG_TAG, "URI_ROUTES_BULK_INSERT");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("PRAGMA synchronous=NORMAL");
            db.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    db.update(ROUTES_TABLE, cv, "id=?", new String[] {cv.getAsString("id")});
                    long rowID = db.insertWithOnConflict(ROUTES_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            return numInserted;
        }

        case URI_ROUTES_LINES:
        {
            Log.d(LOG_TAG, "URI_ROUTES_LINES_BULK_INSERT");
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("PRAGMA synchronous=NORMAL");
            db.beginTransaction();
            try {
                for (ContentValues cv : values) {
                    db.update(ROUTES_LINES_TABLE, cv, "route_id=? and lineno=?", new String[] {cv.getAsString("route_id"), cv.getAsString("lineno")});
                    long rowID = db.insertWithOnConflict(ROUTES_LINES_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                numInserted = values.length;
            } finally {
                db.endTransaction();
            }
            return numInserted;
        }

		case URI_SALES_LOADED:
		{
			Log.d(LOG_TAG, "URI_SALES_LOADED_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(SALES_LOADED_TABLE, cv, "nomenclature_id=? and refdoc=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("refdoc")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(SALES_LOADED_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		
		case URI_NOMENCLATURE:
		{
			Log.d(LOG_TAG, "URI_NOMENCLATURE_BULK_INSERT");
		    SQLiteDatabase db = dbHelper.getWritableDatabase();
		    
		    /*
		    SQLiteStatement stmt = db.compileStatement("SELECT max(_id) as _id FROM nomenclature WHERE id = ?");
		    
	    	InsertHelper ih = new InsertHelper(db, NOMENCLATURE_TABLE);
	    	
	    	final int c__id = ih.getColumnIndex("_id");
	    	final int c_id = ih.getColumnIndex("id");
	    	final int c_isFolder = ih.getColumnIndex("isFolder");
	    	final int c_parent_id = ih.getColumnIndex("parent_id");
	    	final int c_code = ih.getColumnIndex("code");
	    	final int c_descr = ih.getColumnIndex("descr");
	    	final int c_descrFull = ih.getColumnIndex("descrFull");
	    	final int c_quant_1 = ih.getColumnIndex("quant_1");
	    	final int c_quant_2 = ih.getColumnIndex("quant_2");
	    	final int c_edizm_1_id = ih.getColumnIndex("edizm_1_id");
	    	final int c_edizm_2_id = ih.getColumnIndex("edizm_2_id");
	    	final int c_quant_k_1 = ih.getColumnIndex("quant_k_1");
	    	final int c_quant_k_2 = ih.getColumnIndex("quant_k_2");
	    	final int c_opt_price = ih.getColumnIndex("opt_price");
	    	final int c_m_opt_price = ih.getColumnIndex("m_opt_price");
	    	final int c_rozn_price = ih.getColumnIndex("rozn_price");
	    	final int c_incom_price = ih.getColumnIndex("incom_price");
	    	final int c_IsInPrice = ih.getColumnIndex("IsInPrice");
	    	final int c_flagWithoutDiscont = ih.getColumnIndex("flagWithoutDiscont");
	    	final int c_weight_k_1 = ih.getColumnIndex("weight_k_1");
	    	final int c_weight_k_2 = ih.getColumnIndex("weight_k_2");	    	
		    */
	    	// http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(NOMENCLATURE_TABLE, cv, "id=?", new String[] {cv.getAsString("id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(NOMENCLATURE_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }
		    	/*
		    	for (ContentValues cv : values) {
		    		
				    stmt.bindString(1, cv.getAsString("id"));
				    Long _idValue = stmt.simpleQueryForLong();
		    		
				    if (_idValue>0)
				    {
				    	ih.prepareForReplace();
				    	ih.bind(c__id, _idValue);
				    } else
				    {
				    	ih.prepareForInsert();
				    }

		    		ih.bind(c_id, cv.getAsString("id"));
		    		ih.bind(c_isFolder, cv.getAsString("isFolder"));
		    		ih.bind(c_parent_id, cv.getAsString("parent_id"));
		    		ih.bind(c_code, cv.getAsString("code"));
		    		ih.bind(c_descr, cv.getAsString("descr"));
		    		ih.bind(c_descrFull, cv.getAsString("descrFull"));
		    		ih.bind(c_quant_1, cv.getAsString("quant_1"));
		    		ih.bind(c_quant_2, cv.getAsString("quant_2"));
		    		ih.bind(c_edizm_1_id, cv.getAsString("edizm_1_id"));
		    		ih.bind(c_edizm_2_id, cv.getAsString("edizm_2_id"));
		    		ih.bind(c_quant_k_1, cv.getAsDouble("quant_k_1"));
		    		ih.bind(c_quant_k_2, cv.getAsDouble("quant_k_2"));
		    		ih.bind(c_opt_price, cv.getAsDouble("opt_price"));
		    		ih.bind(c_m_opt_price, cv.getAsDouble("m_opt_price"));
		    		ih.bind(c_rozn_price, cv.getAsDouble("rozn_price"));
		    		ih.bind(c_incom_price, cv.getAsString("incom_price"));
		    		ih.bind(c_IsInPrice, cv.getAsLong("IsInPrice"));
		    		ih.bind(c_flagWithoutDiscont, cv.getAsLong("flagWithoutDiscont"));
		    		ih.bind(c_weight_k_1, cv.getAsDouble("weight_k_1"));
		    		ih.bind(c_weight_k_2, cv.getAsDouble("weight_k_2"));
		    		
		    		ih.execute();
		    	}
		    	*/
		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}

		case URI_NOMENCLATURE_HIERARCHY:
		{
			Log.d(LOG_TAG, "URI_NOMENCLATURE_HIERARCHY_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    //long rowID = db.insertWithOnConflict(NOMENCLATURE_HIERARCHY_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);		    
		    //Uri resultUri = ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID);
		    //getContext().getContentResolver().notifyChange(resultUri, null);
			//return resultUri;
			
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		    	for (ContentValues cv : values) {
				    long rowID = db.insertWithOnConflict(NOMENCLATURE_HIERARCHY_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        }
		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		
		case URI_CLIENTS:
		{
			Log.d(LOG_TAG, "URI_CLIENTS_BULK_INSERT");
		    SQLiteDatabase db = dbHelper.getWritableDatabase();
		    
	    	// http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(CLIENTS_TABLE, cv, "id=?", new String[] {cv.getAsString("id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(CLIENTS_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_DISTR_POINTS:
		{
			Log.d(LOG_TAG, "URI_DISTR_POINTS_BULK_INSERT");
		    SQLiteDatabase db = dbHelper.getWritableDatabase();
		    
	    	// http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(DISTR_POINTS_TABLE, cv, "id=? and owner_id=?", new String[] {cv.getAsString("id"), cv.getAsString("owner_id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(DISTR_POINTS_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_CLIENTS_PRICE:
		{
			Log.d(LOG_TAG, "URI_CLIENTS_PRICE_BULK_INSERT");
		    SQLiteDatabase db = dbHelper.getWritableDatabase();
		    
	    	// http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(CLIENTS_PRICE_TABLE, cv, "nomenclature_id=? and client_id=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("client_id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(CLIENTS_PRICE_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_CURATORS_PRICE:
		{
			Log.d(LOG_TAG, "URI_CURATORS_PRICE_BULK_INSERT");
		    SQLiteDatabase db = dbHelper.getWritableDatabase();
		    
	    	// http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
		            //long newID = sqlDB.insertOrThrow(table, null, cv);
		 		    //Do an update if the constraints match
				    db.update(CURATORS_PRICE_TABLE, cv, "nomenclature_id=? and curator_id=?", new String[] {cv.getAsString("nomenclature_id"), cv.getAsString("curator_id")});		    
				    
		 		    //This will return the id of the newly inserted row if no conflict
				    //It will also return the offending row without modifying it if in conflict
				    long rowID = db.insertWithOnConflict(CURATORS_PRICE_TABLE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_ORDERS_LINES:
		{
			Log.d(LOG_TAG, "URI_ORDERS_LINES_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
				    long rowID = db.insert(ORDERS_LINES_TABLE, null, cv);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_REFUNDS_LINES:
		{
			Log.d(LOG_TAG, "URI_REFUNDS_LINES_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
				    long rowID = db.insert(REFUNDS_LINES_TABLE, null, cv);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		case URI_ORDERS_PLACES:
		{
			Log.d(LOG_TAG, "URI_ORDERS_PLACES_BULK_INSERT");
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    db.execSQL("PRAGMA synchronous=NORMAL");		    
		    db.beginTransaction();
		    try {
		        for (ContentValues cv : values) {
				    long rowID = db.insert(ORDERS_PLACES_TABLE, null, cv);		    
		        	
		            //if (rowID <= 0) {
		            //    throw new SQLException("Failed to insert row into " + uri);
		            //}
		        }

		        db.setTransactionSuccessful();
		        getContext().getContentResolver().notifyChange(uri, null);
		        numInserted = values.length;
		    } finally {         
		        db.endTransaction();
		    }
		    //db.close();
		    return numInserted;
		}
		
		default:
			return super.bulkInsert(uri, values);
		}
		
	}	

	@Override
	public boolean onCreate() {
		dbHelper = new DBHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, 
			String sortOrder) {
	    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    
	    //String groupBy=null;
        boolean bAppendIdToSelection=false;
        String appendIdPreffix="";
 
        switch (sUriMatcher.match(uri)) {
            case URI_ORDERS_ID:
                bAppendIdToSelection=true;
            case URI_ORDERS:
                qb.setTables(ORDERS_TABLE);
                qb.setProjectionMap(ordersProjectionMap);
                break;
            case URI_CASH_PAYMENTS_ID:
                bAppendIdToSelection=true;
            case URI_CASH_PAYMENTS:
                qb.setTables(CASH_PAYMENTS_TABLE);
                qb.setProjectionMap(cashPaymentsProjectionMap);
                break;
            case URI_REFUNDS_ID:
                bAppendIdToSelection=true;
            case URI_REFUNDS:
                qb.setTables(REFUNDS_TABLE);
                qb.setProjectionMap(refundsProjectionMap);
                break;
            case URI_JOURNAL_ID:
                bAppendIdToSelection=true;
                appendIdPreffix="journal.";
            case URI_JOURNAL:
                qb.setTables(JOURNAL_TABLE);
                qb.setProjectionMap(journalProjectionMap);
                break;
            case URI_CLIENTS_ID:
                bAppendIdToSelection=true;
            case URI_CLIENTS:
                qb.setTables(CLIENTS_TABLE);
                qb.setProjectionMap(clientsProjectionMap);
            	break;
            case URI_NOMENCLATURE_ID:
                bAppendIdToSelection=true;
            case URI_NOMENCLATURE:
                qb.setTables(NOMENCLATURE_TABLE);
                qb.setProjectionMap(nomenclatureProjectionMap);
            	break;
            case URI_NOMENCLATURE_HIERARCHY:
                qb.setTables(NOMENCLATURE_HIERARCHY_TABLE);
                qb.setProjectionMap(nomenclatureHierarchyProjectionMap);
            	break;
            case URI_NOMENCLATURE_LIST:
            	//if (Common.MEGA)
            	//{
            	//	qb.setTables(NOMENCLATURE_LIST_TABLE_MEGA);
                //    qb.setProjectionMap(nomenclatureListProjectionMapMega);
            	//} else
            	//{
            		qb.setTables(NOMENCLATURE_LIST_TABLE);
                    qb.setProjectionMap(nomenclatureListProjectionMap);
            	//}
                //groupBy="_id, descr";
            	/*
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                //SQLiteDatabase db = dbHelper.getWritableDatabase();

                //ContentValues cv = new ContentValues();
                
                Log.d(LOG_TAG, " --- onQuery --- ");
                
        	    //cv.clear();
        	    //cv.put("numdoc", "0001");
        	    //cv.put("datedoc", "20130101");
        	    //db.insert("orders", null, cv);
                
                //String s=qb.buildQuery(projection, selection, selectionArgs, groupBy, null, sortOrder, null);
                
                Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                // просим ContentResolver уведомлять этот курсор 
                // об изменениях данных в CONTACT_CONTENT_URI
                c.setNotificationUri(getContext().getContentResolver(), uri);
                //db.close();
                return c;
            	*/
                
                /*
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("drop table if exists sales");
                db.execSQL("drop table if exists rests_total");
                db.execSQL("select 1 as x into sales");
                db.execSQL("select 1 as x into rests_total");
                
                //Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                //c.setNotificationUri(getContext().getContentResolver(), uri);
                //return c;
                break;
                */
            	break;
            //case URI_NOMENCLATURE_LIST_STOCK:
            //    qb.setTables(NOMENCLATURE_LIST_STOCK_TABLE);
            //    qb.setProjectionMap(nomenclatureListStockProjectionMap);
            //    break;

            case URI_NOMENCLATURE_SURFING:

                qb.setTables(NOMENCLATURE_SURFING_TABLE);
                qb.setProjectionMap(nomenclatureSurfingProjectionMap);
                // Хорошие примеры, как можно сделать union в запросе
                // https://coderoad.ru/27280721/Использование-метода-SQLiteQueryBuilder-buildUnionSubQuery
                // https://www.codota.com/code/java/methods/android.database.sqlite.SQLiteQueryBuilder/buildUnionSubQuery
                break;

            case URI_RESTS_ID:
                bAppendIdToSelection=true;
            case URI_RESTS:
                qb.setTables(RESTS_TABLE);
                qb.setProjectionMap(restsProjectionMap);
            	break;
            case URI_AGREEMENTS_ID:
                bAppendIdToSelection=true;
            case URI_AGREEMENTS:
                qb.setTables(AGREEMENTS_TABLE);
                qb.setProjectionMap(agreementsProjectionMap);
            	break;
            case URI_AGREEMENTS30_ID:
                bAppendIdToSelection=true;
            case URI_AGREEMENTS30:
                qb.setTables(AGREEMENTS30_TABLE);
                qb.setProjectionMap(agreements30ProjectionMap);
                break;
            case URI_VERSIONS:
                qb.setTables(VERSIONS_TABLE);
                qb.setProjectionMap(versionsProjectionMap);
            	break;
            case URI_VERSIONS_SALES:
                qb.setTables(VERSIONS_SALES_TABLE);
                qb.setProjectionMap(versionsSalesProjectionMap);
            	break;
            case URI_SEANCES:
                qb.setTables(SEANCES_TABLE);
                qb.setProjectionMap(seancesProjectionMap);
            	break;
            case URI_PRICETYPES:
                qb.setTables(PRICETYPES_TABLE);
                qb.setProjectionMap(pricetypesProjectionMap);
            	break;
            case URI_SALDO:
                qb.setTables(SALDO_TABLE);
                qb.setProjectionMap(saldoProjectionMap);
            	break;
            case URI_SALDO_EXTENDED_ID:
                bAppendIdToSelection=true;
            case URI_SALDO_EXTENDED:
                qb.setTables(SALDO_EXTENDED_TABLE);
                qb.setProjectionMap(saldoExtendedProjectionMap);
            	break;
            case URI_SALDO_EXTENDED_JOURNAL:
                qb.setTables(SALDO_EXTENDED_JOURNAL_TABLE);
                qb.setProjectionMap(saldoExtendedJournalProjectionMap);
            	break;
            case URI_STOCKS:
                qb.setTables(STOCKS_TABLE);
                qb.setProjectionMap(stocksProjectionMap);
            	break;
            case URI_PRICES:
                qb.setTables(PRICES_TABLE);
                qb.setProjectionMap(pricesProjectionMap);
            	break;
            case URI_PRICES_AGREEMENTS30:
                qb.setTables(PRICES_AGREEMENTS30_TABLE);
                qb.setProjectionMap(pricesAgreements30ProjectionMap);
                break;
            case URI_CLIENTS_PRICE:
                qb.setTables(CLIENTS_PRICE_TABLE);
                qb.setProjectionMap(clientsPriceProjectionMap);
            	break;
            case URI_CURATORS_PRICE:
                qb.setTables(CURATORS_PRICE_TABLE);
                qb.setProjectionMap(curatorsPriceProjectionMap);
            	break;
            case URI_SIMPLE_DISCOUNTS_ID:
                bAppendIdToSelection=true;
            case URI_SIMPLE_DISCOUNTS:
                qb.setTables(SIMPLE_DISCOUNTS_TABLE);
                qb.setProjectionMap(simpleDiscountsProjectionMap);
            	break;
            case URI_AGENTS_ID:
                bAppendIdToSelection=true;
            case URI_AGENTS:
                qb.setTables(AGENTS_TABLE);
                qb.setProjectionMap(agentsProjectionMap);
            	break;
            case URI_CURATORS_ID:
                bAppendIdToSelection=true;
            case URI_CURATORS:
                qb.setTables(CURATORS_TABLE);
                qb.setProjectionMap(curatorsProjectionMap);
            	break;
            case URI_CURATORS_LIST:
                qb.setTables(CURATORS_LIST_TABLE);
                qb.setProjectionMap(curatorsListProjectionMap);
            	break;
            case URI_DISTR_POINTS_ID:
                bAppendIdToSelection=true;
            case URI_DISTR_POINTS:
                qb.setTables(DISTR_POINTS_TABLE);
                qb.setProjectionMap(distrPointsProjectionMap);
            	break;
            case URI_DISTR_POINTS_LIST:
                qb.setTables(DISTR_POINTS_LIST_TABLE);
                qb.setProjectionMap(distrPointsListProjectionMap);
            	break;
            case URI_ORGANIZATIONS_ID:
                bAppendIdToSelection=true;
            case URI_ORGANIZATIONS:
                qb.setTables(ORGANIZATIONS_TABLE);
                qb.setProjectionMap(organizationsProjectionMap);
            	break;
            case URI_CLIENTS_WITH_SALDO:
                qb.setTables(CLIENTS_WITH_SALDO_TABLE);
                qb.setProjectionMap(clientsWithSaldoProjectionMap);
                break;
            case URI_ORDERS_LINES:
                qb.setTables(ORDERS_LINES_TABLE);
                qb.setProjectionMap(ordersLinesProjectionMap);
                break;
            case URI_REFUNDS_LINES:
                qb.setTables(REFUNDS_LINES_TABLE);
                qb.setProjectionMap(refundsLinesProjectionMap);
                break;
            case URI_ORDERS_JOURNAL:
                qb.setTables(ORDERS_JOURNAL_TABLE);
                qb.setProjectionMap(ordersJournalProjectionMap);
                break;
            case URI_CASH_PAYMENTS_JOURNAL:
                qb.setTables(CASH_PAYMENTS_JOURNAL_TABLE);
                qb.setProjectionMap(cashPaymentsJournalProjectionMap);
                break;
            case URI_AGREEMENTS_LIST:
                qb.setTables(AGREEMENTS_LIST_TABLE);
                qb.setProjectionMap(agreementsListProjectionMap);
                break;
            case URI_AGREEMENTS30_LIST:
                qb.setTables(AGREEMENTS30_LIST_TABLE);
                qb.setProjectionMap(agreements30ListProjectionMap);
                break;
            case URI_AGREEMENTS_WITH_SALDO_ONLY_LIST:
                qb.setTables(AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE);
                qb.setProjectionMap(agreementsListWithSaldoOnlyProjectionMap);
                break;
            case URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST:
                qb.setTables(AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE);
                qb.setProjectionMap(agreements30ListWithSaldoOnlyProjectionMap);
                break;
            case URI_ORDERS_LINES_COMPLEMENTED:
                qb.setTables(ORDERS_LINES_COMPLEMENTED_TABLE);
                qb.setProjectionMap(ordersLinesComplementedProjectionMap);
                break;
            case URI_REFUNDS_LINES_COMPLEMENTED:
                qb.setTables(REFUNDS_LINES_COMPLEMENTED_TABLE);
                qb.setProjectionMap(refundsLinesComplementedProjectionMap);
                break;
            case URI_VICARIOUS_POWER_ID:
                bAppendIdToSelection=true;
            case URI_VICARIOUS_POWER:
                qb.setTables(VICARIOUS_POWER_TABLE);
                qb.setProjectionMap(vicariousPowerProjectionMap);
                break;
            case URI_MESSAGES:
                qb.setTables(MESSAGES_TABLE);
                qb.setProjectionMap(messagesProjectionMap);
                break;
            case URI_MESSAGES_LIST:
                qb.setTables(MESSAGES_LIST_TABLE);
                qb.setProjectionMap(messagesListProjectionMap);
                break;
            case URI_SETTINGS:
                qb.setTables(SETTINGS_TABLE);
                qb.setProjectionMap(settingsProjectionMap);
            	break;
            case URI_SALES_LOADED:
                qb.setTables(SALES_LOADED_TABLE);
                qb.setProjectionMap(salesLoadedProjectionMap);
            	break;
            //case URI_SALES_LOADED_WITH_COMMON_GROUPS:
            //    qb.setTables(SALES_LOADED_WITH_COMMON_GROUPS_TABLE);
            //    qb.setProjectionMap(salesLoadedWithCommonGroupsProjectionMap);
            //	break;
            case URI_SALES_L2:
                qb.setTables(SALES_L2_TABLE);
                qb.setProjectionMap(salesL2ProjectionMap);
            	break;
            case URI_PLACES:
                qb.setTables(PLACES_TABLE);
                qb.setProjectionMap(placesProjectionMap);
            	break;
            case URI_OCCUPIED_PLACES:
                qb.setTables(OCCUPIED_PLACES_TABLE);
                qb.setProjectionMap(occupiedPlacesProjectionMap);
            	break;
            case URI_ORDERS_PLACES:
                qb.setTables(ORDERS_PLACES_TABLE);
                qb.setProjectionMap(ordersPlacesProjectionMap);
            	break;
            case URI_ORDERS_PLACES_LIST:
                qb.setTables(ORDERS_PLACES_LIST_TABLE);
                qb.setProjectionMap(ordersPlacesListProjectionMap);
            	break;
            case URI_GPS_COORD:
                qb.setTables(GPS_COORD_TABLE);
                qb.setProjectionMap(gpsCoordProjectionMap);
            	break;
            case URI_DISTRIBS_CONTRACTS:
                qb.setTables(DISTRIBS_CONTRACTS_TABLE);
                qb.setProjectionMap(distribsContractsProjectionMap);
            	break;
            case URI_DISTRIBS_CONTRACTS_LIST:
                qb.setTables(DISTRIBS_CONTRACTS_LIST_TABLE);
                qb.setProjectionMap(distribsContractsListProjectionMap);
            	break;
            case URI_DISTRIBS_LINES:
                qb.setTables(DISTRIBS_LINES_TABLE);
                qb.setProjectionMap(distribsLinesProjectionMap);
            	break;
            case URI_DISTRIBS_LINES_COMPLEMENTED:
                qb.setTables(DISTRIBS_LINES_COMPLEMENTED_TABLE);
                qb.setProjectionMap(distribsLinesComplementedProjectionMap);
            	break;
            case URI_DISTRIBS_ID:
                bAppendIdToSelection=true;
            case URI_DISTRIBS:
                qb.setTables(DISTRIBS_TABLE);
                qb.setProjectionMap(distribsProjectionMap);
            	break;
            case URI_EQUIPMENT:
                qb.setTables(EQUIPMENT_TABLE);
                qb.setProjectionMap(equipmentProjectionMap);
            	break;
            case URI_EQUIPMENT_RESTS:
                qb.setTables(EQUIPMENT_RESTS_TABLE);
                qb.setProjectionMap(equipmentRestsProjectionMap);
            	break;
            case URI_EQUIPMENT_RESTS_LIST:
                qb.setTables(EQUIPMENT_RESTS_LIST_TABLE);
                qb.setProjectionMap(equipmentRestsListProjectionMap);
            	break;
            case URI_MTRADE_LOG:
                qb.setTables(MTRADE_LOG_TABLE);
                qb.setProjectionMap(mtradeLogProjectionMap);
            	break;

            case URI_PERMISSIONS_REQUESTS:
                qb.setTables(PERMISSIONS_REQUESTS_TABLE);
                qb.setProjectionMap(permissionsRequestsProjectionMap);
                break;

            case URI_ROUTES:
                qb.setTables(ROUTES_TABLE);
                qb.setProjectionMap(routesProjectionMap);
                break;

            case URI_ROUTES_LINES:
                qb.setTables(ROUTES_LINES_TABLE);
                qb.setProjectionMap(routesLinesProjectionMap);
                break;

            case URI_ROUTES_DATES_ID:
                bAppendIdToSelection=true;
            case URI_ROUTES_DATES:
                qb.setTables(ROUTES_DATES_TABLE);
                qb.setProjectionMap(routesDatesProjectionMap);
                break;

            case URI_ROUTES_DATES_LIST_ID:
                bAppendIdToSelection=true;
                appendIdPreffix="routes_dates.";
            case URI_ROUTES_DATES_LIST:
                qb.setTables(ROUTES_DATES_LIST_TABLE);
                qb.setProjectionMap(routesDatesListProjectionMap);
                break;
            case URI_REAL_ROUTES_DATES_ID:
                bAppendIdToSelection=true;
            case URI_REAL_ROUTES_DATES:
                qb.setTables(REAL_ROUTES_DATES_TABLE);
                qb.setProjectionMap(realRoutesDatesProjectionMap);
                break;
            case URI_REAL_ROUTES_LINES:
                qb.setTables(REAL_ROUTES_LINES_TABLE);
                qb.setProjectionMap(realRoutesLinesProjectionMap);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI (for select) " + uri);
        }

        if (bAppendIdToSelection) {
            if (selection==null||selection.isEmpty())
                selection = appendIdPreffix+"_id = " + uri.getLastPathSegment();
            else
                selection = selection + " and "+appendIdPreffix+"_id = " + uri.getLastPathSegment();
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        //ContentValues cv = new ContentValues();
        
        Log.d(LOG_TAG, " --- onQuery --- ");
        
	    //cv.clear();
	    //cv.put("numdoc", "0001");
	    //cv.put("datedoc", "20130101");
	    //db.insert("orders", null, cv);
        
        //String s=qb.buildQuery(projection, selection, selectionArgs, groupBy, null, sortOrder, null);

        /*
        Cursor c1;
        try
        {
        	c1 = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        } catch (SQLException e)
        {
        	Log.e(LOG_TAG, e.toString());
        }
        */
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // просим ContentResolver уведомлять этот курсор 
        // об изменениях данных в CONTACT_CONTENT_URI
        c.setNotificationUri(getContext().getContentResolver(), uri);
        //db.close();
        return c;
    }

	@Override
	public int update(Uri uri, ContentValues cv, String where, String[] selectionArgs) {
		int numUpdated = 0;		
		switch (sUriMatcher.match(uri))
		{
		case URI_ORDERS_ID:
		{
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update orders set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update orders set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(ORDERS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(ORDERS_CONTENT_URI, Integer.parseInt(_id)), null);
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(ORDERS_JOURNAL_CONTENT_URI, Integer.parseInt(_id)), null);
				    
					long journal_id=0;
					Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "order_id=?", new String[]{_id}, null, null, null);
					if (cursor.moveToNext())
					{
						journal_id=cursor.getLong(0);
					    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
					}
					cursor.close();
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_ORDERS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update orders set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update orders set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(ORDERS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(ORDERS_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(ORDERS_JOURNAL_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_ORDERS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(ORDERS_LINES_TABLE, cv, where, selectionArgs);
		}
		// с 19.02.2018, когда стали использовать новую технологию резервного сохранения
		case URI_ORDERS_SILENT:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			return db.update(ORDERS_TABLE, cv, where, selectionArgs);		    
		}
		//
		case URI_REFUNDS_ID:
		{
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update refunds set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update refunds set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(REFUNDS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(REFUNDS_CONTENT_URI, Integer.parseInt(_id)), null);
				    
					long journal_id=0;
					Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "refund_id=?", new String[]{_id}, null, null, null);
					if (cursor.moveToNext())
					{
						journal_id=cursor.getLong(0);
					    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
					}
					cursor.close();
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_REFUNDS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update refunds set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update refunds set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(REFUNDS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}		
		case URI_REFUNDS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(REFUNDS_LINES_TABLE, cv, where, selectionArgs);
		}
		case URI_DISTRIBS_ID:
		{
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update distribs set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update distribs set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(DISTRIBS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(DISTRIBS_CONTENT_URI, Integer.parseInt(_id)), null);
				    
					long journal_id=0;
					Cursor cursor=db.query(JOURNAL_TABLE, new String[]{"journal._id"}, "distribs_id=?", new String[]{_id}, null, null, null);
					if (cursor.moveToNext())
					{
						journal_id=cursor.getLong(0);
					    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(JOURNAL_CONTENT_URI, journal_id), null);
					}
					cursor.close();
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_DISTRIBS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update distribs set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update distribs set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(DISTRIBS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}		
		case URI_DISTRIBS_LINES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(DISTRIBS_LINES_TABLE, cv, where, selectionArgs);
		}
		
		case URI_NOMENCLATURE:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(NOMENCLATURE_TABLE, cv, where, selectionArgs);
		}
		case URI_CLIENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(CLIENTS_TABLE, cv, where, selectionArgs);
		}
		case URI_PRICETYPES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(PRICETYPES_TABLE, cv, where, selectionArgs);
		}
		case URI_PRICES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(PRICES_TABLE, cv, where, selectionArgs);
		}
        case URI_PRICES_AGREEMENTS30:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(PRICES_AGREEMENTS30_TABLE, cv, where, selectionArgs);
        }
		case URI_SALDO:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(SALDO_TABLE, cv, where, selectionArgs);
		}
		case URI_SALDO_EXTENDED:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(SALDO_EXTENDED_TABLE, cv, where, selectionArgs);
		}
		case URI_RESTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(RESTS_TABLE, cv, where, selectionArgs);
		}
		case URI_SIMPLE_DISCOUNTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(SIMPLE_DISCOUNTS_TABLE, cv, where, selectionArgs);
		}
		case URI_SETTINGS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    int cnt=db.update(SETTINGS_TABLE, cv, where, selectionArgs);
		    if (cnt==0)
		    {
		    	db.insert(SETTINGS_TABLE, null, cv);
		    	cnt=1;
		    }
		    return cnt;
		}
		case URI_PLACES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(PLACES_TABLE, cv, where, selectionArgs);
		}
		case URI_OCCUPIED_PLACES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(OCCUPIED_PLACES_TABLE, cv, where, selectionArgs);
		}
		case URI_MESSAGES_ID:
		{
			String _id=uri.getLastPathSegment();
			if (where==null||where.isEmpty())
			{
				where="_id="+_id;
			} else
			{
				where+=" and _id="+_id;
			}
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (cv.containsKey("isMark")&&cv.getAsInteger("isMark")==1)
				{
					// Версию увеличивать не нужно, это мы помечаем на удаление
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update messages set isMarkCnt=isMarkCnt+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						db.execSQL("update messages set isMarkCnt=isMarkCnt+1");
					}
				} else
				{
					if (!cv.containsKey("ver"))
					{
						// если версия не записывается, увеличим ее на единицу
						// (скопировано из заказа, возможно не требуется)
						if (where!=null&&!where.isEmpty())
						{
							db.execSQL("update messages set ver=ver+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
						} else
						{
							// на самом деле такого не будет
							db.execSQL("update messages set ver=ver+1");
						}
					}
				}
		        numUpdated=db.update(MESSAGES_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(MESSAGES_CONTENT_URI, Integer.parseInt(_id)), null);
				    getContext().getContentResolver().notifyChange(ContentUris.withAppendedId(MESSAGES_LIST_CONTENT_URI, Integer.parseInt(_id)), null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_MESSAGES:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (cv.containsKey("isMark")&&cv.getAsInteger("isMark")==1)
				{
					// Версию увеличивать не нужно, это мы помечаем на удаление
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update messages set isMarkCnt=isMarkCnt+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						db.execSQL("update messages set isMarkCnt=isMarkCnt+1");
					}
				} else
				{
					if (!cv.containsKey("ver"))
					{
						// если версия не записывается, увеличим ее на единицу
						if (where!=null&&!where.isEmpty())
						{
							db.execSQL("update messages set ver=ver+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
						} else
						{
							// на самом деле такого не будет
							db.execSQL("update messages set ver=ver+1");
						}
					}
				}
		        numUpdated=db.update(MESSAGES_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(MESSAGES_LIST_CONTENT_URI, null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		
		case URI_CASH_PAYMENTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.beginTransaction();
			try
			{
				if (!cv.containsKey("versionPDA"))
				{
					// если версия не записывается, увеличим ее на единицу
					if (where!=null&&!where.isEmpty())
					{
						db.execSQL("update cash_payments set versionPDA=versionPDA+1 where "+where, selectionArgs==null?new String[]{}:selectionArgs);
					} else
					{
						// на самом деле такого не будет
						db.execSQL("update cash_payments set versionPDA=versionPDA+1");
					}
				}
		        numUpdated=db.update(CASH_PAYMENTS_TABLE, cv, where, selectionArgs);
		        if (numUpdated>0)
		        {
				    getContext().getContentResolver().notifyChange(CASH_PAYMENTS_CONTENT_URI, null);
				    getContext().getContentResolver().notifyChange(CASH_PAYMENTS_JOURNAL_CONTENT_URI, null);
		        }
		        db.setTransactionSuccessful();
		    } finally {         
		        db.endTransaction();
		    }
			return numUpdated;
		}
		case URI_GPS_COORD:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(GPS_COORD_TABLE, cv, where, selectionArgs);
		}
		case URI_DISTRIBS_CONTRACTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(DISTRIBS_CONTRACTS_TABLE, cv, where, selectionArgs);
		}
		case URI_EQUIPMENT:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(EQUIPMENT_TABLE, cv, where, selectionArgs);
		}
		case URI_EQUIPMENT_RESTS:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(EQUIPMENT_RESTS_TABLE, cv, where, selectionArgs);
		}
		case URI_MTRADE_LOG:
		{
			SQLiteDatabase db = dbHelper.getWritableDatabase();
		    return db.update(MTRADE_LOG_TABLE, cv, where, selectionArgs);
		}
        case URI_ROUTES_DATES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(ROUTES_DATES_TABLE, cv, where, selectionArgs);
        }
        case URI_ROUTES_LINES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(ROUTES_LINES_TABLE, cv, where, selectionArgs);
        }
        case URI_REAL_ROUTES_DATES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(REAL_ROUTES_DATES_TABLE, cv, where, selectionArgs);
        }
        case URI_REAL_ROUTES_LINES:
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            return db.update(REAL_ROUTES_LINES_TABLE, cv, where, selectionArgs);
        }
        default:
            throw new IllegalArgumentException("Unknown URI (for update)" + uri);
		}
	}
	
	static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, ORDERS_PATH, URI_ORDERS);
        sUriMatcher.addURI(AUTHORITY, ORDERS_PATH + "/#", URI_ORDERS_ID);
        sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_PATH, URI_CASH_PAYMENTS);
        sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_PATH + "/#", URI_CASH_PAYMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, REFUNDS_PATH, URI_REFUNDS);
        sUriMatcher.addURI(AUTHORITY, REFUNDS_PATH + "/#", URI_REFUNDS_ID);
        sUriMatcher.addURI(AUTHORITY, JOURNAL_PATH, URI_JOURNAL);
        sUriMatcher.addURI(AUTHORITY, JOURNAL_PATH + "/#", URI_JOURNAL_ID);
        sUriMatcher.addURI(AUTHORITY, CLIENTS_PATH, URI_CLIENTS);
        sUriMatcher.addURI(AUTHORITY, CLIENTS_PATH + "/#", URI_CLIENTS_ID);
        sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_PATH, URI_NOMENCLATURE);
        sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_PATH + "/#", URI_NOMENCLATURE_ID);
        sUriMatcher.addURI(AUTHORITY, RESTS_PATH, URI_RESTS);
        sUriMatcher.addURI(AUTHORITY, RESTS_PATH + "/#", URI_RESTS_ID);
        sUriMatcher.addURI(AUTHORITY, SALDO_PATH, URI_SALDO);
        sUriMatcher.addURI(AUTHORITY, SALDO_PATH + "/#", URI_SALDO_ID);
        sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_PATH, URI_SALDO_EXTENDED);
        sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_PATH + "/#", URI_SALDO_EXTENDED_ID);
        sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_JOURNAL_PATH, URI_SALDO_EXTENDED_JOURNAL);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS_PATH, URI_AGREEMENTS);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS_PATH + "/#", URI_AGREEMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_PATH, URI_AGREEMENTS30);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_PATH + "/#", URI_AGREEMENTS30_ID);
        sUriMatcher.addURI(AUTHORITY, VERSIONS_PATH, URI_VERSIONS);
        sUriMatcher.addURI(AUTHORITY, VERSIONS_PATH + "/#", URI_VERSIONS_ID);
        sUriMatcher.addURI(AUTHORITY, PRICETYPES_PATH, URI_PRICETYPES);
        sUriMatcher.addURI(AUTHORITY, PRICETYPES_PATH + "/#", URI_PRICETYPES_ID);
        sUriMatcher.addURI(AUTHORITY, STOCKS_PATH, URI_STOCKS);
        sUriMatcher.addURI(AUTHORITY, STOCKS_PATH + "/#", URI_STOCKS_ID);
        sUriMatcher.addURI(AUTHORITY, PRICES_PATH, URI_PRICES);
        sUriMatcher.addURI(AUTHORITY, PRICES_PATH + "/#", URI_PRICES_ID);
        sUriMatcher.addURI(AUTHORITY, PRICES_AGREEMENTS30_PATH, URI_PRICES_AGREEMENTS30);
        sUriMatcher.addURI(AUTHORITY, AGENTS_PATH, URI_AGENTS);
        sUriMatcher.addURI(AUTHORITY, AGENTS_PATH + "/#", URI_AGENTS_ID);
        sUriMatcher.addURI(AUTHORITY, CURATORS_PATH, URI_CURATORS);
        sUriMatcher.addURI(AUTHORITY, CURATORS_PATH + "/#", URI_CURATORS_ID);
        sUriMatcher.addURI(AUTHORITY, CURATORS_LIST_PATH, URI_CURATORS_LIST);
        sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_PATH, URI_DISTR_POINTS);
        sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_PATH + "/#", URI_DISTR_POINTS_ID);
        sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_LIST_PATH, URI_DISTR_POINTS_LIST);
        sUriMatcher.addURI(AUTHORITY, ORGANIZATIONS_PATH, URI_ORGANIZATIONS);
        sUriMatcher.addURI(AUTHORITY, ORGANIZATIONS_PATH + "/#", URI_ORGANIZATIONS_ID);
        sUriMatcher.addURI(AUTHORITY, CLIENTS_WITH_SALDO_PATH, URI_CLIENTS_WITH_SALDO);
        sUriMatcher.addURI(AUTHORITY, ORDERS_LINES_PATH, URI_ORDERS_LINES);
        sUriMatcher.addURI(AUTHORITY, REFUNDS_LINES_PATH, URI_REFUNDS_LINES);
        sUriMatcher.addURI(AUTHORITY, ORDERS_JOURNAL_PATH, URI_ORDERS_JOURNAL);
        sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_JOURNAL_PATH, URI_CASH_PAYMENTS_JOURNAL);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS_LIST_PATH, URI_AGREEMENTS_LIST);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_LIST_PATH, URI_AGREEMENTS30_LIST);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH, URI_AGREEMENTS_WITH_SALDO_ONLY_LIST);
        sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH, URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST);
        sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH, URI_SIMPLE_DISCOUNTS);
        sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH + "/#", URI_SIMPLE_DISCOUNTS_ID);
        sUriMatcher.addURI(AUTHORITY, ORDERS_LINES_COMPLEMENTED_PATH, URI_ORDERS_LINES_COMPLEMENTED);
        sUriMatcher.addURI(AUTHORITY, REFUNDS_LINES_COMPLEMENTED_PATH, URI_REFUNDS_LINES_COMPLEMENTED);
        sUriMatcher.addURI(AUTHORITY, SEANCES_PATH, URI_SEANCES);
        sUriMatcher.addURI(AUTHORITY, SEANCES_INCOMING_PATH, URI_SEANCES_INCOMING);
        sUriMatcher.addURI(AUTHORITY, SEANCES_OUTGOING_PATH, URI_SEANCES_OUTGOING);
        sUriMatcher.addURI(AUTHORITY, REINDEX_PATH, URI_REINDEX);
        sUriMatcher.addURI(AUTHORITY, VACUUM_PATH, URI_VACUUM);
        sUriMatcher.addURI(AUTHORITY, SORT_PATH, URI_SORT);
        sUriMatcher.addURI(AUTHORITY, VICARIOUS_POWER_PATH, URI_VICARIOUS_POWER);
        sUriMatcher.addURI(AUTHORITY, VICARIOUS_POWER_PATH + "/#", URI_VICARIOUS_POWER_ID);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH, URI_MESSAGES);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH + "/#", URI_MESSAGES_ID);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_LIST_PATH, URI_MESSAGES_LIST);        
        sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_LIST_PATH, URI_NOMENCLATURE_LIST);
        sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_SURFING_PATH, URI_NOMENCLATURE_SURFING);
        sUriMatcher.addURI(AUTHORITY, SALES_PATH, URI_SALES);
        sUriMatcher.addURI(AUTHORITY, RESTS_SALES_STUFF_PATH, URI_RESTS_SALES_STUFF);
        sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_MEGA_PATH, URI_DISCOUNTS_STUFF_MEGA);
        sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_SIMPLE_PATH, URI_DISCOUNTS_STUFF_SIMPLE);
        sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_OTHER_PATH, URI_DISCOUNTS_STUFF_OTHER);
        sUriMatcher.addURI(AUTHORITY, PRICESV_MEGA_PATH, URI_PRICESV_MEGA);
        sUriMatcher.addURI(AUTHORITY, PRICESV_OTHER_PATH, URI_PRICESV_OTHER);
        sUriMatcher.addURI(AUTHORITY, CLIENTS_PRICE_PATH, URI_CLIENTS_PRICE);
        sUriMatcher.addURI(AUTHORITY, CURATORS_PRICE_PATH, URI_CURATORS_PRICE);
        sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH, URI_SIMPLE_DISCOUNTS);
        sUriMatcher.addURI(AUTHORITY, SETTINGS_PATH, URI_SETTINGS);
        sUriMatcher.addURI(AUTHORITY, SALES_LOADED_PATH, URI_SALES_LOADED);
        sUriMatcher.addURI(AUTHORITY, SALES_L2_PATH, URI_SALES_L2);
        sUriMatcher.addURI(AUTHORITY, VERSIONS_SALES_PATH, URI_VERSIONS_SALES);
        sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_HIERARCHY_PATH, URI_NOMENCLATURE_HIERARCHY);
        sUriMatcher.addURI(AUTHORITY, CREATE_VIEWS_PATH, URI_CREATE_VIEWS);
        sUriMatcher.addURI(AUTHORITY, CREATE_SALES_L_PATH, URI_CREATE_SALES_L);
        sUriMatcher.addURI(AUTHORITY, ORDERS_SILENT_PATH, URI_ORDERS_SILENT);
        sUriMatcher.addURI(AUTHORITY, ORDERS_SILENT_PATH + "/#", URI_ORDERS_SILENT_ID);
        
        sUriMatcher.addURI(AUTHORITY, REFUNDS_SILENT_PATH, URI_REFUNDS_SILENT);
        sUriMatcher.addURI(AUTHORITY, REFUNDS_SILENT_PATH + "/#", URI_REFUNDS_SILENT_ID);
        
        sUriMatcher.addURI(AUTHORITY, PLACES_PATH, URI_PLACES);
        sUriMatcher.addURI(AUTHORITY, OCCUPIED_PLACES_PATH, URI_OCCUPIED_PLACES);
        sUriMatcher.addURI(AUTHORITY, ORDERS_PLACES_PATH, URI_ORDERS_PLACES);
        sUriMatcher.addURI(AUTHORITY, ORDERS_PLACES_LIST_PATH, URI_ORDERS_PLACES_LIST);
        
        sUriMatcher.addURI(AUTHORITY, GPS_COORD_PATH, URI_GPS_COORD);
        
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_CONTRACTS_PATH, URI_DISTRIBS_CONTRACTS);
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_CONTRACTS_LIST_PATH, URI_DISTRIBS_CONTRACTS_LIST);
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_LINES_PATH, URI_DISTRIBS_LINES);
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_LINES_COMPLEMENTED_PATH, URI_DISTRIBS_LINES_COMPLEMENTED);
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_PATH, URI_DISTRIBS);
        sUriMatcher.addURI(AUTHORITY, DISTRIBS_PATH + "/#", URI_DISTRIBS_ID);
        
        sUriMatcher.addURI(AUTHORITY, EQUIPMENT_PATH, URI_EQUIPMENT);
        sUriMatcher.addURI(AUTHORITY, EQUIPMENT_RESTS_PATH, URI_EQUIPMENT_RESTS);
        sUriMatcher.addURI(AUTHORITY, EQUIPMENT_RESTS_LIST_PATH, URI_EQUIPMENT_RESTS_LIST);
        
        sUriMatcher.addURI(AUTHORITY, MTRADE_LOG_PATH, URI_MTRADE_LOG);
        sUriMatcher.addURI(AUTHORITY, PERMISSIONS_REQUESTS_PATH, URI_PERMISSIONS_REQUESTS);

        sUriMatcher.addURI(AUTHORITY, ROUTES_PATH, URI_ROUTES);
        sUriMatcher.addURI(AUTHORITY, ROUTES_LINES_PATH, URI_ROUTES_LINES);
        sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_PATH, URI_ROUTES_DATES);
        sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_PATH + "/#", URI_ROUTES_DATES_ID);
        sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_LIST_PATH, URI_ROUTES_DATES_LIST);
        sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_LIST_PATH + "/#", URI_ROUTES_DATES_LIST_ID);

        sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_DATES_PATH, URI_REAL_ROUTES_DATES);
        sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_DATES_PATH + "/#", URI_REAL_ROUTES_DATES_ID);
        sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_LINES_PATH, URI_REAL_ROUTES_LINES);

        ordersProjectionMap = new HashMap<String, String>();
        ordersProjectionMap.put("_id", "_id");
        ordersProjectionMap.put("uid", "uid");
        ordersProjectionMap.put("version", "version");
        ordersProjectionMap.put("version_ack", "version_ack");
        ordersProjectionMap.put("versionPDA", "versionPDA");
        ordersProjectionMap.put("versionPDA_ack", "versionPDA_ack");
        ordersProjectionMap.put("id", "id");
        ordersProjectionMap.put("numdoc", "numdoc");
        ordersProjectionMap.put("datedoc", "datedoc");
        ordersProjectionMap.put("client_id", "client_id");
        ordersProjectionMap.put("agreement30_id", "agreement30_id");
        ordersProjectionMap.put("agreement_id", "agreement_id");
        ordersProjectionMap.put("distr_point_id", "distr_point_id");
        ordersProjectionMap.put("comment", "comment");
        ordersProjectionMap.put("comment_closing", "comment_closing");
        ordersProjectionMap.put("comment_payment", "comment_payment");
        ordersProjectionMap.put("closed_not_full", "closed_not_full");
        ordersProjectionMap.put("state", "state");
        ordersProjectionMap.put("curator_id", "curator_id");
        ordersProjectionMap.put("bw", "bw");
        ordersProjectionMap.put("trade_type", "trade_type");
		ordersProjectionMap.put("datecreation", "datecreation");
        ordersProjectionMap.put("datecoord", "datecoord");
        ordersProjectionMap.put("latitude", "latitude");
        ordersProjectionMap.put("longitude", "longitude");
        ordersProjectionMap.put("gpsstate", "gpsstate");
        ordersProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        ordersProjectionMap.put("gpstype", "gpstype");
        ordersProjectionMap.put("accept_coord", "accept_coord");
        ordersProjectionMap.put("dont_need_send", "dont_need_send");
        ordersProjectionMap.put("price_type_id", "price_type_id");
        ordersProjectionMap.put("stock_id", "stock_id");
        ordersProjectionMap.put("sum_doc", "sum_doc");
        ordersProjectionMap.put("sum_shipping", "sum_shipping");
        ordersProjectionMap.put("weight_doc", "weight_doc");
        ordersProjectionMap.put("shipping_type", "shipping_type");
        ordersProjectionMap.put("shipping_time", "shipping_time");
        ordersProjectionMap.put("shipping_begin_time", "shipping_begin_time");
        ordersProjectionMap.put("shipping_end_time", "shipping_end_time");
        ordersProjectionMap.put("simple_discount_id", "simple_discount_id");
        ordersProjectionMap.put("create_client", "create_client");
        ordersProjectionMap.put("create_client_descr", "create_client_descr");
        ordersProjectionMap.put("create_client_surname", "create_client_surname");
        ordersProjectionMap.put("create_client_firstname", "create_client_firstname");
        ordersProjectionMap.put("create_client_lastname", "create_client_lastname");
        //ordersProjectionMap.put("create_client_phone", "create_client_phone");
        ordersProjectionMap.put("place_num", "place_num");
        ordersProjectionMap.put("card_num", "card_num");
        ordersProjectionMap.put("pay_credit", "pay_credit");
        //ordersProjectionMap.put("ticket_m", "ticket_m");
        //ordersProjectionMap.put("ticket_w", "ticket_w");
        //ordersProjectionMap.put("quant_m", "quant_m");
        //ordersProjectionMap.put("quant_w", "quant_w");
        ordersProjectionMap.put("quant_mw", "quant_mw");
        ordersProjectionMap.put("shipping_date", "shipping_date");
        ordersProjectionMap.put("manager_comment", "manager_comment");
        ordersProjectionMap.put("theme_comment", "theme_comment");
        ordersProjectionMap.put("phone_num", "phone_num");
        ordersProjectionMap.put("editing_backup", "editing_backup");
        ordersProjectionMap.put("old_id", "old_id");
        ordersProjectionMap.put("color", ORDERS_COLOR_COLUMN);
        
        cashPaymentsProjectionMap = new HashMap<String, String>();
        cashPaymentsProjectionMap.put("_id", "_id");
        cashPaymentsProjectionMap.put("uid", "uid");
        cashPaymentsProjectionMap.put("version", "version");
        cashPaymentsProjectionMap.put("version_ack", "version_ack");
        cashPaymentsProjectionMap.put("versionPDA", "versionPDA");
        cashPaymentsProjectionMap.put("versionPDA_ack", "versionPDA_ack");
        cashPaymentsProjectionMap.put("id", "id");
        cashPaymentsProjectionMap.put("numdoc", "numdoc");
        cashPaymentsProjectionMap.put("datedoc", "datedoc");
        cashPaymentsProjectionMap.put("client_id", "client_id");
        cashPaymentsProjectionMap.put("agreement_id", "agreement_id");
        cashPaymentsProjectionMap.put("comment", "comment");
        cashPaymentsProjectionMap.put("comment_closing", "comment_closing");
        cashPaymentsProjectionMap.put("state", "state");
        cashPaymentsProjectionMap.put("curator_id", "curator_id");
        cashPaymentsProjectionMap.put("manager_descr", "manager_descr");
        cashPaymentsProjectionMap.put("organization_descr", "organization_descr");
        cashPaymentsProjectionMap.put("vicarious_power_id", "vicarious_power_id");
        cashPaymentsProjectionMap.put("vicarious_power_descr", "vicarious_power_descr");
        cashPaymentsProjectionMap.put("sum_doc", "sum_doc");
        cashPaymentsProjectionMap.put("datecreation", "datecreation");
        cashPaymentsProjectionMap.put("datecoord", "datecoord");
        cashPaymentsProjectionMap.put("latitude", "latitude");
        cashPaymentsProjectionMap.put("longitude", "longitude");
        cashPaymentsProjectionMap.put("gpsstate", "gpsstate");
        cashPaymentsProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        cashPaymentsProjectionMap.put("gpstype", "gpstype");
        cashPaymentsProjectionMap.put("accept_coord", "accept_coord");
        cashPaymentsProjectionMap.put("distr_point_id", "distr_point_id");
        cashPaymentsProjectionMap.put("color", CASH_PAYMENTS_COLOR_COLUMN);

        refundsProjectionMap = new HashMap<String, String>();
        refundsProjectionMap.put("_id", "_id");
        refundsProjectionMap.put("uid", "uid");
        refundsProjectionMap.put("version", "version");
        refundsProjectionMap.put("version_ack", "version_ack");
        refundsProjectionMap.put("versionPDA", "versionPDA");
        refundsProjectionMap.put("versionPDA_ack", "versionPDA_ack");
        refundsProjectionMap.put("id", "id");
        refundsProjectionMap.put("numdoc", "numdoc");
        refundsProjectionMap.put("datedoc", "datedoc");
        refundsProjectionMap.put("client_id", "client_id");
        refundsProjectionMap.put("agreement_id", "agreement_id");
        refundsProjectionMap.put("distr_point_id", "distr_point_id");
        refundsProjectionMap.put("comment", "comment");
        refundsProjectionMap.put("comment_closing", "comment_closing");
        refundsProjectionMap.put("closed_not_full", "closed_not_full");
        refundsProjectionMap.put("state", "state");
        refundsProjectionMap.put("curator_id", "curator_id");
        refundsProjectionMap.put("bw", "bw");
        refundsProjectionMap.put("trade_type", "trade_type");
        refundsProjectionMap.put("datecoord", "datecoord");
        refundsProjectionMap.put("latitude", "latitude");
        refundsProjectionMap.put("longitude", "longitude");
        refundsProjectionMap.put("gpsstate", "gpsstate");
        refundsProjectionMap.put("datecreation", "datecreation");
        refundsProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        refundsProjectionMap.put("accept_coord", "accept_coord");
        refundsProjectionMap.put("dont_need_send", "dont_need_send");
        refundsProjectionMap.put("stock_id", "stock_id");
        refundsProjectionMap.put("weight_doc", "weight_doc");
        refundsProjectionMap.put("sum_doc", "sum_doc"); // но не используется
        refundsProjectionMap.put("shipping_type", "shipping_type");
        refundsProjectionMap.put("color", REFUNDS_COLOR_COLUMN);
        
        journalProjectionMap = new HashMap<String, String>();
        journalProjectionMap.put("_id", "journal._id");
        journalProjectionMap.put("iddocdef", "iddocdef");
        journalProjectionMap.put("order_id", "journal.order_id");
        journalProjectionMap.put("payment_id", "journal.payment_id");
        journalProjectionMap.put("refund_id", "journal.refund_id");
        journalProjectionMap.put("distribs_id", "journal.distribs_id");
        journalProjectionMap.put("uid", "journal.uid");
        journalProjectionMap.put("id", "journal.id");
        journalProjectionMap.put("numdoc", "journal.numdoc");
        journalProjectionMap.put("datedoc", "journal.datedoc");
        journalProjectionMap.put("state", "state");
        journalProjectionMap.put("client_id", "journal.client_id");
        journalProjectionMap.put("client_descr", "case when use_client_descr then journal.client_descr else IFNULL(clients.descr, \"{\"||journal.client_id||\"}\") end as client_descr");
        journalProjectionMap.put("sum_doc", "journal.sum_doc");
        journalProjectionMap.put("sum_shipping", "journal.sum_shipping");
        journalProjectionMap.put("color", "journal.color");
        journalProjectionMap.put("shipping_date", "shipping_date");
        
        clientsProjectionMap = new HashMap<String, String>();
        clientsProjectionMap.put("_id", "_id");
        clientsProjectionMap.put("id", "id");
        clientsProjectionMap.put("isFolder", "isFolder");
        clientsProjectionMap.put("parent_id", "parent_id");
        clientsProjectionMap.put("code", "code");
        clientsProjectionMap.put("descr", "descr");
        clientsProjectionMap.put("descrFull", "descrFull");
        clientsProjectionMap.put("address", "address");
        clientsProjectionMap.put("address2", "address2");
        clientsProjectionMap.put("comment", "comment");
        clientsProjectionMap.put("curator_id", "curator_id");
        clientsProjectionMap.put("priceType", "priceType");
        clientsProjectionMap.put("blocked", "blocked");
        clientsProjectionMap.put("flags", "flags");
        clientsProjectionMap.put("phone_num", "phone_num");
        clientsProjectionMap.put("email_for_cheques", "email_for_cheques");
        clientsProjectionMap.put("card_num", "card_num");
        clientsProjectionMap.put("isUsed", "isUsed");
        
        nomenclatureProjectionMap = new HashMap<String, String>();
        nomenclatureProjectionMap.put("_id", "_id");
        nomenclatureProjectionMap.put("id", "id");
        nomenclatureProjectionMap.put("isFolder", "isFolder");
        nomenclatureProjectionMap.put("parent_id", "parent_id");
        nomenclatureProjectionMap.put("code", "code");
        nomenclatureProjectionMap.put("descr", "descr");
        nomenclatureProjectionMap.put("descrFull", "descrFull");
        nomenclatureProjectionMap.put("quant_1", "quant_1");
        nomenclatureProjectionMap.put("quant_2", "quant_2");
        nomenclatureProjectionMap.put("edizm_1_id", "edizm_1_id");
        nomenclatureProjectionMap.put("edizm_2_id", "edizm_2_id");
        nomenclatureProjectionMap.put("quant_k_1", "quant_k_1");
        nomenclatureProjectionMap.put("quant_k_2", "quant_k_2");
        nomenclatureProjectionMap.put("opt_price", "opt_price");
        nomenclatureProjectionMap.put("m_opt_price", "m_opt_price");
        nomenclatureProjectionMap.put("rozn_price", "rozn_price");
        nomenclatureProjectionMap.put("incom_price", "incom_price");
        nomenclatureProjectionMap.put("IsInPrice", "IsInPrice");
        nomenclatureProjectionMap.put("flagWithoutDiscont", "flagWithoutDiscont");
        nomenclatureProjectionMap.put("weight_k_1", "weight_k_1");
        nomenclatureProjectionMap.put("weight_k_2", "weight_k_2");
        nomenclatureProjectionMap.put("min_quantity", "min_quantity");
        nomenclatureProjectionMap.put("multiplicity", "multiplicity");
        nomenclatureProjectionMap.put("required_sales", "required_sales");
        nomenclatureProjectionMap.put("image_file", "image_file");
        nomenclatureProjectionMap.put("image_file_checksum", "image_file_checksum");
        nomenclatureProjectionMap.put("discount", "discount");
        nomenclatureProjectionMap.put("flags", "flags");
        nomenclatureProjectionMap.put("nomenclature_color", "nomenclature_color");
        nomenclatureProjectionMap.put("order_for_sorting", "order_for_sorting");
        nomenclatureProjectionMap.put("isUsed", "isUsed");
        nomenclatureProjectionMap.put("image_width", "image_width");
        nomenclatureProjectionMap.put("image_height", "image_height");
        nomenclatureProjectionMap.put("image_file_size", "image_file_size");
        nomenclatureProjectionMap.put("compose_with", "compose_with");

        // Номенклатура
        nomenclatureListProjectionMap = new HashMap<String, String>();
        // Номенклатура, в которой также есть корневой (элемент, с пустым Id, он находится только в hierarchy и там full join)
        nomenclatureSurfingProjectionMap = new HashMap<String, String>();

        int i;
        for (i=0; i<2; i++) {
            HashMap<String, String> tempNomenclatureListProjectionMap=(i==0?nomenclatureListProjectionMap:nomenclatureSurfingProjectionMap);
            // Номенклатура
            tempNomenclatureListProjectionMap.put("_id", "nomenclature._id");
            tempNomenclatureListProjectionMap.put("nomenclature_id", i==0?NOMENCLATURE_ID_COLUMN:NOMENCLATURE_ID_SURFING_COLUMN);
            tempNomenclatureListProjectionMap.put("isFolder", "isFolder");
            tempNomenclatureListProjectionMap.put("parent_id", "parent_id");
            tempNomenclatureListProjectionMap.put("h_groupDescr", NOMENCLATURE_GROUP_COLUMN);
            tempNomenclatureListProjectionMap.put("groupDescr", "groupDescr");
            tempNomenclatureListProjectionMap.put("descr", i==0?"descr":NOMENCLATURE_DESCR_SURFING_COLUMN);
            tempNomenclatureListProjectionMap.put("quant_1", "quant_1");
            tempNomenclatureListProjectionMap.put("quant_2", "quant_2");
            tempNomenclatureListProjectionMap.put("edizm_1_id", "edizm_1_id");
            tempNomenclatureListProjectionMap.put("edizm_2_id", "edizm_2_id");
            tempNomenclatureListProjectionMap.put("quant_k_1", "quant_k_1");
            tempNomenclatureListProjectionMap.put("quant_k_2", "quant_k_2");
            tempNomenclatureListProjectionMap.put("required_sales", "required_sales");
            tempNomenclatureListProjectionMap.put("image_file", "image_file");
            tempNomenclatureListProjectionMap.put("image_file_checksum", "image_file_checksum");
            tempNomenclatureListProjectionMap.put("discount", "discount");
            tempNomenclatureListProjectionMap.put("flags", "flags");
            tempNomenclatureListProjectionMap.put("nomenclature_color", "nomenclature_color");
            tempNomenclatureListProjectionMap.put("compose_with", "compose_with");

            tempNomenclatureListProjectionMap.put("image_width", "image_width");
            tempNomenclatureListProjectionMap.put("image_height", "image_height");

            tempNomenclatureListProjectionMap.put("quantity7_1", "quantity7_1");
            tempNomenclatureListProjectionMap.put("quantity7_2", "quantity7_2");
            tempNomenclatureListProjectionMap.put("quantity7_3", "quantity7_3");
            tempNomenclatureListProjectionMap.put("quantity7_4", "quantity7_4");

            // Прайс
            tempNomenclatureListProjectionMap.put("price", "price");
            tempNomenclatureListProjectionMap.put("k", "k");
            tempNomenclatureListProjectionMap.put("edIzm", "edIzm");
            // Наценки по клиентам (в 7-ке справочник СкидкиПоКлиентам)
            tempNomenclatureListProjectionMap.put("priceAdd", "ifnull(d_s.priceAdd, d_s_p.priceAdd) as priceAdd");
            tempNomenclatureListProjectionMap.put("priceProcent", "ifnull(d_s.priceProcent, d_s_p.priceProcent) as priceProcent");
            // Остатки
            tempNomenclatureListProjectionMap.put("nom_quantity", NOMENCLATURE_REST_STOCK_COLUMN);
            // Продажи
            tempNomenclatureListProjectionMap.put("quantity_saled", "strQuantity as quantity_saled");
            // Продажи текущие
            tempNomenclatureListProjectionMap.put("nom_quantity_saled_now", "saledNow as nom_quantity_saled_now");
            // Дополнительно
            tempNomenclatureListProjectionMap.put("zero", DUMMY_COLUMN);
        }


        //
        nomenclatureHierarchyProjectionMap = new HashMap<String, String>();
        nomenclatureHierarchyProjectionMap.put("id", "id");
        nomenclatureHierarchyProjectionMap.put("ord_idx", "ord_idx");
        nomenclatureHierarchyProjectionMap.put("groupDescr", "groupDescr");
        nomenclatureHierarchyProjectionMap.put("level", "level");
        nomenclatureHierarchyProjectionMap.put("level0_id", "level0_id");
        nomenclatureHierarchyProjectionMap.put("level1_id", "level1_id");
        nomenclatureHierarchyProjectionMap.put("level2_id", "level2_id");
        nomenclatureHierarchyProjectionMap.put("level3_id", "level3_id");
        nomenclatureHierarchyProjectionMap.put("level4_id", "level4_id");
        nomenclatureHierarchyProjectionMap.put("level5_id", "level5_id");
        nomenclatureHierarchyProjectionMap.put("level6_id", "level6_id");
        nomenclatureHierarchyProjectionMap.put("level7_id", "level7_id");
        nomenclatureHierarchyProjectionMap.put("level8_id", "level8_id");
        nomenclatureHierarchyProjectionMap.put("dont_use_in_hierarchy", "dont_use_in_hierarchy");

        
        restsProjectionMap = new HashMap<String, String>();
        restsProjectionMap.put("stock_id", "stock_id");
        restsProjectionMap.put("nomenclature_id", "nomenclature_id");
        restsProjectionMap.put("organization_id", "organization_id");
        restsProjectionMap.put("quantity", "quantity");
        restsProjectionMap.put("quantity_reserve", "quantity_reserve");

        saldoProjectionMap = new HashMap<String, String>();
        saldoProjectionMap.put("_id", "_id");
        saldoProjectionMap.put("client_id", "client_id");
        saldoProjectionMap.put("saldo", "saldo");
        saldoProjectionMap.put("saldo_past", "saldo_past");
        saldoProjectionMap.put("saldo_past30", "saldo_past30");
        
        saldoExtendedProjectionMap = new HashMap<String, String>();
        saldoExtendedProjectionMap.put("_id", "_id");
        saldoExtendedProjectionMap.put("client_id", "client_id");
        saldoExtendedProjectionMap.put("agreement_id", "agreement_id");
        saldoExtendedProjectionMap.put("document_id", "document_id");
        saldoExtendedProjectionMap.put("manager_id", "manager_id");
        saldoExtendedProjectionMap.put("document_descr", "document_descr");
        saldoExtendedProjectionMap.put("manager_descr", "manager_descr");
        saldoExtendedProjectionMap.put("agreement_descr", "agreement_descr");
        saldoExtendedProjectionMap.put("organization_descr", "organization_descr");
        saldoExtendedProjectionMap.put("saldo", "saldo");
        saldoExtendedProjectionMap.put("saldo_past", "saldo_past");
        saldoExtendedProjectionMap.put("saldo_past30", "saldo_past30");
        
        saldoExtendedJournalProjectionMap = new HashMap<String, String>();
        saldoExtendedJournalProjectionMap.put("_id", "_id");
        saldoExtendedJournalProjectionMap.put("client_id", "client_id");
        saldoExtendedJournalProjectionMap.put("agreement_id", "agreement_id");
        saldoExtendedJournalProjectionMap.put("document_id", "document_id");
        saldoExtendedJournalProjectionMap.put("manager_id", "manager_id");
        saldoExtendedJournalProjectionMap.put("document_descr", "document_descr");
        saldoExtendedJournalProjectionMap.put("manager_descr", "manager_descr");
        saldoExtendedJournalProjectionMap.put("saldo", "saldo");
        saldoExtendedJournalProjectionMap.put("saldo_past", "saldo_past");
        saldoExtendedJournalProjectionMap.put("saldo_past30", "saldo_past30");
        saldoExtendedJournalProjectionMap.put("client_descr", SALDO_CLIENT_DESCR_COLUMN);
        saldoExtendedJournalProjectionMap.put("agreement_descr", SALDO_AGREEMENT_DESCR_COLUMN);
        saldoExtendedJournalProjectionMap.put("organization_descr", "organization_descr");
        
        
        agreementsProjectionMap = new HashMap<String, String>();
        agreementsProjectionMap.put("_id", "_id");
        agreementsProjectionMap.put("id", "id");
        agreementsProjectionMap.put("owner_id", "owner_id");
        agreementsProjectionMap.put("organization_id", "organization_id");
        agreementsProjectionMap.put("default_manager_id", "default_manager_id");
        agreementsProjectionMap.put("code", "code");
        agreementsProjectionMap.put("descr", "descr");
        agreementsProjectionMap.put("price_type_id", "price_type_id");
        agreementsProjectionMap.put("sale_id", "sale_id");
        agreementsProjectionMap.put("kredit_days", "kredit_days");
        agreementsProjectionMap.put("kredit_sum", "kredit_sum");
        agreementsProjectionMap.put("flags", "flags");

        agreements30ProjectionMap = new HashMap<String, String>();
        agreements30ProjectionMap.put("_id", "_id");
        agreements30ProjectionMap.put("id", "id");
        agreements30ProjectionMap.put("owner_id", "owner_id");
        agreements30ProjectionMap.put("organization_id", "organization_id");
        agreements30ProjectionMap.put("default_manager_id", "default_manager_id");
        agreements30ProjectionMap.put("code", "code");
        agreements30ProjectionMap.put("descr", "descr");
        agreements30ProjectionMap.put("price_type_id", "price_type_id");
        agreements30ProjectionMap.put("sale_id", "sale_id");
        agreements30ProjectionMap.put("kredit_days", "kredit_days");
        agreements30ProjectionMap.put("kredit_sum", "kredit_sum");
        agreements30ProjectionMap.put("flags", "flags");

        versionsProjectionMap = new HashMap<String, String>();
        versionsProjectionMap.put("param", "param");
        versionsProjectionMap.put("ver", "ver");
        
        pricetypesProjectionMap = new HashMap<String, String>();
        pricetypesProjectionMap.put("_id", "_id");
        pricetypesProjectionMap.put("id", "id");
        pricetypesProjectionMap.put("isFolder", "isFolder");
        pricetypesProjectionMap.put("code", "code");
        pricetypesProjectionMap.put("descr", "descr");
        pricetypesProjectionMap.put("isUsed", "isUsed");
        
        stocksProjectionMap = new HashMap<String, String>();
        stocksProjectionMap.put("_id", "_id");
        stocksProjectionMap.put("id", "id");
        stocksProjectionMap.put("isFolder", "isFolder");
        stocksProjectionMap.put("code", "code");
        stocksProjectionMap.put("descr", "descr");
        stocksProjectionMap.put("flags", "flags");
        
        pricesProjectionMap = new HashMap<String, String>();
        pricesProjectionMap.put("_id", "_id");
        pricesProjectionMap.put("nomenclature_id", "nomenclature_id");
        pricesProjectionMap.put("price_type_id", "price_type_id");
        pricesProjectionMap.put("ed_izm_id", "ed_izm_id");
        pricesProjectionMap.put("edIzm", "edIzm");
        pricesProjectionMap.put("price", "price");
        pricesProjectionMap.put("priceProcent", "priceProcent");
        pricesProjectionMap.put("k", "k");
        pricesProjectionMap.put("isUsed", "isUsed");

        pricesAgreements30ProjectionMap = new HashMap<String, String>();
        pricesAgreements30ProjectionMap.put("_id", "_id");
        pricesAgreements30ProjectionMap.put("agreement30_id", "agreement30_id");
        pricesAgreements30ProjectionMap.put("nomenclature_id", "nomenclature_id");
        pricesAgreements30ProjectionMap.put("pack_id", "pack_id");
        pricesAgreements30ProjectionMap.put("ed_izm_id", "ed_izm_id");
        pricesAgreements30ProjectionMap.put("edIzm", "edIzm");
        pricesAgreements30ProjectionMap.put("price", "price");
        pricesAgreements30ProjectionMap.put("k", "k");

        agentsProjectionMap = new HashMap<String, String>();
        agentsProjectionMap.put("_id", "_id");
        agentsProjectionMap.put("id", "id");
        agentsProjectionMap.put("code", "code");
        agentsProjectionMap.put("descr", "descr");
        
        curatorsProjectionMap = new HashMap<String, String>();
        curatorsProjectionMap.put("_id", "_id");
        curatorsProjectionMap.put("id", "id");
        curatorsProjectionMap.put("isFolder", "isFolder");
        curatorsProjectionMap.put("parent_id", "parent_id");
        curatorsProjectionMap.put("code", "code");
        curatorsProjectionMap.put("descr", "descr");
        
        curatorsListProjectionMap = new HashMap<String, String>();
        /*
        curatorsListProjectionMap.put("_id", "curators._id");
        curatorsListProjectionMap.put("id", "ifnull(curators.id,sd.manager_id) as id");
        curatorsListProjectionMap.put("isFolder", "ifnull(curators.isFolder,2) as isFolder");
        curatorsListProjectionMap.put("parent_id", "curators.parent_id");
        curatorsListProjectionMap.put("code", "ifnull(curators.code,'') as code");
        curatorsListProjectionMap.put("descr", "ifnull(curators.descr,id.manager_descr) as descr");
        */
        curatorsListProjectionMap.put("_id", "_id");
        curatorsListProjectionMap.put("id", "id");
        curatorsListProjectionMap.put("isFolder", "isFolder");
        curatorsListProjectionMap.put("parent_id", "parent_id");
        curatorsListProjectionMap.put("client_id", "client_id");
        curatorsListProjectionMap.put("code", "code");
        curatorsListProjectionMap.put("descr", "descr");
        
        distrPointsProjectionMap = new HashMap<String, String>();
        distrPointsProjectionMap.put("_id", "_id");
        distrPointsProjectionMap.put("id", "id");
        distrPointsProjectionMap.put("owner_id", "owner_id");
        distrPointsProjectionMap.put("code", "code");
        distrPointsProjectionMap.put("descr", "descr");
        distrPointsProjectionMap.put("address", "address");
        distrPointsProjectionMap.put("phones", "phones");
        distrPointsProjectionMap.put("conacts", "conacts");
        distrPointsProjectionMap.put("price_type_id", "price_type_id");
        
        distrPointsListProjectionMap = new HashMap<String, String>();
        distrPointsListProjectionMap.put("_id", "distr_points._id");
        distrPointsListProjectionMap.put("id", "distr_points.id");
        distrPointsListProjectionMap.put("owner_id", "owner_id");
        distrPointsListProjectionMap.put("code", "distr_points.code");
        distrPointsListProjectionMap.put("descr", "distr_points.descr");
        distrPointsListProjectionMap.put("address", "address");
        distrPointsListProjectionMap.put("phones", "phones");
        distrPointsListProjectionMap.put("conacts", "conacts");
        distrPointsListProjectionMap.put("price_type_id", "price_type_id");
        distrPointsListProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN);
        
        organizationsProjectionMap = new HashMap<String, String>();
        organizationsProjectionMap.put("_id", "_id");
        organizationsProjectionMap.put("id", "id");
        organizationsProjectionMap.put("code", "code");
        organizationsProjectionMap.put("descr", "descr");
        
        clientsWithSaldoProjectionMap = new HashMap<String, String>();
        clientsWithSaldoProjectionMap.put("_id", "clients._id");
        clientsWithSaldoProjectionMap.put("id", "clients.id");
        clientsWithSaldoProjectionMap.put("isFolder", "isFolder");
        clientsWithSaldoProjectionMap.put("parent_id", "parent_id");
        clientsWithSaldoProjectionMap.put("descr", "descr");
        clientsWithSaldoProjectionMap.put("address", "address");
        clientsWithSaldoProjectionMap.put("saldo", "saldo");
        clientsWithSaldoProjectionMap.put("saldo_past", "saldo_past");
        clientsWithSaldoProjectionMap.put("saldo_past30", "saldo_past30");
        clientsWithSaldoProjectionMap.put("blocked", "blocked");
        clientsWithSaldoProjectionMap.put("flags", "flags");
        clientsWithSaldoProjectionMap.put("email_for_cheques", "email_for_cheques");
        
        ordersLinesProjectionMap = new HashMap<String, String>();
        ordersLinesProjectionMap.put("_id", "_id");
        ordersLinesProjectionMap.put("order_id", "order_id");
        ordersLinesProjectionMap.put("nomenclature_id", "nomenclature_id");
        ordersLinesProjectionMap.put("quantity_requested", "quantity_requested");
        ordersLinesProjectionMap.put("quantity", "quantity");
        ordersLinesProjectionMap.put("price", "price");
        ordersLinesProjectionMap.put("total", "total");
        ordersLinesProjectionMap.put("discount", "discount");
        ordersLinesProjectionMap.put("k", "k");
        ordersLinesProjectionMap.put("ed", "ed");
        ordersLinesProjectionMap.put("shipping_time", "shipping_time");
        ordersLinesProjectionMap.put("comment_in_line", "comment_in_line");
        //ordersLinesProjectionMap.put("editing_state", "editing_state");
        ordersLinesProjectionMap.put("lineno", "lineno");
        
        
        refundsLinesProjectionMap = new HashMap<String, String>();
        refundsLinesProjectionMap.put("_id", "_id");
        refundsLinesProjectionMap.put("refund_id", "refund_id");
        refundsLinesProjectionMap.put("nomenclature_id", "nomenclature_id");
        refundsLinesProjectionMap.put("quantity_requested", "quantity_requested");
        refundsLinesProjectionMap.put("quantity", "quantity");
        //refundsLinesProjectionMap.put("price", "price");
        //refundsLinesProjectionMap.put("total", "total");
        //refundsLinesProjectionMap.put("discount", "discount");
        refundsLinesProjectionMap.put("k", "k");
        refundsLinesProjectionMap.put("ed", "ed");
        refundsLinesProjectionMap.put("comment_in_line", "comment_in_line");
        
        ordersJournalProjectionMap = new HashMap<String, String>();
        ordersJournalProjectionMap.put("_id", "orders._id");
        ordersJournalProjectionMap.put("numdoc", "numdoc");
        ordersJournalProjectionMap.put("datedoc", "datedoc");
        ordersJournalProjectionMap.put("state", "state");
        ordersJournalProjectionMap.put("client_descr", CLIENT_DESCR_COLUMN);
        ordersJournalProjectionMap.put("client_id", "client_id");
        ordersJournalProjectionMap.put("sum_doc", "sum_doc");
        ordersJournalProjectionMap.put("color", ORDERS_COLOR_COLUMN);
        
        cashPaymentsJournalProjectionMap = new HashMap<String, String>();
        cashPaymentsJournalProjectionMap.put("_id", "cash_payments._id");
        cashPaymentsJournalProjectionMap.put("numdoc", "numdoc");
        cashPaymentsJournalProjectionMap.put("datedoc", "datedoc");
        cashPaymentsJournalProjectionMap.put("state", "state");
        cashPaymentsJournalProjectionMap.put("client_descr", CASH_PAYMENTS_CLIENT_DESCR_COLUMN);
        cashPaymentsJournalProjectionMap.put("client_id", "client_id");
        cashPaymentsJournalProjectionMap.put("sum_doc", "sum_doc");
        cashPaymentsJournalProjectionMap.put("color", CASH_PAYMENTS_COLOR_COLUMN);
        cashPaymentsJournalProjectionMap.put("manager_descr", "manager_descr");
        cashPaymentsJournalProjectionMap.put("zero", DUMMY_COLUMN);
        
        agreementsListProjectionMap = new HashMap<String, String>();
        agreementsListProjectionMap.put("_id", "agreements._id");
        agreementsListProjectionMap.put("agreement_id", "agreements.id as agreement_id");
        agreementsListProjectionMap.put("owner_id", "owner_id");
        agreementsListProjectionMap.put("agreement_descr", AGREEMENT_DESCR_COLUMN);
        agreementsListProjectionMap.put("organization_descr", ORGANIZATION_DESCR_COLUMN);
        agreementsListProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN);
        agreementsListProjectionMap.put("default_manager_id", "default_manager_id");
        
        agreementsListWithSaldoOnlyProjectionMap = new HashMap<String, String>();
        agreementsListWithSaldoOnlyProjectionMap.put("_id", "agreements._id");
        agreementsListWithSaldoOnlyProjectionMap.put("agreement_id", "agreements.id as agreement_id");
        agreementsListWithSaldoOnlyProjectionMap.put("owner_id", "owner_id");
        agreementsListWithSaldoOnlyProjectionMap.put("agreement_descr", AGREEMENT_DESCR_COLUMN);
        agreementsListWithSaldoOnlyProjectionMap.put("organization_descr", ORGANIZATION_DESCR_COLUMN);
        agreementsListWithSaldoOnlyProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN);
        agreementsListWithSaldoOnlyProjectionMap.put("default_manager_id", "default_manager_id");
        agreementsListWithSaldoOnlyProjectionMap.put("default_manager_descr", "\"\" as default_manager_descr");
        agreementsListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo");
        agreementsListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo");
        agreementsListWithSaldoOnlyProjectionMap.put("saldo_past", "saldo0.saldo_past as saldo_past");

        agreements30ListWithSaldoOnlyProjectionMap = new HashMap<String, String>();
        agreements30ListWithSaldoOnlyProjectionMap.put("_id", "agreements30._id");
        agreements30ListWithSaldoOnlyProjectionMap.put("agreement_id", "agreements30.id as agreement_id");
        agreements30ListWithSaldoOnlyProjectionMap.put("owner_id", "owner_id");
        agreements30ListWithSaldoOnlyProjectionMap.put("agreement_descr", AGREEMENT30_DESCR_COLUMN);
        agreements30ListWithSaldoOnlyProjectionMap.put("organization_descr", ORGANIZATION_DESCR_COLUMN);
        agreements30ListWithSaldoOnlyProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN);
        agreements30ListWithSaldoOnlyProjectionMap.put("default_manager_id", "default_manager_id");
        agreements30ListWithSaldoOnlyProjectionMap.put("default_manager_descr", "\"\" as default_manager_descr");
        agreements30ListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo");
        agreements30ListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo");
        agreements30ListWithSaldoOnlyProjectionMap.put("saldo_past", "saldo0.saldo_past as saldo_past");

        ordersLinesComplementedProjectionMap = new HashMap<String, String>();
        ordersLinesComplementedProjectionMap.put("_id", "ordersLines._id");
        ordersLinesComplementedProjectionMap.put("order_id", "order_id");
        ordersLinesComplementedProjectionMap.put("nomenclature_id", "nomenclature_id");
        ordersLinesComplementedProjectionMap.put("quantity_requested", "quantity_requested");
        ordersLinesComplementedProjectionMap.put("quantity", "quantity");
        ordersLinesComplementedProjectionMap.put("price", "price");
        ordersLinesComplementedProjectionMap.put("total", "total");
        ordersLinesComplementedProjectionMap.put("discount", "discount");
        ordersLinesComplementedProjectionMap.put("weight_k_1", NOMENCLATURE_WEIGHT_COLUMN);
        ordersLinesComplementedProjectionMap.put("k", "k");
        ordersLinesComplementedProjectionMap.put("ed", "ed");
        ordersLinesComplementedProjectionMap.put("nomencl_descr", NOMENCLATURE_DESCR_COLUMN);
        //ordersLinesComplementedProjectionMap.put("nomencl_node_id", NOMENCLATURE_NODE_ID_COLUMN);
        ordersLinesComplementedProjectionMap.put("flags", "flags");
        ordersLinesComplementedProjectionMap.put("shipping_time", "shipping_time");
        ordersLinesComplementedProjectionMap.put("comment_in_line", "comment_in_line");
        ordersLinesComplementedProjectionMap.put("lineno", "lineno");
        
        refundsLinesComplementedProjectionMap = new HashMap<String, String>();
        refundsLinesComplementedProjectionMap.put("_id", "refundsLines._id");
        refundsLinesComplementedProjectionMap.put("refund_id", "refund_id");
        refundsLinesComplementedProjectionMap.put("nomenclature_id", "nomenclature_id");
        refundsLinesComplementedProjectionMap.put("quantity_requested", "quantity_requested");
        refundsLinesComplementedProjectionMap.put("quantity", "quantity");
        //refundsLinesComplementedProjectionMap.put("price", "price");
        //refundsLinesComplementedProjectionMap.put("total", "total");
        refundsLinesComplementedProjectionMap.put("weight_k_1", NOMENCLATURE_WEIGHT_COLUMN);
        refundsLinesComplementedProjectionMap.put("k", "k");
        refundsLinesComplementedProjectionMap.put("ed", "ed");
        refundsLinesComplementedProjectionMap.put("nomencl_descr", NOMENCLATURE_DESCR_COLUMN);
        refundsLinesComplementedProjectionMap.put("flags", "flags");
        refundsLinesComplementedProjectionMap.put("comment_in_line", "comment_in_line");
        
        
        clientsPriceProjectionMap = new HashMap<String, String>();
        clientsPriceProjectionMap.put("_id", "_id");
        clientsPriceProjectionMap.put("client_id", "client_id");
        clientsPriceProjectionMap.put("nomenclature_id", "nomenclature_id");
        clientsPriceProjectionMap.put("priceAdd", "priceAdd");
        clientsPriceProjectionMap.put("priceProcent", "priceProcent");
        
        curatorsPriceProjectionMap = new HashMap<String, String>();
        curatorsPriceProjectionMap.put("_id", "_id");
        curatorsPriceProjectionMap.put("curator_id", "curator_id");
        curatorsPriceProjectionMap.put("nomenclature_id", "nomenclature_id");
        curatorsPriceProjectionMap.put("priceAdd", "priceAdd");
        curatorsPriceProjectionMap.put("priceProcent", "priceProcent");
        
        simpleDiscountsProjectionMap = new HashMap<String, String>();
        simpleDiscountsProjectionMap.put("_id", "_id");
        simpleDiscountsProjectionMap.put("id", "id");
        simpleDiscountsProjectionMap.put("code", "code");
        simpleDiscountsProjectionMap.put("descr", "descr");
        simpleDiscountsProjectionMap.put("priceProcent", "priceProcent");
        simpleDiscountsProjectionMap.put("isUsed", "isUsed");
        
        seancesProjectionMap = new HashMap<String, String>();
        seancesProjectionMap.put("incoming", "incoming");
        seancesProjectionMap.put("outgoing", "outgoing");

        messagesProjectionMap = new HashMap<String, String>();
        messagesProjectionMap.put("_id", "messages._id");
        messagesProjectionMap.put("uid", "uid");
        messagesProjectionMap.put("sender_id", "sender_id");
        messagesProjectionMap.put("receiver_id", "receiver_id");
        messagesProjectionMap.put("text", "text");
        messagesProjectionMap.put("fname", "fname");
        messagesProjectionMap.put("datetime", "datetime");
        messagesProjectionMap.put("acknowledged", "acknowledged");
        messagesProjectionMap.put("ver", "ver");
        messagesProjectionMap.put("type_idx", "type_idx");
        messagesProjectionMap.put("date1", "date1");
        messagesProjectionMap.put("date2", "date2");
        messagesProjectionMap.put("client_id", "client_id");
        messagesProjectionMap.put("agreement_id", "agreement_id");
        messagesProjectionMap.put("nomenclature_id", "nomenclature_id");
        messagesProjectionMap.put("report", "report");
        messagesProjectionMap.put("isMark", "isMark");
        messagesProjectionMap.put("isMarkCnt", "isMarkCnt");
        
        messagesListProjectionMap = new HashMap<String, String>();
        messagesListProjectionMap.put("_id", "messages._id");
        messagesListProjectionMap.put("uid", "uid");
        messagesListProjectionMap.put("sender_id", "sender_id");
        messagesListProjectionMap.put("receiver_id", "receiver_id");
        messagesListProjectionMap.put("text", "text");
        messagesListProjectionMap.put("fname", "fname");
        messagesListProjectionMap.put("datetime", "datetime");
        messagesListProjectionMap.put("acknowledged", "acknowledged");
        // 0 - входящие, 1 - исходящие 
        messagesListProjectionMap.put("inout", "case when acknowledged&4==0 then 0 else 1 end inout");
        messagesListProjectionMap.put("ver", "ver");
        messagesListProjectionMap.put("type_idx", "type_idx");
        messagesListProjectionMap.put("date1", "date1");
        messagesListProjectionMap.put("date2", "date2");
        messagesListProjectionMap.put("client_id", "client_id");
        messagesListProjectionMap.put("agreement_id", "agreement_id");
        messagesListProjectionMap.put("nomenclature_id", "nomenclature_id");
        //messagesListProjectionMap.put("descr", "ifnull(clients.descr, agents.descr) as descr");
        messagesListProjectionMap.put("descr", "case when type_idx in ("+E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value()+","+E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value()+") then clients.descr else agents.descr end as descr");
        messagesListProjectionMap.put("zero", DUMMY_COLUMN);
        
        settingsProjectionMap = new HashMap<String, String>();
        settingsProjectionMap.put("_id", "_id");
        settingsProjectionMap.put("fmt", "fmt");
        //settingsProjectionMap.put("ticket_m", "ticket_m");
        //settingsProjectionMap.put("ticket_w", "ticket_w");
        settingsProjectionMap.put("agent_id", "agent_id"); 
        settingsProjectionMap.put("gps_interval", "gps_interval");
        settingsProjectionMap.put("agent_price_type_id", "agent_price_type_id");
        
        salesLoadedProjectionMap = new HashMap<String, String>();
        salesLoadedProjectionMap.put("_id", "_id");
        salesLoadedProjectionMap.put("ver", "ver");
        salesLoadedProjectionMap.put("datedoc", "datedoc");
        salesLoadedProjectionMap.put("numdoc", "numdoc");
        salesLoadedProjectionMap.put("refdoc", "refdoc");
        salesLoadedProjectionMap.put("client_id", "client_id");
        salesLoadedProjectionMap.put("curator_id", "curator_id");
        salesLoadedProjectionMap.put("distr_point_id", "distr_point_id");
        salesLoadedProjectionMap.put("nomenclature_id", "nomenclature_id");
        salesLoadedProjectionMap.put("quantity", "quantity");
        salesLoadedProjectionMap.put("price", "price");
        
        
        /*
        salesLoadedWithCommonGroupsProjectionMap = new HashMap<String, String>();
        //salesLoadedWithCommonGroupsProjectionMap.put("_id", "max(s._id) as _id");
        salesLoadedWithCommonGroupsProjectionMap.put("_id", "_id");
        salesLoadedWithCommonGroupsProjectionMap.put("datedoc", "datedoc");
        salesLoadedWithCommonGroupsProjectionMap.put("numdoc", "numdoc");
        salesLoadedWithCommonGroupsProjectionMap.put("client_id", "client_id");
        //salesLoadedWithCommonGroupsProjectionMap.put("nomenclature_id", "case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id");
        salesLoadedWithCommonGroupsProjectionMap.put("nomenclature_id", "nomenclature_id");
        //salesLoadedWithCommonGroupsProjectionMap.put("quantity", "sum(quantity) as quantity");
        salesLoadedWithCommonGroupsProjectionMap.put("quantity", "quantity");
        salesLoadedWithCommonGroupsProjectionMap.put("price", "price");
        */
        
        salesL2ProjectionMap = new HashMap<String, String>();
        salesL2ProjectionMap.put("_id", "_id"); // из нме
        salesL2ProjectionMap.put("datedoc", "datedoc");
        salesL2ProjectionMap.put("numdoc", "numdoc");
        salesL2ProjectionMap.put("client_id", "client_id");
        salesL2ProjectionMap.put("distr_point_id", "distr_point_id");
        salesL2ProjectionMap.put("nomenclature_id", "nomenclature_id");
        salesL2ProjectionMap.put("quantity", "quantity");
        salesL2ProjectionMap.put("price", "price");
        
        versionsSalesProjectionMap = new HashMap<String, String>();
        versionsSalesProjectionMap.put("param", "param");
        versionsSalesProjectionMap.put("ver", "ver");

        hierarchyProjectionMap = new HashMap<String, String>();
        hierarchyProjectionMap.put("id", "id");
        hierarchyProjectionMap.put("ord_idx", "ord_idx");
        hierarchyProjectionMap.put("groupDescr", "groupDescr");
        hierarchyProjectionMap.put("level", "level");
        
        
        placesProjectionMap = new HashMap<String, String>();
        placesProjectionMap.put("id", "id");
        placesProjectionMap.put("code", "code");
        placesProjectionMap.put("descr", "descr");
        placesProjectionMap.put("isUsed", "isUsed");
        
        occupiedPlacesProjectionMap = new HashMap<String, String>();
        occupiedPlacesProjectionMap.put("place_id", "place_id");
        occupiedPlacesProjectionMap.put("client_id", "client_id");
        occupiedPlacesProjectionMap.put("document_id", "document_id");
        occupiedPlacesProjectionMap.put("datedoc", "datedoc");
        occupiedPlacesProjectionMap.put("document", "document");
        occupiedPlacesProjectionMap.put("shipping_date", "shipping_date");
        occupiedPlacesProjectionMap.put("shipping_time", "shipping_time");
        occupiedPlacesProjectionMap.put("isUsed", "isUsed");
        
        ordersPlacesProjectionMap = new HashMap<String, String>();
        ordersPlacesProjectionMap.put("order_id", "order_id");
        ordersPlacesProjectionMap.put("place_id", "place_id");
        
        ordersPlacesListProjectionMap = new HashMap<String, String>();
        ordersPlacesListProjectionMap.put("_id", "orders._id");
        ordersPlacesListProjectionMap.put("datedoc", "datedoc");
        ordersPlacesListProjectionMap.put("numdoc", "numdoc");
        ordersPlacesListProjectionMap.put("id", "id");
        ordersPlacesListProjectionMap.put("client_id", "client_id");
        ordersPlacesListProjectionMap.put("order_id", "order_id");
        ordersPlacesListProjectionMap.put("place_id", "place_id");
        ordersPlacesListProjectionMap.put("state", "state");
        ordersPlacesListProjectionMap.put("shipping_date", "shipping_date");
        ordersPlacesListProjectionMap.put("shipping_time", "shipping_time");
        
        vicariousPowerProjectionMap = new HashMap<String, String>();
        vicariousPowerProjectionMap.put("_id", "_id");
        vicariousPowerProjectionMap.put("id", "id");
        vicariousPowerProjectionMap.put("descr", "descr");
        vicariousPowerProjectionMap.put("numdoc", "numdoc");
        vicariousPowerProjectionMap.put("datedoc", "datedoc");
        vicariousPowerProjectionMap.put("date_action", "date_action");
        vicariousPowerProjectionMap.put("comment", "comment");
        vicariousPowerProjectionMap.put("client_id", "client_id");
        vicariousPowerProjectionMap.put("client_descr", "client_descr");
        vicariousPowerProjectionMap.put("agreement_id", "agreement_descr");
        vicariousPowerProjectionMap.put("fio_descr", "fio_descr");
        vicariousPowerProjectionMap.put("manager_id", "manager_id");
        vicariousPowerProjectionMap.put("manager_descr", "manager_descr");
        vicariousPowerProjectionMap.put("organization_id", "organization_id");
        vicariousPowerProjectionMap.put("organization_descr", "organization_descr");
        vicariousPowerProjectionMap.put("state", "state");
        vicariousPowerProjectionMap.put("sum_doc", "sum_doc");

        gpsCoordProjectionMap = new HashMap<String, String>();
        gpsCoordProjectionMap.put("datecoord", "datecoord");
        gpsCoordProjectionMap.put("latitude", "latitude");
        gpsCoordProjectionMap.put("longitude", "longitude");
        gpsCoordProjectionMap.put("gpsstate", "gpsstate");
        gpsCoordProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        gpsCoordProjectionMap.put("version", "version");
        gpsCoordProjectionMap.put("gpstype", "gpstype");

        distribsContractsProjectionMap = new HashMap<String, String>();
        distribsContractsProjectionMap.put("_id", "_id");
        distribsContractsProjectionMap.put("id", "id");
        distribsContractsProjectionMap.put("position", "position");
        distribsContractsProjectionMap.put("code", "code");
        distribsContractsProjectionMap.put("descr", "descr");

        // TODO другие поля
        distribsContractsListProjectionMap = new HashMap<String, String>();
        distribsContractsListProjectionMap.put("_id", "_id");
        distribsContractsListProjectionMap.put("id", "id");
        distribsContractsListProjectionMap.put("position", "position");
        distribsContractsListProjectionMap.put("code", "code");
        distribsContractsListProjectionMap.put("descr", "descr");
	
        distribsLinesProjectionMap = new HashMap<String, String>();
        distribsLinesProjectionMap.put("_id", "_id");
        distribsLinesProjectionMap.put("distribs_id", "distribs_id");
        distribsLinesProjectionMap.put("contract_id", "contract_id");
        distribsLinesProjectionMap.put("quantity", "quantity");
        
        distribsLinesComplementedProjectionMap = new HashMap<String, String>();
        distribsLinesComplementedProjectionMap.put("_id", "distribsLines._id");
        distribsLinesComplementedProjectionMap.put("distribs_id", "distribs_id");
        distribsLinesComplementedProjectionMap.put("contract_id", "contract_id");
        distribsLinesComplementedProjectionMap.put("contract_descr", CONTRACT_DESCR_COLUMN);
        distribsLinesComplementedProjectionMap.put("quantity", "quantity");
	      
        distribsProjectionMap = new HashMap<String, String>();
        distribsProjectionMap.put("_id", "_id");
        distribsProjectionMap.put("uid", "uid");
        distribsProjectionMap.put("version", "version");
        distribsProjectionMap.put("version_ack", "version_ack");
        distribsProjectionMap.put("versionPDA", "versionPDA");
        distribsProjectionMap.put("versionPDA_ack", "versionPDA_ack");
        distribsProjectionMap.put("id", "id");
        distribsProjectionMap.put("numdoc", "numdoc");
        distribsProjectionMap.put("datedoc", "datedoc");
        distribsProjectionMap.put("client_id", "client_id");
        distribsProjectionMap.put("curator_id", "curator_id");
        distribsProjectionMap.put("distr_point_id", "distr_point_id");
        distribsProjectionMap.put("state", "state");
        distribsProjectionMap.put("comment", "comment");
        distribsProjectionMap.put("datecoord", "datecoord");
        distribsProjectionMap.put("latitude", "latitude");
        distribsProjectionMap.put("longitude", "longitude");
        distribsProjectionMap.put("gpsstate", "gpsstate");
        distribsProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        distribsProjectionMap.put("gpstype", "gpstype");
        distribsProjectionMap.put("accept_coord", "accept_coord");
        distribsProjectionMap.put("color", DISTRIBS_COLOR_COLUMN);

        equipmentProjectionMap = new HashMap<String, String>();
        equipmentProjectionMap.put("_id", "_id");
        equipmentProjectionMap.put("id", "id");
        equipmentProjectionMap.put("code", "code");
        equipmentProjectionMap.put("descr", "descr");
        equipmentProjectionMap.put("flags", "flags");
        
        equipmentRestsProjectionMap = new HashMap<String, String>();
        equipmentRestsProjectionMap.put("_id", "_id");
        equipmentRestsProjectionMap.put("client_id", "client_id");
        equipmentRestsProjectionMap.put("agreement_id", "agreement_id");
        equipmentRestsProjectionMap.put("nomenclature_id", "nomenclature_id");
        equipmentRestsProjectionMap.put("distr_point_id", "distr_point_id");
        equipmentRestsProjectionMap.put("quantity", "quantity");
        equipmentRestsProjectionMap.put("sum", "sum");
        equipmentRestsProjectionMap.put("flags", "flags");
        equipmentRestsProjectionMap.put("doc_id", "doc_id");
        equipmentRestsProjectionMap.put("doc_descr", "doc_descr");
        equipmentRestsProjectionMap.put("date", "date");
        equipmentRestsProjectionMap.put("datepast", "datepast");

        equipmentRestsListProjectionMap = new HashMap<String, String>();
        equipmentRestsListProjectionMap.put("_id", "equipment_rests._id");
        equipmentRestsListProjectionMap.put("client_id", "client_id");
        equipmentRestsListProjectionMap.put("agreement_id", "agreement_id");
        equipmentRestsListProjectionMap.put("nomenclature_id", "nomenclature_id");
        equipmentRestsListProjectionMap.put("nomenclature_descr", "IFNULL(equipment.descr, \"{\"||nomenclature_id||\"}\") as nomenclature_descr");
        equipmentRestsListProjectionMap.put("distr_point_id", "distr_point_id");
        equipmentRestsListProjectionMap.put("quantity", "quantity");
        equipmentRestsListProjectionMap.put("sum", "sum");
        equipmentRestsListProjectionMap.put("flags", "equipment_rests.flags as flags");
        equipmentRestsListProjectionMap.put("doc_id", "doc_id");
        equipmentRestsListProjectionMap.put("doc_descr", "doc_descr");
        equipmentRestsListProjectionMap.put("date", "date");
        equipmentRestsListProjectionMap.put("datepast", "datepast");
        equipmentRestsListProjectionMap.put("empty", "\"\" as empty");
        
        mtradeLogProjectionMap = new HashMap<String, String>();
        mtradeLogProjectionMap.put("_id", "equipment_rests._id");
        mtradeLogProjectionMap.put("messagetext", "messagetext");
        mtradeLogProjectionMap.put("messagetype", "messagetype");
        mtradeLogProjectionMap.put("version", "version");

        permissionsRequestsProjectionMap = new HashMap<String, String>();
        permissionsRequestsProjectionMap.put("_id", "_id");
        permissionsRequestsProjectionMap.put("permission_name", "permission_name");
        permissionsRequestsProjectionMap.put("datetime", "datetime");
        permissionsRequestsProjectionMap.put("seance_incoming", "seance_incoming");

        routesProjectionMap = new HashMap<String, String>();
        routesProjectionMap.put("_id", "_id");
        routesProjectionMap.put("id", "id");
        routesProjectionMap.put("code", "code");
        routesProjectionMap.put("descr", "descr");
        routesProjectionMap.put("manager_id", "manager_id");

        routesLinesProjectionMap = new HashMap<String, String>();
        routesLinesProjectionMap.put("_id", "_id");
        routesLinesProjectionMap.put("route_id", "route_id");
        routesLinesProjectionMap.put("lineno", "lineno");
        routesLinesProjectionMap.put("distr_point_id", "distr_point_id");
        routesLinesProjectionMap.put("visit_time", "visit_time");

        routesDatesProjectionMap = new HashMap<String, String>();
        routesDatesProjectionMap.put("_id", "_id");
        routesDatesProjectionMap.put("route_id", "route_id");
        routesDatesProjectionMap.put("route_date", "route_date");

        routesDatesListProjectionMap = new HashMap<String, String>();
        routesDatesListProjectionMap.put("_id", "routes_dates._id");
        routesDatesListProjectionMap.put("descr", "routes.descr");
        routesDatesListProjectionMap.put("route_id", "route_id");
        routesDatesListProjectionMap.put("route_date", "route_date");


        realRoutesDatesProjectionMap = new HashMap<String, String>();
        realRoutesDatesProjectionMap.put("_id", "_id");
        realRoutesDatesProjectionMap.put("uid", "uid");
        realRoutesDatesProjectionMap.put("id", "id");
        realRoutesDatesProjectionMap.put("route_date", "route_date");
        realRoutesDatesProjectionMap.put("route_id", "route_id");
        realRoutesDatesProjectionMap.put("route_descr", "route_descr");

        realRoutesLinesProjectionMap = new HashMap<String, String>();
        realRoutesLinesProjectionMap.put("_id", "_id");
        realRoutesLinesProjectionMap.put("real_route_id", "real_route_id");
        realRoutesLinesProjectionMap.put("lineno", "lineno");
        realRoutesLinesProjectionMap.put("distr_point_id", "distr_point_id");
        realRoutesLinesProjectionMap.put("required_visit_time", "required_visit_time");
        realRoutesLinesProjectionMap.put("start_visit_time", "start_visit_time");
        realRoutesLinesProjectionMap.put("end_visit_time", "end_visit_time");
        realRoutesLinesProjectionMap.put("version", "version");
        realRoutesLinesProjectionMap.put("version_ack", "version_ack");
        realRoutesLinesProjectionMap.put("versionPDA", "versionPDA");
        realRoutesLinesProjectionMap.put("versionPDA_ack", "versionPDA_ack");
        realRoutesLinesProjectionMap.put("datecoord", "datecoord");
        realRoutesLinesProjectionMap.put("latitude", "latitude");
        realRoutesLinesProjectionMap.put("longitude", "longitude");
        realRoutesLinesProjectionMap.put("gpsstate", "gpsstate");
        realRoutesLinesProjectionMap.put("gpsaccuracy", "gpsaccuracy");
        realRoutesLinesProjectionMap.put("accept_coord", "accept_coord");
    }

}
