
package com.example.nmendiratta.mytube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Favourites extends Fragment {
    public static final String MY_PREFS_NAME = "";
    private static final String TAG = "HomeActivity";
    public String oauth_token;
    private ListView videosFound;
    private List<PlaylistItem> playlistItemResult1;
    PlaylistItemListResponse playlistItemResultforDeletion;
    List<VideoItem> playlistVideo;
    PlaylistItemListResponse playlistItemResult;
    private Handler handler;
    List<String> videoIds = new ArrayList<String>();
    private YouTube youtube;
    private YouTube.Search.List query;
    List<PlaylistItem> playlistItemList;
    List<String> removalIds = new ArrayList<>();
    ;

    // Your developer key goes here
    public static final String KEY
            = "AIzaSyDD3qgwUIKzdu-zvy_fqvYsztyD_ADwFPc";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        handler = new Handler();
        playlistVideo = new ArrayList<>();

        loadVideos();
        //runQuery();
        View v = inflater.inflate(R.layout.favourites, container, false);
        videosFound = (ListView) v.findViewById(R.id.videos_found);
        TextView tv = (TextView) v.findViewById(R.id.search_input);
        Button button = (Button) v.findViewById(R.id.Delete);

        Button buttonRefreh = (Button) v.findViewById(R.id.Refresh);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("removalIDS", removalIds.toString());

                DeleteVideoFromPlaylist deleteObj = new DeleteVideoFromPlaylist(removalIds);
                deleteObj.execute();
            }
        });

        buttonRefreh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Log.d("removalIDS", removalIds.toString());

                loadVideos();
            }
        });

        tv.setText("SJSU-CMPE-277");
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                // Log.d("SEARCH", "Item clicked " + id);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", playlistVideo.get(pos).getId());
                startActivity(intent);
            }

        });
        return v;
    }


    public void loadVideos() {
        new GetAccessToken().execute();
    }

    private class GetAccessToken extends AsyncTask<String, Void, String> {

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
                getPlayListDetail(youtube);

                return null;
            } catch (Exception e) {
                Log.e(TAG, "CME IN EXCEPTION", e);
            } finally {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateVideosFound();
        }

        private void getPlayListDetail(YouTube youtube) throws IOException {
            playlistVideo.clear();
            YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
            Log.d("Playlist List", p1.execute().getItems().toString());
            PlaylistListResponse p = p1.execute();

            // Retrieve the playlist ID of of SSJU-CMPE-277
            for (Playlist item : p.getItems()
                    ) {
                if (item.getSnippet().getTitle().equalsIgnoreCase("SJSU-CMPE-277")) {
                    uploadPlaylistId = item.getId().toString();
                    break;
                }
            }
            playlistItemList = new ArrayList<PlaylistItem>();

            YouTube.PlaylistItems.List playlistItemRequest =
                    youtube.playlistItems().list("id,contentDetails,snippet");
            playlistItemRequest.setPlaylistId(uploadPlaylistId);


            // Only retrieve data used in this application, thereby making
            // the application more efficient. See:
            // https://developers.google.com/youtube/v3/getting-started#partial
            playlistItemRequest.setFields(
                    "items(contentDetails/videoId,snippet/title,snippet/publishedAt,snippet/thumbnails,id),nextPageToken,pageInfo");

            String nextToken = "";

            do {
                playlistItemRequest.setPageToken(nextToken);
                playlistItemResult = playlistItemRequest.execute();

                playlistItemList.addAll(playlistItemResult.getItems());

                nextToken = playlistItemResult.getNextPageToken();
            } while (nextToken != null);

            // Prints information about the results.
            Log.d("Playlist item result", playlistItemList.toString());
            Iterator<PlaylistItem> playlistIterator = playlistItemList.iterator();
            List<Video> viewslist = new ArrayList<Video>();

            while (playlistIterator.hasNext()) {
                PlaylistItem playlistItem = playlistIterator.next();
                Log.d("TAG", " video name  = " + playlistItem.getSnippet().getTitle());
                Log.d("TAG", " video= " + playlistItem.getContentDetails().getVideoId());
                VideoItem vi = new VideoItem();
                Log.d("TAG", " upload date = " + playlistItem.getSnippet().getPublishedAt());
                vi.setTitle(playlistItem.getSnippet().getTitle());
                Log.d("TAG", playlistItem.toPrettyString());
                //  vi.setViewCount(it.next().getStatistics().getViewCount());
                //vi.setThumbnailURL(playlistItem.getSnippet().getThumbnails().getStandard().getUrl());
                if (playlistItem.getSnippet().getThumbnails().getDefault().getUrl() != null) {
                    vi.setThumbnailURL(playlistItem.getSnippet().getThumbnails().getDefault().getUrl());
                } else {
                    vi.setThumbnailURL("https://i.ytimg.com/vi/ef-6NZjBtW0/default.jpg");
                }
                vi.setPublishDate(playlistItem.getSnippet().getPublishedAt().toString());
                vi.setIsFavorite(true);
                vi.setId(playlistItem.getId());

                //Video list for adapter
                playlistVideo.add(vi);
                Log.d("TAG", "\n-------------------------------------------------------------\n");
                Log.d("OBJECT", playlistVideo.toString());


            }
        }

        private void prettyPrint(int size, Iterator<PlaylistItem> playlistEntries) {
            Log.d("TAG", "=============================================================");
            Log.d("TAG", "\t\tTotal Videos Uploaded: " + size);
            Log.d("TAG", "=============================================================\n");
        }
    }


    private void updateVideosFound() {
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.video_item, playlistVideo) {

            ViewHolderItem viewHolder;

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                VideoItem item = playlistVideo.get(position);
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item1, parent, false);
//                TextView title = (TextView)convertView.findViewById(R.id.video_title);
//                TextView viewCounts = (TextView)convertView.findViewById(R.id.view_count);
//                TextView publishDate = (TextView)convertView.findViewById(R.id.publish_date);
//                final CheckBox favoriteBtn = (CheckBox)convertView.findViewById(R.id.favorite);
//                //checkBox yourCheckBox = (CheckBox) findViewById (R.id.yourId);
//                final List<VideoItem> todelete = new ArrayList<>();

                    viewHolder = new ViewHolderItem();
                    viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                    viewHolder.title = (TextView) convertView.findViewById(R.id.video_title);
                    viewHolder.viewCounts = (TextView) convertView.findViewById(R.id.view_count);
                    viewHolder.publishDate = (TextView) convertView.findViewById(R.id.publish_date);
                    viewHolder.favoriteBox = (CheckBox) convertView.findViewById(R.id.favorite);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolderItem) convertView.getTag();
                }

                viewHolder.favoriteBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg) {
                        VideoItem updatedItem = (VideoItem) arg.getTag();
                        Log.d("UPDATEd ITEM", updatedItem.toString());
                        removalIds.add(updatedItem.getId());
                        // updatedItem.setIsFavorite(!updatedItem.isFavorite());
                        // notifyDataSetChanged();

                    }

                });
                Picasso.with(getActivity().getApplicationContext()).load(item.getThumbnailURL()).into(viewHolder.thumbnail);

                viewHolder.title.setText(item.getTitle());
                viewHolder.viewCounts.setText("Views: " + item.getViewCount());
                viewHolder.publishDate.setText(item.getPublishDate());
                viewHolder.favoriteBox.setTag(item);
                viewHolder.favoriteBox.setChecked(!item.isFavorite());
                return convertView;
            }
        };
        videosFound.setAdapter(adapter);

    }

    ;


    private List<Video> getViews() throws IOException {

        for (PlaylistItem vi : playlistItemList) {
            videoIds.add(vi.getContentDetails().getVideoId());
        }

        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);

        YouTube.Videos.List listVideosRequest = youtube.videos().list("statistics").setId(videoId).setKey(KEY);
        VideoListResponse listResponse = listVideosRequest.execute();

        List<Video> videoList = listResponse.getItems();

        if (videoList != null) {
            // prettyPrint(videoList.iterator(), queryTerm);
            Log.i("Video list api called", videoList.toString());

            Log.i("Printed", "Video list response");
            return videoList;
        }

        return null;


    }

    private class DeleteVideoFromPlaylist extends AsyncTask<String, Void, String> {
        private String playlistId;
        List<String> playlistDeleteListIds = new ArrayList<String>();

        public DeleteVideoFromPlaylist(List<String> playlistDeleteListIds) {
            this.playlistDeleteListIds = playlistDeleteListIds;
        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("Deleting in playlist", oauth_token);
            try {
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);
                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();

                YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
                Log.d("Playlist List", p1.execute().getItems().toString());
                PlaylistListResponse p = p1.execute();

                // Retrieve the playlist ID of of SSJU-CMPE-277
                for (Playlist item : p.getItems()
                        ) {
                    if (item.getSnippet().getTitle().equalsIgnoreCase("SJSU-CMPE-277")) {
                        playlistId = item.getId().toString();
                        break;
                    }
                }



                YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems().list("id,contentDetails,snippet");
                playlistItemRequest.setPlaylistId(playlistId);

                playlistItemRequest.setFields(
                        "items(id),nextPageToken,pageInfo");

                playlistItemResultforDeletion = playlistItemRequest.execute();

                Log.d("chiki", playlistItemResultforDeletion.toString());
                List<PlaylistItem> pd = playlistItemResultforDeletion.getItems();

                List<String> p11 = new ArrayList<>();
                for (PlaylistItem items : pd) {
                    Log.d("KOKO", items.getId());
                    p11.add(items.getId());
                }

                for (String item : playlistDeleteListIds) {
                    Log.d("Playlist List", "AALAAO");
                    Log.d("Playlist playlisttIds", playlistDeleteListIds.toString());
                    if (p11.contains(item)) {
                        Log.d("Playlist P11", p11.toString());
                        //Log.d("Playlist List", "BALAO");
                        YouTube.PlaylistItems.Delete deleteVideo = youtube.playlistItems().delete(item.toString());
                        Log.d("item to delete", item.toString());
                        deleteVideo.execute();
                    }
                }
                YouTube.PlaylistItems.Delete deleteVideo = youtube.playlistItems().delete("PLnKT3vXBiBL9YiWlIN6GYKNP5xQZbXhB4xgVmqxoE-zo");
                deleteVideo.execute();


//                //**
//                Iterator<String> playlistDeleteListIdsIterator = playlistDeleteListIds.iterator();
//                while (playlistDeleteListIdsIterator.hasNext()) {
//                    String playlistItemToDelete = playlistDeleteListIdsIterator.next();
//                    youtube.playlistItems().delete(playlistItemToDelete);
//                }
//

                Log.d("playlist item deleted", "four");
                return null;
            } catch (Exception e) {
            }
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loadVideos();
        }


    }


}