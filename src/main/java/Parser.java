import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class Parser {

    //source path where all jsons are stores
    public static String SOURCE_FOLDER = "./";
    public static String TARGET_FOLDER = "";

    public static boolean ERROR = false;

    public static void main(String[] args) {
        if(args.length == 0){
            System.out.println("Specificate the source path of the directory with the activity json files."
            + System.lineSeparator() + "Example:"
            + System.lineSeparator() + "java -jar runtasticactivityparser /Users/bob/data/runtastic");
            System.exit(0);
        } else {
            SOURCE_FOLDER = args[0];
            TARGET_FOLDER = SOURCE_FOLDER + "/target";
        }

        File sourceFolder = new File(SOURCE_FOLDER);
        if(!sourceFolder.exists()){
            System.out.println("Directory dosent exist");
            System.exit(1);
        }


        System.out.println("===> Start parse Runtastic json to gpx files...");
        File[] jsonFiles = sourceFolder.listFiles();
        for (File json : jsonFiles){
            if(json.toString().contains(".json")){
                parseJsonToGPX(json);
            }

        }

        if(!ERROR){
            System.out.println("===> Parsed " + jsonFiles.length + " without errors.");
        }
    }

    public static void parseJsonToGPX(File json){
        final String LAT_PATTERN = "\"latitude\":([0-9]+.[0-9]+)";
        final String LON_PATTERN = "\"longitude\":([0-9]+.[0-9]+)";
        final String ELE_PATTERN = "\"altitude\":([0-9]+.[0-9]+)";
        final String TIME_PATTERN = "\"timestamp\":\"(.+\\+[0-9]+)\"";
        String gpx  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<gpx version=\"1.1\" creator=\"Runtastic: Life is short - live long, http://www.runtastic.com\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1\n" +
                "                                http://www.topografix.com/GPX/1/1/gpx.xsd\n" +
                "                                http://www.garmin.com/xmlschemas/GpxExtensions/v3\n" +
                "                                http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd\n" +
                "                                http://www.garmin.com/xmlschemas/TrackPointExtension/v1\n" +
                "                                http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                "  <metadata>\n" +
                "    <copyright author=\"www.runtastic.com\">\n" +
                "      <year>2018</year>\n" +
                "      <license>http://www.runtastic.com</license>\n" +
                "    </copyright>\n" +
                "    <link href=\"http://www.runtastic.com\">\n" +
                "      <text>runtastic</text>\n" +
                "    </link>\n" +
                "    <time>$startTime$</time>\n" +
                "  </metadata>" +
                "<trk>\n" +
                "    <trkseg>";

        Pattern lonPattern = Pattern.compile(LON_PATTERN);
        Pattern latPattern = Pattern.compile(LAT_PATTERN);
        Pattern elePattern = Pattern.compile(ELE_PATTERN);
        Pattern timePattern = Pattern.compile(TIME_PATTERN);

        int waypointCounter = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(json.getPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                gpx += "<trkpt lon=\"$sessionLon$\" lat=\"$sessionLat$\">\n" +
                        "        <ele>$sessionEle$</ele>\n" +
                        "        <time>$sessionTime$</time>\n" +
                        "      </trkpt>";

                String lon = getRegexGroup(LON_PATTERN, line, 1);
                String lat = getRegexGroup(LAT_PATTERN, line, 1);
                String ele = getRegexGroup(ELE_PATTERN, line, 1);
                String time = getRegexGroup(TIME_PATTERN, line, 1);
                time = convertTime(time);

                gpx = gpx.replaceAll("\\$sessionLon\\$", lon);
                gpx = gpx.replaceAll("\\$sessionLat\\$", lat);
                gpx = gpx.replaceAll("\\$sessionEle\\$", ele);
                gpx = gpx.replaceAll("\\$sessionTime\\$", time);

                if(waypointCounter == 0){
                    gpx = gpx.replaceAll("\\$startTime\\$", time);
                }
                waypointCounter++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ERROR = true;
        } catch (IOException e) {
            e.printStackTrace();
            ERROR = true;
        }

        gpx += "</trkseg>\n" +
                "  </trk>\n" +
                "</gpx>";

        //write gpx to gpx file
        String fileName = json.getName().replaceFirst("[.][^.]+$", "");
        exportFile(gpx, fileName, ".gpx");
        System.out.println("Parsed " + waypointCounter + " waypoints  from: " + fileName + ".json successfully. " +
                "GPX File is stored at: " + TARGET_FOLDER);
    }

    private static String convertTime(String time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        try {
            Date date = format.parse(time);
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            ERROR = true;
            return "";
        }

    }

    public static String getRegexGroup(String matchPattern, String text, int group){
        Pattern pattern = Pattern.compile(matchPattern);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()){
            return matcher.group(group);
        }
        return "";
    }

    public static void exportFile(String value, String fileName, String extension){
        try {
            new File(TARGET_FOLDER).mkdirs();
            Files.write(Paths.get(TARGET_FOLDER + "/" + fileName + extension), value.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            ERROR = true;
        }
    }
}
