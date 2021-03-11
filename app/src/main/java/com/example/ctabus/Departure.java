package com.example.ctabus;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
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
import java.lang.Character;

public class Departure extends AppCompatActivity {
    private String route;
    private String stopID;
    private String stopName;
    private String direction;

    private RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRrshContainer;
    private Boolean showAllBus;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departure);

        // Get the necessary data from previous activity
        stopID = getIntent().getStringExtra("stopID");
        route = getIntent().getStringExtra("rt");
        stopName = getIntent().getStringExtra("stopName");
        direction = getIntent().getStringExtra("direction");
//                        Log.d("testing", "Departure StopID: " + stopID);        //DEBUG
//                        Log.d("testing", "Departure route: " + route);          //DEBUG
//                        Log.d("testing","Departure stopName" + stopName);       //DEBUG
//                        Log.d("testing","Departure direction" + direction);     //DEBUG
        setMyActionBar();

        // Transparent Status Bar; Flags allow Window to extend out of screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // Initialize variables
        showAllBus = false;
        requestQueue = Volley.newRequestQueue(this);

        // Set RecyclerView
        recyclerView = findViewById(R.id.departureRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Set SwipeRefreshLayout
        swipeRrshContainer = findViewById(R.id.swipeRefreshContainer);
        swipeRrshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (showAllBus) {
                    getDepartureJSON("");
                } else {
                    getDepartureJSON(route);
                }
                Snackbar.make(findViewById(R.id.coordLayout), "Updating arrival times", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
        swipeRrshContainer.setColorSchemeColors(getResources().getColor(R.color.colorOrange),
                getResources().getColor(R.color.colorPrimary));

        // Set Switch
        SwitchCompat switchRoutes = findViewById(R.id.switchRoutes);
        switchRoutes.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If switch is on
                    showAllBus = true;
                    getDepartureJSON("");

                    Snackbar.make(findViewById(R.id.coordLayout), "Showing all buses for this stop", Snackbar.LENGTH_LONG)
                        .show();
                }
                else {
                    // If switch is off
                    showAllBus = false;
                    getDepartureJSON(route);

                    Snackbar.make(findViewById(R.id.coordLayout), "Showing selected route for this stop", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });

        getDepartureJSON(route);
    }

    public void setMyActionBar() {
        Toolbar toolbar = findViewById(R.id.mToolBar);
        setSupportActionBar(toolbar);

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Set TextView
        TextView expandedTitle = findViewById(R.id.expandedTitle);
        expandedTitle.setText(route + " " + direction.charAt(0));
        TextView expandedSub = findViewById(R.id.expandedTextView);
        expandedSub.setText(stopName);
    }

    public void getDepartureJSON(String rt) {
        final String url = "http://www.ctabustracker.com/bustime/api/v2/getpredictions?key={}&format=json&rt=" + rt + "&stpid=" + stopID;
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("testing", "Get Departure Response: " + response.toString());

                        parseDepartureJSON(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("testing", "Get Departure Error" + error.toString());

                        // Stop swipe to refresh animation
                        swipeRrshContainer.setRefreshing(false);

                        // Remove RecyclerView Items
                        createAdapter(new ArrayList<String[]>(0));

                        Toast alert = Toast.makeText(Departure.this, "Network Error", Toast.LENGTH_LONG);
                        alert.show();
                    }
                });
        requestQueue.add(objectRequest);
    }

    public void parseDepartureJSON(JSONObject response) {
        ArrayList<String[]> prediction = new ArrayList<>();
        try {
            JSONObject jsonObj = response.getJSONObject("bustime-response");
            JSONArray jsonArr = jsonObj.getJSONArray("prd");

            for (int i = 0; i < jsonArr.length(); ++i) {
                String[] result = new String[4];
                result[0] = jsonArr.getJSONObject(i).getString("rt");
                result[1] = jsonArr.getJSONObject(i).getString("rtdir");
                result[2] = jsonArr.getJSONObject(i).getString("des");
                result[3] = jsonArr.getJSONObject(i).getString("prdctdn");
                prediction.add(result);
            }

            // Stop swipe to refresh animation & create adapter with data
            swipeRrshContainer.setRefreshing(false);
            createAdapter(prediction);
        } catch (Exception e) {
            Log.d("testing", e.toString());
            try {
                JSONObject jsonObj = response.getJSONObject("bustime-response");
                JSONArray jsonArr = jsonObj.getJSONArray("error");
                String msg = jsonArr.getJSONObject(0).getString("msg");

                // Remove RecyclerView Items
                createAdapter(new ArrayList<String[]>(0));

                // Display Error Message
                Snackbar.make(findViewById(R.id.coordLayout), msg, Snackbar.LENGTH_LONG)
                        .show();

            } catch (Exception error) {
                Log.d("testing", error.toString());
            }
        }
    }

    public void createAdapter(ArrayList<String[]> dataSet) {
        final ArrayList<String[]> predictions = dataSet;
        RecyclerView.Adapter recyclerAdapter = new RecyclerAdapter(new RecyclerInterface() {
            @Override
            public int returnViewId() {
                return R.id.departConstraint;
            }

            @Override
            public View returnView(ViewGroup parent) {
                ConstraintLayout ln = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerlist_layout, parent, false);
                ;
                return ln;
            }

            @Override
            public void setText(int position, View view) {
                view.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bd_textview_main, null));
                TextView rtTV = view.findViewById(R.id.rtTextView);
                rtTV.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bd_textview_routes, null));
                rtTV.setText(predictions.get(position)[0] + " " + predictions.get(position)[1].charAt(0));

                TextView destTV = view.findViewById(R.id.destTextView);
                destTV.setText(predictions.get(position)[2]);

                TextView timeTV = view.findViewById(R.id.timeTextView);
                String time = predictions.get(position)[3];
                String toSet = "";
                if (!Character.isLetter(time.charAt(0))) {
                    if (time.length() < 2 && Character.getNumericValue(time.toCharArray()[0]) == 1) {
                        toSet += time + " min";
                    } else {
                        toSet += time + " mins";
                    }
                } else if (time.matches("DLY")) {
                    toSet += "Delayed";
                } else {
                    toSet += time;
                }
                timeTV.setText(toSet);
            }

            @Override
            public int getDataSize() {
                return predictions.size();
            }
        });
        recyclerView.setAdapter(recyclerAdapter);
    }
}
