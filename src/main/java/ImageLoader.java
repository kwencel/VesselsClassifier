import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ImageLoader {
    private final List<Function<Mat, Mat>> processors;

    public ImageLoader() {
        this.processors = null;
    }

    @SafeVarargs
    public ImageLoader(Function<Mat, Mat>... processingFunction) {
        this.processors = Arrays.asList(processingFunction);
    }

    /**
     * Applies processing on the full image after loading it from disk
     * @param path path to the image file
     * @return loaded and processed OpenCV Mat representing the image
     */
    public Mat loadImage(String path) {
        Mat image = Imgcodecs.imread(path);
        for (Function<Mat, Mat> processor : processors) {
            image = processor.apply(image);
        }
        return image;
    }
}
