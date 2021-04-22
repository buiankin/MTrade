package ru.code22.mtrade;

import java.util.Map;
import java.util.TreeMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum E_DISTRIBS_STATE implements Parcelable
{
    // этих состояний в 1С нет
    E_DISTRIBS_STATE_CREATED,//0
    E_DISTRIBS_STATE_SENT,//1
    E_DISTRIBS_STATE_QUERY_CANCEL,//2
    // эти есть
    E_DISTRIBS_STATE_LOADED,
    E_DISTRIBS_STATE_ACKNOWLEDGED,
    E_DISTRIBS_STATE_COMPLETED,
    E_DISTRIBS_STATE_CANCELED,
    //
    // этого нет в 1С
    E_DISTRIBS_STATE_UNKNOWN
    ;
    
    private static Map<Integer, E_DISTRIBS_STATE> ss = new TreeMap<Integer,E_DISTRIBS_STATE>();
    private static final int START_VALUE = 0;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_DISTRIBS_STATE fromInt(int i) {
        return ss.get(i);
    }

    public int value() {
    return value;
    }
    
    static String getAcceptCoordConditionWhere()
    {
    	return "state in(?,?,?,?) and accept_coord=?";
    }
    static String[] getAcceptCoordConditionSelectionArgs()
    {
    	return new String[]{
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED.value()),
			"1"};
    }
    
    
    static String getDistribsNotClosedConditionWhere()
    {
    	return "state in(?,?,?,?,?,?)";
    }
    static String[] getDistribsNotClosedSelectionArgs()
    {
    	return new String[]{
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value())
    	};
    }
    
    static String getDistribsCanBeDeletedConditionWhere()
    {
    	return "state in(?,?,?,?,?)";
    }
    static String[] getDistribsCanBeDeletedArgs()
    {
    	return new String[]{
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value())
    	};
    }
    static boolean getDistribsCanBeDeleted(E_DISTRIBS_STATE state)
    {
    	return (state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN);
    }

    static String getDistribsCanBeCanceledConditionWhere()
    {
    	return "state in(?,?,?,?,?,?)";
    }
    static String[] getDistribsCanBeCanceledArgs()
    {
    	return new String[]{
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value())
    	};
    }
    
    static boolean getDistribsCanBeCanceled(E_DISTRIBS_STATE state)
    {
    	return (state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED||
    		state==E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN);
    	
    }
    
    static String getDistribsStatisticConditionWhere()
    {
    	return "(state=? or state in(?,?,?,?))";
    }
    static String[] getDistribsStatisticArgs()
    {
    	return new String[]{
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_LOADED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_ACKNOWLEDGED.value()),
			Integer.toString(E_DISTRIBS_STATE.E_DISTRIBS_STATE_COMPLETED.value())
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
    
	public static final Creator<E_DISTRIBS_STATE> CREATOR = new Creator<E_DISTRIBS_STATE>() {
        @Override
        public E_DISTRIBS_STATE createFromParcel(final Parcel source) {
        	return E_DISTRIBS_STATE.fromInt(source.readInt());
        }

        @Override
        public E_DISTRIBS_STATE[] newArray(final int size) {
            return new E_DISTRIBS_STATE[size];
        }
    };	
    
   
};
