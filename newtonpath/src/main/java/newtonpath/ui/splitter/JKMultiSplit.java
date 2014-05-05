package newtonpath.ui.splitter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import newtonpath.images.Images;
import newtonpath.ui.ContentFactory;

import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout.Divider;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Node;

public class JKMultiSplit extends MultiSplitPane {

	private final MyGlassPane myGlassPane;
	protected JKMultiSplitGroup splitGroup = null;
	protected NodeSelectionAction clickAction = null;

	abstract public static class NodeSelectionAction {
		abstract public void doClick(JKMultiSplit _pane, Leaf _l, int _side);

		abstract public boolean isSideSelection(MultiSplitLayout _splitLayout,
				Leaf _overLeaf);

		abstract public void finish();
	}

	public static class SplitAction extends NodeSelectionAction {
		private Container newComponent = null;
		private ContentFactory contentBuilder = null;

		public void prepareContainer(Container _c) {
			this.contentBuilder = null;
			this.newComponent = _c;
		}

		public void prepareContentFactory(ContentFactory _cf) {
			this.contentBuilder = _cf;
			this.newComponent = null;
		}

		@Override
		public void finish() {
			this.newComponent = null;
			this.contentBuilder = null;
		}

		@Override
		public void doClick(JKMultiSplit _pane, Leaf _leaf, int _side) {
			if (this.contentBuilder != null) {
				this.newComponent = this.contentBuilder.getContent();
			}
			if (_leaf != null && this.newComponent != null) {
				_pane.splitLeaf(_leaf, this.newComponent, _side == 1
						|| _side == 2, _side == 2 || _side == 4);
			}
		}

		@Override
		public boolean isSideSelection(MultiSplitLayout _splitLayout,
				Leaf _overLeaf) {
			return _splitLayout.getComponentForNode(_overLeaf) != null;
		}
	}

	public static class RemoveAction extends NodeSelectionAction {
		@Override
		public void doClick(JKMultiSplit arg0, Leaf arg1, int arg2) {
			arg0.removeLeaf(arg1);
		}

		@Override
		public boolean isSideSelection(MultiSplitLayout _splitLayout,
				Leaf _overLeaf) {
			return false;
		}

		@Override
		public void finish() {
		}
	}

	public static class SwapAction extends NodeSelectionAction {
		private String firstLeafName = null;
		private JKMultiSplit firstSplitPane = null;

		public void prepare(JKMultiSplit _firstSplitPane, Leaf _firstLeaf) {
			this.firstLeafName = _firstLeaf.getName();
			this.firstSplitPane = _firstSplitPane;
		}

		@Override
		public void finish() {
			this.firstLeafName = null;
		}

		@Override
		public void doClick(JKMultiSplit _split, Leaf _leaf, int _side) {
			if (_leaf != null) {
				String name1 = this.firstLeafName;
				String name2 = _leaf.getName();
				if (_split == this.firstSplitPane) {
					_split.swapComponents(name1, name2);
				} else {
					swapComponents(this.firstSplitPane, name1, _split, name2);
				}
			}
		}

		@Override
		public boolean isSideSelection(MultiSplitLayout _splitLayout,
				Leaf _overLeaf) {
			return false;
		}
	}

	public JKMultiSplit(Component _c) {
		this.myGlassPane = new MyGlassPane();
		getMultiSplitLayout().setDividerSize(3);
		getMultiSplitLayout().setModel(new MultiSplitLayout.Leaf("0"));
		addPane(_c);
		getMultiSplitLayout().addLayoutComponent("0", _c);
	}

	public MyGlassPane getGlassPane() {
		return this.myGlassPane;
	}

