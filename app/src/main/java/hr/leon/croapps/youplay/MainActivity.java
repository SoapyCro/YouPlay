package hr.leon.croapps.youplay;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.BufferedReader;
import java.io.IOException;
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

                // pozovi async task koji će s neta skinut suggested quarrye i prikazat ih
                showSuggestions(query);
            }
        });


        Button searchButton = (Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) findViewById(R.id.search_button);
                button.setBackgroundResource(R.drawable.btn_search);

                EditText editText = (EditText)findViewById(R.id.editText);
                showResults(editText.getText().toString());
            }
        });
    }
    private void showResults(String s){

        AsyncTask<String, Void, ArrayList<Item>> task = new AsyncTask<String, Void, ArrayList<Item>>() {
            @Override
            protected ArrayList<Item> doInBackground(String... params) {
                YouTube youtube;

                ArrayList<Item> tempList = new ArrayList<>();

                try{
                    String queryTerm = params[0];

                    youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
                            new HttpRequestInitializer() {
                                public void initialize(HttpRequest request) throws IOException{

                                }
                            })
                            .setApplicationName("VideoStreamer")
                            .build();

                    YouTube.Search.List search = youtube.search().list("id,snippet");
                    search.setKey(apiKey);
                    search.setQ(queryTerm);
                    search.setType("video");
                    search.setFields("items(id/kind,id/videoId,snippet/description,snippet/title,snippet/thumbnails/default/url)");
                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    SearchListResponse searchResponse = search.execute();

                    List<SearchResult> searchResultList = searchResponse.getItems();
                    if(searchResultList != null){
                        //prettyPrint(searchResultList.iterator(), queryTerm);
                        Iterator<SearchResult> iteratorSearchResults = searchResultList.iterator();
                        if (!iteratorSearchResults.hasNext()) {
                            // error TODO
                        }

                        while (iteratorSearchResults.hasNext()) {
                            Item temp = new Item();
                            SearchResult singleVideo = iteratorSearchResults.next();
                            ResourceId rId = singleVideo.getId();

                            // Confirm that the result represents a video. Otherwise, the
                            // item will not contain a video ID.
                            if (rId.getKind().equals("youtube#video")) {
                                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                                temp.setId(rId.getVideoId());
                                temp.setTitle(singleVideo.getSnippet().getTitle());
                                temp.setImageUrl(thumbnail.getUrl());

                                tempList.add(temp);
                            }
                        }

                    }else{
                        // error TODO dodaj u LOG!!!!!!!!!
                    }
                }catch(GoogleJsonResponseException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return tempList;
            }


            @Override
            protected void onPostExecute(ArrayList<Item> list) {
                //Toast.makeText(MainActivity.this, list.get(1).getTitle(), Toast.LENGTH_LONG).show();
                ListAdapter adapter = new CustomAdapter(MainActivity.this, list);
                ListView listView = (ListView)findViewById(R.id.listView);
                listView.setAdapter(adapter);
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

                for(int i = 0; i < 10; i++)
                    list[i] = "";

                if(passing[0].length() > 0) {
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
                        }
                        else list[0] = "";
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("SCHEDULE", "exception neki");
                    }
                }
                else list[0] = "";
                return list;
            }

            @Override
            protected void onPostExecute(String[] list) {
                if(list[0].length() > 0){
                    // updateaj dropdown listu suggestiona
                    AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>
                            (MainActivity.this, android.R.layout.simple_list_item_1,list);
                    editText.setThreshold(1);
                    editText.setAdapter(adapter);

                    // refresh
                    editText.showDropDown();
                   /* adapter.notifyDataSetChanged();
                    if (!editText.isPopupShowing()) {
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
