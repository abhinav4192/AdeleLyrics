package fightingpit.adelelyrics;

/**
 * Created by abhinavgarg on 07/03/16.
 */
public final class SongContainer {

    private String SongName;
    private String SongLyrics;

    public SongContainer(String songName, String songLyrics) {
        SongName = songName;
        SongLyrics = songLyrics;
    }

    public String getSongName() {
        return SongName;
    }

    public String getSongLyrics() {
        return SongLyrics;
    }
}
