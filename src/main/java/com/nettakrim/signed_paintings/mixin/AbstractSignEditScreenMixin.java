package com.nettakrim.signed_paintings.mixin;

import com.nettakrim.signed_paintings.SignedPaintingsClient;
import com.nettakrim.signed_paintings.access.AbstractSignEditScreenAccessor;
import com.nettakrim.signed_paintings.access.SignBlockEntityAccessor;
import com.nettakrim.signed_paintings.gui.BackgroundClick;
import com.nettakrim.signed_paintings.gui.InputSlider;
import com.nettakrim.signed_paintings.gui.SignEditingInfo;
import com.nettakrim.signed_paintings.gui.UIHelper;
import com.nettakrim.signed_paintings.rendering.PaintingInfo;
import com.nettakrim.signed_paintings.rendering.SignSideInfo;
import com.nettakrim.signed_paintings.util.ImageManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen implements AbstractSignEditScreenAccessor {

    @Shadow
    private SignText text;

    @Final
    @Shadow
    private String[] messages;

    @Final
    @Shadow
    private SignBlockEntity blockEntity;

    @Final
    @Shadow
    private boolean front;

    @Shadow
    private int currentRow;

    @Shadow
    private SelectionManager selectionManager;

    @Unique
    private String uploadURL = null;

    @Unique
    private ClickableWidget uploadButton;

    @Unique
    private ClickableWidget doneButton;

    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    protected abstract void setCurrentRowMessage(String message);

    @Shadow
    protected abstract void renderSignText(DrawContext context);

    @Shadow
    protected abstract void renderSignBackground(DrawContext var1, BlockState var2);

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/DiffuseLighting;disableGuiDepthLighting()V"), cancellable = true)
    private void modifyRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        PaintingInfo info = getInfo();
        if (info != null && info.isReady()) {
            renderUI(context);
            super.render(context, mouseX, mouseY, delta);
            ci.cancel();
        }
    }

    @Unique
    private PaintingInfo getInfo() {
        SignBlockEntityAccessor sign = (SignBlockEntityAccessor) blockEntity;
        return front ? sign.signedPaintings$getFrontPaintingInfo() : sign.signedPaintings$getBackPaintingInfo();
    }

    @Override
    public void renderBackground(DrawContext context) {
        PaintingInfo info = getInfo();
        if (info == null || !info.isReady() || UIHelper.isBackgroundEnabled()) {
            super.renderBackground(context);
        }
    }

    @Unique
    private void renderUI(DrawContext context) {
        // This could be improved in future with smaller Mixin
        DiffuseLighting.disableGuiDepthLighting();
        this.renderBackground(context);
        BlockState blockState = this.blockEntity.getCachedState();
        context.getMatrices().push();
        if (this.getClass().equals(SignEditScreen.class)) {
            boolean bl = blockState.getBlock() instanceof SignBlock;
            if (bl) {
                context.getMatrices().translate(0.0f, -16.0f, -50.0f);
            } else {
                context.getMatrices().translate(0.0f, -4.0f, -50.0f);
            }
        }
        context.getMatrices().translate(90.0f, 38.0f, 50.0f);
        context.getMatrices().scale(0.5f, 0.5f, 0.5f);
        context.getMatrices().push();
        this.renderSignBackground(context, blockState);
        context.getMatrices().pop();
        this.renderSignText(context);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    @Redirect(method = "renderSignText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/AbstractSignEditScreen;getTextScale()Lorg/joml/Vector3f;"))
    private Vector3f modifyGetTextScale(AbstractSignEditScreen instance) {
        return new Vector3f(1.0f, 1.0f, 1.0f); // For some reason Signs had ugly 0.96
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        doneButton = (ClickableWidget)this.children().get(0);

        UIHelper.init(front, this, (SignBlockEntityAccessor) blockEntity);
        ArrayList<ClickableWidget> uiButtons = UIHelper.getButtons();
        for (ClickableWidget widget : uiButtons) {
            addDrawableChild(widget);
            addSelectableChild(widget);
        }

        uploadButton = ButtonWidget.builder(Text.translatable(SignedPaintingsClient.MODID + ".upload_prompt"), this::upload).dimensions(this.width / 2 - 100, (this.height / 4 + 144) - 25, 200, 20).build();
        addDrawableChild(uploadButton);
        addSelectableChild(uploadButton);
        if (uploadURL == null) uploadButton.visible = false;

        BackgroundClick backgroundClick = new BackgroundClick(UIHelper.getInputSliders());
        addSelectableChild(backgroundClick);
        UIHelper.addButton(backgroundClick);

        SignedPaintingsClient.currentSignEdit.setSelectionManager(selectionManager);

        PaintingInfo info = UIHelper.getInfo();
        signedPaintings$setVisibility(info != null && info.isReady());
    }


    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/block/entity/SignBlockEntity;ZZLnet/minecraft/text/Text;)V")
    private void onScreenOpen(SignBlockEntity blockEntity, boolean front, boolean filtered, Text title, CallbackInfo ci) {
        SignedPaintingsClient.currentSignEdit = new SignEditingInfo(blockEntity, this);
    }

    @Inject(at = @At("TAIL"), method = "finishEditing")
    private void onScreenClose(CallbackInfo ci) {
        SignedPaintingsClient.currentSignEdit = null;
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void onKeyPress(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        for (InputSlider slider : UIHelper.getInputSliders()) {
            if (slider != null && slider.isFocused() && slider.keyPressed(keyCode, scanCode, modifiers)) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "charTyped", cancellable = true)
    private void onCharType(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        for (InputSlider slider : UIHelper.getInputSliders()) {
            if (slider != null && slider.isFocused() && slider.charTyped(chr, modifiers)) {
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
    }

    @ModifyVariable(at = @At("STORE"), method = "renderSignText", ordinal = 0)
    private boolean stopTextCaret(boolean bl) {
        for (InputSlider slider : UIHelper.getInputSliders()) {
            if (slider != null && slider.isFocused() && selectionManager != null) {
                selectionManager.setSelectionEnd(selectionManager.getSelectionStart());
                return false;
            }
        }
        return bl;
    }

    @Override
    public void signedPaintings$clear(boolean setText) {
        for (int i = 0; i < messages.length; i++) {
            this.messages[i] = "";
            this.text = this.text.withMessage(i, Text.literal(""));
        }
        if (setText) {
            this.blockEntity.setText(this.text, this.front);
        }
        this.currentRow = 0;
    }

    @Override
    public int signedPaintings$paste(String pasteString, int selectionStart, int selectionEnd, boolean setText) {
        int maxWidthPerLine = this.blockEntity.getMaxTextWidth();
        TextRenderer textRenderer = SignedPaintingsClient.client.textRenderer;

        if (ImageManager.isValid(pasteString)) {
            String url = SignedPaintingsClient.imageManager.applyURLInferences(pasteString);
            if (SignedPaintingsClient.imageManager.DomainBlocked(url) || (textRenderer.getWidth(SignedPaintingsClient.imageManager.getShortestURLInference(url)) > maxWidthPerLine * 2.5)) {
                uploadURL = url;
                uploadButton.visible = true;
            } else {
                pasteString = url;
            }
        }

        String[] newMessages = new String[messages.length];
        System.arraycopy(messages, 0, newMessages, 0, messages.length);

        selectionStart = MathHelper.clamp(selectionStart, 0, newMessages[currentRow].length());
        selectionEnd = MathHelper.clamp(selectionEnd, 0, newMessages[currentRow].length());
        if (selectionStart > selectionEnd) {
            int temp = selectionEnd;
            selectionEnd = selectionStart;
            selectionStart = temp;
        }

        newMessages[currentRow] = newMessages[currentRow].substring(0, selectionStart) + pasteString + newMessages[currentRow].substring(selectionEnd);
        int currentWidth = textRenderer.getWidth(newMessages[currentRow]);
        int cursor = selectionStart + pasteString.length();

        if (currentWidth < maxWidthPerLine) {
            setCurrentRowMessage(newMessages[currentRow]);
            return cursor;
        }

        int cursorRow = currentRow;

        while (true) {
            String line = newMessages[currentRow];
            int index = SignedPaintingsClient.getMaxFittingIndex(line, maxWidthPerLine, textRenderer);
            newMessages[currentRow] = line.substring(0, index);
            if (currentRow == messages.length - 1 || line.length() <= index) {
                break;
            }
            if (currentRow == cursorRow && cursor > index) {
                cursorRow++;
                cursor -= index;
            }
            currentRow++;
            newMessages[currentRow] = line.substring(index) + newMessages[currentRow];
        }
        cursor = MathHelper.clamp(cursor, 0, newMessages[cursorRow].length());

        for (int i = 0; i < messages.length; i++) {
            this.messages[i] = newMessages[i];
            this.text = this.text.withMessage(i, Text.literal(this.messages[i]));
        }

        if (setText) {
            this.blockEntity.setText(this.text, this.front);
        }

        currentRow = cursorRow;
        return cursor;
    }

    @Unique
    private void upload(ButtonWidget button) {
        if (uploadURL == null) return;
        SignedPaintingsClient.uploadManager.uploadUrlToImgur(uploadURL, this::uploadFinished);
    }

    @Unique
    private void uploadFinished(String link) {
        if (!SignedPaintingsClient.currentSignEdit.sign.equals(blockEntity)) {
            return;
        }
        uploadURL = null;
        if (link == null) {
            uploadButton.setMessage(Text.translatable(SignedPaintingsClient.MODID + ".upload_fail"));
            return;
        }
        uploadButton.visible = false;
        signedPaintings$clear(false);
        signedPaintings$paste(link, 0, 0, false);
        ((SignBlockEntityAccessor) this.blockEntity).signedPaintings$getSideInfo(this.front).loadPainting(this.front, this.blockEntity, true);
    }

    @Override
    public void signedPaintings$setVisibility(boolean to) {
        for (ClickableWidget clickableWidget : UIHelper.getButtons()) {
            clickableWidget.visible = to;
        }
        doneButton.visible = !to;
    }

    @Override
    public void signedPaintings$initSliders(SignSideInfo info) {
        UIHelper.updateUI(info);
    }

    @Override
    public String signedPaintings$getText() {
        StringBuilder s = new StringBuilder();
        for (String message : messages) {
            s.append(message);
        }
        return s.toString();
    }
}
