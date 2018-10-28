package com.longher.www.productmemo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class MyUtil {
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
    private static final String TAG = "Common";

    public static Bitmap downSize(Bitmap srcBitmap, int newSize) {
        if (newSize <= 1) {
            // 如果欲縮小的尺寸過小，就直接定為128
            newSize = 128;
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
