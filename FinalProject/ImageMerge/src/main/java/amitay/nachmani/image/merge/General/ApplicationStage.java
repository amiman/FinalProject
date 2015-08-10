package amitay.nachmani.image.merge.General;

/**
 * Created by Amitay on 20-Jul-15.
 */
public enum ApplicationStage {

    INITIALIZATION,
    START,
    FIRST_IMAGE,
    SECOND_IMAGE,
    SEGMENTATION_MARK_INITIALIZATION,
    SEGMENTATION_MARK,
    SEGMENTATION_ALGORITHM,
    MOVE_FOREGROUND_AND_EDIT_INITIALIZATION,
    MOVE_FOREGROUND_AND_EDIT,
    FINALIZE;

}
