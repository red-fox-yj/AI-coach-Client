package com.example.ai_coach;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class camera extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = "MainActivity";
    private SurfaceView mSurfaceview;
    private Button mBtnStartStop;
    private Button mBtnPlay;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private String filename;
    private TextView textView;
    private int text = 0;
    private BluetoothAdapter mBluetoothAdapter;
    //要连接的目标蓝牙设备（Windows PC电脑的名字）。自己更改
    private final String TARGET_DEVICE_NAME = "DESKTOP-EE61KKE";
    //UUID必须是Android蓝牙客户端和Windows PC电脑端一致。00001101-0000-1000-8000-00805F9B34FB
    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    // 通过广播接收系统发送出来的蓝牙设备发现通知。
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                if (name != null)
                {
                    Log.d(TAG, "发现蓝牙设备:" + name);
                    System.out.println("发现蓝牙设备:" + name);
                }
                if (name != null && name.equals("PHIL-PC")) {
                    Log.d(TAG, "发现目标蓝牙设备，开始线程连接");
                    System.out.println("发现目标蓝牙设备，开始线程连接");
                    // 蓝牙搜索是非常消耗系统资源开销的过程，一旦发现了目标感兴趣的设备，可以关闭扫描。
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        }
    };


    /**
     * 该线程往蓝牙服务器端发送文件数据。
     */
    private class ClientThread extends Thread {
        @Override
        public void run() {
            System.out.println("点击上传按钮");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = getPairedDevices();
            BluetoothSocket socket;
            try {
                assert device != null;
                socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));

                Log.d(TAG, "连接蓝牙服务端...");
                System.out.println("连接蓝牙服务端...");
                socket.connect();
                Log.d(TAG, "连接建立.");
                System.out.println("连接建立");

                // 开始往服务器端发送数据。
                Log.d(TAG, "开始往蓝牙服务器发送数据...");
                System.out.println("开始往蓝牙服务器发送数据...");
                sendDataToServer(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendDataToServer(BluetoothSocket socket) {
            try {
                File i=new File(path);
                FileInputStream fis = new FileInputStream(i);
                System.out.println("第1步");
                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                System.out.println("第2步");

                byte[] buffer = new byte[1024*1024];
                int c;
                int p=1;
                while (true) {
                    c = fis.read(buffer);
                    if (c == -1) {
                        //发送用户选择信息
                        int base;
                        byte[] buffer1 = new byte[1];
                        Intent intent =getIntent();
                        String first = intent.getStringExtra("model");
                        if(first.equals("model_1")){//姿态纠正模式
                            base=10000000;
                        }else{//动作评估模式
                            base=20000000;
                        }
                        String second = intent.getStringExtra("activity");
                        switch (second){
                            case "引体向上":writeToTXT(String.valueOf(base+0)+filename);
                                break;
                            case "下蹲":writeToTXT(String.valueOf(base+1)+filename);
                                break;
                        }
                        File j=new File("/storage/emulated/0/AI-coach/choose.txt");
                        FileInputStream fiss = new FileInputStream(j);
                        c = fiss.read(buffer);
                        System.out.println("读取字节数"+c);
                        bos.write(buffer, 0, c);
                        fiss.close();

                        Log.d(TAG, "读取结束");
                        System.out.println("读取结束");
                        break;
                    } else {
                        Log.d(TAG, "读取"+c);
                        System.out.println("读取编号"+p);
                        p++;
                        bos.write(buffer, 0, c);
                    }
                }

                bos.flush();
                fis.close();

                bos.close();

                Log.d(TAG, "发送文件成功");
                System.out.println("发送文件成功");
                this.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //线程结束
    }

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            text++;
            textView.setText(text+"");
            handler.postDelayed(this,1000);
        }
    };

//    /**
//     * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
//     *
//     * @param isPortrait 是否竖屏
//     * @param surfaceWidth 需要被进行对比的原宽
//     * @param surfaceHeight 需要被进行对比的原高
//     * @param preSizeList 需要对比的预览尺寸列表
//     * @return 得到与原宽高比例最接近的尺寸
//     */
//    public static  Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
//        int reqTmpWidth;
//        int reqTmpHeight;
//        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
//        if (isPortrait) {
//            reqTmpWidth = surfaceHeight;
//            reqTmpHeight = surfaceWidth;
//        } else {
//            reqTmpWidth = surfaceWidth;
//            reqTmpHeight = surfaceHeight;
//        }
//        //先查找preview中是否存在与surfaceview相同宽高的尺寸
//        for(Camera.Size size : preSizeList){
//            if((size.width == reqTmpWidth) && (size.height == reqTmpHeight)){
//                return size;
//            }
//        }
//
//        // 得到与传入的宽高比最接近的size
//        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
//        float curRatio, deltaRatio;
//        float deltaRatioMin = Float.MAX_VALUE;
//        Camera.Size retSize = null;
//        for (Camera.Size size : preSizeList) {
//            curRatio = ((float) size.width) / size.height;
//            deltaRatio = Math.abs(reqRatio - curRatio);
//            if (deltaRatio < deltaRatioMin) {
//                deltaRatioMin = deltaRatio;
//                retSize = size;
//            }
//        }
//
//        return retSize;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        System.out.println("onCreate");
        //申请权限
        permissionRequest();
        //取得启动该Activity的Intent对象
        Intent intent =getIntent();
        /*取出Intent中附加的数据*/
        String first = intent.getStringExtra("model");
        System.out.println(first);
        String sceond = intent.getStringExtra("activity");
        Toast.makeText(camera.this, first+" "+sceond, Toast.LENGTH_SHORT).show();
        System.out.println(getDate());

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        mBtnPlay = (Button) findViewById(R.id.btnPlayVideo);
        textView = (TextView)findViewById(R.id.text);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!mStartedFlg) {//开始录像
                    text=0;
                    System.out.println("开始录像");
                    Toast.makeText(camera.this, "再次点击停止拍摄", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(runnable,1000);
                    mImageView.setVisibility(View.GONE);
                    //mRecorder = new MediaRecorder();
                    if (mRecorder == null) {
                        mRecorder = new MediaRecorder();
                    }

                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (camera != null) {

//                        Camera.Parameters parameters = camera.getParameters();
//                        Camera.Size preSize = getCloselyPreSize(true, mSurfaceview.getWidth(), mSurfaceview.getHeight(), parameters.getSupportedPreviewSizes());
//                        parameters.setPreviewSize(preSize.width, preSize.height);
//
//                        camera.setParameters(parameters);


                        camera.setDisplayOrientation(90);
                        camera.unlock();
                        mRecorder.setCamera(camera);
                    }
                    System.out.println("0");
                    try {

                        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                        // 这两项需要放在setOutputFormat之前
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);


                        //mRecorder.setVideoEncodingBitRate(9 * 1024 * 1024);
                        System.out.println("1");
                        // Set output file format

                        //相机参数配置类
                        //直接采用QUALITY_HIGH,这样可以提高视频的录制质量，但是不能设置编码格式和帧率等参数。
                        CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                        mRecorder.setProfile(cProfile);


                        // 这两项需要放在setOutputFormat之后
                        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        //mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);


//                       CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
//                       mRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
//                       mRecorder.setVideoFrameRate(1);




                        mRecorder.setOrientationHint(90);
                        //设置记录会话的最大持续时间（毫秒）
                        mRecorder.setMaxDuration(30 * 1000);


                        path = getSDPath();
                        if (path != null) {
                            File dir = new File(path + "/AI-coach/video");
                            if (!dir.exists()) {
                                dir.mkdir();
                            }
                            filename=getDate();
                            path = dir + "/" + filename + ".avi";
                            Log.d(TAG, "地址为："+path);
                            System.out.println("地址为："+path);
                            mRecorder.setOutputFile(path);
                            mRecorder.prepare();
                            mRecorder.start();
                            mStartedFlg = true;
                            //mBtnStartStop.setText("Stop");
                            System.out.println("2");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    if (mStartedFlg) {
                        try {
                            handler.removeCallbacks(runnable);
                            mRecorder.stop();
                            mRecorder.reset();
                            mRecorder.release();
                            mRecorder = null;
                            //mBtnStartStop.setText("Start");
                            if (camera != null) {
                                camera.release();
                                camera = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mStartedFlg = false;
                }
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {//播放按钮
            @Override
            public void onClick(View view) {
                if(!mStartedFlg){
                    mIsPlay = true;
                    mImageView.setVisibility(View.GONE);
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    mediaPlayer.reset();
                    Uri uri = Uri.parse(path);
                    mediaPlayer = MediaPlayer.create(camera.this, uri);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDisplay(mSurfaceHolder);
                    try{
                        mediaPlayer.prepare();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    mediaPlayer.start();
                }
                else{
                    Toast.makeText(camera.this, "请先停止拍摄", Toast.LENGTH_SHORT).show();
                }


            }
        });

        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取用户权限
     */
    private void permissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO};

            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(camera.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }

            if (mPermissionList.isEmpty()) {// 全部允许
                System.out.println("全部允许");
            } else {//存在未允许的权限
                String[] mPermissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(camera.this, mPermissions, 1001);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//回调检查权限
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                for (int i = 0; i < grantResults.length; i++) {
//                   如果拒绝获取权限
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean flag= ActivityCompat.shouldShowRequestPermissionRationale(camera.this, permissions[i]);
                        if (flag) {
                            permissionRequest();
                            return;//用户权限是一个一个的请求的，只要有拒绝，剩下的请求就可以停止，再次请求打开权限了
                        } else { // 勾选不再询问，并拒绝
                            //ToastUtils.showToast(SplashActivity.this, "Please go to Settings to get user permissions"));
                            return;
                        }
                    }
                }
                //toMain(); //执行下一步操作
                break;
            default:
                break;
        }
    }




    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        String day1,month1,minute1,hour1,second1;
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒
        if(day<10)
            day1="0"+day;
        else
            day1=""+day;
        if(month+1<10)
            month1="0"+(month+1);
        else
            month1=""+(month+1);
        if(minute<10)
            minute1="0"+minute;
        else
            minute1=""+minute;
        if(hour<10)
            hour1="0"+hour;
        else
            hour1=""+hour;
        if(second<10)
            second1="0"+second;
        else
            second1=""+second;


        String date = "" + year + month1+ day1 + hour1 + minute1 + second1;
        Log.d(TAG, "date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        //camera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceview = null;
        mSurfaceHolder = null;
        handler.removeCallbacks(runnable);
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
            Log.d(TAG, "surfaceDestroyed release mRecorder");
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 获得和当前Android蓝牙已经配对的蓝牙设备。
     *
     * @return
     */
    private BluetoothDevice getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // 把已经取得配对的蓝牙设备名字和地址打印出来。
                Log.d(TAG, device.getName() + " : " + device.getAddress());

                //如果已经发现目标蓝牙设备和Android蓝牙已经配对，则直接返回。
                if (TextUtils.equals(TARGET_DEVICE_NAME, device.getName())) {
                    Log.d(TAG, "已配对目标设备 -> " + TARGET_DEVICE_NAME);
                    System.out.println("已配对目标设备"+ TARGET_DEVICE_NAME);
                    return device;
                }
            }
        }

        return null;
    }

    public void upload(View v){//姿态纠正上传按钮
        System.out.println("点击上传按钮");
        new Thread(new ClientThread()).start();
    }

    /**
     * 保存内容到TXT文件中
     *
     * @param content
     * @return
     */
    public static boolean writeToTXT(String content) {
        FileOutputStream fileOutputStream;
        BufferedWriter bufferedWriter;
        File file = new File("/storage/emulated/0/AI-coach/choose.txt");
        try {
            fileOutputStream = new FileOutputStream(file);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(content);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void GetFile(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(camera.this, IM_lookActivity.class);
        //newIntent.putExtra("model", "model_1");//传递数据
        //newIntent.putExtra("com.examples.helicopter.name", "12345678");//传递数据x
        startActivity(newIntent);//启动Intent对象
    }

}