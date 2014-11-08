package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class DmiResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "dmi_adx", nullable = false, precision = 13, scale = 10)
	private BigDecimal adx;

	@Column(name = "dmi_plus_di", nullable = false, precision = 13, scale = 10)
	private BigDecimal plusDi;

	@Column(name = "dmi_minus_di", nullable = false, precision = 13, scale = 10)
	private BigDecimal minusDi;

	@Enumerated(EnumType.STRING)
	@Column(name = "dmi_trend", nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(name = "dmi_position", nullable = false, length = 13)
	private PositionType position;

	@Column(name = "dmi_smoothed_plus_dm", nullable = false, precision = 13, scale = 10)
	private BigDecimal smoothedPlusDm;

	@Column(name = "dmi_smoothed_minus_dm", nullable = false, precision = 13, scale = 10)
	private BigDecimal smoothedMinusDm;

	@Column(name = "dmi_atr", nullable = false, precision = 13, scale = 10)
	private BigDecimal atr;

	public DmiResult() {
		super();
	}

	public DmiResult(BigDecimal adx, BigDecimal plusDi, BigDecimal minusDi, TrendType trend, PositionType position, BigDecimal smoothedPlusDm,
			BigDecimal smoothedMinusDm, BigDecimal atr) {
		this.adx = adx;
		this.plusDi = plusDi;
		this.minusDi = minusDi;
		this.trend = trend;
		this.position = position;
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

	public TrendType getTrend() {
		return trend;
	}

	public PositionType getPosition() {
		return position;
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
		return String.format("DmiResult [adx=%s, plusDi=%s, minusDi=%s, trend=%s, position=%s, smoothedPlusDm=%s, smoothedMinusDm=%s, atr=%s]", adx,
				plusDi, minusDi, trend, position, smoothedPlusDm, smoothedMinusDm, atr);
	}

	public static class Holder {

		private List<BigDecimal> trList;
		private List<BigDecimal> plusDmList;
		private List<BigDecimal> minusDmList;

		public Holder(int size) {
			trList = new ArrayList<>(size);
			plusDmList = new ArrayList<>(size);
			minusDmList = new ArrayList<>(size);
		}

		public void add(BigDecimal tr, BigDecimal plusDm, BigDecimal minusDm) {
			trList.add(tr);
			plusDmList.add(plusDm);
			minusDmList.add(minusDm);
		}

		public void reverse() {
			Collections.reverse(trList);
			Collections.reverse(plusDmList);
			Collections.reverse(minusDmList);
		}

		public List<BigDecimal> getTrList() {
			return trList;
		}

		public List<BigDecimal> getPlusDmList() {
			return plusDmList;
		}

		public List<BigDecimal> getMinusDmList() {
			return minusDmList;
		}

	}

	public static class SmoothedHolder {

		private List<BigDecimal> atrList;
		private List<BigDecimal> smoothedPlusDmList;
		private List<BigDecimal> smoothedMinusDmList;
		private List<BigDecimal> plusDiList;
		private List<BigDecimal> minusDiList;
		private List<BigDecimal> dxList;

		public SmoothedHolder(int size) {
			atrList = new ArrayList<>(size);
			smoothedPlusDmList = new ArrayList<>(size);
			smoothedMinusDmList = new ArrayList<>(size);
			plusDiList = new ArrayList<>(size);
			minusDiList = new ArrayList<>(size);
			dxList = new ArrayList<>(size);
		}

		public void add(BigDecimal atr, BigDecimal smoothedPlusDm, BigDecimal smoothedMinusDm, BigDecimal plusDi, BigDecimal minusDi, BigDecimal dx) {
			atrList.add(atr);
			smoothedPlusDmList.add(smoothedPlusDm);
			smoothedMinusDmList.add(smoothedMinusDm);
			plusDiList.add(plusDi);
			minusDiList.add(minusDi);
			dxList.add(dx);
		}

		public BigDecimal getLastAtr() {
			return atrList.get(atrList.size() - 1);
		}

		public BigDecimal getLastSmoothedPlusDm() {
			return smoothedPlusDmList.get(smoothedPlusDmList.size() - 1);
		}

		public BigDecimal getLastSmoothedMinusDm() {
			return smoothedMinusDmList.get(smoothedMinusDmList.size() - 1);
		}

		public BigDecimal getLastPlusDi() {
			return plusDiList.get(plusDiList.size() - 1);
		}

		public BigDecimal getLastMinusDi() {
			return minusDiList.get(minusDiList.size() - 1);
		}

		public BigDecimal getLastDx() {
			return dxList.get(dxList.size() - 1);
		}

		public List<BigDecimal> getAtrList() {
			return atrList;
		}

		public List<BigDecimal> getSmoothedPlusDmList() {
			return smoothedPlusDmList;
		}

		public List<BigDecimal> getSmoothedMinusDmList() {
			return smoothedMinusDmList;
		}

		public List<BigDecimal> getPlusDiList() {
			return plusDiList;
		}

		public List<BigDecimal> getMinusDiList() {
			return minusDiList;
		}

		public List<BigDecimal> getDxList() {
			return dxList;
		}

	}

}
