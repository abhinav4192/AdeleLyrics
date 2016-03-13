package fightingpit.adelelyrics;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by abhinavgarg on 07/03/16.
 */
public class LyricsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fl_lyrics, container, false);

        TextView aLyricsView = (TextView) rootView.findViewById(R.id.tv_ly_lyrics);
        aLyricsView.setText(getArguments().getString("Lyrics"));

        TextView aSongTitle = (TextView) rootView.findViewById(R.id.tv_ly_song_title);
        aSongTitle.setText(getArguments().getString("Title"));
        return rootView;
    }
}
