import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.PNG_Writer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.*;

public class Billard_Tracker implements PlugInFilter
{
    private static final String IMAGE_PATH = "images/";
    private static float MAX_VALUE = 255.0f;
    private static float RED_WEIGHT = 0.299f;
    private static float GREEN_WEIGHT = 0.587f;
    private static float BLUE_WEIGHT = 0.114f;

    @Override
    public int setup(String arg, ImagePlus imp)
    {   return DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip1)
    {   int w1 = ip1.getWidth();
        int h1 = ip1.getHeight();
        byte[] pix1 = (byte[]) ip1.getPixels();

        ImagePlus imgGray = NewImage.createByteImage("GrayDeBayered", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipGray = imgGray.getProcessor();
        byte[] pixGray = (byte[]) ipGray.getPixels();
        int w2 = ipGray.getWidth();
        int h2 = ipGray.getHeight();

        ImagePlus imgRGB = NewImage.createRGBImage("RGBDeBayered", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipRGB = imgRGB.getProcessor();
        int[] pixRGB = (int[]) ipRGB.getPixels();

        long msStart = System.currentTimeMillis();

        ImagePlus imgHue = NewImage.createByteImage("Hue", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipHue = imgHue.getProcessor();
        byte[] pixHue = (byte[]) ipHue.getPixels();

        int i1 = 0, i2 = 0;

        for (int y=0; y < h2; y++) {
            for (int x = 0; x < w2; x++) {
                int green, green1, green2, red, blue;

                green1 = pix1[i1] & 0xff;
                blue = pix1[i1 + 1] & 0xff;
                red = pix1[i1 + w1] & 0xff;
                green2 = pix1[i1 + w1 + 1] & 0xff;
                green = (green1 + green2) >> 1;

                float[] hsb = Color.RGBtoHSB(red, green, blue, null);

                pixRGB[i2] = ((red & 0xff) << 16) + ((green & 0xff) << 8) + (blue & 0xff);
                pixHue[i2] = (byte) (int) (hsb[0] * MAX_VALUE);
//                pixGray[i2] = (byte) (int) (hsb[2] * MAX_VALUE);
                int grey = (int) ((red & 0xff) * RED_WEIGHT + (green & 0xff) * GREEN_WEIGHT + (blue & 0xff) * BLUE_WEIGHT);
                pixGray[i2] = (byte) Math.min(MAX_VALUE, grey);

                i1 += 2;
                i2++;
            }
            i1 += w1;
        }

        long ms = System.currentTimeMillis() - msStart;
        System.out.println(ms);
        ImageStatistics stats = ipGray.getStatistics();
        System.out.println("Mean:" + stats.mean);

        PNG_Writer png = new PNG_Writer();
        try
        {   png.writeImage(imgRGB , IMAGE_PATH + "Billard1024x544x3.png",  0);
            png.writeImage(imgHue,  IMAGE_PATH + "Billard1024x544x1H.png", 0);
            png.writeImage(imgGray, IMAGE_PATH + "Billard1024x544x1B.png", 0);

        } catch (Exception e)
        {   e.printStackTrace();
        }

        imgGray.show();
        imgGray.updateAndDraw();
        imgRGB.show();
        imgRGB.updateAndDraw();
        imgHue.show();
        imgHue.updateAndDraw();
    }

    public static void main(String[] args)
    {
        Billard_Tracker plugin = new Billard_Tracker();

        ImagePlus im = new ImagePlus(IMAGE_PATH + "Billard2048x1088x1.png");
        im.show();
        plugin.setup("", im);
        plugin.run(im.getProcessor());
    }
}
