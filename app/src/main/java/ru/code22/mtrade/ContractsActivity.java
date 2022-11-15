package ru.code22.mtrade;


// http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
// http://w2davids.wordpress.com/android-sectioned-headers-in-listviews/

// http://stackoverflow.com/questions/6261593/custom-listview-with-date-as-sectionheader-used-custom-simplecursoradapter

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.MyID;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.core.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;

public class ContractsActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {

	private ActivityResultLauncher<Intent> openImageActivityResultLauncher;
	private ActivityResultLauncher<Intent> quantityRequestActivityResultLauncher;


	//static final int QUANTITY_REQUEST = 1;
	//static final int OPEN_IMAGE_REQUEST = 2;
	
	static final int CONTRACTS_RESULT_DOCUMENT_CHANGED=1;
	
    SimpleCursorAdapter mAdapter;
    MyContractsGroupAdapter mGroupAdapter;
    
    private static final int LOADER_ID = 1;
    
    boolean m_bQuantityChanged;

    static final String[] PROJECTION = new String[] {
    	// Номенклатура
    	//"_id", "isFolder", "h_groupDescr", "descr", "nomenclature_id", "quant_1", "quant_k_1", "quant_2", "quant_k_2", "required_sales", "image_file", "image_file_checksum", "nomenclature_color",
    	// Прайс
    	//"price", "k", "edIzm",
    	// Наценки по клиентам
    	//"priceAdd", "priceProcent",
    	// Остатки
    	//"nom_quantity",
    	// Продажи (история)
    	//"quantity_saled",
    	// Продажи (в текущем периоде)
    	//"nom_quantity_saled_now",
    	// Дополнительно
    	"zero"};
    
    ListView lvContracts;
    ListView lvContractsGroup;
    Spinner sContractsGroup;

    class Tree {String _id; String id; String parent_id; String descr; int level;};
    List<String> m_list_groups;
    ArrayList<Tree> m_list2;
    MyDatabase.MyID m_group_id;
    ArrayList<String> m_group_ids;
    boolean m_b_onlyCurrentLevel;
    
    static boolean m_bInStock=false;
    static boolean m_bPacks=false;
    //static String m_filter="";
    String m_filter;
    //static String m_mode="";
    
    // http://startandroid.ru/ru/uroki/vse-uroki-spiskom/113-urok-54-kastomizatsija-spiska-sozdaem-svoj-adapter
    // http://wowjava.wordpress.com/2011/03/26/dynamic-listview-in-android/
    
    // 26.03.2014
    // http://stackoverflow.com/questions/9392511/how-to-handle-oncheckedchangelistener-for-a-radiogroup-in-a-custom-listview-adap
    
    public class MyContractsGroupAdapter extends BaseAdapter {
    	
    	Context context;
    	LayoutInflater inflater;
    	//ArrayList<Product> objects;
    	//RadioButton checkedButton=null;
    	
    	/*
    	class ViewHolder {
    	    TextView t = null;
    	    RadioGroup group;

    	    ViewHolder(View v) {
    	        t = (TextView) v.findViewById(R.id.textView1);
    	        group = (RadioGroup) v.findViewById(R.id.group_me);
    	    }
    	}
    	*/    	
    	
    	/*
    	static class ViewHolder {
    		//protected TextView text;
    		//protected CheckBox checkbox,checkbox1;
    		protected RadioGroup mgroup;
    		}
    	*/    	
    	
