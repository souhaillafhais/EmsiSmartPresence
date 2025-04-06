package com.example.emsismartpresence;


import android.graphics.Rect;
import android.view.View;
import android.widget.ScrollView;

public class KeyboardUtils {
    public static void setupKeyboardAutoAdjust(View rootView) {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Keyboard is opened
                ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.scroll_view);
                if (scrollView != null) {
                    scrollView.smoothScrollTo(0, scrollView.getBottom());
                }
            }
        });
    }
}
