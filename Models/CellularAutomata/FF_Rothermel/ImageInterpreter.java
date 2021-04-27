package CellularAutomata.FF_Rothermel;

import util.Logging;

import javax.imageio.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

// Image to rgb can be made faster by: https://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image,
// but used the built-in getRGB() for readability.


public class ImageInterpreter {

    public static void main(String[] args) {
        readImage(20, 20);
    }

    /**
     * This static method reads a png image and converts this to a nRow x nCol matrix of fuel types, based on the color
     * of the image.
     *
     * @param nRow The amount of desired rows.
     * @param nCol The amount of desired columns.
     * @return result The matrix of fuel models.
     */
    public static FuelModel[][] readImage(int nRow, int nCol) {

        FuelModel[][] result = new FuelModel[nRow][nCol];
        BufferedImage image;
        Map<Integer, String> rgbToVegetation = JSONLoader.loadVegetationColours("/home/siemen/FireDEVS/fuelColors.json");
        Map<String, FuelModel> nameToModel = JSONLoader.loadFuelModels("/home/siemen/FireDEVS/FuelModels.json");

        try {
            image = ImageIO.read(new File("/home/siemen/FireDEVS/appel.png"));
        } catch (IOException e) {
            Logging.log("ERROR: The provided image could not be opened! Resulting exception: \n" + e.toString(), Logging.error);
            return new FuelModel[1][1];    // Return an empty map to avoid crash
        }

        final int rowStepSize = image.getHeight() / nRow;
        final int colStepSize = image.getWidth() / nCol;
        String fuelName;

        int argb;
        boolean transparentFound = false;

        // Get RGB values from the image + fix values that are not fully opaque
        for (int row = 0; row < nRow; ++row) {
            for (int col = 0; col < nCol; ++col) {
                argb = image.getRGB(col * colStepSize, row * rowStepSize);

                //Check if opacity is 100%, fix and warn if necessary
                if ((byte) (argb >> 24) != (byte) 0xFF) {
                    transparentFound = true;
                    argb |= 0xFF << 24;
                }

                fuelName = rgbToVegetation.get(argb);
                result[row][col] = nameToModel.get(fuelName);
            }
        }


        if (transparentFound)
            Logging.log("WARN: Values with an opacity different from 100% were found and fixed!", Logging.warning);


        Logging.log("INFO: The model was loaded.", Logging.info);
        return result;

    }

}
