package com.ego.shadow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.ego.shadow.AdData;
import com.ego.shadow.NativeExpressRecyclerView;
import com.ego.shadow.R;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lxy on 2016/7/5.
 */
public abstract class Adapter<Data extends AdData> extends RecyclerView.Adapter<Adapter.EHolder> {

    protected List<Data> mData;
    protected Context mContext;
    protected Listener<Data> mListener;
    public static final int TYPE_AD = 1024;
    protected NativeExpressRecyclerView mNativeExpressRecyclerView;

    public Adapter(Context context, List<Data> datas){
        this(context,datas,false);
    }

    public Adapter(Context context, List<Data> datas,boolean debug) {
        this.mContext = context;
        if (datas == null) {
            datas = new ArrayList<>();
        }
        this.mData = datas;

        mNativeExpressRecyclerView = new NativeExpressRecyclerView(context) {
            @Override
            public void addAd(int position, NativeExpressADView adView) {
                if (mListener != null) {
                    Data data = mListener.toAdData(position, adView);
                    if (data == null){
                        return;
                    }
                    mData.add(position, data);
                }
            }
        };

        mNativeExpressRecyclerView.debug(debug);

        //如果初始化的时候有数据了，则主动拉取广告
        if (!mData.isEmpty()) {
            mNativeExpressRecyclerView.loadAd(this, mData);
        }
    }

    @Override
    public int getItemViewType(int position){
        Data data = mData.get(position);
        if (data.isAd) {
            return TYPE_AD;
        }

        return getItemType(position,data);
    }

    protected abstract int getItemType(int position,Data data);

    protected abstract int layout(int viewType);

    protected abstract void bind(EHolder holder, Data data, int position, int viewType);

    @Override
    public EHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_AD) {
            return new EHolder(LayoutInflater.from(mContext).inflate(R.layout.shadow_native_express, parent, false));
        }

        int layoutId = layout(viewType);
        return new EHolder(LayoutInflater.from(mContext).inflate(layoutId,parent,false));
    }

    @Override
    public void onBindViewHolder(EHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_AD) {
            Data data = mData.get(position);
            if (mNativeExpressRecyclerView !=null){
                mNativeExpressRecyclerView.onBindAdHolder(holder, data, position);
            }
            return;
        }

        Data data = mData.get(position);
        bind(holder,data,position,viewType);
        bindingListener(holder.itemView,data,position);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public List<Data> get(){
        return mData;
    }

    public void set(List<Data> data){
        this.mData = data;
    }

    public Data item(int position){
        return mData.get(position);
    }

    public void remove(Data data){
        if (mData !=null && data !=null) {
            int position = mData.indexOf(data);
            if(position == -1) {
                return;
            }
            remove(position);
        }
    }

    public void remove(int position){

        if (mData !=null) {
            if(position <0 || position >= mData.size()) {
                return;
            }

            mData.remove(position);
            notifyItemRemoved(position);
            if (position != mData.size()){
                notifyItemRangeChanged(position, mData.size()-position);
            }
        }
    }

    public void refresh(List<Data> list) {
        mData.clear();
        if (list != null && !list.isEmpty()) {
            mData.addAll(list);
        }
        notifyDataSetChanged();

        this.loadAd();
    }

    public void append(Data data) {
        if (mData != null && data != null) {
            mData.add(data);
            notifyItemInserted(mData.size() - 1);
        }
    }

    public void append(List<Data> list) {
        if (list != null && !list.isEmpty()) {
//            int start = mData.size() - 1;
//            int itemCount = list.size();
//            mData.addAll(list);
//            notifyItemRangeInserted(start, itemCount);
            mData.addAll(list);
            if (mNativeExpressRecyclerView != null) {
                mNativeExpressRecyclerView.append();
            }
        }
    }

    public boolean isEmpty() {
        if (mData == null) {
            return true;
        }
        return mData.isEmpty();
    }

    public void clear() {
        if (mData != null) {
            mData.clear();
            notifyDataSetChanged();
        }
    }

    public void loadAd() {
        if (mNativeExpressRecyclerView != null) {
            mNativeExpressRecyclerView.loadAd(this, mData);
        }
    }

    public void destroy() {
        if (mNativeExpressRecyclerView != null) {
            mNativeExpressRecyclerView.destroy();
        }
    }

    public NativeExpressRecyclerView getNativeExpressRecyclerView() {
        return mNativeExpressRecyclerView;
    }

    public void setNativeExpressRecyclerView(NativeExpressRecyclerView nativeExpressRecyclerView) {
        this.mNativeExpressRecyclerView = nativeExpressRecyclerView;
    }

    public void setListener(Listener<Data> listener) {
        this.mListener = listener;
    }

    private void bindingListener(final View view, final Data data, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.click(view, data, position);
                }
            }
        });
    }

    public static class EHolder extends RecyclerView.ViewHolder{

        public EHolder(View itemView) {
            super(itemView);
        }

        public <V extends View> V find(int id){
            return itemView.findViewById(id);
        }
    }

    public interface Listener<Data>{
        void click(View view, Data data, int position);
        Data toAdData(int position, NativeExpressADView adView);
    }
}
