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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Common {
    static final int MAX_OF_RECENT_SEARCH_RECORD = 5;

    private static final String TAG = "Common";

    // Default DB Name & Version
    private static String DB_NAME;
    private static int DB_VERSION = 1;

    public static String getCurrentDbName()
    {
        if( DB_NAME == null )
        {
            setDbName( "ProductMemo" );
        }
        return DB_NAME;
    }

    public static int getCurrentDbVersion()
    {
        if( DB_VERSION <= 0 )
            DB_VERSION = 1;
        return DB_VERSION;
    }

    public static void setDbName( String strDbName )
    {
        if( !strDbName.equals("") )
        {
            String DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
            DB_NAME = DATABASE_FILE_PATH + File.separator + strDbName;
            Log.d( TAG, "setDbName = " + DB_NAME );
        }
    }

    public static void setDbVersion()
    {
        DB_VERSION = 1;
    }

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

    static public int getRotateAngle( String strFileName )
    {
        String orientString;

        try {
            ExifInterface exifReader = new ExifInterface( strFileName );
            exifReader.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            orientString = exifReader.getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.d(TAG, "getRotateAngle: orientString = " + orientString);
        } catch (IOException e) {
            //e.printStackTrace();
            orientString = "0";
            Log.d(TAG, "getRotateAngle: exception orientString = 0" + orientString);
        }

        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        Log.d(TAG, "getRotateAngle: orientation = " + orientation);

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        Log.d(TAG, "getRotateAngle: rotationAngle = " + rotationAngle);

        return rotationAngle;
    }

    public static Bitmap rotateBitmap(Bitmap source, int angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        source.recycle();
        return result;
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
