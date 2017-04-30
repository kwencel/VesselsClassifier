import java.awt.image.BufferedImage;
import java.util.List;

public interface AbstractVariantModel {

    List<Short> getVector(BufferedImage image);

    String toString();
}
