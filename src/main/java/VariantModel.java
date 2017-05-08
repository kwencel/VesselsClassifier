import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public enum VariantModel implements AbstractVariantModel {

    DIFFERENTIAL {
        @Override
        public List<Double> getVector(Mat image) {
//            byte[][] green = ImageUtils.extractGreen(image);
            // TODO Process extracted green colors and create a vector of attributes
            System.out.println("Debug");
            return new ArrayList<>();
        }
    },
    HU_MOMENTS {
        @Override
        public List<Double> getVector(Mat image) {
            final Moments moments = Imgproc.moments(image);
            final Mat huMoments = new Mat();
            Imgproc.HuMoments(moments, huMoments);
            final List<Double> vector = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                vector.add(huMoments.get(i, 0)[0]);
            }
            return vector;
        }
    }
}
