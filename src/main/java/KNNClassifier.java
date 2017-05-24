import org.opencv.core.Mat;

import java.util.*;
import java.util.stream.Stream;

public class KNNClassifier extends AbstractClassifier {

    final private short numberOfNeighbours;
    final private VariantModel variantModel;

    public KNNClassifier(NormalizedTrainingSet trainingSet, VariantModel variantModel, short numberOfNeighbours) {
        super(trainingSet);
        if (numberOfNeighbours % 2 == 0) {
            throw new IllegalArgumentException("Number of neighbours must not be even");
        }
        this.numberOfNeighbours = numberOfNeighbours;
        this.variantModel = variantModel;
    }

    @Override
    public boolean isVessel(Mat image, int x, int y) {
        final Variant analyzedVariant = new Variant(image); // There should be a cut image, not the function parameter
        final List<Double> analyzedVector = analyzedVariant.getVector(variantModel);
        StatisticUtils.normalize(analyzedVector, trainingSet.getMeans(), trainingSet.getStandardDeviations());
        final Map<TrainingVector, Double> distanceMappedTrainingVectors = new HashMap<>();

        for (TrainingVector trainingVector : trainingSet.getTrainingVectors()) {
            distanceMappedTrainingVectors.put(trainingVector, distance(analyzedVector, trainingVector.getVectorData()));
        }

        final Stream<TrainingVector> neighbouringVariants =
                trainingSet.getTrainingVectors()
                           .stream()
                           .sorted(Comparator.comparingDouble(distanceMappedTrainingVectors::get))
                           .limit(numberOfNeighbours);

        final long positiveCount = neighbouringVariants.filter(TrainingVector::isVessel).count();
        return positiveCount > (numberOfNeighbours / 2);
    }

    private double distance(List<Double> vector1, List<Double> vector2) {
        double sumBeforeRoot = 0;
        for (int attr = 0; attr < vector1.size(); ++attr) {
            sumBeforeRoot += Math.pow(vector1.get(attr) - vector2.get(attr), 2);
        }
        return Math.sqrt(sumBeforeRoot);
    }
}
