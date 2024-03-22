package ru.code22.mtrade;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.jheader.App1Header;
import net.sourceforge.jheader.ExifFormatException;
import net.sourceforge.jheader.JpegFormatException;
import net.sourceforge.jheader.JpegHeaders;
import net.sourceforge.jheader.TagFormatException;
import net.sourceforge.jheader.TagValue;
import net.sourceforge.jheader.App1Header.Tag;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class OrderPreActivity extends Activity {
	
	private static final int CAMERA_REQUEST1 = 5;
	private static final int CAMERA_REQUEST2 = 6;
	
	static final int ORDER_PRE_ACTION_RESULT_OK=1;
	
	//private static final String photoFolder = "/mtrade/photo";
	//private String exif_ORIENTATION = "";
	private static Uri outputPhotoFileUri = null;
	
	boolean m_bHavePicture1=false;
	boolean m_bHavePicture2=false;

	private String m_client_id;
	private String m_distr_point_id;
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent);
	}	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//bButtonPressed=false;
		super.onCreate(savedInstanceState);
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);

		setContentView(R.layout.order_pre_photo);

		if (savedInstanceState!=null)
		{
			m_bHavePicture1=savedInstanceState.getBoolean("HavePicture1");
			m_bHavePicture2=savedInstanceState.getBoolean("HavePicture2");
			m_client_id=savedInstanceState.getString("client_id");
			m_distr_point_id=savedInstanceState.getString("distr_point_id");
		} else
		{
			Intent intent=getIntent();
			m_client_id=intent.getStringExtra("client_id");
			m_distr_point_id=intent.getStringExtra("distr_point_id");
		}
		updateImageCaptions();
		
	    // Кнопка изображения 1
		final Button buttonMakePhoto1=(Button)findViewById(R.id.buttonMakePhoto1);
		final Button buttonMakePhoto2=(Button)findViewById(R.id.buttonMakePhoto2);
		final Button buttonCreateOrder=(Button)findViewById(R.id.buttonCreateOrder);
		
		OnClickListener onClickListener=new OnClickListener() {
			@Override
			public void onClick(View v) {
				String temp_fileName = "tmp.jpg";
				//if (v==buttonMakePhoto2)
				//{
				//	temp_fileName = "tmp2.jpg";
				//}
				
				//File photoDir = new File(Environment.getExternalStorageDirectory() + photoFolder);
	            //if (!photoDir.exists()) {
	            //	photoDir.mkdirs();
	            //}
				//File photoDir=Common.getMyStorageFileDir(getBaseContext(), "photo");
                File photoDir = new File(getFilesDir(), "photos");
                if (!photoDir.exists()) {
                    photoDir.mkdirs();
                }

				// к сожалению приходится устанавливать параметр EXTRA_OUTPUT
				// т.к. есть телефоны с багом, которые не возвращают uri, а только data (небольшую картинку)
				// у меня такой телефон создает только 1 файл в этом случае
				// другие же с указанием EXTRA_OUTPUT создают сразу 2 файла, но пусть будет так
	            
				File temp_photoFile = new File(photoDir, temp_fileName);
				//outputPhotoFileUri=Uri.fromFile(temp_photoFile);
				outputPhotoFileUri= FileProvider.getUriForFile(getBaseContext(), "ru.code22.fileprovider", temp_photoFile);
				
				//Intent intent=new Intent(MainActivity.this, PhotoActivity.class);
				//startActivityForResult(intent, CREATE_PHOTO_MESSAGE_REQUEST);
				
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				//cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				cameraIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());
	            //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, "new-photo-name.jpg");
				
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPhotoFileUri);
				cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				//cameraIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, 90);
				cameraIntent.putExtra("return-data", false); // мне кажется что этот флаг все равно игнорируется, как будто указываем все равно true

				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						cameraIntent.setClipData(ClipData.newRawUri("", outputPhotoFileUri));
					}
					cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
				}

				if (v==buttonMakePhoto1)
				{
					startActivityForResult(cameraIntent, CAMERA_REQUEST1);
				}
				if (v==buttonMakePhoto2)
				{
					startActivityForResult(cameraIntent, CAMERA_REQUEST2);
				}
				//break;
			}
		};

		buttonMakePhoto1.setOnClickListener(onClickListener);		
		buttonMakePhoto2.setOnClickListener(onClickListener);
		
		buttonCreateOrder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	  			Intent intent=new Intent();
				intent.putExtra("client_id", m_client_id);
				intent.putExtra("distr_point_id", m_distr_point_id);
	  			setResult(ORDER_PRE_ACTION_RESULT_OK, intent);
	  			finish();
			}
		});
	}
	
	void updateImageCaptions()
	{
		TextView textViewOrderPhoto1=(TextView)findViewById(R.id.textViewOrderPhoto1);
		TextView textViewOrderPhoto2=(TextView)findViewById(R.id.textViewOrderPhoto2);
		Button buttonCreateOrder=(Button)findViewById(R.id.buttonCreateOrder);
		if (m_bHavePicture1)
			textViewOrderPhoto1.setText(R.string.label_image_ok);
		else
			textViewOrderPhoto1.setText(R.string.label_no_image);
		if (m_bHavePicture2)
			textViewOrderPhoto2.setText(R.string.label_image_ok);
		else
			textViewOrderPhoto2.setText(R.string.label_no_image);
		// TODO
		//buttonCreateOrder.setEnabled(m_bHavePicture1&&m_bHavePicture2);
		buttonCreateOrder.setEnabled(true);
	}
	
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		m_bHavePicture1=savedInstanceState.getBoolean("HavePicture1");
		m_bHavePicture2=savedInstanceState.getBoolean("HavePicture2");
		m_client_id=savedInstanceState.getString("client_id");
		m_distr_point_id=savedInstanceState.getString("distr_point_id");
		updateImageCaptions();
	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("HavePicture1", m_bHavePicture1);
		outState.putBoolean("HavePicture1", m_bHavePicture2);
		outState.putString("client_id", m_client_id);
		outState.putString("distr_point_id", m_distr_point_id);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode)
    	{
    	case CAMERA_REQUEST1:
    	case CAMERA_REQUEST2:
    		if (resultCode == RESULT_OK) 
            {
                // Unfortunately there is a bug on some devices causing the Intend data parameter 
                // in onActivityResult to be null when you use the MediaStore.EXTRA_OUTPUT flag 
                // in your intent for the camera. A workaround is to keep the outputFileUri 
                // variable global so that you can access it again in your onActivityResult method
    			
    			java.util.Date date=new java.util.Date();
    			//String timeString=String.valueOf(System.currentTimeMillis());
    			String timeString=Common.MyDateFormat("dd.MM.yyyy HH:mm:ss", date);
    			//String fileName = android.text.format.DateFormat.format("yyyyMMddkkmmss", date).toString() + ".jpg";
    			String fileName = "order_image_1.jpg";
    			if (requestCode==CAMERA_REQUEST2)
    				fileName = "order_image_2.jpg";
				// Временный файл, будет использоваться не всегда, а только у самсунгов
				String fileNameIntermediate = "S"+fileName;

    			
    			/*
    			File photoDir = new File(Environment.getExternalStorageDirectory() + photoFolder);
                if (!photoDir.exists()) {
                	photoDir.mkdirs();
                }
                */
				File photoDir=Common.getMyStorageFileDir(getBaseContext(), "photo");
                
                boolean bSavedOk=false;
                
                File imgWithTime=new File(photoDir, fileName);

                ExifInterface exifInterface=null;
    			
                if(data != null)
                {
            	  //if the intent data is not null, use it
            	  //puc_image = getImagePath(Uri.parse(data.toURI())); //update the image path accordingly
                  //StoreImage(this, Uri.parse(data.toURI()), puc_img);
                  if (data.getData()!=null)
                  {
                	  bSavedOk=ImagePrinting.StoreImage(this, data.getData(), imgWithTime, timeString);
	            	  if (bSavedOk)
	            	  {
	            		  // Удалим первый файл, созданный телефоном
	            		  String imgPath=ImagePrinting.getImagePath(this, data.getData());
	                      File oldFile = new File(imgPath);
	                      oldFile.delete();
	            	  }
                  } else
                  {
                	  // Самсунг)
					  if (!ImagePrinting.checkImagePath(this, outputPhotoFileUri))
					  {
						  // реального имени файла нет, данные в потоке
						  // заберем их оттуда в промежуточный файл
						  File intermegiateFile=new File(photoDir, fileNameIntermediate);
						  try {
							  InputStream input = getContentResolver().openInputStream(outputPhotoFileUri);
							  if (Common.myCopyStreamToFile(input, intermegiateFile))
							  {
								  outputPhotoFileUri=Uri.fromFile(intermegiateFile);
							  }

						  } catch (FileNotFoundException e) {
							  throw new RuntimeException(e);
						  }
					  }
					  bSavedOk=ImagePrinting.StoreImage(this, outputPhotoFileUri, imgWithTime, timeString);

                  }
                }
                else
                {
            	   //Use the outputFileUri global variable
            	   bSavedOk=ImagePrinting.StoreImage(this, outputPhotoFileUri, imgWithTime, timeString);
                }
                if (bSavedOk)
                {
                    if (outputPhotoFileUri.getScheme().equals("content")){
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                InputStream iStream = getContentResolver().openInputStream(outputPhotoFileUri);
                                exifInterface = new ExifInterface(iStream);
                            }
                        } catch (IOException e) {
                            exifInterface=null;
                        }
                        /* TODO удалять файл
                        FileProvider provider = new FileProvider();
                        grantUriPermission(getApplicationContext().getPackageName(), outputPhotoFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Падает вот здесь
                        provider.delete(outputPhotoFileUri, null, null);
                        */
                    }else{
                        String imgPath=ImagePrinting.getImagePath(this, outputPhotoFileUri);
                        File oldFile = new File(imgPath);
                        try {
                            exifInterface = new ExifInterface(imgPath);
                        } catch (IOException e) {
                            exifInterface=null;
                        }
                        oldFile.delete();
                    }

	               
	               String exif_DATETIME = Common.MyDateFormat("yyyy:MM:dd HH:mm:ss", date);
	               String exif_MODEL = "Android";
	               String exif_MAKE = "";
                   //String exif_IMAGE_LENGTH="";
                   //String exif_IMAGE_WIDTH="";
	               
	               if (exifInterface!=null)
	               {
	            	   //exif_DATETIME = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
	                   exif_MODEL = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
	                   exif_MAKE = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
	                   //String et=exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
	                   //exif_IMAGE_LENGTH = exifInterface.getAttribute(ExifInterface..TAG_IMAGE_LENGTH);
	                   //exif_IMAGE_WIDTH = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
	               }
	               try {
						ExifInterface exifInterface2 = new ExifInterface(imgWithTime.getAbsolutePath());
						exifInterface2.setAttribute(ExifInterface.TAG_DATETIME, exif_DATETIME);
						exifInterface2.setAttribute(ExifInterface.TAG_MODEL, exif_MODEL);
						exifInterface2.setAttribute(ExifInterface.TAG_MAKE, exif_MAKE);
						exifInterface2.saveAttributes();
	            	   
	            	    JpegHeaders headers;
						try {
							
						    // Initializes static data - not necessary as it is done
						    // automatically the first time it is needed.  However doing
						    // it explicitly means the first call to the library won't be
						    // slower than subsequent calls.
						    JpegHeaders.preheat();

						    // Parse the JPEG file
							headers = new JpegHeaders(imgWithTime.getAbsolutePath());

						    // If the file isn't EXIF, convert it
						    if (headers.getApp1Header() == null)
						    	headers.convertToExif();
							
		                    App1Header app1Header = headers.getApp1Header();
		                    //app1Header.setValue(new TagValue(Tag.IMAGEDESCRIPTION,"bla bla bla"));
		                    app1Header.setValue(new TagValue(Tag.DATETIMEORIGINAL, exif_DATETIME));
		                    app1Header.setValue(new TagValue(Tag.DATETIMEDIGITIZED,exif_DATETIME));
		            	    // set a field
		            	    //app1Header.setValue(new TagValue(Tag.IMAGEDESCRIPTION,
		            		//			     "My new image description"));
		                    //headers.save(true);
		                    // Резервная копия не нужна
		                    headers.save(false);
						} catch (ExifFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TagFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JpegFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				   } catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				   }
				   if (requestCode==CAMERA_REQUEST2)
	    			{
	    				m_bHavePicture2=true;
	    			} else
	    			{
	    				m_bHavePicture1=true;
	    			}
	    			updateImageCaptions();
                }
            }
    		break;
    		
    	}
    }
	
	
}
