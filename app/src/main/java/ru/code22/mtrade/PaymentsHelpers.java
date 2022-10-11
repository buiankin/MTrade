package ru.code22.mtrade;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;

public class PaymentsHelpers {

    static public int fillPaymentColor(int defaultTextColor, Resources resources, int color, View viewBackground) {
        // Цвета те же самые
        return OrdersHelpers.fillOrderColor(defaultTextColor, resources, color, viewBackground);
    }


}
