package newtonpath.ui;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import newtonpath.ui.widget.AbstractColorSchema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class ConfigurationManager {
	public static void loadColors(InputSource stream) {
		Chrono c = new Chrono("loadColors");
		try {
			Chrono parse = new Chrono("loadColors:parseXml");

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(stream);
			Element docElement = document.getDocumentElement();
			if (docElement != null && "colors".equals(docElement.getTagName())) {
				Map<String, Map<String, Integer>> m = XMLMapper.getInstance()
						.loadColors(docElement);

				for (String className : m.keySet()) {
					Class<?> clz = Class.forName(className);
					Map<String, Integer> val = m.get(className);

					setColorSchema(clz, val);
				}
			}

			parse.report();
		} catch (Exception e) {
			e.printStackTrace();
		}
		c.report();
	}

	public static void setColorSchema(Class<?> clz, Map<String, Integer> val) {
		AbstractColorSchema s;
		try {
			s = (AbstractColorSchema) clz.getField("colors").get(null);
			s.setSchema(val);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
