package engineer.davidauza.samplemusicplayer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /**
     * The time a song can jump whether backward or forward.
     */
    private static final int JUMP_TIME = 5000;
    /**
     * Keeps track if the media player has already started to play a song.
     */
    private static boolean mHasStarted;
    /**
     * This button jumps 5 seconds back of the media player progress.
     */
    private Button mBackButton;
    /**
     * This button pauses the playback.
     */
    private Button mPauseButton;
    /**
     * This button starts or resumes the playback.
     */
    private Button mPlayButton;
    /**
     * This button jumps 5 seconds forward of the media player progress.
     */
    private Button mForwardButton;
    /**
     * The media player needed to playback the song.
     */
    private MediaPlayer mMediaPlayer;
    /**
     * The starting time position of the song.
     */
    private double mStartingPosition;
    /**
     * The total duration of the song.
     */
    private double mFinalTime;
    /**
     * Handler need to update the UI according to the media player progress.
     */
    private Handler mHandler = new Handler();
    /**
     * The bar to keep track of the song's progress.
     */
    private SeekBar mSeekBar;
    /**
     * The TextView which contains the left timer above the SeekBar.
     */
    private TextView mLeftTimerTextView;
    /**
     * The TextView which contains the right timer above the SeekBar.
     */
    private TextView mRightTimerTextView;
    /**
     * Contains instructions on how to update the UI according to MediaPlayer progress.
     */
    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            mStartingPosition = mMediaPlayer.getCurrentPosition();
            setTimer(mStartingPosition, mLeftTimerTextView);
            mSeekBar.setProgress((int) mStartingPosition);
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find Buttons
        mBackButton = findViewById(R.id.back_button);
        mPauseButton = findViewById(R.id.pause_button);
        mPauseButton.setEnabled(false);
        mPlayButton = findViewById(R.id.play_button);
        mForwardButton = findViewById(R.id.forward_button);

        // Find TextView timers
        mLeftTimerTextView = findViewById(R.id.timer_left);
        mRightTimerTextView = findViewById(R.id.timer_right);

        // Set up MediaPlayer
        mMediaPlayer = MediaPlayer.create(this, R.raw.colombia_anthem);

        // Set up SeekBar
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setClickable(false);

        setPlayButton();

        setPauseButton();

        setBackButton();

        setForwardButton();
    }

    /**
     * This method sets up the play button functioning.
     */
    private void setPlayButton() {
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inform the user the sound is playing now
                Toast.makeText(getApplicationContext(), getString(R.string.main_toast_playing),
                        Toast.LENGTH_SHORT).show();

                mMediaPlayer.start();

                mStartingPosition = mMediaPlayer.getCurrentPosition();
                mFinalTime = mMediaPlayer.getDuration();

                if (!MainActivity.mHasStarted) {
                    mSeekBar.setMax((int) mFinalTime);
                    MainActivity.mHasStarted = true;
                }

                setTimer(mStartingPosition, mLeftTimerTextView);

                setTimer(mFinalTime, mRightTimerTextView);

                mSeekBar.setProgress((int) mStartingPosition);

                mHandler.postDelayed(updateSongTime, 1000);

                mPauseButton.setEnabled(true);

                mPlayButton.setEnabled(false);
            }
        });
    }

    /**
     * This method sets up the pause button functioning.
     */
    private void setPauseButton() {
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inform the user the sound is being paused now
                Toast.makeText(getApplicationContext(), getString(R.string.main_toast_pausing),
                        Toast.LENGTH_SHORT).show();
                mMediaPlayer.pause();
                mPlayButton.setEnabled(true);
                mPauseButton.setEnabled(false);
            }
        });
    }

    /**
     * This method sets up the back button functioning.
     */
    private void setBackButton() {
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) mStartingPosition;

                // Check if the current position of the MediaPlayer is greater than 5000
                // milliseconds.
                if (temp - MainActivity.JUMP_TIME > 0) {
                    // Jump back 5 seconds.
                    mStartingPosition -= MainActivity.JUMP_TIME;
                    mMediaPlayer.seekTo((int) mStartingPosition);
                    // Inform the user he has jumped back 5 seconds.
                    Toast.makeText(getApplicationContext(), getString(R.string.main_toast_back),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Inform the user it is not possible to jump back right now
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.main_toast_back_not_possible),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method sets up the forward button functioning.
     */
    private void setForwardButton() {
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) mStartingPosition;

                // Check if the current position of the MediaPlayer + 5 seconds is less than or
                // equal to the song duration.
                if ((temp + MainActivity.JUMP_TIME <= mFinalTime)) {
                    // Jump forward 5 seconds.
                    mStartingPosition += MainActivity.JUMP_TIME;
                    mMediaPlayer.seekTo((int) mStartingPosition);
                    // Inform the user he has jumped forward 5 seconds.
                    Toast.makeText(getApplicationContext(), getString(R.string.main_toast_forward),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Inform the user it is not possible to jump forward right now
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.main_toast_forward_not_possible),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * This method updates an indicated timer TextView according to MediaPlayer data.
     *
     * @param pTime     is the time used to update the TextView.
     * @param pTextView is the TextView to update.
     */
    private void setTimer(double pTime, TextView pTextView) {
        long songMinutes = millisecondsToMinutes(pTime);
        long songSeconds = millisecondsToSeconds(pTime) - (songMinutes * 60);
        pTextView.setText(getString(R.string.main_timer, songMinutes, songSeconds));
    }

    /**
     * This method converts a given time in milliseconds to a time in minutes
     *
     * @param pMilliseconds The given time in milliseconds.
     * @return The time in minutes.
     */
    private long millisecondsToMinutes(double pMilliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes((long) pMilliseconds);
    }

    /**
     * This method converts a given time in milliseconds to a time in seconds
     *
     * @param pMilliseconds The given time in milliseconds.
     * @return The time in seconds.
     */
    private long millisecondsToSeconds(double pMilliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds((long) pMilliseconds);
    }
}