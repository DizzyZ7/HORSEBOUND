// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

record MenuCommand(
    boolean upPressed,
    boolean downPressed,
    boolean leftPressed,
    boolean rightPressed,
    boolean confirmPressed,
    boolean backPressed
) {
    static MenuCommand idle() {
        return new MenuCommand(false, false, false, false, false, false);
    }

    boolean hasActivity() {
        return upPressed || downPressed || leftPressed || rightPressed || confirmPressed || backPressed;
    }

    MenuCommand merge(MenuCommand other) {
        if (other == null) return this;
        return new MenuCommand(
            upPressed || other.upPressed,
            downPressed || other.downPressed,
            leftPressed || other.leftPressed,
            rightPressed || other.rightPressed,
            confirmPressed || other.confirmPressed,
            backPressed || other.backPressed
        );
    }
}
