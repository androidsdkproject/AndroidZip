package com.example.android1.androidzip;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.example.android1.androidzip.HomeActivity.Selected_item_pathList;
import static com.example.android1.androidzip.HomeActivity.TAG;

public class ZipManager {
    private static final int BUFFER = 80000;

    public boolean zip(String zipFileName) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            Log.d(TAG, "Size : " + Selected_item_pathList.size());

            for (int i = 0; i < Selected_item_pathList.size(); i++) {
                Log.v("Compress", "Adding: " + Selected_item_pathList.get(i));


                FileInputStream fi = new FileInputStream(Selected_item_pathList.get(i));
                origin = new BufferedInputStream(fi, BUFFER);


                ZipEntry entry = new ZipEntry(Selected_item_pathList.get(i).substring(Selected_item_pathList.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();


            }
            out.close();

            return true;
        } catch (Exception e) {


            Log.d(TAG, e.getMessage());

            return false;
        }


    }

    public boolean unzip(String _zipFile, String _targetLocation) {

        //create target location folder if not exist
        dirChecker(_targetLocation);

        try {
            FileInputStream fin = new FileInputStream(_zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {

                Log.v("Unzip", "Extracting: " + ze.getName());
                //create dir if required while unzipping
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
            return true;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return false;
        }
    }

    private void dirChecker(String dir) {
        File f = new File(dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

}