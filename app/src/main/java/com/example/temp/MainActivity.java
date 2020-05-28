package com.example.temp;



import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static java.util.Objects.isNull;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();

    ArrayAdapter arrayAdapter;

    SQLiteDatabase articlesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        articlesDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);

       articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, title VARCHAR, content VARCHAR)");



        DownloadTask task = new DownloadTask();
        try {

            task.execute("https://newsapi.org/v2/top-headlines?country=in&apiKey=c6a8e7bf78dc44fa8f15ca3608b47e1c");

        } catch (Exception e) {

        }

        ListView listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_activated_1, titles);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              Intent intent = new Intent(getApplicationContext(), Main3Activity.class);
                intent.putExtra("content", content.get(i));

                startActivity(intent);
            }
        });

        updateListView();
    }

    public void updateListView() {
        Cursor c = articlesDB.rawQuery("SELECT * FROM articles", null);

        int contentIndex = c.getColumnIndex("content");
        int titleIndex = c.getColumnIndex("title");

        if (c.moveToFirst()) {
            titles.clear();
            content.clear();

            do {

                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            arrayAdapter.notifyDataSetChanged();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection urlConnection = null;
int data;
            try {

                url = new URL(urls[0]);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = urlConnection.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                 data = inputStreamReader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = inputStreamReader.read();
                }
              //  Log.i("url content",result);

                articlesDB.execSQL("DELETE FROM articles");


                JSONObject jsonObject = new JSONObject(result);
                String getarticles = jsonObject.getString("articles");
                JSONArray arr = new JSONArray(getarticles);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonpart = arr.getJSONObject(i);

                            String articleTitle = jsonpart.getString("title");
                            String articleUrl = jsonpart.getString("url");
                    if(articleUrl.startsWith("timesofindia", 8))
                    {
                        continue;
                    }

                            String sql = "INSERT INTO articles (title, content) VALUES (?, ?)";
                            SQLiteStatement statement = articlesDB.compileStatement(sql);
                            statement.bindString(1, articleTitle);
                            statement.bindString(2, articleUrl);
                            statement.execute();

                }
               // Log.i("URL Content", result);
                return result;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }





            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


        //    updateListView();
        }
    }
}
//*/
