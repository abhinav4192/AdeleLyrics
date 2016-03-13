package fightingpit.adelelyrics;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by abhinavgarg on 07/03/16.
 */
public class AdapterExpandableAlbumList extends BaseExpandableListAdapter {

    ArrayList<AlbumContainer> mAlbumList;
    private LayoutInflater inflater;
    private Activity activity;

    public AdapterExpandableAlbumList(ArrayList<AlbumContainer> iAlbumList) {
        mAlbumList = iAlbumList;
    }

    public void setInflater(LayoutInflater inflater, Activity act) {
        this.inflater = inflater;
        activity = act;
    }

    @Override
    public int getGroupCount() {
        return mAlbumList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAlbumList.get(groupPosition).getAlbumSize();
    }

    @Override
    public AlbumContainer getGroup(int groupPosition) {
        return mAlbumList.get(groupPosition);
    }

    @Override
    public SongContainer getChild(int groupPosition, int childPosition) {
        return mAlbumList.get(groupPosition).getAlbumSongs().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.album_list_item, null);
        }

        AlbumContainer aAlbum = getGroup(groupPosition);
        TextView aAlbumName = (TextView) convertView.findViewById(R.id.tv_ali_album_name);
        TextView aAlbumYear = (TextView) convertView.findViewById(R.id.tv_ali_album_year);
        ImageView aExpandIcon = (ImageView) convertView.findViewById(R.id.iv_ali_album_expand);

        aAlbumName.setText(aAlbum.getAlbumName());
        aAlbumYear.setText(aAlbum.getAlbumYear());

        if(isExpanded){
            aExpandIcon.setImageResource(R.drawable.ic_expand_less_white_48dp);
        }  else{
            aExpandIcon.setImageResource(R.drawable.ic_expand_more_white_48dp);
        }


        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.song_list_item, null);
        }

        final SongContainer aSong = getChild(groupPosition, childPosition);
        TextView aSongName = (TextView) convertView.findViewById(R.id.tv_sli_song_name);
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

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }
}
