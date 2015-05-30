package hr.leon.croapps.youplay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends Activity {
    private List<Item> list = new ArrayList<>();
    private static final String apiKey = "AIzaSyBNR6yJGooeMvYaNQldk508oyekgEwhCFM";
    boolean searchStarted = false;
    private static final long NUMBER_OF_VIDEOS_RETURNED = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // tema bez actionbara
        setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        setContentView(R.layout.activity_main);

        // unos search quarrya i prikazz suggested quarrya
        AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // svaki put kad se promijeni tekst updateaj suggested listu
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                // nađi što se nalazi u search boxu i makne spaceove i naše znakove
                String query = s.toString();

                query = query.toLowerCase().replaceAll("\\s", "%25").replaceAll("č", "c").
                        replaceAll("ć", "c").replaceAll("ž", "z").replaceAll("š", "s").replaceAll("đ", "d");

                AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                // pozovi async task koji će s neta skinut suggested quarrye i prikazat ih
                showSuggestions(query);
            }
        });


        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.dismissDropDown();
                showResults(editText.getText().toString());
            }
        });


        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) findViewById(R.id.search_button);
                button.setBackgroundResource(R.drawable.btn_search);

                AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                if(!searchStarted){
                    showResults(editText.getText().toString());
                    searchStarted = true;
                }
            }
        });
    }

    private void showResults(String s) {
        AsyncTask<String, Void, ArrayList<Item>> task = new AsyncTask<String, Void, ArrayList<Item>>() {
            @Override
            protected ArrayList<Item> doInBackground(String... params) {

                YouTube youtube;
                ArrayList<Item> tempList = new ArrayList<>();

                try {
                    String queryTerm = params[0];

                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                            new HttpRequestInitializer() {
                                public void initialize(HttpRequest request) throws IOException {

                                }
                            })
                            .setApplicationName("VideoStreamer")
                            .build();

                    YouTube.Search.List search = youtube.search().list("id,snippet");
                    search.setKey(apiKey);
                    search.setQ(queryTerm);
                    search.setType("video");

                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    SearchListResponse searchResponse = search.execute();
                    List<SearchResult> searchResultList = searchResponse.getItems();
                    List<String> videoIds = new ArrayList<String>();

                    if (searchResultList != null) {

                        for (SearchResult searchResult : searchResultList) {
                            videoIds.add(searchResult.getId().getVideoId());
                        }
                        Joiner stringJoiner = Joiner.on(',');
                        String videoId = stringJoiner.join(videoIds);

                        YouTube.Videos.List statsList = youtube.videos().list("snippet, contentDetails, statistics");
                        statsList.setKey(apiKey);
                        statsList.setId(videoId);
                        VideoListResponse listResponse = statsList.execute();

                        List<Video> videoList = listResponse.getItems();
                        Iterator<Video> iteratorSearchResults = videoList.iterator();

                        if (!iteratorSearchResults.hasNext()) {
                            Log.d("SCHEDULE", "Empty");
                        }

                        while (iteratorSearchResults.hasNext()) {

                            Video singleVideo = iteratorSearchResults.next();

                            // Confirm that the result represents a video. Otherwise, the
                            // item will not contain a video ID.
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
                return tempList;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(ArrayList<Item> list) {
                //Toast.makeText(MainActivity.this, list.get(6).getTitle(), Toast.LENGTH_LONG).show();

                ListAdapter adapter = new CustomAdapter(MainActivity.this, list);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
                searchStarted = false;
            }
        };
        task.execute(s);
    }

    // async task koji s neta skida suggestione i prikazuje ih
    private void showSuggestions(CharSequence s) {
        AsyncTask<String, Void, String[]> task = new AsyncTask<String, Void, String[]>() {

            @Override
            protected String[] doInBackground(String... passing) {

                StringBuilder string = new StringBuilder("");
                String[] list = new String[10];

                for (int i = 0; i < 10; i++)
                    list[i] = "";

                if (passing[0].length() > 0) {
                    try {
                        // skini s neta suggestione
                        String temp_url = "http://suggestqueries.google.com/complete/search?q=" + passing[0] + "&client=toolbar&ds=yt&hl=en";
                        URL oracle = new URL(temp_url);
                        URLConnection yc = oracle.openConnection();
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));

                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            string.append(inputLine);
                        }
                        // ako postoji bar 1 suggestion (ako ne nađe ništa ima cca 40 znakova) napuni listu stringova sa suggestionima
                        if (string.length() > 50) {
                            // u listu punimo ono što se u xml-u nalazi imzmeđu " "
                            Pattern p = Pattern.compile("\"(.*?)\"");
                            Matcher m = p.matcher(string);
                            int i = 0;
                            while (m.find()) {
                                if (!(m.group(1).equals("1.0"))) {
                                    list[i] = m.group(1);
                                    // apostrof
                                    list[i] = list[i].replace("&#39;", "\'");
                                    i++;
                                }
                            }
                        } else list[0] = "";
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("SCHEDULE", "exception neki");
                    }
                } else list[0] = "";
                return list;
            }

            @Override
            protected void onPostExecute(String[] list) {
                if (list[0].length() > 0) {
                    // updateaj dropdown listu suggestiona
                    AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>
                            (MainActivity.this, android.R.layout.simple_list_item_1, list);
                    editText.setThreshold(1);
                    editText.setAdapter(adapter);

                    adapter.notifyDataSetChanged();
                    /*if (!editText.isPopupShowing()) {
                        editText.showDropDown();
                    }*/
                }
            }
        };
        task.execute(s.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
