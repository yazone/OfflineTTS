package io.github.yazone.offline_tts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    public static final int REQUEST_LOAD_MODEL = 0;
    public static final int REQUEST_RUN_MODEL = 1;
    public static final int RESPONSE_LOAD_MODEL_SUCCESSED = 0;
    public static final int RESPONSE_LOAD_MODEL_FAILED = 1;
    public static final int RESPONSE_RUN_MODEL_SUCCESSED = 2;
    public static final int RESPONSE_RUN_MODEL_FAILED = 3;
    // public MediaPlayer mediaPlayer = new MediaPlayer();
    private static final String TAG = YazoneTTS.class.getSimpleName();
    protected ProgressDialog pbLoadModel = null;
    protected ProgressDialog pbRunModel = null;
    // Receive messages from worker thread
    protected Handler receiver = null;
    // Send command to worker thread
    protected Handler sender = null;
    // Worker thread to load&run model
    protected HandlerThread worker = null;
    // UI components of image classification
    protected TextView tvInputSetting;
    protected TextView tvInferenceTime;
    protected Button btn_play;
    protected Button btn_pause;
    protected Button btn_stop;

    protected EditText emt_text;
    // Model settings of image classification
    protected String modelPath = "";
    protected int cpuThreadNum = 1;
    protected String cpuPowerMode = "";
    // protected Predictor predictor = new Predictor();
    int sampleRate = 24000;
    private final String wavName = "tts_output.wav";
    private final String wavFile = Environment.getExternalStorageDirectory() + File.separator + wavName;
    private final String AMmodelName = "fastspeech2_csmsc_arm.nb";
    private final String VOCmodelName = "mb_melgan_csmsc_arm.nb";
    private float[] phones = {};
    private String mCurrentSentence = "请先选择要发音的句子";

    private YazoneTTS yazone_tts = new YazoneTTS();
    static {
        System.loadLibrary("parrot_tts");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                mCurrentSentence = emt_text.getText().toString();
                if (mCurrentSentence.isEmpty()) {
                    mCurrentSentence = "请输入要播放的句子";
                }
                yazone_tts.asyncSynthesis(mCurrentSentence);
                break;
            case R.id.btn_pause:
                break;
            case R.id.btn_stop:
                yazone_tts.stopSynthesis(-1);
                break;
            default:
                break;
        }
    }

    private void copyFileToCache(Context appCtx,String modelPath) {
        if (modelPath.charAt(0) != '/') {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            String realPath = appCtx.getCacheDir() + "/" + modelPath;
            // push model to mobile
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
    }

    private void initAllModels(Context appCtx) {
        copyFileToCache(appCtx,"models");
        // copyFileToCache(this,"models/tts/fs2_encoder_mix_16k_var.nb");
        // copyFileToCache(this,"models/tts/fs2_decoder_mix_16k_var.nb");
        // copyFileToCache(this,"models/tts/mb_melgan_mix_16k_var.nb");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestAllPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        Spinner spinner = findViewById(R.id.spinner1);
        // 建立数据源
        String[] sentences = getResources().getStringArray(R.array.text);
        // 建立 Adapter 并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sentences);
        // 第一个参数表示在哪个 Activity 上显示，第二个参数是系统下拉框的样式，第三个参数是数组。
        spinner.setAdapter(adapter);//绑定Adapter到控件
        spinner.setOnItemSelectedListener(this);

        btn_play = findViewById(R.id.btn_play);
        btn_pause = findViewById(R.id.btn_pause);
        btn_stop = findViewById(R.id.btn_stop);

        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

        btn_play.setVisibility(View.VISIBLE);
        //btn_pause.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);

        emt_text = findViewById(R.id.emt_text_id);

        // Clear all setting items to avoid app crashing due to the incorrect settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        initAllModels(MainActivity.this);
        // 初始化TTS
        yazone_tts.init(MainActivity.this.getCacheDir().getPath());

        // Prepare the worker thread for mode loading and inference
        receiver = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                switch (msg.what) {
                    case RESPONSE_LOAD_MODEL_SUCCESSED:
                        pbLoadModel.dismiss();
                        onLoadModelSuccessed();
                        break;
                    case RESPONSE_LOAD_MODEL_FAILED:
                        pbLoadModel.dismiss();
                        Toast.makeText(MainActivity.this, "Load model failed!", Toast.LENGTH_SHORT).show();
                        onLoadModelFailed();
                        break;
                    case RESPONSE_RUN_MODEL_SUCCESSED:
                        pbRunModel.dismiss();
                        onRunModelSuccessed();
                        break;
                    case RESPONSE_RUN_MODEL_FAILED:
                        pbRunModel.dismiss();
                        Toast.makeText(MainActivity.this, "Run model failed!", Toast.LENGTH_SHORT).show();
                        onRunModelFailed();
                        break;
                    default:
                        break;
                }
                */
            }
        };

        worker = new HandlerThread("Predictor Worker");
        worker.start();
        sender = new Handler(worker.getLooper()) {
            public void handleMessage(Message msg) {
                /*
                switch (msg.what) {
                    case REQUEST_LOAD_MODEL:
                        // Load model and reload test image
                        if (onLoadModel()) {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_FAILED);
                        }
                        break;
                    case REQUEST_RUN_MODEL:
                        // Run model if model is loaded
                        if (onRunModel()) {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_FAILED);
                        }
                        break;
                    default:
                        break;
                }
                 */
            }
        };

        // Setup the UI components
        tvInputSetting = findViewById(R.id.tv_input_setting);
        tvInferenceTime = findViewById(R.id.tv_inference_time);
        tvInputSetting.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean settingsChanged = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String model_path = sharedPreferences.getString(getString(R.string.MODEL_PATH_KEY),getString(R.string.MODEL_PATH_DEFAULT));

        settingsChanged |= !model_path.equalsIgnoreCase(modelPath);

        int cpu_thread_num = Integer.parseInt(sharedPreferences.getString(getString(R.string.CPU_THREAD_NUM_KEY),getString(R.string.CPU_THREAD_NUM_DEFAULT)));
        settingsChanged |= cpu_thread_num != cpuThreadNum;
        String cpu_power_mode =
                sharedPreferences.getString(getString(R.string.CPU_POWER_MODE_KEY),
                        getString(R.string.CPU_POWER_MODE_DEFAULT));
        settingsChanged |= !cpu_power_mode.equalsIgnoreCase(cpuPowerMode);

        if (settingsChanged) {
            modelPath = model_path;
            cpuThreadNum = cpu_thread_num;
            cpuPowerMode = cpu_power_mode;
            // Update UI
            tvInputSetting.setText("Model: " + modelPath.substring(modelPath.lastIndexOf("/") + 1) + "\n" + "CPU" +
                    " Thread Num: " + cpuThreadNum + "\n" + "CPU Power Mode: " + cpuPowerMode + "\n");
            tvInputSetting.scrollTo(0, 0);
            // Reload model if configure has been changed
            // loadModel();
        }
    }

    public void loadModel() {
        // pbLoadModel = ProgressDialog.show(this, "", "Loading model...", false, false);
        sender.sendEmptyMessage(REQUEST_LOAD_MODEL);
    }

    public void runModel() {
        // pbRunModel = ProgressDialog.show(this, "", "Running model...", false, false);
        sender.sendEmptyMessage(REQUEST_RUN_MODEL);
    }

    public boolean onLoadModel() {
        // return predictor.init(MainActivity.this, modelPath, AMmodelName, VOCmodelName, cpuThreadNum,cpuPowerMode);
        return true;
    }

    public boolean onRunModel() {
        // return predictor.isLoaded() && predictor.runModel(phones);
        return true;
    }

    public boolean onLoadModelSuccessed() {
        // Load test image from path and run model
//        runModel();
        return true;
    }

    public void onLoadModelFailed() {
    }

    public void onRunModelSuccessed() {
        // Obtain results and update UI
        btn_play.setVisibility(View.VISIBLE);
        // btn_pause.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);
        /*
        tvInferenceTime.setText("Inference done！\nInference time: " + predictor.inferenceTime() + " ms"
                + "\nRTF: " + predictor.inferenceTime() * sampleRate / (predictor.wav.length * 1000) + "\nAudio saved in " + wavFile);
        try {
            Utils.rawToWave(wavFile, predictor.wav, sampleRate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            // 初始化 MediaPlayer
            initMediaPlayer();
        }
         */
    }

    public void onRunModelFailed() {
    }


    public void onSettingsClicked() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.settings:
                onSettingsClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        worker.quit();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        yazone_tts.stopSynthesis(-1);
    }

    private boolean requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
            return false;
        }
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            mCurrentSentence = parent.getAdapter().getItem(position).toString();
            emt_text.setText(mCurrentSentence);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
