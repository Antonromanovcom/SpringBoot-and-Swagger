package online.prostobank.clients.domain;

import com.google.common.collect.ImmutableList;
import org.apache.commons.collections4.ListUtils;

import java.util.List;

public class OkvedConstants {
	public static final List<String> REALTOR_OKVEDS = ImmutableList.of("68.31", "68.32");
	public static final List<String> TRAFFIC_OKVEDS = ImmutableList.of("49.4");
	// Нужно отображать в заявке рискованные ОКВЭД (по которым нужно запрашивать документы)
	public static final List<String> SPECIAL_RISKY_OKVED = ListUtils.union(REALTOR_OKVEDS, TRAFFIC_OKVEDS);
}
