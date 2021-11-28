package com.mmaoo.spimag.ui.areaShow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.mmaoo.spimag.model.AreaDrawable;
import com.mmaoo.spimag.model.AreaElement;
import com.mmaoo.spimag.model.RectAreaElement;

import java.util.ArrayList;

public class AreaSurfaceView2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder holder;
    private Thread thread;
    private boolean threadRunning = false;
    private Object barrier = new Object();

    private Bitmap areaBitmap;
    private Bitmap backgroundBitmap;

    int width = 512*2;
    int height = 512*2;
    float scale = 1;

    PointF areaBitmapPos = new PointF(0,0);
    PointF lastClickPos = new PointF();

    public ArrayList<AreaElement> areaElements;

    public AreaSurfaceView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

        areaBitmapPos.x = -((float)width/2 - (float)getWidth()/2);
        areaBitmapPos.y = -((float)height/2 - (float)getHeight()/2);

        if (areaBitmap==null){
            areaBitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(areaBitmap);
            canvas.drawARGB(255,255,255,255);
            drawTestCanvas(canvas);
            backgroundBitmap = Bitmap.createBitmap(areaBitmap);
        }

        if(areaElements ==null) areaElements = new ArrayList<AreaElement>();
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
                            if(areaElements !=null) {
                                for (AreaDrawable aE : areaElements) {
                                    //Log.w("test", "aE: " + aE.toString());
                                    aE.draw(canvas, areaBitmapPos, scale,null);//drawElement(canvas,aE);
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
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.w("AreaSurfaceView","OnLongPress");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lastClickPos.set(event.getX(),event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                //Log.d("test","ACTION_MOVE, aBp: "+areaBitmapPos.x+", "+areaBitmapPos.y+"; lCp "+lastClickPos.x+", "+lastClickPos.y+"; event: "+event.getX()+", "+event.getY());


                int i;
                for(i= areaElements.size()-1; i>=0; i--){
                    RectAreaElement aE = (RectAreaElement) areaElements.get(i);
                    //Log.w("test","xywh "+aE.x+" "+aE.y+"; "+aE.x+aE.width+" "+aE.y+aE.height);
                    if(event.getX() >= aE.x+areaBitmapPos.x && event.getX() <= aE.x+aE.width+areaBitmapPos.x && event.getY() >= aE.y+areaBitmapPos.y && event.getY() <= aE.y+aE.height+areaBitmapPos.y){
                        //Log.w("test","przesun element "+aE.x+" "+aE.y+"; "+aE.x+aE.width+" "+aE.y+aE.height);
                        aE.x += event.getX()-lastClickPos.x;
                        aE.y += event.getY()-lastClickPos.y;
                        lastClickPos.set(event.getX(), event.getY());
                        break;
                    }
                }
                Log.w("test","i="+i);
                if(i<0) {
                    areaBitmapPos.x += event.getX() - lastClickPos.x;
                    areaBitmapPos.y += event.getY() - lastClickPos.y;
                    lastClickPos.set(event.getX(), event.getY());
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void setElements(ArrayList<AreaElement> AreaElements){
        this.areaElements = AreaElements;
//        Paint paint = new Paint();
//        for (RectAreaElement aE : rectAreaElements) {
//            paint.setColor(aE.color);
//            synchronized (barrier) {
//                canvas.drawRect(aE.x, aE.y, aE.x + aE.width, aE.y + aE.height, paint);
//            }
//        }
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


}
