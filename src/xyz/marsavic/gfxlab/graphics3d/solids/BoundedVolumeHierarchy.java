package xyz.marsavic.gfxlab.graphics3d.solids;

import xyz.marsavic.gfxlab.Vec3;
import xyz.marsavic.gfxlab.graphics3d.Hit;
import xyz.marsavic.gfxlab.graphics3d.Material;
import xyz.marsavic.gfxlab.graphics3d.Ray;
import xyz.marsavic.gfxlab.graphics3d.Solid;

import java.util.ArrayList;
import java.util.HashMap;

public class BoundedVolumeHierarchy implements Solid {

    public static final int BIN_SIZE = 16;

    private Solid leftChild;
    private Solid rightChild;
    private Solid boundingVolume;

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

    public Solid getBoundingVolume() {
        return boundingVolume;
    }

    public void setBoundingVolume(Solid boundingVolume) {
        this.boundingVolume = boundingVolume;
    }

    private BoundedVolumeHierarchy(Solid leftChild, Solid rightChild, Solid boundingVolume) {
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.boundingVolume = boundingVolume;
    }

    static class Bin {
        private int solidCount;
        private double surfaceArea;

        public int getSolidCount() {
            return solidCount;
        }

        public void setSolidCount(int solidCount) {
            this.solidCount = solidCount;
        }

        public double getSurfaceArea() {
            return surfaceArea;
        }

        public void setSurfaceArea(double surfaceArea) {
            this.surfaceArea = surfaceArea;
        }

        public Bin(int solidCount, double surfaceArea) {
            this.solidCount = solidCount;
            this.surfaceArea = surfaceArea;
        }
    }

    public static BoundedVolumeHierarchy createSequentially(ArrayList<Solid> solids) {
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBall(solids.get(0), solids.get(1)));
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

        solids.add(new BoundedVolumeHierarchy(minU, minV, createBoundingBall(minU, minV)));

        solids.remove(minU);
        solids.remove(minV);

