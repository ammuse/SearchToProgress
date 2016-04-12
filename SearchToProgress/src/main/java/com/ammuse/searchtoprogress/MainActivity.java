package com.ammuse.searchtoprogress;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView imgView;
    private IndeterminateSearchDrawable mSearchDrawable;

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.image_view);
        mSearchDrawable = IndeterminateSearchDrawable.newInstance(this);
        imgView.setImageDrawable(mSearchDrawable);

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRunning) {
                    mSearchDrawable.stop();
                    isRunning = false;
                }
                else {
                    mSearchDrawable.start();
                    isRunning = true;
                }
            }
        });
    }
}
