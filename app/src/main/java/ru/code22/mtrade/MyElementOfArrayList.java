package ru.code22.mtrade;

public class MyElementOfArrayList extends Object {

    private String id;
    private String descr;

    public MyElementOfArrayList(String id, String descr)
    {
        this.id=id;
        this.descr=descr;
    }

    public String getId(){return id;}
    public String getDescr(){return descr;}

    @Override
    public String toString() {
        return descr;
    }


}
