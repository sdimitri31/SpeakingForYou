package g.android.speakingforyou.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewStructure;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import g.android.speakingforyou.controller.HistoryAdapter;
import g.android.speakingforyou.model.History;
import g.android.speakingforyou.model.HistoryDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.model.VoiceSettings;

import static android.content.Context.MODE_PRIVATE;
import static g.android.speakingforyou.controller.MainActivity.PREF_NAME;

public class HistoryFragment extends Fragment implements View.OnClickListener {


    private static final String LOG_TAG = "SFY : HistoryFragment";
    private HistoryDAO mHistoryDAO;
    private List<History> mListHistory;
    private HistoryAdapter mHistoryAdapter;
    private VoiceSettings mVoiceSettings;

    RecyclerView historyRecyclerView;

    //2 - Declare callback
    private HistoryFragment.OnButtonClickedListener mCallback;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }


    // newInstance constructor for creating fragment with arguments
    public static HistoryFragment newInstance() {

        return new HistoryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout of MainFragment
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        mVoiceSettings  = new VoiceSettings(getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE));

        //Setup the recycler for the history
        historyRecyclerView = rootView.findViewById(R.id.recyclerView_History);

        mHistoryDAO = new HistoryDAO(getActivity());
        mListHistory = mHistoryDAO.getAll();
        sortHistoryList(mVoiceSettings.getHistorySort(),mVoiceSettings.getHistoryOrder());

        ClickListener listener = new ClickListener() {
            @Override
            public void onPositionClicked(int position) {

            }

            @Override
            public void onLongClicked(int position) {

            }

            @Override
            public void onItemClick(View view, int position, long id) {
                Log.i(LOG_TAG, "onItemClick Position : " + position);
                if(view.getId() == R.id.constraintLayout_HistoryCell) {
                    //Talk
                    Log.i(LOG_TAG, "onItemClick Callback to Talk");
                    view.setTag(mListHistory.get(position));
                    mCallback.onButtonClicked(view);
                }
                if(view.getId() == R.id.imageButton_HistoryCell_Delete) {
                    //Delete History
                    Log.i(LOG_TAG, "onItemClick Delete");

                    deleteHistory(mListHistory.get(position));  //Delete in database
                    mListHistory.remove(position);              //Delete in list
                    mHistoryAdapter.notifyItemRemoved(position);//Delete in recycler
                }
                if(view.getId() == R.id.imageButton_HistoryCell_AddToSaved){
                    addSavedSentence(mListHistory.get(position).getSentence());
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_addedToSavedSentences), Toast.LENGTH_LONG).show();
                }
                if(view.getId() == R.id.imageButton_HistoryCell_More){
                    //Close other popup
                    Log.i(LOG_TAG,"onItemClick More ");

                    mHistoryAdapter.updateVisibility(false);
                    notifyDataSetChangedExceptPosition(position);
                }
            }
        };

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mHistoryAdapter = new HistoryAdapter(getActivity(), listener);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(mHistoryAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        mHistoryAdapter.setTouchHelper(touchHelper);
        historyRecyclerView.setAdapter(mHistoryAdapter);
        touchHelper.attachToRecyclerView(historyRecyclerView);
        mHistoryAdapter.setHistoryList(mListHistory);

        return rootView;
    }

    public void sortHistoryList(int sort, int order){
        if (mListHistory.size() > 0) {
            switch (order) {

                case VoiceSettings.ORDERBY_DESC:
                    switch (sort) {
                        case VoiceSettings.SORTBY_CHRONO:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return object2.getDate().compareTo(object1.getDate());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_USAGE:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return (object2.getUsage()) - (object1.getUsage());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_ALPHA:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return object2.getSentence().compareToIgnoreCase(object1.getSentence());
                                }
                            });
                            break;
                    }
                    break;

                case VoiceSettings.ORDERBY_ASC:
                    switch (sort) {
                        case VoiceSettings.SORTBY_CHRONO:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return object1.getDate().compareTo(object2.getDate());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_USAGE:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return (object1.getUsage()) - (object2.getUsage());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_ALPHA:
                            Collections.sort(mListHistory, new Comparator<History>() {
                                @Override
                                public int compare(final History object1, final History object2) {
                                    return object1.getSentence().compareToIgnoreCase(object2.getSentence());
                                }
                            });
                            break;
                    }
                    break;
            }
        }
    }

    public void notifyDataSetChangedExceptPosition(int position){
        ((SimpleItemAnimator) historyRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        for(int i = 0; i< mHistoryAdapter.getItemCount(); i++){
            if(i != position){
                mHistoryAdapter.notifyItemChanged(i);
            }
        }
    }

    public void addSavedSentence(String stringToAdd){
        final SavedSentencesDAO savedSentencesDAO = new SavedSentencesDAO(getContext());
        SavedSentences sentenceToSave = new SavedSentences(stringToAdd,
                savedSentencesDAO.getNextPosition()
        );
        savedSentencesDAO.add(sentenceToSave);
    }
    public void deleteHistory(History history){
        mHistoryDAO.delete(history.getId());
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
        Log.i(LOG_TAG,"Fragment Click ID : " + v.getId());
        mCallback.onButtonClicked(v);
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (HistoryFragment.OnButtonClickedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
        mListHistory = mHistoryDAO.getAll();
        sortHistoryList(mVoiceSettings.getHistorySort(),mVoiceSettings.getHistoryOrder());
        mHistoryAdapter.setHistoryList(mListHistory);
    }

}
