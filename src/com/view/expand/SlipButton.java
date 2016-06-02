package com.view.expand;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SlipButton extends View {    
    
    private boolean nowChoose = false;//记录当前按钮是否打开,true为打开,flase为关闭    
    private boolean onSlip = false;//记录用户是否在滑动的变量    
    private float nowX;//按下时的x,当前的x  
    private float downX;  
    private float baseX;  
    private OnChangedListener ChgLsn;    
    private Bitmap slip_btn;   
    private float centerX;  
      
    private float yes_text_width;  
    private float no_text_width;  
      
    private int btnWdith;  
    private String[] messages;  
      
    private Paint paint;  
        
    public SlipButton(Context context) {    
        super(context);     
        init();    
    }    
    
    public SlipButton(Context context, AttributeSet attrs) {    
        super(context, attrs);    
        init();    
    }    
    
    private void init() {   
        paint = new Paint();  
        paint.setAntiAlias(true);  
        paint.setColor(Color.BLACK);  
        paint.setTextSize(14);  
        setMessages(new String[]{"Yes", "No"});  
    }  
      
    public void setButtonBitmap(Bitmap bitmap) {  
            if (bitmap == null) {  
                    return;  
            }  
            slip_btn = bitmap;  
        btnWdith = slip_btn.getWidth() / 3;  
        centerX = btnWdith / 2;  
    }  
      
    public void setTextColor(int color) {  
            paint.setColor(color);  
    }  
      
    public void setTextSize(int size) {  
            paint.setTextSize(size);  
    }  
      
    public void setMessages(String[] messages) {  
            this.messages = messages;  
            yes_text_width = paint.measureText(messages[0]);  
        no_text_width = paint.measureText(messages[1]);  
    }  
      
    public void setStatusOn(boolean on) {  
            nowChoose = on;  
    }  
      
    public boolean getStatusOn() {  
            return nowChoose;  
    }  
      
    public void setOnChangedListener(OnChangedListener l) {  
        ChgLsn = l;    
    }  
        
    @Override    
    protected void onDraw(Canvas canvas) {   
        super.onDraw(canvas);  
        if (slip_btn == null) {  
                    return;  
            }  
          
        float minX = centerX;  
        float maxX = getMeasuredWidth() - centerX;  
          
        if (onSlip) {  
                nowX = baseX - (downX - nowX);  
                nowX = Math.min(Math.max(minX, nowX), maxX);  
        } else {  
                if (nowChoose) {  
                        nowX = maxX;  
                } else {  
                        nowX = minX;  
                }  
                baseX = nowX;  
        }  
            canvas.drawBitmap(slip_btn, nowX - centerX - btnWdith, 0, paint);//画出游标.  
             
        int marginX1 = (int)(btnWdith - yes_text_width) / 2;  
        canvas.drawText(messages[0], nowX - centerX - marginX1 - yes_text_width, 22, paint);  
              
        int marginX2 = (int)(btnWdith - no_text_width) / 2;  
        canvas.drawText(messages[1], nowX + centerX + marginX2, 22, paint);  
    }    
    
    
    @Override  
        public boolean onTouchEvent(MotionEvent event) {  
            if (slip_btn == null) {  
                    return false;  
            }  
              
        switch(event.getAction())//根据动作来执行代码    
        {    
        case MotionEvent.ACTION_MOVE://滑动   
                nowX = event.getX();  
                float moveX = downX - nowX;  
                if (Math.abs(moveX) > 5) {  
                        onSlip = true;  
                }  
            break;    
        case MotionEvent.ACTION_DOWN://按下    
                downX = event.getX();  
            break;    
        case MotionEvent.ACTION_UP://松开    
                boolean LastChoose = nowChoose;  
                if (onSlip) {  
                        moveX = downX - event.getX();  
                        if (nowChoose) {  
                                if ((moveX > 0) && (moveX >= centerX))  
                                        nowChoose = !nowChoose;  
                        } else {  
                                if ((moveX < 0) && (Math.abs(moveX) >= centerX))  
                                        nowChoose = !nowChoose;  
                        }   
                } else {  
                        nowChoose = !nowChoose;  
                }  
            onSlip = false;  
            if ((ChgLsn != null) && (LastChoose != nowChoose)) {  
                    if (nowChoose) {  
                            ChgLsn.onChanged(nowChoose, messages[0]);  
                    } else {  
                            ChgLsn.onChanged(nowChoose, messages[1]);  
                    }  
            }  
            break;    
        default:  
                break;   
        }    
        invalidate();//重画控件    
        return true;  
        }  
      
    public interface OnChangedListener {  
            public void onChanged(boolean on, String value);  
    }  
}  