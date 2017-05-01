import org.opencv.core.Mat;

import java.util.List;

public class Variant {

    private Mat image;

    public Variant(Mat image) {
        this.image = image;
    }

    public List<Short> getVector(VariantModel model) {
        return model.getVector(image);
    }
}
