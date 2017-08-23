package com.example.android1.androidzip.Dropbox;

/**
 * Created by Android1 on 8/3/2017.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.example.android1.androidzip.Dropbox.DropboxActivity.dropboxprogress;
import static com.example.android1.androidzip.Dropbox.DropboxActivity.filename;
import static com.example.android1.androidzip.Dropbox.DropboxActivity.uploadpath;

public class UploadFileToDropbox extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private String path;
    private Context context;

    public UploadFileToDropbox(Context context, DropboxAPI<?> dropbox,
                               String path) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.path = path;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        File file = new File(uploadpath);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            dropbox.putFile(path + filename, fileInputStream, file.length(), null, null);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File Uploaded Sucesfully!",
                    Toast.LENGTH_LONG).show();
            dropboxprogress.dismiss();
        } else {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG)
                    .show();
        }
    }
}