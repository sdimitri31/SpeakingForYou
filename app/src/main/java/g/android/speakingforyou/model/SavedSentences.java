package g.android.speakingforyou.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavedSentences {

    private long id;
    private String mSentence;
    private int mPosition;
    private Date mDate ;
    private int mUsage;

    SavedSentences(long id, String sentence, int position, String stringDate, int usage) {
        super();
        this.id = id;
        this.mSentence = sentence;
        this.mPosition = position;
        this.mUsage = usage;
        setDate(stringDate);
    }

    public SavedSentences(String sentence,  int position, String stringDate, int usage) {
        super();
        this.mSentence = sentence;
        this.mPosition = position;
        this.mUsage = usage;
        setDate(stringDate);
    }

    public SavedSentences(String sentence,  int position, Date date, int usage) {
        super();
        this.mSentence = sentence;
        this.mPosition = position;
        this.mUsage = usage;
        this.mDate = date;
    }

    public SavedSentences(SavedSentences savedSentences) {
        super();
        this.id = savedSentences.getId();
        this.mSentence = savedSentences.getSentence();
        this.mPosition = savedSentences.getPosition();
        this.mDate = savedSentences.getDate();
        this.mUsage = savedSentences.getUsage();
    }

    public long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    public String getSentence() {
        return mSentence;
    }

    void setSentence(String sentence) {
        this.mSentence = sentence;
    }

    int getPosition() {
        return mPosition;
    }

    void setPosition(int position) {
        this.mPosition = position;
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
