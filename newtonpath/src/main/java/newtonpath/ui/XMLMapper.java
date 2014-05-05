package newtonpath.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import newtonpath.kepler.BodySystemRef;
import newtonpath.kepler.eph.EphLoaderFile;
import newtonpath.statemanager.Observable;
import newtonpath.statemanager.Operation;
import newtonpath.statemanager.OperationResult;
import newtonpath.statemanager.Parameter;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.InputSource;

public class XMLMapper {
	private static XMLMapper INSTANCE = new XMLMapper();
	protected DOMImplementation domModel = null;

	public static XMLMapper getInstance() {
		return INSTANCE;
	}

	public Document printOperationResult(OperationResult obj) {
		return saveOperationResult(System.out, obj);
	}

	public Document saveOperationResult(OutputStream out,
			Collection<OperationResult> results) {
		Document doc = createOrbitsDoc();
		for (OperationResult obj : results) {
			appendOperationResult(doc.getDocumentElement(), obj);
		}
		saveDocument(doc, out);
		return doc;
	}

	public Document saveOperationResult(OutputStream out,
			OperationResult... results) {
		return saveOperationResult(out, Arrays.asList(results));
	}

	public void appendOperationResult(Element orbitListElement,
			OperationResult obj) {
		appendOperationResults(orbitListElement, Arrays.asList(obj));
	}

