package saifulla.calculator.simple;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // TextView to display the calculations
    private TextView textViewResult;

    // Represent whether the lastly pressed key is numeric or not
    private boolean lastNumeric;

    // Represent that current state is error or not
    private boolean stateError;

    // If true, do not allow to add another DOT in the current number
    private boolean lastDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the result TextView
        textViewResult = findViewById(R.id.textViewResult);
        // Set click listeners for buttons
        setClickListeners();
    }

    /**
     * Set OnClickListeners for all buttons.
     */
    @SuppressLint("SetTextI18n")
    private void setClickListeners() {
        // --- Number Buttons ---
        View.OnClickListener numberListener = v -> {
            Button button = (Button) v;
            if (stateError) {
                textViewResult.setText(button.getText());
                stateError = false;
            } else {
                if (textViewResult.getText().toString().equals("0")) {
                    textViewResult.setText(button.getText());
                } else {
                    textViewResult.append(button.getText());
                }
            }
            lastNumeric = true;
        };

        findViewById(R.id.button_0).setOnClickListener(numberListener);
        findViewById(R.id.button_1).setOnClickListener(numberListener);
        findViewById(R.id.button_2).setOnClickListener(numberListener);
        findViewById(R.id.button_3).setOnClickListener(numberListener);
        findViewById(R.id.button_4).setOnClickListener(numberListener);
        findViewById(R.id.button_5).setOnClickListener(numberListener);
        findViewById(R.id.button_6).setOnClickListener(numberListener);
        findViewById(R.id.button_7).setOnClickListener(numberListener);
        findViewById(R.id.button_8).setOnClickListener(numberListener);
        findViewById(R.id.button_9).setOnClickListener(numberListener);

        // --- Operator Buttons ---
        View.OnClickListener operatorListener = v -> {
            if (lastNumeric && !stateError) {
                Button button = (Button) v;
                textViewResult.append(button.getText());
                lastNumeric = false;
                lastDot = false; // Reset the DOT flag
            }
        };

        findViewById(R.id.button_add).setOnClickListener(operatorListener);
        findViewById(R.id.button_divide).setOnClickListener(operatorListener);
        findViewById(R.id.button_multiply).setOnClickListener(operatorListener);

        // --- Special Case: Subtract/Minus Button ---
        findViewById(R.id.button_subtract).setOnClickListener(v -> {
            String currentText = textViewResult.getText().toString();
            if (currentText.equals("0")) {
                textViewResult.setText("-");
                lastNumeric = false;
            } else if (!lastNumeric && !currentText.endsWith("-")) {
                textViewResult.append("-");
                lastNumeric = false;
            } else if (lastNumeric && !stateError) {
                textViewResult.append("-");
                lastNumeric = false;
                lastDot = false;
            }
        });

        // --- Dot Button ---
        findViewById(R.id.button_dot).setOnClickListener(v -> {
            if (lastNumeric && !stateError && !lastDot) {
                textViewResult.append(".");
                lastNumeric = false;
                lastDot = true;
            }
        });

        // --- Clear Button ---
        findViewById(R.id.button_clear).setOnClickListener(v -> {
            textViewResult.setText("0");
            lastNumeric = false;
            stateError = false;
            lastDot = false;
        });

        // --- Backspace Button ---
        findViewById(R.id.button_backspace).setOnClickListener(v -> {
            String currentText = textViewResult.getText().toString();
            if (!currentText.equals("0") && !currentText.isEmpty()) {
                char lastChar = currentText.charAt(currentText.length() - 1);
                if (lastChar == '.') {
                    lastDot = false;
                }
                String newText = currentText.substring(0, currentText.length() - 1);
                if (newText.isEmpty() || newText.equals("-")) {
                    textViewResult.setText("0");
                    lastNumeric = false;
                } else {
                    textViewResult.setText(newText);
                    char newLastChar = newText.charAt(newText.length() - 1);
                    lastNumeric = Character.isDigit(newLastChar) || newLastChar == '%';
                }
            }
        });

        // --- Percentage Button ---
        // --- Percentage Button ---
        findViewById(R.id.button_percentage).setOnClickListener(v -> {
            if (!lastNumeric || stateError) {
                return; // Do nothing if the last input wasn't a number or if there's an error
            }

            String expression = textViewResult.getText().toString();

            try {
                // Find the starting index of the last number entered.
                int numberStartIndex = -1;
                for (int i = expression.length() - 1; i >= 0; i--) {
                    char c = expression.charAt(i);
                    if (!Character.isDigit(c) && c != '.') {
                        numberStartIndex = i + 1;
                        break;
                    }
                    if (i == 0) {
                        numberStartIndex = 0;
                    }
                }

                if (numberStartIndex == -1) return; // Should not happen if lastNumeric is true

                String prefix = expression.substring(0, numberStartIndex);
                double numberToConvert = Double.parseDouble(expression.substring(numberStartIndex));
                double result;

                // If there's no operator before the number (e.g., just "50%"), treat it as number/100.
                if (prefix.isEmpty() || prefix.equals("-")) {
                    result = numberToConvert / 100.0;
                    textViewResult.setText(prefix + formatNumber(result));
                } else {
                    // There is an operator.
                    char operator = prefix.charAt(prefix.length() - 1);
                    String expressionBeforeOperator = prefix.substring(0, prefix.length() - 1);

                    // For multiplication or division, B% is simply treated as B/100.
                    if (operator == '×' || operator == '÷') {
                        result = numberToConvert / 100.0;
                    } else { // For addition or subtraction, calculate B% of the preceding number.

                        // --- MODIFIED LOGIC START ---
                        // Find the last number in the expression before the operator.
                        int lastOperatorIndex = -1;
                        for (int i = expressionBeforeOperator.length() - 1; i >= 0; i--) {
                            char c = expressionBeforeOperator.charAt(i);
                            if (c == '+' || c == '-' || c == '×' || c == '÷') {
                                lastOperatorIndex = i;
                                break;
                            }
                        }

                        String baseNumberString;
                        if (lastOperatorIndex != -1) {
                            baseNumberString = expressionBeforeOperator.substring(lastOperatorIndex + 1);
                        } else {
                            baseNumberString = expressionBeforeOperator; // The whole preceding part is the number.
                        }

                        if (baseNumberString.isEmpty()) {
                            // This handles cases like "+5%" where there's no preceding number. Treat base as 0.
                            result = 0;
                        } else {
                            double baseValue = Double.parseDouble(baseNumberString);
                            // Calculate percentage based on the base value. e.g., for 1000-5%, it's 5% of 1000.
                            result = (baseValue * numberToConvert) / 100.0;
                        }
                        // --- MODIFIED LOGIC END ---
                    }
                    // Update the TextView by replacing the last number with the calculated percentage value.
                    textViewResult.setText(prefix + formatNumber(result));
                }

                lastNumeric = true;
                lastDot = textViewResult.getText().toString().contains(".");

            } catch (Exception e) {
                textViewResult.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        });

        // --- Equals Button ---
        findViewById(R.id.button_equals).setOnClickListener(v -> onEqual());
    }

    private String formatNumber(double d) {
        if (d == (long) d) {
            return String.format(Locale.US, "%d", (long) d);
        } else {
            return String.valueOf(d);
        }
    }

    /**
     * Logic to calculate the solution.
     */
    @SuppressLint("SetTextI18n")
    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = textViewResult.getText().toString();
            try {
                // The handlePercentage call is no longer needed here.
                // txt = handlePercentage(txt);

                // Replace user-friendly operators with ones exp4j understands
                txt = txt.replace('×', '*').replace('÷', '/');

                Expression expression = new ExpressionBuilder(txt).build();
                double result = expression.evaluate();

                // Use the new formatNumber helper method here as well
                textViewResult.setText(formatNumber(result));

                lastDot = textViewResult.getText().toString().contains("."); // Correctly set lastDot
            } catch (Exception ex) {
                textViewResult.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        }
    }
}
