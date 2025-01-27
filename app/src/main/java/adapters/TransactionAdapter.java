package adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashMap;

import mdad.localdata.trakit.R;
import mdad.localdata.trakit.transactionfragments.UpdateTransactionFragment;
import mdad.localdata.trakit.transactionfragments.ViewTransactionFragment;

public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> data;
    private FragmentManager fragmentManager;
    private static final String[] from = {"type", "date", "catName", "amount", "typeIcon"};
    private static final int[] to = {R.id.tvTransType, R.id.tvDate, R.id.tvTransCat, R.id.tvAmount, R.id.tvTransIcon};

    public TransactionAdapter(Context context, ArrayList<HashMap<String, String>> data, FragmentManager fragmentManager) {
        this.context = context;
        this.data = data;
        this.fragmentManager = fragmentManager;
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
            holder.btnView = convertView.findViewById(R.id.btnView);
            holder.tvDescription = convertView.findViewById(R.id.tvViewDesc);
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
        holder.tvDescription.setText(item.get("desc"));

        // Set background based on type
        if ("Expense".equalsIgnoreCase(item.get("type"))) {
            holder.tvType.setBackgroundResource(R.drawable.expense_background);
        } else {
            // Reset background or set income background if needed
            holder.tvType.setBackgroundResource(R.drawable.income_background);
            holder.tvTransIcon.setText("+");
        }

        String type = item.get("type");
        String date = item.get("date");
        String catName = item.get("catName");
        String amount = item.get("amount");
        String desc = item.get("desc");
        String base64Image = item.get("image");
        String id = item.get("id");
        String recurring = item.get("recurring");
        Bundle transInfo = new Bundle();
        transInfo.putString("type", type);
        transInfo.putString("date", date);
        transInfo.putString("catName", catName);
        transInfo.putString("amount", amount);
        transInfo.putString("desc", desc);
        transInfo.putString("base64Img", base64Image);
        transInfo.putString("id", id);
        transInfo.putString("recurring", recurring);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment viewTransFragment = new ViewTransactionFragment();
                viewTransFragment.setArguments(transInfo);
                fragmentManager.beginTransaction()
                        .replace(R.id.trans_child_container, viewTransFragment)
                        .commit();
            }
        });

        holder.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment viewTransFragment = new ViewTransactionFragment();
                viewTransFragment.setArguments(transInfo);
                fragmentManager.beginTransaction()
                        .replace(R.id.trans_child_container, viewTransFragment)
                        .commit();
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvType, tvDate, tvCategory, tvAmount, tvTransIcon, tvDescription;
        Button btnView;


    }
}
