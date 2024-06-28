package banana.pekan.math;

import org.joml.Vector3d;

public class Sphere {

    Vector3d origin;
    double radius;

    public static int BYTES = Double.BYTES * 4;

    public Sphere(double x, double y, double z, double radius) {
        this.origin = new Vector3d(x, y, z);
        this.radius = radius;
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public double getRadius() {
        return radius;
    }
}
