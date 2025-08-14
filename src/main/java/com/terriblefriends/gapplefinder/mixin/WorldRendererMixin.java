package com.terriblefriends.gapplefinder.mixin;

import com.terriblefriends.gapplefinder.GappleFinderClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    /*@Redirect(method = "render", at=@At(value="INVOKE",target="Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"))
    private <E extends BlockEntity> void beamInjectionTwo(BlockEntityRenderDispatcher instance, E blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        MinecraftClient client = GapplefinderClient.client;

        client.worldRenderer.blockEntityRenderDispatcher.render(blockEntity, tickDelta, matrices, vertexConsumers);

        if (!GapplefinderClient.hasRenderedBeams) {
            Identifier BEAM_TEXTURE = new Identifier("textures/entity/beacon_beam.png");
            float[] color = DyeColor.YELLOW.getColorComponents();

            Vec3d vec3d = client.gameRenderer.getCamera().getPos();
            double cameraX = vec3d.getX();
            double cameraY = vec3d.getY();
            double cameraZ = vec3d.getZ();

            for (Object o : GapplefinderClient.chestList) {
                matrices.pop();
                matrices.push();
                BlockPos toRenderPos = (BlockPos) o;
                matrices.translate((double) toRenderPos.getX() - cameraX, (double) toRenderPos.getY() - cameraY, (double) toRenderPos.getZ() - cameraZ);
                renderBeam(matrices, vertexConsumers, BEAM_TEXTURE, tickDelta, 1.0F, client.world.getTime(), 0, 1024, color, 0.2F, 0.25F);
            }
            GapplefinderClient.hasRenderedBeams = true;
        }
    }*/

    @Inject(method="Lnet/minecraft/client/render/WorldRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",at=@At("TAIL"))
    private void beamInjectionTwo(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        MinecraftClient client = GappleFinderClient.client;

        //if (!GapplefinderClient.hasRenderedBeams) {
            Identifier BEAM_TEXTURE = new Identifier("textures/entity/beacon_beam.png");
            float[] color = DyeColor.YELLOW.getColorComponents();

            Vec3d vec3d = client.gameRenderer.getCamera().getPos();
            double cameraX = vec3d.getX();
            double cameraY = vec3d.getY();
            double cameraZ = vec3d.getZ();

            Iterator chestListIterator = GappleFinderClient.chestList.iterator();

            while (chestListIterator.hasNext()) {
                matrices.push();
                BlockPos toRenderPos = (BlockPos) chestListIterator.next();
                matrices.translate((double) toRenderPos.getX() - cameraX, (double) toRenderPos.getY() - cameraY, (double) toRenderPos.getZ() - cameraZ);
                if (chestListIterator.hasNext()) {
                    renderBeam(matrices, client.getBufferBuilders().getEntityVertexConsumers(), BEAM_TEXTURE, tickDelta, 1.0F, client.world.getTime(), 0, 1024, color, 0.2F, 0.25F);
                }
                else {renderBeam(matrices, client.getBufferBuilders().getEntityVertexConsumers(), BEAM_TEXTURE, tickDelta, 1.0F, client.world.getTime(), 0, 1024, color, 0.2F, 0);

                }
                matrices.pop();
            }
            //GapplefinderClient.hasRenderedBeams = true;
        //}
    }

    private static void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier textureId, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, float[] color, float innerRadius, float outerRadius) {
        int i = yOffset + maxY;
        matrices.push();
        matrices.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(worldTime, 40) + tickDelta;
        float g = maxY < 0 ? f : -f;
        float h = MathHelper.fractionalPart(g * 0.2F - (float)MathHelper.floor(g * 0.1F));
        float j = color[0];
        float k = color[1];
        float l = color[2];
        matrices.push();
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(f * 2.25F - 45.0F));
        float m = 0.0F;
        float p = 0.0F;
        float q = -innerRadius;
        float r = 0.0F;
        float s = 0.0F;
        float t = -innerRadius;
        float u = 0.0F;
        float v = 1.0F;
        float w = -1.0F + h;
        float x = (float)maxY * heightScale * (0.5F / innerRadius) + w;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, false)), j, k, l, 1.0F, yOffset, i, 0.0F, innerRadius, innerRadius, 0.0F, q, 0.0F, 0.0F, t, 0.0F, 1.0F, x, w);
        matrices.pop();
        m = -outerRadius;
        float n = -outerRadius;
        p = -outerRadius;
        q = -outerRadius;
        u = 0.0F;
        v = 1.0F;
        w = -1.0F + h;
        x = (float)maxY * heightScale + w;
        renderBeamLayer(matrices, vertexConsumers.getBuffer(RenderLayer.getBeaconBeam(textureId, true)), j, k, l, 0.125F, yOffset, i, m, n, outerRadius, p, q, outerRadius, outerRadius, outerRadius, 0.0F, 1.0F, x, w);
        matrices.pop();
    }

    private static void renderBeamLayer(MatrixStack matrices, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float u1, float u2, float v1, float v2) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f matrix4f = entry.getPositionMatrix();
        Matrix3f matrix3f = entry.getNormalMatrix();
        renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x1, z1, x2, z2, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x4, z4, x3, z3, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x2, z2, x4, z4, u1, u2, v1, v2);
        renderBeamFace(matrix4f, matrix3f, vertices, red, green, blue, alpha, yOffset, height, x3, z3, x1, z1, u1, u2, v1, v2);
    }

    private static void renderBeamFace(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int yOffset, int height, float x1, float z1, float x2, float z2, float u1, float u2, float v1, float v2) {
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x1, z1, u2, v1);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x1, z1, u2, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, yOffset, x2, z2, u1, v2);
        renderBeamVertex(positionMatrix, normalMatrix, vertices, red, green, blue, alpha, height, x2, z2, u1, v1);
    }

    private static void renderBeamVertex(Matrix4f positionMatrix, Matrix3f normalMatrix, VertexConsumer vertices, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        vertices.vertex(positionMatrix, x, (float)y, z).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
    }
}
