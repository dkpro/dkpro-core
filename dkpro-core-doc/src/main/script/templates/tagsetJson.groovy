import groovy.json.*;
import java.util.regex.Pattern;

def headerCommentText(tagset)
{
    def text = tagset.mapping.layout.getCanonicalHeaderComment(true) ?: '';

    def lines = stripCommentChar(text.split('\n'));
    
    for (int i = 0; i < lines.size(); i++) {
        // We null the first line here because we assume it contains the long name and we already
        // have that in the heading.
        lines[i] = i == 0 ? '' : lines[i];
    }
    
    text = lines.join('\n').trim();

    return text ?: 'No description.';
}

def stripCommentChar(lines)
{
    result = [];
    for (int i = 0; i < lines.size(); i++) {
        def line = lines[i].trim();
        
        if (line.startsWith('#')) {
            line = line.length() > 1 ? line[1..-1].trim() : '';
        }
        
        result << line;
    }
    return result;
}

def commentText(tag,tagset)
{
    def text = tagset.mapping.layout.getCanonicalComment(tag, true);
    if (text) {
        def lines = stripCommentChar(text.split('\n'));

        // If there is a multiline description, check if the first line starts with the tag. If
        // that is the case, treat all lines as the description. If it is not the case, treat
        // only the last line as the description
        if (lines.size() > 1 && lines[0].startsWith(tag)) {
            // Remove tag
            lines[0] = lines[0].substring(tag.length());
            text = lines.join('\n');
        }
        else {
            text = !lines.isEmpty() ? lines[-1] : '';
        }
            
        if (text == tag) {
            text = '';
        }
        else if (text ==~ "${Pattern.quote(tag)}\\b.*") {
            text = text[tag.length()..-1].trim();
            if (text.startsWith('-') || text.startsWith(':')) {
                text = text[1..-1].trim();
            }
        }
        
        text = text.trim();
    }

    return text ?: 'No description.';
}

def references(tagset) 
{
    def sb = new StringBuilder();
    
    tagset.mapping.keys.sort()
        .findAll { it.startsWith('__META_SOURCE_URL__') }
        .each { source ->
            def link = tagset.mapping.getString(source).trim();
            sb.append  "* ${link}\n";
        } // sources
        
    return sb.toString();
}

tagsets
    .findAll { !it.value.mapping.getString('__META_REDIRECT__') }
    .each { id, tagset ->
        println "Writing tagset: ${tagset.longName} (${id})"
        
        def ts = [
            name: tagset.longName,
            description: headerCommentText(tagset),
            language: tagset.lang,
            tags: []
            ]
        
        def refs = references(tagset);
        if (refs) {
            ts['description'] = ts['description'] + "\n\nReferences:\n" + refs;
        }

        ts['tags'] = tagset.mapping.keys.sort()
            .findAll {
                !it.equals('__META_TYPE_BASE__') &&
                !it.startsWith('__META_SOURCE_URL__') &&
                !it.equals('*') }
            .collect { tag -> [
                tag_name: tag,
                tag_description: commentText(tag,tagset)]
            }
        
        def out = new File("${project.basedir}/target/generated-docs/tagsets/${id}.json");
        out.getParentFile().mkdirs();
        out.withPrintWriter("UTF-8", { it.print JsonOutput.prettyPrint(JsonOutput.toJson(ts)) });
    }