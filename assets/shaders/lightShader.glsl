#type vertex
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in float aTexId;

uniform mat4 uProjection;
uniform mat4 uView;

out vec4 fColor;
out vec2 fTexCoords;
out float fTexId;
out vec3 fPos;

void main()
{
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
    fColor = aColor;
    fTexCoords = aTexCoords;
    fTexId = aTexId;
    fPos = aPos;
}
#type fragment
#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexId;
in vec3 fPos;
out vec4 Color;

uniform mat4 uProjection;
uniform mat4 uView;
uniform sampler2D uTextures[8];

struct Light {
    vec4 color;
    vec2 position;
    float intensity;
    float ambientStrength;
};

uniform int numLights;
uniform Light lights[100];
uniform float brightness;

void main()
{
    vec4 finalColor = fColor;

    // Gestion des textures
    if(fTexId > 0){
        int id = int(fTexId);
        finalColor = texture(uTextures[id], fTexCoords) * fColor;
    } else {
        finalColor = fColor;
    }

    // Calcul de la lumière globale
    vec4 globalLight = finalColor * brightness; // Lumière globale affectée par brightness
    vec4 localLighting = vec4(0.0); // Lumière locale individuelle non affectée par brightness

    // Parcourir toutes les lumières


    // Ajout de l'effet de chaque lumière
    for (int i = 0; i < numLights; ++i) {
        vec4 ambient = lights[i].ambientStrength * lights[i].color;

        float intensity = max(0.001, lights[i].intensity);

        float distance = length(vec3(lights[i].position,0) - fPos) / intensity;
        float attenuation = clamp(1.0 - distance, -10000000, 10000000);
        localLighting += attenuation * ambient * finalColor;
    }
    //    globalLight.w = finalColor.w;
    vec4 result = globalLight + localLighting;
    result.w = finalColor.w;
    Color = result;
}