    	public MyContractsGroupAdapter(Context context) {
    		this.context=context;
    		inflater = (LayoutInflater) context
    		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
    	
		@Override
		public int getCount() {
			return m_list_groups.size();
		}

		@Override
		public Object getItem(int idx) {
			return m_list_groups.get(idx);
		}

		@Override
		public long getItemId(int idx) {
			return idx;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// используем созданные, но не используемые view
		    View view = convertView;
		    if (view == null) {
		      view = inflater.inflate(R.layout.radio_list_item, parent, false);
		    }

		    //Product p = getProduct(position);

		    // заполняем View в пункте списка данными из товаров: наименование, цена
		    // и картинка
		    //((TextView) view.findViewById(R.id.tvDescr)).setText(p.name);
		    //((TextView) view.findViewById(R.id.tvPrice)).setText(p.price + "");
		    //((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

		    //CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
		    // присваиваем чекбоксу обработчик
		    //cbBuy.setOnCheckedChangeListener(myCheckChangList);
		    // пишем позицию
		    //cbBuy.setTag(position);
		    // заполняем данными из товаров: в корзине или нет
		    //cbBuy.setChecked(p.box);
		    
		    RadioButton rbHierarchy = (RadioButton)view.findViewById(R.id.rbHierarchy);
		    rbHierarchy.setText(m_list_groups.get(position));
		    rbHierarchy.setOnCheckedChangeListener(null);
		    rbHierarchy.setTag(position);
		    
		    String id=m_list2.get(position).id;
		    if (id==null&&m_group_id==null&&position==0)
		    {
		    	rbHierarchy.setChecked(true);
		    	//checkedButton=rbHierarchy;
		    } else
		    if (id!=null&&m_group_id!=null&&id.equals(m_group_id.m_id))
		    {	
		    	rbHierarchy.setChecked(true);
		    } else
		    {
		    	rbHierarchy.setChecked(false);
		    }
		    rbHierarchy.setOnCheckedChangeListener(myCheckChangList);
		    return view;	
		}
		
		// обработчик для чекбоксов
		  OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (!arg1)
					return;
			    // меняем данные товара (в корзине или нет)
			    //getProduct((Integer) buttonView.getTag()).box = isChecked;
				/*
				if (checkedButton!=null&&checkedButton!=(RadioButton)arg0)
				{
					checkedButton.setChecked(false);
				}
				checkedButton=(RadioButton)arg0;
				*/
				RadioButton checkedButton=(RadioButton)arg0;
				int position=(Integer)checkedButton.getTag();
				if (m_list2.get(position).id==null)
				{
					m_group_id=null;
					m_group_ids=null;
				} else
				{
					m_group_id=new MyID(m_list2.get(position).id);
					m_group_ids=new ArrayList<String>();
					int i;
					int level=m_list2.get(position).level;
					m_group_ids.add(m_list2.get(position).id);
					for (i=position+1;i<m_list2.size();i++)
					{
						if (m_list2.get(i).level<=level)
							break;
						m_group_ids.add(m_list2.get(i).id);
					}
				}
				//notifyDataSetInvalidated();
				notifyDataSetChanged();
				LoaderManager.getInstance(ContractsActivity.this).restartLoader(LOADER_ID, null, ContractsActivity.this);
			}
		  };		
    	
    }
    
    public class MySimpleCursorAdapter extends SimpleCursorAdapter
    	implements Filterable {
    	
    	// 21.05.2020: не помню, зачем в контрактах картинки - наверное, это из-за копирования из номенклатуры
        OnClickListener MyButtonClick=new OnClickListener(){
            @Override
            public void onClick(View v){
            	String id_imageFile=v.getTag().toString();
            	if (!id_imageFile.isEmpty())
            	{
					String imageFileName=(id_imageFile.indexOf(".")<0)?Common.idToFileName(id_imageFile)+".jpg":id_imageFile;
    	        	File imagesPath;
    	    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    	    		{
    	    		    // получаем путь к изображениям на SD
    	    		    imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
    	    		} else
    	    		{
    	    			imagesPath=new File(Environment.getDataDirectory(), "/data/"+ getBaseContext().getPackageName()+"/temp");
    	    		}
	            	File imageFile=new File(imagesPath, imageFileName);
	            	if (imageFile.exists())
	            	{
						Intent intent=new Intent(ContractsActivity.this, ImageActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("image_file", imageFile.getAbsolutePath());
						//startActivityForResult(intent, OPEN_IMAGE_REQUEST);
						openImageActivityResultLauncher.launch(intent);
	            	}
            	}
            }
        };
    	
    	
    	// TODO runQueryOnBackgroundThread    	

		public MySimpleCursorAdapter(Context context, int layout, Cursor c, String[] from,
				int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
		}
		
		@Override
	    public void bindView(View view, Context context, Cursor cursor){
			super.bindView(view, context, cursor);
	        ImageButton button = (ImageButton) view.findViewById(R.id.ibtnContractsLine);
	        ImageButton buttonGroup = (ImageButton) view.findViewById(R.id.ibtnContractsGroup);
	        
	        int imageFile_index=cursor.getColumnIndex("image_file");
            button.setTag(cursor.getString(imageFile_index));
            buttonGroup.setTag(cursor.getString(imageFile_index));
	        button.setOnClickListener(MyButtonClick);
	        buttonGroup.setOnClickListener(MyButtonClick);
	    }
		
		// TODO переделать как здесь
		// http://stackoverflow.com/questions/8402099/how-to-set-onclicklitsener-to-button-in-listview-with-override-simplecursoradapt		

		/*
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);
            ImageButton button = (ImageButton) row.findViewById(R.id.ibtnNomenclatureLine);
            button.setTag(cursor.get("image_file"));
	        button.setOnClickListener(new OnClickListener(){
	            @Override
	            public void onClick(View v){
					Intent intent=new Intent(NomenclatureActivity.this, ImageActivity.class);
					
			        //int row_id = cursor.get("_id");  //Your row id (might need to replace)
					
					startActivityForResult(intent, OPEN_IMAGE_REQUEST);
	            }
	        });
            return row;
		}
		*/
		
    }
    
	@Override
	public Object onRetainCustomNonConfigurationInstance() {
		return this;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		setContentView(R.layout.contracts);
		
	    m_filter="";
		m_group_id=null;
		m_group_ids=null;
		m_bQuantityChanged=false;
		
		m_b_onlyCurrentLevel=PreferenceManager.getDefaultSharedPreferences(this).getBoolean("contracts_only_current_level", false);
		
		ContentValues cv=new ContentValues();
		cv.put("client_id", MySingleton.getInstance().MyDatabase.m_distribs_editing.client_id.toString());
	    getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
		
		ContractsActivity ob1=(ContractsActivity)getLastCustomNonConfigurationInstance();
		if (ob1!=null)
		{
		    m_filter=ob1.m_filter;
			m_group_id=ob1.m_group_id;
			m_group_ids=ob1.m_group_ids;
		}
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		openImageActivityResultLauncher  = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (result.getResultCode() == ImageActivity.RESULT_OK) {
							Intent data = result.getData();
							if (data != null) {
								// TODO обработчика не было, наверное, он и не нужен тут
							}
						}
					}
				});

		quantityRequestActivityResultLauncher = registerForActivityResult(
				new ActivityResultContracts.StartActivityForResult(),
				new ActivityResultCallback<ActivityResult>() {
					@Override
					public void onActivityResult(ActivityResult result) {
						if (result.getResultCode() == QuantityActivity.QUANTITY_RESULT_OK) {
							Intent data = result.getData();
							if (data != null) {
								// TODO обработчика не было, наверное, он и не нужен тут
							}
						}
					}
				});



		lvContracts = (ListView)findViewById(R.id.lvContracts);
		lvContracts.setEmptyView(findViewById(android.R.id.empty));
		
		lvContractsGroup = (ListView)findViewById(R.id.lvContractsGroup);
		if (lvContractsGroup!=null)
		{
			lvContractsGroup.setEmptyView(findViewById(R.id.emptyGroup));
		}
		
		sContractsGroup=(Spinner)findViewById(R.id.spinnerGroupContracts);
		
		// TODO
		String[] fromColumns = {};//"h_groupDescr", "descr", "price", "nom_quantity", "zero", "quantity_saled", "nom_quantity_saled_now", "zero", "zero"};
        int[] toViews = {};//{R.id.tvContractsGroup, R.id.tvContractsLineDescr, R.id.tvNomenclatureLinePrice, R.id.tvNomenclatureLineRests, R.id.tvNomenclatureLineSales, R.id.tvNomenclatureLineSalesHistoryPeriod, R.id.tvNomenclatureLineSalesNowPeriod, R.id.ibtnNomenclatureLine, R.id.ibtnNomenclatureGroup};
        
        mGroupAdapter = new MyContractsGroupAdapter(this);

        mAdapter = new MySimpleCursorAdapter(this, 
                R.layout.contracts_line_item, null,
                fromColumns, toViews, 0);
        
        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            	
            	int myBackgroundColor=Color.TRANSPARENT;
    			if (MySingleton.getInstance().Common.PRODLIDER||MySingleton.getInstance().Common.TANDEM)
    			{
    				int nomenclature_color_index=cursor.getColumnIndex("nomenclature_color");
    				int color=cursor.getInt(nomenclature_color_index);
    				if (color!=0)
    				{
    					myBackgroundColor=0xFF000000|color;
    					//myBackgroundColor=Color.rgb(0, 130, 0);
    				}
    			}
    			
        		int isFolder_index=cursor.getColumnIndex("isFolder");
            	if (cursor.getInt(isFolder_index)==1)
            	{
            		if (view.getId()==R.id.tvContractsGroup||view.getId()==R.id.ibtnContractsGroup)
            			((View)view.getParent()).setVisibility(View.VISIBLE);
            		else
            			((View)view.getParent()).setVisibility(View.GONE);
            	} else
            	{
            		if (view.getId()==R.id.tvContractsGroup||view.getId()==R.id.ibtnContractsGroup)
            			((View)view.getParent()).setVisibility(View.GONE);
            		else
            			((View)view.getParent()).setVisibility(View.VISIBLE);
            	}
            	switch (view.getId())
            	{
            	/*
            	case R.id.tvNomenclatureLineDescr:
            	{
                	View w1=(View)view.getParent();
            		w1.setBackgroundColor(myBackgroundColor);
            		break;
            	}
            	case R.id.tvNomenclatureLinePrice:
            	{
                	View w1=(View)view.getParent();
            		w1.setBackgroundColor(myBackgroundColor);
            		// Цена и количество в текущей заявке
            		int nomenclatureId_index=cursor.getColumnIndex("nomenclature_id");
            		int edIzm_index=cursor.getColumnIndex("edIzm");
            		int price_index=cursor.getColumnIndex("price");
            		int price_add_index=cursor.getColumnIndex("priceAdd");
            		int price_procent_index=cursor.getColumnIndex("priceProcent");
            		if (cursor.getString(edIzm_index)==null)
            		{
            			// Цена не указана или не указан тип цены в заказе
                		((TextView)view).setText("-");
            		} else
            		{
            			double price=cursor.getDouble(price_index);
            			double priceAdd=cursor.getDouble(price_add_index);
            			double priceProcent=cursor.getDouble(price_procent_index);
            			//
            			//ClientPriceRecord clientPrice=TextDatabase.GetClientPrice(getContentResolver(), MyDatabase.m_order_editing.client_id, new MyID(cursor.getString(nomenclatureId_index)));
            			//if (clientPrice!=null)
            			//{
            			//	price+=Math.floor(price*clientPrice.priceProcent+0.00001)/100.0+clientPrice.priceAdd;
            			//}
            			
            			StringBuilder sb=new StringBuilder(Common.DoubleToStringFormat(price, "%.3f")).append("р");
            			if (priceProcent!=0.0||priceAdd!=0)
            			{
            				price+=Math.floor(price*priceProcent+0.00001)/100.0+priceAdd;
            				sb.append('(');
            				if (priceProcent!=0.0)
            				{
            					if (priceProcent>0.0)
            						sb.append("+");
            					sb.append(priceProcent).append("%");
            				}
            				if (priceAdd!=0.0)
            				{
            					if (priceAdd>0.0)
            						sb.append("+");
            					sb.append(priceAdd);
            				}
            				sb.append(')');
            			}
            			sb.append('/').append(cursor.getString(edIzm_index));            			
            			((TextView)view).setText(sb.toString());
            		}
            		return true;
            	}
            	case R.id.tvNomenclatureLineRests:
            	{
                	View w1=(View)view.getParent().getParent();
            		w1.setBackgroundColor(myBackgroundColor);
            		int nom_quantity_index=cursor.getColumnIndex("nom_quantity");
        			double quantity=cursor.getDouble(nom_quantity_index);
        			if (Common.PHARAOH||m_mode.equals("REFUND"))
        			{
        				((TextView)view).setText("");
        			} else
            		if (quantity>Constants.MAX_QUANTITY)
            		{
            			((TextView)view).setText("Много");
            		} else
            		{
	            		// Остатки на складе с единицей измерения
	            		if (m_bPacks)
	            		{
	                		int quant_1_index=cursor.getColumnIndex("quant_1");
	                		int quant_2_index=cursor.getColumnIndex("quant_2");
	                		int quant_k_2_index=cursor.getColumnIndex("quant_k_2");
	                		double k=cursor.getDouble(quant_k_2_index);
	                		if (quantity>-0.0001&&quantity<0.0001)
	                		{
	                			((TextView)view).setText("-");
	                		} else
	                		if (k<0.0001||k>=0.99999&&k<=1.000001)
	                		{
	                			// Считаем все равно базовую
	                			String strQuantity=Double.toString(cursor.getDouble(nom_quantity_index))+" "+cursor.getString(quant_1_index);
	    	            		((TextView)view).setText(strQuantity);
	                		} else
	                		{
	                			// Упаковки
	                			// если полная упаковка - пишем, например, так
	                			// 15уп(*5шт)
	                			// если неполная, то
	                			// 15уп(*5шт)+3
	                			long full_pack_quantity=Math.round(quantity/k);
	                			double base_quantity=0;
	                			if (full_pack_quantity*k>quantity+0.0001)
	                			{
	                				// Упаковки не целые, но округлилось в большую сторону
	                				full_pack_quantity--;
	                				base_quantity=quantity-full_pack_quantity*k;
	                			} else
	                			if (full_pack_quantity*k<quantity-0.0001)
	                			{
	                				// Упаковки не целые, округлилось в меньшую сторону
	                				base_quantity=quantity-full_pack_quantity*k;
	                			} else
	                			{
	                				// Упаковки целые
	                				base_quantity=0.0;
	                			}
	                			StringBuilder sb=new StringBuilder(Long.toString(full_pack_quantity)).append(cursor.getString(quant_2_index)).append("(*").append(Common.DoubleToStringFormat(k, "%.3f")).append(cursor.getString(quant_1_index)).append(")");
	                			if (base_quantity<-0.0001||base_quantity>0.0001)
	                				sb.append('+').append(Common.DoubleToStringFormat(base_quantity, "%.3f"));
	    	            		((TextView)view).setText(sb.toString());
	                		}
	            		} else
	            		{
	            			// Базовые
	                		int quant_1_index=cursor.getColumnIndex("quant_1");
	                		//int nom_quantity_index=cursor.getColumnIndex("nom_quantity");
	                		//double quantity=cursor.getDouble(nom_quantity_index);
	                		String strQuantity;
	                		if (quantity<-0.0001||quantity>0.0001)
	                		{
	                			strQuantity=Common.DoubleToStringFormat(quantity, "%.3f")+" "+cursor.getString(quant_1_index);
	                		} else
	                		{
	                			strQuantity="-";
	                		}
		            		((TextView)view).setText(strQuantity);
	            		}
            		}
            		return true;
            	}
            	case R.id.tvNomenclatureLineSales:
            	{
            		View w1=(View)view.getParent().getParent();
            		w1.setBackgroundColor(myBackgroundColor);
            		double quantity_total=0.0;
            		double quantity_k=0.0;
            		String quantity_ed="";
            		//int nomenclature_id_index=cursor.getColumnIndex(MTradeContentProvider.NOMENCLATURE_ID_COLUMN);
            		int nomenclature_id_index=cursor.getColumnIndex("nomenclature_id");
            		int k_index=cursor.getColumnIndex("k");
            		String nomenclatureId=cursor.getString(nomenclature_id_index);
            		for (OrderLineRecord line:MyDatabase.m_order_editing.lines)
            		{
            			if (line.nomenclature_id.toString().equals(nomenclatureId))
            			{
                    		double k=cursor.getDouble(k_index);
            				if (quantity_total>-0.0001&&quantity_total<0.0001)
            				{
            					quantity_k=k;
                				quantity_total=line.quantity;
                				quantity_ed=line.ed;
            				} else
            				{
            					if (quantity_k-k>-0.0001&&quantity_k-k<0.0001)
            					{
            						// единицы совпали
            						quantity_total+=line.quantity;
            					} else
            					{
            						// Единицы разные - переводим в базовые единицы
            						// были не базовые
            						if (quantity_k<0.9999||quantity_k>1.00001)
            						{
                						quantity_total=quantity_total*quantity_k;
                						quantity_k=1.0;
                						if (k>0.9999&&quantity_k<1.00001)
                						{
                							// в этой строке базовые
                							quantity_ed=line.ed;
                						} else
                						{
                							// разные, и базовых среди них нет
                							quantity_ed="";
                						}
            						}
            						quantity_total+=line.quantity*k;
            					}
            				}
            			}
            		}
            		
            		TextView tv=(TextView)view;
            		// Желтым цветом выводим количество в данном документе
            		if (quantity_total<-0.0001||quantity_total>0.0001)
            		{
            			if (Common.m_app_theme.equals("DARK"))
            			{
                			tv.setTextColor(Color.YELLOW);
            			} else
            			{
            				tv.setBackgroundColor(Color.YELLOW);
            			}
            			tv.setText(Common.DoubleToStringFormat(quantity_total, "%.3f")+" "+quantity_ed);
            			tv.setVisibility(View.VISIBLE);
                		return true;
            		}
        			tv.setVisibility(View.GONE);
        			break;
            	}
            	case R.id.tvNomenclatureLineSalesHistoryPeriod:
            	{
            		View w1=(View)view.getParent().getParent();
            		w1.setBackgroundColor(myBackgroundColor);
            		int quantity_saled_index=cursor.getColumnIndex("quantity_saled");
            		if (cursor.getDouble(quantity_saled_index)>0.0)
            		{
            			view.setVisibility(View.VISIBLE);
            			if (Common.m_app_theme.equals("DARK"))
            			{
                			((TextView)view).setTextColor(Color.GREEN);
            			} else
            			{
                			((TextView)view).setTextColor(Color.rgb(0, 130, 0));
            			}
            		} else
            		{
            			view.setVisibility(View.GONE);
            		}
            		break;
            	}
            	case R.id.tvNomenclatureLineSalesNowPeriod:
            	{
            		View w1=(View)view.getParent().getParent();
            		int quantity_saled_index=cursor.getColumnIndex("nom_quantity_saled_now");
            		int required_sales_index=cursor.getColumnIndex("required_sales");
            		double quantity_saled=cursor.getDouble(quantity_saled_index);
            		double required_sales=cursor.getDouble(required_sales_index);
            		
            		if (quantity_saled>0.0||required_sales>0.0)
            		{
            			TextView tv=(TextView)view;
            			if (required_sales<quantity_saled+0.0001)
            			{
            				w1.setBackgroundColor(myBackgroundColor);            				
	            			tv.setTextColor(Color.BLUE);
	            			tv.setText(Common.DoubleToStringFormat(quantity_saled, "%.3f"));
            			} else
            			{
	                		w1.setBackgroundColor(Color.rgb(0, 130, 0)); // зеленый
	            			tv.setTextColor(Color.BLUE);
	            			StringBuilder sb=new StringBuilder(Common.DoubleToStringFormat(quantity_saled, "%.3f"));
	            			sb.append(" из ");
	            			sb.append(Common.DoubleToStringFormat(required_sales, "%.3f"));
	            			tv.setText(sb.toString());
            			}
            			//tv.setText("xxx");
            			view.setVisibility(View.VISIBLE);
            			return true;
            		} else
            		{
            			w1.setBackgroundColor(myBackgroundColor);            			
            			view.setVisibility(View.GONE);
            		}
            		break;
            	}
            	case R.id.ibtnNomenclatureLine:
            	{
            		if (cursor.getInt(isFolder_index)==1)
            		{
            			view.setVisibility(View.GONE);
            		} else
            		{
            			int image_file_index=cursor.getColumnIndex("image_file");
            			if (cursor.getString(image_file_index)!=null&&!cursor.getString(image_file_index).isEmpty())
            			{
            				view.setVisibility(View.VISIBLE);
            			} else
            			{
            				view.setVisibility(View.GONE);
            			}
            		}
            	}
            	break;
            	case R.id.ibtnNomenclatureGroup:
            	{
            		if (cursor.getInt(isFolder_index)==1)
            		{
            			int image_file_index=cursor.getColumnIndex("image_file");
            			if (cursor.getString(image_file_index)!=null&&!cursor.getString(image_file_index).isEmpty())
            			{
            				view.setVisibility(View.VISIBLE);
            			} else
            			{
            				view.setVisibility(View.GONE);
            			}
            		} else
            		{
            			view.setVisibility(View.GONE);
            		}
            	}
            	break;
            	*/
            	default:
            	{
            		View w1=(View)view.getParent();
            		if (cursor.getInt(isFolder_index)==1)
            		{
            			w1.setBackgroundColor(Color.GRAY);
            		} else
        			{
	        			w1.setBackgroundColor(myBackgroundColor);
            		}
            	}
            	}
                return false;
            }
        };
        
        mAdapter.setViewBinder(binder);
        
        lvContracts.setAdapter(mAdapter);
        lvContracts.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long _id) {
				/*
				Intent intent = new Intent();
			    intent.putExtra("_id", id);
			    intent.putExtra("quantity", 1.0);
			    intent.putExtra("k", 1.0);
			    intent.putExtra("ed", "шт.");
			    
			    setResult(RESULT_OK, intent);
			    finish();
			    */
				
				/*
			    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
				Cursor cursor=getContentResolver().query(singleUri, new String[]{"id", "descr"}, null, null, null);
				if (cursor!=null)
				{
					if (cursor.moveToFirst())
					{
						int idIndex=cursor.getColumnIndex("id");
						int descrIndex=cursor.getColumnIndex("descr");
						String id=cursor.getString(idIndex);
						String descr=cursor.getString(descrIndex);
						// проверим, есть ли этот товар уже в документе
						
						
					}
					cursor.close();
				}
				*/
				
				Intent intent=new Intent(ContractsActivity.this, QuantityActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    intent.putExtra("_id", _id);
			    intent.putExtra("packs", m_bPacks);
		    	//intent.putExtra("price_type_id", MyDatabase.m_order_editing.price_type_id.toString());
				Cursor cursorList=mAdapter.getCursor();
				cursorList.moveToPosition(position);
				int nom_isFolder=cursorList.getColumnIndex("isFolder");
				// количество вводим только для элементов
				if (cursorList.getInt(nom_isFolder)!=1)
				{
					int nom_quantityIndex=cursorList.getColumnIndex("nom_quantity");
				    intent.putExtra("rest", cursorList.getDouble(nom_quantityIndex));
					//intent.putExtra("MODE", m_mode);
				    
					//startActivityForResult(intent, QUANTITY_REQUEST);
					quantityRequestActivityResultLauncher.launch(intent);
				}
				
			}
        });

		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        
        setContractsSpinnerData(Constants.emptyID);
        
        if (lvContractsGroup!=null)
        {
        	lvContractsGroup.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        	lvContractsGroup.setAdapter(mGroupAdapter);
        	/*
        	lvNomenclatureGroup.setOnItemClickListener(new OnItemClickListener()
            {
    			@Override
    			public void onItemClick(AdapterView<?> adapter, View view, int position,
    					long _id) {
					if (m_list2.get(position).id==null)
					{
						m_group_id=null;
						m_group_ids=null;
					} else
					{
						m_group_id=new MyID(m_list2.get(position).id);
						m_group_ids=new ArrayList<String>();
						int i;
						int level=m_list2.get(position).level;
						m_group_ids.add(m_list2.get(position).id);
						for (i=position+1;i<m_list2.size();i++)
						{
							if (m_list2.get(i).level<=level)
								break;
							m_group_ids.add(m_list2.get(i).id);
						}
					}
					getSupportLoaderManager().restartLoader(LOADER_ID, null, NomenclatureActivity.this);
					
				}

        	});
        	*/
        	
        }
        
        // устанавливаем обработчик нажатия
        if (sContractsGroup!=null)
        {
        	// 
        	if (m_group_id!=null)
        	{
        		sContractsGroup.setOnItemSelectedListener(null);
        		int i;
    	        for (i=0; i<m_list2.size(); i++)
    	        {
    	        	if (m_group_id.toString().equals(m_list2.get(i).id))
    	        	{
    	        		sContractsGroup.setSelection(i);
    	        	}
    	        }
        	}
        	
	        sContractsGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
				          int position, long id) {
					if (m_list2.get(position).id==null)
					{
						m_group_id=null;
						m_group_ids=null;
					} else
					{
						m_group_id=new MyID(m_list2.get(position).id);
						m_group_ids=new ArrayList<String>();
						int i;
						int level=m_list2.get(position).level;
						m_group_ids.add(m_list2.get(position).id);
						for (i=position+1;i<m_list2.size();i++)
						{
							if (m_list2.get(i).level<=level)
								break;
							m_group_ids.add(m_list2.get(i).id);
						}
					}
					LoaderManager.getInstance(ContractsActivity.this).restartLoader(LOADER_ID, null, ContractsActivity.this);
					
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
	        	
			});
	        
        }
        
        /*
        // Установим склад и тип цены
        //ContentValues cv=new ContentValues();
        cv.clear();
        if (!MyDatabase.m_order_editing.stock_id.isEmpty())
        {
        	cv.put("stock_id", MyDatabase.m_order_editing.stock_id.toString());
        }
        if (Common.MEGA)
        {
	        if (!MyDatabase.m_order_editing.curator_id.isEmpty())
	        {
	        	cv.put("curator_id", MyDatabase.m_order_editing.curator_id.toString());
	        }
        } else
        {
	        if (!MyDatabase.m_order_editing.client_id.isEmpty())
	        {
	        	cv.put("client_id", MyDatabase.m_order_editing.client_id.toString());
	        }
        }
        //getContentResolver().insert(MTradeContentProvider.RESTSV_CONTENT_URI,  cv);
        getContentResolver().insert(MTradeContentProvider.RESTS_SALES_STUFF_CONTENT_URI,  cv);
        getContentResolver().insert(MTradeContentProvider.DISCOUNTS_STUFF_CONTENT_URI,  cv);
        // Тип цены обязателен, пусть даже пустой
        cv.clear();
       	cv.put("price_type_id", MyDatabase.m_order_editing.price_type_id.toString());
        getContentResolver().insert(MTradeContentProvider.PRICESV_CONTENT_URI,  cv);
        */
       	//TextDatabase.prepareNomenclatureStuff(getContentResolver());
        TextDatabase.prepareContractsStuff(getContentResolver());
        
        /*
        // Переключатель "На складе"
		final ToggleButton buttonNomenclatureInStock=(ToggleButton)findViewById(R.id.toggleButtonNomenclatureInStock);
		buttonNomenclatureInStock.setChecked(m_bInStock);
		buttonNomenclatureInStock.setVisibility(View.VISIBLE);
		buttonNomenclatureInStock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bInStock=((ToggleButton)v).isChecked();
				getSupportLoaderManager().restartLoader(LOADER_ID, null, ContractsActivity.this);
			}
		});
		
		// Переключатель "Упаковки"
		final ToggleButton buttonNomenclaturePacks=(ToggleButton)findViewById(R.id.toggleButtonNomenclaturePacks);
		buttonNomenclaturePacks.setChecked(m_bPacks);
		buttonNomenclaturePacks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bPacks=((ToggleButton)v).isChecked();
				getSupportLoaderManager().restartLoader(LOADER_ID, null, ContractsActivity.this);
			}
		});
		
		if (Common.PHARAOH)
		{
			buttonNomenclatureInStock.setVisibility(View.GONE);
			buttonNomenclaturePacks.setVisibility(View.GONE);
		}
		*/
		
	}
	
	@Override
	public boolean onSearchRequested() {
	    return super.onSearchRequested();
	}
	
	@Override
    public void onNewIntent(final Intent queryIntent) {
        super.onNewIntent(queryIntent);
        setIntent(queryIntent);
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction))
        {
        	//Toast.makeText(this, queryIntent.getStringExtra(SearchManager.QUERY), Toast.LENGTH_LONG).show();
        	m_filter=queryIntent.getStringExtra(SearchManager.QUERY);
        	m_filter=m_filter.toLowerCase(Locale.getDefault());
			LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, this);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contracts, menu);
		
		// http://android-developers.blogspot.ru/2013/08/actionbarcompat-and-io-2013-app-source.html
		
		//MenuItem shareItem = menu.findItem(R.id.action_search2);
		//ShareActionProvider mActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);		
		
		// http://habrahabr.ru/post/189680/
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
		// Assumes current activity is the searchable activity
		
	    if (searchView!=null)
	    {
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			//searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				
				@Override
				public boolean onClose() {
		        	m_filter="";
					LoaderManager.getInstance(ContractsActivity.this).restartLoader(LOADER_ID, null, ContractsActivity.this);
					return false;
				}
			});
	    }
		
		
		
		return true;
	}
	
	private void setContractsSpinnerData(String parent_id)
	{
		// TODO здесь можно взять данные из hierarchy table 
        Spinner spinner = (Spinner) findViewById(R.id.spinnerGroupContracts);
        ListView lvHierarchy = (ListView) findViewById(R.id.lvContractsGroup);
        
        m_list_groups = new ArrayList<String>();
        ContentResolver contentResolver=getContentResolver();        
	    String[] projection =
	    {
	    	"_id",
	    	"id",
	    	"parent_id",
	        "descr"
	    };
	    m_list2 = new ArrayList<Tree>(); 
	    
	    
	    Cursor cursor;
    	cursor=contentResolver.query(MTradeContentProvider.DISTRIBS_CONTRACTS_CONTENT_URI, projection, "isFolder=1", null, "descr");
	    if (cursor!=null)
	    {
	    	int indexDescr = cursor.getColumnIndex("descr");
	    	int indexId = cursor.getColumnIndex("id");
	    	int indexParentId = cursor.getColumnIndex("parent_id");
	    	int index_Id = cursor.getColumnIndex("_id");
    		Tree t = new Tree();
    		t.descr=getResources().getString(R.string.catalogue_all);
    		t.id=null;
    		t.parent_id=null;
    		t.level=0;
    		m_list2.add(t);
    		t = new Tree();
    		t.descr=getResources().getString(R.string.catalogue_node);
    		t.id="     0   ";
    		t.parent_id="";
    		t.level=0;
    		m_list2.add(t);
    		// Сначала просто заполняем список
	    	while (cursor.moveToNext())
	    	{
	    		t = new Tree();
		    	t.descr=cursor.getString(indexDescr);
		    	t.id=cursor.getString(indexId);
		    	t.parent_id=cursor.getString(indexParentId);
		    	t._id=cursor.getString(index_Id);
		    	t.level=0;
		    	m_list2.add(t);
	    	}
	    	// А потом сортируем его
	    	int i;
	    	// с единицы потому, что в нуле у нас записан null
	    	for (i=1; i<m_list2.size(); i++)
	    	{
	    		int offset=i+1;
	    		int j;
	    		for (j=i+1; j<m_list2.size(); j++)
	    		{
	    			if (m_list2.get(j).parent_id.equals(m_list2.get(i).id))
	    			{
	    				m_list2.get(j).level=m_list2.get(i).level+1;
	    				//Collections.swap(m_list2, offset, j);
	    				m_list2.add(offset, m_list2.get(j));
	    				m_list2.remove(j+1);
	    				offset++;
	    			}
	    		}
	    	}
	    	for (i=0; i<m_list2.size(); i++)
	    	{
	    		String spaces="";
	    		int j;
	    		/*
	    		for (j=1; j<m_list2.get(i).level; j++)
	    		{
	    			spaces+=" ";
	    		}
	    		if (m_list2.get(i).level>0)
	    			spaces+="> ";
	    		*/
	    		for (j=0; j<m_list2.get(i).level; j++)
	    		{
	    			spaces+=">";
	    		}
	    		if (m_list2.get(i).level>0)
	    			spaces+=" ";
	    		m_list_groups.add(spaces+m_list2.get(i).descr);
	    	}
	    }
        
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner!=null)
        {
        	spinner.setAdapter(dataAdapter);
        }
        
        
	}

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
    	if (m_group_id==null)
    	{
    		// не помню уже, для чего проверка isFolder<>3
	        return new CursorLoader(this, MTradeContentProvider.DISTRIBS_CONTRACTS_LIST_CONTENT_URI,
		            PROJECTION, "isFolder<>3"+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), null, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
    	}
    	StringBuilder sb=new StringBuilder();
    	String[] args2=new String[m_group_ids.size()];
    	int i;
    	for (i=0;i<m_group_ids.size();i++)
    	{
    		if (i!=0)
    			sb.append(',');
    		sb.append('?');
    		args2[i]=m_group_ids.get(i);
    	}
    	if (m_b_onlyCurrentLevel)
    	{
    		// isFolder и т.п. в сортировке, конечно, лишние
	    	return new CursorLoader(this, MTradeContentProvider.DISTRIBS_CONTRACTS_LIST_CONTENT_URI,
		            PROJECTION, "isFolder=2 and parent_id=?"+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{m_group_id.toString()}, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
    	}
    	// TODO вспомнить, в каких случаях isFolder=3 (иерархия?)
    	return new CursorLoader(this, MTradeContentProvider.DISTRIBS_CONTRACTS_LIST_CONTENT_URI,
	            PROJECTION, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    	// A switch-case is useful when dealing with multiple Loaders/IDs
        switch (loader.getId()) {
          case LOADER_ID:
            // The asynchronous load is complete and the data
            // is now available for use. Only now can we associate
            // the queried Cursor with the SimpleCursorAdapter.
        	//data.moveToFirst();
            mAdapter.swapCursor(data);
            break;
        }
        // The listview now displays the queried data.
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

	/*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        //String name = data.getStringExtra("name");
        //tvName.setText("Your name is " + name);
      	switch (requestCode)
      	{
      	case QUANTITY_REQUEST:
      		if (resultCode==QuantityActivity.QUANTITY_RESULT_OK)
      		{
      			m_bQuantityChanged=true;
	      		if (data!=null)
	      		{
		      		if (m_mode.equals("REFUND"))
		      		{
						RefundLineRecord line=new RefundLineRecord();
					    long _id=data.getLongExtra("_id", 0);
					    double quantity=data.getDoubleExtra("quantity", 0.0);
					    double k=data.getDoubleExtra("k", 1.0);
					    String ed=data.getStringExtra("ed");
					    String comment_in_line=data.getStringExtra("comment_in_line");
					    
					    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.DISTRIBS_CONTRACTS_LIST_CONTENT_URI, _id);
						Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
						if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
						{
					    	int descrIndex = nomenclatureCursor.getColumnIndex("descr");
					    	int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
					    	int idIndex = nomenclatureCursor.getColumnIndex("id");
					    	int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
					    	line.nomenclature_id=new MyID(nomenclatureCursor.getString(idIndex));
							line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
							line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
							line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
							line.quantity=quantity;
							line.quantity_requested=quantity;
							line.k=k;
							line.ed=ed;
							line.comment_in_line=comment_in_line;
							
							// Проверим, есть ли эта номенклатура в документе
							boolean bNomenclatureFound=false;
							int line_no=0;
							while (line_no<MyDatabase.m_refund_editing.lines.size())
							{
								if (MyDatabase.m_refund_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
								{
									if (bNomenclatureFound)
									{
										MyDatabase.m_refund_editing.lines.remove(line_no);
										continue;
									}
									else
									{
										MyDatabase.m_refund_editing.lines.set(line_no, line);
										bNomenclatureFound=true;
										
										if (MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
										{
											// Аналогично OrderActivity
											ContentValues cv=new ContentValues();
											cv.put("client_id", ""); // в данном случае это не важно
											cv.put("quantity", quantity);
											cv.put("quantity_requested", quantity);
											cv.put("k", k);
											cv.put("ed", ed);
											cv.put("comment_in_line", comment_in_line);
											
											int cnt=getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(MyDatabase.m_refund_editing_id), line.nomenclature_id.toString()});
										}
									}
								}
								line_no++;
							}
							
							if (!bNomenclatureFound)
							{
								MyDatabase.m_refund_editing.lines.add(line);
								
								if (MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
								{
									ContentValues cv=new ContentValues();
									cv.put("nomenclature_id", line.nomenclature_id.toString());
									cv.put("refund_id", String.valueOf(MyDatabase.m_refund_editing_id));
									cv.put("client_id", ""); // в данном случае это не важно
									cv.put("quantity", quantity);
									cv.put("quantity_requested", quantity);
									cv.put("k", k);
									cv.put("ed", ed);
									cv.put("comment_in_line", comment_in_line);
									
									getContentResolver().insert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv);
								}
								
							}
							//MyDatabase.m_linesAdapter.notifyDataSetChanged();
							//setModified();
							
							// В списке у нас указываются продажи, поэтому строку надо перерисовать
						    Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
						    getContentResolver().notifyChange(singleUriList, null);
						    
						}
		      			
		      		} else
		      		{
		  	    	    //long id=data.getLongExtra("id", 0);
						OrderLineRecord line=new OrderLineRecord();
					    long _id=data.getLongExtra("_id", 0);
					    double quantity=data.getDoubleExtra("quantity", 0.0);
					    double k=data.getDoubleExtra("k", 1.0);
					    double price=data.getDoubleExtra("price", 0.0);
					    double price_k=data.getDoubleExtra("price_k", 1.0);
					    String ed=data.getStringExtra("ed");
					    String shipping_time=data.getStringExtra("shipping_time");
					    String comment_in_line=data.getStringExtra("comment_in_line");
					    
					    Uri singleUri = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, _id);
						Cursor nomenclatureCursor=getContentResolver().query(singleUri, new String[]{"id", "descr", "weight_k_1", "flags"}, null, null, null);
						if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext())
						{
					    	int descrIndex = nomenclatureCursor.getColumnIndex("descr");
					    	int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
					    	int idIndex = nomenclatureCursor.getColumnIndex("id");
					    	int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
					    	line.nomenclature_id=new MyID(nomenclatureCursor.getString(idIndex));
							line.stuff_nomenclature=nomenclatureCursor.getString(descrIndex);
							line.stuff_nomenclature_flags=nomenclatureCursor.getInt(flagsIndex);
							line.stuff_weight_k_1=nomenclatureCursor.getDouble(weight_k_1_Index);
							line.quantity=quantity;
							line.quantity_requested=quantity;
							line.discount=0.0;
							line.k=k;
							line.ed=ed;
							if (price_k>-0.0001&&price_k<0.0001)
							{
								price_k=1.0;
							}
							if (price_k-k>-0.0001&&price_k-k<0.0001)
							{
								line.price=price;
							} else
							{
								line.price=Math.floor(price*k/price_k*100.0+0.00001)/100.0;
							}
							line.total=Math.floor(line.price*line.quantity*100.0+0.00001)/100.0;
							line.shipping_time=shipping_time;
							line.comment_in_line=comment_in_line;
							
							// Проверим, есть ли эта номенклатура в документе
							boolean bNomenclatureFound=false;
							int line_no=0;
							while (line_no<MyDatabase.m_order_editing.lines.size())
							{
								if (MyDatabase.m_order_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
								{
									if (bNomenclatureFound)
									{
										MyDatabase.m_order_editing.lines.remove(line_no);
										continue;
									}
									else
									{
										MyDatabase.m_order_editing.lines.set(line_no, line);
										bNomenclatureFound=true;
										
										if (MyDatabase.m_order_editing.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
										{
											// Аналогично OrderActivity
											ContentValues cv=new ContentValues();
											cv.put("client_id", ""); // в данном случае это не важно
											cv.put("quantity", quantity);
											cv.put("quantity_requested", quantity);
											cv.put("k", k);
											cv.put("ed", ed);
											cv.put("price", price);
											cv.put("shipping_time", shipping_time);
											cv.put("comment_in_line", comment_in_line);
											
											int cnt=getContentResolver().update(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cv, "order_id=? and nomenclature_id=?", new String[]{String.valueOf(MyDatabase.m_order_editing_id), line.nomenclature_id.toString()});
										}
									}
								}
								line_no++;
							}
							
							if (!bNomenclatureFound)
							{
								MyDatabase.m_order_editing.lines.add(line);
								
								if (MyDatabase.m_order_editing.state==E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED)
								{
									ContentValues cv=new ContentValues();
									cv.put("nomenclature_id", line.nomenclature_id.toString());
									cv.put("order_id", String.valueOf(MyDatabase.m_order_editing_id));
									cv.put("client_id", ""); // в данном случае это не важно
									cv.put("quantity", quantity);
									cv.put("quantity_requested", quantity);
									cv.put("k", k);
									cv.put("ed", ed);
									cv.put("price", price);
									cv.put("shipping_time", shipping_time);
									cv.put("comment_in_line", comment_in_line);
									
									getContentResolver().insert(MTradeContentProvider.ORDERS_LINES_CONTENT_URI, cv);
								}
								
							}
							//MyDatabase.m_linesAdapter.notifyDataSetChanged();
							//setModified();
							
							// В списке у нас указываются продажи, поэтому строку надо перерисовать
						    Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
						    getContentResolver().notifyChange(singleUriList, null);
						    
						}
		      		}
	      		}
      		}
      		break;
      	}
    }
	*/
    
    private void onCloseActivity()
    {
    	if (m_bQuantityChanged)
    	{
			Intent intent=new Intent();
			setResult(CONTRACTS_RESULT_DOCUMENT_CHANGED, intent);
    	}
    	finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId())
    	{
    	case R.id.action_settings:
    		// TODO
    		//Intent intent=new Intent(ContractsActivity.this, PrefContractsActivity.class);
    		//startActivity(intent);
    		break;
    	}
      
      return super.onOptionsItemSelected(item);
    }
    
    
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
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("contracts_only_current_level"))
		{
			m_b_onlyCurrentLevel=prefs.getBoolean("contracts_only_current_level", false);
			LoaderManager.getInstance(ContractsActivity.this).restartLoader(LOADER_ID, null, ContractsActivity.this);
		}
	}
	

}
