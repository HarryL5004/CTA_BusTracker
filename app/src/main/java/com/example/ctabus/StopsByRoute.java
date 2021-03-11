package com.example.ctabus;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class StopsByRoute extends AppCompatActivity {
    private String route;
    private String direction;

    private RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRrshContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops);

        // Get the necessary data from previous fragment
        route = getIntent().getStringExtra("rt");
        direction = getIntent().getStringExtra("dir");

                //Log.d("testing", "Route value in StopsByRoute: " + route);
                //Log.d("testing", "Direction in StopsByRoute:" + direction);

        setMyActionBar();

        requestQueue = Volley.newRequestQueue(this);

        // Set RecyclerView
        recyclerView = findViewById(R.id.stopsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set SwipeRefreshLayout
        swipeRrshContainer = findViewById(R.id.swipeRefreshContainer);
        swipeRrshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStopsJSON();

                Snackbar.make(findViewById(R.id.coordLayout),"Updating selected route's bus stops", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        swipeRrshContainer.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorOrange),
                getResources().getColor(R.color.colorAccent));

        getStopsJSON();
    }

    public void setMyActionBar() {
        Toolbar toolbar = findViewById(R.id.mToolBar);
        setSupportActionBar(toolbar);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setTitle(route + " " + direction);
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void getStopsJSON() {
        final String url = "http://www.ctabustracker.com/bustime/api/v2/getstops?key={}&format=json&rt=" + route + "&dir=" + direction;
        JsonObjectRequest objRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response)
                    {
                        Log.d("testing", "Get Stops Response: " + response.toString());
                        parseStopsJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError e)
                    {
                        Log.d("testing", "Get Stops Error: " + e.toString());

                        // Stop swipe to refresh animation
                        swipeRrshContainer.setRefreshing(false);

                        // Remove RecyclerView Items
                        createAdapter(new ArrayList<String[]>(0));

                        Toast.makeText(StopsByRoute.this, "Network Error",Toast.LENGTH_LONG)
                            .show();
                    }
                }
        );
        requestQueue.add(objRequest);
    }

    public void parseStopsJSON(JSONObject response) {
        try {
            ArrayList<String[]> stops = new ArrayList<>();
            JSONObject jsonObj = response.getJSONObject("bustime-response");
            JSONArray jsonArray = jsonObj.getJSONArray("stops");

            for (int i = 0; i < jsonArray.length(); ++i) {
                String[] result = new String[3];
                result[0] = jsonArray.getJSONObject(i).getString("stpid");
                result[1] = jsonArray.getJSONObject(i).getString("stpnm");
                stops.add(result);
            }
            swipeRrshContainer.setRefreshing(false);
            createAdapter(stops);
        } catch(Exception e) {
            Log.d("testing", "Parse Stops Error: " + e.toString());

            try {
                JSONObject jsonObj = response.getJSONObject("bustime-response");
                JSONArray jsonArr = jsonObj.getJSONArray("error");
                String msg = jsonArr.getJSONObject(0).getString("msg");

                // Remove RecyclerView Items
                createAdapter(new ArrayList<String[]>(0));

                Toast alert = Toast.makeText(StopsByRoute.this, msg, Toast.LENGTH_LONG);
                alert.show();
            } catch (Exception error) {
                Log.d("testing", "Parse error in Stops Error: " + error.toString());
            }
        }
    }

    public void createAdapter(ArrayList<String[]> dataSet) {
        final ArrayList<String[]> stops = dataSet;

        RecyclerView.Adapter recyclerAdapter = new RecyclerAdapter(new RecyclerInterface() {
            @Override
            public int returnViewId() {
                return R.id.recyclerButton;
            }

            @Override
            public View returnView(ViewGroup parent) {
                Button button = (Button) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerlist_button, parent, false);
                return button;
            }

            @Override
            public void setText(int position, View view) {
                final String stopID = stops.get(position)[0];
                final String stopName = stops.get(position)[1];

                Button button = (Button) view;
                button.setText(stopName);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(StopsByRoute.this.getApplicationContext(), Departure.class);
                        intent.putExtra("stopID", stopID)
                                .putExtra("rt", route)
                                .putExtra("stopName",stopName)
                                .putExtra("direction", direction);
                        StopsByRoute.this.startActivity(intent);
                    }
                });
            }

            @Override
            public int getDataSize() {
                return stops.size();
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
    }

}