	public class MyGlassPane extends JComponent {
		protected abstract class LeafListener implements MouseListener,
				MouseMotionListener {
			protected Leaf currentLeaf = null;

			protected abstract void overNode(Node currentNode,
					Point glassPanePoint);

			public abstract void clickEvent(MouseEvent e);

			public void mouseMoved(MouseEvent e) {
				moveEvent(e);
			}

			public void mouseDragged(MouseEvent e) {
				moveEvent(e);
			}

			public void mouseClicked(MouseEvent e) {
				clickEvent(e);
			}

			public void mouseEntered(MouseEvent e) {
				moveEvent(e);
			}

			public void mouseExited(MouseEvent e) {
				moveEvent(e);
			}

			public void mousePressed(MouseEvent e) {
				moveEvent(e);
			}

			public void mouseReleased(MouseEvent e) {
				moveEvent(e);
			}

			public void moveEvent(MouseEvent e) {
				Point glassPanePoint;
				this.currentLeaf = null;
				if (e.getComponent() != MyGlassPane.this) {
					glassPanePoint = SwingUtilities.convertPoint(e
							.getComponent(), e.getPoint(), MyGlassPane.this);
				} else {
					glassPanePoint = e.getPoint();
				}
				Node n = getMultiSplitLayout().getLeafAt(
						SwingUtilities.convertPoint(e.getComponent(), e
								.getPoint(), JKMultiSplit.this));
				if (n != null && (n instanceof Leaf)) {
					this.currentLeaf = (Leaf) n;
				}
				overNode(n, glassPanePoint);
			}
		}

		protected class SelectionLeafListener extends LeafListener {
			private int selectedSide = 0;

			@Override
			public void clickEvent(MouseEvent e) {
				if (JKMultiSplit.this.clickAction != null) {
					JKMultiSplit.this.clickAction.doClick(JKMultiSplit.this,
							this.currentLeaf, this.selectedSide);
				}
				this.currentLeaf = null;
				finishAction();
			}

			protected void finishAction() {
				if (JKMultiSplit.this.splitGroup != null) {
					JKMultiSplit.this.splitGroup.finishLeafSelection();
				} else {
					finishLeafSelection();
				}
				JKMultiSplit.this.clickAction.finish();
				JKMultiSplit.this.clickAction = null;
			}

			protected void cancelAction() {
				this.currentLeaf = null;
				finishAction();
			}

			@Override
			protected void overNode(Node currentNode, Point glassPanePoint) {
				if (this.currentLeaf != null) {
					MyGlassPane.this.overLeaf.setBounds(SwingUtilities
							.convertRectangle(JKMultiSplit.this,
									this.currentLeaf.getBounds(),
									MyGlassPane.this));
					MyGlassPane.this.overLeaf.setVisible(true);
					if (JKMultiSplit.this.clickAction != null
							&& JKMultiSplit.this.clickAction.isSideSelection(
									getMultiSplitLayout(), this.currentLeaf)) {
						overLeafSide(this.currentLeaf, glassPanePoint);
					} else {
						MyGlassPane.this.leafSide
								.setBounds(MyGlassPane.this.overLeaf
										.getBounds());
						MyGlassPane.this.leafSide.setVisible(true);
					}
				} else {
					MyGlassPane.this.overLeaf.setVisible(false);
					MyGlassPane.this.leafSide.setVisible(false);
				}
			}

			private void overLeafSide(Leaf currentLeaf, Point glassPanePoint) {
				showLeafSide(getLeafSide(currentLeaf, glassPanePoint));
			}

			private int getLeafSide(Leaf currentLeaf, Point glassPanePoint) {
				int currXside, currYside;
				Point containerPoint = SwingUtilities.convertPoint(
						MyGlassPane.this, glassPanePoint,
						MyGlassPane.this.overLeaf);
				float pX, pY, bX, bY;
				Dimension rv;
				rv = MyGlassPane.this.overLeaf.getSize();
				if (rv.height > 0 && rv.width > 0) {
					pX = 2f * containerPoint.x / rv.width - 1f;
					pY = 2f * containerPoint.y / rv.height - 1f;
					bX = Math.abs(pX);
					bY = Math.abs(pY);

					currXside = pX < 0 ? 1 : 2;
					currYside = pY < 0 ? 3 : 4;

					if (bX > 0.5f && bY > 0.5f) {
						if (this.selectedSide != currYside) {
							this.selectedSide = currXside;
						}
					} else if (bX > 0.5f) {
						this.selectedSide = currXside;
					} else if (bY > 0.5f) {
						this.selectedSide = currYside;
					} else {
						if (this.selectedSide != currYside
								&& this.selectedSide != currXside) {
							if (bX > bY) {
								this.selectedSide = currXside;
							} else {
								this.selectedSide = currYside;
							}
						}
					}
				}
				return this.selectedSide;
			}

