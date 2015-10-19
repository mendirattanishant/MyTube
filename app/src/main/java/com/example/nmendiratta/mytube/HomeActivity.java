package com.example.nmendiratta.mytube;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.ResourceId;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity  implements View.OnClickListener {

    public String oauth_token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        init();
    }

    protected void init()
    {
        Button insertButton = (Button)findViewById(R.id.insertButton);
        insertButton.setOnClickListener(this);

        Button deleteButton = (Button)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.insertButton:
                new InsertVideoInPlaylist().execute();
                break;
            case R.id.deleteButton:
                new DeleteVideoFromPlaylist().execute();
                break;

        }
    }

    //insert in playlist
    private class InsertVideoInPlaylist extends AsyncTask<String, Void, String> {

        private YouTube youtube;
        private String playlistId;

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("inserting in playlist", oauth_token);
            try {

                Log.d("one","one");
                // Authorize the request.
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);

                // This object is used to make YouTube Data API requests.
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();



                // list that contains playlists of current user.
                //  String uploadPlaylistId = "PL8_B7e8MFom3V9ktKZ9JaNppL2-Y6gjt0";

                YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
                Log.d("Playlist List", p1.execute().getItems().toString());
                PlaylistListResponse p = p1.execute();

                // Retrieve the playlist ID of of SSJU-CMPE-277
                for (Playlist item:p.getItems()
                        ) {
                    if (item.getSnippet().getTitle().equalsIgnoreCase("SSJU-CMPE-277")){
                        playlistId = item.getId().toString();
                        break;
                    }
                }

                //inserting into playlist

                // Define a resourceId that identifies the video being added to the
                // playlist.
                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                resourceId.setVideoId("8Cn1pYnAZSE");


                // Set fields included in the playlistItem resource's "snippet" part.
                PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                playlistItemSnippet.setTitle("First video in the test playlist");
                playlistItemSnippet.setPlaylistId(playlistId);
                playlistItemSnippet.setResourceId(resourceId);

                Log.d("three", "three");


                PlaylistItem playlistItem = new PlaylistItem();
                playlistItem.setSnippet(playlistItemSnippet);

                YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                        youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
                PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

                Log.d("four","four");
                // Print data from the API response and return the new playlist

                return null;
            } catch (Exception e) {
            } finally {

            }
            return null;
        }

    }


    //insert in playlist
    private class DeleteVideoFromPlaylist extends AsyncTask<String, Void, String> {

        private YouTube youtube;
        private String playlistId;

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("Deleting in playlist", oauth_token);
            try {

               // Authorize the request.
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);

                // This object is used to make YouTube Data API requests.
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();



                // list that contains playlists of current user.
                //  String uploadPlaylistId = "PL8_B7e8MFom3V9ktKZ9JaNppL2-Y6gjt0";

                YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
                Log.d("Playlist List", p1.execute().getItems().toString());
                PlaylistListResponse p = p1.execute();

                // Retrieve the playlist ID of of SSJU-CMPE-277
                for (Playlist item:p.getItems()
                        ) {
                    if (item.getSnippet().getTitle().equalsIgnoreCase("SSJU-CMPE-277")){
                        playlistId = item.getId().toString();
                        break;
                    }
                }
//                YouTube.Playlists.List p_delete_list = youtube.playlists().list("snippet").setMine(true);
//                Log.d("Playlist List", p_delete_list.execute().getItems().toString());


              //  YouTube.PlaylistItems.Delete playlistItemsDeleteCommand =
                //        youtube.playlistItems().delete("8Cn1pYnAZSE");
              // playlistItemsDeleteCommand.execute();

                Log.d("playlist item deleted","four");
                return null;
            } catch (Exception e) {
            } finally {

            }
            return null;
        }

    }
}
