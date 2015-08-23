package com.tiro_app.tiro_app.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.util.Log;

/**
 * Created by user on 19/08/2015.
 */
public class UserProfilAdapter_db {

    UHelper helper;

    public UserProfilAdapter_db(Context context){
        helper = new UHelper(context);
    }

    public Long insertAppUser(String username, ContentValues moreContent){
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(helper.Username, username);

        if(moreContent != null){
            contentValues.put(helper.Description, moreContent.getAsString("Description"));
            contentValues.put(helper.NbFollower, moreContent.getAsString(helper.NbFollower));
            contentValues.put(helper.NbFollowing, moreContent.getAsString(helper.NbFollowing));
        }

        long cursor = db.replace(helper.TABLE_NAME, helper.Description, contentValues);
        return cursor;
    }

    public ContentValues getUserProfil(String username){
        SQLiteDatabase db = helper.getReadableDatabase();
        String [] columns = {UHelper.Username, UHelper.Description, UHelper.NbFollower, UHelper.NbFollowing, UHelper.AvatarURI};
        String selection = UHelper.Username+" = '"+username+"'";
        Cursor cursor = db.query(UHelper.TABLE_NAME, columns, selection, null, null, null, null);

        if(cursor != null){
            cursor.moveToFirst();
            ContentValues map = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor,map);
            return map;
        }else{
            return null;
        }
    }

    static class UHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "tirodatabase.db";
        private static final String TABLE_NAME = "UserProfil";
        private static final String Username = "Username";
        private static final String Description = "Description";
        private static final String NbFollower = "NbFollower";
        private static final String NbFollowing = "NbFollowing";

        private static final String AvatarURI= "AvatarURI";

        private static final int DATABASE_VERSION= 2;
        private static final String CREATE_TABLE ="CREATE TABLE "+TABLE_NAME+" ( "+Username+" VARCHAR(255) PRIMARY KEY,"+Description+" TEXT DEFAULT 'No description' ,"+AvatarURI+" TEXT NULL, "+NbFollowing+" INTEGER NULL DEFAULT 0, "+NbFollower+" INTEGER NULL DEFAULT 0);";
        private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME+" ";


        public UHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_TABLE);
            }catch (SQLException e){
                Log.i("USerprofil", "onCreate exception ");
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try
            {
                db.execSQL(DROP_TABLE);
                db.execSQL(CREATE_TABLE);


            }catch (SQLException e){
                Log.i("USerprofil", "onUpgrade execption");
            }
        }
    }

}
