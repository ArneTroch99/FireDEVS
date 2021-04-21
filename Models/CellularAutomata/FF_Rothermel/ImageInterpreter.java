package CellularAutomata.FF_Rothermel;
import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageInterpreter {

    public static void main(String[] args) {
        readImage(new int[]{10, 10});
    }

    public static Map<int[], String> readImage(int[] resolution) {
        assert (resolution.length == 2);

        BufferedImage image;
        HashMap<int[], String> result = null;

        try {
            image = ImageIO.read(new File("/home/siemen/FireDEVS/appel.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        FastRGB fastRGB = new FastRGB(image);
        short[] rgbValue = fastRGB.getRGB(1, 1);

        for (int x = 0; x < image.getWidth(); ++x){
            for (int y = 0; y < image.getHeight(); ++y){
            }
        }

        return new HashMap<>();
    }

}
