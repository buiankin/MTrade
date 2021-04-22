package ru.code22.mtrade;

import java.util.ArrayList;
import java.util.Vector;

import ru.code22.mtrade.DistribsPageFragment.onSomeDistribsEventListener;
import ru.code22.mtrade.MyDatabase.DistribsLineRecord;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class DistribsLinesAdapter extends BaseAdapter{

	int m_myTextColor;
	
	  public interface onDistribsLinesDataChangedListener {
		    public void onDistribsLinesDataChanged();
		  }
	    
	  onDistribsLinesDataChangedListener someEventListener=null;
	  public void setOnDistribsLinesDataChangedListener(onDistribsLinesDataChangedListener someEventListener)
	  {
		  this.someEventListener=someEventListener;
	  }
		  
	  /*		  @Override
		  public void onAttach(Activity activity) {
		    super.onAttach(activity);
		        try {
		          someEventListener = (onSomeDistribsEventListener) activity;
		        } catch (ClassCastException e) {
		            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
		        }
	  }
	  */	  
	
	Context ctx;
	LayoutInflater lInflater;
	public ArrayList<DistribsLineRecord> lines;
	
	DistribsLinesAdapter(Context context, ArrayList<DistribsLineRecord> lines) {
	    ctx = context;
	    this.lines = lines;
	    lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}	

	// кол-во элементов
	@Override
	public int getCount() {
		return lines.size();
	}

	// элемент по позиции
	@Override
	public Object getItem(int position) {
		return lines.get(position);
	}

	// id по позиции
	@Override
	public long getItemId(int position) {
		return position;
	}

	// пункт списка
	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    // используем созданные, но не используемые view
	    View view = convertView;
	    if (view == null) {
	    	if (MySingleton.getInstance().Common.DISTRIBS_BY_QUANTITY)
	    	{
	    		view = lInflater.inflate(R.layout.distribs_lines_item_quantity, parent, false);
	    	} else
	    	{
	    		view = lInflater.inflate(R.layout.distribs_lines_item, parent, false);	    		
	    	}
	    }
	 
	    DistribsLineRecord line = getDistribsLine(position);
	    
	    if (MySingleton.getInstance().Common.DISTRIBS_BY_QUANTITY)
	    {
		    ((TextView) view.findViewById(R.id.tvDescr)).setText(line.stuff_distribs_contract);
		    ((TextView) view.findViewById(R.id.tvQuantity)).setText(Common.DoubleToStringFormat(line.quantity, "%.0f"));

            ((TextView) view.findViewById(R.id.tvDescr)).setTextColor(m_myTextColor);
            ((TextView) view.findViewById(R.id.tvQuantity)).setTextColor(m_myTextColor);

	    } else
	    {
		    // заполняем View в пункте списка данными из товаров: наименование, цена
		    // и картинка
		    ((TextView) view.findViewById(R.id.tvDescr)).setText(line.stuff_distribs_contract);
		    ((TextView) view.findViewById(R.id.tvPrice)).setText("price");
		    //((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);
		    ((ImageView) view.findViewById(R.id.ivImage)).setImageResource(android.R.color.transparent);

		    CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
		    // чтобы событие, которое меняет данные не сработало 
		    cbBuy.setOnCheckedChangeListener(null);
		    // пишем позицию
		    cbBuy.setTag(position);
		    // заполняем данными из товаров: в корзине или нет
		    cbBuy.setChecked(line.quantity>0.0);
		    // присваиваем чекбоксу обработчик
		    cbBuy.setOnCheckedChangeListener(myCheckChangeList);

            ((TextView) view.findViewById(R.id.tvDescr)).setTextColor(m_myTextColor);
            ((TextView) view.findViewById(R.id.tvPrice)).setTextColor(m_myTextColor);
	    }

	    return view;
	  }
	 
	  // товар по позиции
	  DistribsLineRecord getDistribsLine(int position) {
	    return ((DistribsLineRecord) getItem(position));
	  }
	 
	  // содержимое корзины
	  /*
	  ArrayList<Product> getBox() {
	    ArrayList<Product> box = new ArrayList<Product>();
	    for (Product p : objects) {
	      // если в корзине
	      if (p.box)
	        box.add(p);
	    }
	    return box;
	  }
	  */
	 
	  // обработчик для чекбоксов
	  OnCheckedChangeListener myCheckChangeList = new OnCheckedChangeListener() {
	    public void onCheckedChanged(CompoundButton buttonView,
	        boolean isChecked) {
	      // меняем данные товара (в корзине или нет)
	    	getDistribsLine((Integer) buttonView.getTag()).quantity = isChecked?1.0:0.0;
	    	if (someEventListener!=null)
	    	{
	    		someEventListener.onDistribsLinesDataChanged();
	    	}
	    }
	  };

	public void setMyTextColor(int color)
	{
		m_myTextColor=color;
	}
}
