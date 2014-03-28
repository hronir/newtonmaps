package newtonpath.tasks;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import newtonpath.statemanager.Observable;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.statemanager.Parameter;
import newtonpath.tasks.handler.ResultHandler;
import newtonpath.tasks.step.AbstractStep;
import newtonpath.tasks.step.StepFormat;
import newtonpath.tasks.step.StepOperation;
import newtonpath.ui.AproxObs;
import newtonpath.ui.OperationException;
import newtonpath.ui.XMLMapper;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BatchComputation implements Task {

	private final AproxObs initialOrbit;

	private final List<AbstractStep> operations = new ArrayList<AbstractStep>();
	private ResultHandler resultHandler = null;
	private String reference = null;

	public BatchComputation(AproxObs initialOrbit,
			ResultHandler newResultHandler, String ref) {
		this.resultHandler = newResultHandler;
		this.initialOrbit = initialOrbit;
		this.reference = ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see newtonpath.tasks.Task#getReference()
	 */
	public String getReference() {
		return this.reference;
	}

	private List<AbstractStep> getOperations() {
		return this.operations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see newtonpath.tasks.Task#getResultHandler()
	 */
	public ResultHandler getResultHandler() {
		return this.resultHandler;
	}

	public static Task loadBatchTask(final Element elem, String reference,
			ResultHandler rh) {
		final AproxObs obj = XMLMapper.loadAproxObs((Element) elem
				.getElementsByTagName("orbit").item(0));
		BatchComputation batCal = new BatchComputation(obj, rh, reference);

		Map<String, Operation> opsByName = new HashMap<String, Operation>();
		Operation[] ops = obj.getOperations();
		for (Operation op : ops) {
			opsByName.put(op.toString(), op);
		}

		Map<String, Parameter> paramsByName = new HashMap<String, Parameter>();
		for (Observable par : OperationResult.addObservables(null,
				obj.getParameters(), obj)) {
			paramsByName.put(par.getDescription(), par.getParameter());
		}
		NodeList opList = elem.getChildNodes();
		for (int i = 0; i < opList.getLength(); i++) {
			if ("operation".equals(opList.item(i).getNodeName())) {
				Element opElem = (Element) opList.item(i);
				Operation op = opsByName.get(opElem.getAttribute("name"));

				Map<Parameter, String> parameterList = new HashMap<Parameter, String>();
				NodeList nodes = opElem.getElementsByTagName("parameter");
				for (int j = 0; j < nodes.getLength(); j++) {
					Element e = (Element) nodes.item(j);
					Parameter parameter = paramsByName.get(e
							.getAttribute("name"));
					parameterList.put(parameter, e.getAttribute("value"));
				}

				StepOperation calc = new StepOperation(op, parameterList,
						reference + " operation " + (i + 1));
				batCal.getOperations().add(calc);
			} else if ("format".equals(opList.item(i).getNodeName())) {
				Element opElem = (Element) opList.item(i);
				StepFormat f = new StepFormat(reference + " operation "
						+ (i + 1), opElem.getTextContent());
				batCal.getOperations().add(f);
			}
		}
		return batCal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see newtonpath.tasks.Task#run()
	 */
	public void run() {
		AproxObs orbit = this.initialOrbit;
		for (AbstractStep op : this.operations) {
			try {
				orbit = op.execute(orbit);
			} catch (BatchException e) {
				String exceptionDescription = exceptionDetail(e);

				List<Parameter> emptyList = Collections.emptyList();
				getResultHandler().saveResult(
						new OperationResult(e.getOrbit(), op.getOperation(),
								emptyList, op.getCalculationReference(),
								exceptionDescription));

				throw new RuntimeException(e);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		AbstractStep last = this.operations.get(this.operations.size() - 1);

		getResultHandler().saveResult(last.getOperationResult());
	}

	public static String operationExceptionDetail(AbstractStep op,
			OperationException e) {
		StringWriter sr = new StringWriter();
		PrintWriter pw = new PrintWriter(sr);
		pw.println("Exception on task " + op.getCalculationReference() + "\n");
		e.printStackTrace(pw);
		pw.close();
		String exceptionDescription = sr.getBuffer().toString();
		return exceptionDescription;
	}

	public static String exceptionDetail(OperationException e) {
		StringWriter sr = new StringWriter();
		PrintWriter pw = new PrintWriter(sr);
		e.printStackTrace(pw);
		pw.close();
		String exceptionDescription = sr.getBuffer().toString();
		return exceptionDescription;
	}

}
