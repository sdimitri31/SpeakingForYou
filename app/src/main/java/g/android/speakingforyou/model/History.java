package g.android.speakingforyou.model;

public class History {

    private long id;
    private String mSentence;

    History(long id, String sentence) {
        super();
        this.id = id;
        this.mSentence = sentence;
    }

    public History(String sentence) {
        super();
        this.mSentence = sentence;
    }

    public History(History history) {
        super();
        this.id = history.getId();
        this.mSentence = history.getSentence();
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

}
