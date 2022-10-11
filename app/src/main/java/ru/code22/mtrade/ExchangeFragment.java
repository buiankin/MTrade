package ru.code22.mtrade;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ExchangeFragment extends Fragment {

    private UniversalInterface mListener;

    private String m_exchangeState;
    private ArrayList<String> m_exchange_log_text;

    public ExchangeFragment() {
    }

    void readDataFromBundle(Bundle bundle) {
        if (bundle != null) {
            m_exchangeState=bundle.getString("exchange_state");
            m_exchange_log_text=bundle.getStringArrayList("exchange_log_text");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        outState.putString("exchange_state", m_exchangeState);
        outState.putStringArrayList("exchange_log_text", m_exchange_log_text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (UniversalInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement interface UniversalInterface");
        }
    }

    public void setButtonsExchangeEnabled(boolean enabled)
    {
        View view=getView();

        Button btnConnect = (Button) view.findViewById(R.id.btnConnect);
        Button btnRefresh = (Button) view.findViewById(R.id.btnRefresh);
        Button btnQueryDocs = (Button) view.findViewById(R.id.btnQueryDocs);
        Button btnReceive = (Button) view.findViewById(R.id.btnReceive);
        Button btnNomenclaturePhotos = (Button) view.findViewById(R.id.btnNomenclaturePhotos);
        Button btnExchangeWebService = (Button) view.findViewById(R.id.btnExchangeWebService);

        btnConnect.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnQueryDocs.setEnabled(enabled);
        btnReceive.setEnabled(enabled);
        btnNomenclaturePhotos.setEnabled(enabled);
        btnExchangeWebService.setEnabled(enabled);

    }

    public void setExchangeState(View view, CharSequence text)
    {
        if (view==null) {
            view = getView();
        }
        TextView tvExchangeState = (TextView) view.findViewById(R.id.tvExchangeState);
        // Прервано
        tvExchangeState.setText(text);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState==null)
        {
            // Если данные не сохранялись, берем установленные прогрммно при создании фрейма
            savedInstanceState=getArguments();
        }

        readDataFromBundle(savedInstanceState);

        View view = inflater.inflate(R.layout.exchange_fragment, container, false);
        updatePageMessagesVisibility(view);

        setExchangeState(view, m_exchangeState);
        setLogText(view, m_exchange_log_text);

        return view;
    }


    public void setLogText(View view, ArrayList<String> text)
    {
        if (view==null) {
            view=getView();
        }
        EditText etLog = (EditText) view.findViewById(R.id.etTest);
        etLog.setText(TextUtils.join("\n", text));
    }

    public void appendLogText(View view, String text)
    {
        if (view==null) {
            view=getView();
        }
        EditText etLog = (EditText) view.findViewById(R.id.etTest);
        etLog.append(text + "\n");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    void updatePageMessagesVisibility(View view) {
        MySingleton g = MySingleton.getInstance();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // TODO сделать видимость соответствующей закладки
        boolean bDontShowMessages = sharedPreferences.getString("data_format", "DM").equals("PH");
        // TODO
        /*
        if (bDontShowMessages) {
            if (tabHost.getCurrentTab() == 2) {
                tabHost.setCurrentTab(0);
            }
            tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
        } else {
            tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
        }
        */

        Button btnConnect = (Button) view.findViewById(R.id.btnConnect); // 1
        Button btnRefresh = (Button) view.findViewById(R.id.btnRefresh); // 1
        Button btnQueryDocs = (Button) view.findViewById(R.id.btnQueryDocs);
        Button btnReceive = (Button) view.findViewById(R.id.btnReceive); // 0
        Button btnNomenclaturePhotos = (Button) view.findViewById(R.id.btnNomenclaturePhotos); // 2
        Button btnExchangeWebService = (Button) view.findViewById(R.id.btnExchangeWebService);

        if (g.Common.PHARAON) {
            btnConnect.setVisibility(View.GONE);
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setText(R.string.exchange_ws_all);
            btnReceive.setVisibility(View.GONE);
            btnNomenclaturePhotos.setVisibility(View.GONE);
            btnExchangeWebService.setVisibility(View.VISIBLE);
            btnQueryDocs.setVisibility(View.VISIBLE);
        } else {
            btnConnect.setVisibility(View.VISIBLE);
            btnRefresh.setVisibility(View.VISIBLE);
            btnRefresh.setText(R.string.refresh);
            btnReceive.setVisibility(View.VISIBLE);
            if (!g.Common.PRAIT && !g.Common.MEGA && !g.Common.PRODLIDER && !g.Common.TITAN && !g.Common.TANDEM && !g.Common.ISTART && !g.Common.FACTORY)
                btnNomenclaturePhotos.setVisibility(View.GONE);
            else
                btnNomenclaturePhotos.setVisibility(View.VISIBLE);
            btnExchangeWebService.setVisibility(View.GONE);
            btnQueryDocs.setVisibility(View.GONE);
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUniversalEventListener("ExchangeConnect", null);
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUniversalEventListener("ExchangeRefresh", null);
            }
        });

        btnQueryDocs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mListener.onUniversalEventListener("ExchangeQueryDocs", null);
            }

        });

        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUniversalEventListener("ExchangeReceive", null);
            }
        });

        btnNomenclaturePhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUniversalEventListener("ExchangeNomenclaturePhotos", null);
            }
        });

        btnExchangeWebService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onUniversalEventListener("ExchangeWebService", null);
            }
        });


        // TODO в другом месте сделать видимость закладки
        /*
        MySlider slidingDrawer1 = (MySlider) findViewById(R.id.slidingDrawer1);

        if (g.Common.PRODLIDER || g.Common.TITAN || g.Common.TANDEM || g.Common.ISTART || g.Common.FACTORY) {
            slidingDrawer1.setOnDrawerScrollListener(new MySlider.OnDrawerScrollListener() {

                @Override
                public void onScrollStarted() {
                    // и сейчас не только при смене закладки - у нас также есть слайдер
                    // и это может быть как заказ, так и оплата
                    ActivityCompat.invalidateOptionsMenu(MainActivity.this);
                    try {
                        // из-за бага в версии android 3.2 меню не обновляется
                        onPrepareOptionsMenu(g_options_menu);
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onScrollEnded() {
                }
            });
            slidingDrawer1.setVisibility(View.VISIBLE);
        } else {
            slidingDrawer1.setVisibility(View.GONE);
        }
        */
    }

}
