package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.provider.BaseColumns;
import android.widget.BaseExpandableListAdapter;


enum E_BW implements Parcelable
{
    E_BW_UPR,//0
    E_BW_BOTH,//1
    E_BW_TARA,//2
    E_BW_BPZ;//3

    private static Map<Integer, E_BW> ss = new TreeMap<Integer,E_BW>();
    private static final int START_VALUE = 0;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_BW fromInt(int i) {
        return ss.get(i);
    }

    public int value() {
    	return value;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
	}
	
	public static final Creator<E_BW> CREATOR = new Creator<E_BW>() {
        @Override
        public E_BW createFromParcel(final Parcel source) {
            //return E_BW.values()[source.readInt()];
        	return E_BW.fromInt(source.readInt());
        }

        @Override
        public E_BW[] newArray(final int size) {
            return new E_BW[size];
        }
    };	
    
    
};

enum E_TRADE_TYPE implements Parcelable
{
    E_TRADE_TYPE_NAL,//=1,
    E_TRADE_TYPE_BEZNAL,//=2,
    E_TRADE_TYPE_CONSIGNATION,//=3,
    E_TRADE_TYPE_ROZN;//=7
    
    private static Map<Integer, E_TRADE_TYPE> ss = new TreeMap<Integer,E_TRADE_TYPE>();
    private static final int START_VALUE = 1;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_TRADE_TYPE fromInt(int i) {
        return ss.get(i);
    }

    public int value() {
    	return value;
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
	}
	
	public static final Creator<E_TRADE_TYPE> CREATOR = new Creator<E_TRADE_TYPE>() {
        @Override
        public E_TRADE_TYPE createFromParcel(final Parcel source) {
            //return E_BW.values()[source.readInt()];
        	return E_TRADE_TYPE.fromInt(source.readInt());
        }

        @Override
        public E_TRADE_TYPE[] newArray(final int size) {
            return new E_TRADE_TYPE[size];
        }
    };
    
};

enum E_MESSAGE_TYPES implements Parcelable
{
	E_MESSAGE_TYPE_MESSAGE, // 0
	E_MESSAGE_TYPE_PHOTO, // 1 
	E_MESSAGE_TYPE_DEBT, // 2
	E_MESSAGE_TYPE_SALES; // 3
	
    private static Map<Integer, E_MESSAGE_TYPES> ss = new TreeMap<Integer,E_MESSAGE_TYPES>();
    private static final int START_VALUE = 0;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_MESSAGE_TYPES fromInt(int i) {
    	if (i>=ss.size())
    		return ss.get(0);
        return ss.get(i);
    }

    public int value() {
    	return value;
    }
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
	}
	
	public static final Creator<E_MESSAGE_TYPES> CREATOR = new Creator<E_MESSAGE_TYPES>() {
        @Override
        public E_MESSAGE_TYPES createFromParcel(final Parcel source) {
            //return E_BW.values()[source.readInt()];
        	return E_MESSAGE_TYPES.fromInt(source.readInt());
        }

        @Override
        public E_MESSAGE_TYPES[] newArray(final int size) {
            return new E_MESSAGE_TYPES[size];
        }
    };
	
};


public class MyDatabase
{
	UUID m_seance;
	boolean m_seance_closed;
	
	public int m_nomenclature_version;
	public int m_clients_version;
	public int m_agreements_version;
	public int m_pricetypes_version;
	public int m_stocks_version;
	public int m_rests_version;
	public int m_saldo_version;
	public int m_saldo_extended_version;
	public int m_prices_version;
	public int m_clients_price_version;
	public int m_curators_price_version;
	public int m_simple_discounts_version;
	public int m_agents_version;
	public int m_curators_version;
	public int m_distr_points_version;
	public int m_organizations_version;
	public int m_sales_loaded_version;
	public Map<UUID, Integer> m_sales_loaded_versions;
	public int m_places_version;
	public int m_occupied_places_version;
	public int m_vicarious_power_version;
	public int m_distribs_contracts_version;
	public int m_equipment_version;
	public int m_equipment_rests_version;
	public int m_agreements30_version;
	public int m_prices_agreements30_version;

	public int m_routes_version;
	public int m_routes_dates_version;
	
	public int m_sent_count; // При каждой отправке на сервер счетчик увеличивается
	
	//public ArrayList<UUID> m_empty_orders;
	public Map<UUID, Integer> m_empty_orders; 
	public Map<UUID, Integer> m_empty_payments;
	public Map<UUID, Integer> m_empty_refunds;
	public Map<UUID, Integer> m_empty_distribs;
	public Map<UUID, Integer> m_empty_visits;
	public Map<UUID, Double> m_acks_shipping_sums;
	
	// Заказ
	public OrderRecord m_order_editing;
	public long m_order_editing_id; // _id
	// с 07.02.2018 логика такая:
	// когда открывают существующий документ
	// m_order_editing_id=id документа, m_order_new_editing_id=0
	// если что-то начинают менять, то документ записывается (m_order_new_editing_id)
	// и дальнейшие изменения записываются
	// когда создается новый документ, все то же самое, только m_order_editing_id=0 изначально
	public long m_order_new_editing_id; // _id
	//
	public boolean m_order_editing_created;
	public boolean m_order_editing_modified;
	public BaseExpandableListAdapter m_orderLinesAdapter;
	
	// Платеж
	public CashPaymentRecord m_payment_editing;
	public long m_payment_editing_id; // _id
	public boolean m_payment_editing_modified;
	
	// Возврат
	public RefundRecord m_refund_editing;
	public long m_refund_editing_id; // _id
	public boolean m_refund_editing_created;
	public boolean m_refund_editing_modified;
	public BaseExpandableListAdapter m_refundLinesAdapter;
	
	// Дистрибьюция
	public DistribsRecord m_distribs_editing;
	public long m_distribs_editing_id; // _id
	public boolean m_distribs_editing_created;
	public boolean m_distribs_editing_modified;
	public DistribsLinesAdapter m_distribsLinesAdapter;
	
	// Сообщение
	public MessageRecord m_message_editing;
	public long m_message_editing_id; // _id
	public boolean m_message_editing_modified;
	
	public MyDatabase()
	{
		m_order_editing=new OrderRecord();
		m_payment_editing=new CashPaymentRecord();
		m_refund_editing=new RefundRecord();
		m_distribs_editing=new DistribsRecord();
		m_message_editing=new MessageRecord();
		m_seance=UUID.randomUUID();
		m_seance_closed=false;
		m_orderLinesAdapter=null;
		m_refundLinesAdapter=null;
		m_distribsLinesAdapter=null;
		m_empty_orders=new HashMap<UUID, Integer>();
		m_empty_payments=new HashMap<UUID, Integer>();
		m_empty_refunds=new HashMap<UUID, Integer>();
		m_empty_visits=new HashMap<UUID, Integer>();
		m_empty_distribs=new HashMap<UUID, Integer>();
		m_acks_shipping_sums=new HashMap<UUID, Double>();
	}
	
	public static String GetPriceName(E_PRICE_TYPE pt)
	{
	    switch (pt)
	    {
	    case E_PRICE_TYPE_ROZN:
	        return "Rozn";//"Розничные";
	    case E_PRICE_TYPE_M_OPT:
	        return "M_Opt";//"Мелкооптовые";
	    case E_PRICE_TYPE_OPT:
			return "Opt";//"Оптовые";
	    case E_PRICE_TYPE_INCOM:
	        return "Incom";//"Закупочные";
	    }
	    return "Unknown";//"Неизв.";
	}
	
	public static String GetOrderStateDescr(E_ORDER_STATE state, Context context)
	{
	    switch (state)
	    {
	    case E_ORDER_STATE_CREATED:
	    	if (context==null)
	    		return "Создана";
	    		//return "Created";
	    	return context.getResources().getString(R.string.order_state_created);
	    case E_ORDER_STATE_SENT:
	    	if (context==null)
	    		//return "Sent";
	    		return "Отправлена";
	        return context.getResources().getString(R.string.order_state_sent);
	    case E_ORDER_STATE_QUERY_CANCEL:
	    	if (context==null)
	    		//return "Query cancel";
	    		return "Запрос отм.";
	    	return context.getResources().getString(R.string.order_state_query_cancel);
	    case E_ORDER_STATE_LOADED:
	    	if (context==null)
	    		//return "Loaded";
	    		return "Загружена";
	    	return context.getResources().getString(R.string.order_state_loaded);
	    case E_ORDER_STATE_ACKNOWLEDGED:
	    	if (context==null)
	    		//return "Acknowledged";
	    		return "Принята";
	    	return context.getResources().getString(R.string.order_state_acknowledged);
	    case E_ORDER_STATE_COMPLETED:
	    	if (context==null)
	    		//return "Completed";
	    		return "Выполнена";
	    	return context.getResources().getString(R.string.order_state_completed);
	    case E_ORDER_STATE_CANCELED:
	    	if (context==null)
	    		//return "Canceled";
	    		return "Отменена";
	    	return context.getResources().getString(R.string.order_state_canceled);
	    case E_ORDER_STATE_WAITING_AGREEMENT:
	    	if (context==null)
	    		//return "Waiting agreement";
	    		return "Согласование";
	    	return context.getResources().getString(R.string.order_state_waiting_agreement);
	    case E_ORDER_STATE_BACKUP_NOT_SAVED:
	    	if (context==null)
	    		//return "Waiting agreement";
	    		return "Восстановлен(сбой)";
	    	return context.getResources().getString(R.string.order_state_backup_not_saved);
	    default:
	    	if (context==null)
	    		//return "Unknown";
	    		return "Неизв.";
	    	return context.getResources().getString(R.string.order_state_unknown);
	    }
	}
	
