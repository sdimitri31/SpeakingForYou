package g.android.speakingforyou.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.view.ClickListener;
import g.android.speakingforyou.view.HistoryViewHolder;
import g.android.speakingforyou.view.SwipeAndDragHelper;


public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {

    private ItemTouchHelper touchHelper;
    private List<History> mHistoryList;

    private ClickListener listener;
    private Context appContext;

    private boolean isVisible;

    public HistoryAdapter(Context context, ClickListener listener){
        this.listener = listener;
        this.appContext = context;
        this.isVisible = false;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_cell, parent, false);

        return new HistoryViewHolder(view, listener);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((HistoryViewHolder) holder).setTextSentence(mHistoryList.get(position).getSentence());
        ((HistoryViewHolder) holder).setTextDateFormat(mHistoryList.get(position).getStringDate());
        ((HistoryViewHolder) holder).setTextUsage(mHistoryList.get(position).getUsage());
        if (isVisible){
            ((HistoryViewHolder) holder).setVisibility(View.VISIBLE);
        }else{
            ((HistoryViewHolder) holder).setVisibility(View.INVISIBLE);
        }
    }

    public void updateVisibility(boolean newValue){
        isVisible= newValue;
    }

    @Override
    public int getItemCount() {
        return mHistoryList == null ? 0 : mHistoryList.size();
    }

    public void setHistoryList(List<History> historyList) {
        this.mHistoryList = historyList;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        History targetUser = mHistoryList.get(oldPosition);
        mHistoryList.remove(oldPosition);
        mHistoryList.add(newPosition, targetUser);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //On drop update database
        HistoryDAO historyDAO = new HistoryDAO(appContext);
        historyDAO.refreshDatabase(mHistoryList);
    }

    @Override
    public void onViewSwiped(int position) {
        mHistoryList.remove(position);
        notifyItemRemoved(position);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {

        this.touchHelper = touchHelper;
    }
}