package com.example.WuzzuffJobsAnalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Hyperlink;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

@SpringBootApplication
@RestController
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@ComponentScan({"com.example.demo"})

public class WuzzuffJobsAnalysisMain {

    static String csv_path = "src/main/resources/Wuzzuf_Jobs.csv";
    SparkSession sparkSession;
    public SparkConf sparkConfig;
    JavaSparkContext context;
    public static String line = "<br>";
    String c = "  :  ";
    static WuzzufJobDAO jobDAO0;
    static JavaRDD<WuzzufJob> jobRDD;
    static List<WuzzufJob> allJobs;
    static String center = "<center>";
    static String centerclose = "</center>";
    /*static String refresh = "<head>"
            + "<meta http-equiv=\"refresh\" content=\"3\">"
            + "</head>";
     */
    public static String refresh(String p) {
        String refresh = "<script type='text/javascript'>\n" +
"\n" +
"(function()\n" +
"{\n" +
"  if( window.localStorage )\n" +
"  {\n" +
"    if( !localStorage.getItem('firstLoad') )\n" +
"    {\n" +
"      localStorage['firstLoad'] = true;\n" +
"      window.location.reload();\n" +
"    }  \n" +
"    else\n" +
"      localStorage.removeItem('firstLoad');\n" +
"  }\n" +
"})();\n" +
"\n" +
"</script>";
        return refresh;
    }

    public static void main(String[] args) {
        SpringApplication.run(WuzzuffJobsAnalysisMain.class,
                 args);

        jobDAO0 = new WuzzufJobDAO();
        jobDAO0.sparkConfig = new SparkConf().setAppName("WuzzufJobs").setMaster("local[2]");
        jobDAO0.context = new JavaSparkContext(jobDAO0.sparkConfig);

        jobDAO0.sparkSession = SparkSession
                .builder()
                .appName("WuzzufJobs")
                .getOrCreate();
        jobRDD = jobDAO0.getJobRDD();
        allJobs = jobDAO0.getAllJobs(jobRDD);
    }

    @GetMapping("/index")
    public static Hyperlink index() {
        String s1 = "index.html";
        Hyperlink link = new Hyperlink();
        link.setText("index.html");
        return link;
    }
    @GetMapping("/about")
    public static Hyperlink about() {
        String s1 = "about.html";
        Hyperlink link = new Hyperlink();
        link.setText("about.html");
        return link;
    }

    @GetMapping("/index1")
    public String index1() {
        return "contact";
    }

    @GetMapping("/data1")
    public static String data1() {
        String s1 = "<head>\n" + "  <title>Read data set and convert it to Spark RDD</title>\n" + "</head>";
        //String s1 = "<center><h1>Read data set and convert it to Spark RDD</h1></center>";
        String s11 = "<center><h2>Top 10 records</h2></center>";
        //System.out.println("jobRDD : " + jobRDD.count());
        List<WuzzufJob> firs_10_jobs = jobDAO0.getFirst10Jobs(jobRDD);
        //String first_10_jobs = "";
        String firts10job = jobDAO0.listAllJobsToString(firs_10_jobs);
        String s12 = "<center><h2>Read All Data</h2></center>";
        String alljob = jobDAO0.listAllJobsToString(allJobs);

        //String res = String.join(line, allJobs);
        // String res = StringUtils.join(allJobs, line);
        return (s1 + line + s11 + firts10job + s12 + alljob);
    }

    @GetMapping("/data2")
    public static String data2() {
        String s1 = "<center><h1>Display summary of the data</h1></center>";
        String s2 = "<center><h1>Display structure of the data</h1></center>";

        String dataSummary = jobDAO0.getDataSummary();
        String dataStructure = jobDAO0.getDataStructure();

        return (s2 + line + dataStructure + s1 + line + dataSummary);
    }
    
    
    
/*
    @GetMapping("/data2")
    public static String data2() {

        String s2 = "<center><h1>Display structure and summary of the data</h1></center>";
        String dataStstisticsUsingJoinery = jobDAO0.getDataStstisticsUsingJoinery();

        return (s2 + line + dataStstisticsUsingJoinery);
    }
*/
    @GetMapping("/data3")
    public static String data3() {

        JavaRDD<WuzzufJob> distinct_jobRDD = jobRDD.distinct();
        List<WuzzufJob> all_distinct_Jobs = jobDAO0.getAllJobs(distinct_jobRDD);
        String s3 = "<center><h1>Clean the data (null, duplications)</h1></center>";
        String alljob = jobDAO0.listAllJobsToString(all_distinct_Jobs);
        return (s3 + line + alljob);
    }

