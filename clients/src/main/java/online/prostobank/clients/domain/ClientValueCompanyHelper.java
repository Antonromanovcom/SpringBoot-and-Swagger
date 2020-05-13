package online.prostobank.clients.domain;

import lombok.experimental.UtilityClass;
import online.prostobank.clients.domain.enums.CustomKYCFactors;

import java.util.HashMap;

@UtilityClass
public class ClientValueCompanyHelper {
    public static final HashMap<String, String> okveds = new HashMap<>();

    public static final HashMap<String, String> companyFeatures = new HashMap<>();

    public static final HashMap<String, String> companyScoring = new HashMap<>();

    static {
        okveds.put("20.51", "Производство спичек");
        okveds.put("25.30.2", "Производство ядерных реакторов и их составных частей, в том числе для транспортных средств");
        okveds.put("25.40", "Производство оружия и боеприпасов");
        okveds.put("25.71", "Производство ножевых изделий и столовых приборов");
        okveds.put("30.30.43", "Производство ракет");
        okveds.put("30.30.44", "Производство межконтинентальных баллистических ракет");
        okveds.put("30.4", "Производство военных боевых машин");
        okveds.put("32.1", "Производство ювелирных изделий, бижутерии и подобных товаров");
        okveds.put("46.12.22", "Деятельность агентов по оптовой торговле металлами в первичных формах");
        okveds.put("46.18.2", "Деятельность агентов, специализирующихся на оптовой торговле играми и игрушками, спортивными товарами, велосипедами, книгами, газетами, журналами, писчебумажными и канцелярскими товарами, музыкальными инструментами, часами и ювелирными изделиями, фототоварами и оптическими товарами");
        okveds.put("46.48", "Торговля оптовая часами и ювелирными изделиями");
        okveds.put("46.69.6", "Торговля оптовая оружием и боеприпасами");
        okveds.put("46.72.23", "Торговля оптовая золотом и другими драгоценными металлами");
        okveds.put("46.76.4", "Торговля оптовая драгоценными камнями");
        okveds.put("47.77.2", "Торговля розничная ювелирными изделиями в специализированных магазинах");
        okveds.put("47.78.5", "Деятельность коммерческих художественных галерей, торговля розничная произведениями искусства в коммерческих художественных галереях");
        okveds.put("47.78.7", "Торговля розничная оружием и боеприпасами в специализированных магазинах");
        okveds.put("47.79", "Торговля розничная бывшими в употреблении товарами в магазинах");
        okveds.put("47.99.2", "Деятельность по осуществлению торговли через автоматы");
        okveds.put("47.99.5", "Деятельность по осуществлению розничных продаж комиссионными агентами вне магазинов");
        okveds.put("52.29", "Деятельность вспомогательная прочая, связанная с перевозками");
        okveds.put("64.92", "Предоставление займов и прочих видов кредита");
        okveds.put("66", "Деятельность вспомогательная в сфере финансовых услуг и страхования");
        okveds.put("68", "Операции с недвижимым имуществом");
        okveds.put("92", "Деятельность по организации и проведению азартных игр и заключению пари, по организации и проведению лотерей");
        okveds.put("30.40", "Производство военных боевых машин ");
        okveds.put("32.11", "Чеканка монет");
        okveds.put("32.12", "Производство ювелирных изделий и аналогичных изделий");
        okveds.put("32.12.1", "Производство изделий технического назначения из драгоценных металлов");
        okveds.put("32.12.2", "Производство изделий технического назначения из драгоценных камней");
        okveds.put("32.12.3", "Обработка алмазов");
        okveds.put("32.12.4", "Обработка драгоценных, полудрагоценных, поделочных и синтетических камней, кроме алмазов");
        okveds.put("32.12.5", "Производство ювелирных изделий, медалей из драгоценных металлов и драгоценных камней");
        okveds.put("46.48.1", "Торговля оптовая часами");
        okveds.put("46.48.2", "Торговля оптовая ювелирными изделиями");
        okveds.put("47.79.1", "Торговля розничная предметами антиквариата");
        okveds.put("47.79.2", "Торговля розничная букинистическими книгами");
        okveds.put("47.79.3", "Торговля розничная прочими бывшими в употреблении товарами");
        okveds.put("47.79.4", "Деятельность аукционных домов по розничной торговле");
        okveds.put("64.92.1", "Деятельность по предоставлению потребительского кредита");
        okveds.put("64.92.2", "Деятельность по предоставлению займов промышленности");
        okveds.put("64.92.3", "Деятельность по предоставлению денежных ссуд под залог недвижимого имущества");
        okveds.put("64.92.4", "Деятельность по предоставлению кредитов на покупку домов специализированными учреждениями, не принимающими депозиты");
        okveds.put("64.92.6", "Деятельность по предоставлению ломбардами краткосрочных займов под залог движимого имущества");
        okveds.put("64.92.7", "Деятельность микрофинансовая");
        okveds.put("66.1", "Деятельность вспомогательная в сфере финансовых услуг, кроме страхования и пенсионного обеспечения");
        okveds.put("66.11", "Управление финансовыми рынками");
        okveds.put("66.11.1", "Деятельность по организации торговли на финансовых рынках");
        okveds.put("66.11.2", "Управление и контроль за деятельностью фондовых, товарных, валютных и валютно-фондовых бирж");
        okveds.put("66.11.3", "Деятельность регистраторов по ведению реестра владельцев ценных бумаг");
        okveds.put("66.11.4", "Деятельность по обеспечению эффективности функционирования финансовых рынков");
        okveds.put("66.11.5", "Деятельность по определению взаимных обязательств (клиринг)");
        okveds.put("66.12", "Деятельность брокерская по сделкам с ценными бумагами и товарами");
        okveds.put("66.12.1", "Деятельность биржевых посредников и биржевых брокеров, совершающих товарные фьючерсные и опционные сделки в биржевой торговле");
        okveds.put("66.12.2", "Деятельность по управлению ценными бумагами");
        okveds.put("66.12.3", "Деятельность эмиссионная");
        okveds.put("66.19", "Деятельность вспомогательная прочая в сфере финансовых услуг, кроме страхования и пенсионного обеспечения");
        okveds.put("66.19.1", "Деятельность по предоставлению брокерских услуг по ипотечным операциям");
        okveds.put("66.19.2", "Деятельность по предоставлению услуг по обработке наличных денег");
        okveds.put("66.19.3", "Деятельность по предоставлению консультационных услуг по вопросам финансового посредничества");
        okveds.put("66.19.4", "Предоставление услуг по хранению ценностей, депозитарная деятельность");
        okveds.put("66.19.5", "Деятельность по предоставлению брокерских услуг по ипотечным операциям");
        okveds.put("66.19.6", "Деятельность по приему платежей физических лиц платежными агентами");
        okveds.put("66.19.61", "Деятельность операторов по приему платежей физических лиц");
        okveds.put("66.19.62", "Деятельность платежных субагентов по приему платежей физических лиц");
        okveds.put("66.2", "Деятельность вспомогательная в сфере страхования и пенсионного обеспечения");
        okveds.put("68.1", "Покупка и продажа собственного недвижимого имущества");
        okveds.put("68.10", "Покупка и продажа собственного недвижимого имущества");
        okveds.put("68.10.1", "Подготовка к продаже собственного недвижимого имущества");
        okveds.put("68.10.11", "Подготовка к продаже собственного жилого недвижимого имущества");
        okveds.put("68.10.12", "Подготовка к продаже собственного нежилого недвижимого имущества");
        okveds.put("68.10.2", "Покупка и продажа собственного недвижимого имущества");
        okveds.put("68.10.21", "Покупка и продажа собственного жилого недвижимого имущества");
        okveds.put("68.10.22", "Покупка и продажа собственных нежилых зданий и помещений");
        okveds.put("68.10.23", "Покупка и продажа земельных участков");
        okveds.put("68.2", "Аренда и управление собственным или арендованным недвижимым имуществом");
        okveds.put("68.20", "Аренда и управление собственным или арендованным недвижимым имуществом");
        okveds.put("68.20.1", "Аренда и управление собственным или арендованным жилым недвижимым имуществом");
        okveds.put("68.20.2", "Аренда и управление собственным или арендованным нежилым недвижимым имуществом");
        okveds.put("68.3", "Операции с недвижимым имуществом за вознаграждение или на договорной основе");
        okveds.put("68.31", "Деятельность агентств недвижимости за вознаграждение или на договорной основе");
        okveds.put("68.31.1", "Предоставление посреднических услуг при купле-продаже недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.11", "Предоставление посреднических услуг при купле-продаже жилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.12", "Предоставление посреднических услуг при купле-продаже нежилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.2", "Предоставление посреднических услуг по аренде недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.21", "Предоставление посреднических услуг по аренде жилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.22", "Предоставление посреднических услуг по аренде нежилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.3", "Предоставление консультационных услуг при купле-продаже недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.31", "Предоставление консультационных услуг при купле-продаже жилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("92.1", "Деятельность по организации и проведению азартных игр и заключения пари");
        okveds.put("92.11", "Деятельность казино");
        okveds.put("92.12", "Деятельность залов игровых автоматов");
        okveds.put("92.13", "Деятельность по организации заключения пари");
        okveds.put("92.2", "Деятельность по организации и проведению лотерей");
        okveds.put("92.21", "Деятельность организаторов лотерей");
        okveds.put("92.22", "Деятельность операторов лотерей");
        okveds.put("92.23", "Деятельность распространения лотерейных билетов");
        okveds.put("25.30.21", "Производство ядерных установок, кроме устройств для разделения изотопов");
        okveds.put("25.30.22", "Производство частей ядерных установок, кроме устройств для разделения изотопов");
        okveds.put("25.4", "Производство оружия и боеприпасов");
        okveds.put("32.12.6", "Изготовление ювелирных изделий и аналогичных изделий по индивидуальному заказу населения");
        okveds.put("32.12.7", "Обработка янтаря и производство изделий из янтаря");
        okveds.put("66.21", "Оценка рисков и ущерба");
        okveds.put("66.22", "Деятельность страховых агентов и брокеров");
        okveds.put("66.29", "Деятельность вспомогательная прочая в сфере страхования и пенсионного обеспечения");
        okveds.put("66.29.1", "Деятельность страховых актуариев");
        okveds.put("66.29.2", "Деятельность распорядителей спасательными работами");
        okveds.put("66.29.9", "Деятельность вспомогательная прочая в сфере страхования, кроме обязательного социального страхования");
        okveds.put("66.3", "Деятельность по управлению фондами");
        okveds.put("66.30", "Деятельность по управлению фондами");
        okveds.put("66.30.1", "Управление инвестиционными фондами");
        okveds.put("66.30.2", "Управление фондами денежного рынка");
        okveds.put("66.30.3", "Управление пенсионными накоплениями негосударственных пенсионных фондов");
        okveds.put("66.30.4", "Управление пенсионными резервами негосударственных пенсионных фондов");
        okveds.put("66.30.5", "Управление страховыми резервами субъектов страхового дела");
        okveds.put("66.30.6", "Управление на основе индивидуальных договоров доверительного управления активами");
        okveds.put("66.30.9", "Другие виды деятельности по управлению активами");
        okveds.put("68.31.32", "Предоставление консультационных услуг при купле-продаже нежилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.4", "Предоставление консультационных услуг по аренде недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.41", "Предоставление консультационных услуг по аренде жилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.42", "Предоставление консультационных услуг по аренде нежилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.5", "Предоставление посреднических услуг при оценке недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.51", "Предоставление посреднических услуг при оценке жилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.31.52", "Предоставление посреднических услуг при оценке нежилого недвижимого имущества за вознаграждение или на договорной основе");
        okveds.put("68.32", "Управление недвижимым имуществом за вознаграждение или на договорной основе");
        okveds.put("68.32.1", "Управление эксплуатацией жилого фонда за вознаграждение или на договорной основе");
        okveds.put("68.32.2", "Управление эксплуатацией нежилого фонда за вознаграждение или на договорной основе");
        okveds.put("68.32.3", "Деятельность по технической инвентаризации недвижимого имущества");
    }

    static {
        companyFeatures.put("InBankruptcy", "Компания в процессе банкротства");
        companyFeatures.put("DisqualifiedHeads", "ФИО руководителей или учредителей были найдены в реестре дисквалифицированных лиц");
        companyFeatures.put("NKO", "Организационно-правовая форма относится к некоммерческим организациям");
        companyFeatures.put("UlFoundersShare20", "Учредители - юрлица (количество) - с долей владения больше 20%");
        companyFeatures.put("StateDebtsInsurance", "Сумма задолженности по исполнительным производствам, предметом которых являются страховые взносы");
        companyFeatures.put("UnreliableAddress", "В ЕГРЮЛ указан признак недостоверности сведений в отношении адреса");
        companyFeatures.put("RegistryMFO", "Организация найдена в государственном реестре микрофинансовых организаций");
        companyFeatures.put("RegistryCreditCooperative", "Организация найдена в государственном реестре кредитных потребительских кооперативов");
        companyFeatures.put("StateDebtsTax", "Сумма задолженности по исполнительным производствам, предметом которых являются налоги и сборы");
        companyFeatures.put("RegistryPawnShop", "Организация найдена в государственном реестре ломбардов");
        companyFeatures.put("FounderRegisteredInFatfBanned", "Учредитель зарегистрирован на территории, которая не выполняет требования ФАТФ");
        companyFeatures.put("FounderRegisteredInContraSanctionsCountry", "Учредитель зарегистрирован на территории, в отношении которой применяются специальные экономические меры в соответствии с 281-ФЗ");
        companyFeatures.put("FounderRegisteredInUnSanctionsCountry", "Учредитель зарегистрирован на территории, в отношении которой действуют санкции ООН, одобренные РФ");
        companyFeatures.put("FounderRegisteredInOffshores", "Учредитель зарегистрирован на территории, которая относится к офшорным зонам");
        companyFeatures.put("UnreliableHeadsData", "В ЕГРЮЛ указан признак недостоверности сведений в отношении руководителя или учредителей");
        companyFeatures.put("StateDebtsFssp", "Организация была найдена в списке юридических лиц, имеющих задолженность по уплате налогов более 1000 руб, которая направлялась на взыскание судебному приставу-исполнителю (ФНС)");
        companyFeatures.put("ForeignCompany", "Иностранная компания");
        companyFeatures.put("InLiquidation", "Компания в стадии ликвидации");
        companyFeatures.put("DissolvedCompany", "Юр. лицо является недействующим");
        companyFeatures.put("RegistryCasino", "Организация найдена в реестре лицензий на деятельноcть по организации и проведению азартных игр в букмекерских конторах и тотализаторах");
        companyFeatures.put("RiskActivitiesBlackList", "Виды деятельности входят в чёрный список Банка");
        companyFeatures.put("FounderRegisteredInFnsBannedCountry", "Учредитель зарегистрирован на территории, не обеспечивающей обмен информацией для целей налогообложения с Российской Федерацией");
    }

    static {
        companyScoring.put("BadSupplier", "Организация была найдена в реестре недобросовестных поставщиков");
        companyScoring.put("InReorganisation", "Компания в процессе реорганизации");
        companyScoring.put("AffiliatesInBankruptcyCount", "Количество потенциально связанных организаций, находящихся в  процессе банкротства");
        companyScoring.put("AffiliatedUnreliableSupplierCount", "Количество потенциально связанных организаций, найденных в реестре недобросовестных поставщиков (ФАС, Федеральное Казначейство)");
        companyScoring.put("AffiliatedDisqualifiedHeadsCount", "Количество потенциально связанных организаций, где ФИО руководителей или учредителей найдены в реестре дисквалифицированных лиц (ФНС)");
        companyScoring.put("TooManyHeadsLastYear", "Количество изменений руководителей за последний год");
        companyScoring.put("RegistrationWasChanged", "Организация изменяла место постановки на налоговый учет за последний год");
        companyScoring.put("AccountingReportMiss", "Отсутствует бухгалтерская отчетность за последний отчетный год (на момент, когда такая отчетность становится доступна)");
        companyScoring.put("BankruptcyArbitrationsCount", "Наличие арбитражных дел в качестве ответчика, которые связаны с проведением процедуры банкротства");
        companyScoring.put("NotFinishedArbitrationsCount", "Наличие незавершённых арбитражных дел в качестве ответчика");
        companyScoring.put("NoContacts", "Есть контактные данные организации");
        companyScoring.put("StatedCapital", "Уставный капитал меньше контрольного значения");
        companyScoring.put("ForeignFounders", "Учредителями являются иностранные компании");
        companyScoring.put("FizFounders", "Учредители - физлица (количество)");
        companyScoring.put("UlFounders", "Учредители - юр.лица (количество)");
        companyScoring.put("RegistryTourOperator", "Организация найдена в едином федеральном реестре туроператоров");
        companyScoring.put("ThreeMonthOldCompany", "Организация зарегистрирована менее 3 месяцев тому назад");
        companyScoring.put("SixMonthOldCompany", "Организация зарегистрирована от 3 до 6 месяцев тому назад");
        companyScoring.put("TwelveMonthOldCompany", "Организация зарегистрирована от 6 до 12 месяцев тому назад");
        companyScoring.put("FizShareholders", "Акционеры - физлица (количество)");
        companyScoring.put("AllShareholders", "Общее количество аффилированных акционеров ");
        companyScoring.put("FounderIsHead", "Единственный учередитель является руководителем");
        companyScoring.put("WrongRegion", "Адрес регистрации отличается от региона банка");
        companyScoring.put("RegistrationByWarrant", "Регистрация по доверенности");
        companyScoring.put("Revenue", "Выручка за последний отчетный год (на конец отчетного периода)");
        companyScoring.put("ReportMiss", "Организация была найдена в списке юридических лиц, не представляющих налоговую отчетность более года");
        companyScoring.put("DuplicateRegistrationAddress", "Организация зарегистрирована по адресу, по которому зарегистрировано другое юридическое лицо (без учета номера офиса)");
        companyScoring.put("DiscrepantActivities", "Обнаружены несогласованные виды деятельности");
        companyScoring.put("AffiliatesActiveCount", "Количество действующих потенциально связанных организаций");
        companyScoring.put("AffiliatesDissolvingCount", "Количество потенциально связанных организаций, находящихся в стадии ликвидации");
        companyScoring.put("AffiliatedMassAddressCount", "Количество потенциально связанных организаций, адреса которых найдены в списке \"адресов массовой регистрации\" (ФНС)");
        companyScoring.put("AffiliatedReportMissCount", "Количество потенциально связанных организаций, найденных в списке юридических лиц, не представляющих налоговую отчетность более года (ФНС)");
        companyScoring.put("AffiliatedUnreliableAddress", "Количество потенциально связанных организаций, у которых в ЕГРЮЛ указан признак недостоверности сведений в отношении адреса");
        companyScoring.put("AffiliatedUnreliableHeadsCount", "Количество потенциально связанных организаций, у которых в ЕГРЮЛ указан признак недостоверности сведений в отношении руководителя или учредителей");
        companyScoring.put("MassFounder", "ФИО учредителя было найдено в списке ФНС \"массовых\" учредителей");
        companyScoring.put("AffiliatesDissolvedCount", "Количество ликвидированных потенциально связанных организаций");
        companyScoring.put("RiskActivitiesCustom", "Виды деятельности связаны с операциями повышенной степени риска (Внутренние правила Банка)");
        companyScoring.put("MassHead", "ФИО руководителя было найдено в списке ФНС \"массовых\" руководителей");
        companyScoring.put("MassRegistrationAddress", "Адрес организации был найден в списке \"адресов массовой регистрации\"");
        companyScoring.put("RiskActivities375", "Виды деятельности связаны с операциями повышенной степени риска по 375-П");
        companyScoring.put("Rejected639Count", "Организация найдена в перечне ЦБ на основании 639-П. Количество сообщений: приостановка операции – легализация доходов, финансирование терроризма, расторжение договора банковского счета без учета реабилитации");
        companyScoring.put("RejectedInsufficientDocumentsCount", "Организация найдена в перечне ЦБ на основании 639-П. Количество сообщений: приостановка операций – не предоставлены документы, отказ в заключении договора банковского счета без учета реабилитации");
        companyScoring.put("CompanyInCustomLists", "Организация найдена в пользовательском списке");
        companyScoring.put("HeadInCustomLists", "ФИО руководителя или учредителя найдено в пользовательском списке");
        companyScoring.put(CustomKYCFactors.FINANCE_RESULT.getKey(), CustomKYCFactors.FINANCE_RESULT.getName());
        companyScoring.put(CustomKYCFactors.HEAD_CHANGES_COUNT.getKey(), CustomKYCFactors.HEAD_CHANGES_COUNT.getName());
        companyScoring.put(CustomKYCFactors.FOUNDER_CHANGED.getKey(), CustomKYCFactors.FOUNDER_CHANGED.getName());
        companyScoring.put(CustomKYCFactors.EMPLOYEES_COUNT_ONE.getKey(), CustomKYCFactors.EMPLOYEES_COUNT_ONE.getName());
        companyScoring.put(CustomKYCFactors.EMPLOYEES_COUNT_ZERO.getKey(), CustomKYCFactors.EMPLOYEES_COUNT_ZERO.getName());
    }
}
