package online.prostobank.clients.connectors;

import club.apibank.connectors.ConnectorArrestsFns;
import club.apibank.connectors.ConnectorArrestsFnsImpl;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import online.prostobank.clients.connectors.api.IArrestsCheckService;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ChecksResultValue;
import online.prostobank.clients.domain.statuses.Status;
import online.prostobank.clients.domain.statuses.StatusValue;
import online.prostobank.clients.services.StorageException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ArrestsCheckService implements IArrestsCheckService {
    private final ConnectorArrestsFns connectorArrestsFns;
    private static final String PATTERN_NOT_FOUND_ARREST = "(ОТСУТСТВУЮТ)+";

    public ArrestsCheckService() {
        connectorArrestsFns = ConnectorArrestsFnsImpl.getInstance();
    }


    @Override
    public void checkArrests(AccountApplication accountApplication) {
        String inn = accountApplication.getClient().getInn();
        byte[] resultDocument = connectorArrestsFns.getResultDocument(inn);
        ChecksResultValue checks = accountApplication.getChecks();

        if (resultDocument.length != 0) {
            try {
                PdfReader reader = new PdfReader(resultDocument);

                for (int i = 1; i <= reader.getNumberOfPages(); ++i) {
                    TextExtractionStrategy strategy = new SimpleTextExtractionStrategy();
                    String text = PdfTextExtractor.getTextFromPage(reader, i, strategy);
                    Pattern accountNumberPattern = Pattern.compile(PATTERN_NOT_FOUND_ARREST);
                    Matcher m = accountNumberPattern.matcher(text);
                    if (m.find()) {
                        checks.setArrestsFns(ChecksResultValue.OK);
                        accountApplication.addHistoryRecord(String.format("Проверка арестов с таким ИНН %s прошла успешно", inn));
                    } else {
                        checks.setArrestsFns(ChecksResultValue.HAVE_ARREST);
                        accountApplication.setStatus(new StatusValue(Status.ERR_AUTO_DECLINE));
                        accountApplication.addHistoryRecord(String.format("В ходе проверки с таким ИНН %s были найдены аресты. Карточка переведена в состояние автоотказ", inn));
                    }
                }
                reader.close();
                accountApplication.addBankAttachment(inn + " аресты.pdf", resultDocument, "report");
            } catch (StorageException  | IOException ex) {
                accountApplication.addHistoryRecord(String.format("Не удалось сохранить результаты проверки арестов с ИНН %s ", inn));
            }
        } else {
            accountApplication.addHistoryRecord(String.format("Проверка арестов с таким ИНН %s завершилась ошибкой", inn));
            checks.setArrestsFns(ChecksResultValue.ERROR);
        }
    }
}
