// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

final class HomesteadRuntimeInput {
    private final ControllerStateSource controller = new GdxControllerStateSource();
    private ControllerFrame previous = ControllerFrame.disconnected();

    InputResult sample(boolean buildMode) {
        int directSlot = directSlot();
        int hotbarDelta = 0;
        int buildTypeDelta = 0;
        int rotationDelta = 0;

        ControllerFrame current = controller.poll();
        if (current.connected()) {
            if (buildMode) {
                if (justPressed(current.dpadUp(), previous.dpadUp())) buildTypeDelta--;
                if (justPressed(current.dpadDown(), previous.dpadDown())) buildTypeDelta++;
                if (justPressed(current.dpadLeft(), previous.dpadLeft())) rotationDelta--;
                if (justPressed(current.dpadRight(), previous.dpadRight())) rotationDelta++;
            } else {
                if (justPressed(current.dpadLeft(), previous.dpadLeft())) hotbarDelta--;
                if (justPressed(current.dpadRight(), previous.dpadRight())) hotbarDelta++;
            }
        }
        previous = current;

        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) buildTypeDelta--;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) buildTypeDelta++;
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) rotationDelta++;

        return new InputResult(directSlot, hotbarDelta, buildTypeDelta, rotationDelta);
    }

    private static int directSlot() {
        int[] top = {
            Input.Keys.NUM_1,
            Input.Keys.NUM_2,
            Input.Keys.NUM_3,
            Input.Keys.NUM_4,
            Input.Keys.NUM_5,
            Input.Keys.NUM_6,
            Input.Keys.NUM_7,
            Input.Keys.NUM_8
        };
        int[] numpad = {
            Input.Keys.NUMPAD_1,
            Input.Keys.NUMPAD_2,
            Input.Keys.NUMPAD_3,
            Input.Keys.NUMPAD_4,
            Input.Keys.NUMPAD_5,
            Input.Keys.NUMPAD_6,
            Input.Keys.NUMPAD_7,
            Input.Keys.NUMPAD_8
        };
        for (int i = 0; i < Hotbar.SLOT_COUNT; i++) {
            if (Gdx.input.isKeyJustPressed(top[i]) || Gdx.input.isKeyJustPressed(numpad[i])) return i;
        }
        return -1;
    }

    private static boolean justPressed(boolean current, boolean old) {
        return current && !old;
    }

    record InputResult(int directSlot, int hotbarDelta, int buildTypeDelta, int rotationDelta) {
    }
}
