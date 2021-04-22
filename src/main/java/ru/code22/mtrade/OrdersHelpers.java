package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

import ru.code22.mtrade.MyDatabase.ClientPriceRecord;
import ru.code22.mtrade.MyDatabase.CuratorPriceRecord;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.snackbar.Snackbar;

public class OrdersHelpers {

	static public void PrepareData(Intent intent, Activity activity, long id, MyID client_id, MyID distr_point_id, boolean bCopyOrder)
	{
		MySingleton g=MySingleton.getInstance();
		boolean bCreatedOrder=false;
		OrderRecord rec=g.MyDatabase.m_order_editing;
		g.MyDatabase.m_order_editing_id=id;
		g.MyDatabase.m_order_new_editing_id=0;
		g.MyDatabase.m_order_editing_modified=false;
		//g.MyDatabase.m_order_editing.stuff_full_ticket_m=m_settings_ticket_m;
		//g.MyDatabase.m_order_editing.stuff_full_ticket_w=m_settings_ticket_w;
		g.Common.honorBeerman(2);
		// test
		//byte[] textData={};
		//int ix=NativeCallsClass.convertJpegFile("test", "test", textData, 0, 0, 150, 100, 75, 1);
		//

		// TODO
		/*
		final Map<String, String> clientsDescr=TextDatabase.getClientsDescrOfDistrPoint(getContentResolver(), distr_point_id);
		if (clientsDescr.size()==1){
			parameters.putString("client_id", routeRecord.distr_point_id);
			parameters.putString("client_descr", routeRecord.distr_point_id);
		}
		*/


		Cursor cursor;
		boolean result=TextDatabase.ReadOrderBy_Id(activity.getContentResolver(), rec, g.MyDatabase.m_order_editing_id);
		if (result==false) {
            g.Common.honorBeerman(4);
            bCreatedOrder = true;
            rec.Clear();
            if (client_id == null || client_id.isEmpty()) {
                rec.client_id = new MyID();
                rec.stuff_client_name = activity.getResources().getString(R.string.client_not_set);
                rec.stuff_client_address = "";
                rec.curator_id = new MyID();
                rec.stuff_curator_name = activity.getResources().getString(R.string.curator_not_set);
                rec.stuff_select_account = false;
            } else {
                intent.putExtra("backup_client_id", client_id.toString());
				intent.putExtra("backup_distr_point_id", distr_point_id.toString());
                rec.client_id = client_id.clone();
                rec.curator_id = new MyID();
                // Прочитаем название контрагента и id куратора
                Cursor clientCursor = activity.getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "curator_id", "flags"}, "id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
                if (clientCursor.moveToNext()) {
                    int descrIndex = clientCursor.getColumnIndex("descr");
                    int addressIndex = clientCursor.getColumnIndex("address");
                    int curator_idIndex = clientCursor.getColumnIndex("curator_id");
                    int flags_Index = clientCursor.getColumnIndex("flags");
                    rec.stuff_client_name = clientCursor.getString(descrIndex);
                    rec.stuff_client_address = clientCursor.getString(addressIndex);
                    rec.curator_id = new MyID(clientCursor.getString(curator_idIndex));
                    rec.stuff_select_account = (clientCursor.getInt(flags_Index) & 2) != 0;
                }
                clientCursor.close();
            }
            if (distr_point_id == null || distr_point_id.isEmpty()) {
                rec.distr_point_id = new MyID();
                rec.stuff_distr_point_name=activity.getResources().getString(R.string.trade_point_not_set);
            } else {
                rec.distr_point_id = distr_point_id.clone();
                // Наименование торговой точки
                cursor=activity.getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.distr_point_id.toString()}, null);
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
			rec.comment="";
			rec.comment_payment="";
	    	rec.stuff_debt=0;
	    	rec.stuff_debt_past=0;
	    	rec.stuff_debt_past30=0;
	    	rec.stuff_agreement_debt=0.0;
	    	rec.stuff_agreement_debt_past=0.0;
			rec.agreement30_id=new MyID();
	    	rec.agreement_id=new MyID();
			rec.stuff_agreement30_name=activity.getResources().getString(R.string.agreement30_not_set);
			rec.stuff_agreement_required=true;
	    	rec.stuff_agreement_name=activity.getResources().getString(R.string.agreement_not_set);
	    	rec.simple_discount_id=new MyID();
			rec.stuff_discount=activity.getResources().getString(R.string.without_discount);
			rec.stuff_discount_procent=0.0;
			rec.stuff_organization_name=activity.getResources().getString(R.string.organization_not_set);
			rec.stock_id=TextDatabase.getDefaultStock(activity.getContentResolver(), rec.stuff_stock_descr);
			rec.stuff_places="";
			rec.manager_comment="";
			rec.theme_comment="";
			rec.phone_num="";
    	    rec.quant_mw=0;
    	    rec.sumShipping=-1;
		} else
		{
			// 04.07.2019 это нужно для открытия документа из маршрута
			// если не из маршрута, то здесь будет пусто
			if (distr_point_id!=null) {
				intent.putExtra("backup_distr_point_id", distr_point_id.toString());
			}
			//
			if (bCopyOrder)
			{
				bCreatedOrder=true;
				// Проходим по строкам и выставляем начальное количество
				for (OrderLineRecord line:rec.lines)
				{
					if (line.quantity_requested>line.quantity)
						line.quantity=line.quantity_requested;
				}
			}
			if (bCopyOrder||rec.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
			{
				// Заказ восстановлен, пересчитаем хотя бы вес
				// а для скопированных вес считали всегда
    			rec.sumDoc=rec.GetOrderSum(null, false);
    			rec.weightDoc=TextDatabase.GetOrderWeight(activity.getContentResolver(), rec, null, false);
    			intent.putExtra("recalc_price", true);
			}
			
			// Наименование клиента
		    cursor=activity.getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr", "address", "flags"}, "id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
		    if (cursor!=null &&cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	int addressIndex = cursor.getColumnIndex("address");
		    	int flagsIndex = cursor.getColumnIndex("flags");
		    	String newWord = cursor.getString(descrIndex);
				rec.stuff_client_name=new String(newWord);
				rec.stuff_client_address=new String(cursor.getString(addressIndex));
	        	rec.stuff_select_account=(cursor.getInt(flagsIndex)&2)!=0;
		    } else
		    {
		    	rec.stuff_client_name=activity.getResources().getString(R.string.client_not_set);
		    	rec.stuff_client_address="";
		    	rec.stuff_select_account=false;
		    }
		    cursor.close();
			if (g.Common.FACTORY)
			{
				// Наименование соглашения
				cursor=activity.getContentResolver().query(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, new String[]{"descr", "sale_id", "flags"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement30_id.toString()}, null);
				if (cursor.moveToNext())
				{
					int descrIndex = cursor.getColumnIndex("descr");
					int flagsIndex = cursor.getColumnIndex("flags");
					String descr = cursor.getString(descrIndex);
					int flags = cursor.getInt(flagsIndex);
					rec.stuff_agreement30_name=new String(descr);
					rec.stuff_agreement_required=(flags&1)!=0;
					/*
					if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
					{
						int saleIdIndex = cursor.getColumnIndex("sale_id");
						String sale_id = cursor.getString(saleIdIndex);
						Cursor discountCursor=activity.getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{sale_id}, null);
						if (discountCursor!=null &&discountCursor.moveToNext())
						{
							int discountDescrIndex = discountCursor.getColumnIndex("descr");
							int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
							String discountDescr = discountCursor.getString(discountDescrIndex);
							double discountPriceProcent = discountCursor.getDouble(discountPriceProcentIndex);
							rec.simple_discount_id=new MyID(sale_id);
							rec.stuff_discount=discountDescr;
							rec.stuff_discount_procent=discountPriceProcent;
						} else
						{
							rec.simple_discount_id=new MyID();
							rec.stuff_discount=activity.getResources().getString(R.string.without_discount);
							rec.stuff_discount_procent=0.0;
						}
					} else
					{
						// не обязательно, все равно это поле невидимое
						rec.simple_discount_id=new MyID();
						rec.stuff_discount=activity.getResources().getString(R.string.without_discount);
						rec.stuff_discount_procent=0.0;
					}
					*/
					// TODO выше
					rec.simple_discount_id=new MyID();
					rec.stuff_discount=activity.getResources().getString(R.string.without_discount);
					rec.stuff_discount_procent=0.0;
				} else
				{
					rec.stuff_agreement30_name=activity.getResources().getString(R.string.agreement30_not_set);
                    rec.stuff_agreement_required=false;
					rec.stuff_discount=activity.getResources().getString(R.string.without_discount);
					rec.stuff_discount_procent=0.0;
				}
				cursor.close();
			}
			// Наименование договора
			cursor = activity.getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"descr", "sale_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
			if (cursor.moveToNext()) {
				int descrIndex = cursor.getColumnIndex("descr");
				String descr = cursor.getString(descrIndex);
				rec.stuff_agreement_name = new String(descr);
				if (g.Common.TITAN || g.Common.INFOSTART) {
					int saleIdIndex = cursor.getColumnIndex("sale_id");
					String sale_id = cursor.getString(saleIdIndex);
					Cursor discountCursor = activity.getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{sale_id}, null);
					if (discountCursor != null && discountCursor.moveToNext()) {
						int discountDescrIndex = discountCursor.getColumnIndex("descr");
						int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
						String discountDescr = discountCursor.getString(discountDescrIndex);
						double discountPriceProcent = discountCursor.getDouble(discountPriceProcentIndex);
						rec.simple_discount_id = new MyID(sale_id);
						rec.stuff_discount = discountDescr;
						rec.stuff_discount_procent = discountPriceProcent;
					} else {
						rec.simple_discount_id = new MyID();
						rec.stuff_discount = activity.getResources().getString(R.string.without_discount);
						rec.stuff_discount_procent = 0.0;
					}
				} else {
					// не обязательно, все равно это поле невидимое
					rec.simple_discount_id = new MyID();
					rec.stuff_discount = activity.getResources().getString(R.string.without_discount);
					rec.stuff_discount_procent = 0.0;
				}
			} else {
				rec.stuff_agreement_name = activity.getResources().getString(R.string.agreement_not_set);
				rec.stuff_discount = activity.getResources().getString(R.string.without_discount);
				rec.stuff_discount_procent = 0.0;
			}
			cursor.close();
		    if (g.Common.PHARAON)
		    {
		    	// Скидка в документе
			    Cursor discountCursor=activity.getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{rec.simple_discount_id.toString()}, null);
		    	if (discountCursor.moveToNext())
		    	{
			    	int discountDescrIndex = discountCursor.getColumnIndex("descr");
			    	int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
			    	String discountDescr = discountCursor.getString(discountDescrIndex);
			    	double discountPriceProcent = discountCursor.getDouble(discountPriceProcentIndex);
			    	rec.stuff_discount=discountDescr;
			    	rec.stuff_discount_procent=discountPriceProcent;
		    	}
		    	discountCursor.close();
		    }
	    	// Наименование организации
		    cursor=activity.getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_order_editing.organization_id.toString()}, null);
		    if (cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	String newWord = cursor.getString(descrIndex);
				rec.stuff_organization_name=new String(newWord); 
		    } else
		    {
		    	rec.stuff_organization_name=activity.getResources().getString(R.string.organization_not_set);
		    }
		    cursor.close();
		    // Наименование торговой точки
		    cursor=activity.getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_order_editing.distr_point_id.toString()}, null);
		    if (cursor.moveToNext())
		    {
		    	int descrIndex = cursor.getColumnIndex("descr");
		    	String newWord = cursor.getString(descrIndex);
				rec.stuff_distr_point_name=new String(newWord);
		    } else
		    {
		    	rec.stuff_distr_point_name=activity.getResources().getString(R.string.trade_point_not_set);
		    }
		    cursor.close();
		}
		g.MyDatabase.m_order_editing_created=bCreatedOrder;
		if (bCreatedOrder)
		{
			// В том числе копированием
			rec.uid=UUID.randomUUID();
			rec.numdoc=activity.getString(R.string.numdoc_new);// "НОВЫЙ";
			
			java.util.Date date=new java.util.Date();
			SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(activity);
			java.util.Date work_date=DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
			if (android.text.format.DateFormat.format("yyyyMMdd", date).equals(android.text.format.DateFormat.format("yyyyMMdd", work_date)))
			{
				// День совпал - берем текущее время
				rec.datedoc=Common.getDateTimeAsString14(date);
			} else
			{
				// Берем рабочую дату
				rec.datedoc=Common.getDateTimeAsString14(work_date);
			}
			// 23.05.2017 потребовалась точная дата, а не испорченная датой документа
			rec.datecreation=Common.getDateTimeAsString14(date);
			//
			rec.comment_closing="";
			rec.id=new MyID(); // Это код, присвоенный 1С
			rec.state=E_ORDER_STATE.E_ORDER_STATE_CREATED;
			if (bCopyOrder)
				intent.putExtra("copy_order_id", g.MyDatabase.m_order_editing_id);
			g.MyDatabase.m_order_editing_id=0; // Чтобы при записи не перезаписать уже существующий документ
			rec.dont_need_send=0;
			rec.version=0;
			rec.version_ack=0;
			rec.versionPDA=0;
			rec.versionPDA_ack=0;
			rec.shipping_type=0;
			rec.shipping_time=0;
    		rec.shipping_begin_time="0000";
    		rec.shipping_end_time="2359";
    		rec.shipping_date="";
			if (g.Common.FACTORY)
			{
				rec.shipping_date=new String(rec.datedoc);
			}
    		if (g.Common.PHARAON)
    		{
        		rec.shipping_begin_time="";
    			rec.create_client=1;
    		} else
    		{
    			rec.create_client=0;
    		}
    	    rec.create_client_surname="";
    	    rec.create_client_firstname="";
    	    rec.create_client_lastname="";
    	    //rec.create_client_phone="";
    	    //rec.place_num=0;
    	    //rec.stuff_places="";
    	    rec.card_num=0;
    	    rec.pay_credit=0.0;
    	    //rec.ticket_m=m_settings_ticket_m;
    	    //rec.ticket_w=m_settings_ticket_w;
    	    //rec.quant_m=0;
    	    //rec.quant_w=0;
			
    	    // В старом формате сразу после создания пустой документ записывается
			if (!g.Common.NEW_BACKUP_FORMAT&&g.Common.BACKUP_NOT_SAVED_ORDERS)
			{
				rec.state=E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED;
				g.MyDatabase.m_order_editing_id=TextDatabase.SaveOrderSQL(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id, 0);
			}
			rec.latitude=0;
			rec.longitude=0;
			rec.gpsaccuracy=0.0;
			rec.gpsstate=-1;
			rec.accept_coord=1;
			if (g.Common.PHARAON)
			{
				// заляп (потому, что клиент не всегда выбирается, а иногда создается)
				rec.price_type_id=new MyID(MyWebExchange.dummyId);
			}
		}
		// Долг контрагента
	    cursor=activity.getContentResolver().query(MTradeContentProvider.SALDO_CONTENT_URI, new String[]{"saldo", "saldo_past", "saldo_past30"}, "client_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
	    if (cursor!=null &&cursor.moveToNext())
	    {
	    	int indexDebt = cursor.getColumnIndex("saldo");
	    	int indexDebtPast = cursor.getColumnIndex("saldo_past");
	    	int indexDebtPast30 = cursor.getColumnIndex("saldo_past30");
			rec.stuff_debt=cursor.getDouble(indexDebt); 
			rec.stuff_debt_past=cursor.getDouble(indexDebtPast);
			rec.stuff_debt_past30=cursor.getDouble(indexDebtPast30);
	    } else
	    {
	    	rec.stuff_debt=0;
	    	rec.stuff_debt_past=0;
	    	rec.stuff_debt_past30=0;
	    }
	    cursor.close();
	    // Долг контрагента по договору
	    if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
	    {
	    	double agreement_debt=0;
	    	double agreement_debt_past=0;
		    Cursor cursorAgreementDebt=activity.getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{rec.agreement_id.toString()}, null);
		    int saldoIndex=cursorAgreementDebt.getColumnIndex("saldo");
		    int saldoPastIndex=cursorAgreementDebt.getColumnIndex("saldo_past");
		    while (cursorAgreementDebt.moveToNext())
		    {
		    	agreement_debt=agreement_debt+cursorAgreementDebt.getDouble(saldoIndex);
		    	agreement_debt_past=agreement_debt_past+cursorAgreementDebt.getDouble(saldoPastIndex);
		    }
		    cursorAgreementDebt.close();
	    	rec.stuff_agreement_debt=agreement_debt;
	    	rec.stuff_agreement_debt_past=agreement_debt_past;
	    }
	    // Куратор
	    cursor=activity.getContentResolver().query(MTradeContentProvider.CURATORS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{rec.curator_id.toString()}, null);
	    if (cursor!=null &&cursor.moveToNext())
	    {
	    	int indexDescr = cursor.getColumnIndex("descr");
			g.MyDatabase.m_order_editing.stuff_curator_name=cursor.getString(indexDescr);
	    } else
	    {
	    	g.MyDatabase.m_order_editing.stuff_curator_name=activity.getResources().getString(R.string.curator_not_set);
	    }
	    cursor.close();
	    
		// Список всех возможных организаций
	    /*
		ArrayList<String>list_organizations_descr = new ArrayList<String>();
		ArrayList<String>list_organizations_id = new ArrayList<String>();
	    cursor=getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr", "id"}, null, null, "descr ASC");
	    
	    list_organizations_descr.add(getResources().getString(R.string.item_not_set));
	    list_organizations_id.add(new MyID().toString());
	    
	    int foundOrganization=-1;
	    
	    if (cursor!=null)
	    {
	    	int descrIndex = cursor.getColumnIndex("descr");
	    	int idIndex = cursor.getColumnIndex("id");
	    	
	    	while (cursor.moveToNext())
		    {
		    	String organizationDescr=cursor.getString(descrIndex);
		    	String organizationId=cursor.getString(idIndex);
		    	if (rec.organization_id.m_id.equals(organizationId))
		    	{
		    		foundOrganization=list_organizations_descr.size();
		    	}
		    	list_organizations_descr.add(organizationDescr);
		    	list_organizations_id.add(organizationId);
		    }
	    	
	    	// Организация не найдена
	    	if (foundOrganization<=0)
	    	{
    			// Сбросим организацию
    			rec.organization_id=new MyID();
	    	}
	    }
	    */
	    
		// Список всех возможных типов учета
		ArrayList<String>list_accounts_descr = new ArrayList<String>();
		ArrayList<String>list_accounts_id = new ArrayList<String>();
	    
		list_accounts_descr.add("УПР");
		list_accounts_id.add("0");
		list_accounts_descr.add("ОБЩ");
		list_accounts_id.add("1");
		if (g.Common.MEGA)
		{
			list_accounts_descr.add("Кега");
			list_accounts_id.add("2");
			list_accounts_descr.add("БПЗ");
			list_accounts_id.add("3");
		}
	    
		// Список всех возможных типов цен
		ArrayList<String>list_prices_descr = new ArrayList<String>();
		ArrayList<String>list_prices_id = new ArrayList<String>();
	    
	    list_prices_descr.add(activity.getResources().getString(R.string.item_not_set));
	    list_prices_id.add(new MyID().toString());
	    
	    if (g.Common.MEGA)
	    {
	    	//list_prices_descr.add(g.MyDatabase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_OPT));
	    	list_prices_descr.add("Оптовая");
	    	list_prices_id.add(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_OPT.value()));

	    	//list_prices_descr.add(g.MyDatabase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT));
	    	list_prices_descr.add("М-Оптовая");
	    	list_prices_id.add(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value()));

	    	//list_prices_descr.add(g.MyDatabase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_ROZN));
	    	list_prices_descr.add("Розничная");
	    	list_prices_id.add(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_ROZN.value()));
	    	
	    } else
	    {
		    Cursor priceTypesCursor=activity.getContentResolver().query(MTradeContentProvider.PRICETYPES_CONTENT_URI, new String[]{"descr", "id"}, null, null, "descr ASC");
		    
		    int foundPriceType=-1;
		    
		    if (priceTypesCursor!=null)
		    {
		    	int descrIndex = priceTypesCursor.getColumnIndex("descr");
		    	int idIndex = priceTypesCursor.getColumnIndex("id");
		    	
		    	while (priceTypesCursor.moveToNext())
			    {
			    	String priceDescr=priceTypesCursor.getString(descrIndex);
			    	String priceId=priceTypesCursor.getString(idIndex);
			    	if (rec.price_type_id.m_id.equals(priceId))
			    	{
			    		foundPriceType=list_prices_descr.size();
			    	}
			    	list_prices_descr.add(priceDescr);
			    	list_prices_id.add(priceId);
			    }
		    	priceTypesCursor.close();
		    	
		    	// Тип цены не найден
		    	if (foundPriceType<=0)
		    	{
	    			// Сбросим тип цены
	    			rec.price_type_id=new MyID();
		    	}
		    }
	    }
	    
	    // Склад
	    int defaultStocksCount=0;
	    int defaultStock=-1; 
	    int defaultActionStocksCount=0;
	    int defaultActionStock=-1;
	    int foundStock=-1;
		// Список всех возможных складов
		ArrayList<String>list_stocks_descr = new ArrayList<String>();
		ArrayList<String>list_stocks_id = new ArrayList<String>();
	    cursor=activity.getContentResolver().query(MTradeContentProvider.STOCKS_CONTENT_URI, new String[]{"descr", "id", "flags"}, null, null, "descr ASC");
	    
    	list_stocks_descr.add(activity.getResources().getString(R.string.item_not_set));
    	list_stocks_id.add(new MyID().toString());
	    
	    if (cursor!=null)
	    {
	    	int descrIndex = cursor.getColumnIndex("descr");
	    	int idIndex = cursor.getColumnIndex("id");
	    	int flagsIndex = cursor.getColumnIndex("flags");
	    	
	    	while (cursor.moveToNext())
		    {
		    	String stockDescr=cursor.getString(descrIndex);
		    	String stockId=cursor.getString(idIndex);
		    	int flags=cursor.getInt(flagsIndex);
		    	if (rec.stock_id.m_id.equals(stockId))
		    	{
		    		foundStock=list_stocks_descr.size();
		    	}
		    	// по умолчанию
		    	if ((flags&1)!=0)
		    	{
		    		if ((flags&2)!=0)
		    		{
		    			// по умолчанию акционный склад
		    			defaultActionStocksCount++;
		    			defaultActionStock=list_stocks_descr.size();
		    		} else
		    		{
		    			// по умолчанию обычный склад
		    			defaultStocksCount++;
		    			defaultStock=list_stocks_descr.size();
		    		}
		    	}
		    	list_stocks_descr.add(stockDescr);
		    	list_stocks_id.add(stockId);
		    }
	    	
	    	// Склад не найден
	    	if (foundStock<=0)
	    	{
	    		if (defaultStocksCount==1&&bCreatedOrder)
	    		{
	    			// Установим этот склад по умолчанию
	    			rec.stock_id=new MyID(list_stocks_id.get(defaultStock));
	    			// что делать с акционными складами по умолчанию пока не понятно
	    		} else
	    		{
	    			// Сбросим склад, если он не найден и это не новый документ 
	    			rec.stock_id=new MyID();
	    		}
	    	}
	    	cursor.close();
	    }
		
	    Bundle bundle = new Bundle();
	    //bundle.putStringArray("list_organizations_descr",(String[])list_organizations_descr.toArray(new String[0]));
	    //bundle.putStringArray("list_organizations_id",(String[])list_organizations_id.toArray(new String[0]));
	    bundle.putStringArray("list_accounts_descr",(String[])list_accounts_descr.toArray(new String[0]));
	    bundle.putStringArray("list_accounts_id",(String[])list_accounts_id.toArray(new String[0]));
	    
	    bundle.putStringArray("list_prices_descr",(String[])list_prices_descr.toArray(new String[0]));
	    bundle.putStringArray("list_prices_id",(String[])list_prices_id.toArray(new String[0]));
	    bundle.putStringArray("list_stocks_descr",(String[])list_stocks_descr.toArray(new String[0]));
	    bundle.putStringArray("list_stocks_id",(String[])list_stocks_id.toArray(new String[0]));
	    intent.putExtra("extrasBundle", bundle);
		
	}

	static public int fillOrderColor(int defaultTextColor, Resources resources, int color, View viewBackground)
	{
		//
		//View w1 = (View) view.getParent();
		//TextView tv = (TextView) view;
        int backgroundColor;
		int textColor;

		switch (color) {
			case 1: // не готова, не оправлять (желтая)
                backgroundColor=Color.YELLOW;
				color=Color.BLACK;
				break;
			case 2: // запрос отмены
                backgroundColor=Color.GRAY;
                color=Color.WHITE;
				break;
			case 3:
				// восстановлен, сбой
				// неизвестное состояние
                backgroundColor=Color.RED;
				color=Color.BLACK;
				break;
			case 4: // с признаком выполнена не полностью (closed_not_full)
                backgroundColor=Color.BLUE; // синий
				color=Color.BLACK;
				break;
			case 5: // с признаком отправлена
                backgroundColor=Color.CYAN;
				color=Color.BLACK;
				break;
			case 6: // с признаком согласование
                backgroundColor=resources.getColor(R.color.ORANGE);
				color=Color.BLACK;
				break;
			case 7: // отменена
                backgroundColor=Color.RED;
				color=Color.WHITE;
				break;
			case 10: // заявку изменили, зеленая
				//w1.setBackgroundColor(Color.rgb(0, 100, 0));
                backgroundColor=resources.getColor(R.color.DARK_GREEN);
				color=Color.WHITE;
				break;
			default:
                backgroundColor=Color.TRANSPARENT;
                color=defaultTextColor;
		}
		viewBackground.setBackgroundColor(backgroundColor);
		return color;
	}
	
	public static void recalcPrice(Activity activity)
	{
		MySingleton g=MySingleton.getInstance();
		double oldOrderSum=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
		// пересчет стоимости билетов
		/*
		if (g.Common.PHARAON)
		{
			g.MyDatabase.m_order_editing.ticket_m=Math.floor(g.MyDatabase.m_order_editing.stuff_full_ticket_m*(100.0+g.MyDatabase.m_order_editing.stuff_discount_procent)+0.00001)/100.0;
			g.MyDatabase.m_order_editing.ticket_w=Math.floor(g.MyDatabase.m_order_editing.stuff_full_ticket_w*(100.0+g.MyDatabase.m_order_editing.stuff_discount_procent)+0.00001)/100.0;
		}
		*/
		// пересчет по строкам документа
		for (OrderLineRecord line:g.MyDatabase.m_order_editing.lines)
		{
			//
			line.discount=0.0;
			//
			if (g.Common.MEGA)
			{
				String priceName="rozn_price";
				if (g.MyDatabase.m_order_editing.price_type_id.equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value())))
				{
					priceName="m_opt_price";
				} else
				if (g.MyDatabase.m_order_editing.price_type_id.equals(String.valueOf(E_PRICE_TYPE.E_PRICE_TYPE_OPT.value())))
				{
					priceName="opt_price";
				}
				Cursor cursorNomenclature=activity.getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{priceName}, "id=?", new String[]{line.nomenclature_id.toString()}, null);
				double price=0.0;
				double price_k=1.0;
				if (cursorNomenclature.moveToNext())
				{
					price=cursorNomenclature.getDouble(0);
					CuratorPriceRecord curatorPrice=TextDatabase.GetCuratorPrice(activity.getContentResolver(), g.MyDatabase.m_order_editing.curator_id, line.nomenclature_id);
					if (curatorPrice!=null)
					{
						price+=Math.floor(price*curatorPrice.priceProcent+0.00001)/100.0+curatorPrice.priceAdd;
					}
					// здесь прайс это стоимость штуки или упаковки
					if (price_k-line.k>-0.0001&&price_k-line.k<0.0001)
					{
						// единица измерения совпала
						line.price=price;
					} else
					{
						// разные коэффициенты
						line.price=Math.floor(price*line.k/price_k*100.0+0.00001)/100.0;
					}
					line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
				} else
				{
					// Это невозможно
					line.price=0.0;
					line.total=0.0;
				}
				cursorNomenclature.close();
			} else
			if (g.Common.TITAN||g.Common.PHARAON||g.Common.INFOSTART||g.Common.FACTORY)
			{
				Cursor cursorPrice=activity.getContentResolver().query(MTradeContentProvider.PRICES_CONTENT_URI, new String[]{"price", "k"}, "nomenclature_id=? and price_type_id=?", new String[]{line.nomenclature_id.toString(), g.MyDatabase.m_order_editing.price_type_id.toString()}, null);
				if (cursorPrice.moveToFirst())
				{
					int priceIndex=cursorPrice.getColumnIndex("price");
					int price_k_Index=cursorPrice.getColumnIndex("k");
					double price=cursorPrice.getDouble(priceIndex);
					double price_k=cursorPrice.getDouble(price_k_Index);
					double priceProcent=g.MyDatabase.m_order_editing.stuff_discount_procent;
	    			if (priceProcent!=0.0)
	    			{
	    				price+=Math.floor(price*priceProcent+0.00001)/100.0;
	    			}
					// здесь прайс это стоимость штуки или упаковки
					if (price_k-line.k>-0.0001&&price_k-line.k<0.0001)
					{
						// единица измерения совпала
						line.price=price;
					} else
					{
						// разные коэффициенты
						line.price=Math.floor(price*line.k/price_k*100.0+0.00001)/100.0;
					}
					line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
				} else
				{
					line.price=0.0;
					line.total=0.0;
				}
				cursorPrice.close();

                // TODO Код подобный есть в нескольких местах, возможно, перенести в процедуру
                if (g.Common.FACTORY&&!g.MyDatabase.m_order_editing.agreement30_id.isEmpty())
                {
                    // Прайс в соглашении
                    // если задано, заменяет существующую цену
                    cursorPrice=activity.getContentResolver().query(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, new String[]{"ed_izm_id", "edIzm", "price", "k"}, "agreement30_id=? and nomenclature_id=?", new String[]{g.MyDatabase.m_order_editing.agreement30_id.toString(), line.nomenclature_id.toString()}, null);
                    if (cursorPrice.moveToFirst())
                    {
                        int priceIndex=cursorPrice.getColumnIndex("price");
                        int price_k_Index=cursorPrice.getColumnIndex("k");
                        double price=cursorPrice.getDouble(priceIndex);
                        double price_k=cursorPrice.getDouble(price_k_Index);
                        double priceProcent=g.MyDatabase.m_order_editing.stuff_discount_procent;
                        if (priceProcent!=0.0)
                        {
                            price+=Math.floor(price*priceProcent+0.00001)/100.0;
                        }
                        // здесь прайс это стоимость штуки или упаковки
                        if (price_k-line.k>-0.0001&&price_k-line.k<0.0001)
                        {
                            // единица измерения совпала
                            line.price=price;
                        } else
                        {
                            // разные коэффициенты
                            line.price=Math.floor(price*line.k/price_k*100.0+0.00001)/100.0;
                        }
                        line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
                    }
                    cursorPrice.close();
                }

			} else				
			{
				Cursor cursorPrice=activity.getContentResolver().query(MTradeContentProvider.PRICES_CONTENT_URI, new String[]{"price", "k"}, "nomenclature_id=? and price_type_id=?", new String[]{line.nomenclature_id.toString(), g.MyDatabase.m_order_editing.price_type_id.toString()}, null);
				if (cursorPrice.moveToFirst())
				{
					int priceIndex=cursorPrice.getColumnIndex("price");
					int price_k_Index=cursorPrice.getColumnIndex("k");
	        		//int price_add_index=cursorPrice.getColumnIndex("priceAdd");
	        		//int price_procent_index=cursorPrice.getColumnIndex("priceProcent");
					double price=cursorPrice.getDouble(priceIndex);
					double price_k=cursorPrice.getDouble(price_k_Index);
					/*
	    			double priceAdd=cursorPrice.getDouble(price_add_index);
	    			double priceProcent=cursorPrice.getDouble(price_procent_index);
					if (price_k>-0.0001&&price_k<0.0001)
					{
						price_k=1.0;
					}
					*/
					ClientPriceRecord clientPrice=TextDatabase.GetClientPrice(activity.getContentResolver(), g.MyDatabase.m_order_editing.client_id, line.nomenclature_id);
					if (clientPrice!=null)
					{
						price+=Math.floor(price*clientPrice.priceProcent+0.00001)/100.0+clientPrice.priceAdd;
					}
					/*
	    			if (priceProcent!=0.0||priceAdd!=0)
	    			{
	    				price+=Math.floor(price*priceProcent+0.00001)/100.0+priceAdd;
	    			}
	    			*/
					// здесь прайс это стоимость штуки или упаковки
					if (price_k-line.k>-0.0001&&price_k-line.k<0.0001)
					{
						// единица измерения совпала
						line.price=price;
					} else
					{
						// разные коэффициенты
						line.price=Math.floor(price*line.k/price_k*100.0+0.00001)/100.0;
					}
					line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
				} else
				{
					line.price=0.0;
					line.total=0.0;
				}
				cursorPrice.close();
			}
			if (g.MyDatabase.m_orderLinesAdapter!=null)
			{
				g.MyDatabase.m_orderLinesAdapter.notifyDataSetChanged();				
			}
		}
		// Нужно пересчитать цены при изменении типа цены
		double newOrderSum=g.MyDatabase.m_order_editing.GetOrderSum(null, false);
		if (oldOrderSum!=newOrderSum)
		{
			// При этом значение в заказ все равно не записывается (только при записи)
			//Toast.makeText(this, "Цены пересчитаны, сумма заказа "+g.Common.DoubleToStringFormat(newOrderSum, "%.3f"), Toast.LENGTH_SHORT).show();
			Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.message_price_recalc_sum, Common.DoubleToStringFormat(newOrderSum, "%.3f")), Snackbar.LENGTH_SHORT).show();
		}
	}
	
	static final String ACTION = "refreshSumWeight";
	
	public static void redrawSumWeight(Activity activity)
	{
		Intent intent = new Intent(ACTION);
		//intent.putExtra("test", "test");
		LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
	}

	// Только для FACTORY
	public static boolean onAgreement30Selected_RestrictedVer(Activity activity, View view0, long _id, String[] list_prices_id, String[] list_prices_descr)
	{
		MySingleton g=MySingleton.getInstance();
		if (_id!=0)
		{
			Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, _id);
			Cursor agreements30Cursor=activity.getContentResolver().query(singleUri, new String[]{"id", "descr", "organization_id", "price_type_id", "sale_id", "flags"}, null, null, null);
			if (agreements30Cursor.moveToNext())
			{
				int idIndex = agreements30Cursor.getColumnIndex("id");
				int descrIndex = agreements30Cursor.getColumnIndex("descr");
				int organization_idIndex = agreements30Cursor.getColumnIndex("organization_id");
				int priceType_idIndex = agreements30Cursor.getColumnIndex("price_type_id");
				int flagsIndex = agreements30Cursor.getColumnIndex("flags");
				String agreement30Id = agreements30Cursor.getString(idIndex);
				String agreement30Descr = agreements30Cursor.getString(descrIndex);
				String organizationId = agreements30Cursor.getString(organization_idIndex);
				String priceTypeId = agreements30Cursor.getString(priceType_idIndex);
				int flags = agreements30Cursor.getInt(flagsIndex);
				if (g.MyDatabase.m_order_editing.agreement30_id.toString().equals(agreement30Id))
				{
					return false;
				}
				if (view0!=null)
				{
					EditText etAgreement30=(EditText)view0.findViewById(R.id.etAgreement30);
					etAgreement30.setText(agreement30Descr);
				}
                // заполняем организацию
                Cursor cursorOrganizations=activity.getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId}, null);
                if (cursorOrganizations.moveToNext())
                {
                    int descrIndexOrganization = cursorOrganizations.getColumnIndex("descr");
                    String descrOrganization = cursorOrganizations.getString(descrIndexOrganization);
                    g.MyDatabase.m_order_editing.organization_id=new MyID(organizationId);
                    g.MyDatabase.m_order_editing.stuff_organization_name=descrOrganization;
                    if (view0!=null)
                    {
                        TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization30);
                        tvOrganization.setText(descrOrganization);
                    }
                } else
                {
                    if (view0!=null)
                    {
                        TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization30);
                        tvOrganization.setText(activity.getResources().getString(R.string.organization_not_set));
                    }
                    g.MyDatabase.m_order_editing.organization_id.m_id="";
                    g.MyDatabase.m_order_editing.stuff_organization_name=activity.getResources().getString(R.string.item_not_set);
                }
                cursorOrganizations.close();
				//
				g.MyDatabase.m_order_editing.agreement30_id=new MyID(agreement30Id);
				g.MyDatabase.m_order_editing.stuff_agreement30_name=agreement30Descr;
                g.MyDatabase.m_order_editing.stuff_agreement_required=(flags&1)!=0;
                LinearLayout layoutAgreement=null;
                if (view0!=null)
                    layoutAgreement=(LinearLayout)view0.findViewById(R.id.layoutAgreement);
                if (g.MyDatabase.m_order_editing.stuff_agreement_required)
                {
                    if (layoutAgreement!=null)
                        layoutAgreement.setVisibility(View.VISIBLE);
                    // Код, аналогичный тому, что выполняется при выборе клиента
                    boolean bWrongAgreement=true;
                    // Проверим, принадлежит ли договор текущему контрагенту
                    Cursor agreementsCursor=activity.getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id", "organization_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
                    if (agreementsCursor.moveToNext())
                    {
                        int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
                        int checkOrganization_idIndex = agreementsCursor.getColumnIndex("organization_id"); // проверка организации актуальна только для FACTORY
                        String owner_id = agreementsCursor.getString(owner_idIndex);
                        String organization_id = agreementsCursor.getString(checkOrganization_idIndex);
                        // Организация и контрагент в договоре должны соответствовать соглашению
                        if (g.MyDatabase.m_order_editing.organization_id.toString().equals(organization_id)&&
                            g.MyDatabase.m_order_editing.client_id.toString().equals(owner_id))
                                bWrongAgreement=false;
                    }
                    agreementsCursor.close();
                    if (bWrongAgreement)
                    {
                        // Не принадлежит, установим договор по умолчанию, если он единственный
                        Cursor defaultAgreementCursor=activity.getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=? and organization_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString(), g.MyDatabase.m_order_editing.organization_id.toString()}, null);
                        if (defaultAgreementCursor.moveToNext())
                        {
                            int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
                            Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
                            if (!defaultAgreementCursor.moveToNext())
                            {
                                // Есть единственный договор
                                onAgreementSelected_RestrictedVer(activity, view0, _idAgreement, list_prices_id, list_prices_descr);
                            } else
                            {
                                // Есть несколько договоров
                                onAgreementSelected_RestrictedVer(activity, view0, 0, list_prices_id, list_prices_descr);
                            }
                        } else
                        {
                            // Договора нет
                            onAgreementSelected_RestrictedVer(activity, view0, 0, list_prices_id, list_prices_descr);
                        }
                        defaultAgreementCursor.close();
                    }
                } else
                {
                    if (layoutAgreement!=null)
                        layoutAgreement.setVisibility(View.GONE);
                }
				// Тип цены хранится в соглашении для этого типа формата
				if (onPriceTypeSelected_RestrictedVer(activity, view0, new MyID(priceTypeId), list_prices_id, list_prices_descr)) {
					recalcPrice(activity);
					redrawSumWeight(activity);
				} else
				{
					// В соглашении могут стоять свои цены, поэтому пересчитываем в любом случае
					recalcPrice(activity);
					redrawSumWeight(activity);
				}
				/*
				if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
				{
					// Скидка
					int saleIdIndex = agreementsCursor.getColumnIndex("sale_id");
					String sale_id = agreementsCursor.getString(saleIdIndex);
					Cursor discountCursor=activity.getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{sale_id}, null);
					if (discountCursor.moveToNext())
					{
						int discountDescrIndex = discountCursor.getColumnIndex("descr");
						int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
						String discountDescr = discountCursor.getString(discountDescrIndex);
						g.MyDatabase.m_order_editing.simple_discount_id=new MyID(sale_id);
						g.MyDatabase.m_order_editing.stuff_discount=discountDescr;
						g.MyDatabase.m_order_editing.stuff_discount_procent=discountCursor.getDouble(discountPriceProcentIndex);
					} else
					{
						g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
						g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
						g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;
					}
					if (view0!=null)
					{
						EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
						etDiscount.setText(g.MyDatabase.m_order_editing.stuff_discount);
					}
					discountCursor.close();
				} else
				{
					g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
				}
				*/
				/*
				if (onPriceTypeSelected_RestrictedVer(activity, view0, new MyID(priceTypeId), list_prices_id, list_prices_descr))
				{
					recalcPrice(activity);
					redrawSumWeight(activity);
				}
				// Долг контрагента по договору
				if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
				{
					double agreement_debt=0;
					double agreement_debt_past=0;
					Cursor cursorAgreementDebt=activity.getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
					int saldoIndex=cursorAgreementDebt.getColumnIndex("saldo");
					int saldoPastIndex=cursorAgreementDebt.getColumnIndex("saldo_past");
					while (cursorAgreementDebt.moveToNext())
					{
						agreement_debt=agreement_debt+cursorAgreementDebt.getDouble(saldoIndex);
						agreement_debt_past=agreement_debt_past+cursorAgreementDebt.getDouble(saldoPastIndex);
					}
					cursorAgreementDebt.close();
					g.MyDatabase.m_order_editing.stuff_agreement_debt=agreement_debt;
					g.MyDatabase.m_order_editing.stuff_agreement_debt_past=agreement_debt_past;

					if (view0!=null)
					{
						EditText etDebt = (EditText) view0.findViewById(R.id.editTextAgreementDebt);
						etDebt.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt));
						EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextAgreementDebtPast);
						etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt_past));
					}
				}

				if (g.Common.TANDEM)
				{
					OrderActivity fragmentActivity=(OrderActivity)activity;
					if (fragmentActivity!=null)
					{
						// При изменении договора пересчитываем тару
						EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragmentEquipment");
						if (fragment1!=null)
							fragment1.myRestartLoader();
						EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragmentEquipmentTare");
						if (fragment2!=null)
						{
							fragment2.myRestartLoader();
						}
					}
				}


				if (g.Common.NEW_BACKUP_FORMAT)
				{
					TextDatabase.SetOrderSQLFields(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
				}
				*/

				return true;
			}
			agreements30Cursor.close();
		}
		// Договор не был указан и не изменился
		if (g.MyDatabase.m_order_editing.agreement30_id.isEmpty())
			return false;
		// Договор был указан, а сейчас не найден - очищаем все
		/*
		 * _RestrictedVer
		if (view0!=null)
		{
	    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
	    	TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
	    	EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
	    	etAgreement.setText(activity.getResources().getString(R.string.agreement_not_set));
	    	tvOrganization.setText(activity.getResources().getString(R.string.organization_not_set));
	    	etDiscount.setText(activity.getResources().getString(R.string.without_discount));
		}
		*/
		g.MyDatabase.m_order_editing.agreement30_id=new MyID();
		g.MyDatabase.m_order_editing.stuff_agreement30_name="";
		g.MyDatabase.m_order_editing.organization_id=new MyID();
		g.MyDatabase.m_order_editing.stuff_organization_name="";
		g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
		g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
		g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;

		if (g.Common.NEW_BACKUP_FORMAT)
		{
			TextDatabase.SetOrderSQLFields(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
		}

		return true;
	}


	
	//возвращает true в случае, если договор изменился
	public static boolean onAgreementSelected_RestrictedVer(Activity activity, View view0, long _id, String[] list_prices_id, String[] list_prices_descr)
	{
		// TODO сделать, чтобы срабатывало оповещение при изменении договора и при изменении типа цены и т.д.,
		// чтобы фрагмент сам это отрисровал уже после того, как данные изменились
		
		MySingleton g=MySingleton.getInstance();
		if (_id!=0)
		{
		    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS_CONTENT_URI, _id);
		    Cursor agreementsCursor=activity.getContentResolver().query(singleUri, new String[]{"id", "descr", "organization_id", "price_type_id", "sale_id"}, null, null, null);
		    if (agreementsCursor.moveToNext())
		    {
		    	int idIndex = agreementsCursor.getColumnIndex("id");
		    	int descrIndex = agreementsCursor.getColumnIndex("descr");
		    	int organization_idIndex = agreementsCursor.getColumnIndex("organization_id");
		    	int priceType_idIndex = agreementsCursor.getColumnIndex("price_type_id");
		    	String agreementId = agreementsCursor.getString(idIndex);
		    	String agreementDescr = agreementsCursor.getString(descrIndex);
		    	String organizationId = agreementsCursor.getString(organization_idIndex);
		    	String priceTypeId = agreementsCursor.getString(priceType_idIndex);
		    	if (g.MyDatabase.m_order_editing.agreement_id.toString().equals(agreementId))
		    	{
		    		return false;
		    	}
		    	if (g.Common.PRODLIDER)
		    	{
		    		// Тип цены сейчас в торговой точке, приоритет выше
		    	    Cursor distrPointsCursor=activity.getContentResolver().query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"_id", "price_type_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.distr_point_id.toString()}, null);
		    	    if (distrPointsCursor.moveToFirst()) 
		    	    {
		    	    	int price_type_idIndex=distrPointsCursor.getColumnIndex("price_type_id");
		    	    	MyID priceTypeIdByTradePoint=new MyID(distrPointsCursor.getString(price_type_idIndex));
		    	    	if (!priceTypeIdByTradePoint.isEmpty())
		    	    	{
		    	    		priceTypeId=priceTypeIdByTradePoint.toString();
		    	    	}
		    	    }
		    	    distrPointsCursor.close();	    	    
		    	}
		    	if (view0!=null)
		    	{
			    	EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
			    	etAgreement.setText(agreementDescr);
		    	}
		    	g.MyDatabase.m_order_editing.agreement_id=new MyID(agreementId);
		    	g.MyDatabase.m_order_editing.stuff_agreement_name=agreementDescr;
		    	if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
		    	{
		    		// Скидка
			    	int saleIdIndex = agreementsCursor.getColumnIndex("sale_id");
			    	String sale_id = agreementsCursor.getString(saleIdIndex);
				    Cursor discountCursor=activity.getContentResolver().query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"descr", "priceProcent"}, "id=?", new String[]{sale_id}, null);
				    if (discountCursor.moveToNext())
				    {
				    	int discountDescrIndex = discountCursor.getColumnIndex("descr");
				    	int discountPriceProcentIndex = discountCursor.getColumnIndex("priceProcent");
				    	String discountDescr = discountCursor.getString(discountDescrIndex);
				    	g.MyDatabase.m_order_editing.simple_discount_id=new MyID(sale_id);
				    	g.MyDatabase.m_order_editing.stuff_discount=discountDescr;
				    	g.MyDatabase.m_order_editing.stuff_discount_procent=discountCursor.getDouble(discountPriceProcentIndex);
				    } else
				    {
				    	g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
				    	g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
				    	g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;
				    }
			    	if (view0!=null)
			    	{
				    	EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
				    	etDiscount.setText(g.MyDatabase.m_order_editing.stuff_discount);
			    	}
			    	discountCursor.close();
		    	} else
		    	{
		    		g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
		    	}
		    	// и заполняем организацию
			    Cursor cursorOrganizations=activity.getContentResolver().query(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{organizationId}, null);
			    if (cursorOrganizations.moveToNext())
			    {
			    	int descrIndexOrganization = cursorOrganizations.getColumnIndex("descr");
			    	String descrOrganization = cursorOrganizations.getString(descrIndexOrganization);
					g.MyDatabase.m_order_editing.organization_id=new MyID(organizationId);
					g.MyDatabase.m_order_editing.stuff_organization_name=descrOrganization;
			    	if (view0!=null)
			    	{
				    	TextView tvOrganization=(TextView)view0.findViewById(g.Common.FACTORY?R.id.textViewOrdersOrganization30:R.id.textViewOrdersOrganization);
				    	tvOrganization.setText(descrOrganization);
			    	}
			    } else
			    {
			    	if (view0!=null)
			    	{
				    	TextView tvOrganization=(TextView)view0.findViewById(g.Common.FACTORY?R.id.textViewOrdersOrganization30:R.id.textViewOrdersOrganization);
				    	tvOrganization.setText(activity.getResources().getString(R.string.organization_not_set));
			    	}
			    	g.MyDatabase.m_order_editing.organization_id.m_id="";
			    	g.MyDatabase.m_order_editing.stuff_organization_name=activity.getResources().getString(R.string.item_not_set);
			    }
				cursorOrganizations.close();
			    if (g.Common.FACTORY)
                {
                    // Тип цены хранится в соглашении
                } else {
                    if (onPriceTypeSelected_RestrictedVer(activity, view0, new MyID(priceTypeId), list_prices_id, list_prices_descr)) {
                        recalcPrice(activity);
                        redrawSumWeight(activity);
                    }
                }
			    // Долг контрагента по договору
			    if (g.Common.TITAN||g.Common.INFOSTART||g.Common.FACTORY)
			    {
			    	double agreement_debt=0;
			    	double agreement_debt_past=0;
				    Cursor cursorAgreementDebt=activity.getContentResolver().query(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, new String[]{"saldo", "saldo_past"}, "agreement_id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
				    int saldoIndex=cursorAgreementDebt.getColumnIndex("saldo");
				    int saldoPastIndex=cursorAgreementDebt.getColumnIndex("saldo_past");
				    while (cursorAgreementDebt.moveToNext())
				    {
				    	agreement_debt=agreement_debt+cursorAgreementDebt.getDouble(saldoIndex);
				    	agreement_debt_past=agreement_debt_past+cursorAgreementDebt.getDouble(saldoPastIndex);
				    }
				    cursorAgreementDebt.close();
			    	g.MyDatabase.m_order_editing.stuff_agreement_debt=agreement_debt;
			    	g.MyDatabase.m_order_editing.stuff_agreement_debt_past=agreement_debt_past;

			    	if (view0!=null)
			    	{
						EditText etDebt = (EditText) view0.findViewById(R.id.editTextAgreementDebt);
						etDebt.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt));
						EditText etDebtPast = (EditText) view0.findViewById(R.id.editTextAgreementDebtPast);
						etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_order_editing.stuff_agreement_debt_past));
			    	}
			    }
			    
			    if (g.Common.TANDEM)
			    {
			    	OrderActivity fragmentActivity=(OrderActivity)activity;
			    	if (fragmentActivity!=null)
			    	{
				    	// При изменении договора пересчитываем тару
						EquipmentRestsFragment fragment1 = (EquipmentRestsFragment)fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragmentEquipment");
						if (fragment1!=null)
							fragment1.myRestartLoader();
						EquipmentRestsFragment fragment2 = (EquipmentRestsFragment)fragmentActivity.getSupportFragmentManager().findFragmentByTag("fragmentEquipmentTare");
						if (fragment2!=null)
						{
							fragment2.myRestartLoader();
						}
			    	}
			    }

			    if (g.Common.NEW_BACKUP_FORMAT)
			    {
			    	TextDatabase.SetOrderSQLFields(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
			    }
					
		    	return true;
		    }
		    agreementsCursor.close();
		}
		// Договор не был указан и не изменился
		if (g.MyDatabase.m_order_editing.agreement_id.isEmpty())
			return false;
		// Договор был указан, а сейчас не найден - очищаем все
		g.MyDatabase.m_order_editing.agreement_id=new MyID();
        g.MyDatabase.m_order_editing.stuff_agreement_name="";
		if (g.Common.FACTORY)
        {
            // В этом формате организация берется не из договора, а из соглашения
        } else {
            g.MyDatabase.m_order_editing.organization_id = new MyID();
            g.MyDatabase.m_order_editing.stuff_organization_name = "";
        }
		g.MyDatabase.m_order_editing.simple_discount_id=new MyID();
		g.MyDatabase.m_order_editing.stuff_discount=activity.getResources().getString(R.string.without_discount);
		g.MyDatabase.m_order_editing.stuff_discount_procent=0.0;

        if (view0!=null)
        {
            EditText etAgreement=(EditText)view0.findViewById(R.id.etAgreement);
            TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
            EditText etDiscount=(EditText)view0.findViewById(R.id.editTextDiscount);
            etAgreement.setText(activity.getResources().getString(R.string.agreement_not_set));
            tvOrganization.setText(activity.getResources().getString(R.string.organization_not_set));
            etDiscount.setText(activity.getResources().getString(R.string.without_discount));
        }

        if (g.Common.NEW_BACKUP_FORMAT)
	    {
	    	TextDatabase.SetOrderSQLFields(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"agreement_id", "simple_discount_id"});
	    }
		
		return true;
	}
	
	
	public static boolean onPriceTypeSelected_RestrictedVer(Activity activity, View view0, MyID price_type_id, String[] list_prices_id, String[] list_prices_descr)
	{
		MySingleton g=MySingleton.getInstance();
	    // Проверяем, изменился ли тип цены
	    if (!g.MyDatabase.m_order_editing.price_type_id.equals(price_type_id))
	    {
	    	g.MyDatabase.m_order_editing.price_type_id=price_type_id;
	    	if (view0!=null&&list_prices_id!=null&&list_prices_descr!=null)
	    	{
				Spinner spinnerPriceType=(Spinner)view0.findViewById(R.id.spinnerPriceType);
				EditText etPriceType=(EditText)view0.findViewById(R.id.etPriceType);
		        int i;
		        for (i=0; i<list_prices_id.length; i++)
		        {
		        	if (list_prices_id[i].equals(g.MyDatabase.m_order_editing.price_type_id.toString()))
		        	{
		        		if (!Constants.ENABLE_PRICE_TYPE_SELECT)
		        		{
		        			etPriceType.setText(list_prices_descr[i]);
		        		} else
		        		{
		        			spinnerPriceType.setSelection(i);
		        		}
		        	}
		        }
	    	}
	    	
		    if (g.Common.NEW_BACKUP_FORMAT)
		    {
		    	TextDatabase.SetOrderSQLFields(activity.getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id, new String[]{"price_type_id"});
		    }
	    	
	    	return true;
	    }
		return false;
	}

	static public void fillOrdersHistoryPeriods(Context context, ContentValues cv)
	{
		// Даты периодов
		// Если сегодня 30.11.,
		// то 4-ая неделя это с 24.11. по 30.11,
		// 3-тья неделя с 17.11. по 23.11,
		// 2-ая неделя это с 10.11. по 16.11,
		// 1-ая неделя с 03.11 по 09.11.

		Calendar c = Calendar.getInstance();
		java.util.Date date = c.getTime();

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		// Если текущая дата больше установленной рабочей даты, используем ее
		java.util.Date work_date = DatePreference.getDateFor(sharedPreferences, "work_date").getTime();
		if (work_date.compareTo(date) < 0)
			work_date=date;
		c.setTime(date);
		// Хотя время не обязательно выставлять
		c.set(Calendar.AM_PM, Calendar.AM);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		// Рабочая дата
		c.set(Calendar.YEAR, 2020);
		c.set(Calendar.MONTH, 10);
		c.set(Calendar.DAY_OF_MONTH, 30);
		// Конец 4-й недели
		//c.add(Calendar.DATE, -1);
		cv.put("dte", Common.MyDateFormat("yyyyMMdd", c)+"Z");
		cv.put("dt4e", Common.MyDateFormat("yyyyMMdd", c)+"Z");
		c.add(Calendar.DATE, -6);
		cv.put("dt4b", Common.MyDateFormat("yyyyMMdd", c));
		c.add(Calendar.DATE, -1);
		cv.put("dt3e", Common.MyDateFormat("yyyyMMdd", c)+"Z");
		c.add(Calendar.DATE, -6);
		cv.put("dt3b", Common.MyDateFormat("yyyyMMdd", c));
		c.add(Calendar.DATE, -1);
		cv.put("dt2e", Common.MyDateFormat("yyyyMMdd", c)+"Z");
		c.add(Calendar.DATE, -6);
		cv.put("dt2b", Common.MyDateFormat("yyyyMMdd", c));
		c.add(Calendar.DATE, -1);
		cv.put("dt1e", Common.MyDateFormat("yyyyMMdd", c)+"Z");
		c.add(Calendar.DATE, -6);
		cv.put("dt1b", Common.MyDateFormat("yyyyMMdd", c));
		cv.put("dtb", Common.MyDateFormat("yyyyMMdd", c));

	}
	
}
