import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public enum VariantModel implements AbstractVariantModel  {

    DIFFERENTIAL {
        @Override
        public List<Short> getVector(BufferedImage image) {
            byte[][] green = ImageUtils.extractGreen(image);
            // TODO Process extracted green colors and create a vector of attributes
            System.out.println("Debug");
            return new ArrayList<>();
        }
    }
}
