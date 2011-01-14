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

import static java.util.Collections.singletonList;
import static org.apache.uima.util.Level.INFO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.factory.JCasBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.StopWord;
import de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field;

/**
 * The WSDL Reader processes all .wsdl files in the supplied input directory. For each service
 * element, it creates a semi-structured textual description. This description is assigned to the
 * CAS as the CAS text, while its structural properties are represented as annotations. Annotations
 * are added as Field annotations, with the following possible values for the Name property:
 * ServiceName, ServiceDocumentation, Operation, OperationDocumentation, InputParameter,
 * InputParameterDocumentation, InputParameterName, InputParameterType, OutputParameter,
 * OutputParameterDocumentation, OutputParameterName, OutputParameterType
 */
public class WsdlReader
	extends ResourceCollectionReaderBase
{
	/**
	 * The parameter functions as a filter on port names. A port (and its pertaining operations) is
	 * processed only if its name matches the parameter. The default value is httppost. Other
	 * reasonable values are httpget and soap. Note that independent of the setting of this
	 * parameter, a port will only be processed if its name matches .*(httppost|httpget|soap).*
	 */
	public static final String PARAM_PORT_NAME_FILTER = "PortNameFilter";
	@ConfigurationParameter(name = PARAM_PORT_NAME_FILTER, mandatory = true, defaultValue = "httppost")
	private String portNameFilter;

	/**
	 * only portNames with soap support will be processed (if set to true)
	 */
	public static final String PARAM_SOAP_FILTER = "SoapFilter";
	@ConfigurationParameter(name = PARAM_SOAP_FILTER, mandatory = true, defaultValue = "true")
	private boolean soapFilter;

	/**
	 * if set to true, creates one CAS per operation and not per service
	 */
	public static final String PARAM_OPERATION_AS_DOCID = "OperationAsDocId";
	@ConfigurationParameter(name = PARAM_OPERATION_AS_DOCID, mandatory = true, defaultValue = "false")
	private boolean operationAsDocId;

	/**
	 * If set array content types are resolved
	 */
	public static final String PARAM_RESOLVE_ARRAYS = "ResolveArrays";
	@ConfigurationParameter(name = PARAM_RESOLVE_ARRAYS, mandatory = true, defaultValue = "false")
	private boolean resolveArrays;

//	/**
//	 * If true, write a file to the input wsdl directory in which service names and operation names
//	 * are mapped to the original wsdl files they come from. This mapping file is needed by the UKP
//	 * Semantic Service Discovery Demonstrator.
//	 */
//	public static final String PARAM_WRITE_MAPPINGS_FILE = "WriteMappingsFile";
//	@ConfigurationParameter(name = PARAM_WRITE_MAPPINGS_FILE, mandatory = true, defaultValue = "false")
//	private boolean writeMappingsFile;

	public static final String PREFIX_INPUT = "Input";
	public static final String PREFIX_OUTPUT = "Output";
	public static final String SUFFIX_PARAMETER = "Parameter";
	public static final String SUFFIX_PARAMETER_NAME = "ParameterName";
	public static final String SUFFIX_PARAMETER_TYPE = "ParameterType";
	public static final String SUFFIX_PARAMETER_DOCUMENTATION = "ParameterDocumentation";

	public static final String FIELD_FULLTEXT = "fulltext";

	public static final String FIELD_SERVICE_NAME = "ServiceName";
	public static final String FIELD_SERVICE_DOCUMENTATION = "ServiceDocumentation";
	public static final String FIELD_PORT_NAME = "PortName";
	public static final String FIELD_PORT_DOCUMENTATION = "PortDocumentation";
	public static final String FIELD_OPERATION = "Operation";
	public static final String FIELD_OPERATION_DOCUMENTATION = "OperationDocumentation";
	public static final String FIELD_INPUT_PARAMETER = PREFIX_INPUT+SUFFIX_PARAMETER;
	public static final String FIELD_INPUT_PARAMETER_NAME = PREFIX_INPUT+SUFFIX_PARAMETER_NAME;
	public static final String FIELD_INPUT_PARAMETER_TYPE = PREFIX_INPUT+SUFFIX_PARAMETER_TYPE;
	public static final String FIELD_INPUT_PARAMETER_DOCUMENTATION = PREFIX_INPUT+SUFFIX_PARAMETER_DOCUMENTATION;
	public static final String FIELD_OUTPUT_PARAMETER = PREFIX_OUTPUT+SUFFIX_PARAMETER;
	public static final String FIELD_OUTPUT_PARAMETER_NAME = PREFIX_OUTPUT+SUFFIX_PARAMETER_NAME;
	public static final String FIELD_OUTPUT_PARAMETER_TYPE = PREFIX_OUTPUT+SUFFIX_PARAMETER_TYPE;
	public static final String FIELD_OUTPUT_PARAMETER_DOCUMENTATION = PREFIX_OUTPUT+SUFFIX_PARAMETER_DOCUMENTATION;

	public static final String[] ALL_FIELDS = new String[] { FIELD_SERVICE_NAME,
			FIELD_SERVICE_DOCUMENTATION, FIELD_OPERATION, FIELD_OPERATION_DOCUMENTATION,
			FIELD_INPUT_PARAMETER, FIELD_INPUT_PARAMETER_NAME, FIELD_INPUT_PARAMETER_TYPE,
			FIELD_INPUT_PARAMETER_DOCUMENTATION, FIELD_OUTPUT_PARAMETER,
			FIELD_OUTPUT_PARAMETER_NAME, FIELD_OUTPUT_PARAMETER_TYPE,
			FIELD_OUTPUT_PARAMETER_DOCUMENTATION };

	// Status info
	private int completed;
	private List<List<Selector>> batches;
	private Iterator<List<Selector>> batchIterator;

	// Global data
	private javax.wsdl.xml.WSDLReader reader;
	private Resource lastFile;
	private Definition lastDefinition;
	private WsdlTypeModel typeModel;

	// Per CAS data
	private QName lastService;
	private String lastPort;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		try {
			reader = WSDLFactory.newInstance().newWSDLReader();
			reader.setFeature("javax.wsdl.verbose", false);
			reader.setFeature("javax.wsdl.importDocuments", true);

			if (operationAsDocId) {
				collectOperations();
			}
			else {
				collectServices();
			}
			batchIterator = batches.iterator();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		try {
			long tStart = System.currentTimeMillis();
			List<Selector> batch = batchIterator.next();

			// Batches cannot be empty. If a batch is empty here, then the batch collection method
			// should be fixed.
			Resource fileResource = batch.get(0).file;

			// Initialize the CAS
			initCas(aCAS, fileResource);
			DocumentMetaData meta = DocumentMetaData.get(aCAS);
			meta.setDocumentId(batch.get(0).id);

			JCasBuilder casBuilder = new JCasBuilder(aCAS.getJCas());
			for (Selector selector : batch) {
				Definition def = getDefinition(selector);
				processDefinition(casBuilder, def, selector);
			}
			casBuilder.close();
			completed++;
			getUimaContext().getLogger().log(INFO, "Read file " + completed + " of " +
					batches.size() + " [" + fileResource.getPath() + "] in "
					+ (System.currentTimeMillis() - tStart) + "ms");
		}
		catch (CASException e) {
			throw new CollectionException(e);
		}
		catch (WSDLException e) {
			throw new CollectionException(e);
		}
		finally {
			// Reset per-CAS cache
			lastService = null;
			lastPort = null;
		}
	}

	@Override
	public Progress[] getProgress()
	{
		String unit = operationAsDocId ? "operation" : "service";
		return new Progress[] { new ProgressImpl(completed, batches.size(), unit)  };
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return batchIterator.hasNext();
	}

	@Override
	public void close()
		throws IOException
	{
		// Nothing to do
	}

	/**
	 * Load the service definition from the file and build the XML schema model if necessary.
	 * A definition is usually used more than once since we iterate over services or operations,
	 * thus the definition and XML Schema model are cached and reused if possible.
	 */
	private Definition getDefinition(Selector aEntry)
		throws WSDLException, IOException
	{
		if (lastFile == null || !aEntry.file.equals(lastFile)) {
			Resource fileResource = aEntry.file;
			Definition def = reader.readWSDL(fileResource.getResolvedUri().toString(),
					new InputSource(fileResource.getInputStream()));
			lastFile = aEntry.file;
			lastDefinition = def;
			typeModel = WsdlTypeModel.parse(fileResource.getResolvedUri().toString(), def, resolveArrays);
			return def;
		}
		else {
			return lastDefinition;
		}
	}

	private void collectServices()
		throws CollectionException, IOException, WSDLException
	{
		batches = new ArrayList<List<Selector>>();
		while (super.hasNext()) {
			Resource fileResource = nextFile();
			Definition def = reader.readWSDL(fileResource.getResolvedUri().toString(),
					new InputSource(fileResource.getInputStream()));
			@SuppressWarnings("unchecked")
			Collection<Service> services = def.getServices().values();
			for (Service service : services) {
				// Extracting the name is this way is legacy so my judegments do not break...
				// I do not approve of this. The full service QName should be used as ID instead!
				// In particular this way of having it prevents us from loading more than one
				// service per file... (see below)
				// -- REC 2010-02-22
				String fileName = new File(fileResource.getPath()).getName();
				if (fileName.endsWith(".wsdl")) {
					fileName = fileName.substring(0, fileName.length() - 5);
				}
				else if (fileName.endsWith(".xml")) {
					fileName = fileName.substring(0, fileName.length() - 4);
				}

				String id = fileName;

				List<Selector> batch = new ArrayList<Selector>();
				@SuppressWarnings("unchecked")
				Collection<Port> ports = service.getPorts().values();
				for (Port port : ports) {
					if (!includePort(port)) {
						continue;
					}
					@SuppressWarnings("unchecked")
					List<Operation> operations = port.getBinding().getPortType().getOperations();
					for (Operation operation : operations) {
						batch.add(new Selector(fileResource, service, port, operation, id));
					}
				}
				if (batch.size() > 0) {
					batches.add(batch);
				}

				// Can't really have more than one service per file due to how the ID was generated
				// and us used in current judgements...
				// FIXME WSDLReader only processes the first service
				break;
			}
		}
		getUimaContext().getLogger().log(INFO, "Found ["+batches.size()+"] services");
	}

	private void collectOperations()
		throws CollectionException, IOException, WSDLException
	{
		batches = new ArrayList<List<Selector>>();
		while (super.hasNext()) {
			Resource fileResource = nextFile();
			Definition def = reader.readWSDL(fileResource.getResolvedUri().toString(),
					new InputSource(fileResource.getInputStream()));
			@SuppressWarnings("unchecked")
			Collection<Service> services = def.getServices().values();
			for (Service service : services) {
				@SuppressWarnings("unchecked")
				Collection<Port> ports = service.getPorts().values();
				for (Port port : ports) {
					if (!includePort(port)) {
						continue;
					}
					@SuppressWarnings("unchecked")
					List<Operation> operations = port.getBinding().getPortType().getOperations();
					for (Operation operation : operations) {
						// FIXME This ID is possibly not unique since it is possible to have one
						// operation with the same name but different inputs/outputs - so called
						// overloaded methods. However, the judgement files I have need it like
						// this... - REC 2010-02-22
						String id = service.getQName().getLocalPart()+"#"+operation.getName();
						batches.add(singletonList(new Selector(fileResource, service, port,
								operation, id)));
					}
				}
				// Can't really have more than one service per file due to how the ID was generated
				// and us used in current judgements... See collectServices() above.
				// FIXME WSDLReader only processes the first service
				break;
			}
		}
		getUimaContext().getLogger().log(INFO, "Found ["+batches.size()+"] operations");
	}

	private void processDefinition(JCasBuilder casBuilder, Definition definition, Selector selector)
	{
		processService(casBuilder, definition.getService(selector.service), selector);
	}

	private void processService(JCasBuilder casBuilder, Service service, Selector selector)
	{
		// Prevent writing the service-level info multiple times into a single CAS
		if (!selector.service.equals(lastService)) {
			// Add Service name to textualServiceRepresentation
			// This is used in the new version of the reader
			casBuilder.add("Service:", StopWord.class);
			casBuilder.add(" ");
			casBuilder.add(service.getQName().getLocalPart(), Field.class).setName(FIELD_SERVICE_NAME);
			casBuilder.add("\n");

			processDocumentation(casBuilder, service.getDocumentationElement(), "",
					"Service Documentation", FIELD_SERVICE_DOCUMENTATION);
			lastService = selector.service;
		}

		processPort(casBuilder, service.getPort(selector.port), selector);
	}

	private void processPort(JCasBuilder casBuilder, Port port, Selector selector)
	{
		if (!selector.port.equals(lastPort)) {
			// Add Service name to textualServiceRepresentation
			// This is used in the new version of the reader
			casBuilder.add("Port:", StopWord.class);
			casBuilder.add(" ");
			casBuilder.add(port.getName(), Field.class).setName(FIELD_PORT_NAME);
			casBuilder.add("\n");

			processDocumentation(casBuilder, port.getDocumentationElement(), "",
					"Port Documentation", FIELD_PORT_DOCUMENTATION);
			lastPort = selector.port;
		}

		processOperation(casBuilder, port.getBinding().getPortType().getOperation(
				selector.operation, selector.input, selector.output));
	}

	private void processOperation(JCasBuilder casBuilder, Operation operation)
	{
		// Process operation
		casBuilder.add(" ");
		casBuilder.add("Operation:", StopWord.class);
		casBuilder.add(" ");
		casBuilder.add(operation.getName(), Field.class).setName(FIELD_OPERATION);
		casBuilder.add("\n");

		processDocumentation(casBuilder, operation.getDocumentationElement(), " ",
				"Operation Documentation", FIELD_OPERATION_DOCUMENTATION);

		processInput(casBuilder, operation.getInput());
		processOutput(casBuilder, operation.getOutput());
	}

	private void processInput(JCasBuilder casBuilder, Input input)
	{
		if (input == null) {
			return;
		}

		processMessage(casBuilder, input.getMessage(), PREFIX_INPUT);
	}

	private void processOutput(JCasBuilder casBuilder, Output output)
	{
		if (output == null) {
			return;
		}

		processMessage(casBuilder, output.getMessage(), PREFIX_OUTPUT);
	}

	private void processMessage(JCasBuilder casBuilder, Message message, String direction)
	{
		casBuilder.add("  ");
		casBuilder.add(direction + " Parameter:", StopWord.class);
		casBuilder.add(" ");
		casBuilder.add(message.getQName().getLocalPart(), Field.class).setName(
				direction + SUFFIX_PARAMETER);
		casBuilder.add("\n");

		processDocumentation(casBuilder, message.getDocumentationElement(), "  ", direction
				+ " Parameter Documentation", direction + SUFFIX_PARAMETER_DOCUMENTATION);

		@SuppressWarnings("unchecked")
		Collection<Part> parts = message.getParts().values();
		for (Part part : parts) {
			processPart(casBuilder, part, direction);
		}
	}

	private void processPart(JCasBuilder casBuilder, Part part, String direction)
	{
		int begin = casBuilder.getPosition();

		// Try to figure out which element is the one bearing the information to extract the
		// call parameters from
		QName anchor = part.getTypeName();
		if (anchor == null) {
			anchor = part.getElementName();
		}

		// Depending on the way the call is defined the parameters have to be collected in a
		// slightly different manner
		if (!"body".equalsIgnoreCase(part.getName())
				&& !"parameters".equalsIgnoreCase(part.getName())) {
			casBuilder.add("   ");
			casBuilder.add(part.getName(), Field.class).setName(direction + SUFFIX_PARAMETER_NAME);
			casBuilder.add(" (");
			casBuilder.add(anchor.getLocalPart(), Field.class).setName(
					direction + SUFFIX_PARAMETER_TYPE);
			casBuilder.add(")\n");
		}

		// Now add all the parameters
		for (Param p : typeModel.analyzePart(part)) {
			casBuilder.add("   ");
			casBuilder.add(p.getName(), Field.class).setName(direction + SUFFIX_PARAMETER_NAME);
			casBuilder.add(" (");
			casBuilder.add(p.getType(), Field.class).setName(direction + SUFFIX_PARAMETER_TYPE);
			casBuilder.add(")\n");
		}

		casBuilder.add(begin, Field.class).setName(direction+SUFFIX_PARAMETER);
	}

	private void processDocumentation(JCasBuilder casBuilder, Element aElement, String space,
			String label, String field)
	{
		Node element = aElement;

		String docString = "";
		if (element != null) {
			docString = element.getTextContent().replaceAll("\n", " ").trim();
		}

		casBuilder.add(space);
		casBuilder.add(label + ":", StopWord.class);
		casBuilder.add(" ");
		casBuilder.add(docString, Field.class).setName(field);
		casBuilder.add("\n");
	}

	private boolean includePort(Port port)
	{
		// filters duplicates: soap-Operations and soap12-Operations are the same
		if (port.getName().matches(".*soap12")) {
			return false;
		}

		if (soapFilter) {
			if (!isSoap(port)) {
				return false;
			}
		}
		else {
			if (port.getName().matches(".*" + portNameFilter + ".*") == false) {
				// Skip port if it defines methods not matching the portNameFilter
				return false;
			}
		}
		return true;
	}

	private boolean isSoap(Port port)
	{
		@SuppressWarnings("unchecked")
		List<ExtensibilityElement> extElem = port.getBinding().getExtensibilityElements();
		Iterator<ExtensibilityElement> iterElem = extElem.iterator();

		while (iterElem.hasNext()) {
			ExtensibilityElement extElement = iterElem.next();
			String filt = extElement.getElementType().getNamespaceURI();

			if (filt.matches(".*(/soap/)")) {
				return true;
			}
		}

		return false;
	}


	private static class Selector
	{
		// These two are only relevant for the first item in batch. All items in a batch have to be
		// equal with respect to these two properties. Actually they could remain un-set on all but
		// the first element in a batch.
		final Resource file;
		final String id;

		final QName service;
		final String port;
		final String operation;
		final String input;
		final String output;

		public Selector(Resource aFile, Service aService, Port aPort, Operation aOperation, String aId)
		{
			file = aFile;
			service = aService.getQName();
			port = aPort.getName();
			operation = aOperation.getName();
			String inputName = aOperation.getInput().getName();
			input = (inputName != null) ? inputName : ":none";
			String outputName = aOperation.getOutput().getName();
			output = (outputName != null) ? outputName : ":none";
			id = aId;
		}
	}

//	private static class LSInputListImpl implements LSInputList
//	{
//		private List<LSInput> inputs = new ArrayList<LSInput>();
//
//		public void add(LSInput aInput)
//		{
//			inputs.add(aInput);
//		}
//
//		@Override
//		public int getLength()
//		{
//			return inputs.size();
//		}
//
//		@Override
//		public LSInput item(int aIndex)
//		{
//			return inputs.get(aIndex);
//		}
//	}
}
