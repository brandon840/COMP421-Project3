import java.sql.* ;
import java.util.Scanner;

class Soccer
{
    static void printMainMenu(){
        System.out.println("Soccer Main Menu");
        System.out.println("\t 1. List information of matches of a country");
        System.out.println("\t 2. Insert initial player information for a match");
        System.out.println("\t 3. For you to design");
        System.out.println("\t 4. Exit application");
        System.out.print("Please Enter Your Option: ");
    }

    static void listInfoCountry(int sqlCode, String sqlState, Statement statement, Connection con){
        Scanner reader = new Scanner(System.in);
        String[] rounds = {"group-round", "round-of-16", "quarterfinals", "semifinals", "3rd-place", "final"};
        String country;
        String choice;

        do {
            System.out.print("Enter a country name: ");
            country = reader.next();

            // Query to get
            try
            {
                String querySQL = "SELECT t1.team_country, t2.team_country, m.match_date, m.round_number\n" +
                        "FROM teamPlays t1 \n" +
                        "JOIN teamPlays t2 ON t1.match_id = t2.match_id\n" +
                        "JOIN match m ON m.match_id = t1.match_id\n" +
                        "WHERE t1.team_country != t2.team_country AND t1.team_country =" + country + ";";

                System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    String team_country1 = rs.getString (2);
                    String team_country2 = rs.getString (3);
                    String match_date = rs.getString (4);
                    int round_number = rs.getInt ( 5 ) ;
                    System.out.println ("id:  " + id);
                    System.out.println ("name:  " + name);
                }
                System.out.println ("DONE");
            }
            catch (SQLException e)
            {
                sqlCode = e.getErrorCode(); // Get SQLCODE
                sqlState = e.getSQLState(); // Get SQLSTATE

                // Your code to handle errors comes here;
                // something more meaningful than a print would be good
                System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                System.out.println(e);
            }


            System.out.print("Enter [A] to find matches of another country, [P] to go to the previous menu: ");
            choice = reader.next();

        }while(choice == "A");


    }

    static void initialPlayerInfo(int sqlCode, String sqlState, Statement statement, Connection con) {
        System.out.println("Matches: ");
        /*
            TODO: List all matches that will take place in the next 3 days
         */

        System.out.print("Enter match identifier and country seperated by a space: ");
        Scanner reader = new Scanner(System.in);
        String[] choice = reader.next().split(" ");
        int matchID = Integer.parseInt(choice[0]);
        String country = choice[1];
        String playerID = reader.next();

        do {

        /*
            TODO: Print all players of the chosen country that are registered to play in the selected matchID
         */

            System.out.print("Enter the number of the player you want to insert or [P] to go to the previous menu: ");

            if (playerID.equals("P")) {
                return;
            }
            System.out.print("Enter the specific position the player will have: ");
            String playerPosition = reader.next();

        /*
            TODO: Enter this new player into the database with default values for the other attributes
            TODO: Print same output as above + new player
         */
        } while (true);
    }
    static void forYouToDesign(int sqlCode, String sqlState, Statement statement, Connection con){

    }



    public static void main ( String [ ] args ) throws SQLException
    {
        // Unique table names.  Either the user supplies a unique identifier as a command line argument, or the program makes one up.
        String tableName = "";
        int sqlCode=0;      // Variable to hold SQLCODE
        String sqlState="00000";  // Variable to hold SQLSTATE

        if ( args.length > 0 )
            tableName += args [ 0 ] ;
        else
            tableName += "exampletbl";

        // Register the driver.  You must register the driver before you can use it.
        try { DriverManager.registerDriver ( new com.ibm.db2.jcc.DB2Driver() ) ; }
        catch (Exception cnfe){ System.out.println("Class not found"); }

        // This is the url you must use for DB2.
        //Note: This url may not valid now ! Check for the correct year and semester and server name.
        String url = "jdbc:db2://winter2023-comp421.cs.mcgill.ca:50000/cs421";

        //REMEMBER to remove your user id and password before submitting your code!!
        String your_userid = "cs421g47";
        String your_password = "BrandonSevag421";
        //AS AN ALTERNATIVE, you can just set your password in the shell environment in the Unix (as shown below) and read it from there.
        //$  export SOCSPASSWD=yoursocspasswd 
        if(your_userid == null && (your_userid = System.getenv("SOCSUSER")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        if(your_password == null && (your_password = System.getenv("SOCSPASSWD")) == null)
        {
            System.err.println("Error!! do not have a password to connect to the database!");
            System.exit(1);
        }
        Connection con = DriverManager.getConnection (url,your_userid,your_password) ;
        Statement statement = con.createStatement ( ) ;

        Scanner reader = new Scanner(System.in);
        int choice;

        do {
            printMainMenu();
            choice = reader.nextInt();

            if (choice == 1){
                listInfoCountry(sqlCode, sqlState, statement, con);
            }
            else if (choice == 2){
                initialPlayerInfo(sqlCode, sqlState, statement, con);

            }else if (choice == 3){
                forYouToDesign(sqlCode, sqlState, statement, con);

            }else{
                // Close statement and connection
                statement.close ( ) ;
                con.close ( ) ;
            }

        }while(choice != 4);

        // Creating a table
        try
        {
            String createSQL = "CREATE TABLE " + tableName + " (id INTEGER, name VARCHAR (25)) ";
            System.out.println (createSQL ) ;
            statement.executeUpdate (createSQL ) ;
            System.out.println ("DONE");
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        // Inserting Data into the table
        try
        {
            String insertSQL = "INSERT INTO " + tableName + " VALUES ( 1 , \'Vicki\' ) " ;
            System.out.println ( insertSQL ) ;
            statement.executeUpdate ( insertSQL ) ;
            System.out.println ( "DONE" ) ;

            insertSQL = "INSERT INTO " + tableName + " VALUES ( 2 , \'Vera\' ) " ;
            System.out.println ( insertSQL ) ;
            statement.executeUpdate ( insertSQL ) ;
            System.out.println ( "DONE" ) ;
            insertSQL = "INSERT INTO " + tableName + " VALUES ( 3 , \'Franca\' ) " ;
            System.out.println ( insertSQL ) ;
            statement.executeUpdate ( insertSQL ) ;
            System.out.println ( "DONE" ) ;

        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        // Querying a table
        try
        {
            String querySQL = "SELECT id, name from " + tableName + " WHERE NAME = \'Vicki\'";
            System.out.println (querySQL) ;
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

            while ( rs.next ( ) )
            {
                int id = rs.getInt ( 1 ) ;
                String name = rs.getString (2);
                System.out.println ("id:  " + id);
                System.out.println ("name:  " + name);
            }
            System.out.println ("DONE");
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        //Updating a table
        try
        {
            String updateSQL = "UPDATE " + tableName + " SET NAME = \'Mimi\' WHERE id = 3";
            System.out.println(updateSQL);
            statement.executeUpdate(updateSQL);
            System.out.println("DONE");

            // Dropping a table
            String dropSQL = "DROP TABLE " + tableName;
            System.out.println ( dropSQL ) ;
            statement.executeUpdate ( dropSQL ) ;
            System.out.println ("DONE");
        }
        catch (SQLException e)
        {
            sqlCode = e.getErrorCode(); // Get SQLCODE
            sqlState = e.getSQLState(); // Get SQLSTATE

            // Your code to handle errors comes here;
            // something more meaningful than a print would be good
            System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
            System.out.println(e);
        }

        // Finally but importantly close the statement and connection
        statement.close ( ) ;
        con.close ( ) ;
    }
}
