package xyz.marsavic.gfxlab.graphics3d.scene;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Light;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Scene;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.gfxlab.graphics3d.solids.Ball;
import xyz.marsavic.gfxlab.graphics3d.solids.BoundedVolumeHierarchy;
import xyz.marsavic.gfxlab.graphics3d.solids.Group;
import xyz.marsavic.gfxlab.graphics3d.solids.HalfSpace;
import xyz.marsavic.gfxlab.graphics3d.textures.Grid;
import xyz.marsavic.random.RNG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class BoundedVolumeHierarchyTest extends Scene.Base {

    public BoundedVolumeHierarchyTest(int nBalls) {
        var mL = Grid.standard(Color.hsb(0.0 / 3, 0.5, 0.7));
        var mR = Grid.standard(Color.hsb(1.0 / 3, 0.5, 0.7));
        var mW = Grid.standard(Color.gray(0.7));

        Collection<Solid> solids = new ArrayList<>();
        Collections.addAll(solids,
                HalfSpace.pn(Vec3.xyz(-1,  0,  0), Vec3.xyz( 1,  0,  0), mL),
                HalfSpace.pn(Vec3.xyz( 1,  0,  0), Vec3.xyz(-1,  0,  0), mR),
                HalfSpace.pn(Vec3.xyz( 0, -1,  0), Vec3.xyz( 0,  1,  0), mW),
                HalfSpace.pn(Vec3.xyz( 0,  1,  0), Vec3.xyz( 0, -1,  0), Material.LIGHT),
                HalfSpace.pn(Vec3.xyz( 0,  0,  1), Vec3.xyz( 0,  0, -1), mW)
        );

        Collections.addAll(lights,
                Light.pc(Vec3.xyz(-0.8, 0.8, -0.8), Color.WHITE),
                Light.pc(Vec3.xyz(-0.8, 0.8,  0.8), Color.WHITE),
                Light.pc(Vec3.xyz( 0.8, 0.8, -0.8), Color.WHITE),
                Light.pc(Vec3.xyz( 0.8, 0.8,  0.8), Color.WHITE)
        );

        ArrayList<Solid> boundedVolumeHierarchySolids = new ArrayList<>();

        RNG rng = new RNG();
        for (int i = 0; i < nBalls; i++) {
            double size = round(rng.nextDouble(), 0.1, 5);
            double hue = round(rng.nextDouble(), 0.05, 1);
            boundedVolumeHierarchySolids.add(Ball.cr(Vec3.xyz(rng.nextDouble() * 2 - 1, rng.nextDouble() * 2 - 1, rng.nextDouble() * 2 - 2), size,
                    uv -> Material.matte(Color.hsb(hue, 0.75, 0.6))));
        }

        solids.add(BoundedVolumeHierarchy.createSequentially(boundedVolumeHierarchySolids));

        solid = Group.of(solids);
    }

    public double round(double number, double percentIncrease, double division) {
        double currentPercent = 0;
        while (number > currentPercent) {
            currentPercent += percentIncrease;
        }
        return currentPercent / division;
    }
}
