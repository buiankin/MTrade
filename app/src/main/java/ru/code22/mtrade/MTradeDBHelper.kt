package ru.code22.mtrade

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_REFUND_COLOR_FOR_TRIGGER
import ru.code22.mtrade.MTradeContentProvider.Companion.LOG_TAG
import ru.code22.mtrade.models.AgentsModel

class MTradeDBHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA encoding = \"UTF-8\"")
        // insert into xxx
        // select * from yyyy;
        // create table xxx
        // as select * from yyy
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(LOG_TAG, " --- onCreate database --- ")
        db.execSQL("PRAGMA encoding = \"UTF-8\"")


        /*
            try {
                int insertCount = insertFromFile(getContext(), db, R.raw.backup_dch);
                //Toast.makeText(this, "Rows loaded from file= " + insertCount, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
             */
        db.execSQL(
            "create table messages (" +
                    "_id integer primary key autoincrement," +
                    "uid text," +
                    "sender_id text," +
                    "receiver_id text," +
                    "text text," +
                    "fname text," +
                    "datetime text," +
                    "acknowledged int," +
                    "ver int," +  // новые
                    "type_idx int," +
                    "date1 text," +
                    "date2 text," +
                    "client_id text," +
                    "agreement_id text," +
                    "nomenclature_id text," +
                    "report text," +  //
                    "isMark int default 0," +
                    "isMarkCnt int default 0," +  //
                    "UNIQUE('uid')" +
                    ");"
        )

        db.execSQL(
            "create table seances (" +
                    "incoming text," +
                    "outgoing text" +
                    ");"
        )

        db.execSQL("insert into seances(incoming) values ('CREATED')")

        db.execSQL(
            "create table versions (" +
                    "param text," +
                    "ver int," +
                    "UNIQUE ('param')" +
                    ");"
        )

        db.execSQL(
            "create table sales_versions (" +
                    "param text," +
                    "ver int," +
                    "UNIQUE ('param')" +
                    ");"
        )

        db.execSQL(
            "create table clients (" +
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
                    "flags int," +
                    "card_num text," +
                    "phone_num text," +
                    "email_for_cheques text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table organizations (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table agreements (" +
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
                    ");"
        )

        db.execSQL(
            "create table agreements30 (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "owner_id text," +
                    "organization_id text," +
                    "code text," +
                    "descr text," +
                    "price_type_id text," +
                    "default_manager_id text," +
                    "default_stock_id text," +
                    "sale_id text," +
                    "kredit_days int," +
                    "kredit_sum double," +
                    "flags int," +
                    "UNIQUE ('id', 'owner_id')" +
                    ");"
        )

        db.execSQL(
            "create table prices_agreements30 (" +
                    "_id integer primary key autoincrement, " +
                    "agreement30_id text," +
                    "nomenclature_id text," +
                    "pack_id text," +  // Пока не используется
                    "ed_izm_id text," +
                    "edIzm text," +
                    "price double," +
                    "k double," +
                    "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                    ");"
        )


        db.execSQL(
            "create table pricetypes (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "isFolder text," +  // это баг, тип строка а не число, да и parent_id в справочнике нет
                    "code text," +
                    "descr text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table clients_price (" +
                    "_id integer primary key autoincrement, " +
                    "client_id text," +
                    "nomenclature_id text," +
                    "priceProcent double," +
                    "priceAdd double," +
                    "UNIQUE ('client_id', 'nomenclature_id')" +
                    ");"
        )

        db.execSQL(
            "create table curators_price (" +
                    "_id integer primary key autoincrement, " +
                    "curator_id text," +
                    "nomenclature_id text," +
                    "priceProcent double," +
                    "priceAdd double," +
                    "UNIQUE ('curator_id', 'nomenclature_id')" +
                    ");"
        )

        db.execSQL(
            "create table simple_discounts (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "priceProcent double," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table stocks (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "isFolder text," +
                    "code text," +
                    "descr text," +
                    "flags int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Nomenclature (" +
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
                    "min_quantity DOUBLE," +  // минимальное продаваемое количество за 1 раз
                    "multiplicity DOUBLE," +  // кратность количества (нельзя дробить)
                    "required_sales DOUBLE," +  // план продажи за месяц по каждому клиенту
                    "flags INT," +
                    "image_file text," +
                    "image_file_checksum INT," +  // не используется, т.к. для расчета требуется считывать файл
                    "order_for_sorting INT," +
                    "group_of_analogs INT," +
                    "nomenclature_color int default 0," +
                    "backorder int default 0," +
                    "image_width INT," +
                    "image_height INT," +
                    "image_file_size INT," +
                    "compose_with text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Rests (" +
                    "stock_id TEXT," +
                    "nomenclature_id TEXT," +
                    "organization_id TEXT default ''," +
                    "quantity DOUBLE," +
                    "quantity_reserve DOUBLE," +
                    "isUsed int default 0," +
                    "UNIQUE ('stock_id', 'nomenclature_id','organization_id')" +
                    ");"
        )

        db.execSQL(
            "create table prices (" +
                    "_id integer primary key autoincrement, " +
                    "nomenclature_id text," +
                    "price_type_id text," +
                    "ed_izm_id text," +
                    "edIzm text," +
                    "price double," +
                    "priceProcent double," +  // скорее всего это лишнее поле
                    "k double," +
                    "isUsed int default 0," +
                    "UNIQUE ('nomenclature_id','price_type_id')" +
                    ");"
        )


        db.execSQL(
            "create table agents (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )


        db.execSQL(
            "create table curators (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "parent_id text," +
                    "isFolder int," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table distr_points (" +
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
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Saldo (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client_id TEXT," +
                    "saldo DOUBLE," +
                    "saldo_past DOUBLE," +
                    "saldo_past30 DOUBLE default 0," +
                    "isUsed int default 0," +
                    "UNIQUE ('client_id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE saldo_extended (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client_id TEXT," +
                    "agreement30_id TEXT," +
                    "agreement_id TEXT," +
                    "document_id TEXT," +
                    "manager_id TEXT," +
                    "document_descr TEXT," +
                    "document_datetime TEXT," +
                    "manager_descr TEXT," +
                    "agreement_descr TEXT," +
                    "organization_descr TEXT," +
                    "saldo DOUBLE," +
                    "saldo_past DOUBLE," +
                    "isUsed int default 0," +
                    "UNIQUE ('client_id','agreement30_id','agreement_id','document_id','manager_id')" +
                    ");"
        )

        db.execSQL(
            "create table ordersLines (" +
                    "_id integer primary key autoincrement, " +
                    "order_id integer," +  // внутренний код
                    "nomenclature_id text," +
                    "client_id text," +
                    "quantity_requested double," +
                    "quantity double," +
                    "price double," +
                    "total double," +
                    "discount DOUBLE," +
                    "k double," +
                    "ed text," +
                    "shipping_time text," +
                    "comment_in_line text," +  // 0-строка не менялась
                    // 1-добавленные строки
                    // 2-строка удалена
                    // 3-строка изменена
                    //"editing_state int," +
                    // для состояния 3 - _id измененной строки
                    //"old_id int" +
                    "lineno int" +
                    ");"
        )

        db.execSQL(
            "create table orders (" +
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
                    "sum_shipping double," +  // 0-документ в нормальном состоянии
                    // 1-новый документ, записать не успели
                    // 2-документ начали редактировать, но не записали и не отменили изменения
                    "editing_backup int," +  // для состояния 2 - _id редактируемого документа
                    // в других случаях - не важно
                    "old_id int" +  // До 19.02.2018 editing_backup тут не было
                    //"UNIQUE ('uid','id')" +
                    ");"
        )
        // С 19.02.2018
        db.execSQL("CREATE UNIQUE INDEX OrdersUniqueIndex ON orders ('uid','id','editing_backup')")

        db.execSQL(
            "create table ordersPlaces (" +
                    "_id integer primary key autoincrement, " +
                    "order_id integer," +  // внутренний код
                    "place_id text" +
                    ");"
        )

        db.execSQL(
            "create table cash_payments (" +
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
                    "vicarious_power_id text, " +
                    "vicarious_power_descr text, " +
                    "datecreation text," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpsaccuracy float," +
                    "gpstype int," +
                    "accept_coord int," +
                    "distr_point_id text," +  // это только для того, чтобы документ привязался к маршруту
                    "UNIQUE ('uid','id')" +
                    ");"
        )


        db.execSQL(
            "create table vicarious_power (" +
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
                    ");"
        )

        db.execSQL(
            "create table refundsLines (" +
                    "_id integer primary key autoincrement, " +
                    "refund_id integer," +  // внутренний код
                    "nomenclature_id text," +
                    "client_id text," +
                    "quantity_requested double," +
                    "quantity double," +  //"price double," +
                    //"total double," +
                    //"discount DOUBLE,"+
                    "k double," +
                    "ed text," +
                    "comment_in_line text" +
                    ");"
        )

        db.execSQL(
            "create table refunds (" +
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
                    "simple_discount_id text," +  // 20.05.2020
                    "shipping_type int," +  //
                    "UNIQUE ('uid','id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE distribsContracts (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id TEXT," +
                    "position int," +
                    "code TEXT," +
                    "descr TEXT," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table distribsLines (" +
                    "_id integer primary key autoincrement, " +
                    "distribs_id integer," +  // внутренний код
                    "contract_id text," +
                    "quantity double" +
                    ");"
        )

        db.execSQL(
            "create table distribs (" +
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
                    ");"
        )


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
        db.execSQL(
            "create table settings (" +
                    "_id integer primary key autoincrement, " +
                    "fmt text, " +
                    "ticket_m double," +
                    "ticket_w double," +
                    "agent_id text," +
                    "gps_interval ind default 0," +
                    "agent_price_type_id text" +
                    ");"
        )

        db.execSQL(
            "create table salesloaded (" +
                    "_id integer primary key autoincrement, " +
                    "ver int," +
                    "datedoc text," +
                    "numdoc text," +
                    "refdoc text," +
                    "client_id text," +
                    "curator_id text," +
                    "distr_point_id text," +
                    "nomenclature_id text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double," +
                    "UNIQUE ('nomenclature_id', 'refdoc')" +
                    ");"
        )

        db.execSQL(
            "create table nomenclature_hierarchy (" +
                    "id text," +
                    "ord_idx int," +
                    "groupDescr text," +
                    "level int," +
                    "level0_id text," +
                    "level1_id text," +
                    "level2_id text," +
                    "level3_id text," +
                    "level4_id text," +
                    "level5_id text," +
                    "level6_id text," +
                    "level7_id text," +
                    "level8_id text," +
                    "dont_use_in_hierarchy int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table salesL (" +
                    "nomenclature_id text," +
                    "client_id text," +
                    "distr_point_id text," +  // данные, загруженные из 1С (предыдущие периоды)
                    "strQuantity text," +  // продажи по датам N+N+N+N+
                    "quantity double," +  // общее количество продаж
                    // данные в телефоне (текущий период, обновляются при записи или удалении заказа, отбор по клиенту)
                    //"quantity_now double," +
                    "UNIQUE (nomenclature_id, client_id)" +
                    ");"
        )

        db.execSQL(
            "create table salesL2 (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +  // здесь номенклатура или группа (в зависимости от настроек)
                    "nomenclature_id text," +
                    "client_id text," +
                    "distr_point_id text," +
                    "datedoc text," +
                    "numdoc text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE places (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id TEXT," +
                    "code TEXT," +
                    "descr TEXT," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE occupied_places (" +
                    "place_id TEXT," +
                    "client_id TEXT," +
                    "document_id TEXT," +
                    "datedoc TEXT," +
                    "document TEXT," +
                    "shipping_time TEXT," +
                    "shipping_date TEXT," +
                    "isUsed int default 0," +
                    "UNIQUE ('place_id', 'client_id','document_id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE journal (" +
                    "_id integer primary key autoincrement, " +  // 0-order
                    // 1-refund
                    // 2-payment (но их в журнале сейчас нет, они отдельно)
                    // 3-distribs
                    "iddocdef int," +
                    "order_id INT," +
                    "payment_id INT," +
                    "refund_id INT," +
                    "distribs_id INT," +
                    "uid text," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "shipping_date text," +
                    "client_id text," +
                    "use_client_descr int," +
                    "client_descr text," +
                    "state int," +
                    "sum_doc double," +
                    "sum_shipping double default -1," +
                    "color text," +
                    "isUsed int default 0," +
                    "UNIQUE ('uid','id')," +
                    "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
                    ");"
        )


        //db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
        db.execSQL(
            "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                    "DELETE FROM journal WHERE order_id=OLD._id; " +
                    "END;"
        )


        //db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
        //      "FOR EACH ROW BEGIN " +
        //      "DELETE FROM journal WHERE payment_id=OLD._id; " +
        //      "END;");
        db.execSQL(
            "CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
                    "DELETE FROM journal WHERE refund_id=OLD._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
                    "DELETE FROM journal WHERE distribs_id=OLD._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                    "WHEN new.editing_backup=0 BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color=" + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " where order_id=new._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color=" + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " where refund_id=new._id; " +
                    "END;"
        )


        db.execSQL(
            "CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
                    " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color=" + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " where distribs_id=new._id; " +
                    "END;"
        )

        db.execSQL(
            "create table gps_coord (" +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpstype int," +
                    "gpsaccuracy float," +
                    "version int" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE equipment (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id TEXT," +
                    "code TEXT," +
                    "descr TEXT," +
                    "flags INT," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE equipment_rests (" +
                    "_id integer primary key autoincrement," +
                    "client_id TEXT," +
                    "agreement_id TEXT," +
                    "nomenclature_id TEXT," +
                    "distr_point_id TEXT," +
                    "quantity DOUBLE," +
                    "sum DOUBLE," +
                    "flags INT," +
                    "doc_id TEXT," +
                    "doc_descr TEXT," +
                    "date TEXT," +
                    "datepast TEXT," +
                    "UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')" +
                    ");"
        )

        db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);")

        db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));")

        db.execSQL(
            "create table routes (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "manager_id text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table routes_lines (" +
                    "_id integer primary key autoincrement, " +
                    "route_id text," +
                    "lineno int," +
                    "distr_point_id text," +
                    "visit_time text," +
                    "UNIQUE ('route_id', 'lineno')" +
                    ");"
        )

        db.execSQL(
            "create table routes_dates (" +
                    "_id integer primary key autoincrement, " +
                    "route_date text," +
                    "route_id text," +
                    "UNIQUE ('route_date')" +
                    ");"
        )

        //
        db.execSQL(
            "create table real_routes_dates (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +  // в КПК
                    "id text," +  // в 1С
                    "route_date text," +
                    "route_id text," +
                    "route_descr text," +
                    "UNIQUE ('route_date')" +
                    ");"
        )

        db.execSQL(
            "create table real_routes_lines (" +
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
                    "UNIQUE ('real_route_id', 'lineno')" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // есть еще onDowngrade

        Log.d(LOG_TAG, " --- onUpgrade database --- ")
        db.execSQL("delete from seances")

        if (oldVersion < 2) {
            db.execSQL(
                "create table settings (" +
                        "_id integer primary key autoincrement, " +
                        "fmt text" +
                        ");"
            )
            db.execSQL(
                "create table salesloaded (" +
                        "_id integer primary key autoincrement, " +
                        "ver int," +
                        "datedoc text," +
                        "numdoc text," +
                        "refdoc text," +
                        "curator_id text," +
                        "distr_point_id text," +
                        "nomenclature_id text," +
                        "quantity double," +  // здесь стоимость, а не цена
                        "price double" +
                        ");"
            )
            db.execSQL("alter table clients add column flags int")
        }
        if (oldVersion < 3) {
            db.execSQL(
                "create table sales_versions (" +
                        "param text," +
                        "ver int," +
                        "UNIQUE ('param')" +
                        ");"
            )
        }
        if (oldVersion < 4) {
            db.execSQL("alter table salesloaded add column client_id text")
        }

        if (oldVersion < 8) {
            db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature")
            db.execSQL("DROP TABLE nomenclature")
            db.execSQL(
                "CREATE TABLE Nomenclature (" +
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
                        "min_quantity DOUBLE," +  // минимальное продаваемое количество за 1 раз
                        "multiplicity DOUBLE," +  // кратность количества (нельзя дробить)
                        "required_sales DOUBLE," +  // план продажи за месяц по каждому клиенту
                        "flags INT," +
                        "UNIQUE ('id')" +
                        ");"
            )
            db.execSQL("insert into nomenclature select * from temp_Nomenclature")
            db.execSQL("drop table temp_Nomenclature")
            db.execSQL(
                "update nomenclature set parent_id=?",
                arrayOf<String>(Constants.emptyID)
            )
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )
        }

        if (oldVersion < 9) {
            db.execSQL("alter table messages add column type_idx int")
            db.execSQL("alter table messages add column date1 text")
            db.execSQL("alter table messages add column date2 text")
            db.execSQL("alter table messages add column client_id text")
            db.execSQL("alter table messages add column nomenclature_id text")
        }

        if (oldVersion < 10) {
            db.execSQL("alter table nomenclature add column descr_lower text")
            db.execSQL("alter table clients add column descr_lower text")
        }

        if (oldVersion < 11) {
            db.execSQL("alter table messages add column agreement_id text")
            db.execSQL("alter table messages add column report text")
            db.execSQL("alter table nomenclature add column parent_id_0 text")
            db.execSQL("alter table nomenclature add column parent_id_1 text")
            db.execSQL("alter table nomenclature add column parent_id_2 text")
            db.execSQL("alter table nomenclature add column parent_id_3 text")
            db.execSQL("alter table nomenclature add column parent_id_4 text")

            db.execSQL("alter table clients add column parent_id_0 text")
            db.execSQL("alter table clients add column parent_id_1 text")
            db.execSQL("alter table clients add column parent_id_2 text")
            db.execSQL("alter table clients add column parent_id_3 text")
            db.execSQL("alter table clients add column parent_id_4 text")
        }

        if (oldVersion < 12) {
            // nomenclature_hierarchy создается всегда, см. ниже
        }

        if (oldVersion < 13) {
            db.execSQL("alter table nomenclature add column image_file text")
            db.execSQL("alter table nomenclature add column image_file_checksum INT")
        }

        if (oldVersion < 14) {
            db.execSQL("alter table orders add column shipping_type int")
            db.execSQL("alter table orders add column shipping_time text")
            db.execSQL("alter table orders add column shipping_begin_time text")
            db.execSQL("alter table orders add column shipping_end_time text")
        }

        if (oldVersion < 15) {
            db.execSQL("alter table messages add column isMark int")

            db.execSQL(
                "create table curators_price (" +
                        "_id integer primary key autoincrement, " +
                        "curator_id text," +
                        "nomenclature_id text," +
                        "priceProcent double," +
                        "priceAdd double," +
                        "UNIQUE ('curator_id', 'nomenclature_id')" +
                        ");"
            )
        }

        if (oldVersion < 17) {
            db.execSQL("CREATE TEMPORARY TABLE messages_backup as select * from messages")
            db.execSQL("DROP TABLE messages")

            db.execSQL(
                "create table messages (" +
                        "_id integer primary key autoincrement," +
                        "uid text," +
                        "sender_id text," +
                        "receiver_id text," +
                        "text text," +
                        "fname text," +
                        "datetime text," +
                        "acknowledged int," +
                        "ver int," +
                        "type_idx int," +
                        "date1 text," +
                        "date2 text," +
                        "client_id text," +
                        "agreement_id text," +
                        "nomenclature_id text," +
                        "report text," +
                        "isMark int default 0," +
                        "isMarkCnt int default 0," +
                        "UNIQUE('uid')" +
                        ");"
            )

            db.execSQL("insert into messages select _id,uid,sender_id,receiver_id,text,fname,datetime,acknowledged,ver,type_idx,date1,date2,client_id,agreement_id,nomenclature_id,report,0 isMark,0 isMarkCnt from messages_backup")

            db.execSQL("drop table messages_backup")

            db.execSQL("update messages set isMark=0, isMarkCnt=0")
        }

        if (oldVersion < 18) {
            db.execSQL("alter table ordersLines add column discount DOUBLE")
        }

        if (oldVersion < 19) {
            // надо запросить торговые точки, в них была ошибка
            db.execSQL("update versions set ver=-1 where ver>0 and param=\"D_POINTS\"")
        }

        if (oldVersion < 20) {
            db.execSQL("alter table orders add column comment_payment text")
        }

        if (oldVersion < 21) {
            db.execSQL(
                "create table simple_discounts (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "code text," +
                        "descr text," +
                        "priceProcent double," +
                        "UNIQUE ('id')" +
                        ");"
            )
        }

        if (oldVersion < 22) {
            db.execSQL("alter table orders add column simple_discount_id text")
            db.execSQL("alter table orders add column create_client int")
            db.execSQL("alter table orders add column create_client_descr text")
            db.execSQL("alter table orders add column create_client_surname text")
            db.execSQL("alter table orders add column create_client_firstname text")
            db.execSQL("alter table orders add column create_client_lastname text")
            db.execSQL("alter table orders add column place_num int")
            db.execSQL("alter table orders add column card_num int")
            db.execSQL("alter table orders add column pay_credit double")
            db.execSQL("alter table orders add column ticket_m double")
            db.execSQL("alter table orders add column ticket_w double")
            db.execSQL("alter table orders add column quant_m int")
            db.execSQL("alter table orders add column quant_w int")

            db.execSQL("alter table settings add column ticket_m double")
            db.execSQL("alter table settings add column ticket_w double")
            db.execSQL("alter table settings add column agent_id text")

            db.execSQL(
                "create table cash_payments (" +
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
                        ");"
            )


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
            db.execSQL("alter table clients add column isUsed int default 0")
            db.execSQL("alter table pricetypes add column isUsed int default 0")
            db.execSQL("alter table nomenclature add column isUsed int default 0")
            db.execSQL("alter table prices add column isUsed int default 0")
            db.execSQL("alter table simple_discounts add column isUsed int default 0")
            db.execSQL("alter table saldo add column isUsed int default 0")
            db.execSQL("alter table rests add column isUsed int default 0")
        }

        if (oldVersion < 23) {
            db.execSQL(
                "CREATE TABLE saldo_extended (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "client_id TEXT," +
                        "agreement_id TEXT," +
                        "document_id TEXT," +
                        "manager_id TEXT," +
                        "document_descr TEXT," +
                        "manager_descr TEXT," +
                        "saldo DOUBLE," +
                        "saldo_past DOUBLE," +
                        "isUsed int default 0," +
                        "UNIQUE ('client_id','agreement_id','document_id','manager_id')" +
                        ");"
            )
        }

        if (oldVersion < 24) {
            db.execSQL("alter table orders add column create_client_phone text")
            db.execSQL("alter table clients add column card_num text")
            db.execSQL("alter table clients add column phone_num text")

            db.execSQL("alter table saldo_extended add column agreement_descr text")
            db.execSQL("alter table saldo_extended add column organization_descr text")
            db.execSQL("alter table saldo_extended add column document_datetime text")

            db.execSQL("alter table orders add column quant_mw int")

            db.execSQL("alter table agreements add column default_manager_id text")
            db.execSQL("update agreements set default_manager_id=''")

            db.execSQL("alter table cash_payments add column manager_descr text")
            db.execSQL("alter table cash_payments add column organization_descr text")
        }

        if (oldVersion < 25) {
            db.execSQL(
                "create table ordersPlaces (" +
                        "_id integer primary key autoincrement, " +
                        "order_id integer," +  // внутренний код
                        "place_id text" +
                        ");"
            )

            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")

            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN DELETE FROM ordersLines WHERE order_id=OLD._id; " +
                        "DELETE FROM ordersPlaces WHERE order_id=OLD._id; END;"
            )

            db.execSQL(
                "CREATE TABLE places (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id TEXT," +
                        "code TEXT," +
                        "descr TEXT," +
                        "isUsed int default 0," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "CREATE TABLE occupied_places (" +
                        "place_id TEXT," +
                        "client_id TEXT," +
                        "document_id TEXT," +
                        "datedoc TEXT," +
                        "document TEXT," +
                        "shipping_time TEXT," +
                        "shipping_date TEXT," +
                        "isUsed int default 0," +
                        "UNIQUE ('place_id', 'client_id','document_id')" +
                        ");"
            )

            db.execSQL("alter table nomenclature add column order_for_sorting INT")
            db.execSQL("update nomenclature set order_for_sorting=0")

            db.execSQL("alter table orders add column shipping_date text")
            db.execSQL("update orders set shipping_date=\"\"")

            db.execSQL("alter table ordersLines add column shipping_time text")
            db.execSQL("update ordersLines set shipping_time=\"\"")
        }

        if (oldVersion < 26) {
            db.execSQL("alter table orders add column manager_comment text")
            db.execSQL("update orders set manager_comment=\"\"")

            db.execSQL("alter table orders add column theme_comment text")
            db.execSQL("update orders set theme_comment=\"\"")

            db.execSQL("alter table orders add column phone_num text")
            db.execSQL("update orders set phone_num=\"\"")
        }

        if (oldVersion < 27) {
            db.execSQL("alter table ordersLines add column comment_in_line text")
            db.execSQL("update ordersLines set comment_in_line=\"\"")
        }

        if (oldVersion < 28) {
            db.execSQL("alter table nomenclature add column group_of_analogs INT")
            db.execSQL("update nomenclature set group_of_analogs=0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )


            db.execSQL(
                "create table refundsLines (" +
                        "_id integer primary key autoincrement, " +
                        "refund_id integer," +  // внутренний код
                        "nomenclature_id text," +
                        "client_id text," +
                        "quantity_requested double," +
                        "quantity double," +  //"price double," +
                        //"total double," +
                        //"discount DOUBLE,"+
                        "k double," +
                        "ed text," +
                        "comment_in_line text" +
                        ");"
            )

            db.execSQL(
                "create table refunds (" +
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
                        ");"
            )

            db.execSQL("alter table agreements add column flags int")
            db.execSQL("update agreements set flags=0")
        }

        if (oldVersion < 31) {
            db.execSQL("alter table saldo add column saldo_past30 DOUBLE default 0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "SALDO")
            )
        }
        if (oldVersion < 32) {
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "SALDO")
            )
        }
        if (oldVersion < 34) {
            db.execSQL("alter table orders add column sum_shipping double default -1")
        }

        if (oldVersion < 35) {
            db.execSQL("alter table nomenclature add column nomenclature_color INT default 0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )
        }

        if (oldVersion < 36) {
            db.execSQL("alter table cash_payments add column vicarious_power_id text")
            db.execSQL("alter table cash_payments add column vicarious_power_descr text")

            db.execSQL("update cash_payments set vicarious_power_id=''")
            db.execSQL("update cash_payments set vicarious_power_descr=''")

            db.execSQL(
                "create table vicarious_power (" +
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
                        ");"
            )
        }

        if (oldVersion < 37) {
            db.execSQL(
                "create table gps_coord (" +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "version int" +
                        ");"
            )

            db.execSQL("alter table settings add column gps_interval int default 0")
        }

        if (oldVersion < 38) {
            db.execSQL(
                "CREATE TABLE distribsContracts (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id TEXT," +
                        "position int," +
                        "code TEXT," +
                        "descr TEXT," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "create table distribsLines (" +
                        "_id integer primary key autoincrement, " +
                        "distribs_id integer," +  // внутренний код
                        "contract_id text," +
                        "quantity double" +
                        ");"
            )

            db.execSQL(
                "create table distribs (" +
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
                        ");"
            )
        }

        if (oldVersion < 39) {
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_update")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_update")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_update")


            // из-за того, что непонятно, как можно добавить distribs_id в UNIQUE,
            // удаляем журнал и снова его создаем. заполнится он при последующей реиндексации
            db.execSQL("DROP TABLE IF EXISTS journal")
            db.execSQL(
                "CREATE TABLE journal (" +
                        "_id integer primary key autoincrement, " +  // 0-order
                        // 1-refund
                        // 2-payment (но их в журнале сейчас нет, они отдельно)
                        // 3-distribs
                        "iddocdef int," +
                        "order_id INT," +
                        "payment_id INT," +
                        "refund_id INT," +
                        "distribs_id INT," +
                        "uid text," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "shipping_date text," +
                        "client_id text," +
                        "use_client_descr int," +
                        "client_descr text," +
                        "state int," +
                        "sum_doc double," +
                        "sum_shipping double default -1," +
                        "color text," +
                        "isUsed int default 0," +
                        "UNIQUE ('uid','id')," +
                        "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
                        ");"
            )


            //db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                        "DELETE FROM journal WHERE order_id=OLD._id; " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
            //      "FOR EACH ROW BEGIN " +
            //      "DELETE FROM journal WHERE payment_id=OLD._id; " +
            //      "END;");
            db.execSQL(
                "CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
                        "DELETE FROM journal WHERE refund_id=OLD._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
                        "DELETE FROM journal WHERE distribs_id=OLD._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color=" + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " where order_id=new._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color=" + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " where refund_id=new._id; " +
                        "END;"
            )


            db.execSQL(
                "CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
                        " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color=" + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " where distribs_id=new._id; " +
                        "END;"
            )
        }

        if (oldVersion < 41) {
            db.execSQL(
                "CREATE TABLE equipment (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id TEXT," +
                        "code TEXT," +
                        "descr TEXT," +
                        "flags INT," +
                        "UNIQUE ('id')" +
                        ");"
            )
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

        if (oldVersion < 42) {
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

        if (oldVersion < 43) {
            db.execSQL("alter table orders add column datecreation text default \"\"")
            db.execSQL("alter table orders add column gpsstate int default 0")
            db.execSQL("alter table orders add column gpsaccuracy float default 0")
            db.execSQL("alter table refunds add column datecreation text default \"\"")
            db.execSQL("alter table refunds add column gpsstate int default 0")
            db.execSQL("alter table refunds add column gpsaccuracy float default 0")
            db.execSQL("alter table distribs add column gpsstate int default 0")
            db.execSQL("alter table distribs add column gpsaccuracy float default 0")
            db.execSQL("alter table gps_coord add column gpsstate int default 0")
            db.execSQL("alter table gps_coord add column gpsaccuracy float default 0")
        }

        if (oldVersion < 44) {
            db.execSQL("drop TABLE IF EXISTS equipment_rests")

            db.execSQL(
                "CREATE TABLE equipment_rests (" +
                        "_id integer primary key autoincrement," +
                        "client_id TEXT," +
                        "agreement_id TEXT," +
                        "nomenclature_id TEXT," +
                        "distr_point_id TEXT," +
                        "quantity DOUBLE," +
                        "sum DOUBLE," +
                        "flags INT," +
                        "doc_id TEXT," +
                        "doc_descr TEXT," +
                        "date TEXT," +
                        "datepast TEXT," +
                        "UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')" +
                        ");"
            )

            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "EQUIPMENT_RESTS")
            )
        }

        if (oldVersion < 46) {
            db.execSQL("alter table ordersLines add column lineno int")
            db.execSQL("alter table orders add column editing_backup int")
            db.execSQL("alter table orders add column old_id int")
            db.execSQL("alter table distr_points add column price_type_id text")

            db.execSQL(
                "update orders set editing_backup=0, old_id=0, price_type_id=?",
                arrayOf<String>(
                    Constants.emptyID
                )
            )
            db.execSQL("update ordersLines set lineno=0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "D_POINTS")
            )
        }
        if (oldVersion < 47) {
            // Меняется только один триггер (insert), но т.к. таблицу переименовываем, и удаляем,
            // непонятно, что может произойти с триггером в разных версиях андроида, поэтому пересоздадим все
            //
            // ну и есть подозрение, что очищаются записи в журнале при удалении таблицы
            // поэтому код удаления перенес выше в версии 3.63
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_update")


            //SELECT name FROM sqlite_master WHERE type == 'index' AND lower(tbl_name) == 'orders'
            db.execSQL("ALTER TABLE orders RENAME TO orders_old")
            db.execSQL(
                "create table orders (" +
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
                        "sum_shipping double," +  // 0-документ в нормальном состоянии
                        // 1-новый документ, записать не успели
                        // 2-документ начали редактировать, но не записали и не отменили изменения
                        "editing_backup int," +  // для состояния 2 - _id редактируемого документа
                        // в других случаях - не важно
                        "old_id int" +  // До 19.02.2018 editing_backup тут не было
                        //"UNIQUE ('uid','id')" +
                        ");"
            )

            db.execSQL("PRAGMA foreign_keys=off")
            //db.execSQL("insert into orders select * from orders_old");
            // Важно перечислить поля, т.к. не у всех порядок полей совпадает (из-за обновлений старых версий)
            val all_fields_list =
                "_id,uid,version,version_ack,versionPDA,versionPDA_ack,id,numdoc,datedoc,client_id,agreement_id,distr_point_id," +
                        "comment,comment_closing,comment_payment,closed_not_full,state,curator_id,bw,trade_type," +
                        "datecreation,datecoord,latitude,longitude,gpsstate,gpsaccuracy,accept_coord,dont_need_send," +
                        "price_type_id,stock_id,sum_doc,weight_doc,shipping_type,shipping_time,shipping_begin_time,shipping_end_time," +
                        "shipping_date,simple_discount_id,create_client,create_client_descr,create_client_surname," +
                        "create_client_firstname,create_client_lastname,create_client_phone,place_num,card_num,pay_credit,ticket_m,ticket_w,quant_m," +
                        "quant_w,quant_mw,manager_comment,theme_comment,phone_num,sum_shipping,editing_backup,old_id"
            db.execSQL("insert into orders (" + all_fields_list + ") select " + all_fields_list + " from orders_old order by _id")
            db.execSQL("PRAGMA foreign_keys=on")
            // С 19.02.2018
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS OrdersUniqueIndex ON orders ('uid','id','editing_backup')")
            db.execSQL("drop table orders_old")

            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                        "DELETE FROM journal WHERE order_id=OLD._id; " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
            //        "WHEN new.editing_backup=0 BEGIN " +
            //        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
            //        "END;");
            db.execSQL(
                "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                        "WHEN new.editing_backup=0 BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
            //        "FOR EACH ROW BEGIN " +
            //        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color="+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" where order_id=new._id; " +
            //        "END;");
            db.execSQL(
                "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                        "WHEN new.editing_backup=0 BEGIN " +
                        "insert or replace into journal (uid, id, iddocdef, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color, order_id, payment_id, refund_id, distribs_id) values (new.uid, new.id, 0, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + ", new._id, 0, 0, 0); " +
                        "END;"
            )
        }
        if (oldVersion < 48) {
            db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);")
        }

        if (oldVersion < 49) {
            db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));")
        }

        if (oldVersion < 50) {
            // Поле добавится, но в разделе UNIQUE это поле прописано не будет
            // но т.к. у этой организации, где это важно, будет установлена новая версия,
            // а у всех остальных это поле всегда будет пустым, то это вообще не важно
            db.execSQL("alter table saldo_extended add column agreement30_id text")
            db.execSQL("update saldo_extended set agreement30_id=''")

            db.execSQL(
                "create table agreements30 (" +
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
                        ");"
            )

            db.execSQL("alter table orders add column agreement30_id text")
            db.execSQL("update orders set agreement30_id=''")

            db.execSQL(
                "create table prices_agreements30 (" +
                        "_id integer primary key autoincrement, " +
                        "agreement30_id text," +
                        "nomenclature_id text," +
                        "pack_id text," +  // Пока не используется
                        "ed_izm_id text," +
                        "edIzm text," +
                        "price double," +
                        "k double," +
                        "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                        ");"
            )
        }

        if (oldVersion < 51) {
            db.execSQL("alter table orders add column gpstype int")
            db.execSQL("alter table distribs add column gpstype int")
            db.execSQL("alter table gps_coord add column gpstype int")

            db.execSQL("update orders set gpstype=gpsstate")
            db.execSQL("update distribs set gpstype=gpsstate")
            db.execSQL("update gps_coord set gpstype=gpsstate")

            db.execSQL("alter table cash_payments add column datecreation text")
            db.execSQL("alter table cash_payments add column latitude text")
            db.execSQL("alter table cash_payments add column longitude text")
            db.execSQL("alter table cash_payments add column gpsstate int")
            db.execSQL("alter table cash_payments add column gpsaccuracy float")
            db.execSQL("alter table cash_payments add column gpstype int")

            db.execSQL("update cash_payments set datecreation=''")
            db.execSQL("update cash_payments set latitude=0.0")
            db.execSQL("update cash_payments set longitude=0.0")
            db.execSQL("update cash_payments set gpsstate=0")
            db.execSQL("update cash_payments set gpsaccuracy=0.0")
            db.execSQL("update cash_payments set gpstype=0")
        }

        if (oldVersion < 52) {
            db.execSQL("alter table cash_payments add column datecoord text")
            db.execSQL("update cash_payments set datecoord=''")
        }

        if (oldVersion < 53) {
            db.execSQL("alter table cash_payments add column accept_coord int")
            db.execSQL("update cash_payments set accept_coord=0")
        }

        if (oldVersion < 54) {
            db.execSQL("alter table nomenclature add column image_width int")
            db.execSQL("alter table nomenclature add column image_height int")
        }
        if (oldVersion < 55) {
            db.execSQL("alter table nomenclature add column image_file_size int")
        }

        if (oldVersion < 63) {
            db.execSQL(
                "create table routes (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "code text," +
                        "descr text," +
                        "manager_id text," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "create table routes_dates (" +
                        "_id integer primary key autoincrement, " +
                        "route_date text," +
                        "route_id text," +
                        "UNIQUE ('route_date')" +
                        ");"
            )

            db.execSQL(
                "create table routes_lines (" +
                        "_id integer primary key autoincrement, " +
                        "route_id text," +
                        "lineno int," +
                        "distr_point_id text," +
                        "visit_time text," +
                        "UNIQUE ('route_id', 'lineno')" +
                        ");"
            )
            //
            db.execSQL(
                "create table real_routes_dates (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +  // в КПК
                        "id text," +  // в 1С
                        "route_date text," +
                        "route_id text," +
                        "route_descr text," +
                        "UNIQUE ('route_date')" +
                        ");"
            )

            db.execSQL(
                "create table real_routes_lines (" +
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
                        "UNIQUE ('real_route_id', 'lineno')" +
                        ");"
            )

            db.execSQL("alter table cash_payments add column distr_point_id text")
        }

        if (oldVersion < 64) {
            db.execSQL("alter table real_routes_lines add column gpsstate int")
            db.execSQL("alter table real_routes_lines add column gpsaccuracy float")
            db.execSQL("alter table real_routes_lines add column accept_coord int")
            db.execSQL("update real_routes_lines set gpsstate=0")
            db.execSQL("update real_routes_lines set gpsaccuracy=0.0")
            db.execSQL("update real_routes_lines set accept_coord=0")
        }

        if (oldVersion < 66) {
            db.execSQL("alter table refunds add column shipping_type int")
            db.execSQL("update refunds set shipping_type=1")
        }

        if (oldVersion < 69) {
            //db.execSQL("alter table Rests add column organization_id text default ''");
            //db.execSQL("update Rests set organization_id=''");

            db.execSQL("CREATE TEMPORARY TABLE temp_Rests as select * from Rests")
            db.execSQL("DROP TABLE Rests")
            db.execSQL("alter table temp_Rests add column organization_id int")
            // Можно было EmptyID записать туда
            db.execSQL("update temp_Rests set organization_id=''")

            db.execSQL(
                "CREATE TABLE Rests (" +
                        "stock_id TEXT," +
                        "nomenclature_id TEXT," +
                        "organization_id TEXT default ''," +
                        "quantity DOUBLE," +
                        "quantity_reserve DOUBLE," +
                        "isUsed int default 0," +
                        "UNIQUE ('stock_id', 'nomenclature_id','organization_id')" +
                        ");"
            )

            db.execSQL("insert into Rests select * from temp_Rests")
            db.execSQL("drop table temp_Rests")
            // В принципе это не обязательно для всех, кроме Тандема (а они все равно начинают сначала)
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "RESTS")
            )
        }

        if (oldVersion < 70) {
            db.execSQL("alter table clients add column email_for_cheques text")
            db.execSQL("update clients set email_for_cheques=''")
        }

        if (oldVersion < 71) {
            db.execSQL("alter table settings add column agent_price_type_id text")
        }

        if (oldVersion < 72) {
            db.execSQL("alter table nomenclature add column compose_with text")
        }

        if (oldVersion < 73) {
            // есть подозрение, что там может быть null
            // при загрузке из текстовых файлов, в случае, когда это группа
            db.execSQL("update nomenclature set flags=0")

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

        if (oldVersion < 74) {
            db.execSQL("alter table nomenclature add column backorder int default 0")
            db.execSQL("update nomenclature set backorder=0")
        }

        if (oldVersion < 75) {
            db.execSQL("alter table agreements30 add column default_stock_id text default ''")
            db.execSQL("update agreements30 set default_stock_id=''")
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
        db.execSQL("drop view if exists salesVB")

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
        db.execSQL("drop view if exists salesV")

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
        db.execSQL("drop view if exists salesV_7")

        // Эти таблицы пересоздаем всегда
        db.execSQL("drop table if exists nomenclature_hierarchy")
        db.execSQL(
            "create table nomenclature_hierarchy (" +
                    "id text," +
                    "ord_idx int," +
                    "groupDescr text," +
                    "level int," +
                    "level0_id text," +
                    "level1_id text," +
                    "level2_id text," +
                    "level3_id text," +
                    "level4_id text," +
                    "level5_id text," +
                    "level6_id text," +
                    "level7_id text," +
                    "level8_id text," +
                    "dont_use_in_hierarchy int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL("drop table if exists salesL")
        db.execSQL(
            "create table salesL (" +
                    "nomenclature_id text," +
                    "client_id text," +  //"distr_point_id text," +
                    "strQuantity text," +
                    "quantity double," +  //"quantity_now double," +
                    "UNIQUE (nomenclature_id, client_id)" +
                    ");"
        )

        db.execSQL("drop table if exists salesL2")
        db.execSQL(
            "create table salesL2 (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +  // здесь номенклатура или группа (в зависимости от настроек)
                    "nomenclature_id text," +
                    "client_id text," +  // 20.09.2019
                    "distr_point_id text," +  //
                    "datedoc text," +
                    "numdoc text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double" +
                    ");"
        )

        // ^^^^ таблицы истории продаж перезаполняются при изменении версии базы из MainActivity
        db.execSQL(
            String.format(
                "insert into seances(incoming) values ('UPGRADED%d')",
                oldVersion
            )
        )
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //
        onUpgrade(db, oldVersion, newVersion)
    }

    fun Reindex() {
        val db = getWritableDatabase()

        db.execSQL("DROP INDEX IF EXISTS Clients_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Clients_parent_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Clients_isFolder_idx")
        db.execSQL("DROP INDEX IF EXISTS organizations_idx")
        db.execSQL("DROP INDEX IF EXISTS agreements_id_idx")
        db.execSQL("DROP INDEX IF EXISTS agreements_owner_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_parent_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_isFolder_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_descr_idx")
        db.execSQL("DROP INDEX IF EXISTS Rests_nomenclature_stocks_idx")
        db.execSQL("DROP INDEX IF EXISTS prices_nomenclature_price_idx")
        db.execSQL("DROP INDEX IF EXISTS clients_price_client_idx")
        db.execSQL("DROP INDEX IF EXISTS curators_price_curator_idx")
        db.execSQL("DROP INDEX IF EXISTS agents_idx")
        db.execSQL("DROP INDEX IF EXISTS curators_idx")
        db.execSQL("DROP INDEX IF EXISTS Saldo_client_idx")
        db.execSQL("DROP INDEX IF EXISTS saldo_extended_client_idx")
        db.execSQL("DROP INDEX IF EXISTS distr_points_idx")
        db.execSQL("DROP INDEX IF EXISTS OrdersLines_idx")
        db.execSQL("DROP INDEX IF EXISTS OrdersLines_sales")
        db.execSQL("DROP INDEX IF EXISTS orders_idx")
        db.execSQL("DROP INDEX IF EXISTS orders_datedoc")
        db.execSQL("DROP INDEX IF EXISTS orders_uid")
        db.execSQL("DROP INDEX IF EXISTS orders_state")
        db.execSQL("DROP INDEX IF EXISTS salesloaded_idx")
        db.execSQL("DROP INDEX IF EXISTS unique_salesloaded_idx")
        db.execSQL("DROP INDEX IF EXISTS journal_order_id")
        db.execSQL("DROP INDEX IF EXISTS journal_payment_id")
        db.execSQL("DROP INDEX IF EXISTS journal_refund_id")
        db.execSQL("DROP INDEX IF EXISTS salesL2_idx")
        //db.execSQL("DROP INDEX IF EXISTS salesL2_no_dp_idx");
        db.execSQL("DROP INDEX IF EXISTS equipment_rests_client_id")
        db.execSQL("DROP INDEX IF EXISTS agreements30_owner_id")

        db.execSQL("CREATE INDEX Clients_id_idx ON Clients (id ASC);")
        db.execSQL("CREATE INDEX Clients_parent_id_idx ON Clients (parent_id ASC);")
        db.execSQL("CREATE INDEX Clients_isFolder_idx ON Clients (isFolder ASC);")
        db.execSQL("CREATE INDEX organizations_idx ON organizations (id ASC);")
        db.execSQL("CREATE INDEX agreements_id_idx ON agreements (id ASC);")
        db.execSQL("CREATE INDEX agreements_owner_id_idx ON agreements (owner_id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_id_idx ON Nomenclature (id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_parent_id_idx ON Nomenclature (parent_id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_isFolder_idx ON Nomenclature (isFolder ASC);")
        db.execSQL("CREATE INDEX Nomenclature_descr_idx ON Nomenclature (descr ASC);")
        db.execSQL("CREATE INDEX Rests_nomenclature_stocks_idx ON Rests (nomenclature_id ASC, stock_id ASC);")
        db.execSQL("CREATE INDEX prices_nomenclature_price_idx ON prices (nomenclature_id ASC, price_type_id ASC);")
        db.execSQL("CREATE INDEX clients_price_client_idx ON clients_price (client_id ASC);")
        db.execSQL("CREATE INDEX curators_price_curator_idx ON curators_price (curator_id ASC);")
        db.execSQL("CREATE INDEX agents_idx ON agents (id ASC);")
        db.execSQL("CREATE INDEX curators_idx ON curators (id ASC);")
        db.execSQL("CREATE INDEX Saldo_client_idx ON Saldo (client_id ASC);")
        db.execSQL("CREATE INDEX saldo_extended_client_idx ON saldo_extended (client_id ASC);")
        db.execSQL("CREATE INDEX distr_points_idx ON distr_points (id ASC);")
        db.execSQL("CREATE INDEX OrdersLines_idx ON ordersLines (order_id ASC);")
        db.execSQL("CREATE INDEX OrdersLines_sales ON ordersLines (nomenclature_id,client_id ASC);")
        db.execSQL("CREATE INDEX orders_idx ON orders (id ASC);")
        db.execSQL("CREATE INDEX orders_datedoc ON orders (datedoc ASC);")
        db.execSQL("CREATE INDEX orders_uid ON orders (uid ASC);")
        db.execSQL("CREATE INDEX orders_state ON orders (state ASC, accept_coord ASC);")
        db.execSQL("CREATE INDEX salesloaded_idx ON salesloaded (nomenclature_id ASC, client_id ASC, distr_point_id ASC, datedoc ASC);")
        db.execSQL("CREATE INDEX unique_salesloaded_idx ON salesloaded (nomenclature_id ASC, refdoc ASC);")
        db.execSQL("CREATE INDEX journal_order_id ON journal (order_id ASC);")
        db.execSQL("CREATE INDEX journal_payment_id ON journal (payment_id ASC);")
        db.execSQL("CREATE INDEX journal_refund_id ON journal (refund_id ASC);")
        db.execSQL("CREATE INDEX equipment_rests_client_id ON equipment_rests (client_id ASC, agreement_id ASC);")
        //db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, distr_point_id ASC, nomenclature_id ASC, datedoc ASC);");
        //db.execSQL("CREATE INDEX salesL2_no_dp_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);");
        db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);")
        db.execSQL("CREATE INDEX agreements30_owner_id ON agreements30 (owner_id ASC);")

        // TODO сделать удаление несуществующих и добавление новых
        // (из-за того, что таким образом нарушится сортировка, т.е. порядок ввода документов)
        //db.execSQL("delete from journal");
        db.execSQL("update journal set isUsed=0")

        //db.execSQL("INSERT OR REPLACE INTO journal select j._id, d._id as order_id, 0 as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, d.create_client as use_client_descr, d.create_client_descr as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from orders d left join journal j on j.order_id=d._id;");
        ////db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, d._id as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from cash_payments d join journal j on j.payment_id=d._id;");
        //db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, 0 as payment_id, d._id as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from refunds d left join journal j on j.refund_id=d._id;");
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 0 as iddocdef, new._id as order_id, 0 as payment_id, 0 as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date as shipping_date, new.client_id, new.create_client as use_client_descr, new.create_client_descr as client_descr, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from orders new left join journal j on j.order_id=new._id where editing_backup=0;")
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 1 as iddocdef, 0 as order_id, 0 as payment_id, new._id as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, new.sum_doc, 0 as sum_shipping, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from refunds new left join journal j on j.refund_id=new._id;")
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 3 as iddocdef, 0 as order_id, 0 as payment_id, 0 as refund_id, new._id as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, 0 as sum_doc, 0 as sum_shipping, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from distribs new left join journal j on j.distribs_id=new._id;")

        db.execSQL("delete from journal where isUsed=0")
    }

    fun Vacuum() {
        val db = getWritableDatabase()
        db.execSQL("vacuum")
    }

    fun readAgents(): ArrayList<AgentsModel> {
        // on below line we are creating a database for reading our database.
        val db = this.readableDatabase

        // on below line we are creating a cursor with query to read data from database.
        val cursorCourses: Cursor = db.rawQuery("SELECT _id, id, code, descr FROM $TABLE_NAME", null)

        // on below line we are creating a new array list.
        val courseModelArrayList: ArrayList<AgentsModel> = ArrayList()

        // moving our cursor to first position.
        if (cursorCourses.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                courseModelArrayList.add(
                    AgentsModel(
                        cursorCourses.getLong(0),
                        cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getString(3),
                    )
                )
            } while (cursorCourses.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor and returning our array list.
        cursorCourses.close()
        return courseModelArrayList
    }

    companion object {

        // БД
        //private static final String DIR = "/sdcard";
        const val DB_NAME: String = "mdata.db"
        const val DB_VERSION: Int = 75

        private const val TABLE_NAME = "agents"

        val JOURNAL_ORDER_COLOR_FOR_TRIGGER: String =
            "case when new.dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                    "when new.closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                    "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_ORDER_STATE.E_ORDER_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end"

    }
}


