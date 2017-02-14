package com.xz.lucene.controller;

import com.xz.lucene.pojo.Doc;
import com.xz.lucene.util.DocUtil;
import com.xz.lucene.util.PageUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by xuz-d on 2017/2/13.
 */
@Controller
public class LuceneController {

    private final String INDEX_DIR = "F:\\index";
    private final String DATA_DIR = "F:\\www.bjsxt.com";

    @RequestMapping("/index.do")
    public String createIndex() {
        System.out.print("1");
        File file = new File(INDEX_DIR);
        if (file.exists()) {
            file.delete();
            file.mkdirs();
        }
        Directory dir = null;
        try {
            dir = FSDirectory.open(new File(INDEX_DIR));
            Analyzer an = new IKAnalyzer();
            IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_9, an);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, conf);
            Collection<File> files = FileUtils.listFiles(new File(DATA_DIR), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            RAMDirectory rdir = new RAMDirectory();
            IndexWriterConfig conf1 = new IndexWriterConfig(Version.LUCENE_4_9, an);
            conf1.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter ramWriter = new IndexWriter(rdir, conf1);
            int count = 0;
            for (File f : files) {
                Doc doc = DocUtil.parseHtml(f);
                Document document = new Document();
                if (doc == null) {
                    continue;
                }
                count++;
                document.add(new StringField("title", doc.getTitle(), Field.Store.YES));
                document.add(new StringField("url", doc.getUrl(), Field.Store.YES));
                document.add(new TextField("content", doc.getContent(), Field.Store.YES));
                ramWriter.addDocument(document);
                if (count == 50) {
                    ramWriter.close();
                    writer.addIndexes(rdir);
                    rdir = new RAMDirectory();
                    IndexWriterConfig conf2 = new IndexWriterConfig(Version.LUCENE_4_9, an);
                    conf2.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                    ramWriter = new IndexWriter(rdir, conf2);
                    count = 0;
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "index.jsp";
    }

    @RequestMapping("/search.do")
    public String search(String keywords, int num, Model model) throws Exception {
//        String s = new String(keywords.getBytes("iso8859-1"), "UTF-8");
        System.out.println(keywords);
        Directory dir = FSDirectory.open(new File(INDEX_DIR));
        IKAnalyzer analyzer = new IKAnalyzer();
        MultiFieldQueryParser mq = new MultiFieldQueryParser(Version.LUCENE_4_9, new String[]{"title", "content"}, analyzer);
        Query query = mq.parse(keywords);
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs td = searcher.search(query, 10 * num);
        ScoreDoc[] scoreDocs = td.scoreDocs;
        System.out.println(td.totalHits);
        int count = td.totalHits;
        PageUtil<Doc> page = new PageUtil<Doc>(num + "", 10 + "", count);
        List<Doc> ls = new ArrayList<Doc>();
        for (int i = (num - 1) * 10; i < Math.min(num * 10, count); i++) {
            ScoreDoc sd = scoreDocs[i];
            int docId = sd.doc;
            Document document = reader.document(docId);
            Doc hb = new Doc();
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
            QueryScorer qs = new QueryScorer(query);
            Highlighter highlighter = new Highlighter(formatter, qs);
            String title = highlighter.getBestFragment(analyzer, "title", document.get("title"));
            String context = highlighter.getBestFragments(analyzer.tokenStream("content", document.get("content")), document.get("content"), 3, "...");
            hb.setContent(context);
            hb.setTitle(title);
            hb.setUrl(document.get("url"));
            ls.add(hb);
        }
        page.setList(ls);
        model.addAttribute("page", page);
        model.addAttribute("keywords", keywords);
        return "search.jsp";
    }
}
