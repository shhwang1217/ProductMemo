package com.longher.www.productmemo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Common {
    static final int MAX_OF_RECENT_SEARCH_RECORD = 5;

    static void showAlertDialog(Context ctx, String title, String message )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( ctx );
        builder.setTitle(title).setMessage(message).setCancelable(false)
                .setPositiveButton( R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });

        builder.create().show();
    }

    static void showToast(Context ctx, String message )
    {
        Toast.makeText( ctx, message, Toast.LENGTH_SHORT ).show();
    }


    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 0;
    public static final int IMG_BUTTON_SIZE = 256;
    public static final int IMG_SMALL_SIZE = 32;

    private static final String TAG = "Common";

    public static Bitmap downSize(Bitmap srcBitmap, int newSize) {
        if (newSize <= 1) {
            // 如果欲縮小的尺寸過小，就直接定為128
            newSize = IMG_BUTTON_SIZE;
        }
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        String text = "source image size = " + srcWidth + "x" + srcHeight;
        Log.d(TAG, text);
        int longer = Math.max(srcWidth, srcHeight);

        if (longer > newSize) {
            double scale = longer / (double) newSize;
            int dstWidth = (int) (srcWidth / scale);
            int dstHeight = (int) (srcHeight / scale);
            srcBitmap = Bitmap.createScaledBitmap(srcBitmap, dstWidth, dstHeight, false);
            System.gc();
            text = "\nscale = " + scale + "\nscaled image size = " +
                    srcBitmap.getWidth() + "x" + srcBitmap.getHeight();
            Log.d(TAG, text);
        }
        return srcBitmap;
    }

    public static Bitmap binToBitmap( byte [] picture )
    {
        if( picture != null && picture.length > 0 ) {
            return BitmapFactory.decodeByteArray(picture, 0,
                    picture.length);
        }
        return null;
    }

    public static byte [] pictureToThumbnail( byte [] picture )
    {
        if( picture == null )
            return null;

        Bitmap bitmap = binToBitmap( picture );

        if( bitmap == null )
            return null;

        bitmap = Common.downSize( bitmap, Common.IMG_SMALL_SIZE );
        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.PNG, 100, baos );
            return baos.toByteArray();
        }

        return null;
    }

    public static Bitmap rotateImageIfRequired( Context context, Bitmap img, Uri selectedImageUri)
    {
        int rotation = getRotation(context, selectedImageUri );
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }
        return img;
    }

    private static int getRotation(Context context,Uri selectedImage) {
        int rotation = 0;
        ContentResolver content = context.getContentResolver();
        Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { "orientation", "date_added" },
                null, null, "date_added desc");

        if (mediaCursor != null && mediaCursor.getCount() != 0) {
            while(mediaCursor.moveToNext()){
                rotation = mediaCursor.getInt(0);
                break;
            }
        }
        mediaCursor.close();
        return rotation;
    }

    private static final int MAX_HEIGHT = Common.IMG_BUTTON_SIZE;  //1024;
    private static final int MAX_WIDTH = Common.IMG_BUTTON_SIZE;   //1024;

    public static Bitmap decodeSampledBitmap(Context context, Uri selectedImage)
            throws IOException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired( context, img, selectedImage);
        return img;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }


    // New Permission see Appendix A
    public static void askPermissions(Activity activity, String[] permissions, int requestCode) {
        Set<String> permissionsRequest = new HashSet<>();
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsRequest.add(permission);
            }
        }

        if (!permissionsRequest.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    permissionsRequest.toArray(new String[permissionsRequest.size()]),
                    requestCode);
        }
    }



}
