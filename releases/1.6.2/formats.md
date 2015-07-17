---
layout: page-fullwidth
title: "DKPro Core 1.6.2 Data Formats"

docs-gpl: "http://dkpro-core-gpl.googlecode.com/svn/de.tudarmstadt.ukp.dkpro.core-gpl/tags/de.tudarmstadt.ukp.dkpro.core-gpl-1.6.2/apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
docs-asl: "http://dkpro-core-asl.googlecode.com/svn/de.tudarmstadt.ukp.dkpro.core-asl/tags/de.tudarmstadt.ukp.dkpro.core-asl-1.6.2/apidocs/index.html?de/tudarmstadt/ukp/dkpro/core/"
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
<td>ACL Anthology Corpus</td>
<td><a href="{{page.docs-asl}}io/aclanthology/AclAnthologyReader.html">AclAnthologyReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td>British National Corpus XML</td>
<td><a href="{{page.docs-asl}}io/bnc/BncReader.html">BncReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td>CLARIN WebLicht TCF</td>
<td><a href="{{page.docs-asl}}io/tcf/TcfReader.html">TcfReader</a></td>
<td><a href="{{page.docs-asl}}io/tcf/TcfWriter.html">TcfWriter</a></td>
<td></td>
</tr>


<tr>
<td>CoNLL 2006 format</td>
<td><a href="{{page.docs-asl}}io/conll/Conll2006Reader.html">Conll2006Reader</a></td>
<td><a href="{{page.docs-asl}}io/conll/Conll2006Writer.html">Conll2006Writer</a></td>
<td></td>
</tr>

<tr>
<td>HTML</td>
<td><a href="{{page.docs-asl}}io/html/HtmlReader.html">HtmlReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td>IMS Corpus Workbench format</td>
<td><a href="{{page.docs-asl}}io/imscwb/ImsCwbReader.html">ImsCwbReader</a></td>
<td><a href="{{page.docs-asl}}io/imscwb/ImsCwbWriter.html">ImsCwbWriter</a></td>
<td>also for some <a href="http://wacky.sslmit.unibo.it/doku.php?id=corpora">WaCKy corpora</a></td>
</tr>

<tr>
<td>NeGra Export Format</td>
<td><a href="{{page.docs-asl}}io/negra/NegraExportReader.html">NegraExportReader</a></td>
<td></td>
<td>supports format versions 3 and 4</td>
</tr>

<tr>
<td>PDF</td>
<td><a href="{{page.docs-asl}}io/pdf/PdfReader.html">PdfReader</a></td>
<td></td>
<td>tries to detect heading and paragraph boundaries</td>
</tr>

<tr>
<td>SQL Databases</td>
<td><a href="{{page.docs-asl}}io/jdbc/JdbcReader.html">JdbcReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td>TEI</td>
<td><a href="{{page.docs-asl}}io/tei/TeiReader.html">TeiReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td valign="top">Text</td>
<td valign="top">
<a href="{{page.docs-asl}}io/text/TextReader.html">TextReader</a><br/>
<a href="{{page.docs-asl}}io/text/StringReader.html">StringReader</a></td>
<td valign="top"><a href="{{page.docs-asl}}io/text/TextWriter.html">TextWriter</a></td>
<td></td>
</tr>

<tr>
<td>Tgrep</td>
<td></td>
<td><a href="{{page.docs-gpl}}io/tgrep/TGrepWriter.html">TGrepWriter</a></td>
<td></td>
</tr>

<tr>
<td>Tiger XML</td>
<td><a href="{{page.docs-asl}}io/tiger/TigerXmlReader.html">TigerXmlReader</a></td>
<td><a href="{{page.docs-asl}}io/tiger/TigerXmlWriter.html">TigerXmlWriter</a></td>
<td></td>
</tr>

<tr>
<td>UIMA Binary CAS formats</td>
<td><a href="{{page.docs-asl}}io/bincas/BinaryCasReader.html">BinaryCasReader</a></td>
<td><a href="{{page.docs-asl}}io/bincas/BinaryCasWriter.html">BinaryCasWriter</a></td>
<td></td>
</tr>

<tr>
<td>UIMA XMI format</td>
<td><a href="{{page.docs-asl}}io/xmi/XmiReader.html">XmiReader</a></td>
<td><a href="{{page.docs-asl}}io/xmi/XmiWriter.html">XmiWriter</a></td>
<td></td>
</tr>

<tr>
<td>Web1t n-gram frequencies</td>
<td><a href="{{page.docs-asl}}io/web1t/Web1TFormatWriter.html">Web1TFormatWriter</a></td>
<td></td>
<td></td>
</tr>


<tr>
<td>Wikipedia (online)</td>
<td><a href="{{page.docs-asl}}io/bliki/BlikiWikipediaReader.html">BlikiWikipediaReader</a></td>
<td></td>
<td></td>
</tr>

<tr>
<td valign="top">Wikipedia (offline via JWPL)</td>
<td>
<a href="{{page.docs-asl}}io/jwpl/WikipediaArticleInfoReader.html">WikipediaArticleInfoReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaArticleReader.html">WikipediaArticleReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaDiscussionReader.html">WikipediaDiscussionReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaLinkReader.html">WikipediaLinkReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaPageReader.html">WikipediaPageReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaQueryReader.html">WikipediaQueryReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaRevisionPairReader.html">WikipediaRevisionPairReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaRevisionReader.html">WikipediaRevisionReader</a><br/>
<a href="{{page.docs-asl}}io/jwpl/WikipediaTemplateFilteredArticleReader.html">WikipediaTemplateFilteredArticleReader</a></td>
<td></td>
<td></td>
</tr>


<tr>
<td valign="top">XML (generic)</td>
<td valign="top">
<a href="{{page.docs-asl}}io/xml/XmlReader.html">XmlReader"</a><br/>
<a href="{{page.docs-asl}}io/xml/XmlReaderText.html">XmlReaderText"<a/><br/>
<a href="{{page.docs-asl}}io/xml/XmlReaderXPath.html">XmlReaderXPath</a></td>
<td valign="top"><a href="{{page.docs-asl}}io/xml/XmlWriterInline.html">XmlWriterInline</a></td>
<td valign="top"></td>
</tr>

</table>