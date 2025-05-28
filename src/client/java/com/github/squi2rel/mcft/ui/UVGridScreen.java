package com.github.squi2rel.mcft.ui;

import com.github.squi2rel.mcft.tracking.TrackingRect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

import static com.github.squi2rel.mcft.FTModel.model;

public class UVGridScreen extends GridScreen {
    public static Selection eyeL, eyeR, mouth, lid, inner;

    public UVGridScreen() {
        super(Text.of("编辑材质"), 32, 128);
    }

    @Override
    protected void init() {
        super.init();
        int btnWidth = 100;
        int btnHeight = 20;
        int buttons = 10;
        int totalHeight = buttons * btnHeight + (buttons - 1) * 2;
        int y = (this.height - totalHeight) / 2;
        WidgetGroup group3D = new WidgetGroup();
        WidgetGroup groupFlat = new WidgetGroup();
        addDrawableChild(ButtonWidget.builder(Text.of(model.isFlat ? "切换为3D模型" : "切换为平面模型"), b -> {
            model.isFlat = !model.isFlat;
            b.setMessage(Text.of(model.isFlat ? "切换为3D模型" : "切换为平面模型"));
            groupFlat.visible(model.isFlat);
            group3D.visible(!model.isFlat);
        }).dimensions(20, y, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("标记眼白材质"), b -> inner = getSelection()).dimensions(20, y + btnHeight + 2, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("标记眼皮材质"), b -> lid = getSelection()).dimensions(20, y + (btnHeight + 2) * 2, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("标记左眼珠材质"), b -> eyeL = getSelection()).dimensions(20, y + (btnHeight + 2) * 3, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("标记右眼珠材质"), b -> eyeR = getSelection()).dimensions(20, y + (btnHeight + 2) * 4, btnWidth, btnHeight).build());
        group3D.add(ButtonWidget.builder(Text.of("标记嘴巴材质"), b -> mouth = getSelection()).dimensions(20, y + (btnHeight + 2) * 5, btnWidth, btnHeight).build());
        groupFlat.add(ButtonWidget.builder(Text.of("标记眉毛材质"), b -> mouth = getSelection()).dimensions(20, y + (btnHeight + 2) * 5, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("下一步"), b -> MinecraftClient.getInstance().setScreen(new AvatarGridScreen())).dimensions(20, y + (btnHeight + 2) * 6, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("重置"), b -> inner = lid = eyeL = eyeR = mouth = null).dimensions(20, y + (btnHeight + 2) * 7, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("关闭"), b -> MinecraftClient.getInstance().setScreen(null)).dimensions(20, y + (btnHeight + 2) * 8, btnWidth, btnHeight).build());
        groupFlat.visible(model.isFlat);
        group3D.visible(!model.isFlat);
        gridX = (width * 3 / 2 - drawSize) / 2;
        gridY = (height - drawSize) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        Identifier skin = Objects.requireNonNull(MinecraftClient.getInstance().player).getSkinTextures().texture();

        context.drawTexture(RenderLayer::getGuiTextured, skin, gridX, gridY, 0, 0, drawSize, drawSize / 2, 32, 16, 64, 64);
        context.drawTexture(RenderLayer::getGuiTextured, skin, gridX, gridY + drawSize / 2, 32, 0, drawSize, drawSize / 2, 32, 16, 64, 64);

        drawGrid(context, gridX, gridY);

        drawSelection(context, inner, 0x5500FF00);
        drawSelection(context, lid, 0x550000FF);
        drawSelection(context, eyeR, 0x5500FFFF);
        drawSelection(context, eyeL, 0x55FFFF00);
        drawSelection(context, mouth, 0x55FF00FF);
    }

    public static void applyUV(Selection s, TrackingRect rect) {
        float drawSize = 128;
        if (s == null) return;
        if (s.y() > 63) {
            rect.uv((s.x() + drawSize) / drawSize / 2, (s.y() - drawSize / 2) / drawSize / 2, (s.x() + s.w() + drawSize) / drawSize / 2, (s.y() + s.h() - drawSize / 2) / drawSize / 2);
        } else {
            rect.uv((float) s.x() / drawSize / 2, (float) s.y() / drawSize / 2, (float) (s.x() + s.w()) / drawSize / 2, (float) (s.y() + s.h()) / drawSize / 2);
        }
    }
}
