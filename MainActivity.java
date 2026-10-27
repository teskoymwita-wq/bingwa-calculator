package com.bingwa.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private TextView expressionView, displayView;
    private String display = "0", expression = "";
    private double accumulator = 0, repeatRight = 0;
    private String operator = null, repeatOperator = null;
    private boolean overwrite = true, opJustSet = false, error = false;
    private final List<String> history = new ArrayList<>();
    private LinearLayout historyPanel;
    private final int bg = Color.rgb(16,24,32), panel = Color.rgb(31,42,54),
        lcd = Color.rgb(12,19,27), ink = Color.rgb(240,245,249),
        muted = Color.rgb(154,170,184), green = Color.rgb(32,201,151),
        fn = Color.rgb(57,72,86), operatorColor = Color.rgb(39,99,112);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildUi();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(radius);
        return d;
    }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(14));
        root.setBackgroundColor(bg);
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = text("＝", 24, green);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(shape(panel, dp(10)));
        header.addView(mark, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView title = text("  Bingwa Calculator", 18, ink);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        header.addView(title);
        root.addView(header);

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(dp(18), dp(18), dp(18), dp(18));
        screen.setBackground(shape(lcd, dp(20)));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(142));
        sp.topMargin = dp(22);
        root.addView(screen, sp);
        expressionView = text("", 14, muted);
        expressionView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        screen.addView(expressionView, new LinearLayout.LayoutParams(-1, dp(32)));
        displayView = text("0", 44, ink);
        displayView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        displayView.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        screen.addView(displayView, new LinearLayout.LayoutParams(-1, 0, 1));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        String[][] keys = {
            {"AC", "±", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "−"},
            {"1", "2", "3", "+"},
            {"⌫", "0", ".", "="}
        };
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, 0, 1);
        gp.topMargin = dp(18);
        root.addView(grid, gp);
        for (String[] row : keys) {
            for (String key : row) {
                Button b = new Button(this);
                b.setText(key); b.setTextSize(22);
                b.setAllCaps(false); b.setTextColor(ink);
                b.setPadding(0, 0, 0, 0);
                int c = "÷×−+=".contains(key) ? operatorColor :
                    ("AC±%⌫".contains(key) ? fn : panel);
                if (key.equals("=")) c = green;
                b.setTextColor(key.equals("=") ? bg : ink);
                b.setBackground(shape(c, dp(17)));
                GridLayout.LayoutParams p = new GridLayout.LayoutParams();
                p.width = 0; p.height = 0;
                p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                p.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                p.setMargins(dp(4), dp(4), dp(4), dp(4));
                grid.addView(b, p);
                b.setOnClickListener(v -> press(key));
            }
        }
        TextView historyTitle = text("Recent calculations", 14, muted);
        historyTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, dp(28));
        hp.topMargin = dp(8);
        root.addView(historyTitle, hp);
        historyPanel = new LinearLayout(this);
        historyPanel.setOrientation(LinearLayout.VERTICAL);
        root.addView(historyPanel, new LinearLayout.LayoutParams(-1, dp(66)));
        render();
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    private void press(String key) {
        if (key.matches("[0-9]")) {
            if (error) clear();
            if (overwrite || opJustSet) { display = key.equals("0") ? "0" : key; overwrite = false; opJustSet = false; }
            else if (display.replace("-", "").replace(".", "").length() < 15) display = display.equals("0") ? key : display + key;
        } else if (key.equals(".")) {
            if (error) clear();
            if (overwrite || opJustSet) { display = "0."; overwrite = false; opJustSet = false; }
            else if (!display.contains(".")) display += ".";
        } else if (key.equals("AC")) clear();
        else if (key.equals("⌫")) {
            if (!overwrite && !opJustSet && !error) {
                display = display.length() <= 1 || (display.length() == 2 && display.startsWith("-")) ? "0" : display.substring(0, display.length()-1);
            } else display = "0";
            overwrite = false;
        } else if (key.equals("±")) {
            if (!error && !display.equals("0")) display = display.startsWith("-") ? display.substring(1) : "-" + display;
        } else if (key.equals("%")) {
            if (!error) { display = fmt(Double.parseDouble(display) / 100.0); overwrite = true; }
        } else if ("+−×÷".contains(key)) {
            if (error) return;
            String op = key.equals("+") ? "+" : key.equals("−") ? "-" : key.equals("×") ? "*" : "/";
            if (operator != null && !opJustSet) calculate(false);
            accumulator = Double.parseDouble(display);
            operator = op; expression = fmt(accumulator) + " " + key;
            overwrite = true; opJustSet = true; repeatOperator = null;
        } else if (key.equals("=")) {
            if (error) return;
            if (operator != null) {
                repeatRight = Double.parseDouble(display); repeatOperator = operator;
                calculate(true);
            } else if (repeatOperator != null) {
                double a = Double.parseDouble(display);
                double result = compute(a, repeatOperator, repeatRight);
                if (Double.isNaN(result) || Double.isInfinite(result)) { fail(); return; }
                expression = fmt(a) + " " + sym(repeatOperator) + " " + fmt(repeatRight) + " =";
                display = fmt(result); addHistory(expression, display); overwrite = true;
            }
        }
        render();
    }
    private void calculate(boolean equals) {
        double right = Double.parseDouble(display);
        double result = compute(accumulator, operator, right);
        if (Double.isNaN(result) || Double.isInfinite(result)) { fail(); return; }
        String line = fmt(accumulator) + " " + sym(operator) + " " + fmt(right);
        display = fmt(result);
        if (equals) { expression = line + " ="; addHistory(expression, display); }
        else expression = fmt(result);
        operator = equals ? null : operator;
        overwrite = true; opJustSet = false;
    }
    private double compute(double a, String op, double b) {
        switch(op) { case "+": return a+b; case "-": return a-b; case "*": return a*b; case "/": return b == 0 ? Double.NaN : a/b; default: return b; }
    }
    private String sym(String op) { return op.equals("*") ? "×" : op.equals("/") ? "÷" : op.equals("-") ? "−" : "+"; }
    private String fmt(double x) {
        if (x == 0) return "0";
        if (Math.abs(x) >= 1e12 || Math.abs(x) < 1e-8) return String.format(java.util.Locale.US, "%.6g", x);
        return BigDecimal.valueOf(x).round(new MathContext(12)).stripTrailingZeros().toPlainString();
    }
    private void fail() { display = "Error"; expression = "Cannot divide by zero"; error = true; operator = null; overwrite = true; }
    private void clear() { display="0"; expression=""; accumulator=0; operator=null; repeatOperator=null; repeatRight=0; overwrite=true; opJustSet=false; error=false; }
    private void addHistory(String exp, String result) {
        history.add(0, exp + "  " + result);
        while (history.size() > 2) history.remove(history.size()-1);
    }
    private void render() {
        expressionView.setText(expression);
        displayView.setText(display);
        displayView.setTextSize(display.length() > 12 ? 27 : display.length() > 9 ? 34 : 44);
        if (historyPanel == null) return;
        historyPanel.removeAllViews();
        for (String item : history) {
            TextView t = text(item, 12, muted);
            historyPanel.addView(t, new LinearLayout.LayoutParams(-1, dp(28)));
        }
    }
}
