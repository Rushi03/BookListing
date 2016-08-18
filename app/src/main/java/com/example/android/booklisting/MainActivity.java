package com.example.android.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String BOOK_SEARCH_API = "https://www.googleapis.com/books/v1/volumes?q=";

    private Button submit;
    private ArrayList<Book> searchBook;
    private ListView listView;
    private ResultsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBook = new ArrayList<>();
        listView = (ListView) findViewById(R.id.list);
        adapter = new ResultsAdapter(this, searchBook);
        listView.setAdapter(adapter);

        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()) {
                    new SearchAsyncTask().execute();
                }
            }
        });

        if (savedInstanceState != null) {
            ArrayList<Book> books = (ArrayList<Book>) savedInstanceState.getSerializable("key");
            searchBook.addAll(books);
            adapter.notifyDataSetChanged();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(this, "Not Connected", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);
        savedState.putSerializable("key", searchBook);
    }

    /*
    *Async task to connect and execute gathering search results from Google API
    */
    class SearchAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        TextView search = (TextView) findViewById(R.id.search);
        String query = search.getText().toString();

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {
            String[] searchQuery;
            if (query.contains(" ")) {
                searchQuery = query.split(" ");
                query = searchQuery[0] + searchQuery[1];
            }

            // Create URL object
            URL url = createUrl(BOOK_SEARCH_API + query + "&maxResults=10");

            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error with the the url/search.");
            }

            //Place extracted json response in -Book object
            ArrayList<Book> info = extractItemFromJson(jsonResponse);
            return info;
        }

        /**
         * Set empty TextView just in case there is no book
         *
         * @param book two outputs(title, author) from custom class
         */
        @Override
        protected void onPostExecute(ArrayList<Book> book) {
            TextView emptyText = (TextView) findViewById(R.id.empty_text_view);

            if (book == null) {
                listView.setEmptyView(emptyText);
            } else {
                searchBook.clear();
                searchBook.addAll(book);
                adapter.notifyDataSetChanged();
            }
        }

        /**
         * Create the url being sent to the internet
         *
         * @param stringUrl the url being sent
         * @return either null if error or the proper url
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        /**
         * Requesting the HTTP in the web browser
         *
         * @param url the url being sent in teh request
         * @return the jsonresponse or null depending on the url
         * @throws IOException propability of the url possibly being invalid
         */
        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                // Handle the exception
                Log.e(LOG_TAG, "The input/output is not properly working", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        /**
         * Read the input and piecing it together into a string for jsonResponse
         *
         * @param inputStream information from the site after connection
         * @return null or the string depending on inputStream
         * @throws IOException whether the input is in valid range
         */
        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        /**
         * Extracting relevant information needed from the json
         *
         * @param resultsJSON returned from the Google API site
         * @return null or new Book(title, author) depending on resultsJSON
         */
        private ArrayList<Book> extractItemFromJson(String resultsJSON) {

            ArrayList<Book> temp = new ArrayList<>();

            try {
                JSONObject baseJsonResponse = new JSONObject(resultsJSON);
                JSONArray itemArray = baseJsonResponse.getJSONArray("items");

                for (int i = 0; i < itemArray.length(); i++) {

                    JSONObject responseObject = itemArray.getJSONObject(i);
                    JSONObject volumeInfo = responseObject.getJSONObject("volumeInfo");

                    String title = volumeInfo.getString("title");
                    String author = "Unavailable";

                      if (volumeInfo.has("authors")) {
                          JSONArray authors = volumeInfo.getJSONArray("authors");
                          author = authors.getString(0);
                      }

                        temp.add(new Book(title, author));

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the search result JSON.");
            }

            return temp;
        }
    }
}




