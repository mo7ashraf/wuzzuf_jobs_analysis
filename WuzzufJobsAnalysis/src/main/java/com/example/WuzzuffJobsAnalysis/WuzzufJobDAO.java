package com.example.WuzzuffJobsAnalysis;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static com.example.WuzzuffJobsAnalysis.WuzzufJobDAO.csv_path;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.SparkSession;
import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.Circle;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

/**
 *
 * @author dell
 */
public class WuzzufJobDAO {

    static String csv_path = "src//main//resources//Wuzzuf_Jobs.csv";
    SparkSession sparkSession;
    public SparkConf sparkConfig;
    JavaSparkContext context;
    public static String line = "<br>";

    public static void run(String[] args) {
        WuzzufJobDAO jobDAO = new WuzzufJobDAO();
        jobDAO.sparkConfig = new SparkConf().setAppName("WuzzufJobs").setMaster("local[2]");
        jobDAO.context = new JavaSparkContext(jobDAO.sparkConfig);

        jobDAO.sparkSession = SparkSession
                .builder()
                .appName("WuzzufJobs")
                .getOrCreate();

        String s1 = "**************************1. Read data set and convert it to Spark RDD *************************";
        JavaRDD<WuzzufJob> jobRDD = jobDAO.getJobRDD();
        //System.out.println("jobRDD : " + jobRDD.count());
        List<WuzzufJob> allJobs = jobDAO.getAllJobs(jobRDD);
        List<WuzzufJob> firs_10_jobs = jobDAO.getFirst10Jobs(jobRDD);

        //System.out.println("**************************2. Display structure and summary of the data. *************************");
        String dataStstisticsUsingJoinery = getDataStstisticsUsingJoinery();

        //System.out.println("**************************3. Clean the data (null, duplications) *************************");
        JavaRDD<WuzzufJob> distinct_jobRDD = jobRDD.distinct();
        List<WuzzufJob> all_distinct_Jobs = jobDAO.getAllJobs(distinct_jobRDD);

        //System.out.println("distinct_jobRDD : " + distinct_jobRDD.count());

        //System.out.println("**************************4- Count the jobs for each company and display that in order *************************");
        //System.out.println("---------------------------------------------------------------");
        Map<String, List<WuzzufJob>> companiesJobsMap = jobDAO.getCompaniesJobs(allJobs);
        //System.out.println("---------------------------------------------------------------");
        Map<String, List<WuzzufJob>> sortedCompaniesJobsMap = jobDAO.getSortedCompaniesJobs(companiesJobsMap);
        //System.out.println("---------------------------------------------------------------");
        String maxDemandingCompany = jobDAO.getMaxDemandingCompany(companiesJobsMap);
        //System.out.println("maxDemandingCompany : " + maxDemandingCompany + "  *** " + companiesJobsMap.get(maxDemandingCompany).size());

        //System.out.println("**************************5. Show step 4 in a pie chart *************************");
        //plot(JobTitlesMap, "Trending Companies", "Trending Companies");
        jobDAO.drawPieChart(companiesJobsMap, "comapnies_jobs");

        //System.out.println("**************************6. Find out What are it the most popular job titles?  *************************");
        Map<String, List<WuzzufJob>> jobTitles = jobDAO.getJobTitles(allJobs);
        List<Object> jobTitlesKeys = jobDAO.getSortedJobTitles(jobTitles);
        int count = 0;
        for (Object o : jobTitlesKeys) {
            String key = (String) o;
            List<WuzzufJob> value = jobTitles.get(key);
            count++;
            //System.out.println(count + " : " + key + ", " + value.size());
        }
        //System.out.println("---------------------------------------------------------------");
        List<Object> topJobTitles = jobDAO.getTopJobTitles(jobTitlesKeys);
        count = 0;
        for (Object o : topJobTitles) {
            String key = (String) o;
            List<WuzzufJob> value = jobTitles.get(key);
            count++;
           // System.out.println(count + " : " + key + ", " + value.size());
        }
        //System.out.println("**************************7. Show step 6 in bar chart *************************");
        jobDAO.drawBarChart(jobTitles, topJobTitles, "Popular Job Titles", "Job Titles");

        //System.out.println("**************************8. Find out the most popular areas? *************************");
        Map<String, List<WuzzufJob>> jobAreas = jobDAO.getAreas(allJobs);
        List<Object> jobAreasKeys = jobDAO.getSortedJobTitles(jobAreas);
        count = 0;
        for (Object o : jobAreasKeys) {
            String key = (String) o;
            List<WuzzufJob> value = jobAreas.get(key);
            count++;
           // System.out.println(count + " : " + key + ", " + value.size());
        }
        //System.out.println("---------------------------------------------------------------");
        List<Object> topAreas = jobDAO.getTopAreas(jobAreasKeys);
        count = 0;
        for (Object o : topAreas) {
            String key = (String) o;
            List<WuzzufJob> value = jobAreas.get(key);
            count++;
            //System.out.println(count + " : " + key + ", " + value.size());
        }
        //System.out.println("**************************9. Show step 8 in bar chart *************************");
        jobDAO.drawBarChart(jobAreas, topAreas, "Most Popular Areas", "Areas");
        //System.out.println("**************************10. Print skills one by one and how many each repeated *************************");
        List<String> allSkills = jobDAO.getAllSkills(allJobs);
        Map<String, Long> skillsCount = jobDAO.getSkillsCount(allSkills);
        List<Object> sortedSkills = jobDAO.getSortedSkills(skillsCount);
        count = 0;
        for (Object o : sortedSkills) {
            String key = (String) o;
            Long value = skillsCount.get(key);
            count++;
            //System.out.println(count + " : " + key + ", " + value);
        }
       // System.out.println("---------------------------------------------------------------");
        List<Object> topSkills = jobDAO.getTopSkills(sortedSkills);
        count = 0;
        for (Object o : topSkills) {
            String key = (String) o;
            Long value = skillsCount.get(key);
            count++;
          //  System.out.println(count + " : " + key + ", " + value);
        }
       // System.out.println("**************************11. Show step 10 in bar chart *************************");
        jobDAO.drawSkillsBarChart(skillsCount, topSkills, "Most Popular Skills", "Skills");

    }

