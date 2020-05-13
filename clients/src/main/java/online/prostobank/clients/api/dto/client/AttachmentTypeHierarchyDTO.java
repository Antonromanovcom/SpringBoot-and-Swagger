package online.prostobank.clients.api.dto.client;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;
import online.prostobank.clients.domain.enums.AttachmentFunctionalType;
import online.prostobank.clients.domain.enums.AttachmentMetaType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Справочник типов документов с группировкой
 */
@Value
public class AttachmentTypeHierarchyDTO {
	private static final AttachmentTypeHierarchyDTO INSTANCE = new AttachmentTypeHierarchyDTO();
	private List<AttachmentGroupDTO> groups;
	private List<AttachmentTypeDTO> documentTypeLLC;
	private List<AttachmentTypeDTO> documentTypesSP;
	private Map<String, String> documentTypeNameLLC;
	private Map<String, String> documentTypeNameSP;

	private AttachmentTypeHierarchyDTO() {
		groups = new ArrayList<>();
		documentTypeLLC = new ArrayList<>();
		documentTypesSP = new ArrayList<>();
		documentTypeNameLLC = new HashMap<>();
		documentTypeNameSP = new HashMap<>();
		for (AttachmentMetaType metaType : AttachmentMetaType.values()) {
			groups.add(new AttachmentGroupDTO(metaType.getRuNameSP(), metaType.getRuNameLLC(),
					AttachmentFunctionalType.getTypesByGroup(metaType).stream()
							.map(it -> new AttachmentTypeDTO(it.name(), it.getRuNameSP(), it.getRuNameLLC()))
							.collect(Collectors.toList())));
		}
		AttachmentFunctionalType.getUngroupedTypes().forEach(it ->
				groups.add(new AttachmentGroupDTO(it.getRuNameSP(), it.getRuNameLLC(),
						Collections.singletonList(new AttachmentTypeDTO(it.name(), it.getRuNameSP(), it.getRuNameLLC())))
				));

		for (AttachmentFunctionalType type : AttachmentFunctionalType.values()) {
			if (!StringUtils.isEmpty(type.getRuNameLLC())) {
				documentTypeLLC.add(new AttachmentTypeDTO(type.name(), type.getRuNameSP(), type.getRuNameLLC()));
				documentTypeNameLLC.put(type.name(), type.getRuNameLLC());
			}
			if (!StringUtils.isEmpty(type.getRuNameSP())) {
				documentTypesSP.add(new AttachmentTypeDTO(type.name(), type.getRuNameSP(), type.getRuNameLLC()));
				documentTypeNameSP.put(type.name(), type.getRuNameSP());
			}
		}
	}

	public static AttachmentTypeHierarchyDTO instance() {
		return INSTANCE;
	}

	@Value
	@AllArgsConstructor
	private static class AttachmentTypeDTO {
		@JsonProperty(value = "id")
		private String id;
		@JsonProperty(value = "name_sp")
		private String nameSP;
		@JsonProperty(value = "name_llc")
		private String nameLLC;
	}

	@Value
	@AllArgsConstructor
	private static class AttachmentGroupDTO {
		@JsonProperty(value = "name_sp")
		private String nameSP;
		@JsonProperty(value = "name_llc")
		private String nameLLC;
		@JsonProperty(value = "types")
		private List<AttachmentTypeDTO> types;
	}
}
