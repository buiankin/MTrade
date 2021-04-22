package ru.code22.mtrade;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class MyHashMapAdapter<T_Key, T_Descr> extends ArrayAdapter<T_Descr> {

    ArrayList<T_Key> mKeys;
    ArrayList<T_Descr> mValues;

    public MyHashMapAdapter(@NonNull Context context, int resource, HashMap<T_Key, T_Descr> map) {
        super(context, resource);
        mKeys=new ArrayList();
        mValues=new ArrayList();
        for(HashMap.Entry<T_Key, T_Descr> entry: map.entrySet())
        {
            //mKeys
        }
        //super(context, resource, mValues);

    }

}
