package gov.hhs.onc.leap.adr.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuestionnaireError {
    private String errorMessage;
    private int questionnaireIndex;

    public QuestionnaireError(String errorMessage, int questionnaireIndex) {
        this.errorMessage = errorMessage;
        this.questionnaireIndex = questionnaireIndex;
    }
}