	public static String GetRefundStateDescr(E_REFUND_STATE state, Context context)
	{
	    switch (state)
	    {
	    case E_REFUND_STATE_CREATED:
	    	if (context==null)
	    		return "Создана";
	    		//return "Created";
	    	return context.getResources().getString(R.string.refund_state_created);
	    case E_REFUND_STATE_SENT:
	    	if (context==null)
	    		//return "Sent";
	    		return "Отправлена";
	        return context.getResources().getString(R.string.refund_state_sent);
	    case E_REFUND_STATE_QUERY_CANCEL:
	    	if (context==null)
	    		//return "Query cancel";
	    		return "Запрос отм.";
	    	return context.getResources().getString(R.string.refund_state_query_cancel);
	    case E_REFUND_STATE_LOADED:
	    	if (context==null)
	    		//return "Loaded";
	    		return "Загружена";
	    	return context.getResources().getString(R.string.refund_state_loaded);
	    case E_REFUND_STATE_ACKNOWLEDGED:
	    	if (context==null)
	    		//return "Acknowledged";
	    		return "Принята";
	    	return context.getResources().getString(R.string.refund_state_acknowledged);
	    case E_REFUND_STATE_COMPLETED:
	    	if (context==null)
	    		//return "Completed";
	    		return "Выполнена";
	    	return context.getResources().getString(R.string.refund_state_completed);
	    case E_REFUND_STATE_CANCELED:
	    	if (context==null)
	    		//return "Canceled";
	    		return "Отменена";
	    	return context.getResources().getString(R.string.refund_state_canceled);
	    case E_REFUND_STATE_WAITING_AGREEMENT:
	    	if (context==null)
	    		//return "Waiting agreement";
	    		return "Согласование";
	    	return context.getResources().getString(R.string.refund_state_waiting_agreement);
	    case E_REFUND_STATE_BACKUP_NOT_SAVED:
	    	if (context==null)
	    		//return "Waiting agreement";
	    		return "Восстановлен(сбой)";
	    	return context.getResources().getString(R.string.refund_state_backup_not_saved);
	    default:
	    	if (context==null)
	    		//return "Unknown";
	    		return "Неизв.";
	    	return context.getResources().getString(R.string.refund_state_unknown);
	    }
	}
	
	public static String GetPaymentStateDescr(E_PAYMENT_STATE state, Context context)
	{
	    switch (state)
	    {
	    case E_PAYMENT_STATE_CREATED:
	    	if (context==null)
	    		return "Создан";
	    		//return "Created";
	    	return context.getResources().getString(R.string.payment_state_created);
	    case E_PAYMENT_STATE_SENT:
	    	if (context==null)
	    		//return "Sent";
	    		return "Отправлен";
	        return context.getResources().getString(R.string.payment_state_sent);
	    case E_PAYMENT_STATE_QUERY_CANCEL:
	    	if (context==null)
	    		//return "Query cancel";
	    		return "Запрос отм.";
	    	return context.getResources().getString(R.string.payment_state_query_cancel);
	    case E_PAYMENT_STATE_LOADED:
	    	if (context==null)
	    		//return "Loaded";
	    		return "Загружен";
	    	return context.getResources().getString(R.string.payment_state_loaded);
	    case E_PAYMENT_STATE_ACKNOWLEDGED:
	    	if (context==null)
	    		//return "Acknowledged";
	    		return "Принят";
	    	return context.getResources().getString(R.string.payment_state_acknowledged);
	    case E_PAYMENT_STATE_COMPLETED:
	    	if (context==null)
	    		//return "Completed";
	    		return "Выполнен";
	    	return context.getResources().getString(R.string.payment_state_completed);
	    case E_PAYMENT_STATE_CANCELED:
	    	if (context==null)
	    		//return "Canceled";
	    		return "Отменен";
	    	return context.getResources().getString(R.string.payment_state_canceled);
	    default:
	    	if (context==null)
	    		//return "Unknown";
	    		return "Неизв.";
	    	return context.getResources().getString(R.string.payment_state_unknown);
	    }
	}
	
	public static String GetDistribsStateDescr(E_DISTRIBS_STATE state, Context context)
	{
	    switch (state)
	    {
	    case E_DISTRIBS_STATE_CREATED:
	    	if (context==null)
	    		return "Создана";
	    		//return "Created";
	    	return context.getResources().getString(R.string.order_state_created);
	    case E_DISTRIBS_STATE_SENT:
	    	if (context==null)
	    		//return "Sent";
	    		return "Отправлена";
	        return context.getResources().getString(R.string.order_state_sent);
	    case E_DISTRIBS_STATE_QUERY_CANCEL:
	    	if (context==null)
	    		//return "Query cancel";
	    		return "Запрос отм.";
	    	return context.getResources().getString(R.string.order_state_query_cancel);
	    case E_DISTRIBS_STATE_LOADED:
	    	if (context==null)
	    		//return "Loaded";
	    		return "Загружена";
	    	return context.getResources().getString(R.string.order_state_loaded);
	    case E_DISTRIBS_STATE_ACKNOWLEDGED:
	    	if (context==null)
	    		//return "Acknowledged";
	    		return "Принята";
	    	return context.getResources().getString(R.string.order_state_acknowledged);
	    case E_DISTRIBS_STATE_COMPLETED:
	    	if (context==null)
	    		//return "Completed";
	    		return "Выполнена";
	    	return context.getResources().getString(R.string.order_state_completed);
	    case E_DISTRIBS_STATE_CANCELED:
	    	if (context==null)
	    		//return "Canceled";
	    		return "Отменена";
	    	return context.getResources().getString(R.string.order_state_canceled);
	    default:
	    	if (context==null)
	    		//return "Unknown";
	    		return "Неизв.";
	    	return context.getResources().getString(R.string.order_state_unknown);
	    }
	}
	
	public static String GetMessageTypeDescr(E_MESSAGE_TYPES mt)
	{
		switch (mt)
		{
		case E_MESSAGE_TYPE_PHOTO:
			return "Фотография";
		case E_MESSAGE_TYPE_MESSAGE:
			return "Сообщение";
		case E_MESSAGE_TYPE_DEBT:
			return "Дебиторская задолженность";
		case E_MESSAGE_TYPE_SALES:
			return "Продажи";
		}
		return "Неизв.";
	}
	
	////////////////////////////////////////
	// MyID
	////////////////////////////////////////
	static class MyID implements Cloneable, Parcelable
	{
		public String m_id; // 36+1
		
		public MyID clone(){
			MyID obj;
			try {
				obj = (MyID)super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				obj=new MyID();
			}
	        obj.m_id = new String(m_id);
	        return obj;
	    }		
		
		public MyID()
		{
		    //m_id="     0   ";
			m_id=Constants.emptyID; // ID9
		}
		
		/*
		public MyID(int i)
		{
		    if (i==-1)
		        // вдруг минус не скопируется когда-нибудь, поэтому сделаем пустую строку
		        m_id="";
		    else
		        m_id="     0   ";
		}
		*/
	    public MyID(final String str)
	    {
	    	if (str==null)
	    	{
	    		//m_id="     0   ";
				m_id=Constants.emptyID; // ID9
	    	} else
	    	{
	    		m_id=new String(str);
	    	}
	    }
	    
	    public MyID(Parcel source)
	    {
	    	m_id=source.readString();
	    }
	    /*
	    MyID(const MyID other)
	    {
	    	this=other;
	    }
	    
	    MyID &operator = (const MyID &other);
	    bool operator < (const MyID &other) const;
	    bool operator == (const MyID &other) const;
	    bool operator != (const MyID &other) const;
	    QString toString();
	    */
	    public String toString()
	    {
	    	return m_id;
	    }

		public String toStringOrEmpty(String emptyStringSample)
		{
			if (isEmpty())
				return emptyStringSample;
			return m_id;
		}

		// 26.06.2019 просто проверка, пустая ли строка
		public static boolean isEmptyString(String s)
		{
			if (s==null)
				return true;
			// 28.01.2021 убираем "|" из-за тандема, который использует это, делая ID длиной 36+1+36
			return s.replace('0', ' ').replace('-', ' ').replace('|', ' ').replace(" ", "").isEmpty();
		}
	    
	    public boolean isEmpty()
	    {
	    	//String s=m_id.replace('0', ' ').replace('-', ' ').replace(" ", "");
	    	//return s.isEmpty();
			return isEmptyString(m_id);
	    }
	    
		@Override
		public boolean equals(Object o) {
			return m_id.equals(((MyID)o).m_id);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(m_id);
		}
		
		public static final Creator<MyID> CREATOR = new Creator<MyID>() {
	        @Override
	        public MyID createFromParcel(final Parcel source) {
	        	return new MyID(source);
	        }

	        @Override
	        public MyID[] newArray(final int size) {
	            return new MyID[size];
	        }
	    };	
		
	    
	    
	};
	
