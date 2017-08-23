package com.example.android1.androidzip;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import static com.example.android1.androidzip.HomeActivity.mainactivityprogress;


/**
 * Created by Android1 on 8/3/2017.
 */


public class UnZipAsyncTask extends AsyncTask<Void, Void, Boolean> {

    String TAG = "UnZipAsyncTask";
    private Context context;
    private String unzip_Filepath, outputPath;

    public UnZipAsyncTask(Context context, String unzip_Filepath, String outputPath) {
        this.context = context.getApplicationContext();
        this.unzip_Filepath = unzip_Filepath;
        this.outputPath = outputPath;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        ZipManager zipManager = new ZipManager();
        if (zipManager.unzip(unzip_Filepath, outputPath)) {
            return true;
        } else {
            return false;
        }

    }


    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            mainactivityprogress.dismiss();
            Toast.makeText(context, "Succesfully Extracted files", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show();
        }
    }


}