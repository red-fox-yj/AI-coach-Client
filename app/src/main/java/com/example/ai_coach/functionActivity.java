package com.example.ai_coach;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class functionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);
    }

        public void function01(View v){//动作评估
            Intent newIntent = new Intent();//新建一个Intent对象
            //选择当前Activity和下一个要运行的Activity
            newIntent.setClass(functionActivity.this, ChooseActivity.class);
            newIntent.putExtra("model", "model_0");//传递数据
            //newIntent.putExtra("com.examples.helicopter.name", "12345678");//传递数据x
            startActivity(newIntent);//启动Intent对象
    }

    public void function02(View v){//姿态纠正
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(functionActivity.this, ChooseActivity.class);
        newIntent.putExtra("model", "model_1");//传递数据
        //newIntent.putExtra("com.examples.helicopter.name", "12345678");//传递数据x
        startActivity(newIntent);//启动Intent对象
    }

    public void function03(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(functionActivity.this, IM_lookActivity.class);
        //newIntent.putExtra("model", "model_1");//传递数据
        //newIntent.putExtra("com.examples.helicopter.name", "12345678");//传递数据x
        startActivity(newIntent);//启动Intent对象
    }

    public void function04(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(functionActivity.this, instructionActivity.class);
        startActivity(newIntent);//启动Intent对象
    }
}






