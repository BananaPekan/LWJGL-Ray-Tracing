package banana.pekan.math;

import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    Vector3d position;
    Vector4d rotations;

    Vector3d forward = new Vector3d(0, 0, 0.1);
    Vector3d right = new Vector3d(0.1, 0, 0);
    Vector3d up = new Vector3d(0, 0.1, 0);

    double angleY = 0;
    double angleX = 0;

    public Camera() {
        this.position = new Vector3d();
        this.rotations = new Vector4d(1, 0, 1, 0);
    }

    public void rotate(double x, double y) {
        angleX += x;
        angleY += y;
        rotations.set(Math.cos(angleX), Math.sin(angleX), Math.cos(angleY), Math.sin(angleY));
        Quaterniond quaterniond = new Quaterniond();
        quaterniond = quaterniond.fromAxisAngleRad(0, Math.abs(Math.cos(angleY)), 0, angleY);

        forward = new Vector3d(0, 0, 0.1).rotate(quaterniond);
        right = new Vector3d(0.1, 0, 0).rotate(quaterniond);
    }

    public void translate(double x, double y, double z) {
        position.add(x, y, z);
    }

    public void translate3(Vector3d xyz) {
        position.add(xyz);
    }

    public Vector3d getPosition() {
        return position;
    }

    public void move(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW_PRESS) {
            translate3(forward);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW_PRESS) {
            translate3(forward.negate(new Vector3d()));
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW_PRESS) {
            translate3(right);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW_PRESS) {
            translate3(right.negate(new Vector3d()));
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW_PRESS) {
            translate3(up);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            translate3(up.negate(new Vector3d()));
        }
    }

    public Vector4d getRotations() {
        return rotations;
    }
}
