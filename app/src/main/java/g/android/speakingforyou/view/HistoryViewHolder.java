package g.android.speakingforyou.view;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import g.android.speakingforyou.R;
import g.android.speakingforyou.controller.HistoryAdapter;
import g.android.speakingforyou.model.History;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    public void setTextSentence(String sentence) {
        this.sentence.setText(sentence);
    }

    public void setTextUsage(int usage) {
        String usageTxt = "(" + usage + ")";
        this.usage.setText(usageTxt);
    }

    private static final String LOG_TAG = "SFY : HistoryViewHolder";
    private ConstraintLayout mConstraintLayout;
    TextView sentence;
    TextView usage;
    private ImageView mImageView_Star;
    private WeakReference<ClickListener> listenerRef;
    boolean show;
    boolean showSelectedRadioButtonVisible;

    History currentItem;
    public CheckBox mCheckBox_SelectedCell;
    View itemView;
    private HistoryAdapter.OnItemCheckListener onItemClick;



    public HistoryViewHolder(View itemView, ClickListener listener, @NonNull HistoryAdapter.OnItemCheckListener onItemCheckListener) {
        super(itemView);
        this.itemView = itemView;
        this.onItemClick = onItemCheckListener;

        listenerRef = new WeakReference<>(listener);

        mConstraintLayout =         itemView.findViewById(R.id.constraintLayout_HistoryCell);
        sentence =                  itemView.findViewById(R.id.textView_HistoryCell_Sentence);
        usage =                     itemView.findViewById(R.id.textView_HistoryCell_Usage);
        mImageView_Star =           itemView.findViewById(R.id.imageView_HistoryCell_Star);
        mCheckBox_SelectedCell =    itemView.findViewById(R.id.checkBox_HistoryCell_SelectItem);
        mCheckBox_SelectedCell.setClickable(false);

        show = false;
        setVisibilitySelectRadioButton(View.GONE);

        mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(LOG_TAG,"History ID : " + view.getId());
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
                Log.i(LOG_TAG,"HISTORY LONG CLICK ID : " + view.getId());
                //If the checkbox are not visible
                //Send the longClickEvent to the fragment
                if(!showSelectedRadioButtonVisible) {
                    listenerRef.get().onLongClick(view, getAdapterPosition(), view.getId());
                }
                return true;
            }
        });
    }

    public void showStar(boolean isShown){
        if(isShown)
            mImageView_Star.setVisibility(View.VISIBLE);
        else
            mImageView_Star.setVisibility(View.INVISIBLE);

    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        itemView.setOnClickListener(onClickListener);
    }

    public void setVisibilitySelectRadioButton(int visibility){

        mCheckBox_SelectedCell.setVisibility(visibility);

        showSelectedRadioButtonVisible = visibility != View.GONE;
    }

    public void setCurrentItem(History currentItem){
        this.currentItem = currentItem;
    }
}
