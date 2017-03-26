---
layout: page-fullwidth
title: "DKPro Core 1.7.0 Type System"
---

This graphics gives an overview of the most important types in the DKPro Core type system. All types shown here inherit from the UIMA `Annotation` type which provides `start` and `end` offsets.

If you want to view your annotated documents with the uima annotation viewer, you could serialize your documents using the DKPro XmiWriter. If you configure it correctly, e.g. like below, then it also creates a comprehensive type system descriptor.

In your pipeline:
```
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class ExampleTypeSystemOutputGenerator {
public void run() throws UIMAException, IOException {
		File resultDataDir = new File(System.getProperty("user.dir") + File.separator + "data");
		resultDataDir.mkdirs();
		File typesystemFile = new File(resultDataDir, "typesystem.xml");

		AnalysisEngine documentSerializer = AnalysisEngineFactory.createEngine(XmiWriter.class, 
				XmiWriter.PARAM_TARGET_LOCATION, resultDataDir.getAbsolutePath(),  // this has to be a String
				XmiWriter.PARAM_USE_DOCUMENT_ID, true,
				XmiWriter.PARAM_TYPE_SYSTEM_FILE, typesystemFile);            // this has to be a File
				
				
				// run your pipeline
}
}
```

![DKPro Core Type System]({{ site.urlimg }}/DKProCoreTypeSystem.png)
