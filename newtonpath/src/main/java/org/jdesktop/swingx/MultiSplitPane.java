/*
 * $Id: MultiSplitPane.java,v 1.4 2008-10-17 22:13:55 uqbar Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Node;

/**
 * 
 * <p>
 * All properties in this class are bound: when a properties value is changed,
 * all PropertyChangeListeners are fired.
 * 
 * @author Hans Muller
 */
public class MultiSplitPane extends JPanel {
	private static final long serialVersionUID = -2045975295392162609L;
	private AccessibleContext accessibleContext = null;
	private boolean continuousLayout = true;
	private DividerPainter dividerPainter = new DefaultDividerPainter();

	/**
	 * Creates a MultiSplitPane with it's LayoutManager set to to an empty
	 * MultiSplitLayout.
	 */
	public MultiSplitPane() {
		super(new MultiSplitLayout());
		InputHandler inputHandler = new InputHandler();
		addMouseListener(inputHandler);
		addMouseMotionListener(inputHandler);
		addKeyListener(inputHandler);
		setFocusable(true);
	}

	/**
	 * A convenience method that returns the layout manager cast to
	 * MutliSplitLayout.
	 * 
	 * @return this MultiSplitPane's layout manager
	 * @see java.awt.Container#getLayout
	 * @see #setModel
	 */
	public final MultiSplitLayout getMultiSplitLayout() {
		return (MultiSplitLayout) getLayout();
	}

	/**
	 * A convenience method that sets the MultiSplitLayout model. Equivalent to
	 * <code>getMultiSplitLayout.setModel(model)</code>
	 * 
	 * @param model
	 *            the root of the MultiSplitLayout model
	 * @see #getMultiSplitLayout
	 * @see MultiSplitLayout#setModel
	 */
	public final void setModel(Node model) {
		getMultiSplitLayout().setModel(model);
	}

	/**
	 * A convenience method that sets the MultiSplitLayout dividerSize property.
	 * Equivalent to
	 * <code>getMultiSplitLayout().setDividerSize(newDividerSize)</code>.
	 * 
	 * @param dividerSize
	 *            the value of the dividerSize property
	 * @see #getMultiSplitLayout
	 * @see MultiSplitLayout#setDividerSize
	 */
	public final void setDividerSize(int dividerSize) {
		getMultiSplitLayout().setDividerSize(dividerSize);
	}

	/**
	 * Sets the value of the <code>continuousLayout</code> property. If true,
	 * then the layout is revalidated continuously while a divider is being
	 * moved. The default value of this property is true.
	 * 
	 * @param continuousLayout
	 *            value of the continuousLayout property
	 * @see #isContinuousLayout
	 */
	public void setContinuousLayout(boolean continuousLayout) {
		boolean oldContinuousLayout = this.continuousLayout;
		this.continuousLayout = continuousLayout;
		firePropertyChange("continuousLayout", oldContinuousLayout,
				continuousLayout);
	}

	/**
	 * Returns true if dragging a divider only updates the layout when the drag
	 * gesture ends (typically, when the mouse button is released).
	 * 
	 * @return the value of the <code>continuousLayout</code> property
	 * @see #setContinuousLayout
	 */
	public boolean isContinuousLayout() {
		return this.continuousLayout;
	}

	/**
	 * Returns the Divider that's currently being moved, typically because the
	 * user is dragging it, or null.
	 * 
	 * @return the Divider that's being moved or null.
	 */
	public Divider activeDivider() {
		return this.dragDivider;
	}

	/**
	 * Draws a single Divider. Typically used to specialize the way the active
	 * Divider is painted.
	 * 
	 * @see #getDividerPainter
	 * @see #setDividerPainter
	 */
	public static abstract class DividerPainter {
		/**
		 * Paint a single Divider.
		 * 
		 * @param g
		 *            the Graphics object to paint with
		 * @param divider
		 *            the Divider to paint
		 */
		public abstract void paint(Graphics g, Divider divider);
	}

