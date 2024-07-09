package banana.pekan;

import banana.pekan.math.Sphere;
import banana.pekan.shader.ComputeShader;
import banana.pekan.shader.ShaderBuffer;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;

import java.lang.instrument.Instrumentation;
import java.nio.*;
import java.util.ArrayList;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GraphicsMain {

    private long window;

    int width;
    int height;

    FloatBuffer pixelBuffer;

    Scene scene;

    public void run(int width, int height) {
        this.width = width;
        this.height = height;

        init();

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        window = glfwCreateWindow(width, height, "Ray Tracer", NULL, NULL);
        if ( window == NULL ) throw new RuntimeException("Failed to create the GLFW window");

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);

        glfwShowWindow(window);

        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        pixelBuffer = BufferUtils.createFloatBuffer(width * height * 4);
        scene = new Scene();
        scene.addSphere(new Sphere(0, 9, 7, 5, new Vector4d(1, 0, 0, 1)));
        scene.addSphere(new Sphere(-5, 3, 7, 1, new Vector4d(0, 0, 1, 1)));
    }


    private void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        ComputeShader computeShader = new ComputeShader("shaders/ray_tracing_compute_shader.hlsl", "MainEntry");
        computeShader.loadShader();

        int program = glCreateProgram();

        glAttachShader(program, computeShader.getShader());
        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetProgramInfoLog(program));
        }

        glfwFocusWindow(window);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            applyShader(program);

            glDrawPixels(width, height, GL_RGBA, GL_FLOAT, pixelBuffer);

            double[] mouseX = new double[1];
            double[] mouseY = new double[1];

            glfwGetCursorPos(window, mouseX, mouseY);

            double mouseDX = mouseX[0] - width / 2f;
            double mouseDY = mouseY[0] - height / 2f;

            scene.camera.rotate(mouseDY / height, mouseDX / width);

            glfwSetCursorPos(window, width / 2f, height / 2f);

            pixelBuffer.clear();

            scene.move(window);

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    public void applyShader(int program) {
        glUseProgram(program);

        int ssbo = GL43.glGenBuffers();

        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, pixelBuffer, GL43.GL_DYNAMIC_COPY);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        ShaderBuffer spheresBuffer = new ShaderBuffer(1, Sphere.BYTES * scene.getSpheres().size());

        for (Sphere sphere : scene.getSpheres()) {
            spheresBuffer.putVector3d(sphere.getOrigin());
            spheresBuffer.putDouble(sphere.getRadius());
            spheresBuffer.putVector4d(sphere.getColor());
        }

        spheresBuffer.bindAndPassData();

        ShaderBuffer variablesBuffer = new ShaderBuffer(2, Double.BYTES * 32, GL43.GL_UNIFORM_BUFFER);

        variablesBuffer.putVector3d(scene.getCameraPos());
        variablesBuffer.putFloat(width);
        variablesBuffer.putFloat(height);
        variablesBuffer.putVector4d(scene.getCameraRotations());

        variablesBuffer.bindAndPassData();

        GL43.glDispatchCompute(width / 8, height / 8, 1);

        GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glGetBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, pixelBuffer);

        glUseProgram(0);

        GL43.glDeleteBuffers(ssbo);
        variablesBuffer.deleteBuffer();
        spheresBuffer.deleteBuffer();
    }

    public void setPixel(int index, int rgba) {
        pixelBuffer.put(index++, ((rgba >> 24) & 0xFF) / 255f);
        pixelBuffer.put(index++, ((rgba >> 16) & 0xFF) / 255f);
        pixelBuffer.put(index++, ((rgba >> 8) & 0xFF) / 255f);
        pixelBuffer.put(index, (rgba & 0xFF) / 255f);
    }

    public void setPixel(int x, int y, int rgba) {
        setPixel((y * width + x) * 4, rgba);
    }

}

