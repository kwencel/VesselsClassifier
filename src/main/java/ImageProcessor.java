import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Krzysztof Wencel
 */
public class ImageProcessor {
    protected final List<Function<Mat, Mat>> processors;

    @SafeVarargs
    protected ImageProcessor(Function<Mat, Mat>... processingFunctions) {
        List<Function<Mat, Mat>> functions = Arrays.asList(processingFunctions);
        if (functions.contains(null)) {
            this.processors = new ArrayList<>();
        } else {
            this.processors = functions;
        }
    }

    protected ImageProcessor(List<Function<Mat, Mat>> processingFunctions) {
        this.processors = processingFunctions;
    }

    /**
     * Applies postprocessing functions to the image. The parameter is modified by reference.
     * @param image image to process
     */
    protected Mat applyProcessors(Mat image) {
        for (Function<Mat, Mat> processor : processors) {
            image = processor.apply(image);
        }
        return image;
    }
}
