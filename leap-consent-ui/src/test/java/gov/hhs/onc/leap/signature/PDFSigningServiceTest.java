package gov.hhs.onc.leap.signature;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class PDFSigningServiceTest {

    private PDFSigningService PDFSigningService;

    @Value("${keystore.path}")
    private String keyStorePath;

    @Value("${keystore.password}")
    private String keyStorePassword;

    @Value("${keystore.certificate-alias}")
    private String certificateAlias;

    @Value("${timestamp-authority.url}")
    private String tsaURL;


    @BeforeEach
    void setup(){
        PDFSigningService = new PDFSigningService(keyStorePath, keyStorePassword, certificateAlias, tsaURL);
    }

    @Test
    void signPdf() throws IOException {
        byte[] pdf = this.generatePdf();
        byte[] signedPdf = PDFSigningService.signPdf(pdf);
        PDDocument pdfDoc = createPDFDocFromBA(pdf);
        PDDocument pdfSignedDoc = createPDFDocFromBA(signedPdf);
        Assert.assertNotEquals(pdf.length, signedPdf.length);
        //Validate that original PFD does not contains a signature added.
        List<PDSignatureField> signatures = pdfDoc.getSignatureFields();
        Assert.assertEquals(0, signatures.size());
        //Validate that original PFD contains a signature added.
        signatures = pdfSignedDoc.getSignatureFields();
        Assert.assertEquals(1, signatures.size());
        PDSignature sig = signatures.get(0).getSignature();
        String name = sig.getName();
        Assert.assertEquals("leap_certificate", name);
        String reason = sig.getReason();
        Assert.assertEquals("Leap signed PDF", reason);
    }

    private  PDDocument createPDFDocFromBA(byte[] pdf) throws IOException {
        PDDocument pdDoc = PDDocument.load(pdf);
        return pdDoc;
    }

    private byte[] generatePdf() throws IOException {
        PDDocument pdDocument = new PDDocument();
        PDPage pdPage = new PDPage();
        PDFont pdfFont = PDType1Font.HELVETICA_BOLD;
        int fontSize = 28;

        try (PDPageContentStream contentStream = new PDPageContentStream(pdDocument, pdPage, true, true, true)) {
            contentStream.setFont(pdfFont, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset(200, 685);
            contentStream.showText("PDF Example");
            contentStream.endText();
        }

        pdDocument.addPage(pdPage);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdDocument.save(byteArrayOutputStream);
        pdDocument.close();

        return byteArrayOutputStream.toByteArray();
    }

}