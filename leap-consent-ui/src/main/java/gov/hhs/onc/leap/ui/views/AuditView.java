package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.ConsentLog;
import gov.hhs.onc.leap.backend.TestData;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.navigation.BasicDivider;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.IconSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;

@PageTitle("My Record Disclosures")
@Route(value = "auditview", layout = MainLayout.class)
public class AuditView extends ViewFrame {

    private Button logButton;
    private Button chartButton;
    private Grid<ConsentLog> grid;
    private ListDataProvider<ConsentLog> dataProvider;
    private FlexBoxLayout logLayout;
    private FlexBoxLayout chartLayout;

    public AuditView() {
        setId("auditview");
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {

        logLayout = new FlexBoxLayout(createHeader(VaadinIcon.FILE, "Activity Logs"),createGrid());
        logLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        logLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        logLayout.setHeightFull();
        logLayout.setBackgroundColor("white");
        logLayout.setShadow(Shadow.S);
        logLayout.setBorderRadius(BorderRadius.S);
        logLayout.getStyle().set("margin-bottom", "10px");
        logLayout.getStyle().set("margin-right", "10px");
        logLayout.getStyle().set("margin-left", "10px");
        logLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        chartLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Activity Logs"),createChart());
        chartLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        chartLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        chartLayout.setHeightFull();
        chartLayout.setBackgroundColor("white");
        chartLayout.setShadow(Shadow.S);
        chartLayout.setBorderRadius(BorderRadius.S);
        chartLayout.getStyle().set("margin-bottom", "10px");
        chartLayout.getStyle().set("margin-right", "10px");
        chartLayout.getStyle().set("margin-left", "10px");
        chartLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        chartLayout.setVisible(false);

        FlexBoxLayout content = new FlexBoxLayout(logLayout, chartLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private Component createGrid() {
        dataProvider = DataProvider.ofCollection(TestData.getConsentLogs());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();
        return grid;
    }

    private Component createChart() {
        Chart chart = new Chart(ChartType.SCATTER);
        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("Record Access History - Visualization");

        XAxis xAxis = configuration.getxAxis();
        xAxis.setTitle("Date");
        xAxis.setStartOnTick(true);
        xAxis.setEndOnTick(true);
        xAxis.setShowLastLabel(true);

        Legend legend = configuration.getLegend();
        legend.setLayout(LayoutDirection.VERTICAL);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setVerticalAlign(VerticalAlign.TOP);
        legend.setX(100);
        legend.setY(70);
        legend.setFloating(true);

        PlotOptionsScatter plotOptionsScatter = new PlotOptionsScatter();
        SeriesTooltip scatterTooltip = plotOptionsScatter.getTooltip();
        scatterTooltip.setHeaderFormat("<b>{series.name}</b><br>\",\"pointFormat\":\"{point.x} cm, {point.y} kg");

        DataSeries permitSeries = new DataSeries();
        permitSeries.setName("Permit");

        PlotOptionsScatter permitPlotOptions = new PlotOptionsScatter();
        permitPlotOptions.setClassName("permitSeries");
        permitSeries.setPlotOptions(permitPlotOptions);

        DataSeries denySeries = new DataSeries();
        denySeries.setName("Deny");

        PlotOptionsScatter denyPlotOptions = new PlotOptionsScatter();
        denyPlotOptions.setClassName("denySeries");
        denySeries.setPlotOptions(denyPlotOptions);

        DataSeries noConsentSeries = new DataSeries();
        noConsentSeries.setName("No Consent");

        PlotOptionsScatter noConsentPlotOptions = new PlotOptionsScatter();
        denyPlotOptions.setClassName("noConsentSeries");
        noConsentSeries.setPlotOptions(noConsentPlotOptions);

        // todo populate data series

        configuration.addSeries(permitSeries);
        configuration.addSeries(denySeries);
        configuration.addSeries(noConsentSeries);

        return chart;

    }

    private Component getFooter() {
        logButton = new Button("Logs", new Icon(VaadinIcon.FILE));
        logButton.addClickListener(event -> {
            logLayout.setVisible(true);
            chartLayout.setVisible(false);
        });
        chartButton = new Button("Chart", new Icon(VaadinIcon.CHART));
        chartButton.addClickListener(event -> {
            logLayout.setVisible(false);
            chartLayout.setVisible(true);
        });
        HorizontalLayout footer = new HorizontalLayout(logButton, chartButton);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.setSpacing(true);
        return footer;
    }

    private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
        FlexBoxLayout header = new FlexBoxLayout(
                UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
                UIUtils.createH3Label(title));
        header.getStyle().set("background-color", "#5F9EA0");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(Right.L);
        return header;
    }

}
