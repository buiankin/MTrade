package ru.code22.mtrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.jetbrains.annotations.NotNull;

public class MySingleton {
	
	private static MySingleton mInstance;
	boolean bNeedInitByData;
	boolean bNeedInitImageLoader;
	
	boolean m_preferences_from_ini;
	
    // в настройках приложения
    String m_DataFormat;
    String m_FTP_server_address;
	String m_FTP_server_address_spare;
    String m_FTP_server_user;
    String m_FTP_server_password;
    String m_FTP_server_directory;
	
	public MyDatabase MyDatabase;
	public Common Common;

	MySingleton()
	{
		Common=new Common();
		m_preferences_from_ini=false;
		bNeedInitByData=true;
		bNeedInitImageLoader=true;
	}
	
    boolean readIniFile(Activity activity, @NotNull Context context, SharedPreferences sharedPreferences)
    {
		//if (android.os.Build.VERSION.SDK_INT< Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		if (true)
		{
		//    // получаем путь к SD
		//    File sdPath = new File(Environment.getExternalStorageDirectory(), "/mtrade");
        //    if (!sdPath.exists()) {
        //    	sdPath.mkdirs();
        //    }

			File sdPath = Common.getMyStorageFileDir(context, "");

            File settingsFile=new File(sdPath, "mtrade.ini");
            int line_num=0;
            BufferedReader br;
            String strFtpAddress=null;
			String strFtpAddressSpare=null;
            String strFtpUser=null;
            String strFtpPassword=null;
            // Домашний каталог может быть в файле не указан, считаем что он должен быть пустым
            String strFtpDirectory="";
            // И формат данных
            String strDataFormat="";
    	
			try {
				br = new BufferedReader(new FileReader(settingsFile));
		        String line;
		        while ((line = br.readLine()) != null)
		        {
		        	switch (line_num)
		        	{
		        	case 0:
		        		strFtpAddress=line;
		        		break;
		        	case 1:
		        		strFtpUser=line;
		        		break;
		        	case 2:
		        		strFtpPassword=line;
		        		break;
		        	case 3:
		        		strFtpDirectory=line;
		        		break;
		        	case 4:
		        		strDataFormat=line;
		        		break;
					case 5:
						strFtpAddressSpare=line;
						break;
		        	}
		        	line_num++;
		        }
		        br.close();            
			} catch (Exception e) {
				line_num=0;
			}
			boolean bSettingsChanged=false;
			if (line_num>=3)
			{
				m_preferences_from_ini=true;
				if (!strFtpAddress.equals("#")&&!sharedPreferences.getString("server_address", "").equals(strFtpAddress))
					bSettingsChanged=true;
				if (!strFtpAddressSpare.equals("#")&&!sharedPreferences.getString("server_address_spare", "").equals(strFtpAddress))
					bSettingsChanged=true;
				if (!strFtpUser.equals("#")&&!sharedPreferences.getString("server_user", "").equals(strFtpUser))
					bSettingsChanged=true;
				if (!strFtpPassword.equals("#")&&!sharedPreferences.getString("server_password", "").equals(strFtpPassword))
					bSettingsChanged=true;
				if (!strFtpDirectory.equals("#")&&!sharedPreferences.getString("server_directory", "").equals(strFtpDirectory))
					bSettingsChanged=true;
				if (!strDataFormat.isEmpty()&&!sharedPreferences.getString("data_format", "").equals(strDataFormat))
					bSettingsChanged=true;
			}
			if (bSettingsChanged)
			{
				//Toast.makeText(this, "Восстановлены настройки из файла mtrade.ini", Toast.LENGTH_LONG).show();
				if (activity!=null)
				{
					Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.message_settings_restored), Snackbar.LENGTH_LONG).show();
				}
				
		        SharedPreferences.Editor editor = sharedPreferences.edit();
		        if (!strFtpAddress.equals("#"))
		        	editor.putString("server_address", strFtpAddress);
				if (!strFtpAddress.equals("#"))
					editor.putString("server_address_spare", strFtpAddressSpare);
		        if (!strFtpUser.equals("#"))
		        	editor.putString("server_user", strFtpUser);
		        if (!strFtpPassword.equals("#"))
		        	editor.putString("server_password", strFtpPassword);
		        if (!strFtpDirectory.equals("#"))
		        	editor.putString("server_directory", strFtpDirectory);
		        if (!strDataFormat.isEmpty())
		        	editor.putString("data_format", strDataFormat);
		        editor.commit();				
			}
			return true;
		}
		return false;
    }
    
    public void readPreferences(SharedPreferences sharedPreferences)
    {
    	String strFormat=sharedPreferences.getString("data_format", "DM");
    	updateCommonDataFormat(strFormat);
    	m_DataFormat=strFormat;
        m_FTP_server_address=sharedPreferences.getString("server_address", "");
		m_FTP_server_address_spare=sharedPreferences.getString("server_address_spare", "");
        m_FTP_server_user=sharedPreferences.getString("server_user", "");
        m_FTP_server_password=sharedPreferences.getString("server_password", "");
        m_FTP_server_directory=sharedPreferences.getString("server_directory", "");
	}
    
    @SuppressWarnings("unused")
	void updateCommonDataFormat(String strFormat)
    {
    	MySingleton g=MySingleton.getInstance();
    	// SG по умолчанию
    	g.Common.DEMO=false;
    	g.Common.MEGA=false;
    	g.Common.SNEGOROD=true; // флаг устанавливается всегда кроме меги
    	g.Common.PRODLIDER=false;
    	g.Common.PRAIT=false;
    	g.Common.TITAN=false;    	
    	g.Common.PHARAOH=false;
    	g.Common.TANDEM=false;
    	g.Common.ISTART=false;
		g.Common.FACTORY=false;
		g.Common.VK=false;
    	if (strFormat.equals("IS")||Constants.MY_ISTART)
    	{
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=true;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	if (strFormat.equals("TN"))
    	{
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=true;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	if (strFormat.equals("MG"))
    	{
    		g.Common.MEGA=true;
    		g.Common.SNEGOROD=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	// Демонстрационная база с архитектурой продлидера, данные из демо 1С
    	if (strFormat.equals("PL")||strFormat.equals("DM"))
    	{
    		if (strFormat.equals("DM"))
    		{
    			g.Common.DEMO=true;
    		}
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=true;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	if (strFormat.equals("PT"))
    	{
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=true;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	if (strFormat.equals("PH"))
    	{
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=true;
    		g.Common.TANDEM=false;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	} else
    	if (strFormat.equals("TA"))
    	{
    		g.Common.MEGA=false;
    		g.Common.PRODLIDER=false;
    		g.Common.PRAIT=false;
    		g.Common.TITAN=false;
    		g.Common.PHARAOH=false;
    		g.Common.TANDEM=true;
    		g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=false;
    	}  else
		if (strFormat.equals("FB"))
		{
			g.Common.MEGA=false;
			g.Common.PRODLIDER=false;
			g.Common.PRAIT=false;
			g.Common.TITAN=false;
			g.Common.PHARAOH=false;
			g.Common.TANDEM=false;
			g.Common.ISTART=false;
			g.Common.FACTORY=true;
			g.Common.VK=false;
		}  else
		if (strFormat.equals("VK"))
		{
			g.Common.MEGA=false;
			g.Common.PRODLIDER=true;
			g.Common.PRAIT=false;
			g.Common.TITAN=false;
			g.Common.PHARAOH=false;
			g.Common.TANDEM=false;
			g.Common.ISTART=false;
			g.Common.FACTORY=false;
			g.Common.VK=true;
		}
    	
    }
	
	
	void InitByData(@NotNull Activity activity)
	{
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(activity);
		Common.m_app_theme=sharedPreferences.getString("app_theme", "DARK");
		Common.m_currency=sharedPreferences.getString("currency", "DEFAULT");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			Common.m_locale=activity.getResources().getConfiguration().getLocales().get(0);
		} else{
			//noinspection deprecation
			Common.m_locale=activity.getResources().getConfiguration().locale;
		}
		readIniFile(activity, activity, sharedPreferences);
		readPreferences(sharedPreferences);
	}
	

	 public static void initInstance() {
		  //Log.d("MY", "MySingleton::InitInstance()");
		  if (mInstance == null) {
			  mInstance = new MySingleton();
		  }
	 }
	 
	 public static MySingleton getInstance() {
		 //Log.d("MY", "MySingleton::getInstance()");
		 return mInstance;
	 }
	 
	 public void checkInitByDataAndSetTheme(Activity activity)
	 {
		    if (bNeedInitByData)
		    {
		    	InitByData(activity);
		    	bNeedInitByData=false;
		    }
			if (Common.m_app_theme!=null&&Common.m_app_theme.equals("DARK")){
				//activity.setTheme(R.style.AppTheme);
				activity.setTheme(R.style.AppTheme_NoActionBar);
			} else {
				//activity.setTheme(R.style.LightAppTheme);
				activity.setTheme(R.style.LightAppTheme_NoActionBar);
			}
	 }

	 public void CheckInitImageLoader(Activity activity)
	 {
		 if (bNeedInitImageLoader)
		 {
			 bNeedInitImageLoader=false;
			 // На всякий случай передаем туда appilication, т.к. activity потом будет уничтожен
			 // и как это отрабатывается библиотекой, я не знаю
			 // Application будем надеяться, что будет всегда
			 ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(activity.getApplication()));
		 }
	 }


	
}
