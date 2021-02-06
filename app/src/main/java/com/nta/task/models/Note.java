package com.nta.task.models;

public class Note {

    public static final String USER_TABLE_NAME = "users";

    public static final String USER_ID = "user_id";
    public static final String TABLE_NAME = "notes";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String note;
    private String timestamp;








//    public static final String CREATE_USER_TABLE =
//            "CREATE TABLE " + USER_TABLE_NAME + "("
//                    + USER_NO+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
//                    + USER_ID + " TEXT"
//                    + ")";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NOTE + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    +USER_ID + " TEXT "
//                    +"FOREIGN KEY("+USER_ID+") REFERENCES "+USER_TABLE_NAME+"("+USER_ID+")"
                    + ")";


    public Note() {
    }

    public Note(int id, String note, String timestamp) {
        this.id = id;
        this.note = note;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

//    public static String getUserId() {
//        //TODO implement shared preferences user id logic here
//        return "";
//    }

}
