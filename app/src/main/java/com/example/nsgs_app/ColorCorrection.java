package com.example.nsgs_app;

import android.graphics.ColorMatrix;
import android.view.ViewGroup;

public class ColorCorrection {


    public static ColorMatrix Protanopia() {

        ColorMatrix colorMatrixProteanopia = new ColorMatrix();
        colorMatrixProteanopia.set(new float[]{
                0.567F, 0.433F, 0.000F, 0.000F, 0.000F,
                0.558F, 0.442F, 0.000F, 0.000F, 0.000F,
                0F, 0.242F, 0.758F, 0.000F, 0.000F,
                0F, 0.000F, 0.000F, 1.000F, 0.000F,
        });
        return colorMatrixProteanopia;
    }

    public static ColorMatrix Tritanopia() {

        ColorMatrix colorMatrixTritanopia = new ColorMatrix();
        colorMatrixTritanopia.set(new float[]{
                0.967F, 0.033F, 0, 0, 0,
                0F, 0.733F, 0.267F, 0F, 0F,
                0F, 0.183F, 0.817F, 0F, 0F,
                0F, 0F, 0F, 1F, 0F,
                0F, 0F, 0F, 0F, 1F
        });

        return colorMatrixTritanopia;
    }

    public static ColorMatrix Deuteranopia() {

        ColorMatrix colorMatrixDeuteranopia = new ColorMatrix();
        colorMatrixDeuteranopia.set(new float[]{
                0.625F, 0.375F, 0F, 0F, 0F,
                0.7F, 0.3F, 0F, 0F, 0F,
                0F, 0.3F, 0.7F, 0F, 0F,
                0F, 0F, 0F, 1F, 0F,
                0F, 0F, 0F, 0F, 1F
        });

        return colorMatrixDeuteranopia;
    }

}
