package adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import mdad.localdata.trakit.R;
import mdad.localdata.trakit.budgetfragments.UpdateBudgetFragment;
import mdad.localdata.trakit.budgetfragments.ViewBudgetFragment;
import utils.StringUtils;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    private final ArrayList<HashMap<String, String>> budgetList;
    private final Context context;
    private FragmentManager fragmentManager;

    public BudgetAdapter(Context context, ArrayList<HashMap<String, String>> budgetList, FragmentManager fragmentManager) {
        this.context = context;
        this.budgetList = budgetList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.budget_view_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> budget = budgetList.get(position);
        holder.tvName.setText(budget.get("name"));
        holder.tvStartDate.setText(StringUtils.convertDateFormat(budget.get("start_date")));
        holder.tvEndDate.setText(StringUtils.convertDateFormat(budget.get("end_date")));
        holder.tvAmt.setText(budget.get("limit"));
        holder.tvExpense.setText(budget.get("total_spent"));
        Float floatBalance =Float.parseFloat((budget.get("balance")));
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);
        if (floatBalance <0){
            floatBalance = Math.abs(floatBalance);
            holder.tvBalance.setText(df.format(floatBalance));
            holder.tvMinusIcon.setVisibility(View.VISIBLE);
        }
        else{
            holder.tvBalance.setText(df.format(floatBalance));
            holder.tvMinusIcon.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a bundle and put budget data
                Bundle bundle = new Bundle();
                bundle.putString("id", budget.get("id"));
                bundle.putString("name", budget.get("name"));
                bundle.putString("start_date", StringUtils.convertDateFormat(budget.get("start_date")));
                bundle.putString("end_date", StringUtils.convertDateFormat(budget.get("end_date")));
                bundle.putString("limit", budget.get("limit"));
                bundle.putString("total_spent", budget.get("total_spent"));
                bundle.putString("balance", budget.get("balance"));

                // Create fragment and set arguments
                Fragment goToUpdateBudgetFragment = new UpdateBudgetFragment();
                goToUpdateBudgetFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.home_fragment, goToUpdateBudgetFragment)
                        .commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStartDate, tvEndDate,tvAmt, tvExpense, tvBalance, tvMinusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvAmt = itemView.findViewById(R.id.tvAmt);
            tvExpense = itemView.findViewById(R.id.tvExpense);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            tvMinusIcon = itemView.findViewById(R.id.tvMinusIcon);
        }
    }
}
