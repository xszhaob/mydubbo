package pers.bo.zhao.mydubbo.config;

import junit.framework.TestCase;
import org.junit.Test;
import pers.bo.zhao.mydubbo.config.support.Parameter;

import java.util.HashMap;
import java.util.Map;

public class AbstractConfigTest {

    @Test
    public void testAppendProperties1() throws Exception {
        try {
            System.setProperty("mydubbo.properties.i", "1");
            System.setProperty("mydubbo.properties.c", "c");
            System.setProperty("mydubbo.properties.b", "2");
            System.setProperty("mydubbo.properties.d", "3");
            System.setProperty("mydubbo.properties.f", "4");
            System.setProperty("mydubbo.properties.l", "5");
            System.setProperty("mydubbo.properties.s", "6");
            System.setProperty("mydubbo.properties.str", "dubbo");
            System.setProperty("mydubbo.properties.bool", "true");
            PropertiesConfig config = new PropertiesConfig();
            AbstractConfig.appendProperties(config);
            TestCase.assertEquals(1, config.getI());
            TestCase.assertEquals('c', config.getC());
            TestCase.assertEquals((byte) 0x02, config.getB());
            TestCase.assertEquals(3d, config.getD());
            TestCase.assertEquals(4f, config.getF());
            TestCase.assertEquals(5L, config.getL());
            TestCase.assertEquals(6, config.getS());
            TestCase.assertEquals("dubbo", config.getStr());
            TestCase.assertTrue(config.isBool());
        } finally {
            System.clearProperty("mydubbo.properties.i");
            System.clearProperty("mydubbo.properties.c");
            System.clearProperty("mydubbo.properties.b");
            System.clearProperty("mydubbo.properties.d");
            System.clearProperty("mydubbo.properties.f");
            System.clearProperty("mydubbo.properties.l");
            System.clearProperty("mydubbo.properties.s");
            System.clearProperty("mydubbo.properties.str");
            System.clearProperty("mydubbo.properties.bool");
        }
    }


    @Test
    public void testAppendParameters1() throws Exception {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("default.num", "one");
        parameters.put("num", "ONE");
        AbstractConfig.appendParameters(parameters, new ParameterConfig(1, "hello/world", 30, "password"), "prefix");
        TestCase.assertEquals("one", parameters.get("prefix.key.1"));
        TestCase.assertEquals("two", parameters.get("prefix.key.2"));
        TestCase.assertEquals("ONE,one,1", parameters.get("prefix.num"));
        TestCase.assertEquals("hello%2Fworld", parameters.get("prefix.naming"));
        TestCase.assertEquals("30", parameters.get("prefix.age"));
        TestCase.assertFalse(parameters.containsKey("prefix.key-2"));
        TestCase.assertFalse(parameters.containsKey("prefix.secret"));
    }


    private static class PropertiesConfig extends AbstractConfig {
        private char c;
        private boolean bool;
        private byte b;
        private int i;
        private long l;
        private float f;
        private double d;
        private short s;
        private String str;

        PropertiesConfig() {
        }

        PropertiesConfig(String id) {
            this.id = id;
        }

        public char getC() {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public byte getB() {
            return b;
        }

        public void setB(byte b) {
            this.b = b;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public long getL() {
            return l;
        }

        public void setL(long l) {
            this.l = l;
        }

        public float getF() {
            return f;
        }

        public void setF(float f) {
            this.f = f;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public short getS() {
            return s;
        }

        public void setS(short s) {
            this.s = s;
        }
    }


    private static class ParameterConfig {
        private int number;
        private String name;
        private int age;
        private String secret;

        ParameterConfig() {
        }

        ParameterConfig(int number, String name, int age, String secret) {
            this.number = number;
            this.name = name;
            this.age = age;
            this.secret = secret;
        }

        @Parameter(key = "num", append = true)
        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        @Parameter(key = "naming", append = true, escaped = true, required = true)
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Parameter(excluded = true)
        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Map getParameters() {
            Map<String, String> map = new HashMap<String, String>();
            map.put("key.1", "one");
            map.put("key-2", "two");
            return map;
        }
    }
}