	protected class DefaultDividerPainter extends DividerPainter {
		@Override
		public void paint(Graphics g, Divider divider) {
			if ((divider == activeDivider()) && !isContinuousLayout()) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setColor(Color.black);
				g2d.fill(divider.getBounds());
			}
		}
	}

	/**
	 * The DividerPainter that's used to paint Dividers on this MultiSplitPane.
	 * This property may be null.
	 * 
	 * @return the value of the dividerPainter Property
	 * @see #setDividerPainter
	 */
	public DividerPainter getDividerPainter() {
		return this.dividerPainter;
	}

	/**
	 * Sets the DividerPainter that's used to paint Dividers on this
	 * MultiSplitPane. The default DividerPainter only draws the activeDivider
	 * (if there is one) and then, only if continuousLayout is false. The value
	 * of this property is used by the paintChildren method: Dividers are
	 * painted after the MultiSplitPane's children have been rendered so that
	 * the activeDivider can appear "on top of" the children.
	 * 
	 * @param dividerPainter
	 *            the value of the dividerPainter property, can be null
	 * @see #paintChildren
	 * @see #activeDivider
	 */
	public void setDividerPainter(DividerPainter dividerPainter) {
		this.dividerPainter = dividerPainter;
	}

	/**
	 * Uses the DividerPainter (if any) to paint each Divider that overlaps the
	 * clip Rectangle. This is done after the call to
	 * <code>super.paintChildren()</code> so that Dividers can be rendered
	 * "on top of" the children.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	protected void paintChildren(Graphics g) {
		super.paintChildren(g);
		DividerPainter dp = getDividerPainter();
		Rectangle clipR = g.getClipBounds();
		if ((dp != null) && (clipR != null)) {
			Graphics dpg = g.create();
			try {
				MultiSplitLayout msl = getMultiSplitLayout();
				for (Divider divider : msl.dividersThatOverlap(clipR)) {
					dp.paint(dpg, divider);
				}
			} finally {
				dpg.dispose();
			}
		}
	}

	private boolean dragUnderway = false;
	private MultiSplitLayout.Divider dragDivider = null;
	private Rectangle initialDividerBounds = null;
	private boolean oldFloatingDividers = true;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private int dragMin = -1;
	private int dragMax = -1;

	protected void startDrag(int mx, int my) {
		requestFocusInWindow();
		MultiSplitLayout msl = getMultiSplitLayout();
		MultiSplitLayout.Divider divider = msl.dividerAt(mx, my);
		if (divider != null) {
			MultiSplitLayout.Node prevNode = divider.previousSibling();
			MultiSplitLayout.Node nextNode = divider.nextSibling();
			if ((prevNode == null) || (nextNode == null)) {
				this.dragUnderway = false;
			} else {
				this.initialDividerBounds = divider.getBounds();
				this.dragOffsetX = mx - this.initialDividerBounds.x;
				this.dragOffsetY = my - this.initialDividerBounds.y;
				this.dragDivider = divider;

				if (this.dragDivider.isVertical()) {

					this.dragMin = getMultiSplitLayout().getMinRightLimitNode(
							prevNode);
					this.dragMax = getMultiSplitLayout().getMaxLeftLimitNode(
							nextNode);
					this.dragMax -= this.dragDivider.getBounds().width;
				} else {
					this.dragMin = getMultiSplitLayout().getMinBottomLimitNode(
							prevNode);
					this.dragMax = getMultiSplitLayout().getMaxTopLimitNode(
							nextNode);
					this.dragMax -= this.dragDivider.getBounds().height;
				}
				this.oldFloatingDividers = getMultiSplitLayout()
						.getFloatingDividers();
				getMultiSplitLayout().setFloatingDividers(false);
				this.dragUnderway = true;
			}
		} else {
			this.dragUnderway = false;
		}
	}

	private void repaintDragLimits() {
		Rectangle damageR = this.dragDivider.getBounds();
		if (this.dragDivider.isVertical()) {
			damageR.x = this.dragMin;
			damageR.width = this.dragMax - this.dragMin;
		} else {
			damageR.y = this.dragMin;
			damageR.height = this.dragMax - this.dragMin;
		}
		repaint(damageR);
	}

	protected void updateDrag(int mx, int my) {
		if (!this.dragUnderway) {
			return;
		}
		Rectangle oldBounds = this.dragDivider.getBounds();
		Rectangle bounds = new Rectangle(oldBounds);
		if (this.dragDivider.isVertical()) {
			bounds.x = mx - this.dragOffsetX;
			bounds.x = Math.max(bounds.x, this.dragMin);
			bounds.x = Math.min(bounds.x, this.dragMax);
		} else {
			bounds.y = my - this.dragOffsetY;
			bounds.y = Math.max(bounds.y, this.dragMin);
			bounds.y = Math.min(bounds.y, this.dragMax);
		}
		this.dragDivider.setBounds(bounds);
		if (isContinuousLayout()) {
			revalidate();
			repaintDragLimits();
		} else {
			repaint(oldBounds.union(bounds));
		}
	}

	private void clearDragState() {
		this.dragDivider = null;
		this.initialDividerBounds = null;
		this.oldFloatingDividers = true;
		this.dragOffsetX = this.dragOffsetY = 0;
		this.dragMin = this.dragMax = -1;
		this.dragUnderway = false;
	}

	protected void finishDrag(int x, int y) {
		if (this.dragUnderway) {
			clearDragState();
			if (!isContinuousLayout()) {
				revalidate();
				repaint();
			}
		}
	}

	protected void cancelDrag() {
		if (this.dragUnderway) {
			this.dragDivider.setBounds(this.initialDividerBounds);
			getMultiSplitLayout().setFloatingDividers(this.oldFloatingDividers);
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			repaint();
			revalidate();
			clearDragState();
		}
	}

	protected void updateCursor(int x, int y, boolean show) {
		if (this.dragUnderway) {
			return;
		}
		int cursorID = Cursor.DEFAULT_CURSOR;
		if (show) {
			MultiSplitLayout.Divider divider = getMultiSplitLayout().dividerAt(
					x, y);
			if (divider != null) {
				cursorID = (divider.isVertical()) ? Cursor.E_RESIZE_CURSOR
						: Cursor.N_RESIZE_CURSOR;
			}
		}
		setCursor(Cursor.getPredefinedCursor(cursorID));
	}

	protected class InputHandler extends MouseInputAdapter implements
			KeyListener {

		@Override
		public void mouseEntered(MouseEvent e) {
			updateCursor(e.getX(), e.getY(), true);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			updateCursor(e.getX(), e.getY(), true);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			updateCursor(e.getX(), e.getY(), false);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			startDrag(e.getX(), e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			finishDrag(e.getX(), e.getY());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			updateDrag(e.getX(), e.getY());
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				cancelDrag();
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

	@Override
	public AccessibleContext getAccessibleContext() {
		if (this.accessibleContext == null) {
			this.accessibleContext = new AccessibleMultiSplitPane();
		}
		return this.accessibleContext;
	}

	protected class AccessibleMultiSplitPane extends AccessibleJPanel {
		private static final long serialVersionUID = 4177114112369591280L;

		@Override
		public AccessibleRole getAccessibleRole() {
			return AccessibleRole.SPLIT_PANE;
		}
	}
}
