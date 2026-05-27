package com._ya.neoliz.application.service;

import com._ya.neoliz.domain.User;
import com._ya.neoliz.global.exception.DuplicateNicknameException;
import com._ya.neoliz.global.exception.UserNotFoundException;
import com._ya.neoliz.persistence.repository.UserRepository;
import com._ya.neoliz.presentation.dto.request.UpdateNicknameRequest;
import com._ya.neoliz.presentation.dto.response.UpdateNicknameResponse;
import com._ya.neoliz.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public UserResponse findById(Long id){
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("조회 실패"));
        return UserResponse.from(user);
    }

    @Transactional
    public UpdateNicknameResponse updateNickname(Long id, UpdateNicknameRequest request) {
        String newNickname = request.getNickname();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("조회 실패"));
        if (userRepository.existsByNicknameAndIdNot(newNickname, id)) { throw new DuplicateNicknameException("닉네임 중복"); }
        user.updateNickname(newNickname);
        return UpdateNicknameResponse.from(user);
    }
}
