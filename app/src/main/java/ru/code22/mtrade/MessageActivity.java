package ru.code22.mtrade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import ru.code22.mtrade.MyDatabase.MessageRecord;
import ru.code22.mtrade.MyDatabase.MyID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MessageActivity extends AppCompatActivity {
	
	public static final int IDD_CLOSE_MESSAGE_QUERY = 1;
	public static final int IDD_MESSAGE_CANT_SAVE = 2;
    public static final int IDD_DATE_1 = 3;
    public static final int IDD_DATE_2 = 4;

    //private static final int SELECT_ABONENT_REQUEST = 1;
    //private static final int SELECT_CLIENT_REQUEST = 2;
    //private static final int SELECT_AGREEMENT_REQUEST = 3;
	
	static final int MESSAGE_RESULT_OK=1;
	static final int MESSAGE_RESULT_CANCEL=2;

	private ActivityResultLauncher<Intent> selectAbonentActivityResultLauncher;
	private ActivityResultLauncher<Intent> selectClientActivityResultLauncher;
	private ActivityResultLauncher<Intent> selectAgreementActivityResultLauncher;

	List<String> m_list_message_types;	
	List<Integer> m_list_message_types_idx;
	
	int m_edit_date_idx;
	//boolean m_bImageRecycled;
	
	protected void setModified()
	{
		if (!MySingleton.getInstance().MyDatabase.m_message_editing_modified)
		{
			setTitle(R.string.title_activity_message_changed);
			MySingleton.getInstance().MyDatabase.m_message_editing_modified=true;
		}
	}

    /*
	void recycleDrawable()
	{
		if (!m_bImageRecycled)
		{
			ImageView imageViewMessage=(ImageView)findViewById(R.id.imageViewMessage);
			Drawable toRecycle= imageViewMessage.getDrawable();
			if (toRecycle != null) {
			    ((BitmapDrawable)toRecycle).getBitmap().recycle();
			}
			m_bImageRecycled=true;
			//imageViewMessage.setImageDrawable(R.drawable.ic_launcher);
			imageViewMessage.setImageResource(R.mipmap.ic_launcher);
		}
	}
	*/
	
	protected boolean messageCanBeSaved()
	{
		return true;
	}
	
	private void updateVisibility()
	{
		MySingleton g=MySingleton.getInstance();
        WebView wvMessage=(WebView)findViewById(R.id.wvMessage);
        
        TextView tvMessageFile=(TextView)findViewById(R.id.tvMessageFile);
        
        View tvMessageAbonent=findViewById(R.id.tvMessageAbonent);
        View layoutMessageAbonent=findViewById(R.id.layoutMessageAbonent);
        View tvMessageClient=findViewById(R.id.tvMessageClient);
        View layoutMessageClient=findViewById(R.id.layoutMessageClient);
        View tvMessageAgreement=findViewById(R.id.tvMessageAgreement);
        View layoutMessageAgreement=findViewById(R.id.layoutMessageAgreement);

        View tvMessageDate1=findViewById(R.id.tvMessageLabelDate1);
        View layoutMessageDate1=findViewById(R.id.layoutMessageDate1);
        View tvMessageDate2=findViewById(R.id.tvMessageLabelDate2);
        View layoutMessageDate2=findViewById(R.id.layoutMessageDate2);
        
        View layoutMessageFile=findViewById(R.id.layoutMessageFile);
        
        ImageView imageViewMessage=(ImageView)findViewById(R.id.imageViewMessage);
		
		switch (E_MESSAGE_TYPES.fromInt(g.MyDatabase.m_message_editing.type_idx))
		{
		case E_MESSAGE_TYPE_MESSAGE:
	        tvMessageFile.setVisibility(View.GONE);
	        layoutMessageFile.setVisibility(View.GONE);
	        imageViewMessage.setVisibility(View.GONE);
	        
	        tvMessageAbonent.setVisibility(View.VISIBLE);
	        layoutMessageAbonent.setVisibility(View.VISIBLE);
	        
	        tvMessageClient.setVisibility(View.GONE);
	        layoutMessageClient.setVisibility(View.GONE);
	        tvMessageAgreement.setVisibility(View.GONE);
	        layoutMessageAgreement.setVisibility(View.GONE);

	        tvMessageDate1.setVisibility(View.GONE);
	        layoutMessageDate1.setVisibility(View.GONE);
	        
	        tvMessageDate2.setVisibility(View.GONE);
	        layoutMessageDate2.setVisibility(View.GONE);
			break;
		case E_MESSAGE_TYPE_DEBT:
		case E_MESSAGE_TYPE_SALES:
	        tvMessageFile.setVisibility(View.GONE);
	        layoutMessageFile.setVisibility(View.GONE);
	        imageViewMessage.setVisibility(View.GONE);
			
	        tvMessageAbonent.setVisibility(View.GONE);
	        layoutMessageAbonent.setVisibility(View.GONE);
	        
	        tvMessageClient.setVisibility(View.VISIBLE);
	        layoutMessageClient.setVisibility(View.VISIBLE);
	        tvMessageAgreement.setVisibility(View.VISIBLE);
	        layoutMessageAgreement.setVisibility(View.VISIBLE);

	        tvMessageDate1.setVisibility(View.VISIBLE);
	        layoutMessageDate1.setVisibility(View.VISIBLE);
	        
	        tvMessageDate2.setVisibility(View.VISIBLE);
	        layoutMessageDate2.setVisibility(View.VISIBLE);
			break;
		case E_MESSAGE_TYPE_PHOTO:
	        tvMessageFile.setVisibility(View.GONE);
	        layoutMessageFile.setVisibility(View.GONE);
	        imageViewMessage.setVisibility(View.VISIBLE);
	        
	        tvMessageAbonent.setVisibility(View.GONE);
	        layoutMessageAbonent.setVisibility(View.GONE);
	        
	        tvMessageClient.setVisibility(View.GONE);
	        layoutMessageClient.setVisibility(View.GONE);
	        tvMessageAgreement.setVisibility(View.GONE);
	        layoutMessageAgreement.setVisibility(View.GONE);

	        tvMessageDate1.setVisibility(View.GONE);
	        layoutMessageDate1.setVisibility(View.GONE);
	        
	        tvMessageDate2.setVisibility(View.GONE);
	        layoutMessageDate2.setVisibility(View.GONE);
			break;
		}
		if (g.MyDatabase.m_message_editing.report.isEmpty())
		{
			wvMessage.setVisibility(View.GONE);
		} else
		{
			wvMessage.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}

	public boolean HandleBundle(Bundle savedInstanceState)
	{
		boolean bCreatedInSingleton=false;
		MySingleton g=MySingleton.getInstance();
		if (g.MyDatabase==null)
		{
			g.MyDatabase = new MyDatabase();
			g.MyDatabase.m_order_editing.bCreatedInSingleton=true;
			g.MyDatabase.m_refund_editing.bCreatedInSingleton=true;
			g.MyDatabase.m_distribs_editing.bCreatedInSingleton=true;
			g.MyDatabase.m_payment_editing.bCreatedInSingleton=true;
			g.MyDatabase.m_message_editing.bCreatedInSingleton=true;
		}
		bCreatedInSingleton=g.MyDatabase.m_message_editing.bCreatedInSingleton;

		g.MyDatabase.m_message_editing_id=savedInstanceState.getLong("message_editing_id");
		g.MyDatabase.m_message_editing_modified=savedInstanceState.getBoolean("message_editing_modified");

		g.MyDatabase.m_message_editing=savedInstanceState.getParcelable("message_editing");

		return bCreatedInSingleton;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();

		outState.putLong("message_editing_id", g.MyDatabase.m_message_editing_id);
		outState.putBoolean("message_editing_modified", g.MyDatabase.m_message_editing_modified);

		outState.putParcelable("message_editing", g.MyDatabase.m_message_editing);

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		g.CheckInitImageLoader(this);

		//m_bImageRecycled=true;
		/*
		if (g.Common.m_app_theme!=null&&g.Common.m_app_theme.equals("DARK")){
			//setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.LightAppTheme);s
		}
		*/
		setContentView(R.layout.message);

		selectAbonentActivityResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (result.getResultCode() == AgentsActivity.AGENTS_RESULT_OK) {
							Intent data = result.getData();

							if (data != null) {
								long id=data.getLongExtra("id", 0);
								Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGENTS_CONTENT_URI, id);
								String[] mProjection =
										{
												"descr",
												"id"
										};
								Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
								if (cursor!=null &&cursor.moveToNext())
								{
									int descrIndex = cursor.getColumnIndex("descr");
									int idIndex = cursor.getColumnIndex("id");
									String descr = cursor.getString(descrIndex);
									String abonentId = cursor.getString(idIndex);
									EditText et = (EditText) findViewById(R.id.etMessageAbonent);
									et.setText(descr);
									if ((g.MyDatabase.m_message_editing.acknowledged&4)!=0)
									{
										g.MyDatabase.m_message_editing.receiver_id=new MyID(abonentId);
									} else
									{
										g.MyDatabase.m_message_editing.sender_id=new MyID(abonentId);
									}
									cursor.close();
									setModified();
								}
							}

						}
					}
				});


		selectClientActivityResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (result.getResultCode() == ClientsActivity.CLIENTS_RESULT_OK) {
							Intent data = result.getData();

							if (data != null) {
								long id=data.getLongExtra("id", 0);
								Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, id);
								String[] mProjection =
										{
												"descr",
												"id"
										};
								Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
								if (cursor!=null &&cursor.moveToNext())
								{
									int descrIndex = cursor.getColumnIndex("descr");
									int idIndex = cursor.getColumnIndex("id");
									String descr = cursor.getString(descrIndex);
									String clientId = cursor.getString(idIndex);
									EditText etClient = (EditText) findViewById(R.id.etMessageClient);
									etClient.setText(descr);
									EditText etAgreement = (EditText) findViewById(R.id.etMessageAgreement);
									etAgreement.setText(getResources().getString(R.string.agreement_not_set));

									g.MyDatabase.m_message_editing.client_id=new MyID(clientId);
									g.MyDatabase.m_message_editing.agreement_id=new MyID();
									cursor.close();
									setModified();
								}
							}

						}
					}
				});

		selectAgreementActivityResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (result.getResultCode() == ClientsActivity.CLIENTS_RESULT_OK) {
							Intent data = result.getData();

							if (data != null) {
								long id=data.getLongExtra("id", 0);
								Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS_CONTENT_URI, id);
								String[] mProjection =
										{
												"descr",
												"id"
										};
								Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
								if (cursor!=null &&cursor.moveToNext())
								{
									int descrIndex = cursor.getColumnIndex("descr");
									int idIndex = cursor.getColumnIndex("id");
									String descr = cursor.getString(descrIndex);
									String agreementtId = cursor.getString(idIndex);
									EditText et = (EditText) findViewById(R.id.etMessageAgreement);
									et.setText(descr);
									g.MyDatabase.m_message_editing.agreement_id=new MyID(agreementtId);
									cursor.close();
									setModified();
								}
							}

						}
					}
				});


		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// 21.02.2020
		boolean bCreatedInSingleton=false;
		boolean bRecreationActivity=false;
		if (savedInstanceState!=null&&savedInstanceState.containsKey("message_editing"))
		{
			bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
			// но Singleton при этом не всегда уничтожался
			bCreatedInSingleton=HandleBundle(savedInstanceState);
		}

		if (bCreatedInSingleton)
		{
		} else
		{
			//Intent intent = getIntent();
			// Здесь бы считывались передаваемые параметры
		}

		//recycleDrawable();
		
	    MyID agent_id;
	    if ((g.MyDatabase.m_message_editing.acknowledged&4)!=0)
	    {
	        agent_id=g.MyDatabase.m_message_editing.receiver_id;
	    } else
	    {
	        agent_id=g.MyDatabase.m_message_editing.sender_id;
	    }
	    
		// Агент - название
	    String agentDescr;
	    String clientDescr;
	    String agreementDescr;
        Cursor agentCursor=getContentResolver().query(MTradeContentProvider.AGENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{agent_id.toString()}, null);
        if (agentCursor.moveToNext())
        {
        	int descrIndex = agentCursor.getColumnIndex("descr");
        	agentDescr=agentCursor.getString(descrIndex);
        } else
        if (agent_id.isEmpty())
        {
        	agentDescr=getResources().getString(R.string.agent_not_set);
        } else
        {
        	agentDescr="{"+agent_id.toString()+"}";
        }
        agentCursor.close();
        
        // Клиент - название
        Cursor clientCursor=getContentResolver().query(MTradeContentProvider.CLIENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_message_editing.client_id.toString()}, null);
        if (clientCursor.moveToNext())
        {
        	int descrIndex = clientCursor.getColumnIndex("descr");
        	clientDescr=clientCursor.getString(descrIndex);
        } else
        if (g.MyDatabase.m_message_editing.client_id.isEmpty())
        {
        	clientDescr=getResources().getString(R.string.client_not_set);
        } else
        {
        	clientDescr="{"+g.MyDatabase.m_message_editing.client_id.toString()+"}";
        }
        clientCursor.close();

        // Договор - название 
        Cursor agreementCursor=getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{g.MyDatabase.m_message_editing.agreement_id.toString()}, null);
        if (agreementCursor.moveToNext())
        {
        	int descrIndex = agreementCursor.getColumnIndex("descr");
        	agreementDescr=agreementCursor.getString(descrIndex);
        } else
        if (g.MyDatabase.m_message_editing.agreement_id.isEmpty())
        {
        	agreementDescr=getResources().getString(R.string.agreement_not_set);
        } else
        {
        	agreementDescr="{"+g.MyDatabase.m_message_editing.agreement_id.toString()+"}";
        }
        agreementCursor.close();
        
        // Кнопка выбора абонента 
		final Button buttonMessageSelectAbonent=(Button)findViewById(R.id.buttonMessageSelectAbonent);
		buttonMessageSelectAbonent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MessageActivity.this, AgentsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivityForResult(intent, SELECT_ABONENT_REQUEST);
				selectAbonentActivityResultLauncher.launch(intent);
			}
		});
		
        // Кнопка очистки абонента 
		final Button buttonMessageClearAbonent=(Button)findViewById(R.id.ButtonMessageClearAbonent);
		buttonMessageClearAbonent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MySingleton g=MySingleton.getInstance();
				//Intent intent=new Intent(MessageActivity.this, AgentsActivity.class);
				//startActivityForResult(intent, SELECT_ABONENT_REQUEST);
	    	    Cursor cursor=getContentResolver().query(MTradeContentProvider.AGENTS_CONTENT_URI, new String[]{"descr"}, "id=?", new String[]{Constants.emptyID}, null);
	    	    if (cursor!=null &&cursor.moveToNext())
	    	    {
	    	    	int descrIndex = cursor.getColumnIndex("descr");
	    	    	String descr = cursor.getString(descrIndex);
	    	    	EditText et = (EditText) findViewById(R.id.etMessageAbonent);
	    	    	et.setText(descr);
	    		    if ((g.MyDatabase.m_message_editing.acknowledged&4)!=0)
	    		    {
	    		        g.MyDatabase.m_message_editing.receiver_id=new MyID();
	    		    } else
	    		    {
	    		        g.MyDatabase.m_message_editing.sender_id=new MyID();
	    		    }
	    		    setModified();
	    	    }
			}
		});

        // Кнопка выбора клиента
		final Button buttonMessageSelectClient=(Button)findViewById(R.id.buttonMessageSelectClient);
		buttonMessageSelectClient.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MessageActivity.this, ClientsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivityForResult(intent, SELECT_CLIENT_REQUEST);
				selectClientActivityResultLauncher.launch(intent);
			}
		});
		
		// Кнопка очистки клиента
		final Button buttonClearClient=(Button)findViewById(R.id.ButtonMessageClearClient);
		buttonClearClient.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MySingleton g=MySingleton.getInstance();
		        EditText etMessageClient=(EditText)findViewById(R.id.etMessageClient);
		        etMessageClient.setText(getResources().getString(R.string.client_not_set));
				g.MyDatabase.m_message_editing.client_id=new MyID();
		        EditText etMessageAgreement=(EditText)findViewById(R.id.etMessageAgreement);
		        etMessageAgreement.setText(getResources().getString(R.string.agreement_not_set));
				g.MyDatabase.m_message_editing.agreement_id=new MyID();
				setModified();
			}
		});
		
        // Кнопка выбора договора
		final Button buttonMessageSelectAgreement=(Button)findViewById(R.id.buttonMessageSelectAgreement);
		buttonMessageSelectAgreement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MessageActivity.this, AgreementsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("client_id", MySingleton.getInstance().MyDatabase.m_message_editing.client_id.toString());
				//startActivityForResult(intent, SELECT_AGREEMENT_REQUEST);
				selectAgreementActivityResultLauncher.launch(intent);
			}
		});

		// Кнопка очистки договора
		final Button buttonClearAgreement=(Button)findViewById(R.id.ButtonMessageClearAgreement);
		buttonClearAgreement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        EditText etMessageAgreement=(EditText)findViewById(R.id.etMessageAgreement);
		        etMessageAgreement.setText(getResources().getString(R.string.agreement_not_set));
		        MySingleton.getInstance().MyDatabase.m_message_editing.agreement_id=new MyID();
				setModified();
			}
		});
		
        TextView tvMessageDate=(TextView)findViewById(R.id.tvMessageDate);
    	String d=g.MyDatabase.m_message_editing.datetime;
    	if (d.length()==14)
    	{
    		StringBuilder sb=new StringBuilder();
    		sb.append(d.substring(6,8)).append('.').append(d.substring(4,6)).append('.').append(d.substring(0,4)).append(' ').append(d.substring(8,10)).append(':').append(d.substring(10,12)).append(':').append(d.substring(12,14));
    		d=sb.toString();
    	}
        tvMessageDate.setText(d);
        
        TextView etMessageDate1=(TextView)findViewById(R.id.etMessageDate1);
        TextView etMessageDate2=(TextView)findViewById(R.id.etMessageDate2);

    	String d1=g.MyDatabase.m_message_editing.date1;
    	if (d1.length()==8)
    	{
    		StringBuilder sb=new StringBuilder();
    		sb.append(Common.dateStringAsText(d1));
    		d1=sb.toString();
    	}
    	
    	String d2=g.MyDatabase.m_message_editing.date2;
    	if (d2.length()==8)
    	{
    		StringBuilder sb=new StringBuilder();
    		sb.append(Common.dateStringAsText(d2));
    		d2=sb.toString();
    	}
    	
    	etMessageDate1.setText(d1);
    	etMessageDate2.setText(d2);
    	
        Spinner sMessageType=(Spinner)findViewById(R.id.spinnerMessageType);
        EditText etMessageType=(EditText)findViewById(R.id.etMessageType);
        //if (GetMessageTypeDescr)
        if ((g.MyDatabase.m_message_editing.ver==0)&&((g.MyDatabase.m_message_editing.acknowledged&4)!=0)&&
        		(g.MyDatabase.m_message_editing.type_idx!=E_MESSAGE_TYPES.E_MESSAGE_TYPE_PHOTO.value()))
        {
        	// Это наше сообщение, оно создано и не записано
        	// Можно менять тип
        	sMessageType.setVisibility(View.VISIBLE);
        	etMessageType.setVisibility(View.GONE);
        	
        	m_list_message_types = new ArrayList<String>();
        	m_list_message_types_idx = new ArrayList<Integer>();
        	
        	/*
        	int i;
        	for (i=0; i<E_MESSAGE_TYPES.values().length; i++)
        	{
        		m_list_message_types.add(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.fromInt(i)));
        	}
        	*/
        	
        	m_list_message_types.add(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.E_MESSAGE_TYPE_MESSAGE));
        	m_list_message_types_idx.add(E_MESSAGE_TYPES.E_MESSAGE_TYPE_MESSAGE.value());
        	m_list_message_types.add(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT));
        	m_list_message_types_idx.add(E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value());
        	m_list_message_types.add(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES));
        	m_list_message_types_idx.add(E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value());
        	
        	
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_message_types);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sMessageType.setAdapter(dataAdapter);
            
        	int i;
        	boolean bTypeFound=false;
        	for (i=0; i<m_list_message_types_idx.size(); i++)
        	{
        		if (m_list_message_types_idx.get(i)==g.MyDatabase.m_message_editing.type_idx)
        		{
        			bTypeFound=true;
                    sMessageType.setSelection(i);
        		}
        	}
        	if (!bTypeFound)
        	{
        		// Такого быть не должно - добавим такой тип
            	m_list_message_types.add(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.fromInt(i)));
            	m_list_message_types_idx.add(i);
            	sMessageType.setSelection(i);
        	}
        	
            // устанавливаем обработчик нажатия
            sMessageType.setOnItemSelectedListener(new OnItemSelectedListener() {

    			@Override
    			public void onItemSelected(AdapterView<?> parent, View view,
    			          int position, long id) {
    				MySingleton.getInstance().MyDatabase.m_message_editing.type_idx=m_list_message_types_idx.get(position);
    				updateVisibility();
    			}

    			@Override
    			public void onNothingSelected(AdapterView<?> arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    		});
        	
        } else
        {
        	// Тип сообщения можно просмотреть
        	sMessageType.setVisibility(View.GONE);
        	etMessageType.setVisibility(View.VISIBLE);
        	
        	etMessageType.setText(g.MyDatabase.GetMessageTypeDescr(E_MESSAGE_TYPES.fromInt(g.MyDatabase.m_message_editing.type_idx)));
        }
        
        EditText etMessageAbonent=(EditText)findViewById(R.id.etMessageAbonent);
        etMessageAbonent.setText(agentDescr);
        
        EditText etMessageClient=(EditText)findViewById(R.id.etMessageClient);
        etMessageClient.setText(clientDescr);
        
        EditText etMessageAgreement=(EditText)findViewById(R.id.etMessageAgreement);
        etMessageAgreement.setText(agreementDescr);
        
        EditText etMessageText=(EditText)findViewById(R.id.etMessageText);
        etMessageText.setText(g.MyDatabase.m_message_editing.text);
        
        if (!g.MyDatabase.m_message_editing.report.isEmpty())
        {
        	WebView wvMessage=(WebView)findViewById(R.id.wvMessage);
        	wvMessage.loadData(g.MyDatabase.m_message_editing.report, "text/html; charset=UTF-8", null);
        	
        	wvMessage.getSettings().setBuiltInZoomControls(false);
        	wvMessage.getSettings().setSupportZoom(false);
        	//wvMessage.getSettings().setBuiltInZoomControls(true);
        	//wvMessage.getSettings().setSupportZoom(true);
        	//wvMessage.getSettings().setUseWideViewPort(true);
        	wvMessage.setInitialScale(100);
        }
        
        if (g.MyDatabase.m_message_editing.type_idx==E_MESSAGE_TYPES.E_MESSAGE_TYPE_PHOTO.value())
        {
        	ImageView imageViewMessage=(ImageView)findViewById(R.id.imageViewMessage);
        	//imageViewMessage.setImageURI(Uri.fromFile(new File(g.MyDatabase.m_message_editing.fname)));
        	//m_bImageRecycled=false;
            ImageLoader.getInstance().displayImage(Uri.fromFile(new File(g.MyDatabase.m_message_editing.fname)).toString(), imageViewMessage);
        	if (g.MyDatabase.m_message_editing_id==0)
        	{
        		setModified();
        	}
        	//imageViewMessage.setMinimumHeight(minHeight)
        }
        
        etMessageText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				MySingleton g=MySingleton.getInstance();
				if (!g.MyDatabase.m_message_editing.text.equals(s.toString()))
				{
					setModified();
					g.MyDatabase.m_message_editing.text=s.toString();
				}
			}
		});
        
	    // Кнопка выбора даты 1
		final Button buttonSelectDate1=(Button)findViewById(R.id.btnMessageDate1Edit);
		buttonSelectDate1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_DATE_1);
			}
		});
		
	    // Кнопка выбора даты 2
		final Button buttonSelectDate2=(Button)findViewById(R.id.btnMessageDate2Edit);
		buttonSelectDate2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_DATE_2);
			}
		});
        
        updateVisibility();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}
	
	  @Override
	  protected Dialog onCreateDialog(int id) {
		  MySingleton g=MySingleton.getInstance();
		  MessageRecord rec=g.MyDatabase.m_message_editing;
			switch (id) {
			case IDD_DATE_1:
			{
				int day=0;
				int month=0;
				int year=0;
				if (rec.date1.length()==8)
				{
					day=Integer.parseInt(rec.date1.substring(6,8));
					month=Integer.parseInt(rec.date1.substring(4,6))-1;
					year=Integer.parseInt(rec.date1.substring(0,4));
				}
			    // set date picker as current date
				m_edit_date_idx=0;
				return new DatePickerDialog(this, datePickerListener, 
	                         year, month,day);
			}
			case IDD_DATE_2:
			{
				int day=0;
				int month=0;
				int year=0;
				if (rec.date2.length()==8)
				{
					day=Integer.parseInt(rec.date2.substring(6,8));
					month=Integer.parseInt(rec.date2.substring(4,6))-1;
					year=Integer.parseInt(rec.date2.substring(0,4));
				}
				m_edit_date_idx=1;
			    // set date picker as current date
				return new DatePickerDialog(this, datePickerListener, 
	                         year, month,day);
			}
			case IDD_CLOSE_MESSAGE_QUERY:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setTitle(getResources().getString(R.string.message_modiffied));
				builder.setMessage(getResources().getString(R.string.save_message_before_closing));
				builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						if (messageCanBeSaved())
						{
							Intent intent=new Intent();
							setResult(MESSAGE_RESULT_OK, intent);
							//recycleDrawable();
							finish();
						} else
						{
							dialog.cancel();
							showDialog(IDD_MESSAGE_CANT_SAVE);
						}
					}
				});
				builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						//recycleDrawable();
						finish();
					}
				});
				builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				
				builder.setCancelable(false);
				return builder.create();
			}
			case IDD_MESSAGE_CANT_SAVE:
			{
				AlertDialog.Builder builder=new AlertDialog.Builder(this);
				builder.setMessage(getResources().getString(R.string.message_cant_be_saved));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.cancel();
					}
				});
				builder.setCancelable(false);
				return builder.create();
			}
			}
			return null;
	  }
	  
		@Override
		protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle)
		{
			super.onPrepareDialog(id, dialog);
			MySingleton g=MySingleton.getInstance();
			MessageRecord rec=g.MyDatabase.m_message_editing;
			
			switch (id)
			{
			case IDD_DATE_1:
			{
				int day=0;
				int month=0;
				int year=0;
				if (rec.date1.length()==8)
				{
					day=Integer.parseInt(rec.date1.substring(6,8));
					month=Integer.parseInt(rec.date1.substring(4,6))-1;
					year=Integer.parseInt(rec.date1.substring(0,4));
				}
			    // set date picker as current date
				m_edit_date_idx=0;
				((DatePickerDialog)dialog).updateDate(year, month, day);
				break;
			}
			case IDD_DATE_2:
			{
				int day=0;
				int month=0;
				int year=0;
				if (rec.date2.length()==8)
				{
					day=Integer.parseInt(rec.date2.substring(6,8));
					month=Integer.parseInt(rec.date2.substring(4,6))-1;
					year=Integer.parseInt(rec.date2.substring(0,4));
				}
			    // set date picker as current date
				m_edit_date_idx=1;
				((DatePickerDialog)dialog).updateDate(year, month, day);
				break;
			}
			}
		}
	  
	  
		private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
