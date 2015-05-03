package org.vudroid.core;

import android.content.Context;
import android.graphics.*;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CanvasView extends View{
	Rect mr = new Rect();
    Canvas mCanvas;
    public static Canvas mdrawCanvas;
    static Bitmap metBitmap;
    public static Bitmap mdrawBitmap;
    boolean isMove;
    public static float mx;
    public static float my;
    public static float ux;
    public static float uy;
    public static Path mPath;
    public static Path uPath;
    public static Paint mPaint;
    public static Paint uPaint;
    public static float mX, mY;
    public static float uX, uY;
    public static final float TOUCH_TOLERANCE = 4;
    private String canvasID;
    private int currentIndex;
    private String savePath;
    private BaseViewerActivity baseViewerActivity = new BaseViewerActivity();
    private Boolean sORc;
    public static Boolean upIStrue;
    public String s = "";
    private String down = "";
    private String move = "";
    private String up = "";

	public CanvasView(Context context, int currentIndex, String name) {
		super(context);
		this.currentIndex = currentIndex;
		this.canvasID = name;
		// TODO Auto-generated constructor stub
	}
	
	private void setUp(){
	this.setBackgroundColor(00000000);
	savePath = canvasID + currentIndex;
	File f = new File(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+ savePath + ".jpeg");//保存路径及名字
	if(!f.exists()){
	mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
	}
	else if(f.exists()){
		
	}
    mdrawCanvas = new Canvas(mdrawBitmap);
    mPath = new Path();
    mPaint = new Paint();
    uPath = new Path();
    uPaint = new Paint();
    
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(Color.RED);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(5);
    
    uPaint.setAntiAlias(true);
    uPaint.setDither(true);
    uPaint.setColor(Color.GREEN);
    uPaint.setStyle(Paint.Style.STROKE);
    uPaint.setStrokeJoin(Paint.Join.ROUND);
    uPaint.setStrokeCap(Paint.Cap.ROUND);
    uPaint.setStrokeWidth(5);
    
    mdrawCanvas.save(Canvas.ALL_SAVE_FLAG);
	}
	
	public void showCanvas(){
        post(new Runnable() {
            @Override
			public void run() {
            	setUp();
                sORc = BaseViewerActivity.sORc;
            }
        });
	}

	    @Override
	    protected void onDraw(Canvas canvas) {
	        super.onDraw(canvas);
	            canvas.drawBitmap(mdrawBitmap, 0, 0, null);
	        canvas.drawPath(mPath, mPaint);
	        canvas.drawPath(uPath, uPaint);
	    }
	    
	    public void changeCanvasRight(int lastPage, int currentIndex){
	    	try {
	    		if(lastPage == currentIndex){
	    			saveCanvas(BaseViewerActivity.decodeService.getPageCount()-1, 50);
	    		}else{
				saveCanvas(currentIndex-1, 50);
	    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	if(!mdrawBitmap.isRecycled()){
	        	mdrawBitmap.recycle();
	        	System.gc();
	        }
	    	savePath = canvasID + currentIndex;
	    	File f = new File(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+ savePath + ".jpg");//保存路径及名字
	    	if(!f.exists()){
	    	mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
	    	}
	    	else if(f.exists()){
	    		FileInputStream savedBitmap;
				try {
					mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
					savedBitmap = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+savePath+".jpg");
		    		mdrawBitmap = BitmapFactory.decodeStream(savedBitmap).copy(Bitmap.Config.ARGB_8888, true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	        mdrawCanvas = new Canvas(mdrawBitmap);
	        mdrawCanvas.save(Canvas.ALL_SAVE_FLAG);
	        postInvalidate();
	    }
	    
	    public void changeCanvasLeft(int lastPage, int currentIndex){
	    	try {
	    		if(lastPage == currentIndex){
	    			saveCanvas(0, 50);
	    		}else{
				saveCanvas(currentIndex+1, 50);
	    		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	if(!mdrawBitmap.isRecycled()){
	        	mdrawBitmap.recycle();
	        	System.gc();
	        }
	    	savePath = canvasID + currentIndex;
	    	File f = new File(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+ savePath + ".jpg");//保存路径及名字
	    	if(!f.exists()){
	    	mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
	    	}
	    	else if(f.exists()){
	    		FileInputStream savedBitmap;
				try {
					mdrawBitmap = Bitmap.createBitmap(1000, 2000, Bitmap.Config.ARGB_8888);
					savedBitmap = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+savePath+".jpg");
		    		mdrawBitmap = BitmapFactory.decodeStream(savedBitmap).copy(Bitmap.Config.ARGB_8888, true);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	        mdrawCanvas = new Canvas(mdrawBitmap);
	        mdrawCanvas.save(Canvas.ALL_SAVE_FLAG);
	        postInvalidate();
	    }
/********************************************************************************************************************************/	    
	    public void saveCanvas(int currentIndex, int percent) throws IOException {
	    	savePath = canvasID + currentIndex;
	        File f = new File(Environment.getExternalStorageDirectory().getPath()+"/zzz/"+ savePath + ".jpg");//保存路径及名字
	        f.createNewFile();
	        FileOutputStream fOut = null;
	        try {
	                fOut = new FileOutputStream(f);        
	                } catch (FileNotFoundException e) {        
	                e.printStackTrace();
	        }
	        mdrawBitmap.compress(Bitmap.CompressFormat.PNG, percent, fOut);//这里选择PNG，为JPEG时背景为黑色。。。
	        try {
	                fOut.flush();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
	        try {
	                fOut.close();
	        	} catch (IOException e) {
	                e.printStackTrace();
	        	}
	    }
/****************************************************************************************************/
	    public void changeLayer(int x, int y) {
	        // EditText above self draw layer
	        if (mr.contains(x, y)) {

	            mdrawCanvas.drawBitmap(mdrawBitmap, 0f, 0f, null);
	            if (metBitmap != null) {
	                mdrawCanvas.drawBitmap(metBitmap, mx, my, null);
	            }
	        } else {
	            if (metBitmap != null) {
	                mdrawCanvas.drawBitmap(metBitmap, mx, my, null);
	            }
	            mdrawCanvas.drawBitmap(mdrawBitmap, 0f, 0f, null);
	        }
	        this.postInvalidate();
	    }
	    
	    private void touch_start(float x, float y) {
	        mPath.reset();
	        mPath.moveTo(x, y);
	        mX = x;
	        mY = y;
	    }

	    private void touch_move(float x, float y) {
	        float dx = Math.abs(x - mX);
	        float dy = Math.abs(y - mY);
	        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
	            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
	            mX = x;
	            mY = y;
	        }
	    }

	    private void touch_up() {
	        mPath.lineTo(mX, mY);
	        // commit the path to our offscreen
	        mdrawCanvas.drawPath(mPath, mPaint);
	        //mCanvas.drawPath(mPath, mPaint);
	        // kill this so we don't double draw
	        mPath.reset();
	    }
/**************************************************************************************/	    
	    public void uStart(float x, float y) {
	        uPath.reset();
	        uPath.moveTo(x, y);
	        uX = x;
	        uY = y;
	        postInvalidate();
	    }

	    public void uMove(float x, float y) {
	        float dx = Math.abs(x - uX);
	        float dy = Math.abs(y - uY);
	        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
	            uPath.quadTo(uX, uY, (x + uX) / 2, (y + uY) / 2);
	            uX = x;
	            uY = y;
	            postInvalidate();
	        }
	    }
	    public void uUp() {
	        uPath.lineTo(uX, uY);
	        // commit the path to our offscreen
	        mdrawCanvas.drawPath(uPath, uPaint);
	        //mCanvas.drawPath(mPath, mPaint);
	        // kill this so we don't double draw
	        uPath.reset();
	        postInvalidate();
	    }
	    
	    public void uChange(String[] rPath, String startX, String startY, String endX, String endY){
	    	uStart(Float.parseFloat(startX), Float.parseFloat(startY));
        	changeLayer((int)Float.parseFloat(startX), (int)Float.parseFloat(startY));
        	int i;
            for(i=0; i<rPath.length-1;i++){
            	uMove(Float.parseFloat(rPath[i]), Float.parseFloat(rPath[i+1]));
            	i++;
            }
            uUp();
            changeLayer((int)Float.parseFloat(endX), (int)Float.parseFloat(endY));	    	
	    }
/**************************************************************************************/
	    public boolean onTouchEvent(MotionEvent event) {
	        isMove = false;
	        float x = event.getX();
	        float y = event.getY();
	        String xyStr = "";
	        String xStr = Float.toString(x);
	        String yStr = Float.toString(y);
	        xyStr = xStr+","+yStr;
	        /*for(int i=0; i<1023; i++){
	        	star =star + "*";
	        }*/
	        //send(xyStr);
	        //if (isChange) {
	            switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	down = "down";
	            	s = "";
	            	//send(down);
					//send(xyStr);
	            	s = s+xyStr+",";
	                touch_start(x, y);	                
	                invalidate();
	                break;
	            case MotionEvent.ACTION_MOVE:
	            	move = "move";
	            	//send(move);
	            	//send(xyStr);
	            	s = s+xyStr+",";
	                isMove = true;
	                touch_move(x, y);
	                invalidate();
	                return true;
	            case MotionEvent.ACTION_UP:
	            	up = "up";
	            	s = s+"up";
	            	//send(up);
	            	send(s);
	            	
	                touch_up();
	                invalidate();
	                break;
	            }
	            if (!isMove) {
	                this.changeLayer((int) x, (int) y);
	                return true;
	            }
	        //} 
	        return super.onTouchEvent(event);
	    }
	    
	    public void send(String string){
	    	if(sORc){
	        	baseViewerActivity.serverWrite(string);
	        }else{
	        	baseViewerActivity.clientWrite(string);
	        }	 
	    }

}