	/*
	MyID::MyID(wchar_t *str)
	{
	    //strcpy(m_id, QString::fromStdWString(str).toLocal8Bit().data());
	    MY_STRNCPY(m_id, QString::fromStdWString(str).toLocal8Bit().data());
	    while (strlen(m_id)<9)
	        strcat(m_id, " ");
	}

	MyID::MyID(const QString &s)
	{
	    MY_STRNCPY(m_id, s.toLocal8Bit().data());
	    while (strlen(m_id)<9)
	        strcat(m_id, " ");
	}

	MyID::MyID(const MyID &other)
	{
	    strcpy(this->m_id, other.m_id);
	}

	MyID &MyID::operator = (const MyID &other)
	{
	    strcpy(this->m_id, other.m_id);
	    return *this;
	}

	bool MyID::operator < (const MyID &other) const
	{
	    if (strcmp(this->m_id, other.m_id)<0)
	        return true;
	    return false;
	}

	bool MyID::operator == (const MyID &other) const
	{
	    if (strcmp(this->m_id, other.m_id)==0)
	        return true;
	    return false;
	}

	bool MyID::operator != (const MyID &other) const
	{
	    if (strcmp(this->m_id, other.m_id)!=0)
	        return true;
	    return false;
	}


	QString MyID::toString()
	{
	    return QString::fromStdString(m_id);
	}
	
	*/
	
	
	static public class NomenclatureRecord
	{
		public MyID id;
	    public int IsFolder;
	    public MyID parent_id;
	    public String code;
	    public String descr;
	    public String descrFull;
	    public String quant_1;
	    public String quant_2;
	    public MyID edizm_1_id;
	    public MyID edizm_2_id;

	    public double quant_k_1;
	    public double quant_k_2;
	    public double opt_price;
	    public double m_opt_price;
	    public double rozn_price;
	    public double incom_price;
	    public int IsInPrice;

	    public int flagWithoutDiscont;
	    public double weight_k_1;
	    public double weight_k_2;
	    
	    public double min_quantity;
	    public double multiplicity;
	    public double required_sales;
	    public int flags;
	    public int order_for_sorting;
	    
	    public int group_of_analogs;
	    public int nomenclature_color;
		
		public NomenclatureRecord()
		{
			parent_id=new MyID();
			id=new MyID();
			edizm_1_id=new MyID();
			edizm_2_id=new MyID();
		}
		
		public void clear()
		{
    		id=new MyID();
    	    IsFolder=0;
    	    parent_id=new MyID();
    	    code="";
    	    descr="";
    	    descrFull="";
    	    quant_1="";
    	    quant_2="";
    	    edizm_1_id=new MyID();
    	    edizm_2_id=new MyID();
    	    quant_k_1=1.0;
    	    quant_k_2=1.0;
    	    opt_price=0.0;
    	    m_opt_price=0.0;
    	    rozn_price=0.0;
    	    incom_price=0.0;
    	    IsInPrice=1;
    	    flagWithoutDiscont=0;
    	    weight_k_1=0.0;
    	    weight_k_2=0.0;
    	    min_quantity=0.0;
    	    multiplicity=0.0;
    	    required_sales=0.0;
    	    flags=0;
    	    order_for_sorting=0;
    	    group_of_analogs=0;
    	    nomenclature_color=0;
		}
	}
	
	static public class QuantityRecord
	{
	    public MyID stock_id;
	    public MyID nomenclature_id;
	    public double quantity;
	    public double quantity_reserve;
		public MyID organization_id;
		
	    public QuantityRecord()
	    {
	    	stock_id=new MyID();
	    	nomenclature_id=new MyID();
	    	quantity=0.0;
	    	quantity_reserve=0.0;
			organization_id=new MyID();
	    }
	}
	
	static public class EquipmentRestsRecord
	{
	    public MyID client_id;
	    public MyID agreement_id;
	    public MyID nomenclature_id;
	    public MyID distr_point_id;
	    public MyID doc_id;
	    public String doc_descr;
	    public String date;
	    public String datepast;
	    public double quantity;
	    public double sum;
	    int flags;
		
	    public EquipmentRestsRecord()
	    {
	    	client_id=new MyID();
	    	agreement_id=new MyID();
	    	nomenclature_id=new MyID();
	    	distr_point_id=new MyID();
		    doc_id=new MyID();
		    doc_descr="";
		    date="";
		    datepast="";
	    	quantity=0.0;
	    	sum=0.0;
	    	flags=0;
	    }
	}
	
	
	static public class ClientRecord
	{
		public MyID id;
	    public int IsFolder;
	    public MyID parent_id;
	    public String code;
	    public String descr;
	    public String descrFull;
	    public String address;
	    public String address2;
	    public String comment;
	    public MyID curator_id;
	    public E_PRICE_TYPE priceType;
	    int Blocked;
	    int flags;
		public String email_for_cheques;
	    //ClientRecord &operator = (const ClientRecord &other);
	    //ClientRecord();
	    ClientRecord()
	    {
	    	id=new MyID();
	    	IsFolder=0;
	    	parent_id=new MyID();
	    	code="";
	    	descr="";
	    	descrFull="";
	    	address="";
	    	address2="";
	    	comment="";
	    	curator_id=new MyID();
	    	priceType=E_PRICE_TYPE.E_PRICE_TYPE_ROZN;
	    	Blocked=0;
	    	flags=0;
			email_for_cheques="";
	    }
	}
	
	static public class AgreementRecord
	{
	    public MyID id;
	    public MyID owner_id;
	    public MyID organization_id;
	    public MyID default_manager_id;
	    public String code;
	    public String descr;
	    public MyID price_type_id;
	    public MyID sale_id; // скидка, пока не используется
	    public int kredit_days; // -1 не контролируется
	    public double kredit_sum;
	    public int flags; // &1=1 - это договор чужого менеджера
	    
	    public AgreementRecord()
	    {
	    	id=new MyID();
	    	owner_id=new MyID();
	    	organization_id=new MyID();
	    	default_manager_id=new MyID();
	    	code=new String();
	    	descr=new String();
	    	price_type_id=new MyID();
	    	sale_id=new MyID();
	    	kredit_days=-1;
	    	kredit_sum=0.0;
	    	flags=0;
	    }
	}
	
	
	static public class PriceTypeRecord
	{
	    public MyID id;
	    public int isFolder;
	    public String code;
	    public String descr;
	    
	    public PriceTypeRecord()
	    {
	    	id=new MyID();
	    	isFolder=0;
	    	code=new String();
	    	descr=new String();
	    }
	}
	
	static public class PriceRecord
	{
	    public MyID nomenclature_id;
	    public MyID price_type_id;
	    public MyID ed_izm_id;
	    public String edIzm;
	    public double price;
	    public double priceProcent;
	    public double k;
	    
	    public PriceRecord()
	    {
	    	nomenclature_id=new MyID();
	    	price_type_id=new MyID();
	    	ed_izm_id=new MyID();
	    	edIzm=new String();
	    	price=0.0;
	    	priceProcent=0.0;
	    	k=1.0;
	    }
	}

	static public class PriceAgreement30Record
	{
		public MyID agreement30_id;
		public MyID nomenclature_id;
		public MyID pack_id;
		public MyID ed_izm_id;
		public String edIzm;
		public double price;
		public double k;

		public PriceAgreement30Record()
		{
			agreement30_id=new MyID();
			nomenclature_id=new MyID();
			pack_id=new MyID();
			ed_izm_id=new MyID();
			edIzm=new String();
			price=0.0;
			k=1.0;
		}
	}

	
	static public class SimpleDiscountTypeRecord
	{
	    public MyID id;
	    public String code;
	    public String descr;
	    double priceProcent;
	    
	    public SimpleDiscountTypeRecord()
	    {
	    	id=new MyID();
	    	code=new String();
	    	descr=new String();
	    	priceProcent=0.0;
	    }
	}
	
	
	static public class StockRecord
	{
	    public MyID id;
	    public int isFolder;
	    public String code;
	    public String descr;
	    public int flags;
	    
	    public StockRecord()
	    {
	    	id=new MyID();
	    	isFolder=0;
	    	code=new String();
	    	descr=new String();
	    	flags=0;
	    }
	}
	
	static public class DistribsContractsRecord
	{
	    public MyID id;
	    public int position;
	    public String code;
	    public String descr;
	    
	    public DistribsContractsRecord()
	    {
	    	id=new MyID();
	    	position=0;
	    	code=new String();
	    	descr=new String();
	    }
	}
	
	static public class EquipmentRecord
	{
	    public MyID id;
	    public String code;
	    public String descr;
	    public int flags;
	    
	    public EquipmentRecord()
	    {
	    	id=new MyID();
	    	code=new String();
	    	descr=new String();
	    	flags=0;
	    }
	}
	
	
	static public class SaldoRecord
	{
	    public MyID client_id;
	    public double saldo;
	    public double saldo_past;
	    public double saldo_past30;
		
	    public SaldoRecord()
	    {
	    	client_id=new MyID();
	    	saldo=0.0;
	    	saldo_past=0.0;
	    	saldo_past30=0.0;
	    }
	}
	
	static public class SaldoExtRecord
	{
	    public MyID client_id;
		public MyID agreement30_id; // здесь пока всегда пустой ID (как в 7-ке)
	    public MyID agreement_id;
	    public MyID document_id;
	    public MyID manager_id;
	    public String document_descr;
	    public String manager_descr;
	    public String agreement_descr;
	    public String organization_descr;
	    public String document_datetime;
	    
	    public double saldo;
	    public double saldo_past;
		
