import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class homework {
	static final String inputFile = "input.txt";
	static final String outputFile = "output.txt";
	static final String predicateRegex = "(?<name>[~A-Za-z]+)\\((?<args>.*)\\)\\s*$";
	static final Pattern predicatePattern = Pattern.compile(predicateRegex);
	static final String implicationRegex = "(?<premises>[^\\=]+)\\s*=>\\s*(?<conclusion>[^ ]+)$";
	static final Pattern implicationPattern = Pattern.compile(implicationRegex);
	static List<Sentence> kbList = new ArrayList<>();
	static List<Predicate> queries = new ArrayList<>();
	static Map<String, TableEntry> predicateTable = new HashMap<>();
	static Map<String, TableEntry> kbTable = new HashMap<>();
	static Set<Sentence> newClauses = new HashSet<>();
	//DEBUG
	static boolean printInput = false;
	static boolean printOutput = false;

	public static void main(String[] args) {
		long start = System.currentTimeMillis();

		int queryCount = 0;
		List<String> queryStrings = new ArrayList<>();
		int kbCount = 0;	
		List<String> kbStrings = new ArrayList<>();
		try {
			File myFile = new File(inputFile);
			Scanner scan = new Scanner(myFile);
			queryCount = Integer.valueOf(scan.nextLine());			
			for (int i = 0; i < queryCount; i++) {
				queryStrings.add(scan.nextLine());
			}
			kbCount = Integer.valueOf(scan.nextLine());			
			for (int i = 0; i < kbCount; i++) {
				String str = scan.nextLine();
				if (!kbStrings.contains(str))
					kbStrings.add(str);
			}
			scan.close();
		} catch (Exception e) {
			System.out.println("input error");
			e.printStackTrace();
		}	

		boolean[] queryResults = new boolean[queryCount];
		kbList = createKBSentences(kbStrings);
		queries = createQuerySentences(queryStrings);


		/*
		 * ------------------------Work Space-------------------------------------------------------------------
		 */	
		// TODO debug flags
		printInput = true;
		printOutput = true;
		
		if (printInput) {
			System.out.println("queries:");
			for (Predicate p : queries) {
				System.out.println(p);
			}
			System.out.println("----------\nKB:");

			for (Sentence s : kbList) {
				System.out.println(s);
			}
			System.out.println("----------");
		}

		for (int i = 0; i < queryCount; i++) {
			// query to be proved
			Predicate query = queries.get(i);

			// negated query sentence
			Predicate negaQuery = new Predicate(query);
			negaQuery.negate();
			homework.Sentence negaQuerySentence = new Sentence();
			negaQuerySentence.addPredicate(negaQuery);

			kbTable.clear();

			for (String key : predicateTable.keySet()){
				kbTable.put(key, new TableEntry(predicateTable.get(key)));
			}
			indexPredicate(kbTable, negaQuery, negaQuerySentence);
			queryResults[i] = resolution(kbList, negaQuerySentence);
			if (printOutput)
				System.out.println(query + " is " + queryResults[i]);
		}
		/*
		 * ------------------------Work Space-------------------------------------------------------------------
		 */
		try {
			FileWriter writer = new FileWriter(outputFile);
			for (int i = 0; i < queryCount; i++) {
				if (i == queryCount - 1) {
					if (queryResults[i]) {
						writer.write("TRUE");
					}else {
						writer.write("FALSE");
					}
				}else {
					if (queryResults[i]) {
						writer.write("TRUE\n");
					}else {
						writer.write("FALSE\n");
					}
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println("output error.");
			e.printStackTrace();
		}
		if (printOutput) {
			long length = System.currentTimeMillis() - start;
			System.out.println((double)length/1000 + " s");
		}
	}



	private static boolean resolution(List<Sentence> kb, Sentence query) {

		// DFS queue
		Stack<Sentence> queue = new Stack<>();
		queue.add(query);
		// DFS visited
		Set<Sentence> visited = new HashSet<>();

		//store resolved sentence pairs
		Set<String> seen = new HashSet<>();
		// DEBUG info
		int kbSize = kbList.size()+1;
		
		while (!queue.isEmpty()) {

			Sentence cur = queue.pop();

			if (!visited.contains(cur)) {

				visited.add(cur);
				if (printOutput)
					System.out.println("Current: " + cur);
				Set<Sentence> news = new HashSet<>();

				for (Predicate p : cur.predicates) {
					Set<Sentence> candidates = fetch(kbTable, p);
					for (Sentence can : candidates) {
						// if <cur:can> pair was resolved, skip
						String pair =  pairString(cur, can);
						if (seen.contains(pair)) {
							continue;
						}else {
							seen.add(pair);
						}
						
						List<Sentence> resolvents = resolve(cur, can);
						if (printOutput) {
							if (!resolvents.isEmpty()) {
								System.out.println("resolve: [ " + cur + " ] and [ " + can +" ]");
								System.out.println("resolvents: ");
								for (Sentence s : resolvents) {
									System.out.println(s);
								}
								System.out.println();
							}	
						}
							
						for (Sentence res : resolvents) {
							if (res.predicates.isEmpty()) {
								if (printOutput)
									System.out.println("Total resolution: " + seen.size() + " kb size:" +kbSize);
								return true;
							}

							// If not visited or queued
							if (!visited.contains(res) && !queue.contains(res)) {
								queue.add(res);
								news.add(res);
							}
						}
					}
				}

				// Index new sentence to kbTable
				for (Sentence newSen : news) {
					for (Predicate p : newSen.predicates) {
						indexPredicate(kbTable, p, newSen);
						kbSize++;
					}
				}
			}
		}
		
		if (printOutput)
			System.out.println("Total resolution: " + seen.size() + " kb size:" +kbSize);
		
		return false;
	}


	// Return Sentence pair in fixed order
	private static String pairString(homework.Sentence one, homework.Sentence two) {
		return one.hashCode() <= two.hashCode() ? 
				one.toString() + "%" + two.toString() : two.toString() + "%" + one.toString();
	}



	private static List<homework.Sentence> resolve(homework.Sentence cnf1, homework.Sentence cnf2) {

		List<Sentence> res = new ArrayList<>();

		List<Predicate> preds1 = cnf1.predicates;
		List<Predicate> preds2 = cnf2.predicates;
		// Holds the remaining Predicates other than the resolved 2 Predicates


		for (int i = 0; i < preds1.size(); i++) {
			for (int j = 0; j < preds2.size(); j++) {
				if (isResolvable(preds1.get(i), preds2.get(j))) {
					// collect non-resolvable predicates
					List<Predicate> remainPreds = new ArrayList<>();
					addRemainPred(remainPreds, preds1, i);
					addRemainPred(remainPreds, preds2, j);
					// apply subs to the remain predicates
					List<String[]> subs = unification(preds1.get(i), preds2.get(j));
					// non-var involved resolution does not need unification
					if (subs!=null)
						applySubs(remainPreds, subs);
					remainPreds = new ArrayList<>(new HashSet<>(remainPreds));
					Sentence s = new Sentence(remainPreds);
					// TODO check if self resolvable preds exists, ~Ready(A)
					// remove dups
					if (!res.contains(s))
						res.add(s);
				}
			}
		}

		return res;

	}


	private static void addRemainPred(List<Predicate> remainPreds, List<Predicate> preds, int excludeIndex) {
		for (int i = 0; i < preds.size() ; i++) {
			if (i == excludeIndex)
				continue;
			if (!remainPreds.contains(preds.get(i)))
				remainPreds.add(new Predicate(preds.get(i)));
		}
	}



	private static boolean isResolvable(Predicate p1, Predicate p2) {

		if (!p1.verb.equals(p2.verb))
			return false;

		if (p1.negation == p2.negation)
			return false;

		// # of arguments is assumed to be same
		if (p1.args.size() != p2.args.size())
			return false;

		// Predicates with no variable, if values do not match, e.g. Ready(A) and ~Ready(B) return false
		if (!p1.hasVariable() && !p2.hasVariable()) {
			for (int i = 0; i < p1.args.size(); i++) {
				if (!p1.args.get(i).equals(p2.args.get(i))) {
					return false;
				}
			}
		}else {
			// if the non variable part do not match, e.g. Train(Fetch,x) and ~Train(Down,Hayley) return false
			for (int i = 0; i < p1.args.size(); i++) {
				if (!p1.args.get(i).isVar() && !p2.args.get(i).isVar()) {
					if (!p1.args.get(i).equals(p2.args.get(i))) {
						return false;
					}
				}
			}
		}

		return true;
	}



	/**
	 * replace variables by the given values in the substitutions
	 * @param predicates - a list of predicate to be unified
	 * @param substitutions - a list of String pair indicating var:value
	 */
	private static void applySubs(List<homework.Predicate> predicates, List<String[]> substitutions) {

		for (Predicate p : predicates) {
			if (!p.hasVariable())
				continue;
			for (int j = 0; j< p.args.size(); j++ ) {
				Argument arg = p.args.get(j);
				for (int i = 0; i < substitutions.size(); i++) {
					// if var name matches
					if(arg.name.equals(substitutions.get(i)[0])) {
						arg.name = substitutions.get(i)[1];
						arg.constant = true;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param pred1
	 * @param pred2
	 * @return substitution option as a list of String pair indicating var:value
	 */
	private static List<String[]> unification(homework.Predicate pred1, homework.Predicate pred2) {
		if (!pred1.hasVariable() && !pred2.hasVariable()) {
			return null;
		}

		List<String[]> subs = new ArrayList<>();

		Predicate varPred = pred1;
		Predicate constantPred = pred2;
		if (!pred1.hasVariable()) {
			varPred = pred2;
			constantPred = pred1;
		}

		for (int i = 0; i < varPred.args.size(); i++) {
			if (varPred.args.get(i).isVar()) {
				String[] sub = new String[2];
				sub[0] = varPred.args.get(i).name;
				if (constantPred.args.get(i).isVar()) {
					return null;
				}
				sub[1] = constantPred.args.get(i).name;
				subs.add(sub);
			} else {
				// if non-var values are not equal
				if (!varPred.args.get(i).name.equals(constantPred.args.get(i).name)) {
					return null;
				}
			}
		}

		return subs;
	}

	/**
	 * fetch all the sentences from kb that contain complementary predicates of the query
	 * @param query - Predicate
	 * @return
	 */
	private static Set<Sentence> fetch(Map<String, TableEntry> table, Predicate query) {
		String verb = query.verb;
		// if query has negation sign ~
		if (query.negation) {
			return table.get(verb).positive;
		}else {
			return table.get(verb).negative;
		}

	}

	/**
	 * Populate inference engine kb from a list of input Strings
	 * @param kbStrings - List String
	 * @return List Sentence contain all the kb sentences
	 */
	private static List<homework.Sentence> createKBSentences(List<String> kbStrings) {
		List<Sentence> sentences = new ArrayList<>();

		Matcher matcher = implicationPattern.matcher("");
		String premisesStr = "";
		List<String> premises = new ArrayList<>();
		String conclusion = "";

		for (String str : kbStrings) {
			Sentence sentence = new Sentence();
			if (str.contains("=>")) {
				List<Predicate> predicates = new ArrayList<>();
				Map<String, Predicate> preds = new HashMap<>();

				matcher = implicationPattern.matcher(str);
				while(matcher.find()) {
					premisesStr = matcher.group("premises");
					conclusion = matcher.group("conclusion");
				}
				premises = new ArrayList<>();
				premises = Arrays.asList(premisesStr.split("\\s*&\\s*"));
				for (String p : premises) {
					Predicate literal = strToPredicate(p);
					literal.negate();
					indexPredicate(predicateTable, literal, sentence);
					preds.put(literal.verb, literal);
					predicates.add(literal);
				}
				Predicate con = strToPredicate(conclusion);
				indexPredicate(predicateTable, con, sentence);
				preds.put(con.verb, con);
				predicates.add(con);
				sentence.populateSentence(predicates);
				sentences.add(sentence);

			}else {
				Predicate p = strToPredicate(str);
				indexPredicate(predicateTable, p, sentence);
				sentence.addPredicate(p);
				sentences.add(sentence);
			}
		}

		return sentences;
	}

	/**
	 * Index predicate in kb sentences for faster fetch
	 * @param predicate
	 * @param sentence
	 */
	private static void indexPredicate(Map<String, TableEntry> table, homework.Predicate predicate, homework.Sentence sentence) {

		if (table.containsKey(predicate.verb)) {
			TableEntry te = table.get(predicate.verb);
			if (predicate.negation) {
				te.negative.add(sentence);
			}else {
				te.positive.add(sentence);
			}
		}else {
			TableEntry te = new TableEntry(predicate.verb);
			if (predicate.negation) {
				te.negative.add(sentence);
			}else {
				te.positive.add(sentence);
			}
			table.put(predicate.verb, te);
		}

	}

	/**
	 * convert String to Predicate
	 * @param str - String
	 * @return predicate
	 */
	private static homework.Predicate strToPredicate(String str) {
		Matcher matcher = predicatePattern.matcher(str);
		String name = "";
		List<String> argStrs = new ArrayList<String>();

		while(matcher.find()) {
			name = matcher.group("name");
			argStrs = Arrays.asList(matcher.group("args").split("\\s*,\\s*"));
		}

		return new Predicate(name, argStrs);
	}

	private static List<homework.Predicate> createQuerySentences(List<String> queryStrings) {
		List<Predicate> queries = new ArrayList<>();

		for (String str : queryStrings) {
			queries.add(strToPredicate(str));
		}

		return queries;
	}

	// ----------------------------- Classes -----------------------------
	/**
	 * Hashmap Entry stores sentences that contain a predicate in positive and negative form 
	 * @author Menghan Xu
	 *
	 */
	public static class TableEntry{
		String key;	// name of the predicate
		Set<Sentence> positive = new LinkedHashSet<>();;	// List of sentences that contains key predicate as positive
		Set<Sentence> negative = new LinkedHashSet<>();;	// List of sentences that contains key predicate as positive

		public TableEntry(String predicate) {
			this.key = predicate;
		}

		public TableEntry(TableEntry te) {
			this.key = te.key;
			for (Sentence pos : te.positive){
				this.positive.add(new Sentence(pos));
			}
			for (Sentence neg : te.negative){
				this.negative.add(new Sentence(neg));
			}
		}
	}

	/**
	 * Sentence object represents CNF sentence
	 * @author Menghan Xu
	 *
	 */
	public static  class Sentence {
		boolean implication;
		List<Predicate> predicates;
		//		Set<Predicate> predicates;

		public Sentence() {
			this.implication = false;
			this.predicates = new ArrayList<>();
			//			this.predicates = new HashSet<>();
		}

		public Sentence(Sentence target){
			this.implication = target.implication;
			this.predicates = new ArrayList<>();
			//			this.predicates = new HashSet<>();
			for (Predicate p : target.predicates){
				this.predicates.add(new Predicate(p));
			}
		}

		// Create a senstence based on a give list of predicates
		public Sentence(List<Predicate> preds) {
			this.implication = preds.size() > 1;
			this.predicates = preds;
		}

		public boolean isComplementry(homework.Sentence sus) {
			for (Predicate p : this.predicates) {
				for (Predicate susP : sus.predicates) {
					if (!p.sameVerb(susP.verb))
						continue;
					if (!p.conflicts(susP)){
						return false;
					}
				}
			}		
			return true;
		}

		public void populateSentence(List<homework.Predicate> predicates) {
			this.implication = true;
			//			this.predicates = predicates;
			this.predicates.addAll(predicates);
		}

		public void addPredicate(Predicate literal) {
			this.predicates.add(literal);
		}

		public String prologForm() {
			String output = "";
			if (predicates.size() == 1) {
				Predicate p = this.predicates.get(0);
				if (p.negation) {
					output = "NOT " + this.predicates.get(0).prologForm()+".";
				}else {
					output = this.predicates.get(0).prologForm()+".";
				}
			}else {
				output += this.predicates.get(this.predicates.size()-1).prologForm() + " :-";
				for (int i = 0; i < this.predicates.size()-1; i++) {
					output += " " + this.predicates.get(i).prologForm() +",";
				}
			}
			output = output.substring(0,output.length()-1);
			output += ".";
			return output;
		}

		@Override
		public String toString() {
			//			String str = implication ? "CNF: " : "CNF: ";
			String str = "";
			if (predicates.isEmpty()) {
				return "empty clause";
			}
			if (implication) {
				// List toString
				//				for (int i = 0; i < this.predicates.size()-1; i++) {
				//					str += predicates.get(i)+ " | ";
				//				}
				//				str += predicates.get(predicates.size()-1);
				for (Predicate pred : this.predicates) {
					str += pred + " | ";
				}
				str = str.substring(0, str.length()-3);
			}else {
				for (Predicate l : this.predicates) {
					str += l;
				}
			}
			return str;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!(obj instanceof Sentence)) return false;

			Sentence s2 = (Sentence)obj;
			return this.toString().equals(s2.toString());
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.toString());
		}
	}

	/**
	 * Predicate(arg1, arg2..argn)
	 * verb represents the meaning of predicate
	 * args are a list of Arguments of the predicate
	 * negation is true when the predicate comes with negate sign "~"
	 * @author Menghan Xu
	 *
	 */
	public static class Predicate{
		String verb;
		List<Argument> args = new ArrayList<Argument>();
		boolean negation = false;
		
		public Predicate(String verb, List<String> args) {
			if (verb.charAt(0) == '~') {
				this.negation = true;
				this.verb = verb.substring(1);
			}else {
				this.verb = verb;
			}
			for (String s : args) {
				this.args.add(new Argument(s));
			}
		}

		public String prologForm() {
			String str = "";
			str += this.verb.toLowerCase() + "(";
			int n = this.args.size();
			for (int i = 0; i < n; i++) {
				if (i == n-1) {
					if (this.args.get(i).isVar()) {
						str += this.args.get(i).name.toUpperCase();
					}else {
						str += this.args.get(i).name.toLowerCase();
					}
				}else {
					if (this.args.get(i).isVar()) {
						str += this.args.get(i).name.toUpperCase()+", ";
					}else {
						str += this.args.get(i).name.toLowerCase()+", ";
					}
				}
			}	
			return str+")";
		}

		public boolean conflicts(homework.Predicate susP) {
			if (!this.sameVerb(susP.verb))
				return false;
			if (this.negation == susP.negation)
				return false;
			for (Argument arg : this.args) {
				for (Argument susArg : susP.args) {
					if (!arg.equals(susArg))
						return false;
				}
			}
			return true;
		}

		public boolean sameVerb(String predicate) {
			return this.verb.equals(predicate);
		}

		public boolean hasVariable() {
			for (Argument arg : args) {
				if (arg.isVar()) {
					return true;
				}
			}
			return false;
		}

		// Create a new Predicate with identical info
		public Predicate(Predicate p) {
			this.verb = p.verb;
			this.negation = p.negation;
			this.args = new ArrayList<Argument>();
			for (Argument arg : p.args) {
				this.args.add(new Argument(arg.name));
			}
		}

		public void negate() {
			this.negation = this.negation ? false : true;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!(obj instanceof Predicate)) return false;

			Predicate p2 = (Predicate)obj;
			return this.toString().equals(p2.toString());
		}

		@Override
		public String toString() {
			String str = negation ? "~" : "";
			str += this.verb + "(";
			int n = this.args.size();
			for (int i = 0; i < n; i++) {
				if (i == n-1) {
					str += this.args.get(i).name;
				}else {
					str += this.args.get(i).name+",";
				}
			}	
			return str+")";
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.toString());
		}
	}

	public static class Argument{
		String name;
		boolean constant;

		public Argument(String name) {
			this.name = name;
			if (Character.isUpperCase(name.charAt(0))) {
				this.constant = true;
			}else {
				this.constant = false;
			}
		}

		public boolean isVar() {
			return !this.constant;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (!(obj instanceof Argument)) return false;

			Argument a = (Argument)obj;
			return (this.name.equals(a.name));
		}

		@Override
		public String toString() {
			String str = "";
			if (constant) {
				str += "constant: ";
			}else {
				str += "variable: ";
			}	
			return str + this.name;
		}

	}
}
