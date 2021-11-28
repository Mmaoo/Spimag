package com.mmaoo.spimag.ui.itemList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.AppUser;
import com.mmaoo.spimag.model.Area;
import com.mmaoo.spimag.model.FBDatabase;
import com.mmaoo.spimag.model.Item;
import com.mmaoo.spimag.ui.areaList.FBAreaRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Iterator;

public class FBItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    ArrayList<Item> allItems = new ArrayList<>();
    ArrayList<Item> preparedItems = new ArrayList<>();

    ArrayList<Item> itemArrayList = new ArrayList<>();

    RecyclerView recyclerView;

    // posts count loaded on each loading
    int pageSize = 1;
    // posts count loaded on first loading
    int initLoad = 1;
    // how many posts before end of list loading new page starts
    int prefetchDistance = 0;

    // last downloaded snapshot
    DataSnapshot prevSnapshot = null;

    private MutableLiveData<Boolean> isRefreshing;
    private MutableLiveData<Item> clickedItem;
    private MutableLiveData<Item> longClickedItem;

    public FBItemRecyclerViewAdapter(@NonNull RecyclerView recyclerView, int pageSize, int initialLoad, int prefetchDistance){
        if(prefetchDistance > 0) this.prefetchDistance = prefetchDistance;
        if(initialLoad > 1 ) this.initLoad = initialLoad;
        if(pageSize > 1) this.pageSize = pageSize;
        if(recyclerView != null) this.recyclerView = recyclerView;

//        String uid = AppUser.getInstance().getUid();
//        if(uid != null) this.query = FirebaseDatabase.getInstance().getReference(FBDatabase.PATH_USER+"/"+uid+FBDatabase.PATH_ITEM);

        isRefreshing = new MutableLiveData<>(false);
        clickedItem = new MutableLiveData<>(null);
        longClickedItem = new MutableLiveData<>(null);
//        refresh();
    }

    public LiveData<Boolean> isRefreshing() {
        return isRefreshing;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_element,parent,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.bind(itemArrayList.get(position));
        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().toString(), "onBindViewHolder: clickListener: " + itemArrayList.get(position).getName());
                clickedItem.setValue(itemArrayList.get(position));
            }
        });
        holder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(getClass().toString(), "onBindViewHolder: clickListener: " + itemArrayList.get(position).getName());
                longClickedItem.setValue(itemArrayList.get(position));
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    public void addItem(Item item){
        itemArrayList.add(item);
        notifyItemInserted(itemArrayList.size()-1);
    }

    /**
     * refresh recyclerView
     */
    public void refresh(boolean fullRefresh, String search){
        isRefreshing.setValue(true);
        recyclerView.clearOnScrollListeners();
        notifyDataSetChanged();
        //notifyItemRangeRemoved(0, itemArrayList.size());
        itemArrayList.clear();
//        prevSnapshot=null;
        if(fullRefresh) {
            allItems.clear();
            preparedItems.clear();
            loadFirstPage(search);
        }else{
            preparedItems.clear();
            preparedItems = prepareData(allItems,search);
            loadNextPage();
        }
        //addFirstValueEventListener();
    }

    private ArrayList<Item> prepareData(ArrayList<Item> items,String search){
        ArrayList<Item> tempItems = new ArrayList<>();
        for(int i=items.size()-1;i>=0;i--){
            Item tItem = items.get(i);

            if(search != null && !search.trim().isEmpty()) {
                if(tItem.getName().contains(search)) tempItems.add(tItem);
            }else{
                tempItems.add(tItem);
            }
        }
        return tempItems;
    }

    private void initOnScrollListener(){
        recyclerView.addOnScrollListener(new FBItemRecyclerViewAdapter.OnRecyclerScrollListener(this));
    }

    public Task<ArrayList<Item>> getAllItems() {
        return AppDatabase.getInstance().getAllItems();
    }

    private void loadFirstPage(String search){
        getAllItems().addOnCompleteListener(new OnCompleteListener<ArrayList<Item>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Item>> task) {
                if (task.isSuccessful()) {
                    allItems = task.getResult();
                    preparedItems = prepareData(allItems,search);
                    for(int i=0;i<initLoad;i++){
                        if(i >= preparedItems.size()) break;
                        itemArrayList.add(preparedItems.get(i));
                        notifyItemInserted(i);
                    }
                    initOnScrollListener();
                    isRefreshing.setValue(false);
                }
            }
        });
    };

    private void loadNextPage(){
        int first = itemArrayList.size();
        int last = first+pageSize;
        for(int i=first;i<last;i++){
            if(i >= preparedItems.size()) break;
            itemArrayList.add(preparedItems.get(i));
            Log.d(this.getClass().toString(),"areaArrayList.size="+itemArrayList.size()+", i="+i);
        }
        notifyItemRangeInserted(first,pageSize);
        //recyclerView.post(() -> notifyItemRangeInserted(first,pageSize));
        isRefreshing.setValue(false);
    }

    /**
     * scrollListener to load next pages
     */
    private class OnRecyclerScrollListener extends RecyclerView.OnScrollListener {
        FBItemRecyclerViewAdapter mAdapter;

        public OnRecyclerScrollListener(FBItemRecyclerViewAdapter adapter){
            this.mAdapter = adapter;
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisiblePos = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            if(lastVisiblePos >= mAdapter.getItemCount()-prefetchDistance) loadNextPage();
        }
    }

