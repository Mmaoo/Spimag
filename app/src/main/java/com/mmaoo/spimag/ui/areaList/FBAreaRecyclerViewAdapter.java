package com.mmaoo.spimag.ui.areaList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

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

import java.util.ArrayList;
import java.util.Iterator;

public class FBAreaRecyclerViewAdapter extends RecyclerView.Adapter<AreaViewHolder> {

    ArrayList<Area> allAreas = new ArrayList<>();
    ArrayList<Area> preparedAreas = new ArrayList<>();

    ArrayList<Area> areaArrayList = new ArrayList<Area>();

    RecyclerView recyclerView;

    // posts count loaded on each loading
    int pageSize = 1;
    // posts count loaded on first loading
    int initLoad = 1;
    // how many posts before end of list loading new page starts
    int prefetchDistance = 0;

    // last downloaded snapshot
//    DataSnapshot prevSnapshot = null;

    private MutableLiveData<Boolean> isRefreshing;
    private MutableLiveData<Area> clickedItem;
    private MutableLiveData<Area> longClickedItem;

    public FBAreaRecyclerViewAdapter(@NonNull RecyclerView recyclerView, int pageSize, int initialLoad, int prefetchDistance){
        if(prefetchDistance > 0) this.prefetchDistance = prefetchDistance;
        if(initialLoad > 1 ) this.initLoad = initialLoad;
        if(pageSize > 1) this.pageSize = pageSize;
        if(recyclerView != null) this.recyclerView = recyclerView;

//        String uid = AppUser.getInstance().getUid();

        isRefreshing = new MutableLiveData<>(false);
        clickedItem = new MutableLiveData<Area>(null);
        longClickedItem = new MutableLiveData<Area>(null);
//        refresh();
    }

    public LiveData<Boolean> isRefreshing() {
        return isRefreshing;
    }

    @NonNull
    @Override
    public AreaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.area_recycler_element,parent,false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AreaViewHolder holder, int position) {
        holder.bind(areaArrayList.get(position));
        holder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().toString(), "onBindViewHolder: clickListener: " + areaArrayList.get(position).getName());
                clickedItem.setValue(areaArrayList.get(position));
            }
        });
        holder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(getClass().toString(), "onBindViewHolder: clickListener: " + areaArrayList.get(position).getName());
                longClickedItem.setValue(areaArrayList.get(position));
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return areaArrayList.size();
    }

    public void addItem(Area item){
        areaArrayList.add(item);
        notifyItemInserted(areaArrayList.size()-1);
    }

//    /**
//     * refresh recyclerView
//     */
//    public void refresh(boolean fullRefresh, String search){
//        isRefreshing.setValue(true);
//        recyclerView.clearOnScrollListeners();
//        notifyItemRangeRemoved(0, areaArrayList.size());
//        areaArrayList.clear();
//        prevSnapshot=null;
//        if(fullRefresh) {
//            allAreas.clear();
//            getAllAreas().addOnCompleteListener(new OnCompleteListener<ArrayList<Area>>() {
//                @Override
//                public void onComplete(@NonNull Task<ArrayList<Area>> task) {
//                    if (task.isSuccessful()) {
//                        allAreas = task.getResult();
//                        ArrayList<Area> tempAreas = prepareData(allAreas,search);
//                        for (Area tArea : tempAreas) {
//                            areaArrayList.add(tArea);
//                            notifyItemInserted(areaArrayList.indexOf(tArea));
//                        }
//                        isRefreshing.setValue(false);
//                    }
//                }
//            });
//        }else{
//            ArrayList<Area> tempAreas = prepareData(allAreas,search);
//            for (Area tArea : tempAreas) {
//                areaArrayList.add(tArea);
//                notifyItemInserted(areaArrayList.indexOf(tArea));
//            }
//            isRefreshing.setValue(false);
//        }
//        //addFirstValueEventListener();
//    }