			private void showLeafSide(int selectedSide) {
				boolean visible = true;
				Rectangle r = MyGlassPane.this.overLeaf.getBounds();
				switch (selectedSide) {
				case 1:
					r.width /= 2;
					break;
				case 2:
					r.width /= 2;
					r.x += r.width;
					break;
				case 3:
					r.height /= 2;
					break;
				case 4:
					r.height /= 2;
					r.y += r.height;
					break;
				default:
					visible = (false);
				}
				MyGlassPane.this.leafSide.setBounds(r);
				MyGlassPane.this.leafSide.setVisible(visible);
			}
		}

		protected abstract class SelectionDraggingLeafListener extends
				SelectionLeafListener implements KeyListener {
			private boolean dragging = false;
			private boolean dragActive = false;

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					this.dragging = false;
					this.dragActive = false;
					cancelAction();
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (this.dragging) {
					clickEvent(e);
				}
				this.dragging = false;
				this.dragActive = false;
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				this.dragActive = true;
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				if (this.dragActive) {
					if (startDragging()) {
						this.dragging = true;
					}
					this.dragActive = false;
				}
				if (this.dragging) {
					moveEvent(arg0);
				}
			}

			abstract public boolean startDragging();
		}

		protected class ControlBoxLeafListener extends LeafListener {
			@Override
			protected void overNode(Node currentNode, Point glassPanePoint) {
				showCurrentLeafCB();
				setUpCBListeners(currentNode);
			}

			private void showCurrentLeafCB() {
				if (this.currentLeaf != null
						&& JKMultiSplit.this.controlBoxVisible) {
					showControlBox(this.currentLeaf);
				} else {
					hideControlBox();
				}
			}

			public void reset(boolean _gotFocus) {
				if (_gotFocus
						&& getMultiSplitLayout().getModel() instanceof Leaf) {
					this.currentLeaf = (Leaf) getMultiSplitLayout().getModel();
				} else {
					this.currentLeaf = null;
				}
				showCurrentLeafCB();
				setUpCBListeners(this.currentLeaf);
			}

			@Override
			public void clickEvent(MouseEvent e) {
			}
		}

		private final Component nodeMaskW, nodeMaskE, nodeMaskN, nodeMaskS,
				diverNode;

		protected final JComponent overLeaf, leafSide;
		private final JPanel controlBox;

		private final SelectionLeafListener selectionLeafListener;
		protected final ControlBoxLeafListener controlBoxLeafListener;

		public void startLeafSelection(NodeSelectionAction _action) {
			JKMultiSplit.this.clickAction = _action;
			hideControlBoxMask();
			addMouseListener(this.selectionLeafListener);
			addMouseMotionListener(this.selectionLeafListener);
			setVisible(true);
		}

		public void finishLeafSelection() {
			removeMouseListener(this.selectionLeafListener);
			removeMouseMotionListener(this.selectionLeafListener);
			this.overLeaf.setVisible(false);
			this.leafSide.setVisible(false);
			resetControlBoxLeaf();
		}

