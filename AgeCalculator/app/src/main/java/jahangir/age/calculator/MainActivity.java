package jahangir.age.calculator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView textViewSelectedDate;
    private TextView textViewResultAge;
    private TextView textViewResultTotalMonths;
    private TextView textViewResultTotalWeeks;
    private TextView textViewResultTotalDays;
    private MaterialCardView cardViewResult;
    private static final String DATE_PICKER_TAG = "DATE_PICKER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        Button buttonSelectDate = findViewById(R.id.buttonSelectDate);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        textViewResultAge = findViewById(R.id.textViewResultAge);
        textViewResultTotalMonths = findViewById(R.id.textViewResultTotalMonths);
        textViewResultTotalWeeks = findViewById(R.id.textViewResultTotalWeeks);
        textViewResultTotalDays = findViewById(R.id.textViewResultTotalDays);
        cardViewResult = findViewById(R.id.cardViewResult);

        // Build the Material Date Picker
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Your Date of Birth");
        final MaterialDatePicker<Long> materialDatePicker = builder.build();

        // Set click listener for the date selection button
        buttonSelectDate.setOnClickListener(v -> {
            // Check if the date picker is already showing to prevent a crash on multiple clicks.
            if (getSupportFragmentManager().findFragmentByTag(DATE_PICKER_TAG) == null) {
                materialDatePicker.show(getSupportFragmentManager(), DATE_PICKER_TAG);
            }
        });

        // Handle the positive button click (i.e., when user presses "OK")
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Format the selected date to display it
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String selectedDateString = sdf.format(new Date(selection));
            textViewSelectedDate.setText(selectedDateString);

            // Create Calendar instances for calculation
            Calendar dob = Calendar.getInstance();
            dob.setTimeInMillis(selection);
            // The MaterialDatePicker provides the time in UTC. Adjust to local for display.
            dob.setTimeZone(TimeZone.getTimeZone("UTC"));

            Calendar today = Calendar.getInstance();

            // Perform all age calculations and update the UI
            displayAgeCalculations(dob, today);

            // Make the result card visible
            cardViewResult.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Calculates the age in various formats and updates the UI TextViews.
     * @param dob The Calendar instance for the Date of Birth (in UTC).
     * @param today The Calendar instance for the current date.
     */
    @SuppressLint("SetTextI18n")
    private void displayAgeCalculations(Calendar dob, Calendar today) {
        // --- Years, Months, Days Calculation ---
        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        int days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);

        if (days < 0) {
            months--;
            days += dob.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (months < 0) {
            years--;
            months += 12;
        }

        // Handle future date selection
        if (years < 0) {
            textViewResultAge.setText("Invalid Date");
            textViewResultTotalMonths.setText("Please select a date");
            textViewResultTotalWeeks.setText("from the past.");
            textViewResultTotalDays.setText("");
            return;
        }

        String ageString = String.format(Locale.getDefault(), "%d Years, %d Months, %d Days", years, months, days);
        textViewResultAge.setText(ageString);


        // --- Summary Calculations (Total Months, Weeks, Days) ---
        long diffInMillis = today.getTimeInMillis() - dob.getTimeInMillis();

        long totalDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        long totalWeeks = totalDays / 7;

        // More accurate total months calculation
        int totalMonths = years * 12 + months;

        textViewResultTotalDays.setText(String.format(Locale.getDefault(), "%,d Total Days", totalDays));
        textViewResultTotalWeeks.setText(String.format(Locale.getDefault(), "%,d Total Weeks", totalWeeks));
        textViewResultTotalMonths.setText(String.format(Locale.getDefault(), "%,d Total Months", totalMonths));
    }
}
