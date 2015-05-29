package hr.leon.croapps.youplay;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



// sredi za šćčžđ !!!!!

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                TextView textView = (TextView)findViewById(R.id.myOutputBox);

                String query = s.toString();

                query = query.replaceAll("\\s", "%");
                textView.setText(query);
                showSuggestions(query);
            }
        });

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) findViewById(R.id.button);
                button.setBackgroundResource(R.drawable.btn_search);
            }
        });
    }

    private void showSuggestions(CharSequence s) {
        AsyncTask<String, Void, String[]> task = new AsyncTask<String, Void, String[]>() {

            @Override
            protected String[] doInBackground(String... passing) {

                StringBuilder string = new StringBuilder("");
                String[] list = new String[10];
                //list[0] = "";
                for(int i = 0; i < 10; i++)
                    list[i] = "";
                if(passing[0].length() > 0) {
                    try {

                        String temp_url = "http://suggestqueries.google.com/complete/search?q=" + passing[0] + "&client=toolbar&ds=yt&hl=en";

                        URL oracle = new URL(temp_url);
                        URLConnection yc = oracle.openConnection();
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                yc.getInputStream()));

                        String inputLine;


                        while ((inputLine = in.readLine()) != null) {
                            string.append(inputLine);
                        }
                        if (string.length() > 50) {
                            Pattern p = Pattern.compile("\"(.*?)\"");
                            Matcher m = p.matcher(string);

                            int i = 0;
                            while (m.find()) {
                                if (!(m.group(1).equals("1.0"))) {
                                    list[i] = m.group(1);
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
                    AutoCompleteTextView editText = (AutoCompleteTextView) findViewById(R.id.editText);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>
                            (MainActivity.this, android.R.layout.simple_list_item_1,list);
                    editText.setThreshold(1);
                    editText.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if (!editText.isPopupShowing()) {
                        editText.showDropDown();
                    }
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
