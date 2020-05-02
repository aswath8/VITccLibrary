package in.wizelab.vitcclibrary.ViewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import in.wizelab.vitcclibrary.R;

/**
 * Created by reale on 23/11/2016.
 */

public class TitleParentViewHolder extends ParentViewHolder {
    public TextView _textView;
    public ImageButton _imageButton;

    public TitleParentViewHolder(View itemView) {
        super(itemView);
        _textView = (TextView)itemView.findViewById(R.id.parentTitle);
        _imageButton = (ImageButton) itemView.findViewById(R.id.expandArrow);
    }
}