	public void appendOperationResult(File file, OperationResult obj) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			Document doc = null;
			if (file.canRead()) {
				try {
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					FileInputStream byteStream = new FileInputStream(file);
					InputSource inputSource = new InputSource(byteStream);
					doc = dBuilder.parse(inputSource);
					byteStream.close();
				} catch (Exception e) {
					doc = null;
					e.printStackTrace();
				}
			}
			if (doc == null) {
				doc = createOrbitsDoc();
			}
			final Element orbitListElement = doc.getDocumentElement();
			appendOperationResults(orbitListElement, Arrays.asList(obj));
			OutputStream out = new FileOutputStream(file, false);
			saveDocument(doc, out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Document createOrbitsDoc() {
		Document doc = this.domModel.createDocument("urn:newtonpaths",
				"orbits", null);
		return doc;
	}

	public void appendOperationResults(final Element orbitsElement,
			List<OperationResult> liste) {
		for (OperationResult o : liste) {
			orbitsElement.appendChild(createOperationResult(
					orbitsElement.getOwnerDocument(), o));
		}
	}

	private XMLMapper() {
		try {
			this.domModel = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public OperationResult loadOperationResult(final Element elem) {
		final AproxObs obj = loadAproxObs((Element) elem.getElementsByTagName(
				"orbit").item(0));

		String description = elem.getElementsByTagName("description").item(0)
				.getTextContent();

		Map<String, Operation> opsByName = new HashMap<String, Operation>();
		Operation[] ops = obj.getOperations();
		for (Operation op : ops) {
			opsByName.put(op.toString(), op);
		}
		Operation op = null;
		if (elem.hasAttribute("operation")) {
			opsByName.get(elem.getAttribute("operation"));
		}
		List<Parameter> emptyList = Collections.emptyList();
		return new OperationResult( //
				obj,//
				op, //
				emptyList,//
				description);
	}

	public Element createOperationResult(Document doc, OperationResult obj) {
		OperationResult opres = obj;
		Element el = doc.createElement("result");
		final Operation op = opres.getOperation();
		if (op != null) {
			el.setAttribute("operation", op.toString());
		}
		final Element description = doc.createElement("description");
		description.appendChild(doc.createTextNode(opres.getDescription()));
		el.appendChild(description);

		final Element orbit = doc.createElement("orbit");
		el.appendChild(orbit);

		for (Observable par : OperationResult.addObservables(null,
				((AproxObs) opres.getResultObject()).getParameters(),
				opres.getResultObject())) {
			Element prop = doc.createElement("parameter");
			prop.setAttribute("name", par.getDescription());
			prop.setAttribute("value",
					par.getStringValue(opres.getResultObject()));
			orbit.appendChild(prop);
		}
		if (obj.getComment() != null) {
			Element comment = doc.createElement("comment");
			comment.appendChild(doc.createTextNode(obj.getComment()));
			el.appendChild(comment);
		}
		return el;
	}

	static public AproxObs loadAproxObs(Element elem) {
		AproxObs obj = null;
		try {
			BodySystemRef solarSystemSpacecraft = BodySystemRef
					.getSolarSystemSpacecraft();
			solarSystemSpacecraft.setEphLoader(new EphLoaderFile());
			obj = AproxObs.getInstance(solarSystemSpacecraft);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, Parameter> paramsByName = new HashMap<String, Parameter>();
		for (Observable par : OperationResult.addObservables(null,
				obj.getParameters(), obj)) {
			paramsByName.put(par.getDescription(), par.getParameter());
		}

		NodeList nodes = elem.getElementsByTagName("parameter");
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			try {
				Parameter parameter = paramsByName.get(e.getAttribute("name"));
				parameter.setValue(e.getAttribute("value"));
				parameter.execute(obj);

			} catch (Exception e1) {
				throw new RuntimeException("Error setting property: "
						+ e.getAttribute("name"), e1);
			}
		}

		try {
			obj.section.refreshEpoch();
			obj.section.poincareMap();
			obj.section.toStartPosition();
		} catch (Exception e) {
			e.printStackTrace();
		}
		obj.getEvents();
		return obj;
	}

	public void saveDocument(Document doc, OutputStream out) {

		try {
			javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(
					out);
			Transformer xmlSerializer = TransformerFactory.newInstance()
					.newTransformer();
			xmlSerializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			xmlSerializer.setOutputProperty(OutputKeys.INDENT, "yes");
			xmlSerializer.transform(new DOMSource(doc), result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveColorSchema(Element schemaElement, Map<String, Integer> obj) {
		Document d = schemaElement.getOwnerDocument();
		for (Entry<String, Integer> c : obj.entrySet()) {
			Element newElement = d.createElement("color");
			newElement.setAttribute("name", c.getKey());
			newElement.setAttribute("rgb",
					"0x" + Integer.toHexString(c.getValue().intValue()));
			schemaElement.appendChild(newElement);
		}
	}

	public void saveColorSchemata(Element parentElement, String name,
			Map<String, Integer> obj) {
		Document d = parentElement.getOwnerDocument();
		final Element schemaElement = d.createElement("schema");
		schemaElement.setAttribute("name", name);
		parentElement.appendChild(schemaElement);
		saveColorSchema(schemaElement, obj);
	}

	public Document saveColors(Map<String, Map<String, Integer>> colors) {
		Document doc = this.domModel.createDocument("urn:newtonpaths",
				"colors", null);

		Element e = doc.getDocumentElement();
		for (Entry<String, Map<String, Integer>> x : colors.entrySet()) {
			saveColorSchemata(e, x.getKey(), x.getValue());
		}
		return doc;
	}

	public Map<String, Map<String, Integer>> loadColors(Element schemata) {
		Map<String, Map<String, Integer>> result = new HashMap<String, Map<String, Integer>>();
		NodeList l = schemata.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node n = l.item(i);
			if ("schema".equals(n.getNodeName())) {
				result.put(((Element) n).getAttribute("name"),
						loadColorSchema((Element) n));
			}
		}
		return result;
	}

	public Map<String, Integer> loadColorSchema(Element schema) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		NodeList l = schema.getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node n = l.item(i);
			if ("color".equals(n.getNodeName())) {
				Element el = (Element) n;
				result.put(el.getAttribute("name"),
						Long.decode(el.getAttribute("rgb")).intValue());
			}
		}
		return result;
	}

	public static List<OperationResult> loadOrbits(InputSource... inputSources) {
		List<OperationResult> result = new ArrayList<OperationResult>();
		Chrono c = new Chrono("loadOrbits");
		try {
			final List<Element> elemList = new ArrayList<Element>();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();

			for (InputSource is : inputSources) {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				final NodeList l = dBuilder.parse(is).getDocumentElement()
						.getElementsByTagName("result");
				for (int i = 0; i < l.getLength(); i++) {
					Element element = (Element) l.item(i);
					elemList.add(element);
				}
			}

			for (Element element : elemList) {
				OperationResult r = XMLMapper.getInstance()
						.loadOperationResult(element);
				result.add(r);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		c.report();
		return result;
	}
}
