import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        String FIID = "04715";
        String BB_DBURL = "jdbc:oracle:thin:@//asdev11oclon100.dca.diginsite.net:1526/BBDEV1";
        String BB_USERNAME = "usp_app";
        String BB_PASSWORD = "USPappd3v";
        String EXP_DBURL = "jdbc:oracle:thin:@//asdev11oclon100.dca.diginsite.net:1526/EXPDEV1";
        String EXP_USERNAME = "EXPERIENCE_SVC_APP";
        String EXP_PASSWORD = "exp123456";

        try {
            Class.forName("oracle.jdbc.OracleDriver");

            Map<String, String> segmentExperienceMap = new HashMap<String, String>();

            Connection expCon = DriverManager.getConnection(
                    EXP_DBURL, EXP_USERNAME, EXP_PASSWORD);
            Statement expStmt = expCon.createStatement();
            ResultSet expRs = expStmt.executeQuery("select experience_id,segment_id from experience_svc.experience where segment_id is not null and is_published_copy = 1 and institution_id=" + FIID);
            while (expRs.next()) {
                segmentExperienceMap.put(expRs.getString("SEGMENT_ID"), expRs.getString("EXPERIENCE_ID"));
            }
            expCon.close();

            Connection bbCon = DriverManager.getConnection(
                    BB_DBURL, BB_USERNAME, BB_PASSWORD);
            String updateExpIdString =
                    "update USP_FI1001.BB_COMPANY set EXPERIENCE_ID = ? where SEGMENT_ID = ?";

            try  {
                PreparedStatement updateExpIdStmt = bbCon.prepareStatement(updateExpIdString);
                bbCon.setAutoCommit(false);
                for (Map.Entry<String, String> entry : segmentExperienceMap.entrySet()) {
                    System.out.println("Segment ID:  "+entry.getKey() + " Experience ID: " + entry.getValue());
                    updateExpIdStmt.setString(1, entry.getValue());
                    updateExpIdStmt.setString(2, entry.getKey());
                    updateExpIdStmt.executeUpdate();
                    bbCon.commit();
                }
            } catch (SQLException e) {
                System.out.println(e);
                if (bbCon != null) {
                    try {
                        System.err.print("Transaction is being rolled back");
                        bbCon.rollback();
                    } catch (SQLException excep) {
                        System.out.println(excep);
                    }
                }
            }
            finally {
                bbCon.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}


