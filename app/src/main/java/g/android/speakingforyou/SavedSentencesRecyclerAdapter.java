package g.android.speakingforyou;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

public class SavedSentencesRecyclerAdapter extends RecyclerView.Adapter<SavedSentencesRecyclerAdapter.MyViewHolder> {

    private final ClickListener listener;
    private final List<SavedSentences> mSavedSentencesList;
    private final Context mContext;

    public SavedSentencesRecyclerAdapter(List<SavedSentences> savedSentences, Context context, ClickListener listener)
    {
        this.listener = listener;
        mSavedSentencesList = savedSentences;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mSavedSentencesList.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.saved_sentence_cell, parent, false);
        return new MyViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SavedSentences savedSentence = mSavedSentencesList.get(position);

        String sentence = savedSentence.getSentence();
        String sentenceId = String.valueOf(savedSentence.getId());

        holder.sentence.setText(sentence);
        holder.sentenceId.setText(sentenceId);
        holder.savedSentence = savedSentence;
        holder.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView sentence;
        private final TextView sentenceId;
        private final ImageView delete;


        private WeakReference<ClickListener> listenerRef;

        Context mContext;
        SavedSentences savedSentence;

        public MyViewHolder(final View itemView, ClickListener listener) {
            super(itemView);

            listenerRef = new WeakReference<>(listener);
            sentence = ((TextView) itemView.findViewById(R.id.sentence));
            sentenceId = ((TextView) itemView.findViewById(R.id.sentenceId));
            delete = ((ImageView) itemView.findViewById(R.id.delete));


            delete.setOnClickListener(this);
            sentence.setOnClickListener(this);
            itemView.setOnClickListener(this);
            Log.i("TTS","itemView ID : " + itemView.getId());


            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Speaker speaker = new Speaker(mContext, savedSentence);
                    Log.i("TTS", "Saved Sentence Clicked : " + savedSentence.getSentence());
                }
            });
            */

        }
        // onClick Listener for view
        @Override
        public void onClick(View v) {

            if (v.getId() == delete.getId()) {
                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }

            listenerRef.get().onPositionClicked(getAdapterPosition());
            listenerRef.get().onItemClick(v, getAdapterPosition(), v.getId());
        }
    }




}