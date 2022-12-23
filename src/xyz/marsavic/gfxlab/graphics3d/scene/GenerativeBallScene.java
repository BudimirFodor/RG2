package xyz.marsavic.gfxlab.graphics3d.scene;

import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Light;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.random.RNG;

import java.util.ArrayList;
import java.util.Collections;

public class GenerativeBallScene extends Scene.Base{

    public GenerativeBallScene(int n) {
        RNG rng = new RNG();
        ArrayList<Solid> solidCollection = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double size = round(rng.nextDouble(), 0.1, 5);
            double hue = round(rng.nextDouble(), 0.05, 1);
            solidCollection.add(Ball.cr(Vec3.xyz(rng.nextDouble() * 2 - 1, rng.nextDouble() * 2 - 1, rng.nextDouble() * 2 - 2), size,
                    uv -> Material.matte(Color.hsb(hue, 0.75, 0.6))));
        }

        solidCollection.add(HalfSpace.pn(Vec3.xyz(0, -1, 0), Vec3.xyz(0, 1, 0),
                uv -> Material.matte(Color.hsb(0, 0.2, 0.8))
        ));

        solid = Group.of(solidCollection);

        Collections.addAll(lights,
                Light.pc(Vec3.xyz(-2, 0, 0), Color.gray(0.6)),
                Light.pc(Vec3.xyz( 2, 0,  0), Color.gray(0.6)),
                Light.pc(Vec3.xyz( 0, 0,  -3), Color.gray(0.2))
        );
    }

    public double round(double number, double percentIncrease, double division) {
        double currentPercent = 0;
        while (number > currentPercent) {
            currentPercent += percentIncrease;
        }
        return currentPercent / division;
    }
}
