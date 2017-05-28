import com.google.common.io.Files;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Application {

    private static final int SIZE = 10;
    private static final int NUMBER_OF_SAMPLES = 5;
    private static final short NUMBER_OF_NEIGHBOURS = 11;
    private static final VariantModel VARIANT_MODEL = VariantModel.HU_MOMENTS;
    private final ImageLoader imageLoader = new ImageLoader(ImageUtils::equalizeOnlyGreen);

    public static void main(String[] args) throws Exception {
        nu.pattern.OpenCV.loadShared();
        Application app = new Application();
        if (args.length != 2) {
            System.err.println("Proper arguments: <Training dir containing 'images', 'manuals' and 'masks' subdirs> <image_to_classify>");
            System.exit(1);
        }
        app.run(args);
    }

    public void run(String[] args) throws IOException, InterruptedException {
        String trainingDir = args[0];
        String workingFile = args[1];
        final Path samplesPath = Paths.get(trainingDir, "samples");
        final File samplesFolder = samplesPath.toFile();
        if (!samplesFolder.exists()) {
            boolean succeeded = samplesFolder.mkdir();
            if (!succeeded) {
                throw new RuntimeException("Could not create 'samples' directory");
            }
            System.out.println("Generating samples...");
            ImageUtils.generateSamplesForFolderParallel(trainingDir, samplesPath.toString(), imageLoader,
                                                        NUMBER_OF_SAMPLES, NUMBER_OF_SAMPLES, SIZE);
        }
        final File workingDir = new File(workingFile).getParentFile().getParentFile();
        final Path resultsPath = Paths.get(workingDir.getAbsolutePath(), "results");
        final File resultsFolder = resultsPath.toFile();
        if (!resultsFolder.exists()) {
            boolean succeeded = resultsFolder.mkdir();
            if (!succeeded) {
                throw new RuntimeException("Could not create 'results' directory");
            }
        }
        final File[] samples = samplesFolder.listFiles();
        System.out.println(String.format("There are total of %1d samples of size %2d", samples.length, SIZE * 2 + 1));
        final AbstractClassifier classifier = getTrainedClassifier(samples);

        final Mat image = imageLoader.loadImage(workingFile);
        final Mat result = new Mat(image.size(), image.type(), new Scalar(0));
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            System.out.println("Processing image " + workingFile);
            ProgressInfo progressInfo = new ProgressInfo(image.width() * image.height());
            for (int y = 0; y < image.height(); y++) {
                final int finalY = y;
                exec.submit(() -> {
                    for (int x = 0; x < image.width(); x++) {
                        final Mat surrounding = ImageUtils.getSurroundingPixels(image, x, finalY, SIZE);
                        if (classifier.isVessel(surrounding, surrounding.width() / 2, surrounding.height() / 2)) {
                            result.put(finalY, x, 255, 255, 255);
                        }
                        progressInfo.incrementWorkCount();
                    }
                });
            }
        } finally {
            exec.shutdown();
            // Wait forever for all threads
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            System.gc();
        }

        // Post-process the image
        ImageProcessor postProcessor = new ImageProcessor((Mat resultImage) -> {
            String maskPath = getCorrespondingFile(workingDir.getAbsolutePath(), (new File(workingFile)).getName(), "masks").getAbsolutePath();
            Mat mask = Imgcodecs.imread(maskPath);
            Core.min(resultImage, mask, result);
            return result;
        });

        System.out.println("Postprocessing image");
        Mat processedResult = postProcessor.applyProcessors(result);

        String resultFile = Paths.get(resultsPath.toString(), (new File(workingFile)).getName()).toString();
        System.out.println("Saving result mask " + resultFile);
        Imgcodecs.imwrite(resultFile, processedResult);

        System.out.println("Computing and saving statistics");
        String referenceManualPath = getCorrespondingFile(workingDir.getAbsolutePath(), (new File(workingFile)).getName(),
                                               "manuals").getAbsolutePath();
        Mat referenceManual = Imgcodecs.imread(referenceManualPath);
        StatisticUtils.writeStatistics(referenceManual, result,
                                       Paths.get(resultsPath.toString(), (Files.getNameWithoutExtension(workingFile) + "_stats.txt")).toString());
    }

    private AbstractClassifier getTrainedClassifier(File[] samples) {
        final List<TrainingVector> vectors = new ArrayList<>();
        for (File sample : samples) {
            final boolean isVessel = sample.getName().contains("T");
            final TrainingVariant variant = new TrainingVariant(sample.getPath(), isVessel);
            vectors.add(variant.getTrainingVector(VARIANT_MODEL));
        }
        final NormalizedTrainingSet trainingSet = new NormalizedTrainingSet(vectors);
        return new KNNClassifier(trainingSet, VARIANT_MODEL, NUMBER_OF_NEIGHBOURS);
    }

    private File getCorrespondingFile(String workingDir, String filename, String subfolder) {
        File[] images = Paths.get(workingDir, "images").toFile().listFiles();
        if (images == null) {
            throw new RuntimeException("Images folder does not exist!");
        }
        int imageIndex = -1;
        for (int i = 0; i < images.length; i++) {
            if (Objects.equals(images[i].getName(), filename)) {
                imageIndex = i;
            }
        }
        // Get the file with the same index from a different subfolder
        File[] otherFiles = Paths.get(workingDir, subfolder).toFile().listFiles();
        if (otherFiles == null) {
            throw new RuntimeException(subfolder + " folder does not exist!");
        }
        return otherFiles[imageIndex];
    }
}