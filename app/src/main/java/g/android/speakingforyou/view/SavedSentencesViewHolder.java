package g.android.speakingforyou.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import g.android.speakingforyou.R;
import g.android.speakingforyou.controller.SavedSentencesAdapter;
import g.android.speakingforyou.model.SavedSentences;

public class SavedSentencesViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "SFY : SavedSViewHolder";
    private ConstraintLayout mConstraintLayout;
    private TextView sentence;
    private ImageButton mImageButton_more;
    private ImageButton mImageButton_Delete;

    private WeakReference<ClickListener> listenerRef;
    private LinearLayout mLinearLayout_PopUp;
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
        mImageButton_more = itemView.findViewById(R.id.imageButton_SavedSentenceCell_more);
        mImageButton_Delete = itemView.findViewById(R.id.imageButton_SavedSentenceCell_Delete);
        mLinearLayout_PopUp = itemView.findViewById(R.id.linearLayout_SavedSentenceCell_PopUp);
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
                //Send the longClickEvent then check the box
                if(!showSelectedRadioButtonVisible) {
                    listenerRef.get().onLongClick(view, getAdapterPosition(), view.getId());
                    mCheckBox_SelectedCell.setChecked( !mCheckBox_SelectedCell.isChecked());
                    if (mCheckBox_SelectedCell.isChecked()) {
                        onItemClick.onItemCheck(currentItem);
                    } else {
                        onItemClick.onItemUncheck(currentItem);
                    }
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
                return true;
            }
        });

        mImageButton_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"SAVED SENTENCE CELL MORE ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
                toggleVisibility();
            }
        });

        mImageButton_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"SAVED SENTENCE CELL DELETE ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }

    public void setVisibility(int visibility){

        mLinearLayout_PopUp.setVisibility(visibility);

        if(visibility == View.INVISIBLE)
            show = false;
        else
            show = true;
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

    private void toggleVisibility(){
        show = !show;
        if(show){
            mLinearLayout_PopUp.setVisibility(View.VISIBLE);
        }
        else{
            mLinearLayout_PopUp.setVisibility(View.INVISIBLE);
        }
    }

}
