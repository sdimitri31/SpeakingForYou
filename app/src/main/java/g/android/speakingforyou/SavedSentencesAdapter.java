package g.android.speakingforyou;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


public class SavedSentencesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndDragHelper.ActionCompletionContract {
    private ItemTouchHelper touchHelper;
    private List<SavedSentences> mSavedSentencesList;

    private ClickListener listener;
    private Boolean mIsEditSavedSentences;
    private Context appContext;


    public SavedSentencesAdapter(Context context, ClickListener listener, Boolean isEditSavedSentences){
        this.listener = listener;
        this.mIsEditSavedSentences = isEditSavedSentences;
        this.appContext = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.saved_sentence_cell, parent, false);

        return new SavedSentencesViewHolder(view, listener, mIsEditSavedSentences);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        ((SavedSentencesViewHolder) holder).sentence.setText(mSavedSentencesList.get(position).getSentence());

        ((SavedSentencesViewHolder) holder).reorderView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSavedSentencesList == null ? 0 : mSavedSentencesList.size();
    }

    public void setSavedSentencesList(List<SavedSentences> savedSentencesList) {
        this.mSavedSentencesList = savedSentencesList;
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
        //On drop update database
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