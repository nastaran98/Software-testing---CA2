package edu.stevens.ssw555;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;

import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

public class Gedcom_ServiceTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() throws IOException {
        System.setOut(new PrintStream(outputStreamCaptor));

        Gedcom_Service.fileName = "test_output.txt";
        try (FileWriter writer = new FileWriter(Gedcom_Service.fileName)) {
        }

        Gedcom_Service.individuals.clear();
        Gedcom_Service.families.clear();
    }

    @Test
    void testBirthBeforeDeath() throws IOException, ParseException {
        Individual ind = new Individual("I1");
        ind.setName("Nastaran Ashoori");
        ind.setBirth("01/01/1990");
        ind.setDeath("01/01/1985");
        Gedcom_Service.individuals.put(ind.getId(), ind);

        Gedcom_Service.birthBeforeDeath(Gedcom_Service.individuals);

        String expectedOutput = "ERROR:INDIVIDUAL: User Story US03: Birth Before Death \nIndividual: I1 - Nastaran Ashoori was born after death\nDOB: 01/01/1990 DOD: 01/01/1985\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }

    @Test
    void testMarriageBeforeDivorce() throws IOException, ParseException {
        // Set up a family with marriage date after the divorce date
        Family fam = new Family("F1");
        fam.setMarriage("01/01/2000");
        fam.setDivorce("01/01/1990");
        fam.setHusb("I1");
        fam.setWife("I2");
        Gedcom_Service.families.put(fam.getId(), fam);

        // Set up the individuals involved in the family
        Individual husb = new Individual("I1");
        husb.setName("Ali");
        Individual wife = new Individual("I2");
        wife.setName("Fatemeh");

        Gedcom_Service.individuals.put(husb.getId(), husb);
        Gedcom_Service.individuals.put(wife.getId(), wife);

        Gedcom_Service.Marriagebeforedivorce(Gedcom_Service.individuals, Gedcom_Service.families);

        String expectedOutput = "ERROR:FAMILY: User Story US04: Marriage Before Divorce \nFamily: F1\nIndividual: I1: AliI2: Fatemeh marriage date is before divorce date.\nMarriage Date: 01/01/2000 Divorce Date: 01/01/1990\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }

    @Test
    void testBirthBeforeMarriageOfParent() throws IOException, ParseException {

        Individual husb = new Individual("I1");
        husb.setName("Ali");
        Individual wife = new Individual("I2");
        wife.setName("Fatemeh");

        Family fam = new Family("F1");
        fam.setMarriage("01/01/2000");
        Individual child = new Individual("I3");
        child.setBirth("01/01/1990");
        ArrayList<String> children = new ArrayList<>();
        children.add(child.getId());
        fam.setChild(children);
        Gedcom_Service.individuals.put(child.getId(), child);

        Gedcom_Service.individuals.put(husb.getId(), husb);
        Gedcom_Service.individuals.put(wife.getId(), wife);

        Gedcom_Service.families.put(fam.getId(), fam);

        Gedcom_Service.birthbeforemarriageofparent(Gedcom_Service.individuals, Gedcom_Service.families);

        String expectedOutput = "ERROR: User Story US08: Birth Before Marriage Date \nFamily ID: F1\nIndividual: I3: null Has been born before parents' marriage\nDOB: 01/01/1990 Parents Marriage Date: 01/01/2000\n\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }

    @Test
    void testMaleLastName() throws IOException, ParseException {
        Family fam = new Family("F1");
        Individual child1 = new Individual("I1");
        child1.setName("John Doe");
        child1.setSex("M");

        Individual child2 = new Individual("I2");
        child2.setName("Mike Smith");
        child2.setSex("M");

        ArrayList<String> children = new ArrayList<>();
        children.add(child1.getId());
        children.add(child2.getId());
        fam.setChild(children);

        Gedcom_Service.individuals.put(child1.getId(), child1);
        Gedcom_Service.individuals.put(child2.getId(), child2);

        Gedcom_Service.families.put(fam.getId(), fam);

        Gedcom_Service.Malelastname(Gedcom_Service.families);

        String expectedOutput = "ERROR: User Story US16: Male last name \nFamily ID: F1 family members don't have the same last name \n\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }

    @Test
    void testAuntsAndUnclesName() throws IOException, ParseException {
        // Parents' family
        Family parentFam = new Family("F1");
        Individual father = new Individual("I1");
        father.setName("Ali");
        Individual mother = new Individual("I2");
        mother.setName("Fatemeh");
        parentFam.setHusb(father.getId());
        parentFam.setWife(mother.getId());

        // Child
        Individual child = new Individual("I5");
        child.setName("Child Doe");

        ArrayList<String> children = new ArrayList<>();
        children.add(child.getId());
        parentFam.setChild(children);

        Gedcom_Service.families.put(parentFam.getId(), parentFam);

        // Family of father's siblings (uncles/aunts)
        Family grandParentFam = new Family("F2");
        Individual uncle = new Individual("I3");
        uncle.setName("Uncle Doe");
        Individual aunt = new Individual("I4");
        aunt.setName("Aunt Doe");

        ArrayList<String> grandChildren = new ArrayList<>();
        grandChildren.add(father.getId()); // Father is also a child of the grandParentFam
        grandChildren.add(uncle.getId());
        grandChildren.add(aunt.getId());
        grandParentFam.setChild(grandChildren);
        Gedcom_Service.families.put(grandParentFam.getId(), grandParentFam);

        // Child's family, child is married to an aunt
        Family childFam = new Family("F3");
        child.setSpouseOf(childFam.getId());
        childFam.setHusb(child.getId());
        childFam.setWife(aunt.getId());
        Gedcom_Service.families.put(childFam.getId(), childFam);

        // Populate individuals map
        Gedcom_Service.individuals.put(father.getId(), father);
        Gedcom_Service.individuals.put(mother.getId(), mother);
        Gedcom_Service.individuals.put(uncle.getId(), uncle);
        Gedcom_Service.individuals.put(aunt.getId(), aunt);
        Gedcom_Service.individuals.put(child.getId(), child);

        // Set child relationships
        father.setChildOf(grandParentFam.getId());
        uncle.setChildOf(grandParentFam.getId());
        aunt.setChildOf(grandParentFam.getId());
        child.setChildOf(parentFam.getId());

        Gedcom_Service.AuntsandUnclesname(Gedcom_Service.families);

        String expectedOutput = "ERROR: User Story US20: Aunts and Uncles\nIndividual: I5 - Child Doe is married to either their aunt or uncle I4 - Aunt Doe\n\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }


    @Test
    void testUniqueFamilyNameBySpouses() throws IOException, ParseException {
        Family fam1 = new Family("F1");
        fam1.setHusb("I1");
        fam1.setWife("I2");
        fam1.setMarriage("01/01/2000");

        Family fam2 = new Family("F2");
        fam2.setHusb("I1");
        fam2.setWife("I2");
        fam2.setMarriage("01/01/2000");

        Gedcom_Service.families.put(fam1.getId(), fam1);
        Gedcom_Service.families.put(fam2.getId(), fam2);

        Individual husb = new Individual("I1");
        husb.setName("Ali");
        Individual wife = new Individual("I2");
        wife.setName("Fatemeh");

        Gedcom_Service.individuals.put(husb.getId(), husb);
        Gedcom_Service.individuals.put(wife.getId(), wife);

        Gedcom_Service.uniqueFamilynameBySpouses(Gedcom_Service.individuals, Gedcom_Service.families);

        String expectedOutput = "ERROR: User Story US24: Unique Families By Spouse :\nF1: Husband Name: Ali, Wife Name: Fatemeh and F2: Husband Name: Ali, Wife Name: Fatemeh\n have the same spouses and marriage dates: 01/01/2000\n";
        assertTrue(outputStreamCaptor.toString().contains(expectedOutput));
    }

    @Test
    void testCreateOutputFileInvalidPath() throws IOException {
        // Mock user input with an invalid path followed by a valid path
        String invalidPath = "invalid_directory";
        String validPath = "test_directory";
        Files.createDirectories(Paths.get(validPath));

        String input = invalidPath + "\n" + validPath + "\n";
        InputStream stdin = System.in;
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            Gedcom_Service.createOutputFile(bufferRead);

            String expectedOutput = "The Path You Entered Does Not Exist. Reenter path";
            assertTrue(outputStreamCaptor.toString().contains(expectedOutput), "Should prompt to reenter path");

            Path outputPath = Paths.get(validPath + "/GedcomService_output.txt");
            assertTrue(Files.exists(outputPath), "Output file should be created");

            // Clean up
            Files.deleteIfExists(outputPath);
            Files.deleteIfExists(Paths.get(validPath));
        } finally {
            System.setIn(stdin);
        }
    }


    @Test
    void testReadAndParseFileInvalidFile() {
        String invalidFileName = "non_existent_file.ged";
        try {
            Gedcom_Service.readAndParseFile(invalidFileName);
            fail("Expected IOException to be thrown");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("non_existent_file.ged"), "Expected file not found error");
        }
    }

}
