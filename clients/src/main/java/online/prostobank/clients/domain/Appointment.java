package online.prostobank.clients.domain;

import org.hibernate.annotations.Formula;

import javax.persistence.Basic;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author yurij
 */
@Embeddable
public class Appointment {

    @Basic
    private LocalDate whenDate;
        
    @Basic
    private LocalTime whenTime;

    @Formula("0")
    private int dummy;
    
    @Basic
    private String appointmentAddress;

    @Enumerated(EnumType.STRING)
    @Basic
    private AppointmentType appointmentType;

    transient private int workaroundForBraindeadJpaImplementation = 1;

    public LocalDate getDate() {
        return whenDate;
    }
    
    public LocalTime getTime() {
        return whenTime;
    }

	public String getAppointmentAddress() {
		return appointmentAddress;
	}

	public void setAppointmentAddress(String appointmentAddress) {
		this.appointmentAddress = appointmentAddress;
	}

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Appointment(LocalDate date, LocalTime time) {
        this.whenDate = date;
        this.whenTime = time;
    }
    
    public void reschedule(LocalDate date, LocalTime time) {
        this.whenDate = date;
        this.whenTime = time;
    }

    protected Appointment() {
        
    }

    public enum AppointmentType {
        OFFICE("Офис"),
        OTHER_PLACE("Выезд");

        private String value;

        AppointmentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

