import org.opencv.core.Mat;

public abstract class AbstractClassifier {

    protected final NormalizedTrainingSet trainingSet;

    protected AbstractClassifier(NormalizedTrainingSet trainingSet) {
        this.trainingSet = trainingSet;
    }

    abstract boolean isVessel(Mat image, int x, int y);
}
