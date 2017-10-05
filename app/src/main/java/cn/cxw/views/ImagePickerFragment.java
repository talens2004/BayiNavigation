/**
 * Copyright (c) 2014 China Telecom Corp. Ltd. All rights reserved.
 */

package cn.cxw.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;

import cn.cxw.armymap.R;


/**
 * A non-UI fragment to show operations of picking an image. It will ask user to
 * select which way to pick(take photo or pick photo) and guide user to crop
 * target image if need.
 */
public class ImagePickerFragment extends BasicFragment {
	/**
	 * No further Action after picking.
	 */
	public static final int FURTHER_ACTION_NONE = 0;
	/**
	 * Crop the picked image.
	 */
	public static final int FURTHER_ACTION_CROP = 1;
	/**
	 * Compress the picked image.
	 */
	public static final int FURTHER_ACTION_COMPRESS = 2;

	private static final String TAG = "ImagePickerFragment";
	private static final String TAG_PICK_IMAGE = "tag_pick_image";
	private static final String TAG_PICK_OPTIONS = "tag_pick_options";
	private static final String IMAGE_CONTENT_TYPE = "image/*";
	private static final String EXTRA_KEY_RETURN_DATA = "return-data";
	private static final String STR_ARG_SELECTOR_TITLE = "selector_title";
	private static final String INT_ARG_FURTHER_ACTION = "further_action";
	private static final String BOOL_ARG_STATE_SAVED = "state_saved";
	private static final String IMAGE_FILE_NAME = "image_target.jpg";
	private static final String IMAGE_TEMP_FILE_NAME = "image_temp.jpg";
	private static final File DEFAULT_DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	private static final int REQUEST_CODE_PICK_PHOTO = 1;
	private static final int REQUEST_CODE_TAKE_PHOTO = 2;
	private static final int REQUEST_CODE_CROP_PHOTO = 3;
	private static final int POS_TAKE_PHOTO = 0;
	private static final int POS_PICK_PHOTO = 1;
	private static final int OUTPUT_AVATAR_SIZE = 300;
	private static final int PICK_IMAGE_MAX_SIZE = 1024;
	private static final int MAX_AVATAR_WH = 150;

	private static OnImagePickFinishListener mListener;
	AlertDialog mAlertDialog;

	/**
	 * Interface to receive pick finish event, Fragment user should
	 * implement this interface.
	 */
	public interface OnImagePickFinishListener {
		/**
		 * Called while image pick finished.
		 * 
		 * @param bitmap
		 *                picked bitmap if success otherwise null.
		 */
		void onPickFinish(Bitmap bitmap);
	}

	/**
	 * Show a {@link ImagePickerFragment} to let user pick an image.
	 * 
	 * @param fragment
	 *                Fragment to show operations.
	 * @param title
	 *                the title to be displayed while selecting the way to
	 *                pick.
	 */
	public static void show(int contentId, Fragment fragment, String title, OnImagePickFinishListener listener) {
		if (fragment == null) {
			return;
		}
		FragmentTransaction ft = fragment.getFragmentManager().beginTransaction();
		show(contentId, ft, title, listener, FURTHER_ACTION_NONE);
	}

	/**
	 * 从Activity调用时传入
	 * 
	 * @param title
	 * @param listener
	 */
	public static void show(int contentId, FragmentActivity fmActivity, String title, OnImagePickFinishListener listener) {
		if (fmActivity == null) {
			return;
		}
		FragmentTransaction ft = fmActivity.getSupportFragmentManager().beginTransaction();
		show(contentId, ft, title, listener, FURTHER_ACTION_NONE);
	}

	/**
	 * Show a {@link ImagePickerFragment} to let user pick an image.
	 * 
	 * @param ft
	 *                the Fragment to show operations.
	 * @param title
	 *                the title to be displayed while selecting the way to
	 *                pick.
	 * @param furtherAction
	 *                the further action need to do after picking image.
	 *                Should be one of {@link #FURTHER_ACTION_NONE},
	 *                {@link #FURTHER_ACTION_CROP} and
	 *                {@link #FURTHER_ACTION_COMPRESS}
	 */
	public static void show(int contentId, FragmentTransaction ft, String title, OnImagePickFinishListener listener, int furtherAction) {
		mListener = listener;
		Bundle args = new Bundle();
		args.putString(STR_ARG_SELECTOR_TITLE, title);
		args.putInt(INT_ARG_FURTHER_ACTION, furtherAction);
		showWithArgs(contentId, ft, args);
	}

