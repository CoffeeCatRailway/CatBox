#version 330 core

const float ID_CIRCLE = 0.;
const float ID_BOX = 1.;
const float ID_LINE = 2.;

in float f_shapeId;
in vec3 f_color;
in vec2 f_size;
//in float f_rotation;
in float f_outline;
in vec2 f_uv;

out vec4 FragColor;

float sdCircle(in vec2 p, in float r)
{
	return length(p) - r;
}

float sdBox(in vec2 p, in vec2 b)
{
	vec2 d = abs(p) - b;
	return length(max(d, 0.)) + min(max(d.x, d.y), 0.);
}

void main()
{
	float l;
	if (f_shapeId == ID_CIRCLE)
		l = sdCircle(f_uv, 1.);
	else
		l = sdBox(f_uv, f_size / max(f_size.x, f_size.y));
	
	if (l > 0.)
		discard;
	if (l > -f_outline)
		FragColor =	vec4(0., 0., 0., 1.);
	else
		FragColor = vec4(f_color, 1.);
//	FragColor = vec4(f_uv * .5 + .5, 0., 1.);
}
