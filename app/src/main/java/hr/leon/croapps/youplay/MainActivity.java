package hr.leon.croapps.youplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    boolean searchStarted = false;
    private static final long NUMBER_OF_VIDEOS_RETURNED = 15;
    private int preLast;
    private int pageNum = 0;
    private Search search = new Search();
    private boolean showDone = false;
    private ListAdapter adapter;
    private Toast loadingMore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // tema bez actionbara
        setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        setContentView(R.layout.activity_main);

        showResults(" ");

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

                // nadi sto se nalazi u search boxu i makne spaceove i nase znakove
                String query = s.toString();

                query = query.toLowerCase().replaceAll("\\s", "%25").replaceAll("č", "c").
                        replaceAll("ć", "c").replaceAll("ž", "z").replaceAll("š", "s").replaceAll("đ", "d");

                // pozovi async task koji ce s neta skinut suggested quarrye i prikazat ih
                showSuggestions(query);
            }
        });

        // kad user klikne na listview pozovi novi activity i salji mu id i startTime 0
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("startTime", "0");
                startActivity(intent);
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if((lastInScreen == totalItemCount && showDone) ) {
                    if(preLast!=lastItem){
                        preLast = lastItem;
                        AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                        pageNum++;
                        showResults(editText.getText().toString());
                    }
                }
            }
        });


        // kad user klikne na dropdown od autocompletetextviewa zapocni pretragu, makni tipkovnicu i skloni dropdown
        editText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                // tipkovnica
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                // dropdown
                editText.dismissDropDown();
                // trazi rezultate i prikazi ih
                pageNum = 0;
                showResults(editText.getText().toString());
            }
        });

        // kad user pritisne gotovo na tipkovnici zapocni pretragu
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    editText.dismissDropDown();
                    pageNum = 0;
                    showResults(editText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // kad user stisne search button sakrij dropdown, makni tipkovnicu i zapocni pretragu
        // i blokiraj mashanje po gumbu (kad ga jednom stisne ide 1 pretraga i dok ona ne zavrsi ne prihvaca se novo zapocinjanje pretrage
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) findViewById(R.id.search_button);
                button.setBackgroundResource(R.drawable.btn_search);

                AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                pageNum = 0;
                if (!searchStarted) {
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
                // nadi search rezultate i napuni ih u listu
                search.start(params[0], NUMBER_OF_VIDEOS_RETURNED, pageNum, "0");
                return search.getTempList();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showDone = false;
                if(loadingMore == null)
                    loadingMore = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);

                if(loadingMore.getView().getWindowVisibility() == View.VISIBLE){
                    loadingMore.cancel();
                }

                loadingMore.setDuration(Toast.LENGTH_LONG);
                loadingMore.setText("Loading...");
                loadingMore.show();
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(ArrayList<Item> list) {
                //Toast.makeText(MainActivity.this, list.get(6).getTitle(), Toast.LENGTH_LONG).show();

                // prikazi rezultate pretrage u CustomAdapteru
                ListView listView = (ListView) findViewById(R.id.listView);
                int index = listView.getFirstVisiblePosition();
                View v = listView.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                adapter = new CustomAdapter(MainActivity.this, list);
                listView.setAdapter(adapter);
                listView.requestLayout();
                if(pageNum != 0) listView.setSelectionFromTop(index, top);

                if(loadingMore.getView().getWindowVisibility() == View.VISIBLE){
                    loadingMore.cancel();
                }
                loadingMore.setText("Done");
                loadingMore.setDuration(Toast.LENGTH_LONG);
                loadingMore.show();

                searchStarted = false;
                showDone = true;
            }
        };
        task.execute(s);
    }

    // async task koji s neta skida suggestione i prikazuje ih
    private void showSuggestions(CharSequence s) {
        AsyncTask<String, Void, String[]> task = new AsyncTask<String, Void, String[]>() {

            @Override
            protected String[] doInBackground(String... passing) {
                // Nadi prijedloge i napuni ih u listu
                Suggestions suggestions = new Suggestions();
                suggestions.find(passing[0]);
                return suggestions.getList();
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
