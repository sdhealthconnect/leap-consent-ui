package gov.hhs.onc.leap.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.lumo.Lumo;
import gov.hhs.onc.leap.backend.ConsentDecorator;
import gov.hhs.onc.leap.backend.ConsentUserService;
import gov.hhs.onc.leap.backend.DBConsentDecorator;
import gov.hhs.onc.leap.backend.TestData;
import gov.hhs.onc.leap.backend.fhir.FhirConsentDecorator;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRPatient;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.security.model.User;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.navigation.bar.AppBar;
import gov.hhs.onc.leap.ui.components.navigation.bar.TabBar;
import gov.hhs.onc.leap.ui.components.navigation.drawer.NaviDrawer;
import gov.hhs.onc.leap.ui.components.navigation.drawer.NaviItem;
import gov.hhs.onc.leap.ui.components.navigation.drawer.NaviMenu;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.Display;
import gov.hhs.onc.leap.ui.util.css.Overflow;
import gov.hhs.onc.leap.ui.views.*;
import gov.hhs.onc.leap.ui.views.acorn.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@CssImport(value = "./styles/components/charts.css", themeFor = "vaadin-chart", include = "vaadin-chart-default-theme")
@CssImport(value = "./styles/components/floating-action-button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/components/grid.css", themeFor = "vaadin-grid")
@CssImport("./styles/lumo/border-radius.css")
@CssImport("./styles/lumo/icon-size.css")
@CssImport("./styles/lumo/margin.css")
@CssImport("./styles/lumo/padding.css")
@CssImport("./styles/lumo/shadow.css")
@CssImport("./styles/lumo/spacing.css")
@CssImport("./styles/lumo/typography.css")
@CssImport("./styles/misc/box-shadow-borders.css")
@CssImport(value = "./styles/styles.css", include = "lumo-badge")
@JsModule("@vaadin/vaadin-lumo-styles/badge")
public class MainLayout extends FlexBoxLayout
		implements RouterLayout, AfterNavigationObserver {

	private static final Logger log = LoggerFactory.getLogger(MainLayout.class);
	private static final String CLASS_NAME = "root";

	private Div appHeaderOuter;

	private FlexBoxLayout row;
	private NaviDrawer naviDrawer;
	private FlexBoxLayout column;

	private Div appHeaderInner;
	private Main viewContainer;
	private Div appFooterInner;

	private Div appFooterOuter;

	private TabBar tabBar;
	private boolean navigationTabs = false;
	private AppBar appBar;

	private ConsentSession consentSession;

	private FHIRPatient fhirPatient;
	private ConsentUserService consentUserService;



	public MainLayout(@Autowired FHIRPatient fhirPatient, @Autowired ConsentUserService consentUserService) {
		this.fhirPatient = fhirPatient;
		this.consentUserService = consentUserService;
		VaadinSession.getCurrent()
				.setErrorHandler((ErrorHandler) errorEvent -> {
					log.error("Uncaught UI exception",
							errorEvent.getThrowable());
					Notification.show(
							"We are sorry, but an internal error occurred");
				});

		consentSession = TestData.getConsentSession();
		VaadinSession.getCurrent().setAttribute("consentSession", consentSession);

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		//Clearing the password to not have this information in the bean
		user.setPassword("");
		consentSession.setConsentUser(new ConsentUser()); // empty object to be fullfilled by decorators
		consentSession.getConsentUser().setUser(user);
		ConsentDecorator consentDecorator = new DBConsentDecorator(consentSession, consentUserService);
		consentDecorator.decorate();
		consentDecorator = new FhirConsentDecorator(consentSession, fhirPatient);
		consentDecorator.decorate();

		UIUtils.setLanguage(VaadinRequest.getCurrent(), UI.getCurrent().getSession());

		addClassName(CLASS_NAME);
		setFlexDirection(FlexDirection.COLUMN);
		setSizeFull();

		// Initialise the UI building blocks
		initStructure();

		// Populate the navigation drawer
		initNaviItems();

		// Configure the headers and footers (optional)
		initHeadersAndFooters();
	}

	/**
	 * Initialise the required components and containers.
	 */
	private void initStructure() {
		naviDrawer = new NaviDrawer();

		viewContainer = new Main();
		viewContainer.addClassName(CLASS_NAME + "__view-container");
		UIUtils.setDisplay(Display.FLEX, viewContainer);
		UIUtils.setFlexGrow(1, viewContainer);
		UIUtils.setOverflow(Overflow.HIDDEN, viewContainer);

		column = new FlexBoxLayout(viewContainer);
		column.addClassName(CLASS_NAME + "__column");
		column.setFlexDirection(FlexDirection.COLUMN);
		column.setFlexGrow(1, viewContainer);
		column.setOverflow(Overflow.HIDDEN);

		row = new FlexBoxLayout(naviDrawer, column);
		row.addClassName(CLASS_NAME + "__row");
		row.setFlexGrow(1, column);
		row.setOverflow(Overflow.HIDDEN);
		add(row);
		setFlexGrow(1, row);
	}

	/**
	 * Initialise the navigation items.
	 */
	private void initNaviItems() {
		NaviMenu menu = naviDrawer.getMenu();
		menu.addNaviItem(VaadinIcon.HOME, getTranslation("mainLayout-menu-home"), Home.class);
		menu.addNaviItem(VaadinIcon.FAMILY, "My Social Needs", AcornHome.class);
		menu.addNaviItem(VaadinIcon.CHECK_SQUARE, "MED Reconciliation", MedReconciliation.class);
		menu.addNaviItem(VaadinIcon.RECORDS, getTranslation("mainLayout-menu-my_consent_documents"), ConsentDocumentsView.class);
		menu.addNaviItem(VaadinIcon.COGS, getTranslation("mainLayout-menu-analyze_my_data"), AnalyzeRecordView.class);
		menu.addNaviItem(VaadinIcon.SHARE, getTranslation("mainLayout-menu-share_my_data"), SharePatientDataView.class);

		NaviItem advDirective = menu.addNaviItem(VaadinIcon.EDIT, getTranslation("mainLayout-menu-my_directives"), AdvancedDirectiveView.class);
		menu.addNaviItem(advDirective, getTranslation("mainLayout-menu-living_will"), LivingWill.class);
		menu.addNaviItem(advDirective, getTranslation("mainLayout-menu-healt_care_POA"), HealthcarePowerOfAttorney.class);
		menu.addNaviItem(advDirective, getTranslation("mainLayout-menu-mental_healt_care_POA"), MentalHealthPowerOfAttorney.class);
		menu.addNaviItem(advDirective, getTranslation("mainLayout-menu-do_not_resuscitate"), DoNotResuscitate.class);
		advDirective.setSubItemsVisible(false);

		menu.addNaviItem(VaadinIcon.CLIPBOARD, getTranslation("mainLayout-menu-portable_medical_order"), PortableMedicalOrder.class);

		menu.addNaviItem(VaadinIcon.LIST, getTranslation("mainLayout-menu-activity_logs"), AuditView.class );
		menu.addNaviItem(VaadinIcon.ENVELOPES, getTranslation("mainLayout-menu-notifications"), NotificationView.class);
		/*
		NaviItem covid = menu.addNaviItem(VaadinIcon.GLOBE, "COVID-19",
				COVID.class);
		menu.addNaviItem(covid, "Release Allergy Intolerances", ReleaseAllergyIntoleranceView.class);
		menu.addNaviItem(covid, "Authorize Treatment", COVIDAuthorizeTreatment.class);
		menu.addNaviItem(covid, "Release Immunization Record", COVIDReleaseMyRecords.class);
		menu.addNaviItem(covid, "Participate In Outcome Research", COVIDOutcomeResearch.class);
		menu.addNaviItem(covid, "Provider Verify Consent",  COVIDProviderVerifyConsent.class);
		menu.addNaviItem(covid, "Provider Attestation", COVIDProviderAttestation.class);

		 */
		RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
		if (!routeConfiguration.isRouteRegistered(UserPreferencesView.class)) {
			routeConfiguration.setAnnotatedRoute(UserPreferencesView.class);
		}
	}

	/**
	 * Configure the app's inner and outer headers and footers.
	 */
	private void initHeadersAndFooters() {
		// setAppHeaderOuter();
		// setAppFooterInner();
		// setAppFooterOuter();

		// Default inner header setup:
		// - When using tabbed navigation the view title, user avatar and main menu button will appear in the TabBar.
		// - When tabbed navigation is turned off they appear in the AppBar.

		appBar = new AppBar("");

		// Tabbed navigation
		if (navigationTabs) {
			tabBar = new TabBar();
			UIUtils.setTheme(Lumo.DARK, tabBar);

			// Shift-click to add a new tab
			for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
				item.addClickListener(e -> {
					if (e.getButton() == 0 && e.isShiftKey()) {
						tabBar.setSelectedTab(tabBar.addClosableTab(item.getText(), item.getNavigationTarget()));
					}
				});
			}
			appBar.getAvatar().setVisible(false);
			setAppHeaderInner(tabBar, appBar);

			// Default navigation
		} else {
			UIUtils.setTheme(Lumo.DARK, appBar);
			setAppHeaderInner(appBar);
		}
	}

	private void setAppHeaderOuter(Component... components) {
		if (appHeaderOuter == null) {
			appHeaderOuter = new Div();
			appHeaderOuter.addClassName("app-header-outer");
			getElement().insertChild(0, appHeaderOuter.getElement());
		}
		appHeaderOuter.removeAll();
		appHeaderOuter.add(components);
	}

	private void setAppHeaderInner(Component... components) {
		if (appHeaderInner == null) {
			appHeaderInner = new Div();
			appHeaderInner.addClassName("app-header-inner");
			column.getElement().insertChild(0, appHeaderInner.getElement());
		}
		appHeaderInner.removeAll();
		appHeaderInner.add(components);
	}

	private void setAppFooterInner(Component... components) {
		if (appFooterInner == null) {
			appFooterInner = new Div();
			appFooterInner.addClassName("app-footer-inner");
			column.getElement().insertChild(column.getElement().getChildCount(),
					appFooterInner.getElement());
		}
		appFooterInner.removeAll();
		appFooterInner.add(components);
	}

	private void setAppFooterOuter(Component... components) {
		if (appFooterOuter == null) {
			appFooterOuter = new Div();
			appFooterOuter.addClassName("app-footer-outer");
			getElement().insertChild(getElement().getChildCount(),
					appFooterOuter.getElement());
		}
		appFooterOuter.removeAll();
		appFooterOuter.add(components);
	}

	@Override
	public void showRouterLayoutContent(HasElement content) {
		this.viewContainer.getElement().appendChild(content.getElement());
	}

	public NaviDrawer getNaviDrawer() {
		return naviDrawer;
	}

	public static MainLayout get() {
		return (MainLayout) UI.getCurrent().getChildren()
				.filter(component -> component.getClass() == MainLayout.class)
				.findFirst().get();
	}

	public AppBar getAppBar() {
		return appBar;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		if (navigationTabs) {
			afterNavigationWithTabs(event);
		} else {
			afterNavigationWithoutTabs(event);
		}
	}

	private void afterNavigationWithTabs(AfterNavigationEvent e) {
		NaviItem active = getActiveItem(e);
		if (active == null) {
			if (tabBar.getTabCount() == 0) {
				tabBar.addClosableTab("", Home.class);
			}
		} else {
			if (tabBar.getTabCount() > 0) {
				tabBar.updateSelectedTab(active.getText(),
						active.getNavigationTarget());
			} else {
				tabBar.addClosableTab(active.getText(),
						active.getNavigationTarget());
			}
		}
		appBar.getMenuIcon().setVisible(false);
	}

	private NaviItem getActiveItem(AfterNavigationEvent e) {
		for (NaviItem item : naviDrawer.getMenu().getNaviItems()) {
			if (item.isHighlighted(e)) {
				return item;
			}
		}
		return null;
	}

	private void afterNavigationWithoutTabs(AfterNavigationEvent e) {
		NaviItem active = getActiveItem(e);
		if (active != null) {
			getAppBar().setTitle(active.getText());
		}
	}

}
