package ru.code22.mtrade;


// http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
// http://w2davids.wordpress.com/android-sectioned-headers-in-listviews/

// http://stackoverflow.com/questions/6261593/custom-listview-with-date-as-sectionheader-used-custom-simplecursoradapter

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import ru.code22.mtrade.MyDatabase.MyID;
import ru.code22.mtrade.MyDatabase.OrderLineRecord;
import ru.code22.mtrade.MyDatabase.RefundLineRecord;
import ru.code22.mtrade.bean.NomenclatureGroup;
import ru.code22.mtrade.viewbinder.DirectoryNodeBinder;
import ru.code22.mtrade.viewbinder.NomenclatureGroupNodeBinder;

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
import android.preference.PreferenceManager;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.recyclertreeview_lib.TreeNode;
import com.recyclertreeview_lib.TreeViewAdapter;

public class NomenclatureActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {
	
	//static final int QUANTITY_REQUEST = 1;
	//static final int OPEN_IMAGE_REQUEST = 2;
	private ActivityResultLauncher<Intent> openImageActivityResultLauncher;
	private ActivityResultLauncher<Intent> quantityRequestActivityResultLauncher;

	
	static final int NOMENCLATURE_RESULT_DOCUMENT_CHANGED=1;
	
    SimpleCursorAdapter mAdapter;
    MyNomenclatureGroupAdapter mGroupAdapter;
    
    private static final int LOADER_ID = 1;
    
    boolean m_bQuantityChanged;

    static final String[] PROJECTION = new String[] {
    	// Номенклатура
    	"_id", "isFolder", "h_groupDescr", "descr", "nomenclature_id", "quant_1", "quant_k_1", "quant_2", "quant_k_2", "required_sales", "image_file", "image_file_checksum", "nomenclature_color",
    	// Прайс
    	"price", "k", "edIzm",
    	// Наценки по клиентам
    	"priceAdd", "priceProcent",
    	// Остатки
    	"nom_quantity",
    	// Продажи (история)
    	"quantity_saled",
    	// Продажи (в текущем периоде)
    	"nom_quantity_saled_now",
		//
		"quantity7_1", "quantity7_2", "quantity7_3", "quantity7_4",
    	// Дополнительно
    	"zero"};
    
    ListView lvNomenclature;
    //ListView lvNomenclatureGroup;
	RecyclerView rvNomenclatureGroup;
	private TreeViewAdapter adapter;

    Spinner sNomenclatureGroup;

    //class Tree {String _id; String id; String parent_id; String descr; int level;};
    List<String> m_list_groups;
    ArrayList<MyNomenclatureGroupAdapter.Tree> m_list2;
    MyDatabase.MyID m_group_id;
    ArrayList<String> m_group_ids;
    boolean m_b_onlyCurrentLevel;
    
    static boolean m_bInStock=false;
    static boolean m_bPacks=false;
    //static String m_filter="";
    String m_filter;
    static String m_mode="";
    
	long m_copy_order_id; // если объект введен копированием, здесь будет код копируемого
	// это нужно для того, чтобы при восстановлении приложения восстановились данные (если еще не успела произойти запись в базу, при изменении)
	String m_backup_client_id; // с отбором по какому клиенту был создан заказ
	String m_backup_distr_point_id;
    
    

