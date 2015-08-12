package amitay.nachmani.image.merge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import amitay.nachmani.image.merge.General.GeneralInfo;
import amitay.nachmani.image.merge.Share.SharePlatform;

public class ShareActivity extends Activity {

    private static final String IMAGE_MERGE_FACEBOOK_CAPTION = "Created by ImageMerge app";

    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;

    // image
    private Bitmap mSharedImage;

    // Buttons
    private ImageButton mBtnShareFacebook;
    private ImageButton mBtnShareWhatsUp;
    private ImageButton mBtnShareGmail;

    // Share platform
    private SharePlatform mSharePlatform;
    private String mSharedImagePath;
    private int mStartingActivityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Get the ID of the starting activity in order to choose actions
        Bundle bundle = getIntent().getExtras();
        mStartingActivityID = bundle.getInt(GeneralInfo.ACTIVITY_KEY_BUNDLE);
        if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_LOAD)
        {
            mSharedImagePath = getIntent().getStringExtra(GeneralInfo.BITMAP_BUNDLE_KEY);
            LoadImage();
        }
        /**************************************************** FACEBOOK ********************************************************/
        // Initialize the SDK before executing any other operations,
        // especially, if you're using Facebook UI elements.
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        mBtnShareFacebook = (ImageButton) findViewById(R.id.btnShareFacebook);
        mBtnShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSharePlatform = SharePlatform.FACEBOOK;
                if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_START) {
                    RunLoadImage();
                } else if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_LOAD) {
                    ShareFacebook();
                    finish();
                }
            }
        });
        /**************************************************** FACEBOOK ********************************************************/

        /**************************************************** WhatsUp ********************************************************/


        mBtnShareWhatsUp = (ImageButton) findViewById(R.id.btnShareWatsUP);
        mBtnShareWhatsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSharePlatform = SharePlatform.WHATSAPP;
                if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_START) {
                    RunLoadImage();
                } else if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_LOAD) {
                    ShareWhatsAPP();
                    finish();
                }
            }
        });
        /**************************************************** WhatsUp ********************************************************/

        /**************************************************** GMAIL ********************************************************/


        mBtnShareGmail = (ImageButton) findViewById(R.id.btnShareGmail);
        mBtnShareGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSharePlatform = SharePlatform.GMAIL;
                if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_START) {
                    RunLoadImage();
                } else if(mStartingActivityID == GeneralInfo.ACTIVITY_ID_LOAD) {
                    ShareGmail();
                    finish();
                }
            }
        });
        /**************************************************** GMAIL ********************************************************/


    }


    /**
     * RunLoadImage:
     *
     * Start the load image activity inorder to get the chosen image the user wants to share
     *
     */
    private void RunLoadImage()
    {
        // start load file activity for result in order to get the choosen image
        Intent intent = new Intent(getApplicationContext(), LoadImages.class);
        Bundle bundleForLoadImagesActivity = new Bundle();
        bundleForLoadImagesActivity.putInt(GeneralInfo.ACTIVITY_KEY_BUNDLE, GeneralInfo.ACTIVITY_ID_SHARE);
        intent.putExtras(bundleForLoadImagesActivity);

        startActivityForResult(intent, GeneralInfo.ACTIVITY_ID_SHARE);
    }

    /**
     * ShareGmail:
     *
     * Opens an intent for Gmail with the chosen image
     */
    private void ShareGmail() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setData(Uri.parse("mailto:"));
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(this, mSharedImage));
        shareIntent.setType("image/jpeg");
        shareIntent.setPackage("com.google.android.gm");
        startActivity(shareIntent);
    }

    /**
     * ShareWhatsAPP:
     *
     * Opens an intent for whatsapp with the chosen image
     */
    private void ShareWhatsAPP() {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(this, mSharedImage));
        shareIntent.setType("image/jpeg");
        shareIntent.setPackage("com.whatsapp");
        startActivity(shareIntent);

    }

    /**
     * ShareFacebook:
     *
     * login to facebook and posts the desired image on pepole wall
     */
    private void ShareFacebook() {

        // First do a login to facebook

        //this loginManager helps you eliminate adding a LoginButton to your UI
        mLoginManager = LoginManager.getInstance();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        mLoginManager.logInWithPublishPermissions(this, permissionNeeds);

        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                // On success share the image on facebook
                SharePhotoToFacebook();
            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                System.out.println("onError");
            }
        });


    }


    /**
     * SharePhotoToFacebook:
     *
     * create a photo share from the chosen image and shares it on facebook
     */
    private void SharePhotoToFacebook() {

        //Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(mSharedImage)
                .setCaption(IMAGE_MERGE_FACEBOOK_CAPTION)
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();

        ShareApi.share(content, null);

    }


    /**
     * onActivityResult:
     *
     * according to the returned request code share data or finilaize facebook login
     *
     * @param requestCode
     * @param responseCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        if(requestCode == GeneralInfo.ACTIVITY_ID_SHARE)
        {
            if(responseCode == RESULT_OK)
            {
                // get the returned image path
                mSharedImagePath = data.getStringExtra(GeneralInfo.BITMAP_BUNDLE_KEY);
                LoadImage();

                switch(mSharePlatform) {
                    case FACEBOOK:
                        ShareFacebook();
                        break;
                    case WHATSAPP:
                       ShareWhatsAPP();
                        break;
                    case GMAIL:
                        ShareGmail();
                        break;
                    default:
                        break;
                }
            }

        } else {
            super.onActivityResult(requestCode, responseCode, data);
            mCallbackManager.onActivityResult(requestCode, responseCode, data);
        }
    }

    /**
     * LoadImage:
     *
     * Loads the image we got from the load image activity
     */
    private void LoadImage()
    {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        mSharedImage = BitmapFactory.decodeFile(mSharedImagePath, options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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

    /**
     * getImageUri:
     *
     * convert the bitmap to URI
     *
     * @param inContext
     * @param inImage
     * @return
     */
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
