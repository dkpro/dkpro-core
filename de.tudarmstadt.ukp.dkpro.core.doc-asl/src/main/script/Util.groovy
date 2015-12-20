import java.text.BreakIterator;

class Util {
    static def typeLink(name)
    {
        if (name.startsWith('uima.cas.')) {
          return name.substring(9);
        }
        if (name.startsWith('uima.tcas.')) {
          return "${name.tokenize('.')[-1]}"
        }
        else {
          return "<<typesystem-reference.adoc#type-${name},${name.tokenize('.')[-1]}>>"
        }
    }

    static def engineLink(name)
    {
        return "<<component-reference.adoc#engine-${name},${name}>>"
    }
    
    static def preparePassthrough(description)
    {
        if (description) {
            if (
                !description.contains('<p>') ||
                description.contains('<div>') ||
                description.contains('<table>')
            ) {
                description = "<p>${description}</p>";
            }
            if (!description.startsWith('<p>') && description.contains('<p>')) {
                def i = description.indexOf('<p>');
                description = "<p>${description[0..i-1]}</p>${description[i..-1]}";
            }
            description = "<div class='paragraph'>${description}</div>";
        }
        return description;
    }
    
    static def shortDesc(description) {
        if (description) {
            BreakIterator tokenizer = BreakIterator.getSentenceInstance(Locale.US);
            tokenizer.setText(description);
            def start = tokenizer.first();
            def end = tokenizer.next();
            if (start > -1 && end > -1) {
                description = description.substring(start, end);
            }
            description = description.trim().replaceAll(']', '{endsb}');
        }
        return description ? "pass:[${description}]" : '__No description__';
    }
}