package com.example.ctabus;

import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRrshContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMyActionBar();

        requestQueue = Volley.newRequestQueue(this);

        // Set RecyclerView
        recyclerView = findViewById(R.id.routesRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set SwipeRefreshLayout
        swipeRrshContainer = findViewById(R.id.swipeRefreshContainer);
        swipeRrshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getJSON();

                Snackbar.make(findViewById(R.id.coordLayout),"Updating bus routes", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        swipeRrshContainer.setColorSchemeColors(getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorOrange),
                getResources().getColor(R.color.colorAccent));

        getJSON();
    }

    public void setMyActionBar() {
        Toolbar toolbar = findViewById(R.id.mToolBar);
        setSupportActionBar(toolbar);
    }

    public void getJSON() {
        final String url = "http://www.ctabustracker.com/bustime/api/v2/getroutes?key={}&format=json";

        JsonObjectRequest objRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response)
                    {
                        Log.d("testing", "getRoute Response: " + response.toString());
                        parseJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError e)
                    {
                        Log.d("getJson", "Failed Msg: " + e.toString());

                        // Stop swipe to refresh animation
                        swipeRrshContainer.setRefreshing(false);

                        // Remove RecyclerView Items
                        createAdapter(new ArrayList<String[]>(0));

                        // Show getJSON error
                        Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG)
                            .show();
                    }
                }
        );
        requestQueue.add(objRequest);
    }

    public void parseJSON(JSONObject response) {
        ArrayList<String[]> routes = new ArrayList<>();
        try {
            JSONObject jsonObj = response.getJSONObject("bustime-response");
            JSONArray jsonArr = jsonObj.getJSONArray("routes");

            for (int i = 0; i < jsonArr.length(); ++i) {
                String[] result = new String[2];
                result[0] = jsonArr.getJSONObject(i).getString("rt");
                result[1] = jsonArr.getJSONObject(i).getString("rtnm");
                routes.add(result);
            }
                createAdapter(routes);
                swipeRrshContainer.setRefreshing(false);
        } catch (Exception e) {
            Log.d("parseJSON", "Exception: " + e.toString());

            try {
                JSONObject jsonObj = response.getJSONObject("bustime-response");
                JSONArray jsonArr = jsonObj.getJSONArray("error");
                String msg = jsonArr.getJSONObject(0).getString("msg");

                // Remove RecyclerView Items
                createAdapter(new ArrayList<String[]>(0));

                Toast alert = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG);
                alert.show();
            } catch (Exception error) {
                Log.d("testing", "Parse error in Routes Error" + error.toString());
            }
        }
    }

    public void createAdapter(ArrayList<String[]> dataSet) {
        final FragmentManager fm = getSupportFragmentManager();
        final ArrayList<String[]> routes = dataSet;

        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(new RecyclerInterface() {
            @Override
            public int returnViewId() {
                return R.id.recyclerButton;
            }

            @Override
            public View returnView(ViewGroup parent) {
                Button button = (Button) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recyclerlist_button, parent, false);
                return button;
            }

            @Override
            public void setText(int position, View view) {
                Button button = (Button) view;
                button.setText(routes.get(position)[0] + " " + routes.get(position)[1]);
                final FragmentManager fragManager = fm;
                final String rt = routes.get(position)[0];

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DirectionFragment directionFragment = new DirectionFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("rt", rt);
                        directionFragment.setArguments(bundle);
                        directionFragment.show(fragManager, "Showing Dialog Fragment");
                    }
                });
            }

            public int getDataSize() {
                return routes.size();
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
    }

}
