package io.github.liujiewentt.hugfenny;

import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import io.github.liujiewentt.hugfenny.AppRecyclerViewItem;
import io.github.liujiewentt.hugfenny.Common;


public class AppRecyclerViewAdapter extends RecyclerView.Adapter<AppRecyclerViewAdapter.MyViewHolder> {
    private static final String TAG = "AppRecyclerViewAdapter";
    private List<AppRecyclerViewItem> itemList;

    public AppRecyclerViewAdapter(List<AppRecyclerViewItem> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AppRecyclerViewItem item = itemList.get(position);

        // 绑定数据
        holder.packageNameTextView.setText(item.getPackageName());
        holder.remarkTextView.setText(item.getRemark());
        holder.iconImageView.setImageDrawable(item.getIcon());
        holder.xValueTextView.setText(item.getXValue().toString());

        // 设置按钮点击事件，切换 X 值
        holder.xValueButton.setOnClickListener(v -> {
            Integer newXValue = item.getOpposingXValue();
            String dirPath = String.join("/", Common.dataDirectoryPath, item.packageName);

            if ( Common.updateLocalizationFile(dirPath, newXValue) ) {
                item.toggleXValue();
                Common.localizationValues.put(item.packageName, newXValue);
                notifyItemChanged(position); // 通知数据已更新，刷新视图
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView packageNameTextView;
        TextView remarkTextView;
        ImageView iconImageView;
        TextView xValueTextView;
        Button xValueButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            packageNameTextView = itemView.findViewById(R.id.app_name);
            remarkTextView = itemView.findViewById(R.id.app_remark);
            iconImageView = itemView.findViewById(R.id.app_icon);
            xValueTextView = itemView.findViewById(R.id.x_value);
            xValueButton = itemView.findViewById(R.id.toggle_button);
        }
    }
}
