package ru.code22.mtrade;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.widget.ImageView;

public class ImageActivity extends Activity {

	static final int IMAGE_RESULT_OK=1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image);
		//
		//Intent intent=new Intent(NomenclatureActivity.this, ImageActivity.class);
		//intent.putExtra("image_file", Uri.fromFile(imageFile));
		//myImage.setImageURI();
		//startActivityForResult(intent, OPEN_IMAGE_REQUEST);
		
	   	Intent intent=getIntent();
		File imageFile=new File(intent.getStringExtra("image_file"));
	   		   	
	   	ImageView imgView=(ImageView) findViewById(R.id.imageViewImage);
	   	imgView.setImageURI(Uri.fromFile(imageFile));
	}
	
	@Override
    public void onDestroy()
    {
		ImageView imageViewMessage=(ImageView)findViewById(R.id.imageViewImage);
		Drawable toRecycle= imageViewMessage.getDrawable();
		if (toRecycle != null) {
		    ((BitmapDrawable)toRecycle).getBitmap().recycle();
		}
        super.onDestroy();
    }	
	
	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}
	*/

}
