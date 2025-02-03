package mdad.localdata.trakit.mainfragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import data.network.ICallback;
import data.network.controller.ChartController;
import formatter.LineChartXAxisValueFormatter;
import mdad.localdata.trakit.AuthActivity;
import mdad.localdata.trakit.R;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    LineChart lineChart;
    PieChart pieChart;
    int currYear;
    TextView tvYear;
    ImageButton btnNextYear, btnPrevYear;
    ChartController chartController;
    List<Entry> monthlyIncomeArr = new ArrayList<>();
    List<Entry> monthlySpendingArr = new ArrayList<>();
    ArrayList<PieEntry> pieChartValues = new ArrayList<>();

    String[] labels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    int[] colorArray = {Color.GREEN, Color.RED};
    String[] legendName = {"Income", "Expense"};

    public void onViewCreated(View view, Bundle savedInstanceState){
        int[] pieChartColorArray = {ContextCompat.getColor(getContext(), R.color.pastel_red), ContextCompat.getColor(getContext(), R.color.pastel_blue), ContextCompat.getColor(getContext(), R.color.pastel_yellow),ContextCompat.getColor(getContext(), R.color.pastel_purple),ContextCompat.getColor(getContext(), R.color.pastel_green)};
        chartController = new ChartController(getContext());
        lineChart = view.findViewById(R.id.lineChart);
        pieChart = view.findViewById(R.id.pieChart);
        tvYear = view.findViewById(R.id.tvMonth);
        btnPrevYear = view.findViewById(R.id.btnPrevMonth);
        btnNextYear = view.findViewById(R.id.btnNextMonth);
        currYear = Calendar.getInstance().get(Calendar.YEAR);
        tvYear.setText(String.valueOf(currYear));
        getChartData(String.valueOf(currYear), pieChartColorArray);


//        pieChartValues = new ArrayList<>();
//        pieChartValues.add(new PieEntry(15,"Food"));
//        pieChartValues.add(new PieEntry(8,"Trans"));
//        pieChartValues.add(new PieEntry(22,"Entertainment"));
//        pieChartValues.add(new PieEntry(30,"Bills"));
//        pieChartValues.add(new PieEntry(18,"Custom"));

        btnPrevYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currYear--;
                tvYear.setText(String.valueOf(currYear));
                getChartData(String.valueOf(currYear),pieChartColorArray);
            }
        });
        btnNextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currYear++;
                tvYear.setText(String.valueOf(currYear));
                getChartData(String.valueOf(currYear),pieChartColorArray);
            }
        });
    }
    public void getChartData(String year, int[] pieChartColorArray){
        chartController.getLineChartData(year, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null){
                    lineChart.setNoDataText("No data found");
                }
                try{
                    monthlyIncomeArr.clear();
                    monthlySpendingArr.clear();
                    JSONObject dataObject = (JSONObject) result;
                    JSONArray monthlyIncome = new JSONArray(dataObject.optString("monthlyIncome"));
                    JSONArray monthlySpending = new JSONArray(dataObject.optString("monthlySpending"));

                    for (int i=0;i<monthlyIncome.length();i++){
                        float incomeValue = ((Number) monthlyIncome.get(i)).floatValue();
                        float spendingValue = ((Number) monthlySpending.get(i)).floatValue();
                        monthlyIncomeArr.add(new Entry(i,incomeValue));
                        monthlySpendingArr.add(new Entry(i, spendingValue));
                    }

                    List<Entry> spendingEntries = monthlySpendingArr;
                    List<Entry> IncomeEntries = monthlyIncomeArr;
                    LineDataSet spendingDataset = new LineDataSet(spendingEntries, "spendings");
                    LineDataSet IncomeDataset = new LineDataSet(IncomeEntries, "incomes");
                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(spendingDataset);
                    dataSets.add(IncomeDataset);
                    spendingDataset.setDrawValues(true); //set the value for each point; boolean

                    spendingDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
                    spendingDataset.setLineWidth(4);
                    spendingDataset.setColor(Color.RED);
                    IncomeDataset.setLineWidth(4);
                    IncomeDataset.setColor(Color.GREEN);
                    spendingDataset.setDrawCircles(false);
                    IncomeDataset.setDrawCircles(false);
                    spendingDataset.setValueTextSize(10);
                    IncomeDataset.setValueTextSize(10);

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setValueFormatter(new LineChartXAxisValueFormatter(labels));
                    xAxis.setGranularityEnabled(true);
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setAxisMinimum(0f);
                    xAxis.setAxisMaximum(labels.length - 1);
                    xAxis.setLabelCount(labels.length, true);

                    // Setup Y Axis
                    YAxis yAxis = lineChart.getAxisLeft();
                    yAxis.setGranularity(1f);

                    Legend legend = lineChart.getLegend();
                    legend.setEnabled(true); // Enable legend if not visible
                    legend.setTextSize(12);
                    legend.setForm(Legend.LegendForm.LINE);
                    legend.setFormSize(10);
                    legend.setXEntrySpace(15);
                    legend.setFormToTextSpace(10);

                    LegendEntry[] legendEntries = new LegendEntry[2];

                    for (int i=0; i<legendEntries.length;i++){
                        LegendEntry legendEntry = new LegendEntry();
                        legendEntry.formColor = colorArray[i];
                        legendEntry.label = String.valueOf(legendName[i]);
                        legendEntries[i] = legendEntry;
                    }
                    legend.setCustom(legendEntries);

                    LineData lineData = new LineData(dataSets);
                    lineChart.getXAxis().setDrawGridLines(false);
                    lineChart.getLegend().setYEntrySpace(5);
                    lineChart.getDescription().setEnabled(false);

                    lineChart.getAxisLeft().setDrawGridLines(false);
                    lineChart.getAxisLeft().setDrawAxisLine(false);
                    lineChart.getAxisLeft().setDrawLabels(false);
                    lineChart.getAxisLeft().setStartAtZero(true);

                    lineChart.getAxisRight().setDrawGridLines(false);
                    lineChart.getAxisRight().setDrawLabels(false);
                    lineChart.getAxisRight().setDrawAxisLine(false);
                    lineChart.getAxisLeft().setStartAtZero(true);

                    lineChart.setNoDataText("No data found");
                    lineChart.setNoDataTextColor(ContextCompat.getColor(getContext(), R.color.base));
                    lineChart.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.light_gray));
                    lineChart.animateX(1500, Easing.EaseInOutQuad);
                    lineChart.setData(lineData);
                    lineChart.invalidate();

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "JSON error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
        chartController.getPieChartData(year, new ICallback() {
            @Override
            public void onSuccess(Object result) {
                if (result == null){
                    pieChart.clear(); // Clear any existing data
                    pieChart.setCenterText("No data found");
                    pieChart.setHoleColor(Color.TRANSPARENT);
                    PieDataSet pieDataSet = new PieDataSet(new ArrayList<>(), "");
                    PieData pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);

                    // Refresh the chart to display the no data message
                    pieChart.invalidate();
                }
                else{
                    try{
                        pieChartValues.clear();
                        JSONArray dataArray = (JSONArray) result;
                        for(int i=0; i<dataArray.length();i++){
                            JSONObject jsonObject = dataArray.getJSONObject(i);
                            String name = jsonObject.getString("name");
                            float totalAmount = (float) jsonObject.getDouble("total_amount");
                            pieChartValues.add(new PieEntry(totalAmount, name));
                        }
                        PieDataSet pieDataSet = new PieDataSet(pieChartValues, "");
                        pieDataSet.setColors(pieChartColorArray);
                        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

                        PieData pieData = new PieData(pieDataSet);
                        pieData.setValueTextSize(10);
                        pieData.setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                return String.format("%.1f%%", value); // Append % with one decimal precision
                            }
                        });

                        Legend legend = pieChart.getLegend();
                        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                        legend.setTextSize(10);
                        pieChart.setUsePercentValues(true);
                        pieChart.setDrawEntryLabels(false);
                        pieChart.setEntryLabelColor(ContextCompat.getColor(getContext(),R.color.base));
                        pieChart.setTransparentCircleRadius(0);
                        pieChart.setDragDecelerationFrictionCoef(0.8f);
                        pieChart.setRotationAngle(0);
                        pieChart.animateY(1500, Easing.EaseInOutQuad);
                        pieChart.setCenterText("Top 5 categories");
                        pieChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.setData(pieData);
                        pieChart.invalidate();
                        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                            @Override
                            public void onValueSelected(Entry e, Highlight h) {
                                PieEntry pieEntry = (PieEntry) e;
                                String label = pieEntry.getLabel();
                                float value = pieEntry.getValue();
                                pieChart.setCenterText(label + "\n" + value);
                            }

                            @Override
                            public void onNothingSelected() {
                                pieChart.setCenterText("Top 5 categories");
                            }
                        });
//
//                    Log.d("result", String.valueOf(dataObject));
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getContext(), "JSON error: "+ e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthFailure(String message) {
                Intent goToLoginPage = new Intent(getContext(), AuthActivity.class);
                goToLoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goToLoginPage);
            }
        });
    }

}