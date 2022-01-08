package com.mmaoo.spimag.model;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mmaoo.spimag.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class FBDatabase implements Database {

    public static final String PATH_USER = "/users";
    public static final String PATH_ITEM = "/items";
    public static final String PATH_AREA = "/areas";
//    public static final String PATH_PACKAGE = "/packages";

    FirebaseDatabase firebaseDatabase = null;

    public FBDatabase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setLogLevel(Logger.Level.DEBUG);
    }

    public Task<Item> add(Item item){
        if (item != null){
            Log.d(this.getClass().toString(),"addItem "+ item.toString());

            String uid = AppUser.getInstance().getUid();

            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_ITEM);

            //add(item.getPack());
            item.setId(reference.push().getKey());
            Map<String, Object> values = item.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put(item.getId(), values);
            reference.updateChildren(childUpdates);

            AreaElement areaElement = item.getAreaElement();
            if(areaElement != null){
                reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA).child(areaElement.areaId).child(PATH_ITEM);
                reference.updateChildren(childUpdates);
            }
        }
        return null;
    }

    @Override
    public Task<Item> update(Item item) {
        if (item != null){
            Log.d(this.getClass().toString(),"updateItem "+ item.toString());

            String uid = AppUser.getInstance().getUid();
            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_ITEM);

            Map<String, Object> values = item.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put(item.getId(), values);
            reference.updateChildren(childUpdates);

            AreaElement areaElement = item.getAreaElement();
            if(areaElement != null){
                reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA).child(areaElement.areaId).child(PATH_ITEM);
                reference.updateChildren(childUpdates);
            }
        }
        return null;
    }

    @Override
    public Task<Item> remove(Item item) {
        if (item != null){
            Log.d(this.getClass().toString(),"removeItem "+ item.toString());

            String uid = AppUser.getInstance().getUid();
            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_ITEM);
            reference.child(item.getId()).removeValue();

            AreaElement areaElement = item.getAreaElement();
            if(areaElement != null){
                reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA).child(areaElement.areaId).child(PATH_ITEM);
                reference.child(item.getId()).removeValue();
            }
        }
        return null;
    }

    @Override
    public Task<ArrayList<Item>> getAllItems() {
        String uid = AppUser.getInstance().getUid();
        Query query = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_ITEM).orderByChild("timestamp");

        Command<DataSnapshot,ArrayList<Item>> command = new Command<DataSnapshot, ArrayList<Item>>() {
            @Override
            public ArrayList<Item> run(DataSnapshot dataSnapshot) {
                Log.d("Command","run: "+dataSnapshot.toString());
                ArrayList<Item> itemArrayList = new ArrayList<>();

                for (DataSnapshot itemDS : dataSnapshot.getChildren()) {
                    Item item = new Item();
                    item.setId(itemDS.getKey());
                    String itemName = itemDS.child("name").getValue(String.class);
                    String itemShortName = itemDS.child("shortName").getValue(String.class);
                    String itemPack = itemDS.child("pack").getValue(String.class);
                    Float itemAmount = itemDS.child("amount").getValue(Float.class);
                    Long itemTimestamp = itemDS.child("timestamp").getValue(Long.class);
                    if(itemName != null) item.setName(itemName);
                    if(itemShortName != null) item.setShortName(itemShortName);
                    if(itemPack != null) item.setPack(itemPack);
                    if(itemAmount != null) item.setAmount(itemAmount);
                    if(itemTimestamp != null) item.setTimestamp(itemTimestamp);

                    AreaElement areaElement = castAreaElement(itemDS.child("areaElement"));
                    if(areaElement != null) item.setAreaElement(areaElement);
                    itemArrayList.add(item);
                }

                return itemArrayList;
            }
        };
        Task<ArrayList<Item>> task = new GetDataTask<ArrayList<Item>>(query,command);
        return task;
    }

    @Override
    public Task<Area> add(Area area) {
        if (area != null){
            Log.d(this.getClass().toString(),"addArea "+ area.toString());

            String uid = AppUser.getInstance().getUid();

            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA);

            area.setId(reference.push().getKey());
            Map<String, Object> values = area.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put(area.getId(), values);
            reference.updateChildren(childUpdates);
        }
        return null;
    }

    @Override
    public Task<Area> update(Area area) {
        if (area != null){
            Log.d(this.getClass().toString(),"updateArea "+ area.toString());

            String uid = AppUser.getInstance().getUid();

            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA);

            Map<String, Object> values = area.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            childUpdates.put(area.getId(), values);
            reference.updateChildren(childUpdates);
        }
        return null;
    }

    @Override
    public Task<Area> remove(Area area) {
        if (area != null){
            Log.d(this.getClass().toString(),"removeItem "+ area.toString());

            String uid = AppUser.getInstance().getUid();
            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA);
            reference.child(area.getId()).removeValue();
            // get items of this area
            // set area of items to null
        }
        return null;
    }

    @Override
    public Task<Area> getArea(String id) {
        String uid = AppUser.getInstance().getUid();
        Query query = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA).child(id);
        Command<DataSnapshot,Area> command = new Command<DataSnapshot, Area>() {
            @Override
            public Area run(DataSnapshot areaDS) {
                Area area = new Area(areaDS.getKey());
                String name = areaDS.child("name").getValue(String.class);
                Integer height = areaDS.child("height").getValue(Integer.class);
                Integer width = areaDS.child("weight").getValue(Integer.class);
                if(name != null) area.setName(name);
                if(height != null) area.setHeight(height);
                if(width != null) area.setWidth(width);

                for(DataSnapshot subAreaDS : areaDS.child("areas").getChildren()){
                    Area subArea = new Area(subAreaDS.getKey());
                    AreaElement areaElement = castAreaElement(subAreaDS.child("areaElement"));
                    areaElement.areaId = area.getId();
                    Pair<Area,AreaElement> pair = new Pair<>(subArea,areaElement);
                    area.getAreas().add(pair);
                }

                for(DataSnapshot itemDS : areaDS.child("items").getChildren()){
                    Item item = new Item();
                    item.setId(itemDS.getKey());
                    String itemName = itemDS.child("name").getValue(String.class);
                    String itemShortName = itemDS.child("shortName").getValue(String.class);
                    String itemPack = itemDS.child("pack").getValue(String.class);
                    Float itemAmount = itemDS.child("amount").getValue(Float.class);
                    Long itemTimestamp = itemDS.child("timestamp").getValue(Long.class);
                    if(itemName != null) item.setName(itemName);
                    if(itemShortName != null) item.setShortName(itemShortName);
                    if(itemPack != null) item.setPack(itemPack);
                    if(itemAmount != null) item.setAmount(itemAmount);
                    if(itemTimestamp != null) item.setTimestamp(itemTimestamp);

                    AreaElement areaElement = castAreaElement(itemDS.child("areaElement"));
                    areaElement.areaId = area.getId();
                    if(area != null) item.setAreaElement(areaElement);
                    area.getItems().add(new Pair<Item,AreaElement>(item,item.getAreaElement()));
                }
                return area;
            }
        };
        Task<Area> task = new GetDataTask<>(query,command);
        return task;
    }

    @Override
    public Task<ArrayList<Area>> getAllAreas(){
        String uid = AppUser.getInstance().getUid();
        Query query = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA).orderByChild("timestamp");

        Command<DataSnapshot,ArrayList<Area>> command = new Command<DataSnapshot, ArrayList<Area>>() {
            @Override
            public ArrayList<Area> run(DataSnapshot dataSnapshot) {
                Log.d("Command","run: "+dataSnapshot.toString());
                ArrayList<Area> areaArrayList = new ArrayList<>();
                HashMap<String,Area> areaHashMap = new HashMap<>();
//                HashMap<String,ArrayList<Pair<Area,AreaElement>>> tempAreas = new HashMap<>();
                for (DataSnapshot areaDS : dataSnapshot.getChildren()) {
                    Area area = new Area(areaDS.getKey());
                    String name = areaDS.child("name").getValue(String.class);
                    Integer height = areaDS.child("height").getValue(Integer.class);
                    Integer width = areaDS.child("weight").getValue(Integer.class);
                    if(name != null) area.setName(name);
                    if(height != null) area.setHeight(height);
                    if(width != null) area.setWidth(width);

                    for(DataSnapshot subAreaDS : areaDS.child("areas").getChildren()){
                        Area subArea = new Area(subAreaDS.getKey());
                        AreaElement areaElement = castAreaElement(subAreaDS.child("areaElement"));
                        areaElement.areaId = area.getId();
                        Pair<Area,AreaElement> pair = new Pair<>(subArea,areaElement);
                        area.getAreas().add(pair);
                    }

                    for(DataSnapshot itemDS : areaDS.child("items").getChildren()){
                        Item item = new Item();
                        item.setId(itemDS.getKey());
                        String itemName = itemDS.child("name").getValue(String.class);
                        String itemShortName = itemDS.child("shortName").getValue(String.class);
                        String itemPack = itemDS.child("pack").getValue(String.class);
                        Float itemAmount = itemDS.child("amount").getValue(Float.class);
                        Long itemTimestamp = itemDS.child("timestamp").getValue(Long.class);
                        if(itemName != null) item.setName(itemName);
                        if(itemShortName != null) item.setShortName(itemShortName);
                        if(itemPack != null) item.setPack(itemPack);
                        if(itemAmount != null) item.setAmount(itemAmount);
                        if(itemTimestamp != null) item.setTimestamp(itemTimestamp);

                        AreaElement areaElement = castAreaElement(itemDS.child("areaElement"));
                        areaElement.areaId = area.getId();
                        if(area != null) item.setAreaElement(areaElement);
                        area.getItems().add(new Pair<Item,AreaElement>(item,item.getAreaElement()));
                        Log.d("getItems",item.toString());
                    }

                    areaArrayList.add(area);
                    areaHashMap.put(area.getId(),area);
                }

                for(Area area : areaArrayList){
                    ArrayList<Pair<Area,AreaElement>> newSubAreas = new ArrayList<>();
                    for(Pair<Area,AreaElement> pair : area.getAreas()){
                        Area newArea = areaHashMap.get(pair.first.getId());
                        AreaElement areaElement = pair.second;
                        Pair<Area,AreaElement> newPair = new Pair<>(newArea,areaElement);
                        newSubAreas.add(newPair);
                    }
                    area.setAreas(newSubAreas);
                }

                return areaArrayList;
            }
        };
        Task<ArrayList<Area>> task = new GetDataTask<ArrayList<Area>>(query,command);
        return task;
        //return new GetDataTask();
    }

    private AreaElement castAreaElement(DataSnapshot dataSnapshot){
        String elClass = dataSnapshot.child("class").getValue(String.class);
        if(elClass == null) return null;
        if(elClass.contentEquals("AreaElement")){
            String areaId = dataSnapshot.child("areaId").getValue(String.class);
            Float x = dataSnapshot.child("x").getValue(Float.class);
            Float y = dataSnapshot.child("y").getValue(Float.class);
            Integer color = dataSnapshot.child("color").getValue(Integer.class);
            AreaElement areaElement = new AreaElement();
            if(areaId != null) areaElement.areaId = areaId;
            if(x != null) areaElement.x = x;
            if(y != null) areaElement.y = y;
            if(color != null) areaElement.color = color;
            return areaElement;

        }else if(elClass.contentEquals("RectAreaElement")) {
            String areaId = dataSnapshot.child("areaId").getValue(String.class);
            Float x = dataSnapshot.child("x").getValue(Float.class);
            Float y = dataSnapshot.child("y").getValue(Float.class);
            Integer color = dataSnapshot.child("color").getValue(Integer.class);
            Integer width = dataSnapshot.child("width").getValue(Integer.class);
            Integer height = dataSnapshot.child("height").getValue(Integer.class);
            RectAreaElement areaElement = new RectAreaElement();
            if(areaId != null) areaElement.areaId = areaId;
            if(x != null) areaElement.x = x;
            if(y != null) areaElement.y = y;
            if(color != null) areaElement.color = color;
            if(width != null) areaElement.width = width;
            if(height != null) areaElement.height = height;
            return areaElement;
        }
        return null;
    }


