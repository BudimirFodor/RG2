package xyz.marsavic.gfxlab.playground.colorfunctions;

import xyz.marsavic.geometry.Vector;
import xyz.marsavic.gfxlab.Color;
import xyz.marsavic.gfxlab.ColorFunctionT;
import xyz.marsavic.utils.Numeric;

public class CustomCFT implements ColorFunctionT {
    @Override
    public Color at(double t, Vector p) {
        double circle = p.x() * p.x() + p.y() * p.y();
        if (circle > 0.5 && circle < 0.55) {
            return Color.rgb(45, 45, 45);
        } else if (circle > 0.55) {
            return Color.rgb(167, 255, 174);
        }

        double eyeX1 = p.x() - 0.2;
        double eyeX2 = p.x() + 0.2;
        double eyeY = (p.y() - 0.2);

        if (eyeX1 * eyeX1 + eyeY * eyeY < 0.005 + 0.005 * Math.abs(Numeric.sinT(t)) || eyeX2 * eyeX2 + eyeY * eyeY < 0.005 + 0.005 * Math.abs(Numeric.sinT(t + 0.25))) {
            return Color.rgb(45, 45, 45);
        }

        if (circle > 0.2 && circle < 0.25 && p.y() < -0.2) {
            return Color.rgb(45, 45, 45);
        }

        return Color.rgb(255, 226, 156);
    }
}
