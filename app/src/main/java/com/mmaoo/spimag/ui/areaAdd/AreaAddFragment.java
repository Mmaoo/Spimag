package com.mmaoo.spimag.ui.areaAdd;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mmaoo.spimag.Backable;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.Area;

import java.util.Arrays;

public class AreaAddFragment extends Fragment implements Backable {

    public static final int ACTION_ADD = 1;
    public static final int ACTION_EDIT = 2;

    private View root;

    EditText areaNameTextView;

    Navigable navigable;
    private int action = -1;

    Area area = null;

    public ActionMode actionMode = null;

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.edit_menu,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.edit_menu_action_save:
                    area.setName(areaNameTextView.getText().toString());
                    if(action == ACTION_ADD) AppDatabase.getInstance().add(area);
                    else AppDatabase.getInstance().update(area);
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            navigable.navigateUp();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.w("test","fragment area add");
        root = inflater.inflate(R.layout.fragment_area_add, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();

        if(arguments == null) {
            Log.d(this.getClass().toString(),"Arguments is null");
            navigable.navigateUp();
        }

        areaNameTextView = root.findViewById(R.id.areaNameTextView);

        action = arguments.getInt("action",Integer.MAX_VALUE);
        switch(action){
            case ACTION_EDIT:
                try {
                    area = (Area) arguments.getSerializable("area");
                    areaNameTextView.setText(area.getName());
                }catch (ClassCastException e) {
                    Log.w(this.getClass().toString(),e.getMessage());
                    Log.w(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
                    navigable.navigateUp();
                }
                break;
            case ACTION_ADD:
                area = new Area();
                actionMode = root.startActionMode(actionModeCallback);
                break;
            default: navigable.navigateUp();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigable = (Navigable) context;
        }catch (ClassCastException e){
            Log.e(this.getClass().toString(),e.getMessage());
            Log.e(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public boolean onBackPressed() {
        if(actionMode != null){
            actionMode.finish();
            return true;
        }else {
            return false;
        }
    }
}
