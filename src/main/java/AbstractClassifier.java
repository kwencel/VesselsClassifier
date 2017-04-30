import java.awt.image.BufferedImage;
import java.util.Set;

public abstract class AbstractClassifier {

    protected final Set<TrainingVector> trainingVectors;

    protected AbstractClassifier(Set<TrainingVector> trainingVectors) {
        this.trainingVectors = trainingVectors;
    }

    abstract boolean isVessel(BufferedImage image, int x, int y);
}
