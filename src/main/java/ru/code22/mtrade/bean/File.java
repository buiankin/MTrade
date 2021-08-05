package ru.code22.mtrade.bean;

import com.recyclertreeview_lib.LayoutItemType;

import ru.code22.mtrade.R;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class File implements LayoutItemType {
    public String fileName;

    public File(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_file;
    }
}
