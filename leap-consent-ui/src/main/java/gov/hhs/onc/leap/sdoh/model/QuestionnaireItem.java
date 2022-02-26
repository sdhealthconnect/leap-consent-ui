package gov.hhs.onc.leap.sdoh.model;

import org.hl7.fhir.r4.model.Coding;

public class QuestionnaireItem {
    private String link;
    private String display;
    private Coding coding;
    private String type;
    private int score;

    public QuestionnaireItem() {

    }

    public QuestionnaireItem(String link, String display, Coding coding, String type, int score) {
        this.link = link;
        this.display = display;
        this.coding = coding;
        this.type = type;
        this.setScore(score);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Coding getCoding() {
        return coding;
    }

    public void setCoding(Coding coding) {
        this.coding = coding;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
