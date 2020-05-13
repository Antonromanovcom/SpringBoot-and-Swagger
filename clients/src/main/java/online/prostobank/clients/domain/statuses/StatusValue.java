package online.prostobank.clients.domain.statuses;

import lombok.Data;
import lombok.NoArgsConstructor;
import online.prostobank.clients.domain.state.state.ClientStates;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.Instant;

@Data
@NoArgsConstructor
@Embeddable
public class StatusValue {

    @Enumerated(EnumType.STRING)
    private Status status;
    private Integer subcode;
    private Instant dateTime;

	public StatusValue(Status s, KycSystemForDeclineCode subcode) {
        this.status = s;
		this.dateTime = Instant.now();
        this.subcode = subcode.getKycCode();
    }

	public StatusValue(Status s) {
        this.status = s;
		this.dateTime = Instant.now();
    }

    public boolean is(Status st) {
        return this.status == st;
    }

    public Status getValue() {
        return this.status;
    }

    @Deprecated
    public Status getLegacyState(ClientStates nextState){
        Status result;
        switch (nextState){
            case MANAGER_PROCESSING:
                result = Status.MANAGER_PROCESSING;
                break;
            case NO_ANSWER:
                result = Status.NO_ANSWER;
                break;
            case NEW_CLIENT:
                result = Status.CONTACT_INFO_UNCONFIRMED;
                break;
            case ACTIVE_CLIENT:
                result = Status.FULFILLED;
                break;
            case REQUIRED_DOCS:
            case WAIT_FOR_DOCS:
            case DOCUMENTS_EXISTS:
                result = Status.WAIT_FOR_DOCS;
                break;
            case CHECK_LEAD:
            case CONTACT_INFO_CONFIRMED:
                result = Status.CONTACT_INFO_CONFIRMED;
                break;
            case CLIENT_DECLINED:
                result = Status.ERR_CLIENT_DECLINE;
                break;
            case INACTIVE_CLIENT:
            case AUTO_DECLINED:
            default:
                result = Status.ERR_AUTO_DECLINE;
        }
        return result;
    }

    @Deprecated
    public ClientStates getNewStatus(){
        switch (this.status){
            case CONTACT_INFO_UNCONFIRMED:
                return ClientStates.NEW_CLIENT;
            case CONTACT_INFO_CONFIRMED:
                return ClientStates.CONTACT_INFO_CONFIRMED;
            case NO_ANSWER:
                return ClientStates.NO_ANSWER;
            case WAIT_FOR_DOCS:
                return ClientStates.WAIT_FOR_DOCS;
            case RESERVING:
            case NEW:
            case MANAGER_PROCESSING:
            case NOW_SIGNING:
            case NO_ANSWER_DELIVERY:
            case APPOINTMENT_MADE:
            case SECURITY_PROCESSING:
            case ISSUING_CERT:
            case GO_OPEN:
                return ClientStates.MANAGER_PROCESSING;
            case ERR_SECURITY_DECLINE:
            case ERR_CLIENT_DECLINE:
            case ERR_AUTO_DECLINE:
            case ERR_MANAGER_DECLINE:
            case BANK_REFUSED:
                return ClientStates.AUTO_DECLINED;
            case GO_ACTIVE:
            case FULFILLED:
                return ClientStates.ACTIVE_CLIENT;
            case CLOSED:
                return ClientStates.INACTIVE_CLIENT;

            case POS_BACK_REFUSED:
            case ERR_CANT_ACTIVATE:
            case ERR_ALREADY_OPENED:
            case SELF_EMPLOYED_APPLICATION:

            default:
                return ClientStates.NEW_CLIENT;
        }
    }
}
