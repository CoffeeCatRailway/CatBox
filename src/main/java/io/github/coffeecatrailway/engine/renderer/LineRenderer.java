package io.github.coffeecatrailway.engine.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.opengl.GL45.glEnableVertexArrayAttrib;

public class LineRenderer
{
	/*
	 * Shader data:
	 * - float2 pos1
	 * - float3 color1
	 * - float2 pos2
	 * - float3 color2
	 *
	 * Floats: 5
	 * Bytes: 20
	 */
	
	private static final int FLOATS = 5;
	
	private FloatBuffer buffer;
	private Shader shader;
	private int vao, vbo;
	
	public LineRenderer(int defaultShapeCount)
	{
		this.buffer = MemoryUtil.memCallocFloat(defaultShapeCount * FLOATS * 2);
	}
	
	public void init()
	{
		System.out.println("Initializing line renderer");
		this.shader = new Shader("/shaders/line.vert", "/shaders/line.frag");
		
		this.vao = glCreateVertexArrays();
		this.vbo = glCreateBuffers();
		glBindVertexArray(this.vao);
		
		this.buffer.clear();
		glNamedBufferData(this.vbo, this.buffer, GL_DYNAMIC_DRAW);
		glVertexArrayVertexBuffer(this.vao, 0, this.vbo, 0, FLOATS * Float.BYTES);
		
		int locPos = this.shader.getAttribLocation("i_position");
		int locColor = this.shader.getAttribLocation("i_color");
		
		int offset = 0;
		glVertexArrayAttribFormat(this.vao, locPos, 2, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locPos, 0);
		offset += 2 * Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locColor, 3, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locColor, 0);
//		offset += 3 * Float.BYTES;
		
		glEnableVertexArrayAttrib(this.vao, locPos);
		glEnableVertexArrayAttrib(this.vao, locColor);
		
		this.buffer.clear();
		this.buffer.rewind();
	}
	
	private void checkSpace()
	{
		if (this.buffer.position() >= this.buffer.capacity() - 1)
			this.buffer = MemoryUtil.memRealloc(this.buffer, this.buffer.capacity() + FLOATS);
	}
	
	public void pushLine(Vector2f pos1, Vector3f color1, Vector2f pos2, Vector3f color2)
	{
		this.checkSpace();
		
		this.buffer.put(pos1.x);
		this.buffer.put(pos1.y);
		
		this.buffer.put(color1.x);
		this.buffer.put(color1.y);
		this.buffer.put(color1.z);
		
		this.buffer.put(pos2.x);
		this.buffer.put(pos2.y);
		
		this.buffer.put(color2.x);
		this.buffer.put(color2.y);
		this.buffer.put(color2.z);
	}
	
	public void drawFlush(Matrix4f transformMatrix)
	{
		if (this.buffer.position() < FLOATS - 1)
			return;
		
		this.shader.bind();
		this.shader.setUniformMatrix4f("u_transform", transformMatrix);
		
		int drawCount = this.buffer.position() / FLOATS;
		
		this.buffer.clear();
		glNamedBufferData(this.vbo, this.buffer, GL_DYNAMIC_DRAW);
//		glNamedBufferSubData(this.vbo, 0L, this.buffer);
		
		glBindVertexArray(this.vao);
		glDrawArrays(GL_LINES, 0, drawCount);
		
		this.buffer.rewind();
	}
	
	public void destroy()
	{
		System.out.println("Destroying line renderer");
		this.shader.destroy();
		glDeleteBuffers(this.vbo);
		glDeleteVertexArrays(this.vao);
		MemoryUtil.memFree(this.buffer);
		this.buffer = null;
	}
}
