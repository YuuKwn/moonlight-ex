package com.limelight.streaming;

import com.limelight.LimeLog;
import com.limelight.binding.input.KeyboardTranslator;
import com.limelight.binding.input.evdev.EvdevListener;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.KeyboardPacket;
import com.limelight.nvstream.input.MouseButtonPacket;

public class HostInputMapper {
    public interface SpecialKeyHandler {
        boolean handleSpecialKey(short androidKeyCode, boolean buttonDown);
    }

    private final NvConnection connection;
    private final KeyboardTranslator keyboardTranslator;
    private final SpecialKeyHandler specialKeyHandler;
    private final ModifierStateProvider modifierStateProvider;

    public interface ModifierStateProvider {
        byte getModifierState();
    }

    public HostInputMapper(NvConnection connection,
                           KeyboardTranslator keyboardTranslator,
                           SpecialKeyHandler specialKeyHandler,
                           ModifierStateProvider modifierStateProvider) {
        this.connection = connection;
        this.keyboardTranslator = keyboardTranslator;
        this.specialKeyHandler = specialKeyHandler;
        this.modifierStateProvider = modifierStateProvider;
    }

    public void mouseMove(int deltaX, int deltaY) {
        connection.sendMouseMove((short) deltaX, (short) deltaY);
    }

    public void mouseButtonEvent(int buttonId, boolean down) {
        byte buttonIndex;

        switch (buttonId) {
            case EvdevListener.BUTTON_LEFT:
                buttonIndex = MouseButtonPacket.BUTTON_LEFT;
                break;
            case EvdevListener.BUTTON_MIDDLE:
                buttonIndex = MouseButtonPacket.BUTTON_MIDDLE;
                break;
            case EvdevListener.BUTTON_RIGHT:
                buttonIndex = MouseButtonPacket.BUTTON_RIGHT;
                break;
            case EvdevListener.BUTTON_X1:
                buttonIndex = MouseButtonPacket.BUTTON_X1;
                break;
            case EvdevListener.BUTTON_X2:
                buttonIndex = MouseButtonPacket.BUTTON_X2;
                break;
            default:
                LimeLog.warning("Unhandled button: " + buttonId);
                return;
        }

        if (down) {
            connection.sendMouseButtonDown(buttonIndex);
        } else {
            connection.sendMouseButtonUp(buttonIndex);
        }
    }

    public void mouseVScroll(byte amount) {
        connection.sendMouseScroll(amount);
    }

    public void mouseHScroll(byte amount) {
        connection.sendMouseHScroll(amount);
    }

    public void keyboardEvent(boolean buttonDown, short keyCode) {
        short keyMap = keyboardTranslator.translate(keyCode, 0, -1);
        if (keyMap == 0) {
            return;
        }

        if (specialKeyHandler.handleSpecialKey(keyCode, buttonDown)) {
            return;
        }

        connection.sendKeyboardInput(keyMap,
                buttonDown ? KeyboardPacket.KEY_DOWN : KeyboardPacket.KEY_UP,
                modifierStateProvider.getModifierState(),
                (byte) 0);
    }
}
