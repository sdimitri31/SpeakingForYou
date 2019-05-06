package g.android.speakingforyou;

class SavedSentences {

    // Notez que l'identifiant est un long
    private long id;
    private String mSentence;
    private String mLanguage;
    private int mPitch;
    private int mSpeechRate;

    SavedSentences(long id, String sentence, String language, int pitch, int speechRate) {
        super();
        this.id = id;
        this.mSentence = sentence;
        this.mLanguage = language;
        this.mPitch = pitch;
        this.mSpeechRate = speechRate;
    }

    SavedSentences(String sentence, String language, int pitch, int speechRate) {
        super();
        this.mSentence = sentence;
        this.mLanguage = language;
        this.mPitch = pitch;
        this.mSpeechRate = speechRate;
    }


    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    String getSentence() {
        return mSentence;
    }

    void setSentence(String sentence) {
        this.mSentence = sentence;
    }

    String getLanguage() {
        return mLanguage;
    }

    void setLanguage(String language) {
        this.mLanguage = language;
    }

    int getPitch() {
        return mPitch;
    }

    void setPitch(int pitch) {
        this.mPitch = pitch;
    }

    int getSpeechRate() {
        return mSpeechRate;
    }

    void setSpeechRate(int speechRate) {
        this.mSpeechRate = speechRate;
    }
}
