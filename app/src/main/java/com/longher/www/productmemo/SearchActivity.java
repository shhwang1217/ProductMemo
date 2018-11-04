package com.longher.www.productmemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    Button btnSearch;
    ImageView imgBtnScan;
    EditText etBarcode;

    ListView lvHistory;
    final List<SearchRecord> recList = new ArrayList<>();
    SearchRecordAdapter adapter;

    MySQLiteOpenHelper sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d( "SearchActivity", "onCreate");
        setContentView(R.layout.activity_search);

        sql = new MySQLiteOpenHelper( this );
        if( sql == null )
        {
            Common.showAlertDialog( this, getString( R.string.prompt_error ), getString( R.string.error_db ));
            finish();
        }

        findViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d( "SearchActivity", "onStart: Refresh ListView");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d( "SearchActivity", "onResume: Refresh ListView");
        // Refresh lvView if anyt

        if( adapter != null )
            adapter.updateHistory();

        if( etBarcode != null )
            etBarcode.setText( "" );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( sql != null) {
            sql.close();
        }
    }

    void findViews()
    {
        etBarcode = (EditText) findViewById(R.id.etBarcode);

        imgBtnScan = (ImageView) findViewById(R.id.imgBtnScan);
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

        // Search Record List
        lvHistory = (ListView) findViewById(R.id.lvHistory );

        adapter = new SearchRecordAdapter(this, recList );
        lvHistory.setAdapter( adapter );

        lvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                SearchRecord rec = (SearchRecord) parent.getItemAtPosition(position);
                Log.d( "SearchActivity", "Remove barcode = " + rec.getBarcode() );
                sql.deleteSearchByBarcode( rec.getBarcode() ); // Just Delete Search Record, DON'T delete Product
                recList.remove( position );
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                SearchRecord rec = (SearchRecord) parent.getItemAtPosition(position);
                // Search barcode
                String barcode = rec.getBarcode();
                if( barcode.equals("") )
                    return;

                Log.d( "SearchActivity", "Search barcode = " + rec.getBarcode() );
                dealProductBarcode( barcode, true );
            }
        });

    }

    void dealProductBarcode( String strBarcode, boolean isFromHistory )
    {
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
            intent.putExtra( "isFromHistory", isFromHistory );
            startActivity(intent);
        }
    }

    void doSearch()
    {
        String strBarcode = etBarcode.getText().toString();
        if( strBarcode == "" )
        {
            Common.showAlertDialog( this, getString( R.string.prompt_error ), getString( R.string.error_without_barcode) );
            return;
        }
        dealProductBarcode( strBarcode, false );
        Log.d( "SearchActivity", "dealProductBarcode: barcode = " + strBarcode );
    }

    void doScanBarcode( View view )
    {
        Log.d( "SearchActivity", "Scan barcode begin" );

        IntentIntegrator integrator = new IntentIntegrator(this);
        //integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setOrientationLocked(true);
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
                dealProductBarcode( result.getContents().toString(), false ); // From Scanner
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
            //Toast.makeText(this, "Scanned NG", Toast.LENGTH_LONG).show();
        }
    }


    private class SearchRecordAdapter extends BaseAdapter {
        Context context;
        List<SearchRecord> recList;

        SearchRecordAdapter(Context context, List<SearchRecord> recList) {
            this.context = context;
            this.recList = recList;
        }

        @Override
        public int getCount() {
            return recList.size();
        }

        @Override
        public long getItemId(int i) {
            return 0; // Ignored
        }

        @Override
        public Object getItem(int i) {
            return recList.get(i);
        }

        @Nullable
        @Override
        public CharSequence[] getAutofillOptions() {
            return new CharSequence[0];
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parent) {
            if (itemView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                itemView = layoutInflater.inflate(R.layout.product_item_view, parent, false);
            }

            SearchRecord rec = recList.get( position );

            byte [] image = rec.getImage();
            if( image != null )
            {
                ImageView ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                ivImage.setImageBitmap( Common.binToBitmap( image ) );
            }

            TextView tvId = (TextView) itemView.findViewById(R.id.tvId);
            tvId.setText( rec.getBarcode() );

            TextView tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvName.setText(rec.getName());

            TextView tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            tvPrice.setText( Double.toString( rec.getPrice() ) );

            return itemView;
        }

        public void updateHistory()
        {
            Log.d( "SearchRecordAdapter", "updateHistory");
            MySQLiteOpenHelper sql = new MySQLiteOpenHelper( getBaseContext() );
            if( sql != null )
            {
                List<SearchRecord> list = sql.getHistoryRecords( Common.MAX_OF_RECENT_SEARCH_RECORD );
                if( list != null )
                {
                    recList.clear();
                    recList.addAll( list );
                    notifyDataSetChanged();
                }
            }

        }
    }
}
