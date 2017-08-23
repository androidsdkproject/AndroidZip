package com.example.android1.androidzip.Dropbox;

/**
 * Created by Android1 on 8/3/2017.
 */

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

import static com.example.android1.androidzip.Dropbox.DropboxActivity.dropboxprogress;


public class ListDropboxFiles extends AsyncTask<Void, Void, ArrayList<String>> {

    private DropboxAPI<?> dropbox;
    private String path;
    private Handler handler;

    public ListDropboxFiles(DropboxAPI<?> dropbox, String path, Handler handler) {
        this.dropbox = dropbox;
        this.path = path;
        this.handler = handler;
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params) {
        ArrayList<String> files = new ArrayList<String>();
        try {
            Entry directory = dropbox.metadata(path, 1000, null, true, null);
            for (Entry entry : directory.contents) {
                files.add(entry.fileName());
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return files;
    }

    @Override
    protected void onPostExecute(ArrayList<String> result) {
        dropboxprogress.dismiss();
        Message msgObj = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putStringArrayList("data", result);
        msgObj.setData(b);
        handler.sendMessage(msgObj);

    }
}
