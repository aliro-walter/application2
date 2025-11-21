package com.walter.myinternshiplogbook;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;

public class NewsActivity extends AppCompatActivity {

    private ListView newsListView;
    private ArrayAdapter<String> newsAdapter;
    private ArrayList<String> newsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        newsListView = findViewById(R.id.newsListView);
        newsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newsList);
        newsListView.setAdapter(newsAdapter);

        new FetchNewsTask().execute();
    }

    private class FetchNewsTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> headlines = new ArrayList<>();
            try {
                Document doc = Jsoup.connect("https://www.must.ac.ug/news-and-events").get();
                Elements newsHeadlines = doc.select("h2.entry-title a");
                for (Element headline : newsHeadlines) {
                    headlines.add(headline.text());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return headlines;
        }

        @Override
        protected void onPostExecute(ArrayList<String> headlines) {
            if (headlines.isEmpty()) {
                Toast.makeText(NewsActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
            } else {
                newsList.clear();
                newsList.addAll(headlines);
                newsAdapter.notifyDataSetChanged();
            }
        }
    }
}
