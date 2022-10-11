package ru.code22.mtrade.bean;

import com.recyclertreeview_lib.LayoutItemType;

import ru.code22.mtrade.R;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class NomenclatureGroup implements LayoutItemType {
    public String nomenclatureName;

    public NomenclatureGroup(String nomenclatureName) {
        this.nomenclatureName = nomenclatureName;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_dir;
    }
}
