package g.android.speakingforyou;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class SavedSentencesViewHolder extends RecyclerView.ViewHolder {

    TextView sentence;
    TextView sentenceId;
    ImageView delete;
    private WeakReference<ClickListener> listenerRef;
    ImageView reorderView;

    public SavedSentencesViewHolder(View itemView, ClickListener listener, Boolean isEditSavedSentences) {
        super(itemView);

        listenerRef = new WeakReference<>(listener);
        sentence = ((TextView) itemView.findViewById(R.id.sentence));
        sentenceId = ((TextView) itemView.findViewById(R.id.sentenceId));
        delete = ((ImageView) itemView.findViewById(R.id.delete));
        reorderView = ((ImageView) itemView.findViewById(R.id.imageView_Reorder));

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

        sentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("TTS","SENTENCE CLICK ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

        if(isEditSavedSentences != null){
            if(isEditSavedSentences)
            {
                delete.setVisibility(View.VISIBLE);
                reorderView.setVisibility(View.VISIBLE);
            }
            else
            {
                delete.setVisibility(View.GONE);
                reorderView.setVisibility(View.GONE);

            }
        }

    }


}
