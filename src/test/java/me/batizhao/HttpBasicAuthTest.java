package me.batizhao;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple HttpBasicAuth.
 */
public class HttpBasicAuthTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HttpBasicAuthTest(String testName)
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HttpBasicAuthTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testGetHttpStatus()
    {
        HttpBasicAuth basicAuth = new HttpBasicAuth();
        //assertEquals(200, basicAuth.getHttpStatus());
    }
}
