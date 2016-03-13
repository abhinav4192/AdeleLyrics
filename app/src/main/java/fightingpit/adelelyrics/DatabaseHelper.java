package fightingpit.adelelyrics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by abhinavgarg on 08/03/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    Context DB_CONTEXT;
    public DatabaseHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
        DB_CONTEXT = context;
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        for(int i=0; i < DatabaseContract.SQL_CREATE_TABLE_ARRAY.length; i++) {
            db.execSQL(DatabaseContract.SQL_CREATE_TABLE_ARRAY[i]);
        }

    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i=0; i < DatabaseContract.SQL_DROP_TABLE_ARRAY.length; i++) {
            db.execSQL(DatabaseContract.SQL_DROP_TABLE_ARRAY[i]);
        }
        onCreate(db);
    }

    public ArrayList<Integer> getAllAlbumId(){

        ArrayList<Integer> aAlbumIdList = new ArrayList<>();
        String[] projection = {DatabaseContract.Albums.ALBUM_ID};

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(DatabaseContract.Albums.TABLE_NAME, projection, null, null, null, null,
                DatabaseContract.Albums.ALBUM_ID + " DESC");
        c.moveToFirst();
        while(!c.isAfterLast()){
            aAlbumIdList.add(c.getInt(c.getColumnIndexOrThrow(DatabaseContract.Albums.ALBUM_ID)));
            c.moveToNext();
        }
        c.close();
        db.close();
        return aAlbumIdList;
    }

    public ArrayList<Integer> getSongIdFromAlbumId(Integer iAlbumId){

        ArrayList<Integer> aSongIdList = new ArrayList<>();
        String[] projection = {DatabaseContract.Songs.SONG_ID};
        String selection = DatabaseContract.Songs.ALBUM_ID + "=?";
        String[] selectionArgs = {iAlbumId.toString()};

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(DatabaseContract.Songs.TABLE_NAME, projection, selection, selectionArgs, null, null, DatabaseContract.Songs.SONG_ID);
        c.moveToFirst();
        while(!c.isAfterLast()){
            aSongIdList.add(c.getInt(c.getColumnIndexOrThrow(DatabaseContract.Songs.SONG_ID)));
            c.moveToNext();
        }
        c.close();
        db.close();
        return aSongIdList;
    }

    public ArrayList<AlbumContainer> getAllAlbumDetails(){

        ArrayList<AlbumContainer> aAlbumList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(DatabaseContract.Albums.TABLE_NAME, null, null, null, null, null,
                DatabaseContract.Albums.ALBUM_YEAR +" DESC, " + DatabaseContract.Albums.ALBUM_ID + " DESC");
        c.moveToFirst();
        while(!c.isAfterLast()){
            aAlbumList.add(new AlbumContainer(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseContract.Albums.ALBUM_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.Albums.ALBUM_NAME)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.Albums.ALBUM_YEAR))));
            c.moveToNext();
        }
        c.close();
        db.close();
        return aAlbumList;
    }

    public ArrayList<SongContainer> getAllSongDetailsFromAlbumId(Integer iAlbumId){

        ArrayList<SongContainer> aSongList = new ArrayList<>();
        String[] projection = {DatabaseContract.Songs.SONG_NAME,DatabaseContract.Songs.SONG_LYRICS};
        String selection = DatabaseContract.Songs.ALBUM_ID + "=?";
        String[] selectionArgs = {iAlbumId.toString()};

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(DatabaseContract.Songs.TABLE_NAME, projection, selection, selectionArgs, null, null, DatabaseContract.Songs.SONG_ID);
        c.moveToFirst();
        while(!c.isAfterLast()){
            aSongList.add(new SongContainer(
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.Songs.SONG_NAME)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseContract.Songs.SONG_LYRICS))));
            c.moveToNext();
        }
        c.close();
        db.close();
        return aSongList;
    }

    public boolean doesAlbumExists(Integer iAlbumId){

        boolean aReturnValue = false;
        String[] projection = {DatabaseContract.Albums.ALBUM_ID};
        String selection = DatabaseContract.Songs.ALBUM_ID + "=?";
        String[] selectionArgs = {iAlbumId.toString()};

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(DatabaseContract.Albums.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if(c.getCount()==1){
            aReturnValue = true;
        }
        c.close();
        db.close();

        return aReturnValue;

    }

    public boolean insertAlbum(Integer iAlbumId, String iAlbumName, Integer iAlbumYear){
        boolean aReturnValue =true;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues aVal = new ContentValues();
        aVal.put(DatabaseContract.Albums.ALBUM_ID,iAlbumId);
        aVal.put(DatabaseContract.Albums.ALBUM_NAME, iAlbumName);
        aVal.put(DatabaseContract.Albums.ALBUM_YEAR, iAlbumYear);
        try{
            db.insertOrThrow(DatabaseContract.Albums.TABLE_NAME, null, aVal);
        }catch(SQLiteConstraintException e){
            Log.d("ABG", e.toString() );
            aReturnValue = false;
        }
        return aReturnValue;
    }

    public boolean insertSong(Integer iSongID, Integer iAlbumId, String iSongName, String iSongLyrics){
        boolean aReturnValue =true;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues aVal = new ContentValues();
        aVal.put(DatabaseContract.Songs.SONG_ID,iSongID);
        aVal.put(DatabaseContract.Songs.ALBUM_ID,iAlbumId);
        aVal.put(DatabaseContract.Songs.SONG_NAME,iSongName);
        aVal.put(DatabaseContract.Songs.SONG_LYRICS,iSongLyrics);
        try{
            db.insertOrThrow(DatabaseContract.Songs.TABLE_NAME, null, aVal);
        }catch(SQLiteConstraintException e){
            Log.d("ABG", e.toString() );
            aReturnValue = false;
        }
        return aReturnValue;
    }
}
