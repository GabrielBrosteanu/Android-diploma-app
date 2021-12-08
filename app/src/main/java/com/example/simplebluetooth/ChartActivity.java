package com.example.simplebluetooth;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class ChartActivity
        extends AppCompatActivity {

    TextView tvWizards, tvHornets;
    Button newGame;
    PieChart pieChart;
    private float statHome;
    private float statGuest;
    private float hTeamScored;
    private float hTeamMissed;
    private float gTeamScored;
    private float gTeamMissed;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_display);

        tvWizards = findViewById(R.id.tvWizards);
        tvHornets = findViewById(R.id.tvHornets);
        newGame = findViewById(R.id.newGameBtn);
        pieChart = findViewById(R.id.piechart);

        Intent intent = getIntent();
        Intent intent2 = new Intent(this,StartMenu.class);

        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent2);
            }
        });

        hTeamScored = intent.getIntExtra("homeTeamShotsScored", 0);
        hTeamMissed = intent.getIntExtra("homeTeamMisses", 0);
        gTeamScored = intent.getIntExtra("guestTeamShotsScored", 0);
        gTeamMissed = intent.getLongExtra("guestTeamMisses", 0);

        statHome = (hTeamScored / (hTeamScored + hTeamMissed))*100;
        statGuest = (gTeamScored / (gTeamScored + gTeamMissed))*100;

        setData();
    }

    private void setData()
    {
        String trD = Float.toString(statHome);
        String tr = String.format("%.2s", trD);
        tvWizards.setText(tr);
        String trDg = Float.toString(statGuest);
        String trg = String.format("%.2s", trDg);
        tvHornets.setText(trg);

        pieChart.addPieSlice(
                new PieModel(
                        "Wizards %",
                        Float.parseFloat(tvWizards.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Hornets %",
                        Float.parseFloat(tvHornets.getText().toString()),
                        Color.parseColor("#66BB6A")));

        pieChart.startAnimation();
    }
}
