package hr.leon.croapps.youplay;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.concurrent.CountDownLatch;


public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private String id;
    private String startTime;
    private boolean pause = false;
    private boolean queue = false;
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
    }


    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
        Toast.makeText(this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(Provider provider, final YouTubePlayer player, boolean wasRestored) {

        player.setPlayerStateChangeListener(playerStateChangeListener);
        player.setPlaybackEventListener(playbackEventListener);
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);

        if (!wasRestored) {
            int time = Integer.parseInt(startTime);
            player.loadVideo(id, time * 60);
        }

        Button playButton = (Button) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int time = 0;
                EditText editText = (EditText)findViewById(R.id.timeInput);
                if(editText.getText().length()>0)
                    time = Integer.parseInt(editText.getText().toString());

                if(pause){
                    if(time > 0 && !queue) {
                        player.play();
                        pause = false;
                        queue = true;
                        final CountDownTimer countDownTimer = new CountDownTimer(time * 1000, 1000) {
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
                        player.play();
                        pause = false;
                    }
                }
                else{
                    if(time > 0 && !queue) {
                        player.pause();
                        pause = true;
                        queue = true;
                        final CountDownTimer countDownTimer = new CountDownTimer(time * 1000, 1000) {
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
                        player.pause();
                        pause = true;
                    }
                }
            }
        });
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
