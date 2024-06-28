package banana.pekan.math;

import org.joml.Vector3d;
import org.joml.Vector4d;

public class Sphere {

    Vector3d origin;
    double radius;
    Vector4d color;

    public static int BYTES = Double.BYTES * 8;

    public Sphere(double x, double y, double z, double radius, Vector4d color) {
        this.origin = new Vector3d(x, y, z);
        this.radius = radius;
        this.color = color;
    }

    public void translate(double x, double y, double z) {
        origin.add(x, y, z);
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public double getRadius() {
        return radius;
    }

    public Vector4d getColor() {
        return color;
    }

}
