package formatter;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class LineChartXAxisValueFormatter extends ValueFormatter {
    private final String[] mValues;

    //used to reduce the number of values displayed on the x-axis of the line chart
    public LineChartXAxisValueFormatter(String[] values) {
        this.mValues = values;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        axis.setLabelCount(6, true);
        if (value >= 0 && value < mValues.length) {
            return mValues[(int) value];
        }
        return "";
    }
}
