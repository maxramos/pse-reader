package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DmiResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "dmi_adx", nullable = false, precision = 13, scale = 10)
	private BigDecimal adx;

	@Column(name = "dmi_plus_di", nullable = false, precision = 13, scale = 10)
	private BigDecimal plusDi;

	@Column(name = "dmi_minus_di", nullable = false, precision = 13, scale = 10)
	private BigDecimal minusDi;

	@Column(name = "dmi_smoothed_plus_dm", nullable = false, precision = 13, scale = 10)
	private BigDecimal smoothedPlusDm;

	@Column(name = "dmi_smoothed_minus_dm", nullable = false, precision = 13, scale = 10)
	private BigDecimal smoothedMinusDm;

	@Column(name = "dmi_atr", nullable = false, precision = 13, scale = 10)
	private BigDecimal atr;

	public DmiResult() {
		super();
	}

	public DmiResult(BigDecimal adx, BigDecimal plusDi, BigDecimal minusDi, BigDecimal smoothedPlusDm, BigDecimal smoothedMinusDm, BigDecimal atr) {
		this.adx = adx;
		this.plusDi = plusDi;
		this.minusDi = minusDi;
		this.smoothedPlusDm = smoothedPlusDm;
		this.smoothedMinusDm = smoothedMinusDm;
		this.atr = atr;
	}

	public BigDecimal getAdx() {
		return adx;
	}

	public BigDecimal getPlusDi() {
		return plusDi;
	}

	public BigDecimal getMinusDi() {
		return minusDi;
	}

	public BigDecimal getSmoothedPlusDm() {
		return smoothedPlusDm;
	}

	public BigDecimal getSmoothedMinusDm() {
		return smoothedMinusDm;
	}

	public BigDecimal getAtr() {
		return atr;
	}

	@Override
	public String toString() {
		return String.format("DmiResult [adx=%s, plusDi=%s, minusDi=%s, smoothedPlusDm=%s, smoothedMinusDm=%s, atr=%s]", adx, plusDi, minusDi,
				smoothedPlusDm, smoothedMinusDm, atr);
	}

	public static class Holder {

		private List<BigDecimal> trs;
		private List<BigDecimal> plusDms;
		private List<BigDecimal> minusDms;

		public Holder(int size) {
			trs = new ArrayList<>(size);
			plusDms = new ArrayList<>(size);
			minusDms = new ArrayList<>(size);
		}

		public void add(BigDecimal tr, BigDecimal plusDm, BigDecimal minusDm) {
			trs.add(tr);
			plusDms.add(plusDm);
			minusDms.add(minusDm);
		}

		public void reverse() {
			Collections.reverse(trs);
			Collections.reverse(plusDms);
			Collections.reverse(minusDms);
		}

		public List<BigDecimal> getTrs() {
			return trs;
		}

		public List<BigDecimal> getPlusDms() {
			return plusDms;
		}

		public List<BigDecimal> getMinusDms() {
			return minusDms;
		}

	}

	public static class SmoothedHolder {

		private List<BigDecimal> atrs;
		private List<BigDecimal> smoothedPlusDms;
		private List<BigDecimal> smoothedMinusDms;
		private List<BigDecimal> plusDis;
		private List<BigDecimal> minusDis;
		private List<BigDecimal> dxs;

		public SmoothedHolder(int size) {
			atrs = new ArrayList<>(size);
			smoothedPlusDms = new ArrayList<>(size);
			smoothedMinusDms = new ArrayList<>(size);
			plusDis = new ArrayList<>(size);
			minusDis = new ArrayList<>(size);
			dxs = new ArrayList<>(size);
		}

		public void add(BigDecimal atr, BigDecimal smoothedPlusDm, BigDecimal smoothedMinusDm, BigDecimal plusDi, BigDecimal minusDi, BigDecimal dx) {
			atrs.add(atr);
			smoothedPlusDms.add(smoothedPlusDm);
			smoothedMinusDms.add(smoothedMinusDm);
			plusDis.add(plusDi);
			minusDis.add(minusDi);
			dxs.add(dx);
		}

		public BigDecimal getLastAtr() {
			return atrs.get(atrs.size() - 1);
		}

		public BigDecimal getLastSmoothedPlusDm() {
			return smoothedPlusDms.get(smoothedPlusDms.size() - 1);
		}

		public BigDecimal getLastSmoothedMinusDm() {
			return smoothedMinusDms.get(smoothedMinusDms.size() - 1);
		}

		public BigDecimal getLastPlusDi() {
			return plusDis.get(plusDis.size() - 1);
		}

		public BigDecimal getLastMinusDi() {
			return minusDis.get(minusDis.size() - 1);
		}

		public List<BigDecimal> getDxs() {
			return dxs;
		}

	}

}
