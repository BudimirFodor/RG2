package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.functions.interfaces.F1;
import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;
import xyz.marsavic.utils.Numeric;


public class Ball implements Solid {
	
	private final Vec3 c;
	private final double r;
	private final F1<Material, Vector> mapMaterial;
	
	// transient
	private final double rSqr;

	
	private Ball(Vec3 c, double r, F1<Material, Vector> mapMaterial) {
		this.c = c;
		this.r = r;
		rSqr = r * r;
		this.mapMaterial = mapMaterial;
	}
	
	
	public static Ball cr(Vec3 c, double r, F1<Material, Vector> mapMaterial) {
		return new Ball(c, r, mapMaterial);
	}
	
	
	public Vec3 c() {
		return c;
	}
	
	
	public double r() {
		return r;
	}
	
	
	
	@Override
	public HitBall firstHit(Ray ray, double afterTime) {
		Vec3 e = c().sub(ray.p());                                // Vector from the ray origin to the ball center

		double dSqr = ray.d().lengthSquared();
		double l = e.dot(ray.d()) / dSqr;
		double mSqr = l * l - (e.lengthSquared() - rSqr) / dSqr;

		if (mSqr > 0) {
			double m = Math.sqrt(mSqr);
			if (l - m > afterTime) return new HitBall(ray, l - m);
			if (l + m > afterTime) return new HitBall(ray, l + m);
		}
		return null;
	}

//	@Override
//	public HitBall firstHit(Ray ray, double afterTime) {
//		double px = ray.p().x();
//		double py = ray.p().y();
//		double pz = ray.p().z();
//
//		double dx = ray.d().x();
//		double dy = ray.d().y();
//		double dz = ray.d().z();
//
//		double cx = c.x();
//		double cy = c.y();
//		double cz = c.z();
//
//		double ka = dx * dx + dy * dy + dz * dz;
//		double kb = 2 * (dx * (px  - cx) + dy * (py - cy) + dz * (pz - cz));
//		double kc = (px - cx) * (px - cx) + (py - cy) * (py - cy) + (pz - cz) * (pz - cz) - r * r;
//
//		double disk = kb * kb - 4 * ka * kc;
//		if (disk > 0) {
//			double rootDisk = Math.sqrt(disk);
//			double lower = ((-1) * kb - rootDisk)/(2 * ka);
//			if (lower > afterTime) return new HitBall(ray, lower);
//			double upper = ((-1) * kb + rootDisk)/(2 * ka);
//			if (upper > afterTime) return new HitBall(ray, upper);
//		}
//		return null;
//	}
	
	
	class HitBall extends Hit.RayT {
		
		protected HitBall(Ray ray, double t) {
			super(ray, t);
		}
		
		@Override
		public Vec3 n() {
			return ray().at(t()).sub(c());
		}
		
		@Override
		public Material material() {
			return Ball.this.mapMaterial.at(uv());
		}
		
		@Override
		public Vector uv() {
			Vec3 n = n();
			return Vector.xy(
					Numeric.atan2T(n.z(), n.x()),
					-2 * Numeric.asinT(n.y() / r) + 0.5
			);
		}
		
		@Override
		public Vec3 n_() {
			return n().div(r);
		}
		
	}
	
}
