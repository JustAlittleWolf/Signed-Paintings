package com.nettakrim.signed_paintings.gui;

import com.nettakrim.signed_paintings.SignedPaintingsClient;
import com.nettakrim.signed_paintings.access.SignBlockEntityAccessor;
import com.nettakrim.signed_paintings.rendering.BackType;
import com.nettakrim.signed_paintings.rendering.Centering;
import com.nettakrim.signed_paintings.rendering.PaintingInfo;
import com.nettakrim.signed_paintings.rendering.SignSideInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.Clipboard;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Locale;

public class UIHelper {

    public static final int BUTTON_HEIGHT = 14;
    private static final int PADDING = 10;
    private static final int AREA_SIZE = BUTTON_HEIGHT * 2 + 5;

    private static final InputSlider[] inputSliders = new InputSlider[9];
    private static final ArrayList<ClickableWidget> buttons = new ArrayList<>();

    private static Screen screen;

    private static boolean front;
    private static int screenWidth;
    private static boolean aspectLocked = true;
    private static boolean isBackgroundEnabled = true;
    private static float aspectRatio;
    private static PaintingInfo info;
    private static ButtonWidget backModeButton;

    private static Vector3f offsetVec;
    private static Vector3f rotationVec;

    public static void init(boolean isFront, Screen screen, SignBlockEntityAccessor blockEntity) {
        UIHelper.front = isFront;
        UIHelper.screenWidth = screen.width;
        UIHelper.screen = screen;

        buttons.clear();
        addCenteringButtons();

        float width;
        float height;
        BackType.Type backType;
        float pixelsPerBlock;

        info = front ? blockEntity.signedPaintings$getFrontPaintingInfo() : blockEntity.signedPaintings$getBackPaintingInfo();
        if (info == null) {
            width = 1f;
            height = 1f;
            backType = BackType.Type.SIGN;
            pixelsPerBlock = 0;
            offsetVec = new Vector3f(0, 0, 0);
            rotationVec = new Vector3f(0, 0, 0);
        } else {
            width = info.getWidth();
            height = info.getHeight();
            backType = info.getBackType();
            offsetVec = info.offsetVec;
            rotationVec = info.rotationVec;
            pixelsPerBlock = info.getPixelsPerBlock();
            info.working = true;
        }

        inputSliders[0] = createSizingSlider(Centering.Type.MAX, AREA_SIZE, 50, 50, BUTTON_HEIGHT, 5, SignedPaintingsClient.MODID + ".size.x", width);
        createLockingButton(Centering.Type.CENTER, AREA_SIZE, BUTTON_HEIGHT, getAspectLockIcon(aspectLocked));
        createResetButton(Centering.Type.CENTER, AREA_SIZE, 80, BUTTON_HEIGHT, Text.translatable(SignedPaintingsClient.MODID + ".size.reset"));
        inputSliders[1] = createSizingSlider(Centering.Type.MIN, AREA_SIZE, 50, 50, BUTTON_HEIGHT, 5, SignedPaintingsClient.MODID + ".size.y", height);

        inputSliders[0].setOnValueChanged(value -> onSizeSliderChanged(value, true));
        inputSliders[1].setOnValueChanged(value -> onSizeSliderChanged(value, false));
        aspectRatio = width / height;

        createBackModeButton(104, BUTTON_HEIGHT, backType);
        inputSliders[2] = createPixelSlider(50, 50, BUTTON_HEIGHT, 5, SignedPaintingsClient.MODID + ".pixels_per_block", pixelsPerBlock);
        inputSliders[2].setOnValueChanged(UIHelper::onPixelSliderChanged);
        createBackgroundButton(104, BUTTON_HEIGHT);

        inputSliders[3] = createOffsetSlider(0, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".offset_x", offsetVec.x);
        inputSliders[3].setOnValueChanged(UIHelper::onXOffsetSliderChanged);
        inputSliders[4] = createOffsetSlider(16, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".offset_y", offsetVec.y);
        inputSliders[4].setOnValueChanged(UIHelper::onYOffsetSliderChanged);
        inputSliders[5] = createOffsetSlider(32, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".offset_z", offsetVec.z);
        inputSliders[5].setOnValueChanged(UIHelper::onZOffsetSliderChanged);

        inputSliders[6] = createRotateSlider(0, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".rotation_x", rotationVec.x);
        inputSliders[6].setOnValueChanged(UIHelper::onXRotationSliderChanged);
        inputSliders[7] = createRotateSlider(16, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".rotation_y", rotationVec.y);
        inputSliders[7].setOnValueChanged(UIHelper::onYRotationSliderChanged);
        inputSliders[8] = createRotateSlider(32, 45, 58, BUTTON_HEIGHT, 15, SignedPaintingsClient.MODID + ".rotation_z", rotationVec.z);
        inputSliders[8].setOnValueChanged(UIHelper::onZRotationSliderChanged);

        createCopyUrlButton();
        createCopyUncompressedButton();
    }

