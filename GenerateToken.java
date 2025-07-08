import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GenerateToken {
    private static final String SECRET = "biangqiang-fresh-delivery-secret-key-2024";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7天
    
    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("openId", "test-openid");
        claims.put("userType", "USER");
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        
        System.out.println("Generated JWT Token:");
        System.out.println(token);
    }
}