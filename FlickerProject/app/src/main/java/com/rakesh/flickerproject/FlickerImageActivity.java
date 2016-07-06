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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlickerImageActivity extends AppCompatActivity {
    private String FLICKER_API_KEY = "efe0217da16ff6c3c1c7fb0cefee1b01";
    private EditText tagEt;
    private Button submitBt;
    private Activity activity = FlickerImageActivity.this;
    private String SERVER_URL;
    private List<FlickerResponseModel> flickerResponseList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> viewsStatusList = new ArrayList<>();
    private String IMAGE_HIDDEN = "imageHidden";
    private String IMAGE_VISIBLE = "imageVisible";
    private MyGridAdapter photoAdapter = new MyGridAdapter();
    ;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flicker_image);

        getWidgets();

        clickListeners();

        setImageLoader();

    }

    private void setImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
    }

    private void clickListeners() {
        submitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.hideSoftKeyboard(activity);
                String tag = tagEt.getText().toString();
                if (!TextUtils.isEmpty(tag)) {
                    SERVER_URL = "https://api.flickr.com/services/rest/?method=flickr.tags.getClusterPhotos&api_key=" + FLICKER_API_KEY + "&tag=" + tag + "&format=json&nojsoncallback=1";
                    getImages();
                } else {
                    Toast.makeText(activity, "Please enter a tag", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getWidgets() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        tagEt = (EditText) findViewById(R.id.tag_et);
        submitBt = (Button) findViewById(R.id.submit_bt);
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
                        setFreshList();
                        jsonObject = new JSONObject(response);
                        JSONObject pSetJsonObj = jsonObject.getJSONObject("photos");
                        JSONArray pDataArray = pSetJsonObj.getJSONArray("photo");

                        Gson gson = new Gson();
                        flickerResponseList = gson.fromJson(pDataArray.toString(), new TypeToken<ArrayList<FlickerResponseModel>>() {
                        }.getType());
                        if (flickerResponseList.size() == 0) {
                            Toast.makeText(activity.getApplicationContext(), "No results found with this tag name", Toast.LENGTH_SHORT).show();

                        } else {
                            for (int i = 0; i < flickerResponseList.size(); i++) {
                                viewsStatusList.add(IMAGE_VISIBLE);
                            }
                            setGrid();
                        }
                    } else {
                        setFreshList();
                        Toast.makeText(activity.getApplicationContext(), "Something went wrong,Please try again.", Toast.LENGTH_SHORT).show();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            progressBar.cancel();
        }


    }

    private void setFreshList() {
        viewsStatusList.clear();
        flickerResponseList.clear();
        photoAdapter.notifyDataSetChanged();
    }

    private void setGrid() {
        layoutManager = new GridLayoutManager(activity, 2);
        recyclerView.setLayoutManager(layoutManager);


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
            ProgressBar imageSpinner;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
                dataLayout = (RelativeLayout) itemView.findViewById(R.id.data_layout);
                flipRl = (RelativeLayout) itemView.findViewById(R.id.flip_rl);
                imageSpinner = (ProgressBar) itemView.findViewById(R.id.image_spinner);
                imageTitleTv = (TextView) itemView.findViewById(R.id.image_title_tv);
                imageIdTv = (TextView) itemView.findViewById(R.id.image_id_tv);

            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //set image url
            String imageWebUrl = "https://farm" + flickerResponseList.get(position).getFarm() + ".staticflickr.com/" + flickerResponseList.get(position).getServer() + "/" + flickerResponseList.get(position).getPhotoId() + "_" + flickerResponseList.get(position).getSecret() + "_m.jpg";

            //check visibility
            if (viewsStatusList.get(position).equalsIgnoreCase(IMAGE_VISIBLE)) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.dataLayout.setVisibility(View.GONE);
            } else {
                holder.imageView.setVisibility(View.GONE);
                holder.dataLayout.setVisibility(View.VISIBLE);
            }


            //set image
            setImage(imageWebUrl, holder.imageView, holder.imageSpinner);

            holder.imageTitleTv.setText(flickerResponseList.get(position).getTitle());
            holder.imageIdTv.setText(flickerResponseList.get(position).getPhotoId());

            holder.flipRl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    setAnimation(holder.flipRl, holder.imageView, holder.dataLayout, position);


                }


                private void setAnimation(final View itemImageView, ImageView imageView, final RelativeLayout relativeLayout, final int position) {

                    FlipAnimation fAnimation = new FlipAnimation(imageView, relativeLayout);
                    if (IMAGE_HIDDEN.equalsIgnoreCase(viewsStatusList.get(position))) {

                        viewsStatusList.set(position, IMAGE_VISIBLE);

                        fAnimation.reverse();

                        delayVisibility(relativeLayout, View.GONE);
                    } else {

                        viewsStatusList.set(position, IMAGE_HIDDEN);

                        delayVisibility(relativeLayout, View.VISIBLE);


                    }
                    itemImageView.startAnimation(fAnimation);
                }

            });
        }

        private void delayVisibility(final RelativeLayout relativeLayout, final int visiblity) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    relativeLayout.setVisibility(visiblity);

                }
            }, 500);
        }

        @Override
        public int getItemCount() {
            return flickerResponseList.size();
        }
    }

    private void setImage(String imageWebUrl, final ImageView imageView, final ProgressBar imageSpinner) {
        DisplayImageOptions optionsImage = new DisplayImageOptions.Builder().cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisc(true).build();
        ImageLoader.getInstance().displayImage(imageWebUrl, imageView, optionsImage, new SimpleImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
                imageSpinner.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                imageSpinner.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageSpinner.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

            }
        });

    }
}
