package com.spintly.base.utilities;

import com.spintly.base.managers.ResultManager;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PdfHelper {

    public String[] extractPDF(String filename) throws IOException {
        String[] content = new String[100];
        try {
            PDDocument document = Loader.loadPDF(new File(filename));
            String text = "";

            if (!document.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                text = stripper.getText(document);
                content = text.split("\r\n");

                System.out.println("PDF Content : " + text);
            }
            document.close();
            new File(filename).delete();
            File dir = new File(Paths.get("").toAbsolutePath() + File.separator + "Downloads");
//            FileUtils.cleanDirectory(dir);
        }
        catch(Exception e)
        {
            ResultManager.error("PDF file should be read", "PDF file not read :"+ filename, e.getMessage(), true);
        }
        return content;
    }
}
