package com.xz.lucene.util;

import com.xz.lucene.pojo.Doc;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by xuz-d on 2017/2/13.
 */
public class DocUtil {

    public static Doc parseHtml(File file) {
        Source source = null;
        try {
            source = new Source(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Element title = source.getFirstElement(HTMLElementName.TITLE);
            String content = source.getTextExtractor().toString();
            Doc doc = new Doc();
            doc.setContent(content);
            doc.setTitle(title.getTextExtractor().toString());
            String path = file.getAbsolutePath();
            doc.setUrl("http://" + path.substring(3));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
