package ru.code22.mtrade;

import java.util.Map;
import java.util.TreeMap;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum E_PRICE_TYPE implements Parcelable
{
    E_PRICE_TYPE_OPT,
    E_PRICE_TYPE_M_OPT,
    E_PRICE_TYPE_ROZN,
    E_PRICE_TYPE_INCOM;
    
    private static Map<Integer, E_PRICE_TYPE> ss = new TreeMap<Integer,E_PRICE_TYPE>();
    private static final int START_VALUE = 0;
    private int value;

    static {
        for(int i=0;i<values().length;i++)
        {
            values()[i].value = START_VALUE + i;
            ss.put(values()[i].value, values()[i]);
        }
    }

    public static E_PRICE_TYPE fromInt(int i) {
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
    
    
	public static final Creator<E_PRICE_TYPE> CREATOR = new Creator<E_PRICE_TYPE>() {
        @Override
        public E_PRICE_TYPE createFromParcel(final Parcel source) {
        	return E_PRICE_TYPE.fromInt(source.readInt());
        }

        @Override
        public E_PRICE_TYPE[] newArray(final int size) {
            return new E_PRICE_TYPE[size];
        }
    };

};
