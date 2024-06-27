package banana.pekan.math;

import org.joml.Vector3d;

public class Ray {

    Vector3d origin;
    Vector3d direction;

    public Ray(Vector3d origin, Vector3d direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3d getOrigin() {
        return origin;
    }

    public Vector3d getDirection() {
        return direction;
    }
}
