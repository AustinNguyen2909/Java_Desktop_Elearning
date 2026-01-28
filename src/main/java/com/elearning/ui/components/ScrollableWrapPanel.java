package com.elearning.ui.components;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Scrollable panel that tracks viewport width to avoid horizontal scrolling.
 */
public class ScrollableWrapPanel extends JPanel implements Scrollable {
    public ScrollableWrapPanel(java.awt.LayoutManager layout) {
        super(layout);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
