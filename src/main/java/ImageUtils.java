import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_UNCHANGED;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;

import org.opencv.imgproc.Imgproc;

public class ImageUtils {

    public static Mat loadImageFromInputStream(InputStream is) throws IOException {
        int nRead;
        byte[] data = new byte[16 * 1024];
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        byte[] bytes = buffer.toByteArray();
        return imdecode(new MatOfByte(bytes), CV_LOAD_IMAGE_UNCHANGED);
    }

    public static Mat substractMeanFilter(Mat image, int size)
    {
        Mat result = new Mat();
        Mat kernel = new Mat(size, size, CvType.CV_8SC1, new Scalar(1 / (size * size)));
        Imgproc.filter2D(image, result, -1, kernel);
        Core.subtract(image, result, result);
        Imgproc.equalizeHist(result, result);
        return result;
    }

    public static Mat opening(Mat image, int size)
    {
        Mat result = new Mat();
        Mat kernel = Imgproc.getStructuringElement(0, new Size(size, size));
        Imgproc.morphologyEx(image, result, Imgproc.MORPH_OPEN, kernel);
        kernel.release();
        return result;
    }

    public static Mat medianFilter(Mat image, int size)
    {
        Mat result = new Mat();
        Imgproc.medianBlur(image, result, size);
        return result;
    }

    public static Mat gaussianFilter(Mat image, int size, double sigma)
    {
        Mat result = new Mat();
        Imgproc.GaussianBlur(image, result, new Size(size, size), sigma);
        return result;
    }

    public static Mat backgroundHomogenization(Mat image)
    {
        List<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        Mat green = channels.get(1);

        Mat open = opening(green, 11);
        Mat median = medianFilter(open, 5);
        Mat gaussian = gaussianFilter(median, 9, 1.8);
        Mat shadeCorrected = substractMeanFilter(gaussian, 69);

        Mat result = new Mat();
        channels.set(1, shadeCorrected);
        Core.merge(channels, result);
        return result;
    }
    
    public static Mat equalizeOnlyGreen(Mat image) {
        List<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        Mat green = channels.get(1);
        Imgproc.equalizeHist(green, green);
        Mat equalized = new Mat();
        Core.merge(channels, equalized);
        return equalized;
    }

    public static Mat equalizeHistogram(Mat image) {
        List<Mat> channels = new ArrayList<>();
        Mat img_hist_equalized = new Mat();
        Imgproc.cvtColor(image, img_hist_equalized, Imgproc.COLOR_BGR2YCrCb); //change the color image from BGR to YCrCb format
        Core.split(img_hist_equalized, channels); //split the image into channels
        Imgproc.equalizeHist(img_hist_equalized, img_hist_equalized); //equalize histogram on the 1st channel (Y)
        Core.merge(channels,img_hist_equalized); //merge 3 channels including the modified 1st channel into one image
        Imgproc.cvtColor(img_hist_equalized, img_hist_equalized, Imgproc.COLOR_YCrCb2BGR); //change the color image from YCrCb to BGR format (to
        return img_hist_equalized;
    }

    /**
     * Extract green channel from image.
     *
     * @param image BGR image
     * @return single channel image with green channel from input image as intensity
     */
    public static Mat extractGreen(Mat image) {
        List<Mat> bgr = new ArrayList<>();
        Core.split(image, bgr);
        return bgr.get(1);
    }

