package com.example.notedroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImagePicker {
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static final String KEY_CLASS_NAME = "ImagePicker";
    private static final String TEMP_IMAGE_NAME = "TempImage";
    private static File imageFile;
    public static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;

    /**
     * All pictures will come in the data pack, even for the camera.
     * instead of making the camera save to file, we can treat it as
     * coming from a gallery.
     *
     * @param context
     **/
    public static Intent getPickImageIntent(Context context) {
        imageFile = new File(context.getFilesDir(), TEMP_IMAGE_NAME);
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), "Pick up an image");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            if (!packageName.equals("com.google.android.GoogleCamera")) {
                list.add(targetedIntent);
                Log.d(KEY_CLASS_NAME, "Intent: " + intent.getAction() + " package: " + packageName);
            }
        }
        return list;
    }

    /**
     * Get Bitmap for the image from the given Intent.
     * Use inside onActivityResult.
     *
     * @param context
     * @param resultCode
     * @param imageReturnedIntent
     **/
    public static Bitmap getBitmapFromResult(Context context, int resultCode,
                                             Intent imageReturnedIntent) {
        Log.d(KEY_CLASS_NAME, "getBitmapFromResult, resultCode: " + resultCode);
        Bitmap bm = null;
        Uri selectedImage = null;
        if (resultCode == Activity.RESULT_OK) {
            boolean isCamera = (imageReturnedIntent == null ||
                    imageReturnedIntent.getData() == null ||
                    imageReturnedIntent.getData().toString().contains(imageFile.toString()));
            try {
                selectedImage = imageReturnedIntent.getData();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            Log.d(KEY_CLASS_NAME, "selectedImage: " + selectedImage);

            // convert to bitmap and resize
            bm = getImageResized(context, selectedImage);
            int rotation = getRotation(context, selectedImage, isCamera);
            bm = rotate(bm, rotation);
        }
        return bm;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert fileDescriptor != null;
        Bitmap actuallyUsableBitmap = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);

        Log.d(KEY_CLASS_NAME, options.inSampleSize + " sample method bitmap ... " +
                actuallyUsableBitmap.getWidth() + "*" + actuallyUsableBitmap.getHeight());

        return actuallyUsableBitmap;
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    private static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm = null;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            i++;
        } while (bm.getWidth() < minWidthQuality && i < sampleSizes.length);
        return bm;
    }

    private static int getRotation(Context context, Uri imageUri, boolean isCamera) {
        int rotation;
        if (isCamera) {
            rotation = getRotationFromCamera(context, imageUri);
        } else {
            rotation = getRotationFromGallery(context, imageUri);
        }
        Log.d(KEY_CLASS_NAME, "Image rotation: " + rotation);
        return rotation;
    }

    private static int getRotationFromCamera(Context context, Uri imageFile) {
        int rotate = 0;
        try {

            context.getContentResolver().notifyChange(imageFile, null);
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public static int getRotationFromGallery(Context context, Uri imageUri) {
        int result = 0;
        String[] columns = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int orientationColumnIndex = cursor.getColumnIndex(columns[0]);
                result = cursor.getInt(orientationColumnIndex);
            }
        } catch (Exception e) {
            //Do nothing
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }//End of try-catch block
        return result;
    }

    public static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }

    public static String writeImage(Context context, Bitmap image, String imageName) {

        File dir = new File(getImageFolder(context));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = getImageDir(context, imageName);

        byte[] byteArray = bitmapToByteArray(image);

        if (saveToInternalPrivately(fileName, byteArray)) {
            Log.d(KEY_CLASS_NAME, "writeImage: Path: " + fileName);
        } else {
            fileName = null;
        }
        return fileName;
    }

    public static Bitmap readImage(Context context, String imageName) {

        Bitmap bmImg = null;
        String imageDir = getImageDir(context, imageName);

        if (new File(imageDir).exists()) {
            Log.d(KEY_CLASS_NAME, "readImage: found image to use ");
            Log.d(KEY_CLASS_NAME, imageDir);
            bmImg = BitmapFactory.decodeFile(imageDir);
        }
        return bmImg;
    }


    private static String getImageDir(Context context, String imageName) {
        return context.getFilesDir().getPath()
                + "/storage/images"
                + "/" + imageName
                + ".jpeg";
    }

    private static String getImageFolder(Context context) {
        return context.getFilesDir().getPath()
                + "/storage/images";
    }

    private static byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } finally {
            try {
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stream.toByteArray();
    }

    private static boolean saveToInternalPrivately(String pathWithName, byte[] byteArray) {
        boolean done = false;
        try {
            FileOutputStream fos = new FileOutputStream(
                    new File(pathWithName), true); // true will be same as Context.MODE_APPEND
            try {
                fos.write(byteArray);
                fos.flush();
                done = true;
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            done = false;
        }
        return done;
    }

}