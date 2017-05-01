import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TrainingVariant extends Variant {

    private final boolean isVessel;

    public TrainingVariant(Mat image, boolean isVessel) {
        super(image);
        this.isVessel = isVessel;
    }

    public TrainingVariant(String filepath, boolean isVessel) {
        this(Imgcodecs.imread(filepath), isVessel);
    }

    public TrainingVariant(File file, boolean isVessel) {
        this((Imgcodecs.imread(file.getAbsolutePath())), isVessel);
    }

    public TrainingVariant(InputStream fileStream, boolean isVessel) throws IOException {
        this(ImageUtils.loadImageFromInputStream(fileStream), isVessel);
    }

    public TrainingVector getTrainingVector(VariantModel model) {
        return new TrainingVector(getVector(model), isVessel);
    }
}
