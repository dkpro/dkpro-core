/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.DOMInputSource;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XSGrammar;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTypeDefinition;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class WsdlTypeModel
{
	private XSModel schemaModel;
	private Map<String, QName> arrayMapping;

	// Required depth of recursions for complex type expansion
	private int initialRecDepth = 5;

	public List<Param> analyzePart(Part part)
	{
		// Try to figure out which element is the one bearing the information to extract the
		// call parameters from
		QName anchor = part.getTypeName();
		if (anchor == null) {
			anchor = part.getElementName();
		}

		// Depending on the way the call is defined the parameters have to be collected in a
		// slightly different manner
		List<Param> params = new ArrayList<Param>();
		if ("body".equalsIgnoreCase(part.getName())
				|| "parameters".equalsIgnoreCase(part.getName())) {
			analyzeElement(anchor, params);
		}
		else {
			analyzeType(anchor, params);
		}

		return params;
	}

	// this method extracts types and names from complex elements (i.e. elements
	// of complex types)
	private void analyzeElement(QName aElementName, List<Param> aParams)
		throws XNIException
	{
		if (schemaModel == null) {
			return;
		}

		XSNamedMap elementDecls = schemaModel.getComponents(XSConstants.ELEMENT_DECLARATION);

		// iterate over array of elements till the requested element is found
		// and then extract the particles from this element
		for (int i = 0; i < elementDecls.getLength(); i++) {
			XSObject item = elementDecls.item(i);
			if (matches(item, aElementName)) {
				XSElementDeclaration element = (XSElementDeclaration) item;
				String particleType = element.getTypeDefinition().getName();

				switch (element.getTypeDefinition().getTypeCategory()) {
				case XSTypeDefinition.COMPLEX_TYPE: {
					if (particleType == null) {
						particleType = "complexType";
					}
					aParams.add(new Param(element.getName(), particleType));
					analyzeComplexType(aParams, (XSComplexTypeDecl) element
							.getTypeDefinition(), initialRecDepth);
					break;
				}
				case XSTypeDefinition.SIMPLE_TYPE:
					aParams.add(new Param(element.getName(), particleType));
					break;
				default:
					throw new IllegalStateException("Unknown type ["
							+ element.getTypeDefinition().getTypeCategory() + "]");
				}
				break; // There can only be one item matching the qName, so we can bail out
			}
		}
	}

	/**
	 * This method extracts types and names of messages, which have complex types
	 */
	private void analyzeType(QName aTypeName, List<Param> aParams)
		throws XNIException
	{
		if (schemaModel == null) {
			return;
		}

		XSNamedMap components = schemaModel.getComponents(XSTypeDefinition.COMPLEX_TYPE);

		for (int i = 0; i < components.getLength() - 1; i++) {
			XSObject item = components.item(i);
			// iterate over complex types till the requested type is found and then extract the
			// particles from this complex type
			if (matches(item, aTypeName)) {
				XSComplexTypeDecl newComplexTypeElem = (XSComplexTypeDecl) item;
				analyzeComplexType(aParams, newComplexTypeElem, initialRecDepth);
				break; // There can only be one item matching the qName, so we can bail out
			}
		}
	}

	/**
	 * Extract all the particles from the element, iterate over those and if these particles
	 * represent complex elements too, extract particles of those recursive
	 */
	private void analyzeComplexType(List<Param> aParams,
			XSComplexTypeDecl newComplexTypeElem, Integer recDepth)
	{
		recDepth--;

		QName arrayType = arrayMapping.get(newComplexTypeElem.getName());
		if (arrayType != null) {
			analyzeType(arrayType, aParams);
		}

		XSParticle particle = newComplexTypeElem.getParticle();

		if (particle == null) {
			return;
		}

		XSObjectList particles = ((XSModelGroup) particle.getTerm()).getParticles();

		for (int j1 = 0; j1 < particles.getLength(); j1++) {
			XSParticle item = (XSParticle) particles.item(j1);

			// If it is not an element, ignore it
			if (item.getTerm().getType() != XSComplexTypeDefinition.CONTENTTYPE_ELEMENT) {
				continue;
			}

			XSElementDeclaration elementDecl = (XSElementDeclaration) item.getTerm();
			aParams.add(new Param(elementDecl.getName(), elementDecl.getTypeDefinition().getName()));

			// If the element is of a complex type, recurse into it
			if ((elementDecl.getTypeDefinition().getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)
					&& recDepth > 0) {
				XSComplexTypeDecl complexTypeDscl = (XSComplexTypeDecl) elementDecl
						.getTypeDefinition();
				analyzeComplexType(aParams, complexTypeDscl, recDepth);
			}
		}
	}

	public static WsdlTypeModel parse(String aBaseSystemId, Definition def, boolean resolveArrays)
		throws XNIException, IOException
	{
		WsdlTypeModel m = new WsdlTypeModel();
		m.parseModel(aBaseSystemId, def, resolveArrays);
		return m;
	}

	/**
	 * Get the XML Schema model attached to this WSDL definition.
	 */
	private void parseModel(String aBaseSystemId, Definition def, boolean resolveArrays)
		throws XNIException, IOException
	{
		arrayMapping = new HashMap<String, QName>();

		if (def.getTypes() == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<Schema> extElem = def.getTypes().getExtensibilityElements();
		if (extElem.size() == 0) {
			schemaModel = null;
		}

		Schema schema = extElem.get(0);

		Element root = schema.getElement();
		Document doc = root.getOwnerDocument();
		Element eImport = doc.createElementNS("http://www.w3.org/2001/XMLSchema", "import");
		eImport.setAttribute("namespace", "http://schemas.xmlsoap.org/soap/encoding/");
		eImport.setAttribute("schemaLocation", WsdlReader.class.getResource("encoding.xsd").toString());
		root.insertBefore(eImport, root.getFirstChild());

		XMLGrammarPreparser xmlGrammarPreparser = new XMLGrammarPreparser();
		xmlGrammarPreparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);

		DOMInputSource src = new DOMInputSource(schema.getElement());
		src.setBaseSystemId(aBaseSystemId);
		src.setSystemId(aBaseSystemId);
		Grammar preparseGrammar = xmlGrammarPreparser.preparseGrammar(
				XMLGrammarDescription.XML_SCHEMA, src);

		XSGrammar xsGrammar = (XSGrammar) preparseGrammar;
		if (xsGrammar == null) {
			schemaModel = null;
		}
		else {
			schemaModel = xsGrammar.toXSModel();
		}

		if (resolveArrays) {
			// The grammar parser does not allow to access WSDL array informations, so we have to try
			// and extract this here manually on the DOM level
			try {
				SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
				nsContext.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
				nsContext.addNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");

				XPath xpAttrs = new DOMXPath("//xsd:attribute[@wsdl:arrayType]");
				xpAttrs.setNamespaceContext(nsContext);
				List<Element> result = xpAttrs.selectNodes(root);

				XPath xpType = new DOMXPath("ancestor::xsd:complexType/@name");
				xpType.setNamespaceContext(nsContext);

				for (Element e : result) {
					String type = ((Node) xpType.selectSingleNode(e)).getNodeValue();
					if (arrayMapping.get(type) != null) {
						throw new IOException("WSDL array type ["+type+"] redefined");
					}

					String arrayType = e.getAttributeNS("http://schemas.xmlsoap.org/wsdl/", "arrayType");
					arrayType = arrayType.substring(0, arrayType.length()-2);
					String[] components = arrayType.split(":", 2);
					if (components.length == 1) {
						arrayMapping.put(type, QName.valueOf('{'+XMLConstants.DEFAULT_NS_PREFIX+'}'+components[0]));
					}
					else {
						String uri = e.lookupNamespaceURI(components[0]);
						arrayMapping.put(type, QName.valueOf('{'+uri+'}'+components[1]));
					}
				}
			}
			catch (JaxenException e) {
				throw new IOException(e);
			}
		}

//		try {
//			StringWriter stw = new StringWriter();
//	        Transformer serializer = TransformerFactory.newInstance().newTransformer();
//	        serializer.transform(new DOMSource(schema.getElement()), new StreamResult(stw));
//
//			Vector<String> uris = new Vector<String>();
//			uris.add(WsdlReader.class.getResource("/encoding.xsd").toString());
//			uris.add(WsdlReader.class.getResource("/envelope.xsd").toString());
//			uris.add(WsdlReader.class.getResource("/envelope.xsd").toString());
//
//			LSInputListImpl list = new LSInputListImpl();
//			list.add(new DOMInputImpl(null, null, null, WsdlReader.class.getResource("/encoding.xsd").openStream(), null));
//			list.add(new DOMInputImpl(null, null, null, WsdlReader.class.getResource("/envelope.xsd").openStream(), null));
//			list.add(new DOMInputImpl(aBaseSystemId, aBaseSystemId, aBaseSystemId, new StringReader(stw.getBuffer().toString()), null));
//
//			XSLoaderImpl loader = new XSLoaderImpl();
//			return loader.loadInputList(list);
//		}
//		catch (TransformerException e) {
//			throw new IOException(e);
//		}
	}

	private static boolean matches(XSObject aObject, QName aQName)
	{
		return aObject.getName().equals(aQName.getLocalPart())
				&& aObject.getNamespace().equals(aQName.getNamespaceURI());
	}
}
