package io.github.yazone.offline_tts;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class YazoneTTS {
    private AudioTrack mAudioTrack;

    public void init(String resPath)
    {
        Log.i("YazoneTTS", "资源路径××××××××××："+resPath);
        initAudioTrack();
        init_system(resPath);
    }

    public void asyncSynthesis(String text) {
        Log.i("YazoneTTS", "asyncSynthesis "+ text);
        stopSynthesis(-1);
        async_synthsis(text);
        mAudioTrack.play();
    }

    public void stopSynthesis(int task_id) {
        Log.i("YazoneTTS", "停止合成××××××××××");
        stop_synthsis(-1);
        mAudioTrack.pause();
        mAudioTrack.flush();
        Log.i("YazoneTTS", "停止合成完成××××××××××");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int pcm_callback(int task_id, int block_id, float[] pcm_buffer, int pcm_length)
    {
        Log.i("YazoneTTS", "pcm_callback block id is "+block_id + " 时长:"+ pcm_length/16+"ms");
        mAudioTrack.write(pcm_buffer,0,pcm_length,AudioTrack.WRITE_BLOCKING);
        return 0;
    }

    private void initAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(16000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                minBufferSize,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    private native int init_system(String res_path);

    private native int async_synthsis(String text);

    private native void stop_synthsis(int task_id);

}
