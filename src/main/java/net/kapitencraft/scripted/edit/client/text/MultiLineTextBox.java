package net.kapitencraft.scripted.edit.client.text;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class MultiLineTextBox extends AbstractWidget {
    private String value = "";
    private final Font font;
    private Vector2i cursorPos;
    private int highlightPos;
    private int frame;
    private boolean shiftPressed;

    private BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY);

    public MultiLineTextBox(int pX, int pY, int pWidth, int pHeight, Font font) {
        super(pX, pY, pWidth, pHeight, Component.empty());
        this.font = font;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public String line(int lineIndex) {
        return value.split("\n")[lineIndex];
    }

    public String getValue() {
        return value;
    }

    public void insertText(String pTextToWrite) {

        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = Integer.MAX_VALUE - this.value.length() - (i - j);
        String s = SharedConstants.filterText(pTextToWrite);
        int l = s.length();
        if (k < l) {
            s = s.substring(0, k);
            l = k;
        }

        String s1 = (new StringBuilder(this.value)).replace(i, j, s).toString();
        this.value = s1;
        this.setCursorPosition(i + l);
        this.setHighlightPos(this.cursorPos);
    }

    private void deleteText(int pCount) {
        if (Screen.hasControlDown()) {
            this.deleteWords(pCount);
        } else {
            this.deleteChars(pCount);
        }

    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(pNum) - this.cursorPos);
            }
        }
    }

    public void tick() {
        ++this.frame;
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteChars(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(pNum);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    this.value = (new StringBuilder(this.value)).delete(j, k).toString();
                    this.moveCursorTo(j);
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getWordPosition(int pNumWords) {
        return this.getWordPosition(pNumWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private int getWordPosition(int pN, int pPos) {
        return this.getWordPosition(pN, pPos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private int getWordPosition(int pN, int pPos, boolean pSkipWs) {
        int i = pPos;
        boolean flag = pN < 0;
        int j = Math.abs(pN);

        for(int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(pSkipWs && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(pSkipWs && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursor(int pDelta) {
        this.moveCursorTo(this.getCursorPos(pDelta));
    }

    private int getCursorPos(int pDelta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, pDelta);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void moveCursorTo(int pPos) {
        this.setCursorPosition(pPos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }
    }

    public void setCursorPosition(int pPos) {
        this.cursorPos = Mth.clamp(pPos, 0, this.value.length());
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    /**
     * Called when a keyboard key is pressed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     * @param pKeyCode the key code of the pressed key.
     * @param pScanCode the scan code of the pressed key.
     * @param pModifiers the keyboard modifiers.
     */
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.canNotConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(pKeyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(pKeyCode)) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());

                return true;
            } else if (Screen.isCut(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                this.insertText("");

                return true;
            } else {
                switch (pKeyCode) {
                    case 259:
                        this.shiftPressed = false;
                        this.deleteText(-1);
                        this.shiftPressed = Screen.hasShiftDown();
                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        this.shiftPressed = false;
                        this.deleteText(1);
                        this.shiftPressed = Screen.hasShiftDown();
                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public boolean canNotConsumeInput() {
        return !this.isVisible() || !this.isFocused();
    }

    /**
     * Called when a character is typed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     * @param pCodePoint the code point of the typed character.
     * @param pModifiers the keyboard modifiers.
     */
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (this.canNotConsumeInput()) {
            return false;
        }
        this.insertText(Character.toString(pCodePoint));
        return true;
    }

    public void onClick(double pMouseX, double pMouseY) {
        int i = Mth.floor(pMouseX) - this.getX();

        String s = this.font.plainSubstrByWidth(this.value, this.width);
        this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length());
    }

    public void playDownSound(SoundManager pHandler) {
    }

    public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isVisible()) {
            int j = this.cursorPos;
            int k = this.highlightPos;
            for (String s : this.value.split("\n")) {
                boolean flag = j >= 0 && j <= s.length();
                boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
                int i1 = this.getY();
                int j1 = this.getX();
                if (k > s.length()) {
                    k = s.length();
                }

                if (!s.isEmpty()) {
                    String s1 = flag ? s.substring(0, j) : s;
                    j1 = pGuiGraphics.drawString(this.font, this.formatter.apply(s1, 0), this.getX(), i1, 0);
                }

                boolean flag2 = this.cursorPos < this.value.length();
                int k1 = j1;
                if (!flag) {
                    k1 = j > 0 ? getX() + this.width : getX();
                } else if (flag2) {
                    k1 = j1 - 1;
                    --j1;
                }

                if (!s.isEmpty() && flag && j < s.length()) {
                    pGuiGraphics.drawString(this.font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, 0);
                }

                if (!flag2 && this.suggestion != null) {
                    pGuiGraphics.drawString(this.font, this.suggestion, k1 - 1, i1, -8355712);
                }

                if (flag1) {
                    if (flag2) {
                        pGuiGraphics.fill(RenderType.guiOverlay(), k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                    } else {
                        pGuiGraphics.drawString(this.font, "_", k1, i1, 0);
                    }
                }

                if (k != j) {
                    int l1 = this.getX() + this.font.width(s.substring(0, k));
                    this.renderHighlight(pGuiGraphics, k1, i1 - 1, l1 - 1, i1 + 1 + 9);
                }
            }
        }
    }

    private void renderHighlight(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY) {
        if (pMinX < pMaxX) {
            int i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY) {
            int j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        if (pMaxX > this.getX() + this.width) {
            pMaxX = this.getX() + this.width;
        }

        if (pMinX > this.getX() + this.width) {
            pMinX = this.getX() + this.width;
        }

        pGuiGraphics.fill(RenderType.guiTextHighlight(), pMinX, pMinY, pMaxX, pMaxY, -16776961);
    }

    /**
     * Returns the current position of the cursor.
     */
    public Vector2i getCursorPosition() {
        return this.cursorPos;
    }

    /**
     * Checks if the given mouse coordinates are over the GUI element.
     * <p>
     * @return {@code true} if the mouse is over the GUI element, {@code false} otherwise.
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     */
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.visible && pMouseX >= (double)this.getX() && pMouseX < (double)(this.getX() + this.width) && pMouseY >= (double)this.getY() && pMouseY < (double)(this.getY() + this.height);
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setHighlightPos(int pPosition) {
        int i = this.value.length();
        this.highlightPos = Mth.clamp(pPosition, 0, i);
        if (this.font != null) {

            int j = this.width;
            String s = this.font.plainSubstrByWidth(this.value, j);
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean pIsVisible) {
        this.visible = pIsVisible;
    }

    public void setSuggestion(@Nullable String pSuggestion) {
        this.suggestion = pSuggestion;
    }

    public int getScreenX(int pCharNum) {
        return pCharNum > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, pCharNum));
    }
}
