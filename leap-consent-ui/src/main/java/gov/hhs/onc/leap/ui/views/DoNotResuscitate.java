package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import org.vaadin.alejandro.PdfBrowserViewer;


@PageTitle("Do Not Resuscitate (DNR)")
@Route(value = "dnrview", layout = MainLayout.class)
public class DoNotResuscitate extends ViewFrame {

    private PDFDocumentHandler pdfHandler;

    public DoNotResuscitate() {
        setId("dnrview");
        pdfHandler = new PDFDocumentHandler();
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        StreamResource streamResource = pdfHandler.retrievePDFForm("DNR");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("95%");
        viewer.setWidth("840px");

        Component buttonPanel = createSignaturePanel();

        FlexBoxLayout content = new FlexBoxLayout(viewer, buttonPanel);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private Component createSignaturePanel() {
        Button signature = new Button("Sign & Submit Form");
        signature.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.PENCIL));

        Button witnessSignature = new Button("Witness Signature");
        witnessSignature.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.PENCIL));

        Button uploadExisting = new Button("Upload a DNR");
        uploadExisting.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD));

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("840px");
        layout.setHeight("5%");
        layout.setMargin(true);
        layout.setPadding(false);

        layout.add(signature, uploadExisting);

        return layout;
    }
}
