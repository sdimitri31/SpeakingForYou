package g.android.speakingforyou.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import g.android.speakingforyou.R;
import g.android.speakingforyou.controller.SavedSentencesAdapter;
import g.android.speakingforyou.model.SavedSentences;

public class SavedSentencesViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "SFY : SavedSViewHolder";
    private ConstraintLayout mConstraintLayout;
    private TextView sentence;

    private WeakReference<ClickListener> listenerRef;
    boolean show;
    boolean showSelectedRadioButtonVisible;

    SavedSentences currentItem;

    public CheckBox mCheckBox_SelectedCell;
    View itemView;
    private SavedSentencesAdapter.OnItemCheckListener onItemClick;

    public SavedSentencesViewHolder(final View itemView, ClickListener listener, @NonNull SavedSentencesAdapter.OnItemCheckListener onItemCheckListener) {
        super(itemView);
        this.itemView = itemView;
        this.onItemClick = onItemCheckListener;

        //Used to do get a callback in the Fragment
        listenerRef = new WeakReference<>(listener);

        mConstraintLayout = itemView.findViewById(R.id.constraintLayout_SavedSentenceCell);
        sentence = itemView.findViewById(R.id.textView_SavedSentenceCell_Sentence);
        mCheckBox_SelectedCell = itemView.findViewById(R.id.checkBox_SavedSentenceCell_SelectItem);
        mCheckBox_SelectedCell.setClickable(false);

        show = false;
        setVisibilitySelectRadioButton(View.GONE);

        mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"SAVED SENTENCE CLICK ID : " + view.getId());
                //If the checkbox are not visible
                //Return the sentence to speak
                if(!showSelectedRadioButtonVisible) {
                    listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
                }
                //Select the item
                else{
                    mCheckBox_SelectedCell.setChecked( !mCheckBox_SelectedCell.isChecked());
                    if (mCheckBox_SelectedCell.isChecked()) {
                        onItemClick.onItemCheck(currentItem);
                    } else {
                        onItemClick.onItemUncheck(currentItem);
                    }
                }
            }
        });

        mConstraintLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i(LOG_TAG,"SAVED SENTENCE LONG CLICK ID : " + view.getId());
                //If the checkbox are not visible
                //Send the longClickEvent to the fragment
                if(!showSelectedRadioButtonVisible) {
                    listenerRef.get().onLongClick(view, getAdapterPosition(), view.getId());
                }
                return true;
            }
        });

    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }

    public void setVisibilitySelectRadioButton(int visibility){

        mCheckBox_SelectedCell.setVisibility(visibility);

        showSelectedRadioButtonVisible = visibility != View.GONE;
    }

    public void setTextSentence(String sentence) {
        this.sentence.setText(sentence);
    }

    public void setCurrentItem(SavedSentences currentItem){
        this.currentItem = currentItem;
    }

}
