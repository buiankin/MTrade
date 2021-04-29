package ru.code22.mtrade;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTPClient;

public class Common {

	static final String LOG_TAG = "mtradeLogs";
	
	public int m_mtrade_version;
	
	public Locale m_locale=null;
	
	// Format 1 MG (мега)
	// Format 2 SG (снежный городок)
	// Format 3 PL (продлидер)
	// Format 4 PT (прайт)
	// Format 5 TN (титан)
	// Format 6 PH (фараон)
	// Format 7 TA (тандем)
	// Format 8 IS (инфостарт)
	// Format 9 FB (фабрика)
	
	public boolean DEMO=false;
	
	public boolean MEGA=false;
	// Должен быть включен всегда (кроме Меги)
	public boolean SNEGOROD=false;

    //public static final int MAX_VALUE = 300;
    //public static final int MIN_VALUE = 100;
    
    //public static boolean PRODLIDER = true;
	public boolean PRODLIDER = false;
	public boolean VK = false;
	//public static final String UserFtpHomeDir="pda001"; // Prestigio
    //public static final String UserFtpHomeDir="pda003"; // Алексей, телефон Star
    //public static final String UserFtpHomeDir="pda004"; // Отладка, локальный комп
    
    //public static boolean PRAIT = false;
	public boolean PRAIT = false;
	//public static final String UserFtpHomeDir="002";
	//public static final String UserFtpHomeDir="004";
	//public static final String UserFtpHomeDir="";
	
	public boolean TITAN = false;
	public boolean PHARAON = false;
	public boolean TANDEM = false;
	public boolean ISTART = false;
	public boolean FACTORY = false;

	public boolean NEW_BACKUP_FORMAT = true;
	
	public boolean HAVE_GPS_SETTINGS = false;
	
	public boolean BACKUP_NOT_SAVED_ORDERS=true;
	
	public boolean DISTRIBS_BY_QUANTITY = true;
	
	public String m_app_theme="DARK";

    public String m_currency="DEFAULT";

    public boolean isDataFormatWithTradePoints()
    {
        if (MEGA||PHARAON||TANDEM||TITAN||FACTORY)
            return false;
        return true;
    }

	// убирает из строки лишние нули справа
	static public String DoubleToStringFormat(double d, String formatString)
	{
		String res=String.format(formatString, d).replace(",", ".");
		int i;
		int last_zero=res.length();
		for (i=res.length()-1; i>=0; i--)
		{
			if (res.charAt(i)!='0')
			{
				if (res.charAt(i)=='.'||res.charAt(i)==',')
				{
					if (last_zero==i+1)
						last_zero=i;
					return res.substring(0, last_zero);
				}
			} else
			{
				if (last_zero==i+1)
					last_zero=i;
			}
		}
		return res;
	}
	
	//#define ZERO_MINUS_VER(x) (x)<=0?-1:(x)
	static public int ZERO_MINUS_VER(int x)
	{
		return x<=0?-1:x;
	}
	//#define QUERY_VER(a) if ((a)>0) (a)=-(a);
	
	static public double MyStringToDouble(String s)
	{
		if (s.isEmpty())
			return 0;
		if (s.contains(","))
			return Double.parseDouble(s.replace(',', '.'));
		return Double.parseDouble(s); 
	}
	
	
	static String NormalizeString(String param)
	{
		String supportedSym="! 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
				"АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЬЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
				"().,;:-+=_\\/*@?%#$№&[]{}'";
	    int i;
	    StringBuilder sb = new StringBuilder();
	    for (i=0; i<param.length(); i++)
	    {
	    	if (supportedSym.indexOf(param.substring(i, i+1))<0)
	        {
	            sb.append(" ");
	        } else
	            sb.append(param.substring(i, i+1));
	    }
	    return sb.toString();
	}
	
	static String idToFileName(String id)
	{
		String res="";
		int i;
		if (id.length()==36)
		{
			return id;
		}
		for (i=0; i<id.length(); i++)
		{
			res+=String.format("%2X", (int)id.charAt(i));
		}
		return res;
	}
	
	static String fileNameToId(String fileName)
	{
		String res="";
		int val_i=0;
		int i;
		boolean bFileAsIs=false;
		for (i=0; i<fileName.length(); i++)
		{
			char ch=fileName.charAt(i);
			if (ch>='0'&&ch<='9')
			{
				val_i=val_i*16+ch-'0';
			} else
			if (ch>='A'&&ch<='F')
			{
				val_i=val_i*16+ch-'A'+10;
			} else
			if (ch>='a'&&ch<='f')
			{
				val_i=val_i*16+ch-'a'+10;
			} else
			if (ch=='-')
			{
				bFileAsIs=true;
			} else
			{
				return "";
			}
			if (i%2==1)
			{
				res+=(char)val_i;
				val_i=0;
			}
		}
		if (bFileAsIs)
			return fileName;
		return res;
	}
	
