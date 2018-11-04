package com.longher.www.productmemo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ModifyRecordActivity extends AppCompatActivity {
    private static final int REQUEST_TAKE_PICTURE = 501;

    File file;
    Uri contentUri;

    MySQLiteOpenHelper sql;

    String strBarcode;
    boolean isInsert = false;

    ImageView imgBtnSnapshot;

    Button btnConfirm;
    Button btnCancel;

    TextView tvBarcode;

    EditText etName;
    EditText etPrice;
    EditText etCost;

    // Spinner
    ProductRecord rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_record);

        Intent i = getIntent();
        strBarcode = i.getStringExtra( "barcode" );
        isInsert = i.getBooleanExtra( "isInsert", false);

        if( strBarcode == "" )
        {
            Log.d( "ModifyRecordActivity", "Get null barcode" );
            finish();
            return;
        }

        if (sql == null) {
            sql = new MySQLiteOpenHelper(this);
        }

        if( sql == null )
        {
            Log.d( "ModifyRecordActivity", "DB NG" );
            finish();
            return;
        }

        if( isInsert )
        {
            rec = new ProductRecord( strBarcode );
        }
        else
        {
            rec = sql.findProductByBarcode( strBarcode );
            if( rec == null )
            {
                Log.d( "ModifyRecordActivity", "Not found barcode " + strBarcode );
                finish();
                return;
            }
        }

        findViews();
    }

    void findViews()
    {
        imgBtnSnapshot = (ImageView) findViewById( R.id.imgBtnSnapshot );

        Bitmap bitmap = Common.binToBitmap( rec.getPicture() );
        if( bitmap != null )
            imgBtnSnapshot.setImageBitmap( bitmap );

        imgBtnSnapshot.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doTakePicture( view );
            }
        });

        btnConfirm = (Button) findViewById( R.id.btnConfirm);
        btnConfirm.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doConfirm( view );
            }
        });

        btnCancel = (Button) findViewById( R.id.btnCancel);
        btnCancel.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvBarcode = (TextView) findViewById( R.id.txtBarcode );
        tvBarcode.setText( strBarcode );

        etName = (EditText) findViewById( R.id.etName );
        etName.setText( rec.getName() );

        etPrice = (EditText) findViewById( R.id.etPrice );
        etPrice.setText( Double.toString( rec.getPrice() ) );

        etCost = (EditText) findViewById( R.id.etCost );
        etCost.setText( Double.toString( rec.getCost() ) );
    }

    void doConfirm( View view )
    {
        Log.d( "ModifyRecordActivity", "Confirm begin, isInsert " + isInsert + " for barcode " + strBarcode );

        // Bar code has been  set
        rec.setName( etName.getText().toString() );
        rec.setCost( Double.parseDouble( etCost.getText().toString() ) );
        rec.setPrice( Double.parseDouble( etPrice.getText().toString() ) );

        // rec has been set if take a picture, or let it be null.
        // Picture is set after take a picture

        if( isInsert )
            sql.insertProduct( rec );
        else
            sql.updateProduct( rec );

        Log.d( "ModifyRecordActivity", "Confirm end, isInsert " + isInsert + " for barcode " + strBarcode );

        // New SearchRecord
        byte[] thumbnail = Common.pictureToThumbnail(rec.getPicture());

        SearchRecord r = new SearchRecord(rec.getBarcode(), rec.getName(), rec.getPrice(), thumbnail);
        sql.updateOrInsertSearchRecord(r);

        finish();
    }

    private boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PICTURE:
                    Log.d( "REQUEST_TAKE_PICTURE", "onActivityResult begin" );
/*
                    Bitmap bitmap = null;
                    try {
                        bitmap = Common.decodeSampledBitmap(this, contentUri );
                    } catch (IOException e) {
                        // e.printStackTrace();
                        Log.d( "REQUEST_TAKE_PICTURE", "onActivityResult exception" );
                    }
*/
                    Bitmap srcPicture = BitmapFactory.decodeFile(file.getPath());
                    Bitmap bitmap = Common.downSize(srcPicture, Common.IMG_BUTTON_SIZE );

                    if (bitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                        rec.setPicture( baos.toByteArray() );
                        imgBtnSnapshot.setImageBitmap( bitmap );
                    }
                    break;
                default:
                    break;
            }
        }
    }

    void doTakePicture( View view )
    {
        Log.d( "ModifyRecordActivity", "Take a picture");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        file = new File(file, "picture.jpg");
        contentUri = FileProvider.getUriForFile(
                this, getPackageName() + ".provider", file); // 不可用 Uri.fromFile
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        if (isIntentAvailable(this, intent)) {
            startActivityForResult(intent, REQUEST_TAKE_PICTURE);
        } else {
            Common.showAlertDialog( this, getString( R.string.prompt_error), "No Camera" );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( sql != null) {
            sql.close();
        }
    }
}
