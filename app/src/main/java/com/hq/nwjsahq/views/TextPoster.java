package com.hq.nwjsahq.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hq.nwjsahq.R;

public class TextPoster extends RelativeLayout {


    public TextPoster(Context context) {
        super(context);
    }

    public TextPoster(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setOnSendListener(OnClickListener listener)
    {
        View button = findViewById(R.id.button);
        button.setOnClickListener(listener);
    }

    public String getText()
    {
        TextView tv = findViewById(R.id.editText);



        return tv.getText().toString();
    }

    public void clearText()
    {
        TextView tv = findViewById(R.id.editText);
        tv.setText("");
    }


}