package banana.pekan.shader;

import banana.pekan.GraphicsMain;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL46;
import org.lwjgl.util.shaderc.Shaderc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL46C.GL_SHADER_BINARY_FORMAT_SPIR_V;

public class ComputeShader {

    String shaderFile;
    String entryPoint;

    int shader;
    boolean initialised = false;

    public ComputeShader(String shaderFile, String entryPoint) {
        this.shaderFile = shaderFile;
        this.entryPoint = entryPoint;
    }

    public void loadShader() {
        String hlslSource;

        try {
            URL resource = GraphicsMain.class.getClassLoader().getResource(shaderFile);
            if (resource == null) throw new NullPointerException();
            hlslSource = Files.readString(Path.of(resource.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        int computeShader = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);
        this.shader = computeShader;

        long compiler = Shaderc.shaderc_compiler_initialize();
        long options = Shaderc.shaderc_compile_options_initialize();
        Shaderc.shaderc_compile_options_set_target_env(options , Shaderc.shaderc_target_env_opengl, Shaderc.shaderc_env_version_opengl_4_5);
        Shaderc.shaderc_compile_options_set_source_language(options, Shaderc.shaderc_source_language_hlsl);

        long result = Shaderc.shaderc_compile_into_spv(compiler, hlslSource, Shaderc.shaderc_glsl_compute_shader, shaderFile, entryPoint, options);

        if (Shaderc.shaderc_result_get_compilation_status(result) != Shaderc.shaderc_compilation_status_success) {
            throw new RuntimeException(Shaderc.shaderc_result_get_error_message(result));
        }

        ByteBuffer shaderBytecode = Shaderc.shaderc_result_get_bytes(result);

        if (shaderBytecode == null) throw new Error("Failed to fetch bytecode for compiled shader.");

        GL46.glShaderBinary(new int[]{computeShader}, GL_SHADER_BINARY_FORMAT_SPIR_V, shaderBytecode);

        GL46.glSpecializeShader(computeShader, entryPoint, new int[0], new int[0]);

        initialised = true;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public int getShader() {
        return shader;
    }
}
