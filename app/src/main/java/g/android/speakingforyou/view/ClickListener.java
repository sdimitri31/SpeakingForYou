package g.android.speakingforyou.view;

import android.view.View;

public interface ClickListener {

    void onPositionClicked(int position);

    void onLongClick(View view, int position, long id);

    void onItemClick(View view, int position, long id);


}