    private static void addCenteringButtons() {
        Centering.Type[] centering = Centering.Type.values();
        //x centering is reversed to make the buttons have a sensible order when using tab
        for (Centering.Type yCentering : centering) {
            for (int i = 0; i < centering.length; i++) {
                createCenteringButton(centering[centering.length - 1 - i], yCentering);
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination") // Square button
    private static void createCenteringButton(Centering.Type xCentering, Centering.Type yCentering) {
        String id = (Centering.getName(true, xCentering) + Centering.getName(false, yCentering)).toLowerCase(Locale.ROOT);
        int xPos = getCenteringButtonPosition(AREA_SIZE, xCentering, BUTTON_HEIGHT, 0) + (AREA_SIZE / 2) + (BUTTON_HEIGHT / 2) + PADDING;
        int yPos = -getCenteringButtonPosition(AREA_SIZE, yCentering, BUTTON_HEIGHT, 0) + (AREA_SIZE / 2) - (BUTTON_HEIGHT / 2) + PADDING;
        ButtonWidget widget = ButtonWidget.builder(Text.translatable(SignedPaintingsClient.MODID + ".align." + id),
                        button -> SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingCentering(xCentering, yCentering))
                .position(xPos, yPos)
                .size(BUTTON_HEIGHT, BUTTON_HEIGHT)
                .build();

        buttons.add(widget);
    }

    private static int getCenteringButtonPosition(int size, Centering.Type centering, int buttonSize, int screenSize) {
        return MathHelper.floor(Centering.getOffset(size, centering)) + screenSize / 2 - buttonSize / 2;
    }

    private static void createBackModeButton(int buttonWidth, int buttonHeight, BackType.Type backType) {
        backModeButton = ButtonWidget.builder(getBackTypeText(backType), UIHelper::cyclePaintingBack)
                .position(screenWidth - PADDING - buttonWidth,
                        58)
                .size(buttonWidth, buttonHeight)
                .build();
        buttons.add(backModeButton);
    }

    private static void createBackgroundButton(int buttonWidth, int buttonHeight) {
        ButtonWidget backgroundButton = ButtonWidget.builder(getBackgroundText(isBackgroundEnabled), UIHelper::cycleBackground)
                .position(screenWidth - PADDING - buttonWidth, 90)
                .size(buttonWidth, buttonHeight)
                .build();
        buttons.add(backgroundButton);
    }

    private static void createCopyUrlButton() {
        ButtonWidget widget = ButtonWidget.builder(Text.translatable(SignedPaintingsClient.MODID + ".copy_url"),
                        button -> {
                            copyToClipboard(SignedPaintingsClient.currentSignEdit.getSideInfo(front).getUrl());
                            screen.close();
                        })
                .position(PADDING, 64)
                .size(46, BUTTON_HEIGHT)
                .build();

        buttons.add(widget);
    }

    private static void createCopyUncompressedButton() {
        ButtonWidget widget = ButtonWidget.builder(Text.translatable(SignedPaintingsClient.MODID + ".copy_data"),
                        button -> {
                            copyToClipboard(SignedPaintingsClient.currentSignEdit.getSideInfo(front).getData());
                            screen.close();
                        })
                .position(PADDING + 46 + 10, 64)
                .size(48, BUTTON_HEIGHT)
                .build();

        buttons.add(widget);
    }

    private static InputSlider createSizingSlider(Centering.Type centering, int areaSize, int textWidth, int sliderWidth, int widgetHeight, int elementSpacing, String key, float startingValue) {
        int x = screenWidth - PADDING - textWidth - sliderWidth - elementSpacing + 1;
        int y = getCenteringButtonPosition(areaSize, centering, widgetHeight, 0) + (areaSize / 2) + (widgetHeight / 2) + PADDING;
        InputSlider inputSlider = new InputSlider(x, y, textWidth, sliderWidth, widgetHeight, elementSpacing, 0.5f, 10f, 0.5f, startingValue, 1 / 32f, 64f, Text.translatable(key));
        buttons.add(inputSlider.sliderWidget);
        buttons.add(inputSlider.textFieldWidget);
        return inputSlider;
    }

    private static void createLockingButton(Centering.Type centering, int areaSize, int buttonSize, Text text) {
        ButtonWidget widget = ButtonWidget.builder(text, UIHelper::toggleAspectLock)
                .position(screenWidth - buttonSize - PADDING - 90,
                        getCenteringButtonPosition(areaSize, centering, buttonSize, 0) + (areaSize / 2) + (buttonSize / 2) + PADDING)
                .size(buttonSize, buttonSize)
                .build();
        buttons.add(widget);
    }

    private static void createResetButton(Centering.Type centering, int areaSize, int buttonWidth, int buttonHeight, Text text) {
        ButtonWidget widget = ButtonWidget.builder(text, UIHelper::resetSize)
                .position(screenWidth - buttonWidth - PADDING,
                        getCenteringButtonPosition(areaSize, centering, buttonHeight, 0) + (areaSize / 2) + (buttonHeight / 2) + PADDING)
                .size(buttonWidth, buttonHeight)
                .build();
        buttons.add(widget);
    }

    private static InputSlider createOffsetSlider(int yOffset, int textWidth, int sliderWidth, int widgetHeight, int elementSpacing, String key, float startingValue) {
        int x = PADDING;
        int y = 64 + BUTTON_HEIGHT + 4 + yOffset;
        InputSlider inputSlider = new InputSlider(x, y, textWidth, sliderWidth, widgetHeight, elementSpacing, -8f, 8f, 0.25f, startingValue, -64f, 64f, Text.translatable(key));
        buttons.add(inputSlider.sliderWidget);
        buttons.add(inputSlider.textFieldWidget);
        return inputSlider;
    }

    private static InputSlider createRotateSlider(int yOffset, int textWidth, int sliderWidth, int widgetHeight, int elementSpacing, String key, float startingValue) {
        int x = PADDING;
        int y = yOffset + 132;
        InputSlider inputSlider = new InputSlider(x, y, textWidth, sliderWidth, widgetHeight, elementSpacing, -180f, 180f, 22.5f, startingValue, -360f, 360f, Text.translatable(key));
        buttons.add(inputSlider.sliderWidget);
        buttons.add(inputSlider.textFieldWidget);
        return inputSlider;
    }

    private static InputSlider createPixelSlider(int textWidth, int sliderWidth, int widgetHeight, int elementSpacing, String key, float startingValue) {
        int x = screenWidth - PADDING - textWidth - sliderWidth - elementSpacing + 1;
        int y = 74;
        InputSlider inputSlider = new InputSlider(x, y, textWidth, sliderWidth, BUTTON_HEIGHT, elementSpacing, 0, 64, 16, startingValue, 0, 1024f, Text.translatable(key));
        buttons.add(inputSlider.sliderWidget);
        buttons.add(inputSlider.textFieldWidget);
        return inputSlider;
    }

    private static void toggleAspectLock(ButtonWidget button) {
        setAspectLock(!aspectLocked);
        button.setMessage(getAspectLockIcon(aspectLocked));
    }

    private static void setAspectLock(boolean to) {
        aspectLocked = to;
        if (aspectLocked) {
            aspectRatio = inputSliders[0].getValue() / inputSliders[1].getValue();
        }
    }

    private static void resetSize(ButtonWidget button) {
        SignSideInfo info = SignedPaintingsClient.currentSignEdit.getSideInfo(front);
        info.resetSize();
        inputSliders[0].setValue(info.paintingInfo.getWidth());
        inputSliders[1].setValue(info.paintingInfo.getHeight());
        aspectRatio = inputSliders[0].getValue() / inputSliders[1].getValue();
    }

    private static Text getAspectLockIcon(boolean aspectLocked) {
        return Text.translatable(SignedPaintingsClient.MODID + ".aspect." + (aspectLocked ? "locked" : "unlocked"));
    }

    private static Text getBackTypeText(BackType.Type backType) {
        return Text.translatable(SignedPaintingsClient.MODID + ".back_mode." + (backType.toString().toLowerCase(Locale.ROOT)));
    }

    private static Text getBackgroundText(boolean isEnabled) {
        return Text.translatable(SignedPaintingsClient.MODID + ".background." + (isEnabled ? "y" : "n"));
    }

    private static void cyclePaintingBack(ButtonWidget button) {
        BackType.Type newType = SignedPaintingsClient.currentSignEdit.getSideInfo(front).cyclePaintingBack();
        button.setMessage(getBackTypeText(newType));
    }

    private static void cycleBackground(ButtonWidget button) {
        isBackgroundEnabled = !isBackgroundEnabled;
        button.setMessage(getBackgroundText(isBackgroundEnabled));
    }

    private static void onSizeSliderChanged(float value, boolean isWidth) {
        if (aspectLocked) {
            if (isWidth) value /= aspectRatio;
            else value *= aspectRatio;

            value = SignedPaintingsClient.roundFloatTo3DP(value);

            inputSliders[isWidth ? 1 : 0].setValue(value);
        }
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingSize(inputSliders[0].getValue(), inputSliders[1].getValue());
    }

    private static void onXOffsetSliderChanged(float value) {
        offsetVec.x = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingOffset(offsetVec);
    }

    private static void onYOffsetSliderChanged(float value) {
        offsetVec.y = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingOffset(offsetVec);
    }

    private static void onZOffsetSliderChanged(float value) {
        offsetVec.z = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingOffset(offsetVec);
    }

    private static void onXRotationSliderChanged(float value) {
        rotationVec.x = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updateRotatingVector(rotationVec);
    }

    private static void onYRotationSliderChanged(float value) {
        rotationVec.y = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updateRotatingVector(rotationVec);
    }

    private static void onZRotationSliderChanged(float value) {
        rotationVec.z = value;
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updateRotatingVector(rotationVec);
    }

    private static void onPixelSliderChanged(float value) {
        SignedPaintingsClient.currentSignEdit.getSideInfo(front).updatePaintingPixelsPerBlock(value);
    }

    private static void copyToClipboard(String string) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Clipboard clipboard = new Clipboard();
            clipboard.setClipboard(client.getWindow().getHandle(), string);
        }
    }

    public static ArrayList<ClickableWidget> getButtons() {
        return buttons;
    }

    public static InputSlider[] getInputSliders() {
        return inputSliders;
    }

    public static PaintingInfo getInfo() {
        return info;
    }

    public static boolean isBackgroundEnabled() {
        return isBackgroundEnabled;
    }

    public static void updateUI(SignSideInfo info) {
        // todo check if it's safe
        inputSliders[0].setValue(info.paintingInfo.getWidth());
        inputSliders[1].setValue(info.paintingInfo.getHeight());
        inputSliders[2].setValue(info.paintingInfo.getPixelsPerBlock());
        inputSliders[3].setValue(info.paintingInfo.offsetVec.x);
        inputSliders[4].setValue(info.paintingInfo.offsetVec.y);
        inputSliders[5].setValue(info.paintingInfo.offsetVec.z);
        inputSliders[6].setValue(info.paintingInfo.rotationVec.x);
        inputSliders[7].setValue(info.paintingInfo.rotationVec.y);
        inputSliders[8].setValue(info.paintingInfo.rotationVec.z);
        backModeButton.setMessage(getBackTypeText(info.paintingInfo.getBackType()));
    }

    public static void addButton(BackgroundClick backgroundClick) {
        buttons.add(backgroundClick);
    }
}
