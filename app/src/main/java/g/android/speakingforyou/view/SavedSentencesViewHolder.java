package g.android.speakingforyou.view;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import g.android.speakingforyou.R;

public class SavedSentencesViewHolder extends RecyclerView.ViewHolder {

    private static final String LOG_TAG = "SFY : SavedSViewHolder";
    private ConstraintLayout mConstraintLayout;
    private TextView sentence;
    private ImageButton mImageButton_more;
    private ImageButton mImageButton_Delete;
    private WeakReference<ClickListener> listenerRef;
    private LinearLayout mLinearLayout_PopUp;
    boolean show;

    public SavedSentencesViewHolder(final View itemView, ClickListener listener) {
        super(itemView);

        //Used to do get a callback in the Fragment
        listenerRef = new WeakReference<>(listener);

        mConstraintLayout = itemView.findViewById(R.id.constraintLayout_SavedSentenceCell);
        sentence = itemView.findViewById(R.id.textView_SavedSentenceCell_Sentence);
        mImageButton_more = itemView.findViewById(R.id.imageButton_SavedSentenceCell_more);
        mImageButton_Delete = itemView.findViewById(R.id.imageButton_SavedSentenceCell_Delete);
        mLinearLayout_PopUp = itemView.findViewById(R.id.linearLayout_SavedSentenceCell_PopUp);

        show = false;

        mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"SAVED SENTENCE CLICK ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
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

    public void setVisibility(int visibility){

        mLinearLayout_PopUp.setVisibility(visibility);
        if(visibility == View.INVISIBLE)
            show = false;
        else
            show = true;
    }

    public void setTextSentence(String sentence) {
        this.sentence.setText(sentence);
    }

    private void toggleVisibility(){
        show = !show;
        if(show){
            mLinearLayout_PopUp.setVisibility(View.VISIBLE);
        }
        else
            mLinearLayout_PopUp.setVisibility(View.INVISIBLE);
    }

}
