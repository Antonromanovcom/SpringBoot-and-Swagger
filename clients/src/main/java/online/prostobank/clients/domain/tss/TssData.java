package online.prostobank.clients.domain.tss;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TssData {
	private String name;
	private List<TssParam> params;
}
