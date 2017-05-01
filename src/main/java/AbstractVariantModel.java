import org.opencv.core.Mat;

import java.util.List;

public interface AbstractVariantModel {

    List<Short> getVector(Mat image);

    String toString();
}
