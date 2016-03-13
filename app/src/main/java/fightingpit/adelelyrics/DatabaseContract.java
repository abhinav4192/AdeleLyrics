package fightingpit.adelelyrics;

import android.provider.BaseColumns;

/**
 * Created by abhinavgarg on 08/03/16.
 */
public class DatabaseContract {

    public static final  int    DATABASE_VERSION    = 1;
    public static final  String DATABASE_NAME       = "database.db";
    private static final String UNIQUE              = "UNIQUE";
    private static final String COMMA_SEP           = ", ";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DatabaseContract() {}

    public static abstract class Albums implements BaseColumns {

        public static final String TABLE_NAME     = "ALBUMS";
        public static final String ALBUM_ID       = "ALBUM_ID";
        public static final String ALBUM_NAME     = "ALBUM_NAME";
        public static final String ALBUM_YEAR     = "ALBUM_YEAR";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                ALBUM_ID + " INTEGER" + COMMA_SEP +
                ALBUM_NAME + " TEXT NOT NULL" + COMMA_SEP +
                ALBUM_YEAR + " INTEGER NOT NULL" + COMMA_SEP +
                UNIQUE + " (" + ALBUM_NAME + ")" +
                " )";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static abstract class Songs implements BaseColumns {

        public static final String TABLE_NAME     = "SONGS";
        public static final String SONG_ID        = "SONG_ID";
        public static final String ALBUM_ID       = "ALBUM_ID";
        public static final String SONG_NAME      = "SONG_NAME";
        public static final String SONG_LYRICS    = "SONG_LYRICS";

        public static final String CREATE_TABLE = "CREATE TABLE " +
                TABLE_NAME + " (" +
                SONG_ID + " INTEGER" + COMMA_SEP +
                ALBUM_ID + " INTEGER NOT NULL" + COMMA_SEP +
                SONG_NAME + " TEXT NOT NULL" + COMMA_SEP +
                SONG_LYRICS + " TEXT NOT NULL" + COMMA_SEP +
                UNIQUE + " (" + ALBUM_ID + COMMA_SEP + SONG_NAME + ")" +
                " )";

        public static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }


    public static final String[] SQL_CREATE_TABLE_ARRAY = {
            Albums.CREATE_TABLE,
            Songs.CREATE_TABLE,
    };

    public static final String[] SQL_DROP_TABLE_ARRAY = {
            Albums.DROP_TABLE,
            Songs.DROP_TABLE,
    };
}
