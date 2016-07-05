package com.rakesh.flickerproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlickerImageActivity extends AppCompatActivity {
    private String FLICKER_API_KEY = "39329f5f765ae6ba3a513286d20f4706";
    private String PHOTOSET_ID = "72157670543955816";
    private String USER_ID = "144889814%40N04";
    private String AUTH_TOKEN = "72157670011283331-f929d0d9da5683e3";
    private String API_SIGN = "bab39ff10b45ec78e859db61ee632f72";
    private Activity activity = FlickerImageActivity.this;
    private String SERVER_URL = "https://api.flickr.com/services/rest/?method=flickr.photosets.getPhotos&api_key=" + FLICKER_API_KEY + "&photoset_id=" + PHOTOSET_ID + "&user_id=" + USER_ID + "&format=json&nojsoncallback=1&auth_token=" + AUTH_TOKEN + "&api_sig=" + API_SIGN;
    private List<FlickerResponseModel> flickerResponseList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flicker_image);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getImages();
    }

    private void getImages() {
        if (AppUtils.isOnline(activity)) {
            AsynchFlickerImageHandler asynchCommitFetcher = new AsynchFlickerImageHandler();
            asynchCommitFetcher.execute();
        } else {
            Toast.makeText(activity.getApplicationContext(), "Please check your internet connectivity.", Toast.LENGTH_SHORT).show();

        }
    }

    private class AsynchFlickerImageHandler extends AsyncTask<String, Long, String> {
        private ProgressDialog progressBar;
        private String responseCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar = new ProgressDialog(activity);
            progressBar.setCancelable(false);
            progressBar.setMessage("Fetching your images...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> response = new HashMap<>();


            response = AppUtils.connectToServer(SERVER_URL);
            responseCode = response.get("responseCode");
            return response.get("result");
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (TextUtils.isEmpty(response)) {
                Toast.makeText(activity, R.string.server_connection_error, Toast.LENGTH_SHORT).show();

            } else {

                JSONObject jsonObject = null;

                try {

                    if ("200".equalsIgnoreCase(responseCode)) {
                        jsonObject = new JSONObject(response);
                        JSONObject pSetJsonObj = jsonObject.getJSONObject("photoset");
                        JSONArray pDataArray = pSetJsonObj.getJSONArray("photo");

                        Gson gson = new Gson();
                        flickerResponseList = gson.fromJson(pDataArray.toString(), new TypeToken<ArrayList<FlickerResponseModel>>() {
                        }.getType());

                        setGrid();
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Something went wrong,Please try again.", Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            progressBar.cancel();
        }


    }

    private void setGrid() {
        layoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(layoutManager);
        MyGridAdapter photoAdapter;
        photoAdapter = new MyGridAdapter();
        recyclerView.setAdapter(photoAdapter);
    }


    private class MyGridAdapter extends RecyclerView.Adapter<MyGridAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item_layout, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            RelativeLayout dataLayout, flipRl;
            TextView imageTitleTv, imageIdTv;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
                dataLayout = (RelativeLayout) itemView.findViewById(R.id.data_layout);
                flipRl = (RelativeLayout) itemView.findViewById(R.id.flip_rl);

                imageTitleTv = (TextView) itemView.findViewById(R.id.image_title_tv);
                imageIdTv = (TextView) itemView.findViewById(R.id.image_id_tv);

            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            String imageWebUrl = "https://farm" + flickerResponseList.get(position).getFarm() + ".staticflickr.com/" + flickerResponseList.get(position).getServer() + "/" + flickerResponseList.get(position).getPhotoId() + "_" + flickerResponseList.get(position).getSecret() + "_m.jpg";

            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).writeDebugLogs().build();
            ImageLoader.getInstance().init(config);
            DisplayImageOptions optionsImage = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisc(true).build();
            ImageLoader.getInstance().displayImage(imageWebUrl, holder.imageView, optionsImage);

            holder.imageTitleTv.setText(flickerResponseList.get(position).getTitle());
            holder.imageIdTv.setText(flickerResponseList.get(position).getPhotoId());
            holder.dataLayout.setVisibility(View.GONE);

            holder.flipRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setAnimation(holder.flipRl, holder.imageView, holder.dataLayout);


                }


                private void setAnimation(final View itemImageView, ImageView imageView, final RelativeLayout relativeLayout) {
                    FlipAnimation fAnimation = new FlipAnimation(imageView, relativeLayout);

                    if (imageView.getVisibility() == View.GONE) {
                        fAnimation.reverse();


                        relativeLayout.setVisibility(View.GONE);
                    } else {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                relativeLayout.setVisibility(View.VISIBLE);

                            }
                        }, 500);

                    }
                    itemImageView.startAnimation(fAnimation);
                }

            });
        }

        @Override
        public int getItemCount() {
            return flickerResponseList.size();
        }
    }
}
