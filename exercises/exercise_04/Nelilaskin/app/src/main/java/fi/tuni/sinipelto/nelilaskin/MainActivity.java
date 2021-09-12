package fi.tuni.sinipelto.nelilaskin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set button functionality
        final EditText inp1 = findViewById(R.id.numInput1);
        final EditText inp2 = findViewById(R.id.numInput2);
        final TextView res = findViewById(R.id.resultView);
        final Button btn = findViewById(R.id.calcBtn);

        btn.setOnClickListener(v -> {
            String v1 = String.valueOf(inp1.getText());
            if (v1.isEmpty()) return;
            String v2 = String.valueOf(inp2.getText());
            if (v2.isEmpty()) return;
            res.setText(
                    String.valueOf(MathUtils.calcSum(Double.parseDouble(v1), Double.parseDouble(v2)))
            );
        });
    }
}
