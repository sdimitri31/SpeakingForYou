package g.android.speakingforyou.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class History {

    private long id;
    private String mSentence;
    private Date mDate ;
    private int mUsage;

    History(long id, String sentence, String stringDate, int usage) {
        super();
        this.id = id;
        this.mSentence = sentence;
        this.mUsage = usage;
        setDate(stringDate);
    }

    public History(String sentence, String stringDate, int usage) {
        super();
        this.mSentence = sentence;
        setDate(stringDate);
        this.mUsage = usage;
    }

    public History(String sentence, Date date, int usage) {
        super();
        this.mSentence = sentence;
        this.mDate = date;
        this.mUsage = usage;
    }

    public History(History history) {
        super();
        this.id = history.getId();
        this.mSentence = history.getSentence();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSentence() {
        return mSentence;
    }

    public void setSentence(String sentence) {
        this.mSentence = sentence;
    }

    public Date getDate(){ return mDate;}

    public String getStringDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return dateFormat.format(mDate);
    }

    public void setDate(String stringDate){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date date = format.parse(stringDate);
            this.mDate = date;
            System.out.println(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void setDate(Date date){
        this.mDate = date;
    }

    public int getUsage(){ return mUsage;}

    public void setUsage(int usage){
        this.mUsage = usage;
    }
}
