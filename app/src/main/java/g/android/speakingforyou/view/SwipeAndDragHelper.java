package g.android.speakingforyou.view;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Srijith on 22-10-2017.
 */

public class SwipeAndDragHelper extends ItemTouchHelper.Callback {

    private ActionCompletionContract contract;

    public SwipeAndDragHelper(ActionCompletionContract contract) {
        this.contract = contract;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

        //Prevent the move and delete of history element
        if(viewHolder instanceof HistoryViewHolder) {
            return makeMovementFlags(0, 0);
        }
        //Allow the move of Saved element, but prevent to swipe to delete
        if(viewHolder instanceof SavedSentencesViewHolder) {
           // return makeMovementFlags(dragFlags, 0);
            return makeMovementFlags(0, 0);
        }

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        contract.onViewMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        contract.onViewSwiped(viewHolder.getAdapterPosition());
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public void onChildDraw(Canvas c,
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float alpha = 1 - (Math.abs(dX) / recyclerView.getWidth());
            viewHolder.itemView.setAlpha(alpha);
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        // Action finished
        contract.clearView(recyclerView, viewHolder);
    }

    public interface ActionCompletionContract {
        void onViewMoved(int oldPosition, int newPosition);

        void onViewSwiped(int position);

        void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder);
    }

}