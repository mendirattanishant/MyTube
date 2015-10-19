package com.example.nmendiratta.mytube;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.google.api.services.youtube.model.Channel;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment{
    public static final String MY_PREFS_NAME = "";
    private static final String TAG = "HomeActivity";
    public String oauth_token;
    private ListView videosFound;
    private List playlistItemResult;
    List<VideoItem> playlistVideo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new GetAccessToken().execute();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    private class GetAccessToken extends AsyncTask<String, Void, String> {

        private YouTube youtube;
        public String uploadPlaylistId;

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("oauth_token from ", oauth_token);
            try {
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);

                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();
                playlistItemResult = getPlayListDetail(youtube);

                // list that contains playlists of current user.
              //  String uploadPlaylistId = "PL8_B7e8MFom3V9ktKZ9JaNppL2-Y6gjt0";



                return null;
            } catch (Exception e) {
            } finally {

            }
            return null;
        }
        private List getPlayListDetail(YouTube youtube) throws IOException {
            YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
            Log.d("Playlist List", p1.execute().getItems().toString());
            PlaylistListResponse p = p1.execute();

            // Retrieve the playlist ID of of SSJU-CMPE-277
            for (Playlist item:p.getItems()
                    ) {
                if (item.getSnippet().getTitle().equalsIgnoreCase("SSJU-CMPE-277")){
                    uploadPlaylistId = item.getId().toString();
                    break;
                }
                Log.d("uploadPlayListid1",uploadPlaylistId);
            }
            Log.d("uploadPlayListid",uploadPlaylistId);

            //get playlist items
            List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

            YouTube.PlaylistItems.List playlistItemRequest =
                    youtube.playlistItems().list("id,contentDetails,snippet");
            playlistItemRequest.setPlaylistId(uploadPlaylistId);


            // Only retrieve data used in this application, thereby making
            // the application more efficient. See:
            // https://developers.google.com/youtube/v3/getting-started#partial
            playlistItemRequest.setFields(
                    "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

            String nextToken = "";
            PlaylistItemListResponse playlistItemResult;
            do {
                playlistItemRequest.setPageToken(nextToken);
                playlistItemResult = playlistItemRequest.execute();

                playlistItemList.addAll(playlistItemResult.getItems());

                nextToken = playlistItemResult.getNextPageToken();
            } while (nextToken != null);

            // Prints information about the results.
            //prettyPrint(playlistItemList.size(), playlistItemList.iterator());
            return playlistItemList;
        }


        private void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
            Log.d("TAG", "=============================================================");
            Log.d("TAG", "\t\tTotal Videos Uploaded: " + size);
            Log.d("TAG", "=============================================================\n");

            while (playlistEntries.hasNext()) {
                PlaylistItem playlistItem = playlistEntries.next();
                Log.d("TAG", " video name  = " + playlistItem.getSnippet().getTitle());
                Log.d("TAG", " video id    = " + playlistItem.getContentDetails().getVideoId());
                Log.d("TAG", " upload date = " + playlistItem.getSnippet().getPublishedAt());
                Log.d("TAG", "\n-------------------------------------------------------------\n");
            }
        }
    }









}