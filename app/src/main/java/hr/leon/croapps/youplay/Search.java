package hr.leon.croapps.youplay;


import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Search {

    private ArrayList<Item> tempList = new ArrayList<>();
    private String nextPage = "";

    public ArrayList<Item> getTempList(){
        return tempList;
    }

    public void start(String query, long numberOfVideos, int pageNum, String relatedToId) {
        try {
            YouTube youtube;
            // prvo cemo naci sve ID-ove od videa koje nade
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                    new HttpRequestInitializer() {
                        public void initialize(HttpRequest request) throws IOException {

                        }
                    })
                    .setApplicationName("VideoStreamer")
                    .build();

            YouTube.Search.List search = youtube.search().list("id,snippet");
            search.setKey(Config.getApiKey());
            search.setQ(query);
            search.setType("video");

            if(relatedToId.length() > 2)
                search.setRelatedToVideoId(relatedToId);

            search.setMaxResults(numberOfVideos);
            SearchListResponse searchResponse;

            if(pageNum == 0) {
                nextPage = "";
                if(!tempList.isEmpty())
                    tempList.clear();
            }
            if(nextPage != null) {
                if (nextPage.length() < 2) {
                    searchResponse = search.execute();
                    nextPage = searchResponse.getNextPageToken();
                } else {
                    search.setPageToken(nextPage);
                    searchResponse = search.execute();
                    nextPage = searchResponse.getNextPageToken();
                }
            }else searchResponse = null;

            List<SearchResult> searchResultList = null;

            if(searchResponse != null) {
                searchResultList = searchResponse.getItems();
            }
            List<String> videoIds = new ArrayList<>();
            // ako je nesto nasao
            if (searchResultList != null) {

                // spremi ID-ove koje je nasao na jedno mjesto, jer cemo pomocu njih obavit sljedecu pretragu
                for (SearchResult searchResult : searchResultList) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                // formatiranje stringa id-ova
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                // ovog puta trazimo title, statistike, thumbnail
                YouTube.Videos.List statsList = youtube.videos().list("snippet, contentDetails, statistics");
                statsList.setKey(Config.getApiKey());
                statsList.setId(videoId);
                VideoListResponse listResponse = statsList.execute();

                List<Video> videoList = listResponse.getItems();
                Iterator<Video> iteratorSearchResults = videoList.iterator();

                if (!iteratorSearchResults.hasNext()) {
                    Log.d("SCHEDULE", "Empty");
                }

                while (iteratorSearchResults.hasNext()) {

                    Video singleVideo = iteratorSearchResults.next();

                    // ako je pronadeni rezultat video nastavi obradu
                    if (singleVideo.getKind().equals("youtube#video")) {
                        Item temp = new Item();
                        Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                        try {
                            String string = thumbnail.getUrl();
                            InputStream in = new URL(string).openStream();
                            temp.setBmp(BitmapFactory.decodeStream(in));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // puni temp koji je tipa Item i na kraju ga dodaju u listu
                        temp.setTitle(singleVideo.getSnippet().getTitle());
                        temp.setId(singleVideo.getId());
                        temp.setImageUrl(thumbnail.getUrl());
                        temp.setDuration(singleVideo.getContentDetails().getDuration());
                        temp.setViews(singleVideo.getStatistics().getViewCount().toString());
                        temp.setLikes(singleVideo.getStatistics().getLikeCount().toString());
                        temp.setDislikes(singleVideo.getStatistics().getDislikeCount().toString());
                        tempList.add(temp);
                    }
                }

            } else {
                Log.d("SCHEDULE", "Nothing found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
