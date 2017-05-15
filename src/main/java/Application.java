import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

public class Application {

    private static final int SIZE = 32;
    private static final int NUMBER_OF_SAMPLES = 20;
    private static final short NUMBER_OF_NEIGHBOURS = 11;
    private static final VariantModel VARIANT_MODEL = VariantModel.HU_MOMENTS;

    public static void main(String[] args) throws Exception {
        nu.pattern.OpenCV.loadShared();
        Application app = new Application();
        app.run();
    }

    private void run() throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Provide path to folder with learing examples.");
        String line = buffer.readLine();
        final Path samplesPath = Paths.get(line, "samples");
        final File samplesFolder = samplesPath.toFile();
        if (!samplesFolder.exists()) {
            samplesFolder.mkdir();
            ImageUtils.generateSamplesForFolder(line, samplesPath.toString(), NUMBER_OF_SAMPLES, NUMBER_OF_SAMPLES, SIZE);
        }
        final File[] samples = samplesFolder.listFiles();
        System.out.println(String.format("There are total of %1d samples of size %2d",
                samples.length, SIZE * 2 + 1
        ));
        final AbstractClassifier classifier = getTrainedClassifier(samples);

        System.out.println("Provide path to image to classify.");
        line = buffer.readLine();
        final Mat image = Imgcodecs.imread(line);
        final Mat result = new Mat(image.size(), image.type(), new Scalar(0));
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                final Mat surrounding = ImageUtils.getSurroundingPixels(image, x, y, SIZE);
                if (classifier.isVessel(surrounding, surrounding.width() / 2, surrounding.height() / 2)) {
                    result.put(y, x, new double[]{255});
                }
            }
        }
        Imgcodecs.imwrite(line + "_result", result);
    }

    private AbstractClassifier getTrainedClassifier(File[] samples) {
        final List<TrainingVector> vectors = new ArrayList<>();
        for (File sample : samples) {
            final boolean isVessel = sample.getName().contains("T");
            final TrainingVariant variant = new TrainingVariant(sample.getPath(), isVessel);
            vectors.add(variant.getTrainingVector(VARIANT_MODEL));
        }
        final NormalizedTrainingSet trainingSet = new NormalizedTrainingSet(vectors);
        final AbstractClassifier classifier = new KNNClassifier(trainingSet, VARIANT_MODEL, NUMBER_OF_NEIGHBOURS);
        return classifier;
    }
}
