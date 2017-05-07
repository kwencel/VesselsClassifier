import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

    /**
     * Extract green channel from image.
     *
     * @param image BGR image
     * @return single channel image with green channel from input image as intensity
     */
    public static Mat extractGreen(Mat image) {
        final List<Mat> in = new ArrayList<>();
        in.add(image);
        final Mat green = new Mat(image.height(), image.width(), CvType.CV_8UC1);
        final List<Mat> out = new ArrayList<>();
        out.add(green);
        final int[] swaps = {1, 0};
        final MatOfInt fromTo = new MatOfInt(swaps);
        Core.mixChannels(in, out, fromTo);
        return out.get(0);
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
        final Mat paddedImage = addBlackBorder(image, size);
        final int regionSide = 2 * size + 1;
        final Rect region = new Rect(x, y, regionSide, regionSide);
        return new Mat(paddedImage, region);
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
}