//    private ArrayList<Area> prepareData(ArrayList<Area> areas,String search){
//        ArrayList<Area> tempAreas = new ArrayList<>();
//        for(int i=areas.size()-1;i>=0;i--){
//            Area tArea = areas.get(i);
//
//            if(search != null && !search.trim().isEmpty()) {
//                if(tArea.getName().contains(search)) tempAreas.add(tArea);
//            }else{
//                tempAreas.add(tArea);
//            }
//        }
//        return tempAreas;
//    }

    /**
     * refresh recyclerView
     */
    public void refresh(boolean fullRefresh, String search){
        isRefreshing.setValue(true);
        recyclerView.clearOnScrollListeners();
        notifyItemRangeRemoved(0, areaArrayList.size());
        areaArrayList.clear();
//        prevSnapshot=null;
        if(fullRefresh) {
            allAreas.clear();
            preparedAreas.clear();
            loadFirstPage(search);
        }else{
            preparedAreas.clear();
            preparedAreas = prepareData(allAreas,search);
            loadNextPage();
        }
        //addFirstValueEventListener();
    }

    private ArrayList<Area> prepareData(ArrayList<Area> areas,String search){
        ArrayList<Area> tempAreas = new ArrayList<>();
        for(int i=areas.size()-1;i>=0;i--){
            Area tArea = areas.get(i);

            if(search != null && !search.trim().isEmpty()) {
                if(tArea.getName().contains(search)) tempAreas.add(tArea);
            }else{
                tempAreas.add(tArea);
            }
        }
        return tempAreas;
    }

    private void initOnScrollListener(){
        recyclerView.addOnScrollListener(new OnRecyclerScrollListener(this));
    }

    public Task<ArrayList<Area>> getAllAreas() {
        return AppDatabase.getInstance().getAllAreas();
    }

    private void loadFirstPage(String search){
        getAllAreas().addOnCompleteListener(new OnCompleteListener<ArrayList<Area>>() {
            @Override
            public void onComplete(@NonNull Task<ArrayList<Area>> task) {
                if (task.isSuccessful()) {
                    allAreas = task.getResult();
                    preparedAreas = prepareData(allAreas,search);
                    for(int i=0;i<initLoad;i++){
                        if(i >= preparedAreas.size()) break;
                        areaArrayList.add(preparedAreas.get(i));
                        notifyItemInserted(i);
                    }
                    initOnScrollListener();
                    isRefreshing.setValue(false);
                }
            }
        });
    };

    private void loadNextPage(){
        int first = areaArrayList.size();
        int last = first+pageSize;
        for(int i=first;i<last;i++){
            if(i >= preparedAreas.size()) break;
            areaArrayList.add(preparedAreas.get(i));
            Log.d(this.getClass().toString(),"areaArrayList.size="+areaArrayList.size()+", i="+i);
        }
        notifyItemRangeInserted(first,pageSize);
//        recyclerView.post(() -> notifyItemRangeInserted(first,pageSize));
        isRefreshing.setValue(false);
    }

    /**
     * scrollListener to load next pages
     */
    private class OnRecyclerScrollListener extends RecyclerView.OnScrollListener {
        FBAreaRecyclerViewAdapter mAdapter;

        public OnRecyclerScrollListener(FBAreaRecyclerViewAdapter adapter){
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

//    private class FirstValueEventListener implements ValueEventListener {
//        FBAreaRecyclerViewAdapter mAdapter;
//
//        public FirstValueEventListener(FBAreaRecyclerViewAdapter adapter){
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
//                Area area = a.get(i).getValue(Area.class);
//                area.setId(a.get(i).getKey());
//                mAdapter.addItem(area);
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

//    private class NextValueEventListener implements ValueEventListener{
//        FBAreaRecyclerViewAdapter mAdapter;
//
//        public NextValueEventListener(FBAreaRecyclerViewAdapter adapter){
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
//                Area area = a.get(i).getValue(Area.class);
//                area.setId(a.get(i).getKey());
//                mAdapter.addItem(area);
//            }
//            if (a.size()>0) prevSnapshot = a.get(0);
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//
//        }
//    }

//    /**
//     * scrollListener to load next pages
//     */
//    private class OnRecyclerScrollListener extends RecyclerView.OnScrollListener {
//        FBAreaRecyclerViewAdapter mAdapter;
//
//        public OnRecyclerScrollListener(FBAreaRecyclerViewAdapter adapter){
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

    public MutableLiveData<Area> getClickedItem() {
        return clickedItem;
    }

    public MutableLiveData<Area> getLongClickedItem() {
        return longClickedItem;
    }
}
