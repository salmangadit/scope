package com.example.scope;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class UploadFragment extends Fragment {

	// Initial Declarations
	private static int RESULT_LOAD_IMAGE = 1;
	// setContentView(R.layout.uploadfragment.xml);
	private static final String TAG = "Scope.java";
	protected static ImageButton _button;
	public final int RESULT_OK = -1;
    public static Bitmap myimage;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate layout for upload fragment
		View view = inflater.inflate(R.layout.uploadfragment, container, false);
		upload_file(view);
		//globe= getView();
		return view;
	}

	// Listening for button events
	public void upload_file(View view) {

		_button = (ImageButton) view.findViewById(R.id.image_upload);
		_button.setOnClickListener(new ButtonClickHandler());

	}

	// Upon button click
	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			Log.v(TAG, "Gallery button pressed");
			Log.v(TAG, "1.. Pass");
			// Activity to open Gallery
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			Log.v(TAG, "2.5.. Pass");
			startActivityForResult(i, RESULT_LOAD_IMAGE);
			Log.v(TAG, "3.. Pass");

		}
	}

	// To save the chosen image
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "4.. Pass");
		super.onActivityResult(requestCode, resultCode, data);
		Log.v(TAG, "5.. Pass");
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Log.v(TAG, "6.. Pass");
			Log.v(TAG,selectedImage.toString());

			// IMP : Getting global activity status to use getContentresolver()
			// under a fragment
			Context a = getActivity();
			Log.i(TAG, "Activity: " + a);
			Log.v(TAG, "not screwed");

			Cursor cursor = a.getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			Log.v(TAG, "7.5.. Pass");
			cursor.moveToFirst();
			Log.v(TAG, "8.. Pass");

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();
			Log.v(TAG, "9.. Pass");
			Log.v(TAG, filePath);
 
			//Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
			//Log.v(TAG, "Image Selected");
		    //myimage=yourSelectedImage;	
			
			Intent intent = new Intent(a, CropScreen.class);
			intent.putExtra("file_path", filePath);
			intent.putExtra("image_uri",selectedImage.toString() );
			startActivity(intent);
		    
		}
	}


}
