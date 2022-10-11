package ru.code22.mtrade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.loader.app.LoaderManager;

import java.util.ArrayList;
import java.util.List;

// http://startandroid.ru/ru/uroki/vse-uroki-spiskom/113-urok-54-kastomizatsija-spiska-sozdaem-svoj-adapter
// http://wowjava.wordpress.com/2011/03/26/dynamic-listview-in-android/

// 26.03.2014
// http://stackoverflow.com/questions/9392511/how-to-handle-oncheckedchangelistener-for-a-radiogroup-in-a-custom-listview-adap

public class MyNomenclatureGroupAdapter extends BaseAdapter {

    private RedrawListLisnener mListener;

    static class Tree {String _id; String id; String parent_id; String descr; int level;};

    Context context;
    LayoutInflater inflater;
    List<String> m_list_groups;
    ArrayList<Tree> m_list2;
    MyDatabase.MyID m_group_id;
    ArrayList<String> m_group_ids;


    public MyNomenclatureGroupAdapter(Context context) {
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

    public void setMyListGroups(List<String> list_groups, List<Tree> tree, MyDatabase.MyID group_id, List<String> group_ids)
    {
        if (list_groups==null)
            m_list_groups=null;
        else
            m_list_groups=new ArrayList<>(list_groups);
        if (tree==null)
            m_list2=null;
        else
            m_list2=new ArrayList<Tree>(tree);
        if (group_id==null)
            m_group_id=null;
        else
            m_group_id=group_id.clone();
        if (group_ids==null)
            m_group_ids=null;
        else
            m_group_ids=new ArrayList<String>(group_ids);
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



        // обработчик для чекбоксов
        CompoundButton.OnCheckedChangeListener myCheckChangList = new CompoundButton.OnCheckedChangeListener() {

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
                    m_group_id=new MyDatabase.MyID(m_list2.get(position).id);
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
                if (mListener!=null)
                    mListener.onRestartLoader();
            }
        };

        rbHierarchy.setOnCheckedChangeListener(myCheckChangList);
        return view;
    }


    public void setOnRedrawListListener(RedrawListLisnener listener) {
        this.mListener = listener;
    }

    public interface RedrawListLisnener {
        void onRestartLoader();
    }



}