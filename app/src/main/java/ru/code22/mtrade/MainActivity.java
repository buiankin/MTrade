package ru.code22.mtrade;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sourceforge.jheader.App1Header;
import net.sourceforge.jheader.App1Header.Tag;
import net.sourceforge.jheader.ExifFormatException;
import net.sourceforge.jheader.JpegFormatException;
import net.sourceforge.jheader.JpegHeaders;
import net.sourceforge.jheader.TagFormatException;
import net.sourceforge.jheader.TagValue;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import ru.code22.mtrade.MyDatabase.CashPaymentRecord;
import ru.code22.mtrade.MyDatabase.DistribsLineRecord;
import ru.code22.mtrade.MyDatabase.DistribsRecord;
import ru.code22.mtrade.MyDatabase.MessageRecord;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.MyDatabase.RefundRecord;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.nostra13.universalimageloader.utils.L;

public class MainActivity extends AppCompatActivity
        implements OnSharedPreferenceChangeListener, NavigationView.OnNavigationItemSelectedListener, UniversalInterface {

    private AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    //private static final int IDD_RECEIVE_PROGRESS = 0;
    private static final int IDD_REINDEX_CREATED = 1;
    private static final int IDD_REINDEX_UPGRADED = 2;
    private static final int IDD_REINDEX_INCORRECT_CLOSED = 3;
    private static final int IDD_DELETE_CURRENT_ORDER = 4;
    private static final int IDD_CANCEL_CURRENT_ORDER = 5;
    private static final int IDD_DELETE_CLOSED_DOCUMENTS = 6;
    public static final int IDD_DELETE_CURRENT_MESSAGE = 7;
    private static final int IDD_DELETE_READED_MESSAGES = 8;
    private static final int IDD_QUERY_SEND_IMAGES = 9;
    private static final int IDD_SHOW_STATS = 10;
    private static final int IDD_DELETE_CURRENT_PAYMENT = 11;
    private static final int IDD_CANCEL_CURRENT_PAYMENT = 12;
    private static final int IDD_DELETE_CLOSED_PAYMENTS = 13;
    private static final int IDD_SHOW_PAYMENTS_STATS = 14;
    private static final int IDD_DATA_FILTER = 15;
    //private static final int IDD_DELETE_CLOSED_REFUNDS = 16;

    private static final int IDD_FILTER_DATE_BEGIN = 17;
    private static final int IDD_FILTER_DATE_END = 18;

    private static final int IDD_DELETE_CURRENT_REFUND = 19;
    private static final int IDD_CANCEL_CURRENT_REFUND = 20;

    private static final int IDD_DELETE_CURRENT_DISTRIBS = 21;
    private static final int IDD_CANCEL_CURRENT_DISTRIBS = 22;

    private static final int IDD_QUERY_DOCUMENTS_PERIOD = 23;
    private static final int IDD_QUERY_DOCUMENTS_DATE_BEGIN = 24;
    private static final int IDD_QUERY_DOCUMENTS_DATE_END = 25;

    private static final int IDD_SORT_ORDERS = 26;

    private static final int IDD_COULD_NOT_START_BUT_PERMISSIONS = 27;

    private static final int IDD_NEED_GPS_ENABLED = 28;

    private static final int IDD_QUERY_START_VISIT = 29;
    private static final int IDD_QUERY_END_VISIT = 30;
    private static final int IDD_QUERY_CANCEL_VISIT = 31;
    private static final int IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS = 32;

    private static final int IDD_REINDEX_UPGRADED66 = 33;

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;
    private static final int PERMISSION_REQUEST_STORAGE = 3;
    private static final int PERMISSION_REQUEST_CAMERA_FOR_ORDER = 4;

    static final String LOG_TAG = "mtradeLogs";

    static final int BUFFER_SIZE = 2048;

    //private ProgressDialog progressDialog;
    //private ExchangeThread thread;
    //ExchangeTask exchangeTask;

    private static final String photoFolder = "/mtrade/photo";
    //private String exif_ORIENTATION = "";
    private static Uri outputPhotoFileUri = null;
    //File puc_img;

    //boolean m_preferences_from_ini;
    //SharedPreferences m_sharedPreferences;

    //EditText etClient;
    //ListView lvMessages;
    //int defaultTextColor;

    Menu g_options_menu = null;

    //public TabHost tabHost;

    private ActivityResultLauncher<Intent> selectClientActivityResultLauncher;
    private ActivityResultLauncher<Intent> editOrderActivityResultLauncher;
    private ActivityResultLauncher<Intent> editMessageActivityResultLauncher;
    private ActivityResultLauncher<Intent> createCameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> editPaymentActivityResultLauncher;
    private ActivityResultLauncher<Intent> editRefundActivityResultLauncher;
    private ActivityResultLauncher<Intent> editDistribsActivityResultLauncher;
    private ActivityResultLauncher<Intent> editOrderPreActivityResultLauncher;

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

    private static final int REQ_CODE_VERSION_UPDATE = 530;

    private static final int FTP_STATE_LOAD_IN_BASE = 1;
    private static final int FTP_STATE_SUCCESS = 2;
    private static final int FTP_STATE_FINISHED_ERROR = 3;
    private static final int FTP_STATE_SEND_EOUTF = 4;
    private static final int FTP_STATE_RECEIVE_ARCH = 5;
    private static final int FTP_STATE_LOAD_HISTORY_IN_BASE = 6;
    private static final int FTP_STATE_RECEIVE_IMAGE = 7;

    //final int MENU_COLOR_RED = 1;
    //final int MENU_COLOR_GREEN = 2;
    //final int MENU_COLOR_BLUE = 3;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    private static final int ORDERS_LOADER_ID = 1;
    private static final int PAYMENTS_LOADER_ID = 2;
    private static final int MESSAGES_LOADER_ID = 3;

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

    private static final int FRAME_TYPE_ROUTES=1;
    private static final int FRAME_TYPE_ORDERS=2;
    private static final int FRAME_TYPE_PAYMENTS=3;
    private static final int FRAME_TYPE_EXCHANGE=4;
    private static final int FRAME_TYPE_MESSAGES=5;

    // 16.06.2018 перенесено в Globals
    //public static MyDatabase m_myBase=null;
    //
    private int m_current_frame_type;
    private static MyDatabase.MyID m_client_id;
    private String m_client_descr;

    // Это то, что выбрано во фрейме
    private MyDatabase.MyID m_route_id;
    private String m_route_date;
    private String m_route_descr;

    private class ParamsToCreateDocs
    {
        private String client_id;
        private String distr_point_id;
    };

    // При запросе разрешений через эту струкутуру передаются параметры
    // для последующего создания документа
    ParamsToCreateDocs mParamsToCreateDocsWhenRequestPermissions;

    private Dialog m_filter_dialog = null;
    private Dialog m_query_documents_dialog = null;
    private static boolean m_filter_orders = true;
    private static boolean m_filter_refunds = true;
    private static boolean m_filter_distribs = true;
    private static int m_filter_type = 0;
    private static int m_filter_type_current = 0;
    private static int m_filter_date_type = 0;
    private static int m_filter_date_type_current = 0;
    private static String m_filter_date_begin = "", m_filter_date_end = "";
    private static String m_filter_date_begin_current = "", m_filter_date_end_current = "";

    private static int m_query_period_type = 0;
    private static int m_query_period_type_current = 0;
    private static String m_query_date_begin = "", m_query_date_end = "";
    private static String m_query_date_begin_current = "", m_query_date_end_current = "";
    private static boolean m_bNotClosed;
    //SQLiteDatabase m_db;


    long m_order_id_to_process;
    long m_refund_id_to_process;
    long m_distribs_id_to_process;
    long m_payment_id_to_process;
    long m_message_id_to_process;

    boolean m_bExcludeImageMessages;
    int m_imagesToSendSize;

    protected LocationManager m_locationManager;
    protected boolean m_bNeedCoord;
    protected boolean m_bRegisteredEveryTimeUpdate;

    //ERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS

    boolean m_bMayResetBase;
    // внутри SQL базы
    String m_settings_DataFormat;
    //double m_settings_ticket_m;
    //double m_settings_ticket_w;
    String m_settings_agent_id;
    int m_settings_gps_interval;
    // Пусть так и лежит только в базе, считывать не будем
    //String m_settings_agent_price_type_id;

    //
    boolean m_bRefreshPressed;

    private static final int REQUEST_PERMISSION = 61125;
    private static final String STATE_IN_PERMISSION = "inPermission";
    private boolean isInPermission = false;

    private ArrayList<String> m_exchangeLogText;
    private String m_exchangeState;

    private CharSequence  mTitle;

    // This is the select criteria
    //static final String SELECTION = "((" +
    //        ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
    //        ContactsContract.Data.DISPLAY_NAME + " != '' ))";

    String decodeLoginOrPassword(String str, String codeStr) {
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
        if (str.length() == 0 || str.indexOf("*") != 0) {
            return str;
        }

        codeStr = codeStr.toLowerCase(Locale.ENGLISH);
        //long c=CRC32(codeStr);
        Checksum checksum = new CRC32();
        int i;
        for (i = 0; i < codeStr.length(); i++) {
            char ch = codeStr.charAt(i);
            if ("1234567890abcdefghijklmnopqrstuvwxyz".indexOf(ch) >= 0)
                checksum.update(ch);
        }
        long chval = checksum.getValue();

        StringBuilder sb = new StringBuilder();
        for (i = 1; i < str.length(); i++) {
            String str0 = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            char ch = str.charAt(i);
            int idx = str0.indexOf(ch);
            if (idx >= 0) {
                idx += (chval % 10);
                if (idx >= str0.length())
                    idx -= str0.length();
                sb.append(str0.charAt(idx));
            } else {
                // символа нет в списке
                sb.append(ch);
            }
            chval /= 10;
        }
        return sb.toString();
    }


    void readSettings() {
        MySingleton g = MySingleton.getInstance();
        m_settings_DataFormat = "";
        Cursor cursor = getContentResolver().query(MTradeContentProvider.SETTINGS_CONTENT_URI, new String[]{"fmt", "agent_id", "gps_interval"}, null, null, null);
        if (cursor.moveToFirst()) {
            int index_data_format = cursor.getColumnIndex("fmt");
            m_settings_DataFormat = cursor.getString(index_data_format);
            //int index_ticket_m = cursor.getColumnIndex("ticket_m");
            //int index_ticket_w = cursor.getColumnIndex("ticket_w");
            int index_agent_id = cursor.getColumnIndex("agent_id");
            //m_settings_ticket_m = cursor.getDouble(index_ticket_m);
            //m_settings_ticket_w = cursor.getDouble(index_ticket_w);
            m_settings_agent_id = cursor.getString(index_agent_id);
            int index_gps_interval = cursor.getColumnIndex("gps_interval");
            m_settings_gps_interval = cursor.getInt(index_gps_interval);
            if (m_settings_gps_interval < 0) {
                g.Common.HAVE_GPS_SETTINGS = true;
            }
            //int index_agent_price_type_id = cursor.getColumnIndex("agent_price_type_id");
            //m_settings_agent_price_type_id = cursor.getString(index_agent_price_type_id);

        } else {
            // Формат данных не заполняем специально
            m_settings_DataFormat = "";
            //m_settings_ticket_m = 0.0;
            //m_settings_ticket_w = 0.0;
            m_settings_agent_id = null;
            m_settings_gps_interval = 0;
            //m_settings_agent_price_type_id = null;
        }
        cursor.close();
        if (m_settings_agent_id == null || m_settings_agent_id.replace(" ", "").isEmpty()) {
            UUID uuid = UUID.randomUUID();
            m_settings_agent_id = uuid.toString();
            writeSettings();
        }

    }

    void writeSettings() {
        ContentValues cv = new ContentValues();
        cv.put("fmt", m_settings_DataFormat);
        //cv.put("ticket_m", m_settings_ticket_m);
        //cv.put("ticket_w", m_settings_ticket_w);
        cv.put("agent_id", m_settings_agent_id);
        cv.put("gps_interval", m_settings_gps_interval);
        //cv.put("agent_price_type_id", m_settings_agent_price_type_id);
        // если update не сработает, добавится строка через insert (так сделано в ContentProvider)
        getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, "", null);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11111111111111111111!!!!!!!!!!!!!!!!!!!!
    void checkGpsUpdateEveryTime() {
        MySingleton g = MySingleton.getInstance();
        if (m_settings_gps_interval > -1 || !(g.Common.PRODLIDER || g.Common.TANDEM)) {
            // обновление координат не требуется
            if (m_bRegisteredEveryTimeUpdate) {
                m_locationManager.removeUpdates(locListenerEveryTime);
                m_bRegisteredEveryTimeUpdate = false;
            }
        } else {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = m_locationManager.getBestProvider(criteria, true);
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    m_locationManager.requestLocationUpdates(provider, (-m_settings_gps_interval - 1) * 1000, 0, locListenerEveryTime);
                }
                m_bRegisteredEveryTimeUpdate = true;
            }
        }
    }

    @SuppressWarnings("unused")
    void resetBase() {
        MySingleton g = MySingleton.getInstance();
        // Сначала в настройках очищаем формат
        ContentValues cv = new ContentValues();
        cv.put("fmt", "");
        getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);
        // Затем очищаем все справочники
        getContentResolver().delete(MTradeContentProvider.VERSIONS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.SALES_LOADED_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.MESSAGES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.PLACES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null);
        getContentResolver().delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null);
        // TODO остальные таблицы
        // ...
        // Если демо-режим, загружаем данные из архива
        if (MySingleton.getInstance().m_DataFormat.equals("DM") && !Constants.MY_ISTART) {
            int size;
            InputStream is = getResources().openRawResource(R.raw.demo_arch);
            //ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
            try {
                ZipInputStream zin = new ZipInputStream(new BufferedInputStream(is, BUFFER_SIZE));
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    int total = (int) ze.getSize();
                    byte[] data = new byte[total];
                    try {
                        int rd = 0, block;
                        while (total - rd > 0 && (block = zin.read(data, rd, total - rd)) != -1) {
                            rd += block;
                        }
                        zin.closeEntry();
                        if (rd == total && rd > 0) {
                            String strUnzipped = new String(data, "windows-1251");

                            Pattern pattern = Pattern.compile("(##.*##)");
                            Matcher mtch = pattern.matcher(strUnzipped);
                            int start_prev = -1;
                            int start_this;
                            String prevSection = "";
                            boolean bContinue = true;
                            while (bContinue) {
                                String section = "";
                                if (mtch.find()) {
                                    section = mtch.group();
                                    start_this = mtch.start();
                                } else {
                                    bContinue = false;
                                    start_this = strUnzipped.length();
                                }
                                if (start_prev > 0) {
                                    if (prevSection.equals("##CLIENTS##")) {
                                        TextDatabase.LoadClients(getContentResolver(), MySingleton.getInstance().MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##AGREEMENTS30##")) {
                                        TextDatabase.LoadAgreements30(getContentResolver(), MySingleton.getInstance().MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##AGREEMENTS##")) {
                                        TextDatabase.LoadAgreements(getContentResolver(), MySingleton.getInstance().MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##NOMENCL##")) {
                                        TextDatabase.LoadNomenclature(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                        TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                                    } else if (prevSection.equals("##OSTAT##")) {
                                        TextDatabase.LoadOstat(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##DOLG##")) {
                                        TextDatabase.LoadDolg(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##PRICETYPE##")) {
                                        TextDatabase.LoadPriceTypes(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##PRICE##")) {
                                        TextDatabase.LoadPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##PRICE_AGREEMENTS30##")) {
                                        TextDatabase.LoadPricesAgreements30(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##STOCKS##")) {
                                        TextDatabase.LoadStocks(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##AGENTS##")) {
                                        TextDatabase.LoadAgents(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##CURATORS##")) {
                                        TextDatabase.LoadCurators(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##D_POINTS##")) {
                                        TextDatabase.LoadDistrPoints(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##ORGANIZATIONS##")) {
                                        TextDatabase.LoadOrganizations(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##PRICE_CLIENT##")) {
                                        TextDatabase.LoadClientsPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##PRICE_CURATOR##")) {
                                        TextDatabase.LoadCuratorsPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##ROUTES##")) {
                                        TextDatabase.LoadRoutes(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##ROUTES_DATES##")) {
                                        TextDatabase.LoadRoutesDates(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##MESSAGES##")) {
                                        TextDatabase.LoadMessages(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    } else if (prevSection.equals("##DISCOUNTS##")) {
                                        TextDatabase.LoadSimpleDiscounts(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                    }
                                }
                                start_prev = start_this + section.length();
                                prevSection = section;
                            }
                        }
                        if (g.Common.PHARAOH) {
                            // если в демо-режиме отлаживаем для фараона
                            //
                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId);
                            cv.put("code", "000001");
                            cv.put("descr", "Без скидки");
                            cv.put("priceProcent", 0.0);
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
                            //
                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId_2);
                            cv.put("code", "000002");
                            cv.put("descr", "Скидка 10%");
                            cv.put("priceProcent", -10.0);
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
                            //
                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId_3);
                            cv.put("code", "000003");
                            cv.put("descr", "Скидка 30%");
                            cv.put("priceProcent", -30.0);
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
                            //
                            //m_settings_ticket_m=150.0;
                            //m_settings_ticket_w=100.0;

                            //cv.clear();
                            //cv.put("ticket_m", m_settings_ticket_m);
                            //cv.put("ticket_w", m_settings_ticket_w);
                            //getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);

                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId);
                            cv.put("code", "000001");
                            cv.put("descr", "Стол 1");
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.PLACES_CONTENT_URI, cv);

                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId_2);
                            cv.put("code", "000002");
                            cv.put("descr", "Стол 2");
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.PLACES_CONTENT_URI, cv);

                            cv.clear();
                            cv.put("id", MyWebExchange.dummyId_3);
                            cv.put("code", "000003");
                            cv.put("descr", "Стол 3");
                            cv.put("isUsed", 1);
                            getContentResolver().insert(MTradeContentProvider.PLACES_CONTENT_URI, cv);

                        }
                    } finally {
                    }
                }
            } catch (IOException e) {

            } finally {

            }
            //Toast.makeText(MainActivity.this, "Изменен формат данных, справочники заполнены демонстрационными данными!", Toast.LENGTH_LONG).show();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_database_demo_filled), Snackbar.LENGTH_LONG).show();
        } else {
            //Toast.makeText(MainActivity.this, "Изменен формат данных, справочники очищены!", Toast.LENGTH_LONG).show();
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_database_cleared), Snackbar.LENGTH_LONG).show();
        }
        // В настройках устанавливаем формат
        //cv.clear();
        //cv.put("fmt", m_DataFormat);
        //getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);
        m_settings_DataFormat = MySingleton.getInstance().m_DataFormat;
        writeSettings();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MySingleton g = MySingleton.getInstance();

        mParamsToCreateDocsWhenRequestPermissions = null;

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

        g.Common.m_mtrade_version = BuildConfig.VERSION_CODE;

        m_bRefreshPressed = false;

        m_bMayResetBase = false;

        //m_sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);

        //readIniFile();
        //readPreferences();
        //g.Common.m_app_theme=m_sharedPreferences.getString("app_theme", "DARK");
        g.checkInitByDataAndSetTheme(this);

        if (savedInstanceState != null) {
            isInPermission = savedInstanceState.getBoolean(STATE_IN_PERMISSION, false);
            m_exchangeLogText = savedInstanceState.getStringArrayList("EXCHANGE_LOG_TEXT");
            m_exchangeState = savedInstanceState.getString("EXCHANGE_STATE");
            // Отбор клиентов
            String client_id = savedInstanceState.getString("client_id");
            if (client_id != null)
                m_client_id = new MyID(client_id);
            else
                m_client_id = new MyID();
            m_client_descr = savedInstanceState.getString("client_descr");
            // Отбор маршрута
            String route_id = savedInstanceState.getString("route_id");
            if (route_id != null)
                m_route_id = new MyID(route_id);
            else
                m_route_id = new MyID();
            m_route_date = savedInstanceState.getString("route_date");
            m_route_descr = savedInstanceState.getString("route_descr");
        } else {
            m_exchangeLogText = new ArrayList<>();
            m_exchangeState = getString(R.string.exchange_not_executed);
            m_client_id = null;
            m_client_descr = "";
            m_route_id = null;
            m_route_date = "";
            m_route_descr = "";
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Если текущая дата больше установленной рабочей даты, меняем рабочую дату
        java.util.Date date = new java.util.Date();
        java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
        if (work_date.compareTo(date) < 0)
            if (!Common.MyDateFormat("yyyyMMdd", date).equals(Common.MyDateFormat("yyyyMMdd", work_date))) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("work_date", DatePreference.formatter().format(date));
                editor.commit();
            }

		/*
		if (g.Common.m_app_theme.equals("DARK")){
			//setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.LightAppTheme);
		}
		*/

        selectClientActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == ClientsActivity.CLIENTS_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {

                                long id = data.getLongExtra("id", 0);
                                //Toast.makeText(MainActivity.this, "id=" + id, Toast.LENGTH_SHORT).show();
                                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, id);
                                //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
                                Cursor cursor = getContentResolver().query(singleUri, new String[]{"descr", "id"}, null, null, null);
                                if (cursor.moveToNext()) {
                                    int descrIndex = cursor.getColumnIndex("descr");
                                    int idIndex = cursor.getColumnIndex("id");
                                    String newWord = cursor.getString(descrIndex);
                                    String clientId = cursor.getString(idIndex);
                                    //EditText et = (EditText) findViewById(R.id.etClient);
                                    //et.setText(newWord);
                                    m_client_id = new MyID(clientId);
                                    m_client_descr = newWord;
                                    //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                                    //getSupportLoaderManager().restartLoader(PAYMENTS_LOADER_ID, null, MainActivity.this);
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
                                    if (fragment instanceof OrdersListFragment) {
                                        OrdersListFragment ordersListFragment = (OrdersListFragment) fragment;
                                        ordersListFragment.onFilterClientSelected(clientId, newWord);
                                    }
                                    if (fragment instanceof PaymentsListFragment) {
                                        PaymentsListFragment paymentsListFragment = (PaymentsListFragment) fragment;
                                        paymentsListFragment.onFilterClientSelected(clientId, newWord);
                                    }

                                }
                                cursor.close();
                            }
                        }
                    }
                });

        editOrderActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        Intent data = result.getData();
                        if (data != null && resultCode == OrderActivity.ORDER_RESULT_OK) {
                            if (g.MyDatabase.m_order_editing.accept_coord == 1) {
                                boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                g.MyDatabase.m_order_editing.gpsstate = gpsenabled ? 1 : 0;
                            }
                            g.MyDatabase.m_order_editing.versionPDA++;
                            // Перед записью считаем сумму документа
                            g.MyDatabase.m_order_editing.sumDoc = g.MyDatabase.m_order_editing.GetOrderSum(null, false);
                            g.MyDatabase.m_order_editing.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
                            if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED) {
                                g.MyDatabase.m_order_editing.state = E_ORDER_STATE.E_ORDER_STATE_CREATED;
                            }
                            TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id, 0);
                            if (g.Common.NEW_BACKUP_FORMAT) {
                                // Удаляем резервную копию документа после записи настоящего документа
                                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_new_editing_id);
                                getContentResolver().delete(singleUri, "editing_backup<>0", null);
                            }
                            // Переименовываем файл изображения
                            if ((g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) && g.MyDatabase.m_order_editing_created) {
                                File photoFileDir = g.Common.getMyStorageFileDir(getBaseContext(), "photo");
                                File attachFileDir = g.Common.getMyStorageFileDir(getBaseContext(), "attaches");
                                File fileOrderImage1 = new File(photoFileDir, "order_image_1.jpg");
                                if (fileOrderImage1.exists()) {
                                    File fileOrderImage1Dest = new File(attachFileDir, "order_image_1_" + g.MyDatabase.m_order_editing.uid.toString().replace("{", "").replace("}", "") + ".jpg");
                                    fileOrderImage1.renameTo(fileOrderImage1Dest);
                                }
                                File fileOrderImage2 = new File(photoFileDir, "order_image_2.jpg");
                                if (fileOrderImage2.exists()) {
                                    File fileOrderImage2Dest = new File(attachFileDir, "order_image_2_" + g.MyDatabase.m_order_editing.uid.toString().replace("{", "").replace("}", "") + ".jpg");
                                    fileOrderImage2.renameTo(fileOrderImage2Dest);
                                }

                            }
                            //
                            if (!m_bNeedCoord && g.MyDatabase.m_order_editing.accept_coord == 1) {
                                m_bNeedCoord = true;
                                startGPS();
                            }
                            // Резервное сохрание заявки в текстовый файл, либо удаление этой копии
                            if (!g.Common.NEW_BACKUP_FORMAT && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                // получаем путь к SD
                                File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                                // создаем каталог
                                if (!sdPath.exists()) {
                                    sdPath.mkdirs();
                                }
                                //
                                File sdFile = new File(sdPath, g.MyDatabase.m_order_editing.uid.toString() + ".txt");
                                if (E_ORDER_STATE.getCanBeRestoredFromTextFile(g.MyDatabase.m_order_editing.state)) {
                                    try {
                                        //BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sdFile), "cp1251"));
                                        bw.write("0\r\n"); // obsolette m_orders_version
                                        bw.write(String.valueOf(g.Common.m_mtrade_version));
                                        bw.write("\r\n");
                                        TextDatabase.SaveOrderToText(bw, g.MyDatabase.m_order_editing, false, true);
                                        bw.write("@@@"); // начиная с версии 3.0
                                        bw.flush();
                                        bw.close();
                                    } catch (IOException e) {
                                        Log.e(LOG_TAG, "error", e);
                                    }
                                } else {
                                    sdFile.delete();
                                }
                            }

                            if (g.Common.PHARAOH && g.MyDatabase.m_order_editing.dont_need_send == 0) {
                                // и запускаем обмен
                                new ExchangeTask().execute(3, 0, "");
                            }

                            // если режим маршрута, добавим туда заказ
                            updateDocumentInRoute(g.MyDatabase.m_order_editing.distr_point_id.toString(), g.MyDatabase.m_order_editing);

                        }
                        if (resultCode == OrderActivity.ORDER_RESULT_CANCEL) {
                            if (g.Common.NEW_BACKUP_FORMAT) {
                                // если документ не меняли, там будет -1, соответственно удалять нечего
                                if (g.MyDatabase.m_order_new_editing_id > 0) {
                                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_new_editing_id);
                                    getContentResolver().delete(singleUri, "editing_backup<>0", null);
                                }
                            } else {
                                // Старый вариант
                                if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_order_editing_id != 0) {
                                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_editing_id);
                                    getContentResolver().delete(singleUri, E_ORDER_STATE.getCanBeDeletedConditionWhere(), E_ORDER_STATE.getCanBeDeletedArgs());
                                }
                            }
                        }
                    }
                });

        editMessageActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Можем менять только свои сообщения (&4)!=0
                        if (result.getResultCode() == MessageActivity.MESSAGE_RESULT_OK && (g.MyDatabase.m_message_editing.acknowledged & 4) != 0) {
                            Intent data = result.getData();
                            if (data != null) {
                                g.MyDatabase.m_message_editing.ver++;
                                TextDatabase.SaveMessageSQL(getContentResolver(), g.MyDatabase, g.MyDatabase.m_message_editing, g.MyDatabase.m_message_editing_id);

                                if (!g.MyDatabase.m_message_editing.fname.isEmpty()) {
                                    File attachFileDir = Common.getMyStorageFileDir(MainActivity.this, "attaches");

                                    File inFile = new File(g.MyDatabase.m_message_editing.fname);
                                    File outFile = new File(attachFileDir, inFile.getName());

                                    boolean bOutFileOpened = false;

                                    InputStream in;
                                    OutputStream out;
                                    try {
                                        in = new FileInputStream(inFile);
                                        out = new FileOutputStream(outFile);
                                        bOutFileOpened = true;
                                        // Transfer bytes from in to out
                                        byte[] buf = new byte[1024];
                                        int len;
                                        while ((len = in.read(buf)) > 0) {
                                            out.write(buf, 0, len);
                                        }
                                        in.close();
                                        out.close();
                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        //e.printStackTrace();
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        //e.printStackTrace();
                                        if (bOutFileOpened) {
                                            outFile.delete();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
        createCameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Можем менять только свои сообщения (&4)!=0
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
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

                            Date date = new Date();
                            //String timeString=String.valueOf(System.currentTimeMillis());
                            String timeString = Common.MyDateFormat("dd.MM.yyyy HH:mm:ss", date);

                            String fileName = Common.getDateTimeAsString14(date).toString() + ".jpg";

                            File photoDir = Common.getMyStorageFileDir(MainActivity.this, photoFolder);
                            if (!photoDir.exists()) {
                                photoDir.mkdirs();
                            }

                            boolean bSavedOk = false;

                            File imgWithTime = new File(photoDir, fileName);
                            ExifInterface exifInterface = null;

                            if (data != null) {
                                //if the intent data is not null, use it
                                //puc_image = getImagePath(Uri.parse(data.toURI())); //update the image path accordingly
                                //StoreImage(this, Uri.parse(data.toURI()), puc_img);
                                if (data.getData() != null) {
                                    bSavedOk = ImagePrinting.StoreImage(MainActivity.this, data.getData(), imgWithTime, timeString);
                                    if (bSavedOk) {
                                        // Удалим первый файл, созданный телефоном
                                        String imgPath = ImagePrinting.getImagePath(MainActivity.this, data.getData());
                                        File oldFile = new File(imgPath);
                                        oldFile.delete();
                                    }
                                } else {
                                    // Самсунг)
                                    bSavedOk = ImagePrinting.StoreImage(MainActivity.this, outputPhotoFileUri, imgWithTime, timeString);
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
                                bSavedOk = ImagePrinting.StoreImage(MainActivity.this, outputPhotoFileUri, imgWithTime, timeString);
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
                                if (outputPhotoFileUri.getScheme().equals("content")) {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            InputStream iStream = getContentResolver().openInputStream(outputPhotoFileUri);
                                            exifInterface = new ExifInterface(iStream);
                                        }
                                    } catch (IOException e) {
                                        exifInterface = null;
                                    }
                        /* TODO удалять файл
                        FileProvider provider = new FileProvider();
                        grantUriPermission(getApplicationContext().getPackageName(), outputPhotoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Падает вот здесь
                        provider.delete(outputPhotoFileUri, null, null);
                        */
                                } else {
                                    String imgPath = ImagePrinting.getImagePath(MainActivity.this, outputPhotoFileUri);
                                    File oldFile = new File(imgPath);
                                    try {
                                        exifInterface = new ExifInterface(imgPath);
                                    } catch (IOException e) {
                                        exifInterface = null;
                                    }
                                    oldFile.delete();
                                }

                                String exif_DATETIME = Common.MyDateFormat("yyyy:MM:dd HH:mm:ss", date);
                                String exif_MODEL = "Android";
                                String exif_MAKE = "";
                                //String exif_IMAGE_LENGTH="";
                                //String exif_IMAGE_WIDTH="";

                                if (exifInterface != null) {
                                    //exif_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                                    exif_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                                    exif_MAKE = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                                    //String et=exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                                    //exif_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface..TAG_IMAGE_LENGTH);
                                    //exif_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                                }
                                try {
                                    ExifInterface exifInterface2 = new ExifInterface(imgWithTime.getAbsolutePath());
                                    exifInterface2.setAttribute(ExifInterface.TAG_DATETIME, exif_DATETIME);
                                    exifInterface2.setAttribute(ExifInterface.TAG_MODEL, exif_MODEL);
                                    exifInterface2.setAttribute(ExifInterface.TAG_MAKE, exif_MAKE);
                                    exifInterface2.saveAttributes();

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
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                editMessage(0, imgWithTime.getAbsolutePath());
                            }
                        }
                    }
                });
        editPaymentActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == PaymentActivity.PAYMENT_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                g.MyDatabase.m_payment_editing.versionPDA++;
                                TextDatabase.SavePaymentSQL(getContentResolver(), g.MyDatabase, g.MyDatabase.m_payment_editing, g.MyDatabase.m_payment_editing_id);
                                if (!m_bNeedCoord && g.MyDatabase.m_payment_editing.accept_coord == 1) {
                                    m_bNeedCoord = true;
                                    startGPS();
                                }
                                // если режим маршрута, добавим туда платеж
                                updateDocumentInRoute(g.MyDatabase.m_payment_editing.distr_point_id.toString(), g.MyDatabase.m_payment_editing);
                            }
                        }
                    }
                });
        editRefundActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        int resultCode = result.getResultCode();
                        if (resultCode == RefundActivity.REFUND_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                if (g.MyDatabase.m_refund_editing.accept_coord == 1) {
                                    boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    g.MyDatabase.m_refund_editing.gpsstate = gpsenabled ? 1 : 0;
                                }
                                g.MyDatabase.m_refund_editing.versionPDA++;
                                // Перед записью считаем вес документа
                                g.MyDatabase.m_refund_editing.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
                                if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
                                    g.MyDatabase.m_refund_editing.state = E_REFUND_STATE.E_REFUND_STATE_CREATED;
                                }
                                TextDatabase.SaveRefundSQL(getContentResolver(), g.MyDatabase.m_refund_editing, g.MyDatabase.m_refund_editing_id);
                                if (!m_bNeedCoord && g.MyDatabase.m_refund_editing.accept_coord == 1) {
                                    m_bNeedCoord = true;
                                    startGPS();
                                }
                                // Резервное сохрание возврата в текстовый файл, либо удаление этой копии
                                // не выполняем, в отличие от заказа
                                // TODO

                                // если режим маршрута, добавим туда возврат
                                updateDocumentInRoute(g.MyDatabase.m_refund_editing.distr_point_id.toString(), g.MyDatabase.m_refund_editing);
                            }
                        }
                        if (resultCode == RefundActivity.REFUND_RESULT_CANCEL) {
                            if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_refund_editing_id != 0) {
                                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.REFUNDS_CONTENT_URI, g.MyDatabase.m_refund_editing_id);
                                getContentResolver().delete(singleUri, E_REFUND_STATE.getCanBeDeletedConditionWhere(), E_REFUND_STATE.getCanBeDeletedArgs());
                            }
                        }
                    }
                });
        editDistribsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == DistribsActivity.DISTRIBS_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                if (g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                                    boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    g.MyDatabase.m_distribs_editing.gpsstate = gpsenabled ? 1 : 0;
                                }
                                g.MyDatabase.m_distribs_editing.versionPDA++;
                                TextDatabase.SaveDistribsSQL(getContentResolver(), g.MyDatabase.m_distribs_editing, g.MyDatabase.m_distribs_editing_id);
                                if (!m_bNeedCoord && g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                                    m_bNeedCoord = true;
                                    startGPS();
                                }
                                // если режим маршрута, добавим туда дистрибьюцию
                                updateDocumentInRoute(g.MyDatabase.m_distribs_editing.distr_point_id.toString(), g.MyDatabase.m_distribs_editing);
                            }
                        }
                    }
                });

        editOrderPreActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == OrderPreActivity.ORDER_PRE_ACTION_RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null)
                                editOrder(0, false, data.getStringExtra("client_id"), data.getStringExtra("distr_point_id"));
                        }
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        setContentView(R.layout.main_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setPopupTheme();

        if (Constants.MY_DEBUG) {
            setTitle("DEBUG");
        } else {
            setTitle(R.string.app_label);
        }

        mTitle = getTitle();

        m_current_frame_type = (g.Common.PRODLIDER && !g.Common.DEMO) ? FRAME_TYPE_ROUTES : FRAME_TYPE_ORDERS;
        if (savedInstanceState != null)
            m_current_frame_type = savedInstanceState.getInt("current_frame_type", FRAME_TYPE_ORDERS);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        boolean reindexNeeded = false;

        if (g.MyDatabase == null) {
            // Первый запуск
            g.MyDatabase = new MyDatabase();
            g.MyDatabase.m_order_editing.bCreatedInSingleton = false;
            g.MyDatabase.m_refund_editing.bCreatedInSingleton = false;
            //g.Common=new Common();
            // TODO все остальное тоже в globals перенести
            //m_client_id = null;
            //m_client_descr = "";
            m_bNotClosed = false;

            if (Constants.MY_DEBUG == false) {
                // Проверим, требуется ли индексация базы
                Cursor cursor = getContentResolver().query(MTradeContentProvider.SEANCES_CONTENT_URI, new String[]{"incoming", "outgoing"}, null, null, null);
                if (cursor != null) {
                    int index_incoming = cursor.getColumnIndex("incoming");
                    int index_outgoing = cursor.getColumnIndex("outgoing");

                    String incoming = null;
                    String outgoing = null;

                    if (cursor.moveToNext()) {
                        incoming = cursor.getString(index_incoming);
                        outgoing = cursor.getString(index_outgoing);
                    }
                    if (incoming == null || outgoing == null || !incoming.equals(outgoing)) {
                        // Реиндексация требуется
                        // В новом формате вопросов не задается
                        if (g.Common.PHARAOH || g.Common.NEW_BACKUP_FORMAT) {
                            //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                            //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                            if (incoming != null && incoming.startsWith("UPGRADED") && MTradeContentProvider.DB_VERSION == 67) {
                                // 66 это версия базы, когда изменилась у андроида работа с файлами на флэшке
                                // поэтому попытаемся скопировать
                                new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED), "66", "");
                            } else {
                                new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED), "", "");
                            }
                        } else {
                            reindexNeeded = true;
                            if (incoming != null && incoming.equals("CREATED")) {
                                showDialog(IDD_REINDEX_CREATED);
                            } else if (incoming != null && incoming.startsWith("UPGRADED")) {
                                // 66 это версия базы, когда изменилась у андроида работа с файлами на флэшке
                                // поэтому попытаемся скопировать
                                if (MTradeContentProvider.DB_VERSION == 67)
                                    showDialog(IDD_REINDEX_UPGRADED66);
                                else
                                    showDialog(IDD_REINDEX_UPGRADED);
                            } else {
                                showDialog(IDD_REINDEX_INCORRECT_CLOSED);
                            }
                        }
                    }
                    cursor.close();
                }
            }

            // Проверим, есть ли заказы не до конца записанные (сбой)
            // у них мы будем пересчитывать сумму и прочее
            if (g.Common.NEW_BACKUP_FORMAT) {
                // TODO перенести в RecoverOrders может быть
                // восстанавливаем созданные заказы, которые не были сохранены (editing_backup=1)
                Cursor cursor = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "editing_backup=1", null, null);
                if (cursor != null) {
                    int index_id = cursor.getColumnIndex("_id");
                    while (cursor.moveToNext()) {
                        OrderRecord or = new OrderRecord();
                        //etClient.setText(cursor.getString(0));
                        long backup_order_id = cursor.getLong(index_id);
                        TextDatabase.ReadOrderBy_Id(getContentResolver(), or, backup_order_id);
                        // TODO остальные поля, такие как цены, например
                        or.sumDoc = or.GetOrderSum(null, false);
                        or.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), or, null, false);
                        // TODO не помню уже что имел в виду, когда писал следующий комментарий
                        or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
                        or.state = E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED;
                        long _id = 0; // чтобы все же в журнал документ добавился
                        _id = TextDatabase.SaveOrderSQL(getContentResolver(), or, _id, 0);
                        // теперь удаляем копию
                        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, backup_order_id);
                        getContentResolver().delete(singleUri, "editing_backup=1", null);
                    }
                    cursor.close();
                }
                // ищем заказы, которые начинали редактировать
                // и если оригинальный заказ пустой, перезапишем его
                cursor = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id", "old_id"}, "editing_backup=2", null, null);
                if (cursor != null) {
                    int index_id = cursor.getColumnIndex("_id");
                    int index_old_id = cursor.getColumnIndex("old_id");
                    while (cursor.moveToNext()) {
                        OrderRecord or = new OrderRecord();
                        long backup_order_id = cursor.getLong(index_id);
                        long old_order_id = cursor.getLong(index_old_id);
                        TextDatabase.ReadOrderBy_Id(getContentResolver(), or, old_order_id);
                        if (or.lines.size() == 0) {
                            int versionPDA = or.versionPDA;
                            TextDatabase.ReadOrderBy_Id(getContentResolver(), or, backup_order_id);
                            or.versionPDA = versionPDA + 1;
                            // TODO остальные поля, такие как цены, например
                            or.sumDoc = or.GetOrderSum(null, false);
                            or.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), or, null, false);
                            or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
                            or.state = E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED;
                            TextDatabase.SaveOrderSQL(getContentResolver(), or, old_order_id, 0);
                            // теперь удаляем копию
                            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, backup_order_id);
                            getContentResolver().delete(singleUri, "editing_backup=2", null);
                        }
                    }
                    cursor.close();
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
                ContentValues cv = new ContentValues();
                cv.put("incoming", g.MyDatabase.m_seance.toString());
                getContentResolver().insert(MTradeContentProvider.SEANCES_INCOMING_CONTENT_URI, cv);
            } catch (Exception e) {
                // при просмотре ошибок на маркете (в версии 3.49) есть только такая "Unknown URL content://ru.code22.providers.mtrade/seancesIncoming"
            }
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu navMenu = navigationView.getMenu();

        // Здесь также установится название клиента из отбора
        afterCreate(reindexNeeded);

        setCheckedMode(m_current_frame_type);
        attachFrameType(m_current_frame_type);

        // Проверка добавлена 24.04.2023
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU)
        {
            // взято отсюда
            // https://github.com/commonsguy/cw-omnibus/blob/master/Location/Classic/app/src/main/java/com/commonsware/android/weather2/AbstractPermissionActivity.java
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                onReady(savedInstanceState);
            } else {
                if (!isInPermission) {
                    isInPermission = true;
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                }
            }
        }

        checkForAppUpdate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
        outState.putStringArrayList("EXCHANGE_LOG_TEXT", m_exchangeLogText);
        outState.putInt("current_frame_type", m_current_frame_type);
        if (m_client_id!=null)
        {
            outState.putString("client_id", m_client_id.toString());
        } else
        {
            outState.putString("client_id", null);
        }
        outState.putString("client_descr", m_client_descr);
        if (m_route_id!=null)
        {
            outState.putString("route_id", m_route_id.toString());
        } else
        {
            outState.putString("route_id", null);
        }
        outState.putString("route_date", m_route_date);
        outState.putString("route_descr", m_route_descr);
    }

    private void onReady(Bundle savedInstanceState) {
        //MySingleton g = MySingleton.getInstance();
    }

    private void afterCreate(boolean reindexNeeded) {

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

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        // Отбор по клиенту
        m_client_descr=getString(R.string.catalogue_all_clients);
        if (m_client_id != null && !m_client_id.isEmpty()) {
            // клиент уже был выбран, повторный запуск, но память не освобождалась
            // Прочитаем название
            Cursor clientCursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{m_client_id.toString()}, null);
            if (clientCursor.moveToNext()) {
                m_client_descr=clientCursor.getString(0);
            } else
            {
                m_client_descr="{"+m_client_id.toString()+"}";
            }
            clientCursor.close();
        }
        // Маршрут
        // вообще, эти действия не обязательны, т.к. название клиента и маршрут считывается из настроек
        m_route_descr=getString(R.string.route_not_set);
        if (m_route_id!=null&&!m_route_id.isEmpty())
        {
            Cursor routeDateCursor = getContentResolver().query(MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI, new String[]{"route_date", "descr"}, "route_id=?", new String[]{m_route_id.toString()}, null);
            if (routeDateCursor.moveToNext()) {
                int index_route_date=routeDateCursor.getColumnIndex("route_date");
                int index_route_descr=routeDateCursor.getColumnIndex("descr");
                m_route_date=routeDateCursor.getString(index_route_date);
                m_route_descr=routeDateCursor.getString(index_route_descr);
            } else
            {
                m_route_date="";
                m_route_descr="{"+m_client_id.toString()+"}";
            }
            routeDateCursor.close();
        } else
        {
            // 05.06.2019 установим по умолчанию маршрут за этот день
            m_route_date=Common.MyDateFormat("yyyyMMdd", new Date());
            Cursor routeDateCursor = getContentResolver().query(MTradeContentProvider.ROUTES_DATES_LIST_CONTENT_URI, new String[]{"route_id", "descr"}, "route_date=?", new String[]{m_route_date}, null);
            if (routeDateCursor.moveToNext()) {
                int index_route_id=routeDateCursor.getColumnIndex("route_id");
                int index_route_descr=routeDateCursor.getColumnIndex("descr");
                m_route_id=new MyID(routeDateCursor.getString(index_route_id));
                m_route_descr=routeDateCursor.getString(index_route_descr);
            } else
            {
                m_route_date="";
                m_route_id=new MyID();
            }
            routeDateCursor.close();
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

        m_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // TODO остальные виды документа, кроме заказов
        String accept_coord_where = E_ORDER_STATE.getAcceptCoordConditionWhere();
        String[] accept_coord_selectionArgs = E_ORDER_STATE.getAcceptCoordConditionSelectionArgs();
        m_bNeedCoord = false;
        Cursor cursorAcceptCoord;
        cursorAcceptCoord = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"id"}, accept_coord_where, accept_coord_selectionArgs, null);
        if (cursorAcceptCoord.moveToFirst()) {
            m_bNeedCoord = true;
        }
        cursorAcceptCoord.close();
        // Визиты тоже проверим
        cursorAcceptCoord = getContentResolver().query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id"}, "accept_coord=1", null, null);
        if (cursorAcceptCoord.moveToFirst()) {
            m_bNeedCoord = true;
        }
        cursorAcceptCoord.close();
        if (m_bNeedCoord)
        {
            startGPS();
        }
        //
        m_bRegisteredEveryTimeUpdate = false;

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

        if (reindexNeeded == false)
            loadDataAfterStart();
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

    void checkSendImages() {
        m_imagesToSendSize = 0;
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

        File attachesFileDir=Common.getMyStorageFileDir(MainActivity.this, "attaches");
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


    protected void loadDataAfterStart() {
        // читаем версии объектов из базы
        readVersions();
        // читаем настройки
        readSettings();
        // в случае необходимости запускаем лог GPS
        checkGpsUpdateEveryTime();
        m_bMayResetBase = true;
        MySingleton g = MySingleton.getInstance();
        if (!m_settings_DataFormat.equals(g.m_DataFormat)) {
            // Очистим базу, либо заполним ее тестовыми данными
            // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
            if (m_settings_DataFormat.equals("DM") || g.m_DataFormat.equals("DM")) {
                resetBase();
            } else {
                m_settings_DataFormat = g.m_DataFormat;
                writeSettings();
            }
            // 24.03.2017
            // после смены формата будет выполнен "запросить все"
            setNegativeVersions();
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            // TODO 22.01.2019
            //getSupportLoaderManager().initLoader(ORDERS_LOADER_ID, null, this);
            //getSupportLoaderManager().initLoader(PAYMENTS_LOADER_ID, null, this);
            //getSupportLoaderManager().initLoader(MESSAGES_LOADER_ID, null, this);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);

            if (fragment instanceof OrdersListFragment)
            {
                OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                ordersListFragment.restartLoaderForListView();
            }
            if (fragment instanceof PaymentsListFragment)
            {
                PaymentsListFragment paymentsListFragment=(PaymentsListFragment)fragment;
                paymentsListFragment.restartLoaderForListView();
            }
            if (fragment instanceof MessagesListFragment)
            {
                MessagesListFragment messagesListFragment=(MessagesListFragment)fragment;
                messagesListFragment.restartLoaderForListView();
            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO Auto-generated method stub
        MySingleton g = MySingleton.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        g.readPreferences(sharedPreferences);

        // Если изменился формат
        if (g.Common.m_app_theme != null && !g.Common.m_app_theme.equals(sharedPreferences.getString("app_theme", "DARK"))) {
            //Toast.makeText(MainActivity.this, "Тема приложения изменится при перезапуске программы!", Toast.LENGTH_SHORT).show();
            // До 03.08.2018
            //Toast.makeText(MainActivity.this, getString(R.string.message_theme_will_changed_after_restart), Toast.LENGTH_SHORT).show();
            //
            g.Common.m_app_theme = sharedPreferences.getString("app_theme", "DARK");
            if (g.Common.m_app_theme.equals("DARK")) {
                setTheme(R.style.AppTheme);
            } else {
                setTheme(R.style.LightAppTheme);
            }
            recreate();
        }
        if (m_bMayResetBase && !m_settings_DataFormat.equals(g.m_DataFormat)) {
            // Очистим базу, либо заполним ее тестовыми данными
            // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
            if (m_settings_DataFormat.equals("DM") || g.m_DataFormat.equals("DM")) {
                resetBase();
            } else {
                m_settings_DataFormat = g.m_DataFormat;
                writeSettings();
            }
            // 24.03.2017
            // после смены формата будет выполнен "запросить все"
            setNegativeVersions();
        }
        // 05.04.2019
        updatePageMessagesVisibility();

        checkNewAppVersionState();
    }

    @Override
    protected void onDestroy() {
        MySingleton g = MySingleton.getInstance();
        if (g.MyDatabase.m_seance_closed) {
            ContentValues cv = new ContentValues();
            cv.put("outgoing", g.MyDatabase.m_seance.toString());
            getContentResolver().insert(MTradeContentProvider.SEANCES_OUTGOING_CONTENT_URI, cv);
        }
        // С 03.08.2018
        if (m_bNeedCoord) {
            m_locationManager.removeUpdates(locListener);
        }
        if (m_bRegisteredEveryTimeUpdate) {
            m_locationManager.removeUpdates(locListenerEveryTime);
        }
        //
        unregisterInstallStateUpdListener();
        super.onDestroy();
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

    private void setCheckedMode(int frameType)
    {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu navMenu=navigationView.getMenu();

        switch (frameType)
        {
            case FRAME_TYPE_ROUTES:
                navMenu.findItem(R.id.nav_routes).setChecked(true);
                break;
            case FRAME_TYPE_ORDERS:
                navMenu.findItem(R.id.nav_orders).setChecked(true);
                break;
            case FRAME_TYPE_PAYMENTS:
                navMenu.findItem(R.id.nav_payments).setChecked(true);
                break;
            case FRAME_TYPE_EXCHANGE:
                navMenu.findItem(R.id.nav_exchange).setChecked(true);
                break;
            case FRAME_TYPE_MESSAGES:
                navMenu.findItem(R.id.nav_messages).setChecked(true);
                break;
        }

    }

    private void attachFrameType(int frameType)
    {
        Fragment fragment = null;
        CharSequence pageTitle = null;
        if (frameType==FRAME_TYPE_ROUTES) {
            fragment = new RoutesListFragment();
            Bundle args = new Bundle();
            if (m_route_id != null) {
                args.putString("route_id", m_route_id.toString());
            } else {
                args.putString("route_id", null);
            }
            args.putString("route_date", m_route_date);
            args.putString("route_descr", m_route_descr);
            /*
            args.putInt("filter_orders", m_filter_orders ? 1 : 0);
            args.putInt("filter_refunds", m_filter_refunds ? 1 : 0);
            args.putInt("filter_distribs", m_filter_distribs ? 1 : 0);
            args.putString("filter_date_begin", m_filter_date_begin);
            args.putString("filter_date_end", m_filter_date_end);
            args.putInt("filter_type", m_filter_type);
            args.putInt("filter_date_type", m_filter_date_type);
            */
            fragment.setArguments(args);
            pageTitle = getString(R.string.tab_routes);
        } else if (frameType==FRAME_TYPE_ORDERS) {
            fragment = new OrdersListFragment();
            Bundle args = new Bundle();
            if (m_client_id!=null) {
                args.putString("client_id", m_client_id.toString());
            } else
            {
                args.putString("client_id", null);
            }
            args.putString("client_descr", m_client_descr);
            args.putInt("filter_orders", m_filter_orders?1:0);
            args.putInt("filter_refunds", m_filter_refunds?1:0);
            args.putInt("filter_distribs", m_filter_distribs?1:0);
            args.putString("filter_date_begin", m_filter_date_begin);
            args.putString("filter_date_end", m_filter_date_end);
            args.putInt("filter_type", m_filter_type);
            args.putInt("filter_date_type", m_filter_date_type);
            fragment.setArguments(args);
            pageTitle = getString(R.string.tab_orders);
        } else if (frameType==FRAME_TYPE_PAYMENTS) {
            fragment = new PaymentsListFragment();
            Bundle args = new Bundle();
            if (m_client_id!=null) {
                args.putString("client_id", m_client_id.toString());
            } else
            {
                args.putString("client_id", null);
            }
            args.putString("client_descr", m_client_descr);
            fragment.setArguments(args);
            pageTitle = getString(R.string.tab_payments);
        } else if (frameType==FRAME_TYPE_EXCHANGE) {
            fragment = new ExchangeFragment();
            Bundle args = new Bundle();
            //((ExchangeFragment)fragment).setExchangeState(m_exchangeState);
            //((ExchangeFragment)fragment).setLogText(m_exchangeLogText);
            args.putString("exchange_state", m_exchangeState);
            args.putStringArrayList("exchange_log_text", m_exchangeLogText);
            fragment.setArguments(args);
            pageTitle = getString(R.string.tab_exchange);
        } else if (frameType==FRAME_TYPE_MESSAGES) {
            fragment = new MessagesListFragment();
            pageTitle = getString(R.string.tab_messages);
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commitAllowingStateLoss();
            //mDrawerListView.setItemChecked(position, true);
            //mDrawerListView.setSelection(position);
            //setTitle(mItemTitles[position]);
            if (pageTitle!=null)
                setTitle(pageTitle);
            //mDrawerLayout.closeDrawer(mDrawerListView);
        }

        m_current_frame_type=frameType;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        MySingleton g = MySingleton.getInstance();

        int id = item.getItemId();
        if (id == R.id.nav_routes) {
            attachFrameType(FRAME_TYPE_ROUTES);
        } else if (id == R.id.nav_orders) {
            attachFrameType(FRAME_TYPE_ORDERS);
        } else if (id == R.id.nav_payments) {
            attachFrameType(FRAME_TYPE_PAYMENTS);
        } else if (id == R.id.nav_exchange) {
            attachFrameType(FRAME_TYPE_EXCHANGE);
        } else if (id == R.id.nav_messages) {
            attachFrameType(FRAME_TYPE_MESSAGES);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, PrefActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (g.m_preferences_from_ini)
                intent.putExtra("readOnly", true);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            onCloseActivity();
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    String getFileContents(File theFile) throws IOException {
        byte[] bytes = new byte[(int) theFile.length()];
        InputStream in = new FileInputStream(theFile);
        int m = 0, n = 0;
        while (m < bytes.length) {
            n = in.read(bytes, m, bytes.length - m);
            m += n;
        }
        in.close();

        return new String(bytes, "cp1251");
    }


    protected boolean deleteOrder(long _id) {
        MySingleton g = MySingleton.getInstance();
        boolean result = false;
        File sdFile = null;
        String uid = "";
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, _id);

        Cursor cursor = getContentResolver().query(singleUri, new String[]{"uid"}, null, null, null);
        if (cursor.moveToNext()) {
            int uid_Index = cursor.getColumnIndex("uid");
            uid=cursor.getString(uid_Index);
        }
        cursor.close();

        if (!g.Common.NEW_BACKUP_FORMAT) {
            // Удаление резервной копии заявки (текстовый файл)
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                if (!uid.isEmpty()) {
                    // получаем путь к SD
                    File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                    sdFile = new File(sdPath, uid + ".txt");
                }
            }
        }
        if (getContentResolver().delete(singleUri, E_ORDER_STATE.getCanBeDeletedConditionWhere(), E_ORDER_STATE.getCanBeDeletedArgs()) > 0) {
            result = true;
            if (sdFile != null) {
                sdFile.delete();
            }
        }
        if (g.Common.NEW_BACKUP_FORMAT) {
            // До 13.09.2018
            //singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, _id);
            //getContentResolver().delete(singleUri, "editing_backup<>0", null);
            // После 13.09.2018
            getContentResolver().delete(MTradeContentProvider.ORDERS_CONTENT_URI, "old_id=? and editing_backup<>0", new String[]{String.valueOf(_id)});
        }

        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid);

        return result;
    }

    protected boolean deleteRefund(long _id) {
        boolean result = false;
        //File sdFile = null;
        String uid = "";
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.REFUNDS_CONTENT_URI, _id);
        Cursor cursor = getContentResolver().query(singleUri, new String[]{"uid"}, null, null, null);
        if (cursor.moveToNext()) {
            int uid_Index = cursor.getColumnIndex("uid");
            uid=cursor.getString(uid_Index);
        }
        cursor.close();

        // Удаление резервной копии заявки (текстовый файл)
        // ... нет
        if (getContentResolver().delete(singleUri, E_REFUND_STATE.getCanBeDeletedConditionWhere(), E_REFUND_STATE.getCanBeDeletedArgs()) > 0) {
            result = true;
	    	/*
	    	if (sdFile!=null)
	    	{
	    		sdFile.delete();
	    	}
	    	*/
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid);

        return result;
    }

    protected boolean deleteDistribs(long _id) {
        boolean result = false;
        //File sdFile = null;
        String uid = "";
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTRIBS_CONTENT_URI, _id);
        Cursor cursor = getContentResolver().query(singleUri, new String[]{"uid"}, null, null, null);
        if (cursor.moveToNext()) {
            int uid_Index = cursor.getColumnIndex("uid");
            uid=cursor.getString(uid_Index);
        }
        cursor.close();
        // Удаление резервной копии заявки (текстовый файл)
        // ... нет
        if (getContentResolver().delete(singleUri, E_DISTRIBS_STATE.getDistribsCanBeDeletedConditionWhere(), E_DISTRIBS_STATE.getDistribsCanBeDeletedArgs()) > 0) {
            result = true;
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid);

        return result;
    }

    protected boolean deletePayment(long _id) {
        boolean result = false;
        String uid = "";
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, _id);
        Cursor cursor = getContentResolver().query(singleUri, new String[]{"uid"}, null, null, null);
        if (cursor.moveToNext()) {
            int uid_Index = cursor.getColumnIndex("uid");
            uid=cursor.getString(uid_Index);
        }
        cursor.close();
        if (getContentResolver().delete(singleUri, E_PAYMENT_STATE.getPaymentCanBeDeletedConditionWhere(), E_PAYMENT_STATE.getPaymentCanBeDeletedArgs()) > 0) {
            result = true;
        }
        // если режим маршрута, он должен быть перерисован
        updateDocumentInRouteDeleted(uid);

        return result;
    }

    protected void deleteMessage(long _id) {
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.MESSAGES_CONTENT_URI, _id);
        //getContentResolver().delete(singleUri, null, null);
        ContentValues cv = new ContentValues();
        cv.put("isMark", 1);
        getContentResolver().update(singleUri, cv, null, null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MySingleton g = MySingleton.getInstance();

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        //menu.setHeaderTitle(getString(R.string.menu_context_title));
        MenuInflater inflater = getMenuInflater();
        switch (v.getId()) {
            case R.id.listViewOrders: {
                inflater.inflate(R.menu.main_orders_list, menu);

                MenuItem itemCopyOrder = menu.findItem(R.id.action_copy_order);
                itemCopyOrder.setVisible(false);

                MenuItem itemCopyOrderToRefund = menu.findItem(R.id.action_copy_order_to_refund);
                itemCopyOrderToRefund.setVisible(false);

                MenuItem itemCancelOrder = menu.findItem(R.id.action_cancel_order);
                itemCancelOrder.setVisible(false);

                MenuItem itemDeleteOrder = menu.findItem(R.id.action_delete_order);
                itemDeleteOrder.setVisible(false);

                MenuItem itemCopyRefund = menu.findItem(R.id.action_copy_refund);
                itemCopyRefund.setVisible(false);

                MenuItem itemCancelRefund = menu.findItem(R.id.action_cancel_refund);
                itemCancelRefund.setVisible(false);

                MenuItem itemDeleteRefund = menu.findItem(R.id.action_delete_refund);
                itemDeleteRefund.setVisible(false);

                MenuItem itemCancelDistribs = menu.findItem(R.id.action_cancel_distribs);
                itemCancelDistribs.setVisible(false);

                MenuItem itemDeleteDistribs = menu.findItem(R.id.action_delete_distribs);
                itemDeleteDistribs.setVisible(false);


                //selectedWord = ((TextView) info.targetView).getText().toString();
                long _id = info.id;
                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, _id);
                Cursor cursor = getContentResolver().query(singleUri, new String[]{"iddocdef", "state"}, null, null, null);
                if (cursor.moveToNext()) {
                    int iddocdefIndex = cursor.getColumnIndex("iddocdef");
                    int stateIndex = cursor.getColumnIndex("state");
                    int iddocdef = cursor.getInt(iddocdefIndex);
                    switch (iddocdef) {
                        case 0: {
                            itemCopyOrder.setVisible(true);
                            if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                                itemCopyOrderToRefund.setVisible(true);
                            }
                            E_ORDER_STATE state = E_ORDER_STATE.fromInt(cursor.getInt(stateIndex));
                            if (E_ORDER_STATE.getCanBeDeleted(state)) {
                                itemDeleteOrder.setVisible(true);
                            }
                            if (E_ORDER_STATE.getCanBeCanceled(state)) {
                                itemCancelOrder.setVisible(true);
                            }
                            break;
                        }
                        case 1: {
                            E_REFUND_STATE state = E_REFUND_STATE.fromInt(cursor.getInt(stateIndex));
                            itemCopyRefund.setVisible(true);
                            if (E_REFUND_STATE.getCanBeDeleted(state)) {
                                itemDeleteRefund.setVisible(true);
                            }
                            if (E_REFUND_STATE.getCanBeCanceled(state)) {
                                itemCancelRefund.setVisible(true);
                            }
                            break;
                        }
                        case 3: {
                            E_DISTRIBS_STATE state = E_DISTRIBS_STATE.fromInt(cursor.getInt(stateIndex));
                            if (E_DISTRIBS_STATE.getDistribsCanBeDeleted(state)) {
                                itemDeleteDistribs.setVisible(true);
                            }
                            if (E_DISTRIBS_STATE.getDistribsCanBeCanceled(state)) {
                                itemCancelDistribs.setVisible(true);
                            }
                            break;
                        }
                    }
                }
                cursor.close();
            }
            //menu.add(0, MENU_COLOR_RED, 0, "Red");
            //menu.add(0, MENU_COLOR_GREEN, 0, "Green");
            //menu.add(0, MENU_COLOR_BLUE, 0, "Blue");
            break;
            case R.id.listViewPayments: {
                inflater.inflate(R.menu.main_payments_list, menu);

                MenuItem itemCancelPayment = menu.findItem(R.id.action_cancel_payment);
                itemCancelPayment.setVisible(false);

                MenuItem itemDeletePayment = menu.findItem(R.id.action_delete_payment);
                itemDeletePayment.setVisible(false);

                long _id = info.id;
                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, _id);
                Cursor cursor = getContentResolver().query(singleUri, new String[]{"state"}, null, null, null);
                if (cursor.moveToNext()) {
                    int stateIndex = cursor.getColumnIndex("state");
                    E_PAYMENT_STATE state = E_PAYMENT_STATE.fromInt(cursor.getInt(stateIndex));
                    if (E_PAYMENT_STATE.getPaymentCanBeDeleted(state)) {
                        itemDeletePayment.setVisible(true);
                    }
                    // TODO
				/*
				if (E_PAYMENT_STATE.getPaymentCanBeCanceled(state))
				{
					itemCancelPayment.setVisible(true);
				}
				*/
                }
                cursor.close();
            }
            break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_order: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long _id = info.id;
                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, _id), new String[]{"order_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    _id = cursor.getLong(0);
                } else {
                    _id = 0;
                }
                if (_id != 0) {
                    MySingleton g = MySingleton.getInstance();
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled())
                            editOrder(_id, true, null, null);
                    } else
                        editOrder(_id, true, null, null);
                }
                return true;
            }
            case R.id.action_copy_order_to_refund: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long _id = info.id;
                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, _id), new String[]{"order_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    _id = cursor.getLong(0);
                } else {
                    _id = 0;
                }
                cursor.close();
                if (_id != 0) {
                    MySingleton g = MySingleton.getInstance();
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled())
                            editRefund(0, false, _id, m_client_id, new MyID());
                    } else
                        editRefund(0, false, _id, m_client_id, new MyID());
                }
                return true;
            }
            case R.id.action_cancel_order: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_CANCEL_CURRENT_ORDER, bundle);
                return true;
            }
            case R.id.action_delete_order: {

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_DELETE_CURRENT_ORDER, bundle);
                return true;
            }
            case R.id.action_copy_refund: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long _id = info.id;
                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, _id), new String[]{"refund_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    _id = cursor.getLong(0);
                } else {
                    _id = 0;
                }
                if (_id != 0) {
                    MySingleton g = MySingleton.getInstance();
                    if (g.Common.PRODLIDER) {
                        if (checkGeoEnabled())
                            editRefund(_id, true, 0, m_client_id, new MyID());
                    } else
                        editRefund(_id, true, 0, m_client_id, new MyID());
                }
                return true;
            }
            case R.id.action_cancel_refund: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_CANCEL_CURRENT_REFUND, bundle);
                return true;
            }
            case R.id.action_delete_refund: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_DELETE_CURRENT_REFUND, bundle);
                return true;
            }

            case R.id.action_cancel_distribs: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_CANCEL_CURRENT_DISTRIBS, bundle);
                return true;
            }
            case R.id.action_delete_distribs: {

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_DELETE_CURRENT_DISTRIBS, bundle);
                return true;
            }
            case R.id.action_copy_payment: {
                MySingleton g = MySingleton.getInstance();
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                long _id = info.id;
                if (g.Common.PRODLIDER) {
                    if (checkGeoEnabled())
                        editPayment(_id, true, null, null);
                } else
                    editPayment(_id, true, null, null);
                return true;
            }
            case R.id.action_cancel_payment: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_CANCEL_CURRENT_PAYMENT, bundle);
                return true;
            }
            case R.id.action_delete_payment: {

                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(IDD_DELETE_CURRENT_PAYMENT, bundle);
                return true;
            }
            case R.id.action_delete_message: {
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Bundle bundle = new Bundle();
                bundle.putLong("_id", info.id);
                showDialog(MainActivity.IDD_DELETE_CURRENT_MESSAGE, bundle);
                return true;
            }

        }
        return super.onContextItemSelected(item);
    }

    public void createOrderPre(String client_id, String distr_point_id) {
        Intent intent = new Intent(this, OrderPreActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("client_id", client_id);
        intent.putExtra("distr_point_id", distr_point_id);
        //intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //startActivityForResult(intent, EDIT_ORDER_PRE_REQUEST);
        editOrderPreActivityResultLauncher.launch(intent);
    }

    public void editOrder(long id, boolean bCopyOrder, String client_id, String distr_point_id) {
        //Intent intent=new Intent(this, OrderActivity.class);
        //startActivityForResult(intent, EDIT_ORDER_REQUEST);
        Intent intent = new Intent(this, OrderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("client_id", client_id);
        intent.putExtra("distr_point_id", distr_point_id);
        //intent.putExtra("id", "");

        OrdersHelpers.PrepareData(intent, this, id, new MyID(client_id), new MyID(distr_point_id), bCopyOrder);

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_ORDER_REQUEST);
        editOrderActivityResultLauncher.launch(intent);
    }

    public void cancelOrder(long id)
    {
        MySingleton g = MySingleton.getInstance();
        String uid = "";
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, id);

        Cursor cursor = getContentResolver().query(singleUri, new String[]{"uid"}, null, null, null);
        if (cursor.moveToNext()) {
            int uid_Index = cursor.getColumnIndex("uid");
            uid = cursor.getString(uid_Index);
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("state", E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value());
        cv.put("closed_not_full", 0);
        getContentResolver().update(singleUri, cv, E_ORDER_STATE.getCanBeCanceledConditionWhere(), E_ORDER_STATE.getCanBeCanceledArgs());
        // Удаляем такую заявку из текстовых файлов
        if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!uid.isEmpty()) {
                // получаем путь к SD
                File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                File sdFile = new File(sdPath, uid + ".txt");
                sdFile.delete();
            }
        }

        // если режим маршрута, обновим документ
        updateDocumentInRoute(g.MyDatabase.m_order_editing.distr_point_id.toString(), g.MyDatabase.m_order_editing);

    }

    public void cancelRefund(long id)
    {
        MySingleton g = MySingleton.getInstance();
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.REFUNDS_CONTENT_URI, m_refund_id_to_process);

        ContentValues cv = new ContentValues();
        cv.put("state", E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value());
        cv.put("closed_not_full", 0);
        getContentResolver().update(singleUri, cv, E_REFUND_STATE.getCanBeCanceledConditionWhere(), E_REFUND_STATE.getCanBeCanceledArgs());

        // если режим маршрута, обновим документ
        updateDocumentInRoute(g.MyDatabase.m_refund_editing.distr_point_id.toString(), g.MyDatabase.m_refund_editing);
    }

    public void cancelDistribs(long id) {
        MySingleton g = MySingleton.getInstance();
        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTRIBS_CONTENT_URI, id);

        ContentValues cv = new ContentValues();
        cv.put("state", E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value());
        getContentResolver().update(singleUri, cv, E_DISTRIBS_STATE.getDistribsCanBeCanceledConditionWhere(), E_DISTRIBS_STATE.getDistribsCanBeCanceledArgs());

        // если режим маршрута, обновим документ
        updateDocumentInRoute(g.MyDatabase.m_distribs_editing.distr_point_id.toString(), g.MyDatabase.m_distribs_editing);

    }

    public void editPayment(long id, boolean bCopyPayment, MyID client_id, MyID distr_point_id) {
        MySingleton g = MySingleton.getInstance();
        boolean bCreatedPayment = false;

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.putExtra("id", "");
        CashPaymentRecord rec = g.MyDatabase.m_payment_editing;
        g.MyDatabase.m_payment_editing_id = id;
        g.MyDatabase.m_payment_editing_modified = false;
        Cursor cursor;
        boolean result = TextDatabase.ReadPaymentBy_Id(getContentResolver(), rec, g.MyDatabase.m_payment_editing_id);
        if (result == false) {
            bCreatedPayment = true;
            rec.client_id = new MyID();
            rec.stuff_client_name = getResources().getString(R.string.client_not_set);
            rec.stuff_client_address = "";
            rec.stuff_email="";
            rec.manager_id = new MyID();
            rec.stuff_manager_name = getResources().getString(R.string.manager_not_set);
            if (client_id != null && !client_id.isEmpty()) {
                rec.client_id = client_id.clone();
                // Прочитаем название контрагента и id куратора
                Cursor clientCursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "curator_id", "flags", "email_for_cheques"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.client_id.toString()}, null);
                if (clientCursor.moveToNext()) {
                    int descrIndex = clientCursor.getColumnIndex("descr");
                    int addressIndex = clientCursor.getColumnIndex("address");
                    int curator_idIndex = clientCursor.getColumnIndex("curator_id");
                    int email_for_chequesIndex = clientCursor.getColumnIndex("email_for_cheques");
                    //int flags_Index=clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex);
                    rec.stuff_client_address = clientCursor.getString(addressIndex);
                    rec.manager_id = new MyID(clientCursor.getString(curator_idIndex));
                    rec.stuff_email=clientCursor.getString(email_for_chequesIndex);
                    // Наименование менеджера
                    cursor = getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.manager_id.toString()}, null);
                    if (cursor.moveToNext()) {
                        int descrIndex0 = cursor.getColumnIndex("descr");
                        String newWord = cursor.getString(descrIndex0);
                        rec.stuff_manager_name = new String(newWord);
                    }
                    cursor.close();
                } else
                {
                    rec.stuff_client_name="{"+rec.client_id.toString()+"}";
                }
                clientCursor.close();
            }
            rec.distr_point_id=distr_point_id.clone();
            rec.comment = "";
            rec.stuff_debt = 0;
            rec.stuff_debt_past = 0;
            rec.stuff_debt_past30 = 0;
            rec.stuff_agreement_debt = 0.0;
            rec.stuff_agreement_debt_past = 0.0;
            rec.agreement_id = new MyID();
            rec.sumDoc = 0.0;
            rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set);
            rec.stuff_organization_name = getResources().getString(R.string.organization_not_set);
            rec.vicarious_power_id = new MyID();
            rec.vicarious_power_descr = getResources().getString(R.string.vicarious_power_not_set);
        } else {
            if (bCopyPayment) {
                bCreatedPayment = true;
            }
            // Наименование клиента
            cursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "flags", "email_for_cheques"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.client_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                int addressIndex = cursor.getColumnIndex("address");
                int flagsIndex = cursor.getColumnIndex("flags");
                int email_for_chequesIndex = cursor.getColumnIndex("email_for_cheques");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_client_name = new String(newWord);
                rec.stuff_client_address = new String(cursor.getString(addressIndex));
                rec.stuff_email = cursor.getString(email_for_chequesIndex);
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
                rec.stuff_email = "";
            }
            cursor.close();
            // Наименование договора
            MyID organizationId = new MyID();
            cursor = getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"descr", "sale_id", "organization_id"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.agreement_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                int organization_idIndex = cursor.getColumnIndex("organization_id");
                String descr = cursor.getString(descrIndex);
                rec.stuff_agreement_name = new String(descr);
                organizationId = new MyID(cursor.getString(organization_idIndex));
            } else {
                rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set);
            }
            cursor.close();
            // Наименование организации
            cursor = getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_organization_name = new String(newWord);
            } else {
                if (rec.stuff_organization_name==null||rec.stuff_organization_name.isEmpty()) {
                    rec.stuff_organization_name = getResources().getString(R.string.organization_not_set);
                }
            }
            cursor.close();
            // Наименование менеджера
            cursor = getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.manager_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_manager_name = new String(newWord);
            } else {
                if (rec.stuff_manager_name.isEmpty()) {
                    rec.stuff_manager_name = getResources().getString(R.string.manager_not_set);
                }
            }
            cursor.close();
        }
        if (bCreatedPayment) {
            // В том числе копированием
            rec.uid = UUID.randomUUID();
            rec.numdoc = getString(R.string.numdoc_new);// "НОВЫЙ"
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            //java.util.Date work_date=DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
            rec.datedoc = Common.getDateTimeAsString14(null);
            rec.id = new MyID(); // Это код, присвоенный 1С
            rec.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED;
            g.MyDatabase.m_payment_editing_id = 0; // Чтобы при записи не перезаписать уже существующий документ
            rec.version = 0;
            rec.version_ack = 0;
            rec.versionPDA = 0;
            rec.versionPDA_ack = 0;
            //
            rec.latitude = 0;
            rec.longitude = 0;
            rec.gpsaccuracy = 0.0;
            rec.gpsstate = -1;
            rec.accept_coord = 1;
        }
        // Долг контрагента
        Cursor cursorDebt = getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{rec.client_id.toString()}, null);
        if (cursorDebt.moveToNext()) {
            int indexDebt = cursorDebt.getColumnIndex("saldo");
            int indexDebtPast = cursorDebt.getColumnIndex("saldo_past");
            int indexDebtPast30 = cursorDebt.getColumnIndex("saldo_past30");
            rec.stuff_debt = cursorDebt.getDouble(indexDebt);
            rec.stuff_debt_past = cursorDebt.getDouble(indexDebtPast);
            rec.stuff_debt_past30 = cursorDebt.getDouble(indexDebtPast30);
        } else {
            rec.stuff_debt = 0.0;
            rec.stuff_debt_past = 0.0;
            rec.stuff_debt_past30 = 0.0;
        }
        cursorDebt.close();

        // Долг контрагента по договору
        if (g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) {
            double agreement_debt = 0;
            double agreement_debt_past = 0;
            Cursor cursorAgreementDebt = getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{rec.agreement_id.toString()}, null);
            int saldoIndex = cursorAgreementDebt.getColumnIndex("saldo");
            int saldoPastIndex = cursorAgreementDebt.getColumnIndex("saldo_past");
            while (cursorAgreementDebt.moveToNext()) {
                agreement_debt = agreement_debt + cursorAgreementDebt.getDouble(saldoIndex);
                agreement_debt_past = agreement_debt_past + cursorAgreementDebt.getDouble(saldoPastIndex);
            }
            cursorAgreementDebt.close();
            rec.stuff_agreement_debt = agreement_debt;
            rec.stuff_agreement_debt_past = agreement_debt_past;
        }

        //startActivityForResult(intent, EDIT_PAYMENT_REQUEST);
        editPaymentActivityResultLauncher.launch(intent);

    }


    public void editRefund(long id, boolean bCopyRefund, long order_id, MyID client_id, MyID distr_point_id) {
        MySingleton g = MySingleton.getInstance();
        boolean bCreatedRefund = false;
        Intent intent = new Intent(this, RefundActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        RefundRecord rec = g.MyDatabase.m_refund_editing;
        g.MyDatabase.m_refund_editing_id = id;
        g.MyDatabase.m_refund_editing_modified = false;
        Cursor cursor;
        boolean needReadDescrs = false;
        boolean result = TextDatabase.ReadRefundBy_Id(getContentResolver(), rec, g.MyDatabase.m_refund_editing_id);
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
            bCreatedRefund = true;
            rec.Clear();
            if (client_id == null || client_id.isEmpty()) {
                rec.client_id = new MyID();
                rec.stuff_client_name = getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
                rec.curator_id = new MyID();
                rec.stuff_curator_name = getResources().getString(R.string.curator_not_set);
                rec.stuff_select_account = false;
            } else {
                rec.client_id = client_id.clone();
                rec.curator_id = new MyID();
                // Прочитаем название контрагента и id куратора
                Cursor clientCursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "curator_id", "flags"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
                if (clientCursor != null && clientCursor.moveToNext()) {
                    int descrIndex = clientCursor.getColumnIndex("descr");
                    int addressIndex = clientCursor.getColumnIndex("address");
                    int curator_idIndex = clientCursor.getColumnIndex("curator_id");
                    int flags_Index = clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex);
                    rec.stuff_client_address = clientCursor.getString(addressIndex);
                    rec.curator_id = new MyID(clientCursor.getString(curator_idIndex));
                    rec.stuff_select_account = (clientCursor.getInt(flags_Index) & 2) != 0;
                }
            }
            if (distr_point_id == null || distr_point_id.isEmpty()) {
                rec.distr_point_id = new MyID();
                rec.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
            } else {
                rec.distr_point_id = distr_point_id.clone();
                // Наименование торговой точки
                cursor=getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.distr_point_id.toString()}, null);
                if (cursor.moveToNext())
                {
                    int descrIndex = cursor.getColumnIndex("descr");
                    String newWord = cursor.getString(descrIndex);
                    rec.stuff_distr_point_name=new String(newWord);
                } else
                {
                    rec.stuff_distr_point_name="{"+rec.distr_point_id+"}";
                }
                cursor.close();
            }
            rec.comment = "";
            rec.stuff_debt = 0;
            rec.stuff_debt_past = 0;
            rec.stuff_debt_past30 = 0;
            rec.agreement_id = new MyID();
            rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set);
            rec.stuff_organization_name = getResources().getString(R.string.organization_not_set);
            rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set);
            rec.stock_id = TextDatabase.getDefaultStock(getContentResolver(), rec.stuff_stock_descr);

            if (order_id != 0) {
                // на основании заказа
                OrderRecord order = new OrderRecord();
                boolean resultOrder = TextDatabase.ReadOrderBy_Id(getContentResolver(), order, order_id);
                // вообще здесь заказ всегда будет прочитан
                if (resultOrder) {
                    rec.client_id = order.client_id;
                    rec.stuff_client_name = order.stuff_client_name;
                    rec.stuff_client_address = order.stuff_client_address;
                    rec.curator_id = order.curator_id;
                    rec.stuff_curator_name = order.stuff_curator_name;
                    rec.stuff_select_account = order.stuff_select_account;
                    rec.agreement_id = order.agreement_id;
                    rec.stuff_agreement_name = order.stuff_agreement_name;
                    rec.stuff_organization_name = order.stuff_organization_name;
                    rec.stuff_distr_point_name = order.stuff_distr_point_name;
                    rec.stock_id = order.stock_id;

                    for (OrderLineRecord line : order.lines) {
                        RefundLineRecord refundLine = new RefundLineRecord();
                        refundLine.nomenclature_id = line.nomenclature_id;
                        refundLine.quantity_requested = line.quantity_requested;
                        refundLine.quantity = line.quantity_requested;
                        refundLine.k = line.k;
                        refundLine.ed = line.ed;
                        refundLine.stuff_nomenclature = line.stuff_nomenclature;
                        refundLine.stuff_weight_k_1 = line.stuff_weight_k_1;
                        refundLine.temp_quantity = line.temp_quantity;
                        refundLine.stuff_nomenclature_flags = line.stuff_nomenclature_flags;
                        refundLine.comment_in_line = "";
                        rec.lines.add(refundLine);
                    }

                    needReadDescrs = true;
                }
            }

        } else {
            if (bCopyRefund) {
                bCreatedRefund = true;
                // Проходим по строкам и выставляем начальное количество
                for (RefundLineRecord line : rec.lines) {
                    if (line.quantity_requested > line.quantity)
                        line.quantity = line.quantity_requested;
                }
            }
            if (bCopyRefund || rec.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
                // Заказ восстановлен, пересчитаем хотя бы вес
                // а для скопированных вес считали всегда
                rec.weightDoc = TextDatabase.GetRefundWeight(getContentResolver(), rec, null, false);
            }
            needReadDescrs = true;
        }
        // в случае, если введен копированием (с другого возврата или на основании заказа)
        if (needReadDescrs) {
            // Наименование клиента
            cursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "flags"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
            if (cursor != null && cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                int addressIndex = cursor.getColumnIndex("address");
                int flagsIndex = cursor.getColumnIndex("flags");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_client_name = new String(newWord);
                rec.stuff_client_address = new String(cursor.getString(addressIndex));
                rec.stuff_select_account = (cursor.getInt(flagsIndex) & 2) != 0;
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
                rec.stuff_select_account = false;
            }
            cursor.close();
            // Наименование договора
            cursor = getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"descr", "sale_id"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.agreement_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String descr = cursor.getString(descrIndex);
                rec.stuff_agreement_name = new String(descr);
            } else {
                rec.stuff_agreement_name = getResources().getString(R.string.agreement_not_set);
            }
            cursor.close();
            // Наименование организации
            cursor = getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.organization_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_organization_name = new String(newWord);
            } else {
                rec.stuff_organization_name = getResources().getString(R.string.organization_not_set);
            }
            cursor.close();
            // Наименование торговой точки
            cursor = getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_refund_editing.distr_point_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_distr_point_name = new String(newWord);
            } else {
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set);
            }
            cursor.close();
        }
        g.MyDatabase.m_refund_editing_created = bCreatedRefund;
        if (bCreatedRefund) {
            // В том числе копированием
            rec.uid = UUID.randomUUID();
            rec.numdoc = getString(R.string.numdoc_new);// "НОВЫЙ"

            java.util.Date date = new java.util.Date();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
            if (Common.MyDateFormat("yyyyMMdd", date).equals(Common.MyDateFormat("yyyyMMdd", work_date))) {
                // День совпал - берем текущее время
                rec.datedoc = Common.getDateTimeAsString14(date);
            } else {
                // Берем рабочую дату
                rec.datedoc = Common.getDateTimeAsString14(work_date);
            }
            rec.comment_closing = "";
            rec.id = new MyID(); // Это код, присвоенный 1С
            rec.state = E_REFUND_STATE.E_REFUND_STATE_CREATED;
            g.MyDatabase.m_refund_editing_id = 0; // Чтобы при записи не перезаписать уже существующий документ
            rec.dont_need_send = 0;
            rec.version = 0;
            rec.version_ack = 0;
            rec.versionPDA = 0;
            rec.versionPDA_ack = 0;

            if (!g.Common.NEW_BACKUP_FORMAT && g.Common.BACKUP_NOT_SAVED_ORDERS) {
                rec.state = E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED;
                g.MyDatabase.m_refund_editing_id = TextDatabase.SaveRefundSQL(getContentResolver(), g.MyDatabase.m_refund_editing, g.MyDatabase.m_refund_editing_id);
            }
            //
            rec.latitude = 0;
            rec.longitude = 0;
            rec.gpsaccuracy = 0.0;
            rec.gpsstate = -1;
            rec.accept_coord = 1;
        }
        // Долг контрагента
        cursor = getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
        if (cursor != null && cursor.moveToNext()) {
            int indexDebt = cursor.getColumnIndex("saldo");
            int indexDebtPast = cursor.getColumnIndex("saldo_past");
            int indexDebtPast30 = cursor.getColumnIndex("saldo_past30");
            g.MyDatabase.m_refund_editing.stuff_debt = cursor.getDouble(indexDebt);
            g.MyDatabase.m_refund_editing.stuff_debt_past = cursor.getDouble(indexDebtPast);
            g.MyDatabase.m_refund_editing.stuff_debt_past30 = cursor.getDouble(indexDebtPast30);
        } else {
            g.MyDatabase.m_refund_editing.stuff_debt = 0;
            g.MyDatabase.m_refund_editing.stuff_debt_past = 0;
            g.MyDatabase.m_refund_editing.stuff_debt_past30 = 0;
        }
        cursor.close();
        // Куратор
        cursor = getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.curator_id.toString()}, null);
        if (cursor != null && cursor.moveToNext()) {
            int indexDescr = cursor.getColumnIndex("descr");
            g.MyDatabase.m_refund_editing.stuff_curator_name = cursor.getString(indexDescr);
        } else {
            g.MyDatabase.m_refund_editing.stuff_curator_name = getResources().getString(R.string.curator_not_set);
        }
        cursor.close();

        // Список всех возможных типов учета
        ArrayList<String> list_accounts_descr = new ArrayList<String>();
        ArrayList<String> list_accounts_id = new ArrayList<String>();

        list_accounts_descr.add("УПР");
        list_accounts_id.add("0");
        list_accounts_descr.add("ОБЩ");
        list_accounts_id.add("1");
        if (g.Common.MEGA) {
            list_accounts_descr.add("Кега");
            list_accounts_id.add("2");
            list_accounts_descr.add("БПЗ");
            list_accounts_id.add("3");
        }

        // Склад
        int defaultStocksCount = 0;
        int defaultStock = -1;
        int defaultActionStocksCount = 0;
        int defaultActionStock = -1;
        int foundStock = -1;
        // Список всех возможных складов
        ArrayList<String> list_stocks_descr = new ArrayList<String>();
        ArrayList<String> list_stocks_id = new ArrayList<String>();
        cursor = getContentResolver().query(MTradeContentProvider.STOCKS_CONTENT_URI, new String[]{"descr", "id", "flags"}, null, null, "descr ASC");

        list_stocks_descr.add(getResources().getString(R.string.item_not_set));
        list_stocks_id.add(new MyID().toString());

        if (cursor != null) {
            int descrIndex = cursor.getColumnIndex("descr");
            int idIndex = cursor.getColumnIndex("id");
            int flagsIndex = cursor.getColumnIndex("flags");

            while (cursor.moveToNext()) {
                String stockDescr = cursor.getString(descrIndex);
                String stockId = cursor.getString(idIndex);
                int flags = cursor.getInt(flagsIndex);
                if (rec.stock_id.m_id.equals(stockId)) {
                    foundStock = list_stocks_descr.size();
                }
                // по умолчанию
                if ((flags & 1) != 0) {
                    if ((flags & 2) != 0) {
                        // по умолчанию акционный склад
                        defaultActionStocksCount++;
                        defaultActionStock = list_stocks_descr.size();
                    } else {
                        // по умолчанию обычный склад
                        defaultStocksCount++;
                        defaultStock = list_stocks_descr.size();
                    }
                }
                list_stocks_descr.add(stockDescr);
                list_stocks_id.add(stockId);
            }

            // Склад не найден
            if (foundStock <= 0) {
                if (defaultStocksCount == 1 && bCreatedRefund) {
                    // Установим этот склад по умолчанию
                    rec.stock_id = new MyID(list_stocks_id.get(defaultStock));
                    // что делать с акционными складами по умолчанию пока не понятно
                } else {
                    // Сбросим склад, если он не найден и это не новый документ
                    rec.stock_id = new MyID();
                }
            }
            cursor.close();
        }
        Bundle bundle = new Bundle();
        //bundle.putStringArray("list_organizations_descr",(String[])list_organizations_descr.toArray(new String[0]));
        //bundle.putStringArray("list_organizations_id",(String[])list_organizations_id.toArray(new String[0]));
        bundle.putStringArray("list_accounts_descr", (String[]) list_accounts_descr.toArray(new String[0]));
        bundle.putStringArray("list_accounts_id", (String[]) list_accounts_id.toArray(new String[0]));

        bundle.putStringArray("list_stocks_descr", (String[]) list_stocks_descr.toArray(new String[0]));
        bundle.putStringArray("list_stocks_id", (String[]) list_stocks_id.toArray(new String[0]));
        intent.putExtra("extrasBundle", bundle);

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_REFUND_REQUEST);
        editRefundActivityResultLauncher.launch(intent);
    }


    public void editDistribs(long id, boolean bCopyDistribs, MyID client_id, MyID distr_point_id) {
        MySingleton g = MySingleton.getInstance();
        boolean bCreatedDistribs = false;
        Intent intent = new Intent(this, DistribsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        DistribsRecord rec = g.MyDatabase.m_distribs_editing;
        g.MyDatabase.m_distribs_editing_id = id;
        g.MyDatabase.m_distribs_editing_modified = false;
        Cursor cursor;
        boolean needReadDescrs = false;
        boolean result = TextDatabase.ReadDistribsBy_Id(getContentResolver(), rec, g.MyDatabase.m_distribs_editing_id);
        if (result == false) {
            bCreatedDistribs = true;
            rec.Clear();
            if (client_id == null || client_id.isEmpty()) {
                rec.client_id = new MyID();
                rec.stuff_client_name = getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
                rec.curator_id = new MyID();
                rec.stuff_curator_name = getResources().getString(R.string.curator_not_set);
            } else {
                rec.client_id = client_id.clone();
                rec.curator_id = new MyID();
                // Прочитаем название контрагента и id куратора
                Cursor clientCursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "curator_id", "flags"}, "id=?", new String[]{g.MyDatabase.m_distribs_editing.client_id.toString()}, null);
                if (clientCursor != null && clientCursor.moveToNext()) {
                    int descrIndex = clientCursor.getColumnIndex("descr");
                    int addressIndex = clientCursor.getColumnIndex("address");
                    int curator_idIndex = clientCursor.getColumnIndex("curator_id");
                    //int flags_Index=clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex);
                    rec.stuff_client_address = clientCursor.getString(addressIndex);
                    rec.curator_id = new MyID(clientCursor.getString(curator_idIndex));
                }
            }
            if (distr_point_id == null || distr_point_id.isEmpty()) {
                rec.distr_point_id = new MyID();
                rec.stuff_distr_point_name=getResources().getString(R.string.trade_point_not_set);
            } else {
                rec.distr_point_id = distr_point_id.clone();
                // Наименование торговой точки
                cursor=getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.distr_point_id.toString()}, null);
                if (cursor.moveToNext())
                {
                    int descrIndex = cursor.getColumnIndex("descr");
                    String newWord = cursor.getString(descrIndex);
                    rec.stuff_distr_point_name=new String(newWord);
                } else
                {
                    rec.stuff_distr_point_name="{"+rec.distr_point_id+"}";
                }
                cursor.close();
            }
            rec.comment = "";
            rec.stuff_debt = 0;
            rec.stuff_debt_past = 0;
            rec.stuff_debt_past30 = 0;

            if (bCopyDistribs) {
                bCreatedDistribs = true;
                needReadDescrs = true;
            }
        } else {
            needReadDescrs = true;
        }
        // в случае, если введен копированием
        if (needReadDescrs) {
            // Наименование клиента
            cursor = getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "flags"}, "id=?", new String[]{g.MyDatabase.m_distribs_editing.client_id.toString()}, null);
            if (cursor != null && cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                int addressIndex = cursor.getColumnIndex("address");
                int flagsIndex = cursor.getColumnIndex("flags");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_client_name = new String(newWord);
                rec.stuff_client_address = new String(cursor.getString(addressIndex));
            } else {
                rec.stuff_client_name = getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
            }
            cursor.close();
            // Наименование торговой точки
            cursor = getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_distribs_editing.distr_point_id.toString()}, null);
            if (cursor.moveToNext()) {
                int descrIndex = cursor.getColumnIndex("descr");
                String newWord = cursor.getString(descrIndex);
                rec.stuff_distr_point_name = new String(newWord);
            } else {
                rec.stuff_distr_point_name = getResources().getString(R.string.trade_point_not_set);
            }
            cursor.close();
        }
        g.MyDatabase.m_distribs_editing_created = bCreatedDistribs;
        if (bCreatedDistribs) {
            // В том числе копированием
            rec.uid = UUID.randomUUID();
            rec.numdoc = getString(R.string.numdoc_new);// "НОВЫЙ"

            // Берем текущее время
            rec.datedoc = Common.getDateTimeAsString14(null);
            rec.id = new MyID(); // Это код, присвоенный 1С
            rec.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED;
            g.MyDatabase.m_distribs_editing_id = 0; // Чтобы при записи не перезаписать уже существующий документ
            rec.version = 0;
            rec.version_ack = 0;
            rec.versionPDA = 0;
            rec.versionPDA_ack = 0;

            rec.accept_coord = 1;

            // Добавим строки
            cursor = getContentResolver().query(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, new String[]{"id", "descr"}, null, null, "position");
            int indexId = cursor.getColumnIndex("id");
            int indexDescr = cursor.getColumnIndex("descr");
            while (cursor.moveToNext()) {
                DistribsLineRecord line = new DistribsLineRecord();
                line.distribs_contract_id = new MyID(cursor.getString(indexId));
                line.stuff_distribs_contract = cursor.getString(indexDescr);
                line.quantity = 0.0;
                rec.lines.add(line);
            }
            cursor.close();
        }
        // Долг контрагента
        cursor = getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{g.MyDatabase.m_refund_editing.client_id.toString()}, null);
        if (cursor != null && cursor.moveToNext()) {
            int indexDebt = cursor.getColumnIndex("saldo");
            int indexDebtPast = cursor.getColumnIndex("saldo_past");
            int indexDebtPast30 = cursor.getColumnIndex("saldo_past30");
            g.MyDatabase.m_distribs_editing.stuff_debt = cursor.getDouble(indexDebt);
            g.MyDatabase.m_distribs_editing.stuff_debt_past = cursor.getDouble(indexDebtPast);
            g.MyDatabase.m_distribs_editing.stuff_debt_past30 = cursor.getDouble(indexDebtPast30);
        } else {
            g.MyDatabase.m_distribs_editing.stuff_debt = 0;
            g.MyDatabase.m_distribs_editing.stuff_debt_past = 0;
            g.MyDatabase.m_distribs_editing.stuff_debt_past30 = 0;
        }
        cursor.close();
        // Куратор
        cursor = getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.curator_id.toString()}, null);
        if (cursor != null && cursor.moveToNext()) {
            int indexDescr = cursor.getColumnIndex("descr");
            g.MyDatabase.m_distribs_editing.stuff_curator_name = cursor.getString(indexDescr);
        } else {
            g.MyDatabase.m_distribs_editing.stuff_curator_name = getResources().getString(R.string.curator_not_set);
        }
        cursor.close();

        //Bundle bundle = new Bundle();
        //bundle.putStringArray("list_accounts_descr",(String[])list_accounts_descr.toArray(new String[0]));
        //bundle.putStringArray("list_accounts_id",(String[])list_accounts_id.toArray(new String[0]));

        //bundle.putStringArray("list_stocks_descr",(String[])list_stocks_descr.toArray(new String[0]));
        //bundle.putStringArray("list_stocks_id",(String[])list_stocks_id.toArray(new String[0]));
        //intent.putExtra("extrasBundle", bundle);

        //getArguments().getStringArray(ARGUMENT_)

        //startActivityForResult(intent, EDIT_DISTRIBS_REQUEST);
        editDistribsActivityResultLauncher.launch(intent);
    }


    // photo имеет смысл для id=0
    public void editMessage(long id, String photo) {
        MySingleton g = MySingleton.getInstance();
        //boolean bCreatedMessage=false;
        Intent intent = new Intent(this, MessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        MessageRecord rec = g.MyDatabase.m_message_editing;
        g.MyDatabase.m_message_editing_id = id;
        g.MyDatabase.m_message_editing_modified = false;

        if (id == 0) {
            Calendar c = Calendar.getInstance();
            java.util.Date date = c.getTime();
            c.set(Calendar.DAY_OF_MONTH, 1);
            java.util.Date bom = c.getTime();
            rec.uid = UUID.randomUUID();
            rec.ver = 0;
            rec.acknowledged = 4;
            rec.sender_id = new MyID();
            rec.receiver_id = new MyID();
            rec.datetime = Common.getDateTimeAsString14(date);
            rec.text = "";
            rec.type_idx = E_MESSAGE_TYPES.E_MESSAGE_TYPE_MESSAGE.value();
            rec.date1 = Common.MyDateFormat("yyyyMMdd", bom);
            rec.date2 = Common.MyDateFormat("yyyyMMdd", date);
            rec.client_id = new MyID();
            rec.nomenclature_id = new MyID();
            rec.report = "";
            rec.fname = photo;
            if (rec.fname == null)
                rec.fname = "";
            if (!rec.fname.isEmpty()) {
                rec.type_idx = E_MESSAGE_TYPES.E_MESSAGE_TYPE_PHOTO.value();
            }
            //startActivityForResult(intent, EDIT_MESSAGE_REQUEST);
            editMessageActivityResultLauncher.launch(intent);
        } else {
            boolean result = TextDatabase.ReadMessageBy_Id(getContentResolver(), g.MyDatabase, rec, g.MyDatabase.m_message_editing_id);
            if ((rec.acknowledged & (4 | 16)) == 0) {
                rec.acknowledged |= 16;
                ContentValues cv = new ContentValues();
                cv.put("acknowledged", rec.acknowledged);
                cv.put("ver", rec.ver); // версия не меняется
                getContentResolver().update(MTradeContentProvider.MESSAGES_CONTENT_URI, cv, "_id=?", new String[]{String.valueOf(g.MyDatabase.m_message_editing_id)});
            }
            if (result) {
                //startActivityForResult(intent, EDIT_MESSAGE_REQUEST);
                editMessageActivityResultLauncher.launch(intent);
            }
        }
    }


    private void readVersions() {
        MySingleton g = MySingleton.getInstance();
        g.MyDatabase.m_nomenclature_version = 0;
        g.MyDatabase.m_clients_version = 0;
        g.MyDatabase.m_agreements_version = 0;
        g.MyDatabase.m_stocks_version = 0;
        g.MyDatabase.m_rests_version = 0;
        g.MyDatabase.m_saldo_version = 0;
        g.MyDatabase.m_saldo_extended_version = 0;
        g.MyDatabase.m_prices_version = 0;
        g.MyDatabase.m_pricetypes_version = 0;
        g.MyDatabase.m_clients_price_version = 0;
        g.MyDatabase.m_curators_price_version = 0;
        g.MyDatabase.m_agents_version = 0;
        g.MyDatabase.m_curators_version = 0;
        g.MyDatabase.m_distr_points_version = 0;
        g.MyDatabase.m_organizations_version = 0;
        g.MyDatabase.m_sales_loaded_version = 0;
        g.MyDatabase.m_sales_loaded_versions = new HashMap<UUID, Integer>();
        g.MyDatabase.m_places_version = 0;
        g.MyDatabase.m_occupied_places_version = 0;
        g.MyDatabase.m_vicarious_power_version = 0;
        g.MyDatabase.m_distribs_contracts_version = 0;
        g.MyDatabase.m_equipment_version = -1;
        g.MyDatabase.m_equipment_rests_version = -1;
        g.MyDatabase.m_agreements30_version = 0;
        g.MyDatabase.m_prices_agreements30_version = 0;
        g.MyDatabase.m_routes_version = 0;
        g.MyDatabase.m_routes_dates_version = 0;

        g.MyDatabase.m_sent_count = 0;

        // TODO код-близнец в ExchangeThread
        Cursor cursor = getContentResolver().query(MTradeContentProvider.VERSIONS_CONTENT_URI, new String[]{"param", "ver"}, null, null, null);
        if (cursor != null) {
            int index_param = cursor.getColumnIndex("param");
            int index_ver = cursor.getColumnIndex("ver");
            while (cursor.moveToNext()) {
                String param = cursor.getString(index_param);
                if (param.equals("CLIENTS")) {
                    g.MyDatabase.m_clients_version = cursor.getInt(index_ver);
                } else if (param.equals("NOMENCLATURE")) {
                    g.MyDatabase.m_nomenclature_version = cursor.getInt(index_ver);
                } else if (param.equals("RESTS")) {
                    g.MyDatabase.m_rests_version = cursor.getInt(index_ver);
                } else if (param.equals("AGREEMENTS")) {
                    g.MyDatabase.m_agreements_version = cursor.getInt(index_ver);
                } else if (param.equals("AGREEMENTS30")) {
                    g.MyDatabase.m_agreements30_version = cursor.getInt(index_ver);
                } else if (param.equals("PRICES_AGREEMENTS30")) {
                    g.MyDatabase.m_prices_agreements30_version = cursor.getInt(index_ver);
                } else if (param.equals("SALDO")) {
                    g.MyDatabase.m_saldo_version = cursor.getInt(index_ver);
                } else if (param.equals("SALDO_EXT")) {
                    g.MyDatabase.m_saldo_extended_version = cursor.getInt(index_ver);
                } else if (param.equals("STOCKS")) {
                    g.MyDatabase.m_stocks_version = cursor.getInt(index_ver);
                } else if (param.equals("PRICES")) {
                    g.MyDatabase.m_prices_version = cursor.getInt(index_ver);
                } else if (param.equals("PRICETYPES")) {
                    g.MyDatabase.m_pricetypes_version = cursor.getInt(index_ver);
                } else if (param.equals("CLIENTS_PRICE")) {
                    g.MyDatabase.m_clients_price_version = cursor.getInt(index_ver);
                } else if (param.equals("CURATORS_PRICE")) {
                    g.MyDatabase.m_curators_price_version = cursor.getInt(index_ver);
                } else if (param.equals("SIMPLE_DISCOUNTS")) {
                    g.MyDatabase.m_simple_discounts_version = cursor.getInt(index_ver);
                } else if (param.equals("AGENTS")) {
                    g.MyDatabase.m_agents_version = cursor.getInt(index_ver);
                } else if (param.equals("CURATORS")) {
                    g.MyDatabase.m_curators_version = cursor.getInt(index_ver);
                } else if (param.equals("D_POINTS")) {
                    g.MyDatabase.m_distr_points_version = cursor.getInt(index_ver);
                } else if (param.equals("ORGANIZATIONS")) {
                    g.MyDatabase.m_organizations_version = cursor.getInt(index_ver);
                } else if (param.equals("SALES_LOADED")) {
                    g.MyDatabase.m_sales_loaded_version = cursor.getInt(index_ver);
                } else if (param.equals("PLACES")) {
                    g.MyDatabase.m_places_version = cursor.getInt(index_ver);
                } else if (param.equals("OCCUPIED_PLACES")) {
                    g.MyDatabase.m_occupied_places_version = cursor.getInt(index_ver);
                } else if (param.equals("VICARIOUS_POWER")) {
                    g.MyDatabase.m_vicarious_power_version = cursor.getInt(index_ver);
                } else if (param.equals("SENT_COUNT")) {
                    g.MyDatabase.m_sent_count = cursor.getInt(index_ver);
                } else if (param.equals("DISTRIBS_CONTRACTS")) {
                    g.MyDatabase.m_distribs_contracts_version = cursor.getInt(index_ver);
                } else if (param.equals("EQUIPMENT")) {
                    g.MyDatabase.m_equipment_version = cursor.getInt(index_ver);
                } else if (param.equals("EQUIPMENT_RESTS")) {
                    g.MyDatabase.m_equipment_rests_version = cursor.getInt(index_ver);
                } else if (param.equals("ROUTES")) {
                    g.MyDatabase.m_routes_version = cursor.getInt(index_ver);
                } else if (param.equals("ROUTES_DATES")) {
                    g.MyDatabase.m_routes_dates_version = cursor.getInt(index_ver);
                }
            }
            cursor.close();
        }
        Cursor cursorSales = getContentResolver().query(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, new String[]{"param", "ver"}, null, null, null);
        if (cursorSales != null) {
            int index_param = cursor.getColumnIndex("param");
            int index_ver = cursor.getColumnIndex("ver");
            while (cursor.moveToNext()) {
                String param = cursor.getString(index_param);
                if (param.length() == 36) {
                    UUID uuid = UUID.fromString(param);
                    g.MyDatabase.m_sales_loaded_versions.put(uuid, cursor.getInt(index_ver));
                }
            }
            cursorSales.close();
        }
    }


    private void setNegativeVersions() {
        // Установим отрицательные версии
        Cursor cursorVersions = getContentResolver().query(MTradeContentProvider.VERSIONS_CONTENT_URI, new String[]{"param", "ver"}, null, null, null);
        int index_param = cursorVersions.getColumnIndex("param");
        int index_ver = cursorVersions.getColumnIndex("ver");
        while (cursorVersions.moveToNext()) {
            //#define QUERY_VER(a) if ((a)>0) (a)=-(a);
            int ver = cursorVersions.getInt(index_ver);
            if (ver >= 0) {
                if (ver == 0)
                    ver = -1;
                else
                    ver = -ver;
                ContentValues cv = new ContentValues();
                cv.put("param", cursorVersions.getString(index_param));
                cv.put("ver", ver);
                getContentResolver().insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
            }
        }
        cursorVersions.close();
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

    @Override
    protected Dialog onCreateDialog(int id, Bundle bundle) {
        MySingleton g = MySingleton.getInstance();
        switch (id) {
		/*
		case IDD_RECEIVE_PROGRESS:
			progressDialog=new ProgressDialog(MainActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setTitle("Title");
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			//progressDialog.setIndeterminate(true);
			//thread = new ExchangeThread(progressHandler, getContentResolver(), m_myBase);
			//thread.start();
			exchangeTask = new ExchangeTask();
			exchangeTask.execute();
			return progressDialog;
			*/
            case IDD_REINDEX_CREATED: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Создана новая база данных! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_created_prevent_reindex);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                        //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                        //loadDataAfterStart();
                        new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX), "", "");
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }
            case IDD_REINDEX_UPGRADED: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // База данных обновлена! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_updated_prevent_reindex);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                        //getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null);
                        //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                        //loadDataAfterStart();
                        dialog.cancel();
                        new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_UPGRADE), "", "");
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }
            case IDD_REINDEX_UPGRADED66: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // База данных обновлена! Будет выполнена реиндексация
                builder.setMessage(R.string.message_database_updated_prevent_reindex);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_REINDEX_UPGRADE), "66", "");
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }


            case IDD_REINDEX_INCORRECT_CLOSED: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Предыдущий сеанс был некорректно завершен! Выполнить реиндексацию, поиск потерянных заказов и продолжить работу?
                builder.setMessage(R.string.message_query_reindex);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
                        //TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                        //loadDataAfterStart();
                        //recoverOrders(true);
                        dialog.cancel();
                        new ServiceOperationsTask().execute(Integer.toString(ServiceOperationsTask.SERVICE_TASK_STATE_RECOVER_ORDERS), "", "");
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_CANCEL_CURRENT_ORDER: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_cancel_current_order);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        cancelOrder(m_order_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();

            }

            case IDD_DELETE_CURRENT_ORDER: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_current_order);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteOrder(m_order_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_CANCEL_CURRENT_REFUND: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_cancel_current_refund);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        cancelRefund(m_refund_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DELETE_CURRENT_REFUND: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_current_refund);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteRefund(m_refund_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_CANCEL_CURRENT_DISTRIBS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_cancel_current_distribs);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        cancelDistribs(m_distribs_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DELETE_CURRENT_DISTRIBS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_current_distribs);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteDistribs(m_distribs_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

		/*
		case IDD_DELETE_CLOSED_REFUNDS:
		{
			AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
			builder.setMessage(R.string.message_delete_closed_refunds);
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {

		    		String conditionString=null;
		    		ArrayList<String> conditionArgs=new ArrayList<String>();

		    		conditionString=Common.combineConditions(conditionString, conditionArgs, "not "+E_REFUND_STATE.getNotClosedConditionWhere(), E_REFUND_STATE.getNotClosedSelectionArgs());

		    		switch (m_filter_date_type)
		    		{
		    		case 0:
		    			// все
		    			break;
		    		case 1:
		    			// дата
		    			conditionString=Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin+"Z"});
		    			break;
		    		case 2:
		    			// интервал
		    			conditionString=Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end+"Z"});
		    			break;
		    		}
			    	getContentResolver().delete(MTradeContentProvider.REFUNDS_CONTENT_URI, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]));
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			builder.setCancelable(false);
			return builder.create();
		}
		*/

            case IDD_DELETE_CLOSED_DOCUMENTS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                    builder.setMessage(R.string.message_delete_closed_documents);
                } else {
                    builder.setMessage(R.string.message_delete_closed_orders);
                }
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // TODO
                        final ProgressDialog dlg = new ProgressDialog(MainActivity.this);
                        dlg.setMessage("Deleting closed documents");
                        dlg.setIndeterminate(true);
                        dlg.setCancelable(false);
                        dlg.show();

                        final Thread t = new Thread() {
                            @Override
                            public void run() {

                                MySingleton g = MySingleton.getInstance();

                                // TODO медленная операция, сделать

                                // Заказы, они есть и удаляются во всех форматах
                                String conditionString = null;
                                ArrayList<String> conditionArgs = new ArrayList<String>();

                                conditionString = Common.combineConditions(conditionString, conditionArgs, "not " + E_ORDER_STATE.getNotClosedConditionWhere(), E_ORDER_STATE.getNotClosedSelectionArgs());
                                if (g.Common.PHARAOH && m_filter_type == 1) {
                                    // По дате обслуживания
                                    switch (m_filter_date_type) {
                                        case 0:
                                            // все
                                            break;
                                        case 1:
                                            // дата
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                            break;
                                        case 2:
                                            // интервал
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                            break;
                                    }
                                } else {
                                    // По дате документа
                                    switch (m_filter_date_type) {
                                        case 0:
                                            // все
                                            break;
                                        case 1:
                                            // дата
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                            break;
                                        case 2:
                                            // интервал
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                            break;
                                    }
                                }
                                // 13.09.2018 удаляем резервные копии документов, чтобы они потом не восстановились
                                if (g.Common.NEW_BACKUP_FORMAT) {
                                    // удалим резервные копии документов, а потом сами документы
                                    // такие копии могут создаваться, когда документ уже был создан и записан
                                    // потом его начали редактировать, и программа закрылась из-за сбоя
                                    // при старте такие документы не восстанавливаются (потому, что там были строки товаров в оригинальном документе)
                                    Cursor toDeleteBackupIds = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "editing_backup=0 and " + conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), null);
                                    while (toDeleteBackupIds.moveToNext()) {
                                        getContentResolver().delete(MTradeContentProvider.ORDERS_CONTENT_URI, "old_id=? and editing_backup<>0", new String[]{toDeleteBackupIds.getString(0)});
                                    }
                                    toDeleteBackupIds.close();
                                }
                                // а дальше обычный код, когда удаляются сами докенты
                                //getContentResolver().delete(MTradeContentProvider.ORDERS_CONTENT_URI, "not "+E_ORDER_STATE.getNotClosedConditionWhere(), E_ORDER_STATE.getNotClosedSelectionArgs());
                                getContentResolver().delete(MTradeContentProvider.ORDERS_CONTENT_URI, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]));

                                // Продлидер. Удаляются не только заказы, но также другие виды документов
                                if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                                    // Возвраты
                                    conditionString = null;
                                    conditionArgs = new ArrayList<String>();

                                    conditionString = Common.combineConditions(conditionString, conditionArgs, "not " + E_REFUND_STATE.getNotClosedConditionWhere(), E_REFUND_STATE.getNotClosedSelectionArgs());

                                    switch (m_filter_date_type) {
                                        case 0:
                                            // все
                                            break;
                                        case 1:
                                            // дата
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                            break;
                                        case 2:
                                            // интервал
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                            break;
                                    }
                                    getContentResolver().delete(MTradeContentProvider.REFUNDS_CONTENT_URI, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]));

                                    // Дистрибьюции
                                    conditionString = null;
                                    conditionArgs = new ArrayList<String>();

                                    conditionString = Common.combineConditions(conditionString, conditionArgs, "not " + E_DISTRIBS_STATE.getDistribsNotClosedConditionWhere(), E_DISTRIBS_STATE.getDistribsNotClosedSelectionArgs());

                                    switch (m_filter_date_type) {
                                        case 0:
                                            // все
                                            break;
                                        case 1:
                                            // дата
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                            break;
                                        case 2:
                                            // интервал
                                            conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                            break;
                                    }
                                    getContentResolver().delete(MTradeContentProvider.DISTRIBS_CONTENT_URI, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]));

                                }
                                Common.dismissWithExceptionHandling(dlg);
                            }
                        };
                        t.start();

                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_SORT_ORDERS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.message_sort_orders);
                String[] sort_types_array;
                if (g.Common.PHARAOH) {
                    sort_types_array = getResources().getStringArray(R.array.sort_types_array_ph);
                } else {
                    sort_types_array = getResources().getStringArray(R.array.sort_types_array);
                }
                builder.setItems(sort_types_array, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                ContentValues cv = new ContentValues();
                                cv.put("sort_type", "orders_by_date");
                                getContentResolver().insert(MTradeContentProvider.SORT_CONTENT_URI, cv);
                                break;
                            }
                            case 1: {
                                ContentValues cv = new ContentValues();
                                cv.put("sort_type", "orders_by_service_date");
                                getContentResolver().insert(MTradeContentProvider.SORT_CONTENT_URI, cv);
                                break;
                            }
                        }

                    }
                });
                return builder.create();
            }


            case IDD_DELETE_CURRENT_PAYMENT: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_current_payment);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deletePayment(m_payment_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DELETE_CLOSED_PAYMENTS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_closed_payments);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        getContentResolver().delete(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, "not " + E_PAYMENT_STATE.getPaymentNotClosedConditionWhere(), E_PAYMENT_STATE.getPaymentNotClosedSelectionArgs());
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DELETE_CURRENT_MESSAGE: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_current_message);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        deleteMessage(m_message_id_to_process);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DELETE_READED_MESSAGES: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_delete_readed_messages);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ContentValues cv = new ContentValues();
                        cv.put("isMark", 1);
                        // 4 - это наше сообщение
                        // 16- прочитано
                        getContentResolver().update(MTradeContentProvider.MESSAGES_CONTENT_URI, cv, "(acknowledged&20)=16", null);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_QUERY_SEND_IMAGES: {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.message_send_images);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        m_bExcludeImageMessages = false;
                        new ExchangeTask().execute(1, 0, "");
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        m_bExcludeImageMessages = true;
                        new ExchangeTask().execute(1, 0, "");
                        //dialog.cancel();
                    }
                });
                builder.setCancelable(false);
                return builder.create();
            }

            case IDD_DATA_FILTER: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle("Custom dialog");
                builder.setTitle(R.string.title_filter);
                // создаем view из dialog.xml
                //LinearLayout view = (LinearLayout) getLayoutInflater()
                //    .inflate(R.layout.filter, null);
                final ScrollView view = (ScrollView) getLayoutInflater().inflate(R.layout.filter, null);
                // устанавливаем ее, как содержимое тела диалога
                builder.setView(view);

                // Spinner тип отбора (время документа или время доставки)
                Spinner spinnerFilterType = (Spinner) view.findViewById(R.id.spinnerFilterType);
                String[] dataSpinnerFilterType = getResources().getStringArray(R.array.documents_period_type);

                ArrayAdapter<String> dataAdapterFilterType = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataSpinnerFilterType);


                dataAdapterFilterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterType.setAdapter(dataAdapterFilterType);

                if (m_filter_type < 0 || m_filter_type >= dataSpinnerFilterType.length)
                    spinnerFilterType.setSelection(0);
                else
                    spinnerFilterType.setSelection(m_filter_type);

                spinnerFilterType.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                        m_filter_type_current = index;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

                // Spinner время доставки
                Spinner spinnerFilterPeriod = (Spinner) view.findViewById(R.id.spinnerFilterPeriod);
                String[] dataSpinnerFilterPeriod = getResources().getStringArray(R.array.filter_time_type);

                ArrayAdapter<String> dataAdapterShippingTime = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataSpinnerFilterPeriod);

                if (m_filter_date_begin_current.isEmpty()) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
                    m_filter_date_begin_current = Common.MyDateFormat("yyyyMMdd", work_date);
                }
                if (m_filter_date_end_current.isEmpty()) {
                    m_filter_date_end_current = m_filter_date_begin_current;
                }

                if (m_filter_date_begin_current.length() >= 8) {
                    EditText etFirstDate = (EditText) view.findViewById(R.id.etFirstDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_filter_date_begin_current.substring(6, 8) + "." + m_filter_date_begin_current.substring(4, 6) + "." + m_filter_date_begin_current.substring(0, 4));
                    etFirstDate.setText(sb.toString());
                }

                if (m_filter_date_end_current.length() >= 8) {
                    EditText etSecondDate = (EditText) view.findViewById(R.id.etSecondDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_filter_date_end_current.substring(6, 8) + "." + m_filter_date_end_current.substring(4, 6) + "." + m_filter_date_end_current.substring(0, 4));
                    etSecondDate.setText(sb.toString());
                }

                dataAdapterShippingTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerFilterPeriod.setAdapter(dataAdapterShippingTime);

                if (m_filter_date_type < 0 || m_filter_date_type >= dataSpinnerFilterPeriod.length)
                    spinnerFilterPeriod.setSelection(0);
                else
                    spinnerFilterPeriod.setSelection(m_filter_date_type);

                spinnerFilterPeriod.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                        if (m_filter_date_type_current != index) {
                            if (m_filter_date_type_current == 1) {
                                m_filter_date_end_current = m_filter_date_begin_current;
                            }
                            if (m_filter_date_type_current == 0) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
                                m_filter_date_begin_current = Common.MyDateFormat("yyyyMMdd", work_date);
                                m_filter_date_end_current = m_filter_date_begin_current;
                            }

                            if (m_filter_date_begin_current.length() >= 8) {
                                EditText etFirstDate = (EditText) view.findViewById(R.id.etFirstDate);
                                StringBuilder sb = new StringBuilder();
                                sb.append(m_filter_date_begin_current.substring(6, 8) + "." + m_filter_date_begin_current.substring(4, 6) + "." + m_filter_date_begin_current.substring(0, 4));
                                etFirstDate.setText(sb.toString());
                            }

                            if (m_filter_date_end_current.length() >= 8) {
                                EditText etSecondDate = (EditText) view.findViewById(R.id.etSecondDate);
                                StringBuilder sb = new StringBuilder();
                                sb.append(m_filter_date_end_current.substring(6, 8) + "." + m_filter_date_end_current.substring(4, 6) + "." + m_filter_date_end_current.substring(0, 4));
                                etSecondDate.setText(sb.toString());
                            }

                            m_filter_date_type_current = index;
                            LinearLayout linearLayoutStartDate = (LinearLayout) view.findViewById(R.id.linearLayoutStartDate);
                            LinearLayout linearLayoutEndDate = (LinearLayout) view.findViewById(R.id.linearLayoutEndDate);

                            switch (m_filter_date_type_current) {
                                case 0:
                                    linearLayoutStartDate.setVisibility(View.GONE);
                                    linearLayoutEndDate.setVisibility(View.GONE);
                                    break;
                                case 1:
                                    linearLayoutStartDate.setVisibility(View.VISIBLE);
                                    linearLayoutEndDate.setVisibility(View.GONE);
                                    break;
                                case 2:
                                    linearLayoutStartDate.setVisibility(View.VISIBLE);
                                    linearLayoutEndDate.setVisibility(View.VISIBLE);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

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

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        CheckBox checkBoxOrders = (CheckBox) view.findViewById(R.id.checkBoxOrders);
                        m_filter_orders = checkBoxOrders.isChecked();
                        CheckBox checkBoxRefunds = (CheckBox) view.findViewById(R.id.checkBoxRefunds);
                        m_filter_refunds = checkBoxRefunds.isChecked();
                        CheckBox checkBoxDistribs = (CheckBox) view.findViewById(R.id.checkBoxDistribs);
                        m_filter_distribs = checkBoxDistribs.isChecked();
                        //Spinner spinnerFilterPeriod=(Spinner)view.findViewById(R.id.spinnerFilterPeriod);
                        m_filter_type = m_filter_type_current;
                        m_filter_date_type = m_filter_date_type_current;//spinnerFilterPeriod.getSelectedItemPosition();

                        m_filter_date_begin = m_filter_date_begin_current;
                        m_filter_date_end = m_filter_date_end_current;

                        // TODO 22.01.2019
                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);

                        if (fragment instanceof OrdersListFragment)
                        {
                            OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                            ordersListFragment.onFilterChanged(m_filter_orders, m_filter_refunds, m_filter_distribs,
                                    m_filter_date_begin, m_filter_date_end, m_filter_type, m_filter_date_type);
                            invalidateOptionsMenu();
                        }

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
                        m_filter_orders = true;
                        m_filter_refunds = true;
                        m_filter_distribs = true;
                        m_filter_type = 0;
                        m_filter_date_type = 0;
                        m_filter_date_begin_current = m_filter_date_begin;
                        m_filter_date_end_current = m_filter_date_end;
                        // TODO все такие переделать на вызов метода фрагмента
                        // TODO 22.01.2019
                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);

                        if (fragment instanceof OrdersListFragment)
                        {
                            OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                            ordersListFragment.onFilterChanged(m_filter_orders, m_filter_refunds, m_filter_distribs,
                                    m_filter_date_begin, m_filter_date_end, m_filter_type, m_filter_date_type);
                            invalidateOptionsMenu();
                        }

                    }
                });
                m_filter_dialog = builder.create();
                return m_filter_dialog;
            }

            case IDD_QUERY_DOCUMENTS_PERIOD: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_query_documents);
                final ScrollView view = (ScrollView) getLayoutInflater().inflate(R.layout.query_documents, null);
                builder.setView(view);

                // Spinner тип периода
                Spinner spinnerPeriodType = (Spinner) view.findViewById(R.id.spinnerPeriodType);
                String[] dataSpinnerPeriodType = getResources().getStringArray(R.array.documents_period_type);

                ArrayAdapter<String> dataAdapterPeriodType = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, dataSpinnerPeriodType);

                // Запрашиваемый период возьмем из отбора, если он установлен
                switch (m_filter_date_type_current) {
                    case 0: {
                        // без отбора
                        // это будет установлено по умолчанию
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                        java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
                        m_query_date_begin_current = Common.MyDateFormat("yyyyMMdd", work_date);
                        m_query_date_end_current = m_query_date_begin_current;
                        break;
                    }
                    case 1:
                        // дата
                        m_query_date_begin_current = m_filter_date_begin;
                        m_query_date_end_current = m_filter_date_begin;
                        break;
                    case 2:
                        // интервал
                        m_query_date_begin_current = m_filter_date_begin;
                        m_query_date_end_current = m_filter_date_end;
                        break;
                }

                EditText etFirstDate = (EditText) view.findViewById(R.id.etFirstDate);
                EditText etSecondDate = (EditText) view.findViewById(R.id.etSecondDate);

                if (m_query_date_begin_current.length() >= 8) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_query_date_begin_current.substring(6, 8)).append(".").append(m_query_date_begin_current.substring(4, 6)).append(".").append(m_query_date_begin_current.substring(0, 4));
                    etFirstDate.setText(sb.toString());
                }

                if (m_query_date_end_current.length() >= 8) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_query_date_end_current.substring(6, 8)).append(".").append(m_query_date_end_current.substring(4, 6)).append(".").append(m_query_date_end_current.substring(0, 4));
                    etSecondDate.setText(sb.toString());
                }

                dataAdapterPeriodType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPeriodType.setAdapter(dataAdapterPeriodType);

                if (m_query_period_type < 0 || m_query_period_type >= dataSpinnerPeriodType.length) {
                    spinnerPeriodType.setSelection(0);
                    m_query_period_type_current = 0;
                } else {
                    spinnerPeriodType.setSelection(m_query_period_type);
                    m_query_period_type_current = m_query_period_type;
                }

                spinnerPeriodType.setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int index, long arg3) {
                        m_query_period_type_current = index;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //CheckBox checkBoxOrders=(CheckBox)view.findViewById(R.id.checkBoxOrders);
                        //m_filter_orders=checkBoxOrders.isChecked();
                        //CheckBox checkBoxRefunds=(CheckBox)view.findViewById(R.id.checkBoxRefunds);
                        //m_filter_refunds=checkBoxRefunds.isChecked();
                        //m_filter_date_type=m_filter_date_type_current;//spinnerFilterPeriod.getSelectedItemPosition();

                        m_query_period_type = m_query_period_type_current;

                        m_query_date_begin = m_query_date_begin_current;
                        m_query_date_end = m_query_date_end_current;

                        new ExchangeTask().execute(4, 0, null);

                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);

                        // TODO Хотя наверное здесь это не нужно
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);

                        if (fragment instanceof OrdersListFragment)
                        {
                            OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                            ordersListFragment.onFilterChanged(m_filter_orders, m_filter_refunds, m_filter_distribs,
                                    m_filter_date_begin, m_filter_date_end, m_filter_type, m_filter_date_type);
                            invalidateOptionsMenu();
                        }

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                m_query_documents_dialog = builder.create();
                return m_query_documents_dialog;
            }

            case IDD_FILTER_DATE_BEGIN: {
                int day = Integer.parseInt(m_filter_date_begin_current.substring(6, 8));
                int month = Integer.parseInt(m_filter_date_begin_current.substring(4, 6)) - 1;
                int year = Integer.parseInt(m_filter_date_begin_current.substring(0, 4));

                DatePickerDialog tpd = new DatePickerDialog(this, new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view0, int selectedYear, int selectedMonth,
                                          int selectedDay) {

                        m_filter_date_begin_current = Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                        EditText etDate = (EditText) m_filter_dialog.findViewById(R.id.etFirstDate);
                        StringBuilder sb = new StringBuilder();
                        sb.append(m_filter_date_begin_current.substring(6, 8) + "." + m_filter_date_begin_current.substring(4, 6) + "." + m_filter_date_begin_current.substring(0, 4));
                        etDate.setText(sb.toString());

                    }
                }, year, month, day);
                return tpd;
            }
            case IDD_FILTER_DATE_END: {
                int day = Integer.parseInt(m_filter_date_end_current.substring(6, 8));
                int month = Integer.parseInt(m_filter_date_end_current.substring(4, 6)) - 1;
                int year = Integer.parseInt(m_filter_date_end_current.substring(0, 4));

                DatePickerDialog tpd = new DatePickerDialog(this, new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view0, int selectedYear, int selectedMonth,
                                          int selectedDay) {

                        m_filter_date_end_current = Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                        EditText etDate = (EditText) m_filter_dialog.findViewById(R.id.etSecondDate);
                        StringBuilder sb = new StringBuilder();
                        sb.append(m_filter_date_end_current.substring(6, 8) + "." + m_filter_date_end_current.substring(4, 6) + "." + m_filter_date_end_current.substring(0, 4));
                        etDate.setText(sb.toString());

                    }
                }, year, month, day);
                return tpd;
            }
            case IDD_QUERY_DOCUMENTS_DATE_BEGIN: {
                int day = Integer.parseInt(m_query_date_begin_current.substring(6, 8));
                int month = Integer.parseInt(m_query_date_begin_current.substring(4, 6)) - 1;
                int year = Integer.parseInt(m_query_date_begin_current.substring(0, 4));

                DatePickerDialog tpd = new DatePickerDialog(this, new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view0, int selectedYear, int selectedMonth,
                                          int selectedDay) {

                        m_query_date_begin_current = Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                        EditText etDate = (EditText) m_query_documents_dialog.findViewById(R.id.etFirstDate);
                        StringBuilder sb = new StringBuilder();
                        sb.append(m_query_date_begin_current.substring(6, 8) + "." + m_query_date_begin_current.substring(4, 6) + "." + m_query_date_begin_current.substring(0, 4));
                        etDate.setText(sb.toString());

                    }
                }, year, month, day);
                return tpd;
            }
            case IDD_QUERY_DOCUMENTS_DATE_END: {
                int day = Integer.parseInt(m_query_date_end_current.substring(6, 8));
                int month = Integer.parseInt(m_query_date_end_current.substring(4, 6)) - 1;
                int year = Integer.parseInt(m_query_date_end_current.substring(0, 4));

                DatePickerDialog tpd = new DatePickerDialog(this, new OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view0, int selectedYear, int selectedMonth,
                                          int selectedDay) {

                        m_query_date_end_current = Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));

                        EditText etDate = (EditText) m_query_documents_dialog.findViewById(R.id.etSecondDate);
                        StringBuilder sb = new StringBuilder();
                        sb.append(m_query_date_end_current.substring(6, 8) + "." + m_query_date_end_current.substring(4, 6) + "." + m_query_date_end_current.substring(0, 4));
                        etDate.setText(sb.toString());

                    }
                }, year, month, day);
                return tpd;
            }
            case IDD_COULD_NOT_START_BUT_PERMISSIONS: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                //builder.setTitle();
                builder.setMessage(R.string.error_required_permissions_expected);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        MainActivity.this.finish();
                    }
                });
                return builder.create();
            }

            case IDD_NEED_GPS_ENABLED:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.need_gps_permissions);
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                return builder.create();
            }

            case IDD_QUERY_START_VISIT:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.query_start_visit);
                builder.setCancelable(false);
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
                return builder.create();
            }

            case IDD_QUERY_END_VISIT:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.query_end_visit);
                builder.setCancelable(false);
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
                return builder.create();
            }

            case IDD_QUERY_CANCEL_VISIT:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.query_cancel_visit);
                builder.setCancelable(false);
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
                return builder.create();
            }


            case IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.select_client_to_create_order)); // потом заменится не требуемый вид документа
                builder.setCancelable(false);

                final ArrayAdapter<MyElementOfArrayList> arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.select_dialog_singlechoice);

                builder.setSingleChoiceItems(arrayAdapter, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        //String strName = arrayAdapter.getItem(which).descr;
                    }
                });

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
                return builder.create();
            }

            //AlertDialog dialog=new AlertDialog(getApplicationContext());
            default:
                return null;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*
        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION.VALUE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted(PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionDenied(PERMISSION_REQUEST_FINE_LOCATION);
            }
        }
        */
        boolean bFineLocationGranted = false;
        boolean bCoarseLocationGranted = false;
        boolean bCameraGranted = false;
        boolean bStorageGranted = false;
        int i;
        for (i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION))
                    bFineLocationGranted = true;
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                    bCoarseLocationGranted = true;
                if (permissions[i].equals(Manifest.permission.CAMERA))
                    bCameraGranted = true;
                if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    bStorageGranted = true;
            }
        }

        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS) {
            if (bFineLocationGranted) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                String provider = m_locationManager.getBestProvider(criteria, true);
                if (provider != null) {
                    try {
                        m_locationManager.requestLocationUpdates(provider, 0, 0, locListener);
                    } catch (SecurityException e) {
                    }
                }
            } else if (bCoarseLocationGranted) {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = m_locationManager.getBestProvider(criteria, true);
                if (provider != null) {
                    try {
                        m_locationManager.requestLocationUpdates(provider, 0, 0, locListener);
                    } catch (SecurityException e) {
                    }
                }
            } else {
                // Отказано в доступе, запишем дату отказа
                MySingleton g = MySingleton.getInstance();

                ContentValues cv = new ContentValues();
                cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION);
                cv.put("datetime", Common.getDateTimeAsString14(null));
                cv.put("seance_incoming", g.MyDatabase.m_seance.toString());
                getContentResolver().insert(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, cv);

            }
        }
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (bCameraGranted) {
                doActionCreatePhoto();
            }
        }

        if (requestCode == PERMISSION_REQUEST_CAMERA_FOR_ORDER) {
            if (bCameraGranted) {
                if (mParamsToCreateDocsWhenRequestPermissions!=null)
                {
                    createOrderPre(mParamsToCreateDocsWhenRequestPermissions.client_id, mParamsToCreateDocsWhenRequestPermissions.distr_point_id);
                }
            }
        }

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (bStorageGranted) {
                Bundle extras = getIntent().getExtras();
                onReady(extras);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Пользователь установил пометку "Не спрашивать"
                    showDialog(IDD_COULD_NOT_START_BUT_PERMISSIONS);
                } else {
                    Snackbar.make(findViewById(android.R.id.content), R.string.error_no_permissions_to_sd, Snackbar.LENGTH_LONG).show();
                    //toast.setText();
                    //toast.getView().setBackgroundColor(ContextCompat.getColor(context, R.color.background_toast));
                    //toast.setGravity(Gravity.CENTER, 0, 0);
                    //toast.setDuration(Toast.LENGTH_LONG);
                    //toast.show();

                    finish();
                }
            }
        }

    }

    void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //ActivityCompat.requestPermissions(this, new String[]{permission.toStringValue()}, permission.ordinal());
                MySingleton g = MySingleton.getInstance();
                Cursor permissionsCursor = getContentResolver().query(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, new String[]{"datetime", "seance_incoming"}, "permission_name=?", new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, null);
                if (permissionsCursor.moveToFirst()) {
                    int seanceIncomingIndex = permissionsCursor.getColumnIndex("seance_incoming");
                    String seanceIncoming = permissionsCursor.getString(seanceIncomingIndex);
                    if (seanceIncoming.equals(g.MyDatabase.m_seance.toString())) {
                        // в этом сеансе уже спрашивали
                        return;
                    }
                }
                // Записываем время, когда запросили разрешения
                // (чтобы 2 раза не спрашивать)
                ContentValues cv = new ContentValues();
                cv.put("permission_name", Manifest.permission.ACCESS_FINE_LOCATION);
                cv.put("datetime", Common.getDateTimeAsString14(null));
                cv.put("seance_incoming", g.MyDatabase.m_seance.toString());
                getContentResolver().insert(MTradeContentProvider.PERMISSIONS_REQUESTS_CONTENT_URI, cv);

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION_ALL_ORDERS);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                String provider = m_locationManager.getBestProvider(criteria, true);
                if (provider != null) {
                    m_locationManager.requestLocationUpdates(provider, 0, 0, locListener);
                }
            }
        } else {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = m_locationManager.getBestProvider(criteria, true);
            if (provider != null) {
                m_locationManager.requestLocationUpdates(provider, 0, 0, locListener);
            }
        }
    }

    @Override
    public void onUniversalEventListener(String event, Bundle parameters) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);

        if (event.equals("ExchangeConnect"))
        {

            //checkFirebaseToken();
            checkSendImages();
            if (m_imagesToSendSize > 0) {
                showDialog(IDD_QUERY_SEND_IMAGES, null);
            } else {
                m_bExcludeImageMessages = false;
                new MainActivity.ExchangeTask().execute(1, 0, "");
            }
        }

        if (event.equals("ExchangeRefresh"))
        {
            MySingleton g = MySingleton.getInstance();

            if (g.Common.PHARAOH) {
                g.MyDatabase.m_occupied_places_version = -1;
                new MainActivity.ExchangeTask().execute(3, 0, null);
            } else {
                // Установим отрицательные версии
                setNegativeVersions();
                //Toast.makeText(MainActivity.this,  getResources().getString(R.string.message_query_all_warning), Toast.LENGTH_LONG).show();

                m_bRefreshPressed = true;

                // И обычный обмен
                //checkFirebaseToken();
                checkSendImages();
                if (m_imagesToSendSize > 0) {
                    showDialog(IDD_QUERY_SEND_IMAGES, null);
                } else {
                    m_bExcludeImageMessages = false;
                    new MainActivity.ExchangeTask().execute(1, 0, "");
                }
            }
        }

        if (event.equals("ExchangeQueryDocs")) {
            showDialog(IDD_QUERY_DOCUMENTS_PERIOD, null);
        }

        if (event.equals("ExchangeReceive")) {
            new MainActivity.ExchangeTask().execute(0, 0, "");
        }

        if (event.equals("ExchangeNomenclaturePhotos")) {
            new MainActivity.ExchangeTask().execute(2, 0, "");
        }

        if (event.equals("ExchangeWebService")) {
            new MainActivity.ExchangeTask().execute(3, 0, null);
        }

        // Указали клиента, либо стерли
        if (event.equals("FilterClient"))
        {
            String client_id=parameters.getString("client_id");
            if (client_id!=null)
                m_client_id=new MyID(client_id);
            else
                m_client_id=new MyID();
            m_client_descr=parameters.getString("client_descr");
            if (fragment instanceof OrdersListFragment)
            {
                OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                ordersListFragment.onFilterClientSelected(client_id, m_client_descr);
            }
            if (fragment instanceof PaymentsListFragment)
            {
                PaymentsListFragment paymentsListFragment=(PaymentsListFragment)fragment;
                paymentsListFragment.onFilterClientSelected(client_id, m_client_descr);
            }
        }

        if (event.equals("createOrder"))
        {
            //String client_id=parameters.getString("client_id");
            String distr_point_id=parameters!=null?parameters.getString("distr_point_id"):null;
            if (distr_point_id==null)
            {
                // Создание документа из журнала
                createOrder(m_client_id==null?null:m_client_id.toString(), null);
            } else {
                // Создание документа из маршрута
                HashMap<String, String> clients_ids = TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id);
                switch (clients_ids.size()) {
                    case 0:
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_clients_for_trade_point), Snackbar.LENGTH_LONG).show();
                        break;
                    case 1: {
                        Map.Entry<String, String> entry = clients_ids.entrySet().iterator().next();
                        String client_id = entry.getKey();
                        createOrder(client_id, distr_point_id);
                        break;
                    }
                    default:
                        parameters = new Bundle();
                        parameters.putSerializable("clients_array", clients_ids);
                        parameters.putString("distr_point_id", distr_point_id);
                        parameters.putString("doc_type", "order");
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters);

                }
            }

        }

        if (event.equals("editOrder"))
        {
            editOrder(parameters.getLong("id"), parameters.getBoolean("copy_order"), parameters.getString("client_id"), parameters.getString("distr_point_id"));
        }

        if (event.equals("cancelOrder"))
        {
            cancelOrder(parameters.getLong("id"));
        }

        if (event.equals("deleteOrder"))
        {
            deleteOrder(parameters.getLong("id"));
        }

        if (event.equals("createRefund"))
        {
            String distr_point_id=parameters.getString("distr_point_id");
            if (distr_point_id==null)
            {
                // Создание документа из журнала
                // (не уверен, что это используется, скоррее всего там editRefund всегда)
                editRefund(0, false, 0, m_client_id, null);
            } else {
                // Создание документа из маршрута
                HashMap<String, String> clients_ids = TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id);
                switch (clients_ids.size()) {
                    case 0:
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_clients_for_trade_point), Snackbar.LENGTH_LONG).show();
                        break;
                    case 1: {
                        Map.Entry<String, String> entry = clients_ids.entrySet().iterator().next();
                        String client_id = entry.getKey();
                        editRefund(0, false, 0, new MyID(client_id), new MyID(distr_point_id));
                        break;
                    }
                    default:
                        parameters = new Bundle();
                        parameters.putSerializable("clients_array", clients_ids);
                        parameters.putString("distr_point_id", distr_point_id);
                        parameters.putString("doc_type", "refund");
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters);
                }
            }

        }

        if (event.equals("editRefund"))
        {
            editRefund(parameters.getLong("id"), parameters.getBoolean("copy_refund"), parameters.getLong("order_id"), new MyID(parameters.getString("client_id")), new MyID(parameters.getString("distr_point_id")));
        }

        if (event.equals("cancelRefund"))
        {
            cancelRefund(parameters.getLong("id"));
        }

        if (event.equals("deleteRefund"))
        {
            deleteRefund(parameters.getLong("id"));
        }

        if (event.equals("createDistribs"))
        {
            String distr_point_id=parameters!=null?parameters.getString("distr_point_id"):null;
            if (distr_point_id==null)
            {
                // Создание документа из журнала
                editDistribs(0, false, m_client_id, new MyID());
            } else {
                // Создание документа из маршрута
                HashMap<String, String> clients_ids = TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id);
                switch (clients_ids.size()) {
                    case 0:
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_clients_for_trade_point), Snackbar.LENGTH_LONG).show();
                        break;
                    case 1: {
                        Map.Entry<String, String> entry = clients_ids.entrySet().iterator().next();
                        String client_id = entry.getKey();
                        editDistribs(0, false, new MyID(client_id), new MyID(distr_point_id));
                        break;
                    }
                    default:
                        parameters = new Bundle();
                        parameters.putSerializable("clients_array", clients_ids);
                        parameters.putString("distr_point_id", distr_point_id);
                        parameters.putString("doc_type", "distribs");
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters);

                }
            }

        }

        if (event.equals("editDistribs"))
        {
            editDistribs(parameters.getLong("id"), parameters.getBoolean("copy_distribs"), new MyID(parameters.getString("client_id")), new MyID(parameters.getString("distr_point_id")));
        }

        if (event.equals("cancelDistribs"))
        {
            cancelDistribs(parameters.getLong("id"));
        }

        if (event.equals("deleteDistribs"))
        {
            deleteDistribs(parameters.getLong("id"));
        }

        if (event.equals("createPayment"))
        {
            String distr_point_id=parameters!=null?parameters.getString("distr_point_id"):null;
            if (distr_point_id==null)
            {
                // Создание документа из журнала
                createPayment(m_client_id==null?null:m_client_id.toString(), null);
            } else {
                // Создание документа из маршрута
                HashMap<String, String> clients_ids = TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id);
                switch (clients_ids.size()) {
                    case 0:
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_clients_for_trade_point), Snackbar.LENGTH_LONG).show();
                        break;
                    case 1: {
                        Map.Entry<String, String> entry = clients_ids.entrySet().iterator().next();
                        String client_id = entry.getKey();
                        createPayment(client_id, distr_point_id);
                        break;
                    }
                    default:
                        parameters = new Bundle();
                        parameters.putSerializable("clients_array", clients_ids);
                        parameters.putString("distr_point_id", distr_point_id);
                        parameters.putString("doc_type", "payment");
                        showDialog(IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS, parameters);

                }
            }

        }

        if (event.equals("editPayment"))
        {
            editPayment(parameters.getLong("id"), parameters.getBoolean("copy_payment"), new MyID(parameters.getString("client_id")), new MyID(parameters.getString("distr_point_id")));
        }

        if (event.equals("deletePayment"))
        {
            deletePayment(parameters.getLong("id"));
        }

        if (event.equals("createMessage"))
        {
            editMessage(0, "");
        }

        if (event.equals("editMessage"))
        {
            editMessage(parameters.getLong("id"), parameters.getString("photo"));
        }

        if (event.equals("startVisit"))
        {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_START_VISIT, parameters);
        }

        if (event.equals("cancelVisit"))
        {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_CANCEL_VISIT, parameters);
        }

        if (event.equals("endVisit"))
        {
            //parameters.getLong("position");
            showDialog(IDD_QUERY_END_VISIT, parameters);
        }

        if (event.equals("routeAndDateSelected"))
        {
            String route_id=parameters.getString("route_id");
            if (route_id!=null)
                m_route_id=new MyID(route_id);
            else
                m_route_id=new MyID();
            m_route_date=parameters.getString("route_date");
            m_route_descr=parameters.getString("route_descr");
            if (fragment instanceof RoutesListFragment)
            {
                RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                routesListFragment.onRouteSelected(m_route_id.toString(), m_route_date, m_route_descr);
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




    protected class MyAlertDialog extends AlertDialog {
        Context mContext;
        AlertDialog.Builder builder;
        AlertDialog dialog;

        public MyAlertDialog(Context arg0) {
            super(arg0);
            mContext = arg0;
        }

        public MyAlertDialog setType(int idd_type) {
            MySingleton g = MySingleton.getInstance();

            builder = new AlertDialog.Builder(mContext);
            switch (idd_type) {
                case IDD_SHOW_STATS: {
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

                    String conditionString = "editing_backup=0"; // чтобы текущий созданный документ в статистику не попал
                    ArrayList<String> conditionArgs = new ArrayList<String>();

                    conditionString = Common.combineConditions(conditionString, conditionArgs, E_ORDER_STATE.getStatisticConditionWhere(), E_ORDER_STATE.getStatisticArgs());

                    if (m_client_id != null&&!m_client_id.isEmpty()) {
                        conditionString = Common.combineConditions(conditionString, conditionArgs, "client_id=?", new String[]{m_client_id.m_id});
                    }
                    if (m_bNotClosed) {
                        conditionString = Common.combineConditions(conditionString, conditionArgs, E_ORDER_STATE.getNotClosedConditionWhere(), E_ORDER_STATE.getNotClosedSelectionArgs());
                    }

                    if (g.Common.PHARAOH) {
                        //conditionString=Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_date, m_date+"Z"});
                        switch (m_filter_date_type) {
                            case 0:
                                // все
                                break;
                            case 1:
                                // дата
                                conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                break;
                            case 2:
                                // интервал
                                conditionString = Common.combineConditions(conditionString, conditionArgs, "(shipping_date between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                break;
                        }

                    } else {
                        //conditionString=Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_date, m_date+"Z"});
                        switch (m_filter_date_type) {
                            case 0:
                                // все
                                break;
                            case 1:
                                // дата
                                conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_begin + "Z"});
                                break;
                            case 2:
                                // интервал
                                conditionString = Common.combineConditions(conditionString, conditionArgs, "(datedoc between ? and ?)", new String[]{m_filter_date_begin, m_filter_date_end + "Z"});
                                break;
                        }
                    }

                    int count = 0;
                    double totalSum = 0.0;
                    double totalWeight = 0.0;

                    //Cursor cursor=getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"sum_doc", "weight_doc"}, selection, selectionArgs, null);
                    Cursor cursor = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"sum_doc", "weight_doc"}, conditionString, conditionArgs.toArray(new String[conditionArgs.size()]), null);
                    while (cursor.moveToNext()) {
                        count++;
                        totalSum += cursor.getDouble(0);
                        totalWeight += cursor.getDouble(1);
                    }

                    String statistics = getString(R.string.orders_statistics, count, Common.DoubleToStringFormat(totalSum, "%.2f"), g.Common.DoubleToStringFormat(totalWeight, "%.3f"));

                    //builder.setTitle("title");
                    //builder.setMessage("message");

                    builder.setMessage(statistics);
	          /*
	          builder.setPositiveButton(android.R.string.ok,
	             new DialogInterface.OnClickListener(){
	                public void onClick(DialogInterface dialog, int item) {
	                   //
	                }
	             });
	          builder.setNegativeButton(android.R.string.cancel, null);
	          */
                    builder.setPositiveButton(android.R.string.ok, null);
                    break;
                }
                case IDD_SHOW_PAYMENTS_STATS: {
                    String selection = E_PAYMENT_STATE.getPaymentStatisticConditionWhere();
                    String selectionArgs[] = E_PAYMENT_STATE.getPaymentStatisticArgs();

                    if (m_client_id != null&&!m_client_id.isEmpty()) {
                        selection = selection + " and client_id=?";
                        List<String> argsList0 = Arrays.asList(selectionArgs);
                        ArrayList<String> argsList = new ArrayList<String>(argsList0);
                        argsList.add(m_client_id.toString());
                        selectionArgs = argsList.toArray(new String[0]);
                    }

                    int count = 0;
                    double totalSum = 0.0;

                    Cursor cursor = getContentResolver().query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"sum_doc"}, selection, selectionArgs, null);
                    while (cursor.moveToNext()) {
                        count++;
                        totalSum += cursor.getDouble(0);
                    }

                    String statistics = getString(R.string.payments_statistics, count, Common.DoubleToStringFormat(totalSum, "%.2f"));
                    builder.setMessage(statistics);
                    builder.setPositiveButton(android.R.string.ok, null);
                }
            }
            return this;
        }

        public void show() {
            dialog = builder.create();
            dialog.show();
        }
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
        super.onPrepareDialog(id, dialog);
        MySingleton g = MySingleton.getInstance();

        switch (id) {
            case IDD_CANCEL_CURRENT_ORDER:
            case IDD_DELETE_CURRENT_ORDER: {

                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, bundle.getLong("_id")), new String[]{"order_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    m_order_id_to_process = cursor.getLong(0);
                }
                cursor.close();
                break;
            }
            case IDD_CANCEL_CURRENT_REFUND:
            case IDD_DELETE_CURRENT_REFUND: {
                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, bundle.getLong("_id")), new String[]{"refund_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    m_refund_id_to_process = cursor.getLong(0);
                }
                cursor.close();
                break;
            }
            case IDD_CANCEL_CURRENT_DISTRIBS:
            case IDD_DELETE_CURRENT_DISTRIBS: {
                Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(MTradeContentProvider.JOURNAL_CONTENT_URI, bundle.getLong("_id")), new String[]{"distribs_id"}, null, null, null);
                if (cursor.moveToNext()) {
                    m_distribs_id_to_process = cursor.getLong(0);
                }
                cursor.close();
                break;
            }
            case IDD_CANCEL_CURRENT_PAYMENT:
            case IDD_DELETE_CURRENT_PAYMENT:
                m_payment_id_to_process = bundle.getLong("_id");
                break;
            case IDD_DELETE_CURRENT_MESSAGE:
                m_message_id_to_process = bundle.getLong("_id");
                break;
            case IDD_DATA_FILTER: {
                CheckBox checkBoxOrders = (CheckBox) dialog.findViewById(R.id.checkBoxOrders);
                CheckBox checkBoxRefunds = (CheckBox) dialog.findViewById(R.id.checkBoxRefunds);
                CheckBox checkBoxDistribs = (CheckBox) dialog.findViewById(R.id.checkBoxDistribs);
                Spinner spinnerFilterType = (Spinner) dialog.findViewById(R.id.spinnerFilterType);

                m_filter_type_current = m_filter_type;
                m_filter_date_type_current = m_filter_date_type;
                if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                    checkBoxOrders.setVisibility(View.VISIBLE);
                    checkBoxOrders.setChecked(m_filter_orders);
                    checkBoxRefunds.setVisibility(View.VISIBLE);
                    checkBoxRefunds.setChecked(m_filter_refunds);
                    checkBoxDistribs.setVisibility(View.VISIBLE);
                    checkBoxDistribs.setChecked(m_filter_distribs);
                } else {
                    checkBoxOrders.setVisibility(View.GONE);
                    checkBoxRefunds.setVisibility(View.GONE);
                    checkBoxDistribs.setVisibility(View.GONE);
                }
                if (g.Common.PHARAOH) {
                    spinnerFilterType.setVisibility(View.VISIBLE);
                } else {
                    spinnerFilterType.setVisibility(View.GONE);
                }

                Spinner spinnerFilterPeriod = (Spinner) dialog.findViewById(R.id.spinnerFilterPeriod);

                if (m_filter_date_type < 0 || m_filter_date_type >= spinnerFilterPeriod.getCount())
                    spinnerFilterPeriod.setSelection(0);
                else
                    spinnerFilterPeriod.setSelection(m_filter_date_type);

                LinearLayout linearLayoutStartDate = (LinearLayout) dialog.findViewById(R.id.linearLayoutStartDate);
                LinearLayout linearLayoutEndDate = (LinearLayout) dialog.findViewById(R.id.linearLayoutEndDate);

                switch (m_filter_date_type) {
                    case 0:
                        linearLayoutStartDate.setVisibility(View.GONE);
                        linearLayoutEndDate.setVisibility(View.GONE);
                        break;
                    case 1:
                        linearLayoutStartDate.setVisibility(View.VISIBLE);
                        linearLayoutEndDate.setVisibility(View.GONE);
                        break;
                    case 2:
                        linearLayoutStartDate.setVisibility(View.VISIBLE);
                        linearLayoutEndDate.setVisibility(View.VISIBLE);
                        break;
                }

                if (m_filter_date_begin_current.length() >= 8) {
                    EditText etFirstDate = (EditText) dialog.findViewById(R.id.etFirstDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_filter_date_begin_current.substring(6, 8) + "." + m_filter_date_begin_current.substring(4, 6) + "." + m_filter_date_begin_current.substring(0, 4));
                    etFirstDate.setText(sb.toString());
                }

                if (m_filter_date_end_current.length() >= 8) {
                    EditText etSecondDate = (EditText) dialog.findViewById(R.id.etSecondDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_filter_date_end_current.substring(6, 8) + "." + m_filter_date_end_current.substring(4, 6) + "." + m_filter_date_end_current.substring(0, 4));
                    etSecondDate.setText(sb.toString());
                }

                //
                // Кнопка выбора даты
                final Button buttonSelectFirstDate = (Button) dialog.findViewById(R.id.buttonSelectFirstDate);
                buttonSelectFirstDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(IDD_FILTER_DATE_BEGIN);
                    }
                });

                //
                // Кнопка выбора даты
                final Button buttonSelectSecondDate = (Button) dialog.findViewById(R.id.buttonSelectSecondDate);
                buttonSelectSecondDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(IDD_FILTER_DATE_END);
                    }
                });

                break;
            }
            case IDD_FILTER_DATE_BEGIN: {
                int day = 0;
                int month = 0;
                int year = 0;
                if (m_filter_date_begin_current.length() >= 8) {
                    day = Integer.parseInt(m_filter_date_begin_current.substring(6, 8));
                    month = Integer.parseInt(m_filter_date_begin_current.substring(4, 6)) - 1;
                    year = Integer.parseInt(m_filter_date_begin_current.substring(0, 4));
                    ((DatePickerDialog) dialog).updateDate(year, month, day);
                }
                break;
            }
            case IDD_FILTER_DATE_END: {
                int day = 0;
                int month = 0;
                int year = 0;
                if (m_filter_date_end_current.length() >= 8) {
                    day = Integer.parseInt(m_filter_date_end_current.substring(6, 8));
                    month = Integer.parseInt(m_filter_date_end_current.substring(4, 6)) - 1;
                    year = Integer.parseInt(m_filter_date_end_current.substring(0, 4));
                    ((DatePickerDialog) dialog).updateDate(year, month, day);
                }
                break;
            }
            case IDD_QUERY_DOCUMENTS_PERIOD: {
                m_query_period_type_current = m_query_period_type;

                Spinner spinnerPeriodType = (Spinner) dialog.findViewById(R.id.spinnerPeriodType);

                if (m_query_period_type < 0 || m_query_period_type >= spinnerPeriodType.getCount())
                    spinnerPeriodType.setSelection(0);
                else
                    spinnerPeriodType.setSelection(m_query_period_type);

                if (m_query_date_begin_current.length() >= 8) {
                    EditText etFirstDate = (EditText) dialog.findViewById(R.id.etFirstDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_query_date_begin_current.substring(6, 8) + "." + m_query_date_begin_current.substring(4, 6) + "." + m_query_date_begin_current.substring(0, 4));
                    etFirstDate.setText(sb.toString());
                }

                if (m_query_date_end_current.length() >= 8) {
                    EditText etSecondDate = (EditText) dialog.findViewById(R.id.etSecondDate);
                    StringBuilder sb = new StringBuilder();
                    sb.append(m_query_date_end_current.substring(6, 8) + "." + m_query_date_end_current.substring(4, 6) + "." + m_query_date_end_current.substring(0, 4));
                    etSecondDate.setText(sb.toString());
                }

                //
                // Кнопка выбора даты
                final Button buttonSelectFirstDate = (Button) dialog.findViewById(R.id.buttonSelectFirstDate);
                buttonSelectFirstDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(IDD_QUERY_DOCUMENTS_DATE_BEGIN);
                    }
                });

                //
                // Кнопка выбора даты
                final Button buttonSelectSecondDate = (Button) dialog.findViewById(R.id.buttonSelectSecondDate);
                buttonSelectSecondDate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(IDD_QUERY_DOCUMENTS_DATE_END);
                    }
                });

                break;
            }
            case IDD_QUERY_DOCUMENTS_DATE_BEGIN: {
                int day = 0;
                int month = 0;
                int year = 0;
                if (m_query_date_begin_current.length() >= 8) {
                    day = Integer.parseInt(m_query_date_begin_current.substring(6, 8));
                    month = Integer.parseInt(m_query_date_begin_current.substring(4, 6)) - 1;
                    year = Integer.parseInt(m_query_date_begin_current.substring(0, 4));
                    ((DatePickerDialog) dialog).updateDate(year, month, day);
                }
                break;
            }
            case IDD_QUERY_DOCUMENTS_DATE_END: {
                int day = 0;
                int month = 0;
                int year = 0;
                if (m_query_date_end_current.length() >= 8) {
                    day = Integer.parseInt(m_query_date_end_current.substring(6, 8));
                    month = Integer.parseInt(m_query_date_end_current.substring(4, 6)) - 1;
                    year = Integer.parseInt(m_query_date_end_current.substring(0, 4));
                    ((DatePickerDialog) dialog).updateDate(year, month, day);
                }
                break;
            }

            case IDD_QUERY_START_VISIT: {

                final String distr_point_id=bundle.getString("distr_point_id");
                AlertDialog alertDialog=(AlertDialog)dialog;
                alertDialog.setMessage(getString(R.string.query_start_visit, bundle.getString("distr_point_descr")));
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                        if (fragment instanceof RoutesListFragment)
                        {
                            RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                            routesListFragment.startVisit(distr_point_id);
                            m_bNeedCoord = true;
                            startGPS();
                        }
                    }
                });

                break;
            }

            case IDD_QUERY_END_VISIT: {

                final String distr_point_id=bundle.getString("distr_point_id");
                AlertDialog alertDialog=(AlertDialog)dialog;
                alertDialog.setMessage(getString(R.string.query_end_visit, bundle.getString("distr_point_descr")));
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                        if (fragment instanceof RoutesListFragment)
                        {
                            RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                            routesListFragment.endVisit(distr_point_id);
                        }
                    }
                });

                break;
            }

            case IDD_QUERY_CANCEL_VISIT: {

                final String distr_point_id=bundle.getString("distr_point_id");
                AlertDialog alertDialog=(AlertDialog)dialog;
                alertDialog.setMessage(getString(R.string.query_cancel_visit, bundle.getString("distr_point_descr")));
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                        if (fragment instanceof RoutesListFragment)
                        {
                            RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                            routesListFragment.cancelVisit(distr_point_id);
                        }
                    }
                });

                break;
            }



            case IDD_QUERY_CREATE_DOCUMENT_SELECT_CLIENTS:
            {
                final AlertDialog alertDialog=(AlertDialog)dialog;
                ArrayAdapter<MyElementOfArrayList> arrayAdapter=(ArrayAdapter)alertDialog.getListView().getAdapter();
                HashMap<String, String> clients=(HashMap<String,String>)bundle.getSerializable("clients_array");
                final String distr_point_id=bundle.getString("distr_point_id");
                final String doc_type=bundle.getString("doc_type");

                if (doc_type.equals("order"))
                    alertDialog.setTitle(getString(R.string.select_client_to_create_order));
                if (doc_type.equals("refund"))
                    alertDialog.setTitle(getString(R.string.select_client_to_create_refund));
                if (doc_type.equals("payment"))
                    alertDialog.setTitle(getString(R.string.select_client_to_create_payment));
                if (doc_type.equals("distribs"))
                    alertDialog.setTitle(getString(R.string.select_client_to_create_distribs));


                arrayAdapter.clear();
                for (Map.Entry<String, String> entry: clients.entrySet())
                {
                    arrayAdapter.add(new MyElementOfArrayList(entry.getKey(), entry.getValue()));
                }
                alertDialog.getListView().setItemChecked(0, true);
                alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ListView listView = alertDialog.getListView();
                        MyElementOfArrayList element=(MyElementOfArrayList)listView.getItemAtPosition(listView.getCheckedItemPosition());
                        if (doc_type.equals("order"))
                            createOrder(element.getId(), distr_point_id);
                        if (doc_type.equals("refund")) {
                            editRefund(0, false, 0, new MyID(element.getId()), new MyID(distr_point_id));
                        }
                        if (doc_type.equals("payment"))
                            createPayment(element.getId(), distr_point_id);
                        if (doc_type.equals("distribs")) {
                            editDistribs(0, false, new MyID(element.getId()), new MyID(distr_point_id));
                        }

                    }
                });
                break;
            }


        }
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // TODO не знаю, откуда появилось это событие
    }

    @Override
    public void onNewIntent(final Intent queryIntent) {
        super.onNewIntent(queryIntent);
        setIntent(queryIntent);
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
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
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
    void updateDocumentInRoute(String distr_point_id, Object object) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (fragment instanceof RoutesListFragment) {
            RoutesListFragment routesListFragment = (RoutesListFragment) fragment;
            routesListFragment.updateDocumentInRoute(distr_point_id, object);
        }
    }

    // если режим маршрута, добавим туда заказ
    void updateDocumentInRouteDeleted(String document_uid) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
        if (fragment instanceof RoutesListFragment) {
            RoutesListFragment routesListFragment = (RoutesListFragment) fragment;
            routesListFragment.updateDocumentInRouteDeleted(document_uid);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    boolean getIsOrdersMode() {
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
        return true;
    }

    public boolean isFilterSet()
    {
        // Если установлен фильтр (без учета отбора по клиенту)
        MySingleton g = MySingleton.getInstance();
        if (m_filter_date_type!=0)
            return true;
        if (m_filter_orders && m_filter_refunds && m_filter_distribs || !(g.Common.PRODLIDER || g.Common.TANDEM))
            return false;
        return true;
    }

    // обновление меню
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.clear();
        MySingleton g = MySingleton.getInstance();
        g_options_menu = menu;
        switch (m_current_frame_type) {
            case FRAME_TYPE_ROUTES:
            {
                menu.setGroupVisible(R.id.menu_group_orders, false);
                menu.setGroupVisible(R.id.menu_group_payments, false);
                menu.setGroupVisible(R.id.menu_group_exchange, false);
                menu.setGroupVisible(R.id.menu_group_messages, false);
                menu.setGroupVisible(R.id.menu_group_routes, true);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                MenuItem item=menu.findItem(R.id.action_check_box_only_not_worked_out);
                item.setChecked(sharedPreferences.getBoolean("routes_show_only_worked_out", false));

            }
            break;
            case FRAME_TYPE_ORDERS: {
                //boolean bOrdersMode = true;
                //if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                //    bOrdersMode = getIsOrdersMode();
                //}
                menu.setGroupVisible(R.id.menu_group_orders, true);
                MenuItem itemDataFilterAsAction = menu.findItem(R.id.action_data_filter_as_action);
                if (!isFilterSet())
                {
                    itemDataFilterAsAction.setVisible(false);
                }
                MenuItem itemCreateRefund = menu.findItem(R.id.action_create_refund);
                MenuItem itemCreateDistribs = menu.findItem(R.id.action_create_distribs);
                //MenuItem itemDeleteClosedRefunds = menu.findItem(R.id.action_delete_closed_refunds);
                MenuItem itemDeleteClosedOrders = menu.findItem(R.id.action_delete_closed_orders);
                MenuItem itemDeleteClosedDocuments = menu.findItem(R.id.action_delete_closed_documents);
                if (g.Common.PRODLIDER || g.Common.TANDEM || g.Common.TITAN || g.Common.FACTORY) {
                    itemCreateRefund.setVisible(true);
                    itemCreateDistribs.setVisible(true);
                    //itemDeleteClosedRefunds.setVisible(true);
                    itemDeleteClosedOrders.setVisible(false);
                    itemDeleteClosedDocuments.setVisible(true);
                } else {
                    itemCreateRefund.setVisible(false);
                    itemCreateDistribs.setVisible(false);
                    //itemDeleteClosedRefunds.setVisible(false);
                    itemDeleteClosedOrders.setVisible(true);
                    itemDeleteClosedDocuments.setVisible(false);
                }
                menu.setGroupVisible(R.id.menu_group_payments, false);
                menu.setGroupVisible(R.id.menu_group_exchange, false);
                menu.setGroupVisible(R.id.menu_group_messages, false);
                menu.setGroupVisible(R.id.menu_group_routes, false);
            }
            break;
            case FRAME_TYPE_PAYMENTS: {
                menu.setGroupVisible(R.id.menu_group_orders, false);
                menu.setGroupVisible(R.id.menu_group_payments, true);
                menu.setGroupVisible(R.id.menu_group_exchange, false);
                menu.setGroupVisible(R.id.menu_group_messages, false);
            }
            break;
            case FRAME_TYPE_EXCHANGE:
                menu.setGroupVisible(R.id.menu_group_orders, false);
                menu.setGroupVisible(R.id.menu_group_payments, false);
                menu.setGroupVisible(R.id.menu_group_exchange, true);
                menu.setGroupVisible(R.id.menu_group_messages, false);
                menu.setGroupVisible(R.id.menu_group_routes, false);
                break;
            case FRAME_TYPE_MESSAGES:
                menu.setGroupVisible(R.id.menu_group_orders, false);
                menu.setGroupVisible(R.id.menu_group_payments, false);
                menu.setGroupVisible(R.id.menu_group_exchange, false);
                menu.setGroupVisible(R.id.menu_group_messages, true);
                menu.setGroupVisible(R.id.menu_group_routes, false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private boolean checkGeoEnabled()
    {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        //boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        //return mIsGeoDisabled;
        if (!mIsGPSEnabled) {
            showDialog(IDD_NEED_GPS_ENABLED);
        }
        return mIsGPSEnabled;
    }

    void createOrder(String client_id, String distr_point_id)
    {
        MySingleton g = MySingleton.getInstance();
        if (g.Common.TITAN || g.Common.FACTORY) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                mParamsToCreateDocsWhenRequestPermissions=new ParamsToCreateDocs();
                // Более правильного способа передачи параметров не придумал :)
                mParamsToCreateDocsWhenRequestPermissions.client_id=client_id;
                mParamsToCreateDocsWhenRequestPermissions.distr_point_id=distr_point_id;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA_FOR_ORDER);
            } else {
                createOrderPre(client_id, distr_point_id);
            }
        } else if (g.Common.PRODLIDER) {
            if (checkGeoEnabled())
                editOrder(0, false, client_id, distr_point_id);
        } else {
            editOrder(0, false, client_id, distr_point_id);
        }
    }

    void createPayment(String client_id, String distr_point_id)
    {
        MySingleton g = MySingleton.getInstance();
        if (g.Common.PRODLIDER) {
            if (checkGeoEnabled())
                editPayment(0, false, new MyID(client_id), new MyID(distr_point_id));
        } else
            editPayment(0, false, new MyID(client_id), new MyID(distr_point_id));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        //StringBuilder sb = new StringBuilder();
        MySingleton g = MySingleton.getInstance();

        // Выведем в TextView информацию о нажатом пункте меню
        //sb.append("Item Menu");
        //sb.append("\r\n groupId: " + String.valueOf(item.getGroupId()));
        //sb.append("\r\n itemId: " + String.valueOf(item.getItemId()));
        //sb.append("\r\n order: " + String.valueOf(item.getOrder()));
        //sb.append("\r\n title: " + item.getTitle());
        //tv.setText(sb.toString());

        switch (item.getItemId()) {
            case R.id.action_create_order:
                createOrder(m_client_id!=null?m_client_id.toString():null, null);
                break;
            // В зависимости от формата одно из 2-х событий (в меню есть либо одно, либо другое)
            case R.id.action_delete_closed_orders:
            case R.id.action_delete_closed_documents:
                showDialog(IDD_DELETE_CLOSED_DOCUMENTS);
                break;
            case R.id.action_sort_orders:
                showDialog(IDD_SORT_ORDERS);
                break;
            case R.id.action_show_stats:
                new MyAlertDialog(this).setType(IDD_SHOW_STATS).show();
                break;
            case R.id.action_data_filter:
            case R.id.action_data_filter_as_action:
                showDialog(IDD_DATA_FILTER);
                break;
            case R.id.action_create_payment:
                createPayment(m_client_id==null?null:m_client_id.toString(), null);
                break;
            case R.id.action_delete_closed_payments:
                showDialog(IDD_DELETE_CLOSED_PAYMENTS);
                break;
            case R.id.action_create_refund:
                editRefund(0, false, 0, m_client_id, new MyID());
                break;
            case R.id.action_create_distribs:
                if (g.Common.PRODLIDER) {
                    if (checkGeoEnabled())
                        editDistribs(0, false, m_client_id, new MyID());
                } else
                    editDistribs(0, false, m_client_id, new MyID());
                break;
            //case R.id.action_delete_closed_refunds:
            //	showDialog(IDD_DELETE_CLOSED_REFUNDS);
            //	break;
            case R.id.action_show_payments_stats:
                new MyAlertDialog(this).setType(IDD_SHOW_PAYMENTS_STATS).show();
                break;
            case R.id.action_create_message:
                editMessage(0, "");
                break;
            case R.id.action_create_photo: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                } else {
                    doActionCreatePhoto();
                }
                break;
            }
            case R.id.action_delete_readed_messages:
                showDialog(IDD_DELETE_READED_MESSAGES);
                break;
            case R.id.action_refresh_route: {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                if (fragment instanceof RoutesListFragment)
                {
                    RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                    Boolean bResult=routesListFragment.fillRealRoutes(m_route_date);
                    //routesListFragment.restartLoaderForListView();
                    if (bResult)
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.route_refreshed_successfully), Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.route_cant_be_refreshed), Snackbar.LENGTH_LONG).show();
                }

                break;
            }
            case R.id.action_check_box_only_not_worked_out: {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                if (fragment instanceof RoutesListFragment)
                {
                    RoutesListFragment routesListFragment=(RoutesListFragment)fragment;
                    boolean isChecked=!item.isChecked();
                    item.setChecked(isChecked);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("routes_show_only_worked_out", isChecked);
                    editor.commit();

                    routesListFragment.setOnlyNotWorkedOut(isChecked);


                }
                break;
            }
            case R.id.action_settings: {
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

                Intent intent = new Intent(MainActivity.this, PrefActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (g.m_preferences_from_ini)
                    intent.putExtra("readOnly", true);
                startActivity(intent);
                break;
            }
            case R.id.action_backup:
                doActionBackup();
                break;

            case R.id.action_vacuum_database:
                doVacuumDatabase();
                break;

            //Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    void doActionCreatePhoto() {
        String temp_fileName = "tmp.jpg";

        //File photoDir = new File(Environment.getExternalStorageDirectory() + photoFolder);
        File photoDir = new File(getFilesDir(), "photos");
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        // к сожалению приходится устанавливать параметр EXTRA_OUTPUT
        // т.к. есть телефоны с багом, которые не возвращают uri, а только data (небольшую картинку)
        // у меня такой телефон создает только 1 файл в этом случае
        // другие же с указанием EXTRA_OUTPUT создают сразу 2 файла, но пусть будет так

        File temp_photoFile = new File(photoDir, temp_fileName);
        // До 21.08.2018
        //outputPhotoFileUri=Uri.fromFile(temp_photoFile);
        // Вариант решения
        outputPhotoFileUri = FileProvider.getUriForFile(this, "ru.code22.fileprovider", temp_photoFile);
        //outputPhotoFileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".my.package.name.provider", temp_photoFile);

        //Intent intent=new Intent(MainActivity.this, PhotoActivity.class);
        //startActivityForResult(intent, CREATE_PHOTO_MESSAGE_REQUEST);

        // https://stackoverflow.com/questions/24467696/android-file-provider-permission-denial

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
        //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, "new-photo-name.jpg");

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPhotoFileUri);
        cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 90);
        cameraIntent.putExtra("return-data", false); // мне кажется что этот флаг все равно игнорируется, как будто указываем все равно true
        // Второй вариант решения от 21.08.2018
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                cameraIntent.setClipData(ClipData.newRawUri("", outputPhotoFileUri));
            }
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        //startActivityForResult(cameraIntent, CAMERA_REQUEST);
        createCameraActivityResultLauncher.launch(cameraIntent);
    }

    void doActionBackup() {
        try {
            //File sd = g.Common.myGetExternalStorageDirectory();
            //File sd = g.Common.getExternalSDCardDirectory();
            File sd = Common.getMyStorageFileDir(MainActivity.this, "");//Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd != null && sd.canWrite()) {
                String currentDBPath = "/data/" + getBaseContext().getPackageName() + "/databases/" + MTradeContentProvider.DB_NAME;
                File backupDBPath = sd;//new File(sd, "/mtrade");
                //if (!backupDBPath.exists()) {
                //    backupDBPath.mkdirs();
                //}
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(backupDBPath, "backup.db");
                if (!Common.myCopyFile(currentDB, backupDB, false))
                {
                    Snackbar.make(findViewById(android.R.id.content), R.string.error_during_backup_to_sd, Snackbar.LENGTH_LONG).show();
                    return;
                }
                Snackbar.make(findViewById(android.R.id.content), backupDB.toString(), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content), R.string.error_no_sd_card_access, Snackbar.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    void doVacuumDatabase()
    {
        getContentResolver().insert(MTradeContentProvider.VACUUM_CONTENT_URI, null);
    }

    //@Override
    //public void onListItemClick(ListView l, View v, int position, long id) {
    //    // Do something when a list item is clicked
    //}


    private LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                MySingleton g = MySingleton.getInstance();
                if (m_bNeedCoord) {
                    m_bNeedCoord = false;
                    m_locationManager.removeUpdates(locListener);
                }

                String accept_coord_where;
                String[] accept_coord_selectionArgs;
                ContentValues cv;

                // Обновим координаты заказов
                accept_coord_where = E_ORDER_STATE.getAcceptCoordConditionWhere();
                accept_coord_selectionArgs = E_ORDER_STATE.getAcceptCoordConditionSelectionArgs();

                boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                cv = new ContentValues();
                cv.put("latitude", loc.getLatitude());
                cv.put("longitude", loc.getLongitude());
                cv.put("datecoord", Common.getDateTimeAsString14(new Date(loc.getTime())));
                cv.put("gpsstate", gpsenabled ? 1 : loc.getProvider().equals(LocationManager.NETWORK_PROVIDER) ? 2 : 0);
                cv.put("gpsaccuracy", loc.getAccuracy());
                cv.put("accept_coord", 0);
                if (getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, accept_coord_where, accept_coord_selectionArgs) > 0) {
                    // и увеличиваем версию редактируемой заявки
                    // (не проверяем, действительно ли в данный момент она редактируется, и принимает координаты, т.к. это не важно)
                    g.MyDatabase.m_order_editing.versionPDA++;
                }

                // Обновим координаты дистрибьюции
                accept_coord_where = E_DISTRIBS_STATE.getAcceptCoordConditionWhere();
                accept_coord_selectionArgs = E_DISTRIBS_STATE.getAcceptCoordConditionSelectionArgs();

                if (getContentResolver().update(MTradeContentProvider.DISTRIBS_CONTENT_URI, cv, accept_coord_where, accept_coord_selectionArgs) > 0) {
                    g.MyDatabase.m_distribs_editing.versionPDA++;
                }

                // Обновим координаты возвратов
                accept_coord_where = E_REFUND_STATE.getAcceptCoordConditionWhere();
                accept_coord_selectionArgs = E_REFUND_STATE.getAcceptCoordConditionSelectionArgs();

                if (getContentResolver().update(MTradeContentProvider.REFUNDS_CONTENT_URI, cv, accept_coord_where, accept_coord_selectionArgs) > 0) {
                    g.MyDatabase.m_refund_editing.versionPDA++;
                }

                // Обновим координаты ПКО
                accept_coord_where = E_PAYMENT_STATE.getAcceptCoordConditionWhere();
                accept_coord_selectionArgs = E_PAYMENT_STATE.getAcceptCoordConditionSelectionArgs();

                if (getContentResolver().update(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, cv, accept_coord_where, accept_coord_selectionArgs) > 0) {
                    g.MyDatabase.m_payment_editing.versionPDA++;
                }

                // Обновим координаты визитов
                getContentResolver().update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "accept_coord=1", null);

            }

        }

        @Override
        public void onProviderDisabled(String arg0) {

        }

        @Override
        public void onProviderEnabled(String arg0) {

        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }

    };


    private LocationListener locListenerEveryTime = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                // возможно, в случае определения координат по wifi, здесь будет false
                boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                ContentValues cv = new ContentValues();
                cv.put("latitude", loc.getLatitude());
                cv.put("longitude", loc.getLongitude());
                cv.put("datecoord", Common.getDateTimeAsString14(new Date(loc.getTime())));
                cv.put("gpsstate", gpsenabled ? 1 : loc.getProvider().equals(LocationManager.NETWORK_PROVIDER) ? 2 : 0);
                cv.put("gpsaccuracy", loc.getAccuracy());
                cv.put("version", 0);
                cv.put("gpstype", gpsenabled ? 1 : loc.getProvider().equals(LocationManager.NETWORK_PROVIDER) ? 2 : 0);
                getContentResolver().insert(MTradeContentProvider.GPS_COORD_CONTENT_URI, cv);
            }
        }

        @Override
        public void onProviderDisabled(String arg0) {

        }

        @Override
        public void onProviderEnabled(String arg0) {

        }

        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

        }

    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MySingleton g = MySingleton.getInstance();
        //String name = data.getStringExtra("name");
        //tvName.setText("Your name is " + name);
        switch (requestCode&0xffff) {
            case REQ_CODE_VERSION_UPDATE:
                //Toast.makeText(MainActivity.this,"Received REQ_CODE_VERSION_UPDATE", Toast.LENGTH_SHORT).show();
                if (resultCode != RESULT_OK) { //RESULT_OK / RESULT_CANCELED / RESULT_IN_APP_UPDATE_FAILED
                    //Toast.makeText(MainActivity.this, "Update flow failed! Result code: " + resultCode, Toast.LENGTH_SHORT).show();
                    L.d("Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                    unregisterInstallStateUpdListener();
                }
                break;
                /*
            case EDIT_ORDER_PRE_REQUEST:
                if (resultCode == OrderPreActivity.ORDER_PRE_ACTION_RESULT_OK) {
                    editOrder(0, false, data.getStringExtra("client_id"), data.getStringExtra("distr_point_id"));
                }
                break;
            case SELECT_CLIENT_REQUEST:
                if (data != null) {
                    long id = data.getLongExtra("id", 0);
                    //Toast.makeText(MainActivity.this, "id=" + id, Toast.LENGTH_SHORT).show();
                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, id);
                    //Cursor cursor=getContentResolver().query(singleUri, mProjection, "", selectionArgs, null);
                    Cursor cursor = getContentResolver().query(singleUri, new String[]{"descr", "id"}, null, null, null);
                    if (cursor.moveToNext()) {
                        int descrIndex = cursor.getColumnIndex("descr");
                        int idIndex = cursor.getColumnIndex("id");
                        String newWord = cursor.getString(descrIndex);
                        String clientId = cursor.getString(idIndex);
                        //EditText et = (EditText) findViewById(R.id.etClient);
                        //et.setText(newWord);
                        m_client_id = new MyID(clientId);
                        m_client_descr = newWord;
                        //getSupportLoaderManager().restartLoader(ORDERS_LOADER_ID, null, MainActivity.this);
                        //getSupportLoaderManager().restartLoader(PAYMENTS_LOADER_ID, null, MainActivity.this);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment=fragmentManager.findFragmentById(R.id.content_frame);
                        if (fragment instanceof OrdersListFragment)
                        {
                            OrdersListFragment ordersListFragment=(OrdersListFragment)fragment;
                            ordersListFragment.onFilterClientSelected(clientId, newWord);
                        }
                        if (fragment instanceof PaymentsListFragment)
                        {
                            PaymentsListFragment paymentsListFragment=(PaymentsListFragment)fragment;
                            paymentsListFragment.onFilterClientSelected(clientId, newWord);
                        }

                    }
                    cursor.close();
                }
                break;
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
            case EDIT_ORDER_REQUEST:
                if (data != null && resultCode == OrderActivity.ORDER_RESULT_OK) {
                    if (g.MyDatabase.m_order_editing.accept_coord == 1) {
                        boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        g.MyDatabase.m_order_editing.gpsstate = gpsenabled ? 1 : 0;
                    }
                    g.MyDatabase.m_order_editing.versionPDA++;
                    // Перед записью считаем сумму документа
                    g.MyDatabase.m_order_editing.sumDoc = g.MyDatabase.m_order_editing.GetOrderSum(null, false);
                    g.MyDatabase.m_order_editing.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
                    if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED) {
                        g.MyDatabase.m_order_editing.state = E_ORDER_STATE.E_ORDER_STATE_CREATED;
                    }
                    TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id, 0);
                    if (g.Common.NEW_BACKUP_FORMAT) {
                        // Удаляем резервную копию документа после записи настоящего документа
                        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_new_editing_id);
                        getContentResolver().delete(singleUri, "editing_backup<>0", null);
                    }
                    // Переименовываем файл изображения
                    if ((g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) && g.MyDatabase.m_order_editing_created) {
                        File photoFileDir = g.Common.getMyStorageFileDir(getBaseContext(), "photo");
                        File attachFileDir = g.Common.getMyStorageFileDir(getBaseContext(), "attaches");
                        File fileOrderImage1 = new File(photoFileDir, "order_image_1.jpg");
                        if (fileOrderImage1.exists()) {
                            File fileOrderImage1Dest = new File(attachFileDir, "order_image_1_" + g.MyDatabase.m_order_editing.uid.toString().replace("{", "").replace("}", "") + ".jpg");
                            fileOrderImage1.renameTo(fileOrderImage1Dest);
                        }
                        File fileOrderImage2 = new File(photoFileDir, "order_image_2.jpg");
                        if (fileOrderImage2.exists()) {
                            File fileOrderImage2Dest = new File(attachFileDir, "order_image_2_" + g.MyDatabase.m_order_editing.uid.toString().replace("{", "").replace("}", "") + ".jpg");
                            fileOrderImage2.renameTo(fileOrderImage2Dest);
                        }

                    }
                    //
                    if (!m_bNeedCoord && g.MyDatabase.m_order_editing.accept_coord == 1) {
                        m_bNeedCoord = true;
                        startGPS();
                    }
                    // Резервное сохрание заявки в текстовый файл, либо удаление этой копии
                    if (!g.Common.NEW_BACKUP_FORMAT && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        // получаем путь к SD
                        File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                        // создаем каталог
                        if (!sdPath.exists()) {
                            sdPath.mkdirs();
                        }
                        //
                        File sdFile = new File(sdPath, g.MyDatabase.m_order_editing.uid.toString() + ".txt");
                        if (E_ORDER_STATE.getCanBeRestoredFromTextFile(g.MyDatabase.m_order_editing.state)) {
                            try {
                                //BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sdFile), "cp1251"));
                                bw.write("0\r\n"); // obsolette m_orders_version
                                bw.write(String.valueOf(g.Common.m_mtrade_version));
                                bw.write("\r\n");
                                TextDatabase.SaveOrderToText(bw, g.MyDatabase.m_order_editing, false, true);
                                bw.write("@@@"); // начиная с версии 3.0
                                bw.flush();
                                bw.close();
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "error", e);
                            }
                        } else {
                            sdFile.delete();
                        }
                    }

                    if (g.Common.PHARAOH && g.MyDatabase.m_order_editing.dont_need_send == 0) {
                        // и запускаем обмен
                        new ExchangeTask().execute(3, 0, "");
                    }

                    // если режим маршрута, добавим туда заказ
                    updateDocumentInRoute(g.MyDatabase.m_order_editing.distr_point_id.toString(), g.MyDatabase.m_order_editing);

                }
                if (resultCode == OrderActivity.ORDER_RESULT_CANCEL) {
                    if (g.Common.NEW_BACKUP_FORMAT) {
                        // если документ не меняли, там будет -1, соответственно удалять нечего
                        if (g.MyDatabase.m_order_new_editing_id > 0) {
                            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_new_editing_id);
                            getContentResolver().delete(singleUri, "editing_backup<>0", null);
                        }
                    } else {
                        // Старый вариант
                        if (g.MyDatabase.m_order_editing.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_order_editing_id != 0) {
                            Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.ORDERS_CONTENT_URI, g.MyDatabase.m_order_editing_id);
                            getContentResolver().delete(singleUri, E_ORDER_STATE.getCanBeDeletedConditionWhere(), E_ORDER_STATE.getCanBeDeletedArgs());
                        }
                    }
                }
                break;
            case EDIT_PAYMENT_REQUEST:
                if (data != null && resultCode == PaymentActivity.PAYMENT_RESULT_OK) {
                    g.MyDatabase.m_payment_editing.versionPDA++;
                    TextDatabase.SavePaymentSQL(getContentResolver(), g.MyDatabase, g.MyDatabase.m_payment_editing, g.MyDatabase.m_payment_editing_id);
                    if (!m_bNeedCoord && g.MyDatabase.m_payment_editing.accept_coord == 1) {
                        m_bNeedCoord = true;
                        startGPS();
                    }
                    // если режим маршрута, добавим туда платеж
                    updateDocumentInRoute(g.MyDatabase.m_payment_editing.distr_point_id.toString(), g.MyDatabase.m_payment_editing);
                }
                break;
            case EDIT_REFUND_REQUEST:
                if (data != null && resultCode == RefundActivity.REFUND_RESULT_OK) {
                    if (g.MyDatabase.m_refund_editing.accept_coord == 1) {
                        boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        g.MyDatabase.m_refund_editing.gpsstate = gpsenabled ? 1 : 0;
                    }
                    g.MyDatabase.m_refund_editing.versionPDA++;
                    // Перед записью считаем вес документа
                    g.MyDatabase.m_refund_editing.weightDoc = TextDatabase.GetOrderWeight(getContentResolver(), g.MyDatabase.m_order_editing, null, false);
                    if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
                        g.MyDatabase.m_refund_editing.state = E_REFUND_STATE.E_REFUND_STATE_CREATED;
                    }
                    TextDatabase.SaveRefundSQL(getContentResolver(), g.MyDatabase.m_refund_editing, g.MyDatabase.m_refund_editing_id);
                    if (!m_bNeedCoord && g.MyDatabase.m_refund_editing.accept_coord == 1) {
                        m_bNeedCoord = true;
                        startGPS();
                    }
                    // Резервное сохрание возврата в текстовый файл, либо удаление этой копии
                    // не выполняем, в отличие от заказа
                    // TODO

                    // если режим маршрута, добавим туда возврат
                    updateDocumentInRoute(g.MyDatabase.m_refund_editing.distr_point_id.toString(), g.MyDatabase.m_refund_editing);

                }
                if (resultCode == RefundActivity.REFUND_RESULT_CANCEL) {
                    if (g.MyDatabase.m_refund_editing.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED && g.MyDatabase.m_refund_editing_id != 0) {
                        Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.REFUNDS_CONTENT_URI, g.MyDatabase.m_refund_editing_id);
                        getContentResolver().delete(singleUri, E_REFUND_STATE.getCanBeDeletedConditionWhere(), E_REFUND_STATE.getCanBeDeletedArgs());
                    }
                }
                break;
            case EDIT_DISTRIBS_REQUEST:
                if (data != null && resultCode == DistribsActivity.DISTRIBS_RESULT_OK) {
                    if (g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                        boolean gpsenabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        g.MyDatabase.m_distribs_editing.gpsstate = gpsenabled ? 1 : 0;
                    }
                    g.MyDatabase.m_distribs_editing.versionPDA++;
                    TextDatabase.SaveDistribsSQL(getContentResolver(), g.MyDatabase.m_distribs_editing, g.MyDatabase.m_distribs_editing_id);
                    if (!m_bNeedCoord && g.MyDatabase.m_distribs_editing.accept_coord == 1) {
                        m_bNeedCoord = true;
                        startGPS();
                    }
                    // если режим маршрута, добавим туда дистрибьюцию
                    updateDocumentInRoute(g.MyDatabase.m_distribs_editing.distr_point_id.toString(), g.MyDatabase.m_distribs_editing);

                }
                break;
            case EDIT_MESSAGE_REQUEST:
                // Можем менять только свои сообщения (&4)!=0
                if (data != null && resultCode == MessageActivity.MESSAGE_RESULT_OK && (g.MyDatabase.m_message_editing.acknowledged & 4) != 0) {
                    g.MyDatabase.m_message_editing.ver++;
                    TextDatabase.SaveMessageSQL(getContentResolver(), g.MyDatabase, g.MyDatabase.m_message_editing, g.MyDatabase.m_message_editing_id);

                    if (!g.MyDatabase.m_message_editing.fname.isEmpty()) {
                        File attachFileDir = Common.getMyStorageFileDir(MainActivity.this, "attaches");

                        File inFile = new File(g.MyDatabase.m_message_editing.fname);
                        File outFile = new File(attachFileDir, inFile.getName());

                        boolean bOutFileOpened = false;

                        InputStream in;
                        OutputStream out;
                        try {
                            in = new FileInputStream(inFile);
                            out = new FileOutputStream(outFile);
                            bOutFileOpened = true;
                            // Transfer bytes from in to out
                            byte[] buf = new byte[1024];
                            int len;
                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }
                            in.close();
                            out.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            //e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            //e.printStackTrace();
                            if (bOutFileOpened) {
                                outFile.delete();
                            }
                        }
                    }
                }
                break;
            //case CREATE_PHOTO_MESSAGE_REQUEST:
            //	break;
            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {

                    // Unfortunately there is a bug on some devices causing the Intend data parameter
                    // in onActivityResult to be null when you use the MediaStore.EXTRA_OUTPUT flag
                    // in your intent for the camera. A workaround is to keep the outputFileUri
                    // variable global so that you can access it again in your onActivityResult method

                    Date date = new Date();
                    //String timeString=String.valueOf(System.currentTimeMillis());
                    String timeString = Common.MyDateFormat("dd.MM.yyyy HH:mm:ss", date);

                    String fileName = Common.getDateTimeAsString14(date).toString() + ".jpg";

                    File photoDir = Common.getMyStorageFileDir(MainActivity.this, photoFolder);
                    if (!photoDir.exists()) {
                        photoDir.mkdirs();
                    }

                    boolean bSavedOk = false;

                    File imgWithTime = new File(photoDir, fileName);
                    ExifInterface exifInterface = null;

                    if (data != null) {
                        //if the intent data is not null, use it
                        //puc_image = getImagePath(Uri.parse(data.toURI())); //update the image path accordingly
                        //StoreImage(this, Uri.parse(data.toURI()), puc_img);
                        if (data.getData() != null) {
                            bSavedOk = ImagePrinting.StoreImage(this, data.getData(), imgWithTime, timeString);
                            if (bSavedOk) {
                                // Удалим первый файл, созданный телефоном
                                String imgPath = ImagePrinting.getImagePath(this, data.getData());
                                File oldFile = new File(imgPath);
                                oldFile.delete();
                            }
                        } else {
                            // Самсунг)
                            bSavedOk = ImagePrinting.StoreImage(this, outputPhotoFileUri, imgWithTime, timeString);
                        }
                    } else {
                        //Use the outputFileUri global variable
                        bSavedOk = ImagePrinting.StoreImage(this, outputPhotoFileUri, imgWithTime, timeString);
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
                        if (outputPhotoFileUri.getScheme().equals("content")) {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    InputStream iStream = getContentResolver().openInputStream(outputPhotoFileUri);
                                    exifInterface = new ExifInterface(iStream);
                                }
                            } catch (IOException e) {
                                exifInterface = null;
                            }
                        } else {
                            String imgPath = ImagePrinting.getImagePath(this, outputPhotoFileUri);
                            File oldFile = new File(imgPath);
                            try {
                                exifInterface = new ExifInterface(imgPath);
                            } catch (IOException e) {
                                exifInterface = null;
                            }
                            oldFile.delete();
                        }

                        String exif_DATETIME = Common.MyDateFormat("yyyy:MM:dd HH:mm:ss", date);
                        String exif_MODEL = "Android";
                        String exif_MAKE = "";
                        //String exif_IMAGE_LENGTH="";
                        //String exif_IMAGE_WIDTH="";

                        if (exifInterface != null) {
                            //exif_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                            exif_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                            exif_MAKE = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                            //String et=exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
                            //exif_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface..TAG_IMAGE_LENGTH);
                            //exif_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                        }
                        try {
                            ExifInterface exifInterface2 = new ExifInterface(imgWithTime.getAbsolutePath());
                            exifInterface2.setAttribute(ExifInterface.TAG_DATETIME, exif_DATETIME);
                            exifInterface2.setAttribute(ExifInterface.TAG_MODEL, exif_MODEL);
                            exifInterface2.setAttribute(ExifInterface.TAG_MAKE, exif_MAKE);
                            exifInterface2.saveAttributes();

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
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        editMessage(0, imgWithTime.getAbsolutePath());
                    }
                }
                break;
                 */
        }
    }

    private void onCloseActivity() {
        MySingleton g = MySingleton.getInstance();
        g.MyDatabase.m_seance_closed = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
        if (BuildConfig.DEBUG) {
            System.exit(0);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                //super.onBackPressed();
                onCloseActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Alternative variant for API 5 and higher
    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            onCloseActivity();
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private ExchangeFragment getExchangeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame);
        ExchangeFragment exchangeFragment = null;
        if (fragment instanceof ExchangeFragment) {
            exchangeFragment = (ExchangeFragment) fragment;
        }
        return exchangeFragment;
    }


    class ServiceOperationsTask extends AsyncTask<String, String, String> {

        protected ProgressDialog progressDialog;
        int m_state;

        private static final int SERVICE_TASK_STATE_BEGIN = 1;
        private static final int SERVICE_TASK_STATE_REINDEX = 2;
        private static final int SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED = 3;
        private static final int SERVICE_TASK_STATE_REINDEX_UPGRADE = 4;
        private static final int SERVICE_TASK_STATE_RECOVER_ORDERS = 5;
        private static final int SERVICE_TASK_STATE_MOVE_OLD_FILES = 6;
        //private static final int SERVICE_TASK_STATE_END = 4;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_state = SERVICE_TASK_STATE_BEGIN;

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            progressDialog.setTitle(R.string.title_service_operation);
            progressDialog.setMessage(getString(R.string.service_operation_state_service_operation));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Common.dismissWithExceptionHandling(progressDialog);
            loadDataAfterStart();
        }

        protected void onProgressUpdate(String... progress) {
            //setProgressPercent(progress[0]);

            if (progress[0].isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), progress[1], Snackbar.LENGTH_SHORT).show();
            } else {
                int mode = Integer.parseInt(progress[0]);
                switch (mode) {
                    case SERVICE_TASK_STATE_REINDEX:
                        progressDialog.setMessage(getString(R.string.service_operation_state_reindexing));
                        break;
                    case SERVICE_TASK_STATE_RECOVER_ORDERS:
                        progressDialog.setMessage(getString(R.string.service_operation_state_recovering_orders));
                        break;
                    case SERVICE_TASK_STATE_MOVE_OLD_FILES:
                        progressDialog.setMessage(getString(R.string.service_operation_state_moving_old_files));
                        break;
                }

            }
        }


        @Override
        protected String doInBackground(String... params) {
            //boolean reindexNeeded=false;
            int mode = Integer.parseInt(params[0]);
            publishProgress(Integer.toString(SERVICE_TASK_STATE_REINDEX));
            if (mode == SERVICE_TASK_STATE_RECOVER_ORDERS) {
                getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null);
            }
            getContentResolver().insert(MTradeContentProvider.REINDEX_CONTENT_URI, null);
            MySingleton g = MySingleton.getInstance();
            TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
            if (mode == SERVICE_TASK_STATE_RECOVER_ORDERS) {
                publishProgress(Integer.toString(SERVICE_TASK_STATE_REINDEX_INCORRECT_CLOSED));
                recoverOrders(true);
            }
            if (params[1].equals("66"))
            {
                if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) {
                    publishProgress(Integer.toString(SERVICE_TASK_STATE_MOVE_OLD_FILES));
                    try {
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            File oldDir = Environment.getExternalStorageDirectory();
                            moveOldFiles(new File(oldDir, "/mtrade/goods"), "goods", "*.jpg");
                            moveOldFiles(new File(oldDir, "/mtrade/goods"), "goods", "*.png");
                            moveOldFiles(new File(oldDir, "/mtrade/attaches"), "attaches", "*.*");
                        } else {
                            File oldDir = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName());
                            moveOldFiles(new File(oldDir, "/temp"), "goods", "*.jpg");
                            moveOldFiles(new File(oldDir, "/temp"), "goods", "*.png");
                            moveOldFiles(new File(oldDir, "/attaches"), "attaches", "*.*");
                        }
                    } catch (Error e)
                    {
                        // на всякий случай
                    }
                }
            }

            return "";
        }

        protected void moveOldFiles(File oldDir, String newDir, String wildcard)
        {
            if (oldDir.exists())
            {
                // отправляем все файлы из папки на флэшке
                File[] tempFileNames;
                if (wildcard!=null) {
                    FileFilter fileFilter = new WildcardFileFilter(wildcard);
                    tempFileNames = oldDir.listFiles(fileFilter);
                } else
                {
                    tempFileNames = oldDir.listFiles();
                }
                if (tempFileNames != null) {
                    File destDir=Common.getMyStorageFileDir(MainActivity.this, newDir);
                    for (File tempFile : tempFileNames) {
                        if (!tempFile.isDirectory()) {
                            //InputStream inFile = new FileInputStream(tempFile);
                            File destFile=new File(destDir, tempFile.getName());
                            Common.myCopyFile(tempFile, destFile, true);
                        }
                    }
                }
            }
        }


        protected boolean recoverOrders(boolean bShowMessages) {
            MySingleton g = MySingleton.getInstance();
            int loaded_cnt = 0;
            int file_error_cnt = 0;
            int data_error_cnt = 0;
            if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // получаем путь к SD
                File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                //File sdFile = new File(sdPath, g.MyDatabase.m_order_editing.uid.toString()+".txt");
                File[] listFiles = sdPath.listFiles();
                if (listFiles != null) {
                    for (File file : listFiles) {
                        if (file.getName().toLowerCase().endsWith((".txt"))) {
                            // TODO в случае если имя файла не совпадает с UID внутри документа, удалять такой файл,
                            // т.к. он всегда будет загружаться и никогда удаляться
                            String str;
                            File textFile = new File(sdPath, file.getName());
                            try {
                                str = getFileContents(file);
                                if (TextDatabase.LoadOrdersFromText(getContentResolver(), g.MyDatabase, str, 2, true) > 0)
                                    loaded_cnt++;
                                else
                                    data_error_cnt++; // ошибки в данных или состояние заявки в базе такое, что загрузка не требуется
                            } catch (IOException e) {
                                // Файловая ошибка
                                file_error_cnt++;
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "exception", e);
                                // В случае ошибки в данных удаляем файл
                                textFile.delete();
                                data_error_cnt++;
                            }
                        }
                    }
                }
            } else {
                //Toast.makeText(MainActivity.this, "Нет доступа к SD карте!", Toast.LENGTH_SHORT).show();
                if (bShowMessages)
                    publishProgress("", getResources().getString(R.string.error_no_sd_card_access));
                return false;
            }
            String comment_errors = "";
            if (file_error_cnt > 0 || data_error_cnt > 0) {
                //comment_errors=", ошибок чтения файлов: "+file_error_cnt+", ошибок в данных файлов: "+data_error_cnt;
                comment_errors = getString(R.string.error_files_cnt, file_error_cnt, data_error_cnt);
            }
            if (loaded_cnt == 0) {
                if (bShowMessages)
                    //Toast.makeText(MainActivity.this, getString(R.string.error_no_orders_to_restore)+comment_errors, Toast.LENGTH_SHORT).show();
                    publishProgress("", getString(R.string.error_no_orders_to_restore) + comment_errors);
            } else {
                //Toast.makeText(MainActivity.this, "Заказы из текстовых файлов восстановлены ("+loaded_cnt+")"+comment_errors, Toast.LENGTH_SHORT).show();
                if (bShowMessages)
                    //Toast.makeText(MainActivity.this, getString(R.string.message_orders_restored_from_files)+" ("+loaded_cnt+")"+comment_errors, Toast.LENGTH_SHORT).show();
                    publishProgress("", getString(R.string.message_orders_restored_from_files) + " (" + loaded_cnt + ")" + comment_errors);
            }
            return true;
        }


    }

    // Параметр doInBackground, прогресс, результат
    class ExchangeTask extends AsyncTask<Object, Integer, String> {

        protected ProgressDialog progressDialog;
        int prev_progress_type;
        long file_length;
        boolean bStopped;
        //String textToLog;
        ArrayList<String> textToLog = new ArrayList<String>();
        boolean cancelIsVisible;
        boolean bIniFileReaded;
        int prevProgress;
        //boolean bDontUseExternalStorage = false;//(android.os.Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q);

        String m_wifi_ftp_address;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prev_progress_type = 0;
            prevProgress = -1;
            bStopped = false;
            bIniFileReaded = false;
            cancelIsVisible = true;

            m_wifi_ftp_address = "";

            //EditText etLog = (EditText) findViewById(R.id.etTest);


            ExchangeFragment exchangeFragment=getExchangeFragment();

            if (m_bRefreshPressed) {
                m_exchangeLogText.clear();
                m_bRefreshPressed = false;
                //etLog.setText(getResources().getString(R.string.message_query_all_warning));
                m_exchangeLogText.add(getResources().getString(R.string.message_query_all_warning));
            } else {
                //etLog.setText("");
                m_exchangeLogText.clear();
            }

            if (exchangeFragment!=null) {
                exchangeFragment.setLogText(null, m_exchangeLogText);
                exchangeFragment.setButtonsExchangeEnabled(false);
            }

            //tvInfo.setText("Begin");
            //Log.d(LOG_TAG, "Begin");
            //progressDialog = ProgressDialog.show(MainActivity.this, "Autenticando", "Contactando o servidor, por favor, aguarde alguns instantes.", true, false);
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // Обмен
            progressDialog.setTitle(R.string.title_exchange);
            // Подключение к FTP серверу...
            progressDialog.setMessage(getString(R.string.ftp_state_connecting_to_server));
            progressDialog.setCancelable(false);
            progressDialog.setIcon(getResources().getDrawable(android.R.drawable.ic_popup_sync));
            progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //dialog.dismiss();
                    cancel(true);
                    if (!bStopped) {
                        ExchangeFragment exchangeFragment=getExchangeFragment();
                        // Процесс останавливается
                        m_exchangeState = getString(R.string.message_exchange_state_stopping);
                        if (exchangeFragment!=null){
                            exchangeFragment.setExchangeState(null, m_exchangeState);
                        }
                    }
                }
            });
            // Выполняется
            m_exchangeState = getString(R.string.message_exchange_state_executing);
            if (exchangeFragment!=null){
                exchangeFragment.setExchangeState(null, m_exchangeState);
            }
            progressDialog.show();

            String wifiDescr = WifiConnection.getWifiConnection(MainActivity.this);
            if (wifiDescr != null) {
                Cursor cursor = getContentResolver().query(MTradeContentProvider.SERVERS_WHEN_WIFI_CONTENT_URI, new String[]{"server_address"}, "wifi_name=?", new String[]{wifiDescr}, null);
                if (cursor.moveToNext()) {
                    //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    m_wifi_ftp_address = cursor.getString(0);
                }
                cursor.close();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Common.dismissWithExceptionHandling(progressDialog);
            bStopped = true;
            ExchangeFragment exchangeFragment=getExchangeFragment();
            if (exchangeFragment!=null) {
                // Выполнено
                m_exchangeState = getString(R.string.message_exchange_state_completed);
                exchangeFragment.setExchangeState(null, m_exchangeState);
                exchangeFragment.setButtonsExchangeEnabled(true);
            }
            if (bIniFileReaded) {
                MySingleton g = MySingleton.getInstance();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
                g.readIniFile(null, Globals.getAppContext(), sharedPreferences);
                g.readPreferences(sharedPreferences);
                // Если изменился формат
                if (!m_settings_DataFormat.equals(g.m_DataFormat)) {
                    // Очистим базу, либо заполним ее тестовыми данными
                    // 24.03.2017 - очищаем базу только если база была демо, либо становится демо-базой
                    if (m_settings_DataFormat.equals("DM") || g.m_DataFormat.equals("DM")) {
                        resetBase();
                    } else {
                        m_settings_DataFormat = g.m_DataFormat;
                        writeSettings();
                    }
                    // 24.03.2017
                    // после смены формата будет выполнен "запросить все"
                    setNegativeVersions();
                }
            }
            // 18.02.2021 пусть всегда проверяет
            // (данные могли измениться в результате обмена)
            checkGpsUpdateEveryTime();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e(LOG_TAG, "!!!!!!!!!!Cancel!!!!!!!!!!!!!!!");
            bStopped = true;
            ExchangeFragment exchangeFragment=getExchangeFragment();
            // Прервано
            m_exchangeState=getString(R.string.ftp_state_aborted);
            if (exchangeFragment!=null)
            {
                exchangeFragment.setExchangeState(null, m_exchangeState);
                exchangeFragment.setButtonsExchangeEnabled(true);
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
            boolean bNeedHideCancelButton = false;
            boolean bNeedShowCancelButton = false;
            if (progress[0] == -2) {
                //EditText etLog = (EditText) findViewById(R.id.etTest);
                //etLog.append(textToLog.get(progress[1] - 1) + "\n");
                ExchangeFragment exchangeFragment=getExchangeFragment();
                m_exchangeLogText.add(textToLog.get(progress[1] - 1));
                if (exchangeFragment!=null)
                {
                    exchangeFragment.setLogText(null, m_exchangeLogText);
                }
                return;
            }
            if (prev_progress_type != progress[0] && progress[0] != -1) {
                prev_progress_type = progress[0];
                switch (progress[0]) {
                    case FTP_STATE_LOAD_IN_BASE:
                    case FTP_STATE_LOAD_HISTORY_IN_BASE:
                        // Загрузка в базу...
                        progressDialog.setMessage(getString(R.string.ftp_state_load_in_base));
                        bNeedHideCancelButton = true;
                        break;
                    case FTP_STATE_SUCCESS:
                        // Выполнено
                        progressDialog.setMessage(getString(R.string.ftp_state_success));
                        break;
                    case FTP_STATE_FINISHED_ERROR:
                        // Выполнено с ошибками
                        progressDialog.setMessage(getString(R.string.ftp_state_finished_error));
                        bNeedShowCancelButton = true;
                        break;
                    case FTP_STATE_SEND_EOUTF:
                        // Отправка файла обмена
                        progressDialog.setMessage(getString(R.string.ftp_state_send_eoutf));
                        bNeedShowCancelButton = true;
                        break;
                    case FTP_STATE_RECEIVE_ARCH:
                        // Получение файла обмена
                        progressDialog.setMessage(getString(R.string.ftp_state_receive_arch));
                        bNeedShowCancelButton = true;
                        break;
                    case FTP_STATE_RECEIVE_IMAGE:
                        // Получение изображения номенклатуры
                        progressDialog.setMessage(getString(R.string.ftp_state_receive_image));
                        bNeedShowCancelButton = true;
                        break;
                }
            }
            if (progress.length >= 2 && prevProgress != progress[1]) {
                prevProgress = progress[1];
                progressDialog.setProgress(progress[1]);
            }

            if (!Constants.MY_DEBUG) {
                if (bNeedHideCancelButton && cancelIsVisible || bNeedShowCancelButton && !cancelIsVisible) {
                    int visibility = View.VISIBLE;
                    if ((cancelIsVisible = bNeedShowCancelButton) == false) {
                        visibility = View.GONE;
                    }
                    // Делаем невидимой кнопку
                    progressDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(visibility);
                    try {
                        // И разметку, в которой она находится (это больше для красоты)
                        // TODO свое нормальное окно прогресса
                        View v = (View) progressDialog.getButton(DialogInterface.BUTTON_NEUTRAL).getParent().getParent();
                        v.setVisibility(visibility);
                    } catch (Exception e) {
                        //
                    }
                }
            }

        }

        CopyStreamAdapter streamListener = new CopyStreamAdapter() {

            int prev_percent = 0;

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                //this method will be called everytime some bytes are transferred

                //int percent = (int)(totalBytesTransferred*100/yourFile.length());
                // update your progress bar with this percentage
                int percent = 0;
                if (file_length > 0) {
                    percent = (int) (totalBytesTransferred * 100 / file_length);
                }
                if (percent > 100)
                    percent = 100;
                if (prev_percent != percent) {
                    prev_percent = percent;
                    publishProgress(-1, percent);
                }
            }

        };

        @Override
        //protected Void doInBackground(Void... params)
        protected String doInBackground(Object... params) {
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

            MySingleton g = MySingleton.getInstance();

            int mode = (Integer) params[0];
            textToLog.clear();

            // http://chizztectep.blogspot.ru/2011/07/java-ftp-ftp-client.html
            FTPClient ftpClient = new FTPClient();

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
            ArrayList<UUID> sales_uuids = null;
            int sales_version = -1;

            if (mode == 3 || mode == 4) {
                // Обмен с веб-сервисом
                try {
                    publishProgress(FTP_STATE_SEND_EOUTF, 0);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    String web_server_address = preferences.getString("web_server_address", "192.168.0.100");
                    if (mode == 4) {
                        MyWebExchange.doInBackgroundWebService(g.MyDatabase, getApplicationContext(), (String) params[2], m_settings_agent_id, web_server_address, m_query_period_type, m_query_date_begin, m_query_date_end);
                    } else {
                        MyWebExchange.doInBackgroundWebService(g.MyDatabase, getApplicationContext(), (String) params[2], m_settings_agent_id, web_server_address, -1, "", "");
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
                    textToLog.add(getString(R.string.message_changes_loaded));
                    publishProgress(-2, textToLog.size());
                    Log.v(LOG_TAG, "OK");
                    publishProgress(FTP_STATE_SUCCESS);
                } catch (Exception e) {
                    //Log.v(LOG_TAG, androidHttpTransport.bodyOut);
                    //Log.v(LOG_TAG, androidHttpTransport.responseDump);
                    //Log.v(LOG_TAG, e.toString());
                    textToLog.add(getString(R.string.message_exchange_error) + " " + e.toString());
                    publishProgress(-2, textToLog.size());
                    //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    publishProgress(FTP_STATE_FINISHED_ERROR);
                }
                return null;
            }

            try {
                String wifi_address = g.m_FTP_server_address;
                if (!m_wifi_ftp_address.isEmpty()) {
                    wifi_address = m_wifi_ftp_address;
                }
                int portIndex = g.m_FTP_server_address.indexOf(':');
                if (portIndex < 0) {
                    ftpClient.connect(wifi_address);
                } else {
                    ftpClient.connect(wifi_address.substring(0, portIndex), Integer.parseInt(wifi_address.substring(portIndex + 1)));
                }

       			/*
       			// read the initial response (aka "Welcome message")
       			String[] welcomeMessage = ftpClient.getReplyStrings();
       			String welcomeMessageStr=Arrays.toString(welcomeMessage);
    			textToLog.add(welcomeMessageStr);
    			publishProgress(-2, textToLog.size());
    			*/

                //ftpClient.login("forftp", "lkzrgr");
                //String test=decodeLoginOrPassword(m_FTP_server_user, m_FTP_server_directory);
                if (!ftpClient.login(decodeLoginOrPassword(g.m_FTP_server_user, g.m_FTP_server_directory), decodeLoginOrPassword(g.m_FTP_server_password, g.m_FTP_server_directory)))
                    throw new Exception(getString(R.string.message_cannot_authorize_on_server));
                if (!g.m_FTP_server_directory.isEmpty()) {
                    if (!ftpClient.changeWorkingDirectory(g.m_FTP_server_directory))
                        throw new Exception(getString(R.string.message_cannot_enter_ftp_user_directory));
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
                File log_file=new File(Common.getMyStorageFileDir(MainActivity.this, "" ), "mtrade_log.txt");
                /*
                if (android.os.Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    log_file = new File(Environment.getExternalStorageDirectory(), "/mtrade_log.txt");
                } else {
                    log_file = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/mtrade_log.txt");
                }
                 */
                String newLine = "\r\n";

                FileWriter logWriter = null;
                logWriter = new FileWriter(log_file);

                readVersions();

                class ImageFileState
                {
                    public Long file_size;
                    public int state; // 1 - файл новый, загружаем, 2 - файл новый, но не был загружен
                    // 0 - файл был, не загружаем
                    // 3 - файл есть в номенклатуре, но на диске его нет
                    public int image_width;
                    public int image_height;
                    // 11.07.2019
                    // Без пути, но с расширением
                    // должно быть для Map так: [key].jpg или [key].png
                    public String image_file_name;

                    public ImageFileState(Long file_size, int state, String image_file_name)
                    {
                        this.file_size=file_size;
                        this.state=state;
                        this.image_width=0;
                        this.image_height=0;
                        this.image_file_name=image_file_name;
                    }

                    public ImageFileState(Long file_size, int state, int image_width, int image_height, String image_file_name)
                    {
                        this.file_size=file_size;
                        this.state=state;
                        this.image_width=image_width;
                        this.image_height=image_height;
                        this.image_file_name=image_file_name;
                    }

                };

                boolean bImageDataChanged = false;
                boolean bGoodsImagesMode = false;
                Map<String, ImageFileState> mapImageFiles = new HashMap<String, ImageFileState>();

                switch (mode) {
                    case 0: {
                        boolean bNothingReceived = true;
                        // Получаем данные
                        File zipFile = null;
                        FTPFile[] fileList = null;
                        {
                            String zipFileName = null;

                            publishProgress(FTP_STATE_RECEIVE_ARCH, 0);
                            Common.ftpEnterMode(ftpClient, !g.Common.VK);
                            fileList = ftpClient.listFiles();//"*.zip");
                            for (FTPFile ftpFile : fileList) {
                                if (ftpFile.getName().equalsIgnoreCase("arch.zip")) {
                                    zipFileName = ftpFile.getName();
                                    file_length = ftpFile.getSize();
                                }
                            }
                            if (zipFileName != null) {

                                File zipFileDir = Common.getMyStorageFileDir(MainActivity.this, "temp");
                                // удалим предыдущие файлы из каталога temp данных
                                File[] tempFileNames = zipFileDir.listFiles();
                                if (tempFileNames != null) {
                                    for (File tempFile : tempFileNames) {
                                        if (!tempFile.isDirectory()) {
                                            tempFile.delete();
                                        }
                                    }
                                }
                                zipFile = new File(zipFileDir, "arch.zip");

                                bNothingReceived = false;
                                OutputStream outFile = new FileOutputStream(zipFile);
                                Common.ftpEnterMode(ftpClient, !g.Common.VK);
                                ftpClient.setCopyStreamListener(streamListener);
                                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                                //ftpClient.retrieveFile(m_FTP_server_directory+"/arch.zip", outFile);
                                ftpClient.retrieveFile("arch.zip", outFile);
                                outFile.close();

                                ftpClient.deleteFile("arch.zip");
                                //ftpClient.logout();
                            }
                        }
                        if (zipFile != null) {
                            //String zipFile="/mnt/sdcard/qdata/test.zip";
                            ZipFile zip = new ZipFile(zipFile);
                            try {
                                // В архиве либо ein.txt (старый формат), либо файлы *.xml нового формата
                                // когда там ein.txt есть, то могут быть и *.xml сообщений, которые так выгружались всегда
                                boolean b_progress_start_load_showed=false;  // Это чтобы 2 раза не писать
                                boolean b_ein_txt_found=false; // Это значит другие файлы просматривать не надо
                                Enumeration<? extends ZipEntry> entries = zip.entries();
                                int totalEntries=zip.size();
                                int currentEntry=0;
                                while (entries.hasMoreElements()) {
                                    currentEntry++;
                                    ZipEntry ze = (ZipEntry) entries.nextElement();
                                    // ein.txt
                                    if (!ze.isDirectory() && ze.getName().toLowerCase(Locale.ENGLISH).equals("ein.txt")) {
                                        b_ein_txt_found=true;
                                        InputStream zin = zip.getInputStream(ze);
                                        int total = (int) ze.getSize();
                                        byte[] data = new byte[total];
                                        try {
                                            int rd = 0, block;
                                            while (total - rd > 0 && (block = zin.read(data, rd, total - rd)) != -1) {
                                                rd += block;
                                            }
                                            zin.close();
                                            if (rd == total && rd > 0) {
                                                String strUnzipped = new String(data, "windows-1251");
                                                b_progress_start_load_showed=true;
                                                publishProgress(FTP_STATE_LOAD_IN_BASE, 0);

                                                //String[] lines=strUnzipped.split("##.*##");
                                                Pattern pattern = Pattern.compile("(##.*##)");
                                                Matcher mtch = pattern.matcher(strUnzipped);
                                                int start_prev = -1;
                                                int start_this;
                                                String prevSection = "";
                                                boolean bContinue = true;
                                                while (bContinue) {
                                                    if (isCancelled())
                                                        return null;
                                                    String section = "";
                                                    if (mtch.find()) {
                                                        section = mtch.group();
                                                        start_this = mtch.start();
                                                    } else {
                                                        bContinue = false;
                                                        start_this = strUnzipped.length();
                                                    }
                                                    if (start_prev > 0) {
                                                        publishProgress(FTP_STATE_LOAD_IN_BASE, (int) ((start_prev / (float) strUnzipped.length()) * 100));
                                                        if (prevSection.equals("##CLIENTS##")) {
                                                            logWriter.append("start clients " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadClients(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end clients " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены клиенты"
                                                            textToLog.add(getString(R.string.message_clients_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##AGREEMENTS30##")) {
                                                            logWriter.append("start agreements30 " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadAgreements30(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end agreements30 " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены договоры"
                                                            textToLog.add(getString(R.string.message_agreements30_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##AGREEMENTS##")) {
                                                            logWriter.append("start agreements " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadAgreements(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end agreements " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены договоры"
                                                            textToLog.add(getString(R.string.message_agreements_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##NOMENCL##")) {
                                                            logWriter.append("start nomenclature " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadNomenclature(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end nomenclature " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.fillNomenclatureHierarchy(getContentResolver(), getResources(), "     0   ");
                                                            logWriter.append("end nomenclature hierarchy" + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружена номенклатура"
                                                            textToLog.add(getString(R.string.message_nomenclature_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                            // TODO устанавливать только в случае полной загрузки справочника
                                                            bImageDataChanged = true;
                                                        } else if (prevSection.equals("##OSTAT##")) {
                                                            if (!g.Common.DEMO) {
                                                                logWriter.append("start ostat " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                                TextDatabase.LoadOstat(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                                logWriter.append("end ostat " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            }
                                                            // "Загружены остатки"
                                                            textToLog.add(getString(R.string.message_rests_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##DOLG##")) {
                                                            logWriter.append("start dolg " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadDolg(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end dolg " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены долги"
                                                            textToLog.add(getString(R.string.message_debts_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##DOLG_EXT##")) {
                                                            logWriter.append("start dolg_ext " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadDolgExtended(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end dolg_ext " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены долги по документам"
                                                            textToLog.add(getString(R.string.message_debts_by_docs_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PRICETYPE##")) {
                                                            logWriter.append("start pricetype " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadPriceTypes(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end pricetype " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены типы цен"
                                                            textToLog.add(getString(R.string.message_price_types_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##DISCOUNTS##")) {
                                                            logWriter.append("start discounts " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadSimpleDiscounts(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end discounts " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены скидки"
                                                            textToLog.add(getString(R.string.message_discounts_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PRICE##")) {
                                                            if (!g.Common.DEMO) {
                                                                logWriter.append("start price " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                                TextDatabase.LoadPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                                logWriter.append("end price " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            }
                                                            // "Загружены цены"
                                                            textToLog.add(getString(R.string.message_prices_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PRICE_AGREEMENTS30##")) {
                                                            logWriter.append("start price_agreements30 " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadPricesAgreements30(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end price_agreements30 " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены цены по соглашениям"
                                                            textToLog.add(getString(R.string.message_prices_by_agreements30_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##STOCKS##")) {
                                                            logWriter.append("start stocks " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadStocks(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end stocks " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены склады"
                                                            textToLog.add(getString(R.string.message_contracts_for_distribs_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##DISTR_CONTR##")) {
                                                            logWriter.append("start stocks " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadDistribsContracts(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end stocks " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены контракты для дистрибьюции"
                                                            textToLog.add(getString(R.string.message_contracts_for_distribs_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##AGENTS##")) {
                                                            logWriter.append("start agents " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadAgents(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end agents " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены агенты"
                                                            textToLog.add(getString(R.string.message_agents_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##CURATORS##")) {
                                                            logWriter.append("start curators " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadCurators(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end curators " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены кураторы"
                                                            textToLog.add(getString(R.string.message_curators_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##D_POINTS##")) {
                                                            logWriter.append("start d.points " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadDistrPoints(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end d.points " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены торговые точки"
                                                            textToLog.add(getString(R.string.message_distr_points_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##ORGANIZATIONS##")) {
                                                            logWriter.append("start organizations " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadOrganizations(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end organizations " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены организации"
                                                            textToLog.add(getString(R.string.message_organizations_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PRICE_CLIENT##")) {
                                                            logWriter.append("start clients price " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadClientsPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end clients price" + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружен прайс клиентов"
                                                            textToLog.add(getString(R.string.message_clients_prices_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PRICE_CURATOR##")) {
                                                            logWriter.append("start curators price " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadCuratorsPrice(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end curators price" + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружен прайс кураторов"
                                                            textToLog.add(getString(R.string.message_curators_prices_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##ROUTES##")) {
                                                            logWriter.append("start routes " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadRoutes(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end routes " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены маршруты"
                                                            textToLog.add(getString(R.string.message_routes_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##ROUTES_DATES##")) {
                                                            logWriter.append("start routes dates " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadRoutesDates(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end routes dates " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены даты маршрутов"
                                                            textToLog.add(getString(R.string.message_routes_dates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##MESSAGES##")) {
                                                            logWriter.append("start messages " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadMessages(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end messages " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены сообщения"
                                                            textToLog.add(getString(R.string.message_messages_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##SALES##")) {
                                                            logWriter.append("start sales history " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            sales_uuids = new ArrayList<UUID>();
                                                            sales_version = TextDatabase.LoadSalesHistoryHeader(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), sales_uuids);
                                                            // удалим другие версии
                                                            getContentResolver().delete(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, "ver<>?", new String[]{String.valueOf(sales_version)});
                                                            //
                                                            getContentResolver().delete(MTradeContentProvider.SALES_LOADED_CONTENT_URI, "ver<>?", new String[]{String.valueOf(sales_version)});
                                                            //
                                                            logWriter.append("end sales history " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружен заголовок истории продаж"
                                                            textToLog.add(getString(R.string.message_sales_history_header_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##ORDERS_M##")) {
                                                            logWriter.append("start orders update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            ArrayList<UUID> uuids = new ArrayList<UUID>();

                                                            TextDatabase.LoadOrdersUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), uuids);
                                                            // Это устаревшее (файлов в папке orders сейчас нет), но оставлю
                                                            //if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                                                // получаем путь к SD
                                                                //File sdPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/mtrade/orders");
                                                                File sdPath = Common.getMyStorageFileDir(MainActivity.this, "orders");
                                                                // Удаляем файлы всех загруженных заявок
                                                                for (UUID uuid : uuids) {
                                                                    File sdFile = new File(sdPath, uuid.toString() + ".txt");
                                                                    sdFile.delete();
                                                                }
                                                            //}
                                                            logWriter.append("end orders update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления заказов"
                                                            textToLog.add(getString(R.string.message_orders_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##DISTRIB_M##")) {
                                                            logWriter.append("start distribs update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            ArrayList<UUID> uuids = new ArrayList<UUID>();
                                                            TextDatabase.LoadDistribsUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), uuids);
                                                            logWriter.append("end distribs update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления дистрибьюции"
                                                            textToLog.add(getString(R.string.message_distribs_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##SUM_SHIPPING_NOTIFICATIONS_M##")) {
                                                            logWriter.append("start shipping notification update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadSumShippingNotification(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this));
                                                            logWriter.append("end shipping notification update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления сумм отгрузок по заказам"
                                                            textToLog.add(getString(R.string.message_shipping_notifications_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##PAYMENTS_M##")) {
                                                            logWriter.append("start payments update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            ArrayList<UUID> uuidsPayments = new ArrayList<UUID>();
                                                            TextDatabase.LoadPaymentsUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), uuidsPayments);
                                                            logWriter.append("end payments update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления платежей"
                                                            textToLog.add(getString(R.string.message_payments_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##REFUNDS_M##")) {
                                                            logWriter.append("start refunds update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            ArrayList<UUID> uuids = new ArrayList<UUID>();

                                                            TextDatabase.LoadRefundsUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), uuids);
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
                                                            logWriter.append("end refunds update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления заказов на возврат"
                                                            textToLog.add(getString(R.string.message_refunds_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##VISITS_M##")) {
                                                            logWriter.append("start visits update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            ArrayList<UUID> uuids = new ArrayList<UUID>();
                                                            TextDatabase.LoadVisitsUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), uuids);
                                                            logWriter.append("end visits update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления заказов на возврат"
                                                            textToLog.add(getString(R.string.message_vists_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##VICARIOUS_POWER##")) {
                                                            logWriter.append("start vicarious power update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadVicariousPower(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end vicarious power update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_vicarious_powers_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##GPS##")) {
                                                            logWriter.append("start gps update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            int newGpsUpdateInterval = TextDatabase.LoadGpsUpdate(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this));
                                                            if (newGpsUpdateInterval != m_settings_gps_interval) {
                                                                m_settings_gps_interval = newGpsUpdateInterval;
                                                                writeSettings();
                                                                if (m_settings_gps_interval < 0) {
                                                                    g.Common.HAVE_GPS_SETTINGS = true;
                                                                }
                                                                //checkGpsUpdateEveryTime();
                                                            }
                                                            logWriter.append("end gps update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления gps"
                                                            textToLog.add(getString(R.string.message_gps_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##EQUIPMENT##")) {
                                                            logWriter.append("start equipment update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadEquipment(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end equipment update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_equipment_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else if (prevSection.equals("##OSTAT_TE##")) {
                                                            logWriter.append("start equipment rests update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            TextDatabase.LoadEquipmentRests(getContentResolver(), g.MyDatabase, strUnzipped.substring(start_prev, start_this), true);
                                                            logWriter.append("end equipment rests update " + DateFormat.getDateTimeInstance().format(new Date()) + newLine);
                                                            // "Загружены обновления доверенностей"
                                                            textToLog.add(getString(R.string.message_equipment_rests_updates_loaded));
                                                            publishProgress(-2, textToLog.size());
                                                        } else {
                                                            //Toast.makeText(MainActivity.this, "ERR: "+prevSection, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                    start_prev = start_this + section.length();
                                                    prevSection = section;
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
                                    if (!ze.isDirectory() && ze.getName().toLowerCase(Locale.ENGLISH).equals("mtrade.ini")) {
			    	        		/*
			    	        		InputStream is = zip.getInputStream(ze);
			    	        		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			    	        		String str;
			    	        		while ((str = reader.readLine()) != null) {
			    	                    //buf.append(str + "\n" );
			    	                }
			    	        		is.close();
			    	        		*/
                                        InputStream is = zip.getInputStream(ze);

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
                                        OutputStream os = new FileOutputStream(new File(Common.getMyStorageFileDir(MainActivity.this, ""), "mtrade.ini"));
                                        int read = 0;
                                        byte[] bytes = new byte[1024];

                                        while ((read = is.read(bytes)) != -1) {
                                            os.write(bytes, 0, read);
                                        }
                                        os.close();

                                        is.close();
                                        bIniFileReaded = true;
                                    }
                                    // Сообщения и т.п. *.xml
                                    if (!ze.isDirectory() && ze.getName().length() > 4 && ze.getName().toLowerCase(Locale.ENGLISH).substring(ze.getName().length() - 4).equals(".xml")) {

                                        if (!b_progress_start_load_showed)
                                        {
                                            publishProgress(FTP_STATE_LOAD_IN_BASE, 0);
                                            b_progress_start_load_showed=true;
                                        }

                                        InputStream is = zip.getInputStream(ze);
                                        // Убедимся, что перед '<' нет лишних символов
                                        int total = (int) ze.getSize();
                                        int offset = 0;
                                        if (total > 10) {
                                            total = 10;
                                        }
                                        byte[] data = new byte[total];
                                        int read = is.read(data, 0, total);
                                        int i;
                                        for (i = 0; i < read; i++) {
                                            if (data[i] == '<') {
                                                offset = i;
                                                break;
                                            }
                                        }
                                        is.close();
                                        is = zip.getInputStream(ze);
                                        if (offset != 0)
                                            is.skip(offset);
                                        long timeBefore=new Date().getTime();
                                        TextDatabase.ResultLoadXML resultLoadXML=TextDatabase.LoadXML(getApplicationContext(), g.MyDatabase, is, true);
                                        if (resultLoadXML.bSuccess) {
                                            long timeDiff=new Date().getTime()-timeBefore;
                                            textToLog.add(resultLoadXML.ResultMessage+", "+Common.DoubleToStringFormat(timeDiff/1000.0, "%.2f")+" sec.");
                                            publishProgress(-2, textToLog.size());
                                            // Это не новая загрузка, это просто загрузка сообщений в формате *.xml, что было всегда
                                            // и здесь никакого прогресса не выводится
                                            if (!b_ein_txt_found)
                                            {
                                                publishProgress(FTP_STATE_LOAD_IN_BASE, (int) ((currentEntry / (float) totalEntries) * 100));

                                            }
                                            switch (resultLoadXML.xmlMode) {
                                                case E_MODE_NOMENCLATURE:
                                                    // TODO(старый комментарий) устанавливать только в случае полной загрузки справочника
                                                    bImageDataChanged = true;
                                                    break;
                                                case E_MODE_GPS_M:
                                                    if (resultLoadXML.nResult != m_settings_gps_interval) {
                                                        m_settings_gps_interval = resultLoadXML.nResult;
                                                        writeSettings();
                                                        if (m_settings_gps_interval < 0) {
                                                            g.Common.HAVE_GPS_SETTINGS = true;
                                                        } else
                                                            // вот, честно говоря, не помню, почему
                                                            // в других местах этот флаг не сбрасывается
                                                            // да и может быть оно вообще нигде не используется
                                                            g.Common.HAVE_GPS_SETTINGS = false;
                                                    }
                                                    break;

                                            }

                                            if (resultLoadXML.uuids!=null)
                                            {
                                                switch (resultLoadXML.xmlMode) {
                                                    case E_MODE_ORDERS_M:
                                                        break;
                                                    case E_MODE_GPS_M:
                                                        // TODO uuids будут?
                                                        break;
                                                    case E_MODE_SALES_HISTORY_HEADERS:
                                                        // 21.09.2022
                                                        sales_uuids=resultLoadXML.uuids;
                                                        break;
                                                }
                                            }
                                        }
                                        //
                                        is.close();
                                    }

                                }

                                if (b_progress_start_load_showed) {
                                    publishProgress(FTP_STATE_LOAD_IN_BASE, 100);
                                    publishProgress(FTP_STATE_SUCCESS);
                                }


                                boolean bReceivedSales = false;
                                if (sales_uuids != null) {
                                    bReceivedSales = true;
                                    int historyIdx = 0;
                                    for (UUID uid : sales_uuids) {
                                        boolean bReceivedByUid = false;
                                        // Принимаем файлы продажи, если они не загружены
                                        Cursor verCursor = getContentResolver().query(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, new String[]{"ver"}, "param=?", new String[]{uid.toString()}, null);
                                        if (verCursor.moveToFirst()) {
                                            // Данные уже были загружены ранее
                                            bReceivedByUid = true;
                                        } else {
                                            File historyZipFile;
                                            File zipFileDir = Common.getMyStorageFileDir(MainActivity.this, "temp");
                                            historyZipFile = new File(zipFileDir, uid.toString() + ".zip");

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
                                            file_length = 0;
                                            // большие-маленькие различаются
                                            String history_file_name = uid.toString() + ".zip";
                                            for (FTPFile ftpFile : fileList) {
                                                if (ftpFile.getName().equalsIgnoreCase(uid.toString() + ".zip")) {
                                                    file_length = ftpFile.getSize();
                                                    history_file_name = ftpFile.getName();
                                                    break;
                                                }
                                            }
                                            publishProgress(FTP_STATE_RECEIVE_ARCH, 0);
                                            OutputStream outFile = new FileOutputStream(historyZipFile);
                                            Common.ftpEnterMode(ftpClient, !g.Common.VK);
                                            ftpClient.setCopyStreamListener(streamListener);
                                            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                                            if (ftpClient.retrieveFile(history_file_name, outFile)) {
                                                outFile.flush();
                                                textToLog.add("Загружен файл истории продаж " + uid.toString());
                                                publishProgress(-2, textToLog.size());

                                            } else {
                                                textToLog.add("Не удалось загрузить файл истории продаж " + uid.toString());
                                                publishProgress(-2, textToLog.size());
                                                file_length = 0;
                                            }
                                            outFile.close();

                                            if (file_length > 0) {
                                                publishProgress(FTP_STATE_LOAD_HISTORY_IN_BASE, 0);

                                                ZipFile historyZip = new ZipFile(historyZipFile);
                                                try {
                                                    Enumeration<? extends ZipEntry> historyEntries = historyZip.entries();
                                                    boolean bHistoryZipReaded = false;
                                                    while (historyEntries.hasMoreElements()) {
                                                        ZipEntry ze = (ZipEntry) historyEntries.nextElement();
                                                        // sales.txt
                                                        if (!ze.isDirectory() && ze.getName().toLowerCase(Locale.ENGLISH).equals("sales.txt")) {
                                                            InputStream zin = historyZip.getInputStream(ze);
                                                            int total = (int) ze.getSize();
                                                            byte[] data = new byte[total];
                                                            try {
                                                                int rd = 0, block;
                                                                while (total - rd > 0 && (block = zin.read(data, rd, total - rd)) != -1) {
                                                                    rd += block;
                                                                }
                                                                zin.close();
                                                                if (rd == total && rd > 0) {
                                                                    String strUnzipped = new String(data, "windows-1251");
                                                                    TextDatabase.LoadSalesHistory(getContentResolver(), g.MyDatabase, strUnzipped, uid, sales_version);
                                                                    bReceivedByUid = true;
                                                                    // Установим версию фрагмента
                                                                    ContentValues cv = new ContentValues();
                                                                    cv.put("param", uid.toString());
                                                                    cv.put("ver", sales_version);
                                                                    getContentResolver().insert(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, cv);
                                                                    bHistoryZipReaded = true;
                                                                }
                                                            } catch (IOException e) {
                                                                zin.close();
                                                            }
                                                        }
                                                    }
                                                    if (bHistoryZipReaded) {
                                                        //ftpClient.deleteFile(uid.toString()+".zip");
                                                        ftpClient.deleteFile(history_file_name);
                                                    }
                                                } catch (IOException e) {
                                                    historyZip.close();
                                                }
                                            }
                                            bReceivedSales &= bReceivedByUid;
                                            publishProgress(FTP_STATE_LOAD_HISTORY_IN_BASE, historyIdx * 100 / sales_uuids.size());
                                            historyIdx++;
                                        }
                                        verCursor.close();
                                        publishProgress(FTP_STATE_LOAD_HISTORY_IN_BASE, 100);
                                    }
                                    if (bReceivedSales) {
                                        g.MyDatabase.m_sales_loaded_version = sales_version;
                                        ContentValues cv = new ContentValues();
                                        cv.put("param", "SALES_LOADED");
                                        cv.put("ver", g.MyDatabase.m_sales_loaded_version);
                                        Uri newUri = getContentResolver().insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
                                        // и пересчитываем продажи
                                        getContentResolver().insert(MTradeContentProvider.CREATE_SALES_L_URI, null);
                                    }
                                }
                                publishProgress(FTP_STATE_SUCCESS);
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
                                        ftpClient.logout();
                                        ftpClient.disconnect();
                                    }
                                } catch (IOException e) {
                                    Log.e(LOG_TAG, "Ошибка при отключении от сервера", e);
                                    e.printStackTrace();
                                }
                                // Увеличим счетчик у удаленных сообщений в случае успешного получения архива
                                // (это происходит при записи isMark=1 - isMarkCnt увеличивается на 1)
                                ContentValues cv = new ContentValues();
                                cv.put("isMark", 1);
                                getContentResolver().update(MTradeContentProvider.MESSAGES_CONTENT_URI, cv, "isMark=1", new String[]{});
                                getContentResolver().delete(MTradeContentProvider.MESSAGES_CONTENT_URI, "isMarkCnt>10", new String[]{});
                                // этот вызов не может быть в потоке, поэтому перенесен сюда
                                // 18.02.2021 вообще перенесено на после обмена
                                //checkGpsUpdateEveryTime();
                            } catch (Exception e) {
                                Log.e(LOG_TAG, "Ошибка при работе с архивом", e);
                                e.printStackTrace();
                                // Ошибка при работе с архивом
                                textToLog.add(getString(R.string.message_archive_error) + " " + e.toString());
                                publishProgress(-2, textToLog.size());
                                //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                publishProgress(FTP_STATE_FINISHED_ERROR);
                            } finally {
                                zip.close();
                            }

                        } else //zipFileName==null
                        {
                            ftpClient.logout();
                        }
                        //logWriter.flush();
                        //logWriter.close();
                        if (bNothingReceived) {
                            // Ничего не получено, но и ошибок тоже нет
                            // Успешное подключение, данных к получению нет
                            textToLog.add(getString(R.string.ftp_message_success_no_data));
                            publishProgress(-2, textToLog.size());
                        }
                    }
                    break;
                    case 1: {

                        // Это отличается от того, что пользователь настраивает
                        // значения через getDefaultSharedPreferences
                        final SharedPreferences pref = getSharedPreferences("MTradePreferences", 0);
                        //SharedPreferences.Editor pref_editor = pref.edit();

                        // отправляем данные
                        publishProgress(FTP_STATE_SEND_EOUTF, 0);
                        // удаляем предыдущий файл, если он был
                        ftpClient.deleteFile("arch.zip");
                        File zipFile;
                        File zipFileDir = Common.getMyStorageFileDir(MainActivity.this, "temp");
                        zipFile = new File(zipFileDir, "eoutf.zip");
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

                        ZipOutputStream zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
                        zipStream.setMethod(ZipOutputStream.DEFLATED);
                        zipStream.setLevel(Deflater.DEFAULT_COMPRESSION);
                        try {

                            ArrayList<UUID> uuids = new ArrayList();
                            ArrayList<Long> idsVisits = new ArrayList();
                            ArrayList<UUID> uuidsPayments = new ArrayList();
                            ArrayList<UUID> uuidsRefunds = new ArrayList();
                            ArrayList<UUID> uuidsDistribs = new ArrayList();

                            // Текстовый файл
                            ZipEntry ze = new ZipEntry("eout.txt");
                            zipStream.putNextEntry(ze);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bytes, "cp1251"));
                            // Версии
                            bytes.reset();
                            TextDatabase.SaveVersions(bw, g.MyDatabase);
                            bw.flush();
                            zipStream.write(bytes.toByteArray());
                            // Сообщения
                            bytes.reset();
                            TextDatabase.SaveSendMessages(bw, getContentResolver(), g.MyDatabase, m_bExcludeImageMessages);
                            bw.flush();
                            zipStream.write(bytes.toByteArray());
                            // Заказы отправляемые
                            bytes.reset();
                            TextDatabase.SaveSendOrders(bw, getContentResolver(), g.MyDatabase, uuids);
                            bw.flush();
                            zipStream.write(bytes.toByteArray());
                            // Подтверждение сумм реализаций на основании заказов
                            if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size() > 0) {
                                bytes.reset();
                                bw.write("##SUM_SHIPPING_NOTIFICATIONS##\r\n");
                                for (Map.Entry<UUID, Double> it : g.MyDatabase.m_acks_shipping_sums.entrySet()) {
                                    bw.write(String.format("%s#%.2f\r\n", it.getKey().toString(), it.getValue()));
                                }
                                bw.flush();
                                zipStream.write(bytes.toByteArray());
                                // Очистка перенесена ниже
                                //g.MyDatabase.m_acks_shipping_sums.clear();
                            }
                            // Платежи отправляемые
                            if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                                bytes.reset();
                                TextDatabase.SaveSendPayments(bw, getContentResolver(), g.MyDatabase, uuidsPayments);
                                bw.flush();
                                zipStream.write(bytes.toByteArray());
                            }
                            // Возвраты отправляемые
                            bytes.reset();
                            TextDatabase.SaveSendRefunds(bw, getContentResolver(), g.MyDatabase, uuidsRefunds);
                            bw.flush();
                            zipStream.write(bytes.toByteArray());
                            // Визиты отправляемые
                            bytes.reset();
                            TextDatabase.SaveSendVisits(bw, getContentResolver(), g.MyDatabase, idsVisits);
                            bw.flush();
                            zipStream.write(bytes.toByteArray());
                            // Дистрибьюции отправляемые
                            if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                bytes.reset();
                                TextDatabase.SaveSendDistribs(bw, getContentResolver(), g.MyDatabase, uuidsDistribs);
                                bw.flush();
                                zipStream.write(bytes.toByteArray());
                            }
                            // Раздел "Разное"
                            if (g.Common.PRODLIDER) {
                                bytes.reset();
                                bw.write("##MISC##\r\n");
                                bw.write(String.format("DeviceName=%s\r\n", Common.getDeviceName()));
                                String fcm_instanceId=pref.getString("fcm_instanceId", "");
                                if (fcm_instanceId!=null&&!fcm_instanceId.isEmpty())
                                {
                                    bw.write(String.format("FCM_InstanceId=%s\r\n", fcm_instanceId));
                                }
                                bw.flush();
                                zipStream.write(bytes.toByteArray());
                            }

                            // увеличиваем счетчик отправленных файлов
                            g.MyDatabase.m_sent_count++;
                            ContentValues cv_ver = new ContentValues();
                            cv_ver.clear();
                            cv_ver.put("param", "SENT_COUNT");
                            cv_ver.put("ver", g.MyDatabase.m_sent_count);
                            getContentResolver().insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv_ver);
                            //
                            if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                // GPS координаты
                                bytes.reset();
                                bw.write("##GPS##\r\n");
                                TextDatabase.SaveSendGps(bw, getContentResolver(), g.MyDatabase, g.MyDatabase.m_sent_count);
                                bw.flush();
                                zipStream.write(bytes.toByteArray());
                            }
                            // Конец файла
                            bytes.reset();
                            bw.write("##EOF##");
                            bw.flush();
                            zipStream.write(bytes.toByteArray());

                            zipStream.closeEntry();
                            zipStream.flush();

                            if (g.Common.PRODLIDER||g.Common.VK||g.Common.PRAIT||g.Common.FACTORY) {
                                // XML файл
                                ze = new ZipEntry("versions.xml");
                                zipStream.putNextEntry(ze);
                                TextDatabase.SaveVersionsXML(zipStream, g.MyDatabase);
                                zipStream.closeEntry();
                                zipStream.flush();

                                ze = new ZipEntry("send_messages.xml");
                                zipStream.putNextEntry(ze);
                                TextDatabase.SaveSendMessagesXML(zipStream, getContentResolver(), g.MyDatabase, m_bExcludeImageMessages);
                                zipStream.closeEntry();
                                zipStream.flush();

                                ze = new ZipEntry("orders.xml");
                                zipStream.putNextEntry(ze);
                                TextDatabase.SaveSendOrdersXML(zipStream, getContentResolver(), g.MyDatabase, uuids);
                                zipStream.closeEntry();
                                zipStream.flush();

                                // Подтверждение сумм реализаций на основании заказов
                                // TODO не очищать в коде выше m_acks_shipping_sums
                                if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size() > 0) {
                                    ze = new ZipEntry("sum_shipping_notifications.xml");
                                    zipStream.putNextEntry(ze);
                                    TextDatabase.SaveSendSumShippingNotificationsXML(zipStream, g.MyDatabase.m_acks_shipping_sums);
                                    zipStream.closeEntry();
                                    zipStream.flush();
                                }
                                // Платежи отправляемые
                                if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
                                    ze = new ZipEntry("payments.xml");
                                    zipStream.putNextEntry(ze);
                                    TextDatabase.SaveSendPaymentsXML(zipStream, getContentResolver(), g.MyDatabase, uuidsPayments);
                                    zipStream.closeEntry();
                                    zipStream.flush();
                                }
                                // Возвраты отправляемые
                                ze = new ZipEntry("refunds.xml");
                                zipStream.putNextEntry(ze);
                                TextDatabase.SaveSendRefundsXML(zipStream, getContentResolver(), g.MyDatabase, uuidsRefunds);
                                zipStream.closeEntry();
                                zipStream.flush();
                                // Визиты отправляемые
                                ze = new ZipEntry("visits.xml");
                                zipStream.putNextEntry(ze);
                                TextDatabase.SaveSendVisitsXML(zipStream, getContentResolver(), g.MyDatabase, idsVisits);
                                zipStream.closeEntry();
                                zipStream.flush();
                                // Дистрибьюции отправляемые
                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    ze = new ZipEntry("distribs.xml");
                                    zipStream.putNextEntry(ze);
                                    TextDatabase.SaveSendDistribsXML(zipStream, getContentResolver(), g.MyDatabase, uuidsDistribs);
                                    zipStream.closeEntry();
                                    zipStream.flush();
                                }

                                // Раздел "Разное"
                                ze = new ZipEntry("misc.xml");
                                zipStream.putNextEntry(ze);
                                String fcm_instanceId=pref.getString("fcm_instanceId", "");
                                TextDatabase.SaveSendMiscXML(zipStream, fcm_instanceId);
                                zipStream.closeEntry();
                                zipStream.flush();

                                if (g.Common.PRODLIDER || g.Common.TANDEM) {
                                    // GPS координаты
                                    ze = new ZipEntry("gps.xml");
                                    zipStream.putNextEntry(ze);
                                    TextDatabase.SaveSendGpsXML(zipStream, getContentResolver(), g.MyDatabase, g.MyDatabase.m_sent_count);
                                    zipStream.closeEntry();
                                    zipStream.flush();

                                }

                                // Очистка полей (т.к. в двух местах выгрузка происходит, то очистка сделана отдельно)
                                // Подтверждение сумм реализаций на основании заказов
                                if ((g.Common.PRODLIDER || g.Common.TANDEM) && g.MyDatabase.m_acks_shipping_sums.size() > 0) {
                                    g.MyDatabase.m_acks_shipping_sums.clear();
                                }
                                g.MyDatabase.m_empty_orders.clear();
                                g.MyDatabase.m_empty_refunds.clear();
                                g.MyDatabase.m_empty_payments.clear();
                                g.MyDatabase.m_empty_visits.clear();
                                g.MyDatabase.m_empty_distribs.clear();

                            }

                            zipStream.close();

                            ZipRepair.fixInvalidZipFile(zipFile);

                            boolean bSuccessSend = true;

                            if (!m_bExcludeImageMessages) {
                                // сначала отправляем вложенные файлы и архив
                                // вложенные файлы

                                File attachesFileDir = Common.getMyStorageFileDir(MainActivity.this, "attaches");
                                // отправляем все файлы из папки в памяти устройства
                                File[] tempFileNames = attachesFileDir.listFiles();
                                if (tempFileNames != null) {
                                    for (File tempFile : tempFileNames) {
                                        if (!tempFile.isDirectory()) {
                                            InputStream inFile = new FileInputStream(tempFile);
                                            file_length = tempFile.length();
                                            Common.ftpEnterMode(ftpClient, !g.Common.VK);
                                            ftpClient.setCopyStreamListener(streamListener);
                                            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

                                            if (ftpClient.storeFile(tempFile.getName(), inFile)) {
                                                tempFile.delete();
                                            } else {
                                                bSuccessSend = false;
                                                // Не удалось отправить файл
                                                textToLog.add(getString(R.string.message_file_not_sent));
                                                publishProgress(-2, textToLog.size());
                                            }
                                        }
                                    }
                                }

                            }

                            if (bSuccessSend) {
                                // архив
                                InputStream inFile = new FileInputStream(zipFile);
                                file_length = zipFile.length();
                                Common.ftpEnterMode(ftpClient, !g.Common.VK);
                                ftpClient.setCopyStreamListener(streamListener);
                                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                                //ftpClient.storeFile(m_FTP_server_directory+"/eoutf.zip", inFile);
                                if (ftpClient.storeFile("eoutf.zip", inFile)) {
                                    // При успешной отправке установим признак
                                    for (UUID uuid : uuids) {
                                        // Состояние переключим в "Отправлен", было если "Создан" или "Согласование"
                                        ContentValues cv = new ContentValues();
                                        cv.put("state", E_ORDER_STATE.E_ORDER_STATE_SENT.value());
                                        getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "uid=? and (state=? or state=?)", new String[]{uuid.toString(), String.valueOf(E_ORDER_STATE.E_ORDER_STATE_CREATED.value()), String.valueOf(E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value())});
                                        // для выполненных если меняют, например, комментарий и заказ уходит, состояние не изменится
                                        // установим version_ack=version
                                        Cursor versionCursor = getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"version", "versionPDA"}, "uid=?", new String[]{uuid.toString()}, null);
                                        if (versionCursor.moveToFirst()) {
                                            cv.clear();
                                            cv.put("version_ack", versionCursor.getString(0));
                                            cv.put("versionPDA", versionCursor.getString(1)); // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "uid=?", new String[]{uuid.toString()});
                                        }
                                        versionCursor.close();
                                    }
                                    // При успешной отправке установим признак
                                    for (UUID uuid : uuidsPayments) {
                                        // Состояние переключим в "Отправлен", если было "Создан"
                                        ContentValues cv = new ContentValues();
                                        cv.put("state", E_PAYMENT_STATE.E_PAYMENT_STATE_SENT.value());
                                        getContentResolver().update(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, cv, "uid=? and (state=?)", new String[]{uuid.toString(), String.valueOf(E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED.value())});
                                        // для выполненных если меняют, например, комментарий и заказ уходит, состояние не изменится
                                        // установим version_ack=version
                                        Cursor versionCursor = getContentResolver().query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"version", "versionPDA"}, "uid=?", new String[]{uuid.toString()}, null);
                                        if (versionCursor.moveToFirst()) {
                                            cv.clear();
                                            cv.put("version_ack", versionCursor.getString(0));
                                            cv.put("versionPDA", versionCursor.getString(1)); // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, cv, "uid=?", new String[]{uuid.toString()});
                                        }
                                        versionCursor.close();
                                    }
                                    // При успешной отправке установим признак
                                    for (UUID uuid : uuidsRefunds) {
                                        // Состояние переключим в "Отправлен", если было "Создан" или "Согласование"
                                        ContentValues cv = new ContentValues();
                                        cv.put("state", E_REFUND_STATE.E_REFUND_STATE_SENT.value());
                                        getContentResolver().update(MTradeContentProvider.REFUNDS_CONTENT_URI, cv, "uid=? and (state=? or state=?)", new String[]{uuid.toString(), String.valueOf(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()), String.valueOf(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value())});
                                        // установим version_ack=version
                                        Cursor versionCursor = getContentResolver().query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"version", "versionPDA"}, "uid=?", new String[]{uuid.toString()}, null);
                                        if (versionCursor.moveToFirst()) {
                                            cv.clear();
                                            cv.put("version_ack", versionCursor.getString(0));
                                            cv.put("versionPDA", versionCursor.getString(1)); // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(MTradeContentProvider.REFUNDS_CONTENT_URI, cv, "uid=?", new String[]{uuid.toString()});
                                        }
                                        versionCursor.close();
                                    }
                                    // При успешной отправке установим признак
                                    for (UUID uuid : uuidsDistribs) {
                                        // Состояние переключим в "Отправлен", если было "Создан"
                                        ContentValues cv = new ContentValues();
                                        cv.put("state", E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value());
                                        getContentResolver().update(MTradeContentProvider.DISTRIBS_CONTENT_URI, cv, "uid=? and (state=? or state=?)", new String[]{uuid.toString(), String.valueOf(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value())});
                                        // установим version_ack=version
                                        Cursor versionCursor = getContentResolver().query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"version", "versionPDA"}, "uid=?", new String[]{uuid.toString()}, null);
                                        if (versionCursor.moveToFirst()) {
                                            cv.clear();
                                            cv.put("version_ack", versionCursor.getString(0));
                                            cv.put("versionPDA", versionCursor.getString(1)); // если этого поля не будет, счетчик увеличится на единцу

                                            getContentResolver().update(MTradeContentProvider.DISTRIBS_CONTENT_URI, cv, "uid=?", new String[]{uuid.toString()});
                                        }
                                        versionCursor.close();
                                    }

                                    // При успешной отправке установим признак
                                    for (Long _id : idsVisits) {
                                        ContentValues cv = new ContentValues();
                                        // установим version_ack=version
                                        Cursor versionCursor = getContentResolver().query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"version"}, "_id=?", new String[]{Long.toString(_id)}, null);
                                        if (versionCursor.moveToFirst()) {
                                            cv.clear();
                                            cv.put("version_ack", versionCursor.getString(0));
                                            getContentResolver().update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
                                        }
                                        versionCursor.close();
                                    }


                                } else {
                                    // Не удалось отправить файл
                                    textToLog.add(getString(R.string.message_file_not_sent_reply_code, ftpClient.getReplyCode() ));
                                    publishProgress(-2, textToLog.size());
                                }
                                inFile.close();
                            } // bSuccessSend
                        } catch (IOException e) {
                            // Файловая ошибка
                            textToLog.add(getString(R.string.message_file_error) + " " + e.toString());
                            publishProgress(-2, textToLog.size());
                        }
                        ftpClient.logout();
                        publishProgress(FTP_STATE_SUCCESS);
                    }
                    break;
                    case 2: {
                        // Синхронизируем изображения
                        try {
                            if (!ftpClient.changeWorkingDirectory("images")) {
                                if (g.Common.TANDEM) {
                                    // если каталог не задан, значит мы зашли собственным пользователем
                                    // и надо выйти на уровень выше
                                    //if (m_FTP_server_directory.isEmpty())
                                    {
                                        if (!ftpClient.changeWorkingDirectory("..")) {
                                            throw new Exception("Не удалось зайти в каталог изображений FTP");
                                        }
                                    }
                                } else if (g.Common.PRODLIDER) {
                                    // если каталог не задан, значит мы зашли собственным пользователем
                                    // и надо выйти на уровень выше
                                    if (!ftpClient.changeWorkingDirectory("..")) {
                                        throw new Exception("Не удалось зайти в каталог изображений FTP");
                                    }
                                    if (g.m_FTP_server_directory.isEmpty()) {
                                        if (!ftpClient.changeWorkingDirectory("Images")) {
                                            throw new Exception("Не удалось зайти в каталог изображений FTP");
                                        }
                                    }
                                } else {
                                    throw new Exception("Не удалось зайти в каталог images FTP");
                                }
                            }

                            // Имена существующих на FTP сервере файлов изображений
                            //mapImageFiles= new HashMap<String, Integer>();
                            bGoodsImagesMode = true; // Раньше HashMap уничтожался при выходе за скобки, и туда записывался NULL
                            // поэтому создание HashMap вынесено выше, и применен флаг

                            // Имена существующих на FTP сервере файлов изображений
                            //String ftp_image_names[]=new String[]{};

                            File imagesPath=Common.getMyStorageFileDir(MainActivity.this, "goods");
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
                            FTPFile[] fileList = null;
                            publishProgress(FTP_STATE_RECEIVE_IMAGE, 0);
                            Common.ftpEnterMode(ftpClient, !g.Common.VK);
                            fileList = ftpClient.listFiles();
                            for (FTPFile ftpFile : fileList) {
                                String ftpName = ftpFile.getName();
                                String id = "";
                                if (ftpName.toLowerCase().endsWith(".jpg")||ftpName.toLowerCase().endsWith(".png")) {
                                    id = g.Common.fileNameToId(ftpName.substring(0, ftpName.lastIndexOf(".")));
                                }
                                if (!id.isEmpty()) {
                                    boolean bNeedLoad = true;
                                    Calendar ts = ftpFile.getTimestamp();
                                    file_length = ftpFile.getSize();
                                    File imageFile = new File(imagesPath, ftpName);
                                    if (imageFile.exists()) {
                                        // Из-за того, что setLastModified (ниже по коду) не работает
                                        // (раньше работал), вместо условия == в проверке даты делаем >=
                                        // т.е. более свежий файл по дате создания скорее всего не менялся
                                        if (imageFile.length() == file_length && imageFile.lastModified() >= ts.getTimeInMillis()) {
                                            bNeedLoad = false;
                                            BitmapFactory.Options bitMapOption=new BitmapFactory.Options();
                                            bitMapOption.inJustDecodeBounds=true;
                                            Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bitMapOption);
                                            mapImageFiles.put(id, new ImageFileState(imageFile.length(), 0, bitMapOption.outWidth, bitMapOption.outHeight, ftpName)); // 0 - файл был, не загружаем
                                        }
                                    }
                                    if (bNeedLoad) {
                                        // Скачиваем файл
                                        publishProgress(FTP_STATE_RECEIVE_IMAGE, 0);

                                        OutputStream outFile = new FileOutputStream(imageFile);
                                        Common.ftpEnterMode(ftpClient, !g.Common.VK);
                                        ftpClient.setCopyStreamListener(streamListener);
                                        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                                        if (ftpClient.retrieveFile(ftpName, outFile)) {
                                            outFile.flush();
                                            textToLog.add("Загружен файл изображения " + ftpName);
                                            publishProgress(-2, textToLog.size());
                                            mapImageFiles.put(id, new ImageFileState(imageFile.length(), 1, ftpName)); // 1 - файл новый, загружаем
                                        } else {
                                            textToLog.add("Не удалось загрузить файл изображения " + ftpName);
                                            publishProgress(-2, textToLog.size());
                                            file_length = 0;
                                            mapImageFiles.put(id, new ImageFileState(imageFile.length(), 2, ftpName)); // 2 - файл новый, но не был загружен
                                        }
                                        outFile.close();
                                        if (file_length != 0) {
                                            imageFile.setLastModified(ts.getTimeInMillis());
                                        }
                                    }
                                }
                            }

                            boolean bImagesAreAll = true;

                            // Находим все существующие файлы изображений
                            File[] tempFileNames = imagesPath.listFiles();
                            if (tempFileNames != null) {
                                for (File tempFile : tempFileNames) {
                                    if (!tempFile.isDirectory()) {
                                        String id = "";
                                        String tempFileName = tempFile.getName();
                                        //m_imagesToSendSize+=tempFile.length();
                                        if (tempFileName.toLowerCase().endsWith(".jpg")||tempFileName.toLowerCase().endsWith(".png")) {
                                            id = g.Common.fileNameToId(tempFileName.substring(0, tempFileName.lastIndexOf(".")));
                                        }
                                        if (id.isEmpty()) {
                                            // Неправильное имя файла - удаляем
                                            tempFile.delete();
                                        } else {
                                            ImageFileState val;
                                            // Найдем, есть ли файл в нашем списке
                                            if (mapImageFiles.containsKey(id)) {
                                                val = mapImageFiles.get(id);
                                            } else {
                                                val=new ImageFileState(tempFile.length(), 3, tempFileName); // 3 - файл удален
                                                // Удаляем потому, что это файл уже не нужен
                                                tempFile.delete();
                                                mapImageFiles.put(id, val);
                                            }
                                            if (val.state != 0) {
                                                // Данные менялись
                                                bImageDataChanged = true;
                                            }
                                            if (val.state == 2) {
                                                // Была ошибка при загрузке
                                                bImagesAreAll = false;
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (InterruptedException e) {
                            textToLog.add(getString(R.string.ftp_message_interrupted) + " " + e.toString());
                            publishProgress(-2, textToLog.size());
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                            // Ошибка
                            textToLog.add(getString(R.string.ftp_message_error) + " " + e.toString());
                            publishProgress(-2, textToLog.size());
                        }
                        ftpClient.logout();
                        publishProgress(FTP_STATE_SUCCESS);
                    }
                    break;
                } // switch (mode)

                logWriter.flush();
                logWriter.close();


                // файлы изображений номенклатуры изменились, обновим ссылки в базе
                // 14.09.2018 добавлено обовление (size>0) при любой загрузке изображений товаров, даже если ничего не изменилось
                if (bImageDataChanged || mapImageFiles.size() > 0) {
                    //if (mapImageFiles==null)
                    if (!bGoodsImagesMode) {
                        // Флаг установился только потому, что загружается номенклатура
                        //mapImageFiles= new HashMap<String, Integer>();

                        File imagesPath=Common.getMyStorageFileDir(MainActivity.this, "goods");
                        /*
                        if (!bDontUseExternalStorage && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            // получаем путь к изображениям на SD
                            imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
                        } else {
                            imagesPath = new File(Environment.getDataDirectory(), "/data/" + getBaseContext().getPackageName() + "/temp");
                        }
                         */
                        //if (imagesPath.exists()) {
                            File[] tempFileNames = imagesPath.listFiles();
                            if (tempFileNames != null) {
                                for (File tempFile : tempFileNames) {
                                    if (!tempFile.isDirectory()) {
                                        String id = "";
                                        String tempFileName = tempFile.getName();
                                        if (tempFileName.toLowerCase().endsWith(".jpg")||tempFileName.toLowerCase().endsWith(".png")) {
                                            id = Common.fileNameToId(tempFileName.substring(0, tempFileName.lastIndexOf(".")));
                                        }
                                        if (!id.isEmpty()) {
                                            mapImageFiles.put(id, new ImageFileState(tempFile.length(), 1, tempFileName));
                                        }
                                    }
                                }
                            }
                        //}
                    }

                    // Пройдем по всей номенклатуре с изображениями и удалим ссылку на отсутствующие изображения
                    Cursor nomenclatureCursor = getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"_id", "id", "image_file", "image_file_size"}, "image_file<>?", new String[]{""}, null);
                    int index_id = nomenclatureCursor.getColumnIndex("_id");
                    int indexId = nomenclatureCursor.getColumnIndex("id");
                    //int indexImageFile = nomenclatureCursor.getColumnIndex("image_file");
                    int indexImageFileSize = nomenclatureCursor.getColumnIndex("image_file_size");
                    while (nomenclatureCursor.moveToNext()) {
                        ImageFileState val = new ImageFileState(nomenclatureCursor.getLong(indexImageFileSize), 3, "");
                        String id = nomenclatureCursor.getString(indexId);
                        if (mapImageFiles.containsKey(id)) {
                            val = mapImageFiles.get(id);
                        }
                        if (val.state == 3) {
                            // такого файла уже нет, убираем
                            int _id = nomenclatureCursor.getInt(index_id);
                            ContentValues cv = new ContentValues();
                            cv.put("image_file", "");
                            cv.put("image_file_checksum", 0);
                            cv.put("image_width", 0);
                            cv.put("image_height", 0);
                            cv.put("image_file_size", 0);
                            getContentResolver().update(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv, "_id=?", new String[]{String.valueOf(_id)});
                        }
                    }
                    // А теперь установим имена файлов
                    nomenclatureCursor.close();
                    for (Map.Entry<String, ImageFileState> entry : mapImageFiles.entrySet()) {
                        String key = entry.getKey();
                        ImageFileState value = entry.getValue();
                        if (value.state != 3) {
                            ContentValues cv = new ContentValues();
                            //cv.put("image_file", key);
                            cv.put("image_file", value.image_file_name);
                            // TODO 06.02.2019
                            cv.put("image_file_checksum", 0);
                            cv.put("image_file_size", value.file_size);
                            cv.put("image_width", value.image_width);
                            cv.put("image_height", value.image_height);
                            //
                            getContentResolver().update(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv, "id=?", new String[]{key});
                        }
                    }
                    //image_file
                    // Найдем номенклатуру с этим кодом

                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                // Ошибка связи
                textToLog.add(getString(R.string.ftp_message_connection_error) + " " + e.toString());
                publishProgress(-2, textToLog.size());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Ошибка ввода-вывода", e);
                // Ошибка ввода-вывода
                textToLog.add(getString(R.string.ftp_message_io_error) + " " + e.toString());
                publishProgress(-2, textToLog.size());
            } catch (Exception e) {
                Log.e(LOG_TAG, "Ошибка ", e);
                // Ошибка
                textToLog.add(getString(R.string.ftp_message_error) + " " + e.toString());
                publishProgress(-2, textToLog.size());
            }
            return null;
        }

    }

    void updatePageMessagesVisibility() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu navMenu=navigationView.getMenu();

        MySingleton g = MySingleton.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bDontShowMessages = sharedPreferences.getString("data_format", "DM").equals("PH");
        boolean bDontShowPayments = sharedPreferences.getString("data_format", "DM").equals("PH");
        String dataFormat=sharedPreferences.getString("data_format", "PL");
        boolean bDontShowRoutes = !(dataFormat.equals("PL")||dataFormat.equals("VK"));

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
            navMenu.findItem(R.id.nav_messages).setVisible(false);
            navMenu.findItem(R.id.nav_payments).setVisible(false);
            if (m_current_frame_type==FRAME_TYPE_MESSAGES) {
                navMenu.findItem(R.id.nav_orders).setChecked(true);
                attachFrameType(FRAME_TYPE_ORDERS);
            }
        } else {
            //tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
            navMenu.findItem(R.id.nav_messages).setVisible(true);
        }
        if (bDontShowPayments)
        {
            navMenu.findItem(R.id.nav_payments).setVisible(false);
            if (m_current_frame_type==FRAME_TYPE_PAYMENTS) {
                navMenu.findItem(R.id.nav_orders).setChecked(true);
                attachFrameType(FRAME_TYPE_ORDERS);
            }
        } else {
            navMenu.findItem(R.id.nav_payments).setVisible(true);
        }
        if (bDontShowRoutes) {
            navMenu.findItem(R.id.nav_routes).setVisible(false);
            if (m_current_frame_type==FRAME_TYPE_ROUTES) {
                navMenu.findItem(R.id.nav_orders).setChecked(true);
                attachFrameType(FRAME_TYPE_ORDERS);
            }
        } else {
            navMenu.findItem(R.id.nav_routes).setVisible(true);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        MySingleton g = MySingleton.getInstance();
        g.updateCommonDataFormat(prefs.getString("data_format", "DM"));
        try {
            updatePageMessagesVisibility();
        } catch (Error e) {
            // Can not perform this action after onSaveInstanceState
            // at ru.code22.mtrade.MainActivity.attachFrameType(MainActivity.java:1379)
        }
    }

    private void checkForAppUpdate() {
        // Creates instance of the manager.
        appUpdateManager = AppUpdateManagerFactory.create(Globals.getAppContext());

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Create a listener to track request state updates.
        installStateUpdatedListener = new InstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(InstallState installState) {
                // Show module progress, log state, or install the update.
                if (installState.installStatus() == InstallStatus.DOWNLOADED)
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdateAndUnregister();
            }
        };

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                //Toast.makeText(MainActivity.this,"addOnSuccessListener success", Toast.LENGTH_SHORT).show();
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    //Toast.makeText(MainActivity.this,"UPDATE_AVAILABLE", Toast.LENGTH_SHORT).show();
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                        //Toast.makeText(MainActivity.this,"isUpdateTypeAllowed FLEXIBLE", Toast.LENGTH_SHORT).show();
                        // Before starting an update, register a listener for updates.
                        appUpdateManager.registerListener(installStateUpdatedListener);
                        // Start an update.
                        startAppUpdateFlexible(appUpdateInfo);
                    } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) ) {
                        //Toast.makeText(MainActivity.this,"IMMEDIATE, start update", Toast.LENGTH_SHORT).show();
                        // Start an update.
                        startAppUpdateImmediate(appUpdateInfo);
                    }
                }
            }
        });
    }

    private void startAppUpdateImmediate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    private void startAppUpdateFlexible(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    REQ_CODE_VERSION_UPDATE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            unregisterInstallStateUpdListener();
        }
    }

    /**
     * Displays the snackbar notification and call to action.
     * Needed only for Flexible app update
     */
    private void popupSnackbarForCompleteUpdateAndUnregister() {
        Snackbar snackbar =
                Snackbar.make(findViewById(android.R.id.content), R.string.update_downloaded, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.Restart, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.setActionTextColor(getResources().getColor(R.color.action_color));
        snackbar.show();

        unregisterInstallStateUpdListener();
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * However, you should execute this check at all app entry points.
     */
    private void checkNewAppVersionState() {
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
                                          @Override
                                          public void onSuccess(AppUpdateInfo appUpdateInfo) {
                                              //FLEXIBLE:
                                              // If the update is downloaded but not installed,
                                              // notify the user to complete the update.
                                              if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                                  popupSnackbarForCompleteUpdateAndUnregister();
                                              }

                                              //IMMEDIATE:
                                              if (appUpdateInfo.updateAvailability()
                                                      == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                                  // If an in-app update is already running, resume the update.
                                                  startAppUpdateImmediate(appUpdateInfo);
                                              }
                                          }
                                      });

    }

    /**
     * Needed only for FLEXIBLE update
     */
    private void unregisterInstallStateUpdListener() {
        if (appUpdateManager != null && installStateUpdatedListener != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
    }

}
