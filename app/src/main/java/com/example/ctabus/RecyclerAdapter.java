package com.example.ctabus;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
    private RecyclerInterface myObj;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public MyViewHolder(View v) {
            super(v);
            view = v.findViewById(myObj.returnViewId());
        }

        public void setTextToView(int position) {
            myObj.setText(position, view);
        }
    }

    public RecyclerAdapter(RecyclerInterface obj) {
        this.myObj = obj;
    }

    @NonNull
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(myObj.returnView(parent));
    }

    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setTextToView(position);
    }

    public int getItemCount() {
        return myObj.getDataSize();
    }

}
