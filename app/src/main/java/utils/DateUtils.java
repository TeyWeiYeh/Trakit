package utils;

import java.text.DateFormatSymbols;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
    public static String convertToYearMonth(String monthYear) {
        // Mapping of month names to their respective numeric values
        Map<String, String> monthMap = new HashMap<>();
        String[] months = new DateFormatSymbols().getMonths();
        for (int i = 0; i < months.length; i++) {
            if (!months[i].isEmpty()) {
                monthMap.put(months[i].toLowerCase(), String.format("%02d", i + 1));
            }
        }

        // Split the input to extract month and year
        String[] parts = monthYear.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Input must be in 'Month Year' format");
        }

        String month = parts[0].toLowerCase();
        String year = parts[1];

        // Validate and convert
        if (monthMap.containsKey(month)) {
            String numericMonth = monthMap.get(month);
            return year + "-" + numericMonth;
        } else {
            throw new IllegalArgumentException("Invalid month name: " + month);
        }
    }
}
