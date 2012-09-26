package com.example.scope;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Cropping extends Activity {
	private static final String TAG = "Scope.java";
	public Bitmap myimage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "enter...");
		setContentView(R.layout.cropping);
		Log.v(TAG, "creating...");
		String filepath = getIntent().getStringExtra("file_path");
		Bitmap yourSelectedImage = BitmapFactory.decodeFile(filepath);
		ImageView imageView = (ImageView) findViewById(R.id.imgView);
		imageView.setImageBitmap(yourSelectedImage);
		crop_here(yourSelectedImage);
	}

	public void crop_here(Bitmap image) {
/*
	 final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
 
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
 
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
 
        int size = list.size();
 
        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();
 
            return;
        } else {
            intent.setData(mImageCaptureUri);
 
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
 
            if (size == 1) {
                Intent i        = new Intent(intent);
                ResolveInfo res = list.get(0);
 
                i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
 
                    co.title    = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon     = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent= new Intent(intent);
 
                    co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 
                    cropOptions.add(co);
                }
 
                CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
 
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int item ) {
                        startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });
 
                builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel( DialogInterface dialog ) {
 
                        if (mImageCaptureUri != null ) {
                            getContentResolver().delete(mImageCaptureUri, null, null );
                            mImageCaptureUri = null;
                        }
                    }
                } );
 
                AlertDialog alert = builder.create();
 
                alert.show();
            }
        }	
*/
	}
}