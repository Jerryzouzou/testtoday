package com.hard.function;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hard.function.BezierLines.BezierBeginDerivationActivity;
import com.hard.function.BezierLines.BezierDIYActivity;
import com.hard.function.BezierLines.BezierRun2CircleActivity;
import com.hard.function.BezierLines.BezierRun2CircleView;
import com.hard.function.tool.UIUtils;
import com.hard.function.tool.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private int btnId[] = {R.id.btn_f_01, R.id.btn_f_02, R.id.btn_f_03};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        findView();
        Log.i("Jerry", "onCreate: ");
    }

    private void findView() {
        for (int i=0; i<btnId.length; i++){
            findViewById(btnId[i]).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_f_01:
                Utils.startActivity(mContext, BezierBeginDerivationActivity.class);
                break;
            case R.id.btn_f_02:
                Utils.startActivity(mContext, BezierRun2CircleActivity.class);
                break;
            case R.id.btn_f_03:
                Utils.startActivity(mContext, BezierDIYActivity.class);
                break;
        }
    }


}
