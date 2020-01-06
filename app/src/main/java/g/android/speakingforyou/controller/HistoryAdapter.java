package g.android.speakingforyou.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.view.ClickListener;
import g.android.speakingforyou.view.HistoryViewHolder;
import g.android.speakingforyou.view.SwipeAndDragHelper;


public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {

    private static final String LOG_TAG = "SFY : HistoryAdapter";

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
    private List<SavedSentences> mSavedSentencesList;
    private List<History> mSelectedHistoryList;

    private ClickListener listener;
    private Context appContext;

    private boolean isSelectedRadioButtonVisible;


    public HistoryAdapter(Context context, ClickListener listener, List<History> items, @NonNull OnItemCheckListener onItemCheckListener){
        this.listener = listener;
        this.appContext = context;
        this.isSelectedRadioButtonVisible = false;
        this.items = items;
        this.onItemClick = onItemCheckListener;
        Log.i(LOG_TAG,"HistoryAdapter Instantiation ");
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.i(LOG_TAG,"HistoryAdapter onCreateViewHolder ");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.history_cell, parent, false);

        return new HistoryViewHolder(view, listener, onItemClick);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        Log.i(LOG_TAG,"HistoryAdapter onBindViewHolder position : " + position);
        //Initialize the SavedSentencesList for the first element in the loop to avoid looping multiple times
        if(position == 0){
            Log.i(LOG_TAG,"     Initialize the SavedSentencesList");
            SavedSentencesDAO ssDAO = new SavedSentencesDAO(appContext);
            mSavedSentencesList = ssDAO.getAll();
        }

        Log.i(LOG_TAG,"     Check if the history is also in the savedSentences");
        //Check if the history is also in the savedSentences
        SavedSentences tmp = null;
        tmp = MainActivity.findSavedSentenceUsingEnhancedForLoop(mHistoryList.get(position).getSentence(), mSavedSentencesList);

        if( tmp != null){
            ((HistoryViewHolder) holder).showStar(true);
            //Remove the found element from the list to avoid double checking
            mSavedSentencesList.remove(tmp);
        }
        else {
            ((HistoryViewHolder) holder).showStar(false);
        }

        ((HistoryViewHolder) holder).setTextSentence(mHistoryList.get(position).getSentence());
        ((HistoryViewHolder) holder).setTextUsage(mHistoryList.get(position).getUsage());
        ((HistoryViewHolder) holder).setCurrentItem(mHistoryList.get(position));

        if (isSelectedRadioButtonVisible){
            ((HistoryViewHolder) holder).setVisibilitySelectRadioButton(View.VISIBLE);
        }
        else{
            ((HistoryViewHolder) holder).setVisibilitySelectRadioButton(View.GONE);
        }

        //Look if the history is selected and check it if necessary
        Log.i(LOG_TAG,"     Look if the history is selected");
        if( mSelectedHistoryList.contains(mHistoryList.get(position))){
            ((HistoryViewHolder) holder).mCheckBox_SelectedCell.setChecked(true);
        }
        else {
            ((HistoryViewHolder) holder).mCheckBox_SelectedCell.setChecked(false);
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

    public void setVisibilitySelectRadioButton(boolean isVisible){
        isSelectedRadioButtonVisible = isVisible;
    }

    public void setSelectedHistoryList(List<History> selectedHistoryList){
        this.mSelectedHistoryList = selectedHistoryList;

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