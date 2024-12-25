package ARLib.obj;

import ARLib.obj.TextureCoordinate;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class Face {
    public Vertex[] vertices;
    public Vertex faceNormal;
    public TextureCoordinate[] textureCoordinates;

    public Vertex[] original_vertices;
    public Vertex original_faceNormal;
    public TextureCoordinate[] original_textureCoordinates;

    // Apply transformations to vertices
    public void applyTransformations(Matrix4f transformationMatrix) {
        for (int i = 0; i < original_vertices.length; i++) {
            Vector3f vertexPos = new Vector3f(original_vertices[i].x, original_vertices[i].y, original_vertices[i].z);
            vertexPos.mulPosition(transformationMatrix); // Apply matrix to the vertex position
            vertices[i].x = vertexPos.x;
            vertices[i].y = vertexPos.y;
            vertices[i].z = vertexPos.z;
        }

        // Transform face normal
        Vector3f normalVec = new Vector3f(original_faceNormal.x, original_faceNormal.y, original_faceNormal.z);
        normalVec.mulDirection(transformationMatrix); // Apply matrix to the normal
        faceNormal = new Vertex(normalVec.x, normalVec.y, normalVec.z);
    }

    // Recalculate face normal
    public Vertex calculateFaceNormal() {
        Vec3 v1 = new Vec3(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vec3 v2 = new Vec3(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);

        double nx = v1.y * v2.z - v1.z * v2.y;
        double ny = v1.z * v2.x - v1.x * v2.z;
        double nz = v1.x * v2.y - v1.y * v2.x;

        Vec3 normalVector = new Vec3(nx, ny, nz).normalize();
        return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
    }

    // Render logic (unchanged but uses transformed vertices)
    public void addFaceForRender(PoseStack stack, VertexConsumer v, int packedLight, int packedOverlay, int color) {
        if (faceNormal == null) {
            faceNormal = this.calculateFaceNormal();
        }

        for (int i = 0; i < vertices.length; ++i) {
            if (textureCoordinates != null && textureCoordinates.length > 0) {
                v.addVertex(stack.last(), vertices[i].x, vertices[i].y, vertices[i].z)
                        .setNormal(faceNormal.x, faceNormal.y, faceNormal.z)
                        .setColor(color)
                        .setLight(packedLight)
                        .setOverlay(packedOverlay)
                        .setUv(textureCoordinates[i].u, textureCoordinates[i].v);
            }else{
                v.addVertex(stack.last(), vertices[i].x, vertices[i].y, vertices[i].z)
                        .setNormal(faceNormal.x, faceNormal.y, faceNormal.z)
                        .setColor(color)
                        .setLight(packedLight)
                        .setOverlay(packedOverlay);
            }
        }
    }
    public void scaleUV(float u0, float v0, float u1, float v1){
        if(original_textureCoordinates != null) {
            for (int i = 0; i < original_textureCoordinates.length; i++) {
                textureCoordinates[i].u = u0 + original_textureCoordinates[i].u * (u1 - u0);
                textureCoordinates[i].v = v0 + original_textureCoordinates[i].v * (v1 - v0);
            }
        }
    }
}
