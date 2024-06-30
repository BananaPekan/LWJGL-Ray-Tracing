package banana.pekan.shader;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public class ShaderBuffer {

    int binding;
    int ssbo;
    ByteBuffer buffer;
    int target;

    public ShaderBuffer(int binding, int capacity, int target) {
        this.binding = binding;
        this.ssbo = GL43.glGenBuffers();
        this.buffer = BufferUtils.createByteBuffer(capacity);
        this.target = target;
    }

    public ShaderBuffer(int binding, int capacity) {
        this.binding = binding;
        this.ssbo = GL43.glGenBuffers();
        this.buffer = BufferUtils.createByteBuffer(capacity);
        this.target = GL43.GL_SHADER_STORAGE_BUFFER;
    }

    public void putVector2d(Vector2d vector2d) {
        putDouble(vector2d.x);
        putDouble(vector2d.y);
    }

    public void putVector3d(Vector3d vector3d) {
        putDouble(vector3d.x);
        putDouble(vector3d.y);
        putDouble(vector3d.z);
    }

    public void putVector4d(Vector4d vector4d) {
        putDouble(vector4d.x);
        putDouble(vector4d.y);
        putDouble(vector4d.z);
        putDouble(vector4d.w);
    }

    public void putMatrix3d(Matrix3f matrix3f) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                putFloat(matrix3f.getRowColumn(j, i));
            }
        }
    }

    public void putDouble(double value) {
        buffer.putDouble(value);
    }

    public void putFloat(float value) {
        buffer.putFloat(value);
    }

    public void bindAndPassData() {
        buffer.flip();
        GL43.glBindBuffer(target, ssbo);
        GL43.glBufferData(target, buffer, GL43.GL_DYNAMIC_COPY);
        GL43.glBindBufferBase(target, binding, ssbo);
    }

    public void deleteBuffer() {
        GL43.glDeleteBuffers(ssbo);
    }

    public int getBuffer() {
        return ssbo;
    }

}