	private static void showWithArgs(int contentId, FragmentTransaction ft, Bundle args) {
		ImagePickerFragment fragment = new ImagePickerFragment();
		fragment.setArguments(args);
		ft.add(contentId, fragment, ImagePickerFragment.class.getName());
		ft.addToBackStack(null);
		ft.commit();
	}

	/**
	 * @return the file path to save image file.
	 */
	public static File getImageFile() {
		return new File(DEFAULT_DIRECTORY, IMAGE_FILE_NAME);
	}

	private Bitmap clipAndScaleBitmap(Bitmap bmp) {
		if (bmp == null) {
			return null;
		}
		Bitmap clippedBitmap;
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Matrix matrix = new Matrix();
		if (width > height) {
			int x = (width - height) / 2;
			float scaleWidth = ((float) MAX_AVATAR_WH) / height;
			float scaleHeight = ((float) MAX_AVATAR_WH) / height;
			matrix.postScale(scaleWidth, scaleHeight);
			clippedBitmap = Bitmap.createBitmap(bmp, x, 0, height, height, matrix, false);
		} else {
			int y = (height - width) / 2;
			float scaleWidth = ((float) MAX_AVATAR_WH) / width;
			float scaleHeight = ((float) MAX_AVATAR_WH) / width;
			matrix.postScale(scaleWidth, scaleHeight);
			clippedBitmap = Bitmap.createBitmap(bmp, 0, y, width, width, matrix, false);
		}
		return clippedBitmap;
	}

	private String getFilePathFrom(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		int indexData = 0;
		Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
		if (cursor == null) {
			return null;
		}

		String res = null;
		try {
			if (cursor.moveToFirst()) {
				res = cursor.getString(indexData);
			}
		} finally {
			cursor.close();
		}
		return res;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null || !savedInstanceState.getBoolean(BOOL_ARG_STATE_SAVED)) {
			showPickOptions();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode != REQUEST_CODE_CROP_PHOTO && requestCode != REQUEST_CODE_PICK_PHOTO && requestCode != REQUEST_CODE_TAKE_PHOTO) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
		}

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_PICK_PHOTO:
				Bitmap bmp = null;
				ContentResolver resolver = getActivity().getContentResolver();
				Uri originalUri = data.getData();
				if (originalUri != null) {
					String path = getFilePathFrom(originalUri);
					bmp = BitmapFactory.decodeFile(path);
				}
				mListener.onPickFinish(clipAndScaleBitmap(bmp));
				return;
			case REQUEST_CODE_TAKE_PHOTO:
				if (data == null)
					return;
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap photo = extras.getParcelable("data");
					if (photo != null) {
						mListener.onPickFinish(clipAndScaleBitmap(photo));
					}
				} else {
					Uri data1 = data.getData();
				}
			case REQUEST_CODE_CROP_PHOTO:
				break;
			}
		} else if (resultCode != Activity.RESULT_CANCELED) {
			mApp.showMessage("没有找到应用");
			Log.e(TAG, "Failed on Request:" + requestCode + " with ResultCode:" + resultCode);
		}
	}

	private void onActivityNotFound(Intent intent, String msg) {
		mApp.showMessage(msg);
		Log.e(TAG, "ActivityNotFoundException occurs on intent:" + intent.getAction());
	}

	private void pickPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");

		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
		} catch (ActivityNotFoundException e) {
			onActivityNotFound(intent, "无法选择图片");
		}
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try {
			startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
		} catch (ActivityNotFoundException e) {
			onActivityNotFound(intent, "无法启动相机");
		}
	}

	private void cropImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_CONTENT_TYPE);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", OUTPUT_AVATAR_SIZE);
		intent.putExtra("outputY", OUTPUT_AVATAR_SIZE);
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra(EXTRA_KEY_RETURN_DATA, false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getImageFile()));

		try {
			startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
		} catch (ActivityNotFoundException e) {
			onActivityNotFound(intent, "无法启动切图应用");
		}
	}

	private void showPickOptions() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		mAlertDialog = builder.create();
		mAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mAlertDialog = null;
				getActivity().getSupportFragmentManager().popBackStack();
			}
		});
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final View dialogView = inflater.inflate(R.layout.dialog_avatar_picker, null);
		mAlertDialog.setView(dialogView, 0, 0, 0, 0);

		(dialogView.findViewById(R.id.btn_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mAlertDialog.dismiss();
			}
		});

		(dialogView.findViewById(R.id.tv_take_photo)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				takePhoto();
			}
		});

		(dialogView.findViewById(R.id.tv_pick_photo)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				pickPhoto();
			}
		});
		mAlertDialog.show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(BOOL_ARG_STATE_SAVED, true);
	}
}
