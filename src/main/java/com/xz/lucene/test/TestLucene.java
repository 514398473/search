package com.xz.lucene.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by xuz-d on 2017/2/13.
 */
public class TestLucene {
    private final String PATH = "F:\\data";
    private final String INDEX_PATH = "F:\\index";

    @Test
    public void createIndex() {
        try {
            Directory directory = FSDirectory.open(new File(INDEX_PATH));
            Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_4_9);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
            Collection<File> files = FileUtils.listFiles(new File(PATH), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File f : files) {
                Document document = new Document();
                document.add(new StringField("fileName", f.getName(), Field.Store.YES));
                document.add(new TextField("content", FileUtils.readFileToString(f), Field.Store.YES));
                document.add(new LongField("lastModif", f.lastModified(), Field.Store.YES));
                indexWriter.addDocument(document);
            }
            indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //wget -o /tmp/wget.log -P /root/data --no-parent --no-verbose -m -D www.bjsxt.com -N --convert-links --random-wait -A html,HTML http://www.bjsxt.com/
    @Test
    public void testSearch() {
        try {
            Directory directory = FSDirectory.open(new File(INDEX_PATH));
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_4_9);
            QueryParser queryParser = new QueryParser(Version.LUCENE_4_9, "content", analyzer);
            Query query = queryParser.parse("java");
            TopDocs search = indexSearcher.search(query, 10);
            ScoreDoc[] scoreDocs = search.scoreDocs;
            for (ScoreDoc s : scoreDocs) {
                Document document = indexReader.document(s.doc);
                System.out.println(document.get("title"));
//                System.out.println(document.get("lastModif"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}






