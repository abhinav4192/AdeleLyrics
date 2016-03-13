package fightingpit.adelelyrics;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by abhinavgarg on 10/03/16.
 */
public class SearchFragment extends Fragment {

    private String mSearchQuery = "";
    private ImageView mSearchButton;
    private TextView mNothingFound;
    private ExpandableListView mExpandableListView;
    private ListView mListView;
    private RelativeLayout mAlbum;
    private RelativeLayout mSong;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fl_search, container, false);

        final EditText aSearchTextView = (EditText) rootView.findViewById(R.id.et_fs_search_text);
        aSearchTextView.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

        mAlbum = (RelativeLayout) rootView.findViewById(R.id.ll_fs_album);
        mAlbum.setVisibility(View.GONE);

        mSong = (RelativeLayout) rootView.findViewById(R.id.ll_fs_song);
        mSong.setVisibility(View.GONE);

        mNothingFound = (TextView) rootView.findViewById(R.id.tv_fs_nothing_found);
        mNothingFound.setVisibility(View.GONE);

        mExpandableListView = (ExpandableListView) rootView.findViewById(R.id.elv_fs_albums);
        mListView = (ListView) rootView.findViewById(R.id.lv_fs_songs);

        mSearchButton = (ImageView) rootView.findViewById(R.id.bt_fs_search);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(aSearchTextView.getWindowToken(), 0);
                String aSearchQuery = aSearchTextView.getText().toString().trim().toLowerCase();
                if (aSearchQuery.length() == 0) {
                    Toast.makeText(getActivity().getBaseContext(),
                            getResources().getString(R.string.enter_valid_text),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mSearchQuery = aSearchQuery;
                    searchAlbums(aSearchQuery);
                    aSearchTextView.setText("");
                    aSearchTextView.clearFocus();
                }


            }
        });

        aSearchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(aSearchTextView.getWindowToken(), 0);

                String aSearchQuery = aSearchTextView.getText().toString().trim().toLowerCase();
                if (aSearchQuery.length() == 0) {
                    Toast.makeText(getActivity().getBaseContext(),
                            getResources().getString(R.string.enter_valid_text),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mSearchQuery = aSearchQuery;
                    searchAlbums(aSearchQuery);
                    aSearchTextView.setText("");
                    aSearchTextView.clearFocus();
                }
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSearchQuery.length()>0){
            searchAlbums(mSearchQuery);
        }
    }

    private void searchAlbums(String iSearchQuery){

        ArrayList<AlbumContainer> aAlbumListToShow = new ArrayList<>();
        ArrayList<SongContainer> aSongListToShow = new ArrayList<>();
        getData(iSearchQuery, aAlbumListToShow, aSongListToShow);


        if(aAlbumListToShow.size()>0){
            AdapterExpandableAlbumList mAdapterExpandableAlbumList = new AdapterExpandableAlbumList(aAlbumListToShow);
            mAdapterExpandableAlbumList
                    .setInflater(
                            (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getActivity());
            mExpandableListView.setAdapter(mAdapterExpandableAlbumList);
            CommonUtils.setListViewHeight(mExpandableListView);

        }
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                CommonUtils.setListViewHeight(parent, groupPosition);
                return false;
            }
        });

        if(aSongListToShow.size()>0) {
            AdapterSongList mAdapterSongList = new AdapterSongList(aSongListToShow);
            mAdapterSongList
                    .setInflater(
                            (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                            getActivity());
            mListView.setAdapter(mAdapterSongList);
            CommonUtils.setListViewHeight(mListView);
        }
    }

    private void getData(String iSearchText, ArrayList<AlbumContainer> iAlbumContainer, ArrayList<SongContainer> iSongListToShow){
        final DatabaseHelper aDBHelper = new DatabaseHelper(getActivity().getBaseContext());
        ArrayList<AlbumContainer> aAlbumListFromDB = aDBHelper.getAllAlbumDetails();
        for (AlbumContainer a : aAlbumListFromDB) {

            ArrayList<SongContainer> aSongListFromDbForAlbum = aDBHelper.getAllSongDetailsFromAlbumId(a.getAlbumId());
            for(SongContainer s: aSongListFromDbForAlbum){
                if(s.getSongName().toLowerCase().contains(iSearchText.toLowerCase())){
                    iSongListToShow.add(s);
                }
            }
            if(a.getAlbumName().toLowerCase().contains(iSearchText.toLowerCase())){
                a.setAlbumSongs(aSongListFromDbForAlbum);
                iAlbumContainer.add(a);
            }

        }

        if(iAlbumContainer.size()==0 && iSongListToShow.size()==0){
            mNothingFound.setVisibility(View.VISIBLE);
        } else{
            mNothingFound.setVisibility(View.GONE);
        }

        if(iAlbumContainer.size()==0){
            mAlbum.setVisibility(View.GONE);

        }else{
            mAlbum.setVisibility(View.VISIBLE);

        }

        if(iSongListToShow.size()==0){
            mSong.setVisibility(View.GONE);

        }else{
            mSong.setVisibility(View.VISIBLE);

        }

    }



}
