package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Document {

    private String file_path;
    private int start_line;
    private String  docNumber;
    private Date date;
    private String title;
    private String text;

    public Document() {
    }

    public Document(String file_path, int start_line, String docNumber, Date date, String title) {
        this.file_path = file_path;
        this.start_line = start_line;
        this.docNumber = docNumber;
        this.date = date;
        this.title = title;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public void setStart_line(int start_line) {
        this.start_line = start_line;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public void setDate(String date) {
        DateFormat format = new SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH);
        try {
            this.date = format.parse(date);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Document{" +
                "file_path='" + file_path + '\'' +
                ", start_line=" + start_line +
                ", docNumber='" + docNumber + '\'' +
                ", date=" + date +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
