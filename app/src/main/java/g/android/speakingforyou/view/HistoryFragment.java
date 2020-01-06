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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

public class HistoryFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {


    private static final String LOG_TAG = "SFY : HistoryFragment";
    private HistoryDAO mHistoryDAO;
    private List<History> mListHistory;
    private HistoryAdapter mHistoryAdapter;
    private VoiceSettings mVoiceSettings;
    private List<History> currentSelectedItems = new ArrayList<>();

    RecyclerView historyRecyclerView;

    HistoryAdapter.OnItemCheckListener checkListener;

    //2 - Declare callback
    private HistoryFragment.OnButtonClickedListener mCallback;
    private OnLongClickListener     mCallbackLongClick;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }

    public interface OnLongClickListener {
        void onLongClick(View view);
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
            public void onLongClick(View view, int position, long id) {
                Log.i(LOG_TAG, "onLongClick Callback "  + position + " id " + id + " view " + view.getId());

                if(view.getId() == R.id.constraintLayout_HistoryCell) {

                    view.setTag(position);
                    mHistoryAdapter.setVisibilitySelectRadioButton(true);
                    selectPosition(position);
                    Log.i(LOG_TAG, "currentSelectedItems "  + currentSelectedItems.size());
                    mCallbackLongClick.onLongClick(view); //Set the Checkbox Action Buttons visible
                }
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

                if(view.getId() == R.id.checkBox_HistoryCell_SelectItem){
                    //RadioButton
                    Log.i(LOG_TAG,"onItemClick checkBox_SavedSentenceCell_SelectItem ");
                    view.setTag(mListHistory.get(position));
                    mCallback.onButtonClicked(view);
                }
            }
        };


        checkListener = new HistoryAdapter.OnItemCheckListener() {
            @Override
            public void onItemCheck(History item) {
                Log.i(LOG_TAG,"onItemCheck " + item.getSentence());
                if(!currentSelectedItems.contains(item)){
                    currentSelectedItems.add(item);
                    mHistoryAdapter.setSelectedHistoryList(currentSelectedItems);
                }
            }

            @Override
            public void onItemUncheck(History item) {
                Log.i(LOG_TAG,"onItemUncheck " + item.getSentence());
                currentSelectedItems.remove(item);
                mHistoryAdapter.setSelectedHistoryList(currentSelectedItems);
            }
        };

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mHistoryAdapter = new HistoryAdapter(getActivity(), listener, currentSelectedItems, checkListener);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(mHistoryAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        mHistoryAdapter.setTouchHelper(touchHelper);
        historyRecyclerView.setAdapter(mHistoryAdapter);
        touchHelper.attachToRecyclerView(historyRecyclerView);
        mHistoryAdapter.setHistoryList(mListHistory);
        mHistoryAdapter.setSelectedHistoryList(currentSelectedItems);

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

    public List<History> getCurrentSelectedItems(){
        Log.i(LOG_TAG,"getCurrentSelectedItems Item selected : " + currentSelectedItems.size());

        return currentSelectedItems;
    }

    public void selectPosition(int position){
        Log.i(LOG_TAG,"selectPosition : " + position);
        checkListener.onItemCheck(mListHistory.get(position));
        mHistoryAdapter.notifyDataSetChanged();
    }

    public void selectALL(){
        Log.i(LOG_TAG,"selectALL : " + mListHistory.size());
        for (int i = 0; i < mListHistory.size(); i++) {
            selectPosition(i);
        }
        Log.i(LOG_TAG,"Item selected : " + currentSelectedItems.size());
    }

    public void unSelectPosition(int position){
        Log.i(LOG_TAG,"unSelectPosition : " + position);
        checkListener.onItemUncheck(mListHistory.get(position));
        mHistoryAdapter.notifyDataSetChanged();
    }

    public void unSelectAll(){
        Log.i(LOG_TAG,"unSelectAll : " + mListHistory.size());
        currentSelectedItems.clear();
        mHistoryAdapter.setSelectedHistoryList(currentSelectedItems);
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

    @Override
    public boolean onLongClick(View v){
        mCallbackLongClick.onLongClick(v);
        Log.i(LOG_TAG,"Fragment Long Click ID : " + v.getId());
        return true;
    }
    // --------------
    // FRAGMENT SUPPORT
    // --------------

    // 3 - Create callback to parent activity
    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (HistoryFragment.OnButtonClickedListener) getActivity();
            mCallbackLongClick = (OnLongClickListener) getActivity();
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
