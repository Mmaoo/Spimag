package com.mmaoo.spimag.ui.itemList;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.Query;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.Area;
import com.mmaoo.spimag.model.Item;
import com.mmaoo.spimag.ui.ItemView.ItemViewFragment;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemListFragment extends Fragment {

    Navigable navigable;

    TextView searchTextView;
    FloatingActionButton addFloatingActionButton;
    RecyclerView itemListRecyclerView;
    SwipeRefreshLayout itemListSwipeRefreshLayout;

    FBItemRecyclerViewAdapter itemRecyclerViewAdapter;

    Query itemQuery;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_item_list, container, false);

        searchTextView = root.findViewById(R.id.searchEditText);
        addFloatingActionButton = root.findViewById(R.id.addFloatingActionButton);
        itemListRecyclerView = root.findViewById(R.id.itemListRecyclerView);
        itemListSwipeRefreshLayout = root.findViewById(R.id.itemListSwipeRefreshLayout);

        addFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle args = new Bundle();
//                Item item = new Item("test","testowy",null,1);
//                args.putSerializable("item",item);
//                args.putInt("action",ItemViewFragment.ACTION_SHOW);
                args.putInt("action", ItemViewFragment.ACTION_ADD);
                navigable.navigate(R.id.action_navigate_to_item_view,args);

//                ItemViewFragment itemViewFragment = new ItemViewFragment();
//                getParentFragmentManager()
//                        .beginTransaction()
//                        .add(R.id.nav_host_fragment,itemViewFragment,)
//                        .commit();
            }
        });

        FBItemRecyclerViewAdapter itemRecyclerViewAdapter = new FBItemRecyclerViewAdapter(itemListRecyclerView,5,10,5);
        itemRecyclerViewAdapter.isRefreshing().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRefreshing) {
                if(isRefreshing) {
                    itemListSwipeRefreshLayout.setRefreshing(true);
                }else{
                    itemListSwipeRefreshLayout.setRefreshing(false);
                    itemRecyclerViewAdapter.isRefreshing().removeObserver(this);
                }
            }
        });
        itemRecyclerViewAdapter.refresh(true,searchTextView.getText().toString());

        itemRecyclerViewAdapter.getClickedItem().observe(getViewLifecycleOwner(),new Observer<Item>() {
            @Override
            public void onChanged(Item item) {
                if(item != null) {
                    Bundle args = new Bundle();
                    args.putSerializable("item", item);
                    args.putInt("action", ItemViewFragment.ACTION_SHOW);
//                args.putInt("action", ItemViewFragment.ACTION_ADD);
                    navigable.navigate(R.id.action_navigate_to_item_view, args);
                }
            }
        });
        itemListRecyclerView.setAdapter(itemRecyclerViewAdapter);

        itemListSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String search = searchTextView.getText().toString();
                itemRecyclerViewAdapter.refresh(true,search);
                itemRecyclerViewAdapter.isRefreshing().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isRefreshing) {
                        if(!isRefreshing){
                            itemListSwipeRefreshLayout.setRefreshing(false);
                            itemRecyclerViewAdapter.isRefreshing().removeObserver(this);
                        }
                    }
                });
            }
        });

        searchTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                itemRecyclerViewAdapter.refresh(false,s.toString());
            }
        });

        //        itemListViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
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
}