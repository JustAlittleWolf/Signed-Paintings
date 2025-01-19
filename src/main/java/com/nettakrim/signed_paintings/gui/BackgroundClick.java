package com.nettakrim.signed_paintings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;

public class BackgroundClick extends ClickableWidget {
    private final InputSlider[] sliders;

    public BackgroundClick(InputSlider[] sliders) {
        super(0, 0, 0, 0, null);
        this.sliders = sliders;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!(visible && isValidClickButton(button))) return false;
        for (InputSlider inputSlider : sliders) {
            if (inputSlider.isFocused()) return true;
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}
}
