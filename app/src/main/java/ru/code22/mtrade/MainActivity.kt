package ru.code22.mtrade

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.BitmapFactory
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.nostra13.universalimageloader.utils.L
import org.apache.commons.io.filefilter.WildcardFileFilter
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.net.io.CopyStreamAdapter
import ru.code22.mtrade.MyDatabase.DistribsLineRecord
import ru.code22.mtrade.MyDatabase.MyID
import ru.code22.mtrade.MyDatabase.OrderRecord
import ru.code22.mtrade.MyDatabase.RefundLineRecord
import ru.code22.mtrade.TextDatabase.XMLMode
import ru.code22.mtrade.preferences.DatePreference
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.SocketException
import java.text.DateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import java.util.zip.CRC32
import java.util.zip.Checksum
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.text.StringBuilder
import kotlin.text.charset
import kotlin.text.endsWith
import kotlin.text.equals
import kotlin.text.format
import kotlin.text.indexOf
import kotlin.text.isEmpty
import kotlin.text.lastIndexOf
import kotlin.text.lowercase
import kotlin.text.replace
import kotlin.text.substring
import kotlin.text.toInt
import kotlin.text.trim

// TODO 25.06.2024 почитать
// https://blog.mindorks.com/implementing-android-jetpack-preferences/
// TODO отсюда можно отправлять уведомления FCM
// https://console.firebase.google.com/project/ru-code22-mtrade/notification
// документация
// https://firebase.google.com/docs/cloud-messaging
// getting started
// https://firebase.google.com/docs/android/setup?authuser=0
// http://idev.by/category/android/page/4/
// 87.241.235.54, 002
// выкачивать тестерам приложение можно отсюда
// https://play.google.com/apps/testing/ru.code22.mtrade
// выкладывать так
//https://play.google.com/apps/publish/internalappsharing/
/*
320dp: a typical phone screen (240x320 ldpi, 320x480 mdpi, 480x800 hdpi, etc).
480dp: a tweener tablet like the Streak (480x800 mdpi).
600dp: a 7” tablet (600x1024 mdpi).
720dp: a 10” tablet (720x1280 mdpi, 800x1280 mdpi, etc).
*/
// http://developer.android.com/guide/practices/screen-compat-mode.html


