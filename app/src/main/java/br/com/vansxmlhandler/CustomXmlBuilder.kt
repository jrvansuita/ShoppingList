package br.com.vansxmlhandler

import android.database.Cursor
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import br.com.vansxmlhandler.CustomXmlBuilder

class CustomXmlBuilder {
    companion object {
        const val DATA_BASE_NODO_NAME = "DATABASE"
        const val TABLE_NODO_NAME = "TABLE"
        const val ROW_NODO_NAME = "ROW"
        const val COL_NODO_NAME = "COL"
        const val NAME = "NAME"
    }

    private val OPEN_XML_STANZA = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    private val CLOSE_WITH_TICK = "'>"
    private val DB_OPEN = "<$DATA_BASE_NODO_NAME $NAME='"
    private val DB_CLOSE = "</$DATA_BASE_NODO_NAME>"
    private val TABLE_OPEN = "<$TABLE_NODO_NAME $NAME='"
    private val TABLE_CLOSE = "</$TABLE_NODO_NAME>"
    private val ROW_OPEN = "<$ROW_NODO_NAME>"
    private val ROW_CLOSE = "</$ROW_NODO_NAME>"
    private val COL_OPEN = "<$COL_NODO_NAME $NAME='"
    private val COL_CLOSE = "</$COL_NODO_NAME>"

    private val sb = StringBuilder()

    fun start(dbName: String) {
        sb.append(OPEN_XML_STANZA)
        sb.append("$DB_OPEN$dbName$CLOSE_WITH_TICK")
    }

    fun end(): String {
        sb.append(DB_CLOSE)
        return sb.toString()
    }

    fun openTable(tableName: String) {
        sb.append("$TABLE_OPEN$tableName$CLOSE_WITH_TICK")
    }

    fun closeTable() {
        sb.append(TABLE_CLOSE)
    }

    fun openRow() {
        sb.append(ROW_OPEN)
    }

    fun closeRow() {
        sb.append(ROW_CLOSE)
    }

    fun addColumn(name: String, value: String?) {
        sb.append("$COL_OPEN$name$CLOSE_WITH_TICK${value ?: ""}$COL_CLOSE")
    }



}