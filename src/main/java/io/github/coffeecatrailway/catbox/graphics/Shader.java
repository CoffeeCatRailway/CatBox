package io.github.coffeecatrailway.catbox.graphics;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL41.*;

public class Shader
{
	private final int program;
	
	public Shader(String vFile, String fFile)
	{
		this(vFile, null, fFile);
	}
	
	public Shader(String vFile, @Nullable String gFile, String fFile)
	{
		int vShader, fShader, gShader = -1;
		vShader = compile(GL_VERTEX_SHADER, vFile);
		if (gFile != null)
			gShader = compile(GL_GEOMETRY_SHADER, gFile);
		fShader = compile(GL_FRAGMENT_SHADER, fFile);
		
		this.program = glCreateProgram();
		glAttachShader(this.program, vShader);
		if (gFile != null)
			glAttachShader(this.program, gShader);
		glAttachShader(this.program, fShader);
		
		glBindFragDataLocation(this.program, 0, "FragColor");
		glLinkProgram(this.program);
		
		glDeleteShader(vShader);
		if (gFile != null)
			glDeleteShader(gShader);
		glDeleteShader(fShader);
		System.out.printf("Shader %d linked\n", this.program);
	}
	
	private CharSequence loadInternal(String path)
	{
		try
		{
			URI uri = Objects.requireNonNull(getClass().getResource(path)).toURI();
			return Files.readString(Paths.get(uri), StandardCharsets.UTF_8);
		} catch (URISyntaxException | IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private int compile(int type, String path)
	{
		int shader = glCreateShader(type);
		if (shader == 0)
			throw new RuntimeException(String.format("Failed to create shader '%s'", path));
		
		CharSequence source = this.loadInternal(path);
		glShaderSource(shader, source);
		glCompileShader(shader);
		
		final int status = glGetShaderi(shader, GL_COMPILE_STATUS);
		if (status == GL_FALSE)
		{
			System.err.printf("Shader compilation error: %s\n", path);

			final int logSize = glGetShaderi(shader, GL_INFO_LOG_LENGTH);
			String log = glGetShaderInfoLog(shader, logSize);
			System.err.println(log);
			glDeleteShader(shader);
			System.exit(-1);
		}
		System.out.printf("Shader '%s' compiled\n", path);
		return shader;
	}
	
	public int getProgram()
	{
		return this.program;
	}
	
	public void bind()
	{
		glUseProgram(this.program);
	}
	
	public void destroy()
	{
		glDeleteProgram(this.program);
	}
	
	public int getAttribLocation(CharSequence name)
	{
		return glGetAttribLocation(this.program, name);
	}
	
	public void setUniform1i(String name, int x)
	{
		glProgramUniform1i(this.program, glGetUniformLocation(this.program, name), x);
	}
	
	public void setUniform1ui(String name, int x)
	{
		glProgramUniform1ui(this.program, glGetUniformLocation(this.program, name), x);
	}
	
	public void setUniform1f(String name, float x)
	{
		glProgramUniform1f(this.program, glGetUniformLocation(this.program, name), x);
	}
	
	public void setUniform2f(String name, float x, float y)
	{
		glProgramUniform2f(this.program, glGetUniformLocation(this.program, name), x, y);
	}
	
	public void setUniform2f(String name, Vector2f vec)
	{
		this.setUniform2f(name, vec.x, vec.y);
	}
	
	public void setUniform3f(String name, float x, float y, float z)
	{
		glProgramUniform3f(this.program, glGetUniformLocation(this.program, name), x, y, z);
	}
	
	public void setUniform3f(String name, Vector3f vec)
	{
		this.setUniform3f(name, vec.x, vec.y, vec.z);
	}
	
	public void setUniform4f(String name, float x, float y, float z, float w)
	{
		glProgramUniform4f(this.program, glGetUniformLocation(this.program, name), x, y, z, w);
	}
	
	public void setUniform4f(String name, Vector4f vec)
	{
		this.setUniform4f(name, vec.x, vec.y, vec.z, vec.w);
	}
	
	public void setUniformMatrix4f(String name, Matrix4f mat)
	{
		glProgramUniformMatrix4fv(this.program, glGetUniformLocation(this.program, name), false, mat.get(new float[16]));
	}
}
