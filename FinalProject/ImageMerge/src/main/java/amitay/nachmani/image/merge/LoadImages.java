package amitay.nachmani.image.merge;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import amitay.nachmani.image.merge.Adapters.ImagePagerAdapter;
import amitay.nachmani.image.merge.General.GeneralInfo;

public class LoadImages extends ListActivity {

    public static final String EXTRA_IMAGE = "extra_image";
    private static final String SHARE_BUTTON_TEXT = "SHARE";
    private static final String IMAGE_SHARE = "image has been shared";

    private int mStartingActivityID;
    private ImagePagerAdapter mAdapter;
    public ArrayList<File> mImagePaths;
    private ImageView mImageView;
    private Button mBtnShare;
    private Button mBtnDelete;
    private Button mBtnMerge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ID of the starting activity in order to choose actions
        Bundle bundle = getIntent().getExtras();
        mStartingActivityID = bundle.getInt(GeneralInfo.ACTIVITY_KEY_BUNDLE);

        // Get all the current images saved by the application
        mImagePaths = GetImagesPaths();

        // Add the header dynamically because eit is a list activity
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.load_images_header, lv, false);
        lv.addHeaderView(header, null, false);

        // Go over all the files and for each image in the current view load a small image

        // Create an adapter to load the images and display them in the list view
        mAdapter = new ImagePagerAdapter(this, R.layout.activity_load_images, mImagePaths);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, final int position, long id) {

        // Get the reference to the row image view
        mImageView = (ImageView)v.findViewById(R.id.rowImageView);

        final File photoPath = (File) getListAdapter().getItem(position - 1);

        // Check if the image is allready displayed or not if not load it if yes collapse it
        if(mImageView.getDrawable() == null) {
            // Load bitmap image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            // First only decode bounds
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);

            // Calculate inSampleSize
            options.inSampleSize = CalculateInSampleSize(options, mImageView.getWidth(), mImageView.getHeight());
            options.inJustDecodeBounds = false;
            final Bitmap bitmap = BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);

            // display the image
            mImageView.setImageBitmap(bitmap);

            // add the share button dynamically
            mBtnShare = (Button) v.findViewById(R.id.btnShare);
            mBtnShare.setVisibility(View.VISIBLE);
            mBtnShare.setFocusable(false);
            mBtnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // according to the starting activity decide what to do
                    if (mStartingActivityID == GeneralInfo.ACTIVITY_ID_START) {

                        // Open share activity and send the selected image path
                        Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
                        Bundle bundleForSharemagesActivity = new Bundle();
                        bundleForSharemagesActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_LOAD);
                        bundleForSharemagesActivity.putString(GeneralInfo.BITMAP_BUNDLE_KEY, photoPath.getAbsolutePath());
                        intent.putExtras(bundleForSharemagesActivity);

                        startActivityForResult(intent, GeneralInfo.ACTIVITY_ID_LOAD);

                    } else if (mStartingActivityID == GeneralInfo.ACTIVITY_ID_SHARE) {

                        // return the selected image in a bundle to the starting activity and close this activity
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(GeneralInfo.BITMAP_BUNDLE_KEY, photoPath.getAbsolutePath());
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });

            if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_SHARE) { return; }

            // make delete button visible
            mBtnDelete = (Button) v.findViewById(R.id.btnDeleteImage);
            mBtnDelete.setVisibility(View.VISIBLE);
            mBtnDelete.setFocusable(false);

            mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Delete the chosen image
                    File file = new File(photoPath.getAbsolutePath());
                    boolean deleted = file.delete();

                    if(deleted)
                    {
                        // Collapse image
                        mImageView.setImageDrawable(null);

                        // Collapse button
                        mBtnShare.setVisibility(View.INVISIBLE);
                        mBtnDelete.setVisibility(View.INVISIBLE);

                        // Delete the file from the file array and update the adapter
                        mImagePaths.remove(position - 1);
                        mAdapter.UpdateFilesArray(mImagePaths);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

            // make delete button visible
            mBtnMerge = (Button) v.findViewById(R.id.btnMerge);
            mBtnMerge.setVisibility(View.VISIBLE);
            mBtnMerge.setFocusable(false);

            mBtnMerge.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Open this image as the first image in image merge activity
                    Intent intent = new Intent(getApplicationContext(), ImageMergeMainActivity.class);
                    Bundle bundleForMergeActivity = new Bundle();
                    bundleForMergeActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_LOAD);
                    bundleForMergeActivity.putString(GeneralInfo.BITMAP_BUNDLE_KEY, photoPath.getAbsolutePath());
                    intent.putExtras(bundleForMergeActivity);
                    startActivity(intent);
                    finish();
                }
            });

        } else {

            // Collapse image
            mImageView.setImageDrawable(null);

            // Collapse button
            mBtnShare.setVisibility(View.INVISIBLE);
            if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_SHARE) { return; }
            mBtnDelete.setVisibility(View.INVISIBLE);
            mBtnMerge.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * GetImagesPaths:
     *
     * Get the image paths and convert them to arraylist for the adapter
     *
     * @return
     */
    private ArrayList<File> GetImagesPaths()
    {
        ArrayList<File> files = new ArrayList<File>();

        // check if the folder exists
        if(new File(GeneralInfo.APPLICATION_PATH).exists()) {
            File imageDir = new File(GeneralInfo.APPLICATION_PATH);
            File[] filesArray = imageDir.listFiles();
            for (int i = 0; i < filesArray.length; i++) {
                files.add(filesArray[i]);
            }


        } else {

            // Create the folder
            new File(GeneralInfo.APPLICATION_PATH).mkdir();

        }
        return files;
    }

    //

    /**
     * CalculateInSampleSize:
     *
     * Given the bitmap size and View size calculate a subsampling size (powers of 2)
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    static int CalculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;	//Default subsampling size
        // See if image raw height and width is bigger than that of required view
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            //bigger
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_load_images, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        if(requestCode == GeneralInfo.ACTIVITY_ID_LOAD)
        {
            // show toast for sharing complete
            Toast.makeText(getApplicationContext(),IMAGE_SHARE,Toast.LENGTH_SHORT).show();
        }
    }
}
