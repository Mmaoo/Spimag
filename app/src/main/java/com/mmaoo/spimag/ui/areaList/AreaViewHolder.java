package com.mmaoo.spimag.ui.areaList;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.Area;

public class
AreaViewHolder extends RecyclerView.ViewHolder {

    View itemView;
    TextView name;

    public AreaViewHolder(@NonNull View areaView) {
        super(areaView);

        this.itemView = areaView;
        this.name = areaView.findViewById(R.id.nameTextView);
    }

    /**
     * bind model to view
     * @param model
     */
    public void bind(final Area model){
        if(model.getName() != null){this.name.setText(model.getName());}
    }

    public View getItemView() {
        return itemView;
    }
}
