package gov.hhs.onc.leap.sdoh.model;

import java.util.List;

public class QuestionnaireSection {
    private String linkId;
    private String title;
    private String question;
    private List<QuestionnaireItem> itemList;

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<QuestionnaireItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<QuestionnaireItem> itemList) {
        this.itemList = itemList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
