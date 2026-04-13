import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class CheckHash {
    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$gqHrslMttQWSsDSVRTK1OehkkBCxMfNqOGCsOlOOhVDGYgkiMQxu2";
        System.out.println("Matches 'password': " + encoder.matches("password", hash));
        System.out.println("New hash: " + encoder.encode("password"));
    }
}