	static String inputDateToString4(String inputDate)
	{
		String s=inputDate.replace(" ", "")+"0000";
		int pos=s.indexOf(':');
		if (pos<0)
		{
			if (inputDate.length()==1)
			{
				s="0"+inputDate.replace(" ", "")+"0000";
			}
		} else
		{
			s=s.replace(":", "");
		}
		switch (pos)
		{
		case 0:
			return ("00"+s).substring(0, 4);
		case 1:
			return ("0"+s).substring(0, 4);
		}
		return s.substring(0, 4);
	}
	
	static String combineConditions(String oldCondition, List<String> resultArgs, String newCondition, String[] newArgs)
	{
		//resultArgs.addAll(Arrays.asList(newArgs));
		if (newArgs!=null)
		{
			resultArgs.addAll(Arrays.asList(newArgs));
		}
		if (oldCondition==null||oldCondition.isEmpty())
		{
			if (newCondition==null||newCondition.isEmpty())
			{
				return "";
			}
			return newCondition;
		}
		if (newCondition==null||newCondition.isEmpty())
		{
			return oldCondition;
		}
		return oldCondition+" and "+newCondition;
	}
	
	
	private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
	
	/**
	 * Raturns all available SD-Cards in the system (include emulated)
	 *
	 * Warning: Hack! Based on Android source code of version 4.3 (API 18)
	 * Because there is no standart way to get it.
	 * TODO: Test on future Android versions 4.4+
	 *
	 * @return paths to all available SD-Cards in the system (include emulated)
	 */
	/*
	public static String[] getStorageDirectories()
	{
	    // Final set of paths
	    final Set<String> rv = new HashSet<String>();
	    // Primary physical SD-CARD (not emulated)
	    final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
	    // All Secondary SD-CARDs (all exclude primary) separated by ":"
	    final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
	    // Primary emulated SD-CARD
	    final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
	    if(TextUtils.isEmpty(rawEmulatedStorageTarget))
	    {
	        // Device has physical external storage; use plain paths.
	        if(TextUtils.isEmpty(rawExternalStorage))
	        {
	            // EXTERNAL_STORAGE undefined; falling back to default.
	            rv.add("/storage/sdcard0");
	        }
	        else
	        {
	            rv.add(rawExternalStorage);
	        }
	    }
	    else
	    {
	        // Device has emulated storage; external storage paths should have
	        // userId burned into them.
	        final String rawUserId;
	        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
	        {
	            rawUserId = "";
	        }
	        else
	        {
	            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
	            final String[] folders = DIR_SEPORATOR.split(path);
	            final String lastFolder = folders[folders.length - 1];
	            boolean isDigit = false;
	            try
	            {
	                Integer.valueOf(lastFolder);
	                isDigit = true;
	            }
	            catch(NumberFormatException ignored)
	            {
	            }
	            rawUserId = isDigit ? lastFolder : "";
	        }
	        // /storage/emulated/0[1,2,...]
	        if(TextUtils.isEmpty(rawUserId))
	        {
	            rv.add(rawEmulatedStorageTarget);
	        }
	        else
	        {
	            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
	        }
	    }
	    // Add all secondary storages
	    if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
	    {
	        // All Secondary SD-CARDs splited into array
	        final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
	        Collections.addAll(rv, rawSecondaryStorages);
	    }
	    return rv.toArray(new String[rv.size()]);
	}
	
	public static File myGetExternalStorageDirectory()
	{
		String[] dirs=getStorageDirectories();
		for (String dir:dirs)
		{
			if (dir.toLowerCase().contains("ext"))
			{
				File f=new File(dir);
				if (f.canWrite())
				{
					return f;
				}
			}
		}
		for (String dir:dirs)
		{
			File f=new File(dir);
			if (f.canWrite())
			{
				return f;
			}
		}
		return null;
	}

	public static File getExternalSDCardDirectory()
	{
	    File innerDir = Environment.getExternalStorageDirectory();
	    File rootDir = innerDir.getParentFile();
	    File firstExtSdCard = innerDir ;
	    File[] files = rootDir.listFiles();
	    for (File file : files) {
	        if (file.compareTo(innerDir) != 0) {
	            firstExtSdCard = file;
	            break;
	        }
	    }
	    //Log.i("2", firstExtSdCard.getAbsolutePath().toString());
	    return firstExtSdCard;
	}
	 */
	

