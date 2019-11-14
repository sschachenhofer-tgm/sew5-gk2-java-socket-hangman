package server;

public class OfflineRandomWords implements RandomWordSource {

    private static final String[] words = {
            "Softwareentwicklung",
            "Nebenläufigkeit",
            "Datensynchronisation",
            "Interprozesskommunikation",
            "Vererbungshierarchie",
            "Schnittstelle",
            "Zugriffskapselung",
            "Laufzeitpolymorphie",
            "Sortieralgorithmen",
            "Anwendungsschnittstelle"
    };

    @Override
    public String randomWord() {
        return OfflineRandomWords.words[(int) (Math.random() * OfflineRandomWords.words.length)];
    }
}
