package de.fu_berlin.cdv.chasingpictures.activity;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import java.util.List;

import de.fu_berlin.cdv.chasingpictures.DebugUtilities;
import de.fu_berlin.cdv.chasingpictures.api.Picture;
import de.fu_berlin.cdv.chasingpictures.api.Place;

/**
 * @author Simon Kalt
 */
public class SlideshowTest extends ActivityInstrumentationTestCase2<Slideshow> {

    public SlideshowTest() {
        super(Slideshow.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        Place placeWithPictures = DebugUtilities.getPlaceWithPictures();
        if (placeWithPictures != null) {
            List<Picture> pictures = placeWithPictures.getPictures();

            // Set up activity intent
            Intent intent = Slideshow.createIntent(getInstrumentation().getContext(), pictures);
            setActivityIntent(intent);
        }
    }

    public void testIntentDataReceived() throws Exception {
        List<Picture> pictures = getActivity().pictures;
        assertFalse("Received no pictures from Intent", pictures == null || pictures.isEmpty());
    }
}
