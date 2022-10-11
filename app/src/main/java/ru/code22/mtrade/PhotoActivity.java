package ru.code22.mtrade;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class PhotoActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {
	
	private ImageButton bStart;
	private ImageButton bStop;
	private ImageButton bTake;
	
	private SurfaceView surView;
	private SurfaceHolder surHolder;
	
	private Camera camera;
	private boolean isCameraPreview = false;
	
	private ShutterCallback shutter = new ShutterCallback()
	{
		@Override
		public void onShutter(){}
	};
	
	private PictureCallback raw = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1){}
	};
	
	private PictureCallback jpg = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1)
		{
			Bitmap bitmapPicture = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
			camera.stopPreview();
			camera.release();
			isCameraPreview=false;
			
			bStart.setEnabled(!isCameraPreview);
			bStop.setEnabled(isCameraPreview);
		}
	};


	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		setContentView(R.layout.photo);
		
		surView = (SurfaceView)findViewById(R.id.surfaceview);
		surHolder = surView.getHolder();
		surHolder.addCallback(this);
		
		LayoutInflater inflater=LayoutInflater.from(getBaseContext());
		View overlay=inflater.inflate(R.layout.camera_overlay, null);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addContentView(overlay, params);
		
		bStart=(ImageButton)overlay.findViewById(R.id.bStart);
		bStop=(ImageButton)overlay.findViewById(R.id.bStop);
		bTake=(ImageButton)overlay.findViewById(R.id.bTake);
		
		bStart.setOnClickListener(this);
		bStop.setOnClickListener(this);
		bTake.setOnClickListener(this);
		
		bTake.setEnabled(isCameraPreview);
		bStop.setEnabled(isCameraPreview);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) { }

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (isCameraPreview)
		{
			camera.stopPreview();
			camera.release();
			isCameraPreview=false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.photo, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.bStart:
			try
			{
				int cameraId=0;
				camera = Camera.open();
				
				android.hardware.Camera.CameraInfo info =
			            new android.hardware.Camera.CameraInfo();
			    android.hardware.Camera.getCameraInfo(cameraId, info);
			    int rotation = getWindowManager().getDefaultDisplay().getRotation();
			    int degrees = 0 ;
			    switch ( rotation ) {
			        case Surface.ROTATION_0 : degrees = 0 ; break ;
			        case Surface.ROTATION_90 : degrees = 90 ; break ;
			        case Surface.ROTATION_180 : degrees = 180 ; break ;
			        case Surface.ROTATION_270 : degrees = 270 ; break ;
			    }
			    int result ;
			    if ( info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			        result = ( info.orientation + degrees ) % 360 ;
			         result = ( 360 - result ) % 360 ;   // compensate the mirror
			    } else {   // back-facing

			        result = ( info.orientation - degrees + 360 ) % 360 ;

			    }				
				camera.setDisplayOrientation(result);
				
				camera.setPreviewDisplay(surHolder);
				camera.startPreview();
				isCameraPreview=true;
				
				bStart.setEnabled(!isCameraPreview);
				bStop.setEnabled(!isCameraPreview);
			} catch (IOException e) {
				Snackbar.make(findViewById(android.R.id.content), e.toString(), Snackbar.LENGTH_LONG).show();
			}
			break;
		case R.id.bTake:
			camera.takePicture(shutter, raw, jpg);
			break;
		case R.id.bStop:
			camera.stopPreview();
			camera.release();
			isCameraPreview=false;
			
			bStart.setEnabled(!isCameraPreview);
			bStop.setEnabled(isCameraPreview);
			break;
		}
		
	}

}
