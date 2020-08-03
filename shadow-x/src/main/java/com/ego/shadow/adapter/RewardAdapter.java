package com.ego.shadow.adapter;

import android.content.Context;
import android.widget.TextView;

import com.ego.shadow.R;
import com.ego.shadow.db.DBHelper;
import com.ego.shadow.entity.RewardRow;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 奖励明细
 * @author lxy
 * @time 2020/1/10 10:01
 */
public class RewardAdapter extends Adapter<RewardRow> {

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public RewardAdapter(Context context, List<RewardRow> datas, boolean debug) {
        super(context, datas, debug);
    }

    public RewardAdapter(Context context, List<RewardRow> datas) {
        super(context, datas);
    }

    @Override
    protected int getItemType(int position, RewardRow data) {
        return 0;
    }

    @Override
    protected int layout(int viewType) {
        return R.layout.shadow_item_reward_detail;
    }

    @Override
    protected void bind(EHolder holder, RewardRow data, int position, int viewType) {
        TextView tv_amount = holder.find(R.id.tv_amount);
        TextView tv_timestamp = holder.find(R.id.tv_timestamp);
        TextView tv_ad_type = holder.find(R.id.tv_ad_type);

        tv_amount.setText(String.format("￥%s", data.amount));
        tv_timestamp.setText(format.format(new Date(data.timestamp)));
        tv_ad_type.setText(DBHelper.with(mContext).ad(data.ad_type));
    }
}
