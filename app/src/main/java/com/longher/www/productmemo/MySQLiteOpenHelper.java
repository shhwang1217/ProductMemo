package com.longher.www.productmemo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ProductMemo";
    private static final int DB_VERSION = 1;
    
    // 商品表
    private static final String PRODUCT_TABLE_NAME = "product_table";
    
    private static final String COL_barcode = "barcode";  // 商品條碼, 搜尋用
    private static final String COL_name = "name"; // 商品名稱
    //private static final String COL_vendor = "vendor"; // 供應商
    //private static final String COL_category = "category"; // 分類
    private static final String COL_cost = "cost"; // 進貨成本
    private static final String COL_price  = "price"; // 建議售價
    private static final String COL_picture = "picture"; // 照片縮圖
   
    private static final String CREATE_PRODUCT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + PRODUCT_TABLE_NAME + " ( " +
                    COL_barcode + " TEXT NOT NULL PRIMARY KEY, " +
                    COL_name + " TEXT NOT NULL, " +
                    //COL_vendor + " TEXT, " +
                    //COL_category + " TEXT, " +
                    COL_cost + " REAL, " +
                    COL_price + " REAL, " +
                    COL_picture + " BLOB ); ";

    // 使用者帳號
    /*
    private static final String AUTHORITY_TABLE_NAME = "authority_table";
    
    private static final String COL_id = "id";  // // 使用者 ID
    private static final String COL_nickname = "nickname"; // 使用者暱稱
    private static final String COL_email = "email"; // 使用者 E-Mail, 即為帳號
    private static final String COL_datetime = "datetime";  // 建立帳號的時間, 以 ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS")
    private static final String COL_pwdhash = "pwdhash"; // 密碼 Hash, 用 create_datetime 搭配密碼 => pwdhash
    
    private static final String CREATE_AUTHORITY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + AUTHORITY_TABLE_NAME + " ( " +
                    COL_id + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    COL_nickname + " TEXT NOT NULL, " +
                    COL_email + " TEXT NOT NULL, " +
                    COL_datetime + " TEXT, " +                    
                    COL_pwdhash + " TEXT ); ";
*/

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PRODUCT_TABLE);
        //db.execSQL(CREATE_AUTHORITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCT_TABLE_NAME );
        //db.execSQL("DROP TABLE IF EXISTS " + AUTHORITY_TABLE_NAME );
        onCreate(db);
    }

    // 可能用不到
    /*
       public List<ProductRecord> getAllProductRecords() {
          SQLiteDatabase db = getReadableDatabase();

        String[] productCols = {
                COL_barcode, COL_name, COL_vendor, COL_category, COL_cost, COL_price, COL_picture
        };

        Cursor cursor = db.query(PRODUCT_TABLE_NAME, productCols, null, null, null, null,
                COL_barcode );

        List<ProductRecord> productList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String barcode = cursor.getString(0);
            String name = cursor.getString(1);
            String vendor = cursor.getString(2);
            String category = cursor.getString(3);
            double cost = cursor.getDouble(4);
            double price = cursor.getDouble(5);
            byte[] picture = cursor.getBlob(6);
            ProductRecord r = new ProductRecord( barcode, name, vendor, category, cost, price, picture );
            productList.add(r);
        }
        cursor.close();
        return productList;
      }
      */

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
/*
            String name = cursor.getString(0);
            String vendor = cursor.getString(1);
            String category = cursor.getString(2);
            double cost = cursor.getDouble(3);
            double price = cursor.getDouble(4);
            byte[] picture = cursor.getBlob(5);
            r = new ProductRecord( barcode, name, vendor, category, cost, price, picture ); */
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
        //values.put(COL_vendor, r.getVendor());
        //values.put(COL_category, r.getCategory());
        values.put(COL_cost, r.getCost());
        values.put(COL_price, r.getPrice());
        values.put(COL_picture, r.getPicture());
        return db.insert(PRODUCT_TABLE_NAME, null, values);
    }

    public int updateProduct( ProductRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_name, r.getName());
        //values.put(COL_vendor, r.getVendor());
        //values.put(COL_category, r.getCategory());
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

    /////////////////////////

/*
    public AuthorityRecord findAuthorityByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();

        String[] authorityCols = {
                COL_id, COL_nickname, COL_datetime, COL_pwdhash
        };

        String selection = COL_email + " = ?;";
        String[] selectionArgs = { email };
        Cursor cursor = db.query( AUTHORITY_TABLE_NAME, authorityCols, selection, selectionArgs,
                null, null, null);
        AuthorityRecord r = null;
        if (cursor.moveToNext()) {
            int id = cursor.getInt( 0 );
            String nickname = cursor.getString(1);
            String datetime = cursor.getString(2);
            String pwdhash = cursor.getString(3);
            r = new AuthorityRecord( id, nickname, email, datetime, pwdhash );
        }
        cursor.close();
        return r;
    }

    public long insertAuthority( AuthorityRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        // id is auto increase
        values.put(COL_nickname, r.getNickname());
        values.put(COL_email, r.getEmail());
        values.put(COL_datetime, r.getDatetime());
        values.put(COL_pwdhash, r.getPwdHash());

        return db.insert(AUTHORITY_TABLE_NAME, null, values);
    }

    public long updateAuthority( AuthorityRecord r ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_nickname, r.getNickname());
        values.put(COL_email, r.getEmail());
        values.put(COL_datetime, r.getDatetime());
        values.put(COL_pwdhash, r.getPwdHash());
        String whereClause = COL_id + " = ?;";
        String[] whereArgs = { String.valueOf( r.getId() ) };
        return db.update(AUTHORITY_TABLE_NAME, values, whereClause, whereArgs);
    }

    public int deleteAuthorityById( int id ) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_id + " = ?;";
        String[] whereArgs = { String.valueOf( id ) };
        return db.delete(AUTHORITY_TABLE_NAME, whereClause, whereArgs);
    }

*/

}
