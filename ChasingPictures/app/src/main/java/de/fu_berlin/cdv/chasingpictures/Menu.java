package de.fu_berlin.cdv.chasingpictures;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import de.fu_berlin.cdv.chasingpictures.api.LocationTask;
import de.fu_berlin.cdv.chasingpictures.api.Picture;
import de.fu_berlin.cdv.chasingpictures.api.Place;


public class Menu extends Activity {

    public static final Location BERLIN = new Location(LocationManager.PASSIVE_PROVIDER);
    private static final int REQUEST_PICTURE_SELECTION = 0;
    private static final int MIN_TIME = 5000;
    private static final int MIN_DISTANCE = 5;

    // FIXME: Find a better place, this is currently directly at the town hall!
    static {
        BERLIN.setLatitude(52.518405);
        BERLIN.setLongitude(13.408499);
    }

    private TextView mPlaceDistance;
    private ImageView mImageNext;
    private Place place;
    private final LocationListener distanceCalculatorListener = new LocationHelper2.EasyLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updatePlaceDistance(location);
        }
    };
    private final LocationListener placeFinderListener = new LocationHelper2.EasyLocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocationHelper.stopLocationUpdates(this);
            sendLocationRequest(location);
        }
    };
    private LocationHelper2 mLocationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // find and assign view fields
        mImageNext = (ImageView) findViewById(R.id.imageNext);
        mPlaceDistance = (TextView) findViewById(R.id.menuPlaceDistance);

        // Filter the *next* image to grayscale
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        mImageNext.setColorFilter(cf);

        mLocationHelper = new LocationHelper2(this);
        Location location = mLocationHelper.getLastKnownLocation();

        if (location == null) {
            // Let's just go to Berlin
            location = BERLIN;
            // And wait for a location
            mLocationHelper.startLocationUpdates(placeFinderListener, MIN_TIME, MIN_DISTANCE);
        }

        sendLocationRequest(location);
    }

    private void sendLocationRequest(@NonNull final Location location) {
        new LocationTask(this) {
            @Override
            protected void onPostExecute(@Nullable List<Place> places) {
                if (places == null || places.isEmpty())
                    return;

                place = places.get(0);
                // Check for null, just to be sure
                if (place == null) return;

                updatePictureNext(place.getFirstPicture());
                updatePlaceDistance(location);

                // Register the listener to always show the current distance to that place
                mLocationHelper.startLocationUpdates(distanceCalculatorListener, MIN_TIME, MIN_DISTANCE);
            }
        }.execute(location);
    }

    private void updatePictureNext(final Picture picture) {
        new PictureDownloader(getCacheDir()) {
            private Bitmap pictureNextBitmap;

            @Override
            protected Void doInBackground(Picture... params) {
                super.doInBackground(params);

                File cachedFile = picture.getCachedFile();

                if (cachedFile != null) {
                    pictureNextBitmap = BitmapFactory.decodeFile(cachedFile.getPath());
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updatePictureNext(pictureNextBitmap);
            }
        }.execute(picture);
    }

    private void updatePictureNext(Bitmap pictureNextBitmap) {
        if (pictureNextBitmap != null)
            mImageNext.setImageBitmap(pictureNextBitmap);
    }

    public void goToPictureSelection(View view) {
        Intent intent = new Intent(this, PictureSelectionActivity.class);
        startActivityForResult(intent, REQUEST_PICTURE_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICTURE_SELECTION:
                if (resultCode == RESULT_OK) {
                    // This means, that the user successfully finished a search
                    // TODO: Update the picture *last* to the finished search
                    // TODO: Set the picture *next* to another photo
                }
                break;
        }
    }

    private void updatePlaceDistance(Location location) {
        if (location != null && place != null) {
            int round = Math.round(place.distanceTo(location));
            mPlaceDistance.setText(String.format("%d m", round));
        }
    }
}
