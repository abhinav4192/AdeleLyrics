package fightingpit.adelelyrics;
import java.util.ArrayList;

import fightingpit.adelelyrics.SongContainer;

/**
 * Created by abhinavgarg on 07/03/16.
 */
public final class AlbumContainer {

    private Integer AlbumId;
    private String AlbumName;
    private String AlbumYear;
    private ArrayList<SongContainer> AlbumSongs;

    public AlbumContainer(Integer albumId, String albumName, String albumYear) {
        AlbumId = albumId;
        AlbumName = albumName;
        AlbumYear = albumYear;
    }

    public Integer getAlbumId() {
        return AlbumId;
    }

    public void setAlbumSongs(ArrayList<SongContainer> albumSongs) {
        AlbumSongs = albumSongs;
    }

    public String getAlbumName() {
        return AlbumName;
    }

    public ArrayList<SongContainer> getAlbumSongs() {
        return AlbumSongs;
    }

    public String getAlbumYear() {
        return AlbumYear;
    }

    public int getAlbumSize(){
        return AlbumSongs.size();
    }
}
