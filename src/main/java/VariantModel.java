import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public enum VariantModel implements AbstractVariantModel {

    DIFFERENTIAL {
        @Override
        public List<Double> getVector(Mat image) {
//            byte[][] green = ImageUtils.extractGreen(image);
            // TODO Process extracted green colors and create a vector of attributes
            System.out.println("Debug");
            return new ArrayList<>();
        }
    }
}