@Override
//when dialog box is closed, below method will be called.
public void onDateSet(DatePicker view, int selectedYear,
		int selectedMonth, int selectedDay) {
	MySingleton g=MySingleton.getInstance();	
	MessageRecord rec=g.MyDatabase.m_message_editing;
	String date=m_edit_date_idx==0?rec.date1:rec.date2;
	
	int day=0;
	int month=0;
	int year=0;
	
	if (date.length()==8)
	{
		day=Integer.parseInt(date.substring(6,8));
		month=Integer.parseInt(date.substring(4,6))-1;
		year=Integer.parseInt(date.substring(0,4));
	}
	
	if (year!=selectedYear||month!=selectedMonth||day!=selectedDay)
	{
		date=Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));
		if (m_edit_date_idx==0)
		{
			rec.date1=date;
		} else
		{
			rec.date2=date;
		}

		EditText etDate = (EditText)findViewById(m_edit_date_idx==0?R.id.etMessageDate1:R.id.etMessageDate2);
		StringBuilder sb=new StringBuilder();
		sb.append(Common.dateStringAsText(date));
		etDate.setText(sb.toString());
		
		setModified();
	}
	
}
};
	
	private void onCloseActivity()
	{
		if (MySingleton.getInstance().MyDatabase.m_message_editing_modified)
		{
			showDialog(IDD_CLOSE_MESSAGE_QUERY);
		} else
		{
			//recycleDrawable();
			finish();
		}
	}

	/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	MySingleton g=MySingleton.getInstance();
      //String name = data.getStringExtra("name");
      //tvName.setText("Your name is " + name);
    	switch (requestCode)
    	{
    	case SELECT_ABONENT_REQUEST:
    		if (data!=null)
    		{
	    	    long id=data.getLongExtra("id", 0);
	    	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGENTS_CONTENT_URI, id);
	    	    String[] mProjection =
	    	    {
	    	        "descr",
	    	        "id"
	    	    };
	    	    Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
	    	    if (cursor!=null &&cursor.moveToNext())
	    	    {
	    	    	int descrIndex = cursor.getColumnIndex("descr");
	    	    	int idIndex = cursor.getColumnIndex("id");
	    	    	String descr = cursor.getString(descrIndex);
	    	    	String abonentId = cursor.getString(idIndex);
	    	    	EditText et = (EditText) findViewById(R.id.etMessageAbonent);
	    	    	et.setText(descr);
	    		    if ((g.MyDatabase.m_message_editing.acknowledged&4)!=0)
	    		    {
	    		        g.MyDatabase.m_message_editing.receiver_id=new MyID(abonentId);
	    		    } else
	    		    {
	    		        g.MyDatabase.m_message_editing.sender_id=new MyID(abonentId);
	    		    }
	    		    setModified();
	    	    }
    		}
    		break;
		case SELECT_CLIENT_REQUEST:
			if (data!=null)
			{
	    	    long id=data.getLongExtra("id", 0);
	    	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.CLIENTS_CONTENT_URI, id);
	    	    String[] mProjection =
	    	    {
	    	        "descr",
	    	        "id"
	    	    };
	    	    Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
	    	    if (cursor!=null &&cursor.moveToNext())
	    	    {
	    	    	int descrIndex = cursor.getColumnIndex("descr");
	    	    	int idIndex = cursor.getColumnIndex("id");
	    	    	String descr = cursor.getString(descrIndex);
	    	    	String clientId = cursor.getString(idIndex);
	    	    	EditText etClient = (EditText) findViewById(R.id.etMessageClient);
	    	    	etClient.setText(descr);
	    	    	EditText etAgreement = (EditText) findViewById(R.id.etMessageAgreement);
	    	    	etAgreement.setText(getResources().getString(R.string.agreement_not_set));
	    	    	
    		        g.MyDatabase.m_message_editing.client_id=new MyID(clientId);
    		        g.MyDatabase.m_message_editing.agreement_id=new MyID();
    		        
	    	    }
	    	    setModified();
			}
			break;
		case SELECT_AGREEMENT_REQUEST:
			if (data!=null)
			{
	    	    long id=data.getLongExtra("id", 0);
	    	    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.AGREEMENTS_CONTENT_URI, id);
	    	    String[] mProjection =
	    	    {
	    	        "descr",
	    	        "id"
	    	    };
	    	    Cursor cursor=getContentResolver().query(singleUri, mProjection, null, null, null);
	    	    if (cursor!=null &&cursor.moveToNext())
	    	    {
	    	    	int descrIndex = cursor.getColumnIndex("descr");
	    	    	int idIndex = cursor.getColumnIndex("id");
	    	    	String descr = cursor.getString(descrIndex);
	    	    	String agreementtId = cursor.getString(idIndex);
	    	    	EditText et = (EditText) findViewById(R.id.etMessageAgreement);
	    	    	et.setText(descr);
    		        g.MyDatabase.m_message_editing.agreement_id=new MyID(agreementtId);
	    	    }
	    	    setModified();
			}
			break;
			
	    }
    	
    }
    */
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
		  onCloseActivity();
		  return true;
	  }
	  return super.onKeyDown(keyCode, event);
	}
	      
	// Alternative variant for API 5 and higher
	@Override
	public void onBackPressed() {
		onCloseActivity();
	}
	
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId())
		{
		case R.id.action_save:
		{
			if (messageCanBeSaved())
			{
				Intent intent=new Intent();
				setResult(MESSAGE_RESULT_OK, intent);
				finish();
			} else
			{
				// TODO
				//showDialog(IDD_CANT_SAVE);
			}
			break;
		}
		case R.id.action_close:
		{
			Intent intent=new Intent();
			setResult(MESSAGE_RESULT_CANCEL, intent);
			finish();
			break;
		}
		case R.id.action_settings:
			break;
		case R.id.action_backup:
			break;
		}
	  
	  return super.onOptionsItemSelected(item);
	}

}
