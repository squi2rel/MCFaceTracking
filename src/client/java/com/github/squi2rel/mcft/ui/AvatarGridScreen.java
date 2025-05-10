package com.github.squi2rel.mcft.ui;

import com.github.squi2rel.mcft.Config;
import com.github.squi2rel.mcft.FTClient;
import com.github.squi2rel.mcft.MCFT;
import com.github.squi2rel.mcft.MCFTClient;
import com.github.squi2rel.mcft.tracking.EyeTrackingRect;
import com.github.squi2rel.mcft.tracking.MouthTrackingRect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;

import static com.github.squi2rel.mcft.FTModel.model;
import static com.github.squi2rel.mcft.MCFTClient.config;

@SuppressWarnings("DataFlowIssue")
public class AvatarGridScreen extends GridScreen {
    private boolean showOverlay = true;
    private boolean preview = false;
    private static Selection eyeL, eyeR, mouth;
    private SettingsSlider<Float> eyeW, eyeH, eyeX, eyeY;

    public AvatarGridScreen() {
        super(Text.of("编辑选区"), 8, 128);
    }

    @Override
    protected void init() {
        super.init();
        int btnWidth = 100;
        int btnHeight = 20;
        int buttons = 9;
        int totalHeight = buttons * btnHeight + (buttons - 1) * 2;
        int y = (this.height - totalHeight) / 2;
        WidgetGroup group = new WidgetGroup();
        WidgetGroup previewGroup = new WidgetGroup();
        group.add(ButtonWidget.builder(Text.of("切换覆盖层"), b -> showOverlay = !showOverlay).dimensions(20, y, btnWidth, btnHeight).build());
        group.add(ButtonWidget.builder(Text.of("自由选择"), b -> {
            freeDrag = !freeDrag;
            b.setMessage(Text.of(freeDrag ? "吸附选择" : "自由选择"));
        }).dimensions(20, y + btnHeight + 2, btnWidth, btnHeight).build());
        group.add(ButtonWidget.builder(Text.of("标记为左眼"), b -> eyeL = getSelection()).dimensions(20, y + (btnHeight + 2) * 2, btnWidth, btnHeight).build());
        group.add(ButtonWidget.builder(Text.of("标记为右眼"), b -> eyeR = getSelection()).dimensions(20, y + (btnHeight + 2) * 3, btnWidth, btnHeight).build());
        if (!model.isFlat) group.add(ButtonWidget.builder(Text.of("标记为嘴巴"), b -> mouth = getSelection()).dimensions(20, y + (btnHeight + 2) * 4, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("预览效果"), b -> {
            group.visible(preview);
            preview = !preview;
            if (preview) save();
            previewGroup.visible(preview);
        }).dimensions(20, y + (btnHeight + 2) * 5, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("上一步"), b -> MinecraftClient.getInstance().setScreen(new UVGridScreen())).dimensions(20, y + (btnHeight + 2) * 6, btnWidth, btnHeight).build());
        group.add(ButtonWidget.builder(Text.of("重置"), b -> {
            eyeL = eyeR = mouth = null;
            MinecraftClient.getInstance().setScreen(new AvatarGridScreen());
        }).dimensions(20, y + (btnHeight + 2) * 7, btnWidth, btnHeight).build());
        previewGroup.add(ButtonWidget.builder(Text.of("重置"), b -> {
            eyeW.setValue(0.75f);
            eyeH.setValue(0.75f);
            eyeX.setValue(0.5f);
            eyeY.setValue(0.3f);
        }).dimensions(20, y + (btnHeight + 2) * 7, btnWidth, btnHeight).build());
        addDrawableChild(ButtonWidget.builder(Text.of("完成"), b -> {
            save();
            writeConfig();
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(20, y + (btnHeight + 2) * 8, btnWidth, btnHeight).build());
        eyeW = previewGroup.add(SettingsSlider.floatSlider(20, y, btnWidth, btnHeight, model.eyeR.ball.w, 0.25f, 4f, f -> {
            model.eyeR.ball.w(f);
            model.eyeL.ball.w(f);
        }, f -> String.format("眼球宽度: %.2f", f)));
        eyeH = previewGroup.add(SettingsSlider.floatSlider(20, y + btnHeight + 2, btnWidth, btnHeight, model.eyeR.ball.h, 0.25f, 4f, f -> {
            model.eyeR.ball.h(f);
            model.eyeL.ball.h(f);
        }, f -> String.format("眼球高度: %.2f", f)));
        if (model.isFlat) previewGroup.add(SettingsSlider.floatSlider(20, y + (btnHeight + 2) * 2, btnWidth, btnHeight, model.mouth.h, -3f, 3f, f -> model.mouth.h(f), f -> String.format("眉毛高度: %.2f", f)));
        eyeX = previewGroup.add(SettingsSlider.floatSlider(20, y + (btnHeight + 2) * 3, btnWidth, btnHeight, config.eyeXMul, 0.1f, 2f, f -> config.eyeXMul = f, f -> String.format("眼球X轴移动倍率: %.2f", f)));
        eyeY = previewGroup.add(SettingsSlider.floatSlider(20, y + (btnHeight + 2) * 4, btnWidth, btnHeight, config.eyeYMul, 0.1f, 2f, f -> config.eyeYMul = f, f -> String.format("眼球Y轴移动倍率: %.2f", f)));
        group.visible(!preview);
        previewGroup.visible(preview);
        gridX = (width * 3 / 2 - drawSize) / 2;
        gridY = (height - drawSize) / 2;
    }

    private void writeConfig() {
        Config config = MCFTClient.config;
        config.model = model;
        try {
            MCFT.saveConfig(config, MCFTClient.configPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save() {
        float d = (float) drawSize / gridLength;
        if (eyeR != null) {
            model.eyeR = new EyeTrackingRect(eyeR.x() / d, (eyeR.y() + eyeR.h()) / d, eyeR.w() / d, eyeR.h() / d);
            UVGridScreen.applyUV(UVGridScreen.eyeR, model.eyeR.ball);
            UVGridScreen.applyUV(UVGridScreen.lid, model.eyeR.lid);
            UVGridScreen.applyUV(UVGridScreen.inner, model.eyeR.inner);
        }
        if (eyeL != null) {
            model.eyeL = new EyeTrackingRect(eyeL.x() / d, (eyeL.y() + eyeL.h()) / d, eyeL.w() / d, eyeL.h() / d);
            UVGridScreen.applyUV(UVGridScreen.eyeL, model.eyeL.ball);
            UVGridScreen.applyUV(UVGridScreen.lid, model.eyeL.lid);
            UVGridScreen.applyUV(UVGridScreen.inner, model.eyeL.inner);
        }
        eyeW.applyValue();
        eyeH.applyValue();
        if (mouth != null && !model.isFlat) {
            model.mouth = new MouthTrackingRect(mouth.x() / d, (mouth.y() + mouth.h()) / d, mouth.w() / d, mouth.h() / d);
            UVGridScreen.applyUV(UVGridScreen.mouth, model.mouth);
        } else if (model.isFlat) {
            UVGridScreen.applyUV(UVGridScreen.mouth, model.mouth);
        }
        MinecraftClient.getInstance().execute(() -> FTClient.uploadParams(model));
    }

    @Override
    protected void drawGrid(DrawContext context, int x, int y) {
        super.drawGrid(context, x, y);

        drawSelection(context, eyeR, 0x5500FFFF);
        drawSelection(context, eyeL, 0x55FFFF00);
        if (!model.isFlat) drawSelection(context, mouth, 0x55FF00FF);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (preview) {
            renderModel(context);
        } else {
            renderHead(context);
        }
    }

    private void renderModel(DrawContext context) {
        InventoryScreen.drawEntity(context, gridX, gridY, gridX + drawSize, gridY + drawSize, 200, 0.8f, gridX + drawSize / 2f, gridY + drawSize / 2f, MinecraftClient.getInstance().player);
    }

    private void renderHead(DrawContext context) {
        Identifier skin = MinecraftClient.getInstance().player.getSkinTextures().texture();

        context.drawTexture(RenderLayer::getGuiTextured, skin, gridX, gridY, 8, 8, drawSize, drawSize, 8, 8, 64, 64);
        if (showOverlay) context.drawTexture(RenderLayer::getGuiTextured, skin, gridX - 8, gridY - 8, 40, 8, drawSize + 16, drawSize + 16, 8, 8, 64, 64);

        drawGrid(context, gridX, gridY);
    }
}