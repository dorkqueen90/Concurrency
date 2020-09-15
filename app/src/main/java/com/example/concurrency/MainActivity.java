package com.example.concurrency;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    int times;
    double result;
    String prog;
    TextView displayResult;
    ProgressBar progress;
    TextView displayProgress;
    SeekBar seekbar;
    TextView timesView;
    ExecutorService threadPool;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newFixedThreadPool(2);
        displayResult = findViewById(R.id.result);
        progress = findViewById(R.id.progressBar);
        displayProgress = findViewById(R.id.progressText);
        seekbar = findViewById(R.id.seekBar);
        timesView = findViewById(R.id.timesText);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timesView.setText(String.valueOf(progress + " Times"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                if(msg.getData().containsKey("progress")){
                    int threadProgress = msg.getData().getInt("progress");
                    prog = " " + threadProgress + "/" + times;
                    progress.setProgress(threadProgress);
                    displayProgress.setText(prog);
                }
                return false;
            }
        });

        findViewById(R.id.asyncButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                times = seekbar.getProgress();
                new DoWorkAsync().execute(times);
            }
        });
        findViewById(R.id.threadButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                times = seekbar.getProgress();
                progress.setVisibility(View.VISIBLE);
                displayProgress.setVisibility(View.VISIBLE);
                progress.setMax(times);

                threadPool.execute(new doWork());
            }
        });
    }

    class DoWorkAsync extends AsyncTask<Integer, Integer, Double>{
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            displayProgress.setVisibility(View.VISIBLE);
            progress.setMax(times);
        }

        @Override //what doInBackground returns
        protected void onPostExecute(Double aDouble) {
            result = aDouble;
            progress.setVisibility(View.INVISIBLE);
            displayProgress.setVisibility(View.INVISIBLE);
            displayResult.setText(String.valueOf(result));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            prog = " " + values[0] + "/" + String.valueOf(times);
            progress.setProgress(values[0]);
            displayProgress.setText(prog);
        }

        @Override //how much work to do
        protected Double doInBackground(Integer... param) {
            double avg = 0;
            for(int i = 0; i < param[0]; i++){
                avg =  avg + HeavyWork.getNumber();
                publishProgress(i);
            }
            return avg / times;
        }
    }

    class doWork implements Runnable{

        @Override
        public void run() {
            double avg = 0;
            for(int i = 0; i < times; i++){
                avg =  avg + HeavyWork.getNumber();
                Bundle bundle = new Bundle();
                bundle.putInt("progress", i);
                Message message = new Message();
                message.setData(bundle);
                handler.sendMessage(message);
            }
            avg = avg / times;
            displayResult.setText(String.valueOf(avg));
            progress.setVisibility(View.INVISIBLE);
            displayProgress.setVisibility(View.INVISIBLE);
        }
    }
}