package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;

import java.util.ArrayList;

public class BoundedVolumeHierarchy implements Solid {

    private Solid leftChild;
    private Solid rightChild;
    private Ball boundingBox;

    public Solid getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Solid leftChild) {
        this.leftChild = leftChild;
    }

    public Solid getRightChild() {
        return rightChild;
    }

    public void setRightChild(Solid rightChild) {
        this.rightChild = rightChild;
    }

    public Ball getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Ball boundingBox) {
        this.boundingBox = boundingBox;
    }

    private BoundedVolumeHierarchy(Solid leftChild, Solid rightChild, Ball boundingBox) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.boundingBox = boundingBox;
    }

    public static BoundedVolumeHierarchy createSequentially(ArrayList<Solid> solids) {
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBox(solids.get(0), solids.get(1)));
        }

        double minWeight = Double.MAX_VALUE;
        Solid minU = null;
        Solid minV = null;
        for (int i = 0; i < solids.size() - 1; i++) {
            for (int j = i + 1; j < solids.size(); j++) {
                Ball left = retrieveBall(solids.get(i));
                Ball right = retrieveBall(solids.get(j));

                double distance = left.c().sub(right.c()).length();
                if (distance < minWeight) {
                    minU = solids.get(i);
                    minV = solids.get(j);
                    minWeight = distance;
                }
            }
        }

        solids.add(new BoundedVolumeHierarchy(minU, minV, createBoundingBox(minU, minV)));

        solids.remove(minU);
        solids.remove(minV);

        return createSequentially(solids);
    }

    public static BoundedVolumeHierarchy createByLevel(ArrayList<Solid> solids) {
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBox(solids.get(0), solids.get(1)));
        }

        ArrayList<Solid> boundingBoxes = new ArrayList<>();
        double minWeight = Double.MAX_VALUE;
        Solid minU = null;
        Solid minV = null;
        while (solids.size() > 1) {
            for (int i = 0; i < solids.size() - 1; i++) {
                for (int j = i + 1; j < solids.size(); j++) {
                    Ball left = retrieveBall(solids.get(i));
                    Ball right = retrieveBall(solids.get(j));

                    double distance = left.c().sub(right.c()).length();
                    if (distance < minWeight) {
                        minU = solids.get(i);
                        minV = solids.get(j);
                        minWeight = distance;
                    }
                }
            }

            boundingBoxes.add(new BoundedVolumeHierarchy(minU, minV, createBoundingBox(minU, minV)));

            solids.remove(minU);
            solids.remove(minV);
            minWeight = Double.MAX_VALUE;
        }

        if (solids.size() == 1) {
            boundingBoxes.add(solids.get(0));
        }

        return createByLevel(boundingBoxes);
    }

    public static Ball retrieveBall(Solid solid) {
        if (solid instanceof Ball) {
            return (Ball) solid;
        } else {
            return ((BoundedVolumeHierarchy) solid).getBoundingBox();
        }
    }

    public static Ball createBoundingBox(Solid a_solid, Solid b_solid) {
        Ball a = retrieveBall(a_solid);
        Ball b = retrieveBall(b_solid);
        Vec3 dist = b.c().sub(a.c());
        double dl = dist.length();
        double r = (a.r() + b.r() + dl)/2;

        Vec3 c_ = dist.div(dl);

        Vec3 c = a.c().add(c_.mul(dl + b.r() - r));

        return Ball.cr(c, r, Material.AIR);
    }

    @Override
    public Hit firstHit(Ray ray, double afterTime) {
        if (boundingBox.firstHit(ray, afterTime) != null) {
            Hit left = leftChild.firstHit(ray, afterTime);
            Hit right = rightChild.firstHit(ray, afterTime);

            if (right != null && left == null) {
                return right;
            } else if (left != null && right == null) {
                return left;
            } else if (left != null) {
                if (left.t() < right.t()) {
                    return left;
                } else {
                    return right;
                }
            }
        }

        return null;
    }
}
