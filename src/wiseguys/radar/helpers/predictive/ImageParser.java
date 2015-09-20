package wiseguys.radar.helpers.predictive;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by Graham on 7/31/2015.
 */
public class ImageParser {
    private List<Bitmap> images;

    public ImageParser(List<Bitmap> images) {
        this.images = images;
    }

    /**
     * Add new image to the stack
     * @param newImage
     */
    public void Push(Bitmap newImage) {
        images.add(newImage);
    }

    /**
     * Remove the oldest image
     * @return the image removed
     */
    public Bitmap Pop() {
        return images.remove(0);
        //TODO: how does the effect this list? Do we get a new 0 afterwards?
    }

    public void Parse() {
        for (Bitmap image : images) {

        }

    }
}
