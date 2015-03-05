import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import javax.xml.crypto.Data;

public class DBManager {

    public static final String default_db_file = "line_intersection.db";

    File dbFile;

    public DBManager() {
        this(default_db_file);
    }

    public DBManager(String dbFilename) {
        // Init the connection
        dbFile = new File(dbFilename);
        try {
            dbFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create db file " + dbFilename);
            e.printStackTrace();
            System.exit(1);
        }
        createTables();
    }

    public SQLiteConnection openDb(boolean allowCreate) throws SQLiteException
    {
        SQLiteConnection db = new SQLiteConnection(dbFile);
        db.open(allowCreate);
        db.exec("PRAGMA foreign_keys = ON;");
        return db;
    }

    public void createTables()
    {
        SQLiteConnection db = null;
        try {
            db = openDb(true);
            db.exec(Subjects_Table.table_creation_code);
            db.exec(Data_Table.table_creation_code);
        } catch (SQLiteException e) {
            System.out.println("Failed to create one of the tables");
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.dispose();
        }
    }

    // Subject stuff
    public void addSubject(Subject subject)
    {
        SQLiteConnection db = null;
        try {
            db = openDb(true);
        } catch (SQLiteException e) {
            System.err.println("Failed to open database file: " + dbFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        try {
            String query = "INSERT INTO " + Subjects_Table.table_name + "("
                    + Subjects_Table.Column.subject_name.name + ", "
                    + Subjects_Table.Column.right_handed.name + ", "
                    + Subjects_Table.Column.glasses.name + ", "
                    + Subjects_Table.Column.wearing_glasses.name
                    + ") "
                    + "VALUES( ?, ?, ?, ? );";

            SQLiteStatement st = db.prepare(query);
            // Perform binding
            st.bind(1, subject.name);
            st.bind(2, subject.rightHanded ? 1: 0);
            st.bind(3, subject.hasGlasses ? 1 : 0);
            st.bind(4, subject.hasGlasses && subject.glassesOn ? 1 : 0);
            st.step();
            st.dispose();
            subject.uniqueId = (int) db.getLastInsertId();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.dispose();
        }
        return;
    }

    public List<Subject> getSubjects()
    {
        List<Subject> subjects = new ArrayList<Subject>();

        SQLiteConnection db = null;
        try {
            db = openDb(false);
        } catch (SQLiteException e) {
            System.err.println("Failed to open database file: " + dbFile.getAbsolutePath());
            e.printStackTrace();
            return subjects;
        }

        String query_str = "SELECT * from " + Subjects_Table.table_name + ";";

        try {
            SQLiteStatement query = db.prepare(query_str);
            while(query.step())
            {
                String subjectName = query.columnString(Subjects_Table.Column.subject_name.ordinal());
                boolean rightHanded = query.columnInt(Subjects_Table.Column.right_handed.ordinal()) == 1;
                boolean glasses = query.columnInt(Subjects_Table.Column.glasses.ordinal()) == 1;
                boolean wearingGlasses = query.columnInt(Subjects_Table.Column.wearing_glasses.ordinal()) == 1;
                int subjectId = query.columnInt(Subjects_Table.Column._id.ordinal());
                subjects.add(new Subject(subjectName, rightHanded, glasses, wearingGlasses, subjectId));
            }
            query.dispose();
        } catch (SQLiteException e) {
            System.err.println("Failed to fetch subjects from database");
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.dispose();
        }
        return subjects;
    }

    // Data stuff
    public void addData(int subjectId, boolean correctHand, int[] line_lengths, int[] clicked_lengths)
    {
        SQLiteConnection db = null;
        try {
            db = openDb(true);
        } catch (SQLiteException e) {
            System.err.println("Failed to open database file: " + dbFile.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        // Build the query
        String query = "INSERT INTO " + Data_Table.table_name + "("
                + Data_Table.Column.subject_id.name + ", "
                + Data_Table.Column.correct_hand.name;
        String subQuery = "VALUES( ?, ? ";
        Data_Table.Column[] columns = Data_Table.Column.values();
        for(int i = 2; i < columns.length; i++)
        {
            query += (", " + columns[i].name);
            subQuery += ", ?";
        }
        query += ") " + subQuery + ");";

        try {
            SQLiteStatement st = db.prepare(query);
            // Perform binding
            st.bind(1, subjectId);
            st.bind(2, correctHand ? 1 : 0);

            for (int i = 0; i < line_lengths.length; i++) {
                int bindIndex = (i * 2) + 3;
                st.bind(bindIndex, line_lengths[i]);
                st.bind(bindIndex + 1, clicked_lengths[i]); // convert to mm
            }
            st.step();
            st.dispose();

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.dispose();
        }
        return;
    }

    public Map<Subject, Helper.ResultData> getAllResultData()
    {
        // First build a map of subject id to subjects
        Map<Integer, Subject> subjectIdToSubjectMap = new HashMap<Integer, Subject>();
        {
            List<Subject> subjects = getSubjects();
            for(Subject s : subjects)
                subjectIdToSubjectMap.put(s.uniqueId, s);
        }

        Map<Subject, Helper.ResultData> ret = new HashMap<Subject, Helper.ResultData>();

        SQLiteConnection db = null;
        try {
            db = openDb(false);
        } catch (SQLiteException e) {
            System.err.println("Failed to open database file: " + dbFile.getAbsolutePath());
            e.printStackTrace();
            return ret;
        }

        String query_str = "SELECT * from " + Data_Table.table_name + ";";
        Data_Table.Column[] columns = Data_Table.Column.values();

        try {
            SQLiteStatement query = db.prepare(query_str);
            while(query.step())
            {
                int subjectId = query.columnInt(Data_Table.Column.subject_id.ordinal());
                Subject s = subjectIdToSubjectMap.get(subjectId);
                Helper.ResultData data;

                if((data = ret.get(s)) == null)
                {
                    data = new Helper.ResultData();
                    ret.put(s, data);
                }

                boolean correctHand = query.columnInt(Data_Table.Column.correct_hand.ordinal()) == 1;
                int runIndex = correctHand ? 0 : 1;

                for(int i = 0; i < 18; i++)
                {
                    int columnIndex = (i*2)+2;
                    data.line_lengths[i] = query.columnInt(columns[columnIndex].ordinal());
                    data.clicked_lengths[runIndex][i] = query.columnInt(columns[columnIndex+1].ordinal());
                }
            }
            query.dispose();
        } catch (SQLiteException e) {
            System.err.println("Failed to fetch subjects from database");
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.dispose();
        }
        return ret;
    }


    private static class Subjects_Table  {
        public static String table_name = "subjects";

        public static enum Column{
            _id("id", SQLITE_TYPE.INTEGER), subject_name("subject_name", SQLITE_TYPE.TEXT),
            right_handed("right_handed", SQLITE_TYPE.INTEGER), glasses("glasses", SQLITE_TYPE.INTEGER),
            wearing_glasses("wearing_glasses", SQLITE_TYPE.INTEGER);

            Column(String name, String type)
            {
                this.name = name;
                this.type = type;
            }
            String name;
            String type;
        }

        public static final String table_creation_code =
                "CREATE TABLE IF NOT EXISTS " + table_name + "(" +
                        Column._id.name + " " + Column._id.type + " PRIMARY KEY," +
                        Column.subject_name.name + " " + Column.subject_name.type + "," +
                        Column.right_handed.name + " " + Column.right_handed.type + "," +
                        Column.glasses.name + " " + Column.glasses.type + "," +
                        Column.wearing_glasses.name + " " + Column.wearing_glasses.type +
                        ");";
    }

    private static class Data_Table  {
        public static String table_name = "data";

        public static enum Column{
            subject_id("subject_id", SQLITE_TYPE.INTEGER), correct_hand("correct_hand", SQLITE_TYPE.INTEGER),
            line_1_length("line_1_length", SQLITE_TYPE.INTEGER), line_1_intersected_length("line_1_intersected_length", SQLITE_TYPE.INTEGER),
            line_2_length("line_2_length", SQLITE_TYPE.INTEGER), line_2_intersected_length("line_2_intersected_length", SQLITE_TYPE.INTEGER),
            line_3_length("line_3_length", SQLITE_TYPE.INTEGER), line_3_intersected_length("line_3_intersected_length", SQLITE_TYPE.INTEGER),
            line_4_length("line_4_length", SQLITE_TYPE.INTEGER), line_4_intersected_length("line_4_intersected_length", SQLITE_TYPE.INTEGER),
            line_5_length("line_5_length", SQLITE_TYPE.INTEGER), line_5_intersected_length("line_5_intersected_length", SQLITE_TYPE.INTEGER),
            line_6_length("line_6_length", SQLITE_TYPE.INTEGER), line_6_intersected_length("line_6_intersected_length", SQLITE_TYPE.INTEGER),
            line_7_length("line_7_length", SQLITE_TYPE.INTEGER), line_7_intersected_length("line_7_intersected_length", SQLITE_TYPE.INTEGER),
            line_8_length("line_8_length", SQLITE_TYPE.INTEGER), line_8_intersected_length("line_8_intersected_length", SQLITE_TYPE.INTEGER),
            line_9_length("line_9_length", SQLITE_TYPE.INTEGER), line_9_intersected_length("line_9_intersected_length", SQLITE_TYPE.INTEGER),
            line_10_length("line_10_length", SQLITE_TYPE.INTEGER), line_10_intersected_length("line_10_intersected_length", SQLITE_TYPE.INTEGER),
            line_11_length("line_11_length", SQLITE_TYPE.INTEGER), line_11_intersected_length("line_11_intersected_length", SQLITE_TYPE.INTEGER),
            line_12_length("line_12_length", SQLITE_TYPE.INTEGER), line_12_intersected_length("line_12_intersected_length", SQLITE_TYPE.INTEGER),
            line_13_length("line_13_length", SQLITE_TYPE.INTEGER), line_13_intersected_length("line_13_intersected_length", SQLITE_TYPE.INTEGER),
            line_14_length("line_14_length", SQLITE_TYPE.INTEGER), line_14_intersected_length("line_14_intersected_length", SQLITE_TYPE.INTEGER),
            line_15_length("line_15_length", SQLITE_TYPE.INTEGER), line_15_intersected_length("line_15_intersected_length", SQLITE_TYPE.INTEGER),
            line_16_length("line_16_length", SQLITE_TYPE.INTEGER), line_16_intersected_length("line_16_intersected_length", SQLITE_TYPE.INTEGER),
            line_17_length("line_17_length", SQLITE_TYPE.INTEGER), line_17_intersected_length("line_17_intersected_length", SQLITE_TYPE.INTEGER),
            line_18_length("line_18_length", SQLITE_TYPE.INTEGER), line_18_intersected_length("line_18_intersected_length", SQLITE_TYPE.INTEGER);

            Column(String name, String type)
            {
                this.name = name;
                this.type = type;
            }
            String name;
            String type;
        }

        public static final String table_creation_code =
                "CREATE TABLE IF NOT EXISTS " + table_name + "(" +
                        Column.subject_id.name + " " + Column.subject_id.type + "," +
                        Column.correct_hand.name + " " + Column.correct_hand.type + "," +

                        Column.line_1_length.name + " " + Column.line_1_length.type + "," +
                        Column.line_1_intersected_length.name + " " + Column.line_1_intersected_length.type + "," +

                        Column.line_2_length.name + " " + Column.line_2_length.type + "," +
                        Column.line_2_intersected_length.name + " " + Column.line_2_intersected_length.type + "," +

                        Column.line_3_length.name + " " + Column.line_3_length.type + "," +
                        Column.line_3_intersected_length.name + " " + Column.line_3_intersected_length.type + "," +

                        Column.line_4_length.name + " " + Column.line_4_length.type + "," +
                        Column.line_4_intersected_length.name + " " + Column.line_4_intersected_length.type + "," +

                        Column.line_5_length.name + " " + Column.line_5_length.type + "," +
                        Column.line_5_intersected_length.name + " " + Column.line_5_intersected_length.type + "," +

                        Column.line_6_length.name + " " + Column.line_6_length.type + "," +
                        Column.line_6_intersected_length.name + " " + Column.line_6_intersected_length.type + "," +

                        Column.line_7_length.name + " " + Column.line_7_length.type + "," +
                        Column.line_7_intersected_length.name + " " + Column.line_7_intersected_length.type + "," +

                        Column.line_8_length.name + " " + Column.line_8_length.type + "," +
                        Column.line_8_intersected_length.name + " " + Column.line_8_intersected_length.type + "," +

                        Column.line_9_length.name + " " + Column.line_9_length.type + "," +
                        Column.line_9_intersected_length.name + " " + Column.line_9_intersected_length.type + "," +

                        Column.line_10_length.name + " " + Column.line_10_length.type + "," +
                        Column.line_10_intersected_length.name + " " + Column.line_10_intersected_length.type + "," +

                        Column.line_11_length.name + " " + Column.line_11_length.type + "," +
                        Column.line_11_intersected_length.name + " " + Column.line_11_intersected_length.type + "," +

                        Column.line_12_length.name + " " + Column.line_12_length.type + "," +
                        Column.line_12_intersected_length.name + " " + Column.line_12_intersected_length.type + "," +

                        Column.line_13_length.name + " " + Column.line_13_length.type + "," +
                        Column.line_13_intersected_length.name + " " + Column.line_13_intersected_length.type + "," +

                        Column.line_14_length.name + " " + Column.line_14_length.type + "," +
                        Column.line_14_intersected_length.name + " " + Column.line_14_intersected_length.type + "," +

                        Column.line_15_length.name + " " + Column.line_15_length.type + "," +
                        Column.line_15_intersected_length.name + " " + Column.line_15_intersected_length.type + "," +

                        Column.line_16_length.name + " " + Column.line_16_length.type + "," +
                        Column.line_16_intersected_length.name + " " + Column.line_16_intersected_length.type + "," +

                        Column.line_17_length.name + " " + Column.line_17_length.type + "," +
                        Column.line_17_intersected_length.name + " " + Column.line_17_intersected_length.type + "," +

                        Column.line_18_length.name + " " + Column.line_18_length.type + "," +
                        Column.line_18_intersected_length.name + " " + Column.line_18_intersected_length.type + "," +

                        "FOREIGN KEY(" +Column.subject_id.name + ") REFERENCES " + Subjects_Table.table_name + "(" + Subjects_Table.Column._id.name + ") ON UPDATE CASCADE ON DELETE CASCADE" +
                        ");";
    }

    private static class SQLITE_TYPE{
        private static final String NULL = "NULL";
        private static final String INTEGER = "INTEGER";
        private static final String REAL = "REAL";
        private static final String TEXT = "TEXT";
        private static final String BLOB = "BLOB";
    }
}