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
    private static final String DB_NAME = "ProductMemo";
    private static final int DB_VERSION = 1;
    
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

    // 存放瀏覽過的歷史紀錄, 省得重複掃描查詢, 可以快速查詢
    // 更新原則, 如果有透過 Scan or Input barcode 做的 Search. 新增 or 讀取
    private static final String COL_idx = "idx";

    private static final String HISTORY_TABLE_NAME = "history_table";
    private static final String CREATE_HISTORY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE_NAME + " ( " +
                    COL_idx + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_barcode + " TEXT NOT NULL, " +
                    COL_name + " TEXT, " +
                    COL_price + " REAL, " +
                    COL_picture + " BLOB ); ";


    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d( "MySQLiteOpenHelper", "Create DB" );
        db.execSQL(CREATE_PRODUCT_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + PRODUCT_TABLE_NAME );
        //db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_NAME );

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

    final private String SELECT_TOP_N_SEARCH_RECORD = "SELECT "
            + COL_barcode + ", "
            + COL_name + " , "
            + COL_price + ", "
            + COL_picture
            + " FROM " + HISTORY_TABLE_NAME
            + " ORDER BY " + COL_idx
            + " DESC LIMIT ";

    public List<SearchRecord> getHistoryRecords( int nMax ) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery( SELECT_TOP_N_SEARCH_RECORD + Integer.toString( nMax ), null );
        List<SearchRecord> historyList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String barcode = cursor.getString(0);
            String name = cursor.getString(1);
            double price = cursor.getDouble(2);
            byte[] image = cursor.getBlob(3);
            SearchRecord r = new SearchRecord( barcode, name, price, image );
            historyList.add(r);
        }
        cursor.close();
        return historyList;
    }

    public int deleteSearchByBarcode(String barcode) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_barcode + " = ?;";
        String[] whereArgs = {barcode};
        return db.delete(HISTORY_TABLE_NAME, whereClause, whereArgs);
    }

    public long insertSearchRecord( SearchRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_barcode, r.getBarcode());
        values.put(COL_name, r.getName());
        values.put(COL_price, r.getPrice());
        values.put(COL_picture, r.getImage());
        db.insert( HISTORY_TABLE_NAME, null, values );
        refreshSearchRecord(Common.MAX_OF_RECENT_SEARCH_RECORD);
        return 0;
    }

    // Update = Delete then Insert
    public long updateOrInsertSearchRecord(SearchRecord r ) {
        deleteSearchByBarcode( r.getBarcode() );
        insertSearchRecord( r );
        refreshSearchRecord(Common.MAX_OF_RECENT_SEARCH_RECORD);
        return 0;
    }

    public void refreshSearchRecord( int nReserved ) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL( "DELETE FROM " + HISTORY_TABLE_NAME
                + " WHERE " + COL_idx
                + " IN ( SELECT " + COL_idx + " FROM " + HISTORY_TABLE_NAME
                + " ORDER BY " + COL_idx + " DESC LIMIT -1 OFFSET "
                + Integer.toString( nReserved ) + ")" );
    }

}
