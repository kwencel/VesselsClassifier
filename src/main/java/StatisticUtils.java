import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            means.add(sum / numberOfAttributes);
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
            standardDeviations.add(Math.sqrt(sumOfSquaredDeviations / numberOfAttributes));
        }
        return standardDeviations;
    }

    public static double computeMeanSquaredError(Mat referenceMask, Mat resultMask) {
        int nominator = 0;
        int denominator = 0;
        for (int y = 0; y < referenceMask.height(); ++y) {
            for (int x = 0; x < referenceMask.width(); ++x) {
                nominator += Math.pow(ImageUtils.isWhiteInt(referenceMask.get(y, x)) - ImageUtils.isWhiteInt(resultMask.get(y, x)), 2);
                ++denominator;
            }
        }
        return (double) nominator / denominator;
    }

    public static ConfusionMatrix computeConfusionMatrix(Mat referenceMask, Mat resultMask) {
        int truePositiveAmount = 0;
        int trueNegativeAmount = 0;
        int falsePositiveAmount = 0;
        int falseNegativeAmount = 0;

        for (int y = 0; y < referenceMask.height(); ++y) {
            for (int x = 0; x < referenceMask.width(); ++x) {
                boolean referenceIsVessel = ImageUtils.isWhite(referenceMask.get(y,x));
                boolean resultIsVessel = ImageUtils.isWhite(resultMask.get(y,x));
                if (referenceIsVessel && resultIsVessel) {
                    ++truePositiveAmount;
                } else if (!referenceIsVessel && !resultIsVessel) {
                    ++trueNegativeAmount;
                } else if (referenceIsVessel) {
                    ++falseNegativeAmount;
                } else {
                    ++falsePositiveAmount;
                }
            }
        }

        return new ConfusionMatrix(truePositiveAmount, trueNegativeAmount, falsePositiveAmount, falseNegativeAmount);
    }

    public static void writeStatistics(Mat referenceMask, Mat resultMask, String pathToFile) {
        final double meanSquaredError = computeMeanSquaredError(referenceMask, resultMask);
        final ConfusionMatrix confusionMatrix = computeConfusionMatrix(referenceMask, resultMask);
        final Path path = Paths.get(pathToFile);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("Mean square error ");
            writer.write(String.valueOf(meanSquaredError) + "\n");
            writer.write(confusionMatrix.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
