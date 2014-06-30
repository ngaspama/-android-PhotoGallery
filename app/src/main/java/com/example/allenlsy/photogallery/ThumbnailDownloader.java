package com.example.allenlsy.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailDownloader<Token> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    protected Handler mHandler;
    Map<Token, String> requestMap =
            Collections.synchronizedMap(new HashMap<Token, String>());
    protected Handler mResponseHandler; // the handler on main thread, to do post work
    Listener<Token> mListener;

    public interface Listener<Token> { // another thread should implement this, to do post work
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    // put into inbox
    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "URL: " + url);
        requestMap.put(token, url);

        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token)
                .sendToTarget();
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { // handle message from inbox
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressWarnings("unchecked") Token token = (Token) msg.obj;
                    Log.i(TAG, "ThumbnailDownloader::handleMessage (38): got a request for url: " + requestMap.get(token));
                    handleRequest(token);

                }
            }
        };

        super.onLooperPrepared();
    }

    /**
     * real handling work: get images bytes from url, and inform main thread (UI)
     * @param token
     */
    private void handleRequest(final Token token) {
        try {
            final String url = requestMap.get(token);

            if (url == null)
                return;

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "mResponseHandler.post Runnable (84): called");
                    if (requestMap.get(token) != url)
                        return;

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloader image", ioe);
        }
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();

    }
}
