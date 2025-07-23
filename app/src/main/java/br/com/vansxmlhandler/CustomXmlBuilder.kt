package br.com.vansxmlhandler;

import java.io.IOException;

/**
 * XmlBuilder is used to write XML tags (open and close, and a few attributes)
 * to a StringBuilder. Here we have nothing to do with IO or SQL, just a fancy
 * StringBuilder.
 * 
 */

public class CustomXmlBuilder {
	public static final String DATA_BASE_NODO_NAME = "DATABASE";
	public static final String TABLE_NODO_NAME = "TABLE";
	public static final String ROW_NODO_NAME = "ROW";
	public static final String COL_NODO_NAME = "COL";
	public static final String NAME = "NAME";
	
	private static final String OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		
	private static final String CLOSE_WITH_TICK = "'>";
	
	private static final String DB_OPEN = "<" + DATA_BASE_NODO_NAME + " " + NAME + "='";
	private static final String DB_CLOSE = "</" + DATA_BASE_NODO_NAME + ">";
	
	private static final String TABLE_OPEN = "<" + TABLE_NODO_NAME + " " + NAME + "='";
	private static final String TABLE_CLOSE = "</" + TABLE_NODO_NAME + ">";
	
	private static final String ROW_OPEN = "<" + ROW_NODO_NAME + ">";
	private static final String ROW_CLOSE = "</" + ROW_NODO_NAME + ">";
	
	private static final String COL_OPEN = "<" + COL_NODO_NAME + " " + NAME + "='";
	private static final String COL_CLOSE = "</" + COL_NODO_NAME + ">";

	private final StringBuilder sb;

	public CustomXmlBuilder() throws IOException {
		sb = new StringBuilder();
	}

	void start(final String dbName) {
		sb.append(OPEN_XML_STANZA);
		sb.append(DB_OPEN + dbName + CLOSE_WITH_TICK);
	}

	String end() throws IOException {
		sb.append(DB_CLOSE);
		return sb.toString();
	}

	void openTable(final String tableName) {
		sb.append(TABLE_OPEN + tableName + CLOSE_WITH_TICK);
	}

	void closeTable() {
		sb.append(TABLE_CLOSE);
	}

	void openRow() {
		sb.append(ROW_OPEN);
	}

	void closeRow() {
		sb.append(ROW_CLOSE);
	}

	void addColumn(final String name, final String val) throws IOException {
		sb.append(COL_OPEN + name + CLOSE_WITH_TICK + val + COL_CLOSE);
	}
}
