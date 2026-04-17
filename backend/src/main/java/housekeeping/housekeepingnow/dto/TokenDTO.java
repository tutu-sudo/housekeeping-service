package housekeeping.housekeepingnow.dto;

import lombok.Data;

/**
 * Token响应DTO
 */
@Data
public class TokenDTO {
    
    private String token;
    
    private String refreshToken;
    
    private Long userId;
    
    private String username;
    
    private Integer userType;
}

