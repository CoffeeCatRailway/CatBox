package io.github.coffeecatrailway.engine.renderer;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL45.*;

public class ShapeRenderer
{
	/*
	 * Shader data:
	 * - float id (circle, box, line)
	 * - float2 pos
	 * - float3 color
	 * - float2 size (x=radius/length)
	 * - float rotation
	 * - float outline (0-1, 0=no outline)
	 *
	 * id treated as float (wasteful but convenient)
	 * Floats: 10
	 * Bytes: 40
	 */
	
	private static final int FLOATS = 10;
	
	private static final float ID_CIRCLE = 0.f;
	private static final float ID_BOX = 1.f;
	private static final float ID_LINE = 2.f;
	
	private FloatBuffer buffer;
	private Shader shader;
	private int vao, vbo;
	
	public ShapeRenderer(int defaultShapeCount)
	{
		this.buffer = MemoryUtil.memCallocFloat(defaultShapeCount * FLOATS);
	}
	
	public void init()
	{
		System.out.println("Initializing shape renderer");
		this.shader = new Shader("/shaders/shape.vert", "/shaders/shape.geom", "/shaders/shape.frag");
		
		this.vao = glCreateVertexArrays();
		this.vbo = glCreateBuffers();
		glBindVertexArray(this.vao);
		
		this.buffer.clear();
		glNamedBufferData(this.vbo, this.buffer, GL_DYNAMIC_DRAW);
		glVertexArrayVertexBuffer(this.vao, 0, this.vbo, 0, FLOATS * Float.BYTES);
		
		int locId = this.shader.getAttribLocation("i_shapeId");
		int locPos = this.shader.getAttribLocation("i_position");
		int locColor = this.shader.getAttribLocation("i_color");
		int locSize = this.shader.getAttribLocation("i_size");
		int locRotation = this.shader.getAttribLocation("i_rotation");
		int locOutline = this.shader.getAttribLocation("i_outline");
		
		int offset = 0;
		glVertexArrayAttribFormat(this.vao, locId, 1, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locId, 0);
		offset += Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locPos, 2, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locPos, 0);
		offset += 2 * Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locColor, 3, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locColor, 0);
		offset += 3 * Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locSize, 2, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locSize, 0);
		offset += 2 * Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locRotation, 1, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locRotation, 0);
		offset += Float.BYTES;
		
		glVertexArrayAttribFormat(this.vao, locOutline, 1, GL_FLOAT, false, offset);
		glVertexArrayAttribBinding(this.vao, locOutline, 0);
//		offset += Float.BYTES;
		
		glEnableVertexArrayAttrib(this.vao, locId);
		glEnableVertexArrayAttrib(this.vao, locPos);
		glEnableVertexArrayAttrib(this.vao, locColor);
		glEnableVertexArrayAttrib(this.vao, locSize);
		glEnableVertexArrayAttrib(this.vao, locRotation);
		glEnableVertexArrayAttrib(this.vao, locOutline);
		
		this.buffer.clear();
		this.buffer.rewind();
	}
	
	private void checkSpace()
	{
		if (this.buffer.position() >= this.buffer.capacity() - 1)
			this.buffer = MemoryUtil.memRealloc(this.buffer, this.buffer.capacity() + FLOATS);
	}
	
	public void pushCircle(Vector2f pos, Vector3f color, float radius, float outline)
	{
		this.checkSpace();
		
		this.buffer.put(ID_CIRCLE);
		
		this.buffer.put(pos.x);
		this.buffer.put(pos.y);
		
		this.buffer.put(color.x);
		this.buffer.put(color.y);
		this.buffer.put(color.z);
		
		this.buffer.put(radius);
		this.buffer.put(0.f); // size.y
		
		this.buffer.put(0.f); // rotation
		
		this.buffer.put(outline);
	}
	
	public void pushBox(Vector2f pos, Vector3f color, Vector2f size, float rotation, float outline)
	{
		this.checkSpace();
		
		this.buffer.put(ID_BOX);
		
		this.buffer.put(pos.x);
		this.buffer.put(pos.y);
		
		this.buffer.put(color.x);
		this.buffer.put(color.y);
		this.buffer.put(color.z);
		
		this.buffer.put(size.x);
		this.buffer.put(size.y);
		
		this.buffer.put(rotation);
		
		this.buffer.put(outline);
	}
	
	public void pushLine(Vector2f pos, Vector3f color, float length, float thickness, float rotation, float outline)
	{
		this.checkSpace();
		
		this.buffer.put(ID_LINE);
		
		this.buffer.put(pos.x);
		this.buffer.put(pos.y);
		
		this.buffer.put(color.x);
		this.buffer.put(color.y);
		this.buffer.put(color.z);
		
		this.buffer.put(length);
		this.buffer.put(thickness);
		
		this.buffer.put(rotation);
		
		this.buffer.put(outline);
	}
	
	public void pushLine(Vector2f p1, Vector2f p2, Vector3f color, float thickness, float outline)
	{
		this.checkSpace();
		
		Vector2f delta = new Vector2f(p2).sub(p1);
		
		this.buffer.put(ID_LINE);
		
		this.buffer.put(p1.x);
		this.buffer.put(p1.y);
		
		this.buffer.put(color.x);
		this.buffer.put(color.y);
		this.buffer.put(color.z);
		
		this.buffer.put(delta.length());
		this.buffer.put(thickness);
		
		this.buffer.put(Math.atan2(delta.y, delta.x));
		
		this.buffer.put(outline);
	}
	
	public void drawFlush(Matrix4f pvm)
	{
		if (this.buffer.position() < FLOATS - 1)
			return;
		
		this.shader.bind();
		this.shader.setUniformMatrix4f("u_transform", pvm);
		
		int drawCount = this.buffer.position() / FLOATS;
		
		this.buffer.clear();
		glNamedBufferData(this.vbo, this.buffer, GL_DYNAMIC_DRAW);
//		glNamedBufferSubData(this.vbo, 0L, this.buffer);
		
		glBindVertexArray(this.vao);
		glDrawArrays(GL_POINTS, 0, drawCount);
		
		this.buffer.rewind();
	}
	
	public void destroy()
	{
		System.out.println("Destroying shape renderer");
		this.shader.destroy();
		glDeleteBuffers(this.vbo);
		glDeleteVertexArrays(this.vao);
		MemoryUtil.memFree(this.buffer);
		this.buffer = null;
	}
}
