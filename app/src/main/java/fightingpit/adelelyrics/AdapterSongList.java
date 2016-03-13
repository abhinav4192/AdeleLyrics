package fightingpit.adelelyrics;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by abhinavgarg on 11/03/16.
 */
public class AdapterSongList extends BaseAdapter {


    ArrayList<SongContainer> mSongList;
    private LayoutInflater inflater;
    private Activity activity;

    public AdapterSongList(ArrayList<SongContainer> iSongList) {
        mSongList = iSongList;
    }

    public void setInflater(LayoutInflater inflater, Activity act) {
        this.inflater = inflater;
        activity = act;
    }

    @Override
    public int getCount() {
        return mSongList.size();
    }

    @Override
    public SongContainer getItem(int position) {
        return mSongList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.search_song_list_item, null);
        }

        final SongContainer aSong = getItem(position);
        TextView aSongName = (TextView) convertView.findViewById(R.id.tv_ssli_song_name);
        aSongName.setText(aSong.getSongName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("Lyrics", aSong.getSongLyrics());
                bundle.putString("Title", aSong.getSongName());

                LyricsFragment aFrag = new LyricsFragment();
                aFrag.setArguments(bundle);

                ((MainActivity) activity).showMenu(false);

                activity.getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_main_act, aFrag)
                        .addToBackStack(null)
                        .commit();

            }
        });

        return convertView;
    }
}
