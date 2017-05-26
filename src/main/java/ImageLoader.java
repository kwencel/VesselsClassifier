import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Krzysztof Wencel
 */
public class ImageLoader extends ImageProcessor {

    public ImageLoader() {
        super(new ArrayList<>());
    }

    @SafeVarargs
    public ImageLoader(Function<Mat, Mat>... processingFunctions) {
        super(processingFunctions);
    }

    public ImageLoader(List<Function<Mat, Mat>> processingFunctions) {
        super(processingFunctions);
    }

    /**
     * Applies processing on the full image after loading it from disk
     * @param path path to the image file
     * @return loaded and processed OpenCV Mat representing the image
     */
    public Mat loadImage(String path) {
        Mat image = Imgcodecs.imread(path);
        image = applyProcessors(image);
        return image;
    }
}
