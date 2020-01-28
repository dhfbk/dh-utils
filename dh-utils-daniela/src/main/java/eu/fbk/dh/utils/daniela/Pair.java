package eu.fbk.dh.utils.daniela;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pair {

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    static int calculate(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    static String format(String input) {
        return input.toLowerCase().replaceAll("[^a-z]", "");
    }

    private static int compare(List<String> nodes, List<HostElement> hostElements, int i, int hostIndex) {
        int realHostStatus = hostIndex % 2;
        int realHostIndex = (hostIndex - realHostStatus) / 2;
        int realNodeStatus = i % 2;
        int realNodeIndex = (i - realNodeStatus) / 2;

        String formattedString;
        String compareString;
        try {
            formattedString = nodes.get(realNodeIndex);
            compareString = hostElements.get(realHostIndex).get(realHostStatus);
        } catch (IndexOutOfBoundsException e) {
            return 100;
        }

        if (compareString == null) {
            return 100;
        }

        int maxLen = Math.min(formattedString.length(), compareString.length());
        String str1 = formattedString.substring(0, maxLen);
        String str2 = compareString.substring(0, maxLen);
        if (realHostStatus == 1) {
            str1 = formattedString.substring(formattedString.length() - maxLen);
            str2 = compareString.substring(compareString.length() - maxLen);
        }
        System.out.println(str1 + " --- " + str2);

        int lev = calculate(str1, str2);

        if (maxLen < 6 && lev != 0) {
            return 100;
        }
        return lev;
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./pair-xml")
                    .withHeader("Pairing XML files")
                    .withOption("i", "input", "Input file with speech", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("a", "annotations", "Input file with annotations", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File annotationFile = cmd.getOptionValue("annotations", File.class);

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setFeature("http://xml.org/sax/features/validation", false);
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList;
            String expression;
            FileInputStream inputStream;
            Document xmlDocument;

            List<HostElement> hostElements = new ArrayList<>();
            List<String> nodes = new ArrayList<>();

            expression = "/annotation/body/track";
            inputStream = new FileInputStream(annotationFile);
            xmlDocument = builder.parse(inputStream);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String name = node.getAttributes().getNamedItem("name").getNodeValue();
                if (name.equals("speaker")) {
                    expression = "el";
                    NodeList elList = (NodeList) xPath.compile(expression).evaluate(node, XPathConstants.NODESET);
                    for (int j = 0; j < elList.getLength(); j++) {
                        Node el = elList.item(j);
                        expression = "comment";
                        NodeList commentList = (NodeList) xPath.compile(expression).evaluate(el, XPathConstants.NODESET);
                        String comment = "";
                        for (int k = 0; k < commentList.getLength(); k++) {
                            Node commentNode = commentList.item(k);
                            comment = commentNode.getTextContent().trim();
                        }

                        String start = "", end = null;
                        String[] parts = comment.split("/");
                        start = format(parts[0]);
                        if (parts.length > 1) {
                            end = format(parts[1]);
                        }

                        String startTime = el.getAttributes().getNamedItem("start").getNodeValue();
                        String endTime = el.getAttributes().getNamedItem("end").getNodeValue();

                        hostElements.add(new HostElement(start, end, Double.parseDouble(startTime), Double.parseDouble(endTime)));

//                        System.out.println(startTime + " " + endTime);
//                        System.out.println(start + " " + end);
//                        System.out.println();
                    }
                }
            }

            inputStream = new FileInputStream(inputFile);
            expression = "/trascrizioni/speech/u";
            xmlDocument = builder.parse(inputStream);
            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String formattedString = format(node.getTextContent());
                nodes.add(formattedString);
            }

//            System.out.println(nodes);

            for (HostElement hostElement : hostElements) {
                System.out.println(hostElement.getStart());
            }

            int i = 0;
            int j = hostElements.size() - 1;
            while (i < j) {
                i++;
                j--;
//                System.out.println(i);
//                System.out.println(j);
                String hostString;

                boolean found = false;
                hostString = hostElements.get(i).getStart();
                if (hostString == null) {
                    continue;
                }
//                System.out.println(hostString);
//                for (String node : nodes) {
//                    int maxLen = Math.min(node.length(), hostString.length());
//                    String str1 = node.substring(0, maxLen);
//                    String str2 = hostString.substring(0, maxLen);
//                    if (str1.length() < 6) {
//                        continue;
//                    }
//                    System.out.println(str1 + " --- " + str2 + " --- " + Boolean.toString(found));
//                    if (str1.equals(str2)) {
//                        found = true;
//                    }
//                }
//                System.out.println(found);
            }

//
//            for (String node : nodes) {
////                System.out.println(node);
//            }


//            System.exit(1);
//
//            int hostIndex = 0;
//
//            List<String> hostStrings = new ArrayList<>();
//            for (HostElement hostElement : hostElements) {
//                hostStrings.add(hostElement.getStart());
//                hostStrings.add(hostElement.getEnd());
//            }
//
//            inputStream = new FileInputStream(inputFile);
//            expression = "/trascrizioni/speech/u";
//            xmlDocument = builder.parse(inputStream);
//            nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
//            for (int i = 0; i < nodeList.getLength(); i++) {
//                Node node = nodeList.item(i);
//                String formattedString = format(node.getTextContent());
//                nodes.add(formattedString);
//            }
//
//            mainLoop:
//            for (int i = 0; i < nodes.size() * 2; i++) {
//
//                System.out.println("I: " + i);
//                if ((hostIndex - i) % 2 != 0) {
//                    System.out.println("hI: " + hostIndex);
//                    continue;
//                }
//
//                int num = 4;
//                List<Integer> levs = new ArrayList<>();
//                for (int j = 0; j < num; j++) {
//                    levs.add(compare(nodes, hostElements, i + j, hostIndex + j));
//                }
//                for (int levIndex = 0; levIndex < levs.size(); levIndex++) {
//                    Integer lev = levs.get(levIndex);
//                    if (lev < 3) {
//                        hostIndex += levIndex + 1;
//                        i += levIndex;
//                        System.out.println("Ok " + levIndex);
//                        continue mainLoop;
//                    }
//                }
//
//                System.out.println("No");
//
//            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

}
