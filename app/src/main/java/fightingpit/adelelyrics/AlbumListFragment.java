package fightingpit.adelelyrics;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

/**
 * Created by abhinavgarg on 07/03/16.
 */
public class AlbumListFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fl_album_list, container, false);;
        ExpandableListView aExpandableListView = (ExpandableListView) rootView.findViewById(android.R.id.list);
        AdapterExpandableAlbumList mAdapterExpandableWordList = new AdapterExpandableAlbumList(getData());
        mAdapterExpandableWordList
                .setInflater(
                        (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                        getActivity());
        aExpandableListView.setAdapter(mAdapterExpandableWordList);
        return rootView;

    }

    private ArrayList<AlbumContainer> getData(){
        final DatabaseHelper aDBHelper = new DatabaseHelper(getActivity().getBaseContext());
        ArrayList<AlbumContainer> aAlbumList = aDBHelper.getAllAlbumDetails();
        for (AlbumContainer a : aAlbumList) {
            a.setAlbumSongs(aDBHelper.getAllSongDetailsFromAlbumId(a.getAlbumId()));
        }
        return aAlbumList;

    }
}
