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

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    public void setTextSentence(String sentence) {
        this.sentence.setText(sentence);
    }

    private static final String LOG_TAG = "SFY : HistoryViewHolder";
    private ConstraintLayout mConstraintLayout;
    TextView sentence;
    private ImageButton mImageButton_more;
    private ImageButton mImageButton_Delete;
    private ImageButton mImageButton_AddToSaved;
    private WeakReference<ClickListener> listenerRef;
    private LinearLayout mLinearLayout_PopUp;
    boolean show;


    public HistoryViewHolder(View itemView, ClickListener listener) {
        super(itemView);

        listenerRef = new WeakReference<>(listener);

        mConstraintLayout =         itemView.findViewById(R.id.constraintLayout_HistoryCell);
        sentence =                  itemView.findViewById(R.id.textView_HistoryCell_Sentence);
        mImageButton_more =         itemView.findViewById(R.id.imageButton_HistoryCell_More);
        mImageButton_Delete =       itemView.findViewById(R.id.imageButton_HistoryCell_Delete);
        mImageButton_AddToSaved =   itemView.findViewById(R.id.imageButton_HistoryCell_AddToSaved);
        mLinearLayout_PopUp =       itemView.findViewById(R.id.linearLayout_HistoryCell_PopUp);

        show = false;

        mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(LOG_TAG,"History ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

        mImageButton_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"HISTORY CELL MORE ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
                toggleVisibility();
            }
        });

        mImageButton_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"HISTORY CELL DELETE ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

        mImageButton_AddToSaved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(LOG_TAG,"HISTORY CELL AddToSaved ID : " + view.getId());
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

    private void toggleVisibility(){
        show = !show;
        if(show){
            mLinearLayout_PopUp.setVisibility(View.VISIBLE);
        }
        else
            mLinearLayout_PopUp.setVisibility(View.INVISIBLE);
    }
}
