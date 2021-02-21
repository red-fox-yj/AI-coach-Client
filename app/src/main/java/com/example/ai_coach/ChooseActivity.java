package com.example.ai_coach;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseActivity extends AppCompatActivity {
    private Spinner spinner;
    private String activity;
    private String first;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        spinner = (Spinner) findViewById(R.id.spinner01);
        // 定义一个字符串数组来存储下拉框每个item要显示的文本
        final String[] items = { "动作", "引体向上", "下蹲", "俯卧撑", "卷腹" };
        // 定义数组适配器，利用系统布局文件
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);
        // 定义下拉框的样式
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // 下面的可以直接用适配器添加item(需要把数组适配器最后一个参数去掉)
        // adapter.add("java");
        // adapter.add("android");
        // adapter.add("dotnet");
        // adapter.add("php");

        //取得启动该Activity的Intent对象
         Intent intent =getIntent();
         /*取出Intent中附加的数据*/
         first = intent.getStringExtra("model");
         System.out.println(first);
         Toast.makeText(ChooseActivity.this, first, Toast.LENGTH_SHORT).show();




        // 设置下拉列表的条目被选择监听器
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                //Toast.makeText(ChooseActivity.this, items[arg2], 0).show();

                // 注意： 这句话的作用是当下拉列表刚显示出来的时候，数组中第0个文本不会显示Toast
                // 如果没有这句话，当下拉列表刚显示出来的时候，数组中第0个文本会显示Toast
                // arg0.setVisibility(View.VISIBLE);
                activity = (String) spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                Toast.makeText(ChooseActivity.this, "请选择动作", Toast.LENGTH_SHORT).show();
            }
        });

        spinner.setAdapter(adapter);
    }
    public void function03(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(ChooseActivity.this, camera.class);
        newIntent.putExtra("activity",activity);//传递动作数据
        newIntent.putExtra("model", first);//传递模式选择数据
        startActivity(newIntent);//启动Intent对象
    }
}