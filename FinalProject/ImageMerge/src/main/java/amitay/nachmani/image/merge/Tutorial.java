package amitay.nachmani.image.merge;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import amitay.nachmani.image.merge.General.TutorialStage;

public class Tutorial extends Activity {

    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    private TutorialStage mTutorialStage = TutorialStage.START_SCREEN;
    private ImageView mImageView;

    private int[] mNumberOfSwipesForStage = {1,0,2,0,1,4,1,3,0};
    private int mCurrentSwipeCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Get the image view
        mImageView = (ImageView) findViewById(R.id.tutorialImageView);
        mImageView.setImageResource(R.drawable.start_screen_shot);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tutorial, menu);
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
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                //Log.d(GeneralInfo.DEBUG_TAG,"Distance: " + (x2 - x1));
                if (Math.abs(deltaX) > MIN_DISTANCE)
                {
                    // Decide if it is a left move or a right move
                    if(x2 > x1)
                    {
                        // A left move go to previous screen
                        switch (mTutorialStage) {
                            case START_SCREEN:
                                if(mCurrentSwipeCounter > 0) { mCurrentSwipeCounter--; }
                                break;
                            case FIRST_IMAGE:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.START_SCREEN.ordinal()];
                                    mTutorialStage = TutorialStage.START_SCREEN;
                                    //mImageView.setImageResource(R.drawable.start_screen_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;
                            case FIRST_IMAGE_KEEP_DISCARD:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.FIRST_IMAGE.ordinal()];
                                    mTutorialStage = TutorialStage.FIRST_IMAGE;
                                    //mImageView.setImageResource(R.drawable.first_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }
                                break;
                            case SECOND_IMAGE:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.FIRST_IMAGE_KEEP_DISCARD.ordinal()];
                                    mTutorialStage = TutorialStage.FIRST_IMAGE_KEEP_DISCARD;
                                    //mImageView.setImageResource(R.drawable.first_image_keep_discard);
                                } else {
                                    mCurrentSwipeCounter--;
                                }
                                break;
                            case SECOND_IMAGE_KEEP_DISCARD:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.SECOND_IMAGE.ordinal()];
                                    mTutorialStage = TutorialStage.SECOND_IMAGE;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;
                            case SEGMENTATION_MARK:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.SECOND_IMAGE_KEEP_DISCARD.ordinal()];
                                    mTutorialStage = TutorialStage.SECOND_IMAGE_KEEP_DISCARD;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;
                            case SEGMENTATION_ALGORITHM:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.SEGMENTATION_MARK.ordinal()];
                                    mTutorialStage = TutorialStage.SEGMENTATION_MARK;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;
                            case MOVE_FOREGROUND_AND_EDIT:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.SEGMENTATION_ALGORITHM.ordinal()];
                                    mTutorialStage = TutorialStage.SEGMENTATION_ALGORITHM;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;
                            case SAVE_FINAL_IMAGE:
                                if(mCurrentSwipeCounter == 0)
                                {
                                    mCurrentSwipeCounter = mNumberOfSwipesForStage[TutorialStage.MOVE_FOREGROUND_AND_EDIT.ordinal()];
                                    mTutorialStage = TutorialStage.MOVE_FOREGROUND_AND_EDIT;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter--;
                                }

                                break;

                        }

                    } else if(x1 > x2) {

                        // A right move go to next screen
                        switch (mTutorialStage) {
                            case START_SCREEN:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.FIRST_IMAGE;
                                    //mImageView.setImageResource(R.drawable.first_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case FIRST_IMAGE:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.FIRST_IMAGE_KEEP_DISCARD;
                                    //mImageView.setImageResource(R.drawable.first_image_keep_discard);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case FIRST_IMAGE_KEEP_DISCARD:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.SECOND_IMAGE;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case SECOND_IMAGE:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.SECOND_IMAGE_KEEP_DISCARD;
                                    //mImageView.setImageResource(R.drawable.second_image_keep_discard);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case SECOND_IMAGE_KEEP_DISCARD:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.SEGMENTATION_MARK;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case SEGMENTATION_MARK:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.SEGMENTATION_ALGORITHM;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case SEGMENTATION_ALGORITHM:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.MOVE_FOREGROUND_AND_EDIT;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case MOVE_FOREGROUND_AND_EDIT:
                                if(mCurrentSwipeCounter == mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter = 0;
                                    mTutorialStage = TutorialStage.SAVE_FINAL_IMAGE;
                                    //mImageView.setImageResource(R.drawable.second_image_shot);
                                } else {
                                    mCurrentSwipeCounter++;
                                }

                                break;
                            case SAVE_FINAL_IMAGE:
                                if(mCurrentSwipeCounter < mNumberOfSwipesForStage[mTutorialStage.ordinal()])
                                {
                                    mCurrentSwipeCounter++;
                                } else {
                                    finish();
                                }
                                break;

                        }
                    }

                    RefreshBackgroundAccordingToStageAndSwipe();

                }

                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * RefreshBackgroundAccordingToStageAndSwipe:
     *
     * According to the current swipe number and stage decide what is the background image and what explanation to show
     *
     */
    private void RefreshBackgroundAccordingToStageAndSwipe()
    {

        TextView textView = (TextView) findViewById(R.id.screenExplination);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL | RelativeLayout.CENTER_VERTICAL);
        textView.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        switch (mTutorialStage) {
            case START_SCREEN:
                mImageView.setImageResource(R.drawable.start_screen_shot);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        textView.setText(R.string.explanation_start_screen_start);
                        break;
                    case 1:
                        textView.setText(R.string.explanation_start_screen_new);
                        break;
                    case 2:
                        textView.setText(R.string.explanation_start_screen_gallery);
                        break;
                    case 3:
                        textView.setText(R.string.explanation_start_screen_share);
                        break;
                }
                break;
            case FIRST_IMAGE:
                mImageView.setImageResource(R.drawable.first_image_shot);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        textView.setText(R.string.explanation_first_image_screen);
                        break;
                }

                break;
            case FIRST_IMAGE_KEEP_DISCARD:
                mImageView.setImageResource(R.drawable.first_image_keep_discard);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_first_image_keep_discard_screen_save_file);
                        break;
                    case 1:
                        textView.setText(R.string.explanation_first_image_keep_discard_screen_save);
                        break;
                    case 2:
                        textView.setText(R.string.explanation_first_image_keep_discard_screen_retake);
                        break;
                }

                break;
            case SECOND_IMAGE:
                mImageView.setImageResource(R.drawable.second_image_shot);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        textView.setText(R.string.explanation_second_image_screen);
                        break;
                }

                break;
            case SECOND_IMAGE_KEEP_DISCARD:
                mImageView.setImageResource(R.drawable.second_image_keep_discard_image);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        textView.setText(R.string.explanation_second_image_keep_discard_screen_save);
                        break;
                    case 1:
                        textView.setText(R.string.explanation_second_image_keep_discard_screen_retake);
                        break;
                }

                break;
            case SEGMENTATION_MARK:
                mImageView.setImageResource(R.drawable.second_image_keep_discard);

                switch (mCurrentSwipeCounter) {

                    /*
                    case 0:
                        textView.setText(R.string.explanation_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        break;
                        */
                    case 0:
                        mImageView.setImageResource(R.drawable.keep_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_marking_keep);
                        break;
                    case 1:
                        mImageView.setImageResource(R.drawable.discard_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_marking_discard);
                        break;
                    case 2:
                        mImageView.setImageResource(R.drawable.undo_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_marking_undo);
                        break;
                    case 3:
                        mImageView.setImageResource(R.drawable.discard_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        textView.setText(R.string.explanation_marking_done);
                        break;
                    case 4:
                        mImageView.setImageResource(R.drawable.discard_marking);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_marking_advice);
                        break;
                }

                break;
            case SEGMENTATION_ALGORITHM:

                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                switch (mCurrentSwipeCounter) {
                    case 0:
                        mImageView.setImageResource(R.drawable.algorithm_0);
                        textView.setText(R.string.explanation_algorithm);
                        break;
                    case 1:
                        mImageView.setImageResource(R.drawable.algorithm_50);
                        textView.setText(R.string.explanation_algorithm_50);
                        break;
                }

                break;
            case MOVE_FOREGROUND_AND_EDIT:
                mImageView.setImageResource(R.drawable.edit_screen_start);
                switch (mCurrentSwipeCounter) {
                    /*case 0:
                        textView.setText(R.string.explanation_move_and_erase);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        break;
                        */
                    case 0:
                        mImageView.setImageResource(R.drawable.edit_screen_start);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_move_and_erase_erase);
                        break;
                    case 1:
                        mImageView.setImageResource(R.drawable.edit_screen_erase);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_move_and_erase_undo);
                        break;
                    case 2:
                        mImageView.setImageResource(R.drawable.edit_screen_move);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        textView.setText(R.string.explanation_move_and_erase_move);
                        break;
                    case 3:
                        mImageView.setImageResource(R.drawable.edit_screen_move);
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        textView.setText(R.string.explanation_move_and_erase_done);
                        break;
                }
                break;
            case SAVE_FINAL_IMAGE:
                mImageView.setImageResource(R.drawable.save_screen);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

                switch (mCurrentSwipeCounter) {
                    case 0:
                        textView.setText(R.string.explanation_save);
                        break;
                }
                break;
        }

        textView.setLayoutParams(params);
    }

}

