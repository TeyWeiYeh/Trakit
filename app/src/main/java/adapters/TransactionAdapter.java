package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import mdad.localdata.trakit.R;

public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> data;
    private static final String[] from = {"type", "date", "catName", "amount", "typeIcon"};
    private static final int[] to = {R.id.tvTransType, R.id.tvDate, R.id.tvTransCat, R.id.tvAmount, R.id.tvTransIcon};

    public TransactionAdapter(Context context, ArrayList<HashMap<String, String>> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.transaction_view_items, parent, false);
            holder = new ViewHolder();
            holder.tvType = convertView.findViewById(R.id.tvTransType);
            holder.tvDate = convertView.findViewById(R.id.tvDate);
            holder.tvCategory = convertView.findViewById(R.id.tvTransCat);
            holder.tvAmount = convertView.findViewById(R.id.tvAmount);
            holder.tvTransIcon = convertView.findViewById(R.id.tvTransIcon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> item = data.get(position);

        holder.tvType.setText(item.get("type"));
        holder.tvDate.setText(item.get("date"));
        holder.tvCategory.setText(item.get("catName"));
        holder.tvAmount.setText(item.get("amount"));
        holder.tvTransIcon.setText(item.get("typeIcon"));

        // Set background based on type
        if ("Expense".equalsIgnoreCase(item.get("type"))) {
            holder.tvType.setBackgroundResource(R.drawable.expense_background);
        } else {
            // Reset background or set income background if needed
            holder.tvType.setBackgroundResource(R.drawable.income_background);
            holder.tvTransIcon.setText("+");
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvType;
        TextView tvDate;
        TextView tvCategory;
        TextView tvAmount;
        TextView tvTransIcon;
    }
}
