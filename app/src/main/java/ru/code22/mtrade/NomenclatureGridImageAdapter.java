package ru.code22.mtrade;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class NomenclatureGridImageAdapter extends BaseAdapter {
	
	private Context mContext;
	
	public NomenclatureGridImageAdapter(Context c) {
		mContext = c;
	}	

	@Override
	public int getCount() {
		return 70;
	}

	@Override
	public Object getItem(int position) {
		//mThumbIds[position]
		//return R.drawable.ic_launcher;
		return R.drawable.folder32x32;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = new ImageView(mContext);
		//imageView.setImageResource(mThumbIds[position]);
		//imageView.setImageResource(R.drawable.ic_launcher);
		imageView.setImageResource(R.drawable.folder32x32);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(new GridView.LayoutParams(120, 110));
		return imageView;
	}

}
