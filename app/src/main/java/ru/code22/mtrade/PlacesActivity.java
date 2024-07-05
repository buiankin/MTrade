package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import ru.code22.mtrade.MyDatabase.OrderPlaceRecord;
import ru.code22.mtrade.preferences.DatePreference;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class PlacesActivity extends AppCompatActivity {
	
	static final int PLACES_RESULT_OK=1;
	static final int PLACES_RESULT_CANCEL=2;
	
	private static final int IDD_DATE = 1;
	private static final int IDD_TIME = 2;
	
	private ListView mainListView ;
	//private Place[] places ;
	private ArrayAdapter<Place> listAdapter ;
    Map<String, Place> placesIndex; 
    static ArrayList<Place> placesList;
    
    static String order_time;
    static String order_date;
    static String places;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		
		setContentView(R.layout.places);
		
		order_time=g.MyDatabase.m_order_editing.shipping_begin_time;
		order_date=g.MyDatabase.m_order_editing.shipping_date;
		places=g.MyDatabase.m_order_editing.stuff_places;
		
		EditText etPlacesTime = (EditText)findViewById(R.id.etPlacesTime);
		if (g.MyDatabase.m_order_editing.shipping_begin_time.isEmpty())
		{
			etPlacesTime.setText("");
		} else
		{
			StringBuilder sb=new StringBuilder();
			sb.append(g.MyDatabase.m_order_editing.shipping_begin_time.substring(0,2)+":"+g.MyDatabase.m_order_editing.shipping_begin_time.substring(2,4));
			etPlacesTime.setText(sb.toString());
		}
		
		etPlacesTime.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String newDate=Common.inputDateToString4(s.toString());
				if (!order_time.equals(newDate))
				{
					order_time=newDate;
				}
			}
		});

		EditText etPlacesDate = (EditText)findViewById(R.id.etPlacesDate);
		if (g.MyDatabase.m_order_editing.shipping_date.isEmpty())
		{
			etPlacesDate.setText("");
		} else
		{
			StringBuilder sb=new StringBuilder();
			sb.append(Common.dateStringAsText(g.MyDatabase.m_order_editing.shipping_date));
			etPlacesDate.setText(sb.toString());
		}
		
		final EditText editTextPlaces=(EditText)findViewById(R.id.editTextPlace);
		editTextPlaces.setText(g.MyDatabase.m_order_editing.stuff_places);
		
	    // Find the ListView resource. 
	    mainListView = (ListView) findViewById( R.id.listViewPlaces );
	    mainListView.setEmptyView(findViewById(android.R.id.empty));
	    
	    // When item is tapped, toggle checked properties of CheckBox and Planet.
	    mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	      @Override
	      public void onItemClick( AdapterView<?> parent, View item, 
	                               int position, long id) {
	        Place place = listAdapter.getItem( position );
	        if (!place.occupied||place.checked)
	        {
		        place.checked=!place.checked;
		        PlaceViewHolder viewHolder = (PlaceViewHolder) item.getTag();
		        viewHolder.getCheckBox().setChecked( place.checked );
	        }
	        
	        StringBuilder sb=new StringBuilder();
	        for (Place placeIterator: placesList)
	        {
	        	if (placeIterator.checked)
	        	{
	        		if (sb.length()!=0)
	        			sb.append(",");
	        		sb.append(placeIterator.descr);
	        	}
	        }
	        places=sb.toString();
	        editTextPlaces.setText(places);
	        
	      }
	    });
	    
	    placesIndex = new HashMap<String, Place>(); 
	    placesList = new ArrayList<Place>();
	    // Список всех столов
	    Cursor cursor=getContentResolver().query(MTradeContentProvider.PLACES_CONTENT_URI, null, null, null, "descr");
	    int indexId=cursor.getColumnIndex("id");
	    int indexDescr=cursor.getColumnIndex("descr");
	    while (cursor.moveToNext())
	    {
		    Place place = new Place();
		    place.uid=cursor.getString(indexId);
		    place.descr=cursor.getString(indexDescr);
		    place.checked=false;
		    for (OrderPlaceRecord placeIterator:g.MyDatabase.m_order_editing.places)
		    {
		    	if (placeIterator.place_id.toString().equals(place.uid))
		    	{
		    		place.checked=true;
		    		break;
		    	}
		    }
		    placesList.add(place);
		    
		    placesIndex.put(place.uid, place);
	    }
	    cursor.close();

	    updateOccupiedPlaces();
	    
	    // Кнопки выбора времени
	    Button buttonSelectTime=(Button)findViewById(R.id.buttonPlacesSelectTime);
	    buttonSelectTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_TIME);
			}
		});

	    Button buttonSelectDate=(Button)findViewById(R.id.buttonPlacesSelectDate);
	    buttonSelectDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(IDD_DATE);
			}
		});
	    
	    // Кнопка OK
		final Button buttonOk=(Button)findViewById(R.id.buttonPlacesOk);
		buttonOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent();
				intent.putExtra("order_time", order_time);
				intent.putExtra("order_date", order_date);
				intent.putExtra("places", places);
				
				ArrayList<String> placesIndices=new ArrayList<String>();
				ArrayList<String> placesDescr=new ArrayList<String>();
				//intent.putExtra("placesArray", new ArrayList<String>(placesIndex.keySet()));
				
				for (Place placeIterator: placesList)
				{
					if (placeIterator.checked)
					{
						placesIndices.add(placeIterator.uid);
						placesDescr.add(placeIterator.descr);
					}
				}
				
				intent.putExtra("placesIndices", placesIndices);
				intent.putExtra("placesDescr", placesDescr);
				
				setResult(PLACES_RESULT_OK, intent);
				finish();
			}
		});
		
	    // Кнопка Cancel
		final Button buttonCancel=(Button)findViewById(R.id.buttonPlacesCancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent();
				setResult(PLACES_RESULT_CANCEL, intent);
				finish();
			}
		});
		
	}
	
	void updateOccupiedPlaces()
	{
		MySingleton g=MySingleton.getInstance();
		
	    for (Place placeIterator:placesList)
	    {
	    	placeIterator.occupied=false;
	    	placeIterator.occupiedDocs="";
	    	placeIterator.occupiedDocsList.clear();
	    }
	
    if (order_date.length()>=8)
    {
	    // Занятые столы другими заказами (данные из 1С)
    	Cursor cursor=getContentResolver().query(MTradeContentProvider.OCCUPIED_PLACES_CONTENT_URI, null, "shipping_date between ? and ?", new String[]{order_date.substring(0, 8), order_date.substring(0, 8)+"Z"}, null);
	    int indexPlaceId=cursor.getColumnIndex("place_id");
	    int indexClientId=cursor.getColumnIndex("client_id");
	    int indexDocumentId=cursor.getColumnIndex("document_id");
	    int indexDateDoc=cursor.getColumnIndex("datedoc");
	    int indexDocument=cursor.getColumnIndex("document");
	    while (cursor.moveToNext())
	    {
	    	// Это и есть редактируемый документ, пропускаем
	    	if (cursor.getString(indexDocumentId).equals(g.MyDatabase.m_order_editing.id.toString()))
	    	{
	    		continue;
	    	}
	    	String placeId=cursor.getString(indexPlaceId);
	    	if (placesIndex.containsKey(placeId))
	    	{
			    Place place = placesIndex.get(placeId);
			    if (place.occupied)
			    {
			    	place.occupiedDocs=place.occupiedDocs+","+cursor.getString(indexDocument);
			    } else
			    {
			    	place.occupied=true;
			    	place.occupiedDocs=cursor.getString(indexDocument);
			    }
		    	place.occupiedDocsList.add(cursor.getString(indexDocumentId));
	    	}
	    }
	    cursor.close();
    }
    
    if (order_date.length()>=8)
    {
	    // Занятые столы в КПК
    	Cursor cursor=getContentResolver().query(MTradeContentProvider.ORDERS_PLACES_LIST_CONTENT_URI, null, "shipping_date between ? and ?", new String[]{order_date.substring(0, 8), order_date.substring(0, 8)+"Z"}, null);
	    //cursor=getContentResolver().query(MTradeContentProvider.ORDERS_PLACES_LIST_CONTENT_URI, null, null, null, null);
		int index_Id=cursor.getColumnIndex("_id");
	    int indexPlaceId=cursor.getColumnIndex("place_id");
	    int indexClientId=cursor.getColumnIndex("client_id");
	    int indexDocumentId=cursor.getColumnIndex("order_id"); // это числовое значение _id
	    int indexDocumentIdString=cursor.getColumnIndex("id"); // это строка
	    int indexDateDoc=cursor.getColumnIndex("datedoc");
	    int indexDocument=cursor.getColumnIndex("numdoc");
	    while (cursor.moveToNext())
	    {
	    	// Это и есть редактируемый документ, пропускаем
	    	if (cursor.getInt(index_Id)==g.MyDatabase.m_order_editing_id)
	    	{
	    		continue;
	    	}
	    	String placeId=cursor.getString(indexPlaceId);
	    	if (placesIndex.containsKey(placeId))
	    	{
			    Place place = placesIndex.get(placeId);
			    // если этот документ уже добавлен (из 1С), второй раз его не добавляем
		    	if (place.occupiedDocsList.contains(cursor.getString(indexDocumentIdString)))
		    	{
		    		continue;
		    	}
		    	String datedoc=cursor.getString(indexDateDoc);
		    	if (datedoc.length()>=8)
		    	{
		    		datedoc=datedoc.substring(6,8)+"."+datedoc.substring(4,6)+"."+datedoc.substring(0,4);
		    	}
			    if (place.occupied)
			    {
			    	place.occupiedDocs=place.occupiedDocs+","+"заказ "+cursor.getString(indexDocument)+" от "+datedoc;
			    } else
			    {
			    	place.occupied=true;
			    	place.occupiedDocs=cursor.getString(indexDocument)+" от "+datedoc;
			    }
	    	}
	    	// в place.occupiedDocsList не добавляем, т.к. здесь уже документы не дублируются (которые в КПК)
	    }
	    cursor.close();
    }
    listAdapter = new PlacesArrayAdapter(this, placesList, this);
    mainListView.setAdapter( listAdapter );
	
	}
	
	  @Override
	  protected Dialog onCreateDialog(int id) {
			switch (id) {
			case IDD_DATE:
			{
				int day=0;
				int month=0;
				int year=0;
				if (order_date.length()<8)
				{
					SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
					/*
					java.util.Date date=new java.util.Date();
					java.util.Date work_date=DatePreference.getDateFor(prefs, "work_date").getTime();
					// время - избыточно, требуется только дата (код скопирован из другого места)
					if (android.text.format.DateFormat.format("yyyyMMdd", date).equals(android.text.format.DateFormat.format("yyyyMMdd", work_date)))
					{
						// День совпал - берем текущее время
						order_date=android.text.format.DateFormat.format("yyyyMMddkkmmss", date).toString();
					} else
					{
						// Берем рабочую дату
						order_date=android.text.format.DateFormat.format("yyyyMMddkkmmss", work_date).toString();
					}
					*/
					Date work_date=DatePreference.getDateFor(prefs, "work_date").getTime();
					order_date=Common.MyDateFormat("yyyyMMdd", work_date);
				}
				if (order_date.length()>=8)
				{
					day=Integer.parseInt(order_date.substring(6,8));
					month=Integer.parseInt(order_date.substring(4,6))-1;
					year=Integer.parseInt(order_date.substring(0,4));
				}
			    // set date picker as current date
				return new DatePickerDialog(this, datePickerListener, 
	                         year, month,day);
			}
			case IDD_TIME:
			{
				int hourOfDay=0;
				int minute=0;
				if (order_time.length()==4)
				{
					hourOfDay=Integer.parseInt(order_time.substring(0,2));
					minute=Integer.parseInt(order_time.substring(2,4));
				}
			    // set date picker as current date
				return new TimePickerDialog(this, timePickerListener, 
	                         hourOfDay, minute, true);
			}
			}
			return null;
			}
			
			private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
				@Override
				// when dialog box is closed, below method will be called.
				public void onDateSet(DatePicker view, int selectedYear,
						int selectedMonth, int selectedDay) {
					
					order_date=Common.MyDateFormat("yyyyMMdd", new java.util.GregorianCalendar(selectedYear, selectedMonth, selectedDay));
					
					EditText etPlacesDate = (EditText)findViewById(R.id.etPlacesDate);
					
					StringBuilder sb=new StringBuilder();
					sb.append(Common.dateStringAsText(order_date));
					etPlacesDate.setText(sb.toString());
					
					updateOccupiedPlaces();
					
					//listAdapter.notifyDataSetChanged();
					
				}
			};
			
	
	
			@Override
			protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
				super.onPrepareDialog(id, dialog, args);
				switch (id)
				{
				case IDD_TIME:
				{
					int hourOfDay=0;
					int minute=0;
					if (order_time.length()==4)
					{
						hourOfDay=Integer.parseInt(order_time.substring(0,2));
						minute=Integer.parseInt(order_time.substring(2,4));
						((TimePickerDialog)dialog).updateTime(hourOfDay, minute);
					}
					break;
				}
				}
			}

	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		
		@Override
		public void onTimeSet(TimePicker view, int selectedHourOfDay, int selectedMinute) {
			
			EditText etPlacesTime = (EditText)findViewById(R.id.etPlacesTime);
			
			StringBuilder sb=new StringBuilder();
			order_time=String.format("%02d%02d", selectedHourOfDay, selectedMinute);
			sb.append(order_time.substring(0,2)+":"+order_time.substring(2,4));
			etPlacesTime.setText(sb.toString());
			
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.places, menu);
		return true;
	}
	
	private static class Place {
		String uid="";
		String descr="";
		boolean occupied=false;
		boolean checked=false;
		String occupiedDocs="";
		List<String> occupiedDocsList=new ArrayList<String>();
	}
	
	private static class PlaceViewHolder {
		private CheckBox checkBox ;
		private TextView textView ;
		public PlaceViewHolder() {}
		public PlaceViewHolder( TextView textView, CheckBox checkBox ) {
			this.checkBox = checkBox ;
		    this.textView = textView ;
		}
		public CheckBox getCheckBox() {
			return checkBox;
		}
		public void setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
		}
		
		public TextView getTextView() {
			return textView;
		}
		
	    public void setTextView(TextView textView) {
	    	this.textView = textView;
	    }    
	  }
	
	  private static class PlacesArrayAdapter extends ArrayAdapter<Place> {
		    
		    private LayoutInflater inflater;
		    Activity m_activity;
		    
		    public PlacesArrayAdapter( Context context, List<Place> planetList, Activity activity ) {
		      super( context, R.layout.places, R.id.textViewPlaceDescr, planetList );
		      m_activity = activity;
		      // Cache the LayoutInflate to avoid asking for a new one each time.
		      inflater = LayoutInflater.from(context) ;
		    }

		    @Override
		    public View getView(int position, View convertView, ViewGroup parent) {
		    	
		    	Place place = (Place) this.getItem( position );
		    	
		    	// The child views in each row.
		    	CheckBox checkBox ;
		    	TextView textView ; 
		      
		      // Create a new row view
		      if ( convertView == null ) {
		        convertView = inflater.inflate(R.layout.places_item, null);
		        
		        // Find the child views.
		        textView = (TextView) convertView.findViewById( R.id.textViewPlaceDescr );
		        checkBox = (CheckBox) convertView.findViewById( R.id.CheckBoxPlaceItem );
		        
		        // Optimization: Tag the row with it's child views, so we don't have to 
		        // call findViewById() later when we reuse the row.
		        // оптимизация убрана, чтобы при изменении данных (даты обслуживания) таблица корректно перерисовалась
		        convertView.setTag( new PlaceViewHolder(textView,checkBox) );

		        // If CheckBox is toggled, update the planet it is tagged with.
		        checkBox.setOnClickListener( new OnClickListener() {
		          public void onClick(View v) {
		            CheckBox cb = (CheckBox) v ;
		            Place place = (Place) cb.getTag();
		            place.checked = cb.isChecked();
		            
		            final EditText editTextPlaces=(EditText)m_activity.findViewById(R.id.editTextPlace);
		            
			        StringBuilder sb=new StringBuilder();
			        for (Place placeIterator: placesList)
			        {
			        	if (placeIterator.checked)
			        	{
			        		if (sb.length()!=0)
			        			sb.append(",");
			        		sb.append(placeIterator.descr);
			        	}
			        }
			        places=sb.toString();
			        editTextPlaces.setText(places);
		            
		          }
		        });
		      }
		      // Reuse existing row view
		      else {
		        // Because we use a ViewHolder, we avoid having to call findViewById().
		        PlaceViewHolder viewHolder = (PlaceViewHolder) convertView.getTag();
		        checkBox = viewHolder.getCheckBox() ;
		        textView = viewHolder.getTextView() ;
		      }

		      // Tag the CheckBox with the Planet it is displaying, so that we can
		      // access the planet in onClick() when the CheckBox is toggled.
		      checkBox.setTag( place ); 
		      
		      // Display planet data
		      checkBox.setChecked( place.checked );
		      if (place.occupied)
		      {
		    	  textView.setText( place.descr + "("+place.occupiedDocs+")" );
		    	  textView.setEnabled(false);
		    	  checkBox.setEnabled(false);
		      } else
		      {
			      textView.setText( place.descr );
			      textView.setEnabled(true);
			      checkBox.setEnabled(true);
		      }
		      
		      return convertView;
		    }
		    
		  }

		  
	  /*
		  public Object onRetainNonConfigurationInstance() {
		    return planets ;
		  }
	*/

}
