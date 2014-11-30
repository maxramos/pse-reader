package ph.mar.psereader.business.stock.entity;

import java.math.BigDecimal;

public enum BoardLotAndPriceFluctuation {

	_0_0001___0_0099(new BigDecimal("0.0001"), new BigDecimal("0.0099"), 1_000_000, new BigDecimal("0.0001")),
	_0_01___0_049(new BigDecimal("0.01"), new BigDecimal("0.049"), 100_000, new BigDecimal("0.001")),
	_0_05___0_249(new BigDecimal("0.05"), new BigDecimal("0.249"), 10_000, new BigDecimal("0.001")),
	_0_25___0_495(new BigDecimal("0.25"), new BigDecimal("0.495"), 10_000, new BigDecimal("0.005")),
	_0_50___4_99(new BigDecimal("0.50"), new BigDecimal("4.99"), 1_000, new BigDecimal("0.01")),
	_5_00___9_99(new BigDecimal("5.00"), new BigDecimal("9.99"), 100, new BigDecimal("0.01")),
	_10_00___19_98(new BigDecimal("10.00"), new BigDecimal("19.98"), 100, new BigDecimal("0.02")),
	_20_00___49_95(new BigDecimal("20.00"), new BigDecimal("49.95"), 100, new BigDecimal("0.05")),
	_50_00___99_95(new BigDecimal("50.00"), new BigDecimal("99.95"), 10, new BigDecimal("0.05")),
	_100___199_9(new BigDecimal("100"), new BigDecimal("199.9"), 10, new BigDecimal("0.10")),
	_200___499_8(new BigDecimal("200"), new BigDecimal("499.8"), 10, new BigDecimal("0.20")),
	_500___999_5(new BigDecimal("500"), new BigDecimal("999.5"), 10, new BigDecimal("0.50")),
	_1_000___1_999(new BigDecimal("1000"), new BigDecimal("1999"), 5, new BigDecimal("1.00")),
	_2_000___4_998(new BigDecimal("2000"), new BigDecimal("4998"), 5, new BigDecimal("2.00")),
	_5_000___ABOVE(new BigDecimal("5000"), new BigDecimal("50000"), 5, new BigDecimal("5.00"));

	private BigDecimal floor;
	private BigDecimal ceiling;
	private int boardLot;
	private BigDecimal priceFluctuation;

	private BoardLotAndPriceFluctuation(BigDecimal floor, BigDecimal ceiling, int boardLot, BigDecimal priceFluctuation) {
		this.floor = floor;
		this.ceiling = ceiling;
		this.boardLot = boardLot;
		this.priceFluctuation = priceFluctuation;
	}

	public static int determineBoardLot(BigDecimal price) {
		BoardLotAndPriceFluctuation[] values = BoardLotAndPriceFluctuation.values();

		for (BoardLotAndPriceFluctuation value : values) {
			// FLOOR <= PRICE <= CEILING
			if (value.getFloor().compareTo(price) <= 0 && price.compareTo(value.getCeiling()) <= 0) {
				return value.getBoardLot();
			}
		}

		throw new RuntimeException("No board lot found.");
	}

	public static BigDecimal determinePriceFluctuation(BigDecimal price) {
		BoardLotAndPriceFluctuation[] values = BoardLotAndPriceFluctuation.values();

		for (BoardLotAndPriceFluctuation value : values) {
			// FLOOR <= PRICE <= CEILING
			if (value.getFloor().compareTo(price) <= 0 && price.compareTo(value.getCeiling()) <= 0) {
				return value.getPriceFluctuation();
			}
		}

		throw new RuntimeException("No price fluctuation found.");
	}

	public BigDecimal getFloor() {
		return floor;
	}

	public BigDecimal getCeiling() {
		return ceiling;
	}

	public int getBoardLot() {
		return boardLot;
	}

	public BigDecimal getPriceFluctuation() {
		return priceFluctuation;
	}

}
