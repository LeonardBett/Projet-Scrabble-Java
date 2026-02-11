package fr.u_bordeaux.scrabble.view.cli;

public class MessageRenderer {
        public void  error(String message) {
        System.out.println("❌ " + message);    
        }

        public void success(String message) {
        System.out.println("✅ " + message);
        }

        public void welcome() {
        System.out.println("Bienvenue dans le Scrabble CLI !");
        }

        public void info(String message) {
        System.out.println("ℹ️ " + message);
        }

        public void separator() {
        System.out.println("──────────────────────────────────────────────");
        }

        public void sectionTitle(String title) {
        System.out.println("\n=== " + title + " ===");
        }

        public void warning(String message) {
        System.out.println("⚠️ " + message);
        }
    
}
