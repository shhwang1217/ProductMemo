package com.longher.www.productmemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnOpenDB;
    Button btnNewDB;

    private static final String PREF_NAME = "pref";
    private static final String LAST_OPEN_DB_NAME = "last_open_db_name";
    private static final String LAST_OPEN_DB_VERSION = "last_open_db_version";

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
        btnNewDB = findViewById( R.id.btnNewDB );
        btnNewDB.setOnClickListener( new View.OnClickListener(){
             @Override
             public void onClick(View view) {
                 createNewDb();
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
        String strDB = loadLastOpenDbName();
        int iVersion = loadLastOpenDbVersion();

        if( isDbOK( strDB ) )
        {
            Log.d( TAG, "Open DB: " + strDB );
            Common.setDbName( strDB );
            Common.setDbVersion( iVersion );

            Intent intent = new Intent(MainActivity.this, SearchActivity.class );
            startActivity(intent);
        }
        else
        {
            Common.showAlertDialog( MainActivity.this, getString( R.string.prompt_error ), getString( R.string.error_db));
        }

    }

    void createNewDb()
    {
        String path = Environment.getExternalStorageDirectory().getPath() + "/FOLDER/DB_FILE";

        Common.showToast( this, "Path = " + path );
    }

    void initDb( String strDB, int iVerison )
    {

    }

    boolean isDbOK( String strDB )
    {
        SQLiteDatabase db;
        db = SQLiteDatabase.openDatabase( strDB, null, SQLiteDatabase.OPEN_READONLY);
        if (db == null)
            return false;
        db.close();
        return true;
    }

}
