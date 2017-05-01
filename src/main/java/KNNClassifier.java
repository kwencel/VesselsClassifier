import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.opencv.core.Mat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KNNClassifier extends AbstractClassifier {

    final private short numberOfNeighbours;

    public KNNClassifier(NormalizedTrainingSet trainingSet, short numberOfNeighbours) {
        super(trainingSet);
        if (numberOfNeighbours % 2 == 0) {
            throw new IllegalArgumentException("Number of neighbours must not be even");
        }
        this.numberOfNeighbours = numberOfNeighbours;
    }

    @Override
    public boolean isVessel(Mat image, int x, int y) {
        // TODO Generate a small square surrounding (x, y) and pass it to the Variant constructor
        final Variant analyzedVariant = new Variant(image); // There should be a cut image, not the function parameter
        final List<Double> analyzedVector = analyzedVariant.getVector(VariantModel.DIFFERENTIAL);
        StatisticUtils.normalize(analyzedVector, trainingSet.getMeans(), trainingSet.getStandardDeviations());
        final BiMap<TrainingVector, Double> distanceMappedTrainingVectors = HashBiMap.create();

        for (TrainingVector trainingVector : trainingSet.getTrainingVectors()) {
            distanceMappedTrainingVectors.put(trainingVector, distance(analyzedVector, trainingVector.getVectorData()));
        }

        final Set<TrainingVector> neighbouringVariants
                = trainingSet.getTrainingVectors()
                        .stream()
                        .map(distanceMappedTrainingVectors::get)
                        .sorted()
                        .limit(numberOfNeighbours)
                        .map(d -> distanceMappedTrainingVectors.inverse().get(d))
                        .collect(Collectors.toSet());

        final long positiveCount = neighbouringVariants.stream().filter(TrainingVector::isVessel).count();
        return positiveCount > (numberOfNeighbours / 2);
    }

    private double distance(List<Double> vector1, List<Double> vector2) {
        int sumBeforeRoot = 0;
        for (int attr = 0; attr < vector1.size(); ++attr) {
            sumBeforeRoot += Math.pow(vector1.get(attr) - vector2.get(attr), 2);
        }
        return Math.sqrt(sumBeforeRoot);
    }
}
