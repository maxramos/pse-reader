package ph.mar.psereader.business.indicator.entity;

public enum ReasonType {

	BULLISH_DIP("Bullish Dip", "%K or %D falls below the Oversold level and rises back above it."),
	BEARISH_DIP("Bearish Dip", "%K or %D rises above the Overbought level then falls back below it."),
	BULLISH_CROSSOVER("Bullish Crossover", "%K crosses to above %D."),
	BEARISH_CROSSOVER("Bearish Crossover", "%K crosses to below %D."),
	OVERSOLD("Oversold", "%K or %D falls below the Oversold line."),
	OVERBOUGHT("Overbought", "%K or %D rises above the Overbought line.");

	private String name;
	private String description;

	private ReasonType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

}