    /**
     * NOT READY YET - DON'T USE
     */
    public static int[][] convertToArray(BufferedImage image) {
        // Assuming TYPE_INT_BGR - needs modifying for this project
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    /**
     * Constructs new image from square surroundings of pixel. Surroundings outside image borders are replaced with
     * black.
     *
     * @param image input image
     * @param x     horizontal position of pixel, should be inside image borders
     * @param y     vertical position of pixel, should be inside image borders
     * @param size  how many pixels to take from each side, should be nonnegative
     * @return new image of size NxN, where N = 2*size+1
     */
    public static Mat getSurroundingPixels(Mat image, int x, int y, int size) {
        final int width = image.width();
        final int height = image.height();
        if (x < 0 || x >= width) {
            throw new IllegalArgumentException("Invalid horizontal position: " + x);
        }
        if (y < 0 || y >= height) {
            throw new IllegalArgumentException("Invalid vertical position: " + y);
        }
        if (size < 0) {
            throw new IllegalArgumentException("Negative size: " + size);
        }
        int paddingLeft, paddingRight, paddingTop, paddingBottom;
        int startX, startY, endX, endY;
        if (size > x) {
            startX = 0;
            paddingLeft = size - x;
        } else {
            startX = x - size;
            paddingLeft = 0;
        }
        if (size > y) {
            startY = 0;
            paddingTop = size - y;
        } else {
            startY = y - size;
            paddingTop = 0;
        }
        if (x + size >= width) {
            endX = width;
            paddingRight = size + x - width + 1;
        } else {
            endX = x + size + 1;
            paddingRight = 0;
        }
        if (y + size >= height) {
            endY = height;
            paddingBottom = size + y - height + 1;
        } else {
            endY = y + size + 1;
            paddingBottom = 0;
        }
        final Scalar black = new Scalar(0, 0, 0);
        Mat submat = image.submat(startY, endY, startX, endX);
        if (paddingBottom == 0 && paddingLeft == 0 && paddingRight == 0 && paddingTop == 0) {
            return submat; //If surrounding is entierly contained inside image borders
        }
        final Mat result = new Mat();
        Core.copyMakeBorder(submat, result, paddingTop, paddingBottom, paddingLeft, paddingRight, Core.BORDER_CONSTANT, black);
        return result;
    }

    /**
     * Randomly samples image specified number of times.
     *
     * @param image    input image
     * @param mask     mask denoting region of interest
     * @param manual   mask denoting classes
     * @param positive should samples contain positive cases
     * @param howMany  how many samples to take, should be nonnegative
     * @param size     how many pixels to take from each side of sample, should be nonnegative
     * @return list of three lists with samples
     */
    public static List<List<Mat>> sampleImage(Mat image, Mat mask, Mat manual, boolean positive, int howMany, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative size: " + size);
        }
        if (howMany < 0) {
            throw new IllegalArgumentException("Negative number of samples: " + howMany);
        }
        final List<Point> points = new ArrayList<>();
        final double[] white = {255, 255, 255};
        for (int y = 0; y < image.height(); y++) {
            for (int x = 0; x < image.width(); x++) {
                if (Arrays.equals(mask.get(y, x), white)) {
                    if (Arrays.equals(manual.get(y, x), white)) {
                        if (positive) {
                            points.add(new Point(x, y));
                        }
                    } else {
                        if (!positive) {
                            points.add(new Point(x, y));
                        }
                    }
                }
            }
        }
        Random rng = new Random();
        final List<List<Mat>> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            result.add(new ArrayList<>());
        }
        for (int i = 0; i < howMany; i++) {
            final int index = rng.nextInt(points.size());
            final Point center = points.remove(index);
            result.get(0).add(getSurroundingPixels(image, (int) center.x, (int) center.y, size));
            result.get(1).add(getSurroundingPixels(mask, (int) center.x, (int) center.y, size));
            result.get(2).add(getSurroundingPixels(manual, (int) center.x, (int) center.y, size));
        }
        return result;
    }

    /**
     * Creates new image by adding black border of specified size to image.
     *
     * @param image input image
     * @param size  width of border, should be nonnegative
     * @return new image with black border
     */
    public static Mat addBlackBorder(Mat image, int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative size: " + size);
        }
        final int width = image.width();
        final int height = image.height();
        final Mat imageWithPadding = new Mat(width + 2 * size, height + 2 * size, image.type());
        final Scalar black = new Scalar(0, 0, 0);
        Core.copyMakeBorder(image, imageWithPadding, size, size, size, size, Core.BORDER_CONSTANT, black);
        return imageWithPadding;
    }

    public static Mat gradientMagnitudeMaximumOverScales(Mat image, int startScale, int endScale, int step) {
        Mat maximum = new Mat(image.size(), CvType.CV_32F);

        for (int i = startScale; i <= endScale; i += step) {
            final Mat magnitude = gradientMagnitude(image, i);
            final Mat divided = new Mat();
            Core.divide(magnitude, new Scalar(i), divided);
            Core.max(divided, maximum, maximum);
        }
        return maximum;
    }

    private static Mat gradientMagnitude(Mat image, int scale) {
        final Mat kernel = Imgproc.getGaussianKernel(3 * scale, scale);
        final Mat gauss = new Mat();
        Imgproc.filter2D(image, gauss, CvType.CV_32F, kernel);
        final Mat sobelX = new Mat();
        Imgproc.Sobel(gauss, sobelX, CvType.CV_32F, 1, 0);
        final Mat sobelY = new Mat();
        Imgproc.Sobel(gauss, sobelY, CvType.CV_32F, 0, 1);
        final Mat magnitude = new Mat(sobelX.size(), sobelX.type());
        Core.magnitude(sobelX, sobelY, magnitude);
        return magnitude;
    }

    public static Mat largestEigenvalueMaximumOverScales(Mat image, int startScale, int endScale, int step) {
        Mat maximum = new Mat(image.size(), CvType.CV_32F);

        for (int i = startScale; i <= endScale; i += step) {
            final Mat eigen = largeEigenvalue(image, i);
            final Mat divided = new Mat();
            Core.divide(eigen, new Scalar(i), divided);
            Core.max(divided, maximum, maximum);
        }
        return maximum;
    }

    private static Mat largeEigenvalue(Mat image, int scale) {
        final Mat kernel = Imgproc.getGaussianKernel(3 * scale, scale);
        final Mat gauss = new Mat();
        Imgproc.filter2D(image, gauss, CvType.CV_32F, kernel);
        final Mat sobelXX = new Mat();
        Imgproc.Sobel(gauss, sobelXX, CvType.CV_32F, 2, 0);
        Imgcodecs.imwrite("/home/krzysztof/Pictures/Wallpapers/sobelXX.jpg", sobelXX);
        final Mat sobelYY = new Mat();
        Imgproc.Sobel(gauss, sobelYY, CvType.CV_32F, 0, 2);
        Imgcodecs.imwrite("/home/krzysztof/Pictures/Wallpapers/sobelYY.jpg", sobelYY);
        final Mat sobelXY = new Mat();
        Imgproc.Sobel(gauss, sobelXY, CvType.CV_32F, 1, 1);
        Imgcodecs.imwrite("/home/krzysztof/Pictures/Wallpapers/sobelXY.jpg", sobelXY);
        Mat alpha = calculateAlpha(sobelXX, sobelYY, sobelXY);
        Core.divide(alpha, new Scalar(2), alpha);
        final Mat sum = new Mat();
        Core.divide(sobelXX, new Scalar(2), sobelXX);
        Core.divide(sobelYY, new Scalar(2), sobelYY);
        Core.add(sobelXX, sobelYY, sum);
        final Mat eigen = new Mat();
        Core.add(sum, alpha, eigen);

        return eigen;
    }

    private static Mat calculateAlpha(Mat sobelXX, Mat sobelYY, Mat sobelXY) {
        final Mat sum = new Mat();
        Core.subtract(sobelXX, sobelYY, sum);
        final Mat square1 = new Mat();
        Core.pow(sum, 2, square1);
        final Mat square2 = new Mat();
        Core.pow(sobelXY, 2, square2);
        Core.add(square1, square2, sum);
        final Mat alpha = new Mat();
        Core.sqrt(sum, alpha);
        return alpha;
    }

    public static void generateSamplesForFolder(String inputPath, String outputPath, ImageLoader imageLoader,
                                                int howManyPositives, int howManyNegatives, int size) {

        final Path imagesPath = Paths.get(inputPath, "images");
        final Path masksPath = Paths.get(inputPath, "masks");
        final Path manualsPath = Paths.get(inputPath, "manuals");
        final File[] images = imagesPath.toFile().listFiles();
        final File[] masks = masksPath.toFile().listFiles();
        final File[] manuals = manualsPath.toFile().listFiles();
        int positivesIndex = 1;
        int negativesIndex = 1;
        final ProgressInfo progressInfo = new ProgressInfo(images.length * (howManyPositives + howManyNegatives));
        for (int i = 0; i < images.length; i++) {
            final Mat image = imageLoader.loadImage(images[i].getAbsolutePath());
            final Mat mask = Imgcodecs.imread(masks[i].getAbsolutePath());
            final Mat manual = Imgcodecs.imread(manuals[i].getAbsolutePath());
            final List<Mat> positives = sampleImage(image, mask, manual, true, howManyPositives, size).get(0);
            for (int j = 0; j < positives.size(); j++) {
                final Mat imageToSave = positives.get(j);
                final Path outputImagePath = Paths.get(outputPath, String.format("T_%1$03d.png", positivesIndex++));
                Imgcodecs.imwrite(outputImagePath.toString(), imageToSave);
                progressInfo.incrementWorkCount();
            }
            final List<Mat> negatives = sampleImage(image, mask, manual, false, howManyNegatives, size).get(0);
            for (int j = 0; j < negatives.size(); j++) {
                final Mat imageToSave = negatives.get(j);
                final Path outputImagePath = Paths.get(outputPath, String.format("F_%1$03d.png", negativesIndex++));
                Imgcodecs.imwrite(outputImagePath.toString(), imageToSave);
                progressInfo.incrementWorkCount();
            }
        }
    }
}
