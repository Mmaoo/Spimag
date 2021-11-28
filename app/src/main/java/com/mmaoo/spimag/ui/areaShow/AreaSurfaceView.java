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

import com.mmaoo.spimag.Backable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppDatabase;
import com.mmaoo.spimag.model.Area;
import com.mmaoo.spimag.model.AreaDrawable;
import com.mmaoo.spimag.model.AreaElement;
import com.mmaoo.spimag.model.Item;
import com.mmaoo.spimag.model.RectAreaElement;

import java.util.ArrayList;

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
    MutableLiveData<Boolean> edited = new MutableLiveData<Boolean>();

    public ActionMode actionMode = null;

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.area_element_edit_menu,menu);
            if(action == AreaShowFragment.ACTION_ADD_ITEM) menu.removeItem(R.id.deleteElement);
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
                    Log.d(this.getClass().toString(),"action save (before): "+o.toString());
                    Log.d(this.getClass().toString(),"action save (before): "+area.toString());
                    if(o instanceof Item){
                        AppDatabase.getInstance().update((Item) o);
                    }
                    Log.d(this.getClass().toString(),"action save (after update Item): "+o.toString());
                    Log.d(this.getClass().toString(),"action save (after update Item): "+area.toString());

                    AppDatabase.getInstance().update(area);
                    Log.d(this.getClass().toString(),"action save (after update Area): "+o.toString());
                    Log.d(this.getClass().toString(),"action save (after update Area): "+area.toString());
                    actionMode.finish();
                    return true;
                case R.id.deleteElement:
                    o = getObjectByAreaElement(editedAreaElement);
                    if(o instanceof Item){
                        area.getItems().remove(o);
                    }else if(o instanceof Area){
                        area.getAreas().remove(o);
                    }
                    AppDatabase.getInstance().update(area);
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(this.getClass().toString(),"actionMode finish: "+area.toString());
            if(action == AreaShowFragment.ACTION_ADD_ITEM) action = AreaShowFragment.ACTION_SHOW;
            editedAreaElement = null;
            edited.setValue(false);
            actionMode = null;
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
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.w("GestureDetector","onLongPress");
                if (actionMode == null) {
                    Pair<Item,AreaElement> itemPair = checkItemClick(new PointF(e.getX(),e.getY()));
                    if(itemPair != null){
                        setEditedAreaElement(itemPair);
                    }else {
                        Pair<Area, AreaElement> areaPair = checkAreaClick(new PointF(e.getX(), e.getY()));
                        if (areaPair != null) {
                            editedAreaElement = areaPair.second;
                            actionMode = startActionMode(actionModeCallback);
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

    public void setEditedAreaElement(Pair<Item,AreaElement> itemPair){
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+itemPair.toString());
        if(!area.getItems().contains(itemPair)) area.getItems().add(itemPair);
        Log.d(this.getClass().toString(),"setEditedAreaElement: "+area.toString());
        editedAreaElement = itemPair.second;
        actionMode = startActionMode(actionModeCallback);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        areaBitmapPos.x = -((float)getAreaWidth()/2 - (float)getWidth()/2);
        areaBitmapPos.y = -((float)getAreaHeight()/2 - (float)getHeight()/2);

        if (areaBitmap==null){
            areaBitmap = Bitmap.createBitmap(getAreaWidth(),getAreaHeight(),Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(areaBitmap);
            canvas.drawARGB(255,255,255,255);
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
                            canvas.drawARGB(255,255,0,255);
                            canvas.drawBitmap(backgroundBitmap,areaBitmapPos.x,areaBitmapPos.y,null);
                            
                            //canvas.drawBitmap(areaBitmap,areaBitmapPos.x,areaBitmapPos.y,null);
                            //Log.w("test","can "+canvas.getWidth()+", "+canvas.getHeight()+"; "+getWidth()+", "+getHeight());

                            for (Pair<Area,AreaElement> areaPair : area.getAreas()) {
                                Area area = areaPair.first;
                                AreaElement areaElement = areaPair.second;
                                areaElement.draw(canvas, areaBitmapPos, scale, area.getName());//drawElement(canvas,aE);
                            }

                            for (Pair<Item,AreaElement> itemPair : area.getItems()) {
                                Item item = itemPair.first;
                                AreaElement areaElement = itemPair.second;
                                String name = item.getShortName();
                                if(name == null) name = item.getName();
                                areaElement.draw(canvas, areaBitmapPos, scale, name);//drawElement(canvas,aE);
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
            AreaElement areaElement = area.getItems().get(i).second;
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
        if(actionMode != null){
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
        drawTestCanvas(canvas);
        backgroundBitmap = Bitmap.createBitmap(areaBitmap);
    }

    public void setAction(int action) {
        this.action = action;
    }

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
