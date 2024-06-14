package ru.code22.mtrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;

public class NomenclatureHierarchyHelper {

	Context m_context;
	
	class HierarchyRecord
	{
		int level;
		String descr;
		String level_id[];
	}
	
	public Map<String, HierarchyRecord> m_data;
	
	public NomenclatureHierarchyHelper(Context context) {
		m_context=context;
		m_data=new HashMap<String, HierarchyRecord>();
		init();
	}
	
	public void init()
	{
		m_data.clear();
		Cursor cursor=m_context.getContentResolver().query(MTradeContentProvider.NOMENCLATURE_HIERARCHY_CONTENT_URI, null, null, null, null);
		int index_id=cursor.getColumnIndex("id");
		int index_level=cursor.getColumnIndex("level");
		int index_descr=cursor.getColumnIndex("groupDescr");
		int index_level_0_id=cursor.getColumnIndex("level0_id");
		int index_level_1_id=cursor.getColumnIndex("level1_id");
		int index_level_2_id=cursor.getColumnIndex("level2_id");
		int index_level_3_id=cursor.getColumnIndex("level3_id");
		int index_level_4_id=cursor.getColumnIndex("level4_id");
		int index_level_5_id=cursor.getColumnIndex("level5_id");
		int index_level_6_id=cursor.getColumnIndex("level6_id");
		int index_level_7_id=cursor.getColumnIndex("level7_id");
		int index_level_8_id=cursor.getColumnIndex("level8_id");
		int index_dont_use_in_hierarchy=cursor.getColumnIndex("dont_use_in_hierarchy");

		while (cursor.moveToNext())
		{
			if (cursor.getInt(index_dont_use_in_hierarchy)==1)
				continue;
			HierarchyRecord rec=new HierarchyRecord();
			
			rec.level=cursor.getInt(index_level);
			rec.descr=cursor.getString(index_descr);
			rec.level_id=new String[]{
					cursor.getString(index_level_0_id), cursor.getString(index_level_1_id), cursor.getString(index_level_2_id), cursor.getString(index_level_3_id),
					cursor.getString(index_level_4_id), cursor.getString(index_level_5_id), cursor.getString(index_level_6_id), cursor.getString(index_level_7_id),
					cursor.getString(index_level_8_id)};
			m_data.put(cursor.getString(index_id), rec);
		}
		cursor.close();
	}
	
	public String getDescr(String group_id, int level)
	{
		HierarchyRecord rec=m_data.get(group_id);
		if (rec!=null)
		{
			if (rec.level==level)
				return rec.descr;
			HierarchyRecord rec_level=m_data.get(rec.level_id[level]);
			if (rec_level!=null)
			{
				return rec_level.descr;
			}
		}
		return null;
	}

	public String getId(String group_id, int level)
	{
		HierarchyRecord rec=m_data.get(group_id);
		if (rec!=null)
		{
			if (rec.level==level)
				return group_id;
			return rec.level_id[level];
		}
		return null;
	}
	
	public String getNomenclautureParentId(String element_id)
	{
		String result=null;
		Cursor cursor=m_context.getContentResolver().query(MTradeContentProvider.NOMENCLATURE_CONTENT_URI, new String[]{"parent_id"}, "id=?", new String[]{element_id}, null);
		if (cursor.moveToNext())
		{
			result=cursor.getString(0);
		}
		cursor.close();
		return result;
	}
	
}
