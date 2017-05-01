import java.util.ArrayList;
import java.util.List;

/**
 * Collection of statistical utility functions.
 *
 * @author Krzysztof Tomczak
 */
public class StatisticUtils {

    /**
     * Normalizes list of in place with given means and standard deviations. All parameters should be the same in
     * length.
     *
     * @param values             list of values to normalize
     * @param means              list of means
     * @param standardDeviations list of standard deviations
     */
    public static void normalize(List<Double> values, List<Double> means, List<Double> standardDeviations) {
        for (int i = 0; i < values.size(); i++) {
            final Double normalizedValue = normalize(values.get(i), means.get(i), standardDeviations.get(i));
            values.set(i, normalizedValue);
        }
    }

    /**
     * Normalizes value with given mean and standard deviation.
     *
     * @param value             value to normalize
     * @param mean              mean to normalize with
     * @param standardDeviation standard deviation to normalize with
     * @return normalized value
     */
    public static Double normalize(Double value, Double mean, Double standardDeviation) {
        return (value - mean) / standardDeviation;
    }

    /**
     * Calculates mean for each attribute in training cases.
     *
     * @param trainingVectors list of training cases
     * @return list of means for each attribute in training cases
     */
    public static List<Double> means(List<TrainingVector> trainingVectors) {
        final int numberOfAttributes = trainingVectors.get(0).getVectorData().size();
        final List<Double> means = new ArrayList<>(numberOfAttributes);

        for (int i = 0; i < numberOfAttributes; i++) {
            double sum = 0;
            for (TrainingVector trainingVector : trainingVectors) {
                sum += trainingVector.getVectorData().get(i);
            }
            means.set(i, sum / numberOfAttributes);
        }
        return means;
    }

    /**
     * Calculates standard deviation for each attribute in training cases.
     *
     * @param trainingVectors list of training cases
     * @param means           list of means for each attribute in training cases
     * @return list of standard deviations for each attribute in training cases
     */
    public static List<Double> standardDeviations(List<TrainingVector> trainingVectors, List<Double> means) {
        final int numberOfAttributes = trainingVectors.get(0).getVectorData().size();
        final List<Double> standardDeviations = new ArrayList<>(numberOfAttributes);

        for (int i = 0; i < numberOfAttributes; i++) {
            double sumOfSquaredDeviations = 0;
            for (TrainingVector trainingVector : trainingVectors) {
                final Double deviation = trainingVector.getVectorData().get(i) - means.get(i);
                sumOfSquaredDeviations += deviation * deviation;
            }
            standardDeviations.set(i, Math.sqrt(sumOfSquaredDeviations / numberOfAttributes));
        }
        return standardDeviations;
    }
}
