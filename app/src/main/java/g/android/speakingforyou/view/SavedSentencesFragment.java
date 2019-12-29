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
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import g.android.speakingforyou.model.SavedSentences;
import g.android.speakingforyou.controller.SavedSentencesAdapter;
import g.android.speakingforyou.model.SavedSentencesDAO;
import g.android.speakingforyou.R;
import g.android.speakingforyou.model.VoiceSettings;

import static android.content.Context.MODE_PRIVATE;
import static g.android.speakingforyou.controller.MainActivity.PREF_NAME;

public class SavedSentencesFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener{

    private static final String LOG_TAG = "SFY : SavedSentenceFrag";
    private SavedSentencesDAO mSavedSentencesDAO;
    private List<SavedSentences> mListSavedSentences;
    private SavedSentencesAdapter mSavedSentencesAdapter;
    RecyclerView savedSentencesRecyclerView;
    private VoiceSettings mVoiceSettings;
    private List<SavedSentences> currentSelectedItems = new ArrayList<>();

    //2 - Declare callback
    private OnButtonClickedListener mCallback;
    private OnLongClickListener     mCallbackLongClick;

    // 1 - Declare our interface that will be implemented by any container activity
    public interface OnButtonClickedListener {
        void onButtonClicked(View view);
    }

    public interface OnLongClickListener {
        void onLongClick(View view);
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

        mVoiceSettings  = new VoiceSettings(getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE));
        
        //Setup the recycler for the saved sentences
        savedSentencesRecyclerView = rootView.findViewById(R.id.recyclerView_SavedSentences);

        mSavedSentencesDAO = new SavedSentencesDAO(getActivity());
        mListSavedSentences = mSavedSentencesDAO.getAll();
        sortSavedSentencesList(mVoiceSettings.getSavedSentencesSort(),mVoiceSettings.getSavedSentencesOrder());

        ClickListener listener = new ClickListener() {
            @Override
            public void onPositionClicked(int position) {}

            @Override
            public void onLongClick(View view, int position, long id) {
                if(view.getId() == R.id.constraintLayout_SavedSentenceCell) {
                    Log.i(LOG_TAG, "onLongClick Callback "  + position + " id " + id + " view " + getResources().getResourceEntryName(view.getId()));
                    view.setTag(position);
                    mSavedSentencesAdapter.setVisibilitySelectRadioButton(true);
                    mSavedSentencesAdapter.notifyDataSetChanged();
                    mCallbackLongClick.onLongClick(view);
                }
            }

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

                if(view.getId() == R.id.checkBox_SavedSentenceCell_SelectItem){
                    //RadioButton
                    Log.i(LOG_TAG,"onItemClick checkBox_SavedSentenceCell_SelectItem ");
                    view.setTag(mListSavedSentences.get(position));
                    mCallback.onButtonClicked(view);
                }
            }
        };

        SavedSentencesAdapter.OnItemCheckListener checkListener = new SavedSentencesAdapter.OnItemCheckListener() {
            @Override
            public void onItemCheck(SavedSentences item) {
                Log.i(LOG_TAG,"onItemCheck " + item.getSentence());
                currentSelectedItems.add(item);
            }

            @Override
            public void onItemUncheck(SavedSentences item) {
                Log.i(LOG_TAG,"onItemUncheck " + item.getSentence());
                currentSelectedItems.remove(item);
            }
        };

        savedSentencesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSavedSentencesAdapter = new SavedSentencesAdapter(getActivity(), listener, currentSelectedItems, checkListener);
        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(mSavedSentencesAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);
        mSavedSentencesAdapter.setTouchHelper(touchHelper);
        savedSentencesRecyclerView.setAdapter(mSavedSentencesAdapter);
        touchHelper.attachToRecyclerView(savedSentencesRecyclerView);
        mSavedSentencesAdapter.setSavedSentencesList(mListSavedSentences);

        return rootView;
    }

    public void sortSavedSentencesList(int sort, int order){
        if (mListSavedSentences.size() > 0) {
            switch (order) {

                case VoiceSettings.ORDERBY_DESC:
                    switch (sort) {
                        case VoiceSettings.SORTBY_CHRONO:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return object2.getDate().compareTo(object1.getDate());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_USAGE:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return (object2.getUsage()) - (object1.getUsage());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_ALPHA:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return object2.getSentence().compareToIgnoreCase(object1.getSentence());
                                }
                            });
                            break;
                    }
                    break;

                case VoiceSettings.ORDERBY_ASC:
                    switch (sort) {
                        case VoiceSettings.SORTBY_CHRONO:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return object1.getDate().compareTo(object2.getDate());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_USAGE:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return (object1.getUsage()) - (object2.getUsage());
                                }
                            });
                            break;
                        case VoiceSettings.SORTBY_ALPHA:
                            Collections.sort(mListSavedSentences, new Comparator<SavedSentences>() {
                                @Override
                                public int compare(final SavedSentences object1, final SavedSentences object2) {
                                    return object1.getSentence().compareToIgnoreCase(object2.getSentence());
                                }
                            });
                            break;
                    }
                    break;
            }
        }
    }

    public List<SavedSentences> getCurrentSelectedItems(){
        return currentSelectedItems;
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

    public void selectPosition(int position){
        try{
            Log.i(LOG_TAG,"selectPosition " + position);
            SavedSentencesViewHolder viewHolder = (SavedSentencesViewHolder)savedSentencesRecyclerView.findViewHolderForAdapterPosition(position);
            if(!viewHolder.mCheckBox_SelectedCell.isChecked())
                viewHolder.itemView.performClick();
        }
        catch (Exception e){
            Log.e(LOG_TAG,"Exception : " + e.getMessage());
        }
    }

    public void selectALL(){
        for (int i = 0; i < mSavedSentencesAdapter.getItemCount(); i++) {
            try{
                if(!((SavedSentencesViewHolder) savedSentencesRecyclerView.findViewHolderForAdapterPosition(i)).mCheckBox_SelectedCell.isChecked())
                savedSentencesRecyclerView.findViewHolderForAdapterPosition(i).itemView.performClick();
            }
            catch (Exception e){
                Log.e(LOG_TAG,"Exception : " + e.getMessage());
            }
        }
    }

    public void unSelectPosition(int position){
        try{
            if(((SavedSentencesViewHolder) savedSentencesRecyclerView.findViewHolderForAdapterPosition(position)).mCheckBox_SelectedCell.isChecked())
                savedSentencesRecyclerView.findViewHolderForAdapterPosition(position).itemView.performClick();
        }
        catch (Exception e){
            Log.e(LOG_TAG,"Exception : " + e.getMessage());
        }
    }

    public void unSelectAll(){
        for (int i = 0; i < mSavedSentencesAdapter.getItemCount(); i++) {
            try{
                if(((SavedSentencesViewHolder) savedSentencesRecyclerView.findViewHolderForAdapterPosition(i)).mCheckBox_SelectedCell.isChecked())
                    savedSentencesRecyclerView.findViewHolderForAdapterPosition(i).itemView.performClick();
            }
            catch (Exception e){
                Log.e(LOG_TAG,"Exception : " + e.getMessage());
            }
        }
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
            mCallback = (OnButtonClickedListener) getActivity();
            mCallbackLongClick = (OnLongClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnButtonClickedListener or OnLongClickListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG,"onResume");
        mListSavedSentences = mSavedSentencesDAO.getAll();
        sortSavedSentencesList(mVoiceSettings.getSavedSentencesSort(),mVoiceSettings.getSavedSentencesOrder());
        mSavedSentencesAdapter.setSavedSentencesList(mListSavedSentences);
    }

}
