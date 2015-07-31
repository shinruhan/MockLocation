package org.ShinRH.android.mocklocation.utl;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by Shin on 7/10/15.
 *
 * Helper Class to create HandlerThread
 *
 */
public class HandlerThreadHelper extends HandlerThread {

    private volatile Handler handler;

    private HandlerThreadHelper(String name) {
        super("name");
    }
    private HandlerThreadHelper(String name , int priority) {
        super("name" , priority);
    }


    // This Heapler can ensure Looper and Handler is created before any one post any message to Hander
    public synchronized static HandlerThreadHelper createHandlerThread(String threadName) {

        HandlerThreadHelper helper = new HandlerThreadHelper(threadName);
        helper.start();

        // Wait until we have a Looper and Handler.
        synchronized (helper) {
            while (helper.handler == null) {
                try {
                    helper.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        return helper;
    }

    public synchronized static HandlerThreadHelper createHandlerThread(String threadName , int prioriry) {

        HandlerThreadHelper helper = new HandlerThreadHelper(threadName , prioriry);
        helper.start();

        // Wait until we have a Looper and Handler.
        synchronized (helper) {
            while (helper.handler == null) {
                try {
                    helper.wait();
                } catch (InterruptedException e) {
                }
            }
        }

        return helper;
    }

    @Override
    protected void onLooperPrepared() {
        synchronized (this) {
            handler = new Handler (getLooper());
            notifyAll();
        }
    }

    public Handler getHandler() {
        return handler;
    }
}