        return createSequentially(solids);
    }

    public static BoundedVolumeHierarchy createByLevelBall(ArrayList<Solid> solids) {
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBall(solids.get(0), solids.get(1)));
        }

        ArrayList<Solid> boundingBalls = new ArrayList<>();
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

            boundingBalls.add(new BoundedVolumeHierarchy(minU, minV, createBoundingBall(minU, minV)));

            solids.remove(minU);
            solids.remove(minV);
            minWeight = Double.MAX_VALUE;
        }

        if (solids.size() == 1) {
            boundingBalls.add(solids.get(0));
        }

        return createByLevelBall(boundingBalls);
    }

    public static BoundedVolumeHierarchy createByLevelBox(ArrayList<Solid> solids) {
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBox(solids));
        }

        ArrayList<Solid> boundingBoxes = new ArrayList<>();
        double minWeight = Double.MAX_VALUE;
        Solid minU = null;
        Solid minV = null;
        while (solids.size() > 1) {
            for (int i = 0; i < solids.size() - 1; i++) {
                for (int j = i + 1; j < solids.size(); j++) {
                    Box left = retrieveBox(solids.get(i));
                    Box right = retrieveBox(solids.get(j));

                    double distance = left.c().sub(right.c()).length();
                    if (distance < minWeight) {
                        minU = solids.get(i);
                        minV = solids.get(j);
                        minWeight = distance;
                    }
                }
            }

            boundingBoxes.add(new BoundedVolumeHierarchy(minU, minV, createBoundingBox(solids)));

            solids.remove(minU);
            solids.remove(minV);
            minWeight = Double.MAX_VALUE;
        }

        if (solids.size() == 1) {
            boundingBoxes.add(solids.get(0));
        }

        return createByLevelBox(boundingBoxes);
    }

    public static BoundedVolumeHierarchy createBySurfaceAreaHeuristic(ArrayList<Solid> solids) {
        Box boundingBox = createBoundingBox(solids);

        HashMap<Integer, ArrayList<Solid>> binMap = new HashMap<>();

        double maxDimension = Math.max(boundingBox.z(), Math.max(boundingBox.x(), boundingBox.y()));

        if (boundingBox.x() == maxDimension) {
            for (Solid solid: solids) {
                Box box = retrieveBox(solid);
                int binId = (int)((BIN_SIZE * (1 - 1e-9) * (box.c().x() - boundingBox.x_min()))/(boundingBox.x_max() - boundingBox.x_min()));

                ArrayList<Solid> boundingVolumes = binMap.containsKey(binId) ? binMap.get(binId) : new ArrayList<>();
                boundingVolumes.add(solid);
                binMap.put(binId, boundingVolumes);
            }
        } else if (boundingBox.y() == maxDimension) {
            for (Solid solid: solids) {
                Box box = retrieveBox(solid);
                int binId = (int)((BIN_SIZE * (1 - 1e-9) * (box.c().y() - boundingBox.y_min()))/(boundingBox.y_max() - boundingBox.y_min()));

                ArrayList<Solid> boundingVolumes = binMap.containsKey(binId) ? binMap.get(binId) : new ArrayList<>();
                boundingVolumes.add(solid);
                binMap.put(binId, boundingVolumes);
            }
        } else {
            for (Solid solid: solids) {
                Box box = retrieveBox(solid);
                int binId = (int)((BIN_SIZE * (1 - 1e-9) * (box.c().z() - boundingBox.z_min()))/(boundingBox.z_max() - boundingBox.z_min()));

                ArrayList<Solid> boundingVolumes = binMap.containsKey(binId) ? binMap.get(binId) : new ArrayList<>();
                boundingVolumes.add(solid);
                binMap.put(binId, boundingVolumes);
            }
        }

        ArrayList<Bin> bins = new ArrayList<>();

        for (int i = 0; i < BIN_SIZE; i++) {
            if (binMap.containsKey(i)) {
                double surfaceArea = calculateSurfaceArea(createBoundingBox(binMap.get(i)));
                bins.add(new Bin(binMap.get(i).size(), surfaceArea));
            } else {
                bins.add(null);
            }
        }

        int sumSolids = 0;
        double sumSA = 0;

        int[] n_left = new int[BIN_SIZE - 1];
        int[] n_right = new int[BIN_SIZE - 1];
        double[] a_left = new double[BIN_SIZE - 1];
        double[] a_right = new double[BIN_SIZE - 1];

        for (int i = 1; i < BIN_SIZE; i++) {
            Bin bin = bins.get(i - 1);

            if (bin != null) {
                sumSolids += bin.getSolidCount();
                sumSA += bin.getSurfaceArea();
            }

            n_left[i - 1] = sumSolids;
            a_left[i - 1] = sumSA;
        }

        sumSolids = 0;
        sumSA = 0;

        for (int i = BIN_SIZE - 1; i > 0; i--) {
            Bin bin = bins.get(i);

            if (bin != null) {
                sumSolids += bin.getSolidCount();
                sumSA += bin.getSurfaceArea();
            }

            n_right[i - 1] = sumSolids;
            a_right[i - 1] = sumSA;
        }

        double minSAH = Double.POSITIVE_INFINITY;
        int minSAHPosition = -1;

        for (int i = 0; i < BIN_SIZE - 1; i++) {
            double sah = n_left[i] * a_left[i] + n_right[i] * a_right[i];
            if (n_left[i] != 0 && n_right[i] != 0 && sah <= minSAH) {
                minSAH= sah;
                minSAHPosition = i;
            }
        }

        ArrayList<Solid> leftChildren = new ArrayList<>();
        ArrayList<Solid> rightChildren = new ArrayList<>();

        for (int i = 0; i < BIN_SIZE; i++){
            if (i <= minSAHPosition) {
                if (binMap.containsKey(i))
                    leftChildren.addAll(binMap.get(i));
            } else {
                if (binMap.containsKey(i))
                    rightChildren.addAll(binMap.get(i));
            }
        }

        if (minSAHPosition == -1) {
            int size = rightChildren.size() / 2;
            int counter = 0;
            while (counter < size) {
                leftChildren.add(rightChildren.remove(0));
                counter++;
            }
        }

        return new BoundedVolumeHierarchy(prepareChildren(leftChildren), prepareChildren(rightChildren), boundingBox);
    }

    public static Solid prepareChildren(ArrayList<Solid> solids) {
        if (solids.size() == 0) {
            return null;
        }
        if (solids.size() == 1) {
            return solids.get(0);
        }
        if (solids.size() == 2) {
            return new BoundedVolumeHierarchy(solids.get(0), solids.get(1), createBoundingBox(solids));
        }
        return createBySurfaceAreaHeuristic(solids);
    }

    public static double calculateSurfaceArea(Box box) {
        return 8 * (box.x() * box.y() + box.x() * box.z() + box.y() * box.z());
    }

    public static Ball retrieveBall(Solid solid) {
        if (solid instanceof Ball ball) {
            return ball;
        } else {
            return (Ball) ((BoundedVolumeHierarchy) solid).getBoundingVolume();
        }
    }

    public static Box retrieveBox(Solid solid) {
        if (solid instanceof Box box) {
            return box;
        } else {
            return (Box) solid.boundingVolume();
        }
    }

    public static Ball createBoundingBall(Solid a_solid, Solid b_solid) {
        Ball a = retrieveBall(a_solid);
        Ball b = retrieveBall(b_solid);
        Vec3 dist = b.c().sub(a.c());
        double dl = dist.length();
        double r = (a.r() + b.r() + dl)/2;

        Vec3 c_ = dist.div(dl);

        Vec3 c = a.c().add(c_.mul(dl + b.r() - r));

        return Ball.cr(c, r, Material.AIR);
    }

    public static Box createBoundingBox(ArrayList<Solid> solids) {
        double x_min = Double.POSITIVE_INFINITY;
        double y_min = Double.POSITIVE_INFINITY;
        double z_min = Double.POSITIVE_INFINITY;
        double x_max = Double.NEGATIVE_INFINITY;
        double y_max = Double.NEGATIVE_INFINITY;
        double z_max = Double.NEGATIVE_INFINITY;

        for (Solid solid: solids) {
            Box box = retrieveBox(solid);

            x_min = Math.min(x_min, box.x_min());
            y_min = Math.min(y_min, box.y_min());
            z_min = Math.min(z_min, box.z_min());
            x_max = Math.max(x_max, box.x_max());
            y_max = Math.max(y_max, box.y_max());
            z_max = Math.max(z_max, box.z_max());
        }

        return Box.$.pq(Vec3.xyz(x_min, y_min, z_min), Vec3.xyz(x_max, y_max, z_max)).material(Material.AIR);
    }

    @Override
    public Hit firstHit(Ray ray, double afterTime) {
        if (!(boundingVolume.firstHit(ray, afterTime) instanceof Hit.AtInfinity)) {
            if (leftChild == null) {
                return rightChild.firstHit(ray, afterTime);
            }

            if (rightChild == null) {
                return leftChild.firstHit(ray, afterTime);
            }

            Hit left = leftChild.firstHit(ray, afterTime);
            Hit right = rightChild.firstHit(ray, afterTime);

            if (!(right instanceof Hit.AtInfinity) && left instanceof Hit.AtInfinity) {
                return right;
            } else if (!(left instanceof Hit.AtInfinity) && right instanceof Hit.AtInfinity) {
                return left;
            } else if (!(left instanceof Hit.AtInfinity)) {
                if (left.t() < right.t()) {
                    return left;
                } else {
                    return right;
                }
            }
        }

        return Hit.AtInfinity.axisAlignedIn(ray.d());
    }

    @Override
    public Solid boundingVolume() {
        return getBoundingVolume();
    }
}