	// TODO сделать по статье
    // (но нужен телефон с андроидом API 29)
	// https://commonsware.com/blog/2019/06/07/death-external-storage-end-saga.html
    // getExternalFilesDir()
    // https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)
    // getExternalFilesDir(String)
    // Environment.getExternalStorageState(File)
    // Environment.isExternalStorageEmulated(File)
    // Environment.isExternalStorageRemovable(File)
	public static File getMyStorageFileDir(Context context, String dir)
	{
        File myStorageFileDir;
        File externalFilesDir=context.getExternalFilesDir(null);
        if (dir!=null&&!dir.isEmpty())
            myStorageFileDir=new File(externalFilesDir, dir);
        else
            myStorageFileDir=externalFilesDir;
        if (!myStorageFileDir.exists())
            myStorageFileDir.mkdirs();
	    /*
		File myStorageFileDir;
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
            File externalFilesDir=context.getExternalFilesDir(null);
            myStorageFileDir = new File(externalFilesDir, "/mtrade");
        }
		else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            myStorageFileDir = new File(Environment.getExternalStorageDirectory(), "/mtrade");
        }
		else {
            myStorageFileDir = new File(Environment.getDataDirectory(), "/data/" + context.getPackageName());
        }
		if (!dir.isEmpty())
			myStorageFileDir=new File(myStorageFileDir, dir);
	    if (!myStorageFileDir.exists())
	    	myStorageFileDir.mkdirs();
	     */
	    return myStorageFileDir;
	}
	
	public static String MyDateFormat(String formatString, Date date)
	{
		DateFormat simpleDateFormat = new java.text.SimpleDateFormat(formatString);
		String result=simpleDateFormat.format(date);
		/*
		if (result.contains("H")) {
			// Только формат "H" - это 0..23, а "k" - 1..24
			simpleDateFormat = new java.text.SimpleDateFormat(formatString.replace("H", "k"));
			result = simpleDateFormat.format(date);
		}
		 */
		return result;
	}

    public static String MyDateFormat(String formatString, Calendar calendar)
    {
        DateFormat dateFormat = new java.text.SimpleDateFormat(formatString);
                // 13.01.2019 есть подозрение, что на некоторых телефонах дает для даты, полученной через new Date(loc.getTime)
                // примерно минус 19 лет
		dateFormat.setTimeZone(calendar.getTimeZone());
                //
		//System.out.println(dateFormat.format(calendar.getTime()));
		String result=dateFormat.format(calendar.getTime());
		/*
		if (result.contains("H")) {
			dateFormat = new java.text.SimpleDateFormat(formatString.replace("H", "k"));
			dateFormat.setTimeZone(calendar.getTimeZone());
			result = dateFormat.format(calendar.getTime());
		}
		 */
        return result;
    }


	public static String getDateTimeAsString14(Date date) {
		//java.util.Date date=new java.util.Date();
		//return android.text.format.DateFormat.format("yyyyMMddkkmmss", date).toString();
		if (date == null)
		{
			date=new Date();
		}
		return MyDateFormat("yyyyMMddHHmmss", date);
	}

