package com.longher.www.productmemo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    //private static String DB_NAME = "ProductMemo";
    //private static int DB_VERSION = 1;
    
    // 商品表
    private static final String PRODUCT_TABLE_NAME = "product_table";
    
    private static final String COL_barcode = "barcode";  // 商品條碼, 搜尋用
    private static final String COL_name = "name"; // 商品名稱
    private static final String COL_cost = "cost"; // 進貨成本
    private static final String COL_price  = "price"; // 建議售價
    private static final String COL_picture = "picture"; // 照片縮圖
   
    private static final String CREATE_PRODUCT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + PRODUCT_TABLE_NAME + " ( " +
                    COL_barcode + " TEXT NOT NULL PRIMARY KEY, " +
                    COL_name + " TEXT NOT NULL, " +
                    COL_cost + " REAL, " +
                    COL_price + " REAL, " +
                    COL_picture + " BLOB ); ";

    public MySQLiteOpenHelper(Context context, String strDbName, int iVersion ) {
        super(context, strDbName, null, iVersion );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d( "MySQLiteOpenHelper", "Create DB" );
        db.execSQL(CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <  2) {
            //upgradeVersion2(db);
            // e.g. =>  db.execSQL("ALTER TABLE $DATABASE_TABLE ADD COLUMN $KEY_VALUE INTEGER DEFAULT 0;")
        }
        if (oldVersion <  3) {
            //upgradeVersion3(db);

        }
        if (oldVersion <  4) {
            //upgradeVersion4(db);
        }

        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
    }

    public ProductRecord findProductByBarcode(String barcode) {
        SQLiteDatabase db = getReadableDatabase();

        String[] productCols = { // 不需要 Barcode, 只會找到一筆
                COL_name, //COL_vendor, COL_category,
                COL_cost, COL_price, COL_picture
        };
        String selection = COL_barcode + " = ?;";
        String[] selectionArgs = { barcode };
        Cursor cursor = db.query( PRODUCT_TABLE_NAME, productCols, selection, selectionArgs,
                null, null, null);
        ProductRecord r = null;
        if (cursor.moveToNext()) {
            String name = cursor.getString(0);
            double cost = cursor.getDouble(1);
            double price = cursor.getDouble(2);
            byte[] picture = cursor.getBlob(3);
            r = new ProductRecord( barcode, name, cost, price, picture );
        }
        cursor.close();
        return r;
    }

    public long insertProduct( ProductRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_barcode, r.getBarcode());
        values.put(COL_name, r.getName());
        values.put(COL_cost, r.getCost());
        values.put(COL_price, r.getPrice());
        values.put(COL_picture, r.getPicture());
        return db.insert(PRODUCT_TABLE_NAME, null, values);
    }

    public int updateProduct( ProductRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_name, r.getName());
        values.put(COL_cost, r.getCost());
        values.put(COL_price, r.getPrice());
        values.put(COL_picture, r.getPicture());
        String whereClause = COL_barcode + " = ?;";
        String[] whereArgs = { r.getBarcode()};
        return db.update(PRODUCT_TABLE_NAME, values, whereClause, whereArgs);
    }

    public int deleteProductByBarcode(String barcode) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_barcode + " = ?;";
        String[] whereArgs = {barcode};
        return db.delete(PRODUCT_TABLE_NAME, whereClause, whereArgs);
    }
}
