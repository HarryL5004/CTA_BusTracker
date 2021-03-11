package com.example.ctabus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectionFragment extends DialogFragment {
    private RequestQueue requestQueue;

    private String rt;
    private View view;
    private ArrayList<String> directions = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        if (getArguments() != null) { rt = getArguments().getString("rt"); }
        if (getContext() != null) { requestQueue = Volley.newRequestQueue(getContext()); }
                          //Log.d("testing", "RouteID in DirectionFragment: " + rt);          //DEBUG
        return inflater.inflate(R.layout.fragment_directions, viewGroup);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.view = view;
        getJSON(rt);
    }

    public void getJSON(String rtId) {
        final String url = "http://www.ctabustracker.com/bustime/api/v2/getdirections?key={}&format=json&rt=" + rtId;
        JsonObjectRequest objRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("testing", "Direction Response: " +response.toString());
                        parseJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("testing", "Get Direction Error: " + error.toString());
                    }
                }
        );
        requestQueue.add(objRequest);
    }

    public void parseJSON(JSONObject response) {
        try {
            JSONObject jsonObj = response.getJSONObject("bustime-response");
            JSONArray jsonArr = jsonObj.getJSONArray("directions");
            for(int i = 0; i < jsonArr.length(); ++i) {
                directions.add(jsonArr.getJSONObject(i).getString("dir"));
            }
            createButtons();
        } catch (Exception e) {
            Log.d("testing", "Parse Direction error" + e.toString());
        }
    }

    public void createButtons() {
        LinearLayout linearLayout = view.findViewById(R.id.fragmentLinearLayout);
        Button[] buttons = new Button[directions.size()];

        for (int i = 0; i < directions.size(); ++i) {
            final int counter = i;
            buttons[i] = new Button(getContext());
            buttons[i].setText(directions.get(i));
            buttons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), StopsByRoute.class);
                    intent.putExtra("rt", rt);
                    intent.putExtra("dir", directions.get(counter));
                    startActivity(intent);
                }
            });
            buttons[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(buttons[i]);
        }
    }
}
