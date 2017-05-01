
import org.opencv.core.Mat;

import java.util.List;

public class Variant {

    private final Mat image;

    public Variant(Mat image) {
        this.image = image;
    }

    public List<Double> getVector(VariantModel model) {
        return model.getVector(image);
    }
}
