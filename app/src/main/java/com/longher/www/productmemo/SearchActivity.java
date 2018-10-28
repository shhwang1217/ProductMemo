package com.longher.www.productmemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SearchActivity extends AppCompatActivity {
    Button btnSearch;
    ImageButton imgBtnScan;
    EditText etBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
    }

    void findViews()
    {
        etBarcode = (EditText) findViewById(R.id.etBarcode);

        imgBtnScan = (ImageButton) findViewById(R.id.imgBtnScan);
        imgBtnScan.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doScanBarcode( view );
            }
        });

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doSearch();
            }
        });

    }

    void dealProductBarcode( String strBarcode )
    {
        MySQLiteOpenHelper sql = new MySQLiteOpenHelper(this);

        ProductRecord r = sql.findProductByBarcode( strBarcode );
        if( r == null )
        {
            Log.d( "SearchActivity", "Insert for new barcode = " + strBarcode );
            Intent intent = new Intent(this, ModifyRecordActivity.class );
            intent.putExtra( "barcode", strBarcode );
            intent.putExtra( "isInsert", true );
            startActivity(intent);
        }
        else
        {
            Log.d( "SearchActivity", "Show existed barcode = " + strBarcode );
            Intent intent = new Intent(this, ProductRecordActivity.class );
            intent.putExtra( "barcode", strBarcode );
            startActivity(intent);
        }

        sql.close();
        sql = null;
    }

    void doSearch()
    {
        String strBarcode = etBarcode.getText().toString();
        if( strBarcode == "" )
        {
            MyUtil.showAlertDialog( this, getString( R.string.prompt_error ), getString( R.string.error_without_barcode) );
            return;
        }
        dealProductBarcode( strBarcode );
        Log.d( "SearchActivity", "dealProductBarcode: barcode = " + strBarcode );
    }

    void doScanBarcode( View view )
    {
        Log.d( "SearchActivity", "Scan barcode begin" );

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(true);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                // Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                // etBarcode.setText( result.getContents().toString() );
                dealProductBarcode( result.getContents().toString() );
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
            //Toast.makeText(this, "Scanned NG", Toast.LENGTH_LONG).show();
        }
    }

}
