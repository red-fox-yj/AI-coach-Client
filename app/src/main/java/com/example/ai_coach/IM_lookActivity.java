package com.example.ai_coach;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class IM_lookActivity extends AppCompatActivity implements View.OnClickListener{

    private Button pre,next;
    private ImageView imageView;
    private TextView imageName;
    private ArrayList<File> images;
    private int i = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_look);
        System.out.println("image_look");
        this.images = initData();    //获取本地图片集合
        System.out.println("image_look0000");
        init();
        System.out.println("image_look1111");
    }


    /**
     * 对布局文件进行初始化
     * */
    private void init(){
        pre = (Button) findViewById(R.id.pre);
        pre.setOnClickListener(this);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageview);
        imageName=(TextView)findViewById(R.id.name);
        System.out.println("image_look2222");
        showImage(0);
    }


    /**
     * 为按键添加监听事件
     * 实际上就是控制ArrayList集合中指针的数据来显示图片
     * 速度较慢，每次都需要重新读取。
     * */
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.pre:
                i --;
                if(i < 0) {
                    i = 0;
                    Toast.makeText(this, "已经是第一张了", Toast.LENGTH_SHORT).show();
                    break;
                }
                showImage(i);
                break;
            case R.id.next:
                i ++;
                if(i >= images.size()) {
                    i = images.size() - 1;
                    Toast.makeText(this, "已经是最后一张了", Toast.LENGTH_SHORT).show();
                    break;
                }
                showImage(i);
                break;
        }
    }

    private ArrayList<File> initData(){//按时间排序
        ArrayList<File> mList = new ArrayList<File>();
        String url = Environment.getExternalStorageDirectory().toString()+"/bluetooth";
        System.out.println(url);
        File albumdir = new File(url);
        File[] imgfile = albumdir.listFiles(filefiter);
        System.out.println(imgfile);
        int len = imgfile != null ? imgfile.length : 0;
        for(int i=0;i<len;i++){
            mList.add(imgfile[i]);
        }
        Collections.sort(mList, new FileComparator());
        System.out.println(mList.get(0).getName());
        return mList;
    }

    private FileFilter filefiter = new FileFilter(){

        @Override
        public boolean accept(File f) {
            String tmp = f.getName().toLowerCase();
            if(tmp.endsWith(".png")||tmp.endsWith(".jpg")
                    ||tmp.endsWith(".jpeg")){
                return true;
            }
            return false;
        }

    };

    private class FileComparator implements Comparator<File>{

        @Override
        public int compare(File lhs, File rhs) {
            if(lhs.lastModified()<rhs.lastModified()){
                return 1;//最后修改的照片在前
            }else{
                return -1;
            }
        }

    }

    /**
     * 通过文件获取流，将流转化为Bitmap对象
     * */
    private Bitmap getBMP(File file){
        BufferedInputStream in = null;
        Bitmap BMP = null;
        imageName.setText(file.getName());
        try{
            in = new BufferedInputStream(new FileInputStream(file));
            BMP = BitmapFactory.decodeStream(in);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "程序异常！", Toast.LENGTH_SHORT).show();
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return BMP;
    }

    /**
     * 设置文件的
     * */
    private void showImage(int i){
        imageView.setImageBitmap(getBMP(this.images.get(i)));
        System.out.println("image_look33333");
    }
}