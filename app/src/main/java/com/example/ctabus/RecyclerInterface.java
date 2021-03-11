package com.example.ctabus;

import android.view.View;
import android.view.ViewGroup;


public interface RecyclerInterface {
    int returnViewId();
    View returnView(ViewGroup parent);
    void setText(int position, View view);
    int getDataSize();
}
