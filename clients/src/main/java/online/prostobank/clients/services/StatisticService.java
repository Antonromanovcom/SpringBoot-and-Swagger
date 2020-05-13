package online.prostobank.clients.services;

import lombok.RequiredArgsConstructor;
import online.prostobank.clients.domain.AccountApplication;
import online.prostobank.clients.domain.ClientValueCompanyHelper;
import online.prostobank.clients.domain.repository.AccountApplicationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static online.prostobank.clients.utils.Utils.DD_MM_YYYY_RU;

@RequiredArgsConstructor
@Service
public class StatisticService {

    private final AccountApplicationRepository accountApplicationRepository;

    @Value("${statistic.api.have}")
    private String have;
    @Value("${statistic.api.none}")
    private String none;

    public byte[] getScoringStatisticByInterval(@Nonnull String from, @Nonnull String to, HttpServletResponse response){
        SimpleDateFormat format = new SimpleDateFormat(DD_MM_YYYY_RU);
        try {
            Date fromDate = format.parse(from);
            Date toDate = format.parse(to);
            StringBuffer buffer = new StringBuffer();

            Set<String> keysScoringCompany = ClientValueCompanyHelper.companyScoring.keySet();
            buffer.append("ИНН");
            keysScoringCompany.forEach(keyScoring -> buffer.append(";").append(ClientValueCompanyHelper.companyScoring.get(keyScoring)));

            Set<String> keysScoringFeature = ClientValueCompanyHelper.companyFeatures.keySet();
            keysScoringFeature.forEach(keyScoring -> buffer.append(";").append(ClientValueCompanyHelper.companyFeatures.get(keyScoring)));
            buffer.append(";Балл\n");

            List<AccountApplication> accountApplications = accountApplicationRepository
                    .findAllByDateIntervalAndIsChecked(fromDate.toInstant(), toDate.toInstant());
            if (null != accountApplications) {
                accountApplications
                        .forEach(getAccountApplicationConsumer(buffer, keysScoringCompany, keysScoringFeature));
            }
            return buffer.toString().getBytes(StandardCharsets.UTF_8);

        } catch (ParseException e) {
            System.err.println("При генерации csv файла произошла ошибка :"+e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
        return new byte[]{};
    }

    private Consumer<AccountApplication> getAccountApplicationConsumer(StringBuffer buffer, Set<String> keysScoringCompany, Set<String> keysScoringFeature) {
        return accountApplication -> {
            buffer.append(String.format("%s%s;%s\n",
                    accountApplication.getClient().getInn(),
                    getScoringInfo(keysScoringCompany, keysScoringFeature,
                            accountApplication.getClient().getCompanyKycScoring().getFailedKycScoring(),
                            accountApplication.getClient().getKonturFeature().getFailedFeatures()),
                    accountApplication.getChecks().getKonturCheck()));
        };
    }

    private String getScoringInfo(Set<String> keysScoringCompany, Set<String> keysScoringFeature, String failedKycScoring, String failedKycFeature){
        StringBuffer buffer = new StringBuffer();
        keysScoringCompany.forEach(scoring -> appendValue(failedKycScoring, buffer, scoring));
        keysScoringFeature.forEach(scoring -> appendValue(failedKycFeature, buffer, scoring));
        return buffer.toString();
    }

    private void appendValue(String failedKycScoring, StringBuffer buffer, String scoring) {
        if (failedKycScoring.contains(scoring)) {
            buffer.append(";").append(have);
        } else {
            buffer.append(";").append(none);
        }
    }

}
