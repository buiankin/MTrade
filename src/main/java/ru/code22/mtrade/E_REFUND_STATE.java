package ru.code22.mtrade;

import java.util.Map;
import java.util.TreeMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum E_REFUND_STATE implements Parcelable 
{
    // этих состояний в 1С нет
    E_REFUND_STATE_CREATED,//0
    E_REFUND_STATE_SENT,//1
    E_REFUND_STATE_QUERY_CANCEL,//2
    // эти есть
    E_REFUND_STATE_LOADED,
    E_REFUND_STATE_ACKNOWLEDGED,
    E_REFUND_STATE_COMPLETED,
    E_REFUND_STATE_CANCELED,
    // только продлидер
    E_REFUND_STATE_WAITING_AGREEMENT,
    //
    // этого нет в 1С
    E_REFUND_STATE_UNKNOWN,
    E_REFUND_STATE_BACKUP_NOT_SAVED
    ;
    
    private static Map<Integer, E_REFUND_STATE> ss = new TreeMap<Integer,E_REFUND_STATE>();
    private static final int START_VALUE = 0;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_REFUND_STATE fromInt(int i) {
        return ss.get(i);
    }

    public int value() {
    	return value;
    }
    
    static String getAcceptCoordConditionWhere()
    {
    	return "state in(?,?,?,?,?) and accept_coord=?";
    }
    static String[] getAcceptCoordConditionSelectionArgs()
    {
    	return new String[]{
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_SENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_LOADED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()),
			"1"};
    }
    
    static String getNotClosedConditionWhere()
    {
    	return "state in(?,?,?,?,?,?)";
    }
    static String[] getNotClosedSelectionArgs()
    {
    	return new String[]{
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_SENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_LOADED.value()),
			//Integer.toString(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value())
    	};
    }
    
    static String getCanBeDeletedConditionWhere()
    {
    	return "state in(?,?,?,?,?)";
    }
    static String[] getCanBeDeletedArgs()
    {
    	return new String[]{
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CANCELED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_COMPLETED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value())
    	};
    }
    static boolean getCanBeDeleted(E_REFUND_STATE state)
    {
    	return (state==E_REFUND_STATE.E_REFUND_STATE_CREATED||
    		state==E_REFUND_STATE.E_REFUND_STATE_CANCELED||
    		state==E_REFUND_STATE.E_REFUND_STATE_COMPLETED||
    		state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED||
    		state==E_REFUND_STATE.E_REFUND_STATE_UNKNOWN);
    }

    static String getCanBeCanceledConditionWhere()
    {
    	return "state in(?,?,?,?,?,?,?)";
    }
    static String[] getCanBeCanceledArgs()
    {
    	return new String[]{
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_SENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_LOADED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_COMPLETED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value())
    	};
    }
    
    static boolean getCanBeCanceled(E_REFUND_STATE state)
    {
    	return (state==E_REFUND_STATE.E_REFUND_STATE_CREATED||
    		state==E_REFUND_STATE.E_REFUND_STATE_SENT||
    		state==E_REFUND_STATE.E_REFUND_STATE_LOADED||
    		state==E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED||
    		state==E_REFUND_STATE.E_REFUND_STATE_COMPLETED||
    		state==E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT||
    		state==E_REFUND_STATE.E_REFUND_STATE_UNKNOWN);
    	
    }
    
    static boolean getCanBeRestoredFromTextFile(E_REFUND_STATE state)
    {
		return state==E_REFUND_STATE.E_REFUND_STATE_CREATED||
			state==E_REFUND_STATE.E_REFUND_STATE_UNKNOWN;
    }
    
    
    static String getStatisticConditionWhere()
    {
    	return "(dont_need_send=0 and state=? or state in(?,?,?,?,?))";
    }
    static String[] getStatisticArgs()
    {
    	return new String[]{
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_CREATED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_SENT.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_LOADED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_COMPLETED.value()),
			Integer.toString(E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value())
    	};
    }
    
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
	}	

	public static final Creator<E_REFUND_STATE> CREATOR = new Creator<E_REFUND_STATE>() {
        @Override
        public E_REFUND_STATE createFromParcel(final Parcel source) {
        	return E_REFUND_STATE.fromInt(source.readInt());
        }

        @Override
        public E_REFUND_STATE[] newArray(final int size) {
            return new E_REFUND_STATE[size];
        }
    };	
    
    
};


