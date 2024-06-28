package banana.pekan.shader;

import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

public class ShaderBuffer {

    int binding;
    int ssbo;
    ByteBuffer buffer;
    int position;

    public ShaderBuffer(int binding, int capacity) {
        this.binding = binding;
        this.ssbo = GL43.glGenBuffers();
        this.buffer = BufferUtils.createByteBuffer(capacity);
        this.position = 0;
    }

    public void putVector3d(Vector3d vector3d) {
        putDouble(vector3d.x);
        putDouble(vector3d.y);
        putDouble(vector3d.z);
    }

    public void putDouble(double value) {
        buffer.putDouble(position, value);
        position += Double.BYTES;
    }

    public void bindAndPassData() {
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL43.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer, GL43.GL_DYNAMIC_COPY);
        GL43.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, binding, ssbo);
    }

    public void deleteBuffer() {
        GL43.glDeleteBuffers(ssbo);
    }

    public int getBuffer() {
        return ssbo;
    }

}
