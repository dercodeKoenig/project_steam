package ARLib.obj;


import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Wavefront Object importer
 *  Based heavily off of the specifications found at http://en.wikipedia.org/wiki/Wavefront_.obj_file
 */
public class WavefrontObject {
    private static final Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(v( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static final Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *\\n)|(vn( (\\-){0,1}\\d+(\\.\\d+)?){3,4} *$)");
    private static final Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+(\\.\\d+)?){2,3} *$)");
    private static final Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
    private static final Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
    private static final Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
    private static final Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
    private static final Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

    private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
    private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
    private static Matcher groupObjectMatcher;

    public ArrayList<Vertex> vertices = new ArrayList<>();
    public ArrayList<Vertex> vertexNormals = new ArrayList<>();
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<>();
    public Map<String, GroupObject> groupObjects = new HashMap<>();
    public List<GroupObject> groupObjectsList = new ArrayList<>();
    private GroupObject currentGroupObject;
    private final String fileName;

    public WavefrontObject(ResourceLocation resource) throws ModelFormatException {
        this.fileName = resource.toString();

        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResource(resource).get();
            loadObjModel(res.open());
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }
    }

    public WavefrontObject(String filename, InputStream inputStream) throws ModelFormatException {
        this.fileName = filename;
        loadObjModel(inputStream);
    }

    private void loadObjModel(InputStream inputStream) throws ModelFormatException {
        BufferedReader reader = null;

        String currentLine;
        int lineCount = 0;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

                if (currentLine.startsWith("v ")) {
                    Vertex vertex = parseVertex(currentLine, lineCount);
                    if (vertex != null) {
                        vertices.add(vertex);
                    }
                } else if (currentLine.startsWith("vn ")) {
                    Vertex vertex = parseVertexNormal(currentLine, lineCount);
                    if (vertex != null) {
                        vertexNormals.add(vertex);
                    }
                } else if (currentLine.startsWith("vt ")) {
                    TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                    if (textureCoordinate != null) {
                        textureCoordinates.add(textureCoordinate);
                    }
                } else if (currentLine.startsWith("f ")) {

                    if (currentGroupObject == null) {
                        currentGroupObject = new GroupObject("Default");
                    }

                    Face face = parseFace(currentLine, lineCount);

                    currentGroupObject.faces.add(face);
                } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                    GroupObject group = parseGroupObject(currentLine, lineCount);

                    if (group != null) {
                        if (currentGroupObject != null) {
                            groupObjectsList.add(currentGroupObject);
                        }
                    }
                    currentGroupObject = group;
                }
            }
            groupObjectsList.add(currentGroupObject);

            for (GroupObject i : groupObjectsList){
                groupObjects.put(i.name,i);
                //System.out.println("added object '"+i.name+"'");
            }
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // hush
            }

            try {
                inputStream.close();
            } catch (IOException e) {
                // hush
            }
        }
    }

    public void renderAll(PoseStack stack, MultiBufferSource bufferSource, VertexFormat vertexFormat, RenderType.CompositeState compositeState, int packedLight, int packedOverlay, int color) {

        for (GroupObject groupObject : groupObjects.values()) {
            groupObject.render(stack, bufferSource, vertexFormat, compositeState, packedLight, packedOverlay, color);
        }
    }


    public void renderPart(String partName, PoseStack stack, MultiBufferSource bufferSource, VertexFormat vertexFormat, RenderType.CompositeState compositeState, int packedLight, int packedOverlay) {
        renderPart(partName,stack,bufferSource,vertexFormat,compositeState,packedLight,packedOverlay,0xFFFFFFFF);
    }
    public void renderPart(String partName, PoseStack stack, MultiBufferSource bufferSource, VertexFormat vertexFormat, RenderType.CompositeState compositeState, int packedLight, int packedOverlay, int color) {
                groupObjects.get(partName).render(stack, bufferSource, vertexFormat, compositeState, packedLight, packedOverlay, color);
    }
    public void rotateWorldSpace(String partName, Vector3f axis, float angleDegrees) {
        groupObjects.get(partName).rotateWorldSpace(axis,angleDegrees);
    }
    public void rotateModelSpace( String partName, Vector3f axis, float angleDegrees) {
        groupObjects.get(partName).rotateModelSpace(axis,angleDegrees);
    }
    public void translateWorldSpace(String partName, Vector3f translation) {
        groupObjects.get(partName).translateWorldSpace(translation);
    }
    public void translateModelSpace(String partName, Vector3f translation) {
        groupObjects.get(partName).translateModelSpace(translation);
    }
    public void applyTransformations(String partName) {
        groupObjects.get(partName).applyTransformations();
    }
    public void resetTransformations(String partName) {
        groupObjects.get(partName).resetTransformations();
    }
    public void scaleUV(String partName, float u0, float v0, float u1, float v1) {
        groupObjects.get(partName).scaleUV(u0,v0,u1,v1);
    }

    private Vertex parseVertex(String line, int lineCount) throws ModelFormatException {
        if (isValidVertexLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
                } else if (tokens.length == 3) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
                }
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }

        return null;
    }

    private Vertex parseVertexNormal(String line, int lineCount) throws ModelFormatException {
        if (isValidVertexNormalLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 3)
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }

        return null;
    }

    private TextureCoordinate parseTextureCoordinate(String line, int lineCount) throws ModelFormatException {
        if (isValidTextureCoordinateLine(line)) {
            line = line.substring(line.indexOf(" ") + 1);
            String[] tokens = line.split(" ");

            try {
                if (tokens.length == 2)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
                else if (tokens.length == 3)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }

        return null;
    }

    private Face parseFace(String line, int lineCount) throws ModelFormatException {
        Face face;

        if (isValidFaceLine(line)) {
            face = new Face();

            String trimmedLine = line.substring(line.indexOf(" ") + 1);
            String[] tokens = trimmedLine.split(" ");
            String[] subTokens;

            if (tokens.length == 3) {
                if (currentGroupObject.drawMode == VertexFormat.Mode.DEBUG_LINES) {
                    currentGroupObject.drawMode = VertexFormat.Mode.TRIANGLES;
                } else if (currentGroupObject.drawMode != VertexFormat.Mode.TRIANGLES) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 4, found " + tokens.length + ")");
                }
            } else if (tokens.length == 4) {
                if (currentGroupObject.drawMode == VertexFormat.Mode.DEBUG_LINES) {
                    currentGroupObject.drawMode = VertexFormat.Mode.QUADS;
                } else if (currentGroupObject.drawMode != VertexFormat.Mode.QUADS) {
                    throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 3, found " + tokens.length + ")");
                }
            }

            // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
            if (isValidFace_V_VT_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1/vt1 v2/vt2 v3/vt3 ...
            else if (isValidFace_V_VT_Line(line)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1//vn1 v2//vn2 v3//vn3 ...
            else if (isValidFace_V_VN_Line(line)) {
                face.vertices = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("//");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1 v2 v3 ...
            else if (isValidFace_V_Line(line)) {
                face.vertices = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else {
                throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }


        face.original_vertices = new Vertex[face.vertices.length];
        for (int i = 0; i < face.vertices.length; i++) {
            face.original_vertices[i] = new Vertex(face.vertices[i].x, face.vertices[i].y, face.vertices[i].z);
        }

        if(face.textureCoordinates != null) {
            face.original_textureCoordinates = new TextureCoordinate[face.textureCoordinates.length];
            for (int i = 0; i < face.textureCoordinates.length; i++) {
                face.original_textureCoordinates[i] = new TextureCoordinate(face.textureCoordinates[i].u, face.textureCoordinates[i].v, face.textureCoordinates[i].w);
            }
        }

        face.original_faceNormal = new Vertex(face.faceNormal.x, face.faceNormal.y, face.faceNormal.z);

        return face;
    }

    private GroupObject parseGroupObject(String line, int lineCount) throws ModelFormatException {
        GroupObject group = null;

        if (isValidGroupObjectLine(line)) {
            String trimmedLine = line.substring(line.indexOf(" ") + 1);

            if (trimmedLine.length() > 0) {
                group = new GroupObject(trimmedLine);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + line + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }

        return group;
    }

    /***
     * Verifies that the given line from the model file is a valid vertex
     * @param line the line being validated
     * @return true if the line is a valid vertex, false otherwise
     */
    private static boolean isValidVertexLine(String line) {
        if (vertexMatcher != null) {
            vertexMatcher.reset();
        }

        vertexMatcher = vertexPattern.matcher(line);
        return vertexMatcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid vertex normal
     * @param line the line being validated
     * @return true if the line is a valid vertex normal, false otherwise
     */
    private static boolean isValidVertexNormalLine(String line) {
        if (vertexNormalMatcher != null) {
            vertexNormalMatcher.reset();
        }

        vertexNormalMatcher = vertexNormalPattern.matcher(line);
        return vertexNormalMatcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid texture coordinate
     * @param line the line being validated
     * @return true if the line is a valid texture coordinate, false otherwise
     */
    private static boolean isValidTextureCoordinateLine(String line) {
        if (textureCoordinateMatcher != null) {
            textureCoordinateMatcher.reset();
        }

        textureCoordinateMatcher = textureCoordinatePattern.matcher(line);
        return textureCoordinateMatcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid face that is described by vertices, texture coordinates, and vertex normals
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_VN_Line(String line) {
        if (face_V_VT_VN_Matcher != null) {
            face_V_VT_VN_Matcher.reset();
        }

        face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(line);
        return face_V_VT_VN_Matcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid face that is described by vertices and texture coordinates
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_Line(String line) {
        if (face_V_VT_Matcher != null) {
            face_V_VT_Matcher.reset();
        }

        face_V_VT_Matcher = face_V_VT_Pattern.matcher(line);
        return face_V_VT_Matcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid face that is described by vertices and vertex normals
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1//vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VN_Line(String line) {
        if (face_V_VN_Matcher != null) {
            face_V_VN_Matcher.reset();
        }

        face_V_VN_Matcher = face_V_VN_Pattern.matcher(line);
        return face_V_VN_Matcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid face that is described by only vertices
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_Line(String line) {
        if (face_V_Matcher != null) {
            face_V_Matcher.reset();
        }

        face_V_Matcher = face_V_Pattern.matcher(line);
        return face_V_Matcher.matches();
    }

    /***
     * Verifies that the given line from the model file is a valid face of any of the possible face formats
     * @param line the line being validated
     * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
     */
    private static boolean isValidFaceLine(String line) {
        return isValidFace_V_VT_VN_Line(line) || isValidFace_V_VT_Line(line) || isValidFace_V_VN_Line(line) || isValidFace_V_Line(line);
    }

    /***
     * Verifies that the given line from the model file is a valid group (or object)
     * @param line the line being validated
     * @return true if the line is a valid group (or object), false otherwise
     */
    private static boolean isValidGroupObjectLine(String line) {
        if (groupObjectMatcher != null) {
            groupObjectMatcher.reset();
        }

        groupObjectMatcher = groupObjectPattern.matcher(line);
        return groupObjectMatcher.matches();
    }

    public String getType() {
        return "obj";
    }
}
