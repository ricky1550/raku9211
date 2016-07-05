package com.rakesh.githubproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommitListActivity extends AppCompatActivity {
    private Activity activity = CommitListActivity.this;
    private String GITHUB_URL = "https://api.github.com/repos/rails/rails/commits";
    private ListView commitLv;
    private List<CommitListModel> commitListModelList = new ArrayList<>();
    private TextView authorNameTv, messageTv, commitTv, dateTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_list);
        commitLv = (ListView) findViewById(R.id.commit_lv);
        getCommitsFromRepo();
    }

    private void getCommitsFromRepo() {
        if (AppUtils.isOnline(activity)) {
            AsynchCommitFetcher asynchCommitFetcher = new AsynchCommitFetcher();
            asynchCommitFetcher.execute();
        } else {
            Toast.makeText(activity.getApplicationContext(), "Please check your internet connectivity.", Toast.LENGTH_SHORT).show();

        }
    }

    private class AsynchCommitFetcher extends AsyncTask<String, Long, String> {
        private ProgressDialog progressBar;
        String status;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressBar = new ProgressDialog(activity);
            progressBar.setCancelable(false);
            progressBar.setMessage("Fetching commits...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> result = null;


            result = AppUtils.connectToServer("", GITHUB_URL);
            status = result.get("responseCode");
            return result.get("result");
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (TextUtils.isEmpty(response)) {
                Toast.makeText(activity, getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();

            } else {
                try {
                    if ("200".equalsIgnoreCase(status)) {
                        JSONArray jsonArray = new JSONArray(response);
                        Gson gson = new Gson();
                        commitListModelList = gson.fromJson(jsonArray.toString(), new TypeToken<ArrayList<CommitListModel>>() {
                        }.getType());

                        setList();
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Something went wrong,Please try again.", Toast.LENGTH_SHORT).show();

                    }

                } catch (JSONException e) {
                    Toast.makeText(activity.getApplicationContext(), "Something went wrong,Please try again.", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }
            }
            progressBar.cancel();

        }

    }

    private void setList() {
        CommitListAdapter commitListAdapter = new CommitListAdapter();
        commitLv.setAdapter(commitListAdapter);

    }

    private class CommitListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return commitListModelList.size();
        }

        @Override
        public Object getItem(int position) {
            return commitListModelList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
            }
            authorNameTv = (TextView) itemView.findViewById(R.id.author_name_tv);
            commitTv = (TextView) itemView.findViewById(R.id.commit__tv);
            messageTv = (TextView) itemView.findViewById(R.id.message_tv);
            dateTv = (TextView) itemView.findViewById(R.id.date_tv);

            authorNameTv.setText(commitListModelList.get(position).getCommit().getAuthor().getName());
            String date = commitListModelList.get(position).getCommit().getAuthor().getDate();
            String[] dayAndTime = date.split("T");
            String[] time = dayAndTime[1].split("Z");
            dateTv.setText("( " + time[0] + " | " + dayAndTime[0] + " )");

            commitTv.setText(commitListModelList.get(position).getSha());
            messageTv.setText(commitListModelList.get(position).getCommit().getMessage());
            return itemView;
        }
    }
}
