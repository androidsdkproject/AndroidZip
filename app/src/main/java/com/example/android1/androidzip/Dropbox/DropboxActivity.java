package com.example.android1.androidzip.Dropbox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.example.android1.androidzip.HomeActivity;
import com.example.android1.androidzip.R;

import java.util.ArrayList;

public class DropboxActivity extends AppCompatActivity implements OnClickListener {
    private final static String FILE_DIR = "";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "zev9etkcg15fspl";
    private final static String ACCESS_SECRET = "w49lpx2um55hims";
    public static String uploadpath = null;
    public static String filename = null;
    public static ProgressDialog dropboxprogress;
    ListView listView;
    ArrayAdapter adapter;
    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            adapter = new ArrayAdapter<String>(DropboxActivity.this, R.layout.dropboxitem, result);
            listView.setAdapter(adapter);
        }
    };
    private DropboxAPI<AndroidAuthSession> dropbox;
    private boolean isLoggedIn;
    private Button logIn;
    private Button uploadFile;
    private Button listFiles;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        uploadpath = getIntent().getStringExtra("path");
        filename = getIntent().getStringExtra("filename");
        initUi();

    }

    private void initUi() {
        setTitle("Dropbox");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dropboxprogress = new ProgressDialog(this);
        logIn = (Button) findViewById(R.id.dropbox_login);
        logIn.setOnClickListener(this);
        uploadFile = (Button) findViewById(R.id.upload_file);
        uploadFile.setOnClickListener(this);
        listFiles = (Button) findViewById(R.id.list_files);
        listFiles.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.listview);


        loggedIn(false);
        AndroidAuthSession session;
        AppKeyPair pair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);

        SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
        String key = prefs.getString(ACCESS_KEY, null);
        String secret = prefs.getString(ACCESS_SECRET, null);

        if (key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(pair, AccessType.DROPBOX, token);
        } else {
            session = new AndroidAuthSession(pair, AccessType.DROPBOX);
        }
        dropbox = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onResume() {
        super.onResume();

        AndroidAuthSession session = dropbox.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                TokenPair tokens = session.getAccessTokenPair();
                SharedPreferences prefs = getSharedPreferences(DROPBOX_NAME, 0);
                Editor editor = prefs.edit();
                editor.putString(ACCESS_KEY, tokens.key);
                editor.putString(ACCESS_SECRET, tokens.secret);
                editor.commit();
                loggedIn(true);
            } catch (IllegalStateException e) {
                Toast.makeText(this, "Error during Dropbox authentication",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void loggedIn(boolean isLogged) {
        isLoggedIn = isLogged;
        uploadFile.setEnabled(isLogged);
        listFiles.setEnabled(isLogged);
        logIn.setText(isLogged ? "Log out" : "Log in");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dropbox_login:
                if (isLoggedIn) {
                    dropbox.getSession().unlink();
                    loggedIn(false);
                } else {
                    dropbox.getSession().startAuthentication(DropboxActivity.this);
                }

                break;
            case R.id.list_files:
                dropboxprogress.setMessage("Downloading data");
                dropboxprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dropboxprogress.setIndeterminate(true);
                dropboxprogress.setCancelable(false);
                dropboxprogress.setProgress(0);
                dropboxprogress.show();
                ListDropboxFiles list = new ListDropboxFiles(dropbox, FILE_DIR,
                        handler);
                list.execute();
                break;
            case R.id.upload_file:

                dropboxprogress.setMessage("Uploading file");
                dropboxprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dropboxprogress.setIndeterminate(true);
                dropboxprogress.setCancelable(false);
                dropboxprogress.setProgress(0);
                dropboxprogress.show();


                UploadFileToDropbox upload = new UploadFileToDropbox(this, dropbox, FILE_DIR);
                upload.execute();
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}