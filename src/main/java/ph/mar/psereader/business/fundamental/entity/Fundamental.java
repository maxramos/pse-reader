package ph.mar.psereader.business.fundamental.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ph.mar.psereader.business.stock.entity.Stock;

@Entity
@Table(indexes = @Index(columnList = "stock_id"))
public class Fundamental implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_fundamental", sequenceName = "seq_fundamental", allocationSize = 1)
	@GeneratedValue(generator = "seq_fundamental")
	private Long id;

	@OneToOne
	private Stock stock;

	@Column(name = "up_trend")
	private boolean uptrend;

	public Fundamental() {
		super();
	}

	public Long getId() {
		return id;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public boolean isUptrend() {
		return uptrend;
	}

	public void setUptrend(boolean uptrend) {
		this.uptrend = uptrend;
	}

	@Override
	public String toString() {
		return String.format("Fundamental [id=%s, stock.id=%s, uptrend=%s]", id, stock == null ? null : stock.getId(), uptrend);
	}

}
