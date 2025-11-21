package com.walter.myinternshiplogbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class LogDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "internship_logs.db";
    private static final int DATABASE_VERSION = 1;

    public LogDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table for student logs including supervisor feedback
        db.execSQL("CREATE TABLE student_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "day TEXT, " +
                "reg_no TEXT, " +
                "student_name TEXT, " +
                "activity TEXT, " +
                "problem TEXT, " +
                "solutions TEXT, " +
                "skills TEXT, " +
                "feedback TEXT, " +  // supervisor feedback
                "evidence_image BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS student_logs");
        onCreate(db);
    }

    // ✅ Insert a new log
    public boolean insertLog(String date, String day, String regNo, String studentName,
                             String activity, String problem, String solutions, String skills,
                             byte[] evidenceImage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("day", day);
        values.put("reg_no", regNo);
        values.put("student_name", studentName);
        values.put("activity", activity);
        values.put("problem", problem);
        values.put("solutions", solutions);
        values.put("skills", skills);
        values.put("evidence_image", evidenceImage);
        values.putNull("feedback"); // initially empty

        long result = db.insert("student_logs", null, values);
        db.close();
        return result != -1;
    }

    public boolean saveFeedback(String studentName, String feedback, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("feedback", feedback);

        int rowsUpdated = db.update(
                "student_logs",
                values,
                "student_name = ? AND date = ?",
                new String[]{studentName, date}
        );

        db.close();
        return rowsUpdated > 0;
    }

    // ✅ Save feedback for the latest log entry of a student (helper)
    public boolean saveFeedbackForLatestLog(String studentName, String feedback) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Find the id of the most recent log for the student
            Cursor cursor = db.rawQuery(
                    "SELECT id FROM student_logs WHERE student_name = ? ORDER BY date DESC LIMIT 1",
                    new String[]{studentName}
            );
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                ContentValues values = new ContentValues();
                values.put("feedback", feedback);
                int rows = db.update("student_logs", values, "id = ?", new String[]{String.valueOf(id)});
                cursor.close();
                db.setTransactionSuccessful();
                return rows > 0;
            } else {
                // No log found
                cursor.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // ✅ Fetch only student names who have daily logs
    public ArrayList<String> getDistinctStudentNamesFromLogs() {
        ArrayList<String> students = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT DISTINCT student_name FROM student_logs WHERE student_name IS NOT NULL AND TRIM(student_name) != ''", null);

        if (cursor.moveToFirst()) {
            do {
                students.add(cursor.getString(cursor.getColumnIndexOrThrow("student_name")));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return students;
    }


    // ✅ Fetch all logs for a specific student
    public ArrayList<String> getLogsByStudent(String studentName) {
        ArrayList<String> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT date, day, activity, problem, solutions, skills FROM student_logs WHERE student_name = ?",
                new String[]{studentName}
        );

        if (cursor.moveToFirst()) {
            do {
                String log = "Date: " + cursor.getString(cursor.getColumnIndexOrThrow("date")) +
                        "\nDay: " + cursor.getString(cursor.getColumnIndexOrThrow("day")) +
                        "\nActivity: " + cursor.getString(cursor.getColumnIndexOrThrow("activity")) +
                        "\nProblem: " + cursor.getString(cursor.getColumnIndexOrThrow("problem")) +
                        "\nSolutions: " + cursor.getString(cursor.getColumnIndexOrThrow("solutions")) +
                        "\nSkills: " + cursor.getString(cursor.getColumnIndexOrThrow("skills")) +
                        "\n-----------------------------";
                logs.add(log);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logs;
    }

    // ✅ Fetch all logs for a specific student, including images
    public ArrayList<Log> getLogsWithImagesByStudent(String studentName) {
        ArrayList<Log> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT date, day, activity, problem, solutions, skills, evidence_image FROM student_logs WHERE student_name = ?",
                new String[]{studentName}
        );

        if (cursor.moveToFirst()) {
            do {
                String logContent = "Date: " + cursor.getString(cursor.getColumnIndexOrThrow("date")) +
                        "\nDay: " + cursor.getString(cursor.getColumnIndexOrThrow("day")) +
                        "\nActivity: " + cursor.getString(cursor.getColumnIndexOrThrow("activity")) +
                        "\nProblem: " + cursor.getString(cursor.getColumnIndexOrThrow("problem")) +
                        "\nSolutions: " + cursor.getString(cursor.getColumnIndexOrThrow("solutions")) +
                        "\nSkills: " + cursor.getString(cursor.getColumnIndexOrThrow("skills")) +
                        "\n-----------------------------";
                byte[] evidenceImage = cursor.getBlob(cursor.getColumnIndexOrThrow("evidence_image"));
                logs.add(new Log(logContent, evidenceImage));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logs;
    }

    // ✅ Fetch all feedback entries for a specific student
    public ArrayList<String> getFeedbacksForStudent(String studentName) {
        ArrayList<String> feedbacks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT feedback, date FROM student_logs WHERE student_name = ? AND feedback IS NOT NULL AND TRIM(feedback) != '' ORDER BY date DESC",
                new String[]{studentName}
        );

        if (cursor.moveToFirst()) {
            do {
                String feedback = "Date: " + cursor.getString(cursor.getColumnIndexOrThrow("date")) +
                        "\nFeedback: " + cursor.getString(cursor.getColumnIndexOrThrow("feedback")) +
                        "\n----------------------";
                feedbacks.add(feedback);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return feedbacks;
    }

    // ✅ Fetch all logs (for debugging or supervisor overview)
    public ArrayList<String> getAllLogs() {
        ArrayList<String> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM student_logs", null);

        if (cursor.moveToFirst()) {
            do {
                String log = cursor.getString(cursor.getColumnIndexOrThrow("student_name")) + " - " +
                        cursor.getString(cursor.getColumnIndexOrThrow("activity"));
                logs.add(log);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logs;
    }

    // ✅ Fetch all logs with images
    public ArrayList<Log> getAllLogsWithImages() {
        ArrayList<Log> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT date, day, activity, problem, solutions, skills, evidence_image FROM student_logs", null);

        if (cursor.moveToFirst()) {
            do {
                String logContent = "Date: " + cursor.getString(cursor.getColumnIndexOrThrow("date")) +
                        "\nDay: " + cursor.getString(cursor.getColumnIndexOrThrow("day")) +
                        "\nActivity: " + cursor.getString(cursor.getColumnIndexOrThrow("activity")) +
                        "\nProblem: " + cursor.getString(cursor.getColumnIndexOrThrow("problem")) +
                        "\nSolutions: " + cursor.getString(cursor.getColumnIndexOrThrow("solutions")) +
                        "\nSkills: " + cursor.getString(cursor.getColumnIndexOrThrow("skills")) +
                        "\n-----------------------------";
                byte[] evidenceImage = cursor.getBlob(cursor.getColumnIndexOrThrow("evidence_image"));
                logs.add(new Log(logContent, evidenceImage));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logs;
    }
}
