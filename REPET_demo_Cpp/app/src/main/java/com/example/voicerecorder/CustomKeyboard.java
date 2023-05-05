package com.example.voicerecorder;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class CustomKeyboard extends LinearLayout implements View.OnClickListener {
    private InputConnection inputConnection;
    private HashMap<Integer, String> hashMap;
    private LinearLayout linear_layout_0;
    private LinearLayout linear_layout_1;
    private LinearLayout linear_layout_2;
    private LinearLayout linear_layout_3;
    private LinearLayout linear_layout_4;
    private LinearLayout linear_layout_5;
    private LinearLayout linear_layout_6;
    private LinearLayout linear_layout_7;
    private LinearLayout linear_layout_8;
    private LinearLayout linear_layout_9;
    private LinearLayout linear_layout_q;
    private LinearLayout linear_layout_w;
    private LinearLayout linear_layout_e;
    private LinearLayout linear_layout_r;
    private LinearLayout linear_layout_t;
    private LinearLayout linear_layout_y;
    private LinearLayout linear_layout_u;
    private LinearLayout linear_layout_i;
    private LinearLayout linear_layout_o;
    private LinearLayout linear_layout_p;
    private LinearLayout linear_layout_a;
    private LinearLayout linear_layout_s;
    private LinearLayout linear_layout_d;
    private LinearLayout linear_layout_f;
    private LinearLayout linear_layout_g;
    private LinearLayout linear_layout_h;
    private LinearLayout linear_layout_j;
    private LinearLayout linear_layout_k;
    private LinearLayout linear_layout_l;
    private LinearLayout linear_layout_;
    private LinearLayout linear_layout_z;
    private LinearLayout linear_layout_x;
    private LinearLayout linear_layout_c;
    private LinearLayout linear_layout_v;
    private LinearLayout linear_layout_b;
    private LinearLayout linear_layout_n;
    private LinearLayout linear_layout_m;
    private LinearLayout linear_layout_delete;
    private LinearLayout linear_layout_backspace;

    public CustomKeyboard(Context context) {
        super(context);
    }

    public CustomKeyboard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomKeyboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater.from(context).inflate(R.layout.custom_keyboard_layout, this, true);
        hashMap = new HashMap<>();
        linearLayoutFindViewById();
        linearLayoutSetOnClickListener();
        populateHashMap();
    }

    @Override
    public void onClick(View v) {
        if (inputConnection != null) {
            inputConnection.commitText(hashMap.get(v.getId()), 1);
        }
    }

    public void setInputConnection(InputConnection inputConnection) {
        this.inputConnection = inputConnection;
    }

    private void linearLayoutFindViewById() {
        linear_layout_0 = findViewById(R.id.linear_layout_0);
        linear_layout_1 = findViewById(R.id.linear_layout_1);
        linear_layout_2 = findViewById(R.id.linear_layout_2);
        linear_layout_3 = findViewById(R.id.linear_layout_3);
        linear_layout_4 = findViewById(R.id.linear_layout_4);
        linear_layout_5 = findViewById(R.id.linear_layout_5);
        linear_layout_6 = findViewById(R.id.linear_layout_6);
        linear_layout_7 = findViewById(R.id.linear_layout_7);
        linear_layout_8 = findViewById(R.id.linear_layout_8);
        linear_layout_9 = findViewById(R.id.linear_layout_9);
        linear_layout_q = findViewById(R.id.linear_layout_q);
        linear_layout_w = findViewById(R.id.linear_layout_w);
        linear_layout_e = findViewById(R.id.linear_layout_e);
        linear_layout_r = findViewById(R.id.linear_layout_r);
        linear_layout_t = findViewById(R.id.linear_layout_t);
        linear_layout_y = findViewById(R.id.linear_layout_y);
        linear_layout_u = findViewById(R.id.linear_layout_u);
        linear_layout_i = findViewById(R.id.linear_layout_i);
        linear_layout_o = findViewById(R.id.linear_layout_o);
        linear_layout_p = findViewById(R.id.linear_layout_p);
        linear_layout_a = findViewById(R.id.linear_layout_a);
        linear_layout_s = findViewById(R.id.linear_layout_s);
        linear_layout_d = findViewById(R.id.linear_layout_d);
        linear_layout_f = findViewById(R.id.linear_layout_f);
        linear_layout_g = findViewById(R.id.linear_layout_g);
        linear_layout_h = findViewById(R.id.linear_layout_h);
        linear_layout_j = findViewById(R.id.linear_layout_j);
        linear_layout_k = findViewById(R.id.linear_layout_k);
        linear_layout_l = findViewById(R.id.linear_layout_l);
        linear_layout_ = findViewById(R.id.linear_layout_);
        linear_layout_z = findViewById(R.id.linear_layout_z);
        linear_layout_x = findViewById(R.id.linear_layout_x);
        linear_layout_c = findViewById(R.id.linear_layout_c);
        linear_layout_v = findViewById(R.id.linear_layout_v);
        linear_layout_b = findViewById(R.id.linear_layout_b);
        linear_layout_n = findViewById(R.id.linear_layout_n);
        linear_layout_m = findViewById(R.id.linear_layout_m);
        linear_layout_delete = findViewById(R.id.linear_layout_delete);
        linear_layout_backspace = findViewById(R.id.linear_layout_backspace);
    }

    private void linearLayoutSetOnClickListener() {
        linear_layout_0.setOnClickListener(this);
        linear_layout_1.setOnClickListener(this);
        linear_layout_2.setOnClickListener(this);
        linear_layout_3.setOnClickListener(this);
        linear_layout_4.setOnClickListener(this);
        linear_layout_5.setOnClickListener(this);
        linear_layout_6.setOnClickListener(this);
        linear_layout_7.setOnClickListener(this);
        linear_layout_8.setOnClickListener(this);
        linear_layout_9.setOnClickListener(this);
        linear_layout_q.setOnClickListener(this);
        linear_layout_w.setOnClickListener(this);
        linear_layout_e.setOnClickListener(this);
        linear_layout_r.setOnClickListener(this);
        linear_layout_t.setOnClickListener(this);
        linear_layout_y.setOnClickListener(this);
        linear_layout_u.setOnClickListener(this);
        linear_layout_i.setOnClickListener(this);
        linear_layout_o.setOnClickListener(this);
        linear_layout_p.setOnClickListener(this);
        linear_layout_a.setOnClickListener(this);
        linear_layout_s.setOnClickListener(this);
        linear_layout_d.setOnClickListener(this);
        linear_layout_f.setOnClickListener(this);
        linear_layout_g.setOnClickListener(this);
        linear_layout_h.setOnClickListener(this);
        linear_layout_j.setOnClickListener(this);
        linear_layout_k.setOnClickListener(this);
        linear_layout_l.setOnClickListener(this);
        linear_layout_.setOnClickListener(this);
        linear_layout_z.setOnClickListener(this);
        linear_layout_x.setOnClickListener(this);
        linear_layout_c.setOnClickListener(this);
        linear_layout_v.setOnClickListener(this);
        linear_layout_b.setOnClickListener(this);
        linear_layout_n.setOnClickListener(this);
        linear_layout_m.setOnClickListener(this);
        linearLayoutDeleteSetOnClickListener();
        linearLayoutBackspaceSetOnClickListener();
    }

    private void linearLayoutBackspaceSetOnClickListener() {
        linear_layout_backspace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence = inputConnection.getSelectedText(0);
                if (TextUtils.isEmpty(charSequence)) {
                    inputConnection.deleteSurroundingText(1, 0);
                } else {
                    inputConnection.commitText("", 1);
                }
            }
        });
    }

    private void linearLayoutDeleteSetOnClickListener() {
        linear_layout_delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence charSequence = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
                CharSequence textBeforeCursor = inputConnection.getTextBeforeCursor(charSequence.length(), 0);
                CharSequence textAfterCursor = inputConnection.getTextAfterCursor(charSequence.length(), 0);
                inputConnection.deleteSurroundingText(textBeforeCursor.length(), textAfterCursor.length());
            }
        });
    }

    private void populateHashMap() {
        hashMap.put(R.id.linear_layout_0, getResources().getString(R.string._0));
        hashMap.put(R.id.linear_layout_1, getResources().getString(R.string._1));
        hashMap.put(R.id.linear_layout_2, getResources().getString(R.string._2));
        hashMap.put(R.id.linear_layout_3, getResources().getString(R.string._3));
        hashMap.put(R.id.linear_layout_4, getResources().getString(R.string._4));
        hashMap.put(R.id.linear_layout_5, getResources().getString(R.string._5));
        hashMap.put(R.id.linear_layout_6, getResources().getString(R.string._6));
        hashMap.put(R.id.linear_layout_7, getResources().getString(R.string._7));
        hashMap.put(R.id.linear_layout_8, getResources().getString(R.string._8));
        hashMap.put(R.id.linear_layout_9, getResources().getString(R.string._9));
        hashMap.put(R.id.linear_layout_q, getResources().getString(R.string.q));
        hashMap.put(R.id.linear_layout_w, getResources().getString(R.string.w));
        hashMap.put(R.id.linear_layout_e, getResources().getString(R.string.e));
        hashMap.put(R.id.linear_layout_r, getResources().getString(R.string.r));
        hashMap.put(R.id.linear_layout_t, getResources().getString(R.string.t));
        hashMap.put(R.id.linear_layout_y, getResources().getString(R.string.y));
        hashMap.put(R.id.linear_layout_u, getResources().getString(R.string.u));
        hashMap.put(R.id.linear_layout_i, getResources().getString(R.string.i));
        hashMap.put(R.id.linear_layout_o, getResources().getString(R.string.o));
        hashMap.put(R.id.linear_layout_p, getResources().getString(R.string.p));
        hashMap.put(R.id.linear_layout_a, getResources().getString(R.string.a));
        hashMap.put(R.id.linear_layout_s, getResources().getString(R.string.s));
        hashMap.put(R.id.linear_layout_d, getResources().getString(R.string.d));
        hashMap.put(R.id.linear_layout_f, getResources().getString(R.string.f));
        hashMap.put(R.id.linear_layout_g, getResources().getString(R.string.g));
        hashMap.put(R.id.linear_layout_h, getResources().getString(R.string.h));
        hashMap.put(R.id.linear_layout_j, getResources().getString(R.string.j));
        hashMap.put(R.id.linear_layout_k, getResources().getString(R.string.k));
        hashMap.put(R.id.linear_layout_l, getResources().getString(R.string.l));
        hashMap.put(R.id.linear_layout_, getResources().getString(R.string.__));
        hashMap.put(R.id.linear_layout_z, getResources().getString(R.string.z));
        hashMap.put(R.id.linear_layout_x, getResources().getString(R.string.x));
        hashMap.put(R.id.linear_layout_c, getResources().getString(R.string.c));
        hashMap.put(R.id.linear_layout_v, getResources().getString(R.string.v));
        hashMap.put(R.id.linear_layout_b, getResources().getString(R.string.b));
        hashMap.put(R.id.linear_layout_n, getResources().getString(R.string.n));
        hashMap.put(R.id.linear_layout_m, getResources().getString(R.string.m));
    }
}
