import java.sql.* ;
import java.util.*;

class Soccer
{
    static String[] rounds = {"group-round", "round-of-16", "quarterfinals", "semifinals", "3rd-place", "final"};
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
        Map<Integer,  ArrayList<String>> dict = new HashMap<Integer, ArrayList<String>>();

        String country;
        String choice;

        do {
            System.out.print("Enter a country name: ");
            country = reader.next();

            // Query to get team_country1, team_country2, match date and round_number
            try
            {
                String querySQL = "SELECT t1.match_id, t1.team_country, t2.team_country, m.match_date, m.round_number\n" +
                        "FROM teamPlays t1 \n" +
                        "JOIN teamPlays t2 ON t1.match_id = t2.match_id\n" +
                        "JOIN match m ON m.match_id = t1.match_id\n" +
                        "WHERE t1.team_country != t2.team_country AND t1.team_country =" + country + ";";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String team_country1 = rs.getString (2);
                    String team_country2 = rs.getString (3);
                    String match_date = rs.getString (4);
                    int round_number = rs.getInt ( 5 );
                    ArrayList<String> content = new ArrayList<String>();
                    Collections.addAll(content, team_country1,team_country2, match_date, rounds[round_number]);
                    dict.put(match_id, content);
                    System.out.print(dict.get(match_id));
                }
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

            // Query to get number of goals scored by each team
            try
            {
                String querySQL = "WITH res AS\n" +
                        "(\n" +
                        "SELECT t.match_id, t.team_country, COUNT(s.goal_id) AS num_goals_scored\n" +
                        "FROM scoredIn s \n" +
                        "JOIN goal g ON s.goal_id = g.goal_id\n" +
                        "JOIN isPartOf i ON g.player_id = i.player_id\n" +
                        "RIGHT JOIN teamPlays t ON i.team_country=t.team_country\n" +
                        "WHERE t.match_id in \n" +
                        "(SELECT match_id\n" +
                        "FROM teamPlays  \n" +
                        "WHERE team_country = 'Argentina')\n" +
                        "GROUP BY t.match_id, t.team_country\n" +
                        ")\n" +
                        "SELECT r1.match_id, r1.team_country, r2.team_country, r1.num_goals_scored, r2.num_goals_scored\n" +
                        "FROM res r1 JOIN res r2 ON r1.match_id=r2.match_id\n" +
                        "WHERE r1.team_country != r2.team_country AND r1.team_country = 'Argentina';";

                System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String team1 = rs.getString (2);
                    String team2 = rs.getString (3);
                    String num_goals_scored_team1 = Integer.toString(rs.getInt (4));
                    String num_goals_scored_team2 = Integer.toString(rs.getInt (5));

                    // This is just in case the country orders are switched
                    if (dict.get(match_id).get(0).equals(team1)){
                        dict.get(match_id).add(num_goals_scored_team1);
                        dict.get(match_id).add(num_goals_scored_team2);
                    }else{
                        dict.get(match_id).add(num_goals_scored_team2);
                        dict.get(match_id).add(num_goals_scored_team1);
                    }

                }
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

            // Query to get ticket sales
            try
            {
                String querySQL = "SELECT match_id, COUNT(ticket_id) AS num_tickets_sold\n" +
                        "FROM pertainsTo\n" +
                        "GROUP BY match_id;";

                System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String num_tickets_sold = Integer.toString(rs.getInt(2));
                    dict.get(match_id).add(num_tickets_sold);
                }
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

            // Print query results
            for(Integer key: dict.keySet()){
                ArrayList<String> val = dict.get(key);
                String out = "";
                for (int i = 1; i<val.size(); i++){
                    out += val.get(i);
                    if (i < val.size()-1){
                        out += "\t";
                    }else {
                        out += "\n";
                    }
                }
                System.out.print(out);
            }

            System.out.print("Enter [A] to find matches of another country, [P] to go to the previous menu: ");
            choice = reader.next();

        }while(choice == "A");


    }

    static void initialPlayerInfo(int sqlCode, String sqlState, Statement statement, Connection con) {

        // Print all matches occurring in the next three days
        System.out.println("Matches: ");
        try
        {
            String querySQL = "SELECT match_id, team1, team2, match_date, round_number\n" +
                    "FROM match\n" +
                    "WHERE team1 IS NOT NULL AND team2 IS NOT NULL \n" +
                    "AND match_date BETWEEN CURRENT DATE AND ADD_DAYS(CURRENT_DATE, 3);";

            //System.out.println (querySQL);
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

            while ( rs.next ( ) )
            {
                int match_id = rs.getInt ( 0 );
                String team1 = rs.getString (1);
                String team2 = rs.getString (2);
                String match_date = rs.getString (3);
                String round_number = rounds[rs.getInt(4)];

                String out = "\t" + match_id + "\t" + team1 + "\t" + team2 + "\t" + match_date + "\t" + round_number;
                System.out.println(out);
            }
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

        System.out.print("Enter match identifier and country seperated by a space: ");
        Scanner reader = new Scanner(System.in);
        String[] choice = reader.next().split(" ");
        int matchID = Integer.parseInt(choice[0]);
        String country = choice[1];
        System.out.println("The following players from " + country + " are already entered for match " + matchID + ":\n");

        do {
            int numPlayers = 0; // Need this to keep track of number of players in the match

            // Print players already entered in the match
            try
            {
                String querySQL = "SELECT person.name, player.shirt_num, plays.detailed_position, plays.minute_entered, plays.minute_left, plays.num_yellow_cards, plays.had_red_card\n" +
                        "FROM player \n" +
                        "JOIN person ON player.player_id=person.person_id\n" +
                        "JOIN plays ON player.player_id=plays.player_id\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "WHERE isPartOf.team_country = "+ country +"\n" +
                        "AND plays.match_id= "+matchID+";\n";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    String name = rs.getString ( 0 );
                    int shirt_num = rs.getInt (1);
                    String position = rs.getString (2);
                    int minute_entered = rs.getInt (3);
                    int minute_left = rs.getInt (4);
                    int num_yellow_cards = rs.getInt (5);
                    int num_red_cards = rs.getInt (6);
                    numPlayers++;

                    String out = name + "\t" + shirt_num + "\t" + position + "\t" + minute_entered + "\t" + minute_left+ "\t" + num_yellow_cards + "\t" + num_red_cards;
                    System.out.println(out);
                }
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

            // Get remaining players
            System.out.println("Possible players from " + country+ " not yet selected:");
            ArrayList<String> remaining_player_names = new ArrayList<String>();
            ArrayList<Integer> remaining_player_ID = new ArrayList<Integer>();
            int i = 1;
            try
            {
                String querySQL = "SELECT person.name, player.shirt_num, player.position, player.player_id\n" +
                        "FROM player\n" +
                        "JOIN person ON player.player_id=person.person_id\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "WHERE isPartOf.team_country = 'Argentina'\n" +
                        "AND player.player_id NOT IN \n" +
                        "(SELECT plays.player_id \n" +
                        "FROM player\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "JOIN plays ON player.player_id=plays.player_id\n" +
                        "WHERE isPartOf.team_country="+country+" AND plays.match_id = "+matchID+"\n" +
                        ");";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
                while ( rs.next ( ) )
                {
                    String name = rs.getString ( 0 );
                    remaining_player_names.add(name);

                    int shirt_num = rs.getInt (1);
                    String position = rs.getString (2);

                    int playerID = rs.getInt(3);    //Need this for later
                    remaining_player_ID.add(playerID);

                    String out = i + ". " + name + "\t" + shirt_num + "\t" + position;
                    System.out.println(out);
                    i++;
                }
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

            if (numPlayers < 11){
                System.out.print("Enter the (choice) number of the player you want to insert or [P] to go to the previous menu: ");
                String input = reader.next();
                if (input.equals("P")) {
                    return;
                }

                System.out.print("Enter the specific position the player will have: ");
                String detailedPosition = reader.next();

                // Insert the player into "plays" table
                try
                {
                    String insertSQL = "INSERT INTO plays VALUES ("+matchID+","+ remaining_player_ID.get(i - 1) + ", 0, 0, 0, NULL, "+ detailedPosition +")";
                    statement.executeUpdate ( insertSQL ) ;

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
            }else{
                System.out.print("Max number of entered players (11) reached. Enter [P] to go back to the main menu: ");
                return;
            }



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

        } while(choice != 4);


    }
}
