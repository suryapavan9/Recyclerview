package com.example.android.recyclerview;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    ArrayList<Hotel> hotels;    //source
    RecyclerView recyclerView;  //destination
    MyRecyclerAdapter myRecyclerAdapter;  //adapter
    MyTask myTask;  //Async task

    public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder>{

        //create inner class here
        public class MyViewHolder extends RecyclerView.ViewHolder{

            // it represents the cardview
            public ImageView hotelImage, overflowImage;
            public TextView hotelName, hotelAddress, hotelDishes;

            public MyViewHolder(View itemView) {
                super(itemView);

                hotelImage = (ImageView) itemView.findViewById(R.id.imageview);
                overflowImage = (ImageView) itemView.findViewById(R.id.imageview1);
                hotelName = (TextView) itemView.findViewById(R.id.textview1);
                hotelAddress = (TextView) itemView.findViewById(R.id.textview2);
                hotelDishes = (TextView) itemView.findViewById(R.id.textview3);
            }
        }
        @Override
        public MyRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = getLayoutInflater().inflate(R.layout.row, parent, false);
            MyViewHolder myViewHolder = new MyViewHolder(v);
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyRecyclerAdapter.MyViewHolder holder, int position) {

            //get hotel object based on position from arraylist
            Hotel hotel = hotels.get(position);

            //apply data-bind data onto holder
            holder.hotelName.setText(hotel.getName());
            holder.hotelAddress.setText(hotel.getAddress()+ "\n" +hotel.getCity());
            holder.hotelDishes.setText(hotel.getCuisines());

            //set overflow menu

            holder.overflowImage.setImageResource(R.drawable.download);

            //  later
            //  ask glider library to load hotel thumb nail image onto imageview
            Glide.with(HomeActivity.this).load(hotel.getImageUrl()).into(holder.hotelImage);
        }

        @Override
        public int getItemCount() {
            return hotels.size();
        }
    }
    public class MyTask extends AsyncTask<String, Void, String>{

        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... p1) {

            try {
                myurl = new URL(p1[0]);
                connection = (HttpURLConnection) myurl.openConnection();

                connection.setRequestProperty("Accept","application/json");
                connection.setRequestProperty("user-key","809e707341a062e416bc58a3b3be39ff");
                connection.connect();

                inputStream = connection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                result = new StringBuilder();
                line = bufferedReader.readLine();
                while (line!= null)
                {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("MyLog","Message.."+e.getMessage());
                Log.d("MyLog","Cause.."+e.getCause());
                e.printStackTrace();
            }
            finally {
                if (connection!= null)
                {
                    connection.disconnect();
                    if (inputStream!= null)
                    {
                        try {
                            inputStream.close();
                            if (inputStreamReader!= null)
                            {
                                inputStreamReader.close();
                                if (bufferedReader!= null)
                                {
                                    bufferedReader.close();
                                }
                            }
                        } catch (IOException e) {
                            Log.d("MyLog","Problem is closing connection");
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (s == null){
                Toast.makeText(HomeActivity.this, "NETWORK ISSUE PLEASE FIX IT", Toast.LENGTH_SHORT).show();

                return;
            }

            //we will do parsing here

            try {
                JSONObject j = new JSONObject(s);
                JSONArray restaurants = j.getJSONArray("nearby_restaurants");
                for (int i = 0;i<restaurants.length();i++){
                    JSONObject temp = restaurants.getJSONObject(i);
                    JSONObject res = temp.getJSONObject("restaurant");
                    String name = res.getString("name");
                    JSONObject location = res.getJSONObject("location");
                    String address = location.getString("address");
                    String locality = location.getString("locality");
                    String city = location.getString("city");
                    String latitude = location.getString("latitude");
                    String longitude = location.getString("longitude");
                    String cuisines = res.getString("cuisines");    // famous dishes
                    String imageUrl = res.getString("thumb");   //icon for that string

                    //prepare empty hotel object - pass the values to constructor
                    Hotel hotel = new Hotel(name,address,locality,city,latitude,longitude,cuisines,imageUrl);

                    //push this hotel object to arraylist
                    hotels.add(hotel);
                }
                //tell to adapter
                myRecyclerAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            super.onPostExecute(s);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //initialize all variables
        hotels = new ArrayList<Hotel>();
        recyclerView = (RecyclerView) findViewById(R.id.recylerview1);

        //improve recyclerview performance
        recyclerView.setHasFixedSize(true); //it will double the calculation speed

        myTask = new MyTask();
        myRecyclerAdapter = new MyRecyclerAdapter();

        //create grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);    //for 2 cards per row

        //pass grid layout manager to recycler view
        recyclerView.setLayoutManager(gridLayoutManager);

        //set adapter to recycler view
        recyclerView.setAdapter(myRecyclerAdapter);

        //start async task pass zomato url
        myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=12.8984&lon=77.6179");
    }
}
