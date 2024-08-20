package com.escanor.shiro.token;


import com.escanor.core.exception.ResponseException;
import com.escanor.shiro.util.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 构造jwt及解析jwt的工具类
 *
 * @author accelerator
 * @date 2024-04-09
 */
@Slf4j
public class JwtTokenTemplate {
    /**
     * 密钥
     */
    private final SecretKey secretKey;
    /**
     * Token超时时间，单位毫秒
     */
    private final Long tokenTimeOut;

    public JwtTokenTemplate(String key, int tokenTimeOut) {
        this.secretKey = deserializeKey(key);
        this.tokenTimeOut = 1000L * 60 * tokenTimeOut;
    }

    public String createJsonWebToken(Map<String, Object> claims, String subject) {
        LocalDateTime localDateTime = LocalDateTime.now();
        //添加构成JWT的参数
        return Jwts.builder().subject(subject).claims(claims).expiration(Date.from(localDateTime.plus(tokenTimeOut, ChronoUnit.MILLIS).atZone(ZoneId.systemDefault()).toInstant())).issuedAt(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())).signWith(secretKey, Jwts.SIG.HS512).compact();
    }

    private SecretKey deserializeKey(String encodedKey) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedKey));
    }

    public Map<String, Object> verifyToken(String jwtToken) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwtToken).getPayload();
    }

    public Map<String, Object> parseToken(String jwtToken) {
        String[] splitToken = jwtToken.split("\\.", -1);
        if (splitToken.length < 3) {
            throw new ResponseException("Invalid Token");
        }
        return Json.toBean(Decoders.BASE64.decode(splitToken[1]), new TypeReference<HashMap<String, Object>>() {
        });
    }
}
