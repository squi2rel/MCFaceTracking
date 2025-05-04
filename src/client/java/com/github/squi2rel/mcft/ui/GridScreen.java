package com.github.squi2rel.mcft.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class GridScreen extends Screen {
    protected final int gridLength;
    protected final int drawSize;
    protected boolean selecting = false, freeDrag = false, allowSelect = true;
    protected int gridX, gridY;
    protected int selectedStartX = -1, selectedStartY = -1;
    protected int selectedEndX = -1, selectedEndY = -1;

    public GridScreen(Text title, int gridLength, int drawSize) {
        super(title);
        this.gridLength = gridLength;
        this.drawSize = drawSize;
    }

    protected Selection getSelection() {
        if (freeDrag) {
            int x1 = Math.min(selectedStartX, selectedEndX);
            int x2 = Math.max(selectedStartX, selectedEndX);
            int y1 = Math.min(selectedStartY, selectedEndY);
            int y2 = Math.max(selectedStartY, selectedEndY);
            return new Selection(x1, y1, x2 - x1, y2 - y1);
        } else {
            double cellSize = (double) drawSize / gridLength;
            int x1 = MathHelper.clamp((int) (selectedStartX / cellSize), 0, gridLength - 1);
            int y1 = MathHelper.clamp((int) (selectedStartY / cellSize), 0, gridLength - 1);
            int x2 = MathHelper.clamp((int) (selectedEndX / cellSize), 0, gridLength - 1);
            int y2 = MathHelper.clamp((int) (selectedEndY / cellSize), 0, gridLength - 1);
            int fx1 = (int) (Math.min(x1, x2) * cellSize);
            int fy1 = (int) (Math.min(y1, y2) * cellSize);
            int fx2 = (int) (Math.max(x1, x2) * cellSize + cellSize);
            int fy2 = (int) (Math.max(y1, y2) * cellSize + cellSize);
            return new Selection(fx1, fy1, fx2 - fx1, fy2 - fy1);
        }
    }

    protected void drawGrid(DrawContext context, int x, int y) {
        int cellSize = drawSize / gridLength;
        for (int i = 0; i <= gridLength; i++) {
            context.drawHorizontalLine(x, x + drawSize, y + i * cellSize, 0xFF000000);
            context.drawVerticalLine(x + i * cellSize, y, y + drawSize, 0xFF000000);
        }

        if(selectedStartX != -1 && selectedEndX != -1) {
            if (freeDrag) {
                context.fill(
                        MathHelper.clamp(gridX + selectedStartX, gridX, gridX + drawSize),
                        MathHelper.clamp(gridY + selectedStartY, gridY, gridY + drawSize),
                        MathHelper.clamp(gridX + selectedEndX, gridX, gridX + drawSize),
                        MathHelper.clamp(gridY + selectedEndY, gridY, gridY + drawSize),
                        0x55FF0000
                );
            } else {
                int x1 = MathHelper.clamp(selectedStartX / cellSize, 0, gridLength - 1) * cellSize;
                int y1 = MathHelper.clamp(selectedStartY / cellSize, 0, gridLength - 1) * cellSize;
                int x2 = MathHelper.clamp(selectedEndX / cellSize, 0, gridLength - 1) * cellSize;
                int y2 = MathHelper.clamp(selectedEndY / cellSize, 0, gridLength - 1) * cellSize;
                context.fill(
                        gridX + Math.min(x1, x2),
                        gridY + Math.min(y1, y2),
                        gridX + Math.max(x1, x2) + cellSize,
                        gridY + Math.max(y1, y2) + cellSize,
                        0x55FF0000
                );
            }
        }
    }

    protected void drawSelection(DrawContext context, Selection selection, int color) {
        if (selection == null) return;
        context.fill(gridX + selection.x, gridY + selection.y, gridX + selection.x + selection.w, gridY + selection.y + selection.h, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (allowSelect && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && mouseX >= gridX && mouseX <= gridX + drawSize && mouseY >= gridY && mouseY <= gridY + drawSize) {
            selecting = true;
            updateSelection(mouseX, mouseY, true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selecting && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            updateSelection(mouseX, mouseY, false);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) selecting = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSelection(double mouseX, double mouseY, boolean isStart) {
        double dx = mouseX - gridX, dy = mouseY - gridY;

        if (isStart) {
            selectedStartX = (int) dx;
            selectedStartY = (int) dy;
        }
        selectedEndX = (int) dx;
        selectedEndY = (int) dy;
    }

    public class WidgetGroup {
        private final ArrayList<ClickableWidget> widgets = new ArrayList<>();
        public <T extends ClickableWidget> T add(T e) {
            addDrawableChild(e);
            widgets.add(e);
            return e;
        }

        public void visible(boolean visible) {
            for (ClickableWidget widget : widgets) {
                widget.visible = visible;
            }
        }
    }

    public static class SettingsSlider<T extends Number> extends SliderWidget {
        private final double min;
        private final double max;
        private final Consumer<T> callback;
        private final Function<T, String> textProvider;
        private final Function<Double, T> converter;

        public SettingsSlider(int x, int y, int width, int height, T value, T min, T max, Consumer<T> callback, Function<T, String> textProvider, Function<Double, T> converter) {
            super(x, y, width, height, ScreenTexts.EMPTY, 0.0);
            this.min = min.doubleValue();
            this.max = max.doubleValue();
            this.value = (MathHelper.clamp(value.doubleValue(), min.doubleValue(), max.doubleValue()) -  min.doubleValue()) / (max.doubleValue() - min.doubleValue());
            this.callback = callback;
            this.textProvider = textProvider;
            this.converter = converter;
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            callback.accept(converter.apply(MathHelper.lerp(MathHelper.clamp(this.value, 0.0, 1.0), this.min, this.max)));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Text.of(textProvider.apply(converter.apply(MathHelper.lerp(MathHelper.clamp(this.value, 0.0, 1.0), this.min, this.max)))));
        }

        public static SettingsSlider<Float> floatSlider(int x, int y, int width, int height, float value, float min, float max, Consumer<Float> callback, Function<Float, String> textProvider) {
            return new SettingsSlider<>(x, y, width, height, value, min, max, callback, textProvider, Double::floatValue);
        }
    }

    public record Selection(int x, int y, int w, int h) {
    }
}
