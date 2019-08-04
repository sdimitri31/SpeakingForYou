package g.android.speakingforyou.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.Model.HistoryDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.View.ClickListener;
import g.android.speakingforyou.View.HistoryViewHolder;
import g.android.speakingforyou.View.SwipeAndDragHelper;


public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {
    private ItemTouchHelper touchHelper;
    private List<String> mHistoryList;

    private ClickListener listener;
    private Context appContext;


    public HistoryAdapter(Context context, ClickListener listener){
        this.listener = listener;
        this.appContext = context;
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

        ((HistoryViewHolder) holder).setTextSentence(mHistoryList.get(position));
    }

    @Override
    public int getItemCount() {
        return mHistoryList == null ? 0 : mHistoryList.size();
    }

    public void setHistoryList(List<String> historyList) {
        this.mHistoryList = historyList;
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        String targetUser = mHistoryList.get(oldPosition);
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