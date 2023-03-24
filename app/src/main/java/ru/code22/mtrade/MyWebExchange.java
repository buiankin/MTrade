package ru.code22.mtrade;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderPlaceRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyWebExchange{
	
	static final String LOG_TAG = "mtradeLogs";
	
	// http://androidforums.ru/topic/16938-1с-wsdl-коннектимся-из-androida/
	// http://misha.beshkin.lv/otklik-wsdl-klienta-na-android/
	 
	// самая лучшая статья:
	// http://habrahabr.ru/post/145389/
	// и официальная документация
	// http://seesharpgears.blogspot.ru/2010/10/ksoap-android-web-service-tutorial-with.html
	// работа с double
	// http://seesharpgears.blogspot.ru/2010/11/implementing-ksoap-marshal-interface.html
	 
	// получение массива результатов
	// http://seesharpgears.blogspot.ru/2010/10/web-service-that-returns-array-of.html
	 
	// Проблема
	// http://stackoverflow.com/questions/14186279/ksoap2-receive-array-of-complex-objects-via-soap
	 
	// Более простой вариант
	 // http://stackoverflow.com/questions/12910619/parsing-complex-soap-response
	
	// ksoap качаем отсюда (еле нашел свежий, 29.09.2016)
	// https://oss.sonatype.org/content/repositories/ksoap2-android-releases/com/google/code/ksoap2-android/ksoap2-android-assembly/3.6.1/
	
	//public static final String dummyId="18a56c0a-ce50-4902-b5b1-c1e100f3b209";
	public static final String dummyId="38640fa5-e0a5-11d8-937b-000d884f5d5e"; // также это id розничной цены из demo_arch.zip
	public static final String dummyId_2="7398247f-2fc1-4d4e-b490-917ff511ac59";
	public static final String dummyId_3="7564084c-16e3-4396-aa3c-d5e09ba66b9b";
	
	private static final String NAMESPACE = "http://code22.ru/Pharaon/Order";
	//private static String URL = "http://192.168.0.100/pharaon/ws/service.1cws?wsdl"; 
	//private static String URL = "http://192.168.0.100/pharaon/ws/service.1cws";
	
	private static final String URL = "http://%s/pharaon/ws/service.1cws";
	
	//private static final String METHOD_NAME = "GetNomenclature";
	//private static final String SOAP_ACTION =  "http://code22.ru/Orders#Service:GetNomenclature";
	
	private static final String METHOD_NAME_SAVE_ORDERS = "SaveOrders";
	private static final String SOAP_ACTION_SAVE_ORDERS =  "http://code22.ru/Pharaon/Order#Service:SaveOrders";
	
	private static final String METHOD_NAME_GET_NOMENCLATURE = "GetNomenclature";
	private static final String SOAP_ACTION_GET_NOMENCLATURE = "http://code22.ru/Pharaon/Order#Service:GetNomenclature";
	
	private static final String METHOD_NAME_GET_CLIENTS = "GetClients";
	private static final String SOAP_ACTION_GET_CLIENTS = "http://code22.ru/Pharaon/Order#Service:GetClients";
	
	private static final String METHOD_NAME_GET_ORDERS = "GetOrders";
	private static final String SOAP_ACTION_GET_ORDERS = "http://code22.ru/Pharaon/Order#Service:GetOrders";
	
	private static final String METHOD_NAME_GET_DISCOUNTS = "GetDiscounts";
	private static final String SOAP_ACTION_GET_DISCOUNTS = "http://code22.ru/Pharaon/Order#Service:GetDiscounts";
	
	private static final String METHOD_NAME_GET_SETTINGS = "GetSettings";
	private static final String SOAP_ACTION_GET_SETTINGS = "http://code22.ru/Pharaon/Order#Service:GetSettings";
	
	private static final String METHOD_NAME_GET_PLACES = "GetPlaces";
	private static final String SOAP_ACTION_GET_PLACES = "http://code22.ru/Pharaon/Order#Service:GetPlaces";
	
	private static final String METHOD_NAME_GET_OCCUPIED_PLACES = "GetOccupiedPlaces";
	private static final String SOAP_ACTION_GET_OCCUPIED_PLACES = "http://code22.ru/Pharaon/Order#Service:GetOccupiedPlaces";
	
	private static final String METHOD_NAME_QUERY_ORDERS = "QueryOrders";
	private static final String SOAP_ACTION_QUERY_ORDERS =  "http://code22.ru/Pharaon/Order#Service:QueryOrders";
	
	
	static String myGetPropertyAsString(SoapObject obj, String propertyName)
	{
		/*
		PropertyInfo pi = new PropertyInfo();
		obj.getPropertyInfo(0, pi);
		if (pi.getType()==SoapPrimitive.class){
		   return pi.getValue().toString();
		}
		*/
		String result=obj.getPropertyAsString(propertyName);
		if (result.equals("anyType{}"))
		{
			return "";
		}
		return result;
	}
	
	static void loadClients(Context context, ArrayList<SoapObject> objs)
	{
	    final int BULK_SIZE=300;
	    int bulk_idx_clients=0;
	    int bulk_idx_saldo=0;
	    ContentValues []clients_values= new ContentValues[BULK_SIZE];
	    ContentValues []saldo_values= new ContentValues[BULK_SIZE];
	    
	    for (SoapObject obj:objs)
	    {
			ContentValues cv=new ContentValues();
			cv.put("id", myGetPropertyAsString(obj, "GUID"));
			cv.put("code", myGetPropertyAsString(obj, "Code"));
			cv.put("descr", myGetPropertyAsString(obj, "Name"));
			cv.put("descr_lower", myGetPropertyAsString(obj, "Name").toLowerCase());
			MyID parentId=new MyID(myGetPropertyAsString(obj, "parent_id"));
			if (parentId.isEmpty())
				parentId=new MyID();
			cv.put("parent_id", parentId.toString());
			int isFolder=Integer.parseInt(myGetPropertyAsString(obj, "isFolder"));
			cv.put("isFolder", isFolder);
			if (isFolder==1)
			{
				cv.put("descrFull", "");
				cv.put("phone_num","");
				cv.put("card_num","");
				cv.put("address","");
				cv.put("address2","");
			} else
			{
				cv.put("descrFull", myGetPropertyAsString(obj, "fullName"));
				// + lastname, firstname, surname
				//
				cv.put("phone_num",myGetPropertyAsString(obj, "phone"));
				cv.put("card_num", myGetPropertyAsString(obj, "card_num"));
				cv.put("address", myGetPropertyAsString(obj, "phone"));
				cv.put("address2", myGetPropertyAsString(obj, "phone"));
			}
			cv.put("comment","");
			cv.put("curator_id", "");
			cv.put("priceType", dummyId);
			cv.put("blocked", 0);
			cv.put("flags", 0);
			cv.put("isUsed", 1);
			//context.getContentResolver().insert(MTradeContentProvider.CLIENTS_CONTENT_URI, cv);
			
        	clients_values[bulk_idx_clients]=cv;
        	bulk_idx_clients++;
        	if (bulk_idx_clients>=BULK_SIZE)
        	{
        		context.getContentResolver().bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, clients_values);
        		bulk_idx_clients=0;
        	}
	    
		 	if (isFolder!=1)
		 	{
				ContentValues cv_saldo=new ContentValues();
		 		
				cv_saldo.put("client_id", myGetPropertyAsString(obj, "GUID"));
				cv_saldo.put("saldo", Double.parseDouble(myGetPropertyAsString(obj, "debt")));
				cv_saldo.put("saldo_past", 0.0);
				cv_saldo.put("isUsed", 1);
				//context.getContentResolver().insert(MTradeContentProvider.SALDO_CONTENT_URI, cv);
				
	        	saldo_values[bulk_idx_saldo]=cv_saldo;
	        	bulk_idx_saldo++;
	        	if (bulk_idx_saldo>=BULK_SIZE)
	        	{
	        		context.getContentResolver().bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, saldo_values);
	        		bulk_idx_saldo=0;
	        	}
				
		 	}
	    }
	    
	    if (bulk_idx_clients>0)
	    {
	    	ContentValues []values_2 = new ContentValues[bulk_idx_clients];
	    	int i;
	    	for (i=0; i<bulk_idx_clients; i++)
	    	{
	    		values_2[i]=clients_values[i];
	    	}
	    	context.getContentResolver().bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values_2);
	    }
	    
	    if (bulk_idx_saldo>0)
	    {
	    	ContentValues []values_2 = new ContentValues[bulk_idx_saldo];
	    	int i;
	    	for (i=0; i<bulk_idx_saldo; i++)
	    	{
	    		values_2[i]=saldo_values[i];
	    	}
	    	context.getContentResolver().bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values_2);
	    }
	    
	}
	
	static void doInBackgroundWebService(MyDatabase myDatabase, Context context, String uid_to_print_with_prefix, String settings_agent_id, String serverAddress, int query_orders_type, String query_orders_date_begin, String query_orders_date_end) throws HttpResponseException, IOException, XmlPullParserException, ParseException, OperationApplicationException, RemoteException
	{
    	//Thread.sleep(3000);
    	//ExchangeAgent agent = new ExchangeAgent();
    	//PropertyInfo propInfoAgent=new PropertyInfo();
    	//propInfoAgent.name="Agent";
    	//propInfoAgent.setValue(m_settings_agent_id);
    	//propInfoAgent.setType(agent.getClass());
    	
    	SoapObject agent = new SoapObject(NAMESPACE, "Agent");
    	agent.addProperty("uuid", settings_agent_id);
    	
    	//propInfo.type=PropertyInfo.STRING_CLASS;
    	//request.addPropertyIfValue(propInfo, "John Smith");
    	SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
    	//SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
    	
    	// 29.09.2016 экспериментировал с параметрами, когда не работало с апачем
    	//envelope.implicitTypes = true;
    	//envelope.encodingStyle = SoapSerializationEnvelope.ENV2003;
        
    	// Says that the soap webservice is a .Net service
    	envelope.dotNet = true;
        
    	//MarshalDouble md = new MarshalDouble();
    	//md.register(envelope);
    	//envelope.addMapping(NAMESPACE, "GetListResponse", new GetListResponse().getClass());
    	//envelope.addMapping(NAMESPACE, "ExchangeDiscount", new ExchangeDiscount().getClass());
    	
    	System.setProperty("http.keepAlive", "false");
    	
    	HttpTransportSE androidHttpTransport = new HttpTransportSE(String.format(URL, serverAddress));
    	androidHttpTransport.getServiceConnection().setRequestProperty("Connection", "close");
    	androidHttpTransport.debug=true;
    	SoapObject resultsRequestSOAP;	    	
    		
    	SoapObject request;
    	//request.addProperty(propInfoAgent);
    	
    	SoapObject clientsOrders = new SoapObject(NAMESPACE, "ClientsOrders");		    	
    	SoapObject emptyOrders = new SoapObject(NAMESPACE, "EmptyOrders");
    	
    	//ExchangeOrders clientsOrders=new ExchangeOrders();
    	//ExchangeOrder clientsOrder=new ExchangeOrder();
    	
    	//clientsOrders.setProperty(index, value)
    	
    	//PropertyInfo propInfo=new PropertyInfo();
    	//propInfoAgent.name="ClientsOrders";
    	//propInfoAgent.setValue(clientsOrders);
    	//propInfoAgent.setType(clientsOrders.getClass());
    	
	    boolean bEmpty=true;

	    // один раз отправляем и забываем. если еще раз придут отсутствующие заявки
	    // тогда отправятся еще раз
	    for (Map.Entry<UUID, Integer> it : myDatabase.m_empty_orders.entrySet())
	    {
	        //bw.write("##EMPTYORDERPDA##\r\n");
	        //SaveEmptyOrder(bw, it.getKey(), it.getValue());
	    	
	    	SoapObject emptyOrder = new SoapObject(NAMESPACE, "EmptyOrder");
	    	emptyOrder.addProperty("uuidPDA", it.getKey().toString());
	    	emptyOrders.addSoapObject(emptyOrder);
	    	bEmpty=false;
	    	
	    }
	    myDatabase.m_empty_orders.clear();
	    
		ArrayList<UUID> uuids=new ArrayList<UUID>();    	        		

	    Cursor cursor;
	    if (uid_to_print_with_prefix==null||uid_to_print_with_prefix.isEmpty())
	    {
		    // Заявки со статусом "Запрос отмены отправляем всегда"
	    	cursor=context.getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0 or state="+E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value(), null, null);
	    } else
	    {
	    	String uid_to_print=uid_to_print_with_prefix.substring(1);
	    	cursor=context.getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{uid_to_print}, null);
	    }
	    if (cursor!=null)
	    {
	    	int _idIndex=cursor.getColumnIndex("_id");
		    while (cursor.moveToNext())
		    {
		        OrderRecord rec = new OrderRecord();
		        if (TextDatabase.ReadOrderBy_Id(context.getContentResolver(), rec, cursor.getLong(_idIndex)))
		        {
		            if (bEmpty)
		            {
		            	bEmpty=false;
		            }
		            
			    	SoapObject clientsOrder = new SoapObject(NAMESPACE, "ClientsOrder");		    	
		            //bw.write("##ORDERPDA##\r\n");
		            //bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		            //bw.write(String.format("%d\r\n", Common.m_mtrade_version));
		            //if (Common.PRODLIDER)
		            //{
		            //	// В файл записываем также и требуемое количество
		            //	SaveOrderToText(bw, rec, true, true);
		            //} else
		            //{
		            //	SaveOrderToText(bw, rec, true, false);
		            //}
			    	
			    	clientsOrder.addProperty("uuidPDA", rec.uid.toString());
			    	if (!rec.id.isEmpty())
			    	{
			    		clientsOrder.addProperty("GUID", rec.id.toString());
			    	} else
			    	{
			    		clientsOrder.addProperty("GUID", "");
			    	}
			    	clientsOrder.addProperty("Number", rec.numdoc);
			    	//Date date = new SimpleDateFormat("yyyyMMddkkmmss").parse(datedoc);
			    	//clientsOrder.addProperty("Date", IsoDate.dateToString(date, IsoDate.DATE_TIME));
			    	//String datedoc=rec.datedoc.substring(0, 8)+(rec.shipping_begin_time+"000000").substring(0, 6);
			    	//clientsOrder.addProperty("Date", datedoc);
			    	
			    	clientsOrder.addProperty("Date", rec.datedoc);
			    	
			    	String shipping_date=(rec.shipping_date+"0101010101").substring(0, 8)+(rec.shipping_begin_time+"000000").substring(0, 6);
			    	clientsOrder.addProperty("ShippingDate", shipping_date);
			    	
			    	// Client
			    	SoapObject client = new SoapObject(NAMESPACE, "Client");
			    	if (rec.create_client==1)
			    	{
			    		client.addProperty("uuidPDA", rec.uid.toString()); // uid клиента = uid заказа
			    		client.addProperty("firstname", rec.create_client_firstname);
			    		client.addProperty("lastname", rec.create_client_lastname);
			    		client.addProperty("surname", rec.create_client_surname);
			    	} else
			    	{
			    		client.addProperty("GUID", rec.client_id.toString());
			    	}
		    		client.addProperty("phone", rec.phone_num);
		    		client.addProperty("card_num", rec.card_num);
			    	clientsOrder.addSoapObject(client);
			    	// Products
			    	SoapObject products = new SoapObject(NAMESPACE, "Products");
				    for (OrderLineRecord line : rec.lines)
				    {
				    	SoapObject product = new SoapObject(NAMESPACE, "Product");
				    	SoapObject nomenclature = new SoapObject(NAMESPACE, "Nomenclature");
				    	nomenclature.addProperty("GUID", line.nomenclature_id.toString());
				    	product.addSoapObject(nomenclature);
				    	product.addProperty("Quantity", String.valueOf(line.quantity));
				    	product.addProperty("Price", String.valueOf(line.price));
				    	product.addProperty("Sum", String.valueOf(line.total));
				    	product.addProperty("shipping_time", line.shipping_time);
				    	product.addProperty("comment", line.comment_in_line);
				    	products.addSoapObject(product);
				    }
				    clientsOrder.addSoapObject(products);
				    // Places
			    	SoapObject places = new SoapObject(NAMESPACE, "Places");
				    for (OrderPlaceRecord line : rec.places)
				    {
				    	SoapObject place = new SoapObject(NAMESPACE, "Place0");
				    	place.addProperty("place_id", line.place_id.toString());
				    	places.addSoapObject(place);
				    }
			    	clientsOrder.addSoapObject(places);
			    	//
			    	clientsOrder.addProperty("Version", String.valueOf(rec.version));
			    	//clientsOrder.addProperty("Version_ack", String.valueOf(rec.version_ack));
			    	clientsOrder.addProperty("Version_ack", String.valueOf(rec.version));
			    	clientsOrder.addProperty("VersionPDA", String.valueOf(rec.versionPDA));
			    	clientsOrder.addProperty("VersionPDA_ack", String.valueOf(rec.versionPDA_ack));
			    	clientsOrder.addProperty("Comment", rec.comment);
			    	clientsOrder.addProperty("Kredit", String.valueOf(rec.pay_credit));
			    	clientsOrder.addProperty("Discount", rec.simple_discount_id.toString());
			    	clientsOrder.addProperty("SumDoc", String.valueOf(rec.sumDoc));
			    	//clientsOrder.addProperty("count_m", String.valueOf(rec.quant_m));
			    	//clientsOrder.addProperty("count_w", String.valueOf(rec.quant_w));
			    	clientsOrder.addProperty("card_num", String.valueOf(rec.card_num));
			    	clientsOrder.addProperty("place_num", String.valueOf(rec.stuff_places));
			    	//clientsOrder.addProperty("ticket_w", String.valueOf(rec.ticket_w));
			    	//clientsOrder.addProperty("ticket_m", String.valueOf(rec.ticket_m));
			    	clientsOrder.addProperty("state", String.valueOf(rec.state.value()));
			    	clientsOrder.addProperty("count", String.valueOf(rec.quant_mw));
			    	clientsOrder.addProperty("Manager", String.valueOf(rec.manager_comment));
			    	clientsOrder.addProperty("Theme", String.valueOf(rec.theme_comment));
			    	clientsOrder.addProperty("phone", String.valueOf(rec.phone_num));
			    	
			    	clientsOrders.addSoapObject(clientsOrder);
		            
		            uuids.add(rec.uid);
		        }
		    }
		    cursor.close();
	    }
	    if (!bEmpty)
	    {
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_SAVE_ORDERS);
	    	request.addSoapObject(agent);
	    	request.addSoapObject(emptyOrders);
	    	request.addSoapObject(clientsOrders);
	    	if (uid_to_print_with_prefix==null||uid_to_print_with_prefix.isEmpty())
	    	{
	    		request.addProperty("ToPrinterList", "false");
	    		request.addProperty("ToPrinterKO", "false");
	    	}
	    	else
	    	{
	    		String prefix_to_print=uid_to_print_with_prefix.substring(0, 1);
	    		request.addProperty("ToPrinterList", (prefix_to_print.equals("L")||prefix_to_print.equals("B"))?"true":"false");
	    		request.addProperty("ToPrinterKO", (prefix_to_print.equals("K")||prefix_to_print.equals("B"))?"true":"false");
	    	}
	    	envelope.setOutputSoapObject(request);
    		// Отправляем измененые заказы
    		Log.v(LOG_TAG, envelope.bodyOut.toString());
    		androidHttpTransport.call(SOAP_ACTION_SAVE_ORDERS, envelope);
    		Log.v(LOG_TAG, envelope.bodyIn.toString());
    		resultsRequestSOAP = (SoapObject)envelope.bodyIn;
            //SoapObject result = (SoapObject) envelope.getResponse();
            //Log.v(LOG_TAG, String.valueOf(result));
    		//Log.v(LOG_TAG, resultsRequestSOAP.toString());
    		
            if (resultsRequestSOAP.getPropertyAsString("return").equals("OK"))
            {
        		// При успешной отправке установим признак
			    for (UUID uuid:uuids)
			    {
			    	// Состояние переключим в "Отправлен", было если "Создан" или "Согласование"
			    	ContentValues cv=new ContentValues();
			    	cv.put("state", E_ORDER_STATE.E_ORDER_STATE_SENT.value());
			    	context.getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "uid=? and (state=? or state=?)", new String[]{uuid.toString(), String.valueOf(E_ORDER_STATE.E_ORDER_STATE_CREATED.value()), String.valueOf(E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value())});
			    	// установим version_ack=version
			    	Cursor versionCursor=context.getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"version", "versionPDA"}, "uid=?", new String[]{uuid.toString()}, null);
			    	if (versionCursor.moveToFirst())
			    	{
			    		cv.clear();
			    		cv.put("version_ack", versionCursor.getString(0));
			    		cv.put("versionPDA", versionCursor.getString(1)); // если этого поля не будет, счетчик увеличится на единцу
			    		
			    		context.getContentResolver().update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "uid=?", new String[]{uuid.toString()});
			    	}
			    	versionCursor.close();
			    }
            }
	    }

	    if (uid_to_print_with_prefix==null)
	    {
	    	// Контрагенты
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_CLIENTS);
	    	//request.addProperty(propInfoAgent);
	    	request.addSoapObject(agent);
	    	envelope.setOutputSoapObject(request);
			
    		androidHttpTransport.call(SOAP_ACTION_GET_CLIENTS, envelope);
    		resultsRequestSOAP = (SoapObject)envelope.bodyIn;

			ContentValues cv=new ContentValues();
		 	cv.put("isUsed", 0);
		 	context.getContentResolver().update(MTradeContentProvider.CLIENTS_CONTENT_URI, cv, null, null);
		 	context.getContentResolver().update(MTradeContentProvider.SALDO_CONTENT_URI, cv, null, null);
		 	
			ArrayList<SoapObject> clients=new ArrayList<SoapObject>();
			for(int i = 0; i < ((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyCount(); i++)
			{
				SoapObject obj=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
				if (clients.size()==300)
				{
					loadClients(context, clients);
					clients.clear();
				}
				clients.add(obj);
			}
			if (clients.size()>0)
			{
				loadClients(context, clients);
			}
			clients=null;
			
			context.getContentResolver().delete(MTradeContentProvider.CLIENTS_CONTENT_URI, "isUsed<>?", new String[]{"1"});
			context.getContentResolver().delete(MTradeContentProvider.SALDO_CONTENT_URI, "isUsed<>?", new String[]{"1"});
	    	
    		// Номенклатура
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_NOMENCLATURE);
	    	//request.addProperty(propInfoAgent);
	    	request.addSoapObject(agent);
	    	envelope.setOutputSoapObject(request);
			
    		androidHttpTransport.call(SOAP_ACTION_GET_NOMENCLATURE, envelope);
    		resultsRequestSOAP = (SoapObject)envelope.bodyIn;

			cv.clear();
		 	cv.put("isUsed", 0);
		 	context.getContentResolver().update(MTradeContentProvider.PRICETYPES_CONTENT_URI, cv, null, null);
		 	cv.clear();
			cv.put("id", dummyId);
			cv.put("code", "000001");
			cv.put("descr", "Базовая цена");
			cv.put("isFolder", 2); // не используется
			cv.put("isUsed", 1);
			context.getContentResolver().insert(MTradeContentProvider.PRICETYPES_CONTENT_URI, cv);
			context.getContentResolver().delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, "isUsed<>?", new String[]{"1"});
		 	
		 	cv.clear();
		 	cv.put("isUsed", 0);
		 	context.getContentResolver().update(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv, null, null);
		 	context.getContentResolver().update(MTradeContentProvider.PRICES_CONTENT_URI, cv, null, null);
		 	context.getContentResolver().update(MTradeContentProvider.RESTS_CONTENT_URI, cv, null, null);
		 	
			//lblResult.setText(resultsRequestSOAP.getPropertyAsString("result")); //"result" is the array, which is reported by WebService
			//Log.v(LOG_TAG, String.valueOf(resultsRequestSOAP.getPropertyCount()));
		 	
		 	final int BULK_SIZE=300;
		 	
		    ContentValues []nomenclature_values= new ContentValues[BULK_SIZE];
		    ContentValues []prices_values= new ContentValues[BULK_SIZE];
		    ContentValues []rests_values= new ContentValues[BULK_SIZE];
		    
		    int bulk_idx_nomenclature=0;
		    int bulk_idx_prices=0;
		    int bulk_idx_rests=0;
		    
			for(int i = 0; i < ((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyCount(); i++)
			{
				SoapObject obj=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
				
				ContentValues cv_nomenclature=new ContentValues();
				
				cv_nomenclature.put("id", myGetPropertyAsString(obj, "GUID"));
				cv_nomenclature.put("code", myGetPropertyAsString(obj, "Code"));
				cv_nomenclature.put("descr", myGetPropertyAsString(obj, "Name"));
				cv_nomenclature.put("descr_lower", myGetPropertyAsString(obj, "Name").toLowerCase());
				MyID parentId=new MyID(myGetPropertyAsString(obj, "parent_id"));
				if (parentId.isEmpty())
					parentId=new MyID();
				cv_nomenclature.put("parent_id", parentId.toString());
				int isFolder=Integer.parseInt(myGetPropertyAsString(obj, "isFolder"));
				cv_nomenclature.put("isFolder", isFolder);
				cv_nomenclature.put("descrFull", isFolder==1?"":myGetPropertyAsString(obj, "fullName"));
				cv_nomenclature.put("quant_1", "шт");
				cv_nomenclature.put("quant_2", "шт");
				cv_nomenclature.put("edizm_1_id", dummyId);
				cv_nomenclature.put("edizm_2_id", dummyId);
				cv_nomenclature.put("quant_k_1", 1.0);
				cv_nomenclature.put("quant_k_2", 1.0);
				cv_nomenclature.put("opt_price", 0.0);
				cv_nomenclature.put("m_opt_price", 0.0);
				cv_nomenclature.put("rozn_price", 0.0);
				cv_nomenclature.put("incom_price", 0.0);
				cv_nomenclature.put("IsInPrice", 1);
				cv_nomenclature.put("flagWithoutDiscont", 0);
				cv_nomenclature.put("weight_k_1", 0.0);
				cv_nomenclature.put("weight_k_2", 0.0);
				cv_nomenclature.put("min_quantity", 0.0);
				cv_nomenclature.put("multiplicity", 0.0);
				cv_nomenclature.put("required_sales", 0.0);
				cv_nomenclature.put("flags", 0);
				cv_nomenclature.put("image_file", "");
				cv_nomenclature.put("image_file_checksum", 0);
				cv_nomenclature.put("image_width", 0);
				cv_nomenclature.put("image_height", 0);
				cv_nomenclature.put("image_file_size", 0);
				cv_nomenclature.put("order_for_sorting", Integer.parseInt(myGetPropertyAsString(obj, "order_for_sorting")));
				cv_nomenclature.put("compose_with", "");
				cv_nomenclature.put("isUsed", 1);
    		 	//context.getContentResolver().insert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv);
				
	        	nomenclature_values[bulk_idx_nomenclature]=cv_nomenclature;
	        	bulk_idx_nomenclature++;
	        	if (bulk_idx_nomenclature>=BULK_SIZE)
	        	{
	        		context.getContentResolver().bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, nomenclature_values);
	        		bulk_idx_nomenclature=0;
	        	}
    		 	
    		 	if (isFolder!=1)
    		 	{
    		 		
    				ContentValues cv_prices=new ContentValues();
    				cv_prices.put("nomenclature_id", myGetPropertyAsString(obj, "GUID"));
    				cv_prices.put("ed_izm_id", dummyId);
    				cv_prices.put("price_type_id", dummyId);
    				cv_prices.put("edIzm", "шт.");
    				cv_prices.put("price", Double.parseDouble(myGetPropertyAsString(obj, "Price")));
    				cv_prices.put("priceProcent", 0.0);
    				cv_prices.put("k", 1.0);
    				cv_prices.put("isUsed", 1);
    				//context.getContentResolver().insert(MTradeContentProvider.PRICES_CONTENT_URI, cv);
    				prices_values[bulk_idx_prices]=cv_prices;
    				bulk_idx_prices++;
    	        	if (bulk_idx_prices>=BULK_SIZE)
    	        	{
    	        		context.getContentResolver().bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, prices_values);
    	        		bulk_idx_prices=0;
    	        	}
    				
    				ContentValues cv_rests=new ContentValues();
    				cv_rests.put("stock_id", dummyId); // или null
    				cv_rests.put("nomenclature_id", myGetPropertyAsString(obj, "GUID"));
    				cv_rests.put("quantity", Double.parseDouble(myGetPropertyAsString(obj, "rest")));
    				cv_rests.put("quantity_reserve", 0.0);
    				cv_rests.put("isUsed", 1);
    				//context.getContentResolver().insert(MTradeContentProvider.RESTS_CONTENT_URI, cv);
    				rests_values[bulk_idx_rests]=cv_rests;
    				bulk_idx_rests++;
    	        	if (bulk_idx_rests>=BULK_SIZE)
    	        	{
    	        		context.getContentResolver().bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, rests_values);
    	        		bulk_idx_rests=0;
    	        	}
    				
    		 	}
			}
			
		    if (bulk_idx_nomenclature>0)
		    {
		    	ContentValues []values_2 = new ContentValues[bulk_idx_nomenclature];
		    	int i;
		    	for (i=0; i<bulk_idx_nomenclature; i++)
		    	{
		    		values_2[i]=nomenclature_values[i];
		    	}
		    	context.getContentResolver().bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values_2);
		    }
		    if (bulk_idx_prices>0)
		    {
		    	ContentValues []values_2 = new ContentValues[bulk_idx_prices];
		    	int i;
		    	for (i=0; i<bulk_idx_prices; i++)
		    	{
		    		values_2[i]=prices_values[i];
		    	}
		    	context.getContentResolver().bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values_2);
		    }
		    if (bulk_idx_rests>0)
		    {
		    	ContentValues []values_2 = new ContentValues[bulk_idx_rests];
		    	int i;
		    	for (i=0; i<bulk_idx_rests; i++)
		    	{
		    		values_2[i]=rests_values[i];
		    	}
		    	context.getContentResolver().bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values_2);
		    }
		    
			
			
			context.getContentResolver().delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, "isUsed<>?", new String[]{"1"});
			context.getContentResolver().delete(MTradeContentProvider.PRICES_CONTENT_URI, "isUsed<>?", new String[]{"1"});
			context.getContentResolver().delete(MTradeContentProvider.RESTS_CONTENT_URI, "isUsed<>?", new String[]{"1"});
		 	// обновим иерархию номенклатуры
		 	TextDatabase.fillNomenclatureHierarchy(context.getContentResolver(), context.getResources(), "     0   ");
		 	
    		// Скидки
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_DISCOUNTS);
	    	//request.addProperty(propInfoAgent);
	    	request.addSoapObject(agent);
	    	envelope.setOutputSoapObject(request);
		 	
    		androidHttpTransport.call(SOAP_ACTION_GET_DISCOUNTS, envelope);
		 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
		 	cv.clear();
		 	cv.put("isUsed", 0);
		 	context.getContentResolver().update(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv, null, null);
			//lblResult.setText(resultsRequestSOAP.getPropertyAsString("result")); //"result" is the array, which is reported by WebService
			//Log.v(LOG_TAG, String.valueOf(resultsRequestSOAP.getPropertyCount()));
			for(int i = 0; i < ((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyCount(); i++)
			{
				SoapObject obj=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
				
				cv.clear();
				cv.put("id", myGetPropertyAsString(obj, "GUID"));
				cv.put("code", myGetPropertyAsString(obj, "Code"));
				cv.put("descr", myGetPropertyAsString(obj, "Name"));
				cv.put("priceProcent", Double.parseDouble(myGetPropertyAsString(obj, "procent")));
				cv.put("isUsed", 1);
				context.getContentResolver().insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
				
				//ExchangeDiscount ed=new ExchangeDiscount(); 
				//ed=(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
				//Log.v(LOG_TAG, ((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i).toString());
				//Log.v(LOG_TAG, ed.toString());
				//Log.v(LOG_TAG, String.valueOf(ed.procent));
			}
			context.getContentResolver().delete(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, "isUsed<>?", new String[]{"1"});
		 	
			
    		// Настройки
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_SETTINGS);
	    	request.addSoapObject(agent);
	    	envelope.setOutputSoapObject(request);
		 	
    		androidHttpTransport.call(SOAP_ACTION_GET_SETTINGS, envelope);
		 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
		 	
		 	//double settings_ticket_m=Double.parseDouble(((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyAsString("ticket_m"));
		 	//double settings_ticket_w=Double.parseDouble(((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyAsString("ticket_w"));

		 	//cv.clear();
		 	//cv.put("ticket_m", settings_ticket_m);
		 	//cv.put("ticket_w", settings_ticket_w);
		 	//context.getContentResolver().update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, null, null);
			
			//Log.v(LOG_TAG, androidHttpTransport.responseDump);
			/*
			GetListResponse result = new GetListResponse();
			String s=envelope.bodyIn.toString();
		    Log.v(LOG_TAG, "IN="+envelope.bodyIn.toString());
		    result = (GetListResponse)envelope.bodyIn;
		    */
		 	
		 	// Столы
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_PLACES);
	    	request.addSoapObject(agent);
	    	envelope.setOutputSoapObject(request);
		 	
    		androidHttpTransport.call(SOAP_ACTION_GET_PLACES, envelope);
		 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
		 	
		 	cv.clear();
		 	cv.put("isUsed", 0);
		 	context.getContentResolver().update(MTradeContentProvider.PLACES_CONTENT_URI, cv, null, null);

		 	int places_count=((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyCount(); 
		    ContentValues []cv_places= new ContentValues[places_count];
		 	
			for(int i = 0; i < places_count; i++)
			{
			 	ContentValues cv_place=new ContentValues();
				
				SoapObject obj=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
				
				cv_place.put("id", myGetPropertyAsString(obj, "GUID"));
				cv_place.put("descr", myGetPropertyAsString(obj, "Name"));
				cv_place.put("isUsed", 1);
    		 	//context.getContentResolver().insert(MTradeContentProvider.PLACES_CONTENT_URI, cv);
				cv_places[i]=cv_place;
			}
			context.getContentResolver().bulkInsert(MTradeContentProvider.PLACES_CONTENT_URI, cv_places);
			
			context.getContentResolver().delete(MTradeContentProvider.PLACES_CONTENT_URI, "isUsed<>?", new String[]{"1"});
		 	
	    } // id_to_print==null
	    
	    if (query_orders_type==0||query_orders_type==1)
	    {
	    	request = new SoapObject(NAMESPACE, METHOD_NAME_QUERY_ORDERS);
	    	request.addSoapObject(agent);
	    	request.addProperty("DateType", query_orders_type);
	    	request.addProperty("DateBegin", query_orders_date_begin);
	    	request.addProperty("DateEnd", query_orders_date_end);
	    	envelope.setOutputSoapObject(request);
		 	
			androidHttpTransport.call(SOAP_ACTION_QUERY_ORDERS, envelope);
		 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
		 	
		 	if (resultsRequestSOAP.getPropertyAsString("return").equals("OK"))
		 	{
		 		// запрос выполнен успешно
		 	}
		 	
	    }
		
		// Заказы
    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_ORDERS);
    	//request.addProperty(propInfoAgent);
    	request.addSoapObject(agent);
    	envelope.setOutputSoapObject(request);
	 	
		androidHttpTransport.call(SOAP_ACTION_GET_ORDERS, envelope);
	 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
		//lblResult.setText(resultsRequestSOAP.getPropertyAsString("result")); //"result" is the array, which is reported by WebService
		//Log.v(LOG_TAG, String.valueOf(resultsRequestSOAP.getPropertyCount()));
		for(int i = 0; i < ((SoapObject)resultsRequestSOAP.getProperty(0)).getPropertyCount(); i++)
		{
			SoapObject obj=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)).getProperty(i));
			
			boolean bRecordValid=false;
			boolean bDemandLoadToClient=false;
			
			OrderRecord or=new OrderRecord();
			or.uid=UUID.fromString(myGetPropertyAsString(obj, "uuidPDA"));
			
    	    cursor=context.getContentResolver().query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
	    	if (cursor.moveToNext())
	    	{
	    		int _id = cursor.getInt(0);
	    		if (TextDatabase.ReadOrderBy_Id(context.getContentResolver(), or, _id))
	    		{
	    			bRecordValid=true;
	    			// обнулим текущее количество в документе
	    			// TODO раскомментировать это и добавить загрузку строк из 1С
	    			//for (OrderLineRecord it:or.lines)
	    			//{
	    			//	it.temp_quantity=it.quantity;
	    			//	it.quantity=0.0;
	    			//}
	    		}
	    	}
	    	cursor.close();
	    	
            or.id=new MyID(myGetPropertyAsString(obj, "GUID"));
            or.numdoc=myGetPropertyAsString(obj, "Number");
            //Date date=IsoDate.stringToDate(myGetPropertyAsString(obj, "Date"), IsoDate.DATE_TIME);
            //or.datedoc=android.text.format.DateFormat.format("yyyyMMddkkmmss", date).toString();
            or.datedoc=myGetPropertyAsString(obj, "Date");
			if (myGetPropertyAsString(obj, "ShippingDate").length()<12)
			{
				or.shipping_date=or.datedoc.substring(0, 8);
				or.shipping_begin_time="0000";
			} else
			{
				or.shipping_date=myGetPropertyAsString(obj, "ShippingDate").substring(0, 8);
				or.shipping_begin_time=myGetPropertyAsString(obj, "ShippingDate").substring(8, 12);
			}
            or.version=Integer.parseInt(myGetPropertyAsString(obj, "Version"));
            or.versionPDA=Integer.parseInt(myGetPropertyAsString(obj, "VersionPDA"));
            or.version_ack=Integer.parseInt(myGetPropertyAsString(obj, "Version_ack"));
            or.versionPDA_ack=Integer.parseInt(myGetPropertyAsString(obj, "VersionPDA_ack"));
        	or.closed_not_full=0; // такие данные не передаются
            String state_descr=myGetPropertyAsString(obj, "state_descr");
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_LOADED, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_LOADED;
            else
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED;
            else
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_COMPLETED, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_COMPLETED;
            else
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL;
            else
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_CANCELED, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_CANCELED;
            else
            if (state_descr.equals(MyDatabase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT, null)))
            	or.state=E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT;
            else
            	or.state=E_ORDER_STATE.E_ORDER_STATE_UNKNOWN;
            or.create_client=0;
            SoapObject client=(SoapObject)obj.getProperty("Client");
            ArrayList<SoapObject> clients=new ArrayList<SoapObject>();
            clients.add(client);
            loadClients(context, clients);
            or.client_id=new MyID(myGetPropertyAsString(client, ("GUID")));
            or.comment_closing="";
            or.comment_payment="";
            or.agreement_id=new MyID();
            or.distr_point_id=new MyID();
            or.manager_comment=myGetPropertyAsString(obj, "Manager");
            or.theme_comment=myGetPropertyAsString(obj, "Theme");
            or.phone_num=myGetPropertyAsString(obj, "phone");
            
            bDemandLoadToClient=(Integer.parseInt(myGetPropertyAsString(obj, "demandLoadToClient"))!=0);
            
            if (bDemandLoadToClient)
            {
            	bRecordValid=true;
            	or.price_type_id=new MyID(dummyId);
            }
            
        	if (bRecordValid)
        	{
                // загрузим столы
        		SoapObject places=(SoapObject)obj.getProperty("Places");
        		or.places.clear();
    			for(int j = 0; j < places.getPropertyCount(); j++)
    			{
    				SoapObject place=(SoapObject)(places.getProperty(j));
    				OrderPlaceRecord placeRec=new OrderPlaceRecord();
    				placeRec.place_id=new MyID(myGetPropertyAsString(place, ("place_id")));
    				or.places.add(placeRec);
    			}
    			// и товары
    			SoapObject products=(SoapObject)obj.getProperty("Products");
    			or.lines.clear();
    			for(int j = 0; j < products.getPropertyCount(); j++)
    			{
    				SoapObject product=(SoapObject)(products.getProperty(j));
    				OrderLineRecord lineRec=new OrderLineRecord();
    				lineRec.nomenclature_id=new MyID(myGetPropertyAsString((SoapObject)product.getProperty("Nomenclature"), ("GUID")));
    				lineRec.quantity=Double.parseDouble(myGetPropertyAsString(product, "Quantity"));
    				lineRec.quantity_requested=lineRec.quantity;
    				lineRec.k=1.0;
    				lineRec.price=Double.parseDouble(myGetPropertyAsString(product, "Price"));
    				lineRec.total=Double.parseDouble(myGetPropertyAsString(product, "Sum"));
    				lineRec.shipping_time=myGetPropertyAsString(product, "shipping_time");
    				lineRec.comment_in_line=myGetPropertyAsString(product, "comment");
    				or.lines.add(lineRec);
    			}
        		
                int _id=0;
    			or.sumDoc=or.GetOrderSum(null, false);
    			or.weightDoc=TextDatabase.GetOrderWeight(context.getContentResolver(), or, null, false);
    			or.dont_need_send=0; // этот флаг в случае, если документ на сервере, не может быть установлен
                TextDatabase.SaveOrderSQL(context.getContentResolver(), or, _id, 0);
                // TODO
                //uids.add(or.uid);
        	} else
        	{
        		// такой заявки уже нет, сообщаем об этом
        		myDatabase.m_empty_orders.put(or.uid, or.version);
        	}
		}
		
	 	// Занятые столы обновляем всегда
    	request = new SoapObject(NAMESPACE, METHOD_NAME_GET_OCCUPIED_PLACES);
    	request.addSoapObject(agent);
    	request.addProperty("Ver", myDatabase.m_occupied_places_version);
    	
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(context);
		java.util.Date start_date_for_occupied_places=DatePreference.getDateFor(pref, "start_date_for_occupied_places").getTime();
		String startDate=Common.MyDateFormat("yyyyMMdd", start_date_for_occupied_places);
    	request.addProperty("start_date", startDate);
    	envelope.setOutputSoapObject(request);
	 	
		androidHttpTransport.call(SOAP_ACTION_GET_OCCUPIED_PLACES, envelope);
	 	resultsRequestSOAP = (SoapObject)envelope.bodyIn;
	 	
	 	//cv.clear();
	 	//cv.put("isUsed", 0);
	 	//context.getContentResolver().update(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, cv, null, null);
	 	
	 	SoapObject result=(SoapObject)(((SoapObject)resultsRequestSOAP.getProperty(0)));
	 	
	 	String occupiedPlacesVer=result.getPropertyAsString("Ver");
	 	
	 	// Переданы номера столов, по которым данные переданы полностью (новый вариант, против сбоев)
	 	// очистим столы, занятые перечисленными документами
	 	if (result.hasProperty("OrdersIds"))
	 	{
		 	final int BULK_SIZE=300;
		    int bulk_delete_idx=0;
		    ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		 	SoapObject ordersIds=(SoapObject)((SoapObject)result.getProperty("OrdersIds"));
			for(int i = 0; i < ordersIds.getPropertyCount(); i++)
			{
				String id=ordersIds.getProperty(i).toString();
				//cv_occupied_places.put("place_id", myGetPropertyAsString(obj, "place_id"));
				//context.getContentResolver().
					
				//context.getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, "place_id=? and client_id=? and document_id=?", new String[]{myGetPropertyAsString(obj, "place_id"), myGetPropertyAsString(obj, "client_id"), myGetPropertyAsString(obj, "document_id")});
	    	    ContentProviderOperation operation = ContentProviderOperation
	    	    		.newDelete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI)
	    	    		.withSelection("document_id=?", new String[]{id})
	    	    		.build();
	    	    operations.add(operation);
	    	    bulk_delete_idx++;
            	if (bulk_delete_idx>=BULK_SIZE)
            	{
            		context.getContentResolver().applyBatch(MTradeContentProvider.AUTHORITY, operations);
            		bulk_delete_idx=0;
            	}
				
			}
	    	if (bulk_delete_idx>0)
	    	{
	    		context.getContentResolver().applyBatch(MTradeContentProvider.AUTHORITY, operations);
	    	}
	 	}
	 	
	 	if (result.hasProperty("PlacesOccupied"))
	 	{
	 		// загружаем все, поэтому предварительно очищаем
	 		if (myDatabase.m_occupied_places_version<0)
	 		{
    		 	context.getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, "", null);
	 		}
	 		
		 	SoapObject placesOccupied=(SoapObject)((SoapObject)result.getProperty("PlacesOccupied"));
		 	
		 	final int BULK_SIZE=300;
		    ContentValues []occupied_places_values= new ContentValues[BULK_SIZE];
		    
		    int bulk_idx_occupied_places=0;
		    
			for(int i = 0; i < placesOccupied.getPropertyCount(); i++)
			{
				SoapObject obj=(SoapObject)(placesOccupied.getProperty(i));
				
				ContentValues cv_occupied_places=new ContentValues();
				cv_occupied_places.put("place_id", myGetPropertyAsString(obj, "place_id"));
				cv_occupied_places.put("client_id", myGetPropertyAsString(obj, "client_id"));
				cv_occupied_places.put("document_id", myGetPropertyAsString(obj, "document_id"));
				cv_occupied_places.put("datedoc", myGetPropertyAsString(obj, "datedoc"));
				cv_occupied_places.put("document", myGetPropertyAsString(obj, "document"));
				cv_occupied_places.put("shipping_date", myGetPropertyAsString(obj, "shipping_date"));
				cv_occupied_places.put("shipping_time", myGetPropertyAsString(obj, "shipping_time"));
				cv_occupied_places.put("isUsed", 1);
    		 	//context.getContentResolver().insert(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, cv);
				
				occupied_places_values[bulk_idx_occupied_places]=cv_occupied_places;
	        	bulk_idx_occupied_places++;
	        	if (bulk_idx_occupied_places>=BULK_SIZE)
	        	{
	        		context.getContentResolver().bulkInsert(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, occupied_places_values);
	        		bulk_idx_occupied_places=0;
	        	}
			}
			
		    if (bulk_idx_occupied_places>0)
		    {
		    	ContentValues []values_2 = new ContentValues[bulk_idx_occupied_places];
		    	int i;
		    	for (i=0; i<bulk_idx_occupied_places; i++)
		    	{
		    		values_2[i]=occupied_places_values[i];
		    	}
		    	context.getContentResolver().bulkInsert(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, values_2);
		    }
			
			myDatabase.m_occupied_places_version=Integer.valueOf(occupiedPlacesVer);
	 	}
	 	
	 	if (result.hasProperty("PlacesFree"))
	 	{
		 	final int BULK_SIZE=300;
	 		
		    int bulk_delete_idx=0;
		    
		    ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
	 		
		 	SoapObject placesFree=(SoapObject)((SoapObject)result.getProperty("PlacesFree"));
			for(int i = 0; i < placesFree.getPropertyCount(); i++)
			{
				SoapObject obj=(SoapObject)(placesFree.getProperty(i));
				
				//context.getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, "place_id=? and client_id=? and document_id=?", new String[]{myGetPropertyAsString(obj, "place_id"), myGetPropertyAsString(obj, "client_id"), myGetPropertyAsString(obj, "document_id")});
	    	    ContentProviderOperation operation = ContentProviderOperation
	    	    		.newDelete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI)
	    	    		.withSelection("place_id=? and client_id=? and document_id=?", new String[]{myGetPropertyAsString(obj, "place_id"), myGetPropertyAsString(obj, "client_id"), myGetPropertyAsString(obj, "document_id")})
	    	    		.build();
	    	    operations.add(operation);
	    	    bulk_delete_idx++;
            	if (bulk_delete_idx>=BULK_SIZE)
            	{
            		context.getContentResolver().applyBatch(MTradeContentProvider.AUTHORITY, operations);
            		bulk_delete_idx=0;
            	}
				
			}
			
	    	if (bulk_delete_idx>0)
	    	{
	    		context.getContentResolver().applyBatch(MTradeContentProvider.AUTHORITY, operations);
	    	}
			
			myDatabase.m_occupied_places_version=Integer.valueOf(occupiedPlacesVer);
	 	}
		
		//context.getContentResolver().delete(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, "isUsed<>?", new String[]{"1"});
	 	
	 }
}
