package g.android.speakingforyou;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class SavedSentencesFragment extends Fragment implements View.OnClickListener{


    //2 - Declare callback
    private OnButtonClickedListener mCallback;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        public void onButtonClicked(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate the layout of MainFragment
        View rootView =inflater.inflate(R.layout.fragment_saved_sentences, container, false);

        RecyclerView mRecyclerView_SavedSentences = (RecyclerView) rootView.findViewById(R.id.recyclerView_SavedSentences);
        //Button button = rootView.findViewById(R.id.button);
        //button.setOnClickListener(this);

        //Clear all views and populate with fresh values
        mRecyclerView_SavedSentences.setLayoutManager( new LinearLayoutManager(getActivity()));

        final SavedSentencesDAO SSDAO = new SavedSentencesDAO(getActivity());
        final List<SavedSentences> listSavedSentences = SSDAO.getAll();
        final RecyclerView.Adapter mAdapter;


        mAdapter = new SavedSentencesRecyclerAdapter(listSavedSentences, getActivity(), new ClickListener() {
            @Override
            public void onPositionClicked(int position) {
            }



            @Override
            public void onLongClicked(int position) {

            }

            @Override
            public void onItemClick(View view, int position, long id) {
                Log.i("TTS", "onItemClick ID : " + view.getId() + " Position : " + position);
                if(view.getId() == R.id.delete) {
                    Log.i("TTS", "Deleting : " + listSavedSentences.get(position).getSentence() );
                    SSDAO.delete(listSavedSentences.get(position).getId());

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frameLayoutFragment, new SavedSentencesFragment())
                            .commit();
                }
                else if(view.getId() == R.id.sentence) {
                    //Talk
                    view.setTag(listSavedSentences.get(position));
                    mCallback.onButtonClicked(view);
                }
                else{

                    }


            }
        });

        RecyclerView.ItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView_SavedSentences.removeAllViews();
        mRecyclerView_SavedSentences.setAdapter(mAdapter);
        mRecyclerView_SavedSentences.addItemDecoration(divider);

        //Draggable Event
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {

                int positionDragged = dragged.getAdapterPosition();
                int positionTarget = target.getAdapterPosition();

                Log.i("TTS", "Swap : " + positionDragged + " To : " + positionTarget);
                Collections.swap(listSavedSentences, positionDragged, positionTarget);

                mAdapter.notifyItemMoved(positionDragged, positionTarget);

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        });

        helper.attachToRecyclerView(mRecyclerView_SavedSentences);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // 4 - Call the method that creating callback after being attached to parent activity
        this.createCallbackToParentActivity();
    }

    // --------------
    // ACTIONS
    // --------------


    @Override
    public void onClick(View v) {
        // 5 - Spread the click to the parent activity
        mCallback.onButtonClicked(v);
        Log.i("TTS","Fragment Click ID : " + v.getId());
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (OnButtonClickedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener");
        }
    }
}