	static public int getColorFromAttr(Context context, int colorResId) {

		//return ContextCompat.getColor(context, colorResId); // Doesn't seem to work for R.attr.colorPrimary
		TypedValue typedValue = new TypedValue();
		TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[] {colorResId});
		int color = typedArray.getColor(0, 0);
		typedArray.recycle();
		return color;

	}

	static public String dateStringAsText(String date)
	{
        if (date.length()==8||date.length()==14) {
            StringBuilder sb = new StringBuilder();
            sb.append(date.substring(6, 8)).append('.').append(date.substring(4, 6)).append('.').append(date.substring(0, 4));
            return sb.toString();
        }
        return date;
	}

	static public String dateTimeStringAsText(String date)
	{
        if (date.length()==14) {
            StringBuilder sb = new StringBuilder();
            sb.append(date.substring(6, 8)).append('.').append(date.substring(4, 6)).append('.').append(date.substring(0, 4)).append(' ').append(date.substring(8, 10)).append(':').append(date.substring(10, 12)).append(':').append(date.substring(12, 14));
            return sb.toString();
        }
        if (date.length()==8) {
            StringBuilder sb = new StringBuilder();
            sb.append(date.substring(6, 8)).append('.').append(date.substring(4, 6)).append('.').append(date.substring(0, 4));
            return sb.toString();
        }
        return date;
	}

	public static boolean isDateStringEmpty(String s)
	{
		return (s==null||s.isEmpty()||s.equals("00010101")||s.equals("00010101000000"));
	}


	public static String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	public String getCurrency(Context context)
    {
    	if (ISTART||VK) {
			if (m_currency.equals("RUR")) {
				return context.getString(R.string.currency_rur);
			}
			if (m_currency.equals("UAH")) {
				return context.getString(R.string.currency_uah);
			}
			if (m_currency.equals("KZT")) {
				return context.getString(R.string.currency_kzt);
			}
			if (m_currency.equals("USD")) {
				return context.getString(R.string.currency_usd);
			}
		}
        return context.getString(R.string.currency_default);
    }

    public String getCurrencyFormatted(Context context)
    {
		if (ISTART||VK) {
			if (m_currency.equals("RUR")) {
				return context.getString(R.string.currency_format_rur);
			}
			if (m_currency.equals("UAH")) {
				return context.getString(R.string.currency_format_uah);
			}
			if (m_currency.equals("KZT")) {
				return context.getString(R.string.currency_format_kzt);
			}
			if (m_currency.equals("USD")) {
				return context.getString(R.string.currency_format_usd);
			}
		}
        return context.getString(R.string.currency_format_default);
    }

    public static boolean myCopyFile(File scrFile, File destFile, boolean deleteSrcFile)
    {
        FileChannel src = null;
        FileChannel dst = null;
        boolean bResult=false;
        try {
            src = new FileInputStream(scrFile).getChannel();
            dst = new FileOutputStream(destFile).getChannel();
            dst.transferFrom(src, 0, src.size());
        } catch (IOException e) {
            return false;
        } finally {
            bResult=true;
            try {
                src.close();
            } catch (Exception e) {
                bResult=false;
            }
            try {
                dst.close();
            } catch (Exception e) {
                bResult=false;
            }
        }

        if (deleteSrcFile&&bResult)
        {
            bResult=scrFile.delete();
        }

        return bResult;
    }

	/**
	 * Unzip a zip file.  Will overwrite existing files.
	 *
	 * @param zipFile Full path of the zip file you'd like to unzip.
	 * @param location Full path of the directory you'd like to unzip to (will be created if it doesn't exist).
	 * @throws IOException
	 */

    public static void unzip(String zipFile, String location) throws IOException {
        int size;
        final int BUFFER_SIZE=4096;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            if ( !location.endsWith("/") ) {
                location += "/";
            }
            File f = new File(location);
            if(!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if(!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        // check for and create parent directories if they don't exist
                        File parentDir = unzipFile.getParentFile();
                        if ( null != parentDir ) {
                            if ( !parentDir.isDirectory() ) {
                                parentDir.mkdirs();
                            }
                        }

                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, BUFFER_SIZE);
                        try {
                            while ( (size = zin.read(buffer, 0, BUFFER_SIZE)) != -1 ) {
                                fout.write(buffer, 0, size);
                            }

                            zin.closeEntry();
                        }
                        finally {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }
            finally {
                zin.close();
            }
        }
        catch (Exception e) {
            Log.e(LOG_TAG, "Unzip exception", e);
        }
    }

	private void dismissProgressDialog(ProgressDialog progressDialog) {
		if (progressDialog != null) {
			if (progressDialog.isShowing()) {

				//get the Context object that was used to create the dialog
				Context context = ((ContextWrapper) progressDialog.getContext()).getBaseContext();

				// if the Context used here was an activity AND it hasn't been finished or destroyed
				// then dismiss it
				if (context instanceof Activity) {

					// Api >=17
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed()) {
							dismissWithExceptionHandling(progressDialog);
						}
					} else {

						// Api < 17. Unfortunately cannot check for isDestroyed()
						if (!((Activity) context).isFinishing()) {
							dismissWithExceptionHandling(progressDialog);
						}
					}
				} else
					// if the Context used wasn't an Activity, then dismiss it too
					dismissWithExceptionHandling(progressDialog);
			}
			progressDialog = null;
		}
	}

	public static void dismissWithExceptionHandling(ProgressDialog dialog) {
		try {
			dialog.dismiss();
		} catch (final IllegalArgumentException e) {
			// Do nothing.
		} catch (final Exception e) {
			// Do nothing.
		} finally {
			dialog = null;
		}
	}

	public static void ftpEnterMode(FTPClient ftpClient, boolean passiveMode) {
		if (passiveMode)
			ftpClient.enterLocalPassiveMode();
		else
			ftpClient.enterLocalActiveMode();
	}



}
