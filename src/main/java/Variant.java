import java.awt.image.BufferedImage;
import java.util.List;

public class Variant {

    private BufferedImage image;

    public Variant(BufferedImage image) {
        this.image = image;
    }

    public List<Short> getVector(VariantModel model) {
        return model.getVector(image);
    }
}
