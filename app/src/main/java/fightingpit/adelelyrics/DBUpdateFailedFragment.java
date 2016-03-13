package fightingpit.adelelyrics;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by abhinavgarg on 10/03/16.
 */
public class DBUpdateFailedFragment extends Fragment{

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fl_empty_db, container, false);

        (rootView.findViewById(R.id.tv_fed_try_again)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).updateDb(false);
            }
        });

        return rootView;
    }
}
