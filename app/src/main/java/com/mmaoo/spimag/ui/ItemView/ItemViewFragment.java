package com.mmaoo.spimag.ui.ItemView;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mmaoo.spimag.Backable;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.Item;
import com.mmaoo.spimag.ui.areaShow.AreaShowFragment;

import java.util.Arrays;

public class ItemViewFragment extends Fragment implements Backable {

    public static final int ACTION_SHOW = 0;
    public static final int ACTION_ADD = 1;
    private static final int ACTION_EDIT_NAME = 101;
    private static final int ACTION_EDIT_SHORT_NAME = 102;
    private static final int ACTION_EDIT_PACKAGE = 103;

    private View root;

    private EditText itemNameTextView;
    private Button itemNameEditButton;
    private EditText itemShortNameTextView;
    private Button itemShortNameEditButton;
    private EditText packageTextView;
    private Button packageEditButton;
    private TextView amountTextView;
    private Button decPackButton;
    private Button incPackButton;
    private TextView itemAreaTextView;
    private Button itemAreaEditButton;
    private ImageView itemAreaImageView;

    private Navigable navigable;
    private int action = -1;

    private Item item = null;

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
            itemNameEditButton.setEnabled(false);
            itemShortNameEditButton.setEnabled(false);
            packageEditButton.setEnabled(false);
            incPackButton.setEnabled(false);
            decPackButton.setEnabled(false);
            itemAreaEditButton.setEnabled(false);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.edit_menu_action_save:
                    item.setName(itemNameTextView.getText().toString());
                    item.setShortName(itemShortNameTextView.getText().toString());
                    item.setPack(packageTextView.getText().toString());
                    if(action == ACTION_ADD) AppDatabase.getInstance().add(item);
                    else AppDatabase.getInstance().update(item);
                    action = ACTION_SHOW;
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            itemNameTextView.setEnabled(false);
            itemNameTextView.setText(item.getName());
            itemNameEditButton.setEnabled(true);
            itemShortNameTextView.setEnabled(false);
            itemShortNameTextView.setText(item.getShortName());
            itemShortNameEditButton.setEnabled(true);
            packageTextView.setEnabled(false);
            packageTextView.setText(item.getPack());
            packageEditButton.setEnabled(true);
            incPackButton.setEnabled(true);
            decPackButton.setEnabled(true);
            itemAreaEditButton.setEnabled(true);
            actionMode = null;
            if (action == ACTION_ADD) navigable.navigateUp();
        }
    };

    public static ItemViewFragment newInstance() {
        return new ItemViewFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_item_view, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle arguments = getArguments();

        if(arguments == null) {
            Log.d(this.getClass().toString(),"Arguments is null");
            navigable.navigateUp();
            //getParentFragmentManager().popBackStack();
        }

        itemNameTextView = root.findViewById(R.id.itemNameTextView);
        itemNameEditButton = root.findViewById(R.id.itemNameEditButton);
        itemShortNameTextView = root.findViewById(R.id.itemShortNameTextView);
        itemShortNameEditButton = root.findViewById(R.id.itemShortNameEditButton);
        packageTextView = root.findViewById(R.id.packageTextView);
        packageEditButton = root.findViewById(R.id.packageEditButton);
        amountTextView = root.findViewById(R.id.amountTextView);
        decPackButton = root.findViewById(R.id.decPackButton);
        incPackButton = root.findViewById(R.id.incPackButton);
        itemAreaTextView = root.findViewById(R.id.itemAreaTextView);
        itemAreaEditButton = root.findViewById(R.id.itemAreaEditButton);
        itemAreaImageView = root.findViewById(R.id.itemAreaImageView);

        action = arguments.getInt("action",Integer.MAX_VALUE);
        switch(action){
            case ACTION_SHOW:
                try {
                    item = (Item) arguments.getSerializable("item");
                    initShow(item);
                }catch (ClassCastException e) {
                    Log.w(this.getClass().toString(),e.getMessage());
                    Log.w(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
                    navigable.navigateUp();
                }
                break;
            case ACTION_ADD:
                item = new Item();
                editName();
                break;
            default: navigable.navigateUp();
        }

        itemNameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editName();
            }
        });

        itemShortNameEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editShortName();
            }
        });

        packageEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPackage();
            }
        });

        decPackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(amountTextView.getText().toString());
                if (amount <= 0) amount = 0;
                else --amount;
                item.setAmount(amount);
                amountTextView.setText(Float.toString(item.getAmount()));
                AppDatabase.getInstance().update(item);
            }
        });

        incPackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float amount = Float.parseFloat(amountTextView.getText().toString());
                if(amount >= Float.MAX_VALUE) amount = Float.MAX_VALUE;
                else ++amount;
                item.setAmount(amount);
                amountTextView.setText(Float.toString(item.getAmount()));
                AppDatabase.getInstance().update(item);
            }
        });

        itemAreaEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putSerializable("item", item);
                args.putInt("action", AreaShowFragment.ACTION_ADD_ITEM);
                navigable.navigate(R.id.action_navigate_to_area_list, args);
            }
        });
    }

    private void initShow(Item item){
//        itemNameTextView.setFocusable(false);
        itemNameTextView.setEnabled(false);
        itemNameTextView.setText(item.getName());
//        Package pack = item.getPack();
//        if(pack != null) {
//            packageTextView.setText(pack.getType() + " " + pack.getAmount() + " " + pack.getUnit());
//        }
        itemShortNameTextView.setEnabled(false);
        itemShortNameTextView.setText(item.getShortName());
        packageTextView.setEnabled(false);
        packageTextView.setText(item.getPack());
        amountTextView.setText(Float.toString(item.getAmount()));
    }

    private void editName(){
        Log.w(this.getClass().toString(),"editName");
        action = ACTION_EDIT_NAME;
//        itemNameTextView.setFocusable(true);
        itemNameTextView.setEnabled(true);
        itemNameTextView.requestFocus();
        actionMode = root.startActionMode(actionModeCallback);
    }

    private void editShortName(){
        Log.w(this.getClass().toString(),"editShortName");
        action = ACTION_EDIT_SHORT_NAME;
        itemShortNameTextView.setEnabled(true);
        itemShortNameTextView.requestFocus();
        actionMode = root.startActionMode(actionModeCallback);
    }

    private void editPackage(){
        Log.w(this.getClass().toString(),"editPackage");
        action = ACTION_EDIT_PACKAGE;
        packageTextView.setEnabled(true);
        packageTextView.requestFocus();
        actionMode = root.startActionMode(actionModeCallback);
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

    @Override
    public void onPause() {
//        AppDatabase.getInstance().update(item);
        super.onPause();
    }
}