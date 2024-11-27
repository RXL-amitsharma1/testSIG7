package com.rxlogix.dynamicReports.charts.docx

import groovy.xml.XmlUtil
import net.sf.jasperreports.engine.export.zip.ExportZipEntry

/**
 * Created by gologuzov on 07.08.17.
 */
class ContentTypesZipEntry implements ExportZipEntry {
    private String name

    def contentTypes = new XmlParser().parseText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n" +
            "  <Default Extension=\"gif\" ContentType=\"image/gif\"/>\n" +
            "  <Default Extension=\"jpeg\" ContentType=\"image/jpeg\"/>\n" +
            "  <Default Extension=\"png\" ContentType=\"image/png\"/>\n" +
            "  <Default Extension=\"tiff\" ContentType=\"image/tiff\"/>\n" +
            "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n" +
            "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n" +
            "  <Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>\n" +
            "  <Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>\n" +
            "  <Override PartName=\"/word/settings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml\"/>" +
            "</Types>\n")

    ContentTypesZipEntry(String name) {
        this.name = name
    }

    void addContentType(String nodeStr) {
        def fragmentToAdd = new XmlParser().parseText(nodeStr)
        contentTypes.append(fragmentToAdd)
    }

    @Override
    String getName() {
        return name
    }

    @Override
    Writer getWriter() {
        return null
    }

    @Override
    OutputStream getOutputStream() {
        return null
    }

    @Override
    void writeData(OutputStream outputStream) throws IOException {
        XmlUtil.serialize(contentTypes, outputStream)
    }

    @Override
    void dispose() {

    }
}