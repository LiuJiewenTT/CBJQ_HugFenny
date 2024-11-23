package io.github.liujiewentt.hugfenny;

import android.graphics.drawable.Drawable;

public class AppRecyclerViewItem {
    public String packageName;
    public String remark;
    public Drawable icon;
    public Integer xValue;

    // 构造方法、getter、setter
    public AppRecyclerViewItem(String packageName, String remark, Drawable icon, Integer xValue) {
        this.packageName = packageName;
        this.remark = remark;
        this.icon = icon;
        this.xValue = xValue;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getRemark() {
        return remark;
    }

    public Drawable getIcon() {
        return icon;
    }

    public Integer getXValue() {
        return xValue;
    }

    public void toggleXValue() {
        this.xValue = getOpposingXValue();
    }

    public Integer getOpposingXValue() {
        return (this.xValue == 0 ? 1 : 0);
    }
}
