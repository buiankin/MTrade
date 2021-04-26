package ru.code22.mtrade;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Random;

import androidx.fragment.app.Fragment;

public class PaymentPageFragment extends Fragment {

    static final int SELECT_CLIENT_FROM_PAYMENT_REQUEST = 1;
    static final int SELECT_AGREEMENT_FROM_PAYMENT_REQUEST = 2;
    static final int SELECT_MANAGER_FROM_PAYMENT_REQUEST = 3;
    static final int SELECT_VICARIOUS_POWER_FROM_PAYMENT_REQUEST = 4;

    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";

    int pageNumber;
    int backColor;

    View m_view;

    boolean bHeaderPage=false;
    boolean bSettingsPage=false;

    private void clearVicariousPower(View view0)
    {
        MySingleton g=MySingleton.getInstance();

        g.MyDatabase.m_payment_editing.vicarious_power_id=new MyDatabase.MyID();
        g.MyDatabase.m_payment_editing.vicarious_power_descr=getResources().getString(R.string.vicarious_power_not_set);

        EditText et=(EditText)view0.findViewById(R.id.etPaymentVicariousPower);

        // проверка на всякий случай
        if (et!=null)
        {
            et.setText(g.MyDatabase.m_payment_editing.vicarious_power_descr);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

        MySingleton g=MySingleton.getInstance();

        switch (requestCode)
        {
            case SELECT_CLIENT_FROM_PAYMENT_REQUEST:
            {
                View view0=m_view;
                long _id=data.getLongExtra("id", 0);
                if (someEventListener.onPaymentClientSelected(view0, _id))
                {
                    setModified();
                    clearVicariousPower(view0);
                    boolean bWrongAgreement=true;
                    // Проверим, принадлежит ли договор текущему контрагенту
                    Cursor agreementsCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"owner_id"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.agreement_id.toString()}, null);
                    if (agreementsCursor.moveToNext())
                    {
                        int owner_idIndex = agreementsCursor.getColumnIndex("owner_id");
                        String owner_id = agreementsCursor.getString(owner_idIndex);
                        if (g.MyDatabase.m_payment_editing.client_id.toString().equals(owner_id))
                            bWrongAgreement=false;
                    }
                    agreementsCursor.close();
                    if (bWrongAgreement)
                    {
                        // Не принадлежит, установим договор по умолчанию, если он единственный
                        Cursor defaultAgreementCursor=getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_payment_editing.client_id.toString()}, null);
                        if (defaultAgreementCursor!=null &&defaultAgreementCursor.moveToNext())
                        {
                            int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
                            Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
                            if (!defaultAgreementCursor.moveToNext())
                            {
                                // Есть единственный договор
                                someEventListener.onPaymentAgreementSelected(view0, _idAgreement);
                            } else
                            {
                                // Есть несколько договоров
                                someEventListener.onPaymentAgreementSelected(view0, 0);
                            }
                        } else
                        {
                            // Договора нет
                            someEventListener.onPaymentAgreementSelected(view0, 0);
                        }
                    }
                }
                checkVicariousPowerVisibility(view0);
                break;
            }
            case SELECT_AGREEMENT_FROM_PAYMENT_REQUEST:
            {
                View view0=m_view;
                //EditText et=(EditText)view0.findViewById(R.id.etAgreement);
                //TextView tvOrganization=(TextView)view0.findViewById(R.id.textViewOrdersOrganization);
                long _id=data.getLongExtra("id", 0);
                if (_id!=0)
                {
                    if (someEventListener.onPaymentAgreementSelected(view0, _id))
                    {
                        setModified();
                        clearVicariousPower(view0);
                    }
                } else
                {
                    g.MyDatabase.m_payment_editing.agreement_id=new MyDatabase.MyID(data.getStringExtra("agreement_id"));
                    g.MyDatabase.m_payment_editing.stuff_agreement_name=data.getStringExtra("agreement_descr");
                    g.MyDatabase.m_payment_editing.stuff_organization_name=data.getStringExtra("organization_descr");

                    g.MyDatabase.m_payment_editing.manager_id=new MyDatabase.MyID(data.getStringExtra("manager_id"));
                    g.MyDatabase.m_payment_editing.stuff_manager_name=data.getStringExtra("manager_descr");

                    TextView textViewPaymentOrganization=(TextView)view0.findViewById(R.id.textViewPaymentOrganization);
                    textViewPaymentOrganization.setText(g.MyDatabase.m_payment_editing.stuff_organization_name);

                    EditText etPaymentAgreement=(EditText)view0.findViewById(R.id.etPaymentAgreement);
                    etPaymentAgreement.setText(g.MyDatabase.m_payment_editing.stuff_agreement_name);

                    EditText etPaymentManager=(EditText)view0.findViewById(R.id.etPaymentManager);
                    etPaymentManager.setText(g.MyDatabase.m_payment_editing.stuff_manager_name);

                    double sumDoc=data.getDoubleExtra("sum", -1.0);
                    if (sumDoc>=0.0)
                    {
                        g.MyDatabase.m_payment_editing.sumDoc=sumDoc;
                        EditText editTextPaymentSum=(EditText)view0.findViewById(R.id.editTextPaymentSum);
                        editTextPaymentSum.setText(Math.abs(g.MyDatabase.m_payment_editing.sumDoc)>0.001?Common.DoubleToStringFormat(g.MyDatabase.m_payment_editing.sumDoc, "%.2f"):"");
                    }

                    setModified();
                    clearVicariousPower(view0);
                }
                checkVicariousPowerVisibility(view0);
                break;
            }
            case SELECT_MANAGER_FROM_PAYMENT_REQUEST:
            {
                View view0=m_view;

                setModified();

                EditText et=(EditText)view0.findViewById(R.id.etPaymentManager);

                long _id=data.getLongExtra("id", 0);
                if (_id%2==0)
                {
                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CURATORS_CONTENT_URI, _id/2);
                    Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr"}, null, null, null);
                    if (cursor.moveToNext())
                    {
                        int descrIndex = cursor.getColumnIndex("descr");
                        int idIndex = cursor.getColumnIndex("id");
                        String descr = cursor.getString(descrIndex);
                        String curatorId = cursor.getString(idIndex);
                        et.setText(descr);
                        if (!g.MyDatabase.m_payment_editing.manager_id.toString().equals(curatorId))
                        {
                            clearVicariousPower(view0);
                        }
                        g.MyDatabase.m_payment_editing.manager_id=new MyDatabase.MyID(curatorId);
                        g.MyDatabase.m_payment_editing.stuff_manager_name=descr;
                    } else
                    {
                        g.MyDatabase.m_payment_editing.manager_id=new MyDatabase.MyID();
                        g.MyDatabase.m_payment_editing.stuff_manager_name=getResources().getString(R.string.manager_not_set);
                        et.setText(g.MyDatabase.m_payment_editing.stuff_manager_name);
                        clearVicariousPower(view0);
                    }
                    cursor.close();
                } else
                {
                    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.SALDO_EXTENDED_CONTENT_URI, (_id-1)/2);
                    Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"manager_id", "manager_descr"}, null, null, null);
                    if (cursor.moveToNext())
                    {
                        int descrIndex = cursor.getColumnIndex("manager_descr");
                        int idIndex = cursor.getColumnIndex("manager_id");
                        String descr = cursor.getString(descrIndex);
                        String curatorId = cursor.getString(idIndex);
                        et.setText(descr);
                        if (!g.MyDatabase.m_payment_editing.manager_id.toString().equals(curatorId))
                        {
                            clearVicariousPower(view0);
                        }
                        g.MyDatabase.m_payment_editing.manager_id=new MyDatabase.MyID(curatorId);
                        g.MyDatabase.m_payment_editing.stuff_manager_name=descr;
                    } else
                    {
                        g.MyDatabase.m_payment_editing.manager_id=new MyDatabase.MyID();
                        g.MyDatabase.m_payment_editing.stuff_manager_name=getResources().getString(R.string.manager_not_set);
                        et.setText(g.MyDatabase.m_payment_editing.stuff_manager_name);
                        clearVicariousPower(view0);
                    }
                    cursor.close();
                }
                break;
            }
            case SELECT_VICARIOUS_POWER_FROM_PAYMENT_REQUEST:
            {
                View view0=m_view;

                setModified();

                EditText et=(EditText)view0.findViewById(R.id.etPaymentVicariousPower);

                long _id=data.getLongExtra("id", 0);

                Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.VICARIOUS_POWER_CONTENT_URI, _id);
                Cursor cursor=getActivity().getContentResolver().query(singleUri, new String[]{"id", "descr", "organization_id", "client_id"}, null, null, null);
                if (cursor.moveToNext())
                {
                    int descrIndex = cursor.getColumnIndex("descr");
                    int idIndex = cursor.getColumnIndex("id");
                    int organization_idIndex = cursor.getColumnIndex("organization_id");
                    int client_idIndex = cursor.getColumnIndex("client_id");
                    String descr = cursor.getString(descrIndex);
                    String vicariousPowerId = cursor.getString(idIndex);
                    String organization_id = cursor.getString(organization_idIndex);
                    String client_id = cursor.getString(client_idIndex);
                    // TODO можно было бы и проверить, что клиент и организация совпали
                    et.setText(descr);
                    g.MyDatabase.m_payment_editing.vicarious_power_id=new MyDatabase.MyID(vicariousPowerId);
                    g.MyDatabase.m_payment_editing.vicarious_power_descr=descr;
                } else
                {
                    // вообще это невозможно
                    g.MyDatabase.m_payment_editing.vicarious_power_id=new MyDatabase.MyID();
                    g.MyDatabase.m_payment_editing.vicarious_power_descr=getResources().getString(R.string.vicarious_power_not_set);
                    et.setText(g.MyDatabase.m_payment_editing.vicarious_power_descr);
                }
                cursor.close();

                break;
            }
        }

    }


    private boolean checkVicariousPowerVisibility(View m_view)
    {
        MySingleton g=MySingleton.getInstance();

        TextView tvVicariousPover=(TextView)m_view.findViewById(R.id.tvVicariousPover);
        LinearLayout layoutPaymentVicariousPower=(LinearLayout)m_view.findViewById(R.id.layoutPaymentVicariousPower);

        //EditText etPaymentVicariousPower=(EditText)findViewById(R.id.etPaymentVicariousPower);

        boolean bVisible=false;

        if (g.Common.PRODLIDER||g.Common.TANDEM)
        {
            if (!g.MyDatabase.m_payment_editing.agreement_id.isEmpty())
            {
                //if (!g.MyDatabase.m_payment_editing.organizaion_id.isEmpty())
                bVisible=true;
            }
        }

        if (bVisible)
        {
            tvVicariousPover.setVisibility(View.VISIBLE);
            layoutPaymentVicariousPower.setVisibility(View.VISIBLE);
        } else
        {
            tvVicariousPover.setVisibility(View.GONE);
            layoutPaymentVicariousPower.setVisibility(View.GONE);
        }

        return bVisible;
    }


    protected void setModified()
    {
        MySingleton g=MySingleton.getInstance();
        if (!g.MyDatabase.m_payment_editing_modified)
        {
            getActivity().setTitle(R.string.title_activity_payment_changed);
            g.MyDatabase.m_payment_editing_modified=true;
        }
    }

    public interface onSomePaymentEventListener {
        public void somePaymentEvent(String s);
        public boolean onPaymentClientSelected(View view0, long _id);
        public boolean onPaymentAgreementSelected(View view0, long _id);
        //public boolean orderCanBeSaved(StringBuffer reason);
    }

    PaymentPageFragment.onSomePaymentEventListener someEventListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            someEventListener = (PaymentPageFragment.onSomePaymentEventListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
        }
    }

    static PaymentPageFragment newInstance(int page) {
        PaymentPageFragment pageFragment = new PaymentPageFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        pageFragment.setArguments(arguments);

        return pageFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);

        Random rnd = new Random();
        backColor = Color.argb(40, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        boolean orientationLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        //Globals g=(Globals)getActivity().getApplication();
        MySingleton g = MySingleton.getInstance();

        bHeaderPage = false;
        bSettingsPage = false;

        switch (pageNumber) {
            case 0:
                bHeaderPage = true;
                m_view = inflater.inflate(R.layout.payment_header_fragment, null);
                break;
            case 1:
                bSettingsPage = true;
                m_view = inflater.inflate(R.layout.payment_advanced_fragment, null);
                break;
            default:
                m_view = inflater.inflate(R.layout.page_fragment, null);
                TextView tvPage = (TextView) m_view.findViewById(R.id.tvPage);
                tvPage.setText("Page " + pageNumber);
                tvPage.setBackgroundColor(backColor);
        }

        if (bHeaderPage) {

            TextView tvEmailForCheques=(TextView) m_view.findViewById(R.id.tvEmailForCheques);
            if (g.Common.PRODLIDER) {
                if (g.MyDatabase.m_payment_editing.stuff_email.contains("@"))
                    tvEmailForCheques.setText(g.MyDatabase.m_payment_editing.stuff_email);
                else
                    tvEmailForCheques.setText(R.string.no_email);
                tvEmailForCheques.setVisibility(View.VISIBLE);
            } else
                tvEmailForCheques.setVisibility(View.GONE);

            EditText etPaymentClient = (EditText) m_view.findViewById(R.id.etPaymentClient);
            etPaymentClient.setText(g.MyDatabase.m_payment_editing.stuff_client_name);

            TextView textViewPaymentOrganization = (TextView) m_view.findViewById(R.id.textViewPaymentOrganization);
            textViewPaymentOrganization.setText(g.MyDatabase.m_payment_editing.stuff_organization_name);

            EditText etPaymentAgreement = (EditText) m_view.findViewById(R.id.etPaymentAgreement);
            etPaymentAgreement.setText(g.MyDatabase.m_payment_editing.stuff_agreement_name);

            EditText etPaymentManager = (EditText) m_view.findViewById(R.id.etPaymentManager);
            etPaymentManager.setText(g.MyDatabase.m_payment_editing.stuff_manager_name);

            EditText etPaymentComment = (EditText) m_view.findViewById(R.id.etPaymentComment);
            etPaymentComment.setText(g.MyDatabase.m_payment_editing.comment);
            etPaymentComment.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    MySingleton g = MySingleton.getInstance();
                    if (!g.MyDatabase.m_payment_editing.comment.equals(s.toString())) {
                        setModified();
                        g.MyDatabase.m_payment_editing.comment = s.toString();
                    }
                }
            });

            // Долг
            EditText etDebt = (EditText) m_view.findViewById(R.id.editTextDebt);
            etDebt.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt));
            EditText etDebtPast = (EditText) m_view.findViewById(R.id.editTextDebtPast);
            etDebtPast.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt_past));

            TextView textViewDebtPast30 = (TextView) m_view.findViewById(R.id.textViewDebtPast30);
            EditText etDebtPast30 = (EditText) m_view.findViewById(R.id.editTextDebtPast30);

            if (g.Common.TITAN || g.Common.ISTART) {
                // Долг по договору
                EditText etAgreementDebt = (EditText) m_view.findViewById(R.id.editTextAgreementDebt);
                etAgreementDebt.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt));
                EditText etAgreementDebtPast = (EditText) m_view.findViewById(R.id.editTextAgreementDebtPast);
                etAgreementDebtPast.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt_past));
            }

            //TextView tvVicariousPover=(TextView)findViewById(R.id.tvVicariousPover);
            //LinearLayout layoutPaymentVicariousPower=(LinearLayout)findViewById(R.id.layoutPaymentVicariousPower);

            EditText etPaymentVicariousPower = (EditText) m_view.findViewById(R.id.etPaymentVicariousPower);

            if (g.Common.PRODLIDER || g.Common.TANDEM) {
                etPaymentVicariousPower.setText(g.MyDatabase.m_payment_editing.vicarious_power_descr);
                etDebtPast30.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_debt_past30));
            } else {
                //tvVicariousPover.setVisibility(View.GONE);
                //layoutPaymentVicariousPower.setVisibility(View.GONE);
                textViewDebtPast30.setVisibility(View.GONE);
                etDebtPast30.setVisibility(View.GONE);
            }

            // Долг по договору
            TextView textViewDebtByAgreement = (TextView) m_view.findViewById(R.id.textViewDebtByAgreement);
            TableLayout tableLayoutAgreementDebt = (TableLayout) m_view.findViewById(R.id.tableLayoutAgreementDebt);
            if (g.Common.TITAN || g.Common.ISTART) {
                textViewDebtByAgreement.setVisibility(View.VISIBLE);
                tableLayoutAgreementDebt.setVisibility(View.VISIBLE);
                EditText editTextAgreementDebt = (EditText) m_view.findViewById(R.id.editTextAgreementDebt);
                EditText editTextAgreementDebtPast = (EditText) m_view.findViewById(R.id.editTextAgreementDebtPast);
                editTextAgreementDebt.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt));
                editTextAgreementDebtPast.setText(String.format("%.2f", g.MyDatabase.m_payment_editing.stuff_agreement_debt_past));
            } else {
                textViewDebtByAgreement.setVisibility(View.GONE);
                tableLayoutAgreementDebt.setVisibility(View.GONE);
            }

            checkVicariousPowerVisibility(m_view);

            // Кнопка выбора клиента
            final Button buttonPaymentSelectClient = (Button) m_view.findViewById(R.id.buttonPaymentSelectClient);
            buttonPaymentSelectClient.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MySingleton g = MySingleton.getInstance();
                    Intent intent = new Intent(getActivity(), ClientsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("client_id", g.MyDatabase.m_payment_editing.client_id.toString());
                    // Только у нас и только для платежей
                    if (g.Common.PRODLIDER)
                        intent.putExtra("show_email_for_cheques", true);
                    startActivityForResult(intent, SELECT_CLIENT_FROM_PAYMENT_REQUEST);
                }
            });

            // Кнопка выбора договора
            final Button buttonPaymentSelectAgreement = (Button) m_view.findViewById(R.id.buttonPaymentSelectAgreement);
            buttonPaymentSelectAgreement.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MySingleton g = MySingleton.getInstance();

                    Intent intent;
                    if (g.Common.PRODLIDER || g.Common.TANDEM) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        if (sharedPreferences.getString("payment_agreement_select_style", "DEFAULT").equals("OLDSTYLE")) {
                            intent = new Intent(getActivity(), AgreementsActivity.class);
                        } else {
                            intent = new Intent(getActivity(), AgreementsDebtActivity.class);
                        }
                    } else {
                        intent = new Intent(getActivity(), AgreementsActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("client_id", g.MyDatabase.m_payment_editing.client_id.toString());
                    startActivityForResult(intent, SELECT_AGREEMENT_FROM_PAYMENT_REQUEST);
                }
            });

            // Кнопка выбора менеджера
            final Button buttonPaymentSelectManager = (Button) m_view.findViewById(R.id.buttonPaymentSelectManager);
            buttonPaymentSelectManager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MySingleton g = MySingleton.getInstance();
                    Intent intent = new Intent(getActivity(), CuratorsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("payment", true);
                    intent.putExtra("client_id", g.MyDatabase.m_payment_editing.client_id.toString());
                    startActivityForResult(intent, SELECT_MANAGER_FROM_PAYMENT_REQUEST);
                }
            });

            // Кнопка выбора доверенности
            final Button buttonPaymentSelectVicariousPower = (Button) m_view.findViewById(R.id.buttonPaymentSelectVicariousPower);
            buttonPaymentSelectVicariousPower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MySingleton g = MySingleton.getInstance();

                    boolean bOrganizationSelected = false;
                    String organization_id = "";

                    Cursor cursor = getActivity().getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"organization_id"}, "id=?", new String[]{g.MyDatabase.m_payment_editing.agreement_id.toString()}, null);
                    if (cursor.moveToNext()) {
                        int organizationIdIndex = cursor.getColumnIndex("organization_id");
                        organization_id = cursor.getString(organizationIdIndex);
                        bOrganizationSelected = true;
                    }
                    cursor.close();

                    Intent intent = new Intent(getActivity(), VicariousPowersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("client_id", g.MyDatabase.m_payment_editing.client_id.toString());
                    intent.putExtra("manager_id", g.MyDatabase.m_payment_editing.manager_id.toString());

                    if (bOrganizationSelected) {
                        intent.putExtra("organization_id", organization_id);
                        intent.putExtra("agreement_id", g.MyDatabase.m_payment_editing.agreement_id.toString());
                    }
                    startActivityForResult(intent, SELECT_VICARIOUS_POWER_FROM_PAYMENT_REQUEST);

                }
            });


            EditText editTextPaymentSum = (EditText) m_view.findViewById(R.id.editTextPaymentSum);
            editTextPaymentSum.setText(Math.abs(g.MyDatabase.m_payment_editing.sumDoc) > 0.001 ? Common.DoubleToStringFormat(g.MyDatabase.m_payment_editing.sumDoc, "%.2f") : "");

            editTextPaymentSum.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    MySingleton g = MySingleton.getInstance();
                    try {
                        if (Math.abs(g.MyDatabase.m_payment_editing.sumDoc - Double.parseDouble(s.toString())) > 0.0001) {
                            setModified();
                            g.MyDatabase.m_payment_editing.sumDoc = Math.floor(Double.parseDouble(s.toString()) * 100.0 + 0.00001) / 100.0;
                        }
                    } catch (NumberFormatException e) {
                        setModified();
                        g.MyDatabase.m_payment_editing.sumDoc = 0.0;
                    }
                }
            });


            // Кнопка OK
            final Button buttonOk = (Button) m_view.findViewById(R.id.buttonPaymentOk);
            if (buttonOk != null) {
                buttonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        someEventListener.somePaymentEvent("ok");
                        /*
                        StringBuffer reason=new StringBuffer();
                        if (paymentCanBeSaved(reason))
                        {
                            Intent intent=new Intent();
                            setResult(PAYMENT_RESULT_OK, intent);
                            finish();
                        } else
                        {
                            m_reason_cant_save=reason.toString();
                            removeDialog(IDD_CANT_SAVE);
                            showDialog(IDD_CANT_SAVE);
                        }
                        */
                    }
                });
            }

            // Кнопка Cancel
            final Button buttonClose = (Button) m_view.findViewById(R.id.buttonPaymentClose);
            if (buttonClose != null) {
                buttonClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        someEventListener.somePaymentEvent("close");
                    }
                });
            }

        }

        if (bSettingsPage)
        {
            //m_view = inflater.inflate(R.layout.order_advanced_fragment, null);

            final TextView tvLatitude = (TextView) m_view.findViewById(R.id.textViewPaymentLatitudeValue);
            tvLatitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_payment_editing.latitude));
            final TextView tvLongitude = (TextView) m_view.findViewById(R.id.textViewPaymentLongitudeValue);
            tvLongitude.setText(Double.toString(MySingleton.getInstance().MyDatabase.m_payment_editing.longitude));
            final TextView tvDateCoord = (TextView) m_view.findViewById(R.id.textViewPaymentDateCoordValue);
            tvDateCoord.setText(MySingleton.getInstance().MyDatabase.m_payment_editing.datecoord);

            final CheckBox cbReceiveCoord=(CheckBox)m_view.findViewById(R.id.checkBoxPaymentReceiveCoord);
            cbReceiveCoord.setChecked(MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord==1);
            cbReceiveCoord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setModified();
                    MySingleton.getInstance().MyDatabase.m_payment_editing.accept_coord=isChecked?1:0;
                    someEventListener.somePaymentEvent("coord");
                }
            });
        }


        return m_view;
    }
}

