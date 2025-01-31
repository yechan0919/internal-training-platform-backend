package com.posco.education.service;

import com.posco.education.domain.dto.TokenDto;
import com.posco.education.domain.dto.UserLoginResponse;
import com.posco.education.domain.entity.Lecture;
import com.posco.education.domain.entity.Point;
import com.posco.education.domain.entity.User;
import com.posco.education.jwt.JwtTokenProvider;
import com.posco.education.repository.LectureRepository;
import com.posco.education.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 1. 로그인 요청으로 들어온 ID, PWD 기반으로 Authentication 객체 생성
     * 2. authenticate() 메서드를 통해 요청된 Member에 대한 검증이 진행 => loadUserByUsername 메서드를 실행. 해당 메서드는 검증을 위한 유저 객체를 가져오는 부분으로써, 어떤 객체를 검증할 것인지에 대해 직접 구현
     * 3. 검증이 정상적으로 통과되었다면 인증된 Authentication객체를 기반으로 JWT 토큰을 생성
     */
    @Transactional
    public UserLoginResponse login(String memberId, String password) {
        UserLoginResponse loginResponse = new UserLoginResponse();

        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberId, password);

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateToken(authentication);

        if (tokenDto != null) {
            Optional<User> users = userRepository.findByUserId(memberId);
            if (users.isPresent()) {
                User user = users.get();
                Point point = user.getPoint();
                Integer totalPoint = (point.getFinanceP() + point.getProductionP() + point.getItP() + point.getMarketingP() + point.getLanguageP());

                loginResponse.setTokenDto(tokenDto);
                loginResponse.setTotalPoint(totalPoint);
            }
        } else {
            System.out.println("null - tokenDto");
        }

        return loginResponse;
    }

    @Transactional
    public String join(User user) {
        userRepository.save(user);
        return user.getUserId();
    }

}
