package thesis.testing.runner;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runner for testNg tests
 */
public class NgRunner {

    public static void main(String[] args) {

        XmlSuite suite = new XmlSuite();
        suite.setName(args[0]);
        XmlTest test = new XmlTest(suite);
        test.setName(args[1]);
        List<XmlClass> classes = new ArrayList<XmlClass>();
        XmlClass testClass = new XmlClass(args[0]);
        List<XmlInclude> includedMethods = new ArrayList<>();
        includedMethods.add(new XmlInclude(args[1]));
        testClass.setIncludedMethods(includedMethods);
        classes.add(testClass);
        test.setXmlClasses(classes);
        List<XmlSuite> suites = new ArrayList<XmlSuite>();
        suites.add(suite);
        TestNG tng = new TestNG();
        tng.setXmlSuites(suites);

        // This prevents output files from being generated
        tng.setUseDefaultListeners(false);

        // Prepare TestNG
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);
        // Execute tests
        tng.run();
        System.out.println("TestNg done");
        int status = 0;
        // Examine failed tests and display their name and stack trace
        // set return status accordingly (no failed tests = 0,
        // at least one failed test = 1)
        List<ITestResult> failedTests = tla.getFailedTests();
        for (ITestResult failedTest : failedTests) {
            status = 1;
            Throwable th = failedTest.getThrowable();
            System.out.println("=========");
            System.out.println("FAIL: " + failedTest.getName() + ": " + th);
            System.out.println("STACK TRACE:");
            th.printStackTrace(System.out);
            System.out.println("=========");
            System.out.println();

        }
        // Return status of execution to the invoker
        System.exit(status);
    }
}
