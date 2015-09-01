package amitay.nachmani.image.merge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import amitay.nachmani.image.merge.General.GeneralInfo;

public class StartScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        // Set click listener for each button
        Button btnTutorial = (Button) findViewById(R.id.btnTutorial);
        Button btnImageMerge = (Button) findViewById(R.id.btnImageMerge);
        Button btnLoadImage = (Button) findViewById(R.id.btnLoadImage);
        Button btnShare = (Button) findViewById(R.id.btnShare);

        btnTutorial.setBackgroundResource(R.drawable.button_selector);
        btnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start the tutorial activity
                Intent intent = new Intent(getApplicationContext(), Tutorial.class);
                startActivity(intent);
            }
        });

        btnImageMerge.setBackgroundResource(R.drawable.button_selector);
        btnImageMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start the main merge activity
                Intent intent = new Intent(getApplicationContext(), ImageMergeMainActivity.class);
                Bundle bundleForMergesActivity = new Bundle();
                bundleForMergesActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_START);
                intent.putExtras(bundleForMergesActivity);
                startActivity(intent);
            }
        });

        btnLoadImage.setBackgroundResource(R.drawable.button_selector);
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

        btnShare.setBackgroundResource(R.drawable.button_selector);
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
