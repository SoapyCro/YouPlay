package hr.leon.croapps.youplay;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.ArrayList;

public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    private String id;
    private String startTime;
    private boolean pause = false;
    private boolean queue = false;
    private CountDownTimer countDownTimer = null;

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
        setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
        setContentView(R.layout.activity_player);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        startTime = bundle.getString("startTime");

        YouTubePlayerView youTubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player);
        youTubePlayerView.initialize(Config.getApiKey(), this);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            showResults(" ");

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);

                Intent intent = new Intent(PlayerActivity.this, PlayerActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("startTime", "0");
                startActivity(intent);
                finish();
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
                if ((lastInScreen == totalItemCount && showDone)) {
                    if (preLast != lastItem) {
                        preLast = lastItem;
                        pageNum++;
                        showResults(" ");
                    }
                }
            }
        });

    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
        Toast.makeText(this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(Provider provider, final YouTubePlayer player, boolean wasRestored) {

        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);

        //if (!wasRestored) {
        int time = Integer.parseInt(startTime);
        player.loadVideo(id, time * 1000);
        //}

        /*int time = Integer.parseInt(startTime);
        player.loadVideo(id, time * 1000);*/

        // prekoci n sekundi, ako nema nista preskoci 10 sekundi
        Button skipForward = (Button) findViewById(R.id.skipForward);
        skipForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time;
                EditText editText = (EditText) findViewById(R.id.timeInput);
                if (editText.getText().length() > 0) {
                    time = Integer.parseInt(editText.getText().toString());
                    if (time > 0)
                        player.seekRelativeMillis(time * 1000);
                }
                else
                    player.seekRelativeMillis(10 * 1000);
            }
        });

        // vrati se nazad n sekundi, ako nema nista vrati 10 sekundi
        Button skipBack = (Button) findViewById(R.id.skipBack);
        skipBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time;
                EditText editText = (EditText) findViewById(R.id.timeInput);
                if (editText.getText().length() > 0) {
                    time = Integer.parseInt(editText.getText().toString());
                    if (time > 0)
                        player.seekRelativeMillis(-time * 1000);

                }
                else
                    player.seekRelativeMillis(-10*1000);
            }
        });
        // skoci na n sekundi, ako nema nista skoci na pocetak
        Button jumpTo = (Button) findViewById(R.id.jumpTo);
        jumpTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time;
                EditText editText = (EditText) findViewById(R.id.timeInput);
                if (editText.getText().length() > 0) {
                    time = Integer.parseInt(editText.getText().toString());
                    if (time > 0)
                        player.seekToMillis(time * 1000);
                    else
                        player.seekToMillis(player.getDurationMillis() - time * 1000);
                }
                else
                    player.seekToMillis(0);

                editText.setText("");
            }
        });
        // stop gumb, ubi activity
        Button stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // play / pause gumb
        // ak je play prikazan i user upise neki broj onda ce player
        // pustit video od trenutne pozicije do trenutna pozicija + n sekundi
        Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = 0;
                EditText editText = (EditText)findViewById(R.id.timeInput);
                if(editText.getText().length()>0)
                    time = Integer.parseInt(editText.getText().toString());
                    editText.setText("");
                // video je trenutno pauziran
                if(pause){
                    if(time > 0 && !queue) {
                        player.play();
                        pause = false;
                        queue = true;
                        countDownTimer = new CountDownTimer(time * 1000, 1000) {
                            public void onTick(long millisUnitlFinished) {
                            }
                            public void onFinish() {
                                player.pause();
                                pause = true;
                                queue = false;
                            }
                        }.start();
                    }
                    else {
                        if(countDownTimer != null)
                            countDownTimer.cancel();
                            queue = false;
                        player.play();
                        pause = false;
                    }
                }
                else{
                    if(time > 0 && !queue) {
                        player.pause();
                        pause = true;
                        queue = true;
                        countDownTimer = new CountDownTimer(time * 1000, 1000) {
                            public void onTick(long millisUnitlFinished) {
                            }
                            public void onFinish() {
                                player.play();
                                pause = false;
                                queue = false;
                            }
                        }.start();
                    }
                    else {
                        if(countDownTimer != null)
                            countDownTimer.cancel();
                            queue = false;

                        player.pause();
                        pause = true;
                    }
                }
            }
        });
    }

    private void showResults(String s) {
        AsyncTask<String, Void, ArrayList<Item>> task = new AsyncTask<String, Void, ArrayList<Item>>() {
            @Override
            protected ArrayList<Item> doInBackground(String... params) {
                // nadi search rezultate i napuni ih u listu
                search.start(params[0], NUMBER_OF_VIDEOS_RETURNED, pageNum, id);
                return search.getTempList();
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                showDone = false;
                if(loadingMore == null)
                    loadingMore = Toast.makeText(PlayerActivity.this, "", Toast.LENGTH_LONG);

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
                adapter = new CustomAdapter(PlayerActivity.this, list);
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

    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {
        }

        @Override
        public void onPaused(){
            Button playButton = (Button) findViewById(R.id.playButton);
            playButton.setBackgroundResource(R.drawable.gtk_media_play_ltr);
        }

        @Override
        public void onPlaying() {
            Button playButton = (Button) findViewById(R.id.playButton);
            playButton.setBackgroundResource(R.drawable.gtk_media_pause);
        }

        @Override
        public void onSeekTo(int arg0) {
        }

        @Override
        public void onStopped() {
        }

    };

    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {


        @Override
        public void onAdStarted() {
        }

        @Override
        public void onError(ErrorReason arg0) {
        }

        @Override
        public void onLoaded(String arg0) {
        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {
        }

        @Override
        public void onVideoStarted() {
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer != null)
            countDownTimer.cancel();
        finish();
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
