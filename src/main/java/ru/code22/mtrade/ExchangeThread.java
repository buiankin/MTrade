package ru.code22.mtrade;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ExchangeThread extends Thread {
	
	private static final String LOG_TAG = "mtradeLogs";
	
	public final static int STATE_DONE = 0;
	public final static int STATE_RUNNING = 1;
	
	private Handler handler;
	private int stateRun;
	private int total;
	//private ContentResolver contentResolver;
	private Context context;
	MyDatabase m_myBase;
	Globals g;
	
	ExchangeThread(Handler h, Context c, MyDatabase b, Globals g) {
		handler=h;
		//contentResolver=c;
		context=c;
		m_myBase=b;
		this.g=g;
	}
	
	private void updateProgress(int max, int pos)
	{
		Message msg=handler.obtainMessage();
		Bundle b = new Bundle();
		b.putInt("max", max);
		b.putInt("pos", pos);
		msg.setData(b);
		handler.sendMessage(msg);
	}
	
	public boolean doSomething() throws IOException
	{
	//FTPClient ftpClient = new FTPClient();
	
		//File file = new File(
	    //        Environment.getExternalStorageDirectory(),
	    //        "/file.txt");
		File file = new File(Common.getMyStorageFileDir(context, ""), "file.txt");
		
		String newLine = "\r\n";
		
		FileWriter writer = null;
	    try {
	        writer = new FileWriter(file);
	    } catch (IOException e) {
	        // если возникнет ошибка, то добавим её описание в Log
	        Log.e("MyError", "Не создался writer", e);
	    }
	    
  	 // TODO код-близнец в MainActivity
	    Cursor cursor=context.getContentResolver().query(MTradeContentProvider.VERSIONS_CONTENT_URI, new String[]{"param","ver"}, null, null, null);
	    if (cursor!=null)
	    {
	    	int index_param = cursor.getColumnIndex("param");
	    	int index_ver = cursor.getColumnIndex("ver");
	    	while (cursor.moveToNext())
	    	{
	    		String param = cursor.getString(index_param);
	    		if (param.equals("CLIENTS"))
	    		{
	    			m_myBase.m_clients_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("NOMENCLATURE"))
	    		{
	    			m_myBase.m_nomenclature_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("RESTS"))
	    		{
	    			m_myBase.m_rests_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("PRICETYPES"))
	    		{
	    			m_myBase.m_pricetypes_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("AGREEMENTS"))
	    		{
	    			m_myBase.m_agreements_version=cursor.getInt(index_ver);
	    		} else
				if (param.equals("AGREEMENTS30"))
				{
					m_myBase.m_agreements30_version=cursor.getInt(index_ver);
				} else
	    		if (param.equals("SALDO"))
	    		{
	    			m_myBase.m_saldo_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("STOCKS"))
		    	{
	    			m_myBase.m_stocks_version=cursor.getInt(index_ver);
		    	} else
	    		if (param.equals("PRICES"))
		    	{
	    			m_myBase.m_prices_version=cursor.getInt(index_ver);
		    	} else
	    		if (param.equals("AGENTS"))
		    	{
	    			m_myBase.m_agents_version=cursor.getInt(index_ver);
		    	} else
	    		if (param.equals("CURATORS"))
		    	{
	    			m_myBase.m_curators_version=cursor.getInt(index_ver);
		    	} else
	    		if (param.equals("D_POINTS"))
		    	{
	    			m_myBase.m_distr_points_version=cursor.getInt(index_ver);
		    	} else
	 	    	if (param.equals("ORGANIZATIONS"))
	 	    	{
	 	    		m_myBase.m_organizations_version=cursor.getInt(index_ver);
	 	    	} else
	 	    	if (param.equals("SALES_LOADED"))
	 	    	{
	 	    		m_myBase.m_sales_loaded_version=cursor.getInt(index_ver);
	 	    	} else
	    		if (param.equals("PLACES"))
	    		{
	    			m_myBase.m_places_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("OCCUPIED_PLACES"))
	    		{
	    			m_myBase.m_occupied_places_version=cursor.getInt(index_ver);
	    		}  else
	    		if (param.equals("VICARIOUS_POWER"))
	    		{
	    			m_myBase.m_vicarious_power_version=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("SENT_COUNT"))
	    		{
	    			m_myBase.m_sent_count=cursor.getInt(index_ver);
	    		} else
	    		if (param.equals("DISTRIBS_CONTRACTS"))
	    		{
	    			m_myBase.m_distribs_contracts_version=cursor.getInt(index_ver);
	    		}

	    	}
	    	cursor.close();
	    }
		
		String zipFile="/mnt/sdcard/qdata/test.zip";
		ZipFile zip=new ZipFile(zipFile);
	    try {
	    	Enumeration<? extends ZipEntry> entries = zip.entries();
	    	while (entries.hasMoreElements()) {
	    		ZipEntry ze = (ZipEntry)entries.nextElement();
	        	if (!ze.isDirectory()&&ze.getName().toLowerCase().equals("ein.txt")){
	        		InputStream zin = zip.getInputStream(ze);
	        		int total=(int)ze.getSize();
	        		byte[] data = new byte[total];
	                try {
	                	int rd=0, block;
	                	while (total-rd>0&&(block=zin.read(data, rd, total-rd))!=-1)
	                	{
	                		rd+=block;
	                	}
	                    zin.close();
	                	if (rd==total)
	                	{
	                        String strUnzipped = new String(data, "windows-1251");
	                		updateProgress(strUnzipped.length(), 0);	                		
	                        //String[] lines=strUnzipped.split("##.*##");
	                        Pattern pattern=Pattern.compile("(##.*##)");
	                   	    Matcher mtch=pattern.matcher(strUnzipped);
	                   	    int start_prev=-1;
	                   	    int start_this;
	                   	    String prevSection="";
	                   	    boolean bContinue=true;
	                   	    while (bContinue)
	                   	    {
	                   	    	String section="";
	                   	    	if (mtch.find())
	                   	    	{
	                       	    	section=mtch.group();
	                   	    		start_this=mtch.start();
	                   	    	} else
	                   	    	{
	                   	    		bContinue=false;
	                   	    		start_this=strUnzipped.length()+1;
	                   	    	}
	                   	    	if (start_prev>0)
	                   	    	{
		                   	    	updateProgress(strUnzipped.length(), start_prev);
	                       	    	if (prevSection.equals("##CLIENTS##"))
	                       	    	{
	                       	    		writer.append("start clients " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		//Toast.makeText(MainActivity.this, "start clients", Toast.LENGTH_SHORT).show();	                       	    		
	                       	    		TextDatabase.LoadClients(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end clients " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
									if (prevSection.equals("##AGREEMENTS30##"))
									{
										writer.append("start agreements30 " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
										TextDatabase.LoadAgreements30(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
										writer.append("end agreements30 " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
									} else
	                       	    	if (prevSection.equals("##AGREEMENTS##"))
	                       	    	{
	                       	    		writer.append("start agreements " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadAgreements(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end agreements " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##NOMENCL##"))
	                       	    	{
	                       	    		writer.append("start nomenclature " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadNomenclature(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end nomenclature " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##OSTAT##"))
	                       	    	{
	                       	    		writer.append("start ostat " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadOstat(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end ostat " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##DOLG##"))
	                       	    	{
	                       	    		writer.append("start dolg " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadDolg(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end dolg " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##PRICETYPE##"))
	                       	    	{
	                       	    		writer.append("start pricetype " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadPriceTypes(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end pricetype " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##PRICE##"))
	                       	    	{
	                       	    		writer.append("start price " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadPrice(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end price " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
									if (prevSection.equals("##PRICE_AGREEMENTS30##"))
									{
										writer.append("start price_agreements30 " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
										TextDatabase.LoadPricesAgreements30(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
										writer.append("end price_agreements30 " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
									} else
	                       	    	if (prevSection.equals("##STOCKS##"))
	                       	    	{
	                       	    		writer.append("start stocks " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadStocks(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end stocks " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##AGENTS##"))
	                       	    	{
	                       	    		writer.append("start agents " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadAgents(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end agents " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##CURATORS##"))
	                       	    	{
	                       	    		writer.append("start curators " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadCurators(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end curators " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##D_POINTS##"))
	                       	    	{
	                       	    		writer.append("start d.points " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    		TextDatabase.LoadDistrPoints(context.getContentResolver(), m_myBase, strUnzipped.substring(start_prev, start_this), true);
	                       	    		writer.append("end d.points " + DateFormat.getDateTimeInstance().format(new Date())+newLine);
	                       	    	} else
	                       	    	if (prevSection.equals("##ORDERS_M##"))
	                       	    	{
	                       	    	} else
	                       	    	if (prevSection.equals("##DISTRIB_M##"))
	                       	    	{
	                       	    	} else
	                       	    	{
	                       	    		//Toast.makeText(MainActivity.this, "ERR: "+prevSection, Toast.LENGTH_SHORT).show();
	                       	    	}
	                   	    	}
	                   	    	start_prev=start_this+section.length();
	                   	    	prevSection=section;
	                   	    }
	                        
	                        //Toast.makeText(MainActivity.this, "" + lines.length, Toast.LENGTH_SHORT).show();
	                   	    updateProgress(strUnzipped.length(), strUnzipped.length());	                   	    
	                    }
	                	
	                }
	                finally {
	                    //fout.flush();
	                    //fout.close();
	                }
	        	}
	        	
	        }
	    }
	    catch (Exception e)
	    {
	    	//Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	    finally
	    {
	    	zip.close();
	    }
	    writer.flush();
	    writer.close();
	    
	    updateProgress(-1, -1);	    
	    
	    return true;
    
    }

	@Override
	public void run() {
		
		try
		{
			if (doSomething())
			{
				
			}
			
		//} catch (InterruptedException e)
		//{
		//	Log.e("ERROR", "Thread interrupted");
		} catch (IOException e)
		{
			Log.e("ERROR", "Thread interrupted");
		}		
		
		/*
		stateRun = STATE_RUNNING;
		total=0;
		while (stateRun == STATE_RUNNING)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				Log.e("ERROR", "Thread interrupted");
			}
			Message msg=handler.obtainMessage();
			Bundle b = new Bundle();
			b.putInt("total", total);
			msg.setData(b);
			handler.sendMessage(msg);
			total++;
		}
		*/
		
		
		
		
		
		
		
		
		
		
	}
	
	public void setState(int state)
	{
		stateRun = state;
	}
	
	

}