//    /**
//     * refresh recyclerView
//     */
//    public void refresh(){
//        isRefreshing.setValue(true);
//        recyclerView.clearOnScrollListeners();
//        notifyItemRangeRemoved(0,itemArrayList.size());
//        itemArrayList.clear();
//        prevSnapshot=null;
//        addFirstValueEventListener();
//    }

//    /**
//     * download first page with posts
//     */
//    private void addFirstValueEventListener(){
//        query.orderByChild("timestamp")
//                .limitToLast(initLoad)
//                .addListenerForSingleValueEvent(new FirstValueEventListener(this));
//    }
//
//    /**
//     * download next page with posts
//     */
//    private void addNextValueEventListener(){
//        query.orderByChild("timestamp")
//                .endAt(prevSnapshot.getValue(Item.class).getTimestamp(),"timestamp")
//                .limitToLast(pageSize+1)
//                .addListenerForSingleValueEvent(new NextValueEventListener(this));
//    }
//
//    private class FirstValueEventListener implements ValueEventListener {
//        FBItemRecyclerViewAdapter mAdapter;
//
//        public FirstValueEventListener(FBItemRecyclerViewAdapter adapter){
//            this.mAdapter = adapter;
//        }
//
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
//            DataSnapshot ds = null;
//            ArrayList<DataSnapshot> a = new ArrayList<>();
//            while(it.hasNext()){
//                ds = it.next();
//                a.add(ds);
//            }
//
//            for(int i=a.size()-1; i>=0; i--){
//                Item item = a.get(i).getValue(Item.class);
//                item.setId(a.get(i).getKey());
//                mAdapter.addItem(item);
//            }
//
//            if(a.size()>0) prevSnapshot = a.get(0);
//            else prevSnapshot = null;
//            isRefreshing.setValue(false);
//            initOnScrollListener();
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//        }
//    }
//
//    private class NextValueEventListener implements ValueEventListener{
//        FBItemRecyclerViewAdapter mAdapter;
//
//        public NextValueEventListener(FBItemRecyclerViewAdapter adapter){
//            this.mAdapter = adapter;
//        }
//
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            Iterator<DataSnapshot> it = snapshot.getChildren().iterator();
//            DataSnapshot ds = null;
//            ArrayList<DataSnapshot> a = new ArrayList<>();
//            while(it.hasNext()){
//                ds = it.next();
//                a.add(ds);
//            }
//
//            for(int i=a.size()-2; i>=0; i--){
//                Item item = a.get(i).getValue(Item.class);
//                item.setId(a.get(i).getKey());
//                mAdapter.addItem(item);
//            }
//            if (a.size()>0) prevSnapshot = a.get(0);
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//    }
//
//    /**
//     * scrollListener to load next pages
//     */
//    private class OnRecyclerScrollListener extends RecyclerView.OnScrollListener {
//        FBItemRecyclerViewAdapter mAdapter;
//
//        public OnRecyclerScrollListener(FBItemRecyclerViewAdapter adapter){
//            this.mAdapter = adapter;
//        }
//
//        @Override
//        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//            super.onScrollStateChanged(recyclerView, newState);
//        }
//
//        @Override
//        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//            int lastVisiblePos = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//            if(lastVisiblePos >= mAdapter.getItemCount()-prefetchDistance) addNextValueEventListener();
//        }
//    }

    public MutableLiveData<Item> getClickedItem() {
        return clickedItem;
    }

    public MutableLiveData<Item> getLongClickedItem() {
        return longClickedItem;
    }
}
