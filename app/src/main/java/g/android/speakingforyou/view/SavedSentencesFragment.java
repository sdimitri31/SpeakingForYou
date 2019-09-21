package g.android.speakingforyou.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.controller.SavedSentencesAdapter;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.R;

public class SavedSentencesFragment extends Fragment implements View.OnClickListener{

    private static final String LOG_TAG = "SFY : SavedSentenceFrag";
    private SavedSentencesDAO mSavedSentencesDAO;
    private List<SavedSentences> mListSavedSentences;
    private SavedSentencesAdapter mSavedSentencesAdapter;
    RecyclerView savedSentencesRecyclerView;

    //2 - Declare callback
    private OnButtonClickedListener mCallback;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }


    // newInstance constructor for creating fragment with arguments
    public static SavedSentencesFragment newInstance() {
        return new SavedSentencesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //Inflate the layout of MainFragment
        View rootView = inflater.inflate(R.layout.fragment_saved_sentences, container, false);

        //Setup the recycler for the saved sentences
        savedSentencesRecyclerView = rootView.findViewById(R.id.recyclerView_SavedSentences);

        mSavedSentencesDAO = new SavedSentencesDAO(getActivity());
        mListSavedSentences = mSavedSentencesDAO.getAll();

        ClickListener listener = new ClickListener() {
            @Override
            public void onPositionClicked(int position) {}

            @Override
            public void onLongClicked(int position) {}

            @Override
            public void onItemClick(View view, int position, long id) {
                Log.i(LOG_TAG, "onItemClick Position : " + position);
                if(view.getId() == R.id.constraintLayout_SavedSentenceCell) {
                    //Talk
                    Log.i(LOG_TAG, "onItemClick Callback to Talk");

                    view.setTag(mListSavedSentences.get(position));
                    mCallback.onButtonClicked(view);
                }
                if(view.getId() == R.id.imageButton_SavedSentenceCell_Delete) {
                    //Delete Item
                    Log.i(LOG_TAG, "onItemClick Delete");

                    deleteSavedSentence(mListSavedSentences.get(position)); //Delete in database
                    mListSavedSentences.remove(position);                   //Delete in list
                    mSavedSentencesAdapter.notifyItemRemoved(position);     //Delete in recycler
                }
                if(view.getId() == R.id.imageButton_SavedSentenceCell_more){
                    //Close other popup
                    Log.i(LOG_TAG,"onItemClick More ");

                    mSavedSentencesAdapter.updateVisibility(false);
                    notifyDataSetChangedExceptPosition(position);
                }
            }
        };

        savedSentencesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSavedSentencesAdapter = new SavedSentencesAdapter(getActivity(), listener);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(mSavedSentencesAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        mSavedSentencesAdapter.setTouchHelper(touchHelper);
        savedSentencesRecyclerView.setAdapter(mSavedSentencesAdapter);
        touchHelper.attachToRecyclerView(savedSentencesRecyclerView);
        mSavedSentencesAdapter.setSavedSentencesList(mListSavedSentences);

        return rootView;
    }

    public void notifyDataSetChangedExceptPosition(int position){
        ((SimpleItemAnimator) savedSentencesRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        for(int i = 0; i< mSavedSentencesAdapter.getItemCount(); i++){
            if(i != position){
                mSavedSentencesAdapter.notifyItemChanged(i);
            }
        }
    }

    public void deleteSavedSentence(SavedSentences sentence){
        mSavedSentencesDAO.delete(sentence.getId());
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
        Log.i(LOG_TAG,"Fragment Click ID : " + v.getId());
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


    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
        mListSavedSentences = mSavedSentencesDAO.getAll();
        mSavedSentencesAdapter.setSavedSentencesList(mListSavedSentences);
    }

}
