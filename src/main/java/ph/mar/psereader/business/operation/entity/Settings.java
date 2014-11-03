package ph.mar.psereader.business.operation.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({ @NamedQuery(name = Settings.ALL, query = "SELECT s FROM Settings s") })
public class Settings implements Serializable {

	public static final String ALL = "Settings.ALL";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_settings", sequenceName = "seq_settings", allocationSize = 1)
	@GeneratedValue(generator = "seq_settings")
	private Long id;

	@Column(name = "last_processed_date")
	@Temporal(TemporalType.DATE)
	private Date lastProcessedDate;

	@Column(name = "suspension_date")
	@Temporal(TemporalType.DATE)
	private Date suspensionDate;

	@Lob
	@Column(name = "suspended_stocks")
	private List<String> suspendedStocks;

	public Settings() {
		super();
	}

	public Settings(Date lastProcessedDate, Date suspensionDate) {
		this.lastProcessedDate = lastProcessedDate;
		this.suspensionDate = suspensionDate;
	}

	public Long getId() {
		return id;
	}

	public Date getLastProcessedDate() {
		return lastProcessedDate;
	}

	public void setLastProcessedDate(Date lastProcessedDate) {
		this.lastProcessedDate = lastProcessedDate;
	}

	public Date getSuspensionDate() {
		return suspensionDate;
	}

	public void setSuspensionDate(Date suspensionDate) {
		this.suspensionDate = suspensionDate;
	}

	public List<String> getSuspendedStocks() {
		return suspendedStocks;
	}

	public void setSuspendedStocks(List<String> suspendedStocks) {
		this.suspendedStocks = suspendedStocks;
	}

	@Override
	public String toString() {
		return String.format("Settings [id=%s, lastProcessedDate=%s, suspensionDate=%s, suspendedStocks=%s]", id, lastProcessedDate, suspensionDate,
				suspendedStocks);
	}

}
