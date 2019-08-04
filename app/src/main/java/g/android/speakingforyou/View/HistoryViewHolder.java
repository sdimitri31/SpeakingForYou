package g.android.speakingforyou.View;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import g.android.speakingforyou.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    public void setTextSentence(String sentence) {
        this.sentence.setText(sentence);
    }


    TextView sentence;
    private WeakReference<ClickListener> listenerRef;


    public HistoryViewHolder(View itemView, ClickListener listener) {
        super(itemView);

        listenerRef = new WeakReference<>(listener);
        sentence = ((TextView) itemView.findViewById(R.id.sentence_history_cell));


        sentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("TTS","SENTENCE CLICK ID : " + view.getId());
                listenerRef.get().onItemClick(view, getAdapterPosition(), view.getId());
            }
        });

    }


}
