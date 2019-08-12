package g.android.speakingforyou.View;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.Model.SavedSentences;
import g.android.speakingforyou.Controller.SavedSentencesAdapter;
import g.android.speakingforyou.Model.SavedSentencesDAO;
import g.android.speakingforyou.R;

public class SavedSentencesFragment extends Fragment implements View.OnClickListener{


    //2 - Declare callback
    private OnButtonClickedListener mCallback;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout of MainFragment
        View rootView = inflater.inflate(R.layout.fragment_saved_sentences, container, false);

        //Setup the recycler for the saved sentences
        RecyclerView savedSentencesRecyclerView = rootView.findViewById(R.id.recyclerView_SavedSentences);

        final SavedSentencesDAO savedSentencesDAO = new SavedSentencesDAO(getActivity());
        final List<SavedSentences> listSavedSentences = savedSentencesDAO.getAll();

        ClickListener listener = new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onLongClicked(int position) {

            }

            @Override
            public void onItemClick(View view, int position, long id) {
                Log.i("TTS", "onItemClick ID : " + view.getId() + " Position : " + position);
                if(view.getId() == R.id.sentence_saved_cell) {
                    //Talk
                    view.setTag(listSavedSentences.get(position));
                    mCallback.onButtonClicked(view);
                }
            }
        };


        //RecyclerView.ItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);

        savedSentencesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SavedSentencesAdapter adapter = new SavedSentencesAdapter(getActivity(), listener);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        adapter.setTouchHelper(touchHelper);
        //savedSentencesRecyclerView.addItemDecoration(divider);
        //savedSentencesRecyclerView.setBackground(getResources().getDrawable(R.drawable.border_radius_bottom));
        savedSentencesRecyclerView.setAdapter(adapter);
        touchHelper.attachToRecyclerView(savedSentencesRecyclerView);
        adapter.setSavedSentencesList(listSavedSentences);

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
