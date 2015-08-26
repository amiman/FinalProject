package amitay.nachmani.image.merge.General;

import android.os.Environment;

/**
 * Created by Amitay on 20-Jul-15.
 */
public class GeneralInfo {

    // General Inofrmation for all actvities
    public static final String APPLICATION_FOLDER          = "ImageMerge";
    public static final String APPLICATION_PATH            = Environment.getExternalStorageDirectory() + "/" +"ImageMerge";

    // Debug tags
    public static final String DEBUG_TAG = "ImageMerge";

    // Byte values
    public static final byte BYTE_ZERO = (byte) 0;

    // activities id's
    public static final String ACTIVITY_KEY_BUNDLE           = "activity";
    public static final int ACTIVITY_ID_MERGE                = 4;
    public static final int ACTIVITY_ID_SHARE                = 3;
    public static final int  ACTIVITY_ID_LOAD                = 2;
    public static final int  ACTIVITY_ID_START               = 1;

    // bundels keys
    public static final String BITMAP_BUNDLE_KEY            = "bitmpReturnedKey";

}
