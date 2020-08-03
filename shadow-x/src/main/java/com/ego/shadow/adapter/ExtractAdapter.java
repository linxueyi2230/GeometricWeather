package com.ego.shadow.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.ego.shadow.R;
import com.ego.shadow.db.DBHelper;
import com.ego.shadow.entity.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author lxy
 * @time 2020/1/10 10:02
 */
public class ExtractAdapter extends Adapter<Row> {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public ExtractAdapter(Context context, List<Row> datas) {
        super(context, datas);
    }

    @Override
    protected int getItemType(int position, Row data) {
        return 0;
    }

    @Override
    protected int layout(int viewType) {
        return R.layout.shadow_item_extract_record;
    }

    @Override
    protected void bind(EHolder holder, Row data, int position, int viewType) {
        TextView tv_amount = holder.find(R.id.tv_amount);
        TextView tv_timestamp = holder.find(R.id.tv_timestamp);
        TextView tv_status = holder.find(R.id.tv_status);

        tv_amount.setText(String.format("￥%s", data.amount));
        tv_timestamp.setText(format.format(new Date(data.timestamp)));
        if (data.status == 0) {
            tv_status.setText("正在审核");
            tv_status.setTextColor(mContext.getResources().getColor(R.color.gray11));
        } else {
            tv_status.setText("已完成");
            tv_status.setTextColor(mContext.getResources().getColor(R.color.e_main));
        }
    }
}