class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener,
    NavigationView.OnNavigationItemSelectedListener, UniversalInterface {
    private var appUpdateManager: AppUpdateManager? = null
    private var installStateUpdatedListener: InstallStateUpdatedListener? = null

    //File puc_img;
    //boolean m_preferences_from_ini;
    //SharedPreferences m_sharedPreferences;
    //EditText etClient;
    //ListView lvMessages;
    //int defaultTextColor;
    var g_options_menu: Menu? = null

    //public TabHost tabHost;
    private var selectClientActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editOrderActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editMessageActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var createCameraActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editPaymentActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editRefundActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editDistribsActivityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var editOrderPreActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    // 16.06.2018 перенесено в Globals
    //public static MyDatabase m_myBase=null;
    //
    private var m_current_frame_type = 0
    private var m_client_descr: String? = null

    // Это то, что выбрано во фрейме
    private var m_route_id: MyID? = null
    private var m_route_date: String? = null
    private var m_route_descr: String? = null

    inner class ParamsToCreateDocs {
        var client_id: String? = null
        var distr_point_id: String? = null
    }

    // При запросе разрешений через эту струкутуру передаются параметры
    // для последующего создания документа
    var mParamsToCreateDocsWhenRequestPermissions: ParamsToCreateDocs? = null

    private var m_filter_dialog: Dialog? = null
    private var m_query_documents_dialog: Dialog? = null

    //SQLiteDatabase m_db;
    var m_order_id_to_process: Long = 0
    var m_refund_id_to_process: Long = 0
    var m_distribs_id_to_process: Long = 0
    var m_payment_id_to_process: Long = 0
    var m_message_id_to_process: Long = 0

    var m_bExcludeImageMessages: Boolean = false
    var m_imagesToSendSize: Int = 0

    protected var m_locationManager: LocationManager? = null
    protected var m_bNeedCoord: Boolean = false
    protected var m_bRegisteredEveryTimeUpdate: Boolean = false

    //ERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS
    var m_bMayResetBase: Boolean = false

    // внутри SQL базы
    var m_settings_DataFormat: String? = null

    //double m_settings_ticket_m;
    //double m_settings_ticket_w;
    var m_settings_agent_id: String? = null
    var m_settings_gps_interval: Int = 0

    // Пусть так и лежит только в базе, считывать не будем
    //String m_settings_agent_price_type_id;
    //
    var m_bRefreshPressed: Boolean = false

    private var isInPermission = false

    private var m_exchangeLogText: ArrayList<String?>? = null
    private var m_exchangeState: String? = null

    private var mTitle: CharSequence? = null

    // This is the select criteria
    //static final String SELECTION = "((" +
    //        ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
    //        ContactsContract.Data.DISPLAY_NAME + " != '' ))";
    fun decodeLoginOrPassword(str: String, codeStrParam: String): String {
        /*
    	codeStr=codeStr.toLowerCase(Locale.ENGLISH);
    	//long c=CRC32(codeStr);
    	Checksum checksum=new CRC32();
    	int i;
    	for (i=0;i<codeStr.length();i++)
    	{
    		char ch=codeStr.charAt(i);
    		if ("1234567890abcdefghijklmnopqrstuvwxyz".indexOf(ch)>=0)
    			checksum.update(ch);
    	}
    	long chval=checksum.getValue();
    	*/

        // если в начале не '*', выходим

        if (str.length == 0 || str.indexOf("*") != 0) {
            return str
        }

        val codeStr = codeStrParam.lowercase()
        //long c=CRC32(codeStr);
        val checksum: Checksum = CRC32()
        var i: Int
        i = 0
        while (i < codeStr.length) {
            val ch = codeStr.get(i)
            if ("1234567890abcdefghijklmnopqrstuvwxyz".indexOf(ch) >= 0) checksum.update(ch.code)
            i++
        }
        var chval = checksum.value

        val sb = StringBuilder()
        i = 1
        while (i < str.length) {
            val str0 = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val ch = str.get(i)
            var idx = str0.indexOf(ch)
            if (idx >= 0) {
                idx += (chval % 10).toInt()
                if (idx >= str0.length) idx -= str0.length
                sb.append(str0.get(idx))
            } else {
                // символа нет в списке
                sb.append(ch)
            }
            chval /= 10
            i++
        }
        return sb.toString()
    }


    fun readSettings() {
        val g = MySingleton.getInstance()
        m_settings_DataFormat = ""
        val cursor = getContentResolver().query(
            MTradeContentProvider.SETTINGS_CONTENT_URI,
            arrayOf("fmt", "agent_id", "gps_interval"),
            null,
            null,
            null
        )
        if (cursor!!.moveToFirst()) {
            val index_data_format = cursor.getColumnIndex("fmt")
            m_settings_DataFormat = cursor.getString(index_data_format)
            //int index_ticket_m = cursor.getColumnIndex("ticket_m");
            //int index_ticket_w = cursor.getColumnIndex("ticket_w");
            val index_agent_id = cursor.getColumnIndex("agent_id")
            //m_settings_ticket_m = cursor.getDouble(index_ticket_m);
            //m_settings_ticket_w = cursor.getDouble(index_ticket_w);
            m_settings_agent_id = cursor.getString(index_agent_id)
            val index_gps_interval = cursor.getColumnIndex("gps_interval")
            m_settings_gps_interval = cursor.getInt(index_gps_interval)
            if (m_settings_gps_interval < 0) {
                g.Common.HAVE_GPS_SETTINGS = true
            }

            //int index_agent_price_type_id = cursor.getColumnIndex("agent_price_type_id");
            //m_settings_agent_price_type_id = cursor.getString(index_agent_price_type_id);
        } else {
            // Формат данных не заполняем специально
            m_settings_DataFormat = ""
            //m_settings_ticket_m = 0.0;
            //m_settings_ticket_w = 0.0;
            m_settings_agent_id = null
            m_settings_gps_interval = 0
            //m_settings_agent_price_type_id = null;
        }
        cursor.close()
        if (m_settings_agent_id == null || m_settings_agent_id!!.replace(" ", "").isEmpty()) {
            val uuid = UUID.randomUUID()
            m_settings_agent_id = uuid.toString()
            writeSettings()
        }
    }

    fun writeSettings() {
        val cv = ContentValues()
        cv.put("fmt", m_settings_DataFormat)
        //cv.put("ticket_m", m_settings_ticket_m);
        //cv.put("ticket_w", m_settings_ticket_w);
        cv.put("agent_id", m_settings_agent_id)
        cv.put("gps_interval", m_settings_gps_interval)
        //cv.put("agent_price_type_id", m_settings_agent_price_type_id);
        // если update не сработает, добавится строка через insert (так сделано в ContentProvider)
        getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, "", null)
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11111111111111111111!!!!!!!!!!!!!!!!!!!!
    fun checkGpsUpdateEveryTime() {
        val g = MySingleton.getInstance()
        if (m_settings_gps_interval > -1 || !(g.Common.PRODLIDER || g.Common.TANDEM)) {
            // обновление координат не требуется
            if (m_bRegisteredEveryTimeUpdate) {
                m_locationManager!!.removeUpdates(locListenerEveryTime)
                m_bRegisteredEveryTimeUpdate = false
            }
        } else {
            val criteria = Criteria()
            criteria.setAccuracy(Criteria.ACCURACY_COARSE)
            val provider = m_locationManager!!.getBestProvider(criteria, true)
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    m_locationManager!!.requestLocationUpdates(
                        provider,
                        ((-m_settings_gps_interval - 1) * 1000).toLong(),
                        0f,
                        locListenerEveryTime
                    )
                }
                m_bRegisteredEveryTimeUpdate = true
            }
        }
    }

    @Suppress("unused")
    fun resetBase() {
        val g = MySingleton.getInstance()
        // Сначала в настройках очищаем формат
        val cv = ContentValues()
        cv.put("fmt", "")
        getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null)
        // Затем очищаем все справочники
        getContentResolver().delete(MTradeContentProvider.VERSIONS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.SALES_LOADED_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.MESSAGES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.PLACES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null)
        getContentResolver().delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null)
        // TODO остальные таблицы
        // ...
        // Если демо-режим, загружаем данные из архива
        if (MySingleton.getInstance().m_DataFormat == "DM" && !Constants.MY_ISTART) {
            var size: Int
            val `is` = getResources().openRawResource(R.raw.demo_arch)
            //ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
            try {
                val zin = ZipInputStream(BufferedInputStream(`is`, BUFFER_SIZE))
                var ze: ZipEntry? = null
                while ((zin.getNextEntry().also { ze = it }) != null) {
                    val total = ze!!.getSize().toInt()
                    val data = ByteArray(total)
                    try {
                        var rd = 0
                        while (total - rd > 0) {
                            val block=zin.read(data, rd, total - rd)
                            if (block==-1) {
                                break
                            }
                            rd += block
                        }
                        zin.closeEntry()
                        if (rd == total && rd > 0) {
                            val strUnzipped = String(data, charset("windows-1251"))

                            val pattern = Pattern.compile("(##.*##)")
                            val mtch = pattern.matcher(strUnzipped)
                            var start_prev = -1
                            var start_this: Int
                            var prevSection = ""
                            var bContinue = true
                            while (bContinue) {
                                var section = ""
                                if (mtch.find()) {
                                    section = mtch.group()
                                    start_this = mtch.start()
                                } else {
                                    bContinue = false
                                    start_this = strUnzipped.length
                                }
                                if (start_prev > 0) {
                                    if (prevSection == "##CLIENTS##") {
                                        TextDatabase.LoadClients(
                                            getContentResolver(),
                                            MySingleton.getInstance().MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##AGREEMENTS30##") {
                                        TextDatabase.LoadAgreements30(
                                            getContentResolver(),
                                            MySingleton.getInstance().MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##AGREEMENTS##") {
                                        TextDatabase.LoadAgreements(
                                            getContentResolver(),
                                            MySingleton.getInstance().MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##NOMENCL##") {
                                        TextDatabase.LoadNomenclature(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                        TextDatabase.fillNomenclatureHierarchy(
                                            getContentResolver(),
                                            getResources(),
                                            "     0   "
                                        )
                                    } else if (prevSection == "##OSTAT##") {
                                        TextDatabase.LoadOstat(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##DOLG##") {
                                        TextDatabase.LoadDolg(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##PRICETYPE##") {
                                        TextDatabase.LoadPriceTypes(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##PRICE##") {
                                        TextDatabase.LoadPrice(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##PRICE_AGREEMENTS30##") {
                                        TextDatabase.LoadPricesAgreements30(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##STOCKS##") {
                                        TextDatabase.LoadStocks(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##AGENTS##") {
                                        TextDatabase.LoadAgents(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##CURATORS##") {
                                        TextDatabase.LoadCurators(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##D_POINTS##") {
                                        TextDatabase.LoadDistrPoints(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##ORGANIZATIONS##") {
                                        TextDatabase.LoadOrganizations(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##PRICE_CLIENT##") {
                                        TextDatabase.LoadClientsPrice(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##PRICE_CURATOR##") {
                                        TextDatabase.LoadCuratorsPrice(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##ROUTES##") {
                                        TextDatabase.LoadRoutes(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##ROUTES_DATES##") {
                                        TextDatabase.LoadRoutesDates(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##MESSAGES##") {
                                        TextDatabase.LoadMessages(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    } else if (prevSection == "##DISCOUNTS##") {
                                        TextDatabase.LoadSimpleDiscounts(
                                            getContentResolver(),
                                            g.MyDatabase,
                                            strUnzipped.substring(start_prev, start_this),
                                            true
                                        )
                                    }
                                }
                                start_prev = start_this + section.length
                                prevSection = section
                            }
                        }
                        if (g.Common.PHARAOH) {
                            // если в демо-режиме отлаживаем для фараона
                            //
                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId)
                            cv.put("code", "000001")
                            cv.put("descr", "Без скидки")
                            cv.put("priceProcent", 0.0)
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI,
                                cv
                            )
                            //
                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId_2)
                            cv.put("code", "000002")
                            cv.put("descr", "Скидка 10%")
                            cv.put("priceProcent", -10.0)
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI,
                                cv
                            )
                            //
                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId_3)
                            cv.put("code", "000003")
                            cv.put("descr", "Скидка 30%")
                            cv.put("priceProcent", -30.0)
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI,
                                cv
                            )

                            //
                            //m_settings_ticket_m=150.0;
                            //m_settings_ticket_w=100.0;

                            //cv.clear();
                            //cv.put("ticket_m", m_settings_ticket_m);
                            //cv.put("ticket_w", m_settings_ticket_w);
                            //getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);
                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId)
                            cv.put("code", "000001")
                            cv.put("descr", "Стол 1")
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.PLACES_CONTENT_URI,
                                cv
                            )

                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId_2)
                            cv.put("code", "000002")
                            cv.put("descr", "Стол 2")
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.PLACES_CONTENT_URI,
                                cv
                            )

                            cv.clear()
                            cv.put("id", MyWebExchange.dummyId_3)
                            cv.put("code", "000003")
                            cv.put("descr", "Стол 3")
                            cv.put("isUsed", 1)
                            getContentResolver().insert(
                                MTradeContentProvider.PLACES_CONTENT_URI,
                                cv
                            )
                        }
                    } finally {
                    }
                }
            } catch (e: IOException) {
            } finally {
            }
            //Toast.makeText(MainActivity.this, "Изменен формат данных, справочники заполнены демонстрационными данными!", Toast.LENGTH_LONG).show();
            Snackbar.make(
                findViewById<View?>(android.R.id.content),
                getString(R.string.message_database_demo_filled),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            //Toast.makeText(MainActivity.this, "Изменен формат данных, справочники очищены!", Toast.LENGTH_LONG).show();
            Snackbar.make(
                findViewById<View?>(android.R.id.content),
                getString(R.string.message_database_cleared),
                Snackbar.LENGTH_LONG
            ).show()
        }
        // В настройках устанавливаем формат
        //cv.clear();
        //cv.put("fmt", m_DataFormat);
        //getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);
        m_settings_DataFormat = MySingleton.getInstance().m_DataFormat
        writeSettings()
    }

    /*
    public View onCreateActionView(MenuItem forItem) {
        // Inflate the action view to be shown on the action bar.
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.action_provider, null);
        ImageButton button = (ImageButton) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do something...
            }
        });
        return view;
    }
    */
    // об отправке ошибок
    // http://habrahabr.ru/post/129582/
    override fun onCreate(savedInstanceState: Bundle?) {
        val g = MySingleton.getInstance()

        mParamsToCreateDocsWhenRequestPermissions = null

        //Snackbar.make(findViewById(android.R.id.content), "Replace with your own action", Snackbar.LENGTH_LONG)
        //       .setAction("Action", null).show();

        // Needs to be called before setting the content view
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // TODO удалить
        //getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null);
        //

        /*
		final String[] locales = getResources().getSystem().getAssets().getLocales();

		for (String locale : locales) {
			//tvInfo.append(locale + "\n"); // выводим в TextView
			Log.v(LOG_TAG, locale.toString());
		}
		*/

        // When ready, show the indeterminate progress bar
        //setProgressBarIndeterminateVisibility(true);

        /*
	    final InputStream mfStream = getClass().getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
		Manifest mf = new Manifest();
		try
		{
			mf.read(mfStream);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		Attributes atts = mf.getMainAttributes();
		g.Common.m_mtrade_version=Integer.parseInt(atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION));
		*/

        /*
        try {
            final PackageManager packageManager = getPackageManager();
            if (packageManager != null) {
                g.Common.m_mtrade_version = packageManager.getPackageInfo(getPackageName(), 0).versionCode;
            } else
            {
                g.Common.m_mtrade_version = BuildConfig.VERSION_CODE;
            }
            //String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            g.Common.m_mtrade_version = 300;
        } catch (Exception e)
        {
            g.Common.m_mtrade_version = BuildConfig.VERSION_CODE;
        }
		 */
        g.Common.m_mtrade_version = BuildConfig.VERSION_CODE

        m_bRefreshPressed = false

        m_bMayResetBase = false

        //m_sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState)

        //readIniFile();
        //readPreferences();
        //g.Common.m_app_theme=m_sharedPreferences.getString("app_theme", "DARK");
        g.checkInitByDataAndSetTheme(this)

        if (savedInstanceState != null) {
            isInPermission = savedInstanceState.getBoolean(STATE_IN_PERMISSION, false)
            m_exchangeLogText = savedInstanceState.getStringArrayList("EXCHANGE_LOG_TEXT")
            m_exchangeState = savedInstanceState.getString("EXCHANGE_STATE")
            // Отбор клиентов
            val client_id = savedInstanceState.getString("client_id")
            if (client_id != null) m_client_id = MyID(client_id)
            else m_client_id = MyID()
            m_client_descr = savedInstanceState.getString("client_descr")
            // Отбор маршрута
            val route_id = savedInstanceState.getString("route_id")
            if (route_id != null) m_route_id = MyID(route_id)
            else m_route_id = MyID()
            m_route_date = savedInstanceState.getString("route_date")
            m_route_descr = savedInstanceState.getString("route_descr")
        } else {
            m_exchangeLogText = ArrayList<String?>()
            m_exchangeState = getString(R.string.exchange_not_executed)
            m_client_id = null
            m_client_descr = ""
            m_route_id = null
            m_route_date = ""
            m_route_descr = ""
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Если текущая дата больше установленной рабочей даты, меняем рабочую дату
        val date = Date()

        val work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime()
        if (work_date.compareTo(date) < 0) if (Common.MyDateFormat(
                "yyyyMMdd",
                date
            ) != Common.MyDateFormat("yyyyMMdd", work_date)
        ) {
            val editor = sharedPreferences.edit()
            editor.putString("work_date", DatePreference.formatter().format(date))
            editor.commit()
        }



        /*
		if (g.Common.m_app_theme.equals("DARK")){
			//setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.LightAppTheme);
		}
		*/
        selectClientActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult>{
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == ClientsActivity.CLIENTS_RESULT_OK) {
                        val data = result.data
                        if (data != null) {
                            val id = data.getLongExtra("id", 0)
                            //Toast.makeText(MainActivity.this, "id=" + id, Toast.LENGTH_SHORT).show();
                            val singleUri = ContentUris.withAppendedId(
                                MTradeContentProvider.CLIENTS_CONTENT_URI,
                                id
                            )
                            //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
                            val cursor = getContentResolver().query(
                                singleUri,
                                arrayOf<String>("descr", "id"),
                                null,
                                null,
                                null
                            )
                            if (cursor!!.moveToNext()) {
                                val descrIndex = cursor!!.getColumnIndex("descr")
                                val idIndex = cursor!!.getColumnIndex("id")
                                val newWord = cursor!!.getString(descrIndex)
                                val clientId = cursor!!.getString(idIndex)
                                //EditText et = (EditText) findViewById(R.id.etClient);
                                //et.setText(newWord);
                                m_client_id = MyID(clientId)
                                m_client_descr = newWord
                                //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                                //getSupportLoaderManager().restartLoader(PAYMENTS_LOADER_ID, null, MainActivity.this);
                                val fragmentManager = getSupportFragmentManager()
                                val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                                if (fragment is OrdersListFragment) {
                                    val ordersListFragment = fragment
                                    ordersListFragment.onFilterClientSelected(clientId, newWord)
                                }
                                if (fragment is PaymentsListFragment) {
                                    val paymentsListFragment = fragment
                                    paymentsListFragment.onFilterClientSelected(clientId, newWord)
                                }
                            }
                            cursor!!.close()
                        }
                    }
                }
            })

        editOrderActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    val resultCode = result.resultCode
                    val data = result.data
                    if (data != null && resultCode == OrderActivity.ORDER_RESULT_OK) {
                        if (g.MyDatabase.m_order_editing.accept_coord == 1) {
                            val gpsenabled =
                                m_locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                            g.MyDatabase.m_order_editing.gpsstate = if (gpsenabled) 1 else 0
                        }
                        g.MyDatabase.m_order_editing.versionPDA++
                        // Перед записью считаем сумму документа
                        g.MyDatabase.m_order_editing.sumDoc =
                            g.MyDatabase.m_order_editing.GetOrderSum(null, false)
                        g.MyDatabase.m_order_editing.weightDoc = TextDatabase.GetOrderWeight(
                            getContentResolver(),
                            g.MyDatabase.m_order_editing,
                            null,
                            false
                        )
                        if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED) {
                            g.MyDatabase.m_order_editing.state = E_ORDER_STATE.E_ORDER_STATE_CREATED
                        }
                        TextDatabase.SaveOrderSQL(
                            getContentResolver(),
                            g.MyDatabase.m_order_editing,
                            g.MyDatabase.m_order_editing_id,
                            0
                        )
                        if (g.Common.NEW_BACKUP_FORMAT) {
                            // Удаляем резервную копию документа после записи настоящего документа
                            val singleUri = ContentUris.withAppendedId(
                                MTradeContentProvider.ORDERS_CONTENT_URI,
                                g.MyDatabase.m_order_new_editing_id
                            )
                            getContentResolver().delete(singleUri, "editing_backup<>0", null)
                        }
                        // Переименовываем файл изображения
                        if ((g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) && g.MyDatabase.m_order_editing_created) {
                            val photoFileDir = Common.getMyStorageFileDir(getBaseContext(), "photo")
                            val attachFileDir =
                                Common.getMyStorageFileDir(getBaseContext(), "attaches")
                            val fileOrderImage1 = File(photoFileDir, "order_image_1.jpg")
                            if (fileOrderImage1.exists()) {
                                val fileOrderImage1Dest = File(
                                    attachFileDir,
                                    "order_image_1_" + g.MyDatabase.m_order_editing.uid.toString()
                                        .replace("{", "").replace("}", "") + ".jpg"
                                )
                                fileOrderImage1.renameTo(fileOrderImage1Dest)
                            }
                            val fileOrderImage2 = File(photoFileDir, "order_image_2.jpg")
                            if (fileOrderImage2.exists()) {
                                val fileOrderImage2Dest = File(
                                    attachFileDir,
                                    "order_image_2_" + g.MyDatabase.m_order_editing.uid.toString()
                                        .replace("{", "").replace("}", "") + ".jpg"
                                )
                                fileOrderImage2.renameTo(fileOrderImage2Dest)
                            }
                        }
                        //
                        if (!m_bNeedCoord && g.MyDatabase.m_order_editing.accept_coord == 1) {
                            m_bNeedCoord = true
                            startGPS()
                        }
                        // Резервное сохрание заявки в текстовый файл, либо удаление этой копии
                        if (!g.Common.NEW_BACKUP_FORMAT && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                            // получаем путь к SD
                            val sdPath = File(
                                Environment.getExternalStorageDirectory()
                                    .getAbsolutePath() + "/mtrade/orders"
                            )
                            // создаем каталог
                            if (!sdPath.exists()) {
                                sdPath.mkdirs()
                            }
                            //
                            val sdFile =
                                File(sdPath, g.MyDatabase.m_order_editing.uid.toString() + ".txt")
                            if (E_ORDER_STATE.getCanBeRestoredFromTextFile(g.MyDatabase.m_order_editing.state)) {
                                try {
                                    //BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                                    val bw = BufferedWriter(
                                        OutputStreamWriter(
                                            FileOutputStream(sdFile),
                                            "cp1251"
                                        )
                                    )
                                    bw.write("0\r\n") // obsolette m_orders_version
                                    bw.write(g.Common.m_mtrade_version.toString())
                                    bw.write("\r\n")
                                    TextDatabase.SaveOrderToText(
                                        bw,
                                        g.MyDatabase.m_order_editing,
                                        false,
                                        true
                                    )
                                    bw.write("@@@") // начиная с версии 3.0
                                    bw.flush()
                                    bw.close()
                                } catch (e: IOException) {
                                    Log.e(LOG_TAG, "error", e)
                                }
                            } else {
                                sdFile.delete()
                            }
                        }

                        if (g.Common.PHARAOH && g.MyDatabase.m_order_editing.dont_need_send == 0) {
                            // и запускаем обмен
                            ExchangeTask().execute(3, 0, "")
                        }

                        // если режим маршрута, добавим туда заказ
                        updateDocumentInRoute(
                            g.MyDatabase.m_order_editing.distr_point_id.toString(),
                            g.MyDatabase.m_order_editing
                        )
                    }
                    if (resultCode == OrderActivity.ORDER_RESULT_CANCEL) {
                        if (g.Common.NEW_BACKUP_FORMAT) {
                            // если документ не меняли, там будет -1, соответственно удалять нечего
                            if (g.MyDatabase.m_order_new_editing_id > 0) {
                                val singleUri = ContentUris.withAppendedId(
                                    MTradeContentProvider.ORDERS_CONTENT_URI,
                                    g.MyDatabase.m_order_new_editing_id
                                )
                                getContentResolver().delete(singleUri, "editing_backup<>0", null)
                            }
                        } else {
                            // Старый вариант
                            if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_order_editing_id != 0L) {
                                val singleUri = ContentUris.withAppendedId(
                                    MTradeContentProvider.ORDERS_CONTENT_URI,
                                    g.MyDatabase.m_order_editing_id
                                )
                                getContentResolver().delete(
                                    singleUri,
                                    E_ORDER_STATE.getCanBeDeletedConditionWhere(),
                                    E_ORDER_STATE.getCanBeDeletedArgs()
                                )
                            }
                        }
                    }
                }
            })

        editMessageActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    // Можем менять только свои сообщения (&4)!=0
                    if (result.resultCode == MessageActivity.MESSAGE_RESULT_OK && (g.MyDatabase.m_message_editing.acknowledged and 4) != 0) {
                        val data = result.data
                        if (data != null) {
                            g.MyDatabase.m_message_editing.ver++
                            TextDatabase.SaveMessageSQL(
                                getContentResolver(),
                                g.MyDatabase,
                                g.MyDatabase.m_message_editing,
                                g.MyDatabase.m_message_editing_id
                            )

                            if (!g.MyDatabase.m_message_editing.fname.isEmpty()) {
                                val attachFileDir =
                                    Common.getMyStorageFileDir(this@MainActivity, "attaches")

                                val inFile = File(g.MyDatabase.m_message_editing.fname)
                                val outFile = File(attachFileDir, inFile.getName())

                                var bOutFileOpened = false

                                val `in`: InputStream?
                                val out: OutputStream?
                                try {
                                    `in` = FileInputStream(inFile)
                                    out = FileOutputStream(outFile)
                                    bOutFileOpened = true
                                    // Transfer bytes from in to out
                                    val buf = ByteArray(1024)
                                    var len: Int
                                    while ((`in`.read(buf).also { len = it }) > 0) {
                                        out.write(buf, 0, len)
                                    }
                                    `in`.close()
                                    out.close()
                                } catch (e: FileNotFoundException) {
                                    // TODO Auto-generated catch block
                                    //e.printStackTrace();
                                } catch (e: IOException) {
                                    // TODO Auto-generated catch block
                                    //e.printStackTrace();
                                    if (bOutFileOpened) {
                                        outFile.delete()
                                    }
                                }
                            }
                        }
                    }
                }
            })
        createCameraActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    // Можем менять только свои сообщения (&4)!=0
                    if (result.resultCode == RESULT_OK) {
                        val data = result.data

                        /*
    			Cursor mediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Images.ImageColumns.ORIENTATION, MediaStore.MediaColumns.SIZE }, null, null, null);
    			if (mediaCursor!=null&&mediaCursor.moveToNext())
    			{
    				int rotation=mediaCursor.getInt(0);
    				Toast.makeText(MainActivity.this, Integer.toString(rotation), Toast.LENGTH_LONG).show();
    			}
    			*/
                        /*
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                //imageView.setImageBitmap(photo);
                //MediaStore.Images.Media.insertImage(getContentResolver(), photo,
                //        null, null);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] b = baos.toByteArray();
                */
                        /*
                File outFile = new File(Environment.getExternalStorageDirectory(), "myname.jpeg");
                FileOutputStream fos = new FileOutputStream(outFile);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                */
                        /*
    			File inFile=new File(data.getExtras().get(MediaStore.EXTRA_OUTPUT).toString());
    			try {
					ExifInterface exif = new ExifInterface(inFile.getAbsolutePath());
					exif_ORIENTATION = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/

                        // Unfortunately there is a bug on some devices causing the Intend data parameter
                        // in onActivityResult to be null when you use the MediaStore.EXTRA_OUTPUT flag
                        // in your intent for the camera. A workaround is to keep the outputFileUri
                        // variable global so that you can access it again in your onActivityResult method
                        val date = Date()
                        //String timeString=String.valueOf(System.currentTimeMillis());
                        val timeString = Common.MyDateFormat("dd.MM.yyyy HH:mm:ss", date)

                        val fileName = Common.getDateTimeAsString14(date).toString() + ".jpg"
                        // Временный файл, будет использоваться не всегда, а только у самсунгов
                        val fileNameIntermediate =
                            "S" + Common.getDateTimeAsString14(date).toString() + ".jpg"

                        val photoDir = Common.getMyStorageFileDir(this@MainActivity, photoFolder)
                        if (!photoDir.exists()) {
                            photoDir.mkdirs()
                        }

                        var bSavedOk = false

                        val imgWithTime = File(photoDir, fileName)
                        var exifInterface: ExifInterface? = null

                        if (data != null) {
                            //if the intent data is not null, use it
                            //puc_image = getImagePath(Uri.parse(data.toURI())); //update the image path accordingly
                            //StoreImage(this, Uri.parse(data.toURI()), puc_img);
                            if (data.getData() != null) {
                                bSavedOk = ImagePrinting.StoreImage(
                                    this@MainActivity,
                                    data.getData(),
                                    imgWithTime,
                                    timeString
                                )
                                if (bSavedOk) {
                                    // Удалим первый файл, созданный телефоном
                                    val imgPath = ImagePrinting.getImagePath(
                                        this@MainActivity,
                                        data.getData()
                                    )
                                    val oldFile = File(imgPath)
                                    oldFile.delete()
                                }
                            } else {
                                // Самсунг)
                                if (!ImagePrinting.checkImagePath(
                                        this@MainActivity,
                                        outputPhotoFileUri
                                    )
                                ) {
                                    // реального имени файла нет, данные в потоке
                                    // заберем их оттуда в промежуточный файл
                                    val intermegiateFile = File(photoDir, fileNameIntermediate)
                                    try {
                                        val input = getContentResolver().openInputStream(
                                            outputPhotoFileUri!!
                                        )
                                        if (Common.myCopyStreamToFile(input, intermegiateFile)) {
                                            outputPhotoFileUri = Uri.fromFile(intermegiateFile)
                                        }
                                    } catch (e: FileNotFoundException) {
                                        throw RuntimeException(e)
                                    }
                                }
                                bSavedOk = ImagePrinting.StoreImage(
                                    this@MainActivity,
                                    outputPhotoFileUri,
                                    imgWithTime,
                                    timeString
                                )
                                /*
                	  // test
                	  if (bSavedOk)
                	  {
                		  byte []textData=new byte[100*150*3];
                		  int i;
                		  for (i=0;i<100*150*3;i++)
                		  {
                			  textData[i]=0;
                		  }
                          File imgWithTime2=new File(photoDir, "!17!.jpg");
                		  int ix=NativeCallsClass.convertJpegFile(imgWithTime.getAbsolutePath(), imgWithTime2.getAbsolutePath(), textData, 0, 0, 150, 100, 75, 1);
                	  }
                	  //
                	  */
                            }
                        } else {
                            //Use the outputFileUri global variable
                            bSavedOk = ImagePrinting.StoreImage(
                                this@MainActivity,
                                outputPhotoFileUri,
                                imgWithTime,
                                timeString
                            )
                        }
                        if (bSavedOk) {
                            // И второй в первом случае или единственный во втором
                            // До 21.08.2018
                            //String imgPath=ImagePrinting.getImagePath(this, outputPhotoFileUri);
                            //File oldFile = new File(imgPath);
                            //try {
                            //	  exifInterface = new ExifInterface(imgPath);
                            // } catch (IOException e) {
                            //		// TODO Auto-generated catch block
                            //		//e.printStackTrace();
                            // 	  exifInterface=null;
                            //  }
                            //  oldFile.delete();
                            // После
                            if (outputPhotoFileUri!!.getScheme() == "content") {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        val iStream = getContentResolver().openInputStream(
                                            outputPhotoFileUri!!
                                        )
                                        exifInterface = ExifInterface(iStream!!)
                                    }
                                } catch (e: IOException) {
                                    exifInterface = null
                                }
                                /* TODO удалять файл
                        FileProvider provider = new FileProvider();
                        grantUriPermission(getApplicationContext().getPackageName(), outputPhotoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Падает вот здесь
                        provider.delete(outputPhotoFileUri, null, null);
                        */
                            } else {
                                val imgPath = ImagePrinting.getImagePath(
                                    this@MainActivity,
                                    outputPhotoFileUri
                                )
                                val oldFile = File(imgPath)
                                try {
                                    exifInterface = ExifInterface(imgPath)
                                } catch (e: IOException) {
                                    exifInterface = null
                                }
                                oldFile.delete()
                            }

                            val exif_DATETIME = Common.MyDateFormat("yyyy:MM:dd HH:mm:ss", date)
                            var exif_MODEL: String? = "Android"
                            var exif_MAKE: String? = ""

                            //String exif_IMAGE_LENGTH="";
                            //String exif_IMAGE_WIDTH="";
                            if (exifInterface != null) {
                                //exif_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                                exif_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL)
                                exif_MAKE = exifInterface.getAttribute(ExifInterface.TAG_MAKE)
                                //String et=exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                                //exif_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface..TAG_IMAGE_LENGTH);
                                //exif_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                            }
                            try {
                                val exifInterface2 = ExifInterface(imgWithTime.getAbsolutePath())
                                exifInterface2.setAttribute(
                                    ExifInterface.TAG_DATETIME,
                                    exif_DATETIME
                                )
                                exifInterface2.setAttribute(ExifInterface.TAG_MODEL, exif_MODEL)
                                exifInterface2.setAttribute(ExifInterface.TAG_MAKE, exif_MAKE)
                                exifInterface2.setAttribute(
                                    ExifInterface.TAG_DATETIME_ORIGINAL,
                                    exif_DATETIME
                                )
                                exifInterface2.setAttribute(
                                    ExifInterface.TAG_DATETIME_DIGITIZED,
                                    exif_DATETIME
                                )
                                exifInterface2.saveAttributes()


                                /*
                                    JpegHeaders headers;
                                    try {

                                        // Initializes static data - not necessary as it is done
                                        // automatically the first time it is needed.  However doing
                                        // it explicitly means the first call to the library won't be
                                        // slower than subsequent calls.
                                        JpegHeaders.preheat();

                                        // Parse the JPEG file
                                        headers = new JpegHeaders(imgWithTime.getAbsolutePath());

                                        // If the file isn't EXIF, convert it
                                        if (headers.getApp1Header() == null)
                                            headers.convertToExif();

                                        App1Header app1Header = headers.getApp1Header();
                                        //app1Header.setValue(new TagValue(Tag.IMAGEDESCRIPTION,"bla bla bla"));
                                        app1Header.setValue(new TagValue(Tag.DATETIMEORIGINAL, exif_DATETIME));
                                        app1Header.setValue(new TagValue(Tag.DATETIMEDIGITIZED, exif_DATETIME));
                                        // set a field
                                        //app1Header.setValue(new TagValue(Tag.IMAGEDESCRIPTION,
                                        //			     "My new image description"));
                                        //headers.save(true);
                                        // Резервная копия не нужна
                                        headers.save(false);

                                    } catch (ExifFormatException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (TagFormatException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    } catch (JpegFormatException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                     */
                            } catch (e: IOException) {
                                // TODO Auto-generated catch block
                                e.printStackTrace()
                            }
                            editMessage(0, imgWithTime.getAbsolutePath())
                        }
                    }
                }
            })
        editPaymentActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == PaymentActivity.PAYMENT_RESULT_OK) {
                        val data = result.data
                        if (data != null) {
                            g.MyDatabase.m_payment_editing.versionPDA++
                            TextDatabase.SavePaymentSQL(
                                getContentResolver(),
                                g.MyDatabase,
                                g.MyDatabase.m_payment_editing,
                                g.MyDatabase.m_payment_editing_id
                            )
                            if (!m_bNeedCoord && g.MyDatabase.m_payment_editing.accept_coord == 1) {
                                m_bNeedCoord = true
                                startGPS()
                            }
                            // если режим маршрута, добавим туда платеж
                            updateDocumentInRoute(
                                g.MyDatabase.m_payment_editing.distr_point_id.toString(),
                                g.MyDatabase.m_payment_editing
                            )
                        }
                    }
                }
            })
        editRefundActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    val resultCode = result.resultCode
                    if (resultCode == RefundActivity.REFUND_RESULT_OK) {
                        val data = result.data
                        if (data != null) {
                            if (g.MyDatabase.m_refund_editing.accept_coord == 1) {
                                val gpsenabled =
                                    m_locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                g.MyDatabase.m_refund_editing.gpsstate = if (gpsenabled) 1 else 0
                            }
                            g.MyDatabase.m_refund_editing.versionPDA++
                            // Перед записью считаем вес документа
                            g.MyDatabase.m_refund_editing.weightDoc = TextDatabase.GetOrderWeight(
                                getContentResolver(),
                                g.MyDatabase.m_order_editing,
                                null,
                                false
                            )
                            if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
                                g.MyDatabase.m_refund_editing.state =
                                    E_REFUND_STATE.E_REFUND_STATE_CREATED
                            }
                            TextDatabase.SaveRefundSQL(
                                getContentResolver(),
                                g.MyDatabase.m_refund_editing,
                                g.MyDatabase.m_refund_editing_id
                            )
                            if (!m_bNeedCoord && g.MyDatabase.m_refund_editing.accept_coord == 1) {
                                m_bNeedCoord = true
                                startGPS()
                            }

                            // Резервное сохрание возврата в текстовый файл, либо удаление этой копии
                            // не выполняем, в отличие от заказа
                            // TODO

                            // если режим маршрута, добавим туда возврат
                            updateDocumentInRoute(
                                g.MyDatabase.m_refund_editing.distr_point_id.toString(),
                                g.MyDatabase.m_refund_editing
                            )
                        }
                    }
                    if (resultCode == RefundActivity.REFUND_RESULT_CANCEL) {
                        if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_refund_editing_id != 0L) {
                            val singleUri = ContentUris.withAppendedId(
                                MTradeContentProvider.REFUNDS_CONTENT_URI,
                                g.MyDatabase.m_refund_editing_id
                            )
                            getContentResolver().delete(
                                singleUri,
                                E_REFUND_STATE.getCanBeDeletedConditionWhere(),
                                E_REFUND_STATE.getCanBeDeletedArgs()
                            )
                        }
                    }
                }
            })
        editDistribsActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == DistribsActivity.DISTRIBS_RESULT_OK) {
                        val data = result.data
                        if (data != null) {
                            if (g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                                val gpsenabled =
                                    m_locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                g.MyDatabase.m_distribs_editing.gpsstate = if (gpsenabled) 1 else 0
                            }
                            g.MyDatabase.m_distribs_editing.versionPDA++
                            TextDatabase.SaveDistribsSQL(
                                getContentResolver(),
                                g.MyDatabase.m_distribs_editing,
                                g.MyDatabase.m_distribs_editing_id
                            )
                            if (!m_bNeedCoord && g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                                m_bNeedCoord = true
                                startGPS()
                            }
                            // если режим маршрута, добавим туда дистрибьюцию
                            updateDocumentInRoute(
                                g.MyDatabase.m_distribs_editing.distr_point_id.toString(),
                                g.MyDatabase.m_distribs_editing
                            )
                        }
                    }
                }
            })

        editOrderPreActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == OrderPreActivity.ORDER_PRE_ACTION_RESULT_OK) {
                        val data = result.data
                        if (data != null) editOrder(
                            0,
                            false,
                            data.getStringExtra("client_id"),
                            data.getStringExtra("distr_point_id")
                        )
                    }
                }
            })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager =
                getSystemService<NotificationManager>(NotificationManager::class.java)
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
            )
        }


        setContentView(R.layout.main_navigation_drawer)
        val toolbar = findViewById<View?>(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        //toolbar.setPopupTheme();
        if (Constants.MY_DEBUG) {
            setTitle("DEBUG")
        } else {
            setTitle(R.string.app_label)
        }

        mTitle = getTitle()

        m_current_frame_type =
            if (g.Common.PRODLIDER && !g.Common.DEMO) FRAME_TYPE_ROUTES else FRAME_TYPE_ORDERS
        if (savedInstanceState != null) m_current_frame_type =
            savedInstanceState.getInt("current_frame_type", FRAME_TYPE_ORDERS)

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        var reindexNeeded = false

        if (g.MyDatabase == null) {
            // Первый запуск
            g.MyDatabase = MyDatabase()
            g.MyDatabase.m_order_editing.bCreatedInSingleton = false
            g.MyDatabase.m_refund_editing.bCreatedInSingleton = false
            //g.Common=new Common();
            // TODO все остальное тоже в globals перенести
            //m_client_id = null;
            //m_client_descr = "";
            m_bNotClosed = false

            if (Constants.MY_DEBUG == false) {
                // Проверим, требуется ли индексация базы
                val cursor = getContentResolver().query(
                    MTradeContentProvider.SEANCES_CONTENT_URI,
                    arrayOf<String>("incoming", "outgoing"),
                    null,
                    null,
                    null
                )
                if (cursor != null) {
                    val index_incoming = cursor.getColumnIndex("incoming")
                    val index_outgoing = cursor.getColumnIndex("outgoing")

                    var incoming: String? = null
                    var outgoing: String? = null

                    if (cursor.moveToNext()) {
                        incoming = cursor.getString(index_incoming)
                        outgoing = cursor.getString(index_outgoing)
                    }
                    if (incoming == null || outgoing == null || (incoming != outgoing)) {
                        // Реиндексация требуется
                        // В новом формате вопросов не задается
                        if (g.Common.PHARAOH || g.Common.NEW_BACKUP_FORMAT) {
                            //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                            //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                            /*
                            if (incoming != null && incoming.startsWith("UPGRADED") && MTradeContentProvider.DB_VERSION == 67) {
                                // 66 это версия базы, когда изменилась у андроида работа с файлами на флэшке
                                // поэтому попытаемся скопировать
                                new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED), "66", "");
                            } else {
                                new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED), "", "");
                            }
                             */
                            // 25.03.2025 убрал проверку 66 версии, тем более, что это уже не срабатывает, т.к. проверка на константу 67 не выполняется никогда
                            ServiceOperationsTask().execute(
                                SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED.toString(),
                                "",
                                ""
                            )
                        } else {
                            reindexNeeded = true
                            if (incoming != null && incoming == "CREATED") {
                                showDialog(IDD_REINDEX_CREATED)
                            } else if (incoming != null && incoming.startsWith("UPGRADED")) {
                                // 66 это версия базы, когда изменилась у андроида работа с файлами на флэшке
                                // поэтому попытаемся скопировать
                                /*
                                if (MTradeContentProvider.DB_VERSION == 67)
                                    showDialog(IDD_REINDEX_UPGRADED66);
                                else
                                    showDialog(IDD_REINDEX_UPGRADED);
                                 */
                                // 25.03.2025 убрал проверку 66 версии, тем более, что это уже не срабатывает, т.к. проверка на константу 67 не выполняется никогда
                                showDialog(IDD_REINDEX_UPGRADED)
                            } else {
                                showDialog(IDD_REINDEX_INCORRECT_CLOSED)
                            }
                        }
                    }
                    cursor.close()
                }
            }

            // Проверим, есть ли заказы не до конца записанные (сбой)
            // у них мы будем пересчитывать сумму и прочее
            if (g.Common.NEW_BACKUP_FORMAT) {
                // TODO перенести в RecoverOrders может быть
                // восстанавливаем созданные заказы, которые не были сохранены (editing_backup=1)
                var cursor = getContentResolver().query(
                    MTradeContentProvider.ORDERS_CONTENT_URI,
                    arrayOf<String>("_id"),
                    "editing_backup=1",
                    null,
                    null
                )
                if (cursor != null) {
                    val index_id = cursor.getColumnIndex("_id")
                    while (cursor.moveToNext()) {
                        val or = OrderRecord()
                        //etClient.setText(cursor.getString(0));
                        val backup_order_id = cursor.getLong(index_id)
                        TextDatabase.ReadOrderBy_Id(getContentResolver(), or, backup_order_id)
                        // TODO остальные поля, такие как цены, например
                        or.sumDoc = or.GetOrderSum(null, false)
                        or.weightDoc =
                            TextDatabase.GetOrderWeight(getContentResolver(), or, null, false)
                        // TODO не помню уже что имел в виду, когда писал следующий комментарий
                        or.dont_need_send =
                            0 // этот флаг в случае, если документ на сервере, не может быть установлен
                        or.state = E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED
                        var _id: Long = 0 // чтобы все же в журнал документ добавился
                        _id = TextDatabase.SaveOrderSQL(getContentResolver(), or, _id, 0)
                        // теперь удаляем копию
                        val singleUri = ContentUris.withAppendedId(
                            MTradeContentProvider.ORDERS_CONTENT_URI,
                            backup_order_id
                        )
                        getContentResolver().delete(singleUri, "editing_backup=1", null)
                    }
                    cursor.close()
                }
                // ищем заказы, которые начинали редактировать
                // и если оригинальный заказ пустой, перезапишем его
                cursor = getContentResolver().query(
                    MTradeContentProvider.ORDERS_CONTENT_URI,
                    arrayOf<String>("_id", "old_id"),
                    "editing_backup=2",
                    null,
                    null
                )
                if (cursor != null) {
                    val index_id = cursor.getColumnIndex("_id")
                    val index_old_id = cursor.getColumnIndex("old_id")
                    while (cursor.moveToNext()) {
                        val or = OrderRecord()
                        val backup_order_id = cursor.getLong(index_id)
                        val old_order_id = cursor.getLong(index_old_id)
                        TextDatabase.ReadOrderBy_Id(getContentResolver(), or, old_order_id)
                        if (or.lines.size == 0) {
                            val versionPDA = or.versionPDA
                            TextDatabase.ReadOrderBy_Id(getContentResolver(), or, backup_order_id)
                            or.versionPDA = versionPDA + 1
                            // TODO остальные поля, такие как цены, например
                            or.sumDoc = or.GetOrderSum(null, false)
                            or.weightDoc =
                                TextDatabase.GetOrderWeight(getContentResolver(), or, null, false)
                            or.dont_need_send =
                                0 // этот флаг в случае, если документ на сервере, не может быть установлен
                            or.state = E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED
                            TextDatabase.SaveOrderSQL(getContentResolver(), or, old_order_id, 0)
                            // теперь удаляем копию
                            val singleUri = ContentUris.withAppendedId(
                                MTradeContentProvider.ORDERS_CONTENT_URI,
                                backup_order_id
                            )
                            getContentResolver().delete(singleUri, "editing_backup=2", null)
                        }
                    }
                    cursor.close()
                }
            }

            //

            //DBHelper dbh = new DBHelper(getApplicationContext());
            //DBHelper dbHelper = DBHelper.getInstance(getBaseContext());
            //m_db = dbh.getWritableDatabase();
            //Log.d(LOG_TAG, " --- Staff db v." + db.getVersion() + " --- ");
            //writeStaff(db);
            //dbh.close();
            try {
                val cv = ContentValues()
                cv.put("incoming", g.MyDatabase.m_seance.toString())
                getContentResolver().insert(MTradeContentProvider.SEANCES_INCOMING_CONTENT_URI, cv)
            } catch (e: Exception) {
                // при просмотре ошибок на маркете (в версии 3.49) есть только такая "Unknown URL content://ru.code22.providers.mtrade/seancesIncoming"
            }
        }

        val navigationView = findViewById<View?>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val navMenu = navigationView.getMenu()

        // Здесь также установится название клиента из отбора
        afterCreate(reindexNeeded)

        setCheckedMode(m_current_frame_type)
        attachFrameType(m_current_frame_type)

        // Проверка добавлена 24.04.2023
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // взято отсюда
            // https://github.com/commonsguy/cw-omnibus/blob/master/Location/Classic/app/src/main/java/com/commonsware/android/weather2/AbstractPermissionActivity.java
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onReady(savedInstanceState)
            } else {
                if (!isInPermission) {
                    isInPermission = true
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PERMISSION_REQUEST_STORAGE
                    )
                }
            }
        }

        checkForAppUpdate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_IN_PERMISSION, isInPermission)
        outState.putStringArrayList("EXCHANGE_LOG_TEXT", m_exchangeLogText)
        outState.putInt("current_frame_type", m_current_frame_type)
        if (m_client_id != null) {
            outState.putString("client_id", m_client_id.toString())
        } else {
            outState.putString("client_id", null)
        }
        outState.putString("client_descr", m_client_descr)
        if (m_route_id != null) {
            outState.putString("route_id", m_route_id.toString())
        } else {
            outState.putString("route_id", null)
        }
        outState.putString("route_date", m_route_date)
        outState.putString("route_descr", m_route_descr)
    }

    private fun onReady(savedInstanceState: Bundle?) {
        //MySingleton g = MySingleton.getInstance();
    }

    private fun afterCreate(reindexNeeded: Boolean) {
        /*
               tabHost = (TabHost) findViewById(android.R.id.tabhost);
       
               tabHost.setup();
       
               TabHost.TabSpec spec = tabHost.newTabSpec("tag1");
       
               spec.setContent(R.id.tab1);
               spec.setIndicator(getString(R.string.tab_orders), getResources().getDrawable(android.R.drawable.ic_menu_share));
               tabHost.addTab(spec);
       
               spec = tabHost.newTabSpec("tag2");
               spec.setContent(R.id.tab2);
               spec.setIndicator(getString(R.string.tab_exchange), getResources().getDrawable(android.R.drawable.ic_popup_sync));
               tabHost.addTab(spec);
       
               //if (g.Common.PHARAOH)
               //{
               //	// С сообщениями не работают
               //} else
               //{
               spec = tabHost.newTabSpec("tag3");
               spec.setContent(R.id.tab3);
               spec.setIndicator(getString(R.string.tab_messages), getResources().getDrawable(android.R.drawable.ic_dialog_email));
               tabHost.addTab(spec);
               //}
               updatePageMessagesVisibility();
       
               tabHost.setCurrentTab(0);
               tabHost.setOnTabChangedListener(this);
               */

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        // Отбор по клиенту
        m_client_descr = getString(R.string.catalogue_all_clients)
        if (m_client_id != null && !m_client_id!!.isEmpty()) {
            // клиент уже был выбран, повторный запуск, но память не освобождалась
            // Прочитаем название
            val clientCursor = getContentResolver().query(
                MTradeContentProvider.CLIENTS_CONTENT_URI,
                arrayOf<String>("descr"),
                "id=?",
                arrayOf<String>(
                    m_client_id.toString()
                ),
                null
            )
            if (clientCursor!!.moveToNext()) {
                m_client_descr = clientCursor.getString(0)
            } else {
                m_client_descr = "{" + m_client_id.toString() + "}"
            }
            clientCursor.close()
        }
        // Маршрут
        // вообще, эти действия не обязательны, т.к. название клиента и маршрут считывается из настроек
        m_route_descr = getString(R.string.route_not_set)
        if (m_route_id != null && !m_route_id!!.isEmpty()) {
            val routeDateCursor = getContentResolver().query(
                MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI,
                arrayOf<String>("route_date", "descr"),
                "route_id=?",
                arrayOf<String>(m_route_id.toString()),
                null
            )
            if (routeDateCursor!!.moveToNext()) {
                val index_route_date = routeDateCursor.getColumnIndex("route_date")
                val index_route_descr = routeDateCursor.getColumnIndex("descr")
                m_route_date = routeDateCursor.getString(index_route_date)
                m_route_descr = routeDateCursor.getString(index_route_descr)
            } else {
                m_route_date = ""
                m_route_descr = "{" + m_client_id.toString() + "}"
            }
            routeDateCursor.close()
        } else {
            // 05.06.2019 установим по умолчанию маршрут за этот день
            m_route_date = Common.MyDateFormat("yyyyMMdd", Date())
            val routeDateCursor = getContentResolver().query(
                MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI,
                arrayOf<String>("route_id", "descr"),
                "route_date=?",
                arrayOf<String?>(m_route_date),
                null
            )
            if (routeDateCursor!!.moveToNext()) {
                val index_route_id = routeDateCursor.getColumnIndex("route_id")
                val index_route_descr = routeDateCursor.getColumnIndex("descr")
                m_route_id = MyID(routeDateCursor.getString(index_route_id))
                m_route_descr = routeDateCursor.getString(index_route_descr)
            } else {
                m_route_date = ""
                m_route_id = MyID()
            }
            routeDateCursor.close()
            /*
            db.execSQL("create table routes_dates (" +
                    "_id integer primary key autoincrement, " +
                    "route_date text," +
                    "route_id text," +
                    "UNIQUE ('route_date')"+
                    ");");
                   */
            //
        }

        m_locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // TODO остальные виды документа, кроме заказов
        val accept_coord_where = E_ORDER_STATE.getAcceptCoordConditionWhere()
        val accept_coord_selectionArgs = E_ORDER_STATE.getAcceptCoordConditionSelectionArgs()
        m_bNeedCoord = false
        var cursorAcceptCoord: Cursor?
        cursorAcceptCoord = getContentResolver().query(
            MTradeContentProvider.ORDERS_CONTENT_URI,
            arrayOf<String>("id"),
            accept_coord_where,
            accept_coord_selectionArgs,
            null
        )
        if (cursorAcceptCoord!!.moveToFirst()) {
            m_bNeedCoord = true
        }
        cursorAcceptCoord.close()
        // Визиты тоже проверим
        cursorAcceptCoord = getContentResolver().query(
            MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
            arrayOf<String>("_id"),
            "accept_coord=1",
            null,
            null
        )
        if (cursorAcceptCoord!!.moveToFirst()) {
            m_bNeedCoord = true
        }
        cursorAcceptCoord.close()
        if (m_bNeedCoord) {
            startGPS()
        }
        //
        m_bRegisteredEveryTimeUpdate = false

        /*
        Location loc = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc!=null)
        {
        	long time=loc.getTime();
        }
        */

        //registerForContextMenu(lvOrders);
        //registerForContextMenu(lvPayments);
        //registerForContextMenu(lvMessages);
        if (reindexNeeded == false) loadDataAfterStart()
    }

    /*
    void checkFirebaseToken() {
        final SharedPreferences pref = getSharedPreferences("MTradePreferences", 0);

        String fcm_instanceId=pref.getString("fcm_instanceId", "");

        if (fcm_instanceId==null||fcm_instanceId.isEmpty()) {
            // Запросим InstanceId

            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                //Could not get FirebaseMessagingToken
                                Log.w(LOG_TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            if (null != task.getResult()) {
                                //Got FirebaseMessagingToken
                                String firebaseMessagingToken = Objects.requireNonNull(task.getResult());
                                //Use firebaseMessagingToken further
                                SharedPreferences.Editor pref_editor = pref.edit();
                                pref_editor.putString("fcm_instanceId", firebaseMessagingToken);
                                pref_editor.commit();
                            }
                        });
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
     */
    fun checkSendImages() {
        m_imagesToSendSize = 0

        /*
        // Проверяем, есть ли файлы изображений
        // На флэшке
        if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File attachesFileDir = new File(Environment.getExternalStorageDirectory(), "/mtrade/attaches");
            if (attachesFileDir.exists()) {
                File[] tempFileNames = attachesFileDir.listFiles();
                if (tempFileNames != null) {
                    for (File tempFile : tempFileNames) {
                        if (!tempFile.isDirectory()) {
                            m_imagesToSendSize += tempFile.length();
                        }
                    }
                }
            }
        }
        // В памяти устройства
        File attachesFileDir = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/attaches");
        if (attachesFileDir.exists()) {
            // отправляем все файлы из папки в памяти устройства
            File[] tempFileNames = attachesFileDir.listFiles();
            if (tempFileNames != null) {
                for (File tempFile : tempFileNames) {
                    if (!tempFile.isDirectory()) {
                        m_imagesToSendSize += tempFile.length();
                    }
                }
            }
        }
         */
        val attachesFileDir = Common.getMyStorageFileDir(this@MainActivity, "attaches")
        // отправляем все файлы из папки в памяти устройства
        val tempFileNames = attachesFileDir.listFiles()
        if (tempFileNames != null) {
            for (tempFile in tempFileNames) {
                if (!tempFile.isDirectory()) {
                    m_imagesToSendSize += tempFile.length().toInt()
                }
            }
        }
    }


    protected fun loadDataAfterStart() {
        // читаем версии объектов из базы
        readVersions()
        // читаем настройки
        readSettings()
        // в случае необходимости запускаем лог GPS
        checkGpsUpdateEveryTime()
        m_bMayResetBase = true
        val g = MySingleton.getInstance()
        if (m_settings_DataFormat != g.m_DataFormat) {
            // Очистим базу, либо заполним ее тестовыми данными
            // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
            if (m_settings_DataFormat == "DM" || g.m_DataFormat == "DM") {
                resetBase()
            } else {
                m_settings_DataFormat = g.m_DataFormat
                writeSettings()
            }
            // 24.03.2017
            // после смены формата будет выполнен "запросить все"
            setNegativeVersions()

            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            // TODO 22.01.2019
            //getSupportLoaderManager().initLoader(ORDERS_LOADER_ID, null, this);
            //getSupportLoaderManager().initLoader(PAYMENTS_LOADER_ID, null, this);
            //getSupportLoaderManager().initLoader(MESSAGES_LOADER_ID, null, this);
            val fragmentManager = getSupportFragmentManager()
            val fragment = fragmentManager.findFragmentById(R.id.content_frame)

            if (fragment is OrdersListFragment) {
                val ordersListFragment = fragment
                ordersListFragment.restartLoaderForListView()
            }
            if (fragment is PaymentsListFragment) {
                val paymentsListFragment = fragment
                paymentsListFragment.restartLoaderForListView()
            }
            if (fragment is MessagesListFragment) {
                val messagesListFragment = fragment
                messagesListFragment.restartLoaderForListView()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // TODO Auto-generated method stub
        val g = MySingleton.getInstance()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        g.readPreferences(sharedPreferences)

        // Если изменился формат
        if (g.Common.m_app_theme != null && g.Common.m_app_theme != sharedPreferences.getString(
                "app_theme",
                "DARK"
            )
        ) {
            //Toast.makeText(MainActivity.this, "Тема приложения изменится при перезапуске программы!", Toast.LENGTH_SHORT).show();
            // До 03.08.2018
            //Toast.makeText(MainActivity.this, getString(R.string.message_theme_will_changed_after_restart), Toast.LENGTH_SHORT).show();
            //
            g.Common.m_app_theme = sharedPreferences.getString("app_theme", "DARK")
            if (g.Common.m_app_theme == "DARK") {
                setTheme(R.style.AppTheme)
            } else {
                setTheme(R.style.LightAppTheme)
            }
            recreate()
        }
        if (m_bMayResetBase && m_settings_DataFormat != g.m_DataFormat) {
            // Очистим базу, либо заполним ее тестовыми данными
            // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
            if (m_settings_DataFormat == "DM" || g.m_DataFormat == "DM") {
                resetBase()
            } else {
                m_settings_DataFormat = g.m_DataFormat
                writeSettings()
            }
            // 24.03.2017
            // после смены формата будет выполнен "запросить все"
            setNegativeVersions()
        }
        // 05.04.2019
        updatePageMessagesVisibility()

        checkNewAppVersionState()
    }

    override fun onDestroy() {
        val g = MySingleton.getInstance()
        if (g.MyDatabase.m_seance_closed) {
            val cv = ContentValues()
            cv.put("outgoing", g.MyDatabase.m_seance.toString())
            getContentResolver().insert(MTradeContentProvider.SEANCES_OUTGOING_CONTENT_URI, cv)
        }
        // С 03.08.2018
        if (m_bNeedCoord) {
            m_locationManager!!.removeUpdates(locListener)
        }
        if (m_bRegisteredEveryTimeUpdate) {
            m_locationManager!!.removeUpdates(locListenerEveryTime)
        }
        //
        unregisterInstallStateUpdListener()
        super.onDestroy()
    }

    /*
    //@SuppressLint("NewApi")
    @Override
    public void onTabChanged(String tabId) {
		//int i = tabHost.getCurrentTab();
		//switch (i)
		//{
		//case 0:
		//	break;
		//}
		//if (android.os.Build.VERSION.SDK_INT<11)
		//{
		//	ActivityCompat.invalidateOptionsMenu(MainActivity.this);
		//} else
		//{
		//	invalidateOptionsMenu();
		//}
        ActivityCompat.invalidateOptionsMenu(MainActivity.this);
        try {
            // из-за бага в версии android 3.2 меню не обновляется
            onPrepareOptionsMenu(g_options_menu);
        } catch (Exception e) {
        }
    }
    */
    private fun setCheckedMode(frameType: Int) {
        val navigationView = findViewById<View?>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val navMenu = navigationView.getMenu()

        when (frameType) {
            FRAME_TYPE_ROUTES -> navMenu.findItem(R.id.nav_routes).setChecked(true)
            FRAME_TYPE_ORDERS -> navMenu.findItem(R.id.nav_orders).setChecked(true)
            FRAME_TYPE_PAYMENTS -> navMenu.findItem(R.id.nav_payments).setChecked(true)
            FRAME_TYPE_EXCHANGE -> navMenu.findItem(R.id.nav_exchange).setChecked(true)
            FRAME_TYPE_MESSAGES -> navMenu.findItem(R.id.nav_messages).setChecked(true)
        }
    }

    private fun attachFrameType(frameType: Int) {
        var fragment: Fragment? = null
        var pageTitle: CharSequence? = null
        if (frameType == FRAME_TYPE_ROUTES) {
            fragment = RoutesListFragment()
            val args = Bundle()
            if (m_route_id != null) {
                args.putString("route_id", m_route_id.toString())
            } else {
                args.putString("route_id", null)
            }
            args.putString("route_date", m_route_date)
            args.putString("route_descr", m_route_descr)
            /*
            args.putInt("filter_orders", m_filter_orders ? 1 : 0);
            args.putInt("filter_refunds", m_filter_refunds ? 1 : 0);
            args.putInt("filter_distribs", m_filter_distribs ? 1 : 0);
            args.putString("filter_date_begin", m_filter_date_begin);
            args.putString("filter_date_end", m_filter_date_end);
            args.putInt("filter_type", m_filter_type);
            args.putInt("filter_date_type", m_filter_date_type);
            */
            fragment.setArguments(args)
            pageTitle = getString(R.string.tab_routes)
        } else if (frameType == FRAME_TYPE_ORDERS) {
            fragment = OrdersListFragment()
            val args = Bundle()
            if (m_client_id != null) {
                args.putString("client_id", m_client_id.toString())
            } else {
                args.putString("client_id", null)
            }
            args.putString("client_descr", m_client_descr)
            args.putInt("filter_orders", if (m_filter_orders) 1 else 0)
            args.putInt("filter_refunds", if (m_filter_refunds) 1 else 0)
            args.putInt("filter_distribs", if (m_filter_distribs) 1 else 0)
            args.putString("filter_date_begin", m_filter_date_begin)
            args.putString("filter_date_end", m_filter_date_end)
            args.putInt("filter_type", m_filter_type)
            args.putInt("filter_date_type", m_filter_date_type)
            fragment.setArguments(args)
            pageTitle = getString(R.string.tab_orders)
        } else if (frameType == FRAME_TYPE_PAYMENTS) {
            fragment = PaymentsListFragment()
            val args = Bundle()
            if (m_client_id != null) {
                args.putString("client_id", m_client_id.toString())
            } else {
                args.putString("client_id", null)
            }
            args.putString("client_descr", m_client_descr)
            fragment.setArguments(args)
            pageTitle = getString(R.string.tab_payments)
        } else if (frameType == FRAME_TYPE_EXCHANGE) {
            fragment = ExchangeFragment()
            val args = Bundle()
            //((ExchangeFragment)fragment).setExchangeState(m_exchangeState);
            //((ExchangeFragment)fragment).setLogText(m_exchangeLogText);
            args.putString("exchange_state", m_exchangeState)
            args.putStringArrayList("exchange_log_text", m_exchangeLogText)
            fragment.setArguments(args)
            pageTitle = getString(R.string.tab_exchange)
        } else if (frameType == FRAME_TYPE_MESSAGES) {
            fragment = MessagesListFragment()
            pageTitle = getString(R.string.tab_messages)
        }

        if (fragment != null) {
            val fragmentManager = getSupportFragmentManager()
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                .commitAllowingStateLoss()
            //mDrawerListView.setItemChecked(position, true);
            //mDrawerListView.setSelection(position);
            //setTitle(mItemTitles[position]);
            if (pageTitle != null) setTitle(pageTitle)
            //mDrawerLayout.closeDrawer(mDrawerListView);
        }

        m_current_frame_type = frameType
        invalidateOptionsMenu()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val g = MySingleton.getInstance()

        val id = item.getItemId()
        if (id == R.id.nav_routes) {
            attachFrameType(FRAME_TYPE_ROUTES)
        } else if (id == R.id.nav_orders) {
            attachFrameType(FRAME_TYPE_ORDERS)
        } else if (id == R.id.nav_payments) {
            attachFrameType(FRAME_TYPE_PAYMENTS)
        } else if (id == R.id.nav_exchange) {
            attachFrameType(FRAME_TYPE_EXCHANGE)
        } else if (id == R.id.nav_messages) {
            attachFrameType(FRAME_TYPE_MESSAGES)
        } else if (id == R.id.nav_settings) {
            val intent = Intent(this@MainActivity, PrefActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (g.m_preferences_from_ini) intent.putExtra("readOnly", true)
            startActivity(intent)
        } else if (id == R.id.nav_exit) {
            onCloseActivity()
            return true
        }

        val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun setTitle(title: CharSequence?) {
        mTitle = title
        getSupportActionBar()!!.setTitle(mTitle)
    }

    @Throws(IOException::class)
    fun getFileContents(theFile: File): String {
        val bytes = ByteArray(theFile.length().toInt())
        val `in`: InputStream = FileInputStream(theFile)
        var m = 0
        var n = 0
        while (m < bytes.size) {
            n = `in`.read(bytes, m, bytes.size - m)
            m += n
        }
        `in`.close()

        return String(bytes, charset("cp1251"))
    }


    protected fun deleteOrder(_id: Long): Boolean {
        val g = MySingleton.getInstance()
        var result = false
        var sdFile: File? = null
        var uid = ""
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, _id)

        val cursor = getContentResolver().query(singleUri, arrayOf<String>("uid"), null, null, null)
        if (cursor!!.moveToNext()) {
            val uid_Index = cursor.getColumnIndex("uid")
            uid = cursor.getString(uid_Index)
        }
        cursor.close()

        if (!g.Common.NEW_BACKUP_FORMAT) {
            // Удаление резервной копии заявки (текстовый файл)
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                if (!uid.isEmpty()) {
                    // получаем путь к SD
                    val sdPath = File(
                        Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/mtrade/orders"
                    )
                    sdFile = File(sdPath, uid + ".txt")
                }
            }
        }
        if (getContentResolver().delete(
                singleUri,
                E_ORDER_STATE.getCanBeDeletedConditionWhere(),
                E_ORDER_STATE.getCanBeDeletedArgs()
            ) > 0
        ) {
            result = true
            if (sdFile != null) {
                sdFile.delete()
            }
        }
        if (g.Common.NEW_BACKUP_FORMAT) {
            // До 13.09.2018
            //singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, _id);
            //getContentResolver().delete(singleUri, "editing_backup<>0", null);
            // После 13.09.2018
            getContentResolver().delete(
                MTradeContentProvider.ORDERS_CONTENT_URI,
                "old_id=? and editing_backup<>0",
                arrayOf<String>(_id.toString())
            )
        }

        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid)

        return result
    }

    protected fun deleteRefund(_id: Long): Boolean {
        var result = false
        //File sdFile = null;
        var uid = ""
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.REFUNDS_CONTENT_URI, _id)
        val cursor = getContentResolver().query(singleUri, arrayOf<String>("uid"), null, null, null)
        if (cursor!!.moveToNext()) {
            val uid_Index = cursor.getColumnIndex("uid")
            uid = cursor.getString(uid_Index)
        }
        cursor.close()

        // Удаление резервной копии заявки (текстовый файл)
        // ... нет
        if (getContentResolver().delete(
                singleUri,
                E_REFUND_STATE.getCanBeDeletedConditionWhere(),
                E_REFUND_STATE.getCanBeDeletedArgs()
            ) > 0
        ) {
            result = true
            /*
	    	if (sdFile!=null)
	    	{
	    		sdFile.delete();
	    	}
	    	*/
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid)

        return result
    }

    protected fun deleteDistribs(_id: Long): Boolean {
        var result = false
        //File sdFile = null;
        var uid = ""
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTRIBS_CONTENT_URI, _id)
        val cursor = getContentResolver().query(singleUri, arrayOf<String>("uid"), null, null, null)
        if (cursor!!.moveToNext()) {
            val uid_Index = cursor.getColumnIndex("uid")
            uid = cursor.getString(uid_Index)
        }
        cursor.close()
        // Удаление резервной копии заявки (текстовый файл)
        // ... нет
        if (getContentResolver().delete(
                singleUri,
                E_DISTRIBS_STATE.getDistribsCanBeDeletedConditionWhere(),
                E_DISTRIBS_STATE.getDistribsCanBeDeletedArgs()
            ) > 0
        ) {
            result = true
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid)

        return result
    }

    protected fun deletePayment(_id: Long): Boolean {
        var result = false
        var uid = ""
        val singleUri =
            ContentUris.withAppendedId(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, _id)
        val cursor = getContentResolver().query(singleUri, arrayOf<String>("uid"), null, null, null)
        if (cursor!!.moveToNext()) {
            val uid_Index = cursor.getColumnIndex("uid")
            uid = cursor.getString(uid_Index)
        }
        cursor.close()
        if (getContentResolver().delete(
                singleUri,
                E_PAYMENT_STATE.getPaymentCanBeDeletedConditionWhere(),
                E_PAYMENT_STATE.getPaymentCanBeDeletedArgs()
            ) > 0
        ) {
            result = true
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid)

        return result
    }

    protected fun deleteMessage(_id: Long) {
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.MESSAGES_CONTENT_URI, _id)
        //getContentResolver().delete(singleUri, null, null);
        val cv = ContentValues()
        cv.put("isMark", 1)
        getContentResolver().update(singleUri, cv, null, null)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val g = MySingleton.getInstance()

        val info = menuInfo as AdapterContextMenuInfo

        //menu.setHeaderTitle(getString(R.string.menu_context_title));
        val inflater = getMenuInflater()
        when (v.getId()) {
            R.id.listViewOrders -> {
                inflater.inflate(R.menu.main_orders_list, menu)

                val itemCopyOrder = menu.findItem(R.id.action_copy_order)
                itemCopyOrder.setVisible(false)

                val itemCopyOrderToRefund = menu.findItem(R.id.action_copy_order_to_refund)
                itemCopyOrderToRefund.setVisible(false)

                val itemCancelOrder = menu.findItem(R.id.action_cancel_order)
                itemCancelOrder.setVisible(false)

                val itemDeleteOrder = menu.findItem(R.id.action_delete_order)
                itemDeleteOrder.setVisible(false)

                val itemCopyRefund = menu.findItem(R.id.action_copy_refund)
                itemCopyRefund.setVisible(false)

                val itemCancelRefund = menu.findItem(R.id.action_cancel_refund)
                itemCancelRefund.setVisible(false)

                val itemDeleteRefund = menu.findItem(R.id.action_delete_refund)
                itemDeleteRefund.setVisible(false)

                val itemCancelDistribs = menu.findItem(R.id.action_cancel_distribs)
                itemCancelDistribs.setVisible(false)

                val itemDeleteDistribs = menu.findItem(R.id.action_delete_distribs)
                itemDeleteDistribs.setVisible(false)


                //selectedWord = ((TextView) info.targetView).getText().toString();
                val _id = info.id
                val singleUri =
                    ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, _id)
                val cursor = getContentResolver().query(
                    singleUri,
                    arrayOf<String>("iddocdef", "state"),
                    null,
                    null,
                    null
                )
                if (cursor!!.moveToNext()) {
                    val iddocdefIndex = cursor.getColumnIndex("iddocdef")
                    val stateIndex = cursor.getColumnIndex("state")
                    val iddocdef = cursor.getInt(iddocdefIndex)
                    when (iddocdef) {
                        0 -> {
                            itemCopyOrder.setVisible(true)
                            if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                                itemCopyOrderToRefund.setVisible(true)
                            }
                            val state = E_ORDER_STATE.fromInt(cursor.getInt(stateIndex))
                            if (E_ORDER_STATE.getCanBeDeleted(state)) {
                                itemDeleteOrder.setVisible(true)
                            }
                            if (E_ORDER_STATE.getCanBeCanceled(state)) {
                                itemCancelOrder.setVisible(true)
                            }
                        }

                        1 -> {
                            val state = E_REFUND_STATE.fromInt(cursor.getInt(stateIndex))
                            itemCopyRefund.setVisible(true)
                            if (E_REFUND_STATE.getCanBeDeleted(state)) {
                                itemDeleteRefund.setVisible(true)
                            }
                            if (E_REFUND_STATE.getCanBeCanceled(state)) {
                                itemCancelRefund.setVisible(true)
                            }
                        }

                        3 -> {
                            val state = E_DISTRIBS_STATE.fromInt(cursor.getInt(stateIndex))
                            if (E_DISTRIBS_STATE.getDistribsCanBeDeleted(state)) {
                                itemDeleteDistribs.setVisible(true)
                            }
                            if (E_DISTRIBS_STATE.getDistribsCanBeCanceled(state)) {
                                itemCancelDistribs.setVisible(true)
                            }
                        }
                    }
                }
                cursor.close()
            }

            R.id.listViewPayments -> {
                inflater.inflate(R.menu.main_payments_list, menu)

                val itemCancelPayment = menu.findItem(R.id.action_cancel_payment)
                itemCancelPayment.setVisible(false)

                val itemDeletePayment = menu.findItem(R.id.action_delete_payment)
                itemDeletePayment.setVisible(false)

                val _id = info.id
                val singleUri =
                    ContentUris.withAppendedId(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, _id)
                val cursor = getContentResolver().query(
                    singleUri,
                    arrayOf<String>("state"),
                    null,
                    null,
                    null
                )
                if (cursor!!.moveToNext()) {
                    val stateIndex = cursor.getColumnIndex("state")
                    val state = E_PAYMENT_STATE.fromInt(cursor.getInt(stateIndex))
                    if (E_PAYMENT_STATE.getPaymentCanBeDeleted(state)) {
                        itemDeletePayment.setVisible(true)
                    }
                    // TODO
                    /*
				if (E_PAYMENT_STATE.getPaymentCanBeCanceled(state))
				{
					itemCancelPayment.setVisible(true);
				}
				*/
                }
                cursor.close()
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.action_copy_order -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                var _id = info!!.id
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        _id
                    ), arrayOf<String>("order_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    _id = cursor.getLong(0)
                } else {
                    _id = 0
                }
                if (_id != 0L) {
                    val g = MySingleton.getInstance()
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled()) editOrder(_id, true, null, null)
                    } else editOrder(_id, true, null, null)
                }
                return true
            }

            R.id.action_copy_order_to_refund -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                var _id = info!!.id
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        _id
                    ), arrayOf<String>("order_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    _id = cursor.getLong(0)
                } else {
                    _id = 0
                }
                cursor.close()
                if (_id != 0L) {
                    val g = MySingleton.getInstance()
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled()) editRefund(0, false, _id, m_client_id, MyID())
                    } else editRefund(0, false, _id, m_client_id, MyID())
                }
                return true
            }

            R.id.action_cancel_order -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_CANCEL_CURRENT_ORDER, bundle)
                return true
            }

            R.id.action_delete_order -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_DELETE_CURRENT_ORDER, bundle)
                return true
            }

            R.id.action_copy_refund -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                var _id = info!!.id
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        _id
                    ), arrayOf<String>("refund_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    _id = cursor.getLong(0)
                } else {
                    _id = 0
                }
                if (_id != 0L) {
                    val g = MySingleton.getInstance()
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled()) editRefund(_id, true, 0, m_client_id, MyID())
                    } else editRefund(_id, true, 0, m_client_id, MyID())
                }
                return true
            }

            R.id.action_cancel_refund -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_CANCEL_CURRENT_REFUND, bundle)
                return true
            }

            R.id.action_delete_refund -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_DELETE_CURRENT_REFUND, bundle)
                return true
            }

            R.id.action_cancel_distribs -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_CANCEL_CURRENT_DISTRIBS, bundle)
                return true
            }

            R.id.action_delete_distribs -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_DELETE_CURRENT_DISTRIBS, bundle)
                return true
            }

            R.id.action_copy_payment -> {
                val g = MySingleton.getInstance()
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val _id = info!!.id
                if (g.Common.PRODLIDER) {
                    if (checkGeoEnabled()) editPayment(_id, true, null, null)
                } else editPayment(_id, true, null, null)
                return true
            }

            R.id.action_cancel_payment -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_CANCEL_CURRENT_PAYMENT, bundle)
                return true
            }

            R.id.action_delete_payment -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_DELETE_CURRENT_PAYMENT, bundle)
                return true
            }

            R.id.action_delete_message -> {
                val info = item.getMenuInfo() as AdapterContextMenuInfo?
                val bundle = Bundle()
                bundle.putLong("_id", info!!.id)
                showDialog(IDD_DELETE_CURRENT_MESSAGE, bundle)
                return true
            }

        }
        return super.onContextItemSelected(item)
    }

    fun createOrderPre(client_id: String?, distr_point_id: String?) {
        val intent = Intent(this, OrderPreActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("client_id", client_id)
        intent.putExtra("distr_point_id", distr_point_id)
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //startActivityForResult(intent, EDIT_ORDER_PRE_REQUEST);
        editOrderPreActivityResultLauncher!!.launch(intent)
    }

    fun editOrder(id: Long, bCopyOrder: Boolean, client_id: String?, distr_point_id: String?) {
        //Intent intent=new Intent(this, OrderActivity.class);
        //startActivityForResult(intent, EDIT_ORDER_REQUEST);
        val intent = Intent(this, OrderActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("client_id", client_id)
        intent.putExtra("distr_point_id", distr_point_id)

        //intent.putExtra("id", "");
        OrdersHelpers.PrepareData(
            intent,
            this,
            id,
            MyID(client_id),
            MyID(distr_point_id),
            bCopyOrder
        )

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_ORDER_REQUEST);
        editOrderActivityResultLauncher!!.launch(intent)
    }

    fun cancelOrder(id: Long) {
        val g = MySingleton.getInstance()
        var uid = ""
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, id)

        val cursor = getContentResolver().query(singleUri, arrayOf<String>("uid"), null, null, null)
        if (cursor!!.moveToNext()) {
            val uid_Index = cursor.getColumnIndex("uid")
            uid = cursor.getString(uid_Index)
        }
        cursor.close()

        val cv = ContentValues()
        cv.put("state", E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value())
        cv.put("closed_not_full", 0)
        getContentResolver().update(
            singleUri,
            cv,
            E_ORDER_STATE.getCanBeCanceledConditionWhere(),
            E_ORDER_STATE.getCanBeCanceledArgs()
        )
        // Удаляем такую заявку из текстовых файлов
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            if (!uid.isEmpty()) {
                // получаем путь к SD
                val sdPath = File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders"
                )
                val sdFile = File(sdPath, uid + ".txt")
                sdFile.delete()
            }
        }

        // если режим маршрута, обновим документ
        updateDocumentInRoute(
            g.MyDatabase.m_order_editing.distr_point_id.toString(),
            g.MyDatabase.m_order_editing
        )
    }

    fun cancelRefund(id: Long) {
        val g = MySingleton.getInstance()
        val singleUri = ContentUris.withAppendedId(
            MTradeContentProvider.REFUNDS_CONTENT_URI,
            m_refund_id_to_process
        )

        val cv = ContentValues()
        cv.put("state", E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value())
        cv.put("closed_not_full", 0)
        getContentResolver().update(
            singleUri,
            cv,
            E_REFUND_STATE.getCanBeCanceledConditionWhere(),
            E_REFUND_STATE.getCanBeCanceledArgs()
        )

        // если режим маршрута, обновим документ
        updateDocumentInRoute(
            g.MyDatabase.m_refund_editing.distr_point_id.toString(),
            g.MyDatabase.m_refund_editing
        )
    }

    fun cancelDistribs(id: Long) {
        val g = MySingleton.getInstance()
        val singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTRIBS_CONTENT_URI, id)

        val cv = ContentValues()
        cv.put("state", E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value())
        getContentResolver().update(
            singleUri,
            cv,
            E_DISTRIBS_STATE.getDistribsCanBeCanceledConditionWhere(),
            E_DISTRIBS_STATE.getDistribsCanBeCanceledArgs()
        )

        // если режим маршрута, обновим документ
        updateDocumentInRoute(
            g.MyDatabase.m_distribs_editing.distr_point_id.toString(),
            g.MyDatabase.m_distribs_editing
        )
    }

    private fun editPayment(id: Long, bCopyPayment: Boolean, client_id: MyID?, distr_point_id: MyID?) {
        val g = MySingleton.getInstance()
        var bCreatedPayment = false

        val intent = Intent(this, PaymentActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //intent.putExtra("id", "");
        val rec = g.MyDatabase.m_payment_editing
        g.MyDatabase.m_payment_editing_id = id
        g.MyDatabase.m_payment_editing_modified = false
        var cursor: Cursor?
        val result = TextDatabase.ReadPaymentBy_Id(
            getContentResolver(),
            rec,
            g.MyDatabase.m_payment_editing_id
        )
        if (result == false) {
            bCreatedPayment = true
            rec.client_id = MyID()
            rec.stuff_client_name = getResources().getString(R.string.client_not_set)
            rec.stuff_client_address = ""
            rec.stuff_email = ""
            rec.manager_id = MyID()
            rec.stuff_manager_name = getResources().getString(R.string.manager_not_set)
            if (client_id != null && !client_id.isEmpty()) {
                rec.client_id = client_id.clone()
                // Прочитаем название контрагента и id куратора
                val clientCursor = getContentResolver().query(
                    MTradeContentProvider.CLIENTS_CONTENT_URI,
                    arrayOf<String>("descr", "address", "curator_id", "flags", "email_for_cheques"),
                    "id=?",
                    arrayOf<String>(g.MyDatabase.m_payment_editing.client_id.toString()),
                    null
                )
                if (clientCursor!!.moveToNext()) {
                    val descrIndex = clientCursor.getColumnIndex("descr")
                    val addressIndex = clientCursor.getColumnIndex("address")
                    val curator_idIndex = clientCursor.getColumnIndex("curator_id")
                    val email_for_chequesIndex = clientCursor.getColumnIndex("email_for_cheques")
                    //int flags_Index=clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex)
                    rec.stuff_client_address = clientCursor.getString(addressIndex)
                    rec.manager_id = MyID(clientCursor.getString(curator_idIndex))
                    rec.stuff_email = clientCursor.getString(email_for_chequesIndex)
                    // Наименование менеджера
                    cursor = getContentResolver().query(
                        MTradeContentProvider.CURATORS_CONTENT_URI,
                        arrayOf<String>("descr"),
                        "id=?",
                        arrayOf<String>(rec.manager_id.toString()),
                        null
                    )
                    if (cursor!!.moveToNext()) {
                        val descrIndex0 = cursor.getColumnIndex("descr")
                        val newWord = cursor.getString(descrIndex0)
                        rec.stuff_manager_name = newWord
                    }
                    cursor.close()
                } else {
                    rec.stuff_client_name = "{" + rec.client_id.toString() + "}"
                }
                clientCursor.close()
            }
            rec.distr_point_id = distr_point_id?.clone()
            rec.comment = ""
            rec.stuff_debt = 0.0
            rec.stuff_debt_past = 0.0
            rec.stuff_debt_past30 = 0.0
            rec.stuff_agreement_debt = 0.0
            rec.stuff_agreement_debt_past = 0.0
            rec.agreement_id = MyID()
            rec.sumDoc = 0.0
            rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set)
            rec.stuff_organization_name = getResources().getString(R.string.organization_not_set)
            rec.vicarious_power_id = MyID()
            rec.vicarious_power_descr = getResources().getString(R.string.vicarious_power_not_set)
        } else {
            if (bCopyPayment) {
                bCreatedPayment = true
            }
            // Наименование клиента
            cursor = getContentResolver().query(
                MTradeContentProvider.CLIENTS_CONTENT_URI,
                arrayOf<String>("descr", "address", "flags", "email_for_cheques"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_payment_editing.client_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val addressIndex = cursor.getColumnIndex("address")
                val flagsIndex = cursor.getColumnIndex("flags")
                val email_for_chequesIndex = cursor.getColumnIndex("email_for_cheques")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_client_name = newWord
                rec.stuff_client_address = cursor.getString(addressIndex)
                rec.stuff_email = cursor.getString(email_for_chequesIndex)
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set)
                rec.stuff_client_address = ""
                rec.stuff_email = ""
            }
            cursor.close()
            // Наименование договора
            var organizationId = MyID()
            cursor = getContentResolver().query(
                MTradeContentProvider.AGREEMENTS_CONTENT_URI,
                arrayOf<String>("descr", "sale_id", "organization_id"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_payment_editing.agreement_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val organization_idIndex = cursor.getColumnIndex("organization_id")
                val descr = cursor.getString(descrIndex)
                rec.stuff_agreement_name = descr
                organizationId = MyID(cursor.getString(organization_idIndex))
            } else {
                rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set)
            }
            cursor.close()
            // Наименование организации
            cursor = getContentResolver().query(
                MTradeContentProvider.ORGANIZATIONS_CONTENT_URI,
                arrayOf<String>("descr"),
                "id=?",
                arrayOf<String>(organizationId.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_organization_name = newWord
            } else {
                if (rec.stuff_organization_name == null || rec.stuff_organization_name.isEmpty()) {
                    rec.stuff_organization_name =
                        getResources().getString(R.string.organization_not_set)
                }
            }
            cursor.close()
            // Наименование менеджера
            cursor = getContentResolver().query(
                MTradeContentProvider.CURATORS_CONTENT_URI,
                arrayOf("descr"),
                "id=?",
                arrayOf(g.MyDatabase.m_payment_editing.manager_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_manager_name = newWord
            } else {
                if (rec.stuff_manager_name.isEmpty()) {
                    rec.stuff_manager_name = getResources().getString(R.string.manager_not_set)
                }
            }
            cursor.close()
        }
        if (bCreatedPayment) {
            // В том числе копированием
            rec.uid = UUID.randomUUID()
            rec.numdoc = getString(R.string.numdoc_new) // "НОВЫЙ"
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            //java.util.Date work_date=DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
            rec.datedoc = Common.getDateTimeAsString14(null)
            rec.id = MyID() // Это код, присвоенный 1С
            rec.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED
            g.MyDatabase.m_payment_editing_id =
                0 // Чтобы при записи не перезаписать уже существующий документ
            rec.version = 0
            rec.version_ack = 0
            rec.versionPDA = 0
            rec.versionPDA_ack = 0
            //
            rec.latitude = 0.0
            rec.longitude = 0.0
            rec.gpsaccuracy = 0.0
            rec.gpsstate = -1
            rec.accept_coord = 1
        }
        // Долг контрагента
        val cursorDebt = getContentResolver().query(
            MTradeContentProvider.SALDO_CONTENT_URI,
            arrayOf<String>("saldo", "saldo_past", "saldo_past30"),
            "client_id=?",
            arrayOf<String>(rec.client_id.toString()),
            null
        )
        if (cursorDebt!!.moveToNext()) {
            val indexDebt = cursorDebt.getColumnIndex("saldo")
            val indexDebtPast = cursorDebt.getColumnIndex("saldo_past")
            val indexDebtPast30 = cursorDebt.getColumnIndex("saldo_past30")
            rec.stuff_debt = cursorDebt.getDouble(indexDebt)
            rec.stuff_debt_past = cursorDebt.getDouble(indexDebtPast)
            rec.stuff_debt_past30 = cursorDebt.getDouble(indexDebtPast30)
        } else {
            rec.stuff_debt = 0.0
            rec.stuff_debt_past = 0.0
            rec.stuff_debt_past30 = 0.0
        }
        cursorDebt.close()

        // Долг контрагента по договору
        if (g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) {
            var agreement_debt = 0.0
            var agreement_debt_past = 0.0
            val cursorAgreementDebt = getContentResolver().query(
                MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI,
                arrayOf<String>("saldo", "saldo_past"),
                "agreement_id=?",
                arrayOf<String>(rec.agreement_id.toString()),
                null
            )
            val saldoIndex = cursorAgreementDebt!!.getColumnIndex("saldo")
            val saldoPastIndex = cursorAgreementDebt.getColumnIndex("saldo_past")
            while (cursorAgreementDebt.moveToNext()) {
                agreement_debt = agreement_debt + cursorAgreementDebt.getDouble(saldoIndex)
                agreement_debt_past =
                    agreement_debt_past + cursorAgreementDebt!!.getDouble(saldoPastIndex)
            }
            cursorAgreementDebt.close()
            rec.stuff_agreement_debt = agreement_debt
            rec.stuff_agreement_debt_past = agreement_debt_past
        }

        //startActivityForResult(intent, EDIT_PAYMENT_REQUEST);
        editPaymentActivityResultLauncher!!.launch(intent)
    }


    private fun editRefund(
        id: Long,
        bCopyRefund: Boolean,
        order_id: Long,
        client_id: MyID?,
        distr_point_id: MyID?
    ) {
        val g = MySingleton.getInstance()
        var bCreatedRefund = false
        val intent = Intent(this, RefundActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val rec = g.MyDatabase.m_refund_editing
        g.MyDatabase.m_refund_editing_id = id
        g.MyDatabase.m_refund_editing_modified = false
        var cursor: Cursor?
        var needReadDescrs = false
        val result = TextDatabase.ReadRefundBy_Id(
            getContentResolver(),
            rec,
            g.MyDatabase.m_refund_editing_id
        )
        /*
		if (result)
		{
			Intent test = new Intent();
			test.putExtra("test1", rec); // parcelable
			RefundRecord rec2=test.getParcelableExtra("test1");
			g.MyDatabase.m_refund_editing=rec2;
		}
		*/
        //
        if (result == false) {
            bCreatedRefund = true
            rec.Clear()
            if (client_id == null || client_id.isEmpty()) {
                rec.client_id = MyID()
                rec.stuff_client_name = getResources().getString(R.string.client_not_set)
                rec.stuff_client_address = ""
                rec.curator_id = MyID()
                rec.stuff_curator_name = getResources().getString(R.string.curator_not_set)
                rec.stuff_select_account = false
            } else {
                rec.client_id = client_id.clone()
                rec.curator_id = MyID()
                // Прочитаем название контрагента и id куратора
                val clientCursor = getContentResolver().query(
                    MTradeContentProvider.CLIENTS_CONTENT_URI,
                    arrayOf<String>("descr", "address", "curator_id", "flags"),
                    "id=?",
                    arrayOf<String>(g.MyDatabase.m_refund_editing.client_id.toString()),
                    null
                )
                if (clientCursor != null && clientCursor.moveToNext()) {
                    val descrIndex = clientCursor.getColumnIndex("descr")
                    val addressIndex = clientCursor.getColumnIndex("address")
                    val curator_idIndex = clientCursor.getColumnIndex("curator_id")
                    val flags_Index = clientCursor.getColumnIndex("flags")
                    rec.stuff_client_name = clientCursor.getString(descrIndex)
                    rec.stuff_client_address = clientCursor.getString(addressIndex)
                    rec.curator_id = MyID(clientCursor.getString(curator_idIndex))
                    rec.stuff_select_account = (clientCursor.getInt(flags_Index) and 2) != 0
                }
            }
            if (distr_point_id == null || distr_point_id.isEmpty()) {
                rec.distr_point_id = MyID()
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set)
            } else {
                rec.distr_point_id = distr_point_id.clone()
                // Наименование торговой точки
                cursor = getContentResolver().query(
                    MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
                    arrayOf<String>("descr"),
                    "id=?",
                    arrayOf<String>(rec.distr_point_id.toString()),
                    null
                )
                if (cursor!!.moveToNext()) {
                    val descrIndex = cursor.getColumnIndex("descr")
                    val newWord = cursor.getString(descrIndex)
                    rec.stuff_distr_point_name = newWord
                } else {
                    rec.stuff_distr_point_name = "{" + rec.distr_point_id + "}"
                }
                cursor.close()
            }
            rec.comment = ""
            rec.stuff_debt = 0.0
            rec.stuff_debt_past = 0.0
            rec.stuff_debt_past30 = 0.0
            rec.agreement_id = MyID()
            rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set)
            rec.stuff_organization_name = getResources().getString(R.string.organization_not_set)
            rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set)
            rec.stock_id = TextDatabase.getDefaultStock(getContentResolver(), rec.stuff_stock_descr)

            if (order_id != 0L) {
                // на основании заказа
                val order = OrderRecord()
                val resultOrder = TextDatabase.ReadOrderBy_Id(getContentResolver(), order, order_id)
                // вообще здесь заказ всегда будет прочитан
                if (resultOrder) {
                    rec.client_id = order.client_id
                    rec.stuff_client_name = order.stuff_client_name
                    rec.stuff_client_address = order.stuff_client_address
                    rec.curator_id = order.curator_id
                    rec.stuff_curator_name = order.stuff_curator_name
                    rec.stuff_select_account = order.stuff_select_account
                    rec.agreement_id = order.agreement_id
                    rec.stuff_agreement_name = order.stuff_agreement_name
                    rec.stuff_organization_name = order.stuff_organization_name
                    rec.stuff_distr_point_name = order.stuff_distr_point_name
                    rec.stock_id = order.stock_id

                    for (line in order.lines) {
                        val refundLine = RefundLineRecord()
                        refundLine.nomenclature_id = line.nomenclature_id
                        refundLine.quantity_requested = line.quantity_requested
                        refundLine.quantity = line.quantity_requested
                        refundLine.k = line.k
                        refundLine.ed = line.ed
                        refundLine.stuff_nomenclature = line.stuff_nomenclature
                        refundLine.stuff_weight_k_1 = line.stuff_weight_k_1
                        refundLine.temp_quantity = line.temp_quantity
                        refundLine.stuff_nomenclature_flags = line.stuff_nomenclature_flags
                        refundLine.comment_in_line = ""
                        rec.lines.add(refundLine)
                    }

                    needReadDescrs = true
                }
            }
        } else {
            if (bCopyRefund) {
                bCreatedRefund = true
                // Проходим по строкам и выставляем начальное количество
                for (line in rec.lines) {
                    if (line.quantity_requested > line.quantity) line.quantity =
                        line.quantity_requested
                }
            }
            if (bCopyRefund || rec.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
                // Заказ восстановлен, пересчитаем хотя бы вес
                // а для скопированных вес считали всегда
                rec.weightDoc = TextDatabase.GetRefundWeight(getContentResolver(), rec, null, false)
            }
            needReadDescrs = true
        }
        // в случае, если введен копированием (с другого возврата или на основании заказа)
        if (needReadDescrs) {
            // Наименование клиента
            cursor = getContentResolver().query(
                MTradeContentProvider.CLIENTS_CONTENT_URI,
                arrayOf<String>("descr", "address", "flags"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_refund_editing.client_id.toString()),
                null
            )
            if (cursor != null && cursor.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val addressIndex = cursor.getColumnIndex("address")
                val flagsIndex = cursor.getColumnIndex("flags")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_client_name = newWord
                rec.stuff_client_address = cursor.getString(addressIndex)
                rec.stuff_select_account = (cursor.getInt(flagsIndex) and 2) != 0
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set)
                rec.stuff_client_address = ""
                rec.stuff_select_account = false
            }
            cursor!!.close()
            // Наименование договора
            cursor = getContentResolver().query(
                MTradeContentProvider.AGREEMENTS_CONTENT_URI,
                arrayOf<String>("descr", "sale_id"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_refund_editing.agreement_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val descr = cursor.getString(descrIndex)
                rec.stuff_agreement_name = descr
            } else {
                rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set)
            }
            cursor.close()
            // Наименование организации
            cursor = getContentResolver().query(
                MTradeContentProvider.ORGANIZATIONS_CONTENT_URI,
                arrayOf<String>("descr"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_refund_editing.organization_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_organization_name = newWord
            } else {
                rec.stuff_organization_name =
                    getResources().getString(R.string.organization_not_set)
            }
            cursor.close()
            // Наименование торговой точки
            cursor = getContentResolver().query(
                MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
                arrayOf<String>("descr"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_refund_editing.distr_point_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_distr_point_name = newWord
            } else {
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set)
            }
            cursor.close()
        }
        g.MyDatabase.m_refund_editing_created = bCreatedRefund
        if (bCreatedRefund) {
            // В том числе копированием
            rec.uid = UUID.randomUUID()
            rec.numdoc = getString(R.string.numdoc_new) // "НОВЫЙ"

            val date = Date()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

            val work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime()
            if (Common.MyDateFormat("yyyyMMdd", date) == Common.MyDateFormat(
                    "yyyyMMdd",
                    work_date
                )
            ) {
                // День совпал - берем текущее время
                rec.datedoc = Common.getDateTimeAsString14(date)
            } else {
                // Берем рабочую дату
                rec.datedoc = Common.getDateTimeAsString14(work_date)
            }
            rec.comment_closing = ""
            rec.id = MyID() // Это код, присвоенный 1С
            rec.state = E_REFUND_STATE.E_REFUND_STATE_CREATED
            g.MyDatabase.m_refund_editing_id =
                0 // Чтобы при записи не перезаписать уже существующий документ
            rec.dont_need_send = 0
            rec.version = 0
            rec.version_ack = 0
            rec.versionPDA = 0
            rec.versionPDA_ack = 0

            if (!g.Common.NEW_BACKUP_FORMAT && g.Common.BACKUP_NOT_SAVED_ORDERS) {
                rec.state = E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED
                g.MyDatabase.m_refund_editing_id = TextDatabase.SaveRefundSQL(
                    getContentResolver(),
                    g.MyDatabase.m_refund_editing,
                    g.MyDatabase.m_refund_editing_id
                )
            }
            //
            rec.latitude = 0.0
            rec.longitude = 0.0
            rec.gpsaccuracy = 0.0
            rec.gpsstate = -1
            rec.accept_coord = 1
        }
        // Долг контрагента
        cursor = getContentResolver().query(
            MTradeContentProvider.SALDO_CONTENT_URI,
            arrayOf<String>("saldo", "saldo_past", "saldo_past30"),
            "client_id=?",
            arrayOf<String>(g.MyDatabase.m_refund_editing.client_id.toString()),
            null
        )
        if (cursor != null && cursor.moveToNext()) {
            val indexDebt = cursor.getColumnIndex("saldo")
            val indexDebtPast = cursor.getColumnIndex("saldo_past")
            val indexDebtPast30 = cursor.getColumnIndex("saldo_past30")
            g.MyDatabase.m_refund_editing.stuff_debt = cursor.getDouble(indexDebt)
            g.MyDatabase.m_refund_editing.stuff_debt_past = cursor.getDouble(indexDebtPast)
            g.MyDatabase.m_refund_editing.stuff_debt_past30 = cursor.getDouble(indexDebtPast30)
        } else {
            g.MyDatabase.m_refund_editing.stuff_debt = 0.0
            g.MyDatabase.m_refund_editing.stuff_debt_past = 0.0
            g.MyDatabase.m_refund_editing.stuff_debt_past30 = 0.0
        }
        cursor!!.close()
        // Куратор
        cursor = getContentResolver().query(
            MTradeContentProvider.CURATORS_CONTENT_URI,
            arrayOf<String>("descr"),
            "id=?",
            arrayOf<String>(rec.curator_id.toString()),
            null
        )
        if (cursor != null && cursor.moveToNext()) {
            val indexDescr = cursor.getColumnIndex("descr")
            g.MyDatabase.m_refund_editing.stuff_curator_name = cursor.getString(indexDescr)
        } else {
            g.MyDatabase.m_refund_editing.stuff_curator_name =
                getResources().getString(R.string.curator_not_set)
        }
        cursor!!.close()

        // Список всех возможных типов учета
        val list_accounts_descr = ArrayList<String?>()
        val list_accounts_id = ArrayList<String?>()

        list_accounts_descr.add("УПР")
        list_accounts_id.add("0")
        list_accounts_descr.add("ОБЩ")
        list_accounts_id.add("1")
        if (g.Common.MEGA) {
            list_accounts_descr.add("Кега")
            list_accounts_id.add("2")
            list_accounts_descr.add("БПЗ")
            list_accounts_id.add("3")
        }

        // Склад
        var defaultStocksCount = 0
        var defaultStock = -1
        var defaultActionStocksCount = 0
        var defaultActionStock = -1
        var foundStock = -1
        // Список всех возможных складов
        val list_stocks_descr = ArrayList<String?>()
        val list_stocks_id = ArrayList<String?>()
        cursor = getContentResolver().query(
            MTradeContentProvider.STOCKS_CONTENT_URI,
            arrayOf<String>("descr", "id", "flags"),
            null,
            null,
            "descr ASC"
        )

        list_stocks_descr.add(getResources().getString(R.string.item_not_set))
        list_stocks_id.add(MyID().toString())

        if (cursor != null) {
            val descrIndex = cursor.getColumnIndex("descr")
            val idIndex = cursor.getColumnIndex("id")
            val flagsIndex = cursor.getColumnIndex("flags")

            while (cursor.moveToNext()) {
                val stockDescr = cursor.getString(descrIndex)
                val stockId = cursor.getString(idIndex)
                val flags = cursor.getInt(flagsIndex)
                if (rec.stock_id.m_id == stockId) {
                    foundStock = list_stocks_descr.size
                }
                // по умолчанию
                if ((flags and 1) != 0) {
                    if ((flags and 2) != 0) {
                        // по умолчанию акционный склад
                        defaultActionStocksCount++
                        defaultActionStock = list_stocks_descr.size
                    } else {
                        // по умолчанию обычный склад
                        defaultStocksCount++
                        defaultStock = list_stocks_descr.size
                    }
                }
                list_stocks_descr.add(stockDescr)
                list_stocks_id.add(stockId)
            }

            // Склад не найден
            if (foundStock <= 0) {
                if (defaultStocksCount == 1 && bCreatedRefund) {
                    // Установим этот склад по умолчанию
                    rec.stock_id = MyID(list_stocks_id.get(defaultStock))
                    // что делать с акционными складами по умолчанию пока не понятно
                } else {
                    // Сбросим склад, если он не найден и это не новый документ
                    rec.stock_id = MyID()
                }
            }
            cursor.close()
        }
        val bundle = Bundle()
        //bundle.putStringArray("list_organizations_descr",(String[])list_organizations_descr.toArray(new String[0]));
        //bundle.putStringArray("list_organizations_id",(String[])list_organizations_id.toArray(new String[0]));
        bundle.putStringArray(
            "list_accounts_descr",
            list_accounts_descr.toTypedArray<String?>()
        )
        bundle.putStringArray("list_accounts_id", list_accounts_id.toTypedArray<String?>())

        bundle.putStringArray("list_stocks_descr", list_stocks_descr.toTypedArray<String?>())
        bundle.putStringArray("list_stocks_id", list_stocks_id.toTypedArray<String?>())
        intent.putExtra("extrasBundle", bundle)

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_REFUND_REQUEST);
        editRefundActivityResultLauncher!!.launch(intent)
    }


    private fun editDistribs(id: Long, bCopyDistribs: Boolean, client_id: MyID?, distr_point_id: MyID?) {
        val g = MySingleton.getInstance()
        var bCreatedDistribs = false
        val intent = Intent(this, DistribsActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val rec = g.MyDatabase.m_distribs_editing
        g.MyDatabase.m_distribs_editing_id = id
        g.MyDatabase.m_distribs_editing_modified = false
        var cursor: Cursor?
        var needReadDescrs = false
        val result = TextDatabase.ReadDistribsBy_Id(
            getContentResolver(),
            rec,
            g.MyDatabase.m_distribs_editing_id
        )
        if (result == false) {
            bCreatedDistribs = true
            rec.Clear()
            if (client_id == null || client_id.isEmpty()) {
                rec.client_id = MyID()
                rec.stuff_client_name = getResources().getString(R.string.client_not_set)
                rec.stuff_client_address = ""
                rec.curator_id = MyID()
                rec.stuff_curator_name = getResources().getString(R.string.curator_not_set)
            } else {
                rec.client_id = client_id.clone()
                rec.curator_id = MyID()
                // Прочитаем название контрагента и id куратора
                val clientCursor = getContentResolver().query(
                    MTradeContentProvider.CLIENTS_CONTENT_URI,
                    arrayOf<String>("descr", "address", "curator_id", "flags"),
                    "id=?",
                    arrayOf<String>(g.MyDatabase.m_distribs_editing.client_id.toString()),
                    null
                )
                if (clientCursor != null && clientCursor.moveToNext()) {
                    val descrIndex = clientCursor.getColumnIndex("descr")
                    val addressIndex = clientCursor.getColumnIndex("address")
                    val curator_idIndex = clientCursor.getColumnIndex("curator_id")
                    //int flags_Index=clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex)
                    rec.stuff_client_address = clientCursor.getString(addressIndex)
                    rec.curator_id = MyID(clientCursor.getString(curator_idIndex))
                }
            }
            if (distr_point_id == null || distr_point_id.isEmpty()) {
                rec.distr_point_id = MyID()
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set)
            } else {
                rec.distr_point_id = distr_point_id.clone()
                // Наименование торговой точки
                cursor = getContentResolver().query(
                    MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
                    arrayOf<String>("descr"),
                    "id=?",
                    arrayOf<String>(rec.distr_point_id.toString()),
                    null
                )
                if (cursor!!.moveToNext()) {
                    val descrIndex = cursor.getColumnIndex("descr")
                    val newWord = cursor.getString(descrIndex)
                    rec.stuff_distr_point_name = newWord
                } else {
                    rec.stuff_distr_point_name = "{" + rec.distr_point_id + "}"
                }
                cursor.close()
            }
            rec.comment = ""
            rec.stuff_debt = 0.0
            rec.stuff_debt_past = 0.0
            rec.stuff_debt_past30 = 0.0

            if (bCopyDistribs) {
                bCreatedDistribs = true
                needReadDescrs = true
            }
        } else {
            needReadDescrs = true
        }
        // в случае, если введен копированием
        if (needReadDescrs) {
            // Наименование клиента
            cursor = getContentResolver().query(
                MTradeContentProvider.CLIENTS_CONTENT_URI,
                arrayOf<String>("descr", "address", "flags"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_distribs_editing.client_id.toString()),
                null
            )
            if (cursor != null && cursor.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val addressIndex = cursor.getColumnIndex("address")
                val flagsIndex = cursor.getColumnIndex("flags")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_client_name = newWord
                rec.stuff_client_address = cursor.getString(addressIndex)
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set)
                rec.stuff_client_address = ""
            }
            cursor!!.close()
            // Наименование торговой точки
            cursor = getContentResolver().query(
                MTradeContentProvider.DISTR_POINTS_CONTENT_URI,
                arrayOf<String>("descr"),
                "id=?",
                arrayOf<String>(g.MyDatabase.m_distribs_editing.distr_point_id.toString()),
                null
            )
            if (cursor!!.moveToNext()) {
                val descrIndex = cursor.getColumnIndex("descr")
                val newWord = cursor.getString(descrIndex)
                rec.stuff_distr_point_name = newWord
            } else {
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set)
            }
            cursor.close()
        }
        g.MyDatabase.m_distribs_editing_created = bCreatedDistribs
        if (bCreatedDistribs) {
            // В том числе копированием
            rec.uid = UUID.randomUUID()
            rec.numdoc = getString(R.string.numdoc_new) // "НОВЫЙ"

            // Берем текущее время
            rec.datedoc = Common.getDateTimeAsString14(null)
            rec.id = MyID() // Это код, присвоенный 1С
            rec.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED
            g.MyDatabase.m_distribs_editing_id =
                0 // Чтобы при записи не перезаписать уже существующий документ
            rec.version = 0
            rec.version_ack = 0
            rec.versionPDA = 0
            rec.versionPDA_ack = 0

            rec.accept_coord = 1

            // Добавим строки
            cursor = getContentResolver().query(
                MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI,
                arrayOf<String>("id", "descr"),
                null,
                null,
                "position"
            )
            val indexId = cursor!!.getColumnIndex("id")
            val indexDescr = cursor.getColumnIndex("descr")
            while (cursor.moveToNext()) {
                val line = DistribsLineRecord()
                line.distribs_contract_id = MyID(cursor.getString(indexId))
                line.stuff_distribs_contract = cursor.getString(indexDescr)
                line.quantity = 0.0
                rec.lines.add(line)
            }
            cursor.close()
        }
        // Долг контрагента
        cursor = getContentResolver().query(
            MTradeContentProvider.SALDO_CONTENT_URI,
            arrayOf<String>("saldo", "saldo_past", "saldo_past30"),
            "client_id=?",
            arrayOf<String>(g.MyDatabase.m_refund_editing.client_id.toString()),
            null
        )
        if (cursor != null && cursor.moveToNext()) {
            val indexDebt = cursor.getColumnIndex("saldo")
            val indexDebtPast = cursor.getColumnIndex("saldo_past")
            val indexDebtPast30 = cursor.getColumnIndex("saldo_past30")
            g.MyDatabase.m_distribs_editing.stuff_debt = cursor.getDouble(indexDebt)
            g.MyDatabase.m_distribs_editing.stuff_debt_past = cursor.getDouble(indexDebtPast)
            g.MyDatabase.m_distribs_editing.stuff_debt_past30 = cursor.getDouble(indexDebtPast30)
        } else {
            g.MyDatabase.m_distribs_editing.stuff_debt = 0.0
            g.MyDatabase.m_distribs_editing.stuff_debt_past = 0.0
            g.MyDatabase.m_distribs_editing.stuff_debt_past30 = 0.0
        }
        cursor!!.close()
        // Куратор
        cursor = getContentResolver().query(
            MTradeContentProvider.CURATORS_CONTENT_URI,
            arrayOf<String>("descr"),
            "id=?",
            arrayOf<String>(rec.curator_id.toString()),
            null
        )
        if (cursor != null && cursor.moveToNext()) {
            val indexDescr = cursor.getColumnIndex("descr")
            g.MyDatabase.m_distribs_editing.stuff_curator_name = cursor.getString(indexDescr)
        } else {
            g.MyDatabase.m_distribs_editing.stuff_curator_name =
                getResources().getString(R.string.curator_not_set)
        }
        cursor!!.close()

        //Bundle bundle = new Bundle();
        //bundle.putStringArray("list_accounts_descr",(String[])list_accounts_descr.toArray(new String[0]));
        //bundle.putStringArray("list_accounts_id",(String[])list_accounts_id.toArray(new String[0]));

        //bundle.putStringArray("list_stocks_descr",(String[])list_stocks_descr.toArray(new String[0]));
        //bundle.putStringArray("list_stocks_id",(String[])list_stocks_id.toArray(new String[0]));
        //intent.putExtra("extrasBundle", bundle);

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_DISTRIBS_REQUEST);
        editDistribsActivityResultLauncher!!.launch(intent)
    }


    // photo имеет смысл для id=0
    fun editMessage(id: Long, photo: String?) {
        val g = MySingleton.getInstance()
        //boolean bCreatedMessage=false;
        val intent = Intent(this, MessageActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val rec = g.MyDatabase.m_message_editing
        g.MyDatabase.m_message_editing_id = id
        g.MyDatabase.m_message_editing_modified = false

        if (id == 0L) {
            val c = Calendar.getInstance()
            val date = c.getTime()
            c.set(Calendar.DAY_OF_MONTH, 1)
            val bom = c.getTime()
            rec.uid = UUID.randomUUID()
            rec.ver = 0
            rec.acknowledged = 4
            rec.sender_id = MyID()
            rec.receiver_id = MyID()
            rec.datetime = Common.getDateTimeAsString14(date)
            rec.text = ""
            rec.type_idx = E_MESSAGE_TYPES.E_MESSAGE_TYPE_MESSAGE.value()
            rec.date1 = Common.MyDateFormat("yyyyMMdd", bom)
            rec.date2 = Common.MyDateFormat("yyyyMMdd", date)
            rec.client_id = MyID()
            rec.nomenclature_id = MyID()
            rec.report = ""
            rec.fname = photo
            if (rec.fname == null) rec.fname = ""
            if (!rec.fname.isEmpty()) {
                rec.type_idx = E_MESSAGE_TYPES.E_MESSAGE_TYPE_PHOTO.value()
            }
            //startActivityForResult(intent, EDIT_MESSAGE_REQUEST);
            editMessageActivityResultLauncher!!.launch(intent)
        } else {
            val result = TextDatabase.ReadMessageBy_Id(
                getContentResolver(),
                g.MyDatabase,
                rec,
                g.MyDatabase.m_message_editing_id
            )
            if ((rec.acknowledged and (4 or 16)) == 0) {
                rec.acknowledged = rec.acknowledged or 16
                val cv = ContentValues()
                cv.put("acknowledged", rec.acknowledged)
                cv.put("ver", rec.ver) // версия не меняется
                getContentResolver().update(
                    MTradeContentProvider.MESSAGES_CONTENT_URI,
                    cv,
                    "_id=?",
                    arrayOf<String>(g.MyDatabase.m_message_editing_id.toString())
                )
            }
            if (result) {
                //startActivityForResult(intent, EDIT_MESSAGE_REQUEST);
                editMessageActivityResultLauncher!!.launch(intent)
            }
        }
    }


    private fun readVersions() {
        val g = MySingleton.getInstance()
        g.MyDatabase.m_nomenclature_version = 0
        g.MyDatabase.m_clients_version = 0
        g.MyDatabase.m_agreements_version = 0
        g.MyDatabase.m_stocks_version = 0
        g.MyDatabase.m_rests_version = 0
        g.MyDatabase.m_saldo_version = 0
        g.MyDatabase.m_saldo_extended_version = 0
        g.MyDatabase.m_prices_version = 0
        g.MyDatabase.m_pricetypes_version = 0
        g.MyDatabase.m_clients_price_version = 0
        g.MyDatabase.m_curators_price_version = 0
        g.MyDatabase.m_agents_version = 0
        g.MyDatabase.m_curators_version = 0
        g.MyDatabase.m_distr_points_version = 0
        g.MyDatabase.m_organizations_version = 0
        g.MyDatabase.m_sales_loaded_version = 0
        g.MyDatabase.m_sales_loaded_versions = HashMap<UUID?, Int?>()
        g.MyDatabase.m_places_version = 0
        g.MyDatabase.m_occupied_places_version = 0
        g.MyDatabase.m_vicarious_power_version = 0
        g.MyDatabase.m_distribs_contracts_version = 0
        g.MyDatabase.m_equipment_version = -1
        g.MyDatabase.m_equipment_rests_version = -1
        g.MyDatabase.m_agreements30_version = 0
        g.MyDatabase.m_prices_agreements30_version = 0
        g.MyDatabase.m_routes_version = 0
        g.MyDatabase.m_routes_dates_version = 0

        g.MyDatabase.m_sent_count = 0

        // TODO код-близнец в ExchangeThread
        val cursor = getContentResolver().query(
            MTradeContentProvider.VERSIONS_CONTENT_URI,
            arrayOf<String>("param", "ver"),
            null,
            null,
            null
        )
        if (cursor != null) {
            val index_param = cursor.getColumnIndex("param")
            val index_ver = cursor.getColumnIndex("ver")
            while (cursor.moveToNext()) {
                val param = cursor.getString(index_param)
                if (param == "CLIENTS") {
                    g.MyDatabase.m_clients_version = cursor.getInt(index_ver)
                } else if (param == "NOMENCLATURE") {
                    g.MyDatabase.m_nomenclature_version = cursor.getInt(index_ver)
                } else if (param == "RESTS") {
                    g.MyDatabase.m_rests_version = cursor.getInt(index_ver)
                } else if (param == "AGREEMENTS") {
                    g.MyDatabase.m_agreements_version = cursor.getInt(index_ver)
                } else if (param == "AGREEMENTS30") {
                    g.MyDatabase.m_agreements30_version = cursor.getInt(index_ver)
                } else if (param == "PRICES_AGREEMENTS30") {
                    g.MyDatabase.m_prices_agreements30_version = cursor.getInt(index_ver)
                } else if (param == "SALDO") {
                    g.MyDatabase.m_saldo_version = cursor.getInt(index_ver)
                } else if (param == "SALDO_EXT") {
                    g.MyDatabase.m_saldo_extended_version = cursor.getInt(index_ver)
                } else if (param == "STOCKS") {
                    g.MyDatabase.m_stocks_version = cursor.getInt(index_ver)
                } else if (param == "PRICES") {
                    g.MyDatabase.m_prices_version = cursor.getInt(index_ver)
                } else if (param == "PRICETYPES") {
                    g.MyDatabase.m_pricetypes_version = cursor.getInt(index_ver)
                } else if (param == "CLIENTS_PRICE") {
                    g.MyDatabase.m_clients_price_version = cursor.getInt(index_ver)
                } else if (param == "CURATORS_PRICE") {
                    g.MyDatabase.m_curators_price_version = cursor.getInt(index_ver)
                } else if (param == "SIMPLE_DISCOUNTS") {
                    g.MyDatabase.m_simple_discounts_version = cursor.getInt(index_ver)
                } else if (param == "AGENTS") {
                    g.MyDatabase.m_agents_version = cursor.getInt(index_ver)
                } else if (param == "CURATORS") {
                    g.MyDatabase.m_curators_version = cursor.getInt(index_ver)
                } else if (param == "D_POINTS") {
                    g.MyDatabase.m_distr_points_version = cursor.getInt(index_ver)
                } else if (param == "ORGANIZATIONS") {
                    g.MyDatabase.m_organizations_version = cursor.getInt(index_ver)
                } else if (param == "SALES_LOADED") {
                    g.MyDatabase.m_sales_loaded_version = cursor.getInt(index_ver)
                } else if (param == "PLACES") {
                    g.MyDatabase.m_places_version = cursor.getInt(index_ver)
                } else if (param == "OCCUPIED_PLACES") {
                    g.MyDatabase.m_occupied_places_version = cursor.getInt(index_ver)
                } else if (param == "VICARIOUS_POWER") {
                    g.MyDatabase.m_vicarious_power_version = cursor.getInt(index_ver)
                } else if (param == "SENT_COUNT") {
                    g.MyDatabase.m_sent_count = cursor.getInt(index_ver)
                } else if (param == "DISTRIBS_CONTRACTS") {
                    g.MyDatabase.m_distribs_contracts_version = cursor.getInt(index_ver)
                } else if (param == "EQUIPMENT") {
                    g.MyDatabase.m_equipment_version = cursor.getInt(index_ver)
                } else if (param == "EQUIPMENT_RESTS") {
                    g.MyDatabase.m_equipment_rests_version = cursor.getInt(index_ver)
                } else if (param == "ROUTES") {
                    g.MyDatabase.m_routes_version = cursor.getInt(index_ver)
                } else if (param == "ROUTES_DATES") {
                    g.MyDatabase.m_routes_dates_version = cursor.getInt(index_ver)
                }
            }
            cursor.close()
        }
        val cursorSales = getContentResolver().query(
            MTradeContentProvider.VERSIONS_SALES_CONTENT_URI,
            arrayOf<String>("param", "ver"),
            null,
            null,
            null
        )
        if (cursorSales != null) {
            val index_param = cursor!!.getColumnIndex("param")
            val index_ver = cursor.getColumnIndex("ver")
            while (cursor.moveToNext()) {
                val param = cursor.getString(index_param)
                if (param.length == 36) {
                    val uuid = UUID.fromString(param)
                    g.MyDatabase.m_sales_loaded_versions.put(uuid, cursor.getInt(index_ver))
                }
            }
            cursorSales.close()
        }
    }


    private fun setNegativeVersions() {
        // Установим отрицательные версии
        val cursorVersions = getContentResolver().query(
            MTradeContentProvider.VERSIONS_CONTENT_URI,
            arrayOf<String>("param", "ver"),
            null,
            null,
            null
        )
        val index_param = cursorVersions!!.getColumnIndex("param")
        val index_ver = cursorVersions.getColumnIndex("ver")
        while (cursorVersions.moveToNext()) {
            //#define QUERY_VER(a) if ((a)>0) (a)=-(a);
            var ver = cursorVersions.getInt(index_ver)
            if (ver >= 0) {
                if (ver == 0) ver = -1
                else ver = -ver
                val cv = ContentValues()
                cv.put("param", cursorVersions.getString(index_param))
                cv.put("ver", ver)
                getContentResolver().insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv)
            }
        }
        cursorVersions.close()
    }

    /*
	Thread thread = new Thread(new Runnable() {
		Thread(Handler h)
		{
			handler=h;
		}

		public void run() {
			try {
				doSomething();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	});
	thread.start();
	*/
    override fun onCreateDialog(id: Int, bundle: Bundle?): Dialog? {
        val g = MySingleton.getInstance()
        when (id) {
            IDD_REINDEX_CREATED -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                // Создана новая база данных! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_created_prevent_reindex)
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                        //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                        //loadDataAfterStart();
                        ServiceOperationsTask().execute(
                            SERVICE_TASK_STATE_REINDEX.toString(),
                            "",
                            ""
                        )
                        dialog.cancel()
                    }
                })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_REINDEX_UPGRADED -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                // База данных обновлена! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_updated_prevent_reindex)
                builder.setPositiveButton(
                    android.R.string.ok,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                            //getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null);
                            //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                            //loadDataAfterStart();
                            dialog.cancel()
                            ServiceOperationsTask().execute(
                                SERVICE_TASK_STATE_REINDEX_UPGRADE.toString(),
                                "",
                                ""
                            )
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_REINDEX_UPGRADED66 -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                // База данных обновлена! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_updated_prevent_reindex)
                builder.setPositiveButton(
                    android.R.string.ok,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                            ServiceOperationsTask().execute(
                                SERVICE_TASK_STATE_REINDEX_UPGRADE.toString(),
                                "66",
                                ""
                            )
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }


            IDD_REINDEX_INCORRECT_CLOSED -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                // Предыдущий сеанс был некорректно завершен! Выполнить реиндексацию, поиск потерянных заказов и продолжить работу?
                builder.setMessage(R.string.message_query_reindex)
                builder.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                        //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                        //loadDataAfterStart();
                        //recoverOrders(true);
                        dialog.cancel()
                        ServiceOperationsTask().execute(
                            SERVICE_TASK_STATE_RECOVER_ORDERS.toString(),
                            "",
                            ""
                        )
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        this@MainActivity.finish()
                    }
                })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_CANCEL_CURRENT_ORDER -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_cancel_current_order)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            cancelOrder(m_order_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CURRENT_ORDER -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_current_order)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            deleteOrder(m_order_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_CANCEL_CURRENT_REFUND -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_cancel_current_refund)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            cancelRefund(m_refund_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CURRENT_REFUND -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_current_refund)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            deleteRefund(m_refund_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_CANCEL_CURRENT_DISTRIBS -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_cancel_current_distribs)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            cancelDistribs(m_distribs_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CURRENT_DISTRIBS -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_current_distribs)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            deleteDistribs(m_distribs_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CLOSED_DOCUMENTS -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                    builder.setMessage(R.string.message_delete_closed_documents)
                } else {
                    builder.setMessage(R.string.message_delete_closed_orders)
                }
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            // TODO

                            val dlg = ProgressDialog(this@MainActivity)
                            dlg.setMessage("Deleting closed documents")
                            dlg.setIndeterminate(true)
                            dlg.setCancelable(false)
                            dlg.show()

                            val t: Thread = object : Thread() {
                                override fun run() {
                                    val g = MySingleton.getInstance()

                                    // TODO медленная операция, сделать

                                    // Заказы, они есть и удаляются во всех форматах
                                    var conditionString: String? = null
                                    var conditionArgs = ArrayList<String?>()

                                    conditionString = Common.combineConditions(
                                        conditionString,
                                        conditionArgs,
                                        "not " + E_ORDER_STATE.getNotClosedConditionWhere(),
                                        E_ORDER_STATE.getNotClosedSelectionArgs()
                                    )
                                    if (g.Common.PHARAOH && m_filter_type == 1) {
                                        // По дате обслуживания
                                        when (m_filter_date_type) {
                                            0 -> {}
                                            1 ->                                             // дата
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(shipping_date between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin,
                                                        m_filter_date_begin + "Z"
                                                    )
                                                )

                                            2 ->                                             // интервал
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(shipping_date between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin, m_filter_date_end + "Z"
                                                    )
                                                )
                                        }
                                    } else {
                                        // По дате документа
                                        when (m_filter_date_type) {
                                            0 -> {}
                                            1 ->                                             // дата
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin,
                                                        m_filter_date_begin + "Z"
                                                    )
                                                )

                                            2 ->                                             // интервал
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin, m_filter_date_end + "Z"
                                                    )
                                                )
                                        }
                                    }
                                    // 13.09.2018 удаляем резервные копии документов, чтобы они потом не восстановились
                                    if (g.Common.NEW_BACKUP_FORMAT) {
                                        // удалим резервные копии документов, а потом сами документы
                                        // такие копии могут создаваться, когда документ уже был создан и записан
                                        // потом его начали редактировать, и программа закрылась из-за сбоя
                                        // при старте такие документы не восстанавливаются (потому, что там были строки товаров в оригинальном документе)
                                        val toDeleteBackupIds = getContentResolver().query(
                                            MTradeContentProvider.ORDERS_CONTENT_URI,
                                            arrayOf<String>("_id"),
                                            "editing_backup=0 and " + conditionString,
                                            conditionArgs.toTypedArray<String?>(),
                                            null
                                        )
                                        while (toDeleteBackupIds!!.moveToNext()) {
                                            getContentResolver().delete(
                                                MTradeContentProvider.ORDERS_CONTENT_URI,
                                                "old_id=? and editing_backup<>0",
                                                arrayOf<String?>(
                                                    toDeleteBackupIds!!.getString(0)
                                                )
                                            )
                                        }
                                        toDeleteBackupIds!!.close()
                                    }
                                    // а дальше обычный код, когда удаляются сами докенты
                                    //getContentResolver().delete(MTradeContentProvider.ORDERS_CONTENT_URI, "not "+E_ORDER_STATE.getNotClosedConditionWhere(), E_ORDER_STATE.getNotClosedSelectionArgs());
                                    getContentResolver().delete(
                                        MTradeContentProvider.ORDERS_CONTENT_URI,
                                        conditionString,
                                        conditionArgs.toTypedArray<String?>()
                                    )

                                    // Продлидер. Удаляются не только заказы, но также другие виды документов
                                    if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                                        // Возвраты
                                        conditionString = null
                                        conditionArgs = ArrayList<String?>()

                                        conditionString = Common.combineConditions(
                                            conditionString,
                                            conditionArgs,
                                            "not " + E_REFUND_STATE.getNotClosedConditionWhere(),
                                            E_REFUND_STATE.getNotClosedSelectionArgs()
                                        )

                                        when (m_filter_date_type) {
                                            0 -> {}
                                            1 ->                                             // дата
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin,
                                                        m_filter_date_begin + "Z"
                                                    )
                                                )

                                            2 ->                                             // интервал
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin, m_filter_date_end + "Z"
                                                    )
                                                )
                                        }
                                        getContentResolver().delete(
                                            MTradeContentProvider.REFUNDS_CONTENT_URI,
                                            conditionString,
                                            conditionArgs.toTypedArray<String?>()
                                        )

                                        // Дистрибьюции
                                        conditionString = null
                                        conditionArgs = ArrayList<String?>()

                                        conditionString = Common.combineConditions(
                                            conditionString,
                                            conditionArgs,
                                            "not " + E_DISTRIBS_STATE.getDistribsNotClosedConditionWhere(),
                                            E_DISTRIBS_STATE.getDistribsNotClosedSelectionArgs()
                                        )

                                        when (m_filter_date_type) {
                                            0 -> {}
                                            1 ->                                             // дата
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin,
                                                        m_filter_date_begin + "Z"
                                                    )
                                                )

                                            2 ->                                             // интервал
                                                conditionString = Common.combineConditions(
                                                    conditionString,
                                                    conditionArgs,
                                                    "(datedoc between ? and ?)",
                                                    arrayOf<String?>(
                                                        m_filter_date_begin, m_filter_date_end + "Z"
                                                    )
                                                )
                                        }
                                        getContentResolver().delete(
                                            MTradeContentProvider.DISTRIBS_CONTENT_URI,
                                            conditionString,
                                            conditionArgs.toTypedArray<String?>()
                                        )
                                    }
                                    Common.dismissWithExceptionHandling(dlg)
                                }
                            }
                            t.start()
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_SORT_ORDERS -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(R.string.message_sort_orders)
                val sort_types_array: Array<String?>?
                if (g.Common.PHARAOH) {
                    sort_types_array = getResources().getStringArray(R.array.sort_types_array_ph)
                } else {
                    sort_types_array = getResources().getStringArray(R.array.sort_types_array)
                }
                builder.setItems(sort_types_array, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        when (which) {
                            0 -> {
                                val cv = ContentValues()
                                cv.put("sort_type", "orders_by_date")
                                getContentResolver().insert(
                                    MTradeContentProvider.SORT_CONTENT_URI,
                                    cv
                                )
                            }

                            1 -> {
                                val cv = ContentValues()
                                cv.put("sort_type", "orders_by_service_date")
                                getContentResolver().insert(
                                    MTradeContentProvider.SORT_CONTENT_URI,
                                    cv
                                )
                            }
                        }
                    }
                })
                return builder.create()
            }


            IDD_DELETE_CURRENT_PAYMENT -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_current_payment)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            deletePayment(m_payment_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CLOSED_PAYMENTS -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_closed_payments)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            getContentResolver().delete(
                                MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                                "not " + E_PAYMENT_STATE.getPaymentNotClosedConditionWhere(),
                                E_PAYMENT_STATE.getPaymentNotClosedSelectionArgs()
                            )
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_CURRENT_MESSAGE -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_current_message)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            deleteMessage(m_message_id_to_process)
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DELETE_READED_MESSAGES -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_delete_readed_messages)
                builder.setPositiveButton(
                    android.R.string.yes,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            val cv = ContentValues()
                            cv.put("isMark", 1)
                            // 4 - это наше сообщение
                            // 16- прочитано
                            getContentResolver().update(
                                MTradeContentProvider.MESSAGES_CONTENT_URI,
                                cv,
                                "(acknowledged&20)=16",
                                null
                            )
                        }
                    })
                builder.setNegativeButton(
                    android.R.string.no,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, id: Int) {
                            dialog.cancel()
                        }
                    })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_QUERY_SEND_IMAGES -> {
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setMessage(R.string.message_send_images)
                builder.setPositiveButton(R.string.yes, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        m_bExcludeImageMessages = false
                        ExchangeTask().execute(1, 0, "")
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        m_bExcludeImageMessages = true
                        ExchangeTask().execute(1, 0, "")
                        //dialog.cancel();
                    }
                })
                builder.setCancelable(false)
                return builder.create()
            }

            IDD_DATA_FILTER -> {
                val builder = AlertDialog.Builder(this)
                //builder.setTitle("Custom dialog");
                builder.setTitle(R.string.title_filter)
                // создаем view из dialog.xml
                //LinearLayout view = (LinearLayout) getLayoutInflater()
                //    .inflate(R.layout.filter, null);
                val view = getLayoutInflater().inflate(R.layout.filter, null) as ScrollView
                // устанавливаем ее, как содержимое тела диалога
                builder.setView(view)

                // Spinner тип отбора (время документа или время доставки)
                val spinnerFilterType = view.findViewById<View?>(R.id.spinnerFilterType) as Spinner
                val dataSpinnerFilterType =
                    getResources().getStringArray(R.array.documents_period_type)

                val dataAdapterFilterType = ArrayAdapter<String?>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    dataSpinnerFilterType
                )


                dataAdapterFilterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerFilterType.setAdapter(dataAdapterFilterType)

                if (m_filter_type < 0 || m_filter_type >= dataSpinnerFilterType.size) spinnerFilterType.setSelection(
                    0
                )
                else spinnerFilterType.setSelection(m_filter_type)

                spinnerFilterType.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        arg0: AdapterView<*>?,
                        arg1: View?,
                        index: Int,
                        arg3: Long
                    ) {
                        m_filter_type_current = index
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {
                    }
                })

                // Spinner время доставки
                val spinnerFilterPeriod =
                    view.findViewById<View?>(R.id.spinnerFilterPeriod) as Spinner
                val dataSpinnerFilterPeriod =
                    getResources().getStringArray(R.array.filter_time_type)

                val dataAdapterShippingTime = ArrayAdapter<String?>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    dataSpinnerFilterPeriod
                )

                if (m_filter_date_begin_current.isEmpty()) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                    val work_date =
                        DatePreference.getDateFor(sharedPreferences, "work_date").getTime()
                    m_filter_date_begin_current = Common.MyDateFormat("yyyyMMdd", work_date)
                }
                if (m_filter_date_end_current.isEmpty()) {
                    m_filter_date_end_current = m_filter_date_begin_current
                }

                if (m_filter_date_begin_current.length >= 8) {
                    val etFirstDate = view.findViewById<View?>(R.id.etFirstDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_filter_date_begin_current.substring(
                            6,
                            8
                        ) + "." + m_filter_date_begin_current.substring(
                            4,
                            6
                        ) + "." + m_filter_date_begin_current.substring(0, 4)
                    )
                    etFirstDate.setText(sb.toString())
                }

                if (m_filter_date_end_current.length >= 8) {
                    val etSecondDate = view.findViewById<View?>(R.id.etSecondDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_filter_date_end_current.substring(
                            6,
                            8
                        ) + "." + m_filter_date_end_current.substring(
                            4,
                            6
                        ) + "." + m_filter_date_end_current.substring(0, 4)
                    )
                    etSecondDate.setText(sb.toString())
                }

                dataAdapterShippingTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerFilterPeriod.setAdapter(dataAdapterShippingTime)

                if (m_filter_date_type < 0 || m_filter_date_type >= dataSpinnerFilterPeriod.size) spinnerFilterPeriod.setSelection(
                    0
                )
                else spinnerFilterPeriod.setSelection(m_filter_date_type)

                spinnerFilterPeriod.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        arg0: AdapterView<*>?,
                        arg1: View?,
                        index: Int,
                        arg3: Long
                    ) {
                        if (m_filter_date_type_current != index) {
                            if (m_filter_date_type_current == 1) {
                                m_filter_date_end_current = m_filter_date_begin_current
                            }
                            if (m_filter_date_type_current == 0) {
                                val sharedPreferences =
                                    PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                                val work_date =
                                    DatePreference.getDateFor(sharedPreferences, "work_date")
                                        .getTime()
                                m_filter_date_begin_current =
                                    Common.MyDateFormat("yyyyMMdd", work_date)
                                m_filter_date_end_current = m_filter_date_begin_current
                            }

                            if (m_filter_date_begin_current.length >= 8) {
                                val etFirstDate =
                                    view.findViewById<View?>(R.id.etFirstDate) as EditText
                                val sb = StringBuilder()
                                sb.append(
                                    m_filter_date_begin_current.substring(
                                        6,
                                        8
                                    ) + "." + m_filter_date_begin_current.substring(
                                        4,
                                        6
                                    ) + "." + m_filter_date_begin_current.substring(0, 4)
                                )
                                etFirstDate.setText(sb.toString())
                            }

                            if (m_filter_date_end_current.length >= 8) {
                                val etSecondDate =
                                    view.findViewById<View?>(R.id.etSecondDate) as EditText
                                val sb = StringBuilder()
                                sb.append(
                                    m_filter_date_end_current.substring(
                                        6,
                                        8
                                    ) + "." + m_filter_date_end_current.substring(
                                        4,
                                        6
                                    ) + "." + m_filter_date_end_current.substring(0, 4)
                                )
                                etSecondDate.setText(sb.toString())
                            }

                            m_filter_date_type_current = index
                            val linearLayoutStartDate =
                                view.findViewById<View?>(R.id.linearLayoutStartDate) as LinearLayout
                            val linearLayoutEndDate =
                                view.findViewById<View?>(R.id.linearLayoutEndDate) as LinearLayout

                            when (m_filter_date_type_current) {
                                0 -> {
                                    linearLayoutStartDate.setVisibility(View.GONE)
                                    linearLayoutEndDate.setVisibility(View.GONE)
                                }

                                1 -> {
                                    linearLayoutStartDate.setVisibility(View.VISIBLE)
                                    linearLayoutEndDate.setVisibility(View.GONE)
                                }

                                2 -> {
                                    linearLayoutStartDate.setVisibility(View.VISIBLE)
                                    linearLayoutEndDate.setVisibility(View.VISIBLE)
                                }
                            }
                        }
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {
                    }
                })

                /*
		    final DatePicker datePicker=(DatePicker)view.findViewById(R.id.datePickerFilter);

		    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {

					int   day  = datePicker.getDayOfMonth();
					int   month= datePicker.getMonth();
					int   year = datePicker.getYear();

					//SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					m_date = sdf.format(new Date(year-1900, month, day));
					getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
				}
			});
		    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
				}
			});
		    builder.setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					m_date=null;
					getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
				}
			});
			*/
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        val checkBoxOrders =
                            view.findViewById<View?>(R.id.checkBoxOrders) as CheckBox
                        m_filter_orders = checkBoxOrders.isChecked()
                        val checkBoxRefunds =
                            view.findViewById<View?>(R.id.checkBoxRefunds) as CheckBox
                        m_filter_refunds = checkBoxRefunds.isChecked()
                        val checkBoxDistribs =
                            view.findViewById<View?>(R.id.checkBoxDistribs) as CheckBox
                        m_filter_distribs = checkBoxDistribs.isChecked()
                        //Spinner spinnerFilterPeriod=(Spinner)view.findViewById(R.id.spinnerFilterPeriod);
                        m_filter_type = m_filter_type_current
                        m_filter_date_type =
                            m_filter_date_type_current //spinnerFilterPeriod.getSelectedItemPosition();

                        m_filter_date_begin = m_filter_date_begin_current
                        m_filter_date_end = m_filter_date_end_current

                        // TODO 22.01.2019
                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                        val fragmentManager = getSupportFragmentManager()
                        val fragment = fragmentManager.findFragmentById(R.id.content_frame)

                        if (fragment is OrdersListFragment) {
                            val ordersListFragment = fragment
                            ordersListFragment.onFilterChanged(
                                m_filter_orders,
                                m_filter_refunds,
                                m_filter_distribs,
                                m_filter_date_begin,
                                m_filter_date_end,
                                m_filter_type,
                                m_filter_date_type
                            )
                            invalidateOptionsMenu()
                        }
                    }
                })
                builder.setNegativeButton(
                    R.string.cancel,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                        }
                    })
                builder.setNeutralButton(R.string.clear, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        m_filter_orders = true
                        m_filter_refunds = true
                        m_filter_distribs = true
                        m_filter_type = 0
                        m_filter_date_type = 0
                        m_filter_date_begin_current = m_filter_date_begin
                        m_filter_date_end_current = m_filter_date_end
                        // TODO все такие переделать на вызов метода фрагмента
                        // TODO 22.01.2019
                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                        val fragmentManager = getSupportFragmentManager()
                        val fragment = fragmentManager.findFragmentById(R.id.content_frame)

                        if (fragment is OrdersListFragment) {
                            val ordersListFragment = fragment
                            ordersListFragment.onFilterChanged(
                                m_filter_orders,
                                m_filter_refunds,
                                m_filter_distribs,
                                m_filter_date_begin,
                                m_filter_date_end,
                                m_filter_type,
                                m_filter_date_type
                            )
                            invalidateOptionsMenu()
                        }
                    }
                })
                m_filter_dialog = builder.create()
                return m_filter_dialog
            }

            IDD_QUERY_DOCUMENTS_PERIOD -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.title_query_documents)
                val view = getLayoutInflater().inflate(R.layout.query_documents, null) as ScrollView
                builder.setView(view)

                // Spinner тип периода
                val spinnerPeriodType = view.findViewById<View?>(R.id.spinnerPeriodType) as Spinner
                val dataSpinnerPeriodType =
                    getResources().getStringArray(R.array.documents_period_type)

                val dataAdapterPeriodType = ArrayAdapter<String?>(
                    this@MainActivity,
                    android.R.layout.simple_spinner_item,
                    dataSpinnerPeriodType
                )

                // Запрашиваемый период возьмем из отбора, если он установлен
                when (m_filter_date_type_current) {
                    0 -> {
                        // без отбора
                        // это будет установлено по умолчанию
                        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                        val work_date =
                            DatePreference.getDateFor(sharedPreferences, "work_date").getTime()
                        m_query_date_begin_current = Common.MyDateFormat("yyyyMMdd", work_date)
                        //
                        m_query_date_end_current = m_query_date_begin_current
                    }

                    1 -> {
                        // дата
                        m_query_date_begin_current = m_filter_date_begin
                        m_query_date_end_current = m_filter_date_begin
                    }

                    2 -> {
                        // интервал
                        m_query_date_begin_current = m_filter_date_begin
                        m_query_date_end_current = m_filter_date_end
                    }
                }

                val etFirstDate = view.findViewById<View?>(R.id.etFirstDate) as EditText
                val etSecondDate = view.findViewById<View?>(R.id.etSecondDate) as EditText

                if (m_query_date_begin_current.length >= 8) {
                    val sb = StringBuilder()
                    sb.append(m_query_date_begin_current.substring(6, 8)).append(".").append(
                        m_query_date_begin_current.substring(4, 6)
                    ).append(".").append(
                        m_query_date_begin_current.substring(0, 4)
                    )
                    etFirstDate.setText(sb.toString())
                }

                if (m_query_date_end_current.length >= 8) {
                    val sb = StringBuilder()
                    sb.append(m_query_date_end_current.substring(6, 8)).append(".").append(
                        m_query_date_end_current.substring(4, 6)
                    ).append(".").append(
                        m_query_date_end_current.substring(0, 4)
                    )
                    etSecondDate.setText(sb.toString())
                }

                dataAdapterPeriodType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerPeriodType.setAdapter(dataAdapterPeriodType)

                if (m_query_period_type < 0 || m_query_period_type >= dataSpinnerPeriodType.size) {
                    spinnerPeriodType.setSelection(0)
                    m_query_period_type_current = 0
                } else {
                    spinnerPeriodType.setSelection(m_query_period_type)
                    m_query_period_type_current = m_query_period_type
                }

                spinnerPeriodType.setOnItemSelectedListener(object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        arg0: AdapterView<*>?,
                        arg1: View?,
                        index: Int,
                        arg3: Long
                    ) {
                        m_query_period_type_current = index
                    }

                    override fun onNothingSelected(arg0: AdapterView<*>?) {
                    }
                })

                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        //CheckBox checkBoxOrders=(CheckBox)view.findViewById(R.id.checkBoxOrders);
                        //m_filter_orders=checkBoxOrders.isChecked();
                        //CheckBox checkBoxRefunds=(CheckBox)view.findViewById(R.id.checkBoxRefunds);
                        //m_filter_refunds=checkBoxRefunds.isChecked();
                        //m_filter_date_type=m_filter_date_type_current;//spinnerFilterPeriod.getSelectedItemPosition();

                        m_query_period_type = m_query_period_type_current

                        m_query_date_begin = m_query_date_begin_current
                        m_query_date_end = m_query_date_end_current

                        ExchangeTask().execute(4, 0, null)

                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);

                        // TODO Хотя наверное здесь это не нужно
                        val fragmentManager = getSupportFragmentManager()
                        val fragment = fragmentManager.findFragmentById(R.id.content_frame)

                        if (fragment is OrdersListFragment) {
                            val ordersListFragment = fragment
                            ordersListFragment.onFilterChanged(
                                m_filter_orders,
                                m_filter_refunds,
                                m_filter_distribs,
                                m_filter_date_begin,
                                m_filter_date_end,
                                m_filter_type,
                                m_filter_date_type
                            )
                            invalidateOptionsMenu()
                        }
                    }
                })
                builder.setNegativeButton(
                    R.string.cancel,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                        }
                    })
                m_query_documents_dialog = builder.create()
                return m_query_documents_dialog
            }

            IDD_FILTER_DATE_BEGIN -> {
                val day = m_filter_date_begin_current.substring(6, 8).toInt()
                val month = m_filter_date_begin_current.substring(4, 6).toInt() - 1
                val year = m_filter_date_begin_current.substring(0, 4).toInt()

                val tpd = DatePickerDialog(this, object : OnDateSetListener {
                    override fun onDateSet(
                        view0: DatePicker?, selectedYear: Int, selectedMonth: Int,
                        selectedDay: Int
                    ) {
                        m_filter_date_begin_current = Common.MyDateFormat(
                            "yyyyMMdd",
                            GregorianCalendar(selectedYear, selectedMonth, selectedDay)
                        )

                        val etDate =
                            m_filter_dialog!!.findViewById<View?>(R.id.etFirstDate) as EditText
                        val sb = StringBuilder()
                        sb.append(
                            m_filter_date_begin_current.substring(
                                6,
                                8
                            ) + "." + m_filter_date_begin_current.substring(
                                4,
                                6
                            ) + "." + m_filter_date_begin_current.substring(0, 4)
                        )
                        etDate.setText(sb.toString())
                    }
                }, year, month, day)
                return tpd
            }

            IDD_FILTER_DATE_END -> {
                val day = m_filter_date_end_current.substring(6, 8).toInt()
                val month = m_filter_date_end_current.substring(4, 6).toInt() - 1
                val year = m_filter_date_end_current.substring(0, 4).toInt()

                val tpd = DatePickerDialog(this, object : OnDateSetListener {
                    override fun onDateSet(
                        view0: DatePicker?, selectedYear: Int, selectedMonth: Int,
                        selectedDay: Int
                    ) {
                        m_filter_date_end_current = Common.MyDateFormat(
                            "yyyyMMdd",
                            GregorianCalendar(selectedYear, selectedMonth, selectedDay)
                        )

                        val etDate =
                            m_filter_dialog!!.findViewById<View?>(R.id.etSecondDate) as EditText
                        val sb = StringBuilder()
                        sb.append(
                            m_filter_date_end_current.substring(
                                6,
                                8
                            ) + "." + m_filter_date_end_current.substring(
                                4,
                                6
                            ) + "." + m_filter_date_end_current.substring(0, 4)
                        )
                        etDate.setText(sb.toString())
                    }
                }, year, month, day)
                return tpd
            }

            IDD_QUERY_DOCUMENTS_DATE_BEGIN -> {
                val day = m_query_date_begin_current.substring(6, 8).toInt()
                val month = m_query_date_begin_current.substring(4, 6).toInt() - 1
                val year = m_query_date_begin_current.substring(0, 4).toInt()

                val tpd = DatePickerDialog(this, object : OnDateSetListener {
                    override fun onDateSet(
                        view0: DatePicker?, selectedYear: Int, selectedMonth: Int,
                        selectedDay: Int
                    ) {
                        m_query_date_begin_current = Common.MyDateFormat(
                            "yyyyMMdd",
                            GregorianCalendar(selectedYear, selectedMonth, selectedDay)
                        )

                        val etDate =
                            m_query_documents_dialog!!.findViewById<View?>(R.id.etFirstDate) as EditText
                        val sb = StringBuilder()
                        sb.append(
                            m_query_date_begin_current.substring(
                                6,
                                8
                            ) + "." + m_query_date_begin_current.substring(
                                4,
                                6
                            ) + "." + m_query_date_begin_current.substring(0, 4)
                        )
                        etDate.setText(sb.toString())
                    }
                }, year, month, day)
                return tpd
            }

            IDD_QUERY_DOCUMENTS_DATE_END -> {
                val day = m_query_date_end_current.substring(6, 8).toInt()
                val month = m_query_date_end_current.substring(4, 6).toInt() - 1
                val year = m_query_date_end_current.substring(0, 4).toInt()

                val tpd = DatePickerDialog(this, object : OnDateSetListener {
                    override fun onDateSet(
                        view0: DatePicker?, selectedYear: Int, selectedMonth: Int,
                        selectedDay: Int
                    ) {
                        m_query_date_end_current = Common.MyDateFormat(
                            "yyyyMMdd",
                            GregorianCalendar(selectedYear, selectedMonth, selectedDay)
                        )

                        val etDate =
                            m_query_documents_dialog!!.findViewById<View?>(R.id.etSecondDate) as EditText
                        val sb = StringBuilder()
                        sb.append(
                            m_query_date_end_current.substring(
                                6,
                                8
                            ) + "." + m_query_date_end_current.substring(
                                4,
                                6
                            ) + "." + m_query_date_end_current.substring(0, 4)
                        )
                        etDate.setText(sb.toString())
                    }
                }, year, month, day)
                return tpd
            }

            IDD_COULD_NOT_START_BUT_PERMISSIONS -> {
                val builder = AlertDialog.Builder(this)
                //builder.setTitle();
                builder.setMessage(R.string.error_required_permissions_expected)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.close, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, item: Int) {
                        this@MainActivity.finish()
                    }
                })
                return builder.create()
            }

            IDD_NEED_GPS_ENABLED -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.need_gps_permissions)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, item: Int) {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                })
                return builder.create()
            }

            IDD_QUERY_START_VISIT -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.query_start_visit)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                    }
                })
                return builder.create()
            }

            IDD_QUERY_END_VISIT -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.query_end_visit)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                    }
                })
                return builder.create()
            }

            IDD_QUERY_CANCEL_VISIT -> {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.query_cancel_visit)
                builder.setCancelable(false)
                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                    }
                })
                return builder.create()
            }


            IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(getString(R.string.select_client_to_create_order)) // потом заменится не требуемый вид документа
                builder.setCancelable(false)

                val arrayAdapter: ArrayAdapter<MyElementOfArrayList?> = ArrayAdapter<MyElementOfArrayList?>(
                    this@MainActivity,
                    android.R.layout.select_dialog_singlechoice
                )

                builder.setSingleChoiceItems(
                    arrayAdapter,
                    0,
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                            //String strName = arrayAdapter.getItem(which).descr;
                        }
                    })

                builder.setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    }
                })
                builder.setNegativeButton(R.string.no, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                    }
                })


                //builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                //    @Override
                //    public void onClick(DialogInterface dialogInterface, int which) {
                //        String strName = arrayAdapter.getItem(which).descr;
                //    }
                //});

                /*
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                */
                return builder.create()
            }

            else -> return null
        }
    }

    /*
	@Override
	public void permissionGranted(PermissionChecker.RuntimePermissions permission) {

	}

	@Override
	public void permissionDenied(PermissionChecker.RuntimePermissions permission) {

	}
	*/
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ): Unit {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        /*
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION.VALUE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted(PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionDenied(PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
        */
        var bFineLocationGranted = false
        var bCoarseLocationGranted = false
        var bCameraGranted = false
        var bStorageGranted = false
        var i = 0
        while (i < grantResults.size) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[i] == Manifest.permission.ACCESS_FINE_LOCATION) bFineLocationGranted =
                    true
                if (permissions[i] == Manifest.permission.ACCESS_COARSE_LOCATION) bCoarseLocationGranted =
                    true
                if (permissions[i] == Manifest.permission.CAMERA) bCameraGranted = true
                if (permissions[i] == Manifest.permission.WRITE_EXTERNAL_STORAGE) bStorageGranted =
                    true
            }
            i++
        }

        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS) {
            if (bFineLocationGranted) {
                val criteria = Criteria()
                criteria.setAccuracy(Criteria.ACCURACY_FINE)
                val provider = m_locationManager!!.getBestProvider(criteria, true)
                if (provider != null) {
                    try {
                        m_locationManager!!.requestLocationUpdates(provider, 0, 0f, locListener)
                    } catch (e: SecurityException) {
                    }
                }
            } else if (bCoarseLocationGranted) {
                val criteria = Criteria()
                criteria.setAccuracy(Criteria.ACCURACY_COARSE)
                val provider = m_locationManager!!.getBestProvider(criteria, true)
                if (provider != null) {
                    try {
                        m_locationManager!!.requestLocationUpdates(provider, 0, 0f, locListener)
                    } catch (e: SecurityException) {
                    }
                }
            } else {
                // Отказано в доступе, запишем дату отказа
                val g = MySingleton.getInstance()

                val cv = ContentValues()
                cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION)
                cv.put("datetime", Common.getDateTimeAsString14(null))
                cv.put("seance_incoming", g.MyDatabase.m_seance.toString())
                getContentResolver().insert(
                    MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI,
                    cv
                )
            }
        }
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (bCameraGranted) {
                doActionCreatePhoto()
            }
        }

        if (requestCode == PERMISSION_REQUEST_CAMERA_FOR_ORDER) {
            if (bCameraGranted) {
                if (mParamsToCreateDocsWhenRequestPermissions != null) {
                    createOrderPre(
                        mParamsToCreateDocsWhenRequestPermissions?.client_id,
                        mParamsToCreateDocsWhenRequestPermissions?.distr_point_id
                    )
                }
            }
        }

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (bStorageGranted) {
                val extras = getIntent().getExtras()
                onReady(extras)
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                    // Пользователь установил пометку "Не спрашивать"
                    showDialog(IDD_COULD_NOT_START_BUT_PERMISSIONS)
                } else {
                    Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        R.string.error_no_permissions_to_sd,
                        Snackbar.LENGTH_LONG
                    ).show()

                    //toast.setText();
                    //toast.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.background_toast));
                    //toast.setGravity(Gravity.CENTER, 0, 0);
                    //toast.setDuration(Toast.LENGTH_LONG);
                    //toast.show();
                    finish()
                }
            }
        }
    }

    fun startGPS() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //ActivityCompat.requestPermissions(this, new String[]{permission.toStringValue()}, permission.ordinal());
                val g = MySingleton.getInstance()
                val permissionsCursor = getContentResolver().query(
                    MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI,
                    arrayOf<String>("datetime", "seance_incoming"),
                    "permission_name=?",
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    null
                )
                if (permissionsCursor!!.moveToFirst()) {
                    val seanceIncomingIndex = permissionsCursor.getColumnIndex("seance_incoming")
                    val seanceIncoming = permissionsCursor.getString(seanceIncomingIndex)
                    if (seanceIncoming == g.MyDatabase.m_seance.toString()) {
                        // в этом сеансе уже спрашивали
                        return
                    }
                }
                // Записываем время, когда запросили разрешения
                // (чтобы 2 раза не спрашивать)
                val cv = ContentValues()
                cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION)
                cv.put("datetime", Common.getDateTimeAsString14(null))
                cv.put("seance_incoming", g.MyDatabase.m_seance.toString())
                getContentResolver().insert(
                    MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI,
                    cv
                )

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS
                )
            } else {
                val criteria = Criteria()
                criteria.setAccuracy(Criteria.ACCURACY_COARSE)
                val provider = m_locationManager!!.getBestProvider(criteria, true)
                if (provider != null) {
                    m_locationManager!!.requestLocationUpdates(provider, 0, 0f, locListener)
                }
            }
        } else {
            val criteria = Criteria()
            criteria.setAccuracy(Criteria.ACCURACY_FINE)
            val provider = m_locationManager!!.getBestProvider(criteria, true)
            if (provider != null) {
                m_locationManager!!.requestLocationUpdates(provider, 0, 0f, locListener)
            }
        }
    }

    override fun onUniversalEventListener(event: String, parameters: Bundle?) {
        var parameters = parameters
        val fragmentManager = getSupportFragmentManager()
        val fragment = fragmentManager.findFragmentById(R.id.content_frame)

        if (event == "ExchangeConnect") {
            //checkFirebaseToken();

            checkSendImages()
            if (m_imagesToSendSize > 0) {
                showDialog(IDD_QUERY_SEND_IMAGES, null)
            } else {
                m_bExcludeImageMessages = false
                ExchangeTask().execute(1, 0, "")
            }
        }

        if (event == "ExchangeRefresh") {
            val g = MySingleton.getInstance()

            if (g.Common.PHARAOH) {
                g.MyDatabase.m_occupied_places_version = -1
                ExchangeTask().execute(3, 0, null)
            } else {
                // Установим отрицательные версии
                setNegativeVersions()

                //Toast.makeText(MainActivity.this,  getResources().getString(R.string.message_query_all_warning), Toast.LENGTH_LONG).show();
                m_bRefreshPressed = true

                // И обычный обмен
                //checkFirebaseToken();
                checkSendImages()
                if (m_imagesToSendSize > 0) {
                    showDialog(IDD_QUERY_SEND_IMAGES, null)
                } else {
                    m_bExcludeImageMessages = false
                    ExchangeTask().execute(1, 0, "")
                }
            }
        }

        if (event == "ExchangeQueryDocs") {
            showDialog(IDD_QUERY_DOCUMENTS_PERIOD, null)
        }

        if (event == "ExchangeReceive") {
            ExchangeTask().execute(0, 0, "")
        }

        if (event == "ExchangeNomenclaturePhotos") {
            ExchangeTask().execute(2, 0, "")
        }

        if (event == "ExchangeWebService") {
            ExchangeTask().execute(3, 0, null)
        }

        // Указали клиента, либо стерли
        if (event == "FilterClient") {
            val client_id = parameters!!.getString("client_id")
            if (client_id != null) m_client_id = MyID(client_id)
            else m_client_id = MyID()
            m_client_descr = parameters.getString("client_descr")
            if (fragment is OrdersListFragment) {
                val ordersListFragment = fragment
                ordersListFragment.onFilterClientSelected(client_id, m_client_descr)
            }
            if (fragment is PaymentsListFragment) {
                val paymentsListFragment = fragment
                paymentsListFragment.onFilterClientSelected(client_id, m_client_descr)
            }
        }

        if (event == "createOrder") {
            //String client_id=parameters.getString("client_id");
            val distr_point_id =
                if (parameters != null) parameters.getString("distr_point_id") else null
            if (distr_point_id == null) {
                // Создание документа из журнала
                createOrder(if (m_client_id == null) null else m_client_id.toString(), null)
            } else {
                // Создание документа из маршрута
                val clients_ids =
                    TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id)
                when (clients_ids.size) {
                    0 -> Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.no_clients_for_trade_point),
                        Snackbar.LENGTH_LONG
                    ).show()

                    1 -> {
                        val entry = clients_ids.entries.iterator().next()
                        val client_id = entry.key
                        createOrder(client_id, distr_point_id)
                    }

                    else -> {
                        parameters = Bundle()
                        parameters.putSerializable("clients_array", clients_ids)
                        parameters.putString("distr_point_id", distr_point_id)
                        parameters.putString("doc_type", "order")
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters)
                    }
                }
            }
        }

        if (event == "editOrder") {
            editOrder(
                parameters!!.getLong("id"),
                parameters!!.getBoolean("copy_order"),
                parameters!!.getString("client_id"),
                parameters!!.getString("distr_point_id")
            )
        }

        if (event == "cancelOrder") {
            cancelOrder(parameters!!.getLong("id"))
        }

        if (event == "deleteOrder") {
            deleteOrder(parameters!!.getLong("id"))
        }

        if (event == "createRefund") {
            val distr_point_id = parameters!!.getString("distr_point_id")
            if (distr_point_id == null) {
                // Создание документа из журнала
                // (не уверен, что это используется, скоррее всего там editRefund всегда)
                editRefund(0, false, 0, m_client_id, null)
            } else {
                // Создание документа из маршрута
                val clients_ids =
                    TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id)
                when (clients_ids.size) {
                    0 -> Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.no_clients_for_trade_point),
                        Snackbar.LENGTH_LONG
                    ).show()

                    1 -> {
                        val entry = clients_ids.entries.iterator().next()
                        val client_id = entry.key
                        editRefund(0, false, 0, MyID(client_id), MyID(distr_point_id))
                    }

                    else -> {
                        parameters = Bundle()
                        parameters.putSerializable("clients_array", clients_ids)
                        parameters.putString("distr_point_id", distr_point_id)
                        parameters.putString("doc_type", "refund")
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters)
                    }
                }
            }
        }

        if (event == "editRefund") {
            editRefund(
                parameters!!.getLong("id"),
                parameters!!.getBoolean("copy_refund"),
                parameters!!.getLong("order_id"),
                MyID(
                    parameters!!.getString("client_id")
                ),
                MyID(parameters!!.getString("distr_point_id"))
            )
        }

        if (event == "cancelRefund") {
            cancelRefund(parameters!!.getLong("id"))
        }

        if (event == "deleteRefund") {
            deleteRefund(parameters!!.getLong("id"))
        }

        if (event == "createDistribs") {
            val distr_point_id =
                if (parameters != null) parameters.getString("distr_point_id") else null
            if (distr_point_id == null) {
                // Создание документа из журнала
                editDistribs(0, false, m_client_id, MyID())
            } else {
                // Создание документа из маршрута
                val clients_ids =
                    TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id)
                when (clients_ids.size) {
                    0 -> Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.no_clients_for_trade_point),
                        Snackbar.LENGTH_LONG
                    ).show()

                    1 -> {
                        val entry = clients_ids.entries.iterator().next()
                        val client_id = entry.key
                        editDistribs(0, false, MyID(client_id), MyID(distr_point_id))
                    }

                    else -> {
                        parameters = Bundle()
                        parameters.putSerializable("clients_array", clients_ids)
                        parameters.putString("distr_point_id", distr_point_id)
                        parameters.putString("doc_type", "distribs")
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters)
                    }
                }
            }
        }

        if (event == "editDistribs") {
            editDistribs(
                parameters!!.getLong("id"), parameters.getBoolean("copy_distribs"), MyID(
                    parameters!!.getString("client_id")
                ), MyID(parameters!!.getString("distr_point_id"))
            )
        }

        if (event == "cancelDistribs") {
            cancelDistribs(parameters!!.getLong("id"))
        }

        if (event == "deleteDistribs") {
            deleteDistribs(parameters!!.getLong("id"))
        }

        if (event == "createPayment") {
            val distr_point_id =
                if (parameters != null) parameters.getString("distr_point_id") else null
            if (distr_point_id == null) {
                // Создание документа из журнала
                createPayment(if (m_client_id == null) null else m_client_id.toString(), null)
            } else {
                // Создание документа из маршрута
                val clients_ids =
                    TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id)
                when (clients_ids.size) {
                    0 -> Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.no_clients_for_trade_point),
                        Snackbar.LENGTH_LONG
                    ).show()

                    1 -> {
                        val entry = clients_ids.entries.iterator().next()
                        val client_id = entry.key
                        createPayment(client_id, distr_point_id)
                    }

                    else -> {
                        parameters = Bundle()
                        parameters.putSerializable("clients_array", clients_ids)
                        parameters.putString("distr_point_id", distr_point_id)
                        parameters.putString("doc_type", "payment")
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters)
                    }
                }
            }
        }

        if (event == "editPayment") {
            editPayment(
                parameters!!.getLong("id"), parameters.getBoolean("copy_payment"), MyID(
                    parameters!!.getString("client_id")
                ), MyID(parameters!!.getString("distr_point_id"))
            )
        }

        if (event == "deletePayment") {
            deletePayment(parameters!!.getLong("id"))
        }

        if (event == "createMessage") {
            editMessage(0, "")
        }

        if (event == "editMessage") {
            editMessage(parameters!!.getLong("id"), parameters.getString("photo"))
        }

        if (event == "startVisit") {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_START_VISIT, parameters)
        }

        if (event == "cancelVisit") {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_CANCEL_VISIT, parameters)
        }

        if (event == "endVisit") {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_END_VISIT, parameters)
        }

        if (event == "routeAndDateSelected") {
            val route_id = parameters!!.getString("route_id")
            if (route_id != null) m_route_id = MyID(route_id)
            else m_route_id = MyID()
            m_route_date = parameters.getString("route_date")
            m_route_descr = parameters.getString("route_descr")
            if (fragment is RoutesListFragment) {
                val routesListFragment = fragment
                routesListFragment.onRouteSelected(
                    m_route_id.toString(),
                    m_route_date,
                    m_route_descr
                )
            }
            /*
        case SELECT_ROUTES_WITH_DATES_REQUEST:
        if (data != null) {
            long id = data.getLongExtra("id", 0);
            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI, id);
            //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
            Cursor cursor = getContentResolver().query(singleUri, new String[]{"route_id", "route_date", "descr"}, null, null, null);
            if (cursor.moveToNext()) {
                int indexRouteId = cursor.getColumnIndex("route_id");
                int indexRouteDate = cursor.getColumnIndex("route_date");
                int indexRouteDescr = cursor.getColumnIndex("descr");

                m_route_date = cursor.getString(indexRouteDate);
                m_route_descr = cursor.getString(indexRouteDescr);
                m_route_id = new MyID(cursor.getString(indexRouteId));
                if (m_route_descr==null)
                {
                    m_route_descr="{"+m_route_id+"}";
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                if (fragment instanceof RoutesListFragment)
                {
                    RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                    routesListFragment.onRouteSelected(m_route_id.toString(), m_route_date, m_route_descr);
                }

            }
            cursor.close();
        }
        break;
             */
        }
    }


    protected inner class MyAlertDialog(var mContext: Context) : AlertDialog(
        mContext
    ) {
        var builder: Builder? = null
        var dialog: AlertDialog? = null

        fun setType(idd_type: Int): MyAlertDialog {
            val g = MySingleton.getInstance()

            builder = Builder(mContext)
            when (idd_type) {
                IDD_SHOW_STATS -> {
                    /*
                                  String selection=E_ORDER_STATE.getStatisticConditionWhere();
                                  String selectionArgs[]=E_ORDER_STATE.getStatisticArgs();
                   
                                  if (m_client_id!=null)
                                  {
                                      selection=selection+" and client_id=?";
                                      List<String> argsList0 = Arrays.asList(selectionArgs);
                                      ArrayList<String> argsList = new ArrayList<String>(argsList0);
                                      argsList.add(m_client_id.toString());
                                      selectionArgs = argsList.toArray(new String[0]);
                                  }
                                  */
                    var conditionString =
                        "editing_backup=0" // чтобы текущий созданный документ в статистику не попал
                    val conditionArgs = ArrayList<String?>()

                    conditionString = Common.combineConditions(
                        conditionString,
                        conditionArgs,
                        E_ORDER_STATE.getStatisticConditionWhere(),
                        E_ORDER_STATE.getStatisticArgs()
                    )

                    if (m_client_id != null && !m_client_id!!.isEmpty()) {
                        conditionString = Common.combineConditions(
                            conditionString, conditionArgs, "client_id=?", arrayOf<String?>(
                                m_client_id!!.m_id
                            )
                        )
                    }
                    if (m_bNotClosed) {
                        conditionString = Common.combineConditions(
                            conditionString,
                            conditionArgs,
                            E_ORDER_STATE.getNotClosedConditionWhere(),
                            E_ORDER_STATE.getNotClosedSelectionArgs()
                        )
                    }

                    if (g.Common.PHARAOH) {
                        //conditionString=Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_date, m_date+"Z"});
                        when (m_filter_date_type) {
                            0 -> {}
                            1 ->                                 // дата
                                conditionString = Common.combineConditions(
                                    conditionString,
                                    conditionArgs,
                                    "(shipping_date between ? and ?)",
                                    arrayOf<String?>(
                                        m_filter_date_begin, m_filter_date_begin + "Z"
                                    )
                                )

                            2 ->                                 // интервал
                                conditionString = Common.combineConditions(
                                    conditionString,
                                    conditionArgs,
                                    "(shipping_date between ? and ?)",
                                    arrayOf<String?>(
                                        m_filter_date_begin, m_filter_date_end + "Z"
                                    )
                                )
                        }
                    } else {
                        //conditionString=Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_date, m_date+"Z"});
                        when (m_filter_date_type) {
                            0 -> {}
                            1 ->                                 // дата
                                conditionString = Common.combineConditions(
                                    conditionString,
                                    conditionArgs,
                                    "(datedoc between ? and ?)",
                                    arrayOf<String?>(
                                        m_filter_date_begin, m_filter_date_begin + "Z"
                                    )
                                )

                            2 ->                                 // интервал
                                conditionString = Common.combineConditions(
                                    conditionString,
                                    conditionArgs,
                                    "(datedoc between ? and ?)",
                                    arrayOf<String?>(
                                        m_filter_date_begin, m_filter_date_end + "Z"
                                    )
                                )
                        }
                    }

                    var count = 0
                    var totalSum = 0.0
                    var totalWeight = 0.0

                    //Cursor cursor=getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"sum_doc", "weight_doc"}, selection, selectionArgs, null);
                    val cursor = getContentResolver().query(
                        MTradeContentProvider.ORDERS_CONTENT_URI,
                        arrayOf<String>("sum_doc", "weight_doc"),
                        conditionString,
                        conditionArgs.toTypedArray<String?>(),
                        null
                    )
                    while (cursor!!.moveToNext()) {
                        count++
                        totalSum += cursor.getDouble(0)
                        totalWeight += cursor.getDouble(1)
                    }

                    val statistics = getString(
                        R.string.orders_statistics,
                        count,
                        Common.DoubleToStringFormat(totalSum, "%.2f"),
                        Common.DoubleToStringFormat(totalWeight, "%.3f")
                    )

                    //builder.setTitle("title");
                    //builder.setMessage("message");
                    builder!!.setMessage(statistics)
                    /*
	          builder.setPositiveButton(android.R.string.ok,
	             new DialogInterface.OnClickListener(){
	                public void onClick(DialogInterface dialog, int item) {
	                   //
	                }
	             });
	          builder.setNegativeButton(android.R.string.cancel, null);
	          */
                    builder!!.setPositiveButton(android.R.string.ok, null)
                }

                IDD_SHOW_PAYMENTS_STATS -> {
                    var selection = E_PAYMENT_STATE.getPaymentStatisticConditionWhere()
                    var selectionArgs = E_PAYMENT_STATE.getPaymentStatisticArgs()

                    if (m_client_id != null && !m_client_id!!.isEmpty()) {
                        selection = selection + " and client_id=?"
                        val argsList0 = Arrays.asList<String?>(*selectionArgs)
                        val argsList = ArrayList<String?>(argsList0)
                        argsList.add(m_client_id.toString())
                        selectionArgs = argsList.toTypedArray<String?>()
                    }

                    var count = 0
                    var totalSum = 0.0

                    val cursor = getContentResolver().query(
                        MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                        arrayOf<String>("sum_doc"),
                        selection,
                        selectionArgs,
                        null
                    )
                    while (cursor!!.moveToNext()) {
                        count++
                        totalSum += cursor.getDouble(0)
                    }

                    val statistics = getString(
                        R.string.payments_statistics,
                        count,
                        Common.DoubleToStringFormat(totalSum, "%.2f")
                    )
                    builder!!.setMessage(statistics)
                    builder!!.setPositiveButton(android.R.string.ok, null)
                }
            }
            return this
        }

        override fun show() {
            dialog = builder!!.create()
            dialog!!.show()
        }
    }


    override fun onPrepareDialog(id: Int, dialog: Dialog, bundle: Bundle) {
        super.onPrepareDialog(id, dialog)
        val g = MySingleton.getInstance()

        when (id) {
            IDD_CANCEL_CURRENT_ORDER, IDD_DELETE_CURRENT_ORDER -> {
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        bundle.getLong("_id")
                    ), arrayOf<String>("order_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    m_order_id_to_process = cursor.getLong(0)
                }
                cursor.close()
            }

            IDD_CANCEL_CURRENT_REFUND, IDD_DELETE_CURRENT_REFUND -> {
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        bundle.getLong("_id")
                    ), arrayOf<String>("refund_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    m_refund_id_to_process = cursor.getLong(0)
                }
                cursor.close()
            }

            IDD_CANCEL_CURRENT_DISTRIBS, IDD_DELETE_CURRENT_DISTRIBS -> {
                val cursor = getContentResolver().query(
                    ContentUris.withAppendedId(
                        MTradeContentProvider.JOURNAL_CONTENT_URI,
                        bundle.getLong("_id")
                    ), arrayOf<String>("distribs_id"), null, null, null
                )
                if (cursor!!.moveToNext()) {
                    m_distribs_id_to_process = cursor.getLong(0)
                }
                cursor.close()
            }

            IDD_CANCEL_CURRENT_PAYMENT, IDD_DELETE_CURRENT_PAYMENT -> {
                m_payment_id_to_process =
                    bundle.getLong("_id")
            }

            IDD_DELETE_CURRENT_MESSAGE -> {
                m_message_id_to_process = bundle.getLong("_id")
            }
            IDD_DATA_FILTER -> {
                val checkBoxOrders = dialog.findViewById<View?>(R.id.checkBoxOrders) as CheckBox
                val checkBoxRefunds = dialog.findViewById<View?>(R.id.checkBoxRefunds) as CheckBox
                val checkBoxDistribs = dialog.findViewById<View?>(R.id.checkBoxDistribs) as CheckBox
                val spinnerFilterType =
                    dialog.findViewById<View?>(R.id.spinnerFilterType) as Spinner

                m_filter_type_current = m_filter_type
                m_filter_date_type_current = m_filter_date_type
                if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                    checkBoxOrders.setVisibility(View.VISIBLE)
                    checkBoxOrders.setChecked(m_filter_orders)
                    checkBoxRefunds.setVisibility(View.VISIBLE)
                    checkBoxRefunds.setChecked(m_filter_refunds)
                    checkBoxDistribs.setVisibility(View.VISIBLE)
                    checkBoxDistribs.setChecked(m_filter_distribs)
                } else {
                    checkBoxOrders.setVisibility(View.GONE)
                    checkBoxRefunds.setVisibility(View.GONE)
                    checkBoxDistribs.setVisibility(View.GONE)
                }
                if (g.Common.PHARAOH) {
                    spinnerFilterType.setVisibility(View.VISIBLE)
                } else {
                    spinnerFilterType.setVisibility(View.GONE)
                }

                val spinnerFilterPeriod =
                    dialog.findViewById<View?>(R.id.spinnerFilterPeriod) as Spinner

                if (m_filter_date_type < 0 || m_filter_date_type >= spinnerFilterPeriod.getCount()) spinnerFilterPeriod.setSelection(
                    0
                )
                else spinnerFilterPeriod.setSelection(m_filter_date_type)

                val linearLayoutStartDate =
                    dialog.findViewById<View?>(R.id.linearLayoutStartDate) as LinearLayout
                val linearLayoutEndDate =
                    dialog.findViewById<View?>(R.id.linearLayoutEndDate) as LinearLayout

                when (m_filter_date_type) {
                    0 -> {
                        linearLayoutStartDate.setVisibility(View.GONE)
                        linearLayoutEndDate.setVisibility(View.GONE)
                    }

                    1 -> {
                        linearLayoutStartDate.setVisibility(View.VISIBLE)
                        linearLayoutEndDate.setVisibility(View.GONE)
                    }

                    2 -> {
                        linearLayoutStartDate.setVisibility(View.VISIBLE)
                        linearLayoutEndDate.setVisibility(View.VISIBLE)
                    }
                }

                if (m_filter_date_begin_current.length >= 8) {
                    val etFirstDate = dialog.findViewById<View?>(R.id.etFirstDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_filter_date_begin_current.substring(
                            6,
                            8
                        ) + "." + m_filter_date_begin_current.substring(
                            4,
                            6
                        ) + "." + m_filter_date_begin_current.substring(0, 4)
                    )
                    etFirstDate.setText(sb.toString())
                }

                if (m_filter_date_end_current.length >= 8) {
                    val etSecondDate = dialog.findViewById<View?>(R.id.etSecondDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_filter_date_end_current.substring(
                            6,
                            8
                        ) + "." + m_filter_date_end_current.substring(
                            4,
                            6
                        ) + "." + m_filter_date_end_current.substring(0, 4)
                    )
                    etSecondDate.setText(sb.toString())
                }

                //
                // Кнопка выбора даты
                val buttonSelectFirstDate =
                    dialog.findViewById<View?>(R.id.buttonSelectFirstDate) as Button
                buttonSelectFirstDate.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        showDialog(IDD_FILTER_DATE_BEGIN)
                    }
                })

                //
                // Кнопка выбора даты
                val buttonSelectSecondDate =
                    dialog.findViewById<View?>(R.id.buttonSelectSecondDate) as Button
                buttonSelectSecondDate.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        showDialog(IDD_FILTER_DATE_END)
                    }
                })
            }

            IDD_FILTER_DATE_BEGIN -> {
                var day = 0
                var month = 0
                var year = 0
                if (m_filter_date_begin_current.length >= 8) {
                    day = m_filter_date_begin_current.substring(6, 8).toInt()
                    month = m_filter_date_begin_current.substring(4, 6).toInt() - 1
                    year = m_filter_date_begin_current.substring(0, 4).toInt()
                    (dialog as DatePickerDialog).updateDate(year, month, day)
                }
            }

            IDD_FILTER_DATE_END -> {
                var day = 0
                var month = 0
                var year = 0
                if (m_filter_date_end_current.length >= 8) {
                    day = m_filter_date_end_current.substring(6, 8).toInt()
                    month = m_filter_date_end_current.substring(4, 6).toInt() - 1
                    year = m_filter_date_end_current.substring(0, 4).toInt()
                    (dialog as DatePickerDialog).updateDate(year, month, day)
                }
            }

            IDD_QUERY_DOCUMENTS_PERIOD -> {
                m_query_period_type_current = m_query_period_type

                val spinnerPeriodType =
                    dialog.findViewById<View?>(R.id.spinnerPeriodType) as Spinner

                if (m_query_period_type < 0 || m_query_period_type >= spinnerPeriodType.getCount()) spinnerPeriodType.setSelection(
                    0
                )
                else spinnerPeriodType.setSelection(m_query_period_type)

                if (m_query_date_begin_current.length >= 8) {
                    val etFirstDate = dialog.findViewById<View?>(R.id.etFirstDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_query_date_begin_current.substring(
                            6,
                            8
                        ) + "." + m_query_date_begin_current.substring(
                            4,
                            6
                        ) + "." + m_query_date_begin_current.substring(0, 4)
                    )
                    etFirstDate.setText(sb.toString())
                }

                if (m_query_date_end_current.length >= 8) {
                    val etSecondDate = dialog.findViewById<View?>(R.id.etSecondDate) as EditText
                    val sb = StringBuilder()
                    sb.append(
                        m_query_date_end_current.substring(
                            6,
                            8
                        ) + "." + m_query_date_end_current.substring(
                            4,
                            6
                        ) + "." + m_query_date_end_current.substring(0, 4)
                    )
                    etSecondDate.setText(sb.toString())
                }

                //
                // Кнопка выбора даты
                val buttonSelectFirstDate =
                    dialog.findViewById<View?>(R.id.buttonSelectFirstDate) as Button
                buttonSelectFirstDate.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        showDialog(IDD_QUERY_DOCUMENTS_DATE_BEGIN)
                    }
                })

                //
                // Кнопка выбора даты
                val buttonSelectSecondDate =
                    dialog.findViewById<View?>(R.id.buttonSelectSecondDate) as Button
                buttonSelectSecondDate.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        showDialog(IDD_QUERY_DOCUMENTS_DATE_END)
                    }
                })
            }

            IDD_QUERY_DOCUMENTS_DATE_BEGIN -> {
                var day = 0
                var month = 0
                var year = 0
                if (m_query_date_begin_current.length >= 8) {
                    day = m_query_date_begin_current.substring(6, 8).toInt()
                    month = m_query_date_begin_current.substring(4, 6).toInt() - 1
                    year = m_query_date_begin_current.substring(0, 4).toInt()
                    (dialog as DatePickerDialog).updateDate(year, month, day)
                }
            }

            IDD_QUERY_DOCUMENTS_DATE_END -> {
                var day = 0
                var month = 0
                var year = 0
                if (m_query_date_end_current.length >= 8) {
                    day = m_query_date_end_current.substring(6, 8).toInt()
                    month = m_query_date_end_current.substring(4, 6).toInt() - 1
                    year = m_query_date_end_current.substring(0, 4).toInt()
                    (dialog as DatePickerDialog).updateDate(year, month, day)
                }
            }

            IDD_QUERY_START_VISIT -> {
                val distr_point_id = bundle.getString("distr_point_id")
                val alertDialog = dialog as AlertDialog
                alertDialog.setMessage(
                    getString(
                        R.string.query_start_visit,
                        bundle.getString("distr_point_descr")
                    )
                )
                alertDialog.setButton(
                    Dialog.BUTTON_POSITIVE,
                    getString(R.string.ok),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                            val fragmentManager = getSupportFragmentManager()
                            val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                            if (fragment is RoutesListFragment) {
                                val routesListFragment = fragment
                                routesListFragment.startVisit(distr_point_id)
                                m_bNeedCoord = true
                                startGPS()
                            }
                        }
                    })
            }

            IDD_QUERY_END_VISIT -> {
                val distr_point_id = bundle.getString("distr_point_id")
                val alertDialog = dialog as AlertDialog
                alertDialog.setMessage(
                    getString(
                        R.string.query_end_visit,
                        bundle.getString("distr_point_descr")
                    )
                )
                alertDialog.setButton(
                    Dialog.BUTTON_POSITIVE,
                    getString(R.string.ok),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                            val fragmentManager = getSupportFragmentManager()
                            val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                            if (fragment is RoutesListFragment) {
                                val routesListFragment = fragment
                                routesListFragment.endVisit(distr_point_id)
                            }
                        }
                    })
            }

            IDD_QUERY_CANCEL_VISIT -> {
                val distr_point_id = bundle.getString("distr_point_id")
                val alertDialog = dialog as AlertDialog
                alertDialog.setMessage(
                    getString(
                        R.string.query_cancel_visit,
                        bundle.getString("distr_point_descr")
                    )
                )
                alertDialog.setButton(
                    Dialog.BUTTON_POSITIVE,
                    getString(R.string.ok),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                            val fragmentManager = getSupportFragmentManager()
                            val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                            if (fragment is RoutesListFragment) {
                                val routesListFragment = fragment
                                routesListFragment.cancelVisit(distr_point_id)
                            }
                        }
                    })
            }


            IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS -> {
                val alertDialog = dialog as AlertDialog
                val arrayAdapter: ArrayAdapter<MyElementOfArrayList?> =
                    alertDialog.getListView().getAdapter() as ArrayAdapter<MyElementOfArrayList?>
                val clients =
                    bundle.getSerializable("clients_array") as HashMap<String?, String?>?
                val distr_point_id = bundle.getString("distr_point_id")
                val doc_type = bundle.getString("doc_type")

                if (doc_type == "order") alertDialog.setTitle(getString(R.string.select_client_to_create_order))
                if (doc_type == "refund") alertDialog.setTitle(getString(R.string.select_client_to_create_refund))
                if (doc_type == "payment") alertDialog.setTitle(getString(R.string.select_client_to_create_payment))
                if (doc_type == "distribs") alertDialog.setTitle(getString(R.string.select_client_to_create_distribs))


                arrayAdapter.clear()
                for (entry in clients!!.entries) {
                    arrayAdapter.add(MyElementOfArrayList(entry.key, entry.value))
                }
                alertDialog.getListView().setItemChecked(0, true)
                alertDialog.setButton(
                    Dialog.BUTTON_POSITIVE,
                    getString(R.string.ok),
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                            val listView = alertDialog.getListView()
                            val element =
                                listView.getItemAtPosition(listView.getCheckedItemPosition()) as MyElementOfArrayList
                            if (doc_type == "order") createOrder(element.getId(), distr_point_id)
                            if (doc_type == "refund") {
                                editRefund(0, false, 0, MyID(element.getId()), MyID(distr_point_id))
                            }
                            if (doc_type == "payment") createPayment(
                                element.getId(),
                                distr_point_id
                            )
                            if (doc_type == "distribs") {
                                editDistribs(0, false, MyID(element.getId()), MyID(distr_point_id))
                            }
                        }
                    })
            }


        }
    }

    override fun onSearchRequested(): Boolean {
        return super.onSearchRequested()
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {
        // TODO не знаю, откуда появилось это событие
    }

    public override fun onNewIntent(queryIntent: Intent) {
        super.onNewIntent(queryIntent)
        setIntent(queryIntent)
        //Toast.makeText(this, "New intent", 3000).show();
        /*
		//Получаем Intent
	    Intent intent = getIntent();
		//Проверяем тип Intent
	    if (Intent.ACTION_SEARCH.equals(intent.getAction()))
	    {
	       //Берем строку запроса из экстры
	       String query = intent.getStringExtra(SearchManager.QUERY);
	       //Выполняем поиск
	       //showResults(query);
	       Toast.makeText(this, query, 30);
		   Toast.makeText(this, "Start2", 3000).show();

	       return;
	     }
		Toast.makeText(this, "Start1", 3000).show();
		*/
        val queryAction = queryIntent.getAction()
        if (Intent.ACTION_SEARCH == queryAction) {
            //String[] fromColumns = {"numdoc", "numdoc"};
            //int[] toViews = {android.R.id.text1, android.R.id.text2}; // The TextView in simple_list_item_1
            //mAdapter = new SimpleCursorAdapter(this,
            //        android.R.layout.simple_list_item_2, null,
            //        fromColumns, toViews, 0);
            //lvOrders.setAdapter(mAdapter);
            //getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    // если режим маршрута, добавим туда заказ
    fun updateDocumentInRoute(distr_point_id: String?, `object`: Any?) {
        val fragmentManager = getSupportFragmentManager()
        val fragment = fragmentManager.findFragmentById(R.id.content_frame)
        if (fragment is RoutesListFragment) {
            val routesListFragment = fragment
            routesListFragment.updateDocumentInRoute(distr_point_id, `object`)
        }
    }

    // если режим маршрута, добавим туда заказ
    fun updateDocumentInRouteDeleted(document_uid: String?) {
        val fragmentManager = getSupportFragmentManager()
        val fragment = fragmentManager.findFragmentById(R.id.content_frame)
        if (fragment is RoutesListFragment) {
            val routesListFragment = fragment
            routesListFragment.updateDocumentInRouteDeleted(document_uid)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu)

        return true
    }


    val isOrdersMode: Boolean
        get() {
            /*
            boolean bOrdersMode = true;
            MySlider slidingDrawer1 = (MySlider) findViewById(R.id.slidingDrawer1);

            boolean sliderIsMoving = slidingDrawer1.isMoving();
            boolean sliderIsOpened = slidingDrawer1.isOpened();

            if (!sliderIsMoving && sliderIsOpened) {
                bOrdersMode = false;
            } else if (sliderIsMoving && !sliderIsOpened) {
                bOrdersMode = false;
            }
            //Log.d(LOG_TAG, "Slider IsMoving="+sliderIsMoving);
            //Log.d(LOG_TAG, "Slider IsOpened="+sliderIsOpened);
            return bOrdersMode;
            */
            return true
        }

    val isFilterSet: Boolean
        get() {
            // Если установлен фильтр (без учета отбора по клиенту)
            val g = MySingleton.getInstance()
            if (m_filter_date_type != 0) return true
            if (m_filter_orders && m_filter_refunds && m_filter_distribs || !(g.Common.PRODLIDER || g.Common.TANDEM)) return false
            return true
        }

    // обновление меню
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        //menu.clear();
        val g = MySingleton.getInstance()
        g_options_menu = menu
        when (m_current_frame_type) {
            FRAME_TYPE_ROUTES -> {
                menu.setGroupVisible(R.id.menu_group_orders, false)
                menu.setGroupVisible(R.id.menu_group_payments, false)
                menu.setGroupVisible(R.id.menu_group_exchange, false)
                menu.setGroupVisible(R.id.menu_group_messages, false)
                menu.setGroupVisible(R.id.menu_group_routes, true)

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val item = menu.findItem(R.id.action_check_box_only_not_worked_out)
                item.setChecked(sharedPreferences.getBoolean("routes_show_only_worked_out", false))
            }

            FRAME_TYPE_ORDERS -> {
                //boolean bOrdersMode = true;
                //if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                //    bOrdersMode = getIsOrdersMode();
                //}
                menu.setGroupVisible(R.id.menu_group_orders, true)
                val itemDataFilterAsAction = menu.findItem(R.id.action_data_filter_as_action)
                if (!this.isFilterSet) {
                    itemDataFilterAsAction.setVisible(false)
                }
                val itemCreateRefund = menu.findItem(R.id.action_create_refund)
                val itemCreateDistribs = menu.findItem(R.id.action_create_distribs)
                //MenuItem itemDeleteClosedRefunds = menu.findItem(R.id.action_delete_closed_refunds);
                val itemDeleteClosedOrders = menu.findItem(R.id.action_delete_closed_orders)
                val itemDeleteClosedDocuments = menu.findItem(R.id.action_delete_closed_documents)
                if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                    itemCreateRefund.setVisible(true)
                    itemCreateDistribs.setVisible(true)
                    //itemDeleteClosedRefunds.setVisible(true);
                    itemDeleteClosedOrders.setVisible(false)
                    itemDeleteClosedDocuments.setVisible(true)
                } else {
                    itemCreateRefund.setVisible(false)
                    itemCreateDistribs.setVisible(false)
                    //itemDeleteClosedRefunds.setVisible(false);
                    itemDeleteClosedOrders.setVisible(true)
                    itemDeleteClosedDocuments.setVisible(false)
                }
                menu.setGroupVisible(R.id.menu_group_payments, false)
                menu.setGroupVisible(R.id.menu_group_exchange, false)
                menu.setGroupVisible(R.id.menu_group_messages, false)
                menu.setGroupVisible(R.id.menu_group_routes, false)
            }

            FRAME_TYPE_PAYMENTS -> {
                menu.setGroupVisible(R.id.menu_group_orders, false)
                menu.setGroupVisible(R.id.menu_group_payments, true)
                menu.setGroupVisible(R.id.menu_group_exchange, false)
                menu.setGroupVisible(R.id.menu_group_messages, false)
            }

            FRAME_TYPE_EXCHANGE -> {
                menu.setGroupVisible(R.id.menu_group_orders, false)
                menu.setGroupVisible(R.id.menu_group_payments, false)
                menu.setGroupVisible(R.id.menu_group_exchange, true)
                menu.setGroupVisible(R.id.menu_group_messages, false)
                menu.setGroupVisible(R.id.menu_group_routes, false)
            }

            FRAME_TYPE_MESSAGES -> {
                menu.setGroupVisible(R.id.menu_group_orders, false)
                menu.setGroupVisible(R.id.menu_group_payments, false)
                menu.setGroupVisible(R.id.menu_group_exchange, false)
                menu.setGroupVisible(R.id.menu_group_messages, true)
                menu.setGroupVisible(R.id.menu_group_routes, false)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun checkGeoEnabled(): Boolean {
        val mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        //return mIsGeoDisabled;
        if (!mIsGPSEnabled) {
            showDialog(IDD_NEED_GPS_ENABLED)
        }
        return mIsGPSEnabled
    }

    fun createOrder(client_id: String?, distr_point_id: String?) {
        val g = MySingleton.getInstance()
        if (g.Common.TITAN || g.Common.FACTORY) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                mParamsToCreateDocsWhenRequestPermissions = ParamsToCreateDocs()
                // Более правильного способа передачи параметров не придумал :)
                mParamsToCreateDocsWhenRequestPermissions?.client_id = client_id
                mParamsToCreateDocsWhenRequestPermissions?.distr_point_id = distr_point_id
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.CAMERA),
                    PERMISSION_REQUEST_CAMERA_FOR_ORDER
                )
            } else {
                createOrderPre(client_id, distr_point_id)
            }
        } else if (g.Common.PRODLIDER) {
            if (checkGeoEnabled()) editOrder(0, false, client_id, distr_point_id)
        } else {
            editOrder(0, false, client_id, distr_point_id)
        }
    }

    fun createPayment(client_id: String?, distr_point_id: String?) {
        val g = MySingleton.getInstance()
        if (g.Common.PRODLIDER) {
            if (checkGeoEnabled()) editPayment(0, false, MyID(client_id), MyID(distr_point_id))
        } else editPayment(0, false, MyID(client_id), MyID(distr_point_id))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // TODO Auto-generated method stub
        //StringBuilder sb = new StringBuilder();
        val g = MySingleton.getInstance()

        // Выведем в TextView информацию о нажатом пункте меню
        //sb.append("Item Menu");
        //sb.append("\r\n groupId: " + String.valueOf(item.getGroupId()));
        //sb.append("\r\n itemId: " + String.valueOf(item.getItemId()));
        //sb.append("\r\n order: " + String.valueOf(item.getOrder()));
        //sb.append("\r\n title: " + item.getTitle());
        //tv.setText(sb.toString());
        when (item.getItemId()) {
            R.id.action_create_order -> createOrder(
                if (m_client_id != null) m_client_id.toString() else null,
                null
            )

            R.id.action_delete_closed_orders, R.id.action_delete_closed_documents -> showDialog(
                IDD_DELETE_CLOSED_DOCUMENTS
            )

            R.id.action_sort_orders -> showDialog(IDD_SORT_ORDERS)
            R.id.action_show_stats -> MyAlertDialog(this).setType(IDD_SHOW_STATS).show()
            R.id.action_data_filter, R.id.action_data_filter_as_action -> showDialog(IDD_DATA_FILTER)
            R.id.action_create_payment -> createPayment(
                if (m_client_id == null) null else m_client_id.toString(),
                null
            )

            R.id.action_delete_closed_payments -> showDialog(IDD_DELETE_CLOSED_PAYMENTS)
            R.id.action_create_refund -> editRefund(0, false, 0, m_client_id, MyID())
            R.id.action_create_distribs -> if (g.Common.PRODLIDER) {
                if (checkGeoEnabled()) editDistribs(0, false, m_client_id, MyID())
            } else editDistribs(0, false, m_client_id, MyID())

            R.id.action_show_payments_stats -> MyAlertDialog(this).setType(IDD_SHOW_PAYMENTS_STATS)
                .show()

            R.id.action_create_message -> editMessage(0, "")
            R.id.action_create_photo -> {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(Manifest.permission.CAMERA),
                        PERMISSION_REQUEST_CAMERA
                    )
                } else {
                    doActionCreatePhoto()
                }
            }

            R.id.action_delete_readed_messages -> showDialog(IDD_DELETE_READED_MESSAGES)
            R.id.action_refresh_route -> {
                val fragmentManager = getSupportFragmentManager()
                val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                if (fragment is RoutesListFragment) {
                    val routesListFragment = fragment
                    val bResult = routesListFragment.fillRealRoutes(m_route_date)
                    //routesListFragment.restartLoaderForListView();
                    if (bResult) Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.route_refreshed_successfully),
                        Snackbar.LENGTH_LONG
                    ).show()
                    else Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        getString(R.string.route_cant_be_refreshed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

            R.id.action_check_box_only_not_worked_out -> {
                val fragmentManager = getSupportFragmentManager()
                val fragment = fragmentManager.findFragmentById(R.id.content_frame)
                if (fragment is RoutesListFragment) {
                    val routesListFragment = fragment
                    val isChecked = !item.isChecked()
                    item.setChecked(isChecked)

                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

                    val editor = sharedPreferences.edit()
                    editor.putBoolean("routes_show_only_worked_out", isChecked)
                    editor.commit()

                    routesListFragment.setOnlyNotWorkedOut(isChecked)
                }
            }

            R.id.action_settings -> {
                // временно здесь
                // 1. Пересчет продаж
                //ContentValues cv=new ContentValues();
                //cv.put("datebegin", "20130101");
                //getContentResolver().insert(MTradeContentProvider.SALES_CONTENT_URI, cv);
                //Toast.makeText(MainActivity.this, "Итоги продаж пересчитаны", Toast.LENGTH_SHORT).show();
                // 2. Восстановление заявок из текстовых файлов
                //recoverOrders();
                // 3. Ввод количества
                //Intent intent=new Intent(MainActivity.this, QuantityActivity.class);
                //startActivityForResult(intent, 0);
                val intent = Intent(this@MainActivity, PrefActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (g.m_preferences_from_ini) intent.putExtra("readOnly", true)
                startActivity(intent)
            }

            R.id.action_backup -> doActionBackup()
            R.id.action_vacuum_database -> doVacuumDatabase()
        }

        return super.onOptionsItemSelected(item)
    }

    fun doActionCreatePhoto() {
        val temp_fileName = "tmp.jpg"

        //File photoDir = new File(Environment.getExternalStorageDirectory() + photoFolder);
        val photoDir = File(getFilesDir(), "photos")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        // к сожалению приходится устанавливать параметр EXTRA_OUTPUT
        // т.к. есть телефоны с багом, которые не возвращают uri, а только data (небольшую картинку)
        // у меня такой телефон создает только 1 файл в этом случае
        // другие же с указанием EXTRA_OUTPUT создают сразу 2 файла, но пусть будет так
        val temp_photoFile = File(photoDir, temp_fileName)
        // До 21.08.2018
        //outputPhotoFileUri=Uri.fromFile(temp_photoFile);
        // Вариант решения
        outputPhotoFileUri =
            FileProvider.getUriForFile(this, "ru.code22.fileprovider", temp_photoFile)

        //outputPhotoFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".my.package.name.provider", temp_photoFile);

        //Intent intent=new Intent(MainActivity.this, PhotoActivity.class);
        //startActivityForResult(intent, CREATE_PHOTO_MESSAGE_REQUEST);

        // https://stackoverflow.com/questions/24467696/android-file-provider-permission-denial
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, "new-photo-name.jpg");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPhotoFileUri)
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        //cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 90);
        cameraIntent.putExtra(
            "return-data",
            false
        ) // мне кажется что этот флаг все равно игнорируется, как будто указываем все равно true
        // Второй вариант решения от 21.08.2018
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                cameraIntent.setClipData(ClipData.newRawUri("", outputPhotoFileUri))
            }
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        //startActivityForResult(cameraIntent, CAMERA_REQUEST);
        createCameraActivityResultLauncher!!.launch(cameraIntent)
    }

    fun doActionBackup() {
        try {
            //File sd = g.Common.myGetExternalStorageDirectory();
            //File sd = g.Common.getExternalSDCardDirectory();
            val sd = Common.getMyStorageFileDir(
                this@MainActivity,
                ""
            ) //Environment.getExternalStorageDirectory();
            val data = Environment.getDataDirectory()
            if (sd != null && sd.canWrite()) {
                val currentDBPath =
                    "/data/" + getBaseContext().getPackageName() + "/databases/" + MTradeDBHelper.DB_NAME
                val backupDBPath = sd //new File(sd, "/mtrade");
                //if (!backupDBPath.exists()) {
                //    backupDBPath.mkdirs();
                //}
                val currentDB = File(data, currentDBPath)
                val backupDB = File(backupDBPath, "backup.db")
                if (!Common.myCopyFile(currentDB, backupDB, false)) {
                    Snackbar.make(
                        findViewById<View?>(android.R.id.content),
                        R.string.error_during_backup_to_sd,
                        Snackbar.LENGTH_LONG
                    ).show()
                    return
                }
                Snackbar.make(
                    findViewById<View?>(android.R.id.content),
                    backupDB.toString(),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(
                    findViewById<View?>(android.R.id.content),
                    R.string.error_no_sd_card_access,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Snackbar.make(
                findViewById<View?>(android.R.id.content),
                e.toString(),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    fun doVacuumDatabase() {
        getContentResolver().insert(MTradeContentProvider.VACUUM_CONTENT_URI, null)
    }


    //@Override
    //public void onListItemClick(ListView l, View v, int position, long id) {
    //    // Do something when a list item is clicked
    //}
    private val locListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            val g = MySingleton.getInstance()
            if (m_bNeedCoord) {
                m_bNeedCoord = false
                m_locationManager!!.removeUpdates(locListener)
            }

            var accept_coord_where: String?
            var accept_coord_selectionArgs: Array<String?>?
            val cv: ContentValues?

            // Обновим координаты заказов
            accept_coord_where = E_ORDER_STATE.getAcceptCoordConditionWhere()
            accept_coord_selectionArgs = E_ORDER_STATE.getAcceptCoordConditionSelectionArgs()

            val gpsenabled = m_locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            cv = ContentValues()
            cv.put("latitude", loc.getLatitude())
            cv.put("longitude", loc.getLongitude())
            cv.put("datecoord", Common.getDateTimeAsString14(Date(loc.getTime())))
            cv.put(
                "gpsstate",
                if (gpsenabled) 1 else if (loc.getProvider() == LocationManager.NETWORK_PROVIDER) 2 else 0
            )
            cv.put("gpsaccuracy", loc.getAccuracy())
            cv.put("accept_coord", 0)
            if (getContentResolver().update(
                    MTradeContentProvider.ORDERS_CONTENT_URI,
                    cv,
                    accept_coord_where,
                    accept_coord_selectionArgs
                ) > 0
            ) {
                // и увеличиваем версию редактируемой заявки
                // (не проверяем, действительно ли в данный момент она редактируется, и принимает координаты, т.к. это не важно)
                g.MyDatabase.m_order_editing.versionPDA++
            }

            // Обновим координаты дистрибьюции
            accept_coord_where = E_DISTRIBS_STATE.getAcceptCoordConditionWhere()
            accept_coord_selectionArgs = E_DISTRIBS_STATE.getAcceptCoordConditionSelectionArgs()

            if (getContentResolver().update(
                    MTradeContentProvider.DISTRIBS_CONTENT_URI,
                    cv,
                    accept_coord_where,
                    accept_coord_selectionArgs
                ) > 0
            ) {
                g.MyDatabase.m_distribs_editing.versionPDA++
            }

            // Обновим координаты возвратов
            accept_coord_where = E_REFUND_STATE.getAcceptCoordConditionWhere()
            accept_coord_selectionArgs = E_REFUND_STATE.getAcceptCoordConditionSelectionArgs()

            if (getContentResolver().update(
                    MTradeContentProvider.REFUNDS_CONTENT_URI,
                    cv,
                    accept_coord_where,
                    accept_coord_selectionArgs
                ) > 0
            ) {
                g.MyDatabase.m_refund_editing.versionPDA++
            }

            // Обновим координаты ПКО
            accept_coord_where = E_PAYMENT_STATE.getAcceptCoordConditionWhere()
            accept_coord_selectionArgs = E_PAYMENT_STATE.getAcceptCoordConditionSelectionArgs()

            if (getContentResolver().update(
                    MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                    cv,
                    accept_coord_where,
                    accept_coord_selectionArgs
                ) > 0
            ) {
                g.MyDatabase.m_payment_editing.versionPDA++
            }

            // Обновим координаты визитов
            getContentResolver().update(
                MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                cv,
                "accept_coord=1",
                null
            )
        }

        override fun onProviderDisabled(arg0: String) {
        }

        override fun onProviderEnabled(arg0: String) {
        }

        override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle?) {
        }


    }


    private val locListenerEveryTime: LocationListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
            // возможно, в случае определения координат по wifi, здесь будет false
            val gpsenabled = m_locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val cv = ContentValues()
            cv.put("latitude", loc.getLatitude())
            cv.put("longitude", loc.getLongitude())
            cv.put("datecoord", Common.getDateTimeAsString14(Date(loc.getTime())))
            cv.put(
                "gpsstate",
                if (gpsenabled) 1 else if (loc.getProvider() == LocationManager.NETWORK_PROVIDER) 2 else 0
            )
            cv.put("gpsaccuracy", loc.getAccuracy())
            cv.put("version", 0)
            cv.put(
                "gpstype",
                if (gpsenabled) 1 else if (loc.getProvider() == LocationManager.NETWORK_PROVIDER) 2 else 0
            )
            getContentResolver().insert(MTradeContentProvider.GPS_COORD_CONTENT_URI, cv)
        }

        override fun onProviderDisabled(arg0: String) {
        }

        override fun onProviderEnabled(arg0: String) {
        }

        override fun onStatusChanged(arg0: String, arg1: Int, arg2: Bundle?) {
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val g = MySingleton.getInstance()
        //String name = data.getStringExtra("name");
        //tvName.setText("Your name is " + name);
        when (requestCode and 0xffff) {
            REQ_CODE_VERSION_UPDATE ->                 //Toast.makeText(MainActivity.this,"Received REQ_CODE_VERSION_UPDATE", Toast.LENGTH_SHORT).show();
                if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
                    //Toast.makeText(MainActivity.this, "Update flow failed! Result code: " + resultCode, Toast.LENGTH_SHORT).show();
                    L.d("Update flow failed! Result code: " + resultCode)
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                    unregisterInstallStateUpdListener()
                }
        }
    }

    private fun onCloseActivity() {
        val g = MySingleton.getInstance()
        g.MyDatabase.m_seance_closed = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity()
        } else {
            finish()
        }
        if (BuildConfig.DEBUG) {
            System.exit(0)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                //super.onBackPressed();
                onCloseActivity()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // Alternative variant for API 5 and higher
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            //super.onBackPressed();
            onCloseActivity()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    val exchangeFragment: ExchangeFragment?
        get() {
            val fragmentManager = getSupportFragmentManager()
            val fragment =
                fragmentManager.findFragmentById(R.id.content_frame)
            var exchangeFragment: ExchangeFragment? = null
            if (fragment is ExchangeFragment) {
                exchangeFragment = fragment
            }
            return exchangeFragment
        }


    internal inner class ServiceOperationsTask :
        AsyncTask<String?, String?, String?>() {
        protected var progressDialog: ProgressDialog? = null
        var m_state: Int = 0

        //private static final int SERVICE_TASK_STATE_END = 4;
        override fun onPreExecute() {
            super.onPreExecute()
            m_state = SERVICE_TASK_STATE_BEGIN

            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog!!.getWindow()!!.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            progressDialog!!.setTitle(R.string.title_service_operation)
            progressDialog!!.setMessage(getString(R.string.service_operation_state_service_operation))
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Common.dismissWithExceptionHandling(progressDialog)
            loadDataAfterStart()
        }

        override fun onProgressUpdate(vararg progress: String?) {
            //setProgressPercent(progress[0]);

            if (progress[0]!!.isEmpty()) {
                Snackbar.make(
                    findViewById<View?>(android.R.id.content),
                    progress[1]!!,
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                val mode = progress[0]!!.toInt()
                when (mode) {
                    SERVICE_TASK_STATE_REINDEX -> progressDialog!!.setMessage(getString(R.string.service_operation_state_reindexing))
                    SERVICE_TASK_STATE_RECOVER_ORDERS -> progressDialog!!.setMessage(getString(R.string.service_operation_state_recovering_orders))
                    SERVICE_TASK_STATE_MOVE_OLD_FILES -> progressDialog!!.setMessage(getString(R.string.service_operation_state_moving_old_files))
                }
            }
        }


        override fun doInBackground(vararg params: String?): String {
            //boolean reindexNeeded=false;
            val mode = params[0]!!.toInt()
            publishProgress(SERVICE_TASK_STATE_REINDEX.toString())
            if (mode == SERVICE_TASK_STATE_RECOVER_ORDERS) {
                getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null)
            }
            getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null)
            val g = MySingleton.getInstance()
            TextDatabase.fillNomenclatureHierarchy(
                getContentResolver(),
                getResources(),
                "     0   "
            )
            if (mode == SERVICE_TASK_STATE_RECOVER_ORDERS) {
                publishProgress(SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED.toString())
                recoverOrders(true)
            }
            if (params[1] == "66") {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    publishProgress(SERVICE_TASK_STATE_MOVE_OLD_FILES.toString())
                    try {
                        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                            val oldDir = Environment.getExternalStorageDirectory()
                            moveOldFiles(File(oldDir, "/mtrade/goods"), "goods", "*.jpg")
                            moveOldFiles(File(oldDir, "/mtrade/goods"), "goods", "*.png")
                            moveOldFiles(File(oldDir, "/mtrade/attaches"), "attaches", "*.*")
                        } else {
                            val oldDir = File(
                                Environment.getDataDirectory(),
                                "/data/" + getBaseContext().getPackageName()
                            )
                            moveOldFiles(File(oldDir, "/temp"), "goods", "*.jpg")
                            moveOldFiles(File(oldDir, "/temp"), "goods", "*.png")
                            moveOldFiles(File(oldDir, "/attaches"), "attaches", "*.*")
                        }
                    } catch (e: Error) {
                        // на всякий случай
                    }
                }
            }

            return ""
        }

        protected fun moveOldFiles(oldDir: File, newDir: String?, wildcard: String?) {
            if (oldDir.exists()) {
                // отправляем все файлы из папки на флэшке
                val tempFileNames: Array<File>?
                if (wildcard != null) {
                    val fileFilter: FileFilter = WildcardFileFilter(wildcard)
                    tempFileNames = oldDir.listFiles(fileFilter)
                } else {
                    tempFileNames = oldDir.listFiles()
                }
                if (tempFileNames != null) {
                    val destDir = Common.getMyStorageFileDir(this@MainActivity, newDir)
                    for (tempFile in tempFileNames) {
                        if (!tempFile.isDirectory()) {
                            //InputStream inFile = new FileInputStream(tempFile);
                            val destFile = File(destDir, tempFile.getName())
                            Common.myCopyFile(tempFile, destFile, true)
                        }
                    }
                }
            }
        }


        protected fun recoverOrders(bShowMessages: Boolean): Boolean {
            val g = MySingleton.getInstance()
            var loaded_cnt = 0
            var file_error_cnt = 0
            var data_error_cnt = 0
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                // получаем путь к SD
                val sdPath = File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders"
                )
                //File sdFile = new File(sdPath, g.MyDatabase.m_order_editing.uid.toString()+".txt");
                val listFiles = sdPath.listFiles()
                if (listFiles != null) {
                    for (file in listFiles) {
                        if (file.getName().lowercase(Locale.getDefault()).endsWith((".txt"))) {
                            // TODO в случае если имя файла не совпадает с UID внутри документа, удалять такой файл,
                            // т.к. он всегда будет загружаться и никогда удаляться
                            val str: String
                            val textFile = File(sdPath, file.getName())
                            try {
                                str = getFileContents(file)
                                if (TextDatabase.LoadOrdersFromText(
                                        getContentResolver(),
                                        g.MyDatabase,
                                        str,
                                        2,
                                        true
                                    ) > 0
                                ) loaded_cnt++
                                else data_error_cnt++ // ошибки в данных или состояние заявки в базе такое, что загрузка не требуется
                            } catch (e: IOException) {
                                // Файловая ошибка
                                file_error_cnt++
                            } catch (e: Exception) {
                                Log.e(LOG_TAG, "exception", e)
                                // В случае ошибки в данных удаляем файл
                                textFile.delete()
                                data_error_cnt++
                            }
                        }
                    }
                }
            } else {
                //Toast.makeText(MainActivity.this, "Нет доступа к SD карте!", Toast.LENGTH_SHORT).show();
                if (bShowMessages) publishProgress(
                    "",
                    getResources().getString(R.string.error_no_sd_card_access)
                )
                return false
            }
            var comment_errors = ""
            if (file_error_cnt > 0 || data_error_cnt > 0) {
                //comment_errors=", ошибок чтения файлов: "+file_error_cnt+", ошибок в данных файлов: "+data_error_cnt;
                comment_errors = getString(R.string.error_files_cnt, file_error_cnt, data_error_cnt)
            }
            if (loaded_cnt == 0) {
                if (bShowMessages)  //Toast.makeText(MainActivity.this, getString(R.string.error_no_orders_to_restore)+comment_errors, Toast.LENGTH_SHORT).show();
                    publishProgress(
                        "",
                        getString(R.string.error_no_orders_to_restore) + comment_errors
                    )
            } else {
                //Toast.makeText(MainActivity.this, "Заказы из текстовых файлов восстановлены ("+loaded_cnt+")"+comment_errors, Toast.LENGTH_SHORT).show();
                if (bShowMessages)  //Toast.makeText(MainActivity.this, getString(R.string.message_orders_restored_from_files)+" ("+loaded_cnt+")"+comment_errors, Toast.LENGTH_SHORT).show();
                    publishProgress(
                        "",
                        getString(R.string.message_orders_restored_from_files) + " (" + loaded_cnt + ")" + comment_errors
                    )
            }
            return true
        }

    }

    // Параметр doInBackground, прогресс, результат
    internal inner class ExchangeTask : AsyncTask<Any?, Int?, String?>() {
        protected var progressDialog: ProgressDialog? = null
        var prev_progress_type: Int = 0
        var file_length: Long = 0
        var bStopped: Boolean = false

        //String textToLog;
        var textToLog: ArrayList<String?> = ArrayList<String?>()
        var cancelIsVisible: Boolean = false
        var bIniFileReaded: Boolean = false
        var prevProgress: Int = 0

        //boolean bDontUseExternalStorage = false;//(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q);
        override fun onPreExecute() {
            super.onPreExecute()
            prev_progress_type = 0
            prevProgress = -1
            bStopped = false
            bIniFileReaded = false
            cancelIsVisible = true


            //EditText etLog = (EditText) findViewById(R.id.etTest);
            val exchangeFragment: ExchangeFragment? = exchangeFragment

            if (m_bRefreshPressed) {
                m_exchangeLogText!!.clear()
                m_bRefreshPressed = false
                //etLog.setText(getResources().getString(R.string.message_query_all_warning));
                m_exchangeLogText!!.add(getResources().getString(R.string.message_query_all_warning))
            } else {
                //etLog.setText("");
                m_exchangeLogText!!.clear()
            }

            if (exchangeFragment != null) {
                exchangeFragment.setLogText(null, m_exchangeLogText)
                exchangeFragment.setButtonsExchangeEnabled(false)
            }

            //tvInfo.setText("Begin");
            //Log.d(LOG_TAG, "Begin");
            //progressDialog = ProgressDialog.show(MainActivity.this, "Autenticando", "Contactando o servidor, por favor, aguarde alguns instantes.", true, false);
            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog!!.getWindow()!!.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            // Обмен
            progressDialog!!.setTitle(R.string.title_exchange)
            // Подключение к FTP серверу...
            progressDialog!!.setMessage(getString(R.string.ftp_state_connecting_to_server))
            progressDialog!!.setCancelable(false)
            progressDialog!!.setIcon(getResources().getDrawable(android.R.drawable.ic_popup_sync))
            progressDialog!!.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.cancel),
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        //dialog.dismiss();
                        cancel(true)
                        if (!bStopped) {
                            val exchangeFragment: ExchangeFragment? = exchangeFragment
                            // Процесс останавливается
                            m_exchangeState = getString(R.string.message_exchange_state_stopping)
                            if (exchangeFragment != null) {
                                exchangeFragment.setExchangeState(null, m_exchangeState)
                            }
                        }
                    }
                })
            // Выполняется
            m_exchangeState = getString(R.string.message_exchange_state_executing)
            if (exchangeFragment != null) {
                exchangeFragment.setExchangeState(null, m_exchangeState)
            }
            progressDialog!!.show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Common.dismissWithExceptionHandling(progressDialog)
            bStopped = true
            val exchangeFragment: ExchangeFragment? = exchangeFragment
            if (exchangeFragment != null) {
                // Выполнено
                m_exchangeState = getString(R.string.message_exchange_state_completed)
                exchangeFragment.setExchangeState(null, m_exchangeState)
                exchangeFragment.setButtonsExchangeEnabled(true)
            }
            if (bIniFileReaded) {
                val g = MySingleton.getInstance()
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplication())
                g.readIniFile(null, Globals.appContext, sharedPreferences)
                g.readPreferences(sharedPreferences)
                // Если изменился формат
                if (m_settings_DataFormat != g.m_DataFormat) {
                    // Очистим базу, либо заполним ее тестовыми данными
                    // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
                    if (m_settings_DataFormat == "DM" || g.m_DataFormat == "DM") {
                        resetBase()
                    } else {
                        m_settings_DataFormat = g.m_DataFormat
                        writeSettings()
                    }
                    // 24.03.2017
                    // после смены формата будет выполнен "запросить все"
                    setNegativeVersions()
                }
            }
            // 18.02.2021 пусть всегда проверяет
            // (данные могли измениться в результате обмена)
            checkGpsUpdateEveryTime()
        }


        override fun onCancelled() {
            super.onCancelled()
            Log.e(LOG_TAG, "!!!!!!!!!!Cancel!!!!!!!!!!!!!!!")
            bStopped = true
            val exchangeFragment: ExchangeFragment? = exchangeFragment
            // Прервано
            m_exchangeState = getString(R.string.ftp_state_aborted)
            if (exchangeFragment != null) {
                exchangeFragment.setExchangeState(null, m_exchangeState)
                exchangeFragment.setButtonsExchangeEnabled(true)
            }
        }

        override fun onProgressUpdate(vararg progress: Int?) {
            //setProgressPercent(progress[0]);
            var bNeedHideCancelButton = false
            var bNeedShowCancelButton = false
            if (progress[0] == -2) {
                //EditText etLog = (EditText) findViewById(R.id.etTest);
                //etLog.append(textToLog.get(progress[1] - 1) + "\n");
                val exchangeFragment: ExchangeFragment? = exchangeFragment
                m_exchangeLogText!!.add(textToLog.get(progress[1]!! - 1))
                if (exchangeFragment != null) {
                    exchangeFragment.setLogText(null, m_exchangeLogText)
                }
                return
            }
            if (prev_progress_type != progress[0] && progress[0] != -1) {
                prev_progress_type = progress[0]!!
                when (progress[0]) {
                    FTP_STATE_LOAD_IN_BASE, FTP_STATE_LOAD_HISTORY_IN_BASE -> {
                        // Загрузка в базу...
                        progressDialog!!.setMessage(getString(R.string.ftp_state_load_in_base))
                        bNeedHideCancelButton = true
                    }

                    FTP_STATE_SUCCESS ->                         // Выполнено
                        progressDialog!!.setMessage(getString(R.string.ftp_state_success))

                    FTP_STATE_FINISHED_ERROR -> {
                        // Выполнено с ошибками
                        progressDialog!!.setMessage(getString(R.string.ftp_state_finished_error))
                        bNeedShowCancelButton = true
                    }

                    FTP_STATE_SEND_EOUTF -> {
                        // Отправка файла обмена
                        progressDialog!!.setMessage(getString(R.string.ftp_state_send_eoutf))
                        bNeedShowCancelButton = true
                    }

                    FTP_STATE_RECEIVE_ARCH -> {
                        // Получение файла обмена
                        progressDialog!!.setMessage(getString(R.string.ftp_state_receive_arch))
                        bNeedShowCancelButton = true
                    }

                    FTP_STATE_RECEIVE_IMAGE -> {
                        // Получение изображения номенклатуры
                        progressDialog!!.setMessage(getString(R.string.ftp_state_receive_image))
                        bNeedShowCancelButton = true
                    }
                }
            }
            if (progress.size >= 2 && prevProgress != progress[1]) {
                prevProgress = progress[1]!!
                progressDialog!!.setProgress(progress[1]!!)
            }

            if (!Constants.MY_DEBUG) {
                if (bNeedHideCancelButton && cancelIsVisible || bNeedShowCancelButton && !cancelIsVisible) {
                    var visibility = View.VISIBLE
                    if ((bNeedShowCancelButton.also { cancelIsVisible = it }) == false) {
                        visibility = View.GONE
                    }
                    // Делаем невидимой кнопку
                    progressDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL)
                        .setVisibility(visibility)
                    try {
                        // И разметку, в которой она находится (это больше для красоты)
                        // TODO свое нормальное окно прогресса
                        val v =
                            progressDialog!!.getButton(DialogInterface.BUTTON_NEUTRAL).getParent()
                                .getParent() as View
                        v.setVisibility(visibility)
                    } catch (e: Exception) {
                        //
                    }
                }
            }
        }

        var streamListener: CopyStreamAdapter = object : CopyStreamAdapter() {
            var prev_percent: Int = 0

            override fun bytesTransferred(
                totalBytesTransferred: Long,
                bytesTransferred: Int,
                streamSize: Long
            ) {
                //this method will be called everytime some bytes are transferred

                //int percent = (int)(totalBytesTransferred*100/yourFile.length());
                // update your progress bar with this percentage

                var percent = 0
                if (file_length > 0) {
                    percent = (totalBytesTransferred * 100 / file_length).toInt()
                }
                if (percent > 100) percent = 100
                if (prev_percent != percent) {
                    prev_percent = percent
                    publishProgress(-1, percent)
                }
            }
        }


        //protected Void doInBackground(Void... params)
        override fun doInBackground(vararg params: Any?): String? {
            /*
          try {
            for (int i = 0; i < 5; i++) {
              TimeUnit.SECONDS.sleep(1);
              Log.d(LOG_TAG, "isCancelled: " + isCancelled());
            }
          } catch (InterruptedException e) {
            Log.d(LOG_TAG, "Interrupted");
            e.printStackTrace();
          }
          */

            val g = MySingleton.getInstance()

            val mode = params[0] as Int
            textToLog.clear()

            var sales_uuids: ArrayList<UUID>? = null
            var sales_version = -1

            if (mode == 3 || mode == 4) {
                // Обмен с веб-сервисом
                try {
                    publishProgress(FTP_STATE_SEND_EOUTF, 0)
                    val preferences =
                        PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val web_server_address =
                        preferences.getString("web_server_address", "192.168.0.100")
                    if (mode == 4) {
                        MyWebExchange.doInBackgroundWebService(
                            g.MyDatabase,
                            getApplicationContext(),
                            params[2] as String?,
                            m_settings_agent_id,
                            web_server_address,
                            m_query_period_type,
                            m_query_date_begin,
                            m_query_date_end
                        )
                    } else {
                        MyWebExchange.doInBackgroundWebService(
                            g.MyDatabase,
                            getApplicationContext(),
                            params[2] as String?,
                            m_settings_agent_id,
                            web_server_address,
                            -1,
                            "",
                            ""
                        )
                    }
                    /*
	    		 	Cursor cursor=getContentResolver().query(MTradeContentProvider.SETTINGS_CONTENT_URI, new String[]{"ticket_m", "ticket_w"}, null, null, null);
	       			if (cursor.moveToFirst())
	       			{
	       				m_settings_ticket_m=cursor.getDouble(0);
	       				m_settings_ticket_w=cursor.getDouble(1);
	       			}
	       			cursor.close();
	       			*/
                    textToLog.add(getString(R.string.message_changes_loaded))
                    publishProgress(-2, textToLog.size)
                    Log.v(LOG_TAG, "OK")
                    publishProgress(FTP_STATE_SUCCESS)
                } catch (e: Exception) {
                    //Log.v(LOG_TAG, androidHttpTransport.bodyOut);
                    //Log.v(LOG_TAG, androidHttpTransport.responseDump);
                    //Log.v(LOG_TAG, e.toString());
                    textToLog.add(getString(R.string.message_exchange_error) + " " + e.toString())
                    publishProgress(-2, textToLog.size)
                    //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    publishProgress(FTP_STATE_FINISHED_ERROR)
                }
                return null
            }

            // http://chizztectep.blogspot.ru/2011/07/java-ftp-ftp-client.html
            val ftpClient = FTPClient()
            ftpClient.setRemoteVerificationEnabled(false)

            // 29.11.2022 убрал эти таймауты - это не то, что я думал, похоже))))
            //ftpClient.setControlKeepAliveTimeout(120); // раз в 2 минуты отправлять пустую команду
            //ftpClient.setControlKeepAliveReplyTimeout(120); // чем они отличаются, не знаю

            /*
        	HostnameResolver resolver=new HostnameResolver() {
				@Override
				public String resolve(String hostname) throws UnknownHostException {
					// Тут должен возвращать IP адрес на самом деле
					return hostname;
				}
			};
			*/
            //ftpClient.setPassiveNatWorkaroundStrategy(resolver);
            //ftpClient.setPassiveNatWorkaroundStrategy(null);
            //ftpClient.setCharset(Charset.forName("cp1251"));
            //ftpClient.setAutodetectUTF8(true);

            // 10 секунд
            ftpClient.setConnectTimeout(10000)

            //        } catch (InterruptedException e) {
//            textToLog.add(getString(R.string.ftp_message_interrupted) + " " + e.toString());
//            publishProgress(-2, textToLog.size());
//            Thread.currentThread().interrupt();
//        } catch (Exception e) {
//            // Ошибка
//            textToLog.add(getString(R.string.ftp_message_error) + " " + e.toString());
//            publishProgress(-2, textToLog.size());
//        }
            var connectError: Exception? = null
            var connected = false
            // Если оба адреса пустые, то пользователь увидит это, поэтому инициализируем первым значением
            var ftp_address = g.m_FTP_server_address
            var currentAddress = g.m_FTP_server_address

            try {
                for (i in 0..1) {
                    if (i == 1) {
                        currentAddress = g.m_FTP_server_address_spare
                    }
                    if (!currentAddress.isEmpty()) {
                        val portIndex = currentAddress.indexOf(':')
                        if (portIndex < 0) {
                            ftpClient.connect(currentAddress.trim { it <= ' ' })
                        } else {
                            ftpClient.connect(
                                currentAddress.substring(0, portIndex).trim { it <= ' ' },
                                currentAddress.substring(portIndex + 1).toInt())
                        }
                        ftp_address = currentAddress
                        connected = true
                        break
                    }
                }
            } catch (e: SocketException) {
                // Интересует только первая ошибка, при подключении к основному серверу, или
                // к резервному, если основной не указан
                if (connectError == null) {
                    ftp_address = currentAddress
                    connectError = e
                }
            } catch (e: IOException) {
                if (connectError == null) {
                    ftp_address = currentAddress
                    connectError = e
                }
            }

            try {
                /*
       			// read the initial response (aka "Welcome message")
       			String[] welcomeMessage = ftpClient.getReplyStrings();
       			String welcomeMessageStr=Arrays.toString(welcomeMessage);
    			textToLog.add(welcomeMessageStr);
    			publishProgress(-2, textToLog.size());
    			*/

                if (!connected) {
                    if (connectError != null) {
                        throw RuntimeException(connectError)
                    } else {
                        throw Exception(
                            getString(
                                R.string.message_cannot_connect_to_server,
                                ftp_address
                            )
                        )
                    }
                }

                if (!ftpClient.login(
                        decodeLoginOrPassword(
                            g.m_FTP_server_user,
                            g.m_FTP_server_directory
                        ), decodeLoginOrPassword(g.m_FTP_server_password, g.m_FTP_server_directory)
                    )
                ) throw Exception(getString(R.string.message_cannot_authorize_on_server))
                if (!g.m_FTP_server_directory.isEmpty()) {
                    if (!ftpClient.changeWorkingDirectory(g.m_FTP_server_directory)) throw Exception(
                        getString(R.string.message_cannot_enter_ftp_user_directory)
                    )
                }

                val reply = ftpClient.getReplyCode()
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    throw Exception(getString(R.string.message_ftp_server_refused_connection))
                }
                //EditText et1=(EditText)findViewById(R.id.etTest);
                /*
	        	String dir[] = ftpClient.listNames();
	        	for (String s:dir)
	        	{
	        		Log.d(LOG_TAG, "File: "+s);
	        		//et1.append(s);
	        	}
	        	*/
                val log_file =
                    File(Common.getMyStorageFileDir(this@MainActivity, ""), "mtrade_log.txt")
                /*
                if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    log_file = new File(Environment.getExternalStorageDirectory(), "/mtrade_log.txt");
                } else {
                    log_file = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/mtrade_log.txt");
                }
                 */
                val newLine = "\r\n"

                var logWriter: FileWriter? = null
                logWriter = FileWriter(log_file)

                readVersions()

                class ImageFileState {
                    var file_size: Long?
                    var state: Int // 1 - файл новый, загружаем, 2 - файл новый, но не был загружен

                    // 0 - файл был, не загружаем
                    // 3 - файл есть в номенклатуре, но на диске его нет
                    var image_width: Int
                    var image_height: Int

                    // 11.07.2019
                    // Без пути, но с расширением
                    // должно быть для Map так: [key].jpg или [key].png
                    var image_file_name: String?

                    constructor(file_size: Long?, state: Int, image_file_name: String?) {
                        this.file_size = file_size
                        this.state = state
                        this.image_width = 0
                        this.image_height = 0
                        this.image_file_name = image_file_name
                    }

                    constructor(
                        file_size: Long?,
                        state: Int,
                        image_width: Int,
                        image_height: Int,
                        image_file_name: String?
                    ) {
                        this.file_size = file_size
                        this.state = state
                        this.image_width = image_width
                        this.image_height = image_height
                        this.image_file_name = image_file_name
                    }
                }


                var bImageDataChanged = false
                var bGoodsImagesMode = false
                val mapImageFiles: MutableMap<String?, ImageFileState?> =
                    HashMap<String?, ImageFileState?>()

                when (mode) {
                    0 -> {
                        var bNothingReceived = true
                        // Получаем данные
                        var zipFile: File? = null
                        var fileList: Array<FTPFile>? = null
                        run {
                            var zipFileName: String? = null
                            publishProgress(FTP_STATE_RECEIVE_ARCH, 0)
                            Common.ftpEnterMode(ftpClient, !g.Common.VK)
                            fileList = ftpClient.listFiles() //"*.zip");
                            for (ftpFile in fileList) {
                                if (ftpFile.getName().equals("arch.zip", ignoreCase = true)) {
                                    zipFileName = ftpFile.getName()
                                    file_length = ftpFile.getSize()
                                }
                            }
                            if (zipFileName != null) {
                                val zipFileDir =
                                    Common.getMyStorageFileDir(this@MainActivity, "temp")
                                // удалим предыдущие файлы из каталога temp данных
                                val tempFileNames = zipFileDir.listFiles()
                                if (tempFileNames != null) {
                                    for (tempFile in tempFileNames) {
                                        if (!tempFile.isDirectory()) {
                                            tempFile.delete()
                                        }
                                    }
                                }
                                zipFile = File(zipFileDir, "arch.zip")

                                bNothingReceived = false
                                val outFile: OutputStream = FileOutputStream(zipFile)
                                Common.ftpEnterMode(ftpClient, !g.Common.VK)
                                ftpClient.setCopyStreamListener(streamListener)
                                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
                                //ftpClient.retrieveFile(m_FTP_server_directory+"/arch.zip", outFile);
                                ftpClient.retrieveFile("arch.zip", outFile)
                                outFile.close()

                                ftpClient.deleteFile("arch.zip")
                                //ftpClient.logout();
                            }
                        }
                        if (zipFile != null) {
                            //String zipFile="/mnt/sdcard/qdata/test.zip";
                            val zip = ZipFile(zipFile)
                            try {
                                // В архиве либо ein.txt (старый формат), либо файлы *.xml нового формата
                                // когда там ein.txt есть, то могут быть и *.xml сообщений, которые так выгружались всегда
                                var b_progress_start_load_showed =
                                    false // Это чтобы 2 раза не писать
                                var b_ein_txt_found =
                                    false // Это значит другие файлы просматривать не надо
                                val entries = zip.entries()
                                val totalEntries = zip.size()
                                var currentEntry = 0
                                while (entries.hasMoreElements()) {
                                    currentEntry++
                                    val ze = entries.nextElement() as ZipEntry
                                    // ein.txt
                                    if (!ze.isDirectory() && ze.getName()
                                            .lowercase() == "ein.txt"
                                    ) {
                                        b_ein_txt_found = true
                                        val zin = zip.getInputStream(ze)
                                        val total = ze.getSize().toInt()
                                        val data = ByteArray(total)
                                        try {
                                            var rd = 0
                                            while (total - rd > 0) {
                                                val block = zin.read(data, rd, total - rd)
                                                if (block==-1) {
                                                    break
                                                }
                                                rd += block
                                            }
                                            zin.close()
                                            if (rd == total && rd > 0) {
                                                val strUnzipped =
                                                    String(data, charset("windows-1251"))
                                                b_progress_start_load_showed = true
                                                publishProgress(FTP_STATE_LOAD_IN_BASE, 0)

                                                //String[] lines=strUnzipped.split("##.*##");
                                                val pattern = Pattern.compile("(##.*##)")
                                                val mtch = pattern.matcher(strUnzipped)
                                                var start_prev = -1
                                                var start_this: Int
                                                var prevSection = ""
                                                var bContinue = true
                                                while (bContinue) {
                                                    if (isCancelled()) return null
                                                    var section = ""
                                                    if (mtch.find()) {
                                                        section = mtch.group()
                                                        start_this = mtch.start()
                                                    } else {
                                                        bContinue = false
                                                        start_this = strUnzipped.length
                                                    }
                                                    if (start_prev > 0) {
                                                        publishProgress(
                                                            FTP_STATE_LOAD_IN_BASE,
                                                            ((start_prev / strUnzipped.length.toFloat()) * 100).toInt()
                                                        )
                                                        if (prevSection == "##CLIENTS##") {
                                                            logWriter.append(
                                                                "start clients " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadClients(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end clients " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены клиенты"
                                                            textToLog.add(getString(R.string.message_clients_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##AGREEMENTS30##") {
                                                            logWriter.append(
                                                                "start agreements30 " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadAgreements30(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end agreements30 " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены договоры"
                                                            textToLog.add(getString(R.string.message_agreements30_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##AGREEMENTS##") {
                                                            logWriter.append(
                                                                "start agreements " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadAgreements(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end agreements " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены договоры"
                                                            textToLog.add(getString(R.string.message_agreements_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##NOMENCL##") {
                                                            logWriter.append(
                                                                "start nomenclature " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadNomenclature(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end nomenclature " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.fillNomenclatureHierarchy(
                                                                getContentResolver(),
                                                                getResources(),
                                                                "     0   "
                                                            )
                                                            logWriter.append(
                                                                "end nomenclature hierarchy" + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружена номенклатура"
                                                            textToLog.add(getString(R.string.message_nomenclature_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                            // TODO устанавливать только в случае полной загрузки справочника
                                                            bImageDataChanged = true
                                                        } else if (prevSection == "##OSTAT##") {
                                                            if (!g.Common.DEMO) {
                                                                logWriter.append(
                                                                    "start ostat " + DateFormat.getDateTimeInstance()
                                                                        .format(
                                                                            Date()
                                                                        ) + newLine
                                                                )
                                                                TextDatabase.LoadOstat(
                                                                    getContentResolver(),
                                                                    g.MyDatabase,
                                                                    strUnzipped.substring(
                                                                        start_prev,
                                                                        start_this
                                                                    ),
                                                                    true
                                                                )
                                                                logWriter.append(
                                                                    "end ostat " + DateFormat.getDateTimeInstance()
                                                                        .format(
                                                                            Date()
                                                                        ) + newLine
                                                                )
                                                            }
                                                            // "Загружены остатки"
                                                            textToLog.add(getString(R.string.message_rests_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##DOLG##") {
                                                            logWriter.append(
                                                                "start dolg " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadDolg(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end dolg " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены долги"
                                                            textToLog.add(getString(R.string.message_debts_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##DOLG_EXT##") {
                                                            logWriter.append(
                                                                "start dolg_ext " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadDolgExtended(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end dolg_ext " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены долги по документам"
                                                            textToLog.add(getString(R.string.message_debts_by_docs_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PRICETYPE##") {
                                                            logWriter.append(
                                                                "start pricetype " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadPriceTypes(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end pricetype " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены типы цен"
                                                            textToLog.add(getString(R.string.message_price_types_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##DISCOUNTS##") {
                                                            logWriter.append(
                                                                "start discounts " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadSimpleDiscounts(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end discounts " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены скидки"
                                                            textToLog.add(getString(R.string.message_discounts_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PRICE##") {
                                                            if (!g.Common.DEMO) {
                                                                logWriter.append(
                                                                    "start price " + DateFormat.getDateTimeInstance()
                                                                        .format(
                                                                            Date()
                                                                        ) + newLine
                                                                )
                                                                TextDatabase.LoadPrice(
                                                                    getContentResolver(),
                                                                    g.MyDatabase,
                                                                    strUnzipped.substring(
                                                                        start_prev,
                                                                        start_this
                                                                    ),
                                                                    true
                                                                )
                                                                logWriter.append(
                                                                    "end price " + DateFormat.getDateTimeInstance()
                                                                        .format(
                                                                            Date()
                                                                        ) + newLine
                                                                )
                                                            }
                                                            // "Загружены цены"
                                                            textToLog.add(getString(R.string.message_prices_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PRICE_AGREEMENTS30##") {
                                                            logWriter.append(
                                                                "start price_agreements30 " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadPricesAgreements30(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end price_agreements30 " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены цены по соглашениям"
                                                            textToLog.add(getString(R.string.message_prices_by_agreements30_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##STOCKS##") {
                                                            logWriter.append(
                                                                "start stocks " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadStocks(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end stocks " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены склады"
                                                            textToLog.add(getString(R.string.message_contracts_for_distribs_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##DISTR_CONTR##") {
                                                            logWriter.append(
                                                                "start stocks " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadDistribsContracts(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end stocks " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены контракты для дистрибьюции"
                                                            textToLog.add(getString(R.string.message_contracts_for_distribs_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##AGENTS##") {
                                                            logWriter.append(
                                                                "start agents " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadAgents(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end agents " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены агенты"
                                                            textToLog.add(getString(R.string.message_agents_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##CURATORS##") {
                                                            logWriter.append(
                                                                "start curators " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadCurators(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end curators " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены кураторы"
                                                            textToLog.add(getString(R.string.message_curators_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##D_POINTS##") {
                                                            logWriter.append(
                                                                "start d.points " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadDistrPoints(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end d.points " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены торговые точки"
                                                            textToLog.add(getString(R.string.message_distr_points_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##ORGANIZATIONS##") {
                                                            logWriter.append(
                                                                "start organizations " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadOrganizations(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end organizations " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены организации"
                                                            textToLog.add(getString(R.string.message_organizations_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PRICE_CLIENT##") {
                                                            logWriter.append(
                                                                "start clients price " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadClientsPrice(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end clients price" + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружен прайс клиентов"
                                                            textToLog.add(getString(R.string.message_clients_prices_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PRICE_CURATOR##") {
                                                            logWriter.append(
                                                                "start curators price " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadCuratorsPrice(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end curators price" + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружен прайс кураторов"
                                                            textToLog.add(getString(R.string.message_curators_prices_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##ROUTES##") {
                                                            logWriter.append(
                                                                "start routes " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadRoutes(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end routes " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены маршруты"
                                                            textToLog.add(getString(R.string.message_routes_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##ROUTES_DATES##") {
                                                            logWriter.append(
                                                                "start routes dates " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadRoutesDates(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end routes dates " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены даты маршрутов"
                                                            textToLog.add(getString(R.string.message_routes_dates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##MESSAGES##") {
                                                            logWriter.append(
                                                                "start messages " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadMessages(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end messages " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены сообщения"
                                                            textToLog.add(getString(R.string.message_messages_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##SALES##") {
                                                            logWriter.append(
                                                                "start sales history " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            sales_uuids = ArrayList<UUID>()
                                                            sales_version =
                                                                TextDatabase.LoadSalesHistoryHeader(
                                                                    getContentResolver(),
                                                                    g.MyDatabase,
                                                                    strUnzipped.substring(
                                                                        start_prev,
                                                                        start_this
                                                                    ),
                                                                    sales_uuids
                                                                )
                                                            // удалим другие версии
                                                            getContentResolver().delete(
                                                                MTradeContentProvider.VERSIONS_SALES_CONTENT_URI,
                                                                "ver<>?",
                                                                arrayOf<String?>(
                                                                    sales_version.toString()
                                                                )
                                                            )
                                                            //
                                                            getContentResolver().delete(
                                                                MTradeContentProvider.SALES_LOADED_CONTENT_URI,
                                                                "ver<>?",
                                                                arrayOf<String?>(
                                                                    sales_version.toString()
                                                                )
                                                            )
                                                            //
                                                            logWriter.append(
                                                                "end sales history " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружен заголовок истории продаж"
                                                            textToLog.add(getString(R.string.message_sales_history_header_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##ORDERS_M##") {
                                                            logWriter.append(
                                                                "start orders update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val uuids = ArrayList<UUID>()

                                                            TextDatabase.LoadOrdersUpdate(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                uuids
                                                            )
                                                            // Это устаревшее (файлов в папке orders сейчас нет), но оставлю
                                                            //if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                                            // получаем путь к SD
                                                            //File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                                                            val sdPath = Common.getMyStorageFileDir(
                                                                this@MainActivity,
                                                                "orders"
                                                            )
                                                            // Удаляем файлы всех загруженных заявок
                                                            for (uuid in uuids) {
                                                                val sdFile = File(
                                                                    sdPath,
                                                                    uuid.toString() + ".txt"
                                                                )
                                                                sdFile.delete()
                                                            }
                                                            //}
                                                            logWriter.append(
                                                                "end orders update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления заказов"
                                                            textToLog.add(getString(R.string.message_orders_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##DISTRIB_M##") {
                                                            logWriter.append(
                                                                "start distribs update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val uuids = ArrayList<UUID?>()
                                                            TextDatabase.LoadDistribsUpdate(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                uuids
                                                            )
                                                            logWriter.append(
                                                                "end distribs update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления дистрибьюции"
                                                            textToLog.add(getString(R.string.message_distribs_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##SUM_SHIPPING_NOTIFICATIONS_M##") {
                                                            logWriter.append(
                                                                "start shipping notification update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadSumShippingNotification(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                )
                                                            )
                                                            logWriter.append(
                                                                "end shipping notification update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления сумм отгрузок по заказам"
                                                            textToLog.add(getString(R.string.message_shipping_notifications_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##PAYMENTS_M##") {
                                                            logWriter.append(
                                                                "start payments update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val uuidsPayments = ArrayList<UUID?>()
                                                            TextDatabase.LoadPaymentsUpdate(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                uuidsPayments
                                                            )
                                                            logWriter.append(
                                                                "end payments update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления платежей"
                                                            textToLog.add(getString(R.string.message_payments_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##REFUNDS_M##") {
                                                            logWriter.append(
                                                                "start refunds update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val uuids = ArrayList<UUID?>()

                                                            TextDatabase.LoadRefundsUpdate(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                uuids
                                                            )
                                                            /*
			    	                        			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			    	                        			{
			    	                        			    // получаем путь к SD
			    	                        			    File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/refunds");
			    	                        			    // Удаляем файлы всех загруженных заявок
			    	                        			    for (UUID uuid:uuids)
			    	                        			    {
			    	                        			    	File sdFile = new File(sdPath, uuid.toString()+".txt");
			    	                        			    	sdFile.delete();
			    	                        			    }
			    	                        			}
			    	                        			*/
                                                            logWriter.append(
                                                                "end refunds update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления заказов на возврат"
                                                            textToLog.add(getString(R.string.message_refunds_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##VISITS_M##") {
                                                            logWriter.append(
                                                                "start visits update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val uuids = ArrayList<UUID?>()
                                                            TextDatabase.LoadVisitsUpdate(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                uuids
                                                            )
                                                            logWriter.append(
                                                                "end visits update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления заказов на возврат"
                                                            textToLog.add(getString(R.string.message_vists_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##VICARIOUS_POWER##") {
                                                            logWriter.append(
                                                                "start vicarious power update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadVicariousPower(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end vicarious power update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_vicarious_powers_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##GPS##") {
                                                            logWriter.append(
                                                                "start gps update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            val newGpsUpdateInterval =
                                                                TextDatabase.LoadGpsUpdate(
                                                                    getContentResolver(),
                                                                    g.MyDatabase,
                                                                    strUnzipped.substring(
                                                                        start_prev,
                                                                        start_this
                                                                    )
                                                                )
                                                            if (newGpsUpdateInterval != m_settings_gps_interval) {
                                                                m_settings_gps_interval =
                                                                    newGpsUpdateInterval
                                                                writeSettings()
                                                                if (m_settings_gps_interval < 0) {
                                                                    g.Common.HAVE_GPS_SETTINGS =
                                                                        true
                                                                }
                                                                //checkGpsUpdateEveryTime();
                                                            }
                                                            logWriter.append(
                                                                "end gps update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления gps"
                                                            textToLog.add(getString(R.string.message_gps_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##EQUIPMENT##") {
                                                            logWriter.append(
                                                                "start equipment update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadEquipment(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end equipment update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_equipment_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else if (prevSection == "##OSTAT_TE##") {
                                                            logWriter.append(
                                                                "start equipment rests update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            TextDatabase.LoadEquipmentRests(
                                                                getContentResolver(),
                                                                g.MyDatabase,
                                                                strUnzipped.substring(
                                                                    start_prev,
                                                                    start_this
                                                                ),
                                                                true
                                                            )
                                                            logWriter.append(
                                                                "end equipment rests update " + DateFormat.getDateTimeInstance()
                                                                    .format(
                                                                        Date()
                                                                    ) + newLine
                                                            )
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_equipment_rests_updates_loaded))
                                                            publishProgress(-2, textToLog.size)
                                                        } else {
                                                            //Toast.makeText(MainActivity.this, "ERR: "+prevSection, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    start_prev = start_this + section.length
                                                    prevSection = section
                                                }

                                                //Toast.makeText(MainActivity.this, "" + lines.length, Toast.LENGTH_SHORT).show();
                                                // 23.01.2021 перенесено ниже, там проверяются флаги
                                                //publishProgress(FTP_STATE_LOAD_IN_BASE, 100);
                                                //publishProgress(FTP_STATE_SUCCESS);
                                            }
                                        } finally {
                                            //fout.flush();
                                            //fout.close();
                                        }
                                    }
                                    // mtrade.ini
                                    if (!ze.isDirectory() && ze.getName()
                                            .lowercase() == "mtrade.ini"
                                    ) {
                                        /*
			    	        		InputStream is = zip.getInputStream(ze);
			    	        		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			    	        		String str;
			    	        		while ((str = reader.readLine()) != null) {
			    	                    //buf.append(str + "\n" );
			    	                }
			    	        		is.close();
			    	        		*/
                                        val `is` = zip.getInputStream(ze)

                                        /*
                                        if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                            // получаем путь к SD
                                            File sdPath = new File(Environment.getExternalStorageDirectory(), "/mtrade");
                                            if (!sdPath.exists()) {
                                                sdPath.mkdirs();
                                            }
                                            OutputStream os = new FileOutputStream(new File(sdPath, "mtrade.ini"));
                                            int read = 0;
                                            byte[] bytes = new byte[1024];

                                            while ((read = is.read(bytes)) != -1) {
                                                os.write(bytes, 0, read);
                                            }
                                            os.close();
                                        }
                                         */
                                        val os: OutputStream = FileOutputStream(
                                            File(
                                                Common.getMyStorageFileDir(
                                                    this@MainActivity,
                                                    ""
                                                ), "mtrade.ini"
                                            )
                                        )
                                        var read = 0
                                        val bytes = ByteArray(1024)

                                        while ((`is`.read(bytes).also { read = it }) != -1) {
                                            os.write(bytes, 0, read)
                                        }
                                        os.close()

                                        `is`.close()
                                        bIniFileReaded = true
                                    }
                                    // Сообщения и т.п. *.xml
                                    if (!ze.isDirectory() && ze.getName().length > 4 && ze.getName()
                                            .lowercase()
                                            .substring(ze.getName().length - 4) == ".xml"
                                    ) {
                                        if (!b_progress_start_load_showed) {
                                            publishProgress(FTP_STATE_LOAD_IN_BASE, 0)
                                            b_progress_start_load_showed = true
                                        }

                                        var `is` = zip.getInputStream(ze)
                                        // Убедимся, что перед '<' нет лишних символов
                                        var total = ze.getSize().toInt()
                                        var offset = 0
                                        if (total > 10) {
                                            total = 10
                                        }
                                        val data = ByteArray(total)
                                        val read = `is`.read(data, 0, total)
                                        var i: Int
                                        i = 0
                                        while (i < read) {
                                            if (data[i] == '<'.code.toByte()) {
                                                offset = i
                                                break
                                            }
                                            i++
                                        }
                                        `is`.close()
                                        `is` = zip.getInputStream(ze)
                                        if (offset != 0) `is`.skip(offset.toLong())
                                        val timeBefore = Date().getTime()
                                        val resultLoadXML = TextDatabase.LoadXML(
                                            getApplicationContext(),
                                            g.MyDatabase,
                                            `is`,
                                            true
                                        )
                                        if (resultLoadXML.bSuccess) {
                                            val timeDiff = Date().getTime() - timeBefore
                                            textToLog.add(
                                                resultLoadXML.ResultMessage + ", " + Common.DoubleToStringFormat(
                                                    timeDiff / 1000.0,
                                                    "%.2f"
                                                ) + " sec."
                                            )
                                            publishProgress(-2, textToLog.size)
                                            // Это не новая загрузка, это просто загрузка сообщений в формате *.xml, что было всегда
                                            // и здесь никакого прогресса не выводится
                                            if (!b_ein_txt_found) {
                                                publishProgress(
                                                    FTP_STATE_LOAD_IN_BASE,
                                                    ((currentEntry / totalEntries.toFloat()) * 100).toInt()
                                                )
                                            }
                                            when (resultLoadXML.xmlMode) {
                                                // TODO(старый комментарий) устанавливать только в случае полной загрузки справочника
                                                XMLMode.E_MODE_NOMENCLATURE ->
                                                    bImageDataChanged = true

                                                XMLMode.E_MODE_GPS_M -> if (resultLoadXML.nResult != m_settings_gps_interval) {
                                                    m_settings_gps_interval = resultLoadXML.nResult
                                                    writeSettings()
                                                    if (m_settings_gps_interval < 0) {
                                                        g.Common.HAVE_GPS_SETTINGS = true
                                                    } else  // вот, честно говоря, не помню, почему
                                                    // в других местах этот флаг не сбрасывается
                                                    // да и может быть оно вообще нигде не используется
                                                        g.Common.HAVE_GPS_SETTINGS = false
                                                }
                                                else -> {}
                                            }

                                            if (resultLoadXML.uuids != null) {
                                                when (resultLoadXML.xmlMode) {
                                                    XMLMode.E_MODE_ORDERS_M -> {}
                                                    XMLMode.E_MODE_GPS_M -> {}
                                                    // 21.09.2022
                                                    XMLMode.E_MODE_SALES_HISTORY_HEADERS ->
                                                        sales_uuids = resultLoadXML.uuids
                                                    else -> {}
                                                }
                                            }
                                        }
                                        //
                                        `is`.close()
                                    }
                                }

                                if (b_progress_start_load_showed) {
                                    publishProgress(FTP_STATE_LOAD_IN_BASE, 100)
                                    publishProgress(FTP_STATE_SUCCESS)
                                }


                                var bReceivedSales = false
                                if (sales_uuids != null) {
                                    bReceivedSales = true
                                    var historyIdx = 0
                                    for (uid in sales_uuids) {
                                        var bReceivedByUid = false
                                        // Принимаем файлы продажи, если они не загружены
                                        val verCursor = getContentResolver().query(
                                            MTradeContentProvider.VERSIONS_SALES_CONTENT_URI,
                                            arrayOf<String>("ver"),
                                            "param=?",
                                            arrayOf<String?>(uid.toString()),
                                            null
                                        )
                                        if (verCursor!!.moveToFirst()) {
                                            // Данные уже были загружены ранее
                                            bReceivedByUid = true
                                        } else {
                                            val historyZipFile: File?
                                            val zipFileDir = Common.getMyStorageFileDir(
                                                this@MainActivity,
                                                "temp"
                                            )
                                            historyZipFile =
                                                File(zipFileDir, uid.toString() + ".zip")

                                            /*
                                            if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                                File zipFileDir = new File(Environment.getExternalStorageDirectory(), "/mtrade/temp");
                                                if (!zipFileDir.exists()) {
                                                    zipFileDir.mkdirs();
                                                }
                                                historyZipFile = new File(zipFileDir, uid.toString() + ".zip");
                                            } else {
                                                File zipFileDir = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/temp");
                                                if (!zipFileDir.exists()) {
                                                    zipFileDir.mkdirs();
                                                }
                                                historyZipFile = new File(zipFileDir, uid.toString() + ".zip");
                                            }
                                             */
                                            // Найдем файл в списке
                                            file_length = 0
                                            // большие-маленькие различаются
                                            var history_file_name = uid.toString() + ".zip"
                                            for (ftpFile in fileList!!) {
                                                if (ftpFile.getName().equals(
                                                        uid.toString() + ".zip",
                                                        ignoreCase = true
                                                    )
                                                ) {
                                                    file_length = ftpFile.getSize()
                                                    history_file_name = ftpFile.getName()
                                                    break
                                                }
                                            }
                                            publishProgress(FTP_STATE_RECEIVE_ARCH, 0)
                                            val outFile: OutputStream =
                                                FileOutputStream(historyZipFile)
                                            Common.ftpEnterMode(ftpClient, !g.Common.VK)
                                            ftpClient.setCopyStreamListener(streamListener)
                                            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
                                            if (ftpClient.retrieveFile(
                                                    history_file_name,
                                                    outFile
                                                )
                                            ) {
                                                outFile.flush()
                                                textToLog.add("Загружен файл истории продаж " + uid.toString())
                                                publishProgress(-2, textToLog.size)
                                            } else {
                                                textToLog.add("Не удалось загрузить файл истории продаж " + uid.toString())
                                                publishProgress(-2, textToLog.size)
                                                file_length = 0
                                            }
                                            outFile.close()

                                            if (file_length > 0) {
                                                publishProgress(FTP_STATE_LOAD_HISTORY_IN_BASE, 0)

                                                val historyZip = ZipFile(historyZipFile)
                                                try {
                                                    val historyEntries = historyZip.entries()
                                                    var bHistoryZipReaded = false
                                                    while (historyEntries.hasMoreElements()) {
                                                        val ze =
                                                            historyEntries.nextElement() as ZipEntry
                                                        // sales.txt
                                                        if (!ze.isDirectory() && ze.getName()
                                                                .lowercase() == "sales.txt"
                                                        ) {
                                                            val zin = historyZip.getInputStream(ze)
                                                            val total = ze.getSize().toInt()
                                                            val data = ByteArray(total)
                                                            try {
                                                                var rd = 0
                                                                while (total - rd > 0) {
                                                                    val block = zin.read(
                                                                        data,
                                                                        rd,
                                                                        total - rd
                                                                    )
                                                                    if (block==-1) {
                                                                        break
                                                                    }
                                                                    rd += block
                                                                }
                                                                zin.close()
                                                                if (rd == total && rd > 0) {
                                                                    val strUnzipped = String(
                                                                        data,
                                                                        charset("windows-1251")
                                                                    )
                                                                    TextDatabase.LoadSalesHistory(
                                                                        getContentResolver(),
                                                                        g.MyDatabase,
                                                                        strUnzipped,
                                                                        uid,
                                                                        sales_version
                                                                    )
                                                                    bReceivedByUid = true
                                                                    // Установим версию фрагмента
                                                                    val cv = ContentValues()
                                                                    cv.put("param", uid.toString())
                                                                    cv.put("ver", sales_version)
                                                                    getContentResolver().insert(
                                                                        MTradeContentProvider.VERSIONS_SALES_CONTENT_URI,
                                                                        cv
                                                                    )
                                                                    bHistoryZipReaded = true
                                                                }
                                                            } catch (e: IOException) {
                                                                zin.close()
                                                            }
                                                        }
                                                    }
                                                    if (bHistoryZipReaded) {
                                                        //ftpClient.deleteFile(uid.toString()+".zip");
                                                        ftpClient.deleteFile(history_file_name)
                                                    }
                                                } catch (e: IOException) {
                                                    historyZip.close()
                                                }
                                            }
                                            bReceivedSales = bReceivedSales and bReceivedByUid
                                            publishProgress(
                                                FTP_STATE_LOAD_HISTORY_IN_BASE,
                                                historyIdx * 100 / sales_uuids.size
                                            )
                                            historyIdx++
                                        }
                                        verCursor.close()
                                        publishProgress(FTP_STATE_LOAD_HISTORY_IN_BASE, 100)
                                    }
                                    if (bReceivedSales) {
                                        g.MyDatabase.m_sales_loaded_version = sales_version
                                        val cv = ContentValues()
                                        cv.put("param", "SALES_LOADED")
                                        cv.put("ver", g.MyDatabase.m_sales_loaded_version)
                                        val newUri = getContentResolver().insert(
                                            MTradeContentProvider.VERSIONS_CONTENT_URI,
                                            cv
                                        )
                                        // и пересчитываем продажи
                                        getContentResolver().insert(
                                            MTradeContentProvider.CREATE_SALES_L_URI,
                                            null
                                        )
                                    }
                                }
                                publishProgress(FTP_STATE_SUCCESS)
                                if (bReceivedSales) {
                                }
                                /*
		    				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		    				{
		    					File zipFileDir=new File(Environment.getExternalStorageDirectory(), "/mtrade");
		    		            if (!zipFileDir.exists()) {
		    		            	zipFileDir.mkdirs();
		    		            }
		    		            zipFile=new File(zipFileDir, "arch.zip");
		    				} else
		    				{
		    					zipFile=new File(Environment.getDataDirectory(), "/data/"+ getBaseContext().getPackageName()+"/arch.zip");
		    				}
	    	        		OutputStream outFile=new FileOutputStream(zipFile);
	    	        		Common.ftpEnterMove(ftpClient, !g.Common.VK);
	    	            	ftpClient.setCopyStreamListener(streamListener);
	    	        		ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	    	        		//ftpClient.retrieveFile(m_FTP_server_directory+"/arch.zip", outFile);
	    	        		ftpClient.retrieveFile("arch.zip", outFile);
	    	        		outFile.close();
			    	    	*/
                                try {
                                    if (ftpClient.isConnected()) {
                                        ftpClient.logout()
                                        ftpClient.disconnect()
                                    }
                                } catch (e: IOException) {
                                    Log.e(LOG_TAG, "Ошибка при отключении от сервера", e)
                                    e.printStackTrace()
                                }
                                // Увеличим счетчик у удаленных сообщений в случае успешного получения архива
                                // (это происходит при записи isMark=1 - isMarkCnt увеличивается на 1)
                                val cv = ContentValues()
                                cv.put("isMark", 1)
                                getContentResolver().update(
                                    MTradeContentProvider.MESSAGES_CONTENT_URI,
                                    cv,
                                    "isMark=1",
                                    arrayOf<String?>()
                                )
                                getContentResolver().delete(
                                    MTradeContentProvider.MESSAGES_CONTENT_URI,
                                    "isMarkCnt>10",
                                    arrayOf<String?>()
                                )
                                // этот вызов не может быть в потоке, поэтому перенесен сюда
                                // 18.02.2021 вообще перенесено на после обмена
                                //checkGpsUpdateEveryTime();
                            } catch (e: Exception) {
                                Log.e(LOG_TAG, "Ошибка при работе с архивом", e)
                                e.printStackTrace()
                                // Ошибка при работе с архивом
                                textToLog.add(getString(R.string.message_archive_error) + " " + e.toString())
                                publishProgress(-2, textToLog.size)
                                //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                publishProgress(FTP_STATE_FINISHED_ERROR)
                            } finally {
                                zip.close()
                            }
                        } else  //zipFileName==null
                        {
                            ftpClient.logout()
                        }
                        //logWriter.flush();
                        //logWriter.close();
                        if (bNothingReceived) {
                            // Ничего не получено, но и ошибок тоже нет
                            // Успешное подключение, данных к получению нет
                            textToLog.add(getString(R.string.ftp_message_success_no_data))
                            publishProgress(-2, textToLog.size)
                        }
                    }

                    1 -> {
                        // Это отличается от того, что пользователь настраивает
                        // значения через getDefaultSharedPreferences
                        val pref = getSharedPreferences("MTradePreferences", 0)

                        //SharedPreferences.Editor pref_editor = pref.edit();

                        // отправляем данные
                        publishProgress(FTP_STATE_SEND_EOUTF, 0)
                        Common.ftpEnterMode(ftpClient, !g.Common.VK)
                        // удаляем предыдущий файл, если он был
                        ftpClient.deleteFile("arch.zip")
                        val zipFile: File
                        val zipFileDir = Common.getMyStorageFileDir(this@MainActivity, "temp")
                        zipFile = File(zipFileDir, "eoutf.zip")

                        /*
                        if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            File zipFileDir = new File(Environment.getExternalStorageDirectory(), "/mtrade/temp");
                            if (!zipFileDir.exists()) {
                                zipFileDir.mkdirs();
                            }
                            zipFile = new File(zipFileDir, "eoutf.zip");
                        } else {
                            File zipFileDir = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/temp");
                            if (!zipFileDir.exists()) {
                                zipFileDir.mkdirs();
                            }
                            zipFile = new File(zipFileDir, "eoutf.zip");
                        }
                        //zipFile.delete();
                         */
                        val zipStream =
                            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile)))
                        zipStream.setMethod(ZipOutputStream.DEFLATED)
                        zipStream.setLevel(Deflater.DEFAULT_COMPRESSION)
                        try {
                            val uuids: ArrayList<UUID> = ArrayList()
                            val idsVisits: ArrayList<Long> = ArrayList()
                            val uuidsPayments: ArrayList<UUID> = ArrayList()
                            val uuidsRefunds: ArrayList<UUID> = ArrayList()
                            val uuidsDistribs: ArrayList<UUID> = ArrayList()

                            // увеличиваем счетчик отправленных файлов
                            g.MyDatabase.m_sent_count++
                            val cv_ver = ContentValues()
                            cv_ver.clear()
                            cv_ver.put("param", "SENT_COUNT")
                            cv_ver.put("ver", g.MyDatabase.m_sent_count)
                            getContentResolver().insert(
                                MTradeContentProvider.VERSIONS_CONTENT_URI,
                                cv_ver
                            )

                            //

                            // Те, кому пока еще нужен текстовый файл выгрузки
                            if (!g.Common.PRODLIDER && !g.Common.PRAIT && !g.Common.FACTORY) {
                                // Текстовый файл
                                val ze = ZipEntry("eout.txt")
                                zipStream.putNextEntry(ze)
                                val bytes = ByteArrayOutputStream()
                                val bw = BufferedWriter(OutputStreamWriter(bytes, "cp1251"))
                                // Версии
                                bytes.reset()
                                TextDatabase.SaveVersions(bw, g.MyDatabase)
                                bw.flush()
                                zipStream.write(bytes.toByteArray())
                                // Сообщения
                                bytes.reset()
                                TextDatabase.SaveSendMessages(
                                    bw,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    m_bExcludeImageMessages
                                )
                                bw.flush()
                                zipStream.write(bytes.toByteArray())
                                // Заказы отправляемые
                                bytes.reset()
                                TextDatabase.SaveSendOrders(
                                    bw,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    uuids
                                )
                                bw.flush()
                                zipStream.write(bytes.toByteArray())
                                // Подтверждение сумм реализаций на основании заказов
                                if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size > 0) {
                                    bytes.reset()
                                    bw.write("##SUM_SHIPPING_NOTIFICATIONS##\r\n")
                                    for (it in g.MyDatabase.m_acks_shipping_sums.entries) {
                                        bw.write(
                                            String.format(
                                                "%s#%.2f\r\n",
                                                it.key.toString(),
                                                it.value
                                            )
                                        )
                                    }
                                    bw.flush()
                                    zipStream.write(bytes.toByteArray())
                                    // Очистка перенесена ниже
                                    //g.MyDatabase.m_acks_shipping_sums.clear();
                                }
                                // Платежи отправляемые
                                if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                                    bytes.reset()
                                    TextDatabase.SaveSendPayments(
                                        bw,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        uuidsPayments
                                    )
                                    bw.flush()
                                    zipStream.write(bytes.toByteArray())
                                }
                                // Возвраты отправляемые
                                bytes.reset()
                                TextDatabase.SaveSendRefunds(
                                    bw,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    uuidsRefunds
                                )
                                bw.flush()
                                zipStream.write(bytes.toByteArray())
                                // Визиты отправляемые
                                bytes.reset()
                                TextDatabase.SaveSendVisits(
                                    bw,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    idsVisits
                                )
                                bw.flush()
                                zipStream.write(bytes.toByteArray())
                                // Дистрибьюции отправляемые
                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    bytes.reset()
                                    TextDatabase.SaveSendDistribs(
                                        bw,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        uuidsDistribs
                                    )
                                    bw.flush()
                                    zipStream.write(bytes.toByteArray())
                                }
                                // Раздел "Разное"
                                if (g.Common.PRODLIDER) {
                                    bytes.reset()
                                    bw.write("##MISC##\r\n")
                                    bw.write(
                                        String.format(
                                            "DeviceName=%s\r\n",
                                            Common.getDeviceName()
                                        )
                                    )
                                    val fcm_instanceId = pref.getString("fcm_instanceId", "")
                                    if (fcm_instanceId != null && !fcm_instanceId.isEmpty()) {
                                        bw.write(
                                            String.format(
                                                "FCM_InstanceId=%s\r\n",
                                                fcm_instanceId
                                            )
                                        )
                                    }
                                    bw.flush()
                                    zipStream.write(bytes.toByteArray())
                                }

                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    // GPS координаты
                                    bytes.reset()
                                    bw.write("##GPS##\r\n")
                                    TextDatabase.SaveSendGps(
                                        bw,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        g.MyDatabase.m_sent_count
                                    )
                                    bw.flush()
                                    zipStream.write(bytes.toByteArray())
                                }
                                // Конец файла
                                bytes.reset()
                                bw.write("##EOF##")
                                bw.flush()
                                zipStream.write(bytes.toByteArray())

                                zipStream.closeEntry()
                                zipStream.flush()
                            }

                            if (g.Common.PRODLIDER || g.Common.VK || g.Common.PRAIT || g.Common.FACTORY) {
                                // XML файл
                                var ze = ZipEntry("versions.xml")
                                zipStream.putNextEntry(ze)
                                TextDatabase.SaveVersionsXML(zipStream, g.MyDatabase)
                                zipStream.closeEntry()
                                zipStream.flush()

                                ze = ZipEntry("send_messages.xml")
                                zipStream.putNextEntry(ze)
                                TextDatabase.SaveSendMessagesXML(
                                    zipStream,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    m_bExcludeImageMessages
                                )
                                zipStream.closeEntry()
                                zipStream.flush()

                                ze = ZipEntry("orders.xml")
                                zipStream.putNextEntry(ze)
                                TextDatabase.SaveSendOrdersXML(
                                    zipStream,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    uuids
                                )
                                zipStream.closeEntry()
                                zipStream.flush()

                                // Подтверждение сумм реализаций на основании заказов
                                // TODO не очищать в коде выше m_acks_shipping_sums
                                if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size > 0) {
                                    ze = ZipEntry("sum_shipping_notifications.xml")
                                    zipStream.putNextEntry(ze)
                                    TextDatabase.SaveSendSumShippingNotificationsXML(
                                        zipStream,
                                        g.MyDatabase.m_acks_shipping_sums
                                    )
                                    zipStream.closeEntry()
                                    zipStream.flush()
                                }
                                // Платежи отправляемые
                                if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                                    ze = ZipEntry("payments.xml")
                                    zipStream.putNextEntry(ze)
                                    TextDatabase.SaveSendPaymentsXML(
                                        zipStream,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        uuidsPayments
                                    )
                                    zipStream.closeEntry()
                                    zipStream.flush()
                                }
                                // Возвраты отправляемые
                                ze = ZipEntry("refunds.xml")
                                zipStream.putNextEntry(ze)
                                TextDatabase.SaveSendRefundsXML(
                                    zipStream,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    uuidsRefunds
                                )
                                zipStream.closeEntry()
                                zipStream.flush()
                                // Визиты отправляемые
                                ze = ZipEntry("visits.xml")
                                zipStream.putNextEntry(ze)
                                TextDatabase.SaveSendVisitsXML(
                                    zipStream,
                                    getContentResolver(),
                                    g.MyDatabase,
                                    idsVisits
                                )
                                zipStream.closeEntry()
                                zipStream.flush()
                                // Дистрибьюции отправляемые
                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    ze = ZipEntry("distribs.xml")
                                    zipStream.putNextEntry(ze)
                                    TextDatabase.SaveSendDistribsXML(
                                        zipStream,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        uuidsDistribs
                                    )
                                    zipStream.closeEntry()
                                    zipStream.flush()
                                }

                                // Раздел "Разное"
                                ze = ZipEntry("misc.xml")
                                zipStream.putNextEntry(ze)
                                val fcm_instanceId = pref.getString("fcm_instanceId", "")
                                TextDatabase.SaveSendMiscXML(zipStream, fcm_instanceId)
                                zipStream.closeEntry()
                                zipStream.flush()

                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    // GPS координаты
                                    ze = ZipEntry("gps.xml")
                                    zipStream.putNextEntry(ze)
                                    TextDatabase.SaveSendGpsXML(
                                        zipStream,
                                        getContentResolver(),
                                        g.MyDatabase,
                                        g.MyDatabase.m_sent_count
                                    )
                                    zipStream.closeEntry()
                                    zipStream.flush()
                                }

                                // Очистка полей (т.к. в двух местах выгрузка происходит, то очистка сделана отдельно)
                                // Подтверждение сумм реализаций на основании заказов
                                if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size > 0) {
                                    g.MyDatabase.m_acks_shipping_sums.clear()
                                }
                                g.MyDatabase.m_empty_orders.clear()
                                g.MyDatabase.m_empty_refunds.clear()
                                g.MyDatabase.m_empty_payments.clear()
                                g.MyDatabase.m_empty_visits.clear()
                                g.MyDatabase.m_empty_distribs.clear()
                            }

                            zipStream.close()

                            ZipRepair.fixInvalidZipFile(zipFile)

                            var bSuccessSend = true

                            if (!m_bExcludeImageMessages) {
                                // сначала отправляем вложенные файлы и архив
                                // вложенные файлы

                                val attachesFileDir =
                                    Common.getMyStorageFileDir(this@MainActivity, "attaches")
                                // отправляем все файлы из папки в памяти устройства
                                val tempFileNames = attachesFileDir.listFiles()
                                if (tempFileNames != null) {
                                    for (tempFile in tempFileNames) {
                                        if (!tempFile.isDirectory()) {
                                            val inFile: InputStream = FileInputStream(tempFile)
                                            file_length = tempFile.length()
                                            Common.ftpEnterMode(ftpClient, !g.Common.VK)
                                            ftpClient.setCopyStreamListener(streamListener)
                                            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)

                                            if (ftpClient.storeFile(tempFile.getName(), inFile)) {
                                                tempFile.delete()
                                            } else {
                                                bSuccessSend = false
                                                // Не удалось отправить файл
                                                textToLog.add(getString(R.string.message_file_not_sent))
                                                publishProgress(-2, textToLog.size)
                                            }
                                        }
                                    }
                                }
                            }

                            if (bSuccessSend) {
                                // архив
                                val inFile: InputStream = FileInputStream(zipFile)
                                file_length = zipFile.length()
                                Common.ftpEnterMode(ftpClient, !g.Common.VK)
                                ftpClient.setCopyStreamListener(streamListener)
                                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
                                //ftpClient.storeFile(m_FTP_server_directory+"/eoutf.zip", inFile);
                                if (ftpClient.storeFile("eoutf.zip", inFile)) {
                                    // При успешной отправке установим признак
                                    for (uuid in uuids) {
                                        // Состояние переключим в "Отправлен", было если "Создан" или "Согласование"
                                        val cv = ContentValues()
                                        cv.put("state", E_ORDER_STATE.E_ORDER_STATE_SENT.value())
                                        getContentResolver().update(
                                            MTradeContentProvider.ORDERS_CONTENT_URI,
                                            cv,
                                            "uid=? and (state=? or state=?)",
                                            arrayOf<String?>(
                                                uuid.toString(),
                                                E_ORDER_STATE.E_ORDER_STATE_CREATED.value()
                                                    .toString(),
                                                E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value()
                                                    .toString()
                                            )
                                        )
                                        // для выполненных если меняют, например, комментарий и заказ уходит, состояние не изменится
                                        // установим version_ack=version
                                        val versionCursor = getContentResolver().query(
                                            MTradeContentProvider.ORDERS_CONTENT_URI,
                                            arrayOf<String>("version", "versionPDA"),
                                            "uid=?",
                                            arrayOf<String?>(uuid.toString()),
                                            null
                                        )
                                        if (versionCursor!!.moveToFirst()) {
                                            cv.clear()
                                            cv.put("version_ack", versionCursor.getString(0))
                                            cv.put(
                                                "versionPDA",
                                                versionCursor!!.getString(1)
                                            ) // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(
                                                MTradeContentProvider.ORDERS_CONTENT_URI,
                                                cv,
                                                "uid=?",
                                                arrayOf<String?>(uuid.toString())
                                            )
                                        }
                                        versionCursor.close()
                                    }
                                    // При успешной отправке установим признак
                                    for (uuid in uuidsPayments) {
                                        // Состояние переключим в "Отправлен", если было "Создан"
                                        val cv = ContentValues()
                                        cv.put(
                                            "state",
                                            E_PAYMENT_STATE.E_PAYMENT_STATE_SENT.value()
                                        )
                                        getContentResolver().update(
                                            MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                                            cv,
                                            "uid=? and (state=?)",
                                            arrayOf<String?>(
                                                uuid.toString(),
                                                E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED.value()
                                                    .toString()
                                            )
                                        )
                                        // для выполненных если меняют, например, комментарий и заказ уходит, состояние не изменится
                                        // установим version_ack=version
                                        val versionCursor = getContentResolver().query(
                                            MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                                            arrayOf<String>("version", "versionPDA"),
                                            "uid=?",
                                            arrayOf<String?>(uuid.toString()),
                                            null
                                        )
                                        if (versionCursor!!.moveToFirst()) {
                                            cv.clear()
                                            cv.put("version_ack", versionCursor.getString(0))
                                            cv.put(
                                                "versionPDA",
                                                versionCursor!!.getString(1)
                                            ) // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(
                                                MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI,
                                                cv,
                                                "uid=?",
                                                arrayOf<String?>(uuid.toString())
                                            )
                                        }
                                        versionCursor.close()
                                    }
                                    // При успешной отправке установим признак
                                    for (uuid in uuidsRefunds) {
                                        // Состояние переключим в "Отправлен", если было "Создан" или "Согласование"
                                        val cv = ContentValues()
                                        cv.put("state", E_REFUND_STATE.E_REFUND_STATE_SENT.value())
                                        getContentResolver().update(
                                            MTradeContentProvider.REFUNDS_CONTENT_URI,
                                            cv,
                                            "uid=? and (state=? or state=?)",
                                            arrayOf<String?>(
                                                uuid.toString(),
                                                E_REFUND_STATE.E_REFUND_STATE_CREATED.value()
                                                    .toString(),
                                                E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()
                                                    .toString()
                                            )
                                        )
                                        // установим version_ack=version
                                        val versionCursor = getContentResolver().query(
                                            MTradeContentProvider.REFUNDS_CONTENT_URI,
                                            arrayOf<String>("version", "versionPDA"),
                                            "uid=?",
                                            arrayOf<String?>(uuid.toString()),
                                            null
                                        )
                                        if (versionCursor!!.moveToFirst()) {
                                            cv.clear()
                                            cv.put("version_ack", versionCursor.getString(0))
                                            cv.put(
                                                "versionPDA",
                                                versionCursor!!.getString(1)
                                            ) // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(
                                                MTradeContentProvider.REFUNDS_CONTENT_URI,
                                                cv,
                                                "uid=?",
                                                arrayOf<String?>(uuid.toString())
                                            )
                                        }
                                        versionCursor.close()
                                    }
                                    // При успешной отправке установим признак
                                    for (uuid in uuidsDistribs) {
                                        // Состояние переключим в "Отправлен", если было "Создан"
                                        val cv = ContentValues()
                                        cv.put(
                                            "state",
                                            E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()
                                        )
                                        getContentResolver().update(
                                            MTradeContentProvider.DISTRIBS_CONTENT_URI,
                                            cv,
                                            "uid=? and (state=? or state=?)",
                                            arrayOf<String?>(
                                                uuid.toString(),
                                                E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()
                                                    .toString()
                                            )
                                        )
                                        // установим version_ack=version
                                        val versionCursor = getContentResolver().query(
                                            MTradeContentProvider.DISTRIBS_CONTENT_URI,
                                            arrayOf<String>("version", "versionPDA"),
                                            "uid=?",
                                            arrayOf<String?>(uuid.toString()),
                                            null
                                        )
                                        if (versionCursor!!.moveToFirst()) {
                                            cv.clear()
                                            cv.put("version_ack", versionCursor.getString(0))
                                            cv.put(
                                                "versionPDA",
                                                versionCursor!!.getString(1)
                                            ) // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(
                                                MTradeContentProvider.DISTRIBS_CONTENT_URI,
                                                cv,
                                                "uid=?",
                                                arrayOf<String?>(uuid.toString())
                                            )
                                        }
                                        versionCursor.close()
                                    }

                                    // При успешной отправке установим признак
                                    for (_id in idsVisits) {
                                        val cv = ContentValues()
                                        // установим version_ack=version
                                        val versionCursor = getContentResolver().query(
                                            MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                                            arrayOf<String>("version"),
                                            "_id=?",
                                            arrayOf<String?>(_id.toString()),
                                            null
                                        )
                                        if (versionCursor!!.moveToFirst()) {
                                            cv.clear()
                                            cv.put("version_ack", versionCursor.getString(0))
                                            getContentResolver().update(
                                                MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI,
                                                cv,
                                                "_id=?",
                                                arrayOf<String?>(_id.toString())
                                            )
                                        }
                                        versionCursor.close()
                                    }
                                } else {
                                    // Не удалось отправить файл
                                    textToLog.add(
                                        getString(
                                            R.string.message_file_not_sent_reply_code,
                                            ftpClient.getReplyCode()
                                        )
                                    )
                                    publishProgress(-2, textToLog.size)
                                }
                                inFile.close()
                            } // bSuccessSend
                        } catch (e: IOException) {
                            // Файловая ошибка
                            textToLog.add(getString(R.string.message_file_error) + " " + e.toString())
                            publishProgress(-2, textToLog.size)
                        }
                        ftpClient.logout()
                        publishProgress(FTP_STATE_SUCCESS)
                    }

                    2 -> {
                        // Синхронизируем изображения
                        try {
                            Common.ftpEnterMode(ftpClient, !g.Common.VK)
                            if (!ftpClient.changeWorkingDirectory("images")) {
                                if (g.Common.TANDEM) {
                                    // если каталог не задан, значит мы зашли собственным пользователем
                                    // и надо выйти на уровень выше
                                    //if (m_FTP_server_directory.isEmpty())
                                    run {
                                        if (!ftpClient.changeWorkingDirectory("..")) {
                                            throw Exception("Не удалось зайти в каталог изображений FTP")
                                        }
                                    }
                                } else if (g.Common.PRODLIDER) {
                                    // если каталог не задан, значит мы зашли собственным пользователем
                                    // и надо выйти на уровень выше
                                    if (!ftpClient.changeWorkingDirectory("..")) {
                                        throw Exception("Не удалось зайти в каталог изображений FTP")
                                    }
                                    if (g.m_FTP_server_directory.isEmpty()) {
                                        if (!ftpClient.changeWorkingDirectory("Images")) {
                                            throw Exception("Не удалось зайти в каталог изображений FTP")
                                        }
                                    }
                                } else {
                                    throw Exception("Не удалось зайти в каталог images FTP")
                                }
                            }

                            // Имена существующих на FTP сервере файлов изображений
                            //mapImageFiles= new HashMap<String, Integer>();
                            bGoodsImagesMode =
                                true // Раньше HashMap уничтожался при выходе за скобки, и туда записывался NULL

                            // поэтому создание HashMap вынесено выше, и применен флаг

                            // Имена существующих на FTP сервере файлов изображений
                            //String ftp_image_names[]=new String[]{};
                            val imagesPath = Common.getMyStorageFileDir(this@MainActivity, "goods")
                            /*
                            if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                // получаем путь к изображениям на SD
                                imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
                            } else {
                                imagesPath = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/temp");
                            }
                            if (!imagesPath.exists()) {
                                imagesPath.mkdirs();
                            }
                             */
                            //File zipFile=null;
                            var fileList: Array<FTPFile>? = null
                            publishProgress(FTP_STATE_RECEIVE_IMAGE, 0)
                            Common.ftpEnterMode(ftpClient, !g.Common.VK)
                            fileList = ftpClient.listFiles()
                            for (ftpFile in fileList) {
                                val ftpName = ftpFile.getName()
                                var id = ""
                                if (ftpName.lowercase(Locale.getDefault())
                                        .endsWith(".jpg") || ftpName.lowercase(
                                        Locale.getDefault()
                                    ).endsWith(".png")
                                ) {
                                    id = Common.fileNameToId(
                                        ftpName.substring(
                                            0,
                                            ftpName.lastIndexOf(".")
                                        )
                                    )
                                }
                                if (!id.isEmpty()) {
                                    var bNeedLoad = true
                                    val ts = ftpFile.getTimestamp()
                                    file_length = ftpFile.getSize()
                                    val imageFile = File(imagesPath, ftpName)
                                    if (imageFile.exists()) {
                                        // Из-за того, что setLastModified (ниже по коду) не работает
                                        // (раньше работал), вместо условия == в проверке даты делаем >=
                                        // т.е. более свежий файл по дате создания скорее всего не менялся
                                        if (imageFile.length() == file_length && imageFile.lastModified() >= ts.getTimeInMillis()) {
                                            bNeedLoad = false
                                            val bitMapOption = BitmapFactory.Options()
                                            bitMapOption.inJustDecodeBounds = true
                                            val bmp = BitmapFactory.decodeFile(
                                                imageFile.getAbsolutePath(),
                                                bitMapOption
                                            )
                                            mapImageFiles.put(
                                                id,
                                                ImageFileState(
                                                    imageFile.length(),
                                                    0,
                                                    bitMapOption.outWidth,
                                                    bitMapOption.outHeight,
                                                    ftpName
                                                )
                                            ) // 0 - файл был, не загружаем
                                        }
                                    }
                                    if (bNeedLoad) {
                                        // Скачиваем файл
                                        publishProgress(FTP_STATE_RECEIVE_IMAGE, 0)

                                        val outFile: OutputStream = FileOutputStream(imageFile)
                                        Common.ftpEnterMode(ftpClient, !g.Common.VK)
                                        ftpClient.setCopyStreamListener(streamListener)
                                        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE)
                                        if (ftpClient.retrieveFile(ftpName, outFile)) {
                                            outFile.flush()
                                            textToLog.add("Загружен файл изображения " + ftpName)
                                            publishProgress(-2, textToLog.size)
                                            mapImageFiles.put(
                                                id,
                                                ImageFileState(imageFile.length(), 1, ftpName)
                                            ) // 1 - файл новый, загружаем
                                        } else {
                                            textToLog.add("Не удалось загрузить файл изображения " + ftpName)
                                            publishProgress(-2, textToLog.size)
                                            file_length = 0
                                            mapImageFiles.put(
                                                id,
                                                ImageFileState(imageFile.length(), 2, ftpName)
                                            ) // 2 - файл новый, но не был загружен
                                        }
                                        outFile.close()
                                        if (file_length != 0L) {
                                            imageFile.setLastModified(ts.getTimeInMillis())
                                        }
                                    }
                                }
                            }

                            var bImagesAreAll = true

                            // Находим все существующие файлы изображений
                            val tempFileNames = imagesPath.listFiles()
                            if (tempFileNames != null) {
                                for (tempFile in tempFileNames) {
                                    if (!tempFile.isDirectory()) {
                                        var id = ""
                                        val tempFileName = tempFile.getName()
                                        //m_imagesToSendSize+=tempFile.length();
                                        if (tempFileName.lowercase(Locale.getDefault())
                                                .endsWith(".jpg") || tempFileName.lowercase(
                                                Locale.getDefault()
                                            ).endsWith(".png")
                                        ) {
                                            id = Common.fileNameToId(
                                                tempFileName.substring(
                                                    0,
                                                    tempFileName.lastIndexOf(".")
                                                )
                                            )
                                        }
                                        if (id.isEmpty()) {
                                            // Неправильное имя файла - удаляем
                                            tempFile.delete()
                                        } else {
                                            val `val`: ImageFileState?
                                            // Найдем, есть ли файл в нашем списке
                                            if (mapImageFiles.containsKey(id)) {
                                                `val` = mapImageFiles.get(id)
                                            } else {
                                                `val` = ImageFileState(
                                                    tempFile.length(),
                                                    3,
                                                    tempFileName
                                                ) // 3 - файл удален
                                                // Удаляем потому, что это файл уже не нужен
                                                tempFile.delete()
                                                mapImageFiles.put(id, `val`)
                                            }
                                            if (`val`!!.state != 0) {
                                                // Данные менялись
                                                bImageDataChanged = true
                                            }
                                            if (`val`.state == 2) {
                                                // Была ошибка при загрузке
                                                bImagesAreAll = false
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (e: InterruptedException) {
                            textToLog.add(getString(R.string.ftp_message_interrupted) + " " + e.toString())
                            publishProgress(-2, textToLog.size)
                            Thread.currentThread().interrupt()
                        } catch (e: Exception) {
                            // Ошибка
                            textToLog.add(getString(R.string.ftp_message_error) + " " + e.toString())
                            publishProgress(-2, textToLog.size)
                        }
                        ftpClient.logout()
                        publishProgress(FTP_STATE_SUCCESS)
                    }
                } // switch (mode)

                logWriter.flush()
                logWriter.close()


                // файлы изображений номенклатуры изменились, обновим ссылки в базе
                // 14.09.2018 добавлено обовление (size>0) при любой загрузке изображений товаров, даже если ничего не изменилось
                if (bImageDataChanged || mapImageFiles.size > 0) {
                    //if (mapImageFiles==null)
                    if (!bGoodsImagesMode) {
                        // Флаг установился только потому, что загружается номенклатура
                        //mapImageFiles= new HashMap<String, Integer>();

                        val imagesPath = Common.getMyStorageFileDir(this@MainActivity, "goods")
                        /*
                        if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            // получаем путь к изображениям на SD
                            imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
                        } else {
                            imagesPath = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/temp");
                        }
                         */
                        //if (imagesPath.exists()) {
                        val tempFileNames = imagesPath.listFiles()
                        if (tempFileNames != null) {
                            for (tempFile in tempFileNames) {
                                if (!tempFile.isDirectory()) {
                                    var id = ""
                                    val tempFileName = tempFile.getName()
                                    if (tempFileName.lowercase(Locale.getDefault())
                                            .endsWith(".jpg") || tempFileName.lowercase(
                                            Locale.getDefault()
                                        ).endsWith(".png")
                                    ) {
                                        id = Common.fileNameToId(
                                            tempFileName.substring(
                                                0,
                                                tempFileName.lastIndexOf(".")
                                            )
                                        )
                                    }
                                    if (!id.isEmpty()) {
                                        mapImageFiles.put(
                                            id,
                                            ImageFileState(tempFile.length(), 1, tempFileName)
                                        )
                                    }
                                }
                            }
                        }
                        //}
                    }

                    // Пройдем по всей номенклатуре с изображениями и удалим ссылку на отсутствующие изображения
                    val nomenclatureCursor = getContentResolver().query(
                        MTradeContentProvider.NOMENCLATURE_CONTENT_URI,
                        arrayOf<String>("_id", "id", "image_file", "image_file_size"),
                        "image_file<>?",
                        arrayOf<String>(""),
                        null
                    )
                    val index_id = nomenclatureCursor!!.getColumnIndex("_id")
                    val indexId = nomenclatureCursor.getColumnIndex("id")
                    //int indexImageFile = nomenclatureCursor.getColumnIndex("image_file");
                    val indexImageFileSize = nomenclatureCursor.getColumnIndex("image_file_size")
                    while (nomenclatureCursor.moveToNext()) {
                        var `val`: ImageFileState? =
                            ImageFileState(nomenclatureCursor!!.getLong(indexImageFileSize), 3, "")
                        val id = nomenclatureCursor.getString(indexId)
                        if (mapImageFiles.containsKey(id)) {
                            `val` = mapImageFiles.get(id)
                        }
                        if (`val`!!.state == 3) {
                            // такого файла уже нет, убираем
                            val _id = nomenclatureCursor.getInt(index_id)
                            val cv = ContentValues()
                            cv.put("image_file", "")
                            cv.put("image_file_checksum", 0)
                            cv.put("image_width", 0)
                            cv.put("image_height", 0)
                            cv.put("image_file_size", 0)
                            getContentResolver().update(
                                MTradeContentProvider.NOMENCLATURE_CONTENT_URI,
                                cv,
                                "_id=?",
                                arrayOf<String?>(_id.toString())
                            )
                        }
                    }
                    // А теперь установим имена файлов
                    nomenclatureCursor.close()
                    for (entry in mapImageFiles.entries) {
                        val key = entry.key
                        val value: ImageFileState = entry.value!!
                        if (value.state != 3) {
                            val cv = ContentValues()
                            //cv.put("image_file", key);
                            cv.put("image_file", value.image_file_name)
                            // TODO 06.02.2019
                            cv.put("image_file_checksum", 0)
                            cv.put("image_file_size", value.file_size)
                            cv.put("image_width", value.image_width)
                            cv.put("image_height", value.image_height)
                            //
                            getContentResolver().update(
                                MTradeContentProvider.NOMENCLATURE_CONTENT_URI,
                                cv,
                                "id=?",
                                arrayOf<String?>(key)
                            )
                        }
                    }

                    //image_file
                    // Найдем номенклатуру с этим кодом
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: SocketException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                // Ошибка связи
                textToLog.add(getString(R.string.ftp_message_connection_error) + " " + e.toString())
                publishProgress(-2, textToLog.size)
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Ошибка ввода-вывода", e)
                // Ошибка ввода-вывода
                textToLog.add(getString(R.string.ftp_message_io_error) + " " + e.toString())
                publishProgress(-2, textToLog.size)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Ошибка ", e)
                // Ошибка
                textToLog.add(getString(R.string.ftp_message_error) + " " + e.toString())
                publishProgress(-2, textToLog.size)
            }
            return null
        }
    }

    fun updatePageMessagesVisibility() {
        val navigationView = findViewById<View?>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val navMenu = navigationView.getMenu()

        val g = MySingleton.getInstance()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val bDontShowMessages = sharedPreferences.getString("data_format", "DM") == "PH"
        val bDontShowPayments = sharedPreferences.getString("data_format", "DM") == "PH"
        val dataFormat: String = sharedPreferences.getString("data_format", "PL")!!
        val bDontShowRoutes = !(dataFormat == "PL" || dataFormat == "VK")

        //if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
        //    bOrdersMode = getIsOrdersMode();
        //}
        if (bDontShowMessages) {
            /*
            if (tabHost.getCurrentTab() == 2) {
                tabHost.setCurrentTab(0);
            }
            tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
            */
            navMenu.findItem(R.id.nav_messages).setVisible(false)
            navMenu.findItem(R.id.nav_payments).setVisible(false)
            if (m_current_frame_type == FRAME_TYPE_MESSAGES) {
                navMenu.findItem(R.id.nav_orders).setChecked(true)
                attachFrameType(FRAME_TYPE_ORDERS)
            }
        } else {
            //tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
            navMenu.findItem(R.id.nav_messages).setVisible(true)
        }
        if (bDontShowPayments) {
            navMenu.findItem(R.id.nav_payments).setVisible(false)
            if (m_current_frame_type == FRAME_TYPE_PAYMENTS) {
                navMenu.findItem(R.id.nav_orders).setChecked(true)
                attachFrameType(FRAME_TYPE_ORDERS)
            }
        } else {
            navMenu.findItem(R.id.nav_payments).setVisible(true)
        }
        if (bDontShowRoutes) {
            navMenu.findItem(R.id.nav_routes).setVisible(false)
            if (m_current_frame_type == FRAME_TYPE_ROUTES) {
                navMenu.findItem(R.id.nav_orders).setChecked(true)
                attachFrameType(FRAME_TYPE_ORDERS)
            }
        } else {
            navMenu.findItem(R.id.nav_routes).setVisible(true)
        }
        /*
        TODO
        Button btnConnect = (Button) findViewById(R.id.btnConnect); // 1
        Button btnRefresh = (Button) findViewById(R.id.btnRefresh); // 1
        Button btnQueryDocs = (Button) findViewById(R.id.btnQueryDocs);
        Button btnReceive = (Button) findViewById(R.id.btnReceive); // 0
        Button btnNomenclaturePhotos = (Button) findViewById(R.id.btnNomenclaturePhotos); // 2
        Button btnExchangeWebService = (Button) findViewById(R.id.btnExchangeWebService);

        if (g.Common.PHARAOH) {
            btnConnect.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setText(R.string.exchange_ws_all);
            btnReceive.setVisibility(View.GONE);
            btnNomenclaturePhotos.setVisibility(View.GONE);
            btnExchangeWebService.setVisibility(View.VISIBLE);
            btnQueryDocs.setVisibility(View.VISIBLE);
        } else {
            btnConnect.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setText(R.string.refresh);
            btnReceive.setVisibility(View.VISIBLE);
            if (!g.Common.PRAIT && !g.Common.MEGA && !g.Common.PRODLIDER && !g.Common.TITAN && !g.Common.TANDEM && !g.Common.ISTART && !g.Common.FACTORY)
                btnNomenclaturePhotos.setVisibility(View.GONE);
            else
                btnNomenclaturePhotos.setVisibility(View.VISIBLE);
            btnExchangeWebService.setVisibility(View.GONE);
            btnQueryDocs.setVisibility(View.GONE);
        }


        MySlider slidingDrawer1 = (MySlider) findViewById(R.id.slidingDrawer1);

        if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
            slidingDrawer1.setOnDrawerScrollListener(new OnDrawerScrollListener() {

                @Override
                public void onScrollStarted() {
                    // и сейчас не только при смене закладки - у нас также есть слайдер
                    // и это может быть как заказ, так и оплата
                    ActivityCompat.invalidateOptionsMenu(MainActivity.this);
                    try {
                        // из-за бага в версии android 3.2 меню не обновляется
                        onPrepareOptionsMenu(g_options_menu);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onScrollEnded() {
                }
            });
            slidingDrawer1.setVisibility(View.VISIBLE);
        } else {
            slidingDrawer1.setVisibility(View.GONE);
        }
        */
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String?) {
        val g = MySingleton.getInstance()
        g.updateCommonDataFormat(prefs.getString("data_format", "DM"))
        try {
            updatePageMessagesVisibility()
        } catch (e: Error) {
            // Can not perform this action after onSaveInstanceState
            // at ru.code22.mtrade.MainActivity.attachFrameType(MainActivity.java:1379)
        }
    }

    private fun checkForAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(Globals.appContext)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager!!.getAppUpdateInfo()

        // Create a listener to track request state updates.
        installStateUpdatedListener = object : InstallStateUpdatedListener {
            override fun onStateUpdate(installState: InstallState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)  // After the update is downloaded, show a notification
                // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister()
            }
        }

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(object : OnSuccessListener<AppUpdateInfo?> {
            override fun onSuccess(appUpdateInfo: AppUpdateInfo?) {
                //Toast.makeText(MainActivity.this,"addOnSuccessListener success", Toast.LENGTH_SHORT).show();
                if (appUpdateInfo?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    //Toast.makeText(MainActivity.this,"UPDATE_AVAILABLE", Toast.LENGTH_SHORT).show();
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        //Toast.makeText(MainActivity.this,"isUpdateTypeAllowed FLEXIBLE", Toast.LENGTH_SHORT).show();
                        // Before starting an update, register a listener for updates.
                        appUpdateManager!!.registerListener(installStateUpdatedListener!!)
                        // Start an update.
                        startAppUpdateFlexible(appUpdateInfo)
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        //Toast.makeText(MainActivity.this,"IMMEDIATE, start update", Toast.LENGTH_SHORT).show();
                        // Start an update.
                        startAppUpdateImmediate(appUpdateInfo)
                    }
                }
            }
        })
    }

    private fun startAppUpdateImmediate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    private fun startAppUpdateFlexible(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.FLEXIBLE,  // The current activity making the update request.
                this,  // Include a request code to later monitor this update request.
                REQ_CODE_VERSION_UPDATE
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
            unregisterInstallStateUpdListener()
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private fun popupSnackbarForCompleteUpdateAndUnregister() {
        val snackbar =
            Snackbar.make(
                findViewById<View?>(android.R.id.content),
                R.string.update_downloaded,
                Snackbar.LENGTH_INDEFINITE
            )
        snackbar.setAction(R.string.Restart, object : View.OnClickListener {
            override fun onClick(view: View?) {
                appUpdateManager!!.completeUpdate()
            }
        })
        snackbar.setActionTextColor(getResources().getColor(R.color.action_color))
        snackbar.show()

        unregisterInstallStateUpdListener()
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private fun checkNewAppVersionState() {
        appUpdateManager!!
            .getAppUpdateInfo()
            .addOnSuccessListener(object : OnSuccessListener<AppUpdateInfo?> {
                override fun onSuccess(appUpdateInfo: AppUpdateInfo?) {
                    //FLEXIBLE:
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo?.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdateAndUnregister()
                    }

                    //IMMEDIATE:
                    if (appUpdateInfo?.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    ) {
                        // If an in-app update is already running, resume the update.
                        startAppUpdateImmediate(appUpdateInfo)
                    }
                }
            })
    }

    /**
     * Needed only for FLEXIBLE update
     */
    private fun unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null) appUpdateManager!!.unregisterListener(
            installStateUpdatedListener!!
        )
    }

    companion object {
        //private static final int IDD_RECEIVE_PROGRESS = 0;
        private const val IDD_REINDEX_CREATED = 1
        private const val IDD_REINDEX_UPGRADED = 2
        private const val IDD_REINDEX_INCORRECT_CLOSED = 3
        private const val IDD_DELETE_CURRENT_ORDER = 4
        private const val IDD_CANCEL_CURRENT_ORDER = 5
        private const val IDD_DELETE_CLOSED_DOCUMENTS = 6
        const val IDD_DELETE_CURRENT_MESSAGE: Int = 7
        private const val IDD_DELETE_READED_MESSAGES = 8
        private const val IDD_QUERY_SEND_IMAGES = 9
        private const val IDD_SHOW_STATS = 10
        private const val IDD_DELETE_CURRENT_PAYMENT = 11
        private const val IDD_CANCEL_CURRENT_PAYMENT = 12
        private const val IDD_DELETE_CLOSED_PAYMENTS = 13
        private const val IDD_SHOW_PAYMENTS_STATS = 14
        private const val IDD_DATA_FILTER = 15

        //private static final int IDD_DELETE_CLOSED_REFUNDS = 16;
        private const val IDD_FILTER_DATE_BEGIN = 17
        private const val IDD_FILTER_DATE_END = 18

        private const val IDD_DELETE_CURRENT_REFUND = 19
        private const val IDD_CANCEL_CURRENT_REFUND = 20

        private const val IDD_DELETE_CURRENT_DISTRIBS = 21
        private const val IDD_CANCEL_CURRENT_DISTRIBS = 22

        private const val IDD_QUERY_DOCUMENTS_PERIOD = 23
        private const val IDD_QUERY_DOCUMENTS_DATE_BEGIN = 24
        private const val IDD_QUERY_DOCUMENTS_DATE_END = 25

        private const val IDD_SORT_ORDERS = 26

        private const val IDD_COULD_NOT_START_BUT_PERMISSIONS = 27

        private const val IDD_NEED_GPS_ENABLED = 28

        private const val IDD_QUERY_START_VISIT = 29
        private const val IDD_QUERY_END_VISIT = 30
        private const val IDD_QUERY_CANCEL_VISIT = 31
        private const val IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS = 32

        private const val IDD_REINDEX_UPGRADED66 = 33

        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
        private const val PERMISSION_REQUEST_STORAGE = 3
        private const val PERMISSION_REQUEST_CAMERA_FOR_ORDER = 4

        const val LOG_TAG: String = "mtradeLogs"

        const val BUFFER_SIZE: Int = 2048

        //private ProgressDialog progressDialog;
        //private ExchangeThread thread;
        //ExchangeTask exchangeTask;
        private const val photoFolder = "/mtrade/photo"

        //private String exif_ORIENTATION = "";
        private var outputPhotoFileUri: Uri? = null

        /*
    public static final int SELECT_CLIENT_REQUEST = 1;
    private static final int EDIT_ORDER_REQUEST = 2;
    private static final int EDIT_MESSAGE_REQUEST = 3;
    //private static final int CREATE_PHOTO_MESSAGE_REQUEST = 4;
    private static final int CAMERA_REQUEST = 5;
    private static final int EDIT_PAYMENT_REQUEST = 6;
    private static final int EDIT_REFUND_REQUEST = 7;
    private static final int EDIT_DISTRIBS_REQUEST = 8;
    private static final int EDIT_ORDER_PRE_REQUEST = 9;
    public static final int SELECT_ROUTES_WITH_DATES_REQUEST = 10;
     */
        private const val REQ_CODE_VERSION_UPDATE = 530

        private const val FTP_STATE_LOAD_IN_BASE = 1
        private const val FTP_STATE_SUCCESS = 2
        private const val FTP_STATE_FINISHED_ERROR = 3
        private const val FTP_STATE_SEND_EOUTF = 4
        private const val FTP_STATE_RECEIVE_ARCH = 5
        private const val FTP_STATE_LOAD_HISTORY_IN_BASE = 6
        private const val FTP_STATE_RECEIVE_IMAGE = 7

        //final int MENU_COLOR_RED = 1;
        //final int MENU_COLOR_GREEN = 2;
        //final int MENU_COLOR_BLUE = 3;
        // The loader's unique id. Loader ids are specific to the Activity or
        // Fragment in which they reside.
        private const val ORDERS_LOADER_ID = 1
        private const val PAYMENTS_LOADER_ID = 2
        private const val MESSAGES_LOADER_ID = 3

        // This is the Adapter being used to display the list's data
        //SimpleCursorAdapter ordersAdapter;
        //SimpleCursorAdapter paymentsAdapter;
        //SimpleCursorAdapter messagesAdapter;
        // These are the Contacts rows that we will retrieve
        //static final String[] PROJECTION = new String[] {ContactsContract.Data._ID,
        //        ContactsContract.Data.DISPLAY_NAME};
        //static final String[] ORDERS_PROJECTION = new String[] {"_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc", "color"};
        //static final String[] JOURNAL_PROJECTION = new String[]{"_id", "order_id", "refund_id", "distribs_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc", "sum_shipping", "color", "shipping_date"};
        //static final String[] CASH_PAYMENTS_PROJECTION = new String[]{"_id", "numdoc", "datedoc", "state", "client_descr", "sum_doc", "color", "zero"};
        //static final String[] MESSAGES_LIST_PROJECTION = new String[]{"_id", "uid", "descr", "text", "sender_id", "receiver_id", "client_id", "nomenclature_id", "fname", "datetime", "date1", "date2", "acknowledged", "inout", "type_idx", "zero"};
        private const val FRAME_TYPE_ROUTES = 1
        private const val FRAME_TYPE_ORDERS = 2
        private const val FRAME_TYPE_PAYMENTS = 3
        private const val FRAME_TYPE_EXCHANGE = 4
        private const val FRAME_TYPE_MESSAGES = 5

        private var m_client_id: MyID? = null
        private var m_filter_orders = true
        private var m_filter_refunds = true
        private var m_filter_distribs = true
        private var m_filter_type = 0
        private var m_filter_type_current = 0
        private var m_filter_date_type = 0
        private var m_filter_date_type_current = 0
        private var m_filter_date_begin = ""
        private var m_filter_date_end = ""
        private var m_filter_date_begin_current = ""
        private var m_filter_date_end_current = ""

        private var m_query_period_type = 0
        private var m_query_period_type_current = 0
        private var m_query_date_begin: String? = ""
        private var m_query_date_end: String? = ""
        private var m_query_date_begin_current = ""
        private var m_query_date_end_current = ""
        private var m_bNotClosed = false


        private const val REQUEST_PERMISSION = 61125
        private const val STATE_IN_PERMISSION = "inPermission"

        private const val SERVICE_TASK_STATE_BEGIN = 1
        const val SERVICE_TASK_STATE_REINDEX = 2
        const val SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED = 3
        const val SERVICE_TASK_STATE_REINDEX_UPGRADE = 4
        const val SERVICE_TASK_STATE_RECOVER_ORDERS = 5
        private const val SERVICE_TASK_STATE_MOVE_OLD_FILES = 6

    }
}
