package com.longher.www.productmemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnOpenDB;
    TextView tvDbName;

    private static final String PREF_NAME = "pref";
    private static final String LAST_OPEN_DB_NAME = "last_open_db_name";
    private static final String LAST_OPEN_DB_VERSION = "last_open_db_version";

    public static final int READ_REQUEST_CODE = 501;

    private String loadLastOpenDbName()
    {
        SharedPreferences pref = getSharedPreferences( PREF_NAME, MODE_PRIVATE );
        String strDB = pref.getString(LAST_OPEN_DB_NAME, "");
        Log.d( TAG, "Load last DB Name = " + strDB.toString() );
        return strDB;
    }

    private void saveLastOpenDbName( String strDB )
    {
        SharedPreferences pref = getSharedPreferences( PREF_NAME, MODE_PRIVATE );
        pref.edit().putString(LAST_OPEN_DB_NAME, strDB ).apply();
        Log.d( TAG, "Save last DB Name = " + strDB.toString() );
    }

    private int loadLastOpenDbVersion()
    {
        SharedPreferences pref = getSharedPreferences( PREF_NAME, MODE_PRIVATE );
        int iVersion = pref.getInt(LAST_OPEN_DB_VERSION, 1 );
        Log.d( TAG, "Load last DB Version = " + iVersion );
        return iVersion;
    }

    private void saveLastOpenVersion( int iVersion )
    {
        SharedPreferences pref = getSharedPreferences( PREF_NAME, MODE_PRIVATE );
        pref.edit().putInt(LAST_OPEN_DB_VERSION, iVersion ).apply();
        Log.d( TAG, "Save last DB Version = " + iVersion );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
    }

    void findViews()
    {
        tvDbName = findViewById( R.id.tvDbName );

        String strDB = loadLastOpenDbName();
        tvDbName.setText( strDB );

        tvDbName.setOnClickListener( new View.OnClickListener(){
             @Override
             public void onClick(View view) {
                 pickDbPath();
             }
        });

        btnOpenDB = findViewById( R.id.btnOpenDB );
        btnOpenDB.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openDb();
            }
        });
    }

    void openDb()
    {
        String strDB = tvDbName.getText().toString();
        if( strDB == null || strDB.equals(""))
        {
            Common.showAlertDialog( MainActivity.this, getString( R.string.prompt_error ), getString( R.string.select_db));
            return;
        }

        if( isDbOK( strDB ) )
        {
            Log.d( TAG, "Open DB: " + strDB );
            Common.setDbName( strDB );
            Intent intent = new Intent(MainActivity.this, SearchActivity.class );
            startActivity(intent);
        }
        else
        {
            Common.showAlertDialog( MainActivity.this, getString( R.string.prompt_error ), getString( R.string.error_db));
        }
    }

    void pickDbPath()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType( "*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                // File.separator + "ProductMemo.sqlite3"
                tvDbName.setText( uri.toString() );
            }
        }
    }


    boolean isDbOK( String strDB )
    {
        SQLiteDatabase db;

        try {
            db = SQLiteDatabase.openDatabase( strDB, null, SQLiteDatabase.OPEN_READONLY);
            if (db == null)
                return false;
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
