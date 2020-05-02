package in.wizelab.vitcclibrary.ViewHolders;

import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;

import in.wizelab.vitcclibrary.R;


/**
 * Created by reale on 23/11/2016.
 */

public class TitleChildViewHolder extends ChildViewHolder {
    public TextView option1,option2;
    public TitleChildViewHolder(View itemView) {
        super(itemView);
        option1 = (TextView)itemView.findViewById(R.id.option1);
        option2 = (TextView)itemView.findViewById(R.id.option2);
    }
}
