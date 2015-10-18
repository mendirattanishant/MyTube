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
import android.widget.Button;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class HomeActivityFragment extends Fragment{
    public static final String MY_PREFS_NAME = "";
    private static final String TAG = "HomeActivity";
    public String oauth_token;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new GetAccessToken().execute();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    private class GetAccessToken extends AsyncTask<String, Void, String> {

        private YouTube youtube;

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("oauth_token from Fragments", oauth_token);
            try {
                // Authorize the request.
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);

                // This object is used to make YouTube Data API requests.
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();

                // Call the API's channels.list method to retrieve the
                // resource that represents the authenticated user's channel.
                // In the API response, only include channel information needed for
                // this use case. The channel's contentDetails part contains
                // playlist IDs relevant to the channel, including the ID for the
                // list that contains videos uploaded to the channel.
                YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
                channelRequest.setMine(true);
                channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
                ChannelListResponse channelResult = channelRequest.execute();

                List<Channel> channelsList = channelResult.getItems();

                String uploadPlaylistId = "PL8_B7e8MFom3V9ktKZ9JaNppL2-Y6gjt0";

                List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

                // Retrieve the playlist of the channel's uploaded videos.
                YouTube.PlaylistItems.List playlistItemRequest =
                        youtube.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(uploadPlaylistId);

                // Only retrieve data used in this application, thereby making
                // the application more efficient. See:
                // https://developers.google.com/youtube/v3/getting-started#partial
                playlistItemRequest.setFields(
                        "items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

                String nextToken = "";

                do {
                    playlistItemRequest.setPageToken(nextToken);
                    PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                    playlistItemList.addAll(playlistItemResult.getItems());

                    nextToken = playlistItemResult.getNextPageToken();
                } while (nextToken != null);

                // Prints information about the results.
                prettyPrint(playlistItemList.size(), playlistItemList.iterator());


                return null;
            } catch (Exception e) {
            } finally {

            }
            return null;
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