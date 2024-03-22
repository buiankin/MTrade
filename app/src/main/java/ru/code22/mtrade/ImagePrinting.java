package ru.code22.mtrade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class ImagePrinting {
	
    private static void printTextToBitap(Bitmap bitmap, String timeString, int textSize, int x, int y, Rect bounds)
    {
	    Canvas canvas = new Canvas(bitmap);
	    //int scale=2;
	    //String gText="TEST PHOTO";
	    String gText=timeString;
	    
	    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    // text color - #3D3D3D
	    //paint.setColor(Color.rgb(61, 61, 61));
	    //paint.setColor(Color.rgb(61, 61, 61));
	    paint.setColor(Color.YELLOW); // желтый
	    // text size in pixels
	    //paint.setTextSize((int) (25 * scale));
	    
	    //int min_size=Math.min(bitmap.getWidth(), bitmap.getHeight());
	    //paint.setTextSize((int) (min_size/20.0f));
	    paint.setTextSize(textSize);
	    
	    // text shadow
	    //paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
	    paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
	
	    // draw text to the Canvas center
	    //Rect bounds = new Rect();
	    paint.setTextAlign(Align.LEFT);
	
	    paint.getTextBounds(gText, 0, gText.length(), bounds);
	    //int x = (bitmap.getWidth() - bounds.width())/2;
	    //int y = (bitmap.getHeight() + bounds.height())/2;
	    
	    canvas.drawText(gText, x, y, paint);
    }
	
    static public boolean StoreImage(Activity activity, Uri imageLoc, File imageOutFile, String timeString)  
    {
    	/*
		try
		{
			ExifInterface exif = new ExifInterface(puc_img.getAbsolutePath());
			exif_ORIENTATION = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
			//Toast.makeText(MainActivity.this, exif_ORIENTATION, Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	*/
    	
    	int max_image_size=Constants.max_image_side_size;
    	// Попробуем нативно обработать файл (для этого файл должен существовать на диске)
    	try
    	{
	    	File inameInFile = new File(getImagePath(activity, imageLoc));
	    	if (inameInFile.exists())
	    	{
	        	BitmapFactory.Options options = new BitmapFactory.Options();
	        	options.inJustDecodeBounds = true;
	        	//Returns null, sizes are in the options variable
	        	BitmapFactory.decodeFile(inameInFile.getAbsolutePath(), options);
	        	int width = options.outWidth;
	        	int height = options.outHeight;
	        	//If you want, the MIME type will also be decoded (if possible)
	        	//String type = options.outMimeType;
	        	int max_size=Math.max(width, height);
	        	if (max_size>max_image_size)
	        	{
		        	// ширина, высота
		        	//BufferedImage img = new BufferedImage(150, 100, BufferedImage.TYPE_INT_RGB);
		        	//Canvas canvas0 = new Canvas();
		        	//canvas0.set
	        		int scaleText=1;
		        	int textLeft=0;
		        	int textTop=0;
		        	int textWidth=700;
		        	int textHeight=250;
		        	Bitmap bitmapText=null;
		        	while (true)
		        	{
			        	bitmapText=Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
			        	Rect bounds=new Rect();
			        	// Размер шрифта 80, без отступов
			        	printTextToBitap(bitmapText, timeString, 80/scaleText, 0, 0, bounds);
			        	if (bounds.left<0||bounds.top<0||bounds.right>textWidth||bounds.bottom>textHeight)
			        	{
			        		// Надо увеличить область
			            	textWidth=bounds.right;
			            	if (bounds.left<0)
			            	{
			            		textLeft=-bounds.left;
			            		textWidth=textLeft+bounds.right;
			            	}
			            	textHeight=bounds.bottom;
			            	if (bounds.top<0)
			            	{
			            		textTop=-bounds.top;
			            		textHeight=textTop+bounds.bottom;
			            	}
			            	if (textWidth>700)
			            	{
			            		scaleText++;
			            		continue;
			            	}
			            	bitmapText=Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888);
			            	// Размер шрифта 80, без отступов
			            	printTextToBitap(bitmapText, timeString, 80/scaleText, textLeft, textTop, bounds);
			            	break;
			        	}
		        	}
		        	// TODO нативно обрабатываем только если файл большой
		        	byte []textData=new byte[textHeight*textWidth*4];
		        	int i,j;
		        	for (i=0;i<textHeight;i++)
		        	{
		        		for (j=0;j<textWidth;j++)
		        		{
		                	int color=bitmapText.getPixel(j, i);
		                	textData[(i*textWidth+j)*4]  = (byte) ((color>>16)&0xFF);
		                	textData[(i*textWidth+j)*4+1]= (byte) ((color>>8)&0xFF);
		                	textData[(i*textWidth+j)*4+2]= (byte) ((color>>0)&0xFF);
		                	// либо (byte)255, если надо, чтобы было непрозрачным
		                	textData[(i*textWidth+j)*4+3]= (byte) ((color>>24)&0xFF);
		        		}
		        	}
		            //File imgWithTime2=new File(inameInFile.getParent(), "!17!.jpg");
		  		  	int result=NativeCallsClass.convertJpegFile(inameInFile.getAbsolutePath(), imageOutFile.getAbsolutePath(), textData, 50, 50, textWidth, textHeight, 75, scaleText);
		  		  	if (result==1)
		  		  	{
		  		  		// Успешно
		  		  		return true;
		  		  	}
	        	}
	    	}
    	} catch (Exception e)   
        {  
            e.printStackTrace();  
        }
    		
        Bitmap bitmap = null;  
        try   
        {  
        	bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageLoc);
        	
        	/*
        	int w = 1280;
            int h = 1024;
            
            //int degree=90;

            Matrix mtx = new Matrix();
            //mtx.postRotate(degree);
            mtx.postScale((float)w/bitmap.getWidth(), (float)h/bitmap.getHeight());
            */
        	//int max_image_size=300000;
        	
        	int max_size=Math.max(bitmap.getWidth(), bitmap.getHeight());
        	if (max_size>max_image_size)
        	{
                Matrix mtx = new Matrix();
                //mtx.postRotate(degree);
        		// scale=2,3... size=1024..2047
        		//int scale=(max_size+2047)/2048;
                //mtx.postScale(1.0f/scale, 1.0f/scale);
                mtx.postScale(max_image_size/(float)max_size, max_image_size/(float)max_size);
                //Bitmap b2=Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
                Bitmap b2=Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mtx, false);
                bitmap.recycle();
            	bitmap = b2.copy(Bitmap.Config.ARGB_8888, true);
            	b2.recycle();
            	//Toast.makeText(MainActivity.this, "Уменьшите разрешение в настройках до 2 мегапикселей!", Toast.LENGTH_LONG).show();
				Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.message_decrease_resolution), Snackbar.LENGTH_LONG).show();
        	} else
        	{
        		Bitmap b2 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        		bitmap.recycle();
        		bitmap=b2;
        	}
            
    	    int min_size=Math.min(bitmap.getWidth(), bitmap.getHeight());
    	    Rect bounds=new Rect();
        	printTextToBitap(bitmap, timeString, (int)(min_size/20.0f), 50, 50, bounds);
        	
            FileOutputStream out = new FileOutputStream(imageOutFile);
            // TODO
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);  
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            bitmap.recycle();
            return true;
        }  
        catch (FileNotFoundException e)   
        {  
            //e.printStackTrace();
        	//Toast.makeText(MainActivity.this, "File " + imageLoc.toString() + " can't be found !", Toast.LENGTH_LONG).show();
			Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.error_file_not_found, imageLoc.toString()), Snackbar.LENGTH_LONG).show();
        }  
        catch (IOException e)   
        {  
           //e.printStackTrace();
        	//Toast.makeText(MainActivity.this, "File " + imageLoc.toString() + " can't be read !", Toast.LENGTH_LONG).show();
			Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.error_cant_read_file, imageLoc.toString()), Snackbar.LENGTH_LONG).show();
        }
        catch (Exception e)
        {  
            //e.printStackTrace();
			Snackbar.make(activity.findViewById(android.R.id.content), activity.getString(R.string.error_raised_exception), Snackbar.LENGTH_LONG).show();
        }
        return false;
    }    
	
    public static String getImagePath(Activity activity, Uri uri) {
        String selectedImagePath;
        // 1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
			cursor.close();
        } else {
            selectedImagePath = null;
        }

        if (selectedImagePath == null) {
            // 2:OI FILE Manager --- call method: uri.getPath()
            selectedImagePath = uri.getPath();
        }
        return selectedImagePath;
    }

	public static boolean checkImagePath(Activity activity, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
			cursor.close();
			return column_index>0;
		}

		return true;
	}


}
