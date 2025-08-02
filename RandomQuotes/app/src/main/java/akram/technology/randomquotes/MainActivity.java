package akram.technology.randomquotes;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView quoteText, authorText;
    private List<Quote> quotesList = new ArrayList<>();
    private Random random;
    private boolean isDataLoaded = false;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        quoteText = findViewById(R.id.quoteText);
        authorText = findViewById(R.id.authorText);
        FloatingActionButton fabRefresh = findViewById(R.id.fabRefresh);
        random = new Random();

        // Show loading state
        showLoadingState();

        // Start loading quotes in background
        loadQuotesInBackground();

        // Set click listener for refresh button
        fabRefresh.setOnClickListener(v -> {
            if (isDataLoaded) {
                showRandomQuote();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showLoadingState() {
        quoteText.setText("Loading quotes...");
        authorText.setText("");
    }

    @SuppressLint("SetTextI18n")
    private void loadQuotesInBackground() {
        executorService.execute(() -> {
            try {
                // Read and parse JSON in background thread
                List<Quote> loadedQuotes = loadQuotesFromJson();

                // Update UI on main thread
                mainHandler.post(() -> {
                    quotesList = loadedQuotes;
                    isDataLoaded = true;
                    showRandomQuote();
                });
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading quotes", e);
                mainHandler.post(() -> {
                    quoteText.setText("Error loading quotes");
                    authorText.setText("Please try again later");
                });
            }
        });
    }

    private List<Quote> loadQuotesFromJson() throws IOException, JSONException {
        List<Quote> tempQuotesList = new ArrayList<>();

        // Read JSON file from assets/raw
        InputStream is = getResources().openRawResource(R.raw.random_quotes);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);

        // Parse JSON using JSONArray and JSONObject
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject quoteObject = jsonArray.getJSONObject(i);
            Quote quote = new Quote();
            quote.setQuote(quoteObject.getString("quote"));
            quote.setAuthor(quoteObject.getString("author"));
            tempQuotesList.add(quote);
        }

        return tempQuotesList;
    }

    @SuppressLint("SetTextI18n")
    private void showRandomQuote() {
        if (!quotesList.isEmpty()) {
            int randomIndex = random.nextInt(quotesList.size());
            Quote randomQuote = quotesList.get(randomIndex);

            quoteText.setText(randomQuote.getQuote());
            authorText.setText("- " + randomQuote.getAuthor());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}