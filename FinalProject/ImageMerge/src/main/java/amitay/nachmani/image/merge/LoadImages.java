package amitay.nachmani.image.merge;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
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

import amitay.nachmani.image.merge.Adapters.ImagePagerAdapter;
import amitay.nachmani.image.merge.General.GeneralInfo;

public class LoadImages extends ListActivity {

    public static final String EXTRA_IMAGE = "extra_image";
    private static final String SHARE_BUTTON_TEXT = "SHARE";

    private int mStartingActivityID;
    private ImagePagerAdapter mAdapter;
    public File[] mImagePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ID of the starting activity in order to choose actions
        Bundle bundle = getIntent().getExtras();
        mStartingActivityID = bundle.getInt(GeneralInfo.ACTIVITY_KEY_BUNDLE);

        // Get all the current images saved by the application
        mImagePaths = GetImagesPaths();

        // Go over all the files and for each image in the current view load a small image

        // Create an adapter to load the images and display them in the list view
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, R.layout.activity_load_images, mImagePaths);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        // Get the reference to the row image view
        ImageView imageView = (ImageView)v.findViewById(R.id.rowImageView);

        final File photoPath = (File) getListAdapter().getItem(position);

        // Check if the image is allready displayed or not if not load it if yes collapse it
        if(imageView.getDrawable() == null) {
            // Load bitmap image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            // First only decode bounds
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);

            // Calculate inSampleSize
            options.inSampleSize = CalculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
            options.inJustDecodeBounds = false;
            final Bitmap bitmap = BitmapFactory.decodeFile(photoPath.getAbsolutePath(), options);

            // display the image
            imageView.setImageBitmap(bitmap);

            // add the share button dynamically
            Button btnShare = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);;
            params.gravity = Gravity.BOTTOM;
            btnShare.setLayoutParams(params);

            btnShare.setText(SHARE_BUTTON_TEXT);
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // according to the starting activity decide what to do
                    if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_START)
                    {

                    } else if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_SHARE) {

                        // return the selected image in a bundle to the starting activity and close this activity
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(GeneralInfo.BITMAP_BUNDLE_KEY,photoPath.getAbsolutePath());
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });

            // add button to view
            LinearLayout layout = (LinearLayout) findViewById(R.id.load_image_list_row);
            layout.addView(btnShare);

        } else {

            // Collapse image
            imageView.setImageDrawable(null);
        }
    }

    private File[] GetImagesPaths()
    {
        File imageDir = new File(GeneralInfo.APPLICATION_PATH);
        return imageDir.listFiles();
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
}