    @GetMapping("/data4")
    public static String data4() {
        String s4 = "<center><h1>Count the jobs for each company and display that in order</h1></center>";
        Map<String, List<WuzzufJob>> companiesJobsMap = jobDAO0.getCompaniesJobs(allJobs);
        Map<String, List<WuzzufJob>> sortedCompaniesJobsMap = jobDAO0.getSortedCompaniesJobs(companiesJobsMap);

        String maxDemandingCompany = jobDAO0.getMaxDemandingCompany(companiesJobsMap);

        String s44 = "<center><h2>Max Demandeing Company</h2></center>";
        String maxcomp = "<center><h3> The Most Demanding Company for Jobs : " + maxDemandingCompany + " Compnay it has  " + companiesJobsMap.get(maxDemandingCompany).size() + " Jobs</h3></center>";

        String s444 = "<center><h2>Most Repeated Jobs for each Company</h2></center>";
        String alljob = jobDAO0.listAllJobsToString(companiesJobsMap);
        String s4444 = "<center><h2>Most Demandeing Companies For Jobs</h2></center>";
        String alljobSorted = jobDAO0.listAllJobsToString(sortedCompaniesJobsMap);
        return (s4 + line + s44 + maxcomp + s444 + line + alljobSorted);
    }

    @GetMapping("/data5")
    public static String data5() {

        String s5 = "<center><h1>Pie Chart For Most Demanding Companies for Jobs</h1></center>";
        String s55 = "<head>\n"
                + "  <title>Most Demanding Companies for Jobs Pie Chart</title>\n"
                + "</head>";
        Map<String, List<WuzzufJob>> companiesJobsMap = jobDAO0.getCompaniesJobs(allJobs);
        jobDAO0.drawPieChart(companiesJobsMap, "Companies_jobs");

        //String img = "<img src=\"src/main/resources/charts/Sample_Chart.png\" runat=\"localhost\">";
        String img = "<center><img src=\"img/Companies_jobs.jpg\" ></center>";

        return (s55 + s5 + img + refresh("/data5"));
    }

    @GetMapping("/data6")
    public static String data6() {
        String s6 = "<center><h1>Find out What are it the most popular job titles</h1></center>";

        Map<String, List<WuzzufJob>> jobTitles = jobDAO0.getJobTitles(allJobs);
        List<Object> jobTitlesKeys = jobDAO0.getSortedJobTitles(jobTitles);

        List<Object> topJobTitles = jobDAO0.getTopJobTitles(jobTitlesKeys);

        String s66 = "<center><h1> The most 10 popular job titles</h1></center>";
        String listTopJobTitles = WuzzufJobDAO.listAllMapDataToString(jobTitles, topJobTitles, "Job Title", "Job Title Count");
        String s666 = "<center><h1>The  most popular job titles</h1></center>";
        String listAllJobTitles = WuzzufJobDAO.listAllMapDataToString(jobTitles, jobTitlesKeys, "Job Title", "Job Title Count");

        return (s6 + line + s66 + line + listTopJobTitles + line + s666 + line + listAllJobTitles);
    }

    @GetMapping("/data7")
    public static String data7() {
        String s7 = "<center><h1>Bar Chart For Most Popular Job Titles</h1></center>";
        String s77 = "<head>\n"
                + "  <title>Most Popular Job Titles</title>\n"
                + "</head>";
        Map<String, List<WuzzufJob>> jobTitles = jobDAO0.getJobTitles(allJobs);
        List<Object> jobTitlesKeys = jobDAO0.getSortedJobTitles(jobTitles);

        List<Object> topJobTitles = jobDAO0.getTopJobTitles(jobTitlesKeys);
        jobDAO0.drawBarChart(jobTitles, topJobTitles, "Popular_Job_Titles", "Job Titles");

        String img = "<center><img src=\"img/Popular_Job_Titles.jpg\" </center>>";

        return (s7 + s77 + img + refresh("/data7"));
    }

