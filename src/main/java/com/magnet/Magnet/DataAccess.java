package com.magnet.Magnet;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataAccess {
    private Connection connection;
    // microsoft driverClassName for sql server
    private static final String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    String hostName = "localhost";
    String database = "MagnetSG";

    // obtain a connection to the database "MagnetSG"
    public DataAccess() {
        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection("jdbc:sqlserver://" + hostName + ";databaseName=" + database
                    + ";encrypt=true;trustServerCertificate=true;hostNameInCertificate=*.database.windows.net;loginTimeout=60;",
                    "mousamax", "fastopen");
            System.out.println("Connection successful");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    // add visited url to the database table "CrawlerData"
    public void addVisitedUrl(String url) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO CrawlerData (Urls) Values(?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, url);
            statement.executeUpdate();
            // Execute the query
            // int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // addVisitedUrlandCompactPages to the database table "CrawlerData"
    public void addVisitedUrlandCompactPagesFilename(String url, String compactPages, String filename) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO CrawlerData (Urls, CompactPages, Filename) Values(?, ?, ?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, url);
            statement.setString(2, compactPages);
            statement.setString(3, filename);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the visited urls from the database table "CrawlerData"
    public void getVisitedUrls(ConcurrentHashMap<String, Boolean> visitedUrls) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM CrawlerData";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the visited urls in the ConcurrentHashMap
            while (rs.next()) {
                // add to the visited urls hashmap
                visitedUrls.put(rs.getString("Urls"), true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add UrlsToBeCrawled array to the database table "UrlsToBeCrawled"
    public void addUrlsToBeCrawled(ConcurrentHashMap<String, Boolean> urlsToBeCrawled, String Url) {
        try {
            // get url id from the database table "CrawlerData" where Url = Url
            Statement st0 = connection.createStatement();
            String query = "SELECT Id FROM CrawlerData WHERE Urls = N'" + Url + "'";
            ResultSet rs = st0.executeQuery(query);
            int urlID = 0;
            while (rs.next()) {
                urlID = rs.getInt("Id");
            }
            // Obtain a statement
            Statement st = connection.createStatement();
            // insert the map of urls to be crawled into the database table
            // "UrlsToBeCrawled"
            String queryTobeCrawled = "";
            String queryToHyperLinks = "";

            for (String url : urlsToBeCrawled.keySet()) {
                queryTobeCrawled += "INSERT INTO UrlsToBeCrawled (Urls) Select N'" + url + "' ;";
                queryToHyperLinks += "INSERT INTO HyperLinks (UrlId,InnerUrl) Select " + urlID + " , N'" + url + "' ;";
            }
            // Execute the query
            int count = st.executeUpdate(queryTobeCrawled);
            Statement st2 = connection.createStatement();
            int count2 = st2.executeUpdate(queryToHyperLinks);
        } catch (SQLException e) {
            System.out.println("ignored duplicate url");
        }
    }

    // retrieve all the urls to be crawled from the database table "UrlsToBeCrawled"
    public void getUrlsToBeCrawled(ConcurrentHashMap<String, Boolean> urlsToBeCrawled) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM UrlsToBeCrawled";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the urls to be crawled in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                urlsToBeCrawled.put(url, true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete all the urls to be crawled from the database table "UrlsToBeCrawled"
    public void deleteUrlsToBeCrawled() {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "DELETE FROM UrlsToBeCrawled;";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add compactPage to the database table "CrawlerData" for a given url
    public void addCompactPage(String url, String compactPage) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE CrawlerData SET CompactPages = N'" + compactPage + "' WHERE Urls = N'" + url + "';";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the compactPages from the database table "CrawlerData" into a
    // ConcurrentHashMap
    public void getCompactPages(ConcurrentHashMap<String, String> compactPages) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT * FROM CrawlerData";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // add to the compactPages hashmap
                compactPages.put(rs.getString("CompactPages"), rs.getString("Urls"));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // retrieve all the compactPages from the database table "CrawlerData" into a
    // ConcurrentHashMap
    public void getUrlsAndCompactPages(ConcurrentHashMap<String, Boolean> visitedUrls,
            ConcurrentHashMap<String, String> compactPages) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT * FROM CrawlerData;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            int i = 0;
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // add to the compactPages hashmap
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                if (rs.getString("CompactPages") != null) {
                    String compactPage = rs.getString("CompactPages").replaceAll("\\s+", "");
                    compactPages.put(compactPage, url);
                } else
                    compactPages.put("state:" + i, url);
                visitedUrls.put(url, true);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // getNumOfCrawledFiles from the database table "CrawlerData" count "filename"
    // column
    public int getNumOfCrawledFiles() {
        int num = 0;
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "Select COUNT(FILENAME) as count from CrawlerData;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                num = rs.getInt("count");
            }
        } catch (SQLException e) {
        }
        return num;
    }

    // add query to the database table "SearchData" for a given query
    public void addQuery(String query) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query1 = "INSERT INTO SearchData (Query) VALUES (N'" + query + "');";
            // Execute the query
            int count = st.executeUpdate(query1);
        } catch (SQLException e) {
        }
    }

    // get all non-indexed files from table "CrawlerData"
    // return an array list
    public ArrayList<String> getNonIndexedFiles() {
        ArrayList<String> nonIndexedFiles = new ArrayList<String>();
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            // select all the non-indexed and not null files
            String query = "SELECT FILENAME FROM CrawlerData WHERE INDEXED = 0 AND FILENAME IS NOT NULL;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                nonIndexedFiles.add(rs.getString("FILENAME"));
            }
        } catch (SQLException e) {
        }
        return nonIndexedFiles;
    }

    // markFileAsIndexed in the database table "CrawlerData" in column "INDEXED"
    public void markFileAsIndexed(String filename) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE CrawlerData SET INDEXED = 1 WHERE FILENAME = N'" + filename + "';";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
        }
    }

    // Select all from the database table "SearchData"
    // return in a list
    public ArrayList<String> getQueries() {
        ArrayList<String> queries = new ArrayList<String>();
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Query FROM SearchData;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                queries.add(rs.getString("Query"));
            }
        } catch (SQLException e) {
        }
        return queries;
    }

    // getUrl from the database table "CrawlerData" for a file name
    public String getUrl(String fileName) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Urls FROM CrawlerData WHERE FileName = N'" + fileName + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                String url = rs.getString("Urls").replaceAll("\\s+", "");
                return url;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    // --------------------------------------------------------------

    // Check if StemTerm exists in database table "StemWords"
    // and return its id
    public int getStemTermId(String stemTerm) {
        String query = "";
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            query = "SELECT Id FROM StemWords WHERE StemTerm = N'" + stemTerm + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            // print query
            System.out.println("getStemTermId:" + query);
        }
        return -1;
    }

    // Check if originalWord exists in table "OriginalWords"
    // and return its id
    public int getOriginalWordId(String originalWord) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id FROM OriginalWords WHERE OriginalWord = N'" + originalWord + "';";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Check if file exists in table "FilesAndScores"
    // given a filename and originalWordId
    // and return its id
    public int getFileId(String fileName, int originalWordId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id FROM FilesAndScores WHERE FileName = N'" + fileName + "' AND OriginalWordId = "
                    + originalWordId + ";";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add StemTerm to table "StemWords" and return its id
    public int addStemTerm(String stemTerm) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO StemWords (StemTerm) VALUES (N'" + stemTerm + "');";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM StemWords WHERE StemTerm = N'" + stemTerm + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Add originalWord to table "OriginalWords" and return its id
    // given a stem id
    public int addOriginalWord(String originalWord, int stemId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO OriginalWords (OriginalWord, StemId) VALUES (N'" + originalWord + "', " + stemId
                    + ");";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM OriginalWords WHERE OriginalWord = N'" + originalWord + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stemId;
    }
    // START OF RANKER QUERIES

    public void getRelatedUrls(ConcurrentHashMap<String, List<String>> urlsPointingToMe,
            ConcurrentHashMap<String, Integer> urlsCountMap, ConcurrentHashMap<String, Double> urlPopularityMap,
            int totalLinksNum) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT CD.Urls as Url, H.InnerUrl FROM CrawlerData CD, HyperLinks H WHERE CD.Id = H.UrlId";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the urls and innerUrl in the Maps
            while (rs.next()) {
                // add to the urlsPointingToMe
                List<String> urlList = urlsPointingToMe.get(rs.getString("InnerUrl"));
                if (urlList == null) {
                    // Did not find the InnerUrl before
                    // * Stream.collect(Collectors.toList()); This makes the list modifiable
                    // Initialize the list with the first Url
                    urlList = Stream.of(rs.getString("Url")).collect(Collectors.toList());
                    // Add list of Urls to the map with InnerUrl as the key
                    urlsPointingToMe.put(rs.getString("InnerUrl"), urlList);
                    // Assign initial popularity
                    Double popu = 1.0 / totalLinksNum;
                    urlPopularityMap.put(rs.getString("InnerUrl"), popu);
                } else {
                    // Found the InnerUrl
                    // So add the url to its urlList
                    urlList.add(rs.getString("Url"));
                }

                Integer numberOfUrlsIamPointingTo = urlsCountMap.get(rs.getString("Url"));
                if (numberOfUrlsIamPointingTo == null) {
                    // Did not find the Url
                    urlsCountMap.put(rs.getString("Url"), 1);
                } else {
                    // Found the Url
                    // So increment the numberOfUrlsIamPointingTo
                    urlsCountMap.put(rs.getString("Url"), numberOfUrlsIamPointingTo + 1);
                }
            }
        } catch (SQLException e) {
        }
    }

    // get count of all the urls in the database table "CrawlerData"
    public int getCountOfUrls() {
        int count = 0;
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT COUNT (DISTINCT InnerUrl) AS 'COUNT' FROM HyperLinks;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // Update Urls Popularity
    public void addUrlsPopularity(String[] urlsArray, Double[] popularitiesArray, int start, int end) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            // insert the map of urls to be crawled into the database table
            String query = "";
            // UPDATE CrawlerData SET Popularity = 2.0202 WHERE Urls='A';
            for (int i = start; i < end; i++) {
                query += "UPDATE CrawlerData SET Popularity = " + popularitiesArray[i] +
                        " WHERE Urls='" + urlsArray[i] + "';";
            }
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
        }
    }

    public void getFilenameWithPopularity(LinkedHashMap<String, Double> fileNamePopularityMap) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Filename, Popularity FROM CrawlerData WHERE Filename IS NOT NULL";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the urls and innerUrl in the Maps
            while (rs.next()) {
                String popularityString = rs.getString("Popularity");
                Double popularity = Double.parseDouble(popularityString);
                fileNamePopularityMap.put(rs.getString("Filename"), popularity);
            }
        } catch (SQLException e) {
        }
    }

    // END OF RANKER QUERIES
    // Add fileName and score to table "FilesAndScores" and return its id
    // given an originalWordId
    public int addFile(String fileName, double score, int originalWordId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "INSERT INTO FilesAndScores (FileName, Score, OriginalWordId) VALUES (N'" + fileName + "', "
                    + score + ", " + originalWordId + ");";
            // Execute the query
            int count = st.executeUpdate(query);
            // Obtain a statement
            Statement st1 = connection.createStatement();
            String query1 = "SELECT Id FROM FilesAndScores WHERE FileName = N'" + fileName + "';";
            // Execute the query
            ResultSet rs = st1.executeQuery(query1);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int id = rs.getInt("Id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Get score from table "FilesAndScores"
    // given a fileId
    public double getScore(int fileId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Score FROM FilesAndScores WHERE Id = " + fileId + ";";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                double score = rs.getDouble("Score");
                return score;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Update the score of a file in table "FilesAndScores"
    // given a fileId and a score
    public void updateFileScore(int fileId, double score) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE FilesAndScores SET Score = " + score + " WHERE Id = " + fileId + ";";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Increment number of documents in table "StemWords"
    // given a stemId
    public void incrementNumberOfDocuments(int stemId) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE StemWords SET NumDocs = NumDocs + 1 WHERE Id = " + stemId + ";";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get all terms and the number of documents of each term
    public HashMap<Integer, Integer> getAllTermsAndNumberOfDocuments() {
        HashMap<Integer, Integer> termsAndNumberOfDocuments = new HashMap<Integer, Integer>();
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "SELECT Id, NumDocs FROM StemWords;";
            // Execute the query
            ResultSet rs = st.executeQuery(query);
            // Store the compactPages in the ConcurrentHashMap
            while (rs.next()) {
                // remove spaces from the url
                int termId = rs.getInt("Id");
                int numDocs = rs.getInt("NumDocs");
                termsAndNumberOfDocuments.put(termId, numDocs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return termsAndNumberOfDocuments;
    }

    // Set IDF in table "StemWords"
    public void setIDF(int stemId, double IDF) {
        try {
            // Obtain a statement
            Statement st = connection.createStatement();
            String query = "UPDATE StemWords SET IDF = " + IDF + " WHERE Id = " + stemId + ";";
            // Execute the query
            int count = st.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // close the connection
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
