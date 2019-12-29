package g.android.speakingforyou.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.view.ClickListener;
import g.android.speakingforyou.view.HistoryViewHolder;
import g.android.speakingforyou.view.SwipeAndDragHelper;


public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {


    /*
     *
     * Checkbox listener
     *
     * */
    public interface OnItemCheckListener {
        void onItemCheck(History item);
        void onItemUncheck(History item);
    }

    private OnItemCheckListener onItemClick;
    private List<History> items;

    private ItemTouchHelper touchHelper;
    private List<History> mHistoryList;

    private ClickListener listener;
    private Context appContext;

    private boolean isVisible;
    private boolean isSelectedRadioButtonVisible;


    public HistoryAdapter(Context context, ClickListener listener, List<History> items, @NonNull OnItemCheckListener onItemCheckListener){
        this.listener = listener;
        this.appContext = context;
        this.isVisible = false;
        this.isSelectedRadioButtonVisible = false;
        this.items = items;
        this.onItemClick = onItemCheckListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_cell, parent, false);

        return new HistoryViewHolder(view, listener, onItemClick);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((HistoryViewHolder) holder).setTextSentence(mHistoryList.get(position).getSentence());
        ((HistoryViewHolder) holder).setTextDateFormat(mHistoryList.get(position).getStringDate());
        ((HistoryViewHolder) holder).setTextUsage(mHistoryList.get(position).getUsage());
        ((HistoryViewHolder) holder).setCurrentItem(mHistoryList.get(position));

        SavedSentencesDAO ssDAO = new SavedSentencesDAO(appContext);

        if(MainActivity.findSavedSentenceUsingEnhancedForLoop(mHistoryList.get(position).getSentence(), ssDAO.getAll()) != null)
            ((HistoryViewHolder) holder).showStar(true);
        else
            ((HistoryViewHolder) holder).showStar(false);


        if (isVisible){
            ((HistoryViewHolder) holder).setVisibility(View.VISIBLE);
        }else{
            ((HistoryViewHolder) holder).setVisibility(View.INVISIBLE);
        }


        if (isSelectedRadioButtonVisible){
            ((HistoryViewHolder) holder).setVisibilitySelectRadioButton(View.VISIBLE);
        }
        else{
            ((HistoryViewHolder) holder).setVisibilitySelectRadioButton(View.GONE);
        }

        if (holder instanceof HistoryViewHolder) {
            final History currentItem = mHistoryList.get(position);


            ((HistoryViewHolder) holder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HistoryViewHolder) holder).mCheckBox_SelectedCell.setChecked(
                            !((HistoryViewHolder) holder).mCheckBox_SelectedCell.isChecked());
                    if (((HistoryViewHolder) holder).mCheckBox_SelectedCell.isChecked()) {
                        onItemClick.onItemCheck(currentItem);
                    } else {
                        onItemClick.onItemUncheck(currentItem);
                    }
                }
            });
        }

    }

    public void updateVisibility(boolean newValue){
        isVisible= newValue;
    }


    public void setVisibilitySelectRadioButton(boolean isVisible){
        isSelectedRadioButtonVisible = isVisible;
    }


    @Override
    public int getItemCount() {
        return mHistoryList == null ? 0 : mHistoryList.size();
    }

    public void setHistoryList(List<History> historyList) {
        this.mHistoryList = historyList;
        setVisibilitySelectRadioButton(false);
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