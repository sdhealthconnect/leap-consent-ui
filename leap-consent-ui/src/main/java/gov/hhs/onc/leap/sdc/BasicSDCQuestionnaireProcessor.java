package gov.hhs.onc.leap.sdc;

import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.generator.FHIRGenerator;
import com.ibm.fhir.model.generator.exception.FHIRGeneratorException;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.parser.exception.FHIRParserException;
import com.ibm.fhir.model.patch.exception.FHIRPatchException;
import com.ibm.fhir.model.resource.Questionnaire;
import com.ibm.fhir.model.resource.QuestionnaireResponse;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.Decimal;
import com.ibm.fhir.model.type.Element;
import com.ibm.fhir.model.type.Expression;
import com.ibm.fhir.model.type.Extension;
import com.ibm.fhir.path.FHIRPathNode;
import com.ibm.fhir.path.evaluator.FHIRPathEvaluator;
import com.ibm.fhir.path.exception.FHIRPathException;
import com.ibm.fhir.path.util.FHIRPathUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BasicSDCQuestionnaireProcessor {
    static final String VARIABLE_EXTENSION_URL = "http://hl7.org/fhir/StructureDefinition/variable";
    static final String CALCULATED_EXPRESSION_EXTENSION_URL = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-calculatedExpression";
    static final String CALCULATED_EXPRESSION_EXTENSION_FHIR_PATH = String.format("Questionnaire.descendants().where(url='%s')", CALCULATED_EXPRESSION_EXTENSION_URL);

    public static Map<String, String> getVariableDefinitions(Questionnaire questionnaire) {
        List<Extension> extensions = questionnaire.getExtension();
        Map<String, String> variables = extensions.stream()
                .filter(extension -> VARIABLE_EXTENSION_URL.equals(extension.getUrl()))
                .map(extension -> (Expression) extension.getValue())
                .collect(Collectors.toMap(
                        expression -> expression.getName().getValue(),
                        expression -> expression.getExpression().getValue()));
        return variables;
    }

    public static String evaluateFhirPath(Resource resource, String expression) {
        try {
            return FHIRPathEvaluator.evaluator().evaluate(resource, expression).stream().findFirst().get().toString();
        } catch (FHIRPathException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> resolveVariables(Map<String, String> variableDefinitions, QuestionnaireResponse questionnaireResponse) throws FHIRPathException {
        return variableDefinitions.keySet().stream().collect(Collectors.toMap(
                variableName -> "%" + variableName,
                variableName -> evaluateFhirPath(questionnaireResponse, variableDefinitions.get(variableName))
        ));
    }

    public static String evaluateCalculatedExpression(String expression, Map<String, String> variables, QuestionnaireResponse questionnaireResponse) {
        String expressionWithVariablesResolved = StringUtils.replaceEach(
                expression,
                variables.keySet().toArray(new String[0]),
                variables.values().toArray(new String[0]));
        return evaluateFhirPath(questionnaireResponse, expressionWithVariablesResolved);
    }

    private static String convertQPathToQRPath(String qPath) {
        return StringUtils.replace(
                StringUtils.substringBeforeLast(qPath, "."),
                "Questionnaire",
                "QuestionnaireResponse");
    }

    private static QuestionnaireResponse.Item generateQRItem(String linkId, String value, String type) {
        Element typedValue;
        if ("decimal".equals(type)) {
            typedValue = Decimal.builder().value(value).build();

        } else if ("integer".equals(type)) {
            typedValue = com.ibm.fhir.model.type.Integer.builder().value(value).build();
        } else if ("boolean".equals(type)) {
            typedValue = com.ibm.fhir.model.type.Boolean.builder().value(value).build();
        }
        else {
            typedValue = com.ibm.fhir.model.type.String.builder().value(value).build();
        }

        Extension hiddenExtension = Extension.builder()
                .url("http://hl7.org/fhir/StructureDefinition/questionnaire-hidden")
                .value(true)
                .build();

        QuestionnaireResponse.Item.Answer answer = QuestionnaireResponse.Item.Answer.builder()
                .value(typedValue).build();
        QuestionnaireResponse.Item newItem = QuestionnaireResponse.Item.builder()
                .linkId(linkId)
                .answer(answer)
                .extension(hiddenExtension)
                .build();
        return newItem;
    }

    public static String transform(String questionnaireString, String questionnaireResponseString) throws FHIRParserException, FHIRPathException, FHIRPatchException, FHIRGeneratorException {
        Questionnaire questionnaire = FHIRParser.parser(Format.JSON)
                .parse(new StringReader(questionnaireString));
        QuestionnaireResponse questionnaireResponse = FHIRParser
                .parser(Format.JSON).parse(new StringReader(questionnaireResponseString));

        Map<String, String> variableDefinitions = getVariableDefinitions(questionnaire);

        Map<String, String> variables = resolveVariables(variableDefinitions, questionnaireResponse);

        Collection<FHIRPathNode> fhirPathNodes = FHIRPathEvaluator.evaluator().evaluate(questionnaire, CALCULATED_EXPRESSION_EXTENSION_FHIR_PATH);
        final Iterator<FHIRPathNode> it = fhirPathNodes.iterator();
        while (it.hasNext()) {
            final FHIRPathNode fhirPathNode = it.next();
            String pathToExtension = fhirPathNode.path();
            String pathToElement = StringUtils.substringBeforeLast(pathToExtension, ".extension[");
            String linkId = FHIRPathEvaluator.evaluator().evaluate(questionnaire, pathToElement + ".linkId").stream().findFirst().get().getValue().toString();
            String type = FHIRPathEvaluator.evaluator().evaluate(questionnaire, pathToElement + ".type").stream().findFirst().get().getValue().toString();
            Extension extension = (Extension) fhirPathNode.asElementNode().element();
            String expression = ((Expression) extension.getValue()).getExpression().getValue();
            String value = evaluateCalculatedExpression(expression, variables, questionnaireResponse);
            String pathToElementInQuestionnaireResponse = convertQPathToQRPath(pathToElement);
            QuestionnaireResponse.Item newItem = generateQRItem(linkId, value, type);
            questionnaireResponse = FHIRPathUtil.add(questionnaireResponse, pathToElementInQuestionnaireResponse, "item", newItem);
        }
        StringWriter output = new StringWriter();
        FHIRGenerator.generator(Format.JSON, true).generate(questionnaireResponse, output);
        return output.toString();
    }

    public static void main(String[] args) throws IOException, FHIRParserException, FHIRPathException {
        String fhirPathExpression = "true";
        String questionnaireFilePath = "src/test/fixtures/acorn-questionnaire-response-1.json";
        String questionnaireResponseString = new String(Files.readAllBytes(Paths.get(questionnaireFilePath)));
        QuestionnaireResponse questionnaireResponse = FHIRParser.parser(Format.JSON)
                .parse(new StringReader(questionnaireResponseString));
        String result = FHIRPathEvaluator.evaluator().evaluate(questionnaireResponse, fhirPathExpression).stream().findFirst().get().toString();
        System.out.println(result);
    }

}
