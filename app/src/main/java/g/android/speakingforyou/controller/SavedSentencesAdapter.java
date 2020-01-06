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

import g.android.speakingforyou.view.ClickListener;
import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.view.SavedSentencesViewHolder;
import g.android.speakingforyou.view.SwipeAndDragHelper;


public class SavedSentencesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {

    private static final String LOG_TAG = "SFY : SavedSAdapter";
    /*
    *
    * Checkbox listener
    *
    * */
    public interface OnItemCheckListener {
        void onItemCheck(SavedSentences item);
        void onItemUncheck(SavedSentences item);
    }

    @NonNull
    private OnItemCheckListener onItemCheckListener;
    private List<SavedSentences> items;
    private OnItemCheckListener onItemClick;


    private ItemTouchHelper touchHelper;
    private List<SavedSentences> mSavedSentencesList;
    private List<SavedSentences> mSelectedSavedSentencesList;

    private ClickListener listener;
    private Context appContext;

    private boolean isSelectedRadioButtonVisible;


    public SavedSentencesAdapter(Context context, ClickListener listener, List<SavedSentences> items, @NonNull OnItemCheckListener onItemCheckListener){
        this.listener = listener;
        this.appContext = context;
        this.isSelectedRadioButtonVisible = false;
        this.items = items;
        this.onItemClick = onItemCheckListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.saved_sentence_cell, parent, false);

        return new SavedSentencesViewHolder(view, listener, onItemClick);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((SavedSentencesViewHolder) holder).setTextSentence(mSavedSentencesList.get(position).getSentence());
        ((SavedSentencesViewHolder) holder).setCurrentItem(mSavedSentencesList.get(position));

        if (isSelectedRadioButtonVisible){
            ((SavedSentencesViewHolder) holder).setVisibilitySelectRadioButton(View.VISIBLE);
        }
        else{
            ((SavedSentencesViewHolder) holder).setVisibilitySelectRadioButton(View.GONE);
        }

        //Look if the SavedSentences is selected and check it if necessary
        Log.i(LOG_TAG,"     Look if the SavedSentences is selected");
        if( mSelectedSavedSentencesList.contains(mSavedSentencesList.get(position))){
            ((SavedSentencesViewHolder) holder).mCheckBox_SelectedCell.setChecked(true);
        }
        else {
            ((SavedSentencesViewHolder) holder).mCheckBox_SelectedCell.setChecked(false);
        }
        if (holder instanceof SavedSentencesViewHolder) {
            final SavedSentences currentItem = mSavedSentencesList.get(position);


            ((SavedSentencesViewHolder) holder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SavedSentencesViewHolder) holder).mCheckBox_SelectedCell.setChecked(
                            !((SavedSentencesViewHolder) holder).mCheckBox_SelectedCell.isChecked());
                    if (((SavedSentencesViewHolder) holder).mCheckBox_SelectedCell.isChecked()) {
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

    public void setSelectedSavedSentencesList(List<SavedSentences> selectedSavedSentencesList){
        this.mSelectedSavedSentencesList = selectedSavedSentencesList;

    }

    @Override
    public int getItemCount() {
        return mSavedSentencesList == null ? 0 : mSavedSentencesList.size();
    }

    public void setSavedSentencesList(List<SavedSentences> savedSentencesList) {
        this.mSavedSentencesList = savedSentencesList;
        setVisibilitySelectRadioButton(false);
        notifyDataSetChanged();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        SavedSentences targetUser = mSavedSentencesList.get(oldPosition);
        SavedSentences user = new SavedSentences(targetUser);
        mSavedSentencesList.remove(oldPosition);
        mSavedSentencesList.add(newPosition, user);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //On Swipe or move item
        SavedSentencesDAO sentencesDAO = new SavedSentencesDAO(appContext);
        sentencesDAO.refreshDatabase(mSavedSentencesList);
    }

    @Override
    public void onViewSwiped(int position) {
        mSavedSentencesList.remove(position);
        notifyItemRemoved(position);
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

}