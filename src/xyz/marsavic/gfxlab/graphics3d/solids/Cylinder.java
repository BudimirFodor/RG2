package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;

public class Cylinder implements Solid {

    private final Vec3 c;
    private final double r;

    // transient
    private final double rSqr;



    private Cylinder(Vec3 c, double r) {
        this.c = c;
        this.r = r;
        rSqr = r * r;
    }

    public static Cylinder cr(Vec3 c, double r) {
        return new Cylinder(c, r);
    }


    public Vec3 c() {
        return c;
    }


    public double r() {
        return r;
    }

    @Override
    public Cylinder.HitCylinder firstHit(Ray ray, double afterTime) {
        double px = ray.p().x();
        double pz = ray.p().z();

        double dx = ray.d().x();
        double dz = ray.d().z();

        double cx = c.x();
        double cz = c.z();

        double ka = dx * dx + dz * dz;
        double kb = 2 * (dx * (px  - cx) + dz * (pz - cz));
        double kc = (px - cx) * (px - cx) + (pz - cz) * (pz - cz) - r * r;

        double disk = kb * kb - 4 * ka * kc;
        if (disk > 0) {
            System.out.println(disk);
            double rootDisk = Math.sqrt(disk);
            double lower = ((-1) * kb - rootDisk)/(2 * ka);
            if (lower > afterTime) return new Cylinder.HitCylinder(ray, lower);
            double upper = ((-1) * kb + rootDisk)/(2 * ka);
            if (upper > afterTime) return new Cylinder.HitCylinder(ray, upper);
        }
        return null;
    }

    class HitCylinder extends Hit.RayT {

        protected HitCylinder(Ray ray, double t) {
            super(ray, t);
        }

        @Override
        public Vec3 n() {
            return ray().at(t()).sub(c());
        }

        @Override
        public Vec3 n_() {
            return n().div(r);
        }

    }
}