	    public SaldoExtRecord()
	    {
	    	client_id=new MyID();
            agreement30_id=new MyID();
		    agreement_id=new MyID();
		    document_id=new MyID();
		    manager_id=new MyID();
		    document_descr="";
		    manager_descr="";
		    agreement_descr="";
		    organization_descr="";	    
		    document_datetime="";
	    	saldo=0.0;
	    	saldo_past=0.0;
	    }
	}
	
	static public class AgentRecord
	{
	    public MyID id;
	    public String code;
	    public String descr;
	    
	    public AgentRecord()
	    {
	    	id=new MyID();
	    	code=new String();
	    	descr=new String();
	    }
	}
	
	static public class CuratorRecord
	{
	    public MyID id;
	    public MyID parent_id;
	    public int isFolder;
	    public String code;
	    public String descr;
	    
	    public CuratorRecord()
	    {
	    	id=new MyID();
	    	parent_id=new MyID();
	    	isFolder=0;
	    	code=new String();
	    	descr=new String();
	    }
	}

	
	static public class DistrPointRecord
	{
	    public MyID id;
	    public MyID owner_id;
	    public String code;
	    public String descr;
	    public String address;
	    public String phones;
	    public String contacts;
	    public MyID price_type_id;
	    
	    public DistrPointRecord()
	    {
	    	id=new MyID();
	    	owner_id=new MyID();
	    	code=new String();
	    	descr=new String();
	    	address=new String();
	    	phones=new String();
	    	contacts=new String();
	    	price_type_id=new MyID();
	    }
	}
	
	static public class OrganizationRecord
	{
	    public MyID id;
	    public String code;
	    public String descr;
	    
	    public OrganizationRecord()
	    {
	    	id=new MyID();
	    	code=new String();
	    	descr=new String();
	    }
	}
	
	static public class ClientPriceRecord
	{
	    public MyID client_id;
	    public MyID nomenclature_id;
	    public double priceAdd;
	    public double priceProcent;
	    
	    public ClientPriceRecord()
	    {
	    	client_id=new MyID();
	    	nomenclature_id=new MyID();
	    	priceAdd=0.0;
	    	priceProcent=0.0;
	    }
	}
	
	static public class CuratorPriceRecord
	{
	    public MyID curator_id;
	    public MyID nomenclature_id;
	    public double priceAdd;
	    public double priceProcent;
	    
	    public CuratorPriceRecord()
	    {
	    	curator_id=new MyID();
	    	nomenclature_id=new MyID();
	    	priceAdd=0.0;
	    	priceProcent=0.0;
	    }
	}

	static public class RouteRecord
    {
        public MyID id;
        public String code;
        public String descr;
        public MyID manager_id;

        public RouteRecord()
        {
            id=new MyID();
            code="";
            descr="";
            manager_id=new MyID();
        }
    }

    static public class RouteLineRecord
    {
        public int lineno;
        public MyID distr_point_id;
        public String visit_time;

        public RouteLineRecord()
        {
            lineno=0;
            distr_point_id=new MyID();
            visit_time="";
        }
    }

    static public class RouteDateRecord
    {
        public MyID route_id;
        public String route_date;

        public RouteDateRecord()
        {
            route_id=new MyID();
            route_date="";
        }
    }

	
	static public class VicariousPowerRecord
	{
		public MyID id;
		public String descr;
		public String numdoc;
		public String datedoc;
		public String date_action;
		public String comment;
		public MyID client_id;
		public String client_descr;
		public MyID agreement_id;
		public String agreement_descr;
		public String fio_descr;
		public MyID manager_id;
		public String manager_descr;
		public MyID organization_id;
		public String organization_descr;
		public String state;
		public double sum_doc;
	}
	
	static public class MessageRecord implements Parcelable
	{

		// Если только создан, но ни разу не заполнялся
		public boolean bCreatedInSingleton;

	    public UUID uid;
	    public MyID sender_id;
	    public MyID receiver_id;
	    public String text;
	    public String fname; // имя файла, если мы отправляем изображение
	    public String datetime; // [15];
	    // флаги, выставляемые в файле, выгруженном из 1С
	    // 1 - ПодтвержденоОтправителю
	    // 2 - ПодтвержденоПолучателем
	    // 4 - это наше сообщение
	    // 8 - надо отправить файл
	    public int acknowledged;
	    public int ver;
	    // новые
	    public int type_idx;
		public String date1;
		public String date2;
		public MyID client_id;
		public MyID agreement_id;
		public MyID nomenclature_id;
		public String report;
	    //
		public MessageRecord()
		{
			bCreatedInSingleton=false;
			uid=UUID.randomUUID();
			sender_id=new MyID();
			receiver_id=new MyID();
			text="";
			fname="";
			datetime="";
			acknowledged=0;
			ver=0;
		    type_idx=0;
			date1="";
			date2="";
			client_id=new MyID();
			agreement_id=new MyID();
			nomenclature_id=new MyID();
			report="";
		}

		MessageRecord(Parcel source) {
			bCreatedInSingleton = false;
			uid = UUID.fromString(source.readString());
			sender_id=source.readParcelable(MyID.class.getClassLoader());
			receiver_id=source.readParcelable(MyID.class.getClassLoader());
			text=source.readString();
			fname=source.readString();
			datetime=source.readString();
			acknowledged=source.readInt();
			ver=source.readInt();
			type_idx=source.readInt();
			date1=source.readString();
			date2=source.readString();
			client_id=source.readParcelable(MyID.class.getClassLoader());
			agreement_id=source.readParcelable(MyID.class.getClassLoader());
			nomenclature_id=source.readParcelable(MyID.class.getClassLoader());
			report=source.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(uid.toString());
			dest.writeParcelable(sender_id, flags);
			dest.writeParcelable(receiver_id, flags);
			dest.writeString(text);
			dest.writeString(fname);
			dest.writeString(datetime);
			dest.writeInt(acknowledged);
			dest.writeInt(ver);
			dest.writeInt(type_idx);
			dest.writeString(date1);
			dest.writeString(date2);
			dest.writeParcelable(client_id, flags);
			dest.writeParcelable(agreement_id, flags);
			dest.writeParcelable(nomenclature_id, flags);
			dest.writeString(report);
		}

		public static final Creator<MessageRecord> CREATOR = new Creator<MessageRecord>() {
			@Override
			public MessageRecord createFromParcel(final Parcel source) {
				return new MessageRecord(source);
			}

			@Override
			public MessageRecord[] newArray(final int size) {
				return new MessageRecord[size];
			}
		};

	}
	
	static public class SalesHistoryRecord
	{
		public String numdoc;
		public String datedoc;
		public MyID sale_doc_id;
		public MyID client_id;
		public MyID curator_id;
		public MyID distr_point_id;
		public MyID nomenclature_id;
		public double quantity;
		public double price;
		
		public SalesHistoryRecord()
		{
			numdoc="";
			datedoc="";
			sale_doc_id=new MyID();
			client_id=new MyID();
			curator_id=new MyID();
			distr_point_id=new MyID();
			nomenclature_id=new MyID();
			quantity=0.0f;
			price=0.0f;
		}
	}
	
	
	public static class OrderLineRecord implements Cloneable
	{
	    //int internal_id;
		public MyID nomenclature_id;
		public double quantity_requested; // первоначальное количество, когда создавали заявку
		public double quantity; // количество в единицах измерения
		public double price; // стоимость штуки или упаковки
		public double total; // общая стоимость
		public double discount; // скидка, %
	    //
		public double k; // коэффициент
		public String ed; // единица измерения
		//
		public String stuff_nomenclature;
		public double stuff_weight_k_1; // вес базовой единицы
		public double temp_quantity;
		
		public int stuff_nomenclature_flags;
		public String shipping_time;
		public String comment_in_line;
		
		public int lineno;
		
		// pharaon
		//public String stuff_nomenclature_node_group_id;

		//public OrderLineRecord &operator = (const OrderLineRecord &other);
	    //bool operator == (const OrderLineRecord &other) const;
	    //bool operator < (const OrderLineRecord &other) const;
		
		public OrderLineRecord()
		{
			nomenclature_id=new MyID();
			quantity_requested=0.0;
			quantity=0.0;
			price=0.0;
			total=0.0;
			discount=0.0;
			k=1.0;
			ed="";
			stuff_nomenclature="";
			temp_quantity=0.0;
			stuff_nomenclature_flags=0;
			//stuff_nomenclature_node_group_id="";
			shipping_time="";
			comment_in_line="";
			lineno=0;
		}
		
		public OrderLineRecord clone(){
			OrderLineRecord obj;
			try {
				obj = (OrderLineRecord)super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				obj = new OrderLineRecord();
			}
			obj.nomenclature_id=nomenclature_id.clone();
			obj.quantity_requested=quantity_requested;
			obj.quantity=quantity;
			obj.price=price;
			obj.total=total;
			obj.discount=discount;
			obj.k=k;
			obj.ed=new String(ed);
			obj.stuff_nomenclature=new String(stuff_nomenclature);
			obj.stuff_weight_k_1=stuff_weight_k_1;
			obj.temp_quantity=0.0; // всегда 0
			obj.stuff_nomenclature_flags=stuff_nomenclature_flags;
			//obj.stuff_nomenclature_node_group_id=new String(stuff_nomenclature_node_group_id);
			obj.shipping_time=new String(shipping_time);
			obj.comment_in_line=new String(comment_in_line);
			obj.lineno=lineno;
	        return obj;
	    }		
	};
	
