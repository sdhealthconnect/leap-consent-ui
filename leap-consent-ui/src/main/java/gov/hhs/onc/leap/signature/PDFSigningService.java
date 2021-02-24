package gov.hhs.onc.leap.signature;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;

/**
 * A PDF signing service.
 *
 * The PDF will be signed with a RSA digital certificate.
 *
 * @author sgroh@saperi.io
 */
@Service
public class PDFSigningService {

    private static final String KEY_STORE_TYPE = "jks";
    private final String keyStorePath;
    private final String keyStorePassword;
    private final String certificateAlias;
    private final String tsaUrl;
    private static final Logger log = LoggerFactory.getLogger(PDFSigningService.class);

    public PDFSigningService(@Value("${keystore.path}") String keyStorePath,
                             @Value("${keystore.password}") String keyStorePassword,
                             @Value("${keystore.certificate-alias}") String certificateAlias,
                             @Value("${timestamp-authority.url}") String tsaUrl) {
        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
        this.certificateAlias = certificateAlias;
        this.tsaUrl = tsaUrl;
    }

    public byte[] signPdf(byte[] pdfToSign) {
        try {
            KeyStore keyStore = this.getKeyStore();
            Signature signature = new Signature(keyStore, this.keyStorePassword.toCharArray(), certificateAlias, tsaUrl);
            //create temporary pdf file
            File pdfFile = File.createTempFile("pdf", "");
            //write bytes to created pdf file
            FileUtils.writeByteArrayToFile(pdfFile, pdfToSign);

            //create empty pdf file which will be signed
            File signedPdf = File.createTempFile("signedPdf", "");
            //sign pdfFile and write bytes to signedPdf
            this.signDetached(signature, pdfFile, signedPdf);

            byte[] signedPdfBytes = Files.readAllBytes(signedPdf.toPath());

            //remove temporary files
            pdfFile.deleteOnExit();
            signedPdf.deleteOnExit();

            return signedPdfBytes;
        } catch (NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | KeyStoreException e) {
            log.error("Cannot obtain proper KeyStore or Certificate", e);
        } catch (IOException e) {
            log.error("Cannot obtain proper file", e);
        }

        //if pdf cannot be signed, then return plain, not signed pdf
        return pdfToSign;
    }

    private KeyStore getKeyStore() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        File key = ResourceUtils.getFile(keyStorePath);
        keyStore.load(new FileInputStream(key), keyStorePassword.toCharArray());
        return keyStore;
    }

    private void signDetached(SignatureInterface signature, File inFile, File outFile) throws IOException {
        if (inFile == null || !inFile.exists()) {
            throw new FileNotFoundException("Document for signing does not exist");
        }

        try (FileOutputStream fos = new FileOutputStream(outFile);
             PDDocument doc = PDDocument.load(inFile)) {
            signDetached(signature, doc, fos);
        } catch (Exception e) {
            log.error("Was not possible to add Sign with a certificete the PDF document");
        }
    }

    private void signDetached(SignatureInterface signature, PDDocument document, OutputStream output) throws IOException {
        PDSignature pdSignature = new PDSignature();
        pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        pdSignature.setName(this.certificateAlias);
        pdSignature.setReason("Leap signed PDF");

        // the signing date, needed for valid signature
        pdSignature.setSignDate(Calendar.getInstance());
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null)
        {
            acroForm = new PDAcroForm(document);
            document.getDocumentCatalog().setAcroForm(acroForm);
        }
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        signatureField.setValue(pdSignature);
        signatureField.getWidgets().get(0).setPage(document.getPage(0));
        acroForm.getFields().add(signatureField);
        // page annotation, only needed if PDF/A
        document.getPage(0).getAnnotations().add(signatureField.getWidgets().get(0));
        document.getPage(0).getCOSObject().setNeedToBeUpdated(true);


        // register signature dictionary and sign interface
        document.addSignature(pdSignature, signature);

        // write incremental (only for signing purpose)
        // use saveIncremental to add signature, using plain save method may break up a document
        document.saveIncremental(output);
    }
}
