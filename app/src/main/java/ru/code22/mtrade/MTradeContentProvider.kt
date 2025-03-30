package ru.code22.mtrade

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MTradeContentProvider : ContentProvider() {

    private var dbHelper: MTradeDBHelper? = null

    override fun delete(uri: Uri, where: String?, selectionArgs: Array<String?>?): Int {
        var where = where
        when (sUriMatcher.match(uri)) {
            URI_NOMENCLATURE -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(NOMENCLATURE_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(NOMENCLATURE_CONTENT_URI, null)
                return cnt
            }

            URI_NOMENCLATURE_HIERARCHY -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(NOMENCLATURE_HIERARCHY_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver()
                    .notifyChange(NOMENCLATURE_HIERARCHY_CONTENT_URI, null)
                return cnt
            }

            URI_PLACES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(PLACES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(PLACES_CONTENT_URI, null)
                return cnt
            }

            URI_OCCUPIED_PLACES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(OCCUPIED_PLACES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(OCCUPIED_PLACES_CONTENT_URI, null)
                return cnt
            }

            URI_CLIENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(CLIENTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(CLIENTS_CONTENT_URI, null)
                return cnt
            }

            URI_AGREEMENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(AGREEMENTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(AGREEMENTS_CONTENT_URI, null)
                return cnt
            }

            URI_AGREEMENTS30 -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(AGREEMENTS30_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(AGREEMENTS30_CONTENT_URI, null)
                return cnt
            }

            URI_SALDO -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(SALDO_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(SALDO_CONTENT_URI, null)
                return cnt
            }

            URI_SALDO_EXTENDED -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(SALDO_EXTENDED_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(SALDO_EXTENDED_CONTENT_URI, null)
                return cnt
            }

            URI_RESTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(RESTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(RESTS_CONTENT_URI, null)
                return cnt
            }

            URI_ORGANIZATIONS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ORGANIZATIONS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ORGANIZATIONS_CONTENT_URI, null)
                return cnt
            }

            URI_STOCKS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(STOCKS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(STOCKS_CONTENT_URI, null)
                return cnt
            }

            URI_AGENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(AGENTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(AGENTS_CONTENT_URI, null)
                return cnt
            }

            URI_DISTRIBS_CONTRACTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(DISTRIBS_CONTRACTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver()
                    .notifyChange(DISTRIBS_CONTRACTS_CONTENT_URI, null)
                return cnt
            }

            URI_DISTR_POINTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(DISTR_POINTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(DISTR_POINTS_CONTENT_URI, null)
                return cnt
            }

            URI_PRICETYPES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(PRICETYPES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(PRICETYPES_CONTENT_URI, null)
                return cnt
            }

            URI_PRICES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(PRICES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(PRICES_CONTENT_URI, null)
                return cnt
            }

            URI_PRICES_AGREEMENTS30 -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(PRICES_AGREEMENTS30_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver()
                    .notifyChange(PRICES_AGREEMENTS30_CONTENT_URI, null)
                return cnt
            }

            URI_CLIENTS_PRICE -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(CLIENTS_PRICE_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(CLIENTS_PRICE_CONTENT_URI, null)
                return cnt
            }

            URI_CURATORS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(CURATORS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(CURATORS_CONTENT_URI, null)
                return cnt
            }

            URI_CURATORS_PRICE -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(CURATORS_PRICE_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(CURATORS_PRICE_CONTENT_URI, null)
                return cnt
            }

            URI_SIMPLE_DISCOUNTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(SIMPLE_DISCOUNTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(SIMPLE_DISCOUNTS_CONTENT_URI, null)
                return cnt
            }

            URI_ORDERS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
                    getContext()!!.getContentResolver()
                        .notifyChange(ORDERS_JOURNAL_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                return cnt
            }

            URI_REFUNDS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                return cnt
            }

            URI_DISTRIBS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(DISTRIBS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                return cnt
            }

            URI_ORDERS_SILENT_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
                return cnt
            }

            URI_REFUNDS_SILENT_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
                return cnt
            }

            URI_ORDERS_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "order_id=?",
                    arrayOf<String?>(_id),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                }
                cursor.close()
                val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            ORDERS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            ORDERS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    if (journal_id != 0L) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                }
                return cnt
            }

            URI_REFUNDS_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "refund_id=?",
                    arrayOf<String?>(_id),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                }
                cursor.close()
                val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            REFUNDS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    if (journal_id != 0L) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                }
                return cnt
            }

            URI_DISTRIBS_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "distribs_id=?",
                    arrayOf<String?>(_id),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                }
                cursor.close()
                val cnt = db.delete(DISTRIBS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            DISTRIBS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    if (journal_id != 0L) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                }
                return cnt
            }

            URI_ORDERS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ORDERS_LINES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ORDERS_LINES_CONTENT_URI, null)
                return cnt
            }

            URI_REFUNDS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(REFUNDS_LINES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(REFUNDS_LINES_CONTENT_URI, null)
                return cnt
            }

            URI_DISTRIBS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(DISTRIBS_LINES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(DISTRIBS_LINES_CONTENT_URI, null)
                return cnt
            }

            URI_ORDERS_PLACES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ORDERS_PLACES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ORDERS_PLACES_CONTENT_URI, null)
                return cnt
            }

            URI_CASH_PAYMENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver()
                        .notifyChange(CASH_PAYMENTS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(
                        CASH_PAYMENTS_JOURNAL_CONTENT_URI, null
                    )
                }
                return cnt
            }

            URI_CASH_PAYMENTS_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val cnt = db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            CASH_PAYMENTS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            CASH_PAYMENTS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                }
                return cnt
            }

            URI_VERSIONS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(VERSIONS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(VERSIONS_CONTENT_URI, null)
                return cnt
            }

            URI_VERSIONS_SALES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(VERSIONS_SALES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(VERSIONS_SALES_CONTENT_URI, null)
                return cnt
            }

            URI_SALES_LOADED -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(SALES_LOADED_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(SALES_LOADED_CONTENT_URI, null)
                return cnt
            }

            URI_VICARIOUS_POWER -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(VICARIOUS_POWER_CONTENT_URI, null)
                return cnt
            }

            URI_VICARIOUS_POWER_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val cnt = db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            VICARIOUS_POWER_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                }
                return cnt
            }

            URI_MESSAGES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(MESSAGES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(MESSAGES_LIST_CONTENT_URI, null)
                return cnt
            }

            URI_MESSAGES_ID -> {
                val db = dbHelper!!.getWritableDatabase()
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val cnt = db.delete(MESSAGES_TABLE, where, selectionArgs)
                if (cnt > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            MESSAGES_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            MESSAGES_LIST_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                }
                return cnt
            }

            URI_GPS_COORD -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(GPS_COORD_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(GPS_COORD_CONTENT_URI, null)
                return cnt
            }

            URI_EQUIPMENT -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(EQUIPMENT_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(EQUIPMENT_CONTENT_URI, null)
                return cnt
            }

            URI_EQUIPMENT_RESTS -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(EQUIPMENT_RESTS_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(EQUIPMENT_RESTS_CONTENT_URI, null)
                return cnt
            }

            URI_ROUTES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ROUTES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ROUTES_CONTENT_URI, null)
                return cnt
            }

            URI_ROUTES_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ROUTES_LINES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ROUTES_LINES_CONTENT_URI, null)
                return cnt
            }

            URI_ROUTES_DATES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(ROUTES_DATES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver().notifyChange(ROUTES_DATES_CONTENT_URI, null)
                return cnt
            }

            URI_REAL_ROUTES_DATES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(REAL_ROUTES_DATES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver()
                    .notifyChange(REAL_ROUTES_DATES_CONTENT_URI, null)
                return cnt
            }

            URI_REAL_ROUTES_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                val cnt = db.delete(REAL_ROUTES_LINES_TABLE, where, selectionArgs)
                getContext()!!.getContentResolver()
                    .notifyChange(REAL_ROUTES_LINES_CONTENT_URI, null)
                return cnt
            }


            else -> throw IllegalArgumentException("Unknown URI (for delete)" + uri)
        }
    }

    /**
     * This reads a file from the given Resource-Id and calls every line of it as a SQL-Statement
     *
     * @param context
     *
     * @param resourceId
     * e.g. R.raw.food_db
     *
     * @return Number of SQL-Statements run
     * @throws IOException
     */
    @Throws(IOException::class)
    fun insertFromFile(context: Context, db: SQLiteDatabase, resourceId: Int): Int {
        // Reseting Counter
        var result = 0

        // Open the resource
        val insertsStream = context.getResources().openRawResource(resourceId)
        val insertReader = BufferedReader(InputStreamReader(insertsStream))

        // Iterate through lines (assuming each insert has its own line and theres no other stuff)
        while (insertReader.ready()) {
            val insertStmt = insertReader.readLine()
            db.execSQL(insertStmt)
            result++
        }
        insertReader.close()

        // returning number of inserted rows
        return result
    }


    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(LOG_TAG, "insert, " + uri.toString())
        when (sUriMatcher.match(uri)) {
            URI_NOMENCLATURE -> {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(NOMENCLATURE_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    NOMENCLATURE_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(NOMENCLATURE_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_NOMENCLATURE_ID -> {
                val id = uri.getLastPathSegment()
                Log.d(LOG_TAG, "URI_NOMENCLATURE_ID, " + id)
                val db = dbHelper!!.getWritableDatabase()
                val rowID = db.insert(NOMENCLATURE_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_PLACES -> {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(PLACES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    PLACES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(PLACES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_OCCUPIED_PLACES -> {
                //Log.d(LOG_TAG, "URI_NOMENCLATURE");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "place_id=? and client_id=? and document_id=?"
                val selectionArgs = arrayOf<String?>(
                    values!!.getAsString("place_id"),
                    values!!.getAsString("client_id"),
                    values!!.getAsString("document_id")
                )

                //Do an update if the constraints match
                db.update(OCCUPIED_PLACES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    OCCUPIED_PLACES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(OCCUPIED_PLACES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CLIENTS -> {
                Log.d(LOG_TAG, "URI_CLIENTS")
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(CLIENTS_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    CLIENTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CLIENTS_ID -> {
                val id = uri.getLastPathSegment()
                Log.d(LOG_TAG, "URI_CLIENTS_ID, " + id)
                val db = dbHelper!!.getWritableDatabase()
                val rowID = db.insert(CLIENTS_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_RESTS -> {
                Log.d(LOG_TAG, "URI_RESTS")
                val db = dbHelper!!.getWritableDatabase()

                var selection = "nomenclature_id=? and stock_id=?"
                var selectionArgs = arrayOf<String?>(
                    values!!.getAsString("nomenclature_id"),
                    values.getAsString("stock_id")
                )

                // Тандем
                if (values.containsKey("ogranization_id")) {
                    selection = "nomenclature_id=? and stock_id=? and ogranization_id=?"
                    selectionArgs = arrayOf<String?>(
                        values.getAsString("nomenclature_id"),
                        values.getAsString("stock_id"),
                        values.getAsString("ogranization_id")
                    )
                }

                //Do an update if the constraints match
                db.update(RESTS_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    RESTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(RESTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_SALDO -> {
                Log.d(LOG_TAG, "URI_SALDO")
                val db = dbHelper!!.getWritableDatabase()

                val selection = "client_id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("client_id"))

                //Do an update if the constraints match
                db.update(SALDO_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    SALDO_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(SALDO_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_SALDO_EXTENDED -> {
                Log.d(LOG_TAG, "URI_SALDO_EXTENDED")
                val db = dbHelper!!.getWritableDatabase()

                val selection =
                    "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?"
                val selectionArgs = arrayOf<String?>(
                    values!!.getAsString("client_id"),
                    values.getAsString("agreement30_id"),
                    values.getAsString("agreement_id"),
                    values.getAsString("document_id"),
                    values.getAsString("manager_id")
                )

                //Do an update if the constraints match
                db.update(SALDO_EXTENDED_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    SALDO_EXTENDED_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(SALDO_EXTENDED_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_VERSIONS -> {
                Log.d(LOG_TAG, "URI_VERSIONS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    VERSIONS_TABLE,
                    values,
                    "param=?",
                    arrayOf<String?>(values!!.getAsString("param"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    VERSIONS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(VERSIONS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_VERSIONS_SALES -> {
                Log.d(LOG_TAG, "URI_VERSIONS_SALES")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    VERSIONS_SALES_TABLE,
                    values,
                    "param=?",
                    arrayOf<String?>(values!!.getAsString("param"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    VERSIONS_SALES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(VERSIONS_SALES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_SEANCES_INCOMING -> {
                Log.d(LOG_TAG, "URI_SEANCES_INCOMING")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=ON")
                if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                    db.delete(SEANCES_TABLE, null, null)
                    db.insert(SEANCES_TABLE, null, values)
                }
                db.execSQL("PRAGMA synchronous=NORMAL")
                return null
            }

            URI_SEANCES_OUTGOING -> {
                Log.d(LOG_TAG, "URI_SEANCES_OUTGOING")
                val db = dbHelper!!.getWritableDatabase()
                if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                    db.delete(SEANCES_TABLE, null, null)
                    db.insert(SEANCES_TABLE, null, values)
                }
                db.execSQL("PRAGMA synchronous=ON")
                return null
            }

            URI_REINDEX -> {
                Log.d(LOG_TAG, "URI_REINDEX")
                dbHelper!!.Reindex()
                return null
            }

            URI_VACUUM -> {
                Log.d(LOG_TAG, "URI_VACUUM")
                dbHelper!!.Vacuum()
                return null
            }


            URI_SORT -> {
                Log.d(LOG_TAG, "URI_SORT")
                val sortType = values!!.getAsString("sort_type")
                if (sortType == "orders_by_date") {
                    val db = dbHelper!!.getWritableDatabase()
                    db.execSQL("drop table if exists temp_old_ids")
                    db.execSQL(
                        "CREATE TABLE temp_old_ids (" +
                                "_id integer primary key autoincrement," +
                                "_id_old integer)"
                    )
                    db.execSQL("drop table if exists temp_new_ids")
                    db.execSQL(
                        "CREATE TABLE temp_new_ids (" +
                                "_id integer primary key autoincrement," +
                                "_id_new integer)"
                    )

                    // сначала в первую таблицу записываем старые коды в старом порядке
                    // distinct на случай, если дублируются коды order_id
                    db.execSQL(
                        "insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                                "left join orders on orders._id=journal.order_id " +
                                "left join refunds on refunds._id=journal.refund_id " +
                                "order by journal._id"
                    )
                    // а потом во вторую - в новом порядке
                    db.execSQL(
                        "insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                                "left join orders on orders._id=journal.order_id " +
                                "left join refunds on refunds._id=journal.refund_id " +
                                "order by ifnull(orders.datedoc, refunds.datedoc);"
                    )
                    // количество записей будет совпадать
                    //db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);");
                    db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);")
                    db.execSQL("UPDATE journal set _id = -_id")

                    db.execSQL("drop table if exists temp_old_ids")
                    db.execSQL("drop table if exists temp_new_ids")
                } else if (sortType == "orders_by_service_date") {
                    val db = dbHelper!!.getWritableDatabase()
                    db.execSQL("drop table if exists temp_old_ids")
                    db.execSQL(
                        "CREATE TABLE temp_old_ids (" +
                                "_id integer primary key autoincrement," +
                                "_id_old integer)"
                    )
                    db.execSQL("drop table if exists temp_new_ids")
                    db.execSQL(
                        "CREATE TABLE temp_new_ids (" +
                                "_id integer primary key autoincrement," +
                                "_id_new integer)"
                    )

                    // сначала в первую таблицу записываем старые коды в старом порядке
                    // distinct на случай, если дублируются коды order_id
                    db.execSQL(
                        "insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                                "left join orders on orders._id=journal.order_id " +
                                "left join refunds on refunds._id=journal.refund_id " +
                                "order by journal._id desc"
                    )
                    // а потом во вторую - в новом порядке
                    db.execSQL(
                        "insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                                "left join orders on orders._id=journal.order_id " +
                                "left join refunds on refunds._id=journal.refund_id " +
                                "order by ifnull(orders.shipping_date, refunds.datedoc) desc;"
                    )
                    // количество записей будет совпадать
                    //db.execSQL("UPDATE journal set _id = -(select _id_new from temp_new_ids join temp_old_ids on temp_old_ids._id=temp_new_ids._id where temp_old_ids._id_old=journal._id);");
                    db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);")
                    db.execSQL("UPDATE journal set _id = -_id")

                    db.execSQL("drop table if exists temp_old_ids")
                    db.execSQL("drop table if exists temp_new_ids")
                }
                getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)


                return null
            }


            URI_CREATE_VIEWS -> {
                // TODO отбор по торговым точкам
                // а вообще salesVB нигде не используется уже
                // вместо него rests_sales_stuff
                Log.d(LOG_TAG, "URI_CREATE_VIEWS")
                val db = dbHelper!!.getWritableDatabase()
                // Продажи в телефоне (текущие)
                db.execSQL("drop view if exists salesVB")
                db.execSQL(
                    "CREATE VIEW salesVB as select " +
                            "nomenclature_id, " +
                            "sum(quantity) as quantity_saled from orders " +
                            "join ordersLines on ordersLines.order_id=orders._id and orders.client_id=\"" + values!!.getAsString(
                        "client_id"
                    ) + "\" " +
                            "where orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                            "group by nomenclature_id;"
                )

                // Продажи в 1С (история)
                db.execSQL("drop view if exists salesV")

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
                db.execSQL(
                    "CREATE VIEW salesV as select " +
                            "nomenclature_id," +
                            "strQuantity " +
                            "from salesL " +
                            "where client_id=\"" + values!!.getAsString("client_id") + "\" " +
                            ";"
                )

                db.execSQL("drop view if exists salesV_7")

                db.execSQL(
                    "CREATE VIEW salesV_7 as " +
                            "select " +
                            "nomenclature_id," +
                            "sum(case when datedoc between '" + values!!.getAsString("dt1b") + "' and '" + values!!.getAsString(
                        "dt1e"
                    ) + "' then quantity else 0 end) quantity7_1," +
                            "sum(case when datedoc between '" + values.getAsString("dt2b") + "' and '" + values.getAsString(
                        "dt2e"
                    ) + "' then quantity else 0 end) quantity7_2," +
                            "sum(case when datedoc between '" + values.getAsString("dt3b") + "' and '" + values.getAsString(
                        "dt3e"
                    ) + "' then quantity else 0 end) quantity7_3," +
                            "sum(case when datedoc between '" + values.getAsString("dt4b") + "' and '" + values.getAsString(
                        "dt4e"
                    ) + "' then quantity else 0 end) quantity7_4 " +
                            "from " +
                            "(" +
                            "select " +
                            "nomenclature_id," +
                            "datedoc," +
                            "case when ifnull(sum(quantity_server), 0)>=ifnull(sum(quantity_pda), 0) then ifnull(sum(quantity_server), 0) else sum(quantity_pda) end quantity " +
                            "from" +
                            "(" +
                            "select nomenclature_id, substr(datedoc, 1, 8) datedoc, quantity quantity_server, 0 quantity_pda from salesL2 " +  // Оставим только группы
                            "join nomenclature n on n.id=nomenclature_id and n.isFolder=1 " +
                            "where client_id='" + values.getAsString("client_id") + "' and datedoc between '" + values.getAsString(
                        "dtb"
                    ) + "' and '" + values.getAsString("dte") + "' " +
                            "union all " +
                            "select n.parent_id nomenclature_id, substr(datedoc, 1, 8) datedoc, 0 quantity_server, quantity quantity_pda from orders " +
                            "join ordersLines on ordersLines.order_id=orders._id " +
                            "join nomenclature n on n.id=ordersLines.nomenclature_id " +
                            "where datedoc between '" + values.getAsString("dtb") + "' and '" + values.getAsString(
                        "dte"
                    ) + "' and orders.client_id='" + values.getAsString("client_id") + "' " +
                            "and orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                            ") sales0 " +
                            "group by nomenclature_id, datedoc" +
                            ") sales " +
                            "group by nomenclature_id" +
                            ";"
                )

                return null
            }

            URI_CREATE_SALES_L -> {
                Log.d(LOG_TAG, "URI_RECALC_SALES_IN_LINE")
                val db = dbHelper!!.getWritableDatabase()

                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()

                db.execSQL("delete from salesL")
                db.execSQL("delete from salesL2")

                // Заполнение salesL
                //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, distr_point_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, distr_point_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
                val cursor = db.rawQuery(
                    "select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc",
                    null
                )
                // '    45   '
                //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id where s.nomenclature_id='    45   ' group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
                //
                val nomenclatureIdIndex = cursor.getColumnIndex("nomenclature_id")
                val clientIdIndex = cursor.getColumnIndex("client_id")
                //int distrPointIdIndex=cursor.getColumnIndex("distr_point_id");
                //int datedocIndex=cursor.getColumnIndex("datedoc");
                val quantityIndex = cursor.getColumnIndex("quantity")
                var prevNomenclatureId = ""
                var prevClientId = ""
                //String prevDistrPointId="";
                var strQuantity = ""
                var cnt = 0
                var quantityTotal = 0.0

                try {
                    while (cursor.moveToNext()) {
                        val nomenclatureId = cursor.getString(nomenclatureIdIndex)
                        val clientId = cursor.getString(clientIdIndex)
                        //String distrPointId=cursor.getString(distrPointIdIndex);
                        //String datedoc=cursor.getString(datedocIndex);
                        val quantity = cursor.getDouble(quantityIndex)
                        //if (!prevNomenclatureId.equals(nomenclatureId)||!(prevClientId.equals(clientId))||!(prevDistrPointId.equals(distrPointId)))
                        if (prevNomenclatureId != nomenclatureId || !(prevClientId == clientId)) {
                            if (quantityTotal > 0.001) {
                                val cv = ContentValues()
                                cv.put("nomenclature_id", prevNomenclatureId)
                                cv.put("client_id", prevClientId)
                                //cv.put("distr_point_id", prevDistrPointId);
                                cv.put("strQuantity", strQuantity)
                                cv.put("quantity", quantityTotal)

                                //cv.put("quantity_now", 0.0);

                                //db.insert(SALES_L_TABLE, null, cv);
                                db.insertWithOnConflict(
                                    SALES_L_TABLE,
                                    null,
                                    cv,
                                    SQLiteDatabase.CONFLICT_IGNORE
                                )
                            }

                            prevNomenclatureId = nomenclatureId
                            prevClientId = clientId

                            //prevDistrPointId=distrPointId;
                            quantityTotal = 0.0
                            strQuantity = ""
                            cnt = 0
                        }
                        if (quantity > 0.001) {
                            quantityTotal += quantity
                            if (strQuantity.isEmpty()) {
                                strQuantity = Common.DoubleToStringFormat(quantity, "%.3f")
                            } else {
                                cnt++
                                if (cnt == 4) strQuantity += "+"
                                else if (cnt < 4) strQuantity += "+" + Common.DoubleToStringFormat(
                                    quantity,
                                    "%.3f"
                                )
                            }
                        }
                    }

                    if (quantityTotal > 0.001) {
                        val cv = ContentValues()
                        cv.put("nomenclature_id", prevNomenclatureId)
                        cv.put("client_id", prevClientId)
                        //cv.put("distr_point_id", prevDistrPointId);
                        cv.put("quantity", quantityTotal)
                        cv.put("strQuantity", strQuantity)

                        //db.insert(SALES_L_TABLE, null, cv);
                        db.insertWithOnConflict(
                            SALES_L_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                } catch (e: SQLException) {
                    Log.e(LOG_TAG, e.toString())
                } finally {
                    db.endTransaction()
                }
                cursor.close()

                // Заполнение salesL2
                // если торговые точки не используются, то везде будет одно и то же значение, и группировка от этого не пострадает
                db.execSQL(
                    "insert into salesL2 select " +
                            "null as _id," +
                            "case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id," +
                            "client_id," +  // 20.09.2019
                            "null as distr_point_id," +  //
                            "datedoc," +
                            "numdoc," +
                            "sum(quantity) as quantity," +
                            "sum(price) as price " +
                            "from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id " +
                            "group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, distr_point_id;"
                )

                return null
            }

            URI_PRICETYPES -> {
                Log.d(LOG_TAG, "URI_PRICETYPES")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    PRICETYPES_TABLE,
                    values,
                    "id=?",
                    arrayOf<String?>(values!!.getAsString("id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    PRICETYPES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(PRICETYPES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_AGREEMENTS -> {
                Log.d(LOG_TAG, "URI_AGREEMENTS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    AGREEMENTS_TABLE,
                    values,
                    "id=?",
                    arrayOf<String?>(values!!.getAsString("id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    AGREEMENTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(AGREEMENTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_AGREEMENTS30 -> {
                Log.d(LOG_TAG, "URI_AGREEMENTS30")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    AGREEMENTS30_TABLE,
                    values,
                    "id=? and owner_id=?",
                    arrayOf<String?>(values!!.getAsString("id"), values.getAsString("owner_id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    AGREEMENTS30_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(AGREEMENTS30_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_STOCKS -> {
                Log.d(LOG_TAG, "URI_STOCKS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(STOCKS_TABLE, values, "id=?", arrayOf<String?>(values!!.getAsString("id")))

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    STOCKS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(STOCKS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_PRICES -> {
                Log.d(LOG_TAG, "URI_PRICES")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    PRICES_TABLE,
                    values,
                    "nomenclature_id=? and price_type_id=?",
                    arrayOf<String?>(
                        values!!.getAsString("nomenclature_id"),
                        values.getAsString("price_type_id")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    PRICES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(PRICES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_PRICES_AGREEMENTS30 -> {
                Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    PRICES_AGREEMENTS30_TABLE,
                    values,
                    "agreement30_id=? and nomenclature_id=?",
                    arrayOf<String?>(
                        values!!.getAsString("agreement30_id"),
                        values.getAsString("nomenclature_id")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    PRICES_AGREEMENTS30_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(PRICES_AGREEMENTS30_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CLIENTS_PRICE -> {
                Log.d(LOG_TAG, "URI_CLIENTS_PRICE")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    CLIENTS_PRICE_TABLE,
                    values,
                    "client_id=? and nomenclature_id=?",
                    arrayOf<String?>(
                        values!!.getAsString("client_id"),
                        values.getAsString("nomenclature_id")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    CLIENTS_PRICE_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(CLIENTS_PRICE_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CURATORS_PRICE -> {
                Log.d(LOG_TAG, "URI_CURATORS_PRICE")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    CURATORS_PRICE_TABLE,
                    values,
                    "curator_id=? and nomenclature_id=?",
                    arrayOf<String?>(
                        values!!.getAsString("curator_id"),
                        values.getAsString("nomenclature_id")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    CURATORS_PRICE_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(CURATORS_PRICE_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_SIMPLE_DISCOUNTS -> {
                Log.d(LOG_TAG, "URI_SIMPLE_DISCOUNTS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    SIMPLE_DISCOUNTS_TABLE,
                    values,
                    "id=?",
                    arrayOf<String?>(values!!.getAsString("id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    SIMPLE_DISCOUNTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(SIMPLE_DISCOUNTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_AGENTS -> {
                Log.d(LOG_TAG, "URI_AGENTS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(AGENTS_TABLE, values, "id=?", arrayOf<String?>(values!!.getAsString("id")))

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    AGENTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(AGENTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CURATORS -> {
                Log.d(LOG_TAG, "URI_CURATORS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    CURATORS_TABLE,
                    values,
                    "id=?",
                    arrayOf<String?>(values!!.getAsString("id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    CURATORS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(CURATORS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_DISTR_POINTS -> {
                Log.d(LOG_TAG, "URI_DISTR_POINTS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    DISTR_POINTS_TABLE,
                    values,
                    "id=? and owner_id=?",
                    arrayOf<String?>(values!!.getAsString("id"), values.getAsString("owner_id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    DISTR_POINTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(DISTR_POINTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_ORGANIZATIONS -> {
                Log.d(LOG_TAG, "URI_ORGANIZATIONS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    ORGANIZATIONS_TABLE,
                    values,
                    "id=?",
                    arrayOf<String?>(values!!.getAsString("id"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    ORGANIZATIONS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(ORGANIZATIONS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_ORDERS_SILENT -> {
                Log.d(LOG_TAG, "URI_ORDERS_SILENT")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                // с 19.02.2018
                db.update(
                    ORDERS_TABLE,
                    values,
                    "uid=? and editing_backup=?",
                    arrayOf<String?>(
                        values!!.getAsString("uid"),
                        values.getAsString("editing_backup")
                    )
                )

                // до
                //db.update(ORDERS_TABLE, values, "uid=?", new String[] {values.getAsString("uid")});

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    ORDERS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID)

                return resultUri
            }

            URI_ORDERS -> {
                Log.d(LOG_TAG, "URI_ORDERS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    ORDERS_TABLE,
                    values,
                    "uid=? and editing_backup=?",
                    arrayOf<String?>(
                        values!!.getAsString("uid"),
                        values.getAsString("editing_backup")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    ORDERS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                var resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri = ContentUris.withAppendedId(ORDERS_JOURNAL_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "order_id=?",
                    arrayOf<String>(rowID.toString()),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
                cursor.close()
                //db.close();
                return resultUri
            }

            URI_ORDERS_LINES -> {
                Log.d(LOG_TAG, "URI_ORDERS_LINES")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(ORDERS_LINES_TABLE, null, values)

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                var resultUri = ContentUris.withAppendedId(ORDERS_LINES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri = ContentUris.withAppendedId(ORDERS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }


            URI_REFUNDS_SILENT -> {
                Log.d(LOG_TAG, "URI_REFUNDS_SILENT")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    REFUNDS_TABLE,
                    values,
                    "uid=?",
                    arrayOf<String?>(values!!.getAsString("uid"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    REFUNDS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID)

                return resultUri
            }

            URI_REFUNDS -> {
                Log.d(LOG_TAG, "URI_REFUNDS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    REFUNDS_TABLE,
                    values,
                    "uid=?",
                    arrayOf<String?>(values!!.getAsString("uid"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    REFUNDS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                val resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "refund_id=?",
                    arrayOf<String>(rowID.toString()),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
                cursor.close()
                //db.close();
                return resultUri
            }

            URI_REFUNDS_LINES -> {
                Log.d(LOG_TAG, "URI_REFUNDS_LINES")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(REFUNDS_LINES_TABLE, null, values)

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                var resultUri = ContentUris.withAppendedId(REFUNDS_LINES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri =
                    ContentUris.withAppendedId(REFUNDS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_DISTRIBS_LINES -> {
                Log.d(LOG_TAG, "URI_DISTRIBS_LINES")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(DISTRIBS_LINES_TABLE, null, values)

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                var resultUri = ContentUris.withAppendedId(DISTRIBS_LINES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri =
                    ContentUris.withAppendedId(DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_CASH_PAYMENTS -> {
                Log.d(LOG_TAG, "URI_CASH_PAYMENTS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    CASH_PAYMENTS_TABLE,
                    values,
                    "uid=?",
                    arrayOf<String?>(values!!.getAsString("uid"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    CASH_PAYMENTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                var resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_JOURNAL_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_VICARIOUS_POWER -> {
                //Log.d(LOG_TAG, "URI_MESSAGES");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(VICARIOUS_POWER_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    VICARIOUS_POWER_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(VICARIOUS_POWER_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_MESSAGES -> {
                //Log.d(LOG_TAG, "URI_MESSAGES");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "uid=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("uid"))

                //Do an update if the constraints match
                db.update(MESSAGES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    MESSAGES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                var resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                resultUri = ContentUris.withAppendedId(MESSAGES_LIST_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_SALES -> {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("drop table if exists sales;")
                // Продажи каждому клиенту
                db.execSQL(
                    "create table sales as " +
                            "select ordersLines.client_id as client_id, nomenclature_id, sum(quantity) as quantity_saled from ordersLines join orders on orders._id=ordersLines.order_id and datedoc>=? group by ordersLines.client_id, nomenclature_id" +
                            ";", arrayOf<String?>(values!!.getAsString("datebegin"))
                )
                // Общие продажи (клиент=null)
                db.execSQL(
                    "insert into sales " +
                            "select null as client_id, nomenclature_id, sum(quantity_saled) as quantity_saled from sales group by nomenclature_id" +
                            ";"
                )

                /*
        if (values.containsKey("datebegin"))
        {

        }
        */
                // insert into xxx
                // select * from yyyy;
                return null
            }

            URI_RESTS_SALES_STUFF -> {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("DROP TABLE IF EXISTS rests_sales_stuff;")

                //db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature");
                //db.execSQL("INSERT INTO RestsSalesStuff select * from temp_Nomenclature");
                db.execSQL(
                    "CREATE TABLE rests_sales_stuff AS " +
                            "SELECT nomenclature_id, sum(quantity) as quantity, sum(quantity_reserve) as quantity_reserve, sum(saled) as saledNow " +
                            "FROM ( " +
                            "SELECT nomenclature_id, quantity, quantity_reserve, 0 as saled " +
                            "FROM rests " + (if (values!!.containsKey("stock_id")) "where stock_id=\"" + values.getAsString(
                        "stock_id"
                    ) + "\" " else "") +  // У тандема разделение остатков по организациям
                            // проверяется, есть ли stock_id, чтобы написать where или and
                            (if (values.containsKey("organization_id") && !values.containsKey("stock_id")) " where organization_id=\"" + values.getAsString(
                                "organization_id"
                            ) + "\" " else "") +
                            (if (values.containsKey("organization_id") && values.containsKey("stock_id")) " and organization_id=\"" + values.getAsString(
                                "organization_id"
                            ) + "\" " else "") +  //
                            (if (values.containsKey("client_id")) ("UNION ALL " +
                                    "SELECT nomenclature_id, 0, 0, quantity as saled " +
                                    "FROM orders join ordersLines on ordersLines.order_id=orders._id " +
                                    "WHERE orders.client_id=\"" + values.getAsString("client_id") + "\" " +
                                    (if (values.containsKey("distr_point_id")) " and distr_point_id=\"" + values.getAsString(
                                        "distr_point_id"
                                    ) + "\" " else "") +  // TODO остальные состояния
                                    "and orders.state in (" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + ") ") else "") +  //
                            ") in1 " +
                            "GROUP BY nomenclature_id"
                )

                db.execSQL("CREATE INDEX sales_nomenclature_id ON rests_sales_stuff (nomenclature_id ASC);")

                return null
            }

            URI_DISCOUNTS_STUFF_MEGA, URI_DISCOUNTS_STUFF_SIMPLE, URI_DISCOUNTS_STUFF_OTHER -> {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("DROP TABLE IF EXISTS discounts_stuff;")
                db.execSQL("DROP TABLE IF EXISTS temp_discounts_stuff;")

                //"_id integer primary key autoincrement, " +
                //"client_id text," +
                //"nomenclature_id text," +
                //"priceProcent double," +
                //"priceAdd double," +

                // добавлено 26.02.2018
                var bFastMode = false

                //
                if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_MEGA) {
                    db.execSQL(
                        "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +  // добавляем группы
                                "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join curators_price p on " + (if (values!!.containsKey(
                                "curator_id"
                            )
                        ) "curator_id=\"" + values.getAsString("curator_id") + "\" " else "0=1 ") +
                                "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +  // и номенклатуру отдельно, уровень ставим 100
                                "union all " +
                                "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join curators_price p on " + (if (values.containsKey(
                                "curator_id"
                            )
                        ) "curator_id=\"" + values.getAsString("curator_id") + "\" " else "0=1 ") +
                                "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                                ";"
                    )
                } else  //if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART)
                    if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_SIMPLE) {
                        // Тут простейший (обычный для 1С) вариант, скидка от номенклатуры не зависит
                        // а только от договора, но номенклатуру все равно выберем
                        // будет в итоге при работе соединение с той же самой таблицей
                        // группы нам здесь не нужны, только элементы
                        db.execSQL(
                            "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +
                                    "select _id p_id, id nomenclature_id, 100 level, " + values!!.getAsString(
                                "priceProcent"
                            ) + " priceProcent, 0 priceAdd " +
                                    "from nomenclature where isFolder<>1 " +
                                    ";"
                        )

                        bFastMode = true
                    } else {
                        db.execSQL(
                            "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +  // добавляем группы
                                    "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                                    "from nomenclature_hierarchy h join clients_price p on " + (if (values!!.containsKey(
                                    "client_id"
                                )
                            ) "client_id=\"" + values!!.getAsString("client_id") + "\" " else "0=1 ") +
                                    "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +  // и номенклатуру отдельно, уровень ставим 100
                                    "union all " +
                                    "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                                    "from nomenclature_hierarchy h join clients_price p on " + (if (values.containsKey(
                                    "client_id"
                                )
                            ) "client_id=\"" + values.getAsString("client_id") + "\" " else "0=1 ") +
                                    "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                                    ";"
                        )
                    }

                if (bFastMode) {
                    // Просто копируем эту таблицу
                    db.execSQL(
                        "CREATE TABLE discounts_stuff AS " +
                                "select nomenclature_id, p_id, level, priceProcent, priceAdd from temp_discounts_stuff;"
                    )
                } else {
                    // заполним level максимальным совпавшим значением для номенклатуры, id - чтобы появилась колонка
                    db.execSQL(
                        "CREATE TABLE discounts_stuff AS " +
                                "select nomenclature_id, max(p_id) p_id, max(level) level, 0.0 as priceProcent, 0.0 as priceAdd from temp_discounts_stuff group by nomenclature_id;"
                    )
                    // находим _id из clients_prict
                    db.execSQL("UPDATE discounts_stuff set p_id = (select p_id from temp_discounts_stuff t2 where t2.nomenclature_id=discounts_stuff.nomenclature_id and t2.level=discounts_stuff.level);")
                    // и все остальное
                    db.execSQL(
                        "UPDATE discounts_stuff set " +
                                "priceProcent = (select priceProcent from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id)," +
                                "priceAdd = (select priceAdd from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id);"
                    )
                }

                db.execSQL("CREATE INDEX discounts_nomenclature_id ON discounts_stuff (nomenclature_id ASC);")

                return null
            }

            URI_PRICESV_MEGA, URI_PRICESV_OTHER -> {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("DROP VIEW IF EXISTS PricesV;")
                //if (Common.MEGA)
                if (sUriMatcher.match(uri) == URI_PRICESV_MEGA) {
                    var priceName = "rozn_price"
                    val price_type_id = values!!.getAsString("price_type_id")
                    if (price_type_id == E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value().toString()) {
                        priceName = "m_opt_price"
                    } else if (price_type_id == E_PRICE_TYPE.E_PRICE_TYPE_OPT.value().toString()) {
                        priceName = "opt_price"
                    }
                    db.execSQL(
                        "CREATE VIEW PricesV as select " +
                                "id as nomenclature_id," +
                                "edizm_1_id as ed_izm_id," +
                                "quant_1 as edIzm," +
                                priceName + " as price," +
                                "0 as priceProcent," +
                                "quant_k_1 as k " +
                                "from nomenclature " +
                                ";"
                    )
                } else  /*
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
                    if (values!!.containsKey("agreement30_id")) {
                        // это Common.FACTORY
                        db.execSQL(
                            "CREATE VIEW PricesV as select " +
                                    "ifnull(pa30.nomenclature_id, prices.nomenclature_id) nomenclature_id," +
                                    "ifnull(pa30.ed_izm_id, prices.ed_izm_id) ed_izm_id," +
                                    "ifnull(pa30.edIzm, prices.edIzm) edIzm," +
                                    "ifnull(pa30.price, prices.price) price," +
                                    "priceProcent," +  // на самом деле это лишнее поле, оно не используется
                                    "ifnull(pa30.k, prices.k) k " +
                                    "from prices" +  // вообще тут нужен FULL JOIN, но он не поддерживается
                                    " left join prices_agreements30 pa30 on pa30.agreement30_id=\"" + values.getAsString(
                                "agreement30_id"
                            ) + "\"" +
                                    " and pa30.nomenclature_id=prices.nomenclature_id" +
                                    " where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                    ";"
                        )
                    } else {
                        db.execSQL(
                            "CREATE VIEW PricesV as select " +
                                    "nomenclature_id," +
                                    "ed_izm_id," +
                                    "edIzm," +
                                    "price," +
                                    "priceProcent," +
                                    "k " +
                                    "from prices where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                    ";"
                        )
                    }
                return null
            }

            URI_SETTINGS -> {
                Log.d(LOG_TAG, "URI_SETTINGS")
                val db = dbHelper!!.getWritableDatabase()

                val cnt = db.update(SETTINGS_TABLE, values, null, null)
                if (cnt <= 0) {
                    db.insert(SETTINGS_TABLE, null, values)
                }
                return null
            }

            URI_SALES_LOADED -> {
                Log.d(LOG_TAG, "URI_SALES_LOADED")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    SALES_LOADED_TABLE,
                    values,
                    "nomenclature_id=? and refdoc=?",
                    arrayOf<String?>(
                        values!!.getAsString("nomenclature_id"),
                        values.getAsString("refdoc")
                    )
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    SALES_LOADED_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(SALES_LOADED_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_NOMENCLATURE_HIERARCHY -> {
                Log.d(LOG_TAG, "NOMENCLATURE_HIERARCHY")
                val db = dbHelper!!.getWritableDatabase()
                val rowID = db.insertWithOnConflict(
                    NOMENCLATURE_HIERARCHY_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
                val resultUri =
                    ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                return resultUri
            }

            URI_GPS_COORD -> {
                Log.d(LOG_TAG, "URI_GPS_COORD")
                val db = dbHelper!!.getWritableDatabase()

                // вообще можно просто добавлять, дублирования кода не будет
                //Do an update if the constraints match
                db.update(
                    GPS_COORD_TABLE,
                    values,
                    "datecoord=?",
                    arrayOf<String?>(values!!.getAsString("datecoord"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    GPS_COORD_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                return null
            }

            URI_DISTRIBS_CONTRACTS -> {
                //Log.d(LOG_TAG, "URI_DISTRIBS_CONTRACTS");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(DISTRIBS_CONTRACTS_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    DISTRIBS_CONTRACTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(DISTRIBS_CONTRACTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_DISTRIBS -> {
                Log.d(LOG_TAG, "URI_DISTRIBS")
                val db = dbHelper!!.getWritableDatabase()

                //Do an update if the constraints match
                db.update(
                    DISTRIBS_TABLE,
                    values,
                    "uid=?",
                    arrayOf<String?>(values!!.getAsString("uid"))
                )

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    DISTRIBS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                val resultUri = ContentUris.withAppendedId(DISTRIBS_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                // получим _id журнала
                var journal_id: Long = 0
                val cursor = db.query(
                    JOURNAL_TABLE,
                    arrayOf<String>("journal._id"),
                    "distribs_id=?",
                    arrayOf<String>(rowID.toString()),
                    null,
                    null,
                    null
                )
                if (cursor.moveToNext()) {
                    journal_id = cursor.getLong(0)
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
                cursor.close()
                //db.close();
                return resultUri
            }

            URI_EQUIPMENT -> {
                //Log.d(LOG_TAG, "URI_EQUIPMENT");
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(EQUIPMENT_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    EQUIPMENT_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(EQUIPMENT_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_EQUIPMENT_RESTS -> {
                Log.d(LOG_TAG, "URI_EQUIPMENT_RESTS")
                val db = dbHelper!!.getWritableDatabase()

                val selection =
                    "client_id=? and agreement_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?"
                val selectionArgs = arrayOf<String?>(
                    values!!.getAsString("client_id"),
                    values.getAsString("agreement_id"),
                    values.getAsString("nomenclature_id"),
                    values.getAsString("distr_point_id"),
                    values.getAsString("doc_id")
                )

                //Do an update if the constraints match
                db.update(EQUIPMENT_RESTS_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    EQUIPMENT_RESTS_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(EQUIPMENT_RESTS_CONTENT_URI, rowID)
                // уведомляем ContentResolver, что данные по адресу resultUri изменились
                getContext()!!.getContentResolver().notifyChange(resultUri, null)
                //db.close();
                return resultUri
            }

            URI_MTRADE_LOG -> {
                Log.d(LOG_TAG, "URI_MTRADE_LOG")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(MTRADE_LOG_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(MTRADE_LOG_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_PERMISSIONS_REQUESTS -> {
                Log.d(LOG_TAG, "URI_PERMISSIONS_REQUESTS")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(PERMISSIONS_REQUESTS_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(PERMISSIONS_REQUESTS_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_ROUTES -> {
                Log.d(LOG_TAG, "URI_ROUTES")
                val db = dbHelper!!.getWritableDatabase()

                val selection = "id=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

                //Do an update if the constraints match
                db.update(ROUTES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    ROUTES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(ROUTES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_ROUTES_LINES -> {
                Log.d(LOG_TAG, "URI_ROUTES_LINES")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(ROUTES_LINES_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(ROUTES_LINES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_ROUTES_DATES -> {
                Log.d(LOG_TAG, "URI_ROUTES_DATES")
                val db = dbHelper!!.getWritableDatabase()

                val selection = "route_date=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("route_date"))

                //Do an update if the constraints match
                db.update(ROUTES_DATES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    ROUTES_DATES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(ROUTES_DATES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_REAL_ROUTES_DATES -> {
                Log.d(LOG_TAG, "URI_ROUTES_DATES")
                val db = dbHelper!!.getWritableDatabase()

                val selection = "route_date=?"
                val selectionArgs = arrayOf<String?>(values!!.getAsString("route_date"))

                //Do an update if the constraints match
                db.update(REAL_ROUTES_DATES_TABLE, values, selection, selectionArgs)

                //This will return the id of the newly inserted row if no conflict
                //It will also return the offending row without modifying it if in conflict
                val rowID = db.insertWithOnConflict(
                    REAL_ROUTES_DATES_TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )

                val resultUri = ContentUris.withAppendedId(REAL_ROUTES_DATES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            URI_REAL_ROUTES_LINES -> {
                Log.d(LOG_TAG, "URI_ROUTES_LINES")
                val db = dbHelper!!.getWritableDatabase()

                val rowID = db.insert(REAL_ROUTES_LINES_TABLE, null, values)
                val resultUri = ContentUris.withAppendedId(REAL_ROUTES_LINES_CONTENT_URI, rowID)
                getContext()!!.getContentResolver().notifyChange(resultUri, null)

                return resultUri
            }

            else -> throw IllegalArgumentException("Unknown URI (for insert) " + uri)
        }
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        var numInserted = 0

        when (sUriMatcher.match(uri)) {
            URI_PRICES -> {
                Log.d(LOG_TAG, "URI_PRICES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            PRICES_TABLE,
                            cv,
                            "nomenclature_id=? and price_type_id=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("price_type_id")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            PRICES_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_PRICES_AGREEMENTS30 -> {
                Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            PRICES_AGREEMENTS30_TABLE,
                            cv,
                            "agreement30_id=? and nomenclature_id=?",
                            arrayOf<String?>(
                                cv.getAsString("agreement30_id"),
                                cv.getAsString("nomenclature_id")
                            )
                        )

                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            PRICES_AGREEMENTS30_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )

                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_SALDO -> {
                Log.d(LOG_TAG, "URI_SALDO_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            SALDO_TABLE,
                            cv,
                            "client_id=?",
                            arrayOf<String?>(cv.getAsString("client_id"))
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            SALDO_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_SALDO_EXTENDED -> {
                Log.d(LOG_TAG, "URI_SALDO_EXTENDED_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            SALDO_EXTENDED_TABLE,
                            cv,
                            "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?",
                            arrayOf<String?>(
                                cv.getAsString("client_id"),
                                cv.getAsString("agreement30_id"),
                                cv.getAsString("agreement_id"),
                                cv.getAsString("document_id"),
                                cv.getAsString("manager_id")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            SALDO_EXTENDED_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_AGREEMENTS -> {
                Log.d(LOG_TAG, "URI_AGREEMENTS_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            AGREEMENTS_TABLE,
                            cv,
                            "id=?",
                            arrayOf<String?>(cv.getAsString("id"))
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            AGREEMENTS_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_AGREEMENTS30 -> {
                Log.d(LOG_TAG, "URI_AGREEMENTS30_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            AGREEMENTS30_TABLE,
                            cv,
                            "id=? and owner_id=?",
                            arrayOf<String?>(cv.getAsString("id"), cv.getAsString("owner_id"))
                        )

                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            AGREEMENTS30_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )

                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_RESTS -> {
                Log.d(LOG_TAG, "URI_RESTS_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        // Тандем
                        if (cv.containsKey("organization_id")) db.update(
                            RESTS_TABLE,
                            cv,
                            "nomenclature_id=? and stock_id=? and organization_id=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("stock_id"),
                                cv.getAsString("organization_id")
                            )
                        )
                        else db.update(
                            RESTS_TABLE,
                            cv,
                            "nomenclature_id=? and stock_id=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("stock_id")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            RESTS_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }


            URI_ROUTES -> {
                Log.d(LOG_TAG, "URI_ROUTES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        db.update(ROUTES_TABLE, cv, "id=?", arrayOf<String?>(cv.getAsString("id")))
                        val rowID = db.insertWithOnConflict(
                            ROUTES_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )
                    }
                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                return numInserted
            }

            URI_ROUTES_LINES -> {
                Log.d(LOG_TAG, "URI_ROUTES_LINES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        db.update(
                            ROUTES_LINES_TABLE,
                            cv,
                            "route_id=? and lineno=?",
                            arrayOf<String?>(cv.getAsString("route_id"), cv.getAsString("lineno"))
                        )
                        val rowID = db.insertWithOnConflict(
                            ROUTES_LINES_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )
                    }
                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                return numInserted
            }

            URI_SALES_LOADED -> {
                Log.d(LOG_TAG, "URI_SALES_LOADED_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            SALES_LOADED_TABLE,
                            cv,
                            "nomenclature_id=? and refdoc=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("refdoc")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            SALES_LOADED_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_NOMENCLATURE -> {
                Log.d(LOG_TAG, "URI_NOMENCLATURE_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()


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
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            NOMENCLATURE_TABLE,
                            cv,
                            "id=?",
                            arrayOf<String?>(cv.getAsString("id"))
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            NOMENCLATURE_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


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
                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_NOMENCLATURE_HIERARCHY -> {
                Log.d(LOG_TAG, "URI_NOMENCLATURE_HIERARCHY_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()

                //long rowID = db.insertWithOnConflict(NOMENCLATURE_HIERARCHY_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                //Uri resultUri = ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID);
                //getContext().getContentResolver().notifyChange(resultUri, null);
                //return resultUri;
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        val rowID = db.insertWithOnConflict(
                            NOMENCLATURE_HIERARCHY_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )
                    }
                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_CLIENTS -> {
                Log.d(LOG_TAG, "URI_CLIENTS_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()


                // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(CLIENTS_TABLE, cv, "id=?", arrayOf<String?>(cv.getAsString("id")))


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            CLIENTS_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_DISTR_POINTS -> {
                Log.d(LOG_TAG, "URI_DISTR_POINTS_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()


                // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            DISTR_POINTS_TABLE,
                            cv,
                            "id=? and owner_id=?",
                            arrayOf<String?>(cv.getAsString("id"), cv.getAsString("owner_id"))
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            DISTR_POINTS_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_CLIENTS_PRICE -> {
                Log.d(LOG_TAG, "URI_CLIENTS_PRICE_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()


                // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            CLIENTS_PRICE_TABLE,
                            cv,
                            "nomenclature_id=? and client_id=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("client_id")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            CLIENTS_PRICE_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_CURATORS_PRICE -> {
                Log.d(LOG_TAG, "URI_CURATORS_PRICE_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()


                // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        //long newID = sqlDB.insertOrThrow(table, null, cv);
                        //Do an update if the constraints match
                        db.update(
                            CURATORS_PRICE_TABLE,
                            cv,
                            "nomenclature_id=? and curator_id=?",
                            arrayOf<String?>(
                                cv.getAsString("nomenclature_id"),
                                cv.getAsString("curator_id")
                            )
                        )


                        //This will return the id of the newly inserted row if no conflict
                        //It will also return the offending row without modifying it if in conflict
                        val rowID = db.insertWithOnConflict(
                            CURATORS_PRICE_TABLE,
                            null,
                            cv,
                            SQLiteDatabase.CONFLICT_IGNORE
                        )


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_ORDERS_LINES -> {
                Log.d(LOG_TAG, "URI_ORDERS_LINES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        val rowID = db.insert(ORDERS_LINES_TABLE, null, cv)


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_REFUNDS_LINES -> {
                Log.d(LOG_TAG, "URI_REFUNDS_LINES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        val rowID = db.insert(REFUNDS_LINES_TABLE, null, cv)


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            URI_ORDERS_PLACES -> {
                Log.d(LOG_TAG, "URI_ORDERS_PLACES_BULK_INSERT")
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("PRAGMA synchronous=NORMAL")
                db.beginTransaction()
                try {
                    for (cv in values) {
                        val rowID = db.insert(ORDERS_PLACES_TABLE, null, cv)


                        //if (rowID <= 0) {
                        //    throw new SQLException("Failed to insert row into " + uri);
                        //}
                    }

                    db.setTransactionSuccessful()
                    getContext()!!.getContentResolver().notifyChange(uri, null)
                    numInserted = values.size
                } finally {
                    db.endTransaction()
                }
                //db.close();
                return numInserted
            }

            else -> return super.bulkInsert(uri, values)
        }
    }

    override fun onCreate(): Boolean {
        dbHelper = MTradeDBHelper(getContext())

        return true
    }

    override fun query(
        uri: Uri, projection: Array<String?>?, selection: String?, selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor {
        var selection = selection
        val qb = SQLiteQueryBuilder()


        //String groupBy=null;
        var bAppendIdToSelection = false
        var appendIdPreffix = ""

        when (sUriMatcher.match(uri)) {
            URI_ORDERS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(ORDERS_TABLE)
                qb.setProjectionMap(ordersProjectionMap)
            }

            URI_ORDERS -> {
                qb.setTables(ORDERS_TABLE)
                qb.setProjectionMap(ordersProjectionMap)
            }

            URI_CASH_PAYMENTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(CASH_PAYMENTS_TABLE)
                qb.setProjectionMap(cashPaymentsProjectionMap)
            }

            URI_CASH_PAYMENTS -> {
                qb.setTables(CASH_PAYMENTS_TABLE)
                qb.setProjectionMap(cashPaymentsProjectionMap)
            }

            URI_REFUNDS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(REFUNDS_TABLE)
                qb.setProjectionMap(refundsProjectionMap)
            }

            URI_REFUNDS -> {
                qb.setTables(REFUNDS_TABLE)
                qb.setProjectionMap(refundsProjectionMap)
            }

            URI_JOURNAL_ID -> {
                bAppendIdToSelection = true
                appendIdPreffix = "journal."
                qb.setTables(JOURNAL_TABLE)
                qb.setProjectionMap(journalProjectionMap)
            }

            URI_JOURNAL -> {
                qb.setTables(JOURNAL_TABLE)
                qb.setProjectionMap(journalProjectionMap)
            }

            URI_CLIENTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(CLIENTS_TABLE)
                qb.setProjectionMap(clientsProjectionMap)
            }

            URI_CLIENTS -> {
                qb.setTables(CLIENTS_TABLE)
                qb.setProjectionMap(clientsProjectionMap)
            }

            URI_NOMENCLATURE_ID -> {
                bAppendIdToSelection = true
                qb.setTables(NOMENCLATURE_TABLE)
                qb.setProjectionMap(nomenclatureProjectionMap)
            }

            URI_NOMENCLATURE -> {
                qb.setTables(NOMENCLATURE_TABLE)
                qb.setProjectionMap(nomenclatureProjectionMap)
            }

            URI_NOMENCLATURE_HIERARCHY -> {
                qb.setTables(NOMENCLATURE_HIERARCHY_TABLE)
                qb.setProjectionMap(nomenclatureHierarchyProjectionMap)
            }

            URI_NOMENCLATURE_LIST -> {
                //if (Common.MEGA)
                //{
                //	qb.setTables(NOMENCLATURE_LIST_TABLE_MEGA);
                //    qb.setProjectionMap(nomenclatureListProjectionMapMega);
                //} else
                //{
                qb.setTables(NOMENCLATURE_LIST_TABLE)
                qb.setProjectionMap(nomenclatureListProjectionMap)
            }

            URI_NOMENCLATURE_SURFING -> {
                qb.setTables(NOMENCLATURE_SURFING_TABLE)
                qb.setProjectionMap(nomenclatureSurfingProjectionMap)
            }

            URI_RESTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(RESTS_TABLE)
                qb.setProjectionMap(restsProjectionMap)
            }

            URI_RESTS -> {
                qb.setTables(RESTS_TABLE)
                qb.setProjectionMap(restsProjectionMap)
            }

            URI_AGREEMENTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(AGREEMENTS_TABLE)
                qb.setProjectionMap(agreementsProjectionMap)
            }

            URI_AGREEMENTS -> {
                qb.setTables(AGREEMENTS_TABLE)
                qb.setProjectionMap(agreementsProjectionMap)
            }

            URI_AGREEMENTS30_ID -> {
                bAppendIdToSelection = true
                qb.setTables(AGREEMENTS30_TABLE)
                qb.setProjectionMap(agreements30ProjectionMap)
            }

            URI_AGREEMENTS30 -> {
                qb.setTables(AGREEMENTS30_TABLE)
                qb.setProjectionMap(agreements30ProjectionMap)
            }

            URI_VERSIONS -> {
                qb.setTables(VERSIONS_TABLE)
                qb.setProjectionMap(versionsProjectionMap)
            }

            URI_VERSIONS_SALES -> {
                qb.setTables(VERSIONS_SALES_TABLE)
                qb.setProjectionMap(versionsSalesProjectionMap)
            }

            URI_SEANCES -> {
                qb.setTables(SEANCES_TABLE)
                qb.setProjectionMap(seancesProjectionMap)
            }

            URI_PRICETYPES -> {
                qb.setTables(PRICETYPES_TABLE)
                qb.setProjectionMap(pricetypesProjectionMap)
            }

            URI_SALDO -> {
                qb.setTables(SALDO_TABLE)
                qb.setProjectionMap(saldoProjectionMap)
            }

            URI_SALDO_EXTENDED_ID -> {
                bAppendIdToSelection = true
                qb.setTables(SALDO_EXTENDED_TABLE)
                qb.setProjectionMap(saldoExtendedProjectionMap)
            }

            URI_SALDO_EXTENDED -> {
                qb.setTables(SALDO_EXTENDED_TABLE)
                qb.setProjectionMap(saldoExtendedProjectionMap)
            }

            URI_SALDO_EXTENDED_JOURNAL -> {
                qb.setTables(SALDO_EXTENDED_JOURNAL_TABLE)
                qb.setProjectionMap(saldoExtendedJournalProjectionMap)
            }

            URI_STOCKS -> {
                qb.setTables(STOCKS_TABLE)
                qb.setProjectionMap(stocksProjectionMap)
            }

            URI_PRICES -> {
                qb.setTables(PRICES_TABLE)
                qb.setProjectionMap(pricesProjectionMap)
            }

            URI_PRICES_AGREEMENTS30 -> {
                qb.setTables(PRICES_AGREEMENTS30_TABLE)
                qb.setProjectionMap(pricesAgreements30ProjectionMap)
            }

            URI_CLIENTS_PRICE -> {
                qb.setTables(CLIENTS_PRICE_TABLE)
                qb.setProjectionMap(clientsPriceProjectionMap)
            }

            URI_CURATORS_PRICE -> {
                qb.setTables(CURATORS_PRICE_TABLE)
                qb.setProjectionMap(curatorsPriceProjectionMap)
            }

            URI_SIMPLE_DISCOUNTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(SIMPLE_DISCOUNTS_TABLE)
                qb.setProjectionMap(simpleDiscountsProjectionMap)
            }

            URI_SIMPLE_DISCOUNTS -> {
                qb.setTables(SIMPLE_DISCOUNTS_TABLE)
                qb.setProjectionMap(simpleDiscountsProjectionMap)
            }

            URI_AGENTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(AGENTS_TABLE)
                qb.setProjectionMap(agentsProjectionMap)
            }

            URI_AGENTS -> {
                qb.setTables(AGENTS_TABLE)
                qb.setProjectionMap(agentsProjectionMap)
            }

            URI_CURATORS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(CURATORS_TABLE)
                qb.setProjectionMap(curatorsProjectionMap)
            }

            URI_CURATORS -> {
                qb.setTables(CURATORS_TABLE)
                qb.setProjectionMap(curatorsProjectionMap)
            }

            URI_CURATORS_LIST -> {
                qb.setTables(CURATORS_LIST_TABLE)
                qb.setProjectionMap(curatorsListProjectionMap)
            }

            URI_DISTR_POINTS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(DISTR_POINTS_TABLE)
                qb.setProjectionMap(distrPointsProjectionMap)
            }

            URI_DISTR_POINTS -> {
                qb.setTables(DISTR_POINTS_TABLE)
                qb.setProjectionMap(distrPointsProjectionMap)
            }

            URI_DISTR_POINTS_LIST -> {
                qb.setTables(DISTR_POINTS_LIST_TABLE)
                qb.setProjectionMap(distrPointsListProjectionMap)
            }

            URI_ORGANIZATIONS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(ORGANIZATIONS_TABLE)
                qb.setProjectionMap(organizationsProjectionMap)
            }

            URI_ORGANIZATIONS -> {
                qb.setTables(ORGANIZATIONS_TABLE)
                qb.setProjectionMap(organizationsProjectionMap)
            }

            URI_CLIENTS_WITH_SALDO -> {
                qb.setTables(CLIENTS_WITH_SALDO_TABLE)
                qb.setProjectionMap(clientsWithSaldoProjectionMap)
            }

            URI_ORDERS_LINES -> {
                qb.setTables(ORDERS_LINES_TABLE)
                qb.setProjectionMap(ordersLinesProjectionMap)
            }

            URI_REFUNDS_LINES -> {
                qb.setTables(REFUNDS_LINES_TABLE)
                qb.setProjectionMap(refundsLinesProjectionMap)
            }

            URI_ORDERS_JOURNAL -> {
                qb.setTables(ORDERS_JOURNAL_TABLE)
                qb.setProjectionMap(ordersJournalProjectionMap)
            }

            URI_CASH_PAYMENTS_JOURNAL -> {
                qb.setTables(CASH_PAYMENTS_JOURNAL_TABLE)
                qb.setProjectionMap(cashPaymentsJournalProjectionMap)
            }

            URI_AGREEMENTS_LIST -> {
                qb.setTables(AGREEMENTS_LIST_TABLE)
                qb.setProjectionMap(agreementsListProjectionMap)
            }

            URI_AGREEMENTS30_LIST -> {
                qb.setTables(AGREEMENTS30_LIST_TABLE)
                qb.setProjectionMap(agreements30ListProjectionMap)
            }

            URI_AGREEMENTS_WITH_SALDO_ONLY_LIST -> {
                qb.setTables(AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE)
                qb.setProjectionMap(agreementsListWithSaldoOnlyProjectionMap)
            }

            URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST -> {
                qb.setTables(AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE)
                qb.setProjectionMap(agreements30ListWithSaldoOnlyProjectionMap)
            }

            URI_ORDERS_LINES_COMPLEMENTED -> {
                qb.setTables(ORDERS_LINES_COMPLEMENTED_TABLE)
                qb.setProjectionMap(ordersLinesComplementedProjectionMap)
            }

            URI_REFUNDS_LINES_COMPLEMENTED -> {
                qb.setTables(REFUNDS_LINES_COMPLEMENTED_TABLE)
                qb.setProjectionMap(refundsLinesComplementedProjectionMap)
            }

            URI_VICARIOUS_POWER_ID -> {
                bAppendIdToSelection = true
                qb.setTables(VICARIOUS_POWER_TABLE)
                qb.setProjectionMap(vicariousPowerProjectionMap)
            }

            URI_VICARIOUS_POWER -> {
                qb.setTables(VICARIOUS_POWER_TABLE)
                qb.setProjectionMap(vicariousPowerProjectionMap)
            }

            URI_MESSAGES -> {
                qb.setTables(MESSAGES_TABLE)
                qb.setProjectionMap(messagesProjectionMap)
            }

            URI_MESSAGES_LIST -> {
                qb.setTables(MESSAGES_LIST_TABLE)
                qb.setProjectionMap(messagesListProjectionMap)
            }

            URI_SETTINGS -> {
                qb.setTables(SETTINGS_TABLE)
                qb.setProjectionMap(settingsProjectionMap)
            }

            URI_SALES_LOADED -> {
                qb.setTables(SALES_LOADED_TABLE)
                qb.setProjectionMap(salesLoadedProjectionMap)
            }

            URI_SALES_L2 -> {
                qb.setTables(SALES_L2_TABLE)
                qb.setProjectionMap(salesL2ProjectionMap)
            }

            URI_PLACES -> {
                qb.setTables(PLACES_TABLE)
                qb.setProjectionMap(placesProjectionMap)
            }

            URI_OCCUPIED_PLACES -> {
                qb.setTables(OCCUPIED_PLACES_TABLE)
                qb.setProjectionMap(occupiedPlacesProjectionMap)
            }

            URI_ORDERS_PLACES -> {
                qb.setTables(ORDERS_PLACES_TABLE)
                qb.setProjectionMap(ordersPlacesProjectionMap)
            }

            URI_ORDERS_PLACES_LIST -> {
                qb.setTables(ORDERS_PLACES_LIST_TABLE)
                qb.setProjectionMap(ordersPlacesListProjectionMap)
            }

            URI_GPS_COORD -> {
                qb.setTables(GPS_COORD_TABLE)
                qb.setProjectionMap(gpsCoordProjectionMap)
            }

            URI_DISTRIBS_CONTRACTS -> {
                qb.setTables(DISTRIBS_CONTRACTS_TABLE)
                qb.setProjectionMap(distribsContractsProjectionMap)
            }

            URI_DISTRIBS_CONTRACTS_LIST -> {
                qb.setTables(DISTRIBS_CONTRACTS_LIST_TABLE)
                qb.setProjectionMap(distribsContractsListProjectionMap)
            }

            URI_DISTRIBS_LINES -> {
                qb.setTables(DISTRIBS_LINES_TABLE)
                qb.setProjectionMap(distribsLinesProjectionMap)
            }

            URI_DISTRIBS_LINES_COMPLEMENTED -> {
                qb.setTables(DISTRIBS_LINES_COMPLEMENTED_TABLE)
                qb.setProjectionMap(distribsLinesComplementedProjectionMap)
            }

            URI_DISTRIBS_ID -> {
                bAppendIdToSelection = true
                qb.setTables(DISTRIBS_TABLE)
                qb.setProjectionMap(distribsProjectionMap)
            }

            URI_DISTRIBS -> {
                qb.setTables(DISTRIBS_TABLE)
                qb.setProjectionMap(distribsProjectionMap)
            }

            URI_EQUIPMENT -> {
                qb.setTables(EQUIPMENT_TABLE)
                qb.setProjectionMap(equipmentProjectionMap)
            }

            URI_EQUIPMENT_RESTS -> {
                qb.setTables(EQUIPMENT_RESTS_TABLE)
                qb.setProjectionMap(equipmentRestsProjectionMap)
            }

            URI_EQUIPMENT_RESTS_LIST -> {
                qb.setTables(EQUIPMENT_RESTS_LIST_TABLE)
                qb.setProjectionMap(equipmentRestsListProjectionMap)
            }

            URI_MTRADE_LOG -> {
                qb.setTables(MTRADE_LOG_TABLE)
                qb.setProjectionMap(mtradeLogProjectionMap)
            }

            URI_PERMISSIONS_REQUESTS -> {
                qb.setTables(PERMISSIONS_REQUESTS_TABLE)
                qb.setProjectionMap(permissionsRequestsProjectionMap)
            }

            URI_ROUTES -> {
                qb.setTables(ROUTES_TABLE)
                qb.setProjectionMap(routesProjectionMap)
            }

            URI_ROUTES_LINES -> {
                qb.setTables(ROUTES_LINES_TABLE)
                qb.setProjectionMap(routesLinesProjectionMap)
            }

            URI_ROUTES_DATES_ID -> {
                bAppendIdToSelection = true
                qb.setTables(ROUTES_DATES_TABLE)
                qb.setProjectionMap(routesDatesProjectionMap)
            }

            URI_ROUTES_DATES -> {
                qb.setTables(ROUTES_DATES_TABLE)
                qb.setProjectionMap(routesDatesProjectionMap)
            }

            URI_ROUTES_DATES_LIST_ID -> {
                bAppendIdToSelection = true
                appendIdPreffix = "routes_dates."
                qb.setTables(ROUTES_DATES_LIST_TABLE)
                qb.setProjectionMap(routesDatesListProjectionMap)
            }

            URI_ROUTES_DATES_LIST -> {
                qb.setTables(ROUTES_DATES_LIST_TABLE)
                qb.setProjectionMap(routesDatesListProjectionMap)
            }

            URI_REAL_ROUTES_DATES_ID -> {
                bAppendIdToSelection = true
                qb.setTables(REAL_ROUTES_DATES_TABLE)
                qb.setProjectionMap(realRoutesDatesProjectionMap)
            }

            URI_REAL_ROUTES_DATES -> {
                qb.setTables(REAL_ROUTES_DATES_TABLE)
                qb.setProjectionMap(realRoutesDatesProjectionMap)
            }

            URI_REAL_ROUTES_LINES -> {
                qb.setTables(REAL_ROUTES_LINES_TABLE)
                qb.setProjectionMap(realRoutesLinesProjectionMap)
            }

            else -> throw IllegalArgumentException("Unknown URI (for select) " + uri)
        }

        if (bAppendIdToSelection) {
            if (selection == null || selection.isEmpty()) selection =
                appendIdPreffix + "_id = " + uri.getLastPathSegment()
            else selection =
                selection + " and " + appendIdPreffix + "_id = " + uri.getLastPathSegment()
        }

        val db = dbHelper!!.getReadableDatabase()

        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        //ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, " --- onQuery --- ")


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
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в CONTACT_CONTENT_URI
        c.setNotificationUri(getContext()!!.getContentResolver(), uri)
        //db.close();
        return c
    }

    override fun update(
        uri: Uri,
        cv: ContentValues?,
        where: String?,
        selectionArgs: Array<String?>?
    ): Int {
        var where = where
        var numUpdated = 0
        when (sUriMatcher.match(uri)) {
            URI_ORDERS_ID -> {
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update orders set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update orders set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(ORDERS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                ORDERS_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                ORDERS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )

                        var journal_id: Long = 0
                        val cursor = db.query(
                            JOURNAL_TABLE,
                            arrayOf<String>("journal._id"),
                            "order_id=?",
                            arrayOf<String>(
                                _id
                            ),
                            null,
                            null,
                            null
                        )
                        if (cursor.moveToNext()) {
                            journal_id = cursor.getLong(0)
                            getContext()!!.getContentResolver().notifyChange(
                                ContentUris.withAppendedId(
                                    JOURNAL_CONTENT_URI, journal_id
                                ), null
                            )
                        }
                        cursor.close()
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_ORDERS -> {
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update orders set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update orders set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(ORDERS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
                        getContext()!!.getContentResolver()
                            .notifyChange(ORDERS_JOURNAL_CONTENT_URI, null)
                        getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_ORDERS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(ORDERS_LINES_TABLE, cv, where, selectionArgs)
            }

            URI_ORDERS_SILENT -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(ORDERS_TABLE, cv, where, selectionArgs)
            }

            URI_REFUNDS_ID -> {
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update refunds set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update refunds set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(REFUNDS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                REFUNDS_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )

                        var journal_id: Long = 0
                        val cursor = db.query(
                            JOURNAL_TABLE,
                            arrayOf<String>("journal._id"),
                            "refund_id=?",
                            arrayOf<String>(
                                _id
                            ),
                            null,
                            null,
                            null
                        )
                        if (cursor.moveToNext()) {
                            journal_id = cursor.getLong(0)
                            getContext()!!.getContentResolver().notifyChange(
                                ContentUris.withAppendedId(
                                    JOURNAL_CONTENT_URI, journal_id
                                ), null
                            )
                        }
                        cursor.close()
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_REFUNDS -> {
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update refunds set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update refunds set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(REFUNDS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null)
                        getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_REFUNDS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(REFUNDS_LINES_TABLE, cv, where, selectionArgs)
            }

            URI_DISTRIBS_ID -> {
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update distribs set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update distribs set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(DISTRIBS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                DISTRIBS_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )

                        var journal_id: Long = 0
                        val cursor = db.query(
                            JOURNAL_TABLE,
                            arrayOf<String>("journal._id"),
                            "distribs_id=?",
                            arrayOf<String>(
                                _id
                            ),
                            null,
                            null,
                            null
                        )
                        if (cursor.moveToNext()) {
                            journal_id = cursor.getLong(0)
                            getContext()!!.getContentResolver().notifyChange(
                                ContentUris.withAppendedId(
                                    JOURNAL_CONTENT_URI, journal_id
                                ), null
                            )
                        }
                        cursor.close()
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_DISTRIBS -> {
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update distribs set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update distribs set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(DISTRIBS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null)
                        getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_DISTRIBS_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(DISTRIBS_LINES_TABLE, cv, where, selectionArgs)
            }

            URI_NOMENCLATURE -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(NOMENCLATURE_TABLE, cv, where, selectionArgs)
            }

            URI_CLIENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(CLIENTS_TABLE, cv, where, selectionArgs)
            }

            URI_PRICETYPES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(PRICETYPES_TABLE, cv, where, selectionArgs)
            }

            URI_PRICES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(PRICES_TABLE, cv, where, selectionArgs)
            }

            URI_PRICES_AGREEMENTS30 -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(PRICES_AGREEMENTS30_TABLE, cv, where, selectionArgs)
            }

            URI_SALDO -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(SALDO_TABLE, cv, where, selectionArgs)
            }

            URI_SALDO_EXTENDED -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(SALDO_EXTENDED_TABLE, cv, where, selectionArgs)
            }

            URI_RESTS -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(RESTS_TABLE, cv, where, selectionArgs)
            }

            URI_SIMPLE_DISCOUNTS -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(SIMPLE_DISCOUNTS_TABLE, cv, where, selectionArgs)
            }

            URI_SETTINGS -> {
                val db = dbHelper!!.getWritableDatabase()
                var cnt = db.update(SETTINGS_TABLE, cv, where, selectionArgs)
                if (cnt == 0) {
                    db.insert(SETTINGS_TABLE, null, cv)
                    cnt = 1
                }
                return cnt
            }

            URI_PLACES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(PLACES_TABLE, cv, where, selectionArgs)
            }

            URI_OCCUPIED_PLACES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(OCCUPIED_PLACES_TABLE, cv, where, selectionArgs)
            }

            URI_MESSAGES_ID -> {
                val _id = uri.getLastPathSegment()
                if (where == null || where.isEmpty()) {
                    where = "_id=" + _id
                } else {
                    where += " and _id=" + _id
                }
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (cv!!.containsKey("isMark") && cv.getAsInteger("isMark") == 1) {
                        // Версию увеличивать не нужно, это мы помечаем на удаление
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update messages set isMarkCnt=isMarkCnt+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            db.execSQL("update messages set isMarkCnt=isMarkCnt+1")
                        }
                    } else {
                        if (!cv.containsKey("ver")) {
                            // если версия не записывается, увеличим ее на единицу
                            // (скопировано из заказа, возможно не требуется)
                            if (where != null && !where.isEmpty()) {
                                db.execSQL(
                                    "update messages set ver=ver+1 where " + where,
                                    if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                                )
                            } else {
                                // на самом деле такого не будет
                                db.execSQL("update messages set ver=ver+1")
                            }
                        }
                    }
                    numUpdated = db.update(MESSAGES_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                MESSAGES_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                MESSAGES_LIST_CONTENT_URI, _id!!.toInt().toLong()
                            ), null
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_MESSAGES -> {
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (cv!!.containsKey("isMark") && cv.getAsInteger("isMark") == 1) {
                        // Версию увеличивать не нужно, это мы помечаем на удаление
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update messages set isMarkCnt=isMarkCnt+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            db.execSQL("update messages set isMarkCnt=isMarkCnt+1")
                        }
                    } else {
                        if (!cv.containsKey("ver")) {
                            // если версия не записывается, увеличим ее на единицу
                            if (where != null && !where.isEmpty()) {
                                db.execSQL(
                                    "update messages set ver=ver+1 where " + where,
                                    if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                                )
                            } else {
                                // на самом деле такого не будет
                                db.execSQL("update messages set ver=ver+1")
                            }
                        }
                    }
                    numUpdated = db.update(MESSAGES_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null)
                        getContext()!!.getContentResolver()
                            .notifyChange(MESSAGES_LIST_CONTENT_URI, null)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_CASH_PAYMENTS -> {
                val db = dbHelper!!.getWritableDatabase()
                db.beginTransaction()
                try {
                    if (!cv!!.containsKey("versionPDA")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update cash_payments set versionPDA=versionPDA+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update cash_payments set versionPDA=versionPDA+1")
                        }
                    }
                    numUpdated = db.update(CASH_PAYMENTS_TABLE, cv, where, selectionArgs)
                    if (numUpdated > 0) {
                        getContext()!!.getContentResolver()
                            .notifyChange(CASH_PAYMENTS_CONTENT_URI, null)
                        getContext()!!.getContentResolver().notifyChange(
                            CASH_PAYMENTS_JOURNAL_CONTENT_URI, null
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                return numUpdated
            }

            URI_GPS_COORD -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(GPS_COORD_TABLE, cv, where, selectionArgs)
            }

            URI_DISTRIBS_CONTRACTS -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(DISTRIBS_CONTRACTS_TABLE, cv, where, selectionArgs)
            }

            URI_EQUIPMENT -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(EQUIPMENT_TABLE, cv, where, selectionArgs)
            }

            URI_EQUIPMENT_RESTS -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(EQUIPMENT_RESTS_TABLE, cv, where, selectionArgs)
            }

            URI_MTRADE_LOG -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(MTRADE_LOG_TABLE, cv, where, selectionArgs)
            }

            URI_ROUTES_DATES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(ROUTES_DATES_TABLE, cv, where, selectionArgs)
            }

            URI_ROUTES_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(ROUTES_LINES_TABLE, cv, where, selectionArgs)
            }

            URI_REAL_ROUTES_DATES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(REAL_ROUTES_DATES_TABLE, cv, where, selectionArgs)
            }

            URI_REAL_ROUTES_LINES -> {
                val db = dbHelper!!.getWritableDatabase()
                return db.update(REAL_ROUTES_LINES_TABLE, cv, where, selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown URI (for update)" + uri)
        }
    }

        companion object {
            const val LOG_TAG = "mtradeLogs"


            val JOURNAL_REFUND_COLOR_FOR_TRIGGER: String =
                "case when new.dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                        /**/////////////////////////////////////// */ // запрос отмены - серый
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                        "when new.closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                        "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_REFUND_STATE.E_REFUND_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end"

            val JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER: String =
            // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                "case when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                        "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                        "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end"

            // в принципе, этот журнал используется только если не используется общий журнал
            // (скорее всего он уже не используется совсем)
            val ORDERS_COLOR_COLUMN: String =
                "case when dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                        /**/////////////////////////////////////// */ // запрос отмены - серый
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when state=" + E_ORDER_STATE.E_ORDER_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                        "when closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                        "when versionPDA<>versionPDA_ack and (state=" + E_ORDER_STATE.E_ORDER_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end as color"

            val CASH_PAYMENTS_COLOR_COLUMN: String =
            // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                "case when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                        "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                        "when versionPDA<>versionPDA_ack and (state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end as color"

            val REFUNDS_COLOR_COLUMN: String =
                "case when dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                        /**/////////////////////////////////////// */ // запрос отмены - серый
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when state=" + E_REFUND_STATE.E_REFUND_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                        "when closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                        "when versionPDA<>versionPDA_ack and (state=" + E_REFUND_STATE.E_REFUND_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end as color"

            val DISTRIBS_COLOR_COLUMN: String =  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                "case when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                        /**////////////////////////////////////// */ // отменен - красный
                        "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                        "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                        "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                        "when versionPDA<>versionPDA_ack and (state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value() + ") then 10 " +
                        "else 0 end as color"


            const val CLIENT_DESCR_COLUMN: String =
                "CASE WHEN orders.create_client=1 THEN orders.create_client_descr ELSE IFNULL(clients.descr, \"{\"||orders.client_id||\"}\") END as client_descr"
            const val CASH_PAYMENTS_CLIENT_DESCR_COLUMN: String =
                "IFNULL(clients.descr, \"{\"||cash_payments.client_id||\"}\") as client_descr"
            const val SALDO_CLIENT_DESCR_COLUMN: String =
                "IFNULL(clients.descr, \"{\"||saldo_extended.client_id||\"}\") as client_descr"
            const val SALDO_AGREEMENT_DESCR_COLUMN: String =
                "IFNULL(agreements.descr, saldo_extended.agreement_descr) as agreement_descr"
            const val AGREEMENT_DESCR_COLUMN: String = "agreements.descr as agreement_descr"
            const val AGREEMENT30_DESCR_COLUMN: String = "agreements30.descr as agreement_descr"
            const val ORGANIZATION_DESCR_COLUMN: String = "organizations.descr as organization_descr"
            const val PRICE_TYPE_DESCR_COLUMN: String = "pricetypes.descr as pricetype_descr"
            const val NOMENCLATURE_DESCR_COLUMN: String =
                "IFNULL(nomenclature.descr, \"{\"||nomenclature_id||\"}\") as nomencl_descr"
            const val NOMENCLATURE_WEIGHT_COLUMN: String =
                "nomenclature.weight_k_1 as nomencl_weight_k_1"
            const val NOMENCLATURE_ID_COLUMN: String = "nomenclature.id as nomenclature_id"
            const val NOMENCLATURE_ID_SURFING_COLUMN: String =
                "ifnull(nomenclature.id, h.id) as nomenclature_id"
            const val NOMENCLATURE_DESCR_SURFING_COLUMN: String =
                "ifnull(nomenclature.descr, h.groupDescr) as descr"
            const val NOMENCLATURE_GROUP_COLUMN: String = "h.groupDescr as h_groupDescr"

            const val CONTRACT_DESCR_COLUMN: String =
                "IFNULL(distribsContracts.descr, \"{\"||contract_id||\"}\") as contract_descr"


            const val NOMENCLATURE_REST_STOCK_COLUMN: String =
                "restsS.quantity-restsS.quantity_reserve as nom_quantity"
            const val NOMENCLATURE_SALES_COLUMN: String = "restsS.saledNow as nom_quantity_saled_now"
            const val DUMMY_COLUMN: String = "0 as zero"


            // Таблица
            private const val ORDERS_TABLE = "orders"
            private const val CASH_PAYMENTS_TABLE = "cash_payments"
            private const val REFUNDS_TABLE = "refunds"
            private const val CLIENTS_TABLE = "clients"
            private const val NOMENCLATURE_TABLE = "nomenclature"

            private val NOMENCLATURE_LIST_TABLE =
                "nomenclature left join rests_sales_stuff as restsS on restsS.nomenclature_id=nomenclature.id " +
                        "left join salesV as sales_client on sales_client.nomenclature_id=nomenclature.id or sales_client.nomenclature_id=nomenclature.parent_id " +
                        "left join pricesV as prices on prices.nomenclature_id=nomenclature.id " +  // здесь хранятся данные как товаров, так и групп, но нам важно для групп только данные по группам, а по товарам и не нужно - в выборку они не включены
                        "left join salesV_7 as sales7 on sales7.nomenclature_id=nomenclature.id " +
                        "left join nomenclature_hierarchy h on h.id=nomenclature.id " +
                        "left join nomenclature_hierarchy h_parent on h_parent.id=nomenclature.parent_id " +
                        "left join discounts_stuff d_s on d_s.nomenclature_id=nomenclature.id " +
                        "left join discounts_stuff d_s_p on d_s_p.nomenclature_id=nomenclature.parent_id"

            // отличие от NOMENCLATURE_LIST_TABLE в том, что корневой элемент (он только в иерархии) должен присутствоать
            private val NOMENCLATURE_SURFING_TABLE: String = NOMENCLATURE_LIST_TABLE

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
            private const val RESTS_TABLE = "rests"
            private const val SALDO_TABLE = "saldo"
            private const val SALDO_EXTENDED_TABLE = "saldo_extended"
            private const val SALDO_EXTENDED_JOURNAL_TABLE =
                "saldo_extended left join clients on clients.id=saldo_extended.client_id left join agreements on agreements.id=saldo_extended.agreement_id"
            private const val AGREEMENTS_TABLE = "agreements"
            private const val AGREEMENTS30_TABLE = "agreements30"
            private const val VERSIONS_TABLE = "versions"
            private const val VERSIONS_SALES_TABLE = "sales_versions"
            private const val PRICETYPES_TABLE = "pricetypes"
            private const val STOCKS_TABLE = "stocks"
            private const val PRICES_TABLE = "prices"
            private const val PRICES_AGREEMENTS30_TABLE = "prices_agreements30"
            private const val AGENTS_TABLE = "agents"
            private const val CURATORS_TABLE = "curators"
            private const val PERMISSIONS_REQUESTS_TABLE = "permissions_requests"
            private const val ROUTES_TABLE = "routes"
            private const val ROUTES_LINES_TABLE = "routes_lines"
            private const val ROUTES_DATES_TABLE = "routes_dates"
            private const val ROUTES_DATES_LIST_TABLE =
                "routes_dates left join routes on routes.id=routes_dates.route_id"

            private const val REAL_ROUTES_DATES_TABLE = "real_routes_dates"
            private const val REAL_ROUTES_LINES_TABLE = "real_routes_lines"

            private val CURATORS_LIST_TABLE = "(select" +
                    " _id*2 as _id,id,isFolder,parent_id,null as client_id,code,descr from curators " +
                    " union all" +
                    " select" +
                    " max(saldo_extended._id)*2+1,manager_id,2,null,client_id,manager_descr,manager_descr from saldo_extended" +
                    " left join curators on curators.id=manager_id" +
                    " where curators.id is null" +
                    " group by manager_id,client_id,manager_descr,manager_descr" +
                    ") cr"

            private const val DISTR_POINTS_TABLE = "distr_points"
            private const val DISTR_POINTS_LIST_TABLE =
                "distr_points left join pricetypes on pricetypes.id=distr_points.price_type_id"
            private const val ORGANIZATIONS_TABLE = "organizations"
            private const val CLIENTS_WITH_SALDO_TABLE =
                "clients left join saldo on saldo.client_id=clients.id"
            private const val ORDERS_LINES_TABLE = "ordersLines"
            private const val REFUNDS_LINES_TABLE = "refundsLines"
            private const val ORDERS_JOURNAL_TABLE =
                "orders left join clients on clients.id=orders.client_id"
            private const val JOURNAL_TABLE =
                "journal left join clients on clients.id=journal.client_id"

            private const val CASH_PAYMENTS_JOURNAL_TABLE =
                "cash_payments left join clients on clients.id=cash_payments.client_id"
            private const val AGREEMENTS_LIST_TABLE =
                "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id"
            private const val AGREEMENTS30_LIST_TABLE =
                "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id"

            private val AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE =
                "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id " +
                        "left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements.id"
            private val AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE =
                "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id " +
                        "left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements30.id"

            private const val ORDERS_LINES_COMPLEMENTED_TABLE =
                "ordersLines left join nomenclature on nomenclature.id=ordersLines.nomenclature_id"
            private const val REFUNDS_LINES_COMPLEMENTED_TABLE =
                "refundsLines left join nomenclature on nomenclature.id=refundsLines.nomenclature_id"
            private const val SEANCES_TABLE = "seances"
            private const val VICARIOUS_POWER_TABLE = "vicarious_power"
            private const val GPS_COORD_TABLE = "gps_coord"
            private const val MESSAGES_TABLE = "messages"
            private val MESSAGES_LIST_TABLE =
                "messages left join clients on clients.id=messages.client_id and type_idx in (" + E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value() + "," + E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value() + ") left join agents on agents.id=messages.sender_id and acknowledged&4==0 or agents.id=messages.receiver_id and acknowledged&4!=0"

            //private static final String SALES_TABLE = "sales";
            private const val CLIENTS_PRICE_TABLE = "clients_price"
            private const val CURATORS_PRICE_TABLE = "curators_price"
            private const val SETTINGS_TABLE = "settings"
            private const val SALES_LOADED_TABLE = "salesloaded"

            private const val SALES_LOADED_WITH_COMMON_GROUPS_TABLE =
                "(select max(s._id) as _id, datedoc, numdoc, client_id, case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, sum(quantity) as quantity, price from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, price)"

            private const val NOMENCLATURE_HIERARCHY_TABLE = "nomenclature_hierarchy"
            private const val SALES_L_TABLE = "salesL"
            private const val SALES_L2_TABLE = "salesL2"
            private const val SIMPLE_DISCOUNTS_TABLE = "simple_discounts"

            private const val PLACES_TABLE = "places"
            private const val OCCUPIED_PLACES_TABLE = "occupied_places"
            private const val ORDERS_PLACES_TABLE = "ordersPlaces"
            private const val ORDERS_PLACES_LIST_TABLE =
                "orders join ordersPlaces on ordersPlaces.order_id=orders._id"

            private const val DISTRIBS_CONTRACTS_TABLE = "distribsContracts"
            private const val DISTRIBS_CONTRACTS_LIST_TABLE = "distribsContractsList" // TODO
            private const val DISTRIBS_LINES_TABLE = "distribsLines"
            private const val DISTRIBS_LINES_COMPLEMENTED_TABLE =
                "distribsLines left join distribsContracts on distribsContracts.id=distribsLines.contract_id"
            private const val DISTRIBS_TABLE = "distribs"

            private const val EQUIPMENT_TABLE = "equipment"
            private const val EQUIPMENT_RESTS_TABLE = "equipment_rests"
            private const val EQUIPMENT_RESTS_LIST_TABLE =
                "equipment_rests left join equipment on equipment.id=equipment_rests.nomenclature_id"

            private const val MTRADE_LOG_TABLE = "mtradelog"

        // Uri
        // authority
        const val AUTHORITY: String = "ru.code22.providers.mtrade"

        // path
        private const val ORDERS_PATH = "orders"
        private const val CASH_PAYMENTS_PATH = "cash_payments"
        private const val REFUNDS_PATH = "refunds"
        private const val JOURNAL_PATH = "journal"
        private const val CLIENTS_PATH = "clients"
        private const val NOMENCLATURE_PATH = "nomenclature"
        private const val RESTS_PATH = "rests"
        private const val SALDO_PATH = "saldo"
        private const val SALDO_EXTENDED_PATH = "saldo_extended"
        private const val SALDO_EXTENDED_JOURNAL_PATH = "saldo_extended_journal"
        private const val AGREEMENTS_PATH = "agreement"
        private const val AGREEMENTS30_PATH = "agreement30"
        private const val PRICETYPES_PATH = "pricetypes"
        private const val VERSIONS_PATH = "versions"
        private const val STOCKS_PATH = "stocks"
        private const val PRICES_PATH = "prices"
        private const val PRICES_AGREEMENTS30_PATH = "pricesAgreements30"
        private const val AGENTS_PATH = "agents"
        private const val CURATORS_PATH = "curators"
        private const val CURATORS_LIST_PATH = "curatorsList"
        private const val DISTR_POINTS_PATH = "distr_points"
        private const val DISTR_POINTS_LIST_PATH = "distr_pointsList"
        private const val ORGANIZATIONS_PATH = "organizations"
        private const val CLIENTS_WITH_SALDO_PATH = "clients_saldo"
        private const val ORDERS_LINES_PATH = "ordersLines"
        private const val REFUNDS_LINES_PATH = "refundsLines"
        private const val ORDERS_JOURNAL_PATH = "ordersJournal"
        private const val CASH_PAYMENTS_JOURNAL_PATH = "cash_paymentsJournal"
        private const val AGREEMENTS_LIST_PATH = "agreementsList"
        private const val AGREEMENTS30_LIST_PATH = "agreements30List"
        private const val AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH = "agreementsListSaldoOnly"
        private const val AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH = "agreements30ListSaldoOnly"
        private const val ORDERS_LINES_COMPLEMENTED_PATH = "ordersLinesComplemented"
        private const val REFUNDS_LINES_COMPLEMENTED_PATH = "refundsLinesComplemented"
        private const val SEANCES_PATH = "seances"
        private const val SEANCES_INCOMING_PATH = "seancesIncoming"
        private const val SEANCES_OUTGOING_PATH = "seancesOutgoing"
        private const val REINDEX_PATH = "reindex"
        private const val VACUUM_PATH = "vacuum"
        private const val SORT_PATH = "sort"
        private const val VICARIOUS_POWER_PATH = "vicarious_power"
        private const val MESSAGES_PATH = "messages"
        private const val MESSAGES_LIST_PATH = "messagesList"
        private const val NOMENCLATURE_LIST_PATH = "nomenclatureList"
        private const val NOMENCLATURE_SURFING_PATH = "nomenclatureList"
        private const val SALES_PATH = "sales"
        private const val RESTS_SALES_STUFF_PATH = "rests_sales_stuff"
        private const val DISCOUNTS_STUFF_MEGA_PATH = "discounts_stuff_mega"
        private const val DISCOUNTS_STUFF_SIMPLE_PATH = "discounts_stuff_simple"
        private const val DISCOUNTS_STUFF_OTHER_PATH = "discounts_stuff_other"
        private const val PRICESV_MEGA_PATH = "pricesVmega"
        private const val PRICESV_OTHER_PATH = "pricesVother"
        private const val CLIENTS_PRICE_PATH = "clients_price"
        private const val CURATORS_PRICE_PATH = "curators_price"
        private const val SETTINGS_PATH = "settings"
        private const val SALES_LOADED_PATH = "salesloaded"
        private const val SALES_L2_PATH = "salesL2"

        private const val VERSIONS_SALES_PATH = "salesversions"
        private const val NOMENCLATURE_HIERARCHY_PATH = "nomenclature_hierarchy"
        private const val CREATE_VIEWS_PATH = "create_views"
        private const val CREATE_SALES_L_PATH = "create_salesL"
        private const val ORDERS_SILENT_PATH = "ordersSilewhile (distribsJournal.moveToNext()) {nt"
        private const val REFUNDS_SILENT_PATH = "refundsSilent"

        private const val SIMPLE_DISCOUNTS_PATH = "simple_discounts"

        private const val PLACES_PATH = "places"
        private const val OCCUPIED_PLACES_PATH = "occupiedPlaces"
        private const val ORDERS_PLACES_PATH = "ordersPlaces"
        private const val ORDERS_PLACES_LIST_PATH = "ordersPlacesList"

        private const val GPS_COORD_PATH = "gps_coord"

        private const val DISTRIBS_CONTRACTS_PATH = "distribsContracts"
        private const val DISTRIBS_CONTRACTS_LIST_PATH = "distribsContractsList"
        private const val DISTRIBS_LINES_PATH = "distribsLines"
        private const val DISTRIBS_LINES_COMPLEMENTED_PATH = "distribsLinesComplemented"
        private const val DISTRIBS_PATH = "distribs"

        private const val EQUIPMENT_PATH = "equipment"
        private const val EQUIPMENT_RESTS_PATH = "equipment_rests"
        private const val EQUIPMENT_RESTS_LIST_PATH = "equipment_rests_list"

        private const val MTRADE_LOG_PATH = "mtradelog"
        private const val PERMISSIONS_REQUESTS_PATH = "permissions_requests"

        private const val ROUTES_PATH = "routes"
        private const val ROUTES_LINES_PATH = "routes_lines"
        private const val ROUTES_DATES_PATH = "routes_dates"
        private const val ROUTES_DATES_LIST_PATH = "routes_dates_list"

        private const val REAL_ROUTES_DATES_PATH = "real_routes_dates"
        private const val REAL_ROUTES_LINES_PATH = "real_routes_lines"

        // Общий Uri
        @JvmField
        val ORDERS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_PATH)
        )
        @JvmField
        val CASH_PAYMENTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CASH_PAYMENTS_PATH)
        )
        @JvmField
        val REFUNDS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REFUNDS_PATH)
        )
        @JvmField
        val JOURNAL_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + JOURNAL_PATH)
        )
        @JvmField
        val CLIENTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CLIENTS_PATH)
        )
        @JvmField
        val NOMENCLATURE_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + NOMENCLATURE_PATH)
        )
        @JvmField
        val RESTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + RESTS_PATH)
        )
        @JvmField
        val SALDO_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALDO_PATH)
        )
        @JvmField
        val SALDO_EXTENDED_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALDO_EXTENDED_PATH)
        )
        @JvmField
        val SALDO_EXTENDED_JOURNAL_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALDO_EXTENDED_JOURNAL_PATH)
        )
        @JvmField
        val AGREEMENTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS_PATH)
        )
        @JvmField
        val AGREEMENTS30_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS30_PATH)
        )
        @JvmField
        val VERSIONS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + VERSIONS_PATH)
        )
        @JvmField
        val PRICETYPES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PRICETYPES_PATH)
        )
        @JvmField
        val STOCKS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + STOCKS_PATH)
        )
        @JvmField
        val PRICES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PRICES_PATH)
        )
        @JvmField
        val PRICES_AGREEMENTS30_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PRICES_AGREEMENTS30_PATH)
        )
        @JvmField
        val AGENTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGENTS_PATH)
        )
        @JvmField
        val CURATORS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CURATORS_PATH)
        )
        @JvmField
        val CURATORS_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CURATORS_LIST_PATH)
        )
        @JvmField
        val DISTR_POINTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTR_POINTS_PATH)
        )
        @JvmField
        val DISTR_POINTS_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTR_POINTS_LIST_PATH)
        )
        @JvmField
        val ORGANIZATIONS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORGANIZATIONS_PATH)
        )
        @JvmField
        val CLIENTS_WITH_SALDO_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CLIENTS_WITH_SALDO_PATH)
        )
        @JvmField
        val ORDERS_LINES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_LINES_PATH)
        )
        @JvmField
        val REFUNDS_LINES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REFUNDS_LINES_PATH)
        )
        val ORDERS_JOURNAL_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_JOURNAL_PATH)
        )
        @JvmField
        val CASH_PAYMENTS_JOURNAL_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CASH_PAYMENTS_JOURNAL_PATH)
        )
        @JvmField
        val AGREEMENTS_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS_LIST_PATH)
        )
        val AGREEMENTS30_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS30_LIST_PATH)
        )
        @JvmField
        val AGREEMENTS_LIST_WITH_SALDO_ONLY_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH)
        )
        @JvmField
        val AGREEMENTS30_LIST_WITH_SALDO_ONLY_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH)
        )
        @JvmField
        val ORDERS_LINES_COMPLEMENTED_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_LINES_COMPLEMENTED_PATH)
        )
        @JvmField
        val REFUNDS_LINES_COMPLEMENTED_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REFUNDS_LINES_COMPLEMENTED_PATH)
        )
        @JvmField
        val SEANCES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SEANCES_PATH)
        )
        @JvmField
        val SEANCES_INCOMING_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SEANCES_INCOMING_PATH)
        )
        @JvmField
        val SEANCES_OUTGOING_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SEANCES_OUTGOING_PATH)
        )
        @JvmField
        val REINDEX_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REINDEX_PATH)
        )
        @JvmField
        val VACUUM_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + VACUUM_PATH)
        )
        @JvmField
        val SORT_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SORT_PATH)
        )
        @JvmField
        val VICARIOUS_POWER_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + VICARIOUS_POWER_PATH)
        )
        @JvmField
        val MESSAGES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + MESSAGES_PATH)
        )
        @JvmField
        val MESSAGES_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + MESSAGES_LIST_PATH)
        )
        @JvmField
        val NOMENCLATURE_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + NOMENCLATURE_LIST_PATH)
        )
        @JvmField
        val NOMENCLATURE_SURFING_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + NOMENCLATURE_SURFING_PATH)
        )

        @JvmField
        val SIMPLE_DISCOUNTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SIMPLE_DISCOUNTS_PATH)
        )

        val SALES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALES_PATH)
        )

        @JvmField
        val RESTS_SALES_STUFF_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + RESTS_SALES_STUFF_PATH)
        )

        @JvmField
        val DISCOUNTS_STUFF_MEGA_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISCOUNTS_STUFF_MEGA_PATH)
        )

        @JvmField
        val DISCOUNTS_STUFF_SIMPLE_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISCOUNTS_STUFF_SIMPLE_PATH)
        )

        @JvmField
        val DISCOUNTS_STUFF_OTHER_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISCOUNTS_STUFF_OTHER_PATH)
        )

        @JvmField
        val PRICESV_MEGA_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PRICESV_MEGA_PATH)
        )

        @JvmField
        val PRICESV_OTHER_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PRICESV_OTHER_PATH)
        )

        @JvmField
        val CLIENTS_PRICE_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CLIENTS_PRICE_PATH)
        )

        @JvmField
        val CURATORS_PRICE_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CURATORS_PRICE_PATH)
        )

        @JvmField
        val SETTINGS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SETTINGS_PATH)
        )

        @JvmField
        val SALES_LOADED_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALES_LOADED_PATH)
        )

        //public static final Uri SALES_LOADED_WITH_COMMON_GROUPS_CONTENT_URI = Uri.parse("content://"
        //      + AUTHORITY + "/" + SALES_LOADED_WITH_COMMON_GROUPS_PATH);
        @JvmField
        val SALES_L2_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + SALES_L2_PATH)
        )

        @JvmField
        val VERSIONS_SALES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + VERSIONS_SALES_PATH)
        )

        @JvmField
        val NOMENCLATURE_HIERARCHY_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + NOMENCLATURE_HIERARCHY_PATH)
        )

        @JvmField
        val CREATE_VIEWS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CREATE_VIEWS_PATH)
        )

        @JvmField
        val CREATE_SALES_L_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + CREATE_SALES_L_PATH)
        )

        @JvmField
        val ORDERS_SILENT_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_SILENT_PATH)
        )

        @JvmField
        val REFUNDS_SILENT_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REFUNDS_SILENT_PATH)
        )

        @JvmField
        val PLACES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PLACES_PATH)
        )

        @JvmField
        val OCCUPIED_PLACES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + OCCUPIED_PLACES_PATH)
        )

        @JvmField
        val ORDERS_PLACES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_PLACES_PATH)
        )

        @JvmField
        val ORDERS_PLACES_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ORDERS_PLACES_LIST_PATH)
        )

        @JvmField
        val GPS_COORD_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + GPS_COORD_PATH)
        )


        @JvmField
        val DISTRIBS_CONTRACTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTRIBS_CONTRACTS_PATH)
        )

        @JvmField
        val DISTRIBS_CONTRACTS_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTRIBS_CONTRACTS_LIST_PATH)
        )

        @JvmField
        val DISTRIBS_LINES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTRIBS_LINES_PATH)
        )

        @JvmField
        val DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTRIBS_LINES_COMPLEMENTED_PATH)
        )

        @JvmField
        val DISTRIBS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + DISTRIBS_PATH)
        )

        @JvmField
        val EQUIPMENT_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + EQUIPMENT_PATH)
        )

        @JvmField
        val EQUIPMENT_RESTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + EQUIPMENT_RESTS_PATH)
        )

        @JvmField
        val EQUIPMENT_RESTS_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + EQUIPMENT_RESTS_LIST_PATH)
        )

        val MTRADE_LOG_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + MTRADE_LOG_PATH)
        )

        @JvmField
        val PERMISSIONS_REQUESTS_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + PERMISSIONS_REQUESTS_PATH)
        )

        @JvmField
        val ROUTES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ROUTES_PATH)
        )

        @JvmField
        val ROUTES_LINES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ROUTES_LINES_PATH)
        )

        @JvmField
        val ROUTES_DATES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ROUTES_DATES_PATH)
        )

        @JvmField
        val ROUTES_DATES_LIST_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + ROUTES_DATES_LIST_PATH)
        )

        @JvmField
        val REAL_ROUTES_DATES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REAL_ROUTES_DATES_PATH)
        )

        @JvmField
        val REAL_ROUTES_LINES_CONTENT_URI: Uri = Uri.parse(
            ("content://"
                    + AUTHORITY + "/" + REAL_ROUTES_LINES_PATH)
        )

        private val sUriMatcher: UriMatcher

        private const val URI_ORDERS = 1
        private const val URI_ORDERS_ID = 2
        private const val URI_CLIENTS = 3
        private const val URI_CLIENTS_ID = 4
        private const val URI_NOMENCLATURE = 5
        private const val URI_NOMENCLATURE_ID = 6
        private const val URI_RESTS = 7
        private const val URI_RESTS_ID = 8
        private const val URI_SALDO = 9
        private const val URI_SALDO_ID = 10
        private const val URI_AGREEMENTS = 11
        private const val URI_AGREEMENTS_ID = 12
        private const val URI_VERSIONS = 13
        private const val URI_VERSIONS_ID = 14
        private const val URI_PRICETYPES = 15
        private const val URI_PRICETYPES_ID = 16
        private const val URI_STOCKS = 17
        private const val URI_STOCKS_ID = 18
        private const val URI_PRICES = 19
        private const val URI_PRICES_ID = 20
        private const val URI_AGENTS = 21
        private const val URI_AGENTS_ID = 22
        private const val URI_CURATORS = 23
        private const val URI_CURATORS_ID = 24
        private const val URI_DISTR_POINTS = 25
        private const val URI_DISTR_POINTS_ID = 26
        private const val URI_ORGANIZATIONS = 27
        private const val URI_ORGANIZATIONS_ID = 28
        private const val URI_CLIENTS_WITH_SALDO = 29
        private const val URI_ORDERS_LINES = 30
        private const val URI_ORDERS_JOURNAL = 31
        private const val URI_AGREEMENTS_LIST = 32
        private const val URI_ORDERS_LINES_COMPLEMENTED = 33
        private const val URI_SEANCES = 34
        private const val URI_SEANCES_INCOMING = 35
        private const val URI_SEANCES_OUTGOING = 36
        private const val URI_REINDEX = 37
        private const val URI_MESSAGES = 38
        private const val URI_MESSAGES_ID = 39
        private const val URI_NOMENCLATURE_LIST = 40
        private const val URI_NOMENCLATURE_SURFING = 41
        private const val URI_SALES = 50
        private const val URI_RESTS_SALES_STUFF = 51

        private const val URI_CLIENTS_PRICE = 52
        private const val URI_SETTINGS = 53
        private const val URI_SALES_LOADED = 54
        private const val URI_VERSIONS_SALES = 55
        private const val URI_NOMENCLATURE_HIERARCHY = 56
        private const val URI_CREATE_VIEWS = 57
        private const val URI_CREATE_SALES_L = 58
        private const val URI_MESSAGES_LIST = 59
        private const val URI_ORDERS_SILENT = 60
        private const val URI_ORDERS_SILENT_ID = 61

        //private static final int URI_DISCOUNTS_STUFF = 54;
        private const val URI_CURATORS_PRICE = 62

        private const val URI_SIMPLE_DISCOUNTS = 63
        private const val URI_SIMPLE_DISCOUNTS_ID = 64

        private const val URI_CASH_PAYMENTS = 65
        private const val URI_CASH_PAYMENTS_ID = 66
        private const val URI_CASH_PAYMENTS_JOURNAL = 67

        private const val URI_SALDO_EXTENDED = 90
        private const val URI_SALDO_EXTENDED_ID = 91
        private const val URI_SALDO_EXTENDED_JOURNAL = 92

        private const val URI_CURATORS_LIST = 93

        private const val URI_PLACES = 94
        private const val URI_OCCUPIED_PLACES = 95
        private const val URI_ORDERS_PLACES = 96
        private const val URI_ORDERS_PLACES_LIST = 97

        private const val URI_REFUNDS = 100
        private const val URI_REFUNDS_ID = 101
        private const val URI_DISTRIBS_ID = 102

        private const val URI_JOURNAL = 103
        private const val URI_JOURNAL_ID = 104

        private const val URI_REFUNDS_LINES = 105
        private const val URI_REFUNDS_LINES_COMPLEMENTED = 106

        private const val URI_REFUNDS_SILENT = 107
        private const val URI_REFUNDS_SILENT_ID = 108

        private const val URI_SALES_L2 = 109

        private const val URI_SORT = 111

        private const val URI_VICARIOUS_POWER = 112
        private const val URI_VICARIOUS_POWER_ID = 113

        private const val URI_GPS_COORD = 114

        private const val URI_AGREEMENTS_WITH_SALDO_ONLY_LIST = 115


        private const val URI_DISTRIBS_CONTRACTS = 120
        private const val URI_DISTRIBS_CONTRACTS_LIST = 121
        private const val URI_DISTRIBS_LINES = 122
        private const val URI_DISTRIBS_LINES_COMPLEMENTED = 123
        private const val URI_DISTRIBS = 124

        private const val URI_EQUIPMENT = 125
        private const val URI_EQUIPMENT_RESTS = 126
        private const val URI_EQUIPMENT_RESTS_LIST = 127

        private const val URI_DISTR_POINTS_LIST = 128

        private const val URI_PRICESV_MEGA = 129
        private const val URI_PRICESV_OTHER = 130

        private const val URI_DISCOUNTS_STUFF_MEGA = 131
        private const val URI_DISCOUNTS_STUFF_SIMPLE = 132
        private const val URI_DISCOUNTS_STUFF_OTHER = 133

        private const val URI_MTRADE_LOG = 134
        private const val URI_PERMISSIONS_REQUESTS = 135

        private const val URI_AGREEMENTS30 = 136
        private const val URI_AGREEMENTS30_ID = 137
        private const val URI_AGREEMENTS30_LIST = 138
        private const val URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST = 139
        private const val URI_PRICES_AGREEMENTS30 = 140

        private const val URI_ROUTES = 150
        private const val URI_ROUTES_LINES = 151
        private const val URI_ROUTES_DATES = 152
        private const val URI_ROUTES_DATES_ID = 153
        private const val URI_ROUTES_DATES_LIST = 154
        private const val URI_ROUTES_DATES_LIST_ID = 155

        private const val URI_REAL_ROUTES_DATES = 156
        private const val URI_REAL_ROUTES_DATES_ID = 1157
        private const val URI_REAL_ROUTES_LINES = 158

        private const val URI_VACUUM = 159

        private val ordersProjectionMap: HashMap<String?, String?>?
        private val cashPaymentsProjectionMap: HashMap<String?, String?>?
        private val refundsProjectionMap: HashMap<String?, String?>?
        private val journalProjectionMap: HashMap<String?, String?>?
        private val clientsProjectionMap: HashMap<String?, String?>?
        private val nomenclatureProjectionMap: HashMap<String?, String?>?
        private val nomenclatureHierarchyProjectionMap: HashMap<String?, String?>?
        private val restsProjectionMap: HashMap<String?, String?>?
        private val saldoProjectionMap: HashMap<String?, String?>?
        private val saldoExtendedProjectionMap: HashMap<String?, String?>?
        private val saldoExtendedJournalProjectionMap: HashMap<String?, String?>?
        private val agreementsProjectionMap: HashMap<String?, String?>?
        private val agreements30ProjectionMap: HashMap<String?, String?>?
        private val versionsProjectionMap: HashMap<String?, String?>?
        private val pricetypesProjectionMap: HashMap<String?, String?>?
        private val stocksProjectionMap: HashMap<String?, String?>?
        private val pricesProjectionMap: HashMap<String?, String?>?
        private val pricesAgreements30ProjectionMap: HashMap<String?, String?>?
        private val agentsProjectionMap: HashMap<String?, String?>?
        private val curatorsProjectionMap: HashMap<String?, String?>?
        private val curatorsListProjectionMap: HashMap<String?, String?>?
        private val distrPointsProjectionMap: HashMap<String?, String?>?
        private val distrPointsListProjectionMap: HashMap<String?, String?>?
        private val organizationsProjectionMap: HashMap<String?, String?>?
        private val clientsWithSaldoProjectionMap: HashMap<String?, String?>?
        private val ordersLinesProjectionMap: HashMap<String?, String?>?
        private val refundsLinesProjectionMap: HashMap<String?, String?>?
        private val ordersJournalProjectionMap: HashMap<String?, String?>?
        private val cashPaymentsJournalProjectionMap: HashMap<String?, String?>?
        private val agreementsListProjectionMap: HashMap<String?, String?>?
        private val agreements30ListProjectionMap: HashMap<String?, String?>? = null
        private val agreementsListWithSaldoOnlyProjectionMap: HashMap<String?, String?>?
        private val agreements30ListWithSaldoOnlyProjectionMap: HashMap<String?, String?>?
        private val ordersLinesComplementedProjectionMap: HashMap<String?, String?>?
        private val refundsLinesComplementedProjectionMap: HashMap<String?, String?>?
        private val seancesProjectionMap: HashMap<String?, String?>?
        private val vicariousPowerProjectionMap: HashMap<String?, String?>?
        private val messagesProjectionMap: HashMap<String?, String?>?
        private val messagesListProjectionMap: HashMap<String?, String?>?
        private val nomenclatureListProjectionMap: HashMap<String?, String?>
        private val nomenclatureSurfingProjectionMap: HashMap<String?, String?>?
        private val clientsPriceProjectionMap: HashMap<String?, String?>?
        private val curatorsPriceProjectionMap: HashMap<String?, String?>?
        private val simpleDiscountsProjectionMap: HashMap<String?, String?>?
        private val settingsProjectionMap: HashMap<String?, String?>?
        private val salesLoadedProjectionMap: HashMap<String?, String?>?

        //private static HashMap<String, String> salesLoadedWithCommonGroupsProjectionMap;
        private val salesL2ProjectionMap: HashMap<String?, String?>?
        private val versionsSalesProjectionMap: HashMap<String?, String?>?
        private val hierarchyProjectionMap: HashMap<String?, String?>?

        private val placesProjectionMap: HashMap<String?, String?>?
        private val occupiedPlacesProjectionMap: HashMap<String?, String?>?
        private val ordersPlacesProjectionMap: HashMap<String?, String?>?
        private val ordersPlacesListProjectionMap: HashMap<String?, String?>?

        private val gpsCoordProjectionMap: HashMap<String?, String?>?

        private val distribsContractsProjectionMap: HashMap<String?, String?>?
        private val distribsContractsListProjectionMap: HashMap<String?, String?>?
        private val distribsLinesProjectionMap: HashMap<String?, String?>?
        private val distribsLinesComplementedProjectionMap: HashMap<String?, String?>?
        private val distribsProjectionMap: HashMap<String?, String?>?

        private val equipmentProjectionMap: HashMap<String?, String?>?
        private val equipmentRestsProjectionMap: HashMap<String?, String?>?
        private val equipmentRestsListProjectionMap: HashMap<String?, String?>?

        private val mtradeLogProjectionMap: HashMap<String?, String?>?
        private val permissionsRequestsProjectionMap: HashMap<String?, String?>?

        private val routesProjectionMap: HashMap<String?, String?>?
        private val routesLinesProjectionMap: HashMap<String?, String?>?
        private val routesDatesProjectionMap: HashMap<String?, String?>?
        private val routesDatesListProjectionMap: HashMap<String?, String?>?

        private val realRoutesDatesProjectionMap: HashMap<String?, String?>?
        private val realRoutesLinesProjectionMap: HashMap<String?, String?>?


        init {
            sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            sUriMatcher.addURI(AUTHORITY, ORDERS_PATH, URI_ORDERS)
            sUriMatcher.addURI(AUTHORITY, ORDERS_PATH + "/#", URI_ORDERS_ID)
            sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_PATH, URI_CASH_PAYMENTS)
            sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_PATH + "/#", URI_CASH_PAYMENTS_ID)
            sUriMatcher.addURI(AUTHORITY, REFUNDS_PATH, URI_REFUNDS)
            sUriMatcher.addURI(AUTHORITY, REFUNDS_PATH + "/#", URI_REFUNDS_ID)
            sUriMatcher.addURI(AUTHORITY, JOURNAL_PATH, URI_JOURNAL)
            sUriMatcher.addURI(AUTHORITY, JOURNAL_PATH + "/#", URI_JOURNAL_ID)
            sUriMatcher.addURI(AUTHORITY, CLIENTS_PATH, URI_CLIENTS)
            sUriMatcher.addURI(AUTHORITY, CLIENTS_PATH + "/#", URI_CLIENTS_ID)
            sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_PATH, URI_NOMENCLATURE)
            sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_PATH + "/#", URI_NOMENCLATURE_ID)
            sUriMatcher.addURI(AUTHORITY, RESTS_PATH, URI_RESTS)
            sUriMatcher.addURI(AUTHORITY, RESTS_PATH + "/#", URI_RESTS_ID)
            sUriMatcher.addURI(AUTHORITY, SALDO_PATH, URI_SALDO)
            sUriMatcher.addURI(AUTHORITY, SALDO_PATH + "/#", URI_SALDO_ID)
            sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_PATH, URI_SALDO_EXTENDED)
            sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_PATH + "/#", URI_SALDO_EXTENDED_ID)
            sUriMatcher.addURI(AUTHORITY, SALDO_EXTENDED_JOURNAL_PATH, URI_SALDO_EXTENDED_JOURNAL)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS_PATH, URI_AGREEMENTS)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS_PATH + "/#", URI_AGREEMENTS_ID)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_PATH, URI_AGREEMENTS30)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_PATH + "/#", URI_AGREEMENTS30_ID)
            sUriMatcher.addURI(AUTHORITY, VERSIONS_PATH, URI_VERSIONS)
            sUriMatcher.addURI(AUTHORITY, VERSIONS_PATH + "/#", URI_VERSIONS_ID)
            sUriMatcher.addURI(AUTHORITY, PRICETYPES_PATH, URI_PRICETYPES)
            sUriMatcher.addURI(AUTHORITY, PRICETYPES_PATH + "/#", URI_PRICETYPES_ID)
            sUriMatcher.addURI(AUTHORITY, STOCKS_PATH, URI_STOCKS)
            sUriMatcher.addURI(AUTHORITY, STOCKS_PATH + "/#", URI_STOCKS_ID)
            sUriMatcher.addURI(AUTHORITY, PRICES_PATH, URI_PRICES)
            sUriMatcher.addURI(AUTHORITY, PRICES_PATH + "/#", URI_PRICES_ID)
            sUriMatcher.addURI(AUTHORITY, PRICES_AGREEMENTS30_PATH, URI_PRICES_AGREEMENTS30)
            sUriMatcher.addURI(AUTHORITY, AGENTS_PATH, URI_AGENTS)
            sUriMatcher.addURI(AUTHORITY, AGENTS_PATH + "/#", URI_AGENTS_ID)
            sUriMatcher.addURI(AUTHORITY, CURATORS_PATH, URI_CURATORS)
            sUriMatcher.addURI(AUTHORITY, CURATORS_PATH + "/#", URI_CURATORS_ID)
            sUriMatcher.addURI(AUTHORITY, CURATORS_LIST_PATH, URI_CURATORS_LIST)
            sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_PATH, URI_DISTR_POINTS)
            sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_PATH + "/#", URI_DISTR_POINTS_ID)
            sUriMatcher.addURI(AUTHORITY, DISTR_POINTS_LIST_PATH, URI_DISTR_POINTS_LIST)
            sUriMatcher.addURI(AUTHORITY, ORGANIZATIONS_PATH, URI_ORGANIZATIONS)
            sUriMatcher.addURI(AUTHORITY, ORGANIZATIONS_PATH + "/#", URI_ORGANIZATIONS_ID)
            sUriMatcher.addURI(AUTHORITY, CLIENTS_WITH_SALDO_PATH, URI_CLIENTS_WITH_SALDO)
            sUriMatcher.addURI(AUTHORITY, ORDERS_LINES_PATH, URI_ORDERS_LINES)
            sUriMatcher.addURI(AUTHORITY, REFUNDS_LINES_PATH, URI_REFUNDS_LINES)
            sUriMatcher.addURI(AUTHORITY, ORDERS_JOURNAL_PATH, URI_ORDERS_JOURNAL)
            sUriMatcher.addURI(AUTHORITY, CASH_PAYMENTS_JOURNAL_PATH, URI_CASH_PAYMENTS_JOURNAL)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS_LIST_PATH, URI_AGREEMENTS_LIST)
            sUriMatcher.addURI(AUTHORITY, AGREEMENTS30_LIST_PATH, URI_AGREEMENTS30_LIST)
            sUriMatcher.addURI(
                AUTHORITY,
                AGREEMENTS_LIST_WITH_SALDO_ONLY_PATH,
                URI_AGREEMENTS_WITH_SALDO_ONLY_LIST
            )
            sUriMatcher.addURI(
                AUTHORITY,
                AGREEMENTS30_LIST_WITH_SALDO_ONLY_PATH,
                URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST
            )
            sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH, URI_SIMPLE_DISCOUNTS)
            sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH + "/#", URI_SIMPLE_DISCOUNTS_ID)
            sUriMatcher.addURI(
                AUTHORITY,
                ORDERS_LINES_COMPLEMENTED_PATH,
                URI_ORDERS_LINES_COMPLEMENTED
            )
            sUriMatcher.addURI(
                AUTHORITY,
                REFUNDS_LINES_COMPLEMENTED_PATH,
                URI_REFUNDS_LINES_COMPLEMENTED
            )
            sUriMatcher.addURI(AUTHORITY, SEANCES_PATH, URI_SEANCES)
            sUriMatcher.addURI(AUTHORITY, SEANCES_INCOMING_PATH, URI_SEANCES_INCOMING)
            sUriMatcher.addURI(AUTHORITY, SEANCES_OUTGOING_PATH, URI_SEANCES_OUTGOING)
            sUriMatcher.addURI(AUTHORITY, REINDEX_PATH, URI_REINDEX)
            sUriMatcher.addURI(AUTHORITY, VACUUM_PATH, URI_VACUUM)
            sUriMatcher.addURI(AUTHORITY, SORT_PATH, URI_SORT)
            sUriMatcher.addURI(AUTHORITY, VICARIOUS_POWER_PATH, URI_VICARIOUS_POWER)
            sUriMatcher.addURI(AUTHORITY, VICARIOUS_POWER_PATH + "/#", URI_VICARIOUS_POWER_ID)
            sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH, URI_MESSAGES)
            sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH + "/#", URI_MESSAGES_ID)
            sUriMatcher.addURI(AUTHORITY, MESSAGES_LIST_PATH, URI_MESSAGES_LIST)
            sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_LIST_PATH, URI_NOMENCLATURE_LIST)
            sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_SURFING_PATH, URI_NOMENCLATURE_SURFING)
            sUriMatcher.addURI(AUTHORITY, SALES_PATH, URI_SALES)
            sUriMatcher.addURI(AUTHORITY, RESTS_SALES_STUFF_PATH, URI_RESTS_SALES_STUFF)
            sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_MEGA_PATH, URI_DISCOUNTS_STUFF_MEGA)
            sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_SIMPLE_PATH, URI_DISCOUNTS_STUFF_SIMPLE)
            sUriMatcher.addURI(AUTHORITY, DISCOUNTS_STUFF_OTHER_PATH, URI_DISCOUNTS_STUFF_OTHER)
            sUriMatcher.addURI(AUTHORITY, PRICESV_MEGA_PATH, URI_PRICESV_MEGA)
            sUriMatcher.addURI(AUTHORITY, PRICESV_OTHER_PATH, URI_PRICESV_OTHER)
            sUriMatcher.addURI(AUTHORITY, CLIENTS_PRICE_PATH, URI_CLIENTS_PRICE)
            sUriMatcher.addURI(AUTHORITY, CURATORS_PRICE_PATH, URI_CURATORS_PRICE)
            sUriMatcher.addURI(AUTHORITY, SIMPLE_DISCOUNTS_PATH, URI_SIMPLE_DISCOUNTS)
            sUriMatcher.addURI(AUTHORITY, SETTINGS_PATH, URI_SETTINGS)
            sUriMatcher.addURI(AUTHORITY, SALES_LOADED_PATH, URI_SALES_LOADED)
            sUriMatcher.addURI(AUTHORITY, SALES_L2_PATH, URI_SALES_L2)
            sUriMatcher.addURI(AUTHORITY, VERSIONS_SALES_PATH, URI_VERSIONS_SALES)
            sUriMatcher.addURI(AUTHORITY, NOMENCLATURE_HIERARCHY_PATH, URI_NOMENCLATURE_HIERARCHY)
            sUriMatcher.addURI(AUTHORITY, CREATE_VIEWS_PATH, URI_CREATE_VIEWS)
            sUriMatcher.addURI(AUTHORITY, CREATE_SALES_L_PATH, URI_CREATE_SALES_L)
            sUriMatcher.addURI(AUTHORITY, ORDERS_SILENT_PATH, URI_ORDERS_SILENT)
            sUriMatcher.addURI(AUTHORITY, ORDERS_SILENT_PATH + "/#", URI_ORDERS_SILENT_ID)

            sUriMatcher.addURI(AUTHORITY, REFUNDS_SILENT_PATH, URI_REFUNDS_SILENT)
            sUriMatcher.addURI(AUTHORITY, REFUNDS_SILENT_PATH + "/#", URI_REFUNDS_SILENT_ID)

            sUriMatcher.addURI(AUTHORITY, PLACES_PATH, URI_PLACES)
            sUriMatcher.addURI(AUTHORITY, OCCUPIED_PLACES_PATH, URI_OCCUPIED_PLACES)
            sUriMatcher.addURI(AUTHORITY, ORDERS_PLACES_PATH, URI_ORDERS_PLACES)
            sUriMatcher.addURI(AUTHORITY, ORDERS_PLACES_LIST_PATH, URI_ORDERS_PLACES_LIST)

            sUriMatcher.addURI(AUTHORITY, GPS_COORD_PATH, URI_GPS_COORD)

            sUriMatcher.addURI(AUTHORITY, DISTRIBS_CONTRACTS_PATH, URI_DISTRIBS_CONTRACTS)
            sUriMatcher.addURI(AUTHORITY, DISTRIBS_CONTRACTS_LIST_PATH, URI_DISTRIBS_CONTRACTS_LIST)
            sUriMatcher.addURI(AUTHORITY, DISTRIBS_LINES_PATH, URI_DISTRIBS_LINES)
            sUriMatcher.addURI(
                AUTHORITY,
                DISTRIBS_LINES_COMPLEMENTED_PATH,
                URI_DISTRIBS_LINES_COMPLEMENTED
            )
            sUriMatcher.addURI(AUTHORITY, DISTRIBS_PATH, URI_DISTRIBS)
            sUriMatcher.addURI(AUTHORITY, DISTRIBS_PATH + "/#", URI_DISTRIBS_ID)

            sUriMatcher.addURI(AUTHORITY, EQUIPMENT_PATH, URI_EQUIPMENT)
            sUriMatcher.addURI(AUTHORITY, EQUIPMENT_RESTS_PATH, URI_EQUIPMENT_RESTS)
            sUriMatcher.addURI(AUTHORITY, EQUIPMENT_RESTS_LIST_PATH, URI_EQUIPMENT_RESTS_LIST)

            sUriMatcher.addURI(AUTHORITY, MTRADE_LOG_PATH, URI_MTRADE_LOG)
            sUriMatcher.addURI(AUTHORITY, PERMISSIONS_REQUESTS_PATH, URI_PERMISSIONS_REQUESTS)

            sUriMatcher.addURI(AUTHORITY, ROUTES_PATH, URI_ROUTES)
            sUriMatcher.addURI(AUTHORITY, ROUTES_LINES_PATH, URI_ROUTES_LINES)
            sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_PATH, URI_ROUTES_DATES)
            sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_PATH + "/#", URI_ROUTES_DATES_ID)
            sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_LIST_PATH, URI_ROUTES_DATES_LIST)
            sUriMatcher.addURI(AUTHORITY, ROUTES_DATES_LIST_PATH + "/#", URI_ROUTES_DATES_LIST_ID)

            sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_DATES_PATH, URI_REAL_ROUTES_DATES)
            sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_DATES_PATH + "/#", URI_REAL_ROUTES_DATES_ID)
            sUriMatcher.addURI(AUTHORITY, REAL_ROUTES_LINES_PATH, URI_REAL_ROUTES_LINES)

            ordersProjectionMap = HashMap<String?, String?>()
            ordersProjectionMap.put("_id", "_id")
            ordersProjectionMap.put("uid", "uid")
            ordersProjectionMap.put("version", "version")
            ordersProjectionMap.put("version_ack", "version_ack")
            ordersProjectionMap.put("versionPDA", "versionPDA")
            ordersProjectionMap.put("versionPDA_ack", "versionPDA_ack")
            ordersProjectionMap.put("id", "id")
            ordersProjectionMap.put("numdoc", "numdoc")
            ordersProjectionMap.put("datedoc", "datedoc")
            ordersProjectionMap.put("client_id", "client_id")
            ordersProjectionMap.put("agreement30_id", "agreement30_id")
            ordersProjectionMap.put("agreement_id", "agreement_id")
            ordersProjectionMap.put("distr_point_id", "distr_point_id")
            ordersProjectionMap.put("comment", "comment")
            ordersProjectionMap.put("comment_closing", "comment_closing")
            ordersProjectionMap.put("comment_payment", "comment_payment")
            ordersProjectionMap.put("closed_not_full", "closed_not_full")
            ordersProjectionMap.put("state", "state")
            ordersProjectionMap.put("curator_id", "curator_id")
            ordersProjectionMap.put("bw", "bw")
            ordersProjectionMap.put("trade_type", "trade_type")
            ordersProjectionMap.put("datecreation", "datecreation")
            ordersProjectionMap.put("datecoord", "datecoord")
            ordersProjectionMap.put("latitude", "latitude")
            ordersProjectionMap.put("longitude", "longitude")
            ordersProjectionMap.put("gpsstate", "gpsstate")
            ordersProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            ordersProjectionMap.put("gpstype", "gpstype")
            ordersProjectionMap.put("accept_coord", "accept_coord")
            ordersProjectionMap.put("dont_need_send", "dont_need_send")
            ordersProjectionMap.put("price_type_id", "price_type_id")
            ordersProjectionMap.put("stock_id", "stock_id")
            ordersProjectionMap.put("sum_doc", "sum_doc")
            ordersProjectionMap.put("sum_shipping", "sum_shipping")
            ordersProjectionMap.put("weight_doc", "weight_doc")
            ordersProjectionMap.put("shipping_type", "shipping_type")
            ordersProjectionMap.put("shipping_time", "shipping_time")
            ordersProjectionMap.put("shipping_begin_time", "shipping_begin_time")
            ordersProjectionMap.put("shipping_end_time", "shipping_end_time")
            ordersProjectionMap.put("simple_discount_id", "simple_discount_id")
            ordersProjectionMap.put("create_client", "create_client")
            ordersProjectionMap.put("create_client_descr", "create_client_descr")
            ordersProjectionMap.put("create_client_surname", "create_client_surname")
            ordersProjectionMap.put("create_client_firstname", "create_client_firstname")
            ordersProjectionMap.put("create_client_lastname", "create_client_lastname")
            //ordersProjectionMap.put("create_client_phone", "create_client_phone");
            ordersProjectionMap.put("place_num", "place_num")
            ordersProjectionMap.put("card_num", "card_num")
            ordersProjectionMap.put("pay_credit", "pay_credit")
            //ordersProjectionMap.put("ticket_m", "ticket_m");
            //ordersProjectionMap.put("ticket_w", "ticket_w");
            //ordersProjectionMap.put("quant_m", "quant_m");
            //ordersProjectionMap.put("quant_w", "quant_w");
            ordersProjectionMap.put("quant_mw", "quant_mw")
            ordersProjectionMap.put("shipping_date", "shipping_date")
            ordersProjectionMap.put("manager_comment", "manager_comment")
            ordersProjectionMap.put("theme_comment", "theme_comment")
            ordersProjectionMap.put("phone_num", "phone_num")
            ordersProjectionMap.put("editing_backup", "editing_backup")
            ordersProjectionMap.put("old_id", "old_id")
            ordersProjectionMap.put("color", ORDERS_COLOR_COLUMN)

            cashPaymentsProjectionMap = HashMap<String?, String?>()
            cashPaymentsProjectionMap.put("_id", "_id")
            cashPaymentsProjectionMap.put("uid", "uid")
            cashPaymentsProjectionMap.put("version", "version")
            cashPaymentsProjectionMap.put("version_ack", "version_ack")
            cashPaymentsProjectionMap.put("versionPDA", "versionPDA")
            cashPaymentsProjectionMap.put("versionPDA_ack", "versionPDA_ack")
            cashPaymentsProjectionMap.put("id", "id")
            cashPaymentsProjectionMap.put("numdoc", "numdoc")
            cashPaymentsProjectionMap.put("datedoc", "datedoc")
            cashPaymentsProjectionMap.put("client_id", "client_id")
            cashPaymentsProjectionMap.put("agreement_id", "agreement_id")
            cashPaymentsProjectionMap.put("comment", "comment")
            cashPaymentsProjectionMap.put("comment_closing", "comment_closing")
            cashPaymentsProjectionMap.put("state", "state")
            cashPaymentsProjectionMap.put("curator_id", "curator_id")
            cashPaymentsProjectionMap.put("manager_descr", "manager_descr")
            cashPaymentsProjectionMap.put("organization_descr", "organization_descr")
            cashPaymentsProjectionMap.put("vicarious_power_id", "vicarious_power_id")
            cashPaymentsProjectionMap.put("vicarious_power_descr", "vicarious_power_descr")
            cashPaymentsProjectionMap.put("sum_doc", "sum_doc")
            cashPaymentsProjectionMap.put("datecreation", "datecreation")
            cashPaymentsProjectionMap.put("datecoord", "datecoord")
            cashPaymentsProjectionMap.put("latitude", "latitude")
            cashPaymentsProjectionMap.put("longitude", "longitude")
            cashPaymentsProjectionMap.put("gpsstate", "gpsstate")
            cashPaymentsProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            cashPaymentsProjectionMap.put("gpstype", "gpstype")
            cashPaymentsProjectionMap.put("accept_coord", "accept_coord")
            cashPaymentsProjectionMap.put("distr_point_id", "distr_point_id")
            cashPaymentsProjectionMap.put("color", CASH_PAYMENTS_COLOR_COLUMN)

            refundsProjectionMap = HashMap<String?, String?>()
            refundsProjectionMap.put("_id", "_id")
            refundsProjectionMap.put("uid", "uid")
            refundsProjectionMap.put("version", "version")
            refundsProjectionMap.put("version_ack", "version_ack")
            refundsProjectionMap.put("versionPDA", "versionPDA")
            refundsProjectionMap.put("versionPDA_ack", "versionPDA_ack")
            refundsProjectionMap.put("id", "id")
            refundsProjectionMap.put("numdoc", "numdoc")
            refundsProjectionMap.put("datedoc", "datedoc")
            refundsProjectionMap.put("client_id", "client_id")
            refundsProjectionMap.put("agreement_id", "agreement_id")
            refundsProjectionMap.put("distr_point_id", "distr_point_id")
            refundsProjectionMap.put("comment", "comment")
            refundsProjectionMap.put("comment_closing", "comment_closing")
            refundsProjectionMap.put("closed_not_full", "closed_not_full")
            refundsProjectionMap.put("state", "state")
            refundsProjectionMap.put("curator_id", "curator_id")
            refundsProjectionMap.put("bw", "bw")
            refundsProjectionMap.put("trade_type", "trade_type")
            refundsProjectionMap.put("datecoord", "datecoord")
            refundsProjectionMap.put("latitude", "latitude")
            refundsProjectionMap.put("longitude", "longitude")
            refundsProjectionMap.put("gpsstate", "gpsstate")
            refundsProjectionMap.put("datecreation", "datecreation")
            refundsProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            refundsProjectionMap.put("accept_coord", "accept_coord")
            refundsProjectionMap.put("dont_need_send", "dont_need_send")
            refundsProjectionMap.put("stock_id", "stock_id")
            refundsProjectionMap.put("weight_doc", "weight_doc")
            refundsProjectionMap.put("sum_doc", "sum_doc") // но не используется
            refundsProjectionMap.put("shipping_type", "shipping_type")
            refundsProjectionMap.put("color", REFUNDS_COLOR_COLUMN)

            journalProjectionMap = HashMap<String?, String?>()
            journalProjectionMap.put("_id", "journal._id")
            journalProjectionMap.put("iddocdef", "iddocdef")
            journalProjectionMap.put("order_id", "journal.order_id")
            journalProjectionMap.put("payment_id", "journal.payment_id")
            journalProjectionMap.put("refund_id", "journal.refund_id")
            journalProjectionMap.put("distribs_id", "journal.distribs_id")
            journalProjectionMap.put("uid", "journal.uid")
            journalProjectionMap.put("id", "journal.id")
            journalProjectionMap.put("numdoc", "journal.numdoc")
            journalProjectionMap.put("datedoc", "journal.datedoc")
            journalProjectionMap.put("state", "state")
            journalProjectionMap.put("client_id", "journal.client_id")
            journalProjectionMap.put(
                "client_descr",
                "case when use_client_descr then journal.client_descr else IFNULL(clients.descr, \"{\"||journal.client_id||\"}\") end as client_descr"
            )
            journalProjectionMap.put("sum_doc", "journal.sum_doc")
            journalProjectionMap.put("sum_shipping", "journal.sum_shipping")
            journalProjectionMap.put("color", "journal.color")
            journalProjectionMap.put("shipping_date", "shipping_date")

            clientsProjectionMap = HashMap<String?, String?>()
            clientsProjectionMap.put("_id", "_id")
            clientsProjectionMap.put("id", "id")
            clientsProjectionMap.put("isFolder", "isFolder")
            clientsProjectionMap.put("parent_id", "parent_id")
            clientsProjectionMap.put("code", "code")
            clientsProjectionMap.put("descr", "descr")
            clientsProjectionMap.put("descrFull", "descrFull")
            clientsProjectionMap.put("address", "address")
            clientsProjectionMap.put("address2", "address2")
            clientsProjectionMap.put("comment", "comment")
            clientsProjectionMap.put("curator_id", "curator_id")
            clientsProjectionMap.put("priceType", "priceType")
            clientsProjectionMap.put("blocked", "blocked")
            clientsProjectionMap.put("flags", "flags")
            clientsProjectionMap.put("phone_num", "phone_num")
            clientsProjectionMap.put("email_for_cheques", "email_for_cheques")
            clientsProjectionMap.put("card_num", "card_num")
            clientsProjectionMap.put("isUsed", "isUsed")

            nomenclatureProjectionMap = HashMap<String?, String?>()
            nomenclatureProjectionMap.put("_id", "_id")
            nomenclatureProjectionMap.put("id", "id")
            nomenclatureProjectionMap.put("isFolder", "isFolder")
            nomenclatureProjectionMap.put("parent_id", "parent_id")
            nomenclatureProjectionMap.put("code", "code")
            nomenclatureProjectionMap.put("descr", "descr")
            nomenclatureProjectionMap.put("descrFull", "descrFull")
            nomenclatureProjectionMap.put("quant_1", "quant_1")
            nomenclatureProjectionMap.put("quant_2", "quant_2")
            nomenclatureProjectionMap.put("edizm_1_id", "edizm_1_id")
            nomenclatureProjectionMap.put("edizm_2_id", "edizm_2_id")
            nomenclatureProjectionMap.put("quant_k_1", "quant_k_1")
            nomenclatureProjectionMap.put("quant_k_2", "quant_k_2")
            nomenclatureProjectionMap.put("opt_price", "opt_price")
            nomenclatureProjectionMap.put("m_opt_price", "m_opt_price")
            nomenclatureProjectionMap.put("rozn_price", "rozn_price")
            nomenclatureProjectionMap.put("incom_price", "incom_price")
            nomenclatureProjectionMap.put("IsInPrice", "IsInPrice")
            nomenclatureProjectionMap.put("flagWithoutDiscont", "flagWithoutDiscont")
            nomenclatureProjectionMap.put("weight_k_1", "weight_k_1")
            nomenclatureProjectionMap.put("weight_k_2", "weight_k_2")
            nomenclatureProjectionMap.put("min_quantity", "min_quantity")
            nomenclatureProjectionMap.put("multiplicity", "multiplicity")
            nomenclatureProjectionMap.put("required_sales", "required_sales")
            nomenclatureProjectionMap.put("image_file", "image_file")
            nomenclatureProjectionMap.put("image_file_checksum", "image_file_checksum")
            nomenclatureProjectionMap.put("discount", "discount")
            nomenclatureProjectionMap.put("flags", "flags")
            nomenclatureProjectionMap.put("nomenclature_color", "nomenclature_color")
            nomenclatureProjectionMap.put("order_for_sorting", "order_for_sorting")
            nomenclatureProjectionMap.put("isUsed", "isUsed")
            nomenclatureProjectionMap.put("image_width", "image_width")
            nomenclatureProjectionMap.put("image_height", "image_height")
            nomenclatureProjectionMap.put("image_file_size", "image_file_size")
            nomenclatureProjectionMap.put("compose_with", "compose_with")

            // Номенклатура
            nomenclatureListProjectionMap = HashMap<String?, String?>()
            // Номенклатура, в которой также есть корневой (элемент, с пустым Id, он находится только в hierarchy и там full join)
            nomenclatureSurfingProjectionMap = HashMap<String?, String?>()

            var i: Int
            i = 0
            while (i < 2) {
                val tempNomenclatureListProjectionMap: HashMap<String?, String?> =
                    (if (i == 0) nomenclatureListProjectionMap else nomenclatureSurfingProjectionMap)
                // Номенклатура
                tempNomenclatureListProjectionMap.put("_id", "nomenclature._id")
                tempNomenclatureListProjectionMap.put(
                    "nomenclature_id",
                    if (i == 0) NOMENCLATURE_ID_COLUMN else NOMENCLATURE_ID_SURFING_COLUMN
                )
                tempNomenclatureListProjectionMap.put("isFolder", "isFolder")
                tempNomenclatureListProjectionMap.put("parent_id", "parent_id")
                tempNomenclatureListProjectionMap.put("h_groupDescr", NOMENCLATURE_GROUP_COLUMN)
                tempNomenclatureListProjectionMap.put("groupDescr", "groupDescr")
                tempNomenclatureListProjectionMap.put(
                    "descr",
                    if (i == 0) "descr" else NOMENCLATURE_DESCR_SURFING_COLUMN
                )
                tempNomenclatureListProjectionMap.put("quant_1", "quant_1")
                tempNomenclatureListProjectionMap.put("quant_2", "quant_2")
                tempNomenclatureListProjectionMap.put("edizm_1_id", "edizm_1_id")
                tempNomenclatureListProjectionMap.put("edizm_2_id", "edizm_2_id")
                tempNomenclatureListProjectionMap.put("quant_k_1", "quant_k_1")
                tempNomenclatureListProjectionMap.put("quant_k_2", "quant_k_2")
                tempNomenclatureListProjectionMap.put("required_sales", "required_sales")
                tempNomenclatureListProjectionMap.put("image_file", "image_file")
                tempNomenclatureListProjectionMap.put("image_file_checksum", "image_file_checksum")
                tempNomenclatureListProjectionMap.put("discount", "discount")
                tempNomenclatureListProjectionMap.put("flags", "flags")
                tempNomenclatureListProjectionMap.put("nomenclature_color", "nomenclature_color")
                tempNomenclatureListProjectionMap.put("compose_with", "compose_with")

                tempNomenclatureListProjectionMap.put("image_width", "image_width")
                tempNomenclatureListProjectionMap.put("image_height", "image_height")

                tempNomenclatureListProjectionMap.put("quantity7_1", "quantity7_1")
                tempNomenclatureListProjectionMap.put("quantity7_2", "quantity7_2")
                tempNomenclatureListProjectionMap.put("quantity7_3", "quantity7_3")
                tempNomenclatureListProjectionMap.put("quantity7_4", "quantity7_4")

                // Прайс
                tempNomenclatureListProjectionMap.put("price", "price")
                tempNomenclatureListProjectionMap.put("k", "k")
                tempNomenclatureListProjectionMap.put("edIzm", "edIzm")
                // Наценки по клиентам (в 7-ке справочник СкидкиПоКлиентам)
                tempNomenclatureListProjectionMap.put(
                    "priceAdd",
                    "ifnull(d_s.priceAdd, d_s_p.priceAdd) as priceAdd"
                )
                tempNomenclatureListProjectionMap.put(
                    "priceProcent",
                    "ifnull(d_s.priceProcent, d_s_p.priceProcent) as priceProcent"
                )
                // Остатки
                tempNomenclatureListProjectionMap.put(
                    "nom_quantity",
                    NOMENCLATURE_REST_STOCK_COLUMN
                )
                // Продажи
                tempNomenclatureListProjectionMap.put(
                    "quantity_saled",
                    "strQuantity as quantity_saled"
                )
                // Продажи текущие
                tempNomenclatureListProjectionMap.put(
                    "nom_quantity_saled_now",
                    "saledNow as nom_quantity_saled_now"
                )
                // Дополнительно
                tempNomenclatureListProjectionMap.put("zero", DUMMY_COLUMN)
                i++
            }


            //
            nomenclatureHierarchyProjectionMap = HashMap<String?, String?>()
            nomenclatureHierarchyProjectionMap.put("id", "id")
            nomenclatureHierarchyProjectionMap.put("ord_idx", "ord_idx")
            nomenclatureHierarchyProjectionMap.put("groupDescr", "groupDescr")
            nomenclatureHierarchyProjectionMap.put("level", "level")
            nomenclatureHierarchyProjectionMap.put("level0_id", "level0_id")
            nomenclatureHierarchyProjectionMap.put("level1_id", "level1_id")
            nomenclatureHierarchyProjectionMap.put("level2_id", "level2_id")
            nomenclatureHierarchyProjectionMap.put("level3_id", "level3_id")
            nomenclatureHierarchyProjectionMap.put("level4_id", "level4_id")
            nomenclatureHierarchyProjectionMap.put("level5_id", "level5_id")
            nomenclatureHierarchyProjectionMap.put("level6_id", "level6_id")
            nomenclatureHierarchyProjectionMap.put("level7_id", "level7_id")
            nomenclatureHierarchyProjectionMap.put("level8_id", "level8_id")
            nomenclatureHierarchyProjectionMap.put("dont_use_in_hierarchy", "dont_use_in_hierarchy")


            restsProjectionMap = HashMap<String?, String?>()
            restsProjectionMap.put("stock_id", "stock_id")
            restsProjectionMap.put("nomenclature_id", "nomenclature_id")
            restsProjectionMap.put("organization_id", "organization_id")
            restsProjectionMap.put("quantity", "quantity")
            restsProjectionMap.put("quantity_reserve", "quantity_reserve")

            saldoProjectionMap = HashMap<String?, String?>()
            saldoProjectionMap.put("_id", "_id")
            saldoProjectionMap.put("client_id", "client_id")
            saldoProjectionMap.put("saldo", "saldo")
            saldoProjectionMap.put("saldo_past", "saldo_past")
            saldoProjectionMap.put("saldo_past30", "saldo_past30")

            saldoExtendedProjectionMap = HashMap<String?, String?>()
            saldoExtendedProjectionMap.put("_id", "_id")
            saldoExtendedProjectionMap.put("client_id", "client_id")
            saldoExtendedProjectionMap.put("agreement_id", "agreement_id")
            saldoExtendedProjectionMap.put("document_id", "document_id")
            saldoExtendedProjectionMap.put("manager_id", "manager_id")
            saldoExtendedProjectionMap.put("document_descr", "document_descr")
            saldoExtendedProjectionMap.put("manager_descr", "manager_descr")
            saldoExtendedProjectionMap.put("agreement_descr", "agreement_descr")
            saldoExtendedProjectionMap.put("organization_descr", "organization_descr")
            saldoExtendedProjectionMap.put("saldo", "saldo")
            saldoExtendedProjectionMap.put("saldo_past", "saldo_past")
            saldoExtendedProjectionMap.put("saldo_past30", "saldo_past30")

            saldoExtendedJournalProjectionMap = HashMap<String?, String?>()
            saldoExtendedJournalProjectionMap.put("_id", "_id")
            saldoExtendedJournalProjectionMap.put("client_id", "client_id")
            saldoExtendedJournalProjectionMap.put("agreement_id", "agreement_id")
            saldoExtendedJournalProjectionMap.put("document_id", "document_id")
            saldoExtendedJournalProjectionMap.put("manager_id", "manager_id")
            saldoExtendedJournalProjectionMap.put("document_descr", "document_descr")
            saldoExtendedJournalProjectionMap.put("manager_descr", "manager_descr")
            saldoExtendedJournalProjectionMap.put("saldo", "saldo")
            saldoExtendedJournalProjectionMap.put("saldo_past", "saldo_past")
            saldoExtendedJournalProjectionMap.put("saldo_past30", "saldo_past30")
            saldoExtendedJournalProjectionMap.put("client_descr", SALDO_CLIENT_DESCR_COLUMN)
            saldoExtendedJournalProjectionMap.put("agreement_descr", SALDO_AGREEMENT_DESCR_COLUMN)
            saldoExtendedJournalProjectionMap.put("organization_descr", "organization_descr")


            agreementsProjectionMap = HashMap<String?, String?>()
            agreementsProjectionMap.put("_id", "_id")
            agreementsProjectionMap.put("id", "id")
            agreementsProjectionMap.put("owner_id", "owner_id")
            agreementsProjectionMap.put("organization_id", "organization_id")
            agreementsProjectionMap.put("default_manager_id", "default_manager_id")
            agreementsProjectionMap.put("code", "code")
            agreementsProjectionMap.put("descr", "descr")
            agreementsProjectionMap.put("price_type_id", "price_type_id")
            agreementsProjectionMap.put("sale_id", "sale_id")
            agreementsProjectionMap.put("kredit_days", "kredit_days")
            agreementsProjectionMap.put("kredit_sum", "kredit_sum")
            agreementsProjectionMap.put("flags", "flags")

            agreements30ProjectionMap = HashMap<String?, String?>()
            agreements30ProjectionMap.put("_id", "_id")
            agreements30ProjectionMap.put("id", "id")
            agreements30ProjectionMap.put("owner_id", "owner_id")
            agreements30ProjectionMap.put("organization_id", "organization_id")
            agreements30ProjectionMap.put("default_manager_id", "default_manager_id")
            agreements30ProjectionMap.put("code", "code")
            agreements30ProjectionMap.put("descr", "descr")
            agreements30ProjectionMap.put("price_type_id", "price_type_id")
            agreements30ProjectionMap.put("sale_id", "sale_id")
            agreements30ProjectionMap.put("kredit_days", "kredit_days")
            agreements30ProjectionMap.put("kredit_sum", "kredit_sum")
            agreements30ProjectionMap.put("flags", "flags")

            versionsProjectionMap = HashMap<String?, String?>()
            versionsProjectionMap.put("param", "param")
            versionsProjectionMap.put("ver", "ver")

            pricetypesProjectionMap = HashMap<String?, String?>()
            pricetypesProjectionMap.put("_id", "_id")
            pricetypesProjectionMap.put("id", "id")
            pricetypesProjectionMap.put("isFolder", "isFolder")
            pricetypesProjectionMap.put("code", "code")
            pricetypesProjectionMap.put("descr", "descr")
            pricetypesProjectionMap.put("isUsed", "isUsed")

            stocksProjectionMap = HashMap<String?, String?>()
            stocksProjectionMap.put("_id", "_id")
            stocksProjectionMap.put("id", "id")
            stocksProjectionMap.put("isFolder", "isFolder")
            stocksProjectionMap.put("code", "code")
            stocksProjectionMap.put("descr", "descr")
            stocksProjectionMap.put("flags", "flags")

            pricesProjectionMap = HashMap<String?, String?>()
            pricesProjectionMap.put("_id", "_id")
            pricesProjectionMap.put("nomenclature_id", "nomenclature_id")
            pricesProjectionMap.put("price_type_id", "price_type_id")
            pricesProjectionMap.put("ed_izm_id", "ed_izm_id")
            pricesProjectionMap.put("edIzm", "edIzm")
            pricesProjectionMap.put("price", "price")
            pricesProjectionMap.put("priceProcent", "priceProcent")
            pricesProjectionMap.put("k", "k")
            pricesProjectionMap.put("isUsed", "isUsed")

            pricesAgreements30ProjectionMap = HashMap<String?, String?>()
            pricesAgreements30ProjectionMap.put("_id", "_id")
            pricesAgreements30ProjectionMap.put("agreement30_id", "agreement30_id")
            pricesAgreements30ProjectionMap.put("nomenclature_id", "nomenclature_id")
            pricesAgreements30ProjectionMap.put("pack_id", "pack_id")
            pricesAgreements30ProjectionMap.put("ed_izm_id", "ed_izm_id")
            pricesAgreements30ProjectionMap.put("edIzm", "edIzm")
            pricesAgreements30ProjectionMap.put("price", "price")
            pricesAgreements30ProjectionMap.put("k", "k")

            agentsProjectionMap = HashMap<String?, String?>()
            agentsProjectionMap.put("_id", "_id")
            agentsProjectionMap.put("id", "id")
            agentsProjectionMap.put("code", "code")
            agentsProjectionMap.put("descr", "descr")

            curatorsProjectionMap = HashMap<String?, String?>()
            curatorsProjectionMap.put("_id", "_id")
            curatorsProjectionMap.put("id", "id")
            curatorsProjectionMap.put("isFolder", "isFolder")
            curatorsProjectionMap.put("parent_id", "parent_id")
            curatorsProjectionMap.put("code", "code")
            curatorsProjectionMap.put("descr", "descr")

            curatorsListProjectionMap = HashMap<String?, String?>()
            /*
        curatorsListProjectionMap.put("_id", "curators._id");
        curatorsListProjectionMap.put("id", "ifnull(curators.id,sd.manager_id) as id");
        curatorsListProjectionMap.put("isFolder", "ifnull(curators.isFolder,2) as isFolder");
        curatorsListProjectionMap.put("parent_id", "curators.parent_id");
        curatorsListProjectionMap.put("code", "ifnull(curators.code,'') as code");
        curatorsListProjectionMap.put("descr", "ifnull(curators.descr,id.manager_descr) as descr");
        */
            curatorsListProjectionMap.put("_id", "_id")
            curatorsListProjectionMap.put("id", "id")
            curatorsListProjectionMap.put("isFolder", "isFolder")
            curatorsListProjectionMap.put("parent_id", "parent_id")
            curatorsListProjectionMap.put("client_id", "client_id")
            curatorsListProjectionMap.put("code", "code")
            curatorsListProjectionMap.put("descr", "descr")

            distrPointsProjectionMap = HashMap<String?, String?>()
            distrPointsProjectionMap.put("_id", "_id")
            distrPointsProjectionMap.put("id", "id")
            distrPointsProjectionMap.put("owner_id", "owner_id")
            distrPointsProjectionMap.put("code", "code")
            distrPointsProjectionMap.put("descr", "descr")
            distrPointsProjectionMap.put("address", "address")
            distrPointsProjectionMap.put("phones", "phones")
            distrPointsProjectionMap.put("conacts", "conacts")
            distrPointsProjectionMap.put("price_type_id", "price_type_id")

            distrPointsListProjectionMap = HashMap<String?, String?>()
            distrPointsListProjectionMap.put("_id", "distr_points._id")
            distrPointsListProjectionMap.put("id", "distr_points.id")
            distrPointsListProjectionMap.put("owner_id", "owner_id")
            distrPointsListProjectionMap.put("code", "distr_points.code")
            distrPointsListProjectionMap.put("descr", "distr_points.descr")
            distrPointsListProjectionMap.put("address", "address")
            distrPointsListProjectionMap.put("phones", "phones")
            distrPointsListProjectionMap.put("conacts", "conacts")
            distrPointsListProjectionMap.put("price_type_id", "price_type_id")
            distrPointsListProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN)

            organizationsProjectionMap = HashMap<String?, String?>()
            organizationsProjectionMap.put("_id", "_id")
            organizationsProjectionMap.put("id", "id")
            organizationsProjectionMap.put("code", "code")
            organizationsProjectionMap.put("descr", "descr")

            clientsWithSaldoProjectionMap = HashMap<String?, String?>()
            clientsWithSaldoProjectionMap.put("_id", "clients._id")
            clientsWithSaldoProjectionMap.put("id", "clients.id")
            clientsWithSaldoProjectionMap.put("isFolder", "isFolder")
            clientsWithSaldoProjectionMap.put("parent_id", "parent_id")
            clientsWithSaldoProjectionMap.put("descr", "descr")
            clientsWithSaldoProjectionMap.put("address", "address")
            clientsWithSaldoProjectionMap.put("saldo", "saldo")
            clientsWithSaldoProjectionMap.put("saldo_past", "saldo_past")
            clientsWithSaldoProjectionMap.put("saldo_past30", "saldo_past30")
            clientsWithSaldoProjectionMap.put("blocked", "blocked")
            clientsWithSaldoProjectionMap.put("flags", "flags")
            clientsWithSaldoProjectionMap.put("email_for_cheques", "email_for_cheques")

            ordersLinesProjectionMap = HashMap<String?, String?>()
            ordersLinesProjectionMap.put("_id", "_id")
            ordersLinesProjectionMap.put("order_id", "order_id")
            ordersLinesProjectionMap.put("nomenclature_id", "nomenclature_id")
            ordersLinesProjectionMap.put("quantity_requested", "quantity_requested")
            ordersLinesProjectionMap.put("quantity", "quantity")
            ordersLinesProjectionMap.put("price", "price")
            ordersLinesProjectionMap.put("total", "total")
            ordersLinesProjectionMap.put("discount", "discount")
            ordersLinesProjectionMap.put("k", "k")
            ordersLinesProjectionMap.put("ed", "ed")
            ordersLinesProjectionMap.put("shipping_time", "shipping_time")
            ordersLinesProjectionMap.put("comment_in_line", "comment_in_line")
            //ordersLinesProjectionMap.put("editing_state", "editing_state");
            ordersLinesProjectionMap.put("lineno", "lineno")


            refundsLinesProjectionMap = HashMap<String?, String?>()
            refundsLinesProjectionMap.put("_id", "_id")
            refundsLinesProjectionMap.put("refund_id", "refund_id")
            refundsLinesProjectionMap.put("nomenclature_id", "nomenclature_id")
            refundsLinesProjectionMap.put("quantity_requested", "quantity_requested")
            refundsLinesProjectionMap.put("quantity", "quantity")
            //refundsLinesProjectionMap.put("price", "price");
            //refundsLinesProjectionMap.put("total", "total");
            //refundsLinesProjectionMap.put("discount", "discount");
            refundsLinesProjectionMap.put("k", "k")
            refundsLinesProjectionMap.put("ed", "ed")
            refundsLinesProjectionMap.put("comment_in_line", "comment_in_line")

            ordersJournalProjectionMap = HashMap<String?, String?>()
            ordersJournalProjectionMap.put("_id", "orders._id")
            ordersJournalProjectionMap.put("numdoc", "numdoc")
            ordersJournalProjectionMap.put("datedoc", "datedoc")
            ordersJournalProjectionMap.put("state", "state")
            ordersJournalProjectionMap.put("client_descr", CLIENT_DESCR_COLUMN)
            ordersJournalProjectionMap.put("client_id", "client_id")
            ordersJournalProjectionMap.put("sum_doc", "sum_doc")
            ordersJournalProjectionMap.put("color", ORDERS_COLOR_COLUMN)

            cashPaymentsJournalProjectionMap = HashMap<String?, String?>()
            cashPaymentsJournalProjectionMap.put("_id", "cash_payments._id")
            cashPaymentsJournalProjectionMap.put("numdoc", "numdoc")
            cashPaymentsJournalProjectionMap.put("datedoc", "datedoc")
            cashPaymentsJournalProjectionMap.put("state", "state")
            cashPaymentsJournalProjectionMap.put("client_descr", CASH_PAYMENTS_CLIENT_DESCR_COLUMN)
            cashPaymentsJournalProjectionMap.put("client_id", "client_id")
            cashPaymentsJournalProjectionMap.put("sum_doc", "sum_doc")
            cashPaymentsJournalProjectionMap.put("color", CASH_PAYMENTS_COLOR_COLUMN)
            cashPaymentsJournalProjectionMap.put("manager_descr", "manager_descr")
            cashPaymentsJournalProjectionMap.put("zero", DUMMY_COLUMN)

            agreementsListProjectionMap = HashMap<String?, String?>()
            agreementsListProjectionMap.put("_id", "agreements._id")
            agreementsListProjectionMap.put("agreement_id", "agreements.id as agreement_id")
            agreementsListProjectionMap.put("owner_id", "owner_id")
            agreementsListProjectionMap.put("agreement_descr", AGREEMENT_DESCR_COLUMN)
            agreementsListProjectionMap.put("organization_descr", ORGANIZATION_DESCR_COLUMN)
            agreementsListProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN)
            agreementsListProjectionMap.put("default_manager_id", "default_manager_id")

            agreementsListWithSaldoOnlyProjectionMap = HashMap<String?, String?>()
            agreementsListWithSaldoOnlyProjectionMap.put("_id", "agreements._id")
            agreementsListWithSaldoOnlyProjectionMap.put(
                "agreement_id",
                "agreements.id as agreement_id"
            )
            agreementsListWithSaldoOnlyProjectionMap.put("owner_id", "owner_id")
            agreementsListWithSaldoOnlyProjectionMap.put("agreement_descr", AGREEMENT_DESCR_COLUMN)
            agreementsListWithSaldoOnlyProjectionMap.put(
                "organization_descr",
                ORGANIZATION_DESCR_COLUMN
            )
            agreementsListWithSaldoOnlyProjectionMap.put("pricetype_descr", PRICE_TYPE_DESCR_COLUMN)
            agreementsListWithSaldoOnlyProjectionMap.put("default_manager_id", "default_manager_id")
            agreementsListWithSaldoOnlyProjectionMap.put(
                "default_manager_descr",
                "\"\" as default_manager_descr"
            )
            agreementsListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo")
            agreementsListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo")
            agreementsListWithSaldoOnlyProjectionMap.put(
                "saldo_past",
                "saldo0.saldo_past as saldo_past"
            )

            agreements30ListWithSaldoOnlyProjectionMap = HashMap<String?, String?>()
            agreements30ListWithSaldoOnlyProjectionMap.put("_id", "agreements30._id")
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "agreement_id",
                "agreements30.id as agreement_id"
            )
            agreements30ListWithSaldoOnlyProjectionMap.put("owner_id", "owner_id")
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "agreement_descr",
                AGREEMENT30_DESCR_COLUMN
            )
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "organization_descr",
                ORGANIZATION_DESCR_COLUMN
            )
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "pricetype_descr",
                PRICE_TYPE_DESCR_COLUMN
            )
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "default_manager_id",
                "default_manager_id"
            )
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "default_manager_descr",
                "\"\" as default_manager_descr"
            )
            agreements30ListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo")
            agreements30ListWithSaldoOnlyProjectionMap.put("saldo", "saldo0.saldo as saldo")
            agreements30ListWithSaldoOnlyProjectionMap.put(
                "saldo_past",
                "saldo0.saldo_past as saldo_past"
            )

            ordersLinesComplementedProjectionMap = HashMap<String?, String?>()
            ordersLinesComplementedProjectionMap.put("_id", "ordersLines._id")
            ordersLinesComplementedProjectionMap.put("order_id", "order_id")
            ordersLinesComplementedProjectionMap.put("nomenclature_id", "nomenclature_id")
            ordersLinesComplementedProjectionMap.put("quantity_requested", "quantity_requested")
            ordersLinesComplementedProjectionMap.put("quantity", "quantity")
            ordersLinesComplementedProjectionMap.put("price", "price")
            ordersLinesComplementedProjectionMap.put("total", "total")
            ordersLinesComplementedProjectionMap.put("discount", "discount")
            ordersLinesComplementedProjectionMap.put("weight_k_1", NOMENCLATURE_WEIGHT_COLUMN)
            ordersLinesComplementedProjectionMap.put("k", "k")
            ordersLinesComplementedProjectionMap.put("ed", "ed")
            ordersLinesComplementedProjectionMap.put("nomencl_descr", NOMENCLATURE_DESCR_COLUMN)
            //ordersLinesComplementedProjectionMap.put("nomencl_node_id", NOMENCLATURE_NODE_ID_COLUMN);
            ordersLinesComplementedProjectionMap.put("flags", "flags")
            ordersLinesComplementedProjectionMap.put("shipping_time", "shipping_time")
            ordersLinesComplementedProjectionMap.put("comment_in_line", "comment_in_line")
            ordersLinesComplementedProjectionMap.put("lineno", "lineno")

            refundsLinesComplementedProjectionMap = HashMap<String?, String?>()
            refundsLinesComplementedProjectionMap.put("_id", "refundsLines._id")
            refundsLinesComplementedProjectionMap.put("refund_id", "refund_id")
            refundsLinesComplementedProjectionMap.put("nomenclature_id", "nomenclature_id")
            refundsLinesComplementedProjectionMap.put("quantity_requested", "quantity_requested")
            refundsLinesComplementedProjectionMap.put("quantity", "quantity")
            //refundsLinesComplementedProjectionMap.put("price", "price");
            //refundsLinesComplementedProjectionMap.put("total", "total");
            refundsLinesComplementedProjectionMap.put("weight_k_1", NOMENCLATURE_WEIGHT_COLUMN)
            refundsLinesComplementedProjectionMap.put("k", "k")
            refundsLinesComplementedProjectionMap.put("ed", "ed")
            refundsLinesComplementedProjectionMap.put("nomencl_descr", NOMENCLATURE_DESCR_COLUMN)
            refundsLinesComplementedProjectionMap.put("flags", "flags")
            refundsLinesComplementedProjectionMap.put("comment_in_line", "comment_in_line")


            clientsPriceProjectionMap = HashMap<String?, String?>()
            clientsPriceProjectionMap.put("_id", "_id")
            clientsPriceProjectionMap.put("client_id", "client_id")
            clientsPriceProjectionMap.put("nomenclature_id", "nomenclature_id")
            clientsPriceProjectionMap.put("priceAdd", "priceAdd")
            clientsPriceProjectionMap.put("priceProcent", "priceProcent")

            curatorsPriceProjectionMap = HashMap<String?, String?>()
            curatorsPriceProjectionMap.put("_id", "_id")
            curatorsPriceProjectionMap.put("curator_id", "curator_id")
            curatorsPriceProjectionMap.put("nomenclature_id", "nomenclature_id")
            curatorsPriceProjectionMap.put("priceAdd", "priceAdd")
            curatorsPriceProjectionMap.put("priceProcent", "priceProcent")

            simpleDiscountsProjectionMap = HashMap<String?, String?>()
            simpleDiscountsProjectionMap.put("_id", "_id")
            simpleDiscountsProjectionMap.put("id", "id")
            simpleDiscountsProjectionMap.put("code", "code")
            simpleDiscountsProjectionMap.put("descr", "descr")
            simpleDiscountsProjectionMap.put("priceProcent", "priceProcent")
            simpleDiscountsProjectionMap.put("isUsed", "isUsed")

            seancesProjectionMap = HashMap<String?, String?>()
            seancesProjectionMap.put("incoming", "incoming")
            seancesProjectionMap.put("outgoing", "outgoing")

            messagesProjectionMap = HashMap<String?, String?>()
            messagesProjectionMap.put("_id", "messages._id")
            messagesProjectionMap.put("uid", "uid")
            messagesProjectionMap.put("sender_id", "sender_id")
            messagesProjectionMap.put("receiver_id", "receiver_id")
            messagesProjectionMap.put("text", "text")
            messagesProjectionMap.put("fname", "fname")
            messagesProjectionMap.put("datetime", "datetime")
            messagesProjectionMap.put("acknowledged", "acknowledged")
            messagesProjectionMap.put("ver", "ver")
            messagesProjectionMap.put("type_idx", "type_idx")
            messagesProjectionMap.put("date1", "date1")
            messagesProjectionMap.put("date2", "date2")
            messagesProjectionMap.put("client_id", "client_id")
            messagesProjectionMap.put("agreement_id", "agreement_id")
            messagesProjectionMap.put("nomenclature_id", "nomenclature_id")
            messagesProjectionMap.put("report", "report")
            messagesProjectionMap.put("isMark", "isMark")
            messagesProjectionMap.put("isMarkCnt", "isMarkCnt")

            messagesListProjectionMap = HashMap<String?, String?>()
            messagesListProjectionMap.put("_id", "messages._id")
            messagesListProjectionMap.put("uid", "uid")
            messagesListProjectionMap.put("sender_id", "sender_id")
            messagesListProjectionMap.put("receiver_id", "receiver_id")
            messagesListProjectionMap.put("text", "text")
            messagesListProjectionMap.put("fname", "fname")
            messagesListProjectionMap.put("datetime", "datetime")
            messagesListProjectionMap.put("acknowledged", "acknowledged")
            // 0 - входящие, 1 - исходящие 
            messagesListProjectionMap.put(
                "inout",
                "case when acknowledged&4==0 then 0 else 1 end inout"
            )
            messagesListProjectionMap.put("ver", "ver")
            messagesListProjectionMap.put("type_idx", "type_idx")
            messagesListProjectionMap.put("date1", "date1")
            messagesListProjectionMap.put("date2", "date2")
            messagesListProjectionMap.put("client_id", "client_id")
            messagesListProjectionMap.put("agreement_id", "agreement_id")
            messagesListProjectionMap.put("nomenclature_id", "nomenclature_id")
            //messagesListProjectionMap.put("descr", "ifnull(clients.descr, agents.descr) as descr");
            messagesListProjectionMap.put(
                "descr",
                "case when type_idx in (" + E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value() + "," + E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value() + ") then clients.descr else agents.descr end as descr"
            )
            messagesListProjectionMap.put("zero", DUMMY_COLUMN)

            settingsProjectionMap = HashMap<String?, String?>()
            settingsProjectionMap.put("_id", "_id")
            settingsProjectionMap.put("fmt", "fmt")
            //settingsProjectionMap.put("ticket_m", "ticket_m");
            //settingsProjectionMap.put("ticket_w", "ticket_w");
            settingsProjectionMap.put("agent_id", "agent_id")
            settingsProjectionMap.put("gps_interval", "gps_interval")
            settingsProjectionMap.put("agent_price_type_id", "agent_price_type_id")

            salesLoadedProjectionMap = HashMap<String?, String?>()
            salesLoadedProjectionMap.put("_id", "_id")
            salesLoadedProjectionMap.put("ver", "ver")
            salesLoadedProjectionMap.put("datedoc", "datedoc")
            salesLoadedProjectionMap.put("numdoc", "numdoc")
            salesLoadedProjectionMap.put("refdoc", "refdoc")
            salesLoadedProjectionMap.put("client_id", "client_id")
            salesLoadedProjectionMap.put("curator_id", "curator_id")
            salesLoadedProjectionMap.put("distr_point_id", "distr_point_id")
            salesLoadedProjectionMap.put("nomenclature_id", "nomenclature_id")
            salesLoadedProjectionMap.put("quantity", "quantity")
            salesLoadedProjectionMap.put("price", "price")


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
            salesL2ProjectionMap = HashMap<String?, String?>()
            salesL2ProjectionMap.put("_id", "_id") // из нме
            salesL2ProjectionMap.put("datedoc", "datedoc")
            salesL2ProjectionMap.put("numdoc", "numdoc")
            salesL2ProjectionMap.put("client_id", "client_id")
            salesL2ProjectionMap.put("distr_point_id", "distr_point_id")
            salesL2ProjectionMap.put("nomenclature_id", "nomenclature_id")
            salesL2ProjectionMap.put("quantity", "quantity")
            salesL2ProjectionMap.put("price", "price")

            versionsSalesProjectionMap = HashMap<String?, String?>()
            versionsSalesProjectionMap.put("param", "param")
            versionsSalesProjectionMap.put("ver", "ver")

            hierarchyProjectionMap = HashMap<String?, String?>()
            hierarchyProjectionMap.put("id", "id")
            hierarchyProjectionMap.put("ord_idx", "ord_idx")
            hierarchyProjectionMap.put("groupDescr", "groupDescr")
            hierarchyProjectionMap.put("level", "level")


            placesProjectionMap = HashMap<String?, String?>()
            placesProjectionMap.put("id", "id")
            placesProjectionMap.put("code", "code")
            placesProjectionMap.put("descr", "descr")
            placesProjectionMap.put("isUsed", "isUsed")

            occupiedPlacesProjectionMap = HashMap<String?, String?>()
            occupiedPlacesProjectionMap.put("place_id", "place_id")
            occupiedPlacesProjectionMap.put("client_id", "client_id")
            occupiedPlacesProjectionMap.put("document_id", "document_id")
            occupiedPlacesProjectionMap.put("datedoc", "datedoc")
            occupiedPlacesProjectionMap.put("document", "document")
            occupiedPlacesProjectionMap.put("shipping_date", "shipping_date")
            occupiedPlacesProjectionMap.put("shipping_time", "shipping_time")
            occupiedPlacesProjectionMap.put("isUsed", "isUsed")

            ordersPlacesProjectionMap = HashMap<String?, String?>()
            ordersPlacesProjectionMap.put("order_id", "order_id")
            ordersPlacesProjectionMap.put("place_id", "place_id")

            ordersPlacesListProjectionMap = HashMap<String?, String?>()
            ordersPlacesListProjectionMap.put("_id", "orders._id")
            ordersPlacesListProjectionMap.put("datedoc", "datedoc")
            ordersPlacesListProjectionMap.put("numdoc", "numdoc")
            ordersPlacesListProjectionMap.put("id", "id")
            ordersPlacesListProjectionMap.put("client_id", "client_id")
            ordersPlacesListProjectionMap.put("order_id", "order_id")
            ordersPlacesListProjectionMap.put("place_id", "place_id")
            ordersPlacesListProjectionMap.put("state", "state")
            ordersPlacesListProjectionMap.put("shipping_date", "shipping_date")
            ordersPlacesListProjectionMap.put("shipping_time", "shipping_time")

            vicariousPowerProjectionMap = HashMap<String?, String?>()
            vicariousPowerProjectionMap.put("_id", "_id")
            vicariousPowerProjectionMap.put("id", "id")
            vicariousPowerProjectionMap.put("descr", "descr")
            vicariousPowerProjectionMap.put("numdoc", "numdoc")
            vicariousPowerProjectionMap.put("datedoc", "datedoc")
            vicariousPowerProjectionMap.put("date_action", "date_action")
            vicariousPowerProjectionMap.put("comment", "comment")
            vicariousPowerProjectionMap.put("client_id", "client_id")
            vicariousPowerProjectionMap.put("client_descr", "client_descr")
            vicariousPowerProjectionMap.put("agreement_id", "agreement_descr")
            vicariousPowerProjectionMap.put("fio_descr", "fio_descr")
            vicariousPowerProjectionMap.put("manager_id", "manager_id")
            vicariousPowerProjectionMap.put("manager_descr", "manager_descr")
            vicariousPowerProjectionMap.put("organization_id", "organization_id")
            vicariousPowerProjectionMap.put("organization_descr", "organization_descr")
            vicariousPowerProjectionMap.put("state", "state")
            vicariousPowerProjectionMap.put("sum_doc", "sum_doc")

            gpsCoordProjectionMap = HashMap<String?, String?>()
            gpsCoordProjectionMap.put("datecoord", "datecoord")
            gpsCoordProjectionMap.put("latitude", "latitude")
            gpsCoordProjectionMap.put("longitude", "longitude")
            gpsCoordProjectionMap.put("gpsstate", "gpsstate")
            gpsCoordProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            gpsCoordProjectionMap.put("version", "version")
            gpsCoordProjectionMap.put("gpstype", "gpstype")

            distribsContractsProjectionMap = HashMap<String?, String?>()
            distribsContractsProjectionMap.put("_id", "_id")
            distribsContractsProjectionMap.put("id", "id")
            distribsContractsProjectionMap.put("position", "position")
            distribsContractsProjectionMap.put("code", "code")
            distribsContractsProjectionMap.put("descr", "descr")

            // TODO другие поля
            distribsContractsListProjectionMap = HashMap<String?, String?>()
            distribsContractsListProjectionMap.put("_id", "_id")
            distribsContractsListProjectionMap.put("id", "id")
            distribsContractsListProjectionMap.put("position", "position")
            distribsContractsListProjectionMap.put("code", "code")
            distribsContractsListProjectionMap.put("descr", "descr")

            distribsLinesProjectionMap = HashMap<String?, String?>()
            distribsLinesProjectionMap.put("_id", "_id")
            distribsLinesProjectionMap.put("distribs_id", "distribs_id")
            distribsLinesProjectionMap.put("contract_id", "contract_id")
            distribsLinesProjectionMap.put("quantity", "quantity")

            distribsLinesComplementedProjectionMap = HashMap<String?, String?>()
            distribsLinesComplementedProjectionMap.put("_id", "distribsLines._id")
            distribsLinesComplementedProjectionMap.put("distribs_id", "distribs_id")
            distribsLinesComplementedProjectionMap.put("contract_id", "contract_id")
            distribsLinesComplementedProjectionMap.put("contract_descr", CONTRACT_DESCR_COLUMN)
            distribsLinesComplementedProjectionMap.put("quantity", "quantity")

            distribsProjectionMap = HashMap<String?, String?>()
            distribsProjectionMap.put("_id", "_id")
            distribsProjectionMap.put("uid", "uid")
            distribsProjectionMap.put("version", "version")
            distribsProjectionMap.put("version_ack", "version_ack")
            distribsProjectionMap.put("versionPDA", "versionPDA")
            distribsProjectionMap.put("versionPDA_ack", "versionPDA_ack")
            distribsProjectionMap.put("id", "id")
            distribsProjectionMap.put("numdoc", "numdoc")
            distribsProjectionMap.put("datedoc", "datedoc")
            distribsProjectionMap.put("client_id", "client_id")
            distribsProjectionMap.put("curator_id", "curator_id")
            distribsProjectionMap.put("distr_point_id", "distr_point_id")
            distribsProjectionMap.put("state", "state")
            distribsProjectionMap.put("comment", "comment")
            distribsProjectionMap.put("datecoord", "datecoord")
            distribsProjectionMap.put("latitude", "latitude")
            distribsProjectionMap.put("longitude", "longitude")
            distribsProjectionMap.put("gpsstate", "gpsstate")
            distribsProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            distribsProjectionMap.put("gpstype", "gpstype")
            distribsProjectionMap.put("accept_coord", "accept_coord")
            distribsProjectionMap.put("color", DISTRIBS_COLOR_COLUMN)

            equipmentProjectionMap = HashMap<String?, String?>()
            equipmentProjectionMap.put("_id", "_id")
            equipmentProjectionMap.put("id", "id")
            equipmentProjectionMap.put("code", "code")
            equipmentProjectionMap.put("descr", "descr")
            equipmentProjectionMap.put("flags", "flags")

            equipmentRestsProjectionMap = HashMap<String?, String?>()
            equipmentRestsProjectionMap.put("_id", "_id")
            equipmentRestsProjectionMap.put("client_id", "client_id")
            equipmentRestsProjectionMap.put("agreement_id", "agreement_id")
            equipmentRestsProjectionMap.put("nomenclature_id", "nomenclature_id")
            equipmentRestsProjectionMap.put("distr_point_id", "distr_point_id")
            equipmentRestsProjectionMap.put("quantity", "quantity")
            equipmentRestsProjectionMap.put("sum", "sum")
            equipmentRestsProjectionMap.put("flags", "flags")
            equipmentRestsProjectionMap.put("doc_id", "doc_id")
            equipmentRestsProjectionMap.put("doc_descr", "doc_descr")
            equipmentRestsProjectionMap.put("date", "date")
            equipmentRestsProjectionMap.put("datepast", "datepast")

            equipmentRestsListProjectionMap = HashMap<String?, String?>()
            equipmentRestsListProjectionMap.put("_id", "equipment_rests._id")
            equipmentRestsListProjectionMap.put("client_id", "client_id")
            equipmentRestsListProjectionMap.put("agreement_id", "agreement_id")
            equipmentRestsListProjectionMap.put("nomenclature_id", "nomenclature_id")
            equipmentRestsListProjectionMap.put(
                "nomenclature_descr",
                "IFNULL(equipment.descr, \"{\"||nomenclature_id||\"}\") as nomenclature_descr"
            )
            equipmentRestsListProjectionMap.put("distr_point_id", "distr_point_id")
            equipmentRestsListProjectionMap.put("quantity", "quantity")
            equipmentRestsListProjectionMap.put("sum", "sum")
            equipmentRestsListProjectionMap.put("flags", "equipment_rests.flags as flags")
            equipmentRestsListProjectionMap.put("doc_id", "doc_id")
            equipmentRestsListProjectionMap.put("doc_descr", "doc_descr")
            equipmentRestsListProjectionMap.put("date", "date")
            equipmentRestsListProjectionMap.put("datepast", "datepast")
            equipmentRestsListProjectionMap.put("empty", "\"\" as empty")

            mtradeLogProjectionMap = HashMap<String?, String?>()
            mtradeLogProjectionMap.put("_id", "equipment_rests._id")
            mtradeLogProjectionMap.put("messagetext", "messagetext")
            mtradeLogProjectionMap.put("messagetype", "messagetype")
            mtradeLogProjectionMap.put("version", "version")

            permissionsRequestsProjectionMap = HashMap<String?, String?>()
            permissionsRequestsProjectionMap.put("_id", "_id")
            permissionsRequestsProjectionMap.put("permission_name", "permission_name")
            permissionsRequestsProjectionMap.put("datetime", "datetime")
            permissionsRequestsProjectionMap.put("seance_incoming", "seance_incoming")

            routesProjectionMap = HashMap<String?, String?>()
            routesProjectionMap.put("_id", "_id")
            routesProjectionMap.put("id", "id")
            routesProjectionMap.put("code", "code")
            routesProjectionMap.put("descr", "descr")
            routesProjectionMap.put("manager_id", "manager_id")

            routesLinesProjectionMap = HashMap<String?, String?>()
            routesLinesProjectionMap.put("_id", "_id")
            routesLinesProjectionMap.put("route_id", "route_id")
            routesLinesProjectionMap.put("lineno", "lineno")
            routesLinesProjectionMap.put("distr_point_id", "distr_point_id")
            routesLinesProjectionMap.put("visit_time", "visit_time")

            routesDatesProjectionMap = HashMap<String?, String?>()
            routesDatesProjectionMap.put("_id", "_id")
            routesDatesProjectionMap.put("route_id", "route_id")
            routesDatesProjectionMap.put("route_date", "route_date")

            routesDatesListProjectionMap = HashMap<String?, String?>()
            routesDatesListProjectionMap.put("_id", "routes_dates._id")
            routesDatesListProjectionMap.put("descr", "routes.descr")
            routesDatesListProjectionMap.put("route_id", "route_id")
            routesDatesListProjectionMap.put("route_date", "route_date")


            realRoutesDatesProjectionMap = HashMap<String?, String?>()
            realRoutesDatesProjectionMap.put("_id", "_id")
            realRoutesDatesProjectionMap.put("uid", "uid")
            realRoutesDatesProjectionMap.put("id", "id")
            realRoutesDatesProjectionMap.put("route_date", "route_date")
            realRoutesDatesProjectionMap.put("route_id", "route_id")
            realRoutesDatesProjectionMap.put("route_descr", "route_descr")

            realRoutesLinesProjectionMap = HashMap<String?, String?>()
            realRoutesLinesProjectionMap.put("_id", "_id")
            realRoutesLinesProjectionMap.put("real_route_id", "real_route_id")
            realRoutesLinesProjectionMap.put("lineno", "lineno")
            realRoutesLinesProjectionMap.put("distr_point_id", "distr_point_id")
            realRoutesLinesProjectionMap.put("required_visit_time", "required_visit_time")
            realRoutesLinesProjectionMap.put("start_visit_time", "start_visit_time")
            realRoutesLinesProjectionMap.put("end_visit_time", "end_visit_time")
            realRoutesLinesProjectionMap.put("version", "version")
            realRoutesLinesProjectionMap.put("version_ack", "version_ack")
            realRoutesLinesProjectionMap.put("versionPDA", "versionPDA")
            realRoutesLinesProjectionMap.put("versionPDA_ack", "versionPDA_ack")
            realRoutesLinesProjectionMap.put("datecoord", "datecoord")
            realRoutesLinesProjectionMap.put("latitude", "latitude")
            realRoutesLinesProjectionMap.put("longitude", "longitude")
            realRoutesLinesProjectionMap.put("gpsstate", "gpsstate")
            realRoutesLinesProjectionMap.put("gpsaccuracy", "gpsaccuracy")
            realRoutesLinesProjectionMap.put("accept_coord", "accept_coord")
        }
    }
}

