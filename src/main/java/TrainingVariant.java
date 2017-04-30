import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TrainingVariant extends Variant {

    private final boolean isVessel;

    public TrainingVariant(BufferedImage image, boolean isVessel) {
        super(image);
        this.isVessel = isVessel;
    }

    public TrainingVariant(String filepath, boolean isVessel) throws IOException {
        this(ImageIO.read(new File(filepath)), isVessel);
    }

    public TrainingVariant(File file, boolean isVessel) throws IOException {
        this(ImageIO.read(file), isVessel);
    }

    public TrainingVariant(InputStream fileStream, boolean isVessel) throws IOException {
        this(ImageIO.read(fileStream), isVessel);
    }

    public TrainingVector getTrainingVector(VariantModel model) {
        return new TrainingVector(getVector(model), isVessel);
    }
}
