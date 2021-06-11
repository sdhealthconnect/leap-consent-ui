package gov.hhs.onc.leap;


import com.vaadin.flow.i18n.I18NProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;
import static org.rapidpm.frp.matcher.Case.match;
import static org.rapidpm.frp.matcher.Case.matchCase;
import static org.rapidpm.frp.model.Result.success;

@Slf4j
public class VaadinI18NProvider implements I18NProvider {

    public VaadinI18NProvider() {
        log.info(VaadinI18NProvider.class.getSimpleName() + " was found..");
    }

    public static final String RESOURCE_BUNDLE_NAME = "vaadinapp";
    private static final Locale SPANISH = new Locale("es", "ES");
    private static final Locale SPANISH_NC = new Locale("es");
    private static final ResourceBundle RESOURCE_BUNDLE_EN = getBundle(RESOURCE_BUNDLE_NAME, ENGLISH);
    private static final ResourceBundle RESOURCE_BUNDLE_ES = getBundle(RESOURCE_BUNDLE_NAME, SPANISH);
    private static final ResourceBundle RESOURCE_BUNDLE_ES_NC = getBundle(RESOURCE_BUNDLE_NAME, SPANISH_NC);
    private static final ResourceBundle RESOURCE_BUNDLE_DE = getBundle(RESOURCE_BUNDLE_NAME, GERMAN);
    private static final List<Locale> providedLocales = unmodifiableList(asList(ENGLISH, SPANISH, GERMAN));


    public static Locale getLocale(String language){
        return providedLocales.stream().filter(l -> l.getDisplayLanguage().equals(language)).findFirst().orElse(null);
    }

    @Override
    public List<Locale> getProvidedLocales() {
        log.info("VaadinI18NProvider getProvidedLocales..");
        return providedLocales;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        log.trace("VaadinI18NProvider getTranslation.. key : " + key + " - " + locale);
        return match(
                matchCase(() -> success(RESOURCE_BUNDLE_EN)),
                matchCase(() -> GERMAN.equals(locale), () -> success(RESOURCE_BUNDLE_DE)),
                matchCase(() -> SPANISH.equals(locale) || SPANISH_NC.equals(locale), () -> success(RESOURCE_BUNDLE_ES)),
                matchCase(() -> ENGLISH.equals(locale), () -> success(RESOURCE_BUNDLE_EN))
        )
                .map(resourceBundle -> {
                    if (!resourceBundle.containsKey(key))
                        log.warn("missing ressource key (i18n) " + key);
                    return (resourceBundle.containsKey(key)) ? resourceBundle.getString(key) : key;
                })
                .getOrElse(() -> key + " - " + locale);
    }

}
