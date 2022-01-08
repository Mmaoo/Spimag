package com.mmaoo.spimag.ui.areaShow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mmaoo.spimag.Backable;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.AppStorage;
import com.mmaoo.spimag.model.Area;
import com.mmaoo.spimag.model.AreaElement;
import com.mmaoo.spimag.model.Item;

public class AreaSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable, Backable {

    private SurfaceHolder holder;
    private Thread thread;
    private boolean threadRunning = false;
    private Object barrier = new Object();

    private int action = -1;

    private GestureDetectorCompat gestureDetector;

    private Bitmap areaBitmap;
    private Bitmap backgroundBitmap;
    
//    int width = 512*2;
//    int height = 512*2;
    float scale = 1;

    PointF areaBitmapPos = new PointF(0,0);
    PointF lastClickPos = new PointF();

    private Area area;
//    public ArrayList<AreaElement> areaElements;

    AreaElement draggedElement = null;
    AreaElement editedAreaElement = null;
    AreaElement viewedElement = null;
    MutableLiveData<Boolean> edited = new MutableLiveData<Boolean>();
    MutableLiveData<Object> clickedObject = new MutableLiveData<>();

    public ActionMode actionMode = null;

    private Navigable navigable;

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        private static final int ACTION_EDIT = 0;
        private static final int ACTION_SAVED = 1;
        private static final int ACTION_DELETED = 2;
        int actionModeAction = 0;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.area_element_edit_menu,menu);
            if(action == AreaShowFragment.ACTION_ADD_ITEM) menu.removeItem(R.id.deleteElement);
            actionModeAction = ACTION_EDIT;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Object o;
            switch(item.getItemId()){
                case R.id.save:
                    o = getObjectByAreaElement(editedAreaElement);
                    if(o instanceof Item){
                        Item it = (Item) o;
                        Log.d(this.getClass().toString(),"save: item="+it.toString()+", editedAreaElement="+editedAreaElement.toString());
                        if(it.getAreaElement() != null && !it.getAreaElement().areaId.equals(editedAreaElement.areaId)){
                            Log.d(this.getClass().toString(),"remove item from old area "+it.getAreaElement().areaId);

                            AppDatabase.getInstance().getArea(it.getAreaElement().areaId).addOnSuccessListener(new OnSuccessListener<Area>() {
                                @Override
                                public void onSuccess(Area oldArea) {
                                    for(Pair<Item,AreaElement> pair : oldArea.getItems()){
                                        if(pair.first.getId().equals(it.getId())){
                                            Log.d(this.getClass().toString(),"item found in old area: "+it.toString()+", "+oldArea.toString());
                                            oldArea.getItems().remove(pair);
                                            AppDatabase.getInstance().update(oldArea);
                                        }
                                    }
                                }
                            });
                        }
                        it.setAreaElement(editedAreaElement);
                        AppDatabase.getInstance().update(it);
                    }
                    AppDatabase.getInstance().update(area);
                    actionModeAction = ACTION_SAVED;
                    actionMode.finish();
                    return true;
                case R.id.deleteElement:
                    o = getObjectByAreaElement(editedAreaElement);
                    if(o instanceof Item){
                        for(Pair<Item,AreaElement> pair : area.getItems()){
                            if(pair.first.equals(o)){
                                pair.first.setAreaElement(null);
                                AppDatabase.getInstance().update(pair.first);
                                area.getItems().remove(pair);break;
                            }
                        }
                    }else if(o instanceof Area){
                        for(Pair<Area,AreaElement> pair : area.getAreas()){
                            if(pair.first.equals(o)){ area.getAreas().remove(pair);break; }
                        }
                    }
                    AppDatabase.getInstance().update(area);
                    actionModeAction = ACTION_DELETED;
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(this.getClass().toString(),"actionMode finish: "+area.toString());
            editedAreaElement = null;
            edited.setValue(false);
            actionMode = null;
            if (action == AreaShowFragment.ACTION_ADD_ITEM && actionModeAction != ACTION_SAVED) {
                navigable.navigateUp();
            }
            if(action == AreaShowFragment.ACTION_ADD_ITEM) action = AreaShowFragment.ACTION_ADD_ITEM_SHOW;
        }
    };

    public AreaSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);

        gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("GestureDetector","onSingleTapUp");
                if (actionMode == null) {
                    Pair<Item,AreaElement> itemPair = checkItemClick(new PointF(e.getX(),e.getY()));
                    if(itemPair != null){
                        clickedObject.setValue(itemPair.first);
                    }else {
                        Pair<Area, AreaElement> areaPair = checkAreaClick(new PointF(e.getX(), e.getY()));
                        if (areaPair != null) {
                            clickedObject.setValue(areaPair.first);
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                double d = Math.sqrt((distanceX*distanceX)+(distanceY*distanceY));
//                scale *= d;
//                return true;
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.w("GestureDetector","onLongPress");
                if (actionMode == null) {
                    Pair<Item,AreaElement> itemPair = checkItemClick(new PointF(e.getX(),e.getY()));
                    if(itemPair != null){
                        setEditedAreaElementOfItem(itemPair);
                    }else {
                        Pair<Area, AreaElement> areaPair = checkAreaClick(new PointF(e.getX(), e.getY()));
                        if (areaPair != null) {
                            setEditedAreaElementOfArea(areaPair);
                        }
                    }
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    public void setEditedAreaElementOfItem(Pair<Item,AreaElement> itemPair){
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+itemPair.toString());
        //if(!area.getItems().contains(itemPair)) area.getItems().add(itemPair);
        Pair<Item,AreaElement> newPair = null;
        for(Pair<Item, AreaElement> ip : area.getItems()){
            if(ip.first.getId().equals(itemPair.first.getId())){
                Log.d(this.getClass().toString(),"setEditedAreaElementOfItem oldPair: "+ip.toString());
                newPair = ip;
                break;
            }
        }
        if(newPair == null){
            Log.d(this.getClass().toString(),"setEditedAreaElementOfItem newPair: "+itemPair.toString());
            area.getItems().add(itemPair);
            newPair = itemPair;
        }
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+area.toString());
        editedAreaElement = newPair.second;
        actionMode = startActionMode(actionModeCallback);
    }

    public void setEditedAreaElementOfArea(Pair<Area,AreaElement> areaPair){
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+areaPair.toString());
        if(!area.getAreas().contains(areaPair)) area.getAreas().add(areaPair);
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+area.toString());
        editedAreaElement = areaPair.second;
        actionMode = startActionMode(actionModeCallback);
    }

    public void setViewedAreaElement(Pair<Item,AreaElement> viewedItem){
        viewedElement = viewedItem.second;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        areaBitmapPos.x = -((float)getAreaWidth()/2 - (float)getWidth()/2);
        areaBitmapPos.y = -((float)getAreaHeight()/2 - (float)getHeight()/2);

        if (areaBitmap==null){
            areaBitmap = Bitmap.createBitmap(getAreaWidth(),getAreaHeight(),Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(areaBitmap);
            //canvas.drawARGB(255,255,255,255);
            canvas.drawColor(getResources().getColor(R.color.color_palete_3));
            drawTestCanvas(canvas);
            backgroundBitmap = Bitmap.createBitmap(areaBitmap);
        }

        resumePaint();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        pausePaint();
        threadRunning = false;
    }

    @Override
    public void run() {
        while(threadRunning && backgroundBitmap==null) {
            try {
                Thread.sleep(1000/60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(threadRunning){
            Canvas canvas = null;
            try{
                synchronized (holder){
                    if(!holder.getSurface().isValid()) continue;
                    canvas = holder.lockCanvas(null);
                    synchronized (barrier){
                        if(threadRunning){
                            //canvas.drawARGB(255,255,255,255);
                            canvas.drawColor(getResources().getColor(R.color.color_palete_3));
                            canvas.drawBitmap(backgroundBitmap,areaBitmapPos.x,areaBitmapPos.y,null);
                            
                            //canvas.drawBitmap(areaBitmap,areaBitmapPos.x,areaBitmapPos.y,null);
                            //Log.w("test","can "+canvas.getWidth()+", "+canvas.getHeight()+"; "+getWidth()+", "+getHeight());

                            if(editedAreaElement == null) {
                                for (Pair<Area, AreaElement> areaPair : area.getAreas()) {
                                    Area area = areaPair.first;
                                    AreaElement areaElement = areaPair.second;
                                    areaElement.draw(canvas, areaBitmapPos, scale, area.getName(), new AreaElement.Settings.Builder().setArea(true).build());
                                }

                                for (Pair<Item, AreaElement> itemPair : area.getItems()) {
                                    Item item = itemPair.first;
                                    AreaElement areaElement = itemPair.second;
                                    String name = item.getShortName();
                                    if (name == null) name = item.getName();
                                    areaElement.draw(canvas, areaBitmapPos, scale, name, null);
                                }
                            }else{
                                AreaElement.Settings.Builder selectedSet = (new AreaElement.Settings.Builder()).setColor(Color.RED);
                                AreaElement.Settings.Builder unselectedSet = (new AreaElement.Settings.Builder()).setColor(Color.GRAY);
                                for (Pair<Area, AreaElement> areaPair : area.getAreas()) {
                                    Area area = areaPair.first;
                                    AreaElement areaElement = areaPair.second;
                                    if(areaElement == editedAreaElement) areaElement.draw(canvas, areaBitmapPos, scale, area.getName(), selectedSet.setArea(true).build());
                                    else areaElement.draw(canvas, areaBitmapPos, scale, area.getName(), unselectedSet.setArea(true).build());
                                }

                                for (Pair<Item, AreaElement> itemPair : area.getItems()) {
                                    Item item = itemPair.first;
                                    AreaElement areaElement = itemPair.second;
                                    String name = item.getShortName();
                                    if (name == null) name = item.getName();
                                    if(areaElement == editedAreaElement) areaElement.draw(canvas, areaBitmapPos, scale, name, selectedSet.build());
                                    else areaElement.draw(canvas, areaBitmapPos, scale, name, unselectedSet.build());
                                }
                            }
                        }
                    }
                }
            }finally {
                if(canvas != null){
                    holder.unlockCanvasAndPost(canvas);
                }
            }
            try {
                Thread.sleep(1000/60);
            }catch (InterruptedException e){
                Log.e(this.getTag().toString(),"InterruptedException",e);
            }
        }
    }

    public void resumePaint(){
        thread = new Thread(this);
        threadRunning = true;
        thread.start();
    }

    public void pausePaint(){
        threadRunning = false;
    }

    public void drawTestCanvas(Canvas canvas){
        Paint paint = new Paint();
        for(int i=0;i<=canvas.getWidth();i++){
            for(int j=0;j<=canvas.getHeight();j++){
//                paint.setColor(Color.rgb((canvas.getWidth()/256)*i,0,(canvas.getHeight()/256)*j));
                paint.setColor(Color.rgb((int)(i/(canvas.getWidth()/256)),0,(int)(j/(canvas.getHeight()/256))));
                //float width = canvas.getWidth() < canvas.getHeight() ? canvas.getWidth() : canvas.getHeight();
                canvas.drawPoint(i,j,paint);
            }
        }
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        canvas.drawLine(0,0,0,canvas.getHeight(),paint);
        canvas.drawLine(0,0,canvas.getWidth(),0,paint);
        canvas.drawLine(canvas.getWidth(),canvas.getHeight()-2,0,canvas.getHeight()-2,paint);
        canvas.drawLine(canvas.getWidth()-2,canvas.getHeight(),canvas.getWidth()-2,0,paint);

    }

    public boolean performClick(){
        return super.performClick();
    }

        @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        Log.w("onTouchEvent","onTouchEvent");
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastClickPos.set(event.getX(),event.getY());
                if(editedAreaElement != null && editedAreaElement.insideArea(event.getX()-areaBitmapPos.x,event.getY()-areaBitmapPos.y))
                    draggedElement = editedAreaElement;
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d("test","ACTION_MOVE, aBp: "+areaBitmapPos.x+", "+areaBitmapPos.y+"; lCp "+lastClickPos.x+", "+lastClickPos.y+"; event: "+event.getX()+", "+event.getY());


//                int i;
//                for(i= areaElements.size()-1; i>=0; i--){
//                    RectAreaElement aE = (RectAreaElement) areaElements.get(i);
//                    //Log.w("test","xywh "+aE.x+" "+aE.y+"; "+aE.x+aE.width+" "+aE.y+aE.height);
//                    if(event.getX() >= aE.x+areaBitmapPos.x && event.getX() <= aE.x+aE.width+areaBitmapPos.x && event.getY() >= aE.y+areaBitmapPos.y && event.getY() <= aE.y+aE.height+areaBitmapPos.y){
//                        //Log.w("test","przesun element "+aE.x+" "+aE.y+"; "+aE.x+aE.width+" "+aE.y+aE.height);
//                        aE.x += event.getX()-lastClickPos.x;
//                        aE.y += event.getY()-lastClickPos.y;
//                        lastClickPos.set(event.getX(), event.getY());
//                        break;
//                    }
//                }

//                if(i<0) {
//                    areaBitmapPos.x += event.getX() - lastClickPos.x;
//                    areaBitmapPos.y += event.getY() - lastClickPos.y;
//                    lastClickPos.set(event.getX(), event.getY());
//                }


                if(draggedElement == null) {
                    areaBitmapPos.x += event.getX() - lastClickPos.x;
                    areaBitmapPos.y += event.getY() - lastClickPos.y;
                    lastClickPos.set(event.getX(), event.getY());
                }else{
                    editedAreaElement.x += event.getX()-lastClickPos.x;
                    editedAreaElement.y += event.getY()-lastClickPos.y;
                    lastClickPos.set(event.getX(), event.getY());
                }

                break;
            case MotionEvent.ACTION_UP:
                draggedElement = null;
                break;
        }
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private Pair<Item,AreaElement> checkItemClick(PointF e){
        for (int i = area.getItems().size() - 1; i >= 0; i--) {
            AreaElement areaElement = area.getItems().get(i).second;
            if (areaElement.insideArea(e.x - areaBitmapPos.x, e.y - areaBitmapPos.y)) return area.getItems().get(i);
        }
        return null;
    }

    private Pair<Area,AreaElement> checkAreaClick(PointF e){
        for (int i = area.getAreas().size() - 1; i >= 0; i--) {
            AreaElement areaElement = area.getAreas().get(i).second;
            if (areaElement.insideArea(e.x - areaBitmapPos.x, e.y - areaBitmapPos.y)) return area.getAreas().get(i);
        }
        return null;
    }

//    public void setElements(ArrayList<AreaElement> AreaElements){
//        this.areaElements = AreaElements;
////        Paint paint = new Paint();
////        for (RectAreaElement aE : rectAreaElements) {
////            paint.setColor(aE.color);
////            synchronized (barrier) {
////                canvas.drawRect(aE.x, aE.y, aE.x + aE.width, aE.y + aE.height, paint);
////            }
////        }
//    }

    @Override
    public boolean onBackPressed() {
        Log.d("testActionMode","onBackedPress"+(actionMode != null ? actionMode.toString() : "null"));
        if(actionMode != null){
            Log.d("testActionMode","first");
            if(editedAreaElement != null){
                Object o = getObjectByAreaElement(editedAreaElement);
                if(o instanceof Item){
                    area.getItems().remove(o);
                }else if(o instanceof Area){
                    area.getAreas().remove(o);
                }
            }
            boolean isCaptured = true;
            if(action == AreaShowFragment.ACTION_ADD_ITEM) isCaptured = false;
            actionMode.finish();
            return isCaptured;
        }else {
            Log.d("testActionMode","second");
            if(action == AreaShowFragment.ACTION_ADD_ITEM_SHOW) {
//                Bundle bundle = new Bundle();
//                bundle.putInt("action", AreaListFragment.ACTION_NAVIGATE_UP);
                navigable.navigateUp(null);
                return true;
            }
            return false;
        }
    }

//    public void drawElement(Canvas canvas, RectAreaElement aE){
//        Paint paint = new Paint();
//        paint.setColor(aE.color);
//        //int size = getWidth() < getHeight() ? getWidth()/4 : getHeight()/4;
//        synchronized (barrier) {
//            //canvas.drawRect(aE.x, aE.y, aE.x + size, aE.y + size, paint);
//            canvas.drawRect(aE.x, aE.y, aE.x + aE.width, aE.y + aE.height, paint);
//        }
//    }

    public int getAreaWidth(){
        return area != null ? area.getWidth() : 1024;
    }

    public void setAreaWidth(int width){
        try {
            area.setWidth(width);
        }catch (NullPointerException e) {e.printStackTrace();}
    }

    public int getAreaHeight(){
        return area != null ? area.getHeight() : 1024;
    }

    public void setAreaHeight(int height){
        try {
            area.setHeight(height);
        }catch (NullPointerException e) {e.printStackTrace();}
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;

        areaBitmap = Bitmap.createBitmap(area.getWidth(),area.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(areaBitmap);
        canvas.drawARGB(255,255,255,255);
//        drawTestCanvas(canvas);
        backgroundBitmap = Bitmap.createBitmap(areaBitmap);
        AppStorage.getInstance().getAreaBackground(area.getId()).addOnCompleteListener(new OnCompleteListener<Bitmap>() {
            @Override
            public void onComplete(@NonNull Task<Bitmap> task) {
                if(task.isSuccessful()){
                    backgroundBitmap = task.getResult();
                }else{
                    drawTestCanvas(canvas);
                    backgroundBitmap = backgroundBitmap = Bitmap.createBitmap(areaBitmap);
                }
            }
        });
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setNavigable(Navigable navigable){
        this.navigable = navigable;
    }

    public void setAreaBackgroundBitmap(Bitmap bitmap){ this.backgroundBitmap = bitmap;}

    private Object getObjectByAreaElement(AreaElement areaElement){
        for(Pair<Item,AreaElement> pair : area.getItems()){
            if(areaElement == pair.second) return pair.first;
        }
        for(Pair<Area,AreaElement> pair : area.getAreas()){
            if(areaElement == pair.second) return pair.first;
        }
        return null;
    }

}