    // loop to convert list to string
    public static String listtostring(List ls) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < ls.size() - 1) {

            sb.append(ls.get(i));
            sb.append(line);
            i++;
        }
        sb.append(ls.get(i));
        String res = sb.toString();
        return res;
    }

    public static String listAllJobsToString(List<WuzzufJob> ls) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        String html_table_header = "<table style=\"width:100%\" border=\"1\">";
        sb.append(html_table_header);
        while (i < ls.size() - 1) {
            WuzzufJob j = ls.get(i);
            String html_table_row = "  <tr>\n"
                    + "    <th>" + j.getTitle() + "</th>"
                    + "    <th>" + j.getLocation() + "</th>"
                    + "    <th>" + j.getCompany() + "</th>"
                    + "    <th>" + j.getType() + "</th>"
                    + "    <th>" + j.getCountry() + "</th>"
                    + "    <th>" + j.getLevel() + "</th>"
                    + "  </tr>";
            // String job_data=j.getTitle()+" - "+j.getLocation()+" - "+j.getCompany()+" - "+j.getLocation()+" - "+j.getType();
            sb.append(html_table_row);
            //sb.append(line);
            i++;
        }
        sb.append("</table>");
        //sb.append(ls.get(i));
        String res = sb.toString();
        return res;
    }

    public static String listAllJobsToString(Map<String, List<WuzzufJob>> map) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        String html_table_header = "<table style=\"width:100%\" border=\"1\"> "
                + "<tr>"
                + "    <th>Company</th>"
                + "    <th>Jobs Count</th>"
                + "  </tr>";
        sb.append(html_table_header);
        for (Map.Entry<String, List<WuzzufJob>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<WuzzufJob> val = entry.getValue();

            String html_tr = "<tr>"
                    + "    <th >" + key + "</th>"
                    + "    <th >" + val.size() + "</th>"
                    + "  </tr>";
            sb.append(html_tr);

            /* while (i < val.size() - 1) {
                WuzzufJob j = val.get(i);
                String html_table_row = "  <tr>\n"
                        + "    <th>" + j.getTitle() + "</th>"
                        + "    <th>" + j.getLocation() + "</th>"
                        + "    <th>" + j.getCompany() + "</th>"
                        + "    <th>" + j.getType() + "</th>"
                        + "    <th>" + j.getCountry() + "</th>"
                        + "    <th>" + j.getLevel() + "</th>"
                        + "  </tr>";
                // String job_data=j.getTitle()+" - "+j.getLocation()+" - "+j.getCompany()+" - "+j.getLocation()+" - "+j.getType();
                sb.append(html_table_row);
                //sb.append(line);
                i++;
            }*/
        }

        sb.append("</table>");
        //sb.append(ls.get(i));
        String res = sb.toString();
        return res;
    }

    public static String listAllMapDataToString(Map<String, List<WuzzufJob>> map, List<Object> topJobTitles, String title1, String title2) {
        StringBuilder sb = new StringBuilder();
        String html_table_header = "<table style=\"width:100%\" border=\"1\"> "
                + "<tr>"
                + "    <th>" + title1 + "</th>"
                + "    <th>" + title2 + "</th>"
                + "  </tr>";
        sb.append(html_table_header);

        for (Object o : topJobTitles) {
            String key = (String) o;
            List<WuzzufJob> value = map.get(key);
            String html_tr = "<tr>"
                    + "    <th >" + key + "</th>"
                    + "    <th >" + value.size() + "</th>"
                    + "  </tr>";
            sb.append(html_tr);
        }
        sb.append("</table>");
        String res = sb.toString();
        return res;
    }

    public static String listAllDataToString(Map<String, Long> map, List<Object> topJobTitles, String title1, String title2) {
        StringBuilder sb = new StringBuilder();
        String html_table_header = "<table style=\"width:100%\" border=\"1\"> "
                + "<tr>"
                + "    <th>" + title1 + "</th>"
                + "    <th>" + title2 + "</th>"
                + "  </tr>";
        sb.append(html_table_header);

        for (Object o : topJobTitles) {
            String key = (String) o;
            Long value = map.get(key);
            String html_tr = "<tr>"
                    + "    <th >" + key + "</th>"
                    + "    <th >" + value + "</th>"
                    + "  </tr>";
            sb.append(html_tr);
        }
        sb.append("</table>");
        String res = sb.toString();
        return res;
    }

    public JavaRDD<WuzzufJob> getJobRDD() {
        // Get DataFrameReaderusing SparkSession
        DataFrameReader dataFrameReader = sparkSession.read();
        // Set header option to true to specify that first row in file contains name of columns
        dataFrameReader.option("header", "true");

        // Create an RDD of Person objects from a text file
        JavaRDD<WuzzufJob> jobRDD = dataFrameReader.textFile(csv_path)
                .javaRDD()
                .map(line -> {
                    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    WuzzufJob job = new WuzzufJob();
                    //if (!parts[0].contains("Title")) {
                    job.setTitle(parts[0]);
                    job.setCompany(parts[1]);
                    job.setLocation(parts[2]);
                    job.setType(parts[3]);
                    job.setLevel(parts[4]);
                    job.setExperience_years(parts[5]);
                    job.setCountry(parts[6]);
                    job.setSkills(parts[7].replace("\"", "").split(","));
                    // }
                    return job;
                });
        return jobRDD;
    }

    public List<WuzzufJob> getAllJobs(JavaRDD<WuzzufJob> jobRDD) {
        WuzzufJob header = jobRDD.first();
        List<WuzzufJob> jobList = jobRDD.collect();
        jobList.remove(header);
        /*jobList.forEach(job -> {
            if (!header.getTitle().equals(job.getTitle())) {
                //System.out.println((jobList.indexOf(job)) + "--------------------------------------------------------");
                //System.out.println(job.getTitle());
                String[] skills = job.getSkills();
                for (String skill : skills) {
                    System.out.print(skill + ",");
                }
                //System.out.println();
            }
        });*/
        return jobList;
    }

    public List<WuzzufJob> getFirst10Jobs(JavaRDD<WuzzufJob> jobRDD) {
        WuzzufJob header = jobRDD.first();
        List<WuzzufJob> first_10_jobs = jobRDD.take(11);
        first_10_jobs.remove(header);
        first_10_jobs.forEach(job -> {
            if (!header.getTitle().equals(job.getTitle())) {
                //System.out.println((first_10_jobs.indexOf(job)) + "--------------------------------------------------------");
                //System.out.println(job.getTitle());
                String[] skills = job.getSkills();
                for (String skill : skills) {
                    System.out.print(skill + ",");
                }
                //System.out.println();
            }
        });
        return first_10_jobs;
    }

    public static String getDataStstisticsUsingJoinery() {
        String structure = "";
        try {
            Table data = Table.read().csv(csv_path);
            String summary = data.summary().toString();
            //System.out.println("summary : " + summary);
            structure = data.structure().toString();
            //System.out.println("structure : " + structure);
        } catch (IOException ex) {
            Logger.getLogger(WuzzufJobDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return structure;
    }

    public Map<String, List<WuzzufJob>> getCompaniesJobs(List<WuzzufJob> allJobs) {
        //Count the jobs for each company and display that in order (What are the most demanding companies for jobs?)
        Map<String, List<WuzzufJob>> companiesJobsMap = allJobs.stream().collect(Collectors.groupingBy(c -> c.getCompany()));
        int count = 0;
        for (Map.Entry<String, List<WuzzufJob>> entry : companiesJobsMap.entrySet()) {
            String key = entry.getKey();
            List<WuzzufJob> value = entry.getValue();
            count++;
            //System.out.println(count + " : " + key + ", " + value.size());
        }
        return companiesJobsMap;
    }

    public Map<String, List<WuzzufJob>> getSortedCompaniesJobs(Map<String, List<WuzzufJob>> companiesJobsMap) {
        Map<String, List<WuzzufJob>> sortedCompaniesJobsMap = new LinkedHashMap<>();
        int count = 0;
        Object[] sorted = companiesJobsMap.keySet().stream().sorted(Comparator.comparingInt(c -> companiesJobsMap.get(c).size())).toArray();
        for (int i = sorted.length - 1; i >= 0; i--) {
            String key = (String) sorted[i];
            List<WuzzufJob> val = companiesJobsMap.get(key);
            count++;
            //System.out.println(count + " : " + key + ", " + val.size());
            sortedCompaniesJobsMap.put(key, val);
        }

        return sortedCompaniesJobsMap;
    }

    /*public static LinkedHashMap<String, List<Job0>> sortMap(Map<String,  List<Job0>> map) {
        LinkedHashMap<String,  List<Job0>> sortedMap = map.entrySet().stream()
                //.sorted(Comparator.comparingLong(e -> e.getValue())) // Asc Sorting
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Desc Sorting
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> {
                            throw new AssertionError();
                        },
                        LinkedHashMap::new
                ));
        return sortedMap;
    }*/
    public String getMaxDemandingCompany(Map<String, List<WuzzufJob>> companiesJobsMap) {
        String maxDemandingCompany = companiesJobsMap.keySet().stream().max(Comparator.comparingInt(c -> companiesJobsMap.get(c).size())).get();
        return maxDemandingCompany;
    }

    public Map<String, List<WuzzufJob>> getJobTitles(List<WuzzufJob> allJobs) {
        Map<String, List<WuzzufJob>> JobTitlesMap = allJobs.stream().collect(Collectors.groupingBy(c -> c.getTitle()));
        return JobTitlesMap;
    }

    public List<Object> getSortedJobTitles(Map<String, List<WuzzufJob>> JobTitlesMap) {
        Object[] sorted = JobTitlesMap.keySet().stream().sorted(Comparator.comparingInt(c -> JobTitlesMap.get(c).size())).toArray();
        List<Object> sortedList = Arrays.asList(sorted);
        Collections.reverse(sortedList);
        return sortedList;
    }

    public List<Object> getTopJobTitles(List<Object> sortedJobsList) {
        List<Object> topJobs = sortedJobsList.stream().limit(10).collect(Collectors.toList());
        return topJobs;
    }

    public Map<String, List<WuzzufJob>> getAreas(List<WuzzufJob> allJobs) {
        Map<String, List<WuzzufJob>> JobTitlesMap = allJobs.stream().collect(Collectors.groupingBy(c -> c.getLocation()));
        return JobTitlesMap;
    }

    public List<Object> getSortedAreas(Map<String, List<WuzzufJob>> areasMap) {
        Object[] sorted = areasMap.keySet().stream().sorted(Comparator.comparingInt(c -> areasMap.get(c).size())).toArray();
        List<Object> sortedList = Arrays.asList(sorted);
        Collections.reverse(sortedList);
        return sortedList;
    }

    public List<Object> getTopAreas(List<Object> sortedAreasList) {
        List<Object> topJobs = sortedAreasList.stream().limit(10).collect(Collectors.toList());
        return topJobs;
    }

    public List<String> getAllSkills(List<WuzzufJob> allJobs) {
        //Map<String, List<Job>> JobTitlesMap = allJobs.stream().collect(Collectors.groupingBy(c -> c.getTitle()));
        List<String> skills = new ArrayList<>();
        allJobs.stream()
                .sorted(Comparator.comparingInt(c -> c.getSkills().length))
                .collect(Collectors.toList())
                .forEach(c -> Arrays.asList(c.getSkills()).stream().forEach(s -> {
            //System.out.println(s.trim());
            skills.add(s.trim());
        })
                );
        return skills;
    }

    public Map<String, Long> getSkillsCount(List<String> allSkills) {
        JavaRDD<String> parallelize = context.parallelize(allSkills);
        Map<String, Long> skillsCount = parallelize.countByValue();
        int count = 0;
        //System.out.println("================================================================================");
        for (Map.Entry<String, Long> entry : skillsCount.entrySet()) {
            String key = entry.getKey();
            Long val = entry.getValue();
            count++;
           // System.out.println(count + " : " + key + ", " + val);
        }
       /* System.out.println("================================================================================");
        System.out.println("skills count : " + allSkills.size());
        System.out.println("================================================================================");
        System.out.println("================================================================================");
        System.out.println("countByValue count : " + skillsCount.size());
        System.out.println("================================================================================");*/
        return skillsCount;
    }

    public List<Object> getSortedSkills(Map<String, Long> skillsCount) {
        Object[] sorted = skillsCount.keySet().stream().sorted(Comparator.comparingLong(c -> skillsCount.get(c))).toArray();
        List<Object> sortedList = Arrays.asList(sorted);
        Collections.reverse(sortedList);
        return sortedList;
    }

    public List<Object> getTopSkills(List<Object> sortedSkillsList) {
        List<Object> topJobs = sortedSkillsList.stream().limit(10).collect(Collectors.toList());
        return topJobs;
    }

    public void drawPieChart(final Map<String, List<WuzzufJob>> companiesJobsMap, String name) {
        try {
            /*JFrame frame = new JFrame();
            frame.getContentPane().add(new MyComponent(JobTitlesMap));
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);*/
            // Create Chart
            PieChart chart = new PieChartBuilder().width(1200).height(700).title("My Pie Chart")
                    .theme(Styler.ChartTheme.GGPlot2).build();

            // Customize Chart
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setAnnotationType(PieStyler.AnnotationType.LabelAndValue);
            chart.getStyler().setAnnotationDistance(1.3);
            chart.getStyler().setPlotContentSize(.8);
            chart.getStyler().setStartAngleInDegrees(90);

            for (Map.Entry<String, List<WuzzufJob>> entry : companiesJobsMap.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue().size();
                chart.addSeries(key, value);
            }

            // Show it
            // new SwingWrapper(chart).displayChart();
            // Save it
            BitmapEncoder.saveBitmap(chart, "src//main//resources//public//img//" + name, BitmapEncoder.BitmapFormat.JPG);

            // or save it in high-res
            //BitmapEncoder.saveBitmapWithDPI(chart, "C://charts//"+name+"_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void drawBarChart(Map<String, List<WuzzufJob>> categoryMap, List<Object> topCategoty, String title, String xAxis) {
        try {
            // Create Chart
            CategoryChart chart = new CategoryChartBuilder().width(1200).height(600)
                    .title(title).xAxisTitle(xAxis).yAxisTitle("Count").build();

            // Customize Chart
            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            //chart.getStyler().setAvailableSpaceFill(.96);
            chart.getStyler().setOverlapped(true);

            /*List<Double> jobTitleCount = new ArrayList<>();
            List<String> JobTitles=new ArrayList<>();
            for (Map.Entry<String, List<Job>> entry : categoryMap.entrySet()) {
            String key = entry.getKey();
            JobTitles.add(key);
            Integer value = entry.getValue().size();
            jobTitleCount.add((double) value);
            }*/
            List<Double> categoryCount = new ArrayList<>();
            for (Object key : topCategoty) {
                Integer value = categoryMap.get((String) key).size();
                categoryCount.add((double) value);
            }

            // Series
            chart.addSeries(title, topCategoty, categoryCount);
            // new SwingWrapper<>(chart).displayChart();
            BitmapEncoder.saveBitmap(chart, "src//main//resources//public//img//" + title, BitmapEncoder.BitmapFormat.JPG);

            // or save it in high-res
            BitmapEncoder.saveBitmapWithDPI(chart, "src//main//resources//public//img//" + title + "_Chart_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);
        } catch (IOException ex) {
            Logger.getLogger(WuzzufJobDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void drawSkillsBarChart(Map<String, Long> categoryMap, List<Object> topCategoty, String title, String xAxis) {
        try {
            // Create Chart
            CategoryChart chart = new CategoryChartBuilder().width(1200).height(600)
                    .title(title).xAxisTitle(xAxis).yAxisTitle("Count").build();

            // Customize Chart
            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            //chart.getStyler().setAvailableSpaceFill(.96);
            chart.getStyler().setOverlapped(true);

            /*List<Double> jobTitleCount = new ArrayList<>();
            List<String> JobTitles=new ArrayList<>();
            for (Map.Entry<String, List<Job>> entry : categoryMap.entrySet()) {
            String key = entry.getKey();
            JobTitles.add(key);
            Integer value = entry.getValue().size();
            jobTitleCount.add((double) value);
            }*/
            List<Long> categoryCount = new ArrayList<>();
            for (Object key : topCategoty) {
                Long value = categoryMap.get((String) key);
                categoryCount.add(value);
            }

            // Series
            chart.addSeries(title, topCategoty, categoryCount);
            BitmapEncoder.saveBitmap(chart, "src//main//resources//public//img//" + title, BitmapEncoder.BitmapFormat.JPG);

            // or save it in high-res
            BitmapEncoder.saveBitmapWithDPI(chart, "src//main//resources//public//img//" + title + "_Chart_300_DPI", BitmapEncoder.BitmapFormat.PNG, 300);

            //new SwingWrapper<>(chart).displayChart();
        } catch (IOException ex) {
            Logger.getLogger(WuzzufJobDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void plot(final Map<String, List<WuzzufJob>> companiesJobsMap, final String title, final String seriesName) {
        Thread plotThread = new Thread(() -> {

            final XYChart chart = new XYChartBuilder().theme(Styler.ChartTheme.XChart)
                    .title(title)
                    .build();
            Map<String, Integer> companiesJobsMapModified = new HashMap<>();
            for (Map.Entry<String, List<WuzzufJob>> entry : companiesJobsMap.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue().size();
                companiesJobsMapModified.put(key, value);
            }
            List<String> xList = new ArrayList<>(companiesJobsMapModified.keySet());
            List<Integer> yList = new ArrayList<>(companiesJobsMapModified.values());
            XYSeries residualSeries = chart.addSeries(seriesName, yList);
            residualSeries.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            residualSeries.setMarker(new Circle()).setMarkerColor(Color.RED);

            JPanel panel = new XChartPanel<>(chart);
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
            frame.show();
        });
        plotThread.start();
    }

    
    
    
    
        
    public static String getDataSummary() {
        StringBuilder sb = new StringBuilder();
        String summary = "";
        try {
            Table data = Table.read().csv(csv_path);
            summary = data.summary().toString();

            String html_table_header = "<table style=\"width:100%\" border=\"1\"> "
                    + "<tr>";
            sb.append(html_table_header);

            List<String> columnNames = data.summary().columnNames();
            for (String columnName : columnNames) {
                sb.append("<th>" + columnName + "</th>");
            }
            sb.append("</tr>");

            Iterator<Row> iterator = data.summary().iterator();
            while (iterator.hasNext()) {
                Row next = iterator.next();
                sb.append("<tr>");
                for (String columnName : columnNames) {
                    String cell = next.getString(columnName);
                    sb.append("<td>" + cell + "</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
            //System.out.println("summary : " + summary);
        } catch (IOException ex) {
            Logger.getLogger(WuzzufJobDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public static String getDataStructure() {
        StringBuilder sb = new StringBuilder();
        try {
            Table data = Table.read().csv(csv_path);
            String html_table_header = "<table style=\"width:100%\" border=\"1\"> "
                    + "<tr>";
            sb.append(html_table_header);
            System.out.println(data.structure().toString());
            List<String> columnNames = data.structure().columnNames();
            for (String columnName : columnNames) {
                sb.append("<th>" + columnName + "</th>");
            }
            sb.append("</tr>");

            Iterator<Row> iterator = data.structure().iterator();
            while (iterator.hasNext()) {
                Row next = iterator.next();
                sb.append("<tr>");
                for (String columnName : columnNames) {
                    if (columnName.contains("Index")) {
                        int cell = next.getInt(columnName);
                        sb.append("<td>" + cell + "</td>");
                    } else {
                        String cell = next.getString(columnName);
                        sb.append("<td>" + cell + "</td>");
                    }
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        } catch (IOException ex) {
            Logger.getLogger(WuzzufJobDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }
    
}

class Slice {

    double value;
    Color color;

    public Slice(double value, Color color) {
        this.value = value;
        this.color = color;
    }
}

class MyComponent extends JComponent {

    Slice[] slices;

    MyComponent(final Map<String, List<WuzzufJob>> companiesJobsMap) {
        Map<String, Integer> companiesJobsMapModified = new HashMap<>();
        for (Map.Entry<String, List<WuzzufJob>> entry : companiesJobsMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue().size();
            companiesJobsMapModified.put(key, value);
        }
        List<String> xList = new ArrayList<>(companiesJobsMapModified.keySet());
        List<Integer> yList = new ArrayList<>(companiesJobsMapModified.values());
        Color[] colors = {Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN,
            Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};
        slices = new Slice[yList.size()];

        for (int i = 0; i < yList.size(); i++) {
            Integer y = yList.get(i);
            Color c = colors[i % colors.length];
            slices[i] = new Slice(y, c);
        }
    }

    /*
    public void paint(Graphics g) {
        drawPie((Graphics2D) g, getBounds(), slices);
    }

    void drawPie(Graphics2D g, Rectangle area, Slice[] slices) {
        double total = 0.0D;

        for (int i = 0; i < slices.length; i++) {
            total += slices[i].value;
        }
        double curValue = 0.0D;
        int startAngle = 0;
        for (int i = 0; i < slices.length; i++) {
            startAngle = (int) (curValue * 360 / total);
            int arcAngle = (int) (slices[i].value * 360 / total);
            g.setColor(slices[i].color);
            g.fillArc(area.x, area.y, area.width, area.height, startAngle, arcAngle);
            curValue += slices[i].value;
        }
    }*/
    public void paint(Graphics g) {
        drawPie((Graphics2D) g, getBounds(), slices);
    }

    void drawPie(Graphics2D g, Rectangle area, Slice[] slices) {
        double total = 0.0D;
        for (int i = 0; i < slices.length; i++) {
            total += slices[i].value;
        }
        double curValue = 0.0D;
        int startAngle = 0;
        for (int i = 0; i < slices.length; i++) {
            startAngle = (int) (curValue * 360 / total);
            int arcAngle = (int) (slices[i].value * 360 / total);

            g.setColor(slices[i].color);
            g.fillArc(area.x, area.y, area.width, area.height, startAngle, arcAngle);
            curValue += slices[i].value;
        }
    }

    
    
    

    
    
}
