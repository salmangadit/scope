package com.example.scope;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class ScanFragment extends Fragment {
	protected ImageButton _button;
	protected static String _path;
	protected boolean _taken;
	public static final String lang = "eng";
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/Scope/";
	private static final String TAG = "Scope.java";

	protected static final String PHOTO_TAKEN = "photo_taken";
	final int CAMERA_CAPTURE = 1;
	final int PIC_CROP = 2;
	private Uri outputFileUri;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.scanfragment, container, false);

		// _image = (ImageView) findViewById(R.id.image);
		// _field = (EditText) findViewById(R.id.field);
		_button = (ImageButton) view.findViewById(R.id.image_camera);

		Log.v(TAG, "HERE1");
		if (_button == null)
			Log.v(TAG, "Null bitch");
		_button.setOnClickListener(new ButtonClickHandler());
		_path = DATA_PATH + "/ocr.jpg";
		Log.v(TAG, "HERE2");
		return view;
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			Log.v(TAG, "Starting Camera app");
			startCameraActivity();
		}
	}

	public void startCameraActivity() {
		File file = new File(_path);
		outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, CAMERA_CAPTURE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "resultCode: " + resultCode);
		Log.i(TAG, "requestCode: " + requestCode);
		if (resultCode == -1) {
			// user is returning from capturing an image using the camera
			if (requestCode == CAMERA_CAPTURE) {
				// carry out the crop operation
				//performCrop();
				// Bundle extras = data.getExtras();
				// get the cropped bitmap
				// Bitmap thePic = extras.getParcelable("data");
			    onPhotoTaken();
			}
			// user is returning from cropping the image
			else if (requestCode == PIC_CROP) {
				// get the returned data
				//Bundle extras = data.getExtras();
				// get the cropped bitmap
				//Bitmap thePic = extras.getParcelable("data");
				//onPhotoTaken(thePic);
			}
		} else {
			Log.v(TAG, "User cancelled");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(MainActivity.PHOTO_TAKEN, _taken);
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(MainActivity.PHOTO_TAKEN)) {
			performCrop();
		}
	}

	private void performCrop() {
		try {
			// call the standard crop action intent (the user device may not
			// support it)
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(outputFileUri, "image/*");
			// set crop properties
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			cropIntent.putExtra("scale", true);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, PIC_CROP);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					errorMessage, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	protected void onPhotoTaken() {
		_taken = true;

		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inSampleSize = 4;
		Context context = getActivity().getApplicationContext();

		Bitmap bitmap = BitmapFactory.decodeFile(_path);
//		BitmapHandler bitmaphandler = new BitmapHandler(context);
//		Bitmap bitmap = bitmaphandler.decodeFileAsPath(_path);
		// destImage = sourceImage;
		//Bitmap bitmap = croppedPic;

//		if (bitmap == null)
//			Log.v(TAG, _path);
		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}
			Log.v(TAG, "1");
			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			Log.v(TAG, "2");
		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// Temporary storage for cropped image
		File file = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"temp.bmp");
		String filepath = file.getAbsolutePath();

		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Date now = new Date();
		EdgeDetection detector = new EdgeDetection(context, Uri.fromFile(file), filepath);
		Uri EdgeDetectedImage = detector.AutoRotation();
		
		Date after = new Date();
		Log.v(TAG, "Edge detection time: " + (after.getTime() - now.getTime()));
		
		
		// Passing intent over to Ocrmain class
		Intent intent = new Intent(context, CropScreen.class);
		// intent.putExtra("file_path", filePath);
		intent.putExtra("image_uri", Uri.fromFile(file).toString());
		intent.putExtra("file_path", EdgeDetectedImage.toString());
		startActivity(intent);
	}
}
