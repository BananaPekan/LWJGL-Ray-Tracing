package banana.pekan;

import banana.pekan.math.Camera;
import banana.pekan.math.Sphere;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector4d;

import java.util.ArrayList;

public class Scene {

    Camera camera;
    ArrayList<Sphere> spheres;

    public Scene() {
        camera = new Camera();
        spheres = new ArrayList<>();
    }

    public void addSphere(Sphere sphere) {
        spheres.add(sphere);
    }

    public ArrayList<Sphere> getSpheres() {
        return spheres;
    }

    public Vector3d getCameraPos() {
        return camera.getPosition();
    }

    public Vector4d getCameraRotations() {
        return camera.getRotations();
    }

    public void move(long window) {
        camera.move(window);
    }

}
