package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.UserNotFoundException;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    private final UserRepository userRepository;
    public UserResponse findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("조회 실패"));
        return UserResponse.from(user);
    }
}
