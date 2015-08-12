package amitay.nachmani.image.merge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import amitay.nachmani.image.merge.General.GeneralInfo;
import amitay.nachmani.image.merge.R;

public class StartScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        // Set click listener for each button
        Button btnImageMerge = (Button) findViewById(R.id.btnImageMerge);
        Button btnLoadImage = (Button) findViewById(R.id.btnLoadImage);
        Button btnShare = (Button) findViewById(R.id.btnShare);

        btnImageMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            // Start the main merge activity
            Intent intent = new Intent(getApplicationContext(), ImageMergeMainActivity.class);
            startActivity(intent);
            }
        });

        btnLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            // Start the load images activity
            Intent intent = new Intent(getApplicationContext(), LoadImages.class);
            Bundle bundleForLoadImagesActivity = new Bundle();
            bundleForLoadImagesActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_START);
            intent.putExtras(bundleForLoadImagesActivity);
            startActivity(intent);
                
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            // Start the load images activity
            Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
            Bundle bundleForShareActivity = new Bundle();
            bundleForShareActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_START);
            intent.putExtras(bundleForShareActivity);
            startActivity(intent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_screen, menu);
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
