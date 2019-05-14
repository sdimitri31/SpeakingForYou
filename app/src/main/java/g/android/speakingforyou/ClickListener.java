package g.android.speakingforyou;

import android.view.View;
import android.widget.AdapterView;

public interface ClickListener {

    void onPositionClicked(int position);

    void onLongClicked(int position);

    void onItemClick(View view, int position, long id);
}