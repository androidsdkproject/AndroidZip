package com.example.android1.androidzip;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android1.androidzip.Dropbox.DropboxActivity;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final public static int PERMISSION_ALL = 1;
    final static int GET_CONTENT_CODE = 1;
    final static String TAG = "Home";
    public static ProgressDialog mainactivityprogress;
    public static ArrayList<String> Selected_item_pathList = null;
    String inputPath = Environment.getExternalStorageDirectory().getPath() + "/AndroidZip/";
    String outputPath = Environment.getExternalStorageDirectory().getPath() + "/AndroidUnZip/";
    ArrayList<String> filenamelist;
    ArrayList<String> zipfilelist;
    ListView listView;
    ArrayAdapter adapter;
    TextView textViewTitle;
    String unzip_Filepath = "";
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();

        if (!hasPermissions(HomeActivity.this, PERMISSIONS)) {
            Log.d(TAG, "permission");
            ActivityCompat.requestPermissions(HomeActivity.this, PERMISSIONS, PERMISSION_ALL);
        } else {
            GettingZipFolderList();
        }


    }

    public boolean hasPermissions(Context context, String... permissions) {


        if (android.os.Build.VERSION.SDK_INT >= 21 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void initUI() {
        Selected_item_pathList = new ArrayList<>();
        filenamelist = new ArrayList<>();
        zipfilelist = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listview);
        textViewTitle = (TextView) findViewById(R.id.title);
        mainactivityprogress = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case R.id.Choose_File:
                filenamelist.clear();
                Selected_item_pathList.clear();
                textViewTitle.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                selectfile();
                break;
            case R.id.Zip:
                zipfilelist.clear();
                alertGettingFileName();
                break;
            case R.id.Unzip:

                if (!unzip_Filepath.equals("")) {
                    try {
                        String extension = getExtension(unzip_Filepath);
                        String f_name = getFilename(unzip_Filepath);
                        Log.d(TAG, extension);
                        Log.d(TAG, f_name);
                        if (extension.equals(".zip")) {
                            mainactivityprogress.setMessage("Please wait");
                            mainactivityprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            mainactivityprogress.setIndeterminate(true);
                            mainactivityprogress.setProgress(0);
                            mainactivityprogress.setCancelable(false);
                            mainactivityprogress.show();
                            UnZipAsyncTask unZipAsyncTask = new UnZipAsyncTask(HomeActivity.this, unzip_Filepath, outputPath);
                            unZipAsyncTask.execute();
                            GettingZipFolderList();
                        } else {
                            makeToast("Please Select Valid File");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    makeToast("Please Select item To unzip");
                }

                break;

            case R.id.share:
                if (!unzip_Filepath.equals("")) {
                    try {
                        String extension = getExtension(unzip_Filepath);
                        String f_name = getFilename(unzip_Filepath);
                        Log.d(TAG, extension);
                        Log.d(TAG, f_name);
                        if (extension.equals(".zip")) {
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("*/*");
                            sharingIntent.putExtra(Intent.EXTRA_STREAM, Selected_item_pathList);
                            startActivity(Intent.createChooser(sharingIntent, "Share image using"));
                        } else {
                            makeToast("Please Select .Zip File");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    makeToast("Please Select item To Share");
                }


                break;

            case R.id.Clear:
                listView.clearChoices();
                break;

            case R.id.dropbox:
                Intent in = new Intent(HomeActivity.this, DropboxActivity.class);
                in.putExtra("path", unzip_Filepath);
                in.putExtra("filename", getFilename(unzip_Filepath));
                startActivity(in);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void zip(String temp_file_name) {
        if (Selected_item_pathList.isEmpty()) {
            makeToast("Please Select item To zip");
        } else {
            try {
//                Calendar c = Calendar.getInstance();
//                Log.d(TAG, "Current time => " + c.getTime());
//                SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
//                String formattedDate = df.format(c.getTime());
//                Log.d(TAG, formattedDate);

                Log.d(TAG, "size " + Selected_item_pathList.size());
                Log.d(TAG, "zip file name : " + temp_file_name);
                if (null != temp_file_name) {
                    ZipManager zipManager = new ZipManager();
                    if (zipManager.zip(inputPath + temp_file_name + ".zip")) {
                        makeToast("Succesfully Created Zip File");
                        mainactivityprogress.dismiss();
                        GettingZipFolderList();

                    } else {
                        makeToast("Please Try Again");
                        mainactivityprogress.dismiss();
                    }
                }
            } catch (Exception e) {
                mainactivityprogress.dismiss();
                Log.d(TAG, "Exception");
            }
        }
    }


    private void selectfile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), GET_CONTENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_CONTENT_CODE && resultCode == RESULT_OK
                && null != data) {
            if (null != data.getData()) {
                Log.d(TAG, data.getData().getPath());
                unzip_Filepath = data.getData().getPath();
                filenamelist.add(getFilename(unzip_Filepath));
                Log.d(TAG, "data " + data.getData().getEncodedPath());
            } else if (null != data.getClipData()) {
                Log.d(TAG, data.getClipData().getItemCount() + "");

                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    ClipData.Item item = data.getClipData().getItemAt(i);
                    Uri uri = item.getUri();
                    try {
                        Log.d(TAG, "Clip Data" + PathUtil.getPath(HomeActivity.this, uri));
                        String path = PathUtil.getPath(HomeActivity.this, uri);

                        Selected_item_pathList.add(path);
                        filenamelist.add(getFilename(path));
                    } catch (URISyntaxException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }

            }


            listView.setVisibility(View.VISIBLE);
            textViewTitle.setVisibility(View.VISIBLE);
            textViewTitle.setText("Selected Items");
            adapter = new ArrayAdapter<String>(this,
                    R.layout.androidziplistitem, filenamelist);
            listView.setAdapter(adapter);
        } else {
            GettingZipFolderList();
        }


    }

    void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));

    }

    String alertGettingFileName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
        alert.setMessage("Please Enter filename");
        final String[] file_name = new String[1];
        final EditText input = new EditText(HomeActivity.this);
        alert.setView(input);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mainactivityprogress.setMessage("Please wait");
                mainactivityprogress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mainactivityprogress.setIndeterminate(true);
                mainactivityprogress.setCancelable(false);
                mainactivityprogress.setProgress(0);
                mainactivityprogress.show();
                listView.clearChoices();
                file_name[0] = input.getEditableText().toString();
                zip(file_name[0]);


            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                file_name[0] = null;
                dialog.cancel();
                mainactivityprogress.dismiss();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();

        return file_name[0];
    }

    String getFilename(String filepath) {

        return filepath.substring(filepath.lastIndexOf("/") + 1);
    }


    public void GettingZipFolderList() {
        try {
            File file = new File(inputPath);
            File list[] = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                zipfilelist.add(list[i].getName());
            }

            listView.clearChoices();
            listView.setVisibility(View.VISIBLE);
            textViewTitle.setVisibility(View.VISIBLE);
            textViewTitle.setText("AndroidZip Folder");
            adapter = new ArrayAdapter<String>(this,
                    R.layout.androidziplistitem, zipfilelist);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            makeToast(e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exit:
                open();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void open() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Do you want to exit from Android Zip");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }
}