		public MyGlassPane() {
			this.nodeMaskW = new Component() {
			};
			this.nodeMaskE = new Component() {
			};
			this.nodeMaskS = new Component() {
			};
			this.nodeMaskN = new Component() {
			};
			this.diverNode = new Component() {
			};

			this.overLeaf = new JPanel();
			this.leafSide = new JPanel();
			this.controlBox = new JPanel();
			this.overLeaf.setVisible(false);
			this.leafSide.setVisible(false);
			add(this.overLeaf);
			add(this.leafSide);

			this.leafSide.setBorder(BorderFactory.createLineBorder(new Color(
					0.1f, 0.1f, 0.1f, 0.5f), 2));
			this.leafSide.setBackground(new Color(0.5f, 0.5f, 0.5f, 0.4f));
			this.overLeaf.setBackground(new Color(0.5f, 0.5f, 0.5f, 0.2f));

			this.selectionLeafListener = new SelectionLeafListener();
			this.controlBoxLeafListener = new ControlBoxLeafListener();
			this.controlBox.setSize(new Dimension(80, 20));
			this.controlBox.setBackground(Color.RED);
			this.controlBox.setVisible(false);
			this.controlBox.setOpaque(false);

			this.controlBox.setLayout(null);
			// ////////////////////////////////////////
			JButton button;
			button = new JButton(Images.createImageIcon("window-close.png"));
			button.setMargin(new Insets(1, 1, 1, 1));
			button.setOpaque(false);
			button.setHorizontalAlignment(SwingConstants.CENTER);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (MyGlassPane.this.controlBoxLeafListener.currentLeaf != null) {
						removeLeaf(MyGlassPane.this.controlBoxLeafListener.currentLeaf);
					}
				}
			});
			button.setContentAreaFilled(false);
			button.setRolloverEnabled(false);
			// button.setFocusable(false);

			this.controlBox.add(button);
			button.setSize(new Dimension(18, 18));
			button.setLocation(this.controlBox.getWidth() - button.getWidth()
					- 2, 2);
			button.setBorder(null);
			// ////////////////////////////////////////
			button = new JButton(Images.createImageIcon("edit-copy.png"));
			button.setMargin(new Insets(1, 1, 1, 1));
			button.setOpaque(false);
			button.setHorizontalAlignment(SwingConstants.CENTER);
			SelectionDraggingLeafListener ll = new SelectionDraggingLeafListener() {
				@Override
				public boolean startDragging() {
					startSelectAndSwap(MyGlassPane.this.controlBoxLeafListener.currentLeaf);
					return true;
				}
			};
			button.addMouseListener(ll);
			button.addMouseMotionListener(ll);
			button.addKeyListener(ll);

			button.setContentAreaFilled(false);
			button.setRolloverEnabled(false);
			this.controlBox.add(button);
			button.setSize(new Dimension(18, 18));
			button.setLocation(this.controlBox.getWidth() - 2
					* button.getWidth() - 2 * 2, 2);
			button.setBorder(null);
			// ///////////////////////////////////////

			Label angle;
			angle = new Label();
			angle.setSize(new Dimension(3, 3));
			add(angle);
			angle.setLocation(0, 0);
			angle.setVisible(true);
			angle.setBackground(new Color(0xFF333333));

			this.nodeMaskW.addMouseListener(this.controlBoxLeafListener);
			this.nodeMaskE.addMouseListener(this.controlBoxLeafListener);
			this.nodeMaskN.addMouseListener(this.controlBoxLeafListener);
			this.nodeMaskS.addMouseListener(this.controlBoxLeafListener);

			this.nodeMaskW.addMouseMotionListener(this.controlBoxLeafListener);
			this.nodeMaskE.addMouseMotionListener(this.controlBoxLeafListener);
			this.nodeMaskN.addMouseMotionListener(this.controlBoxLeafListener);
			this.nodeMaskS.addMouseMotionListener(this.controlBoxLeafListener);

			add(this.nodeMaskW);
			add(this.nodeMaskN);
			add(this.nodeMaskS);
			add(this.nodeMaskE);
			add(this.diverNode);
			setContinuousLayout(false);
			addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					resetControlBoxLeaf(false);
				}
			});
		}

		private void setGSP(Component _gp, int left, int right, int top,
				int bottom) {
			if (right <= left || bottom <= top) {
				_gp.setVisible(false);
			} else {
				_gp.setLocation(left, top);
				_gp.setSize(right - left, bottom - top);
				_gp.setVisible(true);
			}
		}

		private void moveGSP(Rectangle _external, Rectangle _internal) {
			if (_internal != null) {
				setGSP(this.nodeMaskW, _external.x, _internal.x, _internal.y,
						_internal.y + _internal.height);
				setGSP(this.nodeMaskE, _internal.x + _internal.width,
						_external.x + _external.width, _internal.y, _internal.y
								+ _internal.height);
				setGSP(this.nodeMaskN, _external.x, _external.x
						+ _external.width, _external.y, _internal.y);
				setGSP(this.nodeMaskS, _external.x, _external.x
						+ _external.width, _internal.y + _internal.height,
						_external.y + _external.height);
			} else {
				this.nodeMaskE.setVisible(false);
				this.nodeMaskW.setVisible(false);
				this.nodeMaskS.setVisible(false);
				this.nodeMaskN.setBounds(_external);
				this.nodeMaskN.setVisible(true);
			}
		}

		protected void setUpCBListeners(Node _node) {
			if (_node != null) {
				Rectangle nodeRectangle = SwingUtilities.convertRectangle(
						JKMultiSplit.this, _node.getBounds(), MyGlassPane.this);
				moveGSP(SwingUtilities.convertRectangle(JKMultiSplit.this
						.getParent(), JKMultiSplit.this.getBounds(),
						MyGlassPane.this), nodeRectangle);
				if (_node instanceof Divider) {
					this.diverNode.setBounds(nodeRectangle);
					this.diverNode.setVisible(true);
					boolean vertical = ((Divider) _node).isVertical();
					int cursorType = vertical ? Cursor.W_RESIZE_CURSOR
							: Cursor.N_RESIZE_CURSOR;
					this.diverNode.setCursor(Cursor
							.getPredefinedCursor(cursorType));

				} else {
					this.diverNode.setVisible(false);
				}
			} else {
				moveGSP(SwingUtilities.convertRectangle(JKMultiSplit.this
						.getParent(), JKMultiSplit.this.getBounds(),
						MyGlassPane.this), null);
				this.diverNode.setVisible(false);
			}
		}

		public void showControlBox(Leaf _l) {
			Point p;
			if (this.controlBox.getParent() == null) {
				MyGlassPane.this.getRootPane().getLayeredPane().add(
						this.controlBox, JLayeredPane.POPUP_LAYER);
			}
			p = SwingUtilities.convertPoint(JKMultiSplit.this, _l.getBounds()
					.getLocation(), this.controlBox.getParent());
			p.translate(_l.getBounds().width - this.controlBox.getWidth(), 0);
			this.controlBox.setLocation(p);
			this.controlBox.setVisible(true);
			if (JKMultiSplit.this.splitGroup != null) {
				JKMultiSplit.this.splitGroup
						.resetControlBoxLeaf(JKMultiSplit.this);
			}
		}

		public void hideControlBox() {
			this.controlBox.setVisible(false);
		}

		public void lockControlBox() {
			hideControlBoxMask();
		}

		public void resetControlBoxLeaf() {
			this.controlBoxLeafListener.reset(false);
		}

		public void resetControlBoxLeaf(boolean _gotFocus) {
			this.controlBoxLeafListener.reset(_gotFocus);
		}

		public void hideControlBoxMask() {
			this.nodeMaskE.setVisible(false);
			this.nodeMaskW.setVisible(false);
			this.nodeMaskS.setVisible(false);
			this.nodeMaskN.setVisible(false);
		}
	}

	public void startLeafSelection(NodeSelectionAction _action) {
		this.myGlassPane.startLeafSelection(_action);
	}

	public void finishLeafSelection() {
		this.myGlassPane.finishLeafSelection();
	}

	public void splitLeaf(Leaf l, Component c, boolean _horiz, boolean _sw) {
		addPane(c);
		getMultiSplitLayout().splitLeaf(l, c, _horiz, _sw);
		revalidate();
		this.myGlassPane.resetControlBoxLeaf();
		repaint();
	}

	public void removeLeaf(Leaf l) {
		Component c = getMultiSplitLayout().getComponentForNode(l);
		getMultiSplitLayout().removeNode(l);
		if (c != null) {
			removePane(c);
		}
		revalidate();
		this.myGlassPane.resetControlBoxLeaf();
		repaint();
	}

	public void addPane(Component _c) {
		add(_c);
	}

	public void removePane(Component _c) {
		remove(_c);
	}

	public void setSplitGroup(JKMultiSplitGroup s) {
		this.splitGroup = s;
	}

	public JKMultiSplitGroup getSplitGroup() {
		return this.splitGroup;
	}

	public void removeCurrentLeaf() {
		if (this.myGlassPane.controlBoxLeafListener.currentLeaf != null) {
			removeLeaf(this.myGlassPane.controlBoxLeafListener.currentLeaf);
		}
	}

	protected void swapComponents(String name1, String name2) {
		Component c1, c2;
		MultiSplitLayout multiSplitLayout = getMultiSplitLayout();
		c1 = multiSplitLayout.getLayoutComponent(name1);
		c2 = multiSplitLayout.getLayoutComponent(name2);

		if (c1 != null) {
			multiSplitLayout.removeLayoutComponent(c1);
		}
		if (c2 != null) {
			multiSplitLayout.removeLayoutComponent(c2);
		}

		if (c1 != null) {
			multiSplitLayout.addLayoutComponent(name2, c1);
		}
		if (c2 != null) {
			multiSplitLayout.addLayoutComponent(name1, c2);
		}

		revalidate();
		this.myGlassPane.resetControlBoxLeaf();
		repaint();
	}

	protected static void swapComponents(JKMultiSplit _split1, String _name1,
			JKMultiSplit _split2, String _name2) {

		Component c1, c2;
		MultiSplitLayout multiSplitLayout1 = _split1.getMultiSplitLayout();
		MultiSplitLayout multiSplitLayout2 = _split2.getMultiSplitLayout();

		c1 = multiSplitLayout1.getLayoutComponent(_name1);
		c2 = multiSplitLayout2.getLayoutComponent(_name2);

		if (c1 != null) {
			_split1.remove(c1);
		}
		if (c2 != null) {
			_split2.remove(c2);
		}

		if (c1 != null) {
			_split2.add(c1, _name2);
		}

		if (c2 != null) {
			_split1.add(c2, _name1);
		}

		_split1.revalidate();
		_split1.myGlassPane.resetControlBoxLeaf();
		_split1.repaint();

		_split2.revalidate();
		_split2.myGlassPane.resetControlBoxLeaf();
		_split2.repaint();
	}

	private final SwapAction clickSwap = new SwapAction();

	protected void startSelectAndSwap(Leaf _leaf) {
		this.clickSwap.prepare(this, _leaf);
		startLeafSelection(this.clickSwap);
	}

	public void resetControlBoxLeaf(boolean _gotFocus) {
		this.myGlassPane.resetControlBoxLeaf(_gotFocus);
	}

	@Override
	protected void startDrag(int mx, int my) {
		this.myGlassPane.hideControlBoxMask();
		super.startDrag(mx, my);
	}

	@Override
	protected void cancelDrag() {
		super.cancelDrag();
		this.myGlassPane.resetControlBoxLeaf(false);
	}

	@Override
	protected void finishDrag(int x, int y) {
		super.finishDrag(x, y);
		this.myGlassPane.resetControlBoxLeaf(false);
	}

	public void setControlBoxVisible(boolean isControlBoxVisible) {
		this.controlBoxVisible = isControlBoxVisible;
		if (!isControlBoxVisible) {
			this.myGlassPane.hideControlBox();
		} else {
			this.myGlassPane.resetControlBoxLeaf();
		}

	}

	public boolean isControlBoxVisible() {
		return this.controlBoxVisible;
	}

	protected boolean controlBoxVisible = false;
}
