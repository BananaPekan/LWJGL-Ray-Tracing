package banana.pekan;

import banana.pekan.math.Ray;
import banana.pekan.math.Sphere;
import banana.pekan.shader.ComputeShader;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.Shaderc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL46C.GL_SHADER_BINARY_FORMAT_SPIR_V;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GraphicsMain {

    private long window;

    int width;
    int height;

    FloatBuffer pixelBuffer;

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
    }

    private void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        ComputeShader computeShader = new ComputeShader("shaders/ray_tracing_compute_shader.hlsl", "MainEntry");
        computeShader.loadShader();

        int program = GL43.glCreateProgram();

        glAttachShader(program, computeShader.getShader());
        glLinkProgram(program);


        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetProgramInfoLog(program));
        }

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            applyShader(program);

            glDrawPixels(width, height, GL_RGBA, GL_FLOAT, pixelBuffer);

            pixelBuffer.clear();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    public void applyShader(int program) {
        GL43.glUseProgram(program);

        int ssbo = GL43.glGenBuffers();

        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, pixelBuffer, GL43.GL_DYNAMIC_COPY);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, ssbo);

        int variablesSSBO = GL43.glGenBuffers();
        ByteBuffer variablesBuffer = BufferUtils.createByteBuffer(Float.BYTES * 2);

        variablesBuffer.putFloat(0, width);
        variablesBuffer.putFloat(Float.BYTES, height);

        GL43.glBindBuffer(GL43.GL_UNIFORM_BUFFER, variablesSSBO);
        GL43.glBufferData(GL43.GL_UNIFORM_BUFFER, variablesBuffer, GL43.GL_DYNAMIC_COPY);
        GL43.glBindBufferBase(GL43.GL_UNIFORM_BUFFER, 1, variablesSSBO);

        GL43.glDispatchCompute(width / 8, height / 8, 1);

        GL43.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        GL43.glGetBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, pixelBuffer);

        GL43.glUseProgram(0);

        GL43.glDeleteBuffers(ssbo);
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

