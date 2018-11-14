package com.longher.www.productmemo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        Common.askPermissions(this, permissions, Common.PERMISSION_READ_EXTERNAL_STORAGE);


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

        boolean bnOK = false;

        try {
            MySQLiteOpenHelper sql = new MySQLiteOpenHelper( this,  strDB, 1 );
            sql.close();
            bnOK = true;
            saveLastOpenDbName( strDB );
            Common.setDbName( strDB );
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if( bnOK )
        {
            Log.d( TAG, "Open DB " + strDB );
            Intent intent = new Intent(this, SearchActivity.class );
            startActivity(intent);
        }
        else
        {
            Common.showAlertDialog( MainActivity.this, getString( R.string.prompt_error ), getString( R.string.error_db));
        }
    }

    void pickDbPath()
    {
        InputDialogFragment alertFragment = new InputDialogFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        alertFragment.show(fragmentManager, "alert");
    }

    public static class InputDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View v = inflater.inflate(R.layout.input_name_dialog, null);
            final EditText etDbName = (EditText) v.findViewById( R.id.etDbName );

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView( v )
                    // Add action buttons
                    .setTitle( R.string.select_db )
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity activity = (MainActivity) getActivity();
                            String strDB = Environment.getExternalStorageDirectory() + File.separator + etDbName.getText();
                            if( !strDB.endsWith(".sqlite3") )
                                strDB += ".sqlite3";
                            activity.tvDbName.setText( strDB );
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            InputDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    getActivity().finish();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.cancel();
                    break;
                default:
                    break;
            }
        }
    }

}
