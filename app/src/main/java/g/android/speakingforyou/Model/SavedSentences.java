package g.android.speakingforyou.Model;

public class SavedSentences {

    private long id;
    private String mSentence;
    private int mPosition;

    SavedSentences(long id, String sentence, int position) {
        super();
        this.id = id;
        this.mSentence = sentence;
        this.mPosition = position;
    }

    public SavedSentences(String sentence,  int position) {
        super();
        this.mSentence = sentence;
        this.mPosition = position;
    }

    public SavedSentences(SavedSentences savedSentences) {
        super();
        this.id = savedSentences.getId();
        this.mSentence = savedSentences.getSentence();
        this.mPosition = savedSentences.getPosition();
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
}