    public class MySimpleCursorAdapter extends SimpleCursorAdapter
    	implements Filterable {
    	
    	
        OnClickListener MyButtonClick=new OnClickListener(){
            @Override
            public void onClick(View v){
            	String id_imageFile=v.getTag().toString();
            	if (!id_imageFile.isEmpty())
            	{
					// если имя файла указано без расширения (так было давно), то добавляем расширение по умолчанию
					String imageFileName=(id_imageFile.indexOf(".")<0)?Common.idToFileName(id_imageFile)+".jpg":id_imageFile;
    	        	File imagesPath=Common.getMyStorageFileDir(NomenclatureActivity.this, "goods");
    	        	/*
    	    		if (android.os.Build.VERSION.SDK_INT< Build.VERSION_CODES.Q&&Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    	    		{
    	    		    // получаем путь к изображениям на SD
    	    		    imagesPath = new File(Environment.getExternalStorageDirectory(), "/mtrade/goods");
    	    		} else
    	    		{
    	    			imagesPath=new File(Environment.getDataDirectory(), "/data/"+ getBaseContext().getPackageName()+"/temp");
    	    		}
    	        	 */
	            	File imageFile=new File(imagesPath, imageFileName);
	            	if (imageFile.exists())
	            	{
						Intent intent=new Intent(NomenclatureActivity.this, ImageActivity.class);
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
	        ImageButton button = (ImageButton) view.findViewById(R.id.ibtnNomenclatureLine);
	        ImageButton buttonGroup = (ImageButton) view.findViewById(R.id.ibtnNomenclatureGroup);
	        
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
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	
    public boolean HandleBundleRefund(Bundle savedInstanceState)
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
		bCreatedInSingleton=g.MyDatabase.m_refund_editing.bCreatedInSingleton;

		g.MyDatabase.m_refund_editing_id=savedInstanceState.getLong("refund_editing_id");
		g.MyDatabase.m_refund_editing_created=savedInstanceState.getBoolean("refund_editing_created");
		g.MyDatabase.m_refund_editing_modified=savedInstanceState.getBoolean("refund_editing_modified");
		
		g.MyDatabase.m_refund_editing=savedInstanceState.getParcelable("refund_editing");
		
	   	//m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
	    //m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
	    //m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
	    //m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");

		return bCreatedInSingleton;
    }
	
	
    public boolean HandleBundleOrder(Bundle savedInstanceState)
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
	    bCreatedInSingleton=g.MyDatabase.m_order_editing.bCreatedInSingleton;
		
		g.MyDatabase.m_order_editing_id=savedInstanceState.getLong("order_editing_id");
		g.MyDatabase.m_order_new_editing_id=savedInstanceState.getLong("order_new_editing_id");
		g.MyDatabase.m_order_editing_created=savedInstanceState.getBoolean("order_editing_created");
		g.MyDatabase.m_order_editing_modified=savedInstanceState.getBoolean("order_editing_modified");
		m_copy_order_id=savedInstanceState.getLong("copy_order_id");
		m_backup_client_id=savedInstanceState.getString("backup_client_id");
		m_backup_distr_point_id=savedInstanceState.getString("backup_distr_point_id");
		
	   	//m_list_accounts_descr = savedInstanceState.getStringArray("list_accounts_descr");
	    //m_list_accounts_id = savedInstanceState.getStringArray("list_accounts_id");
	   	//m_list_prices_descr = savedInstanceState.getStringArray("list_prices_descr");
	    //m_list_prices_id = savedInstanceState.getStringArray("list_prices_id");
	    //m_list_stocks_descr = savedInstanceState.getStringArray("list_stocks_descr");
	    //m_list_stocks_id = savedInstanceState.getStringArray("list_stocks_id");
		
		if (g.MyDatabase.m_order_editing.bCreatedInSingleton)
		{
			Intent intent=new Intent(); // Лишь бы что-нибудь было
			if (g.MyDatabase.m_order_new_editing_id>0)
			{
				Long id1=g.MyDatabase.m_order_new_editing_id;
				Long id2=g.MyDatabase.m_order_editing_id;
				//TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_new_editing_id);
				OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_new_editing_id, new MyID(), new MyID(), false);
				g.MyDatabase.m_order_new_editing_id=id1;
				g.MyDatabase.m_order_editing_id=id2;
				g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
			} else if (g.MyDatabase.m_order_editing_id>0)
			{
				//TextDatabase.ReadOrderBy_Id(getContentResolver(), g.MyDatabase.m_order_editing, g.MyDatabase.m_order_editing_id);
				OrdersHelpers.PrepareData(intent, this, g.MyDatabase.m_order_editing_id, new MyID(), new MyID(),false);
				g.MyDatabase.m_order_editing.bCreatedInSingleton=false;
			} else
			{
				// Документ был создан, но не записан (не было никаких изменений),
				// после этого все было выгружено из памяти
				if (m_copy_order_id>0)
				{
					//
					OrdersHelpers.PrepareData(intent, this, m_copy_order_id, new MyID(m_backup_client_id), new MyID(m_backup_distr_point_id), true);
				} else
				{
					// Создадим новый
					OrdersHelpers.PrepareData(intent, this, 0, new MyID(m_backup_client_id), new MyID(m_backup_distr_point_id), false);
				}
			}
		}
		return bCreatedInSingleton;
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Не требуется, т.к. до этого вызывается onCreate, где все это уже считалось
		//HandleBundle(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		MySingleton g=MySingleton.getInstance();
		outState.putString("MODE", m_mode);
		if (m_mode.equals("REFUND"))
		{
			outState.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
			outState.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
			outState.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
			outState.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
		} else
		{
			outState.putLong("order_editing_id", g.MyDatabase.m_order_editing_id);
			outState.putLong("order_new_editing_id", g.MyDatabase.m_order_new_editing_id);
			outState.putBoolean("order_editing_created", g.MyDatabase.m_order_editing_created);
			outState.putBoolean("order_editing_modified", g.MyDatabase.m_order_editing_modified);
			outState.putLong("copy_order_id", m_copy_order_id);
			outState.putString("backup_client_id", m_backup_client_id);
			outState.putString("backup_distr_point_id", m_backup_distr_point_id);
			
			//outState.putStringArray("list_accounts_descr", m_list_accounts_descr);
			//outState.putStringArray("list_accounts_id", m_list_accounts_id);
			//outState.putStringArray("list_prices_descr", m_list_prices_descr);
			//outState.putStringArray("list_prices_id", m_list_prices_id);
			//outState.putStringArray("list_stocks_descr", m_list_stocks_descr);
			//outState.putStringArray("list_stocks_id", m_list_stocks_id);
		}
		outState.putBoolean("QuantityChanged", m_bQuantityChanged);
	}


	void fillTreeRecursive(List<TreeNode> nodes, List<MyNomenclatureGroupAdapter.Tree> data, String parent_id)
	{
		if (parent_id==null)
		{
			// Сделаем так, что если запрашивается корень, выводим все элементы, для которых нет в массиве родителей
			// (это универсальный способ для случая, если пустой элемент есть в списке или его нет -
			// если он есть, он будет единственным в этой выборке)
			for (MyNomenclatureGroupAdapter.Tree item: data) {
				boolean bFoundParent = false;
				if (MyID.isEmptyString(item.id))
					bFoundParent = true;
				else
					for (MyNomenclatureGroupAdapter.Tree item2 : data) {
						if (item != item2 && item.parent_id.equals(item2.id)) {
							bFoundParent = true;
							break;
						}
					}
				if (!bFoundParent) {
					TreeNode<NomenclatureGroup> treeItem = new TreeNode<>(new NomenclatureGroup(item.descr));
					nodes.add(treeItem);
					// TODO
					//fillTreeRecursive(treeItem, data, item.id);
				}
			}
		}

	}

	private void initData() {
        MySingleton g=MySingleton.getInstance();

		List<TreeNode> nodes = new ArrayList<>();

        String[] projection =
                {
                        "_id",
                        "id",
                        "parent_id",
                        "descr"
                };

        ContentResolver contentResolver=getContentResolver();

        Cursor cursor;
		if (g.Common.PHARAOH)
		{
			cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
		} else
		{
			cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "descr");
		}
		int indexDescr = cursor.getColumnIndex("descr");
		int indexId = cursor.getColumnIndex("id");
		int indexParentId = cursor.getColumnIndex("parent_id");
		int index_Id = cursor.getColumnIndex("_id");


		for (String s : m_list_groups) {
			TreeNode<NomenclatureGroup> app = new TreeNode<>(new NomenclatureGroup(s));
			nodes.add(app);
		}

		/*
		TreeNode<Dir> app = new TreeNode<>(new Dir("app"));
		nodes.add(app);
		app.addChild(
				new TreeNode<>(new Dir("manifests"))
						.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("AndroidManifest.xml")))
		);
		app.addChild(
				new TreeNode<>(new Dir("java")).addChild(
						new TreeNode<>(new Dir("tellh")).addChild(
								new TreeNode<>(new Dir("com")).addChild(
										new TreeNode<>(new Dir("recyclertreeview"))
												.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("Dir")))
												.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("DirectoryNodeBinder")))
												.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("File")))
												.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("FileNodeBinder")))
												.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("TreeViewBinder")))
								)
						)
				)
		);
		TreeNode<Dir> res = new TreeNode<>(new Dir("res"));
		nodes.add(res);
		res.addChild(
				new TreeNode<>(new Dir("layout")).lock() // lock this TreeNode
						.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("activity_main.xml")))
						.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("item_dir.xml")))
						.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("item_file.xml")))
		);
		res.addChild(
				new TreeNode<>(new Dir("mipmap"))
						.addChild(new TreeNode<>(new ru.code22.mtrade.bean.File("ic_launcher.png")))
		);
		 */
		// Это есть только в альбомном варианте
		if (rvNomenclatureGroup!=null) {
			rvNomenclatureGroup.setLayoutManager(new LinearLayoutManager(this));
			//adapter = new TreeViewAdapter(nodes, Arrays.asList(new FileNodeBinder(), new DirectoryNodeBinder()));
			adapter = new TreeViewAdapter(nodes, Arrays.asList(new NomenclatureGroupNodeBinder()));
			// whether collapse child nodes when their parent node was close.
			//        adapter.ifCollapseChildWhileCollapseParent(true);
			adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
				@Override
				public boolean onClick(TreeNode node, RecyclerView.ViewHolder holder) {
					if (!node.isLeaf()) {
						//Update and toggle the node.
						onToggle(!node.isExpand(), holder);
						//                    if (!node.isExpand())
						//                        adapter.collapseBrotherNode(node);
					}
					return false;
				}

				@Override
				public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
					DirectoryNodeBinder.ViewHolder dirViewHolder = (DirectoryNodeBinder.ViewHolder) holder;
					final ImageView ivArrow = dirViewHolder.getIvArrow();
					int rotateDegree = isExpand ? 90 : -90;
					ivArrow.animate().rotationBy(rotateDegree)
							.start();
				}
			});
			rvNomenclatureGroup.setAdapter(adapter);
		}
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		setContentView(R.layout.nomenclature);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	    m_copy_order_id=-1;
	    m_backup_client_id=new MyID().toString();
		
	    m_filter="";
		m_group_id=null;
		m_group_ids=null;
		m_bQuantityChanged=false;
		
		m_b_onlyCurrentLevel=PreferenceManager.getDefaultSharedPreferences(this).getBoolean("nomenclature_only_current_level", false);
		
		String mode=getIntent().getStringExtra("MODE");
		if (mode==null)
		{
			m_mode="";
		}
		else
		{
			m_mode=mode;
		}

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
						int resultCode=result.getResultCode();
						if (resultCode == QuantityActivity.QUANTITY_RESULT_OK) {
							m_bQuantityChanged=true;
							Intent data = result.getData();
							if (data != null) {
								if (m_mode.equals("REFUND"))
								{
									MyDatabase.RefundLineRecord line=new MyDatabase.RefundLineRecord();
									long _id=data.getLongExtra("_id", 0);
									double quantity=data.getDoubleExtra("quantity", 0.0);
									double k=data.getDoubleExtra("k", 1.0);
									String ed=data.getStringExtra("ed");
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
										line.k=k;
										line.ed=ed;
										line.comment_in_line=comment_in_line;

										// Проверим, есть ли эта номенклатура в документе
										boolean bNomenclatureFound=false;
										int line_no=0;
										while (line_no<g.MyDatabase.m_refund_editing.lines.size())
										{
											if (g.MyDatabase.m_refund_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
											{
												if (bNomenclatureFound)
												{
													g.MyDatabase.m_refund_editing.lines.remove(line_no);
													continue;
												}
												else
												{
													g.MyDatabase.m_refund_editing.lines.set(line_no, line);
													bNomenclatureFound=true;

													if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
													{
														// Аналогично OrderActivity
														ContentValues cv=new ContentValues();
														cv.put("client_id", ""); // в данном случае это не важно
														cv.put("quantity", quantity);
														cv.put("quantity_requested", quantity);
														cv.put("k", k);
														cv.put("ed", ed);
														cv.put("comment_in_line", comment_in_line);

														int cnt=getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), line.nomenclature_id.toString()});
													}
												}
											}
											line_no++;
										}

										if (!bNomenclatureFound)
										{
											g.MyDatabase.m_refund_editing.lines.add(line);

											if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
											{
												ContentValues cv=new ContentValues();
												cv.put("nomenclature_id", line.nomenclature_id.toString());
												cv.put("refund_id", String.valueOf(g.MyDatabase.m_refund_editing_id));
												cv.put("client_id", ""); // в данном случае это не важно
												cv.put("quantity", quantity);
												cv.put("quantity_requested", quantity);
												cv.put("k", k);
												cv.put("ed", ed);
												cv.put("comment_in_line", comment_in_line);

												getContentResolver().insert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv);
											}

										}
										//g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
										//setModified();

										// В списке у нас указываются продажи, поэтому строку надо перерисовать
										Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
										getContentResolver().notifyChange(singleUriList, null);

									}

								} else
								{
									//long id=data.getLongExtra("id", 0);
									MyDatabase.OrderLineRecord line=new MyDatabase.OrderLineRecord();
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
									if (nomenclatureCursor!=null&&nomenclatureCursor.moveToNext()) {
										int descrIndex = nomenclatureCursor.getColumnIndex("descr");
										int flagsIndex = nomenclatureCursor.getColumnIndex("flags");
										int idIndex = nomenclatureCursor.getColumnIndex("id");
										int weight_k_1_Index = nomenclatureCursor.getColumnIndex("weight_k_1");
										line.nomenclature_id = new MyID(nomenclatureCursor.getString(idIndex));
										line.stuff_nomenclature = nomenclatureCursor.getString(descrIndex);
										line.stuff_nomenclature_flags = nomenclatureCursor.getInt(flagsIndex);
										line.stuff_weight_k_1 = nomenclatureCursor.getDouble(weight_k_1_Index);
										line.quantity = quantity;
										line.quantity_requested = quantity;
										line.discount = 0.0;
										line.k = k;
										line.ed = ed;
										if (price_k > -0.0001 && price_k < 0.0001) {
											price_k = 1.0;
										}
										if (price_k - k > -0.0001 && price_k - k < 0.0001) {
											line.price = price;
										} else {
											line.price = Math.floor(price * k / price_k * 100.0 + 0.00001) / 100.0;
										}
										line.total = Math.floor(line.price * line.quantity * 100.0 + 0.00001) / 100.0;
										line.shipping_time = shipping_time;
										line.comment_in_line = comment_in_line;
										line.lineno = g.MyDatabase.m_order_editing.getMaxLineno() + 1;

										// Проверим, есть ли эта номенклатура в документе
										boolean bNomenclatureFound = false;
										int line_no = 0;
										while (line_no < g.MyDatabase.m_order_editing.lines.size()) {
											if (g.MyDatabase.m_order_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id)) {
												if (bNomenclatureFound) {
													if (g.Common.NEW_BACKUP_FORMAT) {
														// если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
														// когда m_order_new_editing_id=0
														TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
													}
													g.MyDatabase.m_order_editing.lines.remove(line_no);
													continue;
												} else {
													// Используем старый номер строки
													line.lineno = g.MyDatabase.m_order_editing.lines.get(line_no).lineno;
													g.MyDatabase.m_order_editing.lines.set(line_no, line);
													bNomenclatureFound = true;
													if (g.Common.NEW_BACKUP_FORMAT) {
														if (g.MyDatabase.m_order_new_editing_id == 0) {
															// Что-то поменяли в документе впервые, записываем документ
															g.MyDatabase.m_order_editing.old_id = g.MyDatabase.m_order_editing_id;
															// editing_backup (последний параметр)
															// 0-документ в нормальном состоянии
															// 1-новый документ, записать не успели
															// 2-документ начали редактировать, но не записали и не отменили изменения
															g.MyDatabase.m_order_new_editing_id = TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id == 0 ? 1 : 2);
														} else {
															TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
														}
													}
												}
											}
											line_no++;
										}

										if (!bNomenclatureFound) {
											g.MyDatabase.m_order_editing.lines.add(line);
											if (g.Common.NEW_BACKUP_FORMAT) {
												if (g.MyDatabase.m_order_new_editing_id == 0) {
													// Что-то поменяли в документе впервые, записываем документ
													g.MyDatabase.m_order_editing.old_id = g.MyDatabase.m_order_editing_id;
													// editing_backup (последний параметр)
													// 0-документ в нормальном состоянии
													// 1-новый документ, записать не успели
													// 2-документ начали редактировать, но не записали и не отменили изменения
													g.MyDatabase.m_order_new_editing_id = TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id == 0 ? 1 : 2);
												} else {
													TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
												}
											}

										}

										// В списке у нас указываются продажи, поэтому строку надо перерисовать
										Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
										getContentResolver().notifyChange(singleUriList, null);
									}
								}
							}
						} else
						if (resultCode==QuantityActivity.QUANTITY_RESULT_DELETE_LINE) {
							// Только для формата ISTART так, ну, по крайней мере, пока так
							// в других случаях при вводе нулевого количества просто ничего не происходит
							m_bQuantityChanged = true;
							Intent data = result.getData();
							if (data != null) {
								long _id = data.getLongExtra("_id", 0); // Здесь это только для перерисовки строки
								MyID nomenclature_id = new MyID(data.getStringExtra("id"));

								if (m_mode.equals("REFUND")) {
									int line_no = 0;
									while (line_no < g.MyDatabase.m_refund_editing.lines.size()) {
										RefundLineRecord line = g.MyDatabase.m_refund_editing.lines.get(line_no);
										if (line.nomenclature_id.equals(nomenclature_id)) {
											g.MyDatabase.m_refund_editing.lines.remove(line_no);
										} else {
											line_no++;
										}
									}
								} else {
									int line_no = 0;
									while (line_no < g.MyDatabase.m_order_editing.lines.size()) {
										OrderLineRecord line = g.MyDatabase.m_order_editing.lines.get(line_no);
										if (line.nomenclature_id.equals(nomenclature_id)) {
											if (g.Common.NEW_BACKUP_FORMAT) {
												// если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
												// когда m_order_new_editing_id=0
												TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
											}
											g.MyDatabase.m_order_editing.lines.remove(line_no);
										} else {
											line_no++;
										}
									}
								}
								// В списке у нас указываются продажи, поэтому строку надо перерисовать
								Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
								getContentResolver().notifyChange(singleUriList, null);
							}
						}
					}
				});


	    boolean bCreatedInSingleton=false;
	    boolean bRecreationActivity=false;
	    
	    if (savedInstanceState!=null&&savedInstanceState.containsKey("QuantityChanged"))
	    {
	    	// Чтобы при повороте окна или пересоздании формы не стерся признак 
	    	m_bQuantityChanged=savedInstanceState.getBoolean("QuantityChanged");
	    }	    	
		
		if (m_mode.equals("REFUND"))
		{
		    if (savedInstanceState!=null&&savedInstanceState.containsKey("refund_editing"))
		    {
		    	bRecreationActivity=true; // при повторном создании Activity некоторые действия не надо выполнять
		    	// но Singleton при этом не всегда уничтожался
		    	bCreatedInSingleton=HandleBundleRefund(savedInstanceState);
		    }
		    
		    //if (!bCreatedInSingleton)
		    //{
		    //	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
		    //}
			
			
			ContentValues cv=new ContentValues();
			cv.put("client_id", g.MyDatabase.m_refund_editing.client_id.toString());
		    getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
		} else {
			if (savedInstanceState != null && savedInstanceState.containsKey("order_editing_id")) {
				bRecreationActivity = true; // при повторном создании Activity некоторые действия не надо выполнять
				bCreatedInSingleton = HandleBundleOrder(savedInstanceState);
			}

			//if (!bCreatedInSingleton)
			//{
			//	Toast.makeText(this, "Not in singleton", Toast.LENGTH_LONG).show();
			//}

			// 07.06.2018: окно может быть уничтожено и создано заново
			if (!bRecreationActivity && g.MyDatabase.m_order_editing_created)
			//if (g.MyDatabase.m_order_editing_id==0)
			{
				//
				// это новый документ
				// контрагент в документ попал автоматически из отбора
				if (!g.MyDatabase.m_order_editing.client_id.isEmpty()) {
					// установим договор по умолчанию, если он единственный
					Cursor defaultAgreementCursor = getContentResolver().query(MTradeContentProvider.AGREEMENTS_CONTENT_URI, new String[]{"_id"}, "owner_id=?", new String[]{g.MyDatabase.m_order_editing.client_id.toString()}, null);
					if (defaultAgreementCursor != null && defaultAgreementCursor.moveToNext()) {
						int _idIndex = defaultAgreementCursor.getColumnIndex("_id");
						//int price_type_idIndex = defaultAgreementCursor.getColumnIndex("price_type_id");
						Long _idAgreement = defaultAgreementCursor.getLong(_idIndex);
						if (!defaultAgreementCursor.moveToNext()) {
							// Есть единственный договор
							OrdersHelpers.onAgreementSelected_RestrictedVer(this, null, _idAgreement, null, null);
						}
					}
					defaultAgreementCursor.close();
				}
			}
			Intent intent = getIntent();

			if (intent.hasExtra("copy_order_id")) {
				m_copy_order_id = intent.getLongExtra("copy_order_id", -1L);
			}

			if (intent.hasExtra("backup_client_id")) {
				m_backup_client_id = intent.getStringExtra("backup_client_id");
			}

			if (!bRecreationActivity && intent.getBooleanExtra("recalc_price", false)) {
				OrdersHelpers.recalcPrice(this);
			}

			ContentValues cv = new ContentValues();
			cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());

			OrdersHelpers.fillOrdersHistoryPeriods(this, cv);

			getContentResolver().insert(MTradeContentProvider.CREATE_VIEWS_CONTENT_URI, cv);
		}
		
		NomenclatureActivity ob1=(NomenclatureActivity)getLastCustomNonConfigurationInstance();
		if (ob1!=null)
		{
		    m_filter=ob1.m_filter;
			m_group_id=ob1.m_group_id;
			m_group_ids=ob1.m_group_ids;
		}
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		
		lvNomenclature = (ListView)findViewById(R.id.lvNomenclature);
		lvNomenclature.setEmptyView(findViewById(android.R.id.empty));

		/*
		lvNomenclatureGroup = (ListView)findViewById(R.id.lvNomenclatureGroup);
		if (lvNomenclatureGroup!=null)
		{
			lvNomenclatureGroup.setEmptyView(findViewById(R.id.emptyGroup));
		}
		 */

		sNomenclatureGroup=(Spinner)findViewById(R.id.spinnerGroupNomenclature);
		
		String[] fromColumns = {"h_groupDescr", "descr", "price", "nom_quantity", "zero", "quantity_saled", "nom_quantity_saled_now", "zero", "zero", "zero"};
        int[] toViews = {R.id.tvNomenclatureGroup, R.id.tvNomenclatureLineDescr, R.id.tvNomenclatureLinePrice, R.id.tvNomenclatureLineRests, R.id.tvNomenclatureLineSales, R.id.tvNomenclatureLineSalesHistoryPeriod, R.id.tvNomenclatureLineSalesNowPeriod, R.id.ibtnNomenclatureLine, R.id.ibtnNomenclatureGroup, R.id.tvNomenclatureGroupSales};
        
        if (g.Common.TANDEM) {
            mAdapter = new MySimpleCursorAdapter(this,
                    R.layout.nomenclature_line_item_ta, null,
                    fromColumns, toViews, 0);
        } else {
            mAdapter = new MySimpleCursorAdapter(this,
                    R.layout.nomenclature_line_item, null,
                    fromColumns, toViews, 0);
        }
        
        SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            	
            	MySingleton g=MySingleton.getInstance();
            	
            	int myBackgroundColor=Color.TRANSPARENT;
    			if (g.Common.PRODLIDER||g.Common.TANDEM)
    			{
    				int nomenclature_color_index=cursor.getColumnIndex("nomenclature_color");
    				int color=cursor.getInt(nomenclature_color_index);
    				if (color!=0)
    				{
    					myBackgroundColor=0xFF000000|color;
    					//myBackgroundColor=Color.rgb(0, 130, 0);
    				}
    			}

    			int viewId=view.getId();
        		int isFolder_index=cursor.getColumnIndex("isFolder");
            	if (cursor.getInt(isFolder_index)==1)
            	{
            		// если новые элементы будут добавляться, их надо добавлять сюда
            		if (viewId==R.id.tvNomenclatureGroup||viewId==R.id.ibtnNomenclatureGroup||viewId==R.id.tvNomenclatureGroupSales)
            			((View)view.getParent()).setVisibility(View.VISIBLE);
            		else
            			((View)view.getParent()).setVisibility(View.GONE);
            	} else
            	{
					if (viewId==R.id.tvNomenclatureGroup||viewId==R.id.ibtnNomenclatureGroup||viewId==R.id.tvNomenclatureGroupSales)
						((View) view.getParent()).setVisibility(View.GONE);
            		else
            			((View)view.getParent()).setVisibility(View.VISIBLE);
            	}
            	switch (view.getId())
            	{
            	/*
            	case R.id.tvNomenclatureGroup:
            	{
            		//int group_index=cursor.getColumnIndex(MTradeContentProvider.NOMENCLATURE_GROUP_COLUMN);
            		int group_index=cursor.getColumnIndex("h_groupDescr");            		
            		int descr_index=cursor.getColumnIndex("descr");
            		((TextView)view).setText(cursor.getString(group_index)+"("+cursor.getString(descr_index)+")");
            		return true;
            		//break;
            	}
            	*/
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
            			/*
            			ClientPriceRecord clientPrice=TextDatabase.GetClientPrice(getContentResolver(), g.MyDatabase.m_order_editing.client_id, new MyID(cursor.getString(nomenclatureId_index)));
            			if (clientPrice!=null)
            			{
            				price+=Math.floor(price*clientPrice.priceProcent+0.00001)/100.0+clientPrice.priceAdd;
            			}
            			*/
            			//StringBuilder sb=new StringBuilder(Common.DoubleToStringFormat(price, "%.3f")).append(getResources().getString(R.string.default_currency));
						StringBuilder sb=new StringBuilder(String.format(g.Common.getCurrencyFormatted(NomenclatureActivity.this), Common.DoubleToStringFormat(price, "%.3f")));

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
        			if (g.Common.PHARAOH||m_mode.equals("REFUND"))
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
                                String quant_2_str=cursor.getString(quant_2_index);
	                			StringBuilder sb=new StringBuilder(Long.toString(full_pack_quantity)).append(quant_2_str).append("(*").append(Common.DoubleToStringFormat(k, "%.3f")).append(cursor.getString(quant_1_index)).append(")");
	                			if (base_quantity<-0.0001||base_quantity>0.0001)
	                				sb.append('+').append(Common.DoubleToStringFormat(base_quantity, "%.3f"));
                                //int descr_index = cursor.getColumnIndex("descr");
                                //String descr = cursor.getString(quant_2_index);
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
            	case R.id.tvNomenclatureGroupSales:
				{

					if (true||cursor.getInt(isFolder_index)==1) {
						view.setVisibility(View.VISIBLE);
						// TODO 30.11.2020
						int quantity7_1_index=cursor.getColumnIndex("quantity7_1");
						int quantity7_2_index=cursor.getColumnIndex("quantity7_2");
						int quantity7_3_index=cursor.getColumnIndex("quantity7_3");
						int quantity7_4_index=cursor.getColumnIndex("quantity7_4");

						double quantity7_1=cursor.getDouble(quantity7_1_index);
						double quantity7_2=cursor.getDouble(quantity7_2_index);
						double quantity7_3=cursor.getDouble(quantity7_3_index);
						double quantity7_4=cursor.getDouble(quantity7_4_index);

						TextView tv=(TextView)view;
						if (quantity7_1>0.001||quantity7_2>0.001||quantity7_3>0.001||quantity7_4>0.001) {
							StringBuilder sb = new StringBuilder();
							sb.append(Common.DoubleToStringFormat(quantity7_1, "%.3f"));
							sb.append("/");
							sb.append(Common.DoubleToStringFormat(quantity7_2, "%.3f"));
							sb.append("/");
							sb.append(Common.DoubleToStringFormat(quantity7_3, "%.3f"));
							sb.append("/");
							sb.append(Common.DoubleToStringFormat(quantity7_4, "%.3f"));
							tv.setText(sb.toString());
							tv.setVisibility(View.VISIBLE);
						} else
							tv.setVisibility(View.GONE);
					}
					else
						view.setVisibility(View.GONE);
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
            		
            		if (m_mode.equals("REFUND"))
            		{
	            		for (RefundLineRecord line:g.MyDatabase.m_refund_editing.lines)
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
            		} else
            		{
	            		for (OrderLineRecord line:g.MyDatabase.m_order_editing.lines)
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
            		}
            		TextView tv=(TextView)view;
            		// Желтым цветом выводим количество в данном документе
            		if (quantity_total<-0.0001||quantity_total>0.0001)
            		{
            			if (g.Common.m_app_theme.equals("DARK"))
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
            			if (g.Common.m_app_theme.equals("DARK"))
            			{
                			((TextView)view).setTextColor(Color.GREEN);
            			} else
            			{
                			((TextView)view).setTextColor(getResources().getColor(R.color.MY_GREEN));
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
	                		w1.setBackgroundColor(getResources().getColor(R.color.MY_GREEN)); // зеленый
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
                    //
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
            	default:
            	{
            		View w1=(View)view.getParent();
            		if (cursor.getInt(isFolder_index)==1)
            		{
            			w1.setBackgroundColor(Color.GRAY);
            		} else
            		/*
        			if (Common.PRODLIDER)
        			{
        				int nomenclature_color_index=cursor.getColumnIndex("nomenclature_color");
        				int color=cursor.getInt(nomenclature_color_index);
        				if (color==0)
        				{
        					w1.setBackgroundColor(Color.TRANSPARENT);
        				} else
        				{
        					w1.setBackgroundColor(color);
        				}
        			} else
        			*/
        			{
	        			w1.setBackgroundColor(myBackgroundColor);
            		}
            	}
            	}
            	/*
            	switch (view.getId())
            	{
            	case R.id.client_item_text2:
            		((TextView)view).setText("123");
            		return true;
            	}
            	*/
            	/*
                String name = cursor.getColumnName(columnIndex);
                if ("saldo".equals(name)||"saldo_past".equals(name)) {
                	double val=cursor.getDouble(columnIndex);
                	if (-0.001<val&&val<=0.001)
                		((TextView)view).setText("");
                	else
                		((TextView)view).setText(String.format("%.2f", val));
                    return true;
                }
                */
                return false;
            }
        };
        
        mAdapter.setViewBinder(binder);
        
        lvNomenclature.setAdapter(mAdapter);
        lvNomenclature.setOnItemClickListener(new OnItemClickListener()
        {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,
					long _id) {
				MySingleton g=MySingleton.getInstance();
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
				
				Intent intent=new Intent(NomenclatureActivity.this, QuantityActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    intent.putExtra("_id", _id);
			    intent.putExtra("packs", m_bPacks);
			    if (m_mode.equals("REFUND"))
			    {
			    	
			    } else
			    {
			    	intent.putExtra("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
			    	if (g.Common.FACTORY)
					{
						intent.putExtra("agreement30_id", g.MyDatabase.m_order_editing.agreement30_id.toString());
					}
			    }
				Cursor cursorList=mAdapter.getCursor();
				cursorList.moveToPosition(position);
				int nom_isFolder=cursorList.getColumnIndex("isFolder");
				// количество вводим только для элементов
				if (cursorList.getInt(nom_isFolder)!=1)
				{
					int nom_quantityIndex=cursorList.getColumnIndex("nom_quantity");

					// 25.01.2021 Тандему остатки точно не нужны
					if (g.Common.TANDEM&&m_mode.equals("REFUND")) {

					} else
					{
						intent.putExtra("rest", cursorList.getDouble(nom_quantityIndex));
					}
					intent.putExtra("MODE", m_mode);

					//startActivityForResult(intent, QUANTITY_REQUEST);
					quantityRequestActivityResultLauncher.launch(intent);
				}
				
			}
        });

		LoaderManager.getInstance(this).initLoader(LOADER_ID, null, this);
        
        setNomenclatureSpinnerData(Constants.emptyID);

		mGroupAdapter = new MyNomenclatureGroupAdapter(this);
		mGroupAdapter.setMyListGroups(m_list_groups, m_list2, m_group_id, m_group_ids);
		mGroupAdapter.setOnRedrawListListener(new MyNomenclatureGroupAdapter.RedrawListLisnener() {
			@Override
			public void onRestartLoader() {
				LoaderManager.getInstance(NomenclatureActivity.this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
			}
		});

		rvNomenclatureGroup = (RecyclerView)findViewById(R.id.rvNomenclatureGroup);
		initData();

		/*
		if (lvNomenclatureGroup!=null)
        {
        	lvNomenclatureGroup.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        	lvNomenclatureGroup.setAdapter(mGroupAdapter);
        }
		 */
        
        // устанавливаем обработчик нажатия
        if (sNomenclatureGroup!=null)
        {
        	// 
        	if (m_group_id!=null)
        	{
        		sNomenclatureGroup.setOnItemSelectedListener(null);
        		int i;
    	        for (i=0; i<m_list2.size(); i++)
    	        {
    	        	if (m_group_id.toString().equals(m_list2.get(i).id))
    	        	{
    	        		sNomenclatureGroup.setSelection(i);
    	        	}
    	        }
        	}
        	
	        sNomenclatureGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
	
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
					LoaderManager.getInstance(NomenclatureActivity.this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
					
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
        if (!g.MyDatabase.m_order_editing.stock_id.isEmpty())
        {
        	cv.put("stock_id", g.MyDatabase.m_order_editing.stock_id.toString());
        }
        if (Common.MEGA)
        {
	        if (!g.MyDatabase.m_order_editing.curator_id.isEmpty())
	        {
	        	cv.put("curator_id", g.MyDatabase.m_order_editing.curator_id.toString());
	        }
        } else
        {
	        if (!g.MyDatabase.m_order_editing.client_id.isEmpty())
	        {
	        	cv.put("client_id", g.MyDatabase.m_order_editing.client_id.toString());
	        }
        }
        //getContentResolver().insert(MTradeContentProvider.RESTSV_CONTENT_URI,  cv);
        getContentResolver().insert(MTradeContentProvider.RESTS_SALES_STUFF_CONTENT_URI,  cv);
        getContentResolver().insert(MTradeContentProvider.DISCOUNTS_STUFF_CONTENT_URI,  cv);
        // Тип цены обязателен, пусть даже пустой
        cv.clear();
       	cv.put("price_type_id", g.MyDatabase.m_order_editing.price_type_id.toString());
        getContentResolver().insert(MTradeContentProvider.PRICESV_CONTENT_URI,  cv);
        */
        if (m_mode.equals("REFUND"))
        {
        	// для возвратов не надо ни остатков, ни цен. но нужна история продаж
        	TextDatabase.prepareNomenclatureStuffForRefund(getContentResolver());        	
        } else
        {
        	TextDatabase.prepareNomenclatureStuff(getContentResolver());
        }
        
        // Переключатель "На складе"
		final ToggleButton buttonNomenclatureInStock=(ToggleButton)findViewById(R.id.toggleButtonNomenclatureInStock);
		buttonNomenclatureInStock.setChecked(m_bInStock);
		if (m_mode.equals("REFUND"))
		{
			buttonNomenclatureInStock.setVisibility(View.GONE);
		} else
		{
			buttonNomenclatureInStock.setVisibility(View.VISIBLE);
		}
		buttonNomenclatureInStock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bInStock=((ToggleButton)v).isChecked();
				LoaderManager.getInstance(NomenclatureActivity.this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
			}
		});
		
		// Переключатель "Упаковки"
		final ToggleButton buttonNomenclaturePacks=(ToggleButton)findViewById(R.id.toggleButtonNomenclaturePacks);
		buttonNomenclaturePacks.setChecked(m_bPacks);
		buttonNomenclaturePacks.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bPacks=((ToggleButton)v).isChecked();
				LoaderManager.getInstance(NomenclatureActivity.this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
			}
		});
		
		if (g.Common.PHARAOH)
		{
			buttonNomenclatureInStock.setVisibility(View.GONE);
			buttonNomenclaturePacks.setVisibility(View.GONE);
		}
		
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
			LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nomenclature, menu);
		
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
					LoaderManager.getInstance(NomenclatureActivity.this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
					return false;
				}
			});
	    }
		
		
		
		return true;
	}
	
	private void setNomenclatureSpinnerData(String parent_id)
	{
		MySingleton g=MySingleton.getInstance();
		// TODO здесь можно взять данные из hierarchy table 
        Spinner spinner = (Spinner) findViewById(R.id.spinnerGroupNomenclature);

        m_list_groups = new ArrayList<String>();
        ContentResolver contentResolver=getContentResolver();        
	    String[] projection =
	    {
	    	"_id",
	    	"id",
	    	"parent_id",
	        "descr"
	    };
	    m_list2 = new ArrayList<MyNomenclatureGroupAdapter.Tree>();
	    
	    
	    Cursor cursor;
	    if (g.Common.PHARAOH)
	    {
	    	cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "order_for_sorting, descr");
	    } else
	    {
	    	cursor=contentResolver.query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, projection, "isFolder=1", null, "descr");
	    }
		int indexDescr = cursor.getColumnIndex("descr");
		int indexId = cursor.getColumnIndex("id");
		int indexParentId = cursor.getColumnIndex("parent_id");
		int index_Id = cursor.getColumnIndex("_id");
		MyNomenclatureGroupAdapter.Tree t = new MyNomenclatureGroupAdapter.Tree();
		t.descr=getResources().getString(R.string.catalogue_all);
		t.id=null;
		t.parent_id=null;
		t.level=0;
		m_list2.add(t);
		t = new MyNomenclatureGroupAdapter.Tree();
		t.descr=getResources().getString(R.string.catalogue_node);
		t.id="     0   ";
		t.parent_id="";
		t.level=0;
		m_list2.add(t);
		// Сначала просто заполняем список
		while (cursor.moveToNext())
		{
			t = new MyNomenclatureGroupAdapter.Tree();
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

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_list_groups);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        if (spinner!=null)
        {
        	spinner.setAdapter(dataAdapter);
        }

        cursor.close();
        
        
	}

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
    	MySingleton g=MySingleton.getInstance();
		boolean bInStock=(m_bInStock&&!m_mode.equals("REFUND"));
    	if (m_group_id==null)
    	{
			if (g.Common.TANDEM) {
				// Отличие от общего случая в том, что для групп номенклатуры тоже отбор по остаткам
				return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
						PROJECTION, "isFolder<>3"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), null, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
			} else
    		if (g.Common.PHARAOH)
    		{
		        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
		            PROJECTION, "isFolder<>3"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), null, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
    		} else
    		{
		        return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
			            PROJECTION, "isFolder<>3"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), null, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
    		}
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
    		if (g.Common.PHARAOH)
    		{
    	    	return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
    		            PROJECTION, "isFolder=2 and parent_id=?"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{m_group_id.toString()}, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
    		} else
    		{
    	    	return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
    		            PROJECTION, "isFolder=2 and parent_id=?"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), new String[]{m_group_id.toString()}, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
    		}
        	
    	}
    	// TODO вспомнить, в каких случаях isFolder=3 (иерархия?)
		// (наверное это чтобы просто было любое условие, а потом к нему через and добавлять остальные условия)
		if (g.Common.TANDEM)
		{
			// Отличие от общего случая в том, что для групп номенклатуры тоже отбор по остаткам
			return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
					PROJECTION, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(bInStock?" and nom_quantity>0":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
		} else
		if (g.Common.PHARAOH)
		{
	    	return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
		            PROJECTION, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, order_for_sorting, descr");
		} else
		{
	    	return new CursorLoader(this, MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI,
		            PROJECTION, "isFolder<>3 and parent_id IN ("+sb.toString()+")"+(bInStock?" and (isFolder=1 or nom_quantity>0)":"")+(m_filter.isEmpty()?"":" and descr_lower like '%"+m_filter+"%'"), args2, "ifnull(h.ord_idx, h_parent.ord_idx), isFolder, descr");
		}
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
    	MySingleton g=MySingleton.getInstance();
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
							line.k=k;
							line.ed=ed;
							line.comment_in_line=comment_in_line;
							
							// Проверим, есть ли эта номенклатура в документе
							boolean bNomenclatureFound=false;
							int line_no=0;
							while (line_no<g.MyDatabase.m_refund_editing.lines.size())
							{
								if (g.MyDatabase.m_refund_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
								{
									if (bNomenclatureFound)
									{
										g.MyDatabase.m_refund_editing.lines.remove(line_no);
										continue;
									}
									else
									{
										g.MyDatabase.m_refund_editing.lines.set(line_no, line);
										bNomenclatureFound=true;
										
										if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
										{
											// Аналогично OrderActivity
											ContentValues cv=new ContentValues();
											cv.put("client_id", ""); // в данном случае это не важно
											cv.put("quantity", quantity);
											cv.put("quantity_requested", quantity);
											cv.put("k", k);
											cv.put("ed", ed);
											cv.put("comment_in_line", comment_in_line);
											
											int cnt=getContentResolver().update(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv, "refund_id=? and nomenclature_id=?", new String[]{String.valueOf(g.MyDatabase.m_refund_editing_id), line.nomenclature_id.toString()});
										}
									}
								}
								line_no++;
							}
							
							if (!bNomenclatureFound)
							{
								g.MyDatabase.m_refund_editing.lines.add(line);
								
								if (g.MyDatabase.m_refund_editing.state==E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED)
								{
									ContentValues cv=new ContentValues();
									cv.put("nomenclature_id", line.nomenclature_id.toString());
									cv.put("refund_id", String.valueOf(g.MyDatabase.m_refund_editing_id));
									cv.put("client_id", ""); // в данном случае это не важно
									cv.put("quantity", quantity);
									cv.put("quantity_requested", quantity);
									cv.put("k", k);
									cv.put("ed", ed);
									cv.put("comment_in_line", comment_in_line);
									
									getContentResolver().insert(MTradeContentProvider.REFUNDS_LINES_CONTENT_URI, cv);
								}
								
							}
							//g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
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
							line.lineno=g.MyDatabase.m_order_editing.getMaxLineno()+1;
							
							// Проверим, есть ли эта номенклатура в документе
							boolean bNomenclatureFound=false;
							int line_no=0;
							while (line_no<g.MyDatabase.m_order_editing.lines.size())
							{
								if (g.MyDatabase.m_order_editing.lines.get(line_no).nomenclature_id.equals(line.nomenclature_id))
								{
									if (bNomenclatureFound)
									{
										if (g.Common.NEW_BACKUP_FORMAT)
										{
											// если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
											// когда m_order_new_editing_id=0
											TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
										}
										g.MyDatabase.m_order_editing.lines.remove(line_no);
										continue;
									}
									else
									{
										// Используем старый номер строки
										line.lineno=g.MyDatabase.m_order_editing.lines.get(line_no).lineno;
										g.MyDatabase.m_order_editing.lines.set(line_no, line);
										bNomenclatureFound=true;
										if (g.Common.NEW_BACKUP_FORMAT)
										{
									    	  if (g.MyDatabase.m_order_new_editing_id==0)
									  		  {
									    		  // Что-то поменяли в документе впервые, записываем документ
									    		  g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
									    		  // editing_backup (последний параметр)
									    		  // 0-документ в нормальном состоянии
									    		  // 1-новый документ, записать не успели
									    		  // 2-документ начали редактировать, но не записали и не отменили изменения
									    		  g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
									  		  } else
									  		  {
									  			  TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
									  		  }
										}
									}
								}
								line_no++;
							}
							
							if (!bNomenclatureFound)
							{
								g.MyDatabase.m_order_editing.lines.add(line);
								if (g.Common.NEW_BACKUP_FORMAT)
								{
							    	  if (g.MyDatabase.m_order_new_editing_id==0)
							  		  {
							    		  // Что-то поменяли в документе впервые, записываем документ
							    		  g.MyDatabase.m_order_editing.old_id=g.MyDatabase.m_order_editing_id;
							    		  // editing_backup (последний параметр)
							    		  // 0-документ в нормальном состоянии
							    		  // 1-новый документ, записать не успели
							    		  // 2-документ начали редактировать, но не записали и не отменили изменения
							    		  g.MyDatabase.m_order_new_editing_id=TextDatabase.SaveOrderSQL(getContentResolver(), g.MyDatabase.m_order_editing, 0, g.MyDatabase.m_order_editing_id==0?1:2);
							  		  } else
							  		  {
							  			  TextDatabase.UpdateOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, g.MyDatabase.m_order_editing, line);
							  		  }
								}
								
							}
							//g.MyDatabase.m_linesAdapter.notifyDataSetChanged();
							//setModified();
							
							// В списке у нас указываются продажи, поэтому строку надо перерисовать
						    Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
						    getContentResolver().notifyChange(singleUriList, null);
						    
						}
		      		}
	      		}
      		} else
			if (resultCode==QuantityActivity.QUANTITY_RESULT_DELETE_LINE)
			{
				// Только для формата ISTART так, ну, по крайней мере, пока так
				// в других случаях при вводе нулевого количества просто ничего не происходит
				m_bQuantityChanged=true;
				if (data!=null)
				{
					long _id=data.getLongExtra("_id", 0); // Здесь это только для перерисовки строки
					MyID nomenclature_id = new MyID(data.getStringExtra("id"));

					if (m_mode.equals("REFUND"))
					{
						int line_no=0;
						while (line_no<g.MyDatabase.m_refund_editing.lines.size()) {
							RefundLineRecord line=g.MyDatabase.m_refund_editing.lines.get(line_no);
							if (line.nomenclature_id.equals(nomenclature_id)) {
								g.MyDatabase.m_refund_editing.lines.remove(line_no);
							} else
							{
								line_no++;
							}
						}
					} else
					{
						int line_no=0;
						while (line_no<g.MyDatabase.m_order_editing.lines.size()) {
							OrderLineRecord line=g.MyDatabase.m_order_editing.lines.get(line_no);
							if (line.nomenclature_id.equals(nomenclature_id)) {
								if (g.Common.NEW_BACKUP_FORMAT) {
									// если документ не был изменен и ни разу не сохранился, то эти изменения не будут записаны
									// когда m_order_new_editing_id=0
									TextDatabase.DeleteOrderLine(getContentResolver(), g.MyDatabase.m_order_new_editing_id, line.lineno);
								}
								g.MyDatabase.m_order_editing.lines.remove(line_no);
							} else
							{
								line_no++;
							}
						}
					}
					// В списке у нас указываются продажи, поэтому строку надо перерисовать
					Uri singleUriList = ContentUris.withAppendedId(MTradeContentProvider.NOMENCLATURE_LIST_CONTENT_URI, _id);
					getContentResolver().notifyChange(singleUriList, null);
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
    		MySingleton g=MySingleton.getInstance();
			Intent intent=new Intent();
			if (m_mode.equals("REFUND"))
			{
				// Важно, чтобы эти значения имели приоритет перед теми, что вводились в документе (RefundActivity) до этого
				Bundle bundle=new Bundle();
				bundle.putLong("refund_editing_id", g.MyDatabase.m_refund_editing_id);
				bundle.putBoolean("refund_editing_created", g.MyDatabase.m_refund_editing_created);
				bundle.putBoolean("refund_editing_modified", g.MyDatabase.m_refund_editing_modified);
				bundle.putParcelable("refund_editing", g.MyDatabase.m_refund_editing);
				intent.putExtras(bundle);
			}
			setResult(NOMENCLATURE_RESULT_DOCUMENT_CHANGED, intent);
    	}
    	finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch (item.getItemId())
    	{
    	case R.id.action_settings:
    		Intent intent=new Intent(NomenclatureActivity.this, PrefNomenclatureActivity.class);
    		startActivity(intent);
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
		if (key.equals("nomenclature_only_current_level"))
		{
			m_b_onlyCurrentLevel=prefs.getBoolean("nomenclature_only_current_level", false);
			LoaderManager.getInstance(this).restartLoader(LOADER_ID, null, NomenclatureActivity.this);
		}
	}
	

}
