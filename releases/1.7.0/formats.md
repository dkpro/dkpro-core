---
layout: page-fullwidth
title: "Data Formats (1.7.0)"

docs-gpl: "apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
docs-asl: "apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
---

## Data formats

<table border="1" cellspacing="0" cellpadding="2">
<tr>
<th>Format</th>
<th>Reader</th>
<th>Writer</th>
<th>Comments</th>
</tr>

<tr>
<td><a href="http://acl-arc.comp.nus.edu.sg">ACL Anthology Corpus</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/aclanthology/AclAnthologyReader.html">AclAnthologyReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td><a href="http://www.natcorp.ox.ac.uk">British National Corpus XML</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/bnc/BncReader.html">BncReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td><a href="http://weblicht.sfs.uni-tuebingen.de/englisch/tutorials/html/index.html">CLARIN WebLicht TCF</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tcf/TcfReader.html">TcfReader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tcf/TcfWriter.html">TcfWriter</a></td>
<td></td>
</tr>

<tr>
<td><a href="http://www.clips.uantwerpen.be/conll2000/chunking/">CoNLL 2000 format</a></td>
<td>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2000Reader.html">Conll2000Reader</a></td>
<td>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2000Writer.html">Conll2000Writer</a></td>
<td>Chunking</td>
</tr>

<tr>
<td><a href="http://www.clips.uantwerpen.be/conll2002/ner/">CoNLL 2002 format</a></td>
<td>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2002Reader.html">Conll2002Reader</a></td>
<td>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2002Writer.html">Conll2002Writer</a></td>
<td>Named entities</td>
</tr>

<tr>
<td><a href="http://ilk.uvt.nl/conll/">CoNLL 2006 format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2006Reader.html">Conll2006Reader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2006Writer.html">Conll2006Writer</a></td>
<td>Dependency parsing</td>
</tr>

<tr>
<td><a href="http://ufal.mff.cuni.cz/conll2009-st/task-description.html">CoNLL 2009 format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2009Reader.html">Conll2009Reader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2009Writer.html">Conll2009Writer</a></td>
<td>Semantic dependencies</td>
</tr>

<tr>
<td><a href="http://conll.cemantix.org/2012/data.html">CoNLL 2012 format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2012Reader.html">Conll2012Reader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/conll/Conll2012Writer.html">Conll2012Writer</a></td>
<td>Coreference & Constituents</td>
</tr>

<tr>
<td>HTML</td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/html/HtmlReader.html">HtmlReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td><a href="http://cwb.sourceforge.net">IMS Corpus Workbench format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/imscwb/ImsCwbReader.html">ImsCwbReader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/imscwb/ImsCwbWriter.html">ImsCwbWriter</a></td>
<td>also for some <a href="http://wacky.sslmit.unibo.it/doku.php?id=corpora">WaCKy</a> corpora</td>
</tr>

<tr>
<td><a href="http://www.coli.uni-saarland.de/~thorsten/publications/Brants-CLAUS98.pdf">NeGra Export Format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/negra/NegraExportReader.html">NegraExportReader</a></td>
<td></td>
<td>supports format versions 3 and 4</td>
</tr>

<tr>
<td>PDF</td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/pdf/PdfReader.html">PdfReader</a></td>
<td></td>
<td>tries to detect heading and paragraph boundaries</td>
</tr>

<tr>
<td valign="top">Penn Treebank Chunked</td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/penntree/PennTreebankChunkedReader.html">PennTreebankChunkedReader</a></td>
<td valign="top">
</td>
<td valign="top">
</td>
</tr>

<tr>
<td valign="top">Penn Treebank Combined</td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/penntree/PennTreebankCombinedReader.html">PennTreebankCombinedReader</a></td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/penntree/PennTreebankCombinedWriter.html">PennTreebankCombinedWriter</a></td>
<td valign="top">
</td>
</tr>

<tr>
<td>SQL Databases</td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jdbc/JdbcReader.html">JdbcReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td>TEI</td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tei/TeiReader.html">TeiReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td valign="top">Text</td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/text/TextReader.html">TextReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/text/StringReader.html">StringReader</a></td>
<td valign="top"><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/text/TextWriter.html">TextWriter</a></td>
<td></td>
</tr>

<tr>
<td><a href="http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html">Tiger XML</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tiger/TigerXmlReader.html">TigerXmlReader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tiger/TigerXmlWriter.html">TigerXmlWriter</a></td>
<td>supports <a href="http://www.coli.uni-saarland.de/projects/salsa/salto/doc/html/node55.html">SALSA</a> frame information as well</td>
</tr>

<tr>
<td><a href="http://www.sfs.uni-tuebingen.de/tupp/doc/markupmanual.pdf">TüPP D/Z XML</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/tuepp/TueppReader.html">TueppReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td><a href="http://uima.apache.org/d/uimaj-2.6.0/references.html#ugr.ref.compress">UIMA Binary formats</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/bincas/BinaryCasReader.html">BinaryCasReader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/bincas/BinaryCasWriter.html">BinaryCasWriter</a></td>
<td></td>
</tr>

<tr>
<td><a href="http://uima.apache.org/d/uimaj-2.6.0/references.html#ugr.ref.xmi">UIMA XMI format</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xmi/XmiReader.html">XmiReader</a></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xmi/XmiWriter.html">XmiWriter</a></td>
<td></td>
</tr>

<tr>
<td>Web1t n-gram frequencies</td>
<td></td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/web1t/Web1TFormatWriter.html">Web1TFormatWriter</a></td>
<td></td>
</tr>


<tr>
<td>Wikipedia (online)</td>
<td><a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/bliki/BlikiWikipediaReader.html">BlikiWikipediaReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td valign="top">Wikipedia (offline via JWPL)</td>
<td>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaArticleInfoReader.html">WikipediaArticleInfoReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaArticleReader.html">WikipediaArticleReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaDiscussionReader.html">WikipediaDiscussionReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaLinkReader.html">WikipediaLinkReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaPageReader.html">WikipediaPageReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaQueryReader.html">WikipediaQueryReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaRevisionPairReader.html">WikipediaRevisionPairReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaRevisionReader.html">WikipediaRevisionReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/jwpl/WikipediaTemplateFilteredArticleReader.html">WikipediaTemplateFilteredArticleReader</a></td>
<td></td>
<td></td>
</tr>


<tr>
<td valign="top">XML (generic)</td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xml/XmlReader.html">XmlReader</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xml/XmlReaderText.html">XmlReaderText</a><br/>
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xml/XmlReaderXPath.html">XmlReaderXPath</a></td>
<td valign="top">
<a href="{{ site.url }}/releases/1.7.0{{ page.docs-asl}}io/xml/XmlWriterInline.html">XmlWriterInline</a></td>
<td valign="top"></td>
</tr>

</table>