package ru.code22.mtrade;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import ru.code22.mtrade.MyDatabase.AgentRecord;
import ru.code22.mtrade.MyDatabase.AgreementRecord;
import ru.code22.mtrade.MyDatabase.CashPaymentRecord;
import ru.code22.mtrade.MyDatabase.ClientPriceRecord;
import ru.code22.mtrade.MyDatabase.ClientRecord;
import ru.code22.mtrade.MyDatabase.CuratorPriceRecord;
import ru.code22.mtrade.MyDatabase.CuratorRecord;
import ru.code22.mtrade.MyDatabase.DistrPointRecord;
import ru.code22.mtrade.MyDatabase.DistribsContractsRecord;
import ru.code22.mtrade.MyDatabase.DistribsLineRecord;
import ru.code22.mtrade.MyDatabase.DistribsRecord;
import ru.code22.mtrade.MyDatabase.EquipmentRecord;
import ru.code22.mtrade.MyDatabase.EquipmentRestsRecord;
import ru.code22.mtrade.MyDatabase.MessageRecord;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.NomenclatureRecord;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.OrderPlaceRecord;
import ru.code22.mtrade.MyDatabase.OrderRecord;
import ru.code22.mtrade.MyDatabase.OrganizationRecord;
import ru.code22.mtrade.MyDatabase.PriceRecord;
import ru.code22.mtrade.MyDatabase.PriceAgreement30Record;
import ru.code22.mtrade.MyDatabase.PriceTypeRecord;
import ru.code22.mtrade.MyDatabase.QuantityRecord;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.MyDatabase.RefundRecord;
import ru.code22.mtrade.MyDatabase.SaldoRecord;
import ru.code22.mtrade.MyDatabase.SaldoExtRecord;
import ru.code22.mtrade.MyDatabase.SalesHistoryRecord;
import ru.code22.mtrade.MyDatabase.SimpleDiscountTypeRecord;
import ru.code22.mtrade.MyDatabase.StockRecord;
import ru.code22.mtrade.MyDatabase.RouteRecord;
import ru.code22.mtrade.MyDatabase.RouteLineRecord;
import ru.code22.mtrade.MyDatabase.RouteDateRecord;
import ru.code22.mtrade.MyDatabase.VicariousPowerRecord;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.util.Xml;

public class TextDatabase {

	static public E_BW E_BW_fromInt(int i) {
		switch (i) {
			case 0:
				return E_BW.E_BW_UPR;
			case 1:
				return E_BW.E_BW_BOTH;
			case 2:
				return E_BW.E_BW_TARA;
			case 3:
				return E_BW.E_BW_BPZ;
		}
		return E_BW.E_BW_UPR;
	}

	static public int E_BW_toInt(E_BW bw) {
		if (bw == E_BW.E_BW_UPR)
			return 0;
		if (bw == E_BW.E_BW_BOTH)
			return 1;
		if (bw == E_BW.E_BW_TARA)
			return 2;
		if (bw == E_BW.E_BW_BPZ)
			return 3;
		return 0;
	}

	static public E_TRADE_TYPE E_TRADE_TYPE_fromInt(int i) {
		switch (i) {
			case 0:
				return E_TRADE_TYPE.E_TRADE_TYPE_NAL;
			case 2:
				return E_TRADE_TYPE.E_TRADE_TYPE_BEZNAL;
			case 3:
				return E_TRADE_TYPE.E_TRADE_TYPE_CONSIGNATION;
			case 7:
				return E_TRADE_TYPE.E_TRADE_TYPE_ROZN;
		}
		return E_TRADE_TYPE.E_TRADE_TYPE_NAL;
	}

	static public int E_TRADE_TYPE_toInt(E_TRADE_TYPE tt) {
		if (tt == E_TRADE_TYPE.E_TRADE_TYPE_NAL)
			return 0;
		if (tt == E_TRADE_TYPE.E_TRADE_TYPE_BEZNAL)
			return 2;
		if (tt == E_TRADE_TYPE.E_TRADE_TYPE_CONSIGNATION)
			return 3;
		if (tt == E_TRADE_TYPE.E_TRADE_TYPE_ROZN)
			return 7;
		return 0;
	}

	static public double GetOrderWeight(ContentResolver contentResolver, OrderRecord rec, OrderLineRecord line, boolean excludeLine) {
		// сумма
		double weight = 0;
		for (OrderLineRecord it : rec.lines) {
			if (line != null)
				if ((line == it && excludeLine) || (line != it && !excludeLine))
					continue;
            /*
    	    Cursor cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"weight_k_1"}, "id=?", new String[]{it.nomenclature_id.toString()}, null);
    	    if (cursor!=null)
    	    {
    	    	int weight_k_1_index=cursor.getColumnIndex("weight_k_1");
    	    	if (cursor.moveToNext())
    	    	{
    	    		weight+=Math.floor(it.k*it.quantity*cursor.getDouble(weight_k_1_index)*100+0.000001);
    	    	}
    	    	cursor.close();
    	    }
    	    */
			weight += Math.floor(it.k * it.quantity * it.stuff_weight_k_1 * 1000 + 0.000001);
		}
		return weight / 1000.0;
	}

	static public double GetRefundWeight(ContentResolver contentResolver, RefundRecord rec, RefundLineRecord line, boolean excludeLine) {
		double weight = 0;
		for (RefundLineRecord it : rec.lines) {
			if (line != null)
				if ((line == it && excludeLine) || (line != it && !excludeLine))
					continue;
			weight += Math.floor(it.k * it.quantity * it.stuff_weight_k_1 * 1000 + 0.000001);
		}
		return weight / 1000.0;
	}

	//возвращает склад по умолчанию
	// (на самом деле здесь и ниже descr меняется, не используется и не возвращается из функции)
	static public MyID getDefaultStock(ContentResolver contentResolver, String descr) {
		// По умолчанию и не акционный
		Cursor cursor = contentResolver.query(MTradeContentProvider.STOCKS_CONTENT_URI, new String[]{"id", "descr"}, "(flags&3)=1", null, "descr");
		if (cursor.moveToNext()) {
			int idIndex = cursor.getColumnIndex("id");
			int descrIndex = cursor.getColumnIndex("descr");
			String id = cursor.getString(idIndex);
			descr = cursor.getString(descrIndex);
			cursor.close();
			return new MyID(id);
		}
		cursor.close();
		descr = "";
		return new MyID();
	}

	// возвращает договор, если он один
	static public MyID getDefaultAgreement(ContentResolver contentResolver, MyID client_id, String descr) {
		Cursor cursor = contentResolver.query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"id", "descr"}, "owner_id=?", new String[]{client_id.toString()}, null);
		if (cursor.moveToNext()) {
			int idIndex = cursor.getColumnIndex("id");
			int descrIndex = cursor.getColumnIndex("descr");
			String id = cursor.getString(idIndex);
			descr = cursor.getString(descrIndex);
			// второго нет
			if (!cursor.moveToNext()) {
				cursor.close();
				return new MyID(id);
			}
		}
		cursor.close();
		descr = "";
		return new MyID();
	}

	// возвращает торговую точку, если она одна
	static public MyID getDefaultTradePoint(ContentResolver contentResolver, MyID client_id, String descr) {
		// По умолчанию и не акционный
		Cursor cursor = contentResolver.query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"id", "descr"}, "owner_id=?", new String[]{client_id.toString()}, null);
		if (cursor.moveToNext()) {
			int idIndex = cursor.getColumnIndex("id");
			int descrIndex = cursor.getColumnIndex("descr");
			String id = cursor.getString(idIndex);
			descr = cursor.getString(descrIndex);
			// второй нет
			if (!cursor.moveToNext()) {
				cursor.close();
				return new MyID(id);
			}
		}
		cursor.close();
		descr = "";
		return new MyID();
	}

	// возвращает тип цены агента по умолчанию (если договор и контрагента не выбрали, чтобы даже в этом случае в заказе были цены)
	static public MyID getDefaultAgentPriceType(ContentResolver contentResolver) {
		Cursor cursor = contentResolver.query(MTradeContentProvider.SETTINGS_CONTENT_URI, new String[]{"agent_price_type_id"},"", null, null);
		if (cursor.moveToNext()) {
			String agent_price_type_id=cursor.getString(0);
			cursor.close();
			return new MyID(agent_price_type_id);
		}
		cursor.close();
		return new MyID();
	}


	// Этой функцией лучше не пользоваться (вместо нее - см. URI_DISCOUNTS_STUFF)
	static ClientPriceRecord GetClientPrice(ContentResolver contentResolver, MyID client_id, MyID nomenclature_id) {
		// Сначала считываем содержимое по данному клиенту
		Map<String, ClientPriceRecord> mapClientPrice = new HashMap<String, ClientPriceRecord>();
		Cursor cursor = contentResolver.query(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, new String[]{"nomenclature_id", "priceAdd", "priceProcent"}, "client_id=?", new String[]{client_id.toString()}, null);
		int numenclatureIdIndex = cursor.getColumnIndex("nomenclature_id");
		int priceAddIndex = cursor.getColumnIndex("priceAdd");
		int priceProcentIndex = cursor.getColumnIndex("priceProcent");
		while (cursor.moveToNext()) {
			ClientPriceRecord rec = new ClientPriceRecord();
			rec.client_id = client_id;
			rec.nomenclature_id = new MyID(cursor.getString(numenclatureIdIndex));
			rec.priceAdd = cursor.getDouble(priceAddIndex);
			rec.priceProcent = cursor.getDouble(priceProcentIndex);
			mapClientPrice.put(rec.nomenclature_id.toString(), rec);
		}
		if (mapClientPrice.size() == 0) {
			// Наценка по этому клиенту не применяется - выходим
			cursor.close();
			return null;
		}
		ArrayList<String> list = new ArrayList<String>();
		String currentNomenclatureId = nomenclature_id.toString();
		// Затем для данной номенклатуры пройдем в цикле но верхнего уровня
		do {
			// Номенклатура найдена
			if (mapClientPrice.containsKey(currentNomenclatureId)) {
				return mapClientPrice.get(currentNomenclatureId);
			}
			list.add(currentNomenclatureId);
			//
			Cursor cursorNomenclature = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"parent_id"}, "id=?", new String[]{currentNomenclatureId}, null);
			if (cursorNomenclature.moveToFirst()) {
				currentNomenclatureId = cursorNomenclature.getString(0);
			}
			cursorNomenclature.close();
		} while (!list.contains(currentNomenclatureId));    // защита от зацикливания
		cursor.close();
		return null;
	}

	// Аналогично GetClientPrice - работает медленнее запроса
	static CuratorPriceRecord GetCuratorPrice(ContentResolver contentResolver, MyID curator_id, MyID nomenclature_id) {
		// Сначала считываем содержимое по данному куратору
		Map<String, CuratorPriceRecord> mapCuratorPrice = new HashMap<String, CuratorPriceRecord>();
		Cursor cursor = contentResolver.query(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, new String[]{"nomenclature_id", "priceAdd", "priceProcent"}, "curator_id=?", new String[]{curator_id.toString()}, null);
		int numenclatureIdIndex = cursor.getColumnIndex("nomenclature_id");
		int priceAddIndex = cursor.getColumnIndex("priceAdd");
		int priceProcentIndex = cursor.getColumnIndex("priceProcent");
		while (cursor.moveToNext()) {
			CuratorPriceRecord rec = new CuratorPriceRecord();
			rec.curator_id = curator_id;
			rec.nomenclature_id = new MyID(cursor.getString(numenclatureIdIndex));
			rec.priceAdd = cursor.getDouble(priceAddIndex);
			rec.priceProcent = cursor.getDouble(priceProcentIndex);
			mapCuratorPrice.put(rec.nomenclature_id.toString(), rec);
		}
		if (mapCuratorPrice.size() == 0) {
			// Наценка по этому клиенту не применяется - выходим
			cursor.close();
			return null;
		}
		ArrayList<String> list = new ArrayList<String>();
		String currentNomenclatureId = nomenclature_id.toString();
		// Затем для данной номенклатуры пройдем в цикле но верхнего уровня
		do {
			// Номенклатура найдена
			if (mapCuratorPrice.containsKey(currentNomenclatureId)) {
				return mapCuratorPrice.get(currentNomenclatureId);
			}
			list.add(currentNomenclatureId);
			//
			Cursor cursorNomenclature = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"parent_id"}, "id=?", new String[]{currentNomenclatureId}, null);
			if (cursorNomenclature.moveToFirst()) {
				currentNomenclatureId = cursorNomenclature.getString(0);
			}
			cursorNomenclature.close();
		} while (!list.contains(currentNomenclatureId));    // защита от зацикливания
		cursor.close();
		return null;
	}

	static boolean LoadNomenclature(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int nomenclature_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];
		//ArrayList<ContentValues> values = new ArrayList<Integer>();

		if (!bUpdateMode) {
			//m_nomenclature.clear();
			//("delete * from nomenclature");
			contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null);
		}

		//SQLiteStatement stmt = db.compileStatement("insert into nomenclature() values (?)");
		//stmt.bindString(1, "US");
		//stmt.execute();

		MySingleton g = MySingleton.getInstance();

		NomenclatureRecord nr = new NomenclatureRecord();

		nom_idx = -109;
	    /*
	    pos=0;
	    int len=buf.length();
	    boolean bCancelLoad=false;
	    while (pos<len&&!bCancelLoad)
	    {
	        //int slen=0;
	        String sc="";
	        while (pos<len)
	        {
	            boolean bBreak=false;
	            // r игнорируем
	            if (buf.charAt(pos)!='\r')
	            {
	                if (buf.charAt(pos)!='\n')
	                    sc=sc+buf.charAt(pos);
	                else
	                    break;
	            }
	            pos++;
	        }
	        if(pos<len)
	        {
	            if (buf.charAt(pos)=='\n')
	                pos++;
	        }
	    */
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if ((nom_idx >= 13 && nr.IsFolder == 2) || (nom_idx >= 5 && nr.IsFolder == 1)) {
					//m_nomenclature[nr.id]=nr;
					//int i;
					//for (i=0; i<sizeof(nr.descr); i++)
					//    if (nr.descr[i]==L'%')
					//        nr.descr[i]=L'0';
					//if (nr.parent_id==m_empty_id)
					//{
					//    //TRACE(nr.descr);
					//    //TRACE0("\n");
					//}
					nr.descr = nr.descr.replace("%", "0");

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", nr.id.m_id);
					cv.put("code", nr.code);
					cv.put("isFolder", nr.IsFolder);
					cv.put("parent_id", nr.parent_id.m_id);
					cv.put("descr", nr.descr);
					cv.put("descr_lower", nr.descr.toLowerCase(Locale.getDefault()));
					cv.put("descrFull", nr.descrFull);
					cv.put("quant_1", nr.quant_1);
					cv.put("quant_2", nr.quant_2);
					cv.put("edizm_1_id", nr.edizm_1_id.m_id);
					cv.put("edizm_2_id", nr.edizm_2_id.m_id);
					cv.put("quant_k_1", nr.quant_k_1);
					cv.put("quant_k_2", nr.quant_k_2);
					cv.put("opt_price", nr.opt_price);
					cv.put("m_opt_price", nr.m_opt_price);
					cv.put("rozn_price", nr.rozn_price);
					cv.put("incom_price", nr.incom_price);
					cv.put("IsInPrice", nr.IsInPrice);
					cv.put("flagWithoutDiscont", nr.flagWithoutDiscont);
					cv.put("weight_k_1", nr.weight_k_1);
					cv.put("weight_k_2", nr.weight_k_2);
					cv.put("min_quantity", nr.min_quantity);
					cv.put("multiplicity", nr.multiplicity);
					cv.put("required_sales", nr.required_sales);
					cv.put("flags", nr.flags);
					cv.put("order_for_sorting", nr.order_for_sorting);
					cv.put("group_of_analogs", nr.group_of_analogs);
					cv.put("nomenclature_color", nr.nomenclature_color);
					// с 06.02.2019
					// после загрузки номенклатуры все равно происходит сканирование каталога с фото
					cv.put("image_file", "");
					cv.put("image_file_checksum", 0);
					cv.put("image_width", 0);
					cv.put("image_height", 0);
					cv.put("image_file_size", 0);

					cv.put("compose_with", nr.compose_with.m_id);

					//if (db.update("nomenclature", cv, "id=", new String[]{nr.id.m_id})==0)
					//{
					//	cv.put("id", nr.id.m_id);
					//	db.insert("nomenclature", null, cv);
					//}
					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values);
						bulk_idx = 0;
					}
					//Uri newUri = contentResolver.insert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv);

					// иначе у групп будет цена
					nr.opt_price = 0;
					nr.m_opt_price = 0;
					nr.rozn_price = 0;
					nr.incom_price = 0;
					nr.IsInPrice = 0;
					nr.weight_k_1 = 0.0;
					nr.weight_k_2 = 0.0;
					nr.flags = 0;
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_nomenclature_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_nomenclature_version = -ver;
								contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_nomenclature_version = ver;
						} else {
							myBase.m_nomenclature_version = Integer.parseInt(sc);
						}
						nomenclature_system_version = 0;
						break;
					case -108:
						nomenclature_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						nr.clear();
						nr.id = new MyID(sc);
						break;
					case 1: // IsFolder
						nr.IsFolder = Integer.parseInt(sc);
						break;
					case 2: // parent_id
						nr.parent_id = new MyID(sc);
						// 28.01.2021 багфикс для тандема
						if (nr.parent_id.isEmpty())
							nr.parent_id=new MyID();
						break;
					case 3: // code
						nr.code = new String(sc);
						break;
					case 4: // descr
						nr.descr = new String(sc);
						break;
					case 5: // descrFull
						nr.descrFull = new String(sc);
						if (nr.IsFolder == 1) {
							nom_idx = 113; // 14 - IsInPrice, для элемента и группы
						}
						break;
					case 6:
						// quant_1 - наименование базовой единицы
						nr.quant_1 = new String(sc);
						break;
					case 7: // quant_2 - наименование единицы упаковок
						nr.quant_2 = new String(sc);
						break;
					case 8: // quant_k_1 - коэффициент базовой единицы (всегда 1.0)
						nr.quant_k_1 = Double.parseDouble(sc);
						break;
					case 9: // quant_k_2 - коэффициент единиц измерения упаковок
						nr.quant_k_2 = Double.parseDouble(sc);
						break;
					case 10:
						if (g.Common.MEGA) {
							nr.opt_price = Double.parseDouble(sc);
						} else {
							//Мета.ЗначениеВСтрокуБД(СпрНом.БазоваяЕдиница)
							nr.edizm_1_id = new MyID(sc);
						}
						break;
					case 11:
						if (g.Common.MEGA) {
							nr.m_opt_price = Double.parseDouble(sc);
						} else {
							//Мета.ЗначениеВСтрокуБД(СпрНом.ОсновнаяЕдиница)
							nr.edizm_2_id = new MyID(sc);
						}
						break;
					case 12:
						if (g.Common.MEGA) {
							nr.rozn_price = Double.parseDouble(sc);
						}
						break;
					case 13:
						if (g.Common.MEGA) {
							nr.incom_price = Double.parseDouble(sc);
						}
						break;
					case 14:
						nr.IsInPrice = Integer.parseInt(sc);
						break;
					case 15:
						nr.flagWithoutDiscont = Integer.parseInt(sc);
						break;
					case 16:
						// Вес базовой единицы
						nr.weight_k_1 = Double.parseDouble(sc);
						break;
					case 17:
						// Вес упаковки
						nr.weight_k_2 = Double.parseDouble(sc);
						nr.min_quantity = 0.0;
						nr.multiplicity = 0.0;
						nr.flags = 0;
						nr.nomenclature_color = 0;
						nr.compose_with=new MyID();
						break;
					case 18:
						// Минимальное количество для продажи
						nr.min_quantity = Double.parseDouble(sc);
						break;
					case 19:
						// Кратность для продажи
						nr.multiplicity = Double.parseDouble(sc);
						break;
					case 20:
						// Требуемые продажи
						nr.required_sales = Double.parseDouble(sc);
						break;
					case 21:
						// Флаги
						int pos_next = sc.indexOf(';');
						if (pos_next == -1) {
							nr.flags = Integer.parseInt(sc);
							nr.nomenclature_color = 0;
						} else {
							nr.flags = Integer.parseInt(sc.substring(0, pos_next));
							nr.nomenclature_color = Integer.parseInt(sc.substring(pos_next + 1));
						}
						break;
					// для группы
					case 114:
						// в прайсе
						nr.IsInPrice = Integer.parseInt(sc);
						break;
					case 115:
						// в данной группы аналоги товаров
						nr.group_of_analogs = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if ((nom_idx >= 13 && nr.IsFolder == 2) || (nom_idx >= 5 && nr.IsFolder == 1)) {
			//m_nomenclature.insert(std::make_pair(nr.id, nr));
			//m_nomenclature[nr.id]=nr;

			nr.descr = nr.descr.replace("%", "0");
			ContentValues cv = new ContentValues();
			cv.clear();
			cv.put("id", nr.id.m_id);
			cv.put("code", nr.code);
			cv.put("isFolder", nr.IsFolder);
			cv.put("parent_id", nr.parent_id.m_id);
			cv.put("descr", nr.descr);
			cv.put("descr_lower", nr.descr.toLowerCase(Locale.getDefault()));
			cv.put("descrFull", nr.descrFull);
			cv.put("quant_1", nr.quant_1);
			cv.put("quant_2", nr.quant_2);
			cv.put("edizm_1_id", nr.edizm_1_id.m_id);
			cv.put("edizm_2_id", nr.edizm_2_id.m_id);
			cv.put("quant_k_1", nr.quant_k_1);
			cv.put("quant_k_2", nr.quant_k_2);
			cv.put("opt_price", nr.opt_price);
			cv.put("m_opt_price", nr.m_opt_price);
			cv.put("rozn_price", nr.rozn_price);
			cv.put("incom_price", nr.incom_price);
			cv.put("IsInPrice", nr.IsInPrice);
			cv.put("flagWithoutDiscont", nr.flagWithoutDiscont);
			cv.put("weight_k_1", nr.weight_k_1);
			cv.put("weight_k_2", nr.weight_k_2);
			cv.put("min_quantity", nr.min_quantity);
			cv.put("multiplicity", nr.multiplicity);
			cv.put("required_sales", nr.required_sales);
			cv.put("flags", nr.flags);
			cv.put("order_for_sorting", nr.order_for_sorting);
			cv.put("group_of_analogs", nr.group_of_analogs);
			cv.put("nomenclature_color", nr.nomenclature_color);
			// с 06.02.2019
			// после загрузки номенклатуры все равно происходи сканирование каталога с фото
			cv.put("image_file", "");
			cv.put("image_file_checksum", 0);
			cv.put("image_width", 0);
			cv.put("image_height", 0);
			cv.put("image_file_size", 0);

			cv.put("compose_with", nr.compose_with.m_id);

			//if (db.update("nomenclature", cv, "id=", new String[]{nr.id.m_id})==0)
			//{
			//	cv.put("id", nr.id.m_id);
			//	db.insert("nomenclature", null, cv);
			//}
			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.clear();
		cv.put("param", "NOMENCLATURE");
		cv.put("ver", myBase.m_nomenclature_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadClients(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int clients_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null);
		}

		ClientRecord cr = new ClientRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if ((nom_idx >= 9 && cr.IsFolder == 2) || (nom_idx >= 5 && cr.IsFolder == 1)) {
					cr.descr = cr.descr.replace("%", "0");

					ContentValues cv = new ContentValues();
					cv.put("id", cr.id.m_id);
					cv.put("code", cr.code);
					cv.put("isFolder", cr.IsFolder);
					cv.put("parent_id", cr.parent_id.toString());
					cv.put("descr", cr.descr);
					cv.put("descr_lower", cr.descr.toLowerCase(Locale.getDefault()));
					cv.put("descrFull", cr.descrFull);
					cv.put("address", cr.address);
					cv.put("address2", cr.address2);
					cv.put("comment", cr.comment);
					cv.put("curator_id", cr.curator_id.toString());
					cv.put("priceType", cr.priceType.value());
					cv.put("Blocked", cr.Blocked);
					cv.put("flags", cr.flags);
                    cv.put("email_for_cheques", cr.email_for_cheques);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.CLIENTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_clients_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_clients_version = -ver;
								contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_clients_version = ver;
						} else {
							myBase.m_clients_version = Integer.parseInt(sc);
						}
						clients_system_version = 0;
						break;
					case -108:
						clients_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						//cr.id=get_id(sc);
						cr.id = new MyID(sc);
						cr.email_for_cheques="";
						break;
					case 1: // IsFolder
						cr.IsFolder = Integer.parseInt(sc);
						break;
					case 2: // parent_id
						//cr.parent_id=get_id(sc);
						cr.parent_id = new MyID(sc);
						// 28.01.2021 багфикс для тандема
						if (cr.parent_id.isEmpty())
							cr.parent_id=new MyID();
						break;
					case 3: // code
						cr.code = new String(sc);
						break;
					case 4: // descr
						cr.descr = new String(sc);
						break;
					case 5: // descrFull
						cr.descrFull = new String(sc);
						break;
					case 6: // address
						cr.address = new String(sc);
						break;
					case 7: // address2
						cr.address2 = new String(sc);
						break;
					case 8:
						cr.Blocked = Integer.parseInt(sc);
						break;
					case 9: // comment
						cr.comment = new String(sc);
						break;
					case 10:
						cr.priceType = E_PRICE_TYPE.E_PRICE_TYPE_ROZN;
						if (sc.equals(myBase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_M_OPT)))
							cr.priceType = E_PRICE_TYPE.E_PRICE_TYPE_M_OPT;
						if (sc.equals(myBase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_OPT)))
							cr.priceType = E_PRICE_TYPE.E_PRICE_TYPE_OPT;
						// однако такие контрагенты точно не должны через КПК работать
						if (sc.equals(myBase.GetPriceName(E_PRICE_TYPE.E_PRICE_TYPE_INCOM)))
							cr.priceType = E_PRICE_TYPE.E_PRICE_TYPE_INCOM;
						break;
					case 11:
						// в версии, начиная с 1.00
						//cr.curator_id=get_id(sc);
						cr.curator_id = new MyID(sc);
						break;
					case 12: // начиная с версии 107 Blocked
						cr.Blocked = Integer.parseInt(sc) & 1;
						// Флаги с версии 302
						cr.flags = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if ((nom_idx >= 9 && cr.IsFolder == 2) || (nom_idx >= 5 && cr.IsFolder == 1)) {
			//m_nomenclature.insert(std::make_pair(nr.id, nr));
			//m_nomenclature[nr.id]=nr;
			cr.descr = cr.descr.replace("%", "0");

			ContentValues cv = new ContentValues();
			cv.put("id", cr.id.m_id);
			cv.put("code", cr.code);
			cv.put("isFolder", cr.IsFolder);
			cv.put("parent_id", cr.parent_id.toString());
			cv.put("descr", cr.descr);
			cv.put("descr_lower", cr.descr.toLowerCase(Locale.getDefault()));
			cv.put("descrFull", cr.descrFull);
			cv.put("address", cr.address);
			cv.put("address2", cr.address2);
			cv.put("comment", cr.comment);
			cv.put("curator_id", cr.curator_id.toString());
			cv.put("priceType", String.valueOf(cr.priceType.value())); // Мега
			cv.put("Blocked", cr.Blocked);
			cv.put("flags", cr.flags);
            cv.put("email_for_cheques", cr.email_for_cheques);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.CLIENTS_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "CLIENTS");
		cv.put("ver", myBase.m_clients_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadAgreements(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int agreements_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null);
		}

		AgreementRecord ar = new AgreementRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					ar.descr = ar.descr.replace("%", "0");

					ContentValues cv = new ContentValues();
					cv.put("id", ar.id.m_id);
					cv.put("owner_id", ar.owner_id.toString());
					cv.put("code", ar.code);
					cv.put("descr", ar.descr);
					cv.put("price_type_id", ar.price_type_id.toString());
					cv.put("kredit_days", ar.kredit_days);
					cv.put("kredit_sum", ar.kredit_sum);
					cv.put("organization_id", ar.organization_id.toString());
					cv.put("default_manager_id", ar.default_manager_id.toString());
					cv.put("sale_id", ar.sale_id.toString());
					cv.put("flags", ar.flags);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_agreements_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_agreements_version = -ver;
								contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_agreements_version = ver;
						} else {
							myBase.m_agreements_version = Integer.parseInt(sc);
						}
						agreements_system_version = 0;
						break;
					case -108:
						agreements_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						ar.id = new MyID(sc);
						ar.flags = 0;
						break;
					case 1: // owner_id
						ar.owner_id = new MyID(sc);
						break;
					case 2: // code
						ar.code = new String(sc);
						break;
					case 3: // descr
						ar.descr = new String(sc);
						break;
					case 4: // price_type_id
						ar.price_type_id = new MyID(sc);
						break;
					case 5: // sale_id
						ar.sale_id = new MyID(sc);
						break;
					case 6: // kredit_days
						ar.kredit_days = Integer.parseInt(sc);
						break;
					case 7: // kredit_sum
						ar.kredit_sum = Double.parseDouble(sc);
						ar.organization_id = new MyID();
						break;
					case 8: // organization_id
						ar.organization_id = new MyID(sc);
						break;
					case 9: // default_manager_id
						ar.default_manager_id = new MyID(sc);
						break;
					case 10: // flags
						ar.flags = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6) {
			ar.descr = ar.descr.replace("%", "0");

			ContentValues cv = new ContentValues();
			cv.put("id", ar.id.m_id);
			cv.put("owner_id", ar.owner_id.toString());
			cv.put("code", ar.code);
			cv.put("descr", ar.descr);
			cv.put("price_type_id", ar.price_type_id.toString());
			cv.put("kredit_days", ar.kredit_days);
			cv.put("kredit_sum", ar.kredit_sum);
			cv.put("organization_id", ar.organization_id.toString());
			cv.put("sale_id", ar.sale_id.toString());
			cv.put("flags", ar.flags);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "AGREEMENTS");
		cv.put("ver", myBase.m_agreements_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadAgreements30(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int agreements_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, null, null);
		}

		AgreementRecord ar = new AgreementRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					ar.descr = ar.descr.replace("%", "0");

					ContentValues cv = new ContentValues();
					cv.put("id", ar.id.m_id);
					cv.put("owner_id", ar.owner_id.toString());
					cv.put("code", ar.code);
					cv.put("descr", ar.descr);
					cv.put("price_type_id", ar.price_type_id.toString());
					cv.put("kredit_days", ar.kredit_days);
					cv.put("kredit_sum", ar.kredit_sum);
					cv.put("organization_id", ar.organization_id.toString());
					cv.put("default_manager_id", ar.default_manager_id.toString());
					cv.put("sale_id", ar.sale_id.toString());
					cv.put("flags", ar.flags);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				// соглашение+контрагент (именно контрагент, не партнер)
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, "id=? and owner_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, "id=? and owner_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_agreements30_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_agreements30_version = -ver;
								contentResolver.delete(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_agreements30_version = ver;
						} else {
							myBase.m_agreements30_version = Integer.parseInt(sc);
						}
						agreements_system_version = 0;
						break;
					case -108:
						agreements_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						ar.id = new MyID(sc);
						ar.flags = 0;
						break;
					case 1: // owner_id
						ar.owner_id = new MyID(sc);
						break;
					case 2: // code
						ar.code = new String(sc);
						break;
					case 3: // descr
						ar.descr = new String(sc);
						break;
					case 4: // price_type_id
						ar.price_type_id = new MyID(sc);
						break;
					case 5: // sale_id
						ar.sale_id = new MyID(sc);
						break;
					case 6: // kredit_days
						ar.kredit_days = Integer.parseInt(sc);
						break;
					case 7: // kredit_sum
						ar.kredit_sum = Double.parseDouble(sc);
						ar.organization_id = new MyID();
						break;
					case 8: // organization_id
						ar.organization_id = new MyID(sc);
						break;
					case 9: // default_manager_id
						ar.default_manager_id = new MyID(sc);
						break;
					case 10: // flags
						ar.flags = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6) {
			ar.descr = ar.descr.replace("%", "0");

			ContentValues cv = new ContentValues();
			cv.put("id", ar.id.m_id);
			cv.put("owner_id", ar.owner_id.toString());
			cv.put("code", ar.code);
			cv.put("descr", ar.descr);
			cv.put("price_type_id", ar.price_type_id.toString());
			cv.put("kredit_days", ar.kredit_days);
			cv.put("kredit_sum", ar.kredit_sum);
			cv.put("organization_id", ar.organization_id.toString());
			cv.put("sale_id", ar.sale_id.toString());
			cv.put("flags", ar.flags);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS30_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "AGREEMENTS30");
		cv.put("ver", myBase.m_agreements30_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadOstat(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int rests_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null);
		}

        MySingleton g = MySingleton.getInstance();

		QuantityRecord qr = new QuantityRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					ContentValues cv = new ContentValues();
					cv.put("stock_id", qr.stock_id.m_id);
					cv.put("nomenclature_id", qr.nomenclature_id.m_id);
					cv.put("quantity", qr.quantity);
					cv.put("quantity_reserve", qr.quantity_reserve);
					if (g.Common.TANDEM)
						cv.put("organization_id", qr.organization_id.m_id);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.RESTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
                if (g.Common.TANDEM&&sc.length()==4+73+36*2) {
                    contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, "nomenclature_id=? and stock_id=? and organization_id=?", new String[]{sc.substring(4, 4 + 73), sc.substring(4 + 73, 4 + 73 + 36), sc.substring(4 + 73+36, 4 + 73 + 36+36)});
                } else
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, "nomenclature_id=? and stock_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, "nomenclature_id=? and stock_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_rests_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_rests_version = -ver;
								contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_rests_version = ver;
						} else {
							myBase.m_rests_version = Integer.parseInt(sc);
						}
						rests_system_version = 0;
						break;
					case -108:
						rests_system_version = Integer.parseInt(sc);
						break;
					case 0: // nomenclature_id
						qr.nomenclature_id = new MyID(sc);
						break;
					case 1: // stock_id
						qr.stock_id = new MyID(sc);
						break;
					case 2: // quantity
						qr.quantity = Double.parseDouble(sc);
						break;
					case 3: // quantity_reserve
						qr.quantity_reserve = Double.parseDouble(sc);
						break;
                    case 4: // organization_id
                        qr.organization_id = new MyID(sc);
                        break;

				}
				nom_idx++;
			}
		}
		if (nom_idx >= 4) {
			ContentValues cv = new ContentValues();
			cv.put("stock_id", qr.stock_id.m_id);
			cv.put("nomenclature_id", qr.nomenclature_id.m_id);
			cv.put("quantity", qr.quantity);
			cv.put("quantity_reserve", qr.quantity_reserve);
			if (g.Common.TANDEM)
				cv.put("organization_id", qr.organization_id.m_id);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.RESTS_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "RESTS");
		cv.put("ver", myBase.m_rests_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadDolg(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int saldo_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null);
		}

		SaldoRecord sr = new SaldoRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 2) {
					ContentValues cv = new ContentValues();
					cv.put("client_id", sr.client_id.m_id);
					cv.put("saldo", sr.saldo);
					cv.put("saldo_past", sr.saldo_past);
					cv.put("saldo_past30", sr.saldo_past30);
					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values);
						bulk_idx = 0;
					}
					//Uri newUri = contentResolver.insert(MTradeContentProvider.SALDO_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, "client_id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_saldo_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_saldo_version = -ver;
								contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_saldo_version = ver;
						} else {
							myBase.m_saldo_version = Integer.parseInt(sc);
						}
						saldo_system_version = 0;
						break;
					case -108:
						saldo_system_version = Integer.parseInt(sc);
						break;
					case 0: // client_id
						sr.client_id = new MyID(sc);
						break;
					case 1: // saldo
						sr.saldo = Double.parseDouble(sc);
						break;
					case 2: // saldo_past
						sr.saldo_past = Double.parseDouble(sc);
						sr.saldo_past30 = 0.0;
						break;
					case 3: // saldo_past30
						sr.saldo_past30 = Double.parseDouble(sc);
						break;
				}
				nom_idx++;
			}
		}
		if (nom_idx >= 2) {
			ContentValues cv = new ContentValues();
			cv.put("client_id", sr.client_id.m_id);
			cv.put("saldo", sr.saldo);
			cv.put("saldo_past", sr.saldo_past);
			cv.put("saldo_past30", sr.saldo_past30);
			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.SALDO_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();

		cv.put("param", "SALDO");
		cv.put("ver", myBase.m_saldo_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadDolgExtended(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) throws RemoteException, OperationApplicationException {
		int saldo_ext_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];
		int bulk_delete_idx = 0;

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null);
		}

		SaldoExtRecord sr = new SaldoExtRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 2) {
					ContentValues cv = new ContentValues();
					cv.put("client_id", sr.client_id.toString());
					cv.put("agreement30_id", sr.agreement30_id.toString());
					cv.put("agreement_id", sr.agreement_id.toString());
					cv.put("document_id", sr.document_id.toString());
					cv.put("manager_id", sr.manager_id.toString());
					cv.put("document_descr", sr.document_descr);
					cv.put("document_datetime", sr.document_datetime);
					cv.put("manager_descr", sr.manager_descr);
					cv.put("agreement_descr", sr.agreement_descr);
					cv.put("organization_descr", sr.organization_descr);
					cv.put("saldo", sr.saldo);
					cv.put("saldo_past", sr.saldo_past);
					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, values);
						bulk_idx = 0;
					}
					//Uri newUri = contentResolver.insert(MTradeContentProvider.SALDO_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				ContentProviderOperation operation = ContentProviderOperation
						.newDelete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI)
						.withSelection("client_id=? and agreement_id=? and document_id=? and manager_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36 * 1, 4 + 36 * 2), sc.substring(4 + 36 * 2, 4 + 36 * 3), sc.substring(4 + 36 * 3, 4 + 36 * 4)})
						.build();
				operations.add(operation);
				bulk_delete_idx++;
				if (bulk_delete_idx >= BULK_SIZE) {
					contentResolver.applyBatch(MTradeContentProvider.AUTHORITY, operations);
					bulk_delete_idx = 0;
				}
				//contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, "client_id=? and agreement_id=? and document_id=? and manager_id=?",
				//		new String[] {sc.substring(4, 4+36), sc.substring(4+36*1, 4+36*2), sc.substring(4+36*2, 4+36*3), sc.substring(4+36*3, 4+36*4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_saldo_extended_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_saldo_extended_version = -ver;
								contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_saldo_extended_version = ver;
						} else {
							myBase.m_saldo_extended_version = Integer.parseInt(sc);
						}
						saldo_ext_system_version = 0;
						break;
					case -108:
						saldo_ext_system_version = Integer.parseInt(sc);
						break;
					case 0: // client_id
						sr.client_id = new MyID(sc);
						sr.agreement30_id = new MyID();
						break;
					case 1: // agreement_id
						sr.agreement_id = new MyID(sc);
						break;
					case 2: // document_id
						sr.document_id = new MyID(sc);
						break;
					case 3: // manager_id
						sr.manager_id = new MyID(sc);
						break;
					case 4: // document_descr
						sr.document_descr = new String(sc);
						break;
					case 5: // document_datetime
						sr.document_datetime = new String(sc);
						break;
					case 6: // manager_descr
						sr.manager_descr = new String(sc);
						break;
					case 7: // agreement_descr
						sr.agreement_descr = new String(sc);
						break;
					case 8: // organization_descr
						sr.organization_descr = new String(sc);
						break;
					case 9: // saldo
						sr.saldo = Double.parseDouble(sc);
						break;
					case 10: // saldo_past
						sr.saldo_past = Double.parseDouble(sc);
						break;
				}
				nom_idx++;
			}
		}
		if (nom_idx >= 2) {
			ContentValues cv = new ContentValues();
			cv.put("client_id", sr.client_id.m_id);
			cv.put("agreement30_id", sr.agreement30_id.toString());
			cv.put("agreement_id", sr.agreement_id.toString());
			cv.put("document_id", sr.document_id.toString());
			cv.put("manager_id", sr.manager_id.toString());
			cv.put("document_descr", sr.document_descr);
			cv.put("document_datetime", sr.document_datetime);
			cv.put("manager_descr", sr.manager_descr);
			cv.put("agreement_descr", sr.agreement_descr);
			cv.put("organization_descr", sr.organization_descr);
			cv.put("saldo", sr.saldo);
			cv.put("saldo_past", sr.saldo_past);
			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.SALDO_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, values_2);
		}

		if (bulk_delete_idx > 0) {
			contentResolver.applyBatch(MTradeContentProvider.AUTHORITY, operations);
		}

		ContentValues cv = new ContentValues();

		cv.put("param", "SALDO_EXT");
		cv.put("ver", myBase.m_saldo_extended_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadPriceTypes(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int pricetypes_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
		}

		PriceTypeRecord pr = new PriceTypeRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					ContentValues cv = new ContentValues();
					cv.put("id", pr.id.m_id);
					cv.put("isFolder", pr.isFolder);
					cv.put("code", pr.code);
					cv.put("descr", pr.descr);

					Uri newUri = contentResolver.insert(MTradeContentProvider.PRICETYPES_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_pricetypes_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_pricetypes_version = -ver;
								contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_pricetypes_version = ver;
						} else {
							myBase.m_pricetypes_version = Integer.parseInt(sc);
						}
						pricetypes_system_version = 0;
						break;
					case -108:
						pricetypes_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						pr.id = new MyID(sc);
						break;
					case 1: // isFolder
						pr.isFolder = Integer.parseInt(sc);
						break;
					case 2: // code
						pr.code = new String(sc);
						break;
					case 3: // descr
						pr.descr = new String(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 4) {
			ContentValues cv = new ContentValues();
			cv.put("id", pr.id.m_id);
			cv.put("isFolder", pr.isFolder);
			cv.put("code", pr.code);
			cv.put("descr", pr.descr);

			Uri newUri = contentResolver.insert(MTradeContentProvider.PRICETYPES_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "PRICETYPES");
		cv.put("ver", myBase.m_pricetypes_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadStocks(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int stocks_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null);
		}

		StockRecord sr = new StockRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 3) {
					ContentValues cv = new ContentValues();
					cv.put("id", sr.id.m_id);
					cv.put("isFolder", sr.isFolder);
					cv.put("code", sr.code);
					cv.put("descr", sr.descr);
					// дополнительное поле
					cv.put("flags", sr.flags);

					Uri newUri = contentResolver.insert(MTradeContentProvider.STOCKS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_stocks_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_stocks_version = -ver;
								contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_stocks_version = ver;
						} else {
							myBase.m_stocks_version = Integer.parseInt(sc);
						}
						stocks_system_version = 0;
						break;
					case -108:
						stocks_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						sr.id = new MyID(sc);
						sr.isFolder = 0;
						break;
					case 1: // code
						sr.code = new String(sc);
						break;
					case 2: // descr
						sr.descr = new String(sc);
						sr.flags = 0;
						break;
					case 3: // flags
						sr.flags = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 3) {
			ContentValues cv = new ContentValues();
			cv.put("id", sr.id.m_id);
			cv.put("isFolder", sr.isFolder);
			cv.put("code", sr.code);
			cv.put("descr", sr.descr);
			// дополнительное поле
			cv.put("flags", sr.flags);

			Uri newUri = contentResolver.insert(MTradeContentProvider.STOCKS_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "STOCKS");
		cv.put("ver", myBase.m_stocks_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadDistribsContracts(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int distribs_contracts_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, null, null);
		}

		DistribsContractsRecord sr = new DistribsContractsRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 3) {
					ContentValues cv = new ContentValues();
					cv.put("id", sr.id.m_id);
					cv.put("code", sr.code);
					cv.put("descr", sr.descr);
					cv.put("position", sr.position);

					Uri newUri = contentResolver.insert(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_distribs_contracts_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_distribs_contracts_version = -ver;
								contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_distribs_contracts_version = ver;
						} else {
							myBase.m_distribs_contracts_version = Integer.parseInt(sc);
						}
						distribs_contracts_system_version = 0;
						break;
					case -108:
						distribs_contracts_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						sr.id = new MyID(sc);
						break;
					case 1: // code
						sr.code = new String(sc);
						break;
					case 2: // descr
						sr.descr = new String(sc);
						break;
					case 3: // position
						sr.position = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 3) {
			ContentValues cv = new ContentValues();
			cv.put("id", sr.id.m_id);
			cv.put("code", sr.code);
			cv.put("descr", sr.descr);
			cv.put("position", sr.position);

			Uri newUri = contentResolver.insert(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "DISTRIBS_CONTRACTS");
		cv.put("ver", myBase.m_distribs_contracts_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadPrice(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int price_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];
		//ArrayList<ContentValues> values = new ArrayList<Integer>();

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null);
		}

		MySingleton g=MySingleton.getInstance();

		PriceRecord pr = new PriceRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 7) {
					ContentValues cv = new ContentValues();
					cv.put("nomenclature_id", pr.nomenclature_id.m_id);
					cv.put("price_type_id", pr.price_type_id.m_id);
					cv.put("ed_izm_id", pr.ed_izm_id.m_id);
					cv.put("edIzm", pr.edIzm);
					cv.put("price", pr.price);
					cv.put("priceProcent", pr.priceProcent);
					cv.put("k", pr.k);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values);
						bulk_idx = 0;
					}
					//Uri newUri = contentResolver.insert(MTradeContentProvider.PRICES_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				if (g.Common.TANDEM&&sc.length() == 4 + 73+36) {
					contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, "nomenclature_id=? and price_type_id=?", new String[]{sc.substring(4, 4 + 73), sc.substring(4 + 73, 4 + 73 + 36)});
				}
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, "nomenclature_id=? and price_type_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, "nomenclature_id=? and price_type_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_prices_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_prices_version = -ver;
								contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_prices_version = ver;
						} else {
							myBase.m_prices_version = Integer.parseInt(sc);
						}
						price_system_version = 0;
						break;
					case -108:
						price_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						pr.nomenclature_id = new MyID(sc);
						break;
					case 1: // price_type_id
						pr.price_type_id = new MyID(sc);
						break;
					case 2: // ed_izm_id
						pr.ed_izm_id = new MyID(sc);
						break;
					case 3: // edizm
						pr.edIzm = new String(sc);
						break;
					case 4: // price
						pr.price = Double.parseDouble(sc);
						break;
					case 5: // price_procent
						pr.priceProcent = Double.parseDouble(sc);
						break;
					case 6: // k
						pr.k = Double.parseDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 7) {
			ContentValues cv = new ContentValues();
			cv.put("nomenclature_id", pr.nomenclature_id.m_id);
			cv.put("price_type_id", pr.price_type_id.m_id);
			cv.put("ed_izm_id", pr.ed_izm_id.m_id);
			cv.put("edIzm", pr.edIzm);
			cv.put("price", pr.price);
			cv.put("priceProcent", pr.priceProcent);
			cv.put("k", pr.k);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.PRICES_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "PRICES");
		cv.put("ver", myBase.m_prices_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadAgents(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int agents_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null);
		}

		AgentRecord ar = new AgentRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 3) {
					ContentValues cv = new ContentValues();
					cv.put("id", ar.id.m_id);
					cv.put("code", ar.code);
					cv.put("descr", ar.descr);

					Uri newUri = contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_agents_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_agents_version = -ver;
								contentResolver.delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_agents_version = ver;
						} else {
							myBase.m_agents_version = Integer.parseInt(sc);
						}
						agents_system_version = 0;
						break;
					case -108:
						agents_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						ar.id = new MyID(sc);
						break;
					case 1: // code
						ar.code = new String(sc);
						break;
					case 2: // descr
						ar.descr = new String(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 3) {
			ContentValues cv = new ContentValues();
			cv.put("id", ar.id.m_id);
			cv.put("code", ar.code);
			cv.put("descr", ar.descr);

			Uri newUri = contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);
		}

		// Добавить агента "1C"
		Cursor emptyCursor = contentResolver.query(MTradeContentProvider.AGENTS_CONTENT_URI, new String[]{"_id"}, "id=?", new String[]{Constants.emptyID}, null);
		if (!emptyCursor.moveToFirst()) {
			ContentValues cv = new ContentValues();
			cv.put("id", Constants.emptyID);
			cv.put("code", "");
			cv.put("descr", "<1C>");

			Uri newUri = contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);
		}
		emptyCursor.close();

		ContentValues cv = new ContentValues();
		cv.put("param", "AGENTS");
		cv.put("ver", myBase.m_agents_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadPricesAgreements30(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int price_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];
		//ArrayList<ContentValues> values = new ArrayList<Integer>();

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, null, null);
		}

		PriceAgreement30Record pr = new PriceAgreement30Record();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 7) {
					ContentValues cv = new ContentValues();
					cv.put("agreement30_id", pr.agreement30_id.m_id);
					cv.put("nomenclature_id", pr.nomenclature_id.m_id);
					cv.put("pack_id", pr.pack_id.m_id);
					cv.put("ed_izm_id", pr.ed_izm_id.m_id);
					cv.put("edIzm", pr.edIzm);
					cv.put("price", pr.price);
					cv.put("k", pr.k);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, values);
						bulk_idx = 0;
					}
					//Uri newUri = contentResolver.insert(MTradeContentProvider.PRICES_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, "agreement30_id=? and nomenclature_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, "agreement30_id=? and nomenclature_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_prices_agreements30_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_prices_agreements30_version = -ver;
								contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_prices_agreements30_version = ver;
						} else {
							myBase.m_prices_agreements30_version = Integer.parseInt(sc);
						}
						price_system_version = 0;
						break;
					case -108:
						price_system_version = Integer.parseInt(sc);
						break;
					case 0: // agreement30_id
						pr.agreement30_id = new MyID(sc);
						break;
					case 1: // nomenclature_id
						pr.nomenclature_id = new MyID(sc);
						break;
					case 2: // pack_id
						pr.pack_id = new MyID(sc);
						break;
					case 3: // ed_izm_id
						pr.ed_izm_id = new MyID(sc);
						break;
					case 4: // edizm
						pr.edIzm = new String(sc);
						break;
					case 5: // price
						pr.price = Double.parseDouble(sc);
						break;
					case 6: // k
						pr.k = Double.parseDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 7) {
			ContentValues cv = new ContentValues();
			cv.put("agreement30_id", pr.agreement30_id.m_id);
			cv.put("nomenclature_id", pr.nomenclature_id.m_id);
			cv.put("pack_id", pr.pack_id.m_id);
			cv.put("ed_izm_id", pr.ed_izm_id.m_id);
			cv.put("edIzm", pr.edIzm);
			cv.put("price", pr.price);
			cv.put("k", pr.k);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, values);
				bulk_idx = 0;
			}
			//Uri newUri = contentResolver.insert(MTradeContentProvider.PRICES_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "PRICES_AGREEMENTS30");
		cv.put("ver", myBase.m_prices_agreements30_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadCurators(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int curators_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null);
		}

		CuratorRecord cr = new CuratorRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if ((nom_idx >= 5 && cr.isFolder == 2) || (nom_idx >= 5 && cr.isFolder == 1)) {
					ContentValues cv = new ContentValues();
					cv.put("id", cr.id.m_id);
					cv.put("parent_id", cr.parent_id.m_id);
					cv.put("isFolder", cr.isFolder);
					cv.put("code", cr.code);
					cv.put("descr", cr.descr);

					Uri newUri = contentResolver.insert(MTradeContentProvider.CURATORS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_curators_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_curators_version = -ver;
								contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_curators_version = ver;
						} else {
							myBase.m_curators_version = Integer.parseInt(sc);
						}
						curators_system_version = 0;
						break;
					case -108:
						curators_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						cr.id = new MyID(sc);
						break;
					case 1: // isFolder
						cr.isFolder = Integer.parseInt(sc);
						break;
					case 2: // parent_id
						cr.parent_id = new MyID(sc);
						// 28.01.2021 багфикс для тандема
						if (cr.parent_id.isEmpty())
							cr.parent_id=new MyID();
						break;
					case 3: // code
						cr.code = new String(sc);
						break;
					case 4: // descr
						cr.descr = new String(sc);
						break;
				}
				nom_idx++;
			}

		}
		if ((nom_idx >= 5 && cr.isFolder == 2) || (nom_idx >= 5 && cr.isFolder == 1)) {
			ContentValues cv = new ContentValues();
			cv.put("id", cr.id.m_id);
			cv.put("parent_id", cr.parent_id.m_id);
			cv.put("isFolder", cr.isFolder);
			cv.put("code", cr.code);
			cv.put("descr", cr.descr);

			Uri newUri = contentResolver.insert(MTradeContentProvider.CURATORS_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "CURATORS");
		cv.put("ver", myBase.m_curators_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadDistrPoints(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int distr_points_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null);
		}

		DistrPointRecord dr = new DistrPointRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 7) {
					ContentValues cv = new ContentValues();
					cv.put("id", dr.id.m_id);
					cv.put("owner_id", dr.owner_id.m_id);
					cv.put("code", dr.code);
					cv.put("descr", dr.descr);
					cv.put("address", dr.address);
					cv.put("phones", dr.phones);
					cv.put("contacts", dr.contacts);
					cv.put("price_type_id", dr.price_type_id.m_id);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_distr_points_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_distr_points_version = -ver;
								contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_distr_points_version = ver;
						} else {
							myBase.m_distr_points_version = Integer.parseInt(sc);
						}
						distr_points_system_version = 0;
						break;
					case -108:
						distr_points_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						dr.id = new MyID(sc);
						break;
					case 1: // owner_id
						dr.owner_id = new MyID(sc);
						break;
					case 2: // code
						dr.code = new String(sc);
						break;
					case 3: // descr
						dr.descr = new String(sc);
						break;
					case 4: // address
						dr.address = new String(sc);
						break;
					case 5: // phones
						dr.phones = new String(sc);
						break;
					case 6: // contacts
						dr.contacts = new String(sc);
						break;
					case 7:
						dr.price_type_id = new MyID(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 7) {
			ContentValues cv = new ContentValues();
			cv.put("id", dr.id.m_id);
			cv.put("owner_id", dr.owner_id.m_id);
			cv.put("code", dr.code);
			cv.put("descr", dr.descr);
			cv.put("address", dr.address);
			cv.put("phones", dr.phones);
			cv.put("contacts", dr.contacts);
			cv.put("price_type_id", dr.price_type_id.m_id);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, values);
				bulk_idx = 0;
			}

			//Uri newUri = contentResolver.insert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "D_POINTS");
		cv.put("ver", myBase.m_distr_points_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadOrganizations(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int organizations_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null);
		}

		OrganizationRecord or = new OrganizationRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 3) {
					ContentValues cv = new ContentValues();
					cv.put("id", or.id.m_id);
					cv.put("code", or.code);
					cv.put("descr", or.descr);

					Uri newUri = contentResolver.insert(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_organizations_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_organizations_version = -ver;
								contentResolver.delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_organizations_version = ver;
						} else {
							myBase.m_organizations_version = Integer.parseInt(sc);
						}
						organizations_system_version = 0;
						break;
					case -108:
						organizations_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						or.id = new MyID(sc);
						break;
					case 1: // code
						or.code = new String(sc);
						break;
					case 2: // descr
						or.descr = new String(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 3) {
			ContentValues cv = new ContentValues();
			cv.put("id", or.id.m_id);
			cv.put("code", or.code);
			cv.put("descr", or.descr);

			Uri newUri = contentResolver.insert(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "ORGANIZATIONS");
		cv.put("ver", myBase.m_organizations_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadClientsPrice(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int clients_price_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, null, null);
		}

		ClientPriceRecord cr = new ClientPriceRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					ContentValues cv = new ContentValues();
					cv.put("client_id", cr.client_id.toString());
					cv.put("nomenclature_id", cr.nomenclature_id.toString());
					cv.put("priceAdd", cr.priceAdd);
					cv.put("priceProcent", cr.priceProcent);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, cv);

				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, "nomenclature_id=? and client_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, "nomenclature_id=? and client_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_clients_price_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_clients_price_version = -ver;
								contentResolver.delete(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_clients_price_version = ver;
						} else {
							myBase.m_clients_price_version = Integer.parseInt(sc);
						}
						clients_price_system_version = 0;
						break;
					case -108:
						clients_price_system_version = Integer.parseInt(sc);
						break;
					case 0: // nomenclature_id
						cr.nomenclature_id = new MyID(sc);
						break;
					case 1: // client_id
						cr.client_id = new MyID(sc);
						break;
					case 2: // priceProcent
						cr.priceProcent = Common.MyStringToDouble(sc);
						break;
					case 3: // priceAdd
						cr.priceAdd = Common.MyStringToDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 4) {
			ContentValues cv = new ContentValues();
			cv.put("client_id", cr.client_id.toString());
			cv.put("nomenclature_id", cr.nomenclature_id.toString());
			cv.put("priceAdd", cr.priceAdd);
			cv.put("priceProcent", cr.priceProcent);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, values);
				bulk_idx = 0;
			}

			//Uri newUri = contentResolver.insert(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, cv);
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_PRICE_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "CLIENTS_PRICE");
		cv.put("ver", myBase.m_clients_price_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadCuratorsPrice(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int curators_price_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, null, null);
		}

		CuratorPriceRecord cr = new CuratorPriceRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					ContentValues cv = new ContentValues();
					cv.put("curator_id", cr.curator_id.toString());
					cv.put("nomenclature_id", cr.nomenclature_id.toString());
					cv.put("priceAdd", cr.priceAdd);
					cv.put("priceProcent", cr.priceProcent);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, values);
						bulk_idx = 0;
					}

				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				if (sc.length() == 4 + 36 * 2) {
					contentResolver.delete(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, "nomenclature_id=? and curator_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36)});
				} else if (sc.length() == 4 + 9 * 2) {
					contentResolver.delete(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, "nomenclature_id=? and curator_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_curators_price_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_curators_price_version = -ver;
								contentResolver.delete(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_curators_price_version = ver;
						} else {
							myBase.m_curators_price_version = Integer.parseInt(sc);
						}
						curators_price_system_version = 0;
						break;
					case -108:
						curators_price_system_version = Integer.parseInt(sc);
						break;
					case 0: // nomenclature_id
						cr.nomenclature_id = new MyID(sc);
						break;
					case 1: // curator_id
						cr.curator_id = new MyID(sc);
						break;
					case 2: // priceProcent
						cr.priceProcent = Common.MyStringToDouble(sc);
						break;
					case 3: // priceAdd
						cr.priceAdd = Common.MyStringToDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 4) {
			ContentValues cv = new ContentValues();
			cv.put("curator_id", cr.curator_id.toString());
			cv.put("nomenclature_id", cr.nomenclature_id.toString());
			cv.put("priceAdd", cr.priceAdd);
			cv.put("priceProcent", cr.priceProcent);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, values);
				bulk_idx = 0;
			}

		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.CURATORS_PRICE_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "CURATORS_PRICE");
		cv.put("ver", myBase.m_curators_price_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadVicariousPower(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int vicarious_power_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, null, null);
		}

		VicariousPowerRecord cr = new VicariousPowerRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 21) {
					ContentValues cv = new ContentValues();
					cv.put("id", cr.id.m_id);
					cv.put("descr", cr.descr);
					cv.put("numdoc", cr.numdoc);
					cv.put("datedoc", cr.datedoc);
					cv.put("date_action", cr.date_action);
					cv.put("comment", cr.comment);
					cv.put("client_id", cr.client_id.toString());
					cv.put("client_descr", cr.client_descr);
					cv.put("agreement_id", cr.agreement_id.toString());
					cv.put("agreement_descr", cr.agreement_descr);
					cv.put("fio_descr", cr.fio_descr);
					cv.put("manager_id", cr.manager_id.toString());
					cv.put("manager_descr", cr.manager_descr);
					cv.put("organization_id", cr.organization_id.toString());
					cv.put("organization_descr", cr.organization_descr);
					cv.put("state", cr.state);
					cv.put("sum_doc", cr.sum_doc);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.CLIENTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_vicarious_power_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_vicarious_power_version = -ver;
								contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_vicarious_power_version = ver;
						} else {
							myBase.m_vicarious_power_version = Integer.parseInt(sc);
						}
						vicarious_power_system_version = 0;
						break;
					case -108:
						vicarious_power_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						cr.id = new MyID(sc);
						break;
					case 1: // descr
						cr.descr = new String(sc);
						break;
					case 2: // numdoc
						cr.numdoc = new String(sc);
						break;
					case 3: // datedoc
						cr.datedoc = new String(sc);
						break;
					case 4: // date_action
						cr.date_action = new String(sc);
						break;
					case 5: // state
						cr.state = new String(sc);
						break;
					case 6: // comment
						cr.comment = new String(sc);
						break;
					case 7: // client_id
						cr.client_id = new MyID(sc);
						break;
					case 8: // client_descr
						cr.client_descr = new String(sc);
						break;
					case 9: // agreement_id
						cr.agreement_id = new MyID(sc);
						break;
					case 10: // agreement_descr
						cr.agreement_descr = new String(sc);
						break;
					case 11: // fio_descr
						cr.fio_descr = new String(sc);
						break;
					case 12: // manager_id
						cr.manager_id = new MyID(sc);
						break;
					case 13: // manager_descr
						cr.manager_descr = new String(sc);
						break;
					case 14: // organization_id
						cr.organization_id = new MyID(sc);
						break;
					case 15: // organization_descr
						cr.organization_descr = new String(sc);
						break;
					case 16: // sum_doc
						cr.sum_doc = Double.parseDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 21) {

			ContentValues cv = new ContentValues();
			cv.put("id", cr.id.m_id);
			cv.put("descr", cr.descr);
			cv.put("numdoc", cr.numdoc);
			cv.put("datedoc", cr.datedoc);
			cv.put("date_action", cr.date_action);
			cv.put("comment", cr.comment);
			cv.put("client_id", cr.client_id.toString());
			cv.put("client_descr", cr.client_descr);
			cv.put("agreement_id", cr.agreement_id.toString());
			cv.put("agreement_descr", cr.agreement_descr);
			cv.put("fio_descr", cr.fio_descr);
			cv.put("manager_id", cr.manager_id.toString());
			cv.put("manager_descr", cr.manager_descr);
			cv.put("organization_id", cr.organization_id.toString());
			cv.put("organization_descr", cr.organization_descr);
			cv.put("state", cr.state);
			cv.put("sum_doc", cr.sum_doc);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, values);
				bulk_idx = 0;
			}
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "VICARIOUS_POWER");
		cv.put("ver", myBase.m_vicarious_power_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadRoutes(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int routes_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null);
			contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null);
		}

		RouteRecord sr = new RouteRecord();
		RouteLineRecord lr = new RouteLineRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 3) {
					ContentValues cv = new ContentValues();
					cv.put("id", sr.id.m_id);
					cv.put("code", sr.code);
					cv.put("descr", sr.descr);
					cv.put("manager_id", sr.manager_id.m_id);

					Uri newUri = contentResolver.insert(MTradeContentProvider.ROUTES_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, "route_id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_routes_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_routes_version = -ver;
								contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null);
								contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_routes_version = ver;
						} else {
							myBase.m_routes_version = Integer.parseInt(sc);
						}
						routes_system_version = 0;
						break;
					case -108:
						routes_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						sr.id = new MyID(sc);
						lr.lineno = 0;
						break;
					case 1: // code
						sr.code = new String(sc);
						break;
					case 2: // descr
						sr.descr = new String(sc);
						break;
					case 3: // manager_id
						sr.manager_id = new MyID(sc);
						break;
					case 4:
					case 5:
					case 6:
					case 7:
						break;
					case 8:
						lr.lineno++;
						lr.distr_point_id = new MyID(sc);
						break;
					case 9: {
						lr.visit_time = new String(sc);

						ContentValues cv = new ContentValues();
						cv.put("route_id", sr.id.m_id);
						cv.put("lineno", lr.lineno);
						cv.put("distr_point_id", lr.distr_point_id.m_id);
						cv.put("visit_time", lr.visit_time);

						Uri newUri = contentResolver.insert(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, cv);

						nom_idx = 7;
						break;
					}
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 3) {
			ContentValues cv = new ContentValues();
			cv.put("id", sr.id.m_id);
			cv.put("code", sr.code);
			cv.put("descr", sr.descr);
			cv.put("manager_id", sr.manager_id.m_id);

			Uri newUri = contentResolver.insert(MTradeContentProvider.ROUTES_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "ROUTES");
		cv.put("ver", myBase.m_routes_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadRoutesDates(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int routes_dates_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null);
		}

		RouteDateRecord sr = new RouteDateRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 2) {
					ContentValues cv = new ContentValues();
					cv.put("route_id", sr.route_id.m_id);
					cv.put("route_date", sr.route_date);

					Uri newUri = contentResolver.insert(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				//contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				if (sc.length() == 4 + 36 + 8) {
					contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, "route_id=? and route_date=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 8)});
				} else if (sc.length() == 4 + 9 + 8) {
					contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, "route_id=? and route_date=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 8)});
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_routes_dates_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_routes_dates_version = -ver;
								contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_routes_dates_version = ver;
						} else {
							myBase.m_routes_dates_version = Integer.parseInt(sc);
						}
						routes_dates_system_version = 0;
						break;
					case -108:
						routes_dates_system_version = Integer.parseInt(sc);
						break;
					case 0: // route_id
						sr.route_id = new MyID(sc);
						break;
					case 1: // route_date
						sr.route_date = new String(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 2) {
			ContentValues cv = new ContentValues();
			cv.put("route_id", sr.route_id.m_id);
			cv.put("route_date", sr.route_date);

			Uri newUri = contentResolver.insert(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "ROUTES_DATES");
		cv.put("ver", myBase.m_routes_dates_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadMessages(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int messages_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.MESSAGES_CONTENT_URI, null, null);
		}

		MessageRecord mr = new MessageRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 8) {
					if (bUpdateMode) {
						Cursor cursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"ver", "acknowledged"}, "uid=?", new String[]{mr.uid.toString()}, null);
						if (cursor != null && cursor.moveToNext()) {
							int acknowledgedIndex = cursor.getColumnIndex("acknowledged");
							int verIndex = cursor.getColumnIndex("ver");
							int acknowledged = cursor.getInt(acknowledgedIndex);
							int ver = cursor.getInt(verIndex);
							// &16 == 0 сообщение не прочитано
							// &16 прочитано
							// прислали с флагом, что не прочитано, а у нас прочитано
							if (((mr.acknowledged & 4) == 0) && ((mr.acknowledged & 16) == 0))
								if (((acknowledged & 4) == 0) && ((acknowledged & 16) != 0))
									if (mr.ver == ver) {
										mr.acknowledged |= 16;
									}

						}
					}
	                /*
	                Клиент                Сервер
	                отправка 4   ->       обратный ответ 4+1
	                отправка 4   ->       обратный ответ 4+1
	                при получении ответа 4+1 удаляем у себя сообщение

	                Клиент                Сервер
	                             <-       отправка сообщения 0
	                обратно сообщение 0 ->
	                             <-       отправка сообщения 2
	                при получении 2 повторно не отправляем
	                */
					// подтверждено сервером, удалим это сообщение
					if ((mr.acknowledged & 1) != 0) {
						contentResolver.delete(MTradeContentProvider.MESSAGES_CONTENT_URI, "uid=?", new String[]{mr.uid.toString()});
					} else {
						ContentValues cv = new ContentValues();
						cv.put("uid", mr.uid.toString());
						cv.put("sender_id", mr.sender_id.toString());
						cv.put("receiver_id", mr.receiver_id.toString());
						cv.put("text", mr.text);
						cv.put("fname", mr.fname);
						cv.put("datetime", mr.datetime);
						cv.put("acknowledged", mr.acknowledged);
						cv.put("ver", mr.ver);

						contentResolver.insert(MTradeContentProvider.MESSAGES_CONTENT_URI, cv);
					}
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.MESSAGES_CONTENT_URI, "uid=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						// m_messages_version не используется
						messages_system_version = 0;
						break;
					case -108:
						messages_system_version = Integer.parseInt(sc);
						break;
					case 0: // uid
						mr.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						break;
					case 1: // acknowledged
						mr.acknowledged = Integer.parseInt(sc);
						break;
					case 2: // sender_id
						mr.sender_id = new MyID(sc);
						break;
					case 3: // receiver_id
						mr.receiver_id = new MyID(sc);
						break;
					case 4: // text
						mr.text = new String(sc);
						break;
					case 5: // fname
						mr.fname = new String(sc);
						break;
					case 6: // datetime
						mr.datetime = new String(sc);
						break;
					case 7:
						mr.ver = Integer.parseInt(sc);
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 8) {
			if (bUpdateMode) {
				Cursor cursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"ver", "acknowledged"}, "uid=?", new String[]{mr.uid.toString()}, null);
				if (cursor != null && cursor.moveToNext()) {
					int acknowledgedIndex = cursor.getColumnIndex("acknowledged");
					int verIndex = cursor.getColumnIndex("ver");
					int acknowledged = cursor.getInt(acknowledgedIndex);
					int ver = cursor.getInt(verIndex);
					// &16 == 0 сообщение не прочитано
					// &16 прочитано
					// прислали с флагом, что не прочитано, а у нас прочитано
					if (((mr.acknowledged & 4) == 0) && ((mr.acknowledged & 16) == 0))
						if (((acknowledged & 4) == 0) && ((acknowledged & 16) != 0))
							if (mr.ver == ver) {
								mr.acknowledged |= 16;
							}

				}
			}
			// подтверждено сервером, удалим это сообщение
			if ((mr.acknowledged & 1) != 0) {
				contentResolver.delete(MTradeContentProvider.MESSAGES_CONTENT_URI, "uid=?", new String[]{mr.uid.toString()});
			} else {
				ContentValues cv = new ContentValues();
				cv.put("uid", mr.uid.toString());
				cv.put("sender_id", mr.sender_id.toString());
				cv.put("receiver_id", mr.receiver_id.toString());
				cv.put("text", mr.text);
				cv.put("fname", mr.fname);
				cv.put("datetime", mr.datetime);
				cv.put("acknowledged", mr.acknowledged);
				cv.put("ver", mr.ver);

				contentResolver.insert(MTradeContentProvider.MESSAGES_CONTENT_URI, cv);
			}
		}

		return true;
	}

	static boolean LoadSimpleDiscounts(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int simple_discounts_system_version;
		int nom_idx;
		int pos;

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, null, null);
		}

		SimpleDiscountTypeRecord pr = new SimpleDiscountTypeRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					ContentValues cv = new ContentValues();
					cv.put("id", pr.id.m_id);
					cv.put("code", pr.code);
					cv.put("descr", pr.descr);
					cv.put("priceProcent", pr.priceProcent);

					Uri newUri = contentResolver.insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_simple_discounts_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_simple_discounts_version = -ver;
								contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_simple_discounts_version = ver;
						} else {
							myBase.m_simple_discounts_version = Integer.parseInt(sc);
						}
						simple_discounts_system_version = 0;
						break;
					case -108:
						simple_discounts_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						pr.id = new MyID(sc);
						break;
					case 1: // code
						pr.code = new String(sc);
						break;
					case 2: // descr
						pr.descr = new String(sc);
						break;
					case 3: // priceProcent
						pr.priceProcent = Common.MyStringToDouble(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 4) {
			ContentValues cv = new ContentValues();
			cv.put("id", pr.id.m_id);
			cv.put("code", pr.code);
			cv.put("descr", pr.descr);
			cv.put("priceProcent", pr.priceProcent);

			Uri newUri = contentResolver.insert(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, cv);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "SIMPLE_DISCOUNTS");
		cv.put("ver", myBase.m_simple_discounts_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}


	static boolean LoadEquipment(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int equipment_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, null, null);
		}

		EquipmentRecord er = new EquipmentRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 4) {
					er.descr = er.descr.replace("%", "0");

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", er.id.m_id);
					cv.put("code", er.code);
					cv.put("descr", er.descr);
					cv.put("flags", er.flags);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_CONTENT_URI, values);
						bulk_idx = 0;
					}
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, "id=?", new String[]{sc.substring(4)});
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_equipment_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_equipment_version = -ver;
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_equipment_version = ver;
						} else {
							myBase.m_equipment_version = Integer.parseInt(sc);
						}
						equipment_system_version = 0;
						break;
					case -108:
						equipment_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						er.id = new MyID(sc);
						break;
					case 1: // code
						er.code = new String(sc);
						break;
					case 2: // descr
						er.descr = new String(sc);
						break;
					case 3:    // Флаги
						er.flags = Integer.parseInt(sc);
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 4) {
			er.descr = er.descr.replace("%", "0");
			ContentValues cv = new ContentValues();
			cv.clear();
			cv.put("id", er.id.m_id);
			cv.put("code", er.code);
			cv.put("descr", er.descr);
			cv.put("flags", er.flags);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_CONTENT_URI, values);
				bulk_idx = 0;
			}
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.clear();
		cv.put("param", "EQUIPMENT");
		cv.put("ver", myBase.m_equipment_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	static boolean LoadEquipmentRests(ContentResolver contentResolver, MyDatabase myBase, String buf, boolean bUpdateMode) {
		int equipment_rests_system_version;
		int nom_idx;
		int pos;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (!bUpdateMode) {
			contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, null, null);
		}

		MySingleton g = MySingleton.getInstance();

		EquipmentRestsRecord er = new EquipmentRestsRecord();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 5) {
					ContentValues cv = new ContentValues();
					cv.put("client_id", er.client_id.m_id);
					cv.put("agreement_id", er.agreement_id.m_id);
					cv.put("nomenclature_id", er.nomenclature_id.m_id);
					cv.put("distr_point_id", er.distr_point_id.m_id);
					cv.put("doc_id", er.doc_id.m_id);
					cv.put("doc_descr", er.doc_descr);
					cv.put("date", er.date);
					cv.put("datepast", er.datepast);
					cv.put("quantity", er.quantity);
					cv.put("sum", er.sum);
					cv.put("flags", er.flags);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, values);
						bulk_idx = 0;
					}

					//Uri newUri = contentResolver.insert(MTradeContentProvider.RESTS_CONTENT_URI, cv);
				}
				nom_idx = 0;
			} else if (bUpdateMode && sc.length() > 4 && sc.substring(0, 4).equals("@@@#")) {
				// запись с указанным ID надо удалить
				if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) {
					switch (sc.length()) {
						case 4 + 36 * 4:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 36), sc.substring(4 + 36 + 36 + 36, 4 + 36 + 36 + 36 + 36)});
							break;
						case 4 + 9 * 4:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9), sc.substring(4 + 9 + 9, 4 + 9 + 9 + 9), sc.substring(4 + 9 + 9 + 9, 4 + 9 + 9 + 9 + 9)});
							break;
						case 4 + 36 * 3:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 36)});
							break;
						case 4 + 9 * 3:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9), sc.substring(4 + 9 + 9, 4 + 9 + 9 + 9)});
							break;
					}
				} else {
					switch (sc.length()) {
						case 4 + 73+36 * 3:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=? and doc_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 73), sc.substring(4 + 36 + 36 + 36, 4 + 36 + 36 + 36 + 73)});
							break;
						case 4 + 36 * 4:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=? and doc_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 36), sc.substring(4 + 36 + 36 + 36, 4 + 36 + 36 + 36 + 36)});
							break;
						case 4 + 9 * 4:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=? and doc_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9), sc.substring(4 + 9 + 9, 4 + 9 + 9 + 9), sc.substring(4 + 9 + 9 + 9, 4 + 9 + 9 + 9 + 9)});
							break;
						case 4 + 73+36 * 2:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 73), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 73)});
							break;
						case 4 + 36 * 3:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=?", new String[]{sc.substring(4, 4 + 36), sc.substring(4 + 36, 4 + 36 + 36), sc.substring(4 + 36 + 36, 4 + 36 + 36 + 36)});
							break;
						case 4 + 9 * 3:
							contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=?", new String[]{sc.substring(4, 4 + 9), sc.substring(4 + 9, 4 + 9 + 9), sc.substring(4 + 9 + 9, 4 + 9 + 9 + 9)});
							break;
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						if (bUpdateMode) {
							int ver = Integer.parseInt(sc);
							if (myBase.m_equipment_rests_version >= ver && ver >= 0) {
								// версия старая или текущая, не загружаем
								bCancelLoad = true;
								break;
							}
							if (ver < 0) {
								myBase.m_equipment_rests_version = -ver;
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, null, null);
								bUpdateMode = false;
							} else
								myBase.m_equipment_rests_version = ver;
						} else {
							myBase.m_equipment_rests_version = Integer.parseInt(sc);
						}
						equipment_rests_system_version = 0;
						break;
					case -108:
						equipment_rests_system_version = Integer.parseInt(sc);
						break;
					case 0: // client_id
						er.client_id = new MyID(sc);
						break;
					case 1: // nomenclature_id
						er.nomenclature_id = new MyID(sc);
						break;
					case 2: // distr_point_id
						er.distr_point_id = new MyID(sc);
						break;
					case 3: // quantity
						er.quantity = Double.parseDouble(sc);
						break;
					case 4: // sum
						er.sum = Double.parseDouble(sc);
						break;
					case 5: // flags
						er.flags = Integer.parseInt(sc);
						break;
					case 6: // agreement_id
						if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) {
							er.agreement_id = new MyID();
						} else {
							er.agreement_id = new MyID(sc);
						}
						break;
					case 7: // doc_id
						er.doc_id = new MyID(sc);
						break;
					case 8: // doc_descr
						er.doc_descr = new String(sc);
						break;
					case 9: // date
						er.date = new String(sc);
						break;
					case 10: // datepast
						er.datepast = new String(sc);
						break;
				}
				nom_idx++;
			}
		}
		if (nom_idx >= 5) {
			ContentValues cv = new ContentValues();
			cv.put("client_id", er.client_id.m_id);
			cv.put("agreement_id", er.agreement_id.m_id);
			cv.put("nomenclature_id", er.nomenclature_id.m_id);
			cv.put("distr_point_id", er.distr_point_id.m_id);
			cv.put("doc_id", er.doc_id.m_id);
			cv.put("doc_descr", er.doc_descr);
			cv.put("date", er.date);
			cv.put("datepast", er.datepast);
			cv.put("quantity", er.quantity);
			cv.put("sum", er.sum);
			cv.put("flags", er.flags);

			values[bulk_idx] = cv;
			bulk_idx++;
			if (bulk_idx >= BULK_SIZE) {
				contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, values);
				bulk_idx = 0;
			}
		}

		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, values_2);
		}

		ContentValues cv = new ContentValues();
		cv.put("param", "EQUIPMENT_RESTS");
		cv.put("ver", myBase.m_equipment_rests_version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);

		return true;
	}

	public enum XMLMode {
		E_MODE_INIT,
		E_MODE_UNKNOWN,
		E_MODE_NODE,
		E_MODE_MESSAGES,
		E_MODE_MESSAGE,
		E_MODE_MESSAGE_TEXT,
		E_MODE_MESSAGE_REPORT,
		// с 09.10.2020
		E_MODE_AGENTS,
		E_MODE_AGENT_RECORD,
		E_MODE_AGENT_TO_DELETE,

		E_MODE_AGREEMENTS,
		E_MODE_AGREEMENT_RECORD,
		E_MODE_AGREEMENT_TO_DELETE,

		E_MODE_CLIENTS,
		E_MODE_CLIENT_RECORD,
		E_MODE_CLIENT_TO_DELETE,

		E_MODE_CURATORS,
		E_MODE_CURATOR_RECORD,
		E_MODE_CURATOR_TO_DELETE,

		E_MODE_DEBTS,
		E_MODE_DEBT_RECORD,
		E_MODE_DEBT_TO_DELETE,

		E_MODE_DEBTS_EXT,
		E_MODE_DEBT_EXT_RECORD,
		E_MODE_DEBT_EXT_TO_DELETE,

		E_MODE_EQUIPMENT,
		E_MODE_EQUIPMENT_RECORD,
		E_MODE_EQUIPMENT_TO_DELETE,

		E_MODE_EQUIPMENT_RESTS,
		E_MODE_EQUIPMENT_REST_RECORD,
		E_MODE_EQUIPMENT_REST_TO_DELETE,

		E_MODE_NOMENCLATURE,
		E_MODE_NOMENCLATURE_RECORD,
		E_MODE_NOMENCLATURE_TO_DELETE,

		E_MODE_ORDERS_M,
		E_MODE_ORDERS_M_RECORD,
		E_MODE_ORDERS_M_LINE,

		E_MODE_ORGANIZATIONS,
		E_MODE_ORGANIZATION_RECORD,
		E_MODE_ORGANIZATION_TO_DELETE,

		E_MODE_PRICES,
		E_MODE_PRICE_RECORD,
		E_MODE_PRICE_TO_DELETE,

		E_MODE_PRICES_AGREEMENTS30,
		E_MODE_PRICES_AGREEMENTS30_RECORD,
		E_MODE_PRICES_AGREEMENTS30_TO_DELETE,

		E_MODE_PRICE_TYPES,
		E_MODE_PRICE_TYPE_RECORD,
		E_MODE_PRICE_TYPE_TO_DELETE,

		E_MODE_RESTS,
		E_MODE_REST_RECORD,
		E_MODE_REST_TO_DELETE,

		E_MODE_ROUTE_DATES,
		E_MODE_ROUTE_DATE_RECORD,
		E_MODE_ROUTE_DATE_TO_DELETE,

		E_MODE_ROUTES,
		E_MODE_ROUTE_RECORD,
		E_MODE_ROUTE_LINE,
		E_MODE_ROUTE_TO_DELETE,

		E_MODE_STOCKS,
		E_MODE_STOCK_RECORD,
		E_MODE_STOCK_TO_DELETE,

		E_MODE_SUM_SHIPPING_NOTIFICATIONS_M,
		E_MODE_SUM_SHIPPING_NOTIFICATIONS_M_RECORD,

		E_MODE_DISTR_POINTS,
		E_MODE_DISTR_POINT_RECORD,
		E_MODE_DISTR_POINT_TO_DELETE,

		E_MODE_DISTRIBS_CONTRACTS,
		E_MODE_DISTRIBS_CONTRACT_RECORD,
		E_MODE_DISTRIBS_CONTRACT_TO_DELETE,

		E_MODE_VICARIOUS_POWER,
		E_MODE_VICARIOUS_POWER_RECORD,
		E_MODE_VICARIOUS_POWER_TO_DELETE,

		E_MODE_PAYMENTS_M,
		E_MODE_PAYMENTS_M_RECORD,

		E_MODE_REFUNDS_M,
		E_MODE_REFUNDS_M_RECORD,
        E_MODE_REFUNDS_M_LINE,

		E_MODE_DISTRIBS_M,
		E_MODE_DISTRIBS_M_RECORD,
		E_MODE_DISTRIBS_M_LINE,

		E_MODE_VISIT_M,
		E_MODE_VISIT_M_RECORD,
		E_MODE_VISIT_M_LINE,

		E_MODE_GPS_M,
		E_MODE_GPS_M_RECORD,

		E_MODE_SETTINGS_M,

		E_MODE_SALES_HISTORY_HEADERS,
		E_MODE_SALES_HISTORY_HEADERS_RECORD

	}
	;

	static public class ResultLoadXML
	{
		boolean bSuccess;
		String ResultMessage;
		XMLMode xmlMode;
		ArrayList<UUID> uuids;
		int nResult;

		ResultLoadXML(){bSuccess=false;ResultMessage="";xmlMode=null;uuids=null;nResult=0;}
		ResultLoadXML(boolean bSuccess){this.bSuccess=bSuccess;ResultMessage="";xmlMode=null;uuids=null;nResult=0;}
		ResultLoadXML(boolean bSuccess, String ResultMessage){this.bSuccess=bSuccess;this.ResultMessage=ResultMessage;xmlMode=null;uuids=null;nResult=0;}
		ResultLoadXML(boolean bSuccess, XMLMode xmlMode){this.bSuccess=bSuccess;this.ResultMessage="";this.xmlMode=xmlMode;uuids=null;nResult=0;}
		ResultLoadXML(boolean bSuccess, XMLMode xmlMode, String ResultMessage){this.bSuccess=bSuccess;this.ResultMessage=ResultMessage;this.xmlMode=xmlMode;uuids=null;nResult=0;}
	}

	static ResultLoadXML LoadXML_Agents(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_AGENTS;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_agents_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false, XMLMode.E_MODE_AGENTS);
			}
			if (ver < 0) {
				myBase.m_agents_version = -ver;
				contentResolver.delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_agents_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.AGENTS_CONTENT_URI, null, null);
			myBase.m_agents_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int agents_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_AGENTS && name.equals("AgentToDelete")) {
					xmlmode = XMLMode.E_MODE_AGENT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.AGENTS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "agent_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_AGENTS && name.equals("Agent")) {
					xmlmode = XMLMode.E_MODE_AGENT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "agent_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));

					contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);

				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_AGENTS) {

					// Добавить агента "1C"
					Cursor emptyCursor = contentResolver.query(MTradeContentProvider.AGENTS_CONTENT_URI, new String[]{"_id"}, "id=?", new String[]{Constants.emptyID}, null);
					if (!emptyCursor.moveToFirst()) {
						ContentValues cv = new ContentValues();
						cv.put("id", Constants.emptyID);
						cv.put("code", "");
						cv.put("descr", "<1C>");

						Uri newUri = contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);
					}
					emptyCursor.close();


					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "AGENTS");
					cv.put("ver", myBase.m_agents_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все агенты
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// "Загружены агенты"
		return new ResultLoadXML(true, XMLMode.E_MODE_AGENTS, context.getString(R.string.message_agents_loaded));
	}

	static ResultLoadXML LoadXML_Agreements(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_AGREEMENTS;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_agreements_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false, XMLMode.E_MODE_AGREEMENTS);
			}
			if (ver < 0) {
				myBase.m_agreements_version = -ver;
				contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_agreements_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, null, null);
			myBase.m_agreements_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int agreements_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_AGREEMENTS && name.equals("AgreementToDelete")) {
					xmlmode = XMLMode.E_MODE_AGREEMENT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.AGREEMENTS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "agreement_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_AGREEMENTS && name.equals("Agreement")) {
					xmlmode = XMLMode.E_MODE_AGREEMENT_RECORD;

					//<Agreement agreement_id="d11fa43b-6390-11e6-8389-d05099932cdb"
					// owner_id="82975053-5b6b-11e5-a598-902b3448b42d"
					// code="000019198"
					// descr="Договор К"
					// price_type_id="bbdfdef6-9bdb-11e0-b6ff-0018f376ca92"
					// organization_id="169885ef-cb0f-11e0-9a06-0018f376ca92"
					// main_manager_id="70f1042b-0029-11e8-80d1-ac1f6b1542d1"/>

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "agreement_id"));
					cv.put("owner_id", xpp.getAttributeValue(null, "owner_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("price_type_id", xpp.getAttributeValue(null, "price_type_id"));
					cv.put("kredit_days", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "kredit_days"), "0")));
					cv.put("kredit_sum", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "kredit_sum"), "0.0")));
					cv.put("organization_id", xpp.getAttributeValue(null, "organization_id"));
					cv.put("default_manager_id", xpp.getAttributeValue(null, "main_manager_id"));
					cv.put("sale_id", StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "sale_id"), Constants.emptyID));
					cv.put("flags", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "flags"), "0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				}  else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_AGREEMENTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.AGREEMENTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "AGREEMENTS");
					cv.put("ver", myBase.m_agreements_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась вся номенклатура
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены договоры
		return new ResultLoadXML(true, XMLMode.E_MODE_AGREEMENTS, context.getString(R.string.message_agreements_loaded));
	}


	static ResultLoadXML LoadXML_Clients(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_CLIENTS;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_clients_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_clients_version = -ver;
				contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_clients_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, null, null);
			myBase.m_clients_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int clients_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_CLIENTS && name.equals("ClientToDelete")) {
					xmlmode = XMLMode.E_MODE_CLIENT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.CLIENTS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "client_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_CLIENTS && name.equals("Client")) {
					xmlmode = XMLMode.E_MODE_CLIENT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "client_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("isFolder", Integer.parseInt(xpp.getAttributeValue(null, "isFolder")));
					String parentTd=xpp.getAttributeValue(null, "parent_id");
					// 28.01.2021 Багфикс для тандема
					if (new MyID(parentTd).isEmpty())
						parentTd=Constants.emptyID;
					//
					cv.put("parent_id", parentTd);
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("descr_lower", xpp.getAttributeValue(null, "descr").toLowerCase(Locale.getDefault()));
					cv.put("descrFull", xpp.getAttributeValue(null, "descrFull"));
					cv.put("address", xpp.getAttributeValue(null, "legal_address"));
					cv.put("address2", xpp.getAttributeValue(null, "fact_address"));
					cv.put("comment", xpp.getAttributeValue(null, "comment"));
					cv.put("curator_id", xpp.getAttributeValue(null, "curator_id"));
					cv.put("priceType", xpp.getAttributeValue(null, "priceType")); // Мега
					cv.put("blocked", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "blocked"), "0")));
					cv.put("flags", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "flags"), "0")));
					cv.put("email_for_cheques", xpp.getAttributeValue(null, "email_for_cheques"));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_CLIENTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.CLIENTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "CLIENTS");
					cv.put("ver", myBase.m_clients_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закрылся последний уровень
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены клиенты
		return new ResultLoadXML(true, XMLMode.E_MODE_CLIENTS, context.getString(R.string.message_clients_loaded));
	}

	static ResultLoadXML LoadXML_Curators(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_CURATORS;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_curators_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_curators_version = -ver;
				contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_curators_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, null, null);
			myBase.m_curators_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int curators_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_CURATORS && name.equals("CuratorToDelete")) {
					xmlmode = XMLMode.E_MODE_CURATOR_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.CURATORS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "agent_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_CURATORS && name.equals("Curator")) {
					xmlmode = XMLMode.E_MODE_CURATOR_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "curator_id"));
					cv.put("isFolder", Integer.parseInt(xpp.getAttributeValue(null, "isFolder")));
					String parentTd=xpp.getAttributeValue(null, "parent_id");
					// 28.01.2021 Багфикс для тандема
					if (new MyID(parentTd).isEmpty())
						parentTd=Constants.emptyID;
					//
					cv.put("parent_id", parentTd);
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));

					contentResolver.insert(MTradeContentProvider.CURATORS_CONTENT_URI, cv);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_CURATORS) {

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "CURATORS");
					cv.put("ver", myBase.m_curators_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}


				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		return new ResultLoadXML(true, XMLMode.E_MODE_CURATORS, context.getString(R.string.message_curators_loaded));
	}

	static ResultLoadXML LoadXML_Debts(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_DEBTS;

		final int BULK_SIZE = 1000;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_saldo_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_saldo_version = -ver;
				contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_saldo_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, null, null);
			myBase.m_saldo_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int debts_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_DEBTS && name.equals("DebtToDelete")) {
					xmlmode = XMLMode.E_MODE_DEBT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.SALDO_CONTENT_URI, "client_id=?", new String[]{xpp.getAttributeValue(null, "client_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_DEBTS && name.equals("DebtsRecord")) {
					xmlmode = XMLMode.E_MODE_DEBT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("client_id", xpp.getAttributeValue(null, "client_id"));
					cv.put("saldo", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "saldo"), "0.0")));
					cv.put("saldo_past", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "saldo_past"), "0.0")));
					cv.put("saldo_past30", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "saldo_past30"), "0.0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_DEBTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "SALDO");
					cv.put("ver", myBase.m_saldo_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все долги
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены долги
		return new ResultLoadXML(true, XMLMode.E_MODE_DEBTS, context.getString(R.string.message_debts_loaded));
	}


	static ResultLoadXML LoadXML_DebtsExt(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException, OperationApplicationException, RemoteException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_DEBTS_EXT;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		int bulk_delete_idx = 0;
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_saldo_extended_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_saldo_extended_version = -ver;
				contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_saldo_extended_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, null, null);
			myBase.m_saldo_extended_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int debts_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_DEBTS_EXT && name.equals("DebtExtToDelete")) {
					xmlmode = XMLMode.E_MODE_DEBT_EXT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						if (xpp.getAttributeValue(null, "agreement_id")==null) {
							// Прайм
							contentResolver.delete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, "client_id=?", new String[]{xpp.getAttributeValue(null, "client_id")});
						} else {
							// Продлидер
							ContentProviderOperation operation = ContentProviderOperation
									.newDelete(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI)
									.withSelection("client_id=? and agreement_id=? and document_id=? and manager_id=?", new String[]{xpp.getAttributeValue(null, "client_id"), xpp.getAttributeValue(null, "agreement_id"), xpp.getAttributeValue(null, "document_id"), xpp.getAttributeValue(null, "manager_id")})
									.build();
							operations.add(operation);
							bulk_delete_idx++;
							if (bulk_delete_idx >= BULK_SIZE) {
								contentResolver.applyBatch(MTradeContentProvider.AUTHORITY, operations);
								bulk_delete_idx = 0;
							}
						}
					}
				} else if (xmlmode == XMLMode.E_MODE_DEBTS_EXT && name.equals("DebtsExtRecord")) {
					xmlmode = XMLMode.E_MODE_DEBT_EXT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("client_id", xpp.getAttributeValue(null, "client_id"));
					// тут может быть любое значение, главное, чтобы не NULL, в противном случае условие ключа перестает работать и записи дублируются при добавлении
					cv.put("agreement30_id", StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "agreement30_id"), "0"));
					cv.put("agreement_id", xpp.getAttributeValue(null, "agreement_id"));
					cv.put("document_id", xpp.getAttributeValue(null, "document_id"));
					cv.put("manager_id", xpp.getAttributeValue(null, "manager_id"));
					cv.put("document_descr", xpp.getAttributeValue(null, "document_descr"));
					cv.put("document_datetime", xpp.getAttributeValue(null, "document_datetime"));
					cv.put("manager_descr", xpp.getAttributeValue(null, "manager_descr"));
					cv.put("agreement_descr", xpp.getAttributeValue(null, "agreement_descr"));
					cv.put("organization_descr", xpp.getAttributeValue(null, "agreement_org_descr"));
					cv.put("saldo", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "saldo"), "0.0")));
					cv.put("saldo_past", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "saldo_past"), "0.0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_DEBTS_EXT) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, values_2);
					}

					if (bulk_delete_idx > 0) {
						contentResolver.applyBatch(MTradeContentProvider.AUTHORITY, operations);
					}


					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "SALDO_EXT");
					cv.put("ver", myBase.m_saldo_extended_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все долги
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены долги расширенные
		return new ResultLoadXML(true, XMLMode.E_MODE_DEBTS_EXT, context.getString(R.string.message_debts_by_docs_loaded));

	}

	static ResultLoadXML LoadXML_Equipment(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_EQUIPMENT;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_equipment_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_equipment_version = -ver;
				contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_equipment_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, null, null);
			myBase.m_nomenclature_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int equipment_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_EQUIPMENT && name.equals("EquipmentToDelete")) {
					xmlmode = XMLMode.E_MODE_EQUIPMENT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.EQUIPMENT_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "equipment_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_EQUIPMENT && name.equals("EquipmentRecord")) {
					xmlmode = XMLMode.E_MODE_EQUIPMENT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "equipment_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("flags", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "flags"), "0")));

					contentResolver.insert(MTradeContentProvider.EQUIPMENT_CONTENT_URI, cv);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_EQUIPMENT) {

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "EQUIPMENT");
					cv.put("ver", myBase.m_equipment_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружено оборудование
		return new ResultLoadXML(true, XMLMode.E_MODE_EQUIPMENT, context.getString(R.string.message_equipment_updates_loaded));
	}

	static ResultLoadXML LoadXML_EquipmentRests(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_EQUIPMENT_RESTS;

		MySingleton g = MySingleton.getInstance();

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_equipment_rests_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_equipment_rests_version = -ver;
				contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_equipment_rests_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, null, null);
			myBase.m_equipment_rests_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int equipment_rests_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_EQUIPMENT_RESTS && name.equals("EquipmentRestsToDelete")) {
					xmlmode = XMLMode.E_MODE_EQUIPMENT_REST_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY)
						{
							if (xpp.getAttributeValue(null, "doc_id")!=null)
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?", new String[]{xpp.getAttributeValue(null, "client_id"), xpp.getAttributeValue(null, "nomenclature_id"), xpp.getAttributeValue(null, "distr_point_id"), xpp.getAttributeValue(null, "doc_id")});
							else
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and nomenclature_id=? and distr_point_id=?", new String[]{xpp.getAttributeValue(null, "client_id"), xpp.getAttributeValue(null, "nomenclature_id"), xpp.getAttributeValue(null, "distr_point_id")});
						} else
						{
							if (xpp.getAttributeValue(null, "doc_id")!=null)
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=? and doc_id=?", new String[]{xpp.getAttributeValue(null, "client_id"), xpp.getAttributeValue(null, "agreement_id"), xpp.getAttributeValue(null, "nomenclature_id"), xpp.getAttributeValue(null, "doc_id")});
							else
								contentResolver.delete(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, "client_id=? and agreement_id=? and nomenclature_id=?", new String[]{xpp.getAttributeValue(null, "client_id"), xpp.getAttributeValue(null, "agreement_id"), xpp.getAttributeValue(null, "nomenclature_id")});
						}
					}
				} else if (xmlmode == XMLMode.E_MODE_EQUIPMENT_RESTS && name.equals("EquipmentRestsRecord")) {
					xmlmode = XMLMode.E_MODE_EQUIPMENT_REST_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("client_id", xpp.getAttributeValue(null, "client_id"));
					cv.put("nomenclature_id", xpp.getAttributeValue(null, "nomenclature_id"));
					cv.put("distr_point_id", xpp.getAttributeValue(null, "distr_point_id"));

					cv.put("quantity", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "quantity"), "0.0")));
					cv.put("sum", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "sum"), "0.0")));
					cv.put("flags", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "flags"), "0")));

					// Второй вариант - смотреть, есть ли это поле, но так сделано для лучшего понимания формата данных
					if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.ISTART || g.Common.FACTORY) {
						cv.put("agreement_id", Constants.emptyID);
					} else {
						cv.put("agreement_id", xpp.getAttributeValue(null, "agreement_id"));
					}

					cv.put("doc_id", xpp.getAttributeValue(null, "doc_id"));
					cv.put("doc_descr", xpp.getAttributeValue(null, "doc_descr"));
					cv.put("date", xpp.getAttributeValue(null, "date"));
					cv.put("datepast", xpp.getAttributeValue(null, "datepast"));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_EQUIPMENT_RESTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.EQUIPMENT_RESTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "EQUIPMENT_RESTS");
					cv.put("ver", myBase.m_equipment_rests_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все остатки
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены остатки оборудования
		return new ResultLoadXML(true, XMLMode.E_MODE_EQUIPMENT_RESTS, context.getString(R.string.message_equipment_rests_updates_loaded));
	}


	static ResultLoadXML LoadXML_Nomenclature(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_NOMENCLATURE;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_nomenclature_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_nomenclature_version = -ver;
				contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_nomenclature_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, null, null);
			myBase.m_nomenclature_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int nomenclature_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_NOMENCLATURE && name.equals("NomenclatureToDelete")) {
					xmlmode = XMLMode.E_MODE_NOMENCLATURE_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "nomenclature_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_NOMENCLATURE && name.equals("NomenclatureRecord")) {
					xmlmode = XMLMode.E_MODE_NOMENCLATURE_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "nomenclature_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("isFolder", Integer.parseInt(xpp.getAttributeValue(null, "isFolder")));
					String parentTd=xpp.getAttributeValue(null, "parent_id");
					// 28.01.2021 Багфикс для тандема
					if (new MyID(parentTd).isEmpty())
						parentTd=Constants.emptyID;
					//
					cv.put("parent_id", parentTd);
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("descr_lower", xpp.getAttributeValue(null, "descr").toLowerCase(Locale.getDefault()));
					cv.put("descrFull", xpp.getAttributeValue(null, "descrFull"));
					cv.put("quant_1", xpp.getAttributeValue(null, "base_ed_descr"));
					cv.put("quant_2", xpp.getAttributeValue(null, "pack_ed_descr"));
					cv.put("edizm_1_id", xpp.getAttributeValue(null, "base_ed_id"));
					cv.put("edizm_2_id", xpp.getAttributeValue(null, "pack_ed_id"));
					cv.put("quant_k_1", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "base_ed_k"), "0.0")));
					cv.put("quant_k_2", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "pack_ed_k"), "0.0")));
					cv.put("opt_price", 0.0);
					cv.put("m_opt_price", 0.0);
					cv.put("rozn_price", 0.0);
					cv.put("incom_price", 0.0);
					cv.put("IsInPrice", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "IsInPrice"), "0")));
					cv.put("flagWithoutDiscont", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "edit_price"), "0")));
					cv.put("weight_k_1", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "weight"), "0.0")));
					cv.put("weight_k_2", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "weight_pack"), "0.0")));
					cv.put("min_quantity",  Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "min_quantity"), "0.0")));
					cv.put("multiplicity",  Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "k_quantity"), "0.0")));
					cv.put("required_sales", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "required_sales"), "0.0")));
					cv.put("flags", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "flags"), "0")));
					cv.put("order_for_sorting", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "order_for_sorting"), "-1")));
					cv.put("group_of_analogs", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "group_of_analogs"), "-1")));
					cv.put("nomenclature_color", Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "color"), "0")));
					// с 06.02.2019
					// после загрузки номенклатуры все равно происходит сканирование каталога с фото
					cv.put("image_file", "");
					cv.put("image_file_checksum", 0);
					cv.put("image_width", 0);
					cv.put("image_height", 0);
					cv.put("image_file_size", 0);
					cv.put("compose_with", StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "compose_with"), Constants.emptyID));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values);
						bulk_idx = 0;
					}
				}  else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_NOMENCLATURE) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "NOMENCLATURE");
					cv.put("ver", myBase.m_nomenclature_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась вся номенклатура
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружена номенклатура
		return new ResultLoadXML(true, XMLMode.E_MODE_NOMENCLATURE, context.getString(R.string.message_nomenclature_loaded));
	}

	static ResultLoadXML LoadXML_OrdersUpdate(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_ORDERS_M;

		boolean bRecordValid = false;
		OrderRecord or=new OrderRecord();
		OrderLineRecord line = new OrderLineRecord();

		int orders_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_ORDERS_M && name.equals("OrderM")) {
					xmlmode = XMLMode.E_MODE_ORDERS_M_RECORD;

					/*
					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "agent_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					contentResolver.insert(MTradeContentProvider.AGENTS_CONTENT_URI, cv);
					 */

					bRecordValid = false;

					or.uid = UUID.fromString(xpp.getAttributeValue(null, "order_uid").replace("{", "").replace("}", ""));
					Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=? and editing_backup=0", new String[]{or.uid.toString()}, "datedoc desc");
					if (cursor.moveToNext()) {
						long _id = cursor.getLong(0);
						//String stringId=cursor.getString(0);
						//int rowsUpdated = contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=?", new String[]{stringId});
						//if (rowsUpdated>0)
						//	bOrderUpdated=true;
						if (ReadOrderBy_Id(contentResolver, or, _id)) {
							bRecordValid = true;
							// обнулим текущее количество в документе
							for (OrderLineRecord it : or.lines) {
								it.temp_quantity = it.quantity;
								it.quantity = 0.0;
							}
						}
					}
					cursor.close();

					or.id=new MyID(xpp.getAttributeValue(null, "order_id"));
					or.numdoc=xpp.getAttributeValue(null, "numdoc");
					// Factory
					if (xpp.getAttributeValue(null, "datedoc")!=null)
						or.datedoc=xpp.getAttributeValue(null, "datedoc");
					if (xpp.getAttributeValue(null, "shipping_date")!=null)
						or.shipping_date=xpp.getAttributeValue(null, "shipping_date");
					//
					or.version=Integer.parseInt(xpp.getAttributeValue(null, "version"));
					or.versionPDA=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA"));
					or.version_ack=Integer.parseInt(xpp.getAttributeValue(null, "version_ack"));
					or.versionPDA_ack=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA_ack"));
					or.sumShipping=Double.parseDouble(xpp.getAttributeValue(null, "sumShipping"));

					int flags=Integer.parseInt(xpp.getAttributeValue(null, "flags"));
					// closed_mark и т.п., в КПК не все используется
					or.closed_not_full=(flags & 4) != 0 ? 1 : 0;
					String state=xpp.getAttributeValue(null, "state");
					or.state = E_ORDER_STATE.E_ORDER_STATE_CREATED;
					if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_LOADED, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_LOADED;
					else if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED;
					else if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_COMPLETED, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_COMPLETED;
					else if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL;
					else if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_CANCELED, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_CANCELED;
					else if (state.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT, null)))
						or.state = E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT;
					else
						or.state = E_ORDER_STATE.E_ORDER_STATE_UNKNOWN;

					or.comment_closing=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment_closing"), "");
					or.comment_payment=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment_payment"), "");

					or.agreement_id=new MyID(xpp.getAttributeValue(null, "agreement_id"));
					or.agreement30_id=new MyID(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "agreement30_id"), Constants.emptyID));

					or.distr_point_id=new MyID(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "distr_point_id"), Constants.emptyID));

				} else
				if (xmlmode == XMLMode.E_MODE_ORDERS_M_RECORD && name.equals("Line")) {
					xmlmode = XMLMode.E_MODE_ORDERS_M_LINE;

					line.nomenclature_id = new MyID(xpp.getAttributeValue(null, "nomenclature_id"));
					line.shipping_time = "";
					// заляп от 15.07.2020 для теста
					// проблема такая - если делается скидка из 1С, то вес не считается
					Cursor nomenclatureCursor = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"descr", "weight_k_1", "flags"}, "id=?", new String[]{line.nomenclature_id.toString()}, null);
					if (nomenclatureCursor.moveToFirst()) {
						line.stuff_nomenclature = nomenclatureCursor.getString(0);
						line.stuff_weight_k_1 = nomenclatureCursor.getDouble(1);
						line.stuff_nomenclature_flags = nomenclatureCursor.getInt(2);
					} else {
						// Хотя этого не произойдет
						line.stuff_nomenclature = "";
						line.stuff_weight_k_1 = 1.0;
						line.stuff_nomenclature_flags = 0;
					}
					nomenclatureCursor.close();
					//
					line.quantity = Double.parseDouble(xpp.getAttributeValue(null, "quantity"));
					line.quantity_requested = 0.0;
					line.k = Double.parseDouble(xpp.getAttributeValue(null, "k"));
					line.ed = xpp.getAttributeValue(null, "ed_descr");
					line.total = Double.parseDouble(xpp.getAttributeValue(null, "sum"));
					line.price = Double.parseDouble(xpp.getAttributeValue(null, "price"));
					line.discount = Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "discount"), "0.0"));
					// найдем максимально похожую строку в документе
					OrderLineRecord matchNomenclature = null;
					OrderLineRecord matchQuantity = null;
					OrderLineRecord matchAll = null;
					for (OrderLineRecord search : or.lines) {
						if (search.nomenclature_id.equals(line.nomenclature_id)) {
							if (matchQuantity == null) {
								// если совпадения лучше нет, то запишем это
								matchNomenclature = search;
								// количество совпало и эта строка еще не используется
								if (Math.abs(search.temp_quantity * search.k - line.quantity * line.k) < 0.0001 && search.quantity == 0.0) {
									matchQuantity = search;
									// но полное совпадение еще точно не было найдено, т.к. в этом случае цикл прерывается
									if (search.total == line.total) {
										// нашли полное совпадение
										matchAll = search;
										break;
									}
								}
							}
						}
					}
					if (matchAll != null) {
						// возвращаем количество, которое было - ничего не изменилось
						matchAll.quantity = matchAll.temp_quantity;
					} else if (matchQuantity != null) {
						// совпало количество, цена не совпала
						// заменяем всю строку целиком
						matchQuantity.quantity = line.quantity;
						matchQuantity.price = line.price;
						matchQuantity.total = line.total;
						matchQuantity.discount = line.discount;
						matchQuantity.k = line.k;
						matchQuantity.ed = line.ed;
						matchQuantity.stuff_nomenclature = line.stuff_nomenclature;
						matchQuantity.stuff_weight_k_1 = line.stuff_weight_k_1;

					} else if (matchNomenclature != null) {
						// совпала только номенклатура
						if (matchNomenclature.quantity == 0.0) {
							// если изменилась единица измерения, пересчитаем требуемое количество
							if (Math.abs(matchNomenclature.k - line.k) > 0.0001 && line.k > 0.0001) {
								matchNomenclature.quantity_requested = Math.floor(matchNomenclature.quantity_requested * matchNomenclature.k / line.k * 100 + 0.000001) / 100.0;
							}
							// нигде еще не используется, также заменяем полностью
							matchNomenclature.quantity = line.quantity;
							matchNomenclature.price = line.price;
							matchNomenclature.total = line.total;
							matchNomenclature.discount = line.discount;
							matchNomenclature.k = line.k;
							matchNomenclature.ed = line.ed;
							matchNomenclature.stuff_nomenclature = line.stuff_nomenclature;
							matchNomenclature.stuff_weight_k_1 = line.stuff_weight_k_1;
						} else {
							// Номенклатура будет 2 раза в документе
							or.lines.add(line.clone());
							// в данном случае quantity_requested=0.0
						}
					} else {
						// Это новый товар
						or.lines.add(line.clone());
						// в данном случае quantity_requested=0.0
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_ORDERS_M_RECORD) {

					if (bRecordValid) {
						int _id = 0;
						or.sumDoc = or.GetOrderSum(null, false);
						or.weightDoc = TextDatabase.GetOrderWeight(contentResolver, or, null, false);
						or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
						SaveOrderSQL(contentResolver, or, _id, 0);
						uids.add(or.uid);
					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_orders.put(or.uid, or.version);
					}

				}


				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены обновления заказов
		return new ResultLoadXML(true, XMLMode.E_MODE_ORDERS_M, context.getString(R.string.message_orders_updates_loaded));
	}


	static ResultLoadXML LoadXML_PaymentsUpdate(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_PAYMENTS_M;

		boolean bRecordValid = false;
		CashPaymentRecord or=new CashPaymentRecord();

		int payments_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_PAYMENTS_M && name.equals("PaymentRecord")) {
					xmlmode = XMLMode.E_MODE_PAYMENTS_M_RECORD;

					bRecordValid = false;

					or.uid = UUID.fromString(xpp.getAttributeValue(null, "payment_uid").replace("{", "").replace("}", ""));
					Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
					if (cursor.moveToNext()) {
						long _id = cursor.getLong(0);
						if (ReadPaymentBy_Id(contentResolver, or, _id)) {
							bRecordValid = true;
						}
					}
					cursor.close();

					or.id=new MyID(xpp.getAttributeValue(null, "payment_id"));
					or.numdoc=xpp.getAttributeValue(null, "numdoc");
					String datedoc=xpp.getAttributeValue(null, "datedoc");
					if (datedoc.length()==14)
						or.datedoc=datedoc;
					or.version=Integer.parseInt(xpp.getAttributeValue(null, "version"));
					or.versionPDA=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA"));
					or.version_ack=Integer.parseInt(xpp.getAttributeValue(null, "version_ack"));
					or.versionPDA_ack=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA_ack"));

					String state=xpp.getAttributeValue(null, "state");

					or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED;
					if (state.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_LOADED, null)))
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_LOADED;
					else if (state.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_ACKNOWLEDGED, null)))
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_ACKNOWLEDGED;
					else if (state.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_COMPLETED, null)))
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_COMPLETED;
					else if (state.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL, null)))
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL;
					else if (state.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED, null)))
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED;
					else
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_UNKNOWN;

					or.comment=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment"), "");
					or.comment_closing=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment_closing"), "");

					or.client_id=new MyID(xpp.getAttributeValue(null, "client_id"));
					or.agreement_id=new MyID(xpp.getAttributeValue(null, "agreement_id"));
					or.manager_id=new MyID(xpp.getAttributeValue(null, "manager_id"));
					or.sumDoc=Double.parseDouble(xpp.getAttributeValue(null, "sumDoc"));
					or.stuff_manager_name=xpp.getAttributeValue(null, "stuff_manager_name");
					or.stuff_organization_name=xpp.getAttributeValue(null, "stuff_organization_name");

					or.vicarious_power_id=new MyID(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "vicarious_power_id"), Constants.emptyID));
					or.vicarious_power_descr=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "vicarious_power_descr"), "");

					// Не используется
					//or.distr_point_id=new MyID(xpp.getAttributeValue(null, "distr_point_id"));
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_PAYMENTS_M_RECORD) {

					if (bRecordValid) {
						int _id = 0;
						SavePaymentSQL(contentResolver, myBase, or, _id);
						uids.add(or.uid);
					} else {
						// такого документа уже нет, сообщаем об этом
						myBase.m_empty_payments.put(or.uid, or.version);
					}

				}
				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены обновления платежей
		return new ResultLoadXML(true, XMLMode.E_MODE_PAYMENTS_M, context.getString(R.string.message_payments_updates_loaded));
	}

	static ResultLoadXML LoadXML_RefundsUpdate(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_REFUNDS_M;

		boolean bRecordValid = false;
		RefundRecord or = new RefundRecord();
		RefundLineRecord line = new RefundLineRecord();

		int refunds_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_REFUNDS_M && name.equals("RefundRecord")) {
					xmlmode = XMLMode.E_MODE_REFUNDS_M_RECORD;

					bRecordValid = false;

					or.uid = UUID.fromString(xpp.getAttributeValue(null, "refund_uid").replace("{", "").replace("}", ""));
					Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
					if (cursor.moveToNext()) {
						long _id = cursor.getLong(0);
						if (ReadRefundBy_Id(contentResolver, or, _id)) {
							bRecordValid = true;
							// обнулим текущее количество в документе
							for (RefundLineRecord it : or.lines) {
								it.temp_quantity = it.quantity;
								it.quantity = 0.0;
							}
						}
					}
					cursor.close();

					or.id=new MyID(xpp.getAttributeValue(null, "refund_id"));
					or.numdoc=xpp.getAttributeValue(null, "numdoc");
					//String datedoc=xpp.getAttributeValue(null, "datedoc");
					//if (datedoc.length()==14)
					//	or.datedoc=datedoc;
					or.version=Integer.parseInt(xpp.getAttributeValue(null, "version"));
					or.versionPDA=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA"));
					or.version_ack=Integer.parseInt(xpp.getAttributeValue(null, "version_ack"));
					or.versionPDA_ack=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA_ack"));

					int flags=Integer.parseInt(xpp.getAttributeValue(null, "flags"));
					// closed_mark и т.п., в КПК не все используется
					or.closed_not_full=(flags & 4) != 0 ? 1 : 0;

					or.shipping_type = Integer.parseInt(xpp.getAttributeValue(null, "shipping_type"));

					String state=xpp.getAttributeValue(null, "state");

					or.state = E_REFUND_STATE.E_REFUND_STATE_CREATED;
					if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_LOADED, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_LOADED;
					else if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED;
					else if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_COMPLETED, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_COMPLETED;
					else if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL;
					else if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_CANCELED, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_CANCELED;
					else if (state.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT, null)))
						or.state = E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT;
					else
						or.state = E_REFUND_STATE.E_REFUND_STATE_UNKNOWN;


					//or.comment=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment"), "");
					or.comment_closing=StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "comment_closing"), "");

					or.agreement_id=new MyID(xpp.getAttributeValue(null, "agreement_id"));
					or.distr_point_id=new MyID(xpp.getAttributeValue(null, "distr_point_id"));

				} else if (xmlmode == XMLMode.E_MODE_REFUNDS_M_RECORD && name.equals("LineRecord")) {
					xmlmode = XMLMode.E_MODE_REFUNDS_M_LINE;

					line.nomenclature_id = new MyID(xpp.getAttributeValue(null, "nomenclature_id"));
					line.quantity = Double.parseDouble(xpp.getAttributeValue(null, "quantity"));
					line.quantity_requested = 0.0;
					line.k = Double.parseDouble(xpp.getAttributeValue(null, "k"));
					line.ed = xpp.getAttributeValue(null, "ed");

					// найдем максимально похожую строку в документе
					RefundLineRecord matchNomenclature = null;
					RefundLineRecord matchQuantity = null;
					RefundLineRecord matchAll = null;
					for (RefundLineRecord search : or.lines) {
						if (search.nomenclature_id.equals(line.nomenclature_id)) {
							if (matchQuantity == null) {
								// если совпадения лучше нет, то запишем это
								matchNomenclature = search;
								// количество совпало и эта строка еще не используется
								if (Math.abs(search.temp_quantity * search.k - line.quantity * line.k) < 0.0001 && search.quantity == 0.0) {
									matchQuantity = search;
									// но полное совпадение еще точно не было найдено, т.к. в этом случае цикл прерывается
									//if (search.total==line.total)
									//{
									//	// нашли полное совпадение
									matchAll = search;
									break;
									//}
								}
							}
						}
					}
					if (matchAll != null) {
						// возвращаем количество, которое было - ничего не изменилось
						matchAll.quantity = matchAll.temp_quantity;
					} else if (matchQuantity != null) {
						// совпало количество, цена не совпала
						// заменяем всю строку целиком
						matchQuantity.quantity = line.quantity;
						//matchQuantity.price=line.price;
						//matchQuantity.total=line.total;
						//matchQuantity.discount=line.discount;
						matchQuantity.k = line.k;
						matchQuantity.ed = line.ed;
						matchQuantity.stuff_nomenclature = line.stuff_nomenclature;
						matchQuantity.stuff_weight_k_1 = line.stuff_weight_k_1;
					} else if (matchNomenclature != null) {
						// совпала только номенклатура
						if (matchNomenclature.quantity == 0.0) {
							// если изменилась единица измерения, пересчитаем требуемое количество
							if (Math.abs(matchNomenclature.k - line.k) > 0.0001 && line.k > 0.0001) {
								matchNomenclature.quantity_requested = Math.floor(matchNomenclature.quantity_requested * matchNomenclature.k / line.k * 100 + 0.000001) / 100.0;
							}
							// нигде еще не используется, также заменяем полностью
							matchNomenclature.quantity = line.quantity;
							//matchNomenclature.price=line.price;
							//matchNomenclature.total=line.total;
							//matchNomenclature.discount=line.discount;
							matchNomenclature.k = line.k;
							matchNomenclature.ed = line.ed;
							matchNomenclature.stuff_nomenclature = line.stuff_nomenclature;
							matchNomenclature.stuff_weight_k_1 = line.stuff_weight_k_1;
						} else {
							// Номенклатура будет 2 раза в документе
							or.lines.add(line.clone());
							// в данном случае quantity_requested=0.0
						}
					} else {
						// Это новый товар
						or.lines.add(line.clone());
						// в данном случае quantity_requested=0.0
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_REFUNDS_M_RECORD) {

					if (bRecordValid) {
						int _id = 0;
						or.weightDoc = TextDatabase.GetRefundWeight(contentResolver, or, null, false);
						or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
						SaveRefundSQL(contentResolver, or, _id);
						uids.add(or.uid);
					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_refunds.put(or.uid, or.version);
					}

				}
				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены обновления возвратов
		return new ResultLoadXML(true, XMLMode.E_MODE_REFUNDS_M, context.getString(R.string.message_refunds_updates_loaded));
	}

	static ResultLoadXML LoadXML_DistribsUpdate(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_DISTRIBS_M;

		boolean bRecordValid = false;
		DistribsRecord or = new DistribsRecord();
		DistribsLineRecord line = new DistribsLineRecord();

		int distribs_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_DISTRIBS_M && name.equals("DistribsRecord")) {
					xmlmode = XMLMode.E_MODE_DISTRIBS_M_RECORD;

					bRecordValid = false;

					or.uid = UUID.fromString(xpp.getAttributeValue(null, "distribs_uid").replace("{", "").replace("}", ""));
					Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
					if (cursor.moveToNext()) {
						long _id = cursor.getLong(0);
						if (ReadDistribsBy_Id(contentResolver, or, _id)) {
							bRecordValid = true;
							// 27.01.2017
							// и удаляем старые строки
							or.lines.clear();
							//
						}
					}
					cursor.close();

					or.id=new MyID(xpp.getAttributeValue(null, "distribs_id"));
					or.numdoc=xpp.getAttributeValue(null, "numdoc");
					String datedoc=xpp.getAttributeValue(null, "datedoc");
					if (datedoc.length()==14)
						or.datedoc=datedoc;

					or.client_id=new MyID(xpp.getAttributeValue(null, "client_id"));
					or.distr_point_id=new MyID(xpp.getAttributeValue(null, "distr_point_id"));
					or.curator_id=new MyID(xpp.getAttributeValue(null, "curator_id"));
					or.comment=xpp.getAttributeValue(null, "comment");

					or.version=Integer.parseInt(xpp.getAttributeValue(null, "version"));
					or.versionPDA=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA"));
					or.version_ack=Integer.parseInt(xpp.getAttributeValue(null, "version_ack"));
					or.versionPDA_ack=Integer.parseInt(xpp.getAttributeValue(null, "versionPDA_ack"));

					String state=xpp.getAttributeValue(null, "state");

					or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED;
					if (state.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED, null)))
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED;
					else if (state.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED, null)))
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED;
					else if (state.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED, null)))
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED;
					else if (state.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL, null)))
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL;
					else if (state.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED, null)))
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED;
					else
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN;


				} else if (xmlmode == XMLMode.E_MODE_DISTRIBS_M_RECORD && name.equals("Line")) {
					xmlmode = XMLMode.E_MODE_DISTRIBS_M_LINE;

					line.distribs_contract_id = new MyID(xpp.getAttributeValue(null, "distribs_contract_id"));
					line.quantity = Double.parseDouble(xpp.getAttributeValue(null, "quantity"));

				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_DISTRIBS_M_RECORD) {

					if (bRecordValid) {
						int _id = 0;
						SaveDistribsSQL(contentResolver, or, _id);
						uids.add(or.uid);
					} else {
						// такого документа уже нет, сообщаем об этом
						myBase.m_empty_distribs.put(or.uid, or.version);
					}

				}
				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены обновления дистрибьюций
		return new ResultLoadXML(true, XMLMode.E_MODE_DISTRIBS_M, context.getString(R.string.message_distribs_updates_loaded));
	}




	static ResultLoadXML LoadXML_VicariousPower(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_VICARIOUS_POWER;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_vicarious_power_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_vicarious_power_version = -ver;
				contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_vicarious_power_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, null, null);
			myBase.m_vicarious_power_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int vicarious_power_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_VICARIOUS_POWER && name.equals("VicariousPowerToDelete")) {
					xmlmode = XMLMode.E_MODE_VICARIOUS_POWER_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "vicarious_power_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_VICARIOUS_POWER && name.equals("VicariousPowerRecord")) {
					xmlmode = XMLMode.E_MODE_VICARIOUS_POWER_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "vicarious_power_id"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("numdoc", xpp.getAttributeValue(null, "numdoc"));
					cv.put("datedoc", xpp.getAttributeValue(null, "datedoc"));
					cv.put("date_action", xpp.getAttributeValue(null, "date_action"));
					cv.put("comment", xpp.getAttributeValue(null, "comment"));
					cv.put("client_id", xpp.getAttributeValue(null, "client_id"));
					cv.put("client_descr", xpp.getAttributeValue(null, "client_descr"));

					cv.put("agreement_id", xpp.getAttributeValue(null, "agreement_id"));
					cv.put("agreement_descr", xpp.getAttributeValue(null, "agreement_descr"));
					cv.put("fio_descr", xpp.getAttributeValue(null, "fio_descr"));
					cv.put("manager_id", xpp.getAttributeValue(null, "manager_id"));
					cv.put("manager_descr", xpp.getAttributeValue(null, "manager_descr"));
					cv.put("organization_id", xpp.getAttributeValue(null, "organization_id"));

					cv.put("organization_descr", xpp.getAttributeValue(null, "organization_descr"));
					cv.put("state", xpp.getAttributeValue(null, "state"));
					cv.put("sum_doc", Double.parseDouble(xpp.getAttributeValue(null, "sum_doc")));


					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_VICARIOUS_POWER) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "VICARIOUS_POWER");
					cv.put("ver", myBase.m_vicarious_power_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закрылся последний уровень
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены доверенности
		return new ResultLoadXML(true, XMLMode.E_MODE_VICARIOUS_POWER, context.getString(R.string.message_vicarious_powers_updates_loaded));
	}

	static ResultLoadXML LoadXML_VisitsUpdate(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_VISIT_M;

		Long _id = 0L;
		UUID uid = null;
		int version = 0;

		int lineno = -1;

		class VisitDataToSaveLine {
			int lineno;
			String distr_point_id;
			int version;
			int version_ack;
			int versionPDA;
			int versionPDA_ack;
		}
		;

		class VisitDataToSave {
			//int linesCount;
			ArrayList<VisitDataToSaveLine> lines;
		}
		;

		VisitDataToSave visitDataToSave = new VisitDataToSave();
		visitDataToSave.lines = new ArrayList();

		//ContentValues visitContentValues=new ContentValues();
		//ContentValues visitLineContentValues=new ContentValues();

		//boolean bRecordValid=false; // если истина, значит все строки визитов совпали
		boolean bRecordReaded = false; // если истина, значит документ в принципе найден
		// проверять сначала bRecordReaded, потом bRecordValid
		boolean bRecordAcked = false; // Если документ полностью подтвержден в 1С (версия КПК везде совпадала)
		boolean bSaveChanges = false; // Значит надо записать версии от 1С
		boolean bLastRecordReceived = false; // Простой способ проверки, что все строки совпали по количеству

		int visits_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_VISIT_M && name.equals("VisitRecord")) {
					xmlmode = XMLMode.E_MODE_VISIT_M_RECORD;

					bRecordReaded = false;
					//bRecordValid=true;
					bRecordAcked = true; // Пока расхождений нет, считаем, что подтвержден
					bSaveChanges = false;
					bLastRecordReceived = false;
					//visitContentValues.clear();
					uid = UUID.fromString(xpp.getAttributeValue(null, "visit_uid").replace("{", "").replace("}", ""));
					Cursor cursorRealRoutesDates = contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{uid.toString()}, "route_date desc");
					if (cursorRealRoutesDates.moveToNext()) {
						bRecordReaded = true;
						_id = cursorRealRoutesDates.getLong(0);
					}
					cursorRealRoutesDates.close();
					//visitDataToSave.linesCount=0;
					visitDataToSave.lines.clear();

					Cursor cursorRealRoutesLines = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id", "lineno", "distr_point_id", "version", "version_ack", "versionPDA", "versionPDA_ack"}, "real_route_id=?", new String[]{Long.toString(_id)}, "lineno");
					int index_RealRoutesLinesLineNo = cursorRealRoutesLines.getColumnIndex("lineno");
					int index_RealRoutesLinesDistrPointId = cursorRealRoutesLines.getColumnIndex("distr_point_id");
					int index_RealRoutesLinesVersion = cursorRealRoutesLines.getColumnIndex("version");
					int index_RealRoutesLinesVersionAck = cursorRealRoutesLines.getColumnIndex("version_ack");
					int index_RealRoutesLinesVersionPDA = cursorRealRoutesLines.getColumnIndex("versionPDA");
					int index_RealRoutesLinesVersionPDA_ack = cursorRealRoutesLines.getColumnIndex("versionPDA_ack");
					while (cursorRealRoutesLines.moveToNext()) {
						VisitDataToSaveLine line = new VisitDataToSaveLine();
						line.lineno = cursorRealRoutesLines.getInt(index_RealRoutesLinesLineNo);
						line.distr_point_id = cursorRealRoutesLines.getString(index_RealRoutesLinesDistrPointId);
						line.version = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersion);
						line.version_ack = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionAck);
						line.versionPDA = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA);
						line.versionPDA_ack = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA_ack);
						visitDataToSave.lines.add(line);
						//if (line.lineno!=visitDataToSave.lines.size())
						//{
						//    bRecordValid=false;
						//}
					}
					cursorRealRoutesLines.close();

				} else if (xmlmode == XMLMode.E_MODE_VISIT_M_RECORD && name.equals("LineRecord")) {
					xmlmode = XMLMode.E_MODE_VISIT_M_LINE;

					// Номер строки, нумерация с 1
					lineno = Integer.parseInt(xpp.getAttributeValue(null, "lineno"));
					if (lineno <= 0 || lineno > visitDataToSave.lines.size()) {
						lineno = -1;
						bRecordAcked = false;
					}
					if (lineno == visitDataToSave.lines.size())
						bLastRecordReceived = true;
					//line.nomenclature_id=new MyID(sc);
						/*
						VisitDataToSaveLine line=new VisitDataToSaveLine();
						line.lineno=cursorRealRoutesLines.getInt(index_RealRoutesLinesLineNo);
						line.distr_point_id=cursorRealRoutesLines.getString(index_RealRoutesLinesDistrPointId);
						line.version=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersion);
						line.version_ack=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionAck);
						line.versionPDA=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA);
						line.versionPDA_ack=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA_ack);
						visitDataToSave.lines.add(line);
						*/
					//case 121: // Версия
						if (lineno > 0) {
							int _version = Integer.parseInt(xpp.getAttributeValue(null, "version"));
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.version != _version) {
								line.version = _version;
								bSaveChanges = true;
							}
						}
					//case 122: // ВерсияПодтвержденная
						if (lineno > 0) {
							int _versionAck = Integer.parseInt(xpp.getAttributeValue(null, "version_ack"));
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.version_ack != _versionAck) {
								line.version_ack = _versionAck;
								bSaveChanges = true;
							}
						}
					//case 123: // ВерсияКПК
						if (lineno > 0) {
							int _versionPDA = Integer.parseInt(xpp.getAttributeValue(null, "versionPDA"));
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.versionPDA != _versionPDA) {
								bRecordAcked = false;
								bSaveChanges = true;
							}
						}
					//case 124: // ВерсияКПКПодтвержденная
						if (lineno > 0) {
							int versionPDA_ack = Integer.parseInt(xpp.getAttributeValue(null, "versionPDA_ack"));
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.versionPDA_ack != versionPDA_ack) {
								line.versionPDA_ack = versionPDA_ack;
								bSaveChanges = true;
							}
							if (line.versionPDA != versionPDA_ack) {
								bRecordAcked = false;
							}
						}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_VISIT_M_RECORD) {

					if (bRecordReaded) {
						//int _id=0;
						//or.weightDoc=TextDatabase.GetRefundWeight(contentResolver, or, null, false);
						//or.dont_need_send=0; // этот флаг в случае, если документ на сервере, не может быть установлен
						//SaveRefundSQL(contentResolver, or, _id);
						//uids.add(or.uid);
						if (!bRecordAcked || !bLastRecordReceived) {
							// Не данные не подтверждены, убедимся, что они при обмене будут отправляться
							boolean bFix = true;
							for (VisitDataToSaveLine line : visitDataToSave.lines) {
								if (line.versionPDA != line.versionPDA_ack || line.version != line.version_ack) {
									// Эти данные будут отправляться, менять ничего не нужно
									bFix = false;
									break;
								}
							}
							if (bFix) {
								// Надо будет отправлять данные, но они будут отправляться - увеличим версии
								for (VisitDataToSaveLine line : visitDataToSave.lines) {
									line.versionPDA++;
								}
								bSaveChanges = true;
							}
						}

					} else {
						// такого документа уже нет, сообщаем об этом
						myBase.m_empty_visits.put(uid, version);
					}

					if (bSaveChanges) {
						ContentValues cv = new ContentValues();
						for (VisitDataToSaveLine line : visitDataToSave.lines) {
							cv.put("distr_point_id", line.distr_point_id);
							cv.put("version", line.version);
							cv.put("version_ack", line.version_ack);
							cv.put("versionPDA", line.versionPDA);
							cv.put("versionPDA_ack", line.versionPDA_ack);
							contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "real_route_id=? and lineno=?", new String[]{Long.toString(_id), String.valueOf(line.lineno)});
						}
					}


				}
				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены обновления визитов
		return new ResultLoadXML(true, XMLMode.E_MODE_VISIT_M, context.getString(R.string.message_vists_updates_loaded));
	}


	static int LoadXML_GpsUpdate(Context context, MyDatabase myBase, XmlPullParser xpp) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_GPS_M;

		int gps_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		int updateInterval = 0; // это значит, что не опрашивается
		// отрицательные - опрашивается с интервалом
		// положительные - возможно, значит, что опрашивать координаты в заказах (не помню)
		if (Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "CheckCoord"), "0"))!=0)
		{
			updateInterval = - Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "CheckInterval"), "0"))-1;
		}

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();
				// Не используется, не помню, здесь какое-то подтверждение GPS приходит по-моему
				if (xmlmode == XMLMode.E_MODE_GPS_M && name.equals("GpsRecord")) {
					xmlmode = XMLMode.E_MODE_GPS_M_RECORD;

					//updateInterval = -Integer.parseInt(xpp.getAttributeValue(null, "updateInterval")) - 1;

					contentResolver.delete(MTradeContentProvider.GPS_COORD_CONTENT_URI, "version=?", new String[]{xpp.getAttributeValue(null, "version")});
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		return updateInterval;
	}

	// Вернет true, если данные изменились
	static boolean LoadXML_Settings(Context context, MyDatabase myBase, XmlPullParser xpp) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		//XMLMode xmlmode = XMLMode.E_MODE_SETTINGS_M;

		String agent_price_type_id = StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "agent_price_type_id"), "");
		
		ContentValues cv=new ContentValues();

		boolean bDataChanged=false;
		Cursor cursor = contentResolver.query(MTradeContentProvider.SETTINGS_CONTENT_URI, new String[]{"agent_price_type_id"}, null, null, null);
		// вообще такого быть не должно, но, допустим, если нет настроек, то и не будем записывать ничего
		if (cursor.moveToFirst()) {
			String old_agent_price_type_id=StringUtils.defaultIfBlank(cursor.getString(0), "");
			if (!old_agent_price_type_id.equals(agent_price_type_id))
			{
				cv.put("agent_price_type_id", agent_price_type_id);
				bDataChanged=true;
			}
		}
		cursor.close();
		
		if (bDataChanged)
		{
			contentResolver.update(MTradeContentProvider.SETTINGS_CONTENT_URI, cv, "", null);
		}

		return bDataChanged;
	}




	static ResultLoadXML LoadXML_Organizations(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_ORGANIZATIONS;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_organizations_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_organizations_version = -ver;
				contentResolver.delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_organizations_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, null, null);
			myBase.m_organizations_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int organizations_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_ORGANIZATIONS && name.equals("OrganizationToDelete")) {
					xmlmode = XMLMode.E_MODE_ORGANIZATION_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "organization_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_ORGANIZATIONS && name.equals("OrganizationRecord")) {
					xmlmode = XMLMode.E_MODE_ORGANIZATION_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "organization_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));

					contentResolver.insert(MTradeContentProvider.ORGANIZATIONS_CONTENT_URI, cv);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_ORGANIZATIONS) {

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "ORGANIZATIONS");
					cv.put("ver", myBase.m_organizations_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все организации
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены организации
		return new ResultLoadXML(true, XMLMode.E_MODE_ORGANIZATIONS, context.getString(R.string.message_organizations_loaded));
	}

	static ResultLoadXML LoadXML_Prices(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_PRICES;

		final int BULK_SIZE = 1500;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_prices_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_prices_version = -ver;
				contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_prices_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, null, null);
			myBase.m_prices_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int price_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.END_DOCUMENT) {
				// документ неожиданно закончился, это ошибка в структуре данных
				return new ResultLoadXML(false, XMLMode.E_MODE_PRICES, "Error loading prices"); // TODO строка сообщения
			} else if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_PRICES && name.equals("PriceToDelete")) {
					xmlmode = XMLMode.E_MODE_PRICE_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.PRICES_CONTENT_URI, "nomenclature_id=? and price_type_id=?", new String[]{xpp.getAttributeValue(null, "nomenclature_id"), xpp.getAttributeValue(null, "price_type_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_PRICES && name.equals("PriceRecord")) {
					xmlmode = XMLMode.E_MODE_PRICE_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("nomenclature_id", xpp.getAttributeValue(null, "nomenclature_id"));
					cv.put("price_type_id", xpp.getAttributeValue(null, "price_type_id"));
					cv.put("ed_izm_id", xpp.getAttributeValue(null, "ed_izm_id"));
					cv.put("edIzm", xpp.getAttributeValue(null, "edIzm"));

					cv.put("price", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "price"), "0.0")));
					cv.put("priceProcent", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "priceProcent"), "0.0")));
					cv.put("k", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "k"), "0.0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_PRICES) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "PRICES");
					cv.put("ver", myBase.m_prices_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены цены
		return new ResultLoadXML(true, XMLMode.E_MODE_PRICES, context.getString(R.string.message_prices_loaded));
	}


	static ResultLoadXML LoadXML_PricesAgreements30(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_PRICES_AGREEMENTS30;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_prices_agreements30_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_prices_agreements30_version = -ver;
				contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_prices_agreements30_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, null, null);
			myBase.m_prices_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int price_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_PRICES_AGREEMENTS30 && name.equals("PricesAgreements30ToDelete")) {
					xmlmode = XMLMode.E_MODE_PRICES_AGREEMENTS30_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, "agreement30_id=? and nomenclature_id=?", new String[]{xpp.getAttributeValue(null, "agreement30_id"), xpp.getAttributeValue(null, "nomenclature_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_PRICES_AGREEMENTS30 && name.equals("PricesAgreements30Record")) {
					xmlmode = XMLMode.E_MODE_PRICES_AGREEMENTS30_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("agreement30_id", xpp.getAttributeValue(null, "agreement30_id"));
					cv.put("nomenclature_id", xpp.getAttributeValue(null, "nomenclature_id"));
					cv.put("pack_id", xpp.getAttributeValue(null, "pack_id"));
					cv.put("ed_izm_id", xpp.getAttributeValue(null, "ed_izm_id"));
					cv.put("edIzm", xpp.getAttributeValue(null, "edIzm"));

					cv.put("price", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "price"), "0.0")));
					cv.put("k", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "k"), "0.0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_PRICES_AGREEMENTS30) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.PRICES_AGREEMENTS30_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "PRICES_AGREEMENTS30");
					cv.put("ver", myBase.m_prices_agreements30_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены цены по договорам
		return new ResultLoadXML(true, XMLMode.E_MODE_PRICES_AGREEMENTS30, context.getString(R.string.message_prices_by_agreements30_loaded));
	}

	static ResultLoadXML LoadXML_PriceTypes(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_PRICE_TYPES;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_pricetypes_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_pricetypes_version = -ver;
				contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_pricetypes_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, null, null);
			myBase.m_pricetypes_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int pricetypes_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_PRICE_TYPES && name.equals("PriceTypeToDelete")) {
					xmlmode = XMLMode.E_MODE_PRICE_TYPE_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.PRICETYPES_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "price_type_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_PRICE_TYPES && name.equals("PriceType")) {
					xmlmode = XMLMode.E_MODE_PRICE_TYPE_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "price_type_id"));
					cv.put("isFolder", Integer.parseInt(xpp.getAttributeValue(null, "isFolder")));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));

					contentResolver.insert(MTradeContentProvider.PRICETYPES_CONTENT_URI, cv);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_PRICE_TYPES) {

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "PRICETYPES");
					cv.put("ver", myBase.m_pricetypes_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все типы цен
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены типы цен
		return new ResultLoadXML(true, XMLMode.E_MODE_PRICE_TYPES, context.getString(R.string.message_price_types_loaded));
	}

	static ResultLoadXML LoadXML_Rests(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_RESTS;

		final int BULK_SIZE = 1000;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_rests_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_rests_version = -ver;
				contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_rests_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, null, null);
			myBase.m_rests_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int rests_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_RESTS && name.equals("RestsToDelete")) {
					xmlmode = XMLMode.E_MODE_REST_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.RESTS_CONTENT_URI, "nomenclature_id=? and stock_id=?", new String[]{xpp.getAttributeValue(null, "nomenclature_id"), xpp.getAttributeValue(null, "stock_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_RESTS && name.equals("RestsRecord")) {
					xmlmode = XMLMode.E_MODE_REST_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("nomenclature_id", xpp.getAttributeValue(null, "nomenclature_id"));
					cv.put("stock_id", xpp.getAttributeValue(null, "stock_id"));
					cv.put("quantity", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "quantity"), "0.0")));
					cv.put("quantity_reserve", Double.parseDouble(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "quantity_reserve"), "0.0")));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_RESTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.RESTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "RESTS");
					cv.put("ver", myBase.m_rests_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончилась все остатки
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены остатки
		return new ResultLoadXML(true, XMLMode.E_MODE_RESTS, context.getString(R.string.message_rests_loaded));
	}


	static ResultLoadXML LoadXML_RouteDates(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_ROUTE_DATES;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_routes_dates_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_routes_dates_version = -ver;
				contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_routes_dates_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, null, null);
			myBase.m_routes_dates_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int routes_dates_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_ROUTE_DATES && name.equals("RouteDateToDelete")) {
					xmlmode = XMLMode.E_MODE_ROUTE_DATE_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, "route_id=? and route_date=?", new String[]{xpp.getAttributeValue(null, "route_id"), xpp.getAttributeValue(null, "route_date")});
					}
				} else if (xmlmode == XMLMode.E_MODE_ROUTE_DATES && name.equals("RouteDate")) {
					xmlmode = XMLMode.E_MODE_ROUTE_DATE_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("route_id", xpp.getAttributeValue(null, "route_id"));
					cv.put("route_date", xpp.getAttributeValue(null, "date"));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_ROUTE_DATES) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_DATES_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "ROUTES_DATES");
					cv.put("ver", myBase.m_routes_dates_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены маршруты по датам
		return new ResultLoadXML(true, XMLMode.E_MODE_ROUTE_DATES, context.getString(R.string.message_routes_dates_loaded));
	}

	// TODO ускорить загрузку (сделать пакетную)
	static ResultLoadXML LoadXML_Routes(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_ROUTES;
		String routeId="";
		int lineno=0;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		final int BULK_L_SIZE = 300;
		int bulk_l_idx = 0;
		ContentValues[] values_l = new ContentValues[BULK_L_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_routes_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_routes_version = -ver;
				contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null);
				contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_routes_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, null, null);
			contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, null, null);
			myBase.m_routes_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int routes_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_ROUTES && name.equals("RouteToDelete")) {
					xmlmode = XMLMode.E_MODE_ROUTE_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.ROUTES_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "route_id")});
						contentResolver.delete(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, "route_id=?", new String[]{xpp.getAttributeValue(null, "route_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_ROUTES && name.equals("Route")) {
					xmlmode = XMLMode.E_MODE_ROUTE_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					routeId=xpp.getAttributeValue(null, "route_id");
					cv.put("id", routeId);
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("manager_id", xpp.getAttributeValue(null, "manager_id"));

					//contentResolver.insert(MTradeContentProvider.ROUTES_CONTENT_URI, cv);

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_CONTENT_URI, values);
						bulk_idx = 0;
					}

					lineno=0;
				} else if (xmlmode == XMLMode.E_MODE_ROUTE_RECORD && name.equals("LineRecord")) {
					xmlmode = XMLMode.E_MODE_ROUTE_LINE;

					lineno++;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("route_id", routeId);
					cv.put("lineno", lineno);
					cv.put("distr_point_id", xpp.getAttributeValue(null, "distr_point_id"));
					cv.put("visit_time", xpp.getAttributeValue(null, "visit_time"));

					//contentResolver.insert(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, cv);

					values_l[bulk_l_idx] = cv;
					bulk_l_idx++;
					if (bulk_l_idx >= BULK_L_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, values);
						bulk_idx = 0;
					}

				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_ROUTES) {

					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_CONTENT_URI, values_2);
					}

					if (bulk_l_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_l_idx];
						int i;
						for (i = 0; i < bulk_l_idx; i++) {
							values_2[i] = values_l[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.ROUTES_LINES_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "ROUTES");
					cv.put("ver", myBase.m_routes_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все организации
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены маршруты
		return new ResultLoadXML(true, XMLMode.E_MODE_ROUTES, context.getString(R.string.message_routes_loaded));
	}


	static ResultLoadXML LoadXML_Stocks(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_STOCKS;

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_stocks_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_stocks_version = -ver;
				contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_stocks_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, null, null);
			myBase.m_stocks_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int stocks_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_STOCKS && name.equals("StockToDelete")) {
					xmlmode = XMLMode.E_MODE_STOCK_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.STOCKS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "stock_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_STOCKS && name.equals("Stock")) {
					xmlmode = XMLMode.E_MODE_STOCK_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "stock_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("flags", Integer.parseInt(xpp.getAttributeValue(null, "flags")));

					contentResolver.insert(MTradeContentProvider.STOCKS_CONTENT_URI, cv);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_STOCKS) {

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "STOCKS");
					cv.put("ver", myBase.m_stocks_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закончились все склады
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены склады
		return new ResultLoadXML(true, XMLMode.E_MODE_STOCKS, context.getString(R.string.message_stocks_loaded));
	}


	static ResultLoadXML LoadXML_SumShippingNotificationsM(Context context, MyDatabase myBase, XmlPullParser xpp) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_SUM_SHIPPING_NOTIFICATIONS_M;

		boolean bRecordValid = false;
		OrderRecord or=new OrderRecord();
		OrderLineRecord line = new OrderLineRecord();

		int orders_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_SUM_SHIPPING_NOTIFICATIONS_M && name.equals("SumShippingNotificationRecord")) {
					xmlmode = XMLMode.E_MODE_SUM_SHIPPING_NOTIFICATIONS_M_RECORD;

					String ordersId = xpp.getAttributeValue(null, "order_uid").replace("{", "").replace("}", "");
					double sum_shipping = Common.MyStringToDouble(xpp.getAttributeValue(null, "sum_shipping"));

					// узнаем версию для того, чтобы при последующей записи она не увеличилась на 1
					Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id", "versionPDA"}, "id=?", new String[]{ordersId}, "datedoc desc");
					int _idIndex = cursor.getColumnIndex("_id");
					int versionPDAIndex = cursor.getColumnIndex("versionPDA");
					while (cursor.moveToNext()) {
						// вообще будет найден всегда 1 документ
						ContentValues cv = new ContentValues();
						cv.put("versionPDA", cursor.getInt(versionPDAIndex));
						cv.put("sum_shipping", sum_shipping);
						contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=?", new String[]{cursor.getString(_idIndex)});
					}
					cursor.close();
					myBase.m_acks_shipping_sums.put(UUID.fromString(ordersId), sum_shipping);
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				// Закончились все записи
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены суммы реализаций
		return new ResultLoadXML(true, XMLMode.E_MODE_SUM_SHIPPING_NOTIFICATIONS_M, context.getString(R.string.message_shipping_notifications_updates_loaded));
	}



	static ResultLoadXML LoadXML_DistrPoints(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_DISTR_POINTS;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_distribs_contracts_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_distribs_contracts_version = -ver;
				contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_distribs_contracts_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, null, null);
			myBase.m_distribs_contracts_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int distribs_contracts_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_DISTR_POINTS && name.equals("DistrPointToDelete")) {
					xmlmode = XMLMode.E_MODE_DISTR_POINT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "distr_point_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_DISTR_POINTS && name.equals("DistrPointRecord")) {
					xmlmode = XMLMode.E_MODE_DISTR_POINT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "distr_point_id"));
					cv.put("owner_id", xpp.getAttributeValue(null, "owner_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("address", xpp.getAttributeValue(null, "address"));
					cv.put("phones", xpp.getAttributeValue(null, "phones"));
					cv.put("contacts", xpp.getAttributeValue(null, "contacts"));
					cv.put("price_type_id", xpp.getAttributeValue(null, "price_type_id"));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_DISTR_POINTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "D_POINTS");
					cv.put("ver", myBase.m_distribs_contracts_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закрылся последний уровень
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены торговые точки
		return new ResultLoadXML(true, XMLMode.E_MODE_DISTR_POINTS, context.getString(R.string.message_distr_points_loaded));
	}

	static ResultLoadXML LoadXML_DistribsContracts(Context context, MyDatabase myBase, XmlPullParser xpp, boolean bUpdateMode) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_DISTRIBS_CONTRACTS;

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		if (bUpdateMode) {
			int ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
			if (myBase.m_distribs_contracts_version >= ver && ver >= 0) {
				// версия старая или текущая, не загружаем
				return new ResultLoadXML(false);
			}
			if (ver < 0) {
				myBase.m_distribs_contracts_version = -ver;
				contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, null, null);
				bUpdateMode = false;
			} else
				myBase.m_distr_points_version = ver;
		} else {
			contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, null, null);
			myBase.m_distribs_contracts_version = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
		}

		int distr_points_system_version = Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "system_ver"), "0"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_DISTRIBS_CONTRACTS && name.equals("DistribsContractsToDelete")) {
					xmlmode = XMLMode.E_MODE_DISTRIBS_CONTRACT_TO_DELETE;
					if (bUpdateMode)
					{
						// запись с указанным ID надо удалить
						contentResolver.delete(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, "id=?", new String[]{xpp.getAttributeValue(null, "distribs_contract_id")});
					}
				} else if (xmlmode == XMLMode.E_MODE_DISTRIBS_CONTRACTS && name.equals("DistribsContractsRecord")) {
					xmlmode = XMLMode.E_MODE_DISTRIBS_CONTRACT_RECORD;

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("id", xpp.getAttributeValue(null, "distribs_contract_id"));
					cv.put("code", xpp.getAttributeValue(null, "code"));
					cv.put("descr", xpp.getAttributeValue(null, "descr"));
					cv.put("position", xpp.getAttributeValue(null, "position"));

					values[bulk_idx] = cv;
					bulk_idx++;
					if (bulk_idx >= BULK_SIZE) {
						contentResolver.bulkInsert(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, values);
						bulk_idx = 0;
					}
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				if (xmlmode == XMLMode.E_MODE_DISTRIBS_CONTRACTS) {
					if (bulk_idx > 0) {
						ContentValues[] values_2 = new ContentValues[bulk_idx];
						int i;
						for (i = 0; i < bulk_idx; i++) {
							values_2[i] = values[i];
						}
						contentResolver.bulkInsert(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, values_2);
					}

					ContentValues cv = new ContentValues();
					cv.clear();
					cv.put("param", "DISTRIBS_CONTRACTS");
					cv.put("ver", myBase.m_distribs_contracts_version);
					Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_CONTENT_URI, cv);
				}

				// Закрылся последний уровень
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		// Загружены контракты для дистрибьюции
		return new ResultLoadXML(true, XMLMode.E_MODE_DISTRIBS_CONTRACTS, context.getString(R.string.message_contracts_for_distribs_loaded));
	}


	static int LoadXML_SalesHistoryHeader(Context context, MyDatabase myBase, XmlPullParser xpp, ArrayList<UUID> uids) throws IOException, XmlPullParserException {

		ContentResolver contentResolver=context.getContentResolver();

		List<XMLMode> stack = new ArrayList<XMLMode>();
		XMLMode xmlmode = XMLMode.E_MODE_SALES_HISTORY_HEADERS;

		int version=Integer.parseInt(StringUtils.defaultIfBlank(xpp.getAttributeValue(null, "ver"), "-1"));

		while (true) {
			int eventType = xpp.next();
			if (eventType == XmlPullParser.START_TAG) {
				stack.add(xmlmode);
				//System.out.println("Start tag " + xpp.getName());
				String name = xpp.getName();

				if (xmlmode == XMLMode.E_MODE_SALES_HISTORY_HEADERS && name.equals("SalesHistoryHeaderRecord")) {
					xmlmode = XMLMode.E_MODE_SALES_HISTORY_HEADERS_RECORD;

					uids.add(UUID.fromString(xpp.getAttributeValue(null, "uid")));
				} else xmlmode=XMLMode.E_MODE_UNKNOWN;

			} else if (eventType == XmlPullParser.END_TAG) {

				// Закрылся последний уровень
				if (stack.size() == 0)
					break;
				xmlmode = stack.get(stack.size() - 1);
				stack.remove(stack.size() - 1);
			}
		}

		return version;
	}



	static ResultLoadXML LoadXML(Context context, MyDatabase myBase, InputStream is, boolean bUpdateMode) {

		ResultLoadXML result=new ResultLoadXML(false);

		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(is, "utf-8");
			//xpp.setInput(is, null);

			ContentResolver contentResolver=context.getContentResolver();

			MessageRecord mr = new MessageRecord();

			List<XMLMode> stack = new ArrayList<XMLMode>();
			XMLMode xmlmode = XMLMode.E_MODE_INIT;


			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					System.out.println("Start document");
					xmlmode = XMLMode.E_MODE_NODE;
				} else if (eventType == XmlPullParser.START_TAG) {
					stack.add(xmlmode);
					//System.out.println("Start tag " + xpp.getName());
					String name = xpp.getName();

					// При успешной загрузке этот текст заменится на реальный результат,
					// а в случае исключения останется этот текст
					result.ResultMessage="Error loading "+name;
					//
					if (xmlmode == XMLMode.E_MODE_NODE && name.equals("messages")) {
						xmlmode = XMLMode.E_MODE_MESSAGES;
					} else if (xmlmode == XMLMode.E_MODE_MESSAGES && name.equals("message")) {
						xmlmode = XMLMode.E_MODE_MESSAGE;
						String uid = xpp.getAttributeValue(null, "uid");
						mr.uid = UUID.fromString(uid);
						mr.acknowledged = Integer.parseInt(xpp.getAttributeValue(null, "flags"));
						mr.sender_id = new MyID(xpp.getAttributeValue(null, "sender"));
						mr.receiver_id = new MyID(xpp.getAttributeValue(null, "receiver"));
						mr.fname = xpp.getAttributeValue(null, "filename");
						mr.datetime = xpp.getAttributeValue(null, "datetime");
						mr.ver = Integer.parseInt(xpp.getAttributeValue(null, "ver"));
						mr.type_idx = Integer.parseInt(xpp.getAttributeValue(null, "type_idx"));

						if (bUpdateMode) {
							Cursor cursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"ver", "acknowledged"}, "uid=?", new String[]{mr.uid.toString()}, null);
							if (cursor != null && cursor.moveToNext()) {
								int acknowledgedIndex = cursor.getColumnIndex("acknowledged");
								int verIndex = cursor.getColumnIndex("ver");
								int acknowledged = cursor.getInt(acknowledgedIndex);
								int ver = cursor.getInt(verIndex);
								// &16 == 0 сообщение не прочитано
								// &16 прочитано
								// прислали с флагом, что не прочитано, а у нас прочитано
								if (((mr.acknowledged & 4) == 0) && ((mr.acknowledged & 16) == 0))
									if (((acknowledged & 4) == 0) && ((acknowledged & 16) != 0))
										if (mr.ver == ver) {
											mr.acknowledged |= 16;
										}
							}
						}
					} else if (xmlmode == XMLMode.E_MODE_MESSAGE && name.equals("text")) {
						xmlmode = XMLMode.E_MODE_MESSAGE_TEXT;
					} else if (xmlmode == XMLMode.E_MODE_MESSAGE && name.equals("report")) {
						xmlmode = XMLMode.E_MODE_MESSAGE_REPORT;
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Agents")) {
						result=LoadXML_Agents(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Agreements")) {
						result=LoadXML_Agreements(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Clients")) {
						result=LoadXML_Clients(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Curators")) {
						result=LoadXML_Curators(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Debts")) {
						result=LoadXML_Debts(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("DebtsExt")) {
						result=LoadXML_DebtsExt(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Equipment")) {
						result=LoadXML_Equipment(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("EquipmentRests")) {
						result=LoadXML_EquipmentRests(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Nomenclature")) {
						result=LoadXML_Nomenclature(context, myBase, xpp, bUpdateMode);
						TextDatabase.fillNomenclatureHierarchy(contentResolver, context.getResources(), "     0   ");
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("OrdersM")) {
						ArrayList<UUID> uuids = new ArrayList<UUID>();
						result=LoadXML_OrdersUpdate(context, myBase, xpp, uuids);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("PaymentsM")) {
						ArrayList<UUID> uuids = new ArrayList<UUID>();
						result=LoadXML_PaymentsUpdate(context, myBase, xpp, uuids);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("RefundsM")) {
						ArrayList<UUID> uuids = new ArrayList<UUID>();
						result=LoadXML_RefundsUpdate(context, myBase, xpp, uuids);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("DistribsM")) {
						ArrayList<UUID> uuids = new ArrayList<UUID>();
						result=LoadXML_DistribsUpdate(context, myBase, xpp, uuids);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("VicariousPower")) {
						result=LoadXML_VicariousPower(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("VisitsM")) {
						ArrayList<UUID> uuids = new ArrayList<UUID>();
						result=LoadXML_VisitsUpdate(context, myBase, xpp, uuids);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("GPS_M")) {
						int update_interval=LoadXML_GpsUpdate(context, myBase, xpp);
						result.xmlMode=XMLMode.E_MODE_GPS_M;
						result.bSuccess=true;
						result.ResultMessage=context.getString(R.string.message_gps_updates_loaded);
						result.nResult=update_interval;
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equalsIgnoreCase("Settings_M")) {
						boolean bDataChanged=LoadXML_Settings(context, myBase, xpp);
						result.xmlMode=XMLMode.E_MODE_SETTINGS_M;
						result.bSuccess=true;
						if (bDataChanged)
							result.ResultMessage=context.getString(R.string.message_settings_loaded);
						else
							result.ResultMessage=context.getString(R.string.message_settings_not_changed);

					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Organizations")) {
						result=LoadXML_Organizations(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Prices")) {
						result=LoadXML_Prices(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("PricesAgreements30")) {
						result=LoadXML_PricesAgreements30(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("PriceTypes")) {
						result=LoadXML_PriceTypes(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Rests")) {
						result=LoadXML_Rests(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("RouteDates")) {
						result=LoadXML_RouteDates(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Routes")) {
						result=LoadXML_Routes(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("Stocks")) {
						result=LoadXML_Stocks(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("SumShippingNotificationsM")) {
						result=LoadXML_SumShippingNotificationsM(context, myBase, xpp);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("DistrPoints")) {
						result=LoadXML_DistrPoints(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("DistribsContracts")) {
						result=LoadXML_DistribsContracts(context, myBase, xpp, bUpdateMode);
					} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("SalesHistoryHeader")) {
						// TODO эти данные куда-то наверх передаются, а здесь я сделал локальную переменные
						ArrayList<UUID> sales_uuids = new ArrayList<UUID>();
						// Вернет -1, если файл пустой
						int sales_version = LoadXML_SalesHistoryHeader(context, myBase, xpp, sales_uuids);
						if (sales_version>0) {
							result.xmlMode=XMLMode.E_MODE_SALES_HISTORY_HEADERS;
							result.bSuccess=true;
							result.ResultMessage=context.getString(R.string.message_sales_history_header_loaded);
							// 21.09.2022
							result.uuids=sales_uuids;
							// удалим другие версии
							contentResolver.delete(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, "ver<>?", new String[]{String.valueOf(sales_version)});
							contentResolver.delete(MTradeContentProvider.SALES_LOADED_CONTENT_URI, "ver<>?", new String[]{String.valueOf(sales_version)});
							//
							// TODO проверить, что старый LoadSalesHistory вызывается
						}
					// Эти файлы формируются отдельно в виде txt файлов, пока по-старому история продаж будет загружаться
					//} else if (xmlmode == XMLMode.E_MODE_NODE && name.equals("SalesHistory")) {
					//	LoadXML_SalesHistory(contentResolver, myBase, xpp, bUpdateMode);
					} else {
						xmlmode = XMLMode.E_MODE_UNKNOWN;
					}
				} else if (eventType == XmlPullParser.END_TAG) {
					System.out.println("End tag " + xpp.getName());

					switch (xmlmode) {
						case E_MODE_MESSAGE: {
							// подтверждено сервером, удалим это сообщение
							if ((mr.acknowledged & 1) != 0) {
								contentResolver.delete(MTradeContentProvider.MESSAGES_CONTENT_URI, "uid=?", new String[]{mr.uid.toString()});
							} else {
								ContentValues cv = new ContentValues();
								cv.put("uid", mr.uid.toString());
								cv.put("sender_id", mr.sender_id.toString());
								cv.put("receiver_id", mr.receiver_id.toString());
								cv.put("text", mr.text);
								cv.put("report", mr.report);
								cv.put("fname", mr.fname);
								cv.put("datetime", mr.datetime);
								cv.put("acknowledged", mr.acknowledged);
								cv.put("ver", mr.ver);
								cv.put("type_idx", mr.type_idx);

								contentResolver.insert(MTradeContentProvider.MESSAGES_CONTENT_URI, cv);
							}
							result.xmlMode=XMLMode.E_MODE_MESSAGE;
							result.bSuccess=true;
							result.ResultMessage=context.getString(R.string.message_messages_loaded);

							break;
						}
						default:
					}


					xmlmode = stack.get(stack.size() - 1);
					stack.remove(stack.size() - 1);
				} else if (eventType == XmlPullParser.TEXT) {
					System.out.println("Text " + xpp.getText());
					switch (xmlmode) {
						case E_MODE_MESSAGE_TEXT:
							mr.text = xpp.getText();
							break;
						case E_MODE_MESSAGE_REPORT:
							mr.report = xpp.getText();
							break;
						default:
					}
				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}


	static int LoadSalesHistoryHeader(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uids) {
		int nom_idx = 0;
		int version = -1;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			String sc = sc0.replace("\r", "");
			if (nom_idx == 0) {
				if (sc.isEmpty())
					continue;
				version = Integer.parseInt(sc);
			} else {
				if (sc.length() == 36) {
					uids.add(UUID.fromString(sc));
				}
			}
			nom_idx++;
		}
		return version;
	}

	static boolean LoadSalesHistory(ContentResolver contentResolver, MyDatabase myBase, String buf, UUID fname, int version) {

		SalesHistoryRecord sr = new SalesHistoryRecord();

		final int BULK_SIZE = 300;
		int bulk_idx = 0;
		ContentValues[] values = new ContentValues[BULK_SIZE];

		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			String sc = sc0.replace("\r", "");

			String split2[] = sc.split("#");
			int idx = 0;
			if (split2.length >= 9) {
				for (String sc2 : split2) {
					switch (idx) {
						case 0:
							sr.datedoc = new String(sc2);
							break;
						case 1:
							sr.numdoc = new String(sc2);
							break;
						case 2:
							sr.sale_doc_id = new MyID(sc2);
							break;
						case 3:
							sr.client_id = new MyID(sc2);
							break;
						case 4:
							sr.curator_id = new MyID(sc2);
							break;
						case 5:
							sr.distr_point_id = new MyID(sc2);
							break;
						case 6:
							sr.nomenclature_id = new MyID(sc2);
							break;
						case 7:
							sr.quantity = Common.MyStringToDouble(sc2);
							break;
						case 8:
							sr.price = Common.MyStringToDouble(sc2);
							break;
					}
					idx++;
				}
				ContentValues cv = new ContentValues();
				cv.put("ver", version);
				cv.put("numdoc", sr.numdoc);
				cv.put("datedoc", sr.datedoc);
				cv.put("refdoc", sr.sale_doc_id.toString());
				cv.put("client_id", sr.client_id.toString());
				cv.put("curator_id", sr.curator_id.toString());
				cv.put("distr_point_id", sr.distr_point_id.toString());
				cv.put("nomenclature_id", sr.nomenclature_id.toString());
				cv.put("quantity", sr.quantity);
				cv.put("price", sr.price);
				values[bulk_idx++] = cv;
				if (bulk_idx >= BULK_SIZE) {
					contentResolver.bulkInsert(MTradeContentProvider.SALES_LOADED_CONTENT_URI, values);
					bulk_idx = 0;
				}
			}
		}
		if (bulk_idx > 0) {
			ContentValues[] values_2 = new ContentValues[bulk_idx];
			int i;
			for (i = 0; i < bulk_idx; i++) {
				values_2[i] = values[i];
			}
			contentResolver.bulkInsert(MTradeContentProvider.SALES_LOADED_CONTENT_URI, values_2);
		}
		ContentValues cv = new ContentValues();
		cv.put("param", fname.toString());
		cv.put("ver", version);
		Uri newUri = contentResolver.insert(MTradeContentProvider.VERSIONS_SALES_CONTENT_URI, cv);
		myBase.m_sales_loaded_versions.put(fname, version);

		return true;
	}

	static ContentValues getSafeContentValuesForOrderSQLFields(OrderRecord rec, String[] fieldNames) {
		//
		ContentValues cv = new ContentValues();

		for (String fieldName : fieldNames) {
			boolean bResultOk = false;
			try {
				Field field = null;
				Class<?> currentClass = rec.getClass();
				Class<?> parentClass = currentClass.getSuperclass();
				while (field == null) {
					if (parentClass == null) {
						field = currentClass.getDeclaredField(fieldName);
					} else {
						try {
							// Если поля нет в текущем классе, возможно он есть у родителя
							// кстати, обычный getField сработал бы в любом случае
							field = currentClass.getDeclaredField(fieldName);
						} catch (NoSuchFieldException e) {
							currentClass = parentClass;
							parentClass = currentClass.getSuperclass();
						}
					}
				}
				field.setAccessible(true);
				Class<?> clazz = field.getType();
				//Object value = field.getLong(rec);
				//if (clazz.isPrimitive())
				//{
				if (Long.class.equals(clazz) || long.class.equals(clazz))
					cv.put(fieldName, field.getLong(rec));
				else if (Integer.class.equals(clazz) || int.class.equals(clazz))
					cv.put(fieldName, field.getInt(rec));
				else if (Double.class.equals(clazz) || double.class.equals(clazz))
					cv.put(fieldName, field.getDouble(rec));
				else if (Float.class.equals(clazz) || float.class.equals(clazz))
					cv.put(fieldName, field.getFloat(rec));
				else if (Short.class.equals(clazz) || short.class.equals(clazz))
					cv.put(fieldName, field.getInt(rec));
				else if (Short.class.equals(clazz) || short.class.equals(clazz))
					cv.put(fieldName, field.getInt(rec));
				else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz))
					cv.put(fieldName, field.getBoolean(rec));
				else if (Byte.class.equals(clazz) || byte.class.equals(clazz))
					cv.put(fieldName, field.getByte(rec));
				else if (E_BW.class.equals(clazz)) {
					Object value = field.get(rec);
					cv.put(fieldName, E_BW_toInt((E_BW) value));
				} else if (E_TRADE_TYPE.class.equals(clazz)) {
					Object value = field.get(rec);
					cv.put(fieldName, E_TRADE_TYPE_toInt((E_TRADE_TYPE) value));
				} else {
					cv.put(fieldName, field.get(rec).toString());
				}
				bResultOk = true;
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!bResultOk) {
				Log.d("mtradeLogs", "Update, skipped field = " + fieldName);
			}
		}
		return cv;
	}

	@SuppressWarnings("null")
	static void SetOrderSQLFields(ContentResolver contentResolver, OrderRecord rec, long _id, String[] fieldNames) {
		// Документ еще не был сохранен (когда даже modified еще не был установлен)
		if (_id == 0)
			return;

		ContentValues cv = getSafeContentValuesForOrderSQLFields(rec, fieldNames);

		if (cv.size() > 0) {
			// Меняем эти поля в базе
			contentResolver.update(MTradeContentProvider.ORDERS_SILENT_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
		}
	}

	static void UpdateOrderLine(ContentResolver contentResolver, long order_id, OrderRecord rec, OrderLineRecord orderLine) {
		ContentValues cvl = new ContentValues();
		cvl.put("order_id", Long.toString(order_id));
		cvl.put("nomenclature_id", orderLine.nomenclature_id.toString());
		cvl.put("client_id", rec.client_id.toString());
		cvl.put("quantity_requested", orderLine.quantity_requested);
		cvl.put("quantity", orderLine.quantity);
		cvl.put("price", orderLine.price);
		cvl.put("total", orderLine.total);
		cvl.put("discount", orderLine.discount);
		cvl.put("k", orderLine.k);
		cvl.put("ed", orderLine.ed);
		cvl.put("shipping_time", orderLine.shipping_time);
		cvl.put("comment_in_line", orderLine.comment_in_line);
		cvl.put("lineno", orderLine.lineno);

		int cnt = contentResolver.update(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cvl, "order_id=? and lineno=?", new String[]{Long.toString(order_id), Long.toString(orderLine.lineno)});
		if (cnt == 0) {
			contentResolver.insert(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cvl);
		}
	}

	static void DeleteOrderLine(ContentResolver contentResolver, long order_id, int lineno) {
		contentResolver.delete(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, "order_id=? and lineno=?", new String[]{Long.toString(order_id), Integer.toString(lineno)});
	}

	static void DeleteOrderLines(ContentResolver contentResolver, long order_id) {
		contentResolver.delete(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, "order_id=?", new String[]{Long.toString(order_id)});
	}


	static long SaveOrderSQL(ContentResolver contentResolver, OrderRecord rec, long _id, int editing_backup) {

		boolean bOrderUpdated = false;

		ContentValues cv = new ContentValues();

		cv.put("uid", rec.uid.toString());
		cv.put("version", rec.version);
		cv.put("version_ack", rec.version_ack);
		cv.put("versionPDA", rec.versionPDA);
		cv.put("versionPDA_ack", rec.versionPDA_ack);
		cv.put("id", rec.id.toString());
		cv.put("numdoc", rec.numdoc);
		cv.put("datedoc", rec.datedoc);
		cv.put("client_id", rec.client_id.toString());
		if (rec.agreement30_id!=null)
		{
			cv.put("agreement30_id", rec.agreement30_id.toString());
		} else
		{
			cv.put("agreement30_id", Constants.emptyID);
		}
		cv.put("agreement_id", rec.agreement_id.toString());
		cv.put("distr_point_id", rec.distr_point_id.toString());
		cv.put("comment", rec.comment);
		cv.put("comment_closing", rec.comment_closing);
		cv.put("comment_payment", rec.comment_payment);
		cv.put("closed_not_full", rec.closed_not_full);
		cv.put("state", rec.state.value());
		cv.put("curator_id", rec.curator_id.toString());
		cv.put("bw", E_BW_toInt(rec.bw));
		cv.put("trade_type", E_TRADE_TYPE_toInt(rec.trade_type));
		cv.put("datecoord", rec.datecoord);
		cv.put("latitude", rec.latitude);
		cv.put("longitude", rec.longitude);
		cv.put("gpsstate", rec.gpsstate);
		cv.put("datecreation", rec.datecreation);
		cv.put("gpsaccuracy", rec.gpsaccuracy);
		cv.put("accept_coord", rec.accept_coord);
		cv.put("dont_need_send", rec.dont_need_send);
		cv.put("price_type_id", rec.price_type_id.toString());
		cv.put("stock_id", rec.stock_id.toString());
		cv.put("sum_doc", rec.sumDoc);
		cv.put("sum_shipping", rec.sumShipping);
		cv.put("weight_doc", rec.weightDoc);
		cv.put("shipping_type", rec.shipping_type);
		cv.put("shipping_time", rec.shipping_time);
		cv.put("shipping_begin_time", rec.shipping_begin_time);
		cv.put("shipping_end_time", rec.shipping_end_time);
		cv.put("shipping_date", rec.shipping_date);
		cv.put("simple_discount_id", rec.simple_discount_id.toString());
		cv.put("create_client", rec.create_client);
		cv.put("create_client_descr", rec.create_client_lastname.trim() + " " + rec.create_client_firstname.trim() + " " + rec.create_client_surname.trim());
		cv.put("create_client_surname", rec.create_client_surname);
		cv.put("create_client_firstname", rec.create_client_firstname);
		cv.put("create_client_lastname", rec.create_client_lastname);
		//cv.put("create_client_phone", rec.create_client_phone);
		//cv.put("place_num", rec.place_num);
		cv.put("card_num", rec.card_num);
		//cv.put("ticket_m", rec.ticket_m);
		//cv.put("ticket_w", rec.ticket_w);
		//cv.put("quant_m", rec.quant_m);
		//cv.put("quant_w", rec.quant_w);
		cv.put("quant_mw", rec.quant_mw);
		cv.put("pay_credit", rec.pay_credit);
		cv.put("manager_comment", rec.manager_comment);
		cv.put("theme_comment", rec.theme_comment);
		cv.put("phone_num", rec.phone_num);
		cv.put("editing_backup", editing_backup);
		cv.put("old_id", rec.old_id);

		//if (editing_backup==0)
		//{
		// это новый заказ, либо просто id не указали
		// попытаемся найти по uid
		if (_id == 0) {
			Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=? and editing_backup=?", new String[]{rec.uid.toString(), Integer.toString(editing_backup)}, "datedoc desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int _idIndex = cursor.getColumnIndex("_id");
					_id = cursor.getLong(_idIndex);
				}
				cursor.close();
			}
		}
		//}
		if (editing_backup == 0) {
			int rowsUpdated = contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=? and editing_backup=0", new String[]{Long.toString(_id)});
			if (rowsUpdated > 0)
				bOrderUpdated = true;
			// новый заказ, добавим его
			if (bOrderUpdated == false) {
				if (rec.state == E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED) {
					Uri newUri = contentResolver.insert(MTradeContentProvider.ORDERS_SILENT_CONTENT_URI, cv);
					_id = ContentUris.parseId(newUri);
				} else {
					Uri newUri = contentResolver.insert(MTradeContentProvider.ORDERS_CONTENT_URI, cv);
					_id = ContentUris.parseId(newUri);
				}
			}
		} else {
			if (_id != 0) {
				// Это когда редактировали существующий документ, но вылетели
				// и потом снова зашли редактировать
				contentResolver.update(MTradeContentProvider.ORDERS_SILENT_CONTENT_URI, cv, "_id=? and editing_backup=0", new String[]{Long.toString(_id)});
			} else {
				Uri newUri = contentResolver.insert(MTradeContentProvider.ORDERS_SILENT_CONTENT_URI, cv);
				_id = ContentUris.parseId(newUri);
			}
		}
		// Удалим старые строки, если они были с таким _id
		contentResolver.delete(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, "order_id=?", new String[]{Long.toString(_id)});
		//
		if (rec.lines.size() > 0) {
			ContentValues[] values = new ContentValues[rec.lines.size()];
			for (OrderLineRecord orderLine : rec.lines) {
				orderLine.lineno = rec.lines.indexOf(orderLine) + 1;
				ContentValues cvl = new ContentValues();
				cvl.put("order_id", Long.toString(_id));
				cvl.put("nomenclature_id", orderLine.nomenclature_id.toString());
				cvl.put("client_id", rec.client_id.toString());
				cvl.put("quantity_requested", orderLine.quantity_requested);
				cvl.put("quantity", orderLine.quantity);
				cvl.put("price", orderLine.price);
				cvl.put("total", orderLine.total);
				cvl.put("discount", orderLine.discount);
				cvl.put("k", orderLine.k);
				cvl.put("ed", orderLine.ed);
				cvl.put("shipping_time", orderLine.shipping_time);
				cvl.put("comment_in_line", orderLine.comment_in_line);
				cvl.put("lineno", orderLine.lineno);

				values[rec.lines.indexOf(orderLine)] = cvl;
			}
			contentResolver.bulkInsert(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, values);
		}
		if (MySingleton.getInstance().Common.PHARAOH) {
			contentResolver.delete(MTradeContentProvider.ORDERS_PLACES_CONTENT_URI, "order_id=?", new String[]{Long.toString(_id)});

			ContentValues[] values = new ContentValues[rec.places.size()];
			for (OrderPlaceRecord orderPlace : rec.places) {
				ContentValues cvl = new ContentValues();
				cvl.put("order_id", Long.toString(_id));
				cvl.put("place_id", orderPlace.place_id.toString());

				values[rec.places.indexOf(orderPlace)] = cvl;
			}
			contentResolver.bulkInsert(MTradeContentProvider.ORDERS_PLACES_CONTENT_URI, values);

		}
		return _id;
	}

	static boolean ReadOrderBy_Id(ContentResolver contentResolver, OrderRecord rec, long _id) {

		Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, null, "_id=?", new String[]{Long.toString(_id)}, "datedoc desc");
		if (cursor != null) {
			int index_uid = cursor.getColumnIndex("uid");
			int index_version = cursor.getColumnIndex("version");
			int index_version_ack = cursor.getColumnIndex("version_ack");
			int index_versionPDA = cursor.getColumnIndex("versionPDA");
			int index_versionPDA_ack = cursor.getColumnIndex("versionPDA_ack");
			int index_id = cursor.getColumnIndex("id");
			int index_numdoc = cursor.getColumnIndex("numdoc");
			int index_datedoc = cursor.getColumnIndex("datedoc");
			int index_client_id = cursor.getColumnIndex("client_id");
			int index_agreement30_id = cursor.getColumnIndex("agreement30_id");
			int index_agreement_id = cursor.getColumnIndex("agreement_id");
			int index_distr_point_id = cursor.getColumnIndex("distr_point_id");
			int index_comment = cursor.getColumnIndex("comment");
			int index_comment_closing = cursor.getColumnIndex("comment_closing");
			int index_comment_payment = cursor.getColumnIndex("comment_payment");
			int index_closed_not_full = cursor.getColumnIndex("closed_not_full");
			int index_curator_id = cursor.getColumnIndex("curator_id");
			int index_datecoord = cursor.getColumnIndex("datecoord");
			int index_latitude = cursor.getColumnIndex("latitude");
			int index_longitude = cursor.getColumnIndex("longitude");
			int index_datecreation = cursor.getColumnIndex("datecreation");
			int index_gpsstate = cursor.getColumnIndex("gpsstate");
			int index_gpstype = cursor.getColumnIndex("gpstype");
			int index_gpsaccuracy = cursor.getColumnIndex("gpsaccuracy");
			int index_accept_coord = cursor.getColumnIndex("accept_coord");
			int index_dont_need_send = cursor.getColumnIndex("dont_need_send");
			int index_price_type_id = cursor.getColumnIndex("price_type_id");
			int index_stock_id = cursor.getColumnIndex("stock_id");
			int index_sum = cursor.getColumnIndex("sum_doc");
			int index_sum_shipping = cursor.getColumnIndex("sum_shipping");
			int index_weight = cursor.getColumnIndex("weight_doc");
			int index_trade_type = cursor.getColumnIndex("trade_type");
			int index_bw = cursor.getColumnIndex("bw");
			int index_state = cursor.getColumnIndex("state");
			int index_shipping_type = cursor.getColumnIndex("shipping_type");
			int index_shipping_time = cursor.getColumnIndex("shipping_time");
			int index_shipping_begin_time = cursor.getColumnIndex("shipping_begin_time");
			int index_shipping_end_time = cursor.getColumnIndex("shipping_end_time");
			int index_shipping_date = cursor.getColumnIndex("shipping_date");

			int index_simple_discount_id = cursor.getColumnIndex("simple_discount_id");

			int index_create_client = cursor.getColumnIndex("create_client");
			int index_create_client_surname = cursor.getColumnIndex("create_client_surname");
			int index_create_client_firstname = cursor.getColumnIndex("create_client_firstname");
			int index_create_client_lastname = cursor.getColumnIndex("create_client_lastname");

			//int index_create_client_phone = cursor.getColumnIndex("create_client_phone");

			int index_place_num = cursor.getColumnIndex("place_num");
			int index_card_num = cursor.getColumnIndex("card_num");
			int index_pay_credit = cursor.getColumnIndex("pay_credit");
			//int index_ticket_m = cursor.getColumnIndex("ticket_m");
			//int index_ticket_w = cursor.getColumnIndex("ticket_w");
			int index_quant_m = cursor.getColumnIndex("quant_m");
			int index_quant_w = cursor.getColumnIndex("quant_w");
			int index_quant_mw = cursor.getColumnIndex("quant_mw");

			int index_manager_comment = cursor.getColumnIndex("manager_comment");
			int index_theme_comment = cursor.getColumnIndex("theme_comment");

			int index_phone_num = cursor.getColumnIndex("phone_num");
			int index_old_id = cursor.getColumnIndex("old_id");

			if (cursor.moveToNext()) {
				rec.lines.clear();
				rec.places.clear();
				rec.uid = UUID.fromString(cursor.getString(index_uid));
				rec.version = cursor.getInt(index_version);
				rec.version_ack = cursor.getInt(index_version_ack);
				rec.versionPDA = cursor.getInt(index_versionPDA);
				rec.versionPDA_ack = cursor.getInt(index_versionPDA_ack);
				rec.id = new MyID(cursor.getString(index_id));
				rec.numdoc = new String(cursor.getString(index_numdoc));
				rec.datedoc = new String(cursor.getString(index_datedoc));
				rec.client_id = new MyID(cursor.getString(index_client_id));
				rec.agreement30_id = new MyID(cursor.getString(index_agreement30_id));
				rec.agreement_id = new MyID(cursor.getString(index_agreement_id));
				rec.distr_point_id = new MyID(cursor.getString(index_distr_point_id));
				rec.comment = new String(cursor.getString(index_comment));
				rec.comment_closing = new String(cursor.getString(index_comment_closing));
				if (cursor.getString(index_comment_payment) == null) {
					rec.comment_payment = "";
				} else {
					rec.comment_payment = new String(cursor.getString(index_comment_payment));
				}
				rec.closed_not_full = cursor.getInt(index_closed_not_full);
				rec.curator_id = new MyID(cursor.getString(index_curator_id));
				rec.datecoord = new String(cursor.getString(index_datecoord));
				rec.latitude = cursor.getDouble(index_latitude);
				rec.longitude = cursor.getDouble(index_longitude);
				rec.datecreation = cursor.getString(index_datecreation);
				rec.gpsstate = cursor.getInt(index_gpsstate);
				rec.gpstype = cursor.getInt(index_gpstype);
				rec.gpsaccuracy = cursor.getDouble(index_gpsaccuracy);
				rec.accept_coord = cursor.getInt(index_accept_coord);
				rec.dont_need_send = cursor.getInt(index_dont_need_send);
				rec.price_type_id = new MyID(cursor.getString(index_price_type_id));
				rec.stock_id = new MyID(cursor.getString(index_stock_id));
				rec.sumDoc = cursor.getDouble(index_sum);
				rec.sumShipping = cursor.getDouble(index_sum_shipping);
				rec.weightDoc = cursor.getDouble(index_weight);
				rec.trade_type = E_TRADE_TYPE_fromInt(cursor.getInt(index_trade_type));
				rec.bw = E_BW_fromInt(cursor.getInt(index_bw));
				rec.state = E_ORDER_STATE.fromInt(cursor.getInt(index_state));

				rec.shipping_type = cursor.getInt(index_shipping_type);
				rec.shipping_time = cursor.getInt(index_shipping_time);
				rec.shipping_begin_time = cursor.getString(index_shipping_begin_time);
				if (rec.shipping_begin_time == null) {
					rec.shipping_begin_time = "0000";
				}
				rec.shipping_end_time = cursor.getString(index_shipping_end_time);
				if (rec.shipping_end_time == null) {
					rec.shipping_end_time = "2359";
				}
				rec.shipping_date = cursor.getString(index_shipping_date);
				if (MySingleton.getInstance().Common.PHARAOH) {
					rec.simple_discount_id = new MyID(cursor.getString(index_simple_discount_id));
					rec.create_client = cursor.getInt(index_create_client);
				} else {
					rec.simple_discount_id = new MyID();
					rec.create_client = 0;
				}
				if (rec.create_client == 1) {
					rec.create_client_surname = cursor.getString(index_create_client_surname);
					rec.create_client_firstname = cursor.getString(index_create_client_firstname);
					rec.create_client_lastname = cursor.getString(index_create_client_lastname);
					//rec.create_client_phone=cursor.getString(index_create_client_phone);
				} else {
					rec.create_client_surname = "";
					rec.create_client_firstname = "";
					rec.create_client_lastname = "";
					//rec.create_client_phone="";
				}
				//rec.place_num=cursor.getInt(index_place_num);
				rec.stuff_places = "";
				rec.card_num = cursor.getInt(index_card_num);
				rec.pay_credit = cursor.getDouble(index_pay_credit);
				//rec.ticket_m=cursor.getDouble(index_ticket_m);
				//rec.ticket_w=cursor.getDouble(index_ticket_w);
				//rec.quant_m=cursor.getInt(index_quant_m);
				//rec.quant_w=cursor.getInt(index_quant_w);
				rec.quant_mw = cursor.getInt(index_quant_mw);

				rec.manager_comment = cursor.getString(index_manager_comment);
				rec.theme_comment = cursor.getString(index_theme_comment);
				rec.phone_num = cursor.getString(index_phone_num);
				rec.old_id = cursor.getInt(index_old_id);

				cursor.close();

				Cursor cursor_lines = contentResolver.query(MTradeContentProvider.ORDERS_LINES_COMPLEMENTED_CONTENT_URI, null, "order_id=?", new String[]{Long.toString(_id)}, "ordersLines._id ASC");
				if (cursor_lines != null) {
					//int index_order_id = cursor_lines.getColumnIndex("order_id");
					int index_nomenclature_id = cursor_lines.getColumnIndex("nomenclature_id");
					int index_quantity_requested = cursor_lines.getColumnIndex("quantity_requested");
					int index_quantity = cursor_lines.getColumnIndex("quantity");
					int index_price = cursor_lines.getColumnIndex("price");
					int index_total = cursor_lines.getColumnIndex("total");
					int index_discount = cursor_lines.getColumnIndex("discount");
					int index_k = cursor_lines.getColumnIndex("k");
					int index_ed = cursor_lines.getColumnIndex("ed");
					//int index_nomenclature_descr=cursor_lines.getColumnIndex(MTradeContentProvider.NOMENCLATURE_DESCR_COLUMN);
					int index_nomenclature_descr = cursor_lines.getColumnIndex("nomencl_descr");
					int index_nomenclature_flags = cursor_lines.getColumnIndex("flags");
					int index_weight_k_1 = cursor_lines.getColumnIndex("nomencl_weight_k_1");
					int index_nomencl_node_id = cursor_lines.getColumnIndex("nomencl_node_id");
					int index_line_shipping_time = cursor_lines.getColumnIndex("shipping_time");
					int index_line_comment_in_line = cursor_lines.getColumnIndex("comment_in_line");

					int index_lineno = cursor_lines.getColumnIndex("lineno");

					while (cursor_lines.moveToNext()) {
						OrderLineRecord orderLine = new OrderLineRecord();
						orderLine.nomenclature_id = new MyID(cursor_lines.getString(index_nomenclature_id));

						orderLine.quantity_requested = cursor_lines.getDouble(index_quantity_requested);
						orderLine.quantity = cursor_lines.getDouble(index_quantity);
						orderLine.price = cursor_lines.getDouble(index_price);
						orderLine.total = cursor_lines.getDouble(index_total);
						orderLine.discount = cursor_lines.getDouble(index_discount);
						orderLine.k = cursor_lines.getDouble(index_k);
						orderLine.ed = cursor_lines.getString(index_ed);
						orderLine.stuff_nomenclature = cursor_lines.getString(index_nomenclature_descr);
						orderLine.stuff_nomenclature_flags = cursor_lines.getInt(index_nomenclature_flags);
						orderLine.stuff_weight_k_1 = cursor_lines.getDouble(index_weight_k_1);
						//orderLine.stuff_nomenclature_node_group_id=cursor_lines.getString(index_nomencl_node_id);
						orderLine.shipping_time = cursor_lines.getString(index_line_shipping_time);
						orderLine.comment_in_line = cursor_lines.getString(index_line_comment_in_line);
						orderLine.lineno = cursor_lines.getInt(index_lineno);

						rec.lines.add(orderLine);
					}

					cursor_lines.close();
				}

				StringBuilder sb = new StringBuilder();

				//ArrayList list=new ArrayList<String>();
				cursor = contentResolver.query(MTradeContentProvider.ORDERS_PLACES_CONTENT_URI, null, "order_id=?", new String[]{Long.toString(_id)}, null);
				int indexPlaceId = cursor.getColumnIndex("place_id");
				while (cursor.moveToNext()) {
					OrderPlaceRecord placeRecord = new OrderPlaceRecord();
					placeRecord.place_id = new MyID(cursor.getString(indexPlaceId));

					Cursor placesCursor = contentResolver.query(MTradeContentProvider.PLACES_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{placeRecord.place_id.toString()}, null);
					if (placesCursor.moveToFirst()) {
						placeRecord.stuff_descr = placesCursor.getString(0);
					} else {
						placeRecord.stuff_descr = "?";
					}
					placesCursor.close();
					rec.places.add(placeRecord);

					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(placeRecord.stuff_descr);
				}
				rec.stuff_places = sb.toString();
				cursor.close();


				return true;
			}
		}
		return false;
	}

	static long SavePaymentSQL(ContentResolver contentResolver, MyDatabase myBase, CashPaymentRecord rec, long _id) {
		boolean bOrderUpdated = false;

		ContentValues cv = new ContentValues();

		cv.put("uid", rec.uid.toString());
		cv.put("version", rec.version);
		cv.put("version_ack", rec.version_ack);
		cv.put("versionPDA", rec.versionPDA);
		cv.put("versionPDA_ack", rec.versionPDA_ack);
		cv.put("id", rec.id.toString());
		cv.put("numdoc", rec.numdoc);
		cv.put("datedoc", rec.datedoc);
		cv.put("client_id", rec.client_id.toString());
		cv.put("agreement_id", rec.agreement_id.toString());
		cv.put("comment", rec.comment);
		cv.put("comment_closing", rec.comment_closing);
		cv.put("curator_id", rec.manager_id.toString());
		cv.put("manager_descr", rec.stuff_manager_name);
		cv.put("organization_descr", rec.stuff_organization_name);
		cv.put("state", rec.state.value());
		cv.put("vicarious_power_id", rec.vicarious_power_id.toString());
		cv.put("vicarious_power_descr", rec.vicarious_power_descr);
		cv.put("sum_doc", rec.sumDoc);

		cv.put("datecoord", rec.datecoord);
		cv.put("latitude", rec.latitude);
		cv.put("longitude", rec.longitude);
		cv.put("datecreation", rec.datecreation);
		cv.put("gpsstate", rec.gpsstate);
		cv.put("gpstype", rec.gpstype);
		cv.put("gpsaccuracy", rec.gpsaccuracy);
		cv.put("accept_coord", rec.accept_coord);

		cv.put("distr_point_id", rec.distr_point_id.toString());

		// это новый документ, либо просто id не указали
		// попытаемся найти по uid
		if (_id == 0) {
			Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{rec.uid.toString()}, "datedoc desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int _idIndex = cursor.getColumnIndex("_id");
					_id = cursor.getLong(_idIndex);
				}
				cursor.close();
			}
		}
		int rowsUpdated = contentResolver.update(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
		if (rowsUpdated > 0)
			bOrderUpdated = true;
		// новый заказ, добавим его
		if (bOrderUpdated == false) {
			Uri newUri = contentResolver.insert(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, cv);
			_id = ContentUris.parseId(newUri);
		}
		return _id;
	}

	static long SaveRefundSQL(ContentResolver contentResolver, RefundRecord rec, long _id) {

		boolean bRefundUpdated = false;

		ContentValues cv = new ContentValues();

		cv.put("uid", rec.uid.toString());
		cv.put("version", rec.version);
		cv.put("version_ack", rec.version_ack);
		cv.put("versionPDA", rec.versionPDA);
		cv.put("versionPDA_ack", rec.versionPDA_ack);
		cv.put("id", rec.id.toString());
		cv.put("numdoc", rec.numdoc);
		cv.put("datedoc", rec.datedoc);
		cv.put("client_id", rec.client_id.toString());
		cv.put("agreement_id", rec.agreement_id.toString());
		cv.put("distr_point_id", rec.distr_point_id.toString());
		cv.put("comment", rec.comment);
		cv.put("comment_closing", rec.comment_closing);
		cv.put("closed_not_full", rec.closed_not_full);
		cv.put("state", rec.state.value());
		cv.put("curator_id", rec.curator_id.toString());
		cv.put("bw", E_BW_toInt(rec.bw));
		cv.put("trade_type", E_TRADE_TYPE_toInt(rec.trade_type));
		cv.put("datecoord", rec.datecoord);
		cv.put("latitude", rec.latitude);
		cv.put("longitude", rec.longitude);
		cv.put("datecreation", rec.datecreation);
		cv.put("gpsstate", rec.gpsstate);
		cv.put("gpsaccuracy", rec.gpsaccuracy);
		cv.put("accept_coord", rec.accept_coord);
		cv.put("dont_need_send", rec.dont_need_send);
		cv.put("stock_id", rec.stock_id.toString());
		cv.put("weight_doc", rec.weightDoc);
		cv.put("shipping_type", rec.shipping_type);
		//cv.put("shipping_time",rec.shipping_time);
		//cv.put("shipping_begin_time",rec.shipping_begin_time);
		//cv.put("shipping_end_time",rec.shipping_end_time);
		//cv.put("shipping_date",rec.shipping_date);


		// это новый возврат, либо просто id не указали
		// попытаемся найти по uid
		if (_id == 0) {
			Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{rec.uid.toString()}, "datedoc desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int _idIndex = cursor.getColumnIndex("_id");
					_id = cursor.getLong(_idIndex);
				}
				cursor.close();
			}
		}
		int rowsUpdated = contentResolver.update(MTradeContentProvider.REFUNDS_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
		if (rowsUpdated > 0)
			bRefundUpdated = true;
		// новый заказ, добавим его
		if (bRefundUpdated == false) {
			if (rec.state == E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED) {
				Uri newUri = contentResolver.insert(MTradeContentProvider.REFUNDS_SILENT_CONTENT_URI, cv);
				_id = ContentUris.parseId(newUri);
			} else {
				Uri newUri = contentResolver.insert(MTradeContentProvider.REFUNDS_CONTENT_URI, cv);
				_id = ContentUris.parseId(newUri);
			}
		}
		// Удалим старые строки, если они были с таким _id
		contentResolver.delete(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, "refund_id=?", new String[]{Long.toString(_id)});
		//
		if (rec.lines.size() > 0) {
			ContentValues[] values = new ContentValues[rec.lines.size()];
			for (RefundLineRecord refundLine : rec.lines) {
				ContentValues cvl = new ContentValues();
				cvl.put("refund_id", Long.toString(_id));
				cvl.put("nomenclature_id", refundLine.nomenclature_id.toString());
				cvl.put("client_id", rec.client_id.toString());
				cvl.put("quantity_requested", refundLine.quantity_requested);
				cvl.put("quantity", refundLine.quantity);
				cvl.put("k", refundLine.k);
				cvl.put("ed", refundLine.ed);
				cvl.put("comment_in_line", refundLine.comment_in_line);

				values[rec.lines.indexOf(refundLine)] = cvl;
			}
			contentResolver.bulkInsert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, values);
		}
		return _id;
	}

	static boolean ReadPaymentBy_Id(ContentResolver contentResolver, CashPaymentRecord rec, long _id) {
		if (_id == 0) {
			return false;
		}
		Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, null, "cash_payments._id=?", new String[]{Long.toString(_id)}, "datedoc desc");
		if (cursor != null) {
			int index_uid = cursor.getColumnIndex("uid");
			int index_version = cursor.getColumnIndex("version");
			int index_version_ack = cursor.getColumnIndex("version_ack");
			int index_versionPDA = cursor.getColumnIndex("versionPDA");
			int index_versionPDA_ack = cursor.getColumnIndex("versionPDA_ack");
			int index_id = cursor.getColumnIndex("id");
			int index_numdoc = cursor.getColumnIndex("numdoc");
			int index_datedoc = cursor.getColumnIndex("datedoc");
			int index_client_id = cursor.getColumnIndex("client_id");
			int index_agreement_id = cursor.getColumnIndex("agreement_id");
			int index_comment = cursor.getColumnIndex("comment");
			int index_comment_closing = cursor.getColumnIndex("comment_closing");
			int index_curator_id = cursor.getColumnIndex("curator_id");
			int index_manager_descr = cursor.getColumnIndex("manager_descr");
			int index_organization_name = cursor.getColumnIndex("organization_descr");
			int index_sum = cursor.getColumnIndex("sum_doc");
			int index_state = cursor.getColumnIndex("state");
			int index_vicarious_power_id = cursor.getColumnIndex("vicarious_power_id");
			int index_vicarious_power_descr = cursor.getColumnIndex("vicarious_power_descr");

			int index_datecoord = cursor.getColumnIndex("datecoord");
			int index_latitude = cursor.getColumnIndex("latitude");
			int index_longitude = cursor.getColumnIndex("longitude");
			int index_datecreation = cursor.getColumnIndex("datecreation");
			int index_gpsstate = cursor.getColumnIndex("gpsstate");
			int index_gpstype = cursor.getColumnIndex("gpstype");
			int index_gpsaccuracy = cursor.getColumnIndex("gpsaccuracy");
			int index_accept_coord = cursor.getColumnIndex("accept_coord");

			int index_distr_point_id = cursor.getColumnIndex("distr_point_id");

			if (cursor.moveToNext()) {
				rec.uid = UUID.fromString(cursor.getString(index_uid));
				rec.version = cursor.getInt(index_version);
				rec.version_ack = cursor.getInt(index_version_ack);
				rec.versionPDA = cursor.getInt(index_versionPDA);
				rec.versionPDA_ack = cursor.getInt(index_versionPDA_ack);
				rec.id = new MyID(cursor.getString(index_id));
				rec.numdoc = new String(cursor.getString(index_numdoc));
				rec.datedoc = new String(cursor.getString(index_datedoc));
				rec.client_id = new MyID(cursor.getString(index_client_id));

				rec.agreement_id = new MyID(cursor.getString(index_agreement_id));
				rec.comment = new String(cursor.getString(index_comment));
				rec.comment_closing = new String(cursor.getString(index_comment_closing));
				rec.manager_id = new MyID(cursor.getString(index_curator_id));

				rec.stuff_manager_name = cursor.getString(index_manager_descr);
				// в каком-то случае тут может быть null
				rec.stuff_organization_name = StringUtils.defaultIfBlank(cursor.getString(index_organization_name), "");

				rec.sumDoc = cursor.getDouble(index_sum);
				rec.state = E_PAYMENT_STATE.fromInt(cursor.getInt(index_state));

				rec.vicarious_power_id = new MyID(cursor.getString(index_vicarious_power_id));
				rec.vicarious_power_descr = cursor.getString(index_vicarious_power_descr);

				rec.datecoord = new String(cursor.getString(index_datecoord));
				rec.latitude = cursor.getDouble(index_latitude);
				rec.longitude = cursor.getDouble(index_longitude);
				rec.datecreation = cursor.getString(index_datecreation);
				rec.gpsstate = cursor.getInt(index_gpsstate);
				rec.gpstype = cursor.getInt(index_gpstype);
				rec.gpsaccuracy = cursor.getDouble(index_gpsaccuracy);
				rec.accept_coord = cursor.getInt(index_accept_coord);

				rec.distr_point_id = new MyID(cursor.getString(index_distr_point_id));


				cursor.close();

				return true;
			}
		}
		return false;
	}


	static boolean ReadRefundBy_Id(ContentResolver contentResolver, RefundRecord rec, long _id) {

		Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, null, "_id=?", new String[]{Long.toString(_id)}, "datedoc desc");
		if (cursor != null) {
			int index_uid = cursor.getColumnIndex("uid");
			int index_version = cursor.getColumnIndex("version");
			int index_version_ack = cursor.getColumnIndex("version_ack");
			int index_versionPDA = cursor.getColumnIndex("versionPDA");
			int index_versionPDA_ack = cursor.getColumnIndex("versionPDA_ack");
			int index_id = cursor.getColumnIndex("id");
			int index_numdoc = cursor.getColumnIndex("numdoc");
			int index_datedoc = cursor.getColumnIndex("datedoc");
			int index_client_id = cursor.getColumnIndex("client_id");
			int index_agreement_id = cursor.getColumnIndex("agreement_id");
			int index_distr_point_id = cursor.getColumnIndex("distr_point_id");
			int index_comment = cursor.getColumnIndex("comment");
			int index_comment_closing = cursor.getColumnIndex("comment_closing");
			//int index_comment_payment = cursor.getColumnIndex("comment_payment");
			int index_closed_not_full = cursor.getColumnIndex("closed_not_full");
			int index_curator_id = cursor.getColumnIndex("curator_id");
			int index_datecoord = cursor.getColumnIndex("datecoord");
			int index_latitude = cursor.getColumnIndex("latitude");
			int index_longitude = cursor.getColumnIndex("longitude");
			int index_datecreation = cursor.getColumnIndex("datecreation");
			int index_gpsstate = cursor.getColumnIndex("gpsstate");
			int index_gpsaccuracy = cursor.getColumnIndex("gpsaccuracy");
			int index_accept_coord = cursor.getColumnIndex("accept_coord");
			int index_dont_need_send = cursor.getColumnIndex("dont_need_send");
			//int index_price_type_id = cursor.getColumnIndex("price_type_id");
			int index_stock_id = cursor.getColumnIndex("stock_id");
			//int index_sum = cursor.getColumnIndex("sum_doc");
			int index_weight = cursor.getColumnIndex("weight_doc");
			int index_trade_type = cursor.getColumnIndex("trade_type");
			int index_bw = cursor.getColumnIndex("bw");
			int index_state = cursor.getColumnIndex("state");
			int index_shipping_type = cursor.getColumnIndex("shipping_type");
			//int index_shipping_time = cursor.getColumnIndex("shipping_time");
			//int index_shipping_begin_time = cursor.getColumnIndex("shipping_begin_time");
			//int index_shipping_end_time = cursor.getColumnIndex("shipping_end_time");
			//int index_shipping_date = cursor.getColumnIndex("shipping_date");

			//int index_simple_discount_id = cursor.getColumnIndex("simple_discount_id");

			//int index_create_client = cursor.getColumnIndex("create_client");
			//int index_create_client_surname = cursor.getColumnIndex("create_client_surname");
			//int index_create_client_firstname = cursor.getColumnIndex("create_client_firstname");
			//int index_create_client_lastname = cursor.getColumnIndex("create_client_lastname");

			//int index_create_client_phone = cursor.getColumnIndex("create_client_phone");

			//int index_place_num = cursor.getColumnIndex("place_num");
			//int index_card_num = cursor.getColumnIndex("card_num");
			//int index_pay_credit = cursor.getColumnIndex("pay_credit");
			//int index_ticket_m = cursor.getColumnIndex("ticket_m");
			//int index_ticket_w = cursor.getColumnIndex("ticket_w");
			//int index_quant_m = cursor.getColumnIndex("quant_m");
			//int index_quant_w = cursor.getColumnIndex("quant_w");
			//int index_quant_mw = cursor.getColumnIndex("quant_mw");

			//int index_manager_comment=cursor.getColumnIndex("manager_comment");
			//int index_theme_comment=cursor.getColumnIndex("theme_comment");

			//int index_phone_num=cursor.getColumnIndex("phone_num");

			if (cursor.moveToNext()) {
				rec.lines.clear();
				//rec.places.clear();
				rec.uid = UUID.fromString(cursor.getString(index_uid));
				rec.version = cursor.getInt(index_version);
				rec.version_ack = cursor.getInt(index_version_ack);
				rec.versionPDA = cursor.getInt(index_versionPDA);
				rec.versionPDA_ack = cursor.getInt(index_versionPDA_ack);
				rec.id = new MyID(cursor.getString(index_id));
				rec.numdoc = new String(cursor.getString(index_numdoc));
				rec.datedoc = new String(cursor.getString(index_datedoc));
				rec.client_id = new MyID(cursor.getString(index_client_id));

				rec.agreement_id = new MyID(cursor.getString(index_agreement_id));
				rec.distr_point_id = new MyID(cursor.getString(index_distr_point_id));
				rec.comment = new String(cursor.getString(index_comment));
				rec.comment_closing = new String(cursor.getString(index_comment_closing));
				//if (cursor.getString(index_comment_payment)==null)
				//{
				//	rec.comment_payment="";
				//} else
				//{
				//	rec.comment_payment=new String(cursor.getString(index_comment_payment));
				//}
				rec.closed_not_full = cursor.getInt(index_closed_not_full);
				rec.curator_id = new MyID(cursor.getString(index_curator_id));
				rec.datecoord = new String(cursor.getString(index_datecoord));
				rec.latitude = cursor.getDouble(index_latitude);
				rec.longitude = cursor.getDouble(index_longitude);
				rec.datecreation = cursor.getString(index_datecreation);
				rec.gpsstate = cursor.getInt(index_gpsstate);
				rec.gpsaccuracy = cursor.getDouble(index_gpsaccuracy);
				rec.accept_coord = cursor.getInt(index_accept_coord);
				rec.dont_need_send = cursor.getInt(index_dont_need_send);
				//rec.price_type_id=new MyID(cursor.getString(index_price_type_id));
				rec.stock_id = new MyID(cursor.getString(index_stock_id));
				//rec.sumDoc=cursor.getDouble(index_sum);
				rec.weightDoc = cursor.getDouble(index_weight);
				rec.trade_type = E_TRADE_TYPE_fromInt(cursor.getInt(index_trade_type));
				rec.bw = E_BW_fromInt(cursor.getInt(index_bw));
				rec.state = E_REFUND_STATE.fromInt(cursor.getInt(index_state));

				rec.shipping_type = cursor.getInt(index_shipping_type);
				//rec.shipping_time=cursor.getInt(index_shipping_time);
				//rec.shipping_begin_time=cursor.getString(index_shipping_begin_time);
				//if (rec.shipping_begin_time==null)
				//{
				//	rec.shipping_begin_time="0000";
				//}
				//rec.shipping_end_time=cursor.getString(index_shipping_end_time);
				//if (rec.shipping_end_time==null)
				//{
				//	rec.shipping_end_time="2359";
				//}
				//rec.shipping_date=cursor.getString(index_shipping_date);
				//if (Common.PHARAOH)
				//{
				//	rec.simple_discount_id=new MyID(cursor.getString(index_simple_discount_id));
				//	rec.create_client=cursor.getInt(index_create_client);
				//} else
				//{
				//	rec.simple_discount_id=new MyID();
				//	rec.create_client=0;
				//}
				//if (rec.create_client==1)
				//{
				//	rec.create_client_surname=cursor.getString(index_create_client_surname);
				//	rec.create_client_firstname=cursor.getString(index_create_client_firstname);
				//	rec.create_client_lastname=cursor.getString(index_create_client_lastname);
				//	//rec.create_client_phone=cursor.getString(index_create_client_phone);
				//} else
				//{
				//	rec.create_client_surname="";
				//	rec.create_client_firstname="";
				//	rec.create_client_lastname="";
				//	//rec.create_client_phone="";
				//}
				//rec.place_num=cursor.getInt(index_place_num);
				//rec.stuff_places="";
				//rec.card_num=cursor.getInt(index_card_num);
				//rec.pay_credit=cursor.getDouble(index_pay_credit);
				//rec.ticket_m=cursor.getDouble(index_ticket_m);
				//rec.ticket_w=cursor.getDouble(index_ticket_w);
				//rec.quant_m=cursor.getInt(index_quant_m);
				//rec.quant_w=cursor.getInt(index_quant_w);
				//rec.quant_mw=cursor.getInt(index_quant_mw);

				//rec.manager_comment=cursor.getString(index_manager_comment);
				//rec.theme_comment=cursor.getString(index_theme_comment);
				//rec.phone_num=cursor.getString(index_phone_num);

				cursor.close();

				Cursor cursor_lines = contentResolver.query(MTradeContentProvider.REFUNDS_LINES_COMPLEMENTED_CONTENT_URI, null, "refund_id=?", new String[]{Long.toString(_id)}, "refundsLines._id ASC");
				if (cursor_lines != null) {
					int index_nomenclature_id = cursor_lines.getColumnIndex("nomenclature_id");
					int index_quantity_requested = cursor_lines.getColumnIndex("quantity_requested");
					int index_quantity = cursor_lines.getColumnIndex("quantity");
					//int index_price = cursor_lines.getColumnIndex("price");
					//int index_total = cursor_lines.getColumnIndex("total");
					//int index_discount = cursor_lines.getColumnIndex("discount");
					int index_k = cursor_lines.getColumnIndex("k");
					int index_ed = cursor_lines.getColumnIndex("ed");
					int index_nomenclature_descr = cursor_lines.getColumnIndex("nomencl_descr");
					int index_nomenclature_flags = cursor_lines.getColumnIndex("flags");
					int index_weight_k_1 = cursor_lines.getColumnIndex("nomencl_weight_k_1");
					int index_nomencl_node_id = cursor_lines.getColumnIndex("nomencl_node_id");
					int index_line_shipping_time = cursor_lines.getColumnIndex("shipping_time");
					int index_line_comment_in_line = cursor_lines.getColumnIndex("comment_in_line");

					while (cursor_lines.moveToNext()) {
						RefundLineRecord refundLine = new RefundLineRecord();
						refundLine.nomenclature_id = new MyID(cursor_lines.getString(index_nomenclature_id));

						refundLine.quantity_requested = cursor_lines.getDouble(index_quantity_requested);
						refundLine.quantity = cursor_lines.getDouble(index_quantity);
						//orderLine.price=cursor_lines.getDouble(index_price);
						//orderLine.total=cursor_lines.getDouble(index_total);
						//orderLine.discount=cursor_lines.getDouble(index_discount);
						refundLine.k = cursor_lines.getDouble(index_k);
						refundLine.ed = cursor_lines.getString(index_ed);
						refundLine.stuff_nomenclature = cursor_lines.getString(index_nomenclature_descr);
						refundLine.stuff_nomenclature_flags = cursor_lines.getInt(index_nomenclature_flags);
						refundLine.stuff_weight_k_1 = cursor_lines.getDouble(index_weight_k_1);
						//orderLine.shipping_time=cursor_lines.getString(index_line_shipping_time);
						refundLine.comment_in_line = cursor_lines.getString(index_line_comment_in_line);

						rec.lines.add(refundLine);
					}

					cursor_lines.close();
				}

				return true;
			}
		}
		return false;
	}

	static long SaveDistribsSQL(ContentResolver contentResolver, DistribsRecord rec, long _id) {

		boolean bOrderUpdated = false;

		ContentValues cv = new ContentValues();

		cv.put("uid", rec.uid.toString());
		cv.put("version", rec.version);
		cv.put("version_ack", rec.version_ack);
		cv.put("versionPDA", rec.versionPDA);
		cv.put("versionPDA_ack", rec.versionPDA_ack);
		cv.put("id", rec.id.toString());
		cv.put("numdoc", rec.numdoc);
		cv.put("datedoc", rec.datedoc);
		cv.put("client_id", rec.client_id.toString());
		cv.put("distr_point_id", rec.distr_point_id.toString());
		cv.put("curator_id", rec.curator_id.toString());
		cv.put("comment", rec.comment);
		cv.put("state", rec.state.value());
		cv.put("datecoord", rec.datecoord);
		cv.put("latitude", rec.latitude);
		cv.put("longitude", rec.longitude);
		cv.put("gpsstate", rec.gpsstate);
		cv.put("gpsaccuracy", rec.gpsaccuracy);
		cv.put("accept_coord", rec.accept_coord);

		// это новый заказ, либо просто id не указали
		// попытаемся найти по uid
		if (_id == 0) {
			Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{rec.uid.toString()}, "datedoc desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int _idIndex = cursor.getColumnIndex("_id");
					_id = cursor.getLong(_idIndex);
				}
				cursor.close();
			}
		}
		int rowsUpdated = contentResolver.update(MTradeContentProvider.DISTRIBS_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
		if (rowsUpdated > 0)
			bOrderUpdated = true;
		// новый заказ, добавим его
		if (bOrderUpdated == false) {
			Uri newUri = contentResolver.insert(MTradeContentProvider.DISTRIBS_CONTENT_URI, cv);
			_id = ContentUris.parseId(newUri);
		}
		// Удалим старые строки, если они были с таким _id
		contentResolver.delete(MTradeContentProvider.DISTRIBS_LINES_CONTENT_URI, "distribs_id=?", new String[]{Long.toString(_id)});
		//
		if (rec.lines.size() > 0) {
			ContentValues[] values = new ContentValues[rec.lines.size()];
			for (DistribsLineRecord distribsLine : rec.lines) {
				ContentValues cvl = new ContentValues();
				cvl.put("distribs_id", Long.toString(_id));
				cvl.put("contract_id", distribsLine.distribs_contract_id.toString());
				cvl.put("quantity", distribsLine.quantity);

				values[rec.lines.indexOf(distribsLine)] = cvl;
			}
			contentResolver.bulkInsert(MTradeContentProvider.DISTRIBS_LINES_CONTENT_URI, values);
		}
		return _id;
	}

	static boolean ReadDistribsBy_Id(ContentResolver contentResolver, DistribsRecord rec, long _id) {

		Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, null, "_id=?", new String[]{Long.toString(_id)}, "datedoc desc");
		if (cursor != null) {
			int index_uid = cursor.getColumnIndex("uid");
			int index_version = cursor.getColumnIndex("version");
			int index_version_ack = cursor.getColumnIndex("version_ack");
			int index_versionPDA = cursor.getColumnIndex("versionPDA");
			int index_versionPDA_ack = cursor.getColumnIndex("versionPDA_ack");
			int index_id = cursor.getColumnIndex("id");
			int index_numdoc = cursor.getColumnIndex("numdoc");
			int index_datedoc = cursor.getColumnIndex("datedoc");
			int index_client_id = cursor.getColumnIndex("client_id");
			int index_curator_id = cursor.getColumnIndex("curator_id");
			int index_distr_point_id = cursor.getColumnIndex("distr_point_id");
			int index_comment = cursor.getColumnIndex("comment");
			int index_datecoord = cursor.getColumnIndex("datecoord");
			int index_latitude = cursor.getColumnIndex("latitude");
			int index_longitude = cursor.getColumnIndex("longitude");
			int index_gpsstate = cursor.getColumnIndex("gpsstate");
			int index_gpstype = cursor.getColumnIndex("gpstype");
			int index_accept_coord = cursor.getColumnIndex("accept_coord");
			int index_state = cursor.getColumnIndex("state");

			if (cursor.moveToNext()) {
				rec.lines.clear();
				rec.uid = UUID.fromString(cursor.getString(index_uid));
				rec.version = cursor.getInt(index_version);
				rec.version_ack = cursor.getInt(index_version_ack);
				rec.versionPDA = cursor.getInt(index_versionPDA);
				rec.versionPDA_ack = cursor.getInt(index_versionPDA_ack);
				rec.id = new MyID(cursor.getString(index_id));
				rec.numdoc = new String(cursor.getString(index_numdoc));
				rec.datedoc = new String(cursor.getString(index_datedoc));
				rec.client_id = new MyID(cursor.getString(index_client_id));
				rec.curator_id = new MyID(cursor.getString(index_curator_id));

				rec.distr_point_id = new MyID(cursor.getString(index_distr_point_id));
				rec.comment = new String(cursor.getString(index_comment));
				rec.datecoord = new String(cursor.getString(index_datecoord));
				rec.latitude = cursor.getDouble(index_latitude);
				rec.longitude = cursor.getDouble(index_longitude);
				rec.accept_coord = cursor.getInt(index_accept_coord);
				rec.gpsstate = cursor.getInt(index_gpsstate);
				rec.gpstype = cursor.getInt(index_gpstype);
				rec.state = E_DISTRIBS_STATE.fromInt(cursor.getInt(index_state));

				cursor.close();

				Cursor cursor_lines = contentResolver.query(MTradeContentProvider.DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI, null, "distribs_id=?", new String[]{Long.toString(_id)}, "distribsLines._id ASC");
				if (cursor_lines != null) {
					//int index_order_id = cursor_lines.getColumnIndex("order_id");
					int index_distribs_contract_id = cursor_lines.getColumnIndex("contract_id");
					int index_stuff_distribs_contract = cursor_lines.getColumnIndex("contract_descr");
					int index_quantity = cursor_lines.getColumnIndex("quantity");

					while (cursor_lines.moveToNext()) {
						DistribsLineRecord distribsLine = new DistribsLineRecord();
						distribsLine.distribs_contract_id = new MyID(cursor_lines.getString(index_distribs_contract_id));
						distribsLine.quantity = cursor_lines.getDouble(index_quantity);
						distribsLine.stuff_distribs_contract = cursor_lines.getString(index_stuff_distribs_contract);

						rec.lines.add(distribsLine);
					}

					cursor_lines.close();
				}

				return true;
			}
		}
		return false;
	}


	static boolean ReadMessageBy_Id(ContentResolver contentResolver, MyDatabase myBase, MessageRecord rec, long _id) {

		Cursor cursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, null, "_id=?", new String[]{Long.toString(_id)}, "datetime desc");

		if (cursor != null) {
			int index_uid = cursor.getColumnIndex("uid");
			int index_senderId = cursor.getColumnIndex("sender_id");
			int index_receiverId = cursor.getColumnIndex("receiver_id");
			int index_text = cursor.getColumnIndex("text");
			int index_fname = cursor.getColumnIndex("fname");
			int index_datetime = cursor.getColumnIndex("datetime");
			int index_acknowledged = cursor.getColumnIndex("acknowledged");
			int index_ver = cursor.getColumnIndex("ver");

			int type_idx_ver = cursor.getColumnIndex("type_idx");
			int index_clientId = cursor.getColumnIndex("client_id");
			int index_date1 = cursor.getColumnIndex("date1");
			int index_date2 = cursor.getColumnIndex("date2");
			int index_report = cursor.getColumnIndex("report");

			if (cursor.moveToFirst()) {
				rec.uid = UUID.fromString(cursor.getString(index_uid));
				rec.sender_id = new MyID(cursor.getString(index_senderId));
				rec.receiver_id = new MyID(cursor.getString(index_receiverId));
				rec.text = cursor.getString(index_text);
				rec.fname = cursor.getString(index_fname);
				rec.datetime = cursor.getString(index_datetime);
				rec.acknowledged = cursor.getInt(index_acknowledged);
				rec.ver = cursor.getInt(index_ver);

				rec.type_idx = cursor.getInt(type_idx_ver);
				rec.client_id = new MyID(cursor.getString(index_clientId));
				rec.date1 = cursor.getString(index_date1);
				rec.date2 = cursor.getString(index_date2);
				rec.report = cursor.getString(index_report);

				if (rec.date1 == null)
					rec.date1 = "";
				if (rec.date2 == null)
					rec.date2 = "";
				if (rec.report == null)
					rec.report = "";
			}
			cursor.close();
			return true;
		}
		return false;
	}

	static void SaveMessageSQL(ContentResolver contentResolver, MyDatabase myBase, MessageRecord rec, long _id) {

		boolean bMessageUpdated = false;

		// это новый заказ, либо просто id не указали
		// попытаемся найти по uid
		if (_id == 0) {
			Cursor cursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{rec.uid.toString()}, "datetime desc");
			if (cursor != null) {
				if (cursor.moveToNext()) {
					int _idIndex = cursor.getColumnIndex("_id");
					_id = cursor.getLong(_idIndex);
				}
				cursor.close();
			}
		}

		ContentValues cv = new ContentValues();

		cv.put("uid", rec.uid.toString());
		cv.put("sender_id", rec.sender_id.toString());
		cv.put("receiver_id", rec.receiver_id.toString());
		cv.put("text", rec.text);
		cv.put("fname", rec.fname);
		cv.put("datetime", rec.datetime);
		cv.put("acknowledged", rec.acknowledged);
		cv.put("ver", rec.ver);
		cv.put("type_idx", rec.type_idx);
		cv.put("date1", rec.date1);
		cv.put("date2", rec.date2);
		cv.put("client_id", rec.client_id.toString());
		cv.put("nomenclature_id", rec.nomenclature_id.toString());

		int rowsUpdated = contentResolver.update(MTradeContentProvider.MESSAGES_CONTENT_URI, cv, "_id=?", new String[]{Long.toString(_id)});
		if (rowsUpdated > 0)
			bMessageUpdated = true;
		// новый заказ, добавим его
		if (bMessageUpdated == false) {
			Uri newUri = contentResolver.insert(MTradeContentProvider.MESSAGES_CONTENT_URI, cv);
			_id = ContentUris.parseId(newUri);
		}

	}


	static boolean LoadOrdersUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uids) {
		int orders_system_version;
		int nom_idx;
		int pos;

		OrderRecord or = new OrderRecord();
		OrderLineRecord line = new OrderLineRecord();

		boolean bRecordValid = false;

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 16) {
					if (bRecordValid) {
						int _id = 0;
						or.sumDoc = or.GetOrderSum(null, false);
						or.weightDoc = TextDatabase.GetOrderWeight(contentResolver, or, null, false);
						or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
						SaveOrderSQL(contentResolver, or, _id, 0);
						uids.add(or.uid);
					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_orders.put(or.uid, or.version);
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						orders_system_version = 0;
						break;
					case -108:
						orders_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
					{
						bRecordValid = false;
						or.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=? and editing_backup=0", new String[]{or.uid.toString()}, "datedoc desc");
						if (cursor.moveToNext()) {
							long _id = cursor.getLong(0);
							//String stringId=cursor.getString(0);
							//int rowsUpdated = contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=?", new String[]{stringId});
							//if (rowsUpdated>0)
							//	bOrderUpdated=true;
							if (ReadOrderBy_Id(contentResolver, or, _id)) {
								bRecordValid = true;
								// обнулим текущее количество в документе
								for (OrderLineRecord it : or.lines) {
									it.temp_quantity = it.quantity;
									it.quantity = 0.0;
								}
							}
						}
						cursor.close();
						break;
					}

					case 1: // id
						or.id = new MyID(sc);
						break;
					case 2: // numdoc
						or.numdoc = new String(sc);
						break;
					case 3: // version
						or.version = Integer.parseInt(sc);
						or.versionPDA = 0;
						or.version_ack = 0;
						or.versionPDA_ack = 0;
						or.sumShipping = -1;
						break;
					case 4: // closed_mark и т.п., в КПК не все используется
						//if ()
						or.closed_not_full = (Integer.parseInt(sc) & 4) != 0 ? 1 : 0;
						break;
					case 5: // state
						or.state = E_ORDER_STATE.E_ORDER_STATE_CREATED;
						if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_LOADED, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_LOADED;
						else if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_ACKNOWLEDGED;
						else if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_COMPLETED, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_COMPLETED;
						else if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL;
						else if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_CANCELED, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_CANCELED;
						else if (sc.equals(myBase.GetOrderStateDescr(E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT, null)))
							or.state = E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT;
						else
							or.state = E_ORDER_STATE.E_ORDER_STATE_UNKNOWN;
						break;
					case 6: // comment_closing
					{
						int pos_payment = sc.indexOf("PAYMENT=");
						if (pos_payment >= 0) {
							or.comment_closing = new String(sc.substring(0, pos_payment));
							or.comment_payment = new String(sc.substring(pos_payment + 8));
						} else {
							or.comment_closing = new String(sc);
							or.comment_payment = "";
						}
						break;
					}
					case 7: // agreement_id
						if (sc.length() == 36 * 2) {
							or.agreement_id = new MyID(sc.substring(0, 36));
							or.agreement30_id = new MyID(sc.substring(0 + 36, 36 + 36));
						} else if (sc.length() == 9 * 2) {
							or.agreement_id = new MyID(sc.substring(0, 9));
							or.agreement30_id = new MyID(sc.substring(0 + 9, 9 + 9));
						} else {
							or.agreement_id = new MyID(sc);
							or.agreement30_id = new MyID();
						}
						break;
					case 8: // distr_point_id
						or.distr_point_id = new MyID(sc);
						break;
					case 9: // versionPDA
						or.versionPDA = Integer.parseInt(sc);
						break;
					case 10: // versionPDA_ack
						or.versionPDA_ack = Integer.parseInt(sc);
						break;
					case 11: // version_ack
						or.version_ack = Integer.parseInt(sc);
						break;
					// зарезервированные поля
					case 12:
						if (sc.length() == 29) {
							// FACTORY
							// 2 даты идут через точку с запятой
							or.datedoc = new String(sc.substring(0, 14));
							or.shipping_date = new String(sc.substring(15));
							break;
						}
						if (sc.length() == 14) {
							or.datedoc = new String(sc);
							or.shipping_date = "";
							break;
						}
						break;
					case 13:
						// начиная с версии 4.47
						if (sc.length() > 0) {
							or.sumShipping = Double.parseDouble(sc);
						}
						break;
					case 14:
					case 15:
						break;
					case 16:
						line.nomenclature_id = new MyID(sc);
						line.shipping_time = "";
						// заляп от 15.07.2020 для теста
						// проблема такая - если делается скидка из 1С, то вес не считается
						Cursor nomenclatureCursor = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"descr", "weight_k_1", "flags"}, "id=?", new String[]{sc}, null);
						if (nomenclatureCursor.moveToFirst()) {
							line.stuff_nomenclature = nomenclatureCursor.getString(0);
							line.stuff_weight_k_1 = nomenclatureCursor.getDouble(1);
							line.stuff_nomenclature_flags = nomenclatureCursor.getInt(2);
						} else {
							// Хотя этого не произойдет
							line.stuff_nomenclature = "";
							line.stuff_weight_k_1 = 1.0;
							line.stuff_nomenclature_flags = 0;
						}
						nomenclatureCursor.close();
						//
						break;
					case 17:
						line.quantity = Double.parseDouble(sc);
						line.quantity_requested = 0.0;
						break;
					case 18:
						line.k = Double.parseDouble(sc);
						break;
					case 19:
						line.ed = new String(sc);
						break;
					case 20:
						break;
					case 21:
						line.total = Double.parseDouble(sc);
						break;
					case 22:
						// с 30.11.2013
						int pos_next = sc.indexOf(';');
						if (pos_next == -1) {
							line.price = Common.MyStringToDouble(sc);
							line.discount = 0.0;
						} else {

							line.price = Common.MyStringToDouble(sc.substring(0, pos_next));
							line.discount = Common.MyStringToDouble(sc.substring(pos_next + 1));
						}
						//
						break;
					case 23:
						break;
					case 24:
						break;
					case 25:
						break;
					case 26:
						break;
					case 27: {
						// найдем максимально похожую строку в документе
						OrderLineRecord matchNomenclature = null;
						OrderLineRecord matchQuantity = null;
						OrderLineRecord matchAll = null;
						for (OrderLineRecord search : or.lines) {
							if (search.nomenclature_id.equals(line.nomenclature_id)) {
								if (matchQuantity == null) {
									// если совпадения лучше нет, то запишем это
									matchNomenclature = search;
									// количество совпало и эта строка еще не используется
									if (Math.abs(search.temp_quantity * search.k - line.quantity * line.k) < 0.0001 && search.quantity == 0.0) {
										matchQuantity = search;
										// но полное совпадение еще точно не было найдено, т.к. в этом случае цикл прерывается
										if (search.total == line.total) {
											// нашли полное совпадение
											matchAll = search;
											break;
										}
									}
								}
							}
						}
						if (matchAll != null) {
							// возвращаем количество, которое было - ничего не изменилось
							matchAll.quantity = matchAll.temp_quantity;
						} else if (matchQuantity != null) {
							// совпало количество, цена не совпала
							// заменяем всю строку целиком
							matchQuantity.quantity = line.quantity;
							matchQuantity.price = line.price;
							matchQuantity.total = line.total;
							matchQuantity.discount = line.discount;
							matchQuantity.k = line.k;
							matchQuantity.ed = line.ed;
							matchQuantity.stuff_nomenclature = line.stuff_nomenclature;
							matchQuantity.stuff_weight_k_1 = line.stuff_weight_k_1;

						} else if (matchNomenclature != null) {
							// совпала только номенклатура
							if (matchNomenclature.quantity == 0.0) {
								// если изменилась единица измерения, пересчитаем требуемое количество
								if (Math.abs(matchNomenclature.k - line.k) > 0.0001 && line.k > 0.0001) {
									matchNomenclature.quantity_requested = Math.floor(matchNomenclature.quantity_requested * matchNomenclature.k / line.k * 100 + 0.000001) / 100.0;
								}
								// нигде еще не используется, также заменяем полностью
								matchNomenclature.quantity = line.quantity;
								matchNomenclature.price = line.price;
								matchNomenclature.total = line.total;
								matchNomenclature.discount = line.discount;
								matchNomenclature.k = line.k;
								matchNomenclature.ed = line.ed;
								matchNomenclature.stuff_nomenclature = line.stuff_nomenclature;
								matchNomenclature.stuff_weight_k_1 = line.stuff_weight_k_1;
							} else {
								// Номенклатура будет 2 раза в документе
								or.lines.add(line.clone());
								// в данном случае quantity_requested=0.0
							}
						} else {
							// Это новый товар
							or.lines.add(line.clone());
							// в данном случае quantity_requested=0.0
						}
						nom_idx = 15; // продолжаем строки
						break;
					}
				}
				nom_idx++;
			}

		}
		// и это число 16 не увеличивать, иначе заказ не загрузится
		if (nom_idx >= 16) {
			if (bRecordValid) {
				int _id = 0;
				or.sumDoc = or.GetOrderSum(null, false);
				or.weightDoc = TextDatabase.GetOrderWeight(contentResolver, or, null, false);
				or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
				SaveOrderSQL(contentResolver, or, _id, 0);
				uids.add(or.uid);
			} else {
				// такой заявки уже нет, сообщаем об этом
				myBase.m_empty_orders.put(or.uid, or.version);
			}
		}
		return true;
	}

	static boolean LoadDistribsUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uids) {
		int distribs_system_version;
		int nom_idx;
		int pos;

		DistribsRecord or = new DistribsRecord();
		DistribsLineRecord line = new DistribsLineRecord();

		boolean bRecordValid = false;

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					if (bRecordValid) {
						int _id = 0;
						SaveDistribsSQL(contentResolver, or, _id);
						uids.add(or.uid);
					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_distribs.put(or.uid, or.version);
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						distribs_system_version = 0;
						break;
					case -108:
						distribs_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
					{
						bRecordValid = false;
						or.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
						if (cursor.moveToNext()) {
							long _id = cursor.getLong(0);
							if (ReadDistribsBy_Id(contentResolver, or, _id)) {
								bRecordValid = true;
								// 27.01.2017
								// и удаляем старые строки
								or.lines.clear();
								//
							}
						}
						cursor.close();
						break;
					}
					case 1: // version
						or.version = Integer.parseInt(sc);
						or.versionPDA = 0;
						or.version_ack = 0;
						or.versionPDA_ack = 0;
						break;
					case 2: // versionPDA
						or.versionPDA = Integer.parseInt(sc);
						break;
					case 3: // id
						or.id = new MyID(sc);
						break;
					case 4: // numdoc
						or.numdoc = new String(sc);
						break;
					case 5: // datedoc
						if (sc.length() == 14) {
							or.datedoc = new String(sc);
							break;
						}
					case 6: // client_id
						or.client_id = new MyID(sc);
						break;
					case 7: // distr_point_id
						or.comment = new String(sc);
						break;
					case 8: // ДатаВремяКоординат
						break;
					case 9: // Широта
						break;
					case 10: // Долгота
						break;
					case 11: // version_ack
						or.version_ack = Integer.parseInt(sc);
						break;
					case 12: // versionPDA_ack
						or.versionPDA_ack = Integer.parseInt(sc);
						break;
					case 13: // state
						or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED;
						if (sc.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED, null)))
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED;
						else if (sc.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED, null)))
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED;
						else if (sc.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED, null)))
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED;
						else if (sc.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL, null)))
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL;
						else if (sc.equals(myBase.GetDistribsStateDescr(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED, null)))
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED;
						else
							or.state = E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN;
						break;
					case 14: // distr_point_id
						or.distr_point_id = new MyID(sc);
						break;
					case 15: // curator_id
						or.curator_id = new MyID(sc);
						break;
					case 16: // зарезервировано
					case 17:
						break;
					case 18:
						nom_idx = 120 - 1;
						break;
					case 120:
						line.distribs_contract_id = new MyID(sc);
						break;
					case 121:
						line.quantity = Double.parseDouble(sc);
						break;
					case 122:
					case 123:
					case 124:
						break;
					case 125:
						or.lines.add(line.clone());
						nom_idx = 120 - 1; // продолжаем строки
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6) {
			if (bRecordValid) {
				int _id = 0;
				SaveDistribsSQL(contentResolver, or, _id);
				uids.add(or.uid);
			} else {
				// такой заявки уже нет, сообщаем об этом
				myBase.m_empty_distribs.put(or.uid, or.version);
			}
		}
		return true;
	}

	static boolean LoadSumShippingNotification(ContentResolver contentResolver, MyDatabase myBase, String buf) {
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			String sc = sc0.replace("\r", "");
			String split2[] = sc.split("#");
			int idx = 0;
			String ordersId = "";
			for (String sc2 : split2) {
				switch (idx) {
					case 0:
						ordersId = new String(sc2.replace("{", "").replace("}", ""));
						break;
					case 1: {
						// узнаем версию для того, чтобы при последующей записи она не увеличилась на 1
						Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id", "versionPDA"}, "id=?", new String[]{ordersId}, "datedoc desc");
						int _idIndex = cursor.getColumnIndex("_id");
						int versionPDAIndex = cursor.getColumnIndex("versionPDA");
						while (cursor.moveToNext()) {
							// вообще будет найден всегда 1 документ
							ContentValues cv = new ContentValues();
							cv.put("versionPDA", cursor.getInt(versionPDAIndex));
							cv.put("sum_shipping", Common.MyStringToDouble(sc2));
							contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=?", new String[]{cursor.getString(_idIndex)});
						}
						cursor.close();
						myBase.m_acks_shipping_sums.put(UUID.fromString(ordersId), Common.MyStringToDouble(sc2));
						break;
					}
				}
				idx++;
			}
		}
		return true;
	}

	// bEOFrequired это значит что последней строкой должен быть @@@
	// т.е. в старых вариантах этот символ в конец не записывался и нельзя было определить,
	// что файл записан целиком
	// updateMode=0 загружать все
	// updateMode=1 только обновлять существующие
	// updateMode=2 загружать не существующие, обновлять только не отправленные
	static int LoadOrdersFromText(ContentResolver contentResolver, MyDatabase myBase, String buf, int updateMode, boolean bEOFrequired) {
		int orders_system_version;
		int nom_idx;
		int pos;

		int counter = 0;

		OrderRecord or = new OrderRecord();
		OrderLineRecord line = new OrderLineRecord();

		boolean bRecordExists = false;
		boolean bRecordInBaseCanBeRestored = false;

		MySingleton g = MySingleton.getInstance();

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					if (updateMode == 0 || bRecordExists && updateMode == 1 || updateMode == 2 && (bRecordInBaseCanBeRestored || !bRecordExists)) {
						int _id = 0;
						or.sumDoc = or.GetOrderSum(null, false);
						or.weightDoc = TextDatabase.GetOrderWeight(contentResolver, or, null, false);
						SaveOrderSQL(contentResolver, or, _id, 0);
						counter++;
					}
					bRecordExists = false;
					bRecordInBaseCanBeRestored = false;
					Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
					if (cursor != null) {
						if (cursor.moveToNext()) {
							long _id = cursor.getLong(0);
							//String stringId=cursor.getString(0);
							//int rowsUpdated = contentResolver.update(MTradeContentProvider.ORDERS_CONTENT_URI, cv, "_id=?", new String[]{stringId});
							//if (rowsUpdated>0)
							//	bOrderUpdated=true;
							if (ReadOrderBy_Id(contentResolver, or, _id)) {
								bRecordExists = true;
								bRecordInBaseCanBeRestored = (
										or.state == E_ORDER_STATE.E_ORDER_STATE_CREATED ||
												or.state == E_ORDER_STATE.E_ORDER_STATE_UNKNOWN);
							}
						}
						cursor.close();
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						orders_system_version = 0;
						break;
					case -108:
						orders_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
						or.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						// обнулим не обязательно заполняемые поля
						or.shipping_type = 0;
						or.shipping_time = 0;
						or.shipping_begin_time = "0000";
						or.shipping_end_time = "2359";
						or.shipping_date = "";
						//
						break;
					case 1:
						or.version = Integer.parseInt(sc);
						break;
					case 2:
						or.versionPDA = Integer.parseInt(sc);
						break;
					case 3: // id
						or.id = new MyID(sc);
						break;
					case 4: // numdoc
						or.numdoc = new String(sc);
						break;
					case 5: // datedoc
						or.datedoc = new String(sc);
						break;
					case 6: // client_id
						or.client_id = new MyID(sc);
						break;
					case 7:
						or.comment = new String(sc);
						break;
					case 8: // state
						or.state = E_ORDER_STATE.fromInt(Integer.parseInt(sc));
						break;
					case 9:
						if (g.Common.MEGA) {
							or.bw = E_BW.fromInt(Integer.parseInt(sc));
						} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
							or.closed_not_full = Integer.parseInt(sc) & 1;
							or.bw = E_BW.fromInt(Integer.parseInt(sc) >> 1);
						} else {
							or.closed_not_full = Integer.parseInt(sc);
						}
						break;
					case 10: // stock_id
						if (g.Common.MEGA) {
							or.trade_type = E_TRADE_TYPE.fromInt(Integer.parseInt(sc));
						} else {
							or.stock_id = new MyID(sc);
						}
						break;
					case 11: // curator_id
						or.curator_id = new MyID(sc);
						break;
					case 12: // datecoord
						or.datecoord = new String(sc);
						break;
					case 13: // latitude
						or.latitude = Common.MyStringToDouble(sc);
						break;
					case 14: // longitude
						or.longitude = Common.MyStringToDouble(sc);
						break;
					case 15: // dont_need_send
						or.dont_need_send = Integer.parseInt(sc) & 1;
						break;
					case 16: // agreement_id
						or.agreement_id = new MyID(sc);
						break;
					case 17: // distr_point_id
						or.distr_point_id = new MyID(sc);
						break;
					case 18: // price_type_id
						or.price_type_id = new MyID(sc);
						break;
					case 19:  // comment_closing
						or.comment_closing = new String(sc);
						or.comment_payment = "";
						break;
					case 20: // version_ack
						or.version_ack = Integer.parseInt(sc);
						break;
					case 21: // versionPDA_ack
						or.versionPDA_ack = Integer.parseInt(sc);
						break;
					// зарезервированные поля
					case 22:
						if (sc.length() > 3 && sc.substring(0, 3).equals("PL:")) {
							sc = sc.substring(3);
							int idx = 0;
							for (; ; ) {
								int pos_next = sc.indexOf(';');
								String sc_i;
								if (pos_next >= 0) {
									sc_i = sc.substring(0, pos_next);
									pos_next++;
								} else {
									sc_i = sc;
									pos_next = sc.length();
								}
								switch (idx) {
									case 0:
										or.shipping_type = Integer.parseInt(sc_i);
										break;
									case 1:
										or.shipping_time = Integer.parseInt(sc_i);
										break;
									case 2:
										or.shipping_begin_time = sc_i;
										break;
									case 3:
										or.shipping_end_time = sc_i;
										break;
								}
								if (pos_next == 0)
									break;
								idx++;
								sc = sc.substring(pos_next);
							}
							//bw.write(String.format("PL:%d;%d;%s;%s\r\n", orderRecord.shipping_type, orderRecord.shipping_time, orderRecord.shipping_begin_time, orderRecord.shipping_end_time));
						}
						break;
					case 23:
						break;
					case 24:
						break;
					case 25:
						nom_idx = 120 - 1;
						break;
					case 120:
						line.nomenclature_id = new MyID(sc);
						line.shipping_time = "";
						break;
					case 121: {
						int pos_next = sc.indexOf(';');
						if (pos_next == -1) {
							line.quantity = Common.MyStringToDouble(sc);
							line.quantity_requested = line.quantity;
						} else {

							line.quantity = Common.MyStringToDouble(sc.substring(0, pos_next));
							line.quantity_requested = Common.MyStringToDouble(sc.substring(pos_next + 1));
						}
						break;
					}
					case 122: // k
						line.k = Common.MyStringToDouble(sc);
						break;
					case 123: // единица измерения
						line.ed = new String(sc);
						break;
					case 124: // price
						//line.price=Common.MyStringToDouble(sc);
						// с 30.11.2013
						int pos_next = sc.indexOf(';');
						if (pos_next == -1) {
							line.price = Common.MyStringToDouble(sc);
							line.discount = 0.0;
						} else {

							line.price = Common.MyStringToDouble(sc.substring(0, pos_next));
							line.discount = Common.MyStringToDouble(sc.substring(pos_next + 1));
						}
						//
						break;
					case 125:
						line.total = Common.MyStringToDouble(sc);
						or.lines.add(line.clone());
						nom_idx = 120 - 1; // продолжаем строки
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6 && !bEOFrequired) {
			if (updateMode == 0 || bRecordExists && updateMode == 1 || updateMode == 2 && (bRecordInBaseCanBeRestored || !bRecordExists)) {
				int _id = 0;
				or.sumDoc = or.GetOrderSum(null, false);
				or.weightDoc = TextDatabase.GetOrderWeight(contentResolver, or, null, false);
				// TODO здесь и в других местах стирать резервные копии этого документа
				SaveOrderSQL(contentResolver, or, _id, 0);
				counter++;
			}
		}

		return counter;
	}

	static void SaveOrderToText(BufferedWriter bw, OrderRecord orderRecord, boolean bVersionAsAcked, boolean bKeepRequestedQuantity) throws IOException {
		MySingleton g = MySingleton.getInstance();

		bw.write("@@@\r\n");
		bw.write(orderRecord.uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", orderRecord.version));
		// версия на КПК
		bw.write(String.format("%d\r\n", orderRecord.versionPDA));
		// 3 id
		bw.write(String.format("%s\r\n", orderRecord.id.toString()));
		// 4 numdoc
		bw.write(String.format("%s\r\n", orderRecord.numdoc));
		// 5 datedoc
		bw.write(String.format("%s\r\n", orderRecord.datedoc));
		// 6 client_id
		bw.write(String.format("%s\r\n", orderRecord.client_id.toString()));
		// 7 comment
		bw.write(String.format("%s\r\n", Common.NormalizeString(orderRecord.comment)));
		// 8 state
		bw.write(String.format("%d\r\n", orderRecord.state.value()));
		// 9 bw
		if (g.Common.MEGA) {
			bw.write(String.format("%d\r\n", orderRecord.bw.value()));
		} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
			bw.write(String.format("%d\r\n", orderRecord.bw.value() * 2 + orderRecord.closed_not_full));
		} else {
			bw.write(String.format("%d\r\n", orderRecord.closed_not_full));
		}
		// 10 консигнация и т.п.
//	#ifdef SNEGOROD
		bw.write(String.format("%s\r\n", orderRecord.stock_id.toString()));
//	#else
//	    sprintf(s, "%d\n", rec->trade_type); // Нал, безнал и т.п.
//	    f->WriteString(s);
//	#endif
		// 11 curator_id
		bw.write(String.format("%s\r\n", orderRecord.curator_id.toString()));
		// 12 версия 102 дата координат
		bw.write(String.format("%s\r\n", orderRecord.datecoord));
		// 13 версия 102 широта, долгота
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(orderRecord.latitude, "%.6f")));
		// 14
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(orderRecord.longitude, "%.6f")));
		// 15 версия 104, 5 значений
		bw.write(String.format("%d\r\n", orderRecord.dont_need_send));
		// 16
		if (g.Common.FACTORY) {
			bw.write(String.format("%s\r\n", orderRecord.agreement_id.toStringOrEmpty(Constants.emptyID36) + orderRecord.agreement30_id.toStringOrEmpty(Constants.emptyID36)));
		} else {
			bw.write(String.format("%s\r\n", orderRecord.agreement_id.toString()));
		}
		// 17
		bw.write(String.format("%s\r\n", orderRecord.distr_point_id.toString()));
		// 18
		bw.write(String.format("%s\r\n", orderRecord.price_type_id.toString()));
		// 19
		if (orderRecord.comment_payment.isEmpty()) {
			bw.write(String.format("%s\r\n", Common.NormalizeString(orderRecord.comment_closing)));
		} else {
			bw.write(String.format("%sPAYMENT=%s\r\n", Common.NormalizeString(orderRecord.comment_closing), Common.NormalizeString(orderRecord.comment_payment)));
		}
		// 20
		//sprintf(s, "%d\n", bVersionAsAcked?rec->version:rec->version_ack);
		//f->WriteString(s);
		//bw.write(String.format("%d\r\n", orderRecord.version));
		bw.write(String.format("%d\r\n", bVersionAsAcked ? orderRecord.version : orderRecord.version_ack));
		// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
		// далее, в случае успешной отправки, мы установим version_ack=version
		// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
		// version_ack (если подтверждение он не получал), то отправка будет еще раз
		// 21 версия на КПК подтвержденная
		bw.write(String.format("%d\r\n", orderRecord.versionPDA_ack));
		// зарезервированные поля
		if (g.Common.FACTORY) {
			bw.write(String.format("PL:%d;%d;%s;%s;%s\r\n", orderRecord.shipping_type, orderRecord.shipping_time, orderRecord.shipping_begin_time, orderRecord.shipping_end_time, orderRecord.shipping_date));
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(orderRecord.sumShipping, "%.2f")));
		} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
			bw.write(String.format("PL:%d;%d;%s;%s\r\n", orderRecord.shipping_type, orderRecord.shipping_time, orderRecord.shipping_begin_time, orderRecord.shipping_end_time));
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(orderRecord.sumShipping, "%.2f")));
		} else {
			bw.write("-1\r\n");
			bw.write("-1\r\n");
		}
		bw.write(String.format("%s;%.2f;%d\r\n", orderRecord.datecreation, orderRecord.gpsaccuracy, orderRecord.gpsstate));
		bw.write("-1\r\n");
		// строки документа
		for (OrderLineRecord line : orderRecord.lines) {
			// nomenclature_id
			bw.write(String.format("%s\r\n", line.nomenclature_id.toString()));
			// quantity etc
			if (line.quantity_requested != line.quantity && bKeepRequestedQuantity) {
				bw.write(String.format("%.2f;%.2f\r\n", line.quantity, line.quantity_requested));
			} else {
				bw.write(String.format("%.2f\r\n", line.quantity));
			}
			bw.write(String.format("%.2f\r\n", line.k));
			bw.write(String.format("%s\r\n", line.ed));
			if (line.discount != 0.0) {
				bw.write(String.format("%.2f;%.2f\r\n", line.price, line.discount));
			} else {
				bw.write(String.format("%.2f\r\n", line.price));
			}
			bw.write(String.format("%.2f\r\n", line.total));
		}
	}

	static void SaveOrderToXML(XmlSerializer serializer, OrderRecord orderRecord, boolean bVersionAsAcked, boolean bKeepRequestedQuantity) throws IOException {

		MySingleton g = MySingleton.getInstance();

		// https://stackoverflow.com/questions/5181294/how-to-create-xml-file-in-android

		serializer.startTag(null, "OrderPDA");
		serializer.attribute(null, "uid", orderRecord.uid.toString());
		// версия на сервере
		serializer.attribute(null, "_version", String.format("%d", orderRecord.version));
		// версия на КПК
		serializer.attribute(null, "versionPDA", String.format("%d", orderRecord.versionPDA));
		// 3 id
		serializer.attribute(null, "id", orderRecord.id.toString());
		// 4 numdoc
		serializer.attribute(null, "numdoc", orderRecord.numdoc);
		// 5 datedoc
		serializer.attribute(null, "datedoc", orderRecord.datedoc);
		// 6 client_id
		serializer.attribute(null, "client_id", orderRecord.client_id.toString());
		// 7 comment
		serializer.attribute(null, "comment", Common.NormalizeString(orderRecord.comment));
		// 8 state
		serializer.attribute(null, "state", String.format("%d", orderRecord.state.value()));
		// 9 bw
		if (g.Common.MEGA) {
			serializer.attribute(null, "bw", String.format("%d", orderRecord.bw.value()));
		} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
			serializer.attribute(null, "flags", String.format("%d", orderRecord.bw.value() * 2 + orderRecord.closed_not_full));
		} else {
			serializer.attribute(null, "flags", String.format("%d", orderRecord.closed_not_full));
		}
		// 10 консигнация и т.п.
//	#ifdef SNEGOROD
		serializer.attribute(null, "stock_id", orderRecord.stock_id.toString());
//	#else
//	    sprintf(s, "%d\n", rec->trade_type); // Нал, безнал и т.п.
//	    f->WriteString(s);
//	#endif
		// 11 curator_id
		serializer.attribute(null, "curator_id", orderRecord.curator_id.toString());
		// 12 версия 102 дата координат
		serializer.attribute(null, "datecoord", orderRecord.datecoord);
		// 13 версия 102 широта, долгота
		serializer.attribute(null, "latitude", Common.DoubleToStringFormat(orderRecord.latitude, "%.6f"));
		// 14
		serializer.attribute(null, "longitude", Common.DoubleToStringFormat(orderRecord.longitude, "%.6f"));
		// 15 версия 104, 5 значений
		serializer.attribute(null, "dont_need_send", String.format("%d", orderRecord.dont_need_send));
		// 16
		if (g.Common.FACTORY) {
			serializer.attribute(null, "agreement_id72", orderRecord.agreement_id.toStringOrEmpty(Constants.emptyID36) + orderRecord.agreement30_id.toStringOrEmpty(Constants.emptyID36));
		} else {
			serializer.attribute(null, "agreement_id", orderRecord.agreement_id.toString());
		}
		// 17
		serializer.attribute(null, "distr_point_id", orderRecord.distr_point_id.toString());
		// 18
		serializer.attribute(null, "price_type_id", orderRecord.price_type_id.toString());
		// 19
		serializer.attribute(null, "comment_closing", Common.NormalizeString(orderRecord.comment_closing));
		if (!orderRecord.comment_payment.isEmpty()) {
			serializer.attribute(null, "comment_payment", Common.NormalizeString(orderRecord.comment_payment));
		}
		// 20
		//sprintf(s, "%d\n", bVersionAsAcked?rec->version:rec->version_ack);
		//f->WriteString(s);
		//bw.write(String.format("%d\r\n", orderRecord.version));
		serializer.attribute(null, "version_ack", String.format("%d", bVersionAsAcked ? orderRecord.version : orderRecord.version_ack));
		// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
		// далее, в случае успешной отправки, мы установим version_ack=version
		// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
		// version_ack (если подтверждение он не получал), то отправка будет еще раз
		// 21 версия на КПК подтвержденная
		serializer.attribute(null, "versionPDA_ack", String.format("%d", orderRecord.versionPDA_ack));
		serializer.attribute(null, "shipping_type", String.format("%d", orderRecord.shipping_type));
		serializer.attribute(null, "shipping_time", String.format("%d", orderRecord.shipping_time));
		serializer.attribute(null, "shipping_begin_time", orderRecord.shipping_begin_time);
		serializer.attribute(null, "shipping_end_time", orderRecord.shipping_end_time);
		serializer.attribute(null, "shipping_date", orderRecord.shipping_date);
		serializer.attribute(null, "sumShipping", Common.DoubleToStringFormat(orderRecord.sumShipping, "%.2f"));
		serializer.attribute(null, "datecreation", orderRecord.datecreation);
		serializer.attribute(null, "gpsaccuracy", Common.DoubleToStringFormat(orderRecord.gpsaccuracy, "%.2f"));
		serializer.attribute(null, "gpsstate", String.format("%d", orderRecord.gpsstate));
		// строки документа
		for (OrderLineRecord line : orderRecord.lines) {
			serializer.startTag(null, "Line");
			// nomenclature_id
			serializer.attribute(null, "nomenclature_id", line.nomenclature_id.toString());
			// quantity etc
			serializer.attribute(null, "quantity", Common.DoubleToStringFormat(line.quantity, "%.2f"));
			if (line.quantity_requested != line.quantity && bKeepRequestedQuantity) {
				serializer.attribute(null, "quantity_requested", Common.DoubleToStringFormat(line.quantity_requested, "%.2f"));
			}
			serializer.attribute(null, "k", Common.DoubleToStringFormat(line.k, "%.2f"));
			serializer.attribute(null, "ed", line.ed);
			serializer.attribute(null, "price", Common.DoubleToStringFormat(line.price, "%.2f"));
			if (line.discount != 0.0) {
				serializer.attribute(null, "discount", Common.DoubleToStringFormat(line.discount, "%.2f"));
			}
			serializer.attribute(null, "total", Common.DoubleToStringFormat(line.total, "%.2f"));
			serializer.endTag(null, "Line");
		}

		serializer.endTag(null, "OrderPDA");
	}


	static void SaveEmptyOrder(BufferedWriter bw, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса заказа, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
		// далее отличие от нормальной процедуры
		bw.write("@@@\r\n");
		bw.write(uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", version));
		// версия на КПК - пропускаем
		// ...
	}

	static void SaveEmptyOrderXML(XmlSerializer serializer, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса заказа, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		/*
		Writer w = new OutputStreamWriter(os, "UTF-8");
		w.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		w.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
		// далее отличие от нормальной процедуры
		w.write("@@@\r\n");
		w.write(uid.toString());
		w.write("\r\n");
		// версия на сервере
		w.write(String.format("%d\r\n", version));
		// версия на КПК - пропускаем
		// ...
		 */

		serializer.startTag(null, "EmptyOrderPDA");
		serializer.attribute(null, "uid", uid.toString());
		serializer.attribute(null, "_version", String.format("%d", version));
		serializer.endTag(null, "EmptyOrderPDA");

	}


	static boolean SaveSendOrders(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		// один раз отправляем и забываем. если еще раз придут отсутствующие заявки
		// тогда отправятся еще раз
		//QMap<QUuid, int>::iterator it_e;
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_orders.entrySet()) {
			bw.write("##EMPTYORDERPDA##\r\n");
			//SaveEmptyOrder(f, MyGUID(it_e.key()), it_e.value());
			SaveEmptyOrder(bw, it.getKey(), it.getValue());
		}
		//myBase.m_empty_orders.clear();

		MySingleton g = MySingleton.getInstance();

		//QSqlQuery query;
		//if (!query.exec("select _id from orders where (versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0"))
		//    qDebug()<<query.lastError();
		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0 or state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				OrderRecord rec = new OrderRecord();
				//static boolean ReadOrderBy_Id(ContentResolver contentResolver, g.MyDatabase myBase, OrderRecord rec, long _id)
				if (ReadOrderBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					bw.write("##ORDERPDA##\r\n");
					bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
					bw.write(String.format("%d\r\n", g.Common.m_mtrade_version));
					//SaveOrderToFile(bw, &rec, true);
					if (g.Common.PRODLIDER || g.Common.TANDEM) {
						// В файл записываем также и требуемое количество
						SaveOrderToText(bw, rec, true, true);
					} else {
						SaveOrderToText(bw, rec, true, false);
					}
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}
		return !bEmpty;
	}

	static boolean SaveSendOrdersXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Orders");

		// один раз отправляем и забываем. если еще раз придут отсутствующие заявки
		// тогда отправятся еще раз
		//QMap<QUuid, int>::iterator it_e;
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_orders.entrySet()) {
			SaveEmptyOrderXML(serializer, it.getKey(), it.getValue());
		}
		//myBase.m_empty_orders.clear();

		MySingleton g = MySingleton.getInstance();

		//QSqlQuery query;
		//if (!query.exec("select _id from orders where (versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0"))
		//    qDebug()<<query.lastError();
		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0 or state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				OrderRecord rec = new OrderRecord();
				//static boolean ReadOrderBy_Id(ContentResolver contentResolver, g.MyDatabase myBase, OrderRecord rec, long _id)
				if (ReadOrderBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					// TODO тот код нужен. может быть)
					/*
					bw.write("##ORDERPDA##\r\n");
					bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
					bw.write(String.format("%d\r\n", g.Common.m_mtrade_version));
					//SaveOrderToFile(bw, &rec, true);
					 */
					if (g.Common.PRODLIDER || g.Common.TANDEM) {
						// В файл записываем также и требуемое количество
						//SaveOrderToText(bw, rec, true, true);
						SaveOrderToXML(serializer, rec, true, true);
					} else {
						//SaveOrderToText(bw, rec, true, false);
						SaveOrderToXML(serializer, rec, true, false);
					}
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}

		serializer.endTag(null, "Orders");

		serializer.endDocument();
		serializer.flush();
		// не закрываем, иначе закроется весь поток
		//os.close();

		return !bEmpty;
	}

	static boolean SaveSendSumShippingNotificationsXML(OutputStream os, Map<UUID, Double> acks_shipping_sums) throws IOException {
		// TODO
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		// один раз отправляем и забываем. если еще раз придут отсутствующие заявки
		// тогда отправятся еще раз
		//QMap<QUuid, int>::iterator it_e;
		//for (Map.Entry<UUID, Integer> it : myBase.m_empty_orders.entrySet()) {
		//	SaveEmptyOrderXML(serializer, it.getKey(), it.getValue());
		//}
		//myBase.m_empty_orders.clear();

		for (Map.Entry<UUID, Double> it : acks_shipping_sums.entrySet()) {
			bEmpty = false;
			serializer.startTag(null, "Entry");
			serializer.attribute(null, "uid", it.getKey().toString());
			serializer.attribute(null, "value", Common.DoubleToStringFormat(it.getValue(), "%.2f"));
			serializer.endTag(null, "Entry");

		}

		serializer.endDocument();
		serializer.flush();

		return !bEmpty;
	}

	static boolean SaveSendPaymentsXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Payments");

		// один раз отправляем и забываем. если еще раз придут отсутствующие заявки
		// тогда отправятся еще раз
		//QMap<QUuid, int>::iterator it_e;
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_payments.entrySet()) {
			SaveEmptyPaymentXML(serializer, it.getKey(), it.getValue());
		}

		MySingleton g = MySingleton.getInstance();

		//QSqlQuery query;
		//if (!query.exec("select _id from orders where (versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0"))
		//    qDebug()<<query.lastError();
		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) or state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				CashPaymentRecord rec = new CashPaymentRecord();
				if (ReadPaymentBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					SavePaymentToXML(serializer, rec, true);
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}

		serializer.endTag(null, "Payments");

		serializer.endDocument();
		serializer.flush();

		return !bEmpty;
	}


	static boolean LoadRefundsUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uids) {
		int refunds_system_version;
		int nom_idx;
		int pos;

		RefundRecord or = new RefundRecord();
		RefundLineRecord line = new RefundLineRecord();

		boolean bRecordValid = false;

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					if (bRecordValid) {
						int _id = 0;
						or.weightDoc = TextDatabase.GetRefundWeight(contentResolver, or, null, false);
						or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
						SaveRefundSQL(contentResolver, or, _id);
						uids.add(or.uid);
					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_refunds.put(or.uid, or.version);
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						refunds_system_version = 0;
						break;
					case -108:
						refunds_system_version = Integer.parseInt(sc);
						break;
					case 0: // id
					{
						bRecordValid = false;
						or.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
						if (cursor.moveToNext()) {
							long _id = cursor.getLong(0);
							if (ReadRefundBy_Id(contentResolver, or, _id)) {
								bRecordValid = true;
								// обнулим текущее количество в документе
								for (RefundLineRecord it : or.lines) {
									it.temp_quantity = it.quantity;
									it.quantity = 0.0;
								}
							}
						}
						cursor.close();
						break;
					}

					case 1: // id
						or.id = new MyID(sc);
						break;
					case 2: // numdoc
						or.numdoc = new String(sc);
						break;
					case 3: // version
						or.version = Integer.parseInt(sc);
						or.versionPDA = 0;
						or.version_ack = 0;
						or.versionPDA_ack = 0;
						or.shipping_type = 0;
						break;
					case 4: // closed_mark и т.п., в КПК не все используется
						//if ()
						or.closed_not_full = (Integer.parseInt(sc) & 4) != 0 ? 1 : 0;
						break;
					case 5: // state
						or.state = E_REFUND_STATE.E_REFUND_STATE_CREATED;
						if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_LOADED, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_LOADED;
						else if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED;
						else if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_COMPLETED, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_COMPLETED;
						else if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL;
						else if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_CANCELED, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_CANCELED;
						else if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT, null)))
							or.state = E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT;
						else
							or.state = E_REFUND_STATE.E_REFUND_STATE_UNKNOWN;
						break;
					case 6: // comment_closing
					{
						or.comment_closing = new String(sc);
						break;
					}
					case 7: // agreement_id
						or.agreement_id = new MyID(sc);
						break;
					case 8: // distr_point_id
						or.distr_point_id = new MyID(sc);
						break;
					case 9: // versionPDA
						or.versionPDA = Integer.parseInt(sc);
						break;
					case 10: // versionPDA_ack
						or.versionPDA_ack = Integer.parseInt(sc);
						break;
					case 11: // version_ack
						or.version_ack = Integer.parseInt(sc);
						break;
					// зарезервированные поля
					case 12:
						if (sc.length() > 3 && sc.substring(0, 3).equals("PL:")) {
							sc = sc.substring(3);
							int idx = 0;
							for (; ; ) {
								int pos_next = sc.indexOf(';');
								String sc_i;
								if (pos_next >= 0) {
									sc_i = sc.substring(0, pos_next);
									pos_next++;
								} else {
									sc_i = sc;
									pos_next = sc.length();
								}
								switch (idx) {
									case 0:
										or.shipping_type = Integer.parseInt(sc_i);
										break;
									case 1:
										//or.shipping_time=Integer.parseInt(sc_i);
										break;
									case 2:
										//or.shipping_begin_time=sc_i;
										break;
									case 3:
										//or.shipping_end_time=sc_i;
										break;
								}
								if (pos_next == 0)
									break;
								idx++;
								sc = sc.substring(pos_next);
							}
							//bw.write(String.format("PL:%d;%d;%s;%s\r\n", orderRecord.shipping_type, orderRecord.shipping_time, orderRecord.shipping_begin_time, orderRecord.shipping_end_time));
						}
						break;
					case 13:
					case 14:
					case 15:
						break;
					case 16: // 120
						line.nomenclature_id = new MyID(sc);
						break;
					case 17: // 121
						line.quantity = Double.parseDouble(sc);
						line.quantity_requested = 0.0;
						break;
					case 18: // 122
						line.k = Double.parseDouble(sc);
						break;
					case 19: // 123
						line.ed = new String(sc);
						break;
					case 20: // 124
						break;
					case 21: // 125
						// total
						break;
					case 22: // 126
						// price, discount
						break;
					case 23:
						break;
					case 24:
						break;
					case 25:
						break;
					case 26:
						break;
					case 27: {
						// найдем максимально похожую строку в документе
						RefundLineRecord matchNomenclature = null;
						RefundLineRecord matchQuantity = null;
						RefundLineRecord matchAll = null;
						for (RefundLineRecord search : or.lines) {
							if (search.nomenclature_id.equals(line.nomenclature_id)) {
								if (matchQuantity == null) {
									// если совпадения лучше нет, то запишем это
									matchNomenclature = search;
									// количество совпало и эта строка еще не используется
									if (Math.abs(search.temp_quantity * search.k - line.quantity * line.k) < 0.0001 && search.quantity == 0.0) {
										matchQuantity = search;
										// но полное совпадение еще точно не было найдено, т.к. в этом случае цикл прерывается
										//if (search.total==line.total)
										//{
										//	// нашли полное совпадение
										matchAll = search;
										break;
										//}
									}
								}
							}
						}
						if (matchAll != null) {
							// возвращаем количество, которое было - ничего не изменилось
							matchAll.quantity = matchAll.temp_quantity;
						} else if (matchQuantity != null) {
							// совпало количество, цена не совпала
							// заменяем всю строку целиком
							matchQuantity.quantity = line.quantity;
							//matchQuantity.price=line.price;
							//matchQuantity.total=line.total;
							//matchQuantity.discount=line.discount;
							matchQuantity.k = line.k;
							matchQuantity.ed = line.ed;
							matchQuantity.stuff_nomenclature = line.stuff_nomenclature;
							matchQuantity.stuff_weight_k_1 = line.stuff_weight_k_1;
						} else if (matchNomenclature != null) {
							// совпала только номенклатура
							if (matchNomenclature.quantity == 0.0) {
								// если изменилась единица измерения, пересчитаем требуемое количество
								if (Math.abs(matchNomenclature.k - line.k) > 0.0001 && line.k > 0.0001) {
									matchNomenclature.quantity_requested = Math.floor(matchNomenclature.quantity_requested * matchNomenclature.k / line.k * 100 + 0.000001) / 100.0;
								}
								// нигде еще не используется, также заменяем полностью
								matchNomenclature.quantity = line.quantity;
								//matchNomenclature.price=line.price;
								//matchNomenclature.total=line.total;
								//matchNomenclature.discount=line.discount;
								matchNomenclature.k = line.k;
								matchNomenclature.ed = line.ed;
								matchNomenclature.stuff_nomenclature = line.stuff_nomenclature;
								matchNomenclature.stuff_weight_k_1 = line.stuff_weight_k_1;
							} else {
								// Номенклатура будет 2 раза в документе
								or.lines.add(line.clone());
								// в данном случае quantity_requested=0.0
							}
						} else {
							// Это новый товар
							or.lines.add(line.clone());
							// в данном случае quantity_requested=0.0
						}
						nom_idx = 15; // продолжаем строки
						break;
					}
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6) {
			if (bRecordValid) {
				int _id = 0;
				//or.sumDoc=or.GetOrderSum(null, false);
				or.weightDoc = TextDatabase.GetRefundWeight(contentResolver, or, null, false);
				or.dont_need_send = 0; // этот флаг в случае, если документ на сервере, не может быть установлен
				SaveRefundSQL(contentResolver, or, _id);
				uids.add(or.uid);
			} else {
				// такой заявки уже нет, сообщаем об этом
				myBase.m_empty_refunds.put(or.uid, or.version);
			}
		}
		return true;
	}

	static void SaveRefundToText(BufferedWriter bw, RefundRecord refundRecord, boolean bVersionAsAcked, boolean bKeepRequestedQuantity) throws IOException {
		MySingleton g = MySingleton.getInstance();

		bw.write("@@@\r\n");
		bw.write(refundRecord.uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", refundRecord.version));
		// версия на КПК
		bw.write(String.format("%d\r\n", refundRecord.versionPDA));
		// 3 id
		bw.write(String.format("%s\r\n", refundRecord.id.toString()));
		// 4 numdoc
		bw.write(String.format("%s\r\n", refundRecord.numdoc));
		// 5 datedoc
		bw.write(String.format("%s\r\n", refundRecord.datedoc));
		// 6 client_id
		bw.write(String.format("%s\r\n", refundRecord.client_id.toString()));
		// 7 comment
		bw.write(String.format("%s\r\n", Common.NormalizeString(refundRecord.comment)));
		// 8 state
		bw.write(String.format("%d\r\n", refundRecord.state.value()));
		// 9 bw
		if (g.Common.MEGA) {
			bw.write(String.format("%d\r\n", refundRecord.bw.value()));
		} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
			bw.write(String.format("%d\r\n", refundRecord.bw.value() * 2 + refundRecord.closed_not_full));
		} else {
			bw.write(String.format("%d\r\n", refundRecord.closed_not_full));
		}
		// 10 консигнация и т.п.
		bw.write(String.format("%s\r\n", refundRecord.stock_id.toString()));
		// 11 curator_id
		bw.write(String.format("%s\r\n", refundRecord.curator_id.toString()));
		// 12 версия 102 дата координат
		bw.write(String.format("%s\r\n", refundRecord.datecoord));
		// 13 версия 102 широта, долгота
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(refundRecord.latitude, "%.6f")));
		// 14
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(refundRecord.longitude, "%.6f")));
		// 15 версия 104, 5 значений
		bw.write(String.format("%d\r\n", refundRecord.dont_need_send));
		// 16
		bw.write(String.format("%s\r\n", refundRecord.agreement_id.toString()));
		// 17
		bw.write(String.format("%s\r\n", refundRecord.distr_point_id.toString()));
		// 18 price_type_id
		bw.write("-1\r\n");
		// 19
		bw.write(String.format("%s\r\n", refundRecord.comment_closing));
		// 20
		//sprintf(s, "%d\n", bVersionAsAcked?rec->version:rec->version_ack);
		//f->WriteString(s);
		//bw.write(String.format("%d\r\n", orderRecord.version));
		bw.write(String.format("%d\r\n", bVersionAsAcked ? refundRecord.version : refundRecord.version_ack));
		// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
		// далее, в случае успешной отправки, мы установим version_ack=version
		// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
		// version_ack (если подтверждение он не получал), то отправка будет еще раз
		// 21 версия на КПК подтвержденная
		bw.write(String.format("%d\r\n", refundRecord.versionPDA_ack));
		// зарезервированные поля
		bw.write(String.format("PL:%d;%d;%s;%s\r\n", refundRecord.shipping_type, 0, "", "")); // последние поля зарезервировано под время доставки (как в заказах)
		bw.write("-1\r\n");
		bw.write("-1\r\n");
		bw.write("-1\r\n");
		// строки документа
		for (RefundLineRecord line : refundRecord.lines) {
			// nomenclature_id
			bw.write(String.format("%s\r\n", line.nomenclature_id.toString()));
			// quantity etc
			if (line.quantity_requested != line.quantity && bKeepRequestedQuantity) {
				bw.write(String.format("%.2f;%.2f\r\n", line.quantity, line.quantity_requested));
			} else {
				bw.write(String.format("%.2f\r\n", line.quantity));
			}
			bw.write(String.format("%.2f\r\n", line.k));
			bw.write(String.format("%s\r\n", line.ed));
			bw.write("-1\r\n"); // price
			bw.write("-1\r\n"); // total
			bw.write(String.format("%s\r\n", line.comment_in_line)); // комментарий
		}
	}

	static void SaveRefundToXML(XmlSerializer serializer, RefundRecord refundRecord, boolean bVersionAsAcked, boolean bKeepRequestedQuantity) throws IOException {
		MySingleton g = MySingleton.getInstance();

		serializer.startTag(null, "RefundPDA");

		serializer.attribute(null, "uid", refundRecord.uid.toString());
		// версия на сервере
		serializer.attribute(null, "_version", String.format("%d", refundRecord.version));
		// версия на КПК
		serializer.attribute(null, "versionPDA", String.format("%d", refundRecord.versionPDA));
		// 3 id
		serializer.attribute(null, "id", refundRecord.id.toString());
		// 4 numdoc
		serializer.attribute(null, "numdoc", refundRecord.numdoc);
		// 5 datedoc
		serializer.attribute(null, "datedoc", refundRecord.datedoc);
		// 6 client_id
		serializer.attribute(null, "client_id", refundRecord.client_id.toString());
		// 7 comment
		serializer.attribute(null, "comment", Common.NormalizeString(refundRecord.comment));
		// 8 state
		serializer.attribute(null, "state", String.format("%d", refundRecord.state.value()));
		// 9 bw
		if (g.Common.MEGA) {
			serializer.attribute(null, "bw", String.format("%d", refundRecord.bw.value()));
		} else if (g.Common.PRODLIDER || g.Common.TANDEM) {
			serializer.attribute(null, "flags", String.format("%d", refundRecord.bw.value() * 2 + refundRecord.closed_not_full));
		} else {
			serializer.attribute(null, "flags", String.format("%d", refundRecord.closed_not_full));
		}
		// 10 консигнация и т.п.
		serializer.attribute(null, "stock_id", refundRecord.stock_id.toString());
		// 11 curator_id
		serializer.attribute(null, "curator_id", refundRecord.curator_id.toString());
		// 12 версия 102 дата координат
		serializer.attribute(null, "datecoord", refundRecord.datecoord);
		// 13 версия 102 широта, долгота
		serializer.attribute(null, "latitude", Common.DoubleToStringFormat(refundRecord.latitude, "%.6f"));
		// 14
		serializer.attribute(null, "longitude", Common.DoubleToStringFormat(refundRecord.longitude, "%.6f"));
		// 15 версия 104, 5 значений
		serializer.attribute(null, "dont_need_send", String.format("%d", refundRecord.dont_need_send));
		// 16
		serializer.attribute(null, "agreement_id", refundRecord.agreement_id.toString());
		// 17
		serializer.attribute(null, "distr_point_id", refundRecord.distr_point_id.toString());
		// 18 price_type_id
		// не записываем
		// 19
		serializer.attribute(null, "comment_closing", refundRecord.comment_closing);
		// 20
		//sprintf(s, "%d\n", bVersionAsAcked?rec->version:rec->version_ack);
		//f->WriteString(s);
		//bw.write(String.format("%d\r\n", orderRecord.version));
		serializer.attribute(null, "version_ack", String.format("%d", bVersionAsAcked ? refundRecord.version : refundRecord.version_ack));
		// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
		// далее, в случае успешной отправки, мы установим version_ack=version
		// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
		// version_ack (если подтверждение он не получал), то отправка будет еще раз
		// 21 версия на КПК подтвержденная
		serializer.attribute(null, "versionPDA_ack", String.format("%d", refundRecord.versionPDA_ack));
		// зарезервированные поля
		serializer.attribute(null, "shipping_type", String.format("%d", refundRecord.shipping_type));
		// строки документа
		for (RefundLineRecord line : refundRecord.lines) {
			serializer.startTag(null, "Line");
			// nomenclature_id
			serializer.attribute(null, "nomenclature_id", line.nomenclature_id.toString());
			// quantity etc
			serializer.attribute(null, "quantity", Common.DoubleToStringFormat(line.quantity, "%.2f"));
			if (line.quantity_requested != line.quantity && bKeepRequestedQuantity) {
				serializer.attribute(null, "quantity_requested", Common.DoubleToStringFormat(line.quantity_requested, "%.2f"));
			}
			serializer.attribute(null, "k", Common.DoubleToStringFormat(line.k, "%.2f"));
			serializer.attribute(null, "ed", line.ed);
			serializer.attribute(null, "comment_in_line", line.comment_in_line); // комментарий
			serializer.endTag(null, "Line");
		}

		serializer.endTag(null, "RefundPDA");
	}


	static boolean SaveSendRefunds(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;
		// один раз отправляем и забываем. если еще раз придут отсутствующие возвраты
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_refunds.entrySet()) {
			bw.write("##EMPTYREFUNDPDA##\r\n");
			SaveEmptyOrder(bw, it.getKey(), it.getValue());
		}
		//myBase.m_empty_refunds.clear();

		MySingleton g = MySingleton.getInstance();

		// Возвраты со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0 or state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				RefundRecord rec = new RefundRecord();
				if (ReadRefundBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					bw.write("##REFUNDPDA##\r\n");
					bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
					bw.write(String.format("%d\r\n", g.Common.m_mtrade_version));
					//SaveOrderToFile(bw, &rec, true);
					if (g.Common.PRODLIDER || g.Common.TANDEM) {
						// В файл записываем также и требуемое количество
						SaveRefundToText(bw, rec, true, true);
					} else {
						SaveRefundToText(bw, rec, true, false);
					}
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}
		return !bEmpty;
	}

	static void SaveEmptyRefundXML(XmlSerializer serializer, UUID uid, int version) throws IOException {
		serializer.startTag(null, "EmptyRefundPDA");
		serializer.attribute(null, "uid", uid.toString());
		serializer.attribute(null, "_version", String.format("%d", version));
		serializer.endTag(null, "EmptyRefundPDA");
	}


	static boolean SaveSendRefundsXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Refunds");

		// один раз отправляем и забываем. если еще раз придут отсутствующие возвраты
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_refunds.entrySet()) {
			SaveEmptyRefundXML(serializer, it.getKey(), it.getValue());
		}

		MySingleton g = MySingleton.getInstance();

		// Возвраты со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.REFUNDS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0 or state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				RefundRecord rec = new RefundRecord();
				if (ReadRefundBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					if (g.Common.PRODLIDER || g.Common.TANDEM) {
						// В файл записываем также и требуемое количество
						SaveRefundToXML(serializer, rec, true, true);
					} else {
						SaveRefundToXML(serializer, rec, true, false);
					}
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}

		serializer.endTag(null, "Refunds");

		serializer.endDocument();
		serializer.flush();
		// не закрываем, иначе закроется весь поток

		return !bEmpty;
	}


	static boolean LoadVisitsUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uids) {
		int visits_system_version;
		int nom_idx;
		int pos;

		Long _id = 0L;
		UUID uid = null;
		int version = 0;

		int lineno = -1;

		class VisitDataToSaveLine {
			int lineno;
			String distr_point_id;
			int version;
			int version_ack;
			int versionPDA;
			int versionPDA_ack;
		}
		;

		class VisitDataToSave {
			//int linesCount;
			ArrayList<VisitDataToSaveLine> lines;
		}
		;

		VisitDataToSave visitDataToSave = new VisitDataToSave();
		visitDataToSave.lines = new ArrayList();

		//ContentValues visitContentValues=new ContentValues();
		//ContentValues visitLineContentValues=new ContentValues();

		//boolean bRecordValid=false; // если истина, значит все строки визитов совпали
		boolean bRecordReaded = false; // если истина, значит документ в принципе найден
		// проверять сначала bRecordReaded, потом bRecordValid
		boolean bRecordAcked = false; // Если документ полностью подтвержден в 1С (версия КПК везде совпадала)
		boolean bSaveChanges = false; // Значит надо записать версии от 1С
		boolean bLastRecordReceived = false; // Простой способ проверки, что все строки совпали по количеству

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					if (bRecordReaded) {
						//int _id=0;
						//or.weightDoc=TextDatabase.GetRefundWeight(contentResolver, or, null, false);
						//or.dont_need_send=0; // этот флаг в случае, если документ на сервере, не может быть установлен
						//SaveRefundSQL(contentResolver, or, _id);
						//uids.add(or.uid);
						if (!bRecordAcked || !bLastRecordReceived) {
							// Не данные не подтверждены, убедимся, что они при обмене будут отправляться
							boolean bFix = true;
							for (VisitDataToSaveLine line : visitDataToSave.lines) {
								if (line.versionPDA != line.versionPDA_ack || line.version != line.version_ack) {
									// Эти данные будут отправляться, менять ничего не нужно
									bFix = false;
									break;
								}
							}
							if (bFix) {
								// Надо будет отправлять данные, но они будут отправляться - увеличим версии
								for (VisitDataToSaveLine line : visitDataToSave.lines) {
									line.versionPDA++;
								}
								bSaveChanges = true;
							}
						}

					} else {
						// такой заявки уже нет, сообщаем об этом
						myBase.m_empty_visits.put(uid, version);
					}
				}
				if (bSaveChanges) {
					ContentValues cv = new ContentValues();
					for (VisitDataToSaveLine line : visitDataToSave.lines) {
						cv.put("distr_point_id", line.distr_point_id);
						cv.put("version", line.version);
						cv.put("version_ack", line.version_ack);
						cv.put("versionPDA", line.versionPDA);
						cv.put("versionPDA_ack", line.versionPDA_ack);
						contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "real_route_id=? and lineno=?", new String[]{Long.toString(_id), String.valueOf(line.lineno)});
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						visits_system_version = 0;
						break;
					case -108:
						visits_system_version = Integer.parseInt(sc);
						break;
					case 0: // uid
					{
						bRecordReaded = false;
						//bRecordValid=true;
						bRecordAcked = true; // Пока расхождений нет, считаем, что подтвержден
						bSaveChanges = false;
						bLastRecordReceived = false;
						//visitContentValues.clear();
						uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						Cursor cursorRealRoutesDates = contentResolver.query(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{uid.toString()}, "route_date desc");
						if (cursorRealRoutesDates.moveToNext()) {
							bRecordReaded = true;
							_id = cursorRealRoutesDates.getLong(0);
						}
						cursorRealRoutesDates.close();
						//visitDataToSave.linesCount=0;
						visitDataToSave.lines.clear();

						Cursor cursorRealRoutesLines = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id", "lineno", "distr_point_id", "version", "version_ack", "versionPDA", "versionPDA_ack"}, "real_route_id=?", new String[]{Long.toString(_id)}, "lineno");
						int index_RealRoutesLinesLineNo = cursorRealRoutesLines.getColumnIndex("lineno");
						int index_RealRoutesLinesDistrPointId = cursorRealRoutesLines.getColumnIndex("distr_point_id");
						int index_RealRoutesLinesVersion = cursorRealRoutesLines.getColumnIndex("version");
						int index_RealRoutesLinesVersionAck = cursorRealRoutesLines.getColumnIndex("version_ack");
						int index_RealRoutesLinesVersionPDA = cursorRealRoutesLines.getColumnIndex("versionPDA");
						int index_RealRoutesLinesVersionPDA_ack = cursorRealRoutesLines.getColumnIndex("versionPDA_ack");
						while (cursorRealRoutesLines.moveToNext()) {
							VisitDataToSaveLine line = new VisitDataToSaveLine();
							line.lineno = cursorRealRoutesLines.getInt(index_RealRoutesLinesLineNo);
							line.distr_point_id = cursorRealRoutesLines.getString(index_RealRoutesLinesDistrPointId);
							line.version = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersion);
							line.version_ack = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionAck);
							line.versionPDA = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA);
							line.versionPDA_ack = cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA_ack);
							visitDataToSave.lines.add(line);
							//if (line.lineno!=visitDataToSave.lines.size())
							//{
							//    bRecordValid=false;
							//}
						}
						cursorRealRoutesLines.close();
						break;
					}
					case 1: // id
						// TODO
						//visitContentValues.put("id", sc);
						break;
					case 2: // дата документа
						// TODO
						//visitContentValues.put("route_date", sc);
						break;
					//case 3: // version
					//or.version=Integer.parseInt(sc);
					//or.versionPDA=0;
					//or.version_ack=0;
					//or.versionPDA_ack=0;
					//break;
					//case 4: // closed_mark и т.п., в КПК не все используется
					//if ()
					//or.closed_not_full=(Integer.parseInt(sc)&4)!=0?1:0;
					//break;
					//case 5: // state
					//or.state=E_REFUND_STATE.E_REFUND_STATE_CREATED;
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_LOADED, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_LOADED;
					//else
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED;
					//else
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_COMPLETED, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_COMPLETED;
					//else
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL;
					//else
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_CANCELED, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_CANCELED;
					//else
					//if (sc.equals(myBase.GetRefundStateDescr(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT, null)))
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT;
					//else
					//	or.state=E_REFUND_STATE.E_REFUND_STATE_UNKNOWN;
					//	break;
					//case 6: // comment_closing
					//{
					//	//or.comment_closing=new String(sc);
					//	break;
					//}
					//case 7: // agreement_id
					//or.agreement_id=new MyID(sc);
					//break;
					//case 8: // distr_point_id
					//or.distr_point_id=new MyID(sc);
					//break;
					//case 9: // versionPDA
					//or.versionPDA=Integer.parseInt(sc);
					//break;
					//case 10: // versionPDA_ack
					//or.versionPDA_ack=Integer.parseInt(sc);
					//break;
					//case 11: // version_ack
					//or.version_ack=Integer.parseInt(sc);
					//break;
					// зарезервированные поля
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						break;
					case 8:
						nom_idx = 120 - 1;
						break;
					case 120:
						// Номер строки, нумерация с 1
						lineno = Integer.parseInt(sc);
						if (lineno <= 0 || lineno > visitDataToSave.lines.size()) {
							lineno = -1;
							bRecordAcked = false;
						}
						if (lineno == visitDataToSave.lines.size())
							bLastRecordReceived = true;
						//line.nomenclature_id=new MyID(sc);
						/*
						VisitDataToSaveLine line=new VisitDataToSaveLine();
						line.lineno=cursorRealRoutesLines.getInt(index_RealRoutesLinesLineNo);
						line.distr_point_id=cursorRealRoutesLines.getString(index_RealRoutesLinesDistrPointId);
						line.version=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersion);
						line.version_ack=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionAck);
						line.versionPDA=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA);
						line.versionPDA_ack=cursorRealRoutesLines.getInt(index_RealRoutesLinesVersionPDA_ack);
						visitDataToSave.lines.add(line);
						*/
						break;
					case 121: // Версия
						if (lineno > 0) {
							int _version = Integer.parseInt(sc);
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.version != _version) {
								line.version = _version;
								bSaveChanges = true;
							}
						}
						break;
					case 122: // ВерсияПодтвержденная
						if (lineno > 0) {
							int _versionAck = Integer.parseInt(sc);
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.version_ack != _versionAck) {
								line.version_ack = _versionAck;
								bSaveChanges = true;
							}
						}
						break;
					case 123: // ВерсияКПК
						if (lineno > 0) {
							int _versionPDA = Integer.parseInt(sc);
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.versionPDA != _versionPDA) {
								bRecordAcked = false;
								bSaveChanges = true;
							}
						}
						break;
					case 124: // ВерсияКПКПодтвержденная
						if (lineno > 0) {
							int versionPDA_ack = Integer.parseInt(sc);
							VisitDataToSaveLine line = visitDataToSave.lines.get(lineno - 1);
							if (line.versionPDA_ack != versionPDA_ack) {
								line.versionPDA_ack = versionPDA_ack;
								bSaveChanges = true;
							}
							if (line.versionPDA != versionPDA_ack) {
								bRecordAcked = false;
							}
						}
						break;
					case 125: // ДатаВремяКоординат
						break;
					case 126: // Широта
						break;
					case 127: // Долгота
						break;
					case 128: // ТорговаяТочка
						break;
					case 129: // ВремяНачалаВизита
						break;
					case 130: // ВремяОкончанияВизита
						break;
					case 131:
					case 132:
					case 133:
						break;
					case 134:
						nom_idx = 120 - 1; // продолжаем строки
						break;
				}
				nom_idx++;
			}

		}
		if (nom_idx >= 6) {
			if (bRecordReaded) {
				//int _id=0;
				//or.weightDoc=TextDatabase.GetRefundWeight(contentResolver, or, null, false);
				//or.dont_need_send=0; // этот флаг в случае, если документ на сервере, не может быть установлен
				//SaveRefundSQL(contentResolver, or, _id);
				//uids.add(or.uid);
				if (!bRecordAcked || !bLastRecordReceived) {
					// Не данные не подтверждены, убедимся, что они при обмене будут отправляться
					boolean bFix = true;
					for (VisitDataToSaveLine line : visitDataToSave.lines) {
						if (line.versionPDA != line.versionPDA_ack || line.version != line.version_ack) {
							// Эти данные будут отправляться, менять ничего не нужно
							bFix = false;
							break;
						}
					}
					if (bFix) {
						// Надо будет отправлять данные, но они будут отправляться - увеличим версии
						for (VisitDataToSaveLine line : visitDataToSave.lines) {
							line.versionPDA++;
						}
						bSaveChanges = true;
					}
				}

			} else {
				// такой заявки уже нет, сообщаем об этом
				myBase.m_empty_visits.put(uid, version);
			}
		}
		if (bSaveChanges) {
			ContentValues cv = new ContentValues();
			for (VisitDataToSaveLine line : visitDataToSave.lines) {
				cv.put("distr_point_id", line.distr_point_id);
				cv.put("version", line.version);
				cv.put("version_ack", line.version_ack);
				cv.put("versionPDA", line.versionPDA);
				cv.put("versionPDA_ack", line.versionPDA_ack);
				contentResolver.update(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, cv, "real_route_id=? and lineno=?", new String[]{Long.toString(_id), String.valueOf(line.lineno)});
			}
		}

		return true;
	}

	static void SaveEmptyVisit(BufferedWriter bw, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса визита, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
		// далее отличие от нормальной процедуры
		bw.write("@@@\r\n");
		bw.write(uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", version));
		// версия на КПК - пропускаем
		// ...
	}

	static void SaveEmptyVisitXML(XmlSerializer serializer, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса визита, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		serializer.startTag(null, "EmptyVisitPDA");
		serializer.attribute(null, "uid", uid.toString());
		serializer.attribute(null, "_version", String.format("%d", version));
		serializer.endTag(null, "EmptyVisitPDA");

	}


	static boolean SaveSendVisits(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, ArrayList<Long> ids) throws IOException {
		boolean bEmpty = true;
		// один раз отправляем и забываем. если еще раз придут отсутствующие возвраты
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_visits.entrySet()) {
			bw.write("##EMPTYVISITPDA##\r\n");
			SaveEmptyVisit(bw, it.getKey(), it.getValue());
		}
		//myBase.m_empty_visits.clear();

		MySingleton g = MySingleton.getInstance();

		HashSet<Long> routesHashSet = new HashSet<>();
		// Сначала найдем все визиты, которые надо отправить
		// здесь бы distinct в запросе не помешал
		Cursor cursor = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"real_route_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack)", null, null);
		int _idIndex = cursor.getColumnIndex("real_route_id");
		while (cursor.moveToNext()) {
			routesHashSet.add(cursor.getLong(_idIndex));
		}
		cursor.close();
		Iterator<Long> iterator = routesHashSet.iterator();
		while (iterator.hasNext()) {
			boolean bVersionAsAcked = true;
			Long _id = iterator.next();
			Cursor realRouteDateCursor = contentResolver.query(ContentUris.withAppendedId(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, _id), new String[]{"_id", "uid", "id", "route_date", "route_id", "route_descr"}, null, null, null);
			int index_RouteDateUid = realRouteDateCursor.getColumnIndex("uid");
			int index_RouteDateId = realRouteDateCursor.getColumnIndex("id");
			int index_RouteDate = realRouteDateCursor.getColumnIndex("route_date");

			while (realRouteDateCursor.moveToNext()) {

				UUID realRouteDateUid = UUID.fromString(realRouteDateCursor.getString(index_RouteDateUid));
				String realRouteDateId = realRouteDateCursor.getString(index_RouteDateId);
				String realRouteDate = realRouteDateCursor.getString(index_RouteDate);

				if (bEmpty) {
					bEmpty = false;
				}
				bw.write("##VISITPDA##\r\n");
				bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
				bw.write(String.format("%d\r\n", g.Common.m_mtrade_version));

				bw.write("@@@\r\n");
				bw.write(realRouteDateUid.toString()); // 0
				bw.write("\r\n");
				bw.write(String.format("%s\r\n", realRouteDateId)); // 1
				bw.write(String.format("%s\r\n", realRouteDate)); // 2
				bw.write("-1\r\n"); // 3
				bw.write("-1\r\n"); // 4
				bw.write("-1\r\n"); // 5
				bw.write("-1\r\n"); // 6
				bw.write("-1\r\n"); // 7
				bw.write("-1\r\n"); // 8

				Cursor realRouteLinesCursor = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id", "lineno", "distr_point_id", "start_visit_time", "end_visit_time", "version", "version_ack", "versionPDA", "versionPDA_ack", "datecoord", "latitude", "longitude"}, "real_route_id=?", new String[]{Long.toString(_id)}, "lineno");
				int index__id = realRouteLinesCursor.getColumnIndex("_id");
				int index_RouteLinesLineno = realRouteLinesCursor.getColumnIndex("lineno");
				int index_RouteLinesDistrPointId = realRouteLinesCursor.getColumnIndex("distr_point_id");
				int index_RouteLinesStartVivitTime = realRouteLinesCursor.getColumnIndex("start_visit_time");
				int index_RouteLinesEndVivitTime = realRouteLinesCursor.getColumnIndex("end_visit_time");
				int index_RouteLinesVersion = realRouteLinesCursor.getColumnIndex("version");
				int index_RouteLinesVersionAck = realRouteLinesCursor.getColumnIndex("version_ack");
				int index_RouteLinesVersionPDA = realRouteLinesCursor.getColumnIndex("versionPDA");
				int index_RouteLinesVersionPDAAck = realRouteLinesCursor.getColumnIndex("versionPDA_ack");
				int index_RouteLinesDatecoord = realRouteLinesCursor.getColumnIndex("datecoord");
				int index_RouteLinesLatitude = realRouteLinesCursor.getColumnIndex("latitude");
				int index_RouteLinesLongitude = realRouteLinesCursor.getColumnIndex("longitude");
				while (realRouteLinesCursor.moveToNext()) {
					// Номер строки 120
					bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesLineno)));
					// версия на сервере 121
					bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesVersion)));
					// версия на сервере 122
					if (bVersionAsAcked)
						bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesVersion)));
					else
						bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesVersionAck)));
					// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
					// далее, в случае успешной отправки, мы установим version_ack=version
					// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
					// version_ack (если подтверждение он не получал), то отправка будет еще раз
					// версия на КПК 123
					bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesVersionPDA)));
					// версия на КПК подтвержденная 124
					bw.write(String.format("%d\r\n", realRouteLinesCursor.getInt(index_RouteLinesVersionPDAAck)));
					// дата координат 125
					bw.write(String.format("%s\r\n", realRouteLinesCursor.getString(index_RouteLinesDatecoord)));
					// широта, долгота 126
					bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(realRouteLinesCursor.getDouble(index_RouteLinesLatitude), "%.6f")));
					// 127
					bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(realRouteLinesCursor.getDouble(index_RouteLinesLongitude), "%.6f")));
					// торговая точка 128
					bw.write(String.format("%s\r\n", realRouteLinesCursor.getString(index_RouteLinesDistrPointId)));
					// время начала визита 129
					bw.write(String.format("%s\r\n", realRouteLinesCursor.getString(index_RouteLinesStartVivitTime)));
					// время окончания визита 130
					bw.write(String.format("%s\r\n", realRouteLinesCursor.getString(index_RouteLinesEndVivitTime)));
					// зарезервированные
					bw.write("-1\r\n");
					bw.write("-1\r\n");
					bw.write("-1\r\n");
					bw.write("-1\r\n");

					ids.add(realRouteLinesCursor.getLong(index__id));

				}
				realRouteLinesCursor.close();

                /*
                if (g.Common.PRODLIDER||g.Common.TANDEM)
                {
                    // В файл записываем также и требуемое количество
                    SaveRefundToText(bw, rec, true, true);
                } else
                {
                    SaveRefundToText(bw, rec, true, false);
                }
                */
			}
			realRouteDateCursor.close();
		}
		cursor.close();
		return !bEmpty;
	}

	static boolean SaveSendVisitsXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, ArrayList<Long> ids) throws IOException {
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Visits");

		// один раз отправляем и забываем. если еще раз придут отсутствующие визиты,
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_visits.entrySet()) {
			SaveEmptyVisitXML(serializer, it.getKey(), it.getValue());
		}

		MySingleton g = MySingleton.getInstance();

		HashSet<Long> routesHashSet = new HashSet<>();
		// Сначала найдем все визиты, которые надо отправить
		// здесь бы distinct в запросе не помешал
		Cursor cursor = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"real_route_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack)", null, null);
		int _idIndex = cursor.getColumnIndex("real_route_id");
		while (cursor.moveToNext()) {
			routesHashSet.add(cursor.getLong(_idIndex));
		}
		cursor.close();
		Iterator<Long> iterator = routesHashSet.iterator();
		while (iterator.hasNext()) {
			boolean bVersionAsAcked = true;
			Long _id = iterator.next();
			Cursor realRouteDateCursor = contentResolver.query(ContentUris.withAppendedId(MTradeContentProvider.REAL_ROUTES_DATES_CONTENT_URI, _id), new String[]{"_id", "uid", "id", "route_date", "route_id", "route_descr"}, null, null, null);
			int index_RouteDateUid = realRouteDateCursor.getColumnIndex("uid");
			int index_RouteDateId = realRouteDateCursor.getColumnIndex("id");
			int index_RouteDate = realRouteDateCursor.getColumnIndex("route_date");

			while (realRouteDateCursor.moveToNext()) {

				UUID realRouteDateUid = UUID.fromString(realRouteDateCursor.getString(index_RouteDateUid));
				String realRouteDateId = realRouteDateCursor.getString(index_RouteDateId);
				String realRouteDate = realRouteDateCursor.getString(index_RouteDate);

				if (bEmpty) {
					bEmpty = false;
				}

				serializer.startTag(null, "VisitPDA");

				serializer.attribute(null, "real_route_date_uid", realRouteDateUid.toString()); // 0
				serializer.attribute(null, "real_route_date_id", realRouteDateId); // 1
				serializer.attribute(null, "real_route_date", realRouteDate); // 2

				Cursor realRouteLinesCursor = contentResolver.query(MTradeContentProvider.REAL_ROUTES_LINES_CONTENT_URI, new String[]{"_id", "lineno", "distr_point_id", "start_visit_time", "end_visit_time", "version", "version_ack", "versionPDA", "versionPDA_ack", "datecoord", "latitude", "longitude"}, "real_route_id=?", new String[]{Long.toString(_id)}, "lineno");
				int index__id = realRouteLinesCursor.getColumnIndex("_id");
				int index_RouteLinesLineno = realRouteLinesCursor.getColumnIndex("lineno");
				int index_RouteLinesDistrPointId = realRouteLinesCursor.getColumnIndex("distr_point_id");
				int index_RouteLinesStartVivitTime = realRouteLinesCursor.getColumnIndex("start_visit_time");
				int index_RouteLinesEndVivitTime = realRouteLinesCursor.getColumnIndex("end_visit_time");
				int index_RouteLinesVersion = realRouteLinesCursor.getColumnIndex("version");
				int index_RouteLinesVersionAck = realRouteLinesCursor.getColumnIndex("version_ack");
				int index_RouteLinesVersionPDA = realRouteLinesCursor.getColumnIndex("versionPDA");
				int index_RouteLinesVersionPDAAck = realRouteLinesCursor.getColumnIndex("versionPDA_ack");
				int index_RouteLinesDatecoord = realRouteLinesCursor.getColumnIndex("datecoord");
				int index_RouteLinesLatitude = realRouteLinesCursor.getColumnIndex("latitude");
				int index_RouteLinesLongitude = realRouteLinesCursor.getColumnIndex("longitude");
				while (realRouteLinesCursor.moveToNext()) {

					serializer.startTag(null, "Line");
					// Номер строки 120
					serializer.attribute(null, "lineno", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesLineno)));
					// версия на сервере 121
					serializer.attribute(null, "_version", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesVersion)));
					// версия на сервере 122
					if (bVersionAsAcked)
						serializer.attribute(null, "version_ack", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesVersion)));
					else
						serializer.attribute(null, "version_ack", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesVersionAck)));
					// ^^^^ это правильно, не менять, иначе телефон будет постоянно отправлять
					// далее, в случае успешной отправки, мы установим version_ack=version
					// т.е. мы отправим только один раз но в случае если нам сервера пришлют другое значение
					// version_ack (если подтверждение он не получал), то отправка будет еще раз
					// версия на КПК 123
					serializer.attribute(null, "versionPDA", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesVersionPDA)));
					// версия на КПК подтвержденная 124
					serializer.attribute(null, "versionPDA_ack", String.format("%d", realRouteLinesCursor.getInt(index_RouteLinesVersionPDAAck)));
					// дата координат 125
					serializer.attribute(null, "datecoord", realRouteLinesCursor.getString(index_RouteLinesDatecoord));
					// широта, долгота 126
					serializer.attribute(null, "latitude", Common.DoubleToStringFormat(realRouteLinesCursor.getDouble(index_RouteLinesLatitude), "%.6f"));
					// 127
					serializer.attribute(null, "longitude", Common.DoubleToStringFormat(realRouteLinesCursor.getDouble(index_RouteLinesLongitude), "%.6f"));
					// торговая точка 128
					serializer.attribute(null, "distr_point_id", realRouteLinesCursor.getString(index_RouteLinesDistrPointId));
					// время начала визита 129
					serializer.attribute(null, "start_visit_time", realRouteLinesCursor.getString(index_RouteLinesStartVivitTime));
					// время окончания визита 130
					serializer.attribute(null, "end_visit_time", realRouteLinesCursor.getString(index_RouteLinesEndVivitTime));

					ids.add(realRouteLinesCursor.getLong(index__id));

					serializer.endTag(null, "Line");
				}
				realRouteLinesCursor.close();

				serializer.endTag(null, "VisitPDA");

			}
			realRouteDateCursor.close();
		}
		cursor.close();

		serializer.endTag(null, "Visits");

		serializer.endDocument();
		serializer.flush();

		return !bEmpty;
	}


	static boolean LoadPaymentsUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf, ArrayList<UUID> uidsPayments) {
		int payments_system_version;
		int nom_idx;
		int pos;

		CashPaymentRecord or = new CashPaymentRecord();

		boolean bRecordValid = false;

		nom_idx = -109;
		boolean bCancelLoad = false;
		String split[] = buf.split("\n");
		for (String sc0 : split) {
			if (bCancelLoad)
				break;
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty() && nom_idx == -109)
				continue;
			if (sc.equals("@@@")) {
				if (nom_idx >= 6) {
					if (bRecordValid) {
						int _id = 0;
						SavePaymentSQL(contentResolver, myBase, or, _id);
						uidsPayments.add(or.uid);
					} else {
						// такого документа уже нет, сообщаем об этом
						myBase.m_empty_payments.put(or.uid, or.version);
					}
				}
				nom_idx = 0;
			} else {
				switch (nom_idx) {
					case -109:
						payments_system_version = 0;
						break;
					case -108:
						payments_system_version = Integer.parseInt(sc);
						break;
					case 0: // uid
					{
						bRecordValid = false;
						or.uid = UUID.fromString(sc.replace("{", "").replace("}", ""));
						Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"_id"}, "uid=?", new String[]{or.uid.toString()}, "datedoc desc");
						if (cursor.moveToNext()) {
							Long _id = cursor.getLong(0);
							if (ReadPaymentBy_Id(contentResolver, or, _id)) {
								bRecordValid = true;
							}
						}
						cursor.close();
						break;
					}
					case 1: // id
						or.id = new MyID(sc);
						break;
					case 2: // numdoc
						or.numdoc = new String(sc);
						break;
					case 3: // version
						or.version = Integer.parseInt(sc);
						or.versionPDA = 0;
						or.version_ack = 0;
						or.versionPDA_ack = 0;
						break;
					case 4: // datedoc
						if (sc.length() == 14) {
							or.datedoc = new String(sc);
							break;
						}
						break;
					case 5: // state
						or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED;
						if (sc.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_LOADED, null)))
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_LOADED;
						else if (sc.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_ACKNOWLEDGED, null)))
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_ACKNOWLEDGED;
						else if (sc.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_COMPLETED, null)))
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_COMPLETED;
						else if (sc.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL, null)))
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL;
						else if (sc.equals(myBase.GetPaymentStateDescr(E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED, null)))
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED;
						else
							or.state = E_PAYMENT_STATE.E_PAYMENT_STATE_UNKNOWN;
						break;
					case 6: // comment
						or.comment = new String(sc);
						break;
					case 7: // comment_closing
						or.comment_closing = new String(sc);
						break;
					case 8: // client_id
						or.client_id = new MyID(sc);
						break;
					case 9: // agreement_id
						or.agreement_id = new MyID(sc);
						break;
					case 10: // versionPDA
						or.versionPDA = Integer.parseInt(sc);
						break;
					case 11: // versionPDA_ack
						or.versionPDA_ack = Integer.parseInt(sc);
						break;
					case 12: // version_ack
						or.version_ack = Integer.parseInt(sc);
						break;
					case 13: // manager_id
						or.manager_id = new MyID(sc);
						break;
					case 14: // sumDoc
						or.sumDoc = Double.parseDouble(sc);
						break;
					case 15: // manager_descr
						or.stuff_manager_name = new String(sc);
						break;
					case 16: // organization_descr
						or.stuff_organization_name = new String(sc);
						break;
					case 17:
						if (sc.equals("-1"))
							or.vicarious_power_id = new MyID();
						else
							or.vicarious_power_id = new MyID(sc);
						break;
					case 18:
						if (sc.equals("-1") && or.vicarious_power_id.isEmpty())
							or.vicarious_power_descr = "";
						else
							or.vicarious_power_descr = new String(sc);
						break;
					case 19:
						break;
				}
				nom_idx++;
			}
		}
		if (nom_idx >= 6) {
			if (bRecordValid) {
				int _id = 0;
				SavePaymentSQL(contentResolver, myBase, or, _id);
				uidsPayments.add(or.uid);
			} else {
				// такой заявки уже нет, сообщаем об этом
				myBase.m_empty_payments.put(or.uid, or.version);
			}
		}
		return true;
	}

	static void SavePaymentToXML(XmlSerializer serializer, CashPaymentRecord paymentRecord, boolean bVersionAsAcked) throws IOException {
		MySingleton g = MySingleton.getInstance();

		serializer.startTag(null, "PaymentRecord");
		// 0 uid
		serializer.attribute(null, "uid", paymentRecord.uid.toString());
		// 1 id
		serializer.attribute(null, "id", paymentRecord.id.toString());
		// 2 numdoc
		serializer.attribute(null, "numdoc", paymentRecord.numdoc);
		// 3 версия на сервере
		serializer.attribute(null, "_version", String.format("%d", paymentRecord.version));
		// 4 datedoc
		serializer.attribute(null, "datedoc", paymentRecord.datedoc);
		// 5 state
		serializer.attribute(null, "state", String.format("%d", paymentRecord.state.value()));
		// 6 comment
		serializer.attribute(null, "comment", Common.NormalizeString(paymentRecord.comment));
		// 7 comment_closing
		serializer.attribute(null, "comment_closing", Common.NormalizeString(paymentRecord.comment_closing));
		// 8 client_id
		serializer.attribute(null, "client_id", paymentRecord.client_id.toString());
		// 9 agreement_id
		serializer.attribute(null, "agreement_id", paymentRecord.agreement_id.toString());
		// 10 версия на КПК
		serializer.attribute(null, "versionPDA", String.format("%d", paymentRecord.versionPDA));
		// 11 версия на КПК подтвержденная
		serializer.attribute(null, "versionPDA_ack", String.format("%d", paymentRecord.versionPDA_ack));
		// 12 версия подтвержденная
		serializer.attribute(null, "version_ack", String.format("%d", bVersionAsAcked ? paymentRecord.version : paymentRecord.version_ack));
		// 13 manager_id
		serializer.attribute(null, "manager_id", paymentRecord.manager_id.toString());
		// 14 сумма
		serializer.attribute(null, "sumDoc", Common.DoubleToStringFormat(paymentRecord.sumDoc, "%.2f"));
		// резерв
		serializer.attribute(null, "stuff_manager_name", paymentRecord.stuff_manager_name);
		serializer.attribute(null, "stuff_organization_name", paymentRecord.stuff_organization_name);
		serializer.attribute(null, "vicarious_power_id", paymentRecord.vicarious_power_id.toString());
		serializer.attribute(null, "vicarious_power_descr", paymentRecord.vicarious_power_descr);
		// 3.71
		serializer.attribute(null, "datecoord", paymentRecord.datecoord);
		serializer.attribute(null, "latitude", Common.DoubleToStringFormat(paymentRecord.latitude, "%.6f"));
		serializer.attribute(null, "longitude", Common.DoubleToStringFormat(paymentRecord.longitude, "%.6f"));
		// 3.80
		serializer.attribute(null, "distr_point_id", paymentRecord.distr_point_id.toString());

		serializer.endTag(null, "PaymentRecord");
	}


	static void SavePaymentToText(BufferedWriter bw, CashPaymentRecord paymentRecord, boolean bVersionAsAcked) throws IOException {
		bw.write("@@@\r\n");
		// 0 uid
		bw.write(paymentRecord.uid.toString());
		bw.write("\r\n");
		// 1 id
		bw.write(String.format("%s\r\n", paymentRecord.id.toString()));
		// 2 numdoc
		bw.write(String.format("%s\r\n", paymentRecord.numdoc));
		// 3 версия на сервере
		bw.write(String.format("%d\r\n", paymentRecord.version));
		// 4 datedoc
		bw.write(String.format("%s\r\n", paymentRecord.datedoc));
		// 5 state
		bw.write(String.format("%d\r\n", paymentRecord.state.value()));
		// 6 comment
		bw.write(String.format("%s\r\n", Common.NormalizeString(paymentRecord.comment)));
		// 7 comment_closing
		bw.write(String.format("%s\r\n", Common.NormalizeString(paymentRecord.comment_closing)));
		// 8 client_id
		bw.write(String.format("%s\r\n", paymentRecord.client_id.toString()));
		// 9 agreement_id
		bw.write(String.format("%s\r\n", paymentRecord.agreement_id.toString()));
		// 10 версия на КПК
		bw.write(String.format("%d\r\n", paymentRecord.versionPDA));
		// 11 версия на КПК подтвержденная
		bw.write(String.format("%d\r\n", paymentRecord.versionPDA_ack));
		// 12 версия подтвержденная
		bw.write(String.format("%d\r\n", bVersionAsAcked ? paymentRecord.version : paymentRecord.version_ack));
		// 13 manager_id
		bw.write(String.format("%s\r\n", paymentRecord.manager_id.toString()));
		// 14 сумма
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(paymentRecord.sumDoc, "%.2f")));
		// резерв
		bw.write(String.format("%s\r\n", paymentRecord.stuff_manager_name));
		bw.write(String.format("%s\r\n", paymentRecord.stuff_organization_name));
		bw.write(String.format("%s\r\n", paymentRecord.vicarious_power_id.toString()));
		bw.write(String.format("%s\r\n", paymentRecord.vicarious_power_descr));
		// 3.71
		bw.write(String.format("%s\r\n", paymentRecord.datecoord));
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(paymentRecord.latitude, "%.6f")));
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(paymentRecord.longitude, "%.6f")));

		// 3.80
		bw.write(String.format("%s\r\n", paymentRecord.distr_point_id.toString()));
	}


	static void SaveEmptyPayment(BufferedWriter bw, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса заказа, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
		// далее отличие от нормальной процедуры
		bw.write("@@@\r\n");
		bw.write(uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", version));
		// версия на КПК - пропускаем
		// ...
	}

	static void SaveEmptyPaymentXML(XmlSerializer serializer, UUID uid, int version) throws IOException {
		serializer.startTag(null, "EmptyPaymentPDA");
		serializer.attribute(null, "uid", uid.toString());
		serializer.attribute(null, "_version", String.format("%d", version));
		serializer.endTag(null, "EmptyPaymentPDA");

	}


	static boolean SaveSendPayments(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		// один раз отправляем и забываем. если еще раз придут отсутствующие документы
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_payments.entrySet()) {
			bw.write("##EMPTYPAYMENTPDA##\r\n");
			SaveEmptyPayment(bw, it.getKey(), it.getValue());
		}
		//myBase.m_empty_payments.clear();

		//QSqlQuery query;
		//if (!query.exec("select _id from orders where (versionPDA_ack<>versionPDA or version<>version_ack) and dont_need_send=0"))
		//    qDebug()<<query.lastError();
		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) or state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				CashPaymentRecord rec = new CashPaymentRecord();
				if (ReadPaymentBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					bw.write("##PAYMENTPDA##\r\n");
					bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
					bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
					SavePaymentToText(bw, rec, true);
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}
		return !bEmpty;
	}

	static void SaveDistribsToText(BufferedWriter bw, DistribsRecord distribsRecord, boolean bVersionAsAcked) throws IOException {
		bw.write("@@@\r\n");
		// 0 uid
		bw.write(distribsRecord.uid.toString());
		bw.write("\r\n");
		// 1 версия на сервере
		bw.write(String.format("%d\r\n", distribsRecord.version));
		// 2 версия КПК
		bw.write(String.format("%d\r\n", distribsRecord.versionPDA));
		// 3 id
		bw.write(String.format("%s\r\n", distribsRecord.id.toString()));
		// 4 numdoc
		bw.write(String.format("%s\r\n", distribsRecord.numdoc));
		// 5 datedoc
		bw.write(String.format("%s\r\n", distribsRecord.datedoc));
		// 6 client_id
		bw.write(String.format("%s\r\n", distribsRecord.client_id.toString()));
		// 7 comment
		bw.write(String.format("%s\r\n", Common.NormalizeString(distribsRecord.comment)));
		// 8 дата координат
		bw.write(String.format("%s\r\n", distribsRecord.datecoord));
		// 9 широта
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(distribsRecord.latitude, "%.6f")));
		// 10 долгота
		bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(distribsRecord.longitude, "%.6f")));
		// 11 версия подтвержденная
		bw.write(String.format("%d\r\n", bVersionAsAcked ? distribsRecord.version : distribsRecord.version_ack));
		// 12 версия КПК подтвержденная
		bw.write(String.format("%d\r\n", distribsRecord.versionPDA_ack));
		// 13 state
		bw.write(String.format("%d\r\n", distribsRecord.state.value()));
		// 14 distr_point_id
		bw.write(String.format("%s\r\n", distribsRecord.distr_point_id.toString()));
		// 15 curator_id
		bw.write(String.format("%s\r\n", distribsRecord.curator_id.toString()));
		// резерв
		bw.write("-1\r\n");
		bw.write("-1\r\n");
		bw.write("-1\r\n");

		// строки документа
		for (DistribsLineRecord line : distribsRecord.lines) {
			// distribs_contract_id
			bw.write(String.format("%s\r\n", line.distribs_contract_id.toString()));
			// quantity
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(line.quantity, "%.2f")));
			// резерв
			bw.write("-1\r\n");
			bw.write("-1\r\n");
			bw.write("-1\r\n");
			bw.write("-1\r\n");
		}

	}

	static void SaveDistribsToXML(XmlSerializer serializer, DistribsRecord distribsRecord, boolean bVersionAsAcked) throws IOException {
		serializer.startTag(null, "DistribPDA");
		// 0 uid
		serializer.attribute(null, "uid", distribsRecord.uid.toString());
		// 1 версия на сервере
		serializer.attribute(null, "_version", String.format("%d", distribsRecord.version));
		// 2 версия КПК
		serializer.attribute(null, "versionPDA", String.format("%d", distribsRecord.versionPDA));
		// 3 id
		serializer.attribute(null, "id", distribsRecord.id.toString());
		// 4 numdoc
		serializer.attribute(null, "numdoc", distribsRecord.numdoc);
		// 5 datedoc
		serializer.attribute(null, "datedoc", distribsRecord.datedoc);
		// 6 client_id
		serializer.attribute(null, "client_id", distribsRecord.client_id.toString());
		// 7 comment
		serializer.attribute(null, "comment", Common.NormalizeString(distribsRecord.comment));
		// 8 дата координат
		serializer.attribute(null, "datecoord", distribsRecord.datecoord);
		// 9 широта
		serializer.attribute(null, "latitude", Common.DoubleToStringFormat(distribsRecord.latitude, "%.6f"));
		// 10 долгота
		serializer.attribute(null, "longitude", Common.DoubleToStringFormat(distribsRecord.longitude, "%.6f"));
		// 11 версия подтвержденная
		serializer.attribute(null, "version_ack", String.format("%d", bVersionAsAcked ? distribsRecord.version : distribsRecord.version_ack));
		// 12 версия КПК подтвержденная
		serializer.attribute(null, "versionPDA_ack", String.format("%d", distribsRecord.versionPDA_ack));
		// 13 state
		serializer.attribute(null, "state", String.format("%d", distribsRecord.state.value()));
		// 14 distr_point_id
		serializer.attribute(null, "distr_point_id", distribsRecord.distr_point_id.toString());
		// 15 curator_id
		serializer.attribute(null, "curator_id", distribsRecord.curator_id.toString());

		// строки документа
		for (DistribsLineRecord line : distribsRecord.lines) {
			serializer.startTag(null, "Line");
			// distribs_contract_id
			serializer.attribute(null, "distribs_contract_id", line.distribs_contract_id.toString());
			// quantity
			serializer.attribute(null, "quantity", Common.DoubleToStringFormat(line.quantity, "%.2f"));
			serializer.endTag(null, "Line");
		}

		serializer.endTag(null, "DistribPDA");

	}


	static void SaveEmptyDistribs(BufferedWriter bw, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса заказа, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
		bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
		// далее отличие от нормальной процедуры
		bw.write("@@@\r\n");
		bw.write(uid.toString());
		bw.write("\r\n");
		// версия на сервере
		bw.write(String.format("%d\r\n", version));
		// версия на КПК - пропускаем
		// ...
	}

	static void SaveEmptyDistribsXML(XmlSerializer serializer, UUID uid, int version) throws IOException {
		// заляп на случай что придет с сервера изменение статуса заказа, а у нас такого нет
		// и чтобы оно не ходило бесконечно, отправим пустую заявку
		serializer.startTag(null, "EmptyDistribsPDA");
		serializer.attribute(null, "uid", uid.toString());
		serializer.attribute(null, "_version", String.format("%d", version));
		serializer.endTag(null, "EmptyDistribsPDA");

	}


	static boolean SaveSendDistribs(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		// один раз отправляем и забываем. если еще раз придут отсутствующие документы
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_distribs.entrySet()) {
			bw.write("##EMPTYDISTRIBSPDA##\r\n");
			SaveEmptyDistribs(bw, it.getKey(), it.getValue());
		}
		//myBase.m_empty_distribs.clear();

		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) or state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				DistribsRecord rec = new DistribsRecord();
				if (ReadDistribsBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					bw.write("##DISTRIBSPDA##\r\n");
					bw.write(String.format("%d\r\n", 0)); // устаревшее, версия заявок
					bw.write(String.format("%d\r\n", MySingleton.getInstance().Common.m_mtrade_version));
					SaveDistribsToText(bw, rec, true);
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}

		return !bEmpty;
	}

	static boolean SaveSendDistribsXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, ArrayList<UUID> uids) throws IOException {
		boolean bEmpty = true;

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Distribs");

		// один раз отправляем и забываем. если еще раз придут отсутствующие документы
		// тогда отправятся еще раз
		for (Map.Entry<UUID, Integer> it : myBase.m_empty_distribs.entrySet()) {
			SaveEmptyDistribsXML(serializer, it.getKey(), it.getValue());
		}

		// Заявки со статусом "Запрос отмены отправляем всегда"
		Cursor cursor = contentResolver.query(MTradeContentProvider.DISTRIBS_CONTENT_URI, new String[]{"_id"}, "(versionPDA_ack<>versionPDA or version<>version_ack) or state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value(), null, null);
		if (cursor != null) {
			int _idIndex = cursor.getColumnIndex("_id");
			while (cursor.moveToNext()) {
				DistribsRecord rec = new DistribsRecord();
				if (ReadDistribsBy_Id(contentResolver, rec, cursor.getLong(_idIndex))) {
					if (bEmpty) {
						bEmpty = false;
					}
					SaveDistribsToXML(serializer, rec, true);
					uids.add(rec.uid);
				}
			}
			cursor.close();
		}

		serializer.endTag(null, "Distribs");

		serializer.endDocument();
		serializer.flush();

		return !bEmpty;
	}


	static boolean SaveSendMessages(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, boolean bExcludeImageMessages) throws IOException {
		boolean bEmpty = true;

	    /*
	    Клиент                Сервер
	    отправка 4   ->       обратный ответ 4+1
	    отправка 4   ->       обратный ответ 4+1
	    при получении ответа 4+1 удаляем у себя сообщение

	    Клиент                Сервер
	                 <-       отправка сообщения 0
	    сообщение 0 ->
	                 <-       отправка сообщения 2
	    при получении 2 повторно не отправляем
	    */

		Cursor messageCursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"acknowledged", "uid", "sender_id", "receiver_id", "text", "fname", "datetime", "ver", "type_idx", "date1", "date2", "client_id", "agreement_id"}, "((acknowledged&4)<>0)||((acknowledged&6)==0)", null, null);
		int acknowledgedIndex = messageCursor.getColumnIndex("acknowledged");
		int uidIndex = messageCursor.getColumnIndex("uid");
		int senderIdIndex = messageCursor.getColumnIndex("sender_id");
		int receiverIdIndex = messageCursor.getColumnIndex("receiver_id");
		int textIndex = messageCursor.getColumnIndex("text");
		int fnameIndex = messageCursor.getColumnIndex("fname");
		int datetimeIndex = messageCursor.getColumnIndex("datetime");
		int verIndex = messageCursor.getColumnIndex("ver");
		int typeIdxIndex = messageCursor.getColumnIndex("type_idx");
		int clientIdIndex = messageCursor.getColumnIndex("client_id");
		int agreementIdIndex = messageCursor.getColumnIndex("agreement_id");
		int date1Index = messageCursor.getColumnIndex("date1");
		int date2Index = messageCursor.getColumnIndex("date2");

		while (messageCursor.moveToNext()) {
			// acknowledged&4!=0
			// мы-отправители этого сообщения
			// т.к. мы это сообщение у себя не стерли, значит, нужно отправить
			// acknowledged&6==0
			// мы-получатели этого сообщения и сервер 1С не знает, что мы его получили

			if (bExcludeImageMessages) {
				// сообщения с изображениями не отправлять
				if (!messageCursor.getString(fnameIndex).trim().isEmpty()) {
					continue;
				}
			}

			if (bEmpty) {
				//f.Open(m_Dir+_T("/eout/messages.txt"), CFile::modeCreate|CFile::modeWrite);
				bw.write("##SENDMESSAGES##\r\n");
				bw.write(String.format("%d\n", 0));//abs(m_messages_version));
				bw.write(String.format("%d\n", MySingleton.getInstance().Common.m_mtrade_version));
				bEmpty = false;
			}
			bw.write("@@@\r\n");
			// 0 uid
			bw.write(String.format("%s\r\n", messageCursor.getString(uidIndex)));
			// 1 ack
			bw.write(String.format("%d\r\n", messageCursor.getInt(acknowledgedIndex)));
			// 2 sender_id
			bw.write(String.format("%s\r\n", messageCursor.getString(senderIdIndex)));
			// 3 receiver_id
			bw.write(String.format("%s\r\n", messageCursor.getString(receiverIdIndex)));
			// 4 текст/расширенное
			int type_idx = messageCursor.getInt(typeIdxIndex);
			switch (E_MESSAGE_TYPES.fromInt(type_idx)) {
				case E_MESSAGE_TYPE_DEBT:
				case E_MESSAGE_TYPE_SALES: {
					bw.write(String.format("#%d#", type_idx));
					String clientId = messageCursor.getString(clientIdIndex);
					String agreementId = messageCursor.getString(agreementIdIndex);
					String date1 = messageCursor.getString(date1Index);
					String date2 = messageCursor.getString(date2Index);
					bw.write(String.format("%s#", clientId != null ? clientId : ""));
					bw.write(String.format("%s#", agreementId != null ? agreementId : ""));
					bw.write(String.format("%s#", date1 != null ? date1 : ""));
					bw.write(String.format("%s#", date2 != null ? date2 : ""));
					bw.write(String.format("%s\r\n", Common.NormalizeString(messageCursor.getString(textIndex))));
					break;
				}
				default:
					bw.write(String.format("%s\r\n", Common.NormalizeString(messageCursor.getString(textIndex))));
			}
			String fname = messageCursor.getString(fnameIndex);
			// 5 fname
			bw.write(String.format("%s\r\n", Common.NormalizeString(fname)));
			// 6 datetime
			bw.write(String.format("%s\r\n", messageCursor.getString(datetimeIndex)));
			// 7 ver
			bw.write(String.format("%d\r\n", messageCursor.getInt(verIndex)));
		}
		//if (bEmpty==false)
		//	f.Close();
		return !bEmpty;
	}

	static boolean SaveSendMessagesXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, boolean bExcludeImageMessages) throws IOException {
		boolean bEmpty = true;

	    /*
	    Клиент                Сервер
	    отправка 4   ->       обратный ответ 4+1
	    отправка 4   ->       обратный ответ 4+1
	    при получении ответа 4+1 удаляем у себя сообщение

	    Клиент                Сервер
	                 <-       отправка сообщения 0
	    сообщение 0 ->
	                 <-       отправка сообщения 2
	    при получении 2 повторно не отправляем
	    */

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "SendMessages");

		Cursor messageCursor = contentResolver.query(MTradeContentProvider.MESSAGES_CONTENT_URI, new String[]{"acknowledged", "uid", "sender_id", "receiver_id", "text", "fname", "datetime", "ver", "type_idx", "date1", "date2", "client_id", "agreement_id"}, "((acknowledged&4)<>0)||((acknowledged&6)==0)", null, null);
		int acknowledgedIndex = messageCursor.getColumnIndex("acknowledged");
		int uidIndex = messageCursor.getColumnIndex("uid");
		int senderIdIndex = messageCursor.getColumnIndex("sender_id");
		int receiverIdIndex = messageCursor.getColumnIndex("receiver_id");
		int textIndex = messageCursor.getColumnIndex("text");
		int fnameIndex = messageCursor.getColumnIndex("fname");
		int datetimeIndex = messageCursor.getColumnIndex("datetime");
		int verIndex = messageCursor.getColumnIndex("ver");
		int typeIdxIndex = messageCursor.getColumnIndex("type_idx");
		int clientIdIndex = messageCursor.getColumnIndex("client_id");
		int agreementIdIndex = messageCursor.getColumnIndex("agreement_id");
		int date1Index = messageCursor.getColumnIndex("date1");
		int date2Index = messageCursor.getColumnIndex("date2");

		while (messageCursor.moveToNext()) {
			bEmpty = false;
			// acknowledged&4!=0
			// мы-отправители этого сообщения
			// т.к. мы это сообщение у себя не стерли, значит, нужно отправить
			// acknowledged&6==0
			// мы-получатели этого сообщения и сервер 1С не знает, что мы его получили

			if (bExcludeImageMessages) {
				// сообщения с изображениями не отправлять
				if (!messageCursor.getString(fnameIndex).trim().isEmpty()) {
					continue;
				}
			}

            serializer.startTag(null, "SendMessage");

			// 0 uid
			//bw.write(String.format("%s\r\n", messageCursor.getString(uidIndex)));
			serializer.attribute(null, "uid", messageCursor.getString(uidIndex));
			// 1 ack
			//bw.write(String.format("%d\r\n", messageCursor.getInt(acknowledgedIndex)));
			serializer.attribute(null, "ack", String.format("%d", messageCursor.getInt(acknowledgedIndex)));
			// 2 sender_id
			//bw.write(String.format("%s\r\n", messageCursor.getString(senderIdIndex)));
			serializer.attribute(null, "sender_id", messageCursor.getString(senderIdIndex));
			// 3 receiver_id
			//bw.write(String.format("%s\r\n", messageCursor.getString(receiverIdIndex)));
			serializer.attribute(null, "receiver_id", messageCursor.getString(receiverIdIndex));

            String fname = messageCursor.getString(fnameIndex);
            // 5 fname
            //bw.write(String.format("%s\r\n", Common.NormalizeString(fname)));
            serializer.attribute(null, "fname", Common.NormalizeString(fname));
            // 6 datetime
            //bw.write(String.format("%s\r\n", messageCursor.getString(datetimeIndex)));
            serializer.attribute(null, "datetime", messageCursor.getString(datetimeIndex));
            // 7 ver
            //bw.write(String.format("%d\r\n", messageCursor.getInt(verIndex)));
            serializer.attribute(null, "ver", String.format("%d", messageCursor.getInt(verIndex)));

			// 4 текст/расширенное
			int type_idx = messageCursor.getInt(typeIdxIndex);
			serializer.attribute(null, "message_type", String.format("%d", type_idx));

			switch (E_MESSAGE_TYPES.fromInt(type_idx)) {
				case E_MESSAGE_TYPE_DEBT:
				case E_MESSAGE_TYPE_SALES: {
					serializer.startTag(null, "Details");
					String clientId = messageCursor.getString(clientIdIndex);
					String agreementId = messageCursor.getString(agreementIdIndex);
					String date1 = messageCursor.getString(date1Index);
					String date2 = messageCursor.getString(date2Index);
					//bw.write(String.format("%s#",clientId!=null?clientId:""));
					serializer.attribute(null, "clientId", clientId != null ? clientId : "");
					//bw.write(String.format("%s#",agreementId!=null?agreementId:""));
					serializer.attribute(null, "agreementId", agreementId != null ? agreementId : "");
					//bw.write(String.format("%s#",date1!=null?date1:""));
					serializer.attribute(null, "date1", date1 != null ? date1 : "");
					//bw.write(String.format("%s#",date2!=null?date2:""));
					serializer.attribute(null, "date2", date2 != null ? date2 : "");
					//bw.write(String.format("%s\r\n", Common.NormalizeString(messageCursor.getString(textIndex))));
					serializer.attribute(null, "text", Common.NormalizeString(messageCursor.getString(textIndex)));
					serializer.endTag(null, "Details");
					break;
				}
				default:
					serializer.startTag(null, "Details");
					serializer.attribute(null, "text", Common.NormalizeString(messageCursor.getString(textIndex)));
					serializer.endTag(null, "Details");
			}

            serializer.endTag(null, "SendMessage");
		}

		serializer.endTag(null, "SendMessages");

		serializer.endDocument();
		serializer.flush();

		//if (bEmpty==false)
		//	f.Close();
		return !bEmpty;
	}


	public static void fillNomenclatureHierarchy(ContentResolver contentResolver, Resources resources, String parent_id) {
		class Tree {
			String _id;
			String id;
			String parent_id;
			String descr;
			int level;
			int dont_use_in_hierarchy;
		}
		;
		List<String> m_list_groups = new ArrayList<String>();
		List<Tree> m_list2;

		contentResolver.delete(MTradeContentProvider.NOMENCLATURE_HIERARCHY_CONTENT_URI, null, null);

		String[] projection = {
				"_id",
				"id",
				"parent_id",
				"descr",
				"flags"
		};

		m_list2 = new ArrayList<Tree>();

		Cursor cursor = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
//		if (MySingleton.getInstance().Common.PHARAOH) {
//			cursor = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
//		} else {
//			// 24.11.2022 добавлен order_for_sorting, ранее не использовался
//			cursor = contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
//		}
		if (cursor != null) {
			int indexDescr = cursor.getColumnIndex("descr");
			int indexId = cursor.getColumnIndex("id");
			int indexParentId = cursor.getColumnIndex("parent_id");
			int index_Id = cursor.getColumnIndex("_id");
			int index_flags = cursor.getColumnIndex("flags");

			Tree t = new Tree();
			t.descr = resources.getString(R.string.catalogue_all);
			t.id = null;
			t.parent_id = null;
			t.level = 0;
			m_list2.add(t);
			t = new Tree();
			t.descr = resources.getString(R.string.catalogue_node);
			t.id = "     0   ";
			t.parent_id = "";
			t.level = 0;
			t.dont_use_in_hierarchy = 0;
			m_list2.add(t);
			// Сначала просто заполняем список
			while (cursor.moveToNext()) {
				t = new Tree();
				t.descr = cursor.getString(indexDescr);
				t.id = cursor.getString(indexId);
				t.parent_id = cursor.getString(indexParentId);
				// 28.01.2021 багфикс для тандема
				// сначала сделал, а потом понял, что лучше это производить при загрузке номенклатуры, контрагентов и т.д.
				//if (new MyID(t.parent_id).isEmpty())
				//{
				//	t.parent_id=Constants.emptyID;
				//}
				//
				t._id = cursor.getString(index_Id);
				t.level = 0;
				t.dont_use_in_hierarchy = cursor.getInt(index_flags)&0x01;
				m_list2.add(t);
			}
			// А потом сортируем его
			int i;
			// с единицы потому, что в нуле у нас записан null
			for (i = 1; i < m_list2.size(); i++) {
				int offset = i + 1;
				int j;
				for (j = i + 1; j < m_list2.size(); j++) {
					if (m_list2.get(j).parent_id.equals(m_list2.get(i).id)) {
						m_list2.get(j).level = m_list2.get(i).level + 1;
						//Collections.swap(m_list2, offset, j);
						m_list2.add(offset, m_list2.get(j));
						m_list2.remove(j + 1); // +1 так как запись добавилась
						offset++;
					}
				}
			}

			String[] level_ids = new String[9];
			int prevLevel = -1;


			ContentValues values[] = null;

			if (m_list2.size() > 1) {
				values = new ContentValues[m_list2.size() - 1];
			}

			for (i = 0; i < m_list2.size(); i++) {
				t = m_list2.get(i);
				int level = t.level;
				String spaces = "";
				int j;
				for (j = 1; j < level; j++) {
					spaces += " ";
				}
				if (level > 0)
					spaces += "> ";
				m_list_groups.add(spaces + t.descr);
				//Log.i(LOG_TAG, spaces+m_list2.get(i).descr, null);
				// Ноль пропускаем
				if (i > 0) {
					for (j = prevLevel + 1; j < level && j < 9; j++) {
						level_ids[j] = t.id;
					}
					if (level < 9) {
						level_ids[level] = t.id;
					}
					prevLevel = level;

					ContentValues cv = new ContentValues();

					cv.put("id", t.id);
					cv.put("ord_idx", i);
					cv.put("groupDescr", t.descr);
					cv.put("level", t.level);

					cv.put("level0_id", t.level >= 0 ? level_ids[0] : null);
					cv.put("level1_id", t.level >= 1 ? level_ids[1] : null);
					cv.put("level2_id", t.level >= 2 ? level_ids[2] : null);
					cv.put("level3_id", t.level >= 3 ? level_ids[3] : null);
					cv.put("level4_id", t.level >= 4 ? level_ids[4] : null);
					cv.put("level5_id", t.level >= 5 ? level_ids[5] : null);
					cv.put("level6_id", t.level >= 6 ? level_ids[6] : null);
					cv.put("level7_id", t.level >= 7 ? level_ids[7] : null);
					cv.put("level8_id", t.level >= 8 ? level_ids[8] : null);
					cv.put("level8_id", t.level >= 8 ? level_ids[8] : null);

					cv.put("dont_use_in_hierarchy", t.dont_use_in_hierarchy);

					//contentResolver.insert(MTradeContentProvider.NOMENCLATURE_HIERARCHY_CONTENT_URI, cv);
					values[i - 1] = cv;
				}
			}

			if (values != null) {
				contentResolver.bulkInsert(MTradeContentProvider.NOMENCLATURE_HIERARCHY_CONTENT_URI, values);
			}
		}

		//ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_groups);
		//dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//spinner.setAdapter(dataAdapter);
	}

	static boolean SaveSendGps(BufferedWriter bw, ContentResolver contentResolver, MyDatabase myBase, int current_version) throws IOException {
		// нулевую версию заменим на версию выгрузки
		ContentValues cv = new ContentValues();
		cv.put("version", String.valueOf(current_version));
		contentResolver.update(MTradeContentProvider.GPS_COORD_CONTENT_URI, cv, "version=0", null);
		// отправим все ненулевые значения (и как следствие, если будут добавляться новые координаты, в эту выгрузку они не включатся, но их скорее всего не будет)
		Cursor gpsCursor = contentResolver.query(MTradeContentProvider.GPS_COORD_CONTENT_URI, new String[]{"datecoord", "latitude", "longitude", "gpsstate", "gpsaccuracy", "version"}, null, null, null);
		int index_datecoord = gpsCursor.getColumnIndex("datecoord");
		int index_latitude = gpsCursor.getColumnIndex("latitude");
		int index_longitude = gpsCursor.getColumnIndex("longitude");
		int index_gpsstate = gpsCursor.getColumnIndex("gpsstate");
		int index_gpsaccuracy = gpsCursor.getColumnIndex("gpsaccuracy");
		int index_version = gpsCursor.getColumnIndex("version");
		while (gpsCursor.moveToNext()) {
			bw.write(String.format("%s\r\n", gpsCursor.getString(index_datecoord)));
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(gpsCursor.getFloat(index_latitude), "%.6f")));
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(gpsCursor.getFloat(index_longitude), "%.6f")));
			bw.write(String.format("%d\r\n", gpsCursor.getInt(index_version)));
			bw.write(String.format("%d\r\n", gpsCursor.getInt(index_gpsstate)));
			bw.write(String.format("%s\r\n", Common.DoubleToStringFormat(gpsCursor.getFloat(index_gpsaccuracy), "%.3f")));
			bw.write("-1\r\n");
			bw.write("-1\r\n");
		}
		gpsCursor.close();

		return true;
	}

	static boolean SaveSendGpsXML(OutputStream os, ContentResolver contentResolver, MyDatabase myBase, int current_version) throws IOException {

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "GPS");

		// нулевую версию заменим на версию выгрузки
		ContentValues cv = new ContentValues();
		cv.put("version", String.valueOf(current_version));
		contentResolver.update(MTradeContentProvider.GPS_COORD_CONTENT_URI, cv, "version=0", null);
		// отправим все ненулевые значения (и как следствие, если будут добавляться новые координаты, в эту выгрузку они не включатся, но их скорее всего не будет)
		Cursor gpsCursor = contentResolver.query(MTradeContentProvider.GPS_COORD_CONTENT_URI, new String[]{"datecoord", "latitude", "longitude", "gpsstate", "gpsaccuracy", "version"}, null, null, null);
		int index_datecoord = gpsCursor.getColumnIndex("datecoord");
		int index_latitude = gpsCursor.getColumnIndex("latitude");
		int index_longitude = gpsCursor.getColumnIndex("longitude");
		int index_gpsstate = gpsCursor.getColumnIndex("gpsstate");
		int index_gpsaccuracy = gpsCursor.getColumnIndex("gpsaccuracy");
		int index_version = gpsCursor.getColumnIndex("version");
		while (gpsCursor.moveToNext()) {
			serializer.startTag(null, "Entry");
			serializer.attribute(null, "datecoord", gpsCursor.getString(index_datecoord));
			serializer.attribute(null, "latitude", Common.DoubleToStringFormat(gpsCursor.getFloat(index_latitude), "%.6f"));
			serializer.attribute(null, "longitude", Common.DoubleToStringFormat(gpsCursor.getFloat(index_longitude), "%.6f"));
			serializer.attribute(null, "_version", String.format("%d", gpsCursor.getInt(index_version)));
			serializer.attribute(null, "gpsstate", String.format("%d", gpsCursor.getInt(index_gpsstate)));
			serializer.attribute(null, "accuracy", Common.DoubleToStringFormat(gpsCursor.getFloat(index_gpsaccuracy), "%.3f"));
			serializer.endTag(null, "Entry");
		}
		gpsCursor.close();

		serializer.endTag(null, "GPS");

		serializer.endDocument();
		serializer.flush();

		return true;
	}

	// Интервал опроса сделан специально следующим образом
	// задано значение 0 - возвращается -1
	// задано значение 60 - возвращается -60
	// по умолчанию 0
	static int LoadGpsUpdate(ContentResolver contentResolver, MyDatabase myBase, String buf) {
		int updateInterval = 0;

		int nom_idx = 0;

		ContentValues cv = new ContentValues();

		String split[] = buf.split("\n");
		for (String sc0 : split) {
			String sc = sc0.replace("\r", "");
			if (sc.isEmpty())
				continue;
			switch (nom_idx) {
				case 0:
					updateInterval = -Integer.parseInt(sc) - 1;
					break;
				default:
					contentResolver.delete(MTradeContentProvider.GPS_COORD_CONTENT_URI, "version=?", new String[]{sc});
			}
			nom_idx++;
		}

		return updateInterval;
	}


	static boolean SaveSendMiscXML(OutputStream os, String fcm_instanceId) throws IOException
	{
		MySingleton g=MySingleton.getInstance();

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Misc");
		serializer.attribute(null, "DeviceName", Common.getDeviceName());
		if (fcm_instanceId!=null&&!fcm_instanceId.isEmpty())
		{
			serializer.attribute(null, "FCM_InstanceId", fcm_instanceId);
		}
		serializer.endTag(null, "Misc");

		serializer.endDocument();
		serializer.flush();

		return true;
	}


	static void SaveVersions(BufferedWriter bw, MyDatabase myBase) throws IOException
	{
		MySingleton g=MySingleton.getInstance();
	    bw.write(String.format("%d\r\n", g.Common.m_mtrade_version));
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(myBase.m_nomenclature_version)));
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(myBase.m_clients_version)));
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(0))); // устаревшее, версии заявок
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(myBase.m_rests_version)));
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(myBase.m_saldo_version)));
	    bw.write(String.format("%d\r\n", g.Common.ZERO_MINUS_VER(myBase.m_curators_version)));
	    if (g.Common.SNEGOROD)
	    {
	    	if (g.Common.TITAN||g.Common.ISTART||g.Common.FACTORY)
	    	{
	    		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_simple_discounts_version)));
	    	} else
	    	{
	    		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_clients_price_version)));
	    	}
	    } else
	    {
	    	bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_curators_price_version)));
	    }
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_agents_version)));
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_pricetypes_version)));
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_prices_version)));
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_distr_points_version)));
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_agreements_version)));
		bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_stocks_version)));

		//if (Common.PRODLIDER||Common.PRAIT)
		//{
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_organizations_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_sales_loaded_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_saldo_extended_version)));
		//}
		if (g.Common.PRODLIDER||g.Common.TANDEM||g.Common.TITAN||g.Common.FACTORY||g.Common.PRAIT)
		{
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_vicarious_power_version)));
			bw.write(String.format("%d\r\n", myBase.m_sent_count));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_distribs_contracts_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_equipment_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_equipment_rests_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_agreements30_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_prices_agreements30_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_routes_version)));
			bw.write(String.format("%d\r\n", Common.ZERO_MINUS_VER(myBase.m_routes_dates_version)));
		}
	}

	static void SaveVersionsXML(OutputStream os, MyDatabase myBase) throws IOException
	{
		MySingleton g=MySingleton.getInstance();

		XmlSerializer serializer = Xml.newSerializer();

		serializer.setOutput(os, "UTF-8");
		serializer.startDocument(null, Boolean.valueOf(true));
		// Не знаю, что это, но пусть будет
		serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

		serializer.startTag(null, "Versions");
		//serializer.attribute(null, "uid", uid.toString());
		//serializer.attribute(null, "_version", String.format("%d", version));

		TimeZone tz=TimeZone.getDefault();
		serializer.attribute(null, "time_zone_id", tz.getID());
		serializer.attribute(null, "time_zone_descr", tz.getDisplayName(false, TimeZone.SHORT));
		serializer.attribute(null, "time_zone_descr_daylight", tz.getDisplayName(true, TimeZone.SHORT));
		serializer.attribute(null, "time_in_pda", Common.getDateTimeAsString14(null));

		serializer.attribute(null, "mtrade_version", String.format("%d", g.Common.m_mtrade_version));
		serializer.attribute(null, "nomenclature_version", String.format("%d", g.Common.ZERO_MINUS_VER(myBase.m_nomenclature_version)));
		serializer.attribute(null, "clients_version", String.format("%d", g.Common.ZERO_MINUS_VER(myBase.m_clients_version)));
		serializer.attribute(null, "rests_version", String.format("%d", g.Common.ZERO_MINUS_VER(myBase.m_rests_version)));
		serializer.attribute(null, "saldo_version", String.format("%d", g.Common.ZERO_MINUS_VER(myBase.m_saldo_version)));
		serializer.attribute(null, "curators_version", String.format("%d", g.Common.ZERO_MINUS_VER(myBase.m_curators_version)));
		if (g.Common.SNEGOROD)
		{
			if (g.Common.TITAN||g.Common.ISTART||g.Common.FACTORY)
			{
				serializer.attribute(null, "simple_discounts_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_simple_discounts_version)));
			} else
			{
				serializer.attribute(null, "clients_price_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_clients_price_version)));
			}
		} else
		{
			serializer.attribute(null, "curators_price_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_curators_price_version)));
		}
		serializer.attribute(null, "agents_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_agents_version)));
		serializer.attribute(null, "pricetypes_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_pricetypes_version)));
		serializer.attribute(null, "prices_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_prices_version)));
		serializer.attribute(null, "distr_points_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_distr_points_version)));
		serializer.attribute(null, "agreements_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_agreements_version)));
		serializer.attribute(null, "stocks_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_stocks_version)));

		//if (Common.PRODLIDER||Common.PRAIT)
		//{
		serializer.attribute(null, "organizations_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_organizations_version)));
		serializer.attribute(null, "sales_loaded_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_sales_loaded_version)));
		serializer.attribute(null, "saldo_extended_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_saldo_extended_version)));
		//}
		if (g.Common.PRODLIDER||g.Common.TANDEM||g.Common.TITAN||g.Common.FACTORY)
		{
			serializer.attribute(null, "vicarious_power_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_vicarious_power_version)));
			serializer.attribute(null, "sent_count", String.format("%d", myBase.m_sent_count));
			serializer.attribute(null, "distribs_contracts_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_distribs_contracts_version)));
			serializer.attribute(null, "equipment_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_equipment_version)));
			serializer.attribute(null, "equipment_rests_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_equipment_rests_version)));
			serializer.attribute(null, "agreements30_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_agreements30_version)));
			serializer.attribute(null, "prices_agreements30_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_prices_agreements30_version)));
			serializer.attribute(null, "routes_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_routes_version)));
			serializer.attribute(null, "routes_dates_version", String.format("%d", Common.ZERO_MINUS_VER(myBase.m_routes_dates_version)));
		}

		serializer.endTag(null, "Versions");

		serializer.endDocument();
		serializer.flush();

	}


	static void prepareNomenclatureStuff(ContentResolver contentResolver)
	{
		MySingleton g=MySingleton.getInstance();
        // Установим склад и тип цены
        ContentValues cv=new ContentValues();
        cv.clear();
        if (!g.MyDatabase.m_order_editing.stock_id.isEmpty())
        {
        	cv.put("stock_id", g.MyDatabase.m_order_editing.stock_id.toString());
        }
        if (g.Common.TANDEM)
        {
            // Это был первый вариант, организация не записывается в базу, а потому может быть пустой
            //if (!g.MyDatabase.m_order_editing.organization_id.isEmpty())
            //    cv.put("organization_id", g.MyDatabase.m_order_editing.organization_id.toString());
            // Поэтому считаем договор и организацию из него
            String organization_id=Constants.emptyID;
            MyID agreement_id=g.MyDatabase.m_order_editing.agreement_id;
            if (!agreement_id.isEmpty()) {
                Cursor agreementCursor = contentResolver.query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"organization_id"}, "id=?", new String[]{agreement_id.m_id}, null);
                if (agreementCursor.moveToNext()) {
                    int organizationIdIndex = agreementCursor.getColumnIndex("organization_id");
                    organization_id = agreementCursor.getString(organizationIdIndex);
                }
                agreementCursor.close();
            }
            cv.put("organization_id", organization_id);
        }
        if (g.Common.MEGA)
        {
	        if (!g.MyDatabase.m_order_editing.curator_id.isEmpty())
	        {
	        	cv.put("curator_id", g.MyDatabase.m_order_editing.curator_id.toString());
	        }
        } else
        if (g.Common.PHARAOH)
        {
    	    cv.put("priceProcent", g.MyDatabase.m_order_editing.stuff_discount_procent);
        } else
        {
            if (g.Common.TITAN||g.Common.ISTART||g.Common.FACTORY)
            {
                double priceProcent=0.0;
                // TODO
                //Cursor agreementsCursor=contentResolver.query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"sale_id"}, null, null, null);
                // С 26.02.2018
                Cursor agreementsCursor=contentResolver.query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"sale_id"}, "id=?", new String[]{g.MyDatabase.m_order_editing.agreement_id.toString()}, null);
                if (agreementsCursor.moveToNext())
                {
                    int saleIdIndex = agreementsCursor.getColumnIndex("sale_id");
                    String sale_id = agreementsCursor.getString(saleIdIndex);
                    Cursor discountCursor=contentResolver.query(MTradeContentProvider.SIMPLE_DISCOUNTS_CONTENT_URI, new String[]{"priceProcent"}, "id=?", new String[]{sale_id}, null);
                    if (discountCursor.moveToNext())
                    {
                        int priceProcentIndex = discountCursor.getColumnIndex("priceProcent");
                        priceProcent = discountCursor.getDouble(priceProcentIndex);
                    }
                    discountCursor.close();
                }
                agreementsCursor.close();
                cv.put("priceProcent", priceProcent);
            }
            // Клиент нужен для истории продаж по документам в базе, где вывоятся продажи синим
            // (заполняется в RESTS_SALES_STUFF_CONTENT_URI)
	        if (!g.MyDatabase.m_order_editing.client_id.isEmpty())
	        {
	        	cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());
	        }
	        if (g.Common.isDataFormatWithTradePoints())
            {
                cv.put("distr_point_id", g.MyDatabase.m_order_editing.distr_point_id.toString());
            }

        }
        //getContentResolver().insert(MTradeContentProvider.RESTSV_CONTENT_URI,  cv);
        contentResolver.insert(MTradeContentProvider.RESTS_SALES_STUFF_CONTENT_URI,  cv);
        if (g.Common.MEGA)
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_MEGA_CONTENT_URI,  cv);
        else if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART||g.Common.FACTORY)
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_SIMPLE_CONTENT_URI,  cv);
        else
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_OTHER_CONTENT_URI,  cv);

        // Тип цены обязателен, пусть даже пустой
        cv.clear();
       	cv.put("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
        if (g.Common.MEGA)
        	contentResolver.insert(MTradeContentProvider.PRICESV_MEGA_CONTENT_URI,  cv);
        else {
            if (g.Common.FACTORY)
            {
                // Для цен по соглашениям
                cv.put("agreement30_id", g.MyDatabase.m_order_editing.agreement30_id.toString());
            }
            contentResolver.insert(MTradeContentProvider.PRICESV_OTHER_CONTENT_URI, cv);
        }
	}

	// заляп
	static void prepareNomenclatureStuffForRefund(ContentResolver contentResolver)
	{
		MySingleton g=MySingleton.getInstance();
        // Установим склад и тип цены
        ContentValues cv=new ContentValues();
        cv.clear();
        if (!g.MyDatabase.m_refund_editing.stock_id.isEmpty())
        {
        	cv.put("stock_id", g.MyDatabase.m_refund_editing.stock_id.toString());
        }
        // 25.01.2021 не знаю, насколько актуально
		if (g.Common.TANDEM)
		{
			// Это был первый вариант, организация не записывается в базу, а потому может быть пустой
			//if (!g.MyDatabase.m_order_editing.organization_id.isEmpty())
			//    cv.put("organization_id", g.MyDatabase.m_order_editing.organization_id.toString());
			// Поэтому считаем договор и организацию из него
			String organization_id=Constants.emptyID;
			MyID agreement_id=g.MyDatabase.m_refund_editing.agreement_id;
			if (!agreement_id.isEmpty()) {
				Cursor agreementCursor = contentResolver.query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"organization_id"}, "id=?", new String[]{agreement_id.m_id}, null);
				if (agreementCursor.moveToNext()) {
					int organizationIdIndex = agreementCursor.getColumnIndex("organization_id");
					organization_id = agreementCursor.getString(organizationIdIndex);
				}
				agreementCursor.close();
			}
			cv.put("organization_id", organization_id);
		}
		//
        if (g.Common.MEGA)
        {
	        if (!g.MyDatabase.m_refund_editing.curator_id.isEmpty())
	        {
	        	cv.put("curator_id", g.MyDatabase.m_refund_editing.curator_id.toString());
	        }
        } else
        if (g.Common.PHARAOH)
        {
    	    cv.put("priceProcent", 0.0);
        } else
        if (g.Common.TITAN||g.Common.ISTART||g.Common.FACTORY)
        {
    	    cv.put("priceProcent", 0.0);
        } else
        {
	        if (!g.MyDatabase.m_refund_editing.client_id.isEmpty())
	        {
	        	cv.put("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
	        }
        }
        contentResolver.insert(MTradeContentProvider.RESTS_SALES_STUFF_CONTENT_URI,  cv);
        if (g.Common.MEGA)
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_MEGA_CONTENT_URI,  cv);
        else if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART||g.Common.FACTORY)
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_SIMPLE_CONTENT_URI,  cv);
        else
        	contentResolver.insert(MTradeContentProvider.DISCOUNTS_STUFF_OTHER_CONTENT_URI,  cv);
        // Тип цены обязателен, пусть даже пустой
        cv.clear();
       	cv.put("price_type_id", Constants.emptyID);
        if (g.Common.MEGA)
        	contentResolver.insert(MTradeContentProvider.PRICESV_MEGA_CONTENT_URI,  cv);
        else
        	contentResolver.insert(MTradeContentProvider.PRICESV_OTHER_CONTENT_URI,  cv);

	}

	static void prepareContractsStuff(ContentResolver contentResolver)
	{
		// TODO, может ничего не нужно
	}

	static public String fillSelectionArgsString(List<String> ids, String []arrayToFill, int start_index)
    {
        if (ids.size()==0)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("(?");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(",?");
            arrayToFill[start_index+i]=ids.get(i);
        }
        sb.append(")");
        return sb.toString();
    }

    static public String fillSelectionArgsLong(List<Long> ids, String []arrayToFill, int start_index)
    {
        if (ids.size()==0)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append("(?");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(",?");
            arrayToFill[start_index+i]=Long.toString(ids.get(i));
        }
        sb.append(")");
        return sb.toString();
    }


	static public HashMap<String, String> getClientsDescr(ContentResolver contentResolver, List<String>ids)
	{
        HashMap<String, String> result=new HashMap();
        if (ids.size()>0) {
            String[] selectionArgs = new String[ids.size()];
            String selection = "id in " + fillSelectionArgsString(ids, selectionArgs, 0);
            Cursor cursor = contentResolver.query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"id", "descr"}, selection, selectionArgs, "descr");
            while (cursor.moveToNext())
            {
                result.put(cursor.getString(0), cursor.getString(1));
            }
            cursor.close();
        }
        return result;
	}

	static public class ClientDescrWithSaldo
	{
		public String descr;
		public double saldo;
		public double saldo_past;

		public ClientDescrWithSaldo(String descr, double saldo, double saldo_past)
		{
			this.descr=descr;
			this.saldo=saldo;
			this.saldo_past=saldo_past;
		}

		static public HashMap<String, ClientDescrWithSaldo> getClientsDescrWithSaldo(ContentResolver contentResolver, List<String>ids)
		{
			HashMap<String, ClientDescrWithSaldo> result=new HashMap();
			if (ids.size()>0) {
				String[] selectionArgs = new String[ids.size()];
				String selection = "id in " + fillSelectionArgsString(ids, selectionArgs, 0);
				Cursor cursor = contentResolver.query(MTradeContentProvider.CLIENTS_WITH_SALDO_CONTENT_URI, new String[]{"id", "descr", "saldo", "saldo_past"}, selection, selectionArgs, "descr");
				while (cursor.moveToNext())
				{
					result.put(cursor.getString(0), new ClientDescrWithSaldo(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3)));
				}
				cursor.close();
			}
			return result;
		}
	}

	/*
	static public HashMap<String, String> getClientsDescrWithSaldo(ContentResolver contentResolver, List<String>ids)
	{
		HashMap<String, String> result=new HashMap();
		if (ids.size()>0) {
			String[] selectionArgs = new String[ids.size()];
			String selection = "id in " + fillSelectionArgsString(ids, selectionArgs, 0);
			Cursor cursor = contentResolver.query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"id", "descr"}, selection, selectionArgs, "descr");
			while (cursor.moveToNext())
			{
				result.put(cursor.getString(0), cursor.getString(1));
			}
			cursor.close();
		}
		return result;
	}
	*/


	static public HashMap<String, String> getClientsDescrOfDistrPoint(ContentResolver contentResolver, String distr_point_id)
	{
		// Зная торговую точку, найдем всех контрагентов, у которых она есть
		ArrayList<String> clients_ids=new ArrayList();
		Cursor distrPointsCursor=contentResolver.query(MTradeContentProvider.DISTR_POINTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{distr_point_id}, "_id desc");
		while (distrPointsCursor.moveToNext())
		{
			clients_ids.add(distrPointsCursor.getString(0));
		}
		distrPointsCursor.close();
		HashMap<String, String> clientsDescr=TextDatabase.getClientsDescr(contentResolver, clients_ids);
		// TODO
		//clientsDescr.put("1", "Test1");
		//clientsDescr.put("2", "Test2");
		return clientsDescr;
	}

	static public long getOrderIdByUID(ContentResolver contentResolver, String uid) {
		long result=-1L;
		Cursor cursor = contentResolver.query(MTradeContentProvider.ORDERS_CONTENT_URI, new String[]{"_id"}, "uid=? and editing_backup=0", new String[]{uid}, "datedoc desc");
		if (cursor.moveToNext())
		{
			result=cursor.getLong(0);
		}
		cursor.close();
		return result;
	}

    static public long getDocumentIdByUID(ContentResolver contentResolver, Uri contentUri, String uid) {
        long result=-1L;
        Cursor cursor = contentResolver.query(contentUri, new String[]{"_id"}, "uid=?", new String[]{uid}, "datedoc desc");
        if (cursor.moveToNext())
        {
            result=cursor.getLong(0);
        }
        cursor.close();
        return result;
    }

    static public HashMap<String, Integer> getDocsColors(ContentResolver contentResolver, List<String> documentsUidList) {
		HashMap<String, Integer> docsColors = new HashMap();
		if (documentsUidList.size() > 0) {
			String[] selectionArgs = new String[documentsUidList.size()];
			String selection = "uid in " + TextDatabase.fillSelectionArgsString(documentsUidList, selectionArgs, 0);
			Cursor cursor = contentResolver.query(MTradeContentProvider.JOURNAL_CONTENT_URI, new String[]{"uid", "color"}, selection, selectionArgs, "journal._id desc");
			while (cursor.moveToNext()) {
				docsColors.put(cursor.getString(0), cursor.getInt(1));
			}
			cursor.close();
		}
		return docsColors;
	}

    // TODO На самом деле можно сейчас из документов запросом выбирать цвета, а не из журнала
    static public int getDocColor(ContentResolver contentResolver, String uid) {
        int color=0;
        Cursor cursor = contentResolver.query(MTradeContentProvider.JOURNAL_CONTENT_URI, new String[]{"color"}, "uid=?", new String[]{uid}, "journal._id desc");
        while (cursor.moveToNext()) {
            color=cursor.getInt(0);
        }
        cursor.close();
        return color;
    }

	static public int getDocPaymentColor(ContentResolver contentResolver, String uid) {
		int color=0;
		Cursor cursor = contentResolver.query(MTradeContentProvider.CASH_PAYMENTS_CONTENT_URI, new String[]{"color"}, "uid=?", new String[]{uid}, "_id desc");
		while (cursor.moveToNext()) {
			color=cursor.getInt(0);
		}
		cursor.close();
		return color;
	}



}

