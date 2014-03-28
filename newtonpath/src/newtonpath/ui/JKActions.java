package newtonpath.ui;

import java.awt.Font;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import newtonpath.logging.KLogger;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.statemanager.OperationSubmitter;
import newtonpath.statemanager.OperationTask;
import newtonpath.statemanager.Parameter;
import newtonpath.statemanager.StateManager;
import newtonpath.ui.widget.ResultListModel;
import newtonpath.ui.widget.orbit.LineRenderer;

/**
 * 
 * @author oriol
 */
public class JKActions<E extends StateManager> implements OperationSubmitter {
	private static final KLogger LOGGER = KLogger.getLogger(JKActions.class);

	private final AtomicInteger processCount;
	private final ExecutorService execService;
	final CompletionService<OperationResult> completionService;

	private E integrator;
	private final ResultListModel modelHistory;
	private final DefaultListSelectionModel selectionModelHistory;

	public JKActions() {
		super();
		LOGGER.info("Integrator initialization...");
		initInteg();
		this.processCount = new AtomicInteger();
		LOGGER.info("Executor service initialization...");
		this.execService = Executors.newCachedThreadPool();
		LOGGER.info("Completion service initialization...");
		this.completionService = new ExecutorCompletionService<OperationResult>(
				this.execService);

		LOGGER.info("Initializing orbits list...");
		this.modelHistory = new ResultListModel();
		LOGGER.info("Initializing orbit selection...");
		this.selectionModelHistory = new DefaultListSelectionModel();
		this.selectionModelHistory
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent evt) {
						if (!evt.getValueIsAdjusting()) {
							changeHistorySelection();
						}
					}
				});
		LOGGER.info("Initializing completion listener...");
		startCompletionQueueListener();
	}

	public void addOrbit(OperationResult it) {
		this.modelHistory.add(it);
	}

	private void startCompletionQueueListener() {
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						Future<OperationResult> f = JKActions.this.completionService
								.take();
						SwingUtilities.invokeLater(new EndActionTask(f.get()));
					}
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public E getIntegrator() {
		return this.integrator;
	}

	public OperationResult getSelectedResult() {
		OperationResult val = null;
		if (!this.selectionModelHistory.isSelectionEmpty()) {
			val = this.modelHistory.getElementAt(this.selectionModelHistory
					.getLeadSelectionIndex());
		}
		return val;
	}

	public void setSelection(int value) {
		this.selectionModelHistory.clearSelection();
		this.selectionModelHistory.setSelectionInterval(value, value);
	}

	private void initInteg() {
		this.integrator = newIntegrator();
	}

	public E newIntegrator() {
		return null;
	}

	public void execOperation(Object _o, Operation _e,
			Collection<Parameter> _par) {
		OperationTask execMember;
		boolean bOk = true;
		if (bOk) {
			execMember = new OperationTask(_o, _e, _par);
			if (execMember.prepare()) {
				int i = this.processCount.incrementAndGet();
				this.completionService.submit(execMember);
				if (i == 1) {
					stateChanged();
				}
			}
		}
	}

	private class EndActionTask implements Runnable {
		private final OperationResult result;

		public EndActionTask(OperationResult _result) {
			this.result = _result;
		}

		public void run() {
			endOperationTask(this.result);
		}
	}

	@SuppressWarnings("unchecked")
	public void endOperationTask(OperationResult p) {
		if (p != null) {
			this.modelHistory.add(p);
			this.integrator = (E) p.getResultObject();
			this.selectionModelHistory.setSelectionInterval(
					this.modelHistory.getSize() - 1,
					this.modelHistory.getSize() - 1);
		}
		if (this.processCount.decrementAndGet() == 0) {
			stateChanged();
		}
	}

	public void selectionChanged(E i) {

	}

	public void stateChanged() {

	}

	public boolean isRunning() {
		return this.processCount.get() > 0;
	}

	@SuppressWarnings("unchecked")
	void changeHistorySelection() {
		if (!this.selectionModelHistory.isSelectionEmpty()) {
			this.integrator = (E) this.modelHistory.getElementAt(
					this.selectionModelHistory.getLeadSelectionIndex())
					.getResultObject();
			selectionChanged((this.integrator));
		}
	}

	public JList getNewHistoryList() {
		JList lstHistory;
		lstHistory = new JList();
		lstHistory.setModel(this.modelHistory);
		lstHistory.setSelectionModel(this.selectionModelHistory);
		lstHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstHistory.setFont(this.orbitListFont);
		lstHistory.setCellRenderer(this.lineRenderer);
		lstHistory.setFixedCellHeight(50);
		lstHistory.setFixedCellWidth(100);
		return lstHistory;
	}

	public OperationResult[] getHistoryItems() {
		return this.modelHistory.toArray();
	}

	public void refreshOrbitList() {
		List<OperationResult> list = Arrays.asList(this.modelHistory.toArray());
		this.lineRenderer.adjustOrbitList(list);
		this.modelHistory.refresh();
	}

	private Font orbitListFont = UIManager.getFont("List.font").deriveFont(9f);
	private LineRenderer lineRenderer = new LineRenderer(this.orbitListFont, 50);

	public ResultListModel getModelHistory() {
		return this.modelHistory;
	}

	public DefaultListSelectionModel getSelectionModelHistory() {
		return this.selectionModelHistory;
	}
}
