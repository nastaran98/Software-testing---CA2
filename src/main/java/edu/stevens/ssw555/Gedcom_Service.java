
// Refactored code.

package edu.stevens.ssw555;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Gedcom_Service {

	public static HashMap<String, Family> families = new HashMap<String, Family>();
	public static HashMap<String, Individual> individuals = new HashMap<String, Individual>();
	public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	public static String fileName = null;
	public static ArrayList<Individual> dupInd = new ArrayList<Individual>();
	public static ArrayList<Family> dupFam = new ArrayList<Family>();

	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("Please Enter the Input File Path with filename: ");
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String fileName = bufferRead.readLine();
			createOutputFile();
			readAndParseFile(fileName);
			//printMaps();
			// User Story 3
			birthBeforeDeath(individuals);
			// User Story 4
			Marriagebeforedivorce(individuals, families);
			// User Story 8
			birthbeforemarriageofparent(individuals, families);
			// User Story 16
			Malelastname(families);
			// User Story 20
			AuntsandUnclesname(families);
			// User Story 24
			uniqueFamilynameBySpouses(individuals, families);

		} catch (FileNotFoundException ex) {
			System.out.println("File Not Found. Please reenter path");
			main(null);
		}
	}

	public static void createOutputFile() throws IOException {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		createOutputFile(bufferRead);
	}

	public static void createOutputFile(BufferedReader bufferRead) throws IOException {
		while (true) {
			System.out.println("Please Enter Output File Path: ");
			String fp = bufferRead.readLine();
			if (fp != null && !fp.isEmpty()) {
				Path filePath = Paths.get(fp);
				if (Files.exists(filePath)) {
					fileName = fp + "/GedcomService_output.txt";
					try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)))) {
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				} else {
					System.out.println("The Path You Entered Does Not Exist. Reenter path");
				}
			} else {
				System.out.println("Invalid path. Please reenter.");
			}
		}
	}


	static void readAndParseFile(String fileName) throws IOException {
		String[] validTags = {"INDI", "NAME", "SEX", "BIRT", "DEAT", "FAMC", "FAMS", "FAM", "MARR", "HUSB", "WIFE", "CHIL", "DIV", "DATE", "HEAD", "TRLR", "NOTE"};
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = br.readLine();

		while (line != null) {
			String[] parts = line.split(" ");

			if (Arrays.asList(validTags).contains(parts[1])) {
				line = br.readLine();
			} else {
				if (parts[0].equals("0") && Arrays.asList(validTags).contains(parts[2])) {
					if (parts[2].equals("INDI")) {
						Individual indi = new Individual(parts[1]);
						String individualParts = br.readLine();
						while (individualParts != null && !individualParts.startsWith("0")) {
							String[] indParts = individualParts.split(" ");
							if (indParts.length > 1) {
								if (indParts[1].equals("NAME"))
									indi.setName(indParts[2] + " " + indParts[3].substring(1, indParts[3].length() - 1));
								if (indParts[1].equals("SEX"))
									indi.setSex(indParts[2]);
								if (indParts[1].equals("FAMS"))
									indi.setSpouseOf(indParts[2]);
								if (indParts[1].equals("FAMC"))
									indi.setChildOf(indParts[2]);
								if (indParts[1].equals("BIRT")) {
									individualParts = br.readLine();
									indParts = individualParts.split(" ");
									if (indParts.length > 3) {
										String month = getMonth(indParts[3]);
										indi.setBirth(month + "/" + indParts[2] + "/" + indParts[4]);
									}
								}
								if (indParts[1].equals("DEAT") && indParts[2].equals("Y")) {
									individualParts = br.readLine();
									indParts = individualParts.split(" ");
									if (indParts.length > 3) {
										String month = getMonth(indParts[3]);
										indi.setDeath(month + "/" + indParts[2] + "/" + indParts[4]);
									}
								}
							}
							individualParts = br.readLine();
						}
						line = individualParts;
						if (!individuals.containsKey(indi.getId())) {
							individuals.put(indi.getId(), indi);
						} else {
							dupInd.add(indi);
						}
					} else if (parts[2].equals("FAM")) {
						ArrayList<String> children = new ArrayList<String>();
						Family fam = new Family(parts[1]);
						String familyParts = br.readLine();
						while (familyParts != null && !familyParts.startsWith("0")) {
							String[] indFamParts = familyParts.split(" ");
							if (indFamParts.length > 1) {
								if (indFamParts[1].equals("HUSB"))
									fam.setHusb(indFamParts[2]);
								if (indFamParts[1].equals("WIFE"))
									fam.setWife(indFamParts[2]);
								if (indFamParts[1].equals("CHIL")) {
									children.add(indFamParts[2]);
									fam.setChild(children);
								}
								if (indFamParts[1].equals("MARR")) {
									familyParts = br.readLine();
									indFamParts = familyParts.split(" ");
									if (indFamParts.length > 3) {
										String month = getMonth(indFamParts[3]);
										fam.setMarriage(month + "/" + indFamParts[2] + "/" + indFamParts[4]);
									}
								}
								if (indFamParts[1].equals("DIV")) {
									familyParts = br.readLine();
									indFamParts = familyParts.split(" ");
									if (indFamParts.length > 3) {
										String month = getMonth(indFamParts[3]);
										fam.setDivorce(month + "/" + indFamParts[2] + "/" + indFamParts[4]);
									}
								}
							}
							familyParts = br.readLine();
						}
						if (!families.containsKey(fam.getId())) {
							families.put(fam.getId(), fam);
						} else {
							dupFam.add(fam);
						}
						line = familyParts;
					}
				} else {
					line = br.readLine();
				}
			}
		}
		br.close();
	}
	public static void writeToFile(String output) throws FileNotFoundException, IOException {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)))) {
			out.println(output);
			System.out.println(output);
		} catch (IOException e) {

		}

	}


	//UserStory 03 implementation
	//Refactored code for method

	static void birthBeforeDeath(HashMap<String, Individual> individuals) throws FileNotFoundException, IOException {

		Map<String, Individual> map = new HashMap<String, Individual>(individuals);
		Iterator<Map.Entry<String, Individual>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, Individual> entry = entries.next();
			Individual indi = entry.getValue();

			Date date_of_birth = null;
			Date date_of_death = null;
			try {
				date_of_birth = sdf.parse(indi.getBirth());
				if (indi.getDeath() != null)
					date_of_death = sdf.parse(indi.getDeath());
				else
					date_of_death = null;
			} catch (ParseException e) {

				e.printStackTrace();
			}
			// Compare two dates
			if (date_of_death != null)
				if (date_of_birth.compareTo(date_of_death) > 0) {
					System.out.println("\n");
					writeToFile(
							"ERROR:INDIVIDUAL: User Story US03: Birth Before Death \nIndividual: "
									+ indi.getId() + " - " + indi.getName() + " was born after death\nDOB: "
									+ indi.getBirth() + " DOD: " + indi.getDeath()
									+ "\n");
				}
		}
	}
	//UserStory 04 implementation
	//Refactored code for method
	static void Marriagebeforedivorce(HashMap<String, Individual> individuals, HashMap<String, Family> families)
			throws FileNotFoundException, IOException {

		Date marriageDate = null;
		Date divorceDate = null;
		Map<String, Family> famMap = new HashMap<String, Family>(families);
		Map<String, Individual> indMap = new HashMap<String, Individual>(individuals);
		Iterator<Map.Entry<String, Family>> famEntries = famMap.entrySet().iterator();
		while (famEntries.hasNext()) {
			Map.Entry<String, Family> famEntry = famEntries.next();
			Family fam = famEntry.getValue();
			try {

				if (fam.getDivorce() != null) {
					divorceDate = sdf.parse(fam.getDivorce());
					if (fam.getMarriage() == null)
						System.out.println("marriage date is null");
					marriageDate = sdf.parse(fam.getMarriage());
					if (divorceDate.before(marriageDate)) {
						writeToFile(
								"ERROR:FAMILY: User Story US04: Marriage Before Divorce \nFamily: "
										+ fam.getId() + "\nIndividual: " + fam.getHusb() + ": "
										+ indMap.get(fam.getHusb()).getName() + fam.getWife() + ": "
										+ indMap.get(fam.getWife()).getName()
										+ " marriage date is before divorce date.\nMarriage Date: " + fam.getMarriage()
										+ " Divorce Date: " + fam.getDivorce()
										+ "\n");
					}
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	//UserStory 08 implementation
	static void birthbeforemarriageofparent(HashMap<String, Individual> individuals, HashMap<String, Family> families)
			throws FileNotFoundException, IOException {
		Date marriageDate = null;
		Date divorceDate = null;
		Date birthDate = null;
		Map<String, Individual> indMap = new HashMap<String, Individual>(individuals);
		Map<String, Family> famMap = new HashMap<String, Family>(families);
		Iterator<Map.Entry<String, Family>> famEntries = famMap.entrySet().iterator();
		while (famEntries.hasNext()) {
			Map.Entry<String, Family> famEntry = famEntries.next();
			Family fam = famEntry.getValue();
			try {
				marriageDate = sdf.parse(fam.getMarriage());
				if (fam.getChild() != null) {
					for (int i = 0; i < fam.getChild().size(); i++) {
						Individual indi = indMap.get(fam.getChild().get(i));
						birthDate = sdf.parse(indi.getBirth());
						if (birthDate.before(marriageDate)) {
							writeToFile(
									"ERROR: User Story US08: Birth Before Marriage Date \nFamily ID: "
											+ fam.getId() + "\nIndividual: " + indi.getId() + ": " + indi.getName()
											+ " Has been born before parents' marriage\nDOB: " + indi.getBirth()
											+ " Parents Marriage Date: " + fam.getMarriage()
											+ "\n\n");
						}
						if (fam.getDivorce() != null) {
							divorceDate = sdf.parse(fam.getDivorce());
							if (birthDate.after(divorceDate)) {
								writeToFile(
										"ERROR: User Story US08: Birth After Divorce Date\nFamily ID: "
												+ fam.getId() + "\nIndividual: " + indi.getId() + ": " + indi.getName()
												+ " Has been born after parents' divorce\nDOB: " + indi.getBirth()
												+ " Parents Divorce Date: " + fam.getDivorce()
												+ "\n\n");
							}
						}
					}
				}
			} catch (ParseException e) {

				e.printStackTrace();
			}
		}
	}

//UserStory 16 Implementation
	static void Malelastname(HashMap<String, Family> families)
		throws ParseException, FileNotFoundException, IOException {

	for (Family fam : families.values()) {
		if (fam.getChild() != null && fam.getChild().size() > 1) {
			String maleLastName = null;
			boolean isConsistent = true;

			for (String childId : fam.getChild()) {
				Individual child = individuals.get(childId);
				if (child != null && "M".equals(child.getSex())) {
					String[] nameParts = child.getName().split(" ");
					String lastName = nameParts[nameParts.length - 1];

					if (maleLastName == null) {
						maleLastName = lastName;
					} else if (!maleLastName.equals(lastName)) {
						isConsistent = false;
						break;
					}
				}
			}

			if (!isConsistent) {
				writeToFile("ERROR: User Story US16: Male last name \nFamily ID: "
						+ fam.getId() + " family members don't have the same last name \n\n");
				}
			}
		}
	}

	//UserStory 20 Implementation
	static void AuntsandUnclesname(HashMap<String, Family> families)
			throws ParseException, FileNotFoundException, IOException {

		for (Family fam : families.values()) {
			String fatherId = fam.getHusb();
			String motherId = fam.getWife();
			Individual father = individuals.get(fatherId);
			Individual mother = individuals.get(motherId);

			ArrayList<String> auntsUncles = new ArrayList<>();

			if (father != null && father.getChildOf() != null) {
				Family fatherFamily = families.get(father.getChildOf());
				if (fatherFamily != null && fatherFamily.getChild() != null) {
					auntsUncles.addAll(fatherFamily.getChild());
				}
			}

			if (mother != null && mother.getChildOf() != null) {
				Family motherFamily = families.get(mother.getChildOf());
				if (motherFamily != null && motherFamily.getChild() != null) {
					auntsUncles.addAll(motherFamily.getChild());
				}
			}

			if (fam.getChild() != null) {
				for (String childId : fam.getChild()) {
					Individual child = individuals.get(childId);
					if (child != null && child.getSpouseOf() != null) {
						Family childFam = families.get(child.getSpouseOf());
						if (childFam != null) {
							String spouseId = childFam.getHusb().equals(child.getId()) ? childFam.getWife() : childFam.getHusb();
							if (auntsUncles.contains(spouseId)) {
								Individual spouse = individuals.get(spouseId);
								writeToFile("ERROR: User Story US20: Aunts and Uncles\nIndividual: "
										+ child.getId() + " - " + child.getName()
										+ " is married to either their aunt or uncle " + spouse.getId() + " - "
										+ spouse.getName() + "\n\n");
							}
						}
					}
				}
			}
		}
	}

	//UserStory 24 Implementation
	static void uniqueFamilynameBySpouses(HashMap<String, Individual> individuals, HashMap<String, Family> families)
			throws FileNotFoundException, IOException {

		for (Map.Entry<String, Family> famEntry : families.entrySet()) {
			Family fam = famEntry.getValue();
			for (Map.Entry<String, Family> famEntry2 : families.entrySet()) {
				Family fam2 = famEntry2.getValue();
				if (!fam.getId().equals(fam2.getId()) &&
						fam.getHusb() != null && fam2.getHusb() != null &&
						fam.getWife() != null && fam2.getWife() != null &&
						fam.getHusb().equals(fam2.getHusb()) &&
						fam.getWife().equals(fam2.getWife()) &&
						fam.getMarriage().equals(fam2.getMarriage())) {

					writeToFile("ERROR: User Story US24: Unique Families By Spouse :\n"
							+ fam.getId() + ": Husband Name: " + individuals.get(fam.getHusb()).getName()
							+ ", Wife Name: " + individuals.get(fam.getWife()).getName() + " and "
							+ fam2.getId() + ": Husband Name: " + individuals.get(fam.getHusb()).getName()
							+ ", Wife Name: " + individuals.get(fam.getWife()).getName() + "\n"
							+ " have the same spouses and marriage dates: " + fam.getMarriage() + "\n");
				}
			}
		}
	}


public static String getMonth(String month) {
	String numMonth = null;
	switch (month) {
	case "JAN":
		numMonth = "01";
		break;
	case "FEB":
		numMonth = "02";
		break;
	case "MAR":
		numMonth = "03";
		break;
	case "APR":
		numMonth = "04";
		break;
	case "MAY":
		numMonth = "05";
		break;
	case "JUN":
		numMonth = "06";
		break;
	case "JUL":
		numMonth = "07";
		break;
	case "AUG":
		numMonth = "08";
		break;
	case "SEP":
		numMonth = "09";
		break;
	case "OCT":
		numMonth = "10";
		break;
	case "NOV":
		numMonth = "11";
		break;
	case "DEC":
		numMonth = "12";
		break;
	}
	return numMonth;
}
}
