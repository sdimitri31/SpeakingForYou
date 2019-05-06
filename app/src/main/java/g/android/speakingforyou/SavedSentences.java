package g.android.speakingforyou;

public class SavedSentences {

    // Notez que l'identifiant est un long
    private long id;
    private String mSentence;
    private String mLanguage;
    private int mPitch;
    private int mSpeechRate;

    public SavedSentences(long id, String sentence, String language, int pitch, int speechRate) {
        super();
        this.id = id;
        this.mSentence = sentence;
        this.mLanguage = language;
        this.mPitch = pitch;
        this.mSpeechRate = speechRate;
    }

    public SavedSentences(String sentence, String language, int pitch, int speechRate) {
        super();
        this.mSentence = sentence;
        this.mLanguage = language;
        this.mPitch = pitch;
        this.mSpeechRate = speechRate;
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

    public String getLanguage() {
        return mLanguage;
    }

    public void setLanguage(String language) {
        this.mLanguage = language;
    }

    public int getPitch() {
        return mPitch;
    }

    public void setPitch(int pitch) {
        this.mPitch = pitch;
    }

    public int getSpeechRate() {
        return mSpeechRate;
    }

    public void setSpeechRate(int speechRate) {
        this.mSpeechRate = speechRate;
    }
}
