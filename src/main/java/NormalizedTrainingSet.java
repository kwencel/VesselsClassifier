import java.util.ArrayList;
import java.util.List;

/**
 * Represents collection of training cases after normalization.
 *
 * @author Krzysztof Tomczak
 */
public class NormalizedTrainingSet {

    private final List<TrainingVector> trainingVectors;
    private final List<Double> means;
    private final List<Double> standardDeviations;

    /**
     * Creates new NormalizedTrainingSet containing normalized copy of given training cases.
     *
     * @param trainingVectors list of training cases
     */
    public NormalizedTrainingSet(List<TrainingVector> trainingVectors) {
        this.trainingVectors = new ArrayList<>(trainingVectors);
        this.means = StatisticUtils.means(this.trainingVectors);
        this.standardDeviations = StatisticUtils.standardDeviations(this.trainingVectors, means);
        this.trainingVectors.forEach((trainingVector) -> trainingVector.normalize(means, standardDeviations));
    }

    public List<Double> getMeans() {
        return means;
    }

    public List<Double> getStandardDeviations() {
        return standardDeviations;
    }

    public List<TrainingVector> getTrainingVectors() {
        return trainingVectors;
    }

}
