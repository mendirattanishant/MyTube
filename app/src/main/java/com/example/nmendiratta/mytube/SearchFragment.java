package com.example.nmendiratta.mytube;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.nmendiratta.mytube.Utility.Constatnts;
import com.example.nmendiratta.mytube.Utility.SessionManager;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SearchFragment extends Fragment {
    private EditText searchInput;
    private ListView videosFound;
    private List<VideoItem> searchResults;
    private SessionManager session;
    private Handler handler;
    public String oauth_token;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_search, container, false);
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_search);
        //setContentView(R.layout.tabs);


        session = new SessionManager(getActivity().getApplicationContext());

        searchInput = (EditText) view.findViewById(R.id.search_input);
        videosFound = (ListView) view.findViewById(R.id.videos_found);




        handler = new Handler();

        searchOnYoutube(Constatnts.INITIAL_SEARCH_WORD);


        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });

        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
               // Log.d("SEARCH", "Item clicked " + id);
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });

    return  view;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
           // Log.d("MYTUBE", "Before logout"+session.isLoggedIn());
            session.logoutUser();
            //finish();
           // Log.d("MYTUBE", "After logout" + session.isLoggedIn());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(SearchFragment.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(){
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.video_item, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView viewCounts = (TextView)convertView.findViewById(R.id.view_count);
                TextView publishDate = (TextView)convertView.findViewById(R.id.publish_date);
                final ImageButton favoriteBtn = (ImageButton)convertView.findViewById(R.id.favorite);
                favoriteBtn.setTag(position);


                favoriteBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        int placePosition = (Integer)favoriteBtn.getTag();
                        Log.d("MYTUBE","Place Position"+placePosition);
                        if(!searchResults.get(placePosition).isFavorite()){
                            favoriteBtn.setImageResource(R.drawable.plus);
                            searchResults.get(placePosition).setIsFavorite(true);
                            String vid = searchResults.get(placePosition).getId();
                            String title = searchResults.get(placePosition).getTitle();
                            String url = searchResults.get(placePosition).getThumbnailURL();
                            InsertVideoInPlaylist i = new InsertVideoInPlaylist(vid,title,url);
                            i.execute();
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Item added to favorites", Toast.LENGTH_SHORT).show();
                        }else{
                            favoriteBtn.setImageResource(R.drawable.minus);
                            searchResults.get(placePosition).setIsFavorite(false);
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Item removed from favorites", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

                VideoItem item = searchResults.get(position);
                item.setIsFavorite(false);

                Picasso.with(getActivity().getApplicationContext()).load(item.getThumbnailURL()).into(thumbnail);
                title.setText(item.getTitle());
                viewCounts.setText(""+item.getViewCount() + " views");
                publishDate.setText(item.getPublishDate());
                //description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private class InsertVideoInPlaylist extends AsyncTask<String, Void, String> {

        private String video_id;
        private String title;
        private String url;
        private YouTube youtube;
        private String playlistId;

        public InsertVideoInPlaylist(String video, String title, String url) {
            this.video_id = video;
            this.title = title;
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {

            SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 1); // 0 - for private mode
            oauth_token = pref.getString("oauth_token", "");
            Log.d("inserting in playlist", oauth_token);
            try {

                Log.d("one","one");
                GoogleCredential credential = new GoogleCredential().setAccessToken(oauth_token);

                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(
                        "MyTube").build();

                YouTube.Playlists.List p1 = youtube.playlists().list("snippet").setMine(true);
                Log.d("Playlist List", p1.execute().getItems().toString());
                PlaylistListResponse p = p1.execute();


                for (Playlist item:p.getItems()
                        ) {
                    if (item.getSnippet().getTitle().equalsIgnoreCase("SJSU-CMPE-277")){
                        playlistId = item.getId().toString();
                        break;
                    }
                }

                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                Log.d("VIDEO ID", video_id);
                resourceId.setVideoId(video_id);


                // Set fields included in the playlistItem resource's "snippet" part.
                PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                playlistItemSnippet.setTitle(title);
                playlistItemSnippet.setPlaylistId(playlistId);
                playlistItemSnippet.setResourceId(resourceId);


                Thumbnail thumbnail = new Thumbnail();
                thumbnail.setUrl(url);
                ThumbnailDetails t = new ThumbnailDetails();
                t.setDefault(thumbnail);
                playlistItemSnippet.setThumbnails(t);


                PlaylistItem playlistItem = new PlaylistItem();
                playlistItem.setSnippet(playlistItemSnippet);


                YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                        youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
                PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

                return null;
            } catch (Exception e) {
            } finally {

            }
            return null;
        }

    }
}
