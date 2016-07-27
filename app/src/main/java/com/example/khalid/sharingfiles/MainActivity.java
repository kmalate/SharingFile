package com.example.khalid.sharingfiles;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private Intent mResultIntent;
     // The path to the root of this app's internal storage
    private File mPrivateRootDir;
    // The path to the "images" subdirectory
    private File mImagesDir;
    //Array of files in the images directory
    File[] mImageFiles;
    //Array of filenames corresponding to the image files
    String[] mImageFilenames;
    Uri fileUri;

    Resources mResources;
    ListView fileListView;

    private void copyResourceToImageFolder(int id) {
        String path = mImagesDir.getAbsolutePath();
        File imgDir = new File(path);
        if (imgDir.mkdir() || imgDir.isDirectory()) {
            try {
                InputStream in = mResources.openRawResource(id);
                String filePath = imgDir + File.separator +  mResources.getResourceEntryName(id) + ".png";
                FileOutputStream out = new FileOutputStream(filePath);
                byte[] buff = new byte[1024];
                int read;
                try {
                    while ((read = in.read(buff)) > 0) {
                        out.write(buff, 0, read);
                    }
                }  finally {
                    in.close();
                    out.close();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private AdapterView.OnItemClickListener mMessageClickHandler = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              /*
                 * Get a File for the selected file name.
                 * Assume that the file names are in the
                 * mImageFilename array.
                 */

            File requestFile = new File(mImageFilenames[position]);
            /*
                 * Most file-related method calls need to be in
                 * try-catch blocks.
                 */
            // Use the FileProvider to get a content URI
            try {
                fileUri = FileProvider.getUriForFile(MainActivity.this, "com.example.khalid.sharingfiles.fileprovider",requestFile);
                String test = "";
            } catch (IllegalArgumentException e) {
                Log.e("File Selector", "The selected file can't be shared: " + requestFile.getName());
            }

            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Put the Uri and MIME type in the result Intent
                mResultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
                // set the result
                MainActivity.this.setResult(Activity.RESULT_OK, mResultIntent);
            } else {
                mResultIntent.setDataAndType(null, "");
                MainActivity.this.setResult(RESULT_CANCELED, mResultIntent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mResources = getResources();
       // Set up an Intent to send back to apps that request a file
        mResultIntent = new Intent("com.example.khalid.sharingfiles.ACTION_RETURN_FILE");
         // Get the files/ subdirectory of internal storage
        mPrivateRootDir = getFilesDir();
        // Get the files/images subdirectory;
        mImagesDir = new File(mPrivateRootDir, "images");
        // Get the files in the images subdirectory
        mImageFiles = mImagesDir.listFiles();
        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);

        if (mImageFiles == null || mImageFiles.length == 0) {
            copyResourceToImageFolder(R.drawable.pricelist);
            copyResourceToImageFolder(R.drawable.zeth);
        }

        mImageFiles = mImagesDir.listFiles();

        if (mImageFiles != null || mImageFiles.length > 0) {
            mImageFilenames = new String[mImageFiles.length];

            for (int i = 0; i < mImageFiles.length; i++) {
                mImageFilenames[i] = mImageFiles[i].getAbsolutePath();
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, mImageFilenames);
            fileListView = (ListView) findViewById(R.id.fileListView);
            fileListView.setAdapter(adapter);
            fileListView.setOnItemClickListener(mMessageClickHandler);
        }
    }

    public void onDoneClick(View v) {
        finish();
    }
}