	static public abstract class OrderOrRefundRecord implements Parcelable
	{
	    public UUID uid;
	    public int version; // версия выгрузки из сервера
	    public int version_ack;
	    public int versionPDA; // версия в КПК
	    public int versionPDA_ack;
	    public MyID id; // id из 1С
	    public String numdoc;
	    public String datedoc; // [15]
	    public MyID client_id;
		public MyID agreement30_id; // Соглашение
	    public MyID agreement_id;
	    public MyID distr_point_id;
	    public String comment;
	    public String comment_closing;
	    public int closed_not_full; // флаг 0,1
	    public MyID curator_id;
	    public E_BW bw;
	    public E_TRADE_TYPE trade_type;
	    // версия 353
	    public String datecreation;
	    // версия 102
	    public String datecoord;
	    public double latitude; // ddmm.mmmm
	    public double longitude; // dddmm.mmmm
	    public int gpsstate;
        public int gpstype;
	    public double gpsaccuracy;
	    public int accept_coord;
	    public int dont_need_send; // не отправлять, возврат не готов
	    // SNEGOROD
	    public MyID stock_id;
	    // версия 3.0
	    public MyID organization_id;
	    public double weightDoc;
	    
	    public String stuff_client_name;
	    public String stuff_client_address;
	    public double stuff_debt;
	    public double stuff_debt_past;
	    public double stuff_debt_past30;
	    // для FACTORY
		public boolean stuff_agreement_required; // В соглашении указан признак учета по договорам
        public String stuff_agreement30_name;
		//
	    public String stuff_agreement_name;
	    public String stuff_organization_name;
	    public String stuff_curator_name;
	    public String stuff_distr_point_name;
	    public String stuff_stock_descr;
	    public boolean stuff_select_account; // можно ли указывать тип учета
	    
	    public double stuff_agreement_debt;
	    public double stuff_agreement_debt_past;
	    
	    OrderOrRefundRecord()
	    {
		    uid=UUID.randomUUID();
		    version=0;
		    version_ack=0;
		    versionPDA=0;
		    versionPDA_ack=0;
		    id=new MyID();
		    numdoc="";
		    datedoc="";
		    client_id=new MyID();
		    agreement_id=new MyID();
		    distr_point_id=new MyID();
		    comment="";
		    comment_closing="";
		    closed_not_full=0;
		    curator_id=new MyID();
		    bw=E_BW.E_BW_UPR;
		    trade_type=E_TRADE_TYPE.E_TRADE_TYPE_NAL;
		    datecreation="";
		    datecoord="";
		    latitude=0.0;
		    longitude=0.0;
		    gpsstate=0;
            gpstype=0;
		    gpsaccuracy=0.0;	    
		    accept_coord=0;
		    dont_need_send=0;
		    //price_type_id=new MyID();
		    stock_id=new MyID();
		    //sumDoc=0.0;
		    weightDoc=0.0;
		    organization_id=new MyID();
		    
		    stuff_client_name="";
		    stuff_client_address="";
		    stuff_debt=0.0;
		    stuff_debt_past=0.0;
		    stuff_debt_past30=0.0;
			stuff_agreement_required=false;
            stuff_agreement30_name="";
		    stuff_agreement_name="";
		    stuff_organization_name="";
		    stuff_curator_name="";
		    stuff_distr_point_name="";
		    stuff_stock_descr="";
		    stuff_select_account=false;
		    stuff_agreement_debt=0.0;
		    stuff_agreement_debt_past=0.0;
		    
		    
	    }
	    
