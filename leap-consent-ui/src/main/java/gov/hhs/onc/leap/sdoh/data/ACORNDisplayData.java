package gov.hhs.onc.leap.sdoh.data;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import gov.hhs.onc.leap.sdoh.model.QuestionnaireItem;
import gov.hhs.onc.leap.sdoh.model.QuestionnaireSection;
import org.hl7.fhir.r4.model.*;
import org.mozilla.javascript.NativeArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class ACORNDisplayData {
    //for demo testing purposes
    private QuestionnaireSection livingSituation1;
    private QuestionnaireSection livingSituation2;
    private QuestionnaireSection food1;
    private QuestionnaireSection food2;
    private QuestionnaireSection transportation1;
    private QuestionnaireSection utilities1;
    private QuestionnaireSection safety1;
    private QuestionnaireSection safety2;
    private QuestionnaireSection safety3;
    private QuestionnaireSection safety4;
    private QuestionnaireSection financialStrain1;
    private QuestionnaireSection employment1;
    private QuestionnaireSection familyCommunitySupport1;
    private QuestionnaireSection familyCommunitySupport2;
    private QuestionnaireSection education1;
    private QuestionnaireSection education2;
    private QuestionnaireSection legal;

    private String foodSecurityInfo;
    private String housingInsecurityInfo;
    private String utilityNeedsInfo;
    private String transportationAccessInfo;
    private String personalSafetyInfo;
    private String socialSupportInfo;
    private String employmentAndEducationInfo;
    private String legalSupportInfo;

    private List<Questionnaire.QuestionnaireItemComponent> questionList = new ArrayList<>();


    public QuestionnaireSection getLivingSituation1() {
        livingSituation1 = new QuestionnaireSection();
        livingSituation1.setTitle("Housing Insecurity");
        livingSituation1.setLinkId("1");
        livingSituation1.setQuestion("<p><b>1.  What is your living situation today?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("1.1","I have a steady place to live", null, "boolean", 0 );
        QuestionnaireItem item2 = new QuestionnaireItem("1.2","I have a place to live today, but I am worried about losing it in the future", null, "boolean", 0 );
        QuestionnaireItem item3 = new QuestionnaireItem("1.3","I do not have a steady place to live (I am temporarily staying with others, in a hotel, in a\n" +
                "shelter, living outside on the street, on a beach, in a car, abandoned building, bus or\n" +
                "train station, or in a park)", null, "boolean", 0 );
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        livingSituation1.setItemList(itemList);
        return livingSituation1;
    }

    public void setLivingSituation1(QuestionnaireSection livingSituation1) {
        this.livingSituation1 = livingSituation1;
    }

    public QuestionnaireSection getLivingSituation2() {
        livingSituation2 = new QuestionnaireSection();
        livingSituation2.setTitle("Housing Insecurity");
        livingSituation2.setLinkId("2");
        livingSituation2.setQuestion("<p><b>2. Think about the place you live. Do you have problems with any of the following? </b>" +
                "CHOOSE ALL THAT APPLY</p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("2.1","Pests such as bugs, ants, or mice", null, "boolean", 0 );
        QuestionnaireItem item2 = new QuestionnaireItem("2.2","Mold", null, "boolean", 0 );
        QuestionnaireItem item3 = new QuestionnaireItem("2.3","Lead paint or pipes", null, "boolean", 0 );
        QuestionnaireItem item4 = new QuestionnaireItem("2.4","Lack of heat", null, "boolean", 0 );
        QuestionnaireItem item5 = new QuestionnaireItem("2.5","Oven or stove not working", null, "boolean", 0 );
        QuestionnaireItem item6 = new QuestionnaireItem("2.6","Smoke detectors missing or not working", null, "boolean", 0 );
        QuestionnaireItem item7 = new QuestionnaireItem("2.7","Water leaks", null, "boolean", 0 );
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        itemList.add(item6);
        itemList.add(item7);
        livingSituation2.setItemList(itemList);
        return livingSituation2;
    }

    public void setLivingSituation2(QuestionnaireSection livingSituation2) {
        this.livingSituation2 = livingSituation2;
    }

    public QuestionnaireSection getFood1() {
        food1 = new QuestionnaireSection();
        food1.setTitle("Food Security");
        food1.setLinkId("3");
        food1.setQuestion("<p><b>3. Within the past 12 months, you worried that your food would run out before you got money to buy more.</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("3.1", "Often true", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("3.2", "Sometimes true", null, "boolean", 1);
        QuestionnaireItem item3 = new QuestionnaireItem("3.3", "Never true", null, "boolean", 0);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        food1.setItemList(itemList);
        return food1;
    }



    public void setFood1(QuestionnaireSection food1) {
        this.food1 = food1;
    }

    public QuestionnaireSection getFood2() {
        food2 = new QuestionnaireSection();
        food2.setTitle("Food Security");
        food2.setLinkId("4");
        food2.setQuestion("<p><b>4. Within the past 12 months, the food you bought just didn't last and you didn't have money to get more.</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("4.1", "Often true", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("4.2", "Sometimes true", null, "boolean", 1);
        QuestionnaireItem item3 = new QuestionnaireItem("4.3", "Never true", null, "boolean", 0);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        food2.setItemList(itemList);
        return food2;
    }

    public void setFood2(QuestionnaireSection food2) {
        this.food2 = food2;
    }

    public QuestionnaireSection getTransportation1() {
        transportation1 = new QuestionnaireSection();
        transportation1.setLinkId("5");
        transportation1.setTitle("Transportation Access");
        transportation1.setQuestion("<p><b>5. In the past 12 months, has lack of reliable transportation kept you from medical " +
                "appointments, meetings, work or from getting things needed for daily living?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("5.1", "Yes", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("5.2", "No", null, "boolean", 0);
        itemList.add(item1);
        itemList.add(item2);
        transportation1.setItemList(itemList);
        return transportation1;
    }

    public void setTransportation1(QuestionnaireSection transportation1) {
        this.transportation1 = transportation1;
    }

    public QuestionnaireSection getUtilities1() {
        utilities1 = new QuestionnaireSection();
        utilities1.setLinkId("6");
        utilities1.setTitle("Utility Needs");
        utilities1.setQuestion("<p><b>6. In the past 12 months has the electric, gas, oil, or water company threatened to shut " +
                "off services in your home?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("6.1", "Yes", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("6.2", "No", null, "boolean", 0);
        QuestionnaireItem item3 = new QuestionnaireItem("6.3", "Already shut off", null, "boolean", 2);

        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        utilities1.setItemList(itemList);
        return utilities1;
    }

    public void setUtilities1(QuestionnaireSection utilities1) {
        this.utilities1 = utilities1;
    }

    public QuestionnaireSection getSafety1() {
        safety1 = new QuestionnaireSection();
        safety1.setLinkId("7");
        safety1.setTitle("Personal Safety");
        safety1.setQuestion("<p><b>7. How often does anyone, including family and friends, physically hurt you?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("7.1", "Never", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("7.2", "Rarely", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("7.3", "Sometimes", null, "boolean", 3);
        QuestionnaireItem item4 = new QuestionnaireItem("7.4", "Fairly often", null, "boolean", 4);
        QuestionnaireItem item5 = new QuestionnaireItem("7.5", "Frequently", null, "boolean", 5);


        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        safety1.setItemList(itemList);
        return safety1;
    }

    public void setSafety1(QuestionnaireSection safety1) {
        this.safety1 = safety1;
    }

    public QuestionnaireSection getSafety2() {
        safety2 = new QuestionnaireSection();
        safety2.setLinkId("8");
        safety2.setTitle("Personal Safety");
        safety2.setQuestion("<p><b>8. How often does anyone, including family and friends, insult or talk down to you?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("8.1", "Never", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("8.2", "Rarely", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("8.3", "Sometimes", null, "boolean", 3);
        QuestionnaireItem item4 = new QuestionnaireItem("8.4", "Fairly often", null, "boolean", 4);
        QuestionnaireItem item5 = new QuestionnaireItem("8.5", "Frequently", null, "boolean", 5);


        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        safety2.setItemList(itemList);
        return safety2;
    }

    public void setSafety2(QuestionnaireSection safety2) {
        this.safety2 = safety2;
    }

    public QuestionnaireSection getSafety3() {
        safety3 = new QuestionnaireSection();
        safety3.setLinkId("9");
        safety3.setTitle("Personal Safety");
        safety3.setQuestion("<p><b>9. How often does anyone, including family and friends, threaten you with harm?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("9.1", "Never", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("9.2", "Rarely", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("9.3", "Sometimes", null, "boolean", 3);
        QuestionnaireItem item4 = new QuestionnaireItem("9.4", "Fairly often", null, "boolean", 4);
        QuestionnaireItem item5 = new QuestionnaireItem("9.5", "Frequently", null, "boolean", 5);


        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        safety3.setItemList(itemList);
        return safety3;
    }

    public void setSafety3(QuestionnaireSection safety3) {
        this.safety3 = safety3;
    }

    public QuestionnaireSection getSafety4() {
        safety4 = new QuestionnaireSection();
        safety4.setLinkId("10");
        safety4.setTitle("Personal Safety");
        safety4.setQuestion("<p><b>10. How often does anyone, including family and friends, scream or curse at you?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("10.1", "Never", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("10.2", "Rarely", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("10.3", "Sometimes", null, "boolean", 3);
        QuestionnaireItem item4 = new QuestionnaireItem("10.4", "Fairly often", null, "boolean", 4);
        QuestionnaireItem item5 = new QuestionnaireItem("10.5", "Frequently", null, "boolean", 5);


        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        safety4.setItemList(itemList);
        return safety4;
    }

    public void setSafety4(QuestionnaireSection safety4) {
        this.safety4 = safety4;
    }

    public QuestionnaireSection getFinancialStrain1() {
        financialStrain1 = new QuestionnaireSection();
        financialStrain1.setLinkId("11");
        financialStrain1.setTitle("Employment and Education");
        financialStrain1.setQuestion("<p><b>11. How hard is it for you to pay for the very basics like food, housing, medical care, and " +
                "heating? Would you say it is:</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("11.1", "Very hard", null, "boolean", 3);
        QuestionnaireItem item2 = new QuestionnaireItem("11.2", "Somewhat hard", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("11.3", "Not hard at all", null, "boolean", 1);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        financialStrain1.setItemList(itemList);
        return financialStrain1;
    }

    public void setFinancialStrain1(QuestionnaireSection financialStrain1) {
        this.financialStrain1 = financialStrain1;
    }

    public QuestionnaireSection getFamilyCommunitySupport1() {
        familyCommunitySupport1 = new QuestionnaireSection();
        familyCommunitySupport1.setLinkId("15");
        familyCommunitySupport1.setTitle("Social Support");
        familyCommunitySupport1.setQuestion("<p><b>15. If for any reason you need help with day-to-day activities such as bathing, preparing " +
                "meals, shopping, managing finances, etc., do you get the help you need?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("15.1", "I don't need any help", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("15.2", "I get all the help I need", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("15.3", "I could use a little more help", null, "boolean", 3);
        QuestionnaireItem item4 = new QuestionnaireItem("15.4", "I need a lot more help", null, "boolean", 4);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        familyCommunitySupport1.setItemList(itemList);
        return familyCommunitySupport1;
    }

    public void setFamilyCommunitySupport1(QuestionnaireSection familyCommunitySupport1) {
        this.familyCommunitySupport1 = familyCommunitySupport1;
    }

    public QuestionnaireSection getFamilyCommunitySupport2() {
        familyCommunitySupport2 = new QuestionnaireSection();
        familyCommunitySupport2.setLinkId("16");
        familyCommunitySupport2.setTitle("Social Support");
        familyCommunitySupport2.setQuestion("<p><b>16. How often do you feel lonely or isolated from those around you?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("16.1", "Never", null, "boolean", 0);
        QuestionnaireItem item2 = new QuestionnaireItem("16.2", "Rarely", null, "boolean", 0);
        QuestionnaireItem item3 = new QuestionnaireItem("16.3", "Sometimes", null, "boolean", 0);
        QuestionnaireItem item4 = new QuestionnaireItem("16.4", "Often", null, "boolean", 1);
        QuestionnaireItem item5 = new QuestionnaireItem("16.5", "Always", null, "boolean", 1);

        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        itemList.add(item4);
        itemList.add(item5);
        familyCommunitySupport2.setItemList(itemList);
        return familyCommunitySupport2;
    }

    public void setFamilyCommunitySupport2(QuestionnaireSection familyCommunitySupport2) {
        this.familyCommunitySupport2 = familyCommunitySupport2;
    }

    public QuestionnaireSection getEducation1() {
        education1 = new QuestionnaireSection();
        education1.setLinkId("13");
        education1.setTitle("Employment and Education");
        education1.setQuestion("<p><b>13. Do you speak a language other than English at home?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("13.1", "Yes", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("13.2", "No", null, "boolean", 0);
        itemList.add(item1);
        itemList.add(item2);
        education1.setItemList(itemList);
        return education1;
    }

    public void setEducation1(QuestionnaireSection education1) {
        this.education1 = education1;
    }

    public QuestionnaireSection getEmployment1() {
        employment1 = new QuestionnaireSection();
        employment1.setLinkId("12");
        employment1.setTitle("Employment and Education");
        employment1.setQuestion("<p><b>12. Do you want help finding or keeping work or a job?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("12.1", "Yes, help finding work", null, "boolean", 3);
        QuestionnaireItem item2 = new QuestionnaireItem("12.2", "Yes, help keeping work", null, "boolean", 2);
        QuestionnaireItem item3 = new QuestionnaireItem("12.3", "I do not need or want help", null, "boolean", 1);
        itemList.add(item1);
        itemList.add(item2);
        itemList.add(item3);
        employment1.setItemList(itemList);
        return employment1;
    }

    public void setEmployment1(QuestionnaireSection employment1) {
        this.employment1 = employment1;
    }

    public QuestionnaireSection getEducation2() {
            education2 = new QuestionnaireSection();
            education2.setLinkId("14");
            education2.setTitle("Employment and Education");
            education2.setQuestion("<p><b>14. Do you want help with school or training? For example, starting or completing job " +
                    "training or getting a high school diploma, GED or equivalent.</b></p>");
            List<QuestionnaireItem> itemList = new ArrayList<>();
            QuestionnaireItem item1 = new QuestionnaireItem("14.1", "Yes", null, "boolean", 1);
            QuestionnaireItem item2 = new QuestionnaireItem("14.2", "No", null, "boolean", 0);
            itemList.add(item1);
            itemList.add(item2);
            education2.setItemList(itemList);
            return education2;
    }

    public void setEducation2(QuestionnaireSection education2) {
        this.education2 = education2;
    }

    public QuestionnaireSection getLegal() {
        legal = new QuestionnaireSection();
        legal.setLinkId("17");
        legal.setTitle("Legal Support");
        legal.setQuestion("<p><b>17. Are you currently having difficulties addressing issues such as divorce, child support/custody, " +
                "benefit appeals, or resolving disputes?</b></p>");
        List<QuestionnaireItem> itemList = new ArrayList<>();
        QuestionnaireItem item1 = new QuestionnaireItem("17.1", "Yes", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("17.2", "No", null, "boolean", 0);
        itemList.add(item1);
        itemList.add(item2);
        legal.setItemList(itemList);
        return legal;
    }

    public void setLegal(QuestionnaireSection legal) {
        this.legal = legal;
    }

    public String getFoodSecurityInfo() {
        foodSecurityInfo = "<p><b>Food Security</b> is the disruption of typical eating habits due to " +
                "lack of income and other resources.  Nearly one-quarter of Veterans receiving care in the " +
                "VA Healthcare System report experiencing food insecurity, double the rate for the " +
                "US population of 12%.</p>";
        return foodSecurityInfo;
    }

    public void setFoodSecurityInfo(String foodSecurityInfo) {
        this.foodSecurityInfo = foodSecurityInfo;
    }

    public String getHousingInsecurityInfo() {
        housingInsecurityInfo = "<p><b>Housing Insecurity</b> encompasses a number of challenges, including " +
                "homelessness.  Over 40,000 Veterans experience homelessness on any given day, and are more " +
                "likely to experience poorer physical and mental health outcomes that the general US " +
                "population who are homeless.</p>";
        return housingInsecurityInfo;
    }

    public void setHousingInsecurityInfo(String housingInsecurityInfo) {
        this.housingInsecurityInfo = housingInsecurityInfo;
    }

    public String getUtilityNeedsInfo() {
        utilityNeedsInfo = "<p><b>Utility Needs</b> With nearly 1.4 million Veterans at risk for homelessness, " +
                "utility bill assistance is an essential benefit for Veterans with financial burderns.  Over " +
                "666,000 Veterans in low-income households paid more that half their income for rent and utilites in 2017.</p>";
        return utilityNeedsInfo;
    }

    public void setUtilityNeedsInfo(String utilityNeedsInfo) {
        this.utilityNeedsInfo = utilityNeedsInfo;
    }

    public String getTransportationAccessInfo() {
        transportationAccessInfo = "<p><b>Transportation Access</b> Whether it be age, disability, or income-related.  " +
                "Veterans may face several barriers to travel, requiring the need for increased access to " +
                "transportation resources and assistance to get to medical appointments, work, and other " +
                "things needed for daily living.</p>";

        return transportationAccessInfo;
    }

    public void setTransportationAccessInfo(String transportationAccessInfo) {
        this.transportationAccessInfo = transportationAccessInfo;
    }

    public String getPersonalSafetyInfo() {
        personalSafetyInfo = "<p><b>Personal Safety</b> Exposure to abuse and violence includes intimate partner " +
                "violence (IPV) and elder abuse, amongy other forms of exposure to violence from friends and loved " +
                "ones.  In addition to immediate safety concerns and physical injuries, exposure to abuse and violence " +
                "can promote emotional and mental health conditions like depression and PTSD.</p>";
        return personalSafetyInfo;
    }

    public void setPersonalSafetyInfo(String personalSafetyInfo) {
        this.personalSafetyInfo = personalSafetyInfo;
    }

    public String getSocialSupportInfo() {
        socialSupportInfo = "<p><b>Social Support</b> The more a Veteran can identify sources of support in their life, " +
                "the higher the likelihood of them having positive perceptions of belonging and experiencing lower " +
                "rates of isolation.  With the Veteran suicide rate being 1.5 times the rate for the US general " +
                "population, the presence of a social support system is closely linked to a Veteran's mental " +
                "wellbeing and behaviors.</p>";
        return socialSupportInfo;
    }

    public void setSocialSupportInfo(String socialSupportInfo) {
        this.socialSupportInfo = socialSupportInfo;
    }

    public String getEmploymentAndEducationInfo() {
        employmentAndEducationInfo = "<p><b>Employment and Education</b> Transferring the skills and knowledge " +
                "learned during their military service can prove difficult for Veterans, requiring many to " +
                "complete additional schooling to meet civilian certification standards.  Difficulty finding " +
                "employment can further exacerbate financial strain, making it difficult to afford basic " +
                "needs such as food, housing, utilities, and healthcare costs.</p>";
        return employmentAndEducationInfo;
    }

    public void setEmploymentAndEducationInfo(String employmentAndEducationInfo) {
        this.employmentAndEducationInfo = employmentAndEducationInfo;
    }

    public String getLegalSupportInfo() {
        legalSupportInfo = "<p><b>Legal Support</b> is often and overlooked area of need for the Veterans, " +
                "who may have difficulties addressing legal issues such as divorce, child support/custody, " +
                "benefit appeals, and resolving disputes, among others.  Needing and using legal services " +
                "can be a significant stressor for Veterans, and increase mental distress and negatively " +
                "impact the quality of life.</p>";
        return legalSupportInfo;
    }

    public void setLegalSupportInfo(String legalSupportInfo) {
        this.legalSupportInfo = legalSupportInfo;
    }

    public Questionnaire createFHIRQuestionnaire() {
        Questionnaire acorn = new Questionnaire();
        acorn.setDate(new Date());
        acorn.setId("acorn-himss2022-demonstration");
        List<Coding> codingList = new ArrayList<>();
        Coding coding = new Coding();
        coding.setDisplay("HIMSS 2022 VA ACORN Interoperability Demonstration");
        coding.setCode("2022-2-27");
        coding.setSystem("https://va.gov");
        codingList.add(coding);
        acorn.setCode(codingList);
        List<CodeType> codeTypes = new ArrayList<>();
        CodeType codeType = new CodeType();
        codeType.setSystem("Patient");
        codeTypes.add(codeType);
        acorn.setSubjectType(codeTypes);
        acorn.setExperimental(true);
        acorn.setStatus(Enumerations.PublicationStatus.ACTIVE);

        questionList = new ArrayList<>();
        processSection(getLivingSituation1());
        processSection(getLivingSituation2());
        processSection(getFood1());
        processSection(getFood2());
        processSection(getUtilities1());
        processSection(getTransportation1());
        processSection(getSafety1());
        processSection(getSafety2());
        processSection(getSafety3());
        processSection(getSafety4());
        processSection(getFinancialStrain1());
        processSection(getEmployment1());
        processSection(getEducation1());
        processSection(getEducation2());
        processSection(getFamilyCommunitySupport1());
        processSection(getFamilyCommunitySupport2());
        processSection(getLegal());

        acorn.setItem(questionList);

        FhirContext ctx = FhirContext.forR4();

        IParser parser = ctx.newJsonParser();

        parser.setPrettyPrint(true);
        String serialized = parser.encodeResourceToString(acorn);
        System.out.println(serialized);

        return acorn;
    }

    private void processSection(QuestionnaireSection qSection) {
        Questionnaire.QuestionnaireItemComponent questionnaireItemComponent = new Questionnaire.QuestionnaireItemComponent();
        questionnaireItemComponent.setLinkId(qSection.getLinkId());
        questionnaireItemComponent.setText(qSection.getQuestion());
        questionnaireItemComponent.setType(Questionnaire.QuestionnaireItemType.GROUP);
        questionList.add(questionnaireItemComponent);

        List<QuestionnaireItem> answerList = qSection.getItemList();
        Iterator iter = answerList.iterator();
        while (iter.hasNext()) {
            QuestionnaireItem answer = (QuestionnaireItem)iter.next();
            Questionnaire.QuestionnaireItemComponent answerComponent = new Questionnaire.QuestionnaireItemComponent();
            answerComponent.setLinkId(answer.getLink());
            answerComponent.setText(answer.getDisplay());
            answerComponent.setType(Questionnaire.QuestionnaireItemType.BOOLEAN);
            questionList.add(answerComponent);
         }
    }

}
