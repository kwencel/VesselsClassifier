import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageUtils {

    public static byte[][] extractGreen(BufferedImage image) {
        byte[][] array = new byte[image.getHeight()][image.getWidth()];
        for (int row = 0; row < image.getHeight(); ++row) {
            for (int col = 0; col < image.getWidth(); col++) {
                array[row][col] = (byte) ((image.getRGB(col, row) >> 8) & 0xFF); // Green extraction
            }
        }
        return array;
    }

    /**
     *  NOT READY YET - DON'T USE
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
}