		OrderOrRefundRecord(Parcel source) {
			uid=UUID.fromString(source.readString());
		    version=source.readInt();
		    version_ack=source.readInt();
		    versionPDA=source.readInt();
		    versionPDA_ack=source.readInt();
		    id=source.readParcelable(MyID.class.getClassLoader());
		    numdoc=source.readString();
		    datedoc=source.readString();
		    client_id=source.readParcelable(MyID.class.getClassLoader());
            agreement30_id=source.readParcelable(MyID.class.getClassLoader());
		    agreement_id=source.readParcelable(MyID.class.getClassLoader());
		    distr_point_id=source.readParcelable(MyID.class.getClassLoader());
		    comment=source.readString();
		    comment_closing=source.readString();
		    closed_not_full=source.readInt();
		    curator_id=source.readParcelable(MyID.class.getClassLoader());
		    bw=source.readParcelable(E_BW.class.getClassLoader());
		    trade_type=source.readParcelable(E_TRADE_TYPE.class.getClassLoader());
		    datecreation=source.readString();
		    datecoord=source.readString();
		    latitude=source.readDouble();
		    longitude=source.readDouble();
		    gpsstate=source.readInt();
            gpstype=source.readInt();
		    gpsaccuracy=source.readDouble();
		    accept_coord=source.readInt();
		    dont_need_send=source.readInt();
		    stock_id=source.readParcelable(MyID.class.getClassLoader());
		    organization_id=source.readParcelable(MyID.class.getClassLoader());
		    weightDoc=source.readDouble();
		    stuff_client_name=source.readString();
		    stuff_client_address=source.readString();
		    stuff_debt=source.readDouble();
		    stuff_debt_past=source.readDouble();
		    stuff_debt_past30=source.readDouble();
			stuff_agreement_required=source.readByte()!=0;
            stuff_agreement30_name=source.readString();
		    stuff_agreement_name=source.readString();
		    stuff_organization_name=source.readString();
		    stuff_curator_name=source.readString();
		    stuff_distr_point_name=source.readString();
		    stuff_stock_descr=source.readString();
		    stuff_select_account=source.readByte()!=0;
		    stuff_agreement_debt=source.readDouble();
		    stuff_agreement_debt_past=source.readDouble();
		}
	    
	    
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel parcel, int flags) {
			parcel.writeString(uid.toString());
		    parcel.writeInt(version);
		    parcel.writeInt(version_ack);
		    parcel.writeInt(versionPDA);
		    parcel.writeInt(versionPDA_ack);
		    parcel.writeParcelable(id, flags);
		    parcel.writeString(numdoc);
		    parcel.writeString(datedoc);
		    parcel.writeParcelable(client_id, flags);
            parcel.writeParcelable(agreement30_id, flags);
		    parcel.writeParcelable(agreement_id, flags);
		    parcel.writeParcelable(distr_point_id, flags);
		    parcel.writeString(comment);
		    parcel.writeString(comment_closing);
		    parcel.writeInt(closed_not_full);
		    parcel.writeParcelable(curator_id, flags);
		    parcel.writeParcelable(bw, flags);
		    parcel.writeParcelable(trade_type, flags);
		    parcel.writeString(datecreation);
		    parcel.writeString(datecoord);
		    parcel.writeDouble(latitude);
		    parcel.writeDouble(longitude);
		    parcel.writeInt(gpsstate);
            parcel.writeInt(gpstype);
		    parcel.writeDouble(gpsaccuracy);
		    parcel.writeInt(accept_coord);
		    parcel.writeInt(dont_need_send);
		    parcel.writeParcelable(stock_id, flags);
		    parcel.writeParcelable(organization_id, flags);
		    parcel.writeDouble(weightDoc);
		    parcel.writeString(stuff_client_name);
		    parcel.writeString(stuff_client_address);
		    parcel.writeDouble(stuff_debt);
		    parcel.writeDouble(stuff_debt_past);
		    parcel.writeDouble(stuff_debt_past30);
			parcel.writeByte((byte)(stuff_agreement_required?1:0));
            parcel.writeString(stuff_agreement30_name);
		    parcel.writeString(stuff_agreement_name);
		    parcel.writeString(stuff_organization_name);
		    parcel.writeString(stuff_curator_name);
		    parcel.writeString(stuff_distr_point_name);
		    parcel.writeString(stuff_stock_descr);
		    parcel.writeByte((byte)(stuff_select_account?1:0));
		    parcel.writeDouble(stuff_agreement_debt);
		    parcel.writeDouble(stuff_agreement_debt_past);
		}
		
	};
	
	
	static public class OrderRecord extends OrderOrRefundRecord
	{
		// Если только создан, но ни разу не заполнялся
		public boolean bCreatedInSingleton;
	    //public UUID uid;
	    //public int version; // версия выгрузки из сервера
	    //public int version_ack;
	    //public int versionPDA; // версия в КПК
	    //public int versionPDA_ack;
	    ////int id_internal;
	    //public MyID id; // id из 1С
	    //public String numdoc;
	    //public String datedoc; // [15]
	    //public MyID client_id;
	    //public MyID agreement_id;
	    //public MyID distr_point_id;
	    //public String comment;
	    //public String comment_closing;
	    public String comment_payment;
	    //public int closed_not_full; // флаг 0,1
	    ////public QVector<OrderLineRecord> *lines;
	    public ArrayList<OrderLineRecord> lines;
	    public ArrayList<OrderPlaceRecord> places;
	    public E_ORDER_STATE state;
	    //public MyID curator_id;
	    //public E_BW bw;
	    //public E_TRADE_TYPE trade_type;
	    // версия 102
	    //public String datecoord;
	    //public double latitude; // ddmm.mmmm
	    //public double longitude; // dddmm.mmmm
	    //public int accept_coord;
	    //public int dont_need_send; // не отправлять, заявка не готова
	    // SNEGOROD
	    public MyID price_type_id;
	    //public MyID stock_id;
	    // версия Qt
	    public double sumDoc;
	    public double sumShipping;
	    // версия 3.0
	    //public MyID organization_id;
	    //public double weightDoc;
	    
	    // Продлидер
	    int shipping_type; // 0 - автотранспорт, 1 - самовывоз
	    int shipping_time; // 0 - любое, 1 - время 
	    public String shipping_begin_time; // hhmm
	    public String shipping_end_time; // hhmm
	    
	    //public String stuff_client_name;
	    //public String stuff_client_address;
	    //public double stuff_debt;
	    //public double stuff_debt_past;
	    //public String stuff_agreement_name;
	    //public String stuff_organization_name;
	    //public String stuff_curator_name;
	    //public String stuff_distr_point_name;
	    //public String stuff_stock_descr;
	    //public boolean stuff_select_account; // можно ли указывать тип учета
	    
	    // Титан
	    public String stuff_discount;
	    public double stuff_discount_procent;
	    
	    // Фараон
	    public MyID simple_discount_id;
	    public int create_client; // 0,1
	    public String create_client_surname;
	    public String create_client_firstname;
	    public String create_client_lastname;
	    //public String create_client_phone;
	    //public int place_num;
	    public int card_num;
	    public double pay_credit;
	    //public double ticket_m;
	    //public double ticket_w;
	    public int quant_mw;
	    //public int quant_m;
	    //public int quant_w;
	    //public double stuff_full_ticket_m; // без скидки
	    //public double stuff_full_ticket_w;
	    public String stuff_places;
	    public String shipping_date;
	    public String manager_comment;
	    public String theme_comment;
	    public String phone_num;
	    //public int editing_backup;
	    public long old_id;
	    
	    public void Clear()
	    {
	    	lines.clear();
	    	places.clear();
	    }
	    
	    OrderRecord()
	    {
	    	super();
	    	bCreatedInSingleton=false;
		    comment_payment="";
	    	lines=new ArrayList<OrderLineRecord>();
	    	places=new ArrayList<OrderPlaceRecord>();
		    state=E_ORDER_STATE.E_ORDER_STATE_CREATED;
		    price_type_id=new MyID();
		    sumDoc=0.0;
		    sumShipping=-1.0;
		    
		    simple_discount_id=new MyID();
		    stuff_discount="";
		    stuff_discount_procent=0.0;
		    
		    create_client=0;
		    create_client_surname="";
		    create_client_firstname="";
		    create_client_lastname="";
		    //create_client_phone="";
		    //place_num=0;
		    card_num=0;
		    pay_credit=0.0;
		    //ticket_m=0.0;
		    //ticket_w=0.0;
		    quant_mw=0;
		    //quant_m=0;
		    //quant_w=0;
		    //stuff_full_ticket_m=0.0;
		    //stuff_full_ticket_w=0.0;
		    stuff_places="";
		    manager_comment="";
		    theme_comment="";
		    phone_num="";
		    //editing_backup=0;
		    old_id=0;
		    
		    shipping_type=0;
		    shipping_time=0; 
		    shipping_begin_time="0000";
		    shipping_end_time="2359";	    	
	    }
	    
	    OrderRecord(Parcel source)
	    {
	    	super(source);
	    	bCreatedInSingleton=false;
	    	lines=new ArrayList<OrderLineRecord>();
	    	source.readList(lines, RefundLineRecord.class.getClassLoader());
	    	places=new ArrayList<OrderPlaceRecord>();
	    	source.readList(places, OrderPlaceRecord.class.getClassLoader());
		    comment_payment=source.readString();
		    state=source.readParcelable(E_ORDER_STATE.class.getClassLoader());
		    price_type_id=source.readParcelable(MyID.class.getClassLoader());
		    sumDoc=source.readDouble();
		    sumShipping=source.readDouble();
		    shipping_type=source.readInt();
		    shipping_time=source.readInt(); 
		    shipping_begin_time=source.readString();
		    shipping_end_time=source.readString();
		    stuff_discount=source.readString();
		    stuff_discount_procent=source.readDouble();
		    simple_discount_id=source.readParcelable(MyID.class.getClassLoader());
		    create_client=source.readInt();
		    create_client_surname=source.readString();
		    create_client_firstname=source.readString();
		    create_client_lastname=source.readString();
		    card_num=source.readInt();
		    pay_credit=source.readDouble();
		    quant_mw=source.readInt();
		    stuff_places=source.readString();
		    shipping_date=source.readString();
		    manager_comment=source.readString();
		    theme_comment=source.readString();
		    phone_num=source.readString();
		    old_id=source.readLong();
	    }
	    
	    //OrderRecord(const OrderRecord &other);
	    //~OrderRecord()
	    //OrderRecord &operator = (const OrderRecord &other);
	    //bool operator < (const OrderRecord &other) const;
	    //public double GetOrderSum(OrderLineRecord *line, bool excludeLine)
	    //{
	    //	
	    //}
	    //public double GetOrderWeight(CMyTextDatabase *data, OrderLineRecord *line, bool excludeLine)
	    //{
	    //	
	    //}
    
	    double GetOrderSum(OrderLineRecord line, boolean excludeLine)
	    {
	        // сумма
	        double sum=0;
	        //QVector<OrderLineRecord>::iterator it;
	        //for (it=lines->begin(); it!=lines->end(); it++)
	        for (OrderLineRecord it: lines)
	        {
	            if (line!=null)
	                if ((line==it&&excludeLine)||(line!=it&&!excludeLine))
	                    continue;
	            // фикс от 04.05.2017
	            // это на случай, что строка из документа в 1С была удалена
	            if (it.quantity<=0.000001)
	            	continue;
	            //
	            sum+=Math.floor(it.total*100+0.000001);
	        }
	        /*
	        if (Common.PHARAON)
	        {
	        	sum+=Math.floor(ticket_m*quant_m*100.0+0.000001)+Math.floor(ticket_w*quant_w*100.0+0.000001);
	        }
	        */
	        return sum/100.0;
	    }
	    
	    int getMaxLineno()
	    {
	    	int maxLineno=0;
	        for (OrderLineRecord it: lines)
	        {
	        	if (maxLineno<it.lineno)
	        	{
	        		maxLineno=it.lineno;
	        	}
	        }
	        return maxLineno;
	    }
	    
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
		    dest.writeList(lines);
		    dest.writeList(places);
		    dest.writeString(comment_payment);
		    dest.writeParcelable(state, flags);
		    dest.writeParcelable(price_type_id, flags);
		    dest.writeDouble(sumDoc);
		    dest.writeDouble(sumShipping);
		    dest.writeInt(shipping_type);
		    dest.writeInt(shipping_time); 
		    dest.writeString(shipping_begin_time);
		    dest.writeString(shipping_end_time);
		    dest.writeString(stuff_discount);
		    dest.writeDouble(stuff_discount_procent);
		    dest.writeParcelable(simple_discount_id, flags);
		    dest.writeInt(create_client);
		    dest.writeString(create_client_surname);
		    dest.writeString(create_client_firstname);
		    dest.writeString(create_client_lastname);
		    dest.writeInt(card_num);
		    dest.writeDouble(pay_credit);
		    dest.writeInt(quant_mw);
		    dest.writeString(stuff_places);
		    dest.writeString(shipping_date);
		    dest.writeString(manager_comment);
		    dest.writeString(theme_comment);
		    dest.writeString(phone_num);
		    dest.writeLong(old_id);
		}
		
		public static final Creator<OrderRecord> CREATOR = new Creator<OrderRecord>() {
	        @Override
	        public OrderRecord createFromParcel(final Parcel source) {
	        	return new OrderRecord(source);
	        }

	        @Override
	        public OrderRecord[] newArray(final int size) {
	            return new OrderRecord[size];
	        }
	    };

	    // Начиная с 20.03.2019 сделана отдельная функция
        // раньше цвет заполнялся только в журнале
        // (но, потом я уже заметил, что расчетная колонка в projectionMap для каждого вида документов есть)
	    public int getColor()
        {
            // JOURNAL_ORDER_COLOR_FOR_TRIGGER
            if (dont_need_send==1)
                return 1;
            // сначала состояния, при которых данные отправляются
            //////////////////////////////////////////
            // запрос отмены - серый
            if (state==E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL)
                return 2;
            // согласование - оранжевый
            if (state==E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT)
                return 6;
            // а потом все остальные
            /////////////////////////////////////////
            // отменен - красный
            if (state==E_ORDER_STATE.E_ORDER_STATE_CANCELED)
                return 7;
            // восстановлен, сбой - красный
            if (state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
                return 3;
            // неизвестное - красный
            if (state==E_ORDER_STATE.E_ORDER_STATE_UNKNOWN)
                return 3;
            // отправлен - голубой
            if (state==E_ORDER_STATE.E_ORDER_STATE_SENT)
                return 5;
            // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
            if (closed_not_full==1)
                return 4;
            // создана и будет отправляться - зеленая
            if (versionPDA!=versionPDA_ack && state==E_ORDER_STATE.E_ORDER_STATE_CREATED)
                return 10;
            return 0;
        }


	};
	
	
	public static class DistribsLineRecord implements Cloneable, Parcelable
	{
		public MyID distribs_contract_id;
		public double quantity;
		//
		public String stuff_distribs_contract;
		
		public DistribsLineRecord()
		{
			distribs_contract_id=new MyID();
			quantity=0.0;
			stuff_distribs_contract="";
		}
		
		public DistribsLineRecord(Parcel source)
		{
			distribs_contract_id=source.readParcelable(MyID.class.getClassLoader());
			quantity=source.readDouble();
			stuff_distribs_contract=source.readString();
		}
		
		public DistribsLineRecord clone(){
			DistribsLineRecord obj;
			try {
				obj = (DistribsLineRecord)super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				obj = new DistribsLineRecord();
			}
			obj.distribs_contract_id=distribs_contract_id.clone();
			obj.quantity=quantity;
			obj.stuff_distribs_contract=stuff_distribs_contract;
	        return obj;
	    }

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(distribs_contract_id, flags);
			dest.writeDouble(quantity);
			dest.writeString(stuff_distribs_contract);
		}
		
		public static final Creator<DistribsLineRecord> CREATOR = new Creator<DistribsLineRecord>() {
	        @Override
	        public DistribsLineRecord createFromParcel(final Parcel source) {
	        	return new DistribsLineRecord(source);
	        }

	        @Override
	        public DistribsLineRecord[] newArray(final int size) {
	            return new DistribsLineRecord[size];
	        }
	    };
		
	};
	
	
	static public class DistribsRecord implements Parcelable
	{
		// Если только создан, но ни разу не заполнялся
		public boolean bCreatedInSingleton;
		
	    public UUID uid;
	    public int version; // версия выгрузки из сервера
	    public int version_ack;
	    public int versionPDA; // версия в КПК
	    public int versionPDA_ack;
	    public MyID id; // id из 1С
	    public String numdoc;
	    public String datedoc; // [15]
	    //public String timedoc; // hhmm
	    public MyID client_id;
	    public MyID curator_id;
	    public MyID distr_point_id;
	    public String comment;
	    public ArrayList<DistribsLineRecord> lines;
	    public E_DISTRIBS_STATE state;
	    public String datecoord;
	    public double latitude; // ddmm.mmmm
	    public double longitude; // dddmm.mmmm
	    public int gpsstate;
        public int gpstype;
	    public double gpsaccuracy;	    
	    public int accept_coord;
	    public String stuff_client_name;
	    public String stuff_client_address;
	    public String stuff_curator_name;
	    public String stuff_distr_point_name;
	    
	    public double stuff_debt;
	    public double stuff_debt_past;
	    public double stuff_debt_past30;
	    
	    public void Clear()
	    {
	    	lines.clear();
	    }
	    DistribsRecord()
	    {
	    	bCreatedInSingleton=false;
		    uid=UUID.randomUUID();
		    version=0;
		    version_ack=0;
		    versionPDA=0;
		    versionPDA_ack=0;
		    id=new MyID();
		    numdoc="";
		    datedoc="";
		    //timedoc="0000";
		    client_id=new MyID();
		    distr_point_id=new MyID();
		    comment="";
	    	lines=new ArrayList<DistribsLineRecord>();
		    state=E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED;
		    curator_id=new MyID();
		    datecoord="";
		    latitude=0.0;
		    longitude=0.0;
		    gpsstate=0;
            gpstype=0;
		    gpsaccuracy=0.0;	    
		    accept_coord=0;
		    
		    stuff_client_name="";
		    stuff_client_address="";
		    stuff_debt=0.0;
		    stuff_debt_past=0.0;
		    stuff_debt_past30=0.0;
		    stuff_curator_name="";
		    stuff_distr_point_name="";
	    }
	    
	    DistribsRecord(Parcel source)
	    {
	    	bCreatedInSingleton=false;
	    	uid=UUID.fromString(source.readString());
		    version=source.readInt();
		    version_ack=source.readInt();
		    versionPDA=source.readInt();
		    versionPDA_ack=source.readInt();
		    id=source.readParcelable(MyID.class.getClassLoader());
		    numdoc=source.readString();
		    datedoc=source.readString();
		    client_id=source.readParcelable(MyID.class.getClassLoader());
		    curator_id=source.readParcelable(MyID.class.getClassLoader());
		    distr_point_id=source.readParcelable(MyID.class.getClassLoader());
		    comment=source.readString();
		    lines=new ArrayList<DistribsLineRecord>();
		    source.readList(lines, DistribsLineRecord.class.getClassLoader());
		    state=source.readParcelable(E_DISTRIBS_STATE.class.getClassLoader());
		    datecoord=source.readString();
		    latitude=source.readDouble();
		    longitude=source.readDouble();
		    gpsstate=source.readInt();
            gpstype=source.readInt();
		    gpsaccuracy=source.readDouble();	    
		    accept_coord=source.readInt();
		    stuff_client_name=source.readString();
		    stuff_client_address=source.readString();
		    stuff_curator_name=source.readString();
		    stuff_distr_point_name=source.readString();
		    stuff_debt=source.readDouble();
		    stuff_debt_past=source.readDouble();
		    stuff_debt_past30=source.readDouble();
	    }
	    
		@Override
		public int describeContents() {
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
		    dest.writeString(uid.toString());
		    dest.writeInt(version);
		    dest.writeInt(version_ack);
		    dest.writeInt(versionPDA);
		    dest.writeInt(versionPDA_ack);
		    dest.writeParcelable(id, flags);
		    dest.writeString(numdoc);
		    dest.writeString(datedoc);
		    dest.writeParcelable(client_id, flags);
		    dest.writeParcelable(curator_id, flags);
		    dest.writeParcelable(distr_point_id, flags);
		    dest.writeString(comment);
		    dest.writeList(lines);
		    dest.writeParcelable(state, flags);
		    dest.writeString(datecoord);
		    dest.writeDouble(latitude);
		    dest.writeDouble(longitude);
		    dest.writeInt(gpsstate);
            dest.writeInt(gpstype);
		    dest.writeDouble(gpsaccuracy);	    
		    dest.writeInt(accept_coord);
		    dest.writeString(stuff_client_name);
		    dest.writeString(stuff_client_address);
		    dest.writeString(stuff_curator_name);
		    dest.writeString(stuff_distr_point_name);
		    dest.writeDouble(stuff_debt);
		    dest.writeDouble(stuff_debt_past);
		    dest.writeDouble(stuff_debt_past30);
		}
		
		public static final Creator<DistribsRecord> CREATOR = new Creator<DistribsRecord>() {
	        @Override
	        public DistribsRecord createFromParcel(final Parcel source) {
	        	return new DistribsRecord(source);
	        }

	        @Override
	        public DistribsRecord[] newArray(final int size) {
	            return new DistribsRecord[size];
	        }
	    };

        public int getColor(){
            // TODO
            return 0;
        }

	};
	
	
	public static class OrderPlaceRecord implements Cloneable
	{
		public MyID place_id;
		public String stuff_descr;
		
		public OrderPlaceRecord clone(){
			OrderPlaceRecord obj;
			try {
				obj = (OrderPlaceRecord)super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				obj = new OrderPlaceRecord();
			}
			obj.place_id=place_id.clone();
			obj.stuff_descr=new String(stuff_descr);
	        return obj;
	    }		
		
	};
		
    public static final class Orders implements BaseColumns {
    	
        private Orders() {
        }		
	
        /*
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + MTradeContentProvider.AUTHORITY + "/orders");
 
        //public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwei512.notes";
 
        public static final String ORDER_ID = "ORDER_ID";
 
        public static final String TITLE = "title";
 
        public static final String TEXT = "text";
        */
    }

    
	static public class CashPaymentRecord implements Parcelable
	{
		// Если только создан, но ни разу не заполнялся
		public boolean bCreatedInSingleton;
		
	    public UUID uid;
	    public int version; // версия выгрузки из сервера
	    public int version_ack;
	    public int versionPDA; // версия в КПК
	    public int versionPDA_ack;
	    public MyID id; // id из 1С
	    public String numdoc;
	    public String datedoc; // [15]
	    public MyID client_id;
	    public MyID agreement_id;
		public MyID distr_point_id; // только для привязки к маршруту
	    public String comment;
	    public String comment_closing;
	    public E_PAYMENT_STATE state;
	    public MyID manager_id;
	    public MyID vicarious_power_id;
	    public String vicarious_power_descr;
	    public double sumDoc;

		// версия 371
		public String datecreation;
		public String datecoord;
		public double latitude; // ddmm.mmmm
		public double longitude; // dddmm.mmmm
		public int gpsstate;
        public int gpstype;
		public double gpsaccuracy;
		public int accept_coord;
	    //
	    
	    public String stuff_client_name;
	    public String stuff_client_address;
	    public double stuff_debt;
	    public double stuff_debt_past;
	    public double stuff_debt_past30;
	    public String stuff_agreement_name;
	    public String stuff_organization_name;
	    public String stuff_manager_name;
	    public double stuff_agreement_debt;
	    public double stuff_agreement_debt_past;

	    public String stuff_email;

	    CashPaymentRecord()
	    {
	    	bCreatedInSingleton=false;
		    uid=UUID.randomUUID();
		    version=0;
		    version_ack=0;
		    versionPDA=0;
		    versionPDA_ack=0;
		    id=new MyID();
		    numdoc="";
		    datedoc="";
		    client_id=new MyID();
		    agreement_id=new MyID();
			distr_point_id=new MyID();
		    comment="";
		    comment_closing="";
		    state=E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED;
		    manager_id=new MyID();
		    vicarious_power_id=new MyID();
		    vicarious_power_descr="";
		    sumDoc=0.0;

            datecreation="";
            datecoord="";
            latitude=0.0;
            longitude=0.0;
            gpsstate=0;
            gpsaccuracy=0.0;
            gpstype=0;
            accept_coord=0;

		    stuff_client_name="";
		    stuff_client_address="";
		    stuff_debt=0.0;
		    stuff_debt_past=0.0;
		    stuff_debt_past30=0.0;
		    stuff_agreement_name="";
		    stuff_organization_name="";
		    stuff_manager_name="";
		    stuff_agreement_debt=0.0;
		    stuff_agreement_debt_past=0.0;
			stuff_email="";
	    }
	    
	    CashPaymentRecord(Parcel source)
	    {
	    	bCreatedInSingleton=false;
			uid=UUID.fromString(source.readString());
			version=source.readInt();
			version_ack=source.readInt();
			versionPDA=source.readInt();
			versionPDA_ack=source.readInt();
			id=source.readParcelable(MyID.class.getClassLoader());
		    numdoc=source.readString();
		    datedoc=source.readString();
		    client_id=source.readParcelable(MyID.class.getClassLoader());
		    agreement_id=source.readParcelable(MyID.class.getClassLoader());
			distr_point_id=source.readParcelable(MyID.class.getClassLoader());
		    comment=source.readString();
		    comment_closing=source.readString();
		    state=source.readParcelable(MyID.class.getClassLoader());
		    manager_id=source.readParcelable(MyID.class.getClassLoader());
		    vicarious_power_id=source.readParcelable(MyID.class.getClassLoader());
		    vicarious_power_descr=source.readString();
		    sumDoc=source.readDouble();

            datecreation=source.readString();
            datecoord=source.readString();
            latitude=source.readDouble();
            longitude=source.readDouble();
            gpsstate=source.readInt();
            gpsaccuracy=source.readDouble();
            gpstype=source.readInt();
            accept_coord=source.readInt();

		    stuff_client_name=source.readString();
		    stuff_client_address=source.readString();
		    stuff_debt=source.readDouble();
		    stuff_debt_past=source.readDouble();
		    stuff_debt_past30=source.readDouble();
		    stuff_agreement_name=source.readString();
		    stuff_organization_name=source.readString();
		    stuff_manager_name=source.readString();
		    stuff_agreement_debt=source.readDouble();
		    stuff_agreement_debt_past=source.readDouble();

			stuff_email=source.readString();
	    }
	    

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(uid.toString());
			dest.writeInt(version);
			dest.writeInt(version_ack);
			dest.writeInt(versionPDA);
			dest.writeInt(versionPDA_ack);
			dest.writeParcelable(id, flags);
		    dest.writeString(numdoc);
		    dest.writeString(datedoc);
		    dest.writeParcelable(client_id, flags);
		    dest.writeParcelable(agreement_id, flags);
			dest.writeParcelable(distr_point_id, flags);
		    dest.writeString(comment);
		    dest.writeString(comment_closing);
		    dest.writeParcelable(state, flags);
		    dest.writeParcelable(manager_id, flags);
		    dest.writeParcelable(vicarious_power_id, flags);
		    dest.writeString(vicarious_power_descr);
		    dest.writeDouble(sumDoc);

            dest.writeString(datecreation);
            dest.writeString(datecoord);
            dest.writeDouble(latitude);
            dest.writeDouble(longitude);
            dest.writeInt(gpsstate);
            dest.writeDouble(gpsaccuracy);
            dest.writeInt(gpstype);
            dest.writeInt(accept_coord);

		    dest.writeString(stuff_client_name);
		    dest.writeString(stuff_client_address);
		    dest.writeDouble(stuff_debt);
		    dest.writeDouble(stuff_debt_past);
		    dest.writeDouble(stuff_debt_past30);
		    dest.writeString(stuff_agreement_name);
		    dest.writeString(stuff_organization_name);
		    dest.writeString(stuff_manager_name);
		    dest.writeDouble(stuff_agreement_debt);
		    dest.writeDouble(stuff_agreement_debt_past);

			dest.writeString(stuff_email);
		}
		
		public static final Creator<CashPaymentRecord> CREATOR = new Creator<CashPaymentRecord>() {
	        @Override
	        public CashPaymentRecord createFromParcel(final Parcel source) {
	        	return new CashPaymentRecord(source);
	        }

	        @Override
	        public CashPaymentRecord[] newArray(final int size) {
	            return new CashPaymentRecord[size];
	        }
	    };

        public int getColor(){
            // TODO
            return 0;
        }

	};
	
	
	public static class RefundLineRecord implements Cloneable, Parcelable
	{
		public MyID nomenclature_id;
		public double quantity_requested; // первоначальное количество, когда создавали заявку
		public double quantity; // количество в единицах измерения
		//public double price; // стоимость штуки или упаковки
		//public double total; // общая стоимость
		//public double discount; // скидка, %
	    //
		public double k; // коэффициент
		public String ed; // единица измерения
		//
		public String stuff_nomenclature;
		public double stuff_weight_k_1; // вес базовой единицы
		public double temp_quantity;
		
		public int stuff_nomenclature_flags;
		public String comment_in_line;
		
		public RefundLineRecord()
		{
			nomenclature_id=new MyID();
			quantity_requested=0.0;
			quantity=0.0;
			//price=0.0;
			//total=0.0;
			//discount=0.0;
			k=1.0;
			ed="";
			stuff_nomenclature="";
			temp_quantity=0.0;
			stuff_nomenclature_flags=0;
			comment_in_line="";
		}
		
		public RefundLineRecord(Parcel source) {
			nomenclature_id=source.readParcelable(MyID.class.getClassLoader());
			quantity_requested=source.readDouble();
			quantity=source.readDouble();
			k=source.readDouble();
			ed=source.readString();
			stuff_nomenclature=source.readString();
			stuff_weight_k_1=source.readDouble();
			temp_quantity=source.readDouble();
			stuff_nomenclature_flags=source.readInt();
			comment_in_line=source.readString();
		}
		
		
		public RefundLineRecord clone(){
			RefundLineRecord obj;
			try {
				obj = (RefundLineRecord)super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				obj = new RefundLineRecord();
			}
			obj.nomenclature_id=nomenclature_id.clone();
			obj.quantity_requested=quantity_requested;
			obj.quantity=quantity;
			//obj.price=price;
			//obj.total=total;
			//obj.discount=discount;
			obj.k=k;
			obj.ed=new String(ed);
			obj.stuff_nomenclature=new String(stuff_nomenclature);
			obj.stuff_weight_k_1=stuff_weight_k_1;
			obj.temp_quantity=0.0; // всегда 0
			obj.stuff_nomenclature_flags=stuff_nomenclature_flags;
			obj.comment_in_line=new String(comment_in_line);
	        return obj;
	    }

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(nomenclature_id, flags);
			dest.writeDouble(quantity_requested);
			dest.writeDouble(quantity);
			dest.writeDouble(k);
			dest.writeString(ed);
			dest.writeString(stuff_nomenclature);
			dest.writeDouble(stuff_weight_k_1);
			dest.writeDouble(temp_quantity);
			dest.writeInt(stuff_nomenclature_flags);
			dest.writeString(comment_in_line);
		}
		
		public static final Creator<RefundLineRecord> CREATOR = new Creator<RefundLineRecord>() {
	        @Override
	        public RefundLineRecord createFromParcel(final Parcel source) {
	        	return new RefundLineRecord(source);
	        }

	        @Override
	        public RefundLineRecord[] newArray(final int size) {
	            return new RefundLineRecord[size];
	        }
	    };
		
		
	};
	
	static public class RefundRecord extends OrderOrRefundRecord
	{
		// Если только создан, но ни разу не заполнялся
		public boolean bCreatedInSingleton;
		
		public E_REFUND_STATE state;
	    public ArrayList<RefundLineRecord> lines;

		int shipping_type; // 0 - автотранспорт, 1 - самовывоз

		public void Clear()
	    {
	    	lines.clear();
	    }
	    RefundRecord()
	    {
	    	super();
			bCreatedInSingleton=false;
		    state=E_REFUND_STATE.E_REFUND_STATE_CREATED;
	    	lines=new ArrayList<RefundLineRecord>();
			shipping_type=0;
	    }
	    
	    RefundRecord(Parcel source)
	    {
	    	super(source);
	    	bCreatedInSingleton=false;
	    	state=source.readParcelable(E_REFUND_STATE.class.getClassLoader());
			shipping_type=source.readInt();
	    	lines=new ArrayList<RefundLineRecord>();
	    	source.readList(lines, RefundLineRecord.class.getClassLoader());
	    }
	    
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeParcelable(state, flags);
			dest.writeInt(shipping_type);
		    dest.writeList(lines);
		}
	    
		public static final Creator<RefundRecord> CREATOR = new Creator<RefundRecord>() {
	        @Override
	        public RefundRecord createFromParcel(final Parcel source) {
	        	return new RefundRecord(source);
	        }

	        @Override
	        public RefundRecord[] newArray(final int size) {
	            return new RefundRecord[size];
	        }
	    };

        public int getColor(){
            // TODO
            return 0;
        }
	    
	};
		
    public static final class Clients implements BaseColumns {
    	
        private Clients() {
        }		
	
        /*
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + MTradeContentProvider.AUTHORITY + "/clients");
 
        //public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwei512.notes";
 
        public static final String CLIENT_ID = "CLIENT_ID";
 
        public static final String DESCR = "descr";
 
        public static final String ADDRESS = "address";
        */
        
    }
    

}
