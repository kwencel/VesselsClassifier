import org.opencv.core.Mat;

import java.util.List;

public interface AbstractVariantModel {

    List<Double> getVector(Mat image);

    String toString();
}