    @GetMapping("/data8")
    public static String data8() {
        String s6 = "<center><h1>Find out the most popular areas?</h1></center>";

        Map<String, List<WuzzufJob>> jobAreas = jobDAO0.getAreas(allJobs);
        List<Object> jobAreasKeys = jobDAO0.getSortedJobTitles(jobAreas);

        List<Object> topAreas = jobDAO0.getTopAreas(jobAreasKeys);

        String s66 = "<center><h1> The most 10 popular Areas</h1></center>";
        String listTopJobTitles = WuzzufJobDAO.listAllMapDataToString(jobAreas, topAreas, "Job Area", "Job Area Count");
        String s666 = "<center><h1>The most popular Areas</h1></center>";
        String listAllJobTitles = WuzzufJobDAO.listAllMapDataToString(jobAreas, jobAreasKeys, "Job Area", "Job Area Count");

        return (s6 + line + s66 + line + listTopJobTitles + line + s666 + line + listAllJobTitles);
    }

    @GetMapping("/data9")
    public static String data9() {
        String s8 = "<center><h1>Bar Chart For Most Popular Areas For Job</h1></center>";
        String s88 = "<head>\n"
                + "  <title>Most Most Popular Areas For Jobs</title>\n"
                + "</head>";
        Map<String, List<WuzzufJob>> jobAreas = jobDAO0.getAreas(allJobs);
        List<Object> jobAreasKeys = jobDAO0.getSortedJobTitles(jobAreas);

        List<Object> topAreas = jobDAO0.getTopAreas(jobAreasKeys);
        jobDAO0.drawBarChart(jobAreas, topAreas, "Most_Popular_Areas", "Areas");
        // String img = "<img src=\"src/main/resources/charts/Sample_Chart.png\" runat=\"localhost\">";
        String img = "<center><img src=\"img/Most_Popular_Areas.jpg\" ></center>";
        return (s88 + s8 + img + refresh("/data9"));
    }

    @GetMapping("/data10")
    public static String data10() {
        String s10 = "<center><h1>Find out the most popular Skills?</h1></center>";

        List<String> allSkills = jobDAO0.getAllSkills(allJobs);
        Map<String, Long> skillsCount = jobDAO0.getSkillsCount(allSkills);
        List<Object> sortedSkills = jobDAO0.getSortedSkills(skillsCount);
        List<Object> topSkills = jobDAO0.getTopSkills(sortedSkills);

        String s1010 = "<center><h1> The most 10 popular Skills</h1></center>";
        String listTopJobTitles = WuzzufJobDAO.listAllDataToString(skillsCount, topSkills, "Job Skills", "Job Skills Count");
        String s101010 = "<center><h1>The most popular Skills</h1></center>";
        String listAllJobTitles = WuzzufJobDAO.listAllDataToString(skillsCount, sortedSkills, "Job Skills", "Job Skills Count");

        return (s10 + line + s1010 + line +  listTopJobTitles + line + s101010 + line + listAllJobTitles);
    }

    @GetMapping("/data11")
    public static String data11() {
        String s11 = "<center><h1>Bar Chart For Most Popular Skills Required For Jobs</h1></center>";
        String s1111 = "<head>\n"
                + "  <title>Most Popular Skills Required For Jobs</title>\n"
                + "</head>";
        List<String> allSkills = jobDAO0.getAllSkills(allJobs);
        Map<String, Long> skillsCount = jobDAO0.getSkillsCount(allSkills);
        List<Object> sortedSkills = jobDAO0.getSortedSkills(skillsCount);
        List<Object> topSkills = jobDAO0.getTopSkills(sortedSkills);
        jobDAO0.drawSkillsBarChart(skillsCount, topSkills, "Most_Popular_Skills", "Skills");
        
        String img = "<center><img src=\"img/Most_Popular_Skills.jpg\" ></center>";
        return (s11 + s1111 + img + refresh("/data11"));
    }

}
