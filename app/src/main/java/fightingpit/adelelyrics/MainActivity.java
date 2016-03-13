package fightingpit.adelelyrics;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    static final Integer MAX_AD_TIME_DIFF = 60;
    InterstitialAd mInterstitialAd;
    Time mAdLastShownTime;
    JSONParser jParser = new JSONParser();
    private ProgressDialog pDialog;
    MenuItem mSearchMenuButton;
    MenuItem mUpdateLyrics;
    MenuItem mRequest;
    MenuItem mRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DatabaseHelper aDBHelper = new DatabaseHelper(getBaseContext());
        if(aDBHelper.getAllAlbumId().size()<=0){
            new DBUpdate().execute(null, null, null);
        } else{
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_main_act, new AlbumListFragment())
                    .commit();
        }

        // InterstitialAd
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                mAdLastShownTime.setToNow();
            }
        });
        requestNewInterstitial();
        mAdLastShownTime = new Time();
        mAdLastShownTime.setToNow();

        // BannerAd
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Analytics
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getResources().getString(R.string.app_name));
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            // Show ad ony if not shown in last few seconds.
            Time aNow = new Time();
            aNow.setToNow();
            Long aTimeDiff =TimeUnit.MILLISECONDS.toSeconds(aNow.toMillis(true) - mAdLastShownTime.toMillis(true));
            if(aTimeDiff>MAX_AD_TIME_DIFF) {
                mInterstitialAd.show();
                mAdLastShownTime.setToNow();
            }
        }
        if(getFragmentManager().getBackStackEntryCount() != 0) {

            if(getFragmentManager().getBackStackEntryCount() ==1){
                showMenu(true);
            }
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        mSearchMenuButton = menu.getItem(0);
        mUpdateLyrics = menu.getItem(1);
        mRequest = menu.getItem(2);
        mRate = menu.getItem(3);
        return true;
    }

    public void showMenu(boolean iButtonState){
        mSearchMenuButton.setVisible(iButtonState);
        mUpdateLyrics.setVisible(iButtonState);
        mRate.setVisible(iButtonState);
        mRequest.setVisible(iButtonState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_search:
                showMenu(false);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_main_act, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
                break;

            case R.id.action_update:
                updateDb(true);
                break;

            case R.id.action_request:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", getResources().getString(R.string.mail_address), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Request Lyrics: " + getResources().getString(R.string.app_name));
                startActivity(Intent.createChooser(emailIntent, null));
                break;

            case R.id.action_rate:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
        }
        return true;
    }


    public void updateDb(boolean iIsCalledByUser){
        DBUpdate aDBUpdate = new DBUpdate();
        aDBUpdate.setIsCalledByUser(iIsCalledByUser);
        aDBUpdate.execute(null, null, null);
    }


    private class DBUpdate extends AsyncTask<Void, Void, Void> {
        boolean isDBUpdateSuccess = true;
        boolean isCalledByUser = false;
        ArrayList<String> aDetailsParam = new ArrayList<>();

        public void setIsCalledByUser(boolean isCalledByUser) {
            this.isCalledByUser = isCalledByUser;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(getResources().getString(R.string.db_update_wait));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected Void doInBackground(Void... args) {
            //Log.d("ABG", "In back 1");

            List<NameValuePair> aParamsList = new ArrayList<>();
            aParamsList.add(new BasicNameValuePair(getResources().getString(R.string.json_action_code),
                    getResources().getString(R.string.json_action_list)));
            aParamsList.add(new BasicNameValuePair(getResources().getString(R.string.json_artist_name),
                    getResources().getString(R.string.artist_value)));

            String aUrl = getResources().getString(R.string.data_script_path);
            JSONObject jsonList = jParser.makeHttpRequest(aUrl, "GET", aParamsList);

            if(jsonList!=null) {
                //Log.d("ABG", "JSON:" + jsonList.toString());
                try{
                    int aSuccess = jsonList.getInt(getResources().getString(R.string.json_success));
                    if(aSuccess==1){
                        // Get Album List from Local DB
                        final DatabaseHelper aDBHelper = new DatabaseHelper(getBaseContext());
                        ArrayList<Integer> aLocalAlbumIdArray = aDBHelper.getAllAlbumId();

                        // Get Album and Song list from Server.
                        JSONArray aServerAlbumsArray = jsonList.getJSONArray(getResources().getString(R.string.json_albums));
                        for(int i=0;i<aServerAlbumsArray.length();i++){
                            JSONObject aServerAlbum = aServerAlbumsArray.getJSONObject(i);
                            Integer aAlbumIdServer = aServerAlbum.getInt(getResources().getString(R.string.json_album_id));
                            ArrayList<Integer> aSongsToFetchFromServer = new ArrayList<>();

                            if(aLocalAlbumIdArray.size()>0){
                                for(int z=0;z<aLocalAlbumIdArray.size();z++){
                                    if(aAlbumIdServer==aLocalAlbumIdArray.get(z)){

                                        // Get Songs list for Album from Local DB.
                                        ArrayList<Integer> aLocalSongIdArray = aDBHelper.getSongIdFromAlbumId(aAlbumIdServer);

                                        JSONArray aServerSongArray = aServerAlbum.getJSONArray(getResources().getString(R.string.json_song_id));
                                        for(int j=0; j<aServerSongArray.length();j++){


                                            Integer aSongIdServer = Integer.parseInt(aServerSongArray.getString(j));
                                            boolean toAdd = true;
                                            for(int y=0;y<aLocalSongIdArray.size();y++){
                                                if(aSongIdServer==aLocalSongIdArray.get(y)){
                                                    toAdd=false;
                                                    break;
                                                }
                                            }
                                            if(toAdd){
                                                aSongsToFetchFromServer.add(aSongIdServer);
                                            }
                                        }
                                        break;
                                    } else if(z==aLocalAlbumIdArray.size()-1) {
                                        JSONArray aServerSongArray = aServerAlbum.getJSONArray(getResources().getString(R.string.json_song_id));
                                        for (int j = 0; j < aServerSongArray.length(); j++) {
                                            Integer aSongIdServer = Integer.parseInt(aServerSongArray.getString(j));
                                            aSongsToFetchFromServer.add(aSongIdServer);

                                        }
                                    }
                                }
                            }else{
                                JSONArray aServerSongArray = aServerAlbum.getJSONArray(getResources().getString(R.string.json_song_id));
                                for (int j = 0; j < aServerSongArray.length(); j++) {
                                    Integer aSongIdServer = Integer.parseInt(aServerSongArray.getString(j));
                                    aSongsToFetchFromServer.add(aSongIdServer);
                                }
                            }

                            if(aSongsToFetchFromServer.size()>0){
                                String aTempFetchParam = aAlbumIdServer.toString() +":";
                                for(int x=0;x<aSongsToFetchFromServer.size();x++){
                                    if(x!=0){
                                        aTempFetchParam =aTempFetchParam + "," + aSongsToFetchFromServer.get(x).toString();
                                    }else{
                                        aTempFetchParam =aTempFetchParam + aSongsToFetchFromServer.get(x).toString();
                                    }
                                }
                                aDetailsParam.add(aTempFetchParam);
                            }
                        }

                        if(aDetailsParam.size()>0){

                            int aParamSize = aDetailsParam.size();
                            int aLoopSize = 1; // Albums to fetch from server in one go.
                            int aLoopNum = aParamSize/aLoopSize;
                            int aRemainderNum = aParamSize-aLoopNum*aLoopSize;

                            int aCounter =0;

                            for(int i=1;i<=aLoopNum;i++){
                                String aFetchParam = new String("");
                                for(int j=1;j<=aLoopSize;j++){
                                    if(j!=1){
                                        aFetchParam = aFetchParam + "+" + aDetailsParam.get(aCounter);
                                    }else{
                                        aFetchParam = aFetchParam + aDetailsParam.get(aCounter);
                                    }
                                    aCounter++;
                                }

                                isDBUpdateSuccess = isDBUpdateSuccess && updateLyricsInDB(aFetchParam);
                            }
                            String aFetchParam = new String("");
                            for(int i=1;i<=aRemainderNum;i++){
                                if(i!=1){
                                    aFetchParam = aFetchParam + "+" + aDetailsParam.get(aCounter);
                                }else{
                                    aFetchParam = aFetchParam + aDetailsParam.get(aCounter);
                                }
                                aCounter++;
                                if(i==aRemainderNum){
                                    isDBUpdateSuccess = isDBUpdateSuccess && updateLyricsInDB(aFetchParam);
                                }
                            }


                        }else {
                            Log.d("ABG","Nothing to fetch." );
                        }
                    }else{
                        // Failed
                        isDBUpdateSuccess = false;
                        Log.d("ABG", "Error From Server. ErrorCode:" +
                                jsonList.getString(getResources().getString(R.string.json_error_code)));
                    }

                } catch (Exception e){
                    isDBUpdateSuccess = false;
                    e.printStackTrace();
                    Log.d("ABG", "Exception:Fetch List:" + e.toString());
                }

            }else{
                isDBUpdateSuccess = false;
                Log.d("ABG", "Check Internet Connection");
            }

            return null;
        }

        protected void onPostExecute(Void result) {

            if(isDBUpdateSuccess) {
                if(aDetailsParam.size()>0){
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.new_lyrics_found), Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.no_new_lyrics), Toast.LENGTH_SHORT).show();
                }
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fl_main_act, new AlbumListFragment())
                        .commit();

            }
            else{
                Toast.makeText(getBaseContext(),getResources().getString(R.string.db_user_update_failed),Toast.LENGTH_SHORT).show();
                if(!isCalledByUser){
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fl_main_act, new DBUpdateFailedFragment())
                            .commit();
                }
            }
            pDialog.dismiss();

        }
    }

    public boolean updateLyricsInDB(String iFetchParam){
        boolean aReturn = true;
        String aUrl = getResources().getString(R.string.data_script_path);
        List<NameValuePair> aParamsFetch = new ArrayList<>();
        aParamsFetch.add(new BasicNameValuePair(getResources().getString(R.string.json_action_code),
                getResources().getString(R.string.json_action_see)));
        aParamsFetch.add(new BasicNameValuePair(getResources().getString(R.string.json_song_fetch_list),
                iFetchParam));

        JSONObject jsonFetch = jParser.makeHttpRequest(aUrl, "GET", aParamsFetch);
        if(jsonFetch!=null){
            try{
                //Log.d("ABG", "JSON:" + jsonFetch.toString());
                int aSuccess = jsonFetch.getInt(getResources().getString(R.string.json_success));
                if(aSuccess==1){
                    final DatabaseHelper aDBHelper = new DatabaseHelper(getBaseContext());
                    JSONArray aServerAlbumsArray = jsonFetch.getJSONArray(getResources().getString(R.string.json_albums));
                    for(int i=0;i<aServerAlbumsArray.length();i++){
                        JSONObject aServerAlbum = aServerAlbumsArray.getJSONObject(i);
                        Integer aAlbumId = aServerAlbum.getInt(getResources().getString(R.string.json_album_id));
                        String aAlbumName = aServerAlbum.getString(getResources().getString(R.string.json_album_name));
                        Integer aAlbumYear = aServerAlbum.getInt(getResources().getString(R.string.json_album_year));

                        // Try to insert Album into DB.
                        if(!aDBHelper.doesAlbumExists(aAlbumId)){
                            aDBHelper.insertAlbum(aAlbumId, aAlbumName, aAlbumYear);
                        }

                        JSONArray aSongsArray = aServerAlbum.getJSONArray(getResources().getString(R.string.json_songs));
                        for(int j=0;j<aSongsArray.length();j++){
                            JSONObject aServerSong = aSongsArray.getJSONObject(j);
                            Integer aSongId = aServerSong.getInt(getResources().getString(R.string.json_song_id));
                            String aSongName = aServerSong.getString(getResources().getString(R.string.json_song_name));
                            String aSongLyrics = aServerSong.getString(getResources().getString(R.string.json_song_lyrics));

                            // Insert Song into DB.
                            aDBHelper.insertSong(aSongId, aAlbumId, aSongName, aSongLyrics);
                        }
                    }
                }else{
                    // Failed
                    aReturn = false;
                    Log.d("ABG", "Error From Server. ErrorCode:" +
                            jsonFetch.getString(getResources().getString(R.string.json_error_code)));
                }

            } catch (Exception e){
                aReturn = false;
                Log.d("ABG", "Exception:Fetch Data:" + e.toString());
            }
        }else{
            aReturn = false;
            Log.d("ABG", "Check Internet Connection");
        }
        return aReturn;

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

}