//    /**
//     * Universal runnable method to execute
//     * @param <T> - class of method's parameter
//     * @param <R> - class of method's result
//     */
//    private interface Command<T,R>{
//        public R run(T param);
//    }

    /**
     * Universal task to get data from firebase database
     * @param <T> - result type
     */
    private class GetDataTask<T> extends Task<T>{

        boolean isComplete = false;
        boolean isSuccessfull = false;
        boolean isCanceled = false;
        Exception exception = null;

        T result;

        ArrayList<OnSuccessListener> onSuccessListeners = new ArrayList<>();
        ArrayList<OnFailureListener> onFailtureListeners = new ArrayList<>();
        ArrayList<OnCompleteListener> onCompleteListeners = new ArrayList<>();

        private class ValueListener implements ValueEventListener {
            private Task<T> task;
            Command<DataSnapshot,T> command;

            public ValueListener(Task<T> task, Command<DataSnapshot,T> command){
                this.task = task;
                this.command = command;
            }

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(command != null) result = command.run(snapshot);
                isComplete = true;
                isSuccessfull = true;
                isCanceled = false;
                for(OnSuccessListener listener : onSuccessListeners) listener.onSuccess(result);
                for(OnCompleteListener listener : onCompleteListeners) listener.onComplete(task);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                exception = error.toException();
                isComplete = true;
                isSuccessfull = false;
                isCanceled = true;
                for(OnFailureListener listener : onFailtureListeners) listener.onFailure(exception);
                for(OnCompleteListener listener : onCompleteListeners) listener.onComplete(task);
            }
        };

        /**
         * @param query - reference to data in database
         * @param command - execute on success to cast DataSnapshot to T result
         */
        public GetDataTask(Query query, Command<DataSnapshot,T> command){
//            String uid = AppUser.getInstance().getUid();
//            DatabaseReference reference = firebaseDatabase.getReference().child(PATH_USER).child(uid).child(PATH_AREA);
            query.addListenerForSingleValueEvent(new ValueListener(this,command));
        }

        @Override
        public boolean isComplete() {
            return isComplete;
        }

        @Override
        public boolean isSuccessful() {
            return isSuccessfull;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Nullable
        @Override
        public T getResult() {
            return result;
        }

        @Nullable
        @Override
        public <X extends Throwable> T getResult(@NonNull Class<X> aClass) throws X {
            return result;
        }

        @Nullable
        @Override
        public Exception getException() {
            return exception;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull Executor executor, @NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull Activity activity, @NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }
    }
}
