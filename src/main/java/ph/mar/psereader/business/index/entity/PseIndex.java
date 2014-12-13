package ph.mar.psereader.business.index.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "pse_index")
@NamedQueries({ @NamedQuery(name = PseIndex.ALL_WITH_QUOTES_BY_TYPE, query = "SELECT DISTINCT pi FROM PseIndex pi JOIN FETCH pi.quotes WHERE pi.type = :type") })
public class PseIndex implements Serializable {

	public static final String ALL_WITH_QUOTES_BY_TYPE = "PseIndex.ALL_WITH_QUOTES_BY_TYPE";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_pse_index", sequenceName = "seq_pse_index", allocationSize = 1)
	@GeneratedValue(generator = "seq_pse_index")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 4)
	private Type type;

	@OrderBy("date DESC")
	@OneToMany(mappedBy = "index", cascade = CascadeType.ALL)
	private List<PseIndexQuote> quotes;

	public PseIndex() {
		super();
	}

	public PseIndex(Type type) {
		this.type = type;
		quotes = new ArrayList<>();
	}

	public boolean add(PseIndexQuote quote) {
		return quotes.add(quote);
	}

	public boolean addAll(List<PseIndexQuote> newQuotes) {
		for (PseIndexQuote quote : newQuotes) {
			quote.setIndex(this);
		}

		return quotes.addAll(newQuotes);
	}

	public Long getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public List<PseIndexQuote> getQuotes() {
		return quotes;
	}

	@Override
	public String toString() {
		return String.format("PseIndex [id=%s, type=%s, quotes=%s]", id, type, quotes);
	}

	public static enum Type {

		FIN("Financials", Pattern.compile("Financials (.+)\n")),
		IND("Industrials", Pattern.compile("Industrials (.+)\n")),
		HLDG("Holding Firms", Pattern.compile("Holding Firms (.+)\n")),
		PRO("Property", Pattern.compile("Property (.+)\n")),
		SVC("Services", Pattern.compile("Services (.+)\n")),
		MO("Mining & Oil", Pattern.compile("Mining & Oil (.+)\n")),
		SME("Small & Medium Enterprises", Pattern.compile("SME (.+)\n")),
		ETF("Exchange Traded Funds", Pattern.compile("ETF (.+)\n")),
		PSEI("PSEi", Pattern.compile("PSEi (.+)\n")),
		ALL("All Shares", Pattern.compile("All Shares (.+)\n"));

		private String name;
		private Pattern regex;

		private Type(String name, Pattern regex) {
			this.name = name;
			this.regex = regex;
		}

		public String getName() {
			return name;
		}

		public Pattern getRegex() {
			return regex;
		}

		public static Type[] getSectoralIndeces() {
			return new Type[] { FIN, HLDG, IND, MO, PRO, SVC };
		}

	}

}
