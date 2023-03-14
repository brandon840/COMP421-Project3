import java.sql.* ;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

class Soccer
{
    static String[] rounds = {"group-round", "round-of-16", "quarterfinals", "semifinals", "3rd-place", "final"};
    static void printMainMenu(){
        System.out.println("Soccer Main Menu");
        System.out.println("\t 1. List information of matches of a country");
        System.out.println("\t 2. Insert initial player information for a match");
        System.out.println("\t 3. Insert goal information");
        System.out.println("\t 4. Exit application");
        System.out.print("Please Enter Your Option: ");
    }

    static void listInfoCountry(int sqlCode, String sqlState, Statement statement, Connection con){
        Scanner reader = new Scanner(System.in);
        String choice;
        do {
            String country;


            Map<Integer,  ArrayList<String>> dict = new HashMap<>();
            System.out.print("Enter a country name: ");
            country = reader.nextLine();

            // Query to get team_country1, team_country2, match date and round_number
            try
            {
                String querySQL = "SELECT match_id, team1, team2, match_date, round_number\n" +
                        "FROM match\n" +
                        "WHERE team1 = '"+country+"' OR team2 = '"+country+"';";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String team_country1 = rs.getString (2);
                    String team_country2 = rs.getString (3);
                    String match_date = rs.getString (4);
                    int round_number = rs.getInt ( 5 );

                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date match_date_formatted = dateFormatter.parse(match_date);
                    Date todayDate = new Date();
                    ArrayList<String> content = new ArrayList<String>();

                    // Check if match date after current time
                    if (match_date_formatted.compareTo(todayDate) > 0){
                        Collections.addAll(content, team_country1, team_country2, match_date, rounds[round_number], "NULL", "NULL");
                        dict.put(match_id, content);
                    }else{
                        Collections.addAll(content, team_country1, team_country2, match_date, rounds[round_number]);
                        dict.put(match_id, content);
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
            } catch (ParseException e) {
                throw new RuntimeException(e);
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
                        "WHERE team_country = '"+country+"')\n" +
                        "GROUP BY t.match_id, t.team_country\n" +
                        ")\n" +
                        "SELECT r1.match_id, r1.team_country, r2.team_country, r1.num_goals_scored, r2.num_goals_scored\n" +
                        "FROM res r1 JOIN res r2 ON r1.match_id=r2.match_id\n" +
                        "WHERE r1.team_country != r2.team_country AND r1.team_country = '"+country+"';";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String team1 = rs.getString (2);
                    String team2 = rs.getString (3);
                    String num_goals_scored_team1 = Integer.toString(rs.getInt (4));
                    String num_goals_scored_team2 = Integer.toString(rs.getInt (5));

                    // Check if we already put null values for the score
                    if (dict.get(match_id).size() > 4){
                        continue;
                    }


                    // This is just in case the country orders are switched
                    if (dict.get(match_id).get(0).equals(team1)){
                        dict.get(match_id).add(num_goals_scored_team1);
                        dict.get(match_id).add(num_goals_scored_team2);
                    }else{
                        dict.get(match_id).add(num_goals_scored_team2);
                        dict.get(match_id).add(num_goals_scored_team1);
                    }

                }
                for(Integer key: dict.keySet()) {
                    ArrayList<String> val = dict.get(key);
                    if (val.size() <= 4){
                        val.add("0");
                        val.add("0");
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
                String querySQL = "SELECT pertainsTo.match_id, COUNT(pertainsTo.ticket_id) AS number_of_tickets\n" +
                        "FROM pertainsTo\n" +
                        "JOIN match ON pertainsTo.match_id=match.match_id\n" +
                        "WHERE match.team1 = '"+country+"' OR match.team2 = '"+country+"'\n" +
                        "GROUP BY pertainsTo.match_id;";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    int match_id = rs.getInt ( 1 );
                    String num_tickets_sold = Integer.toString(rs.getInt(2));
                    dict.get(match_id).add(num_tickets_sold);
                }

                for(Integer key: dict.keySet()) {
                    ArrayList<String> val = dict.get(key);
                    if (val.size() <= 6){
                        val.add("0");
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

            // Print query results
            for(Integer key: dict.keySet()){
                ArrayList<String> val = dict.get(key);
                String out = "";
                for (int i = 0; i<val.size(); i++){
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
            choice = reader.nextLine();

        }while(choice.equals("A"));


    }

    static void initialPlayerInfo(int sqlCode, String sqlState, Statement statement, Connection con) {

        // Print all matches occurring in the next three days
        System.out.println("Matches: ");
        try
        {
            String querySQL = "SELECT match_id, team1, team2, match_date, round_number\n" +
                    "FROM match\n" +
                    "WHERE (team1 IS NOT NULL OR team2 IS NOT NULL)\n" +
                    "AND match_date BETWEEN CURRENT DATE AND ADD_DAYS(CURRENT_DATE, 3);";

            //System.out.println (querySQL);
            java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

            while ( rs.next ( ) )
            {
                int match_id = rs.getInt ( 1 );
                String team1 = rs.getString (2);
                String team2 = rs.getString (3);
                String match_date = rs.getString (4);
                String round_number = rounds[rs.getInt(5)];

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
        String[] choice = reader.nextLine().split("\\s+");
        int matchID = Integer.parseInt(choice[0]);
        String country = choice[1];


        do {
            System.out.println("\nThe following players from " + country + " are already entered for match " + matchID + ":\n");
            int numPlayers = 0; // Need this to keep track of number of players in the match

            // Print players already entered in the match
            try
            {
                String querySQL = "SELECT person.name, player.shirt_num, plays.detailed_position, plays.minute_entered, plays.minute_left, plays.num_yellow_cards, plays.had_red_card\n" +
                        "FROM player \n" +
                        "JOIN person ON player.player_id=person.person_id\n" +
                        "JOIN plays ON player.player_id=plays.player_id\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "WHERE isPartOf.team_country = '"+ country +"'\n" +
                        "AND plays.match_id= "+matchID+";\n";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;

                while ( rs.next ( ) )
                {
                    String name = rs.getString ( 1 );
                    int shirt_num = rs.getInt (2);
                    String position = rs.getString (3);
                    int minute_entered = rs.getInt (4);
                    int minute_left = rs.getInt (5);
                    int num_yellow_cards = rs.getInt (6);
                    int num_red_cards = rs.getInt (7);
                    numPlayers++;

                    String out = name + "\t" + shirt_num + "\t" + position + "\tfrom minute: " + minute_entered + "\tto minute: " + minute_left+ "\tyellow: " + num_yellow_cards + "\tred: " + num_red_cards;
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
            System.out.println("\nPossible players from " + country+ " not yet selected:");
            ArrayList<String> remaining_player_names = new ArrayList<String>();
            ArrayList<Integer> remaining_player_ID = new ArrayList<Integer>();
            int i = 1;
            try
            {
                String querySQL = "SELECT person.name, player.shirt_num, player.position, player.player_id\n" +
                        "FROM player\n" +
                        "JOIN person ON player.player_id=person.person_id\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "WHERE isPartOf.team_country = '"+country+"'\n" +
                        "AND player.player_id NOT IN \n" +
                        "(SELECT plays.player_id \n" +
                        "FROM player\n" +
                        "JOIN isPartOf ON player.player_id=isPartOf.player_id\n" +
                        "JOIN plays ON player.player_id=plays.player_id\n" +
                        "WHERE isPartOf.team_country='"+country+"' AND plays.match_id = "+matchID+"\n" +
                        ");";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
                while ( rs.next ( ) )
                {
                    String name = rs.getString ( 1 );
                    remaining_player_names.add(name);

                    int shirt_num = rs.getInt (2);
                    String position = rs.getString (3);

                    int playerID = rs.getInt(4);    //Need this for later
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
                System.out.print("\nEnter the (choice) number of the player you want to insert or [P] to go to the previous menu: ");
                String input = reader.nextLine();
                if (input.equals("P")) {
                    return;
                }

                System.out.print("Enter the specific position the player will have: ");
                String detailedPosition = reader.nextLine();

                // Insert the player into "plays" table
                try
                {
                    String insertSQL = "INSERT INTO plays VALUES ("+matchID+","+ remaining_player_ID.get(Integer.parseInt(input) - 1) + ", 0, 0, 0, NULL, '"+ detailedPosition +"')";
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
    static void insertGoalInfo(int sqlCode, String sqlState, Statement statement, Connection con){

        while(true) {
            System.out.print("Enter the match id: ");
            Scanner reader = new Scanner(System.in);
            int match_id = Integer.parseInt(reader.nextLine());
            System.out.println();
            System.out.println("Here are all the goals scored in match "+ match_id +": \n");

            // Show goal information for chosen match
            try
            {
                String querySQL = "SELECT p.name, i.team_country,g.minute, \n" +
                        "CASE\n" +
                        "\tWHEN g.during_penalty_kicks=0 THEN 'NO'\n" +
                        "\tELSE 'YES'\n" +
                        "END AS penalty_kick\n" +
                        ", g.occurrence\n" +
                        "FROM goal g\n" +
                        "JOIN person p ON g.player_id=p.person_id\n" +
                        "JOIN isPartOf i ON g.player_id=i.player_id\n" +
                        "JOIN scoredIn s ON g.goal_id=s.goal_id\n" +
                        "WHERE s.match_id = "+match_id+"\n" +
                        "ORDER BY g.occurrence ASC;";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery ( querySQL ) ;
                while ( rs.next ( ) )
                {
                    String playerName = rs.getString(1);
                    String teamCountry = rs.getString(2);
                    int playMinute = rs.getInt(3);
                    String penaltyKickYN = rs.getString(4);
                    int playOccurrence = rs.getInt(5);

                    String out = playerName + "\t" + teamCountry+ "\tMinute Scored: " + playMinute+ "\tpenalty kick: " + penaltyKickYN + "\tOccurrence: " + playOccurrence;
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

            System.out.print("\nEnter [G] to add another goal to this match, press [P] to return to main menu:");
            String a = reader.nextLine();
            if (a.equals("P")){
                return;
            }

            System.out.print("Enter the country of the team that scored: ");
            String country = reader.nextLine();

            System.out.print("Enter the name of the player who scored: ");
            String name = reader.nextLine();
            System.out.println();

            System.out.print("Enter the time (minute) of the goal: ");
            int minute = Integer.parseInt(reader.nextLine());
            System.out.println();

            System.out.print("Was the goal scored during penalty kicks? (y/n): ");
            String input = reader.nextLine();
            String penalty = "";

            if (input.equals("y")) {
                penalty = "1";
            } else {
                penalty = "0";
            }
            System.out.println();

            // Need to get some info first
            int numOccurrences = 0;
            int numGoals = 0;
            int playerID = 0;
            try {
                String querySQL = "SELECT COUNT(*) FROM goal";

                //System.out.println (querySQL);
                java.sql.ResultSet rs = statement.executeQuery(querySQL);
                while (rs.next()) {
                    numGoals = rs.getInt(1);
                }

                querySQL = "SELECT COUNT(*)\n" +
                        "FROM goal g\n" +
                        "JOIN scoredIn s ON g.goal_id, s.goal_id\n" +
                        "WHERE s.match_id = " + match_id + ";";

                rs = statement.executeQuery(querySQL);
                while (rs.next()) {
                    numOccurrences = rs.getInt(1);
                }

                querySQL = "SELECT player.player_id\n" +
                        "FROM person\n" +
                        "JOIN player ON person.person_id = player.player_id\n" +
                        "WHERE person.name = '" + name + "';";
                rs = statement.executeQuery(querySQL);
                while (rs.next()) {
                    playerID = rs.getInt(1);
                }
            } catch (SQLException e) {
                sqlCode = e.getErrorCode(); // Get SQLCODE
                sqlState = e.getSQLState(); // Get SQLSTATE

                // Your code to handle errors comes here;
                // something more meaningful than a print would be good
                System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                System.out.println(e);
            }

            // Insert the goal!
            try {
                // Update "goal" table
                String insertSQL = "INSERT INTO goal VALUES (" + numGoals + "," + minute + "," + numOccurrences + 1 + "," + penalty + "," + playerID + ")";
                statement.executeUpdate(insertSQL);

                // Update "scoredIn" table
                insertSQL = "INSERT INTO scoredIn VALUES(" + numGoals + "," + match_id + ")";
                statement.executeUpdate(insertSQL);

            } catch (SQLException e) {
                sqlCode = e.getErrorCode(); // Get SQLCODE
                sqlState = e.getSQLState(); // Get SQLSTATE

                // Your code to handle errors comes here;
                // something more meaningful than a print would be good
                System.out.println("Code: " + sqlCode + "  sqlState: " + sqlState);
                System.out.println(e);
            }


        }
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
                insertGoalInfo(sqlCode, sqlState, statement, con);

            }else{
                // Close statement and connection
                statement.close ( ) ;
                con.close ( ) ;
            }

        } while(choice != 4);


    }
}
