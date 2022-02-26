package gov.hhs.onc.leap.sdoh.data;

import gov.hhs.onc.leap.sdoh.model.QuestionnaireItem;
import gov.hhs.onc.leap.sdoh.model.QuestionnaireSection;

import java.util.ArrayList;
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
        QuestionnaireItem item1 = new QuestionnaireItem("5.1", "Yes", null, "boolean", 1);
        QuestionnaireItem item2 = new QuestionnaireItem("5.2", "No", null, "boolean", 0);
        QuestionnaireItem item3 = new QuestionnaireItem("5.3", "Already shut off", null, "boolean", 2);

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
}
