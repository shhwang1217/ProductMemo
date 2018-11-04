package com.longher.www.productmemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ProductRecordActivity extends AppCompatActivity {
    MySQLiteOpenHelper sql;

    String strBarcode;

    Button btnEdit;
    Button btnClose;
    Button btnDelete;

    ImageView imgPic;

    ImageView imgBtnShow;

    TextView tvBarcode;
    TextView tvName;

    TextView tvPrice;
    TextView tvCost;

    ProductRecord rec;

    boolean isFromHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_record);

        Intent i = getIntent();
        strBarcode = i.getStringExtra( "barcode" );
        isFromHistory = i.getBooleanExtra( "isFromHistory", false );

        Log.d( "ProductRecordActivity", "Barcode = " + strBarcode + ", isFromHistory = " + isFromHistory );

        if( strBarcode == "" )
        {
            Log.d( "ProductRecordActivity", "Get null barcode" );
            finish();
            return;
        }

        if (sql == null) {
            sql = new MySQLiteOpenHelper(this);
        }

        if( sql == null )
        {
            Log.d( "ProductRecordActivity", "DB NG" );
            finish();
            return;
        }

        rec = sql.findProductByBarcode( strBarcode );
        if( rec == null )
        {
            Log.d( "ProductRecordActivity", "Not found barcode " + strBarcode );
            finish();
            return;
        }

        findViews();
    }

    void findViews()
    {
        btnEdit = (Button) findViewById(R.id.btnEdit );
        btnEdit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                doEditProduct( view );
            }
        });

        btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if( !isFromHistory &&  rec != null )
                {   // Not from History => From Search
                    byte[] thumbnail = Common.pictureToThumbnail(rec.getPicture());
                    SearchRecord r = new SearchRecord(rec.getBarcode(), rec.getName(), rec.getPrice(), thumbnail);
                    sql.updateOrInsertSearchRecord(r);
                }
                finish();
            }
        });

        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sql.deleteProductByBarcode(strBarcode);
                sql.deleteSearchByBarcode(strBarcode);
                finish();
            }
        });

        imgPic = (ImageView) findViewById(R.id.imgBtnSnapshot);
        Bitmap bitmap = Common.binToBitmap( rec.getPicture() );
        if( bitmap != null )
            imgPic.setImageBitmap( bitmap );

        tvBarcode = (TextView) findViewById(R.id.txtBarcode);
        tvBarcode.setText( strBarcode );

        tvName = (TextView) findViewById(R.id.txtName);
        tvName.setText( rec.getName() );

        tvPrice = (TextView) findViewById(R.id.txtPrice );
        tvPrice.setText( Double.toString( rec.getPrice() ) );

        tvCost = (TextView) findViewById(R.id.txtCost );
        tvCost.setText( "?" );

        imgBtnShow = (ImageView) findViewById(R.id.imgBtnShow);
        imgBtnShow.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if( tvCost.getText().equals("?") )
                    tvCost.setText( Double.toString( rec.getCost() ) );
                else
                    tvCost.setText("?");
            }
        });
    }

    void doEditProduct( View view )
    {
        Log.d( "SearchActivity", "Insert for new barcode = " + strBarcode );
        Intent intent = new Intent(this, ModifyRecordActivity.class );
        intent.putExtra( "barcode", strBarcode );
        intent.putExtra( "isInsert", false ); // Update
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( sql != null) {
            sql.close();
        }
    }
}
