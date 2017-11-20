package com.ge.predix.pae.cassandra.spring.cv.util;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * The Enum LuceneUtil.
 *
 * @author Thomas Thinh PHAM 502640065
 */
public enum LuceneUtil {

	/** The instance. */
	INSTANCE;

	/** The directory. */
	private Directory directory;

	/** The index writer. */
	private IndexWriter indexWriter;

	/** The analyzer. */
	private Analyzer analyzer;

	/**
	 * Gets the directory.
	 *
	 * @return the directory
	 */
	public Directory getDirectory() {
		return directory;
	}

	/**
	 * Gets the analyzer.
	 *
	 * @return the analyzer
	 */
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Gets the index writer.
	 *
	 * @return the index writer
	 */
	public IndexWriter getIndexWriter() {
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
				Version.LUCENE_46, analyzer);

		try {
			indexWriter = new IndexWriter(directory, indexWriterConfig);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return indexWriter;
	}

	{
		analyzer = new StandardAnalyzer(Version.LUCENE_46);
		directory = new RAMDirectory();
		Directory hd = getDirecotoryHD();
		backup(hd, directory);

	}

	/**
	 * Gets the direcotory hd.
	 *
	 * @return the direcotory hd
	 */
	private Directory getDirecotoryHD() {
		File luceneFile = new File(System.getProperty("user.home").concat(
				"/lucene/"));
		if (!luceneFile.exists()) {
			luceneFile.mkdir();
		}
		File file = new File(System.getProperty("user.home").concat(
				"/lucene/resume/"));
		if (!file.exists()) {
			file.mkdir();
		}
		try {
			return FSDirectory.open(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the memory to hd.
	 *
	 * @return the memory to hd
	 */
	public void getMemoryToHD() {
		Directory hd = getDirecotoryHD();
		backup(directory, hd);
	}

	/**
	 * Backup.
	 *
	 * @param fromDiretory
	 *            the from diretory
	 * @param toDiretory
	 *            the to diretory
	 */
	private void backup(Directory fromDiretory, Directory toDiretory) {
		try {
			for (String file : fromDiretory.listAll()) {
				fromDiretory.copy(toDiretory, file, file, IOContext.DEFAULT);
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
}
