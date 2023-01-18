package xyz.marsavic.gfxlab.resources;

import javafx.scene.image.Image;
import xyz.marsavic.functions.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.graphics3d.Material;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Texture implements F1<Material, Vector> {

    private Material[][] pixelMaterial;
    private int height;
    private int width;

    private Texture(String fileName) {
        try {
            Image image = new Image(new FileInputStream(fileName));

            height = (int) image.getHeight();
            width = (int) image.getWidth();

            pixelMaterial = new Material[height][width];

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    pixelMaterial[i][j] = Material.matte(Color.code(image.getPixelReader().getArgb(j, i)));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Texture file(String fileName) {
        return new Texture(fileName);
    }

    @Override
    public Material at(Vector vector) {
        Vector p = vector.mod().mul(Vector.xy(width, height));
        return pixelMaterial[p.yInt()][p.xInt()];
    }

}
