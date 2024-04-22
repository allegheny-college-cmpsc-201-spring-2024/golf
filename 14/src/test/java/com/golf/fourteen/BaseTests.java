package com.interpreter.taxi;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.util.regex.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.Assert.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.CoreMatchers.not;

import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cfh.taxi.cli.Main;

class BaseTests {

  /*
    Word to the accepted answer at SO, they're a real one.
    https://stackoverflow.com/questions/1119385/junit-test-for-system-out-println
  */

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  private final InputStream originalIn = System.in;
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @BeforeAll
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  static Stream<Arguments> testProgramOutput() throws Exception {
    URL resource = BaseTests.class.getClassLoader().getResource("main.txt");
    File file = Paths.get(resource.toURI()).toFile();
    String absPath = file.getAbsolutePath();
    return Stream.of(
      Arguments.of((Object) new String[]{"default.map", absPath})
    );
  }

  @MethodSource
  @Test
  @ParameterizedTest
  void testProgramOutput(String[] args) throws Exception {
    String[] tests = {"trial\ntrail\n", "shoe tome\ntone poem\n", "fun house\nbig manor\n"};
    String[] outcomes = {"2\n", "6\n", "8\n"};
    for(int i = 0; i < tests.length; i ++){
      ByteArrayInputStream in = new ByteArrayInputStream(tests[i].getBytes());
      System.setIn(in);
      Main.main(args);
      assertThat(
        outContent.toString(),
        containsString(outcomes[i])
      );
      assertThat(
        outContent.toString(),
        containsString("The taxi is back in the garage.")
      );
    }
    restoreStreams();
    totalOutputStats();
  }

  void totalOutputStats() {
    Pattern distPattern = Pattern.compile("([0-9]+\\.[0-9]+) units");
    Matcher distMatcher = distPattern.matcher(outContent.toString());
    System.out.println("FINAL STATS");
    double sum = 0;
    while(distMatcher.find()) {
      double metric = Double.parseDouble(distMatcher.group(1));
      sum += metric;
    }
    System.out.println(String.format("%1$,.2f units traveled.", sum));
  }

  @AfterAll
  public void restoreStreams() {
    System.setIn(originalIn);
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

}
