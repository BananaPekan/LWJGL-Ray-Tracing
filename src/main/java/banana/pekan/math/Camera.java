package banana.pekan.math;

import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public class Camera {

    Vector3d position;
    double forwardSpeed = 0.03;
    double rightSpeed = 0.03;

    public Camera() {
        this.position = new Vector3d();
    }

    public void translate(double x, double y, double z) {
        position.add(x, y, z);
    }

    public Vector3d getPosition() {
        return position;
    }

    public void move(long window) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW_PRESS) {
            translate(0, 0, forwardSpeed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW_PRESS) {
            translate(0, 0, -forwardSpeed);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW_PRESS) {
            translate(rightSpeed, 0, 0);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW_PRESS) {
            translate(-rightSpeed, 0, 0);
        }
    }

}
